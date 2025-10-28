package com.honyrun.interceptor;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;


/**
 * 响应式日志拦截器
 *
 * 基于Spring WebFlux的响应式日志拦截器，用于记录请求和响应的详细信息。
 * 该拦截器采用非阻塞I/O模式，支持结构化日志记录和性能监控。
 *
 * 特性：
 * - 非阻塞日志记录
 * - 请求响应链路追踪
 * - 性能指标收集
 * - 结构化日志输出
 * - 背压支持
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  17:50:00
 * @modified 2025-07-01 17:50:00
 * @version 2.0.0
 */
@Component
public class ReactiveLoggingInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveLoggingInterceptor.class);
    private static final String REQUEST_START_TIME_KEY = "request.start.time";

    /**
     * 拦截请求进行日志记录
     *
     * @param exchange 服务器Web交换对象
     * @return 处理结果的Mono
     */
    public Mono<Void> intercept(@NonNull ServerWebExchange exchange) {
        return logRequest(exchange)
                .then(Mono.fromRunnable(() -> setupRequestContext(exchange)));
    }

    /**
     * 记录请求信息
     *
     * @param exchange 服务器Web交换对象
     * @return 处理结果的Mono
     */
    private Mono<Void> logRequest(ServerWebExchange exchange) {
        return Mono.fromRunnable(() -> {
            LocalDateTime startTime = LocalDateTime.now();

            // 设置请求上下文（仅记录开始时间，TraceId由上游过滤器统一处理）
            exchange.getAttributes().put(REQUEST_START_TIME_KEY, startTime);

            // 获取请求信息
            String method = exchange.getRequest().getMethod().name();
            String path = exchange.getRequest().getPath().value();
            String queryString = exchange.getRequest().getURI().getQuery();
            String clientIp = getClientIp(exchange);
            String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
            // 读取TraceId（优先MDC，其次请求头）
            String traceId = org.slf4j.MDC.get(com.honyrun.util.LoggingUtil.TRACE_ID_KEY);
            if (traceId == null) {
                traceId = exchange.getRequest().getHeaders().getFirst(com.honyrun.util.TraceIdUtil.X_TRACE_ID_HEADER);
            }

            // 构建查询字符串展示
            String fullPath = (queryString != null && !queryString.isEmpty()) ? (path + "?" + queryString) : path;

            // 记录用户信息（从Spring Security上下文获取）
            exchange.getPrincipal()
                    .cast(org.springframework.security.core.Authentication.class)
                    .doOnNext(auth -> {
                        if (auth.isAuthenticated()) {
                            // 认证信息补充日志（避免捕获非final变量，内部读取TraceId）
                            com.honyrun.util.LoggingUtil.debug(logger,
                                    "用户已认证: user={}",
                                    auth.getName());
                        }
                    })
                    .subscribe();

            // 若没有认证信息或上述日志未触发，仍记录基础开始日志
            com.honyrun.util.LoggingUtil.info(logger,
                    "请求开始: method={}, path={}, clientIp={}, ua={}",
                    method,
                    fullPath,
                    clientIp,
                    userAgent != null ? userAgent : "Unknown");
        });
    }

    /**
     * 记录响应信息
     *
     * @param exchange 服务器Web交换对象
     * @return 处理结果的Mono
     */
    public Mono<Void> logResponse(ServerWebExchange exchange) {
        return Mono.fromRunnable(() -> {
            LocalDateTime startTime = exchange.getAttribute(REQUEST_START_TIME_KEY);
            LocalDateTime endTime = LocalDateTime.now();
            
            if (startTime == null) {
                return;
            }

            // 计算处理时间
            long processingTime = java.time.Duration.between(startTime, endTime).toMillis();

            // 获取响应信息
            HttpStatusCode statusCodeObj = exchange.getResponse().getStatusCode();
            int statusCode = statusCodeObj != null ? statusCodeObj.value() : 200;
            // 请求信息补充
            String method = exchange.getRequest().getMethod().name();
            String path = exchange.getRequest().getPath().value();
            String queryString = exchange.getRequest().getURI().getQuery();
            String fullPath = (queryString != null && !queryString.isEmpty()) ? (path + "?" + queryString) : path;
            // 读取TraceId（优先MDC，其次响应头，其次请求头）
            String traceId = org.slf4j.MDC.get(com.honyrun.util.LoggingUtil.TRACE_ID_KEY);
            if (traceId == null) {
                traceId = exchange.getResponse().getHeaders().getFirst(com.honyrun.util.TraceIdUtil.X_TRACE_ID_HEADER);
            }
            if (traceId == null) {
                traceId = exchange.getRequest().getHeaders().getFirst(com.honyrun.util.TraceIdUtil.X_TRACE_ID_HEADER);
            }

            // 统一结构化日志（TraceId由统一前缀提供）
            String message = "响应完成: status={}, time={}ms, method={}, path={}";

            // 根据状态码选择日志级别
            if (statusCode >= 500) {
                com.honyrun.util.LoggingUtil.error(logger, message,
                        statusCode, processingTime, method, fullPath);
            } else if (statusCode >= 400) {
                com.honyrun.util.LoggingUtil.warn(logger, message,
                        statusCode, processingTime, method, fullPath);
            } else {
                com.honyrun.util.LoggingUtil.info(logger, message,
                        statusCode, processingTime, method, fullPath);
            }

            // 记录性能指标
            recordPerformanceMetrics(exchange, processingTime, statusCode);
        });
    }

    /**
     * 设置请求上下文
     *
     * @param exchange 服务器Web交换对象
     */
    private void setupRequestContext(ServerWebExchange exchange) {
        // TraceId 已由 ReactiveTraceIdFilter 统一设置到 Reactor Context 与 MDC
        // 这里无需额外上下文设置，保持轻量。
    }

    /**
     * 生成请求ID
     *
     * @return 唯一的请求ID
     */
    // 保留TraceId相关逻辑由过滤器统一处理，此处不再生成临时RequestId

    /**
     * 获取客户端IP地址
     *
     * @param exchange 服务器Web交换对象
     * @return 客户端IP地址
     */
    private String getClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ?
                remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    /**
     * 记录性能指标
     *
     * @param exchange 服务器Web交换对象
     * @param processingTime 处理时间
     * @param statusCode 状态码
     */
    private void recordPerformanceMetrics(ServerWebExchange exchange, long processingTime, int statusCode) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();

        // 这里可以集成监控系统，如Micrometer、Prometheus等
        if (processingTime > 1000) { // 超过1秒的请求
            LoggingUtil.warn(logger,
                    String.format("SLOW_REQUEST - Method: %s, Path: %s, ProcessingTime: %dms",
                            method, path, processingTime));
        }

        if (statusCode >= 500) {
            LoggingUtil.error(logger,
                    String.format("SERVER_ERROR - Method: %s, Path: %s, StatusCode: %d",
                            method, path, statusCode));
        }
    }

    // 移除请求ID相关方法：日志链路统一使用TraceId，由ReactiveTraceIdFilter管理
}


