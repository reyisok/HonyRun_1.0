package com.honyrun.service.startup;

import com.honyrun.config.cache.RedisConnectionHealthChecker;
import com.honyrun.service.health.UnifiedHealthCheckService;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 统一启动管理器
 *
 * <p>管理应用启动顺序，简化组件依赖关系，提供清晰的启动流程。
 *
 * <p><strong>启动流程：</strong>
 * <ol>
 *   <li>数据库连接检查</li>
 *   <li>Redis连接检查（支持降级）</li>
 *   <li>基础组件初始化</li>
 *   <li>启动完成通知</li>
 * </ol>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @version 1.0.0
 */
@Service
public class UnifiedStartupManager {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedStartupManager.class);

    private final RedisConnectionHealthChecker redisHealthChecker;
    private final UnifiedHealthCheckService healthCheckService;

    /**
     * 构造函数注入依赖
     *
     * @param redisHealthChecker Redis连接健康检查器
     * @param healthCheckService 统一健康检查服务
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public UnifiedStartupManager(RedisConnectionHealthChecker redisHealthChecker,
                               UnifiedHealthCheckService healthCheckService) {
        this.redisHealthChecker = redisHealthChecker;
        this.healthCheckService = healthCheckService;
    }

    private final AtomicBoolean startupCompleted = new AtomicBoolean(false);
    private long startupStartTime;

    /**
     * 启动状态枚举
     */
    public enum StartupStatus {
        NOT_STARTED,    // 未开始
        IN_PROGRESS,    // 进行中
        COMPLETED,      // 已完成
        FAILED          // 失败
    }

    private volatile StartupStatus currentStatus = StartupStatus.NOT_STARTED;

    /**
     * 应用启动完成事件监听器
     *
     * @param event 应用就绪事件
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        if (startupCompleted.compareAndSet(false, true)) {
            startupStartTime = System.currentTimeMillis();
            executeStartupSequence()
                .subscribe(
                    success -> {
                        if (success) {
                            onStartupSuccess();
                        } else {
                            onStartupFailure("启动序列执行失败");
                        }
                    },
                    error -> onStartupFailure("启动序列异常: " + error.getMessage())
                );
        }
    }

    /**
     * 执行启动序列
     *
     * @return 启动是否成功
     */
    public Mono<Boolean> executeStartupSequence() {
        LoggingUtil.info(logger, "开始执行统一启动序列");
        currentStatus = StartupStatus.IN_PROGRESS;

        return Mono.just(true)
            // 第一步：检查数据库连接
            .flatMap(ignored -> checkDatabaseConnection())
            // 第二步：检查Redis连接（支持降级）
            .flatMap(ignored -> checkRedisConnection())
            // 第三步：执行健康检查（优化超时和重试）
            .flatMap(ignored -> performInitialHealthCheckWithRetry())
            // 第四步：完成启动
            .map(ignored -> {
                currentStatus = StartupStatus.COMPLETED;
                return true;
            })
            .timeout(Duration.ofSeconds(90)) // 增加超时时间到90秒
            .onErrorResume(error -> {
                LoggingUtil.error(logger, "启动序列执行失败: {}", error.getMessage());
                currentStatus = StartupStatus.FAILED;
                return Mono.just(false);
            });
    }

    /**
     * 检查数据库连接
     *
     * @return 检查结果
     */
    private Mono<Boolean> checkDatabaseConnection() {
        LoggingUtil.info(logger, "启动序列 - 步骤1: 检查数据库连接");

        return healthCheckService.checkComponentHealth("database")
            .map(healthStatus -> healthStatus.getStatus().getCode().equals("UP"))
            .doOnNext(healthy -> {
                if (healthy) {
                    LoggingUtil.info(logger, "数据库连接检查通过");
                } else {
                    LoggingUtil.error(logger, "数据库连接检查失败，应用无法启动");
                }
            })
            .filter(healthy -> healthy)
            .switchIfEmpty(Mono.error(new RuntimeException("数据库连接失败，应用启动中止")));
    }

    /**
     * 检查Redis连接
     *
     * @return 检查结果
     */
    private Mono<Boolean> checkRedisConnection() {
        LoggingUtil.info(logger, "启动序列 - 步骤2: 检查Redis连接");

        return redisHealthChecker.waitForRedisReady()
            .doOnNext(ready -> {
                if (ready) {
                    LoggingUtil.info(logger, "Redis连接检查通过");
                } else {
                    LoggingUtil.warn(logger, "Redis连接不可用，将使用降级模式");
                }
            })
            .map(ready -> true); // Redis失败不影响启动，支持降级
    }

    /**
     * 执行初始健康检查（带重试机制）
     *
     * @return 检查结果
     */
    private Mono<Boolean> performInitialHealthCheckWithRetry() {
        LoggingUtil.info(logger, "启动序列 - 步骤3: 执行初始健康检查（带重试）");

        return healthCheckService.checkAllHealth()
            .timeout(Duration.ofSeconds(30)) // 单次检查超时30秒
            .retry(2) // 最多重试2次
            .doOnNext(healthMap -> {
                LoggingUtil.info(logger, "初始健康检查完成: {}", healthMap);
            })
            .map(healthMap -> {
                 // 检查关键组件是否健康
                 boolean databaseHealthy = healthMap.getOrDefault("database", 
                     com.honyrun.service.health.HealthStatus.down("database")).isHealthy();
                 
                 if (!databaseHealthy) {
                     LoggingUtil.warn(logger, "数据库健康检查失败，但继续启动");
                 }
                 
                 // Redis可以降级，不影响启动
                 boolean redisHealthy = healthMap.getOrDefault("redis", 
                     com.honyrun.service.health.HealthStatus.down("redis")).isHealthy();
                 
                 if (!redisHealthy) {
                     LoggingUtil.warn(logger, "Redis健康检查失败，启用降级模式");
                 }
                 
                 return true; // 即使部分组件不健康也允许启动
             })
            .onErrorResume(error -> {
                LoggingUtil.warn(logger, "初始健康检查失败，但允许应用启动: {}", error.getMessage());
                return Mono.just(true); // 健康检查失败不阻止启动
            });
    }

    /**
     * 执行初始健康检查（原方法保留作为备用）
     *
     * @return 检查结果
     */
    private Mono<Boolean> performInitialHealthCheck() {
        LoggingUtil.info(logger, "启动序列 - 步骤3: 执行初始健康检查");

        return healthCheckService.checkAllHealth()
            .doOnNext(healthMap -> {
                LoggingUtil.info(logger, "初始健康检查完成: {}", healthMap);
            })
            .map(healthMap -> true);
    }

    /**
     * 启动成功处理
     */
    private void onStartupSuccess() {
        long duration = System.currentTimeMillis() - startupStartTime;
        LoggingUtil.info(logger, "应用启动成功完成，耗时: {}ms", duration);

        // 清除健康检查缓存，确保后续检查的准确性
        healthCheckService.clearHealthCache();
    }

    /**
     * 启动失败处理
     *
     * @param reason 失败原因
     */
    private void onStartupFailure(String reason) {
        long duration = System.currentTimeMillis() - startupStartTime;
        LoggingUtil.error(logger, "应用启动失败: {}, 耗时: {}ms", reason, duration);
        currentStatus = StartupStatus.FAILED;
    }

    /**
     * 获取当前启动状态
     *
     * @return 启动状态
     */
    public StartupStatus getCurrentStatus() {
        return currentStatus;
    }

    /**
     * 检查启动是否完成
     *
     * @return 是否完成启动
     */
    public boolean isStartupCompleted() {
        return startupCompleted.get() && currentStatus == StartupStatus.COMPLETED;
    }
}
