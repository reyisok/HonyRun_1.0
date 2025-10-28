package com.honyrun.model.entity.system;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 并发用户统计实体类
 *
 * 并发用户统计管理实体，支持用户并发数、统计时间、峰值记录等功能
 * 继承AuditableEntity，支持R2DBC响应式数据访问和审计功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  19:10:00
 * @modified 2025-07-01 19:10:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("sys_concurrent_user_stats")
public class ConcurrentUserStats extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 统计时间
     * 统计数据的时间点
     */
    @Column("stat_time")
    private LocalDateTime statTime;

    /**
     * 当前在线用户数
     * 统计时刻的在线用户数量
     */
    @Column("current_online_users")
    private Integer currentOnlineUsers;

    /**
     * 当前活跃用户数
     * 统计时刻的活跃用户数量（最近5分钟内有操作）
     */
    @Column("current_active_users")
    private Integer currentActiveUsers;

    /**
     * 峰值在线用户数
     * 统计周期内的最大在线用户数
     */
    @Column("peak_online_users")
    private Integer peakOnlineUsers;

    /**
     * 峰值活跃用户数
     * 统计周期内的最大活跃用户数
     */
    @Column("peak_active_users")
    private Integer peakActiveUsers;

    /**
     * 峰值时间
     * 达到峰值的时间
     */
    @Column("peak_time")
    private LocalDateTime peakTime;

    /**
     * 平均在线用户数
     * 统计周期内的平均在线用户数
     */
    @Column("avg_online_users")
    private Double avgOnlineUsers;

    /**
     * 平均活跃用户数
     * 统计周期内的平均活跃用户数
     */
    @Column("avg_active_users")
    private Double avgActiveUsers;

    /**
     * 新增用户数
     * 统计周期内的新增用户数量
     */
    @Column("new_users")
    private Integer newUsers;

    /**
     * 登录次数
     * 统计周期内的总登录次数
     */
    @Column("login_count")
    private Integer loginCount;

    /**
     * 登出次数
     * 统计周期内的总登出次数
     */
    @Column("logout_count")
    private Integer logoutCount;

    /**
     * 会话总数
     * 统计周期内的会话总数
     */
    @Column("session_count")
    private Integer sessionCount;

    /**
     * 平均会话时长
     * 统计周期内的平均会话时长（分钟）
     */
    @Column("avg_session_duration")
    private Double avgSessionDuration;

    /**
     * 统计周期类型
     * MINUTE-分钟，HOUR-小时，DAY-天，WEEK-周，MONTH-月
     */
    @Column("period_type")
    private String periodType;

    /**
     * 统计周期开始时间
     * 统计周期的开始时间
     */
    @Column("period_start")
    private LocalDateTime periodStart;

    /**
     * 统计周期结束时间
     * 统计周期的结束时间
     */
    @Column("period_end")
    private LocalDateTime periodEnd;

    /**
     * 用户类型分布
     * 不同用户类型的分布统计（JSON格式）
     */
    @Column("user_type_distribution")
    private String userTypeDistribution;

    /**
     * 地域分布
     * 用户地域分布统计（JSON格式）
     */
    @Column("region_distribution")
    private String regionDistribution;

    /**
     * 设备类型分布
     * 用户设备类型分布统计（JSON格式）
     */
    @Column("device_distribution")
    private String deviceDistribution;

    /**
     * 浏览器分布
     * 用户浏览器分布统计（JSON格式）
     */
    @Column("browser_distribution")
    private String browserDistribution;

    /**
     * 操作系统分布
     * 用户操作系统分布统计（JSON格式）
     */
    @Column("os_distribution")
    private String osDistribution;

    /**
     * 页面访问统计
     * 页面访问量统计（JSON格式）
     */
    @Column("page_view_stats")
    private String pageViewStats;

    /**
     * 功能使用统计
     * 功能模块使用统计（JSON格式）
     */
    @Column("feature_usage_stats")
    private String featureUsageStats;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public ConcurrentUserStats() {
        super();
        this.statTime = LocalDateTime.now();
        this.currentOnlineUsers = 0;
        this.currentActiveUsers = 0;
        this.peakOnlineUsers = 0;
        this.peakActiveUsers = 0;
        this.newUsers = 0;
        this.loginCount = 0;
        this.logoutCount = 0;
        this.sessionCount = 0;
    }

    /**
     * 带参数的构造函数
     *
     * @param periodType 统计周期类型
     * @param periodStart 统计周期开始时间
     * @param periodEnd 统计周期结束时间
     */
    public ConcurrentUserStats(String periodType, LocalDateTime periodStart, LocalDateTime periodEnd) {
        this();
        this.periodType = periodType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    // ==================== Getter和Setter方法 ====================

    public LocalDateTime getStatTime() {
        return statTime;
    }

    public void setStatTime(LocalDateTime statTime) {
        this.statTime = statTime;
    }

    public Integer getCurrentOnlineUsers() {
        return currentOnlineUsers;
    }

    public void setCurrentOnlineUsers(Integer currentOnlineUsers) {
        this.currentOnlineUsers = currentOnlineUsers;
    }

    public Integer getCurrentActiveUsers() {
        return currentActiveUsers;
    }

    public void setCurrentActiveUsers(Integer currentActiveUsers) {
        this.currentActiveUsers = currentActiveUsers;
    }

    public Integer getPeakOnlineUsers() {
        return peakOnlineUsers;
    }

    public void setPeakOnlineUsers(Integer peakOnlineUsers) {
        this.peakOnlineUsers = peakOnlineUsers;
    }

    public Integer getPeakActiveUsers() {
        return peakActiveUsers;
    }

    public void setPeakActiveUsers(Integer peakActiveUsers) {
        this.peakActiveUsers = peakActiveUsers;
    }

    public LocalDateTime getPeakTime() {
        return peakTime;
    }

    public void setPeakTime(LocalDateTime peakTime) {
        this.peakTime = peakTime;
    }

    public Double getAvgOnlineUsers() {
        return avgOnlineUsers;
    }

    public void setAvgOnlineUsers(Double avgOnlineUsers) {
        this.avgOnlineUsers = avgOnlineUsers;
    }

    public Double getAvgActiveUsers() {
        return avgActiveUsers;
    }

    public void setAvgActiveUsers(Double avgActiveUsers) {
        this.avgActiveUsers = avgActiveUsers;
    }

    public Integer getNewUsers() {
        return newUsers;
    }

    public void setNewUsers(Integer newUsers) {
        this.newUsers = newUsers;
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public Integer getLogoutCount() {
        return logoutCount;
    }

    public void setLogoutCount(Integer logoutCount) {
        this.logoutCount = logoutCount;
    }

    public Integer getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(Integer sessionCount) {
        this.sessionCount = sessionCount;
    }

    public Double getAvgSessionDuration() {
        return avgSessionDuration;
    }

    public void setAvgSessionDuration(Double avgSessionDuration) {
        this.avgSessionDuration = avgSessionDuration;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public String getUserTypeDistribution() {
        return userTypeDistribution;
    }

    public void setUserTypeDistribution(String userTypeDistribution) {
        this.userTypeDistribution = userTypeDistribution;
    }

    public String getRegionDistribution() {
        return regionDistribution;
    }

    public void setRegionDistribution(String regionDistribution) {
        this.regionDistribution = regionDistribution;
    }

    public String getDeviceDistribution() {
        return deviceDistribution;
    }

    public void setDeviceDistribution(String deviceDistribution) {
        this.deviceDistribution = deviceDistribution;
    }

    public String getBrowserDistribution() {
        return browserDistribution;
    }

    public void setBrowserDistribution(String browserDistribution) {
        this.browserDistribution = browserDistribution;
    }

    public String getOsDistribution() {
        return osDistribution;
    }

    public void setOsDistribution(String osDistribution) {
        this.osDistribution = osDistribution;
    }

    public String getPageViewStats() {
        return pageViewStats;
    }

    public void setPageViewStats(String pageViewStats) {
        this.pageViewStats = pageViewStats;
    }

    public String getFeatureUsageStats() {
        return featureUsageStats;
    }

    public void setFeatureUsageStats(String featureUsageStats) {
        this.featureUsageStats = featureUsageStats;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断是否为实时统计
     */
    public boolean isRealTimeStats() {
        return "MINUTE".equalsIgnoreCase(this.periodType);
    }

    /**
     * 判断是否为日统计
     */
    public boolean isDailyStats() {
        return "DAY".equalsIgnoreCase(this.periodType);
    }

    /**
     * 更新峰值数据
     */
    public void updatePeakValues(Integer onlineUsers, Integer activeUsers) {
        if (onlineUsers != null && (this.peakOnlineUsers == null || onlineUsers > this.peakOnlineUsers)) {
            this.peakOnlineUsers = onlineUsers;
            this.peakTime = LocalDateTime.now();
        }
        if (activeUsers != null && (this.peakActiveUsers == null || activeUsers > this.peakActiveUsers)) {
            this.peakActiveUsers = activeUsers;
            if (this.peakTime == null) {
                this.peakTime = LocalDateTime.now();
            }
        }
    }

    /**
     * 增加登录次数
     */
    public void incrementLoginCount() {
        if (this.loginCount == null) {
            this.loginCount = 0;
        }
        this.loginCount++;
    }

    /**
     * 增加登出次数
     */
    public void incrementLogoutCount() {
        if (this.logoutCount == null) {
            this.logoutCount = 0;
        }
        this.logoutCount++;
    }

    /**
     * 增加新用户数
     */
    public void incrementNewUsers() {
        if (this.newUsers == null) {
            this.newUsers = 0;
        }
        this.newUsers++;
    }

    /**
     * 增加会话数
     */
    public void incrementSessionCount() {
        if (this.sessionCount == null) {
            this.sessionCount = 0;
        }
        this.sessionCount++;
    }

    /**
     * 计算活跃率
     */
    public double getActiveRate() {
        if (this.currentOnlineUsers == null || this.currentOnlineUsers == 0) {
            return 0.0;
        }
        int active = this.currentActiveUsers != null ? this.currentActiveUsers : 0;
        return (double) active / this.currentOnlineUsers * 100;
    }

    /**
     * 计算峰值利用率
     */
    public double getPeakUtilizationRate() {
        if (this.peakOnlineUsers == null || this.peakOnlineUsers == 0) {
            return 0.0;
        }
        double avg = this.avgOnlineUsers != null ? this.avgOnlineUsers : 0.0;
        return avg / this.peakOnlineUsers * 100;
    }

    /**
     * 获取统计摘要
     */
    public String getStatsSummary() {
        return String.format("Period: %s, Online: %d (Peak: %d), Active: %d (Peak: %d), Sessions: %d",
                            this.periodType,
                            this.currentOnlineUsers != null ? this.currentOnlineUsers : 0,
                            this.peakOnlineUsers != null ? this.peakOnlineUsers : 0,
                            this.currentActiveUsers != null ? this.currentActiveUsers : 0,
                            this.peakActiveUsers != null ? this.peakActiveUsers : 0,
                            this.sessionCount != null ? this.sessionCount : 0);
    }

    /**
     * 判断是否为高峰期
     */
    public boolean isPeakPeriod(double threshold) {
        if (this.currentOnlineUsers == null || this.peakOnlineUsers == null || this.peakOnlineUsers == 0) {
            return false;
        }
        double rate = (double) this.currentOnlineUsers / this.peakOnlineUsers;
        return rate >= threshold;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        ConcurrentUserStats that = (ConcurrentUserStats) obj;
        return Objects.equals(statTime, that.statTime) &&
               Objects.equals(periodType, that.periodType) &&
               Objects.equals(periodStart, that.periodStart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), statTime, periodType, periodStart);
    }

    @Override
    public String toString() {
        return "ConcurrentUserStats{" +
                "id=" + getId() +
                ", statTime=" + statTime +
                ", currentOnlineUsers=" + currentOnlineUsers +
                ", currentActiveUsers=" + currentActiveUsers +
                ", peakOnlineUsers=" + peakOnlineUsers +
                ", peakActiveUsers=" + peakActiveUsers +
                ", peakTime=" + peakTime +
                ", periodType='" + periodType + '\'' +
                ", periodStart=" + periodStart +
                ", periodEnd=" + periodEnd +
                '}';
    }
}


