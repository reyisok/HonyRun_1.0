package com.honyrun.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 系统监控专用日志工具类
 *
 * 提供系统监控数据的文件日志记录功能，替代数据库写入方式：
 * - 性能监控数据记录
 * - 健康检查结果记录
 * - 资源使用情况记录
 * - 告警信息记录
 *
 * 【设计目标】：
 * 1. 减少数据库写入压力，提升系统性能
 * 2. 提供结构化的监控日志格式
 * 3. 支持日志轮转和归档
 * 4. 便于监控数据分析和查询
 *
 * 【重要提醒 - 日志路径配置】：
 * 本工具类使用SLF4J Logger，日志文件路径由logback-spring.xml配置控制。
 * 确保logback-spring.xml中LOG_PATH配置为相对路径"logs"，避免路径重复。
 * 正确路径：\HonyRun\backend\logs\
 * 错误路径：\honyrun_mv20251013\HonyRun\backend\backend\logs\
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-02 当前时间
 * @modified 2025-10-23 16:57:03
 * @version 1.0.1 - 添加日志路径配置说明
 */
public class MonitoringLogUtil {

    // 监控专用日志记录器 - 使用独立的日志配置
    private static final Logger PERFORMANCE_LOGGER = LoggerFactory.getLogger("MONITORING.PERFORMANCE");
    private static final Logger HEALTH_LOGGER = LoggerFactory.getLogger("MONITORING.HEALTH");
    private static final Logger RESOURCE_LOGGER = LoggerFactory.getLogger("MONITORING.RESOURCE");
    private static final Logger ALERT_LOGGER = LoggerFactory.getLogger("MONITORING.ALERT");
    private static final Logger SYSTEM_LOGGER = LoggerFactory.getLogger("MONITORING.SYSTEM");

    // 日志格式化器
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // ==================== 性能监控日志 ====================

    /**
     * 记录性能监控数据
     *
     * @param operation 操作类型
     * @param metrics   性能指标数据
     */
    public static void logPerformanceMetrics(String operation, Map<String, Object> metrics) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        StringBuilder logMessage = new StringBuilder();

        logMessage.append("PERFORMANCE_METRICS|")
                .append("timestamp=").append(timestamp).append("|")
                .append("operation=").append(operation).append("|");

        // 添加性能指标
        metrics.forEach((key, value) -> logMessage.append(key).append("=").append(value).append("|"));

        PERFORMANCE_LOGGER.info(logMessage.toString());
    }

    /**
     * 记录性能告警
     *
     * @param alertType    告警类型
     * @param threshold    阈值
     * @param currentValue 当前值
     * @param message      告警消息
     */
    public static void logPerformanceAlert(String alertType, double threshold, double currentValue, String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        PERFORMANCE_LOGGER.warn("PERFORMANCE_ALERT|timestamp={}|type={}|threshold={}|current={}|message={}",
                timestamp, alertType, threshold, currentValue, message);
    }

    // ==================== 健康检查日志 ====================

    /**
     * 记录健康检查结果
     *
     * @param component 组件名称
     * @param status    健康状态
     * @param details   详细信息
     */
    public static void logHealthCheck(String component, String status, Map<String, Object> details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        StringBuilder logMessage = new StringBuilder();

        logMessage.append("HEALTH_CHECK|")
                .append("timestamp=").append(timestamp).append("|")
                .append("component=").append(component).append("|")
                .append("status=").append(status).append("|");

        // 添加详细信息
        if (details != null && !details.isEmpty()) {
            details.forEach((key, value) -> logMessage.append(key).append("=").append(value).append("|"));
        }

        if ("UP".equals(status)) {
            HEALTH_LOGGER.info(logMessage.toString());
        } else {
            HEALTH_LOGGER.warn(logMessage.toString());
        }
    }

    // ==================== 资源使用日志 ====================

    /**
     * 记录资源使用情况
     *
     * @param resourceType 资源类型（MEMORY, CPU, DISK等）
     * @param usage        使用情况数据
     */
    public static void logResourceUsage(String resourceType, Map<String, Object> usage) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        StringBuilder logMessage = new StringBuilder();

        logMessage.append("RESOURCE_USAGE|")
                .append("timestamp=").append(timestamp).append("|")
                .append("type=").append(resourceType).append("|");

        // 添加使用情况数据
        usage.forEach((key, value) -> logMessage.append(key).append("=").append(value).append("|"));

        RESOURCE_LOGGER.info(logMessage.toString());
    }

    /**
     * 记录资源告警
     *
     * @param resourceType 资源类型
     * @param alertLevel   告警级别
     * @param message      告警消息
     */
    public static void logResourceAlert(String resourceType, String alertLevel, String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        RESOURCE_LOGGER.warn("RESOURCE_ALERT|timestamp={}|type={}|level={}|message={}",
                timestamp, resourceType, alertLevel, message);
    }

    // ==================== 系统告警日志 ====================

    /**
     * 记录系统告警
     *
     * @param alertType 告警类型
     * @param level     告警级别（INFO, WARN, ERROR）
     * @param message   告警消息
     * @param details   详细信息
     */
    public static void logSystemAlert(String alertType, String level, String message, Map<String, Object> details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        StringBuilder logMessage = new StringBuilder();

        logMessage.append("SYSTEM_ALERT|")
                .append("timestamp=").append(timestamp).append("|")
                .append("type=").append(alertType).append("|")
                .append("level=").append(level).append("|")
                .append("message=").append(message).append("|");

        // 添加详细信息
        if (details != null && !details.isEmpty()) {
            details.forEach((key, value) -> logMessage.append(key).append("=").append(value).append("|"));
        }

        switch (level.toUpperCase()) {
            case "ERROR":
                ALERT_LOGGER.error(logMessage.toString());
                break;
            case "WARN":
                ALERT_LOGGER.warn(logMessage.toString());
                break;
            default:
                ALERT_LOGGER.info(logMessage.toString());
                break;
        }
    }

    // ==================== 系统事件日志 ====================

    /**
     * 记录系统事件
     *
     * @param eventType 事件类型
     * @param operation 操作类型
     * @param message   事件消息
     */
    public static void logSystemEvent(String eventType, String operation, String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        SYSTEM_LOGGER.info("SYSTEM_EVENT|timestamp={}|type={}|operation={}|message={}",
                timestamp, eventType, operation, message);
    }

    /**
     * 记录系统启动事件
     *
     * @param phase    启动阶段
     * @param duration 耗时（毫秒）
     * @param message  启动消息
     */
    public static void logStartupEvent(String phase, long duration, String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        SYSTEM_LOGGER.info("STARTUP_EVENT|timestamp={}|phase={}|duration={}ms|message={}",
                timestamp, phase, duration, message);
    }

    /**
     * 记录系统运行时间
     *
     * @param uptime          运行时间（毫秒）
     * @param formattedUptime 格式化的运行时间（HH:MM:SS）
     */
    public static void logSystemUptime(long uptime, String formattedUptime) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        SYSTEM_LOGGER.info("SYSTEM_UPTIME|timestamp={}|uptime={}ms|formatted={}",
                timestamp, uptime, formattedUptime);
    }

    // ==================== 并发用户监控日志 ====================

    /**
     * 记录并发用户统计
     *
     * @param stats 用户统计数据
     */
    public static void logConcurrentUserStats(Map<String, Object> stats) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        StringBuilder logMessage = new StringBuilder();

        logMessage.append("CONCURRENT_USERS|")
                .append("timestamp=").append(timestamp).append("|");

        // 添加用户统计数据
        stats.forEach((key, value) -> logMessage.append(key).append("=").append(value).append("|"));

        SYSTEM_LOGGER.info(logMessage.toString());
    }

    // ==================== 日志清理事件 ====================

    /**
     * 记录日志清理事件
     *
     * @param cleanupType  清理类型
     * @param deletedCount 删除数量
     * @param cleanupDate  清理截止日期
     */
    public static void logCleanupEvent(String cleanupType, long deletedCount, LocalDateTime cleanupDate) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        SYSTEM_LOGGER.info("CLEANUP_EVENT|timestamp={}|type={}|deleted={}|cleanup_date={}",
                timestamp, cleanupType, deletedCount, cleanupDate.format(TIMESTAMP_FORMATTER));
    }
}
