package com.honyrun.config.database;

import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

/**
 * 开发环境 R2DBC 初始化配置
 *
 * 在 dev 配置下，应用启动时通过 R2DBC 执行统一的数据库初始化脚本：
 * 1) db/unified-schema.sql - 统一的数据库架构脚本，包含：
 *    - 所有表结构创建
 *    - 所有索引创建
 *    - 基础数据插入
 *    - 系统配置数据
 *
 * 为避免重复执行导致的索引或数据已存在错误，开发环境启用 continueOnError=true。
 *
 * 注意：已整合所有数据库脚本到unified-schema.sql，不再使用分散的data.sql、test-data.sql等脚本。
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-23 16:57:03
 * @modified 2025-10-23 16:57:03
 * @version 1.1.0
 */
@Configuration
@Profile("dev")
@ConditionalOnProperty(prefix = "honyrun.dev.db.init", name = "enabled", havingValue = "true", matchIfMissing = false)
public class DevR2dbcInitializerConfig {

    private static final Logger logger = LoggerFactory.getLogger(DevR2dbcInitializerConfig.class);

    public DevR2dbcInitializerConfig() {
        logger.info("[DevR2dbcInitializerConfig] Configuration class instantiated for 'dev' profile");
    }

    /**
     * 开发环境数据库初始化器
     *
     * 执行统一的数据库初始化脚本，确保开发环境数据库完整性。
     * 使用unified-schema.sql统一脚本，包含表结构、索引和初始数据。
     *
     * @param connectionFactory R2DBC连接工厂
     * @return 配置好的连接工厂初始化器
     */
    @Bean("devR2dbcInitializer")
    public ConnectionFactoryInitializer devR2dbcInitializer(ConnectionFactory connectionFactory) {
        logger.info("[DevR2dbcInitializer] Creating devR2dbcInitializer bean for 'dev' profile");

        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        // 统一数据库初始化脚本（包含表结构、索引和初始数据）
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/unified-schema.sql"));
        populator.setContinueOnError(true);

        initializer.setDatabasePopulator(populator);

        logger.info("[DevR2dbcInitializer] Running unified database initialization script 'db/unified-schema.sql' with continueOnError=true under 'dev' profile.");
        return initializer;
    }
}
