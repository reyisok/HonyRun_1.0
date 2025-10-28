package com.honyrun.service.cache;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存失效事件服务
 *
 * 提供统一的缓存失效事件发布和监听机制：
 * - 缓存失效事件定义和发布
 * - 缓存失效事件监听和处理
 * - 缓存失效统计和监控
 * - 缓存一致性保证机制
 *
 * 支持的失效事件类型：
 * - 手动失效：用户主动清除缓存
 * - 自动失效：TTL过期自动清除
 * - 更新失效：数据更新时清除相关缓存
 * - 批量失效：批量清除匹配模式的缓存
 * - 级联失效：关联数据变更时级联清除缓存
 *
 * @author Mr.Rey
 * @version 1.0.0
 * @created 2025-07-01 12:00:00
 * @modified 2025-07-01 12:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
public class CacheEvictionEventService {

    private static final Logger logger = LoggerFactory.getLogger(CacheEvictionEventService.class);

    private final ApplicationEventPublisher eventPublisher;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param eventPublisher 应用事件发布器
     * @param reactiveRedisTemplate Redis模板
     */
    public CacheEvictionEventService(ApplicationEventPublisher eventPublisher,
                                   @Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.eventPublisher = eventPublisher;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }



    // ==================== 失效事件统计 ====================

    /**
     * 失效事件计数器
     */
    private final AtomicLong evictionEventCount = new AtomicLong(0);

    /**
     * 失效事件类型统计
     */
    private final Map<String, AtomicLong> evictionTypeStats = new ConcurrentHashMap<>();

    /**
     * 失效事件监听器注册表
     */
    private final Map<String, CacheEvictionEventListener> eventListeners = new ConcurrentHashMap<>();

    // ==================== 缓存失效事件定义 ====================

    /**
     * 缓存失效事件
     */
    public static class CacheEvictionEvent {
        private final String cacheKey;
        private final String evictionType;
        private final String evictionReason;
        private final LocalDateTime eventTime;
        private final Map<String, Object> metadata;

        public CacheEvictionEvent(String cacheKey, String evictionType, String evictionReason) {
            this.cacheKey = cacheKey;
            this.evictionType = evictionType;
            this.evictionReason = evictionReason;
            this.eventTime = LocalDateTime.now();
            this.metadata = new ConcurrentHashMap<>();
        }

        public CacheEvictionEvent(String cacheKey, String evictionType, String evictionReason, Map<String, Object> metadata) {
            this.cacheKey = cacheKey;
            this.evictionType = evictionType;
            this.evictionReason = evictionReason;
            this.eventTime = LocalDateTime.now();
            this.metadata = metadata != null ? new ConcurrentHashMap<>(metadata) : new ConcurrentHashMap<>();
        }

        // Getters
        public String getCacheKey() { return cacheKey; }
        public String getEvictionType() { return evictionType; }
        public String getEvictionReason() { return evictionReason; }
        public LocalDateTime getEventTime() { return eventTime; }
        public Map<String, Object> getMetadata() { return metadata; }

        @Override
        public String toString() {
            return "CacheEvictionEvent{" +
                    "cacheKey='" + cacheKey + '\'' +
                    ", evictionType='" + evictionType + '\'' +
                    ", evictionReason='" + evictionReason + '\'' +
                    ", eventTime=" + eventTime +
                    ", metadata=" + metadata +
                    '}';
        }
    }

    /**
     * 缓存失效事件监听器接口
     */
    public interface CacheEvictionEventListener {
        /**
         * 处理缓存失效事件
         *
         * @param event 失效事件
         */
        void onCacheEvicted(CacheEvictionEvent event);

        /**
         * 获取监听器名称
         *
         * @return 监听器名称
         */
        String getListenerName();

        /**
         * 获取监听的缓存键模式
         *
         * @return 缓存键模式
         */
        String[] getKeyPatterns();
    }

    // ==================== 失效事件类型常量 ====================

    /**
     * 手动失效
     */
    public static final String EVICTION_TYPE_MANUAL = "MANUAL";

    /**
     * TTL过期失效
     */
    public static final String EVICTION_TYPE_TTL_EXPIRED = "TTL_EXPIRED";

    /**
     * 数据更新失效
     */
    public static final String EVICTION_TYPE_DATA_UPDATE = "DATA_UPDATE";

    /**
     * 批量失效
     */
    public static final String EVICTION_TYPE_BATCH = "BATCH";

    /**
     * 级联失效
     */
    public static final String EVICTION_TYPE_CASCADE = "CASCADE";

    /**
     * 系统维护失效
     */
    public static final String EVICTION_TYPE_MAINTENANCE = "MAINTENANCE";

    /**
     * 内存不足失效
     */
    public static final String EVICTION_TYPE_MEMORY_PRESSURE = "MEMORY_PRESSURE";

    // ==================== 事件发布方法 ====================

    /**
     * 发布手动失效事件
     *
     * @param cacheKey 缓存键
     * @param reason 失效原因
     * @return 处理结果
     */
    public Mono<Void> publishManualEvictionEvent(String cacheKey, String reason) {
        return publishEvictionEvent(cacheKey, EVICTION_TYPE_MANUAL, reason);
    }

    /**
     * 发布数据更新失效事件
     *
     * @param cacheKey 缓存键
     * @param reason 失效原因
     * @return 处理结果
     */
    public Mono<Void> publishDataUpdateEvictionEvent(String cacheKey, String reason) {
        return publishEvictionEvent(cacheKey, EVICTION_TYPE_DATA_UPDATE, reason);
    }

    /**
     * 发布批量失效事件
     *
     * @param keyPattern 键模式
     * @param reason 失效原因
     * @return 处理结果
     */
    public Mono<Void> publishBatchEvictionEvent(String keyPattern, String reason) {
        return publishEvictionEvent(keyPattern, EVICTION_TYPE_BATCH, reason);
    }

    /**
     * 发布级联失效事件
     *
     * @param cacheKey 缓存键
     * @param reason 失效原因
     * @param relatedKeys 关联键
     * @return 处理结果
     */
    public Mono<Void> publishCascadeEvictionEvent(String cacheKey, String reason, String[] relatedKeys) {
        Map<String, Object> metadata = new ConcurrentHashMap<>();
        metadata.put("relatedKeys", relatedKeys);
        return publishEvictionEvent(cacheKey, EVICTION_TYPE_CASCADE, reason, metadata);
    }

    /**
     * 发布缓存失效事件
     *
     * @param cacheKey 缓存键
     * @param evictionType 失效类型
     * @param reason 失效原因
     * @return 处理结果
     */
    public Mono<Void> publishEvictionEvent(String cacheKey, String evictionType, String reason) {
        return publishEvictionEvent(cacheKey, evictionType, reason, null);
    }

    /**
     * 发布缓存失效事件（带元数据）
     *
     * @param cacheKey 缓存键
     * @param evictionType 失效类型
     * @param reason 失效原因
     * @param metadata 元数据
     * @return 处理结果
     */
    public Mono<Void> publishEvictionEvent(String cacheKey, String evictionType, String reason, Map<String, Object> metadata) {
        return Mono.fromRunnable(() -> {
            try {
                CacheEvictionEvent event = new CacheEvictionEvent(cacheKey, evictionType, reason, metadata);

                // 发布Spring事件
                eventPublisher.publishEvent(event);

                // 更新统计
                evictionEventCount.incrementAndGet();
                evictionTypeStats.computeIfAbsent(evictionType, k -> new AtomicLong(0)).incrementAndGet();

                LoggingUtil.info(logger, "发布缓存失效事件成功，缓存键: {}, 失效类型: {}, 失效原因: {}",
                        cacheKey, evictionType, reason);

            } catch (Exception e) {
                LoggingUtil.error(logger, "发布缓存失效事件失败，缓存键: " + cacheKey + ", 失效类型: " + evictionType, e);
                throw new RuntimeException("发布缓存失效事件失败", e);
            }
        });
    }

    // ==================== 事件监听方法 ====================

    /**
     * 监听缓存失效事件
     *
     * @param event 失效事件
     */
    @EventListener
    public void handleCacheEvictionEvent(CacheEvictionEvent event) {
        LoggingUtil.debug(logger, "处理缓存失效事件: {}", event);

        // 通知注册的监听器
        eventListeners.values().forEach(listener -> {
            try {
                String[] keyPatterns = listener.getKeyPatterns();
                boolean shouldHandle = false;

                for (String pattern : keyPatterns) {
                    if (event.getCacheKey().matches(pattern.replace("*", ".*"))) {
                        shouldHandle = true;
                        break;
                    }
                }

                if (shouldHandle) {
                    listener.onCacheEvicted(event);
                    LoggingUtil.debug(logger, "缓存失效事件处理成功，监听器: {}", listener.getListenerName());
                }

            } catch (Exception e) {
                LoggingUtil.error(logger, "缓存失效事件处理失败，监听器: " + listener.getListenerName(), e);
            }
        });
    }

    // ==================== 监听器管理 ====================

    /**
     * 注册缓存失效事件监听器
     *
     * @param listener 监听器
     */
    public void registerEventListener(CacheEvictionEventListener listener) {
        eventListeners.put(listener.getListenerName(), listener);
        LoggingUtil.info(logger, "注册缓存失效事件监听器: {}, 监听模式: {}",
                listener.getListenerName(), String.join(", ", listener.getKeyPatterns()));
    }

    /**
     * 移除缓存失效事件监听器
     *
     * @param listenerName 监听器名称
     */
    public void removeEventListener(String listenerName) {
        CacheEvictionEventListener removed = eventListeners.remove(listenerName);
        if (removed != null) {
            LoggingUtil.info(logger, "移除缓存失效事件监听器: {}", listenerName);
        } else {
            LoggingUtil.warn(logger, "未找到要移除的缓存失效事件监听器: {}", listenerName);
        }
    }

    /**
     * 获取已注册的监听器列表
     *
     * @return 监听器名称列表
     */
    public String[] getRegisteredListeners() {
        return eventListeners.keySet().toArray(new String[0]);
    }

    // ==================== 缓存失效操作 ====================

    /**
     * 手动失效单个缓存并发布事件
     *
     * @param cacheKey 缓存键
     * @param reason 失效原因
     * @return 是否成功失效
     */
    public Mono<Boolean> evictCacheWithEvent(String cacheKey, String reason) {
        LoggingUtil.info(logger, "手动失效缓存并发布事件，缓存键: {}, 失效原因: {}", cacheKey, reason);

        return reactiveRedisTemplate.delete(cacheKey)
                .flatMap(deleted -> {
                    if (deleted > 0) {
                        return publishManualEvictionEvent(cacheKey, reason)
                                .thenReturn(true);
                    } else {
                        LoggingUtil.warn(logger, "缓存键不存在或已失效: {}", cacheKey);
                        return Mono.just(false);
                    }
                })
                .doOnError(error -> LoggingUtil.error(logger, "手动失效缓存失败，缓存键: " + cacheKey, error));
    }

    /**
     * 批量失效缓存并发布事件
     *
     * @param keyPattern 键模式
     * @param reason 失效原因
     * @return 失效的缓存数量
     */
    public Mono<Long> evictCachesBatchWithEvent(String keyPattern, String reason) {
        LoggingUtil.info(logger, "批量失效缓存并发布事件，键模式: {}, 失效原因: {}", keyPattern, reason);

        return reactiveRedisTemplate.keys(keyPattern)
                .collectList()
                .flatMap(keys -> {
                    if (keys.isEmpty()) {
                        LoggingUtil.info(logger, "未找到匹配的缓存键，键模式: {}", keyPattern);
                        return Mono.just(0L);
                    }

                    // 发布批量失效事件
                    Mono<Void> batchEvent = publishBatchEvictionEvent(keyPattern, reason);

                    // 为每个键发布单独的失效事件
                    Flux<Void> individualEvents = Flux.fromIterable(keys)
                            .flatMap(key -> publishManualEvictionEvent(key, reason));

                    // 删除缓存
                    Mono<Long> deleteResult = reactiveRedisTemplate.delete(keys.toArray(new String[0]));

                    return batchEvent.then(individualEvents.then(deleteResult));
                })
                .doOnSuccess(count -> LoggingUtil.info(logger, "批量缓存失效完成，键模式: {}, 失效数量: {}", keyPattern, count))
                .doOnError(error -> LoggingUtil.error(logger, "批量缓存失效失败，键模式: " + keyPattern, error));
    }

    /**
     * 级联失效相关缓存并发布事件
     *
     * @param primaryKey 主缓存键
     * @param relatedKeyPatterns 关联键模式
     * @param reason 失效原因
     * @return 失效的缓存数量
     */
    public Mono<Long> evictCascadeWithEvent(String primaryKey, String[] relatedKeyPatterns, String reason) {
        LoggingUtil.info(logger, "级联失效缓存并发布事件，主键: {}, 关联模式: {}, 失效原因: {}",
                primaryKey, String.join(", ", relatedKeyPatterns), reason);

        // 收集所有相关键
        Flux<String> allRelatedKeys = Flux.fromArray(relatedKeyPatterns)
                .flatMap(pattern -> reactiveRedisTemplate.keys(pattern));

        return allRelatedKeys.collectList()
                .flatMap(relatedKeys -> {
                    // 发布级联失效事件
                    Mono<Void> cascadeEvent = publishCascadeEvictionEvent(primaryKey, reason, relatedKeys.toArray(new String[0]));

                    // 删除主键
                    Mono<Long> deletePrimary = reactiveRedisTemplate.delete(primaryKey);

                    // 删除关联键
                    Mono<Long> deleteRelated = relatedKeys.isEmpty() ?
                            Mono.just(0L) :
                            reactiveRedisTemplate.delete(relatedKeys.toArray(new String[0]));

                    return cascadeEvent.then(
                            Mono.zip(deletePrimary, deleteRelated)
                                    .map(tuple -> tuple.getT1() + tuple.getT2())
                    );
                })
                .doOnSuccess(count -> LoggingUtil.info(logger, "级联缓存失效完成，主键: {}, 失效数量: {}", primaryKey, count))
                .doOnError(error -> LoggingUtil.error(logger, "级联缓存失效失败，主键: " + primaryKey, error));
    }

    // ==================== 统计和监控 ====================

    /**
     * 获取失效事件统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getEvictionStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalEvictionEvents", evictionEventCount.get());
        stats.put("evictionTypeStats", evictionTypeStats.entrySet().stream()
                .collect(ConcurrentHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue().get()),
                        ConcurrentHashMap::putAll));
        stats.put("registeredListeners", eventListeners.size());
        stats.put("listenerNames", eventListeners.keySet());

        LoggingUtil.debug(logger, "获取失效事件统计信息: {}", stats);
        return stats;
    }

    /**
     * 重置失效事件统计
     */
    public void resetEvictionStatistics() {
        evictionEventCount.set(0);
        evictionTypeStats.clear();
        LoggingUtil.info(logger, "重置失效事件统计信息");
    }

    /**
     * 获取失效事件计数
     *
     * @return 事件计数
     */
    public long getEvictionEventCount() {
        return evictionEventCount.get();
    }

    /**
     * 获取指定类型的失效事件计数
     *
     * @param evictionType 失效类型
     * @return 事件计数
     */
    public long getEvictionEventCount(String evictionType) {
        AtomicLong counter = evictionTypeStats.get(evictionType);
        return counter != null ? counter.get() : 0;
    }
}
