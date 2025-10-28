package com.honyrun.repository.r2dbc;

import com.honyrun.model.entity.system.SystemLog;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 响应式系统日志仓库接口
 *
 * 基于R2DBC的响应式系统日志数据访问层，提供非阻塞的系统日志相关数据库操作
 * 支持系统日志实体的响应式数据访问，所有方法返回Mono或Flux类型
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 04:40:00
 * @modified 2025-07-01 04:40:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Repository
public interface ReactiveSystemLogRepository extends ReactiveCrudRepository<SystemLog, Long> {

    /**
     * 根据日志类型查找日志列表
     *
     * @param logType 日志类型
     * @return 日志列表的Flux包装
     */
    Flux<SystemLog> findByLogType(String logType);

    /**
     * 根据日志级别查找日志列表
     *
     * @param logLevel 日志级别
     * @return 日志列表的Flux包装
     */
    Flux<SystemLog> findByLogLevel(String logLevel);

    /**
     * 根据操作用户ID查找日志列表
     *
     * @param userId 用户ID
     * @return 日志列表的Flux包装
     */
    Flux<SystemLog> findByUserId(Long userId);

    /**
     * 根据操作用户名查找日志列表
     *
     * @param username 用户名
     * @return 日志列表的Flux包装
     */
    Flux<SystemLog> findByUsername(String username);

    /**
     * 根据操作模块查找日志列表
     *
     * @param module 操作模块
     * @return 日志列表的Flux包装
     */
    Flux<SystemLog> findByModule(String module);

    /**
     * 根据操作状态查找日志列表
     *
     * @param status 操作状态
     * @return 日志列表的Flux包装
     */
    Flux<SystemLog> findByStatus(Integer status);

    /**
     * 根据时间范围查找日志列表
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表的Flux包装
     */
    @Query("SELECT * FROM sys_system_logs WHERE created_at >= :startTime AND created_at <= :endTime AND deleted = 0 ORDER BY created_at DESC")
    Flux<SystemLog> findByCreatedDateBetween(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 根据日志类型和级别查找日志
     *
     * @param logType 日志类型
     * @param logLevel 日志级别
     * @return 日志列表的Flux包装
     */
    Flux<SystemLog> findByLogTypeAndLogLevel(String logType, String logLevel);

    /**
     * 查找错误日志
     *
     * @return 错误日志列表的Flux包装
     */
    @Query("SELECT * FROM sys_system_logs WHERE log_level IN ('ERROR', 'FATAL') AND deleted = 0 ORDER BY created_at DESC")
    Flux<SystemLog> findErrorLogs();

    /**
     * 查找安全相关日志
     *
     * @return 安全日志列表的Flux包装
     */
    @Query("SELECT * FROM sys_system_logs WHERE log_type = 'SECURITY' AND deleted = 0 ORDER BY created_at DESC")
    Flux<SystemLog> findSecurityLogs();

    /**
     * 查找指定用户的操作日志
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作日志列表的Flux包装
     */
    @Query("SELECT * FROM sys_system_logs WHERE user_id = :userId AND created_at >= :startTime AND created_at <= :endTime AND deleted = 0 ORDER BY created_at DESC")
    Flux<SystemLog> findUserOperationLogs(@Param("userId") Long userId,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的日志数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志数量的Mono包装
     */
    @Query("SELECT COUNT(*) FROM sys_system_logs WHERE created_at >= :startTime AND created_at <= :endTime AND deleted = 0")
    Mono<Long> countByDateRange(@Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各日志级别的数量
     *
     * @return 日志级别统计的Flux包装
     */
    @Query("SELECT log_level, COUNT(*) as count FROM sys_system_logs WHERE deleted = 0 GROUP BY log_level ORDER BY count DESC")
    Flux<Object[]> countByLogLevel();

    /**
     * 删除指定时间之前的日志
     *
     * @param beforeTime 时间阈值
     * @return 删除行数的Mono包装
     */
    @Query("DELETE FROM sys_system_logs WHERE created_at < :beforeTime")
    Mono<Integer> deleteLogsBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 根据多个条件查找日志
     *
     * @param logType 日志类型（可选）
     * @param logLevel 日志级别（可选）
     * @param module 操作模块（可选）
     * @param userId 用户ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 日志列表的Flux包装
     */
    @Query("SELECT * FROM sys_system_logs WHERE " +
           "(:logType IS NULL OR log_type = :logType) AND " +
           "(:logLevel IS NULL OR log_level = :logLevel) AND " +
           "(:module IS NULL OR module = :module) AND " +
           "(:userId IS NULL OR user_id = :userId) AND " +
           "(:startTime IS NULL OR created_at >= :startTime) AND " +
           "(:endTime IS NULL OR created_at <= :endTime) AND " +
           "deleted = 0 ORDER BY created_at DESC")
    Flux<SystemLog> findByConditions(@Param("logType") String logType,
                                    @Param("logLevel") String logLevel,
                                    @Param("module") String module,
                                    @Param("userId") Long userId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定日期之后的日志数量
     *
     * @param createdDate 创建日期
     * @return 日志数量的Mono包装
     */
    @Query("SELECT COUNT(*) FROM sys_system_logs WHERE created_at > :createdDate AND deleted = 0")
    Mono<Long> countByCreatedDateAfter(@Param("createdDate") LocalDateTime createdDate);

    /**
     * 统计指定日志级别和日期之后的日志数量
     *
     * @param logLevel 日志级别
     * @param createdDate 创建日期
     * @return 日志数量的Mono包装
     */
    @Query("SELECT COUNT(*) FROM sys_system_logs WHERE log_level = :logLevel AND created_at > :createdDate AND deleted = 0")
    Mono<Long> countByLogLevelAndCreatedDateAfter(@Param("logLevel") String logLevel, 
                                                 @Param("createdDate") LocalDateTime createdDate);

    /**
     * 根据日志类型和级别查找日志，按创建日期降序排列
     *
     * @param logTypes 日志类型列表
     * @param logLevels 日志级别列表
     * @return 日志列表的Flux包装
     */
    @Query("SELECT * FROM sys_system_logs WHERE log_type IN (:logTypes) AND log_level IN (:logLevels) AND deleted = 0 ORDER BY created_at DESC")
    Flux<SystemLog> findByLogTypeInAndLogLevelInOrderByCreatedDateDesc(@Param("logTypes") List<String> logTypes,
                                                                      @Param("logLevels") List<String> logLevels);

    /**
     * 根据日志级别查找日志，按创建日期降序排列
     *
     * @param logLevel 日志级别
     * @return 日志列表的Flux包装
     */
    @Query("SELECT * FROM sys_system_logs WHERE log_level = :logLevel AND deleted = 0 ORDER BY created_at DESC")
    Flux<SystemLog> findByLogLevelOrderByCreatedDateDesc(@Param("logLevel") String logLevel);
}

