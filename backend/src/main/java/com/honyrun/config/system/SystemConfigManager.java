package com.honyrun.config.system;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.honyrun.model.entity.system.SystemConfig;
import com.honyrun.repository.r2dbc.ReactiveSystemConfigRepository;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 系统级配置管理类
 *
 * 提供系统配置的统一管理、缓存和访问功能，支持响应式数据访问
 * 负责系统配置的加载、缓存、更新和刷新机制
 *
 * 主要功能：
 * - 系统配置的响应式加载和缓存
 * - 配置项的类型安全访问
 * - Spring Cache统一缓存管理
 * - 配置有效性验证
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 11:00:00
 * @modified 2025-07-01 11:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class SystemConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigManager.class);

    private final ReactiveSystemConfigRepository systemConfigRepository;
    private final CacheManager cacheManager;

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param systemConfigRepository 系统配置仓库
     * @param cacheManager 缓存管理器
     */
    public SystemConfigManager(ReactiveSystemConfigRepository systemConfigRepository,
                              CacheManager cacheManager) {
        this.systemConfigRepository = systemConfigRepository;
        this.cacheManager = cacheManager;
    }

    // ==================== 配置获取方法 ====================

    /**
     * 获取字符串类型配置值
     *
     * @param configKey 配置键名
     * @return 配置值的Mono包装
     */
    @Cacheable(value = "systemCache", key = "#configKey", unless = "#result == null")
    public Mono<String> getStringConfig(String configKey) {
        LoggingUtil.debug(logger, "获取字符串配置: {}", configKey);

        return systemConfigRepository.findByConfigKey(configKey)
                .filter(config -> config.isEnabled())
                .map(SystemConfig::getConfigValue)
                .doOnNext(value -> LoggingUtil.debug(logger, "获取到字符串配置 {}: {}", configKey, value))
                .doOnError(error -> LoggingUtil.error(logger, "获取字符串配置失败: " + configKey, error));
    }

    /**
     * 获取字符串类型配置值，带默认值
     *
     * @param configKey 配置键名
     * @param defaultValue 默认值
     * @return 配置值的Mono包装
     */
    public Mono<String> getStringConfig(String configKey, String defaultValue) {
        return getStringConfig(configKey)
                .switchIfEmpty(Mono.just(defaultValue));
    }

    /**
     * 获取整数类型配置值
     *
     * @param configKey 配置键名
     * @return 配置值的Mono包装
     */
    @Cacheable(value = "systemCache", key = "#configKey", unless = "#result == null")
    public Mono<Integer> getIntegerConfig(String configKey) {
        LoggingUtil.debug(logger, "获取整数配置: {}", configKey);

        return systemConfigRepository.findByConfigKey(configKey)
                .filter(config -> config.isEnabled())
                .map(SystemConfig::getConfigValue)
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        LoggingUtil.warn(logger, "配置值不是有效整数: {} = {}", configKey, value);
                        throw new IllegalArgumentException("配置值不是有效整数: " + configKey, e);
                    }
                })
                .doOnNext(value -> LoggingUtil.debug(logger, "获取到整数配置 {}: {}", configKey, value))
                .doOnError(error -> LoggingUtil.error(logger, "获取整数配置失败: " + configKey, error));
    }

    /**
     * 获取整数类型配置值，带默认值
     *
     * @param configKey 配置键名
     * @param defaultValue 默认值
     * @return 配置值的Mono包装
     */
    public Mono<Integer> getIntegerConfig(String configKey, Integer defaultValue) {
        return getIntegerConfig(configKey)
                .switchIfEmpty(Mono.just(defaultValue));
    }

    /**
     * 获取布尔类型配置值
     *
     * @param configKey 配置键名
     * @return 配置值的Mono包装
     */
    @Cacheable(value = "systemCache", key = "#configKey", unless = "#result == null")
    public Mono<Boolean> getBooleanConfig(String configKey) {
        LoggingUtil.debug(logger, "获取布尔配置: {}", configKey);

        return systemConfigRepository.findByConfigKey(configKey)
                .filter(config -> config.isEnabled())
                .map(SystemConfig::getConfigValue)
                .map(value -> "true".equalsIgnoreCase(value) || "1".equals(value))
                .doOnNext(value -> LoggingUtil.debug(logger, "获取到布尔配置 {}: {}", configKey, value))
                .doOnError(error -> LoggingUtil.error(logger, "获取布尔配置失败: " + configKey, error));
    }

    /**
     * 获取布尔类型配置值，带默认值
     *
     * @param configKey 配置键名
     * @param defaultValue 默认值
     * @return 配置值的Mono包装
     */
    public Mono<Boolean> getBooleanConfig(String configKey, Boolean defaultValue) {
        return getBooleanConfig(configKey)
                .switchIfEmpty(Mono.just(defaultValue));
    }

    // ==================== 配置设置方法 ====================

    /**
     * 设置系统配置
     *
     * @param configKey 配置键名
     * @param configValue 配置值
     * @param configGroup 配置分组
     * @param configType 配置类型
     * @return 设置结果的Mono包装
     */
    @CachePut(value = "systemCache", key = "#configKey")
    public Mono<SystemConfig> setSystemConfig(String configKey, String configValue,
                                            String configGroup, String configType) {
        LoggingUtil.info(logger, "设置系统配置: {} = {}", configKey, configValue);

        return systemConfigRepository.findByConfigKey(configKey)
                .switchIfEmpty(Mono.defer(() -> {
                    SystemConfig newConfig = new SystemConfig();
                    newConfig.setConfigKey(configKey);
                    newConfig.setConfigGroup(configGroup);
                    newConfig.setConfigType(configType);
                    return Mono.just(newConfig);
                }))
                .flatMap(config -> {
                    config.setConfigValue(configValue);
                    config.setEnabled(1);
                    return systemConfigRepository.save(config);
                })
                .doOnNext(savedConfig -> LoggingUtil.info(logger, "系统配置设置成功: {}", configKey))
                .doOnError(error -> LoggingUtil.error(logger, "设置系统配置失败: " + configKey, error));
    }

    // ==================== 配置缓存管理 ====================

    /**
     * 刷新配置缓存
     *
     * @return 刷新结果的Mono包装
     */
    @CacheEvict(value = "systemCache", allEntries = true)
    public Mono<Void> refreshConfigCache() {
        LoggingUtil.info(logger, "开始刷新配置缓存");

        return systemConfigRepository.findAllEnabled()
                .collectList()
                .doOnNext(configList -> LoggingUtil.info(logger, "配置缓存刷新完成，共加载 {} 个配置项", configList.size()))
                .then()
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "刷新配置缓存失败，可能是表未就绪或查询异常，将忽略并稍后重试。", error);
                    return Mono.empty();
                });
    }

    /**
     * 清空配置缓存
     */
    @CacheEvict(value = "systemCache", allEntries = true)
    public void clearConfigCache() {
        LoggingUtil.info(logger, "清空配置缓存");
    }

    // ==================== 配置查询方法 ====================

    /**
     * 获取所有启用的系统配置
     *
     * @return 系统配置列表的Flux包装
     */
    @Cacheable(value = "systemConfigs", key = "'all_enabled'")
    public Flux<SystemConfig> getAllEnabledConfigs() {
        LoggingUtil.debug(logger, "获取所有启用的系统配置");

        return systemConfigRepository.findAllEnabled()
                .doOnNext(config -> LoggingUtil.debug(logger, "加载配置: {} = {}",
                        config.getConfigKey(), config.getConfigValue()))
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "获取系统配置失败，返回空列表", error);
                    return Flux.empty();
                });
    }

    /**
     * 刷新配置分组缓存
     *
     * @param configGroup 配置分组
     * @return 刷新结果的Mono包装
     */
    @CacheEvict(value = "systemCache", key = "#configGroup + '*'")
    public Mono<Boolean> refreshConfigGroup(String configGroup) {
        LoggingUtil.info(logger, "刷新配置分组缓存: {}", configGroup);

        return getConfigsByGroup(configGroup)
                .collectList()
                .timeout(Duration.ofSeconds(10))
                .doOnNext(configList -> LoggingUtil.info(logger, "配置分组 {} 缓存刷新完成，共更新 {} 个配置项",
                        configGroup, configList.size()))
                .map(configList -> true)
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "刷新配置分组缓存失败: " + configGroup, error);
                    return Mono.just(false);
                });
    }

    /**
     * 根据配置分组获取配置
     *
     * @param configGroup 配置分组
     * @return 系统配置列表的Flux包装
     */
    public Flux<SystemConfig> getConfigsByGroup(String configGroup) {
        LoggingUtil.debug(logger, "获取配置分组 {} 的配置", configGroup);

        return systemConfigRepository.findByConfigGroup(configGroup)
                .filter(SystemConfig::isEnabled)
                .doOnNext(config -> LoggingUtil.debug(logger, "加载分组配置: {} = {}",
                        config.getConfigKey(), config.getConfigValue()))
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "获取分组配置失败: {}，返回空列表", configGroup, error);
                    return Flux.empty();
                });
    }

    /**
     * 检查配置是否存在
     *
     * @param configKey 配置键名
     * @return 是否存在的Mono包装
     */
    public Mono<Boolean> configExists(String configKey) {
        return systemConfigRepository.existsByConfigKey(configKey)
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "检查配置键存在失败或表未就绪，返回false: {}", configKey, error);
                    return Mono.just(false);
                });
    }

    // ==================== 配置状态管理 ====================

    /**
     * 获取缓存状态信息
     *
     * @return 缓存状态信息
     */
    public Map<String, Object> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("cacheType", "Spring Cache");
        status.put("cacheProvider", "统一缓存管理器");
        status.put("lastUpdate", LocalDateTime.now());

        // 添加缓存管理器信息
        if (cacheManager != null) {
            status.put("cacheManagerClass", cacheManager.getClass().getSimpleName());
            status.put("cacheNames", cacheManager.getCacheNames());
        }

        return status;
    }

    /**
     * 预热配置缓存
     *
     * @return 预热结果的Mono包装
     */
    public Mono<Void> warmupCache() {
        LoggingUtil.info(logger, "开始预热配置缓存");
        return refreshConfigCache();
    }
}
