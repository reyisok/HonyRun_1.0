package com.honyrun.util;

import com.honyrun.model.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

/**
 * 统一响应处理工具类
 *
 * 提供统一的响应处理方法，减少控制器中重复的响应处理逻辑。
 * 包括成功响应、错误响应、异常处理等常用响应模式的封装。
 *
 * 主要功能：
 * - 统一成功响应格式
 * - 统一错误响应格式
 * - 异常处理响应
 * - 日志记录集成
 * - 响应式数据流处理
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 16:00:00
 * @modified 2025-07-01 16:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class ResponseUtil {

    /**
     * 创建成功响应
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应的ResponseEntity
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 创建成功响应（无数据）
     *
     * @return 成功响应的ResponseEntity
     */
    public static ResponseEntity<ApiResponse<Void>> success() {
        return ResponseEntity.ok(ApiResponse.success("操作成功"));
    }

    /**
     * 创建成功响应的Mono
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应的Mono包装
     */
    public static <T> Mono<ResponseEntity<ApiResponse<T>>> successMono(T data) {
        return Mono.just(success(data));
    }

    /**
     * 创建成功响应的Mono（无数据）
     *
     * @return 成功响应的Mono包装
     */
    public static Mono<ResponseEntity<ApiResponse<Void>>> successMono() {
        return Mono.just(success());
    }

    /**
     * 创建错误响应
     *
     * @param message 错误消息
     * @param status HTTP状态码
     * @param <T> 数据类型
     * @return 错误响应的ResponseEntity
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(String message, HttpStatus status) {
        // 同步场景下无法访问Reactor Context，作为回退生成新的TraceId
        String traceId = com.honyrun.util.TraceIdUtil.generateTraceId();
        // 简化错误详情，不依赖ErrorDetailsUtil构造函数
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        details.put("errorType", "RuntimeException");
        details.put("requestPath", "ResponseUtil.error");
        details.put("timestamp", java.time.LocalDateTime.now().toString());
        details.put("message", message);
        
        return ResponseEntity.status(status).body(
            ApiResponse.error(String.valueOf(status.value()), message, traceId, details, null)
        );
    }

    /**
     * 创建错误响应的Mono
     *
     * @param message 错误消息
     * @param status HTTP状态码
     * @param <T> 数据类型
     * @return 错误响应的Mono包装
     */
    public static <T> Mono<ResponseEntity<ApiResponse<T>>> errorMono(String message, HttpStatus status) {
        // 在响应式场景下优先复用Reactor Context中的TraceId
        return Mono.deferContextual(ctxView -> {
            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
            // 简化错误详情，不依赖ErrorDetailsUtil构造函数
            java.util.Map<String, Object> details = new java.util.HashMap<>();
            details.put("errorType", "RuntimeException");
            details.put("requestPath", "ResponseUtil.errorMono");
            details.put("timestamp", java.time.LocalDateTime.now().toString());
            details.put("message", message);
            
            ApiResponse<T> body = ApiResponse.error(String.valueOf(status.value()), message, traceId, details, null);
            return Mono.just(ResponseEntity.status(status).body(body));
        });
    }

    /**
     * 处理Mono响应，包含日志记录
     *
     * @param mono 原始Mono
     * @param logger 日志记录器
     * @param successMessage 成功日志消息
     * @param errorMessage 错误日志消息
     * @param <T> 数据类型
     * @return 处理后的响应Mono
     */
    public static <T> Mono<ResponseEntity<ApiResponse<T>>> handleMono(
            Mono<T> mono, 
            Logger logger, 
            String successMessage, 
            String errorMessage) {
        
        return mono
                .map(data -> {
                    LoggingUtil.info(logger, successMessage);
                    return success(data);
                })
                .doOnError(error -> LoggingUtil.error(logger, errorMessage, error))
                .onErrorResume(error -> Mono.deferContextual(ctxView -> {
                    String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                    // 简化错误详情，不依赖ErrorDetailsUtil构造函数
                    java.util.Map<String, Object> details = new java.util.HashMap<>();
                    details.put("errorType", error.getClass().getSimpleName());
                    details.put("requestPath", "ResponseUtil.handleMono");
                    details.put("timestamp", java.time.LocalDateTime.now().toString());
                    details.put("message", error.getMessage());
                    
                    ApiResponse<T> body = ApiResponse.error(
                            String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                            errorMessage,
                            traceId,
                            details,
                            null
                    );
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
                }));
    }

    /**
     * 处理Mono响应，包含自定义成功处理
     *
     * @param mono 原始Mono
     * @param logger 日志记录器
     * @param successHandler 成功处理函数
     * @param errorMessage 错误日志消息
     * @param <T> 数据类型
     * @param <R> 响应类型
     * @return 处理后的响应Mono
     */
    public static <T, R> Mono<ResponseEntity<ApiResponse<R>>> handleMonoWithCustomSuccess(
            Mono<T> mono,
            Logger logger,
            Function<T, ResponseEntity<ApiResponse<R>>> successHandler,
            String errorMessage) {
        
        return mono
                .map(successHandler)
                .doOnError(error -> LoggingUtil.error(logger, errorMessage, error))
                .onErrorResume(error -> Mono.deferContextual(ctxView -> {
                    String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                    // 简化错误详情，不依赖ErrorDetailsUtil构造函数
                    java.util.Map<String, Object> details = new java.util.HashMap<>();
                    details.put("errorType", error.getClass().getSimpleName());
                    details.put("requestPath", "ResponseUtil.handleMonoWithCustomSuccess");
                    details.put("timestamp", java.time.LocalDateTime.now().toString());
                    details.put("message", error.getMessage());
                    
                    ApiResponse<R> body = ApiResponse.error(
                            String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                            errorMessage,
                            traceId,
                            details,
                            null
                    );
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
                }));
    }

    /**
     * 处理Flux响应，包含日志记录
     *
     * @param flux 原始Flux
     * @param logger 日志记录器
     * @param successMessage 成功日志消息
     * @param errorMessage 错误日志消息
     * @param <T> 数据类型
     * @return 处理后的响应Mono
     */
    public static <T> Mono<ResponseEntity<ApiResponse<Flux<T>>>> handleFlux(
            Flux<T> flux,
            Logger logger,
            String successMessage,
            String errorMessage) {
        
        return Mono.just(flux)
                .map(data -> {
                    LoggingUtil.info(logger, successMessage);
                    return success(data);
                })
                .doOnError(error -> LoggingUtil.error(logger, errorMessage, error))
                .onErrorResume(error -> Mono.deferContextual(ctxView -> {
                    String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                    // 简化错误详情，不依赖ErrorDetailsUtil构造函数
                    java.util.Map<String, Object> details = new java.util.HashMap<>();
                    details.put("errorType", error.getClass().getSimpleName());
                    details.put("requestPath", "ResponseUtil.handleFlux");
                    details.put("timestamp", java.time.LocalDateTime.now().toString());
                    details.put("message", error.getMessage());
                    
                    ApiResponse<Flux<T>> body = ApiResponse.error(
                            String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                            errorMessage,
                            traceId,
                            details,
                            null
                    );
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
                }));
    }

    /**
     * 处理异常响应
     *
     * @param throwable 异常
     * @param logger 日志记录器
     * @param <T> 数据类型
     * @return 异常响应的Mono包装
     */
    public static <T> Mono<ResponseEntity<ApiResponse<T>>> handleException(
            Throwable throwable, 
            Logger logger) {
        
        LoggingUtil.error(logger, "处理请求时发生异常", throwable);

        String message = throwable.getMessage() != null ? throwable.getMessage() : "系统内部错误";
        return Mono.deferContextual(ctxView -> {
            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
            // 简化错误详情，不依赖ErrorDetailsUtil构造函数
            java.util.Map<String, Object> details = new java.util.HashMap<>();
            details.put("errorType", throwable.getClass().getSimpleName());
            details.put("requestPath", "ResponseUtil.handleException");
            details.put("timestamp", java.time.LocalDateTime.now().toString());
            details.put("message", throwable.getMessage());
            
            ApiResponse<T> body = ApiResponse.error(
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    message,
                    traceId,
                    details,
                    null
            );
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
        });
    }

    /**
     * 创建业务异常响应
     *
     * @param message 异常消息
     * @param logger 日志记录器
     * @param <T> 数据类型
     * @return 业务异常响应的Mono包装
     */
    public static <T> Mono<ResponseEntity<ApiResponse<T>>> handleBusinessException(
            String message, 
            Logger logger) {
        
        LoggingUtil.warn(logger, "业务异常: {}", message);
        return errorMono(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * 创建参数验证异常响应
     *
     * @param message 验证错误消息
     * @param logger 日志记录器
     * @param <T> 数据类型
     * @return 参数验证异常响应的Mono包装
     */
    public static <T> Mono<ResponseEntity<ApiResponse<T>>> handleValidationException(
            String message, 
            Logger logger) {
        
        LoggingUtil.warn(logger, "参数验证失败: {}", message);
        return errorMono(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * 创建权限异常响应
     *
     * @param message 权限错误消息
     * @param logger 日志记录器
     * @param <T> 数据类型
     * @return 权限异常响应的Mono包装
     */
    public static <T> Mono<ResponseEntity<ApiResponse<T>>> handlePermissionException(
            String message, 
            Logger logger) {
        
        LoggingUtil.warn(logger, "权限验证失败: {}", message);
        return errorMono(message, HttpStatus.FORBIDDEN);
    }

    /**
     * 创建资源未找到异常响应
     *
     * @param message 未找到错误消息
     * @param logger 日志记录器
     * @param <T> 数据类型
     * @return 资源未找到异常响应的Mono包装
     */
    public static <T> Mono<ResponseEntity<ApiResponse<T>>> handleNotFoundException(
            String message, 
            Logger logger) {
        
        LoggingUtil.warn(logger, "资源未找到: {}", message);
        return errorMono(message, HttpStatus.NOT_FOUND);
    }

    /**
     * 创建通用错误响应Map
     *
     * @param errorCode 错误代码
     * @param message 错误消息
     * @return 错误响应Map
     */
    public static Map<String, Object> createErrorMap(String errorCode, String message) {
        return Map.of(
            "error", errorCode,
            "message", message,
            "timestamp", LocalDateTime.now()
        );
    }

    /**
     * 创建通用错误响应Map的Mono
     *
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param status HTTP状态码
     * @return 错误响应的Mono包装
     */
    public static Mono<ResponseEntity<Map<String, Object>>> createErrorMapMono(
            String errorCode, 
            String message, 
            HttpStatus status) {
        
        Map<String, Object> errorMap = createErrorMap(errorCode, message);
        return Mono.just(ResponseEntity.status(status).body(errorMap));
    }

    /**
     * 处理操作结果，根据结果返回相应响应
     *
     * @param result 操作结果
     * @param successMessage 成功消息
     * @param failureMessage 失败消息
     * @param logger 日志记录器
     * @return 响应的Mono包装
     */
    public static Mono<ResponseEntity<ApiResponse<String>>> handleOperationResult(
            boolean result, 
            String successMessage, 
            String failureMessage, 
            Logger logger) {
        
        if (result) {
            LoggingUtil.info(logger, successMessage);
            return successMono(successMessage);
        } else {
            LoggingUtil.warn(logger, failureMessage);
            return errorMono(failureMessage, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 处理计数结果
     *
     * @param count 计数结果
     * @param logger 日志记录器
     * @param operation 操作描述
     * @return 响应的Mono包装
     */
    public static Mono<ResponseEntity<ApiResponse<Long>>> handleCountResult(
            Long count, 
            Logger logger, 
            String operation) {
        
        LoggingUtil.info(logger, "{}完成，影响记录数: {}", operation, count);
        return successMono(count);
    }
}

