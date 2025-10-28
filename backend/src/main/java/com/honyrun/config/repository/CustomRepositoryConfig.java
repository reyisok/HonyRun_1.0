package com.honyrun.config.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.honyrun.util.LoggingUtil;

import jakarta.annotation.PostConstruct;

/**
 * 自定义Repository配置类
 *
 * <p>
 * <strong>功能说明：</strong>
 * </p>
 * <ul>
 * <li>专门负责自定义Repository实现的配置和管理</li>
 * <li>精确指定组件扫描范围为repository.custom包</li>
 * <li>实现分层Repository模式的自定义实现层</li>
 * <li>与R2DBC Repository配置完全分离</li>
 * </ul>
 *
 * <p>
 * <strong>配置范围：</strong>
 * </p>
 * <ul>
 * <li>扫描包：com.honyrun.repository.custom</li>
 * <li>组件类型：自定义Repository实现类</li>
 * <li>环境支持：所有环境</li>
 * <li>配置策略：独立配置，清晰分层</li>
 * </ul>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-27 12:26:43
 * @modified 2025-10-27 12:26:43
 * @version 1.0.0 - 初始版本，实现分层Repository模式的自定义实现层配置
 */
@Configuration
@ComponentScan(basePackages = "com.honyrun.repository.custom")
public class CustomRepositoryConfig {

    private static final Logger logger = LoggerFactory.getLogger(CustomRepositoryConfig.class);

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
        LoggingUtil.info(logger, "自定义Repository配置已初始化 - 扫描包: com.honyrun.repository.custom");
        LoggingUtil.debug(logger, "分层Repository模式 - 自定义实现层配置完成");
    }
}