package com.honyrun.filter.reactive;

import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.service.reactive.ReactiveSystemConfigService;
import com.honyrun.util.ErrorDetailsUtil;
import com.honyrun.util.common.JsonUtil;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.NetworkUtil;
import com.honyrun.util.TraceIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 响应式API限流过滤器
 * 基于Redis实现分布式限流，防止API滥用和DDoS攻击
 *
 * @author Mr.Rey
 * @since 2025-07-01 11:00:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component("reactiveApiRateLimitFilter")
public class ReactiveApiRateLimitFilter implements WebFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveApiRateLimitFilter.class);

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReactiveSystemConfigService systemConfigService;
    private final ErrorDetailsUtil errorDetailsUtil;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis模板
     * @param systemConfigService 系统配置服务
     * @param errorDetailsUtil 错误详情工具类
     */
    public ReactiveApiRateLimitFilter(@Qualifier("unifiedReactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate,
                                   ReactiveSystemConfigService systemConfigService,
                                   ErrorDetailsUtil errorDetailsUtil) {
        this.redisTemplate = redisTemplate;
        this.systemConfigService = systemConfigService;
        this.errorDetailsUtil = errorDetailsUtil;
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return isRateLimitEnabled()
                .flatMap(enabled -> {
                    if (!enabled) {
                        return chain.filter(exchange);
                    }
                    
                    String clientKey = getClientKey(exchange);
                    
                    return checkRateLimit(clientKey)
                            .flatMap(allowed -> {
                                if (!allowed) {
                                    String traceId = TraceIdUtil.generateTraceId();
                                    
                                    return Mono.fromCallable(() -> {
                                        String requestPath = exchange.getRequest().getPath().value();
                                        Map<String, Object> details = errorDetailsUtil.buildSimpleErrorDetails("RateLimitExceededException", requestPath);
                                        
                                        LoggingUtil.warn(logger, "[{}] API限流触发，客户端: {}, 路径: {}", 
                                                       traceId, clientKey, requestPath);
                                        
                                        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                                                .success(false)
                                                .code(String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()))
                                                .message("Rate limit exceeded")
                                                .traceId(traceId)
                                                .details(details)
                                                .path(requestPath)
                                                .build();

                                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                                        exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");
                                        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", "60");
                                        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
                                        exchange.getResponse().getHeaders().add("Retry-After", "60");
                                        
                                        String errorResponse = JsonUtil.toJson(apiResponse);
                                        
                                        return exchange.getResponse().writeWith(
                                            Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
                                        );
                                    })
                                    .contextWrite(context -> TraceIdUtil.putTraceIdToContext(context, traceId))
                                    .flatMap(mono -> mono);
                                }
                                
                                return chain.filter(exchange);
                            });
                });
    }

    /**
     * 检查是否启用限流
     *
     * @return 如果启用返回true，否则返回false
     */
    private Mono<Boolean> isRateLimitEnabled() {
        return systemConfigService.getConfigValue("security.api.rate.limit.enabled", "false")
                .map(Boolean::parseBoolean)
                .onErrorReturn(false);
    }

    /**
     * 获取每分钟最大请求数配置
     *
     * @return 每分钟最大请求数
     */
    private Mono<Integer> getRequestsPerMinute() {
        return systemConfigService.getConfigValue("security.api.rate.limit.requests_per_minute", "60")
                .map(Integer::parseInt)
                .onErrorReturn(60);
    }

    /**
     * 获取客户端标识键
     *
     * @param exchange 服务器Web交换对象
     * @return 客户端标识键
     */
    private String getClientKey(ServerWebExchange exchange) {
        return NetworkUtil.getClientIpAddress(exchange);
    }

    /**
     * 检查限流
     * 使用滑动窗口算法实现限流
     *
     * @param clientKey 客户端标识键
     * @return 如果允许请求返回true，否则返回false
     */
    private Mono<Boolean> checkRateLimit(String clientKey) {
        return getRequestsPerMinute()
                .flatMap(maxRequests -> {
                    String currentMinute = getCurrentMinute();
                    String rateLimitKey = RATE_LIMIT_KEY_PREFIX + clientKey + ":" + currentMinute;
                    
                    return redisTemplate.opsForValue().increment(rateLimitKey)
                            .flatMap(currentCount -> {
                                if (currentCount == 1) {
                                    // 第一次访问，设置过期时间
                                    return redisTemplate.expire(rateLimitKey, Duration.ofMinutes(1))
                                            .then(Mono.just(true));
                                }
                                
                                boolean allowed = currentCount <= maxRequests;
                                
                                if (!allowed) {
                                    LoggingUtil.debug(logger, "限流检查失败，客户端: {}, 当前计数: {}, 最大允许: {}", 
                                                    clientKey, currentCount, maxRequests);
                                }
                                
                                return Mono.just(allowed);
                            });
                })
                .onErrorReturn(true); // 出现异常时允许请求通过
    }

    /**
     * 获取当前分钟的时间戳字符串
     *
     * @return 当前分钟的时间戳字符串
     */
    private String getCurrentMinute() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
    }

    @Override
    public int getOrder() {
        // 设置较高的优先级，在认证过滤器之前执行
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}

