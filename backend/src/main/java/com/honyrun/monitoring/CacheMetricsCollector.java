package com.honyrun.monitoring;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.honyrun.constant.CacheConstants;
import com.honyrun.util.LoggingUtil;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;

/**
 * 缓存指标收集器
 *
 * 集成Micrometer提供缓存性能指标的收集和监控
 * 支持Redis缓存、本地缓存的全面监控，专注于后端指标收集
 *
 * 【监控指标】：
 * - 缓存命中率统计
 * - 缓存操作耗时
 * - 缓存大小监控
 * - 缓存异常统计
 * - 缓存热点分析
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-26 01:36:12
 * @version 1.0.1 - 移除可视化依赖，专注于后端指标收集
 */
@Component
public class CacheMetricsCollector {

    private static final Logger logger = LoggerFactory.getLogger(CacheMetricsCollector.class);
    private final MeterRegistry meterRegistry;
    private final CacheManager cacheManager;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    // 缓存操作计数器
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Counter cacheErrorCounter;
    private final Counter cacheEvictionCounter;

    // 缓存操作计时器
    private final Timer cacheGetTimer;
    private final Timer cachePutTimer;
    private final Timer cacheEvictTimer;

    // 缓存统计数据
    private final ConcurrentHashMap<String, CacheStats> cacheStatsMap = new ConcurrentHashMap<>();
    private final AtomicLong totalCacheOperations = new AtomicLong(0);
    private final AtomicLong totalCacheSize = new AtomicLong(0);

    // 热点缓存统计
    private final ConcurrentHashMap<String, LongAdder> hotKeysCounter = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> keyLastAccessTime = new ConcurrentHashMap<>();

    /**
     * 构造函数注入依赖
     *
     * @param meterRegistry         指标注册器
     * @param cacheManager          缓存管理器
     * @param reactiveRedisTemplate Redis模板
     */
    public CacheMetricsCollector(MeterRegistry meterRegistry,
            CacheManager cacheManager,
            ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.meterRegistry = meterRegistry;
        this.cacheManager = cacheManager;
        this.reactiveRedisTemplate = reactiveRedisTemplate;

        // 初始化计数器
        this.cacheHitCounter = Counter.builder("cache.hit.total")
                .description("缓存命中总次数")
                .register(meterRegistry);

        this.cacheMissCounter = Counter.builder("cache.miss.total")
                .description("缓存未命中总次数")
                .register(meterRegistry);

        this.cacheErrorCounter = Counter.builder("cache.error.total")
                .description("缓存操作错误总次数")
                .register(meterRegistry);

        this.cacheEvictionCounter = Counter.builder("cache.eviction.total")
                .description("缓存驱逐总次数")
                .register(meterRegistry);

        // 初始化计时器
        this.cacheGetTimer = Timer.builder("cache.get.duration")
                .description("缓存获取操作耗时")
                .register(meterRegistry);

        this.cachePutTimer = Timer.builder("cache.put.duration")
                .description("缓存存储操作耗时")
                .register(meterRegistry);

        this.cacheEvictTimer = Timer.builder("cache.evict.duration")
                .description("缓存驱逐操作耗时")
                .register(meterRegistry);

        LoggingUtil.info(logger, "缓存指标收集器初始化完成");
    }

    /**
     * 初始化组件后的回调方法，用于注册需要 this 引用的指标
     * 使用 @PostConstruct 避免 this-escape 警告
     */
    @PostConstruct
    private void initializeGaugeMetrics() {
        // 注册缓存大小指标
        Gauge.builder("cache.size.total", this, collector -> collector.totalCacheSize.get())
                .description("缓存总大小")
                .register(meterRegistry);

        Gauge.builder("cache.operations.total", this, collector -> collector.totalCacheOperations.get())
                .description("缓存操作总次数")
                .register(meterRegistry);

        // 注册缓存命中率指标
        Gauge.builder("cache.hit.ratio", this, collector -> collector.calculateOverallHitRatio())
                .description("缓存命中率")
                .register(meterRegistry);

        // 注册Spring Cache指标
        registerSpringCacheMetrics();
    }

    /**
     * 注册Spring Cache指标
     *
     * 为Spring Cache Manager管理的所有缓存注册监控指标
     */
    private void registerSpringCacheMetrics() {
        if (cacheManager != null) {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            LoggingUtil.info(logger, "注册Spring Cache指标", "缓存数量", cacheNames.size());

            for (String cacheName : cacheNames) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    // 使用Spring Boot的缓存指标注册方式
                    // 为每个缓存创建自定义指标
                    Gauge.builder("cache.size", cache, c -> {
                        try {
                            // 尝试获取缓存大小，如果不支持则返回-1
                            return c.getNativeCache() instanceof java.util.Map
                                    ? ((java.util.Map<?, ?>) c.getNativeCache()).size()
                                    : -1;
                        } catch (Exception e) {
                            return -1;
                        }
                    })
                            .tag("cache", cacheName)
                            .description("缓存大小")
                            .register(meterRegistry);
                }
            }
        }
    }

    /**
     * 记录缓存命中
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     */
    public void recordCacheHit(String cacheName, String key) {
        cacheHitCounter.increment();
        totalCacheOperations.incrementAndGet();

        CacheStats stats = getOrCreateCacheStats(cacheName);
        stats.recordHit();

        recordKeyAccess(key);

        LoggingUtil.debug(logger, "缓存命中: cache={}, key={}", cacheName, key);
    }

    /**
     * 记录缓存未命中
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     */
    public void recordCacheMiss(String cacheName, String key) {
        cacheMissCounter.increment();
        totalCacheOperations.incrementAndGet();

        CacheStats stats = getOrCreateCacheStats(cacheName);
        stats.recordMiss();

        LoggingUtil.debug(logger, "缓存未命中: cache={}, key={}", cacheName, key);
    }

    /**
     * 记录缓存错误
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param error     错误信息
     */
    public void recordCacheError(String cacheName, String key, Throwable error) {
        cacheErrorCounter.increment();

        CacheStats stats = getOrCreateCacheStats(cacheName);
        stats.recordError();

        LoggingUtil.warn(logger, "缓存操作错误: cache={}, key={}, error={}",
                cacheName, key, error.getMessage());
    }

    /**
     * 记录缓存驱逐
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     */
    public void recordCacheEviction(String cacheName, String key) {
        cacheEvictionCounter.increment();

        CacheStats stats = getOrCreateCacheStats(cacheName);
        stats.recordEviction();

        LoggingUtil.debug(logger, "缓存驱逐: cache={}, key={}", cacheName, key);
    }

    /**
     * 记录缓存获取操作耗时
     *
     * @param cacheName 缓存名称
     * @param duration  耗时
     */
    public void recordCacheGetDuration(String cacheName, Duration duration) {
        cacheGetTimer.record(duration);

        CacheStats stats = getOrCreateCacheStats(cacheName);
        stats.recordGetDuration(duration);
    }

    /**
     * 记录缓存存储操作耗时
     *
     * @param cacheName 缓存名称
     * @param duration  耗时
     */
    public void recordCachePutDuration(String cacheName, Duration duration) {
        cachePutTimer.record(duration);

        CacheStats stats = getOrCreateCacheStats(cacheName);
        stats.recordPutDuration(duration);
    }

    /**
     * 记录缓存驱逐操作耗时
     *
     * @param cacheName 缓存名称
     * @param duration  耗时
     */
    public void recordCacheEvictDuration(String cacheName, Duration duration) {
        cacheEvictTimer.record(duration);

        CacheStats stats = getOrCreateCacheStats(cacheName);
        stats.recordEvictDuration(duration);
    }

    /**
     * 记录键访问
     */
    private void recordKeyAccess(String key) {
        hotKeysCounter.computeIfAbsent(key, k -> new LongAdder()).increment();
        keyLastAccessTime.put(key, LocalDateTime.now());
    }

    /**
     * 获取或创建缓存统计
     */
    private CacheStats getOrCreateCacheStats(String cacheName) {
        return cacheStatsMap.computeIfAbsent(cacheName, name -> {
            CacheStats stats = new CacheStats(name);
            registerCacheStatsMetrics(stats);
            return stats;
        });
    }

    /**
     * 注册缓存统计指标
     */
    private void registerCacheStatsMetrics(CacheStats stats) {
        String cacheName = stats.getCacheName();

        Gauge.builder("cache.hit.ratio.by.name", stats, CacheStats::getHitRatio)
                .description("按缓存名称统计的命中率")
                .tag("cache", cacheName)
                .register(meterRegistry);

        Gauge.builder("cache.size.by.name", stats, CacheStats::getSize)
                .description("按缓存名称统计的大小")
                .tag("cache", cacheName)
                .register(meterRegistry);

        Gauge.builder("cache.operations.by.name", stats, CacheStats::getTotalOperations)
                .description("按缓存名称统计的操作次数")
                .tag("cache", cacheName)
                .register(meterRegistry);
    }

    /**
     * 定期收集Redis缓存指标
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void collectRedisMetrics() {
        try {
            // 收集Redis缓存大小
            collectRedisCacheSize();

            // 清理过期的热点键统计
            cleanupExpiredHotKeys();

            LoggingUtil.debug(logger, "Redis缓存指标收集完成");

        } catch (Exception e) {
            LoggingUtil.error(logger, "收集Redis缓存指标时发生错误", e);
        }
    }

    /**
     * 收集Redis缓存大小
     */
    private void collectRedisCacheSize() {
        long totalSize = 0;

        // 统计各个缓存的大小
        for (String cachePrefix : getCachePrefixes()) {
            try {
                String pattern = cachePrefix + "*";
                Long count = reactiveRedisTemplate.keys(pattern)
                        .count()
                        .block(Duration.ofSeconds(5));

                if (count != null) {
                    totalSize += count;

                    // 更新单个缓存的大小统计
                    CacheStats stats = getOrCreateCacheStats(cachePrefix);
                    stats.setSize(count);
                }

            } catch (Exception e) {
                LoggingUtil.warn(logger, "统计缓存大小失败: prefix={}, error={}",
                        cachePrefix, e.getMessage());
            }
        }

        totalCacheSize.set(totalSize);
    }

    /**
     * 获取缓存前缀列表
     */
    private String[] getCachePrefixes() {
        return new String[] {
                CacheConstants.USER_KEY_PREFIX,
                CacheConstants.PERMISSION_KEY_PREFIX,
                CacheConstants.SYSTEM_CONFIG_KEY_PREFIX,
                CacheConstants.TOKEN_BLACKLIST_KEY_PREFIX,
                CacheConstants.SECURITY_DETECTION_KEY_PREFIX
        };
    }

    /**
     * 清理过期的热点键统计
     */
    private void cleanupExpiredHotKeys() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);

        keyLastAccessTime.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(cutoffTime)) {
                hotKeysCounter.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    /**
     * 计算总体命中率
     */
    private double calculateOverallHitRatio() {
        long totalHits = 0;
        long totalRequests = 0;

        for (CacheStats stats : cacheStatsMap.values()) {
            totalHits += stats.getHitCount();
            totalRequests += stats.getTotalOperations();
        }

        return totalRequests > 0 ? (double) totalHits / totalRequests : 0.0;
    }

    /**
     * 获取热点键统计
     *
     * @param topN 返回前N个热点键
     * @return 热点键及其访问次数
     */
    public java.util.Map<String, Long> getHotKeys(int topN) {
        return hotKeysCounter.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().longValue(), e1.getValue().longValue()))
                .limit(topN)
                .collect(java.util.stream.Collectors.toMap(
                        java.util.Map.Entry::getKey,
                        entry -> entry.getValue().longValue(),
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new));
    }

    /**
     * 获取缓存统计信息
     *
     * @param cacheName 缓存名称
     * @return 缓存统计信息
     */
    public CacheStats getCacheStats(String cacheName) {
        return cacheStatsMap.get(cacheName);
    }

    /**
     * 获取所有缓存统计信息
     *
     * @return 所有缓存统计信息
     */
    public java.util.Map<String, CacheStats> getAllCacheStats() {
        return new ConcurrentHashMap<>(cacheStatsMap);
    }

    /**
     * 缓存统计数据类
     */
    public static class CacheStats {
        private final String cacheName;
        private final AtomicLong hitCount = new AtomicLong(0);
        private final AtomicLong missCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong evictionCount = new AtomicLong(0);
        private volatile long size = 0;

        // 操作耗时统计
        private final LongAdder totalGetDuration = new LongAdder();
        private final LongAdder totalPutDuration = new LongAdder();
        private final LongAdder totalEvictDuration = new LongAdder();
        private final AtomicLong getOperationCount = new AtomicLong(0);
        private final AtomicLong putOperationCount = new AtomicLong(0);
        private final AtomicLong evictOperationCount = new AtomicLong(0);

        public CacheStats(String cacheName) {
            this.cacheName = cacheName;
        }

        public void recordHit() {
            hitCount.incrementAndGet();
        }

        public void recordMiss() {
            missCount.incrementAndGet();
        }

        public void recordError() {
            errorCount.incrementAndGet();
        }

        public void recordEviction() {
            evictionCount.incrementAndGet();
        }

        public void recordGetDuration(Duration duration) {
            totalGetDuration.add(duration.toNanos());
            getOperationCount.incrementAndGet();
        }

        public void recordPutDuration(Duration duration) {
            totalPutDuration.add(duration.toNanos());
            putOperationCount.incrementAndGet();
        }

        public void recordEvictDuration(Duration duration) {
            totalEvictDuration.add(duration.toNanos());
            evictOperationCount.incrementAndGet();
        }

        public void setSize(long size) {
            this.size = size;
        }

        // Getter方法
        public String getCacheName() {
            return cacheName;
        }

        public long getHitCount() {
            return hitCount.get();
        }

        public long getMissCount() {
            return missCount.get();
        }

        public long getErrorCount() {
            return errorCount.get();
        }

        public long getEvictionCount() {
            return evictionCount.get();
        }

        public long getSize() {
            return size;
        }

        public long getTotalOperations() {
            return hitCount.get() + missCount.get();
        }

        public double getHitRatio() {
            long total = getTotalOperations();
            return total > 0 ? (double) hitCount.get() / total : 0.0;
        }

        public double getAverageGetDuration() {
            long count = getOperationCount.get();
            return count > 0 ? (double) totalGetDuration.sum() / count / 1_000_000 : 0.0; // 转换为毫秒
        }

        public double getAveragePutDuration() {
            long count = putOperationCount.get();
            return count > 0 ? (double) totalPutDuration.sum() / count / 1_000_000 : 0.0; // 转换为毫秒
        }

        public double getAverageEvictDuration() {
            long count = evictOperationCount.get();
            return count > 0 ? (double) totalEvictDuration.sum() / count / 1_000_000 : 0.0; // 转换为毫秒
        }
    }
}
