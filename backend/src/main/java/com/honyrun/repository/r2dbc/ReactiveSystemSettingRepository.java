package com.honyrun.repository.r2dbc;

import com.honyrun.model.entity.system.SystemSetting;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式系统设置仓库接口
 *
 * 基于R2DBC的响应式系统设置数据访问层，提供非阻塞的系统设置相关数据库操作
 * 支持系统设置实体的响应式数据访问，所有方法返回Mono或Flux类型
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 04:40:00
 * @modified 2025-07-01 04:40:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Repository
public interface ReactiveSystemSettingRepository extends ReactiveCrudRepository<SystemSetting, Long> {

    /**
     * 根据配置键名查找系统设置
     *
     * @param settingKey 配置键名
     * @return 系统设置的Mono包装
     */
    Mono<SystemSetting> findBySettingKey(String settingKey);

    /**
     * 根据配置分类查找系统设置列表
     *
     * @param category 配置分类
     * @return 系统设置列表的Flux包装
     */
    Flux<SystemSetting> findByCategory(String category);

    /**
     * 根据启用状态查找系统设置列表
     *
     * @param enabled 启用状态
     * @return 系统设置列表的Flux包装
     */
    Flux<SystemSetting> findByEnabled(Integer enabled);

    /**
     * 根据配置类型查找系统设置列表
     *
     * @param configType 配置类型
     * @return 系统设置列表的Flux包装
     */
    Flux<SystemSetting> findByConfigType(String configType);

    /**
     * 检查设置键是否存在
     * 修复Boolean到Integer转换错误：使用CASE WHEN语句确保返回明确的布尔值
     *
     * @param settingKey 设置键
     * @return 是否存在
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM system_setting WHERE setting_key = :settingKey")
    Mono<Boolean> existsBySettingKey(String settingKey);

    /**
     * 根据分类和启用状态查找系统设置
     *
     * @param category 配置分类
     * @param enabled 启用状态
     * @return 系统设置列表的Flux包装
     */
    Flux<SystemSetting> findByCategoryAndEnabled(String category, Integer enabled);

    /**
     * 查找所有启用的系统设置
     *
     * @return 启用的系统设置列表的Flux包装
     */
    @Query("SELECT * FROM sys_system_settings WHERE enabled = 1 AND deleted = 0 ORDER BY category, setting_key")
    Flux<SystemSetting> findAllEnabled();

    /**
     * 根据配置键名模糊查找系统设置
     *
     * @param keyPattern 配置键名模式
     * @return 系统设置列表的Flux包装
     */
    @Query("SELECT * FROM sys_system_settings WHERE setting_key LIKE CONCAT('%', :keyPattern, '%') AND deleted = 0 ORDER BY setting_key")
    Flux<SystemSetting> findBySettingKeyContaining(@Param("keyPattern") String keyPattern);

    /**
     * 统计指定分类的配置项数量
     *
     * @param category 配置分类
     * @return 配置项数量的Mono包装
     */
    Mono<Long> countByCategory(String category);

    /**
     * 批量更新配置启用状态
     *
     * @param settingKeys 配置键名列表
     * @param enabled 启用状态
     * @return 更新行数的Mono包装
     */
    @Query("UPDATE sys_system_settings SET enabled = :enabled, last_modified_date = CURRENT_TIMESTAMP WHERE setting_key IN (:settingKeys) AND deleted = 0")
    Mono<Integer> updateEnabledBySettingKeys(@Param("settingKeys") Iterable<String> settingKeys,
                                            @Param("enabled") Integer enabled);
}
