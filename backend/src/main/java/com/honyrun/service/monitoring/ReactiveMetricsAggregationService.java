package com.honyrun.service.monitoring;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式监控指标聚合服务接口
 * 
 * 提供监控指标的聚合、统计和分析功能：
 * - 实时指标聚合：按时间窗口聚合监控数据
 * - 统计分析：计算平均值、最大值、最小值、百分位数等
 * - 趋势分析：分析指标变化趋势和异常检测
 * - 报告生成：生成监控报告和告警信息
 * 
 * 聚合策略：
 * - 时间窗口聚合：按固定时间窗口聚合数据
 * - 滑动窗口聚合：使用滑动窗口进行实时聚合
 * - 分层聚合：支持多级聚合（秒级、分钟级、小时级）
 * - 自定义聚合：支持自定义聚合规则和函数
 * 
 * 性能优化：
 * - 异步处理：所有聚合操作异步执行
 * - 内存管理：自动清理过期数据
 * - 批量处理：批量聚合提升性能
 * - 缓存机制：缓存常用聚合结果
 * 
 * @author Mr.Rey
 * @created 2025-07-01 21:03:21
 * @modified 2025-07-01 21:03:21
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveMetricsAggregationService {

    // ==================== 指标收集方法 ====================

    /**
     * 收集单个监控指标
     * 
     * @param metricName 指标名称
     * @param value 指标值
     * @param timestamp 时间戳
     * @param tags 标签信息
     * @return 收集结果的Mono
     */
    Mono<Boolean> collectMetric(String metricName, double value, LocalDateTime timestamp, Map<String, String> tags);

    /**
     * 批量收集监控指标
     * 
     * @param metrics 指标数据列表
     * @return 收集结果的Mono
     */
    Mono<Boolean> collectMetrics(List<MetricData> metrics);

    /**
     * 收集性能指标
     * 
     * @param performanceData 性能数据
     * @return 收集结果的Mono
     */
    Mono<Boolean> collectPerformanceMetrics(Map<String, Object> performanceData);

    /**
     * 收集系统指标
     * 
     * @param systemData 系统数据
     * @return 收集结果的Mono
     */
    Mono<Boolean> collectSystemMetrics(Map<String, Object> systemData);

    // ==================== 指标聚合方法 ====================

    /**
     * 按时间窗口聚合指标
     * 
     * @param metricName 指标名称
     * @param windowSize 窗口大小
     * @param aggregationType 聚合类型（AVG, MAX, MIN, SUM, COUNT）
     * @return 聚合结果的Flux
     */
    Flux<AggregatedMetric> aggregateByTimeWindow(String metricName, Duration windowSize, String aggregationType);

    /**
     * 滑动窗口聚合
     * 
     * @param metricName 指标名称
     * @param windowSize 窗口大小
     * @param slideInterval 滑动间隔
     * @param aggregationType 聚合类型
     * @return 聚合结果的Flux
     */
    Flux<AggregatedMetric> aggregateBySlideWindow(String metricName, Duration windowSize, 
                                                  Duration slideInterval, String aggregationType);

    /**
     * 多指标聚合
     * 
     * @param metricNames 指标名称列表
     * @param windowSize 窗口大小
     * @param aggregationType 聚合类型
     * @return 聚合结果的Flux
     */
    Flux<AggregatedMetric> aggregateMultipleMetrics(List<String> metricNames, Duration windowSize, String aggregationType);

    /**
     * 自定义聚合函数
     * 
     * @param metricName 指标名称
     * @param windowSize 窗口大小
     * @param aggregationFunction 自定义聚合函数
     * @return 聚合结果的Flux
     */
    Flux<AggregatedMetric> aggregateWithCustomFunction(String metricName, Duration windowSize, 
                                                       AggregationFunction aggregationFunction);

    // ==================== 统计分析方法 ====================

    /**
     * 计算指标统计信息
     * 
     * @param metricName 指标名称
     * @param timeRange 时间范围
     * @return 统计信息的Mono
     */
    Mono<MetricStatistics> calculateStatistics(String metricName, Duration timeRange);

    /**
     * 计算百分位数
     * 
     * @param metricName 指标名称
     * @param percentiles 百分位数列表（如：50, 90, 95, 99）
     * @param timeRange 时间范围
     * @return 百分位数结果的Mono
     */
    Mono<Map<Double, Double>> calculatePercentiles(String metricName, List<Double> percentiles, Duration timeRange);

    /**
     * 趋势分析
     * 
     * @param metricName 指标名称
     * @param timeRange 时间范围
     * @return 趋势分析结果的Mono
     */
    Mono<TrendAnalysis> analyzeTrend(String metricName, Duration timeRange);

    /**
     * 异常检测
     * 
     * @param metricName 指标名称
     * @param threshold 异常阈值
     * @param timeRange 时间范围
     * @return 异常检测结果的Flux
     */
    Flux<AnomalyDetection> detectAnomalies(String metricName, double threshold, Duration timeRange);

    // ==================== 报告生成方法 ====================

    /**
     * 生成监控报告
     * 
     * @param reportType 报告类型（SUMMARY, DETAILED, PERFORMANCE）
     * @param timeRange 时间范围
     * @return 监控报告的Mono
     */
    Mono<MonitoringReport> generateReport(String reportType, Duration timeRange);

    /**
     * 生成性能报告
     * 
     * @param timeRange 时间范围
     * @return 性能报告的Mono
     */
    Mono<PerformanceReport> generatePerformanceReport(Duration timeRange);

    /**
     * 生成告警报告
     * 
     * @param severity 严重程度
     * @param timeRange 时间范围
     * @return 告警报告的Mono
     */
    Mono<AlertReport> generateAlertReport(String severity, Duration timeRange);

    /**
     * 导出监控数据
     * 
     * @param metricNames 指标名称列表
     * @param timeRange 时间范围
     * @param format 导出格式（JSON, CSV, XML）
     * @return 导出数据的Mono
     */
    Mono<String> exportMetrics(List<String> metricNames, Duration timeRange, String format);

    // ==================== 数据管理方法 ====================

    /**
     * 清理过期数据
     * 
     * @param retentionPeriod 保留期间
     * @return 清理结果的Mono
     */
    Mono<Long> cleanupExpiredData(Duration retentionPeriod);

    /**
     * 获取指标列表
     * 
     * @return 指标名称列表的Flux
     */
    Flux<String> getAvailableMetrics();

    /**
     * 获取指标元数据
     * 
     * @param metricName 指标名称
     * @return 指标元数据的Mono
     */
    Mono<MetricMetadata> getMetricMetadata(String metricName);

    /**
     * 重置聚合状态
     * 
     * @return 重置结果的Mono
     */
    Mono<Boolean> resetAggregationState();

    // ==================== 数据传输对象 ====================

    /**
     * 指标数据
     */
    class MetricData {
        private String name;
        private double value;
        private LocalDateTime timestamp;
        private Map<String, String> tags;

        public MetricData(String name, double value, LocalDateTime timestamp, Map<String, String> tags) {
            this.name = name;
            this.value = value;
            this.timestamp = timestamp;
            this.tags = tags;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public Map<String, String> getTags() { return tags; }
        public void setTags(Map<String, String> tags) { this.tags = tags; }
    }

    /**
     * 聚合指标
     */
    class AggregatedMetric {
        private String metricName;
        private double value;
        private String aggregationType;
        private LocalDateTime windowStart;
        private LocalDateTime windowEnd;
        private long count;

        public AggregatedMetric(String metricName, double value, String aggregationType, 
                               LocalDateTime windowStart, LocalDateTime windowEnd, long count) {
            this.metricName = metricName;
            this.value = value;
            this.aggregationType = aggregationType;
            this.windowStart = windowStart;
            this.windowEnd = windowEnd;
            this.count = count;
        }

        // Getters and Setters
        public String getMetricName() { return metricName; }
        public void setMetricName(String metricName) { this.metricName = metricName; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        public String getAggregationType() { return aggregationType; }
        public void setAggregationType(String aggregationType) { this.aggregationType = aggregationType; }
        public LocalDateTime getWindowStart() { return windowStart; }
        public void setWindowStart(LocalDateTime windowStart) { this.windowStart = windowStart; }
        public LocalDateTime getWindowEnd() { return windowEnd; }
        public void setWindowEnd(LocalDateTime windowEnd) { this.windowEnd = windowEnd; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    /**
     * 指标统计信息
     */
    class MetricStatistics {
        private String metricName;
        private double average;
        private double maximum;
        private double minimum;
        private double standardDeviation;
        private long count;
        private double sum;

        public MetricStatistics(String metricName, double average, double maximum, double minimum, 
                               double standardDeviation, long count, double sum) {
            this.metricName = metricName;
            this.average = average;
            this.maximum = maximum;
            this.minimum = minimum;
            this.standardDeviation = standardDeviation;
            this.count = count;
            this.sum = sum;
        }

        // Getters and Setters
        public String getMetricName() { return metricName; }
        public void setMetricName(String metricName) { this.metricName = metricName; }
        public double getAverage() { return average; }
        public void setAverage(double average) { this.average = average; }
        public double getMaximum() { return maximum; }
        public void setMaximum(double maximum) { this.maximum = maximum; }
        public double getMinimum() { return minimum; }
        public void setMinimum(double minimum) { this.minimum = minimum; }
        public double getStandardDeviation() { return standardDeviation; }
        public void setStandardDeviation(double standardDeviation) { this.standardDeviation = standardDeviation; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
        public double getSum() { return sum; }
        public void setSum(double sum) { this.sum = sum; }
    }

    /**
     * 趋势分析结果
     */
    class TrendAnalysis {
        private String metricName;
        private String trend; // INCREASING, DECREASING, STABLE
        private double slope;
        private double correlation;
        private String confidence; // HIGH, MEDIUM, LOW

        public TrendAnalysis(String metricName, String trend, double slope, double correlation, String confidence) {
            this.metricName = metricName;
            this.trend = trend;
            this.slope = slope;
            this.correlation = correlation;
            this.confidence = confidence;
        }

        // Getters and Setters
        public String getMetricName() { return metricName; }
        public void setMetricName(String metricName) { this.metricName = metricName; }
        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
        public double getSlope() { return slope; }
        public void setSlope(double slope) { this.slope = slope; }
        public double getCorrelation() { return correlation; }
        public void setCorrelation(double correlation) { this.correlation = correlation; }
        public String getConfidence() { return confidence; }
        public void setConfidence(String confidence) { this.confidence = confidence; }
    }

    /**
     * 异常检测结果
     */
    class AnomalyDetection {
        private String metricName;
        private double value;
        private LocalDateTime timestamp;
        private String anomalyType; // SPIKE, DROP, OUTLIER
        private double severity;
        private String description;

        public AnomalyDetection(String metricName, double value, LocalDateTime timestamp, 
                               String anomalyType, double severity, String description) {
            this.metricName = metricName;
            this.value = value;
            this.timestamp = timestamp;
            this.anomalyType = anomalyType;
            this.severity = severity;
            this.description = description;
        }

        // Getters and Setters
        public String getMetricName() { return metricName; }
        public void setMetricName(String metricName) { this.metricName = metricName; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getAnomalyType() { return anomalyType; }
        public void setAnomalyType(String anomalyType) { this.anomalyType = anomalyType; }
        public double getSeverity() { return severity; }
        public void setSeverity(double severity) { this.severity = severity; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 监控报告
     */
    class MonitoringReport {
        private String reportType;
        private LocalDateTime generatedAt;
        private Duration timeRange;
        private Map<String, Object> summary;
        private List<String> recommendations;

        public MonitoringReport(String reportType, LocalDateTime generatedAt, Duration timeRange, 
                               Map<String, Object> summary, List<String> recommendations) {
            this.reportType = reportType;
            this.generatedAt = generatedAt;
            this.timeRange = timeRange;
            this.summary = summary;
            this.recommendations = recommendations;
        }

        // Getters and Setters
        public String getReportType() { return reportType; }
        public void setReportType(String reportType) { this.reportType = reportType; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public Duration getTimeRange() { return timeRange; }
        public void setTimeRange(Duration timeRange) { this.timeRange = timeRange; }
        public Map<String, Object> getSummary() { return summary; }
        public void setSummary(Map<String, Object> summary) { this.summary = summary; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }

    /**
     * 性能报告
     */
    class PerformanceReport extends MonitoringReport {
        private Map<String, Double> performanceMetrics;
        private List<String> bottlenecks;

        public PerformanceReport(String reportType, LocalDateTime generatedAt, Duration timeRange, 
                                Map<String, Object> summary, List<String> recommendations,
                                Map<String, Double> performanceMetrics, List<String> bottlenecks) {
            super(reportType, generatedAt, timeRange, summary, recommendations);
            this.performanceMetrics = performanceMetrics;
            this.bottlenecks = bottlenecks;
        }

        // Getters and Setters
        public Map<String, Double> getPerformanceMetrics() { return performanceMetrics; }
        public void setPerformanceMetrics(Map<String, Double> performanceMetrics) { this.performanceMetrics = performanceMetrics; }
        public List<String> getBottlenecks() { return bottlenecks; }
        public void setBottlenecks(List<String> bottlenecks) { this.bottlenecks = bottlenecks; }
    }

    /**
     * 告警报告
     */
    class AlertReport extends MonitoringReport {
        private List<AnomalyDetection> alerts;
        private String severity;

        public AlertReport(String reportType, LocalDateTime generatedAt, Duration timeRange, 
                          Map<String, Object> summary, List<String> recommendations,
                          List<AnomalyDetection> alerts, String severity) {
            super(reportType, generatedAt, timeRange, summary, recommendations);
            this.alerts = alerts;
            this.severity = severity;
        }

        // Getters and Setters
        public List<AnomalyDetection> getAlerts() { return alerts; }
        public void setAlerts(List<AnomalyDetection> alerts) { this.alerts = alerts; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }

    /**
     * 指标元数据
     */
    class MetricMetadata {
        private String name;
        private String description;
        private String unit;
        private String type; // COUNTER, GAUGE, HISTOGRAM, TIMER
        private Map<String, String> tags;

        public MetricMetadata(String name, String description, String unit, String type, Map<String, String> tags) {
            this.name = name;
            this.description = description;
            this.unit = unit;
            this.type = type;
            this.tags = tags;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Map<String, String> getTags() { return tags; }
        public void setTags(Map<String, String> tags) { this.tags = tags; }
    }

    /**
     * 自定义聚合函数接口
     */
    @FunctionalInterface
    interface AggregationFunction {
        double aggregate(List<Double> values);
    }
}


