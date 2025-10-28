package com.honyrun.config.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import com.honyrun.util.LoggingUtil;

/**
 * Redis启动顺序配置
 *
 * <p>
 * <strong>核心功能：</strong>
 * <ul>
 * <li><strong>启动顺序控制</strong> - 确保Redis相关Bean按正确顺序初始化</li>
 * <li><strong>依赖管理</strong> - 管理Bean之间的依赖关系</li>
 * <li><strong>条件化启动</strong> - 根据配置决定是否启用Redis功能</li>
 * <li><strong>环境隔离</strong> - 支持不同环境的配置</li>
 * </ul>
 *
 * <p>
 * <strong>启动顺序：</strong>
 * <ol>
 * <li>RedisConnectionHealthChecker - 健康检查器</li>
 * <li>Redis连接工厂 - 连接池初始化</li>
 * <li>Redis模板 - 操作模板</li>
 * <li>Redis消息监听容器 - 消息处理</li>
 * </ol>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @modified 2025-10-25 19:52:56
 * @version 1.0.0 - 初始版本，实现Redis启动顺序控制
 * @since 1.0.0
 */
@Configuration
@Profile("dev")
@ConditionalOnProperty(name = "honyrun.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisStartupOrderConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisStartupOrderConfig.class);

    /**
     * Redis启动顺序管理器
     *
     * <p>
     * <strong>职责：</strong>
     * <ul>
     * <li>确保健康检查器优先初始化</li>
     * <li>协调各个Redis组件的启动顺序</li>
     * <li>提供启动状态监控</li>
     * </ul>
     *
     * @param healthChecker Redis连接健康检查器
     * @return 启动顺序管理器实例
     */
    @Bean("redisStartupOrderManager")
    @Order(1)
    @DependsOn("redisConnectionHealthChecker")
    public RedisStartupOrderManager redisStartupOrderManager(RedisConnectionHealthChecker healthChecker) {
        LoggingUtil.info(logger, "正在初始化Redis启动顺序管理器...");

        RedisStartupOrderManager manager = new RedisStartupOrderManager(healthChecker);

        LoggingUtil.info(logger, "Redis启动顺序管理器初始化完成");
        return manager;
    }

    /**
     * Redis启动顺序管理器内部类
     */
    public static class RedisStartupOrderManager {

        private final RedisConnectionHealthChecker healthChecker;
        private volatile boolean redisReady = false;

        public RedisStartupOrderManager(RedisConnectionHealthChecker healthChecker) {
            this.healthChecker = healthChecker;
            this.checkRedisReadiness();
        }

        /**
         * 检查Redis就绪状态
         */
        private void checkRedisReadiness() {
            try {
                // 使用响应式方式检查Redis就绪状态，避免阻塞调用
                // 在构造函数中使用subscribe而不是block，避免在Netty线程中阻塞
                healthChecker.waitForRedisReady()
                    .subscribe(
                        ready -> {
                            this.redisReady = Boolean.TRUE.equals(ready);
                            LoggingUtil.info(LoggerFactory.getLogger(RedisStartupOrderManager.class),
                                    "Redis就绪状态检查完成: {}", redisReady);
                        },
                        error -> {
                            LoggingUtil.warn(LoggerFactory.getLogger(RedisStartupOrderManager.class),
                                    "Redis就绪状态检查失败: {}", error.getMessage());
                            this.redisReady = false;
                        }
                    );
            } catch (Exception e) {
                LoggingUtil.warn(LoggerFactory.getLogger(RedisStartupOrderManager.class),
                        "Redis就绪状态检查失败: {}", e.getMessage());
                this.redisReady = false;
            }
        }

        /**
         * 获取Redis就绪状态
         *
         * @return Redis是否就绪
         */
        public boolean isRedisReady() {
            return redisReady;
        }

        /**
         * 获取健康检查器
         *
         * @return 健康检查器实例
         */
        public RedisConnectionHealthChecker getHealthChecker() {
            return healthChecker;
        }
    }
}
