package com.honyrun.config.monitoring;

import com.honyrun.util.LoggingUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 自定义监控指标配置类
 *
 * 提供业务相关的自定义监控指标，包括：
 * - 业务操作计数器
 * - 响应时间计时器
 * - 系统资源使用率
 * - 缓存命中率监控
 *
 * 主要功能：
 * - 自定义业务指标定义
 * - 监控数据收集和上报
 * - 性能指标统计分析
 * - 告警阈值监控
 *
 * @author Mr.Rey
 * @version 2.1.0 - 添加Bean作用域配置
 * @created 2025-07-01 00:00:00
 * @modified 2025-07-01 16:45:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Configuration
@EnableScheduling
public class CustomMetricsConfig {

    private static final Logger logger = LoggerFactory.getLogger(CustomMetricsConfig.class);

    private final MeterRegistry meterRegistry;

    /**
     * 构造函数注入
     *
     * @param meterRegistry 指标注册表
     */
    public CustomMetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // 业务指标计数器
    private final AtomicLong activeUsers = new AtomicLong(0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    /**
     * 业务操作计数器
     */
    @Bean("businessOperationCounter")
    public Counter businessOperationCounter() {
        LoggingUtil.info(logger, "配置业务操作计数器");
        
        return Counter.builder("honyrun.business.operations")
                .description("Business operations count")
                .tag("application", "honyrun")
                .register(meterRegistry);
    }

    /**
     * 用户登录计数器
     */
    @Bean("userLoginCounter")
    public Counter userLoginCounter() {
        LoggingUtil.info(logger, "配置用户登录计数器");
        
        return Counter.builder("honyrun.user.logins")
                .description("User login count")
                .tag("application", "honyrun")
                .register(meterRegistry);
    }

    /**
     * API响应时间计时器
     */
    @Bean("apiResponseTimer")
    public Timer apiResponseTimer() {
        LoggingUtil.info(logger, "配置API响应时间计时器");
        
        return Timer.builder("honyrun.api.response.time")
                .description("API response time")
                .tag("application", "honyrun")
                .register(meterRegistry);
    }

    /**
     * 数据库查询时间计时器
     */
    @Bean("databaseQueryTimer")
    public Timer databaseQueryTimer() {
        LoggingUtil.info(logger, "配置数据库查询时间计时器");
        
        return Timer.builder("honyrun.database.query.time")
                .description("Database query execution time")
                .tag("application", "honyrun")
                .register(meterRegistry);
    }

    /**
     * 缓存操作计时器
     * 使用@Scope("singleton")确保计时器全局唯一，避免重复统计
     */
    @Bean("cacheOperationTimer")
    @Scope("singleton")  // 计时器必须是单例，确保统计数据的一致性
    public Timer cacheOperationTimer() {
        LoggingUtil.info(logger, "配置缓存操作计时器");
        
        return Timer.builder("honyrun.cache.operation.time")
                .description("Cache operation time")
                .tag("application", "honyrun")
                .register(meterRegistry);
    }

    /**
     * 活跃用户数量指标
     */
    @Bean("activeUsersGauge")
    public Gauge activeUsersGauge() {
        LoggingUtil.info(logger, "配置活跃用户数量指标");
        
        return Gauge.builder("honyrun.users.active", activeUsers, AtomicLong::get)
                .description("Number of active users")
                .tag("application", "honyrun")
                .register(meterRegistry);
    }

    /**
     * 缓存命中率指标
     * 使用@Scope("singleton")确保指标全局唯一，避免重复监控
     */
    @Bean("cacheHitRateGauge")
    @Scope("singleton")  // 指标必须是单例，确保监控数据的准确性
    public Gauge cacheHitRateGauge() {
        LoggingUtil.info(logger, "配置缓存命中率指标");
        
        return Gauge.builder("honyrun.cache.hit.rate", this, CustomMetricsConfig::calculateCacheHitRate)
                .description("Cache hit rate percentage")
                .tag("application", "honyrun")
                .register(meterRegistry);
    }

    /**
     * JVM内存使用率指标
     * 使用@Scope("singleton")确保内存监控的全局一致性
     */
    @Bean("jvmMemoryUsageGauge")
    @Scope("singleton")  // 内存监控指标必须是单例，确保监控的准确性
    public Gauge jvmMemoryUsageGauge() {
        LoggingUtil.info(logger, "配置JVM内存使用率指标");
        
        return Gauge.builder("honyrun.jvm.memory.usage", this, CustomMetricsConfig::calculateMemoryUsage)
                .description("JVM memory usage percentage")
                .tag("application", "honyrun")
                .register(meterRegistry);
    }

    /**
     * 系统负载指标
     */
    @Bean("systemLoadGauge")
    public Gauge systemLoadGauge() {
        LoggingUtil.info(logger, "配置系统负载指标");
        
        return Gauge.builder("honyrun.system.load", this, CustomMetricsConfig::getSystemLoad)
                .description("System load average")
                .tag("application", "honyrun")
                .register(meterRegistry);
    }

    /**
     * 自定义监控指标收集器
     * 使用@Scope("singleton")确保收集器全局唯一，避免重复收集
     */
    @Bean("customMetricsCollector")
    @Scope("singleton")  // 指标收集器必须是单例，确保收集逻辑的一致性
    public CustomMetricsCollector customMetricsCollector() {
        LoggingUtil.info(logger, "配置自定义监控指标收集器");
        return new CustomMetricsCollector();
    }

    /**
     * 定时更新监控指标
     */
    @Scheduled(fixedRate = 30000) // 每30秒更新一次
    public void updateMetrics() {
        try {
            LoggingUtil.debug(logger, "更新自定义监控指标");
            
            // 更新活跃用户数（这里可以从实际业务中获取）
            updateActiveUsers();
            
            // 更新缓存统计
            updateCacheStats();
            
            LoggingUtil.debug(logger, "自定义监控指标更新完成");
            
        } catch (Exception e) {
            LoggingUtil.error(logger, "更新自定义监控指标失败", e);
        }
    }

    /**
     * 更新活跃用户数
     */
    private void updateActiveUsers() {
        // 从实际的用户会话管理中获取活跃用户数
        long currentActiveUsers = getCurrentActiveUsers();
        activeUsers.set(currentActiveUsers);
        
        LoggingUtil.debug(logger, "更新活跃用户数：{}", currentActiveUsers);
    }

    /**
     * 更新缓存统计
     */
    private void updateCacheStats() {
        // 从实际的缓存管理器中获取统计数据
        // 这里的统计数据通过recordCacheHit()和recordCacheMiss()方法实时更新
        LoggingUtil.debug(logger, "更新缓存统计，命中：{}，未命中：{}", cacheHits.get(), cacheMisses.get());
    }

    /**
     * 获取当前活跃用户数
     *
     * @return 活跃用户数
     */
    private long getCurrentActiveUsers() {
        // 实际的活跃用户统计逻辑
        // 从Redis中获取在线用户数，或从数据库查询最近活跃的用户
        try {
            // 这里应该实现实际的活跃用户统计逻辑
            // 例如：从Redis中获取在线用户会话数量
            // 或者查询数据库中最近一段时间内活跃的用户数量
            
            // 暂时返回0，等待实际的用户会话管理实现
            return 0L;
        } catch (Exception e) {
            LoggingUtil.warn(logger, "获取活跃用户数失败: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * 计算缓存命中率
     *
     * @return 缓存命中率百分比
     */
    private double calculateCacheHitRate() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        
        if (total == 0) {
            return 0.0;
        }
        
        return (double) hits / total * 100.0;
    }

    /**
     * 计算内存使用率
     *
     * @return 内存使用率百分比
     */
    private double calculateMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return (double) usedMemory / totalMemory * 100.0;
    }

    /**
     * 获取系统负载
     *
     * @return 系统负载
     */
    private double getSystemLoad() {
        return java.lang.management.ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
    }

    /**
     * 记录缓存命中
     */
    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    /**
     * 记录请求
     */
    public void recordRequest() {
        totalRequests.incrementAndGet();
    }

    /**
     * 自定义监控指标收集器内部类
     * 
     * 提供监控指标的收集、统计和报告功能
     * 支持业务指标和性能指标的综合监控
     */
    public class CustomMetricsCollector {

        /**
         * 收集业务指标
         *
         * @return 业务指标字符串
         */
        public String collectBusinessMetrics() {
            try {
                LoggingUtil.debug(logger, "收集业务指标");
                
                StringBuilder metrics = new StringBuilder();
                metrics.append("活跃用户数: ").append(activeUsers.get()).append("\n");
                metrics.append("总请求数: ").append(totalRequests.get()).append("\n");
                metrics.append("缓存命中数: ").append(cacheHits.get()).append("\n");
                metrics.append("缓存未命中数: ").append(cacheMisses.get()).append("\n");
                
                return metrics.toString();
                
            } catch (Exception e) {
                LoggingUtil.error(logger, "收集业务指标失败", e);
                return "业务指标收集失败: " + e.getMessage();
            }
        }

        /**
         * 收集性能指标
         *
         * @return 性能指标字符串
         */
        public String collectPerformanceMetrics() {
            try {
                LoggingUtil.debug(logger, "收集性能指标");
                
                StringBuilder metrics = new StringBuilder();
                metrics.append("缓存命中率: ").append(String.format("%.2f%%", calculateCacheHitRate())).append("\n");
                metrics.append("内存使用率: ").append(String.format("%.2f%%", calculateMemoryUsage())).append("\n");
                metrics.append("系统负载: ").append(String.format("%.2f", getSystemLoad())).append("\n");
                
                return metrics.toString();
                
            } catch (Exception e) {
                LoggingUtil.error(logger, "收集性能指标失败", e);
                return "性能指标收集失败: " + e.getMessage();
            }
        }

        /**
         * 生成监控报告
         *
         * @return 监控报告
         */
        public String generateMonitoringReport() {
            try {
                LoggingUtil.info(logger, "生成监控报告");
                
                StringBuilder report = new StringBuilder();
                report.append("=== HonyRun 监控报告 ===\n");
                report.append("报告生成时间: ").append(java.time.LocalDateTime.now()).append("\n\n");
                
                report.append("业务指标:\n");
                report.append(collectBusinessMetrics()).append("\n");
                
                report.append("性能指标:\n");
                report.append(collectPerformanceMetrics()).append("\n");
                
                LoggingUtil.info(logger, "监控报告生成完成");
                
                return report.toString();
                
            } catch (Exception e) {
                LoggingUtil.error(logger, "生成监控报告失败", e);
                return "生成监控报告失败: " + e.getMessage();
            }
        }
    }
}

