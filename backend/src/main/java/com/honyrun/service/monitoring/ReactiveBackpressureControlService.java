package com.honyrun.service.monitoring;

import java.time.Duration;
import java.util.concurrent.Semaphore;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式背压控制服务接口
 * 
 * 提供监控系统的背压控制和流量限制功能：
 * - 控制并发监控任务数量
 * - 实现流量限制和背压处理
 * - 提供监控任务队列管理
 * - 支持动态调整背压参数
 * 
 * 背压控制策略：
 * - 使用信号量控制并发任务数
 * - 实现队列超时机制
 * - 支持多种溢出策略（丢弃、缓冲、阻塞）
 * - 提供背压统计和监控
 * 
 * @author Mr.Rey
 * @created 2025-07-01 18:45:00
 * @modified 2025-07-01 18:45:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveBackpressureControlService {

    // ==================== 背压控制方法 ====================

    /**
     * 执行带背压控制的监控任务
     * 
     * @param <T> 返回类型
     * @param task 监控任务
     * @param taskName 任务名称
     * @return 执行结果的Mono
     */
    <T> Mono<T> executeWithBackpressure(Mono<T> task, String taskName);

    /**
     * 执行带背压控制的流式监控任务
     * 
     * @param <T> 返回类型
     * @param task 流式监控任务
     * @param taskName 任务名称
     * @return 执行结果的Flux
     */
    <T> Flux<T> executeFluxWithBackpressure(Flux<T> task, String taskName);

    /**
     * 获取监控任务信号量
     * 
     * @return 监控任务信号量
     */
    Semaphore getMonitoringTaskSemaphore();

    /**
     * 检查是否可以执行新的监控任务
     * 
     * @return 是否可以执行的Mono
     */
    Mono<Boolean> canExecuteTask();

    /**
     * 获取当前等待队列大小
     * 
     * @return 等待队列大小的Mono
     */
    Mono<Integer> getQueueSize();

    // ==================== 流量限制方法 ====================

    /**
     * 应用速率限制
     * 
     * @param <T> 返回类型
     * @param task 任务
     * @param taskName 任务名称
     * @return 限流后的任务
     */
    <T> Mono<T> applyRateLimit(Mono<T> task, String taskName);

    /**
     * 检查速率限制状态
     * 
     * @param taskName 任务名称
     * @return 是否允许执行的Mono
     */
    Mono<Boolean> checkRateLimit(String taskName);

    /**
     * 重置速率限制计数器
     * 
     * @param taskName 任务名称
     * @return 重置结果的Mono
     */
    Mono<Boolean> resetRateLimit(String taskName);

    // ==================== 背压统计方法 ====================

    /**
     * 获取背压控制统计信息
     * 
     * @return 背压统计信息的Mono
     */
    Mono<BackpressureStats> getBackpressureStats();

    /**
     * 获取流量限制统计信息
     * 
     * @return 流量限制统计信息的Mono
     */
    Mono<RateLimitStats> getRateLimitStats();

    /**
     * 重置所有统计计数器
     * 
     * @return 重置结果的Mono
     */
    Mono<Boolean> resetAllStats();

    // ==================== 配置管理方法 ====================

    /**
     * 动态调整最大并发任务数
     * 
     * @param maxConcurrentTasks 新的最大并发任务数
     * @return 调整结果的Mono
     */
    Mono<Boolean> adjustMaxConcurrentTasks(int maxConcurrentTasks);

    /**
     * 动态调整队列超时时间
     * 
     * @param timeout 新的超时时间
     * @return 调整结果的Mono
     */
    Mono<Boolean> adjustQueueTimeout(Duration timeout);

    /**
     * 动态调整速率限制参数
     * 
     * @param permitsPerSecond 每秒允许的请求数
     * @param burstCapacity 突发容量
     * @return 调整结果的Mono
     */
    Mono<Boolean> adjustRateLimit(double permitsPerSecond, int burstCapacity);

    // ==================== 数据传输对象 ====================

    /**
     * 背压控制统计信息
     */
    class BackpressureStats {
        private final long totalTasks;
        private final long completedTasks;
        private final long rejectedTasks;
        private final long timeoutTasks;
        private final int currentQueueSize;
        private final int availablePermits;

        public BackpressureStats(long totalTasks, long completedTasks, long rejectedTasks, 
                               long timeoutTasks, int currentQueueSize, int availablePermits) {
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.rejectedTasks = rejectedTasks;
            this.timeoutTasks = timeoutTasks;
            this.currentQueueSize = currentQueueSize;
            this.availablePermits = availablePermits;
        }

        // Getters
        public long getTotalTasks() { return totalTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public long getRejectedTasks() { return rejectedTasks; }
        public long getTimeoutTasks() { return timeoutTasks; }
        public int getCurrentQueueSize() { return currentQueueSize; }
        public int getAvailablePermits() { return availablePermits; }
    }

    /**
     * 速率限制统计信息
     */
    class RateLimitStats {
        private final long totalRequests;
        private final long allowedRequests;
        private final long rejectedRequests;
        private final double currentRate;
        private final int availableTokens;

        public RateLimitStats(long totalRequests, long allowedRequests, long rejectedRequests,
                            double currentRate, int availableTokens) {
            this.totalRequests = totalRequests;
            this.allowedRequests = allowedRequests;
            this.rejectedRequests = rejectedRequests;
            this.currentRate = currentRate;
            this.availableTokens = availableTokens;
        }

        // Getters
        public long getTotalRequests() { return totalRequests; }
        public long getAllowedRequests() { return allowedRequests; }
        public long getRejectedRequests() { return rejectedRequests; }
        public double getCurrentRate() { return currentRate; }
        public int getAvailableTokens() { return availableTokens; }
    }
}

