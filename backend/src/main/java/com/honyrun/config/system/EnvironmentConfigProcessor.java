package com.honyrun.config.system;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.honyrun.model.entity.system.SystemConfig;
import com.honyrun.repository.r2dbc.ReactiveSystemConfigRepository;
import com.honyrun.util.LoggingUtil;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 环境特定配置处理类
 *
 * 负责处理不同环境（开发、测试、生产）下的配置加载、验证和管理
 * 支持环境特定的配置覆盖和环境变量集成
 *
 * 主要功能：
 * - 环境特定配置的加载和管理
 * - 配置优先级处理（环境变量 > 数据库配置 > 默认配置）
 * - 环境配置的验证和校验
 * - 配置环境切换支持
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 11:15:00
 * @modified 2025-07-01 11:15:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class EnvironmentConfigProcessor {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfigProcessor.class);

    private final Environment environment;
    private final ReactiveSystemConfigRepository systemConfigRepository;

    /**
     * 环境配置缓存
     */
    private final Map<String, Object> environmentConfigCache = new ConcurrentHashMap<>();

    /**
     * 支持的环境列表
     */
    private static final String[] SUPPORTED_ENVIRONMENTS = { "dev", "test", "prod" };

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param environment            Spring环境对象
     * @param systemConfigRepository 系统配置仓库
     */
    public EnvironmentConfigProcessor(Environment environment,
            ReactiveSystemConfigRepository systemConfigRepository) {
        this.environment = environment;
        this.systemConfigRepository = systemConfigRepository;
    }

    @PostConstruct
    public void init() {
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        LoggingUtil.info(logger, "初始化环境配置处理器，当前环境: {}", activeProfile);
        validateEnvironment();
    }

    // ==================== 环境配置获取方法 ====================

    /**
     * 获取环境特定的配置值
     *
     * @param configKey 配置键名
     * @return 配置值的Mono包装
     */
    public Mono<String> getEnvironmentConfig(String configKey) {
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        LoggingUtil.debug(logger, "获取环境配置: {} (环境: {})", configKey, activeProfile);

        return getConfigWithPriority(configKey)
                .doOnNext(value -> LoggingUtil.debug(logger, "获取到环境配置 {}: {}", configKey, value))
                .doOnError(error -> LoggingUtil.error(logger, "获取环境配置失败: " + configKey, error));
    }

    /**
     * 获取环境特定的配置值，带默认值
     *
     * @param configKey    配置键名
     * @param defaultValue 默认值
     * @return 配置值的Mono包装
     */
    public Mono<String> getEnvironmentConfig(String configKey, String defaultValue) {
        return getEnvironmentConfig(configKey)
                .switchIfEmpty(Mono.just(defaultValue));
    }

    /**
     * 按优先级获取配置值
     * 优先级：环境变量 > 数据库环境配置 > 数据库通用配置 > Spring配置
     *
     * @param configKey 配置键名
     * @return 配置值的Mono包装
     */
    private Mono<String> getConfigWithPriority(String configKey) {
        // 1. 首先检查环境变量
        String envValue = System.getenv(configKey.toUpperCase().replace(".", "_"));
        if (envValue != null) {
            LoggingUtil.debug(logger, "从环境变量获取配置 {}: {}", configKey, envValue);
            return Mono.just(envValue);
        }

        // 2. 检查Spring Environment（包含application-{profile}.properties）
        String springValue = environment.getProperty(configKey);
        if (springValue != null) {
            LoggingUtil.debug(logger, "从Spring环境获取配置 {}: {}", configKey, springValue);
            return Mono.just(springValue);
        }

        // 3. 检查数据库中的环境特定配置
        return getEnvironmentSpecificConfigFromDb(configKey)
                .switchIfEmpty(getGeneralConfigFromDb(configKey));
    }

    /**
     * 从数据库获取环境特定配置
     *
     * @param configKey 配置键名
     * @return 配置值的Mono包装
     */
    private Mono<String> getEnvironmentSpecificConfigFromDb(String configKey) {
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        return systemConfigRepository.findByConfigGroupAndStatus("ENVIRONMENT", "ACTIVE")
                .subscribeOn(Schedulers.boundedElastic()) // 在弹性调度器上执行数据库查询，避免阻塞
                .filter(config -> configKey.equals(config.getConfigKey()) && config.isEnabled())
                .map(SystemConfig::getConfigValue)
                .next()
                .doOnNext(value -> LoggingUtil.debug(logger, "从数据库获取环境特定配置 {} ({}): {}",
                        configKey, activeProfile, value))
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "读取环境特定配置失败或表未就绪，返回空值: {} ({} )", configKey, activeProfile, error);
                    return Mono.empty();
                });
    }

    /**
     * 从数据库获取通用配置
     *
     * @param configKey 配置键名
     * @return 配置值的Mono包装
     */
    private Mono<String> getGeneralConfigFromDb(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey)
                .subscribeOn(Schedulers.boundedElastic()) // 在弹性调度器上执行数据库查询，避免阻塞
                .filter(SystemConfig::isEnabled)
                .map(SystemConfig::getConfigValue)
                .doOnNext(value -> LoggingUtil.debug(logger, "从数据库获取通用配置 {}: {}", configKey, value))
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "读取通用配置失败或表未就绪，返回空值: {}", configKey, error);
                    return Mono.empty();
                });
    }

    // ==================== 环境配置管理方法 ====================

    /**
     * 设置环境特定配置
     *
     * @param configKey   配置键名
     * @param configValue 配置值
     * @param environment 目标环境
     * @return 设置结果的Mono包装
     */
    public Mono<SystemConfig> setEnvironmentConfig(String configKey, String configValue, String environment) {
        LoggingUtil.info(logger, "设置环境配置: {} = {} (环境: {})", configKey, configValue, environment);

        if (!isValidEnvironment(environment)) {
            return Mono.error(new IllegalArgumentException("不支持的环境: " + environment));
        }

        return systemConfigRepository.findByConfigGroupAndStatus("ENVIRONMENT", "ACTIVE")
                .filter(config -> configKey.equals(config.getConfigKey()))
                .next()
                .switchIfEmpty(Mono.defer(() -> {
                    SystemConfig newConfig = new SystemConfig();
                    newConfig.setConfigKey(configKey);
                    newConfig.setConfigGroup("ENVIRONMENT");
                    newConfig.setConfigType("STRING");
                    return Mono.just(newConfig);
                }))
                .flatMap(config -> {
                    config.setConfigValue(configValue);
                    config.setEnabled(1);
                    config.setStatus("ACTIVE");
                    config.setDescription("环境特定配置 - " + environment);
                    return systemConfigRepository.save(config);
                })
                .doOnNext(savedConfig -> {
                    // 清除相关缓存
                    clearEnvironmentCache(configKey);
                    LoggingUtil.info(logger, "环境配置设置成功: {} (环境: {})", configKey, environment);
                })
                .doOnError(error -> LoggingUtil.error(logger, "设置环境配置失败: " + configKey, error));
    }

    /**
     * 获取当前环境的所有配置
     *
     * @return 当前环境配置列表的Flux包装
     */
    public Flux<SystemConfig> getCurrentEnvironmentConfigs() {
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        LoggingUtil.debug(logger, "获取当前环境 {} 的所有配置", activeProfile);

        return systemConfigRepository.findByConfigGroupAndStatus("ENVIRONMENT", "ACTIVE")
                .filter(SystemConfig::isEnabled)
                .doOnNext(config -> LoggingUtil.debug(logger, "加载环境配置: {} = {}",
                        config.getConfigKey(), config.getConfigValue()))
                .doOnError(error -> LoggingUtil.error(logger, "获取环境配置失败", error));
    }

    /**
     * 获取指定环境的所有配置
     *
     * @param environment 环境名称
     * @return 环境配置列表的Flux包装
     */
    public Flux<SystemConfig> getEnvironmentConfigs(String environment) {
        LoggingUtil.debug(logger, "获取环境 {} 的所有配置", environment);

        if (!isValidEnvironment(environment)) {
            return Flux.error(new IllegalArgumentException("不支持的环境: " + environment));
        }

        return systemConfigRepository.findByConfigGroupAndStatus("ENVIRONMENT", "ACTIVE")
                .filter(SystemConfig::isEnabled)
                .doOnNext(config -> LoggingUtil.debug(logger, "加载环境配置: {} = {}",
                        config.getConfigKey(), config.getConfigValue()))
                .doOnError(error -> LoggingUtil.error(logger, "获取环境配置失败: " + environment, error));
    }

    // ==================== 环境验证和工具方法 ====================

    /**
     * 验证当前环境配置
     */
    private void validateEnvironment() {
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        if (!isValidEnvironment(activeProfile)) {
            LoggingUtil.warn(logger, "当前环境 {} 不在支持列表中: {}",
                    activeProfile, Arrays.toString(SUPPORTED_ENVIRONMENTS));
        } else {
            LoggingUtil.info(logger, "环境验证通过: {}", activeProfile);
        }
    }

    /**
     * 检查环境是否有效
     *
     * @param environment 环境名称
     * @return 是否有效
     */
    private boolean isValidEnvironment(String environment) {
        return Arrays.asList(SUPPORTED_ENVIRONMENTS).contains(environment.toLowerCase());
    }

    /**
     * 获取当前活动环境
     *
     * @return 当前环境名称
     */
    public String getActiveProfile() {
        return environment.getProperty("spring.profiles.active", "dev");
    }

    /**
     * 获取支持的环境列表
     *
     * @return 支持的环境数组
     */
    public String[] getSupportedEnvironments() {
        return SUPPORTED_ENVIRONMENTS.clone();
    }

    /**
     * 检查是否为开发环境
     *
     * @return 是否为开发环境
     */
    public boolean isDevelopmentEnvironment() {
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        return "dev".equalsIgnoreCase(activeProfile);
    }

    /**
     * 检查是否为生产环境
     *
     * @return 是否为生产环境
     */
    public boolean isProductionEnvironment() {
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        return "prod".equalsIgnoreCase(activeProfile);
    }

    /**
     * 检查是否为测试环境
     *
     * @return 是否为测试环境
     */
    public boolean isTestEnvironment() {
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        return "test".equalsIgnoreCase(activeProfile);
    }

    // ==================== 缓存管理方法 ====================

    /**
     * 清除环境配置缓存
     *
     * @param configKey 配置键名，如果为null则清除所有缓存
     */
    public void clearEnvironmentCache(String configKey) {
        if (configKey != null) {
            environmentConfigCache.remove(configKey);
            LoggingUtil.debug(logger, "清除环境配置缓存: {}", configKey);
        } else {
            environmentConfigCache.clear();
            LoggingUtil.info(logger, "清除所有环境配置缓存");
        }
    }

    /**
     * 刷新环境配置缓存
     *
     * @return 刷新结果的Mono包装
     */
    public Mono<Void> refreshEnvironmentCache() {
        LoggingUtil.info(logger, "刷新环境配置缓存");

        return getCurrentEnvironmentConfigs()
                .collectMap(SystemConfig::getConfigKey, SystemConfig::getConfigValue)
                .doOnNext(configMap -> {
                    environmentConfigCache.clear();
                    environmentConfigCache.putAll(configMap);
                    LoggingUtil.info(logger, "环境配置缓存刷新完成，共加载 {} 个配置项", configMap.size());
                })
                .then()
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "刷新环境配置缓存失败，可能是表未就绪或查询异常，将忽略并稍后重试。", error);
                    return Mono.empty();
                });
    }

    /**
     * 获取环境配置缓存状态
     *
     * @return 缓存状态信息
     */
    public Map<String, Object> getEnvironmentCacheStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        status.put("activeProfile", activeProfile);
        status.put("cacheSize", environmentConfigCache.size());
        status.put("supportedEnvironments", SUPPORTED_ENVIRONMENTS);
        status.put("cachedKeys", environmentConfigCache.keySet());
        return status;
    }
}