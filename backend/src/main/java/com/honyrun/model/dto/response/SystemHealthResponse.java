package com.honyrun.model.dto.response;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 系统健康检查响应DTO
 *
 * 用于返回系统健康检查的结果信息，包含系统状态、组件健康度、性能指标等
 * 支持响应式数据传输和JSON序列化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  17:15:00
 * @modified 2025-07-01 17:15:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class SystemHealthResponse {

    // ==================== 基本健康信息 ====================

    /**
     * 系统整体健康状态
     * UP - 健康, DOWN - 不健康, DEGRADED - 降级, UNKNOWN - 未知
     */
    private String status;

    /**
     * 健康检查时间
     */
    private LocalDateTime checkTime;

    /**
     * 系统版本
     */
    private String version;

    /**
     * 系统启动时间
     */
    private LocalDateTime startTime;

    /**
     * 系统运行时长（毫秒）
     */
    private Long uptime;

    // ==================== 组件健康状态 ====================

    /**
     * 数据库健康状态
     */
    private ComponentHealth database;

    /**
     * Redis健康状态
     */
    private ComponentHealth redis;

    /**
     * 外部服务健康状态
     */
    private Map<String, ComponentHealth> externalServices;

    /**
     * 业务功能健康状态
     */
    private Map<String, ComponentHealth> businessFunctions;

    // ==================== 系统资源信息 ====================

    /**
     * CPU使用率（百分比）
     */
    private Float cpuUsage;

    /**
     * 内存使用率（百分比）
     */
    private Float memoryUsage;

    /**
     * 磁盘使用率（百分比）
     */
    private Float diskUsage;

    /**
     * 活跃线程数
     */
    private Integer activeThreads;

    /**
     * 当前并发用户数
     */
    private Integer concurrentUsers;

    // ==================== 性能指标 ====================

    /**
     * 平均响应时间（毫秒）
     */
    private Long averageResponseTime;

    /**
     * 每秒请求数
     */
    private Float requestsPerSecond;

    /**
     * 错误率（百分比）
     */
    private Float errorRate;

    /**
     * 最近1分钟负载
     */
    private Float loadAverage1m;

    /**
     * 最近5分钟负载
     */
    private Float loadAverage5m;

    /**
     * 最近15分钟负载
     */
    private Float loadAverage15m;

    // ==================== 告警信息 ====================

    /**
     * 告警数量
     */
    private Integer alertCount;

    /**
     * 告警信息列表
     */
    private java.util.List<String> alerts;

    /**
     * 建议操作
     */
    private java.util.List<String> recommendations;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public SystemHealthResponse() {
        this.checkTime = LocalDateTime.now();
        this.status = "UNKNOWN";
        this.alertCount = 0;
        this.alerts = new java.util.ArrayList<>();
        this.recommendations = new java.util.ArrayList<>();
    }

    /**
     * 带状态的构造函数
     *
     * @param status 健康状态
     */
    public SystemHealthResponse(String status) {
        this();
        this.status = status;
    }

    // ==================== 内部类：组件健康状态 ====================

    /**
     * 组件健康状态
     */
    public static class ComponentHealth {
        /**
         * 组件状态
         */
        private String status;

        /**
         * 组件名称
         */
        private String name;

        /**
         * 响应时间（毫秒）
         */
        private Long responseTime;

        /**
         * 错误信息
         */
        private String error;

        /**
         * 详细信息
         */
        private Map<String, Object> details;

        /**
         * 检查时间
         */
        private LocalDateTime checkTime;

        public ComponentHealth() {
            this.checkTime = LocalDateTime.now();
        }

        public ComponentHealth(String name, String status) {
            this();
            this.name = name;
            this.status = status;
        }

        // Getter和Setter方法
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getResponseTime() { return responseTime; }
        public void setResponseTime(Long responseTime) { this.responseTime = responseTime; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }
        public LocalDateTime getCheckTime() { return checkTime; }
        public void setCheckTime(LocalDateTime checkTime) { this.checkTime = checkTime; }

        public boolean isHealthy() { return "UP".equals(status); }
        public boolean isDown() { return "DOWN".equals(status); }
    }

    // ==================== Getter和Setter方法 ====================

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(LocalDateTime checkTime) {
        this.checkTime = checkTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Long getUptime() {
        return uptime;
    }

    public void setUptime(Long uptime) {
        this.uptime = uptime;
    }

    public ComponentHealth getDatabase() {
        return database;
    }

    public void setDatabase(ComponentHealth database) {
        this.database = database;
    }

    public ComponentHealth getRedis() {
        return redis;
    }

    public void setRedis(ComponentHealth redis) {
        this.redis = redis;
    }

    public Map<String, ComponentHealth> getExternalServices() {
        return externalServices;
    }

    public void setExternalServices(Map<String, ComponentHealth> externalServices) {
        this.externalServices = externalServices;
    }

    public Map<String, ComponentHealth> getBusinessFunctions() {
        return businessFunctions;
    }

    public void setBusinessFunctions(Map<String, ComponentHealth> businessFunctions) {
        this.businessFunctions = businessFunctions;
    }

    public Float getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Float cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Float getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Float memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public Float getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(Float diskUsage) {
        this.diskUsage = diskUsage;
    }

    public Integer getActiveThreads() {
        return activeThreads;
    }

    public void setActiveThreads(Integer activeThreads) {
        this.activeThreads = activeThreads;
    }

    public Integer getConcurrentUsers() {
        return concurrentUsers;
    }

    public void setConcurrentUsers(Integer concurrentUsers) {
        this.concurrentUsers = concurrentUsers;
    }

    public Long getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(Long averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public Float getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public void setRequestsPerSecond(Float requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }

    public Float getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(Float errorRate) {
        this.errorRate = errorRate;
    }

    public Float getLoadAverage1m() {
        return loadAverage1m;
    }

    public void setLoadAverage1m(Float loadAverage1m) {
        this.loadAverage1m = loadAverage1m;
    }

    public Float getLoadAverage5m() {
        return loadAverage5m;
    }

    public void setLoadAverage5m(Float loadAverage5m) {
        this.loadAverage5m = loadAverage5m;
    }

    public Float getLoadAverage15m() {
        return loadAverage15m;
    }

    public void setLoadAverage15m(Float loadAverage15m) {
        this.loadAverage15m = loadAverage15m;
    }

    public Integer getAlertCount() {
        return alertCount;
    }

    public void setAlertCount(Integer alertCount) {
        this.alertCount = alertCount;
    }

    public java.util.List<String> getAlerts() {
        return alerts;
    }

    public void setAlerts(java.util.List<String> alerts) {
        this.alerts = alerts;
    }

    public java.util.List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(java.util.List<String> recommendations) {
        this.recommendations = recommendations;
    }

    // ==================== 业务方法 ====================

    /**
     * 系统是否健康
     *
     * @return 系统是否健康
     */
    public boolean isHealthy() {
        return "UP".equals(status);
    }

    /**
     * 系统是否不健康
     *
     * @return 系统是否不健康
     */
    public boolean isDown() {
        return "DOWN".equals(status);
    }

    /**
     * 系统是否降级
     *
     * @return 系统是否降级
     */
    public boolean isDegraded() {
        return "DEGRADED".equals(status);
    }

    /**
     * 是否有告警
     *
     * @return 是否有告警
     */
    public boolean hasAlerts() {
        return alertCount != null && alertCount > 0;
    }

    /**
     * 添加告警
     *
     * @param alert 告警信息
     */
    public void addAlert(String alert) {
        if (alerts == null) {
            alerts = new java.util.ArrayList<>();
        }
        alerts.add(alert);
        alertCount = alerts.size();
    }

    /**
     * 添加建议
     *
     * @param recommendation 建议信息
     */
    public void addRecommendation(String recommendation) {
        if (recommendations == null) {
            recommendations = new java.util.ArrayList<>();
        }
        recommendations.add(recommendation);
    }

    /**
     * 获取运行时长描述
     *
     * @return 运行时长描述
     */
    public String getUptimeDescription() {
        if (uptime == null) {
            return "未知";
        }

        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%d天%d小时%d分钟", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟", minutes);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    /**
     * 获取整体健康评分（0-100）
     *
     * @return 健康评分
     */
    public Integer getHealthScore() {
        if ("UP".equals(status)) {
            int score = 100;

            // 根据资源使用率扣分
            if (cpuUsage != null && cpuUsage > 80) score -= 10;
            if (memoryUsage != null && memoryUsage > 85) score -= 10;
            if (diskUsage != null && diskUsage > 90) score -= 15;

            // 根据错误率扣分
            if (errorRate != null && errorRate > 5) score -= 20;

            // 根据告警数量扣分
            if (alertCount != null && alertCount > 0) score -= alertCount * 5;

            return Math.max(score, 0);
        } else if ("DEGRADED".equals(status)) {
            return 60;
        } else if ("DOWN".equals(status)) {
            return 0;
        } else {
            return null;
        }
    }

    // ==================== Object方法重写 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SystemHealthResponse that = (SystemHealthResponse) obj;
        return Objects.equals(status, that.status) &&
               Objects.equals(checkTime, that.checkTime) &&
               Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, checkTime, version);
    }

    @Override
    public String toString() {
        return "SystemHealthResponse{" +
                "status='" + status + '\'' +
                ", checkTime=" + checkTime +
                ", version='" + version + '\'' +
                ", cpuUsage=" + cpuUsage +
                ", memoryUsage=" + memoryUsage +
                ", alertCount=" + alertCount +
                ", healthScore=" + getHealthScore() +
                '}';
    }
}


