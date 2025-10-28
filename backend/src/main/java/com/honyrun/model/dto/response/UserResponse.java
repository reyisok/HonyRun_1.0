package com.honyrun.model.dto.response;

import com.honyrun.model.enums.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 用户响应DTO
 *
 * 用于返回用户信息的响应对象，过滤敏感信息，包含权限信息
 * 支持响应式数据传输和JSON序列化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  17:15:00
 * @modified 2025-07-01 17:15:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户类型
     */
    private UserType userType;

    /**
     * 用户类型代码
     */
    private String userTypeCode;

    /**
     * 用户类型名称
     */
    private String userTypeName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 账户状态
     * ACTIVE-启用，INACTIVE-未激活，DISABLED-禁用，LOCKED-锁定
     */
    /**
     * 用户状态 - 使用String类型以保持与User实体一致
     * ACTIVE: 激活, INACTIVE: 未激活, DISABLED: 禁用, LOCKED: 锁定
     */
    private String status;

    /**
     * 账户状态描述
     */
    private String statusDescription;

    /**
     * 账户有效期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validFrom;

    /**
     * 账户有效期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validTo;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP（脱敏处理）
     */
    private String lastLoginIp;

    /**
     * 登录失败次数
     */
    private Integer loginFailureCount;

    /**
     * 账户是否锁定
     */
    private Boolean locked;

    /**
     * 账户锁定时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockedTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModifiedDate;

    /**
     * 最后修改人ID
     */
    private Long lastModifiedBy;

    /**
     * 最后修改人姓名
     */
    private String lastModifiedByName;

    /**
     * 版本号
     */
    private Long version;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 用户权限列表
     */
    private List<UserPermissionInfo> permissions;

    /**
     * 账户可用性状态
     */
    private Boolean available;

    /**
     * 权限级别
     */
    private Integer permissionLevel;

    /**
     * 是否为系统用户
     */
    private Boolean systemUser;

    /**
     * 是否为普通用户
     */
    private Boolean normalUser;

    /**
     * 是否为访客用户
     */
    private Boolean guest;

    /**
     * 默认构造函数
     */
    public UserResponse() {
    }

    /**
     * 带用户ID的构造函数
     *
     * @param id 用户ID
     */
    public UserResponse(Long id) {
        this.id = id;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置用户ID
     *
     * @param id 用户ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * 设置用户类型
     *
     * @param userType 用户类型
     */
    public void setUserType(UserType userType) {
        this.userType = userType;
        if (userType != null) {
            this.userTypeCode = userType.getCode();
            this.userTypeName = userType.getName();
            this.permissionLevel = userType.getPermissionLevel();
            this.systemUser = userType == UserType.SYSTEM_USER;
            this.normalUser = userType == UserType.NORMAL_USER;
            this.guest = userType == UserType.GUEST;
        }
    }

    /**
     * 获取用户类型代码
     *
     * @return 用户类型代码
     */
    public String getUserTypeCode() {
        return userTypeCode;
    }

    /**
     * 设置用户类型代码
     *
     * @param userTypeCode 用户类型代码
     */
    public void setUserTypeCode(String userTypeCode) {
        this.userTypeCode = userTypeCode;
    }

    /**
     * 获取用户类型名称
     *
     * @return 用户类型名称
     */
    public String getUserTypeName() {
        return userTypeName;
    }

    /**
     * 设置用户类型名称
     *
     * @param userTypeName 用户类型名称
     */
    public void setUserTypeName(String userTypeName) {
        this.userTypeName = userTypeName;
    }

    /**
     * 获取真实姓名
     *
     * @return 真实姓名
     */
    public String getRealName() {
        return realName;
    }

    /**
     * 设置真实姓名
     *
     * @param realName 真实姓名
     */
    public void setRealName(String realName) {
        this.realName = realName;
    }

    /**
     * 获取邮箱地址
     *
     * @return 邮箱地址
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置邮箱地址
     *
     * @param email 邮箱地址
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取手机号码
     *
     * @return 手机号码
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置手机号码
     *
     * @param phone 手机号码
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取账户状态
     *
     * @return 账户状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置账户状态
     *
     * @param status 账户状态：ACTIVE-启用，INACTIVE-未激活，DISABLED-禁用，LOCKED-锁定
     */
    public void setStatus(String status) {
        this.status = status;
        this.statusDescription = getStatusDescription(status);
    }

    /**
     * 获取账户状态描述
     *
     * @return 账户状态描述
     */
    public String getStatusDescription() {
        return statusDescription;
    }

    /**
     * 设置账户状态描述
     *
     * @param statusDescription 账户状态描述
     */
    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    /**
     * 获取账户有效期开始时间
     *
     * @return 账户有效期开始时间
     */
    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    /**
     * 设置账户有效期开始时间
     *
     * @param validFrom 账户有效期开始时间
     */
    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * 获取账户有效期结束时间
     *
     * @return 账户有效期结束时间
     */
    public LocalDate getValidTo() {
        return validTo;
    }

    /**
     * 设置账户有效期结束时间
     *
     * @param validTo 账户有效期结束时间
     */
    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    /**
     * 获取最后登录时间
     *
     * @return 最后登录时间
     */
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    /**
     * 设置最后登录时间
     *
     * @param lastLoginTime 最后登录时间
     */
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    /**
     * 获取最后登录IP
     *
     * @return 最后登录IP
     */
    public String getLastLoginIp() {
        return lastLoginIp;
    }

    /**
     * 设置最后登录IP（自动脱敏处理）
     *
     * @param lastLoginIp 最后登录IP
     */
    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = maskIpAddress(lastLoginIp);
    }

    /**
     * 获取登录失败次数
     *
     * @return 登录失败次数
     */
    public Integer getLoginFailureCount() {
        return loginFailureCount;
    }

    /**
     * 设置登录失败次数
     *
     * @param loginFailureCount 登录失败次数
     */
    public void setLoginFailureCount(Integer loginFailureCount) {
        this.loginFailureCount = loginFailureCount;
    }

    /**
     * 获取账户是否锁定
     *
     * @return 账户是否锁定
     */
    public Boolean getLocked() {
        return locked;
    }

    /**
     * 设置账户是否锁定
     *
     * @param locked 账户是否锁定
     */
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    /**
     * 获取账户锁定时间
     *
     * @return 账户锁定时间
     */
    public LocalDateTime getLockedTime() {
        return lockedTime;
    }

    /**
     * 设置账户锁定时间
     *
     * @param lockedTime 账户锁定时间
     */
    public void setLockedTime(LocalDateTime lockedTime) {
        this.lockedTime = lockedTime;
        this.locked = lockedTime != null;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    /**
     * 设置创建时间
     *
     * @param createdDate 创建时间
     */
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * 获取创建人ID
     *
     * @return 创建人ID
     */
    public Long getCreatedBy() {
        return createdBy;
    }

    /**
     * 设置创建人ID
     *
     * @param createdBy 创建人ID
     */
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * 获取创建人姓名
     *
     * @return 创建人姓名
     */
    public String getCreatedByName() {
        return createdByName;
    }

    /**
     * 设置创建人姓名
     *
     * @param createdByName 创建人姓名
     */
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    /**
     * 获取最后修改时间
     *
     * @return 最后修改时间
     */
    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * 设置最后修改时间
     *
     * @param lastModifiedDate 最后修改时间
     */
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * 获取最后修改人ID
     *
     * @return 最后修改人ID
     */
    public Long getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * 设置最后修改人ID
     *
     * @param lastModifiedBy 最后修改人ID
     */
    public void setLastModifiedBy(Long lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /**
     * 获取最后修改人姓名
     *
     * @return 最后修改人姓名
     */
    public String getLastModifiedByName() {
        return lastModifiedByName;
    }

    /**
     * 设置最后修改人姓名
     *
     * @param lastModifiedByName 最后修改人姓名
     */
    public void setLastModifiedByName(String lastModifiedByName) {
        this.lastModifiedByName = lastModifiedByName;
    }

    /**
     * 获取版本号
     *
     * @return 版本号
     */
    public Long getVersion() {
        return version;
    }

    /**
     * 设置版本号
     *
     * @param version 版本号
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * 获取备注信息
     *
     * @return 备注信息
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置备注信息
     *
     * @param remark 备注信息
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取用户权限列表
     *
     * @return 用户权限列表
     */
    public List<UserPermissionInfo> getPermissions() {
        return permissions;
    }

    /**
     * 设置用户权限列表
     *
     * @param permissions 用户权限列表
     */
    public void setPermissions(List<UserPermissionInfo> permissions) {
        this.permissions = permissions;
    }

    /**
     * 获取权限数量
     *
     * @return 权限数量
     */
    public Integer getPermissionsCount() {
        return permissions != null ? permissions.size() : 0;
    }

    /**
     * 获取账户可用性状态
     *
     * @return 账户可用性状态
     */
    public Boolean getAvailable() {
        return available;
    }

    /**
     * 设置账户可用性状态
     *
     * @param available 账户可用性状态
     */
    public void setAvailable(Boolean available) {
        this.available = available;
    }

    /**
     * 获取权限级别
     *
     * @return 权限级别
     */
    public Integer getPermissionLevel() {
        return permissionLevel;
    }

    /**
     * 设置权限级别
     *
     * @param permissionLevel 权限级别
     */
    public void setPermissionLevel(Integer permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    /**
     * 获取是否为系统用户
     *
     * @return 是否为系统用户
     */
    public Boolean getSystemUser() {
        return systemUser;
    }

    /**
     * 设置是否为系统用户
     *
     * @param systemUser 是否为系统用户
     */
    public void setSystemUser(Boolean systemUser) {
        this.systemUser = systemUser;
    }

    /**
     * 获取是否为普通用户
     *
     * @return 是否为普通用户
     */
    public Boolean getNormalUser() {
        return normalUser;
    }

    /**
     * 设置是否为普通用户
     *
     * @param normalUser 是否为普通用户
     */
    public void setNormalUser(Boolean normalUser) {
        this.normalUser = normalUser;
    }

    /**
     * 获取是否为访客用户
     *
     * @return 是否为访客用户
     */
    public Boolean getGuest() {
        return guest;
    }

    /**
     * 契约字段别名：createdAt
     * 
     * 保持原有 createdDate 字段不变，同时提供 createdAt 序列化键，
     * 以满足合约测试对字段命名的要求。
     */
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getCreatedAt() {
        return this.createdDate;
    }

    /**
     * 设置是否为访客用户
     *
     * @param guest 是否为访客用户
     */
    public void setGuest(Boolean guest) {
        this.guest = guest;
    }

    // ==================== 工具方法 ====================



    /**
     * IP地址脱敏处理
     *
     * @param ip 原始IP地址
     * @return 脱敏后的IP地址
     */
    private String maskIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return ip;
        }

        // IPv4地址脱敏：192.168.***.***
        if (ip.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".***." + "***";
            }
        }

        // IPv6或其他格式，保留前缀
        if (ip.length() > 8) {
            return ip.substring(0, 8) + "***";
        }

        return ip;
    }

    /**
     * 获取状态描述
     *
     * @param status 状态值
     * @return 状态描述
     */
    private String getStatusDescription(String status) {
        if (status == null) {
            return "未知";
        }

        switch (status) {
            case "ACTIVE":
                return "启用";
            case "INACTIVE":
                return "未激活";
            case "DISABLED":
                return "禁用";
            case "LOCKED":
                return "锁定";
            default:
                return "未知";
        }
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 从用户实体创建响应DTO
     *
     * @param user 用户实体
     * @return 用户响应DTO
     */
    public static UserResponse fromEntity(com.honyrun.model.entity.business.User user) {
        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setUserType(user.getUserType());
        response.setRealName(user.getRealName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setStatus(user.getStatus());
        response.setLocked(user.isLocked());
        response.setValidFrom(user.getValidFrom());
        // 数据库表中没有有效期结束时间字段，设置为null
        response.setValidTo(null);
        // 数据库表中没有最后登录时间字段，设置为null
        response.setLastLoginTime(null);
        // 数据库表中没有登录失败次数字段，设置为null
        response.setLoginFailureCount(null);
        response.setCreatedDate(user.getCreatedDate());
        response.setLastModifiedDate(user.getLastModifiedDate());
        response.setVersion(user.getVersion());
        response.setRemark(user.getRemark());

        // 设置计算字段
        response.setAvailable(user.isEnabled() && !user.isLocked());
        response.setSystemUser(user.getUserType() == UserType.SYSTEM_USER);
        response.setNormalUser(user.getUserType() == UserType.NORMAL_USER);
        response.setGuest(user.getUserType() == UserType.GUEST);

        return response;
    }

    /**
     * 转换为用户实体
     *
     * @return 用户实体
     */
    public com.honyrun.model.entity.business.User toEntity() {
        com.honyrun.model.entity.business.User user = new com.honyrun.model.entity.business.User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setUserType(this.userType);
        user.setRealName(this.realName);
        user.setEmail(this.email);
        user.setPhone(this.phone);
        user.setStatus(this.status);
        user.setValidFrom(this.validFrom);
        // user.setValidTo(this.validTo); // validTo字段已移除
        // user.setLastLoginTime(this.lastLoginTime); // lastLoginTime字段已移除
        // user.setLoginFailureCount(this.loginFailureCount); // loginFailureCount字段已移除
        user.setCreatedDate(this.createdDate);
        user.setLastModifiedDate(this.lastModifiedDate);
        user.setVersion(this.version);
        user.setRemark(this.remark);

        return user;
    }

    // ==================== 内部类 ====================

    /**
     * 用户权限信息内部类
     */
    public static class UserPermissionInfo {

        /**
         * 权限ID
         */
        private Long id;

        /**
         * 权限代码
         */
        private String permissionCode;

        /**
         * 权限名称
         */
        private String permissionName;

        /**
         * 权限描述
         */
        private String permissionDescription;

        /**
         * 权限级别
         */
        private Integer permissionLevel;

        /**
         * 权限状态 - 使用Integer类型以保持与权限业务表一致
         * 1: 有效, 0: 无效
         */
        private Integer status;

        /**
         * 是否可用
         */
        private Boolean available;

        /**
         * 授权时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime grantedTime;

        /**
         * 有效期结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime validTo;

        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getPermissionCode() { return permissionCode; }
        public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }

        public String getPermissionName() { return permissionName; }
        public void setPermissionName(String permissionName) { this.permissionName = permissionName; }

        public String getPermissionDescription() { return permissionDescription; }
        public void setPermissionDescription(String permissionDescription) { this.permissionDescription = permissionDescription; }

        public Integer getPermissionLevel() { return permissionLevel; }
        public void setPermissionLevel(Integer permissionLevel) { this.permissionLevel = permissionLevel; }

        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }

        public Boolean getAvailable() { return available; }
        public void setAvailable(Boolean available) { this.available = available; }

        public LocalDateTime getGrantedTime() { return grantedTime; }
        public void setGrantedTime(LocalDateTime grantedTime) { this.grantedTime = grantedTime; }

        public LocalDateTime getValidTo() { return validTo; }
        public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写equals方法
     * 基于用户ID进行比较
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

        UserResponse that = (UserResponse) obj;
        return Objects.equals(this.id, that.id);
    }

    /**
     * 重写hashCode方法
     * 基于用户ID生成哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    /**
     * 重写toString方法
     * 提供响应DTO的字符串表示（不包含敏感信息）
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", userType=" + userType +
                ", realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                ", available=" + available +
                ", locked=" + locked +
                ", systemUser=" + systemUser +
                ", normalUser=" + normalUser +
                ", guest=" + guest +
                ", permissionLevel=" + permissionLevel +
                ", permissionsCount=" + (permissions != null ? permissions.size() : 0) +
                '}';
    }
}


