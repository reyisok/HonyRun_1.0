package com.honyrun.util.converter;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

/**
 * 响应式转换器
 *
 * 提供通用的响应式数据转换功能，支持Mono和Flux的数据转换
 * 采用函数式编程范式，支持链式转换操作
 *
 * 主要功能：
 * - Mono数据转换
 * - Flux数据转换
 * - 批量数据转换
 * - 条件转换
 * - 错误处理和恢复
 *
 * 响应式特性：
 * - 非阻塞转换：所有转换操作均为非阻塞
 * - 流式处理：支持数据流的链式转换
 * - 背压支持：转换过程支持背压控制
 * - 错误恢复：提供转换失败的恢复机制
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 00:10:00
 * @modified 2025-07-01 00:10:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class ReactiveConverter {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveConverter.class);

    /**
     * 转换单个对象
     *
     * @param source 源对象的Mono
     * @param converter 转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的目标对象Mono
     */
    public <S, T> Mono<T> convert(Mono<S> source, Function<S, T> converter) {
        LoggingUtil.debug(logger, "开始执行Mono对象转换");

        return source
                .map(converter)
                .doOnSuccess(result -> LoggingUtil.debug(logger, "Mono对象转换成功"))
                .doOnError(error -> LoggingUtil.error(logger, "Mono对象转换失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Mono转换错误，返回空结果", error);
                    return Mono.empty();
                });
    }

    /**
     * 转换对象流
     *
     * @param source 源对象的Flux
     * @param converter 转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的目标对象Flux
     */
    public <S, T> Flux<T> convert(Flux<S> source, Function<S, T> converter) {
        LoggingUtil.debug(logger, "开始执行Flux对象转换");

        return source
                .map(converter)
                .doOnComplete(() -> LoggingUtil.debug(logger, "Flux对象转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "Flux对象转换失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Flux转换错误，返回空流", error);
                    return Flux.empty();
                });
    }

    /**
     * 批量转换对象列表
     *
     * @param sourceList 源对象列表的Mono
     * @param converter 转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的目标对象列表Mono
     */
    public <S, T> Mono<List<T>> convertList(Mono<List<S>> sourceList, Function<S, T> converter) {
        LoggingUtil.debug(logger, "开始执行批量列表转换");

        return sourceList
                .flatMapMany(Flux::fromIterable)
                .map(converter)
                .collectList()
                .doOnSuccess(result -> LoggingUtil.debug(logger, "批量列表转换成功，转换数量: {}", result.size()))
                .doOnError(error -> LoggingUtil.error(logger, "批量列表转换失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "批量列表转换错误，返回空列表", error);
                    return Mono.just(List.of());
                });
    }

    /**
     * 条件转换
     *
     * @param source 源对象的Mono
     * @param condition 转换条件
     * @param converter 转换函数
     * @param defaultValue 默认值
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的目标对象Mono
     */
    public <S, T> Mono<T> convertIf(Mono<S> source, Function<S, Boolean> condition,
                                   Function<S, T> converter, T defaultValue) {
        LoggingUtil.debug(logger, "开始执行条件转换");

        return source
                .flatMap(sourceObj -> {
                    if (condition.apply(sourceObj)) {
                        LoggingUtil.debug(logger, "条件满足，执行转换");
                        return Mono.just(converter.apply(sourceObj));
                    } else {
                        LoggingUtil.debug(logger, "条件不满足，返回默认值");
                        return Mono.just(defaultValue);
                    }
                })
                .doOnSuccess(result -> LoggingUtil.debug(logger, "条件转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "条件转换失败", error))
                .onErrorReturn(defaultValue);
    }

    /**
     * 安全转换（带空值检查）
     *
     * @param source 源对象的Mono
     * @param converter 转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的目标对象Mono
     */
    public <S, T> Mono<T> convertSafely(Mono<S> source, Function<S, T> converter) {
        LoggingUtil.debug(logger, "开始执行安全转换");

        return source
                .filter(obj -> obj != null)
                .map(converter)
                .filter(result -> result != null)
                .doOnSuccess(result -> LoggingUtil.debug(logger, "安全转换成功"))
                .doOnError(error -> LoggingUtil.error(logger, "安全转换失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "安全转换错误，返回空结果", error);
                    return Mono.empty();
                });
    }

    /**
     * 链式转换
     *
     * @param source 源对象的Mono
     * @param firstConverter 第一个转换函数
     * @param secondConverter 第二个转换函数
     * @param <S> 源类型
     * @param <M> 中间类型
     * @param <T> 目标类型
     * @return 转换后的目标对象Mono
     */
    public <S, M, T> Mono<T> convertChain(Mono<S> source, Function<S, M> firstConverter,
                                         Function<M, T> secondConverter) {
        LoggingUtil.debug(logger, "开始执行链式转换");

        return source
                .map(firstConverter)
                .map(secondConverter)
                .doOnSuccess(result -> LoggingUtil.debug(logger, "链式转换成功"))
                .doOnError(error -> LoggingUtil.error(logger, "链式转换失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "链式转换错误，返回空结果", error);
                    return Mono.empty();
                });
    }

    /**
     * 异步转换
     *
     * @param source 源对象的Mono
     * @param asyncConverter 异步转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的目标对象Mono
     */
    public <S, T> Mono<T> convertAsync(Mono<S> source, Function<S, Mono<T>> asyncConverter) {
        LoggingUtil.debug(logger, "开始执行异步转换");

        return source
                .flatMap(asyncConverter)
                .doOnSuccess(result -> LoggingUtil.debug(logger, "异步转换成功"))
                .doOnError(error -> LoggingUtil.error(logger, "异步转换失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "异步转换错误，返回空结果", error);
                    return Mono.empty();
                });
    }

    /**
     * 批量异步转换
     *
     * @param source 源对象的Flux
     * @param asyncConverter 异步转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的目标对象Flux
     */
    public <S, T> Flux<T> convertAsyncBatch(Flux<S> source, Function<S, Mono<T>> asyncConverter) {
        LoggingUtil.debug(logger, "开始执行批量异步转换");

        return source
                .flatMap(asyncConverter)
                .doOnComplete(() -> LoggingUtil.debug(logger, "批量异步转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "批量异步转换失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "批量异步转换错误，返回空流", error);
                    return Flux.empty();
                });
    }

    /**
     * 带重试的转换
     *
     * @param source 源对象的Mono
     * @param converter 转换函数
     * @param retryTimes 重试次数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的目标对象Mono
     */
    public <S, T> Mono<T> convertWithRetry(Mono<S> source, Function<S, T> converter, long retryTimes) {
        LoggingUtil.debug(logger, "开始执行带重试的转换，重试次数: {}", retryTimes);

        return source
                .map(converter)
                .retry(retryTimes)
                .doOnSuccess(result -> LoggingUtil.debug(logger, "带重试的转换成功"))
                .doOnError(error -> LoggingUtil.error(logger, "带重试的转换最终失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "带重试的转换错误，返回空结果", error);
                    return Mono.empty();
                });
    }

    /**
     * 转换并收集统计信息
     *
     * @param source 源对象的Flux
     * @param converter 转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换统计信息的Mono
     */
    public <S, T> Mono<ConversionStatistics<T>> convertWithStatistics(Flux<S> source, Function<S, T> converter) {
        LoggingUtil.debug(logger, "开始执行带统计的转换");

        return source
                .map(converter)
                .collectList()
                .map(results -> {
                    ConversionStatistics<T> stats = new ConversionStatistics<>();
                    stats.setTotalCount(results.size());
                    stats.setSuccessCount(results.size());
                    stats.setFailureCount(0);
                    stats.setResults(results);
                    return stats;
                })
                .doOnSuccess(stats -> LoggingUtil.debug(logger, "带统计的转换完成，总数: {}, 成功: {}",
                        stats.getTotalCount(), stats.getSuccessCount()))
                .doOnError(error -> LoggingUtil.error(logger, "带统计的转换失败", error));
    }

    /**
     * 转换统计信息类
     */
    public static class ConversionStatistics<T> {
        private int totalCount;
        private int successCount;
        private int failureCount;
        private List<T> results;

        // Getters and Setters
        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void setFailureCount(int failureCount) {
            this.failureCount = failureCount;
        }

        public List<T> getResults() {
            return results;
        }

        public void setResults(List<T> results) {
            this.results = results;
        }

        /**
         * 获取成功率
         *
         * @return 成功率百分比
         */
        public double getSuccessRate() {
            return totalCount > 0 ? (double) successCount / totalCount * 100 : 0.0;
        }
    }
}

