package com.honyrun.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 系统状态响应DTO
 *
 * 用于返回系统运行状态信息的响应对象，包含系统健康状态、性能指标、资源使用情况等
 * 支持响应式数据传输和JSON序列化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  22:20:00
 * @modified 2025-07-01 22:20:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemStatusResponse {

    /**
     * 系统名称
     */
    @JsonProperty("systemName")
    private String systemName;

    /**
     * 系统版本
     */
    @JsonProperty("systemVersion")
    private String systemVersion;

    /**
     * 健康状态
     * HEALTHY-健康，UNHEALTHY-不健康，DOWN-宕机，UNKNOWN-未知
     */
    @JsonProperty("healthStatus")
    private String healthStatus;

    /**
     * 系统状态
     * RUNNING-运行中，STOPPED-已停止，STARTING-启动中，STOPPING-停止中
     */
    @JsonProperty("systemStatus")
    private String systemStatus;

    /**
     * 状态（用于兼容性）
     * 映射到systemStatus的值，用于满足API测试的期望
     */
    @JsonProperty("status")
    private String status;

    /**
     * 启动时间
     */
    @JsonProperty("startTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 运行时长（毫秒）
     */
    @JsonProperty("uptime")
    private Long uptime;

    /**
     * 运行时长（格式化）
     */
    @JsonProperty("uptimeFormatted")
    private String uptimeFormatted;

    /**
     * CPU使用率（百分比）
     */
    @JsonProperty("cpuUsage")
    private Double cpuUsage;

    /**
     * 内存使用率（百分比）
     */
    @JsonProperty("memoryUsage")
    private Double memoryUsage;

    /**
     * 磁盘使用率（百分比）
     */
    @JsonProperty("diskUsage")
    private Double diskUsage;

    /**
     * 错误计数
     */
    @JsonProperty("errorCount")
    private Long errorCount;

    /**
     * 警告计数
     */
    @JsonProperty("warningCount")
    private Long warningCount;

    /**
     * 处理请求数
     */
    @JsonProperty("requestCount")
    private Long requestCount;

    /**
     * 平均响应时间（毫秒）
     */
    @JsonProperty("avgResponseTime")
    private Double avgResponseTime;

    /**
     * 并发用户数
     */
    @JsonProperty("concurrentUsers")
    private Integer concurrentUsers;

    /**
     * 活跃连接数
     */
    @JsonProperty("activeConnections")
    private Integer activeConnections;

    /**
     * 数据库连接状态
     */
    @JsonProperty("databaseStatus")
    private String databaseStatus;

    /**
     * Redis连接状态
     */
    @JsonProperty("redisStatus")
    private String redisStatus;

    /**
     * JVM信息
     */
    @JsonProperty("jvmInfo")
    private Map<String, Object> jvmInfo;

    /**
     * 内存信息
     */
    @JsonProperty("memoryInfo")
    private Map<String, Object> memoryInfo;

    /**
     * 系统信息
     */
    @JsonProperty("systemInfo")
    private Map<String, Object> systemInfo;

    /**
     * 性能指标
     */
    @JsonProperty("performanceMetrics")
    private Map<String, Object> performanceMetrics;

    /**
     * 状态描述
     */
    @JsonProperty("statusDescription")
    private String statusDescription;

    /**
     * 最后检查时间
     */
    @JsonProperty("lastCheckTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCheckTime;

    /**
     * 下次检查时间
     */
    @JsonProperty("nextCheckTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextCheckTime;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public SystemStatusResponse() {
        this.lastCheckTime = LocalDateTime.now();
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取系统名称
     *
     * @return 系统名称
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * 设置系统名称
     *
     * @param systemName 系统名称
     */
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    /**
     * 获取系统版本
     *
     * @return 系统版本
     */
    public String getSystemVersion() {
        return systemVersion;
    }

    /**
     * 设置系统版本
     *
     * @param systemVersion 系统版本
     */
    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    /**
     * 获取健康状态
     *
     * @return 健康状态
     */
    public String getHealthStatus() {
        return healthStatus;
    }

    /**
     * 设置健康状态
     *
     * @param healthStatus 健康状态
     */
    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    /**
     * 获取系统状态
     *
     * @return 系统状态
     */
    public String getSystemStatus() {
        return systemStatus;
    }

    /**
     * 设置系统状态
     *
     * @param systemStatus 系统状态
     */
    public void setSystemStatus(String systemStatus) {
        this.systemStatus = systemStatus;
        this.status = systemStatus; // 同时设置status字段以保持兼容性
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态
     *
     * @param status 状态
     */
    public void setStatus(String status) {
        this.status = status;
        this.systemStatus = status; // 同时设置systemStatus字段以保持一致性
    }

    /**
     * 获取启动时间
     *
     * @return 启动时间
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * 设置启动时间
     *
     * @param startTime 启动时间
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * 获取运行时长
     *
     * @return 运行时长（毫秒）
     */
    public Long getUptime() {
        return uptime;
    }

    /**
     * 设置运行时长
     *
     * @param uptime 运行时长（毫秒）
     */
    public void setUptime(Long uptime) {
        this.uptime = uptime;
        this.uptimeFormatted = formatUptime(uptime);
    }

    /**
     * 获取格式化的运行时长
     *
     * @return 格式化的运行时长
     */
    public String getUptimeFormatted() {
        return uptimeFormatted;
    }

    /**
     * 设置格式化的运行时长
     *
     * @param uptimeFormatted 格式化的运行时长
     */
    public void setUptimeFormatted(String uptimeFormatted) {
        this.uptimeFormatted = uptimeFormatted;
    }

    /**
     * 获取CPU使用率
     *
     * @return CPU使用率
     */
    public Double getCpuUsage() {
        return cpuUsage;
    }

    /**
     * 设置CPU使用率
     *
     * @param cpuUsage CPU使用率
     */
    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    /**
     * 获取内存使用率
     *
     * @return 内存使用率
     */
    public Double getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * 设置内存使用率
     *
     * @param memoryUsage 内存使用率
     */
    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    /**
     * 获取磁盘使用率
     *
     * @return 磁盘使用率
     */
    public Double getDiskUsage() {
        return diskUsage;
    }

    /**
     * 设置磁盘使用率
     *
     * @param diskUsage 磁盘使用率
     */
    public void setDiskUsage(Double diskUsage) {
        this.diskUsage = diskUsage;
    }

    /**
     * 获取错误计数
     *
     * @return 错误计数
     */
    public Long getErrorCount() {
        return errorCount;
    }

    /**
     * 设置错误计数
     *
     * @param errorCount 错误计数
     */
    public void setErrorCount(Long errorCount) {
        this.errorCount = errorCount;
    }

    /**
     * 获取警告计数
     *
     * @return 警告计数
     */
    public Long getWarningCount() {
        return warningCount;
    }

    /**
     * 设置警告计数
     *
     * @param warningCount 警告计数
     */
    public void setWarningCount(Long warningCount) {
        this.warningCount = warningCount;
    }

    /**
     * 获取处理请求数
     *
     * @return 处理请求数
     */
    public Long getRequestCount() {
        return requestCount;
    }

    /**
     * 设置处理请求数
     *
     * @param requestCount 处理请求数
     */
    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }

    /**
     * 获取平均响应时间
     *
     * @return 平均响应时间
     */
    public Double getAvgResponseTime() {
        return avgResponseTime;
    }

    /**
     * 设置平均响应时间
     *
     * @param avgResponseTime 平均响应时间
     */
    public void setAvgResponseTime(Double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    /**
     * 获取并发用户数
     *
     * @return 并发用户数
     */
    public Integer getConcurrentUsers() {
        return concurrentUsers;
    }

    /**
     * 设置并发用户数
     *
     * @param concurrentUsers 并发用户数
     */
    public void setConcurrentUsers(Integer concurrentUsers) {
        this.concurrentUsers = concurrentUsers;
    }

    /**
     * 获取活跃连接数
     *
     * @return 活跃连接数
     */
    public Integer getActiveConnections() {
        return activeConnections;
    }

    /**
     * 设置活跃连接数
     *
     * @param activeConnections 活跃连接数
     */
    public void setActiveConnections(Integer activeConnections) {
        this.activeConnections = activeConnections;
    }

    /**
     * 获取数据库连接状态
     *
     * @return 数据库连接状态
     */
    public String getDatabaseStatus() {
        return databaseStatus;
    }

    /**
     * 设置数据库连接状态
     *
     * @param databaseStatus 数据库连接状态
     */
    public void setDatabaseStatus(String databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    /**
     * 获取Redis连接状态
     *
     * @return Redis连接状态
     */
    public String getRedisStatus() {
        return redisStatus;
    }

    /**
     * 设置Redis连接状态
     *
     * @param redisStatus Redis连接状态
     */
    public void setRedisStatus(String redisStatus) {
        this.redisStatus = redisStatus;
    }

    /**
     * 获取JVM信息
     *
     * @return JVM信息
     */
    public Map<String, Object> getJvmInfo() {
        return jvmInfo;
    }

    /**
     * 设置JVM信息
     *
     * @param jvmInfo JVM信息
     */
    public void setJvmInfo(Map<String, Object> jvmInfo) {
        this.jvmInfo = jvmInfo;
    }

    /**
     * 获取内存信息
     *
     * @return 内存信息
     */
    public Map<String, Object> getMemoryInfo() {
        return memoryInfo;
    }

    /**
     * 设置内存信息
     *
     * @param memoryInfo 内存信息
     */
    public void setMemoryInfo(Map<String, Object> memoryInfo) {
        this.memoryInfo = memoryInfo;
    }

    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    public Map<String, Object> getSystemInfo() {
        return systemInfo;
    }

    /**
     * 设置系统信息
     *
     * @param systemInfo 系统信息
     */
    public void setSystemInfo(Map<String, Object> systemInfo) {
        this.systemInfo = systemInfo;
    }

    /**
     * 获取性能指标
     *
     * @return 性能指标
     */
    public Map<String, Object> getPerformanceMetrics() {
        return performanceMetrics;
    }

    /**
     * 设置性能指标
     *
     * @param performanceMetrics 性能指标
     */
    public void setPerformanceMetrics(Map<String, Object> performanceMetrics) {
        this.performanceMetrics = performanceMetrics;
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
     */
    public String getStatusDescription() {
        return statusDescription;
    }

    /**
     * 设置状态描述
     *
     * @param statusDescription 状态描述
     */
    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    /**
     * 获取最后检查时间
     *
     * @return 最后检查时间
     */
    public LocalDateTime getLastCheckTime() {
        return lastCheckTime;
    }

    /**
     * 设置最后检查时间
     *
     * @param lastCheckTime 最后检查时间
     */
    public void setLastCheckTime(LocalDateTime lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    /**
     * 获取下次检查时间
     *
     * @return 下次检查时间
     */
    public LocalDateTime getNextCheckTime() {
        return nextCheckTime;
    }

    /**
     * 设置下次检查时间
     *
     * @param nextCheckTime 下次检查时间
     */
    public void setNextCheckTime(LocalDateTime nextCheckTime) {
        this.nextCheckTime = nextCheckTime;
    }

    // ==================== 工具方法 ====================

    /**
     * 判断系统是否健康
     *
     * @return true-健康，false-不健康
     */
    public boolean isHealthy() {
        return "HEALTHY".equalsIgnoreCase(healthStatus);
    }

    /**
     * 判断系统是否运行中
     *
     * @return true-运行中，false-未运行
     */
    public boolean isRunning() {
        return "RUNNING".equalsIgnoreCase(systemStatus);
    }

    /**
     * 格式化运行时长
     *
     * @param uptimeMillis 运行时长（毫秒）
     * @return 格式化的运行时长
     */
    private String formatUptime(Long uptimeMillis) {
        if (uptimeMillis == null) {
            return "未知";
        }

        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%d天%d小时%d分钟", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    /**
     * 获取系统健康等级
     *
     * @return 健康等级（1-5，5为最健康）
     */
    public int getHealthLevel() {
        if (!isHealthy()) {
            return 1;
        }

        int level = 5;

        // 根据CPU使用率调整等级
        if (cpuUsage != null && cpuUsage > 80) {
            level--;
        }

        // 根据内存使用率调整等级
        if (memoryUsage != null && memoryUsage > 80) {
            level--;
        }

        // 根据错误数量调整等级
        if (errorCount != null && errorCount > 10) {
            level--;
        }

        // 根据响应时间调整等级
        if (avgResponseTime != null && avgResponseTime > 1000) {
            level--;
        }

        return Math.max(1, level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemStatusResponse that = (SystemStatusResponse) o;
        return Objects.equals(systemName, that.systemName) &&
               Objects.equals(systemVersion, that.systemVersion) &&
               Objects.equals(healthStatus, that.healthStatus) &&
               Objects.equals(systemStatus, that.systemStatus) &&
               Objects.equals(lastCheckTime, that.lastCheckTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(systemName, systemVersion, healthStatus, systemStatus, lastCheckTime);
    }

    @Override
    public String toString() {
        return "SystemStatusResponse{" +
               "systemName='" + systemName + '\'' +
               ", systemVersion='" + systemVersion + '\'' +
               ", healthStatus='" + healthStatus + '\'' +
               ", systemStatus='" + systemStatus + '\'' +
               ", uptime=" + uptime +
               ", cpuUsage=" + cpuUsage +
               ", memoryUsage=" + memoryUsage +
               ", errorCount=" + errorCount +
               ", requestCount=" + requestCount +
               ", concurrentUsers=" + concurrentUsers +
               ", lastCheckTime=" + lastCheckTime +
               '}';
    }
}


