package com.honyrun.service.health;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Redis健康检查器
 *
 * <p>实现Redis连接和基本操作的健康检查，确保Redis服务可用性。
 *
 * <p><strong>检查内容：</strong>
 * <ul>
 *   <li>Redis连接状态</li>
 *   <li>基本PING操作</li>
 *   <li>响应时间监控</li>
 * </ul>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @version 1.0.0
 */
@Component
public class RedisHealthChecker implements ComponentHealthChecker {

    private static final Logger logger = LoggerFactory.getLogger(RedisHealthChecker.class);

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    /**
     * 构造函数注入
     *
     * @param reactiveRedisTemplate Redis响应式模板
     */
    public RedisHealthChecker(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @Override
    public Mono<Boolean> checkHealth() {
        LoggingUtil.debug(logger, "开始执行Redis健康检查");

        return reactiveRedisTemplate.getConnectionFactory()
            .getReactiveConnection()
            .ping()
            .map(response -> {
                boolean isHealthy = "PONG".equals(response);
                LoggingUtil.debug(logger, "Redis PING响应: {}, 健康状态: {}", response, isHealthy);
                return isHealthy;
            })
            .onErrorResume(error -> {
                LoggingUtil.error(logger, "Redis健康检查失败: {}", error.getMessage(), error);
                return Mono.just(false);
            })
            .timeout(Duration.ofSeconds(getTimeoutSeconds()))
            .doOnSuccess(healthy ->
                LoggingUtil.info(logger, "Redis健康检查完成，状态: {}", healthy ? "健康" : "不健康"))
            .doOnError(error ->
                LoggingUtil.error(logger, "Redis健康检查超时或异常: {}", error.getMessage(), error));
    }

    @Override
    public String getComponentName() {
        return "redis";
    }

    @Override
    public int getTimeoutSeconds() {
        return 12; // 增加Redis检查超时时间到12秒，与连接池配置保持一致
    }

    @Override
    public String getComponentDescription() {
        return "Redis缓存服务健康检查";
    }

    @Override
    public boolean isEnabled() {
        return reactiveRedisTemplate != null;
    }
}
