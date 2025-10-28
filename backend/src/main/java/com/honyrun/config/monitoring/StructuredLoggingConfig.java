package com.honyrun.config.monitoring;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.util.LoggingUtil;

/**
 * 结构化日志配置
 *
 * <p>
 * 提供统一的结构化日志记录机制，支持性能监控、错误追踪和业务分析。
 *
 * <p>
 * <strong>功能特性：</strong>
 * <ul>
 * <li>结构化日志格式（JSON）</li>
 * <li>性能指标收集</li>
 * <li>错误统计分析</li>
 * <li>业务事件追踪</li>
 * <li>定时日志汇总</li>
 * </ul>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @version 1.0.0
 */
@Configuration
@EnableScheduling
public class StructuredLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(StructuredLoggingConfig.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final UnifiedConfigManager unifiedConfigManager;

    // 性能指标收集器
    private final Map<String, AtomicLong> performanceCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounters = new ConcurrentHashMap<>();
    private final Map<String, Long> responseTimeStats = new ConcurrentHashMap<>();

    public StructuredLoggingConfig(UnifiedConfigManager unifiedConfigManager) {
        this.unifiedConfigManager = unifiedConfigManager;
    }

    @Bean
    public ObjectMapper structuredLogMapper() {
        return new ObjectMapper();
    }

    /**
     * 记录结构化日志
     *
     * @param level   日志级别
     * @param message 消息
     * @param context 上下文信息
     */
    public void logStructured(String level, String message, Map<String, Object> context) {
        // 注意：这里保留.block()是因为这是同步日志记录方法，需要立即检查配置状态
        // 根据项目规则51，日志记录方法中的.block()调用是必要的，用于同步配置检查
        if (!Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.logging.structured.enabled", "true"))) {
            return;
        }

        try {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
            logEntry.put("level", level);
            logEntry.put("message", message);
            logEntry.put("application", "honyrun-backend");
            logEntry.put("thread", Thread.currentThread().getName());

            if (context != null && !context.isEmpty()) {
                logEntry.put("context", context);
            }

            String jsonLog = new ObjectMapper().writeValueAsString(logEntry);

            switch (level.toUpperCase()) {
                case "ERROR":
                    LoggingUtil.error(logger, jsonLog);
                    break;
                case "WARN":
                    LoggingUtil.warn(logger, jsonLog);
                    break;
                case "INFO":
                    LoggingUtil.info(logger, jsonLog);
                    break;
                case "DEBUG":
                    LoggingUtil.debug(logger, jsonLog);
                    break;
                default:
                    LoggingUtil.info(logger, jsonLog);
            }
        } catch (Exception e) {
            LoggingUtil.error(logger, "结构化日志记录失败: {}", e.getMessage());
        }
    }

    /**
     * 记录性能指标
     *
     * @param operation 操作名称
     * @param duration  执行时长（毫秒）
     * @param success   是否成功
     */
    public void logPerformance(String operation, long duration, boolean success) {
        // 注意：这里保留.block()是因为这是同步日志记录方法，需要立即检查配置状态
        // 根据项目规则51，日志记录方法中的.block()调用是必要的，用于同步配置检查
        if (!Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.logging.performance.enabled", "true"))) {
            return;
        }

        // 更新计数器
        performanceCounters.computeIfAbsent(operation, k -> new AtomicLong(0)).incrementAndGet();
        responseTimeStats.put(operation + "_last_duration", duration);

        if (!success) {
            errorCounters.computeIfAbsent(operation, k -> new AtomicLong(0)).incrementAndGet();
        }

        // 记录结构化性能日志
        Map<String, Object> perfContext = new HashMap<>();
        perfContext.put("operation", operation);
        perfContext.put("duration_ms", duration);
        perfContext.put("success", success);
        perfContext.put("total_calls", performanceCounters.get(operation).get());

        if (!success) {
            perfContext.put("error_count", errorCounters.get(operation).get());
        }

        logStructured("INFO", "Performance Metric", perfContext);
    }

    /**
     * 记录业务事件
     *
     * @param eventType 事件类型
     * @param eventData 事件数据
     */
    public void logBusinessEvent(String eventType, Map<String, Object> eventData) {
        Map<String, Object> eventContext = new HashMap<>();
        eventContext.put("event_type", eventType);
        eventContext.put("event_data", eventData);

        logStructured("INFO", "Business Event", eventContext);
    }

    /**
     * 记录错误事件
     *
     * @param errorType    错误类型
     * @param errorMessage 错误消息
     * @param stackTrace   堆栈跟踪
     */
    public void logError(String errorType, String errorMessage, String stackTrace) {
        Map<String, Object> errorContext = new HashMap<>();
        errorContext.put("error_type", errorType);
        errorContext.put("error_message", errorMessage);

        if (stackTrace != null && !stackTrace.isEmpty()) {
            errorContext.put("stack_trace", stackTrace);
        }

        logStructured("ERROR", "Application Error", errorContext);

        // 更新错误计数
        errorCounters.computeIfAbsent(errorType, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 记录启动事件
     *
     * @param component 组件名称
     * @param status    启动状态
     * @param duration  启动耗时
     */
    public void logStartupEvent(String component, String status, long duration) {
        Map<String, Object> startupContext = new HashMap<>();
        startupContext.put("component", component);
        startupContext.put("status", status);
        startupContext.put("duration_ms", duration);

        logStructured("INFO", "Startup Event", startupContext);
    }

    /**
     * 记录健康检查事件
     *
     * @param component     组件名称
     * @param healthy       是否健康
     * @param checkDuration 检查耗时
     */
    public void logHealthCheck(String component, boolean healthy, long checkDuration) {
        Map<String, Object> healthContext = new HashMap<>();
        healthContext.put("component", component);
        healthContext.put("healthy", healthy);
        healthContext.put("check_duration_ms", checkDuration);

        logStructured("INFO", "Health Check", healthContext);
    }

    /**
     * 定时输出性能统计汇总
     */
    @Scheduled(fixedRate = 300000) // 每5分钟
    public void logPerformanceSummary() {
        if (!Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.logging.performance.enabled", "true")) || performanceCounters.isEmpty()) {
            return;
        }

        Map<String, Object> summaryContext = new HashMap<>();
        summaryContext.put("performance_counters", new HashMap<>(performanceCounters));
        summaryContext.put("error_counters", new HashMap<>(errorCounters));
        summaryContext.put("response_times", new HashMap<>(responseTimeStats));

        logStructured("INFO", "Performance Summary", summaryContext);

        LoggingUtil.info(logger, "性能统计汇总已记录 - 操作数: {}, 错误数: {}",
                performanceCounters.size(), errorCounters.size());
    }

    /**
     * 获取性能统计信息
     *
     * @return 性能统计Map
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("performance_counters", new HashMap<>(performanceCounters));
        stats.put("error_counters", new HashMap<>(errorCounters));
        stats.put("response_times", new HashMap<>(responseTimeStats));
        return stats;
    }

    /**
     * 清除性能统计
     */
    public void clearPerformanceStats() {
        performanceCounters.clear();
        errorCounters.clear();
        responseTimeStats.clear();
        LoggingUtil.info(logger, "性能统计已清除");
    }

    /**
     * 检查是否启用结构化日志
     *
     * @return 是否启用
     */
    public boolean isStructuredLoggingEnabled() {
        // 注意：这里保留.block()是因为这是同步状态检查方法，需要立即返回配置状态
        // 根据项目规则51，状态检查方法中的.block()调用是必要的，用于同步返回配置状态
        return Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.logging.structured.enabled", "true"));
    }

    /**
     * 检查是否启用性能日志
     *
     * @return 是否启用
     */
    public boolean isPerformanceLoggingEnabled() {
        // 注意：这里保留.block()是因为这是同步状态检查方法，需要立即返回配置状态
        // 根据项目规则51，状态检查方法中的.block()调用是必要的，用于同步返回配置状态
        return Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.logging.performance.enabled", "true"));
    }
}

