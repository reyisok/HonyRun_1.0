package com.honyrun.exception.handler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.honyrun.exception.AuthenticationException;
import com.honyrun.exception.BusinessException;
import com.honyrun.exception.DataAccessException;
import com.honyrun.exception.ErrorCode;
import com.honyrun.exception.ExternalServiceException;
import com.honyrun.exception.RateLimitException;
import com.honyrun.exception.SystemException;
import com.honyrun.exception.ValidationException;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.util.ErrorDetailsUtil;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.TraceIdUtil;

import reactor.core.publisher.Mono;

/**
 * Web异常处理器
 * 实现HTTP异常处理和状态码映射
 *
 * 统一使用ApiResponse格式返回错误响应，确保与ReactiveGlobalExceptionFilter一致。
 * 支持traceId追踪、环境相关的错误详细信息控制。
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @modified 2025-07-02 20:50:00
 * @version 2.1.0
 *          Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
@Order(-2)
public class WebExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebExceptionHandler.class);
    private final ObjectMapper objectMapper;
    private final ErrorDetailsUtil errorDetailsUtil;

    /**
     * 构造函数注入
     *
     * @param objectMapper     JSON对象映射器
     * @param errorDetailsUtil 错误详情工具
     */
    public WebExceptionHandler(ObjectMapper objectMapper, ErrorDetailsUtil errorDetailsUtil) {
        this.objectMapper = objectMapper;
        this.errorDetailsUtil = errorDetailsUtil;
    }

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return Mono.deferContextual(ctxView -> {
            String contextTraceId = com.honyrun.util.TraceIdUtil.getTraceIdFromContext(ctxView).orElse(null);
            String headerTraceId = exchange.getRequest().getHeaders().getFirst(TraceIdUtil.X_TRACE_ID_HEADER);
            String traceId = contextTraceId != null && !contextTraceId.isBlank()
                    ? contextTraceId
                    : (headerTraceId != null && !headerTraceId.isBlank()
                            ? headerTraceId
                            : TraceIdUtil.generateTraceId());

            response.getHeaders().set(TraceIdUtil.X_TRACE_ID_HEADER, traceId);

            try {
                ApiResponse<Void> errorResponse = buildApiErrorResponse(ex, exchange, traceId);

                logException(ex, exchange, traceId);

                HttpStatus status = determineHttpStatus(ex);
                response.setStatusCode(status);

                String responseBody = objectMapper.writeValueAsString(errorResponse);

                String path = exchange.getRequest().getPath().value();
                String exType = ex.getClass().getSimpleName();
                Map<String, Object> details = errorResponse.getDetails();
                if (status == HttpStatus.BAD_REQUEST) {
                    LoggingUtil.warn(logger,
                            "异常响应(400) - Path: {}, Status: {}, Exception: {}, Details: {}, Body: {}",
                            path, status.value(), exType, details, responseBody);
                } else {
                    LoggingUtil.error(logger,
                            "异常响应 - Path: {}, Status: {}, Exception: {}, Body: {}",
                            path, status.value(), exType, responseBody);
                }

                DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes());
                return response.writeWith(Mono.just(buffer));
            } catch (JsonProcessingException e) {
                LoggingUtil.error(logger, "序列化错误响应失败", e);
                return response.writeWith(Mono.empty());
            }
        });
    }

    private ApiResponse<Void> buildApiErrorResponse(Throwable ex, ServerWebExchange exchange, String traceId) {
        String path = exchange.getRequest().getPath().value();
        Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, path);

        if (ex instanceof BusinessException) {
            BusinessException businessEx = (BusinessException) ex;
            String code = businessEx.getErrorCode() != null ? String.valueOf(businessEx.getErrorCode().getCode())
                    : String.valueOf(ErrorCode.BUSINESS_ERROR.getCode());
            String message = businessEx.getMessage() != null ? businessEx.getMessage()
                    : ErrorCode.BUSINESS_ERROR.getMessage();
            return ApiResponse.error(code, message, traceId, details, path);
        } else if (ex instanceof SystemException) {
            SystemException systemEx = (SystemException) ex;
            String code = systemEx.getErrorCode() != null ? String.valueOf(systemEx.getErrorCode().getCode())
                    : String.valueOf(ErrorCode.SYSTEM_ERROR.getCode());
            String message = systemEx.getMessage() != null ? systemEx.getMessage()
                    : ErrorCode.SYSTEM_ERROR.getMessage();
            return ApiResponse.error(code, message, traceId, details, path);
        } else if (ex instanceof ValidationException) {
            ValidationException vex = (ValidationException) ex;
            details.put("fieldErrors", vex.getFieldErrors());
            String code = String.valueOf(
                    vex.getErrorCode() != null ? vex.getErrorCode().getCode() : ErrorCode.VALIDATION_ERROR.getCode());
            String message = vex.getMessage() != null ? vex.getMessage() : ErrorCode.VALIDATION_ERROR.getMessage();
            return ApiResponse.error(code, message, traceId, details, path);
        } else if (ex instanceof AuthenticationException) {
            AuthenticationException aex = (AuthenticationException) ex;
            String code = String.valueOf(aex.getErrorCode() != null ? aex.getErrorCode().getCode()
                    : ErrorCode.AUTHENTICATION_FAILED.getCode());
            String message = aex.getMessage() != null ? aex.getMessage() : ErrorCode.AUTHENTICATION_FAILED.getMessage();
            return ApiResponse.error(code, message, traceId, details, path);
        } else if (ex instanceof DataAccessException) {
            DataAccessException dex = (DataAccessException) ex;
            String code = String.valueOf(
                    dex.getErrorCode() != null ? dex.getErrorCode().getCode() : ErrorCode.DATABASE_ERROR.getCode());
            String message = dex.getMessage() != null ? dex.getMessage() : ErrorCode.DATABASE_ERROR.getMessage();
            return ApiResponse.error(code, message, traceId, details, path);
        } else if (ex instanceof NotAcceptableStatusException) {
            return ApiResponse.error(
                    String.valueOf(ErrorCode.INVALID_REQUEST.getCode()),
                    ErrorCode.INVALID_REQUEST.getMessage(),
                    traceId,
                    details,
                    path);
        } else if (ex instanceof org.springframework.web.server.UnsupportedMediaTypeStatusException) {
            return ApiResponse.error(
                    String.valueOf(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode()),
                    ErrorCode.UNSUPPORTED_MEDIA_TYPE.getMessage(),
                    traceId,
                    details,
                    path);
        } else if (ex instanceof org.springframework.web.server.MethodNotAllowedException) {
            return ApiResponse.error(
                    String.valueOf(ErrorCode.METHOD_NOT_ALLOWED.getCode()),
                    ErrorCode.METHOD_NOT_ALLOWED.getMessage(),
                    traceId,
                    details,
                    path);
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException statusEx = (ResponseStatusException) ex;
            String message = statusEx.getReason() != null ? statusEx.getReason() : "请求处理失败";
            return ApiResponse.error(
                    String.valueOf(statusEx.getStatusCode().value()),
                    message,
                    traceId, details, path);
        } else {
            return ApiResponse.error(String.valueOf(ErrorCode.INTERNAL_SERVER_ERROR.getCode()),
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), traceId, details, path);
        }
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof BusinessException) {
            BusinessException b = (BusinessException) ex;
            return b.getErrorCode() != null ? b.getErrorCode().getHttpStatus() : HttpStatus.BAD_REQUEST;
        } else if (ex instanceof SystemException) {
            SystemException s = (SystemException) ex;
            return s.getErrorCode() != null ? s.getErrorCode().getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        } else if (ex instanceof ValidationException) {
            ValidationException v = (ValidationException) ex;
            return v.getErrorCode() != null ? v.getErrorCode().getHttpStatus() : HttpStatus.BAD_REQUEST;
        } else if (ex instanceof AuthenticationException) {
            AuthenticationException a = (AuthenticationException) ex;
            return a.getErrorCode() != null ? a.getErrorCode().getHttpStatus() : HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof DataAccessException) {
            DataAccessException d = (DataAccessException) ex;
            return d.getErrorCode() != null ? d.getErrorCode().getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        } else if (ex instanceof ExternalServiceException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex instanceof RateLimitException) {
            return HttpStatus.TOO_MANY_REQUESTS;
        } else if (ex instanceof NotAcceptableStatusException) {
            return HttpStatus.NOT_ACCEPTABLE;
        } else if (ex instanceof org.springframework.web.server.UnsupportedMediaTypeStatusException) {
            return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        } else if (ex instanceof org.springframework.web.server.MethodNotAllowedException) {
            return HttpStatus.METHOD_NOT_ALLOWED;
        } else if (ex instanceof ResponseStatusException) {
            return HttpStatus.valueOf(((ResponseStatusException) ex).getStatusCode().value());
        } else if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof IllegalStateException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private void logException(Throwable ex, ServerWebExchange exchange, String traceId) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        String remoteAddress = getRemoteAddress(exchange);

        if (ex instanceof BusinessException) {
            LoggingUtil.warn(logger, "业务异常 - Path: {}, Method: {}, RemoteAddress: {}, Message: {}",
                    path, method, remoteAddress, ex.getMessage());
        } else if (ex instanceof SystemException) {
            LoggingUtil.error(logger, "系统异常 - Path: {}, Method: {}, RemoteAddress: {}",
                    ex, path, method, remoteAddress);
        } else if (ex instanceof AuthenticationException) {
            LoggingUtil.warn(logger, "认证异常 - Path: {}, Method: {}, RemoteAddress: {}, Message: {}",
                    path, method, remoteAddress, ex.getMessage());
        } else if (ex instanceof DataAccessException) {
            LoggingUtil.error(logger, "数据访问异常 - Path: {}, Method: {}, RemoteAddress: {}",
                    ex, path, method, remoteAddress);
        } else {
            LoggingUtil.error(logger, "未知异常 - Path: {}, Method: {}, RemoteAddress: {}",
                    ex, path, method, remoteAddress);
        }
    }

    private String getRemoteAddress(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        var remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null && remoteAddress.getAddress() != null ? remoteAddress.getAddress().getHostAddress()
                : "unknown";
    }
}
