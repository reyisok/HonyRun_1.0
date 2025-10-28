package com.honyrun.service.cache;

import com.honyrun.constant.CacheConstants;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * 缓存分层服务
 *
 * 提供统一的缓存操作接口，实现缓存策略分层管理：
 * - 短TTL缓存：权限、会话、验证码等安全敏感数据
 * - 中等TTL缓存：用户信息、业务数据等
 * - 长TTL缓存：系统配置、字典数据等相对稳定数据
 *
 * 主要功能：
 * - 分层缓存存储和获取
 * - 缓存键命名规范管理
 * - 缓存失效事件处理
 * - 缓存性能监控
 *
 * @author Mr.Rey
 * @version 1.0.0
 * @created 2025-07-01 12:00:00
 * @modified 2025-07-01 12:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
public class CacheLayerService {

    private static final Logger logger = LoggerFactory.getLogger(CacheLayerService.class);

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final CacheKeyNamingService cacheKeyNamingService;
    private final CacheEvictionEventService cacheEvictionEventService;

    /**
     * 构造函数注入依赖
     *
     * @param reactiveRedisTemplate 响应式Redis模板
     * @param cacheKeyNamingService 缓存键命名服务
     * @param cacheEvictionEventService 缓存失效事件服务
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public CacheLayerService(@Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                           CacheKeyNamingService cacheKeyNamingService,
                           CacheEvictionEventService cacheEvictionEventService) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.cacheKeyNamingService = cacheKeyNamingService;
        this.cacheEvictionEventService = cacheEvictionEventService;
    }

    // ==================== 短TTL缓存操作 (安全敏感数据) ====================

    /**
     * 存储短TTL缓存数据（权限、会话等安全敏感数据）
     *
     * @param key 缓存键
     * @param value 缓存值
     * @return 存储结果
     */
    public Mono<Boolean> putShortTtlCache(String key, Object value) {
        // 验证缓存键格式
        cacheKeyNamingService.validateCacheKey(key);

        Duration ttl = CacheConstants.PERMISSION_EXPIRE_DURATION; // 使用权限TTL作为短TTL的默认值

        LoggingUtil.info(logger, "存储短TTL缓存数据，缓存键: {}, TTL: {}秒", key, ttl.getSeconds());

        return reactiveRedisTemplate.opsForValue()
                .set(key, value, ttl)
                .doOnSuccess(result -> {
                    if (result) {
                        LoggingUtil.debug(logger, "短TTL缓存存储成功，缓存键: {}", key);
                    } else {
                        LoggingUtil.warn(logger, "短TTL缓存存储失败，缓存键: {}", key);
                    }
                })
                .doOnError(error -> LoggingUtil.error(logger, "短TTL缓存存储异常，缓存键: " + key, error));
    }

    /**
     * 存储权限缓存
     *
     * @param userId 用户ID
     * @param permissions 权限数据
     * @return 存储结果
     */
    public Mono<Boolean> putUserPermissionCache(String userId, Object permissions) {
        String cacheKey = cacheKeyNamingService.generateUserPermissionKey(userId);
        return putShortTtlCache(cacheKey, permissions);
    }

    /**
     * 获取权限缓存
     *
     * @param userId 用户ID
     * @return 权限数据
     */
    public Mono<Object> getPermissionCache(String userId) {
        String cacheKey = cacheKeyNamingService.generateUserPermissionKey(userId);

        return reactiveRedisTemplate.opsForValue()
                .get(cacheKey)
                .doOnNext(result -> LoggingUtil.debug(logger, "权限缓存获取成功，用户ID: {}", userId))
                .doOnError(error -> LoggingUtil.error(logger, "权限缓存获取失败，用户ID: " + userId, error));
    }

    /**
     * 获取会话缓存
     *
     * @param sessionId 会话ID
     * @return 会话数据
     */
    public Mono<Object> getSessionCache(String sessionId) {
        String cacheKey = cacheKeyNamingService.generateUserSessionKey(sessionId);

        return reactiveRedisTemplate.opsForValue()
                .get(cacheKey)
                .doOnNext(result -> LoggingUtil.debug(logger, "会话缓存获取成功，会话ID: {}", sessionId))
                .doOnError(error -> LoggingUtil.error(logger, "会话缓存获取失败，会话ID: " + sessionId, error));
    }

    /**
     * 存储验证码缓存
     *
     * @param captchaKey 验证码键
     * @param captchaValue 验证码值
     * @return 操作结果
     */
    public Mono<Boolean> setCaptchaCache(String captchaKey, String captchaValue) {
        String cacheKey = cacheKeyNamingService.generateCaptchaKey(captchaKey);
        return putShortTtlCache(cacheKey, captchaValue);
    }

    // ==================== 中等TTL缓存操作 (业务数据) ====================

    /**
     * 存储中等TTL缓存数据（用户信息、业务数据等）
     *
     * @param key 缓存键
     * @param value 缓存值
     * @return 存储结果
     */
    public Mono<Boolean> putMediumTtlCache(String key, Object value) {
        // 验证缓存键格式
        cacheKeyNamingService.validateCacheKey(key);

        Duration ttl = CacheConstants.USER_INFO_EXPIRE_DURATION; // 使用用户信息TTL作为中等TTL的默认值

        LoggingUtil.info(logger, "存储中等TTL缓存数据，缓存键: {}, TTL: {}秒", key, ttl.getSeconds());

        return reactiveRedisTemplate.opsForValue()
                .set(key, value, ttl)
                .doOnSuccess(result -> {
                    if (result) {
                        LoggingUtil.debug(logger, "中等TTL缓存存储成功，缓存键: {}", key);
                    } else {
                        LoggingUtil.warn(logger, "中等TTL缓存存储失败，缓存键: {}", key);
                    }
                })
                .doOnError(error -> LoggingUtil.error(logger, "中等TTL缓存存储异常，缓存键: " + key, error));
    }

    /**
     * 存储用户信息缓存
     *
     * @param userId 用户ID
     * @param userInfo 用户信息
     * @return 操作结果
     */
    public Mono<Boolean> setUserInfoCache(String userId, Object userInfo) {
        String cacheKey = cacheKeyNamingService.generateUserInfoKey(userId);
        return putMediumTtlCache(cacheKey, userInfo);
    }

    /**
     * 获取用户信息缓存
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public Mono<Object> getUserInfoCache(String userId) {
        String cacheKey = cacheKeyNamingService.generateUserInfoKey(userId);

        return reactiveRedisTemplate.opsForValue()
                .get(cacheKey)
                .doOnNext(result -> LoggingUtil.debug(logger, "用户信息缓存获取成功，用户ID: {}", userId))
                .doOnError(error -> LoggingUtil.error(logger, "用户信息缓存获取失败，用户ID: " + userId, error));
    }

    /**
     * 存储业务数据缓存
     *
     * @param businessModule 业务模块
     * @param businessId 业务ID
     * @param businessData 业务数据
     * @return 操作结果
     */
    public Mono<Boolean> setBusinessDataCache(String businessModule, String businessId, Object businessData) {
        String cacheKey = cacheKeyNamingService.generateBusinessDataKey(businessModule, businessId);
        return putMediumTtlCache(cacheKey, businessData);
    }

    // ==================== 长TTL缓存操作 (系统配置、字典数据) ====================

    /**
     * 存储长TTL缓存数据（系统配置、字典数据等）
     *
     * @param key 缓存键
     * @param value 缓存值
     * @return 存储结果
     */
    public Mono<Boolean> putLongTtlCache(String key, Object value) {
        // 验证缓存键格式
        cacheKeyNamingService.validateCacheKey(key);

        Duration ttl = CacheConstants.SYSTEM_SETTING_EXPIRE_DURATION; // 使用系统设置TTL作为长TTL的默认值

        LoggingUtil.info(logger, "存储长TTL缓存数据，缓存键: {}, TTL: {}秒", key, ttl.getSeconds());

        return reactiveRedisTemplate.opsForValue()
                .set(key, value, ttl)
                .doOnSuccess(result -> {
                    if (result) {
                        LoggingUtil.debug(logger, "长TTL缓存存储成功，缓存键: {}", key);
                    } else {
                        LoggingUtil.warn(logger, "长TTL缓存存储失败，缓存键: {}", key);
                    }
                })
                .doOnError(error -> LoggingUtil.error(logger, "长TTL缓存存储异常，缓存键: " + key, error));
    }

    /**
     * 存储系统设置缓存
     *
     * @param settingKey 设置键
     * @param settingValue 设置值
     * @return 操作结果
     */
    public Mono<Boolean> setSystemSettingCache(String settingKey, Object settingValue) {
        String cacheKey = cacheKeyNamingService.generateSystemSettingKey(settingKey);
        return putLongTtlCache(cacheKey, settingValue);
    }

    /**
     * 获取系统设置缓存
     *
     * @param settingKey 设置键
     * @return 设置值
     */
    public Mono<Object> getSystemSettingCache(String settingKey) {
        String cacheKey = cacheKeyNamingService.generateSystemSettingKey(settingKey);

        return reactiveRedisTemplate.opsForValue()
                .get(cacheKey)
                .doOnNext(result -> LoggingUtil.debug(logger, "系统设置缓存获取成功，设置键: {}", settingKey))
                .doOnError(error -> LoggingUtil.error(logger, "系统设置缓存获取失败，设置键: " + settingKey, error));
    }

    /**
     * 存储系统配置缓存
     *
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 操作结果
     */
    public Mono<Boolean> setSystemConfigCache(String configKey, Object configValue) {
        String cacheKey = cacheKeyNamingService.generateSystemConfigKey(configKey);
        return putLongTtlCache(cacheKey, configValue);
    }

    // ==================== 缓存失效操作 ====================

    /**
     * 失效权限缓存并发布事件
     *
     * @param userId 用户ID
     * @return 失效结果
     */
    public Mono<Boolean> evictPermissionCache(String userId) {
        String cacheKey = cacheKeyNamingService.generateUserPermissionKey(userId);

        LoggingUtil.info(logger, "失效权限缓存，用户ID: {}", userId);

        return cacheEvictionEventService.evictCacheWithEvent(cacheKey, "用户权限更新")
                .doOnSuccess(result -> LoggingUtil.debug(logger, "权限缓存失效成功，用户ID: {}", userId))
                .doOnError(error -> LoggingUtil.error(logger, "权限缓存失效失败，用户ID: " + userId, error));
    }

    /**
     * 失效会话缓存并发布事件
     *
     * @param sessionId 会话ID
     * @return 失效结果
     */
    public Mono<Boolean> evictSessionCache(String sessionId) {
        String cacheKey = cacheKeyNamingService.generateUserSessionKey(sessionId);

        LoggingUtil.info(logger, "失效会话缓存，会话ID: {}", sessionId);

        return cacheEvictionEventService.evictCacheWithEvent(cacheKey, "会话过期或注销")
                .doOnSuccess(result -> LoggingUtil.debug(logger, "会话缓存失效成功，会话ID: {}", sessionId))
                .doOnError(error -> LoggingUtil.error(logger, "会话缓存失效失败，会话ID: " + sessionId, error));
    }

    /**
     * 批量失效用户相关缓存并发布事件
     *
     * @param userId 用户ID
     * @return 失效的缓存数量
     */
    public Mono<Long> evictUserRelatedCaches(String userId) {
        LoggingUtil.info(logger, "批量失效用户相关缓存，用户ID: {}", userId);

        String userKeyPattern = CacheConstants.USER_KEY_PREFIX + userId + "*";

        return cacheEvictionEventService.evictCachesBatchWithEvent(userKeyPattern, "用户数据更新")
                .doOnSuccess(count -> {
                    if (count > 0) {
                        LoggingUtil.info(logger, "用户相关缓存批量失效成功，用户ID: {}, 失效数量: {}", userId, count);
                    } else {
                        LoggingUtil.info(logger, "未找到用户相关缓存，用户ID: {}", userId);
                    }
                })
                .doOnError(error -> LoggingUtil.error(logger, "用户相关缓存批量失效失败，用户ID: " + userId, error));
    }

    // ==================== 缓存监控操作 ====================

    /**
     * 获取缓存层级统计信息
     *
     * @return 缓存统计信息
     */
    public Mono<Map<String, Object>> getCacheLayerStats() {
        LoggingUtil.debug(logger, "获取缓存层级统计信息");

        return Mono.fromCallable(() -> {
            Map<String, Object> stats = new java.util.HashMap<>();

            // 短TTL缓存统计
            stats.put("shortTtlLayer", Map.of(
                "permissionTtl", CacheConstants.PERMISSION_EXPIRE_DURATION.toString(),
                "sessionTtl", CacheConstants.SESSION_EXPIRE_DURATION.toString(),
                "captchaTtl", CacheConstants.CAPTCHA_EXPIRE_DURATION.toString()
            ));

            // 中等TTL缓存统计
            stats.put("mediumTtlLayer", Map.of(
                "userInfoTtl", CacheConstants.USER_INFO_EXPIRE_DURATION.toString(),
                "businessDataTtl", CacheConstants.BUSINESS_DATA_EXPIRE_DURATION.toString(),
                "statisticsTtl", CacheConstants.STATISTICS_EXPIRE_DURATION.toString()
            ));

            // 长TTL缓存统计
            stats.put("longTtlLayer", Map.of(
                "systemSettingTtl", CacheConstants.SYSTEM_SETTING_EXPIRE_DURATION.toString(),
                "systemConfigTtl", CacheConstants.SYSTEM_CONFIG_EXPIRE_DURATION.toString(),
                "dictionaryTtl", CacheConstants.DICTIONARY_EXPIRE_DURATION.toString()
            ));

            return stats;
        })
        .doOnSuccess(stats -> LoggingUtil.debug(logger, "缓存层级统计信息获取成功"))
        .doOnError(error -> LoggingUtil.error(logger, "缓存层级统计信息获取失败", error));
    }

    /**
     * 获取指定前缀的缓存键数量
     *
     * @param keyPrefix 键前缀
     * @return 缓存键数量
     */
    public Mono<Long> getCacheKeyCount(String keyPrefix) {
        String pattern = keyPrefix + "*";

        return reactiveRedisTemplate.keys(pattern)
                .count()
                .doOnSuccess(count -> LoggingUtil.debug(logger, "缓存键数量统计完成，前缀: {}, 数量: {}", keyPrefix, count))
                .doOnError(error -> LoggingUtil.error(logger, "缓存键数量统计失败，前缀: " + keyPrefix, error));
    }
}
