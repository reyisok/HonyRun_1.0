package com.honyrun.repository.r2dbc;

import com.honyrun.model.entity.business.VerificationRequest;
import com.honyrun.model.entity.business.VerificationResult;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 响应式核验仓库接口
 *
 * 基于R2DBC的响应式核验数据访问层，提供非阻塞的数据库操作
 * 继承ReactiveCrudRepository，支持基本的CRUD操作和自定义查询方法
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  20:56:00
 * @modified 2025-07-01 20:56:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Repository
public interface ReactiveVerificationRepository extends ReactiveCrudRepository<VerificationRequest, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据请求编号查找核验请求
     *
     * @param requestNo 请求编号
     * @return 核验请求的Mono包装
     */
    Mono<VerificationRequest> findByRequestNo(String requestNo);

    /**
     * 根据请求类型查找核验请求列表
     *
     * @param requestType 请求类型
     * @return 核验请求列表的Flux包装
     */
    Flux<VerificationRequest> findByRequestType(String requestType);

    /**
     * 根据请求人ID查找核验请求列表
     *
     * @param requesterId 请求人ID
     * @return 核验请求列表的Flux包装
     */
    Flux<VerificationRequest> findByRequesterId(Long requesterId);

    /**
     * 根据处理人ID查找核验请求列表
     *
     * @param processorId 处理人ID
     * @return 核验请求列表的Flux包装
     */
    Flux<VerificationRequest> findByProcessorId(Long processorId);

    /**
     * 根据状态查找核验请求列表
     *
     * @param status 处理状态
     * @return 核验请求列表的Flux包装
     */
    Flux<VerificationRequest> findByStatus(Integer status);

    /**
     * 根据优先级查找核验请求列表
     *
     * @param priority 优先级
     * @return 核验请求列表的Flux包装
     */
    Flux<VerificationRequest> findByPriority(Integer priority);

    /**
     * 根据标题模糊查找核验请求
     *
     * @param title 标题关键词
     * @return 核验请求列表的Flux包装
     */
    Flux<VerificationRequest> findByTitleContainingIgnoreCase(String title);

    // ==================== 存在性检查方法 ====================

    /**
     * 检查请求编号是否存在
     * 修复Boolean到Integer转换错误：使用CASE WHEN语句确保返回明确的布尔值
     *
     * @param requestNo 请求编号
     * @return 是否存在的Mono包装
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM biz_verification_request WHERE request_no = :requestNo")
    Mono<Boolean> existsByRequestNo(String requestNo);

    /**
     * 检查用户是否有待处理的验证请求
     * 修复Boolean到Integer转换错误：使用CASE WHEN语句确保返回明确的布尔值
     *
     * @param requesterId 请求者ID
     * @return 是否有待处理请求的Mono包装
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM biz_verification_request WHERE requester_id = :requesterId AND status = 0 AND deleted = 0")
    Mono<Boolean> hasPendingRequest(@Param("requesterId") Long requesterId);

    // ==================== 统计查询方法 ====================

    /**
     * 统计指定状态的核验请求数量
     *
     * @param status 处理状态
     * @return 请求数量的Mono包装
     */
    Mono<Long> countByStatus(Integer status);

    /**
     * 统计指定请求类型的核验请求数量
     *
     * @param requestType 请求类型
     * @return 请求数量的Mono包装
     */
    Mono<Long> countByRequestType(String requestType);

    /**
     * 统计指定优先级的核验请求数量
     *
     * @param priority 优先级
     * @return 请求数量的Mono包装
     */
    Mono<Long> countByPriority(Integer priority);

    /**
     * 统计待处理的核验请求数量
     *
     * @return 待处理请求数量的Mono包装
     */
    @Query("SELECT COUNT(*) FROM biz_verification_request WHERE status = 0 AND deleted = 0")
    Mono<Long> countPendingRequests();

    /**
     * 统计超时的核验请求数量
     *
     * @return 超时请求数量的Mono包装
     */
    @Query("SELECT COUNT(*) FROM biz_verification_request WHERE expected_completion_time < CURRENT_TIMESTAMP AND status IN (0, 1) AND deleted = 0")
    Mono<Long> countOverdueRequests();

    /**
     * 统计高优先级的核验请求数量
     *
     * @return 高优先级请求数量的Mono包装
     */
    @Query("SELECT COUNT(*) FROM biz_verification_request WHERE priority >= 3 AND status IN (0, 1) AND deleted = 0")
    Mono<Long> countHighPriorityRequests();

    // ==================== 时间范围查询方法 ====================

    /**
     * 查找指定时间范围内创建的核验请求
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 核验请求列表的Flux包装
     */
    @Query("SELECT * FROM biz_verification_request WHERE created_at >= :startTime AND created_at <= :endTime AND deleted = 0 ORDER BY created_at DESC")
    Flux<VerificationRequest> findByCreatedDateBetween(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 查找指定时间范围内完成的核验请求
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 核验请求列表的Flux包装
     */
    @Query("SELECT * FROM biz_verification_request WHERE actual_completion_time >= :startTime AND actual_completion_time <= :endTime AND status = 2 AND deleted = 0 ORDER BY actual_completion_time DESC")
    Flux<VerificationRequest> findCompletedBetween(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 查找即将超时的核验请求
     *
     * @param beforeTime 超时时间阈值
     * @return 核验请求列表的Flux包装
     */
    @Query("SELECT * FROM biz_verification_request WHERE expected_completion_time <= :beforeTime AND status IN (0, 1) AND deleted = 0 ORDER BY expected_completion_time ASC")
    Flux<VerificationRequest> findRequestsExpiringBefore(@Param("beforeTime") LocalDateTime beforeTime);

    // ==================== 复杂查询方法 ====================

    /**
     * 根据多个条件查找核验请求
     *
     * @param requestType 请求类型（可选）
     * @param status 处理状态（可选）
     * @param priority 优先级（可选）
     * @param requesterId 请求人ID（可选）
     * @param keyword 关键词（标题或内容，可选）
     * @return 核验请求列表的Flux包装
     */
    @Query("SELECT * FROM biz_verification_request WHERE " +
           "(:requestType IS NULL OR request_type = :requestType) AND " +
           "(:status IS NULL OR status = :status) AND " +
           "(:priority IS NULL OR priority = :priority) AND " +
           "(:requesterId IS NULL OR requester_id = :requesterId) AND " +
           "(:keyword IS NULL OR title LIKE CONCAT('%', :keyword, '%') OR content LIKE CONCAT('%', :keyword, '%')) AND " +
           "deleted = 0 ORDER BY created_at DESC")
    Flux<VerificationRequest> findByConditions(@Param("requestType") String requestType,
                                              @Param("status") Integer status,
                                              @Param("priority") Integer priority,
                                              @Param("requesterId") Long requesterId,
                                              @Param("keyword") String keyword);

    /**
     * 查找需要紧急处理的核验请求
     *
     * @return 紧急处理请求列表的Flux包装
     */
    @Query("SELECT * FROM biz_verification_request WHERE " +
           "(priority = 4 OR expected_completion_time <= CURRENT_TIMESTAMP) AND " +
           "status IN (0, 1) AND " +
           "deleted = 0 ORDER BY priority DESC, expected_completion_time ASC")
    Flux<VerificationRequest> findUrgentRequests();

    /**
     * 查找指定用户的活跃请求
     *
     * @param requesterId 请求人ID
     * @return 活跃请求列表的Flux包装
     */
    @Query("SELECT * FROM biz_verification_request WHERE " +
           "requester_id = :requesterId AND " +
           "status IN (0, 1) AND " +
           "deleted = 0 ORDER BY created_at DESC")
    Flux<VerificationRequest> findActiveRequestsByRequesterId(@Param("requesterId") Long requesterId);

    // ==================== 批量操作方法 ====================

    /**
     * 批量更新核验请求状态
     *
     * @param requestIds 请求ID列表
     * @param status 新状态
     * @return 更新行数的Mono包装
     */
    @Query("UPDATE biz_verification_request SET status = :status, last_modified_date = CURRENT_TIMESTAMP WHERE id IN (:requestIds) AND deleted = 0")
    Mono<Integer> updateStatusByIds(@Param("requestIds") Iterable<Long> requestIds,
                                   @Param("status") Integer status);

    /**
     * 批量分配处理人
     *
     * @param requestIds 请求ID列表
     * @param processorId 处理人ID
     * @param processorName 处理人姓名
     * @return 更新记录数的Mono包装
     */
    @Query("UPDATE biz_verification_request SET processor_id = :processorId, processor_name = :processorName, status = 1, actual_start_time = CURRENT_TIMESTAMP, last_modified_date = CURRENT_TIMESTAMP, version = version + 1 WHERE id IN (:requestIds) AND status = 0 AND deleted = 0")
    Mono<Integer> assignProcessorByIds(@Param("requestIds") Iterable<Long> requestIds,
                                      @Param("processorId") Long processorId,
                                      @Param("processorName") String processorName);

    /**
     * 批量更新状态
     *
     * @param requestIds 请求ID列表
     * @param status 新状态
     * @return 更新记录数的Mono包装
     */
    @Query("UPDATE biz_verification_request SET status = :status, last_modified_date = CURRENT_TIMESTAMP, version = version + 1 WHERE id IN (:requestIds) AND deleted = 0")
    Mono<Integer> batchUpdateStatus(@Param("requestIds") List<Long> requestIds,
                                   @Param("status") Integer status);

    // ==================== 核验结果相关查询方法 ====================

    /**
     * 根据请求ID查找核验结果
     *
     * @param requestId 请求ID
     * @return 核验结果的Mono包装
     */
    @Query("SELECT * FROM biz_verification_result WHERE request_id = :requestId AND deleted = 0")
    Mono<VerificationResult> findResultByRequestId(@Param("requestId") Long requestId);

    /**
     * 根据结果编号查找核验结果
     *
     * @param resultNo 结果编号
     * @return 核验结果的Mono包装
     */
    @Query("SELECT * FROM biz_verification_result WHERE result_no = :resultNo AND deleted = 0")
    Mono<VerificationResult> findResultByResultNo(@Param("resultNo") String resultNo);

    /**
     * 根据核验人员ID查找核验结果列表
     *
     * @param verifierId 核验人员ID
     * @return 核验结果列表的Flux包装
     */
    @Query("SELECT * FROM biz_verification_result WHERE verifier_id = :verifierId AND deleted = 0 ORDER BY created_at DESC")
    Flux<VerificationResult> findResultsByVerifierId(@Param("verifierId") Long verifierId);

    /**
     * 根据结果状态查找核验结果列表
     *
     * @param resultStatus 结果状态
     * @return 核验结果列表的Flux包装
     */
    @Query("SELECT * FROM biz_verification_result WHERE result_status = :resultStatus AND deleted = 0 ORDER BY created_at DESC")
    Flux<VerificationResult> findResultsByStatus(@Param("resultStatus") Integer resultStatus);

    /**
     * 根据风险等级查找核验结果列表
     *
     * @param riskLevel 风险等级
     * @return 核验结果列表的Flux包装
     */
    @Query("SELECT * FROM biz_verification_result WHERE risk_level = :riskLevel AND deleted = 0 ORDER BY created_at DESC")
    Flux<VerificationResult> findResultsByRiskLevel(@Param("riskLevel") Integer riskLevel);

    /**
     * 查找需要复核的核验结果
     *
     * @return 需要复核的结果列表的Flux包装
     */
    @Query("SELECT * FROM biz_verification_result WHERE requires_recheck = true AND deleted = 0 ORDER BY created_at DESC")
    Flux<VerificationResult> findResultsRequiringRecheck();

    /**
     * 查找高风险的核验结果
     *
     * @return 高风险结果列表的Flux包装
     */
    @Query("SELECT * FROM biz_verification_result WHERE risk_level >= 3 AND deleted = 0 ORDER BY risk_level DESC, created_at DESC")
    Flux<VerificationResult> findHighRiskResults();

    /**
     * 查找即将过期的核验结果
     *
     * @param beforeTime 过期时间阈值
     * @return 即将过期的结果列表的Flux包装
     */
    @Query("SELECT * FROM biz_verification_result WHERE validity_period IS NOT NULL AND validity_period <= :beforeTime AND deleted = 0 ORDER BY validity_period ASC")
    Flux<VerificationResult> findResultsExpiringBefore(@Param("beforeTime") LocalDateTime beforeTime);

    // ==================== 统计分析方法 ====================

    /**
     * 统计指定时间范围内的核验请求数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 请求数量的Mono包装
     */
    @Query("SELECT COUNT(*) FROM biz_verification_request WHERE created_at >= :startTime AND created_at <= :endTime AND deleted = 0")
    Mono<Long> countRequestsBetween(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内完成的核验请求数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 完成请求数量的Mono包装
     */
    @Query("SELECT COUNT(*) FROM biz_verification_request WHERE actual_completion_time >= :startTime AND actual_completion_time <= :endTime AND status = 2 AND deleted = 0")
    Mono<Long> countCompletedRequestsBetween(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 计算指定时间范围内的平均处理时间（分钟）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均处理时间的Mono包装
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, actual_start_time, actual_completion_time)) FROM biz_verification_request WHERE actual_completion_time >= :startTime AND actual_completion_time <= :endTime AND status = 2 AND actual_start_time IS NOT NULL AND deleted = 0")
    Mono<BigDecimal> calculateAverageProcessingTime(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各状态的核验请求数量
     *
     * @return 状态统计结果的Flux包装（status, count）
     */
    @Query("SELECT status, COUNT(*) as count FROM biz_verification_request WHERE deleted = 0 GROUP BY status ORDER BY status")
    Flux<Object[]> countRequestsByStatus();

    /**
     * 统计各优先级的核验请求数量
     *
     * @return 优先级统计结果的Flux包装（priority, count）
     */
    @Query("SELECT priority, COUNT(*) as count FROM biz_verification_request WHERE deleted = 0 GROUP BY priority ORDER BY priority")
    Flux<Object[]> countRequestsByPriority();

    /**
     * 统计各请求类型的核验请求数量
     *
     * @return 请求类型统计结果的Flux包装（request_type, count）
     */
    @Query("SELECT request_type, COUNT(*) as count FROM biz_verification_request WHERE deleted = 0 GROUP BY request_type ORDER BY count DESC")
    Flux<Object[]> countRequestsByType();

    /**
     * 统计核验结果的通过率
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 通过率的Mono包装（百分比）
     */
    @Query("SELECT (COUNT(CASE WHEN result_status = 1 THEN 1 END) * 100.0 / COUNT(*)) as pass_rate FROM biz_verification_result WHERE created_at >= :startTime AND created_at <= :endTime AND deleted = 0")
    Mono<BigDecimal> calculatePassRate(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各风险等级的核验结果数量
     *
     * @return 风险等级统计结果的Flux包装（risk_level, count）
     */
    @Query("SELECT risk_level, COUNT(*) as count FROM biz_verification_result WHERE deleted = 0 GROUP BY risk_level ORDER BY risk_level")
    Flux<Object[]> countResultsByRiskLevel();
}


