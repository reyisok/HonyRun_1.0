package com.honyrun.config.cache;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Redis可配置降级配置
 *
 * <p><strong>核心功能：</strong>
 * <ul>
 *   <li><strong>可配置降级</strong> - 仅在用户明确启用时提供内存缓存备用方案</li>
 *   <li><strong>故障快速失败</strong> - 默认情况下Redis故障立即停止应用启动</li>
 *   <li><strong>手动降级控制</strong> - 需要用户明确配置才能启用降级模式</li>
 *   <li><strong>性能监控</strong> - 监控降级状态和性能指标</li>
 * </ul>
 *
 * <p><strong>降级策略：</strong>
 * <ol>
 *   <li>默认关闭降级功能，Redis故障时立即停止应用</li>
 *   <li>仅在用户明确配置启用时才允许降级</li>
 *   <li>降级时使用内存缓存并记录详细日志</li>
 *   <li>定期尝试恢复Redis连接</li>
 * </ol>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @modified 2025-10-25 19:52:56
 * @version 1.1.0 - 修改为可配置降级，默认关闭
 * @since 1.0.0
 */
@Configuration
@Profile("dev")
@ConditionalOnProperty(name = "honyrun.redis.graceful-degradation.enabled", havingValue = "true", matchIfMissing = false)
public class RedisGracefulDegradationConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisGracefulDegradationConfig.class);

    /**
     * 备用缓存服务
     *
     * @author: Mr.Rey Copyright © 2025
     * @created: 2025-10-25 19:52:56
     * @version: 1.0.0
     * @return 备用缓存服务实例
     */
    @Bean
    @ConditionalOnProperty(name = "honyrun.redis.graceful-degradation.enabled", havingValue = "true")
    public FallbackCacheService fallbackCacheService() {
        LoggingUtil.info(logger, "初始化备用缓存服务");
        return new FallbackCacheService();
    }

    /**
     * Redis降级管理器
     *
     * @author: Mr.Rey Copyright © 2025
     * @created: 2025-10-25 19:52:56
     * @version: 1.0.0
     * @param fallbackCacheService 备用缓存服务
     * @return Redis降级管理器实例
     */
    @Bean
    @ConditionalOnProperty(name = "honyrun.redis.graceful-degradation.enabled", havingValue = "true")
    public RedisDegradationManager redisDegradationManager(FallbackCacheService fallbackCacheService) {
        LoggingUtil.info(logger, "初始化Redis降级管理器");
        return new RedisDegradationManager(fallbackCacheService);
    }

    /**
     * 备用缓存服务实现
     */
    public static class FallbackCacheService {

        private static final Logger logger = LoggerFactory.getLogger(FallbackCacheService.class);
        private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, Long> expireMap = new ConcurrentHashMap<>();
        private volatile boolean degraded = false;

        /**
         * 设置缓存值
         *
         * @param key 缓存键
         * @param value 缓存值
         * @return 操作结果
         */
        public Mono<Boolean> set(String key, Object value) {
            return Mono.fromCallable(() -> {
                if (!degraded) {
                    LoggingUtil.warn(logger, "启用备用缓存模式，Redis服务不可用");
                    degraded = true;
                }

                cache.put(key, value);
                LoggingUtil.debug(logger, "备用缓存设置成功: key={}", key);
                return true;
            });
        }

        /**
         * 设置缓存值（带过期时间）
         *
         * @param key 缓存键
         * @param value 缓存值
         * @param ttlSeconds TTL秒数
         * @return 操作结果
         */
        public Mono<Boolean> setWithTtl(String key, Object value, long ttlSeconds) {
            return Mono.fromCallable(() -> {
                if (!degraded) {
                    LoggingUtil.warn(logger, "启用备用缓存模式，Redis服务不可用");
                    degraded = true;
                }

                cache.put(key, value);
                expireMap.put(key, System.currentTimeMillis() + ttlSeconds * 1000);
                LoggingUtil.debug(logger, "备用缓存设置成功（TTL={}s）: key={}", ttlSeconds, key);
                return true;
            });
        }

        /**
         * 获取缓存值
         *
         * @param key 缓存键
         * @return 缓存值
         */
        public Mono<Object> get(String key) {
            return Mono.fromCallable(() -> {
                // 检查是否过期
                Long expireTime = expireMap.get(key);
                if (expireTime != null && System.currentTimeMillis() > expireTime) {
                    cache.remove(key);
                    expireMap.remove(key);
                    LoggingUtil.debug(logger, "备用缓存键已过期: key={}", key);
                    return null;
                }

                Object value = cache.get(key);
                LoggingUtil.debug(logger, "备用缓存获取: key={}, found={}", key, value != null);
                return value;
            });
        }

        /**
         * 删除缓存值
         *
         * @param key 缓存键
         * @return 操作结果
         */
        public Mono<Boolean> delete(String key) {
            return Mono.fromCallable(() -> {
                Object removed = cache.remove(key);
                expireMap.remove(key);
                LoggingUtil.debug(logger, "备用缓存删除: key={}, existed={}", key, removed != null);
                return removed != null;
            });
        }

        /**
         * 检查键是否存在
         *
         * @param key 缓存键
         * @return 是否存在
         */
        public Mono<Boolean> exists(String key) {
            return get(key).map(value -> value != null);
        }

        /**
         * 获取缓存大小
         *
         * @return 缓存大小
         */
        public int size() {
            return cache.size();
        }

        /**
         * 清空缓存
         */
        public void clear() {
            cache.clear();
            expireMap.clear();
            LoggingUtil.info(logger, "备用缓存已清空");
        }

        /**
         * 是否处于降级模式
         *
         * @return 降级状态
         */
        public boolean isDegraded() {
            return degraded;
        }

        /**
         * 重置降级状态
         */
        public void resetDegradationStatus() {
            degraded = false;
            LoggingUtil.info(logger, "备用缓存降级状态已重置");
        }
    }

    /**
     * Redis降级管理器实现
     */
    public static class RedisDegradationManager {

        private static final Logger logger = LoggerFactory.getLogger(RedisDegradationManager.class);
        private final FallbackCacheService fallbackCacheService;
        private volatile boolean redisAvailable = true;

        public RedisDegradationManager(FallbackCacheService fallbackCacheService) {
            this.fallbackCacheService = fallbackCacheService;
        }

        /**
         * 标记Redis不可用
         */
        public void markRedisUnavailable() {
            if (redisAvailable) {
                redisAvailable = false;
                LoggingUtil.warn(logger, "Redis服务标记为不可用，启用降级模式");
            }
        }

        /**
         * 标记Redis可用
         */
        public void markRedisAvailable() {
            if (!redisAvailable) {
                redisAvailable = true;
                fallbackCacheService.resetDegradationStatus();
                LoggingUtil.info(logger, "Redis服务恢复可用，退出降级模式");
            }
        }

        /**
         * 检查Redis是否可用
         *
         * @return Redis可用状态
         */
        public boolean isRedisAvailable() {
            return redisAvailable;
        }

        /**
         * 获取备用缓存服务
         *
         * @return 备用缓存服务
         */
        public FallbackCacheService getFallbackCacheService() {
            return fallbackCacheService;
        }

        /**
         * 获取降级统计信息
         *
         * @return 统计信息
         */
        public String getDegradationStats() {
            return String.format("Redis可用: %s, 备用缓存大小: %d, 降级状态: %s",
                    redisAvailable,
                    fallbackCacheService.size(),
                    fallbackCacheService.isDegraded());
        }
    }
}
