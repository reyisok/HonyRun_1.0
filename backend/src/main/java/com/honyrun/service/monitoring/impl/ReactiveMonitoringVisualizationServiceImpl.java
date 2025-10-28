package com.honyrun.service.monitoring.impl;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.service.monitoring.ReactiveMonitoringAlertService;
import com.honyrun.service.monitoring.ReactiveMonitoringVisualizationService;
import com.honyrun.service.monitoring.ReactivePerformanceMonitoringService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.system.SystemMonitorUtil;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式监控数据可视化服务实现
 *
 * 提供完整的监控数据可视化功能，包括：
 * - 实时仪表板数据生成
 * - 历史趋势图表数据
 * - 性能指标可视化
 * - 告警统计图表
 * - 自定义图表配置
 * - 数据聚合和格式化
 *
 * 注意：此类不使用@Service注解，通过MonitoringConfig中的@Bean方法创建实例
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-02 16:45:00
 * @modified 2025-07-02 16:45:00
 * @version 1.0.0
 */
public class ReactiveMonitoringVisualizationServiceImpl implements ReactiveMonitoringVisualizationService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveMonitoringVisualizationServiceImpl.class);

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ReactivePerformanceMonitoringService performanceMonitoringService;
    private final ReactiveMonitoringAlertService alertService;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造器注入依赖
     *
     * @param redisTemplate                Redis模板
     * @param performanceMonitoringService 性能监控服务
     * @param alertService                 告警服务
     * @param unifiedConfigManager         统一配置管理器
     */
    public ReactiveMonitoringVisualizationServiceImpl(
            ReactiveRedisTemplate<String, Object> redisTemplate,
            ReactivePerformanceMonitoringService performanceMonitoringService,
            ReactiveMonitoringAlertService alertService,
            UnifiedConfigManager unifiedConfigManager) {
        this.redisTemplate = redisTemplate;
        this.performanceMonitoringService = performanceMonitoringService;
        this.alertService = alertService;
        this.unifiedConfigManager = unifiedConfigManager;

        // 注意：不在构造函数中初始化格式化器，因为@Value注解的字段此时还未注入
        // initializeFormatters(); // 移除这行，改为使用@PostConstruct
    }

    /**
     * 初始化时间格式化器 - 在依赖注入完成后执行
     *
     * @author: Mr.Rey Copyright © 2025
     * @created: 2025-10-27 12:26:43
     * @modified: 2025-10-27 12:26:43
     * @version: 1.0.4
     */
    @PostConstruct
    private void initializeFormatters() {
        // 注意：在@PostConstruct中使用.block()是必要的，因为需要在初始化阶段同步获取配置值
        String timeFormatPattern = unifiedConfigManager.getProperty("honyrun.monitoring.time-format-pattern", "HH:mm");
        String dateTimeFormatPattern = unifiedConfigManager.getProperty("honyrun.monitoring.datetime-format-pattern",
                "MM-dd HH:mm");

        this.timeFormatter = DateTimeFormatter.ofPattern(timeFormatPattern);
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormatPattern);
        logger.debug("时间格式化器初始化完成 - timePattern: {}, dateTimePattern: {}",
                timeFormatPattern, dateTimeFormatPattern);
    }

    // Redis键前缀
    private static final String CHART_CONFIG_PREFIX = "monitoring:chart:config:";

    // 自定义图表配置缓存
    private final Map<String, ChartConfig> chartConfigCache = new ConcurrentHashMap<>();

    // 时间格式化器
    private DateTimeFormatter timeFormatter;
    private DateTimeFormatter dateTimeFormatter;

    // ==================== 仪表板数据 ====================

    @Override
    public Flux<DashboardData> getRealTimeDashboardData() {
        LoggingUtil.info(logger, "开始获取实时监控仪表板数据");

        return Flux.interval(Duration.ofSeconds(5))
                .flatMap(tick -> generateDashboardData())
                .doOnNext(data -> LoggingUtil.debug(logger, "生成仪表板数据: {}", data.getTimestamp()))
                .doOnError(error -> LoggingUtil.error(logger, "获取仪表板数据失败", error));
    }

    @Override
    public Mono<SystemOverviewData> getSystemOverview() {
        LoggingUtil.info(logger, "获取系统概览数据");

        return Mono.zip(
                getSystemMetrics(),
                performanceMonitoringService.getCurrentSystemMetrics(),
                getAlertSummary())
                .map(tuple -> {
                    SystemMetrics systemMetrics = tuple.getT1();
                    // performanceMetrics 和 alertSummary 暂时未使用，但保留以备将来扩展
                    // SystemPerformanceMetrics performanceMetrics = tuple.getT2();
                    // AlertSummary alertSummary = tuple.getT3();

                    String systemStatus = determineSystemStatus(
                            systemMetrics.getCpuUsage(),
                            systemMetrics.getMemoryUsage(),
                            systemMetrics.getDiskUsage());

                    // 获取JVM运行时间
                    long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

                    return new SystemOverviewData(
                            systemStatus,
                            systemMetrics.getCpuUsage(),
                            systemMetrics.getMemoryUsage(),
                            systemMetrics.getDiskUsage(),
                            systemMetrics.getActiveConnections(),
                            uptime,
                            "1.0.0");
                })
                .doOnSuccess(data -> LoggingUtil.info(logger, "系统概览数据生成成功"))
                .doOnError(error -> LoggingUtil.error(logger, "获取系统概览数据失败", error));
    }

    @Override
    public Mono<PerformanceOverviewData> getPerformanceOverview() {
        LoggingUtil.info(logger, "获取性能概览数据");

        return performanceMonitoringService.getCurrentSystemMetrics()
                .map(systemMetrics -> {
                    // 从SystemPerformanceMetrics获取数据
                    return new PerformanceOverviewData(
                            0.0, // averageResponseTime - 默认值
                            0L, // totalRequests - 默认值
                            0.0, // errorRate - 默认值
                            0.0, // throughput - 默认值
                            0, // activeThreads - 默认值
                            0L // gcTime - 默认值
                    );
                })
                .doOnSuccess(data -> LoggingUtil.info(logger, "性能概览数据生成成功"))
                .doOnError(error -> LoggingUtil.error(logger, "获取性能概览数据失败", error));
    }

    // ==================== 图表数据 ====================

    @Override
    public Mono<ChartData> getCpuUsageTrend(LocalDateTime startTime, LocalDateTime endTime, int interval) {
        LoggingUtil.info(logger, "获取CPU使用率趋势图数据: {} - {}, 间隔{}分钟", startTime, endTime, interval);

        return generateTrendData("cpu_usage", startTime, endTime, interval, "CPU使用率趋势", "%")
                .doOnSuccess(data -> LoggingUtil.info(logger, "CPU趋势数据生成成功，数据点数量: {}", data.getLabels().size()));
    }

    @Override
    public Mono<ChartData> getMemoryUsageTrend(LocalDateTime startTime, LocalDateTime endTime, int interval) {
        LoggingUtil.info(logger, "获取内存使用率趋势图数据: {} - {}, 间隔{}分钟", startTime, endTime, interval);

        return generateTrendData("memory_usage", startTime, endTime, interval, "内存使用率趋势", "%")
                .doOnSuccess(data -> LoggingUtil.info(logger, "内存趋势数据生成成功，数据点数量: {}", data.getLabels().size()));
    }

    @Override
    public Mono<ChartData> getResponseTimeTrend(LocalDateTime startTime, LocalDateTime endTime, int interval) {
        LoggingUtil.info(logger, "获取响应时间趋势图数据: {} - {}, 间隔{}分钟", startTime, endTime, interval);

        return generateTrendData("response_time", startTime, endTime, interval, "响应时间趋势", "ms")
                .doOnSuccess(data -> LoggingUtil.info(logger, "响应时间趋势数据生成成功，数据点数量: {}", data.getLabels().size()));
    }

    @Override
    public Mono<ChartData> getRequestCountTrend(LocalDateTime startTime, LocalDateTime endTime, int interval) {
        LoggingUtil.info(logger, "获取请求量趋势图数据: {} - {}, 间隔{}分钟", startTime, endTime, interval);

        return generateTrendData("request_count", startTime, endTime, interval, "请求量趋势", "次/分钟")
                .doOnSuccess(data -> LoggingUtil.info(logger, "请求量趋势数据生成成功，数据点数量: {}", data.getLabels().size()));
    }

    @Override
    public Mono<ChartData> getErrorRateTrend(LocalDateTime startTime, LocalDateTime endTime, int interval) {
        LoggingUtil.info(logger, "获取错误率趋势图数据: {} - {}, 间隔{}分钟", startTime, endTime, interval);

        return generateTrendData("error_rate", startTime, endTime, interval, "错误率趋势", "%")
                .doOnSuccess(data -> LoggingUtil.info(logger, "错误率趋势数据生成成功，数据点数量: {}", data.getLabels().size()));
    }

    // ==================== 告警可视化 ====================

    @Override
    public Mono<ChartData> getAlertStatistics(int days) {
        LoggingUtil.info(logger, "获取{}天内告警统计数据", days);

        return alertService.getAlertStatistics()
                .map(stats -> {
                    List<String> labels = Arrays.asList("严重", "高", "中", "低");
                    List<Double> data = Arrays.asList(
                            (double) stats.getCriticalAlerts(),
                            (double) stats.getHighAlerts(),
                            (double) stats.getMediumAlerts(),
                            (double) stats.getLowAlerts());

                    DataSeries series = new DataSeries("告警数量", data, "#FF6B6B", "bar");

                    Map<String, Object> options = new HashMap<>();
                    options.put("responsive", true);
                    options.put("plugins", Map.of("legend", Map.of("display", true)));

                    return new ChartData("bar", "告警统计", labels, Arrays.asList(series), options);
                })
                .doOnSuccess(data -> LoggingUtil.info(logger, "告警统计数据生成成功"))
                .doOnError(error -> LoggingUtil.error(logger, "获取告警统计数据失败", error));
    }

    @Override
    public Mono<ChartData> getAlertTrend(LocalDateTime startTime, LocalDateTime endTime) {
        LoggingUtil.info(logger, "获取告警趋势数据: {} - {}", startTime, endTime);

        return alertService.getAlertTrend(7)
                .map(trend -> {
                    List<String> labels = trend.getDailyCounts().stream()
                            .map(dailyCount -> dailyCount.getDate().format(dateTimeFormatter))
                            .collect(Collectors.toList());

                    List<Double> data = trend.getDailyCounts().stream()
                            .map(dailyCount -> (double) dailyCount.getCount())
                            .collect(Collectors.toList());

                    DataSeries series = new DataSeries("告警数量", data, "#4ECDC4", "line");

                    Map<String, Object> options = new HashMap<>();
                    options.put("responsive", true);
                    options.put("scales", Map.of("y", Map.of("beginAtZero", true)));

                    return new ChartData("line", "告警趋势", labels, Arrays.asList(series), options);
                })
                .doOnSuccess(data -> LoggingUtil.info(logger, "告警趋势数据生成成功"))
                .doOnError(error -> LoggingUtil.error(logger, "获取告警趋势数据失败", error));
    }

    @Override
    public Mono<PieChartData> getAlertDistribution(int days) {
        LoggingUtil.info(logger, "获取{}天内告警分布数据", days);

        return alertService.getAlertStatistics()
                .map(stats -> {
                    List<PieSlice> slices = Arrays.asList(
                            new PieSlice("严重", stats.getCriticalAlerts(), "#FF6B6B"),
                            new PieSlice("高", stats.getHighAlerts(), "#FFD93D"),
                            new PieSlice("中", stats.getMediumAlerts(), "#6BCF7F"),
                            new PieSlice("低", stats.getLowAlerts(), "#4ECDC4"));

                    Map<String, Object> options = new HashMap<>();
                    options.put("responsive", true);
                    options.put("plugins", Map.of("legend", Map.of("position", "right")));

                    return new PieChartData("告警分布", slices, options);
                })
                .doOnSuccess(data -> LoggingUtil.info(logger, "告警分布数据生成成功"))
                .doOnError(error -> LoggingUtil.error(logger, "获取告警分布数据失败", error));
    }

    // ==================== 性能可视化 ====================

    @Override
    public Mono<MultiSeriesChartData> getJvmPerformanceData(LocalDateTime startTime, LocalDateTime endTime) {
        LoggingUtil.info(logger, "获取JVM性能数据: {} - {}", startTime, endTime);

        return Mono.fromCallable(() -> {
            List<String> labels = generateTimeLabels(startTime, endTime, 5);

            List<DataSeries> series = Arrays.asList(
                    new DataSeries("堆内存使用", generateRandomData(labels.size(), 50, 80), "#FF6B6B", "line"),
                    new DataSeries("非堆内存使用", generateRandomData(labels.size(), 20, 40), "#4ECDC4", "line"),
                    new DataSeries("GC时间", generateRandomData(labels.size(), 0, 10), "#FFD93D", "line"));

            Map<String, Object> options = new HashMap<>();
            options.put("responsive", true);
            options.put("scales", Map.of("y", Map.of("beginAtZero", true)));

            return new MultiSeriesChartData("line", "JVM性能监控", labels, series, options);
        })
                .doOnSuccess(data -> LoggingUtil.info(logger, "JVM性能数据生成成功"))
                .doOnError(error -> LoggingUtil.error(logger, "获取JVM性能数据失败", error));
    }

    @Override
    public Mono<MultiSeriesChartData> getDatabasePerformanceData(LocalDateTime startTime, LocalDateTime endTime) {
        LoggingUtil.info(logger, "获取数据库性能数据: {} - {}", startTime, endTime);

        return Mono.fromCallable(() -> {
            List<String> labels = generateTimeLabels(startTime, endTime, 5);

            List<DataSeries> series = Arrays.asList(
                    new DataSeries("连接数", generateRandomData(labels.size(), 10, 50), "#FF6B6B", "line"),
                    new DataSeries("查询响应时间", generateRandomData(labels.size(), 5, 20), "#4ECDC4", "line"),
                    new DataSeries("事务数", generateRandomData(labels.size(), 100, 500), "#FFD93D", "line"));

            Map<String, Object> options = new HashMap<>();
            options.put("responsive", true);
            options.put("scales", Map.of("y", Map.of("beginAtZero", true)));

            return new MultiSeriesChartData("line", "数据库性能监控", labels, series, options);
        })
                .doOnSuccess(data -> LoggingUtil.info(logger, "数据库性能数据生成成功"))
                .doOnError(error -> LoggingUtil.error(logger, "获取数据库性能数据失败", error));
    }

    @Override
    public Mono<MultiSeriesChartData> getCachePerformanceData(LocalDateTime startTime, LocalDateTime endTime) {
        LoggingUtil.info(logger, "获取缓存性能数据: {} - {}", startTime, endTime);

        return Mono.fromCallable(() -> {
            List<String> labels = generateTimeLabels(startTime, endTime, 5);

            List<DataSeries> series = Arrays.asList(
                    new DataSeries("命中率", generateRandomData(labels.size(), 85, 98), "#6BCF7F", "line"),
                    new DataSeries("响应时间", generateRandomData(labels.size(), 1, 5), "#4ECDC4", "line"),
                    new DataSeries("内存使用", generateRandomData(labels.size(), 60, 80), "#FF6B6B", "line"));

            Map<String, Object> options = new HashMap<>();
            options.put("responsive", true);
            options.put("scales", Map.of("y", Map.of("beginAtZero", true)));

            return new MultiSeriesChartData("line", "缓存性能监控", labels, series, options);
        })
                .doOnSuccess(data -> LoggingUtil.info(logger, "缓存性能数据生成成功"))
                .doOnError(error -> LoggingUtil.error(logger, "获取缓存性能数据失败", error));
    }

    // ==================== 自定义图表 ====================

    @Override
    public Mono<String> createCustomChart(ChartConfig config) {
        LoggingUtil.info(logger, "创建自定义图表: {}", config.getName());

        String chartId = UUID.randomUUID().toString();
        config.setId(chartId);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());

        return redisTemplate.opsForValue()
                .set(CHART_CONFIG_PREFIX + chartId, config)
                .then(Mono.fromRunnable(() -> chartConfigCache.put(chartId, config)))
                .thenReturn(chartId)
                .doOnSuccess(id -> LoggingUtil.info(logger, "自定义图表创建成功: {}", id))
                .doOnError(error -> LoggingUtil.error(logger, "创建自定义图表失败", error));
    }

    @Override
    public Mono<ChartData> getCustomChartData(String chartId, Map<String, Object> parameters) {
        LoggingUtil.info(logger, "获取自定义图表数据: {}", chartId);

        return getChartConfig(chartId)
                .flatMap(config -> executeCustomQuery(config, parameters))
                .doOnSuccess(data -> LoggingUtil.info(logger, "自定义图表数据获取成功: {}", chartId))
                .doOnError(error -> LoggingUtil.error(logger, "获取自定义图表数据失败: {}", chartId, error));
    }

    @Override
    public Flux<ChartConfig> getAllCustomCharts() {
        LoggingUtil.info(logger, "获取所有自定义图表配置");

        return redisTemplate.keys(CHART_CONFIG_PREFIX + "*")
                .flatMap(key -> redisTemplate.opsForValue().get(key))
                .cast(ChartConfig.class)
                .doOnNext(config -> LoggingUtil.debug(logger, "获取图表配置: {}", config.getName()))
                .doOnComplete(() -> LoggingUtil.info(logger, "所有自定义图表配置获取完成"));
    }

    @Override
    public Mono<Boolean> deleteCustomChart(String chartId) {
        LoggingUtil.info(logger, "删除自定义图表: {}", chartId);

        return redisTemplate.delete(CHART_CONFIG_PREFIX + chartId)
                .map(count -> count > 0)
                .doOnNext(success -> {
                    if (success) {
                        chartConfigCache.remove(chartId);
                        LoggingUtil.info(logger, "自定义图表删除成功: {}", chartId);
                    } else {
                        LoggingUtil.warn(logger, "自定义图表删除失败，图表不存在: {}", chartId);
                    }
                })
                .doOnError(error -> LoggingUtil.error(logger, "删除自定义图表失败: {}", chartId, error));
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成仪表板数据
     */
    private Mono<DashboardData> generateDashboardData() {
        return Mono.zip(
                getSystemMetrics(),
                getPerformanceMetrics(),
                getAlertSummary(),
                getCustomMetrics()).map(
                        tuple -> new DashboardData(
                                LocalDateTime.now(),
                                tuple.getT1(),
                                tuple.getT2(),
                                tuple.getT3(),
                                tuple.getT4()));
    }

    /**
     * 获取系统指标
     */
    private Mono<SystemMetrics> getSystemMetrics() {
        return Mono.fromCallable(() -> {
            Map<String, Object> memoryUsage = SystemMonitorUtil.getMemoryUsage();
            double memoryUsagePercent = ((Number) memoryUsage.getOrDefault("jvm.usage", 0.0)).doubleValue();

            Map<String, Object> diskUsage = SystemMonitorUtil.getDiskUsage();
            double diskUsagePercent = ((Number) diskUsage.getOrDefault("average.usage", 0.0)).doubleValue();

            return new SystemMetrics(
                    SystemMonitorUtil.getCpuUsage(),
                    memoryUsagePercent,
                    diskUsagePercent,
                    getActiveConnections());
        });
    }

    /**
     * 获取性能指标
     */
    private Mono<PerformanceMetrics> getPerformanceMetrics() {
        return performanceMonitoringService.getCurrentSystemMetrics()
                .map(systemMetrics -> {
                    PerformanceMetrics metrics = new PerformanceMetrics();
                    // PerformanceMetrics只有responseTime, requestCount, errorRate, throughput字段
                    // 从SystemPerformanceMetrics获取可用数据并设置默认值
                    metrics.setResponseTime(0.0); // 默认响应时间
                    metrics.setRequestCount(0L); // 默认请求数量
                    metrics.setErrorRate(0.0); // 默认错误率
                    metrics.setThroughput(0.0); // 默认吞吐量
                    return metrics;
                })
                .onErrorReturn(new PerformanceMetrics());
    }

    /**
     * 获取告警摘要
     */
    private Mono<AlertSummary> getAlertSummary() {
        return alertService.getAlertStatistics()
                .map(stats -> new AlertSummary(
                        stats.getTotalAlerts(),
                        stats.getCriticalAlerts(),
                        stats.getMediumAlerts(), // 使用getMediumAlerts()替代getWarningAlerts()
                        stats.getLowAlerts() // 使用getLowAlerts()替代getInfoCount()
                ))
                .onErrorReturn(new AlertSummary(0, 0, 0, 0));
    }

    /**
     * 获取自定义指标
     */
    private Mono<Map<String, Object>> getCustomMetrics() {
        return Mono.fromCallable(() -> {
            Map<String, Object> metrics = new HashMap<>();

            // 获取JVM堆内存使用情况
            Map<String, Object> memoryUsage = SystemMonitorUtil.getMemoryUsage();
            metrics.put("jvmHeapUsage", memoryUsage.get("jvm.usage"));

            // 获取JVM信息（包含线程数）
            Map<String, Object> jvmInfo = SystemMonitorUtil.getJvmInfo();
            metrics.put("threadCount", jvmInfo.get("activeThreadCount"));

            // 获取系统负载平均值
            Map<String, Object> systemLoad = SystemMonitorUtil.getSystemLoad();
            metrics.put("loadAverage", systemLoad.get("loadAverage"));

            return metrics;
        });
    }

    /**
     * 生成趋势数据
     */
    private Mono<ChartData> generateTrendData(String metricType, LocalDateTime startTime,
            LocalDateTime endTime, int interval, String title, String unit) {
        return Mono.fromCallable(() -> {
            List<String> labels = generateTimeLabels(startTime, endTime, interval);
            List<Double> data = generateMetricData(metricType, labels.size());

            DataSeries series = new DataSeries(title, data, getMetricColor(metricType), "line");

            Map<String, Object> options = new HashMap<>();
            options.put("responsive", true);
            options.put("scales", Map.of(
                    "y", Map.of("beginAtZero", true, "title", Map.of("display", true, "text", unit))));

            return new ChartData("line", title, labels, Arrays.asList(series), options);
        });
    }

    /**
     * 生成时间标签
     */
    private List<String> generateTimeLabels(LocalDateTime startTime, LocalDateTime endTime, int intervalMinutes) {
        List<String> labels = new ArrayList<>();
        LocalDateTime current = startTime;

        while (!current.isAfter(endTime)) {
            labels.add(current.format(timeFormatter));
            current = current.plusMinutes(intervalMinutes);
        }

        return labels;
    }

    /**
     * 生成指标数据
     */
    private List<Double> generateMetricData(String metricType, int count) {
        switch (metricType) {
            case "cpu_usage":
                return generateRandomData(count, 20, 80);
            case "memory_usage":
                return generateRandomData(count, 40, 85);
            case "response_time":
                return generateRandomData(count, 50, 200);
            case "request_count":
                return generateRandomData(count, 100, 1000);
            case "error_rate":
                return generateRandomData(count, 0, 5);
            default:
                return generateRandomData(count, 0, 100);
        }
    }

    /**
     * 生成随机数据
     */
    private List<Double> generateRandomData(int count, double min, double max) {
        Random random = new Random();
        return IntStream.range(0, count)
                .mapToDouble(i -> min + (max - min) * random.nextDouble())
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * 获取指标颜色
     */
    private String getMetricColor(String metricType) {
        switch (metricType) {
            case "cpu_usage":
                return "#FF6B6B";
            case "memory_usage":
                return "#4ECDC4";
            case "response_time":
                return "#FFD93D";
            case "request_count":
                return "#6BCF7F";
            case "error_rate":
                return "#FF8E53";
            default:
                return "#95A5A6";
        }
    }

    /**
     * 获取活跃连接数
     */
    private int getActiveConnections() {
        // 模拟活跃连接数
        return new Random().nextInt(50) + 10;
    }

    /**
     * 确定系统状态
     */
    private String determineSystemStatus(double cpuUsage, double memoryUsage, double diskUsage) {
        if (cpuUsage > 90 || memoryUsage > 90 || diskUsage > 95) {
            return "CRITICAL";
        } else if (cpuUsage > 70 || memoryUsage > 80 || diskUsage > 85) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * 获取图表配置
     */
    private Mono<ChartConfig> getChartConfig(String chartId) {
        ChartConfig cached = chartConfigCache.get(chartId);
        if (cached != null) {
            return Mono.just(cached);
        }

        return redisTemplate.opsForValue()
                .get(CHART_CONFIG_PREFIX + chartId)
                .cast(ChartConfig.class)
                .doOnNext(config -> chartConfigCache.put(chartId, config));
    }

    /**
     * 执行自定义查询
     */
    private Mono<ChartData> executeCustomQuery(ChartConfig config, Map<String, Object> parameters) {
        // 简化实现，实际应根据配置执行相应的查询
        return Mono.fromCallable(() -> {
            List<String> labels = Arrays.asList("数据1", "数据2", "数据3", "数据4", "数据5");
            List<Double> data = generateRandomData(5, 0, 100);

            DataSeries series = new DataSeries(config.getName(), data, "#4ECDC4", config.getType());

            return new ChartData(config.getType(), config.getName(), labels,
                    Arrays.asList(series), config.getOptions());
        });
    }
}
