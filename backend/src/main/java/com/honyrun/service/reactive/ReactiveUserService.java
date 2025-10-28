package com.honyrun.service.reactive;

import com.honyrun.model.entity.business.UserPermission;
import com.honyrun.model.enums.UserType;
import com.honyrun.model.dto.request.UserCreateRequest;
import com.honyrun.model.dto.request.UserUpdateRequest;
import com.honyrun.model.dto.response.UserResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 响应式用户服务接口 - 简化权限模型
 * 【权限架构】：采用用户-权限直接映射模型，去除角色中间层
 * 【用户类型标准化】：严格使用SYSTEM_USER、NORMAL_USER、GUEST，禁止role概念
 * 【项目规则56】：本项目任何地方禁止出现role相关的代码、逻辑、目录、包
 * 【项目规则58】：用户类型必须使用"SYSTEM_USER"、"NORMAL_USER"、"GUEST"，不得出现任何其他简化版本
 *
 * 定义用户管理相关的业务方法，包括用户CRUD操作、权限管理、状态控制等功能
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理
 * 提供完整的用户生命周期管理和权限控制功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  20:00:00
 * @modified 2025-01-15 当前时间
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveUserService {

    // ==================== 用户基础CRUD操作 ====================

    /**
     * 创建新用户
     *
     * @param request 用户创建请求对象，包含用户基本信息
     * @return 创建成功的用户响应对象
     */
    Mono<UserResponse> createUser(UserCreateRequest request);

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户响应对象，如果用户不存在则返回空Mono
     */
    Mono<UserResponse> getUserById(Long userId);

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户响应对象，如果用户不存在则返回空Mono
     */
    Mono<UserResponse> getUserByUsername(String username);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param request 用户更新请求对象
     * @return 更新后的用户响应对象
     */
    Mono<UserResponse> updateUser(Long userId, UserUpdateRequest request);

    /**
     * 删除用户（软删除）
     *
     * @param userId 用户ID
     * @return 删除操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> deleteUser(Long userId);

    /**
     * 批量删除用户（软删除）
     *
     * @param userIds 用户ID列表
     * @return 删除操作结果，返回成功删除的用户数量
     */
    Mono<Long> batchDeleteUsers(List<Long> userIds);

    // ==================== 用户查询操作 ====================

    /**
     * 分页查询所有用户
     *
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 用户响应对象流
     */
    Flux<UserResponse> getAllUsers(int page, int size);

    /**
     * 根据用户类型查询用户
     *
     * @param userType 用户类型
     * @return 用户响应对象流
     */
    Flux<UserResponse> getUsersByType(UserType userType);

    /**
     * 根据用户状态查询用户
     *
     * @param enabled 用户状态，true为启用，false为禁用
     * @return 用户响应对象流
     */
    Flux<UserResponse> getUsersByStatus(Boolean enabled);

    /**
     * 根据关键字搜索用户
     * 支持按用户名、真实姓名、邮箱进行模糊搜索
     *
     * @param keyword 搜索关键字
     * @return 用户响应对象流
     */
    Flux<UserResponse> searchUsers(String keyword);

    /**
     * 分页查询用户列表（带搜索功能）
     *
     * @param page 页码，从0开始
     * @param size 每页大小
     * @param keyword 搜索关键字，可为空
     * @return 用户响应对象流
     */
    Flux<UserResponse> getUserList(int page, int size, String keyword);

    /**
     * 统计用户总数
     *
     * @return 用户总数
     */
    Mono<Long> countUsers();

    /**
     * 根据用户类型统计用户数量
     *
     * @param userType 用户类型
     * @return 指定类型的用户数量
     */
    Mono<Long> countUsersByType(UserType userType);

    // ==================== 用户状态管理 ====================

    /**
     * 启用用户
     *
     * @param userId 用户ID
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> enableUser(Long userId);

    /**
     * 禁用用户
     *
     * @param userId 用户ID
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> disableUser(Long userId);

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 用户状态
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> updateUserStatus(Long userId, String status);

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> resetPassword(Long userId, String newPassword);

    /**
     * 修改用户密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> changePassword(Long userId, String oldPassword, String newPassword);

    // ==================== 用户权限管理 ====================

    /**
     * 为用户分配权限
     *
     * @param userId 用户ID
     * @param permissionCodes 权限代码列表
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> assignPermissions(Long userId, List<String> permissionCodes);

    /**
     * 撤销用户权限
     *
     * @param userId 用户ID
     * @param permissionCodes 权限代码列表
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> revokePermissions(Long userId, List<String> permissionCodes);

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 用户权限对象流
     */
    Flux<UserPermission> getUserPermissions(Long userId);

    /**
     * 获取用户权限代码列表
     *
     * @param userId 用户ID
     * @return 用户权限代码流
     */
    Flux<String> getUserPermissionCodes(Long userId);

    /**
     * 检查用户是否具有特定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return 是否具有权限
     */
    Mono<Boolean> hasPermission(Long userId, String permissionCode);

    /**
     * 批量检查用户权限
     *
     * @param userId 用户ID
     * @param permissionCodes 权限代码列表
     * @return 权限检查结果映射，key为权限代码，value为是否拥有该权限
     */
    Mono<Map<String, Boolean>> hasPermissions(Long userId, List<String> permissionCodes);

    /**
     * 清除用户所有权限
     *
     * @param userId 用户ID
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> clearUserPermissions(Long userId);

    // ==================== 用户活动记录 ====================

    /**
     * 获取用户活动记录
     *
     * @param userId 用户ID
     * @return 用户活动记录流
     */
    Flux<com.honyrun.model.dto.reactive.ReactiveActiveActivity> getUserActivities(Long userId);

    // ==================== 用户验证操作 ====================

    /**
     * 用户认证
     * 验证用户名和密码，返回认证成功的用户对象
     *
     * @param username 用户名
     * @param password 密码
     * @return 认证成功的用户对象
     */
    Mono<com.honyrun.model.entity.business.User> authenticateUser(String username, String password);

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    Mono<Boolean> existsByUsername(String username);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱地址
     * @return 是否存在
     */
    Mono<Boolean> existsByEmail(String email);

    /**
     * 检查手机号是否已存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    Mono<Boolean> existsByPhone(String phone);

    /**
     * 验证用户密码
     *
     * @param userId 用户ID
     * @param password 密码
     * @return 验证结果
     */
    Mono<Boolean> validatePassword(Long userId, String password);

    /**
     * 批量启用用户
     *
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    Mono<Boolean> batchEnableUsers(List<Long> userIds);

    /**
     * 批量禁用用户
     *
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    Mono<Boolean> batchDisableUsers(List<Long> userIds);

    // ==================== 用户统计分析 ====================

    /**
     * 获取用户注册统计
     * 统计指定时间段内的用户注册数量
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 注册统计数据
     */
    Mono<Map<String, Object>> getUserRegistrationStats(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取用户类型分布统计
     *
     * @return 用户类型分布统计数据
     */
    Mono<Map<UserType, Long>> getUserTypeDistribution();

    /**
     * 获取用户活跃度统计
     * 统计指定时间段内的活跃用户数量
     *
     * @param days 统计天数
     * @return 活跃用户数量
     */
    Mono<Long> getActiveUserCount(int days);

    /**
     * 获取最近登录的用户列表
     *
     * @param limit 返回数量限制
     * @return 用户响应对象流
     */
    Flux<UserResponse> getRecentlyLoggedInUsers(int limit);

    /**
     * 获取用户统计信息
     * 包含用户总数、活跃用户数、用户类型分布等统计数据
     *
     * @return 用户统计信息
     */
    Mono<Map<String, Object>> getUserStatistics();
}

