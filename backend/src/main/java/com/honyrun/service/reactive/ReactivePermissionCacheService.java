package com.honyrun.service.reactive;


import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import com.honyrun.config.UnifiedConfigManager;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 响应式权限缓存服务
 * 【权限缓存】：实现权限的缓存机制，提升权限验证性能
 * 【缓存策略】：使用Spring Cache + 内存缓存双重策略
 * 【缓存失效】：支持手动清除和自动过期机制
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-15 当前时间
 * @version 1.0.0
 */
@Service
public class ReactivePermissionCacheService {

    private static final Logger logger = LoggerFactory.getLogger(ReactivePermissionCacheService.class);

    // 内存缓存，作为Spring Cache的补充
    private final ConcurrentMap<String, CacheEntry<List<String>>> permissionCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheEntry<Boolean>> permissionCheckCache = new ConcurrentHashMap<>();

    // 配置注入 - 消除硬编码违规
    private final Duration cacheDuration;

    private final ReactiveAuthService authService;
    private final UnifiedConfigManager configManager;

    public ReactivePermissionCacheService(ReactiveAuthService authService, UnifiedConfigManager configManager) {
        this.authService = authService;
        this.configManager = configManager;
        this.cacheDuration = Duration.parse(configManager.getProperty("honyrun.permission.cache-duration", "PT30M"));
    }

    /**
     * 获取用户权限列表（带缓存）
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Cacheable(value = "user-permissions", key = "#userId")
    public Mono<List<String>> getUserPermissions(Long userId) {
        LoggingUtil.debug(logger, "获取用户权限，用户ID: {}", userId);

        String cacheKey = "permissions:" + userId;

        // 检查内存缓存
        CacheEntry<List<String>> cached = permissionCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            LoggingUtil.debug(logger, "从内存缓存获取用户权限，用户ID: {}", userId);
            return Mono.just(cached.getValue());
        }

        // 从服务获取权限并缓存
        return authService.getUserPermissions(userId)
                .collectList()
                .doOnNext(permissions -> {
                    // 更新内存缓存
                    permissionCache.put(cacheKey, new CacheEntry<>(permissions, cacheDuration));
                    LoggingUtil.debug(logger, "缓存用户权限，用户ID: {}, 权限数量: {}", userId, permissions.size());
                })
                .doOnError(error -> LoggingUtil.error(logger, "获取用户权限失败，用户ID: " + userId, error));
    }

    /**
     * 检查用户权限（带缓存）
     *
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return 是否拥有权限
     */
    @Cacheable(value = "permission-checks", key = "#userId + ':' + #permissionCode")
    public Mono<Boolean> hasPermission(Long userId, String permissionCode) {
        LoggingUtil.debug(logger, "检查用户权限，用户ID: {}, 权限代码: {}", userId, permissionCode);

        String cacheKey = "check:" + userId + ":" + permissionCode;

        // 检查内存缓存
        CacheEntry<Boolean> cached = permissionCheckCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            LoggingUtil.debug(logger, "从内存缓存获取权限检查结果，用户ID: {}, 权限代码: {}", userId, permissionCode);
            return Mono.just(cached.getValue());
        }

        // 从服务检查权限并缓存
        return authService.hasPermission(userId, permissionCode)
                .doOnNext(hasPermission -> {
                    // 更新内存缓存
                    permissionCheckCache.put(cacheKey, new CacheEntry<>(hasPermission, cacheDuration));
                    LoggingUtil.debug(logger, "缓存权限检查结果，用户ID: {}, 权限代码: {}, 结果: {}",
                            userId, permissionCode, hasPermission);
                })
                .doOnError(error -> LoggingUtil.error(logger,
                        "检查用户权限失败，用户ID: " + userId + ", 权限代码: " + permissionCode, error));
    }

    /**
     * 刷新用户权限缓存
     *
     * @param userId 用户ID
     * @return 更新后的权限列表
     */
    @CachePut(value = "user-permissions", key = "#userId")
    public Mono<List<String>> refreshUserPermissions(Long userId) {
        LoggingUtil.info(logger, "刷新用户权限缓存，用户ID: {}", userId);

        // 清除内存缓存
        clearUserMemoryCache(userId);

        // 重新获取权限
        return authService.getUserPermissions(userId)
                .collectList()
                .doOnNext(permissions -> {
                    // 更新内存缓存
                    String cacheKey = "permissions:" + userId;
                    permissionCache.put(cacheKey, new CacheEntry<>(permissions, cacheDuration));
                    LoggingUtil.info(logger, "刷新用户权限缓存完成，用户ID: {}, 权限数量: {}", userId, permissions.size());
                });
    }

    /**
     * 清除用户权限缓存
     *
     * @param userId 用户ID
     */
    @CacheEvict(value = {"user-permissions", "permission-checks"}, key = "#userId")
    public Mono<Void> clearUserPermissions(Long userId) {
        LoggingUtil.info(logger, "清除用户权限缓存，用户ID: {}", userId);

        // 清除内存缓存
        clearUserMemoryCache(userId);

        return Mono.empty()
                .doOnSuccess(v -> LoggingUtil.info(logger, "清除用户权限缓存完成，用户ID: {}", userId))
                .then();
    }

    /**
     * 清除所有权限缓存
     */
    @CacheEvict(value = {"user-permissions", "permission-checks"}, allEntries = true)
    public Mono<Void> clearAllPermissions() {
        LoggingUtil.info(logger, "清除所有权限缓存");

        // 清除内存缓存
        permissionCache.clear();
        permissionCheckCache.clear();

        return Mono.empty()
                .doOnSuccess(v -> LoggingUtil.info(logger, "清除所有权限缓存完成"))
                .then();
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    public Mono<CacheStats> getCacheStats() {
        return Mono.fromCallable(() -> {
            int permissionCacheSize = permissionCache.size();
            int permissionCheckCacheSize = permissionCheckCache.size();

            // 清理过期缓存
            cleanExpiredCache();

            return new CacheStats(permissionCacheSize, permissionCheckCacheSize);
        });
    }

    /**
     * 清除用户相关的内存缓存
     *
     * @param userId 用户ID
     */
    private void clearUserMemoryCache(Long userId) {
        String permissionKey = "permissions:" + userId;
        permissionCache.remove(permissionKey);

        // 清除权限检查缓存
        permissionCheckCache.entrySet().removeIf(entry ->
                entry.getKey().startsWith("check:" + userId + ":"));
    }

    /**
     * 清理过期的缓存条目
     */
    private void cleanExpiredCache() {
        permissionCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        permissionCheckCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * 缓存条目类
     *
     * @param <T> 缓存值类型
     */
    private static class CacheEntry<T> {
        private final T value;
        private final long expireTime;

        public CacheEntry(T value, Duration duration) {
            this.value = value;
            this.expireTime = System.currentTimeMillis() + duration.toMillis();
        }

        public T getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 缓存统计信息类
     */
    public static class CacheStats {
        private final int permissionCacheSize;
        private final int permissionCheckCacheSize;

        public CacheStats(int permissionCacheSize, int permissionCheckCacheSize) {
            this.permissionCacheSize = permissionCacheSize;
            this.permissionCheckCacheSize = permissionCheckCacheSize;
        }

        public int getPermissionCacheSize() {
            return permissionCacheSize;
        }

        public int getPermissionCheckCacheSize() {
            return permissionCheckCacheSize;
        }

        public int getTotalCacheSize() {
            return permissionCacheSize + permissionCheckCacheSize;
        }
    }
}
