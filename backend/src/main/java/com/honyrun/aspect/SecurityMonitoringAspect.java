package com.honyrun.aspect;

import com.honyrun.util.LoggingUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 安全监控切面
 *
 * 监控安全相关操作和异常：
 * - 认证和授权操作监控
 * - 安全异常记录和告警
 * - 可疑操作检测
 * - 安全事件统计
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01 16:00:00
 * @modified 2025-07-01 16:00:00
 * @version 1.0.0
 */
@Aspect
@Component
@Order(300) // 确保在LoggingMonitoringAspect之后执行
public class SecurityMonitoringAspect {

    // 安全事件统计
    private final ConcurrentHashMap<String, AtomicLong> securityEventCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> lastSecurityEvents = new ConcurrentHashMap<>();

    // 可疑操作阈值
    private static final int SUSPICIOUS_LOGIN_ATTEMPTS = 5;
    private static final int SUSPICIOUS_ACCESS_ATTEMPTS = 10;

    /**
     * 监控认证服务方法
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("execution(* com.honyrun.service.auth..*.*(..))")
    public Object monitorAuthService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        LoggingUtil.logSecurityEvent(
            "AUTH_OPERATION_START",
            methodName,
            sanitizeArgs(args),
            "INFO"
        );

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            // 处理响应式返回类型
            if (result instanceof Mono) {
                return ((Mono<?>) result)
                    .doOnSuccess(value -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        recordSuccessfulAuthOperation(methodName, executionTime);
                    })
                    .doOnError(error -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        recordFailedAuthOperation(methodName, error, executionTime);
                    });
            } else if (result instanceof Flux) {
                return ((Flux<?>) result)
                    .doOnComplete(() -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        recordSuccessfulAuthOperation(methodName, executionTime);
                    })
                    .doOnError(error -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        recordFailedAuthOperation(methodName, error, executionTime);
                    });
            } else {
                // 同步方法
                long executionTime = System.currentTimeMillis() - startTime;
                recordSuccessfulAuthOperation(methodName, executionTime);
                return result;
            }
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            recordFailedAuthOperation(methodName, throwable, executionTime);
            throw throwable;
        }
    }

    /**
     * 监控权限验证方法
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("execution(* com.honyrun.service.security..*.*(..))")
    public Object monitorSecurityService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        LoggingUtil.logSecurityEvent(
            "SECURITY_CHECK_START",
            methodName,
            sanitizeArgs(args),
            "INFO"
        );

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            // 处理响应式返回类型
            if (result instanceof Mono) {
                return ((Mono<?>) result)
                    .doOnSuccess(value -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        recordSuccessfulSecurityCheck(methodName, executionTime);
                    })
                    .doOnError(error -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        recordFailedSecurityCheck(methodName, error, executionTime);
                    });
            } else if (result instanceof Flux) {
                return ((Flux<?>) result)
                    .doOnComplete(() -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        recordSuccessfulSecurityCheck(methodName, executionTime);
                    })
                    .doOnError(error -> {
                        long executionTime = System.currentTimeMillis() - startTime;
                        recordFailedSecurityCheck(methodName, error, executionTime);
                    });
            } else {
                // 同步方法
                long executionTime = System.currentTimeMillis() - startTime;
                recordSuccessfulSecurityCheck(methodName, executionTime);
                return result;
            }
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            recordFailedSecurityCheck(methodName, throwable, executionTime);
            throw throwable;
        }
    }

    /**
     * 监控登录操作
     *
     * @param joinPoint 连接点
     */
    @Before("execution(* com.honyrun.service..*.login*(..))")
    public void monitorLoginAttempt(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String username = extractUsername(args);

        if (username != null) {
            incrementSecurityEventCount("LOGIN_ATTEMPT_" + username);

            // 检查可疑登录尝试
            long attempts = getSecurityEventCount("LOGIN_ATTEMPT_" + username);
            if (attempts >= SUSPICIOUS_LOGIN_ATTEMPTS) {
                LoggingUtil.logSecurityEvent(
                    "SUSPICIOUS_LOGIN_ATTEMPTS",
                    "用户 " + username + " 登录尝试次数异常",
                    "尝试次数: " + attempts,
                    "WARNING"
                );
            }
        }

        LoggingUtil.logSecurityEvent(
            "LOGIN_ATTEMPT",
            "用户登录尝试",
            "用户名: " + (username != null ? username : "未知"),
            "INFO"
        );
    }

    /**
     * 监控登录成功
     *
     * @param joinPoint 连接点
     * @param result 返回结果
     */
    @AfterReturning(pointcut = "execution(* com.honyrun.service..*.login*(..))", returning = "result")
    public void monitorLoginSuccess(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        String username = extractUsername(args);

        if (username != null) {
            // 重置失败计数
            resetSecurityEventCount("LOGIN_ATTEMPT_" + username);
        }

        LoggingUtil.logSecurityEvent(
            "LOGIN_SUCCESS",
            "用户登录成功",
            "用户名: " + (username != null ? username : "未知"),
            "INFO"
        );
    }

    /**
     * 监控登录失败
     *
     * @param joinPoint 连接点
     * @param exception 异常
     */
    @AfterThrowing(pointcut = "execution(* com.honyrun.service..*.login*(..))", throwing = "exception")
    public void monitorLoginFailure(JoinPoint joinPoint, Throwable exception) {
        Object[] args = joinPoint.getArgs();
        String username = extractUsername(args);

        if (username != null) {
            incrementSecurityEventCount("LOGIN_FAILURE_" + username);
        }

        LoggingUtil.logSecurityEvent(
            "LOGIN_FAILURE",
            "用户登录失败",
            "用户名: " + (username != null ? username : "未知") + ", 错误: " + exception.getMessage(),
            "WARNING"
        );
    }

    /**
     * 监控权限验证失败
     *
     * @param joinPoint 连接点
     * @param exception 异常
     */
    @AfterThrowing(pointcut = "execution(* com.honyrun.service.security..*.*(..))", throwing = "exception")
    public void monitorPermissionDenied(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();

        incrementSecurityEventCount("PERMISSION_DENIED");

        LoggingUtil.logSecurityEvent(
            "PERMISSION_DENIED",
            "权限验证失败",
            "方法: " + methodName + ", 错误: " + exception.getMessage(),
            "WARNING"
        );
    }

    /**
     * 监控可疑访问操作
     *
     * @param joinPoint 连接点
     */
    @Before("execution(* com.honyrun.controller..*.*(..))")
    public void monitorSuspiciousAccess(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();

        // 检查访问频率
        incrementSecurityEventCount("ACCESS_" + methodName);
        long accessCount = getSecurityEventCount("ACCESS_" + methodName);

        if (accessCount >= SUSPICIOUS_ACCESS_ATTEMPTS) {
            LoggingUtil.logSecurityEvent(
                "SUSPICIOUS_ACCESS_PATTERN",
                "检测到可疑访问模式",
                "方法: " + methodName + ", 访问次数: " + accessCount,
                "WARNING"
            );
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 记录成功的认证操作
     *
     * @param methodName 方法名
     * @param executionTime 执行时间
     */
    private void recordSuccessfulAuthOperation(String methodName, long executionTime) {
        LoggingUtil.logSecurityEvent(
            "AUTH_OPERATION_SUCCESS",
            methodName + " 执行成功",
            "执行时间: " + executionTime + "ms",
            "INFO"
        );
    }

    /**
     * 记录失败的认证操作
     *
     * @param methodName 方法名
     * @param error 错误
     * @param executionTime 执行时间
     */
    private void recordFailedAuthOperation(String methodName, Throwable error, long executionTime) {
        incrementSecurityEventCount("AUTH_FAILURE_" + methodName);

        LoggingUtil.logSecurityEvent(
            "AUTH_OPERATION_FAILURE",
            methodName + " 执行失败",
            "执行时间: " + executionTime + "ms, 错误: " + error.getMessage(),
            "ERROR"
        );
    }

    /**
     * 记录成功的安全检查
     *
     * @param methodName 方法名
     * @param executionTime 执行时间
     */
    private void recordSuccessfulSecurityCheck(String methodName, long executionTime) {
        LoggingUtil.logSecurityEvent(
            "SECURITY_CHECK_SUCCESS",
            methodName + " 安全检查通过",
            "执行时间: " + executionTime + "ms",
            "INFO"
        );
    }

    /**
     * 记录失败的安全检查
     *
     * @param methodName 方法名
     * @param error 错误
     * @param executionTime 执行时间
     */
    private void recordFailedSecurityCheck(String methodName, Throwable error, long executionTime) {
        incrementSecurityEventCount("SECURITY_CHECK_FAILURE_" + methodName);

        LoggingUtil.logSecurityEvent(
            "SECURITY_CHECK_FAILURE",
            methodName + " 安全检查失败",
            "执行时间: " + executionTime + "ms, 错误: " + error.getMessage(),
            "ERROR"
        );
    }

    /**
     * 清理参数，避免记录敏感信息
     *
     * @param args 原始参数
     * @return 清理后的参数字符串
     */
    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("[SANITIZED]");
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * 从参数中提取用户名
     *
     * @param args 方法参数
     * @return 用户名
     */
    private String extractUsername(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        // 简化实现，实际应该根据具体的参数结构提取
        for (Object arg : args) {
            if (arg != null && arg.toString().length() > 0 && !arg.toString().contains("password")) {
                return arg.toString();
            }
        }

        return null;
    }

    /**
     * 增加安全事件计数
     *
     * @param eventType 事件类型
     */
    private void incrementSecurityEventCount(String eventType) {
        securityEventCounts.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
        lastSecurityEvents.put(eventType, LocalDateTime.now());
    }

    /**
     * 获取安全事件计数
     *
     * @param eventType 事件类型
     * @return 事件计数
     */
    private long getSecurityEventCount(String eventType) {
        AtomicLong count = securityEventCounts.get(eventType);
        return count != null ? count.get() : 0;
    }

    /**
     * 重置安全事件计数
     *
     * @param eventType 事件类型
     */
    private void resetSecurityEventCount(String eventType) {
        securityEventCounts.remove(eventType);
        lastSecurityEvents.remove(eventType);
    }
}

