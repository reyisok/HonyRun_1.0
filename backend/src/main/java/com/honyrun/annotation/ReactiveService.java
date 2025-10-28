package com.honyrun.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * 响应式服务注解
 *
 * 用于标识基于Spring WebFlux的响应式服务类，提供非阻塞的业务逻辑处理能力。
 * 该注解结合了@Service的功能，同时明确标识该服务采用响应式编程模型。
 *
 * 特性：
 * - 支持Mono和Flux操作
 * - 非阻塞业务处理
 * - 响应式流组合
 * - 错误处理和恢复
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  15:30:00
 * @modified 2025-07-01 15:30:00
 * @version 2.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface ReactiveService {

    /**
     * 服务名称，用于标识和管理
     *
     * @return 服务名称
     */
    @AliasFor(annotation = Service.class)
    String value() default "";

    /**
     * 服务描述信息
     *
     * @return 服务描述
     */
    String description() default "";

    /**
     * 服务类型分类
     *
     * @return 服务类型
     */
    ServiceType type() default ServiceType.BUSINESS;

    /**
     * 是否启用事务支持
     *
     * @return 是否启用事务
     */
    boolean transactional() default false;

    /**
     * 是否启用缓存支持
     *
     * @return 是否启用缓存
     */
    boolean cacheable() default false;

    /**
     * 服务类型枚举
     */
    enum ServiceType {
        /** 业务服务 */
        BUSINESS,
        /** 系统服务 */
        SYSTEM,
        /** 工具服务 */
        UTILITY,
        /** 基础设施服务 */
        INFRASTRUCTURE
    }
}


