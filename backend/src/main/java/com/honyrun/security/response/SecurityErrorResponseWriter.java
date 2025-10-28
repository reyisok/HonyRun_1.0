package com.honyrun.security.response;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.util.TraceIdUtil;

/**
 * 统一安全错误响应写入器
 * 将401/403/404等安全相关错误统一为一致的JSON响应格式。
 */
public final class SecurityErrorResponseWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SecurityErrorResponseWriter() {}

    public static reactor.core.publisher.Mono<Void> writeUnauthorized(ServerWebExchange exchange, String message) {
        return write(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", 401, message);
    }

    public static reactor.core.publisher.Mono<Void> writeForbidden(ServerWebExchange exchange, String message) {
        return write(exchange, HttpStatus.FORBIDDEN, "Forbidden", 403, message);
    }

    public static reactor.core.publisher.Mono<Void> writeNotFound(ServerWebExchange exchange, String message) {
        return write(exchange, HttpStatus.NOT_FOUND, "Not Found", 404, message);
    }

    private static reactor.core.publisher.Mono<Void> write(ServerWebExchange exchange,
                                                           HttpStatus status,
                                                           String error,
                                                           int code,
                                                           String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        // 解析或生成 traceId，并写入响应头
        String headerTraceId = exchange.getRequest().getHeaders().getFirst(TraceIdUtil.X_TRACE_ID_HEADER);
        String traceId = (headerTraceId != null && !headerTraceId.isBlank())
                ? headerTraceId
                : TraceIdUtil.generateTraceId();
        exchange.getResponse().getHeaders().set(TraceIdUtil.X_TRACE_ID_HEADER, traceId);

        // 路径与详情
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "UNKNOWN";
        Map<String, Object> details = new HashMap<>();
        details.put("errorType", error);
        details.put("status", code);
        details.put("path", path);
        details.put("method", method);

        // 使用统一的ApiResponse错误结构，确保包含timestamp、traceId、details、path
        ApiResponse<Object> body = ApiResponse.error(String.valueOf(code), message, traceId, details, path);
        try {
            byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(body);
            org.springframework.core.io.buffer.DataBuffer buffer = exchange.getResponse()
                    .bufferFactory()
                    .wrap(bytes);
            return exchange.getResponse().writeWith(reactor.core.publisher.Mono.just(buffer));
        } catch (Exception e) {
            // 序列化异常回退为简化JSON，尽量包含timestamp、traceId与path
            String fallback = String.format("{\"success\":false,\"code\":\"%d\",\"message\":\"%s\",\"data\":null,\"error\":{\"code\":\"%d\",\"message\":\"%s\"},\"timestamp\":\"%s\",\"traceId\":\"%s\",\"path\":\"%s\"}",
                    code, message, code, message, java.time.LocalDateTime.now(), traceId, path);
            org.springframework.core.io.buffer.DataBuffer buffer = exchange.getResponse()
                    .bufferFactory()
                    .wrap(fallback.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(reactor.core.publisher.Mono.just(buffer));
        }
    }
}
