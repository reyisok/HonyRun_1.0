package com.honyrun.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量用户创建请求DTO
 *
 * 用于批量创建用户的请求参数封装
 * 支持批量创建多个用户，并可以统一设置权限和属性
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 11:30:00
 * @modified 2025-07-01 11:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Schema(description = "批量用户创建请求")
public class BatchUserCreateRequest {

    /**
     * 用户信息列表
     */
    @Schema(description = "用户信息列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<UserCreateInfo> users;

    /**
     * 默认密码 - 从配置文件读取，避免硬编码
     */
    @Schema(description = "默认密码", example = "123")
    private String defaultPassword;

    /**
     * 默认用户类型
     */
    @Schema(description = "默认用户类型", example = "NORMAL_USER")
    private String defaultUserType = "NORMAL_USER";

    /**
     * 默认有效期
     */
    @Schema(description = "默认有效期")
    private LocalDateTime defaultValidUntil;

    /**
     * 默认权限列表
     */
    @Schema(description = "默认权限列表")
    private List<String> defaultPermissions;

    /**
     * 是否发送通知
     */
    @Schema(description = "是否发送通知", example = "false")
    private Boolean sendNotification = false;

    /**
     * 创建模式
     * SKIP_EXISTING - 跳过已存在用户
     * UPDATE_EXISTING - 更新已存在用户
     * FAIL_ON_EXISTING - 遇到已存在用户时失败
     */
    @Schema(description = "创建模式", example = "SKIP_EXISTING")
    private String createMode = "SKIP_EXISTING";

    /**
     * 用户创建信息内部类
     */
    @Schema(description = "用户创建信息")
    public static class UserCreateInfo {
        
        /**
         * 用户名
         */
        @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "user001")
        private String username;

        /**
         * 密码（可选，为空时使用默认密码）
         */
        @Schema(description = "密码", example = "123")
        private String password;

        /**
         * 姓名
         */
        @Schema(description = "姓名", example = "张三")
        private String realName;

        /**
         * 手机号
         */
        @Schema(description = "手机号", example = "13800138000")
        private String phoneNumber;

        /**
         * 邮箱
         */
        @Schema(description = "邮箱", example = "user001@example.com")
        private String email;

        /**
         * 部门
         */
        @Schema(description = "部门", example = "技术部")
        private String department;

        /**
         * 职位
         */
        @Schema(description = "职位", example = "开发工程师")
        private String position;

        /**
         * 用户类型
         */
        @Schema(description = "用户类型", example = "NORMAL_USER")
        private String userType;

        /**
         * 有效期（可选，为空时使用默认有效期）
         */
        @Schema(description = "有效期")
        private LocalDateTime validUntil;

        /**
         * 权限列表（可选，为空时使用默认权限）
         */
        @Schema(description = "权限列表")
        private List<String> permissions;

        /**
         * 备注
         */
        @Schema(description = "备注")
        private String remark;

        // ==================== Getter和Setter方法 ====================

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public LocalDateTime getValidUntil() {
            return validUntil;
        }

        public void setValidUntil(LocalDateTime validUntil) {
            this.validUntil = validUntil;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        @Override
        public String toString() {
            return "UserCreateInfo{" +
                    "username='" + username + '\'' +
                    ", realName='" + realName + '\'' +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", email='" + email + '\'' +
                    ", department='" + department + '\'' +
                    ", position='" + position + '\'' +
                    ", userType='" + userType + '\'' +
                    ", validUntil=" + validUntil +
                    '}';
        }
    }

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public BatchUserCreateRequest() {
    }

    // ==================== Getter和Setter方法 ====================

    public List<UserCreateInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserCreateInfo> users) {
        this.users = users;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getDefaultUserType() {
        return defaultUserType;
    }

    public void setDefaultUserType(String defaultUserType) {
        this.defaultUserType = defaultUserType;
    }

    public LocalDateTime getDefaultValidUntil() {
        return defaultValidUntil;
    }

    public void setDefaultValidUntil(LocalDateTime defaultValidUntil) {
        this.defaultValidUntil = defaultValidUntil;
    }

    public List<String> getDefaultPermissions() {
        return defaultPermissions;
    }

    public void setDefaultPermissions(List<String> defaultPermissions) {
        this.defaultPermissions = defaultPermissions;
    }

    public Boolean getSendNotification() {
        return sendNotification;
    }

    public void setSendNotification(Boolean sendNotification) {
        this.sendNotification = sendNotification;
    }

    public String getCreateMode() {
        return createMode;
    }

    public void setCreateMode(String createMode) {
        this.createMode = createMode;
    }

    // ==================== 业务方法 ====================

    /**
     * 验证请求参数
     *
     * @return 验证结果
     */
    public boolean isValid() {
        if (users == null || users.isEmpty()) {
            return false;
        }
        
        for (UserCreateInfo user : users) {
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 获取用户数量
     *
     * @return 用户数量
     */
    public int getUserCount() {
        return users != null ? users.size() : 0;
    }

    /**
     * 获取有效的用户信息列表
     *
     * @return 有效的用户信息列表
     */
    public List<UserCreateInfo> getValidUsers() {
        if (users == null) {
            return List.of();
        }
        
        return users.stream()
                .filter(user -> user.getUsername() != null && !user.getUsername().trim().isEmpty())
                .toList();
    }

    @Override
    public String toString() {
        return "BatchUserCreateRequest{" +
                "userCount=" + getUserCount() +
                ", defaultPassword='" + (defaultPassword != null ? "***" : null) + '\'' +
                ", defaultUserType='" + defaultUserType + '\'' +
                ", defaultValidUntil=" + defaultValidUntil +
                ", sendNotification=" + sendNotification +
                ", createMode='" + createMode + '\'' +
                '}';
    }
}

