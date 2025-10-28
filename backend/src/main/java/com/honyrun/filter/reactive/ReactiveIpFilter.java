package com.honyrun.filter.reactive;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.service.security.ReactiveIpFilterService;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;

/**
 * 响应式IP过滤器
 * 基于白名单/黑名单机制过滤客户端IP地址
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-13 15:45:00
 * @version 1.0.0
 */
@Component("reactiveIpFilter")
public class ReactiveIpFilter implements WebFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveIpFilter.class);

    private final ReactiveIpFilterService ipFilterService;
    private final ObjectMapper objectMapper;

    // 跳过IP过滤的路径
    private static final List<String> SKIP_PATHS = Arrays.asList(
            "/actuator/health",
            "/favicon.ico",
            "/static/",
            "/css/",
            "/js/",
            "/images/",
            "/webjars/");

    public ReactiveIpFilter(ReactiveIpFilterService ipFilterService, ObjectMapper objectMapper) {
        this.ipFilterService = ipFilterService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 跳过静态资源和健康检查
        if (shouldSkipIpFilter(path)) {
            return chain.filter(exchange);
        }

        String clientIp = getClientIp(request);
        LoggingUtil.info(logger, "IP过滤检查: {} 访问 {}", clientIp, path);

        return ipFilterService.isIpAllowed(clientIp)
                .flatMap(allowed -> {
                    if (allowed) {
                        LoggingUtil.debug(logger, "IP {} 通过过滤检查", clientIp);
                        return chain.filter(exchange);
                    } else {
                        LoggingUtil.warn(logger, "IP {} 被拒绝访问 {}", clientIp, path);
                        return handleIpBlocked(exchange, clientIp);
                    }
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "IP过滤检查异常: {}", error, error.getMessage());
                    // 发生异常时允许通过，避免影响正常访问
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        // 在认证过滤器之前执行，但在速率限制之后
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }

    /**
     * 处理IP被阻止的情况
     */
    private Mono<Void> handleIpBlocked(ServerWebExchange exchange, String clientIp) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> apiResponse = ApiResponse.error(
                String.valueOf(HttpStatus.FORBIDDEN.value()),
                "访问被拒绝：您的IP地址不在允许访问的范围内");

        try {
            String responseBody = objectMapper.writeValueAsString(apiResponse);
            DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "序列化IP过滤响应失败: {}", e, e.getMessage());
            return response.setComplete();
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(ServerHttpRequest request) {
        // 优先从X-Forwarded-For获取
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For可能包含多个IP，取第一个
            return xForwardedFor.split(",")[0].trim();
        }

        // 从X-Real-IP获取
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        // 从Proxy-Client-IP获取
        String proxyClientIp = request.getHeaders().getFirst("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp;
        }

        // 从WL-Proxy-Client-IP获取
        String wlProxyClientIp = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp;
        }

        // 从HTTP_CLIENT_IP获取
        String httpClientIp = request.getHeaders().getFirst("HTTP_CLIENT_IP");
        if (httpClientIp != null && !httpClientIp.isEmpty() && !"unknown".equalsIgnoreCase(httpClientIp)) {
            return httpClientIp;
        }

        // 从HTTP_X_FORWARDED_FOR获取
        String httpXForwardedFor = request.getHeaders().getFirst("HTTP_X_FORWARDED_FOR");
        if (httpXForwardedFor != null && !httpXForwardedFor.isEmpty()
                && !"unknown".equalsIgnoreCase(httpXForwardedFor)) {
            return httpXForwardedFor;
        }

        // 最后从远程地址获取
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    /**
     * 判断是否应该跳过IP过滤
     */
    private boolean shouldSkipIpFilter(String path) {
        if (path == null) {
            return false;
        }

        return SKIP_PATHS.stream().anyMatch(path::startsWith);
    }
}
