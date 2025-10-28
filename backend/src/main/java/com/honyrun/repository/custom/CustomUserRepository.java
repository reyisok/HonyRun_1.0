package com.honyrun.repository.custom;

import com.honyrun.model.entity.business.User;
import com.honyrun.model.enums.UserType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 自定义用户仓库接口
 *
 * 定义复杂的用户查询方法，用于实现无法通过Spring Data R2DBC自动生成的复杂查询
 * 提供响应式的自定义数据访问方法，支持复杂的业务查询需求
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  21:03:21
 * @modified 2025-07-01 21:03:21
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface CustomUserRepository {

    // ==================== 复杂条件查询方法 ====================

    /**
     * 根据动态条件查询用户列表
     *
     * @param conditions 查询条件Map，支持以下键值：
     *                  - username: 用户名（模糊匹配）
     *                  - realName: 真实姓名（模糊匹配）
     *                  - email: 邮箱地址（精确匹配）
     *                  - phone: 手机号码（精确匹配）
     *                  - userType: 用户类型
     *                  - status: 用户状态
     *                  - enabled: 是否启用
     *                  - locked: 是否锁定
     *                  - validFrom: 有效期开始时间（大于等于）
     *                  - validTo: 有效期结束时间（小于等于）
     *                  - createdAfter: 创建时间（大于等于）
     *                  - createdBefore: 创建时间（小于等于）
     *                  - lastLoginAfter: 最后登录时间（大于等于）
     *                  - lastLoginBefore: 最后登录时间（小于等于）
     * @param offset 偏移量（分页）
     * @param limit 限制数量（分页）
     * @param orderBy 排序字段
     * @param orderDirection 排序方向（ASC/DESC）
     * @return 用户列表的Flux包装
     */
    Flux<User> findByDynamicConditions(Map<String, Object> conditions,
                                      Long offset,
                                      Integer limit,
                                      String orderBy,
                                      String orderDirection);

    /**
     * 统计动态条件查询的用户数量
     *
     * @param conditions 查询条件Map（同findByDynamicConditions）
     * @return 用户数量的Mono包装
     */
    Mono<Long> countByDynamicConditions(Map<String, Object> conditions);

    // ==================== 统计分析方法 ====================

    /**
     * 获取用户类型统计信息
     *
     * @return 用户类型统计Map的Mono包装，键为用户类型，值为数量
     */
    Mono<Map<UserType, Long>> getUserTypeStatistics();

    /**
     * 获取用户状态统计信息
     *
     * @return 用户状态统计Map的Mono包装，键为状态描述，值为数量
     */
    Mono<Map<String, Long>> getUserStatusStatistics();

    /**
     * 获取用户注册趋势统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param groupBy 分组方式（DAY/WEEK/MONTH）
     * @return 注册趋势统计的Flux包装，每个元素包含日期和数量
     */
    Flux<Map<String, Object>> getUserRegistrationTrend(LocalDateTime startDate,
                                                      LocalDateTime endDate,
                                                      String groupBy);

    /**
     * 获取用户活跃度统计
     *
     * @param days 统计天数
     * @return 活跃度统计Map的Mono包装，包含活跃用户数、总用户数等
     */
    Mono<Map<String, Object>> getUserActivityStatistics(Integer days);

    // ==================== 安全相关查询方法 ====================

    /**
     * 查找可疑登录用户
     * 根据登录失败次数、登录IP变化等因素判断
     *
     * @param failureThreshold 失败次数阈值
     * @param timeWindow 时间窗口（小时）
     * @return 可疑用户列表的Flux包装
     */
    Flux<User> findSuspiciousUsers(Integer failureThreshold, Integer timeWindow);

    /**
     * 查找需要密码重置的用户
     * 根据密码使用时间、登录失败次数等因素判断
     *
     * @param passwordMaxAge 密码最大使用天数
     * @param forceResetFailureCount 强制重置的失败次数
     * @return 需要密码重置的用户列表的Flux包装
     */
    Flux<User> findUsersNeedingPasswordReset(Integer passwordMaxAge, Integer forceResetFailureCount);

    /**
     * 查找长期未使用的账户
     *
     * @param inactiveDays 未活跃天数
     * @return 长期未使用的用户列表的Flux包装
     */
    Flux<User> findInactiveAccounts(Integer inactiveDays);

    // ==================== 权限相关查询方法 ====================

    /**
     * 查找具有特定权限的用户
     *
     * @param permissionCode 权限代码
     * @return 具有该权限的用户列表的Flux包装
     */
    Flux<User> findUsersByPermission(String permissionCode);

    /**
     * 查找用户的所有权限
     *
     * @param userId 用户ID
     * @return 用户权限列表的Flux包装
     */
    Flux<String> findUserPermissions(Long userId);

    /**
     * 检查用户是否具有特定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return 是否具有权限的Mono包装
     */
    Mono<Boolean> hasPermission(Long userId, String permissionCode);

    // ==================== 批量操作方法 ====================

    /**
     * 批量更新用户信息
     *
     * @param userIds 用户ID列表
     * @param updateFields 更新字段Map
     * @return 更新行数的Mono包装
     */
    Mono<Integer> batchUpdateUsers(Iterable<Long> userIds, Map<String, Object> updateFields);

    /**
     * 批量启用/禁用用户
     *
     * @param userIds 用户ID列表
     * @param enabled 是否启用
     * @return 更新行数的Mono包装
     */
    Mono<Integer> batchToggleUserStatus(Iterable<Long> userIds, Boolean enabled);

    /**
     * 批量重置用户密码
     *
     * @param userIds 用户ID列表
     * @param newPassword 新密码（已加密）
     * @return 更新行数的Mono包装
     */
    Mono<Integer> batchResetPasswords(Iterable<Long> userIds, String newPassword);

    // ==================== 高级查询方法 ====================

    /**
     * 全文搜索用户
     * 在用户名、真实姓名、邮箱、备注等字段中搜索
     *
     * @param keyword 搜索关键词
     * @param userTypes 用户类型过滤（可选）
     * @param limit 结果限制数量
     * @return 搜索结果的Flux包装
     */
    Flux<User> fullTextSearchUsers(String keyword, Iterable<UserType> userTypes, Integer limit);

    /**
     * 查找相似用户
     * 根据用户属性（姓名、邮箱域名、注册时间等）查找相似用户
     *
     * @param userId 参考用户ID
     * @param similarityThreshold 相似度阈值（0.0-1.0）
     * @param limit 结果限制数量
     * @return 相似用户列表的Flux包装
     */
    Flux<User> findSimilarUsers(Long userId, Double similarityThreshold, Integer limit);

    /**
     * 获取用户详细信息（包含权限、统计等）
     *
     * @param userId 用户ID
     * @return 用户详细信息Map的Mono包装
     */
    Mono<Map<String, Object>> getUserDetailedInfo(Long userId);

    // ==================== 数据导出方法 ====================

    /**
     * 导出用户数据
     *
     * @param conditions 导出条件
     * @param fields 导出字段列表
     * @return 用户数据的Flux包装
     */
    Flux<Map<String, Object>> exportUserData(Map<String, Object> conditions, Iterable<String> fields);

    /**
     * 获取用户数据摘要
     *
     * @param userId 用户ID
     * @return 用户数据摘要的Mono包装
     */
    Mono<Map<String, Object>> getUserDataSummary(Long userId);

    // ==================== 缓存相关方法 ====================

    /**
     * 刷新用户缓存
     *
     * @param userId 用户ID
     * @return 操作结果的Mono包装
     */
    Mono<Boolean> refreshUserCache(Long userId);

    /**
     * 清除用户相关缓存
     *
     * @param userId 用户ID
     * @return 操作结果的Mono包装
     */
    Mono<Boolean> clearUserCache(Long userId);

    // ==================== 审计相关方法 ====================

    /**
     * 记录用户操作审计日志
     *
     * @param userId 用户ID
     * @param operation 操作类型
     * @param details 操作详情
     * @param ipAddress IP地址
     * @return 操作结果的Mono包装
     */
    Mono<Boolean> auditUserOperation(Long userId, String operation, String details, String ipAddress);

    /**
     * 查询用户操作历史
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 操作历史的Flux包装
     */
    Flux<Map<String, Object>> getUserOperationHistory(Long userId,
                                                     LocalDateTime startTime,
                                                     LocalDateTime endTime,
                                                     Integer limit);
}



