package com.honyrun.model.dto.reactive;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 响应式响应基类
 *
 * 为所有响应式响应DTO提供基础字段和通用方法，支持非阻塞I/O和事件驱动处理。
 * 该类定义了响应的基本结构，包括状态码、消息、时间戳等通用字段。
 *
 * 特性：
 * - 支持响应式编程模型
 * - 统一的响应格式
 * - 状态码管理
 * - 序列化支持
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  16:45:00
 * @modified 2025-07-01 16:45:00
 * @version 2.0.0
 */
public abstract class ReactiveResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应是否成功
     */
    private boolean success;

    /**
     * 响应状态码
     */
    private String code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private transient T data;

    /**
     * 响应时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 请求ID
     * 对应的请求ID，用于请求响应关联
     */
    private String requestId;

    /**
     * 跟踪ID
     * 用于分布式追踪的跟踪ID
     */
    private String traceId;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;

    /**
     * 服务器信息
     */
    private String serverInfo;

    /**
     * 默认构造函数
     */
    protected ReactiveResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 成功响应构造函数
     *
     * @param data 响应数据
     */
    protected ReactiveResponse(T data) {
        this();
        this.success = true;
        this.code = "200";
        this.message = "操作成功";
        this.data = data;
    }

    /**
     * 带状态的响应构造函数
     *
     * @param success 是否成功
     * @param code 状态码
     * @param message 响应消息
     * @param data 响应数据
     */
    protected ReactiveResponse(boolean success, String code, String message, T data) {
        this();
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 获取响应是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置响应是否成功
     *
     * @param success 是否成功
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 获取响应状态码
     *
     * @return 状态码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置响应状态码
     *
     * @param code 状态码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取响应消息
     *
     * @return 响应消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置响应消息
     *
     * @param message 响应消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取响应数据
     *
     * @return 响应数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置响应数据
     *
     * @param data 响应数据
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 获取响应时间戳
     *
     * @return 响应时间戳
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * 设置响应时间戳
     *
     * @param timestamp 响应时间戳
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取请求ID
     *
     * @return 请求ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * 设置请求ID
     *
     * @param requestId 请求ID
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * 获取跟踪ID
     *
     * @return 跟踪ID
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 设置跟踪ID
     *
     * @param traceId 跟踪ID
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * 获取处理耗时
     *
     * @return 处理耗时（毫秒）
     */
    public Long getProcessingTime() {
        return processingTime;
    }

    /**
     * 设置处理耗时
     *
     * @param processingTime 处理耗时（毫秒）
     */
    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * 获取服务器信息
     *
     * @return 服务器信息
     */
    public String getServerInfo() {
        return serverInfo;
    }

    /**
     * 设置服务器信息
     *
     * @param serverInfo 服务器信息
     */
    public void setServerInfo(String serverInfo) {
        this.serverInfo = serverInfo;
    }

    /**
     * 创建成功响应
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ReactiveResponse<T> success(T data) {
        return new ReactiveResponse<T>(data) {};
    }

    /**
     * 创建成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ReactiveResponse<T> success() {
        return success(null);
    }

    /**
     * 创建成功响应（带消息）
     *
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ReactiveResponse<T> success(String message, T data) {
        ReactiveResponse<T> response = new ReactiveResponse<T>(data) {};
        response.setMessage(message);
        return response;
    }

    /**
     * 创建失败响应
     *
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> ReactiveResponse<T> error(String code, String message) {
        return new ReactiveResponse<T>(false, code, message, null) {};
    }

    /**
     * 创建失败响应（默认错误码）
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> ReactiveResponse<T> error(String message) {
        return error("500", message);
    }

    /**
     * 创建失败响应（带数据）
     *
     * @param code 错误码
     * @param message 错误消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> ReactiveResponse<T> error(String code, String message, T data) {
        return new ReactiveResponse<T>(false, code, message, data) {};
    }

    /**
     * 判断响应是否为错误
     *
     * @return 如果是错误响应返回true，否则返回false
     */
    public boolean isError() {
        return !this.success;
    }

    /**
     * 判断是否有数据
     *
     * @return 如果有数据返回true，否则返回false
     */
    public boolean hasData() {
        return this.data != null;
    }

    /**
     * 获取响应摘要信息
     *
     * @return 响应摘要字符串
     */
    public String getSummary() {
        return String.format("Response{success=%s, code='%s', message='%s', hasData=%s}",
                success, code, message, hasData());
    }

    @Override
    public String toString() {
        return String.format("%s{success=%s, code='%s', message='%s', data=%s, timestamp=%s, requestId='%s'}",
                getClass().getSimpleName(), success, code, message, data, timestamp, requestId);
    }
}


