package com.honyrun.util.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.honyrun.util.LoggingUtil;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 背压工具类
 * 提供背压策略配置、缓冲区管理
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  15:45:00
 * @modified 2025-07-01 15:45:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class BackpressureUtil {

    private static final Logger logger = LoggerFactory.getLogger(BackpressureUtil.class);

    /**
     * 默认缓冲区大小
     */
    public static final int DEFAULT_BUFFER_SIZE = 256;

    /**
     * 默认预取大小
     */
    public static final int DEFAULT_PREFETCH_SIZE = 32;

    /**
     * 默认背压超时时间
     */
    public static final Duration DEFAULT_BACKPRESSURE_TIMEOUT = Duration.ofSeconds(10);

    private BackpressureUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 应用缓冲背压策略
     * 使用缓冲区处理背压
     *
     * @param flux 原始Flux
     * @param bufferSize 缓冲区大小
     * @param <T> 数据类型
     * @return 应用缓冲背压的Flux
     */
    public static <T> Flux<T> buffer(Flux<T> flux, int bufferSize) {
        return flux.onBackpressureBuffer(bufferSize)
                .doOnError(throwable -> LoggingUtil.warn(logger, "缓冲背压溢出", throwable));
    }

    /**
     * 应用默认缓冲背压策略
     * 使用默认缓冲区大小
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 应用缓冲背压的Flux
     */
    public static <T> Flux<T> buffer(Flux<T> flux) {
        return buffer(flux, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 应用丢弃背压策略
     * 当消费者跟不上时丢弃新元素
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 应用丢弃背压的Flux
     */
    public static <T> Flux<T> drop(Flux<T> flux) {
        return flux.onBackpressureDrop(dropped ->
                LoggingUtil.debug(logger, "背压丢弃元素: " + dropped));
    }

    /**
     * 应用保留最新背压策略
     * 当消费者跟不上时保留最新元素，丢弃旧元素
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 应用保留最新背压的Flux
     */
    public static <T> Flux<T> latest(Flux<T> flux) {
        return flux.onBackpressureLatest()
                .doOnNext(value -> LoggingUtil.debug(logger, "背压保留最新元素: " + value));
    }

    /**
     * 应用错误背压策略
     * 当消费者跟不上时抛出异常
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 应用错误背压的Flux
     */
    public static <T> Flux<T> error(Flux<T> flux) {
        return flux.onBackpressureError()
                .doOnError(throwable -> LoggingUtil.error(logger, "背压错误", throwable));
    }

    /**
     * 限制请求速率
     * 限制每秒处理的元素数量
     *
     * @param flux 原始Flux
     * @param elementsPerSecond 每秒元素数
     * @param <T> 数据类型
     * @return 限速后的Flux
     */
    public static <T> Flux<T> limitRate(Flux<T> flux, int elementsPerSecond) {
        Duration interval = Duration.ofMillis(1000 / elementsPerSecond);
        return flux.delayElements(interval)
                .doOnNext(value -> LoggingUtil.debug(logger, "限速处理元素: " + value));
    }

    /**
     * 批量处理背压
     * 将元素分批处理以减少背压
     *
     * @param flux 原始Flux
     * @param batchSize 批处理大小
     * @param <T> 数据类型
     * @return 批量处理的Flux
     */
    public static <T> Flux<T> batchProcess(Flux<T> flux, int batchSize) {
        return flux.buffer(batchSize)
                .flatMap(batch -> Flux.fromIterable(batch)
                        .publishOn(Schedulers.boundedElastic()))
                .doOnNext(value -> LoggingUtil.debug(logger, "批量处理元素: " + value));
    }

    /**
     * 窗口处理背压
     * 使用时间窗口处理背压
     *
     * @param flux 原始Flux
     * @param windowDuration 窗口时间
     * @param <T> 数据类型
     * @return 窗口处理的Flux
     */
    public static <T> Flux<T> windowProcess(Flux<T> flux, Duration windowDuration) {
        return flux.window(windowDuration)
                .flatMap(window -> window.collectList()
                        .flatMapMany(Flux::fromIterable))
                .doOnNext(value -> LoggingUtil.debug(logger, "窗口处理元素: " + value));
    }

    /**
     * 采样处理背压
     * 定期采样元素以减少背压
     *
     * @param flux 原始Flux
     * @param sampleDuration 采样间隔
     * @param <T> 数据类型
     * @return 采样处理的Flux
     */
    public static <T> Flux<T> sample(Flux<T> flux, Duration sampleDuration) {
        return flux.sample(sampleDuration)
                .doOnNext(value -> LoggingUtil.debug(logger, "采样元素: " + value));
    }

    /**
     * 节流处理背压
     * 在指定时间内只处理第一个元素
     *
     * @param flux 原始Flux
     * @param throttleDuration 节流时间
     * @param <T> 数据类型
     * @return 节流处理的Flux
     */
    public static <T> Flux<T> throttleFirst(Flux<T> flux, Duration throttleDuration) {
        return flux.take(Duration.ofMillis(throttleDuration.toMillis()))
                .doOnNext(value -> LoggingUtil.debug(logger, "节流处理元素: " + value));
    }

    /**
     * 防抖处理背压
     * 在指定时间内没有新元素时才发出最后一个元素
     *
     * @param flux 原始Flux
     * @param debounceDuration 防抖时间
     * @param <T> 数据类型
     * @return 防抖处理的Flux
     */
    public static <T> Flux<T> debounce(Flux<T> flux, Duration debounceDuration) {
        return flux.delayElements(debounceDuration)
                .doOnNext(value -> LoggingUtil.debug(logger, "防抖处理元素: " + value));
    }

    /**
     * 监控背压状态
     * 监控并记录背压相关指标
     *
     * @param flux 原始Flux
     * @param name 监控名称
     * @param <T> 数据类型
     * @return 带监控的Flux
     */
    public static <T> Flux<T> monitor(Flux<T> flux, String name) {
        AtomicLong requestCount = new AtomicLong(0);
        AtomicLong emitCount = new AtomicLong(0);

        return flux
                .doOnRequest(n -> {
                    long total = requestCount.addAndGet(n);
                    LoggingUtil.debug(logger, "{}背压监控 - 请求元素数: {}, 累计请求: " + name, n, total);
                })
                .doOnNext(value -> {
                    long total = emitCount.incrementAndGet();
                    LoggingUtil.debug(logger, "{}背压监控 - 发出元素: {}, 累计发出: " + name, value, total);
                })
                .doOnComplete(() -> {
                    LoggingUtil.info(logger, "{}背压监控完成 - 总请求: {}, 总发出: {}", 
                            name, requestCount.get(), emitCount.get());
                });
    }

    /**
     * 自适应背压处理
     * 根据系统负载自动调整背压策略
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 自适应背压处理的Flux
     */
    public static <T> Flux<T> adaptive(Flux<T> flux) {
        return flux.publishOn(Schedulers.boundedElastic(), DEFAULT_PREFETCH_SIZE)
                .onBackpressureBuffer(DEFAULT_BUFFER_SIZE,
                        dropped -> LoggingUtil.warn(logger, "自适应背压丢弃元素: " + dropped))
                .doOnNext(value -> LoggingUtil.debug(logger, "自适应背压处理元素: " + value));
    }

    /**
     * 背压超时处理
     * 为背压操作添加超时机制
     *
     * @param mono 原始Mono
     * @param timeout 超时时间
     * @param <T> 数据类型
     * @return 带超时的Mono
     */
    public static <T> Mono<T> withTimeout(Mono<T> mono, Duration timeout) {
        return mono.timeout(timeout)
                .doOnError(throwable -> LoggingUtil.warn(logger, "背压操作超时", throwable));
    }

    /**
     * 背压超时处理（使用默认超时时间）
     *
     * @param mono 原始Mono
     * @param <T> 数据类型
     * @return 带超时的Mono
     */
    public static <T> Mono<T> withTimeout(Mono<T> mono) {
        return withTimeout(mono, DEFAULT_BACKPRESSURE_TIMEOUT);
    }

    /**
     * 背压超时处理
     * 为背压操作添加超时机制
     *
     * @param flux 原始Flux
     * @param timeout 超时时间
     * @param <T> 数据类型
     * @return 带超时的Flux
     */
    public static <T> Flux<T> withTimeout(Flux<T> flux, Duration timeout) {
        return flux.timeout(timeout)
                .doOnError(throwable -> LoggingUtil.warn(logger, "背压操作超时", throwable));
    }

    /**
     * 背压超时处理（使用默认超时时间）
     *
     * @param flux 原始Flux
     * @param <T> 数据类型
     * @return 带超时的Flux
     */
    public static <T> Flux<T> withTimeout(Flux<T> flux) {
        return withTimeout(flux, DEFAULT_BACKPRESSURE_TIMEOUT);
    }
}


