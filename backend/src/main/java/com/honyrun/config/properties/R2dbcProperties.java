package com.honyrun.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * R2DBC属性配置
 *
 * 定义R2DBC响应式数据库连接相关的配置属性
 * 支持连接池、事务、审计等配置
 *
 * 主要配置：
 * - 数据库连接配置
 * - 连接池配置
 * - 事务配置
 * - 审计配置
 * - 性能优化配置
 *
 * 配置特性：
 * - 响应式连接：支持非阻塞数据库操作
 * - 连接池管理：优化连接池配置
 * - 事务支持：配置响应式事务
 * - 审计功能：支持数据审计
 *
 * 响应式特性：
 * - 非阻塞I/O：数据库操作采用非阻塞模式
 * - 背压支持：支持数据流背压控制
 * - 连接复用：优化连接复用和管理
 * - 异步事务：支持异步事务处理
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 01:10:00
 * @modified 2025-07-01 01:10:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
@ConfigurationProperties(prefix = "honyrun.r2dbc")
public class R2dbcProperties {

    /**
     * 数据库连接配置
     */
    private Connection connection = new Connection();

    /**
     * 连接池配置
     */
    private Pool pool = new Pool();

    /**
     * 事务配置
     */
    private Transaction transaction = new Transaction();

    /**
     * 审计配置
     */
    private Audit audit = new Audit();

    /**
     * 性能配置
     */
    private Performance performance = new Performance();

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

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    /**
     * 数据库连接配置类
     */
    public static class Connection {
        /**
         * 数据库连接URL
         * 从配置文件获取: spring.r2dbc.url
         */
        private String url;

        /**
         * 数据库用户名
         * 从配置文件获取: spring.r2dbc.username
         */
        private String username;

        /**
         * 数据库密码
         * 从配置文件获取: spring.r2dbc.password
         */
        private String password;

        /**
         * 数据库驱动
         * 从配置文件获取: spring.r2dbc.driver
         */
        private String driver;

        /**
         * 连接超时时间
         * 从配置文件获取: spring.r2dbc.connect-timeout
         */
        private Duration connectTimeout;

        /**
         * 语句执行超时时间
         * 从配置文件获取: spring.r2dbc.statement-timeout
         */
        private Duration statementTimeout;

        /**
         * 是否启用SSL
         * 从配置文件获取: spring.r2dbc.ssl
         */
        private boolean ssl;

        /**
         * 连接选项
         * 从配置文件获取: spring.r2dbc.options
         */
        private java.util.Map<String, String> options;

        // Getters and Setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriver() {
            return driver;
        }

        public void setDriver(String driver) {
            this.driver = driver;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Duration getStatementTimeout() {
            return statementTimeout;
        }

        public void setStatementTimeout(Duration statementTimeout) {
            this.statementTimeout = statementTimeout;
        }

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }

        public java.util.Map<String, String> getOptions() {
            return options;
        }

        public void setOptions(java.util.Map<String, String> options) {
            this.options = options;
        }
    }

    /**
     * 连接池配置类
     */
    public static class Pool {
        /**
         * 初始连接数
         * 从配置文件获取: spring.r2dbc.pool.initial-size
         */
        private int initialSize;

        /**
         * 最大连接数
         * 从配置文件获取: spring.r2dbc.pool.max-size
         */
        private int maxSize;

        /**
         * 连接最大空闲时间
         * 从配置文件获取: spring.r2dbc.pool.max-idle-time
         */
        private Duration maxIdleTime;

        /**
         * 连接最大生命周期
         * 从配置文件获取: spring.r2dbc.pool.max-life-time
         */
        private Duration maxLifeTime;

        /**
         * 获取连接最大等待时间
         * 从配置文件获取: spring.r2dbc.pool.max-acquire-time
         */
        private Duration maxAcquireTime;

        /**
         * 创建连接最大等待时间
         * 从配置文件获取: spring.r2dbc.pool.max-create-connection-time
         */
        private Duration maxCreateConnectionTime;

        /**
         * 连接验证查询
         * 从配置文件获取: spring.r2dbc.pool.validation-query
         */
        private String validationQuery;

        /**
         * 验证深度
         * 从配置文件获取: spring.r2dbc.pool.validation-depth
         */
        private String validationDepth;

        // Getters and Setters
        public int getInitialSize() {
            return initialSize;
        }

        public void setInitialSize(int initialSize) {
            this.initialSize = initialSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
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

        public Duration getMaxAcquireTime() {
            return maxAcquireTime;
        }

        public void setMaxAcquireTime(Duration maxAcquireTime) {
            this.maxAcquireTime = maxAcquireTime;
        }

        public Duration getMaxCreateConnectionTime() {
            return maxCreateConnectionTime;
        }

        public void setMaxCreateConnectionTime(Duration maxCreateConnectionTime) {
            this.maxCreateConnectionTime = maxCreateConnectionTime;
        }

        public String getValidationQuery() {
            return validationQuery;
        }

        public void setValidationQuery(String validationQuery) {
            this.validationQuery = validationQuery;
        }

        public String getValidationDepth() {
            return validationDepth;
        }

        public void setValidationDepth(String validationDepth) {
            this.validationDepth = validationDepth;
        }
    }

    /**
     * 事务配置类
     */
    public static class Transaction {
        /**
         * 默认事务超时时间
         * 从配置文件获取: spring.r2dbc.transaction.default-timeout
         */
        private Duration defaultTimeout;

        /**
         * 默认事务隔离级别
         * 从配置文件获取: spring.r2dbc.transaction.default-isolation-level
         */
        private String defaultIsolationLevel;

        /**
         * 是否启用事务
         * 从配置文件获取: spring.r2dbc.transaction.enabled
         */
        private boolean enabled;

        /**
         * 事务管理器名称
         * 从配置文件获取: spring.r2dbc.transaction.manager-name
         */
        private String managerName;

        /**
         * 提交失败时是否回滚
         * 从配置文件获取: spring.r2dbc.transaction.rollback-on-commit-failure
         */
        private boolean rollbackOnCommitFailure;

        // Getters and Setters
        public Duration getDefaultTimeout() {
            return defaultTimeout;
        }

        public void setDefaultTimeout(Duration defaultTimeout) {
            this.defaultTimeout = defaultTimeout;
        }

        public String getDefaultIsolationLevel() {
            return defaultIsolationLevel;
        }

        public void setDefaultIsolationLevel(String defaultIsolationLevel) {
            this.defaultIsolationLevel = defaultIsolationLevel;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getManagerName() {
            return managerName;
        }

        public void setManagerName(String managerName) {
            this.managerName = managerName;
        }

        public boolean isRollbackOnCommitFailure() {
            return rollbackOnCommitFailure;
        }

        public void setRollbackOnCommitFailure(boolean rollbackOnCommitFailure) {
            this.rollbackOnCommitFailure = rollbackOnCommitFailure;
        }
    }

    /**
     * 审计配置类
     */
    public static class Audit {
        /**
         * 是否启用审计
         * 从配置文件获取: spring.r2dbc.audit.enabled
         */
        private boolean enabled;

        /**
         * 审计提供者
         * 从配置文件获取: spring.r2dbc.audit.auditor-provider
         */
        private String auditorProvider;

        /**
         * 是否设置日期
         * 从配置文件获取: spring.r2dbc.audit.set-dates
         */
        private boolean setDates;

        /**
         * 创建时是否修改
         * 从配置文件获取: spring.r2dbc.audit.modify-on-create
         */
        private boolean modifyOnCreate;

        /**
         * 日期时间提供者
         * 从配置文件获取: spring.r2dbc.audit.date-time-provider
         */
        private String dateTimeProvider;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAuditorProvider() {
            return auditorProvider;
        }

        public void setAuditorProvider(String auditorProvider) {
            this.auditorProvider = auditorProvider;
        }

        public boolean isSetDates() {
            return setDates;
        }

        public void setSetDates(boolean setDates) {
            this.setDates = setDates;
        }

        public boolean isModifyOnCreate() {
            return modifyOnCreate;
        }

        public void setModifyOnCreate(boolean modifyOnCreate) {
            this.modifyOnCreate = modifyOnCreate;
        }

        public String getDateTimeProvider() {
            return dateTimeProvider;
        }

        public void setDateTimeProvider(String dateTimeProvider) {
            this.dateTimeProvider = dateTimeProvider;
        }
    }

    /**
     * 性能配置类
     */
    public static class Performance {
        /**
         * 是否启用监控
         * 从配置文件获取: spring.r2dbc.performance.monitoring-enabled
         */
        private boolean monitoringEnabled;

        /**
         * 慢查询阈值（毫秒）
         * 从配置文件获取: spring.r2dbc.performance.slow-query-threshold
         */
        private long slowQueryThreshold;

        /**
         * 是否记录慢查询
         * 从配置文件获取: spring.r2dbc.performance.log-slow-queries
         */
        private boolean logSlowQueries;

        /**
         * 批处理大小
         * 从配置文件获取: spring.r2dbc.performance.batch-size
         */
        private int batchSize;

        /**
         * 获取大小
         * 从配置文件获取: spring.r2dbc.performance.fetch-size
         */
        private int fetchSize;

        /**
         * 是否启用查询缓存
         * 从配置文件获取: spring.r2dbc.performance.query-cache-enabled
         */
        private boolean queryCacheEnabled;

        /**
         * 查询缓存大小
         * 从配置文件获取: spring.r2dbc.performance.query-cache-size
         */
        private int queryCacheSize;

        // Getters and Setters
        public boolean isMonitoringEnabled() {
            return monitoringEnabled;
        }

        public void setMonitoringEnabled(boolean monitoringEnabled) {
            this.monitoringEnabled = monitoringEnabled;
        }

        public long getSlowQueryThreshold() {
            return slowQueryThreshold;
        }

        public void setSlowQueryThreshold(long slowQueryThreshold) {
            this.slowQueryThreshold = slowQueryThreshold;
        }

        public boolean isLogSlowQueries() {
            return logSlowQueries;
        }

        public void setLogSlowQueries(boolean logSlowQueries) {
            this.logSlowQueries = logSlowQueries;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public int getFetchSize() {
            return fetchSize;
        }

        public void setFetchSize(int fetchSize) {
            this.fetchSize = fetchSize;
        }

        public boolean isQueryCacheEnabled() {
            return queryCacheEnabled;
        }

        public void setQueryCacheEnabled(boolean queryCacheEnabled) {
            this.queryCacheEnabled = queryCacheEnabled;
        }

        public int getQueryCacheSize() {
            return queryCacheSize;
        }

        public void setQueryCacheSize(int queryCacheSize) {
            this.queryCacheSize = queryCacheSize;
        }
    }
}
