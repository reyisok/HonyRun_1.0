package com.honyrun.repository.r2dbc;

import com.honyrun.model.entity.transaction.TransactionLog;
import com.honyrun.request.TransactionMonitoringRequest;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 响应式事务日志数据访问接口
 *
 * 提供事务日志的响应式数据访问功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 02:00:00
 * @modified 2025-07-01 02:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Repository
public interface ReactiveTransactionLogRepository extends R2dbcRepository<TransactionLog, Long> {

    /**
     * 根据事务ID查找
     *
     * @param transactionId 事务ID
     * @return 事务日志
     */
    Mono<TransactionLog> findByTransactionId(String transactionId);

    /**
     * 根据状态查找
     *
     * @param status 状态
     * @return 事务日志流
     */
    Flux<TransactionLog> findByStatus(String status);

    /**
     * 根据事务类型查找
     *
     * @param transactionType 事务类型
     * @return 事务日志流
     */
    Flux<TransactionLog> findByTransactionType(String transactionType);

    /**
     * 根据用户ID查找
     *
     * @param userId 用户ID
     * @return 事务日志流
     */
    Flux<TransactionLog> findByUserId(String userId);

    /**
     * 根据条件查询
     *
     * @param request 查询条件
     * @return 事务日志流
     */
    @Query("SELECT * FROM sys_transaction_log WHERE " +
           "(:#{#request.transactionType} IS NULL OR transaction_type = :#{#request.transactionType}) AND " +
           "(:#{#request.status} IS NULL OR status = :#{#request.status}) AND " +
           "(:#{#request.userId} IS NULL OR user_id = :#{#request.userId}) AND " +
           "(:#{#request.startTime} IS NULL OR start_time >= :#{#request.startTime}) AND " +
           "(:#{#request.endTime} IS NULL OR end_time <= :#{#request.endTime}) AND " +
           "(:#{#request.minDuration} IS NULL OR duration >= :#{#request.minDuration}) AND " +
           "(:#{#request.maxDuration} IS NULL OR duration <= :#{#request.maxDuration}) " +
           "ORDER BY start_time DESC LIMIT :#{#request.size} OFFSET :#{#request.page * #request.size}")
    Flux<TransactionLog> findByConditions(@Param("request") TransactionMonitoringRequest request);

    /**
     * 统计各状态的事务数量
     *
     * @param status 状态
     * @return 数量
     */
    @Query("SELECT COUNT(*) FROM sys_transaction_log WHERE status = :status")
    Mono<Long> countByStatus(@Param("status") String status);

    /**
     * 统计各类型的事务数量
     *
     * @param transactionType 事务类型
     * @return 数量
     */
    @Query("SELECT COUNT(*) FROM sys_transaction_log WHERE transaction_type = :transactionType")
    Mono<Long> countByTransactionType(@Param("transactionType") String transactionType);

    /**
     * 统计用户的事务数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    @Query("SELECT COUNT(*) FROM sys_transaction_log WHERE user_id = :userId")
    Mono<Long> countByUserId(@Param("userId") String userId);

    /**
     * 统计指定类型和状态的事务数量
     *
     * @param transactionType 事务类型
     * @param status 状态
     * @return 数量
     */
    @Query("SELECT COUNT(*) FROM sys_transaction_log WHERE transaction_type = :transactionType AND status = :status")
    Mono<Long> countByTransactionTypeAndStatus(@Param("transactionType") String transactionType, 
                                               @Param("status") String status);

    /**
     * 统计指定用户和状态的事务数量
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 数量
     */
    @Query("SELECT COUNT(*) FROM sys_transaction_log WHERE user_id = :userId AND status = :status")
    Mono<Long> countByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);

    /**
     * 获取平均执行时间
     *
     * @return 平均执行时间
     */
    @Query("SELECT AVG(duration) FROM sys_transaction_log WHERE duration IS NOT NULL")
    Mono<Double> getAverageDuration();

    /**
     * 获取指定类型的平均执行时间
     *
     * @param transactionType 事务类型
     * @return 平均执行时间
     */
    @Query("SELECT AVG(duration) FROM sys_transaction_log WHERE transaction_type = :transactionType AND duration IS NOT NULL")
    Mono<Double> getAverageDurationByType(@Param("transactionType") String transactionType);

    /**
     * 获取指定用户的平均执行时间
     *
     * @param userId 用户ID
     * @return 平均执行时间
     */
    @Query("SELECT AVG(duration) FROM sys_transaction_log WHERE user_id = :userId AND duration IS NOT NULL")
    Mono<Double> getAverageDurationByUserId(@Param("userId") String userId);

    /**
     * 查找慢事务
     *
     * @param threshold 时间阈值（毫秒）
     * @return 慢事务流
     */
    @Query("SELECT * FROM sys_transaction_log WHERE duration > :threshold ORDER BY duration DESC")
    Flux<TransactionLog> findSlowTransactions(@Param("threshold") Long threshold);

    /**
     * 查找最近的失败事务
     *
     * @param since 时间起点
     * @return 失败事务流
     */
    @Query("SELECT * FROM sys_transaction_log WHERE status = 'FAILED' AND start_time >= :since ORDER BY start_time DESC")
    Flux<TransactionLog> findRecentFailedTransactions(@Param("since") LocalDateTime since);

    /**
     * 根据事务ID列表查询
     *
     * @param transactionIds 事务ID列表
     * @return 事务日志流
     */
    @Query("SELECT * FROM sys_transaction_log WHERE transaction_id IN (:transactionIds)")
    Flux<TransactionLog> findByTransactionIdIn(@Param("transactionIds") List<String> transactionIds);

    /**
     * 获取性能指标
     *
     * @return 性能指标流
     */
    @Query("SELECT " +
           "CONCAT('avg_duration:', AVG(duration)) as metric " +
           "FROM sys_transaction_log WHERE duration IS NOT NULL " +
           "UNION ALL " +
           "SELECT CONCAT('max_duration:', MAX(duration)) as metric " +
           "FROM sys_transaction_log WHERE duration IS NOT NULL " +
           "UNION ALL " +
           "SELECT CONCAT('min_duration:', MIN(duration)) as metric " +
           "FROM sys_transaction_log WHERE duration IS NOT NULL")
    Flux<String> getPerformanceMetrics();

    /**
     * 获取每小时事务数量统计
     *
     * @return 统计数据流
     */
    @Query("SELECT " +
           "DATE_FORMAT(start_time, '%Y-%m-%d %H:00:00') as hour, " +
           "COUNT(*) as count " +
           "FROM sys_transaction_log " +
           "WHERE start_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR) " +
           "GROUP BY DATE_FORMAT(start_time, '%Y-%m-%d %H:00:00') " +
           "ORDER BY hour")
    Flux<String> getHourlyTransactionCount();

    /**
     * 删除指定时间之前的记录
     *
     * @param endTime 结束时间
     * @return 删除数量
     */
    @Query("DELETE FROM sys_transaction_log WHERE end_time < :endTime")
    Mono<Long> deleteByEndTimeBefore(@Param("endTime") LocalDateTime endTime);

    /**
     * 查找指定时间之前的记录
     *
     * @param endTime 结束时间
     * @return 事务日志流
     */
    Flux<TransactionLog> findByEndTimeBefore(LocalDateTime endTime);
}

