package com.honyrun.service.monitoring;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 响应式性能监控服务接口
 * 
 * 提供系统性能监控功能：
 * - 实时性能指标收集
 * - 性能数据分析和报告
 * - 性能阈值监控和告警
 * - 历史性能数据查询
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01 15:30:00
 * @modified 2025-07-01 15:30:00
 * @version 1.0.0
 */
public interface ReactivePerformanceMonitoringService {

    // ==================== 实时性能指标 ====================

    /**
     * 获取当前系统性能指标
     * 
     * @return 系统性能指标的Mono
     */
    Mono<SystemPerformanceMetrics> getCurrentSystemMetrics();

    /**
     * 获取应用性能指标
     * 
     * @return 应用性能指标的Mono
     */
    Mono<ApplicationPerformanceMetrics> getCurrentApplicationMetrics();

    /**
     * 获取数据库性能指标
     * 
     * @return 数据库性能指标的Mono
     */
    Mono<DatabasePerformanceMetrics> getCurrentDatabaseMetrics();

    /**
     * 获取缓存性能指标
     * 
     * @return 缓存性能指标的Mono
     */
    Mono<CachePerformanceMetrics> getCurrentCacheMetrics();

    // ==================== 性能数据收集 ====================

    /**
     * 记录方法执行性能
     * 
     * @param methodName 方法名
     * @param executionTime 执行时间（毫秒）
     * @param success 是否成功
     * @return 记录结果的Mono
     */
    Mono<Void> recordMethodPerformance(String methodName, long executionTime, boolean success);

    /**
     * 记录API请求性能
     * 
     * @param endpoint API端点
     * @param httpMethod HTTP方法
     * @param responseTime 响应时间（毫秒）
     * @param statusCode 状态码
     * @return 记录结果的Mono
     */
    Mono<Void> recordApiPerformance(String endpoint, String httpMethod, long responseTime, int statusCode);

    /**
     * 记录数据库查询性能
     * 
     * @param queryType 查询类型
     * @param executionTime 执行时间（毫秒）
     * @param recordCount 记录数量
     * @return 记录结果的Mono
     */
    Mono<Void> recordDatabaseQueryPerformance(String queryType, long executionTime, long recordCount);

    // ==================== 性能分析和报告 ====================

    /**
     * 获取性能趋势分析
     * 
     * @param metricType 指标类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 性能趋势数据的Flux
     */
    Flux<PerformanceTrendData> getPerformanceTrend(String metricType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取性能统计报告
     * 
     * @param reportType 报告类型
     * @param period 统计周期
     * @return 性能报告的Mono
     */
    Mono<PerformanceReport> getPerformanceReport(String reportType, String period);

    /**
     * 获取慢查询分析
     * 
     * @param threshold 阈值（毫秒）
     * @param limit 限制数量
     * @return 慢查询数据的Flux
     */
    Flux<SlowQueryData> getSlowQueries(long threshold, int limit);

    // ==================== 性能阈值监控 ====================

    /**
     * 设置性能阈值
     * 
     * @param metricName 指标名称
     * @param threshold 阈值
     * @param alertEnabled 是否启用告警
     * @return 设置结果的Mono
     */
    Mono<Boolean> setPerformanceThreshold(String metricName, double threshold, boolean alertEnabled);

    /**
     * 检查性能阈值
     * 
     * @return 阈值检查结果的Flux
     */
    Flux<ThresholdCheckResult> checkPerformanceThresholds();

    /**
     * 获取性能告警
     * 
     * @param severity 严重程度
     * @param limit 限制数量
     * @return 性能告警的Flux
     */
    Flux<PerformanceAlert> getPerformanceAlerts(String severity, int limit);

    // ==================== 数据传输对象 ====================

    /**
     * 系统性能指标
     */
    class SystemPerformanceMetrics {
        private final double cpuUsage;
        private final double memoryUsage;
        private final double diskUsage;
        private final double networkUsage;
        private final LocalDateTime timestamp;

        public SystemPerformanceMetrics(double cpuUsage, double memoryUsage, 
                                      double diskUsage, double networkUsage) {
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.diskUsage = diskUsage;
            this.networkUsage = networkUsage;
            this.timestamp = LocalDateTime.now();
        }

        public double getCpuUsage() { return cpuUsage; }
        public double getMemoryUsage() { return memoryUsage; }
        public double getDiskUsage() { return diskUsage; }
        public double getNetworkUsage() { return networkUsage; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * 应用性能指标
     */
    class ApplicationPerformanceMetrics {
        private final long activeThreads;
        private final long totalRequests;
        private final double averageResponseTime;
        private final double errorRate;
        private final LocalDateTime timestamp;

        public ApplicationPerformanceMetrics(long activeThreads, long totalRequests,
                                           double averageResponseTime, double errorRate) {
            this.activeThreads = activeThreads;
            this.totalRequests = totalRequests;
            this.averageResponseTime = averageResponseTime;
            this.errorRate = errorRate;
            this.timestamp = LocalDateTime.now();
        }

        public long getActiveThreads() { return activeThreads; }
        public long getTotalRequests() { return totalRequests; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public double getErrorRate() { return errorRate; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * 数据库性能指标
     */
    class DatabasePerformanceMetrics {
        private final int activeConnections;
        private final int idleConnections;
        private final double averageQueryTime;
        private final long totalQueries;
        private final LocalDateTime timestamp;

        public DatabasePerformanceMetrics(int activeConnections, int idleConnections,
                                        double averageQueryTime, long totalQueries) {
            this.activeConnections = activeConnections;
            this.idleConnections = idleConnections;
            this.averageQueryTime = averageQueryTime;
            this.totalQueries = totalQueries;
            this.timestamp = LocalDateTime.now();
        }

        public int getActiveConnections() { return activeConnections; }
        public int getIdleConnections() { return idleConnections; }
        public double getAverageQueryTime() { return averageQueryTime; }
        public long getTotalQueries() { return totalQueries; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * 缓存性能指标
     */
    class CachePerformanceMetrics {
        private final long hitCount;
        private final long missCount;
        private final double hitRate;
        private final long evictionCount;
        private final LocalDateTime timestamp;

        public CachePerformanceMetrics(long hitCount, long missCount, long evictionCount) {
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.hitRate = (hitCount + missCount) > 0 ? 
                (double) hitCount / (hitCount + missCount) : 0.0;
            this.evictionCount = evictionCount;
            this.timestamp = LocalDateTime.now();
        }

        public long getHitCount() { return hitCount; }
        public long getMissCount() { return missCount; }
        public double getHitRate() { return hitRate; }
        public long getEvictionCount() { return evictionCount; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * 性能趋势数据
     */
    class PerformanceTrendData {
        private final String metricName;
        private final double value;
        private final LocalDateTime timestamp;

        public PerformanceTrendData(String metricName, double value, LocalDateTime timestamp) {
            this.metricName = metricName;
            this.value = value;
            this.timestamp = timestamp;
        }

        public String getMetricName() { return metricName; }
        public double getValue() { return value; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * 性能报告
     */
    class PerformanceReport {
        private final String reportType;
        private final String period;
        private final Map<String, Object> metrics;
        private final LocalDateTime generatedAt;

        public PerformanceReport(String reportType, String period, Map<String, Object> metrics) {
            this.reportType = reportType;
            this.period = period;
            this.metrics = metrics;
            this.generatedAt = LocalDateTime.now();
        }

        public String getReportType() { return reportType; }
        public String getPeriod() { return period; }
        public Map<String, Object> getMetrics() { return metrics; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
    }

    /**
     * 慢查询数据
     */
    class SlowQueryData {
        private final String queryType;
        private final long executionTime;
        private final String queryDetails;
        private final LocalDateTime timestamp;

        public SlowQueryData(String queryType, long executionTime, String queryDetails, LocalDateTime timestamp) {
            this.queryType = queryType;
            this.executionTime = executionTime;
            this.queryDetails = queryDetails;
            this.timestamp = timestamp;
        }

        public String getQueryType() { return queryType; }
        public long getExecutionTime() { return executionTime; }
        public String getQueryDetails() { return queryDetails; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * 阈值检查结果
     */
    class ThresholdCheckResult {
        private final String metricName;
        private final double currentValue;
        private final double threshold;
        private final boolean exceeded;
        private final LocalDateTime checkTime;

        public ThresholdCheckResult(String metricName, double currentValue, double threshold, boolean exceeded) {
            this.metricName = metricName;
            this.currentValue = currentValue;
            this.threshold = threshold;
            this.exceeded = exceeded;
            this.checkTime = LocalDateTime.now();
        }

        public String getMetricName() { return metricName; }
        public double getCurrentValue() { return currentValue; }
        public double getThreshold() { return threshold; }
        public boolean isExceeded() { return exceeded; }
        public LocalDateTime getCheckTime() { return checkTime; }
    }

    /**
     * 性能告警
     */
    class PerformanceAlert {
        private final String alertId;
        private final String metricName;
        private final String severity;
        private final String message;
        private final LocalDateTime alertTime;

        public PerformanceAlert(String alertId, String metricName, String severity, String message) {
            this.alertId = alertId;
            this.metricName = metricName;
            this.severity = severity;
            this.message = message;
            this.alertTime = LocalDateTime.now();
        }

        public String getAlertId() { return alertId; }
        public String getMetricName() { return metricName; }
        public String getSeverity() { return severity; }
        public String getMessage() { return message; }
        public LocalDateTime getAlertTime() { return alertTime; }
    }
}

