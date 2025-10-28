package com.honyrun.aspect;

import com.honyrun.util.LoggingUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * 日志监控切面
 * 
 * 统一日志记录和审计功能：
 * - 自动记录方法调用日志
 * - 记录方法参数和返回值
 * - 异常日志记录
 * - 业务操作审计日志
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01 15:55:00
 * @modified 2025-07-01 15:55:00
 * @version 1.0.0
 */
@Aspect
@Component
@Order(200) // 确保在PerformanceMonitoringAspect之后执行
public class LoggingMonitoringAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingMonitoringAspect.class);

    /**
     * Service层方法执行前日志记录
     * 
     * @param joinPoint 连接点
     */
    @Before("execution(* com.honyrun.service..*.*(..)) && !execution(* com.honyrun.service.monitoring..*.*(..))")
    public void logServiceMethodEntry(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        LoggingUtil.debug(logger, "进入Service方法: {}, 参数: {}", methodName, Arrays.toString(args));
    }

    /**
     * Service层方法执行后日志记录
     * 
     * @param joinPoint 连接点
     * @param result 返回结果
     */
    @AfterReturning(pointcut = "execution(* com.honyrun.service..*.*(..)) && !execution(* com.honyrun.service.monitoring..*.*(..))", 
                   returning = "result")
    public void logServiceMethodExit(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        
        if (result instanceof Mono || result instanceof Flux) {
            LoggingUtil.debug(logger, "Service方法执行完成: {}, 返回响应式类型: {}", 
                methodName, result.getClass().getSimpleName());
        } else {
            LoggingUtil.debug(logger, "Service方法执行完成: {}, 返回值类型: {}", 
                methodName, result != null ? result.getClass().getSimpleName() : "null");
        }
    }

    /**
     * Service层方法异常日志记录
     * 
     * @param joinPoint 连接点
     * @param exception 异常
     */
    @AfterThrowing(pointcut = "execution(* com.honyrun.service..*.*(..)) && !execution(* com.honyrun.service.monitoring..*.*(..))", 
                  throwing = "exception")
    public void logServiceMethodException(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        LoggingUtil.error(logger, "Service方法执行异常: {}, 参数: {}, 异常: {}", 
            methodName, Arrays.toString(args), exception.getMessage(), exception);
    }

    /**
     * Controller层方法环绕日志记录
     * 
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("execution(* com.honyrun.controller..*.*(..))")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        LoggingUtil.info(logger, "API请求开始: {}, 参数: {}", methodName, Arrays.toString(args));
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            // 处理响应式返回类型
            if (result instanceof Mono) {
                return ((Mono<?>) result)
                    .doOnSuccess(value -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        LoggingUtil.info(logger, "API请求成功: {}, 执行时间: {}ms", methodName, executionTime);
                    })
                    .doOnError(error -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        LoggingUtil.error(logger, "API请求失败: {}, 执行时间: {}ms, 错误: {}", 
                            methodName, executionTime, error.getMessage(), error);
                    });
            } else if (result instanceof Flux) {
                return ((Flux<?>) result)
                    .doOnComplete(() -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        LoggingUtil.info(logger, "API请求完成: {}, 执行时间: {}ms", methodName, executionTime);
                    })
                    .doOnError(error -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        LoggingUtil.error(logger, "API请求失败: {}, 执行时间: {}ms, 错误: {}", 
                            methodName, executionTime, error.getMessage(), error);
                    });
            } else {
                // 同步方法
                long executionTime = System.currentTimeMillis() - startTime;
                LoggingUtil.info(logger, "API请求完成: {}, 执行时间: {}ms", methodName, executionTime);
                return result;
            }
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            LoggingUtil.error(logger, "API请求异常: {}, 执行时间: {}ms, 异常: {}", 
                methodName, executionTime, throwable.getMessage(), throwable);
            throw throwable;
        }
    }

    /**
     * Repository层方法环绕日志记录
     * 
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("execution(* com.honyrun.repository..*.*(..))")
    public Object logRepositoryMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        LoggingUtil.debug(logger, "数据库操作开始: {}, 参数: {}", methodName, Arrays.toString(args));
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            // 处理响应式返回类型
            if (result instanceof Mono) {
                return ((Mono<?>) result)
                    .doOnSuccess(value -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        LoggingUtil.debug(logger, "数据库操作成功: {}, 执行时间: {}ms", methodName, executionTime);
                    })
                    .doOnError(error -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        LoggingUtil.error(logger, "数据库操作失败: {}, 执行时间: {}ms, 错误: {}", 
                            methodName, executionTime, error.getMessage(), error);
                    });
            } else if (result instanceof Flux) {
                return ((Flux<?>) result)
                    .doOnComplete(() -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        LoggingUtil.debug(logger, "数据库操作完成: {}, 执行时间: {}ms", methodName, executionTime);
                    })
                    .doOnError(error -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        LoggingUtil.error(logger, "数据库操作失败: {}, 执行时间: {}ms, 错误: {}", 
                            methodName, executionTime, error.getMessage(), error);
                    });
            } else {
                // 同步方法
                long executionTime = System.currentTimeMillis() - startTime;
                LoggingUtil.debug(logger, "数据库操作完成: {}, 执行时间: {}ms", methodName, executionTime);
                return result;
            }
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            LoggingUtil.error(logger, "数据库操作异常: {}, 执行时间: {}ms, 异常: {}", 
                methodName, executionTime, throwable.getMessage(), throwable);
            throw throwable;
        }
    }

    /**
     * 业务操作审计日志记录
     * 
     * @param joinPoint 连接点
     */
    @Before("execution(* com.honyrun.service..*.create*(..)) || " +
            "execution(* com.honyrun.service..*.update*(..)) || " +
            "execution(* com.honyrun.service..*.delete*(..)) || " +
            "execution(* com.honyrun.service..*.register*(..)) || " +
            "execution(* com.honyrun.service..*.login*(..))")
    public void logBusinessOperation(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        Object[] args = joinPoint.getArgs();
        
        // 记录业务操作审计日志
        LoggingUtil.logBusinessOperation(
            methodName,
            className,
            Arrays.toString(args)
        );
    }

    /**
     * 安全相关操作日志记录
     * 
     * @param joinPoint 连接点
     */
    @Before("execution(* com.honyrun.service.auth..*.*(..)) || " +
            "execution(* com.honyrun.service.security..*.*(..))")
    public void logSecurityOperation(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // 记录安全操作日志（不记录敏感参数）
        String sanitizedArgs = sanitizeSecurityArgs(args);
        
        LoggingUtil.logSecurityEvent(
            "SECURITY_OPERATION",
            methodName,
            sanitizedArgs,
            "INFO"
        );
    }

    /**
     * 清理安全相关参数，避免记录敏感信息
     * 
     * @param args 原始参数
     * @return 清理后的参数字符串
     */
    private String sanitizeSecurityArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            
            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else {
                String argStr = arg.toString();
                // 检查是否包含敏感信息
                if (containsSensitiveInfo(argStr)) {
                    sb.append("[SENSITIVE_DATA_MASKED]");
                } else {
                    sb.append(argStr);
                }
            }
        }
        sb.append("]");
        
        return sb.toString();
    }

    /**
     * 检查字符串是否包含敏感信息
     * 
     * @param str 待检查字符串
     * @return 是否包含敏感信息
     */
    private boolean containsSensitiveInfo(String str) {
        if (str == null) {
            return false;
        }
        
        String lowerStr = str.toLowerCase();
        return lowerStr.contains("password") || 
               lowerStr.contains("token") || 
               lowerStr.contains("secret") || 
               lowerStr.contains("key") ||
               lowerStr.contains("credential");
    }
}

