package com.honyrun.config.database;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.util.LoggingUtil;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;

/**
 * 生产环境数据库配置类
 *
 * ========================================
 * 【重要】MySQL为项目必备组件 - 生产环境配置
 * ========================================
 * MySQL是本项目的核心数据存储组件，必须确保连接正常：
 * - 连接地址：localhost:8906
 * - 数据库名：honyrundb
 * - 用户名：honyrunMysql
 * - 密码：honyrun@sys
 * - 连接池：生产环境优化配置
 * ========================================
 *
 * <p>
 * <strong>配置范围：</strong>仅处理生产环境(prod)的数据库配置
 * </p>
 * <p>
 * <strong>Bean命名规范：</strong>所有Bean使用prod前缀，避免与其他环境冲突
 * </p>
 * <p>
 * <strong>环境隔离：</strong>通过@Profile("prod")确保仅在生产环境激活
 * </p>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-06-28 20:00:00
 * @modified 2025-06-29 20:00:00
 * @version 3.0.0 - 重构为生产环境专用配置
 */
@Configuration
@Profile("prod")
public class ProdDatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(ProdDatabaseConfig.class);

    private final Environment environment;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入依赖
     *
     * @param environment Spring环境配置
     * @param unifiedConfigManager 统一配置管理器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public ProdDatabaseConfig(Environment environment, UnifiedConfigManager unifiedConfigManager) {
        this.environment = environment;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 生产环境R2DBC连接工厂配置
     * 注意：此处必须使用.block()调用，因为Spring Bean初始化需要同步返回ConnectionFactory对象
     *
     * <p>
     * <strong>Bean命名：</strong>prodConnectionFactory
     * </p>
     * <p>
     * <strong>环境隔离：</strong>仅在prod环境激活
     * </p>
     * <p>
     * <strong>连接配置：</strong>生产环境MySQL连接池
     * </p>
     *
     * @return 连接工厂实例
     */
    @Bean("prodConnectionFactory")
    public ConnectionFactory prodConnectionFactory() {
        LoggingUtil.info(logger, "正在初始化生产环境R2DBC连接工厂...");

        // 从配置文件获取数据库连接信息，不使用硬编码默认值
        // 使用同步方法获取数据库配置，避免阻塞调用
        String databaseUrl = unifiedConfigManager.getProperty("spring.r2dbc.url", null);
        String username = unifiedConfigManager.getProperty("spring.r2dbc.username", null);
        String password = unifiedConfigManager.getProperty("spring.r2dbc.password", null);
        
        // 验证必要的配置是否存在
        if (databaseUrl == null || username == null || password == null) {
            throw new IllegalStateException(
                    "数据库连接配置不完整，请检查配置文件中的 spring.r2dbc.url、spring.r2dbc.username、spring.r2dbc.password 配置");
        }
        
        // 必须使用.block()调用，因为Spring Bean初始化需要同步返回值
        Integer initialSize = unifiedConfigManager.getProperty("spring.r2dbc.pool.initial-size") != null ? 
                Integer.parseInt(unifiedConfigManager.getProperty("spring.r2dbc.pool.initial-size")) : null;
        Integer maxSize = unifiedConfigManager.getProperty("spring.r2dbc.pool.max-size") != null ? 
                Integer.parseInt(unifiedConfigManager.getProperty("spring.r2dbc.pool.max-size")) : null;
        String maxIdleTimeStr = unifiedConfigManager.getProperty("spring.r2dbc.pool.max-idle-time", null);
        String maxLifeTimeStr = unifiedConfigManager.getProperty("spring.r2dbc.pool.max-life-time", null);
        String maxAcquireTimeStr = unifiedConfigManager.getProperty("spring.r2dbc.pool.max-acquire-time", null);
        String maxCreateConnectionTimeStr = unifiedConfigManager.getProperty("spring.r2dbc.pool.max-create-connection-time", null);
        String validationQuery = unifiedConfigManager.getProperty("spring.r2dbc.pool.validation-query", null);
        
        // 验证连接池配置是否存在
        if (initialSize == null || maxSize == null || maxIdleTimeStr == null || 
            maxLifeTimeStr == null || maxAcquireTimeStr == null || 
            maxCreateConnectionTimeStr == null || validationQuery == null) {
            throw new IllegalStateException(
                    "数据库连接池配置不完整，请检查配置文件中的连接池相关配置");
        }
        
        Duration maxIdleTime = Duration.parse(maxIdleTimeStr);
        Duration maxLifeTime = Duration.parse(maxLifeTimeStr);
        Duration maxAcquireTime = Duration.parse(maxAcquireTimeStr);
        Duration maxCreateConnectionTime = Duration.parse(maxCreateConnectionTimeStr);

        LoggingUtil.info(logger, "数据库连接URL: {}", databaseUrl);
        LoggingUtil.info(logger, "数据库用户名: {}", username);
        LoggingUtil.info(logger, "连接池配置 - 初始大小: {}, 最大大小: {}, 最大空闲时间: {}",
                initialSize, maxSize, maxIdleTime);

        try {
            // 解析数据库URL
            ConnectionFactoryOptions options = ConnectionFactoryOptions.parse(databaseUrl)
                    .mutate()
                    .option(ConnectionFactoryOptions.USER, username)
                    .option(ConnectionFactoryOptions.PASSWORD, password)
                    .build();

            // 创建基础连接工厂
            ConnectionFactory connectionFactory = ConnectionFactories.get(options);

            // 配置连接池
            ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
                    .initialSize(initialSize)
                    .maxSize(maxSize)
                    .maxIdleTime(maxIdleTime)
                    .maxLifeTime(maxLifeTime)
                    .maxAcquireTime(maxAcquireTime)
                    .maxCreateConnectionTime(maxCreateConnectionTime)
                    .validationQuery(validationQuery)
                    .build();

            ConnectionPool connectionPool = new ConnectionPool(poolConfig);

            LoggingUtil.info(logger, "生产环境R2DBC连接工厂初始化成功");
            return connectionPool;
        } catch (Exception e) {
            LoggingUtil.error(logger, "生产环境R2DBC连接工厂初始化失败", e);
            throw new RuntimeException("生产环境数据库连接配置失败", e);
        }
    }

    /**
     * 生产环境响应式事务管理器
     *
     * <p>
     * <strong>Bean命名：</strong>prodTransactionManager
     * </p>
     * <p>
     * <strong>依赖：</strong>prodConnectionFactory
     * </p>
     * <p>
     * <strong>事务配置：</strong>生产环境事务管理
     * </p>
     *
     * @param connectionFactory 生产环境连接工厂
     * @return 事务管理器实例
     */
    @Bean("prodTransactionManager")
    public ReactiveTransactionManager prodTransactionManager(ConnectionFactory connectionFactory) {
        LoggingUtil.info(logger, "正在初始化生产环境响应式事务管理器...");

        R2dbcTransactionManager transactionManager = new R2dbcTransactionManager(connectionFactory);

        LoggingUtil.info(logger, "生产环境响应式事务管理器初始化成功");
        return transactionManager;
    }

    /**
     * 生产环境R2DBC实体模板
     *
     * <p>
     * <strong>Bean命名：</strong>prodR2dbcEntityTemplate
     * </p>
     * <p>
     * <strong>依赖：</strong>prodConnectionFactory
     * </p>
     * <p>
     * <strong>模板配置：</strong>生产环境数据操作模板
     * </p>
     *
     * @param connectionFactory 生产环境连接工厂
     * @return 实体模板实例
     */
    @Bean("prodR2dbcEntityTemplate")
    public R2dbcEntityTemplate prodR2dbcEntityTemplate(ConnectionFactory connectionFactory) {
        LoggingUtil.info(logger, "正在初始化生产环境R2DBC实体模板...");

        R2dbcEntityTemplate entityTemplate = new R2dbcEntityTemplate(connectionFactory);

        LoggingUtil.info(logger, "生产环境R2DBC实体模板初始化成功");
        return entityTemplate;
    }
}
