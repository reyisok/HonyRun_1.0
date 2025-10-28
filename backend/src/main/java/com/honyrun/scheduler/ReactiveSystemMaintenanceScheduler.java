package com.honyrun.scheduler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.service.reactive.ReactiveSystemService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.MonitoringLogUtil;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 响应式系统维护定时任务
 *
 * 提供系统维护和清理功能，包括数据清理、缓存维护、系统优化等
 * 支持响应式异步执行，避免阻塞主线程
 *
 * 主要功能：
 * - 系统数据清理和维护
 * - 临时文件清理
 * - 数据库连接池维护
 * - 系统缓存优化
 * - 系统资源回收
 *
 * 响应式特性：
 * - 非阻塞执行：维护任务异步执行，不影响系统正常运行
 * - 错误恢复：维护失败时提供错误恢复机制
 * - 流式处理：支持批量数据的流式处理
 * - 背压控制：控制维护任务的执行速度
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 23:45:00
 * @modified 2025-07-01 23:45:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
@ConditionalOnProperty(name = "honyrun.scheduler.maintenance.enabled", havingValue = "true", matchIfMissing = true)
public class ReactiveSystemMaintenanceScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveSystemMaintenanceScheduler.class);

    private final ReactiveSystemService systemService;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入依赖
     *
     * @param systemService        响应式系统服务
     * @param unifiedConfigManager 统一配置管理器
     */
    public ReactiveSystemMaintenanceScheduler(ReactiveSystemService systemService,
            UnifiedConfigManager unifiedConfigManager) {
        this.systemService = systemService;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    // 系统启动时间
    private final LocalDateTime startTime = LocalDateTime.now();

    // 维护任务计数器
    private final AtomicLong maintenanceTaskCount = new AtomicLong(0);
    private final AtomicLong dataCleanupCount = new AtomicLong(0);
    private final AtomicLong tempFileCleanupCount = new AtomicLong(0);
    private final AtomicLong cacheMaintenanceCount = new AtomicLong(0);

    // 最后执行时间
    private volatile LocalDateTime lastMaintenanceTime;
    private volatile LocalDateTime lastDataCleanupTime;
    private volatile LocalDateTime lastTempFileCleanupTime;
    private volatile LocalDateTime lastCacheMaintenanceTime;

    // ==================== 系统维护任务 ====================

    /**
     * 系统综合维护任务
     * 每天凌晨3点执行，进行系统综合维护
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void systemMaintenanceTask() {
        LoggingUtil.info(logger, "开始执行系统综合维护任务");

        executeSystemMaintenance()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    maintenanceTaskCount.incrementAndGet();
                    lastMaintenanceTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "系统综合维护任务完成，累计执行次数: {}", maintenanceTaskCount.get());
                })
                .doOnError(error -> LoggingUtil.error(logger, "系统综合维护任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "系统维护任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("SYSTEM_MAINTENANCE", "ERROR",
                            "系统维护任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 数据清理任务
     * 每周日凌晨4点执行，清理过期数据
     */
    @Scheduled(cron = "0 0 4 * * SUN")
    public void dataCleanupTask() {
        LoggingUtil.info(logger, "开始执行数据清理任务");

        executeDataCleanup()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    dataCleanupCount.incrementAndGet();
                    lastDataCleanupTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "数据清理任务完成，累计执行次数: {}", dataCleanupCount.get());
                })
                .doOnError(error -> LoggingUtil.error(logger, "数据清理任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "数据清理任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("DATA_CLEANUP", "ERROR",
                            "数据清理任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 临时文件清理任务
     * 每天凌晨1点执行，清理临时文件
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void tempFileCleanupTask() {
        LoggingUtil.info(logger, "开始执行临时文件清理任务");

        executeTempFileCleanup()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    tempFileCleanupCount.incrementAndGet();
                    lastTempFileCleanupTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "临时文件清理任务完成，累计执行次数: {}", tempFileCleanupCount.get());
                })
                .doOnError(error -> LoggingUtil.error(logger, "临时文件清理任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "临时文件清理任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("TEMP_FILE_CLEANUP", "ERROR",
                            "临时文件清理任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 缓存维护任务
     * 每6小时执行一次，维护系统缓存
     */
    @Scheduled(fixedRate = 21600000) // 6小时
    public void cacheMaintenanceTask() {
        // 注意：在系统维护调度器初始化中使用同步方法是必要的，因为需要在Bean创建时确保配置已加载
        boolean cacheRefreshEnabled = Boolean
                .parseBoolean(unifiedConfigManager.getProperty("honyrun.maintenance.cache-refresh-enabled", "true"));

        if (!cacheRefreshEnabled) {
            LoggingUtil.debug(logger, "缓存维护任务已禁用，跳过执行");
            return;
        }

        LoggingUtil.info(logger, "开始执行缓存维护任务");

        executeCacheMaintenance()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    cacheMaintenanceCount.incrementAndGet();
                    lastCacheMaintenanceTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "缓存维护任务完成，累计执行次数: {}", cacheMaintenanceCount.get());
                })
                .doOnError(error -> LoggingUtil.error(logger, "缓存维护任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "缓存维护任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("CACHE_MAINTENANCE", "ERROR",
                            "缓存维护任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    // ==================== 维护任务实现 ====================

    /**
     * 执行系统综合维护
     */
    private Mono<Void> executeSystemMaintenance() {
        return Mono.fromRunnable(() -> LoggingUtil.info(logger, "开始系统综合维护"))
                .then(performDatabaseMaintenance())
                .then(performConnectionPoolMaintenance())
                .then(performSystemOptimization())
                .then(Mono
                        .fromRunnable(() -> MonitoringLogUtil.logSystemEvent("SYSTEM_MAINTENANCE", "INFO", "系统综合维护完成")))
                .then(Mono.empty());
    }

    /**
     * 执行数据清理
     */
    private Mono<Void> executeDataCleanup() {
        int dataCleanupDays = Integer
                .parseInt(unifiedConfigManager.getProperty("honyrun.maintenance.data-cleanup-days", "90"));
        LocalDateTime cleanupDate = LocalDateTime.now().minusDays(dataCleanupDays);

        return Mono.fromRunnable(() -> LoggingUtil.info(logger, "开始数据清理，清理日期: {}", cleanupDate))
                .then(systemService.cleanupExpiredData(cleanupDate))
                .flatMap(deletedCount -> {
                    LoggingUtil.info(logger, "数据清理完成，删除记录数: {}", deletedCount);
                    MonitoringLogUtil.logSystemEvent("DATA_CLEANUP", "INFO",
                            String.format("数据清理完成，删除%d条过期记录", deletedCount));
                    return Mono.empty();
                })
                .then();
    }

    /**
     * 执行临时文件清理
     */
    private Mono<Void> executeTempFileCleanup() {
        int tempFileCleanupDays = Integer
                .parseInt(unifiedConfigManager.getProperty("honyrun.maintenance.temp-file-cleanup-days", "7"));
        LocalDateTime cleanupDate = LocalDateTime.now().minusDays(tempFileCleanupDays);

        return Mono.fromRunnable(() -> LoggingUtil.info(logger, "开始临时文件清理，清理日期: {}", cleanupDate))
                .then(systemService.cleanupTempFiles(cleanupDate))
                .flatMap(deletedCount -> {
                    LoggingUtil.info(logger, "临时文件清理完成，删除文件数: {}", deletedCount);
                    MonitoringLogUtil.logSystemEvent("TEMP_FILE_CLEANUP", "INFO",
                            String.format("临时文件清理完成，删除%d个文件", deletedCount));
                    return Mono.empty();
                })
                .then();
    }

    /**
     * 执行缓存维护
     */
    private Mono<Void> executeCacheMaintenance() {
        return Mono.fromRunnable(() -> LoggingUtil.info(logger, "开始缓存维护"))
                .then(performCacheCleanup())
                .then(performCacheOptimization())
                .then(Mono.fromRunnable(() -> MonitoringLogUtil.logSystemEvent("CACHE_MAINTENANCE", "INFO", "缓存维护完成")))
                .then(Mono.empty());
    }

    /**
     * 执行数据库维护
     */
    private Mono<Void> performDatabaseMaintenance() {
        LoggingUtil.info(logger, "执行数据库维护");

        return systemService.performDatabaseMaintenance()
                .flatMap(result -> {
                    Map<String, Object> maintenanceResult = result;
                    LoggingUtil.info(logger, "数据库维护完成: {}", maintenanceResult);
                    return Mono.empty();
                })
                .doOnError(error -> LoggingUtil.error(logger, "数据库维护失败", error))
                .then();
    }

    /**
     * 执行连接池维护
     */
    private Mono<Void> performConnectionPoolMaintenance() {
        LoggingUtil.info(logger, "执行连接池维护");

        return systemService.collectR2dbcMetrics()
                .doOnSuccess(metrics -> LoggingUtil.info(logger, "连接池维护完成，收集到指标: {}", metrics))
                .doOnError(error -> LoggingUtil.error(logger, "连接池维护失败", error))
                .then();
    }

    /**
     * 执行系统优化
     */
    private Mono<Void> performSystemOptimization() {
        LoggingUtil.info(logger, "执行系统优化");

        return systemService.validateSystemConfiguration()
                .doOnSuccess(unused -> LoggingUtil.info(logger, "系统优化完成"))
                .doOnError(error -> LoggingUtil.error(logger, "系统优化失败", error))
                .then();
    }

    /**
     * 执行缓存清理
     */
    private Mono<Void> performCacheCleanup() {
        LoggingUtil.info(logger, "执行缓存清理");

        return systemService.performCacheOptimization()
                .doOnSuccess(unused -> LoggingUtil.info(logger, "缓存清理完成"))
                .doOnError(error -> LoggingUtil.error(logger, "缓存清理失败", error))
                .then();
    }

    /**
     * 执行缓存优化
     */
    private Mono<Void> performCacheOptimization() {
        LoggingUtil.info(logger, "执行缓存优化");

        return systemService.performCacheOptimization()
                .doOnSuccess(unused -> LoggingUtil.info(logger, "缓存优化完成"))
                .doOnError(error -> LoggingUtil.error(logger, "缓存优化失败", error))
                .then();
    }

    // ==================== 辅助方法 ====================

    // 已移除createSystemLog和createErrorLog方法，现在使用MonitoringLogUtil进行文件日志记录

    // ==================== 监控状态查询方法 ====================

    /**
     * 获取调度器运行状态
     */
    public java.util.Map<String, Object> getSchedulerStatus() {
        java.util.Map<String, Object> status = new java.util.HashMap<>();
        status.put("startTime", startTime);
        status.put("uptime", java.time.Duration.between(startTime, LocalDateTime.now()).toMillis());
        status.put("maintenanceTaskCount", maintenanceTaskCount.get());
        status.put("dataCleanupCount", dataCleanupCount.get());
        status.put("tempFileCleanupCount", tempFileCleanupCount.get());
        status.put("cacheMaintenanceCount", cacheMaintenanceCount.get());
        status.put("lastMaintenanceTime", lastMaintenanceTime);
        status.put("lastDataCleanupTime", lastDataCleanupTime);
        status.put("lastTempFileCleanupTime", lastTempFileCleanupTime);
        status.put("lastCacheMaintenanceTime", lastCacheMaintenanceTime);

        // 使用默认值，避免在状态查询时阻塞
        int tempFileCleanupDays = Integer
                .parseInt(unifiedConfigManager.getProperty("honyrun.maintenance.temp-file-cleanup-days", "7"));
        int dataCleanupDays = Integer
                .parseInt(unifiedConfigManager.getProperty("honyrun.maintenance.data-cleanup-days", "90"));
        boolean cacheRefreshEnabled = Boolean
                .parseBoolean(unifiedConfigManager.getProperty("honyrun.maintenance.cache-refresh-enabled", "true"));

        status.put("tempFileCleanupDays", tempFileCleanupDays);
        status.put("dataCleanupDays", dataCleanupDays);
        status.put("cacheRefreshEnabled", cacheRefreshEnabled);
        return status;
    }

    /**
     * 重置维护计数器
     */
    public void resetCounters() {
        LoggingUtil.info(logger, "重置维护计数器");
        maintenanceTaskCount.set(0);
        dataCleanupCount.set(0);
        tempFileCleanupCount.set(0);
        cacheMaintenanceCount.set(0);
    }
}
