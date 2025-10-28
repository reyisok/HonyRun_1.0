package com.honyrun.annotation.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 响应式缓存更新注解
 * 
 * <p>
 * <strong>功能说明：</strong>
 * </p>
 * <ul>
 * <li>支持响应式的缓存更新操作</li>
 * <li>总是执行方法并更新缓存</li>
 * <li>支持条件更新和过期时间配置</li>
 * <li>集成分布式锁确保缓存一致性</li>
 * </ul>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * </p>
 * <pre>
 * {@code
 * @ReactiveCachePut(value = "user", key = "#user.id", ttl = "PT2H")
 * public Mono<User> updateUser(User user) {
 *     return userRepository.save(user);
 * }
 * 
 * @ReactiveCachePut(value = "userProfile", key = "#userId", condition = "#result != null")
 * public Mono<UserProfile> updateUserProfile(String userId, UserProfile profile) {
 *     return userProfileRepository.save(profile);
 * }
 * }
 * </pre>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-26 01:36:12
 * @version 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReactiveCachePut {

    /**
     * 缓存名称
     * 
     * @return 缓存名称数组
     */
    String[] value() default {};

    /**
     * 缓存键表达式
     * 支持SpEL表达式，如：#userId, #user.id, #p0等
     * 
     * @return 缓存键表达式
     */
    String key() default "";

    /**
     * 缓存键生成器Bean名称
     * 当key为空时使用
     * 
     * @return 键生成器Bean名称
     */
    String keyGenerator() default "";

    /**
     * 缓存管理器Bean名称
     * 
     * @return 缓存管理器Bean名称
     */
    String cacheManager() default "";

    /**
     * 缓存条件表达式
     * 支持SpEL表达式，返回true时才更新缓存
     * 
     * @return 缓存条件表达式
     */
    String condition() default "";

    /**
     * 排除缓存条件表达式
     * 支持SpEL表达式，返回true时不缓存结果
     * 
     * @return 排除缓存条件表达式
     */
    String unless() default "";

    /**
     * 缓存过期时间
     * 支持ISO-8601 Duration格式，如：PT30M（30分钟）、PT2H（2小时）、P1D（1天）
     * 
     * @return 缓存过期时间
     */
    String ttl() default "";

    /**
     * 缓存过期时间（秒）
     * 当ttl为空时使用，优先级低于ttl
     * 
     * @return 缓存过期时间（秒）
     */
    long ttlSeconds() default -1;

    /**
     * 是否启用分布式锁确保缓存一致性
     * 
     * @return 是否启用分布式锁
     */
    boolean distributedLock() default true;

    /**
     * 分布式锁等待时间（毫秒）
     * 
     * @return 分布式锁等待时间
     */
    long lockWaitTime() default 100;

    /**
     * 分布式锁持有时间（毫秒）
     * 从统一配置获取，避免硬编码
     * 
     * @return 分布式锁持有时间
     */
    long lockLeaseTime() default -1; // 使用-1表示从配置获取，避免硬编码

    /**
     * 是否同步执行
     * true：同步执行缓存更新操作
     * false：异步执行缓存更新操作
     * 
     * @return 是否同步执行
     */
    boolean sync() default false;

    /**
     * 缓存空值
     * true：缓存空值（Mono.empty()或Flux.empty()）
     * false：不缓存空值
     * 
     * @return 是否缓存空值
     */
    boolean cacheNull() default true;

    /**
     * 是否覆盖已存在的缓存
     * true：总是覆盖已存在的缓存
     * false：只在缓存不存在时更新
     * 
     * @return 是否覆盖已存在的缓存
     */
    boolean override() default true;

    /**
     * 是否级联更新
     * true：更新相关联的缓存
     * false：只更新指定的缓存
     * 
     * @return 是否级联更新
     */
    boolean cascade() default false;

    /**
     * 级联更新的缓存名称
     * 当cascade为true时生效
     * 
     * @return 级联更新的缓存名称数组
     */
    String[] cascadeCaches() default {};
}
