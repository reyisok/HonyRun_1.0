package com.honyrun.service.cache;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * 响应式缓存服务接口
 * 
 * 提供统一的响应式缓存操作接口：
 * - 基础缓存操作（get、set、delete）
 * - 批量缓存操作
 * - 缓存失效和刷新
 * - 缓存统计和监控
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01 15:00:00
 * @modified 2025-07-01 15:00:00
 * @version 1.0.0
 */
public interface ReactiveCacheService {

    // ==================== 基础缓存操作 ====================

    /**
     * 获取缓存值
     * 
     * @param key 缓存键
     * @param valueType 值类型
     * @param <T> 值类型泛型
     * @return 缓存值的Mono
     */
    <T> Mono<T> get(String key, Class<T> valueType);

    /**
     * 设置缓存值
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时间
     * @return 操作结果的Mono
     */
    Mono<Boolean> set(String key, Object value, Duration ttl);

    /**
     * 设置缓存值（使用默认TTL）
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @return 操作结果的Mono
     */
    Mono<Boolean> set(String key, Object value);

    /**
     * 删除缓存
     * 
     * @param key 缓存键
     * @return 操作结果的Mono
     */
    Mono<Boolean> delete(String key);

    /**
     * 检查缓存是否存在
     * 
     * @param key 缓存键
     * @return 存在性的Mono
     */
    Mono<Boolean> exists(String key);

    // ==================== 批量缓存操作 ====================

    /**
     * 批量获取缓存值
     * 
     * @param keys 缓存键集合
     * @param valueType 值类型
     * @param <T> 值类型泛型
     * @return 缓存值映射的Mono
     */
    <T> Mono<Map<String, T>> multiGet(Flux<String> keys, Class<T> valueType);

    /**
     * 批量设置缓存值
     * 
     * @param keyValues 键值映射
     * @param ttl 过期时间
     * @return 操作结果的Mono
     */
    Mono<Boolean> multiSet(Map<String, Object> keyValues, Duration ttl);

    /**
     * 批量删除缓存
     * 
     * @param keys 缓存键集合
     * @return 删除数量的Mono
     */
    Mono<Long> multiDelete(Flux<String> keys);

    // ==================== 缓存失效和刷新 ====================

    /**
     * 按模式删除缓存
     * 
     * @param pattern 键模式（支持通配符）
     * @return 删除数量的Mono
     */
    Mono<Long> deleteByPattern(String pattern);

    /**
     * 刷新缓存
     * 
     * @param key 缓存键
     * @param supplier 数据提供者
     * @param ttl 过期时间
     * @param <T> 值类型泛型
     * @return 刷新后的值的Mono
     */
    <T> Mono<T> refresh(String key, Mono<T> supplier, Duration ttl);

    /**
     * 获取或设置缓存（缓存穿透保护）
     * 
     * @param key 缓存键
     * @param supplier 数据提供者
     * @param ttl 过期时间
     * @param valueType 值类型
     * @param <T> 值类型泛型
     * @return 缓存值的Mono
     */
    <T> Mono<T> getOrSet(String key, Mono<T> supplier, Duration ttl, Class<T> valueType);

    // ==================== 缓存统计和监控 ====================

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计的Mono
     */
    Mono<CacheStats> getStats();

    /**
     * 获取缓存大小
     * 
     * @return 缓存大小的Mono
     */
    Mono<Long> size();

    /**
     * 清空所有缓存
     * 
     * @return 操作结果的Mono
     */
    Mono<Boolean> clear();

    /**
     * 缓存统计信息
     */
    class CacheStats {
        private final long hitCount;
        private final long missCount;
        private final long evictionCount;
        private final double hitRate;

        public CacheStats(long hitCount, long missCount, long evictionCount) {
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.evictionCount = evictionCount;
            this.hitRate = (hitCount + missCount) > 0 ? 
                (double) hitCount / (hitCount + missCount) : 0.0;
        }

        public long getHitCount() { return hitCount; }
        public long getMissCount() { return missCount; }
        public long getEvictionCount() { return evictionCount; }
        public double getHitRate() { return hitRate; }
    }
}

