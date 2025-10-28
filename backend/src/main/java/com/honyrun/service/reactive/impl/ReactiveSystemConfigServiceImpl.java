package com.honyrun.service.reactive.impl;

import com.honyrun.repository.r2dbc.ReactiveSystemConfigRepository;
import com.honyrun.service.reactive.ReactiveSystemConfigService;
import com.honyrun.service.reactive.ReactiveOptimisticLockingService;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 响应式系统配置服务实现类
 * 提供系统配置的读取和管理功能的具体实现
 *
 * @author Mr.Rey
 * @since 2025-07-01 11:30:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
public class ReactiveSystemConfigServiceImpl implements ReactiveSystemConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveSystemConfigServiceImpl.class);

    private final ReactiveSystemConfigRepository systemConfigRepository;
    private final ReactiveOptimisticLockingService optimisticLockingService;

    /**
     * 构造函数注入
     *
     * @param systemConfigRepository 系统配置仓库
     * @param optimisticLockingService 乐观锁服务
     */
    public ReactiveSystemConfigServiceImpl(ReactiveSystemConfigRepository systemConfigRepository,
                                         ReactiveOptimisticLockingService optimisticLockingService) {
        this.systemConfigRepository = systemConfigRepository;
        this.optimisticLockingService = optimisticLockingService;
    }

    @Override
    public Mono<String> getConfigValue(String configKey, String defaultValue) {
        LoggingUtil.debug(logger, "获取系统配置值，键: {}", configKey);
        
        return systemConfigRepository.findByConfigKey(configKey)
                .filter(config -> (config.getDeleted() == null || config.getDeleted() == 0)
                        && (config.getEnabled() == null || config.getEnabled() == 1))
                .map(com.honyrun.model.entity.system.SystemConfig::getConfigValue)
                .switchIfEmpty(Mono.just(defaultValue))
                .onErrorReturn(defaultValue);
    }

    @Override
    public Mono<Void> setConfigValue(String configKey, String configValue) {
        LoggingUtil.info(logger, "设置系统配置值，键: {}, 值: {}", configKey, configValue);

        // 使用统一的乐观锁服务处理并发更新
        return optimisticLockingService.executeWithOptimisticLockRetry(
                systemConfigRepository.findByConfigKey(configKey)
                        .flatMap(config -> {
                            // 忽略已删除的配置（当作不存在）
                            if (config.getDeleted() != null && config.getDeleted() == 1) {
                                return Mono.empty();
                            }
                            config.setConfigValue(configValue);
                            // 更新为启用状态
                            config.setEnabled(1);
                            config.setLastModifiedDate(java.time.LocalDateTime.now());
                            return systemConfigRepository.save(config);
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            // 不存在则创建新配置
                            com.honyrun.model.entity.system.SystemConfig newConfig = new com.honyrun.model.entity.system.SystemConfig();
                            newConfig.setConfigKey(configKey);
                            newConfig.setConfigValue(configValue);
                            newConfig.setConfigGroup("DEFAULT");
                            newConfig.setConfigType("STRING");
                            newConfig.setDescription("自动创建的配置项: " + configKey);
                            newConfig.setEnabled(1);
                            newConfig.setStatus("ACTIVE");
                            newConfig.setDeleted(0);
                            newConfig.setCreatedDate(java.time.LocalDateTime.now());
                            newConfig.setLastModifiedDate(java.time.LocalDateTime.now());
                            newConfig.setVersion(null);
                            return systemConfigRepository.save(newConfig);
                        }))
                        .then(),
                "updateSystemConfig"
        ).onErrorResume(error -> {
            LoggingUtil.error(logger, "设置系统配置值失败，键: {}", configKey, error);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Boolean> hasConfig(String configKey) {
        LoggingUtil.debug(logger, "检查系统配置是否存在，键: {}", configKey);
        
        return systemConfigRepository.findByConfigKey(configKey)
                .filter(config -> (config.getDeleted() == null || config.getDeleted() == 0)
                        && (config.getEnabled() == null || config.getEnabled() == 1))
                .hasElement()
                .onErrorReturn(false);
    }

    @Override
    public Mono<Void> deleteConfig(String configKey) {
        LoggingUtil.info(logger, "删除系统配置，键: {}", configKey);
        
        // 使用统一的乐观锁服务处理软删除操作
        return optimisticLockingService.executeWithOptimisticLockRetry(
                systemConfigRepository.findByConfigKey(configKey)
                        .flatMap(config -> {
                            // 软删除：标记deleted=1
                            config.setDeleted(1);
                            config.setLastModifiedDate(java.time.LocalDateTime.now());
                            return systemConfigRepository.save(config);
                        })
                        .then(),
                "deleteSystemConfig"
        ).onErrorResume(error -> {
            LoggingUtil.error(logger, "删除系统配置失败，键: {}", configKey, error);
            return Mono.empty();
        });
    }
}

