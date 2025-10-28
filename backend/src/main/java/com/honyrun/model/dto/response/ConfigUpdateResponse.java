package com.honyrun.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置更新响应DTO
 *
 * 用于系统配置更新结果的响应数据封装
 * 提供详细的更新结果信息和状态反馈
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 12:15:00
 * @modified 2025-07-01 12:15:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Schema(description = "配置更新响应")
public class ConfigUpdateResponse {

    /**
     * 更新任务ID
     */
    @Schema(description = "更新任务ID", example = "config_update_20250128_121500")
    private String taskId;

    /**
     * 更新状态
     * SUCCESS - 成功
     * FAILED - 失败
     * PARTIAL_SUCCESS - 部分成功
     * PENDING - 等待中
     * PROCESSING - 处理中
     */
    @Schema(description = "更新状态", example = "SUCCESS")
    private String status;

    /**
     * 更新类型
     * SINGLE - 单个配置更新
     * BATCH - 批量配置更新
     * ASYNC - 异步更新
     * MERGE - 合并更新
     * REPLACE - 替换更新
     * RESTORE - 恢复更新
     * TEMPLATE - 模板更新
     * IMPORT - 导入更新
     * ENV_SYNC - 环境同步
     * ENV_SWITCH - 环境切换
     */
    @Schema(description = "更新类型", example = "SINGLE")
    private String updateType;

    /**
     * 更新模式
     */
    @Schema(description = "更新模式", example = "SINGLE")
    private String updateMode;

    /**
     * 总配置项数量
     */
    @Schema(description = "总配置项数量", example = "5")
    private Integer totalCount;

    /**
     * 成功更新数量
     */
    @Schema(description = "成功更新数量", example = "4")
    private Integer successCount;

    /**
     * 失败更新数量
     */
    @Schema(description = "失败更新数量", example = "1")
    private Integer failureCount;

    /**
     * 跳过更新数量
     */
    @Schema(description = "跳过更新数量", example = "0")
    private Integer skipCount;

    /**
     * 更新开始时间
     */
    @Schema(description = "更新开始时间", example = "2025-07-01 12:15:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 更新结束时间
     */
    @Schema(description = "更新结束时间", example = "2025-07-01 12:15:30")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 执行时长（毫秒）
     */
    @Schema(description = "执行时长（毫秒）", example = "30000")
    private Long executionTime;

    /**
     * 操作用户ID
     */
    @Schema(description = "操作用户ID", example = "1")
    private Long operatorId;

    /**
     * 操作用户名
     */
    @Schema(description = "操作用户名", example = "honyrun-sys")
    private String operatorName;

    /**
     * 更新原因
     */
    @Schema(description = "更新原因", example = "运维调整")
    private String updateReason;

    /**
     * 是否需要重启
     */
    @Schema(description = "是否需要重启", example = "false")
    private Boolean requireRestart;

    /**
     * 需要重启的配置项数量
     */
    @Schema(description = "需要重启的配置项数量", example = "0")
    private Integer restartRequiredCount;

    /**
     * 成功更新的配置项列表
     */
    @Schema(description = "成功更新的配置项列表")
    private List<ConfigUpdateItem> successItems;

    /**
     * 失败更新的配置项列表
     */
    @Schema(description = "失败更新的配置项列表")
    private List<ConfigUpdateItem> failureItems;

    /**
     * 跳过更新的配置项列表
     */
    @Schema(description = "跳过更新的配置项列表")
    private List<ConfigUpdateItem> skipItems;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 警告信息列表
     */
    @Schema(description = "警告信息列表")
    private List<String> warnings;

    /**
     * 备份信息
     */
    @Schema(description = "备份信息")
    private BackupInfo backupInfo;

    /**
     * 配置更新项内部类
     */
    @Schema(description = "配置更新项")
    public static class ConfigUpdateItem {
        
        /**
         * 配置键名
         */
        @Schema(description = "配置键名", example = "system.monitor.enabled")
        private String configKey;

        /**
         * 旧配置值
         */
        @Schema(description = "旧配置值", example = "false")
        private String oldValue;

        /**
         * 新配置值
         */
        @Schema(description = "新配置值", example = "true")
        private String newValue;

        /**
         * 配置分组
         */
        @Schema(description = "配置分组", example = "SYSTEM")
        private String configGroup;

        /**
         * 配置类型
         */
        @Schema(description = "配置类型", example = "BOOLEAN")
        private String configType;

        /**
         * 更新状态
         */
        @Schema(description = "更新状态", example = "SUCCESS")
        private String status;

        /**
         * 更新消息
         */
        @Schema(description = "更新消息", example = "配置更新成功")
        private String message;

        /**
         * 错误代码
         */
        @Schema(description = "错误代码", example = "CONFIG_VALIDATION_FAILED")
        private String errorCode;

        /**
         * 是否需要重启
         */
        @Schema(description = "是否需要重启", example = "false")
        private Boolean requireRestart;

        /**
         * 更新时间
         */
        @Schema(description = "更新时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;

        // ==================== 构造函数 ====================

        public ConfigUpdateItem() {
        }

        public ConfigUpdateItem(String configKey, String oldValue, String newValue, String status, String message) {
            this.configKey = configKey;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.status = status;
            this.message = message;
            this.updateTime = LocalDateTime.now();
        }

        // ==================== Getter和Setter方法 ====================

        public String getConfigKey() {
            return configKey;
        }

        public void setConfigKey(String configKey) {
            this.configKey = configKey;
        }

        public String getOldValue() {
            return oldValue;
        }

        public void setOldValue(String oldValue) {
            this.oldValue = oldValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }

        public String getConfigGroup() {
            return configGroup;
        }

        public void setConfigGroup(String configGroup) {
            this.configGroup = configGroup;
        }

        public String getConfigType() {
            return configType;
        }

        public void setConfigType(String configType) {
            this.configType = configType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public Boolean getRequireRestart() {
            return requireRestart;
        }

        public void setRequireRestart(Boolean requireRestart) {
            this.requireRestart = requireRestart;
        }

        public LocalDateTime getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
        }

        @Override
        public String toString() {
            return "ConfigUpdateItem{" +
                    "configKey='" + configKey + '\'' +
                    ", oldValue='" + oldValue + '\'' +
                    ", newValue='" + newValue + '\'' +
                    ", status='" + status + '\'' +
                    ", message='" + message + '\'' +
                    ", requireRestart=" + requireRestart +
                    ", updateTime=" + updateTime +
                    '}';
        }
    }

    /**
     * 备份信息内部类
     */
    @Schema(description = "备份信息")
    public static class BackupInfo {
        
        /**
         * 备份ID
         */
        @Schema(description = "备份ID", example = "backup_20250128_121500")
        private String backupId;

        /**
         * 备份时间
         */
        @Schema(description = "备份时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime backupTime;

        /**
         * 备份配置数量
         */
        @Schema(description = "备份配置数量", example = "5")
        private Integer backupCount;

        /**
         * 备份文件路径
         */
        @Schema(description = "备份文件路径", example = "/backup/config/backup_20250128_121500.json")
        private String backupFilePath;

        /**
         * 备份描述
         */
        @Schema(description = "备份描述", example = "配置更新前自动备份")
        private String description;

        // ==================== 构造函数 ====================

        public BackupInfo() {
        }

        public BackupInfo(String backupId, Integer backupCount, String description) {
            this.backupId = backupId;
            this.backupCount = backupCount;
            this.description = description;
            this.backupTime = LocalDateTime.now();
        }

        // ==================== Getter和Setter方法 ====================

        public String getBackupId() {
            return backupId;
        }

        public void setBackupId(String backupId) {
            this.backupId = backupId;
        }

        public LocalDateTime getBackupTime() {
            return backupTime;
        }

        public void setBackupTime(LocalDateTime backupTime) {
            this.backupTime = backupTime;
        }

        public Integer getBackupCount() {
            return backupCount;
        }

        public void setBackupCount(Integer backupCount) {
            this.backupCount = backupCount;
        }

        public String getBackupFilePath() {
            return backupFilePath;
        }

        public void setBackupFilePath(String backupFilePath) {
            this.backupFilePath = backupFilePath;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return "BackupInfo{" +
                    "backupId='" + backupId + '\'' +
                    ", backupTime=" + backupTime +
                    ", backupCount=" + backupCount +
                    ", backupFilePath='" + backupFilePath + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public ConfigUpdateResponse() {
        this.status = "PENDING";
        this.startTime = LocalDateTime.now();
        this.successCount = 0;
        this.failureCount = 0;
        this.skipCount = 0;
        this.restartRequiredCount = 0;
    }

    /**
     * 带参数的构造函数
     *
     * @param taskId 任务ID
     * @param updateMode 更新模式
     * @param totalCount 总数量
     */
    public ConfigUpdateResponse(String taskId, String updateMode, Integer totalCount) {
        this();
        this.taskId = taskId;
        this.updateMode = updateMode;
        this.totalCount = totalCount;
    }

    // ==================== Getter和Setter方法 ====================

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public String getUpdateMode() {
        return updateMode;
    }

    public void setUpdateMode(String updateMode) {
        this.updateMode = updateMode;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    /**
     * 获取失败数量（兼容方法）
     *
     * @return 失败数量
     */
    public Integer getFailedCount() {
        return failureCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public Integer getSkipCount() {
        return skipCount;
    }

    public void setSkipCount(Integer skipCount) {
        this.skipCount = skipCount;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getUpdateReason() {
        return updateReason;
    }

    public void setUpdateReason(String updateReason) {
        this.updateReason = updateReason;
    }

    public Boolean getRequireRestart() {
        return requireRestart;
    }

    public void setRequireRestart(Boolean requireRestart) {
        this.requireRestart = requireRestart;
    }

    public Integer getRestartRequiredCount() {
        return restartRequiredCount;
    }

    public void setRestartRequiredCount(Integer restartRequiredCount) {
        this.restartRequiredCount = restartRequiredCount;
    }

    public List<ConfigUpdateItem> getSuccessItems() {
        return successItems;
    }

    public void setSuccessItems(List<ConfigUpdateItem> successItems) {
        this.successItems = successItems;
    }

    public List<ConfigUpdateItem> getFailureItems() {
        return failureItems;
    }

    public void setFailureItems(List<ConfigUpdateItem> failureItems) {
        this.failureItems = failureItems;
    }

    public List<ConfigUpdateItem> getSkipItems() {
        return skipItems;
    }

    public void setSkipItems(List<ConfigUpdateItem> skipItems) {
        this.skipItems = skipItems;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public BackupInfo getBackupInfo() {
        return backupInfo;
    }

    public void setBackupInfo(BackupInfo backupInfo) {
        this.backupInfo = backupInfo;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断更新是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    /**
     * 判断更新是否失败
     *
     * @return true-失败，false-未失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * 判断更新是否部分成功
     *
     * @return true-部分成功，false-非部分成功
     */
    public boolean isPartialSuccess() {
        return "PARTIAL_SUCCESS".equals(status);
    }

    /**
     * 获取成功率
     *
     * @return 成功率（百分比）
     */
    public double getSuccessRate() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }

    /**
     * 获取失败率
     *
     * @return 失败率（百分比）
     */
    public double getFailureRate() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return (double) failureCount / totalCount * 100;
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
     */
    public String getStatusDescription() {
        switch (status) {
            case "SUCCESS":
                return "更新成功";
            case "FAILED":
                return "更新失败";
            case "PARTIAL_SUCCESS":
                return "部分成功";
            case "PENDING":
                return "等待中";
            case "PROCESSING":
                return "处理中";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取格式化的执行时间
     *
     * @return 格式化的执行时间
     */
    public String getFormattedExecutionTime() {
        if (executionTime == null) {
            return "未知";
        }
        
        if (executionTime < 1000) {
            return executionTime + "ms";
        } else if (executionTime < 60000) {
            return String.format("%.2fs", executionTime / 1000.0);
        } else {
            long minutes = executionTime / 60000;
            long seconds = (executionTime % 60000) / 1000;
            return String.format("%dm%ds", minutes, seconds);
        }
    }

    /**
     * 标记为成功
     */
    public void markSuccess() {
        this.status = "SUCCESS";
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.executionTime = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * 标记为失败
     *
     * @param errorMessage 错误信息
     */
    public void markFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.executionTime = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * 标记为部分成功
     */
    public void markPartialSuccess() {
        this.status = "PARTIAL_SUCCESS";
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.executionTime = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * 标记为处理中
     */
    public void markProcessing() {
        this.status = "PROCESSING";
    }

    /**
     * 获取更新摘要
     *
     * @return 更新摘要
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("taskId", taskId);
        summary.put("status", status);
        summary.put("statusDescription", getStatusDescription());
        summary.put("totalCount", totalCount);
        summary.put("successCount", successCount);
        summary.put("failureCount", failureCount);
        summary.put("skipCount", skipCount);
        summary.put("successRate", String.format("%.2f%%", getSuccessRate()));
        summary.put("executionTime", getFormattedExecutionTime());
        summary.put("requireRestart", requireRestart);
        summary.put("restartRequiredCount", restartRequiredCount);
        return summary;
    }

    @Override
    public String toString() {
        return "ConfigUpdateResponse{" +
                "taskId='" + taskId + '\'' +
                ", status='" + status + '\'' +
                ", updateMode='" + updateMode + '\'' +
                ", totalCount=" + totalCount +
                ", successCount=" + successCount +
                ", failureCount=" + failureCount +
                ", skipCount=" + skipCount +
                ", successRate=" + String.format("%.2f%%", getSuccessRate()) +
                ", executionTime=" + getFormattedExecutionTime() +
                ", operatorName='" + operatorName + '\'' +
                ", requireRestart=" + requireRestart +
                ", restartRequiredCount=" + restartRequiredCount +
                '}';
    }
}

