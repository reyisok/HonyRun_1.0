package com.honyrun.service.cache.impl;

import com.honyrun.service.cache.ReactiveCacheService;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 响应式缓存服务实现类
 * 提供多级缓存（L1本地缓存 + L2 Redis缓存）和防护机制
 * 
 * 防护机制包括：
 * - 缓存穿透防护：空值缓存
 * - 缓存击穿防护：分布式锁
 * - 缓存雪崩防护：随机TTL和重试机制
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-24 16:01:03
 * @version 1.0.3
 */
@Service
public class ReactiveCacheServiceImpl implements ReactiveCacheService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveCacheServiceImpl.class);

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final CacheManager cacheManager;
    private final Duration defaultTtl = Duration.ofMinutes(30);
    
    // 缓存防护配置
    private static final String NULL_VALUE = "CACHE_NULL_VALUE";
    private static final String LOCK_PREFIX = "cache:lock:";
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration NULL_VALUE_TTL = Duration.ofMinutes(5);
    private static final int MAX_TTL_JITTER_SECONDS = 300; // 5分钟随机抖动
    
    // 缓存统计
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictionCount = new AtomicLong(0);


    public ReactiveCacheServiceImpl(
            @Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> redisTemplate,
            CacheManager cacheManager) {
        this.redisTemplate = redisTemplate;
        this.cacheManager = cacheManager;
    }

    // ==================== 缓存防护辅助方法 ====================
    
    /**
     * 获取带随机抖动的TTL，防止缓存雪崩
     */
    private Duration getJitteredTtl(Duration baseTtl) {
        int jitterSeconds = ThreadLocalRandom.current().nextInt(MAX_TTL_JITTER_SECONDS);
        return baseTtl.plusSeconds(jitterSeconds);
    }
    
    /**
     * 获取分布式锁
     */
    private Mono<Boolean> acquireLock(String key) {
        String lockKey = LOCK_PREFIX + key;
        return redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "locked", LOCK_TIMEOUT)
            .doOnSuccess(acquired -> {
                if (acquired) {
                    LoggingUtil.debug(logger, "获取分布式锁成功: {}", lockKey);
                } else {
                    LoggingUtil.debug(logger, "获取分布式锁失败: {}", lockKey);
                }
            });
    }
    
    /**
     * 释放分布式锁
     */
    private Mono<Boolean> releaseLock(String key) {
        String lockKey = LOCK_PREFIX + key;
        return redisTemplate.delete(lockKey)
            .map(count -> count > 0)
            .doOnSuccess(released -> LoggingUtil.debug(logger, "释放分布式锁: {} -> {}", lockKey, released));
    }
    
    /**
     * 检查是否为空值标记
     */
    private boolean isNullValue(Object value) {
        return NULL_VALUE.equals(value);
    }

    // ==================== 基础缓存操作 ====================

    @Override
    public <T> Mono<T> get(String key, Class<T> valueType) {
        return Mono.fromCallable(() -> {
            // 先尝试L1缓存
            Cache cache = cacheManager.getCache("default");
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    Object value = wrapper.get();
                    // 检查是否为空值标记（缓存穿透防护）
                    if (isNullValue(value)) {
                        hitCount.incrementAndGet();
                        LoggingUtil.debug(logger, "L1缓存命中空值: {}", key);
                        return null; // 返回null标记，后续会转换为Mono.empty()
                    }
                    hitCount.incrementAndGet();
                    LoggingUtil.debug(logger, "L1缓存命中: {}", key);
                    return valueType.cast(value);
                }
            }
            return null; // L1缓存未命中时返回null标记
        })
        .flatMap(result -> {
            if (result != null) {
                return Mono.just(result);
            }
            // L1缓存未命中，尝试L2缓存（Redis）
            return redisTemplate.opsForValue().get(key)
                .doOnNext(value -> {
                    // 检查是否为空值标记（缓存穿透防护）
                    if (isNullValue(value)) {
                        hitCount.incrementAndGet();
                        LoggingUtil.debug(logger, "L2缓存命中空值: {}", key);
                        // 回写空值到L1缓存
                        Cache cache = cacheManager.getCache("default");
                        if (cache != null) {
                            cache.put(key, NULL_VALUE);
                        }
                    } else {
                        hitCount.incrementAndGet();
                        LoggingUtil.debug(logger, "L2缓存命中: {}", key);
                        // 回写到L1缓存
                        Cache cache = cacheManager.getCache("default");
                        if (cache != null) {
                            cache.put(key, value);
                        }
                    }
                })
                .filter(value -> !isNullValue(value))
                .cast(valueType)
                .switchIfEmpty(Mono.fromRunnable(() -> {
                    missCount.incrementAndGet();
                    LoggingUtil.debug(logger, "缓存未命中: {}", key);
                }));
        });
    }

    @Override
    public Mono<Boolean> set(String key, Object value, Duration ttl) {
        // 使用随机抖动TTL防止缓存雪崩
        Duration jitteredTtl = getJitteredTtl(ttl);
        
        return Mono.fromRunnable(() -> {
            // 设置L1缓存
            Cache cache = cacheManager.getCache("default");
            if (cache != null) {
                cache.put(key, value);
            }
        })
        .then(
            // 设置L2缓存（Redis）
            redisTemplate.opsForValue().set(key, value, jitteredTtl)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                    .maxBackoff(Duration.ofSeconds(2)))
                .doOnSuccess(result -> LoggingUtil.debug(logger, "缓存设置成功: {} -> {}", key, result))
                .doOnError(error -> LoggingUtil.error(logger, "缓存设置失败: {} - {}", key, error.getMessage(), error))
        );
    }

    @Override
    public Mono<Boolean> set(String key, Object value) {
        return set(key, value, defaultTtl);
    }

    @Override
    public Mono<Boolean> delete(String key) {
        return Mono.fromRunnable(() -> {
            // 删除L1缓存
            Cache cache = cacheManager.getCache("default");
            if (cache != null) {
                cache.evict(key);
            }
            evictionCount.incrementAndGet();
        })
        .then(
            // 删除L2缓存（Redis）
            redisTemplate.delete(key)
                .map(count -> count > 0)
                .doOnSuccess(result -> LoggingUtil.debug(logger, "缓存删除: {} -> {}", key, result))
                .doOnError(error -> LoggingUtil.error(logger, "缓存删除失败: {} - {}", key, error.getMessage(), error))
        );
    }

    @Override
    public Mono<Boolean> exists(String key) {
        return Mono.fromCallable(() -> {
            // 检查L1缓存
            Cache cache = cacheManager.getCache("default");
            if (cache != null && cache.get(key) != null) {
                return true;
            }
            return false;
        })
        .switchIfEmpty(
            // 检查L2缓存（Redis）
            redisTemplate.hasKey(key)
        );
    }

    // ==================== 批量缓存操作 ====================

    @Override
    public <T> Mono<Map<String, T>> multiGet(Flux<String> keys, Class<T> valueType) {
        return keys.collectList()
            .flatMap(keyList ->
                redisTemplate.opsForValue().multiGet(keyList)
                    .map(values -> {
                        Map<String, T> result = new ConcurrentHashMap<>();
                        for (int i = 0; i < keyList.size() && i < values.size(); i++) {
                            Object value = values.get(i);
                            if (value != null) {
                                result.put(keyList.get(i), valueType.cast(value));
                            }
                        }
                        return result;
                    })
            );
    }

    @Override
    public Mono<Boolean> multiSet(Map<String, Object> keyValues, Duration ttl) {
        return redisTemplate.opsForValue().multiSet(keyValues)
            .flatMap(result -> {
                if (result) {
                    // 为每个键设置过期时间
                    return Flux.fromIterable(keyValues.keySet())
                        .flatMap(key -> redisTemplate.expire(key, ttl))
                        .all(Boolean::booleanValue);
                }
                return Mono.just(false);
            });
    }

    @Override
    public Mono<Long> multiDelete(Flux<String> keys) {
        return keys.collectList()
            .flatMap(keyList -> {
                // 删除L1缓存
                keyList.forEach(key -> {
                    Cache cache = cacheManager.getCache("default");
                    if (cache != null) {
                        cache.evict(key);
                    }
                });
                evictionCount.addAndGet(keyList.size());

                // 删除L2缓存（Redis）
                return redisTemplate.delete(Flux.fromIterable(keyList));
            });
    }

    // ==================== 缓存失效和刷新 ====================

    @Override
    public Mono<Long> deleteByPattern(String pattern) {
        return redisTemplate.keys(pattern)
            .collectList()
            .flatMap(keys -> {
                if (keys.isEmpty()) {
                    return Mono.just(0L);
                }

                // 删除L1缓存
                keys.forEach(key -> {
                    Cache cache = cacheManager.getCache("default");
                    if (cache != null) {
                        cache.evict(key);
                    }
                });
                evictionCount.addAndGet(keys.size());

                // 删除L2缓存（Redis）
                return redisTemplate.delete(Flux.fromIterable(keys));
            });
    }

    @Override
    public <T> Mono<T> refresh(String key, Mono<T> supplier, Duration ttl) {
        return supplier
            .flatMap(value -> {
                if (value != null) {
                    // 使用随机抖动TTL防止缓存雪崩
                    Duration jitteredTtl = getJitteredTtl(ttl);
                    return set(key, value, jitteredTtl)
                        .thenReturn(value);
                } else {
                    // 缓存空值防止穿透
                    return set(key, NULL_VALUE, NULL_VALUE_TTL)
                        .then(Mono.<T>empty());
                }
            })
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                .maxBackoff(Duration.ofSeconds(2)))
            .doOnSuccess(value -> LoggingUtil.debug(logger, "缓存刷新成功: {}", key))
            .doOnError(error -> LoggingUtil.error(logger, "缓存刷新失败: {} - {}", key, error.getMessage(), error));
    }

    @Override
    public <T> Mono<T> getOrSet(String key, Mono<T> supplier, Duration ttl, Class<T> valueType) {
        return get(key, valueType)
            .switchIfEmpty(
                // 缓存未命中，使用分布式锁防止缓存击穿
                acquireLock(key)
                    .flatMap(acquired -> {
                        if (acquired) {
                            // 获取锁成功，执行数据加载
                            return supplier
                                .flatMap(value -> {
                                    if (value != null) {
                                        // 缓存实际数据
                                        return set(key, value, ttl).thenReturn(value);
                                    } else {
                                        // 缓存空值防止穿透
                                        return set(key, NULL_VALUE, NULL_VALUE_TTL)
                                            .then(Mono.<T>empty());
                                    }
                                })
                                .doFinally(signal -> {
                                    // 释放锁
                                    releaseLock(key).subscribe();
                                });
                        } else {
                            // 获取锁失败，等待并重试获取缓存
                            return Mono.delay(Duration.ofMillis(50))
                                .then(get(key, valueType))
                                .switchIfEmpty(
                                    // 如果仍然没有数据，返回空
                                    Mono.<T>empty()
                                );
                        }
                    })
                    .onErrorResume(error -> {
                        LoggingUtil.error(logger, "缓存击穿防护失败: {} - {}", key, error.getMessage(), error);
                        return Mono.<T>empty();
                    })
            );
    }

    // ==================== 缓存统计和监控 ====================

    @Override
    public Mono<CacheStats> getStats() {
        return Mono.just(new CacheStats(
            hitCount.get(),
            missCount.get(),
            evictionCount.get()
        ));
    }

    @Override
    public Mono<Long> size() {
        return redisTemplate.execute(connection ->
            connection.serverCommands().dbSize()
        ).next();
    }

    @Override
    public Mono<Boolean> clear() {
        return Mono.fromRunnable(() -> {
            // 清空L1缓存
            cacheManager.getCacheNames().forEach(cacheName -> {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        })
        .then(
            // 清空L2缓存（Redis）
            redisTemplate.execute(connection ->
                connection.serverCommands().flushDb()
            ).then(Mono.just(true))
        );
    }
}

