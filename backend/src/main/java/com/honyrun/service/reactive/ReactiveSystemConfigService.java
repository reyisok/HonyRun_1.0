package com.honyrun.service.reactive;

import reactor.core.publisher.Mono;

/**
 * 响应式系统配置服务接口
 * 提供系统配置的读取和管理功能
 *
 * @author Mr.Rey
 * @since 2025-07-01 11:30:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveSystemConfigService {

    /**
     * 获取配置值
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Mono<String> getConfigValue(String configKey, String defaultValue);

    /**
     * 设置配置值
     *
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 设置结果
     */
    Mono<Void> setConfigValue(String configKey, String configValue);

    /**
     * 检查配置是否存在
     *
     * @param configKey 配置键
     * @return 如果存在返回true，否则返回false
     */
    Mono<Boolean> hasConfig(String configKey);

    /**
     * 删除配置
     *
     * @param configKey 配置键
     * @return 删除结果
     */
    Mono<Void> deleteConfig(String configKey);
}

