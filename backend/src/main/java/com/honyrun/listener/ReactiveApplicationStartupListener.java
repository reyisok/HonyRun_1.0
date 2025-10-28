package com.honyrun.listener;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.honyrun.config.cache.RedisConnectionHealthChecker;
import com.honyrun.service.cache.CacheConsistencyService;
import com.honyrun.service.reactive.ReactiveSystemService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.MonitoringLogUtil;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 响应式应用启动监听器
 *
 * 监听Spring Boot应用启动完成事件，执行系统初始化任务
 * 支持响应式异步处理，避免阻塞应用启动过程
 *
 * 主要功能：
 * - 监听应用启动完成事件
 * - 执行系统初始化检查
 * - 记录应用启动日志
 * - 初始化系统状态信息
 * - 执行启动后的健康检查
 * - 记录系统启动完成标志和耗时统计
 *
 * 响应式特性：
 * - 非阻塞处理：启动任务异步执行，不阻塞应用启动
 * - 错误恢复：启动失败时提供错误恢复机制
 * - 流式处理：支持多个初始化任务的流式组合
 * - 背压控制：控制初始化任务的执行速度
 *
 * @author Mr.Rey
 * @version 2.1.0
 * @created 2025-07-01 23:30:00
 * @modified 2025-07-02 当前时间
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class ReactiveApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveApplicationStartupListener.class);

    private final ReactiveSystemService systemService;
    private final CacheConsistencyService cacheConsistencyService;
    private final RedisConnectionHealthChecker redisHealthChecker;

    /**
     * 构造函数注入
     *
     * @param systemService           响应式系统服务
     * @param cacheConsistencyService 缓存一致性服务
     * @param redisHealthChecker      Redis连接健康检查器
     */
    public ReactiveApplicationStartupListener(ReactiveSystemService systemService,
            CacheConsistencyService cacheConsistencyService,
            RedisConnectionHealthChecker redisHealthChecker) {
        this.systemService = systemService;
        this.cacheConsistencyService = cacheConsistencyService;
        this.redisHealthChecker = redisHealthChecker;
    }

    /**
     * 启动完成标志
     */
    private final AtomicBoolean startupCompleted = new AtomicBoolean(false);

    /**
     * 应用启动时间
     */
    private volatile LocalDateTime startupTime;

    /**
     * 系统启动开始时间（毫秒）
     */
    private static final long SYSTEM_START_TIME = System.currentTimeMillis();

    /**
     * 处理应用启动完成事件
     *
     * @param event 应用启动完成事件
     */
    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        LoggingUtil.info(logger, "ReactiveApplicationStartupListener 收到 ApplicationReadyEvent 事件");

        if (startupCompleted.compareAndSet(false, true)) {
            startupTime = LocalDateTime.now();

            LoggingUtil.info(logger, "应用启动完成，开始执行初始化任务");

            executeStartupTasks()
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnSubscribe(subscription -> {
                        LoggingUtil.info(logger, "开始订阅启动任务执行流");
                    })
                    .doOnSuccess(unused -> {
                        // 在所有初始化任务完成后计算启动耗时
                        long startupDuration = System.currentTimeMillis() - SYSTEM_START_TIME;
                        LoggingUtil.info(logger, "启动任务执行成功，准备记录系统启动完成标志");
                        // 记录系统启动完成标志
                        logSystemStartupComplete(startupDuration);
                        LoggingUtil.info(logger, "应用启动初始化任务完成");
                    })
                    .doOnError(error -> {
                        LoggingUtil.error(logger, "应用启动初始化任务执行失败", error);
                        LoggingUtil.error(logger, "错误详情: {}", error.getMessage());
                    })
                    .onErrorResume(error -> {
                        LoggingUtil.error(logger, "进入错误恢复流程");
                        // 即使出错也要计算启动耗时
                        long startupDuration = System.currentTimeMillis() - SYSTEM_START_TIME;
                        return handleStartupError(error, startupDuration);
                    })
                    .subscribe(
                            unused -> LoggingUtil.info(logger, "启动任务订阅完成"),
                            error -> LoggingUtil.error(logger, "启动任务订阅失败", error));
        } else {
            LoggingUtil.warn(logger, "ReactiveApplicationStartupListener 已经执行过，跳过重复执行");
        }
    }

    /**
     * 记录系统启动完成标志
     *
     * @param startupDuration 启动耗时（毫秒）
     */
    private void logSystemStartupComplete(long startupDuration) {
        String formattedDuration = formatDuration(startupDuration);

        // 记录到应用日志
        LoggingUtil.info(logger, "=== 系统启动完成 =========================");
        LoggingUtil.info(logger, "启动耗时: {} ({}ms)", formattedDuration, startupDuration);
        LoggingUtil.info(logger, "启动时间: {}", startupTime);
        LoggingUtil.info(logger, "系统状态: 初始化完成，等待第一次完整监控统计");
        LoggingUtil.info(logger, "========================================");

        // 记录到监控日志
        MonitoringLogUtil.logStartupEvent("APPLICATION_READY", startupDuration,
                String.format("系统启动完成，耗时: %s", formattedDuration));

        // 记录到系统事件日志
        MonitoringLogUtil.logSystemEvent("SYSTEM_STARTUP", "COMPLETED",
                String.format("系统启动完成，启动时间: %s，耗时: %s (%dms)",
                        startupTime, formattedDuration, startupDuration));
    }

    /**
     * 格式化持续时间
     *
     * @param durationMs 持续时间（毫秒）
     * @return 格式化的持续时间字符串
     */
    private String formatDuration(long durationMs) {
        if (durationMs < 1000) {
            return durationMs + "ms";
        } else if (durationMs < 60000) {
            return String.format("%.2fs", durationMs / 1000.0);
        } else {
            long minutes = durationMs / 60000;
            long seconds = (durationMs % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }

    /**
     * 执行启动任务
     * 优化：使用并行执行提高启动效率，同时保持任务间的依赖关系
     *
     * @return 启动任务执行结果
     */
    private Mono<Void> executeStartupTasks() {
        LoggingUtil.info(logger, "进入 executeStartupTasks 方法");

        return Mono.fromRunnable(() -> {
            LoggingUtil.info(logger, "开始执行应用启动初始化任务");
            MonitoringLogUtil.logStartupEvent("INITIALIZATION_START", 0, "开始执行启动初始化任务");
        })
                .doOnNext(unused -> LoggingUtil.info(logger, "初始化日志记录完成，开始并行执行启动任务"))
                // 并行执行独立的启动任务以提高效率
                .then(Mono.zip(
                        warmupConfigCache().doOnNext(unused -> LoggingUtil.info(logger, "配置缓存预热完成")),
                        recordStartupEvent().doOnNext(unused -> LoggingUtil.info(logger, "启动事件记录完成")),
                        performSystemHealthCheck().doOnNext(unused -> LoggingUtil.info(logger, "系统健康检查完成"))).then())
                .doOnNext(unused -> LoggingUtil.info(logger, "并行任务完成，开始执行依赖任务"))
                // 执行依赖于前面任务的后续任务
                .then(initializeSystemStatus())
                .doOnNext(unused -> LoggingUtil.info(logger, "系统状态初始化完成，开始验证系统配置"))
                .then(validateSystemConfiguration())
                .doOnNext(unused -> LoggingUtil.info(logger, "系统配置验证完成，准备完成所有启动任务"))
                .then(Mono.fromRunnable(() -> {
                    LoggingUtil.info(logger, "所有启动初始化任务执行完成");
                    MonitoringLogUtil.logStartupEvent("INITIALIZATION_COMPLETE", 0, "所有启动初始化任务执行完成");
                }))
                .then()
                .doOnError(error -> LoggingUtil.error(logger, "executeStartupTasks 执行过程中出现错误", error));
    }

    /**
     * 记录启动事件
     *
     * @return 记录结果
     */
    private Mono<Void> recordStartupEvent() {
        LoggingUtil.info(logger, "记录应用启动事件");

        return systemService.recordSystemEvent(
                "APPLICATION_STARTUP",
                "INFO",
                "STARTUP",
                String.format("应用启动完成，启动时间: %s", startupTime)).then();
    }

    /**
     * 执行系统健康检查
     *
     * @return 健康检查结果
     */
    private Mono<Void> performSystemHealthCheck() {
        LoggingUtil.info(logger, "执行启动后系统健康检查");

        return systemService.getHealthCheck()
                .flatMap(healthCheck -> {
                    String status = (String) healthCheck.get("status");
                    LoggingUtil.info(logger, "系统健康状态: {}", status);

                    if ("HEALTHY".equals(status)) {
                        MonitoringLogUtil.logHealthCheck("SYSTEM", "UP", healthCheck);
                        return systemService.recordSystemEvent(
                                "HEALTH_CHECK",
                                "INFO",
                                "STARTUP",
                                "启动后健康检查通过");
                    } else {
                        String details = String.valueOf(healthCheck);
                        LoggingUtil.warn(logger, "系统健康检查异常: {}", details);
                        MonitoringLogUtil.logHealthCheck("SYSTEM", "DOWN", healthCheck);
                        return systemService.recordSystemEvent(
                                "HEALTH_CHECK",
                                "WARN",
                                "STARTUP",
                                "启动后健康检查异常: " + details);
                    }
                })
                .then();
    }

    /**
     * 初始化系统状态
     *
     * @return 初始化结果
     */
    private Mono<Void> initializeSystemStatus() {
        LoggingUtil.info(logger, "初始化系统状态信息");

        return systemService.initializeSystemStatus()
                .doOnSuccess(unused -> {
                    LoggingUtil.info(logger, "系统状态初始化完成");
                    MonitoringLogUtil.logSystemEvent("SYSTEM_STATUS", "INITIALIZED", "系统状态初始化完成");
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "系统状态初始化失败", error);
                    MonitoringLogUtil.logSystemEvent("SYSTEM_STATUS", "INITIALIZATION_FAILED",
                            "系统状态初始化失败: " + error.getMessage());
                })
                .then();
    }

    /**
     * 验证系统配置
     *
     * @return 验证结果
     */
    private Mono<Void> validateSystemConfiguration() {
        LoggingUtil.info(logger, "验证系统配置");

        return systemService.validateSystemConfiguration()
                .then(systemService.recordSystemEvent(
                        "CONFIG_VALIDATION",
                        "INFO",
                        "STARTUP",
                        "系统配置验证通过"))
                .then()
                .doOnSuccess(unused -> {
                    LoggingUtil.info(logger, "系统配置验证通过");
                    MonitoringLogUtil.logSystemEvent("CONFIG_VALIDATION", "PASSED", "系统配置验证通过");
                })
                .doOnError(error -> {
                    LoggingUtil.warn(logger, "系统配置验证失败: {}", error.getMessage());
                    MonitoringLogUtil.logSystemEvent("CONFIG_VALIDATION", "FAILED",
                            "系统配置验证失败: " + error.getMessage());
                    systemService.recordSystemEvent(
                            "CONFIG_VALIDATION",
                            "WARN",
                            "STARTUP",
                            "系统配置验证失败: " + error.getMessage()).subscribe();
                });
    }

    /**
     * 预热配置缓存 - 优化版本
     * 使用批量预热提高效率
     *
     * @return 预热结果
     */
    private Mono<Void> warmupConfigCache() {
        LoggingUtil.info(logger, "开始预热配置缓存，首先等待Redis连接就绪");

        // 首先等待Redis连接就绪，然后再执行缓存预热
        return redisHealthChecker.waitForRedisReady()
                .flatMap(redisReady -> {
                    if (redisReady) {
                        LoggingUtil.info(logger, "Redis连接已就绪，开始执行缓存预热");
                        // 使用批量预热功能，提高启动效率
                        return cacheConsistencyService.warmupAllCaches()
                                .doOnSuccess(unused -> {
                                    LoggingUtil.info(logger, "配置缓存预热完成");
                                    MonitoringLogUtil.logSystemEvent("CONFIG_CACHE", "WARMUP_COMPLETE", "配置缓存预热完成");
                                })
                                .doOnError(error -> {
                                    LoggingUtil.warn(logger, "配置缓存预热失败: {}", error.getMessage());
                                    MonitoringLogUtil.logSystemEvent("CONFIG_CACHE", "WARMUP_FAILED",
                                            "配置缓存预热失败: " + error.getMessage());
                                    systemService.recordSystemEvent(
                                            "CONFIG_CACHE_WARMUP",
                                            "WARN",
                                            "STARTUP",
                                            "配置缓存预热失败: " + error.getMessage()).subscribe();
                                })
                                .onErrorResume(error -> Mono.empty()); // 预热失败不影响启动
                    } else {
                        LoggingUtil.warn(logger, "Redis连接未就绪，跳过缓存预热");
                        MonitoringLogUtil.logSystemEvent("CONFIG_CACHE", "WARMUP_SKIPPED", "Redis连接未就绪，跳过缓存预热");
                        return Mono.empty();
                    }
                })
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "Redis连接检查失败，跳过缓存预热: {}", error.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * 处理启动错误
     *
     * @param error           启动错误
     * @param startupDuration 启动耗时
     * @return 错误处理结果
     */
    private Mono<Void> handleStartupError(Throwable error, long startupDuration) {
        LoggingUtil.error(logger, "应用启动初始化任务执行失败", error);

        // 记录启动失败事件
        MonitoringLogUtil.logStartupEvent("INITIALIZATION_FAILED", startupDuration,
                "应用启动初始化失败: " + error.getMessage());

        return systemService.recordSystemEvent(
                "STARTUP_ERROR",
                "ERROR",
                "STARTUP",
                "应用启动初始化失败: " + error.getMessage()).then();
    }

    // ==================== 状态查询方法 ====================

    /**
     * 检查启动是否完成
     *
     * @return true-启动完成，false-未完成
     */
    public boolean isStartupCompleted() {
        return startupCompleted.get();
    }

    /**
     * 获取应用启动时间
     *
     * @return 启动时间
     */
    public LocalDateTime getStartupTime() {
        return startupTime;
    }

    /**
     * 获取应用运行时长（毫秒）
     *
     * @return 运行时长
     */
    public long getUptimeMillis() {
        if (startupTime == null) {
            return 0;
        }
        return java.time.Duration.between(startupTime, LocalDateTime.now()).toMillis();
    }

    /**
     * 获取格式化的运行时长
     *
     * @return 格式化运行时长
     */
    public String getFormattedUptime() {
        long uptimeMillis = getUptimeMillis();
        if (uptimeMillis == 0) {
            return "未启动";
        }

        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%d天%d小时%d分钟", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    /**
     * 获取系统启动耗时（毫秒）
     *
     * @return 启动耗时
     */
    public long getStartupDuration() {
        if (startupTime == null) {
            return 0;
        }
        return System.currentTimeMillis() - SYSTEM_START_TIME;
    }
}
