package com.honyrun.model.dto.request;

import com.honyrun.model.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户创建请求DTO
 *
 * 用于接收创建用户的请求参数，包含验证注解和数据校验规则
 * 支持响应式数据绑定和参数验证
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  17:10:00
 * @modified 2025-07-01 17:10:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class UserCreateRequest {

    /**
     * 用户名
     * 必填，长度3-50字符，只能包含字母、数字、下划线
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    /**
     * 密码
     * 必填，长度6-100字符
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100字符之间")
    private String password;

    /**
     * 确认密码
     * 可选，提供时必须与密码一致
     */
    private String confirmPassword;

    /**
     * 用户类型
     * 可选，未提供时在服务层默认赋值为 NORMAL_USER
     */
    private UserType userType;

    /**
     * 真实姓名
     * 可选，长度不超过50字符
     */
    @Size(max = 50, message = "真实姓名长度不能超过50字符")
    private String realName;

    /**
     * 邮箱地址
     * 可选，必须是有效的邮箱格式
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100字符")
    private String email;

    /**
     * 手机号码
     * 可选，必须是有效的手机号格式
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 账户状态
     * 可选，默认为ACTIVE（启用），ACTIVE-启用，INACTIVE-未激活，DISABLED-禁用，LOCKED-锁定
     */
    private String status = "ACTIVE";

    /**
     * 账户有效期开始时间
     * 可选，默认为当前时间
     */
    private LocalDateTime validFrom;

    /**
     * 账户有效期结束时间
     * 可选，null表示永久有效
     */
    private LocalDateTime validTo;

    /**
     * 备注信息
     * 可选，长度不超过500字符
     */
    @Size(max = 500, message = "备注信息长度不能超过500字符")
    private String remark;

    /**
     * 默认构造函数
     */
    public UserCreateRequest() {
        this.validFrom = LocalDateTime.now();
    }

    /**
     * 带用户名的构造函数
     *
     * @param username 用户名
     */
    public UserCreateRequest(String username) {
        this();
        this.username = username;
    }

    // ==================== Getter和Setter方法 ====================

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
     * 获取密码
     *
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取确认密码
     *
     * @return 确认密码
     */
    public String getConfirmPassword() {
        return confirmPassword;
    }

    /**
     * 设置确认密码
     *
     * @param confirmPassword 确认密码
     */
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
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
     * @param status 账户状态：ACTIVE-启用，INACTIVE-未激活，DISABLED-禁用，LOCKED-锁定
     */
    public void setStatus(String status) {
        this.status = status;
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
    public LocalDateTime getValidTo() {
        return validTo;
    }

    /**
     * 设置账户有效期结束时间
     *
     * @param validTo 账户有效期结束时间
     */
    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
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

    // ==================== 验证方法 ====================

    /**
     * 验证密码和确认密码是否一致
     *
     * @return true-一致，false-不一致
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isPasswordMatch() {
        // 当未提供确认密码时不视为错误；提供时需与密码一致
        return this.confirmPassword == null || (this.password != null && this.password.equals(this.confirmPassword));
    }

    /**
     * 验证用户类型是否有效
     *
     * @return true-有效，false-无效
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isValidUserType() {
        return this.userType != null &&
               (this.userType == UserType.SYSTEM_USER ||
                this.userType == UserType.NORMAL_USER ||
                this.userType == UserType.GUEST);
    }

    /**
     * 验证有效期设置是否合理
     *
     * @return true-合理，false-不合理
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isValidDateRange() {
        if (this.validFrom == null) {
            return false;
        }

        if (this.validTo != null && this.validTo.isBefore(this.validFrom)) {
            return false;
        }

        return true;
    }

    /**
     * 验证邮箱和手机号是否至少填写一个
     *
     * @return true-至少填写一个，false-都未填写
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean hasContactInfo() {
        return (this.email != null && !this.email.trim().isEmpty()) ||
               (this.phone != null && !this.phone.trim().isEmpty());
    }

    /**
     * 获取所有验证错误信息
     *
     * @return 验证错误信息列表
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public java.util.List<String> getValidationErrors() {
        java.util.List<String> errors = new java.util.ArrayList<>();

        if (this.confirmPassword != null && !isPasswordMatch()) {
            errors.add("密码和确认密码不一致");
        }

        if (!isValidUserType()) {
            errors.add("用户类型无效");
        }

        if (!isValidDateRange()) {
            errors.add("有效期设置不合理");
        }

        return errors;
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写equals方法
     * 基于用户名进行比较
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

        UserCreateRequest that = (UserCreateRequest) obj;
        return Objects.equals(this.username, that.username);
    }

    /**
     * 重写hashCode方法
     * 基于用户名生成哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.username);
    }

    /**
     * 重写toString方法
     * 提供请求DTO的字符串表示（不包含敏感信息）
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "UserCreateRequest{" +
                "username='" + username + '\'' +
                ", userType=" + userType +
                ", realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", remark='" + remark + '\'' +
                ", passwordMatch=" + isPasswordMatch() +
                ", validUserType=" + isValidUserType() +
                ", validDateRange=" + isValidDateRange() +
                '}';
    }
}


