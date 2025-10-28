package com.honyrun.service.reactive;

import com.honyrun.model.dto.request.PermissionAssignRequest;
import com.honyrun.model.dto.request.PermissionBatchRequest;
import com.honyrun.model.dto.response.PermissionMatrixResponse;
import com.honyrun.model.entity.business.UserPermission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 响应式权限管理服务接口
 *
 * 定义权限管理相关的业务方法，包括用户权限矩阵管理、批量权限操作、
 * 权限查询、权限统计、权限模板管理、权限审计等功能。
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理。
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 01:10:00
 * @modified 2025-07-01 01:10:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactivePermissionService {

    // ==================== 权限矩阵管理 ====================

    /**
     * 获取用户权限矩阵
     *
     * @return 用户权限矩阵的响应式单值
     */
    Mono<PermissionMatrixResponse> getPermissionMatrix();

    /**
     * 获取指定用户的权限矩阵
     *
     * @param userId 用户ID
     * @return 用户权限矩阵的响应式单值，key为权限代码，value为是否拥有该权限
     */
    Mono<Map<String, Boolean>> getUserPermissionMatrix(Long userId);

    /**
     * 获取权限分类树
     *
     * @return 权限分类树的响应式单值
     */
    Mono<List<Map<String, Object>>> getPermissionTree();

    // ==================== 权限分配管理 ====================

    /**
     * 分配用户权限
     *
     * @param request 权限分配请求
     * @return 分配结果的响应式单值
     */
    Mono<Boolean> assignPermissions(PermissionAssignRequest request);

    /**
     * 撤销用户权限
     *
     * @param userId 用户ID
     * @param permissionCodes 权限代码列表
     * @return 撤销结果的响应式单值
     */
    Mono<Boolean> revokePermissions(Long userId, List<String> permissionCodes);

    /**
     * 更新用户权限
     *
     * @param userId 用户ID
     * @param permissionCodes 新的权限代码列表
     * @return 更新结果的响应式单值
     */
    Mono<Boolean> updateUserPermissions(Long userId, List<String> permissionCodes);

    // ==================== 批量权限操作 ====================

    /**
     * 批量分配权限
     *
     * @param request 批量权限分配请求
     * @return 分配结果的响应式单值，包含成功和失败的统计信息
     */
    Mono<Map<String, Object>> batchAssignPermissions(PermissionBatchRequest request);

    /**
     * 批量撤销权限
     *
     * @param request 批量权限撤销请求
     * @return 撤销结果的响应式单值，包含成功和失败的统计信息
     */
    Mono<Map<String, Object>> batchRevokePermissions(PermissionBatchRequest request);

    /**
     * 批量复制权限
     *
     * @param sourceUserId 源用户ID
     * @param targetUserIds 目标用户ID列表
     * @return 复制结果的响应式单值，包含成功和失败的统计信息
     */
    Mono<Map<String, Object>> batchCopyPermissions(Long sourceUserId, List<Long> targetUserIds);

    // ==================== 权限查询 ====================

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 用户权限列表的响应式流
     */
    Flux<UserPermission> getUserPermissions(Long userId);

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return 权限检查结果的响应式单值
     */
    Mono<Boolean> hasPermission(Long userId, String permissionCode);

    /**
     * 批量检查用户权限
     *
     * @param userId 用户ID
     * @param permissionCodes 权限代码列表
     * @return 权限检查结果映射的响应式单值，key为权限代码，value为是否拥有该权限
     */
    Mono<Map<String, Boolean>> hasPermissions(Long userId, List<String> permissionCodes);

    /**
     * 根据权限代码查询拥有该权限的用户
     *
     * @param permissionCode 权限代码
     * @return 用户列表的响应式单值
     */
    Mono<List<Map<String, Object>>> getUsersByPermission(String permissionCode);

    /**
     * 获取用户权限代码列表
     *
     * @param userId 用户ID
     * @return 权限代码列表的响应式流
     */
    Flux<String> getUserPermissionCodes(Long userId);

    /**
     * 获取有效的用户权限列表
     *
     * @param userId 用户ID
     * @return 有效权限列表的响应式流
     */
    Flux<UserPermission> getValidUserPermissions(Long userId);

    // ==================== 权限统计 ====================

    /**
     * 获取权限统计信息
     *
     * @return 权限统计信息的响应式单值
     */
    Mono<Map<String, Object>> getPermissionStatistics();

    /**
     * 获取用户类型权限统计
     *
     * @return 用户类型权限统计的响应式单值
     */
    Mono<Map<String, Object>> getUserTypePermissionStatistics();

    /**
     * 获取权限使用情况统计
     *
     * @return 权限使用情况统计的响应式单值
     */
    Mono<Map<String, Object>> getPermissionUsageStatistics();

    /**
     * 获取权限分布统计
     *
     * @return 权限分布统计的响应式单值
     */
    Mono<Map<String, Object>> getPermissionDistributionStatistics();

    // ==================== 权限模板管理 ====================

    /**
     * 获取权限模板列表
     *
     * @return 权限模板列表的响应式单值
     */
    Mono<List<Map<String, Object>>> getPermissionTemplates();

    /**
     * 创建权限模板
     *
     * @param templateName 模板名称
     * @param permissionCodes 权限代码列表
     * @param description 模板描述
     * @return 创建结果的响应式单值
     */
    Mono<Map<String, Object>> createPermissionTemplate(String templateName, 
                                                       List<String> permissionCodes, 
                                                       String description);

    /**
     * 应用权限模板
     *
     * @param templateId 模板ID
     * @param userIds 用户ID列表
     * @return 应用结果的响应式单值，包含成功和失败的统计信息
     */
    Mono<Map<String, Object>> applyPermissionTemplate(Long templateId, List<Long> userIds);

    /**
     * 删除权限模板
     *
     * @param templateId 模板ID
     * @return 删除结果的响应式单值
     */
    Mono<Boolean> deletePermissionTemplate(Long templateId);

    // ==================== 权限审计 ====================

    /**
     * 获取权限变更历史
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 权限变更历史的响应式单值
     */
    Mono<List<Map<String, Object>>> getPermissionAuditHistory(Long userId, int page, int size);

    /**
     * 获取权限操作日志
     *
     * @param page 页码
     * @param size 每页大小
     * @return 权限操作日志的响应式单值
     */
    Mono<List<Map<String, Object>>> getPermissionOperationLogs(int page, int size);

    /**
     * 记录权限操作日志
     *
     * @param operationType 操作类型
     * @param userId 用户ID
     * @param permissionCodes 权限代码列表
     * @param operatorId 操作人ID
     * @param description 操作描述
     * @return 记录结果的响应式单值
     */
    Mono<Boolean> recordPermissionOperation(String operationType, 
                                          Long userId, 
                                          List<String> permissionCodes, 
                                          Long operatorId, 
                                          String description);

    // ==================== 权限验证 ====================

    /**
     * 验证权限分配请求
     *
     * @param request 权限分配请求
     * @return 验证结果的响应式单值
     */
    Mono<Boolean> validatePermissionAssignRequest(PermissionAssignRequest request);

    /**
     * 验证权限代码是否存在
     *
     * @param permissionCode 权限代码
     * @return 验证结果的响应式单值
     */
    Mono<Boolean> validatePermissionCode(String permissionCode);

    /**
     * 验证用户是否存在
     *
     * @param userId 用户ID
     * @return 验证结果的响应式单值
     */
    Mono<Boolean> validateUser(Long userId);

    // ==================== 权限缓存管理 ====================

    /**
     * 刷新用户权限缓存
     *
     * @param userId 用户ID
     * @return 刷新结果的响应式单值
     */
    Mono<Boolean> refreshUserPermissionCache(Long userId);

    /**
     * 清除用户权限缓存
     *
     * @param userId 用户ID
     * @return 清除结果的响应式单值
     */
    Mono<Boolean> clearUserPermissionCache(Long userId);

    /**
     * 预热权限缓存
     *
     * @return 预热结果的响应式单值
     */
    Mono<Boolean> warmupPermissionCache();

    // ==================== 权限同步 ====================

    /**
     * 同步用户权限到缓存
     *
     * @param userId 用户ID
     * @return 同步结果的响应式单值
     */
    Mono<Boolean> syncUserPermissionsToCache(Long userId);

    /**
     * 从缓存获取用户权限
     *
     * @param userId 用户ID
     * @return 用户权限列表的响应式流
     */
    Flux<String> getUserPermissionsFromCache(Long userId);

    /**
     * 批量同步用户权限
     *
     * @param userIds 用户ID列表
     * @return 同步结果的响应式单值，包含成功和失败的统计信息
     */
    Mono<Map<String, Object>> batchSyncUserPermissions(List<Long> userIds);
}

