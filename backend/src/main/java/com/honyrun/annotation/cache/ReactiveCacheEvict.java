package com.honyrun.annotation.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 响应式缓存清除注解
 * 
 * <p>
 * <strong>功能说明：</strong>
 * </p>
 * <ul>
 * <li>支持响应式的缓存清除操作</li>
 * <li>支持条件清除和批量清除</li>
 * <li>支持方法执行前后清除缓存</li>
 * <li>集成分布式锁确保缓存一致性</li>
 * </ul>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * </p>
 * <pre>
 * {@code
 * @ReactiveCacheEvict(value = "user", key = "#userId")
 * public Mono<Void> deleteUser(String userId) {
 *     return userRepository.deleteById(userId);
 * }
 * 
 * @ReactiveCacheEvict(value = "users", allEntries = true)
 * public Mono<Void> clearAllUsers() {
 *     return userRepository.deleteAll();
 * }
 * 
 * @ReactiveCacheEvict(value = "user", key = "#user.id", beforeInvocation = true)
 * public Mono<User> updateUser(User user) {
 *     return userRepository.save(user);
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
public @interface ReactiveCacheEvict {

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
     * 支持SpEL表达式，返回true时才清除缓存
     * 
     * @return 缓存条件表达式
     */
    String condition() default "";

    /**
     * 是否清除所有条目
     * true：清除指定缓存的所有条目
     * false：只清除指定键的条目
     * 
     * @return 是否清除所有条目
     */
    boolean allEntries() default false;

    /**
     * 是否在方法调用前清除缓存
     * true：方法调用前清除
     * false：方法调用后清除
     * 
     * @return 是否在方法调用前清除缓存
     */
    boolean beforeInvocation() default false;

    /**
     * 键模式匹配
     * 支持通配符模式，如：user:*, *:profile等
     * 
     * @return 键模式
     */
    String keyPattern() default "";

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
     * 默认值-1表示从统一配置获取
     * 
     * @return 分布式锁持有时间
     */
    long lockLeaseTime() default -1;

    /**
     * 是否同步执行
     * true：同步执行缓存清除操作
     * false：异步执行缓存清除操作
     * 
     * @return 是否同步执行
     */
    boolean sync() default false;

    /**
     * 是否级联清除
     * true：清除相关联的缓存
     * false：只清除指定的缓存
     * 
     * @return 是否级联清除
     */
    boolean cascade() default false;

    /**
     * 级联清除的缓存名称
     * 当cascade为true时生效
     * 
     * @return 级联清除的缓存名称数组
     */
    String[] cascadeCaches() default {};
}
