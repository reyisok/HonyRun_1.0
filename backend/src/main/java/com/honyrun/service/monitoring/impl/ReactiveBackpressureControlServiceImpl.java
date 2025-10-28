package com.honyrun.service.monitoring.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.honyrun.config.monitoring.UnifiedMonitoringConfig;
import com.honyrun.service.monitoring.ReactiveBackpressureControlService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.MonitoringLogUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 响应式背压控制服务实现类
 *
 * 实现监控系统的背压控制和流量限制功能：
 * - 使用信号量控制并发监控任务数量
 * - 实现令牌桶算法进行流量限制
 * - 提供队列超时和溢出处理
 * - 统计背压和流量限制指标
 *
 * 背压控制实现：
 * - 信号量控制：限制同时执行的监控任务数
 * - 队列超时：防止任务在队列中等待过久
 * - 溢出策略：支持丢弃、缓冲、阻塞等策略
 * - 统计监控：记录任务执行和拒绝情况
 *
 * @author Mr.Rey
 * @created 2025-07-01 18:50:00
 * @modified 2025-07-01 18:50:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
public class ReactiveBackpressureControlServiceImpl implements ReactiveBackpressureControlService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveBackpressureControlServiceImpl.class);

    private final UnifiedMonitoringConfig monitoringConfiguration;
    private final Semaphore monitoringTaskSemaphore;

    /**
     * 构造函数注入
     *
     * @param monitoringConfiguration 监控配置
     * @param monitoringTaskSemaphore 监控任务信号量
     */
    public ReactiveBackpressureControlServiceImpl(UnifiedMonitoringConfig monitoringConfiguration,
                                                @Qualifier("devMonitoringTaskSemaphore") Semaphore monitoringTaskSemaphore) {
        this.monitoringConfiguration = monitoringConfiguration;
        this.monitoringTaskSemaphore = monitoringTaskSemaphore;
    }

    // ==================== 统计计数器 ====================

    private final AtomicLong totalTasks = new AtomicLong(0);
    private final AtomicLong completedTasks = new AtomicLong(0);
    private final AtomicLong rejectedTasks = new AtomicLong(0);
    private final AtomicLong timeoutTasks = new AtomicLong(0);

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong allowedRequests = new AtomicLong(0);
    private final AtomicLong rejectedRequests = new AtomicLong(0);

    // ==================== 速率限制状态 ====================

    private volatile LocalDateTime lastRefillTime = LocalDateTime.now();
    private volatile int availableTokens;
    private volatile double currentRate = 0.0;

    // ==================== 背压控制方法 ====================

    @Override
    public <T> Mono<T> executeWithBackpressure(Mono<T> task, String taskName) {
        totalTasks.incrementAndGet();

        LoggingUtil.debug(logger, "开始执行带背压控制的监控任务: {}", taskName);

        return Mono.fromCallable(() -> {
                    // 尝试获取信号量许可
                    boolean acquired = monitoringTaskSemaphore.tryAcquire(
                            monitoringConfiguration.getQueueTimeout().toMillis(),
                            TimeUnit.MILLISECONDS
                    );

                    if (!acquired) {
                        timeoutTasks.incrementAndGet();
                        LoggingUtil.warn(logger, "监控任务等待超时: {}", taskName);
                        MonitoringLogUtil.logSystemAlert("BACKPRESSURE_CONTROL", "TIMEOUT",
                                "监控任务等待超时: " + taskName, null);
                        throw new RuntimeException("监控任务等待超时: " + taskName);
                    }

                    return acquired;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(acquired -> {
                    if (acquired) {
                        return task
                                .doOnSuccess(result -> {
                                    completedTasks.incrementAndGet();
                                    LoggingUtil.debug(logger, "监控任务执行成功: {}", taskName);
                                })
                                .doOnError(error -> {
                                    rejectedTasks.incrementAndGet();
                                    LoggingUtil.warn(logger, "监控任务执行失败: {}, 错误: {}", taskName, error.getMessage());
                                })
                                .doFinally(signal -> {
                                    // 释放信号量许可
                                    monitoringTaskSemaphore.release();
                                    LoggingUtil.debug(logger, "释放监控任务信号量: {}", taskName);
                                });
                    } else {
                        rejectedTasks.incrementAndGet();
                        return Mono.error(new RuntimeException("无法获取监控任务许可: " + taskName));
                    }
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "背压控制执行失败: {}", taskName, error);
                    MonitoringLogUtil.logSystemAlert("BACKPRESSURE_CONTROL", "ERROR",
                            "背压控制执行失败: " + taskName + ", 错误: " + error.getMessage(), null);
                    return Mono.empty();
                });
    }

    @Override
    public <T> Flux<T> executeFluxWithBackpressure(Flux<T> task, String taskName) {
        LoggingUtil.debug(logger, "开始执行带背压控制的流式监控任务: {}", taskName);

        return task
                .onBackpressureBuffer(
                        monitoringConfiguration.getBackpressureBufferSize(),
                        this::handleBufferOverflow
                )
                .doOnSubscribe(subscription -> {
                    totalTasks.incrementAndGet();
                    LoggingUtil.debug(logger, "流式监控任务开始订阅: {}", taskName);
                })
                .doOnComplete(() -> {
                    completedTasks.incrementAndGet();
                    LoggingUtil.debug(logger, "流式监控任务完成: {}", taskName);
                })
                .doOnError(error -> {
                    rejectedTasks.incrementAndGet();
                    LoggingUtil.warn(logger, "流式监控任务失败: {}, 错误: {}", taskName, error.getMessage());
                });
    }

    @Override
    public Semaphore getMonitoringTaskSemaphore() {
        return monitoringTaskSemaphore;
    }

    @Override
    public Mono<Boolean> canExecuteTask() {
        return Mono.fromCallable(() -> monitoringTaskSemaphore.availablePermits() > 0)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Integer> getQueueSize() {
        return Mono.fromCallable(() -> monitoringTaskSemaphore.getQueueLength())
                .subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== 流量限制方法 ====================

    @Override
    public <T> Mono<T> applyRateLimit(Mono<T> task, String taskName) {
        if (!monitoringConfiguration.isRateLimitEnabled()) {
            return task;
        }

        totalRequests.incrementAndGet();

        return checkRateLimit(taskName)
                .flatMap(allowed -> {
                    if (allowed) {
                        allowedRequests.incrementAndGet();
                        return task;
                    } else {
                        rejectedRequests.incrementAndGet();
                        LoggingUtil.warn(logger, "监控任务被速率限制拒绝: {}", taskName);
                        MonitoringLogUtil.logSystemAlert("RATE_LIMIT", "REJECTED",
                                "监控任务被速率限制拒绝: " + taskName, null);
                        return Mono.empty();
                    }
                });
    }

    @Override
    public Mono<Boolean> checkRateLimit(String taskName) {
        return Mono.fromCallable(() -> {
                    refillTokens();

                    if (availableTokens > 0) {
                        availableTokens--;
                        updateCurrentRate();
                        LoggingUtil.debug(logger, "速率限制检查通过: {}, 剩余令牌: {}", taskName, availableTokens);
                        return true;
                    } else {
                        LoggingUtil.debug(logger, "速率限制检查失败: {}, 无可用令牌", taskName);
                        return false;
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> resetRateLimit(String taskName) {
        return Mono.fromCallable(() -> {
                    availableTokens = monitoringConfiguration.getRateLimitBurstCapacity();
                    lastRefillTime = LocalDateTime.now();
                    currentRate = 0.0;
                    LoggingUtil.info(logger, "重置速率限制: {}", taskName);
                    return true;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== 背压统计方法 ====================

    @Override
    public Mono<BackpressureStats> getBackpressureStats() {
        return Mono.fromCallable(() -> new BackpressureStats(
                        totalTasks.get(),
                        completedTasks.get(),
                        rejectedTasks.get(),
                        timeoutTasks.get(),
                        monitoringTaskSemaphore.getQueueLength(),
                        monitoringTaskSemaphore.availablePermits()
                ))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<RateLimitStats> getRateLimitStats() {
        return Mono.fromCallable(() -> new RateLimitStats(
                        totalRequests.get(),
                        allowedRequests.get(),
                        rejectedRequests.get(),
                        currentRate,
                        availableTokens
                ))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> resetAllStats() {
        return Mono.fromCallable(() -> {
                    totalTasks.set(0);
                    completedTasks.set(0);
                    rejectedTasks.set(0);
                    timeoutTasks.set(0);

                    totalRequests.set(0);
                    allowedRequests.set(0);
                    rejectedRequests.set(0);

                    LoggingUtil.info(logger, "重置所有背压控制统计计数器");
                    return true;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== 配置管理方法 ====================

    @Override
    public Mono<Boolean> adjustMaxConcurrentTasks(int maxConcurrentTasks) {
        return Mono.fromCallable(() -> {
                    // 注意：Semaphore不支持动态调整许可数，这里只是记录日志
                    LoggingUtil.warn(logger, "动态调整最大并发任务数需要重启应用: 当前={}, 新值={}",
                                   monitoringConfiguration.getMaxConcurrentTasks(), maxConcurrentTasks);
                    MonitoringLogUtil.logSystemAlert("BACKPRESSURE_CONFIG", "ADJUSTMENT_REQUIRED",
                            "需要重启应用以调整最大并发任务数: " + maxConcurrentTasks, null);
                    return false; // 返回false表示需要重启
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> adjustQueueTimeout(Duration timeout) {
        return Mono.fromCallable(() -> {
                    LoggingUtil.info(logger, "队列超时时间调整请求: {}", timeout);
                    MonitoringLogUtil.logSystemAlert("BACKPRESSURE_CONFIG", "TIMEOUT_ADJUSTMENT",
                            "队列超时时间调整请求: " + timeout, null);
                    return false; // 当前实现不支持动态调整
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> adjustRateLimit(double permitsPerSecond, int burstCapacity) {
        return Mono.fromCallable(() -> {
                    LoggingUtil.info(logger, "速率限制参数调整请求: permitsPerSecond={}, burstCapacity={}",
                                   permitsPerSecond, burstCapacity);
                    MonitoringLogUtil.logSystemAlert("RATE_LIMIT_CONFIG", "PARAMETER_ADJUSTMENT",
                            "速率限制参数调整请求: " + permitsPerSecond + "/" + burstCapacity, null);
                    return false; // 当前实现不支持动态调整
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 处理缓冲区溢出
     */
    private void handleBufferOverflow(Object dropped) {
        rejectedTasks.incrementAndGet();
        LoggingUtil.warn(logger, "背压缓冲区溢出，丢弃数据: {}", dropped);
        MonitoringLogUtil.logSystemAlert("BACKPRESSURE_BUFFER", "OVERFLOW",
                "背压缓冲区溢出，丢弃数据", null);
    }

    /**
     * 补充令牌
     */
    private void refillTokens() {
        LocalDateTime now = LocalDateTime.now();
        Duration elapsed = Duration.between(lastRefillTime, now);

        if (elapsed.toMillis() >= 1000) { // 每秒补充一次
            double tokensToAdd = monitoringConfiguration.getRateLimitPermitsPerSecond() *
                               (elapsed.toMillis() / 1000.0);

            availableTokens = Math.min(
                    availableTokens + (int) tokensToAdd,
                    monitoringConfiguration.getRateLimitBurstCapacity()
            );

            lastRefillTime = now;
            LoggingUtil.debug(logger, "补充令牌: 添加={}, 当前={}", (int) tokensToAdd, availableTokens);
        }
    }

    /**
     * 更新当前速率
     */
    private void updateCurrentRate() {
        LocalDateTime now = LocalDateTime.now();
        Duration elapsed = Duration.between(lastRefillTime, now);

        if (elapsed.toMillis() > 0) {
            currentRate = (double) allowedRequests.get() / (elapsed.toMillis() / 1000.0);
        }
    }
}

