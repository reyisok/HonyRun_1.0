package com.honyrun.service.monitoring;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.honyrun.util.LoggingUtil;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import reactor.core.publisher.Mono;

/**
 * 预热机制量化监控指标服务
 *
 * <p>
 * 提供预热机制的量化监控指标收集、统计和分析功能，包括：
 * <ul>
 * <li>预热执行时间统计</li>
 * <li>预热成功率监控</li>
 * <li>预热效果评估</li>
 * <li>预热性能指标收集</li>
 * <li>预热异常监控</li>
 * </ul>
 *
 * <p>
 * <strong>监控指标类型：</strong>
 * <ul>
 * <li>计数器指标：预热执行次数、成功次数、失败次数</li>
 * <li>计时器指标：预热执行时间、各阶段耗时</li>
 * <li>仪表指标：预热效果评分、缓存命中率提升</li>
 * <li>分布指标：预热数据量分布、性能提升分布</li>
 * </ul>
 *
 * <p>
 * <strong>使用示例：</strong>
 * 
 * <pre>{@code
 * // 记录预热开始
 * Timer.Sample sample = preheatingMetricsService.startPreheatingTimer("cache_warmup");
 *
 * // 执行预热逻辑
 * performCacheWarmup();
 *
 * // 记录预热完成
 * preheatingMetricsService.recordPreheatingSuccess("cache_warmup", sample);
 * }</pre>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01 18:45:00
 * @modified 2025-07-01 18:45:00
 * @version 1.0.0
 * @see MeterRegistry Micrometer指标注册表
 * @see Timer 计时器指标
 * @see Counter 计数器指标
 * @see Gauge 仪表指标
 */
@Service
public class PreheatingMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(PreheatingMetricsService.class);

    private final MeterRegistry meterRegistry;

    /**
     * 构造函数注入
     *
     * @param meterRegistry 指标注册表
     */
    public PreheatingMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // ==================== 指标计数器 ====================

    private final AtomicLong totalPreheatingExecutions = new AtomicLong(0);
    private final AtomicLong successfulPreheatingExecutions = new AtomicLong(0);
    private final AtomicLong failedPreheatingExecutions = new AtomicLong(0);

    // ==================== 预热统计数据 ====================

    private final Map<String, PreheatingStats> preheatingStatsMap = new ConcurrentHashMap<>();
    private final Map<String, Timer> preheatingTimers = new ConcurrentHashMap<>();
    private final Map<String, Counter> preheatingCounters = new ConcurrentHashMap<>();

    // ==================== 预热效果评估 ====================

    private final Map<String, Double> preheatingEffectiveness = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastPreheatingTime = new ConcurrentHashMap<>();

    /**
     * 初始化预热监控指标
     */
    public void initializeMetrics() {
        LoggingUtil.info(logger, "初始化预热机制监控指标");

        try {
            // 注册基础计数器指标
            registerCounterMetrics();

            // 注册仪表指标
            registerGaugeMetrics();

            // 注册计时器指标
            registerTimerMetrics();

            LoggingUtil.info(logger, "预热机制监控指标初始化完成");

        } catch (Exception e) {
            LoggingUtil.error(logger, "预热机制监控指标初始化失败", e);
            throw new RuntimeException("预热机制监控指标初始化失败", e);
        }
    }

    /**
     * 注册计数器指标
     */
    private void registerCounterMetrics() {
        // 预热执行总次数
        Counter.builder("preheating.executions.total")
                .description("预热机制执行总次数")
                .tag("type", "total")
                .register(meterRegistry);

        // 预热成功次数
        Counter.builder("preheating.executions.success")
                .description("预热机制成功执行次数")
                .tag("type", "success")
                .register(meterRegistry);

        // 预热失败次数
        Counter.builder("preheating.executions.failure")
                .description("预热机制失败执行次数")
                .tag("type", "failure")
                .register(meterRegistry);
    }

    /**
     * 注册仪表指标
     */
    private void registerGaugeMetrics() {
        // 预热成功率
        Gauge.builder("preheating.success.rate", this, PreheatingMetricsService::calculateSuccessRate)
                .description("预热机制成功率")
                .register(meterRegistry);

        // 预热效果评分
        Gauge.builder("preheating.effectiveness.score", this, PreheatingMetricsService::calculateEffectivenessScore)
                .description("预热效果评分")
                .register(meterRegistry);

        // 活跃预热任务数
        Gauge.builder("preheating.tasks.active", this, PreheatingMetricsService::getActivePreheatingTasks)
                .description("当前活跃的预热任务数")
                .register(meterRegistry);
    }

    /**
     * 注册计时器指标
     */
    private void registerTimerMetrics() {
        // 预热执行时间
        Timer.builder("preheating.execution.duration")
                .description("预热机制执行时间")
                .register(meterRegistry);
    }

    /**
     * 开始预热计时
     *
     * @param preheatingType 预热类型
     * @return Timer.Sample 计时器样本
     */
    public Timer.Sample startPreheatingTimer(String preheatingType) {
        LoggingUtil.debug(logger, "开始预热计时 - 类型: {}", preheatingType);

        Timer timer = getOrCreateTimer(preheatingType);
        Timer.Sample sample = Timer.start(meterRegistry);

        // 增加执行次数计数
        totalPreheatingExecutions.incrementAndGet();
        incrementCounter("preheating.executions.total", preheatingType);

        return sample;
    }

    /**
     * 记录预热成功
     *
     * @param preheatingType 预热类型
     * @param sample         计时器样本
     */
    public void recordPreheatingSuccess(String preheatingType, Timer.Sample sample) {
        LoggingUtil.debug(logger, "记录预热成功 - 类型: {}", preheatingType);

        try {
            // 停止计时并记录
            Timer timer = getOrCreateTimer(preheatingType);
            long durationNanos = sample.stop(timer);
            Duration duration = Duration.ofNanos(durationNanos);

            // 增加成功次数计数
            successfulPreheatingExecutions.incrementAndGet();
            incrementCounter("preheating.executions.success", preheatingType);

            // 更新预热统计
            updatePreheatingStats(preheatingType, duration, true);

            // 记录预热时间
            lastPreheatingTime.put(preheatingType, LocalDateTime.now());

            LoggingUtil.info(logger, "预热成功记录完成 - 类型: {}, 耗时: {}ms",
                    preheatingType, duration.toMillis());

        } catch (Exception e) {
            LoggingUtil.error(logger, "记录预热成功失败 - 类型: " + preheatingType, e);
        }
    }

    /**
     * 记录预热失败
     *
     * @param preheatingType 预热类型
     * @param sample         计时器样本
     * @param error          错误信息
     */
    public void recordPreheatingFailure(String preheatingType, Timer.Sample sample, Throwable error) {
        LoggingUtil.warn(logger, "记录预热失败 - 类型: {}, 错误: {}", preheatingType, error.getMessage());

        try {
            // 停止计时并记录
            Timer timer = getOrCreateTimer(preheatingType);
            long durationNanos = sample.stop(timer);
            Duration duration = Duration.ofNanos(durationNanos);

            // 增加失败次数计数
            failedPreheatingExecutions.incrementAndGet();
            incrementCounter("preheating.executions.failure", preheatingType);

            // 更新预热统计
            updatePreheatingStats(preheatingType, duration, false);

            LoggingUtil.warn(logger, "预热失败记录完成 - 类型: {}, 耗时: {}ms, 错误: {}",
                    preheatingType, duration.toMillis(), error.getMessage());

        } catch (Exception e) {
            LoggingUtil.error(logger, "记录预热失败失败 - 类型: " + preheatingType, e);
        }
    }

    /**
     * 记录预热效果评估
     *
     * @param preheatingType     预热类型
     * @param effectivenessScore 效果评分 (0.0-1.0)
     */
    public void recordPreheatingEffectiveness(String preheatingType, double effectivenessScore) {
        LoggingUtil.debug(logger, "记录预热效果评估 - 类型: {}, 评分: {}", preheatingType, effectivenessScore);

        preheatingEffectiveness.put(preheatingType, effectivenessScore);

        // 记录效果评分指标
        Gauge.builder("preheating.effectiveness.score", () -> effectivenessScore)
                .description("预热效果评分")
                .tag("type", preheatingType)
                .register(meterRegistry);
    }

    /**
     * 记录预热性能提升指标
     *
     * @param preheatingType        预热类型
     * @param improvementPercentage 性能提升百分比
     */
    public void recordPreheatingPerformanceImprovement(String preheatingType, double improvementPercentage) {
        LoggingUtil.debug(logger, "记录预热性能提升 - 类型: {}, 提升: {}%", preheatingType, improvementPercentage);

        // 记录性能提升指标
        Gauge.builder("honyrun.preheating.performance.improvement", () -> improvementPercentage)
                .description("预热性能提升百分比")
                .tag("type", preheatingType)
                .register(meterRegistry);
    }

    /**
     * 记录预热数据量
     *
     * @param preheatingType 预热类型
     * @param dataVolume     数据量
     */
    public void recordPreheatingDataVolume(String preheatingType, long dataVolume) {
        LoggingUtil.debug(logger, "记录预热数据量 - 类型: {}, 数据量: {}", preheatingType, dataVolume);

        // 记录数据量指标
        Gauge.builder("honyrun.preheating.data.volume", () -> (double) dataVolume)
                .description("预热数据量")
                .tag("type", preheatingType)
                .register(meterRegistry);
    }

    /**
     * 获取预热统计信息
     *
     * @return Mono<Map<String, Object>> 预热统计信息
     */
    public Mono<Map<String, Object>> getPreheatingStatistics() {
        return Mono.fromCallable(() -> {
            Map<String, Object> stats = new ConcurrentHashMap<>();

            stats.put("totalExecutions", totalPreheatingExecutions.get());
            stats.put("successfulExecutions", successfulPreheatingExecutions.get());
            stats.put("failedExecutions", failedPreheatingExecutions.get());
            stats.put("successRate", calculateSuccessRate());
            stats.put("effectivenessScore", calculateEffectivenessScore());
            stats.put("activePreheatingTasks", getActivePreheatingTasks());
            stats.put("preheatingStats", preheatingStatsMap);
            stats.put("lastPreheatingTimes", lastPreheatingTime);

            return stats;
        });
    }

    /**
     * 获取或创建计时器
     */
    private Timer getOrCreateTimer(String preheatingType) {
        return preheatingTimers.computeIfAbsent(preheatingType, type -> Timer.builder("preheating.execution.duration")
                .description("预热执行时间")
                .tag("type", type)
                .register(meterRegistry));
    }

    /**
     * 增加计数器
     */
    private void incrementCounter(String counterName, String preheatingType) {
        Counter counter = preheatingCounters.computeIfAbsent(
                counterName + "." + preheatingType,
                name -> Counter.builder(counterName)
                        .description("预热计数器")
                        .tag("type", preheatingType)
                        .register(meterRegistry));
        counter.increment();
    }

    /**
     * 更新预热统计
     */
    private void updatePreheatingStats(String preheatingType, Duration duration, boolean success) {
        preheatingStatsMap.compute(preheatingType, (key, stats) -> {
            if (stats == null) {
                stats = new PreheatingStats();
            }
            stats.addExecution(duration, success);
            return stats;
        });
    }

    /**
     * 计算成功率
     */
    private double calculateSuccessRate() {
        long total = totalPreheatingExecutions.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) successfulPreheatingExecutions.get() / total;
    }

    /**
     * 计算效果评分
     */
    private double calculateEffectivenessScore() {
        if (preheatingEffectiveness.isEmpty()) {
            return 0.0;
        }
        return preheatingEffectiveness.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    /**
     * 获取活跃预热任务数
     */
    private double getActivePreheatingTasks() {
        return preheatingStatsMap.size();
    }

    /**
     * 预热统计数据内部类
     */
    public static class PreheatingStats {
        private long totalExecutions = 0;
        private long successfulExecutions = 0;
        private long totalDurationMs = 0;
        private long minDurationMs = Long.MAX_VALUE;
        private long maxDurationMs = 0;

        public void addExecution(Duration duration, boolean success) {
            totalExecutions++;
            if (success) {
                successfulExecutions++;
            }

            long durationMs = duration.toMillis();
            totalDurationMs += durationMs;
            minDurationMs = Math.min(minDurationMs, durationMs);
            maxDurationMs = Math.max(maxDurationMs, durationMs);
        }

        // Getters
        public long getTotalExecutions() {
            return totalExecutions;
        }

        public long getSuccessfulExecutions() {
            return successfulExecutions;
        }

        public double getSuccessRate() {
            return totalExecutions == 0 ? 0.0 : (double) successfulExecutions / totalExecutions;
        }

        public double getAverageDurationMs() {
            return totalExecutions == 0 ? 0.0 : (double) totalDurationMs / totalExecutions;
        }

        public long getMinDurationMs() {
            return minDurationMs == Long.MAX_VALUE ? 0 : minDurationMs;
        }

        public long getMaxDurationMs() {
            return maxDurationMs;
        }
    }
}
