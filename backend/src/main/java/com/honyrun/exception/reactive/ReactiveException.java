package com.honyrun.exception.reactive;

import com.honyrun.exception.ErrorCode;

/**
 * 响应式异常基类
 *
 * 定义响应式流处理中的异常基类
 * 支持响应式流异常处理和错误传播
 *
 * 主要功能：
 * - 响应式流异常定义
 * - 异常信息封装
 * - 错误码管理
 * - 异常链传播
 * - 路径信息记录
 *
 * 响应式特性：
 * - 非阻塞异常传播：支持响应式流中的异常传播
 * - 流式错误处理：与响应式流错误处理机制集成
 * - 背压异常：支持背压相关异常处理
 * - 流中断异常：支持流处理中断异常
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 12:30:00
 * @modified 2025-07-01 12:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class ReactiveException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private ErrorCode errorCode;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 流处理上下文
     */
    private String streamContext;

    /**
     * 异常发生时间戳
     */
    private long timestamp;

    // ==================== 构造方法 ====================

    /**
     * 默认构造方法
     */
    public ReactiveException() {
        super();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 带消息的构造方法
     *
     * @param message 异常消息
     */
    public ReactiveException(String message) {
        super(message);
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 带消息和原因的构造方法
     *
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ReactiveException(String message, Throwable cause) {
        super(message, cause);
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 带错误码的构造方法
     *
     * @param errorCode 错误码
     * @param message 异常消息
     */
    public ReactiveException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 带错误码和原因的构造方法
     *
     * @param errorCode 错误码
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ReactiveException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 完整构造方法
     *
     * @param errorCode 错误码
     * @param message 异常消息
     * @param cause 原因异常
     * @param path 请求路径
     * @param streamContext 流处理上下文
     */
    public ReactiveException(ErrorCode errorCode, String message, Throwable cause, String path, String streamContext) {
        super(message, cause);
        this.errorCode = errorCode;
        this.path = path;
        this.streamContext = streamContext;
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 设置错误码
     *
     * @param errorCode 错误码
     */
    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 获取请求路径
     *
     * @return 请求路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置请求路径
     *
     * @param path 请求路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取流处理上下文
     *
     * @return 流处理上下文
     */
    public String getStreamContext() {
        return streamContext;
    }

    /**
     * 设置流处理上下文
     *
     * @param streamContext 流处理上下文
     */
    public void setStreamContext(String streamContext) {
        this.streamContext = streamContext;
    }

    /**
     * 获取异常发生时间戳
     *
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置异常发生时间戳
     *
     * @param timestamp 时间戳
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // ==================== 工具方法 ====================

    /**
     * 创建响应式异常
     *
     * @param message 异常消息
     * @return 响应式异常实例
     */
    public static ReactiveException create(String message) {
        return new ReactiveException(message);
    }

    /**
     * 创建带错误码的响应式异常
     *
     * @param errorCode 错误码
     * @param message 异常消息
     * @return 响应式异常实例
     */
    public static ReactiveException create(ErrorCode errorCode, String message) {
        return new ReactiveException(errorCode, message);
    }

    /**
     * 创建带原因的响应式异常
     *
     * @param message 异常消息
     * @param cause 原因异常
     * @return 响应式异常实例
     */
    public static ReactiveException create(String message, Throwable cause) {
        return new ReactiveException(message, cause);
    }

    /**
     * 创建完整的响应式异常
     *
     * @param errorCode 错误码
     * @param message 异常消息
     * @param cause 原因异常
     * @param path 请求路径
     * @param streamContext 流处理上下文
     * @return 响应式异常实例
     */
    public static ReactiveException create(ErrorCode errorCode, String message, Throwable cause, String path, String streamContext) {
        return new ReactiveException(errorCode, message, cause, path, streamContext);
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
        sb.append("ReactiveException{");
        sb.append("errorCode=").append(errorCode);
        sb.append(", message='").append(getMessage()).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append(", streamContext='").append(streamContext).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }
}

