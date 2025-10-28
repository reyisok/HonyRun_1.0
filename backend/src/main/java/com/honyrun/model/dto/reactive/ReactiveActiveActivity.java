package com.honyrun.model.dto.reactive;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 响应式活跃活动数据传输对象
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  20:45:00
 * @modified 2025-07-01 20:45:00
 *
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 用于表示用户活跃活动状态的数据传输对象
 * 包含活动状态数据、用户信息、时间戳等信息
 */
public class ReactiveActiveActivity {

    /**
     * 活动ID
     */
    @JsonProperty("activityId")
    private String activityId;

    /**
     * 用户ID
     */
    @JsonProperty("userId")
    private Long userId;

    /**
     * 用户名
     */
    @JsonProperty("username")
    private String username;

    /**
     * 用户类型
     */
    @JsonProperty("userType")
    private String userType;

    /**
     * JWT令牌ID
     */
    @JsonProperty("tokenId")
    private String tokenId;

    /**
     * 客户端IP地址
     */
    @JsonProperty("clientIp")
    private String clientIp;

    /**
     * 用户代理信息
     */
    @JsonProperty("userAgent")
    private String userAgent;

    /**
     * 登录时间
     */
    @JsonProperty("loginTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    /**
     * 最后活动时间
     */
    @JsonProperty("lastActivityTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActivityTime;

    /**
     * 活动状态
     */
    @JsonProperty("activityStatus")
    private String activityStatus;

    /**
     * 设备类型
     */
    @JsonProperty("deviceType")
    private String deviceType;

    /**
     * 地理位置信息
     */
    @JsonProperty("location")
    private String location;

    /**
     * 活动持续时间（秒）
     */
    @JsonProperty("durationSeconds")
    private Long durationSeconds;

    /**
     * 请求计数
     */
    @JsonProperty("requestCount")
    private Long requestCount;

    /**
     * 是否强制下线
     */
    @JsonProperty("forceLogout")
    private Boolean forceLogout;

    /**
     * 下线原因
     */
    @JsonProperty("logoutReason")
    private String logoutReason;

    /**
     * 创建时间
     */
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 默认构造函数
     */
    public ReactiveActiveActivity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.forceLogout = false;
        this.requestCount = 0L;
        this.durationSeconds = 0L;
    }

    /**
     * 构造函数
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param activityId 活动ID
     * @param tokenId 令牌ID
     * @param clientIp 客户端IP
     */
    public ReactiveActiveActivity(Long userId, String username, String activityId, String tokenId, String clientIp) {
        this();
        this.userId = userId;
        this.username = username;
        this.activityId = activityId;
        this.tokenId = tokenId;
        this.clientIp = clientIp;
        this.loginTime = LocalDateTime.now();
        this.lastActivityTime = LocalDateTime.now();
        this.activityStatus = "ACTIVE";

    }

    /**
     * 更新最后活动时间
     */
    public void updateLastActivity() {
        this.lastActivityTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.requestCount == null) {
            this.requestCount = 0L;
        }
        this.requestCount++;

        // 计算活动持续时间
        if (this.loginTime != null) {
            this.durationSeconds = java.time.Duration.between(this.loginTime, this.lastActivityTime).getSeconds();
        }
    }

    /**
     * 标记为强制下线
     *
     * @param reason 下线原因
     */
    public void markForceLogout(String reason) {
        this.forceLogout = true;
        this.logoutReason = reason;
        this.activityStatus = "FORCE_LOGOUT";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记为正常下线
     */
    public void markNormalLogout() {
        this.activityStatus = "LOGOUT";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查活动是否过期
     *
     * @param timeoutMinutes 超时时间（分钟）
     * @return 是否过期
     */
    public boolean isExpired(int timeoutMinutes) {
        if (this.lastActivityTime == null) {
            return true;
        }
        LocalDateTime expireTime = this.lastActivityTime.plusMinutes(timeoutMinutes);
        return LocalDateTime.now().isAfter(expireTime);
    }

    // Getter和Setter方法

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(LocalDateTime lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public String getActivityStatus() {
        return activityStatus;
    }

    public void setActivityStatus(String activityStatus) {
        this.activityStatus = activityStatus;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }

    public Boolean getForceLogout() {
        return forceLogout;
    }

    public void setForceLogout(Boolean forceLogout) {
        this.forceLogout = forceLogout;
    }

    public String getLogoutReason() {
        return logoutReason;
    }

    public void setLogoutReason(String logoutReason) {
        this.logoutReason = logoutReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReactiveActiveActivity that = (ReactiveActiveActivity) o;
        return Objects.equals(activityId, that.activityId) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(activityId, that.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, userId, activityId);
    }

    @Override
    public String toString() {
        return "ReactiveActiveActivity{" +
                "activityId='" + activityId + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", userType='" + userType + '\'' +
                ", activityId='" + activityId + '\'' +
                ", tokenId='" + (tokenId != null ? tokenId.substring(0, Math.min(8, tokenId.length())) + "***" : null) + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", loginTime=" + loginTime +
                ", lastActivityTime=" + lastActivityTime +
                ", activityStatus='" + activityStatus + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", durationSeconds=" + durationSeconds +
                ", requestCount=" + requestCount +
                ", forceLogout=" + forceLogout +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    /**
     * 创建Builder实例
     *
     * @return Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder内部类
     */
    public static class Builder {
        private String activityId;
        private Long userId;
        private String username;
        private String userType;
        private String tokenId;
        private String clientIp;
        private String userAgent;
        private LocalDateTime loginTime;
        private LocalDateTime lastActivityTime;
        private String activityStatus;
        private String deviceType;
        private String location;
        private Long durationSeconds;
        private Long requestCount;
        private Boolean forceLogout;
        private String logoutReason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            // 为了兼容性，id方法设置userId
            this.userId = id;
            return this;
        }

        public Builder activityId(String activityId) {
            this.activityId = activityId;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder userType(String userType) {
            this.userType = userType;
            return this;
        }

        public Builder tokenId(String tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder loginTime(LocalDateTime loginTime) {
            this.loginTime = loginTime;
            return this;
        }

        public Builder lastActivityTime(LocalDateTime lastActivityTime) {
            this.lastActivityTime = lastActivityTime;
            return this;
        }

        public Builder activityTime(LocalDateTime activityTime) {
            // 为了兼容性，activityTime方法设置lastActivityTime
            this.lastActivityTime = activityTime;
            return this;
        }

        public Builder activityStatus(String activityStatus) {
            this.activityStatus = activityStatus;
            return this;
        }

        public Builder deviceType(String deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder durationSeconds(Long durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public Builder requestCount(Long requestCount) {
            this.requestCount = requestCount;
            return this;
        }

        public Builder forceLogout(Boolean forceLogout) {
            this.forceLogout = forceLogout;
            return this;
        }

        public Builder logoutReason(String logoutReason) {
            this.logoutReason = logoutReason;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ReactiveActiveActivity build() {
            ReactiveActiveActivity activity = new ReactiveActiveActivity();
            activity.activityId = this.activityId;
            activity.userId = this.userId;
            activity.username = this.username;
            activity.userType = this.userType;
            activity.tokenId = this.tokenId;
            activity.clientIp = this.clientIp;
            activity.userAgent = this.userAgent;
            activity.loginTime = this.loginTime;
            activity.lastActivityTime = this.lastActivityTime;
            activity.activityStatus = this.activityStatus;
            activity.deviceType = this.deviceType;
            activity.location = this.location;
            activity.durationSeconds = this.durationSeconds;
            activity.requestCount = this.requestCount;
            activity.forceLogout = this.forceLogout;
            activity.logoutReason = this.logoutReason;
            activity.createdAt = this.createdAt;
            activity.updatedAt = this.updatedAt;
            return activity;
        }
    }
}


