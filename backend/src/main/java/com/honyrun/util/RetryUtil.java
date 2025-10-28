package com.honyrun.util;

import com.honyrun.exception.ErrorClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * 重试工具类
 * 
 * 基于错误分类提供智能重试策略：
 * - 支持指数退避重试
 * - 根据异常类型决定是否重试
 * - 提供重试次数和延迟配置
 * - 支持响应式编程模型
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-02 20:40:00
 * @modified 2025-07-02 20:40:00
 * @version 1.0.0
 */
public class RetryUtil {

    private static final Logger logger = LoggerFactory.getLogger(RetryUtil.class);

    // 默认重试配置
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final Duration DEFAULT_MIN_BACKOFF = Duration.ofMillis(100);
    private static final Duration DEFAULT_MAX_BACKOFF = Duration.ofSeconds(5);

    /**
     * 创建基于错误分类的重试策略
     * 
     * @return 重试策略
     */
    public static Retry createRetrySpec() {
        return createRetrySpec(DEFAULT_MAX_ATTEMPTS, DEFAULT_MIN_BACKOFF, DEFAULT_MAX_BACKOFF);
    }

    /**
     * 创建自定义重试策略
     * 
     * @param maxAttempts 最大重试次数
     * @param minBackoff 最小退避时间
     * @param maxBackoff 最大退避时间
     * @return 重试策略
     */
    public static Retry createRetrySpec(int maxAttempts, Duration minBackoff, Duration maxBackoff) {
        return Retry.backoff(maxAttempts, minBackoff)
                .maxBackoff(maxBackoff)
                .filter(createRetryPredicate())
                .doBeforeRetry(retrySignal -> {
                    TraceIdUtil.getCurrentTraceId()
                        .subscribe(traceId -> {
                            LoggingUtil.setMDC(LoggingUtil.TRACE_ID_KEY, traceId);
                            try {
                                LoggingUtil.warn(logger, "重试执行 - 第{}次重试, 异常: {}",
                                        retrySignal.totalRetries() + 1,
                                        retrySignal.failure().getMessage());
                            } finally {
                                LoggingUtil.clearMDC(LoggingUtil.TRACE_ID_KEY);
                            }
                        });
                })
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    TraceIdUtil.getCurrentTraceId()
                        .subscribe(traceId -> {
                            LoggingUtil.setMDC(LoggingUtil.TRACE_ID_KEY, traceId);
                            try {
                                LoggingUtil.error(logger, "重试耗尽 - 最大重试次数: {}, 最终异常: {}",
                                        maxAttempts,
                                        retrySignal.failure().getMessage());
                            } finally {
                                LoggingUtil.clearMDC(LoggingUtil.TRACE_ID_KEY);
                            }
                        });
                    return retrySignal.failure();
                });
    }

    /**
     * 创建重试判断条件
     * 
     * @return 重试判断谓词
     */
    private static Predicate<Throwable> createRetryPredicate() {
        return throwable -> {
            ErrorClassification classification = ErrorClassification.classify(throwable);
            boolean shouldRetry = classification.isRetryable();
            
            if (shouldRetry) {
                LoggingUtil.info(logger, "异常可重试 - 分类: {}, 异常: {}", 
                        classification.getDescription(), 
                        throwable.getMessage());
            } else {
                LoggingUtil.info(logger, "异常不可重试 - 分类: {}, 异常: {}", 
                        classification.getDescription(), 
                        throwable.getMessage());
            }
            
            return shouldRetry;
        };
    }

    /**
     * 为Mono添加重试策略
     * 
     * @param mono 原始Mono
     * @param <T> 数据类型
     * @return 带重试策略的Mono
     */
    public static <T> Mono<T> withRetry(Mono<T> mono) {
        return mono.retryWhen(createRetrySpec());
    }

    /**
     * 为Mono添加自定义重试策略
     * 
     * @param mono 原始Mono
     * @param maxAttempts 最大重试次数
     * @param minBackoff 最小退避时间
     * @param maxBackoff 最大退避时间
     * @param <T> 数据类型
     * @return 带重试策略的Mono
     */
    public static <T> Mono<T> withRetry(Mono<T> mono, int maxAttempts, Duration minBackoff, Duration maxBackoff) {
        return mono.retryWhen(createRetrySpec(maxAttempts, minBackoff, maxBackoff));
    }

    /**
     * 创建数据访问重试策略（针对数据库操作）
     * 
     * @return 数据访问重试策略
     */
    public static Retry createDataAccessRetrySpec() {
        return createRetrySpec(2, Duration.ofMillis(50), Duration.ofSeconds(1));
    }

    /**
     * 创建外部服务重试策略（针对外部API调用）
     * 
     * @return 外部服务重试策略
     */
    public static Retry createExternalServiceRetrySpec() {
        return createRetrySpec(3, Duration.ofMillis(200), Duration.ofSeconds(10));
    }
}

