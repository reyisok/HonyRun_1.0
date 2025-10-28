package com.honyrun.filter.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.honyrun.exception.BusinessException;
import com.honyrun.exception.SystemException;
import com.honyrun.exception.AuthenticationException;
import com.honyrun.exception.DataAccessException;
import com.honyrun.exception.ValidationException;
import com.honyrun.exception.ErrorCode;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.TraceIdUtil;
import com.honyrun.util.ErrorDetailsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import jakarta.validation.ConstraintViolationException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 响应式全局异常处理过滤器
 *
 * 符合函数式路由配置的异常处理机制，替代@RestControllerAdvice。
 * 通过WebFilter实现全局异常捕获和统一响应格式处理。
 *
 * 【统一规则】根据HonyRun后端统一接口检查报告要求：
 * 1. 符合"统一采用函数式路由配置"的最佳实践
 * 2. 统一使用ApiResponse<T>格式返回错误响应
 * 3. 支持响应式编程模型，所有方法返回Mono包装的响应
 * 4. 遵循Spring Boot 3最佳实践和响应式编程规范
 * 5. 确保异常处理的一致性和标准化
 *
 * 主要功能：
 * - 统一异常处理策略
 * - 标准化错误响应格式
 * - 异常分类和映射
 * - 错误日志记录
 * - 响应式异常处理
 *
 * 异常处理优先级：
 * 1. 业务异常（BusinessException）
 * 2. 验证异常（ValidationException）
 * 3. 认证异常（AuthenticationException）
 * 4. 数据访问异常（DataAccessException）
 * 5. 系统异常（SystemException）
 * 6. 通用异常（Exception）
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-24 16:01:03
 * @modified 2025-10-24 16:01:03
 * @version 1.0.0
 */
@Component("reactiveGlobalExceptionFilter")
public class ReactiveGlobalExceptionFilter implements WebFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveGlobalExceptionFilter.class);

    private final ErrorDetailsUtil errorDetailsUtil;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数注入
     *
     * @param errorDetailsUtil 错误详情工具
     * @param objectMapper JSON对象映射器
     */
    public ReactiveGlobalExceptionFilter(ErrorDetailsUtil errorDetailsUtil, ObjectMapper objectMapper) {
        this.errorDetailsUtil = errorDetailsUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(throwable -> handleException(exchange, throwable));
    }

    @Override
    public int getOrder() {
        // 设置最高优先级，确保能捕获所有异常
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * 统一异常处理入口
     */
    private Mono<Void> handleException(ServerWebExchange exchange, Throwable throwable) {
        return TraceIdUtil.getCurrentTraceId()
                .flatMap(traceId -> {
                    String requestPath = exchange.getRequest().getURI().getPath();

                    LoggingUtil.info(logger, "全局异常处理 - 路径: {}, 异常类型: {}, TraceId: {}",
                            requestPath, throwable.getClass().getSimpleName(), traceId);

                    // 根据异常类型进行分类处理
                    if (throwable instanceof BusinessException) {
                        return handleBusinessException(exchange, (BusinessException) throwable);
                    } else if (throwable instanceof ValidationException) {
                        return handleValidationException(exchange, (ValidationException) throwable);
                    } else if (throwable instanceof AuthenticationException) {
                        return handleAuthenticationException(exchange, (AuthenticationException) throwable);
                    } else if (throwable instanceof DataAccessException) {
                        return handleDataAccessException(exchange, (DataAccessException) throwable);
                    } else if (throwable instanceof SystemException) {
                        return handleSystemException(exchange, (SystemException) throwable);
                    } else if (throwable instanceof WebExchangeBindException) {
                        return handleWebExchangeBindException(exchange, (WebExchangeBindException) throwable);
                    } else if (throwable instanceof MethodArgumentNotValidException) {
                        return handleMethodArgumentNotValidException(exchange, (MethodArgumentNotValidException) throwable);
                    } else if (throwable instanceof ConstraintViolationException) {
                        return handleConstraintViolationException(exchange, (ConstraintViolationException) throwable);
                    } else if (throwable instanceof AccessDeniedException) {
                        return handleAccessDeniedException(exchange, (AccessDeniedException) throwable);
                    } else if (throwable instanceof ExpiredJwtException) {
                        return handleExpiredJwtException(exchange, (ExpiredJwtException) throwable);
                    } else if (throwable instanceof MalformedJwtException) {
                        return handleMalformedJwtException(exchange, (MalformedJwtException) throwable);
                    } else if (throwable instanceof SignatureException) {
                        return handleSignatureException(exchange, (SignatureException) throwable);
                    } else if (throwable instanceof UnsupportedJwtException) {
                        return handleUnsupportedJwtException(exchange, (UnsupportedJwtException) throwable);
                    } else if (throwable instanceof DataIntegrityViolationException) {
                        return handleDataIntegrityViolationException(exchange, (DataIntegrityViolationException) throwable);
                    } else if (throwable instanceof ResponseStatusException) {
                        return handleResponseStatusException(exchange, (ResponseStatusException) throwable);
                    } else {
                        return handleGenericException(exchange, throwable);
                    }
                });
    }

    // ========================================
    // 业务异常处理
    // ========================================

    private Mono<Void> handleBusinessException(ServerWebExchange exchange, BusinessException ex) {
        return buildErrorResponse(exchange, ex.getErrorCode(), ex.getMessage(),
                HttpStatus.BAD_REQUEST, "WARN", ex);
    }

    // ========================================
    // 验证异常处理
    // ========================================

    private Mono<Void> handleValidationException(ServerWebExchange exchange, ValidationException ex) {
        return buildErrorResponse(exchange, ex.getErrorCode(), ex.getMessage(),
                HttpStatus.BAD_REQUEST, "WARN", ex);
    }

    private Mono<Void> handleWebExchangeBindException(ServerWebExchange exchange, WebExchangeBindException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildErrorResponse(exchange, ErrorCode.VALIDATION_ERROR,
                "请求参数验证失败: " + errorMessage, HttpStatus.BAD_REQUEST, "WARN", ex);
    }

    private Mono<Void> handleMethodArgumentNotValidException(ServerWebExchange exchange, MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return buildErrorResponse(exchange, ErrorCode.VALIDATION_ERROR,
                "方法参数验证失败: " + errorMessage, HttpStatus.BAD_REQUEST, "WARN", ex);
    }

    private Mono<Void> handleConstraintViolationException(ServerWebExchange exchange, ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        return buildErrorResponse(exchange, ErrorCode.VALIDATION_ERROR,
                "约束验证失败: " + errorMessage, HttpStatus.BAD_REQUEST, "WARN", ex);
    }

    // ========================================
    // 认证和授权异常处理
    // ========================================

    private Mono<Void> handleAuthenticationException(ServerWebExchange exchange, AuthenticationException ex) {
        return buildErrorResponse(exchange, ex.getErrorCode(), ex.getMessage(),
                HttpStatus.UNAUTHORIZED, "WARN", ex);
    }

    private Mono<Void> handleAccessDeniedException(ServerWebExchange exchange, AccessDeniedException ex) {
        return buildErrorResponse(exchange, ErrorCode.FORBIDDEN, "访问被拒绝",
                HttpStatus.FORBIDDEN, "WARN", ex);
    }

    private Mono<Void> handleExpiredJwtException(ServerWebExchange exchange, ExpiredJwtException ex) {
        return buildErrorResponse(exchange, ErrorCode.TOKEN_EXPIRED, "JWT令牌已过期",
                HttpStatus.UNAUTHORIZED, "WARN", ex);
    }

    private Mono<Void> handleMalformedJwtException(ServerWebExchange exchange, MalformedJwtException ex) {
        return buildErrorResponse(exchange, ErrorCode.INVALID_TOKEN, "JWT令牌格式错误",
                HttpStatus.UNAUTHORIZED, "WARN", ex);
    }

    private Mono<Void> handleSignatureException(ServerWebExchange exchange, SignatureException ex) {
        return buildErrorResponse(exchange, ErrorCode.INVALID_TOKEN, "JWT令牌签名无效",
                HttpStatus.UNAUTHORIZED, "WARN", ex);
    }

    private Mono<Void> handleUnsupportedJwtException(ServerWebExchange exchange, UnsupportedJwtException ex) {
        return buildErrorResponse(exchange, ErrorCode.INVALID_TOKEN, "不支持的JWT令牌",
                HttpStatus.UNAUTHORIZED, "WARN", ex);
    }

    // ========================================
    // 数据访问异常处理
    // ========================================

    private Mono<Void> handleDataAccessException(ServerWebExchange exchange, DataAccessException ex) {
        return buildErrorResponse(exchange, ex.getErrorCode(), ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR, "ERROR", ex);
    }

    private Mono<Void> handleDataIntegrityViolationException(ServerWebExchange exchange, DataIntegrityViolationException ex) {
        return buildErrorResponse(exchange, ErrorCode.DATA_INTEGRITY_ERROR, "数据完整性约束违反",
                HttpStatus.CONFLICT, "WARN", ex);
    }

    // ========================================
    // 系统异常处理
    // ========================================

    private Mono<Void> handleSystemException(ServerWebExchange exchange, SystemException ex) {
        return buildErrorResponse(exchange, ex.getErrorCode(), ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR, "ERROR", ex);
    }

    private Mono<Void> handleResponseStatusException(ServerWebExchange exchange, ResponseStatusException ex) {
        ErrorCode errorCode = mapStatusToErrorCode(HttpStatus.valueOf(ex.getStatusCode().value()));
        return buildErrorResponse(exchange, errorCode, ex.getReason(),
                HttpStatus.valueOf(ex.getStatusCode().value()), "WARN", ex);
    }

    private Mono<Void> handleGenericException(ServerWebExchange exchange, Throwable ex) {
        return buildErrorResponse(exchange, ErrorCode.INTERNAL_SERVER_ERROR, "系统内部错误",
                HttpStatus.INTERNAL_SERVER_ERROR, "ERROR", ex);
    }

    // ========================================
    // 通用错误响应构建
    // ========================================

    /**
     * 构建错误响应
     */
    private Mono<Void> buildErrorResponse(ServerWebExchange exchange, ErrorCode errorCode,
                                        String message, HttpStatus status, String logLevel, Throwable ex) {
        return TraceIdUtil.getCurrentTraceId()
                .flatMap(traceId -> {
                    String requestPath = exchange.getRequest().getURI().getPath();

                    // 记录日志
                    switch (logLevel) {
                        case "ERROR":
                            LoggingUtil.error(logger, "异常处理 - 路径: {}, 错误码: {}, 消息: {}, TraceId: {}",
                                    requestPath, errorCode.getCode(), message, traceId, ex);
                            break;
                        case "WARN":
                            LoggingUtil.warn(logger, "异常处理 - 路径: {}, 错误码: {}, 消息: {}, TraceId: {}",
                                    requestPath, errorCode.getCode(), message, traceId);
                            break;
                        default:
                            LoggingUtil.info(logger, "异常处理 - 路径: {}, 错误码: {}, 消息: {}, TraceId: {}",
                                    requestPath, errorCode.getCode(), message, traceId);
                    }

                    // 构建错误详情 - 使用响应式版本
                    Mono<Map<String, Object>> errorDetailsMono = Mono.empty();
                    if (errorDetailsUtil != null) {
                        errorDetailsMono = errorDetailsUtil.buildErrorDetailsReactive(ex, requestPath)
                                .onErrorResume(e -> {
                                    LoggingUtil.warn(logger, "构建错误详情失败: {}", e.getMessage());
                                    return Mono.empty();
                                });
                    }

                    return errorDetailsMono
                            .defaultIfEmpty(null)
                            .flatMap(errorDetails -> {
                                // 构建API响应
                                ApiResponse<Void> apiResponse = ApiResponse.error(String.valueOf(errorCode.getCode()), message, traceId, errorDetails, requestPath);

                                // 设置响应头和状态码
                                ServerHttpResponse response = exchange.getResponse();
                                response.setStatusCode(status);
                                response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                                response.getHeaders().add("X-Trace-Id", traceId);

                                // 序列化响应体
                                try {
                                    String responseBody = objectMapper.writeValueAsString(apiResponse);
                                    DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));
                                    return response.writeWith(Mono.just(buffer));
                                } catch (JsonProcessingException e) {
                                    LoggingUtil.error(logger, "序列化异常响应失败: {}", e.getMessage(), e);
                                    return response.setComplete();
                                }
                            });
                 });
     }

    /**
     * 将HTTP状态码映射到错误码
     */
    private ErrorCode mapStatusToErrorCode(HttpStatus status) {
        if (status == HttpStatus.NOT_FOUND) return ErrorCode.NOT_FOUND;
        if (status == HttpStatus.METHOD_NOT_ALLOWED) return ErrorCode.METHOD_NOT_ALLOWED;
        if (status == HttpStatus.UNSUPPORTED_MEDIA_TYPE) return ErrorCode.UNSUPPORTED_MEDIA_TYPE;
        if (status == HttpStatus.FORBIDDEN) return ErrorCode.FORBIDDEN;
        if (status == HttpStatus.UNAUTHORIZED) return ErrorCode.UNAUTHORIZED;
        if (status == HttpStatus.BAD_REQUEST) return ErrorCode.BAD_REQUEST;
        if (status == HttpStatus.NOT_ACCEPTABLE) return ErrorCode.INVALID_REQUEST;
        return ErrorCode.INTERNAL_SERVER_ERROR;
    }
}
