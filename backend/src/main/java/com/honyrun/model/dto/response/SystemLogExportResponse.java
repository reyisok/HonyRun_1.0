package com.honyrun.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 系统日志导出响应DTO
 *
 * 用于系统日志导出功能的响应数据封装
 * 提供导出任务的状态信息和下载链接
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 10:40:00
 * @modified 2025-07-01 10:40:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Schema(description = "系统日志导出响应")
public class SystemLogExportResponse {

    /**
     * 导出任务ID
     */
    @Schema(description = "导出任务ID", example = "export_20250128_103000")
    private String taskId;

    /**
     * 导出状态
     * PENDING-等待中，PROCESSING-处理中，COMPLETED-已完成，FAILED-失败
     */
    @Schema(description = "导出状态", example = "COMPLETED")
    private String status;

    /**
     * 导出文件名
     */
    @Schema(description = "导出文件名", example = "sys_system_logs_20250128.xlsx")
    private String fileName;

    /**
     * 导出文件大小（字节）
     */
    @Schema(description = "导出文件大小（字节）", example = "1048576")
    private Long fileSize;

    /**
     * 导出记录数量
     */
    @Schema(description = "导出记录数量", example = "1000")
    private Long recordCount;

    /**
     * 下载链接
     */
    @Schema(description = "下载链接", example = "/api/v1/system/logs/export/download/export_20250128_103000")
    private String downloadUrl;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-07-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间", example = "2025-07-01 10:31:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间", example = "2025-07-02 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 进度百分比
     */
    @Schema(description = "进度百分比", example = "100")
    private Integer progress;

    /**
     * 导出格式
     * EXCEL、CSV、JSON
     */
    @Schema(description = "导出格式", example = "EXCEL")
    private String format;

    /**
     * 导出用户ID
     */
    @Schema(description = "导出用户ID", example = "1")
    private Long userId;

    /**
     * 导出用户名
     */
    @Schema(description = "导出用户名", example = "honyrun-sys")
    private String username;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public SystemLogExportResponse() {
        this.status = "PENDING";
        this.progress = 0;
        this.createdTime = LocalDateTime.now();
    }

    /**
     * 带参数的构造函数
     *
     * @param taskId 任务ID
     * @param format 导出格式
     * @param userId 用户ID
     * @param username 用户名
     */
    public SystemLogExportResponse(String taskId, String format, Long userId, String username) {
        this();
        this.taskId = taskId;
        this.format = format;
        this.userId = userId;
        this.username = username;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Long recordCount) {
        this.recordCount = recordCount;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(LocalDateTime completedTime) {
        this.completedTime = completedTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断导出是否完成
     *
     * @return true-已完成，false-未完成
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(this.status);
    }

    /**
     * 判断导出是否失败
     *
     * @return true-失败，false-未失败
     */
    public boolean isFailed() {
        return "FAILED".equals(this.status);
    }

    /**
     * 判断导出是否正在处理
     *
     * @return true-处理中，false-非处理中
     */
    public boolean isProcessing() {
        return "PROCESSING".equals(this.status);
    }

    /**
     * 判断文件是否已过期
     *
     * @return true-已过期，false-未过期
     */
    public boolean isExpired() {
        return expireTime != null && LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 获取格式化的文件大小
     *
     * @return 格式化的文件大小
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "未知";
        }
        
        if (fileSize < 1024) {
            return fileSize + "B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2fKB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2fMB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.2fGB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
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
            default:
                return "未知";
        }
    }

    /**
     * 设置导出完成
     *
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param recordCount 记录数量
     * @param downloadUrl 下载链接
     */
    public void markCompleted(String fileName, Long fileSize, Long recordCount, String downloadUrl) {
        this.status = "COMPLETED";
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.recordCount = recordCount;
        this.downloadUrl = downloadUrl;
        this.completedTime = LocalDateTime.now();
        this.progress = 100;
        // 设置24小时后过期
        this.expireTime = LocalDateTime.now().plusHours(24);
    }

    /**
     * 设置导出失败
     *
     * @param errorMessage 错误信息
     */
    public void markFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.completedTime = LocalDateTime.now();
    }

    /**
     * 设置处理中状态
     *
     * @param progress 进度百分比
     */
    public void markProcessing(Integer progress) {
        this.status = "PROCESSING";
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "SystemLogExportResponse{" +
                "taskId='" + taskId + '\'' +
                ", status='" + status + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", recordCount=" + recordCount +
                ", progress=" + progress +
                ", format='" + format + '\'' +
                ", username='" + username + '\'' +
                ", createdTime=" + createdTime +
                ", completedTime=" + completedTime +
                '}';
    }
}


