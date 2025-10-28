package com.honyrun.config.monitoring;

import java.time.Duration;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.service.monitoring.ReactiveDynamicSchedulingService;
import com.honyrun.service.monitoring.ReactiveMonitoringAlertService;
import com.honyrun.service.monitoring.ReactiveMonitoringAlertServiceImpl;
import com.honyrun.service.monitoring.ReactiveMonitoringVisualizationService;
import com.honyrun.service.monitoring.ReactivePerformanceMonitoringService;
import com.honyrun.service.monitoring.impl.ReactiveDynamicSchedulingServiceImpl;
import com.honyrun.service.monitoring.impl.ReactiveMonitoringVisualizationServiceImpl;
import com.honyrun.service.monitoring.impl.ReactivePerformanceMonitoringServiceImpl;
import com.honyrun.util.LoggingUtil;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * 统一监控配置管理中心
 *
 * 整合所有监控相关的配置，避免配置分散在多个文件中
 * 提供统一的配置管理和验证机制，同时包含Bean配置
 *
 * 主要功能：
 * - 监控任务开关配置
 * - 性能阈值配置
 * - 告警配置
 * - 超时配置
 * - 调度配置
 * - 监控服务Bean配置
 * - Micrometer指标配置
 *
 * @author Mr.Rey
 * @created 2025-07-02 当前时间
 * @modified 2025-07-02 当前时间
 * @version 2.0.0 - 整合MonitoringConfig功能
 *          Copyright © 2025 HonyRun. All rights reserved.
 */
@Configuration
@ConfigurationProperties(prefix = "honyrun.monitoring")
@Validated
@EnableAspectJAutoProxy(exposeProxy = true) // 启用代理暴露，支持ExposeInvocationInterceptor
@EnableAsync
@EnableScheduling
public class UnifiedMonitoringConfig {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedMonitoringConfig.class);

    /**
     * 监控任务开关配置
     */
    @NotNull
    private TaskConfig task = new TaskConfig();

    /**
     * 性能阈值配置
     */
    @NotNull
    private ThresholdConfig threshold = new ThresholdConfig();

    /**
     * 告警配置
     */
    @NotNull
    private AlertConfig alert = new AlertConfig();

    /**
     * 超时配置
     */
    @NotNull
    private TimeoutConfig timeout = new TimeoutConfig();

    /**
     * 调度配置
     */
    @NotNull
    private ScheduleConfig schedule = new ScheduleConfig();

    /**
     * 速率限制配置
     */
    @NotNull
    private RateLimitConfig rateLimit = new RateLimitConfig();

    // Getters and Setters
    public TaskConfig getTask() {
        return task;
    }

    public void setTask(TaskConfig task) {
        this.task = task;
    }

    public ThresholdConfig getThreshold() {
        return threshold;
    }

    public void setThreshold(ThresholdConfig threshold) {
        this.threshold = threshold;
    }

    public AlertConfig getAlert() {
        return alert;
    }

    public void setAlert(AlertConfig alert) {
        this.alert = alert;
    }

    public TimeoutConfig getTimeout() {
        return timeout;
    }

    public void setTimeout(TimeoutConfig timeout) {
        this.timeout = timeout;
    }

    public ScheduleConfig getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleConfig schedule) {
        this.schedule = schedule;
    }

    public RateLimitConfig getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimitConfig rateLimit) {
        this.rateLimit = rateLimit;
    }

    // 速率限制相关方法
    public boolean isRateLimitEnabled() {
        return rateLimit.isEnabled();
    }

    public int getRateLimitBurstCapacity() {
        return rateLimit.getBurstCapacity();
    }

    public int getMaxConcurrentTasks() {
        return rateLimit.getMaxConcurrentTasks();
    }

    public double getRateLimitPermitsPerSecond() {
        return rateLimit.getPermitsPerSecond();
    }

    public Duration getQueueTimeout() {
        return Duration.ofSeconds(rateLimit.getQueueTimeoutSeconds());
    }

    public int getBackpressureBufferSize() {
        return rateLimit.getBackpressureBufferSize();
    }

    /**
     * 监控任务开关配置
     */
    public static class TaskConfig {
        /** 是否启用监控调度器 */
        private boolean enabled = true;

        /** 是否启用性能监控 */
        private boolean performanceEnabled = true;

        /** 是否启用系统监控 */
        private boolean systemEnabled = true;

        /** 是否启用WebFlux监控 */
        private boolean webfluxEnabled = true;

        /** 是否启用R2DBC监控 */
        private boolean r2dbcEnabled = true;

        /** 是否启用Redis监控 */
        private boolean redisEnabled = true;

        /** 是否启用系统资源监控 */
        private boolean systemResourceEnabled = true;

        /** 是否启用响应式流监控 */
        private boolean reactiveStreamEnabled = true;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isPerformanceEnabled() {
            return performanceEnabled;
        }

        public void setPerformanceEnabled(boolean performanceEnabled) {
            this.performanceEnabled = performanceEnabled;
        }

        public boolean isSystemEnabled() {
            return systemEnabled;
        }

        public void setSystemEnabled(boolean systemEnabled) {
            this.systemEnabled = systemEnabled;
        }

        public boolean isWebfluxEnabled() {
            return webfluxEnabled;
        }

        public void setWebfluxEnabled(boolean webfluxEnabled) {
            this.webfluxEnabled = webfluxEnabled;
        }

        public boolean isR2dbcEnabled() {
            return r2dbcEnabled;
        }

        public void setR2dbcEnabled(boolean r2dbcEnabled) {
            this.r2dbcEnabled = r2dbcEnabled;
        }

        public boolean isRedisEnabled() {
            return redisEnabled;
        }

        public void setRedisEnabled(boolean redisEnabled) {
            this.redisEnabled = redisEnabled;
        }

        public boolean isSystemResourceEnabled() {
            return systemResourceEnabled;
        }

        public void setSystemResourceEnabled(boolean systemResourceEnabled) {
            this.systemResourceEnabled = systemResourceEnabled;
        }

        public boolean isReactiveStreamEnabled() {
            return reactiveStreamEnabled;
        }

        public void setReactiveStreamEnabled(boolean reactiveStreamEnabled) {
            this.reactiveStreamEnabled = reactiveStreamEnabled;
        }
    }

    /**
     * 性能阈值配置
     */
    public static class ThresholdConfig {
        /** 内存使用率阈值 (%) */
        @Min(value = 1, message = "内存阈值必须大于1%")
        @Max(value = 99, message = "内存阈值必须小于99%")
        private double memory = 80.0;

        /** CPU使用率阈值 (%) */
        @Min(value = 1, message = "CPU阈值必须大于1%")
        @Max(value = 99, message = "CPU阈值必须小于99%")
        private double cpu = 80.0;

        /** 磁盘使用率阈值 (%) */
        @Min(value = 1, message = "磁盘阈值必须大于1%")
        @Max(value = 99, message = "磁盘阈值必须小于99%")
        private double disk = 90.0;

        /** 响应时间阈值 (ms) */
        @Min(value = 100, message = "响应时间阈值必须大于100ms")
        private long responseTime = 5000;

        /** 错误率阈值 (%) */
        @Min(value = 0, message = "错误率阈值不能为负数")
        @Max(value = 50, message = "错误率阈值不能超过50%")
        private double errorRate = 5.0;

        /** 错误数量阈值 */
        @Min(value = 1, message = "错误数量阈值必须大于1")
        private long errorCount = 100;

        // Getters and Setters
        public double getMemory() {
            return memory;
        }

        public void setMemory(double memory) {
            this.memory = memory;
        }

        public double getCpu() {
            return cpu;
        }

        public void setCpu(double cpu) {
            this.cpu = cpu;
        }

        public double getDisk() {
            return disk;
        }

        public void setDisk(double disk) {
            this.disk = disk;
        }

        public long getResponseTime() {
            return responseTime;
        }

        public void setResponseTime(long responseTime) {
            this.responseTime = responseTime;
        }

        public double getErrorRate() {
            return errorRate;
        }

        public void setErrorRate(double errorRate) {
            this.errorRate = errorRate;
        }

        public long getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(long errorCount) {
            this.errorCount = errorCount;
        }
    }

    /**
     * 告警配置
     */
    public static class AlertConfig {
        /** 是否启用告警 */
        private boolean enabled = true;

        /** 告警抑制时间 (分钟) */
        @Min(value = 1, message = "告警抑制时间必须大于1分钟")
        private int suppressionMinutes = 10;

        /** 最大告警数量 */
        @Min(value = 1, message = "最大告警数量必须大于1")
        private int maxAlerts = 100;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getSuppressionMinutes() {
            return suppressionMinutes;
        }

        public void setSuppressionMinutes(int suppressionMinutes) {
            this.suppressionMinutes = suppressionMinutes;
        }

        public int getMaxAlerts() {
            return maxAlerts;
        }

        public void setMaxAlerts(int maxAlerts) {
            this.maxAlerts = maxAlerts;
        }
    }

    /**
     * 超时配置
     */
    public static class TimeoutConfig {
        /** 综合性能监控超时 (秒) */
        @Min(value = 5, message = "综合性能监控超时必须大于5秒")
        private int comprehensiveMonitor = 60;

        /** 系统性能监控超时 (秒) */
        @Min(value = 5, message = "系统性能监控超时必须大于5秒")
        private int performanceMonitor = 30;

        /** 健康检查超时 (秒) */
        @Min(value = 1, message = "健康检查超时必须大于1秒")
        private int healthCheck = 15;

        /** 资源监控超时 (秒) */
        @Min(value = 1, message = "资源监控超时必须大于1秒")
        private int resourceMonitor = 10;

        /** WebFlux监控超时 (秒) */
        @Min(value = 5, message = "WebFlux监控超时必须大于5秒")
        private int webfluxMonitor = 30;

        /** R2DBC监控超时 (秒) */
        @Min(value = 5, message = "R2DBC监控超时必须大于5秒")
        private int r2dbcMonitor = 30;

        /** Redis监控超时 (秒) */
        @Min(value = 1, message = "Redis监控超时必须大于1秒")
        private int redisMonitor = 20;

        // Getters and Setters
        public int getComprehensiveMonitor() {
            return comprehensiveMonitor;
        }

        public void setComprehensiveMonitor(int comprehensiveMonitor) {
            this.comprehensiveMonitor = comprehensiveMonitor;
        }

        public int getPerformanceMonitor() {
            return performanceMonitor;
        }

        public void setPerformanceMonitor(int performanceMonitor) {
            this.performanceMonitor = performanceMonitor;
        }

        public int getHealthCheck() {
            return healthCheck;
        }

        public void setHealthCheck(int healthCheck) {
            this.healthCheck = healthCheck;
        }

        public int getResourceMonitor() {
            return resourceMonitor;
        }

        public void setResourceMonitor(int resourceMonitor) {
            this.resourceMonitor = resourceMonitor;
        }

        public int getWebfluxMonitor() {
            return webfluxMonitor;
        }

        public void setWebfluxMonitor(int webfluxMonitor) {
            this.webfluxMonitor = webfluxMonitor;
        }

        public int getR2dbcMonitor() {
            return r2dbcMonitor;
        }

        public void setR2dbcMonitor(int r2dbcMonitor) {
            this.r2dbcMonitor = r2dbcMonitor;
        }

        public int getRedisMonitor() {
            return redisMonitor;
        }

        public void setRedisMonitor(int redisMonitor) {
            this.redisMonitor = redisMonitor;
        }
    }

    /**
     * 调度配置
     */
    public static class ScheduleConfig {
        /** 综合性能监控间隔 (毫秒) */
        @Min(value = 60000, message = "综合性能监控间隔必须大于1分钟")
        private long comprehensiveMonitorInterval = 300000; // 5分钟

        /** 系统性能监控间隔 (毫秒) */
        @Min(value = 60000, message = "系统性能监控间隔必须大于1分钟")
        private long performanceMonitorInterval = 300000; // 5分钟

        /** 健康检查间隔 (毫秒) */
        @Min(value = 30000, message = "健康检查间隔必须大于30秒")
        private long healthCheckInterval = 120000; // 2分钟

        /** 资源监控间隔 (毫秒) */
        @Min(value = 30000, message = "资源监控间隔必须大于30秒")
        private long resourceMonitorInterval = 180000; // 3分钟

        /** 日志清理保留天数 */
        @Min(value = 1, message = "日志清理保留天数必须大于1天")
        private int logCleanupDays = 30;

        // Getters and Setters
        public long getComprehensiveMonitorInterval() {
            return comprehensiveMonitorInterval;
        }

        public void setComprehensiveMonitorInterval(long comprehensiveMonitorInterval) {
            this.comprehensiveMonitorInterval = comprehensiveMonitorInterval;
        }

        public long getPerformanceMonitorInterval() {
            return performanceMonitorInterval;
        }

        public void setPerformanceMonitorInterval(long performanceMonitorInterval) {
            this.performanceMonitorInterval = performanceMonitorInterval;
        }

        public long getHealthCheckInterval() {
            return healthCheckInterval;
        }

        public void setHealthCheckInterval(long healthCheckInterval) {
            this.healthCheckInterval = healthCheckInterval;
        }

        public long getResourceMonitorInterval() {
            return resourceMonitorInterval;
        }

        public void setResourceMonitorInterval(long resourceMonitorInterval) {
            this.resourceMonitorInterval = resourceMonitorInterval;
        }

        public int getLogCleanupDays() {
            return logCleanupDays;
        }

        public void setLogCleanupDays(int logCleanupDays) {
            this.logCleanupDays = logCleanupDays;
        }
    }

    // ========================================
    // Bean Configuration Methods (migrated from MonitoringConfig)
    // ========================================

    /**
     * 响应式性能监控服务Bean
     *
     * @return ReactivePerformanceMonitoringService实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-13 15:30:00
     * @version 1.0.0
     */
    @Bean("devReactivePerformanceMonitoringService")
    @Scope("singleton") // 性能监控服务必须是单例，确保监控数据一致性
    @Profile("dev")
    public ReactivePerformanceMonitoringService reactivePerformanceMonitoringService() {
        LoggingUtil.info(logger, "创建响应式性能监控服务Bean");
        return new ReactivePerformanceMonitoringServiceImpl();
    }

    /**
     * Micrometer指标注册表自定义器Bean
     *
     * @return MeterRegistryCustomizer实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-13 15:30:00
     * @version 1.0.0
     */
    @Bean("devMetricsCommonTags")
    @Profile("dev")
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        LoggingUtil.info(logger, "创建指标通用标签自定义器Bean");
        return registry -> {
            registry.config()
                    .commonTags("application", "honyrun")
                    .commonTags("environment", "dev")
                    .commonTags("version", "1.0.0")
                    .meterFilter(MeterFilter.deny(id -> {
                        String name = id.getName();
                        return name.startsWith("jvm.gc.pause") ||
                                name.startsWith("jvm.gc.concurrent.phase.time") ||
                                name.startsWith("jvm.gc.memory.promoted") ||
                                name.startsWith("jvm.gc.memory.allocated") ||
                                name.startsWith("jvm.gc.max.data.size") ||
                                name.startsWith("jvm.gc.live.data.size") ||
                                name.startsWith("jvm.buffer") ||
                                name.startsWith("jvm.threads") ||
                                name.startsWith("process") ||
                                name.startsWith("system");
                    }))
                    .meterFilter(MeterFilter.denyNameStartsWith("tomcat"))
                    .meterFilter(MeterFilter.denyNameStartsWith("hikaricp"));
        };
    }

    /**
     * 指标端点Bean
     *
     * @param meterRegistry 指标注册表
     * @return MetricsEndpoint实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-13 15:30:00
     * @version 1.0.0
     */
    @Bean("devMetricsEndpoint")
    @Scope("singleton") // 指标端点必须是单例，确保指标收集的一致性
    @Profile("dev")
    public MetricsEndpoint metricsEndpoint(MeterRegistry meterRegistry) {
        LoggingUtil.info(logger, "创建指标端点Bean");
        return new MetricsEndpoint(meterRegistry);
    }

    /**
     * 响应式监控告警服务Bean
     *
     * @param redisTemplate Redis模板
     * @return ReactiveMonitoringAlertService实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-13 15:30:00
     * @version 1.0.0
     */
    @Bean("devReactiveMonitoringAlertService")
    @Scope("singleton") // 告警服务必须是单例，避免重复告警和状态不一致
    @Profile("dev")
    public ReactiveMonitoringAlertService reactiveMonitoringAlertService(
            @Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> redisTemplate) {
        LoggingUtil.info(logger, "创建响应式监控告警服务Bean");
        return new ReactiveMonitoringAlertServiceImpl(redisTemplate);
    }

    /**
     * 响应式动态调度服务Bean
     *
     * @return ReactiveDynamicSchedulingService实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-13 15:30:00
     * @version 1.0.0
     */
    @Bean("devReactiveDynamicSchedulingService")
    @Scope("singleton") // 调度服务必须是单例，确保任务调度的一致性
    @Profile("dev")
    public ReactiveDynamicSchedulingService reactiveDynamicSchedulingService(
            @Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
            @Qualifier("unifiedTaskScheduler") TaskScheduler taskScheduler,
            @Qualifier("devMonitoringScheduler") Scheduler monitoringScheduler,
            UnifiedConfigManager unifiedConfigManager) {
        LoggingUtil.info(logger, "创建响应式动态调度服务Bean");
        return new ReactiveDynamicSchedulingServiceImpl(reactiveRedisTemplate, taskScheduler, monitoringScheduler, unifiedConfigManager);
    }

    /**
     * 响应式监控可视化服务Bean
     *
     * @param redisTemplate                Redis模板
     * @param performanceMonitoringService 性能监控服务
     * @param alertService                 告警服务
     * @return ReactiveMonitoringVisualizationService实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-13 15:30:00
     * @version 1.0.0
     */
    @Bean("devReactiveMonitoringVisualizationService")
    @Scope("singleton") // 可视化服务必须是单例，确保数据展示的一致性
    @Profile("dev")
    public ReactiveMonitoringVisualizationService reactiveMonitoringVisualizationService(
            @Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> redisTemplate,
            ReactivePerformanceMonitoringService performanceMonitoringService,
            ReactiveMonitoringAlertService alertService,
            UnifiedConfigManager unifiedConfigManager) {
        LoggingUtil.info(logger, "创建响应式监控可视化服务Bean");
        return new ReactiveMonitoringVisualizationServiceImpl(redisTemplate, performanceMonitoringService,
                alertService, unifiedConfigManager);
    }

    /**
     * 监控调度器Bean
     *
     * @return Scheduler实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-13 15:30:00
     * @version 1.0.0
     */
    @Bean("devMonitoringScheduler")
    @Scope("singleton") // 调度器必须是单例，确保线程池的复用和性能
    @Profile("dev")
    public Scheduler monitoringScheduler() {
        LoggingUtil.info(logger, "创建监控调度器Bean");
        return Schedulers.newBoundedElastic(
                Runtime.getRuntime().availableProcessors() * 2,
                Integer.MAX_VALUE,
                "monitoring-scheduler");
    }

    /**
     * 统一任务调度器Bean
     *
     * @return TaskScheduler实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @version 1.0.0
     */
    @Bean("unifiedTaskScheduler")
    @Scope("singleton") // 任务调度器必须是单例，确保调度任务的一致性
    @Profile("dev")
    public TaskScheduler unifiedTaskScheduler() {
        LoggingUtil.info(logger, "创建统一任务调度器Bean");
        org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler scheduler = new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        scheduler.setThreadNamePrefix("unified-task-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        return scheduler;
    }

    /**
     * 监控任务信号量Bean
     *
     * @return Semaphore实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-13 15:30:00
     * @version 1.0.0
     */
    @Bean("devMonitoringTaskSemaphore")
    @Scope("singleton") // 信号量必须是单例，确保并发控制的全局一致性
    @Profile("dev")
    public Semaphore monitoringTaskSemaphore() {
        LoggingUtil.info(logger, "创建监控任务信号量Bean，最大并发任务数: 50");
        return new Semaphore(50);
    }

    /**
     * 获取是否启用监控
     */
    public boolean isEnabled() {
        return task.isEnabled();
    }

    /**
     * 获取指标聚合窗口
     */
    public Duration getMetricsAggregationWindow() {
        return Duration.ofSeconds(300); // 默认5分钟
    }

    /**
     * 获取指标保留时长
     */
    public Duration getMetricsRetentionDuration() {
        return Duration.ofDays(7); // 默认7天
    }

    /**
     * 限流配置类
     *
     * @author: Mr.Rey Copyright © 2025
     * @created: 2025-01-13 16:57:03
     * @version: 1.0.0
     */
    public static class RateLimitConfig {

        /**
         * 是否启用限流
         */
        private boolean enabled = true;

        /**
         * 每秒允许的请求数
         */
        @Min(value = 1, message = "每秒允许的请求数必须大于1")
        private double permitsPerSecond = 100.0;

        /**
         * 突发容量
         */
        @Min(value = 1, message = "突发容量必须大于1")
        private int burstCapacity = 200;

        /**
         * 最大并发任务数
         */
        @Min(value = 1, message = "最大并发任务数必须大于1")
        private int maxConcurrentTasks = 50;

        /**
         * 队列超时时间（秒）
         */
        @Min(value = 1, message = "队列超时时间必须大于1秒")
        private int queueTimeoutSeconds = 30;

        /**
         * 背压缓冲区大小
         */
        @Min(value = 1, message = "背压缓冲区大小必须大于1")
        private int backpressureBufferSize = 1000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public double getPermitsPerSecond() {
            return permitsPerSecond;
        }

        public void setPermitsPerSecond(double permitsPerSecond) {
            this.permitsPerSecond = permitsPerSecond;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }

        public int getMaxConcurrentTasks() {
            return maxConcurrentTasks;
        }

        public void setMaxConcurrentTasks(int maxConcurrentTasks) {
            this.maxConcurrentTasks = maxConcurrentTasks;
        }

        public int getQueueTimeoutSeconds() {
            return queueTimeoutSeconds;
        }

        public void setQueueTimeoutSeconds(int queueTimeoutSeconds) {
            this.queueTimeoutSeconds = queueTimeoutSeconds;
        }

        public int getBackpressureBufferSize() {
            return backpressureBufferSize;
        }

        public void setBackpressureBufferSize(int backpressureBufferSize) {
            this.backpressureBufferSize = backpressureBufferSize;
        }
    }

}
