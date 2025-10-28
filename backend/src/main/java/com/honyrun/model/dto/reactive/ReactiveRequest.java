package com.honyrun.model.dto.reactive;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 响应式请求基类
 *
 * 为所有响应式请求DTO提供基础字段和通用方法，支持非阻塞I/O和事件驱动处理。
 * 该类定义了请求的基本结构，包括请求ID、时间戳等通用字段。
 *
 * 特性：
 * - 支持响应式编程模型
 * - 统一的请求标识
 * - 时间戳管理
 * - 序列化支持
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  16:40:00
 * @modified 2025-07-01 16:40:00
 * @version 2.0.0
 */
public abstract class ReactiveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求ID
     * 唯一标识请求的ID
     */
    private String requestId;

    /**
     * 请求时间戳
     * 请求创建的时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 客户端IP地址
     * 发起请求的客户端IP地址
     */
    private String clientIp;

    /**
     * 用户代理
     * 客户端的用户代理信息
     */
    private String userAgent;

    /**
     * 请求来源
     * 标识请求的来源系统或模块
     */
    private String source;

    /**
     * 跟踪ID
     * 用于分布式追踪的跟踪ID
     */
    private String traceId;

    /**
     * 默认构造函数
     */
    protected ReactiveRequest() {
        this.timestamp = LocalDateTime.now();
        this.requestId = generateRequestId();
    }

    /**
     * 带请求ID的构造函数
     *
     * @param requestId 请求ID
     */
    protected ReactiveRequest(String requestId) {
        this.requestId = requestId;
        this.timestamp = LocalDateTime.now();
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
     * 获取请求时间戳
     *
     * @return 请求时间戳
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * 设置请求时间戳
     *
     * @param timestamp 请求时间戳
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取客户端IP地址
     *
     * @return 客户端IP地址
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * 设置客户端IP地址
     *
     * @param clientIp 客户端IP地址
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    /**
     * 获取用户代理
     *
     * @return 用户代理
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * 设置用户代理
     *
     * @param userAgent 用户代理
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * 获取请求来源
     *
     * @return 请求来源
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置请求来源
     *
     * @param source 请求来源
     */
    public void setSource(String source) {
        this.source = source;
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
     * 生成请求ID
     *
     * @return 生成的请求ID
     */
    private String generateRequestId() {
        return "REQ_" + System.currentTimeMillis() + "_" + Thread.currentThread().threadId();
    }

    /**
     * 验证请求参数
     * 子类可以重写此方法来实现自定义验证逻辑
     *
     * @return 如果验证通过返回true，否则返回false
     */
    public boolean validate() {
        return this.requestId != null && !this.requestId.trim().isEmpty();
    }

    /**
     * 获取请求摘要信息
     *
     * @return 请求摘要字符串
     */
    public String getSummary() {
        return String.format("Request{id='%s', timestamp=%s, source='%s'}",
                requestId, timestamp, source);
    }

    @Override
    public String toString() {
        return String.format("%s{requestId='%s', timestamp=%s, clientIp='%s', source='%s', traceId='%s'}",
                getClass().getSimpleName(), requestId, timestamp, clientIp, source, traceId);
    }
}


