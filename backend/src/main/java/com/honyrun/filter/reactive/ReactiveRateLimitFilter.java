package com.honyrun.filter.reactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.service.reactive.ReactiveRateLimitService;
import com.honyrun.util.LoggingUtil;
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
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 响应式速率限制过滤器
 *
 * 在WebFlux请求处理链中集成速率限制功能：
 * - 对所有API请求进行速率限制检查
 * - 支持多维度限流（全局、IP、用户、端点）
 * - 提供统一的限流响应格式
 * - 高优先级执行，确保在认证之前进行限流
 *
 * 过滤器执行流程：
 * 1. 提取请求信息（IP、用户ID、端点）
 * 2. 执行多维度速率限制检查
 * 3. 通过：继续请求处理链
 * 4. 拒绝：返回429状态码和错误信息
 *
 * 特殊处理：
 * - 静态资源请求跳过限流
 * - 健康检查端点跳过限流
 * - 管理端点可配置是否限流
 *
 * @author Mr.Rey
 * @created 2025-07-01 17:20:00
 * @modified 2025-07-01 17:20:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component("reactiveRateLimitFilter")
public class ReactiveRateLimitFilter implements WebFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveRateLimitFilter.class);

    private final ReactiveRateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数注入
     *
     * @param rateLimitService 响应式限流服务
     * @param objectMapper JSON对象映射器
     */
    public ReactiveRateLimitFilter(ReactiveRateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    // 过滤器优先级，高于认证过滤器
    private static final int ORDER = -100;

    // 跳过限流的路径模式
    private static final List<String> SKIP_PATHS = List.of(
            "/actuator/health",
            "/actuator/info",
            "/favicon.ico",
            "/static/",
            "/css/",
            "/js/",
            "/images/",
            "/webjars/"
    );

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 跳过不需要限流的路径
        if (shouldSkipRateLimit(path)) {
            LoggingUtil.debug(logger, "跳过速率限制检查，路径: {}", path);
            return chain.filter(exchange);
        }

        // 提取请求信息
        String clientIp = getClientIp(request);
        Long userId = getUserId(exchange);
        String endpoint = getEndpoint(request);

        LoggingUtil.debug(logger, "开始速率限制检查，IP: {}, 用户: {}, 端点: {}", clientIp, userId, endpoint);

        // 执行速率限制检查
        return rateLimitService.checkAllRateLimits(clientIp, userId, endpoint)
                .flatMap(allowed -> {
                    if (allowed) {
                        LoggingUtil.debug(logger, "速率限制检查通过，继续处理请求");
                        return chain.filter(exchange);
                    } else {
                        LoggingUtil.warn(logger, "速率限制检查失败，拒绝请求，IP: {}, 用户: {}, 端点: {}",
                                clientIp, userId, endpoint);
                        return handleRateLimitExceeded(exchange);
                    }
                })
                .onErrorResume(throwable -> {
                    LoggingUtil.error(logger, "速率限制检查异常，允许请求通过，错误: {}", throwable.getMessage());
                    return chain.filter(exchange);
                });
    }

    /**
     * 判断是否应该跳过速率限制检查
     *
     * @param path 请求路径
     * @return 是否跳过
     */
    private boolean shouldSkipRateLimit(String path) {
        return SKIP_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return 客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        // 优先从代理头获取真实IP
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For可能包含多个IP，取第一个
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp.trim();
        }

        // 从远程地址获取
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    /**
     * 获取用户ID
     *
     * @param exchange 服务器交换对象
     * @return 用户ID，如果未认证则返回null
     */
    private Long getUserId(ServerWebExchange exchange) {
        // 尝试从认证信息中获取用户ID
        // 这里假设认证信息已经在之前的过滤器中设置到exchange的attributes中
        Object userId = exchange.getAttribute("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }

        // 如果没有认证信息，返回null（匿名用户）
        return null;
    }

    /**
     * 获取端点标识
     *
     * @param request HTTP请求
     * @return 端点标识
     */
    private String getEndpoint(ServerHttpRequest request) {
        String method = request.getMethod().name();
        String path = request.getPath().value();

        // 标准化路径，移除路径参数
        String normalizedPath = normalizePath(path);

        return method + ":" + normalizedPath;
    }

    /**
     * 标准化路径，将路径参数替换为占位符
     *
     * @param path 原始路径
     * @return 标准化后的路径
     */
    private String normalizePath(String path) {
        // 简单的路径标准化，将数字ID替换为占位符
        return path.replaceAll("/\\d+", "/{id}")
                  .replaceAll("/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}", "/{uuid}");
    }

    /**
     * 处理速率限制超出的情况
     *
     * @param exchange 服务器交换对象
     * @return 响应结果
     */
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();

        // 设置响应状态和头部
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("X-RateLimit-Limit", "请求频率超出限制");
        response.getHeaders().set("Retry-After", "60"); // 建议60秒后重试

        // 构建错误响应
        ApiResponse<Object> errorResponse = ApiResponse.error(
                String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()),
                "请求频率超出限制，请稍后重试",
                null
        );

        try {
            String responseBody = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));

            LoggingUtil.info(logger, "返回速率限制错误响应: {}", responseBody);

            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            LoggingUtil.error(logger, "构建速率限制错误响应失败: {}", e.getMessage());

            // 如果JSON序列化失败，返回简单的文本响应
            String fallbackResponse = "{\"success\":false,\"code\":429,\"message\":\"请求频率超出限制，请稍后重试\"}";
            DataBuffer buffer = response.bufferFactory().wrap(fallbackResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }
}
