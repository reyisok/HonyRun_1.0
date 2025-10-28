package com.honyrun.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 响应式属性配置类
 *
 * 【重要】：所有配置值必须从环境配置文件中读取
 * 配置文件路径：
 * - 开发环境：application-dev.properties
 * - 测试环境：application-test.properties  
 * - 生产环境：application-prod.properties
 *
 * @author Mr.Rey
 * @version 2.0.1 - 移除硬编码，严格遵循统一配置管理规范
 * @created 2025-07-01  16:20:00
 * @modified 2025-10-27 12:26:43
 *
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 配置响应式系统的核心参数
 * 包括背压策略、缓冲区大小、超时设置等
 */
@Component
@ConfigurationProperties(prefix = "honyrun.reactive")
public class ReactiveProperties {

    /**
     * 背压配置
     * 必须从配置文件获取：honyrun.reactive.backpressure.*
     */
    private Backpressure backpressure;

    /**
     * 缓冲区配置
     * 必须从配置文件获取：honyrun.reactive.buffer.*
     */
    private Buffer buffer;

    /**
     * 超时配置
     * 必须从配置文件获取：honyrun.reactive.timeout.*
     */
    private Timeout timeout;

    /**
     * 线程池配置
     * 必须从配置文件获取：honyrun.reactive.thread-pool.*
     */
    private ThreadPool threadPool;

    /**
     * 背压配置类
     */
    public static class Backpressure {
        /**
         * 背压策略：BUFFER, DROP, LATEST, ERROR
         * 必须从配置文件获取：honyrun.reactive.backpressure.strategy
         */
        private String strategy;

        /**
         * 背压缓冲区大小
         * 必须从配置文件获取：honyrun.reactive.backpressure.buffer-size
         */
        private int bufferSize;

        /**
         * 是否启用背压
         * 必须从配置文件获取：honyrun.reactive.backpressure.enabled
         */
        private boolean enabled;

        // Getters and Setters
        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * 缓冲区配置类
     */
    public static class Buffer {
        /**
         * 默认缓冲区大小
         * 必须从配置文件获取：honyrun.reactive.buffer.default-size
         */
        private int defaultSize;

        /**
         * 最大缓冲区大小
         * 必须从配置文件获取：honyrun.reactive.buffer.max-size
         */
        private int maxSize;

        /**
         * 预取数量
         * 必须从配置文件获取：honyrun.reactive.buffer.prefetch
         */
        private int prefetch;

        /**
         * 是否启用缓冲
         * 必须从配置文件获取：honyrun.reactive.buffer.enabled
         */
        private boolean enabled;

        // Getters and Setters
        public int getDefaultSize() {
            return defaultSize;
        }

        public void setDefaultSize(int defaultSize) {
            this.defaultSize = defaultSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public int getPrefetch() {
            return prefetch;
        }

        public void setPrefetch(int prefetch) {
            this.prefetch = prefetch;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * 超时配置类
     */
    public static class Timeout {
        /**
         * 默认超时时间
         * 必须从配置文件获取：honyrun.reactive.timeout.default-timeout
         */
        private Duration defaultTimeout;

        /**
         * 连接超时时间
         * 必须从配置文件获取：honyrun.reactive.timeout.connection-timeout
         */
        private Duration connectionTimeout;

        /**
         * 读取超时时间
         * 必须从配置文件获取：honyrun.reactive.timeout.read-timeout
         */
        private Duration readTimeout;

        /**
         * 写入超时时间
         * 必须从配置文件获取：honyrun.reactive.timeout.write-timeout
         */
        private Duration writeTimeout;

        // Getters and Setters
        public Duration getDefaultTimeout() {
            return defaultTimeout;
        }

        public void setDefaultTimeout(Duration defaultTimeout) {
            this.defaultTimeout = defaultTimeout;
        }

        public Duration getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(Duration connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public Duration getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
        }

        public Duration getWriteTimeout() {
            return writeTimeout;
        }

        public void setWriteTimeout(Duration writeTimeout) {
            this.writeTimeout = writeTimeout;
        }
    }

    /**
     * 线程池配置类
     */
    public static class ThreadPool {
        /**
         * 核心线程数
         * 必须从配置文件获取：honyrun.reactive.thread-pool.core-size
         */
        private int coreSize;

        /**
         * 最大线程数
         * 必须从配置文件获取：honyrun.reactive.thread-pool.max-size
         */
        private int maxSize;

        /**
         * 队列容量
         * 必须从配置文件获取：honyrun.reactive.thread-pool.queue-capacity
         */
        private int queueCapacity;

        /**
         * 线程名称前缀
         * 必须从配置文件获取：honyrun.reactive.thread-pool.thread-name-prefix
         */
        private String threadNamePrefix;

        // Getters and Setters
        public int getCoreSize() {
            return coreSize;
        }

        public void setCoreSize(int coreSize) {
            this.coreSize = coreSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }
    }

    // Main class getters and setters
    public Backpressure getBackpressure() {
        return backpressure;
    }

    public void setBackpressure(Backpressure backpressure) {
        this.backpressure = backpressure;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public ThreadPool getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }
}


