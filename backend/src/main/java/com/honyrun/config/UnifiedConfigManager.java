package com.honyrun.config;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.honyrun.config.properties.HonyRunProperties;
import com.honyrun.config.properties.JwtProperties;
import com.honyrun.config.properties.NettyProperties;
import com.honyrun.config.properties.PathConfigProperties;
import com.honyrun.config.properties.ReactiveProperties;
import com.honyrun.config.system.EnvironmentConfigProcessor;
import com.honyrun.config.system.SystemConfigManager;
import com.honyrun.model.entity.system.SystemConfig;
import com.honyrun.util.LoggingUtil;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 统一配置管理器
 *
 * 【@Primary注解说明】：
 * UnifiedConfigManager作为全局配置管理的核心组件，需要确保在整个应用中的唯一性。
 * 使用@Primary注解的合理性：
 * 1. 全局配置管理：作为配置管理的统一入口，应该保持单例
 * 2. 依赖注入简化：避免在其他组件中使用@Qualifier指定具体实现
 * 3. 配置一致性：确保所有组件使用相同的配置管理策略
 * 4. 框架集成：与Spring Boot配置体系无缝集成
 *
 * 负责整合和管理所有配置相关的功能，提供统一的配置访问接口。
 * 整合系统配置、环境配置、属性配置等多种配置源，提供统一的配置管理策略。
 *
 * 主要功能：
 * - 统一配置源管理
 * - 配置优先级控制
 * - 配置缓存和刷新
 * - 配置变更通知
 * - 配置验证和校验
 * - 环境特定配置支持
 *
 * 配置优先级（从高到低）：
 * 1. 环境变量
 * 2. 系统属性
 * 3. 数据库环境特定配置
 * 4. 数据库通用配置
 * 5. 配置文件属性
 *
 * @author Mr.Rey Copyright © 2025
 * @version 2.1.0
 * @created 2025-07-01 15:45:00
 * @modified 2025-10-26 01:36:12
 *           修复循环依赖问题，使用ApplicationContext延迟获取EnvironmentConfigProcessor
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Configuration
@Primary
@EnableConfigurationProperties({ HonyRunProperties.class, ReactiveProperties.class, NettyProperties.class,
        JwtProperties.class, PathConfigProperties.class })
public class UnifiedConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedConfigManager.class);

    private final ApplicationContext applicationContext;
    private final SystemConfigManager systemConfigManager;
    private final HonyRunProperties honyRunProperties;
    private final ReactiveProperties reactiveProperties;
    private final NettyProperties nettyProperties;
    private final JwtProperties jwtProperties;
    private final PathConfigProperties pathConfigProperties;
    private final Environment environment;

    /**
     * 构造函数注入，避免循环依赖
     *
     * @param applicationContext   Spring应用上下文，用于延迟获取EnvironmentConfigProcessor
     * @param systemConfigManager  系统配置管理器
     * @param honyRunProperties    HonyRun属性配置
     * @param reactiveProperties   响应式属性配置
     * @param nettyProperties      Netty属性配置
     * @param jwtProperties        JWT属性配置
     * @param pathConfigProperties 路径配置属性
     * @param environment          Spring环境对象
     */
    public UnifiedConfigManager(
            ApplicationContext applicationContext,
            @Qualifier("systemConfigManager") SystemConfigManager systemConfigManager,
            HonyRunProperties honyRunProperties,
            ReactiveProperties reactiveProperties,
            NettyProperties nettyProperties,
            JwtProperties jwtProperties,
            PathConfigProperties pathConfigProperties,
            Environment environment) {
        this.applicationContext = applicationContext;
        this.systemConfigManager = systemConfigManager;
        this.honyRunProperties = honyRunProperties;
        this.reactiveProperties = reactiveProperties;
        this.nettyProperties = nettyProperties;
        this.jwtProperties = jwtProperties;
        this.pathConfigProperties = pathConfigProperties;
        this.environment = environment;
    }

    /**
     * 延迟获取EnvironmentConfigProcessor，避免循环依赖
     *
     * @return EnvironmentConfigProcessor实例
     */
    private EnvironmentConfigProcessor getEnvironmentConfigProcessor() {
        return applicationContext.getBean("environmentConfigProcessor", EnvironmentConfigProcessor.class);
    }

    /**
     * 配置缓存
     */
    private final Map<String, ConfigCacheEntry> configCache = new ConcurrentHashMap<>();

    /**
     * 配置管理器状态
     */
    private ConfigManagerStatus status = ConfigManagerStatus.INITIALIZING;

    @PostConstruct
    public void init() {
        LoggingUtil.info(logger, "Initializing unified configuration manager");

        try {
            // 初始化配置缓存
            initializeConfigCache();

            // 验证配置完整性
            validateConfigurations();

            status = ConfigManagerStatus.READY;
            LoggingUtil.info(logger, "Configuration manager initialized successfully");

        } catch (Exception e) {
            status = ConfigManagerStatus.ERROR;
            LoggingUtil.error(logger, "Failed to initialize configuration manager: {}", e.getMessage(), e);
            throw new RuntimeException("Configuration manager initialization failed", e);
        }
    }

    // ==================== 路径配置管理接口 ====================

    /**
     * 获取路径配置属性值
     * 按优先级顺序查找路径配置
     *
     * @param pathKey 路径配置键名（相对于honyrun.path前缀）
     * @return 路径配置值的Mono包装
     */
    public Mono<String> getPathProperty(String pathKey) {
        return getStringConfig("honyrun.path." + pathKey);
    }

    /**
     * 获取路径配置属性值，带默认值
     *
     * @param pathKey      路径配置键名（相对于honyrun.path前缀）
     * @param defaultValue 默认值
     * @return 路径配置值的Mono包装
     */
    public Mono<String> getPathProperty(String pathKey, String defaultValue) {
        return getPathProperty(pathKey)
                .switchIfEmpty(Mono.just(defaultValue));
    }

    // ==================== 统一配置访问接口 ====================

    /**
     * 获取字符串配置值（同步方法）
     * 按优先级顺序查找配置值
     *
     * @param configKey 配置键名
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getProperty(String configKey, String defaultValue) {
        try {
            // 优先级1: 环境变量
            String envValue = System.getenv(configKey.toUpperCase().replace(".", "_"));
            if (envValue != null) {
                LoggingUtil.debug(logger, "Config found in environment variables: {}", configKey);
                return envValue;
            }

            // 优先级2: 系统属性
            String sysValue = System.getProperty(configKey);
            if (sysValue != null) {
                LoggingUtil.debug(logger, "Config found in system properties: {}", configKey);
                return sysValue;
            }

            // 优先级3: Spring Environment
            String springValue = environment.getProperty(configKey);
            if (springValue != null) {
                LoggingUtil.debug(logger, "Config found in Spring environment: {}", configKey);
                return springValue;
            }

            // 返回默认值
            return defaultValue;
        } catch (Exception e) {
            LoggingUtil.error(logger, "Failed to get property: {}", configKey, e);
            return defaultValue;
        }
    }

    /**
     * 获取字符串配置值（同步方法）
     *
     * @param configKey 配置键名
     * @return 配置值
     */
    public String getProperty(String configKey) {
        return getProperty(configKey, null);
    }

    /**
     * 获取字符串配置值
     * 按优先级顺序查找配置值
     *
     * @param configKey 配置键名
     * @return 配置值的Mono包装
     */
    public Mono<String> getStringConfig(String configKey) {
        return getConfigWithPriority(configKey, String.class);
    }

    /**
     * 获取字符串配置值，带默认值
     *
     * @param configKey    配置键名
     * @param defaultValue 默认值
     * @return 配置值的Mono包装
     */
    public Mono<String> getStringConfig(String configKey, String defaultValue) {
        return getStringConfig(configKey)
                .switchIfEmpty(Mono.just(defaultValue));
    }

    /**
     * 获取整数配置值
     *
     * @param configKey 配置键名
     * @return 配置值的Mono包装
     */
    public Mono<Integer> getIntegerConfig(String configKey) {
        return getConfigWithPriority(configKey, Integer.class);
    }

    /**
     * 获取整数配置值，带默认值
     *
     * @param configKey    配置键名
     * @param defaultValue 默认值
     * @return 配置值的Mono包装
     */
    public Mono<Integer> getIntegerConfig(String configKey, Integer defaultValue) {
        return getIntegerConfig(configKey)
                .switchIfEmpty(Mono.just(defaultValue));
    }

    /**
     * 获取布尔配置值
     *
     * @param configKey 配置键名
     * @return 配置值的Mono包装
     */
    public Mono<Boolean> getBooleanConfig(String configKey) {
        return getConfigWithPriority(configKey, Boolean.class);
    }

    /**
     * 获取布尔配置值，带默认值
     *
     * @param configKey    配置键名
     * @param defaultValue 默认值
     * @return 配置值的Mono包装
     */
    public Mono<Boolean> getBooleanConfig(String configKey, Boolean defaultValue) {
        return getBooleanConfig(configKey)
                .switchIfEmpty(Mono.just(defaultValue));
    }

    /**
     * 获取Long类型配置值
     *
     * @param configKey 配置键
     * @return Long类型配置值的Mono
     */
    public Mono<Long> getLongConfig(String configKey) {
        return getLongConfig(configKey, null);
    }

    /**
     * 获取Long类型配置值（带默认值）
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return Long类型配置值的Mono
     */
    public Mono<Long> getLongConfig(String configKey, Long defaultValue) {
        return getConfigWithPriority(configKey, String.class)
                .map(value -> {
                    try {
                        return Long.parseLong(value);
                    } catch (NumberFormatException e) {
                        LoggingUtil.warn(logger, "Failed to parse Long config for key: {}, value: {}, using default: {}", 
                                configKey, value, defaultValue);
                        return defaultValue;
                    }
                })
                .switchIfEmpty(Mono.justOrEmpty(defaultValue));
    }

    /**
     * 获取Float类型配置值
     *
     * @param configKey 配置键
     * @return Float类型配置值的Mono
     */
    public Mono<Float> getFloatConfig(String configKey) {
        return getFloatConfig(configKey, null);
    }

    /**
     * 获取Float类型配置值（带默认值）
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return Float类型配置值的Mono
     */
    public Mono<Float> getFloatConfig(String configKey, Float defaultValue) {
        return getConfigWithPriority(configKey, String.class)
                .map(value -> {
                    try {
                        return Float.parseFloat(value);
                    } catch (NumberFormatException e) {
                        LoggingUtil.warn(logger, "Failed to parse Float config for key: {}, value: {}, using default: {}", 
                                configKey, value, defaultValue);
                        return defaultValue;
                    }
                })
                .switchIfEmpty(Mono.justOrEmpty(defaultValue));
    }

    // ==================== 配置优先级处理 ====================

    /**
     * 按优先级获取配置值
     *
     * @param configKey  配置键名
     * @param targetType 目标类型
     * @return 配置值的Mono包装
     */
    private <T> Mono<T> getConfigWithPriority(String configKey, Class<T> targetType) {
        LoggingUtil.debug(logger, "Getting config with priority: {} (type: {})", configKey, targetType.getSimpleName());

        // 1. 检查缓存
        ConfigCacheEntry cacheEntry = configCache.get(configKey);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            LoggingUtil.debug(logger, "Config found in cache: {}", configKey);
            T cachedValue = convertValue(cacheEntry.getValue(), targetType);
            return Mono.just(cachedValue);
        }

        // 2. 按优先级查找配置 - 使用subscribeOn避免阻塞
        return Mono.fromCallable(() -> {
            // 环境变量
            String envValue = System.getenv(configKey.toUpperCase().replace(".", "_"));
            if (envValue != null) {
                LoggingUtil.debug(logger, "Config found in environment variables: {}", configKey);
                return envValue;
            }

            // 系统属性
            String sysValue = System.getProperty(configKey);
            if (sysValue != null) {
                LoggingUtil.debug(logger, "Config found in system properties: {}", configKey);
                return sysValue;
            }

            return null;
        })
                .subscribeOn(Schedulers.boundedElastic()) // 在弹性调度器上执行，避免阻塞
                .cast(String.class)
                .switchIfEmpty(
                        // 配置文件属性 - 优先使用本地配置，避免数据库查询
                        getPropertyFileConfig(configKey)
                                .subscribeOn(Schedulers.boundedElastic())
                                .doOnNext(value -> LoggingUtil.debug(logger, "Config found in property files: {}",
                                        configKey)))
                .switchIfEmpty(
                        // 数据库环境特定配置 - 在弹性调度器上执行
                        getEnvironmentConfigProcessor().getEnvironmentConfig(configKey)
                                .subscribeOn(Schedulers.boundedElastic())
                                .doOnNext(value -> LoggingUtil.debug(logger,
                                        "Config found in environment-specific database: {}", configKey)))
                .switchIfEmpty(
                        // 数据库通用配置 - 在弹性调度器上执行
                        systemConfigManager.getStringConfig(configKey)
                                .subscribeOn(Schedulers.boundedElastic())
                                .doOnNext(value -> LoggingUtil.debug(logger, "Config found in general database: {}",
                                        configKey)))
                .map(value -> {
                    T convertedValue = convertValue(value, targetType);
                    // 缓存配置值
                    cacheConfig(configKey, convertedValue);
                    return convertedValue;
                })
                .doOnError(error -> LoggingUtil.error(logger, "Failed to get config: {}", configKey, error));
    }

    /**
     * 从配置文件属性中获取配置
     *
     * @param configKey 配置键名
     * @return 配置值的Mono包装
     */
    private Mono<String> getPropertyFileConfig(String configKey) {
        return Mono.fromCallable(() -> {
            // 这里可以根据需要从不同的属性配置中获取值
            // 示例：从HonyRunProperties中获取
            if (configKey.startsWith("honyrun.path.")) {
                return getFromPathConfigProperties(configKey);
            } else if (configKey.startsWith("honyrun.")) {
                return getFromHonyRunProperties(configKey);
            } else if (configKey.startsWith("reactive.")) {
                return getFromReactiveProperties(configKey);
            } else if (configKey.startsWith("netty.")) {
                return getFromNettyProperties(configKey);
            }
            // 直接从环境变量中获取其他配置
            return environment.getProperty(configKey);
        })
                .subscribeOn(Schedulers.boundedElastic()); // 在弹性调度器上执行，避免阻塞
    }

    // ==================== 配置缓存管理 ====================

    /**
     * 缓存配置值
     *
     * @param configKey 配置键名
     * @param value     配置值
     */
    private void cacheConfig(String configKey, Object value) {
        configCache.put(configKey, new ConfigCacheEntry(value, LocalDateTime.now()));
        LoggingUtil.debug(logger, "Config cached: {}", configKey);
    }

    /**
     * 清除配置缓存
     */
    public void clearConfigCache() {
        LoggingUtil.info(logger, "Clearing configuration cache");
        configCache.clear();
        systemConfigManager.clearConfigCache();
    }

    /**
     * 清除指定配置的缓存
     *
     * @param configKey 配置键名
     */
    public void clearConfigCache(String configKey) {
        LoggingUtil.debug(logger, "Clearing cache for config: {}", configKey);
        configCache.remove(configKey);
    }

    /**
     * 获取Duration配置值
     * 按优先级顺序查找配置值
     *
     * @param configKey 配置键名
     * @return Duration配置值的Mono包装
     */
    public Mono<Duration> getDurationConfig(String configKey) {
        return getConfigWithPriority(configKey, Duration.class);
    }

    /**
     * 获取Duration配置值，带默认值
     * 按优先级顺序查找配置值
     *
     * @param configKey    配置键名
     * @param defaultValue 默认值
     * @return Duration配置值的Mono包装
     */
    public Mono<Duration> getDurationConfig(String configKey, Duration defaultValue) {
        return getDurationConfig(configKey)
                .switchIfEmpty(Mono.just(defaultValue))
                .doOnNext(value -> {
                    if (value != null) {
                        cacheConfig(configKey, value);
                    }
                });
    }

    // ==================== 配置管理功能 ====================

    /**
     * 获取所有配置
     *
     * @return 所有配置的Flux包装
     */
    public Flux<SystemConfig> getAllConfigs() {
        return systemConfigManager.getAllEnabledConfigs();
    }

    /**
     * 获取配置分组
     *
     * @param configGroup 配置分组
     * @return 配置列表的Flux包装
     */
    public Flux<SystemConfig> getConfigsByGroup(String configGroup) {
        return systemConfigManager.getConfigsByGroup(configGroup);
    }

    /**
     * 设置配置值
     *
     * @param configKey   配置键名
     * @param configValue 配置值
     * @param configGroup 配置分组
     * @param configType  配置类型
     * @return 设置结果的Mono包装
     */
    public Mono<SystemConfig> setConfig(String configKey, String configValue,
            String configGroup, String configType) {
        return systemConfigManager.setSystemConfig(configKey, configValue, configGroup, configType)
                .doOnNext(config -> clearConfigCache(configKey));
    }

    /**
     * 检查配置是否存在
     *
     * @param configKey 配置键名
     * @return 是否存在的Mono包装
     */
    public Mono<Boolean> configExists(String configKey) {
        return systemConfigManager.configExists(configKey);
    }

    /**
     * 获取配置管理器状态
     *
     * @return 配置管理器状态
     */
    public ConfigManagerStatus getStatus() {
        return status;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 初始化配置缓存
     */
    private void initializeConfigCache() {
        LoggingUtil.debug(logger, "Initializing configuration cache");
        // 预加载一些常用配置到缓存
        // 这里可以根据需要预加载特定的配置项
    }

    /**
     * 验证配置完整性
     */
    private void validateConfigurations() {
        LoggingUtil.debug(logger, "Validating configuration integrity");

        try {
            // 验证JWT配置
            if (jwtProperties != null && !jwtProperties.isValid()) {
                throw new IllegalStateException("JWT配置验证失败");
            }

            // 验证响应式配置
            if (reactiveProperties != null) {
                // 验证响应式配置的有效性
                validateReactiveProperties();
            }

            // 验证Netty配置
            if (nettyProperties != null) {
                // 验证Netty配置的有效性
                validateNettyProperties();
            }

            // 验证路径配置
            if (pathConfigProperties != null) {
                // 验证路径配置的有效性
                validatePathConfig();
            }

            // 验证数据库连接配置
            validateDatabaseConfiguration();

            // 验证Redis连接配置
            validateRedisConfiguration();

            LoggingUtil.info(logger, "Configuration integrity validation completed successfully");

        } catch (Exception e) {
            LoggingUtil.error(logger, "Configuration validation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Configuration validation failed", e);
        }
    }

    /**
     * 验证响应式配置
     */
    private void validateReactiveProperties() {
        // 这里可以添加响应式配置的具体验证逻辑
        LoggingUtil.debug(logger, "Validating reactive properties");
    }

    /**
     * 验证Netty配置
     */
    private void validateNettyProperties() {
        // 这里可以添加Netty配置的具体验证逻辑
        LoggingUtil.debug(logger, "Validating Netty properties");
    }

    /**
     * 验证数据库配置
     */
    private void validateDatabaseConfiguration() {
        LoggingUtil.debug(logger, "Validating database configuration");
        // 验证数据库连接URL、用户名等必要配置
        String dbUrl = environment.getProperty("spring.r2dbc.url");
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            throw new IllegalStateException("数据库连接URL未配置");
        }

        String dbUsername = environment.getProperty("spring.r2dbc.username");
        if (dbUsername == null || dbUsername.trim().isEmpty()) {
            throw new IllegalStateException("数据库用户名未配置");
        }
    }

    /**
     * 验证路径配置
     */
    private void validatePathConfig() {
        LoggingUtil.debug(logger, "Validating path configuration");

        // 验证src基础路径配置
        if (pathConfigProperties.getSrcBase() == null || pathConfigProperties.getSrcBase().trim().isEmpty()) {
            throw new IllegalStateException("src基础路径未配置");
        }

        // 验证日志目录配置
        if (pathConfigProperties.getLogsDir() == null || pathConfigProperties.getLogsDir().trim().isEmpty()) {
            throw new IllegalStateException("日志目录未配置");
        }
    }

    /**
     * 验证Redis配置
     */
    private void validateRedisConfiguration() {
        LoggingUtil.debug(logger, "Validating Redis configuration");
        // 验证Redis连接配置
        String redisHost = environment.getProperty("spring.data.redis.host");
        if (redisHost == null || redisHost.trim().isEmpty()) {
            throw new IllegalStateException("Redis主机地址未配置");
        }

        String redisPort = environment.getProperty("spring.data.redis.port");
        if (redisPort == null || redisPort.trim().isEmpty()) {
            throw new IllegalStateException("Redis端口未配置");
        }

        try {
            int port = Integer.parseInt(redisPort);
            if (port <= 0 || port > 65535) {
                throw new IllegalStateException("Redis端口配置无效: " + port);
            }
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Redis端口配置格式错误: " + redisPort);
        }
    }

    /**
     * 值类型转换
     *
     * @param value      原始值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    /**
     * 转换配置值到指定类型
     *
     * @param value      原始值
     * @param targetType 目标类型
     * @return 转换后的值
     * @author: Mr.Rey Copyright © 2025 @created 2025-07-01 @modified
     *          2025-07-01 @version 1.0
     */
    private <T> T convertValue(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isInstance(value)) {
            return targetType.cast(value);
        }

        String stringValue = value.toString();

        if (targetType == String.class) {
            return targetType.cast(stringValue);
        } else if (targetType == Integer.class) {
            return targetType.cast(Integer.valueOf(stringValue));
        } else if (targetType == Boolean.class) {
            return targetType.cast(Boolean.valueOf("true".equalsIgnoreCase(stringValue) || "1".equals(stringValue)));
        } else if (targetType == Long.class) {
            return targetType.cast(Long.valueOf(stringValue));
        } else if (targetType == Double.class) {
            return targetType.cast(Double.valueOf(stringValue));
        } else if (targetType == Duration.class) {
            // 支持Duration类型转换
            // 支持格式: PT30M (ISO-8601), 30m, 30 (默认分钟)
            try {
                if (stringValue.startsWith("PT") || stringValue.startsWith("P")) {
                    return targetType.cast(Duration.parse(stringValue));
                } else if (stringValue.matches("\\d+[smhd]")) {
                     // 支持简单格式: 30s, 30m, 30h, 30d
                     char unit = stringValue.charAt(stringValue.length() - 1);
                     long durationValue = Long.parseLong(stringValue.substring(0, stringValue.length() - 1));
                     switch (unit) {
                         case 's': return targetType.cast(Duration.ofSeconds(durationValue));
                         case 'm': return targetType.cast(Duration.ofMinutes(durationValue));
                         case 'h': return targetType.cast(Duration.ofHours(durationValue));
                         case 'd': return targetType.cast(Duration.ofDays(durationValue));
                     }
                } else {
                    // 默认按分钟处理
                    return targetType.cast(Duration.ofMinutes(Long.parseLong(stringValue)));
                }
            } catch (Exception e) {
                LoggingUtil.warn(logger, "Failed to parse Duration from value: {}, using default", stringValue);
                return null;
            }
        }

        throw new IllegalArgumentException("Unsupported target type: " + targetType);
    }

    /**
     * 从HonyRunProperties获取配置
     */
    private String getFromHonyRunProperties(String configKey) {
        // 实现从HonyRunProperties获取配置的逻辑
        if (honyRunProperties == null) {
            return null;
        }
        // 根据configKey从honyRunProperties获取对应的配置值
        // 这里可以根据实际的HonyRunProperties结构来实现
        return null;
    }

    /**
     * 从ReactiveProperties获取配置
     */
    private String getFromReactiveProperties(String configKey) {
        // 实现从ReactiveProperties获取配置的逻辑
        if (reactiveProperties == null) {
            return null;
        }
        // 根据configKey从reactiveProperties获取对应的配置值
        // 这里可以根据实际的ReactiveProperties结构来实现
        return null;
    }

    /**
     * 从NettyProperties获取配置
     */
    private String getFromNettyProperties(String configKey) {
        // 实现从NettyProperties获取配置的逻辑
        if (nettyProperties == null) {
            return null;
        }
        // 根据configKey从nettyProperties获取对应的配置值
        // 这里可以根据实际的NettyProperties结构来实现
        return null;
    }

    /**
     * 从PathConfigProperties获取路径配置
     */
    private String getFromPathConfigProperties(String configKey) {
        // 实现从PathConfigProperties获取配置的逻辑
        if (pathConfigProperties == null) {
            LoggingUtil.warn(logger, "PathConfigProperties未初始化，配置键: {}", configKey);
            return getDefaultPathValue(configKey);
        }

        // 去除honyrun.path前缀
        String key = configKey.substring("honyrun.path.".length());

        String value = null;
        switch (key) {
            case "src-base":
                value = pathConfigProperties.getSrcBase();
                break;
            case "logs-dir":
                value = pathConfigProperties.getLogsDir();
                break;
            case "temp-dir":
                value = pathConfigProperties.getTempDir();
                break;
            case "monitoring-dir":
                value = pathConfigProperties.getMonitoringDir();
                break;
            case "reports-dir":
                value = pathConfigProperties.getReportsDir();
                break;
            default:
                LoggingUtil.warn(logger, "未知的路径配置键: {}", key);
                return getDefaultPathValue(configKey);
        }

        // 如果配置值为空，返回默认值
        if (value == null || value.trim().isEmpty()) {
            LoggingUtil.warn(logger, "路径配置值为空，使用默认值，配置键: {}", configKey);
            return getDefaultPathValue(configKey);
        }

        return value;
    }

    /**
     * 获取默认路径值
     */
    private String getDefaultPathValue(String configKey) {
        if (configKey == null) {
            return null;
        }

        String key = configKey.startsWith("honyrun.path.") ? 
            configKey.substring("honyrun.path.".length()) : configKey;

        switch (key) {
            case "src-base":
                return "src";
            case "logs-dir":
                return "logs";
            case "temp-dir":
                return "target/temp";
            case "monitoring-dir":
                return "target/monitoring";
            case "reports-dir":
                return "target/reports";
            default:
                LoggingUtil.warn(logger, "无法提供默认值，未知配置键: {}", configKey);
                return null;
        }
    }

    // ==================== 内部类定义 ====================

    /**
     * 配置缓存条目
     */
    private static class ConfigCacheEntry {
        private final Object value;
        private final LocalDateTime timestamp;
        private static final long CACHE_EXPIRE_MINUTES = 30;

        public ConfigCacheEntry(Object value, LocalDateTime timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public Object getValue() {
            return value;
        }

        public boolean isExpired() {
            return timestamp.plusMinutes(CACHE_EXPIRE_MINUTES).isBefore(LocalDateTime.now());
        }
    }

    /**
     * 配置管理器状态枚举
     */
    public enum ConfigManagerStatus {
        INITIALIZING,
        READY,
        ERROR
    }
}