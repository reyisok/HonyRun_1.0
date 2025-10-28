package com.honyrun.interceptor;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.honyrun.config.UnifiedConfigManager;

import reactor.core.publisher.Mono;

/**
 * 响应式限流拦截器
 *
 * 基于Spring WebFlux和Redis的响应式限流拦截器，用于控制API请求频率。
 * 该拦截器采用令牌桶算法进行限流，支持基于用户和IP的请求频率控制。
 *
 * 特性：
 * - 非阻塞限流处理
 * - 基于Redis的分布式限流
 * - 令牌桶算法实现
 * - 多维度限流策略
 * - 背压支持
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01 17:55:00
 * @modified 2025-07-01 17:55:00
 * @version 2.0.0
 */
@Component
public class ReactiveRateLimitInterceptor {

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit_interceptor:";

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final Environment environment;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数
     *
     * @param redisTemplate        Redis响应式模板
     * @param environment          环境配置
     * @param unifiedConfigManager 统一配置管理器
     */
    public ReactiveRateLimitInterceptor(
            @Qualifier("unifiedReactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate,
            Environment environment,
            UnifiedConfigManager unifiedConfigManager) {
        this.redisTemplate = redisTemplate;
        this.environment = environment;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 拦截请求进行限流检查
     *
     * @param exchange 服务器Web交换对象
     * @return 限流检查结果的Mono
     */
    public Mono<Boolean> intercept(@NonNull ServerWebExchange exchange) {
        return getLimitKey(exchange)
                .flatMap(limitKey -> checkRateLimit(limitKey, exchange))
                .onErrorReturn(true); // 限流检查失败时允许通过（降级策略）
    }

    /**
     * 获取限流键
     *
     * @param exchange 服务器Web交换对象
     * @return 限流键的Mono
     */
    private Mono<String> getLimitKey(ServerWebExchange exchange) {
        // 优先使用用户ID进行限流（从Spring Security上下文获取）
        return exchange.getPrincipal()
                .cast(org.springframework.security.core.Authentication.class)
                .map(auth -> auth.getName())
                .cast(String.class)
                .map(userId -> RATE_LIMIT_KEY_PREFIX + "user:" + userId)
                .onErrorResume(throwable -> {
                    // 回退到IP限流
                    String clientIp = getClientIp(exchange);
                    return Mono.just(RATE_LIMIT_KEY_PREFIX + "ip:" + clientIp);
                })
                .switchIfEmpty(Mono.fromCallable(() -> {
                    // 如果没有认证信息，使用IP限流
                    String clientIp = getClientIp(exchange);
                    return RATE_LIMIT_KEY_PREFIX + "ip:" + clientIp;
                }));
    }

    /**
     * 检查限流状态
     *
     * @param limitKey 限流键
     * @param exchange 服务器Web交换对象
     * @return 是否允许请求的Mono
     */
    private Mono<Boolean> checkRateLimit(String limitKey, ServerWebExchange exchange) {
        return getTokenBucket(limitKey)
                .flatMap(bucket -> consumeToken(limitKey, bucket))
                .map(success -> {
                    if (!success) {
                        // 设置限流响应头
                        setRateLimitHeaders(exchange, 0,
                                Integer.parseInt(
                                        unifiedConfigManager.getProperty("honyrun.rate-limit.default-capacity", "10")));
                    }
                    return success;
                });
    }

    /**
     * 获取令牌桶状态
     *
     * @param limitKey 限流键
     * @return 令牌桶状态的Mono
     */
    private Mono<TokenBucket> getTokenBucket(String limitKey) {
        String tokensKey = limitKey + ":tokens";
        String lastRefillKey = limitKey + ":last_refill";

        return redisTemplate.opsForValue().get(tokensKey)
                .cast(String.class)
                .map(Integer::parseInt)
                .defaultIfEmpty(
                        Integer.parseInt(unifiedConfigManager.getProperty("honyrun.rate-limit.default-capacity", "10")))
                .zipWith(
                        redisTemplate.opsForValue().get(lastRefillKey)
                                .cast(String.class)
                                .map(Long::parseLong)
                                .defaultIfEmpty(System.currentTimeMillis()))
                .map(tuple -> new TokenBucket(tuple.getT1(), tuple.getT2()));
    }

    /**
     * 消费令牌
     *
     * @param limitKey 限流键
     * @param bucket   令牌桶
     * @return 是否成功消费令牌的Mono
     */
    private Mono<Boolean> consumeToken(String limitKey, TokenBucket bucket) {
        return Mono.fromCallable(() -> {
            long currentTime = System.currentTimeMillis();

            // 计算需要补充的令牌数
            long timeSinceLastRefill = currentTime - bucket.lastRefillTime;
            Duration refillPeriod = Duration.ofMinutes(
                    Long.parseLong(
                            unifiedConfigManager.getProperty("honyrun.rate-limit.default-refill-period-minutes", "1")));
            long tokensToAdd = (timeSinceLastRefill / refillPeriod.toMillis())
                    * Integer.parseInt(
                            unifiedConfigManager.getProperty("honyrun.rate-limit.default-refill-tokens", "10"));

            // 更新令牌数量
            int newTokens = Math.min(
                    Integer.parseInt(unifiedConfigManager.getProperty("honyrun.rate-limit.default-capacity", "10")),
                    (int) (bucket.tokens + tokensToAdd));

            if (newTokens > 0) {
                // 消费一个令牌
                newTokens--;

                // 更新Redis中的令牌桶状态
                updateTokenBucket(limitKey, newTokens, currentTime);

                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * 更新令牌桶状态
     *
     * @param limitKey       限流键
     * @param tokens         令牌数量
     * @param lastRefillTime 最后补充时间
     */
    private void updateTokenBucket(String limitKey, int tokens, long lastRefillTime) {
        String tokensKey = limitKey + ":tokens";
        String lastRefillKey = limitKey + ":last_refill";

        Duration refillPeriod = Duration.ofMinutes(
                Long.parseLong(
                        unifiedConfigManager.getProperty("honyrun.rate-limit.default-refill-period-minutes", "1")));
        redisTemplate.opsForValue().set(tokensKey, String.valueOf(tokens), refillPeriod.multipliedBy(2))
                .and(redisTemplate.opsForValue().set(lastRefillKey, String.valueOf(lastRefillTime),
                        refillPeriod.multipliedBy(2)))
                .subscribe();
    }

    /**
     * 设置限流相关响应头
     *
     * @param exchange  服务器Web交换对象
     * @param remaining 剩余令牌数
     * @param capacity  令牌桶容量
     */
    private void setRateLimitHeaders(ServerWebExchange exchange, int remaining, int capacity) {
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(remaining));
        exchange.getResponse().getHeaders().add("X-RateLimit-Capacity", String.valueOf(capacity));
        Duration refillPeriod = Duration.ofMinutes(
                Long.parseLong(
                        unifiedConfigManager.getProperty("honyrun.rate-limit.default-refill-period-minutes", "1")));
        exchange.getResponse().getHeaders().add("X-RateLimit-Refill-Period", refillPeriod.toString());
    }

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

        // 安全处理RemoteAddress可能为null的情况
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }

        // 使用同步方法避免阻塞调用
        return "127.0.0.1"; // 默认IP地址
    }

    /**
     * 检查特定用户的限流状态
     *
     * @param userId 用户ID
     * @return 是否允许请求的Mono
     */
    public Mono<Boolean> checkUserRateLimit(String userId) {
        String limitKey = RATE_LIMIT_KEY_PREFIX + "user:" + userId;
        return getTokenBucket(limitKey)
                .flatMap(bucket -> consumeToken(limitKey, bucket));
    }

    /**
     * 检查特定IP的限流状态
     *
     * @param clientIp 客户端IP
     * @return 是否允许请求的Mono
     */
    public Mono<Boolean> checkIpRateLimit(String clientIp) {
        String limitKey = RATE_LIMIT_KEY_PREFIX + "ip:" + clientIp;
        return getTokenBucket(limitKey)
                .flatMap(bucket -> consumeToken(limitKey, bucket));
    }

    /**
     * 令牌桶内部类
     */
    private static class TokenBucket {
        private final int tokens;
        private final long lastRefillTime;

        public TokenBucket(int tokens, long lastRefillTime) {
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
        }
    }
}
