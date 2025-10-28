package com.honyrun.repository.r2dbc;

import com.honyrun.model.entity.business.User;
import com.honyrun.model.enums.UserType;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 响应式用户仓库接口
 *
 * 基于R2DBC的响应式用户数据访问层，提供非阻塞的数据库操作
 * 继承ReactiveCrudRepository，支持基本的CRUD操作和自定义查询方法
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  18:30:00
 * @modified 2025-07-01 18:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Repository
public interface ReactiveUserRepository extends ReactiveCrudRepository<User, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息的Mono包装
     */
    Mono<User> findByUsername(String username);

    /**
     * 根据用户名和状态查找用户
     *
     * @param username 用户名
     * @param status 用户状态
     * @return 用户信息的Mono包装
     */
    Mono<User> findByUsernameAndStatus(String username, String status);

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱地址
     * @return 用户信息的Mono包装
     */
    Mono<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     *
     * @param phone 手机号码
     * @return 用户信息的Mono包装
     */
    Mono<User> findByPhone(String phone);

    /**
     * 根据用户类型查找用户列表
     *
     * @param userType 用户类型
     * @return 用户列表的Flux包装
     */
    Flux<User> findByUserType(UserType userType);

    /**
     * 根据状态查找用户列表
     *
     * @param status 用户状态
     * @return 用户列表的Flux包装
     */
    Flux<User> findByStatus(String status);

    /**
     * 根据真实姓名模糊查找用户
     *
     * @param realName 真实姓名（支持模糊匹配）
     * @return 用户列表的Flux包装
     */
    Flux<User> findByRealNameContainingIgnoreCase(String realName);

    /**
     * 检查用户名是否存在（排除已删除用户）
     * 修复Boolean到Integer转换错误：返回COUNT结果，在Service层转换为Boolean
     *
     * @param username 用户名
     * @return 匹配记录数的Mono包装
     */
    @Query("SELECT COUNT(*) FROM sys_users WHERE username = :username AND deleted = 0")
    Mono<Long> countByUsernameAndDeletedFalse(String username);

    /**
     * 检查邮箱是否存在（排除已删除用户）
     * 修复Boolean到Integer转换错误：返回COUNT结果，在Service层转换为Boolean
     *
     * @param email 邮箱地址
     * @return 匹配记录数的Mono包装
     */
    @Query("SELECT COUNT(*) FROM sys_users WHERE email = :email AND deleted = 0")
    Mono<Long> countByEmailAndDeletedFalse(String email);

    /**
     * 检查手机号是否存在（排除已删除用户）
     * 修复Boolean到Integer转换错误：返回COUNT结果，在Service层转换为Boolean
     *
     * @param phone 手机号码
     * @return 匹配记录数的Mono包装
     */
    @Query("SELECT COUNT(*) FROM sys_users WHERE phone = :phone AND deleted = 0")
    Mono<Long> countByPhoneAndDeletedFalse(String phone);

    /**
     * 根据用户名查找未删除的用户
     * 使用自定义查询确保正确处理deleted字段
     *
     * @param username 用户名
     * @return 用户信息的Mono包装
     */
    @Query("SELECT * FROM sys_users WHERE username = :username AND deleted = 0")
    Mono<User> findByUsernameAndDeletedFalse(String username);

    /**
     * 根据ID查找未删除的用户
     * 使用自定义查询确保正确处理deleted字段
     *
     * @param id 用户ID
     * @return 用户信息的Mono包装
     */
    @Query("SELECT * FROM sys_users WHERE id = :id AND deleted = 0")
    Mono<User> findByIdAndDeletedFalse(Long id);

    /**
     * 查找所有未删除的用户
     * 使用自定义查询确保正确处理deleted字段
     *
     * @return 用户列表的Flux包装
     */
    @Query("SELECT * FROM sys_users WHERE deleted = 0 ORDER BY created_at DESC")
    Flux<User> findByDeletedFalse();

    /**
     * 统计未删除的用户数量
     *
     * @return 用户数量的Mono包装
     */
    @Query("SELECT COUNT(*) FROM sys_users WHERE deleted = 0")
    Mono<Long> countByDeletedFalse();

    /**
     * 根据用户类型统计未删除的用户数量
     *
     * @param userType 用户类型
     * @return 用户数量的Mono包装
     */
    @Query("SELECT COUNT(*) FROM sys_users WHERE user_type = :userType AND deleted = 0")
    Mono<Long> countByUserTypeAndDeletedFalse(UserType userType);

    /**
     * 根据用户类型查找未删除的用户列表
     *
     * @param userType 用户类型
     * @return 用户列表的Flux包装
     */
    @Query("SELECT * FROM sys_users WHERE user_type = :userType AND deleted = 0 ORDER BY created_at DESC")
    Flux<User> findByUserTypeAndDeletedFalse(UserType userType);

    /**
     * 根据启用状态查找用户列表（排除已删除用户）
     * 修复Boolean到BIGINT转换错误：使用数值比较而不是Boolean值
     *
     * @param enabled 启用状态
     * @return 用户列表的Flux包装
     */
    @Query("SELECT * FROM sys_users WHERE " +
           "enabled = CASE WHEN :enabled = true THEN 1 ELSE 0 END " +
           "AND deleted = 0 ORDER BY created_at DESC")
    Flux<User> findByEnabledAndDeletedFalse(Boolean enabled);

    /**
     * 检查用户名是否存在
     * 修复Boolean转换错误：返回COUNT结果，在Service层转换为Boolean
     *
     * @param username 用户名
     * @return 匹配记录数的Mono包装
     */
    @Query("SELECT COUNT(*) FROM sys_users WHERE username = :username")
    Mono<Long> countByUsername(String username);

    /**
     * 检查邮箱是否存在
     * 修复Boolean转换错误：返回COUNT结果，在Service层转换为Boolean
     *
     * @param email 邮箱地址
     * @return 匹配记录数的Mono包装
     */
    @Query("SELECT COUNT(*) FROM sys_users WHERE email = :email")
    Mono<Long> countByEmail(String email);

    /**
     * 检查手机号是否存在
     * 修复Boolean转换错误：返回COUNT结果，在Service层转换为Boolean
     *
     * @param phone 手机号码
     * @return 匹配记录数的Mono包装
     */
    @Query("SELECT COUNT(*) FROM sys_users WHERE phone = :phone")
    Mono<Long> countByPhone(String phone);

    // ==================== 统计查询方法 ====================

    /**
     * 统计指定用户类型的用户数量
     *
     * @param userType 用户类型
     * @return 用户数量的Mono包装
     */
    Mono<Long> countByUserType(UserType userType);

    /**
     * 根据状态统计用户数量
     *
     * @param status 用户状态
     * @return 用户数量的Mono包装
     */
    Mono<Long> countByStatus(String status);

    /**
     * 统计启用状态的用户数量
     *
     * @return 启用用户数量的Mono包装
     */
    @Query("SELECT COUNT(*) FROM sys_users WHERE status = 'ACTIVE'")
    Mono<Long> countEnabledUsers();

    /**
     * 统计被锁定的用户数量
     *
     * @return 锁定用户数量的Mono包装
     */
    @Query("SELECT COUNT(*) FROM sys_users WHERE locked_time IS NOT NULL")
    Mono<Long> countLockedUsers();

    // ==================== 时间范围查询方法 ====================

    /**
     * 查找指定时间范围内创建的用户
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户列表的Flux包装
     */
    @Query("SELECT * FROM sys_users WHERE created_at >= :startTime AND created_at <= :endTime ORDER BY created_at DESC")
    Flux<User> findByCreatedDateBetween(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 查找指定时间范围内最后登录的用户
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户列表的Flux包装
     */
    @Query("SELECT * FROM sys_users WHERE last_login_time >= :startTime AND last_login_time <= :endTime ORDER BY last_login_time DESC")
    Flux<User> findByLastLoginTimeBetween(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 查找账户即将过期的用户
     *
     * @param beforeTime 过期时间阈值
     * @return 用户列表的Flux包装
     */
    @Query("SELECT * FROM sys_users WHERE expiry_date IS NOT NULL AND expiry_date <= :beforeTime AND status = 'ACTIVE' ORDER BY expiry_date ASC")
    Flux<User> findUsersExpiringBefore(@Param("beforeTime") LocalDate beforeTime);

    // ==================== 安全相关查询方法 ====================

    /**
     * 查找登录失败次数超过指定值的用户
     *
     * @param failureCount 失败次数阈值
     * @return 用户列表的Flux包装
     */
    @Query("SELECT * FROM sys_users WHERE failed_login_attempts >= :failureCount ORDER BY failed_login_attempts DESC")
    Flux<User> findByLoginFailureCountGreaterThanEqual(@Param("failureCount") Integer failureCount);

    /**
     * 查找被锁定的用户列表
     *
     * @return 被锁定用户列表的Flux包装
     */
    @Query("SELECT * FROM sys_users WHERE locked_until IS NOT NULL ORDER BY locked_until DESC")
    Flux<User> findLockedUsers();

    /**
     * 查找长时间未登录的用户
     *
     * @param beforeTime 最后登录时间阈值
     * @return 用户列表的Flux包装
     */
    @Query("SELECT * FROM sys_users WHERE (last_login_time IS NULL OR last_login_time < :beforeTime) AND status = 'ACTIVE' ORDER BY last_login_time ASC")
    Flux<User> findInactiveUsersBefore(@Param("beforeTime") LocalDateTime beforeTime);

    // ==================== 批量操作方法 ====================

    /**
     * 批量更新用户状态
     *
     * @param userIds 用户ID列表
     * @param status 新状态
     * @return 更新行数的Mono包装
     */
    @Query("UPDATE sys_users SET status = :status, last_modified_date = CURRENT_TIMESTAMP WHERE id IN (:userIds)")
    Mono<Integer> updateStatusByIds(@Param("userIds") Iterable<Long> userIds,
                                   @Param("status") String status);

    /**
     * 批量解锁用户
     *
     * @param userIds 用户ID列表
     * @return 更新行数的Mono包装
     */
    @Query("UPDATE sys_users SET locked_until = NULL, failed_login_attempts = 0, last_modified_date = CURRENT_TIMESTAMP WHERE id IN (:userIds)")
    Mono<Integer> unlockUsersByIds(@Param("userIds") Iterable<Long> userIds);

    /**
     * 重置用户登录失败次数
     *
     * @param username 用户名
     * @return 更新行数的Mono包装
     */
    @Query("UPDATE sys_users SET failed_login_attempts = 0, last_modified_date = CURRENT_TIMESTAMP WHERE username = :username")
    Mono<Integer> resetLoginFailureCount(@Param("username") String username);

    // ==================== 复杂查询方法 ====================

    /**
     * 根据多个条件查找用户
     *
     * @param userType 用户类型（可选）
     * @param status 用户状态（可选）
     * @param keyword 关键词（用户名或真实姓名，可选）
     * @return 用户列表的Flux包装
     */
    @Query("SELECT * FROM sys_users WHERE " +
           "(:userType IS NULL OR user_type = :userType) AND " +
           "(:status IS NULL OR status = :status) AND " +
           "(:keyword IS NULL OR username LIKE CONCAT('%', :keyword, '%') OR full_name LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY created_at DESC")
    Flux<User> findByConditions(@Param("userType") UserType userType,
                               @Param("status") String status,
                               @Param("keyword") String keyword);

    /**
     * 查找活跃用户（最近登录且状态正常）
     *
     * @param recentTime 最近时间阈值
     * @return 活跃用户列表的Flux包装
     */
    @Query("SELECT * FROM sys_users WHERE " +
           "last_login_time >= :recentTime AND " +
           "status = 'ACTIVE' AND " +
           "locked_until IS NULL AND " +
           "(expiry_date IS NULL OR expiry_date > CURRENT_DATE) " +
           "ORDER BY last_login_time DESC")
    Flux<User> findActiveUsers(@Param("recentTime") LocalDateTime recentTime);

    /**
     * 查找需要密码重置的用户
     *
     * @param passwordAge 密码使用时间阈值
     * @return 需要密码重置的用户列表的Flux包装
     */
    @Query("SELECT * FROM sys_users WHERE " +
           "last_modified_date < :passwordAge AND " +
           "status = 'ACTIVE' " +
           "ORDER BY last_modified_date ASC")
    Flux<User> findUsersNeedingPasswordReset(@Param("passwordAge") LocalDateTime passwordAge);
}