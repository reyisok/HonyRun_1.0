package com.honyrun.annotation.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 响应式缓存组合注解
 * 
 * <p>
 * <strong>功能说明：</strong>
 * </p>
 * <ul>
 * <li>支持多个缓存操作的组合</li>
 * <li>可以同时配置缓存、清除、更新操作</li>
 * <li>支持复杂的缓存策略</li>
 * <li>提供事务性缓存操作</li>
 * </ul>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * </p>
 * <pre>
 * {@code
 * @ReactiveCaching(
 *     cacheable = @ReactiveCacheable(value = "user", key = "#userId"),
 *     evict = @ReactiveCacheEvict(value = "userList", allEntries = true)
 * )
 * public Mono<User> getUserAndClearList(String userId) {
 *     return userRepository.findById(userId);
 * }
 * 
 * @ReactiveCaching(
 *     put = @ReactiveCachePut(value = "user", key = "#user.id"),
 *     evict = {
 *         @ReactiveCacheEvict(value = "userList", allEntries = true),
 *         @ReactiveCacheEvict(value = "userStats", key = "#user.department")
 *     }
 * )
 * public Mono<User> updateUserAndClearRelated(User user) {
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
public @interface ReactiveCaching {

    /**
     * 缓存操作配置
     * 
     * @return 缓存操作数组
     */
    ReactiveCacheable[] cacheable() default {};

    /**
     * 缓存清除操作配置
     * 
     * @return 缓存清除操作数组
     */
    ReactiveCacheEvict[] evict() default {};

    /**
     * 缓存更新操作配置
     * 
     * @return 缓存更新操作数组
     */
    ReactiveCachePut[] put() default {};

    /**
     * 操作执行顺序
     * 
     * <p>可选值：</p>
     * <ul>
     * <li>BEFORE_METHOD：方法执行前</li>
     * <li>AFTER_METHOD：方法执行后</li>
     * <li>AROUND_METHOD：方法执行前后</li>
     * </ul>
     * 
     * @return 操作执行顺序
     */
    ExecutionOrder executionOrder() default ExecutionOrder.AROUND_METHOD;

    /**
     * 是否启用事务性缓存操作
     * true：所有缓存操作在同一事务中执行
     * false：独立执行各个缓存操作
     * 
     * @return 是否启用事务性缓存操作
     */
    boolean transactional() default false;

    /**
     * 事务隔离级别
     * 当transactional为true时生效
     * 
     * @return 事务隔离级别
     */
    String isolationLevel() default "READ_COMMITTED";

    /**
     * 事务传播行为
     * 当transactional为true时生效
     * 
     * @return 事务传播行为
     */
    String propagation() default "REQUIRED";

    /**
     * 操作失败时的回滚策略
     * 
     * <p>可选值：</p>
     * <ul>
     * <li>ROLLBACK_ALL：回滚所有操作</li>
     * <li>ROLLBACK_FAILED：只回滚失败的操作</li>
     * <li>IGNORE_FAILURES：忽略失败继续执行</li>
     * </ul>
     * 
     * @return 回滚策略
     */
    RollbackStrategy rollbackStrategy() default RollbackStrategy.ROLLBACK_FAILED;

    /**
     * 操作执行顺序枚举
     */
    enum ExecutionOrder {
        /**
         * 方法执行前
         */
        BEFORE_METHOD,
        
        /**
         * 方法执行后
         */
        AFTER_METHOD,
        
        /**
         * 方法执行前后
         */
        AROUND_METHOD
    }

    /**
     * 回滚策略枚举
     */
    enum RollbackStrategy {
        /**
         * 回滚所有操作
         */
        ROLLBACK_ALL,
        
        /**
         * 只回滚失败的操作
         */
        ROLLBACK_FAILED,
        
        /**
         * 忽略失败继续执行
         */
        IGNORE_FAILURES
    }
}
