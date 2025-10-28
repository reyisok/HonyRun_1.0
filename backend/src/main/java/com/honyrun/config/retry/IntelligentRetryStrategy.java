package com.honyrun.config.retry;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * 智能重试策略配置
 * 
 * <p>根据不同的错误类型和场景，提供智能化的重试策略。
 * 
 * <p><strong>重试策略：</strong>
 * <ul>
 *   <li>网络连接错误：指数退避重试</li>
 *   <li>数据库连接错误：固定间隔重试</li>
 *   <li>业务逻辑错误：不重试</li>
 *   <li>系统资源错误：延迟重试</li>
 * </ul>
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @version 1.0.0
 */
@Component
public class IntelligentRetryStrategy {

    private static final Logger logger = LoggerFactory.getLogger(IntelligentRetryStrategy.class);

    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        NETWORK_CONNECTION,     // 网络连接错误
        DATABASE_CONNECTION,    // 数据库连接错误
        BUSINESS_LOGIC,        // 业务逻辑错误
        SYSTEM_RESOURCE,       // 系统资源错误
        TIMEOUT,               // 超时错误
        UNKNOWN                // 未知错误
    }

    /**
     * 获取Redis连接重试策略
     * 
     * @return Redis重试策略
     */
    public Retry getRedisConnectionRetry() {
        return Retry.backoff(3, Duration.ofSeconds(2))
            .maxBackoff(Duration.ofSeconds(10))
            .filter(isRetryableError())
            .doBeforeRetry(retrySignal -> 
                LoggingUtil.warn(logger, "Redis连接重试 - 第{}次, 错误: {}", 
                    retrySignal.totalRetries() + 1, retrySignal.failure().getMessage()))
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                LoggingUtil.error(logger, "Redis连接重试次数耗尽: {}", retrySignal.failure().getMessage());
                return retrySignal.failure();
            });
    }

    /**
     * 获取数据库连接重试策略
     * 
     * @return 数据库重试策略
     */
    public Retry getDatabaseConnectionRetry() {
        return Retry.fixedDelay(5, Duration.ofSeconds(3))
            .filter(isDatabaseRetryableError())
            .doBeforeRetry(retrySignal -> 
                LoggingUtil.warn(logger, "数据库连接重试 - 第{}次, 错误: {}", 
                    retrySignal.totalRetries() + 1, retrySignal.failure().getMessage()))
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                LoggingUtil.error(logger, "数据库连接重试次数耗尽: {}", retrySignal.failure().getMessage());
                return retrySignal.failure();
            });
    }

    /**
     * 获取HTTP请求重试策略
     * 
     * @return HTTP重试策略
     */
    public Retry getHttpRequestRetry() {
        return Retry.backoff(2, Duration.ofSeconds(1))
            .maxBackoff(Duration.ofSeconds(5))
            .filter(isHttpRetryableError())
            .doBeforeRetry(retrySignal -> 
                LoggingUtil.warn(logger, "HTTP请求重试 - 第{}次, 错误: {}", 
                    retrySignal.totalRetries() + 1, retrySignal.failure().getMessage()))
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                LoggingUtil.error(logger, "HTTP请求重试次数耗尽: {}", retrySignal.failure().getMessage());
                return retrySignal.failure();
            });
    }

    /**
     * 获取系统资源重试策略
     * 
     * @return 系统资源重试策略
     */
    public Retry getSystemResourceRetry() {
        return Retry.backoff(4, Duration.ofSeconds(5))
            .maxBackoff(Duration.ofSeconds(30))
            .filter(isSystemResourceError())
            .doBeforeRetry(retrySignal -> 
                LoggingUtil.warn(logger, "系统资源重试 - 第{}次, 错误: {}", 
                    retrySignal.totalRetries() + 1, retrySignal.failure().getMessage()))
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                LoggingUtil.error(logger, "系统资源重试次数耗尽: {}", retrySignal.failure().getMessage());
                return retrySignal.failure();
            });
    }

    /**
     * 根据错误类型获取重试策略
     * 
     * @param errorType 错误类型
     * @return 对应的重试策略
     */
    public Retry getRetryByErrorType(ErrorType errorType) {
        switch (errorType) {
            case NETWORK_CONNECTION:
            case TIMEOUT:
                return getRedisConnectionRetry();
            case DATABASE_CONNECTION:
                return getDatabaseConnectionRetry();
            case SYSTEM_RESOURCE:
                return getSystemResourceRetry();
            case BUSINESS_LOGIC:
                return Retry.max(0); // 业务逻辑错误不重试
            default:
                return Retry.backoff(1, Duration.ofSeconds(2)); // 默认重试策略
        }
    }

    /**
     * 判断是否为可重试的错误
     * 
     * @return 错误判断谓词
     */
    private Predicate<Throwable> isRetryableError() {
        return throwable -> {
            String message = throwable.getMessage();
            if (message == null) return false;
            
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("connection") ||
                   lowerMessage.contains("timeout") ||
                   lowerMessage.contains("network") ||
                   lowerMessage.contains("refused") ||
                   lowerMessage.contains("unreachable");
        };
    }

    /**
     * 判断是否为数据库可重试错误
     * 
     * @return 错误判断谓词
     */
    private Predicate<Throwable> isDatabaseRetryableError() {
        return throwable -> {
            String message = throwable.getMessage();
            if (message == null) return false;
            
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("connection") ||
                   lowerMessage.contains("timeout") ||
                   lowerMessage.contains("deadlock") ||
                   lowerMessage.contains("lock wait timeout");
        };
    }

    /**
     * 判断是否为HTTP可重试错误
     * 
     * @return 错误判断谓词
     */
    private Predicate<Throwable> isHttpRetryableError() {
        return throwable -> {
            String message = throwable.getMessage();
            if (message == null) return false;
            
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("timeout") ||
                   lowerMessage.contains("502") ||
                   lowerMessage.contains("503") ||
                   lowerMessage.contains("504");
        };
    }

    /**
     * 判断是否为系统资源错误
     * 
     * @return 错误判断谓词
     */
    private Predicate<Throwable> isSystemResourceError() {
        return throwable -> {
            String message = throwable.getMessage();
            if (message == null) return false;
            
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("memory") ||
                   lowerMessage.contains("disk") ||
                   lowerMessage.contains("cpu") ||
                   lowerMessage.contains("resource");
        };
    }

    /**
     * 分析错误类型
     * 
     * @param throwable 异常
     * @return 错误类型
     */
    public ErrorType analyzeErrorType(Throwable throwable) {
        if (throwable == null) return ErrorType.UNKNOWN;
        
        String message = throwable.getMessage();
        if (message == null) return ErrorType.UNKNOWN;
        
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("redis") || lowerMessage.contains("connection")) {
            return ErrorType.NETWORK_CONNECTION;
        } else if (lowerMessage.contains("database") || lowerMessage.contains("sql")) {
            return ErrorType.DATABASE_CONNECTION;
        } else if (lowerMessage.contains("timeout")) {
            return ErrorType.TIMEOUT;
        } else if (lowerMessage.contains("memory") || lowerMessage.contains("resource")) {
            return ErrorType.SYSTEM_RESOURCE;
        } else if (lowerMessage.contains("business") || lowerMessage.contains("validation")) {
            return ErrorType.BUSINESS_LOGIC;
        }
        
        return ErrorType.UNKNOWN;
    }

    /**
     * 应用智能重试策略
     * 
     * @param mono 要重试的Mono
     * @param <T> 返回类型
     * @return 应用重试策略后的Mono
     */
    public <T> Mono<T> applyIntelligentRetry(Mono<T> mono) {
        return mono.retryWhen(Retry.withThrowable(throwableFlux -> 
            throwableFlux.flatMap(throwable -> {
                ErrorType errorType = analyzeErrorType(throwable);
                LoggingUtil.debug(logger, "检测到错误类型: {}, 应用对应重试策略", errorType);
                
                Retry retryStrategy = getRetryByErrorType(errorType);
                return Mono.error(throwable).retryWhen(retryStrategy);
            })
        ));
    }
}
