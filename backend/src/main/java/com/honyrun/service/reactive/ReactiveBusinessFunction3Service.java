package com.honyrun.service.reactive;

import com.honyrun.model.entity.business.BusinessFunction3;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 响应式业务功能3服务接口
 *
 * 定义业务功能3相关的业务方法，包括预留业务数据的CRUD操作、业务流程处理、
 * 业务规则配置和执行、业务报表生成等功能。
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理。
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  22:40:00
 * @modified 2025-07-01 22:40:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveBusinessFunction3Service {

    // ==================== 基础CRUD操作 ====================

    /**
     * 创建业务功能3数据
     *
     * @param businessFunction3 业务功能3对象
     * @return 创建成功的业务功能3对象
     */
    Mono<BusinessFunction3> createBusinessFunction3(BusinessFunction3 businessFunction3);

    /**
     * 根据ID查询业务功能3数据
     *
     * @param id 业务功能3ID
     * @return 业务功能3对象
     */
    Mono<BusinessFunction3> getBusinessFunction3ById(Long id);

    /**
     * 根据业务编号查询业务功能3数据
     *
     * @param businessNo 业务编号
     * @return 业务功能3对象
     */
    Mono<BusinessFunction3> getBusinessFunction3ByNo(String businessNo);

    /**
     * 更新业务功能3数据
     *
     * @param id 业务功能3ID
     * @param businessFunction3 更新的业务功能3对象
     * @return 更新后的业务功能3对象
     */
    Mono<BusinessFunction3> updateBusinessFunction3(Long id, BusinessFunction3 businessFunction3);

    /**
     * 删除业务功能3数据
     *
     * @param id 业务功能3ID
     * @return 删除操作结果
     */
    Mono<Boolean> deleteBusinessFunction3(Long id);

    /**
     * 批量删除业务功能3数据
     *
     * @param ids 业务功能3ID列表
     * @return 删除的记录数
     */
    Mono<Long> batchDeleteBusinessFunction3(List<Long> ids);

    // ==================== 查询方法 ====================

    /**
     * 分页查询所有业务功能3数据
     *
     * @param page 页码
     * @param size 每页大小
     * @return 业务功能3数据列表
     */
    Flux<BusinessFunction3> getAllBusinessFunction3(int page, int size);

    /**
     * 根据业务类型查询业务功能3数据
     *
     * @param businessType 业务类型
     * @return 业务功能3数据列表
     */
    Flux<BusinessFunction3> getBusinessFunction3ByType(String businessType);

    /**
     * 根据状态查询业务功能3数据
     *
     * @param status 业务状态
     * @return 业务功能3数据列表
     */
    Flux<BusinessFunction3> getBusinessFunction3ByStatus(BusinessFunction3.BusinessStatus status);

    /**
     * 根据重要性查询业务功能3数据
     *
     * @param importance 重要性
     * @return 业务功能3数据列表
     */
    Flux<BusinessFunction3> getBusinessFunction3ByImportance(BusinessFunction3.Importance importance);

    /**
     * 根据执行模式查询业务功能3数据
     *
     * @param executionMode 执行模式
     * @return 业务功能3数据列表
     */
    Flux<BusinessFunction3> getBusinessFunction3ByExecutionMode(BusinessFunction3.ExecutionMode executionMode);

    /**
     * 根据业务分组查询业务功能3数据
     *
     * @param businessGroup 业务分组
     * @return 业务功能3数据列表
     */
    Flux<BusinessFunction3> getBusinessFunction3ByGroup(String businessGroup);

    /**
     * 根据负责人查询业务功能3数据
     *
     * @param ownerId 负责人ID
     * @return 业务功能3数据列表
     */
    Flux<BusinessFunction3> getBusinessFunction3ByOwner(Long ownerId);

    /**
     * 搜索业务功能3数据
     *
     * @param keyword 搜索关键词
     * @return 业务功能3数据列表
     */
    Flux<BusinessFunction3> searchBusinessFunction3(String keyword);

    /**
     * 根据条件查询业务功能3数据
     *
     * @param businessType 业务类型
     * @param status 业务状态
     * @param importance 重要性
     * @param businessGroup 业务分组
     * @param keyword 关键词
     * @return 业务功能3数据列表
     */
    Flux<BusinessFunction3> getBusinessFunction3ByConditions(String businessType,
                                                            BusinessFunction3.BusinessStatus status,
                                                            BusinessFunction3.Importance importance,
                                                            String businessGroup,
                                                            String keyword);

    // ==================== 业务状态管理 ====================

    /**
     * 开始执行业务功能3
     *
     * @param id 业务功能3ID
     * @return 操作结果
     */
    Mono<Boolean> startBusinessFunction3(Long id);

    /**
     * 暂停业务功能3
     *
     * @param id 业务功能3ID
     * @return 操作结果
     */
    Mono<Boolean> pauseBusinessFunction3(Long id);

    /**
     * 完成业务功能3
     *
     * @param id 业务功能3ID
     * @return 操作结果
     */
    Mono<Boolean> completeBusinessFunction3(Long id);

    /**
     * 取消业务功能3
     *
     * @param id 业务功能3ID
     * @return 操作结果
     */
    Mono<Boolean> cancelBusinessFunction3(Long id);

    /**
     * 重置业务功能3状态
     *
     * @param id 业务功能3ID
     * @return 操作结果
     */
    Mono<Boolean> resetBusinessFunction3(Long id);

    /**
     * 批量更新状态
     *
     * @param ids 业务功能3ID列表
     * @param status 新状态
     * @return 更新的记录数
     */
    Mono<Long> batchUpdateStatus(List<Long> ids, BusinessFunction3.BusinessStatus status);

    // ==================== 业务流程处理 ====================

    /**
     * 开始处理业务功能3
     *
     * @param id 业务功能3ID
     * @param processorId 处理人ID
     * @param processorName 处理人姓名
     * @return 操作结果
     */
    Mono<Boolean> startProcessing(Long id, Long processorId, String processorName);

    /**
     * 完成业务功能3处理
     *
     * @param id 业务功能3ID
     * @param result 处理结果
     * @return 操作结果
     */
    Mono<Boolean> completeProcessing(Long id, String result);

    /**
     * 更新处理进度
     *
     * @param id 业务功能3ID
     * @param progress 进度百分比
     * @return 操作结果
     */
    Mono<Boolean> updateProgress(Long id, Integer progress);

    /**
     * 设置预计完成时间
     *
     * @param id 业务功能3ID
     * @param estimatedEndTime 预计完成时间
     * @return 操作结果
     */
    Mono<Boolean> setEstimatedEndTime(Long id, LocalDateTime estimatedEndTime);

    // ==================== 业务规则配置 ====================

    /**
     * 更新业务参数
     *
     * @param id 业务功能3ID
     * @param parameters 业务参数
     * @return 操作结果
     */
    Mono<Boolean> updateBusinessParameters(Long id, String parameters);

    /**
     * 验证业务规则
     *
     * @param id 业务功能3ID
     * @param ruleData 规则数据
     * @return 验证结果
     */
    Mono<Boolean> validateBusinessRule(Long id, String ruleData);

    /**
     * 执行业务规则
     *
     * @param id 业务功能3ID
     * @param inputData 输入数据
     * @return 执行结果
     */
    Mono<String> executeBusinessRule(Long id, String inputData);

    /**
     * 更新执行模式
     *
     * @param id 业务功能3ID
     * @param executionMode 执行模式
     * @return 操作结果
     */
    Mono<Boolean> updateExecutionMode(Long id, BusinessFunction3.ExecutionMode executionMode);

    // ==================== 统计和报表 ====================

    /**
     * 统计业务功能3总数
     *
     * @return 总数
     */
    Mono<Long> countBusinessFunction3();

    /**
     * 根据状态统计业务功能3数量
     *
     * @param status 业务状态
     * @return 数量
     */
    Mono<Long> countBusinessFunction3ByStatus(BusinessFunction3.BusinessStatus status);

    /**
     * 获取活跃业务数量
     *
     * @return 活跃业务数量
     */
    Mono<Long> countActiveBusinessFunction3();

    /**
     * 获取已完成业务数量
     *
     * @return 已完成业务数量
     */
    Mono<Long> countCompletedBusinessFunction3();

    /**
     * 获取状态统计
     *
     * @return 状态统计Map
     */
    Mono<Map<BusinessFunction3.BusinessStatus, Long>> getStatusStatistics();

    /**
     * 获取重要性统计
     *
     * @return 重要性统计Map
     */
    Mono<Map<BusinessFunction3.Importance, Long>> getImportanceStatistics();

    /**
     * 获取执行模式统计
     *
     * @return 执行模式统计Map
     */
    Mono<Map<BusinessFunction3.ExecutionMode, Long>> getExecutionModeStatistics();

    /**
     * 获取业务分组统计
     *
     * @return 业务分组统计Map
     */
    Mono<Map<String, Long>> getBusinessGroupStatistics();

    /**
     * 获取业务趋势统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势统计Map
     */
    Mono<Map<String, Long>> getBusinessTrends(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取执行效率统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 执行效率统计
     */
    Mono<Map<String, Object>> getExecutionEfficiencyStats(LocalDateTime startDate, LocalDateTime endDate);

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
     * 检查业务是否已完成
     *
     * @param id 业务功能3ID
     * @return 是否已完成
     */
    Mono<Boolean> isBusinessCompleted(Long id);

    /**
     * 检查业务是否已取消
     *
     * @param id 业务功能3ID
     * @return 是否已取消
     */
    Mono<Boolean> isBusinessCancelled(Long id);

    /**
     * 获取即将到期的业务
     *
     * @param days 天数
     * @return 即将到期的业务列表
     */
    Flux<BusinessFunction3> getBusinessExpiringWithinDays(int days);

    /**
     * 获取超期未完成的业务
     *
     * @return 超期未完成的业务列表
     */
    Flux<BusinessFunction3> getOverdueBusinessFunction3();

    /**
     * 获取高重要性的业务
     *
     * @return 高重要性业务列表
     */
    Flux<BusinessFunction3> getHighImportanceBusinessFunction3();

    /**
     * 获取需要关注的业务
     *
     * @return 需要关注的业务列表
     */
    Flux<BusinessFunction3> getBusinessRequiringAttention();
}


