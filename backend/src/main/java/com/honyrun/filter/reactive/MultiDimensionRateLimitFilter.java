package com.honyrun.filter.reactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.service.reactive.ReactiveSystemConfigService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.NetworkUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多维度响应式限流过滤器
 * 支持全局、IP、用户、端点级别的多维度限流控制
 * 基于令牌桶算法实现分布式限流，防止API滥用和DDoS攻击
 *
 * @author Mr.Rey
 * @since 2025-07-01 15:30:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 * @created 2025-07-01 15:30:00
 * @modified 2025-07-01 15:30:00
 */
@Component("multiDimensionRateLimitFilter")
public class MultiDimensionRateLimitFilter implements WebFilter, Ordered {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MultiDimensionRateLimitFilter.class);

    private static final String RATE_LIMIT_KEY_PREFIX = "multi_rate_limit:";
    private static final String GLOBAL_LIMIT_KEY = "global";
    private static final String IP_LIMIT_KEY_PREFIX = "ip:";
    private static final String USER_LIMIT_KEY_PREFIX = "user:";
    private static final String ENDPOINT_LIMIT_KEY_PREFIX = "endpoint:";

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReactiveSystemConfigService systemConfigService;
    private final ObjectMapper objectMapper;

    // 缓存限流配置，避免频繁查询
    private final ConcurrentHashMap<String, RateLimitConfig> configCache = new ConcurrentHashMap<>();
    private volatile long lastConfigRefresh = 0;
    private static final long CONFIG_CACHE_TTL = 60000; // 1分钟缓存

    /**
     * 限流配置内部类
     */
    private static class RateLimitConfig {
        private final boolean enabled;
        private final int requestsPerSecond;
        private final int burstCapacity;

        public RateLimitConfig(boolean enabled, int requestsPerSecond, int burstCapacity) {
            this.enabled = enabled;
            this.requestsPerSecond = requestsPerSecond;
            this.burstCapacity = burstCapacity;
        }

        public boolean isEnabled() { return enabled; }
        public int getRequestsPerSecond() { return requestsPerSecond; }
        public int getBurstCapacity() { return burstCapacity; }
    }

    /**
     * 构造函数注入
     *
     * @param redisTemplate Redis模板
     * @param systemConfigService 系统配置服务
     * @param objectMapper JSON对象映射器
     */
    public MultiDimensionRateLimitFilter(@Qualifier("unifiedReactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate,
                                        ReactiveSystemConfigService systemConfigService,
                                        ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.systemConfigService = systemConfigService;
        this.objectMapper = objectMapper;
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return isRateLimitEnabled()
                .flatMap(enabled -> {
                    if (!enabled) {
                        return chain.filter(exchange);
                    }

                    String clientIp = NetworkUtil.getClientIpAddress(exchange);
                    String requestPath = exchange.getRequest().getPath().value();

                    return ReactiveSecurityContextHolder.getContext()
                            .map(securityContext -> securityContext.getAuthentication())
                            .cast(Authentication.class)
                            .map(auth -> auth.getName())
                            .defaultIfEmpty("anonymous")
                            .flatMap(username -> checkAllRateLimits(clientIp, username, requestPath))
                            .flatMap(allowed -> {
                                if (!allowed) {
                                    return handleRateLimitExceeded(exchange, clientIp, requestPath);
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
        return systemConfigService.getConfigValue("honyrun.rate-limit.enabled", "false")
                .map(Boolean::parseBoolean)
                .onErrorReturn(false);
    }

    /**
     * 检查所有维度的限流
     *
     * @param clientIp 客户端IP
     * @param username 用户名
     * @param requestPath 请求路径
     * @return 是否允许请求
     */
    private Mono<Boolean> checkAllRateLimits(String clientIp, String username, String requestPath) {
        return Mono.zip(
                checkGlobalRateLimit(),
                checkIpRateLimit(clientIp),
                checkUserRateLimit(username),
                checkEndpointRateLimit(requestPath)
        ).map(tuple -> tuple.getT1() && tuple.getT2() && tuple.getT3() && tuple.getT4());
    }

    /**
     * 检查全局限流
     *
     * @return 是否允许请求
     */
    private Mono<Boolean> checkGlobalRateLimit() {
        return getRateLimitConfig("global")
                .flatMap(config -> {
                    if (!config.isEnabled()) {
                        return Mono.just(true);
                    }
                    return checkRateLimit(GLOBAL_LIMIT_KEY, config.getRequestsPerSecond(), config.getBurstCapacity());
                });
    }

    /**
     * 检查IP级限流
     *
     * @param clientIp 客户端IP
     * @return 是否允许请求
     */
    private Mono<Boolean> checkIpRateLimit(String clientIp) {
        return getRateLimitConfig("ip")
                .flatMap(config -> {
                    if (!config.isEnabled()) {
                        return Mono.just(true);
                    }
                    return checkRateLimit(IP_LIMIT_KEY_PREFIX + clientIp, config.getRequestsPerSecond(), config.getBurstCapacity());
                });
    }

    /**
     * 检查用户级限流
     *
     * @param username 用户名
     * @return 是否允许请求
     */
    private Mono<Boolean> checkUserRateLimit(String username) {
        if ("anonymous".equals(username)) {
            return Mono.just(true); // 匿名用户不进行用户级限流
        }

        return getRateLimitConfig("user")
                .flatMap(config -> {
                    if (!config.isEnabled()) {
                        return Mono.just(true);
                    }
                    return checkRateLimit(USER_LIMIT_KEY_PREFIX + username, config.getRequestsPerSecond(), config.getBurstCapacity());
                });
    }

    /**
     * 检查端点级限流
     *
     * @param requestPath 请求路径
     * @return 是否允许请求
     */
    private Mono<Boolean> checkEndpointRateLimit(String requestPath) {
        return getRateLimitConfig("endpoint")
                .flatMap(config -> {
                    if (!config.isEnabled()) {
                        return Mono.just(true);
                    }
                    // 简化路径，去除路径参数
                    String simplifiedPath = simplifyPath(requestPath);
                    return checkRateLimit(ENDPOINT_LIMIT_KEY_PREFIX + simplifiedPath, config.getRequestsPerSecond(), config.getBurstCapacity());
                });
    }

    /**
     * 获取限流配置
     *
     * @param dimension 限流维度
     * @return 限流配置
     */
    private Mono<RateLimitConfig> getRateLimitConfig(String dimension) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastConfigRefresh > CONFIG_CACHE_TTL) {
            configCache.clear();
            lastConfigRefresh = currentTime;
        }

        RateLimitConfig cachedConfig = configCache.get(dimension);
        if (cachedConfig != null) {
            return Mono.just(cachedConfig);
        }

        return loadRateLimitConfig(dimension)
                .doOnNext(config -> configCache.put(dimension, config));
    }

    /**
     * 加载限流配置
     *
     * @param dimension 限流维度
     * @return 限流配置
     */
    private Mono<RateLimitConfig> loadRateLimitConfig(String dimension) {
        String enabledKey = "honyrun.rate-limit." + dimension + ".enabled";
        String rpsKey = "honyrun.rate-limit." + dimension + ".requests-per-second";
        String burstKey = "honyrun.rate-limit." + dimension + ".burst-capacity";

        return Mono.zip(
                systemConfigService.getConfigValue(enabledKey, "true").map(Boolean::parseBoolean),
                systemConfigService.getConfigValue(rpsKey, getDefaultRequestsPerSecond(dimension)).map(Integer::parseInt),
                systemConfigService.getConfigValue(burstKey, getDefaultBurstCapacity(dimension)).map(Integer::parseInt)
        ).map(tuple -> new RateLimitConfig(tuple.getT1(), tuple.getT2(), tuple.getT3()))
        .onErrorReturn(new RateLimitConfig(true, getDefaultRequestsPerSecondInt(dimension), getDefaultBurstCapacityInt(dimension)));
    }

    /**
     * 获取默认请求速率
     *
     * @param dimension 限流维度
     * @return 默认请求速率字符串
     */
    private String getDefaultRequestsPerSecond(String dimension) {
        switch (dimension) {
            case "global": return "1000";
            case "ip": return "100";
            case "user": return "200";
            case "endpoint": return "500";
            default: return "100";
        }
    }

    /**
     * 获取默认突发容量
     *
     * @param dimension 限流维度
     * @return 默认突发容量字符串
     */
    private String getDefaultBurstCapacity(String dimension) {
        switch (dimension) {
            case "global": return "2000";
            case "ip": return "200";
            case "user": return "400";
            case "endpoint": return "1000";
            default: return "200";
        }
    }

    /**
     * 获取默认请求速率整数
     *
     * @param dimension 限流维度
     * @return 默认请求速率整数
     */
    private int getDefaultRequestsPerSecondInt(String dimension) {
        return Integer.parseInt(getDefaultRequestsPerSecond(dimension));
    }

    /**
     * 获取默认突发容量整数
     *
     * @param dimension 限流维度
     * @return 默认突发容量整数
     */
    private int getDefaultBurstCapacityInt(String dimension) {
        return Integer.parseInt(getDefaultBurstCapacity(dimension));
    }

    /**
     * 检查限流（令牌桶算法）
     *
     * @param limitKey 限流键
     * @param requestsPerSecond 每秒请求数
     * @param burstCapacity 突发容量
     * @return 是否允许请求
     */
    private Mono<Boolean> checkRateLimit(String limitKey, int requestsPerSecond, int burstCapacity) {
        String currentSecond = getCurrentSecond();
        String rateLimitKey = RATE_LIMIT_KEY_PREFIX + limitKey + ":" + currentSecond;

        return redisTemplate.opsForValue().increment(rateLimitKey)
                .flatMap(currentCount -> {
                    if (currentCount == 1) {
                        // 第一次访问，设置过期时间
                        return redisTemplate.expire(rateLimitKey, Duration.ofSeconds(1))
                                .then(Mono.just(true));
                    }

                    boolean allowed = currentCount <= Math.max(requestsPerSecond, burstCapacity);

                    if (!allowed) {
                        LoggingUtil.debug(logger, "限流检查失败，键: {}, 当前计数: {}, 限制: {}/{}", 
                                        limitKey, currentCount, requestsPerSecond, burstCapacity);
                    }

                    return Mono.just(allowed);
                })
                .onErrorReturn(true); // 出现异常时允许请求通过
    }

    /**
     * 简化请求路径，去除路径参数
     *
     * @param path 原始路径
     * @return 简化后的路径
     */
    private String simplifyPath(String path) {
        // 将数字ID替换为通配符，例如 /api/v1/users/123 -> /api/v1/users/*
        return path.replaceAll("/\\d+", "/*")
                  .replaceAll("/[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}", "/*"); // UUID
    }

    /**
     * 获取当前秒的时间戳字符串
     *
     * @return 当前秒的时间戳字符串
     */
    private String getCurrentSecond() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
    }

    /**
     * 处理限流超限情况
     *
     * @param exchange 服务器Web交换对象
     * @param clientIp 客户端IP
     * @param requestPath 请求路径
     * @return 限流响应
     */
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, String clientIp, String requestPath) {
        LoggingUtil.warn(logger, "多维度限流触发，客户端IP: {}, 路径: {}", clientIp, requestPath);

        // 设置响应状态和头部
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", "100");
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
        exchange.getResponse().getHeaders().add("Retry-After", "60");

        // 构建 traceId（从上下文或请求头），并写入响应头
        String headerTraceId = exchange.getRequest().getHeaders().getFirst(com.honyrun.util.TraceIdUtil.X_TRACE_ID_HEADER);
        String traceId = headerTraceId != null && !headerTraceId.isBlank()
                ? headerTraceId
                : com.honyrun.util.TraceIdUtil.generateTraceId();
        exchange.getResponse().getHeaders().set(com.honyrun.util.TraceIdUtil.X_TRACE_ID_HEADER, traceId);

        // 创建错误响应（使用字符串化HTTP状态码、携带traceId与简化details）
        String path = exchange.getRequest().getPath().value();
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        details.put("reason", "rate_limit_exceeded");
        details.put("retry_after_seconds", 60);
        ApiResponse<Object> errorResponse = ApiResponse.error(
                String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()),
                "请求频率超限，请稍后重试",
                traceId,
                details,
                path
        );

        try {
            String responseBody = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            LoggingUtil.error(logger, "序列化限流响应失败", e);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        // 设置较高的优先级，在认证过滤器之前执行
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }
}

