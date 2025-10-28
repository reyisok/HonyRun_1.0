package com.honyrun.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 密码修改请求DTO
 *
 * @author: Mr.Rey
 * Copyright © 2025
 * @created 2025-09-28 09:45:00
 * @modified 2025-09-28 09:45:00
 * @version 1.0.0
 */
public class PasswordChangeRequest {

    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20位之间")
    private String newPassword;

    /**
     * 默认构造函数
     */
    public PasswordChangeRequest() {
    }

    /**
     * 构造函数
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public PasswordChangeRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    /**
     * 获取旧密码
     *
     * @return 旧密码
     */
    public String getOldPassword() {
        return oldPassword;
    }

    /**
     * 设置旧密码
     *
     * @param oldPassword 旧密码
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * 获取新密码
     *
     * @return 新密码
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * 设置新密码
     *
     * @param newPassword 新密码
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public String toString() {
        return "PasswordChangeRequest{" +
                "oldPassword='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                '}';
    }
}
