package com.honyrun.model.dto.request;

import com.honyrun.model.enums.UserType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 用户更新请求DTO
 *
 * 用于用户信息更新的数据传输对象
 * 包含用户基本信息的更新字段，支持部分更新
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  22:30:00
 * @modified 2025-07-01 22:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class UserUpdateRequest {

    /**
     * 真实姓名
     * 用户的真实姓名，用于显示和识别
     */
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    /**
     * 邮箱地址
     * 用户的邮箱地址，可用于找回密码等功能
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    /**
     * 手机号码
     * 用户的手机号码，可用于短信通知等功能
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phone;

    /**
     * 用户类型
     * 支持三种类型：SYSTEM_USER、NORMAL_USER、GUEST
     */
    private UserType userType;

    /**
     * 用户状态 - 使用String类型以保持与User实体一致
     * ACTIVE: 启用, INACTIVE: 未激活, DISABLED: 禁用, LOCKED: 锁定
     */
    private String status;

    /**
     * 账户有效期开始时间
     * 账户生效的开始时间
     */
    private LocalDateTime validFrom;

    /**
     * 账户有效期结束时间
     * 账户失效的结束时间，null表示永久有效
     */
    private LocalDateTime validTo;

    /**
     * 备注信息
     * 用户的备注信息
     */
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    private String remarks;

    /**
     * 默认构造函数
     */
    public UserUpdateRequest() {
    }

    /**
     * 带参数的构造函数
     *
     * @param realName 真实姓名
     * @param email 邮箱地址
     * @param phone 手机号码
     */
    public UserUpdateRequest(String realName, String email, String phone) {
        this.realName = realName;
        this.email = email;
        this.phone = phone;
    }

    // ==================== Getter和Setter方法 ====================

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
     * 获取用户状态
     *
     * @return 用户状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置用户状态
     *
     * @param status 用户状态：ACTIVE-启用，INACTIVE-未激活，DISABLED-禁用，LOCKED-锁定
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
    public String getRemarks() {
        return remarks;
    }

    /**
     * 设置备注信息
     *
     * @param remarks 备注信息
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断是否有任何字段需要更新
     *
     * @return true-有字段需要更新，false-无字段需要更新
     */
    public boolean hasAnyFieldToUpdate() {
        return realName != null || email != null || phone != null ||
               userType != null || status != null || validFrom != null ||
               validTo != null || remarks != null;
    }

    /**
     * 判断是否启用用户
     *
     * @return true-启用，false-禁用或未设置
     */
    public boolean isEnabled() {
        return "ACTIVE".equals(status);
    }

    /**
     * 判断是否禁用用户
     *
     * @return true-禁用，false-启用或未设置
     */
    public boolean isDisabled() {
        return "DISABLED".equals(status);
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写toString方法
     * 提供用户更新请求的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "UserUpdateRequest{" +
                "realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", userType=" + userType +
                ", status=" + status +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}


