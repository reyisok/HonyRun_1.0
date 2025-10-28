package com.honyrun.service.cache;

import com.honyrun.constant.CacheConstants;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 缓存键命名服务
 *
 * 提供统一的缓存键命名约定和失效事件机制：
 * - 标准化缓存键命名规范
 * - 缓存键验证和格式化
 * - 缓存失效事件发布和监听
 * - 缓存键生命周期管理
 *
 * 缓存键命名规范：
 * - 应用前缀：honyrun:
 * - 业务模块：user:, system:, security:, business:
 * - 具体功能：info:, permission:, session:, setting:
 * - 标识符：用户ID、会话ID、配置键等
 *
 * 示例：
 * - 用户信息：honyrun:user:info:123456
 * - 用户权限：honyrun:user:permission:123456
 * - 系统设置：honyrun:system:setting:app.name
 * - 会话数据：honyrun:user:session:abc123def456
 *
 * @author Mr.Rey
 * @version 1.0.0
 * @created 2025-07-01 12:00:00
 * @modified 2025-07-01 12:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
public class CacheKeyNamingService {

    private static final Logger logger = LoggerFactory.getLogger(CacheKeyNamingService.class);

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    /**
     * 构造函数注入依赖
     *
     * @param reactiveRedisTemplate 响应式Redis模板
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public CacheKeyNamingService(@Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    // ==================== 缓存键命名规范常量 ====================

    /**
     * 缓存键分隔符
     */
    private static final String KEY_SEPARATOR = ":";

    /**
     * 缓存键最大长度
     */
    private static final int MAX_KEY_LENGTH = 250;

    /**
     * 缓存键最小长度
     */
    private static final int MIN_KEY_LENGTH = 10;

    /**
     * 缓存键有效字符模式
     */
    private static final Pattern VALID_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9:._-]+$");

    /**
     * 时间戳格式
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ==================== 缓存失效事件管理 ====================

    /**
     * 缓存失效事件监听器映射
     */
    private final Map<String, CacheEvictionListener> evictionListeners = new ConcurrentHashMap<>();

    /**
     * 缓存失效事件接口
     */
    public interface CacheEvictionListener {
        /**
         * 处理缓存失效事件
         *
         * @param cacheKey 缓存键
         * @param evictionReason 失效原因
         */
        void onCacheEvicted(String cacheKey, String evictionReason);
    }

    // ==================== 用户相关缓存键生成 ====================

    /**
     * 生成用户信息缓存键
     *
     * @param userId 用户ID
     * @return 缓存键
     */
    public String generateUserInfoKey(String userId) {
        validateIdentifier(userId, "用户ID");
        String cacheKey = CacheConstants.USER_INFO_KEY_PREFIX + userId;
        validateCacheKey(cacheKey);

        LoggingUtil.debug(logger, "生成用户信息缓存键: {}", cacheKey);
        return cacheKey;
    }

    /**
     * 生成用户权限缓存键
     *
     * @param userId 用户ID
     * @return 缓存键
     */
    public String generateUserPermissionKey(String userId) {
        validateIdentifier(userId, "用户ID");
        String cacheKey = CacheConstants.USER_PERMISSION_KEY_PREFIX + userId;
        validateCacheKey(cacheKey);

        LoggingUtil.debug(logger, "生成用户权限缓存键: {}", cacheKey);
        return cacheKey;
    }

    /**
     * 生成用户会话缓存键
     *
     * @param sessionId 会话ID
     * @return 缓存键
     */
    public String generateUserSessionKey(String sessionId) {
        validateIdentifier(sessionId, "会话ID");
        String cacheKey = CacheConstants.USER_SESSION_KEY_PREFIX + sessionId;
        validateCacheKey(cacheKey);

        LoggingUtil.debug(logger, "生成用户会话缓存键: {}", cacheKey);
        return cacheKey;
    }

    /**
     * 生成用户类型缓存键
     *
     * @param userId 用户ID
     * @return 缓存键
     */
    public String generateUserUserTypeKey(String userId) {
        validateIdentifier(userId, "用户ID");
        String cacheKey = CacheConstants.USER_USER_TYPE_KEY_PREFIX + userId;
        validateCacheKey(cacheKey);

        LoggingUtil.debug(logger, "生成用户类型缓存键: {}", cacheKey);
        return cacheKey;
    }

    // ==================== 系统相关缓存键生成 ====================

    /**
     * 生成系统设置缓存键
     *
     * @param settingKey 设置键
     * @return 缓存键
     */
    public String generateSystemSettingKey(String settingKey) {
        validateIdentifier(settingKey, "设置键");
        String cacheKey = CacheConstants.SYSTEM_SETTING_KEY_PREFIX + settingKey;
        validateCacheKey(cacheKey);

        LoggingUtil.debug(logger, "生成系统设置缓存键: {}", cacheKey);
        return cacheKey;
    }

    /**
     * 生成系统配置缓存键
     *
     * @param configKey 配置键
     * @return 缓存键
     */
    public String generateSystemConfigKey(String configKey) {
        validateIdentifier(configKey, "配置键");
        String cacheKey = CacheConstants.SYSTEM_CONFIG_KEY_PREFIX + configKey;
        validateCacheKey(cacheKey);

        LoggingUtil.debug(logger, "生成系统配置缓存键: {}", cacheKey);
        return cacheKey;
    }

    /**
     * 生成系统状态缓存键
     *
     * @param statusKey 状态键
     * @return 缓存键
     */
    public String generateSystemStatusKey(String statusKey) {
        validateIdentifier(statusKey, "状态键");
        String cacheKey = CacheConstants.SYSTEM_STATUS_KEY_PREFIX + statusKey;
        validateCacheKey(cacheKey);

        LoggingUtil.debug(logger, "生成系统状态缓存键: {}", cacheKey);
        return cacheKey;
    }

    // ==================== 安全相关缓存键生成 ====================

    /**
     * 生成令牌黑名单缓存键
     *
     * @param tokenId 令牌ID
     * @return 缓存键
     */
    public String generateTokenBlacklistKey(String tokenId) {
        validateIdentifier(tokenId, "令牌ID");
        String cacheKey = CacheConstants.TOKEN_BLACKLIST_KEY_PREFIX + tokenId;
        validateCacheKey(cacheKey);

        LoggingUtil.debug(logger, "生成令牌黑名单缓存键: {}", cacheKey);
        return cacheKey;
    }

    /**
     * 生成验证码缓存键
     *
     * @param captchaId 验证码ID
     * @return 缓存键
     */
    public String generateCaptchaKey(String captchaId) {
        validateIdentifier(captchaId, "验证码ID");
        String cacheKey = CacheConstants.CAPTCHA_KEY_PREFIX + captchaId;
        validateCacheKey(cacheKey);

        LoggingUtil.debug(logger, "生成验证码缓存键: {}", cacheKey);
        return cacheKey;
    }

    /**
     * 生成限流缓存键
     *
     * @param rateLimitKey 限流键
     * @return 缓存键
     */
    public String generateRateLimitKey(String rateLimitKey) {
        validateIdentifier(rateLimitKey, "限流键");
        String cacheKey = CacheConstants.RATE_LIMIT_KEY_PREFIX + rateLimitKey;
        validateCacheKey(cacheKey);

        LoggingUtil.debug(logger, "生成限流缓存键: {}", cacheKey);
        return cacheKey;
    }

    // ==================== 业务相关缓存键生成 ====================

    /**
     * 生成业务数据缓存键
     *
     * @param businessModule 业务模块
     * @param businessId 业务ID
     * @return 缓存键
     */
    public String generateBusinessDataKey(String businessModule, String businessId) {
        validateIdentifier(businessModule, "业务模块");
        validateIdentifier(businessId, "业务ID");
        String cacheKey = CacheConstants.BUSINESS_KEY_PREFIX + businessModule + KEY_SEPARATOR + businessId;
        validateCacheKey(cacheKey);

        LoggingUtil.debug(logger, "生成业务数据缓存键: {}", cacheKey);
        return cacheKey;
    }

    /**
     * 生成统计数据缓存键
     *
     * @param statsType 统计类型
     * @param timeRange 时间范围
     * @return 缓存键
     */
    public String generateStatisticsKey(String statsType, String timeRange) {
        validateIdentifier(statsType, "统计类型");
        validateIdentifier(timeRange, "时间范围");
        String cacheKey = CacheConstants.STATISTICS_KEY_PREFIX + statsType + KEY_SEPARATOR + timeRange;
        validateCacheKey(cacheKey);

        LoggingUtil.debug(logger, "生成统计数据缓存键: {}", cacheKey);
        return cacheKey;
    }

    // ==================== 临时缓存键生成 ====================

    /**
     * 生成带时间戳的临时缓存键
     *
     * @param prefix 前缀
     * @param identifier 标识符
     * @return 缓存键
     */
    public String generateTemporaryKey(String prefix, String identifier) {
        validateIdentifier(prefix, "前缀");
        validateIdentifier(identifier, "标识符");

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String cacheKey = prefix + identifier + KEY_SEPARATOR + timestamp;
        validateCacheKey(cacheKey);

        LoggingUtil.debug(logger, "生成临时缓存键: {}", cacheKey);
        return cacheKey;
    }

    // ==================== 缓存键验证 ====================

    /**
     * 验证缓存键格式
     *
     * @param cacheKey 缓存键
     * @throws IllegalArgumentException 如果缓存键格式无效
     */
    public void validateCacheKey(String cacheKey) {
        if (cacheKey == null || cacheKey.trim().isEmpty()) {
            throw new IllegalArgumentException("缓存键不能为空");
        }

        if (cacheKey.length() < MIN_KEY_LENGTH) {
            throw new IllegalArgumentException("缓存键长度不能少于" + MIN_KEY_LENGTH + "个字符: " + cacheKey);
        }

        if (cacheKey.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException("缓存键长度不能超过" + MAX_KEY_LENGTH + "个字符: " + cacheKey);
        }

        if (!VALID_KEY_PATTERN.matcher(cacheKey).matches()) {
            throw new IllegalArgumentException("缓存键包含无效字符，只允许字母、数字、冒号、点、下划线和连字符: " + cacheKey);
        }

        if (!cacheKey.startsWith(CacheConstants.KEY_PREFIX)) {
            throw new IllegalArgumentException("缓存键必须以应用前缀开头: " + CacheConstants.KEY_PREFIX);
        }

        LoggingUtil.debug(logger, "缓存键验证通过: {}", cacheKey);
    }

    /**
     * 验证标识符
     *
     * @param identifier 标识符
     * @param identifierType 标识符类型
     * @throws IllegalArgumentException 如果标识符无效
     */
    private void validateIdentifier(String identifier, String identifierType) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException(identifierType + "不能为空");
        }

        if (identifier.length() > 100) {
            throw new IllegalArgumentException(identifierType + "长度不能超过100个字符: " + identifier);
        }

        // 检查是否包含分隔符
        if (identifier.contains(KEY_SEPARATOR)) {
            throw new IllegalArgumentException(identifierType + "不能包含分隔符'" + KEY_SEPARATOR + "': " + identifier);
        }
    }

    // ==================== 缓存失效事件管理 ====================

    /**
     * 注册缓存失效事件监听器
     *
     * @param keyPattern 键模式
     * @param listener 监听器
     */
    public void registerEvictionListener(String keyPattern, CacheEvictionListener listener) {
        evictionListeners.put(keyPattern, listener);
        LoggingUtil.info(logger, "注册缓存失效事件监听器，键模式: {}", keyPattern);
    }

    /**
     * 移除缓存失效事件监听器
     *
     * @param keyPattern 键模式
     */
    public void removeEvictionListener(String keyPattern) {
        evictionListeners.remove(keyPattern);
        LoggingUtil.info(logger, "移除缓存失效事件监听器，键模式: {}", keyPattern);
    }

    /**
     * 发布缓存失效事件
     *
     * @param cacheKey 缓存键
     * @param evictionReason 失效原因
     * @return 处理结果
     */
    public Mono<Void> publishEvictionEvent(String cacheKey, String evictionReason) {
        return Mono.fromRunnable(() -> {
            LoggingUtil.info(logger, "发布缓存失效事件，缓存键: {}, 失效原因: {}", cacheKey, evictionReason);

            evictionListeners.entrySet().stream()
                    .filter(entry -> cacheKey.matches(entry.getKey().replace("*", ".*")))
                    .forEach(entry -> {
                        try {
                            entry.getValue().onCacheEvicted(cacheKey, evictionReason);
                            LoggingUtil.debug(logger, "缓存失效事件处理成功，监听器: {}", entry.getKey());
                        } catch (Exception e) {
                            LoggingUtil.error(logger, "缓存失效事件处理失败，监听器: " + entry.getKey(), e);
                        }
                    });
        });
    }

    /**
     * 批量失效缓存并发布事件
     *
     * @param keyPattern 键模式
     * @param evictionReason 失效原因
     * @return 失效的缓存键数量
     */
    public Mono<Long> evictCachesWithEvent(String keyPattern, String evictionReason) {
        LoggingUtil.info(logger, "批量失效缓存并发布事件，键模式: {}, 失效原因: {}", keyPattern, evictionReason);

        return reactiveRedisTemplate.keys(keyPattern)
                .collectList()
                .flatMap(keys -> {
                    if (keys.isEmpty()) {
                        LoggingUtil.info(logger, "未找到匹配的缓存键，键模式: {}", keyPattern);
                        return Mono.just(0L);
                    }

                    // 发布失效事件
                    Flux<Void> evictionEvents = Flux.fromIterable(keys)
                            .flatMap(key -> publishEvictionEvent(key, evictionReason));

                    // 删除缓存
                    Mono<Long> deleteResult = reactiveRedisTemplate.delete(keys.toArray(new String[0]));

                    return evictionEvents.then(deleteResult);
                })
                .doOnSuccess(count -> LoggingUtil.info(logger, "批量缓存失效完成，键模式: {}, 失效数量: {}", keyPattern, count))
                .doOnError(error -> LoggingUtil.error(logger, "批量缓存失效失败，键模式: " + keyPattern, error));
    }

    // ==================== 缓存键分析工具 ====================

    /**
     * 解析缓存键信息
     *
     * @param cacheKey 缓存键
     * @return 缓存键信息
     */
    public Map<String, String> parseCacheKey(String cacheKey) {
        Map<String, String> keyInfo = new ConcurrentHashMap<>();

        if (cacheKey == null || !cacheKey.startsWith(CacheConstants.KEY_PREFIX)) {
            keyInfo.put("valid", "false");
            keyInfo.put("error", "无效的缓存键格式");
            return keyInfo;
        }

        String[] parts = cacheKey.split(KEY_SEPARATOR);
        keyInfo.put("valid", "true");
        keyInfo.put("prefix", parts.length > 0 ? parts[0] : "");
        keyInfo.put("module", parts.length > 1 ? parts[1] : "");
        keyInfo.put("function", parts.length > 2 ? parts[2] : "");
        keyInfo.put("identifier", parts.length > 3 ? parts[3] : "");
        keyInfo.put("fullKey", cacheKey);
        keyInfo.put("length", String.valueOf(cacheKey.length()));

        LoggingUtil.debug(logger, "缓存键解析完成: {}", keyInfo);
        return keyInfo;
    }
}
