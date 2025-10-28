package com.honyrun.service.monitoring.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import com.honyrun.config.monitoring.UnifiedMonitoringConfig;
import com.honyrun.service.monitoring.ReactiveMetricsAggregationService;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 响应式监控指标聚合服务实现类
 *
 * 核心功能：
 * - 实时指标收集：收集各类监控指标数据
 * - 时间窗口聚合：按时间窗口聚合监控数据
 * - 统计分析：计算统计指标和趋势分析
 * - 异常检测：检测指标异常和告警
 * - 报告生成：生成各类监控报告
 *
 * 技术特性：
 * - 响应式编程：基于Reactor实现异步处理
 * - Redis存储：使用Redis存储聚合数据
 * - 内存缓存：缓存热点数据提升性能
 * - 批量处理：批量操作提升吞吐量
 * - 自动清理：定期清理过期数据
 *
 * 性能优化：
 * - 异步处理：所有操作异步执行
 * - 批量聚合：批量处理提升性能
 * - 内存管理：自动管理内存使用
 * - 缓存策略：智能缓存常用数据
 *
 * @author Mr.Rey
 * @created 2025-07-01 19:05:00
 * @modified 2025-07-01 19:05:00
 * @version 1.0.0
 *          Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
public class ReactiveMetricsAggregationServiceImpl implements ReactiveMetricsAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveMetricsAggregationServiceImpl.class);

    private static final String REDIS_KEY_PREFIX = "metrics:aggregation:";
    private static final String METRICS_DATA_KEY = REDIS_KEY_PREFIX + "data:";

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final UnifiedMonitoringConfig unifiedMonitoringConfig;

    /**
     * 构造函数注入依赖
     *
     * @param reactiveRedisTemplate   响应式Redis模板
     * @param unifiedMonitoringConfig 统一监控配置
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public ReactiveMetricsAggregationServiceImpl(
            @Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
            UnifiedMonitoringConfig unifiedMonitoringConfig) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.unifiedMonitoringConfig = unifiedMonitoringConfig;
    }

    // 内存缓存
    private final Map<String, List<MetricData>> metricsCache = new ConcurrentHashMap<>();
    private final Map<String, MetricMetadata> metadataCache = new ConcurrentHashMap<>();
    private final AtomicLong totalMetricsCollected = new AtomicLong(0);
    private final AtomicLong totalAggregationsPerformed = new AtomicLong(0);

    // ==================== 指标收集方法 ====================

    @Override
    public Mono<Boolean> collectMetric(String metricName, double value, LocalDateTime timestamp,
            Map<String, String> tags) {
        return Mono.fromCallable(() -> {
            // 开始收集单个监控指标

            MetricData metricData = new MetricData(metricName, value, timestamp, tags);

            // 添加到内存缓存
            metricsCache.computeIfAbsent(metricName, k -> new ArrayList<>()).add(metricData);

            // 限制缓存大小
            List<MetricData> cachedData = metricsCache.get(metricName);
            if (cachedData.size() > unifiedMonitoringConfig.getMetricsAggregationWindow().getSeconds() * 10) {
                cachedData.removeIf(data -> data.getTimestamp().isBefore(timestamp.minusMinutes(5)));
            }

            totalMetricsCollected.incrementAndGet();
            return true;
        })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(result -> {
                    // 异步存储到Redis
                    String redisKey = METRICS_DATA_KEY + metricName + ":" + timestamp.format(TIMESTAMP_FORMATTER);
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("value", value);
                    dataMap.put("timestamp", timestamp.toString());
                    dataMap.put("tags", tags);

                    return reactiveRedisTemplate.opsForHash().putAll(redisKey, dataMap)
                            .then(reactiveRedisTemplate.expire(redisKey,
                                    unifiedMonitoringConfig.getMetricsRetentionDuration()))
                            .thenReturn(result);
                })
                .doOnSuccess(result -> LoggingUtil.info(logger, "成功收集监控指标: {}", metricName))
                .doOnError(error -> LoggingUtil.error(logger, "收集监控指标失败: {}, 错误: {}", metricName, error.getMessage()));
    }

    @Override
    public Mono<Boolean> collectMetrics(List<MetricData> metrics) {
        return Flux.fromIterable(metrics)
                .flatMap(metric -> collectMetric(metric.getName(), metric.getValue(),
                        metric.getTimestamp(), metric.getTags()))
                .all(result -> result)
                .doOnSuccess(result -> LoggingUtil.info(logger, "批量收集监控指标完成，总数: {}", metrics.size()))
                .doOnError(error -> LoggingUtil.error(logger, "批量收集监控指标失败: {}", error.getMessage()));
    }

    @Override
    public Mono<Boolean> collectPerformanceMetrics(Map<String, Object> performanceData) {
        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "开始收集性能指标，数据项数: {}", performanceData.size());

            LocalDateTime now = LocalDateTime.now();
            List<MetricData> metrics = new ArrayList<>();

            performanceData.forEach((key, value) -> {
                if (value instanceof Number) {
                    Map<String, String> tags = new HashMap<>();
                    tags.put("type", "performance");
                    tags.put("source", "system");

                    metrics.add(new MetricData("performance." + key, ((Number) value).doubleValue(), now, tags));
                }
            });

            return metrics;
        })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(this::collectMetrics);
    }

    @Override
    public Mono<Boolean> collectSystemMetrics(Map<String, Object> systemData) {
        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "开始收集系统指标，数据项数: {}", systemData.size());

            LocalDateTime now = LocalDateTime.now();
            List<MetricData> metrics = new ArrayList<>();

            systemData.forEach((key, value) -> {
                if (value instanceof Number) {
                    Map<String, String> tags = new HashMap<>();
                    tags.put("type", "system");
                    tags.put("source", "monitor");

                    metrics.add(new MetricData("system." + key, ((Number) value).doubleValue(), now, tags));
                }
            });

            return metrics;
        })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(this::collectMetrics);
    }

    // ==================== 指标聚合方法 ====================

    @Override
    public Flux<AggregatedMetric> aggregateByTimeWindow(String metricName, Duration windowSize,
            String aggregationType) {
        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "开始按时间窗口聚合指标: {}, 窗口大小: {}, 聚合类型: {}",
                    metricName, windowSize, aggregationType);

            List<MetricData> cachedData = metricsCache.get(metricName);
            if (cachedData == null || cachedData.isEmpty()) {
                return Collections.<AggregatedMetric>emptyList();
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowStart = now.minus(windowSize);

            List<MetricData> windowData = cachedData.stream()
                    .filter(data -> data.getTimestamp().isAfter(windowStart))
                    .collect(Collectors.toList());

            if (windowData.isEmpty()) {
                return Collections.<AggregatedMetric>emptyList();
            }

            double aggregatedValue = calculateAggregation(windowData, aggregationType);
            AggregatedMetric result = new AggregatedMetric(metricName, aggregatedValue, aggregationType,
                    windowStart, now, windowData.size());

            totalAggregationsPerformed.incrementAndGet();
            return Collections.singletonList(result);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .doOnComplete(() -> LoggingUtil.info(logger, "时间窗口聚合完成: {}", metricName))
                .doOnError(error -> LoggingUtil.error(logger, "时间窗口聚合失败: {}, 错误: {}", metricName, error.getMessage()));
    }

    @Override
    public Flux<AggregatedMetric> aggregateBySlideWindow(String metricName, Duration windowSize,
            Duration slideInterval, String aggregationType) {
        return Flux.interval(slideInterval)
                .flatMap(tick -> aggregateByTimeWindow(metricName, windowSize, aggregationType))
                .doOnSubscribe(subscription -> LoggingUtil.info(logger, "开始滑动窗口聚合: {}, 窗口: {}, 间隔: {}",
                        metricName, windowSize, slideInterval))
                .doOnError(error -> LoggingUtil.error(logger, "滑动窗口聚合失败: {}, 错误: {}", metricName, error.getMessage()));
    }

    @Override
    public Flux<AggregatedMetric> aggregateMultipleMetrics(List<String> metricNames, Duration windowSize,
            String aggregationType) {
        return Flux.fromIterable(metricNames)
                .flatMap(metricName -> aggregateByTimeWindow(metricName, windowSize, aggregationType))
                .doOnComplete(() -> LoggingUtil.info(logger, "多指标聚合完成，指标数: {}", metricNames.size()))
                .doOnError(error -> LoggingUtil.error(logger, "多指标聚合失败: {}", error.getMessage()));
    }

    @Override
    public Flux<AggregatedMetric> aggregateWithCustomFunction(String metricName, Duration windowSize,
            AggregationFunction aggregationFunction) {
        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "开始自定义函数聚合指标: {}, 窗口大小: {}", metricName, windowSize);

            List<MetricData> cachedData = metricsCache.get(metricName);
            if (cachedData == null || cachedData.isEmpty()) {
                return Collections.<AggregatedMetric>emptyList();
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowStart = now.minus(windowSize);

            List<Double> values = cachedData.stream()
                    .filter(data -> data.getTimestamp().isAfter(windowStart))
                    .map(MetricData::getValue)
                    .collect(Collectors.toList());

            if (values.isEmpty()) {
                return Collections.<AggregatedMetric>emptyList();
            }

            double aggregatedValue = aggregationFunction.aggregate(values);
            AggregatedMetric result = new AggregatedMetric(metricName, aggregatedValue, "CUSTOM",
                    windowStart, now, values.size());

            return Collections.singletonList(result);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .doOnComplete(() -> LoggingUtil.info(logger, "自定义函数聚合完成: {}", metricName))
                .doOnError(error -> LoggingUtil.error(logger, "自定义函数聚合失败: {}, 错误: {}", metricName, error.getMessage()));
    }

    // ==================== 统计分析方法 ====================

    @Override
    public Mono<MetricStatistics> calculateStatistics(String metricName, Duration timeRange) {
        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "开始计算指标统计信息: {}, 时间范围: {}", metricName, timeRange);

            List<MetricData> cachedData = metricsCache.get(metricName);
            if (cachedData == null || cachedData.isEmpty()) {
                return new MetricStatistics(metricName, 0.0, 0.0, 0.0, 0.0, 0, 0.0);
            }

            LocalDateTime cutoff = LocalDateTime.now().minus(timeRange);
            List<Double> values = cachedData.stream()
                    .filter(data -> data.getTimestamp().isAfter(cutoff))
                    .map(MetricData::getValue)
                    .collect(Collectors.toList());

            if (values.isEmpty()) {
                return new MetricStatistics(metricName, 0.0, 0.0, 0.0, 0.0, 0, 0.0);
            }

            double sum = values.stream().mapToDouble(Double::doubleValue).sum();
            double average = sum / values.size();
            double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

            double variance = values.stream()
                    .mapToDouble(value -> Math.pow(value - average, 2))
                    .average()
                    .orElse(0.0);
            double standardDeviation = Math.sqrt(variance);

            return new MetricStatistics(metricName, average, max, min, standardDeviation, values.size(), sum);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(stats -> LoggingUtil.info(logger, "统计信息计算完成: {}, 平均值: {}", metricName, stats.getAverage()))
                .doOnError(error -> LoggingUtil.error(logger, "统计信息计算失败: {}, 错误: {}", metricName, error.getMessage()));
    }

    @Override
    public Mono<Map<Double, Double>> calculatePercentiles(String metricName, List<Double> percentiles,
            Duration timeRange) {
        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "开始计算百分位数: {}, 百分位: {}, 时间范围: {}", metricName, percentiles, timeRange);

            List<MetricData> cachedData = metricsCache.get(metricName);
            if (cachedData == null || cachedData.isEmpty()) {
                return percentiles.stream().collect(Collectors.toMap(p -> p, p -> 0.0));
            }

            LocalDateTime cutoff = LocalDateTime.now().minus(timeRange);
            List<Double> values = cachedData.stream()
                    .filter(data -> data.getTimestamp().isAfter(cutoff))
                    .map(MetricData::getValue)
                    .sorted()
                    .collect(Collectors.toList());

            if (values.isEmpty()) {
                return percentiles.stream().collect(Collectors.toMap(p -> p, p -> 0.0));
            }

            Map<Double, Double> result = new HashMap<>();
            for (Double percentile : percentiles) {
                int index = (int) Math.ceil(percentile / 100.0 * values.size()) - 1;
                index = Math.max(0, Math.min(index, values.size() - 1));
                result.put(percentile, values.get(index));
            }

            return result;
        })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> LoggingUtil.info(logger, "百分位数计算完成: {}", metricName))
                .doOnError(error -> LoggingUtil.error(logger, "百分位数计算失败: {}, 错误: {}", metricName, error.getMessage()));
    }

    @Override
    public Mono<TrendAnalysis> analyzeTrend(String metricName, Duration timeRange) {
        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "开始趋势分析: {}, 时间范围: {}", metricName, timeRange);

            List<MetricData> cachedData = metricsCache.get(metricName);
            if (cachedData == null || cachedData.size() < 2) {
                return new TrendAnalysis(metricName, "STABLE", 0.0, 0.0, "LOW");
            }

            LocalDateTime cutoff = LocalDateTime.now().minus(timeRange);
            List<MetricData> timeRangeData = cachedData.stream()
                    .filter(data -> data.getTimestamp().isAfter(cutoff))
                    .sorted(Comparator.comparing(MetricData::getTimestamp))
                    .collect(Collectors.toList());

            if (timeRangeData.size() < 2) {
                return new TrendAnalysis(metricName, "STABLE", 0.0, 0.0, "LOW");
            }

            // 简单线性回归计算趋势
            double[] x = new double[timeRangeData.size()];
            double[] y = new double[timeRangeData.size()];

            for (int i = 0; i < timeRangeData.size(); i++) {
                x[i] = i;
                y[i] = timeRangeData.get(i).getValue();
            }

            double slope = calculateSlope(x, y);
            double correlation = calculateCorrelation(x, y);

            String trend = slope > 0.1 ? "INCREASING" : (slope < -0.1 ? "DECREASING" : "STABLE");
            String confidence = Math.abs(correlation) > 0.8 ? "HIGH" : (Math.abs(correlation) > 0.5 ? "MEDIUM" : "LOW");

            return new TrendAnalysis(metricName, trend, slope, correlation, confidence);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(
                        analysis -> LoggingUtil.info(logger, "趋势分析完成: {}, 趋势: {}", metricName, analysis.getTrend()))
                .doOnError(error -> LoggingUtil.error(logger, "趋势分析失败: {}, 错误: {}", metricName, error.getMessage()));
    }

    @Override
    public Flux<AnomalyDetection> detectAnomalies(String metricName, double threshold, Duration timeRange) {
        return calculateStatistics(metricName, timeRange)
                .flatMapMany(stats -> {
                    List<MetricData> cachedData = metricsCache.get(metricName);
                    if (cachedData == null || cachedData.isEmpty()) {
                        return Flux.empty();
                    }

                    LocalDateTime cutoff = LocalDateTime.now().minus(timeRange);
                    double upperBound = stats.getAverage() + threshold * stats.getStandardDeviation();
                    double lowerBound = stats.getAverage() - threshold * stats.getStandardDeviation();

                    return Flux.fromIterable(cachedData)
                            .filter(data -> data.getTimestamp().isAfter(cutoff))
                            .filter(data -> data.getValue() > upperBound || data.getValue() < lowerBound)
                            .map(data -> {
                                String anomalyType = data.getValue() > upperBound ? "SPIKE" : "DROP";
                                double severity = Math.abs(data.getValue() - stats.getAverage())
                                        / stats.getStandardDeviation();
                                String description = String.format("指标值 %.2f 超出正常范围 [%.2f, %.2f]",
                                        data.getValue(), lowerBound, upperBound);

                                return new AnomalyDetection(metricName, data.getValue(), data.getTimestamp(),
                                        anomalyType, severity, description);
                            });
                })
                .doOnComplete(() -> LoggingUtil.info(logger, "异常检测完成: {}", metricName))
                .doOnError(error -> LoggingUtil.error(logger, "异常检测失败: {}, 错误: {}", metricName, error.getMessage()));
    }

    // ==================== 报告生成方法 ====================

    @Override
    public Mono<MonitoringReport> generateReport(String reportType, Duration timeRange) {
        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "开始生成监控报告，类型: {}, 时间范围: {}", reportType, timeRange);

            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> summary = new HashMap<>();
            List<String> recommendations = new ArrayList<>();

            // 基础统计信息
            summary.put("totalMetricsCollected", totalMetricsCollected.get());
            summary.put("totalAggregationsPerformed", totalAggregationsPerformed.get());
            summary.put("activeMetrics", metricsCache.size());
            summary.put("reportGeneratedAt", now.toString());

            // 生成建议
            if (totalMetricsCollected.get() > 10000) {
                recommendations.add("考虑增加数据清理频率以优化内存使用");
            }
            if (metricsCache.size() > 100) {
                recommendations.add("监控指标数量较多，建议分类管理");
            }

            return new MonitoringReport(reportType, now, timeRange, summary, recommendations);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(report -> LoggingUtil.info(logger, "监控报告生成完成，类型: {}", reportType))
                .doOnError(error -> LoggingUtil.error(logger, "监控报告生成失败: {}, 错误: {}", reportType, error.getMessage()));
    }

    @Override
    public Mono<PerformanceReport> generatePerformanceReport(Duration timeRange) {
        return generateReport("PERFORMANCE", timeRange)
                .cast(MonitoringReport.class)
                .map(baseReport -> {
                    Map<String, Double> performanceMetrics = new HashMap<>();
                    List<String> bottlenecks = new ArrayList<>();

                    // 收集性能指标
                    metricsCache.entrySet().stream()
                            .filter(entry -> entry.getKey().startsWith("performance."))
                            .forEach(entry -> {
                                List<MetricData> data = entry.getValue();
                                if (!data.isEmpty()) {
                                    double avgValue = data.stream()
                                            .mapToDouble(MetricData::getValue)
                                            .average()
                                            .orElse(0.0);
                                    performanceMetrics.put(entry.getKey(), avgValue);
                                }
                            });

                    // 识别性能瓶颈
                    performanceMetrics.entrySet().stream()
                            .filter(entry -> entry.getValue() > 1000) // 假设阈值
                            .forEach(entry -> bottlenecks.add("高延迟检测: " + entry.getKey() + " = " + entry.getValue()));

                    return new PerformanceReport(baseReport.getReportType(), baseReport.getGeneratedAt(),
                            baseReport.getTimeRange(), baseReport.getSummary(),
                            baseReport.getRecommendations(), performanceMetrics, bottlenecks);
                });
    }

    @Override
    public Mono<AlertReport> generateAlertReport(String severity, Duration timeRange) {
        return generateReport("ALERT", timeRange)
                .cast(MonitoringReport.class)
                .flatMap(baseReport -> {
                    // 收集所有异常
                    return Flux.fromIterable(metricsCache.keySet())
                            .flatMap(metricName -> detectAnomalies(metricName, 2.0, timeRange))
                            .filter(anomaly -> severity.equals("ALL") ||
                                    (severity.equals("HIGH") && anomaly.getSeverity() > 3.0) ||
                                    (severity.equals("MEDIUM") && anomaly.getSeverity() > 2.0))
                            .collectList()
                            .map(alerts -> new AlertReport(baseReport.getReportType(), baseReport.getGeneratedAt(),
                                    baseReport.getTimeRange(), baseReport.getSummary(),
                                    baseReport.getRecommendations(), alerts, severity));
                });
    }

    @Override
    public Mono<String> exportMetrics(List<String> metricNames, Duration timeRange, String format) {
        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "开始导出监控数据，指标: {}, 格式: {}", metricNames, format);

            LocalDateTime cutoff = LocalDateTime.now().minus(timeRange);
            StringBuilder exportData = new StringBuilder();

            if ("JSON".equalsIgnoreCase(format)) {
                exportData.append("{\n  \"metrics\": [\n");
                boolean first = true;

                for (String metricName : metricNames) {
                    List<MetricData> data = metricsCache.get(metricName);
                    if (data != null) {
                        for (MetricData metric : data) {
                            if (metric.getTimestamp().isAfter(cutoff)) {
                                if (!first)
                                    exportData.append(",\n");
                                exportData.append("    {\n")
                                        .append("      \"name\": \"").append(metric.getName()).append("\",\n")
                                        .append("      \"value\": ").append(metric.getValue()).append(",\n")
                                        .append("      \"timestamp\": \"").append(metric.getTimestamp()).append("\",\n")
                                        .append("      \"tags\": ").append(metric.getTags()).append("\n")
                                        .append("    }");
                                first = false;
                            }
                        }
                    }
                }
                exportData.append("\n  ]\n}");
            } else if ("CSV".equalsIgnoreCase(format)) {
                exportData.append("MetricName,Value,Timestamp,Tags\n");

                for (String metricName : metricNames) {
                    List<MetricData> data = metricsCache.get(metricName);
                    if (data != null) {
                        for (MetricData metric : data) {
                            if (metric.getTimestamp().isAfter(cutoff)) {
                                exportData.append(metric.getName()).append(",")
                                        .append(metric.getValue()).append(",")
                                        .append(metric.getTimestamp()).append(",")
                                        .append(metric.getTags()).append("\n");
                            }
                        }
                    }
                }
            }

            return exportData.toString();
        })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(data -> LoggingUtil.info(logger, "监控数据导出完成，格式: {}", format))
                .doOnError(error -> LoggingUtil.error(logger, "监控数据导出失败: {}, 错误: {}", format, error.getMessage()));
    }

    // ==================== 数据管理方法 ====================

    @Override
    public Mono<Long> cleanupExpiredData(Duration retentionPeriod) {
        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "开始清理过期数据，保留期间: {}", retentionPeriod);

            LocalDateTime cutoff = LocalDateTime.now().minus(retentionPeriod);
            long cleanedCount = 0;

            for (Map.Entry<String, List<MetricData>> entry : metricsCache.entrySet()) {
                List<MetricData> data = entry.getValue();
                int originalSize = data.size();
                data.removeIf(metric -> metric.getTimestamp().isBefore(cutoff));
                cleanedCount += (originalSize - data.size());
            }

            return cleanedCount;
        })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(count -> LoggingUtil.info(logger, "过期数据清理完成，清理数量: {}", count))
                .doOnError(error -> LoggingUtil.error(logger, "过期数据清理失败: {}", error.getMessage()));
    }

    @Override
    public Flux<String> getAvailableMetrics() {
        return Flux.fromIterable(metricsCache.keySet())
                .doOnComplete(() -> LoggingUtil.info(logger, "获取可用指标列表完成，总数: {}", metricsCache.size()));
    }

    @Override
    public Mono<MetricMetadata> getMetricMetadata(String metricName) {
        return Mono.fromCallable(() -> {
            MetricMetadata metadata = metadataCache.get(metricName);
            if (metadata == null) {
                // 创建默认元数据
                Map<String, String> tags = new HashMap<>();
                tags.put("source", "system");
                metadata = new MetricMetadata(metricName, "系统监控指标", "count", "GAUGE", tags);
                metadataCache.put(metricName, metadata);
            }
            return metadata;
        })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> resetAggregationState() {
        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "开始重置聚合状态");

            metricsCache.clear();
            metadataCache.clear();
            totalMetricsCollected.set(0);
            totalAggregationsPerformed.set(0);

            return true;
        })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> LoggingUtil.info(logger, "聚合状态重置完成"))
                .doOnError(error -> LoggingUtil.error(logger, "聚合状态重置失败: {}", error.getMessage()));
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 计算聚合值
     */
    private double calculateAggregation(List<MetricData> data, String aggregationType) {
        List<Double> values = data.stream().map(MetricData::getValue).collect(Collectors.toList());

        switch (aggregationType.toUpperCase()) {
            case "AVG":
            case "AVERAGE":
                return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            case "MAX":
            case "MAXIMUM":
                return values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            case "MIN":
            case "MINIMUM":
                return values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            case "SUM":
                return values.stream().mapToDouble(Double::doubleValue).sum();
            case "COUNT":
                return values.size();
            default:
                return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }
    }

    /**
     * 计算斜率
     */
    private double calculateSlope(double[] x, double[] y) {
        int n = x.length;
        double sumX = Arrays.stream(x).sum();
        double sumY = Arrays.stream(y).sum();
        double sumXY = 0.0;
        double sumXX = 0.0;

        for (int i = 0; i < n; i++) {
            sumXY += x[i] * y[i];
            sumXX += x[i] * x[i];
        }

        return (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
    }

    /**
     * 计算相关系数
     */
    private double calculateCorrelation(double[] x, double[] y) {
        int n = x.length;
        double sumX = Arrays.stream(x).sum();
        double sumY = Arrays.stream(y).sum();
        double sumXY = 0.0;
        double sumXX = 0.0;
        double sumYY = 0.0;

        for (int i = 0; i < n; i++) {
            sumXY += x[i] * y[i];
            sumXX += x[i] * x[i];
            sumYY += y[i] * y[i];
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumXX - sumX * sumX) * (n * sumYY - sumY * sumY));

        return denominator == 0 ? 0 : numerator / denominator;
    }
}
