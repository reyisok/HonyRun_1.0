package com.honyrun.repository.r2dbc;

import com.honyrun.model.entity.business.Permission;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 权限响应式仓库接口 - 简化权限模型
 * 【权限架构】：采用用户-权限直接映射模型，去除角色中间层
 * 【权限分类】：系统权限、业务权限、功能权限三大类
 * 【用户类型标准化】：严格使用SYSTEM_USER、NORMAL_USER、GUEST，禁止role概念
 *
 * @author Mr.Rey
 * @version 1.0.0
 * @created 2025-01-15
 * @modified 2025-01-15
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Repository
public interface ReactivePermissionRepository extends R2dbcRepository<Permission, Long> {

    /**
     * 根据权限名称查询权限
     * 【权限查询】：通过唯一权限名称查找权限
     *
     * @param name 权限名称
     * @return 权限信息
     */
    Mono<Permission> findByName(String name);

    /**
     * 根据资源查询权限列表
     * 【资源权限】：查询特定资源的所有权限
     *
     * @param resource 资源名称
     * @return 权限列表
     */
    Flux<Permission> findByResource(String resource);

    /**
     * 根据资源和操作查询权限
     * 【精确权限】：查询特定资源的特定操作权限
     *
     * @param resource 资源名称
     * @param action 操作类型
     * @return 权限信息
     */
    Mono<Permission> findByResourceAndAction(String resource, String action);

    /**
     * 查询所有系统权限
     * 【系统权限】：查询所有系统级权限
     *
     * @return 系统权限列表
     */
    @Query("SELECT * FROM permissions WHERE resource = 'system' ORDER BY name")
    Flux<Permission> findSystemPermissions();

    /**
     * 查询所有业务权限
     * 【业务权限】：查询所有业务级权限
     *
     * @return 业务权限列表
     */
    @Query("SELECT * FROM permissions WHERE resource = 'business' ORDER BY name")
    Flux<Permission> findBusinessPermissions();

    /**
     * 查询所有功能权限
     * 【功能权限】：查询所有功能级权限
     *
     * @return 功能权限列表
     */
    @Query("SELECT * FROM permissions WHERE resource = 'function' ORDER BY name")
    Flux<Permission> findFunctionPermissions();

    /**
     * 检查权限名称是否存在
     * 修复Boolean到Integer转换错误：使用CASE WHEN语句确保返回明确的布尔值
     *
     * @param name 权限名称
     * @return 是否存在的Mono包装
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM permissions WHERE name = :name")
    Mono<Boolean> existsByName(String name);

    /**
     * 根据权限名称列表查询权限
     * 【批量查询】：批量查询权限信息
     *
     * @param names 权限名称列表
     * @return 权限列表
     */
    @Query("SELECT * FROM permissions WHERE name IN (:names)")
    Flux<Permission> findByNameIn(Iterable<String> names);

    /**
     * 查询所有权限名称
     * 【权限列表】：获取所有权限名称
     *
     * @return 权限名称列表
     */
    @Query("SELECT name FROM permissions ORDER BY name")
    Flux<String> findAllPermissionNames();
}
