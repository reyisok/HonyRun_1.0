package com.honyrun.config.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import com.honyrun.util.LoggingUtil;

import jakarta.annotation.PostConstruct;

/**
 * R2DBC Repository配置类
 *
 * <p>
 * <strong>功能说明：</strong>
 * </p>
 * <ul>
 * <li>专门负责R2DBC Repository的配置和管理</li>
 * <li>精确指定Repository扫描范围为repository.r2dbc包</li>
 * <li>避免与Spring Data Redis Repository配置冲突</li>
 * <li>实现分层Repository模式的数据访问层</li>
 * </ul>
 *
 * <p>
 * <strong>配置范围：</strong>
 * </p>
 * <ul>
 * <li>扫描包：com.honyrun.repository.r2dbc</li>
 * <li>Repository类型：R2DBC响应式Repository接口</li>
 * <li>环境支持：开发和生产环境</li>
 * <li>配置策略：独立配置，避免冲突</li>
 * </ul>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-27 12:26:43
 * @modified 2025-10-27 12:26:43
 * @version 1.0.0 - 初始版本，实现分层Repository模式的数据访问层配置
 */
@Configuration
@Profile("!test") // 排除测试环境，测试环境有独立的配置
@EnableR2dbcRepositories(basePackages = "com.honyrun.repository.r2dbc")
public class R2dbcRepositoryConfig {

    private static final Logger logger = LoggerFactory.getLogger(R2dbcRepositoryConfig.class);

    /**
     * 配置初始化后的日志记录
     *
     * <p>
     * 在配置类初始化完成后记录配置信息，便于调试和监控。
     * </p>
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-27 12:26:43
     * @version 1.0.0
     */
    @PostConstruct
    public void logConfigurationInfo() {
        LoggingUtil.info(logger, "R2DBC Repository配置已初始化 - 扫描包: com.honyrun.repository.r2dbc");
        LoggingUtil.debug(logger, "分层Repository模式 - 数据访问层配置完成");
    }
}