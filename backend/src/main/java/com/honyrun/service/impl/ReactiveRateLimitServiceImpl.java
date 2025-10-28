package com.honyrun.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.honyrun.config.properties.RateLimitProperties;
import com.honyrun.service.reactive.ReactiveRateLimitService;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;

/**
 * 响应式速率限制服务实现类
 *
 * 基于Redis实现分布式速率限制，使用令牌桶算法：
 * - 支持多维度限流（全局、IP、用户、端点）
 * - 使用Redis Lua脚本确保原子性操作
 * - 提供详细的限流统计和监控
 * - 支持动态配置和实时调整
 *
 * 令牌桶算法实现：
 * - 每个维度维护独立的令牌桶
 * - 定期向桶中添加令牌（根据配置的速率）
 * - 请求消耗令牌，无令牌时拒绝请求
 * - 支持突发流量（桶容量大于速率）
 *
 * @author Mr.Rey
 * @created 2025-07-01 17:15:00
 * @modified 2025-07-01 17:15:00
 * @version 1.0.0
 *          Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
public class ReactiveRateLimitServiceImpl implements ReactiveRateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveRateLimitServiceImpl.class);

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final RateLimitProperties rateLimitProperties;

    /**
     * 构造函数注入
     *
     * @param redisTemplate       统一的响应式Redis模板
     * @param rateLimitProperties 限流配置属性
     */
    public ReactiveRateLimitServiceImpl(
            @Qualifier("unifiedReactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate,
            RateLimitProperties rateLimitProperties) {
        this.redisTemplate = redisTemplate;
        this.rateLimitProperties = rateLimitProperties;
    }

    // Redis键前缀
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String GLOBAL_KEY = "global";

    // Lua脚本：令牌桶算法实现
    private static final String TOKEN_BUCKET_SCRIPT = "local key = KEYS[1]\n" +
            "local capacity = tonumber(ARGV[1])\n" +
            "local tokens = tonumber(ARGV[2])\n" +
            "local interval = tonumber(ARGV[3])\n" +
            "local requested = tonumber(ARGV[4])\n" +
            "local now = tonumber(ARGV[5])\n" +
            "\n" +
            "local bucket = redis.call('hmget', key, 'tokens', 'last_refill')\n" +
            "local current_tokens = tonumber(bucket[1]) or capacity\n" +
            "local last_refill = tonumber(bucket[2]) or now\n" +
            "\n" +
            "-- 计算需要添加的令牌数\n" +
            "local elapsed = math.max(0, now - last_refill)\n" +
            "local tokens_to_add = math.floor(elapsed / interval * tokens)\n" +
            "current_tokens = math.min(capacity, current_tokens + tokens_to_add)\n" +
            "\n" +
            "-- 检查是否有足够的令牌\n" +
            "if current_tokens >= requested then\n" +
            "    current_tokens = current_tokens - requested\n" +
            "    redis.call('hmset', key, 'tokens', current_tokens, 'last_refill', now)\n" +
            "    redis.call('expire', key, interval * 2)\n" +
            "    return {1, current_tokens}\n" +
            "else\n" +
            "    redis.call('hmset', key, 'tokens', current_tokens, 'last_refill', now)\n" +
            "    redis.call('expire', key, interval * 2)\n" +
            "    return {0, current_tokens}\n" +
            "end";

    @SuppressWarnings("rawtypes")
    private final RedisScript<List> tokenBucketScript = RedisScript.of(TOKEN_BUCKET_SCRIPT, List.class);

    @Override
    public Mono<Boolean> checkGlobalRateLimit() {
        if (!rateLimitProperties.isEnabled() || !rateLimitProperties.getGlobal().isEnabled()) {
            return Mono.just(true);
        }

        String key = RATE_LIMIT_PREFIX + GLOBAL_KEY;
        RateLimitProperties.DimensionConfig config = rateLimitProperties.getGlobal();

        return executeTokenBucket(key, config, 1)
                .doOnNext(allowed -> {
                    if (!allowed) {
                        LoggingUtil.warn(logger, "全局速率限制触发，拒绝请求");
                    }
                })
                .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> checkIpRateLimit(String clientIp) {
        if (!rateLimitProperties.isEnabled() || !rateLimitProperties.getIp().isEnabled() || clientIp == null) {
            return Mono.just(true);
        }

        String key = RATE_LIMIT_PREFIX + "ip:" + clientIp;
        RateLimitProperties.DimensionConfig config = rateLimitProperties.getIp();

        return executeTokenBucket(key, config, 1)
                .doOnNext(allowed -> {
                    if (!allowed) {
                        LoggingUtil.warn(logger, "IP速率限制触发，IP: {}", clientIp);
                    }
                })
                .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> checkUserRateLimit(Long userId) {
        if (!rateLimitProperties.isEnabled() || !rateLimitProperties.getUser().isEnabled() || userId == null) {
            return Mono.just(true);
        }

        String key = RATE_LIMIT_PREFIX + "user:" + userId;
        RateLimitProperties.DimensionConfig config = rateLimitProperties.getUser();

        return executeTokenBucket(key, config, 1)
                .doOnNext(allowed -> {
                    if (!allowed) {
                        LoggingUtil.warn(logger, "用户速率限制触发，用户ID: {}", userId);
                    }
                })
                .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> checkEndpointRateLimit(String endpoint) {
        if (!rateLimitProperties.isEnabled() || !rateLimitProperties.getEndpoint().isEnabled() || endpoint == null) {
            return Mono.just(true);
        }

        String key = RATE_LIMIT_PREFIX + "endpoint:" + endpoint.replace("/", "_");
        RateLimitProperties.DimensionConfig config = rateLimitProperties.getEndpoint();

        return executeTokenBucket(key, config, 1)
                .doOnNext(allowed -> {
                    if (!allowed) {
                        LoggingUtil.warn(logger, "端点速率限制触发，端点: {}", endpoint);
                    }
                })
                .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> checkAllRateLimits(String clientIp, Long userId, String endpoint) {
        return checkGlobalRateLimit()
                .flatMap(globalAllowed -> {
                    if (!globalAllowed) {
                        return Mono.just(false);
                    }
                    return checkIpRateLimit(clientIp);
                })
                .flatMap(ipAllowed -> {
                    if (!ipAllowed) {
                        return Mono.just(false);
                    }
                    return checkUserRateLimit(userId);
                })
                .flatMap(userAllowed -> {
                    if (!userAllowed) {
                        return Mono.just(false);
                    }
                    return checkEndpointRateLimit(endpoint);
                })
                .doOnNext(allowed -> {
                    if (allowed) {
                        LoggingUtil.debug(logger, "请求通过所有速率限制检查，IP: {}, 用户: {}, 端点: {}",
                                clientIp, userId, endpoint);
                    } else {
                        LoggingUtil.warn(logger, "请求被速率限制拒绝，IP: {}, 用户: {}, 端点: {}",
                                clientIp, userId, endpoint);
                    }
                });
    }

    @Override
    public Mono<Long> getRemainingQuota(String dimension, String key) {
        String redisKey = RATE_LIMIT_PREFIX + dimension + ":" + key;

        return redisTemplate.opsForHash().get(redisKey, "tokens")
                .cast(String.class)
                .map(Long::parseLong)
                .defaultIfEmpty(0L)
                .onErrorReturn(0L);
    }

    @Override
    public Mono<Boolean> resetRateLimit(String dimension, String key) {
        String redisKey = RATE_LIMIT_PREFIX + dimension + ":" + key;

        return redisTemplate.delete(redisKey)
                .map(deleted -> deleted > 0)
                .doOnNext(success -> {
                    if (success) {
                        LoggingUtil.info(logger, "重置速率限制成功，维度: {}, 键: {}", dimension, key);
                    } else {
                        LoggingUtil.warn(logger, "重置速率限制失败，维度: {}, 键: {}", dimension, key);
                    }
                })
                .onErrorReturn(false);
    }

    @Override
    public Mono<RateLimitStats> getRateLimitStats(String dimension, String key) {
        String redisKey = RATE_LIMIT_PREFIX + dimension + ":" + key;
        String statsKey = redisKey + ":stats";

        return redisTemplate.opsForHash().multiGet(statsKey,
                List.of("total", "allowed", "blocked"))
                .map(values -> {
                    long total = parseLong(values.get(0), 0L);
                    long allowed = parseLong(values.get(1), 0L);
                    long blocked = parseLong(values.get(2), 0L);

                    return new RateLimitStats(total, allowed, blocked, 0L,
                            Instant.now().getEpochSecond());
                })
                .flatMap(stats -> getRemainingQuota(dimension, key)
                        .map(remaining -> new RateLimitStats(stats.getTotalRequests(), stats.getAllowedRequests(),
                                stats.getBlockedRequests(), remaining, stats.getResetTime()))
                        .defaultIfEmpty(stats))
                .onErrorReturn(new RateLimitStats(0, 0, 0, 0, Instant.now().getEpochSecond()));
    }

    /**
     * 执行令牌桶算法
     *
     * @param key       Redis键
     * @param config    限流配置
     * @param requested 请求的令牌数量
     * @return 是否允许请求
     */
    private Mono<Boolean> executeTokenBucket(String key, RateLimitProperties.DimensionConfig config, int requested) {
        long now = Instant.now().toEpochMilli();

        return redisTemplate.execute(tokenBucketScript,
                Collections.singletonList(key),
                String.valueOf(config.getBurstCapacity()),
                String.valueOf(config.getRequestsPerSecond()),
                String.valueOf(config.getTimeWindowSeconds() * 1000), // 转换为毫秒
                String.valueOf(requested),
                String.valueOf(now))
                .cast(List.class)
                .next() // 将 Flux 转换为 Mono
                .map(result -> {
                    Number allowed = (Number) result.get(0);
                    Number remaining = (Number) result.get(1);

                    LoggingUtil.debug(logger, "令牌桶检查结果，键: {}, 允许: {}, 剩余: {}",
                            key, allowed.intValue() == 1, remaining.longValue());

                    // 更新统计信息
                    updateStats(key, allowed.intValue() == 1);

                    return allowed.intValue() == 1;
                })
                .onErrorResume(throwable -> {
                    LoggingUtil.error(logger, "执行令牌桶算法失败，键: {}, 错误: {}", key, throwable.getMessage());
                    return Mono.just(true); // 发生错误时允许请求通过，避免影响业务
                });
    }

    /**
     * 更新统计信息
     *
     * @param key     Redis键
     * @param allowed 是否允许请求
     */
    private void updateStats(String key, boolean allowed) {
        String statsKey = key + ":stats";

        redisTemplate.opsForHash().increment(statsKey, "total", 1)
                .then(redisTemplate.opsForHash().increment(statsKey, allowed ? "allowed" : "blocked", 1))
                .then(redisTemplate.expire(statsKey, Duration.ofHours(rateLimitProperties.getStatsExpirationHours())))
                .subscribe(
                        result -> LoggingUtil.debug(logger, "更新统计信息成功，键: {}", statsKey),
                        error -> LoggingUtil.warn(logger, "更新统计信息失败，键: {}, 错误: {}", statsKey, error.getMessage()));
    }

    /**
     * 安全地解析长整型数值
     *
     * @param value        待解析的值
     * @param defaultValue 默认值
     * @return 解析结果
     */
    private long parseLong(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            LoggingUtil.warn(logger, "解析数值失败，值: {}, 使用默认值: {}", value, defaultValue);
            return defaultValue;
        }
    }
}
