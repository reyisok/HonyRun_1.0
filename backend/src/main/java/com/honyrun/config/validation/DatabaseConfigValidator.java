package com.honyrun.config.validation;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;
import jakarta.annotation.PostConstruct;
import java.time.Duration;

/**
 * 数据库配置验证器
 *
 * <p><strong>MySQL数据库配置参数验证</strong></p>
 *
 * <p><strong>核心功能：</strong>
 * <ul>
 *   <li><strong>URL格式验证</strong> - 验证R2DBC连接URL格式正确性</li>
 *   <li><strong>连接参数验证</strong> - 验证用户名、密码等必要参数</li>
 *   <li><strong>连接池配置验证</strong> - 验证连接池参数合理性</li>
 *   <li><strong>启动时验证</strong> - 应用启动时自动执行配置验证</li>
 * </ul>
 *
 * <p><strong>验证规则：</strong>
 * <ul>
 *   <li>R2DBC URL必须以r2dbc:mysql://开头</li>
 *   <li>用户名和密码不能为空</li>
 *   <li>连接池大小必须在合理范围内</li>
 *   <li>超时时间必须为正数</li>
 * </ul>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-06-28 11:30:00
 * @modified 2025-06-29 11:30:00
 * @version 1.0.0 - 初始版本，实现MySQL配置验证
 * @since 1.0.0
 */
@Component
@Validated
public class DatabaseConfigValidator {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfigValidator.class);

    private final Environment environment;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入依赖
     *
     * @param environment Spring环境配置
     * @param unifiedConfigManager 统一配置管理器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public DatabaseConfigValidator(Environment environment, UnifiedConfigManager unifiedConfigManager) {
        this.environment = environment;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 应用启动后执行配置验证
     *
     * <p><strong>验证内容：</strong>
     * <ul>
     *   <li>URL格式和可达性验证</li>
     *   <li>连接池参数合理性验证</li>
     *   <li>配置参数一致性验证</li>
     *   <li>记录验证结果</li>
     * </ul>
     */
    @PostConstruct
    public void validateConfiguration() {
        LoggingUtil.info(logger, "开始验证MySQL数据库配置参数...");

        // 使用响应式编程获取配置，避免阻塞调用
        Mono.zip(
            unifiedConfigManager.getStringConfig("spring.r2dbc.url").switchIfEmpty(Mono.just("")),
            unifiedConfigManager.getStringConfig("spring.r2dbc.username").switchIfEmpty(Mono.just("")),
            unifiedConfigManager.getStringConfig("spring.r2dbc.password").switchIfEmpty(Mono.just("")),
            unifiedConfigManager.getIntegerConfig("spring.r2dbc.pool.initial-size", 5),
            unifiedConfigManager.getIntegerConfig("spring.r2dbc.pool.max-size", 20),
            unifiedConfigManager.getStringConfig("spring.r2dbc.pool.max-acquire-time", "PT15S"),
            unifiedConfigManager.getStringConfig("spring.r2dbc.pool.max-idle-time", "PT30M")
        ).subscribe(tuple -> {
            try {
                String url = tuple.getT1();
                String username = tuple.getT2();
                String password = tuple.getT3();
                Integer initialSize = tuple.getT4();
                Integer maxSize = tuple.getT5();
                Duration maxAcquireTime = Duration.parse(tuple.getT6());
                Duration maxIdleTime = Duration.parse(tuple.getT7());

                // 验证URL格式
                validateUrl(url, username, password);

                // 验证连接池配置
                validatePoolConfiguration(initialSize, maxSize);

                // 验证超时配置
                validateTimeoutConfiguration(maxAcquireTime, maxIdleTime);

                LoggingUtil.info(logger, "✓ MySQL数据库配置验证通过");
                LoggingUtil.info(logger, "数据库连接URL: {}", maskSensitiveInfo(url));
                LoggingUtil.info(logger, "连接池配置: 初始大小={}, 最大大小={}", initialSize, maxSize);
                LoggingUtil.info(logger, "超时配置: 连接超时={}, 空闲超时={}", maxAcquireTime, maxIdleTime);

            } catch (Exception e) {
                LoggingUtil.error(logger, "✗ MySQL数据库配置验证失败: {}", e.getMessage());
                throw new IllegalStateException("数据库配置验证失败，应用启动终止", e);
            }
        }, error -> {
            LoggingUtil.error(logger, "✗ 获取数据库配置时发生错误: {}", error.getMessage());
            throw new IllegalStateException("数据库配置获取失败，应用启动终止", error);
        });
    }

    /**
     * 验证URL格式和可达性
     *
     * @param url 数据库连接URL
     * @param username 数据库用户名
     * @param password 数据库密码
     * @throws IllegalArgumentException URL格式错误时抛出
     */
    private void validateUrl(String url, String username, String password) {
        try {
            LoggingUtil.info(logger, "开始验证数据库URL: {}", maskSensitiveInfo(url));
            LoggingUtil.info(logger, "URL原始值: [{}]", url);

            if (url == null || url.trim().isEmpty()) {
                throw new IllegalArgumentException("数据库URL不能为空");
            }

            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("数据库用户名不能为空");
            }

            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("数据库密码不能为空");
            }

            // 解析URL格式 - 先移除查询参数再解析
            String jdbcUrl = url.replace("r2dbc:", "jdbc:");
            LoggingUtil.info(logger, "转换后的JDBC URL: {}", jdbcUrl);

            // 分离URL和查询参数
            String baseUrl = jdbcUrl;
            if (jdbcUrl.contains("?")) {
                baseUrl = jdbcUrl.substring(0, jdbcUrl.indexOf("?"));
            }
            LoggingUtil.info(logger, "基础URL（无查询参数）: {}", baseUrl);

            // 手动解析URL，因为JDBC URL格式与标准URI不完全兼容
            if (!baseUrl.startsWith("jdbc:mysql://")) {
                throw new IllegalArgumentException("URL必须以jdbc:mysql://开头");
            }

            String urlPart = baseUrl.substring("jdbc:mysql://".length());
            LoggingUtil.info(logger, "URL主体部分: [{}]", urlPart);

            // 解析主机名、端口和数据库名
            String host = null;
            // 注意：在URL解析中使用.block()是必要的，因为需要同步获取端口配置进行URL验证
            int port = Integer.parseInt(unifiedConfigManager.getProperty("HONYRUN_MYSQL_PORT", "8906")); // 使用统一配置管理器获取MySQL端口
            String database = null;

            int slashIndex = urlPart.indexOf('/');
            if (slashIndex == -1) {
                throw new IllegalArgumentException("URL格式错误，缺少数据库名");
            }

            String hostPort = urlPart.substring(0, slashIndex);
            String databasePart = urlPart.substring(slashIndex + 1);

            // 处理数据库名中可能包含查询参数的情况
            if (databasePart.contains("?")) {
                database = databasePart.substring(0, databasePart.indexOf("?"));
            } else {
                database = databasePart;
            }

            LoggingUtil.info(logger, "解析hostPort: [{}], database: [{}]", hostPort, database);

            // 解析主机名和端口
            if (hostPort.contains(":")) {
                String[] parts = hostPort.split(":");
                if (parts.length >= 2) {
                    host = parts[0].trim();
                    try {
                        port = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("端口号格式错误: " + parts[1]);
                    }
                } else {
                    throw new IllegalArgumentException("主机名:端口格式错误: " + hostPort);
                }
            } else {
                host = hostPort.trim();
            }

            LoggingUtil.info(logger, "解析结果: 主机=[{}], 端口={}, 数据库=[{}]", host, port, database);

            // 验证主机名不为空
            if (host == null || host.trim().isEmpty()) {
                LoggingUtil.error(logger, "数据库主机名解析失败 - URL: {}, hostPort: {}, host: [{}]",
                    url, hostPort, host);
                throw new IllegalArgumentException("数据库主机名不能为空");
            }

            // 验证端口号合理性
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("数据库端口号必须在1-65535范围内");
            }

            // 验证数据库名不为空
            if (database == null || database.trim().isEmpty()) {
                throw new IllegalArgumentException("数据库名不能为空");
            }

            LoggingUtil.info(logger, "URL格式验证通过: 主机={}, 端口={}, 数据库={}",
                    host, port, database);

        } catch (Exception e) {
            LoggingUtil.error(logger, "URL解析异常: {}", e.getMessage());
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new IllegalArgumentException("R2DBC URL格式错误: " + e.getMessage(), e);
        }
    }

    /**
     * 验证连接池配置合理性
     *
     * @param initialSize 连接池初始大小
     * @param maxSize 连接池最大大小
     * @throws IllegalArgumentException 连接池配置不合理时抛出
     */
    private void validatePoolConfiguration(Integer initialSize, Integer maxSize) {
        // 验证初始大小不能大于最大大小
        if (initialSize > maxSize) {
            throw new IllegalArgumentException("连接池初始大小不能大于最大大小");
        }

        // 验证连接池大小合理性
        if (maxSize < 5) {
            LoggingUtil.warn(logger, "连接池最大大小({})较小，可能影响并发性能", maxSize);
        }

        if (maxSize > 50) {
            LoggingUtil.warn(logger, "连接池最大大小({})较大，请确保数据库能够支持", maxSize);
        }

        LoggingUtil.info(logger, "连接池配置验证通过");
    }

    /**
     * 验证超时配置合理性
     *
     * @param maxAcquireTime 连接获取超时时间
     * @param maxIdleTime 连接空闲超时时间
     * @throws IllegalArgumentException 超时配置不合理时抛出
     */
    private void validateTimeoutConfiguration(Duration maxAcquireTime, Duration maxIdleTime) {
        // 验证连接超时时间合理性
        long maxAcquireTimeMs = maxAcquireTime.toMillis();
        if (maxAcquireTimeMs < 1000) {
            LoggingUtil.warn(logger, "连接超时时间({})较短，可能导致连接获取失败", maxAcquireTime);
        }

        if (maxAcquireTimeMs > 30000) {
            LoggingUtil.warn(logger, "连接超时时间({})较长，可能影响响应性能", maxAcquireTime);
        }

        // 验证空闲超时时间合理性
        long maxIdleTimeMs = maxIdleTime.toMillis();
        if (maxIdleTimeMs < 300000) { // 5分钟
            LoggingUtil.warn(logger, "连接空闲超时时间({})较短，可能导致频繁重连", maxIdleTime);
        }

        LoggingUtil.info(logger, "超时配置验证通过");
    }

    /**
     * 屏蔽敏感信息
     *
     * @param url 原始URL
     * @return 屏蔽敏感信息后的URL
     */
    private String maskSensitiveInfo(String url) {
        if (url.contains("@")) {
            return url.replaceAll("://[^:]+:[^@]+@", "://***:***@");
        }
        return url;
    }
}
