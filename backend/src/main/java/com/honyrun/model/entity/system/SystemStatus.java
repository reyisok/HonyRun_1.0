package com.honyrun.model.entity.system;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 系统状态实体类
 *
 * 系统运行状态管理实体，支持系统运行状态、健康检查、性能指标等功能
 * 继承AuditableEntity，支持R2DBC响应式数据访问和审计功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  18:50:00
 * @modified 2025-07-01 18:50:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("sys_system_status")
public class SystemStatus extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 系统名称
     * 系统或服务的名称标识
     */
    @Column("system_name")
    private String systemName;

    /**
     * 系统版本
     * 当前系统的版本号
     */
    @Column("system_version")
    private String systemVersion;

    /**
     * 运行状态
     * RUNNING-运行中，STOPPED-已停止，STARTING-启动中，STOPPING-停止中，ERROR-错误
     */
    @Column("status")
    private String status;

    /**
     * 健康状态
     * HEALTHY-健康，UNHEALTHY-不健康，DEGRADED-降级，UNKNOWN-未知
     */
    @Column("health_status")
    private String healthStatus;

    /**
     * 启动时间
     * 系统启动的时间
     */
    @Column("startup_time")
    private LocalDateTime startupTime;

    /**
     * 最后心跳时间
     * 系统最后一次心跳检测的时间
     */
    @Column("last_heartbeat")
    private LocalDateTime lastHeartbeat;

    /**
     * 运行时长
     * 系统运行的总时长（秒）
     */
    @Column("uptime_seconds")
    private Long uptimeSeconds;

    /**
     * CPU使用率
     * 当前CPU使用率（百分比）
     */
    @Column("cpu_usage")
    private Double cpuUsage;

    /**
     * 内存使用率
     * 当前内存使用率（百分比）
     */
    @Column("memory_usage")
    private Double memoryUsage;

    /**
     * 磁盘使用率
     * 当前磁盘使用率（百分比）
     */
    @Column("disk_usage")
    private Double diskUsage;

    /**
     * 网络IO
     * 网络输入输出统计（JSON格式）
     */
    @Column("network_io")
    private String networkIo;

    /**
     * 活跃连接数
     * 当前活跃的连接数量
     */
    @Column("active_connections")
    private Integer activeConnections;

    /**
     * 活跃线程数
     * 当前活跃的线程数量
     */
    @Column("active_threads")
    private Integer activeThreads;

    /**
     * 数据库连接池状态
     * 数据库连接池的状态信息（JSON格式）
     */
    @Column("db_pool_status")
    private String dbPoolStatus;

    /**
     * Redis连接状态
     * Redis连接的状态信息（JSON格式）
     */
    @Column("redis_status")
    private String redisStatus;

    /**
     * JVM信息
     * JVM运行时信息（JSON格式）
     */
    @Column("jvm_info")
    private String jvmInfo;

    /**
     * 系统负载
     * 系统平均负载
     */
    @Column("system_load")
    private Double systemLoad;

    /**
     * 错误计数
     * 系统错误的累计数量
     */
    @Column("error_count")
    private Long errorCount;

    /**
     * 警告计数
     * 系统警告的累计数量
     */
    @Column("warning_count")
    private Long warningCount;

    /**
     * 处理请求数
     * 系统处理的请求总数
     */
    @Column("request_count")
    private Long requestCount;

    /**
     * 平均响应时间
     * 系统平均响应时间（毫秒）
     */
    @Column("avg_response_time")
    private Double avgResponseTime;

    /**
     * 状态描述
     * 系统状态的详细描述
     */
    @Column("status_description")
    private String statusDescription;

    /**
     * 检查时间
     * 状态检查的时间
     */
    @Column("check_time")
    private LocalDateTime checkTime;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public SystemStatus() {
        super();
        this.status = "UNKNOWN";
        this.healthStatus = "UNKNOWN";
        this.checkTime = LocalDateTime.now();
        this.errorCount = 0L;
        this.warningCount = 0L;
        this.requestCount = 0L;
        this.activeConnections = 0;
        this.activeThreads = 0;
    }

    /**
     * 带参数的构造函数
     *
     * @param systemName 系统名称
     * @param systemVersion 系统版本
     * @param status 运行状态
     * @param healthStatus 健康状态
     */
    public SystemStatus(String systemName, String systemVersion, String status, String healthStatus) {
        this();
        this.systemName = systemName;
        this.systemVersion = systemVersion;
        this.status = status;
        this.healthStatus = healthStatus;
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
     * 获取运行状态
     *
     * @return 运行状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置运行状态
     *
     * @param status 运行状态
     */
    public void setStatus(String status) {
        this.status = status;
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
     * 获取启动时间
     *
     * @return 启动时间
     */
    public LocalDateTime getStartupTime() {
        return startupTime;
    }

    /**
     * 设置启动时间
     *
     * @param startupTime 启动时间
     */
    public void setStartupTime(LocalDateTime startupTime) {
        this.startupTime = startupTime;
    }

    /**
     * 获取最后心跳时间
     *
     * @return 最后心跳时间
     */
    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    /**
     * 设置最后心跳时间
     *
     * @param lastHeartbeat 最后心跳时间
     */
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    /**
     * 获取运行时长
     *
     * @return 运行时长（秒）
     */
    public Long getUptimeSeconds() {
        return uptimeSeconds;
    }

    /**
     * 设置运行时长
     *
     * @param uptimeSeconds 运行时长（秒）
     */
    public void setUptimeSeconds(Long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    /**
     * 获取CPU使用率
     *
     * @return CPU使用率（百分比）
     */
    public Double getCpuUsage() {
        return cpuUsage;
    }

    /**
     * 设置CPU使用率
     *
     * @param cpuUsage CPU使用率（百分比）
     */
    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    /**
     * 获取内存使用率
     *
     * @return 内存使用率（百分比）
     */
    public Double getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * 设置内存使用率
     *
     * @param memoryUsage 内存使用率（百分比）
     */
    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    /**
     * 获取磁盘使用率
     *
     * @return 磁盘使用率（百分比）
     */
    public Double getDiskUsage() {
        return diskUsage;
    }

    /**
     * 设置磁盘使用率
     *
     * @param diskUsage 磁盘使用率（百分比）
     */
    public void setDiskUsage(Double diskUsage) {
        this.diskUsage = diskUsage;
    }

    /**
     * 获取网络IO
     *
     * @return 网络IO统计
     */
    public String getNetworkIo() {
        return networkIo;
    }

    /**
     * 设置网络IO
     *
     * @param networkIo 网络IO统计
     */
    public void setNetworkIo(String networkIo) {
        this.networkIo = networkIo;
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
     * 获取活跃线程数
     *
     * @return 活跃线程数
     */
    public Integer getActiveThreads() {
        return activeThreads;
    }

    /**
     * 设置活跃线程数
     *
     * @param activeThreads 活跃线程数
     */
    public void setActiveThreads(Integer activeThreads) {
        this.activeThreads = activeThreads;
    }

    /**
     * 获取数据库连接池状态
     *
     * @return 数据库连接池状态
     */
    public String getDbPoolStatus() {
        return dbPoolStatus;
    }

    /**
     * 设置数据库连接池状态
     *
     * @param dbPoolStatus 数据库连接池状态
     */
    public void setDbPoolStatus(String dbPoolStatus) {
        this.dbPoolStatus = dbPoolStatus;
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
    public String getJvmInfo() {
        return jvmInfo;
    }

    /**
     * 设置JVM信息
     *
     * @param jvmInfo JVM信息
     */
    public void setJvmInfo(String jvmInfo) {
        this.jvmInfo = jvmInfo;
    }

    /**
     * 获取系统负载
     *
     * @return 系统负载
     */
    public Double getSystemLoad() {
        return systemLoad;
    }

    /**
     * 设置系统负载
     *
     * @param systemLoad 系统负载
     */
    public void setSystemLoad(Double systemLoad) {
        this.systemLoad = systemLoad;
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
     * @return 平均响应时间（毫秒）
     */
    public Double getAvgResponseTime() {
        return avgResponseTime;
    }

    /**
     * 设置平均响应时间
     *
     * @param avgResponseTime 平均响应时间（毫秒）
     */
    public void setAvgResponseTime(Double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
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
     * 获取检查时间
     *
     * @return 检查时间
     */
    public LocalDateTime getCheckTime() {
        return checkTime;
    }

    /**
     * 设置检查时间
     *
     * @param checkTime 检查时间
     */
    public void setCheckTime(LocalDateTime checkTime) {
        this.checkTime = checkTime;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断系统是否正在运行
     *
     * @return true-运行中，false-其他状态
     */
    public boolean isRunning() {
        return "RUNNING".equalsIgnoreCase(this.status);
    }

    /**
     * 判断系统是否健康
     *
     * @return true-健康，false-不健康
     */
    public boolean isHealthy() {
        return "HEALTHY".equalsIgnoreCase(this.healthStatus);
    }

    /**
     * 判断系统是否降级
     *
     * @return true-降级，false-正常
     */
    public boolean isDegraded() {
        return "DEGRADED".equalsIgnoreCase(this.healthStatus);
    }

    /**
     * 判断CPU使用率是否过高
     *
     * @param threshold 阈值（百分比）
     * @return true-过高，false-正常
     */
    public boolean isCpuUsageHigh(double threshold) {
        return this.cpuUsage != null && this.cpuUsage > threshold;
    }

    /**
     * 判断内存使用率是否过高
     *
     * @param threshold 阈值（百分比）
     * @return true-过高，false-正常
     */
    public boolean isMemoryUsageHigh(double threshold) {
        return this.memoryUsage != null && this.memoryUsage > threshold;
    }

    /**
     * 判断磁盘使用率是否过高
     *
     * @param threshold 阈值（百分比）
     * @return true-过高，false-正常
     */
    public boolean isDiskUsageHigh(double threshold) {
        return this.diskUsage != null && this.diskUsage > threshold;
    }

    /**
     * 判断响应时间是否过长
     *
     * @param threshold 阈值（毫秒）
     * @return true-过长，false-正常
     */
    public boolean isResponseTimeSlow(double threshold) {
        return this.avgResponseTime != null && this.avgResponseTime > threshold;
    }

    /**
     * 判断心跳是否超时
     *
     * @param timeoutMinutes 超时时间（分钟）
     * @return true-超时，false-正常
     */
    public boolean isHeartbeatTimeout(long timeoutMinutes) {
        if (this.lastHeartbeat == null) {
            return true;
        }
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(timeoutMinutes);
        return this.lastHeartbeat.isBefore(timeout);
    }

    /**
     * 更新心跳时间
     */
    public void updateHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
        this.checkTime = LocalDateTime.now();
    }

    /**
     * 增加错误计数
     */
    public void incrementErrorCount() {
        if (this.errorCount == null) {
            this.errorCount = 0L;
        }
        this.errorCount++;
    }

    /**
     * 增加警告计数
     */
    public void incrementWarningCount() {
        if (this.warningCount == null) {
            this.warningCount = 0L;
        }
        this.warningCount++;
    }

    /**
     * 增加请求计数
     */
    public void incrementRequestCount() {
        if (this.requestCount == null) {
            this.requestCount = 0L;
        }
        this.requestCount++;
    }

    /**
     * 计算运行时长
     *
     * @return 运行时长（秒）
     */
    public long calculateUptime() {
        if (this.startupTime == null) {
            return 0L;
        }
        return java.time.Duration.between(this.startupTime, LocalDateTime.now()).getSeconds();
    }

    /**
     * 设置系统为运行状态
     */
    public void setRunning() {
        this.status = "RUNNING";
        this.healthStatus = "HEALTHY";
        if (this.startupTime == null) {
            this.startupTime = LocalDateTime.now();
        }
        updateHeartbeat();
    }

    /**
     * 设置系统为停止状态
     */
    public void setStopped() {
        this.status = "STOPPED";
        this.healthStatus = "UNHEALTHY";
        updateHeartbeat();
    }

    /**
     * 设置系统为错误状态
     *
     * @param errorDescription 错误描述
     */
    public void setError(String errorDescription) {
        this.status = "ERROR";
        this.healthStatus = "UNHEALTHY";
        this.statusDescription = errorDescription;
        incrementErrorCount();
        updateHeartbeat();
    }

    /**
     * 获取系统状态摘要
     *
     * @return 状态摘要
     */
    public String getStatusSummary() {
        return String.format("System: %s, Status: %s, Health: %s, CPU: %.1f%%, Memory: %.1f%%, Uptime: %ds",
                            this.systemName,
                            this.status,
                            this.healthStatus,
                            this.cpuUsage != null ? this.cpuUsage : 0.0,
                            this.memoryUsage != null ? this.memoryUsage : 0.0,
                            this.uptimeSeconds != null ? this.uptimeSeconds : 0L);
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写equals方法
     * 基于系统名称和检查时间进行比较
     *
     * @param obj 比较对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        SystemStatus that = (SystemStatus) obj;
        return Objects.equals(systemName, that.systemName) &&
               Objects.equals(checkTime, that.checkTime);
    }

    /**
     * 重写hashCode方法
     * 基于系统名称和检查时间生成哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), systemName, checkTime);
    }

    /**
     * 重写toString方法
     * 提供系统状态实体的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "SystemStatus{" +
                "id=" + getId() +
                ", systemName='" + systemName + '\'' +
                ", systemVersion='" + systemVersion + '\'' +
                ", status='" + status + '\'' +
                ", healthStatus='" + healthStatus + '\'' +
                ", startupTime=" + startupTime +
                ", lastHeartbeat=" + lastHeartbeat +
                ", uptimeSeconds=" + uptimeSeconds +
                ", cpuUsage=" + cpuUsage +
                ", memoryUsage=" + memoryUsage +
                ", diskUsage=" + diskUsage +
                ", activeConnections=" + activeConnections +
                ", activeThreads=" + activeThreads +
                ", errorCount=" + errorCount +
                ", warningCount=" + warningCount +
                ", requestCount=" + requestCount +
                ", avgResponseTime=" + avgResponseTime +
                ", checkTime=" + checkTime +
                '}';
    }
}


