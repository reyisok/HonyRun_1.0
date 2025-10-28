package com.honyrun.repository.r2dbc;

import com.honyrun.model.entity.security.UserPasswordHistory;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 用户密码历史记录响应式仓库接口
 * 提供密码历史记录的数据访问操作
 *
 * @author Mr.Rey
 * @since 2025-07-01 10:30:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Repository
public interface ReactiveUserPasswordHistoryRepository extends ReactiveCrudRepository<UserPasswordHistory, Long> {

    /**
     * 根据用户ID查找密码历史记录，按创建时间倒序排列
     *
     * @param userId 用户ID
     * @return 密码历史记录流
     */
    @Query("SELECT * FROM user_password_history WHERE user_id = :userId ORDER BY created_at DESC")
    Flux<UserPasswordHistory> findByUserIdOrderByCreatedTimeDesc(@Param("userId") Long userId);

    /**
     * 查找指定用户最旧的密码记录（用于保留操作）
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 密码历史记录流
     */
    @Query("SELECT * FROM (" +
        "SELECT * FROM user_password_history " +
        "WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit" +
        ") " +
        "ORDER BY created_at ASC")
    Flux<UserPasswordHistory> findOldestPasswordsToKeep(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 删除指定用户的旧密码历史记录，保留最新的指定数量
     *
     * @param userId    用户ID
     * @param keepCount 保留数量
     * @return 删除的记录数量
     */
    @Modifying
    @Query("DELETE FROM user_password_history " +
        "WHERE user_id = :userId " +
        "AND id NOT IN (" +
        "SELECT id FROM (" +
        "SELECT id FROM user_password_history " +
        "WHERE user_id = :userId " +
        "ORDER BY created_at DESC LIMIT :keepCount" +
        ") AS recent_passwords" +
        ")")
    Mono<Integer> deleteOldPasswordsByUserId(@Param("userId") Long userId, @Param("keepCount") int keepCount);

    /**
     * 根据用户ID和密码哈希查找匹配的历史记录
     *
     * @param userId       用户ID
     * @param passwordHash 密码哈希值
     * @return 匹配的密码历史记录
     */
    @Query("SELECT * FROM user_password_history " +
        "WHERE user_id = :userId AND password_hash = :passwordHash")
    Mono<UserPasswordHistory> findByUserIdAndPasswordHash(@Param("userId") Long userId, 
                                                          @Param("passwordHash") String passwordHash);

    /**
     * 根据用户ID查询最近的密码历史记录（限制数量）
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 密码历史记录列表
     */
    @Query("SELECT * FROM user_password_history WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    Flux<UserPasswordHistory> findRecentPasswordsByUserId(Long userId, int limit);

    /**
     * 检查用户是否使用过指定密码
     * 修复Boolean到Integer转换错误：使用CASE WHEN语句确保返回明确的布尔值
     *
     * @param userId 用户ID
     * @param passwordHash 密码哈希值
     * @return 是否使用过的Mono包装
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM user_password_history " +
           "WHERE user_id = :userId AND password_hash = :passwordHash AND deleted = 0")
    Mono<Boolean> existsByUserIdAndPasswordHash(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);

    /**
     * 检查用户在指定时间内是否更改过密码
     * 修复Boolean到Integer转换错误：使用CASE WHEN语句确保返回明确的布尔值
     *
     * @param userId 用户ID
     * @param days 天数
     * @return 是否更改过的Mono包装
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM (" +
           "SELECT 1 FROM user_password_history " +
           "WHERE user_id = :userId AND created_at >= DATEADD(DAY, -:days, GETDATE()) AND deleted = 0" +
           ") AS recent_changes")
    Mono<Boolean> hasPasswordChangedInDays(@Param("userId") Long userId, @Param("days") int days);

    /**
     * 检查最近密码中是否存在指定密码
     * 修复Boolean到Integer转换错误：使用CASE WHEN语句确保返回明确的布尔值
     *
     * @param userId       用户ID
     * @param passwordHash 密码哈希值
     * @param limit        检查的最近密码数量
     * @return 是否存在的Mono包装
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM (" +
           "SELECT password_hash FROM user_password_history " +
           "WHERE user_id = :userId ORDER BY created_date DESC LIMIT :limit" +
           ") AS recent WHERE password_hash = :passwordHash")
    Mono<Boolean> existsInRecentPasswords(Long userId, String passwordHash, int limit);

    /**
     * 删除用户的旧密码历史记录，只保留最近N条
     *
     * @param userId 用户ID
     * @param keepCount 保留的记录数量
     * @return 删除的记录数量
     */
    @Query("DELETE FROM user_password_history WHERE user_id = :userId AND id NOT IN (" +
           "SELECT id FROM (" +
           "SELECT id FROM user_password_history WHERE user_id = :userId " +
           "ORDER BY created_date DESC LIMIT :keepCount" +
           ") AS keep_records)")
    Mono<Integer> deleteOldPasswordHistory(Long userId, int keepCount);

    /**
     * 根据用户ID删除所有密码历史记录
     *
     * @param userId 用户ID
     * @return 删除的记录数量
     */
    Mono<Integer> deleteByUserId(Long userId);

    /**
     * 统计用户的密码历史记录数量
     *
     * @param userId 用户ID
     * @return 记录数量
     */
    Mono<Long> countByUserId(Long userId);

    /**
     * 删除指定时间之前的所有密码历史记录
     *
     * @param beforeTime 时间阈值
     * @return 删除的记录数量
     */
    @Modifying
    @Query("DELETE FROM user_password_history WHERE created_at < :beforeTime")
    Mono<Integer> deleteByCreatedTimeBefore(String beforeTime);
}

