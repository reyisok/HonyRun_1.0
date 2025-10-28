package com.honyrun.service.security;

import java.time.Duration;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.honyrun.util.LoggingUtil;

/**
 * 响应式IP过滤服务
 * 基于Redis实现的分布式IP白名单/黑名单机制
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-13 15:40:00
 * @version 1.0.0
 */
@Service
public class ReactiveIpFilterService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveIpFilterService.class);

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    // Redis键前缀
    private static final String IP_WHITELIST_KEY = "security:ip:whitelist";
    private static final String IP_BLACKLIST_KEY = "security:ip:blacklist";
    private static final String IP_TEMP_BLOCK_KEY = "security:ip:temp_block:";

    // IP地址正则表达式
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    // CIDR网段正则表达式
    private static final Pattern CIDR_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/([0-9]|[1-2][0-9]|3[0-2])$");

    public ReactiveIpFilterService(@Qualifier("unifiedReactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检查IP地址是否被允许访问
     *
     * @param ipAddress IP地址
     * @return 是否允许访问（true=允许，false=拒绝）
     */
    public Mono<Boolean> isIpAllowed(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return Mono.just(false);
        }

        // 首先检查是否在临时封禁列表中
        return isTemporarilyBlocked(ipAddress)
                .flatMap(tempBlocked -> {
                    if (tempBlocked) {
                        LoggingUtil.warn(logger, "IP {} 在临时封禁列表中", ipAddress);
                        return Mono.just(false);
                    }

                    // 检查黑名单
                    return isInBlacklist(ipAddress)
                            .flatMap(blacklisted -> {
                                if (blacklisted) {
                                    LoggingUtil.warn(logger, "IP {} 在黑名单中", ipAddress);
                                    return Mono.just(false);
                                }

                                // 检查白名单
                                return isInWhitelist(ipAddress)
                                        .flatMap(whitelisted -> {
                                            if (whitelisted) {
                                                LoggingUtil.debug(logger, "IP {} 在白名单中，允许访问", ipAddress);
                                                return Mono.just(true);
                                            }

                                            // 如果白名单为空，则允许所有不在黑名单中的IP
                                            return isWhitelistEmpty()
                                                    .map(empty -> {
                                                        if (empty) {
                                                            LoggingUtil.debug(logger, "白名单为空，IP {} 允许访问", ipAddress);
                                                            return true;
                                                        } else {
                                                            LoggingUtil.warn(logger, "IP {} 不在白名单中，拒绝访问", ipAddress);
                                                            return false;
                                                        }
                                                    });
                                        });
                            });
                });
    }

    /**
     * 添加IP到白名单
     *
     * @param ipAddress IP地址或CIDR网段
     * @return 添加结果
     */
    public Mono<Boolean> addToWhitelist(String ipAddress) {
        if (!isValidIpOrCidr(ipAddress)) {
            LoggingUtil.warn(logger, "无效的IP地址或CIDR网段: {}", ipAddress);
            return Mono.just(false);
        }

        return redisTemplate.opsForSet().add(IP_WHITELIST_KEY, ipAddress)
                .map(added -> added > 0)
                .doOnNext(success -> {
                    if (success) {
                        LoggingUtil.info(logger, "IP {} 已添加到白名单", ipAddress);
                    }
                });
    }

    /**
     * 从白名单中移除IP
     *
     * @param ipAddress IP地址或CIDR网段
     * @return 移除结果
     */
    public Mono<Boolean> removeFromWhitelist(String ipAddress) {
        return redisTemplate.opsForSet().remove(IP_WHITELIST_KEY, ipAddress)
                .map(removed -> removed > 0)
                .doOnNext(success -> {
                    if (success) {
                        LoggingUtil.info(logger, "IP {} 已从白名单移除", ipAddress);
                    }
                });
    }

    /**
     * 添加IP到黑名单
     *
     * @param ipAddress IP地址或CIDR网段
     * @return 添加结果
     */
    public Mono<Boolean> addToBlacklist(String ipAddress) {
        if (!isValidIpOrCidr(ipAddress)) {
            LoggingUtil.warn(logger, "无效的IP地址或CIDR网段: {}", ipAddress);
            return Mono.just(false);
        }

        return redisTemplate.opsForSet().add(IP_BLACKLIST_KEY, ipAddress)
                .map(added -> added > 0)
                .doOnNext(success -> {
                    if (success) {
                        LoggingUtil.info(logger, "IP {} 已添加到黑名单", ipAddress);
                    }
                });
    }

    /**
     * 从黑名单中移除IP
     *
     * @param ipAddress IP地址或CIDR网段
     * @return 移除结果
     */
    public Mono<Boolean> removeFromBlacklist(String ipAddress) {
        return redisTemplate.opsForSet().remove(IP_BLACKLIST_KEY, ipAddress)
                .map(removed -> removed > 0)
                .doOnNext(success -> {
                    if (success) {
                        LoggingUtil.info(logger, "IP {} 已从黑名单移除", ipAddress);
                    }
                });
    }

    /**
     * 临时封禁IP地址
     *
     * @param ipAddress IP地址
     * @param duration  封禁时长
     * @return 封禁结果
     */
    public Mono<Boolean> temporarilyBlockIp(String ipAddress, Duration duration) {
        if (!isValidIp(ipAddress)) {
            LoggingUtil.warn(logger, "无效的IP地址: {}", ipAddress);
            return Mono.just(false);
        }

        String key = IP_TEMP_BLOCK_KEY + ipAddress;
        return redisTemplate.opsForValue().set(key, "blocked", duration)
                .doOnNext(success -> {
                    if (success) {
                        LoggingUtil.info(logger, "IP {} 已临时封禁 {} 秒", ipAddress, duration.getSeconds());
                    }
                });
    }

    /**
     * 解除IP的临时封禁
     *
     * @param ipAddress IP地址
     * @return 解除结果
     */
    public Mono<Boolean> unblockIp(String ipAddress) {
        String key = IP_TEMP_BLOCK_KEY + ipAddress;
        return redisTemplate.delete(key)
                .map(deleted -> deleted > 0)
                .doOnNext(success -> {
                    if (success) {
                        LoggingUtil.info(logger, "IP {} 的临时封禁已解除", ipAddress);
                    }
                });
    }

    /**
     * 获取白名单中的所有IP
     *
     * @return IP列表
     */
    public Flux<String> getWhitelistIps() {
        return redisTemplate.opsForSet().members(IP_WHITELIST_KEY);
    }

    /**
     * 获取黑名单中的所有IP
     *
     * @return IP列表
     */
    public Flux<String> getBlacklistIps() {
        return redisTemplate.opsForSet().members(IP_BLACKLIST_KEY);
    }

    /**
     * 清空白名单
     *
     * @return 清空结果
     */
    public Mono<Boolean> clearWhitelist() {
        return redisTemplate.delete(IP_WHITELIST_KEY)
                .map(deleted -> deleted > 0)
                .doOnNext(success -> {
                    if (success) {
                        LoggingUtil.info(logger, "白名单已清空");
                    }
                });
    }

    /**
     * 清空黑名单
     *
     * @return 清空结果
     */
    public Mono<Boolean> clearBlacklist() {
        return redisTemplate.delete(IP_BLACKLIST_KEY)
                .map(deleted -> deleted > 0)
                .doOnNext(success -> {
                    if (success) {
                        LoggingUtil.info(logger, "黑名单已清空");
                    }
                });
    }

    /**
     * 检查IP是否在白名单中
     */
    private Mono<Boolean> isInWhitelist(String ipAddress) {
        return redisTemplate.opsForSet().members(IP_WHITELIST_KEY)
                .any(entry -> matchesIpOrCidr(ipAddress, entry));
    }

    /**
     * 检查IP是否在黑名单中
     */
    private Mono<Boolean> isInBlacklist(String ipAddress) {
        return redisTemplate.opsForSet().members(IP_BLACKLIST_KEY)
                .any(entry -> matchesIpOrCidr(ipAddress, entry));
    }

    /**
     * 检查IP是否被临时封禁
     */
    private Mono<Boolean> isTemporarilyBlocked(String ipAddress) {
        String key = IP_TEMP_BLOCK_KEY + ipAddress;
        return redisTemplate.hasKey(key);
    }

    /**
     * 检查白名单是否为空
     */
    private Mono<Boolean> isWhitelistEmpty() {
        return redisTemplate.opsForSet().size(IP_WHITELIST_KEY)
                .map(size -> size == 0);
    }

    /**
     * 验证IP地址格式
     */
    private boolean isValidIp(String ipAddress) {
        return ipAddress != null && IP_PATTERN.matcher(ipAddress).matches();
    }

    /**
     * 验证IP地址或CIDR网段格式
     */
    private boolean isValidIpOrCidr(String ipOrCidr) {
        if (ipOrCidr == null) {
            return false;
        }
        return IP_PATTERN.matcher(ipOrCidr).matches() || CIDR_PATTERN.matcher(ipOrCidr).matches();
    }

    /**
     * 检查IP是否匹配指定的IP或CIDR网段
     */
    private boolean matchesIpOrCidr(String ipAddress, String ipOrCidr) {
        if (ipAddress.equals(ipOrCidr)) {
            return true;
        }

        // 检查CIDR网段匹配
        if (ipOrCidr.contains("/")) {
            return isIpInCidr(ipAddress, ipOrCidr);
        }

        return false;
    }

    /**
     * 检查IP是否在CIDR网段内
     */
    private boolean isIpInCidr(String ipAddress, String cidr) {
        try {
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                return false;
            }

            String networkIp = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            long ipLong = ipToLong(ipAddress);
            long networkLong = ipToLong(networkIp);
            long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;

            return (ipLong & mask) == (networkLong & mask);
        } catch (Exception e) {
            LoggingUtil.warn(logger, "CIDR匹配异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 将IP地址转换为长整型
     */
    private long ipToLong(String ipAddress) {
        String[] parts = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) + Integer.parseInt(parts[i]);
        }
        return result;
    }
}
