package com.honyrun.service.reactive;

import com.honyrun.model.entity.business.VerificationRequest;
import com.honyrun.model.entity.business.VerificationResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 响应式核验服务接口
 *
 * 定义核验业务相关的业务方法，包括核验请求处理、结果管理、统计分析等功能
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理
 * 提供完整的核验业务流程管理和数据分析功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  20:58:00
 * @modified 2025-07-01 20:58:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveVerificationService {

    // ==================== 核验请求基础CRUD操作 ====================

    /**
     * 创建核验请求
     *
     * @param request 核验请求对象，包含请求基本信息
     * @return 创建成功的核验请求对象
     */
    Mono<VerificationRequest> createVerificationRequest(VerificationRequest request);

    /**
     * 根据请求ID查询核验请求
     *
     * @param requestId 请求ID
     * @return 核验请求对象，如果不存在则返回空Mono
     */
    Mono<VerificationRequest> getVerificationRequestById(Long requestId);

    /**
     * 根据请求编号查询核验请求
     *
     * @param requestNo 请求编号
     * @return 核验请求对象，如果不存在则返回空Mono
     */
    Mono<VerificationRequest> getVerificationRequestByNo(String requestNo);

    /**
     * 更新核验请求
     *
     * @param requestId 请求ID
     * @param request 更新的核验请求对象
     * @return 更新后的核验请求对象
     */
    Mono<VerificationRequest> updateVerificationRequest(Long requestId, VerificationRequest request);

    /**
     * 删除核验请求（软删除）
     *
     * @param requestId 请求ID
     * @return 删除操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> deleteVerificationRequest(Long requestId);

    /**
     * 批量删除核验请求（软删除）
     *
     * @param requestIds 请求ID列表
     * @return 删除操作结果，返回成功删除的请求数量
     */
    Mono<Long> batchDeleteVerificationRequests(List<Long> requestIds);

    // ==================== 核验请求查询操作 ====================

    /**
     * 分页查询所有核验请求
     *
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 核验请求对象流
     */
    Flux<VerificationRequest> getAllVerificationRequests(int page, int size);

    /**
     * 根据请求类型查询核验请求
     *
     * @param requestType 请求类型
     * @return 核验请求对象流
     */
    Flux<VerificationRequest> getVerificationRequestsByType(String requestType);

    /**
     * 根据请求状态查询核验请求
     *
     * @param status 请求状态
     * @return 核验请求对象流
     */
    Flux<VerificationRequest> getVerificationRequestsByStatus(Integer status);

    /**
     * 根据请求人ID查询核验请求
     *
     * @param requesterId 请求人ID
     * @return 核验请求对象流
     */
    Flux<VerificationRequest> getVerificationRequestsByRequesterId(Long requesterId);

    /**
     * 根据处理人ID查询核验请求
     *
     * @param processorId 处理人ID
     * @return 核验请求对象流
     */
    Flux<VerificationRequest> getVerificationRequestsByProcessorId(Long processorId);

    /**
     * 根据优先级查询核验请求
     *
     * @param priority 优先级
     * @return 核验请求对象流
     */
    Flux<VerificationRequest> getVerificationRequestsByPriority(Integer priority);

    /**
     * 根据关键字搜索核验请求
     * 支持按标题、内容进行模糊搜索
     *
     * @param keyword 搜索关键字
     * @return 核验请求对象流
     */
    Flux<VerificationRequest> searchVerificationRequests(String keyword);

    /**
     * 根据多个条件查询核验请求
     *
     * @param requestType 请求类型（可选）
     * @param status 请求状态（可选）
     * @param priority 优先级（可选）
     * @param requesterId 请求人ID（可选）
     * @param keyword 关键字（可选）
     * @return 核验请求对象流
     */
    Flux<VerificationRequest> getVerificationRequestsByConditions(String requestType, Integer status,
                                                                 Integer priority, Long requesterId, String keyword);

    // ==================== 核验请求状态管理 ====================

    /**
     * 开始处理核验请求
     * 将请求状态从待处理改为处理中，并分配处理人
     *
     * @param requestId 请求ID
     * @param processorId 处理人ID
     * @param processorName 处理人姓名
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> startProcessing(Long requestId, Long processorId, String processorName);

    /**
     * 完成核验请求处理
     * 将请求状态改为已完成，并记录完成时间和结果
     *
     * @param requestId 请求ID
     * @param processResult 处理结果
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> completeProcessing(Long requestId, String processResult);

    /**
     * 取消核验请求
     * 将请求状态改为已取消
     *
     * @param requestId 请求ID
     * @param reason 取消原因
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> cancelVerificationRequest(Long requestId, String reason);

    /**
     * 标记核验请求处理失败
     * 将请求状态改为处理失败，并记录失败原因
     *
     * @param requestId 请求ID
     * @param failureReason 失败原因
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> markProcessingFailed(Long requestId, String failureReason);

    /**
     * 批量分配处理人
     *
     * @param requestIds 请求ID列表
     * @param processorId 处理人ID
     * @param processorName 处理人姓名
     * @return 成功分配的请求数量
     */
    Mono<Long> batchAssignProcessor(List<Long> requestIds, Long processorId, String processorName);

    /**
     * 批量更新请求状态
     *
     * @param requestIds 请求ID列表
     * @param status 新状态
     * @return 成功更新的请求数量
     */
    Mono<Long> batchUpdateStatus(List<Long> requestIds, Integer status);

    // ==================== 核验结果管理 ====================

    /**
     * 创建核验结果
     *
     * @param result 核验结果对象
     * @return 创建成功的核验结果对象
     */
    Mono<VerificationResult> createVerificationResult(VerificationResult result);

    /**
     * 根据结果ID查询核验结果
     *
     * @param resultId 结果ID
     * @return 核验结果对象，如果不存在则返回空Mono
     */
    Mono<VerificationResult> getVerificationResultById(Long resultId);

    /**
     * 根据请求ID查询核验结果
     *
     * @param requestId 请求ID
     * @return 核验结果对象，如果不存在则返回空Mono
     */
    Mono<VerificationResult> getVerificationResultByRequestId(Long requestId);

    /**
     * 根据结果编号查询核验结果
     *
     * @param resultNo 结果编号
     * @return 核验结果对象，如果不存在则返回空Mono
     */
    Mono<VerificationResult> getVerificationResultByNo(String resultNo);

    /**
     * 更新核验结果
     *
     * @param resultId 结果ID
     * @param result 更新的核验结果对象
     * @return 更新后的核验结果对象
     */
    Mono<VerificationResult> updateVerificationResult(Long resultId, VerificationResult result);

    /**
     * 删除核验结果（软删除）
     *
     * @param resultId 结果ID
     * @return 删除操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> deleteVerificationResult(Long resultId);

    /**
     * 根据核验人员ID查询核验结果
     *
     * @param verifierId 核验人员ID
     * @return 核验结果对象流
     */
    Flux<VerificationResult> getVerificationResultsByVerifierId(Long verifierId);

    /**
     * 根据结果状态查询核验结果
     *
     * @param resultStatus 结果状态
     * @return 核验结果对象流
     */
    Flux<VerificationResult> getVerificationResultsByStatus(Integer resultStatus);

    /**
     * 根据风险等级查询核验结果
     *
     * @param riskLevel 风险等级
     * @return 核验结果对象流
     */
    Flux<VerificationResult> getVerificationResultsByRiskLevel(Integer riskLevel);

    // ==================== 核验业务流程 ====================

    /**
     * 执行核验处理
     * 根据核验请求执行具体的核验逻辑，生成核验结果
     *
     * @param requestId 请求ID
     * @param verifierId 核验人员ID
     * @param verifierName 核验人员姓名
     * @return 核验结果对象
     */
    Mono<VerificationResult> performVerification(Long requestId, Long verifierId, String verifierName);

    /**
     * 审核核验结果
     * 对核验结果进行审核，确认结果的准确性
     *
     * @param resultId 结果ID
     * @param reviewerId 审核人员ID
     * @param reviewerName 审核人员姓名
     * @param reviewComments 审核意见
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> reviewVerificationResult(Long resultId, Long reviewerId, String reviewerName, String reviewComments);

    /**
     * 标记需要复核
     * 将核验结果标记为需要复核状态
     *
     * @param resultId 结果ID
     * @param recheckReason 复核原因
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> markForRecheck(Long resultId, String recheckReason);

    /**
     * 重新核验
     * 对已有的核验请求重新执行核验流程
     *
     * @param requestId 请求ID
     * @param verifierId 核验人员ID
     * @param verifierName 核验人员姓名
     * @return 新的核验结果对象
     */
    Mono<VerificationResult> reverify(Long requestId, Long verifierId, String verifierName);

    // ==================== 查询特殊状态的数据 ====================

    /**
     * 查询待处理的核验请求
     *
     * @return 待处理请求对象流
     */
    Flux<VerificationRequest> getPendingRequests();

    /**
     * 查询需要紧急处理的核验请求
     *
     * @return 紧急处理请求对象流
     */
    Flux<VerificationRequest> getUrgentRequests();

    /**
     * 查询超时的核验请求
     *
     * @return 超时请求对象流
     */
    Flux<VerificationRequest> getOverdueRequests();

    /**
     * 查询高风险的核验结果
     *
     * @return 高风险结果对象流
     */
    Flux<VerificationResult> getHighRiskResults();

    /**
     * 查询需要复核的核验结果
     *
     * @return 需要复核的结果对象流
     */
    Flux<VerificationResult> getResultsRequiringRecheck();

    /**
     * 查询即将过期的核验结果
     *
     * @param days 提前天数
     * @return 即将过期的结果对象流
     */
    Flux<VerificationResult> getResultsExpiringWithinDays(int days);

    // ==================== 统计分析功能 ====================

    /**
     * 统计核验请求总数
     *
     * @return 请求总数
     */
    Mono<Long> countVerificationRequests();

    /**
     * 根据状态统计核验请求数量
     *
     * @param status 请求状态
     * @return 指定状态的请求数量
     */
    Mono<Long> countVerificationRequestsByStatus(Integer status);

    /**
     * 统计待处理的核验请求数量
     *
     * @return 待处理请求数量
     */
    Mono<Long> countPendingRequests();

    /**
     * 统计超时的核验请求数量
     *
     * @return 超时请求数量
     */
    Mono<Long> countOverdueRequests();

    /**
     * 统计高优先级的核验请求数量
     *
     * @return 高优先级请求数量
     */
    Mono<Long> countHighPriorityRequests();

    /**
     * 计算指定时间范围内的平均处理时间
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均处理时间（分钟）
     */
    Mono<BigDecimal> calculateAverageProcessingTime(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 计算核验结果的通过率
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 通过率（百分比）
     */
    Mono<BigDecimal> calculatePassRate(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取各状态的核验请求数量统计
     *
     * @return 状态统计结果映射，key为状态，value为数量
     */
    Mono<Map<Integer, Long>> getRequestStatusStatistics();

    /**
     * 获取各优先级的核验请求数量统计
     *
     * @return 优先级统计结果映射，key为优先级，value为数量
     */
    Mono<Map<Integer, Long>> getRequestPriorityStatistics();

    /**
     * 获取各请求类型的核验请求数量统计
     *
     * @return 请求类型统计结果映射，key为请求类型，value为数量
     */
    Mono<Map<String, Long>> getRequestTypeStatistics();

    /**
     * 获取各风险等级的核验结果数量统计
     *
     * @return 风险等级统计结果映射，key为风险等级，value为数量
     */
    Mono<Map<Integer, Long>> getResultRiskLevelStatistics();

    /**
     * 获取指定时间范围内的核验请求趋势统计
     * 按日期统计核验请求数量
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势统计结果映射，key为日期，value为请求数量
     */
    Mono<Map<String, Long>> getVerificationRequestTrends(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取指定时间范围内的核验完成趋势统计
     * 按日期统计完成的核验请求数量
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 完成趋势统计结果映射，key为日期，value为完成数量
     */
    Mono<Map<String, Long>> getVerificationCompletionTrends(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取处理人员工作量统计
     * 统计各处理人员处理的核验请求数量
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工作量统计结果映射，key为处理人员姓名，value为处理数量
     */
    Mono<Map<String, Long>> getProcessorWorkloadStatistics(LocalDateTime startDate, LocalDateTime endDate);

    // ==================== 数据验证功能 ====================

    /**
     * 验证请求编号是否已存在
     *
     * @param requestNo 请求编号
     * @return 验证结果，存在返回true，不存在返回false
     */
    Mono<Boolean> existsByRequestNo(String requestNo);

    /**
     * 验证结果编号是否已存在
     *
     * @param resultNo 结果编号
     * @return 验证结果，存在返回true，不存在返回false
     */
    Mono<Boolean> existsByResultNo(String resultNo);

    /**
     * 检查指定用户是否有待处理的请求
     *
     * @param requesterId 请求人ID
     * @return 检查结果，有待处理请求返回true，否则返回false
     */
    Mono<Boolean> hasPendingRequestByRequesterId(Long requesterId);

    /**
     * 生成唯一的请求编号
     *
     * @return 唯一的请求编号
     */
    Mono<String> generateRequestNo();

    /**
     * 生成唯一的结果编号
     *
     * @return 唯一的结果编号
     */
    Mono<String> generateResultNo();
}


