package com.honyrun.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 批量权限分配请求DTO
 *
 * 用于批量分配用户权限的请求参数封装
 * 支持为多个用户批量分配或移除权限
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 11:40:00
 * @modified 2025-07-01 11:40:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Schema(description = "批量权限分配请求")
public class BatchPermissionAssignRequest {

    /**
     * 用户ID列表
     */
    @Schema(description = "用户ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> userIds;

    /**
     * 权限代码列表
     */
    @Schema(description = "权限代码列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> permissionCodes;

    /**
     * 操作类型
     * ASSIGN - 分配权限
     * REVOKE - 撤销权限
     * REPLACE - 替换权限（先清除所有权限，再分配新权限）
     */
    @Schema(description = "操作类型", example = "ASSIGN")
    private String operationType = "ASSIGN";

    /**
     * 是否覆盖现有权限
     * 仅在ASSIGN操作时有效
     */
    @Schema(description = "是否覆盖现有权限", example = "false")
    private Boolean overrideExisting = false;

    /**
     * 权限分配原因
     */
    @Schema(description = "权限分配原因", example = "批量权限调整")
    private String reason;

    /**
     * 是否发送通知
     */
    @Schema(description = "是否发送通知", example = "false")
    private Boolean sendNotification = false;

    /**
     * 批次大小
     */
    @Schema(description = "批次大小", example = "50")
    private Integer batchSize = 50;

    /**
     * 是否异步执行
     */
    @Schema(description = "是否异步执行", example = "true")
    private Boolean asyncExecution = true;

    /**
     * 权限分配详情列表
     * 用于更精细的权限控制
     */
    @Schema(description = "权限分配详情列表")
    private List<PermissionAssignDetail> assignDetails;

    /**
     * 权限分配详情内部类
     */
    @Schema(description = "权限分配详情")
    public static class PermissionAssignDetail {
        
        /**
         * 用户ID
         */
        @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long userId;

        /**
         * 权限代码列表
         */
        @Schema(description = "权限代码列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<String> permissionCodes;

        /**
         * 操作类型
         */
        @Schema(description = "操作类型", example = "ASSIGN")
        private String operationType = "ASSIGN";

        /**
         * 备注
         */
        @Schema(description = "备注")
        private String remark;

        // ==================== Getter和Setter方法 ====================

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public List<String> getPermissionCodes() {
            return permissionCodes;
        }

        public void setPermissionCodes(List<String> permissionCodes) {
            this.permissionCodes = permissionCodes;
        }

        public String getOperationType() {
            return operationType;
        }

        public void setOperationType(String operationType) {
            this.operationType = operationType;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        @Override
        public String toString() {
            return "PermissionAssignDetail{" +
                    "userId=" + userId +
                    ", permissionCodes=" + permissionCodes +
                    ", operationType='" + operationType + '\'' +
                    ", remark='" + remark + '\'' +
                    '}';
        }
    }

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public BatchPermissionAssignRequest() {
    }

    /**
     * 带参数的构造函数
     *
     * @param userIds 用户ID列表
     * @param permissionCodes 权限代码列表
     * @param operationType 操作类型
     */
    public BatchPermissionAssignRequest(List<Long> userIds, List<String> permissionCodes, String operationType) {
        this.userIds = userIds;
        this.permissionCodes = permissionCodes;
        this.operationType = operationType;
    }

    // ==================== Getter和Setter方法 ====================

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Boolean getOverrideExisting() {
        return overrideExisting;
    }

    public void setOverrideExisting(Boolean overrideExisting) {
        this.overrideExisting = overrideExisting;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getSendNotification() {
        return sendNotification;
    }

    public void setSendNotification(Boolean sendNotification) {
        this.sendNotification = sendNotification;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Boolean getAsyncExecution() {
        return asyncExecution;
    }

    public void setAsyncExecution(Boolean asyncExecution) {
        this.asyncExecution = asyncExecution;
    }

    public List<PermissionAssignDetail> getAssignDetails() {
        return assignDetails;
    }

    public void setAssignDetails(List<PermissionAssignDetail> assignDetails) {
        this.assignDetails = assignDetails;
    }

    // ==================== 业务方法 ====================

    /**
     * 验证请求参数
     *
     * @return 验证结果
     */
    public boolean isValid() {
        // 检查基本参数
        if ((userIds == null || userIds.isEmpty()) && (assignDetails == null || assignDetails.isEmpty())) {
            return false;
        }
        
        if (operationType == null || operationType.trim().isEmpty()) {
            return false;
        }
        
        // 如果使用基本模式，检查权限代码
        if (userIds != null && !userIds.isEmpty()) {
            if (permissionCodes == null || permissionCodes.isEmpty()) {
                return false;
            }
        }
        
        // 如果使用详细模式，检查详情列表
        if (assignDetails != null && !assignDetails.isEmpty()) {
            for (PermissionAssignDetail detail : assignDetails) {
                if (detail.getUserId() == null || 
                    detail.getPermissionCodes() == null || 
                    detail.getPermissionCodes().isEmpty()) {
                    return false;
                }
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
        if (assignDetails != null && !assignDetails.isEmpty()) {
            return (int) assignDetails.stream()
                    .map(PermissionAssignDetail::getUserId)
                    .distinct()
                    .count();
        }
        return userIds != null ? userIds.size() : 0;
    }

    /**
     * 获取权限数量
     *
     * @return 权限数量
     */
    public int getPermissionCount() {
        if (assignDetails != null && !assignDetails.isEmpty()) {
            return assignDetails.stream()
                    .mapToInt(detail -> detail.getPermissionCodes().size())
                    .sum();
        }
        return permissionCodes != null ? permissionCodes.size() : 0;
    }

    /**
     * 判断是否为分配操作
     *
     * @return true-分配操作，false-其他操作
     */
    public boolean isAssignOperation() {
        return "ASSIGN".equalsIgnoreCase(operationType);
    }

    /**
     * 判断是否为撤销操作
     *
     * @return true-撤销操作，false-其他操作
     */
    public boolean isRevokeOperation() {
        return "REVOKE".equalsIgnoreCase(operationType);
    }

    /**
     * 判断是否为替换操作
     *
     * @return true-替换操作，false-其他操作
     */
    public boolean isReplaceOperation() {
        return "REPLACE".equalsIgnoreCase(operationType);
    }

    /**
     * 判断是否使用详细模式
     *
     * @return true-详细模式，false-基本模式
     */
    public boolean isDetailMode() {
        return assignDetails != null && !assignDetails.isEmpty();
    }

    /**
     * 获取操作描述
     *
     * @return 操作描述
     */
    public String getOperationDescription() {
        switch (operationType.toUpperCase()) {
            case "ASSIGN":
                return "分配权限";
            case "REVOKE":
                return "撤销权限";
            case "REPLACE":
                return "替换权限";
            default:
                return "未知操作";
        }
    }

    /**
     * 估算操作数量
     *
     * @return 操作数量
     */
    public int getEstimatedOperationCount() {
        return getUserCount() * (permissionCodes != null ? permissionCodes.size() : 1);
    }

    @Override
    public String toString() {
        return "BatchPermissionAssignRequest{" +
                "userCount=" + getUserCount() +
                ", permissionCount=" + getPermissionCount() +
                ", operationType='" + operationType + '\'' +
                ", overrideExisting=" + overrideExisting +
                ", reason='" + reason + '\'' +
                ", sendNotification=" + sendNotification +
                ", batchSize=" + batchSize +
                ", asyncExecution=" + asyncExecution +
                ", detailMode=" + isDetailMode() +
                '}';
    }
}

