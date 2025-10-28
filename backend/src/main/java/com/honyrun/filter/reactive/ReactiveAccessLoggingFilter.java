package com.honyrun.filter.reactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.honyrun.service.security.AccessTypeDetectionService;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 响应式访问日志过滤器
 * 
 * 统一记录所有请求的访问信息，包括：
 * - 访问类型（本地/内网/外网）
 * - 客户端IP地址
 * - 请求路径和方法
 * - 用户代理信息
 * - 访问时间
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-23 22:35:00
 * @version 1.0.0
 */
@Component("reactiveAccessLoggingFilter")
public class ReactiveAccessLoggingFilter implements WebFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveAccessLoggingFilter.class);
    private static final Logger accessLogger = LoggerFactory.getLogger("ACCESS_LOG");

    private final AccessTypeDetectionService accessTypeDetectionService;
    private final ObjectMapper objectMapper;

    // 跳过日志记录的路径
    private static final List<String> SKIP_PATHS = Arrays.asList(
            "/favicon.ico",
            "/static/",
            "/css/",
            "/js/",
            "/images/",
            "/webjars/"
    );

    public ReactiveAccessLoggingFilter(AccessTypeDetectionService accessTypeDetectionService, 
                                     ObjectMapper objectMapper) {
        this.accessTypeDetectionService = accessTypeDetectionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 跳过静态资源的日志记录
        if (shouldSkipLogging(path)) {
            return chain.filter(exchange);
        }

        long startTime = System.currentTimeMillis();
        
        return accessTypeDetectionService.detectAccessType(exchange)
                .flatMap(accessType -> {
                    // 记录请求开始日志
                    logRequestStart(exchange, accessType);
                    
                    return chain.filter(exchange)
                            .doOnSuccess(unused -> {
                                // 记录请求完成日志
                                long duration = System.currentTimeMillis() - startTime;
                                logRequestEnd(exchange, accessType, duration, true);
                            })
                            .doOnError(error -> {
                                // 记录请求错误日志
                                long duration = System.currentTimeMillis() - startTime;
                                logRequestEnd(exchange, accessType, duration, false);
                                LoggingUtil.error(logger, "请求处理异常: {}", error, error.getMessage());
                            });
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "访问日志记录异常: {}", error, error.getMessage());
                    // 发生异常时继续处理请求，不影响正常业务
                    return chain.filter(exchange);
                });
    }

    /**
     * 记录请求开始日志
     */
    private void logRequestStart(ServerWebExchange exchange, AccessTypeDetectionService.AccessType accessType) {
        ServerHttpRequest request = exchange.getRequest();
        String clientIp = accessTypeDetectionService.getClientIp(exchange);
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String referer = request.getHeaders().getFirst("Referer");
        
        // 构建访问日志信息
        String logMessage = String.format(
            "[%s] %s访问 - %s %s - IP: %s - UserAgent: %s - Referer: %s",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            accessType.getDescription(),
            method,
            path,
            clientIp,
            userAgent != null ? userAgent : "Unknown",
            referer != null ? referer : "Direct"
        );

        // 根据访问类型使用不同的日志级别
        switch (accessType) {
            case EXTERNAL:
                LoggingUtil.info(accessLogger, "外部访问: {}", logMessage);
                LoggingUtil.info(logger, "检测到外部访问: {} {} - IP: {}", method, path, clientIp);
                break;
            case INTERNAL:
                LoggingUtil.info(accessLogger, "内网访问: {}", logMessage);
                LoggingUtil.debug(logger, "内网访问: {} {} - IP: {}", method, path, clientIp);
                break;
            case LOCAL:
                LoggingUtil.debug(accessLogger, "本地访问: {}", logMessage);
                LoggingUtil.debug(logger, "本地访问: {} {} - IP: {}", method, path, clientIp);
                break;
            default:
                LoggingUtil.warn(accessLogger, "未知访问: {}", logMessage);
                LoggingUtil.warn(logger, "未知访问类型: {} {} - IP: {}", method, path, clientIp);
                break;
        }
    }

    /**
     * 记录请求完成日志
     */
    private void logRequestEnd(ServerWebExchange exchange, AccessTypeDetectionService.AccessType accessType, 
                              long duration, boolean success) {
        ServerHttpRequest request = exchange.getRequest();
        String clientIp = accessTypeDetectionService.getClientIp(exchange);
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        
        String status = success ? "成功" : "失败";
        String logMessage = String.format(
            "[%s] %s访问%s - %s %s - IP: %s - 耗时: %dms",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            accessType.getDescription(),
            status,
            method,
            path,
            clientIp,
            duration
        );

        if (success) {
            if (duration > 1000) { // 超过1秒的请求记录为警告
                LoggingUtil.warn(accessLogger, "慢请求: {}", logMessage);
            } else {
                LoggingUtil.info(accessLogger, "请求完成: {}", logMessage);
            }
        } else {
            LoggingUtil.error(accessLogger, "请求失败: {}", logMessage);
        }
    }

    /**
     * 判断是否应该跳过日志记录
     */
    private boolean shouldSkipLogging(String path) {
        if (path == null) {
            return false;
        }

        return SKIP_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        // 设置较高优先级，确保在其他过滤器之前执行
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
