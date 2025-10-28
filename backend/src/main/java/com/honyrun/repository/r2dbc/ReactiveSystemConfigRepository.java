package com.honyrun.repository.r2dbc;

import com.honyrun.model.entity.system.SystemConfig;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式系统配置仓库接口
 *
 * 基于R2DBC的响应式系统配置数据访问层，提供非阻塞的系统配置相关数据库操作
 * 支持系统配置实体的响应式数据访问，所有方法返回Mono或Flux类型
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 04:40:00
 * @modified 2025-07-01 04:40:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Repository
public interface ReactiveSystemConfigRepository extends ReactiveCrudRepository<SystemConfig, Long> {

    /**
     * 根据配置键名查找系统配置
     *
     * @param configKey 配置键名
     * @return 系统配置的Mono包装
     */
    Mono<SystemConfig> findByConfigKey(String configKey);

    /**
     * 根据配置分组查找系统配置列表
     *
     * @param configGroup 配置分组
     * @return 系统配置列表的Flux包装
     */
    Flux<SystemConfig> findByConfigGroup(String configGroup);

    /**
     * 根据状态查找系统配置列表
     *
     * @param status 状态
     * @return 系统配置列表的Flux包装
     */
    Flux<SystemConfig> findByStatus(String status);

    /**
     * 根据启用状态查找系统配置列表
     *
     * @param enabled 启用状态
     * @return 系统配置列表的Flux包装
     */
    Flux<SystemConfig> findByEnabled(Integer enabled);

    /**
     * 检查配置键是否存在
     * 修复Boolean到Integer转换错误：使用CASE WHEN语句确保返回明确的布尔值
     *
     * @param configKey 配置键
     * @return 是否存在
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM sys_system_configs WHERE config_key = :configKey")
    Mono<Boolean> existsByConfigKey(String configKey);

    /**
     * 查找所有启用的系统配置
     *
     * @return 启用的系统配置列表的Flux包装
     */
    @Query("SELECT * FROM sys_system_configs WHERE enabled = 1 AND deleted = 0 ORDER BY config_group, config_key")
    Flux<SystemConfig> findAllEnabled();

    /**
     * 根据分组和状态查找配置
     *
     * @param configGroup 配置分组
     * @param status 状态
     * @return 系统配置列表的Flux包装
     */
    @Query("SELECT * FROM sys_system_configs WHERE config_group = :configGroup AND status = :status AND deleted = 0 ORDER BY config_key")
    Flux<SystemConfig> findByConfigGroupAndStatus(String configGroup, String status);
}
