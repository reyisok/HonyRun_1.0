package com.honyrun.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.honyrun.config.properties.ErrorRecoveryProperties;
import com.honyrun.exception.reactive.BackpressureException;
import com.honyrun.exception.reactive.ReactiveException;
import com.honyrun.exception.reactive.StreamingException;
import com.honyrun.service.reactive.ReactiveErrorRecoveryService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.MonitoringLogUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

/**
 * 响应式错误恢复服务实现
 *
 * 提供自动错误恢复、降级机制和错误处理策略
 * 支持响应式流错误恢复和系统自愈能力
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
 * - 非阻塞错误恢复：异步执行错误恢复操作
 * - 流式错误处理：支持响应式流中的错误恢复
 * - 背压控制：控制错误恢复的执行速度
 * - 自适应恢复：根据错误类型自动选择恢复策略
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 14:00:00
 * @modified 2025-07-01 14:00:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
@ConditionalOnProperty(name = "honyrun.error-recovery.enabled", havingValue = "true", matchIfMissing = true)
public class ReactiveErrorRecoveryServiceImpl implements ReactiveErrorRecoveryService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveErrorRecoveryServiceImpl.class);

    // ==================== 恢复统计计数器 ====================

    private final AtomicLong totalRecoveryAttempts = new AtomicLong(0);
    private final AtomicLong successfulRecoveries = new AtomicLong(0);
    private final AtomicLong failedRecoveries = new AtomicLong(0);
    private final AtomicLong degradationActivations = new AtomicLong(0);
    private final AtomicLong circuitBreakerTrips = new AtomicLong(0);

    // ==================== 恢复策略配置 ====================

    // 统一配置管理 - 消除@Value硬编码违规
    private final ErrorRecoveryProperties errorRecoveryProperties;

    // ==================== 构造函数 ====================

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param errorRecoveryProperties 错误恢复配置属性
     */
    public ReactiveErrorRecoveryServiceImpl(ErrorRecoveryProperties errorRecoveryProperties) {
        this.errorRecoveryProperties = errorRecoveryProperties;
    }

    // ==================== 主要错误恢复方法 ====================

    /**
     * 执行自动错误恢复
     */
    @Override
    public <T> Mono<T> performAutoRecovery(Mono<T> operation, String operationName) {
        totalRecoveryAttempts.incrementAndGet();

        LoggingUtil.info(logger, "开始自动错误恢复 - 操作: {}", operationName);

        return operation
                .retryWhen(createRetrySpec(operationName))
                .doOnSuccess(result -> {
                    successfulRecoveries.incrementAndGet();
                    LoggingUtil.info(logger, "自动错误恢复成功 - 操作: {}", operationName);
                    createRecoveryLog(operationName, "SUCCESS", "自动错误恢复成功").subscribe();
                })
                .onErrorResume(error -> {
                    failedRecoveries.incrementAndGet();
                    LoggingUtil.error(logger, "自动错误恢复失败 - 操作: {}, 错误: {}", operationName, error.getMessage(), error);
                    return activateDegradationMode(operationName, error);
                });
    }

    /**
     * 执行背压恢复
     */
    @Override
    public <T> Mono<T> performBackpressureRecovery(Mono<T> operation, String operationName) {
        totalRecoveryAttempts.incrementAndGet();

        LoggingUtil.info(logger, "开始背压恢复 - 操作: {}", operationName);

        return operation
                .delayElement(Duration.ofSeconds(2)) // 延迟执行，减少系统压力
                .retryWhen(createBackpressureRetrySpec(operationName))
                .doOnSuccess(result -> {
                    successfulRecoveries.incrementAndGet();
                    LoggingUtil.info(logger, "背压恢复成功 - 操作: {}", operationName);
                    createRecoveryLog(operationName, "SUCCESS", "背压恢复成功").subscribe();
                })
                .onErrorResume(error -> {
                    failedRecoveries.incrementAndGet();
                    LoggingUtil.warn(logger, "背压恢复失败 - 操作: {}, 错误: {}", operationName, error.getMessage());
                    return activateBackpressureDegradation(operationName, error);
                });
    }

    /**
     * 执行流处理恢复
     */
    @Override
    public <T> Flux<T> performStreamingRecovery(Flux<T> stream, String operationName) {
        totalRecoveryAttempts.incrementAndGet();

        LoggingUtil.info(logger, "开始流处理恢复 - 操作: {}", operationName);

        return stream
                .retryWhen(createStreamingRetrySpec(operationName))
                .doOnComplete(() -> {
                    successfulRecoveries.incrementAndGet();
                    LoggingUtil.info(logger, "流处理恢复成功 - 操作: {}", operationName);
                    createRecoveryLog(operationName, "SUCCESS", "流处理恢复成功").subscribe();
                })
                .onErrorResume(error -> {
                    failedRecoveries.incrementAndGet();
                    LoggingUtil.error(logger, "流处理恢复失败 - 操作: {}, 错误: {}", operationName, error.getMessage(), error);
                    return activateStreamingDegradation(operationName, error);
                });
    }

    // ==================== 降级机制实现 ====================

    /**
     * 激活降级模式
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> activateDegradationMode(String operationName, Throwable error) {
        degradationActivations.incrementAndGet();

        LoggingUtil.warn(logger, "激活降级模式 - 操作: {}, 错误: {}", operationName, error.getMessage());

        return createRecoveryLog(operationName, "WARN", "激活降级模式: " + error.getMessage())
                .then(Mono.fromCallable(() -> {
                    // 根据操作类型返回不同的降级响应
                    if (operationName.toLowerCase().contains("user")) {
                        return (T) createDegradedUserResponse(operationName);
                    } else if (operationName.toLowerCase().contains("data")) {
                        return (T) createDegradedDataResponse(operationName);
                    } else if (operationName.toLowerCase().contains("auth")) {
                        return (T) createDegradedAuthResponse(operationName);
                    } else {
                        return (T) createGenericDegradedResponse(operationName);
                    }
                }));
    }

    /**
     * 激活背压降级
     */
    @SuppressWarnings("unchecked")
    private <T> Mono<T> activateBackpressureDegradation(String operationName, Throwable error) {
        degradationActivations.incrementAndGet();

        LoggingUtil.warn(logger, "激活背压降级 - 操作: {}", operationName);

        return (Mono<T>) Mono.just(createBackpressureDegradedResponse(operationName));
    }

    /**
     * 激活流处理降级
     */
    private <T> Flux<T> activateStreamingDegradation(String operationName, Throwable error) {
        degradationActivations.incrementAndGet();

        LoggingUtil.warn(logger, "激活流处理降级 - 操作: {}", operationName);

        return Flux.empty(); // 返回空流作为降级策略
    }

    // ==================== 熔断器实现 ====================

    /**
     * 激活熔断器
     */
    @Override
    public <T> Mono<T> activateCircuitBreaker(String operationName, Throwable error) {
        circuitBreakerTrips.incrementAndGet();

        LoggingUtil.error(logger, "激活熔断器 - 操作: {}, 错误: {}", operationName, error.getMessage());

        return createRecoveryLog(operationName, "ERROR", "激活熔断器: " + error.getMessage())
                .then(Mono.error(new RuntimeException("熔断器已激活，服务暂时不可用")));
    }

    // ==================== 重试策略创建 ====================

    /**
     * 创建重试规范
     * 【统一配置管理原则】使用配置化的重试参数，避免硬编码值
     */
    private Retry createRetrySpec(String operationName) {
        return Retry
                .backoff(errorRecoveryProperties.getMaxRetryAttempts(), errorRecoveryProperties.getInitialRetryDelay())
                .maxBackoff(errorRecoveryProperties.getMaxRetryDelay())
                .jitter(errorRecoveryProperties.getJitterFactor())
                .filter(this::isRetryableException)
                .doBeforeRetry(retrySignal -> {
                    LoggingUtil.warn(logger, "重试操作 - 操作: {}, 重试次数: {}, 错误: {}",
                            operationName, retrySignal.totalRetries() + 1, retrySignal.failure().getMessage());
                });
    }

    /**
     * 创建背压重试规范
     * 【统一配置管理原则】使用配置化的重试参数，避免硬编码值
     */
    private Retry createBackpressureRetrySpec(String operationName) {
        return Retry
                .backoff(errorRecoveryProperties.getBackpressureRetryAttempts(),
                        errorRecoveryProperties.getBackpressureInitialDelay())
                .maxBackoff(errorRecoveryProperties.getBackpressureMaxDelay())
                .jitter(errorRecoveryProperties.getBackpressureJitter())
                .filter(error -> error instanceof BackpressureException)
                .doBeforeRetry(retrySignal -> {
                    LoggingUtil.warn(logger, "背压重试 - 操作: {}, 重试次数: {}",
                            operationName, retrySignal.totalRetries() + 1);
                });
    }

    /**
     * 创建流处理重试规范
     */
    private Retry createStreamingRetrySpec(String operationName) {
        return Retry
                .backoff(errorRecoveryProperties.getStreamingRetryAttempts(),
                        errorRecoveryProperties.getStreamingInitialDelay())
                .maxBackoff(errorRecoveryProperties.getStreamingMaxDelay())
                .filter(error -> error instanceof StreamingException)
                .doBeforeRetry(retrySignal -> {
                    LoggingUtil.warn(logger, "流处理重试 - 操作: {}, 重试次数: {}",
                            operationName, retrySignal.totalRetries() + 1);
                });
    }

    // ==================== 降级响应创建 ====================

    /**
     * 创建降级用户响应
     */
    private Map<String, Object> createDegradedUserResponse(String operationName) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "DEGRADED");
        response.put("message", "用户服务暂时不可用，已启用降级模式");
        response.put("operation", operationName);
        response.put("degradedAt", LocalDateTime.now());
        response.put("fallbackData", createUserFallbackData());
        return response;
    }

    /**
     * 创建降级数据响应
     */
    private Map<String, Object> createDegradedDataResponse(String operationName) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "DEGRADED");
        response.put("message", "数据服务暂时不可用，已启用降级模式");
        response.put("operation", operationName);
        response.put("degradedAt", LocalDateTime.now());
        response.put("fallbackData", createDataFallbackData());
        return response;
    }

    /**
     * 创建降级认证响应
     */
    private Map<String, Object> createDegradedAuthResponse(String operationName) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "DEGRADED");
        response.put("message", "认证服务暂时不可用，已启用降级模式");
        response.put("operation", operationName);
        response.put("degradedAt", LocalDateTime.now());
        response.put("fallbackAuth", false);
        return response;
    }

    /**
     * 创建通用降级响应
     */
    private Map<String, Object> createGenericDegradedResponse(String operationName) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "DEGRADED");
        response.put("message", "服务暂时不可用，已启用降级模式");
        response.put("operation", operationName);
        response.put("degradedAt", LocalDateTime.now());
        return response;
    }

    /**
     * 创建背压降级响应
     */
    private Map<String, Object> createBackpressureDegradedResponse(String operationName) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "DEGRADED");
        response.put("message", "系统负载过高，已启用背压降级模式");
        response.put("operation", operationName);
        response.put("degradedAt", LocalDateTime.now());
        response.put("retryAfter", 30); // 建议30秒后重试
        return response;
    }

    // ==================== 回退数据创建 ====================

    /**
     * 创建用户回退数据
     */
    private Map<String, Object> createUserFallbackData() {
        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put("id", -1);
        fallbackData.put("username", "fallback_user");
        fallbackData.put("status", "DEGRADED");
        fallbackData.put("message", "用户数据暂时不可用");
        return fallbackData;
    }

    /**
     * 创建数据回退数据
     */
    private Map<String, Object> createDataFallbackData() {
        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put("data", new HashMap<>());
        fallbackData.put("status", "DEGRADED");
        fallbackData.put("message", "数据暂时不可用");
        fallbackData.put("cached", false);
        return fallbackData;
    }

    // ==================== 异常类型判断 ====================

    /**
     * 判断是否为可重试异常
     */
    private boolean isRetryableException(Throwable error) {
        return error instanceof ReactiveException ||
                error instanceof IllegalArgumentException ||
                error instanceof RuntimeException ||
                error.getMessage().contains("timeout") ||
                error.getMessage().contains("connection");
    }

    // ==================== 恢复统计方法 ====================

    /**
     * 获取错误恢复统计信息
     */
    @Override
    public Mono<Map<String, Object>> getRecoveryStatistics() {
        return Mono.fromCallable(() -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRecoveryAttempts", totalRecoveryAttempts.get());
            stats.put("successfulRecoveries", successfulRecoveries.get());
            stats.put("failedRecoveries", failedRecoveries.get());
            stats.put("degradationActivations", degradationActivations.get());
            stats.put("circuitBreakerTrips", circuitBreakerTrips.get());
            stats.put("recoverySuccessRate", calculateRecoverySuccessRate());
            stats.put("degradationRate", calculateDegradationRate());
            stats.put("timestamp", LocalDateTime.now());
            return stats;
        });
    }

    /**
     * 重置恢复统计
     */
    @Override
    public Mono<Void> resetRecoveryStatistics() {
        return Mono.fromRunnable(() -> {
            totalRecoveryAttempts.set(0);
            successfulRecoveries.set(0);
            failedRecoveries.set(0);
            degradationActivations.set(0);
            circuitBreakerTrips.set(0);
            LoggingUtil.info(logger, "错误恢复统计已重置");
        });
    }

    // ==================== 工具方法 ====================

    /**
     * 计算恢复成功率
     */
    private double calculateRecoverySuccessRate() {
        long total = totalRecoveryAttempts.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) successfulRecoveries.get() / total * 100;
    }

    /**
     * 计算降级率
     */
    private double calculateDegradationRate() {
        long total = totalRecoveryAttempts.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) degradationActivations.get() / total * 100;
    }

    /**
     * 创建恢复日志
     */
    private Mono<Void> createRecoveryLog(String operation, String level, String message) {
        // 使用文件日志替代数据库写入，符合监控数据文件日志规范
        return Mono.fromRunnable(() -> {
            try {
                // 使用MonitoringLogUtil记录错误恢复事件到文件
                MonitoringLogUtil.logSystemEvent(
                        "ERROR_RECOVERY",
                        operation != null ? operation : "RECOVERY",
                        message != null ? message : "错误恢复事件");
                LoggingUtil.debug(logger, "错误恢复日志文件记录成功: {}", operation);
            } catch (Exception e) {
                LoggingUtil.error(logger, "错误恢复日志文件记录失败", e);
            }
        })
                .subscribeOn(Schedulers.boundedElastic())
                .then()
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "保存恢复日志失败", error);
                    return Mono.empty();
                });
    }
}
