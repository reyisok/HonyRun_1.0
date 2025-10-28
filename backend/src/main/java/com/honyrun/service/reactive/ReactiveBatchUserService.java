package com.honyrun.service.reactive;

import java.util.List;
import java.util.Map;

import com.honyrun.model.dto.request.BatchPermissionAssignRequest;
import com.honyrun.model.dto.request.BatchUserCreateRequest;
import com.honyrun.model.dto.request.BatchUserImportRequest;
import com.honyrun.model.dto.response.BatchOperationResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式批量用户服务接口
 *
 * 基于WebFlux的响应式批量用户管理服务，提供非阻塞的批量用户操作功能
 * 支持批量创建、导入、权限分配等批量用户管理功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 11:50:00
 * @modified 2025-07-01 11:50:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveBatchUserService {

    // ==================== 批量用户创建功能 ====================

    /**
     * 批量创建用户
     *
     * @param request 批量创建请求
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> batchCreateUsers(BatchUserCreateRequest request, Long operatorId, String operatorName);

    /**
     * 异步批量创建用户
     *
     * @param request 批量创建请求
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> asyncBatchCreateUsers(BatchUserCreateRequest request, Long operatorId, String operatorName);

    /**
     * 验证批量创建用户数据
     *
     * @param request 批量创建请求
     * @return 验证结果的Mono包装
     */
    Mono<Map<String, Object>> validateBatchCreateData(BatchUserCreateRequest request);

    /**
     * 预览批量创建用户
     *
     * @param request 批量创建请求
     * @return 预览结果的Mono包装
     */
    Mono<Map<String, Object>> previewBatchCreate(BatchUserCreateRequest request);

    // ==================== 批量用户导入功能 ====================

    /**
     * 批量导入用户
     *
     * @param request 批量导入请求
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> batchImportUsers(BatchUserImportRequest request, Long operatorId, String operatorName);

    /**
     * 异步批量导入用户
     *
     * @param request 批量导入请求
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> asyncBatchImportUsers(BatchUserImportRequest request, Long operatorId, String operatorName);

    /**
     * 解析导入文件
     *
     * @param request 批量导入请求
     * @return 解析结果的Mono包装
     */
    Mono<Map<String, Object>> parseImportFile(BatchUserImportRequest request);

    /**
     * 验证导入数据
     *
     * @param request 批量导入请求
     * @return 验证结果的Mono包装
     */
    Mono<Map<String, Object>> validateImportData(BatchUserImportRequest request);

    /**
     * 预览导入数据
     *
     * @param request 批量导入请求
     * @param previewCount 预览数量
     * @return 预览结果的Mono包装
     */
    Mono<Map<String, Object>> previewImportData(BatchUserImportRequest request, int previewCount);

    /**
     * 下载用户导入模板
     *
     * @param format 文件格式（EXCEL、CSV）
     * @return 模板文件字节流的Mono包装
     */
    Mono<byte[]> downloadImportTemplate(String format);

    // ==================== 批量权限分配功能 ====================

    /**
     * 批量分配权限
     *
     * @param request 批量权限分配请求
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> batchAssignPermissions(BatchPermissionAssignRequest request, Long operatorId, String operatorName);

    /**
     * 异步批量分配权限
     *
     * @param request 批量权限分配请求
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> asyncBatchAssignPermissions(BatchPermissionAssignRequest request, Long operatorId, String operatorName);

    /**
     * 批量撤销权限
     *
     * @param userIds 用户ID列表
     * @param permissionCodes 权限代码列表
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> batchRevokePermissions(List<Long> userIds, List<String> permissionCodes, Long operatorId, String operatorName);

    /**
     * 批量替换权限
     *
     * @param userIds 用户ID列表
     * @param permissionCodes 权限代码列表
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> batchReplacePermissions(List<Long> userIds, List<String> permissionCodes, Long operatorId, String operatorName);

    /**
     * 预览权限分配
     *
     * @param request 批量权限分配请求
     * @return 预览结果的Mono包装
     */
    Mono<Map<String, Object>> previewPermissionAssign(BatchPermissionAssignRequest request);

    // ==================== 批量用户操作功能 ====================

    /**
     * 批量启用用户
     *
     * @param userIds 用户ID列表
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> batchEnableUsers(List<Long> userIds, Long operatorId, String operatorName);

    /**
     * 批量禁用用户
     *
     * @param userIds 用户ID列表
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> batchDisableUsers(List<Long> userIds, Long operatorId, String operatorName);

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID列表
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> batchDeleteUsers(List<Long> userIds, Long operatorId, String operatorName);

    /**
     * 批量重置密码
     *
     * @param userIds 用户ID列表
     * @param newPassword 新密码
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> batchResetPasswords(List<Long> userIds, String newPassword, Long operatorId, String operatorName);

    /**
     * 批量更新用户信息
     *
     * @param userIds 用户ID列表
     * @param updateData 更新数据
     * @param operatorId 操作用户ID
     * @param operatorName 操作用户名
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> batchUpdateUsers(List<Long> userIds, Map<String, Object> updateData, Long operatorId, String operatorName);

    // ==================== 批量操作状态管理 ====================

    /**
     * 获取批量操作状态
     *
     * @param taskId 任务ID
     * @return 批量操作响应的Mono包装
     */
    Mono<BatchOperationResponse> getBatchOperationStatus(String taskId);

    /**
     * 取消批量操作
     *
     * @param taskId 任务ID
     * @param operatorId 操作用户ID
     * @return 取消结果的Mono包装
     */
    Mono<Boolean> cancelBatchOperation(String taskId, Long operatorId);

    /**
     * 获取用户的批量操作历史
     *
     * @param operatorId 操作用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 批量操作历史的Flux包装
     */
    Flux<BatchOperationResponse> getBatchOperationHistory(Long operatorId, int page, int size);

    /**
     * 清理过期的批量操作记录
     *
     * @return 清理结果的Mono包装
     */
    Mono<Void> cleanupExpiredBatchOperations();

    // ==================== 批量操作统计功能 ====================

    /**
     * 统计批量操作概况
     *
     * @param operatorId 操作用户ID（可选）
     * @return 统计结果的Mono包装
     */
    Mono<Map<String, Object>> getBatchOperationStats(Long operatorId);

    /**
     * 获取批量操作性能指标
     *
     * @return 性能指标的Mono包装
     */
    Mono<Map<String, Object>> getBatchOperationMetrics();

    /**
     * 分析批量操作趋势
     *
     * @param days 分析天数
     * @return 趋势分析结果的Mono包装
     */
    Mono<Map<String, Object>> analyzeBatchOperationTrends(int days);

    // ==================== 批量数据验证功能 ====================

    /**
     * 验证用户名唯一性
     *
     * @param usernames 用户名列表
     * @return 验证结果的Mono包装
     */
    Mono<Map<String, Boolean>> validateUsernameUniqueness(List<String> usernames);

    /**
     * 验证手机号唯一性
     *
     * @param phoneNumbers 手机号列表
     * @return 验证结果的Mono包装
     */
    Mono<Map<String, Boolean>> validatePhoneNumberUniqueness(List<String> phoneNumbers);

    /**
     * 验证邮箱唯一性
     *
     * @param emails 邮箱列表
     * @return 验证结果的Mono包装
     */
    Mono<Map<String, Boolean>> validateEmailUniqueness(List<String> emails);

    /**
     * 验证权限代码有效性
     *
     * @param permissionCodes 权限代码列表
     * @return 验证结果的Mono包装
     */
    Mono<Map<String, Boolean>> validatePermissionCodes(List<String> permissionCodes);

    // ==================== 批量数据导出功能 ====================

    /**
     * 导出批量操作结果
     *
     * @param taskId 任务ID
     * @param format 导出格式（EXCEL、CSV、JSON）
     * @return 导出文件字节流的Mono包装
     */
    Mono<byte[]> exportBatchOperationResult(String taskId, String format);

    /**
     * 导出用户数据模板
     *
     * @param includePermissions 是否包含权限信息
     * @param format 导出格式
     * @return 模板文件字节流的Mono包装
     */
    Mono<byte[]> exportUserDataTemplate(boolean includePermissions, String format);

    /**
     * 批量导出用户数据
     *
     * @param userIds 用户ID列表
     * @param includePermissions 是否包含权限信息
     * @param format 导出格式
     * @return 导出文件字节流的Mono包装
     */
    Mono<byte[]> batchExportUserData(List<Long> userIds, boolean includePermissions, String format);

    // ==================== 批量操作配置管理 ====================

    /**
     * 获取批量操作配置
     *
     * @return 配置信息的Mono包装
     */
    Mono<Map<String, Object>> getBatchOperationConfig();

    /**
     * 更新批量操作配置
     *
     * @param config 配置信息
     * @param operatorId 操作用户ID
     * @return 更新结果的Mono包装
     */
    Mono<Boolean> updateBatchOperationConfig(Map<String, Object> config, Long operatorId);

    /**
     * 获取批量操作限制
     *
     * @param operatorId 操作用户ID
     * @return 限制信息的Mono包装
     */
    Mono<Map<String, Object>> getBatchOperationLimits(Long operatorId);
}

