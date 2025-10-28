package com.honyrun.filter.reactive;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 响应式日志过滤器
 *
 * 基于Spring WebFlux的响应式日志过滤器，用于记录HTTP请求和响应信息。
 * 该过滤器采用非阻塞方式记录访问日志，支持请求追踪和性能监控。
 *
 * 特性：
 * - 非阻塞I/O日志记录
 * - 请求响应追踪
 * - 性能监控支持
 * - 统一日志格式
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  17:10:00
 * @modified 2025-07-01 17:10:00
 * @version 2.0.0
 */
@Component("reactiveLoggingFilter")
@Order(0)
public class ReactiveLoggingFilter implements WebFilter {

    private static final String REQUEST_ID_ATTRIBUTE = "request.id";
    private static final String START_TIME_ATTRIBUTE = "request.start.time";
    private static final String REQUEST_INFO_ATTRIBUTE = "request.info";

    private static final Logger logger = LoggerFactory.getLogger(ReactiveLoggingFilter.class);

    /**
     * 过滤器处理方法
     *
     * @param exchange 服务器Web交换对象
     * @param chain Web过滤器链
     * @return 响应式处理结果
     */
    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return logRequest(exchange)
                .then(chain.filter(exchange))
                .doOnSuccess(unused -> logResponse(exchange))
                .doOnError(throwable -> logError(exchange, throwable))
                .doFinally(signalType -> logCompletion(exchange, signalType));
    }

    /**
     * 记录请求信息
     *
     * @param exchange 服务器Web交换对象
     * @return 记录完成的Mono
     */
    private Mono<Void> logRequest(ServerWebExchange exchange) {
        return Mono.fromRunnable(() -> {
            ServerHttpRequest request = exchange.getRequest();

            // 生成请求ID
            String requestId = generateRequestId();
            exchange.getAttributes().put(REQUEST_ID_ATTRIBUTE, requestId);

            // 记录开始时间
            long startTime = System.currentTimeMillis();
            exchange.getAttributes().put(START_TIME_ATTRIBUTE, startTime);

            // 创建请求信息对象
            RequestInfo requestInfo = createRequestInfo(request, requestId, startTime);
            exchange.getAttributes().put(REQUEST_INFO_ATTRIBUTE, requestInfo);

            // 记录请求日志
            logRequestInfo(requestInfo);
        });
    }

    /**
     * 记录响应信息
     *
     * @param exchange 服务器Web交换对象
     */
    private void logResponse(ServerWebExchange exchange) {
        try {
            ServerHttpResponse response = exchange.getResponse();
            RequestInfo requestInfo = (RequestInfo) exchange.getAttributes().get(REQUEST_INFO_ATTRIBUTE);

            if (requestInfo != null) {
                // 更新响应信息 - 安全访问getStatusCode()
                requestInfo.setResponseStatus(Optional.ofNullable(response.getStatusCode())
                        .map(HttpStatusCode::value)
                        .orElse(200));
                requestInfo.setResponseTime(LocalDateTime.now());
                requestInfo.setProcessingTime(calculateProcessingTime(exchange));

                // 记录响应日志
                logResponseInfo(requestInfo);
            }
        } catch (Exception e) {
            // 日志记录失败不应该影响正常请求处理
            LoggingUtil.warn(logger, "记录响应日志失败: {}", e.getMessage());
        }
    }

    /**
     * 记录错误信息
     *
     * @param exchange 服务器Web交换对象
     * @param throwable 异常信息
     */
    private void logError(ServerWebExchange exchange, Throwable throwable) {
        try {
            RequestInfo requestInfo = (RequestInfo) exchange.getAttributes().get(REQUEST_INFO_ATTRIBUTE);

            if (requestInfo != null) {
                requestInfo.setError(true);
                requestInfo.setErrorMessage(throwable.getMessage());
                requestInfo.setErrorType(throwable.getClass().getSimpleName());
                requestInfo.setResponseTime(LocalDateTime.now());
                requestInfo.setProcessingTime(calculateProcessingTime(exchange));

                // 记录错误日志
                logErrorInfo(requestInfo, throwable);
            }
        } catch (Exception e) {
            // 日志记录失败不应该影响正常请求处理
            LoggingUtil.warn(logger, "记录错误日志失败: {}", e.getMessage());
        }
    }

    /**
     * 记录请求完成信息
     *
     * @param exchange 服务器Web交换对象
     * @param signalType 信号类型
     */
    private void logCompletion(ServerWebExchange exchange, reactor.core.publisher.SignalType signalType) {
        try {
            RequestInfo requestInfo = (RequestInfo) exchange.getAttributes().get(REQUEST_INFO_ATTRIBUTE);

            if (requestInfo != null) {
                requestInfo.setCompletionType(signalType.toString());
                requestInfo.setCompletionTime(LocalDateTime.now());

                // 记录完成日志
                logCompletionInfo(requestInfo);
            }
        } catch (Exception e) {
            // 日志记录失败不应该影响正常请求处理
            LoggingUtil.warn(logger, "记录完成日志失败: {}", e.getMessage());
        }
    }

    /**
     * 生成请求ID
     *
     * @return 请求ID
     */
    private String generateRequestId() {
        return LoggingUtil.generateRequestId();
    }

    /**
     * 创建请求信息对象
     *
     * @param request HTTP请求对象
     * @param requestId 请求ID
     * @param startTime 开始时间
     * @return 请求信息对象
     */
    private RequestInfo createRequestInfo(ServerHttpRequest request, String requestId, long startTime) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setRequestId(requestId);
        requestInfo.setMethod(request.getMethod() != null ? request.getMethod().name() : "UNKNOWN");
        requestInfo.setUri(request.getURI().toString());
        requestInfo.setPath(request.getPath().value());
        requestInfo.setQueryString(request.getURI().getQuery());
        requestInfo.setRemoteAddress(getRemoteAddress(request));
        requestInfo.setUserAgent(request.getHeaders().getFirst("User-Agent"));
        requestInfo.setContentType(request.getHeaders().getFirst("Content-Type"));
        requestInfo.setContentLength(request.getHeaders().getContentLength());
        requestInfo.setRequestTime(LocalDateTime.now());
        requestInfo.setStartTimeMillis(startTime);

        return requestInfo;
    }

    /**
     * 获取远程地址
     *
     * @param request HTTP请求对象
     * @return 远程地址
     */
    private String getRemoteAddress(ServerHttpRequest request) {
        // 优先获取X-Forwarded-For头
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // 获取X-Real-IP头
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.trim().isEmpty()) {
            return xRealIp;
        }

        // 获取远程地址
        return Optional.ofNullable(request.getRemoteAddress())
                .map(InetSocketAddress::getAddress)
                .filter(Objects::nonNull)
                .map(InetAddress::getHostAddress)
                .orElse("unknown");
    }

    /**
     * 计算处理时间
     *
     * @param exchange 服务器Web交换对象
     * @return 处理时间（毫秒）
     */
    private long calculateProcessingTime(ServerWebExchange exchange) {
        Long startTime = (Long) exchange.getAttributes().get(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }

    /**
     * 记录请求信息日志
     *
     * @param requestInfo 请求信息
     */
    private void logRequestInfo(RequestInfo requestInfo) {
        LoggingUtil.info(logger,
                "请求开始: method={}, path={}, remote={}, ua={}",
                requestInfo.getMethod(),
                requestInfo.getPath(),
                requestInfo.getRemoteAddress(),
                requestInfo.getUserAgent() != null ? requestInfo.getUserAgent() : "");
    }

    /**
     * 记录响应信息日志
     *
     * @param requestInfo 请求信息
     */
    private void logResponseInfo(RequestInfo requestInfo) {
        int status = requestInfo.getResponseStatus();
        long time = requestInfo.getProcessingTime();

        if (status >= 500) {
            LoggingUtil.error(logger, "响应完成: status={}, time={}ms, method={}, path={}", status, time, requestInfo.getMethod(), requestInfo.getPath());
        } else if (status >= 400) {
            LoggingUtil.warn(logger, "响应完成: status={}, time={}ms, method={}, path={}", status, time, requestInfo.getMethod(), requestInfo.getPath());
        } else {
            LoggingUtil.info(logger, "响应完成: status={}, time={}ms, method={}, path={}", status, time, requestInfo.getMethod(), requestInfo.getPath());
        }
    }

    /**
     * 记录错误信息日志
     *
     * @param requestInfo 请求信息
     * @param throwable 异常信息
     */
    private void logErrorInfo(RequestInfo requestInfo, Throwable throwable) {
        LoggingUtil.error(logger,
                "请求错误: method={}, path={}, message={}",
                throwable,
                requestInfo.getMethod(),
                requestInfo.getPath(),
                throwable.getMessage());
    }

    /**
     * 记录完成信息日志
     *
     * @param requestInfo 请求信息
     */
    private void logCompletionInfo(RequestInfo requestInfo) {
        LoggingUtil.debug(logger,
                "请求完成: signal={}, time={}ms, method={}, path={}",
                requestInfo.getCompletionType(),
                requestInfo.getProcessingTime(),
                requestInfo.getMethod(),
                requestInfo.getPath());
    }

    /**
     * 请求信息类
     */
    public static class RequestInfo {
        private String requestId;
        private String method;
        private String uri;
        private String path;
        private String queryString;
        private String remoteAddress;
        private String userAgent;
        private String contentType;
        private long contentLength;
        private LocalDateTime requestTime;
        private LocalDateTime responseTime;
        private LocalDateTime completionTime;
        private long startTimeMillis;
        private long processingTime;
        private int responseStatus;
        private boolean error;
        private String errorMessage;
        private String errorType;
        private String completionType;

        // Getter and Setter methods

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getQueryString() {
            return queryString;
        }

        public void setQueryString(String queryString) {
            this.queryString = queryString;
        }

        public String getRemoteAddress() {
            return remoteAddress;
        }

        public void setRemoteAddress(String remoteAddress) {
            this.remoteAddress = remoteAddress;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public long getContentLength() {
            return contentLength;
        }

        public void setContentLength(long contentLength) {
            this.contentLength = contentLength;
        }

        public LocalDateTime getRequestTime() {
            return requestTime;
        }

        public void setRequestTime(LocalDateTime requestTime) {
            this.requestTime = requestTime;
        }

        public LocalDateTime getResponseTime() {
            return responseTime;
        }

        public void setResponseTime(LocalDateTime responseTime) {
            this.responseTime = responseTime;
        }

        public LocalDateTime getCompletionTime() {
            return completionTime;
        }

        public void setCompletionTime(LocalDateTime completionTime) {
            this.completionTime = completionTime;
        }

        public long getStartTimeMillis() {
            return startTimeMillis;
        }

        public void setStartTimeMillis(long startTimeMillis) {
            this.startTimeMillis = startTimeMillis;
        }

        public long getProcessingTime() {
            return processingTime;
        }

        public void setProcessingTime(long processingTime) {
            this.processingTime = processingTime;
        }

        public int getResponseStatus() {
            return responseStatus;
        }

        public void setResponseStatus(int responseStatus) {
            this.responseStatus = responseStatus;
        }

        public boolean isError() {
            return error;
        }

        public void setError(boolean error) {
            this.error = error;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorType() {
            return errorType;
        }

        public void setErrorType(String errorType) {
            this.errorType = errorType;
        }

        public String getCompletionType() {
            return completionType;
        }

        public void setCompletionType(String completionType) {
            this.completionType = completionType;
        }

        @Override
        public String toString() {
            return String.format("RequestInfo{requestId='%s', method='%s', path='%s', status=%d, time=%dms}",
                    requestId, method, path, responseStatus, processingTime);
        }
    }
}


