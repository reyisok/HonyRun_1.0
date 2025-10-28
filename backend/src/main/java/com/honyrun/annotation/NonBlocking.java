package com.honyrun.annotation;

import java.lang.annotation.*;

/**
 * 非阻塞操作注解
 *
 * 用于标识方法或类采用非阻塞I/O操作，确保不会阻塞事件循环线程。
 * 该注解主要用于代码文档化和静态分析，帮助开发者识别和维护非阻塞代码。
 *
 * 特性：
 * - 标识非阻塞操作
 * - 支持静态分析检查
 * - 提供操作类型分类
 * - 性能监控支持
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
public @interface NonBlocking {

    /**
     * 操作描述信息
     *
     * @return 操作描述
     */
    String value() default "";

    /**
     * 非阻塞操作类型
     *
     * @return 操作类型
     */
    OperationType type() default OperationType.IO;

    /**
     * 预期响应时间（毫秒）
     *
     * @return 预期响应时间
     */
    long expectedResponseTime() default -1;

    /**
     * 是否支持背压控制
     *
     * @return 是否支持背压
     */
    boolean supportBackpressure() default true;

    /**
     * 是否启用性能监控
     *
     * @return 是否启用监控
     */
    boolean enableMonitoring() default false;

    /**
     * 非阻塞操作类型枚举
     */
    enum OperationType {
        /** I/O操作 */
        IO,
        /** 数据库操作 */
        DATABASE,
        /** 网络操作 */
        NETWORK,
        /** 缓存操作 */
        CACHE,
        /** 计算操作 */
        COMPUTATION,
        /** 文件操作 */
        FILE,
        /** 消息操作 */
        MESSAGE
    }
}


