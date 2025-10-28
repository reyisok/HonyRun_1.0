package com.honyrun.model.dto.image;

import java.time.LocalDateTime;

/**
 * 图片转换响应DTO
 * 封装图片转换操作的响应结果
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 响应信息:
 * - 转换结果状态
 * - 输出文件信息
 * - 处理统计数据
 * - 错误信息记录
 */
public class ImageConversionResponse {

    /**
     * 转换是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 原始文件名
     */
    private String originalFileName;

    /**
     * 输出文件名
     */
    private String outputFileName;

    /**
     * 原始文件格式
     */
    private String originalFormat;

    /**
     * 输出文件格式
     */
    private String outputFormat;

    /**
     * 原始文件大小（字节）
     */
    private Long originalFileSize;

    /**
     * 输出文件大小（字节）
     */
    private Long outputFileSize;

    /**
     * 文件大小变化百分比
     * 正数表示增大，负数表示减小
     */
    private Double sizeChangePercent;

    /**
     * 原始图片宽度
     */
    private Integer originalWidth;

    /**
     * 原始图片高度
     */
    private Integer originalHeight;

    /**
     * 输出图片宽度
     */
    private Integer outputWidth;

    /**
     * 输出图片高度
     */
    private Integer outputHeight;

    /**
     * 转换开始时间
     */
    private LocalDateTime startTime;

    /**
     * 转换结束时间
     */
    private LocalDateTime endTime;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTimeMs;

    /**
     * 应用的质量设置
     */
    private Float appliedQuality;

    /**
     * 是否进行了尺寸调整
     */
    private Boolean resized;

    /**
     * 是否进行了旋转
     */
    private Boolean rotated;

    /**
     * 应用的旋转角度
     */
    private Integer appliedRotationAngle;

    /**
     * 是否进行了翻转
     */
    private Boolean flipped;

    /**
     * 是否添加了水印
     */
    private Boolean watermarked;

    /**
     * 是否进行了优化
     */
    private Boolean optimized;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误详情
     */
    private String errorDetails;

    /**
     * 文件下载URL（如果适用）
     */
    private String downloadUrl;

    /**
     * 文件访问令牌（如果适用）
     */
    private String accessToken;

    /**
     * 令牌过期时间
     */
    private LocalDateTime tokenExpireTime;

    /**
     * 批处理模式下的处理索引
     */
    private Integer batchIndex;

    /**
     * 批处理总数
     */
    private Integer batchTotal;

    // 构造函数
    public ImageConversionResponse() {
        this.success = false;
        this.startTime = LocalDateTime.now();
    }

    public ImageConversionResponse(Boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }

    // 静态工厂方法
    public static ImageConversionResponse success(String message) {
        ImageConversionResponse response = new ImageConversionResponse(true, message);
        response.endTime = LocalDateTime.now();
        return response;
    }

    public static ImageConversionResponse failure(String message, String errorCode) {
        ImageConversionResponse response = new ImageConversionResponse(false, message);
        response.errorCode = errorCode;
        response.endTime = LocalDateTime.now();
        return response;
    }

    // Getter和Setter方法
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
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

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public String getOriginalFormat() {
        return originalFormat;
    }

    public void setOriginalFormat(String originalFormat) {
        this.originalFormat = originalFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Long getOriginalFileSize() {
        return originalFileSize;
    }

    public void setOriginalFileSize(Long originalFileSize) {
        this.originalFileSize = originalFileSize;
    }

    public Long getOutputFileSize() {
        return outputFileSize;
    }

    public void setOutputFileSize(Long outputFileSize) {
        this.outputFileSize = outputFileSize;
    }

    public Double getSizeChangePercent() {
        return sizeChangePercent;
    }

    public void setSizeChangePercent(Double sizeChangePercent) {
        this.sizeChangePercent = sizeChangePercent;
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

    public Integer getOutputWidth() {
        return outputWidth;
    }

    public void setOutputWidth(Integer outputWidth) {
        this.outputWidth = outputWidth;
    }

    public Integer getOutputHeight() {
        return outputHeight;
    }

    public void setOutputHeight(Integer outputHeight) {
        this.outputHeight = outputHeight;
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
        // 自动计算处理时间
        if (this.startTime != null && endTime != null) {
            this.processingTimeMs = java.time.Duration.between(this.startTime, endTime).toMillis();
        }
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public Float getAppliedQuality() {
        return appliedQuality;
    }

    public void setAppliedQuality(Float appliedQuality) {
        this.appliedQuality = appliedQuality;
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

    public Integer getAppliedRotationAngle() {
        return appliedRotationAngle;
    }

    public void setAppliedRotationAngle(Integer appliedRotationAngle) {
        this.appliedRotationAngle = appliedRotationAngle;
    }

    public Boolean getFlipped() {
        return flipped;
    }

    public void setFlipped(Boolean flipped) {
        this.flipped = flipped;
    }

    public Boolean getWatermarked() {
        return watermarked;
    }

    public void setWatermarked(Boolean watermarked) {
        this.watermarked = watermarked;
    }

    public Boolean getOptimized() {
        return optimized;
    }

    public void setOptimized(Boolean optimized) {
        this.optimized = optimized;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public LocalDateTime getTokenExpireTime() {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(LocalDateTime tokenExpireTime) {
        this.tokenExpireTime = tokenExpireTime;
    }

    public Integer getBatchIndex() {
        return batchIndex;
    }

    public void setBatchIndex(Integer batchIndex) {
        this.batchIndex = batchIndex;
    }

    public Integer getBatchTotal() {
        return batchTotal;
    }

    public void setBatchTotal(Integer batchTotal) {
        this.batchTotal = batchTotal;
    }

    /**
     * 计算文件大小变化百分比
     */
    public void calculateSizeChangePercent() {
        if (originalFileSize != null && outputFileSize != null && originalFileSize > 0) {
            double change = ((double) (outputFileSize - originalFileSize) / originalFileSize) * 100;
            this.sizeChangePercent = Math.round(change * 100.0) / 100.0; // 保留两位小数
        }
    }

    /**
     * 获取格式化的文件大小
     */
    public String getFormattedOriginalFileSize() {
        return formatFileSize(originalFileSize);
    }

    public String getFormattedOutputFileSize() {
        return formatFileSize(outputFileSize);
    }

    /**
     * 获取格式化的处理时间
     */
    public String getFormattedProcessingTime() {
        if (processingTimeMs == null) {
            return "未知";
        }
        if (processingTimeMs < 1000) {
            return processingTimeMs + "ms";
        } else {
            return String.format("%.2fs", processingTimeMs / 1000.0);
        }
    }

    /**
     * 是否为批处理模式
     */
    public boolean isBatchMode() {
        return batchIndex != null && batchTotal != null;
    }

    /**
     * 获取批处理进度
     */
    public String getBatchProgress() {
        if (!isBatchMode()) {
            return null;
        }
        return String.format("%d/%d", batchIndex, batchTotal);
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null) {
            return "未知";
        }
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2fKB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2fMB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2fGB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return "ImageConversionResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                ", outputFileName='" + outputFileName + '\'' +
                ", originalFormat='" + originalFormat + '\'' +
                ", outputFormat='" + outputFormat + '\'' +
                ", originalFileSize=" + originalFileSize +
                ", outputFileSize=" + outputFileSize +
                ", sizeChangePercent=" + sizeChangePercent +
                ", originalWidth=" + originalWidth +
                ", originalHeight=" + originalHeight +
                ", outputWidth=" + outputWidth +
                ", outputHeight=" + outputHeight +
                ", processingTimeMs=" + processingTimeMs +
                ", appliedQuality=" + appliedQuality +
                ", resized=" + resized +
                ", rotated=" + rotated +
                ", appliedRotationAngle=" + appliedRotationAngle +
                ", flipped=" + flipped +
                ", watermarked=" + watermarked +
                ", optimized=" + optimized +
                ", errorCode='" + errorCode + '\'' +
                ", batchIndex=" + batchIndex +
                ", batchTotal=" + batchTotal +
                '}';
    }
}

