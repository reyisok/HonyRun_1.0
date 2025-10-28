package com.honyrun.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限分配请求DTO
 *
 * 用于权限分配操作的请求数据传输对象，包含用户ID、权限代码列表、
 * 有效期等信息。支持参数验证和数据绑定。
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 01:20:00
 * @modified 2025-07-01 01:20:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class PermissionAssignRequest {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 权限代码列表
     */
    @NotEmpty(message = "权限代码列表不能为空")
    private List<String> permissionCodes;

    /**
     * 权限有效期开始时间
     */
    private LocalDateTime validFrom;

    /**
     * 权限有效期结束时间
     */
    private LocalDateTime validTo;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 是否覆盖现有权限
     */
    private Boolean overrideExisting = false;

    /**
     * 默认构造函数
     */
    public PermissionAssignRequest() {
    }

    /**
     * 带参数的构造函数
     *
     * @param userId 用户ID
     * @param permissionCodes 权限代码列表
     */
    public PermissionAssignRequest(Long userId, List<String> permissionCodes) {
        this.userId = userId;
        this.permissionCodes = permissionCodes;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取权限代码列表
     *
     * @return 权限代码列表
     */
    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    /**
     * 设置权限代码列表
     *
     * @param permissionCodes 权限代码列表
     */
    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }

    /**
     * 获取权限有效期开始时间
     *
     * @return 权限有效期开始时间
     */
    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    /**
     * 设置权限有效期开始时间
     *
     * @param validFrom 权限有效期开始时间
     */
    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * 获取权限有效期结束时间
     *
     * @return 权限有效期结束时间
     */
    public LocalDateTime getValidTo() {
        return validTo;
    }

    /**
     * 设置权限有效期结束时间
     *
     * @param validTo 权限有效期结束时间
     */
    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }

    /**
     * 获取操作描述
     *
     * @return 操作描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置操作描述
     *
     * @param description 操作描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取是否覆盖现有权限
     *
     * @return 是否覆盖现有权限
     */
    public Boolean getOverrideExisting() {
        return overrideExisting;
    }

    /**
     * 设置是否覆盖现有权限
     *
     * @param overrideExisting 是否覆盖现有权限
     */
    public void setOverrideExisting(Boolean overrideExisting) {
        this.overrideExisting = overrideExisting;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断是否为永久权限
     *
     * @return true-永久权限，false-临时权限
     */
    public boolean isPermanent() {
        return validTo == null;
    }

    /**
     * 判断是否有有效期限制
     *
     * @return true-有有效期限制，false-无有效期限制
     */
    public boolean hasValidityPeriod() {
        return validFrom != null || validTo != null;
    }

    /**
     * 获取权限数量
     *
     * @return 权限数量
     */
    public int getPermissionCount() {
        return permissionCodes != null ? permissionCodes.size() : 0;
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写toString方法
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "PermissionAssignRequest{" +
                "userId=" + userId +
                ", permissionCodes=" + permissionCodes +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", description='" + description + '\'' +
                ", overrideExisting=" + overrideExisting +
                ", permissionCount=" + getPermissionCount() +
                ", permanent=" + isPermanent() +
                '}';
    }
}

