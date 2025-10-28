package com.honyrun.config.startup;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 连接重试配置
 *
 * <p><strong>用于配置MySQL和Redis连接检测的重试机制参数</strong></p>
 *
 * <p><strong>配置参数：</strong>
 * <ul>
 *   <li><strong>maxRetries</strong> - 最大重试次数，默认3次</li>
 *   <li><strong>retryDelay</strong> - 重试间隔时间，默认2秒</li>
 *   <li><strong>backoffMultiplier</strong> - 退避倍数，默认1.5倍</li>
 *   <li><strong>maxRetryDelay</strong> - 最大重试间隔，默认10秒</li>
 * </ul>
 *
 * <p><strong>重试策略：</strong>
 * 采用指数退避算法，每次重试间隔时间递增，避免对服务造成过大压力
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-06-28 11:00:00
 * @modified 2025-06-29 11:00:00
 * @version 1.0.0 - 初始版本，实现连接重试配置
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "honyrun.connection.retry")
public class ConnectionRetryConfig {

    /**
     * 最大重试次数
     * 默认值：3次
     */
    private int maxRetries = 3;

    /**
     * 初始重试间隔时间（毫秒）
     * 默认值：2000毫秒（2秒）
     */
    private long retryDelayMs = 2000;

    /**
     * 退避倍数
     * 每次重试后，间隔时间乘以此倍数
     * 默认值：1.5
     */
    private double backoffMultiplier = 1.5;

    /**
     * 最大重试间隔时间（毫秒）
     * 默认值：10000毫秒（10秒）
     */
    private long maxRetryDelayMs = 10000;

    /**
     * 连接超时时间（毫秒）
     * 默认值：10000毫秒（10秒）
     */
    private long connectionTimeoutMs = 10000;

    /**
     * 获取最大重试次数
     *
     * @return 最大重试次数
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * 设置最大重试次数
     *
     * @param maxRetries 最大重试次数，必须大于等于0
     */
    public void setMaxRetries(int maxRetries) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("最大重试次数不能小于0");
        }
        this.maxRetries = maxRetries;
    }

    /**
     * 获取初始重试间隔时间
     *
     * @return 重试间隔时间（Duration对象）
     */
    public Duration getRetryDelay() {
        return Duration.ofMillis(retryDelayMs);
    }

    /**
     * 获取初始重试间隔时间（毫秒）
     *
     * @return 重试间隔时间（毫秒）
     */
    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    /**
     * 设置初始重试间隔时间（毫秒）
     *
     * @param retryDelayMs 重试间隔时间（毫秒），必须大于0
     */
    public void setRetryDelayMs(long retryDelayMs) {
        if (retryDelayMs <= 0) {
            throw new IllegalArgumentException("重试间隔时间必须大于0");
        }
        this.retryDelayMs = retryDelayMs;
    }

    /**
     * 获取退避倍数
     *
     * @return 退避倍数
     */
    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    /**
     * 设置退避倍数
     *
     * @param backoffMultiplier 退避倍数，必须大于1.0
     */
    public void setBackoffMultiplier(double backoffMultiplier) {
        if (backoffMultiplier <= 1.0) {
            throw new IllegalArgumentException("退避倍数必须大于1.0");
        }
        this.backoffMultiplier = backoffMultiplier;
    }

    /**
     * 获取最大重试间隔时间
     *
     * @return 最大重试间隔时间（Duration对象）
     */
    public Duration getMaxRetryDelay() {
        return Duration.ofMillis(maxRetryDelayMs);
    }

    /**
     * 获取最大重试间隔时间（毫秒）
     *
     * @return 最大重试间隔时间（毫秒）
     */
    public long getMaxRetryDelayMs() {
        return maxRetryDelayMs;
    }

    /**
     * 设置最大重试间隔时间（毫秒）
     *
     * @param maxRetryDelayMs 最大重试间隔时间（毫秒），必须大于初始间隔时间
     */
    public void setMaxRetryDelayMs(long maxRetryDelayMs) {
        if (maxRetryDelayMs <= retryDelayMs) {
            throw new IllegalArgumentException("最大重试间隔时间必须大于初始重试间隔时间");
        }
        this.maxRetryDelayMs = maxRetryDelayMs;
    }

    /**
     * 获取连接超时时间
     *
     * @return 连接超时时间（Duration对象）
     */
    public Duration getConnectionTimeout() {
        return Duration.ofMillis(connectionTimeoutMs);
    }

    /**
     * 获取连接超时时间（毫秒）
     *
     * @return 连接超时时间（毫秒）
     */
    public long getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    /**
     * 设置连接超时时间（毫秒）
     *
     * @param connectionTimeoutMs 连接超时时间（毫秒），必须大于0
     */
    public void setConnectionTimeoutMs(long connectionTimeoutMs) {
        if (connectionTimeoutMs <= 0) {
            throw new IllegalArgumentException("连接超时时间必须大于0");
        }
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    /**
     * 计算指定重试次数的延迟时间
     *
     * @param retryAttempt 当前重试次数（从1开始）
     * @return 延迟时间（Duration对象）
     */
    public Duration calculateDelay(int retryAttempt) {
        if (retryAttempt <= 0) {
            return Duration.ofMillis(retryDelayMs);
        }

        // 计算指数退避延迟时间
        long delay = (long) (retryDelayMs * Math.pow(backoffMultiplier, retryAttempt - 1));
        
        // 限制最大延迟时间
        delay = Math.min(delay, maxRetryDelayMs);
        
        return Duration.ofMillis(delay);
    }

    /**
     * 检查是否应该继续重试
     *
     * @param currentAttempt 当前尝试次数（从1开始）
     * @return 如果应该重试返回true，否则返回false
     */
    public boolean shouldRetry(int currentAttempt) {
        return currentAttempt <= maxRetries;
    }

    @Override
    public String toString() {
        return "ConnectionRetryConfig{" +
                "maxRetries=" + maxRetries +
                ", retryDelayMs=" + retryDelayMs +
                ", backoffMultiplier=" + backoffMultiplier +
                ", maxRetryDelayMs=" + maxRetryDelayMs +
                '}';
    }
}


