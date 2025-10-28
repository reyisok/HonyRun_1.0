package com.honyrun.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * 统一日志工具类
 * 提供响应式日志记录、日志级别控制、格式化等功能
 *
 * 重要说明：日志路径配置
 * - 应用启动目录为 backend/，日志配置中的路径必须使用相对路径 "logs"
 * - 不能使用 "backend/logs"，否则会导致日志写入到 backend/backend/logs/ 错误路径
 * - 正确的日志输出路径应为：backend/logs/
 * - 日志配置在 logback-spring.xml 中的 LOG_PATH 属性控制
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @since 2025-07-01
 * @modified 2025-10-23 16:57:03
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class LoggingUtil {

    // 日志上下文键常量
    public static final String TRACE_ID_KEY = "traceId";
    public static final String USER_ID_KEY = "userId";
    public static final String REQUEST_ID_KEY = "requestId";
    public static final String ACTIVITY_ID_KEY = "activityId";
    public static final String OPERATION_KEY = "operation";

    // 日期时间格式化器
    private static final DateTimeFormatter DATETIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 私有构造函数，防止实例化
     */
    private LoggingUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 生成跟踪ID
     *
     * @return 唯一跟踪ID
     */
    public static String generateTraceId() {
        return "TR" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    /**
     * 生成请求ID
     *
     * @return 唯一请求ID
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * 设置MDC上下文
     *
     * @param key 键
     * @param value 值
     */
    public static void setMDC(String key, String value) {
        if (key != null && value != null) {
            MDC.put(key, value);
        }
    }

    /**
     * 清除MDC上下文
     *
     * @param key 键
     */
    public static void clearMDC(String key) {
        if (key != null) {
            MDC.remove(key);
        }
    }

    /**
     * 清除所有MDC上下文
     */
    public static void clearAllMDC() {
        MDC.clear();
    }

    /**
     * 获取格式化的当前时间
     *
     * @return 格式化的时间字符串
     */
    public static String getCurrentFormattedTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    /**
     * 记录DEBUG级别日志
     *
     * @param logger 日志记录器
     * @param message 日志消息
     * @param args 参数
     */
    public static void debug(Logger logger, String message, Object... args) {
        if (logger.isDebugEnabled()) {
            String traceId = MDC.get(TRACE_ID_KEY);
            if (traceId != null) {
                String fmt = "TraceId: {} - " + message;
                Object[] finalArgs = prependFirstArg(traceId, args);
                logger.debug(fmt, finalArgs);
            } else {
                logger.debug(message, args);
            }
        }
    }

    /**
     * 记录INFO级别日志
     *
     * @param logger 日志记录器
     * @param message 日志消息
     * @param args 参数
     */
    public static void info(Logger logger, String message, Object... args) {
        if (logger.isInfoEnabled()) {
            String traceId = MDC.get(TRACE_ID_KEY);
            if (traceId != null) {
                String fmt = "TraceId: {} - " + message;
                Object[] finalArgs = prependFirstArg(traceId, args);
                logger.info(fmt, finalArgs);
            } else {
                logger.info(message, args);
            }
        }
    }

    /**
     * 记录WARN级别日志
     *
     * @param logger 日志记录器
     * @param message 日志消息
     * @param args 参数
     */
    public static void warn(Logger logger, String message, Object... args) {
        if (logger.isWarnEnabled()) {
            String traceId = MDC.get(TRACE_ID_KEY);
            if (traceId != null) {
                String fmt = "TraceId: {} - " + message;
                Object[] finalArgs = prependFirstArg(traceId, args);
                logger.warn(fmt, finalArgs);
            } else {
                logger.warn(message, args);
            }
        }
    }

    /**
     * 记录ERROR级别日志
     *
     * @param logger 日志记录器
     * @param message 日志消息
     * @param throwable 异常
     * @param args 参数
     */
    public static void error(Logger logger, String message, Throwable throwable, Object... args) {
        if (logger.isErrorEnabled()) {
            String traceId = MDC.get(TRACE_ID_KEY);
            String fmt = (traceId != null) ? ("TraceId: {} - " + message) : message;
            Object[] finalArgs = (traceId != null) ? prependFirstArg(traceId, args) : args;
            String rendered = org.slf4j.helpers.MessageFormatter.arrayFormat(fmt, finalArgs).getMessage();
            logger.error(rendered, throwable);
        }
    }

    /**
     * 记录ERROR级别日志（无异常）
     *
     * @param logger 日志记录器
     * @param message 日志消息
     * @param args 参数
     */
    public static void error(Logger logger, String message, Object... args) {
        if (logger.isErrorEnabled()) {
            String traceId = MDC.get(TRACE_ID_KEY);
            if (traceId != null) {
                String fmt = "TraceId: {} - " + message;
                Object[] finalArgs = prependFirstArg(traceId, args);
                logger.error(fmt, finalArgs);
            } else {
                logger.error(message, args);
            }
        }
    }

    /**
     * 响应式日志记录 - Mono
     * 在Mono流中记录日志，不影响数据流
     *
     * @param <T> 数据类型
     * @param mono Mono流
     * @param logAction 日志记录动作
     * @return 原始Mono流
     */
    public static <T> Mono<T> logMono(Mono<T> mono, Consumer<T> logAction) {
        return mono.doOnNext(logAction)
                   .doOnError(throwable -> {
                       Logger contextLogger = LoggerFactory.getLogger("ReactiveLogging");
                       error(contextLogger, "Mono执行出错", throwable);
                   });
    }

    /**
     * 响应式日志记录 - Flux
     * 在Flux流中记录日志，不影响数据流
     *
     * @param <T> 数据类型
     * @param flux Flux流
     * @param logAction 日志记录动作
     * @return 原始Flux流
     */
    public static <T> Flux<T> logFlux(Flux<T> flux, Consumer<T> logAction) {
        return flux.doOnNext(logAction)
                   .doOnError(throwable -> {
                       Logger contextLogger = LoggerFactory.getLogger("ReactiveLogging");
                       error(contextLogger, "Flux执行出错", throwable);
                   });
    }

    /**
     * 响应式日志装饰器 - Mono版本
     * 为Mono操作添加统一的日志记录，包括TraceId管理
     *
     * @param <T> 数据类型
     * @param mono 原始Mono
     * @param logger 日志记录器
     * @param operationName 操作名称
     * @return 带有日志记录的Mono
     */
    public static <T> Mono<T> decorateWithLogging(Mono<T> mono, Logger logger, String operationName) {
        return mono.contextWrite(context -> {
                    // 从上下文获取或生成TraceId
                    String traceId = TraceIdUtil.getTraceIdFromContext(context)
                            .orElseGet(() -> generateTraceId());
                    return TraceIdUtil.putTraceIdToContext(Context.of(context), traceId);
                })
                .doOnSubscribe(subscription -> {
                    // 从上下文获取TraceId并设置MDC
                    mono.contextWrite(ctx -> ctx)
                        .cast(Object.class)
                        .contextWrite(context -> {
                            String traceId = TraceIdUtil.getOrGenerateTraceId(context);
                            setMDC(TRACE_ID_KEY, traceId);
                            debug(logger, "开始执行操作: {}", operationName);
                            return context;
                        })
                        .subscribe();
                })
                .doOnNext(result -> {
                    debug(logger, "操作执行成功: {} | 结果类型: {}", operationName, 
                          result != null ? result.getClass().getSimpleName() : "null");
                })
                .doOnError(throwable -> {
                    error(logger, "操作执行失败: {} | 错误: {}", throwable, operationName, throwable.getMessage());
                })
                .doFinally(signalType -> {
                    debug(logger, "操作完成: {} | 信号类型: {}", operationName, signalType);
                    // 清理MDC
                    clearMDC(TRACE_ID_KEY);
                });
    }

    /**
     * 响应式日志装饰器 - Flux版本
     * 为Flux操作添加统一的日志记录，包括TraceId管理
     *
     * @param <T> 数据类型
     * @param flux 原始Flux
     * @param logger 日志记录器
     * @param operationName 操作名称
     * @return 带有日志记录的Flux
     */
    public static <T> Flux<T> decorateWithLogging(Flux<T> flux, Logger logger, String operationName) {
        return flux.contextWrite(context -> {
                    // 从上下文获取或生成TraceId
                    String traceId = TraceIdUtil.getTraceIdFromContext(context)
                            .orElseGet(TraceIdUtil::generateTraceId);
                    return TraceIdUtil.putTraceIdToContext(Context.of(context), traceId);
                })
                .doOnSubscribe(subscription -> {
                    // 从上下文获取TraceId并设置MDC
                    flux.contextWrite(ctx -> ctx)
                        .cast(Object.class)
                        .contextWrite(context -> {
                            String traceId = TraceIdUtil.getOrGenerateTraceId(context);
                            setMDC(TRACE_ID_KEY, traceId);
                            debug(logger, "开始执行流操作: {}", operationName);
                            return context;
                        })
                        .subscribe();
                })
                .doOnNext(result -> {
                    debug(logger, "流操作产生数据: {} | 数据类型: {}", operationName, 
                          result != null ? result.getClass().getSimpleName() : "null");
                })
                .doOnError(throwable -> {
                    error(logger, "流操作执行失败: {} | 错误: {}", throwable, operationName, throwable.getMessage());
                })
                .doOnComplete(() -> {
                    debug(logger, "流操作完成: {}", operationName);
                })
                .doFinally(signalType -> {
                    debug(logger, "流操作结束: {} | 信号类型: {}", operationName, signalType);
                    // 清理MDC
                    clearMDC(TRACE_ID_KEY);
                });
    }

    /**
     * 记录方法执行时间
     *
     * @param <T> 返回类型
     * @param mono Mono流
     * @param methodName 方法名
     * @return 带有执行时间记录的Mono流
     */
    public static <T> Mono<T> logExecutionTime(Mono<T> mono, String methodName) {
        return Mono.fromCallable(System::currentTimeMillis)
                   .flatMap(startTime -> mono
                       .doOnSuccess(result -> {
                           long executionTime = System.currentTimeMillis() - startTime;
                           Logger performanceLogger = LoggerFactory.getLogger("Performance");
                           info(performanceLogger, "方法 {} 执行完成，耗时: {}ms", methodName, executionTime);
                       })
                       .doOnError(throwable -> {
                           long executionTime = System.currentTimeMillis() - startTime;
                           Logger performanceLogger = LoggerFactory.getLogger("Performance");
                           error(performanceLogger, "方法 {} 执行失败，耗时: {}ms", throwable, methodName, executionTime);
                       }));
    }

    /**
     * 记录业务操作日志
     *
     * @param operation 操作名称
     * @param userId 用户ID
     * @param details 操作详情
     */
    public static void logBusinessOperation(String operation, String userId, String details) {
        Logger businessLogger = LoggerFactory.getLogger("BUSINESS");

        try {
            setMDC(USER_ID_KEY, userId);
            setMDC(OPERATION_KEY, operation);

            // 【功能实施说明】：敏感信息脱敏功能暂缓实施
            // 原因：当前开发阶段需要完整日志信息用于调试和分析
            // 计划：在生产环境部署前实施敏感信息脱敏功能
            // 风险评估：开发环境中的完整日志有助于问题排查，但需要确保开发环境的安全性
            // String sanitizedDetails = sanitizeLogMessage(details);

            info(businessLogger, "业务操作 - 操作: {}, 用户: {}, 详情: {}, 时间: {}",
                operation,
                userId,
                details, // 使用原始详情，不进行脱敏
                getCurrentFormattedTime());
        } finally {
            clearMDC(USER_ID_KEY);
            clearMDC(OPERATION_KEY);
        }
    }

    /**
     * 记录安全事件日志
     *
     * @param event 安全事件
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param details 事件详情
     */
    public static void logSecurityEvent(String event, String userId, String ipAddress, String details) {
        Logger securityLogger = LoggerFactory.getLogger("SECURITY");

        try {
            setMDC(USER_ID_KEY, userId);
            setMDC("ipAddress", ipAddress);
            setMDC("securityEvent", event);

            // 【功能实施说明】：敏感信息脱敏功能暂缓实施
            // 原因：当前开发阶段需要完整日志信息用于安全分析和威胁检测
            // 计划：在生产环境部署前实施敏感信息脱敏和IP地址掩码功能
            // 风险评估：开发环境中的完整安全日志有助于安全事件分析，但需要确保日志存储的安全性
            // String sanitizedDetails = sanitizeLogMessage(details);
            // String maskedIp = maskIpAddress(ipAddress);

            warn(securityLogger, "安全事件 - 事件: {}, 用户: {}, IP: {}, 详情: {}, 时间: {}",
                event,
                userId,
                ipAddress, // 使用原始IP地址
                details, // 使用原始详情
                getCurrentFormattedTime());
        } finally {
            clearMDC(USER_ID_KEY);
            clearMDC("ipAddress");
            clearMDC("securityEvent");
        }
    }

    /**
     * 记录性能指标日志
     *
     * @param metric 指标名称
     * @param value 指标值
     * @param unit 单位
     */
    public static void logPerformanceMetric(String metric, double value, String unit) {
        Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");

        try {
            setMDC("metric", metric);
            setMDC("value", String.valueOf(value));
            setMDC("unit", unit);

            if (value > getPerformanceThreshold(metric)) {
                warn(performanceLogger, "性能警告 - 指标: {}, 值: {} {}, 阈值已超出, 时间: {}",
                    metric, value, unit, getCurrentFormattedTime());
            } else {
                info(performanceLogger, "性能指标 - 指标: {}, 值: {} {}, 时间: {}",
                    metric, value, unit, getCurrentFormattedTime());
            }
        } finally {
            clearMDC("metric");
            clearMDC("value");
            clearMDC("unit");
        }
    }

    /**
     * 敏感信息脱敏处理
     * 
     * 【功能实施说明】：敏感信息脱敏功能暂缓实施
     * 原因：当前开发阶段需要完整日志信息用于调试和分析
     * 计划：在生产环境部署前实施完整的敏感信息脱敏功能
     * 包括：密码、邮箱、手机号、身份证号等敏感信息的脱敏处理
     * 风险评估：开发环境中保留完整信息有助于问题排查，但需要严格控制日志访问权限
     *
     * @param message 原始消息
     * @return 脱敏后的消息
     */
    /*
    private static String sanitizeLogMessage(String message) {
        if (message == null) {
            return null;
        }

        // 脱敏密码
        message = message.replaceAll("(?i)(password|pwd|passwd)\\s*[=:]\\s*[^\\s,}]+", "$1=***");

        // 脱敏手机号
        message = message.replaceAll("(1[3-9]\\d)\\d{4}(\\d{4})", "$1****$2");

        // 脱敏身份证号
        message = message.replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2");

        // 脱敏邮箱
        message = message.replaceAll("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})",
                                   "$1***@$2");

        // 脱敏银行卡号
        message = message.replaceAll("(\\d{4})\\d{8,12}(\\d{4})", "$1********$2");

        return message;
    }
    */

    /**
     * IP地址脱敏处理
     * 
     * 【功能实施说明】：IP地址脱敏功能暂缓实施
     * 原因：当前开发阶段需要完整IP地址信息用于安全分析和威胁检测
     * 计划：在生产环境部署前实施IP地址脱敏功能，保留前三段，脱敏最后一段
     * 示例：192.168.1.100 -> 192.168.1.***
     * 风险评估：开发环境中的完整IP信息有助于安全事件追踪，但需要确保日志的安全存储
     *
     * @param ipAddress IP地址
     * @return 脱敏后的IP地址
     */
    /*
    private static String maskIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return ipAddress;
        }

        // IPv4地址脱敏：保留前两段，后两段用*替代
        if (ipAddress.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String[] parts = ipAddress.split("\\.");
            return parts[0] + "." + parts[1] + ".*.*";
        }

        // IPv6地址脱敏：保留前两段
        if (ipAddress.contains(":")) {
            String[] parts = ipAddress.split(":");
            if (parts.length >= 2) {
                return parts[0] + ":" + parts[1] + ":****";
            }
        }

        return ipAddress;
    }
    */

    /**
     * 获取性能指标阈值
     *
     * @param metric 指标名称
     * @return 阈值
     */
    private static double getPerformanceThreshold(String metric) {
        switch (metric.toLowerCase()) {
            case "response_time":
            case "响应时间":
                return 1000.0; // 1秒
            case "memory_usage":
            case "内存使用率":
                return 80.0; // 80%
            case "cpu_usage":
            case "CPU使用率":
                return 70.0; // 70%
            case "db_query_time":
            case "数据库查询时间":
                return 500.0; // 500毫秒
            default:
                return Double.MAX_VALUE;
        }
    }

    /**
     * 记录编译日志
     *
     * @param phase 编译阶段
     * @param message 编译消息
     */
    public static void logCompile(String phase, String message) {
        Logger compileLogger = LoggerFactory.getLogger("compile");
        info(compileLogger, "[{}] {}", phase, message);
    }

    private static Object[] prependFirstArg(Object first, Object... args) {
        Object[] finalArgs = new Object[(args == null ? 0 : args.length) + 1];
        finalArgs[0] = first;
        if (args != null && args.length > 0) {
            System.arraycopy(args, 0, finalArgs, 1, args.length);
        }
        return finalArgs;
    }

    /**
     * 创建带有上下文的日志记录器
     *
     * @param clazz 类
     * @param contextMap 上下文映射
     * @return 配置好的日志记录器
     */
    public static Logger createContextLogger(Class<?> clazz, Map<String, String> contextMap) {
        Logger contextLogger = LoggerFactory.getLogger(clazz);

        // 设置上下文
        if (contextMap != null) {
            contextMap.forEach(LoggingUtil::setMDC);
        }

        return contextLogger;
    }


}

