package com.honyrun.util.response;

import com.honyrun.exception.ErrorCode;
import com.honyrun.model.dto.request.PageRequest;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.model.dto.response.PageResponse;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

/**
 * 响应标准化工具类
 *
 * 提供统一的响应格式处理，包括成功响应、错误响应、分页响应等
 * 支持响应式编程和传统编程模式
 *
 * 主要功能：
 * - 统一响应格式标准化
 * - 分页响应处理
 * - 错误响应标准化
 * - 响应式响应包装
 * - HTTP状态码映射
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 14:45:00
 * @modified 2025-07-01 14:45:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class ResponseStandardUtil {

    private static final Logger logger = LoggerFactory.getLogger(ResponseStandardUtil.class);

    /**
     * 私有构造函数，防止实例化
     */
    private ResponseStandardUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== 成功响应方法 ====================

    /**
     * 创建成功响应
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        LoggingUtil.debug(logger, "创建成功响应");
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 创建成功响应（带消息）
     *
     * @param data 响应数据
     * @param message 响应消息
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        LoggingUtil.debug(logger, "创建成功响应: {}", message);
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * 创建成功响应（仅消息）
     *
     * @param message 响应消息
     * @return 成功响应
     */
    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        LoggingUtil.debug(logger, "创建成功响应: {}", message);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    // ==================== 错误响应方法 ====================

    /**
     * 创建错误响应（错误码）
     *
     * @param errorCode 错误码枚举
     * @param <T> 数据类型
     * @return 错误响应
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode) {
        LoggingUtil.warn(logger, "创建错误响应: {} - {}", errorCode.getCode(), errorCode.getMessage());
        String traceId = com.honyrun.util.TraceIdUtil.generateTraceId();
        
        // 简化错误详情，避免依赖注入问题
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        details.put("exception", "RuntimeException");
        details.put("path", "ResponseStandardUtil.error");
        details.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.error(String.valueOf(errorCode.getCode()), errorCode.getMessage(), traceId, details, "ResponseStandardUtil.error"));
    }

    /**
     * 创建错误响应（自定义消息）
     *
     * @param errorCode 错误码枚举
     * @param customMessage 自定义错误消息
     * @param <T> 数据类型
     * @return 错误响应
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode, String customMessage) {
        LoggingUtil.warn(logger, "创建错误响应: {} - {}", errorCode.getCode(), customMessage);
        String traceId = com.honyrun.util.TraceIdUtil.generateTraceId();
        
        // 简化错误详情，避免依赖注入问题
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        details.put("exception", "RuntimeException");
        details.put("message", customMessage);
        details.put("path", "ResponseStandardUtil.error");
        details.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.error(String.valueOf(errorCode.getCode()), customMessage, traceId, details, "ResponseStandardUtil.error"));
    }

    /**
     * 创建错误响应（HTTP状态码）
     *
     * @param httpStatus HTTP状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus httpStatus, String message) {
        LoggingUtil.warn(logger, "创建错误响应: {} - {}", httpStatus.value(), message);
        String traceId = com.honyrun.util.TraceIdUtil.generateTraceId();
        
        // 简化错误详情，避免依赖注入问题
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        details.put("exception", "RuntimeException");
        details.put("message", message);
        details.put("path", "ResponseStandardUtil.error");
        details.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.status(httpStatus)
                .body(ApiResponse.error(String.valueOf(httpStatus.value()), message, traceId, details, "ResponseStandardUtil.error"));
    }

    /**
     * 创建错误响应（带异常信息）
     *
     * @param errorCode 错误码枚举
     * @param exception 异常对象
     * @param <T> 数据类型
     * @return 错误响应
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode, Exception exception) {
        LoggingUtil.error(logger, "创建异常错误响应: {} - {}", errorCode.getCode(), errorCode.getMessage(), exception);
        String traceId = com.honyrun.util.TraceIdUtil.generateTraceId();
        
        // 简化错误详情，避免依赖注入问题
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        details.put("exception", exception.getClass().getSimpleName());
        details.put("message", exception.getMessage());
        details.put("path", "ResponseStandardUtil.error");
        details.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.error(String.valueOf(errorCode.getCode()), errorCode.getMessage(), traceId, details, "ResponseStandardUtil.error"));
    }

    // ==================== 分页响应方法 ====================

    /**
     * 创建分页成功响应
     *
     * @param list 分页数据列表
     * @param total 总记录数
     * @param pageRequest 分页请求
     * @param <T> 数据类型
     * @return 分页成功响应
     */
    public static <T> ResponseEntity<ApiResponse<PageResponse<T>>> pageSuccess(
            List<T> list, Long total, PageRequest pageRequest) {
        LoggingUtil.debug(logger, "创建分页成功响应: 总记录数={}, 当前页={}, 页大小={}", 
                total, pageRequest.getPageNum(), pageRequest.getPageSize());
        
        PageResponse<T> pageResponse = PageResponse.of(list, total, 
                pageRequest.getPageNum(), pageRequest.getPageSize());
        return ResponseEntity.ok(ApiResponse.pageSuccess(pageResponse));
    }

    /**
     * 创建分页成功响应（带消息）
     *
     * @param list 分页数据列表
     * @param total 总记录数
     * @param pageRequest 分页请求
     * @param message 响应消息
     * @param <T> 数据类型
     * @return 分页成功响应
     */
    public static <T> ResponseEntity<ApiResponse<PageResponse<T>>> pageSuccess(
            List<T> list, Long total, PageRequest pageRequest, String message) {
        LoggingUtil.debug(logger, "创建分页成功响应: {} - 总记录数={}, 当前页={}, 页大小={}", 
                message, total, pageRequest.getPageNum(), pageRequest.getPageSize());
        
        PageResponse<T> pageResponse = PageResponse.of(list, total, 
                pageRequest.getPageNum(), pageRequest.getPageSize());
        return ResponseEntity.ok(ApiResponse.pageSuccess(pageResponse, message));
    }

    /**
     * 创建空分页响应
     *
     * @param pageRequest 分页请求
     * @param <T> 数据类型
     * @return 空分页响应
     */
    public static <T> ResponseEntity<ApiResponse<PageResponse<T>>> emptyPage(PageRequest pageRequest) {
        LoggingUtil.debug(logger, "创建空分页响应: 当前页={}, 页大小={}", 
                pageRequest.getPageNum(), pageRequest.getPageSize());
        
        PageResponse<T> pageResponse = PageResponse.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
        return ResponseEntity.ok(ApiResponse.pageSuccess(pageResponse, "暂无数据"));
    }

    // ==================== 响应式响应方法 ====================

    /**
     * 响应式成功响应
     *
     * @param dataMono 响应数据Mono
     * @param <T> 数据类型
     * @return 响应式成功响应
     */
    public static <T> Mono<ResponseEntity<ApiResponse<T>>> reactiveSuccess(Mono<T> dataMono) {
        return dataMono
                .map(data -> {
                    LoggingUtil.debug(logger, "创建响应式成功响应");
                    return ResponseEntity.ok(ApiResponse.success(data));
                })
                .doOnError(error -> LoggingUtil.error(logger, "响应式成功响应处理失败", error));
    }

    /**
     * 响应式成功响应（带消息）
     *
     * @param dataMono 响应数据Mono
     * @param message 响应消息
     * @param <T> 数据类型
     * @return 响应式成功响应
     */
    public static <T> Mono<ResponseEntity<ApiResponse<T>>> reactiveSuccess(Mono<T> dataMono, String message) {
        return dataMono
                .map(data -> {
                    LoggingUtil.debug(logger, "创建响应式成功响应: {}", message);
                    return ResponseEntity.ok(ApiResponse.success(data, message));
                })
                .doOnError(error -> LoggingUtil.error(logger, "响应式成功响应处理失败", error));
    }

    /**
     * 响应式分页成功响应
     *
     * @param dataFlux 数据流
     * @param totalMono 总记录数Mono
     * @param pageRequest 分页请求
     * @param <T> 数据类型
     * @return 响应式分页成功响应
     */
    public static <T> Mono<ResponseEntity<ApiResponse<PageResponse<T>>>> reactivePageSuccess(
            Flux<T> dataFlux, Mono<Long> totalMono, PageRequest pageRequest) {
        
        return Mono.zip(
                dataFlux.collectList(),
                totalMono
        ).map(tuple -> {
            List<T> list = tuple.getT1();
            Long total = tuple.getT2();
            
            LoggingUtil.debug(logger, "创建响应式分页成功响应: 总记录数={}, 当前页={}, 页大小={}", 
                    total, pageRequest.getPageNum(), pageRequest.getPageSize());
            
            PageResponse<T> pageResponse = PageResponse.of(list, total, 
                    pageRequest.getPageNum(), pageRequest.getPageSize());
            return ResponseEntity.ok(ApiResponse.pageSuccess(pageResponse));
        }).doOnError(error -> LoggingUtil.error(logger, "响应式分页成功响应处理失败", error));
    }

    /**
     * 响应式错误响应
     *
     * @param errorCode 错误码枚举
     * @param <T> 数据类型
     * @return 响应式错误响应
     */
    public static <T> Mono<ResponseEntity<ApiResponse<T>>> reactiveError(ErrorCode errorCode) {
        LoggingUtil.warn(logger, "创建响应式错误响应: {} - {}", errorCode.getCode(), errorCode.getMessage());
        return reactor.core.publisher.Mono.deferContextual(ctxView ->
                reactor.core.publisher.Mono.fromCallable(() -> {
                    String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                    // 简化错误详情，避免依赖注入问题
                    java.util.Map<String, Object> details = new java.util.HashMap<>();
                    details.put("exception", "RuntimeException");
                    details.put("message", errorCode.getMessage());
                    details.put("path", "ResponseStandardUtil.reactiveError");
                    details.put("timestamp", java.time.LocalDateTime.now().toString());
                    return ResponseEntity.status(errorCode.getHttpStatus())
                             .<ApiResponse<T>>body(ApiResponse.error(String.valueOf(errorCode.getCode()), errorCode.getMessage(), traceId, details, "ResponseStandardUtil.reactiveError"));
                })
        );
    }

    // ==================== 数据转换方法 ====================

    /**
     * 转换分页数据
     *
     * @param sourceList 源数据列表
     * @param converter 转换函数
     * @param <S> 源数据类型
     * @param <T> 目标数据类型
     * @return 转换后的数据列表
     */
    public static <S, T> List<T> convertPageData(List<S> sourceList, Function<S, T> converter) {
        LoggingUtil.debug(logger, "转换分页数据: 源数据数量={}", sourceList.size());
        return sourceList.stream()
                .map(converter)
                .toList();
    }

    /**
     * 响应式转换分页数据
     *
     * @param sourceFlux 源数据流
     * @param converter 转换函数
     * @param <S> 源数据类型
     * @param <T> 目标数据类型
     * @return 转换后的数据流
     */
    public static <S, T> Flux<T> reactiveConvertPageData(Flux<S> sourceFlux, Function<S, T> converter) {
        LoggingUtil.debug(logger, "响应式转换分页数据");
        return sourceFlux
                .map(converter)
                .doOnComplete(() -> LoggingUtil.debug(logger, "响应式分页数据转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "响应式分页数据转换失败", error));
    }

    // ==================== 参数验证方法 ====================

    /**
     * 验证分页参数
     *
     * @param pageRequest 分页请求
     * @return 验证结果
     */
    public static boolean validatePageRequest(PageRequest pageRequest) {
        if (pageRequest == null) {
            return false;
        }
        
        if (!pageRequest.isValid()) {
            LoggingUtil.warn(logger, "分页请求参数无效: {}", pageRequest);
            return false;
        }
        
        return true;
    }

    /**
     * 验证排序参数
     *
     * @param pageRequest 分页请求
     * @return 验证结果
     */
    public static boolean validateSortRequest(PageRequest pageRequest) {
        if (pageRequest == null) {
            return false;
        }
        
        if (!pageRequest.isValidSort()) {
            LoggingUtil.warn(logger, "排序请求参数无效: {}", pageRequest);
            return false;
        }
        
        return true;
    }
}

