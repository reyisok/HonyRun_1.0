package com.honyrun.listener;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.service.reactive.ReactiveSystemService;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 响应式系统事件监听器
 *
 * 监听系统级事件，处理系统状态变更和异常情况
 * 支持响应式异步处理，确保系统事件的及时响应
 *
 * 主要功能：
 * - 监听应用上下文刷新事件
 * - 监听应用关闭事件
 * - 监听应用启动失败事件
 * - 处理系统状态变更
 * - 记录系统事件日志
 *
 * 响应式特性：
 * - 非阻塞处理：系统事件处理异步执行，不影响系统性能
 * - 错误恢复：事件处理失败时提供错误恢复机制
 * - 流式处理：支持系统事件的流式处理和分析
 * - 背压控制：控制系统事件处理的速度，防止系统过载
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 23:40:00
 * @modified 2025-07-01 23:40:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class ReactiveSystemEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveSystemEventListener.class);

    private final ReactiveSystemService systemService;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入
     *
     * @param systemService        响应式系统服务
     * @param unifiedConfigManager 统一配置管理器
     */
    public ReactiveSystemEventListener(ReactiveSystemService systemService, UnifiedConfigManager unifiedConfigManager) {
        this.systemService = systemService;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 系统事件计数器
     */
    private final AtomicLong systemEventCount = new AtomicLong(0);

    /**
     * 上下文刷新计数器
     */
    private final AtomicLong contextRefreshCount = new AtomicLong(0);

    /**
     * 系统关闭计数器
     */
    private final AtomicLong systemShutdownCount = new AtomicLong(0);

    /**
     * 最后事件时间
     */
    private volatile LocalDateTime lastEventTime;

    /**
     * 监听应用上下文刷新事件
     *
     * @param event 上下文刷新事件
     */
    @EventListener
    public void handleContextRefreshedEvent(ContextRefreshedEvent event) {
        LoggingUtil.info(logger, "应用上下文刷新事件触发");

        processSystemEvent("CONTEXT_REFRESHED", "应用上下文刷新完成", "INFO")
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    contextRefreshCount.incrementAndGet();
                    LoggingUtil.info(logger, "应用上下文刷新事件处理完成，累计次数: {}", contextRefreshCount.get());
                })
                .doOnError(error -> LoggingUtil.error(logger, "应用上下文刷新事件处理失败", error))
                .onErrorResume(error -> handleSystemEventError("CONTEXT_REFRESHED", error))
                .subscribe();
    }

    /**
     * 监听应用关闭事件
     *
     * @param event 应用关闭事件
     */
    @EventListener
    public void handleContextClosedEvent(ContextClosedEvent event) {
        LoggingUtil.info(logger, "应用关闭事件触发");

        processSystemEvent("CONTEXT_CLOSED", "应用正在关闭", "WARN")
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    systemShutdownCount.incrementAndGet();
                    LoggingUtil.info(logger, "应用关闭事件处理完成，累计次数: {}", systemShutdownCount.get());
                })
                .doOnError(error -> LoggingUtil.error(logger, "应用关闭事件处理失败", error))
                .onErrorResume(error -> handleSystemEventError("CONTEXT_CLOSED", error))
                .subscribe();
    }

    /**
     * 监听应用启动失败事件
     *
     * @param event 应用启动失败事件
     */
    @EventListener
    public void handleApplicationFailedEvent(ApplicationFailedEvent event) {
        LoggingUtil.error(logger, "应用启动失败事件触发", event.getException());

        String errorMessage = String.format("应用启动失败: %s",
                event.getException() != null ? event.getException().getMessage() : "未知错误");

        processSystemEvent("APPLICATION_FAILED", errorMessage, "ERROR")
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> LoggingUtil.info(logger, "应用启动失败事件处理完成"))
                .doOnError(error -> LoggingUtil.error(logger, "应用启动失败事件处理失败", error))
                .onErrorResume(error -> handleSystemEventError("APPLICATION_FAILED", error))
                .subscribe();
    }

    /**
     * 处理系统事件
     *
     * @param eventType    事件类型
     * @param eventMessage 事件消息
     * @param logLevel     日志级别
     * @return 处理结果
     */
    private Mono<Void> processSystemEvent(String eventType, String eventMessage, String logLevel) {
        return unifiedConfigManager.getBooleanConfig("honyrun.startup.r2dbc.enabled", false)
                .flatMap(startupR2dbcEnabled -> {
                    return Mono.fromRunnable(() -> LoggingUtil.info(logger, "开始处理系统事件，类型: {}", eventType))
                            .then(startupR2dbcEnabled ? recordSystemEvent(eventType, eventMessage, logLevel)
                                    : Mono.empty())
                            .then(startupR2dbcEnabled ? updateSystemStatus(eventType) : Mono.empty())
                            .then(checkSystemHealth(eventType))
                            .then(Mono.fromRunnable(() -> {
                                systemEventCount.incrementAndGet();
                                lastEventTime = LocalDateTime.now();
                                LoggingUtil.info(logger, "系统事件处理完成，类型: {}, 累计事件数: {}", eventType,
                                        systemEventCount.get());
                            }));
                });
    }

    /**
     * 记录系统事件
     *
     * @param eventType    事件类型
     * @param eventMessage 事件消息
     * @param logLevel     日志级别
     * @return 记录结果
     */
    private Mono<Void> recordSystemEvent(String eventType, String eventMessage, String logLevel) {
        LoggingUtil.info(logger, "记录系统事件，类型: {}, 消息: {}", eventType, eventMessage);

        return systemService.recordSystemEvent(
                eventType,
                logLevel,
                "SYSTEM",
                eventMessage).then();
    }

    /**
     * 更新系统状态
     *
     * @param eventType 事件类型
     * @return 更新结果
     */
    private Mono<Void> updateSystemStatus(String eventType) {
        LoggingUtil.info(logger, "更新系统状态，事件类型: {}", eventType);

        return systemService.updateSystemStatusByEvent(eventType)
                .doOnSuccess(unused -> LoggingUtil.info(logger, "系统状态更新完成，事件类型: {}", eventType))
                .doOnError(error -> LoggingUtil.error(logger, "系统状态更新失败，事件类型: " + eventType, error))
                .then();
    }

    /**
     * 检查系统健康状态
     *
     * @param eventType 事件类型
     * @return 检查结果
     */
    private Mono<Void> checkSystemHealth(String eventType) {
        LoggingUtil.info(logger, "检查系统健康状态，事件类型: {}", eventType);

        // 对于关键系统事件，执行健康检查
        if (isHealthCheckRequired(eventType)) {
            return unifiedConfigManager.getBooleanConfig("honyrun.startup.r2dbc.enabled", false)
                    .flatMap(startupR2dbcEnabled -> {
                        // 在启动期禁用涉及数据库的健康检查，防止无DB导致超时
                        if (!startupR2dbcEnabled) {
                            LoggingUtil.warn(logger, "启动期禁用数据库健康检查，开关未开启: honyrun.startup.r2dbc.enabled=false");
                            return Mono.empty();
                        }
                        return systemService.getHealthCheck()
                                .flatMap(healthStatus -> {
                                    String status = (String) healthStatus.get("status");
                                    LoggingUtil.info(logger, "系统健康检查完成，状态: {}", status);

                                    if (!"HEALTHY".equals(status)) {
                                        LoggingUtil.warn(logger, "系统健康检查异常，详情: {}", healthStatus);

                                        return systemService.recordSystemEvent(
                                                "HEALTH_CHECK_WARNING",
                                                "WARN",
                                                "HEALTH",
                                                String.format("系统健康检查异常 - 事件: %s, 状态: %s, 详情: %s",
                                                        eventType, status, healthStatus));
                                    }
                                    return Mono.empty();
                                })
                                .then();
                    });
        }

        return Mono.empty();
    }

    /**
     * 判断是否需要健康检查
     *
     * @param eventType 事件类型
     * @return true-需要检查，false-不需要检查
     */
    private boolean isHealthCheckRequired(String eventType) {
        return "CONTEXT_REFRESHED".equals(eventType) ||
                "APPLICATION_FAILED".equals(eventType) ||
                "CONTEXT_CLOSED".equals(eventType);
    }

    /**
     * 处理系统事件错误
     *
     * @param eventType 事件类型
     * @param error     错误信息
     * @return 错误处理结果
     */
    private Mono<Void> handleSystemEventError(String eventType, Throwable error) {
        LoggingUtil.error(logger, "系统事件处理失败，类型: " + eventType, error);

        return systemService.recordSystemEvent(
                "SYSTEM_EVENT_ERROR",
                "ERROR",
                "SYSTEM",
                String.format("系统事件处理失败 - 类型: %s, 错误: %s", eventType, error.getMessage())).then();
    }

    // ==================== 统计查询方法 ====================

    /**
     * 获取系统事件总数
     *
     * @return 系统事件总数
     */
    public long getSystemEventCount() {
        return systemEventCount.get();
    }

    /**
     * 获取上下文刷新次数
     *
     * @return 上下文刷新次数
     */
    public long getContextRefreshCount() {
        return contextRefreshCount.get();
    }

    /**
     * 获取系统关闭次数
     *
     * @return 系统关闭次数
     */
    public long getSystemShutdownCount() {
        return systemShutdownCount.get();
    }

    /**
     * 获取最后事件时间
     *
     * @return 最后事件时间
     */
    public LocalDateTime getLastEventTime() {
        return lastEventTime;
    }

    /**
     * 重置事件统计
     */
    public void resetEventStatistics() {
        LoggingUtil.info(logger, "重置系统事件统计信息");
        systemEventCount.set(0);
        contextRefreshCount.set(0);
        systemShutdownCount.set(0);
        lastEventTime = null;
    }

    /**
     * 获取监听器状态
     *
     * @return 监听器状态信息
     */
    public String getListenerStatus() {
        return String.format("系统事件总数: %d, 上下文刷新次数: %d, 系统关闭次数: %d, 最后事件时间: %s",
                systemEventCount.get(),
                contextRefreshCount.get(),
                systemShutdownCount.get(),
                lastEventTime != null ? lastEventTime.toString() : "无");
    }
}
