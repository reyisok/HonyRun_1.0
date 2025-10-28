package com.honyrun.service.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 响应式错误恢复服务接口
 *
 * 定义自动错误恢复、降级机制和错误处理策略的响应式接口
 * 支持异步错误恢复和系统自愈能力
 *
 * 主要功能：
 * - 自动错误恢复和重试机制
 * - 降级策略和熔断保护
 * - 错误模式识别和处理
 * - 系统自愈和恢复监控
 * - 错误恢复统计和分析
 * - 恢复策略动态调整
 *
 * 响应式特性：
 * - 非阻塞错误恢复：所有恢复方法返回响应式类型
 * - 异步执行：支持并行错误恢复处理
 * - 流式恢复：支持响应式流中的错误恢复
 * - 自适应恢复：根据错误类型自动选择恢复策略
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 16:57:03
 * @modified 2025-07-01 16:57:03
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveErrorRecoveryService {

    // ==================== 主要错误恢复方法 ====================

    /**
     * 执行自动错误恢复
     * 
     * 对失败的操作执行自动恢复，包括重试、降级等策略
     * 根据错误类型自动选择最适合的恢复策略
     *
     * @param operation 需要恢复的操作
     * @param operationName 操作名称
     * @param <T> 操作返回类型
     * @return 恢复后的操作结果
     */
    <T> Mono<T> performAutoRecovery(Mono<T> operation, String operationName);

    /**
     * 执行背压恢复
     * 
     * 针对背压异常执行专门的恢复策略
     * 包括延迟执行、流量控制、缓冲区管理等
     *
     * @param operation 需要恢复的操作
     * @param operationName 操作名称
     * @param <T> 操作返回类型
     * @return 恢复后的操作结果
     */
    <T> Mono<T> performBackpressureRecovery(Mono<T> operation, String operationName);

    /**
     * 执行流处理恢复
     * 
     * 针对响应式流处理异常执行恢复策略
     * 包括流重启、数据重新处理、流合并恢复等
     *
     * @param stream 需要恢复的流
     * @param operationName 操作名称
     * @param <T> 流元素类型
     * @return 恢复后的流
     */
    <T> Flux<T> performStreamingRecovery(Flux<T> stream, String operationName);

    // ==================== 降级机制方法 ====================

    /**
     * 激活降级模式
     * 
     * 当错误恢复失败时，激活降级模式提供基础服务
     * 根据操作类型返回不同的降级响应
     *
     * @param operationName 操作名称
     * @param error 导致降级的错误
     * @param <T> 返回类型
     * @return 降级响应
     */
    <T> Mono<T> activateDegradationMode(String operationName, Throwable error);

    /**
     * 激活熔断器
     * 
     * 当系统错误率过高时，激活熔断器保护系统
     * 暂时阻止对失败服务的调用，给系统恢复时间
     *
     * @param operationName 操作名称
     * @param error 导致熔断的错误
     * @param <T> 返回类型
     * @return 熔断响应
     */
    <T> Mono<T> activateCircuitBreaker(String operationName, Throwable error);

    // ==================== 恢复统计方法 ====================

    /**
     * 获取错误恢复统计信息
     * 
     * 获取错误恢复的执行次数、成功率、降级次数等统计信息
     * 提供错误恢复的历史数据和趋势分析
     *
     * @return 错误恢复统计信息
     */
    Mono<Map<String, Object>> getRecoveryStatistics();

    /**
     * 重置恢复统计
     * 
     * 重置所有错误恢复相关的统计计数器
     * 用于统计周期重置或系统维护
     *
     * @return 重置完成信号
     */
    Mono<Void> resetRecoveryStatistics();
}


