package com.honyrun.service.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式乐观锁服务接口
 *
 * 提供统一的乐观锁异常处理和重试机制，包括：
 * - 自动检测OptimisticLockingFailureException
 * - 指数退避重试策略，带抖动
 * - 详细的统计和监控
 * - 可配置的重试参数
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01 01:00:00
 * @modified 2025-07-01 01:00:00
 * @version 1.0.0
 */
public interface ReactiveOptimisticLockingService {

    /**
     * 使用默认配置执行带乐观锁重试的操作
     *
     * @param <T> 操作返回类型
     * @param operation 要执行的操作
     * @param operationName 操作名称，用于日志记录
     * @return 操作结果的响应式单值
     */
    <T> Mono<T> executeWithOptimisticLockRetry(Mono<T> operation, String operationName);

    /**
     * 使用默认配置执行带乐观锁重试的批量操作
     *
     * @param <T> 操作返回类型
     * @param operations 要执行的操作流
     * @param operationName 操作名称，用于日志记录
     * @return 操作结果的响应式流
     */
    <T> Flux<T> executeWithOptimisticLockRetry(Flux<T> operations, String operationName);

    /**
     * 使用自定义配置执行带乐观锁重试的操作
     *
     * @param <T> 操作返回类型
     * @param operation 要执行的操作
     * @param operationName 操作名称，用于日志记录
     * @param maxRetries 最大重试次数
     * @param retryDelayMs 重试延迟毫秒数
     * @return 操作结果的响应式单值
     */
    <T> Mono<T> executeWithCustomRetry(Mono<T> operation, String operationName, int maxRetries, long retryDelayMs);

    /**
     * 检测版本冲突
     *
     * @param entityName 实体名称
     * @param entityId 实体ID
     * @param expectedVersion 期望版本
     * @param actualVersion 实际版本
     * @return 是否存在版本冲突的响应式单值
     */
    Mono<Boolean> detectVersionConflict(String entityName, Long entityId, Long expectedVersion, Long actualVersion);

    /**
     * 判断异常是否为乐观锁异常
     *
     * @param throwable 异常对象
     * @return 是否为乐观锁异常
     */
    boolean isOptimisticLockingException(Throwable throwable);

    /**
     * 获取乐观锁统计信息
     *
     * @return 统计信息的响应式单值
     */
    Mono<java.util.Map<String, Object>> getOptimisticLockStatistics();

    /**
     * 重置乐观锁统计信息
     *
     * @return 重置完成的响应式单值
     */
    Mono<Void> resetOptimisticLockStatistics();

    /**
     * 获取指定操作的重试统计信息
     *
     * @param operationName 操作名称
     * @return 统计信息的响应式单值
     */
    Mono<java.util.Map<String, Object>> getRetryStatistics(String operationName);
}

