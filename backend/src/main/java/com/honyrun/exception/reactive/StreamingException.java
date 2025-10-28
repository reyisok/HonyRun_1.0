package com.honyrun.exception.reactive;

import com.honyrun.exception.ErrorCode;

/**
 * 流处理异常类
 *
 * 处理响应式流处理中的异常情况
 * 包括流中断、数据转换错误、流合并异常等
 *
 * 主要功能：
 * - 流处理异常定义和处理
 * - 流状态和处理进度记录
 * - 数据转换异常管理
 * - 流合并和分割异常处理
 * - 流操作链异常跟踪
 *
 * 响应式特性：
 * - 流式处理：支持响应式流处理异常
 * - 数据流监控：记录数据流处理状态
 * - 操作链跟踪：跟踪流操作链异常
 * - 流恢复：支持流处理异常恢复
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 13:30:00
 * @modified 2025-07-01 13:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class StreamingException extends ReactiveException {

    private static final long serialVersionUID = 1L;

    /**
     * 缓冲区大小
     */
    private int bufferSize;

    /**
     * 已处理元素数量
     */
    private long processedElements;

    /**
     * 总元素数量
     */
    private long totalElements;

    /**
     * 流操作类型
     */
    private String operationType;

    /**
     * 流状态
     */
    private String streamState;

    /**
     * 失败的操作步骤
     */
    private String failedOperation;

    /**
     * 流处理开始时间
     */
    private long startTime;

    /**
     * 流处理结束时间
     */
    private long endTime;

    /**
     * 处理速率（元素/秒）
     */
    private double processingRate;

    // ==================== 构造方法 ====================

    /**
     * 默认构造方法
     */
    public StreamingException() {
        super();
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 带消息的构造方法
     *
     * @param message 异常消息
     */
    public StreamingException(String message) {
        super(message);
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 带消息和原因的构造方法
     *
     * @param message 异常消息
     * @param cause 原因异常
     */
    public StreamingException(String message, Throwable cause) {
        super(message, cause);
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 带处理信息的构造方法
     *
     * @param message 异常消息
     * @param operationType 操作类型
     * @param processedElements 已处理元素数量
     * @param totalElements 总元素数量
     */
    public StreamingException(String message, String operationType, long processedElements, long totalElements) {
        super(message);
        this.operationType = operationType;
        this.processedElements = processedElements;
        this.totalElements = totalElements;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 带缓冲区信息的构造方法
     *
     * @param message 异常消息
     * @param operationType 操作类型
     * @param bufferSize 缓冲区大小
     * @param processedElements 已处理元素数量
     */
    public StreamingException(String message, String operationType, int bufferSize, long processedElements) {
        super(message);
        this.operationType = operationType;
        this.bufferSize = bufferSize;
        this.processedElements = processedElements;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 完整构造方法
     *
     * @param errorCode 错误码
     * @param message 异常消息
     * @param cause 原因异常
     * @param path 请求路径
     * @param operationType 操作类型
     * @param bufferSize 缓冲区大小
     * @param processedElements 已处理元素数量
     * @param totalElements 总元素数量
     * @param streamState 流状态
     * @param failedOperation 失败的操作
     */
    public StreamingException(ErrorCode errorCode, String message, Throwable cause, String path,
                             String operationType, int bufferSize, long processedElements, long totalElements,
                             String streamState, String failedOperation) {
        super(errorCode, message, cause, path, "STREAMING");
        this.operationType = operationType;
        this.bufferSize = bufferSize;
        this.processedElements = processedElements;
        this.totalElements = totalElements;
        this.streamState = streamState;
        this.failedOperation = failedOperation;
        this.startTime = System.currentTimeMillis();
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取缓冲区大小
     *
     * @return 缓冲区大小
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * 设置缓冲区大小
     *
     * @param bufferSize 缓冲区大小
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * 获取已处理元素数量
     *
     * @return 已处理元素数量
     */
    public long getProcessedElements() {
        return processedElements;
    }

    /**
     * 设置已处理元素数量
     *
     * @param processedElements 已处理元素数量
     */
    public void setProcessedElements(long processedElements) {
        this.processedElements = processedElements;
    }

    /**
     * 获取总元素数量
     *
     * @return 总元素数量
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * 设置总元素数量
     *
     * @param totalElements 总元素数量
     */
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * 获取操作类型
     *
     * @return 操作类型
     */
    public String getOperationType() {
        return operationType;
    }

    /**
     * 设置操作类型
     *
     * @param operationType 操作类型
     */
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    /**
     * 获取流状态
     *
     * @return 流状态
     */
    public String getStreamState() {
        return streamState;
    }

    /**
     * 设置流状态
     *
     * @param streamState 流状态
     */
    public void setStreamState(String streamState) {
        this.streamState = streamState;
    }

    /**
     * 获取失败的操作
     *
     * @return 失败的操作
     */
    public String getFailedOperation() {
        return failedOperation;
    }

    /**
     * 设置失败的操作
     *
     * @param failedOperation 失败的操作
     */
    public void setFailedOperation(String failedOperation) {
        this.failedOperation = failedOperation;
    }

    /**
     * 获取开始时间
     *
     * @return 开始时间
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * 设置开始时间
     *
     * @param startTime 开始时间
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * 获取结束时间
     *
     * @return 结束时间
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * 设置结束时间
     *
     * @param endTime 结束时间
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * 获取处理速率
     *
     * @return 处理速率
     */
    public double getProcessingRate() {
        return processingRate;
    }

    /**
     * 设置处理速率
     *
     * @param processingRate 处理速率
     */
    public void setProcessingRate(double processingRate) {
        this.processingRate = processingRate;
    }

    // ==================== 工具方法 ====================

    /**
     * 创建流中断异常
     *
     * @param operationType 操作类型
     * @param processedElements 已处理元素数量
     * @return 流处理异常实例
     */
    public static StreamingException streamInterrupted(String operationType, long processedElements) {
        return new StreamingException(
            String.format("流处理中断，操作: %s, 已处理: %d 个元素", operationType, processedElements),
            operationType, processedElements, -1
        );
    }

    /**
     * 创建数据转换异常
     *
     * @param operationType 操作类型
     * @param element 转换失败的元素
     * @param cause 原因异常
     * @return 流处理异常实例
     */
    public static StreamingException dataTransformationFailed(String operationType, Object element, Throwable cause) {
        StreamingException exception = new StreamingException(
            String.format("数据转换失败，操作: %s, 元素: %s", operationType, element),
            cause
        );
        exception.setOperationType(operationType);
        exception.setFailedOperation("DATA_TRANSFORMATION");
        return exception;
    }

    /**
     * 创建流合并异常
     *
     * @param streamCount 流数量
     * @param cause 原因异常
     * @return 流处理异常实例
     */
    public static StreamingException streamMergeFailed(int streamCount, Throwable cause) {
        StreamingException exception = new StreamingException(
            String.format("流合并失败，流数量: %d", streamCount),
            cause
        );
        exception.setOperationType("STREAM_MERGE");
        exception.setFailedOperation("MERGE");
        return exception;
    }

    /**
     * 创建缓冲区溢出异常
     *
     * @param bufferSize 缓冲区大小
     * @param operationType 操作类型
     * @return 流处理异常实例
     */
    public static StreamingException bufferOverflow(int bufferSize, String operationType) {
        StreamingException exception = new StreamingException(
            String.format("缓冲区溢出，大小: %d, 操作: %s", bufferSize, operationType)
        );
        exception.setBufferSize(bufferSize);
        exception.setOperationType(operationType);
        exception.setFailedOperation("BUFFER_OVERFLOW");
        return exception;
    }

    /**
     * 创建流超时异常
     *
     * @param operationType 操作类型
     * @param timeoutMs 超时时间（毫秒）
     * @return 流处理异常实例
     */
    public static StreamingException streamTimeout(String operationType, long timeoutMs) {
        StreamingException exception = new StreamingException(
            String.format("流处理超时，操作: %s, 超时时间: %d ms", operationType, timeoutMs)
        );
        exception.setOperationType(operationType);
        exception.setFailedOperation("TIMEOUT");
        return exception;
    }

    // ==================== 状态计算方法 ====================

    /**
     * 计算处理进度
     *
     * @return 处理进度（百分比）
     */
    public double getProcessingProgress() {
        if (totalElements <= 0) {
            return 0.0;
        }
        return (double) processedElements / totalElements * 100;
    }

    /**
     * 计算处理时长
     *
     * @return 处理时长（毫秒）
     */
    public long getProcessingDuration() {
        if (endTime > 0) {
            return endTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 计算实际处理速率
     *
     * @return 处理速率（元素/秒）
     */
    public double calculateActualProcessingRate() {
        long duration = getProcessingDuration();
        if (duration <= 0) {
            return 0.0;
        }
        return (double) processedElements / (duration / 1000.0);
    }

    /**
     * 检查是否处理完成
     *
     * @return 是否处理完成
     */
    public boolean isProcessingComplete() {
        return totalElements > 0 && processedElements >= totalElements;
    }

    /**
     * 获取剩余元素数量
     *
     * @return 剩余元素数量
     */
    public long getRemainingElements() {
        if (totalElements <= 0) {
            return -1;
        }
        return Math.max(0, totalElements - processedElements);
    }

    // ==================== 重写方法 ====================

    /**
     * 重写toString方法
     *
     * @return 异常字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("StreamingException{");
        sb.append("message='").append(getMessage()).append('\'');
        sb.append(", operationType='").append(operationType).append('\'');
        sb.append(", bufferSize=").append(bufferSize);
        sb.append(", processedElements=").append(processedElements);
        sb.append(", totalElements=").append(totalElements);
        sb.append(", streamState='").append(streamState).append('\'');
        sb.append(", failedOperation='").append(failedOperation).append('\'');
        sb.append(", processingProgress=").append(String.format("%.2f%%", getProcessingProgress()));
        sb.append(", processingDuration=").append(getProcessingDuration()).append("ms");
        sb.append(", path='").append(getPath()).append('\'');
        sb.append(", timestamp=").append(getTimestamp());
        sb.append('}');
        return sb.toString();
    }
}

