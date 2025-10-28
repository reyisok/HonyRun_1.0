package com.honyrun.exception.reactive;

import com.honyrun.exception.ErrorCode;

/**
 * 背压异常类
 *
 * 处理响应式流中的背压相关异常
 * 当生产者速度超过消费者处理能力时抛出
 *
 * 主要功能：
 * - 背压异常定义和处理
 * - 队列大小和缓冲区状态记录
 * - 背压策略信息封装
 * - 流量控制异常处理
 * - 系统负载异常管理
 *
 * 响应式特性：
 * - 背压控制：支持响应式流背压机制
 * - 流量管理：记录流量控制相关信息
 * - 缓冲区监控：监控缓冲区状态和容量
 * - 负载均衡：支持负载均衡异常处理
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 13:00:00
 * @modified 2025-07-01 13:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class BackpressureException extends ReactiveException {

    private static final long serialVersionUID = 1L;

    /**
     * 队列大小
     */
    private int queueSize;

    /**
     * 最大队列容量
     */
    private int maxQueueCapacity;

    /**
     * 当前缓冲区大小
     */
    private int bufferSize;

    /**
     * 最大缓冲区容量
     */
    private int maxBufferCapacity;

    /**
     * 背压策略
     */
    private String backpressureStrategy;

    /**
     * 生产者速率（每秒元素数）
     */
    private long producerRate;

    /**
     * 消费者速率（每秒元素数）
     */
    private long consumerRate;

    // ==================== 构造方法 ====================

    /**
     * 默认构造方法
     */
    public BackpressureException() {
        super();
    }

    /**
     * 带消息的构造方法
     *
     * @param message 异常消息
     */
    public BackpressureException(String message) {
        super(message);
    }

    /**
     * 带消息和原因的构造方法
     *
     * @param message 异常消息
     * @param cause 原因异常
     */
    public BackpressureException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 带队列信息的构造方法
     *
     * @param message 异常消息
     * @param queueSize 当前队列大小
     * @param maxQueueCapacity 最大队列容量
     */
    public BackpressureException(String message, int queueSize, int maxQueueCapacity) {
        super(message);
        this.queueSize = queueSize;
        this.maxQueueCapacity = maxQueueCapacity;
    }

    /**
     * 带缓冲区信息的构造方法
     *
     * @param message 异常消息
     * @param queueSize 当前队列大小
     * @param maxQueueCapacity 最大队列容量
     * @param bufferSize 当前缓冲区大小
     * @param maxBufferCapacity 最大缓冲区容量
     */
    public BackpressureException(String message, int queueSize, int maxQueueCapacity, 
                                int bufferSize, int maxBufferCapacity) {
        super(message);
        this.queueSize = queueSize;
        this.maxQueueCapacity = maxQueueCapacity;
        this.bufferSize = bufferSize;
        this.maxBufferCapacity = maxBufferCapacity;
    }

    /**
     * 完整构造方法
     *
     * @param errorCode 错误码
     * @param message 异常消息
     * @param cause 原因异常
     * @param path 请求路径
     * @param queueSize 当前队列大小
     * @param maxQueueCapacity 最大队列容量
     * @param bufferSize 当前缓冲区大小
     * @param maxBufferCapacity 最大缓冲区容量
     * @param backpressureStrategy 背压策略
     * @param producerRate 生产者速率
     * @param consumerRate 消费者速率
     */
    public BackpressureException(ErrorCode errorCode, String message, Throwable cause, String path,
                                int queueSize, int maxQueueCapacity, int bufferSize, int maxBufferCapacity,
                                String backpressureStrategy, long producerRate, long consumerRate) {
        super(errorCode, message, cause, path, "BACKPRESSURE");
        this.queueSize = queueSize;
        this.maxQueueCapacity = maxQueueCapacity;
        this.bufferSize = bufferSize;
        this.maxBufferCapacity = maxBufferCapacity;
        this.backpressureStrategy = backpressureStrategy;
        this.producerRate = producerRate;
        this.consumerRate = consumerRate;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取队列大小
     *
     * @return 队列大小
     */
    public int getQueueSize() {
        return queueSize;
    }

    /**
     * 设置队列大小
     *
     * @param queueSize 队列大小
     */
    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    /**
     * 获取最大队列容量
     *
     * @return 最大队列容量
     */
    public int getMaxQueueCapacity() {
        return maxQueueCapacity;
    }

    /**
     * 设置最大队列容量
     *
     * @param maxQueueCapacity 最大队列容量
     */
    public void setMaxQueueCapacity(int maxQueueCapacity) {
        this.maxQueueCapacity = maxQueueCapacity;
    }

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
     * 获取最大缓冲区容量
     *
     * @return 最大缓冲区容量
     */
    public int getMaxBufferCapacity() {
        return maxBufferCapacity;
    }

    /**
     * 设置最大缓冲区容量
     *
     * @param maxBufferCapacity 最大缓冲区容量
     */
    public void setMaxBufferCapacity(int maxBufferCapacity) {
        this.maxBufferCapacity = maxBufferCapacity;
    }

    /**
     * 获取背压策略
     *
     * @return 背压策略
     */
    public String getBackpressureStrategy() {
        return backpressureStrategy;
    }

    /**
     * 设置背压策略
     *
     * @param backpressureStrategy 背压策略
     */
    public void setBackpressureStrategy(String backpressureStrategy) {
        this.backpressureStrategy = backpressureStrategy;
    }

    /**
     * 获取生产者速率
     *
     * @return 生产者速率
     */
    public long getProducerRate() {
        return producerRate;
    }

    /**
     * 设置生产者速率
     *
     * @param producerRate 生产者速率
     */
    public void setProducerRate(long producerRate) {
        this.producerRate = producerRate;
    }

    /**
     * 获取消费者速率
     *
     * @return 消费者速率
     */
    public long getConsumerRate() {
        return consumerRate;
    }

    /**
     * 设置消费者速率
     *
     * @param consumerRate 消费者速率
     */
    public void setConsumerRate(long consumerRate) {
        this.consumerRate = consumerRate;
    }

    // ==================== 工具方法 ====================

    /**
     * 创建队列满异常
     *
     * @param queueSize 当前队列大小
     * @param maxCapacity 最大容量
     * @return 背压异常实例
     */
    public static BackpressureException queueFull(int queueSize, int maxCapacity) {
        return new BackpressureException(
            String.format("队列已满，当前大小: %d, 最大容量: %d", queueSize, maxCapacity),
            queueSize, maxCapacity
        );
    }

    /**
     * 创建缓冲区溢出异常
     *
     * @param bufferSize 当前缓冲区大小
     * @param maxCapacity 最大容量
     * @return 背压异常实例
     */
    public static BackpressureException bufferOverflow(int bufferSize, int maxCapacity) {
        BackpressureException exception = new BackpressureException(
            String.format("缓冲区溢出，当前大小: %d, 最大容量: %d", bufferSize, maxCapacity)
        );
        exception.setBufferSize(bufferSize);
        exception.setMaxBufferCapacity(maxCapacity);
        return exception;
    }

    /**
     * 创建生产者过快异常
     *
     * @param producerRate 生产者速率
     * @param consumerRate 消费者速率
     * @return 背压异常实例
     */
    public static BackpressureException producerTooFast(long producerRate, long consumerRate) {
        BackpressureException exception = new BackpressureException(
            String.format("生产者速度过快，生产速率: %d/s, 消费速率: %d/s", producerRate, consumerRate)
        );
        exception.setProducerRate(producerRate);
        exception.setConsumerRate(consumerRate);
        return exception;
    }

    /**
     * 创建背压策略失败异常
     *
     * @param strategy 背压策略
     * @param reason 失败原因
     * @return 背压异常实例
     */
    public static BackpressureException strategyFailed(String strategy, String reason) {
        BackpressureException exception = new BackpressureException(
            String.format("背压策略失败，策略: %s, 原因: %s", strategy, reason)
        );
        exception.setBackpressureStrategy(strategy);
        return exception;
    }

    // ==================== 状态检查方法 ====================

    /**
     * 检查队列是否已满
     *
     * @return 队列是否已满
     */
    public boolean isQueueFull() {
        return maxQueueCapacity > 0 && queueSize >= maxQueueCapacity;
    }

    /**
     * 检查缓冲区是否溢出
     *
     * @return 缓冲区是否溢出
     */
    public boolean isBufferOverflow() {
        return maxBufferCapacity > 0 && bufferSize >= maxBufferCapacity;
    }

    /**
     * 计算队列使用率
     *
     * @return 队列使用率（百分比）
     */
    public double getQueueUsageRatio() {
        if (maxQueueCapacity <= 0) {
            return 0.0;
        }
        return (double) queueSize / maxQueueCapacity * 100;
    }

    /**
     * 计算缓冲区使用率
     *
     * @return 缓冲区使用率（百分比）
     */
    public double getBufferUsageRatio() {
        if (maxBufferCapacity <= 0) {
            return 0.0;
        }
        return (double) bufferSize / maxBufferCapacity * 100;
    }

    /**
     * 计算速率差异
     *
     * @return 生产者和消费者速率差异
     */
    public long getRateDifference() {
        return producerRate - consumerRate;
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
        sb.append("BackpressureException{");
        sb.append("message='").append(getMessage()).append('\'');
        sb.append(", queueSize=").append(queueSize);
        sb.append(", maxQueueCapacity=").append(maxQueueCapacity);
        sb.append(", bufferSize=").append(bufferSize);
        sb.append(", maxBufferCapacity=").append(maxBufferCapacity);
        sb.append(", backpressureStrategy='").append(backpressureStrategy).append('\'');
        sb.append(", producerRate=").append(producerRate);
        sb.append(", consumerRate=").append(consumerRate);
        sb.append(", path='").append(getPath()).append('\'');
        sb.append(", timestamp=").append(getTimestamp());
        sb.append('}');
        return sb.toString();
    }
}

