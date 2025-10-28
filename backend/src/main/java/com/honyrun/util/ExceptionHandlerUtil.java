package com.honyrun.util;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.honyrun.config.MonitoringProperties;
import com.honyrun.exception.BusinessException;
import com.honyrun.model.dto.response.ApiResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import reactor.core.publisher.Mono;

/**
 * 统一异常处理工具类
 *
 * 提供统一的异常处理方法，减少控制器中重复的异常处理逻辑。
 * 包括各种常见异常的处理方法和响应格式标准化。
 *
 * 主要功能：
 * - 业务异常处理
 * - 参数验证异常处理
 * - 数据访问异常处理
 * - 权限异常处理
 * - 系统异常处理
 * - 异常日志记录
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 16:15:00
 * @modified 2025-10-27 12:26:43
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class ExceptionHandlerUtil {

    private final MonitoringProperties monitoringProperties;

    public ExceptionHandlerUtil(MonitoringProperties monitoringProperties) {
        this.monitoringProperties = monitoringProperties;
    }

    /**
     * 处理业务异常
     *
     * @param ex     业务异常
     * @param logger 日志记录器
     * @return 业务异常响应的Mono包装
     */
    public static Mono<ResponseEntity<ApiResponse<Void>>> handleBusinessException(
            BusinessException ex,
            Logger logger) {
        return Mono.deferContextual(ctx -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctx);

            LoggingUtil.warn(logger, "业务异常: {}", ex.getMessage());

            Map<String, Object> errorDetails = new java.util.HashMap<>();
            errorDetails.put("exception", "BUSINESS_ERROR");
            errorDetails.put("message", ex.getMessage());
            errorDetails.put("path", "/api/business-error");
            errorDetails.put("timestamp", LocalDateTime.now());

            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .traceId(traceId)
                            .details(errorDetails)
                            .build()));
        });
    }

    /**
     * 处理参数验证异常
     *
     * @param ex     参数验证异常
     * @param logger 日志记录器
     * @return 参数验证异常响应的Mono包装
     */
    public static Mono<ResponseEntity<ApiResponse<Void>>> handleValidationException(
            MethodArgumentNotValidException ex,
            Logger logger) {
        return Mono.deferContextual(ctx -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctx);

            String errorMessage = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            LoggingUtil.warn(logger, "参数验证失败: {}", errorMessage);

            Map<String, Object> errorDetails = new java.util.HashMap<>();
            errorDetails.put("exception", ex.getClass().getSimpleName());
            errorDetails.put("message", ex.getMessage());
            errorDetails.put("path", "/api/validation-error");
            errorDetails.put("timestamp", LocalDateTime.now());

            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("参数验证失败: " + errorMessage)
                            .traceId(traceId)
                            .details(errorDetails)
                            .build()));
        });
    }

    /**
     * 处理绑定异常
     *
     * @param ex     绑定异常
     * @param logger 日志记录器
     * @return 绑定异常响应的Mono包装
     */
    public static Mono<ResponseEntity<ApiResponse<Void>>> handleBindException(
            BindException ex,
            Logger logger) {
        return Mono.deferContextual(ctx -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctx);

            String errorMessage = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            LoggingUtil.warn(logger, "绑定异常: {}", errorMessage);

            Map<String, Object> errorDetails = new java.util.HashMap<>();
            errorDetails.put("exception", ex.getClass().getSimpleName());
            errorDetails.put("message", ex.getMessage());
            errorDetails.put("path", "/api/bind-error");
            errorDetails.put("timestamp", LocalDateTime.now());

            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("参数绑定失败: " + errorMessage)
                            .traceId(traceId)
                            .details(errorDetails)
                            .build()));
        });
    }

    /**
     * 处理约束违反异常
     *
     * @param ex     约束违反异常
     * @param logger 日志记录器
     * @return 约束违反异常响应的Mono包装
     */
    public static Mono<ResponseEntity<ApiResponse<Void>>> handleConstraintViolationException(
            ConstraintViolationException ex,
            Logger logger) {
        return Mono.deferContextual(ctx -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctx);

            String errorMessage = ex.getConstraintViolations()
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            LoggingUtil.warn(logger, "约束违反: {}", errorMessage);

            Map<String, Object> errorDetails = new java.util.HashMap<>();
            errorDetails.put("exception", ex.getClass().getSimpleName());
            errorDetails.put("message", ex.getMessage());
            errorDetails.put("path", "/api/constraint-violation");
            errorDetails.put("timestamp", LocalDateTime.now());

            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("约束违反: " + errorMessage)
                            .traceId(traceId)
                            .details(errorDetails)
                            .build()));
        });
    }

    /**
     * 处理访问拒绝异常
     *
     * @param ex     访问拒绝异常
     * @param logger 日志记录器
     * @return 访问拒绝异常响应的Mono包装
     */
    public static Mono<ResponseEntity<ApiResponse<Void>>> handleAccessDeniedException(
            AccessDeniedException ex,
            Logger logger) {
        return Mono.deferContextual(ctx -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctx);

            LoggingUtil.warn(logger, "访问拒绝: {}", ex.getMessage());

            Map<String, Object> errorDetails = new java.util.HashMap<>();
            errorDetails.put("exception", ex.getClass().getSimpleName());
            errorDetails.put("message", ex.getMessage());
            errorDetails.put("path", "/api/access-denied");
            errorDetails.put("timestamp", LocalDateTime.now());

            return Mono.just(ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("访问被拒绝: " + ex.getMessage())
                            .traceId(traceId)
                            .details(errorDetails)
                            .build()));
        });
    }

    /**
     * 处理数据访问异常
     *
     * @param ex     数据访问异常
     * @param logger 日志记录器
     * @return 数据访问异常响应的Mono包装
     */
    public static Mono<ResponseEntity<ApiResponse<Void>>> handleDataAccessException(
            DataAccessException ex,
            Logger logger) {
        return Mono.deferContextual(ctx -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctx);

            LoggingUtil.error(logger, "数据访问异常", ex);

            Map<String, Object> errorDetails = new java.util.HashMap<>();
            errorDetails.put("exception", ex.getClass().getSimpleName());
            errorDetails.put("message", ex.getMessage());
            errorDetails.put("path", "/api/data-access-error");
            errorDetails.put("timestamp", LocalDateTime.now());

            return Mono.just(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("数据访问错误，请稍后重试")
                            .traceId(traceId)
                            .details(errorDetails)
                            .build()));
        });
    }

    /**
     * 处理通用异常
     *
     * @param ex     通用异常
     * @param logger 日志记录器
     * @return 通用异常响应的Mono包装
     */
    public static Mono<ResponseEntity<ApiResponse<Void>>> handleGenericException(
            Exception ex,
            Logger logger) {
        return Mono.deferContextual(ctx -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctx);

            LoggingUtil.error(logger, "系统异常", ex);

            Map<String, Object> errorDetails = new java.util.HashMap<>();
            errorDetails.put("exception", "SYSTEM_ERROR");
            errorDetails.put("message", "SYSTEM_ERROR");
            errorDetails.put("path", "/api/system-error");
            errorDetails.put("timestamp", LocalDateTime.now());

            return Mono.just(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("系统内部错误，请稍后重试")
                            .traceId(traceId)
                            .details(errorDetails)
                            .build()));
        });
    }

    /**
     * 处理运行时异常
     *
     * @param ex     运行时异常
     * @param logger 日志记录器
     * @return 运行时异常响应的Mono包装
     */
    public static Mono<ResponseEntity<ApiResponse<Void>>> handleRuntimeException(
            RuntimeException ex,
            Logger logger) {
        return Mono.deferContextual(ctx -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctx);

            LoggingUtil.error(logger, "运行时异常", ex);

            Map<String, Object> errorDetails = new java.util.HashMap<>();
            errorDetails.put("exception", ex.getClass().getSimpleName());
            errorDetails.put("message", ex.getMessage());
            errorDetails.put("path", "/api/runtime-error");
            errorDetails.put("timestamp", LocalDateTime.now());

            return Mono.just(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("运行时错误: " + ex.getMessage())
                            .traceId(traceId)
                            .details(errorDetails)
                            .build()));
        });
    }

    /**
     * 处理非法参数异常
     *
     * @param ex     非法参数异常
     * @param logger 日志记录器
     * @return 非法参数异常响应的Mono包装
     */
    public static Mono<ResponseEntity<ApiResponse<Void>>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            Logger logger) {
        return Mono.deferContextual(ctx -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctx);

            LoggingUtil.warn(logger, "非法参数: {}", ex.getMessage());

            // 简化错误详情，避免依赖注入问题
            Map<String, Object> errorDetails = new java.util.HashMap<>();
            errorDetails.put("exception", ex.getClass().getSimpleName());
            errorDetails.put("message", ex.getMessage());
            errorDetails.put("path", "/api/illegal-argument");
            errorDetails.put("timestamp", java.time.LocalDateTime.now().toString());

            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("参数错误: " + ex.getMessage())
                            .traceId(traceId)
                            .details(errorDetails)
                            .build()));
        });
    }

    /**
     * 处理空指针异常
     *
     * @param ex     空指针异常
     * @param logger 日志记录器
     * @return 空指针异常响应的Mono包装
     */
    public static Mono<ResponseEntity<ApiResponse<Void>>> handleNullPointerException(
            NullPointerException ex,
            Logger logger) {
        return Mono.deferContextual(ctx -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctx);

            LoggingUtil.error(logger, "空指针异常", ex);

            // 简化错误详情，避免依赖注入问题
            Map<String, Object> errorDetails = new java.util.HashMap<>();
            errorDetails.put("exception", ex.getClass().getSimpleName());
            errorDetails.put("message", ex.getMessage());
            errorDetails.put("path", "/api/null-pointer-error");
            errorDetails.put("timestamp", java.time.LocalDateTime.now().toString());

            return Mono.just(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("系统内部错误，请稍后重试")
                            .traceId(traceId)
                            .details(errorDetails)
                            .build()));
        });
    }

    // ==================== Map格式异常响应 ====================

    /**
     * 处理业务异常（Map格式响应）
     *
     * @param ex     业务异常
     * @param logger 日志记录器
     * @return 业务异常响应的Mono包装
     */
    public static Mono<ResponseEntity<Map<String, Object>>> handleBusinessExceptionMap(
            BusinessException ex,
            Logger logger) {

        LoggingUtil.warn(logger, "业务异常: {}", ex.getMessage());

        Map<String, Object> errorResponse = Map.of(
                "error", "BUSINESS_ERROR",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now());

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    /**
     * 处理通用异常（Map格式响应）
     *
     * @param ex     通用异常
     * @param logger 日志记录器
     * @return 通用异常响应的Mono包装
     */
    public static Mono<ResponseEntity<Map<String, Object>>> handleGenericExceptionMap(
            Exception ex,
            Logger logger) {

        LoggingUtil.error(logger, "系统异常", ex);

        Map<String, Object> errorResponse = Map.of(
                "error", "INTERNAL_ERROR",
                "message", "系统内部错误",
                "timestamp", LocalDateTime.now());

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

    /**
     * 处理权限拒绝异常（Map格式响应）
     *
     * @param ex     权限拒绝异常
     * @param logger 日志记录器
     * @return 权限拒绝异常响应的Mono包装
     */
    public static Mono<ResponseEntity<Map<String, Object>>> handleAccessDeniedExceptionMap(
            AccessDeniedException ex,
            Logger logger) {

        LoggingUtil.warn(logger, "权限拒绝: {}", ex.getMessage());

        Map<String, Object> errorResponse = Map.of(
                "error", "ACCESS_DENIED",
                "message", "权限不足，拒绝访问",
                "timestamp", LocalDateTime.now());

        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse));
    }

    /**
     * 处理参数验证异常（Map格式响应）
     *
     * @param ex     参数验证异常
     * @param logger 日志记录器
     * @return 参数验证异常响应的Mono包装
     */
    public static Mono<ResponseEntity<Map<String, Object>>> handleValidationExceptionMap(
            MethodArgumentNotValidException ex,
            Logger logger) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        LoggingUtil.warn(logger, "参数验证失败: {}", errorMessage);

        Map<String, Object> errorResponse = Map.of(
                "error", "VALIDATION_ERROR",
                "message", "参数验证失败: " + errorMessage,
                "timestamp", LocalDateTime.now());

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    // ==================== 异常处理辅助方法 ====================

    /**
     * 获取异常的根本原因
     *
     * @param throwable 异常
     * @return 根本原因异常
     */
    public static Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * 获取异常的简化消息
     *
     * @param throwable 异常
     * @return 简化的异常消息
     */
    public String getSimplifiedMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return throwable.getClass().getSimpleName();
        }

        // 截取过长的消息 - 使用配置的字符串长度限制
        int maxLength = monitoringProperties.getConstants().getShortStringLength();
        if (message.length() > maxLength) {
            return message.substring(0, maxLength) + "...";
        }

        return message;
    }

    /**
     * 判断是否为业务异常
     *
     * @param throwable 异常
     * @return 是否为业务异常
     */
    public static boolean isBusinessException(Throwable throwable) {
        return throwable instanceof BusinessException ||
                throwable instanceof IllegalArgumentException ||
                throwable instanceof IllegalStateException;
    }

    /**
     * 判断是否为系统异常
     *
     * @param throwable 异常
     * @return 是否为系统异常
     */
    public static boolean isSystemException(Throwable throwable) {
        return throwable instanceof RuntimeException ||
                throwable instanceof DataAccessException ||
                throwable instanceof NullPointerException;
    }
}
