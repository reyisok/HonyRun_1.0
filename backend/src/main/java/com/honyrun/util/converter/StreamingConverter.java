package com.honyrun.util.converter;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 流式转换器
 *
 * 提供流式数据的转换和映射功能，支持背压控制和流式处理
 * 专门针对Flux数据流的高效转换操作
 *
 * 主要功能：
 * - 流式数据转换
 * - 分批处理转换
 * - 并行转换处理
 * - 流式过滤转换
 * - 流式聚合转换
 *
 * 响应式特性：
 * - 背压支持：自动处理背压，防止内存溢出
 * - 流式处理：支持大数据量的流式转换
 * - 并行处理：支持并行转换提高性能
 * - 错误隔离：单个元素转换失败不影响整个流
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 00:15:00
 * @modified 2025-07-01 00:15:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class StreamingConverter {

    private static final Logger logger = LoggerFactory.getLogger(StreamingConverter.class);

    /**
     * 流式转换
     *
     * @param source 源数据流
     * @param converter 转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的数据流
     */
    public <S, T> Flux<T> convertStream(Flux<S> source, Function<S, T> converter) {
        LoggingUtil.debug(logger, "开始执行流式转换");

        return source
                .map(converter)
                .doOnNext(item -> LoggingUtil.debug(logger, "流式转换处理一个元素"))
                .doOnComplete(() -> LoggingUtil.debug(logger, "流式转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "流式转换失败", error))
                .onErrorContinue((error, item) -> {
                    LoggingUtil.error(logger, "流式转换单个元素失败，继续处理其他元素: {}", item, error);
                });
    }

    /**
     * 分批流式转换
     *
     * @param source 源数据流
     * @param converter 转换函数
     * @param batchSize 批次大小
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的数据流
     */
    public <S, T> Flux<T> convertStreamBatch(Flux<S> source, Function<S, T> converter, int batchSize) {
        LoggingUtil.debug(logger, "开始执行分批流式转换，批次大小: {}", batchSize);

        return source
                .buffer(batchSize)
                .flatMap(batch -> {
                    LoggingUtil.debug(logger, "处理批次，大小: {}", batch.size());
                    return Flux.fromIterable(batch)
                            .map(converter)
                            .onErrorContinue((error, item) -> {
                                LoggingUtil.error(logger, "批次转换单个元素失败: {}", item, error);
                            });
                })
                .doOnComplete(() -> LoggingUtil.debug(logger, "分批流式转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "分批流式转换失败", error));
    }

    /**
     * 并行流式转换
     *
     * @param source 源数据流
     * @param converter 转换函数
     * @param parallelism 并行度
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的数据流
     */
    public <S, T> Flux<T> convertStreamParallel(Flux<S> source, Function<S, T> converter, int parallelism) {
        LoggingUtil.debug(logger, "开始执行并行流式转换，并行度: {}", parallelism);

        return source
                .parallel(parallelism)
                .map(converter)
                .doOnNext(item -> LoggingUtil.debug(logger, "并行转换处理一个元素"))
                .doOnError(error -> LoggingUtil.error(logger, "并行转换失败", error))
                .sequential()
                .doOnComplete(() -> LoggingUtil.debug(logger, "并行流式转换完成"));
    }

    /**
     * 流式过滤转换
     *
     * @param source 源数据流
     * @param filter 过滤条件
     * @param converter 转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的数据流
     */
    public <S, T> Flux<T> convertStreamWithFilter(Flux<S> source, Predicate<S> filter, Function<S, T> converter) {
        LoggingUtil.debug(logger, "开始执行流式过滤转换");

        return source
                .filter(filter)
                .map(converter)
                .doOnNext(item -> LoggingUtil.debug(logger, "过滤转换处理一个元素"))
                .doOnComplete(() -> LoggingUtil.debug(logger, "流式过滤转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "流式过滤转换失败", error))
                .onErrorContinue((error, item) -> {
                    LoggingUtil.error(logger, "过滤转换单个元素失败: {}", item, error);
                });
    }

    /**
     * 流式异步转换
     *
     * @param source 源数据流
     * @param asyncConverter 异步转换函数
     * @param concurrency 并发数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的数据流
     */
    public <S, T> Flux<T> convertStreamAsync(Flux<S> source, Function<S, Mono<T>> asyncConverter, int concurrency) {
        LoggingUtil.debug(logger, "开始执行流式异步转换，并发数: {}", concurrency);

        return source
                .flatMap(asyncConverter, concurrency)
                .doOnNext(item -> LoggingUtil.debug(logger, "异步转换处理一个元素"))
                .doOnComplete(() -> LoggingUtil.debug(logger, "流式异步转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "流式异步转换失败", error))
                .onErrorContinue((error, item) -> {
                    LoggingUtil.error(logger, "异步转换单个元素失败: {}", item, error);
                });
    }

    /**
     * 流式窗口转换
     *
     * @param source 源数据流
     * @param windowSize 窗口大小
     * @param converter 窗口转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的数据流
     */
    public <S, T> Flux<T> convertStreamWindow(Flux<S> source, int windowSize, Function<Flux<S>, Mono<T>> converter) {
        LoggingUtil.debug(logger, "开始执行流式窗口转换，窗口大小: {}", windowSize);

        return source
                .window(windowSize)
                .flatMap(window -> {
                    LoggingUtil.debug(logger, "处理一个窗口");
                    return converter.apply(window)
                            .onErrorResume(error -> {
                                LoggingUtil.error(logger, "窗口转换失败", error);
                                return Mono.empty();
                            });
                })
                .doOnComplete(() -> LoggingUtil.debug(logger, "流式窗口转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "流式窗口转换失败", error));
    }

    /**
     * 流式时间窗口转换
     *
     * @param source 源数据流
     * @param duration 时间窗口长度
     * @param converter 窗口转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的数据流
     */
    public <S, T> Flux<T> convertStreamTimeWindow(Flux<S> source, Duration duration, Function<Flux<S>, Mono<T>> converter) {
        LoggingUtil.debug(logger, "开始执行流式时间窗口转换，窗口时长: {}", duration);

        return source
                .window(duration)
                .flatMap(window -> {
                    LoggingUtil.debug(logger, "处理一个时间窗口");
                    return converter.apply(window)
                            .onErrorResume(error -> {
                                LoggingUtil.error(logger, "时间窗口转换失败", error);
                                return Mono.empty();
                            });
                })
                .doOnComplete(() -> LoggingUtil.debug(logger, "流式时间窗口转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "流式时间窗口转换失败", error));
    }

    /**
     * 流式分组转换
     *
     * @param source 源数据流
     * @param keySelector 分组键选择器
     * @param groupConverter 分组转换函数
     * @param <S> 源类型
     * @param <K> 键类型
     * @param <T> 目标类型
     * @return 转换后的数据流
     */
    public <S, K, T> Flux<T> convertStreamGroupBy(Flux<S> source, Function<S, K> keySelector,
                                                  Function<Flux<S>, Mono<T>> groupConverter) {
        LoggingUtil.debug(logger, "开始执行流式分组转换");

        return source
                .groupBy(keySelector)
                .flatMap(group -> {
                    LoggingUtil.debug(logger, "处理分组，键: {}", group.key());
                    return groupConverter.apply(group)
                            .onErrorResume(error -> {
                                LoggingUtil.error(logger, "分组转换失败，键: {}", group.key(), error);
                                return Mono.empty();
                            });
                })
                .doOnComplete(() -> LoggingUtil.debug(logger, "流式分组转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "流式分组转换失败", error));
    }

    /**
     * 流式聚合转换
     *
     * @param source 源数据流
     * @param converter 元素转换函数
     * @param accumulator 聚合函数
     * @param <S> 源类型
     * @param <T> 转换类型
     * @param <R> 结果类型
     * @return 聚合结果的Mono
     */
    public <S, T, R> Mono<R> convertStreamAggregate(Flux<S> source, Function<S, T> converter,
                                                    Function<Flux<T>, Mono<R>> accumulator) {
        LoggingUtil.debug(logger, "开始执行流式聚合转换");

        return source
                .map(converter)
                .as(accumulator)
                .doOnSuccess(result -> LoggingUtil.debug(logger, "流式聚合转换成功"))
                .doOnError(error -> LoggingUtil.error(logger, "流式聚合转换失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "聚合转换错误，返回空结果", error);
                    return Mono.empty();
                });
    }

    /**
     * 流式限流转换
     *
     * @param source 源数据流
     * @param converter 转换函数
     * @param requestsPerSecond 每秒请求数限制
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的数据流
     */
    public <S, T> Flux<T> convertStreamWithRateLimit(Flux<S> source, Function<S, T> converter, int requestsPerSecond) {
        LoggingUtil.debug(logger, "开始执行流式限流转换，限制: {}请求/秒", requestsPerSecond);

        Duration interval = Duration.ofMillis(1000 / requestsPerSecond);

        return source
                .delayElements(interval)
                .map(converter)
                .doOnNext(item -> LoggingUtil.debug(logger, "限流转换处理一个元素"))
                .doOnComplete(() -> LoggingUtil.debug(logger, "流式限流转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "流式限流转换失败", error))
                .onErrorContinue((error, item) -> {
                    LoggingUtil.error(logger, "限流转换单个元素失败: {}", item, error);
                });
    }

    /**
     * 流式重试转换
     *
     * @param source 源数据流
     * @param converter 转换函数
     * @param maxRetries 最大重试次数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的数据流
     */
    public <S, T> Flux<T> convertStreamWithRetry(Flux<S> source, Function<S, T> converter, long maxRetries) {
        LoggingUtil.debug(logger, "开始执行流式重试转换，最大重试次数: {}", maxRetries);

        return source
                .map(converter)
                .retry(maxRetries)
                .doOnNext(item -> LoggingUtil.debug(logger, "重试转换处理一个元素"))
                .doOnComplete(() -> LoggingUtil.debug(logger, "流式重试转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "流式重试转换最终失败", error))
                .onErrorContinue((error, item) -> {
                    LoggingUtil.error(logger, "重试转换单个元素最终失败: {}", item, error);
                });
    }

    /**
     * 流式缓存转换
     *
     * @param source 源数据流
     * @param converter 转换函数
     * @param cacheSize 缓存大小
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的数据流
     */
    public <S, T> Flux<T> convertStreamWithCache(Flux<S> source, Function<S, T> converter, int cacheSize) {
        LoggingUtil.debug(logger, "开始执行流式缓存转换，缓存大小: {}", cacheSize);

        return source
                .map(converter)
                .cache(cacheSize)
                .doOnNext(item -> LoggingUtil.debug(logger, "缓存转换处理一个元素"))
                .doOnComplete(() -> LoggingUtil.debug(logger, "流式缓存转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "流式缓存转换失败", error));
    }
}

