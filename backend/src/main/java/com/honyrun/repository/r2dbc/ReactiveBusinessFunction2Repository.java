package com.honyrun.repository.r2dbc;

import com.honyrun.model.entity.business.BusinessFunction2;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 响应式业务功能2仓库接口
 *
 * 提供业务功能2实体的响应式数据访问操作，基于R2DBC实现非阻塞数据库访问。
 * 包含基础CRUD操作、条件查询、统计分析等功能。
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  22:10:00
 * @modified 2025-07-01 22:10:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Repository
public interface ReactiveBusinessFunction2Repository extends R2dbcRepository<BusinessFunction2, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据业务编号查询业务功能2
     *
     * @param businessNo 业务编号
     * @return 业务功能2对象
     */
    Mono<BusinessFunction2> findByBusinessNo(String businessNo);

    /**
     * 根据业务名称查询业务功能2
     *
     * @param businessName 业务名称
     * @return 业务功能2对象
     */
    Mono<BusinessFunction2> findByBusinessName(String businessName);

    /**
     * 根据业务类型查询业务功能2列表
     *
     * @param businessType 业务类型
     * @return 业务功能2列表
     */
    Flux<BusinessFunction2> findByBusinessType(String businessType);

    /**
     * 根据状态查询业务功能2列表
     *
     * @param status 业务状态
     * @return 业务功能2列表
     */
    Flux<BusinessFunction2> findByStatus(BusinessFunction2.BusinessStatus status);

    /**
     * 根据优先级查询业务功能2列表
     *
     * @param priority 优先级
     * @return 业务功能2列表
     */
    Flux<BusinessFunction2> findByPriority(BusinessFunction2.Priority priority);

    /**
     * 根据分类查询业务功能2列表
     *
     * @param category 分类
     * @return 业务功能2列表
     */
    Flux<BusinessFunction2> findByCategory(String category);

    /**
     * 根据标签查询业务功能2列表
     *
     * @param tags 标签
     * @return 业务功能2列表
     */
    Flux<BusinessFunction2> findByTagsContaining(String tags);

    // ==================== 模糊查询方法 ====================

    /**
     * 根据业务名称模糊查询
     *
     * @param businessName 业务名称关键词
     * @return 业务功能2列表
     */
    Flux<BusinessFunction2> findByBusinessNameContainingIgnoreCase(String businessName);

    /**
     * 根据描述模糊查询
     *
     * @param description 描述关键词
     * @return 业务功能2列表
     */
    Flux<BusinessFunction2> findByDescriptionContainingIgnoreCase(String description);

    // ==================== 时间范围查询 ====================

    /**
     * 查询指定时间范围内创建的业务功能2
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 业务功能2列表
     */
    Flux<BusinessFunction2> findByCreatedDateBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询指定时间范围内修改的业务功能2
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 业务功能2列表
     */
    Flux<BusinessFunction2> findByLastModifiedDateBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询指定时间范围内开始的业务功能2
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 业务功能2列表
     */
    Flux<BusinessFunction2> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询指定时间范围内结束的业务功能2
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 业务功能2列表
     */
    Flux<BusinessFunction2> findByEndTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 复合条件查询 ====================

    /**
     * 根据多个条件查询业务功能2
     *
     * @param businessType 业务类型
     * @param status 业务状态
     * @param priority 优先级
     * @param category 分类
     * @param keyword 关键词
     * @return 业务功能2列表
     */
    @Query("SELECT * FROM biz_business_function2 WHERE " +
           "(:businessType IS NULL OR business_type = :businessType) AND " +
           "(:status IS NULL OR status = :status) AND " +
           "(:priority IS NULL OR priority = :priority) AND " +
           "(:category IS NULL OR category = :category) AND " +
           "(:keyword IS NULL OR business_name ILIKE CONCAT('%', :keyword, '%') OR " +
           "description ILIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY created_at DESC")
    Flux<BusinessFunction2> findByConditions(@Param("businessType") String businessType,
                                            @Param("status") String status,
                                            @Param("priority") String priority,
                                            @Param("category") String category,
                                            @Param("keyword") String keyword);

    // ==================== 统计查询方法 ====================

    /**
     * 根据状态统计业务功能2数量
     *
     * @param status 业务状态
     * @return 数量
     */
    Mono<Long> countByStatus(BusinessFunction2.BusinessStatus status);

    /**
     * 根据优先级统计业务功能2数量
     *
     * @param priority 优先级
     * @return 数量
     */
    Mono<Long> countByPriority(BusinessFunction2.Priority priority);

    /**
     * 根据分类统计业务功能2数量
     *
     * @param category 分类
     * @return 数量
     */
    Mono<Long> countByCategory(String category);

    /**
     * 统计活跃状态的业务功能2数量
     *
     * @return 活跃业务数量
     */
    @Query("SELECT COUNT(*) FROM biz_business_function2 WHERE status = 'ACTIVE'")
    Mono<Long> countActiveBusinessFunction2();

    /**
     * 统计指定时间范围内创建的业务功能2数量
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
     * @param ids 业务功能2ID列表
     * @param status 新状态
     * @return 更新的记录数
     */
    @Query("UPDATE biz_business_function2 SET status = :status, last_modified_date = CURRENT_TIMESTAMP " +
           "WHERE id IN (:ids)")
    Mono<Integer> batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    /**
     * 批量删除业务功能2
     *
     * @param ids 业务功能2ID列表
     * @return 删除的记录数
     */
    @Query("DELETE FROM biz_business_function2 WHERE id IN (:ids)")
    Mono<Integer> batchDeleteByIds(@Param("ids") List<Long> ids);

    // ==================== 特殊查询方法 ====================

    /**
     * 查询即将到期的业务功能2
     *
     * @param targetDate 目标日期
     * @return 即将到期的业务功能2列表
     */
    @Query("SELECT * FROM biz_business_function2 WHERE end_time IS NOT NULL AND " +
           "end_time <= :targetDate AND status = 'ACTIVE' ORDER BY end_time ASC")
    Flux<BusinessFunction2> findExpiringBusiness(@Param("targetDate") LocalDateTime targetDate);

    /**
     * 查询长时间未处理的业务功能2
     *
     * @param targetDate 目标日期
     * @return 长时间未处理的业务功能2列表
     */
    @Query("SELECT * FROM biz_business_function2 WHERE last_process_time IS NULL OR " +
           "last_process_time <= :targetDate ORDER BY created_at ASC")
    Flux<BusinessFunction2> findUnprocessedBusiness(@Param("targetDate") LocalDateTime targetDate);

    /**
     * 查询高优先级的业务功能2
     *
     * @return 高优先级业务功能2列表
     */
    @Query("SELECT * FROM biz_business_function2 WHERE priority = 'HIGH' AND status = 'ACTIVE' " +
           "ORDER BY created_at DESC")
    Flux<BusinessFunction2> findHighPriorityBusiness();

    /**
     * 查询需要关注的业务功能2
     *
     * @return 需要关注的业务功能2列表
     */
    @Query("SELECT * FROM biz_business_function2 WHERE " +
           "(priority = 'HIGH' OR status = 'SUSPENDED') AND " +
           "end_time > CURRENT_TIMESTAMP ORDER BY priority DESC, created_at DESC")
    Flux<BusinessFunction2> findBusinessRequiringAttention();

    // ==================== 存在性检查方法 ====================

    /**
     * 检查业务编号是否存在
     * 修复Boolean到Integer转换错误：使用CASE WHEN语句确保返回明确的布尔值
     *
     * @param businessNo 业务编号
     * @return 是否存在
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM business_function2 WHERE business_no = :businessNo")
    Mono<Boolean> existsByBusinessNo(String businessNo);

    /**
     * 检查业务名称是否存在
     * 修复Boolean到Integer转换错误：使用CASE WHEN语句确保返回明确的布尔值
     *
     * @param businessName 业务名称
     * @return 是否存在
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM business_function2 WHERE business_name = :businessName")
    Mono<Boolean> existsByBusinessName(String businessName);

    /**
     * 检查指定ID的业务是否有效（活跃状态且未过期）
     *
     * @param id 业务功能2ID
     * @return 是否有效
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM biz_business_function2 " +
           "WHERE id = :id AND status = 'ACTIVE' AND " +
           "(end_time IS NULL OR end_time > CURRENT_TIMESTAMP)")
    Mono<Boolean> isBusinessValid(@Param("id") Long id);
}


