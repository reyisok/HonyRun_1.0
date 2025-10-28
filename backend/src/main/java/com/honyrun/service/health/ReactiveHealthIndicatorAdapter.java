package com.honyrun.service.health;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 响应式健康指示器适配器
 *
 * <p>将统一健康检查服务集成到Spring Boot Actuator健康检查体系中，
 * 提供标准的/actuator/health端点支持。
 *
 * <p><strong>功能特性：</strong>
 * <ul>
 *   <li>集成统一健康检查服务</li>
 *   <li>提供Spring Boot Actuator标准接口</li>
 *   <li>支持响应式编程模型</li>
 *   <li>统一健康状态格式转换</li>
 * </ul>
 *
 * <p><strong>注意：</strong>
 * 此类通过HealthCheckConfiguration中的@Bean配置注册，
 * 不使用@Component注解以避免重复注册导致的Spring Boot Actuator冲突。
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @modified 2025-10-26 01:36:12
 * @version 1.0.1
 */
public class ReactiveHealthIndicatorAdapter implements ReactiveHealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveHealthIndicatorAdapter.class);

    private final UnifiedHealthCheckService unifiedHealthCheckService;

    /**
     * 构造函数注入
     *
     * @param unifiedHealthCheckService 统一健康检查服务
     */
    public ReactiveHealthIndicatorAdapter(UnifiedHealthCheckService unifiedHealthCheckService) {
        this.unifiedHealthCheckService = unifiedHealthCheckService;
    }

    @Override
    public Mono<Health> health() {
        LoggingUtil.debug(logger, "执行Spring Boot Actuator健康检查");

        return unifiedHealthCheckService.checkAllHealth()
            .map(this::convertToActuatorHealth)
            .onErrorResume(error -> {
                LoggingUtil.error(logger, "健康检查适配器执行失败", error);
                return Mono.just(Health.down()
                    .withDetail("error", error.getMessage())
                    .withDetail("adapter", "ReactiveHealthIndicatorAdapter")
                    .build());
            })
            .doOnSuccess(health ->
                LoggingUtil.debug(logger, "Spring Boot Actuator健康检查完成，状态: {}", health.getStatus()));
    }

    /**
     * 将统一健康状态转换为Spring Boot Actuator Health格式
     *
     * @param healthStatusMap 统一健康状态Map
     * @return Spring Boot Actuator Health对象
     */
    private Health convertToActuatorHealth(Map<String, HealthStatus> healthStatusMap) {
        boolean allHealthy = healthStatusMap.values().stream()
            .allMatch(HealthStatus::isHealthy);

        Health.Builder healthBuilder = allHealthy ? Health.up() : Health.down();

        // 添加系统级别统计信息
        long healthyCount = healthStatusMap.values().stream()
            .mapToLong(status -> status.isHealthy() ? 1 : 0)
            .sum();

        healthBuilder
            .withDetail("totalComponents", healthStatusMap.size())
            .withDetail("healthyComponents", healthyCount)
            .withDetail("unhealthyComponents", healthStatusMap.size() - healthyCount);

        // 添加各组件详细状态
        for (Map.Entry<String, HealthStatus> entry : healthStatusMap.entrySet()) {
            String componentName = entry.getKey();
            HealthStatus status = entry.getValue();

            Health.Builder componentBuilder = status.isHealthy() ?
                Health.up() : Health.down();

            // 添加组件详细信息
            if (status.getError() != null) {
                componentBuilder.withDetail("error", status.getError());
            }

            if (status.getResponseTime() != null) {
                componentBuilder.withDetail("responseTime", status.getResponseTime() + "ms");
            }

            if (status.getDetails() != null && !status.getDetails().isEmpty()) {
                status.getDetails().forEach(componentBuilder::withDetail);
            }

            componentBuilder
                .withDetail("timestamp", status.getTimestamp())
                .withDetail("status", status.getStatus().getCode());

            healthBuilder.withDetail(componentName, componentBuilder.build());
        }

        return healthBuilder.build();
    }
}
