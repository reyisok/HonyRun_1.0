package com.honyrun.service.monitoring;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.honyrun.util.LoggingUtil;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

/**
 * 应用健康检查服务
 *
 * 提供全面的应用组件健康状态监控，包括：
 * - 数据库连接健康检查（MySQL R2DBC）
 * - Redis连接健康检查
 * - 应用核心组件状态监控
 * - 外部依赖服务健康检查
 * - 业务功能可用性检查
 *
 * 健康检查策略：
 * - 响应式健康检查，支持非阻塞操作
 * - 定时执行健康检查，缓存检查结果
 * - 分级健康状态，支持部分可用状态
 * - 健康状态变更通知和告警
 *
 * 性能优化：
 * - 异步执行健康检查，避免阻塞主线程
 * - 合理设置超时时间，防止检查任务阻塞
 * - 健康状态缓存，减少重复检查开销
 * - 智能重试机制，提高检查可靠性
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-26 01:36:12
 * @version 1.0.0
 */
@Service
public class ApplicationHealthService implements ReactiveHealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationHealthService.class);

    private final DatabaseClient databaseClient;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;

    // 健康检查指标
    private final Counter healthCheckCounter;
    private final Counter healthCheckFailureCounter;
    private final Timer healthCheckTimer;

    // 健康状态缓存
    private final Map<String, HealthStatus> healthStatusCache = new ConcurrentHashMap<>();
    private final AtomicLong lastHealthCheckTime = new AtomicLong(0);
    private final AtomicBoolean overallHealthy = new AtomicBoolean(true);

    public ApplicationHealthService(DatabaseClient databaseClient,
            @Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> redisTemplate,
            MeterRegistry meterRegistry) {
        this.databaseClient = databaseClient;
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;

        // 初始化监控指标
        this.healthCheckCounter = Counter.builder("honyrun.health.checks.total")
                .description("健康检查总次数")
                .register(meterRegistry);

        this.healthCheckFailureCounter = Counter.builder("honyrun.health.checks.failures")
                .description("健康检查失败次数")
                .register(meterRegistry);

        this.healthCheckTimer = Timer.builder("honyrun.health.checks.duration")
                .description("健康检查耗时")
                .register(meterRegistry);

        LoggingUtil.info(logger, "应用健康检查服务初始化完成");
    }

    /**
     * 初始化组件后的回调方法，用于注册需要 this 引用的指标
     * 使用 @PostConstruct 避免 this-escape 警告
     */
    @PostConstruct
    private void initializeHealthMetrics() {
        registerHealthMetrics();
    }

    /**
     * 注册健康状态监控指标
     */
    private void registerHealthMetrics() {
        // 整体健康状态指标
        Gauge.builder("honyrun.health.overall.status", this, self -> self.overallHealthy.get() ? 1.0 : 0.0)
                .description("应用整体健康状态（1=健康，0=不健康）")
                .register(meterRegistry);

        // 数据库健康状态指标
        Gauge.builder("honyrun.health.database.status", this, self -> {
            HealthStatus status = self.healthStatusCache.get("database");
            return status != null && status.isHealthy() ? 1.0 : 0.0;
        })
                .description("数据库健康状态（1=健康，0=不健康）")
                .register(meterRegistry);

        // Redis健康状态指标
        Gauge.builder("honyrun.health.redis.status", this, self -> {
            HealthStatus status = self.healthStatusCache.get("redis");
            return status != null && status.isHealthy() ? 1.0 : 0.0;
        })
                .description("Redis健康状态（1=健康，0=不健康）")
                .register(meterRegistry);
    }

    /**
     * 响应式健康检查实现
     *
     * 实现Spring Boot Actuator的ReactiveHealthIndicator接口，
     * 提供响应式的健康状态检查。
     */
    @Override
    public Mono<Health> health() {
        return performHealthCheck()
                .map(this::buildHealthResponse)
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "健康检查执行失败", error);
                    return Mono.just(Health.down(error)
                            .withDetail("error", error.getMessage())
                            .withDetail("timestamp", System.currentTimeMillis())
                            .build());
                });
    }

    /**
     * 执行全面的健康检查
     *
     * @return 健康检查结果的响应式流
     */
    public Mono<Map<String, HealthStatus>> performHealthCheck() {
        Timer.Sample sample = Timer.start(meterRegistry);

        return Mono.fromCallable(() -> {
            healthCheckCounter.increment();
            LoggingUtil.debug(logger, "开始执行应用健康检查");
            return System.currentTimeMillis();
        })
                .flatMap(startTime -> Mono.zip(
                        checkDatabaseHealth(),
                        checkRedisHealth(),
                        checkApplicationComponents())
                        .map(tuple -> {
                            Map<String, HealthStatus> results = new ConcurrentHashMap<>();
                            results.put("database", tuple.getT1());
                            results.put("redis", tuple.getT2());
                            results.put("application", tuple.getT3());

                            // 更新缓存
                            healthStatusCache.putAll(results);
                            lastHealthCheckTime.set(System.currentTimeMillis());

                            // 更新整体健康状态
                            boolean allHealthy = results.values().stream()
                                    .allMatch(HealthStatus::isHealthy);
                            overallHealthy.set(allHealthy);

                            LoggingUtil.debug(logger, "应用健康检查完成 - 整体状态: {}", allHealthy ? "健康" : "异常");
                            return results;
                        }))
                .doOnError(error -> {
                    healthCheckFailureCounter.increment();
                    LoggingUtil.error(logger, "应用健康检查失败", error);
                })
                .doFinally(signalType -> sample.stop(healthCheckTimer));
    }

    /**
     * 检查数据库健康状态
     *
     * @return 数据库健康状态的响应式流
     */
    private Mono<HealthStatus> checkDatabaseHealth() {
        return databaseClient.sql("SELECT 1")
                .fetch()
                .first()
                .timeout(Duration.ofSeconds(8)) // 增加超时时间到8秒，与配置文件一致
                .map(result -> {
                    LoggingUtil.debug(logger, "数据库健康检查成功");
                    return HealthStatus.healthy("数据库连接正常");
                })
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "数据库健康检查失败: {}", error.getMessage());
                    return Mono.just(HealthStatus.unhealthy("数据库连接异常: " + error.getMessage()));
                });
    }

    /**
     * 检查Redis健康状态
     *
     * @return Redis健康状态的响应式流
     */
    private Mono<HealthStatus> checkRedisHealth() {
        String testKey = "health:check:" + System.currentTimeMillis();
        String testValue = "ping";

        return redisTemplate.opsForValue()
                .set(testKey, testValue, Duration.ofSeconds(10))
                .then(redisTemplate.opsForValue().get(testKey))
                .timeout(Duration.ofSeconds(12)) // 增加超时时间到12秒，与配置文件一致
                .flatMap(value -> {
                    if (testValue.equals(value)) {
                        LoggingUtil.debug(logger, "Redis健康检查成功");
                        return redisTemplate.delete(testKey)
                                .then(Mono.just(HealthStatus.healthy("Redis连接正常")));
                    } else {
                        LoggingUtil.warn(logger, "Redis健康检查失败: 数据不一致");
                        return Mono.just(HealthStatus.unhealthy("Redis数据不一致"));
                    }
                })
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "Redis健康检查失败: {}", error.getMessage());
                    return Mono.just(HealthStatus.unhealthy("Redis连接异常: " + error.getMessage()));
                });
    }

    /**
     * 检查应用核心组件健康状态
     *
     * @return 应用组件健康状态的响应式流
     */
    private Mono<HealthStatus> checkApplicationComponents() {
        return Mono.fromCallable(() -> {
            // 检查JVM内存使用情况
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double memoryUsage = (double) usedMemory / maxMemory * 100;

            if (memoryUsage > 90.0) {
                LoggingUtil.warn(logger, "应用内存使用率过高: {}%", String.format("%.2f", memoryUsage));
                return HealthStatus.unhealthy(String.format("内存使用率过高: %.2f%%", memoryUsage));
            }

            // 检查线程数量
            int activeThreads = Thread.activeCount();
            if (activeThreads > 1000) {
                LoggingUtil.warn(logger, "活跃线程数过多: {}", activeThreads);
                return HealthStatus.unhealthy("活跃线程数过多: " + activeThreads);
            }

            LoggingUtil.debug(logger, "应用组件健康检查成功 - 内存使用率: {}%, 活跃线程: {}",
                    String.format("%.2f", memoryUsage), activeThreads);
            return HealthStatus.healthy("应用组件运行正常");
        });
    }

    /**
     * 定时执行健康检查
     *
     * 每60秒执行一次完整的健康检查，更新健康状态缓存。
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    public void scheduledHealthCheck() {
        performHealthCheck()
                .subscribe(
                        results -> LoggingUtil.debug(logger, "定时健康检查完成"),
                        error -> LoggingUtil.error(logger, "定时健康检查失败", error));
    }

    /**
     * 获取健康状态概览
     *
     * @return 健康状态概览的响应式流
     */
    public Mono<Map<String, Object>> getHealthOverview() {
        return Mono.fromCallable(() -> {
            Map<String, Object> overview = new ConcurrentHashMap<>();
            overview.put("overall.healthy", overallHealthy.get());
            overview.put("last.check.time", lastHealthCheckTime.get());
            overview.put("components", healthStatusCache);

            // 统计健康组件数量
            long healthyCount = healthStatusCache.values().stream()
                    .mapToLong(status -> status.isHealthy() ? 1 : 0)
                    .sum();
            overview.put("healthy.components", healthyCount);
            overview.put("total.components", healthStatusCache.size());

            return overview;
        });
    }

    /**
     * 构建健康检查响应
     *
     * @param healthResults 健康检查结果
     * @return Health响应对象
     */
    private Health buildHealthResponse(Map<String, HealthStatus> healthResults) {
        boolean allHealthy = healthResults.values().stream()
                .allMatch(HealthStatus::isHealthy);

        Health.Builder builder = allHealthy ? Health.up() : Health.down();

        // 添加各组件健康状态详情
        healthResults.forEach((component, status) -> {
            builder.withDetail(component + ".status", status.isHealthy() ? "UP" : "DOWN")
                    .withDetail(component + ".message", status.getMessage())
                    .withDetail(component + ".timestamp", status.getTimestamp());
        });

        // 添加整体信息
        builder.withDetail("overall.healthy", allHealthy)
                .withDetail("check.timestamp", System.currentTimeMillis())
                .withDetail("components.total", healthResults.size())
                .withDetail("components.healthy",
                        healthResults.values().stream().mapToLong(s -> s.isHealthy() ? 1 : 0).sum());

        return builder.build();
    }

    /**
     * 健康状态内部类
     */
    public static class HealthStatus {
        private final boolean healthy;
        private final String message;
        private final long timestamp;

        private HealthStatus(boolean healthy, String message) {
            this.healthy = healthy;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public static HealthStatus healthy(String message) {
            return new HealthStatus(true, message);
        }

        public static HealthStatus unhealthy(String message) {
            return new HealthStatus(false, message);
        }

        public boolean isHealthy() {
            return healthy;
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format("HealthStatus{healthy=%s, message='%s', timestamp=%d}",
                    healthy, message, timestamp);
        }
    }
}
