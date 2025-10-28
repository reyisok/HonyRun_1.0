package com.honyrun.service.monitoring;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 响应式监控数据可视化服务接口
 * 
 * 提供监控数据的可视化功能，包括：
 * - 实时监控仪表板数据
 * - 历史趋势图表数据
 * - 性能指标图表
 * - 系统资源使用图表
 * - 告警统计图表
 * - 自定义图表配置
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-02 16:30:00
 * @modified 2025-07-02 16:30:00
 * @version 1.0.0
 */
public interface ReactiveMonitoringVisualizationService {

    // ==================== 仪表板数据 ====================

    /**
     * 获取实时监控仪表板数据
     * 
     * @return 仪表板数据流
     */
    Flux<DashboardData> getRealTimeDashboardData();

    /**
     * 获取系统概览数据
     * 
     * @return 系统概览数据
     */
    Mono<SystemOverviewData> getSystemOverview();

    /**
     * 获取性能概览数据
     * 
     * @return 性能概览数据
     */
    Mono<PerformanceOverviewData> getPerformanceOverview();

    // ==================== 图表数据 ====================

    /**
     * 获取CPU使用率趋势图数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔（分钟）
     * @return CPU使用率趋势数据
     */
    Mono<ChartData> getCpuUsageTrend(LocalDateTime startTime, LocalDateTime endTime, int interval);

    /**
     * 获取内存使用率趋势图数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔（分钟）
     * @return 内存使用率趋势数据
     */
    Mono<ChartData> getMemoryUsageTrend(LocalDateTime startTime, LocalDateTime endTime, int interval);

    /**
     * 获取响应时间趋势图数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔（分钟）
     * @return 响应时间趋势数据
     */
    Mono<ChartData> getResponseTimeTrend(LocalDateTime startTime, LocalDateTime endTime, int interval);

    /**
     * 获取请求量趋势图数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔（分钟）
     * @return 请求量趋势数据
     */
    Mono<ChartData> getRequestCountTrend(LocalDateTime startTime, LocalDateTime endTime, int interval);

    /**
     * 获取错误率趋势图数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔（分钟）
     * @return 错误率趋势数据
     */
    Mono<ChartData> getErrorRateTrend(LocalDateTime startTime, LocalDateTime endTime, int interval);

    // ==================== 告警可视化 ====================

    /**
     * 获取告警统计图表数据
     * 
     * @param days 统计天数
     * @return 告警统计数据
     */
    Mono<ChartData> getAlertStatistics(int days);

    /**
     * 获取告警趋势图数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 告警趋势数据
     */
    Mono<ChartData> getAlertTrend(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取告警分布饼图数据
     * 
     * @param days 统计天数
     * @return 告警分布数据
     */
    Mono<PieChartData> getAlertDistribution(int days);

    // ==================== 性能可视化 ====================

    /**
     * 获取JVM性能图表数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return JVM性能数据
     */
    Mono<MultiSeriesChartData> getJvmPerformanceData(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取数据库性能图表数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 数据库性能数据
     */
    Mono<MultiSeriesChartData> getDatabasePerformanceData(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取缓存性能图表数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 缓存性能数据
     */
    Mono<MultiSeriesChartData> getCachePerformanceData(LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 自定义图表 ====================

    /**
     * 创建自定义图表配置
     * 
     * @param config 图表配置
     * @return 创建结果
     */
    Mono<String> createCustomChart(ChartConfig config);

    /**
     * 获取自定义图表数据
     * 
     * @param chartId 图表ID
     * @param parameters 查询参数
     * @return 图表数据
     */
    Mono<ChartData> getCustomChartData(String chartId, Map<String, Object> parameters);

    /**
     * 获取所有自定义图表配置
     * 
     * @return 图表配置列表
     */
    Flux<ChartConfig> getAllCustomCharts();

    /**
     * 删除自定义图表
     * 
     * @param chartId 图表ID
     * @return 删除结果
     */
    Mono<Boolean> deleteCustomChart(String chartId);

    // ==================== 数据传输对象 ====================

    /**
     * 仪表板数据
     */
    class DashboardData {
        private LocalDateTime timestamp;
        private SystemMetrics systemMetrics;
        private PerformanceMetrics performanceMetrics;
        private AlertSummary alertSummary;
        private Map<String, Object> customMetrics;

        // 构造函数
        public DashboardData() {}

        public DashboardData(LocalDateTime timestamp, SystemMetrics systemMetrics, 
                           PerformanceMetrics performanceMetrics, AlertSummary alertSummary,
                           Map<String, Object> customMetrics) {
            this.timestamp = timestamp;
            this.systemMetrics = systemMetrics;
            this.performanceMetrics = performanceMetrics;
            this.alertSummary = alertSummary;
            this.customMetrics = customMetrics;
        }

        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public SystemMetrics getSystemMetrics() { return systemMetrics; }
        public void setSystemMetrics(SystemMetrics systemMetrics) { this.systemMetrics = systemMetrics; }
        public PerformanceMetrics getPerformanceMetrics() { return performanceMetrics; }
        public void setPerformanceMetrics(PerformanceMetrics performanceMetrics) { this.performanceMetrics = performanceMetrics; }
        public AlertSummary getAlertSummary() { return alertSummary; }
        public void setAlertSummary(AlertSummary alertSummary) { this.alertSummary = alertSummary; }
        public Map<String, Object> getCustomMetrics() { return customMetrics; }
        public void setCustomMetrics(Map<String, Object> customMetrics) { this.customMetrics = customMetrics; }
    }

    /**
     * 系统概览数据
     */
    class SystemOverviewData {
        private String systemStatus;
        private double cpuUsage;
        private double memoryUsage;
        private double diskUsage;
        private int activeConnections;
        private long uptime;
        private String version;

        // 构造函数
        public SystemOverviewData() {}

        public SystemOverviewData(String systemStatus, double cpuUsage, double memoryUsage,
                                double diskUsage, int activeConnections, long uptime, String version) {
            this.systemStatus = systemStatus;
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.diskUsage = diskUsage;
            this.activeConnections = activeConnections;
            this.uptime = uptime;
            this.version = version;
        }

        // Getters and Setters
        public String getSystemStatus() { return systemStatus; }
        public void setSystemStatus(String systemStatus) { this.systemStatus = systemStatus; }
        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }
        public double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
        public double getDiskUsage() { return diskUsage; }
        public void setDiskUsage(double diskUsage) { this.diskUsage = diskUsage; }
        public int getActiveConnections() { return activeConnections; }
        public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }
        public long getUptime() { return uptime; }
        public void setUptime(long uptime) { this.uptime = uptime; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }

    /**
     * 性能概览数据
     */
    class PerformanceOverviewData {
        private double averageResponseTime;
        private long totalRequests;
        private double errorRate;
        private double throughput;
        private int activeThreads;
        private double gcTime;

        // 构造函数
        public PerformanceOverviewData() {}

        public PerformanceOverviewData(double averageResponseTime, long totalRequests, double errorRate,
                                     double throughput, int activeThreads, double gcTime) {
            this.averageResponseTime = averageResponseTime;
            this.totalRequests = totalRequests;
            this.errorRate = errorRate;
            this.throughput = throughput;
            this.activeThreads = activeThreads;
            this.gcTime = gcTime;
        }

        // Getters and Setters
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
        public double getThroughput() { return throughput; }
        public void setThroughput(double throughput) { this.throughput = throughput; }
        public int getActiveThreads() { return activeThreads; }
        public void setActiveThreads(int activeThreads) { this.activeThreads = activeThreads; }
        public double getGcTime() { return gcTime; }
        public void setGcTime(double gcTime) { this.gcTime = gcTime; }
    }

    /**
     * 图表数据
     */
    class ChartData {
        private String chartType;
        private String title;
        private List<String> labels;
        private List<DataSeries> series;
        private Map<String, Object> options;

        // 构造函数
        public ChartData() {}

        public ChartData(String chartType, String title, List<String> labels, 
                        List<DataSeries> series, Map<String, Object> options) {
            this.chartType = chartType;
            this.title = title;
            this.labels = labels;
            this.series = series;
            this.options = options;
        }

        // Getters and Setters
        public String getChartType() { return chartType; }
        public void setChartType(String chartType) { this.chartType = chartType; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<String> getLabels() { return labels; }
        public void setLabels(List<String> labels) { this.labels = labels; }
        public List<DataSeries> getSeries() { return series; }
        public void setSeries(List<DataSeries> series) { this.series = series; }
        public Map<String, Object> getOptions() { return options; }
        public void setOptions(Map<String, Object> options) { this.options = options; }
    }

    /**
     * 饼图数据
     */
    class PieChartData {
        private String title;
        private List<PieSlice> slices;
        private Map<String, Object> options;

        // 构造函数
        public PieChartData() {}

        public PieChartData(String title, List<PieSlice> slices, Map<String, Object> options) {
            this.title = title;
            this.slices = slices;
            this.options = options;
        }

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<PieSlice> getSlices() { return slices; }
        public void setSlices(List<PieSlice> slices) { this.slices = slices; }
        public Map<String, Object> getOptions() { return options; }
        public void setOptions(Map<String, Object> options) { this.options = options; }
    }

    /**
     * 多系列图表数据
     */
    class MultiSeriesChartData {
        private String chartType;
        private String title;
        private List<String> labels;
        private List<DataSeries> series;
        private Map<String, Object> options;

        // 构造函数
        public MultiSeriesChartData() {}

        public MultiSeriesChartData(String chartType, String title, List<String> labels,
                                  List<DataSeries> series, Map<String, Object> options) {
            this.chartType = chartType;
            this.title = title;
            this.labels = labels;
            this.series = series;
            this.options = options;
        }

        // Getters and Setters
        public String getChartType() { return chartType; }
        public void setChartType(String chartType) { this.chartType = chartType; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<String> getLabels() { return labels; }
        public void setLabels(List<String> labels) { this.labels = labels; }
        public List<DataSeries> getSeries() { return series; }
        public void setSeries(List<DataSeries> series) { this.series = series; }
        public Map<String, Object> getOptions() { return options; }
        public void setOptions(Map<String, Object> options) { this.options = options; }
    }

    /**
     * 数据系列
     */
    class DataSeries {
        private String name;
        private List<Double> data;
        private String color;
        private String type;

        // 构造函数
        public DataSeries() {}

        public DataSeries(String name, List<Double> data, String color, String type) {
            this.name = name;
            this.data = data;
            this.color = color;
            this.type = type;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<Double> getData() { return data; }
        public void setData(List<Double> data) { this.data = data; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    /**
     * 饼图切片
     */
    class PieSlice {
        private String name;
        private double value;
        private String color;

        // 构造函数
        public PieSlice() {}

        public PieSlice(String name, double value, String color) {
            this.name = name;
            this.value = value;
            this.color = color;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    /**
     * 图表配置
     */
    class ChartConfig {
        private String id;
        private String name;
        private String type;
        private String dataSource;
        private Map<String, Object> query;
        private Map<String, Object> options;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // 构造函数
        public ChartConfig() {}

        public ChartConfig(String id, String name, String type, String dataSource,
                          Map<String, Object> query, Map<String, Object> options,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.dataSource = dataSource;
            this.query = query;
            this.options = options;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }
        public Map<String, Object> getQuery() { return query; }
        public void setQuery(Map<String, Object> query) { this.query = query; }
        public Map<String, Object> getOptions() { return options; }
        public void setOptions(Map<String, Object> options) { this.options = options; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    /**
     * 系统指标
     */
    class SystemMetrics {
        private double cpuUsage;
        private double memoryUsage;
        private double diskUsage;
        private int activeConnections;

        // 构造函数
        public SystemMetrics() {}

        public SystemMetrics(double cpuUsage, double memoryUsage, double diskUsage, int activeConnections) {
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.diskUsage = diskUsage;
            this.activeConnections = activeConnections;
        }

        // Getters and Setters
        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }
        public double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
        public double getDiskUsage() { return diskUsage; }
        public void setDiskUsage(double diskUsage) { this.diskUsage = diskUsage; }
        public int getActiveConnections() { return activeConnections; }
        public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }
    }

    /**
     * 性能指标
     */
    class PerformanceMetrics {
        private double responseTime;
        private long requestCount;
        private double errorRate;
        private double throughput;

        // 构造函数
        public PerformanceMetrics() {}

        public PerformanceMetrics(double responseTime, long requestCount, double errorRate, double throughput) {
            this.responseTime = responseTime;
            this.requestCount = requestCount;
            this.errorRate = errorRate;
            this.throughput = throughput;
        }

        // Getters and Setters
        public double getResponseTime() { return responseTime; }
        public void setResponseTime(double responseTime) { this.responseTime = responseTime; }
        public long getRequestCount() { return requestCount; }
        public void setRequestCount(long requestCount) { this.requestCount = requestCount; }
        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
        public double getThroughput() { return throughput; }
        public void setThroughput(double throughput) { this.throughput = throughput; }
    }

    /**
     * 告警摘要
     */
    class AlertSummary {
        private int totalAlerts;
        private int criticalAlerts;
        private int warningAlerts;
        private int infoAlerts;

        // 构造函数
        public AlertSummary() {}

        public AlertSummary(int totalAlerts, int criticalAlerts, int warningAlerts, int infoAlerts) {
            this.totalAlerts = totalAlerts;
            this.criticalAlerts = criticalAlerts;
            this.warningAlerts = warningAlerts;
            this.infoAlerts = infoAlerts;
        }

        // Getters and Setters
        public int getTotalAlerts() { return totalAlerts; }
        public void setTotalAlerts(int totalAlerts) { this.totalAlerts = totalAlerts; }
        public int getCriticalAlerts() { return criticalAlerts; }
        public void setCriticalAlerts(int criticalAlerts) { this.criticalAlerts = criticalAlerts; }
        public int getWarningAlerts() { return warningAlerts; }
        public void setWarningAlerts(int warningAlerts) { this.warningAlerts = warningAlerts; }
        public int getInfoAlerts() { return infoAlerts; }
        public void setInfoAlerts(int infoAlerts) { this.infoAlerts = infoAlerts; }
    }
}

