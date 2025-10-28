package com.honyrun.service.reactive;

import java.util.List;
import java.util.Map;

import com.honyrun.model.dto.request.ConfigUpdateRequest;
import com.honyrun.model.dto.response.ConfigUpdateResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式配置热更新服务接口
 *
 * 基于WebFlux的响应式配置热更新服务，提供非阻塞的配置动态更新功能
 * 支持单个配置更新、批量配置更新、配置验证、配置备份等功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 12:20:00
 * @modified 2025-07-01 12:20:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveConfigHotUpdateService {

    // ==================== 配置更新功能 ====================

    /**
     * 单个配置热更新
     *
     * @param request 配置更新请求
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 配置更新响应的Mono包装
     */
    Mono<ConfigUpdateResponse> updateSingleConfig(ConfigUpdateRequest request, Long operatorId, String operatorName);

    /**
     * 批量配置热更新
     *
     * @param request 批量配置更新请求
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 配置更新响应的Mono包装
     */
    Mono<ConfigUpdateResponse> updateBatchConfigs(ConfigUpdateRequest request, Long operatorId, String operatorName);

    /**
     * 异步配置热更新
     *
     * @param request 配置更新请求
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 配置更新响应的Mono包装
     */
    Mono<ConfigUpdateResponse> asyncUpdateConfigs(ConfigUpdateRequest request, Long operatorId, String operatorName);

    /**
     * 合并配置更新
     *
     * @param configMap 配置映射
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 配置更新响应的Mono包装
     */
    Mono<ConfigUpdateResponse> mergeConfigs(Map<String, String> configMap, Long operatorId, String operatorName);

    /**
     * 替换配置更新
     *
     * @param configGroup 配置分组
     * @param configMap 配置映射
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 配置更新响应的Mono包装
     */
    Mono<ConfigUpdateResponse> replaceConfigs(String configGroup, Map<String, String> configMap, Long operatorId, String operatorName);

    // ==================== 配置验证功能 ====================

    /**
     * 验证配置更新请求
     *
     * @param request 配置更新请求
     * @return 验证结果的Mono包装
     */
    Mono<Map<String, Object>> validateConfigUpdate(ConfigUpdateRequest request);

    /**
     * 验证单个配置值
     *
     * @param configKey 配置键名
     * @param configValue 配置值
     * @param configType 配置类型
     * @return 验证结果的Mono包装
     */
    Mono<Map<String, Object>> validateConfigValue(String configKey, String configValue, String configType);

    /**
     * 验证配置依赖关系
     *
     * @param configKey 配置键名
     * @param configValue 配置值
     * @return 验证结果的Mono包装
     */
    Mono<Map<String, Object>> validateConfigDependencies(String configKey, String configValue);

    /**
     * 预览配置更新效果
     *
     * @param request 配置更新请求
     * @return 预览结果的Mono包装
     */
    Mono<Map<String, Object>> previewConfigUpdate(ConfigUpdateRequest request);

    // ==================== 配置备份功能 ====================

    /**
     * 备份当前配置
     *
     * @param configKeys 配置键名列表
     * @param backupReason 备份原因
     * @param operatorId 操作用户ID
     * @return 备份结果的Mono包装
     */
    Mono<Map<String, Object>> backupConfigs(List<String> configKeys, String backupReason, Long operatorId);

    /**
     * 备份配置分组
     *
     * @param configGroup 配置分组
     * @param backupReason 备份原因
     * @param operatorId 操作用户ID
     * @return 备份结果的Mono包装
     */
    Mono<Map<String, Object>> backupConfigGroup(String configGroup, String backupReason, Long operatorId);

    /**
     * 备份所有配置
     *
     * @param backupReason 备份原因
     * @param operatorId 操作用户ID
     * @return 备份结果的Mono包装
     */
    Mono<Map<String, Object>> backupAllConfigs(String backupReason, Long operatorId);

    /**
     * 恢复配置备份
     *
     * @param backupId 备份ID
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 恢复结果的Mono包装
     */
    Mono<ConfigUpdateResponse> restoreConfigBackup(String backupId, Long operatorId, String operatorName);

    /**
     * 获取配置备份列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 备份列表的Flux包装
     */
    Flux<Map<String, Object>> getConfigBackupList(int page, int size);

    /**
     * 删除配置备份
     *
     * @param backupId 备份ID
     * @param operatorId 操作用户ID
     * @return 删除结果的Mono包装
     */
    Mono<Boolean> deleteConfigBackup(String backupId, Long operatorId);

    /**
     * 批量回滚配置
     *
     * @param configKeys 配置键名列表
     * @param version 回滚版本
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 配置更新响应的Mono包装
     */
    Mono<ConfigUpdateResponse> rollbackBatchConfigs(List<String> configKeys, String version, Long operatorId, String operatorName);

    /**
     * 创建配置快照
     *
     * @param configKeys 配置键名列表
     * @param snapshotName 快照名称
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 快照创建结果的Mono包装
     */
    Mono<Map<String, Object>> createConfigSnapshot(List<String> configKeys, String snapshotName, Long operatorId, String operatorName);

    /**
     * 恢复配置快照
     *
     * @param snapshotId 快照ID
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 配置更新响应的Mono包装
     */
    Mono<ConfigUpdateResponse> restoreConfigSnapshot(String snapshotId, Long operatorId, String operatorName);

    /**
     * 获取配置更新状态
     *
     * @param taskId 任务ID
     * @return 更新状态的Mono包装
     */
    Mono<Map<String, Object>> getConfigUpdateStatus(String taskId);

    /**
     * 获取配置变更日志
     *
     * @param operatorId 操作用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 变更日志的Flux包装
     */
    Flux<Map<String, Object>> getConfigChangeLog(Long operatorId, int page, int size);

    /**
     * 获取配置更新统计信息
     *
     * @param operatorId 操作用户ID
     * @return 统计信息的Mono包装
     */
    Mono<Map<String, Object>> getConfigUpdateStatistics(Long operatorId);

    // ==================== 配置缓存管理 ====================

    /**
     * 刷新配置缓存
     *
     * @param configKeys 配置键名列表（为空时刷新所有）
     * @return 刷新结果的Mono包装
     */
    Mono<Map<String, Object>> refreshConfigCache(List<String> configKeys);

    /**
     * 清空配置缓存
     *
     * @param configKeys 配置键列表，如果为空则清空所有缓存
     * @return 清空结果的Mono包装
     */
    Mono<Void> clearConfigCache(List<String> configKeys);

    /**
     * 预热配置缓存
     *
     * @return 预热结果的Mono包装
     */
    Mono<Map<String, Object>> warmupConfigCache();

    /**
     * 获取配置缓存状态
     *
     * @return 缓存状态的Mono包装
     */
    Mono<Map<String, Object>> getConfigCacheStatus();

    /**
     * 同步配置缓存
     *
     * @param targetNodes 目标节点列表（集群环境）
     * @return 同步结果的Mono包装
     */
    Mono<Map<String, Object>> syncConfigCache(List<String> targetNodes);

    // ==================== 配置监控功能 ====================

    /**
     * 获取配置更新历史
     *
     * @param configKey 配置键
     * @param page 页码
     * @param size 每页大小
     * @return 更新历史的Flux包装
     */
    Flux<Map<String, Object>> getConfigUpdateHistory(String configKey, int page, int size);

    /**
     * 获取配置更新统计
     *
     * @param days 统计天数
     * @return 更新统计的Mono包装
     */
    Mono<Map<String, Object>> getConfigUpdateStats(int days);

    /**
     * 监控配置变更
     *
     * @param configKeys 监控的配置键名列表
     * @return 配置变更事件流的Flux包装
     */
    Flux<Map<String, Object>> monitorConfigChanges(List<String> configKeys);

    /**
     * 获取配置健康状态
     *
     * @return 健康状态的Mono包装
     */
    Mono<Map<String, Object>> getConfigHealthStatus();

    // ==================== 配置模板功能 ====================

    /**
     * 获取配置模板
     *
     * @param templateType 模板类型
     * @return 配置模板的Mono包装
     */
    Mono<Map<String, Object>> getConfigTemplate(String templateType);

    /**
     * 应用配置模板
     *
     * @param templateType 模板类型
     * @param templateParams 模板参数
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 应用结果的Mono包装
     */
    Mono<ConfigUpdateResponse> applyConfigTemplate(String templateType, Map<String, Object> templateParams, Long operatorId, String operatorName);

    /**
     * 创建配置模板
     *
     * @param templateName 模板名称
     * @param templateDescription 模板描述
     * @param configKeys 配置键名列表
     * @param operatorId 操作用户ID
     * @return 创建结果的Mono包装
     */
    Mono<Map<String, Object>> createConfigTemplate(String templateName, String templateDescription, List<String> configKeys, Long operatorId);

    /**
     * 删除配置模板
     *
     * @param templateId 模板ID
     * @param operatorId 操作用户ID
     * @return 删除结果的Mono包装
     */
    Mono<Boolean> deleteConfigTemplate(String templateId, Long operatorId);

    // ==================== 配置导入导出功能 ====================

    /**
     * 导出配置
     *
     * @param configKeys 配置键名列表（为空时导出所有）
     * @param format 导出格式（JSON、YAML、PROPERTIES）
     * @param includeMetadata 是否包含元数据
     * @return 导出数据的Mono包装
     */
    Mono<byte[]> exportConfigs(List<String> configKeys, String format, boolean includeMetadata);

    /**
     * 导入配置
     *
     * @param configData 配置数据
     * @param format 数据格式
     * @param importMode 导入模式（MERGE、REPLACE、SKIP_EXISTING）
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 导入结果的Mono包装
     */
    Mono<ConfigUpdateResponse> importConfigs(byte[] configData, String format, String importMode, Long operatorId, String operatorName);

    /**
     * 验证导入配置
     *
     * @param configData 配置数据
     * @param format 数据格式
     * @return 验证结果的Mono包装
     */
    Mono<Map<String, Object>> validateImportConfigs(byte[] configData, String format);

    // ==================== 配置安全功能 ====================

    /**
     * 加密敏感配置
     *
     * @param configKey 配置键名
     * @param configValue 配置值
     * @return 加密结果的Mono包装
     */
    Mono<String> encryptSensitiveConfig(String configKey, String configValue);

    /**
     * 解密敏感配置
     *
     * @param configKey 配置键名
     * @param encryptedValue 加密值
     * @return 解密结果的Mono包装
     */
    Mono<String> decryptSensitiveConfig(String configKey, String encryptedValue);

    /**
     * 检查配置权限
     *
     * @param configKey 配置键名
     * @param operatorId 操作用户ID
     * @param operation 操作类型（READ、WRITE、DELETE）
     * @return 权限检查结果的Mono包装
     */
    Mono<Boolean> checkConfigPermission(String configKey, Long operatorId, String operation);

    /**
     * 审计配置操作
     *
     * @param configKey 配置键名
     * @param operation 操作类型
     * @param oldValue 旧值
     * @param newValue 新值
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 审计结果的Mono包装
     */
    Mono<Void> auditConfigOperation(String configKey, String operation, String oldValue, String newValue, Long operatorId, String operatorName);

    // ==================== 配置通知功能 ====================

    /**
     * 发送配置变更通知
     *
     * @param configKeys 变更的配置键名列表
     * @param changeType 变更类型
     * @param operatorName 操作用户名
     * @return 通知结果的Mono包装
     */
    Mono<Void> sendConfigChangeNotification(List<String> configKeys, String changeType, String operatorName);

    /**
     * 订阅配置变更通知
     *
     * @param configKeys 订阅的配置键名列表
     * @param subscriberId 订阅者ID
     * @return 订阅结果的Mono包装
     */
    Mono<Boolean> subscribeConfigChangeNotification(List<String> configKeys, String subscriberId);

    /**
     * 取消配置变更通知订阅
     *
     * @param subscriberId 订阅者ID
     * @return 取消结果的Mono包装
     */
    Mono<Boolean> unsubscribeConfigChangeNotification(String subscriberId);

    // ==================== 配置环境管理 ====================

    /**
     * 获取环境配置差异
     *
     * @param sourceEnv 源环境
     * @param targetEnv 目标环境
     * @return 配置差异的Mono包装
     */
    Mono<Map<String, Object>> getConfigDifferences(String sourceEnv, String targetEnv);

    /**
     * 同步环境配置
     *
     * @param sourceEnv 源环境
     * @param targetEnv 目标环境
     * @param configKeys 同步的配置键名列表
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 同步结果的Mono包装
     */
    Mono<ConfigUpdateResponse> syncEnvironmentConfigs(String sourceEnv, String targetEnv, List<String> configKeys, Long operatorId, String operatorName);

    /**
     * 切换配置环境
     *
     * @param targetEnv 目标环境
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 切换结果的Mono包装
     */
    Mono<ConfigUpdateResponse> switchConfigEnvironment(String targetEnv, Long operatorId, String operatorName);
}

