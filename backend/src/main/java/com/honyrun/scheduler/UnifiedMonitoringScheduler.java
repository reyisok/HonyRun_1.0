package com.honyrun.scheduler;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.honyrun.config.monitoring.UnifiedMonitoringConfig;
import com.honyrun.service.monitoring.ReactiveBackpressureControlService;
import com.honyrun.service.monitoring.ReactiveDynamicSchedulingService;
import com.honyrun.service.reactive.ReactiveSystemService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.MonitoringLogUtil;
import com.honyrun.util.system.SystemMonitorUtil;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

/**
 * 统一监控调度器
 *
 * 整合原有的ReactivePerformanceMonitorScheduler和ReactiveSystemMonitorScheduler，
 * 消除重复的监控任务，提供统一的监控调度管理。
 *
 * 主要功能：
 * - 系统性能监控（CPU、内存、磁盘、网络）
 * - 应用性能监控（响应时间、错误率、吞吐量）
 * - 数据库性能监控（R2DBC连接池、查询性能）
 * - 缓存性能监控（Redis连接、命中率）
 * - 健康检查和告警
 * - 日志清理和维护
 *
 * 技术特性：
 * - 响应式编程：基于Reactor实现异步处理
 * - 统一配置：使用UnifiedMonitoringConfig统一管理配置
 * - 背压控制：防止监控任务过载
 * - 错误恢复：监控失败时提供错误恢复机制
 * - 真实数据：使用SystemMonitorUtil获取真实系统数据
 *
 * @author Mr.Rey
 * @created 2025-07-02 当前时间
 * @modified 2025-07-02 当前时间
 * @version 1.0.0
 *          Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
@ConditionalOnProperty(name = "honyrun.monitoring.task.enabled", havingValue = "true", matchIfMissing = true)
public class UnifiedMonitoringScheduler {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedMonitoringScheduler.class);

    private final ReactiveSystemService systemService;
    private final UnifiedMonitoringConfig unifiedMonitoringConfig;
    private final Scheduler monitoringScheduler;
    private final ReactiveBackpressureControlService backpressureControlService;
    private final ReactiveDynamicSchedulingService dynamicSchedulingService;
    private final com.honyrun.service.monitoring.ReactiveMetricsAggregationService metricsAggregationService;

    /**
     * 构造函数注入
     *
     * @param systemService              响应式系统服务
     * @param unifiedMonitoringConfig    统一监控配置
     * @param monitoringScheduler        监控调度器
     * @param backpressureControlService 响应式背压控制服务
     * @param dynamicSchedulingService   响应式动态调度服务
     * @param metricsAggregationService  响应式指标聚合服务
     */
    public UnifiedMonitoringScheduler(ReactiveSystemService systemService,
            UnifiedMonitoringConfig unifiedMonitoringConfig,
            Scheduler monitoringScheduler,
            ReactiveBackpressureControlService backpressureControlService,
            ReactiveDynamicSchedulingService dynamicSchedulingService,
            com.honyrun.service.monitoring.ReactiveMetricsAggregationService metricsAggregationService) {
        this.systemService = systemService;
        this.unifiedMonitoringConfig = unifiedMonitoringConfig;
        this.monitoringScheduler = monitoringScheduler;
        this.backpressureControlService = backpressureControlService;
        this.dynamicSchedulingService = dynamicSchedulingService;
        this.metricsAggregationService = metricsAggregationService;
    }

    // JVM管理Bean
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    // 系统启动时间
    private final LocalDateTime startTime = LocalDateTime.now();

    // 监控计数器
    private final AtomicLong performanceMonitorCount = new AtomicLong(0);
    private final AtomicLong healthCheckCount = new AtomicLong(0);
    private final AtomicLong resourceMonitorCount = new AtomicLong(0);
    private final AtomicLong alertCount = new AtomicLong(0);

    // 最后执行时间记录
    private volatile LocalDateTime lastPerformanceMonitorTime;
    private volatile LocalDateTime lastHealthCheckTime;
    private volatile LocalDateTime lastResourceMonitorTime;
    private volatile LocalDateTime lastLogCleanupTime;
    private volatile LocalDateTime lastUptimeDisplayTime;

    // 启动状态管理
    private volatile boolean startupValidationCompleted = false;
    private volatile LocalDateTime startupCompletionTime;
    private volatile Map<String, Object> startupBaselineMetrics;

    /**
     * 初始化监控调度器
     */
    @PostConstruct
    public void initialize() {
        LoggingUtil.info(logger, "初始化统一监控调度器");

        // 验证监控配置的一致性
        validateMonitoringConfiguration();

        LoggingUtil.info(logger, "监控配置 - 性能监控: {}, 系统监控: {}, WebFlux监控: {}, R2DBC监控: {}, Redis监控: {}",
                unifiedMonitoringConfig.getTask().isPerformanceEnabled(),
                unifiedMonitoringConfig.getTask().isSystemEnabled(),
                unifiedMonitoringConfig.getTask().isWebfluxEnabled(),
                unifiedMonitoringConfig.getTask().isR2dbcEnabled(),
                unifiedMonitoringConfig.getTask().isRedisEnabled());
    }

    /**
     * 验证监控配置的一致性
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 当前时间
     * @modified 2025-07-02 当前时间
     * @version 1.0.0
     */
    private void validateMonitoringConfiguration() {
        if (unifiedMonitoringConfig != null) {
            LoggingUtil.info(logger, "验证监控配置一致性");

            // 检查配置是否启用
            if (unifiedMonitoringConfig.isEnabled()) {
                LoggingUtil.info(logger, "监控配置已启用，验证通过");
            } else {
                LoggingUtil.warn(logger, "监控配置未启用，部分监控功能可能受限");
            }

            // 验证配置完整性
            if (unifiedMonitoringConfig.getSchedule() != null) {
                LoggingUtil.debug(logger, "监控调度配置验证通过");
            } else {
                LoggingUtil.warn(logger, "监控调度配置缺失，使用默认配置");
            }
        } else {
            LoggingUtil.warn(logger, "监控配置对象为空，使用默认配置");
        }
    }

    // ==================== 启动协作接口 ====================

    /**
     * 接收启动验证完成通知
     *
     * <p>
     * <strong>协作机制：</strong>
     * <ul>
     * <li>接收来自EssentialComponentsValidator的启动完成通知</li>
     * <li>记录启动完成时间和基线指标</li>
     * <li>启用基于启动状态的监控任务优化</li>
     * </ul>
     *
     * @param validationResults 启动验证结果，包含MySQL和Redis连接状态
     * @param baselineMetrics   基线指标数据，用于后续监控对比
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 当前时间
     * @modified 2025-07-02 当前时间
     * @version 1.0.0
     */
    public void onStartupValidationCompleted(Map<String, Object> validationResults,
            Map<String, Object> baselineMetrics) {
        try {
            LoggingUtil.info(logger, "接收到启动验证完成通知");

            this.startupValidationCompleted = true;
            this.startupCompletionTime = LocalDateTime.now();
            this.startupBaselineMetrics = new HashMap<>(baselineMetrics != null ? baselineMetrics : new HashMap<>());

            // 记录验证结果
            if (validationResults != null) {
                LoggingUtil.info(logger, "启动验证结果: MySQL连接={}, Redis连接={}",
                        validationResults.getOrDefault("mysql", "未知"),
                        validationResults.getOrDefault("redis", "未知"));
            }

            // 记录基线指标
            if (baselineMetrics != null && !baselineMetrics.isEmpty()) {
                LoggingUtil.info(logger, "已记录启动基线指标，指标数量: {}", baselineMetrics.size());
                baselineMetrics.forEach((key, value) -> LoggingUtil.debug(logger, "基线指标 - {}: {}", key, value));
            }

            LoggingUtil.info(logger, "统一监控调度器已接收启动完成通知，开始优化监控策略");

        } catch (Exception e) {
            LoggingUtil.error(logger, "处理启动验证完成通知时发生异常", e);
        }
    }

    /**
     * 获取启动状态信息
     *
     * @return 启动状态信息，包含完成状态、完成时间和基线指标
     */
    public Map<String, Object> getStartupStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("completed", startupValidationCompleted);
        status.put("completionTime", startupCompletionTime);
        status.put("baselineMetricsCount", startupBaselineMetrics != null ? startupBaselineMetrics.size() : 0);
        return status;
    }

    // ==================== 综合性能监控任务 ====================

    /**
     * 综合性能监控任务
     * 每5分钟执行一次，收集所有性能指标
     *
     * <p>
     * <strong>启动状态优化：</strong>
     * <ul>
     * <li>启动验证完成前：降低监控频率，避免干扰启动过程</li>
     * <li>启动验证完成后：正常执行监控，使用基线指标进行对比分析</li>
     * </ul>
     */
    @Scheduled(fixedRateString = "#{${honyrun.monitoring.schedule.comprehensive-monitor-interval:300000}}")
    public void comprehensivePerformanceMonitorTask() {
        if (!unifiedMonitoringConfig.getTask().isPerformanceEnabled()) {
            return;
        }

        // 启动状态检查：如果启动验证未完成，延迟执行监控任务
        if (!startupValidationCompleted) {
            LoggingUtil.debug(logger, "启动验证未完成，跳过综合性能监控任务");
            return;
        }

        LoggingUtil.info(logger, "开始执行综合性能监控任务（启动验证已完成）");

        executeComprehensivePerformanceMonitor()
                .subscribeOn(monitoringScheduler)
                .timeout(Duration.ofSeconds(unifiedMonitoringConfig.getTimeout().getComprehensiveMonitor()))
                .doOnSuccess(unused -> {
                    performanceMonitorCount.incrementAndGet();
                    lastPerformanceMonitorTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "综合性能监控任务完成，累计执行次数: {}", performanceMonitorCount.get());

                    // 触发指标聚合
                    metricsAggregationService.aggregateByTimeWindow(
                            "performance_metrics",
                            Duration.ofMinutes(5),
                            "avg")
                            .doOnNext(report -> LoggingUtil.info(logger, "指标聚合完成，聚合指标数量: {}", report.getMetricName()))
                            .doOnError(error -> LoggingUtil.error(logger, "指标聚合失败", error))
                            .subscribe();
                })
                .doOnError(TimeoutException.class, error -> {
                    LoggingUtil.error(logger, "综合性能监控任务超时，超时时间: {}秒",
                            unifiedMonitoringConfig.getTimeout().getComprehensiveMonitor());
                    MonitoringLogUtil.logSystemAlert("COMPREHENSIVE_PERFORMANCE_MONITOR", "TIMEOUT",
                            "综合性能监控任务超时，超时时间: " + unifiedMonitoringConfig.getTimeout().getComprehensiveMonitor() + "秒",
                            null);
                })
                .doOnError(error -> LoggingUtil.error(logger, "综合性能监控任务执行失败", error))
                .onErrorResume(error -> {
                    if (error instanceof TimeoutException) {
                        LoggingUtil.error(logger, "综合性能监控任务超时", error);
                        MonitoringLogUtil.logSystemAlert("COMPREHENSIVE_PERFORMANCE_MONITOR", "TIMEOUT",
                                "综合性能监控任务超时: " + error.getMessage(), null);
                    } else {
                        LoggingUtil.error(logger, "综合性能监控任务执行失败", error);
                        MonitoringLogUtil.logSystemAlert("COMPREHENSIVE_PERFORMANCE_MONITOR", "ERROR",
                                "综合性能监控任务执行失败: " + error.getMessage(), null);
                    }
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 系统健康检查任务
     * 每2分钟执行一次，检查系统健康状态
     *
     * <p>
     * <strong>启动状态优化：</strong>
     * <ul>
     * <li>启动验证完成前：仅执行基础健康检查</li>
     * <li>启动验证完成后：执行完整健康检查，包括基线对比</li>
     * </ul>
     */
    @Scheduled(fixedRateString = "#{${honyrun.monitoring.schedule.health-check-interval:120000}}")
    public void healthCheckTask() {
        if (!unifiedMonitoringConfig.getTask().isSystemEnabled()) {
            return;
        }

        // 启动状态检查：启动验证完成前执行简化的健康检查
        if (!startupValidationCompleted) {
            LoggingUtil.debug(logger, "启动验证未完成，执行简化健康检查");
            // 可以在这里执行简化的健康检查逻辑
            return;
        }

        LoggingUtil.info(logger, "开始执行系统健康检查任务（启动验证已完成）");

        Mono<Void> healthCheckTask = systemService.getHealthCheck()
                .subscribeOn(monitoringScheduler)
                .timeout(Duration.ofSeconds(unifiedMonitoringConfig.getTimeout().getHealthCheck()))
                .flatMap(this::processHealthCheckResult);

        backpressureControlService.executeWithBackpressure(healthCheckTask, "HEALTH_CHECK")
                .doOnSuccess(unused -> {
                    healthCheckCount.incrementAndGet();
                    lastHealthCheckTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "系统健康检查任务完成，累计执行次数: {}", healthCheckCount.get());
                })
                .doOnError(TimeoutException.class, error -> {
                    LoggingUtil.error(logger, "系统健康检查任务超时，超时时间: {}秒",
                            unifiedMonitoringConfig.getTimeout().getHealthCheck());
                    MonitoringLogUtil.logSystemAlert("HEALTH_CHECK", "TIMEOUT",
                            "健康检查任务超时，超时时间: " + unifiedMonitoringConfig.getTimeout().getHealthCheck() + "秒", null);
                })
                .doOnError(error -> LoggingUtil.error(logger, "系统健康检查任务执行失败", error))
                .onErrorResume(error -> {
                    if (error instanceof TimeoutException) {
                        LoggingUtil.error(logger, "健康检查任务超时", error);
                        MonitoringLogUtil.logSystemAlert("HEALTH_CHECK", "TIMEOUT",
                                "健康检查任务超时: " + error.getMessage(), null);
                    } else {
                        LoggingUtil.error(logger, "健康检查任务执行失败", error);
                        MonitoringLogUtil.logSystemAlert("HEALTH_CHECK", "ERROR",
                                "健康检查任务执行失败: " + error.getMessage(), null);
                    }
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 资源使用监控任务
     * 每3分钟执行一次，监控系统资源使用情况
     *
     * <p>
     * <strong>启动状态优化：</strong>
     * <ul>
     * <li>启动验证完成前：跳过资源监控，避免干扰启动过程</li>
     * <li>启动验证完成后：执行完整资源监控，包括基线对比</li>
     * </ul>
     */
    @Scheduled(fixedRateString = "#{${honyrun.monitoring.schedule.resource-monitor-interval:180000}}")
    public void resourceMonitorTask() {
        if (!unifiedMonitoringConfig.getTask().isSystemResourceEnabled()) {
            return;
        }

        // 启动状态检查：启动验证完成前跳过资源监控
        if (!startupValidationCompleted) {
            LoggingUtil.debug(logger, "启动验证未完成，跳过资源使用监控任务");
            return;
        }

        LoggingUtil.info(logger, "开始执行资源使用监控任务（启动验证已完成）");

        Mono<Map<String, Object>> resourceTask = Mono.fromCallable(this::collectResourceMetrics)
                .subscribeOn(monitoringScheduler)
                .timeout(Duration.ofSeconds(unifiedMonitoringConfig.getTimeout().getResourceMonitor()))
                .flatMap(this::processResourceMetrics)
                .then(Mono.empty());

        backpressureControlService.executeWithBackpressure(resourceTask, "RESOURCE_MONITOR")
                .doOnSuccess(unused -> {
                    resourceMonitorCount.incrementAndGet();
                    lastResourceMonitorTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "资源使用监控任务完成，累计执行次数: {}", resourceMonitorCount.get());
                })
                .doOnError(TimeoutException.class, error -> {
                    LoggingUtil.error(logger, "资源使用监控任务超时，超时时间: {}秒",
                            unifiedMonitoringConfig.getTimeout().getResourceMonitor());
                    MonitoringLogUtil.logSystemAlert("RESOURCE_MONITOR", "TIMEOUT",
                            "资源监控任务超时，超时时间: " + unifiedMonitoringConfig.getTimeout().getResourceMonitor() + "秒", null);
                })
                .doOnError(error -> LoggingUtil.error(logger, "资源使用监控任务执行失败", error))
                .onErrorResume(error -> {
                    if (error instanceof TimeoutException) {
                        LoggingUtil.error(logger, "资源监控任务超时", error);
                        MonitoringLogUtil.logSystemAlert("RESOURCE_MONITOR", "TIMEOUT",
                                "资源监控任务超时: " + error.getMessage(), null);
                    } else {
                        LoggingUtil.error(logger, "资源监控任务执行失败", error);
                        MonitoringLogUtil.logSystemAlert("RESOURCE_MONITOR", "ERROR",
                                "资源监控任务执行失败: " + error.getMessage(), null);
                    }
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 日志归档任务
     * 每日凌晨2点执行，归档过期日志
     *
     * <p>
     * <strong>启动状态优化：</strong>
     * <ul>
     * <li>启动验证完成前：跳过日志归档，避免影响启动性能</li>
     * <li>启动验证完成后：正常执行日志归档任务</li>
     * </ul>
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void logArchiveTask() {
        // 启动状态检查：启动验证完成前跳过日志归档
        if (!startupValidationCompleted) {
            LoggingUtil.debug(logger, "启动验证未完成，跳过日志归档任务");
            return;
        }

        LoggingUtil.info(logger, "开始执行日志归档任务（启动验证已完成）");

        Mono.fromRunnable(() -> {
            // 执行统一日志归档任务，启用压缩
            systemService.performUnifiedLogArchive(true)
                    .subscribe(result -> {
                        LoggingUtil.info(logger, "日志归档任务完成，归档结果: {}", result);
                    });
        })
                .subscribeOn(monitoringScheduler)
                .timeout(Duration.ofMinutes(15))
                .doOnSuccess(unused -> {
                    lastLogCleanupTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "日志归档任务完成，已压缩归档30天前的日志");
                })
                .doOnError(error -> LoggingUtil.error(logger, "日志归档任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "日志归档任务失败", error);
                    MonitoringLogUtil.logSystemAlert("LOG_ARCHIVE", "ERROR",
                            "日志归档任务失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    // ==================== 私有方法 ====================

    /**
     * 执行综合性能监控
     */
    private Mono<Void> executeComprehensivePerformanceMonitor() {
        return Mono.fromCallable(this::collectAllPerformanceMetrics)
                .flatMap(this::processPerformanceMetrics)
                .then();
    }

    /**
     * 收集所有性能指标
     */
    private Map<String, Object> collectAllPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // 使用SystemMonitorUtil获取真实系统数据
            metrics.put("cpu_usage", SystemMonitorUtil.getCpuUsage());
            metrics.put("memory_usage", SystemMonitorUtil.getMemoryUsage());
            metrics.put("disk_usage", SystemMonitorUtil.getDiskUsage());
            metrics.put("system_load", SystemMonitorUtil.getSystemLoad());

            // JVM指标
            long usedMemory = memoryMXBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryMXBean.getHeapMemoryUsage().getMax();
            metrics.put("jvm_memory_used", usedMemory);
            metrics.put("jvm_memory_max", maxMemory);
            metrics.put("jvm_memory_usage_percent", maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0);
            metrics.put("jvm_uptime", runtimeMXBean.getUptime());

            // 线程指标
            metrics.put("thread_count", ManagementFactory.getThreadMXBean().getThreadCount());
            metrics.put("daemon_thread_count", ManagementFactory.getThreadMXBean().getDaemonThreadCount());

            metrics.put("timestamp", LocalDateTime.now());

            LoggingUtil.debug(logger, "收集性能指标完成，指标数量: {}", metrics.size());
        } catch (Exception e) {
            LoggingUtil.error(logger, "收集性能指标失败", e);
            throw new RuntimeException("收集性能指标失败", e);
        }

        return metrics;
    }

    /**
     * 收集资源指标
     */
    private Map<String, Object> collectResourceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // 使用SystemMonitorUtil获取真实系统资源数据
            Map<String, Object> systemResources = SystemMonitorUtil.getSystemResourceUsage();
            metrics.putAll(systemResources);

            metrics.put("timestamp", LocalDateTime.now());

            LoggingUtil.debug(logger, "收集资源指标完成，指标数量: {}", metrics.size());
        } catch (Exception e) {
            LoggingUtil.error(logger, "收集资源指标失败", e);
            throw new RuntimeException("收集资源指标失败", e);
        }

        return metrics;
    }

    /**
     * 处理性能指标
     */
    private Mono<Void> processPerformanceMetrics(Map<String, Object> metrics) {
        return Mono.fromRunnable(() -> {
            try {
                // 记录性能指标到监控日志
                Map<String, Object> performanceMetrics = new HashMap<>();
                performanceMetrics.put("cpu_usage", metrics.get("cpu_usage"));
                performanceMetrics.put("jvm_memory_used", metrics.get("jvm_memory_used"));
                performanceMetrics.put("thread_count", metrics.get("thread_count"));
                performanceMetrics.put("system_load", metrics.get("system_load"));
                MonitoringLogUtil.logPerformanceMetrics("COMPREHENSIVE_PERFORMANCE", performanceMetrics);

                // 检查性能阈值
                checkPerformanceThresholds(metrics);

                LoggingUtil.debug(logger, "性能指标处理完成");
            } catch (Exception e) {
                LoggingUtil.error(logger, "处理性能指标失败", e);
                throw new RuntimeException("处理性能指标失败", e);
            }
        });
    }

    /**
     * 处理资源指标
     */
    private Mono<Void> processResourceMetrics(Map<String, Object> metrics) {
        return Mono.fromRunnable(() -> {
            try {
                // 记录资源指标到监控日志
                Map<String, Object> resourceUsageData = new HashMap<>();
                resourceUsageData.put("cpu_usage", (Double) metrics.getOrDefault("cpu_usage", 0.0));
                resourceUsageData.put("memory_usage", (Double) metrics.getOrDefault("memory_usage", 0.0));
                resourceUsageData.put("disk_usage", (Double) metrics.getOrDefault("disk_usage", 0.0));
                MonitoringLogUtil.logResourceUsage("SYSTEM_RESOURCE", resourceUsageData);

                // 检查资源阈值
                checkResourceThresholds(metrics);

                LoggingUtil.debug(logger, "资源指标处理完成");
            } catch (Exception e) {
                LoggingUtil.error(logger, "处理资源指标失败", e);
                throw new RuntimeException("处理资源指标失败", e);
            }
        });
    }

    /**
     * 处理健康检查结果
     */
    private Mono<Void> processHealthCheckResult(Map<String, Object> healthData) {
        return Mono.fromRunnable(() -> {
            try {
                String status = (String) healthData.getOrDefault("status", "UNKNOWN");
                // 记录健康检查结果
                Map<String, Object> healthDetails = new HashMap<>();
                healthDetails.put("status", status);
                healthDetails.put("timestamp", System.currentTimeMillis());
                MonitoringLogUtil.logHealthCheck("SYSTEM", status, healthDetails);

                if (!"UP".equals(status)) {
                    alertCount.incrementAndGet();
                    MonitoringLogUtil.logSystemAlert("HEALTH_CHECK", "WARNING",
                            "系统健康检查异常，状态: " + status, healthData);
                }

                LoggingUtil.debug(logger, "健康检查结果处理完成，状态: {}", status);
            } catch (Exception e) {
                LoggingUtil.error(logger, "处理健康检查结果失败", e);
                throw new RuntimeException("处理健康检查结果失败", e);
            }
        });
    }

    /**
     * 检查性能阈值
     */
    private void checkPerformanceThresholds(Map<String, Object> metrics) {
        try {
            Double cpuUsage = (Double) metrics.get("cpu_usage");
            Double memoryUsagePercent = (Double) metrics.get("jvm_memory_usage_percent");

            if (cpuUsage != null && cpuUsage > unifiedMonitoringConfig.getThreshold().getCpu()) {
                alertCount.incrementAndGet();
                MonitoringLogUtil.logPerformanceAlert("CPU_USAGE", unifiedMonitoringConfig.getThreshold().getCpu(),
                        cpuUsage, "CPU使用率超过阈值");
            }

            if (memoryUsagePercent != null && memoryUsagePercent > unifiedMonitoringConfig.getThreshold().getMemory()) {
                alertCount.incrementAndGet();
                MonitoringLogUtil.logPerformanceAlert("MEMORY_USAGE",
                        unifiedMonitoringConfig.getThreshold().getMemory(),
                        memoryUsagePercent, "内存使用率超过阈值");
            }
        } catch (Exception e) {
            LoggingUtil.error(logger, "检查性能阈值失败", e);
        }
    }

    /**
     * 检查资源阈值
     */
    private void checkResourceThresholds(Map<String, Object> metrics) {
        try {
            Double diskUsage = (Double) metrics.get("disk_usage");

            if (diskUsage != null && diskUsage > unifiedMonitoringConfig.getThreshold().getDisk()) {
                alertCount.incrementAndGet();
                MonitoringLogUtil.logPerformanceAlert("DISK_USAGE", unifiedMonitoringConfig.getThreshold().getDisk(),
                        diskUsage, "磁盘使用率超过阈值");
            }
        } catch (Exception e) {
            LoggingUtil.error(logger, "检查资源阈值失败", e);
        }
    }

    // ==================== 系统运行时间显示任务 ====================

    /**
     * 系统运行时间显示任务
     * 每30秒执行一次，显示系统运行时间（格式：hh:mm:ss）
     *
     * <p>
     * <strong>启动状态优化：</strong>
     * <ul>
     * <li>启动验证完成前：显示启动进度信息</li>
     * <li>启动验证完成后：显示正常运行时间</li>
     * </ul>
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 当前时间
     * @modified 2025-10-24 16:01:03
     * @version 1.1.0 - 修复调度配置，使用配置化的fixedRateString
     */
    @Scheduled(fixedRateString = "#{${honyrun.monitoring.schedule.uptime-display-interval:30000}}")
    public void systemUptimeDisplayTask() {
        LoggingUtil.debug(logger, "开始执行系统运行时间显示任务");

        try {
            // 计算运行时间
            Duration uptime = Duration.between(startTime, LocalDateTime.now());

            // 格式化为 hh:mm:ss
            long hours = uptime.toHours();
            long minutes = uptime.toMinutesPart();
            long seconds = uptime.toSecondsPart();

            String uptimeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            // 根据启动状态显示不同信息
            if (!startupValidationCompleted) {
                LoggingUtil.info(logger, "====================系统启动中，已运行时间: {} (等待基础组件验证完成)", uptimeFormatted);
            } else {
                LoggingUtil.info(logger, "====================系统已运行时间: {} (启动验证已完成)", uptimeFormatted);
                // 启动完成后，检查是否需要动态调整调度策略
                checkAndAdjustSchedulingStrategy(uptime);
            }

            // 更新最后执行时间
            lastUptimeDisplayTime = LocalDateTime.now();

        } catch (Exception error) {
            LoggingUtil.error(logger, "系统运行时间显示任务执行失败", error);
        }
    }

    /**
     * 检查并调整调度策略
     *
     * @param uptime 系统运行时间
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 当前时间
     * @modified 2025-07-02 当前时间
     * @version 1.0.0
     */
    private void checkAndAdjustSchedulingStrategy(Duration uptime) {
        if (dynamicSchedulingService != null) {
            // 根据系统运行时间和负载情况动态调整调度策略
            long uptimeMinutes = uptime.toMinutes();

            // 每小时检查一次调度策略
            if (uptimeMinutes > 0 && uptimeMinutes % 60 == 0) {
                LoggingUtil.debug(logger, "检查动态调度策略，系统已运行 {} 分钟", uptimeMinutes);

                dynamicSchedulingService.adjustSchedulingStrategy()
                        .doOnSuccess(strategy -> LoggingUtil.info(logger, "动态调度策略调整完成: {}", strategy))
                        .doOnError(error -> LoggingUtil.error(logger, "动态调度策略调整失败", error))
                        .subscribe();
            }
        }
    }

    // ==================== 监控状态查询方法 ====================

    /**
     * 获取监控统计信息
     */
    public Map<String, Object> getMonitoringStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("start_time", startTime);
        stats.put("performance_monitor_count", performanceMonitorCount.get());
        stats.put("health_check_count", healthCheckCount.get());
        stats.put("resource_monitor_count", resourceMonitorCount.get());
        stats.put("alert_count", alertCount.get());
        stats.put("last_performance_monitor_time", lastPerformanceMonitorTime);
        stats.put("last_health_check_time", lastHealthCheckTime);
        stats.put("last_resource_monitor_time", lastResourceMonitorTime);
        stats.put("last_log_cleanup_time", lastLogCleanupTime);
        stats.put("last_uptime_display_time", lastUptimeDisplayTime);
        return stats;
    }
}
