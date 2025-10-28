package com.honyrun.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量权限操作请求DTO
 *
 * 用于批量权限操作的请求数据传输对象，包含用户ID列表、权限代码列表、
 * 有效期等信息。支持参数验证和数据绑定。
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 01:25:00
 * @modified 2025-07-01 01:25:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class PermissionBatchRequest {

    /**
     * 用户ID列表
     */
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

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
     * 是否忽略错误继续执行
     */
    private Boolean continueOnError = true;

    /**
     * 批量操作类型
     */
    private BatchOperationType operationType = BatchOperationType.ASSIGN;

    /**
     * 默认构造函数
     */
    public PermissionBatchRequest() {
    }

    /**
     * 带参数的构造函数
     *
     * @param userIds 用户ID列表
     * @param permissionCodes 权限代码列表
     */
    public PermissionBatchRequest(List<Long> userIds, List<String> permissionCodes) {
        this.userIds = userIds;
        this.permissionCodes = permissionCodes;
    }

    /**
     * 批量操作类型枚举
     */
    public enum BatchOperationType {
        /**
         * 分配权限
         */
        ASSIGN,
        
        /**
         * 撤销权限
         */
        REVOKE,
        
        /**
         * 更新权限
         */
        UPDATE,
        
        /**
         * 复制权限
         */
        COPY
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取用户ID列表
     *
     * @return 用户ID列表
     */
    public List<Long> getUserIds() {
        return userIds;
    }

    /**
     * 设置用户ID列表
     *
     * @param userIds 用户ID列表
     */
    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
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

    /**
     * 获取是否忽略错误继续执行
     *
     * @return 是否忽略错误继续执行
     */
    public Boolean getContinueOnError() {
        return continueOnError;
    }

    /**
     * 设置是否忽略错误继续执行
     *
     * @param continueOnError 是否忽略错误继续执行
     */
    public void setContinueOnError(Boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    /**
     * 获取批量操作类型
     *
     * @return 批量操作类型
     */
    public BatchOperationType getOperationType() {
        return operationType;
    }

    /**
     * 设置批量操作类型
     *
     * @param operationType 批量操作类型
     */
    public void setOperationType(BatchOperationType operationType) {
        this.operationType = operationType;
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
     * 获取用户数量
     *
     * @return 用户数量
     */
    public int getUserCount() {
        return userIds != null ? userIds.size() : 0;
    }

    /**
     * 获取权限数量
     *
     * @return 权限数量
     */
    public int getPermissionCount() {
        return permissionCodes != null ? permissionCodes.size() : 0;
    }

    /**
     * 获取总操作数量（用户数量 × 权限数量）
     *
     * @return 总操作数量
     */
    public int getTotalOperationCount() {
        return getUserCount() * getPermissionCount();
    }

    /**
     * 判断是否为大批量操作（操作数量超过100）
     *
     * @return true-大批量操作，false-小批量操作
     */
    public boolean isLargeBatchOperation() {
        return getTotalOperationCount() > 100;
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写toString方法
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "PermissionBatchRequest{" +
                "userIds=" + userIds +
                ", permissionCodes=" + permissionCodes +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", description='" + description + '\'' +
                ", overrideExisting=" + overrideExisting +
                ", continueOnError=" + continueOnError +
                ", operationType=" + operationType +
                ", userCount=" + getUserCount() +
                ", permissionCount=" + getPermissionCount() +
                ", totalOperationCount=" + getTotalOperationCount() +
                ", permanent=" + isPermanent() +
                '}';
    }
}

