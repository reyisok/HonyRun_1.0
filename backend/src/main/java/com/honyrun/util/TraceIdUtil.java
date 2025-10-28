package com.honyrun.util;

import java.util.UUID;
import java.util.Optional;

import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

/**
 * 追踪ID工具类 - 响应式版本
 * 
 * 用于生成和管理请求追踪ID，支持分布式系统的链路追踪。
 * 提供统一的追踪ID生成策略，便于问题定位和性能监控。
 * 
 * 响应式特性：
 * - 支持Reactor Context传播TraceId
 * - 兼容ThreadLocal方式（用于非响应式代码）
 * - 自动MDC集成，确保日志包含TraceId
 * - 支持HTTP头传播
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-02 20:20:00
 * @modified 2025-01-16 16:57:03
 * @version 2.0.0 - 响应式上下文传播版本
 */
public class TraceIdUtil {

    private static final String TRACE_ID_PREFIX = "TR";
    private static final String TRACE_ID_CONTEXT_KEY = "traceId";
    public static final String X_TRACE_ID_HEADER = "X-Trace-Id";
    
    // 保留ThreadLocal用于非响应式代码兼容
    // private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>(); // 暂时未使用，保留以备将来扩展

    /**
     * 生成新的追踪ID
     * 
     * @return 追踪ID
     */
    public static String generateTraceId() {
        return TRACE_ID_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 从Reactor Context中获取TraceId
     * 
     * @param context Reactor上下文
     * @return TraceId的Optional包装
     */
    public static Optional<String> getTraceIdFromContext(ContextView context) {
        return context.getOrEmpty(TRACE_ID_CONTEXT_KEY);
    }

    /**
     * 将TraceId添加到Reactor Context中
     * 
     * @param context 原始上下文
     * @param traceId 追踪ID
     * @return 包含TraceId的新上下文
     */
    public static Context putTraceIdToContext(Context context, String traceId) {
        return context.put(TRACE_ID_CONTEXT_KEY, traceId);
    }

    /**
     * 为Mono添加TraceId上下文支持
     * 
     * @param <T> 数据类型
     * @param mono 原始Mono
     * @param traceId 追踪ID，如果为null则自动生成
     * @return 包含TraceId上下文的Mono
     */
    public static <T> Mono<T> withTraceId(Mono<T> mono, String traceId) {
        String actualTraceId = traceId != null ? traceId : generateTraceId();
        return mono.contextWrite(context -> putTraceIdToContext(context, actualTraceId))
                   .doOnSubscribe(subscription -> {
                       // 设置MDC以支持日志记录
                       MDC.put(LoggingUtil.TRACE_ID_KEY, actualTraceId);
                   })
                   .doFinally(signalType -> {
                       // 清理MDC
                       MDC.remove(LoggingUtil.TRACE_ID_KEY);
                   });
    }

    /**
     * 为Flux添加TraceId上下文支持
     * 
     * @param <T> 数据类型
     * @param flux 原始Flux
     * @param traceId 追踪ID，如果为null则自动生成
     * @return 包含TraceId上下文的Flux
     */
    public static <T> Flux<T> withTraceId(Flux<T> flux, String traceId) {
        String actualTraceId = traceId != null ? traceId : generateTraceId();
        return flux.contextWrite(context -> putTraceIdToContext(context, actualTraceId))
                   .doOnSubscribe(subscription -> {
                       // 设置MDC以支持日志记录
                       MDC.put(LoggingUtil.TRACE_ID_KEY, actualTraceId);
                   })
                   .doFinally(signalType -> {
                       // 清理MDC
                       MDC.remove(LoggingUtil.TRACE_ID_KEY);
                   });
    }

    /**
     * 从当前Reactor Context中获取TraceId
     * 如果不存在则生成新的TraceId
     * 
     * @return TraceId的Mono包装
     */
    public static Mono<String> getCurrentTraceId() {
        return Mono.deferContextual(context -> {
            Optional<String> traceId = getTraceIdFromContext(context);
            return Mono.just(traceId.orElseGet(TraceIdUtil::generateTraceId));
        });
    }

    /**
     * 从当前Reactor Context中获取TraceId（同步方法）
     * 主要用于在响应式流中的同步操作
     * 
     * @param context Reactor上下文
     * @return TraceId，如果不存在则生成新的
     */
    public static String getOrGenerateTraceId(ContextView context) {
        return getTraceIdFromContext(context).orElseGet(() -> generateTraceId());
    }

    /**
     * 验证TraceId格式是否有效
     * 
     * @param traceId 要验证的TraceId
     * @return 如果格式有效返回true，否则返回false
     */
    public static boolean isValidTraceId(String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            return false;
        }
        
        // 验证格式：TR + 32位UUID（去掉连字符）
        return traceId.startsWith(TRACE_ID_PREFIX) && 
               traceId.length() == 34 && 
               traceId.substring(2).matches("[a-fA-F0-9]{32}");
    }

    // ========== 兼容性方法（用于非响应式代码） ==========




}


