package com.honyrun.repository.r2dbc;

import com.honyrun.model.entity.business.UserPermission;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 用户权限响应式仓库接口 - 简化权限模型
 * 【权限架构】：采用用户-权限直接映射模型，去除角色中间层
 * 【用户类型标准化】：严格使用SYSTEM_USER、NORMAL_USER、GUEST，禁止role概念
 *
 * @author Mr.Rey
 * @version 1.0.0
 * @created 2025-01-15
 * @modified 2025-01-15
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Repository
public interface ReactiveUserPermissionRepository extends R2dbcRepository<UserPermission, Long> {

    /**
     * 根据用户ID查询用户权限列表
     * 【权限查询】：直接查询用户权限，无需通过角色中间层
     *
     * @param userId 用户ID
     * @return 用户权限列表
     */
    @Query("SELECT up.id, up.user_id, up.permission AS permission_code, up.permission AS permission_name, '' AS permission_description, up.created_at AS granted_time, 'SYSTEM' AS granted_by, up.created_at AS valid_from, NULL AS valid_to, CASE WHEN up.is_active THEN 1 ELSE 0 END AS status, 1 AS permission_level, 0 AS deleted, up.created_at, up.updated_at AS last_modified_date FROM user_permissions up WHERE up.user_id = :userId AND up.is_active = 1")
    Flux<UserPermission> findByUserId(Long userId);

    /**
     * 根据用户ID和权限名称查询权限
     * 【权限验证】：验证用户是否拥有特定权限
     *
     * @param userId 用户ID
     * @param permissionName 权限名称
     * @return 用户权限
     */
    @Query("SELECT up.id, up.user_id, up.permission AS permission_code, up.permission AS permission_name, '' AS permission_description, up.created_at AS granted_time, 'SYSTEM' AS granted_by, up.created_at AS valid_from, NULL AS valid_to, CASE WHEN up.is_active THEN 1 ELSE 0 END AS status, 1 AS permission_level, 0 AS deleted, up.created_at, up.updated_at AS last_modified_date FROM user_permissions up WHERE up.user_id = :userId AND up.permission = :permissionName AND up.is_active = 1")
    Mono<UserPermission> findByUserIdAndPermissionName(Long userId, String permissionName);

    /**
     * 根据用户ID查询权限名称列表
     * 【权限获取】：获取用户所有有效权限名称
     *
     * @param userId 用户ID
     * @return 权限名称列表
     */
    @Query("SELECT up.permission FROM user_permissions up " +
           "WHERE up.user_id = :userId " +
           "AND up.is_active = 1")
    Flux<String> findPermissionNamesByUserId(Long userId);

    /**
     * 检查用户是否拥有指定权限
     * 修复Boolean到Integer转换错误：使用CASE WHEN语句确保返回明确的布尔值
     *
     * @param userId 用户ID
     * @param permissionName 权限名称
     * @return 是否拥有权限的Mono包装
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM user_permissions up " +
           "WHERE up.user_id = :userId " +
           "AND up.permission = :permissionName " +
           "AND up.is_active = 1")
    Mono<Boolean> hasPermission(Long userId, String permissionName);

    /**
     * 根据用户ID删除所有权限
     * 【权限清理】：清理用户所有权限
     *
     * @param userId 用户ID
     * @return 删除数量
     */
    @Query("DELETE FROM user_permissions WHERE user_id = :userId")
    Mono<Integer> deleteByUserId(Long userId);

    /**
     * 根据用户ID和权限ID删除权限
     * 【权限撤销】：撤销用户特定权限
     *
     * @param userId 用户ID
     * @param permissionId 权限ID
     * @return 删除数量
     */
    @Query("DELETE FROM user_permissions WHERE user_id = :userId AND permission = :permission")
    Mono<Integer> deleteByUserIdAndPermission(Long userId, String permission);

    /**
     * 禁用用户权限
     * 【权限禁用】：禁用而非删除权限，保留审计记录
     *
     * @param userId 用户ID
     * @param permissionId 权限ID
     * @return 更新数量
     */
    @Query("UPDATE user_permissions SET is_active = 0, updated_at = CURRENT_TIMESTAMP " +
           "WHERE user_id = :userId AND permission = :permission")
    Mono<Integer> disablePermission(Long userId, String permission);

    /**
     * 启用用户权限
     * 【权限启用】：启用指定用户的特定权限
     *
     * @param userId 用户ID
     * @param permission 权限名称
     * @return 更新数量
     */
    @Query("UPDATE user_permissions SET is_active = 1, updated_at = CURRENT_TIMESTAMP " +
           "WHERE user_id = :userId AND permission = :permission")
    Mono<Integer> enablePermission(Long userId, String permission);

    /**
     * 更新用户权限状态
     * 【权限状态管理】：统一管理权限的启用/禁用状态
     *
     * @param userId 用户ID
     * @param permission 权限名称
     * @param isActive 是否激活
     * @return 更新数量
     */
    @Query("UPDATE user_permissions SET is_active = :isActive, updated_at = CURRENT_TIMESTAMP " +
           "WHERE user_id = :userId AND permission = :permission")
    Mono<Integer> updateActiveStatus(Long userId, String permission, Boolean isActive);
}