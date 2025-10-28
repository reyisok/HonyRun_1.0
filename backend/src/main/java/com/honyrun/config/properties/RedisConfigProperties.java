package com.honyrun.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis配置属性类
 *
 * 统一管理Redis相关的配置参数，替代@Value注解的硬编码配置方式
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
 * <li>环境变量：SPRING_DATA_REDIS_*</li>
 * </ul>
 *
 * <p>
 * <strong>配置范围：</strong>
 * </p>
 * <ul>
 * <li>Redis连接配置</li>
 * <li>连接池配置</li>
 * <li>超时配置</li>
 * <li>数据库选择配置</li>
 * </ul>
 *
 * <p>
 * <strong>配置前缀：</strong>spring.data.redis
 * </p>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-27 12:26:43
 * @modified 2025-10-27 12:26:43
 * @version 1.0.1 - 移除硬编码，严格遵循统一配置管理规范
 */
@Component
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisConfigProperties {

    /**
     * Redis主机地址
     * 必须在环境配置文件中配置：spring.data.redis.host
     */
    private String host;

    /**
     * Redis端口号
     * 必须在环境配置文件中配置：spring.data.redis.port
     */
    private int port;

    /**
     * Redis密码
     * 必须在环境配置文件中配置：spring.data.redis.password
     */
    private String password;

    /**
     * Redis数据库索引
     * 必须在环境配置文件中配置：spring.data.redis.database
     */
    private int database;

    /**
     * 连接超时时间
     * 必须在环境配置文件中配置：spring.data.redis.timeout
     */
    private Duration timeout;

    /**
     * 命令执行超时时间
     * 必须在环境配置文件中配置：spring.data.redis.command-timeout
     */
    private Duration commandTimeout;

    /**
     * Lettuce连接池配置
     */
    private final Lettuce lettuce = new Lettuce();

    /**
     * Lettuce配置类
     */
    public static class Lettuce {
        private final Pool pool = new Pool();

        public Pool getPool() {
            return pool;
        }
    }

    /**
     * 连接池配置类
     * 所有配置值必须从环境配置文件读取，禁止硬编码！
     */
    public static class Pool {
        /**
         * 最大连接数
         * 必须在环境配置文件中配置：spring.data.redis.pool.max-active
         */
        private int maxActive;

        /**
         * 最大空闲连接数
         * 必须在环境配置文件中配置：spring.data.redis.pool.max-idle
         */
        private int maxIdle;

        /**
         * 最小空闲连接数
         * 必须在环境配置文件中配置：spring.data.redis.pool.min-idle
         */
        private int minIdle;

        /**
         * 获取连接时的最大等待时间
         * 必须在环境配置文件中配置：spring.data.redis.pool.max-wait
         */
        private Duration maxWait;

        /**
         * 是否在获取连接时验证连接
         * 必须在环境配置文件中配置：spring.data.redis.pool.test-on-borrow
         */
        private boolean testOnBorrow;

        /**
         * 是否在归还连接时验证连接
         * 必须在环境配置文件中配置：spring.data.redis.pool.test-on-return
         */
        private boolean testOnReturn;

        /**
         * 是否在空闲时验证连接
         * 必须在环境配置文件中配置：spring.data.redis.pool.test-while-idle
         */
        private boolean testWhileIdle;

        // ==================== Getter和Setter方法 ====================

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
    }

    // ==================== 主要属性的Getter和Setter方法 ====================

    /**
     * 获取Redis主机地址
     *
     * @return Redis主机地址
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置Redis主机地址
     *
     * @param host Redis主机地址
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 获取Redis端口号
     *
     * @return Redis端口号
     */
    public int getPort() {
        return port;
    }

    /**
     * 设置Redis端口号
     *
     * @param port Redis端口号
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 获取Redis密码
     *
     * @return Redis密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置Redis密码
     *
     * @param password Redis密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取Redis数据库索引
     *
     * @return Redis数据库索引
     */
    public int getDatabase() {
        return database;
    }

    /**
     * 设置Redis数据库索引
     *
     * @param database Redis数据库索引
     */
    public void setDatabase(int database) {
        this.database = database;
    }

    /**
     * 获取连接超时时间
     *
     * @return 连接超时时间
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * 设置连接超时时间
     *
     * @param timeout 连接超时时间
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    /**
     * 获取命令执行超时时间
     *
     * @return 命令执行超时时间
     */
    public Duration getCommandTimeout() {
        return commandTimeout;
    }

    /**
     * 设置命令执行超时时间
     *
     * @param commandTimeout 命令执行超时时间
     */
    public void setCommandTimeout(Duration commandTimeout) {
        this.commandTimeout = commandTimeout;
    }

    /**
     * 获取连接池配置
     *
     * @return 连接池配置
     */
    public Pool getPool() {
        return lettuce.getPool();
    }

    /**
     * 获取Lettuce配置
     *
     * @return Lettuce配置
     */
    public Lettuce getLettuce() {
        return lettuce;
    }

    /**
     * 获取Redis连接URL
     *
     * @return Redis连接URL
     */
    public String getConnectionUrl() {
        return String.format("redis://%s:%d/%d", host, port, database);
    }

    /**
     * 获取超时时间的毫秒值
     *
     * @return 超时时间毫秒值
     */
    public long getTimeoutMillis() {
        return timeout.toMillis();
    }

    @Override
    public String toString() {
        return "RedisConfigProperties{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", password='***'" + // 密码脱敏
                ", database=" + database +
                ", timeout=" + timeout +
                ", lettuce=" + lettuce +
                '}';
    }
}
