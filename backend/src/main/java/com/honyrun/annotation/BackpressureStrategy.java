package com.honyrun.annotation;

import java.lang.annotation.*;

/**
 * 背压策略注解
 *
 * 用于配置响应式流的背压处理策略，控制数据流速和缓冲区管理。
 * 该注解帮助开发者明确指定背压处理方式，优化系统性能和资源利用。
 *
 * 特性：
 * - 配置背压处理策略
 * - 控制缓冲区大小
 * - 支持多种背压模式
 * - 性能优化支持
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  15:30:00
 * @modified 2025-07-01 15:30:00
 * @version 2.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BackpressureStrategy {

    /**
     * 背压策略类型
     *
     * @return 背压策略
     */
    Strategy value() default Strategy.BUFFER;

    /**
     * 缓冲区大小
     *
     * @return 缓冲区大小
     */
    int bufferSize() default 256;

    /**
     * 是否启用溢出处理
     *
     * @return 是否启用溢出处理
     */
    boolean enableOverflowHandling() default true;

    /**
     * 溢出处理策略
     *
     * @return 溢出处理策略
     */
    OverflowStrategy overflowStrategy() default OverflowStrategy.DROP_LATEST;

    /**
     * 背压监控阈值
     *
     * @return 监控阈值
     */
    double monitoringThreshold() default 0.8;

    /**
     * 背压策略枚举
     */
    enum Strategy {
        /** 缓冲策略 */
        BUFFER,
        /** 丢弃策略 */
        DROP,
        /** 错误策略 */
        ERROR,
        /** 最新策略 */
        LATEST,
        /** 采样策略 */
        SAMPLE
    }

    /**
     * 溢出处理策略枚举
     */
    enum OverflowStrategy {
        /** 丢弃最新 */
        DROP_LATEST,
        /** 丢弃最旧 */
        DROP_OLDEST,
        /** 抛出错误 */
        ERROR,
        /** 忽略 */
        IGNORE
    }
}


