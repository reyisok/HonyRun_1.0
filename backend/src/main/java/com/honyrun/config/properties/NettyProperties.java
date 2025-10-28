package com.honyrun.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Netty配置属性类
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 10:45:00
 * @modified 2025-07-01 10:45:00
 *
 *           Copyright © 2025 HonyRun. All rights reserved.
 *
 *           配置Netty服务器的各项参数
 *           包括服务器端口、连接超时、缓冲区大小、线程池配置等
 *           支持通过配置文件进行外部化配置
 */
@ConfigurationProperties(prefix = "honyrun.netty")
public class NettyProperties {

    /**
     * 服务器配置
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private Server server;

    /**
     * 连接配置
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private Connection connection;

    /**
     * HTTP配置
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private Http http;

    /**
     * 线程池配置
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private ThreadPool threadPool;

    /**
     * 缓冲区配置
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private Buffer buffer;

    /**
     * 服务器配置类
     */
    public static class Server {
        /**
         * 服务器端口
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer port;

        /**
         * 是否启用网络监听
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Boolean wiretap;

        /**
         * 是否启用访问日志
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Boolean accessLog;

        /**
         * 是否启用压缩
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Boolean compression;

        /**
         * 是否启用转发头处理
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Boolean forwarded;

        // Getters and Setters
        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isWiretap() {
            return wiretap;
        }

        public void setWiretap(boolean wiretap) {
            this.wiretap = wiretap;
        }

        public boolean isAccessLog() {
            return accessLog;
        }

        public void setAccessLog(boolean accessLog) {
            this.accessLog = accessLog;
        }

        public boolean isCompression() {
            return compression;
        }

        public void setCompression(boolean compression) {
            this.compression = compression;
        }

        public boolean isForwarded() {
            return forwarded;
        }

        public void setForwarded(boolean forwarded) {
            this.forwarded = forwarded;
        }
    }

    /**
     * 连接配置类
     */
    public static class Connection {
        /**
         * 连接超时时间（毫秒）
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer connectTimeoutMillis;

        /**
         * 空闲超时时间
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Duration idleTimeout;

        /**
         * 请求超时时间
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Duration requestTimeout;

        /**
         * 读取超时时间（秒）
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer readTimeoutSeconds;

        /**
         * 写入超时时间（秒）
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer writeTimeoutSeconds;

        /**
         * 是否启用Keep-Alive
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Boolean keepAlive;

        /**
         * 是否启用TCP_NODELAY
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Boolean tcpNoDelay;

        /**
         * 是否启用地址重用
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Boolean reuseAddress;

        /**
         * 连接队列大小
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer backlog;

        /**
         * 最大连接数
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxConnections;

        /**
         * 等待获取连接的超时时间
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Duration pendingAcquireTimeout;

        /**
         * 最大空闲时间
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Duration maxIdleTime;

        /**
         * 连接的最大生存时间
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Duration maxLifeTime;

        /**
         * 连接获取重试次数
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer pendingAcquireMaxCount;

        /**
         * 是否启用连接池指标
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Boolean metrics;

        /**
         * 连接池清理间隔
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Duration evictInBackground;

        // Getters and Setters
        public int getConnectTimeoutMillis() {
            return connectTimeoutMillis;
        }

        public void setConnectTimeoutMillis(int connectTimeoutMillis) {
            this.connectTimeoutMillis = connectTimeoutMillis;
        }

        public Duration getIdleTimeout() {
            return idleTimeout;
        }

        public void setIdleTimeout(Duration idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        public Duration getRequestTimeout() {
            return requestTimeout;
        }

        public void setRequestTimeout(Duration requestTimeout) {
            this.requestTimeout = requestTimeout;
        }

        public int getReadTimeoutSeconds() {
            return readTimeoutSeconds;
        }

        public void setReadTimeoutSeconds(int readTimeoutSeconds) {
            this.readTimeoutSeconds = readTimeoutSeconds;
        }

        public int getWriteTimeoutSeconds() {
            return writeTimeoutSeconds;
        }

        public void setWriteTimeoutSeconds(int writeTimeoutSeconds) {
            this.writeTimeoutSeconds = writeTimeoutSeconds;
        }

        public boolean isKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
        }

        public boolean isTcpNoDelay() {
            return tcpNoDelay;
        }

        public void setTcpNoDelay(boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
        }

        public boolean isReuseAddress() {
            return reuseAddress;
        }

        public void setReuseAddress(boolean reuseAddress) {
            this.reuseAddress = reuseAddress;
        }

        public int getBacklog() {
            return backlog;
        }

        public void setBacklog(int backlog) {
            this.backlog = backlog;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }

        public Duration getPendingAcquireTimeout() {
            return pendingAcquireTimeout;
        }

        public void setPendingAcquireTimeout(Duration pendingAcquireTimeout) {
            this.pendingAcquireTimeout = pendingAcquireTimeout;
        }

        public Duration getMaxIdleTime() {
            return maxIdleTime;
        }

        public void setMaxIdleTime(Duration maxIdleTime) {
            this.maxIdleTime = maxIdleTime;
        }

        public Duration getMaxLifeTime() {
            return maxLifeTime;
        }

        public void setMaxLifeTime(Duration maxLifeTime) {
            this.maxLifeTime = maxLifeTime;
        }

        public int getPendingAcquireMaxCount() {
            return pendingAcquireMaxCount;
        }

        public void setPendingAcquireMaxCount(int pendingAcquireMaxCount) {
            this.pendingAcquireMaxCount = pendingAcquireMaxCount;
        }

        public boolean isMetrics() {
            return metrics;
        }

        public void setMetrics(boolean metrics) {
            this.metrics = metrics;
        }

        public Duration getEvictInBackground() {
            return evictInBackground;
        }

        public void setEvictInBackground(Duration evictInBackground) {
            this.evictInBackground = evictInBackground;
        }
    }

    /**
     * HTTP配置类
     */
    public static class Http {
        /**
         * 最大初始行长度
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxInitialLineLength;

        /**
         * 最大头部大小
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxHeaderSize;

        /**
         * 是否验证头部
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Boolean validateHeaders;

        /**
         * 初始缓冲区大小
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer initialBufferSize;

        // Getters and Setters
        public int getMaxInitialLineLength() {
            return maxInitialLineLength;
        }

        public void setMaxInitialLineLength(int maxInitialLineLength) {
            this.maxInitialLineLength = maxInitialLineLength;
        }

        public int getMaxHeaderSize() {
            return maxHeaderSize;
        }

        public void setMaxHeaderSize(int maxHeaderSize) {
            this.maxHeaderSize = maxHeaderSize;
        }

        public boolean isValidateHeaders() {
            return validateHeaders;
        }

        public void setValidateHeaders(boolean validateHeaders) {
            this.validateHeaders = validateHeaders;
        }

        public int getInitialBufferSize() {
            return initialBufferSize;
        }

        public void setInitialBufferSize(int initialBufferSize) {
            this.initialBufferSize = initialBufferSize;
        }
    }

    /**
     * 线程池配置类
     */
    public static class ThreadPool {
        /**
         * 事件循环线程数
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer eventLoopThreads;

        /**
         * HTTP事件循环线程数
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer httpEventLoopThreads;

        /**
         * 是否使用守护线程
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Boolean daemon;

        /**
         * 线程名前缀
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private String threadNamePrefix;

        /**
         * HTTP线程名前缀
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private String httpThreadNamePrefix;

        // Getters and Setters
        public int getEventLoopThreads() {
            return eventLoopThreads;
        }

        public void setEventLoopThreads(int eventLoopThreads) {
            this.eventLoopThreads = eventLoopThreads;
        }

        public int getHttpEventLoopThreads() {
            return httpEventLoopThreads;
        }

        public void setHttpEventLoopThreads(int httpEventLoopThreads) {
            this.httpEventLoopThreads = httpEventLoopThreads;
        }

        public boolean isDaemon() {
            return daemon;
        }

        public void setDaemon(boolean daemon) {
            this.daemon = daemon;
        }

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        public String getHttpThreadNamePrefix() {
            return httpThreadNamePrefix;
        }

        public void setHttpThreadNamePrefix(String httpThreadNamePrefix) {
            this.httpThreadNamePrefix = httpThreadNamePrefix;
        }
    }

    /**
     * 缓冲区配置类
     */
    public static class Buffer {
        /**
         * 接收缓冲区大小
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer receiveBufferSize;

        /**
         * 发送缓冲区大小
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer sendBufferSize;

        /**
         * 写缓冲区低水位
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer writeBufferLowWaterMark;

        /**
         * 写缓冲区高水位
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer writeBufferHighWaterMark;

        // Getters and Setters
        public int getReceiveBufferSize() {
            return receiveBufferSize;
        }

        public void setReceiveBufferSize(int receiveBufferSize) {
            this.receiveBufferSize = receiveBufferSize;
        }

        public int getSendBufferSize() {
            return sendBufferSize;
        }

        public void setSendBufferSize(int sendBufferSize) {
            this.sendBufferSize = sendBufferSize;
        }

        public int getWriteBufferLowWaterMark() {
            return writeBufferLowWaterMark;
        }

        public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
            this.writeBufferLowWaterMark = writeBufferLowWaterMark;
        }

        public int getWriteBufferHighWaterMark() {
            return writeBufferHighWaterMark;
        }

        public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
            this.writeBufferHighWaterMark = writeBufferHighWaterMark;
        }
    }

    // Main class getters and setters
    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Http getHttp() {
        return http;
    }

    public void setHttp(Http http) {
        this.http = http;
    }

    public ThreadPool getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }
}
