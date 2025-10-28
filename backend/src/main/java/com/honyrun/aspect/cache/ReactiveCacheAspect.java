package com.honyrun.aspect.cache;

import com.honyrun.annotation.cache.ReactiveCacheable;
import com.honyrun.annotation.cache.ReactiveCacheEvict;
import com.honyrun.annotation.cache.ReactiveCachePut;
import com.honyrun.annotation.cache.ReactiveCaching;
import com.honyrun.service.cache.ReactiveCacheService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.config.UnifiedConfigManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 响应式缓存切面
 * 
 * <p>
 * <strong>功能说明：</strong>
 * </p>
 * <ul>
 * <li>处理响应式缓存注解的AOP逻辑</li>
 * <li>支持Mono和Flux的缓存操作</li>
 * <li>提供SpEL表达式解析</li>
 * <li>集成分布式锁和缓存一致性</li>
 * </ul>
 * 
 * <p>
 * <strong>处理流程：</strong>
 * </p>
 * <ol>
 * <li>解析缓存注解配置</li>
 * <li>计算缓存键和条件</li>
 * <li>执行缓存操作</li>
 * <li>处理响应式流</li>
 * </ol>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-26 01:36:12
 * @version 1.0.0
 */
@Aspect
@Component
public class ReactiveCacheAspect {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveCacheAspect.class);

    private final ReactiveCacheService cacheService;
    private final ApplicationContext applicationContext;
    private final ExpressionParser expressionParser;
    private final ConcurrentMap<String, Expression> expressionCache;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数
     * @param cacheService 缓存服务
     * @param applicationContext 应用上下文
     * @param unifiedConfigManager 统一配置管理器
     * @author: Mr.Rey Copyright © 2025
     * @created: 2025-10-26 01:36:12
     * @version: 1.0.3
     */
    public ReactiveCacheAspect(ReactiveCacheService cacheService, ApplicationContext applicationContext, UnifiedConfigManager unifiedConfigManager) {
        this.cacheService = cacheService;
        this.applicationContext = applicationContext;
        this.unifiedConfigManager = unifiedConfigManager;
        this.expressionParser = new SpelExpressionParser();
        this.expressionCache = new ConcurrentHashMap<>();
    }

    /**
     * 处理@ReactiveCacheable注解
     *
     * @param joinPoint 连接点
     * @param cacheable 缓存注解
     * @return 处理结果
     */
    @Around("@annotation(cacheable)")
    public Object handleCacheable(ProceedingJoinPoint joinPoint, ReactiveCacheable cacheable) {
        LoggingUtil.debug(logger, "处理@ReactiveCacheable注解: {}", cacheable);

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();

        // 解析缓存键
        String cacheKey = resolveCacheKey(cacheable.key(), cacheable.keyGenerator(), method, args);
        
        // 解析条件
        boolean shouldCache = evaluateCondition(cacheable.condition(), method, args, null);
        if (!shouldCache) {
            LoggingUtil.debug(logger, "缓存条件不满足，直接执行方法");
            return proceedMethod(joinPoint);
        }

        // 解析缓存名称
        String[] cacheNames = cacheable.value();
        if (cacheNames.length == 0) {
            cacheNames = new String[]{method.getDeclaringClass().getSimpleName()};
        }

        // 解析TTL
        Duration ttl = resolveTtl(cacheable.ttl(), cacheable.ttlSeconds());

        // 检查返回类型
        Class<?> returnType = method.getReturnType();
        
        if (Mono.class.isAssignableFrom(returnType)) {
            return handleMonoCacheable(joinPoint, cacheNames[0], cacheKey, ttl, cacheable);
        } else if (Flux.class.isAssignableFrom(returnType)) {
            return handleFluxCacheable(joinPoint, cacheNames[0], cacheKey, ttl, cacheable);
        } else {
            LoggingUtil.warn(logger, "不支持的返回类型: {}", returnType);
            return proceedMethod(joinPoint);
        }
    }

    /**
     * 处理@ReactiveCacheEvict注解
     *
     * @param joinPoint 连接点
     * @param cacheEvict 缓存清除注解
     * @return 处理结果
     */
    @Around("@annotation(cacheEvict)")
    public Object handleCacheEvict(ProceedingJoinPoint joinPoint, ReactiveCacheEvict cacheEvict) {
        LoggingUtil.debug(logger, "处理@ReactiveCacheEvict注解: {}", cacheEvict);

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();

        // 解析条件
        boolean shouldEvict = evaluateCondition(cacheEvict.condition(), method, args, null);
        if (!shouldEvict) {
            LoggingUtil.debug(logger, "缓存清除条件不满足，直接执行方法");
            return proceedMethod(joinPoint);
        }

        // 解析缓存名称
        String[] cacheNames = cacheEvict.value();
        if (cacheNames.length == 0) {
            cacheNames = new String[]{method.getDeclaringClass().getSimpleName()};
        }

        if (cacheEvict.beforeInvocation()) {
            // 方法执行前清除缓存
            evictCache(cacheEvict, cacheNames, method, args);
            return proceedMethod(joinPoint);
        } else {
            // 方法执行后清除缓存
            Object result = proceedMethod(joinPoint);
            evictCache(cacheEvict, cacheNames, method, args);
            return result;
        }
    }

    /**
     * 处理@ReactiveCachePut注解
     *
     * @param joinPoint 连接点
     * @param cachePut 缓存更新注解
     * @return 处理结果
     */
    @Around("@annotation(cachePut)")
    public Object handleCachePut(ProceedingJoinPoint joinPoint, ReactiveCachePut cachePut) {
        LoggingUtil.debug(logger, "处理@ReactiveCachePut注解: {}", cachePut);

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();

        // 解析缓存键
        String cacheKey = resolveCacheKey(cachePut.key(), cachePut.keyGenerator(), method, args);
        
        // 解析缓存名称
        String[] cacheNames = cachePut.value();
        if (cacheNames.length == 0) {
            cacheNames = new String[]{method.getDeclaringClass().getSimpleName()};
        }

        // 解析TTL
        Duration ttl = resolveTtl(cachePut.ttl(), cachePut.ttlSeconds());

        // 执行方法并更新缓存
        Object result = proceedMethod(joinPoint);
        
        // 检查条件
        boolean shouldPut = evaluateCondition(cachePut.condition(), method, args, result);
        boolean shouldNotPut = evaluateCondition(cachePut.unless(), method, args, result);
        
        if (shouldPut && !shouldNotPut) {
            putCache(result, cacheNames[0], cacheKey, ttl, cachePut);
        }

        return result;
    }

    /**
     * 处理@ReactiveCaching注解
     *
     * @param joinPoint 连接点
     * @param caching 缓存组合注解
     * @return 处理结果
     */
    @Around("@annotation(caching)")
    public Object handleCaching(ProceedingJoinPoint joinPoint, ReactiveCaching caching) {
        LoggingUtil.debug(logger, "处理@ReactiveCaching注解: {}", caching);

        // 处理执行前的操作
        if (caching.executionOrder() == ReactiveCaching.ExecutionOrder.BEFORE_METHOD ||
            caching.executionOrder() == ReactiveCaching.ExecutionOrder.AROUND_METHOD) {
            
            // 处理缓存清除（beforeInvocation=true）
            Arrays.stream(caching.evict())
                .filter(ReactiveCacheEvict::beforeInvocation)
                .forEach(evict -> handleCacheEvict(joinPoint, evict));
        }

        // 尝试从缓存获取（如果有@ReactiveCacheable）
        for (ReactiveCacheable cacheable : caching.cacheable()) {
            Object cachedResult = tryGetFromCache(joinPoint, cacheable);
            if (cachedResult != null) {
                return cachedResult;
            }
        }

        // 执行方法
        Object result = proceedMethod(joinPoint);

        // 处理执行后的操作
        if (caching.executionOrder() == ReactiveCaching.ExecutionOrder.AFTER_METHOD ||
            caching.executionOrder() == ReactiveCaching.ExecutionOrder.AROUND_METHOD) {
            
            // 处理缓存更新
            Arrays.stream(caching.put())
                .forEach(put -> handleCachePut(joinPoint, put));
            
            // 处理缓存清除（beforeInvocation=false）
            Arrays.stream(caching.evict())
                .filter(evict -> !evict.beforeInvocation())
                .forEach(evict -> handleCacheEvict(joinPoint, evict));
        }

        return result;
    }

    /**
     * 处理Mono类型的缓存
     */
    @SuppressWarnings("unchecked")
    private Mono<Object> handleMonoCacheable(ProceedingJoinPoint joinPoint, String cacheName, 
                                           String cacheKey, Duration ttl, ReactiveCacheable cacheable) {
        return cacheService.get(cacheKey, Object.class)
            .cast(Object.class)
            .switchIfEmpty(Mono.defer(() -> {
                LoggingUtil.debug(logger, "缓存未命中，执行方法: {}", cacheKey);
                Mono<Object> result = (Mono<Object>) proceedMethod(joinPoint);
                
                return result.flatMap(value -> {
                    // 检查unless条件
                    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
                    Object[] args = joinPoint.getArgs();
                    boolean shouldNotCache = evaluateCondition(cacheable.unless(), method, args, value);
                    
                    if (!shouldNotCache && (cacheable.cacheNull() || value != null)) {
                        return cacheService.set(cacheKey, value, ttl)
                            .thenReturn(value);
                    }
                    return Mono.just(value);
                });
            }))
            .doOnNext(value -> LoggingUtil.debug(logger, "缓存命中: {}", cacheKey))
            .doOnError(error -> LoggingUtil.error(logger, "缓存操作失败: " + cacheKey, error));
    }

    /**
     * 处理Flux类型的缓存
     */
    @SuppressWarnings("unchecked")
    private Flux<Object> handleFluxCacheable(ProceedingJoinPoint joinPoint, String cacheName, 
                                           String cacheKey, Duration ttl, ReactiveCacheable cacheable) {
        return cacheService.get(cacheKey, Object.class)
            .cast(Flux.class)
            .flatMapMany(flux -> (Flux<Object>) flux)
            .switchIfEmpty(Flux.defer(() -> {
                LoggingUtil.debug(logger, "缓存未命中，执行方法: {}", cacheKey);
                Flux<Object> result = (Flux<Object>) proceedMethod(joinPoint);
                
                return result.collectList()
                    .flatMap(list -> {
                        // 检查unless条件
                        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
                        Object[] args = joinPoint.getArgs();
                        boolean shouldNotCache = evaluateCondition(cacheable.unless(), method, args, list);
                        
                        if (!shouldNotCache && (cacheable.cacheNull() || !list.isEmpty())) {
                            return cacheService.set(cacheKey, Flux.fromIterable(list), ttl)
                                .thenReturn(list);
                        }
                        return Mono.just(list);
                    })
                    .flatMapMany(Flux::fromIterable);
            }))
            .doOnNext(value -> LoggingUtil.debug(logger, "缓存命中: {}", cacheKey))
            .doOnError(error -> LoggingUtil.error(logger, "缓存操作失败: " + cacheKey, error));
    }

    /**
     * 清除缓存
     */
    private void evictCache(ReactiveCacheEvict cacheEvict, String[] cacheNames, Method method, Object[] args) {
        for (String cacheName : cacheNames) {
            if (cacheEvict.allEntries()) {
                cacheService.deleteByPattern(cacheName + ":*")
                    .subscribe(count -> LoggingUtil.debug(logger, "清除缓存 {} 的所有条目，数量: {}", cacheName, count));
            } else {
                String cacheKey = resolveCacheKey(cacheEvict.key(), cacheEvict.keyGenerator(), method, args);
                if (!cacheKey.isEmpty()) {
                    cacheService.delete(cacheKey)
                        .subscribe(success -> LoggingUtil.debug(logger, "清除缓存: {} = {}", cacheKey, success));
                }
            }
        }
    }

    /**
     * 更新缓存
     */
    private void putCache(Object result, String cacheName, String cacheKey, Duration ttl, ReactiveCachePut cachePut) {
        if (result instanceof Mono) {
            ((Mono<?>) result).flatMap(value -> cacheService.set(cacheKey, value, ttl))
                .subscribe(success -> LoggingUtil.debug(logger, "更新缓存: {} = {}", cacheKey, success));
        } else if (result instanceof Flux) {
            ((Flux<?>) result).collectList()
                .flatMap(list -> cacheService.set(cacheKey, Flux.fromIterable(list), ttl))
                .subscribe(success -> LoggingUtil.debug(logger, "更新缓存: {} = {}", cacheKey, success));
        } else {
            cacheService.set(cacheKey, result, ttl)
                .subscribe(success -> LoggingUtil.debug(logger, "更新缓存: {} = {}", cacheKey, success));
        }
    }

    /**
     * 尝试从缓存获取数据
     */
    private Object tryGetFromCache(ProceedingJoinPoint joinPoint, ReactiveCacheable cacheable) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();

        // 解析条件
        boolean shouldCache = evaluateCondition(cacheable.condition(), method, args, null);
        if (!shouldCache) {
            return null;
        }

        String cacheKey = resolveCacheKey(cacheable.key(), cacheable.keyGenerator(), method, args);
        Duration ttl = resolveTtl(cacheable.ttl(), cacheable.ttlSeconds());

        Class<?> returnType = method.getReturnType();
        if (Mono.class.isAssignableFrom(returnType)) {
            return handleMonoCacheable(joinPoint, cacheable.value()[0], cacheKey, ttl, cacheable);
        } else if (Flux.class.isAssignableFrom(returnType)) {
            return handleFluxCacheable(joinPoint, cacheable.value()[0], cacheKey, ttl, cacheable);
        }

        return null;
    }

    /**
     * 解析缓存键
     */
    private String resolveCacheKey(String keyExpression, String keyGenerator, Method method, Object[] args) {
        if (!keyExpression.isEmpty()) {
            return evaluateExpression(keyExpression, method, args, null, String.class);
        }
        
        if (!keyGenerator.isEmpty()) {
            // 实现键生成器逻辑
            try {
                // 尝试从Spring上下文获取键生成器Bean
                Object keyGeneratorBean = applicationContext.getBean(keyGenerator);
                if (keyGeneratorBean instanceof org.springframework.cache.interceptor.KeyGenerator) {
                    org.springframework.cache.interceptor.KeyGenerator generator = 
                        (org.springframework.cache.interceptor.KeyGenerator) keyGeneratorBean;
                    Object key = generator.generate(null, method, args);
                    return key != null ? key.toString() : "null";
                }
            } catch (Exception e) {
                LoggingUtil.warn(logger, "无法获取键生成器Bean: {}, 使用默认策略", keyGenerator, e);
            }
        }
        
        // 默认键生成策略
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(method.getDeclaringClass().getSimpleName())
                  .append(":")
                  .append(method.getName());
        
        if (args.length > 0) {
            keyBuilder.append(":");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) keyBuilder.append(",");
                keyBuilder.append(args[i] != null ? args[i].toString() : "null");
            }
        }
        
        return keyBuilder.toString();
    }

    /**
     * 解析TTL
     */
    private Duration resolveTtl(String ttlExpression, long ttlSeconds) {
        if (!ttlExpression.isEmpty()) {
            try {
                return Duration.parse(ttlExpression);
            } catch (Exception e) {
                LoggingUtil.warn(logger, "解析TTL表达式失败: {}", ttlExpression, e);
            }
        }
        
        if (ttlSeconds > 0) {
            return Duration.ofSeconds(ttlSeconds);
        }
        
        // 从统一配置获取默认缓存TTL，而不是硬编码30分钟
        return Duration.parse(unifiedConfigManager.getProperty("honyrun.cache.default.ttl", "PT30M"));
    }

    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(String condition, Method method, Object[] args, Object result) {
        if (condition.isEmpty()) {
            return true;
        }
        
        try {
            return evaluateExpression(condition, method, args, result, Boolean.class);
        } catch (Exception e) {
            LoggingUtil.warn(logger, "评估条件表达式失败: {}", condition, e);
            return true;
        }
    }

    /**
     * 评估SpEL表达式
     */
    private <T> T evaluateExpression(String expressionString, Method method, Object[] args, Object result, Class<T> returnType) {
        Expression expression = expressionCache.computeIfAbsent(expressionString, 
            key -> expressionParser.parseExpression(key));
        
        EvaluationContext context = new StandardEvaluationContext();
        
        // 设置参数
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                context.setVariable("p" + i, args[i]);
                context.setVariable("a" + i, args[i]);
            }
        }
        
        // 设置结果
        if (result != null) {
            context.setVariable("result", result);
        }
        
        // 设置方法参数名（如果可用）
        // 实现参数名解析
        try {
            java.lang.reflect.Parameter[] parameters = method.getParameters();
            if (parameters.length > 0 && args != null) {
                for (int i = 0; i < Math.min(parameters.length, args.length); i++) {
                    String paramName = parameters[i].getName();
                    context.setVariable(paramName, args[i]);
                }
            }
        } catch (Exception e) {
            LoggingUtil.debug(logger, "无法获取参数名，使用默认索引: {}", e.getMessage());
        }
        
        return expression.getValue(context, returnType);
    }

    /**
     * 执行原方法
     */
    private Object proceedMethod(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            LoggingUtil.error(logger, "执行方法失败", throwable);
            throw new RuntimeException("方法执行失败", throwable);
        }
    }
}
