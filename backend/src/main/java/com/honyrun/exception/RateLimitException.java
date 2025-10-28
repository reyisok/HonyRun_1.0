package com.honyrun.exception;

import java.time.LocalDateTime;

/**
 * 限流异常类
 * 实现请求频率限制和流量控制异常
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class RateLimitException extends RuntimeException {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final ErrorCode errorCode;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 限流类型
     */
    private String limitType;

    /**
     * 限流键
     */
    private String limitKey;

    /**
     * 当前请求数
     */
    private Long currentRequests;

    /**
     * 最大允许请求数
     */
    private Long maxRequests;

    /**
     * 时间窗口（秒）
     */
    private Long timeWindow;

    /**
     * 重试时间
     */
    private LocalDateTime retryAfter;

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     */
    public RateLimitException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     */
    public RateLimitException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public RateLimitException(String message) {
        super(message);
        this.errorCode = ErrorCode.RATE_LIMIT_EXCEEDED;
    }

    /**
     * 创建请求频率超限异常
     *
     * @param limitKey 限流键
     * @param currentRequests 当前请求数
     * @param maxRequests 最大允许请求数
     * @param timeWindow 时间窗口（秒）
     * @return 限流异常实例
     */
    public static RateLimitException rateLimitExceeded(String limitKey, long currentRequests,
                                                     long maxRequests, long timeWindow) {
        RateLimitException exception = new RateLimitException(ErrorCode.RATE_LIMIT_EXCEEDED,
                String.format("请求频率超限: %d/%d requests in %d seconds", currentRequests, maxRequests, timeWindow));
        exception.setLimitKey(limitKey);
        exception.setCurrentRequests(currentRequests);
        exception.setMaxRequests(maxRequests);
        exception.setTimeWindow(timeWindow);
        exception.setLimitType("rate_limit");
        return exception;
    }

    /**
     * 创建并发数超限异常
     *
     * @param limitKey 限流键
     * @param currentConcurrent 当前并发数
     * @param maxConcurrent 最大并发数
     * @return 限流异常实例
     */
    public static RateLimitException concurrentLimitExceeded(String limitKey, long currentConcurrent,
                                                           long maxConcurrent) {
        RateLimitException exception = new RateLimitException(ErrorCode.CONCURRENT_LIMIT_EXCEEDED,
                String.format("并发数超限: %d/%d concurrent requests", currentConcurrent, maxConcurrent));
        exception.setLimitKey(limitKey);
        exception.setCurrentRequests(currentConcurrent);
        exception.setMaxRequests(maxConcurrent);
        exception.setLimitType("concurrent_limit");
        return exception;
    }

    /**
     * 创建用户请求频率超限异常
     *
     * @param userId 用户ID
     * @param currentRequests 当前请求数
     * @param maxRequests 最大允许请求数
     * @param timeWindow 时间窗口（秒）
     * @return 限流异常实例
     */
    public static RateLimitException userRateLimitExceeded(String userId, long currentRequests,
                                                         long maxRequests, long timeWindow) {
        RateLimitException exception = new RateLimitException(ErrorCode.RATE_LIMIT_EXCEEDED,
                String.format("用户请求频率超限: %d/%d requests in %d seconds", currentRequests, maxRequests, timeWindow));
        exception.setLimitKey("user:" + userId);
        exception.setCurrentRequests(currentRequests);
        exception.setMaxRequests(maxRequests);
        exception.setTimeWindow(timeWindow);
        exception.setLimitType("user_rate_limit");
        return exception;
    }

    /**
     * 创建IP请求频率超限异常
     *
     * @param ipAddress IP地址
     * @param currentRequests 当前请求数
     * @param maxRequests 最大允许请求数
     * @param timeWindow 时间窗口（秒）
     * @return 限流异常实例
     */
    public static RateLimitException ipRateLimitExceeded(String ipAddress, long currentRequests,
                                                       long maxRequests, long timeWindow) {
        RateLimitException exception = new RateLimitException(ErrorCode.RATE_LIMIT_EXCEEDED,
                String.format("IP请求频率超限: %d/%d requests in %d seconds", currentRequests, maxRequests, timeWindow));
        exception.setLimitKey("ip:" + ipAddress);
        exception.setCurrentRequests(currentRequests);
        exception.setMaxRequests(maxRequests);
        exception.setTimeWindow(timeWindow);
        exception.setLimitType("ip_rate_limit");
        return exception;
    }

    /**
     * 创建API请求频率超限异常
     *
     * @param apiPath API路径
     * @param currentRequests 当前请求数
     * @param maxRequests 最大允许请求数
     * @param timeWindow 时间窗口（秒）
     * @return 限流异常实例
     */
    public static RateLimitException apiRateLimitExceeded(String apiPath, long currentRequests,
                                                        long maxRequests, long timeWindow) {
        RateLimitException exception = new RateLimitException(ErrorCode.RATE_LIMIT_EXCEEDED,
                String.format("API请求频率超限: %d/%d requests in %d seconds", currentRequests, maxRequests, timeWindow));
        exception.setLimitKey("api:" + apiPath);
        exception.setCurrentRequests(currentRequests);
        exception.setMaxRequests(maxRequests);
        exception.setTimeWindow(timeWindow);
        exception.setLimitType("api_rate_limit");
        return exception;
    }

    /**
     * 创建系统负载过高异常
     *
     * @param currentLoad 当前负载
     * @param maxLoad 最大负载
     * @return 限流异常实例
     */
    public static RateLimitException systemOverload(double currentLoad, double maxLoad) {
        RateLimitException exception = new RateLimitException(ErrorCode.TOO_MANY_REQUESTS,
                String.format("系统负载过高: %.2f/%.2f", currentLoad, maxLoad));
        exception.setLimitKey("system_load");
        exception.setLimitType("system_overload");
        return exception;
    }

    /**
     * 设置重试时间
     *
     * @param retryAfterSeconds 重试等待秒数
     * @return 当前异常实例（支持链式调用）
     */
    public RateLimitException withRetryAfter(long retryAfterSeconds) {
        this.retryAfter = LocalDateTime.now().plusSeconds(retryAfterSeconds);
        return this;
    }

    /**
     * 设置重试时间
     *
     * @param retryAfter 重试时间
     * @return 当前异常实例（支持链式调用）
     */
    public RateLimitException withRetryAfter(LocalDateTime retryAfter) {
        this.retryAfter = retryAfter;
        return this;
    }

    // Getter和Setter方法
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLimitType() {
        return limitType;
    }

    public void setLimitType(String limitType) {
        this.limitType = limitType;
    }

    public String getLimitKey() {
        return limitKey;
    }

    public void setLimitKey(String limitKey) {
        this.limitKey = limitKey;
    }

    public Long getCurrentRequests() {
        return currentRequests;
    }

    public void setCurrentRequests(Long currentRequests) {
        this.currentRequests = currentRequests;
    }

    public Long getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(Long maxRequests) {
        this.maxRequests = maxRequests;
    }

    public Long getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(Long timeWindow) {
        this.timeWindow = timeWindow;
    }

    public LocalDateTime getRetryAfter() {
        return retryAfter;
    }

    public void setRetryAfter(LocalDateTime retryAfter) {
        this.retryAfter = retryAfter;
    }

    public RateLimitException withPath(String path) {
        this.path = path;
        return this;
    }

    public RateLimitException withLimitType(String limitType) {
        this.limitType = limitType;
        return this;
    }

    public RateLimitException withLimitKey(String limitKey) {
        this.limitKey = limitKey;
        return this;
    }

    /**
     * 获取重试等待秒数
     *
     * @return 重试等待秒数，如果未设置返回null
     */
    public Long getRetryAfterSeconds() {
        if (retryAfter == null) {
            return null;
        }
        return java.time.Duration.between(LocalDateTime.now(), retryAfter).getSeconds();
    }

    @Override
    public String toString() {
        return String.format("RateLimitException{errorCode=%s, message='%s', limitType='%s', limitKey='%s', current=%d, max=%d}",
                errorCode, getMessage(), limitType, limitKey, currentRequests, maxRequests);
    }
}

