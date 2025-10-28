package com.honyrun.util.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.honyrun.util.LoggingUtil;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 统一响应式工具类
 * 整合了原有的ReactiveUtil、FluxUtil、MonoUtil功能
 * 提供Mono/Flux操作辅助方法、错误处理工具、批处理工具等
 *
 * @author Mr.Rey
 * @version 3.0.0
 * @created 2025-07-01  15:30:00
 * @modified 2025-07-02 21:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class ReactiveUtil {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveUtil.class);

    /**
     * 默认超时时间（秒）
     */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /**
     * 默认重试次数
     */
    public static final int DEFAULT_RETRY_ATTEMPTS = 3;

    /**
     * 默认重试延迟（毫秒）
     */
    public static final Duration DEFAULT_RETRY_DELAY = Duration.ofMillis(500);

    /**
     * 默认批处理大小
     */
    public static final int DEFAULT_BATCH_SIZE = 100;

    /**
     * 默认缓冲区大小
     */
    public static final int DEFAULT_BUFFER_SIZE = 256;

    private ReactiveUtil() {
        // 工具类，禁止实例化
    }

    // ==================== 重试处理 ====================

    /**
     * 为Mono添加默认重试处理
     *
     * @param mono 原始Mono
     * @param <T> 数据类型
     * @return 带重试处理的Mono
     */
    public static <T> Mono<T> withRetry(Mono<T> mono) {
        return withRetry(mono, DEFAULT_RETRY_ATTEMPTS, DEFAULT_RETRY_DELAY);
    }

    /**
     * 为Mono添加指定重试处理
     *
     * @param mono 原始Mono
     * @param maxAttempts 最大重试次数
     * @param retryDelay 重试延迟
     * @param <T> 数据类型
     * @return 带重试处理的Mono
     */
    public static <T> Mono<T> withRetry(Mono<T> mono, int maxAttempts, Duration retryDelay) {
        if (maxAttempts <= 0) {
            return mono;
        }
        
        return mono.retryWhen(Retry.backoff(maxAttempts, retryDelay)
                .doBeforeRetry(retrySignal -> 
                    LoggingUtil.debug(logger, "Mono重试第{}次", retrySignal.totalRetries() + 1))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> 
                    retrySignal.failure()));
    }

    /**
     * 为Flux添加默认重试处理
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 带重试处理的Flux
     */
    public static <T> Flux<T> withRetry(Flux<T> flux) {
        return withRetry(flux, DEFAULT_RETRY_ATTEMPTS, DEFAULT_RETRY_DELAY);
    }

    /**
     * 为Flux添加指定重试处理
     *
     * @param flux 原始Flux
     * @param maxAttempts 最大重试次数
     * @param retryDelay 重试延迟
     * @param <T> 数据类型
     * @return 带重试处理的Flux
     */
    public static <T> Flux<T> withRetry(Flux<T> flux, int maxAttempts, Duration retryDelay) {
        if (maxAttempts <= 0) {
            return flux;
        }
        
        return flux.retryWhen(Retry.backoff(maxAttempts, retryDelay)
                .doBeforeRetry(retrySignal -> 
                    LoggingUtil.debug(logger, "Flux重试第{}次", retrySignal.totalRetries() + 1))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> 
                    retrySignal.failure()));
    }

    // ==================== 超时处理 ====================

    /**
     * 为Mono添加默认超时处理
     *
     * @param mono 原始Mono
     * @param <T> 数据类型
     * @return 带超时处理的Mono
     */
    public static <T> Mono<T> withTimeout(Mono<T> mono) {
        return withTimeout(mono, DEFAULT_TIMEOUT);
    }

    /**
     * 为Mono添加指定超时处理
     *
     * @param mono 原始Mono
     * @param timeout 超时时间
     * @param <T> 数据类型
     * @return 带超时处理的Mono
     */
    public static <T> Mono<T> withTimeout(Mono<T> mono, Duration timeout) {
        return mono.timeout(timeout)
                .doOnError(throwable -> LoggingUtil.warn(logger, "Mono操作超时", throwable));
    }

    /**
     * 为Flux添加默认超时处理
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 带超时处理的Flux
     */
    public static <T> Flux<T> withTimeout(Flux<T> flux) {
        return withTimeout(flux, DEFAULT_TIMEOUT);
    }

    /**
     * 为Flux添加指定超时处理
     *
     * @param flux 原始Flux
     * @param timeout 超时时间
     * @param <T> 数据类型
     * @return 带超时处理的Flux
     */
    public static <T> Flux<T> withTimeout(Flux<T> flux, Duration timeout) {
        return flux.timeout(timeout)
                .doOnError(throwable -> LoggingUtil.warn(logger, "Flux操作超时", throwable));
    }



    // ==================== 错误处理 ====================

    /**
     * 为Mono添加错误恢复机制
     *
     * @param mono 原始Mono
     * @param fallback 错误恢复函数
     * @param <T> 数据类型
     * @return 带错误恢复的Mono
     */
    public static <T> Mono<T> withFallback(Mono<T> mono, Function<Throwable, Mono<T>> fallback) {
        return mono.onErrorResume(throwable -> {
            LoggingUtil.warn(logger, "Mono操作发生错误，执行fallback", throwable);
            return fallback.apply(throwable);
        });
    }

    /**
     * 为Flux添加错误恢复机制
     *
     * @param flux 原始Flux
     * @param fallback 错误恢复函数
     * @param <T> 数据类型
     * @return 带错误恢复的Flux
     */
    public static <T> Flux<T> withFallback(Flux<T> flux, Function<Throwable, Flux<T>> fallback) {
        return flux.onErrorResume(throwable -> {
            LoggingUtil.warn(logger, "Flux操作发生错误，执行fallback", throwable);
            return fallback.apply(throwable);
        });
    }

    // ==================== Mono工具方法（整合自MonoUtil） ====================

    /**
     * 从Optional创建Mono
     *
     * @param optional Optional对象
     * @param <T> 数据类型
     * @return Mono对象
     */
    public static <T> Mono<T> fromOptional(Optional<T> optional) {
        return optional.map(Mono::just).orElse(Mono.empty());
    }

    /**
     * 从Callable创建Mono
     *
     * @param callable Callable对象
     * @param <T> 数据类型
     * @return Mono对象
     */
    public static <T> Mono<T> fromCallable(Callable<T> callable) {
        return Mono.fromCallable(callable);
    }

    /**
     * 将Mono转换为Optional
     *
     * @param mono Mono对象
     * @param <T> 数据类型
     * @return Optional对象的Mono
     */
    public static <T> Mono<Optional<T>> toOptional(Mono<T> mono) {
        return mono.map(Optional::of)
                .defaultIfEmpty(Optional.empty());
    }

    /**
     * 条件执行Mono操作
     *
     * @param condition 条件
     * @param monoSupplier Mono供应商
     * @param <T> 数据类型
     * @return 条件执行结果
     */
    public static <T> Mono<T> conditionalMono(boolean condition, Supplier<Mono<T>> monoSupplier) {
        return condition ? monoSupplier.get() : Mono.empty();
    }

    /**
     * 条件映射
     * 如果条件为真，则应用映射函数，否则返回原值
     *
     * @param mono 原始Mono
     * @param condition 条件
     * @param mapper 映射函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 映射后的Mono
     */
    public static <T, R> Mono<R> mapIf(Mono<T> mono, Predicate<T> condition, Function<T, R> mapper) {
        return mono.flatMap(value -> {
            if (condition.test(value)) {
                return Mono.just(mapper.apply(value));
            } else {
                return Mono.empty();
            }
        });
    }

    /**
     * 条件过滤Mono
     *
     * @param mono 原始Mono
     * @param predicate 过滤条件
     * @param <T> 数据类型
     * @return 过滤后的Mono
     */
    public static <T> Mono<T> filterMono(Mono<T> mono, Predicate<T> predicate) {
        return mono.filter(predicate);
    }

    /**
     * 条件过滤Flux
     *
     * @param flux 原始Flux
     * @param predicate 过滤条件
     * @param <T> 数据类型
     * @return 过滤后的Flux
     */
    public static <T> Flux<T> filterFlux(Flux<T> flux, Predicate<T> predicate) {
        return flux.filter(predicate);
    }

    /**
     * 默认值处理
     * 如果Mono为空，则使用默认值
     *
     * @param mono 原始Mono
     * @param defaultValue 默认值
     * @param <T> 数据类型
     * @return 带默认值的Mono
     */
    public static <T> Mono<T> defaultIfEmpty(Mono<T> mono, T defaultValue) {
        return mono.defaultIfEmpty(defaultValue);
    }

    /**
     * 默认值供应商处理
     * 如果Mono为空，则使用默认值供应商
     *
     * @param mono 原始Mono
     * @param defaultSupplier 默认值供应商
     * @param <T> 数据类型
     * @return 带默认值的Mono
     */
    public static <T> Mono<T> defaultIfEmptySupplier(Mono<T> mono, Supplier<T> defaultSupplier) {
        return mono.switchIfEmpty(Mono.fromSupplier(defaultSupplier));
    }

    /**
     * 空值检查
     * 检查Mono是否为空
     *
     * @param mono 原始Mono
     * @param <T> 数据类型
     * @return 是否为空的Mono
     */
    public static <T> Mono<Boolean> isEmpty(Mono<T> mono) {
        return mono.hasElement().map(hasElement -> !hasElement);
    }

    /**
     * 非空检查
     * 检查Mono是否非空
     *
     * @param mono 原始Mono
     * @param <T> 数据类型
     * @return 是否非空的Mono
     */
    public static <T> Mono<Boolean> isNotEmpty(Mono<T> mono) {
        return mono.hasElement();
    }

    // ==================== Flux工具方法（整合自FluxUtil） ====================

    /**
     * 从集合创建Flux
     *
     * @param collection 集合
     * @param <T> 数据类型
     * @return Flux对象
     */
    public static <T> Flux<T> fromCollection(Collection<T> collection) {
        return collection != null ? Flux.fromIterable(collection) : Flux.empty();
    }

    /**
     * 从Iterable创建Flux
     *
     * @param iterable Iterable对象
     * @param <T> 数据类型
     * @return Flux对象
     */
    public static <T> Flux<T> fromIterable(Iterable<T> iterable) {
        return iterable != null ? Flux.fromIterable(iterable) : Flux.empty();
    }

    /**
     * 批处理操作
     * 将Flux分批处理，每批指定大小
     *
     * @param flux 原始Flux
     * @param batchSize 批处理大小
     * @param <T> 数据类型
     * @return 批处理后的Flux
     */
    public static <T> Flux<List<T>> batch(Flux<T> flux, int batchSize) {
        return flux.buffer(batchSize);
    }

    /**
     * 默认批处理操作
     * 使用默认批处理大小
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 批处理后的Flux
     */
    public static <T> Flux<List<T>> batch(Flux<T> flux) {
        return batch(flux, DEFAULT_BATCH_SIZE);
    }

    /**
     * 时间窗口批处理
     * 在指定时间窗口内收集元素
     *
     * @param flux 原始Flux
     * @param timespan 时间窗口
     * @param <T> 数据类型
     * @return 时间窗口批处理后的Flux
     */
    public static <T> Flux<List<T>> batchByTime(Flux<T> flux, Duration timespan) {
        return flux.bufferTimeout(DEFAULT_BATCH_SIZE, timespan);
    }

    /**
     * 并行处理
     * 将Flux转换为并行处理
     *
     * @param flux 原始Flux
     * @param parallelism 并行度
     * @param <T> 数据类型
     * @return 并行处理的Flux
     */
    public static <T> Flux<T> parallel(Flux<T> flux, int parallelism) {
        return flux.parallel(parallelism).runOn(reactor.core.scheduler.Schedulers.parallel()).sequential();
    }

    /**
     * 默认并行处理
     * 使用默认并行度（CPU核心数）
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 并行处理的Flux
     */
    public static <T> Flux<T> parallel(Flux<T> flux) {
        return flux.parallel().runOn(reactor.core.scheduler.Schedulers.parallel()).sequential();
    }

    /**
     * 非空过滤
     * 过滤掉空值
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 过滤后的Flux
     */
    public static <T> Flux<T> filterNotNull(Flux<T> flux) {
        return flux.filter(item -> item != null);
    }

    /**
     * 去重
     * 去除重复元素
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 去重后的Flux
     */
    public static <T> Flux<T> distinct(Flux<T> flux) {
        return flux.distinct();
    }

    /**
     * 根据键去重
     * 根据指定键函数去除重复元素
     *
     * @param flux 原始Flux
     * @param keySelector 键选择函数
     * @param <T> 数据类型
     * @param <K> 键类型
     * @return 去重后的Flux
     */
    public static <T, K> Flux<T> distinctByKey(Flux<T> flux, Function<T, K> keySelector) {
        return flux.distinct(keySelector);
    }

    /**
     * 限制元素数量
     * 限制Flux发出的元素数量
     *
     * @param flux 原始Flux
     * @param count 限制数量
     * @param <T> 数据类型
     * @return 限制后的Flux
     */
    public static <T> Flux<T> limit(Flux<T> flux, long count) {
        return flux.take(count);
    }

    /**
     * 跳过元素
     * 跳过指定数量的元素
     *
     * @param flux 原始Flux
     * @param count 跳过数量
     * @param <T> 数据类型
     * @return 跳过后的Flux
     */
    public static <T> Flux<T> skip(Flux<T> flux, long count) {
        return flux.skip(count);
    }

    /**
     * 分页处理
     * 实现分页功能
     *
     * @param flux 原始Flux
     * @param page 页码（从0开始）
     * @param size 页大小
     * @param <T> 数据类型
     * @return 分页后的Flux
     */
    public static <T> Flux<T> page(Flux<T> flux, int page, int size) {
        return flux.skip((long) page * size).take(size);
    }

    /**
     * 收集为列表
     * 将Flux收集为List的Mono
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 包含List的Mono
     */
    public static <T> Mono<List<T>> collectToList(Flux<T> flux) {
        return flux.collectList();
    }

    /**
     * 计数
     * 计算Flux中元素的数量
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 包含计数的Mono
     */
    public static <T> Mono<Long> count(Flux<T> flux) {
        return flux.count();
    }

    /**
     * 检查Flux是否为空
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 是否为空的Mono
     */
    public static <T> Mono<Boolean> isFluxEmpty(Flux<T> flux) {
        return flux.hasElements().map(hasElements -> !hasElements);
    }

    /**
     * 检查Flux是否非空
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 是否非空的Mono
     */
    public static <T> Mono<Boolean> isFluxNotEmpty(Flux<T> flux) {
        return flux.hasElements();
    }

    // ==================== 调试和日志 ====================

    /**
     * 记录Mono执行时间
     *
     * @param mono 原始Mono
     * @param operationName 操作名称
     * @param <T> 数据类型
     * @return 带时间记录的Mono
     */
    public static <T> Mono<T> logExecutionTime(Mono<T> mono, String operationName) {
        return mono
                .doOnSubscribe(subscription -> LoggingUtil.debug(logger, "开始执行操作: " + operationName))
                .elapsed()
                .doOnNext(tuple -> LoggingUtil.debug(logger, "操作 {} 执行完成，耗时: {}ms", operationName, tuple.getT1()))
                .map(tuple -> tuple.getT2());
    }

    /**
     * 记录Flux执行时间
     *
     * @param flux 原始Flux
     * @param operationName 操作名称
     * @param <T> 数据类型
     * @return 带时间记录的Flux
     */
    public static <T> Flux<T> logExecutionTime(Flux<T> flux, String operationName) {
        return flux
                .doOnSubscribe(subscription -> LoggingUtil.debug(logger, "开始执行操作: " + operationName))
                .elapsed()
                .doOnNext(tuple -> LoggingUtil.debug(logger, "操作 {} 处理元素，耗时: {}ms", operationName, tuple.getT1()))
                .map(tuple -> tuple.getT2())
                .doOnComplete(() -> LoggingUtil.debug(logger, "操作 {} 执行完成", operationName));
    }

    /**
     * 记录调试信息
     * 记录Mono的值用于调试
     *
     * @param mono 原始Mono
     * @param message 调试消息
     * @param <T> 数据类型
     * @return 原始Mono
     */
    public static <T> Mono<T> debug(Mono<T> mono, String message) {
        return mono.doOnNext(value -> LoggingUtil.debug(logger, message + ": " + value));
    }

    /**
     * 记录调试信息
     * 记录Flux的值用于调试
     *
     * @param flux 原始Flux
     * @param message 调试消息
     * @param <T> 数据类型
     * @return 原始Flux
     */
    public static <T> Flux<T> debug(Flux<T> flux, String message) {
        return flux.doOnNext(value -> LoggingUtil.debug(logger, message + ": " + value));
    }

    /**
     * 记录错误信息
     * 记录Mono的错误用于调试
     *
     * @param mono 原始Mono
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 原始Mono
     */
    public static <T> Mono<T> debugError(Mono<T> mono, String message) {
        return mono.doOnError(error -> LoggingUtil.error(logger, message + ": " + error.getMessage(), error));
    }

    /**
     * 记录错误信息
     * 记录Flux的错误用于调试
     *
     * @param flux 原始Flux
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 原始Flux
     */
    public static <T> Flux<T> debugError(Flux<T> flux, String message) {
        return flux.doOnError(error -> LoggingUtil.error(logger, message + ": " + error.getMessage(), error));
    }

    // ==================== 缓存和延迟 ====================

    /**
     * 缓存Mono结果
     * 缓存Mono的结果，避免重复计算
     *
     * @param mono 原始Mono
     * @param <T> 数据类型
     * @return 缓存的Mono
     */
    public static <T> Mono<T> cache(Mono<T> mono) {
        return mono.cache();
    }

    /**
     * 缓存Flux结果
     * 缓存Flux的结果，避免重复计算
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 缓存的Flux
     */
    public static <T> Flux<T> cache(Flux<T> flux) {
        return flux.cache();
    }

    /**
     * 延迟Mono执行
     * 延迟Mono的执行
     *
     * @param mono 原始Mono
     * @param delay 延迟时间
     * @param <T> 数据类型
     * @return 延迟的Mono
     */
    public static <T> Mono<T> delay(Mono<T> mono, Duration delay) {
        return mono.delayElement(delay);
    }

    /**
     * 延迟Flux执行
     * 延迟Flux的执行
     *
     * @param flux 原始Flux
     * @param delay 延迟时间
     * @param <T> 数据类型
     * @return 延迟的Flux
     */
    public static <T> Flux<T> delay(Flux<T> flux, Duration delay) {
        return flux.delayElements(delay);
    }

    // ==================== 组合操作 ====================

    /**
     * 合并多个Flux
     * 同时处理多个Flux的数据
     *
     * @param fluxes Flux数组
     * @param <T> 数据类型
     * @return 合并后的Flux
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Flux<T> merge(Flux<T>... fluxes) {
        return Flux.merge(fluxes);
    }

    /**
     * 连接多个Flux
     * 按顺序连接多个Flux
     *
     * @param fluxes Flux数组
     * @param <T> 数据类型
     * @return 连接后的Flux
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Flux<T> concat(Flux<T>... fluxes) {
        return Flux.concat(fluxes);
    }

    /**
     * 压缩多个Flux
     * 将多个Flux的元素配对
     *
     * @param flux1 第一个Flux
     * @param flux2 第二个Flux
     * @param combiner 组合函数
     * @param <T1> 第一个Flux的类型
     * @param <T2> 第二个Flux的类型
     * @param <R> 结果类型
     * @return 压缩后的Flux
     */
    public static <T1, T2, R> Flux<R> zip(Flux<T1> flux1, Flux<T2> flux2, BiFunction<T1, T2, R> combiner) {
        return Flux.zip(flux1, flux2, combiner);
    }

    /**
     * 获取第一个有值的Mono
     *
     * @param monos Mono数组
     * @param <T> 数据类型
     * @return 合并后的Mono
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Mono<T> firstWithValue(Mono<T>... monos) {
        if (monos == null || monos.length == 0) {
            return Mono.empty();
        }
        if (monos.length == 1) {
            return monos[0];
        }
        return Mono.firstWithValue(monos[0], java.util.Arrays.copyOfRange(monos, 1, monos.length));
    }

    /**
     * 条件切换
     * 根据条件选择不同的Mono
     *
     * @param condition 条件
     * @param trueMono 条件为真时的Mono
     * @param falseMono 条件为假时的Mono
     * @param <T> 数据类型
     * @return 选择的Mono
     */
    public static <T> Mono<T> switchIf(boolean condition, Mono<T> trueMono, Mono<T> falseMono) {
        return condition ? trueMono : falseMono;
    }
}


