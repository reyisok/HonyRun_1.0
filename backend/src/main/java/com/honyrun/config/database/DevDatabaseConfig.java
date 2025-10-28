package com.honyrun.config.database;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.lang.NonNull;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.honyrun.config.converter.BooleanToIntegerConverter;
import com.honyrun.config.converter.IntegerToBooleanConverter;
import com.honyrun.util.LoggingUtil;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;

/**
 * 开发环境数据库配置类
 *
 * ========================================
 * 【重要】MySQL为项目必备组件 - 开发环境配置
 * ========================================
 * MySQL是本项目的核心数据存储组件，必须确保连接成功：
 * - 连接地址：localhost:8906
 * - 数据库名：honyrundb
 * - 用户名：honyrunMysql
 * - 密码：honyrun@sys
 * - 启动检测：通过EssentialComponentsValidator进行连接验证
 * - 失败处理：连接失败将导致应用启动终止
 * ========================================
 *
 * <p>
 * <strong>配置范围：</strong>仅处理开发环境(dev)的数据库配置
 * </p>
 * <p>
 * <strong>Bean命名规范：</strong>所有Bean使用dev前缀，避免与其他环境冲突
 * </p>
 * <p>
 * <strong>环境隔离：</strong>通过@Profile("dev")确保仅在开发环境激活
 * </p>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-06-28 20:00:00
 * @modified 2025-10-27 12:26:43
 * @version 3.1.0 - 移除UnifiedConfigManager依赖，解决循环依赖问题，直接使用Environment获取配置
 */
@Configuration
@Profile("dev")
@EnableTransactionManagement
public class DevDatabaseConfig extends AbstractR2dbcConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DevDatabaseConfig.class);

    private final Environment environment;

    /**
     * 构造函数注入 - 移除UnifiedConfigManager依赖以解决循环依赖
     * 
     * @param environment Spring环境配置
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-27 12:26:43
     * @modified 2025-10-27 12:26:43
     * @version 1.0.0
     */
    public DevDatabaseConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * 开发环境事务操作器
     *
     * <p>
     * <strong>Bean命名：</strong>devTransactionalOperator
     * </p>
     * <p>
     * <strong>依赖：</strong>devTransactionManager
     * </p>
     * <p>
     * <strong>事务配置：</strong>为开发环境提供响应式事务操作
     * </p>
     *
     * @return 事务操作器实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @version 1.0.0
     */
    @Bean("devTransactionalOperator")
    public TransactionalOperator devTransactionalOperator() {
        LoggingUtil.info(logger, "正在初始化开发环境事务操作器...");

        TransactionalOperator transactionalOperator = TransactionalOperator.create(devTransactionManager());

        LoggingUtil.info(logger, "开发环境事务操作器初始化成功");
        return transactionalOperator;
    }

    /**
     * 开发环境只读事务操作器
     *
     * @return TransactionalOperator
     */
    @Bean("devReadOnlyTransactionalOperator")
    public TransactionalOperator devReadOnlyTransactionalOperator() {
        LoggingUtil.info(logger, "正在初始化开发环境只读事务操作器...");

        TransactionalOperator transactionalOperator = TransactionalOperator.create(devTransactionManager());

        LoggingUtil.info(logger, "开发环境只读事务操作器初始化成功");
        return transactionalOperator;
    }

    /**
     * 开发环境长事务操作器
     *
     * @return TransactionalOperator
     */
    @Bean("devLongTransactionalOperator")
    public TransactionalOperator devLongTransactionalOperator() {
        LoggingUtil.info(logger, "正在初始化开发环境长事务操作器...");

        TransactionalOperator transactionalOperator = TransactionalOperator.create(devTransactionManager());

        LoggingUtil.info(logger, "开发环境长事务操作器初始化成功");
        return transactionalOperator;
    }

    /**
     * 开发环境连接工厂
     * 从application-dev.properties读取数据库配置
     * 
     * 注意：直接使用Environment获取配置以避免循环依赖
     *
     * @return ConnectionFactory 开发环境数据库连接工厂
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-24 16:01:03
     * @modified 2025-10-27 12:26:43
     * @version 1.1.0 - 移除UnifiedConfigManager依赖，直接使用Environment
     */
    @Bean("devConnectionFactory")
    @Override
    @NonNull
    public ConnectionFactory connectionFactory() {
        LoggingUtil.info(logger, "正在初始化开发环境数据库连接工厂...");

        // 直接从Environment获取数据库连接信息，避免循环依赖
        String databaseUrl = environment.getProperty("spring.r2dbc.url");
        String username = environment.getProperty("spring.r2dbc.username");
        String password = environment.getProperty("spring.r2dbc.password");

        // 验证必要的配置是否存在
        if (databaseUrl == null || username == null || password == null) {
            throw new IllegalStateException(
                    "数据库连接配置不完整，请检查配置文件中的 spring.r2dbc.url、spring.r2dbc.username、spring.r2dbc.password 配置");
        }
        int initialSize = Integer.parseInt(environment.getProperty("spring.r2dbc.pool.initial-size", "5"));
        int maxSize = Integer.parseInt(environment.getProperty("spring.r2dbc.pool.max-size", "20"));
        Duration maxIdleTime = parseDurationSafely(environment.getProperty("spring.r2dbc.pool.max-idle-time"), "PT10M");
        Duration maxAcquireTime = parseDurationSafely(environment.getProperty("spring.r2dbc.pool.max-acquire-time"), "PT15S");
        Duration maxCreateConnectionTime = parseDurationSafely(
                environment.getProperty("spring.r2dbc.pool.max-create-connection-time"), "PT15S");
        Duration maxLifeTime = parseDurationSafely(environment.getProperty("spring.r2dbc.pool.max-life-time"), "PT30M");
        String validationQuery = environment.getProperty("spring.r2dbc.pool.validation-query", "SELECT 1");
        Duration backgroundEvictionInterval = parseDurationSafely(
                environment.getProperty("spring.r2dbc.pool.background-eviction-interval"), "PT10S");
        int acquireRetry = Integer.parseInt(environment.getProperty("spring.r2dbc.pool.acquire-retry", "2"));

        LoggingUtil.info(logger, "数据库连接配置 - URL: {}, 用户名: {}", databaseUrl, username);
        LoggingUtil.info(logger, "连接池配置 - 初始大小: {}, 最大大小: {}, 最大空闲时间: {}", initialSize, maxSize, maxIdleTime);

        try {
            // 从配置文件读取数据库连接参数，避免硬编码
            String host = environment.getProperty("spring.r2dbc.host");
            String portStr = environment.getProperty("spring.r2dbc.port");
            String database = environment.getProperty("spring.r2dbc.database");

            // 验证必要的配置是否存在
            if (host == null || portStr == null || database == null) {
                throw new IllegalStateException(
                        "数据库连接池配置不完整，请检查配置文件中的 spring.r2dbc.host、spring.r2dbc.port、spring.r2dbc.database 配置");
            }
            Integer port = Integer.parseInt(portStr);
            String driver = environment.getProperty("spring.r2dbc.driver", "mysql");

            LoggingUtil.info(logger, "数据库连接参数 - 主机: {}, 端口: {}, 数据库: {}, 驱动: {}", host, port, database, driver);

            // 创建基础连接工厂
            ConnectionFactory baseConnectionFactory = ConnectionFactories.get(
                    ConnectionFactoryOptions.builder()
                            .option(ConnectionFactoryOptions.DRIVER, driver)
                            .option(ConnectionFactoryOptions.HOST, host)
                            .option(ConnectionFactoryOptions.PORT, port)
                            .option(ConnectionFactoryOptions.USER, username)
                            .option(ConnectionFactoryOptions.PASSWORD, password)
                            .option(ConnectionFactoryOptions.DATABASE, database)
                            .build());

            // 连接池配置
            ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder(baseConnectionFactory)
                    .initialSize(initialSize)
                    .maxSize(maxSize)
                    .maxIdleTime(maxIdleTime)
                    .maxAcquireTime(maxAcquireTime)
                    .maxCreateConnectionTime(maxCreateConnectionTime)
                    .maxLifeTime(maxLifeTime)
                    .validationQuery(validationQuery)
                    .backgroundEvictionInterval(backgroundEvictionInterval)
                    .acquireRetry(acquireRetry)
                    .build();

            ConnectionPool connectionPool = new ConnectionPool(poolConfig);
            LoggingUtil.info(logger, "开发环境数据库连接工厂初始化成功");
            return connectionPool;

        } catch (Exception e) {
            LoggingUtil.error(logger, "开发环境数据库连接工厂初始化失败", e);
            throw new RuntimeException("数据库连接配置失败", e);
        }
    }

    /**
     * 开发环境事务管理器
     *
     * <p>
     * <strong>Bean命名：</strong>devTransactionManager
     * </p>
     * <p>
     * <strong>依赖：</strong>devConnectionFactory
     * </p>
     * <p>
     * <strong>事务配置：</strong>适合开发环境的事务策略
     * </p>
     *
     * @return 响应式事务管理器实例
     */
    @Bean("devTransactionManager")
    public R2dbcTransactionManager devTransactionManager() {
        LoggingUtil.info(logger, "正在初始化开发环境事务管理器...");

        R2dbcTransactionManager transactionManager = new R2dbcTransactionManager(connectionFactory());

        LoggingUtil.info(logger, "开发环境事务管理器初始化成功");
        return transactionManager;
    }

    /**
     * 标准事务管理器 - 解决NoSuchBeanDefinitionException
     *
     * <p>
     * <strong>Bean命名：</strong>transactionManager
     * </p>
     * <p>
     * <strong>依赖：</strong>devConnectionFactory
     * </p>
     * <p>
     * <strong>事务配置：</strong>为开发环境提供标准名称的事务管理器
     * </p>
     *
     * @return 响应式事务管理器实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-13 15:30:00
     * @version 1.0.0
     */
    @Bean("transactionManager")
    @org.springframework.context.annotation.Primary
    public R2dbcTransactionManager transactionManager() {
        LoggingUtil.info(logger, "正在初始化开发环境标准事务管理器...");

        R2dbcTransactionManager transactionManager = new R2dbcTransactionManager(connectionFactory());

        LoggingUtil.info(logger, "开发环境标准事务管理器初始化成功");
        return transactionManager;
    }

    /**
     * 开发环境R2DBC实体模板
     *
     * <p>
     * <strong>Bean命名：</strong>devR2dbcEntityTemplate
     * </p>
     * <p>
     * <strong>依赖：</strong>devConnectionFactory
     * </p>
     * <p>
     * <strong>模板配置：</strong>优化开发环境数据操作
     * </p>
     *
     * @return R2DBC实体模板实例
     */
    @Bean("devR2dbcEntityTemplate")
    public R2dbcEntityTemplate devR2dbcEntityTemplate() {
        LoggingUtil.info(logger, "正在初始化开发环境R2DBC实体模板...");

        R2dbcEntityTemplate entityTemplate = new R2dbcEntityTemplate(connectionFactory());

        LoggingUtil.info(logger, "开发环境R2DBC实体模板初始化成功");
        return entityTemplate;
    }

    /**
     * 标准R2DBC实体模板 - 解决NoSuchBeanDefinitionException
     *
     * <p>
     * <strong>Bean命名：</strong>r2dbcEntityTemplate
     * </p>
     * <p>
     * <strong>依赖：</strong>devConnectionFactory
     * </p>
     * <p>
     * <strong>模板配置：</strong>为开发环境提供标准名称的R2DBC实体模板
     * </p>
     * <p>
     * <strong>环境隔离：</strong>仅在开发环境中激活，避免与其他环境的Bean定义冲突
     * </p>
     *
     * @return R2DBC实体模板实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @version 1.0.0
     */
    @Bean("r2dbcEntityTemplate")
    @Profile("dev")
    public R2dbcEntityTemplate r2dbcEntityTemplate() {
        LoggingUtil.info(logger, "正在初始化开发环境标准R2DBC实体模板...");

        R2dbcEntityTemplate entityTemplate = new R2dbcEntityTemplate(connectionFactory());

        LoggingUtil.info(logger, "开发环境标准R2DBC实体模板初始化成功");
        return entityTemplate;
    }

    /**
     * 自定义类型转换器配置
     *
     * @return 自定义转换器列表
     */
    @Override
    protected List<Object> getCustomConverters() {
        List<Object> converters = new ArrayList<>();
        converters.add(new BooleanToIntegerConverter());
        converters.add(new IntegerToBooleanConverter());
        LoggingUtil.info(logger, "已注册自定义类型转换器：BooleanToIntegerConverter, IntegerToBooleanConverter");
        return converters;
    }

    /**
     * 安全解析Duration配置值
     * 支持ISO-8601格式(PT30M)和数字格式(30000)
     *
     * @param value        配置值
     * @param defaultValue 默认值
     * @return Duration对象
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-24 16:01:03
     * @version 1.0.0
     */
    private Duration parseDurationSafely(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return Duration.parse(defaultValue);
        }

        try {
            // 尝试解析ISO-8601格式 (PT30M, PT10S等)
            return Duration.parse(value);
        } catch (Exception e) {
            try {
                // 尝试解析纯数字格式 (毫秒)
                long millis = Long.parseLong(value.trim());
                return Duration.ofMillis(millis);
            } catch (NumberFormatException nfe) {
                LoggingUtil.warn(logger, "无法解析Duration配置值: {}, 使用默认值: {}", value, defaultValue);
                return Duration.parse(defaultValue);
            }
        }
    }
}
