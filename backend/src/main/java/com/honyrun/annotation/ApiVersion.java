package com.honyrun.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API版本注解
 *
 * 用于标记API方法的版本信息，支持版本控制和向后兼容
 * 可以应用于类或方法级别，方法级别的注解会覆盖类级别的注解
 *
 * 使用示例：
 * <pre>
 * {@code
 * @ApiVersion("v1")
 * @RestController
 * public class UserController {
 *     
 *     @ApiVersion("v1")
 *     @GetMapping("/users")
 *     public Flux<UserResponse> getUsersV1() {
 *         // V1版本实现
 *     }
 *     
 *     @ApiVersion("v2")
 *     @GetMapping("/users")
 *     public Flux<UserResponse> getUsersV2() {
 *         // V2版本实现
 *     }
 * }
 * }
 * </pre>
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 12:20:00
 * @modified 2025-07-01 12:20:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {

    /**
     * API版本号
     * 
     * @return 版本号，如 "v1", "v2" 等
     */
    String value();

    /**
     * 版本描述
     * 
     * @return 版本描述信息
     */
    String description() default "";

    /**
     * 是否已弃用
     * 
     * @return true表示该版本已弃用，false表示仍在使用
     */
    boolean deprecated() default false;

    /**
     * 弃用消息
     * 
     * @return 弃用消息，当deprecated为true时提供弃用原因和替代方案
     */
    String deprecatedMessage() default "";

    /**
     * 最小支持版本
     * 
     * @return 最小支持的版本号
     */
    String minVersion() default "";

    /**
     * 最大支持版本
     * 
     * @return 最大支持的版本号
     */
    String maxVersion() default "";
}

