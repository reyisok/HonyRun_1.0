package com.honyrun.service.impl;

import com.honyrun.model.entity.business.VerificationRequest;
import com.honyrun.model.entity.business.VerificationResult;
import com.honyrun.repository.r2dbc.ReactiveVerificationRepository;
import com.honyrun.service.reactive.ReactiveVerificationService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.exception.BusinessException;
import com.honyrun.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 核验服务实现类
 * 
 * @author Mr.Rey
 * @created 2025-07-01  15:30:00
 * @modified 2025-07-01 15:30:00
 * @version 1.0
 */
@Service
@Transactional
public class ReactiveVerificationServiceImpl implements ReactiveVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveVerificationServiceImpl.class);

    private final ReactiveVerificationRepository verificationRepository;
    private final DatabaseClient databaseClient;
    private final Random random = new Random();

    /**
     * 构造函数
     * 
     * @param verificationRepository 核验仓库
     * @param databaseClient 数据库客户端
     */
    public ReactiveVerificationServiceImpl(ReactiveVerificationRepository verificationRepository,
                                         DatabaseClient databaseClient) {
        this.verificationRepository = verificationRepository;
        this.databaseClient = databaseClient;
    }

    // ==================== 核验请求管理 ====================

    @Override
    public Mono<VerificationRequest> createVerificationRequest(VerificationRequest request) {
        LoggingUtil.info(logger, "创建核验请求", "title", request.getTitle());
        
        return validateVerificationRequest(request)
                .then(generateRequestNo())
                .flatMap(requestNo -> {
                    request.setRequestNo(requestNo);
                    request.setStatus(0); // 待处理
                    request.setCreatedDate(LocalDateTime.now());
                    request.setLastModifiedDate(LocalDateTime.now());
                    return verificationRepository.save(request);
                })
                .doOnSuccess(saved -> LoggingUtil.info(logger, "核验请求创建成功", "id", saved.getId()))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "创建核验请求失败", ex);
                    return ex instanceof BusinessException ? ex : new BusinessException("创建核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<VerificationRequest> getVerificationRequestById(Long requestId) {
        LoggingUtil.info(logger, "查询核验请求", "requestId", requestId);
        
        if (requestId == null) {
            return Mono.error(new ValidationException("请求ID不能为空"));
        }

        return verificationRepository.findById(requestId)
                .doOnNext(request -> LoggingUtil.info(logger, "查询核验请求成功", "requestId", requestId))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "查询核验请求失败", ex, "requestId", requestId);
                    return new BusinessException("查询核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<VerificationRequest> getVerificationRequestByNo(String requestNo) {
        LoggingUtil.info(logger, "根据请求号查询核验请求", "requestNo", requestNo);
        
        if (!StringUtils.hasText(requestNo)) {
            return Mono.error(new ValidationException("请求号不能为空"));
        }

        return verificationRepository.findByRequestNo(requestNo)
                .doOnNext(request -> LoggingUtil.info(logger, "根据请求号查询核验请求成功", "requestNo", requestNo))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "根据请求号查询核验请求失败", ex, "requestNo", requestNo);
                    return new BusinessException("根据请求号查询核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<VerificationRequest> updateVerificationRequest(Long requestId, VerificationRequest request) {
        LoggingUtil.info(logger, "更新核验请求", "requestId", requestId);
        
        return validateVerificationRequest(request)
                .then(getVerificationRequestById(requestId))
                .switchIfEmpty(Mono.error(new BusinessException("核验请求不存在")))
                .flatMap(existing -> {
                    updateRequestFields(existing, request);
                    return verificationRepository.save(existing);
                })
                .doOnSuccess(updated -> LoggingUtil.info(logger, "核验请求更新成功", "requestId", requestId))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "更新核验请求失败", ex, "requestId", requestId);
                    return ex instanceof BusinessException ? ex : new BusinessException("更新核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<Boolean> deleteVerificationRequest(Long requestId) {
        LoggingUtil.info(logger, "删除核验请求", "requestId", requestId);
        
        if (requestId == null) {
            return Mono.just(false);
        }

        return getVerificationRequestById(requestId)
                .flatMap(existing -> verificationRepository.deleteById(requestId)
                        .then(Mono.just(true)))
                .switchIfEmpty(Mono.just(false))
                .doOnSuccess(result -> {
                    if (result) {
                        LoggingUtil.info(logger, "核验请求删除成功", "requestId", requestId);
                    }
                })
                .onErrorResume(Exception.class, ex -> {
                    LoggingUtil.error(logger, "删除核验请求失败", ex, "requestId", requestId);
                    return Mono.just(false);
                });
    }

    @Override
    public Mono<Long> batchDeleteVerificationRequests(List<Long> requestIds) {
        LoggingUtil.info(logger, "批量删除核验请求", "count", requestIds.size());
        
        if (requestIds == null || requestIds.isEmpty()) {
            return Mono.error(new ValidationException("请求ID列表不能为空"));
        }

        return verificationRepository.deleteAllById(requestIds)
                .then(Mono.just((long) requestIds.size()))
                .doOnSuccess(count -> LoggingUtil.info(logger, "批量删除核验请求成功", "count", count))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "批量删除核验请求失败", ex);
                    return new BusinessException("批量删除核验请求失败: " + ex.getMessage());
                });
    }

    // ==================== 查询方法 ====================

    @Override
    public Flux<VerificationRequest> getAllVerificationRequests(int page, int size) {
        LoggingUtil.info(logger, "分页查询所有核验请求", "page", page, "size", size);
        
        if (page < 0 || size <= 0) {
            return Flux.error(new ValidationException("分页参数无效"));
        }

        return verificationRepository.findAll()
                .sort((r1, r2) -> r2.getCreatedDate().compareTo(r1.getCreatedDate()))
                .skip((long) page * size)
                .take(size)
                .doOnComplete(() -> LoggingUtil.info(logger, "分页查询所有核验请求完成"))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "分页查询所有核验请求失败", ex);
                    return new BusinessException("分页查询所有核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Flux<VerificationRequest> getVerificationRequestsByType(String requestType) {
        LoggingUtil.info(logger, "根据类型查询核验请求", "requestType", requestType);
        
        if (!StringUtils.hasText(requestType)) {
            return Flux.error(new ValidationException("请求类型不能为空"));
        }

        return verificationRepository.findByRequestType(requestType)
                .doOnComplete(() -> LoggingUtil.info(logger, "根据类型查询核验请求完成"))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "根据类型查询核验请求失败", ex);
                    return new BusinessException("根据类型查询核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Flux<VerificationRequest> getVerificationRequestsByStatus(Integer status) {
        LoggingUtil.info(logger, "根据状态查询核验请求", "status", status);
        
        if (status == null) {
            return Flux.error(new ValidationException("状态不能为空"));
        }

        return verificationRepository.findByStatus(status)
                .doOnComplete(() -> LoggingUtil.info(logger, "根据状态查询核验请求完成"))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "根据状态查询核验请求失败", ex);
                    return new BusinessException("根据状态查询核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Flux<VerificationRequest> getVerificationRequestsByRequesterId(Long requesterId) {
        LoggingUtil.info(logger, "根据请求人ID查询核验请求", "requesterId", requesterId);
        
        if (requesterId == null) {
            return Flux.error(new ValidationException("请求人ID不能为空"));
        }

        return verificationRepository.findByRequesterId(requesterId)
                .doOnComplete(() -> LoggingUtil.info(logger, "根据请求人ID查询核验请求完成"))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "根据请求人ID查询核验请求失败", ex);
                    return new BusinessException("根据请求人ID查询核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Flux<VerificationRequest> getVerificationRequestsByProcessorId(Long processorId) {
        LoggingUtil.info(logger, "根据处理人ID查询核验请求", "processorId", processorId);
        
        if (processorId == null) {
            return Flux.error(new ValidationException("处理人ID不能为空"));
        }

        return verificationRepository.findByProcessorId(processorId)
                .doOnComplete(() -> LoggingUtil.info(logger, "根据处理人ID查询核验请求完成"))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "根据处理人ID查询核验请求失败", ex);
                    return new BusinessException("根据处理人ID查询核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Flux<VerificationRequest> getVerificationRequestsByPriority(Integer priority) {
        LoggingUtil.info(logger, "根据优先级查询核验请求", "priority", priority);
        
        if (priority == null) {
            return Flux.error(new ValidationException("优先级不能为空"));
        }

        return verificationRepository.findByPriority(priority)
                .doOnComplete(() -> LoggingUtil.info(logger, "根据优先级查询核验请求完成"))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "根据优先级查询核验请求失败", ex);
                    return new BusinessException("根据优先级查询核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Flux<VerificationRequest> searchVerificationRequests(String keyword) {
        LoggingUtil.info(logger, "搜索核验请求", "keyword", keyword);
        
        if (!StringUtils.hasText(keyword)) {
            return Flux.error(new ValidationException("搜索关键词不能为空"));
        }

        return verificationRepository.findByTitleContainingIgnoreCase(keyword)
                .concatWith(verificationRepository.findByConditions(null, null, null, null, keyword))
                .doOnComplete(() -> LoggingUtil.info(logger, "搜索核验请求完成"))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "搜索核验请求失败", ex);
                    return new BusinessException("搜索核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Flux<VerificationRequest> getVerificationRequestsByConditions(String requestType, Integer status,
                                                                        Integer priority, Long requesterId, String keyword) {
        LoggingUtil.info(logger, "根据条件查询核验请求");
        
        return verificationRepository.findByConditions(requestType, status, priority, requesterId, keyword)
                .doOnComplete(() -> LoggingUtil.info(logger, "根据条件查询核验请求完成"))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "根据条件查询核验请求失败", ex);
                    return new BusinessException("根据条件查询核验请求失败: " + ex.getMessage());
                });
    }

    // ==================== 状态管理 ====================

    @Override
    public Mono<Boolean> startProcessing(Long requestId, Long processorId, String processorName) {
        LoggingUtil.info(logger, "开始处理核验请求", "requestId", requestId, "processorId", processorId);
        
        return getVerificationRequestById(requestId)
                .switchIfEmpty(Mono.error(new BusinessException("核验请求不存在")))
                .flatMap(request -> {
                    request.setStatus(1); // 处理中
                    request.setProcessorId(processorId);
                    request.setProcessorName(processorName);
                    request.setActualStartTime(LocalDateTime.now());
                    request.setLastModifiedDate(LocalDateTime.now());
                    return verificationRepository.save(request);
                })
                .map(updated -> true)
                .doOnSuccess(result -> LoggingUtil.info(logger, "开始处理核验请求成功", "requestId", requestId))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "开始处理核验请求失败", ex, "requestId", requestId);
                    return ex instanceof BusinessException ? ex : new BusinessException("开始处理核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<Boolean> completeProcessing(Long requestId, String processResult) {
        LoggingUtil.info(logger, "完成处理核验请求", "requestId", requestId);
        
        return getVerificationRequestById(requestId)
                .switchIfEmpty(Mono.error(new BusinessException("核验请求不存在")))
                .flatMap(request -> {
                    request.setStatus(2); // 已完成
                    request.setProcessResult(processResult);
                    request.setActualCompletionTime(LocalDateTime.now());
                    request.setLastModifiedDate(LocalDateTime.now());
                    // 保持现有版本号，让@Version注解自动管理乐观锁
                    return verificationRepository.save(request);
                })
                .map(updated -> true)
                .doOnSuccess(result -> LoggingUtil.info(logger, "完成处理核验请求成功", "requestId", requestId))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "完成处理核验请求失败", ex, "requestId", requestId);
                    return ex instanceof BusinessException ? ex : new BusinessException("完成处理核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<Boolean> cancelVerificationRequest(Long requestId, String reason) {
        LoggingUtil.info(logger, "取消核验请求", "requestId", requestId);
        
        return getVerificationRequestById(requestId)
                .switchIfEmpty(Mono.error(new BusinessException("核验请求不存在")))
                .flatMap(request -> {
                    request.setStatus(3); // 已取消
                    request.setFailureReason(reason);
                    request.setLastModifiedDate(LocalDateTime.now());
                    // 保持现有版本号，让@Version注解自动管理乐观锁
                    return verificationRepository.save(request);
                })
                .map(updated -> true)
                .doOnSuccess(result -> LoggingUtil.info(logger, "取消核验请求成功", "requestId", requestId))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "取消核验请求失败", ex, "requestId", requestId);
                    return ex instanceof BusinessException ? ex : new BusinessException("取消核验请求失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<Boolean> markProcessingFailed(Long requestId, String failureReason) {
        LoggingUtil.info(logger, "标记处理失败", "requestId", requestId);
        
        return getVerificationRequestById(requestId)
                .flatMap(request -> {
                    request.setStatus(4); // 处理失败
                    request.setFailureReason(failureReason);
                    request.setLastModifiedDate(LocalDateTime.now());
                    return verificationRepository.save(request);
                })
                .map(updated -> true)
                .switchIfEmpty(Mono.just(false))
                .doOnSuccess(result -> {
                    if (result) {
                        LoggingUtil.info(logger, "标记处理失败成功", "requestId", requestId);
                    }
                })
                .onErrorResume(Exception.class, ex -> {
                    LoggingUtil.error(logger, "标记处理失败失败", ex, "requestId", requestId);
                    return Mono.just(false);
                });
    }

    @Override
    public Mono<Long> batchAssignProcessor(List<Long> requestIds, Long processorId, String processorName) {
        LoggingUtil.info(logger, "批量分配处理人", "count", requestIds.size());
        
        return verificationRepository.assignProcessorByIds(requestIds, processorId, processorName)
                .map(count -> count.longValue())
                .doOnSuccess(count -> LoggingUtil.info(logger, "批量分配处理人成功", "count", count))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "批量分配处理人失败", ex);
                    return new BusinessException("批量分配处理人失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<Long> batchUpdateStatus(List<Long> requestIds, Integer status) {
        LoggingUtil.info(logger, "批量更新状态", "count", requestIds.size());
        
        return verificationRepository.batchUpdateStatus(requestIds, status)
                .map(count -> count.longValue())
                .doOnSuccess(count -> LoggingUtil.info(logger, "批量更新状态成功", "count", count))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "批量更新状态失败", ex);
                    return new BusinessException("批量更新状态失败: " + ex.getMessage());
                });
    }

    // ==================== 核验结果管理 ====================

    @Override
    public Mono<VerificationResult> createVerificationResult(VerificationResult result) {
        LoggingUtil.info(logger, "创建核验结果", "requestId", result.getRequestId());
        
        return validateVerificationResult(result)
                .then(generateResultNo())
                .flatMap(resultNo -> {
                    result.setResultNo(resultNo);
                    result.setCreatedDate(LocalDateTime.now());
                    result.setLastModifiedDate(LocalDateTime.now());
                    
                    return databaseClient.sql("""
                        INSERT INTO verification_results
                        (request_id, result_no, result_status, result_description, score,
                         verification_start_time, verification_end_time, processing_time_ms,
                         verifier_id, verifier_name, risk_level, requires_recheck,
                         created_at, last_modified_date, version)
                        VALUES (:requestId, :resultNo, :resultStatus, :resultDescription, :score,
                                :verificationStartTime, :verificationEndTime, :processingTimeMs,
                                :verifierId, :verifierName, :riskLevel, :requiresRecheck,
                                :createdDate, :lastModifiedDate, 1)
                        """)
                        .bind("requestId", result.getRequestId())
                        .bind("resultNo", result.getResultNo())
                        .bind("resultStatus", result.getResultStatus())
                        .bind("resultDescription", result.getResultDescription())
                        .bind("score", result.getScore())
                        .bind("verificationStartTime", result.getVerificationStartTime())
                        .bind("verificationEndTime", result.getVerificationEndTime())
                        .bind("processingTimeMs", result.getProcessingTimeMs())
                        .bind("verifierId", result.getVerifierId())
                        .bind("verifierName", result.getVerifierName())
                        .bind("riskLevel", result.getRiskLevel())
                        .bind("requiresRecheck", result.getRequiresRecheck())
                        .bind("createdDate", result.getCreatedDate())
                        .bind("lastModifiedDate", result.getLastModifiedDate())
                        .fetch()
                        .first()
                        .map(row -> result);
                })
                .doOnSuccess(saved -> LoggingUtil.info(logger, "核验结果创建成功", "resultNo", saved.getResultNo()))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "创建核验结果失败", ex);
                    return ex instanceof BusinessException ? ex : new BusinessException("创建核验结果失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<VerificationResult> getVerificationResultById(Long resultId) {
        LoggingUtil.info(logger, "查询核验结果", "resultId", resultId);
        
        if (resultId == null) {
            return Mono.error(new ValidationException("结果ID不能为空"));
        }

        return databaseClient.sql("SELECT * FROM verification_results WHERE id = :id")
                .bind("id", resultId)
                .map(this::mapRowToVerificationResult)
                .first()
                .switchIfEmpty(Mono.error(new BusinessException("核验结果不存在")))
                .doOnNext(result -> LoggingUtil.info(logger, "查询核验结果成功", "resultId", resultId))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "查询核验结果失败", ex, "resultId", resultId);
                    return ex instanceof BusinessException ? ex : new BusinessException("查询核验结果失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<VerificationResult> getVerificationResultByRequestId(Long requestId) {
        LoggingUtil.info(logger, "根据请求ID查询核验结果", "requestId", requestId);
        
        return verificationRepository.findResultByRequestId(requestId)
                .doOnNext(result -> LoggingUtil.info(logger, "根据请求ID查询核验结果成功", "requestId", requestId))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "根据请求ID查询核验结果失败", ex, "requestId", requestId);
                    return new BusinessException("根据请求ID查询核验结果失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<VerificationResult> getVerificationResultByNo(String resultNo) {
        LoggingUtil.info(logger, "根据结果号查询核验结果", "resultNo", resultNo);
        
        return verificationRepository.findResultByResultNo(resultNo)
                .doOnNext(result -> LoggingUtil.info(logger, "根据结果号查询核验结果成功", "resultNo", resultNo))
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "根据结果号查询核验结果失败", ex, "resultNo", resultNo);
                    return new BusinessException("根据结果号查询核验结果失败: " + ex.getMessage());
                });
    }

    // ==================== 统计方法 ====================

    @Override
    public Mono<Long> countVerificationRequests() {
        return verificationRepository.count();
    }

    @Override
    public Mono<Long> countVerificationRequestsByStatus(Integer status) {
        return verificationRepository.countByStatus(status);
    }

    @Override
    public Mono<Long> countPendingRequests() {
        return verificationRepository.countByStatus(0);
    }

    @Override
    public Mono<Long> countOverdueRequests() {
        return verificationRepository.countOverdueRequests();
    }

    @Override
    public Mono<Long> countHighPriorityRequests() {
        return verificationRepository.countByPriority(3);
    }

    @Override
    public Mono<BigDecimal> calculateAverageProcessingTime(LocalDateTime startTime, LocalDateTime endTime) {
        return verificationRepository.calculateAverageProcessingTime(startTime, endTime);
    }

    @Override
    public Mono<BigDecimal> calculatePassRate(LocalDateTime startTime, LocalDateTime endTime) {
        return verificationRepository.calculatePassRate(startTime, endTime);
    }

    // ==================== 核验结果操作 ====================

    @Override
    public Mono<VerificationResult> updateVerificationResult(Long resultId, VerificationResult result) {
        LoggingUtil.info(logger, "更新核验结果", "resultId", resultId);
        
        if (resultId == null) {
            return Mono.error(new ValidationException("结果ID不能为空"));
        }
        
        return validateVerificationResult(result)
                .then(getVerificationResultById(resultId))
                .switchIfEmpty(Mono.error(new BusinessException("核验结果不存在")))
                .flatMap(existing -> {
                    // 更新字段
                    if (result.getResultStatus() != null) {
                        existing.setResultStatus(result.getResultStatus());
                    }
                    if (StringUtils.hasText(result.getResultDescription())) {
                        existing.setResultDescription(result.getResultDescription());
                    }
                    if (result.getScore() != null) {
                        existing.setScore(result.getScore());
                    }
                    if (result.getRiskLevel() != null) {
                        existing.setRiskLevel(result.getRiskLevel());
                    }
                    if (result.getRequiresRecheck() != null) {
                        existing.setRequiresRecheck(result.getRequiresRecheck());
                    }
                    existing.setLastModifiedDate(LocalDateTime.now());
                    
                    // 使用 DatabaseClient 更新
                    return databaseClient.sql("""
                        UPDATE verification_results
                        SET result_status = :resultStatus,
                            result_description = :resultDescription,
                            score = :score,
                            risk_level = :riskLevel,
                            requires_recheck = :requiresRecheck,
                            last_modified_date = :lastModifiedDate,
                            version = version + 1
                        WHERE id = :id
                        """)
                        .bind("resultStatus", existing.getResultStatus())
                        .bind("resultDescription", existing.getResultDescription())
                        .bind("score", existing.getScore())
                        .bind("riskLevel", existing.getRiskLevel())
                        .bind("requiresRecheck", existing.getRequiresRecheck())
                        .bind("lastModifiedDate", existing.getLastModifiedDate())
                        .bind("id", resultId)
                        .fetch()
                        .rowsUpdated()
                        .map(count -> {
                            if (count > 0) {
                                LoggingUtil.info(logger, "核验结果更新成功", "resultId", resultId);
                                return existing;
                            } else {
                                throw new BusinessException("核验结果更新失败");
                            }
                        });
                })
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "更新核验结果失败", ex, "resultId", resultId);
                    return ex instanceof BusinessException ? ex : new BusinessException("更新核验结果失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<Boolean> deleteVerificationResult(Long resultId) {
        LoggingUtil.info(logger, "删除核验结果", "resultId", resultId);
        
        if (resultId == null) {
            return Mono.error(new ValidationException("结果ID不能为空"));
        }
        
        return getVerificationResultById(resultId)
                .switchIfEmpty(Mono.error(new BusinessException("核验结果不存在")))
                .flatMap(existing -> 
                    databaseClient.sql("DELETE FROM verification_results WHERE id = :id")
                        .bind("id", resultId)
                        .fetch()
                        .rowsUpdated()
                        .map(count -> {
                            boolean success = count > 0;
                            if (success) {
                                LoggingUtil.info(logger, "核验结果删除成功", "resultId", resultId);
                            } else {
                                LoggingUtil.warn(logger, "核验结果删除失败", "resultId", resultId);
                            }
                            return success;
                        })
                )
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "删除核验结果失败", ex, "resultId", resultId);
                    return ex instanceof BusinessException ? ex : new BusinessException("删除核验结果失败: " + ex.getMessage());
                });
    }

    @Override
    public Flux<VerificationResult> getVerificationResultsByVerifierId(Long verifierId) {
        return verificationRepository.findResultsByVerifierId(verifierId);
    }

    @Override
    public Flux<VerificationResult> getVerificationResultsByStatus(Integer resultStatus) {
        return verificationRepository.findResultsByStatus(resultStatus);
    }

    @Override
    public Flux<VerificationResult> getVerificationResultsByRiskLevel(Integer riskLevel) {
        return verificationRepository.findResultsByRiskLevel(riskLevel);
    }

    @Override
    public Mono<VerificationResult> performVerification(Long requestId, Long verifierId, String verifierName) {
        LoggingUtil.info(logger, "执行核验处理", "requestId", requestId, "verifierId", verifierId);
        
        if (requestId == null || verifierId == null) {
            return Mono.error(new ValidationException("请求ID和核验人ID不能为空"));
        }
        
        return getVerificationRequestById(requestId)
                .switchIfEmpty(Mono.error(new BusinessException("核验请求不存在")))
                .flatMap(request -> {
                    // 检查请求状态
                    if (request.getStatus() != 0) {
                        return Mono.error(new BusinessException("请求状态不允许进行核验"));
                    }
                    
                    // 创建核验结果
                    VerificationResult result = new VerificationResult();
                    result.setRequestId(requestId);
                    result.setVerifierId(verifierId);
                    result.setVerifierName(verifierName);
                    result.setResultStatus(1); // 核验中
                    result.setResultDescription("核验处理中");
                    result.setScore(BigDecimal.ZERO);
                    result.setRiskLevel(0);
                    result.setRequiresRecheck(false);
                    result.setVerificationStartTime(LocalDateTime.now());
                    
                    return generateResultNo()
                            .flatMap(resultNo -> {
                                result.setResultNo(resultNo);
                                return createVerificationResult(result);
                            })
                            .flatMap(createdResult -> {
                                // 更新请求状态为处理中
                                return startProcessing(requestId, verifierId, verifierName)
                                        .map(success -> createdResult);
                            });
                })
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "执行核验处理失败", ex, "requestId", requestId);
                    return ex instanceof BusinessException ? ex : new BusinessException("执行核验处理失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<Boolean> reviewVerificationResult(Long resultId, Long reviewerId, String reviewerName, String reviewComments) {
        LoggingUtil.info(logger, "审核核验结果", "resultId", resultId, "reviewerId", reviewerId);
        
        if (resultId == null || reviewerId == null) {
            return Mono.error(new ValidationException("结果ID和审核人ID不能为空"));
        }
        
        return getVerificationResultById(resultId)
                .switchIfEmpty(Mono.error(new BusinessException("核验结果不存在")))
                .flatMap(result -> {
                    // 更新审核信息
                    result.setResultStatus(3); // 已审核
                    result.setResultDescription(StringUtils.hasText(reviewComments) ? reviewComments : "审核完成");
                    result.setLastModifiedDate(LocalDateTime.now());
                    
                    return updateVerificationResult(resultId, result)
                            .map(updatedResult -> true);
                })
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "审核核验结果失败", ex, "resultId", resultId);
                    return ex instanceof BusinessException ? ex : new BusinessException("审核核验结果失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<Boolean> markForRecheck(Long resultId, String recheckReason) {
        LoggingUtil.info(logger, "标记复核", "resultId", resultId);
        
        if (resultId == null) {
            return Mono.error(new ValidationException("结果ID不能为空"));
        }
        
        return getVerificationResultById(resultId)
                .switchIfEmpty(Mono.error(new BusinessException("核验结果不存在")))
                .flatMap(result -> {
                    // 标记需要复核
                    result.setRequiresRecheck(true);
                    result.setResultStatus(4); // 待复核
                    result.setResultDescription(StringUtils.hasText(recheckReason) ? recheckReason : "标记为需要复核");
                    result.setLastModifiedDate(LocalDateTime.now());
                    
                    return updateVerificationResult(resultId, result)
                            .map(updatedResult -> true);
                })
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "标记复核失败", ex, "resultId", resultId);
                    return ex instanceof BusinessException ? ex : new BusinessException("标记复核失败: " + ex.getMessage());
                });
    }

    @Override
    public Mono<VerificationResult> reverify(Long requestId, Long verifierId, String verifierName) {
        LoggingUtil.info(logger, "重新核验", "requestId", requestId, "verifierId", verifierId);
        
        if (requestId == null || verifierId == null) {
            return Mono.error(new ValidationException("请求ID和核验人ID不能为空"));
        }
        
        return getVerificationRequestById(requestId)
                .switchIfEmpty(Mono.error(new BusinessException("核验请求不存在")))
                .flatMap(request -> {
                    // 获取原有结果
                    return getVerificationResultByRequestId(requestId)
                            .flatMap(existingResult -> {
                                // 创建新的核验结果
                                VerificationResult newResult = new VerificationResult();
                                newResult.setRequestId(requestId);
                                newResult.setVerifierId(verifierId);
                                newResult.setVerifierName(verifierName);
                                newResult.setResultStatus(1); // 重新核验中
                                newResult.setResultDescription("重新核验处理中");
                                newResult.setScore(BigDecimal.ZERO);
                                newResult.setRiskLevel(0);
                                newResult.setRequiresRecheck(false);
                                newResult.setVerificationStartTime(LocalDateTime.now());
                                
                                return generateResultNo()
                                        .flatMap(resultNo -> {
                                            newResult.setResultNo(resultNo);
                                            return createVerificationResult(newResult);
                                        });
                            })
                            .switchIfEmpty(
                                // 如果没有原有结果，直接创建新结果
                                performVerification(requestId, verifierId, verifierName)
                            );
                })
                .onErrorMap(Exception.class, ex -> {
                    LoggingUtil.error(logger, "重新核验失败", ex, "requestId", requestId);
                    return ex instanceof BusinessException ? ex : new BusinessException("重新核验失败: " + ex.getMessage());
                });
    }

    // ==================== 特殊查询接口 ====================

    @Override
    public Flux<VerificationRequest> getPendingRequests() {
        return verificationRepository.findByStatus(0);
    }

    @Override
    public Flux<VerificationRequest> getUrgentRequests() {
        return verificationRepository.findUrgentRequests();
    }

    @Override
    public Flux<VerificationRequest> getOverdueRequests() {
        return verificationRepository.findRequestsExpiringBefore(LocalDateTime.now());
    }

    @Override
    public Flux<VerificationResult> getHighRiskResults() {
        return verificationRepository.findHighRiskResults();
    }

    @Override
    public Flux<VerificationResult> getResultsRequiringRecheck() {
        return verificationRepository.findResultsRequiringRecheck();
    }

    @Override
    public Flux<VerificationResult> getResultsExpiringWithinDays(int days) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(days);
        return verificationRepository.findResultsExpiringBefore(threshold);
    }

    // ==================== 统计分析 ====================

    @Override
    public Mono<Map<Integer, Long>> getRequestStatusStatistics() {
        return verificationRepository.countRequestsByStatus()
                .collectMap(
                    row -> (Integer) row[0],
                    row -> (Long) row[1]
                );
    }

    @Override
    public Mono<Map<Integer, Long>> getRequestPriorityStatistics() {
        return verificationRepository.countRequestsByPriority()
                .collectMap(
                    row -> (Integer) row[0],
                    row -> (Long) row[1]
                );
    }

    @Override
    public Mono<Map<String, Long>> getRequestTypeStatistics() {
        return verificationRepository.countRequestsByType()
                .collectMap(
                    row -> (String) row[0],
                    row -> (Long) row[1]
                );
    }

    @Override
    public Mono<Map<Integer, Long>> getResultRiskLevelStatistics() {
        return verificationRepository.countResultsByRiskLevel()
                .collectMap(
                    row -> (Integer) row[0],
                    row -> (Long) row[1]
                );
    }

    @Override
    public Mono<Map<String, Long>> getVerificationRequestTrends(LocalDateTime startDate, LocalDateTime endDate) {
        return Mono.just(new HashMap<>());
    }

    @Override
    public Mono<Map<String, Long>> getVerificationCompletionTrends(LocalDateTime startDate, LocalDateTime endDate) {
        return Mono.just(new HashMap<>());
    }

    @Override
    public Mono<Map<String, Long>> getProcessorWorkloadStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return Mono.just(new HashMap<>());
    }

    // ==================== 存在性检查 ====================

    @Override
    public Mono<Boolean> existsByRequestNo(String requestNo) {
        return verificationRepository.existsByRequestNo(requestNo);
    }

    @Override
    public Mono<Boolean> existsByResultNo(String resultNo) {
        return Mono.just(false); // 简化实现
    }

    @Override
    public Mono<Boolean> hasPendingRequestByRequesterId(Long requesterId) {
        return verificationRepository.hasPendingRequest(requesterId);
    }

    // ==================== 编号生成 ====================

    @Override
    public Mono<String> generateRequestNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = String.format("%04d", random.nextInt(10000));
        return Mono.just("VR" + timestamp + randomSuffix);
    }

    @Override
    public Mono<String> generateResultNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = String.format("%04d", random.nextInt(10000));
        return Mono.just("VRS" + timestamp + randomSuffix);
    }

    // ==================== 私有辅助方法 ====================

    private Mono<Void> validateVerificationRequest(VerificationRequest request) {
        if (request == null) {
            return Mono.error(new ValidationException("核验请求不能为空"));
        }
        if (!StringUtils.hasText(request.getTitle())) {
            return Mono.error(new ValidationException("请求标题不能为空"));
        }
        if (!StringUtils.hasText(request.getRequestType())) {
            return Mono.error(new ValidationException("请求类型不能为空"));
        }
        if (request.getRequesterId() == null) {
            return Mono.error(new ValidationException("请求人ID不能为空"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateVerificationResult(VerificationResult result) {
        if (result == null) {
            return Mono.error(new ValidationException("核验结果不能为空"));
        }
        if (result.getRequestId() == null) {
            return Mono.error(new ValidationException("关联请求ID不能为空"));
        }
        return Mono.empty();
    }

    private void updateRequestFields(VerificationRequest existing, VerificationRequest request) {
        if (StringUtils.hasText(request.getTitle())) {
            existing.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getContent())) {
            existing.setContent(request.getContent());
        }
        if (request.getPriority() != null) {
            existing.setPriority(request.getPriority());
        }
        if (request.getExpectedCompletionTime() != null) {
            existing.setExpectedCompletionTime(request.getExpectedCompletionTime());
        }
        // 保持现有版本号，让JPA自动处理乐观锁
        // 不修改version字段，让@Version注解自动管理
        existing.setLastModifiedDate(LocalDateTime.now());
    }

    private VerificationResult mapRowToVerificationResult(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata metadata) {
        VerificationResult result = new VerificationResult();
        result.setId(row.get("id", Long.class));
        result.setRequestId(row.get("request_id", Long.class));
        result.setResultNo(row.get("result_no", String.class));
        result.setResultStatus(row.get("result_status", Integer.class));
        result.setResultDescription(row.get("result_description", String.class));
        result.setScore(row.get("score", BigDecimal.class));
        result.setVerificationStartTime(row.get("verification_start_time", LocalDateTime.class));
        result.setVerificationEndTime(row.get("verification_end_time", LocalDateTime.class));
        result.setProcessingTimeMs(row.get("processing_time_ms", Long.class));
        result.setVerifierId(row.get("verifier_id", Long.class));
        result.setVerifierName(row.get("verifier_name", String.class));
        result.setRiskLevel(row.get("risk_level", Integer.class));
        result.setRequiresRecheck(row.get("requires_recheck", Boolean.class));
        result.setCreatedDate(row.get("created_at", LocalDateTime.class));
        result.setVersion(row.get("version", Long.class));
        return result;
    }
}


