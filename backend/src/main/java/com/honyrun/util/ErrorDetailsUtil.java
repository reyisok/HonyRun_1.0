package com.honyrun.util;

import com.honyrun.config.UnifiedConfigManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 错误详细信息工具类
 * 
 * 根据环境配置控制错误详细信息的展示：
 * - 生产环境：关闭详细堆栈信息，仅保留traceId
 * - 开发环境：展示精简堆栈信息
 * - 测试环境：展示完整堆栈信息
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-02 20:25:00
 * @modified 2025-10-26 01:36:12
 * @version 1.1.0
 */
@Component
public class ErrorDetailsUtil {

    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数
     *
     * @param unifiedConfigManager 统一配置管理器
     */
    public ErrorDetailsUtil(UnifiedConfigManager unifiedConfigManager) {
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 构建错误详细信息（响应式版本）
     * 
     * @param exception 异常对象
     * @param requestPath 请求路径
     * @return 错误详细信息Map的Mono
     */
    public Mono<Map<String, Object>> buildErrorDetailsReactive(Throwable exception, String requestPath) {
        // 避免阻塞调用，直接使用系统属性获取环境配置
        String activeProfile = System.getProperty("spring.profiles.active", "dev");
        
        Map<String, Object> details = new HashMap<>();
        
        // 基本信息
        details.put("exception", exception.getClass().getSimpleName());
        details.put("path", requestPath);
        details.put("environment", activeProfile);
        
        // 根据环境配置决定是否包含堆栈信息
        if (!"prod".equals(activeProfile) && !"production".equals(activeProfile)) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            
            String fullStackTrace = sw.toString();
            
            // 开发环境显示精简堆栈信息
            if ("dev".equals(activeProfile) || "development".equals(activeProfile)) {
                details.put("stackTrace", limitStackTrace(fullStackTrace, 10));
            } else {
                details.put("stackTrace", fullStackTrace);
            }
        }
        
        return Mono.just(details);
    }

    /**
     * 构建错误详细信息（同步版本，用于非响应式环境）
     * 
     * @param exception 异常对象
     * @param requestPath 请求路径
     * @return 错误详细信息Map
     */
    public Map<String, Object> buildErrorDetails(Throwable exception, String requestPath) {
        Map<String, Object> details = new HashMap<>();
        
        // 基本信息
        details.put("exception", exception.getClass().getSimpleName());
        details.put("path", requestPath);
        
        // 使用默认配置，避免阻塞调用
        String activeProfile = System.getProperty("spring.profiles.active", "dev");
        details.put("environment", activeProfile);
        
        // 在开发和测试环境显示堆栈信息
        if (!"prod".equals(activeProfile) && !"production".equals(activeProfile)) {
            String stackTrace = getStackTraceSync(exception);
            details.put("stackTrace", stackTrace);
        }
        
        return details;
    }

    /**
     * 构建简化错误详细信息（仅包含基本信息）
     * 
     * @param errorType 错误类型
     * @param requestPath 请求路径
     * @return 简化错误详细信息Map
     */
    public Map<String, Object> buildSimpleErrorDetails(String errorType, String requestPath) {
        Map<String, Object> details = new HashMap<>();
        details.put("errorType", errorType);
        details.put("path", requestPath);
        String activeProfile = System.getProperty("spring.profiles.active", "dev");
        details.put("environment", activeProfile);
        return details;
    }

    /**
     * 判断是否应该显示堆栈信息（响应式版本）
     * 
     * @param activeProfile 当前环境配置
     * @return true如果应该显示堆栈信息
     */
    private Mono<Boolean> shouldShowStackTraceReactive(String activeProfile) {
        // 生产环境不显示堆栈信息
        if ("prod".equals(activeProfile) || "production".equals(activeProfile)) {
            return Mono.just(false);
        }
        
        // 避免阻塞调用，使用默认值
        return Mono.just(true);
    }

    /**
     * 获取堆栈信息（响应式版本）
     * 
     * @param exception 异常对象
     * @param activeProfile 当前环境配置
     * @return 堆栈信息字符串的Mono
     */
    private Mono<String> getStackTraceReactive(Throwable exception, String activeProfile) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        
        String fullStackTrace = sw.toString();
        
        // 开发环境显示精简堆栈信息（使用默认值避免阻塞调用）
        if ("dev".equals(activeProfile) || "development".equals(activeProfile)) {
            return Mono.just(limitStackTrace(fullStackTrace, 10));
        }
        
        return Mono.just(fullStackTrace);
    }

    /**
     * 获取堆栈信息（同步版本）
     * 
     * @param exception 异常对象
     * @return 堆栈信息字符串
     */
    private String getStackTraceSync(Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        
        String fullStackTrace = sw.toString();
        
        // 开发环境显示精简堆栈信息（默认10行）
        String activeProfile = System.getProperty("spring.profiles.active", "dev");
        if ("dev".equals(activeProfile) || "development".equals(activeProfile)) {
            return limitStackTrace(fullStackTrace, 10);
        }
        
        return fullStackTrace;
    }

    /**
     * 限制堆栈信息行数
     * 
     * @param stackTrace 完整堆栈信息
     * @param maxStackLines 最大行数
     * @return 限制行数后的堆栈信息
     */
    private String limitStackTrace(String stackTrace, int maxStackLines) {
        String[] lines = stackTrace.split("\n");
        if (lines.length <= maxStackLines) {
            return stackTrace;
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxStackLines; i++) {
            sb.append(lines[i]).append("\n");
        }
        sb.append("... (").append(lines.length - maxStackLines).append(" more lines)");
        
        return sb.toString();
    }

    /**
     * 获取当前环境配置（响应式版本）
     * 
     * @return 当前环境配置的Mono
     */
    public Mono<String> getActiveProfileReactive() {
        // 避免阻塞调用，直接从系统属性获取
        String activeProfile = System.getProperty("spring.profiles.active", "dev");
        return Mono.just(activeProfile);
    }

    /**
     * 获取当前环境配置（同步版本）
     * 
     * @return 当前活动的配置文件
     */
    public String getActiveProfile() {
        return System.getProperty("spring.profiles.active", "dev");
    }

    /**
     * 判断是否为生产环境（响应式版本）
     * 
     * @return true如果是生产环境
     */
    public Mono<Boolean> isProductionEnvironmentReactive() {
        // 避免阻塞调用，直接从系统属性获取
        String activeProfile = System.getProperty("spring.profiles.active", "dev");
        return Mono.just("prod".equals(activeProfile) || "production".equals(activeProfile));
    }

    /**
     * 判断是否为生产环境（同步版本）
     * 
     * @return true如果是生产环境
     */
    public boolean isProductionEnvironment() {
        String activeProfile = System.getProperty("spring.profiles.active", "dev");
        return "prod".equals(activeProfile) || "production".equals(activeProfile);
    }
}


