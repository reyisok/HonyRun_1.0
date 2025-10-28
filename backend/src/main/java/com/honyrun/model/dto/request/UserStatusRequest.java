package com.honyrun.model.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 用户状态修改请求DTO
 *
 * @author: Mr.Rey
 * Copyright © 2025
 * @created 2025-09-28 09:45:00
 * @modified 2025-09-28 09:45:00
 * @version 1.0.0
 */
public class UserStatusRequest {

    /**
     * 用户启用状态
     */
    @NotNull(message = "用户状态不能为空")
    private Boolean enabled;

    /**
     * 默认构造函数
     */
    public UserStatusRequest() {
    }

    /**
     * 构造函数
     *
     * @param enabled 用户启用状态
     */
    public UserStatusRequest(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取用户启用状态
     *
     * @return 用户启用状态
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * 设置用户启用状态
     *
     * @param enabled 用户启用状态
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 检查用户是否启用
     *
     * @return 如果用户启用返回true，否则返回false
     */
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public String toString() {
        return "UserStatusRequest{" +
                "enabled=" + enabled +
                '}';
    }
}
