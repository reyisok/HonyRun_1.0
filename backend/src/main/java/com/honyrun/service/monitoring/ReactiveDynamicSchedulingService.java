package com.honyrun.service.monitoring;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 响应式动态调度配置服务接口
 * 
 * 提供动态调度配置管理功能，包括：
 * - 调度任务动态配置
 * - 调度频率动态调整
 * - 调度器状态监控
 * - 调度任务管理
 * - 调度性能优化
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-02 当前时间
 * @version 1.0.0 - 初始版本
 */
public interface ReactiveDynamicSchedulingService {

    // ==================== 调度配置管理方法 ====================

    /**
     * 动态调整调度任务频率
     * 
     * @param taskName 任务名称
     * @param newInterval 新的执行间隔
     * @return 调整结果的Mono
     */
    Mono<Boolean> adjustScheduleInterval(String taskName, Duration newInterval);

    /**
     * 动态启用/禁用调度任务
     * 
     * @param taskName 任务名称
     * @param enabled 是否启用
     * @return 调整结果的Mono
     */
    Mono<Boolean> toggleScheduleTask(String taskName, boolean enabled);

    /**
     * 动态调整调度任务超时时间
     * 
     * @param taskName 任务名称
     * @param timeout 新的超时时间
     * @return 调整结果的Mono
     */
    Mono<Boolean> adjustTaskTimeout(String taskName, Duration timeout);

    /**
     * 动态调整调度器线程池配置
     * 
     * @param corePoolSize 核心线程数
     * @param maxPoolSize 最大线程数
     * @param queueCapacity 队列容量
     * @return 调整结果的Mono
     */
    Mono<Boolean> adjustSchedulerThreadPool(int corePoolSize, int maxPoolSize, int queueCapacity);

    // ==================== 调度任务管理方法 ====================

    /**
     * 获取所有调度任务状态
     * 
     * @return 调度任务状态列表的Flux
     */
    Flux<ScheduleTaskStatus> getAllScheduleTaskStatus();

    /**
     * 获取指定调度任务状态
     * 
     * @param taskName 任务名称
     * @return 调度任务状态的Mono
     */
    Mono<ScheduleTaskStatus> getScheduleTaskStatus(String taskName);

    /**
     * 重启调度任务
     * 
     * @param taskName 任务名称
     * @return 重启结果的Mono
     */
    Mono<Boolean> restartScheduleTask(String taskName);

    /**
     * 立即执行调度任务
     * 
     * @param taskName 任务名称
     * @return 执行结果的Mono
     */
    Mono<Boolean> executeScheduleTaskNow(String taskName);

    // ==================== 调度性能监控方法 ====================

    /**
     * 获取调度器性能统计
     * 
     * @return 性能统计的Mono
     */
    Mono<SchedulerPerformanceStats> getSchedulerPerformanceStats();

    /**
     * 获取调度任务执行历史
     * 
     * @param taskName 任务名称
     * @param hours 查询小时数
     * @return 执行历史的Flux
     */
    Flux<TaskExecutionHistory> getTaskExecutionHistory(String taskName, int hours);

    /**
     * 获取调度器健康状态
     * 
     * @return 健康状态的Mono
     */
    Mono<SchedulerHealthStatus> getSchedulerHealthStatus();

    /**
     * 动态调整调度策略
     * 
     * 根据系统运行状态和负载情况动态调整调度策略，包括：
     * - 调整任务执行频率
     * - 优化线程池配置
     * - 调整超时时间
     * - 启用/禁用特定任务
     * 
     * @return 调整后的策略信息的Mono
     */
    Mono<String> adjustSchedulingStrategy();

    // ==================== 配置持久化方法 ====================

    /**
     * 保存调度配置
     * 
     * @param config 调度配置
     * @return 保存结果的Mono
     */
    Mono<Boolean> saveScheduleConfig(ScheduleConfig config);

    /**
     * 加载调度配置
     * 
     * @param taskName 任务名称
     * @return 调度配置的Mono
     */
    Mono<ScheduleConfig> loadScheduleConfig(String taskName);

    /**
     * 重置调度配置为默认值
     * 
     * @param taskName 任务名称
     * @return 重置结果的Mono
     */
    Mono<Boolean> resetScheduleConfig(String taskName);

    // ==================== 数据传输对象 ====================

    /**
     * 调度任务状态
     */
    class ScheduleTaskStatus {
        private String taskName;
        private boolean enabled;
        private Duration interval;
        private Duration timeout;
        private LocalDateTime lastExecutionTime;
        private LocalDateTime nextExecutionTime;
        private String status; // RUNNING, IDLE, ERROR, DISABLED
        private long executionCount;
        private long successCount;
        private long failureCount;
        private String lastError;
        private Duration averageExecutionTime;
        private Map<String, Object> metadata;

        // Getters and Setters
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public Duration getInterval() { return interval; }
        public void setInterval(Duration interval) { this.interval = interval; }

        public Duration getTimeout() { return timeout; }
        public void setTimeout(Duration timeout) { this.timeout = timeout; }

        public LocalDateTime getLastExecutionTime() { return lastExecutionTime; }
        public void setLastExecutionTime(LocalDateTime lastExecutionTime) { this.lastExecutionTime = lastExecutionTime; }

        public LocalDateTime getNextExecutionTime() { return nextExecutionTime; }
        public void setNextExecutionTime(LocalDateTime nextExecutionTime) { this.nextExecutionTime = nextExecutionTime; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public long getExecutionCount() { return executionCount; }
        public void setExecutionCount(long executionCount) { this.executionCount = executionCount; }

        public long getSuccessCount() { return successCount; }
        public void setSuccessCount(long successCount) { this.successCount = successCount; }

        public long getFailureCount() { return failureCount; }
        public void setFailureCount(long failureCount) { this.failureCount = failureCount; }

        public String getLastError() { return lastError; }
        public void setLastError(String lastError) { this.lastError = lastError; }

        public Duration getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(Duration averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    /**
     * 调度器性能统计
     */
    class SchedulerPerformanceStats {
        private int totalTasks;
        private int activeTasks;
        private int idleTasks;
        private int errorTasks;
        private int disabledTasks;
        private long totalExecutions;
        private long totalSuccesses;
        private long totalFailures;
        private double successRate;
        private Duration averageExecutionTime;
        private int threadPoolSize;
        private int activeThreads;
        private int queuedTasks;
        private double cpuUsage;
        private double memoryUsage;
        private LocalDateTime lastUpdateTime;

        // Getters and Setters
        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

        public int getActiveTasks() { return activeTasks; }
        public void setActiveTasks(int activeTasks) { this.activeTasks = activeTasks; }

        public int getIdleTasks() { return idleTasks; }
        public void setIdleTasks(int idleTasks) { this.idleTasks = idleTasks; }

        public int getErrorTasks() { return errorTasks; }
        public void setErrorTasks(int errorTasks) { this.errorTasks = errorTasks; }

        public int getDisabledTasks() { return disabledTasks; }
        public void setDisabledTasks(int disabledTasks) { this.disabledTasks = disabledTasks; }

        public long getTotalExecutions() { return totalExecutions; }
        public void setTotalExecutions(long totalExecutions) { this.totalExecutions = totalExecutions; }

        public long getTotalSuccesses() { return totalSuccesses; }
        public void setTotalSuccesses(long totalSuccesses) { this.totalSuccesses = totalSuccesses; }

        public long getTotalFailures() { return totalFailures; }
        public void setTotalFailures(long totalFailures) { this.totalFailures = totalFailures; }

        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }

        public Duration getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(Duration averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }

        public int getThreadPoolSize() { return threadPoolSize; }
        public void setThreadPoolSize(int threadPoolSize) { this.threadPoolSize = threadPoolSize; }

        public int getActiveThreads() { return activeThreads; }
        public void setActiveThreads(int activeThreads) { this.activeThreads = activeThreads; }

        public int getQueuedTasks() { return queuedTasks; }
        public void setQueuedTasks(int queuedTasks) { this.queuedTasks = queuedTasks; }

        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }

        public double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }

        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    }

    /**
     * 任务执行历史
     */
    class TaskExecutionHistory {
        private String taskName;
        private LocalDateTime executionTime;
        private Duration executionDuration;
        private String status; // SUCCESS, FAILURE, TIMEOUT
        private String errorMessage;
        private Map<String, Object> executionContext;

        // Getters and Setters
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }

        public LocalDateTime getExecutionTime() { return executionTime; }
        public void setExecutionTime(LocalDateTime executionTime) { this.executionTime = executionTime; }

        public Duration getExecutionDuration() { return executionDuration; }
        public void setExecutionDuration(Duration executionDuration) { this.executionDuration = executionDuration; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public Map<String, Object> getExecutionContext() { return executionContext; }
        public void setExecutionContext(Map<String, Object> executionContext) { this.executionContext = executionContext; }
    }

    /**
     * 调度器健康状态
     */
    class SchedulerHealthStatus {
        private String status; // HEALTHY, WARNING, CRITICAL
        private String message;
        private double healthScore; // 0-100
        private Map<String, String> healthChecks;
        private LocalDateTime lastCheckTime;

        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public double getHealthScore() { return healthScore; }
        public void setHealthScore(double healthScore) { this.healthScore = healthScore; }

        public Map<String, String> getHealthChecks() { return healthChecks; }
        public void setHealthChecks(Map<String, String> healthChecks) { this.healthChecks = healthChecks; }

        public LocalDateTime getLastCheckTime() { return lastCheckTime; }
        public void setLastCheckTime(LocalDateTime lastCheckTime) { this.lastCheckTime = lastCheckTime; }
    }

    /**
     * 调度配置
     */
    class ScheduleConfig {
        private String taskName;
        private boolean enabled;
        private Duration interval;
        private Duration timeout;
        private int retryCount;
        private Duration retryDelay;
        private String cronExpression;
        private Map<String, Object> properties;

        // Getters and Setters
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public Duration getInterval() { return interval; }
        public void setInterval(Duration interval) { this.interval = interval; }

        public Duration getTimeout() { return timeout; }
        public void setTimeout(Duration timeout) { this.timeout = timeout; }

        public int getRetryCount() { return retryCount; }
        public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

        public Duration getRetryDelay() { return retryDelay; }
        public void setRetryDelay(Duration retryDelay) { this.retryDelay = retryDelay; }

        public String getCronExpression() { return cronExpression; }
        public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    }
}

