package com.honyrun.aspect;

import com.honyrun.service.monitoring.ReactivePerformanceMonitoringService;
import com.honyrun.util.LoggingUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 性能监控切面
 * 
 * 自动监控方法执行性能：
 * - 记录方法执行时间
 * - 监控响应式方法性能
 * - 记录方法执行成功/失败状态
 * - 性能数据统计和分析
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01 15:50:00
 * @modified 2025-07-01 15:50:00
 * @version 1.0.0
 */
@Aspect
@Component
@Order(100) // 确保在ExposeInvocationInterceptor之后执行，避免AOP调用异常
public class PerformanceMonitoringAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);

    private final ReactivePerformanceMonitoringService performanceMonitoringService;

    public PerformanceMonitoringAspect(ReactivePerformanceMonitoringService performanceMonitoringService) {
        this.performanceMonitoringService = performanceMonitoringService;
    }

    /**
     * 监控Service层方法执行性能
     * 
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("execution(* com.honyrun.service..*.*(..)) && !execution(* com.honyrun.service.monitoring..*.*(..))")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        LoggingUtil.debug(logger, "开始监控方法执行: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            
            // 处理响应式返回类型
            if (result instanceof Mono) {
                return ((Mono<?>) result)
                    .doOnSuccess(value -> recordPerformance(methodName, startTime, true))
                    .doOnError(error -> {
                        recordPerformance(methodName, startTime, false);
                        LoggingUtil.error(logger, "方法执行失败: " + methodName, error);
                    });
            } else if (result instanceof Flux) {
                return ((Flux<?>) result)
                    .doOnComplete(() -> recordPerformance(methodName, startTime, true))
                    .doOnError(error -> {
                        recordPerformance(methodName, startTime, false);
                        LoggingUtil.error(logger, "方法执行失败: " + methodName, error);
                    });
            } else {
                // 同步方法
                recordPerformance(methodName, startTime, true);
                return result;
            }
        } catch (Throwable throwable) {
            recordPerformance(methodName, startTime, false);
            LoggingUtil.error(logger, "方法执行异常: " + methodName, throwable);
            throw throwable;
        }
    }

    /**
     * 监控Controller层方法执行性能
     * 
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("execution(* com.honyrun.controller..*.*(..))")
    public Object monitorControllerPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        LoggingUtil.debug(logger, "开始监控API方法执行: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            
            // 处理响应式返回类型
            if (result instanceof Mono) {
                return ((Mono<?>) result)
                    .doOnSuccess(value -> recordApiPerformance(methodName, startTime, 200))
                    .doOnError(error -> {
                        recordApiPerformance(methodName, startTime, 500);
                        LoggingUtil.error(logger, "API方法执行失败: " + methodName, error);
                    });
            } else if (result instanceof Flux) {
                return ((Flux<?>) result)
                    .doOnComplete(() -> recordApiPerformance(methodName, startTime, 200))
                    .doOnError(error -> {
                        recordApiPerformance(methodName, startTime, 500);
                        LoggingUtil.error(logger, "API方法执行失败: " + methodName, error);
                    });
            } else {
                // 同步方法
                recordApiPerformance(methodName, startTime, 200);
                return result;
            }
        } catch (Throwable throwable) {
            recordApiPerformance(methodName, startTime, 500);
            LoggingUtil.error(logger, "API方法执行异常: " + methodName, throwable);
            throw throwable;
        }
    }

    /**
     * 监控Repository层方法执行性能
     * 
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("execution(* com.honyrun.repository..*.*(..))")
    public Object monitorRepositoryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        LoggingUtil.debug(logger, "开始监控数据库方法执行: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            
            // 处理响应式返回类型
            if (result instanceof Mono) {
                return ((Mono<?>) result)
                    .doOnSuccess(value -> recordDatabasePerformance(methodName, startTime, 1))
                    .doOnError(error -> {
                        recordDatabasePerformance(methodName, startTime, 0);
                        LoggingUtil.error(logger, "数据库方法执行失败: " + methodName, error);
                    });
            } else if (result instanceof Flux) {
                return ((Flux<?>) result)
                    .collectList()
                    .doOnSuccess(list -> recordDatabasePerformance(methodName, startTime, list.size()))
                    .doOnError(error -> {
                        recordDatabasePerformance(methodName, startTime, 0);
                        LoggingUtil.error(logger, "数据库方法执行失败: " + methodName, error);
                    })
                    .flatMapMany(Flux::fromIterable);
            } else {
                // 同步方法
                recordDatabasePerformance(methodName, startTime, 1);
                return result;
            }
        } catch (Throwable throwable) {
            recordDatabasePerformance(methodName, startTime, 0);
            LoggingUtil.error(logger, "数据库方法执行异常: " + methodName, throwable);
            throw throwable;
        }
    }

    /**
     * 记录方法执行性能
     * 
     * @param methodName 方法名
     * @param startTime 开始时间
     * @param success 是否成功
     */
    private void recordPerformance(String methodName, long startTime, boolean success) {
        long executionTime = System.currentTimeMillis() - startTime;
        
        performanceMonitoringService.recordMethodPerformance(methodName, executionTime, success)
            .subscribe(
                unused -> LoggingUtil.debug(logger, "方法性能记录完成: {}, 执行时间: {}ms", methodName, executionTime),
                error -> LoggingUtil.error(logger, "方法性能记录失败: " + methodName, error)
            );
    }

    /**
     * 记录API执行性能
     * 
     * @param methodName 方法名
     * @param startTime 开始时间
     * @param statusCode 状态码
     */
    private void recordApiPerformance(String methodName, long startTime, int statusCode) {
        long executionTime = System.currentTimeMillis() - startTime;
        
        performanceMonitoringService.recordApiPerformance(methodName, "POST", executionTime, statusCode)
            .subscribe(
                unused -> LoggingUtil.debug(logger, "API性能记录完成: {}, 执行时间: {}ms", methodName, executionTime),
                error -> LoggingUtil.error(logger, "API性能记录失败: " + methodName, error)
            );
    }

    /**
     * 记录数据库执行性能
     * 
     * @param methodName 方法名
     * @param startTime 开始时间
     * @param recordCount 记录数
     */
    private void recordDatabasePerformance(String methodName, long startTime, long recordCount) {
        long executionTime = System.currentTimeMillis() - startTime;
        
        performanceMonitoringService.recordDatabaseQueryPerformance(methodName, executionTime, recordCount)
            .subscribe(
                unused -> LoggingUtil.debug(logger, "数据库性能记录完成: {}, 执行时间: {}ms, 记录数: {}", 
                    methodName, executionTime, recordCount),
                error -> LoggingUtil.error(logger, "数据库性能记录失败: " + methodName, error)
            );
    }
}

