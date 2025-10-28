package com.honyrun.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 监控服务配置属性类
 *
 * 统一管理所有监控相关的配置参数，避免硬编码。
 * 支持通过application.properties进行配置。
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-27 12:26:43
 * @modified 2025-10-27 12:26:43
 * @version 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "honyrun.monitoring")
public class MonitoringProperties {

    /**
     * 系统监控配置
     */
    private SystemConfig system = new SystemConfig();

    /**
     * 事务监控配置
     */
    private TransactionConfig transaction = new TransactionConfig();

    /**
     * 背压控制配置
     */
    private BackpressureConfig backpressure = new BackpressureConfig();

    /**
     * 动态调度配置
     */
    private SchedulingConfig scheduling = new SchedulingConfig();

    /**
     * 预热指标配置
     */
    private PreheatingConfig preheating = new PreheatingConfig();

    /**
     * 指标聚合配置
     */
    private MetricsConfig metrics = new MetricsConfig();

    /**
     * 健康检查配置
     */
    private HealthConfig health = new HealthConfig();

    /**
     * 告警服务配置
     */
    private AlertConfig alert = new AlertConfig();

    /**
     * 性能监控配置
     */
    private PerformanceConfig performance = new PerformanceConfig();

    /**
     * 可视化配置
     */
    private VisualizationConfig visualization = new VisualizationConfig();

    // Getters and Setters
    public SystemConfig getSystem() {
        return system;
    }

    public void setSystem(SystemConfig system) {
        this.system = system;
    }

    public TransactionConfig getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionConfig transaction) {
        this.transaction = transaction;
    }

    public BackpressureConfig getBackpressure() {
        return backpressure;
    }

    public void setBackpressure(BackpressureConfig backpressure) {
        this.backpressure = backpressure;
    }

    public SchedulingConfig getScheduling() {
        return scheduling;
    }

    public void setScheduling(SchedulingConfig scheduling) {
        this.scheduling = scheduling;
    }

    public PreheatingConfig getPreheating() {
        return preheating;
    }

    public void setPreheating(PreheatingConfig preheating) {
        this.preheating = preheating;
    }

    public MetricsConfig getMetrics() {
        return metrics;
    }

    public void setMetrics(MetricsConfig metrics) {
        this.metrics = metrics;
    }

    public HealthConfig getHealth() {
        return health;
    }

    public void setHealth(HealthConfig health) {
        this.health = health;
    }

    public AlertConfig getAlert() {
        return alert;
    }

    public void setAlert(AlertConfig alert) {
        this.alert = alert;
    }

    public PerformanceConfig getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceConfig performance) {
        this.performance = performance;
    }

    public VisualizationConfig getVisualization() {
        return visualization;
    }

    public void setVisualization(VisualizationConfig visualization) {
        this.visualization = visualization;
    }

    /**
     * 系统监控配置
     */
    public static class SystemConfig {
        private long fixedRate = 30000;
        private String uptimeKey = "uptime";
        private String errorKey = "error";
        private String timestampKey = "timestamp";

        // Getters and Setters
        public long getFixedRate() {
            return fixedRate;
        }

        public void setFixedRate(long fixedRate) {
            this.fixedRate = fixedRate;
        }

        public String getUptimeKey() {
            return uptimeKey;
        }

        public void setUptimeKey(String uptimeKey) {
            this.uptimeKey = uptimeKey;
        }

        public String getErrorKey() {
            return errorKey;
        }

        public void setErrorKey(String errorKey) {
            this.errorKey = errorKey;
        }

        public String getTimestampKey() {
            return timestampKey;
        }

        public void setTimestampKey(String timestampKey) {
            this.timestampKey = timestampKey;
        }
    }

    /**
     * 事务监控配置
     */
    public static class TransactionConfig {
        private String beforeCommitPhase = "BEFORE_COMMIT";
        private String successStatus = "SUCCESS";
        private String failedStatus = "FAILED";
        private String totalKey = "totalTransactions";
        private String successKey = "successTransactions";
        private String defaultValue = "0";
        private String unknownValue = "unknown";

        // Getters and Setters
        public String getBeforeCommitPhase() {
            return beforeCommitPhase;
        }

        public void setBeforeCommitPhase(String beforeCommitPhase) {
            this.beforeCommitPhase = beforeCommitPhase;
        }

        public String getSuccessStatus() {
            return successStatus;
        }

        public void setSuccessStatus(String successStatus) {
            this.successStatus = successStatus;
        }

        public String getFailedStatus() {
            return failedStatus;
        }

        public void setFailedStatus(String failedStatus) {
            this.failedStatus = failedStatus;
        }

        public String getTotalKey() {
            return totalKey;
        }

        public void setTotalKey(String totalKey) {
            this.totalKey = totalKey;
        }

        public String getSuccessKey() {
            return successKey;
        }

        public void setSuccessKey(String successKey) {
            this.successKey = successKey;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getUnknownValue() {
            return unknownValue;
        }

        public void setUnknownValue(String unknownValue) {
            this.unknownValue = unknownValue;
        }
    }

    /**
     * 背压控制配置
     */
    public static class BackpressureConfig {
        private List<String> alertTypes = List.of("BACKPRESSURE_CONTROL", "TIMEOUT", "ERROR", "RATE_LIMIT", "REJECTED",
                "BACKPRESSURE_CONFIG", "ADJUSTMENT_REQUIRED", "TIMEOUT_ADJUSTMENT", "RATE_LIMIT_CONFIG",
                "PARAMETER_ADJUSTMENT", "BACKPRESSURE_BUFFER", "OVERFLOW");
        private long elapsedMillis = 1000;

        // Getters and Setters
        public List<String> getAlertTypes() {
            return alertTypes;
        }

        public void setAlertTypes(List<String> alertTypes) {
            this.alertTypes = alertTypes;
        }

        public long getElapsedMillis() {
            return elapsedMillis;
        }

        public void setElapsedMillis(long elapsedMillis) {
            this.elapsedMillis = elapsedMillis;
        }
    }

    /**
     * 动态调度配置
     */
    public static class SchedulingConfig {
        private List<String> taskStatuses = List.of("IDLE", "DISABLED", "RUNNING", "ERROR");
        private List<String> healthKeys = List.of("task_error_rate", "thread_pool");
        private List<Integer> healthThresholds = List.of(100, 30, 15, 20, 10, 80, 60);
        private Duration cleanupDuration = Duration.ofDays(30);
        private Duration checkInterval = Duration.ofMinutes(5);
        private Duration adjustmentInterval = Duration.ofMinutes(2);

        // Getters and Setters
        public List<String> getTaskStatuses() {
            return taskStatuses;
        }

        public void setTaskStatuses(List<String> taskStatuses) {
            this.taskStatuses = taskStatuses;
        }

        public List<String> getHealthKeys() {
            return healthKeys;
        }

        public void setHealthKeys(List<String> healthKeys) {
            this.healthKeys = healthKeys;
        }

        public List<Integer> getHealthThresholds() {
            return healthThresholds;
        }

        public void setHealthThresholds(List<Integer> healthThresholds) {
            this.healthThresholds = healthThresholds;
        }

        public Duration getCleanupDuration() {
            return cleanupDuration;
        }

        public void setCleanupDuration(Duration cleanupDuration) {
            this.cleanupDuration = cleanupDuration;
        }

        public Duration getCheckInterval() {
            return checkInterval;
        }

        public void setCheckInterval(Duration checkInterval) {
            this.checkInterval = checkInterval;
        }

        public Duration getAdjustmentInterval() {
            return adjustmentInterval;
        }

        public void setAdjustmentInterval(Duration adjustmentInterval) {
            this.adjustmentInterval = adjustmentInterval;
        }
    }

    /**
     * 预热指标配置
     */
    public static class PreheatingConfig {
        private List<String> tagTypes = List.of("total", "success", "failure");
        private List<String> statsKeys = List.of("totalExecutions", "successfulExecutions", "failedExecutions",
                "successRate", "effectivenessScore", "activePreheatingTasks", "preheatingStats", "lastPreheatingTimes");

        // Getters and Setters
        public List<String> getTagTypes() {
            return tagTypes;
        }

        public void setTagTypes(List<String> tagTypes) {
            this.tagTypes = tagTypes;
        }

        public List<String> getStatsKeys() {
            return statsKeys;
        }

        public void setStatsKeys(List<String> statsKeys) {
            this.statsKeys = statsKeys;
        }
    }

    /**
     * 指标聚合配置
     */
    public static class MetricsConfig {
        private String redisPrefix = "metrics:aggregation:";
        private String dataPrefix = "metrics:aggregation:data:";
        private List<String> dataKeys = List.of("value", "timestamp", "tags");
        private List<String> tagTypes = List.of("performance", "system", "monitor");
        private List<String> trendValues = List.of("STABLE", "INCREASING", "DECREASING", "HIGH", "MEDIUM", "LOW");
        private List<String> anomalyTypes = List.of("SPIKE", "DROP");
        private List<String> summaryKeys = List.of("totalMetricsCollected", "totalAggregationsPerformed",
                "activeMetrics", "reportGeneratedAt");
        private List<String> reportTypes = List.of("PERFORMANCE", "ALERT");
        private List<String> formatTypes = List.of("JSON", "CSV");
        private List<String> metadataFields = List.of("系统监控指标", "count", "GAUGE");
        private List<String> aggregationTypes = List.of("AVG", "AVERAGE", "MAX", "MAXIMUM", "MIN", "MINIMUM", "SUM",
                "COUNT");
        private List<Double> thresholdValues = List.of(0.1, -0.1, 0.8, 0.5, 3.0, 2.0);

        // Getters and Setters
        public String getRedisPrefix() {
            return redisPrefix;
        }

        public void setRedisPrefix(String redisPrefix) {
            this.redisPrefix = redisPrefix;
        }

        public String getDataPrefix() {
            return dataPrefix;
        }

        public void setDataPrefix(String dataPrefix) {
            this.dataPrefix = dataPrefix;
        }

        public List<String> getDataKeys() {
            return dataKeys;
        }

        public void setDataKeys(List<String> dataKeys) {
            this.dataKeys = dataKeys;
        }

        public List<String> getTagTypes() {
            return tagTypes;
        }

        public void setTagTypes(List<String> tagTypes) {
            this.tagTypes = tagTypes;
        }

        public List<String> getTrendValues() {
            return trendValues;
        }

        public void setTrendValues(List<String> trendValues) {
            this.trendValues = trendValues;
        }

        public List<String> getAnomalyTypes() {
            return anomalyTypes;
        }

        public void setAnomalyTypes(List<String> anomalyTypes) {
            this.anomalyTypes = anomalyTypes;
        }

        public List<String> getSummaryKeys() {
            return summaryKeys;
        }

        public void setSummaryKeys(List<String> summaryKeys) {
            this.summaryKeys = summaryKeys;
        }

        public List<String> getReportTypes() {
            return reportTypes;
        }

        public void setReportTypes(List<String> reportTypes) {
            this.reportTypes = reportTypes;
        }

        public List<String> getFormatTypes() {
            return formatTypes;
        }

        public void setFormatTypes(List<String> formatTypes) {
            this.formatTypes = formatTypes;
        }

        public List<String> getMetadataFields() {
            return metadataFields;
        }

        public void setMetadataFields(List<String> metadataFields) {
            this.metadataFields = metadataFields;
        }

        public List<String> getAggregationTypes() {
            return aggregationTypes;
        }

        public void setAggregationTypes(List<String> aggregationTypes) {
            this.aggregationTypes = aggregationTypes;
        }

        public List<Double> getThresholdValues() {
            return thresholdValues;
        }

        public void setThresholdValues(List<Double> thresholdValues) {
            this.thresholdValues = thresholdValues;
        }
    }

    /**
     * 健康检查配置
     */
    public static class HealthConfig {
        private List<String> redisKeys = List.of("database", "redis", "application");
        private List<String> errorKeys = List.of("error", "timestamp");
        private List<Duration> timeoutDurations = List.of(Duration.ofSeconds(8), Duration.ofSeconds(10),
                Duration.ofSeconds(12));
        private long fixedRate = 60000;
        private List<String> statusValues = List.of("UP", "DOWN");
        private double cpuThreshold = 80.0;
        private double memoryThreshold = 85.0;
        private double heapThreshold = 90.0;

        // Getters and Setters
        public List<String> getRedisKeys() {
            return redisKeys;
        }

        public void setRedisKeys(List<String> redisKeys) {
            this.redisKeys = redisKeys;
        }

        public List<String> getErrorKeys() {
            return errorKeys;
        }

        public void setErrorKeys(List<String> errorKeys) {
            this.errorKeys = errorKeys;
        }

        public List<Duration> getTimeoutDurations() {
            return timeoutDurations;
        }

        public void setTimeoutDurations(List<Duration> timeoutDurations) {
            this.timeoutDurations = timeoutDurations;
        }

        public long getFixedRate() {
            return fixedRate;
        }

        public void setFixedRate(long fixedRate) {
            this.fixedRate = fixedRate;
        }

        public List<String> getStatusValues() {
            return statusValues;
        }

        public void setStatusValues(List<String> statusValues) {
            this.statusValues = statusValues;
        }

        public double getCpuThreshold() {
            return cpuThreshold;
        }

        public void setCpuThreshold(double cpuThreshold) {
            this.cpuThreshold = cpuThreshold;
        }

        public double getMemoryThreshold() {
            return memoryThreshold;
        }

        public void setMemoryThreshold(double memoryThreshold) {
            this.memoryThreshold = memoryThreshold;
        }

        public double getHeapThreshold() {
            return heapThreshold;
        }

        public void setHeapThreshold(double heapThreshold) {
            this.heapThreshold = heapThreshold;
        }
    }

    /**
     * 告警服务配置
     */
    public static class AlertConfig {
        private List<String> redisKeys = List.of("monitoring:alert:rules", "monitoring:alert:events",
                "monitoring:alert:suppressions", "monitoring:alert:statistics");
        private int maxHistorySize = 1000;
        private String manualRuleId = "MANUAL";
        private List<Integer> severityCounts = List.of(0, 0, 0, 0, 0, 0);
        private String systemResolution = "SYSTEM";
        private String notifierType = "LogNotifier";
        private double cpuThreshold = 75.0;
        private double memoryThreshold = 80.0;
        private double heapThreshold = 85.0;
        private double loadMultiplier = 2.0;

        // Getters and Setters
        public List<String> getRedisKeys() {
            return redisKeys;
        }

        public void setRedisKeys(List<String> redisKeys) {
            this.redisKeys = redisKeys;
        }

        public int getMaxHistorySize() {
            return maxHistorySize;
        }

        public void setMaxHistorySize(int maxHistorySize) {
            this.maxHistorySize = maxHistorySize;
        }

        public String getManualRuleId() {
            return manualRuleId;
        }

        public void setManualRuleId(String manualRuleId) {
            this.manualRuleId = manualRuleId;
        }

        public List<Integer> getSeverityCounts() {
            return severityCounts;
        }

        public void setSeverityCounts(List<Integer> severityCounts) {
            this.severityCounts = severityCounts;
        }

        public String getSystemResolution() {
            return systemResolution;
        }

        public void setSystemResolution(String systemResolution) {
            this.systemResolution = systemResolution;
        }

        public String getNotifierType() {
            return notifierType;
        }

        public void setNotifierType(String notifierType) {
            this.notifierType = notifierType;
        }

        public double getMemoryThreshold() {
            return memoryThreshold;
        }

        public void setMemoryThreshold(double memoryThreshold) {
            this.memoryThreshold = memoryThreshold;
        }

        public double getHeapThreshold() {
            return heapThreshold;
        }

        public void setHeapThreshold(double heapThreshold) {
            this.heapThreshold = heapThreshold;
        }

        public double getLoadMultiplier() {
            return loadMultiplier;
        }

        public void setLoadMultiplier(double loadMultiplier) {
            this.loadMultiplier = loadMultiplier;
        }

        public double getCpuThreshold() {
            return cpuThreshold;
        }

        public void setCpuThreshold(double cpuThreshold) {
            this.cpuThreshold = cpuThreshold;
        }
    }

    /**
     * 性能监控配置
     */
    public static class PerformanceConfig {
        private List<String> metricSuffixes = List.of("_execution_time", "ms", "api_response_time", "db_query_time");
        private List<Integer> statusCodes = List.of(400);
        private List<String> statsKeys = List.of("count", "totalTime", "averageTime", "methodStats", "apiStats");
        private List<String> thresholdKeys = List.of("cpu_usage", "memory_usage", "response_time", "db_query_time");
        private List<Double> thresholdValues = List.of(80.0, 85.0, 1000.0, 500.0);
        private boolean alertEnabled = true;
        private double defaultValue = 0.0;
        private int connectionPoolSize = 20;
        private int loopLimit = 5;
        private String alertPrefix = "ALERT_";
        private List<String> metricTypes = List.of("cpu_usage", "memory_usage", "disk_usage", "network_usage",
                "thread_count", "error_rate", "active_connections", "idle_connections", "cache_hit_rate");

        // Getters and Setters
        public List<String> getMetricSuffixes() {
            return metricSuffixes;
        }

        public void setMetricSuffixes(List<String> metricSuffixes) {
            this.metricSuffixes = metricSuffixes;
        }

        public List<Integer> getStatusCodes() {
            return statusCodes;
        }

        public void setStatusCodes(List<Integer> statusCodes) {
            this.statusCodes = statusCodes;
        }

        public List<String> getStatsKeys() {
            return statsKeys;
        }

        public void setStatsKeys(List<String> statsKeys) {
            this.statsKeys = statsKeys;
        }

        public List<String> getThresholdKeys() {
            return thresholdKeys;
        }

        public void setThresholdKeys(List<String> thresholdKeys) {
            this.thresholdKeys = thresholdKeys;
        }

        public List<Double> getThresholdValues() {
            return thresholdValues;
        }

        public void setThresholdValues(List<Double> thresholdValues) {
            this.thresholdValues = thresholdValues;
        }

        public boolean isAlertEnabled() {
            return alertEnabled;
        }

        public void setAlertEnabled(boolean alertEnabled) {
            this.alertEnabled = alertEnabled;
        }

        public double getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(double defaultValue) {
            this.defaultValue = defaultValue;
        }

        public int getConnectionPoolSize() {
            return connectionPoolSize;
        }

        public void setConnectionPoolSize(int connectionPoolSize) {
            this.connectionPoolSize = connectionPoolSize;
        }

        public int getLoopLimit() {
            return loopLimit;
        }

        public void setLoopLimit(int loopLimit) {
            this.loopLimit = loopLimit;
        }

        public String getAlertPrefix() {
            return alertPrefix;
        }

        public void setAlertPrefix(String alertPrefix) {
            this.alertPrefix = alertPrefix;
        }

        public List<String> getMetricTypes() {
            return metricTypes;
        }

        public void setMetricTypes(List<String> metricTypes) {
            this.metricTypes = metricTypes;
        }
    }

    /**
     * Redis键配置
     */
    public static class RedisKeys {
        /**
         * 系统监控相关键
         */
        private String cpuUsage = "cpu.usage";
        private String memoryUsed = "memory.used";
        private String memoryMax = "memory.max";
        private String memoryUsagePercent = "memory.usage.percent";
        private String heapUsed = "heap.used";
        private String heapMax = "heap.max";
        private String heapUsagePercent = "heap.usage.percent";
        private String loadAverage = "load.average";

        /**
         * 健康检查相关键
         */
        private String healthCheckPrefix = "health:check:";

        /**
         * 缓存锁相关键
         */
        private String cacheLockPrefix = "cache:lock:";

        /**
         * 限流相关键
         */
        private String rateLimitPrefix = "rate_limit:";
        private String rateLimitIpPrefix = "ip:";
        private String rateLimitUserPrefix = "user:";
        private String rateLimitEndpointPrefix = "endpoint:";

        /**
         * 权限相关键
         */
        private String permissionCachePrefix = "permission:user:";
        private String userPermissionsPrefix = "user:permissions:";

        /**
         * 系统状态相关键
         */
        private String systemConcurrentUsersCount = "system:concurrent:users:count";

        /**
         * 缓存失效通知相关键
         */
        private String cacheEvictTopicPrefix = "honyrun:cache:evict:";

        /**
         * IP过滤相关键
         */
        private String ipWhitelistKey = "security:ip:whitelist";
        private String ipBlacklistKey = "security:ip:blacklist";
        private String ipTempBlockPrefix = "security:ip:temp_block:";

        /**
         * 告警相关键
         */
        private String alertRulesKey = "monitoring:alert:rules";
        private String alertEventsKey = "monitoring:alert:events";
        private String alertSuppressionsKey = "monitoring:alert:suppressions";
        private String alertStatisticsKey = "monitoring:alert:statistics";

        /**
         * 缓存版本控制相关键
         */
        private String cacheVersionPrefix = "cache:version:";
        private String cacheSyncChannel = "cache:sync:channel";

        // Getters and Setters
        public String getCpuUsage() {
            return cpuUsage;
        }

        public void setCpuUsage(String cpuUsage) {
            this.cpuUsage = cpuUsage;
        }

        public String getMemoryUsed() {
            return memoryUsed;
        }

        public void setMemoryUsed(String memoryUsed) {
            this.memoryUsed = memoryUsed;
        }

        public String getMemoryMax() {
            return memoryMax;
        }

        public void setMemoryMax(String memoryMax) {
            this.memoryMax = memoryMax;
        }

        public String getMemoryUsagePercent() {
            return memoryUsagePercent;
        }

        public void setMemoryUsagePercent(String memoryUsagePercent) {
            this.memoryUsagePercent = memoryUsagePercent;
        }

        public String getHeapUsed() {
            return heapUsed;
        }

        public void setHeapUsed(String heapUsed) {
            this.heapUsed = heapUsed;
        }

        public String getHeapMax() {
            return heapMax;
        }

        public void setHeapMax(String heapMax) {
            this.heapMax = heapMax;
        }

        public String getHeapUsagePercent() {
            return heapUsagePercent;
        }

        public void setHeapUsagePercent(String heapUsagePercent) {
            this.heapUsagePercent = heapUsagePercent;
        }

        public String getLoadAverage() {
            return loadAverage;
        }

        public void setLoadAverage(String loadAverage) {
            this.loadAverage = loadAverage;
        }

        public String getHealthCheckPrefix() {
            return healthCheckPrefix;
        }

        public void setHealthCheckPrefix(String healthCheckPrefix) {
            this.healthCheckPrefix = healthCheckPrefix;
        }

        public String getCacheLockPrefix() {
            return cacheLockPrefix;
        }

        public void setCacheLockPrefix(String cacheLockPrefix) {
            this.cacheLockPrefix = cacheLockPrefix;
        }

        public String getRateLimitPrefix() {
            return rateLimitPrefix;
        }

        public void setRateLimitPrefix(String rateLimitPrefix) {
            this.rateLimitPrefix = rateLimitPrefix;
        }

        public String getRateLimitIpPrefix() {
            return rateLimitIpPrefix;
        }

        public void setRateLimitIpPrefix(String rateLimitIpPrefix) {
            this.rateLimitIpPrefix = rateLimitIpPrefix;
        }

        public String getRateLimitUserPrefix() {
            return rateLimitUserPrefix;
        }

        public void setRateLimitUserPrefix(String rateLimitUserPrefix) {
            this.rateLimitUserPrefix = rateLimitUserPrefix;
        }

        public String getRateLimitEndpointPrefix() {
            return rateLimitEndpointPrefix;
        }

        public void setRateLimitEndpointPrefix(String rateLimitEndpointPrefix) {
            this.rateLimitEndpointPrefix = rateLimitEndpointPrefix;
        }

        public String getPermissionCachePrefix() {
            return permissionCachePrefix;
        }

        public void setPermissionCachePrefix(String permissionCachePrefix) {
            this.permissionCachePrefix = permissionCachePrefix;
        }

        public String getUserPermissionsPrefix() {
            return userPermissionsPrefix;
        }

        public void setUserPermissionsPrefix(String userPermissionsPrefix) {
            this.userPermissionsPrefix = userPermissionsPrefix;
        }

        public String getSystemConcurrentUsersCount() {
            return systemConcurrentUsersCount;
        }

        public void setSystemConcurrentUsersCount(String systemConcurrentUsersCount) {
            this.systemConcurrentUsersCount = systemConcurrentUsersCount;
        }

        public String getCacheEvictTopicPrefix() {
            return cacheEvictTopicPrefix;
        }

        public void setCacheEvictTopicPrefix(String cacheEvictTopicPrefix) {
            this.cacheEvictTopicPrefix = cacheEvictTopicPrefix;
        }

        public String getIpWhitelistKey() {
            return ipWhitelistKey;
        }

        public void setIpWhitelistKey(String ipWhitelistKey) {
            this.ipWhitelistKey = ipWhitelistKey;
        }

        public String getIpBlacklistKey() {
            return ipBlacklistKey;
        }

        public void setIpBlacklistKey(String ipBlacklistKey) {
            this.ipBlacklistKey = ipBlacklistKey;
        }

        public String getIpTempBlockPrefix() {
            return ipTempBlockPrefix;
        }

        public void setIpTempBlockPrefix(String ipTempBlockPrefix) {
            this.ipTempBlockPrefix = ipTempBlockPrefix;
        }

        public String getAlertRulesKey() {
            return alertRulesKey;
        }

        public void setAlertRulesKey(String alertRulesKey) {
            this.alertRulesKey = alertRulesKey;
        }

        public String getAlertEventsKey() {
            return alertEventsKey;
        }

        public void setAlertEventsKey(String alertEventsKey) {
            this.alertEventsKey = alertEventsKey;
        }

        public String getAlertSuppressionsKey() {
            return alertSuppressionsKey;
        }

        public void setAlertSuppressionsKey(String alertSuppressionsKey) {
            this.alertSuppressionsKey = alertSuppressionsKey;
        }

        public String getAlertStatisticsKey() {
            return alertStatisticsKey;
        }

        public void setAlertStatisticsKey(String alertStatisticsKey) {
            this.alertStatisticsKey = alertStatisticsKey;
        }

        public String getCacheVersionPrefix() {
            return cacheVersionPrefix;
        }

        public void setCacheVersionPrefix(String cacheVersionPrefix) {
            this.cacheVersionPrefix = cacheVersionPrefix;
        }

        public String getCacheSyncChannel() {
            return cacheSyncChannel;
        }

        public void setCacheSyncChannel(String cacheSyncChannel) {
            this.cacheSyncChannel = cacheSyncChannel;
        }
    }

    /**
     * 可视化配置
     */
    public static class VisualizationConfig {
        /**
         * 图表配置Redis前缀
         */
        private String redisPrefix = "monitoring:chart:config:";

        /**
         * 图表类型
         */
        private List<String> chartTypes = List.of("line", "bar", "pie", "area", "scatter");

        /**
         * 数据刷新间隔（毫秒）
         */
        private long refreshInterval = 5000;

        /**
         * 历史数据保留天数
         */
        private int historyRetentionDays = 30;

        /**
         * 最大数据点数
         */
        private int maxDataPoints = 1000;

        /**
         * 图表缓存TTL（秒）
         */
        private long chartCacheTtl = 300;

        /**
         * 实时数据更新间隔（毫秒）
         */
        private long realTimeUpdateInterval = 1000;

        /**
         * 图表配置缓存TTL（秒）
         */
        private long configCacheTtl = 3600;

        // Getters and Setters
        public String getRedisPrefix() {
            return redisPrefix;
        }

        public void setRedisPrefix(String redisPrefix) {
            this.redisPrefix = redisPrefix;
        }

        public List<String> getChartTypes() {
            return chartTypes;
        }

        public void setChartTypes(List<String> chartTypes) {
            this.chartTypes = chartTypes;
        }

        public long getRefreshInterval() {
            return refreshInterval;
        }

        public void setRefreshInterval(long refreshInterval) {
            this.refreshInterval = refreshInterval;
        }

        public int getHistoryRetentionDays() {
            return historyRetentionDays;
        }

        public void setHistoryRetentionDays(int historyRetentionDays) {
            this.historyRetentionDays = historyRetentionDays;
        }

        public int getMaxDataPoints() {
            return maxDataPoints;
        }

        public void setMaxDataPoints(int maxDataPoints) {
            this.maxDataPoints = maxDataPoints;
        }

        public long getChartCacheTtl() {
            return chartCacheTtl;
        }

        public void setChartCacheTtl(long chartCacheTtl) {
            this.chartCacheTtl = chartCacheTtl;
        }

        public long getRealTimeUpdateInterval() {
            return realTimeUpdateInterval;
        }

        public void setRealTimeUpdateInterval(long realTimeUpdateInterval) {
            this.realTimeUpdateInterval = realTimeUpdateInterval;
        }

        public long getConfigCacheTtl() {
            return configCacheTtl;
        }

        public void setConfigCacheTtl(long configCacheTtl) {
            this.configCacheTtl = configCacheTtl;
        }
    }

    /**
     * Redis键配置实例
     */
    private RedisKeys redisKeys = new RedisKeys();

    public RedisKeys getRedisKeys() {
        return redisKeys;
    }

    public void setRedisKeys(RedisKeys redisKeys) {
        this.redisKeys = redisKeys;
    }

    /**
     * 数值常量配置
     *
     * @author: Mr.Rey Copyright © 2025
     * @created: 2025-10-27 12:26:43
     * @version: 1.0.0
     */
    public static class Constants {
        // 通用数值常量
        private int defaultBatchSize = 100;
        private int maxBatchSize = 1000;
        private int defaultPageSize = 100;
        private int maxPageSize = 1000;
        private int defaultQueryLimit = 100;
        private int maxQueryLimit = 10000;
        private int defaultDaysRange = 30;

        // 百分比计算常量
        private double percentageBase = 100.0;
        private double fullPercentage = 100.0;

        // 文件大小常量
        private long defaultMaxFileSize = 10 * 1024 * 1024; // 10MB
        private long maxFileSize = 50 * 1024 * 1024; // 50MB
        private long bytesPerKB = 1024;
        private long bytesPerMB = 1024 * 1024;

        // 字符串长度限制
        private int defaultStringLength = 100;
        private int maxStringLength = 10000;
        private int shortStringLength = 200;
        private int mediumStringLength = 500;
        private int longStringLength = 5000;

        // 密码长度限制
        private int minPasswordLength = 3;
        private int maxPasswordLength = 100;
        private int defaultPasswordLength = 6;

        // 系统阈值
        private int maxConcurrentUsers = 100;
        private int maxActiveThreads = 1000;
        private int maxHistorySize = 1000;
        private int maxCacheSize = 10000;

        // 状态码
        private int successStatusCode = 200;
        private int errorStatusCode = 500;
        private int clientErrorStatusCode = 400;

        // 重试相关
        private int defaultRetryCount = 3;
        private int maxRetryCount = 10;

        // 随机数范围
        private int randomSuffixRange = 10000;
        private int randomValueRange = 100;

        // 进度和质量分数
        private int minScore = 0;
        private int maxScore = 100;
        private int defaultScore = 100;

        // 网络相关
        private int defaultNetworkBufferSize = 1024;
        private int largeNetworkBufferSize = 2048;
        private int maxNetworkBufferSize = 4096;

        // 图像处理
        private int maxImageDimension = 5000;
        private int defaultImageQuality = 100;

        // 时间单位转换
        private long millisecondsPerSecond = 1000;
        private long secondsPerMinute = 60;
        private long minutesPerHour = 60;
        private long hoursPerDay = 24;
        private long millisecondsPerMinute = 60000;
        private long millisecondsPerDay = 24 * 60 * 60 * 1000;

        // Getters and Setters
        public int getDefaultBatchSize() {
            return defaultBatchSize;
        }

        public void setDefaultBatchSize(int defaultBatchSize) {
            this.defaultBatchSize = defaultBatchSize;
        }

        public int getMaxBatchSize() {
            return maxBatchSize;
        }

        public void setMaxBatchSize(int maxBatchSize) {
            this.maxBatchSize = maxBatchSize;
        }

        public int getDefaultPageSize() {
            return defaultPageSize;
        }

        public void setDefaultPageSize(int defaultPageSize) {
            this.defaultPageSize = defaultPageSize;
        }

        public int getMaxPageSize() {
            return maxPageSize;
        }

        public void setMaxPageSize(int maxPageSize) {
            this.maxPageSize = maxPageSize;
        }

        public int getDefaultQueryLimit() {
            return defaultQueryLimit;
        }

        public void setDefaultQueryLimit(int defaultQueryLimit) {
            this.defaultQueryLimit = defaultQueryLimit;
        }

        public int getMaxQueryLimit() {
            return maxQueryLimit;
        }

        public void setMaxQueryLimit(int maxQueryLimit) {
            this.maxQueryLimit = maxQueryLimit;
        }

        public int getQueryLimit() {
            return defaultQueryLimit;
        }

        public double getPercentageBase() {
            return percentageBase;
        }

        public void setPercentageBase(double percentageBase) {
            this.percentageBase = percentageBase;
        }

        public double getFullPercentage() {
            return fullPercentage;
        }

        public void setFullPercentage(double fullPercentage) {
            this.fullPercentage = fullPercentage;
        }

        public long getDefaultMaxFileSize() {
            return defaultMaxFileSize;
        }

        public void setDefaultMaxFileSize(long defaultMaxFileSize) {
            this.defaultMaxFileSize = defaultMaxFileSize;
        }

        public long getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(long maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public long getBytesPerKB() {
            return bytesPerKB;
        }

        public void setBytesPerKB(long bytesPerKB) {
            this.bytesPerKB = bytesPerKB;
        }

        public long getBytesPerMB() {
            return bytesPerMB;
        }

        public void setBytesPerMB(long bytesPerMB) {
            this.bytesPerMB = bytesPerMB;
        }

        public int getDefaultStringLength() {
            return defaultStringLength;
        }

        public void setDefaultStringLength(int defaultStringLength) {
            this.defaultStringLength = defaultStringLength;
        }

        public int getMaxStringLength() {
            return maxStringLength;
        }

        public void setMaxStringLength(int maxStringLength) {
            this.maxStringLength = maxStringLength;
        }

        public int getShortStringLength() {
            return shortStringLength;
        }

        public void setShortStringLength(int shortStringLength) {
            this.shortStringLength = shortStringLength;
        }

        public int getMediumStringLength() {
            return mediumStringLength;
        }

        public void setMediumStringLength(int mediumStringLength) {
            this.mediumStringLength = mediumStringLength;
        }

        public int getLongStringLength() {
            return longStringLength;
        }

        public void setLongStringLength(int longStringLength) {
            this.longStringLength = longStringLength;
        }

        public int getMinPasswordLength() {
            return minPasswordLength;
        }

        public void setMinPasswordLength(int minPasswordLength) {
            this.minPasswordLength = minPasswordLength;
        }

        public int getMaxPasswordLength() {
            return maxPasswordLength;
        }

        public void setMaxPasswordLength(int maxPasswordLength) {
            this.maxPasswordLength = maxPasswordLength;
        }

        public int getDefaultPasswordLength() {
            return defaultPasswordLength;
        }

        public void setDefaultPasswordLength(int defaultPasswordLength) {
            this.defaultPasswordLength = defaultPasswordLength;
        }

        public int getMaxConcurrentUsers() {
            return maxConcurrentUsers;
        }

        public void setMaxConcurrentUsers(int maxConcurrentUsers) {
            this.maxConcurrentUsers = maxConcurrentUsers;
        }

        public int getMaxActiveThreads() {
            return maxActiveThreads;
        }

        public void setMaxActiveThreads(int maxActiveThreads) {
            this.maxActiveThreads = maxActiveThreads;
        }

        public int getMaxHistorySize() {
            return maxHistorySize;
        }

        public void setMaxHistorySize(int maxHistorySize) {
            this.maxHistorySize = maxHistorySize;
        }

        public int getMaxCacheSize() {
            return maxCacheSize;
        }

        public void setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
        }

        public int getSuccessStatusCode() {
            return successStatusCode;
        }

        public void setSuccessStatusCode(int successStatusCode) {
            this.successStatusCode = successStatusCode;
        }

        public int getErrorStatusCode() {
            return errorStatusCode;
        }

        public void setErrorStatusCode(int errorStatusCode) {
            this.errorStatusCode = errorStatusCode;
        }

        public int getClientErrorStatusCode() {
            return clientErrorStatusCode;
        }

        public void setClientErrorStatusCode(int clientErrorStatusCode) {
            this.clientErrorStatusCode = clientErrorStatusCode;
        }

        public int getDefaultRetryCount() {
            return defaultRetryCount;
        }

        public void setDefaultRetryCount(int defaultRetryCount) {
            this.defaultRetryCount = defaultRetryCount;
        }

        public int getMaxRetryCount() {
            return maxRetryCount;
        }

        public void setMaxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
        }

        public int getRandomSuffixRange() {
            return randomSuffixRange;
        }

        public void setRandomSuffixRange(int randomSuffixRange) {
            this.randomSuffixRange = randomSuffixRange;
        }

        public int getRandomValueRange() {
            return randomValueRange;
        }

        public void setRandomValueRange(int randomValueRange) {
            this.randomValueRange = randomValueRange;
        }

        public int getMinScore() {
            return minScore;
        }

        public void setMinScore(int minScore) {
            this.minScore = minScore;
        }

        public int getMaxScore() {
            return maxScore;
        }

        public void setMaxScore(int maxScore) {
            this.maxScore = maxScore;
        }

        public int getDefaultScore() {
            return defaultScore;
        }

        public void setDefaultScore(int defaultScore) {
            this.defaultScore = defaultScore;
        }

        public int getDefaultNetworkBufferSize() {
            return defaultNetworkBufferSize;
        }

        public void setDefaultNetworkBufferSize(int defaultNetworkBufferSize) {
            this.defaultNetworkBufferSize = defaultNetworkBufferSize;
        }

        public int getLargeNetworkBufferSize() {
            return largeNetworkBufferSize;
        }

        public void setLargeNetworkBufferSize(int largeNetworkBufferSize) {
            this.largeNetworkBufferSize = largeNetworkBufferSize;
        }

        public int getMaxNetworkBufferSize() {
            return maxNetworkBufferSize;
        }

        public void setMaxNetworkBufferSize(int maxNetworkBufferSize) {
            this.maxNetworkBufferSize = maxNetworkBufferSize;
        }

        public int getMaxImageDimension() {
            return maxImageDimension;
        }

        public void setMaxImageDimension(int maxImageDimension) {
            this.maxImageDimension = maxImageDimension;
        }

        public int getDefaultImageQuality() {
            return defaultImageQuality;
        }

        public void setDefaultImageQuality(int defaultImageQuality) {
            this.defaultImageQuality = defaultImageQuality;
        }

        public long getMillisecondsPerSecond() {
            return millisecondsPerSecond;
        }

        public void setMillisecondsPerSecond(long millisecondsPerSecond) {
            this.millisecondsPerSecond = millisecondsPerSecond;
        }

        public long getSecondsPerMinute() {
            return secondsPerMinute;
        }

        public void setSecondsPerMinute(long secondsPerMinute) {
            this.secondsPerMinute = secondsPerMinute;
        }

        public long getMinutesPerHour() {
            return minutesPerHour;
        }

        public void setMinutesPerHour(long minutesPerHour) {
            this.minutesPerHour = minutesPerHour;
        }

        public long getHoursPerDay() {
            return hoursPerDay;
        }

        public void setHoursPerDay(long hoursPerDay) {
            this.hoursPerDay = hoursPerDay;
        }

        public long getMillisecondsPerMinute() {
            return millisecondsPerMinute;
        }

        public void setMillisecondsPerMinute(long millisecondsPerMinute) {
            this.millisecondsPerMinute = millisecondsPerMinute;
        }

        public long getMillisecondsPerDay() {
            return millisecondsPerDay;
        }

        public void setMillisecondsPerDay(long millisecondsPerDay) {
            this.millisecondsPerDay = millisecondsPerDay;
        }

        public int getDefaultDaysRange() {
            return defaultDaysRange;
        }

        public void setDefaultDaysRange(int defaultDaysRange) {
            this.defaultDaysRange = defaultDaysRange;
        }
    }

    /**
     * Duration配置
     *
     * @author: Mr.Rey Copyright © 2025
     * @created: 2025-10-27 12:26:43
     * @version: 1.0.0
     */
    public static class Durations {
        // 超时配置
        private Duration defaultTimeout = Duration.ofSeconds(30);
        private Duration shortTimeout = Duration.ofSeconds(5);
        private Duration mediumTimeout = Duration.ofSeconds(10);
        private Duration longTimeout = Duration.ofSeconds(60);

        // 重试间隔
        private Duration defaultRetryDelay = Duration.ofMillis(500);
        private Duration shortRetryDelay = Duration.ofMillis(100);
        private Duration mediumRetryDelay = Duration.ofMillis(200);
        private Duration longRetryDelay = Duration.ofSeconds(1);

        // 缓存TTL配置
        private Duration shortCacheTtl = Duration.ofMinutes(5);
        private Duration mediumCacheTtl = Duration.ofMinutes(30);
        private Duration longCacheTtl = Duration.ofHours(1);
        private Duration extraLongCacheTtl = Duration.ofHours(24);
        private Duration maxCacheTtl = Duration.ofHours(48);
        private Duration ultraLongCacheTtl = Duration.ofHours(72);
        private Duration defaultCacheTtl = Duration.ofMinutes(15);

        // 调度间隔
        private Duration shortScheduleInterval = Duration.ofSeconds(30);
        private Duration mediumScheduleInterval = Duration.ofMinutes(1);
        private Duration longScheduleInterval = Duration.ofMinutes(5);
        private Duration defaultScheduleInterval = Duration.ofMinutes(2);

        // 监控间隔
        private Duration healthCheckInterval = Duration.ofSeconds(30);
        private Duration metricsCollectionInterval = Duration.ofMinutes(1);
        private Duration alertCheckInterval = Duration.ofMinutes(5);

        // 清理间隔
        private Duration dataCleanupInterval = Duration.ofHours(24);
        private Duration logCleanupInterval = Duration.ofDays(7);
        private Duration cacheCleanupInterval = Duration.ofHours(6);

        // Getters and Setters
        public Duration getDefaultTimeout() {
            return defaultTimeout;
        }

        public void setDefaultTimeout(Duration defaultTimeout) {
            this.defaultTimeout = defaultTimeout;
        }

        public Duration getShortTimeout() {
            return shortTimeout;
        }

        public void setShortTimeout(Duration shortTimeout) {
            this.shortTimeout = shortTimeout;
        }

        public Duration getMediumTimeout() {
            return mediumTimeout;
        }

        public void setMediumTimeout(Duration mediumTimeout) {
            this.mediumTimeout = mediumTimeout;
        }

        public Duration getLongTimeout() {
            return longTimeout;
        }

        public void setLongTimeout(Duration longTimeout) {
            this.longTimeout = longTimeout;
        }

        public Duration getDefaultRetryDelay() {
            return defaultRetryDelay;
        }

        public void setDefaultRetryDelay(Duration defaultRetryDelay) {
            this.defaultRetryDelay = defaultRetryDelay;
        }

        public Duration getShortRetryDelay() {
            return shortRetryDelay;
        }

        public void setShortRetryDelay(Duration shortRetryDelay) {
            this.shortRetryDelay = shortRetryDelay;
        }

        public Duration getMediumRetryDelay() {
            return mediumRetryDelay;
        }

        public void setMediumRetryDelay(Duration mediumRetryDelay) {
            this.mediumRetryDelay = mediumRetryDelay;
        }

        public Duration getLongRetryDelay() {
            return longRetryDelay;
        }

        public void setLongRetryDelay(Duration longRetryDelay) {
            this.longRetryDelay = longRetryDelay;
        }

        public Duration getShortCacheTtl() {
            return shortCacheTtl;
        }

        public void setShortCacheTtl(Duration shortCacheTtl) {
            this.shortCacheTtl = shortCacheTtl;
        }

        public Duration getMediumCacheTtl() {
            return mediumCacheTtl;
        }

        public void setMediumCacheTtl(Duration mediumCacheTtl) {
            this.mediumCacheTtl = mediumCacheTtl;
        }

        public Duration getLongCacheTtl() {
            return longCacheTtl;
        }

        public void setLongCacheTtl(Duration longCacheTtl) {
            this.longCacheTtl = longCacheTtl;
        }

        public Duration getExtraLongCacheTtl() {
            return extraLongCacheTtl;
        }

        public void setExtraLongCacheTtl(Duration extraLongCacheTtl) {
            this.extraLongCacheTtl = extraLongCacheTtl;
        }

        public Duration getMaxCacheTtl() {
            return maxCacheTtl;
        }

        public void setMaxCacheTtl(Duration maxCacheTtl) {
            this.maxCacheTtl = maxCacheTtl;
        }

        public Duration getUltraLongCacheTtl() {
            return ultraLongCacheTtl;
        }

        public void setUltraLongCacheTtl(Duration ultraLongCacheTtl) {
            this.ultraLongCacheTtl = ultraLongCacheTtl;
        }

        public Duration getDefaultCacheTtl() {
            return defaultCacheTtl;
        }

        public void setDefaultCacheTtl(Duration defaultCacheTtl) {
            this.defaultCacheTtl = defaultCacheTtl;
        }

        public Duration getShortScheduleInterval() {
            return shortScheduleInterval;
        }

        public void setShortScheduleInterval(Duration shortScheduleInterval) {
            this.shortScheduleInterval = shortScheduleInterval;
        }

        public Duration getMediumScheduleInterval() {
            return mediumScheduleInterval;
        }

        public void setMediumScheduleInterval(Duration mediumScheduleInterval) {
            this.mediumScheduleInterval = mediumScheduleInterval;
        }

        public Duration getLongScheduleInterval() {
            return longScheduleInterval;
        }

        public void setLongScheduleInterval(Duration longScheduleInterval) {
            this.longScheduleInterval = longScheduleInterval;
        }

        public Duration getDefaultScheduleInterval() {
            return defaultScheduleInterval;
        }

        public void setDefaultScheduleInterval(Duration defaultScheduleInterval) {
            this.defaultScheduleInterval = defaultScheduleInterval;
        }

        public Duration getHealthCheckInterval() {
            return healthCheckInterval;
        }

        public void setHealthCheckInterval(Duration healthCheckInterval) {
            this.healthCheckInterval = healthCheckInterval;
        }

        public Duration getMetricsCollectionInterval() {
            return metricsCollectionInterval;
        }

        public void setMetricsCollectionInterval(Duration metricsCollectionInterval) {
            this.metricsCollectionInterval = metricsCollectionInterval;
        }

        public Duration getAlertCheckInterval() {
            return alertCheckInterval;
        }

        public void setAlertCheckInterval(Duration alertCheckInterval) {
            this.alertCheckInterval = alertCheckInterval;
        }

        public Duration getDataCleanupInterval() {
            return dataCleanupInterval;
        }

        public void setDataCleanupInterval(Duration dataCleanupInterval) {
            this.dataCleanupInterval = dataCleanupInterval;
        }

        public Duration getLogCleanupInterval() {
            return logCleanupInterval;
        }

        public void setLogCleanupInterval(Duration logCleanupInterval) {
            this.logCleanupInterval = logCleanupInterval;
        }

        public Duration getCacheCleanupInterval() {
            return cacheCleanupInterval;
        }

        public void setCacheCleanupInterval(Duration cacheCleanupInterval) {
            this.cacheCleanupInterval = cacheCleanupInterval;
        }
    }

    /**
     * 超时配置
     *
     * @author: Mr.Rey Copyright © 2025
     * @created: 2025-10-27 12:26:43
     * @version: 1.0.0
     */
    public static class Timeouts {
        // HTTP超时
        private long httpConnectionTimeout = 5000;
        private long httpReadTimeout = 30000;
        private long httpWriteTimeout = 30000;

        // 数据库超时
        private long databaseQueryTimeout = 10000;
        private long databaseConnectionTimeout = 5000;
        private long databaseTransactionTimeout = 30000;

        // 缓存超时
        private long cacheOperationTimeout = 1000;
        private long cacheConnectionTimeout = 2000;

        // 任务超时
        private long taskExecutionTimeout = 60000;
        private long batchProcessingTimeout = 300000; // 5分钟

        // 文件操作超时
        private long fileUploadTimeout = 120000; // 2分钟
        private long fileDownloadTimeout = 300000; // 5分钟

        // 网络超时
        private long networkRequestTimeout = 30000;
        private long networkResponseTimeout = 30000;

        // 认证相关超时
        private long authenticationTimeout = 10000;
        private long tokenValidationTimeout = 5000;

        // 事务超时配置
        private long shortTransactionTimeout = 15;
        private long mediumTransactionTimeout = 30;
        private long longTransactionTimeout = 60;

        // Getters and Setters
        public long getHttpConnectionTimeout() {
            return httpConnectionTimeout;
        }

        public void setHttpConnectionTimeout(long httpConnectionTimeout) {
            this.httpConnectionTimeout = httpConnectionTimeout;
        }

        public long getHttpReadTimeout() {
            return httpReadTimeout;
        }

        public void setHttpReadTimeout(long httpReadTimeout) {
            this.httpReadTimeout = httpReadTimeout;
        }

        public long getHttpWriteTimeout() {
            return httpWriteTimeout;
        }

        public void setHttpWriteTimeout(long httpWriteTimeout) {
            this.httpWriteTimeout = httpWriteTimeout;
        }

        public long getDatabaseQueryTimeout() {
            return databaseQueryTimeout;
        }

        public void setDatabaseQueryTimeout(long databaseQueryTimeout) {
            this.databaseQueryTimeout = databaseQueryTimeout;
        }

        public long getDatabaseConnectionTimeout() {
            return databaseConnectionTimeout;
        }

        public void setDatabaseConnectionTimeout(long databaseConnectionTimeout) {
            this.databaseConnectionTimeout = databaseConnectionTimeout;
        }

        public long getDatabaseTransactionTimeout() {
            return databaseTransactionTimeout;
        }

        public void setDatabaseTransactionTimeout(long databaseTransactionTimeout) {
            this.databaseTransactionTimeout = databaseTransactionTimeout;
        }

        public long getCacheOperationTimeout() {
            return cacheOperationTimeout;
        }

        public void setCacheOperationTimeout(long cacheOperationTimeout) {
            this.cacheOperationTimeout = cacheOperationTimeout;
        }

        public long getCacheConnectionTimeout() {
            return cacheConnectionTimeout;
        }

        public void setCacheConnectionTimeout(long cacheConnectionTimeout) {
            this.cacheConnectionTimeout = cacheConnectionTimeout;
        }

        public long getTaskExecutionTimeout() {
            return taskExecutionTimeout;
        }

        public void setTaskExecutionTimeout(long taskExecutionTimeout) {
            this.taskExecutionTimeout = taskExecutionTimeout;
        }

        public long getBatchProcessingTimeout() {
            return batchProcessingTimeout;
        }

        public void setBatchProcessingTimeout(long batchProcessingTimeout) {
            this.batchProcessingTimeout = batchProcessingTimeout;
        }

        public long getFileUploadTimeout() {
            return fileUploadTimeout;
        }

        public void setFileUploadTimeout(long fileUploadTimeout) {
            this.fileUploadTimeout = fileUploadTimeout;
        }

        public long getFileDownloadTimeout() {
            return fileDownloadTimeout;
        }

        public void setFileDownloadTimeout(long fileDownloadTimeout) {
            this.fileDownloadTimeout = fileDownloadTimeout;
        }

        public long getNetworkRequestTimeout() {
            return networkRequestTimeout;
        }

        public void setNetworkRequestTimeout(long networkRequestTimeout) {
            this.networkRequestTimeout = networkRequestTimeout;
        }

        public long getNetworkResponseTimeout() {
            return networkResponseTimeout;
        }

        public void setNetworkResponseTimeout(long networkResponseTimeout) {
            this.networkResponseTimeout = networkResponseTimeout;
        }

        public long getAuthenticationTimeout() {
            return authenticationTimeout;
        }

        public void setAuthenticationTimeout(long authenticationTimeout) {
            this.authenticationTimeout = authenticationTimeout;
        }

        public long getTokenValidationTimeout() {
            return tokenValidationTimeout;
        }

        public void setTokenValidationTimeout(long tokenValidationTimeout) {
            this.tokenValidationTimeout = tokenValidationTimeout;
        }

        public long getShortTransactionTimeout() {
            return shortTransactionTimeout;
        }

        public void setShortTransactionTimeout(long shortTransactionTimeout) {
            this.shortTransactionTimeout = shortTransactionTimeout;
        }

        public long getMediumTransactionTimeout() {
            return mediumTransactionTimeout;
        }

        public void setMediumTransactionTimeout(long mediumTransactionTimeout) {
            this.mediumTransactionTimeout = mediumTransactionTimeout;
        }

        public long getLongTransactionTimeout() {
            return longTransactionTimeout;
        }

        public void setLongTransactionTimeout(long longTransactionTimeout) {
            this.longTransactionTimeout = longTransactionTimeout;
        }
    }

    // 新增配置实例
    private Constants constants = new Constants();
    private Durations durations = new Durations();
    private Timeouts timeouts = new Timeouts();

    public Constants getConstants() {
        return constants;
    }

    public void setConstants(Constants constants) {
        this.constants = constants;
    }

    public Durations getDurations() {
        return durations;
    }

    public void setDurations(Durations durations) {
        this.durations = durations;
    }

    public Timeouts getTimeouts() {
        return timeouts;
    }

    public void setTimeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
    }
}
