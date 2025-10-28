package com.honyrun.config.reactive;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;

/**
 * 生产环境响应式Redis配置类
 *
 * <p>
 * <strong>功能说明：</strong>
 * </p>
 * <ul>
 * <li>提供生产环境Redis缓存预热功能</li>
 * <li>管理Redis缓存生命周期</li>
 * <li>支持系统配置和权限模板预热</li>
 * </ul>
 *
 * <p>
 * <strong>环境隔离：</strong>仅在生产环境激活
 * </p>
 *
 * <p>
 * <strong>Repository扫描配置：</strong>
 * 项目不使用Redis repository，已移除@EnableRedisRepositories注解，
 * 避免Spring Data Redis错误扫描R2DBC repository接口，
 * 消除"Could not safely identify store assignment"警告信息
 * </p>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-13 15:30:00
 * @modified 2025-10-27 14:28:00
 * @version 1.2.0 - 移除@EnableRedisRepositories注解，彻底解决repository扫描警告
 */
@Configuration
@Profile("prod")
public class ReactiveRedisProdConfig {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveRedisProdConfig.class);

    /**
     * 生产环境Redis缓存管理器
     *
     * @param redisTemplate 生产环境Redis模板
     * @return RedisCacheManager实例
     */
    @Bean("prodRedisCacheManagementService")
    public RedisCacheManager redisCacheManager(
            @Qualifier("prodReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> redisTemplate) {
        LoggingUtil.info(logger, "正在初始化生产环境Redis缓存管理器...");
        return new RedisCacheManager(redisTemplate);
    }

    /**
     * Redis缓存管理器内部类
     *
     * <p>
     * <strong>功能说明：</strong>
     * </p>
     * <ul>
     * <li>执行Redis缓存预热</li>
     * <li>预热系统配置数据</li>
     * <li>预热权限模板数据</li>
     * <li>预热常用业务数据</li>
     * </ul>
     */
    @Component
    @Profile("prod")
    public static class RedisCacheManager {

        private static final Logger logger = LoggerFactory.getLogger(RedisCacheManager.class);

        private final ReactiveRedisTemplate<String, Object> redisTemplate;

        /**
         * 构造函数注入 - 符合Spring Boot 3最佳实践
         *
         * @param redisTemplate 生产环境Redis模板
         */
        public RedisCacheManager(
                @Qualifier("prodReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        /**
         * 缓存预热
         * <p>
         * 在系统启动时预热关键缓存数据，包括：
         * <ul>
         * <li>系统配置预热</li>
         * <li>权限模板预热</li>
         * <li>常用数据预热</li>
         * </ul>
         */
        public Mono<Void> warmupCache() {
            LoggingUtil.info(logger, "开始Redis缓存预热");

            return warmupSystemConfiguration()
                    .then(warmupPermissionTemplates())
                    .then(warmupCommonData())
                    .doOnSuccess(v -> LoggingUtil.info(logger, "Redis缓存预热完成"))
                    .doOnError(e -> LoggingUtil.error(logger, "Redis缓存预热失败", e))
                    .onErrorResume(e -> Mono.empty());
        }

        /**
         * 预热系统配置
         *
         * @return Mono<Void>
         */
        private Mono<Void> warmupSystemConfiguration() {
            LoggingUtil.info(logger, "开始预热系统配置");

            return Mono.fromRunnable(() -> {
                // 预热应用基本配置
                redisTemplate.opsForValue().set("system:config:app.name", "HonyRun", Duration.ofHours(24)).subscribe();
                redisTemplate.opsForValue().set("system:config:app.version", "1.0.0", Duration.ofHours(24)).subscribe();
                redisTemplate.opsForValue().set("system:config:app.environment", "production", Duration.ofHours(24))
                        .subscribe();

                // 预热缓存配置
                redisTemplate.opsForValue().set("system:config:cache.ttl.default", "3600", Duration.ofHours(24))
                        .subscribe();

                // 预热安全配置
                redisTemplate.opsForValue().set("system:config:security.jwt.expiration", "86400", Duration.ofHours(24))
                        .subscribe();

                LoggingUtil.info(logger, "系统配置预热完成");
            });
        }

        /**
         * 预热权限模板
         *
         * @return Mono<Void>
         */
        private Mono<Void> warmupPermissionTemplates() {
            LoggingUtil.info(logger, "开始预热权限模板");

            return Mono.fromRunnable(() -> {
                // 预热系统用户权限模板
                redisTemplate.opsForValue().set("permission:template:SYSTEM_USER",
                        "system:read,system:write,system:admin", Duration.ofHours(12)).subscribe();

                // 预热普通用户权限模板
                redisTemplate.opsForValue().set("permission:template:NORMAL_USER",
                        "user:read,user:write", Duration.ofHours(12)).subscribe();

                // 预热访客权限模板
                redisTemplate.opsForValue().set("permission:template:GUEST",
                        "public:read", Duration.ofHours(12)).subscribe();

                LoggingUtil.info(logger, "权限模板预热完成");
            });
        }

        /**
         * 预热常用数据
         *
         * @return Mono<Void>
         */
        private Mono<Void> warmupCommonData() {
            LoggingUtil.info(logger, "开始预热常用数据");

            return Mono.fromRunnable(() -> {
                // 预热用户类型数据
                redisTemplate.opsForValue().set("common:data:user_types",
                        "SYSTEM_USER,NORMAL_USER,GUEST", Duration.ofHours(6)).subscribe();

                // 预热系统状态数据
                redisTemplate.opsForValue().set("common:data:system_status",
                        "ACTIVE", Duration.ofHours(1)).subscribe();

                // 预热默认设置
                redisTemplate.opsForValue().set("common:data:default_settings",
                        "{\"theme\":\"light\",\"language\":\"zh-CN\"}", Duration.ofHours(12)).subscribe();

                LoggingUtil.info(logger, "常用数据预热完成");
            });
        }
    }
}
