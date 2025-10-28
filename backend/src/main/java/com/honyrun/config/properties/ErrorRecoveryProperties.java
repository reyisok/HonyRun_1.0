package com.honyrun.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 错误恢复配置属性类
 *
 * 统一管理错误恢复相关的配置参数，替代@Value注解的硬编码配置方式
 *
 * <p>
 * <strong>重要说明：</strong>
 * 本类严格遵循统一配置管理规范，所有配置值必须从环境配置文件读取，
 * 禁止在代码中硬编码任何默认值！
 * </p>
 *
 * <p>
 * <strong>配置文件位置：</strong>
 * </p>
 * <ul>
 * <li>开发环境：application-dev.yml</li>
 * <li>测试环境：application-test.yml</li>
 * <li>生产环境：application-prod.yml</li>
 * <li>环境变量：HONYRUN_ERROR_RECOVERY_*</li>
 * </ul>
 *
 * <p>
 * <strong>配置范围：</strong>
 * </p>
 * <ul>
 * <li>重试机制配置</li>
 * <li>背压处理配置</li>
 * <li>流式处理配置</li>
 * <li>延迟和抖动配置</li>
 * </ul>
 *
 * <p>
 * <strong>配置前缀：</strong>honyrun.error-recovery
 * </p>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-27 12:26:43
 * @modified 2025-10-27 12:26:43
 * @version 1.0.1 - 移除硬编码，严格遵循统一配置管理规范
 */
@Component
@ConfigurationProperties(prefix = "honyrun.error-recovery")
public class ErrorRecoveryProperties {

    /**
     * 最大重试次数
     * 必须在环境配置文件中配置：honyrun.error-recovery.max-retry-attempts
     */
    private int maxRetryAttempts;

    /**
     * 初始重试延迟
     * 必须在环境配置文件中配置：honyrun.error-recovery.initial-retry-delay
     */
    private Duration initialRetryDelay;

    /**
     * 最大重试延迟
     * 必须在环境配置文件中配置：honyrun.error-recovery.max-retry-delay
     */
    private Duration maxRetryDelay;

    /**
     * 抖动因子
     * 必须在环境配置文件中配置：honyrun.error-recovery.jitter-factor
     */
    private double jitterFactor;

    /**
     * 背压重试次数
     * 必须在环境配置文件中配置：honyrun.error-recovery.backpressure-retry-attempts
     */
    private int backpressureRetryAttempts;

    /**
     * 背压初始延迟
     * 必须在环境配置文件中配置：honyrun.error-recovery.backpressure-initial-delay
     */
    private Duration backpressureInitialDelay;

    /**
     * 背压最大延迟
     * 必须在环境配置文件中配置：honyrun.error-recovery.backpressure-max-delay
     */
    private Duration backpressureMaxDelay;

    /**
     * 背压抖动因子
     * 必须在环境配置文件中配置：honyrun.error-recovery.backpressure-jitter
     */
    private double backpressureJitter;

    /**
     * 流式处理重试次数
     * 必须在环境配置文件中配置：honyrun.error-recovery.streaming-retry-attempts
     */
    private int streamingRetryAttempts;

    /**
     * 流式处理初始延迟
     * 必须在环境配置文件中配置：honyrun.error-recovery.streaming-initial-delay
     */
    private Duration streamingInitialDelay;

    /**
     * 流式处理最大延迟
     * 必须在环境配置文件中配置：honyrun.error-recovery.streaming-max-delay
     */
    private Duration streamingMaxDelay;

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取最大重试次数
     *
     * @return 最大重试次数
     */
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    /**
     * 设置最大重试次数
     *
     * @param maxRetryAttempts 最大重试次数
     */
    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    /**
     * 获取初始重试延迟
     *
     * @return 初始重试延迟
     */
    public Duration getInitialRetryDelay() {
        return initialRetryDelay;
    }

    /**
     * 设置初始重试延迟
     *
     * @param initialRetryDelay 初始重试延迟
     */
    public void setInitialRetryDelay(Duration initialRetryDelay) {
        this.initialRetryDelay = initialRetryDelay;
    }

    /**
     * 获取最大重试延迟
     *
     * @return 最大重试延迟
     */
    public Duration getMaxRetryDelay() {
        return maxRetryDelay;
    }

    /**
     * 设置最大重试延迟
     *
     * @param maxRetryDelay 最大重试延迟
     */
    public void setMaxRetryDelay(Duration maxRetryDelay) {
        this.maxRetryDelay = maxRetryDelay;
    }

    /**
     * 获取抖动因子
     *
     * @return 抖动因子
     */
    public double getJitterFactor() {
        return jitterFactor;
    }

    /**
     * 设置抖动因子
     *
     * @param jitterFactor 抖动因子
     */
    public void setJitterFactor(double jitterFactor) {
        this.jitterFactor = jitterFactor;
    }

    /**
     * 获取背压重试次数
     *
     * @return 背压重试次数
     */
    public int getBackpressureRetryAttempts() {
        return backpressureRetryAttempts;
    }

    /**
     * 设置背压重试次数
     *
     * @param backpressureRetryAttempts 背压重试次数
     */
    public void setBackpressureRetryAttempts(int backpressureRetryAttempts) {
        this.backpressureRetryAttempts = backpressureRetryAttempts;
    }

    /**
     * 获取背压初始延迟
     *
     * @return 背压初始延迟
     */
    public Duration getBackpressureInitialDelay() {
        return backpressureInitialDelay;
    }

    /**
     * 设置背压初始延迟
     *
     * @param backpressureInitialDelay 背压初始延迟
     */
    public void setBackpressureInitialDelay(Duration backpressureInitialDelay) {
        this.backpressureInitialDelay = backpressureInitialDelay;
    }

    /**
     * 获取背压最大延迟
     *
     * @return 背压最大延迟
     */
    public Duration getBackpressureMaxDelay() {
        return backpressureMaxDelay;
    }

    /**
     * 设置背压最大延迟
     *
     * @param backpressureMaxDelay 背压最大延迟
     */
    public void setBackpressureMaxDelay(Duration backpressureMaxDelay) {
        this.backpressureMaxDelay = backpressureMaxDelay;
    }

    /**
     * 获取背压抖动因子
     *
     * @return 背压抖动因子
     */
    public double getBackpressureJitter() {
        return backpressureJitter;
    }

    /**
     * 设置背压抖动因子
     *
     * @param backpressureJitter 背压抖动因子
     */
    public void setBackpressureJitter(double backpressureJitter) {
        this.backpressureJitter = backpressureJitter;
    }

    /**
     * 获取流式处理重试次数
     *
     * @return 流式处理重试次数
     */
    public int getStreamingRetryAttempts() {
        return streamingRetryAttempts;
    }

    /**
     * 设置流式处理重试次数
     *
     * @param streamingRetryAttempts 流式处理重试次数
     */
    public void setStreamingRetryAttempts(int streamingRetryAttempts) {
        this.streamingRetryAttempts = streamingRetryAttempts;
    }

    /**
     * 获取流式处理初始延迟
     *
     * @return 流式处理初始延迟
     */
    public Duration getStreamingInitialDelay() {
        return streamingInitialDelay;
    }

    /**
     * 设置流式处理初始延迟
     *
     * @param streamingInitialDelay 流式处理初始延迟
     */
    public void setStreamingInitialDelay(Duration streamingInitialDelay) {
        this.streamingInitialDelay = streamingInitialDelay;
    }

    /**
     * 获取流式处理最大延迟
     *
     * @return 流式处理最大延迟
     */
    public Duration getStreamingMaxDelay() {
        return streamingMaxDelay;
    }

    /**
     * 设置流式处理最大延迟
     *
     * @param streamingMaxDelay 流式处理最大延迟
     */
    public void setStreamingMaxDelay(Duration streamingMaxDelay) {
        this.streamingMaxDelay = streamingMaxDelay;
    }

    @Override
    public String toString() {
        return "ErrorRecoveryProperties{" +
                "maxRetryAttempts=" + maxRetryAttempts +
                ", initialRetryDelay=" + initialRetryDelay +
                ", maxRetryDelay=" + maxRetryDelay +
                ", jitterFactor=" + jitterFactor +
                ", backpressureRetryAttempts=" + backpressureRetryAttempts +
                ", backpressureInitialDelay=" + backpressureInitialDelay +
                ", backpressureMaxDelay=" + backpressureMaxDelay +
                ", backpressureJitter=" + backpressureJitter +
                ", streamingRetryAttempts=" + streamingRetryAttempts +
                ", streamingInitialDelay=" + streamingInitialDelay +
                ", streamingMaxDelay=" + streamingMaxDelay +
                '}';
    }
}
