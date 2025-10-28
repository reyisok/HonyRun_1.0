package com.honyrun.model.dto.response;

import com.honyrun.model.enums.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 认证响应DTO
 *
 * 用于返回用户认证结果的响应对象，包含JWT令牌、用户信息和权限信息
 * 支持响应式数据传输和JSON序列化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  21:35:00
 * @modified 2025-07-01 21:35:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    /**
     * 访问令牌
     */
    @JsonProperty("accessToken")
    private String accessToken;

    /**
     * 令牌类型
     */
    @JsonProperty("tokenType")
    private String tokenType = "Bearer";

    /**
     * 令牌过期时间（秒）
     */
    @JsonProperty("expiresIn")
    private Long expiresIn;

    /**
     * 刷新令牌
     */
    @JsonProperty("refreshToken")
    private String refreshToken;

    /**
     * 用户信息
     */
    @JsonProperty("user")
    private UserInfo userInfo;

    /**
     * 权限信息
     */
    @JsonProperty("permissions")
    private List<String> permissions;

    /**
     * 认证时间
     */
    @JsonProperty("authTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime authTime;

    /**
     * 令牌过期时间
     */
    @JsonProperty("expireTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 设备ID
     */
    @JsonProperty("deviceId")
    private String deviceId;

    /**
     * 活动ID
     */
    @JsonProperty("activityId")
    private String activityId;

    /**
     * 默认构造函数
     */
    public AuthResponse() {
        this.authTime = LocalDateTime.now();
    }

    /**
     * 构造函数
     *
     * @param accessToken 访问令牌
     * @param expiresIn 过期时间（秒）
     * @param userInfo 用户信息
     */
    public AuthResponse(String accessToken, Long expiresIn, UserInfo userInfo) {
        this();
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
        this.expireTime = LocalDateTime.now().plusSeconds(expiresIn);
    }

    /**
     * 完整构造函数
     *
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @param expiresIn 过期时间（秒）
     * @param userInfo 用户信息
     * @param permissions 权限列表
     * @param deviceId 设备ID
     * @param activityId 活动ID
     */
    public AuthResponse(String accessToken, String refreshToken, Long expiresIn,
                       UserInfo userInfo, List<String> permissions,
                       String deviceId, String activityId) {
        this();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
        this.permissions = permissions;
        this.deviceId = deviceId;
        this.activityId = activityId;
        this.expireTime = LocalDateTime.now().plusSeconds(expiresIn);
    }

    // Getter和Setter方法

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
        if (expiresIn != null) {
            this.expireTime = LocalDateTime.now().plusSeconds(expiresIn);
        }
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public LocalDateTime getAuthTime() {
        return authTime;
    }

    public void setAuthTime(LocalDateTime authTime) {
        this.authTime = authTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    /**
     * 检查令牌是否即将过期（30分钟内）
     *
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon() {
        if (expireTime == null) {
            return false;
        }
        return expireTime.isBefore(LocalDateTime.now().plusMinutes(30));
    }

    /**
     * 检查令牌是否已过期
     *
     * @return 是否已过期
     */
    public boolean isTokenExpired() {
        if (expireTime == null) {
            return false;
        }
        return expireTime.isBefore(LocalDateTime.now());
    }

    /**
     * 获取剩余有效时间（秒）
     *
     * @return 剩余有效时间
     */
    public long getRemainingSeconds() {
        if (expireTime == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (expireTime.isBefore(now)) {
            return 0;
        }
        return java.time.Duration.between(now, expireTime).getSeconds();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthResponse that = (AuthResponse) o;
        return Objects.equals(accessToken, that.accessToken) &&
                Objects.equals(tokenType, that.tokenType) &&
                Objects.equals(expiresIn, that.expiresIn) &&
                Objects.equals(refreshToken, that.refreshToken) &&
                Objects.equals(userInfo, that.userInfo) &&
                Objects.equals(permissions, that.permissions) &&
                Objects.equals(deviceId, that.deviceId) &&
                Objects.equals(activityId, that.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, tokenType, expiresIn, refreshToken,
                          userInfo, permissions, deviceId, activityId);
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", userInfo=" + userInfo +
                ", permissions=" + permissions +
                ", authTime=" + authTime +
                ", expireTime=" + expireTime +
                ", deviceId='" + deviceId + '\'' +
                ", activityId='" + activityId + '\'' +
                '}';
    }

    /**
     * 用户信息内部类
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {

        /**
         * 用户ID
         */
        @JsonProperty("id")
        @JsonInclude(JsonInclude.Include.ALWAYS)
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
        private UserType userType;

        /**
         * 用户类型代码
         */
        @JsonProperty("userTypeCode")
        private String userTypeCode;

        /**
         * 用户类型名称
         */
        @JsonProperty("userTypeName")
        private String userTypeName;

        /**
         * 真实姓名
         */
        @JsonProperty("realName")
        private String realName;

        /**
         * 邮箱地址
         */
        @JsonProperty("email")
        private String email;

        /**
         * 手机号码（脱敏处理）
         */
        @JsonProperty("phone")
        private String phone;

        /**
         * 账户状态 - 使用Integer类型便于前端处理和显示
         * 1: 正常(ACTIVE), 0: 禁用(DISABLED/LOCKED/INACTIVE)
         */
        @JsonProperty("status")
        private Integer status;

        /**
         * 账户状态描述
         */
        @JsonProperty("statusDescription")
        private String statusDescription;

        /**
         * 是否为系统用户
         */
        @JsonProperty("isSystemUser")
        private Boolean isSystemUser;

        /**
         * 是否可用
         */
        @JsonProperty("isAvailable")
        private Boolean isAvailable;

        /**
         * 最后登录时间
         */
        @JsonProperty("lastLoginTime")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginTime;

        /**
         * 创建时间
         */
        @JsonProperty("createdAt")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        /**
         * 权限列表
         */
        @JsonProperty("permissions")
        private java.util.List<String> permissions;

        /**
         * 默认构造函数
         */
        public UserInfo() {
        }

        /**
         * 构造函数
         *
         * @param userId 用户ID
         * @param username 用户名
         * @param userType 用户类型
         * @param realName 真实姓名
         */
        public UserInfo(Long userId, String username, UserType userType, String realName) {
            this.userId = userId;
            this.username = username;
            this.userType = userType;
            this.realName = realName;
            this.userTypeCode = userType != null ? userType.getCode() : null;
            this.userTypeName = userType != null ? userType.getDescription() : null;
            this.isSystemUser = userType == UserType.SYSTEM_USER;
        }

        // Getter和Setter方法

        @JsonProperty("id")
        @JsonInclude(JsonInclude.Include.ALWAYS)
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

        public UserType getUserType() {
            return userType;
        }

        public void setUserType(UserType userType) {
            this.userType = userType;
            this.userTypeCode = userType != null ? userType.getCode() : null;
            this.userTypeName = userType != null ? userType.getDescription() : null;
            this.isSystemUser = userType == UserType.SYSTEM_USER;
        }

        public String getUserTypeCode() {
            return userTypeCode;
        }

        public void setUserTypeCode(String userTypeCode) {
            this.userTypeCode = userTypeCode;
        }

        public String getUserTypeName() {
            return userTypeName;
        }

        public void setUserTypeName(String userTypeName) {
            this.userTypeName = userTypeName;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getStatusDescription() {
            return statusDescription;
        }

        public void setStatusDescription(String statusDescription) {
            this.statusDescription = statusDescription;
        }

        public Boolean getIsSystemUser() {
            return isSystemUser;
        }

        public void setIsSystemUser(Boolean isSystemUser) {
            this.isSystemUser = isSystemUser;
        }

        public Boolean getIsAvailable() {
            return isAvailable;
        }

        public void setIsAvailable(Boolean isAvailable) {
            this.isAvailable = isAvailable;
        }

        public LocalDateTime getLastLoginTime() {
            return lastLoginTime;
        }

        public void setLastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public java.util.List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(java.util.List<String> permissions) {
            this.permissions = permissions;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserInfo userInfo = (UserInfo) o;
            return Objects.equals(userId, userInfo.userId) &&
                    Objects.equals(username, userInfo.username) &&
                    userType == userInfo.userType &&
                    Objects.equals(realName, userInfo.realName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, username, userType, realName);
        }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "userId=" + userId +
                    ", username='" + username + '\'' +
                    ", userType=" + userType +
                    ", userTypeCode='" + userTypeCode + '\'' +
                    ", userTypeName='" + userTypeName + '\'' +
                    ", realName='" + realName + '\'' +
                    ", email='" + email + '\'' +
                    ", status=" + status +
                    ", statusDescription='" + statusDescription + '\'' +
                    ", isSystemUser=" + isSystemUser +
                    ", isAvailable=" + isAvailable +
                    ", lastLoginTime=" + lastLoginTime +
                    '}';
        }
    }
}


