package com.honyrun.config.validation;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.util.LoggingUtil;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Redis配置验证器
 *
 * <p>
 * <strong>Redis缓存配置参数验证</strong>
 * </p>
 *
 * <p>
 * <strong>核心功能：</strong>
 * <ul>
 * <li><strong>连接参数验证</strong> - 验证主机、端口、密码等连接参数</li>
 * <li><strong>连接池配置验证</strong> - 验证连接池参数合理性</li>
 * <li><strong>超时配置验证</strong> - 验证各种超时参数设置</li>
 * <li><strong>启动时验证</strong> - 应用启动时自动执行配置验证</li>
 * </ul>
 *
 * <p>
 * <strong>验证规则：</strong>
 * <ul>
 * <li>主机地址不能为空</li>
 * <li>端口号必须在有效范围内</li>
 * <li>连接池参数必须合理</li>
 * <li>超时时间必须为正数</li>
 * </ul>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-06-28 11:35:00
 * @modified 2025-10-26 01:36:12
 * @version 1.1.0 - 修复timeout属性setter方法，重构为构造函数注入
 * @since 1.0.0
 */
@Component
@Validated
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisConfigValidator {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfigValidator.class);

    /**
     * Spring环境对象注入
     */
    private final Environment environment;

    /**
     * 统一配置管理器注入
     */
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * timeout属性，用于Spring Boot配置绑定
     */
    private Duration timeout;

    /**
     * 构造函数注入，符合最佳实践
     *
     * @param environment          Spring环境对象
     * @param unifiedConfigManager 统一配置管理器
     */
    public RedisConfigValidator(Environment environment, UnifiedConfigManager unifiedConfigManager) {
        this.environment = environment;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 应用启动后执行配置验证
     *
     * <p>
     * <strong>验证内容：</strong>
     * <ul>
     * <li>连接参数有效性验证</li>
     * <li>连接池配置合理性验证</li>
     * <li>超时配置验证</li>
     * <li>记录验证结果</li>
     * </ul>
     */
    @PostConstruct
    public void validateConfiguration() {
        LoggingUtil.info(logger, "开始验证Redis配置参数...");

        // 异步执行配置验证，避免阻塞Spring容器初始化
        validateConfigurationAsync()
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        result -> LoggingUtil.info(logger, "✓ Redis配置验证通过"),
                        error -> {
                            LoggingUtil.error(logger, "✗ Redis配置验证失败: {}", error.getMessage());
                            // 在生产环境中，配置验证失败应该阻止应用启动
                            // 这里记录错误但不阻止启动，允许应用在配置修复后重新连接
                        });
    }

    /**
     * 异步验证Redis配置
     *
     * @return 验证结果的Mono
     */
    private Mono<Void> validateConfigurationAsync() {
        return validateConnectionParametersAsync()
                .then(validatePoolConfigurationAsync())
                .then(validateTimeoutConfigurationAsync());
    }

    /**
     * 异步验证连接参数
     *
     * @return 验证结果的Mono
     * @throws IllegalArgumentException 连接参数无效时抛出
     */
    private Mono<Void> validateConnectionParametersAsync() {
        return Mono.zip(getHost(), getPort(), getDatabase())
                .flatMap(tuple -> {
                    String host = tuple.getT1();
                    Integer port = tuple.getT2();
                    String database = tuple.getT3();

                    // 验证主机地址
                    if (host == null || host.trim().isEmpty()) {
                        return Mono.error(new IllegalArgumentException("Redis主机地址不能为空"));
                    }

                    // 验证端口号
                    if (port == null || port <= 0 || port > 65535) {
                        return Mono.error(new IllegalArgumentException("Redis端口号无效: " + port));
                    }

                    // 验证数据库索引
                    if (database != null) {
                        try {
                            int dbIndex = Integer.parseInt(database);
                            if (dbIndex < 0 || dbIndex > 15) {
                                LoggingUtil.warn(logger, "Redis数据库索引({})超出常规范围[0-15]", dbIndex);
                            }
                        } catch (NumberFormatException e) {
                            return Mono.error(new IllegalArgumentException("Redis数据库索引格式错误: " + database));
                        }
                    }

                    LoggingUtil.info(logger, "Redis连接地址: {}:{}", host, port);
                    LoggingUtil.info(logger, "数据库索引: {}", database != null ? database : "0");
                    LoggingUtil.info(logger, "认证配置: {}", getPassword() != null ? "已设置密码" : "无密码");
                    LoggingUtil.info(logger, "连接参数验证通过");

                    return Mono.empty();
                });
    }

    /**
     * 异步验证连接池配置合理性
     *
     * @return 验证结果的Mono
     * @throws IllegalArgumentException 连接池配置不合理时抛出
     */
    private Mono<Void> validatePoolConfigurationAsync() {
        return Mono.zip(getMaxActive(), getMaxIdle(), getMinIdle())
                .flatMap(tuple -> {
                    Integer maxActive = tuple.getT1();
                    Integer maxIdle = tuple.getT2();
                    Integer minIdle = tuple.getT3();

                    // 验证最大连接数
                    if (maxActive != null && maxActive <= 0) {
                        return Mono.error(new IllegalArgumentException("Redis最大连接数必须大于0: " + maxActive));
                    }

                    // 验证最大空闲连接数
                    if (maxIdle != null && maxIdle < 0) {
                        return Mono.error(new IllegalArgumentException("Redis最大空闲连接数不能为负数: " + maxIdle));
                    }

                    // 验证最小空闲连接数
                    if (minIdle != null && minIdle < 0) {
                        return Mono.error(new IllegalArgumentException("Redis最小空闲连接数不能为负数: " + minIdle));
                    }

                    // 验证连接池配置的逻辑关系
                    if (maxActive != null && maxIdle != null && maxIdle > maxActive) {
                        LoggingUtil.warn(logger, "Redis最大空闲连接数({})大于最大连接数({})", maxIdle, maxActive);
                    }

                    if (maxIdle != null && minIdle != null && minIdle > maxIdle) {
                        return Mono.error(new IllegalArgumentException(
                                String.format("Redis最小空闲连接数(%d)不能大于最大空闲连接数(%d)", minIdle, maxIdle)));
                    }

                    LoggingUtil.info(logger, "连接池配置 - 最大连接: {}, 最大空闲: {}, 最小空闲: {}",
                            maxActive, maxIdle, minIdle);
                    LoggingUtil.info(logger, "连接池配置验证通过");

                    return Mono.empty();
                });
    }

    /**
     * 异步验证超时配置合理性
     *
     * @return 验证结果的Mono
     * @throws IllegalArgumentException 超时配置不合理时抛出
     */
    private Mono<Void> validateTimeoutConfigurationAsync() {
        return Mono.zip(getConnectTimeout(), getReadTimeout())
                .flatMap(tuple -> {
                    Integer connectTimeout = tuple.getT1();
                    Integer readTimeout = tuple.getT2();

                    // 验证连接超时
                    if (connectTimeout != null && connectTimeout <= 0) {
                        return Mono.error(new IllegalArgumentException("Redis连接超时必须大于0毫秒: " + connectTimeout));
                    }

                    // 验证读取超时
                    if (readTimeout != null && readTimeout <= 0) {
                        return Mono.error(new IllegalArgumentException("Redis读取超时必须大于0毫秒: " + readTimeout));
                    }

                    // 检查超时配置的合理性
                    if (connectTimeout != null && connectTimeout > 30000) {
                        LoggingUtil.warn(logger, "Redis连接超时({})较长，可能影响应用响应性能", connectTimeout);
                    }

                    if (readTimeout != null && readTimeout > 60000) {
                        LoggingUtil.warn(logger, "Redis读取超时({})较长，可能影响应用响应性能", readTimeout);
                    }

                    LoggingUtil.info(logger, "超时配置 - 连接超时: {}ms, 读取超时: {}ms",
                            connectTimeout, readTimeout);
                    LoggingUtil.info(logger, "超时配置验证通过");

                    return Mono.empty();
                });
    }

    // ==================== Getter方法 ====================

    /**
     * 获取主机地址
     *
     * @return 主机地址的Mono包装
     */
    public Mono<String> getHost() {
        // 使用配置键而非硬编码值，确保配置统一管理
        return unifiedConfigManager.getStringConfig(
                com.honyrun.constant.SystemConstants.REDIS_HOST_CONFIG_KEY)
                .switchIfEmpty(
                        unifiedConfigManager.getStringConfig("spring.data.redis.host")
                                .switchIfEmpty(
                                        Mono.just(
                                                unifiedConfigManager.getProperty("HONYRUN_REDIS_HOST", "localhost"))));
    }

    /**
     * 获取端口号
     *
     * @return 端口号的Mono
     */
    public Mono<Integer> getPort() {
        // 配置标准化说明：使用统一配置管理器获取Redis端口，符合最佳实践
        // 优先级：环境变量 HONYRUN_REDIS_PORT > 配置文件 honyrun.redis.port > 默认值 8902
        return unifiedConfigManager.getIntegerConfig("HONYRUN_REDIS_PORT",
                Integer.parseInt(unifiedConfigManager.getProperty("honyrun.redis.port", "8902")));
    }

    /**
     * 获取密码
     *
     * @return 密码的Mono
     */
    public Mono<String> getPassword() {
        return unifiedConfigManager.getStringConfig("HONYRUN_REDIS_PASSWORD");
    }

    /**
     * 获取数据库
     *
     * @return 数据库的Mono
     */
    public Mono<String> getDatabase() {
        return unifiedConfigManager.getStringConfig("HONYRUN_REDIS_DATABASE", "1");
    }

    /**
     * 获取超时时间
     *
     * @return 超时时间的Mono
     */
    public Mono<Duration> getTimeout() {
        if (timeout != null) {
            return Mono.just(timeout);
        }
        return unifiedConfigManager.getStringConfig("HONYRUN_REDIS_TIMEOUT", "12000")
                .map(timeoutStr -> Duration.parse("PT" + timeoutStr.replaceAll("[^0-9]", "") + "S"));
    }

    /**
     * 获取最大活跃连接数
     *
     * @return 最大活跃连接数的Mono
     */
    public Mono<Integer> getMaxActive() {
        return unifiedConfigManager.getIntegerConfig("spring.data.redis.lettuce.pool.max-active", 8);
    }

    /**
     * 获取最大空闲连接数
     *
     * @return 最大空闲连接数的Mono
     */
    public Mono<Integer> getMaxIdle() {
        return unifiedConfigManager.getIntegerConfig("spring.data.redis.lettuce.pool.max-idle", 8);
    }

    /**
     * 获取最小空闲连接数
     *
     * @return 最小空闲连接数的Mono
     */
    public Mono<Integer> getMinIdle() {
        return unifiedConfigManager.getIntegerConfig("spring.data.redis.lettuce.pool.min-idle", 2);
    }

    /**
     * 获取最大等待时间
     *
     * @return 最大等待时间的Mono
     */
    public Mono<Duration> getMaxWait() {
        return unifiedConfigManager.getStringConfig("spring.data.redis.lettuce.pool.max-wait", "5000")
                .map(maxWaitStr -> Duration.ofMillis(Long.parseLong(maxWaitStr)));
    }

    // ==================== Setter方法 - Spring Boot属性绑定需要 ====================

    /**
     * 设置超时时间（Duration类型）
     *
     * @param timeout 超时时间
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
        LoggingUtil.debug(logger, "接收到timeout配置（Duration类型）: {}", timeout);
    }

    /**
     * 设置超时时间（String类型，兼容性）
     *
     * @param timeout 超时时间字符串
     */
    public void setTimeout(String timeout) {
        // 这个setter方法是为了满足Spring Boot的@ConfigurationProperties属性绑定要求
        // 实际的配置值通过UnifiedConfigManager管理
        LoggingUtil.debug(logger, "接收到timeout配置（String类型）: {}", timeout);
        if (timeout != null && !timeout.trim().isEmpty()) {
            try {
                this.timeout = Duration.parse("PT" + timeout.replaceAll("[^0-9]", "") + "S");
            } catch (Exception e) {
                LoggingUtil.warn(logger, "无法解析timeout配置: {}", timeout);
            }
        }
    }

    /**
     * 设置主机地址
     *
     * @param host 主机地址
     */
    public void setHost(String host) {
        LoggingUtil.debug(logger, "接收到host配置: {}", host);
    }

    /**
     * 设置端口号
     *
     * @param port 端口号
     */
    public void setPort(Integer port) {
        LoggingUtil.debug(logger, "接收到port配置: {}", port);
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        LoggingUtil.debug(logger, "接收到password配置: [已隐藏]");
    }

    /**
     * 设置数据库
     *
     * @param database 数据库
     */
    public void setDatabase(String database) {
        LoggingUtil.debug(logger, "接收到database配置: {}", database);
    }

    /**
     * 获取连接超时时间
     *
     * @return 连接超时时间的Mono
     */
    public Mono<Integer> getConnectTimeout() {
        return unifiedConfigManager.getIntegerConfig("honyrun.redis.connect-timeout", 5000);
    }

    /**
     * 获取读取超时时间
     *
     * @return 读取超时时间的Mono
     */
    public Mono<Integer> getReadTimeout() {
        return unifiedConfigManager.getIntegerConfig("honyrun.redis.read-timeout", 3000);
    }
}
