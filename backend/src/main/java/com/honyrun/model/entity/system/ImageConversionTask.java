package com.honyrun.model.entity.system;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 图片转换任务实体类
 *
 * 用于存储图片转换任务信息，支持R2DBC响应式数据访问。
 * 该实体管理图片处理任务的状态、进度和结果信息。
 *
 * 特性：
 * - 支持R2DBC响应式数据访问
 * - 继承审计功能
 * - 任务状态管理
 * - 进度跟踪支持
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  16:35:00
 * @modified 2025-07-01 16:35:00
 * @version 2.0.0
 */
@Table("sys_image_conversion_task")
public class ImageConversionTask extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 任务名称
     * 图片转换任务的显示名称
     */
    @Column("task_name")
    private String taskName;

    /**
     * 任务类型
     * CONVERT-格式转换, RESIZE-尺寸调整, COMPRESS-压缩, WATERMARK-水印, BATCH-批量处理
     */
    @Column("task_type")
    private TaskType taskType;

    /**
     * 任务状态
     * PENDING-待处理, PROCESSING-处理中, COMPLETED-已完成, FAILED-失败, CANCELLED-已取消
     */
    @Column("task_status")
    private TaskStatus taskStatus = TaskStatus.PENDING;

    /**
     * 源文件路径
     * 原始图片文件的路径
     */
    @Column("source_path")
    private String sourcePath;

    /**
     * 目标文件路径
     * 转换后图片文件的路径
     */
    @Column("target_path")
    private String targetPath;

    /**
     * 源文件格式
     * 原始图片的格式（JPG、PNG、GIF等）
     */
    @Column("source_format")
    private String sourceFormat;

    /**
     * 目标文件格式
     * 转换后图片的格式
     */
    @Column("target_format")
    private String targetFormat;

    /**
     * 源文件大小（字节）
     * 原始图片文件的大小
     */
    @Column("source_size")
    private Long sourceSize;

    /**
     * 目标文件大小（字节）
     * 转换后图片文件的大小
     */
    @Column("target_size")
    private Long targetSize;

    /**
     * 源图片宽度
     * 原始图片的宽度（像素）
     */
    @Column("source_width")
    private Integer sourceWidth;

    /**
     * 源图片高度
     * 原始图片的高度（像素）
     */
    @Column("source_height")
    private Integer sourceHeight;

    /**
     * 目标图片宽度
     * 转换后图片的宽度（像素）
     */
    @Column("target_width")
    private Integer targetWidth;

    /**
     * 目标图片高度
     * 转换后图片的高度（像素）
     */
    @Column("target_height")
    private Integer targetHeight;

    /**
     * 质量设置
     * 图片质量设置（0-100）
     */
    @Column("quality")
    private Integer quality;

    /**
     * 转换参数
     * 转换时使用的参数配置（JSON格式）
     */
    @Column("conversion_params")
    private String conversionParams;

    /**
     * 进度百分比
     * 任务完成进度（0-100）
     */
    @Column("progress_percentage")
    private Integer progressPercentage = 0;

    /**
     * 开始时间
     * 任务开始处理的时间
     */
    @Column("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     * 任务完成的时间
     */
    @Column("end_time")
    private LocalDateTime endTime;

    /**
     * 处理耗时（毫秒）
     * 任务处理的总耗时
     */
    @Column("processing_duration")
    private Long processingDuration;

    /**
     * 错误信息
     * 任务失败时的错误信息
     */
    @Column("error_message")
    private String errorMessage;

    /**
     * 错误堆栈
     * 任务失败时的详细错误堆栈
     */
    @Column("error_stack")
    private String errorStack;

    /**
     * 批次ID
     * 批量处理时的批次标识
     */
    @Column("batch_id")
    private String batchId;

    /**
     * 批次序号
     * 在批次中的序号
     */
    @Column("batch_sequence")
    private Integer batchSequence;

    /**
     * 优先级
     * 任务处理的优先级（1-10，数值越大优先级越高）
     */
    @Column("priority")
    private Integer priority = 5;

    /**
     * 重试次数
     * 任务失败后的重试次数
     */
    @Column("retry_count")
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     * 任务允许的最大重试次数
     */
    @Column("max_retry_count")
    private Integer maxRetryCount = 3;

    /**
     * 是否自动重试
     * 标识任务失败后是否自动重试
     */
    @Column("auto_retry")
    private Boolean autoRetry = true;

    /**
     * 备注信息
     * 任务的备注说明
     */
    @Column("remarks")
    private String remarks;

    /**
     * 默认构造函数
     */
    public ImageConversionTask() {
        super();
    }

    /**
     * 带参数的构造函数
     *
     * @param taskName 任务名称
     * @param taskType 任务类型
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     */
    public ImageConversionTask(String taskName, TaskType taskType, String sourcePath, String targetPath) {
        super();
        this.taskName = taskName;
        this.taskType = taskType;
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
    }

    // Getter and Setter methods

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getSourceFormat() {
        return sourceFormat;
    }

    public void setSourceFormat(String sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    public String getTargetFormat() {
        return targetFormat;
    }

    public void setTargetFormat(String targetFormat) {
        this.targetFormat = targetFormat;
    }

    public Long getSourceSize() {
        return sourceSize;
    }

    public void setSourceSize(Long sourceSize) {
        this.sourceSize = sourceSize;
    }

    public Long getTargetSize() {
        return targetSize;
    }

    public void setTargetSize(Long targetSize) {
        this.targetSize = targetSize;
    }

    public Integer getSourceWidth() {
        return sourceWidth;
    }

    public void setSourceWidth(Integer sourceWidth) {
        this.sourceWidth = sourceWidth;
    }

    public Integer getSourceHeight() {
        return sourceHeight;
    }

    public void setSourceHeight(Integer sourceHeight) {
        this.sourceHeight = sourceHeight;
    }

    public Integer getTargetWidth() {
        return targetWidth;
    }

    public void setTargetWidth(Integer targetWidth) {
        this.targetWidth = targetWidth;
    }

    public Integer getTargetHeight() {
        return targetHeight;
    }

    public void setTargetHeight(Integer targetHeight) {
        this.targetHeight = targetHeight;
    }

    public Integer getQuality() {
        return quality;
    }

    public void setQuality(Integer quality) {
        this.quality = quality;
    }

    public String getConversionParams() {
        return conversionParams;
    }

    public void setConversionParams(String conversionParams) {
        this.conversionParams = conversionParams;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
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
        // 自动计算处理耗时
        if (this.startTime != null && endTime != null) {
            this.processingDuration = java.time.Duration.between(this.startTime, endTime).toMillis();
        }
    }

    public Long getProcessingDuration() {
        return processingDuration;
    }

    public void setProcessingDuration(Long processingDuration) {
        this.processingDuration = processingDuration;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorStack() {
        return errorStack;
    }

    public void setErrorStack(String errorStack) {
        this.errorStack = errorStack;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Integer getBatchSequence() {
        return batchSequence;
    }

    public void setBatchSequence(Integer batchSequence) {
        this.batchSequence = batchSequence;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public Boolean getAutoRetry() {
        return autoRetry;
    }

    public void setAutoRetry(Boolean autoRetry) {
        this.autoRetry = autoRetry;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    // Business methods

    /**
     * 判断任务是否完成
     *
     * @return 如果完成返回true，否则返回false
     */
    public boolean isCompleted() {
        return this.taskStatus == TaskStatus.COMPLETED;
    }

    /**
     * 判断任务是否失败
     *
     * @return 如果失败返回true，否则返回false
     */
    public boolean isFailed() {
        return this.taskStatus == TaskStatus.FAILED;
    }

    /**
     * 判断任务是否正在处理
     *
     * @return 如果正在处理返回true，否则返回false
     */
    public boolean isProcessing() {
        return this.taskStatus == TaskStatus.PROCESSING;
    }

    /**
     * 判断是否可以重试
     *
     * @return 如果可以重试返回true，否则返回false
     */
    public boolean canRetry() {
        return Boolean.TRUE.equals(this.autoRetry) &&
               this.retryCount != null &&
               this.maxRetryCount != null &&
               this.retryCount < this.maxRetryCount;
    }

    /**
     * 判断是否为批量任务
     *
     * @return 如果是批量任务返回true，否则返回false
     */
    public boolean isBatchTask() {
        return this.batchId != null && !this.batchId.trim().isEmpty();
    }

    /**
     * 开始处理任务
     */
    public void startProcessing() {
        this.taskStatus = TaskStatus.PROCESSING;
        this.startTime = LocalDateTime.now();
        this.progressPercentage = 0;
    }

    /**
     * 完成任务
     */
    public void completeTask() {
        this.taskStatus = TaskStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.progressPercentage = 100;
        if (this.startTime != null) {
            this.processingDuration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }

    /**
     * 任务失败
     *
     * @param errorMessage 错误信息
     * @param errorStack 错误堆栈
     */
    public void failTask(String errorMessage, String errorStack) {
        this.taskStatus = TaskStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.errorStack = errorStack;
        if (this.startTime != null) {
            this.processingDuration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }

    /**
     * 取消任务
     */
    public void cancelTask() {
        this.taskStatus = TaskStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.processingDuration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }

    /**
     * 重试任务
     */
    public void retryTask() {
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        this.retryCount++;
        this.taskStatus = TaskStatus.PENDING;
        this.errorMessage = null;
        this.errorStack = null;
        this.progressPercentage = 0;
    }

    /**
     * 更新进度
     *
     * @param percentage 进度百分比
     */
    public void updateProgress(Integer percentage) {
        if (percentage != null && percentage >= 0 && percentage <= 100) {
            this.progressPercentage = percentage;
        }
    }

    /**
     * 计算压缩率
     *
     * @return 压缩率百分比
     */
    public double getCompressionRatio() {
        if (sourceSize == null || targetSize == null || sourceSize == 0) {
            return 0.0;
        }
        return (1.0 - (double) targetSize / sourceSize) * 100;
    }

    /**
     * 获取格式化的处理耗时
     *
     * @return 格式化的处理耗时字符串
     */
    public String getFormattedProcessingDuration() {
        if (processingDuration == null) {
            return "未知";
        }

        long seconds = processingDuration / 1000;
        long milliseconds = processingDuration % 1000;

        if (seconds > 0) {
            return String.format("%d.%03d秒", seconds, milliseconds);
        } else {
            return String.format("%d毫秒", milliseconds);
        }
    }

    /**
     * 任务类型枚举
     */
    public enum TaskType {
        /** 格式转换 */
        CONVERT("格式转换"),
        /** 尺寸调整 */
        RESIZE("尺寸调整"),
        /** 压缩 */
        COMPRESS("压缩"),
        /** 水印 */
        WATERMARK("水印"),
        /** 批量处理 */
        BATCH("批量处理");

        private final String description;

        TaskType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        /** 待处理 */
        PENDING("待处理"),
        /** 处理中 */
        PROCESSING("处理中"),
        /** 已完成 */
        COMPLETED("已完成"),
        /** 失败 */
        FAILED("失败"),
        /** 已取消 */
        CANCELLED("已取消");

        private final String description;

        TaskStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return String.format("ImageConversionTask{id=%d, name='%s', type=%s, status=%s, progress=%d%%, duration=%s}",
                getId(), taskName, taskType, taskStatus, progressPercentage, getFormattedProcessingDuration());
    }
}


