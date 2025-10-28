package com.honyrun.service.health;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 统一健康检查服务
 *
 * <p>
 * 整合所有组件的健康检查，避免重复检查，提供统一的健康状态管理。
 *
 * <p>
 * <strong>功能特性：</strong>
 * <ul>
 * <li>统一管理所有组件健康检查</li>
 * <li>缓存健康检查结果，避免频繁检查</li>
 * <li>支持批量健康检查</li>
 * <li>提供健康状态汇总</li>
 * <li>响应式编程支持</li>
 * <li>可扩展的组件检查器架构</li>
 * </ul>
 *
 * <p>
 * <strong>设计原则：</strong>
 * <ul>
 * <li>响应式编程：所有操作返回Mono/Flux类型</li>
 * <li>非阻塞执行：避免阻塞线程</li>
 * <li>缓存机制：减少重复检查开销</li>
 * <li>统一接口：标准化健康检查流程</li>
 * <li>可扩展性：支持动态添加新的健康检查器</li>
 * </ul>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @modified 2025-10-25 19:52:56
 * @version 1.1.0
 */
@Service
public class UnifiedHealthCheckService {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedHealthCheckService.class);

    /** 健康检查器列表 */
    private final List<ComponentHealthChecker> healthCheckers;

    /** 健康状态缓存，避免频繁检查 */
    private final Map<String, CachedHealthStatus> healthStatusCache = new ConcurrentHashMap<>();

    /** 统一配置管理器 */
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 缓存的健康状态
     */
    private static class CachedHealthStatus {
        private final HealthStatus healthStatus;
        private final LocalDateTime cacheTime;
        private final long cacheDurationSeconds;

        public CachedHealthStatus(HealthStatus healthStatus, long cacheDurationSeconds) {
            this.healthStatus = healthStatus;
            this.cacheTime = LocalDateTime.now();
            this.cacheDurationSeconds = cacheDurationSeconds;
        }

        public HealthStatus getHealthStatus() {
            return healthStatus;
        }

        public boolean isExpired() {
            return Duration.between(cacheTime, LocalDateTime.now())
                    .getSeconds() > cacheDurationSeconds;
        }
    }

    /**
     * 构造函数，自动注入所有健康检查器
     *
     * @param healthCheckers       健康检查器列表
     * @param unifiedConfigManager 统一配置管理器
     */
    public UnifiedHealthCheckService(List<ComponentHealthChecker> healthCheckers,
            UnifiedConfigManager unifiedConfigManager) {
        this.healthCheckers = healthCheckers != null ? healthCheckers : List.of();
        this.unifiedConfigManager = unifiedConfigManager;
        LoggingUtil.info(logger, "统一健康检查服务初始化完成，注册了{}个健康检查器: {}",
                this.healthCheckers.size(),
                this.healthCheckers.stream()
                        .map(ComponentHealthChecker::getComponentName)
                        .collect(Collectors.joining(", ")));
    }

    /**
     * 检查指定组件的健康状态
     *
     * <p>
     * 根据组件名称查找对应的健康检查器并执行检查。
     * 支持缓存机制，避免频繁检查同一组件。
     *
     * @param componentName 组件名称
     * @return 组件健康状态的Mono包装
     */
    public Mono<HealthStatus> checkComponentHealth(String componentName) {
        return getCachedOrCheck(componentName, () -> {
            return findHealthChecker(componentName)
                    .flatMap(this::executeHealthCheck)
                    .switchIfEmpty(Mono.just(HealthStatus.unknown(componentName)
                            .withError("未找到对应的健康检查器")));
        });
    }

    /**
     * 检查所有组件的健康状态
     *
     * <p>
     * 并行执行所有已注册组件的健康检查，返回汇总结果。
     *
     * @return 所有组件健康状态的Map，键为组件名称，值为健康状态
     */
    public Mono<Map<String, HealthStatus>> checkAllHealth() {
        LoggingUtil.info(logger, "开始执行所有组件健康检查，共{}个组件", healthCheckers.size());

        return Flux.fromIterable(healthCheckers)
                .filter(ComponentHealthChecker::isEnabled)
                .flatMap(checker -> checkComponentHealth(checker.getComponentName())
                        .map(status -> Map.entry(checker.getComponentName(), status))
                        .onErrorReturn(Map.entry(checker.getComponentName(),
                                HealthStatus.down(checker.getComponentName())
                                        .withError("健康检查执行异常"))))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .doOnSuccess(results -> LoggingUtil.info(logger, "所有组件健康检查完成，结果: {}",
                        results.entrySet().stream()
                                .map(entry -> entry.getKey() + "=" + entry.getValue().getStatus().getCode())
                                .collect(Collectors.joining(", "))))
                .doOnError(error -> LoggingUtil.error(logger, "执行所有组件健康检查时发生异常", error));
    }

    /**
     * 检查系统整体健康状态
     *
     * <p>
     * 基于所有组件的健康状态判断系统是否健康。
     * 只有当所有启用的组件都健康时，系统才被认为是健康的。
     *
     * @return 系统是否健康的Mono包装
     */
    public Mono<Boolean> isSystemHealthy() {
        return checkAllHealth()
                .map(healthMap -> healthMap.values().stream()
                        .allMatch(HealthStatus::isHealthy))
                .doOnSuccess(healthy -> LoggingUtil.info(logger, "系统整体健康状态: {}", healthy ? "健康" : "不健康"));
    }

    /**
     * 获取系统健康状态汇总
     *
     * <p>
     * 返回包含所有组件状态和系统整体状态的汇总信息。
     *
     * @return 系统健康状态汇总
     */
    public Mono<HealthStatus> getSystemHealthSummary() {
        return checkAllHealth()
                .map(healthMap -> {
                    boolean allHealthy = healthMap.values().stream().allMatch(HealthStatus::isHealthy);
                    long healthyCount = healthMap.values().stream().mapToLong(status -> status.isHealthy() ? 1 : 0)
                            .sum();

                    HealthStatus systemStatus = allHealthy ? HealthStatus.up("system") : HealthStatus.down("system");

                    return systemStatus
                            .withDetail("totalComponents", healthMap.size())
                            .withDetail("healthyComponents", healthyCount)
                            .withDetail("unhealthyComponents", healthMap.size() - healthyCount)
                            .withDetail("components", healthMap);
                });
    }

    /**
     * 清除健康状态缓存
     *
     * <p>
     * 清除所有缓存的健康检查结果，强制下次检查时重新执行。
     */
    public void clearHealthCache() {
        healthStatusCache.clear();
        LoggingUtil.info(logger, "健康状态缓存已清除");
    }

    /**
     * 清除指定组件的健康状态缓存
     *
     * @param componentName 组件名称
     */
    public void clearComponentHealthCache(String componentName) {
        healthStatusCache.remove(componentName);
        LoggingUtil.info(logger, "组件{}的健康状态缓存已清除", componentName);
    }

    /**
     * 获取已注册的健康检查器列表
     *
     * @return 健康检查器组件名称列表
     */
    public List<String> getRegisteredComponents() {
        return healthCheckers.stream()
                .map(ComponentHealthChecker::getComponentName)
                .collect(Collectors.toList());
    }

    /**
     * 查找指定组件的健康检查器
     *
     * @param componentName 组件名称
     * @return 健康检查器的Mono包装，如果未找到则返回empty
     */
    private Mono<ComponentHealthChecker> findHealthChecker(String componentName) {
        return Flux.fromIterable(healthCheckers)
                .filter(checker -> componentName.equals(checker.getComponentName()))
                .next();
    }

    /**
     * 执行健康检查
     *
     * @param checker 健康检查器
     * @return 健康状态
     */
    private Mono<HealthStatus> executeHealthCheck(ComponentHealthChecker checker) {
        long startTime = System.currentTimeMillis();

        return checker.checkHealth()
                .timeout(Duration.ofSeconds(checker.getTimeoutSeconds()))
                .map(healthy -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    HealthStatus status = healthy ? HealthStatus.up(checker.getComponentName())
                            : HealthStatus.down(checker.getComponentName());

                    return status
                            .withResponseTime(responseTime)
                            .withDetail("description", checker.getComponentDescription())
                            .withDetail("timeout", checker.getTimeoutSeconds() + "s");
                })
                .onErrorResume(error -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    LoggingUtil.error(logger, "组件{}健康检查失败", checker.getComponentName(), error);

                    return Mono.just(HealthStatus.down(checker.getComponentName())
                            .withError(error.getMessage())
                            .withResponseTime(responseTime)
                            .withDetail("description", checker.getComponentDescription()));
                });
    }

    /**
     * 获取缓存的健康状态或执行新的检查
     *
     * @param componentName 组件名称
     * @param checker       健康检查执行器
     * @return 健康状态
     */
    private Mono<HealthStatus> getCachedOrCheck(String componentName,
            java.util.function.Supplier<Mono<HealthStatus>> checker) {
        CachedHealthStatus cached = healthStatusCache.get(componentName);

        if (cached != null && !cached.isExpired()) {
            LoggingUtil.debug(logger, "使用缓存的健康状态: {}", componentName);
            return Mono.just(cached.getHealthStatus());
        }

        return checker.get()
                .doOnSuccess(status -> {
                    // 通过UnifiedConfigManager获取缓存时间配置
                    long cacheDuration = Long
                            .parseLong(unifiedConfigManager.getProperty("honyrun.health.cache-duration", "300"));
                    healthStatusCache.put(componentName, new CachedHealthStatus(status, cacheDuration));
                    LoggingUtil.debug(logger, "缓存健康状态: {} = {}", componentName, status.getStatus().getCode());
                });
    }
}
