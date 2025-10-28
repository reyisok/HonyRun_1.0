package com.honyrun.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 批量操作响应DTO
 *
 * 用于批量操作结果的响应数据封装
 * 提供详细的操作结果统计和错误信息
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 11:45:00
 * @modified 2025-07-01 11:45:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Schema(description = "批量操作响应")
public class BatchOperationResponse {

    /**
     * 操作任务ID
     */
    @Schema(description = "操作任务ID", example = "batch_20250128_114500")
    private String taskId;

    /**
     * 操作类型
     */
    @Schema(description = "操作类型", example = "BATCH_CREATE_USER")
    private String operationType;

    /**
     * 操作状态
     * PENDING - 等待中
     * PROCESSING - 处理中
     * COMPLETED - 已完成
     * FAILED - 失败
     * PARTIAL_SUCCESS - 部分成功
     */
    @Schema(description = "操作状态", example = "COMPLETED")
    private String status;

    /**
     * 总数量
     */
    @Schema(description = "总数量", example = "100")
    private Integer totalCount;

    /**
     * 成功数量
     */
    @Schema(description = "成功数量", example = "95")
    private Integer successCount;

    /**
     * 失败数量
     */
    @Schema(description = "失败数量", example = "5")
    private Integer failureCount;

    /**
     * 跳过数量
     */
    @Schema(description = "跳过数量", example = "0")
    private Integer skipCount;

    /**
     * 进度百分比
     */
    @Schema(description = "进度百分比", example = "100")
    private Integer progress;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2025-07-01 11:45:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间", example = "2025-07-01 11:46:30")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 执行时长（毫秒）
     */
    @Schema(description = "执行时长（毫秒）", example = "90000")
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
     * 操作描述
     */
    @Schema(description = "操作描述", example = "批量创建用户")
    private String description;

    /**
     * 成功项目列表
     */
    @Schema(description = "成功项目列表")
    private List<BatchOperationItem> successItems;

    /**
     * 失败项目列表
     */
    @Schema(description = "失败项目列表")
    private List<BatchOperationItem> failureItems;

    /**
     * 跳过项目列表
     */
    @Schema(description = "跳过项目列表")
    private List<BatchOperationItem> skipItems;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 详细结果
     */
    @Schema(description = "详细结果")
    private Map<String, Object> detailResult;

    /**
     * 批量操作项目内部类
     */
    @Schema(description = "批量操作项目")
    public static class BatchOperationItem {
        
        /**
         * 项目标识
         */
        @Schema(description = "项目标识", example = "user001")
        private String itemId;

        /**
         * 项目名称
         */
        @Schema(description = "项目名称", example = "用户001")
        private String itemName;

        /**
         * 操作状态
         */
        @Schema(description = "操作状态", example = "SUCCESS")
        private String status;

        /**
         * 结果信息
         */
        @Schema(description = "结果信息", example = "用户创建成功")
        private String message;

        /**
         * 错误代码
         */
        @Schema(description = "错误代码", example = "USER_ALREADY_EXISTS")
        private String errorCode;

        /**
         * 处理时间
         */
        @Schema(description = "处理时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime processTime;

        /**
         * 额外数据
         */
        @Schema(description = "额外数据")
        private Map<String, Object> extraData;

        // ==================== 构造函数 ====================

        public BatchOperationItem() {
        }

        public BatchOperationItem(String itemId, String itemName, String status, String message) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.status = status;
            this.message = message;
            this.processTime = LocalDateTime.now();
        }

        // ==================== Getter和Setter方法 ====================

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
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

        public LocalDateTime getProcessTime() {
            return processTime;
        }

        public void setProcessTime(LocalDateTime processTime) {
            this.processTime = processTime;
        }

        public Map<String, Object> getExtraData() {
            return extraData;
        }

        public void setExtraData(Map<String, Object> extraData) {
            this.extraData = extraData;
        }

        @Override
        public String toString() {
            return "BatchOperationItem{" +
                    "itemId='" + itemId + '\'' +
                    ", itemName='" + itemName + '\'' +
                    ", status='" + status + '\'' +
                    ", message='" + message + '\'' +
                    ", errorCode='" + errorCode + '\'' +
                    ", processTime=" + processTime +
                    '}';
        }
    }

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public BatchOperationResponse() {
        this.status = "PENDING";
        this.progress = 0;
        this.startTime = LocalDateTime.now();
        this.successCount = 0;
        this.failureCount = 0;
        this.skipCount = 0;
    }

    /**
     * 带参数的构造函数
     *
     * @param taskId 任务ID
     * @param operationType 操作类型
     * @param totalCount 总数量
     */
    public BatchOperationResponse(String taskId, String operationType, Integer totalCount) {
        this();
        this.taskId = taskId;
        this.operationType = operationType;
        this.totalCount = totalCount;
    }

    // ==================== Getter和Setter方法 ====================

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<BatchOperationItem> getSuccessItems() {
        return successItems;
    }

    public void setSuccessItems(List<BatchOperationItem> successItems) {
        this.successItems = successItems;
    }

    public List<BatchOperationItem> getFailureItems() {
        return failureItems;
    }

    public void setFailureItems(List<BatchOperationItem> failureItems) {
        this.failureItems = failureItems;
    }

    public List<BatchOperationItem> getSkipItems() {
        return skipItems;
    }

    public void setSkipItems(List<BatchOperationItem> skipItems) {
        this.skipItems = skipItems;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, Object> getDetailResult() {
        return detailResult;
    }

    public void setDetailResult(Map<String, Object> detailResult) {
        this.detailResult = detailResult;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断操作是否完成
     *
     * @return true-已完成，false-未完成
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status) || "FAILED".equals(status) || "PARTIAL_SUCCESS".equals(status);
    }

    /**
     * 判断操作是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return "COMPLETED".equals(status);
    }

    /**
     * 判断操作是否失败
     *
     * @return true-失败，false-未失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * 判断操作是否部分成功
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
            case "PENDING":
                return "等待中";
            case "PROCESSING":
                return "处理中";
            case "COMPLETED":
                return "已完成";
            case "FAILED":
                return "失败";
            case "PARTIAL_SUCCESS":
                return "部分成功";
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
     * 更新进度
     *
     * @param processedCount 已处理数量
     */
    public void updateProgress(int processedCount) {
        if (totalCount != null && totalCount > 0) {
            this.progress = (int) ((double) processedCount / totalCount * 100);
        }
    }

    /**
     * 标记为完成
     */
    public void markCompleted() {
        this.status = "COMPLETED";
        this.endTime = LocalDateTime.now();
        this.progress = 100;
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
        this.progress = 100;
        if (startTime != null) {
            this.executionTime = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * 标记为处理中
     *
     * @param progress 进度百分比
     */
    public void markProcessing(Integer progress) {
        this.status = "PROCESSING";
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "BatchOperationResponse{" +
                "taskId='" + taskId + '\'' +
                ", operationType='" + operationType + '\'' +
                ", status='" + status + '\'' +
                ", totalCount=" + totalCount +
                ", successCount=" + successCount +
                ", failureCount=" + failureCount +
                ", skipCount=" + skipCount +
                ", progress=" + progress +
                ", successRate=" + String.format("%.2f%%", getSuccessRate()) +
                ", executionTime=" + getFormattedExecutionTime() +
                ", operatorName='" + operatorName + '\'' +
                '}';
    }
}

