package com.honyrun.config;

import com.honyrun.service.health.ComponentHealthChecker;
import com.honyrun.service.health.DatabaseHealthChecker;
import com.honyrun.service.health.RedisHealthChecker;
import com.honyrun.service.health.ReactiveHealthIndicatorAdapter;
import com.honyrun.service.health.UnifiedHealthCheckService;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.List;

/**
 * 健康检查配置类
 * 
 * <p>负责配置和注册统一健康检查服务及其相关组件，包括：
 * <ul>
 *   <li>组件健康检查器的注册和配置</li>
 *   <li>Spring Boot Actuator集成适配器</li>
 *   <li>健康检查服务的依赖注入配置</li>
 * </ul>
 * 
 * <p>设计原则：
 * <ul>
 *   <li>响应式编程：所有健康检查操作均为非阻塞</li>
 *   <li>可扩展性：支持动态添加新的健康检查组件</li>
 *   <li>统一接口：提供标准化的健康检查接口</li>
 *   <li>Spring Boot集成：与Actuator无缝集成</li>
 * </ul>
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @version 1.0.0
 */
@Configuration
public class HealthCheckConfiguration {

    /**
     * 配置Redis健康检查器
     * 
     * @param reactiveRedisTemplate Redis响应式模板
     * @return Redis健康检查器实例
     */
    @Bean
    public RedisHealthChecker redisHealthChecker(@org.springframework.beans.factory.annotation.Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        return new RedisHealthChecker(reactiveRedisTemplate);
    }

    /**
     * 配置数据库健康检查器
     * 
     * @param databaseClient 数据库客户端
     * @return 数据库健康检查器实例
     */
    @Bean
    public DatabaseHealthChecker databaseHealthChecker(DatabaseClient databaseClient) {
        return new DatabaseHealthChecker(databaseClient);
    }

    /**
     * 配置统一健康检查服务
     * 
     * @param healthCheckers 所有组件健康检查器列表
     * @param unifiedConfigManager 统一配置管理器
     * @return 统一健康检查服务实例
     */
    @Bean
    public UnifiedHealthCheckService unifiedHealthCheckService(List<ComponentHealthChecker> healthCheckers, UnifiedConfigManager unifiedConfigManager) {
        return new UnifiedHealthCheckService(healthCheckers, unifiedConfigManager);
    }

    /**
     * 配置Spring Boot Actuator健康指标适配器
     * 
     * @param unifiedHealthCheckService 统一健康检查服务
     * @return 响应式健康指标适配器
     */
    @Bean("healthCheckConfigurationAdapter")
    public ReactiveHealthIndicator reactiveHealthIndicatorAdapter(UnifiedHealthCheckService unifiedHealthCheckService) {
        return new ReactiveHealthIndicatorAdapter(unifiedHealthCheckService);
    }
}
