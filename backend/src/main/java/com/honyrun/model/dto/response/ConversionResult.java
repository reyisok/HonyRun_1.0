package com.honyrun.model.dto.response;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 图片转换结果响应DTO
 *
 * 用于返回图片转换操作的结果信息，包含转换状态、文件信息、处理详情等
 * 支持响应式数据传输和JSON序列化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  17:10:00
 * @modified 2025-07-01 17:10:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class ConversionResult {

    // ==================== 基本信息 ====================

    /**
     * 转换任务ID
     */
    private String taskId;

    /**
     * 转换状态
     * SUCCESS - 成功, FAILED - 失败, PROCESSING - 处理中
     */
    private String status;

    /**
     * 转换消息
     */
    private String message;

    /**
     * 原始文件名
     */
    private String originalFileName;

    /**
     * 转换后文件名
     */
    private String convertedFileName;

    // ==================== 文件信息 ====================

    /**
     * 原始文件大小（字节）
     */
    private Long originalFileSize;

    /**
     * 转换后文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 下载URL
     */
    private String downloadUrl;

    /**
     * 文件格式
     */
    private String format;

    // ==================== 图片信息 ====================

    /**
     * 原始图片宽度
     */
    private Integer originalWidth;

    /**
     * 原始图片高度
     */
    private Integer originalHeight;

    /**
     * 转换后图片宽度
     */
    private Integer width;

    /**
     * 转换后图片高度
     */
    private Integer height;

    /**
     * 图片质量
     */
    private Float quality;

    // ==================== 处理信息 ====================

    /**
     * 处理开始时间
     */
    private LocalDateTime startTime;

    /**
     * 处理结束时间
     */
    private LocalDateTime endTime;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTimeMs;

    /**
     * 压缩比例（百分比）
     */
    private Float compressionRatio;

    /**
     * 是否应用了水印
     */
    private Boolean watermarkApplied;

    /**
     * 是否进行了尺寸调整
     */
    private Boolean resized;

    /**
     * 是否进行了旋转
     */
    private Boolean rotated;

    /**
     * 错误信息（转换失败时）
     */
    private String errorMessage;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public ConversionResult() {
        this.status = "PROCESSING";
        this.startTime = LocalDateTime.now();
    }

    /**
     * 成功结果构造函数
     *
     * @param fileName 文件名
     * @param fileSize 文件大小
     */
    public ConversionResult(String fileName, Long fileSize) {
        this();
        this.convertedFileName = fileName;
        this.fileSize = fileSize;
        this.status = "SUCCESS";
        this.endTime = LocalDateTime.now();
    }

    /**
     * 失败结果构造函数
     *
     * @param errorMessage 错误信息
     */
    public ConversionResult(String errorMessage) {
        this();
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.message = "转换失败: " + errorMessage;
        this.endTime = LocalDateTime.now();
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getConvertedFileName() {
        return convertedFileName;
    }

    public void setConvertedFileName(String convertedFileName) {
        this.convertedFileName = convertedFileName;
    }

    public Long getOriginalFileSize() {
        return originalFileSize;
    }

    public void setOriginalFileSize(Long originalFileSize) {
        this.originalFileSize = originalFileSize;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getOriginalWidth() {
        return originalWidth;
    }

    public void setOriginalWidth(Integer originalWidth) {
        this.originalWidth = originalWidth;
    }

    public Integer getOriginalHeight() {
        return originalHeight;
    }

    public void setOriginalHeight(Integer originalHeight) {
        this.originalHeight = originalHeight;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Float getQuality() {
        return quality;
    }

    public void setQuality(Float quality) {
        this.quality = quality;
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

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public Float getCompressionRatio() {
        return compressionRatio;
    }

    public void setCompressionRatio(Float compressionRatio) {
        this.compressionRatio = compressionRatio;
    }

    public Boolean getWatermarkApplied() {
        return watermarkApplied;
    }

    public void setWatermarkApplied(Boolean watermarkApplied) {
        this.watermarkApplied = watermarkApplied;
    }

    public Boolean getResized() {
        return resized;
    }

    public void setResized(Boolean resized) {
        this.resized = resized;
    }

    public Boolean getRotated() {
        return rotated;
    }

    public void setRotated(Boolean rotated) {
        this.rotated = rotated;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // ==================== 业务方法 ====================

    /**
     * 是否转换成功
     *
     * @return 转换是否成功
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    /**
     * 是否转换失败
     *
     * @return 转换是否失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * 是否正在处理
     *
     * @return 是否正在处理
     */
    public boolean isProcessing() {
        return "PROCESSING".equals(status);
    }

    /**
     * 计算文件大小变化百分比
     *
     * @return 文件大小变化百分比（正数表示增大，负数表示减小）
     */
    public Float getFileSizeChangePercentage() {
        if (originalFileSize == null || fileSize == null || originalFileSize == 0) {
            return null;
        }
        return ((float) (fileSize - originalFileSize) / originalFileSize) * 100;
    }

    /**
     * 获取处理时间（秒）
     *
     * @return 处理时间（秒）
     */
    public Long getProcessingTimeSeconds() {
        if (processingTimeMs == null) {
            return null;
        }
        return processingTimeMs / 1000;
    }

    /**
     * 获取文件大小描述
     *
     * @return 文件大小描述
     */
    public String getFileSizeDescription() {
        if (fileSize == null) {
            return "未知";
        }
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    /**
     * 获取尺寸描述
     *
     * @return 尺寸描述
     */
    public String getDimensionDescription() {
        if (width == null || height == null) {
            return "未知";
        }
        return width + "x" + height;
    }

    /**
     * 完成转换处理
     *
     * @param success 是否成功
     * @param message 处理消息
     */
    public void completeProcessing(boolean success, String message) {
        this.status = success ? "SUCCESS" : "FAILED";
        this.message = message;
        this.endTime = LocalDateTime.now();

        if (startTime != null && endTime != null) {
            this.processingTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
        }

        if (originalFileSize != null && fileSize != null && originalFileSize > 0) {
            this.compressionRatio = ((float) (originalFileSize - fileSize) / originalFileSize) * 100;
        }
    }

    // ==================== Object方法重写 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConversionResult that = (ConversionResult) obj;
        return Objects.equals(taskId, that.taskId) &&
               Objects.equals(originalFileName, that.originalFileName) &&
               Objects.equals(convertedFileName, that.convertedFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, originalFileName, convertedFileName);
    }

    @Override
    public String toString() {
        return "ConversionResult{" +
                "taskId='" + taskId + '\'' +
                ", status='" + status + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                ", convertedFileName='" + convertedFileName + '\'' +
                ", fileSize=" + fileSize +
                ", width=" + width +
                ", height=" + height +
                ", processingTimeMs=" + processingTimeMs +
                '}';
    }
}


