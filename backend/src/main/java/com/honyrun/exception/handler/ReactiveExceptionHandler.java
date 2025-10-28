package com.honyrun.exception.handler;

import com.honyrun.exception.*;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.TraceIdUtil;
import com.honyrun.util.ErrorDetailsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

/**
 * 响应式异常处理器
 *
 * 提供响应式异常处理、错误恢复策略和降级机制
 * 支持异步异常处理和错误恢复
 *
 * 主要功能：
 * - 响应式异常处理和错误响应
 * - 错误恢复策略和重试机制
 * - 降级机制和熔断保护
 * - 异常统计和监控
 * - 错误日志记录和分析
 * - 自动错误恢复和通知
 *
 * 响应式特性：
 * - 非阻塞异常处理：异步处理异常和错误恢复
 * - 流式错误处理：支持响应式流中的错误处理
 * - 背压控制：控制错误处理的执行速度
 * - 错误恢复：提供多种错误恢复策略
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 12:00:00
 * @modified 2025-07-01 12:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
@ConditionalOnProperty(name = "honyrun.exception.reactive.enabled", havingValue = "true", matchIfMissing = true)
public class ReactiveExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveExceptionHandler.class);

    private final ErrorDetailsUtil errorDetailsUtil;

    /**
     * 构造函数注入
     *
     * @param errorDetailsUtil 错误详情工具
     */
    public ReactiveExceptionHandler(ErrorDetailsUtil errorDetailsUtil) {
        this.errorDetailsUtil = errorDetailsUtil;
    }

    /**
     * 默认重试次数
     */
    private static final int DEFAULT_RETRY_ATTEMPTS = 3;

    /**
     * 默认重试延迟（毫秒）
     */
    private static final long DEFAULT_RETRY_DELAY_MS = 1000;

    /**
     * 处理Mono异常
     *
     * @param mono 原始Mono
     * @param <T> 数据类型
     * @return 处理异常后的Mono
     */
    public <T> Mono<T> handleMonoException(Mono<T> mono) {
        return mono
                .onErrorMap(this::mapException)
                .doOnError(this::logError);
    }

    /**
     * 处理Mono异常并提供默认值
     *
     * @param mono 原始Mono
     * @param defaultValue 默认值
     * @param <T> 数据类型
     * @return 处理异常后的Mono
     */
    public <T> Mono<T> handleMonoExceptionWithDefault(Mono<T> mono, T defaultValue) {
        return mono
                .onErrorMap(this::mapException)
                .doOnError(this::logError)
                .onErrorReturn(defaultValue);
    }

    /**
     * 处理Mono异常并提供回退Mono
     *
     * @param mono 原始Mono
     * @param fallback 回退Mono提供者
     * @param <T> 数据类型
     * @return 处理异常后的Mono
     */
    public <T> Mono<T> handleMonoExceptionWithFallback(Mono<T> mono, Function<Throwable, Mono<T>> fallback) {
        return mono
                .onErrorMap(this::mapException)
                .doOnError(this::logError)
                .onErrorResume(fallback);
    }

    /**
     * 处理Flux异常
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 处理异常后的Flux
     */
    public <T> Flux<T> handleFluxException(Flux<T> flux) {
        return flux
                .onErrorMap(this::mapException)
                .doOnError(this::logError);
    }

    /**
     * 处理Flux异常并继续处理
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 处理异常后的Flux
     */
    public <T> Flux<T> handleFluxExceptionAndContinue(Flux<T> flux) {
        return flux
                .onErrorMap(this::mapException)
                .doOnError(this::logError)
                .onErrorContinue((throwable, obj) -> {
                    LoggingUtil.warn(logger, "Flux处理异常，继续处理下一个元素: {}", throwable.getMessage());
                });
    }

    /**
     * 处理Flux异常并提供回退Flux
     *
     * @param flux 原始Flux
     * @param fallback 回退Flux提供者
     * @param <T> 数据类型
     * @return 处理异常后的Flux
     */
    public <T> Flux<T> handleFluxExceptionWithFallback(Flux<T> flux, Function<Throwable, Flux<T>> fallback) {
        return flux
                .onErrorMap(this::mapException)
                .doOnError(this::logError)
                .onErrorResume(fallback);
    }

    /**
     * 带重试的Mono异常处理
     *
     * @param mono 原始Mono
     * @param retryAttempts 重试次数
     * @param retryDelayMs 重试延迟（毫秒）
     * @param <T> 数据类型
     * @return 带重试的Mono
     */
    public <T> Mono<T> handleMonoWithRetry(Mono<T> mono, int retryAttempts, long retryDelayMs) {
        return mono
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofMillis(retryDelayMs))
                        .filter(this::isRetryableException)
                        .doBeforeRetry(retrySignal ->
                            LoggingUtil.warn(logger, "重试Mono操作，第{}次重试: {}",
                                retrySignal.totalRetries() + 1, retrySignal.failure().getMessage())))
                .onErrorMap(this::mapException)
                .doOnError(this::logError);
    }

    /**
     * 带重试的Mono异常处理（使用默认参数）
     *
     * @param mono 原始Mono
     * @param <T> 数据类型
     * @return 带重试的Mono
     */
    public <T> Mono<T> handleMonoWithRetry(Mono<T> mono) {
        return handleMonoWithRetry(mono, DEFAULT_RETRY_ATTEMPTS, DEFAULT_RETRY_DELAY_MS);
    }

    /**
     * 带重试和降级的Mono异常处理
     *
     * @param mono 原始Mono
     * @param retryAttempts 重试次数
     * @param retryDelayMs 重试延迟（毫秒）
     * @param fallbackValue 降级值
     * @param <T> 数据类型
     * @return 带重试和降级的Mono
     */
    public <T> Mono<T> handleMonoWithRetryAndFallback(Mono<T> mono, int retryAttempts, long retryDelayMs, T fallbackValue) {
        return mono
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofMillis(retryDelayMs))
                        .filter(this::isRetryableException)
                        .doBeforeRetry(retrySignal ->
                            LoggingUtil.warn(logger, "重试Mono操作，第{}次重试: {}",
                                retrySignal.totalRetries() + 1, retrySignal.failure().getMessage())))
                .onErrorReturn(throwable -> {
                    LoggingUtil.warn(logger, "Mono操作最终失败，使用降级值: {}", throwable.getMessage());
                    return true; // 所有异常都使用降级值
                }, fallbackValue)
                .doOnError(this::logError);
    }

    /**
     * 带超时和重试的Mono异常处理
     *
     * @param mono 原始Mono
     * @param timeoutSeconds 超时时间（秒）
     * @param retryAttempts 重试次数
     * @param retryDelayMs 重试延迟（毫秒）
     * @param <T> 数据类型
     * @return 带超时和重试的Mono
     */
    public <T> Mono<T> handleMonoWithTimeoutAndRetry(Mono<T> mono, long timeoutSeconds, int retryAttempts, long retryDelayMs) {
        return mono
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofMillis(retryDelayMs))
                        .filter(this::isRetryableException)
                        .doBeforeRetry(retrySignal ->
                            LoggingUtil.warn(logger, "超时重试Mono操作，第{}次重试: {}",
                                retrySignal.totalRetries() + 1, retrySignal.failure().getMessage())))
                .onErrorMap(this::mapException)
                .doOnError(this::logError);
    }

    /**
     * 带重试的Flux异常处理
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 带重试的Flux
     */
    public <T> Flux<T> handleFluxWithRetry(Flux<T> flux) {
        return handleFluxWithRetry(flux, DEFAULT_RETRY_ATTEMPTS, DEFAULT_RETRY_DELAY_MS);
    }

    /**
     * 带重试的Flux异常处理
     *
     * @param flux 原始Flux
     * @param retryAttempts 重试次数
     * @param retryDelayMs 重试延迟（毫秒）
     * @param <T> 数据类型
     * @return 带重试的Flux
     */
    public <T> Flux<T> handleFluxWithRetry(Flux<T> flux, int retryAttempts, long retryDelayMs) {
        return flux
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofMillis(retryDelayMs))
                        .filter(this::isRetryableException)
                        .doBeforeRetry(retrySignal ->
                            LoggingUtil.warn(logger, "重试Flux操作，第{}次重试: {}",
                                retrySignal.totalRetries() + 1, retrySignal.failure().getMessage())))
                .onErrorMap(this::mapException)
                .doOnError(this::logError);
    }

    /**
     * 带降级的Flux异常处理
     *
     * @param flux 原始Flux
     * @param fallbackFlux 降级Flux
     * @param <T> 数据类型
     * @return 带降级的Flux
     */
    public <T> Flux<T> handleFluxWithFallback(Flux<T> flux, Flux<T> fallbackFlux) {
        return flux
                .onErrorResume(throwable -> {
                    LoggingUtil.warn(logger, "Flux操作失败，使用降级流: {}", throwable.getMessage());
                    return fallbackFlux;
                })
                .doOnError(this::logError);
    }

    /**
     * 带断路器模式的Mono异常处理
     *
     * @param mono 原始Mono
     * @param failureThreshold 失败阈值
     * @param recoveryTimeMs 恢复时间（毫秒）
     * @param fallbackValue 降级值
     * @param <T> 数据类型
     * @return 带断路器的Mono
     */
    public <T> Mono<T> handleMonoWithCircuitBreaker(Mono<T> mono, int failureThreshold, long recoveryTimeMs, T fallbackValue) {
        // 简化的断路器实现，实际项目中建议使用Resilience4j
        return mono
                .onErrorReturn(throwable -> {
                    LoggingUtil.warn(logger, "断路器触发，使用降级值: {}", throwable.getMessage());
                    return true; // 简化处理，所有异常都使用降级值
                }, fallbackValue)
                .doOnError(this::logError);
    }

    /**
     * 创建错误恢复Mono
     *
     * @param errorCode 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误Mono
     */
    public <T> Mono<T> createErrorMono(ErrorCode errorCode, String message) {
        return Mono.error(new BusinessException(errorCode, message));
    }

    /**
     * 创建错误恢复Flux
     *
     * @param errorCode 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误Flux
     */
    public <T> Flux<T> createErrorFlux(ErrorCode errorCode, String message) {
        return Flux.error(new BusinessException(errorCode, message));
    }

    /**
     * 映射异常类型
     *
     * @param throwable 原始异常
     * @return 映射后的异常
     */
    private Throwable mapException(Throwable throwable) {
        // 如果已经是自定义异常，直接返回
        if (throwable instanceof BusinessException ||
            throwable instanceof SystemException ||
            throwable instanceof ValidationException ||
            throwable instanceof AuthenticationException ||
            throwable instanceof DataAccessException ||
            throwable instanceof ExternalServiceException ||
            throwable instanceof RateLimitException) {
            return throwable;
        }

        // 映射常见异常类型
        if (throwable instanceof IllegalArgumentException) {
            return new ValidationException(ErrorCode.INVALID_PARAMETER, throwable.getMessage());
        }

        if (throwable instanceof IllegalStateException) {
            return new SystemException(ErrorCode.SYSTEM_ERROR, throwable.getMessage(), throwable);
        }

        if (throwable instanceof java.util.concurrent.TimeoutException) {
            return new SystemException(ErrorCode.DATABASE_TIMEOUT, "操作超时", throwable);
        }

        if (throwable instanceof java.net.ConnectException) {
            return new SystemException(ErrorCode.DATABASE_CONNECTION_ERROR, "连接失败", throwable);
        }

        if (throwable instanceof java.io.IOException) {
            return new SystemException(ErrorCode.NETWORK_ERROR, "网络错误", throwable);
        }

        // 数据库相关异常
        if (throwable.getClass().getName().contains("R2dbcException") ||
            throwable.getClass().getName().contains("DatabaseException")) {
            return new DataAccessException(ErrorCode.DATABASE_ERROR, throwable.getMessage(), throwable);
        }

        // 默认映射为系统异常
        return new SystemException(ErrorCode.INTERNAL_SERVER_ERROR, "系统内部错误", throwable);
    }

    /**
     * 记录错误日志
     *
     * @param throwable 异常
     */
    private void logError(Throwable throwable) {
        if (throwable instanceof BusinessException) {
            LoggingUtil.warn(logger, "业务异常: {}", throwable.getMessage());
        } else if (throwable instanceof ValidationException) {
            LoggingUtil.warn(logger, "验证异常: {}", throwable.getMessage());
        } else if (throwable instanceof AuthenticationException) {
            LoggingUtil.warn(logger, "认证异常: {}", throwable.getMessage());
        } else {
            LoggingUtil.error(logger, "系统异常: {}", throwable, throwable.getMessage());
        }
    }

    /**
     * 判断异常是否可重试
     *
     * @param throwable 异常
     * @return 如果可重试返回true，否则返回false
     */
    private boolean isRetryableException(Throwable throwable) {
        // 业务异常和验证异常不重试
        if (throwable instanceof BusinessException ||
            throwable instanceof ValidationException ||
            throwable instanceof AuthenticationException) {
            return false;
        }

        // 网络异常、超时异常、连接异常可重试
        if (throwable instanceof java.net.ConnectException ||
            throwable instanceof java.util.concurrent.TimeoutException ||
            throwable instanceof java.io.IOException) {
            return true;
        }

        // 数据库连接异常可重试
        if (throwable instanceof DataAccessException) {
            DataAccessException dae = (DataAccessException) throwable;
            return dae.getErrorCode() == ErrorCode.DATABASE_CONNECTION_ERROR ||
                   dae.getErrorCode() == ErrorCode.DATABASE_TIMEOUT;
        }

        // 外部服务异常可重试
        if (throwable instanceof ExternalServiceException) {
            return true;
        }

        // 系统异常中的部分错误可重试
        if (throwable instanceof SystemException) {
            SystemException se = (SystemException) throwable;
            return se.getErrorCode() == ErrorCode.DATABASE_CONNECTION_ERROR ||
                   se.getErrorCode() == ErrorCode.DATABASE_TIMEOUT ||
                   se.getErrorCode() == ErrorCode.NETWORK_ERROR;
        }

        return false;
    }

    // ========================================
    // 静态工具方法 - 用于测试和通用异常处理
    // ========================================

    /**
     * 带异常处理的Mono包装
     *
     * @param mono 原始Mono
     * @param fallbackValue 降级值
     * @param <T> 数据类型
     * @return 处理异常后的Mono
     */
    public static <T> Mono<T> withExceptionHandling(Mono<T> mono, T fallbackValue) {
        return mono.onErrorReturn(fallbackValue);
    }

    /**
     * 带重试和降级的Mono包装
     *
     * @param mono 原始Mono
     * @param retryAttempts 重试次数
     * @param fallbackValue 降级值
     * @param <T> 数据类型
     * @return 处理异常后的Mono
     */
    public static <T> Mono<T> withRetryAndFallback(Mono<T> mono, int retryAttempts, T fallbackValue) {
        return mono
                .retry(retryAttempts)
                .onErrorReturn(fallbackValue);
    }

    // ========================================
    // 测试专用异常处理方法
    // ========================================

    /**
     * 处理业务异常（测试专用）
     *
     * @param ex 业务异常
     * @return 业务异常响应的Mono包装
     */
    public Mono<org.springframework.http.ResponseEntity<com.honyrun.model.dto.response.ApiResponse<Void>>> handleBusinessException(BusinessException ex) {
        return Mono.deferContextual(ctxView -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            return Mono.fromCallable(() -> {
                LoggingUtil.warn(logger, "业务异常", ex);

                String code = ex.getErrorCode() != null ?
                        String.valueOf(ex.getErrorCode().getCode()) : "400";
                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, "reactive-handler");

                com.honyrun.model.dto.response.ApiResponse<Void> response =
                    com.honyrun.model.dto.response.ApiResponse.error(code, ex.getMessage(), traceId, details, "reactive-handler");

                return org.springframework.http.ResponseEntity.badRequest().body(response);
            });
        });
    }

    /**
     * 处理验证异常（测试专用）
     *
     * @param ex 验证异常
     * @return 验证异常响应的Mono包装
     */
    public Mono<org.springframework.http.ResponseEntity<com.honyrun.model.dto.response.ApiResponse<Void>>> handleValidationException(ValidationException ex) {
        return Mono.deferContextual(ctxView -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            return Mono.fromCallable(() -> {
                LoggingUtil.warn(logger, "验证异常", ex);

                String code = ex.getErrorCode() != null ?
                        String.valueOf(ex.getErrorCode().getCode()) : "400";
                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, "reactive-handler");

                com.honyrun.model.dto.response.ApiResponse<Void> response =
                    com.honyrun.model.dto.response.ApiResponse.error(code, "验证失败: " + ex.getMessage(), traceId, details, "reactive-handler");

                return org.springframework.http.ResponseEntity.badRequest().body(response);
            });
        });
    }

    /**
     * 处理认证异常（测试专用）
     *
     * @param ex 认证异常
     * @return 认证异常响应的Mono包装
     */
    public Mono<org.springframework.http.ResponseEntity<com.honyrun.model.dto.response.ApiResponse<Void>>> handleAuthenticationException(AuthenticationException ex) {
        return Mono.deferContextual(ctxView -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            return Mono.fromCallable(() -> {
                LoggingUtil.warn(logger, "认证异常", ex);

                String code = ex.getErrorCode() != null ?
                        String.valueOf(ex.getErrorCode().getCode()) : "401";
                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, "reactive-handler");

                com.honyrun.model.dto.response.ApiResponse<Void> response =
                    com.honyrun.model.dto.response.ApiResponse.error(code, "认证失败: " + ex.getMessage(), traceId, details, "reactive-handler");

                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(response);
            });
        });
    }

    /**
     * 处理权限拒绝异常（测试专用）
     *
     * @param ex 权限拒绝异常
     * @return 权限拒绝异常响应的Mono包装
     */
    public Mono<org.springframework.http.ResponseEntity<com.honyrun.model.dto.response.ApiResponse<Void>>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        return Mono.deferContextual(ctxView -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            return Mono.fromCallable(() -> {
                String message = "权限不足: " + ex.getMessage();
                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, "/api/access-denied");

                LoggingUtil.warn(logger, "权限拒绝异常", ex);

                com.honyrun.model.dto.response.ApiResponse<Void> response =
                    com.honyrun.model.dto.response.ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .traceId(traceId)
                        .details(details)
                        .build();

                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(response);
            });
        });
    }

    /**
     * 处理数据库异常（测试专用）
     *
     * @param ex 数据库异常
     * @return 数据库异常响应的Mono包装
     */
    public Mono<org.springframework.http.ResponseEntity<com.honyrun.model.dto.response.ApiResponse<Void>>> handleDatabaseException(com.honyrun.exception.DatabaseException ex) {
        return Mono.deferContextual(ctxView -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            return Mono.fromCallable(() -> {
                String message = "数据库操作失败: " + ex.getMessage();
                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, "/api/database-error");

                LoggingUtil.error(logger, "数据库异常", ex);

                com.honyrun.model.dto.response.ApiResponse<Void> response =
                    com.honyrun.model.dto.response.ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .traceId(traceId)
                        .details(details)
                        .build();

                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            });
        });
    }

    /**
     * 处理数据访问异常（测试专用）
     *
     * @param ex 数据访问异常
     * @return 数据访问异常响应的Mono包装
     */
    public Mono<org.springframework.http.ResponseEntity<com.honyrun.model.dto.response.ApiResponse<Void>>> handleDataAccessException(org.springframework.dao.DataAccessException ex) {
        return Mono.deferContextual(ctxView -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            return Mono.fromCallable(() -> {
                String message = "数据访问错误: " + ex.getMessage();
                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, "/api/data-access-error");

                LoggingUtil.error(logger, "数据访问异常", ex);

                com.honyrun.model.dto.response.ApiResponse<Void> response =
                    com.honyrun.model.dto.response.ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .traceId(traceId)
                        .details(details)
                        .build();

                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            });
        });
    }

    /**
     * 处理运行时异常（测试专用）
     *
     * @param ex 运行时异常
     * @return 运行时异常响应的Mono包装
     */
    public Mono<org.springframework.http.ResponseEntity<com.honyrun.model.dto.response.ApiResponse<Void>>> handleRuntimeException(RuntimeException ex) {
        return Mono.deferContextual(ctxView -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            return Mono.fromCallable(() -> {
                String message = "运行时错误: " + ex.getMessage();
                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, "/api/runtime-error");

                LoggingUtil.error(logger, "运行时异常", ex);

                com.honyrun.model.dto.response.ApiResponse<Void> response =
                    com.honyrun.model.dto.response.ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .traceId(traceId)
                        .details(details)
                        .build();

                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            });
        });
    }

    /**
     * 处理非法参数异常（测试专用）
     *
     * @param ex 非法参数异常
     * @return 非法参数异常响应的Mono包装
     */
    public Mono<org.springframework.http.ResponseEntity<com.honyrun.model.dto.response.ApiResponse<Void>>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Mono.deferContextual(ctxView -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            return Mono.fromCallable(() -> {
                String message = "参数错误: " + ex.getMessage();
                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, "/api/illegal-argument");

                LoggingUtil.warn(logger, "非法参数异常", ex);

                com.honyrun.model.dto.response.ApiResponse<Void> response =
                    com.honyrun.model.dto.response.ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .traceId(traceId)
                        .details(details)
                        .build();

                return org.springframework.http.ResponseEntity.badRequest().body(response);
            });
        });
    }

    /**
     * 处理通用异常（测试专用）
     *
     * @param ex 通用异常
     * @return 通用异常响应的Mono包装
     */
    public Mono<org.springframework.http.ResponseEntity<com.honyrun.model.dto.response.ApiResponse<Void>>> handleGenericException(Exception ex) {
        return Mono.deferContextual(ctxView -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            return Mono.fromCallable(() -> {
                String message = "系统错误: " + ex.getMessage();
                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, "/api/generic-error");

                LoggingUtil.error(logger, "通用异常", ex);

                com.honyrun.model.dto.response.ApiResponse<Void> response =
                    com.honyrun.model.dto.response.ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .traceId(traceId)
                        .details(details)
                        .build();

                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            });
        });
    }

    /**
     * 处理约束违反异常（测试专用）
     *
     * @param ex 约束违反异常
     * @return 约束违反异常响应的Mono包装
     */
    public Mono<org.springframework.http.ResponseEntity<com.honyrun.model.dto.response.ApiResponse<Void>>> handleConstraintViolationException(jakarta.validation.ConstraintViolationException ex) {
        return Mono.deferContextual(ctxView -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            return Mono.fromCallable(() -> {
                String errorMessage = ex.getConstraintViolations().stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .collect(java.util.stream.Collectors.joining(", "));

                String message = "约束验证失败: " + errorMessage;
                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, "/api/constraint-violation");

                LoggingUtil.warn(logger, "约束违反异常", ex);

                com.honyrun.model.dto.response.ApiResponse<Void> response =
                    com.honyrun.model.dto.response.ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .traceId(traceId)
                        .details(details)
                        .build();

                return org.springframework.http.ResponseEntity.badRequest().body(response);
            });
        });
    }

    /**
     * 处理绑定异常（测试专用）
     *
     * @param ex 绑定异常
     * @return 绑定异常响应的Mono包装
     */
    public Mono<org.springframework.http.ResponseEntity<com.honyrun.model.dto.response.ApiResponse<Void>>> handleBindException(org.springframework.validation.BindException ex) {
        return Mono.deferContextual(ctxView -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            return Mono.fromCallable(() -> {
                String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                        .map(org.springframework.validation.FieldError::getDefaultMessage)
                        .collect(java.util.stream.Collectors.joining(", "));

                String message = "数据绑定失败: " + errorMessage;
                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, "/api/bind-error");

                LoggingUtil.warn(logger, "数据绑定异常", ex);

                com.honyrun.model.dto.response.ApiResponse<Void> response =
                    com.honyrun.model.dto.response.ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .traceId(traceId)
                        .details(details)
                        .build();

                return org.springframework.http.ResponseEntity.badRequest().body(response);
            });
        });
    }

    /**
     * 处理方法参数无效异常（测试专用）
     *
     * @param ex 方法参数无效异常
     * @return 方法参数无效异常响应的Mono包装
     */
    public Mono<org.springframework.http.ResponseEntity<com.honyrun.model.dto.response.ApiResponse<Void>>> handleMethodArgumentNotValidException(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        return Mono.deferContextual(ctxView -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            return Mono.fromCallable(() -> {
                String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                        .map(org.springframework.validation.FieldError::getDefaultMessage)
                        .collect(java.util.stream.Collectors.joining(", "));

                String message = "参数验证失败: " + errorMessage;
                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, "/api/method-argument-not-valid");

                LoggingUtil.warn(logger, "方法参数验证异常", ex);

                com.honyrun.model.dto.response.ApiResponse<Void> response =
                    com.honyrun.model.dto.response.ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .traceId(traceId)
                        .details(details)
                        .build();

                return org.springframework.http.ResponseEntity.badRequest().body(response);
            });
        });
    }

    /**
     * 处理空指针异常（测试专用）
     *
     * @param ex 空指针异常
     * @return 空指针异常响应的Mono包装
     */
    public Mono<org.springframework.http.ResponseEntity<com.honyrun.model.dto.response.ApiResponse<Void>>> handleNullPointerException(NullPointerException ex) {
        return Mono.deferContextual(ctxView -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            return Mono.fromCallable(() -> {
                String message = "系统内部错误: 空指针异常";
                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, "/api/null-pointer-error");

                LoggingUtil.error(logger, "空指针异常", ex);

                com.honyrun.model.dto.response.ApiResponse<Void> response =
                    com.honyrun.model.dto.response.ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .traceId(traceId)
                        .details(details)
                        .build();

                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            });
        });
    }
}
