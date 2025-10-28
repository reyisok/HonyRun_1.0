package com.honyrun.service.reactive.impl;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.honyrun.service.reactive.ReactiveOptimisticLockingService;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * 响应式乐观锁服务实现类
 *
 * 提供统一的乐观锁异常处理和重试机制，包括：
 * - 自动检测OptimisticLockingFailureException
 * - 指数退避重试策略，带抖动
 * - 详细的统计和监控
 * - 可配置的重试参数
 *
 * 默认配置：
 * - 最大重试次数：3次
 * - 初始延迟：100毫秒
 * - 最大延迟：2秒
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01 01:00:00
 * @modified 2025-07-01 01:00:00
 * @version 1.0.0
 */
@Service
public class ReactiveOptimisticLockingServiceImpl implements ReactiveOptimisticLockingService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveOptimisticLockingServiceImpl.class);

    // 默认重试配置
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final Duration DEFAULT_INITIAL_DELAY = Duration.ofMillis(100);
    // private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(2); // 暂时未使用，保留以备将来扩展
    private static final double DEFAULT_JITTER = 0.1;

    // 统计计数器
    private final AtomicLong totalRetryAttempts = new AtomicLong(0);
    private final AtomicLong successfulRetries = new AtomicLong(0);
    private final AtomicLong failedRetries = new AtomicLong(0);
    private final AtomicLong optimisticLockExceptions = new AtomicLong(0);

    @Override
    public <T> Mono<T> executeWithOptimisticLockRetry(Mono<T> operation, String operationName) {
        return executeWithCustomRetry(operation, operationName, DEFAULT_MAX_RETRIES, DEFAULT_INITIAL_DELAY.toMillis());
    }

    @Override
    public <T> reactor.core.publisher.Flux<T> executeWithOptimisticLockRetry(reactor.core.publisher.Flux<T> operations,
            String operationName) {
        return operations.flatMap(operation -> executeWithOptimisticLockRetry(Mono.just(operation), operationName));
    }

    @Override
    public <T> Mono<T> executeWithCustomRetry(Mono<T> operation, String operationName,
            int maxRetries, long retryDelayMs) {
        Duration initialDelay = Duration.ofMillis(retryDelayMs);
        Duration maxDelay = Duration.ofSeconds(2);

        LoggingUtil.debug(logger, "执行乐观锁操作: {}, 最大重试次数: {}, 初始延迟: {}ms, 最大延迟: {}ms",
                operationName, maxRetries, initialDelay.toMillis(), maxDelay.toMillis());

        return operation
                .retryWhen(createOptimisticLockingRetrySpec(maxRetries, initialDelay, maxDelay))
                .doOnSuccess(result -> {
                    LoggingUtil.debug(logger, "乐观锁操作 {} 执行成功", operationName);
                })
                .doOnError(error -> {
                    if (isOptimisticLockingException(error)) {
                        optimisticLockExceptions.incrementAndGet();
                        failedRetries.incrementAndGet();
                        LoggingUtil.warn(logger, "乐观锁操作 {} 最终失败，已达到最大重试次数: {}", operationName, maxRetries);
                    } else {
                        LoggingUtil.error(logger, "乐观锁操作 {} 执行失败", operationName, error);
                    }
                });
    }

    @Override
    public Mono<Boolean> detectVersionConflict(String entityName, Long entityId,
            Long expectedVersion, Long actualVersion) {
        boolean hasConflict = expectedVersion != null && actualVersion != null &&
                !expectedVersion.equals(actualVersion);

        if (hasConflict) {
            LoggingUtil.warn(logger, "检测到版本冲突 - 实体: {}, ID: {}, 期望版本: {}, 实际版本: {}",
                    entityName, entityId, expectedVersion, actualVersion);
        }

        return Mono.just(hasConflict);
    }

    @Override
    public Mono<java.util.Map<String, Object>> getOptimisticLockStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalRetryAttempts", totalRetryAttempts.get());
        stats.put("successfulRetries", successfulRetries.get());
        stats.put("failedRetries", failedRetries.get());
        stats.put("optimisticLockExceptions", optimisticLockExceptions.get());

        long total = totalRetryAttempts.get();
        double successRate = total > 0 ? (double) successfulRetries.get() / total : 0.0;
        stats.put("successRate", successRate);

        return Mono.just(stats);
    }

    @Override
    public Mono<Void> resetOptimisticLockStatistics() {
        totalRetryAttempts.set(0);
        successfulRetries.set(0);
        failedRetries.set(0);
        optimisticLockExceptions.set(0);

        LoggingUtil.info(logger, "乐观锁统计信息已重置");
        return Mono.empty();
    }

    @Override
    public Mono<java.util.Map<String, Object>> getRetryStatistics(String operationName) {
        // 简化实现，返回全局统计信息
        return getOptimisticLockStatistics();
    }

    /**
     * 判断异常是否为乐观锁异常
     *
     * @param throwable 异常对象
     * @return 是否为乐观锁异常
     */
    @Override
    public boolean isOptimisticLockingException(Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        // 直接检查是否为OptimisticLockingFailureException
        if (throwable instanceof OptimisticLockingFailureException) {
            return true;
        }

        // 检查异常链中是否包含OptimisticLockingFailureException
        Throwable cause = throwable.getCause();
        while (cause != null) {
            if (cause instanceof OptimisticLockingFailureException) {
                return true;
            }
            cause = cause.getCause();
        }

        // 检查异常消息中是否包含乐观锁相关关键词
        String message = throwable.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("optimistic") && lowerMessage.contains("lock") ||
                    lowerMessage.contains("version") && lowerMessage.contains("conflict") ||
                    lowerMessage.contains("concurrent modification");
        }

        return false;
    }

    /**
     * 获取统计信息（向后兼容方法）
     *
     * @return 统计信息对象
     */
    public OptimisticLockingStatistics getStatistics() {
        OptimisticLockingStatistics stats = new OptimisticLockingStatistics();
        stats.setTotalRetryAttempts(totalRetryAttempts.get());
        stats.setSuccessfulRetries(successfulRetries.get());
        stats.setFailedRetries(failedRetries.get());
        stats.setOptimisticLockExceptions(optimisticLockExceptions.get());

        long total = totalRetryAttempts.get();
        double successRate = total > 0 ? (double) successfulRetries.get() / total : 0.0;
        stats.setSuccessRate(successRate);

        return stats;
    }

    /**
     * 重置统计信息（向后兼容方法）
     */
    public void resetStatistics() {
        totalRetryAttempts.set(0);
        successfulRetries.set(0);
        failedRetries.set(0);
        optimisticLockExceptions.set(0);
        LoggingUtil.info(logger, "乐观锁统计信息已重置");
    }

    /**
     * 创建乐观锁重试规范
     *
     * @param maxRetries   最大重试次数
     * @param initialDelay 初始延迟
     * @param maxDelay     最大延迟
     * @return 重试规范
     */
    private Retry createOptimisticLockingRetrySpec(int maxRetries, Duration initialDelay, Duration maxDelay) {
        return Retry.backoff(maxRetries, initialDelay)
                .maxBackoff(maxDelay)
                .jitter(DEFAULT_JITTER)
                .filter(this::isOptimisticLockingException)
                .doBeforeRetry(retrySignal -> {
                    totalRetryAttempts.incrementAndGet();
                    optimisticLockExceptions.incrementAndGet();
                    LoggingUtil.warn(logger, "检测到乐观锁冲突，第{}次重试，延迟{}ms",
                            retrySignal.totalRetries() + 1,
                            retrySignal.totalRetriesInARow());
                })
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    failedRetries.incrementAndGet();
                    LoggingUtil.error(logger, "乐观锁重试已耗尽，最大重试次数: {}", maxRetries);
                    return retrySignal.failure();
                });
    }

    /**
     * 乐观锁统计信息内部类
     */
    public static class OptimisticLockingStatistics {
        private long totalRetryAttempts;
        private long successfulRetries;
        private long failedRetries;
        private long optimisticLockExceptions;
        private double successRate;

        // Getters and Setters
        public long getTotalRetryAttempts() {
            return totalRetryAttempts;
        }

        public void setTotalRetryAttempts(long totalRetryAttempts) {
            this.totalRetryAttempts = totalRetryAttempts;
        }

        public long getSuccessfulRetries() {
            return successfulRetries;
        }

        public void setSuccessfulRetries(long successfulRetries) {
            this.successfulRetries = successfulRetries;
        }

        public long getFailedRetries() {
            return failedRetries;
        }

        public void setFailedRetries(long failedRetries) {
            this.failedRetries = failedRetries;
        }

        public long getOptimisticLockExceptions() {
            return optimisticLockExceptions;
        }

        public void setOptimisticLockExceptions(long optimisticLockExceptions) {
            this.optimisticLockExceptions = optimisticLockExceptions;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(double successRate) {
            this.successRate = successRate;
        }
    }
}

