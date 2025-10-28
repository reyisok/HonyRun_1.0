package com.honyrun.repository.r2dbc;

import com.honyrun.model.entity.business.BusinessFunction3;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 响应式业务功能3仓库接口
 *
 * 提供业务功能3实体的响应式数据访问操作，基于R2DBC实现非阻塞数据库访问。
 * 包含基础CRUD操作、条件查询、统计分析等功能。
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  22:50:00
 * @modified 2025-07-01 22:50:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Repository
public interface ReactiveBusinessFunction3Repository extends R2dbcRepository<BusinessFunction3, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据业务编号查询业务功能3
     *
     * @param businessNo 业务编号
     * @return 业务功能3对象
     */
    Mono<BusinessFunction3> findByBusinessNo(String businessNo);

    /**
     * 根据业务名称查询业务功能3
     *
     * @param businessName 业务名称
     * @return 业务功能3对象
     */
    Mono<BusinessFunction3> findByBusinessName(String businessName);

    /**
     * 根据业务类型查询业务功能3列表
     *
     * @param businessType 业务类型
     * @return 业务功能3列表
     */
    Flux<BusinessFunction3> findByBusinessType(String businessType);

    /**
     * 根据状态查询业务功能3列表
     *
     * @param status 业务状态
     * @return 业务功能3列表
     */
    Flux<BusinessFunction3> findByStatus(BusinessFunction3.BusinessStatus status);

    /**
     * 根据重要性查询业务功能3列表
     *
     * @param importance 重要性
     * @return 业务功能3列表
     */
    Flux<BusinessFunction3> findByImportance(BusinessFunction3.Importance importance);

    /**
     * 根据执行模式查询业务功能3列表
     *
     * @param executionMode 执行模式
     * @return 业务功能3列表
     */
    Flux<BusinessFunction3> findByExecutionMode(BusinessFunction3.ExecutionMode executionMode);

    /**
     * 根据业务分组查询业务功能3列表
     *
     * @param businessGroup 业务分组
     * @return 业务功能3列表
     */
    Flux<BusinessFunction3> findByBusinessGroup(String businessGroup);

    /**
     * 根据负责人查询业务功能3列表
     *
     * @param ownerId 负责人ID
     * @return 业务功能3列表
     */
    Flux<BusinessFunction3> findByOwnerId(Long ownerId);

    // ==================== 模糊查询方法 ====================

    /**
     * 根据业务名称模糊查询
     *
     * @param businessName 业务名称关键词
     * @return 业务功能3列表
     */
    Flux<BusinessFunction3> findByBusinessNameContainingIgnoreCase(String businessName);

    /**
     * 根据描述模糊查询
     *
     * @param description 描述关键词
     * @return 业务功能3列表
     */
    Flux<BusinessFunction3> findByDescriptionContainingIgnoreCase(String description);

    /**
     * 根据内容模糊查询
     *
     * @param content 内容关键词
     * @return 业务功能3列表
     */
    Flux<BusinessFunction3> findByContentContainingIgnoreCase(String content);

    // ==================== 时间范围查询 ====================

    /**
     * 查询指定时间范围内创建的业务功能3
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 业务功能3列表
     */
    Flux<BusinessFunction3> findByCreatedDateBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询指定时间范围内修改的业务功能3
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 业务功能3列表
     */
    Flux<BusinessFunction3> findByLastModifiedDateBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询指定时间范围内开始的业务功能3
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 业务功能3列表
     */
    Flux<BusinessFunction3> findByActualStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询指定时间范围内结束的业务功能3
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 业务功能3列表
     */
    Flux<BusinessFunction3> findByActualEndTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 复合条件查询 ====================

    /**
     * 根据多个条件查询业务功能3
     *
     * @param businessType 业务类型
     * @param status 业务状态
     * @param importance 重要性
     * @param businessGroup 业务分组
     * @param keyword 关键词
     * @return 业务功能3列表
     */
    @Query("SELECT * FROM biz_business_function3 WHERE " +
           "(:businessType IS NULL OR business_type = :businessType) AND " +
           "(:status IS NULL OR status = :status) AND " +
           "(:importance IS NULL OR importance = :importance) AND " +
           "(:businessGroup IS NULL OR business_group = :businessGroup) AND " +
           "(:keyword IS NULL OR business_name ILIKE CONCAT('%', :keyword, '%') OR " +
           "description ILIKE CONCAT('%', :keyword, '%') OR " +
           "content ILIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY created_at DESC")
    Flux<BusinessFunction3> findByConditions(@Param("businessType") String businessType,
                                            @Param("status") String status,
                                            @Param("importance") String importance,
                                            @Param("businessGroup") String businessGroup,
                                            @Param("keyword") String keyword);

    // ==================== 统计查询方法 ====================

    /**
     * 根据状态统计业务功能3数量
     *
     * @param status 业务状态
     * @return 数量
     */
    Mono<Long> countByStatus(BusinessFunction3.BusinessStatus status);

    /**
     * 根据重要性统计业务功能3数量
     *
     * @param importance 重要性
     * @return 数量
     */
    Mono<Long> countByImportance(BusinessFunction3.Importance importance);

    /**
     * 根据执行模式统计业务功能3数量
     *
     * @param executionMode 执行模式
     * @return 数量
     */
    Mono<Long> countByExecutionMode(BusinessFunction3.ExecutionMode executionMode);

    /**
     * 根据业务分组统计业务功能3数量
     *
     * @param businessGroup 业务分组
     * @return 数量
     */
    Mono<Long> countByBusinessGroup(String businessGroup);

    /**
     * 统计活跃状态的业务功能3数量
     *
     * @return 活跃业务数量
     */
    @Query("SELECT COUNT(*) FROM biz_business_function3 WHERE status = 'ACTIVE'")
    Mono<Long> countActiveBusinessFunction3();

    /**
     * 统计已完成状态的业务功能3数量
     *
     * @return 已完成业务数量
     */
    @Query("SELECT COUNT(*) FROM biz_business_function3 WHERE status = 'COMPLETED'")
    Mono<Long> countCompletedBusinessFunction3();

    /**
     * 统计指定时间范围内创建的业务功能3数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 数量
     */
    Mono<Long> countByCreatedDateBetween(LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 批量操作方法 ====================

    /**
     * 批量更新状态
     *
     * @param ids 业务功能3ID列表
     * @param status 新状态
     * @return 更新的记录数
     */
    @Query("UPDATE biz_business_function3 SET status = :status, last_modified_date = CURRENT_TIMESTAMP " +
           "WHERE id IN (:ids)")
    Mono<Integer> batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    /**
     * 批量删除业务功能3
     *
     * @param ids 业务功能3ID列表
     * @return 删除的记录数
     */
    @Query("DELETE FROM biz_business_function3 WHERE id IN (:ids)")
    Mono<Integer> batchDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 批量更新进度
     *
     * @param ids 业务功能3ID列表
     * @param progress 进度百分比
     * @return 更新的记录数
     */
    @Query("UPDATE biz_business_function3 SET progress = :progress, last_modified_date = CURRENT_TIMESTAMP " +
           "WHERE id IN (:ids)")
    Mono<Integer> batchUpdateProgress(@Param("ids") List<Long> ids, @Param("progress") Integer progress);

    // ==================== 特殊查询方法 ====================

    /**
     * 查询即将到期的业务功能3
     *
     * @param targetDate 目标日期
     * @return 即将到期的业务功能3列表
     */
    @Query("SELECT * FROM biz_business_function3 WHERE estimated_end_time IS NOT NULL AND " +
           "estimated_end_time <= :targetDate AND status IN ('ACTIVE', 'PAUSED') ORDER BY estimated_end_time ASC")
    Flux<BusinessFunction3> findExpiringBusiness(@Param("targetDate") LocalDateTime targetDate);

    /**
     * 查询超期未完成的业务功能3
     *
     * @return 超期未完成的业务功能3列表
     */
    @Query("SELECT * FROM biz_business_function3 WHERE estimated_end_time IS NOT NULL AND " +
           "estimated_end_time < CURRENT_TIMESTAMP AND status NOT IN ('COMPLETED', 'CANCELLED') " +
           "ORDER BY estimated_end_time ASC")
    Flux<BusinessFunction3> findOverdueBusiness();

    /**
     * 查询高重要性的业务功能3
     *
     * @return 高重要性业务功能3列表
     */
    @Query("SELECT * FROM biz_business_function3 WHERE importance = 'HIGH' AND status = 'ACTIVE' " +
           "ORDER BY created_at DESC")
    Flux<BusinessFunction3> findHighImportanceBusiness();

    /**
     * 查询需要关注的业务功能3
     *
     * @return 需要关注的业务功能3列表
     */
    @Query("SELECT * FROM biz_business_function3 WHERE " +
           "(importance = 'HIGH' OR status = 'PAUSED' OR " +
           "(estimated_end_time IS NOT NULL AND estimated_end_time <= CURRENT_TIMESTAMP + INTERVAL '3 days')) AND " +
           "status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY importance DESC, created_at DESC")
    Flux<BusinessFunction3> findBusinessRequiringAttention();

    /**
     * 查询长时间未更新的业务功能3
     *
     * @param targetDate 目标日期
     * @return 长时间未更新的业务功能3列表
     */
    @Query("SELECT * FROM biz_business_function3 WHERE last_modified_date <= :targetDate AND " +
           "status = 'ACTIVE' ORDER BY last_modified_date ASC")
    Flux<BusinessFunction3> findStagnantBusiness(@Param("targetDate") LocalDateTime targetDate);

    /**
     * 查询进度为0的业务功能3
     *
     * @return 进度为0的业务功能3列表
     */
    @Query("SELECT * FROM biz_business_function3 WHERE (progress IS NULL OR progress = 0) AND " +
           "status = 'ACTIVE' ORDER BY created_at ASC")
    Flux<BusinessFunction3> findNotStartedBusiness();

    /**
     * 查询进度接近完成的业务功能3
     *
     * @param minProgress 最小进度
     * @return 进度接近完成的业务功能3列表
     */
    @Query("SELECT * FROM biz_business_function3 WHERE progress >= :minProgress AND " +
           "status = 'ACTIVE' ORDER BY progress DESC")
    Flux<BusinessFunction3> findNearCompletionBusiness(@Param("minProgress") Integer minProgress);

    // ==================== 存在性检查方法 ====================

    /**
     * 检查业务编号是否存在
     * 修复Boolean到Integer转换错误：使用CASE WHEN语句确保返回明确的布尔值
     *
     * @param businessNo 业务编号
     * @return 是否存在
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM business_function3 WHERE business_no = :businessNo")
    Mono<Boolean> existsByBusinessNo(String businessNo);

    /**
     * 检查业务名称是否存在
     * 修复Boolean到Integer转换错误：使用CASE WHEN语句确保返回明确的布尔值
     *
     * @param businessName 业务名称
     * @return 是否存在
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM business_function3 WHERE business_name = :businessName")
    Mono<Boolean> existsByBusinessName(String businessName);

    /**
     * 检查指定ID的业务是否已完成
     *
     * @param id 业务功能3ID
     * @return 是否已完成
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM biz_business_function3 " +
           "WHERE id = :id AND status = 'COMPLETED'")
    Mono<Boolean> isBusinessCompleted(@Param("id") Long id);

    /**
     * 检查指定ID的业务是否已取消
     *
     * @param id 业务功能3ID
     * @return 是否已取消
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM biz_business_function3 " +
           "WHERE id = :id AND status = 'CANCELLED'")
    Mono<Boolean> isBusinessCancelled(@Param("id") Long id);

    /**
     * 检查指定ID的业务是否活跃
     *
     * @param id 业务功能3ID
     * @return 是否活跃
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM biz_business_function3 " +
           "WHERE id = :id AND status = 'ACTIVE'")
    Mono<Boolean> isBusinessActive(@Param("id") Long id);

    // ==================== 统计分析方法 ====================

    /**
     * 获取平均完成时间（小时）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 平均完成时间
     */
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (actual_end_time - actual_start_time))/3600) " +
           "FROM biz_business_function3 WHERE status = 'COMPLETED' AND " +
           "actual_start_time IS NOT NULL AND actual_end_time IS NOT NULL AND " +
           "created_at BETWEEN :startDate AND :endDate")
    Mono<Double> getAverageCompletionTimeInHours(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * 获取完成率
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 完成率
     */
    @Query("SELECT CASE WHEN COUNT(*) = 0 THEN 0 ELSE " +
           "CAST(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*) * 100 END " +
           "FROM biz_business_function3 WHERE created_at BETWEEN :startDate AND :endDate")
    Mono<Double> getCompletionRate(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 获取平均进度
     *
     * @return 平均进度
     */
    @Query("SELECT AVG(COALESCE(progress, 0)) FROM biz_business_function3 WHERE status = 'ACTIVE'")
    Mono<Double> getAverageProgress();
}


