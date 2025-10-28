package com.honyrun.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis属性配置
 *
 * 定义Redis连接和缓存相关的配置属性
 * 支持Redis连接池、缓存策略、序列化等配置
 *
 * 主要配置：
 * - Redis连接配置
 * - 连接池配置
 * - 缓存策略配置
 * - 序列化配置
 * - 集群配置
 *
 * 配置特性：
 * - 连接管理：支持单机和集群模式
 * - 连接池优化：配置连接池参数
 * - 缓存策略：配置缓存过期和淘汰策略
 * - 序列化：配置键值序列化方式
 *
 * 响应式特性：
 * - 响应式连接：支持响应式Redis操作
 * - 非阻塞I/O：Redis操作采用非阻塞模式
 * - 连接复用：优化连接复用和管理
 * - 背压支持：支持背压控制
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 01:05:00
 * @modified 2025-07-01 01:05:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
@ConfigurationProperties(prefix = "honyrun.redis")
public class RedisProperties {

    /**
     * Redis连接配置
     * 必须从配置文件获取：honyrun.redis.connection
     */
    private Connection connection;

    /**
     * 连接池配置
     * 必须从配置文件获取：honyrun.redis.pool
     */
    private Pool pool;

    /**
     * 缓存配置
     * 必须从配置文件获取：honyrun.redis.cache
     */
    private Cache cache;

    /**
     * 序列化配置
     * 必须从配置文件获取：honyrun.redis.serialization
     */
    private Serialization serialization;

    /**
     * 集群配置
     * 必须从配置文件获取：honyrun.redis.cluster
     */
    private Cluster cluster;

    // Getters and Setters
    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public Serialization getSerialization() {
        return serialization;
    }

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    /**
     * Redis连接配置类
     */
    public static class Connection {
        /**
         * Redis主机地址
         * 从配置文件获取: spring.data.redis.host
         */
        private String host;

        /**
         * Redis端口
         * 从配置文件获取: spring.data.redis.port
         */
        private int port;

        /**
         * Redis密码
         * 从配置文件获取: spring.data.redis.password
         */
        private String password;

        /**
         * 数据库索引
         * 从配置文件获取: spring.data.redis.database
         */
        private String database;

        /**
         * 连接超时时间
         * 从配置文件获取: spring.data.redis.timeout
         */
        private Duration timeout;

        /**
         * 是否启用SSL
         * 从配置文件获取: spring.data.redis.ssl
         */
        private boolean ssl;

        /**
         * 客户端名称
         * 从配置文件获取: spring.data.redis.client-name
         */
        private String clientName;

        // Getters and Setters
        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }
    }

    /**
     * 连接池配置类
     */
    public static class Pool {
        /**
         * 最大连接数
         * 从配置文件获取: spring.data.redis.jedis.pool.max-active
         */
        private int maxActive;

        /**
         * 最大空闲连接数
         * 从配置文件获取: spring.data.redis.jedis.pool.max-idle
         */
        private int maxIdle;

        /**
         * 最小空闲连接数
         * 从配置文件获取: spring.data.redis.jedis.pool.min-idle
         */
        private int minIdle;

        /**
         * 获取连接时的最大等待时间
         * 从配置文件获取: spring.data.redis.jedis.pool.max-wait
         */
        private Duration maxWait;

        /**
         * 是否在获取连接时验证连接
         * 从配置文件获取: spring.data.redis.jedis.pool.test-on-borrow
         */
        private boolean testOnBorrow;

        /**
         * 是否在归还连接时验证连接
         * 从配置文件获取: spring.data.redis.jedis.pool.test-on-return
         */
        private boolean testOnReturn;

        /**
         * 是否在空闲时验证连接
         * 从配置文件获取: spring.data.redis.jedis.pool.test-while-idle
         */
        private boolean testWhileIdle;

        /**
         * 空闲连接检测间隔
         * 从配置文件获取: spring.data.redis.jedis.pool.time-between-eviction-runs
         */
        private Duration timeBetweenEvictionRuns;

        /**
         * 连接最小空闲时间
         * 从配置文件获取: spring.data.redis.jedis.pool.min-evictable-idle-time
         */
        private Duration minEvictableIdleTime;

        // Getters and Setters
        public int getMaxActive() {
            return maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public int getMaxIdle() {
            return maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public Duration getMaxWait() {
            return maxWait;
        }

        public void setMaxWait(Duration maxWait) {
            this.maxWait = maxWait;
        }

        public boolean isTestOnBorrow() {
            return testOnBorrow;
        }

        public void setTestOnBorrow(boolean testOnBorrow) {
            this.testOnBorrow = testOnBorrow;
        }

        public boolean isTestOnReturn() {
            return testOnReturn;
        }

        public void setTestOnReturn(boolean testOnReturn) {
            this.testOnReturn = testOnReturn;
        }

        public boolean isTestWhileIdle() {
            return testWhileIdle;
        }

        public void setTestWhileIdle(boolean testWhileIdle) {
            this.testWhileIdle = testWhileIdle;
        }

        public Duration getTimeBetweenEvictionRuns() {
            return timeBetweenEvictionRuns;
        }

        public void setTimeBetweenEvictionRuns(Duration timeBetweenEvictionRuns) {
            this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
        }

        public Duration getMinEvictableIdleTime() {
            return minEvictableIdleTime;
        }

        public void setMinEvictableIdleTime(Duration minEvictableIdleTime) {
            this.minEvictableIdleTime = minEvictableIdleTime;
        }
    }

    /**
     * 缓存配置类
     */
    public static class Cache {
        /**
         * 缓存键前缀
         * 从配置文件获取: spring.cache.redis.key-prefix
         */
        private String keyPrefix;

        /**
         * 默认过期时间
         * 从配置文件获取: spring.cache.redis.time-to-live
         */
        private Duration defaultExpiration;

        /**
         * 用户缓存过期时间
         * 从配置文件获取: spring.cache.redis.user-expiration
         */
        private Duration userExpiration;

        /**
         * 系统缓存过期时间
         * 从配置文件获取: spring.cache.redis.system-expiration
         */
        private Duration systemExpiration;

        /**
         * 会话缓存过期时间
         * 从配置文件获取: spring.cache.redis.session-expiration
         */
        private Duration sessionExpiration;

        /**
         * 是否缓存空值
         * 从配置文件获取: spring.cache.redis.cache-null-values
         */
        private boolean cacheNullValues;

        /**
         * 是否使用键前缀
         * 从配置文件获取: spring.cache.redis.use-key-prefix
         */
        private boolean useKeyPrefix;

        /**
         * 键分隔符
         * 从配置文件获取: spring.cache.redis.key-separator
         */
        private String keySeparator;

        // Getters and Setters
        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        public Duration getDefaultExpiration() {
            return defaultExpiration;
        }

        public void setDefaultExpiration(Duration defaultExpiration) {
            this.defaultExpiration = defaultExpiration;
        }

        public Duration getUserExpiration() {
            return userExpiration;
        }

        public void setUserExpiration(Duration userExpiration) {
            this.userExpiration = userExpiration;
        }

        public Duration getSystemExpiration() {
            return systemExpiration;
        }

        public void setSystemExpiration(Duration systemExpiration) {
            this.systemExpiration = systemExpiration;
        }

        public Duration getSessionExpiration() {
            return sessionExpiration;
        }

        public void setSessionExpiration(Duration sessionExpiration) {
            this.sessionExpiration = sessionExpiration;
        }

        public boolean isCacheNullValues() {
            return cacheNullValues;
        }

        public void setCacheNullValues(boolean cacheNullValues) {
            this.cacheNullValues = cacheNullValues;
        }

        public boolean isUseKeyPrefix() {
            return useKeyPrefix;
        }

        public void setUseKeyPrefix(boolean useKeyPrefix) {
            this.useKeyPrefix = useKeyPrefix;
        }
    }

    /**
     * 序列化配置类
     */
    public static class Serialization {
        /**
         * 键序列化器类型
         * 必须从配置文件获取：honyrun.redis.serialization.key-serializer
         */
        private String keySerializer;

        /**
         * 值序列化器类型
         * 必须从配置文件获取：honyrun.redis.serialization.value-serializer
         */
        private String valueSerializer;

        /**
         * 哈希键序列化器类型
         * 必须从配置文件获取：honyrun.redis.serialization.hash-key-serializer
         */
        private String hashKeySerializer;

        /**
         * 哈希值序列化器类型
         * 必须从配置文件获取：honyrun.redis.serialization.hash-value-serializer
         */
        private String hashValueSerializer;

        // Getters and Setters
        public String getKeySerializer() {
            return keySerializer;
        }

        public void setKeySerializer(String keySerializer) {
            this.keySerializer = keySerializer;
        }

        public String getValueSerializer() {
            return valueSerializer;
        }

        public void setValueSerializer(String valueSerializer) {
            this.valueSerializer = valueSerializer;
        }

        public String getHashKeySerializer() {
            return hashKeySerializer;
        }

        public void setHashKeySerializer(String hashKeySerializer) {
            this.hashKeySerializer = hashKeySerializer;
        }

        public String getHashValueSerializer() {
            return hashValueSerializer;
        }

        public void setHashValueSerializer(String hashValueSerializer) {
            this.hashValueSerializer = hashValueSerializer;
        }
    }

    /**
     * 集群配置类
     */
    public static class Cluster {
        /**
         * 是否启用集群模式
         * 必须从配置文件获取：honyrun.redis.cluster.enabled
         */
        private boolean enabled;

        /**
         * 集群节点列表
         * 必须从配置文件获取：honyrun.redis.cluster.nodes
         */
        private java.util.List<String> nodes;

        /**
         * 最大重定向次数
         * 必须从配置文件获取：honyrun.redis.cluster.max-redirects
         */
        private int maxRedirects;

        /**
         * 集群刷新间隔
         * 必须从配置文件获取：honyrun.redis.cluster.refresh-period
         */
        private Duration refreshPeriod;

        /**
         * 是否启用自适应刷新
         * 必须从配置文件获取：honyrun.redis.cluster.adaptive-refresh
         */
        private boolean adaptiveRefresh;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public java.util.List<String> getNodes() {
            return nodes;
        }

        public void setNodes(java.util.List<String> nodes) {
            this.nodes = nodes;
        }

        public int getMaxRedirects() {
            return maxRedirects;
        }

        public void setMaxRedirects(int maxRedirects) {
            this.maxRedirects = maxRedirects;
        }

        public Duration getRefreshPeriod() {
            return refreshPeriod;
        }

        public void setRefreshPeriod(Duration refreshPeriod) {
            this.refreshPeriod = refreshPeriod;
        }

        public boolean isAdaptiveRefresh() {
            return adaptiveRefresh;
        }

        public void setAdaptiveRefresh(boolean adaptiveRefresh) {
            this.adaptiveRefresh = adaptiveRefresh;
        }
    }
}


