package com.honyrun.scheduler;

import java.time.LocalDateTime;
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
 * 响应式缓存刷新定时任务
 *
 * 提供系统缓存刷新和维护功能，包括用户缓存、权限缓存、配置缓存等
 * 支持响应式异步执行，避免阻塞主线程
 *
 * 主要功能：
 * - 用户信息缓存刷新
 * - 权限数据缓存刷新
 * - 系统配置缓存刷新
 * - 业务数据缓存刷新
 * - 缓存预热和优化
 * - 缓存统计和监控
 *
 * 响应式特性：
 * - 非阻塞执行：缓存刷新异步执行，不影响系统正常运行
 * - 错误恢复：缓存刷新失败时提供错误恢复机制
 * - 流式处理：支持批量缓存数据的流式处理
 * - 背压控制：控制缓存刷新的执行速度
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 23:55:00
 * @modified 2025-07-01 23:55:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
@ConditionalOnProperty(name = "honyrun.scheduler.cache-refresh.enabled", havingValue = "true", matchIfMissing = true)
public class ReactiveCacheRefreshScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveCacheRefreshScheduler.class);

    private final ReactiveSystemService systemService;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入依赖
     *
     * @param systemService        响应式系统服务
     * @param unifiedConfigManager 统一配置管理器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public ReactiveCacheRefreshScheduler(ReactiveSystemService systemService,
            UnifiedConfigManager unifiedConfigManager) {
        this.systemService = systemService;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    // 系统启动时间
    private final LocalDateTime startTime = LocalDateTime.now();

    // 缓存刷新计数器
    private final AtomicLong cacheRefreshCount = new AtomicLong(0);
    private final AtomicLong userCacheRefreshCount = new AtomicLong(0);
    private final AtomicLong permissionCacheRefreshCount = new AtomicLong(0);
    private final AtomicLong configCacheRefreshCount = new AtomicLong(0);
    private final AtomicLong businessCacheRefreshCount = new AtomicLong(0);
    private final AtomicLong cachePreloadCount = new AtomicLong(0);

    // 最后执行时间
    private volatile LocalDateTime lastCacheRefreshTime;
    private volatile LocalDateTime lastUserCacheRefreshTime;
    private volatile LocalDateTime lastPermissionCacheRefreshTime;
    private volatile LocalDateTime lastConfigCacheRefreshTime;
    private volatile LocalDateTime lastBusinessCacheRefreshTime;
    private volatile LocalDateTime lastCachePreloadTime;

    // ==================== 缓存刷新任务 ====================

    /**
     * 综合缓存刷新任务
     * 每4小时执行一次，刷新所有类型的缓存
     */
    @Scheduled(fixedRate = 14400000) // 4小时
    public void comprehensiveCacheRefreshTask() {
        LoggingUtil.info(logger, "开始执行综合缓存刷新任务");

        executeComprehensiveCacheRefresh()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    cacheRefreshCount.incrementAndGet();
                    lastCacheRefreshTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "综合缓存刷新任务完成，累计执行次数: {}", cacheRefreshCount.get());
                })
                .doOnError(error -> LoggingUtil.error(logger, "综合缓存刷新任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "综合缓存刷新任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("COMPREHENSIVE_CACHE_REFRESH", "ERROR",
                            "综合缓存刷新任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 用户缓存刷新任务
     * 每2小时执行一次，刷新用户相关缓存
     */
    @Scheduled(fixedRate = 7200000) // 2小时
    public void userCacheRefreshTask() {
        unifiedConfigManager.getBooleanConfig("honyrun.cache.refresh.user-cache-enabled", true)
                .flatMap(userCacheEnabled -> {
                    if (!userCacheEnabled) {
                        LoggingUtil.debug(logger, "用户缓存刷新已禁用，跳过执行");
                        return Mono.empty();
                    }

                    LoggingUtil.info(logger, "开始执行用户缓存刷新任务");
                    return executeUserCacheRefresh();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    userCacheRefreshCount.incrementAndGet();
                    lastUserCacheRefreshTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "用户缓存刷新任务完成，累计执行次数: {}", userCacheRefreshCount.get());
                })
                .doOnError(error -> LoggingUtil.error(logger, "用户缓存刷新任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "用户缓存刷新任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("USER_CACHE_REFRESH", "ERROR",
                            "用户缓存刷新任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 权限缓存刷新任务
     * 每3小时执行一次，刷新权限相关缓存
     */
    @Scheduled(fixedRate = 10800000) // 3小时
    public void permissionCacheRefreshTask() {
        unifiedConfigManager.getBooleanConfig("honyrun.cache.refresh.permission-cache-enabled", true)
                .flatMap(permissionCacheEnabled -> {
                    if (!permissionCacheEnabled) {
                        LoggingUtil.debug(logger, "权限缓存刷新已禁用，跳过执行");
                        return Mono.empty();
                    }

                    LoggingUtil.info(logger, "开始执行权限缓存刷新任务");
                    return executePermissionCacheRefresh();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    permissionCacheRefreshCount.incrementAndGet();
                    lastPermissionCacheRefreshTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "权限缓存刷新任务完成，累计执行次数: {}", permissionCacheRefreshCount.get());
                })
                .doOnError(error -> LoggingUtil.error(logger, "权限缓存刷新任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "权限缓存刷新任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("PERMISSION_CACHE_REFRESH", "ERROR",
                            "权限缓存刷新任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 配置缓存刷新任务
     * 每1小时执行一次，刷新系统配置缓存
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void configCacheRefreshTask() {
        unifiedConfigManager.getBooleanConfig("honyrun.cache.refresh.config-cache-enabled", true)
                .flatMap(configCacheEnabled -> {
                    if (!configCacheEnabled) {
                        LoggingUtil.debug(logger, "配置缓存刷新已禁用，跳过执行");
                        return Mono.empty();
                    }

                    LoggingUtil.info(logger, "开始执行配置缓存刷新任务");
                    return executeConfigCacheRefresh();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    configCacheRefreshCount.incrementAndGet();
                    lastConfigCacheRefreshTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "配置缓存刷新任务完成，累计执行次数: {}", configCacheRefreshCount.get());
                })
                .doOnError(error -> LoggingUtil.error(logger, "配置缓存刷新任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "配置缓存刷新任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("CONFIG_CACHE_REFRESH", "ERROR",
                            "配置缓存刷新任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 业务缓存刷新任务
     * 每6小时执行一次，刷新业务数据缓存
     */
    @Scheduled(fixedRate = 21600000) // 6小时
    public void businessCacheRefreshTask() {
        unifiedConfigManager.getBooleanConfig("honyrun.cache.refresh.business-cache-enabled", true)
                .flatMap(businessCacheEnabled -> {
                    if (!businessCacheEnabled) {
                        LoggingUtil.debug(logger, "业务缓存刷新已禁用，跳过执行");
                        return Mono.empty();
                    }

                    LoggingUtil.info(logger, "开始执行业务缓存刷新任务");
                    return executeBusinessCacheRefresh();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    businessCacheRefreshCount.incrementAndGet();
                    lastBusinessCacheRefreshTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "业务缓存刷新任务完成，累计执行次数: {}", businessCacheRefreshCount.get());
                })
                .doOnError(error -> LoggingUtil.error(logger, "业务缓存刷新任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "业务缓存刷新任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("BUSINESS_CACHE_REFRESH", "ERROR",
                            "业务缓存刷新任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 缓存预热任务
     * 每天凌晨5点执行，预热常用缓存数据
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void cachePreloadTask() {
        unifiedConfigManager.getBooleanConfig("honyrun.cache.refresh.preload-enabled", true)
                .flatMap(preloadEnabled -> {
                    if (!preloadEnabled) {
                        LoggingUtil.debug(logger, "缓存预热已禁用，跳过执行");
                        return Mono.empty();
                    }

                    LoggingUtil.info(logger, "开始执行缓存预热任务");
                    return executeCachePreload();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    cachePreloadCount.incrementAndGet();
                    lastCachePreloadTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "缓存预热任务完成，累计执行次数: {}", cachePreloadCount.get());
                })
                .doOnError(error -> LoggingUtil.error(logger, "缓存预热任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "缓存预热任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("CACHE_PRELOAD", "ERROR",
                            "缓存预热任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    // ==================== 缓存刷新实现 ====================

    /**
     * 执行综合缓存刷新
     */
    private Mono<Void> executeComprehensiveCacheRefresh() {
        return Mono.fromRunnable(() -> LoggingUtil.info(logger, "开始综合缓存刷新"))
                .then(refreshUserCache())
                .then(refreshPermissionCache())
                .then(refreshConfigCache())
                .then(refreshBusinessCache())
                .then(performCacheOptimization())
                .then(Mono.fromRunnable(
                        () -> MonitoringLogUtil.logSystemEvent("COMPREHENSIVE_CACHE_REFRESH", "INFO", "综合缓存刷新完成")))
                .then(Mono.empty());
    }

    /**
     * 执行用户缓存刷新
     */
    private Mono<Void> executeUserCacheRefresh() {
        return refreshUserCache()
                .then(Mono.empty());
    }

    /**
     * 执行权限缓存刷新
     */
    private Mono<Void> executePermissionCacheRefresh() {
        return refreshPermissionCache()
                .then(Mono.empty());
    }

    /**
     * 执行配置缓存刷新
     */
    private Mono<Void> executeConfigCacheRefresh() {
        return refreshConfigCache()
                .then(Mono.empty());
    }

    /**
     * 执行业务缓存刷新
     */
    private Mono<Void> executeBusinessCacheRefresh() {
        return refreshBusinessCache()
                .then(Mono.empty());
    }

    /**
     * 执行缓存预热
     */
    private Mono<Void> executeCachePreload() {
        return Mono.fromRunnable(() -> LoggingUtil.info(logger, "开始缓存预热"))
                .then(preloadUserCache())
                .then(preloadPermissionCache())
                .then(preloadConfigCache())
                .then(preloadBusinessCache())
                .then(Mono.fromRunnable(() -> MonitoringLogUtil.logSystemEvent("CACHE_PRELOAD", "INFO", "缓存预热完成")))
                .then(Mono.empty());
    }

    /**
     * 刷新用户缓存
     */
    private Mono<Void> refreshUserCache() {
        LoggingUtil.info(logger, "刷新用户缓存");

        return unifiedConfigManager.getIntegerConfig("honyrun.cache.refresh.batch-size", 100)
                .flatMap(batchSize -> systemService.refreshUserCache(batchSize))
                .flatMap(refreshedCount -> {
                    LoggingUtil.info(logger, "用户缓存刷新完成，刷新数量: {}", refreshedCount);
                    return Mono.empty();
                })
                .then();
    }

    /**
     * 刷新权限缓存
     */
    private Mono<Void> refreshPermissionCache() {
        LoggingUtil.info(logger, "刷新权限缓存");

        return unifiedConfigManager.getIntegerConfig("honyrun.cache.refresh.batch-size", 100)
                .flatMap(batchSize -> systemService.refreshPermissionCache(batchSize))
                .flatMap(refreshedCount -> {
                    LoggingUtil.info(logger, "权限缓存刷新完成，刷新数量: {}", refreshedCount);
                    return Mono.empty();
                })
                .then();
    }

    /**
     * 刷新配置缓存
     */
    private Mono<Void> refreshConfigCache() {
        LoggingUtil.info(logger, "刷新配置缓存");

        return systemService.refreshConfigCache()
                .flatMap(refreshedCount -> {
                    LoggingUtil.info(logger, "配置缓存刷新完成，刷新数量: {}", refreshedCount);
                    return Mono.empty();
                })
                .then();
    }

    /**
     * 刷新业务缓存
     */
    private Mono<Void> refreshBusinessCache() {
        LoggingUtil.info(logger, "刷新业务缓存");

        return unifiedConfigManager.getIntegerConfig("honyrun.cache.refresh.batch-size", 100)
                .flatMap(batchSize -> systemService.refreshBusinessCache(batchSize))
                .flatMap(refreshedCount -> {
                    LoggingUtil.info(logger, "业务缓存刷新完成，刷新数量: {}", refreshedCount);
                    return Mono.empty();
                })
                .then();
    }

    /**
     * 预热用户缓存
     */
    private Mono<Void> preloadUserCache() {
        LoggingUtil.info(logger, "预热用户缓存");

        return unifiedConfigManager.getIntegerConfig("honyrun.cache.refresh.batch-size", 100)
                .flatMap(batchSize -> systemService.preloadUserCache(batchSize))
                .doOnSuccess(unused -> LoggingUtil.info(logger, "用户缓存预热完成"))
                .doOnError(error -> LoggingUtil.error(logger, "用户缓存预热失败", error))
                .then();
    }

    /**
     * 预热权限缓存
     */
    private Mono<Void> preloadPermissionCache() {
        LoggingUtil.info(logger, "预热权限缓存");

        return systemService.preloadPermissionCache()
                .doOnSuccess(unused -> LoggingUtil.info(logger, "权限缓存预热完成"))
                .doOnError(error -> LoggingUtil.error(logger, "权限缓存预热失败", error))
                .then();
    }

    /**
     * 预热配置缓存
     */
    private Mono<Void> preloadConfigCache() {
        LoggingUtil.info(logger, "预热配置缓存");

        return systemService.preloadConfigCache()
                .doOnSuccess(unused -> LoggingUtil.info(logger, "配置缓存预热完成"))
                .doOnError(error -> LoggingUtil.error(logger, "配置缓存预热失败", error))
                .then();
    }

    /**
     * 预热业务缓存
     */
    private Mono<Void> preloadBusinessCache() {
        LoggingUtil.info(logger, "预热业务缓存");

        return unifiedConfigManager.getIntegerConfig("honyrun.cache.refresh.batch-size", 100)
                .flatMap(batchSize -> systemService.preloadBusinessCache(batchSize))
                .doOnSuccess(unused -> LoggingUtil.info(logger, "业务缓存预热完成"))
                .doOnError(error -> LoggingUtil.error(logger, "业务缓存预热失败", error))
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

        // 获取当前配置值 - 使用响应式方式，避免阻塞调用
        // 使用默认值，避免在状态查询时阻塞
        boolean preloadEnabled = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.cache.refresh.preload-enabled", "true"));
        int batchSize = Integer.parseInt(unifiedConfigManager.getProperty("honyrun.cache.refresh.batch-size", "100"));
        boolean userCacheEnabled = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.cache.refresh.user-cache-enabled", "true"));
        boolean permissionCacheEnabled = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.cache.refresh.permission-cache-enabled", "true"));
        boolean configCacheEnabled = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.cache.refresh.config-cache-enabled", "true"));
        boolean businessCacheEnabled = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.cache.refresh.business-cache-enabled", "true"));

        status.put("startTime", startTime);
        status.put("uptime", java.time.Duration.between(startTime, LocalDateTime.now()).toMillis());
        status.put("cacheRefreshCount", cacheRefreshCount.get());
        status.put("userCacheRefreshCount", userCacheRefreshCount.get());
        status.put("permissionCacheRefreshCount", permissionCacheRefreshCount.get());
        status.put("configCacheRefreshCount", configCacheRefreshCount.get());
        status.put("businessCacheRefreshCount", businessCacheRefreshCount.get());
        status.put("cachePreloadCount", cachePreloadCount.get());
        status.put("lastCacheRefreshTime", lastCacheRefreshTime);
        status.put("lastUserCacheRefreshTime", lastUserCacheRefreshTime);
        status.put("lastPermissionCacheRefreshTime", lastPermissionCacheRefreshTime);
        status.put("lastConfigCacheRefreshTime", lastConfigCacheRefreshTime);
        status.put("lastBusinessCacheRefreshTime", lastBusinessCacheRefreshTime);
        status.put("lastCachePreloadTime", lastCachePreloadTime);
        status.put("userCacheEnabled", userCacheEnabled);
        status.put("permissionCacheEnabled", permissionCacheEnabled);
        status.put("configCacheEnabled", configCacheEnabled);
        status.put("businessCacheEnabled", businessCacheEnabled);
        status.put("preloadEnabled", preloadEnabled);
        status.put("batchSize", batchSize);
        return status;
    }

    /**
     * 重置缓存刷新计数器
     */
    public void resetCounters() {
        LoggingUtil.info(logger, "重置缓存刷新计数器");
        cacheRefreshCount.set(0);
        userCacheRefreshCount.set(0);
        permissionCacheRefreshCount.set(0);
        configCacheRefreshCount.set(0);
        businessCacheRefreshCount.set(0);
        cachePreloadCount.set(0);
    }
}
