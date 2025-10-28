package com.honyrun.service.reactive;

import com.honyrun.model.entity.business.BusinessFunction2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 响应式业务功能2服务接口
 *
 * 定义业务功能2相关的业务方法，包括预留业务数据的CRUD操作、业务流程处理、
 * 业务规则配置和执行、业务报表生成等功能。
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理。
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  22:00:00
 * @modified 2025-07-01 22:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveBusinessFunction2Service {

    // ==================== 基础CRUD操作 ====================

    /**
     * 创建业务功能2数据
     *
     * @param businessFunction2 业务功能2对象
     * @return 创建成功的业务功能2对象
     */
    Mono<BusinessFunction2> createBusinessFunction2(BusinessFunction2 businessFunction2);

    /**
     * 根据ID查询业务功能2数据
     *
     * @param id 业务功能2ID
     * @return 业务功能2对象
     */
    Mono<BusinessFunction2> getBusinessFunction2ById(Long id);

    /**
     * 根据业务编号查询业务功能2数据
     *
     * @param businessNo 业务编号
     * @return 业务功能2对象
     */
    Mono<BusinessFunction2> getBusinessFunction2ByNo(String businessNo);

    /**
     * 更新业务功能2数据
     *
     * @param id 业务功能2ID
     * @param businessFunction2 更新的业务功能2对象
     * @return 更新后的业务功能2对象
     */
    Mono<BusinessFunction2> updateBusinessFunction2(Long id, BusinessFunction2 businessFunction2);

    /**
     * 删除业务功能2数据
     *
     * @param id 业务功能2ID
     * @return 删除操作结果
     */
    Mono<Boolean> deleteBusinessFunction2(Long id);

    /**
     * 批量删除业务功能2数据
     *
     * @param ids 业务功能2ID列表
     * @return 删除的记录数
     */
    Mono<Long> batchDeleteBusinessFunction2(List<Long> ids);

    // ==================== 查询方法 ====================

    /**
     * 分页查询所有业务功能2数据
     *
     * @param page 页码
     * @param size 每页大小
     * @return 业务功能2数据列表
     */
    Flux<BusinessFunction2> getAllBusinessFunction2(int page, int size);

    /**
     * 根据业务类型查询业务功能2数据
     *
     * @param businessType 业务类型
     * @return 业务功能2数据列表
     */
    Flux<BusinessFunction2> getBusinessFunction2ByType(String businessType);

    /**
     * 根据状态查询业务功能2数据
     *
     * @param status 业务状态
     * @return 业务功能2数据列表
     */
    Flux<BusinessFunction2> getBusinessFunction2ByStatus(BusinessFunction2.BusinessStatus status);

    /**
     * 根据优先级查询业务功能2数据
     *
     * @param priority 优先级
     * @return 业务功能2数据列表
     */
    Flux<BusinessFunction2> getBusinessFunction2ByPriority(BusinessFunction2.Priority priority);

    /**
     * 根据分类查询业务功能2数据
     *
     * @param category 分类
     * @return 业务功能2数据列表
     */
    Flux<BusinessFunction2> getBusinessFunction2ByCategory(String category);

    /**
     * 搜索业务功能2数据
     *
     * @param keyword 搜索关键词
     * @return 业务功能2数据列表
     */
    Flux<BusinessFunction2> searchBusinessFunction2(String keyword);

    /**
     * 根据条件查询业务功能2数据
     *
     * @param businessType 业务类型
     * @param status 业务状态
     * @param priority 优先级
     * @param category 分类
     * @param keyword 关键词
     * @return 业务功能2数据列表
     */
    Flux<BusinessFunction2> getBusinessFunction2ByConditions(String businessType,
                                                            BusinessFunction2.BusinessStatus status,
                                                            BusinessFunction2.Priority priority,
                                                            String category,
                                                            String keyword);

    // ==================== 业务状态管理 ====================

    /**
     * 激活业务功能2
     *
     * @param id 业务功能2ID
     * @return 操作结果
     */
    Mono<Boolean> activateBusinessFunction2(Long id);

    /**
     * 停用业务功能2
     *
     * @param id 业务功能2ID
     * @return 操作结果
     */
    Mono<Boolean> deactivateBusinessFunction2(Long id);

    /**
     * 暂停业务功能2
     *
     * @param id 业务功能2ID
     * @return 操作结果
     */
    Mono<Boolean> suspendBusinessFunction2(Long id);

    /**
     * 归档业务功能2
     *
     * @param id 业务功能2ID
     * @return 操作结果
     */
    Mono<Boolean> archiveBusinessFunction2(Long id);

    /**
     * 批量更新状态
     *
     * @param ids 业务功能2ID列表
     * @param status 新状态
     * @return 更新的记录数
     */
    Mono<Long> batchUpdateStatus(List<Long> ids, BusinessFunction2.BusinessStatus status);

    // ==================== 业务流程处理 ====================

    /**
     * 开始处理业务功能2
     *
     * @param id 业务功能2ID
     * @param processorId 处理人ID
     * @param processorName 处理人姓名
     * @return 操作结果
     */
    Mono<Boolean> startProcessing(Long id, Long processorId, String processorName);

    /**
     * 完成业务功能2处理
     *
     * @param id 业务功能2ID
     * @param result 处理结果
     * @return 操作结果
     */
    Mono<Boolean> completeProcessing(Long id, String result);

    /**
     * 重新处理业务功能2
     *
     * @param id 业务功能2ID
     * @param reason 重新处理原因
     * @return 操作结果
     */
    Mono<Boolean> reprocessBusinessFunction2(Long id, String reason);

    // ==================== 业务规则配置 ====================

    /**
     * 更新业务配置
     *
     * @param id 业务功能2ID
     * @param configData 配置数据
     * @return 操作结果
     */
    Mono<Boolean> updateBusinessConfig(Long id, String configData);

    /**
     * 验证业务规则
     *
     * @param id 业务功能2ID
     * @param ruleData 规则数据
     * @return 验证结果
     */
    Mono<Boolean> validateBusinessRule(Long id, String ruleData);

    /**
     * 执行业务规则
     *
     * @param id 业务功能2ID
     * @param inputData 输入数据
     * @return 执行结果
     */
    Mono<String> executeBusinessRule(Long id, String inputData);

    // ==================== 统计和报表 ====================

    /**
     * 统计业务功能2总数
     *
     * @return 总数
     */
    Mono<Long> countBusinessFunction2();

    /**
     * 根据状态统计业务功能2数量
     *
     * @param status 业务状态
     * @return 数量
     */
    Mono<Long> countBusinessFunction2ByStatus(BusinessFunction2.BusinessStatus status);

    /**
     * 获取活跃业务数量
     *
     * @return 活跃业务数量
     */
    Mono<Long> countActiveBusinessFunction2();

    /**
     * 获取状态统计
     *
     * @return 状态统计Map
     */
    Mono<Map<BusinessFunction2.BusinessStatus, Long>> getStatusStatistics();

    /**
     * 获取优先级统计
     *
     * @return 优先级统计Map
     */
    Mono<Map<BusinessFunction2.Priority, Long>> getPriorityStatistics();

    /**
     * 获取分类统计
     *
     * @return 分类统计Map
     */
    Mono<Map<String, Long>> getCategoryStatistics();

    /**
     * 获取业务趋势统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势统计Map
     */
    Mono<Map<String, Long>> getBusinessTrends(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取处理效率统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 处理效率统计
     */
    Mono<Map<String, Object>> getProcessingEfficiencyStats(LocalDateTime startDate, LocalDateTime endDate);

    // ==================== 工具方法 ====================

    /**
     * 检查业务编号是否存在
     *
     * @param businessNo 业务编号
     * @return 是否存在
     */
    Mono<Boolean> existsByBusinessNo(String businessNo);

    /**
     * 生成业务编号
     *
     * @return 业务编号
     */
    Mono<String> generateBusinessNo();

    /**
     * 检查业务是否有效
     *
     * @param id 业务功能2ID
     * @return 是否有效
     */
    Mono<Boolean> isBusinessValid(Long id);

    /**
     * 获取即将到期的业务
     *
     * @param days 天数
     * @return 即将到期的业务列表
     */
    Flux<BusinessFunction2> getBusinessExpiringWithinDays(int days);
}


