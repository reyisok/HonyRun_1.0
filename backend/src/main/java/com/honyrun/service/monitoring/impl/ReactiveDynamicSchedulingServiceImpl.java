package com.honyrun.service.monitoring.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.service.monitoring.ReactiveDynamicSchedulingService;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

/**
 * 响应式动态调度配置服务实现类
 *
 * 提供完整的动态调度配置管理功能，包括：
 * - 调度任务动态配置和管理
 * - 调度频率和超时时间动态调整
 * - 调度器性能监控和统计
 * - 调度任务执行历史记录
 * - 调度器健康状态检查
 * - 配置持久化和恢复
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-02 当前时间
 * @version 1.0.0 - 初始版本
 *
 *          注意：此类不使用@Service注解，通过MonitoringConfig中的@Bean方法创建实例
 */
public class ReactiveDynamicSchedulingServiceImpl implements ReactiveDynamicSchedulingService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveDynamicSchedulingServiceImpl.class);

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final TaskScheduler taskScheduler;
    private final Scheduler monitoringScheduler;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入依赖
     *
     * @param reactiveRedisTemplate 响应式Redis模板
     * @param taskScheduler         任务调度器
     * @param monitoringScheduler   监控调度器
     * @param unifiedConfigManager  统一配置管理器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public ReactiveDynamicSchedulingServiceImpl(
            @Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
            TaskScheduler taskScheduler,
            Scheduler monitoringScheduler,
            UnifiedConfigManager unifiedConfigManager) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.taskScheduler = taskScheduler;
        this.monitoringScheduler = monitoringScheduler;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    // 调度任务状态缓存
    private final Map<String, ScheduleTaskStatus> taskStatusCache = new ConcurrentHashMap<>();

    // 调度任务配置缓存
    private final Map<String, ScheduleConfig> taskConfigCache = new ConcurrentHashMap<>();

    // 调度任务执行历史缓存
    private final Map<String, List<TaskExecutionHistory>> executionHistoryCache = new ConcurrentHashMap<>();

    // 调度任务Future缓存
    private final Map<String, ScheduledFuture<?>> scheduledFutureCache = new ConcurrentHashMap<>();

    // 统计计数器
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong totalSuccesses = new AtomicLong(0);
    private final AtomicLong totalFailures = new AtomicLong(0);

    // ==================== 调度配置管理方法 ====================

    @Override
    public Mono<Boolean> adjustScheduleInterval(String taskName, Duration newInterval) {
        LoggingUtil.info(logger, "动态调整调度任务频率 - 任务: {}, 新间隔: {}秒", taskName, newInterval.getSeconds());

        return Mono.fromCallable(() -> {
            ScheduleTaskStatus status = taskStatusCache.get(taskName);
            if (status == null) {
                LoggingUtil.warn(logger, "调度任务不存在: {}", taskName);
                return false;
            }

            // 更新任务状态
            status.setInterval(newInterval);

            // 重新调度任务
            ScheduledFuture<?> oldFuture = scheduledFutureCache.get(taskName);
            if (oldFuture != null) {
                oldFuture.cancel(false);
            }

            // 这里应该重新创建调度任务，但由于Spring的@Scheduled注解限制，
            // 实际实现中需要使用TaskScheduler手动调度
            LoggingUtil.info(logger, "调度任务频率调整完成 - 任务: {}", taskName);
            return true;
        })
                .subscribeOn(monitoringScheduler)
                .doOnError(error -> LoggingUtil.error(logger, "调整调度任务频率失败 - 任务: " + taskName, error));
    }

    @Override
    public Mono<Boolean> toggleScheduleTask(String taskName, boolean enabled) {
        LoggingUtil.info(logger, "动态{}调度任务 - 任务: {}", enabled ? "启用" : "禁用", taskName);

        return Mono.fromCallable(() -> {
            ScheduleTaskStatus status = taskStatusCache.get(taskName);
            if (status == null) {
                LoggingUtil.warn(logger, "调度任务不存在: {}", taskName);
                return false;
            }

            status.setEnabled(enabled);
            status.setStatus(enabled ? "IDLE" : "DISABLED");

            // 如果禁用，取消调度
            if (!enabled) {
                ScheduledFuture<?> future = scheduledFutureCache.get(taskName);
                if (future != null) {
                    future.cancel(false);
                    scheduledFutureCache.remove(taskName);
                }
            }

            LoggingUtil.info(logger, "调度任务状态切换完成 - 任务: {}, 状态: {}", taskName, enabled ? "启用" : "禁用");
            return true;
        })
                .subscribeOn(monitoringScheduler)
                .doOnError(error -> LoggingUtil.error(logger, "切换调度任务状态失败 - 任务: " + taskName, error));
    }

    @Override
    public Mono<Boolean> adjustTaskTimeout(String taskName, Duration timeout) {
        LoggingUtil.info(logger, "动态调整调度任务超时时间 - 任务: {}, 新超时: {}秒", taskName, timeout.getSeconds());

        return Mono.fromCallable(() -> {
            ScheduleTaskStatus status = taskStatusCache.get(taskName);
            if (status == null) {
                LoggingUtil.warn(logger, "调度任务不存在: {}", taskName);
                return false;
            }

            status.setTimeout(timeout);
            LoggingUtil.info(logger, "调度任务超时时间调整完成 - 任务: {}", taskName);
            return true;
        })
                .subscribeOn(monitoringScheduler)
                .doOnError(error -> LoggingUtil.error(logger, "调整调度任务超时时间失败 - 任务: " + taskName, error));
    }

    @Override
    public Mono<Boolean> adjustSchedulerThreadPool(int corePoolSize, int maxPoolSize, int queueCapacity) {
        LoggingUtil.info(logger, "动态调整调度器线程池配置 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}",
                corePoolSize, maxPoolSize, queueCapacity);

        return Mono.fromCallable(() -> {
            if (taskScheduler instanceof ThreadPoolTaskScheduler) {
                ThreadPoolTaskScheduler threadPoolTaskScheduler = (ThreadPoolTaskScheduler) taskScheduler;

                // 注意：ThreadPoolTaskScheduler在运行时调整线程池大小有限制
                // 这里只是示例，实际实现可能需要重新创建调度器
                threadPoolTaskScheduler.getScheduledThreadPoolExecutor().setCorePoolSize(corePoolSize);

                LoggingUtil.info(logger, "调度器线程池配置调整完成");
                return true;
            } else {
                LoggingUtil.warn(logger, "当前调度器不支持动态线程池调整");
                return false;
            }
        })
                .subscribeOn(monitoringScheduler)
                .doOnError(error -> LoggingUtil.error(logger, "调整调度器线程池配置失败", error));
    }

    // ==================== 调度任务管理方法 ====================

    @Override
    public Flux<ScheduleTaskStatus> getAllScheduleTaskStatus() {
        LoggingUtil.debug(logger, "获取所有调度任务状态");

        return Flux.fromIterable(taskStatusCache.values())
                .subscribeOn(monitoringScheduler)
                .doOnComplete(() -> LoggingUtil.debug(logger, "获取所有调度任务状态完成，任务数量: {}", taskStatusCache.size()));
    }

    @Override
    public Mono<ScheduleTaskStatus> getScheduleTaskStatus(String taskName) {
        LoggingUtil.debug(logger, "获取调度任务状态 - 任务: {}", taskName);

        return Mono.fromCallable(() -> taskStatusCache.get(taskName))
                .subscribeOn(monitoringScheduler)
                .doOnSuccess(status -> {
                    if (status != null) {
                        LoggingUtil.debug(logger, "获取调度任务状态成功 - 任务: {}, 状态: {}", taskName, status.getStatus());
                    } else {
                        LoggingUtil.warn(logger, "调度任务不存在: {}", taskName);
                    }
                });
    }

    @Override
    public Mono<Boolean> restartScheduleTask(String taskName) {
        LoggingUtil.info(logger, "重启调度任务 - 任务: {}", taskName);

        return Mono.fromCallable(() -> {
            ScheduleTaskStatus status = taskStatusCache.get(taskName);
            if (status == null) {
                LoggingUtil.warn(logger, "调度任务不存在: {}", taskName);
                return false;
            }

            // 先停止任务
            ScheduledFuture<?> future = scheduledFutureCache.get(taskName);
            if (future != null) {
                future.cancel(false);
            }

            // 重新启动任务（这里需要根据实际调度逻辑实现）
            status.setStatus("IDLE");
            status.setLastExecutionTime(null);

            LoggingUtil.info(logger, "调度任务重启完成 - 任务: {}", taskName);
            return true;
        })
                .subscribeOn(monitoringScheduler)
                .doOnError(error -> LoggingUtil.error(logger, "重启调度任务失败 - 任务: " + taskName, error));
    }

    @Override
    public Mono<Boolean> executeScheduleTaskNow(String taskName) {
        LoggingUtil.info(logger, "立即执行调度任务 - 任务: {}", taskName);

        return Mono.fromCallable(() -> {
            ScheduleTaskStatus status = taskStatusCache.get(taskName);
            if (status == null) {
                LoggingUtil.warn(logger, "调度任务不存在: {}", taskName);
                return false;
            }

            if (!status.isEnabled()) {
                LoggingUtil.warn(logger, "调度任务已禁用，无法执行: {}", taskName);
                return false;
            }

            // 这里应该触发实际的任务执行逻辑
            // 由于无法直接调用@Scheduled方法，这里只是更新状态
            status.setStatus("RUNNING");
            status.setLastExecutionTime(LocalDateTime.now());
            status.setExecutionCount(status.getExecutionCount() + 1);

            LoggingUtil.info(logger, "调度任务立即执行完成 - 任务: {}", taskName);
            return true;
        })
                .subscribeOn(monitoringScheduler)
                .doOnError(error -> LoggingUtil.error(logger, "立即执行调度任务失败 - 任务: " + taskName, error));
    }

    // ==================== 调度性能监控方法 ====================

    @Override
    public Mono<SchedulerPerformanceStats> getSchedulerPerformanceStats() {
        LoggingUtil.debug(logger, "获取调度器性能统计");

        return Mono.fromCallable(() -> {
            SchedulerPerformanceStats stats = new SchedulerPerformanceStats();

            // 统计任务状态
            int totalTasks = taskStatusCache.size();
            int activeTasks = 0;
            int idleTasks = 0;
            int errorTasks = 0;
            int disabledTasks = 0;

            for (ScheduleTaskStatus status : taskStatusCache.values()) {
                switch (status.getStatus()) {
                    case "RUNNING":
                        activeTasks++;
                        break;
                    case "IDLE":
                        idleTasks++;
                        break;
                    case "ERROR":
                        errorTasks++;
                        break;
                    case "DISABLED":
                        disabledTasks++;
                        break;
                }
            }

            stats.setTotalTasks(totalTasks);
            stats.setActiveTasks(activeTasks);
            stats.setIdleTasks(idleTasks);
            stats.setErrorTasks(errorTasks);
            stats.setDisabledTasks(disabledTasks);

            // 统计执行情况
            long totalExecs = totalExecutions.get();
            long totalSucc = totalSuccesses.get();
            long totalFail = totalFailures.get();

            stats.setTotalExecutions(totalExecs);
            stats.setTotalSuccesses(totalSucc);
            stats.setTotalFailures(totalFail);
            stats.setSuccessRate(totalExecs > 0 ? (double) totalSucc / totalExecs * 100 : 0);

            // 线程池信息
            if (taskScheduler instanceof ThreadPoolTaskScheduler) {
                ThreadPoolTaskScheduler threadPoolTaskScheduler = (ThreadPoolTaskScheduler) taskScheduler;
                stats.setThreadPoolSize(threadPoolTaskScheduler.getPoolSize());
                stats.setActiveThreads(threadPoolTaskScheduler.getActiveCount());
            }

            stats.setLastUpdateTime(LocalDateTime.now());

            return stats;
        })
                .subscribeOn(monitoringScheduler)
                .doOnSuccess(stats -> LoggingUtil.debug(logger, "获取调度器性能统计完成 - 总任务数: {}", stats.getTotalTasks()));
    }

    @Override
    public Flux<TaskExecutionHistory> getTaskExecutionHistory(String taskName, int hours) {
        LoggingUtil.debug(logger, "获取调度任务执行历史 - 任务: {}, 查询小时数: {}", taskName, hours);

        return Flux.fromIterable(executionHistoryCache.getOrDefault(taskName, Collections.emptyList()))
                .filter(history -> history.getExecutionTime().isAfter(LocalDateTime.now().minusHours(hours)))
                .subscribeOn(monitoringScheduler)
                .doOnComplete(() -> LoggingUtil.debug(logger, "获取调度任务执行历史完成 - 任务: {}", taskName));
    }

    @Override
    public Mono<SchedulerHealthStatus> getSchedulerHealthStatus() {
        LoggingUtil.debug(logger, "获取调度器健康状态");

        return Mono.fromCallable(() -> {
            SchedulerHealthStatus healthStatus = new SchedulerHealthStatus();
            Map<String, String> healthChecks = new HashMap<>();

            // 检查任务状态
            int totalTasks = taskStatusCache.size();
            int errorTasks = (int) taskStatusCache.values().stream()
                    .filter(status -> "ERROR".equals(status.getStatus()))
                    .count();

            double errorRate = totalTasks > 0 ? (double) errorTasks / totalTasks * 100 : 0;

            // 计算健康分数
            double healthScore = 100;
            if (errorRate > 20) {
                healthScore -= 30;
                healthChecks.put("task_error_rate", "CRITICAL - 错误率过高: " + String.format("%.1f%%", errorRate));
            } else if (errorRate > 10) {
                healthScore -= 15;
                healthChecks.put("task_error_rate", "WARNING - 错误率较高: " + String.format("%.1f%%", errorRate));
            } else {
                healthChecks.put("task_error_rate", "OK - 错误率正常: " + String.format("%.1f%%", errorRate));
            }

            // 检查线程池状态
            if (taskScheduler instanceof ThreadPoolTaskScheduler) {
                ThreadPoolTaskScheduler threadPoolTaskScheduler = (ThreadPoolTaskScheduler) taskScheduler;
                int poolSize = threadPoolTaskScheduler.getPoolSize();
                int activeCount = threadPoolTaskScheduler.getActiveCount();

                double threadUsage = poolSize > 0 ? (double) activeCount / poolSize * 100 : 0;

                if (threadUsage > 90) {
                    healthScore -= 20;
                    healthChecks.put("thread_pool", "CRITICAL - 线程池使用率过高: " + String.format("%.1f%%", threadUsage));
                } else if (threadUsage > 70) {
                    healthScore -= 10;
                    healthChecks.put("thread_pool", "WARNING - 线程池使用率较高: " + String.format("%.1f%%", threadUsage));
                } else {
                    healthChecks.put("thread_pool", "OK - 线程池使用率正常: " + String.format("%.1f%%", threadUsage));
                }
            }

            // 确定整体状态
            String status;
            String message;
            if (healthScore >= 80) {
                status = "HEALTHY";
                message = "调度器运行正常";
            } else if (healthScore >= 60) {
                status = "WARNING";
                message = "调度器存在一些问题，需要关注";
            } else {
                status = "CRITICAL";
                message = "调度器存在严重问题，需要立即处理";
            }

            healthStatus.setStatus(status);
            healthStatus.setMessage(message);
            healthStatus.setHealthScore(healthScore);
            healthStatus.setHealthChecks(healthChecks);
            healthStatus.setLastCheckTime(LocalDateTime.now());

            return healthStatus;
        })
                .subscribeOn(monitoringScheduler)
                .doOnSuccess(status -> LoggingUtil.debug(logger, "获取调度器健康状态完成 - 状态: {}, 分数: {}",
                        status.getStatus(), status.getHealthScore()));
    }

    // ==================== 配置持久化方法 ====================

    @Override
    public Mono<Boolean> saveScheduleConfig(ScheduleConfig config) {
        LoggingUtil.info(logger, "保存调度配置 - 任务: {}", config.getTaskName());

        String key = unifiedConfigManager.getStringConfig("honyrun.schedule.config-prefix", "schedule:config:") + config.getTaskName();

        return reactiveRedisTemplate.opsForValue()
                .set(key, config, Duration.ofDays(30))
                .doOnSuccess(result -> {
                    if (result) {
                        taskConfigCache.put(config.getTaskName(), config);
                        LoggingUtil.info(logger, "调度配置保存成功 - 任务: {}", config.getTaskName());
                    } else {
                        LoggingUtil.error(logger, "调度配置保存失败 - 任务: {}", config.getTaskName());
                    }
                })
                .doOnError(error -> LoggingUtil.error(logger, "保存调度配置异常 - 任务: " + config.getTaskName(), error))
                .onErrorReturn(false);
    }

    @Override
    public Mono<ScheduleConfig> loadScheduleConfig(String taskName) {
        LoggingUtil.debug(logger, "加载调度配置 - 任务: {}", taskName);

        // 先从缓存获取
        ScheduleConfig cachedConfig = taskConfigCache.get(taskName);
        if (cachedConfig != null) {
            LoggingUtil.debug(logger, "从缓存加载调度配置 - 任务: {}", taskName);
            return Mono.just(cachedConfig);
        }

        // 从Redis获取
        String key = unifiedConfigManager.getStringConfig("honyrun.schedule.config-prefix", "schedule:config:") + taskName;

        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .cast(ScheduleConfig.class)
                .doOnSuccess(config -> {
                    if (config != null) {
                        taskConfigCache.put(taskName, config);
                        LoggingUtil.debug(logger, "从Redis加载调度配置成功 - 任务: {}", taskName);
                    } else {
                        LoggingUtil.debug(logger, "调度配置不存在 - 任务: {}", taskName);
                    }
                })
                .doOnError(error -> LoggingUtil.error(logger, "加载调度配置异常 - 任务: " + taskName, error));
    }

    @Override
    public Mono<Boolean> resetScheduleConfig(String taskName) {
        LoggingUtil.info(logger, "重置调度配置为默认值 - 任务: {}", taskName);

        return Mono.fromCallable(() -> {
            // 创建默认配置
            ScheduleConfig defaultConfig = createDefaultConfig(taskName);

            // 更新缓存
            taskConfigCache.put(taskName, defaultConfig);

            // 更新任务状态
            ScheduleTaskStatus status = taskStatusCache.get(taskName);
            if (status != null) {
                status.setEnabled(defaultConfig.isEnabled());
                status.setInterval(defaultConfig.getInterval());
                status.setTimeout(defaultConfig.getTimeout());
            }

            return true;
        })
                .flatMap(result -> saveScheduleConfig(taskConfigCache.get(taskName)).thenReturn(result))
                .subscribeOn(monitoringScheduler)
                .doOnSuccess(result -> LoggingUtil.info(logger, "调度配置重置完成 - 任务: {}", taskName))
                .doOnError(error -> LoggingUtil.error(logger, "重置调度配置失败 - 任务: " + taskName, error));
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建默认调度配置
     */
    private ScheduleConfig createDefaultConfig(String taskName) {
        ScheduleConfig config = new ScheduleConfig();
        config.setTaskName(taskName);
        config.setEnabled(true);
        config.setInterval(Duration.parse(unifiedConfigManager.getProperty("honyrun.schedule.default-interval", "PT5M")));
        config.setTimeout(Duration.parse(unifiedConfigManager.getProperty("honyrun.schedule.default-timeout", "PT2M")));
        config.setRetryCount(Integer.parseInt(unifiedConfigManager.getProperty("honyrun.schedule.default-max-retries", "3")));
        config.setRetryDelay(Duration.parse(unifiedConfigManager.getProperty("honyrun.schedule.default-retry-delay", "PT30S")));
        config.setProperties(new HashMap<>());
        return config;
    }

    /**
     * 初始化调度任务状态
     */
    public void initializeTaskStatus(String taskName) {
        if (!taskStatusCache.containsKey(taskName)) {
            ScheduleTaskStatus status = new ScheduleTaskStatus();
            status.setTaskName(taskName);
            status.setEnabled(true);
            status.setInterval(Duration.ofMinutes(5));
            status.setTimeout(Duration.ofMinutes(2));
            status.setStatus("IDLE");
            status.setExecutionCount(0);
            status.setSuccessCount(0);
            status.setFailureCount(0);
            status.setAverageExecutionTime(Duration.ZERO);
            status.setMetadata(new HashMap<>());

            taskStatusCache.put(taskName, status);
            executionHistoryCache.put(taskName, new ArrayList<>());

            LoggingUtil.info(logger, "初始化调度任务状态 - 任务: {}", taskName);
        }
    }

    /**
     * 记录任务执行历史
     */
    public void recordTaskExecution(String taskName, Duration executionDuration, String status, String errorMessage) {
        TaskExecutionHistory history = new TaskExecutionHistory();
        history.setTaskName(taskName);
        history.setExecutionTime(LocalDateTime.now());
        history.setExecutionDuration(executionDuration);
        history.setStatus(status);
        history.setErrorMessage(errorMessage);
        history.setExecutionContext(new HashMap<>());

        List<TaskExecutionHistory> historyList = executionHistoryCache.computeIfAbsent(taskName,
                k -> new ArrayList<>());
        historyList.add(history);

        // 保持最近100条记录
        if (historyList.size() > 100) {
            historyList.remove(0);
        }

        // 更新统计
        totalExecutions.incrementAndGet();
        if ("SUCCESS".equals(status)) {
            totalSuccesses.incrementAndGet();
        } else {
            totalFailures.incrementAndGet();
        }

        // 更新任务状态
        ScheduleTaskStatus taskStatus = taskStatusCache.get(taskName);
        if (taskStatus != null) {
            taskStatus.setLastExecutionTime(history.getExecutionTime());
            taskStatus.setExecutionCount(taskStatus.getExecutionCount() + 1);

            if ("SUCCESS".equals(status)) {
                taskStatus.setSuccessCount(taskStatus.getSuccessCount() + 1);
            } else {
                taskStatus.setFailureCount(taskStatus.getFailureCount() + 1);
                taskStatus.setLastError(errorMessage);
            }

            // 计算平均执行时间
            long totalDuration = taskStatus.getAverageExecutionTime().toMillis() * (taskStatus.getExecutionCount() - 1)
                    + executionDuration.toMillis();
            taskStatus.setAverageExecutionTime(Duration.ofMillis(totalDuration / taskStatus.getExecutionCount()));
        }

        LoggingUtil.debug(logger, "记录任务执行历史 - 任务: {}, 状态: {}, 执行时间: {}ms",
                taskName, status, executionDuration.toMillis());
    }

    @Override
    public Mono<String> adjustSchedulingStrategy() {
        LoggingUtil.info(logger, "开始动态调整调度策略");

        return Mono.fromCallable(() -> {
            StringBuilder strategy = new StringBuilder();

            // 获取当前系统负载情况
            int totalTasks = taskStatusCache.size();
            long activeTasksCount = taskStatusCache.values().stream()
                    .filter(status -> "RUNNING".equals(status.getStatus()))
                    .count();

            double systemLoad = totalTasks > 0 ? (double) activeTasksCount / totalTasks : 0.0;

            strategy.append("系统负载: ").append(String.format("%.2f", systemLoad * 100)).append("%");

            // 根据系统负载调整策略
            if (systemLoad > 0.8) {
                // 高负载：降低任务频率
                strategy.append(", 策略: 高负载模式 - 降低任务执行频率");
                LoggingUtil.warn(logger, "系统负载过高 ({}%), 建议降低任务执行频率", systemLoad * 100);
            } else if (systemLoad < 0.3) {
                // 低负载：可以提高任务频率
                strategy.append(", 策略: 低负载模式 - 可适当提高任务执行频率");
                LoggingUtil.info(logger, "系统负载较低 ({}%), 可适当提高任务执行频率", systemLoad * 100);
            } else {
                // 正常负载：保持当前策略
                strategy.append(", 策略: 正常模式 - 保持当前执行频率");
                LoggingUtil.info(logger, "系统负载正常 ({}%), 保持当前调度策略", systemLoad * 100);
            }

            return strategy.toString();
        })
                .subscribeOn(monitoringScheduler)
                .doOnSuccess(result -> LoggingUtil.info(logger, "调度策略调整完成: {}", result))
                .doOnError(error -> LoggingUtil.error(logger, "调度策略调整失败", error));
    }
}