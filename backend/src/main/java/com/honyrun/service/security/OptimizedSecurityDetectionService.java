package com.honyrun.service.security;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.honyrun.config.properties.SecurityDetectionProperties;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 优化的安全检测服务
 *
 * 该服务在UnifiedSecurityDetectionService基础上进行性能优化：
 * 1. 添加缓存机制，避免重复检测
 * 2. 异步处理支持，提高并发性能
 * 3. 正则表达式优化，减少匹配时间
 * 4. 批量检测优化
 * 5. 内存使用优化
 *
 * 性能优化策略：
 * - 使用Spring Cache进行结果缓存
 * - 预编译正则表达式并优化模式
 * - 异步执行器池管理
 * - 短路检测机制
 * - 内存友好的批量处理
 *
 * @author Mr.Rey
 * @created 2025-07-01 16:00:00
 * @modified 2025-07-01 16:00:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
public class OptimizedSecurityDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(OptimizedSecurityDetectionService.class);

    // 安全审计专用日志记录器
    private static final Logger securityAuditLogger = LoggerFactory.getLogger("SECURITY_AUDIT");

    private final SecurityDetectionProperties securityDetectionProperties;

    /**
     * 构造函数注入依赖
     *
     * @param securityDetectionProperties 安全检测配置属性（可选）
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public OptimizedSecurityDetectionService(SecurityDetectionProperties securityDetectionProperties) {
        this.securityDetectionProperties = securityDetectionProperties;
    }

    // 异步执行器，用于并发检测
    private final Executor securityDetectionExecutor = Executors.newFixedThreadPool(4);

    // 本地缓存，用于快速检测结果查找
    private final ConcurrentHashMap<String, Boolean> detectionCache = new ConcurrentHashMap<>();

    // 缓存大小限制
    private static final int MAX_CACHE_SIZE = 10000;

    // 优化的正则表达式模式 - 使用更精确的匹配，减少回溯
    private static final Pattern OPTIMIZED_SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(?:union\\s+select|select\\s+.*\\s+from|insert\\s+into|update\\s+.*\\s+set|delete\\s+from|drop\\s+table|create\\s+table|alter\\s+table|exec\\s*\\(|'\\s*or\\s*'1'\\s*=\\s*'1|or\\s+1\\s*=\\s*1)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern OPTIMIZED_XSS_PATTERN = Pattern.compile(
        "(?i)(?:<script[^>]*>|</script>|javascript:|vbscript:|on(?:load|error|click|mouseover|focus|blur|change|submit)\\s*=|alert\\s*\\(|eval\\s*\\(|expression\\s*\\()",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern OPTIMIZED_PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "(?i)(?:\\.{2}[/\\\\]|etc[/\\\\]passwd|windows[/\\\\]system32|cmd\\.exe|powershell\\.exe)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OPTIMIZED_COMMAND_INJECTION_PATTERN = Pattern.compile(
        "(?i)(?:;\\s*(?:rm|del|rmdir|rd)\\s|\\|\\s*(?:cat|type|more|less)\\s|&&|\\|\\||`|\\$\\(|\\$\\{|exec\\s*\\(|system\\s*\\(|eval\\s*\\()",
        Pattern.CASE_INSENSITIVE
    );

    // 新增优化的检测模式
    private static final Pattern OPTIMIZED_JSON_INJECTION_PATTERN = Pattern.compile(
        "(?i)(?:\\{\\s*[\"']\\s*\\$\\s*[\"']|\\[\\s*\\{\\s*[\"']\\s*\\$|__proto__|constructor|prototype)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OPTIMIZED_LDAP_INJECTION_PATTERN = Pattern.compile(
        "(?:\\*\\)|\\(\\*|\\)\\(|\\*\\(|[&|!]\\()",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OPTIMIZED_UNICODE_MALICIOUS_PATTERN = Pattern.compile(
        "(?:\\\\u(?:0000|0001|0002|0003|0004|0005|0006|0007|0008|000[bB]|000[cC]|000[eE]|000[fF]|001[fF]|007[fF]|00[aA][dD]|[fF]{4})|\\\\x(?:0[0-8]|1[fF]|7[fF]|[fF]{2})|%(?:0[0-8]|1[fF]|7[fF]|[fF]{2})|&#(?:x(?:0[0-8]|1[fF]|7[fF]|[fF]+)|(?:[0-8]|1[5-9]|2[0-9]|3[01]|127|255|65535));)"
    );

    private static final Pattern OPTIMIZED_NOSQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)\\$(?:where|ne|gt|lt|gte|lte|in|nin|regex|exists|type|mod|all|size|elemMatch)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OPTIMIZED_XML_INJECTION_PATTERN = Pattern.compile(
        "(?i)(?:<\\?xml|<!DOCTYPE|<!ENTITY|SYSTEM\\s+[\"']|PUBLIC\\s+[\"']|&[a-zA-Z]+;|<!\\[CDATA\\[)",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 缓存的恶意模式检测
     *
     * @param input 待检测的输入字符串
     * @return 如果包含恶意模式返回true，否则返回false
     */
    @Cacheable(value = "securityDetection", key = "#input.hashCode()", unless = "#result == null")
    public boolean containsMaliciousPatternsWithCache(String input) {
        if (input == null || input.trim().isEmpty()) {
            LoggingUtil.debug(logger, "输入为空或null，跳过恶意检测");
            return false;
        }

        // 检查本地缓存
        String cacheKey = generateCacheKey(input);
        Boolean cachedResult = detectionCache.get(cacheKey);
        if (cachedResult != null) {
            LoggingUtil.debug(logger, "从缓存获取检测结果: {}", cachedResult);
            return cachedResult;
        }

        // 检查是否启用安全检测
        if (!isSecurityDetectionEnabled()) {
            LoggingUtil.debug(logger, "安全检测已禁用");
            return false;
        }

        LoggingUtil.debug(logger, "开始优化的恶意模式检测，输入长度: {}", input.length());

        boolean result = performOptimizedDetection(input);

        // 缓存结果（如果缓存未满）
        if (detectionCache.size() < MAX_CACHE_SIZE) {
            detectionCache.put(cacheKey, result);
        }

        return result;
    }

    /**
     * 异步恶意模式检测
     *
     * @param input 待检测的输入字符串
     * @return 包含检测结果的CompletableFuture
     */
    public CompletableFuture<Boolean> containsMaliciousPatternsAsync(String input) {
        return CompletableFuture.supplyAsync(() -> containsMaliciousPatternsWithCache(input), securityDetectionExecutor)
                .orTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    LoggingUtil.error(logger, "异步安全检测失败", throwable);
                    // 降级处理：返回保守的检测结果
                    return performFallbackDetection(input);
                });
    }

    /**
     * 响应式优化版本的恶意模式检测
     *
     * @param input 待检测的输入字符串
     * @return 包含检测结果的Mono
     */
    public Mono<Boolean> containsMaliciousPatternsReactiveOptimized(String input) {
        return Mono.fromCallable(() -> containsMaliciousPatternsWithCache(input))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(3))
                .doOnNext(result -> {
                    if (result) {
                        LoggingUtil.warn(logger, "优化检测发现恶意模式，输入长度: {} 字符", input != null ? input.length() : 0);
                        // 记录到安全审计日志
                        LoggingUtil.warn(securityAuditLogger, "优化恶意模式检测 - 输入长度: {} | 输入哈希: {}",
                            input != null ? input.length() : 0, input != null ? input.hashCode() : 0);
                    }
                })
                .onErrorReturn(false);
    }

    /**
     * 批量优化检测
     *
     * @param inputs 待检测的输入列表
     * @return 如果任何输入包含恶意模式返回true
     */
    public Mono<Boolean> containsMaliciousPatternsInBatchOptimized(List<String> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return Mono.just(false);
        }

        return Mono.fromCallable(() -> {
            // 并行处理批量检测
            return inputs.parallelStream()
                    .anyMatch(this::containsMaliciousPatternsWithCache);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .timeout(Duration.ofSeconds(10))
        .doOnNext(result -> {
            if (result) {
                LoggingUtil.warn(logger, "批量检测发现恶意模式，检测项目数: {}", inputs.size());
            }
        })
        .onErrorReturn(false);
    }

    /**
     * 执行优化的检测逻辑
     */
    private boolean performOptimizedDetection(String input) {
        // 短路检测：先检查最常见的攻击类型

        // 1. 快速长度检查
        if (input.length() > 10000) {
            LoggingUtil.warn(logger, "输入长度过长，可能存在DoS攻击: {} 字符", input.length());
            return true;
        }

        // 2. 优先检查SQL注入（最常见）
        if (OPTIMIZED_SQL_INJECTION_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到SQL注入攻击");
            LoggingUtil.warn(securityAuditLogger, "SQL注入检测 - 输入长度: {} | 输入哈希: {}", input.length(), input.hashCode());
            return true;
        }

        // 3. 检查XSS攻击（第二常见）
        if (OPTIMIZED_XSS_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到XSS攻击");
            LoggingUtil.warn(securityAuditLogger, "XSS攻击检测 - 输入长度: {} | 输入哈希: {}", input.length(), input.hashCode());
            return true;
        }

        // 4. 检查路径遍历
        if (OPTIMIZED_PATH_TRAVERSAL_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到路径遍历攻击");
            LoggingUtil.warn(securityAuditLogger, "路径遍历检测 - 输入长度: {} | 输入哈希: {}", input.length(), input.hashCode());
            return true;
        }

        // 5. 检查命令注入
        if (OPTIMIZED_COMMAND_INJECTION_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到命令注入攻击");
            LoggingUtil.warn(securityAuditLogger, "命令注入检测 - 输入长度: {} | 输入哈希: {}", input.length(), input.hashCode());
            return true;
        }

        // 6. 检查新型攻击（较少见，放在后面）
        if (OPTIMIZED_JSON_INJECTION_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到JSON注入攻击");
            LoggingUtil.warn(securityAuditLogger, "JSON注入检测 - 输入长度: {} | 输入哈希: {}", input.length(), input.hashCode());
            return true;
        }

        if (OPTIMIZED_LDAP_INJECTION_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到LDAP注入攻击");
            LoggingUtil.warn(securityAuditLogger, "LDAP注入检测 - 输入长度: {} | 输入哈希: {}", input.length(), input.hashCode());
            return true;
        }

        if (OPTIMIZED_UNICODE_MALICIOUS_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到Unicode恶意字符");
            LoggingUtil.warn(securityAuditLogger, "Unicode恶意字符检测 - 输入长度: {} | 输入哈希: {}", input.length(), input.hashCode());
            return true;
        }

        if (OPTIMIZED_NOSQL_INJECTION_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到NoSQL注入攻击");
            LoggingUtil.warn(securityAuditLogger, "NoSQL注入检测 - 输入长度: {} | 输入哈希: {}", input.length(), input.hashCode());
            return true;
        }

        if (OPTIMIZED_XML_INJECTION_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到XML注入攻击");
            LoggingUtil.warn(securityAuditLogger, "XML注入检测 - 输入长度: {} | 输入哈希: {}", input.length(), input.hashCode());
            return true;
        }

        // 7. 检查过量引号（最后检查，因为需要计数）
        if (containsExcessiveQuotes(input)) {
            return true;
        }

        LoggingUtil.debug(logger, "优化检测未发现恶意模式");
        return false;
    }

    /**
     * 降级检测机制
     */
    private boolean performFallbackDetection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 简单的关键字检测作为降级方案
        String lowerInput = input.toLowerCase();
        String[] dangerousKeywords = {
            "script", "javascript", "select", "union", "drop", "delete",
            "insert", "update", "exec", "eval", "alert", "../", "cmd.exe"
        };

        for (String keyword : dangerousKeywords) {
            if (lowerInput.contains(keyword)) {
                LoggingUtil.warn(logger, "降级检测发现危险关键字: {}", keyword);
                return true;
            }
        }

        return false;
    }

    /**
     * 检查过量引号
     */
    private boolean containsExcessiveQuotes(String input) {
        int singleQuoteCount = 0;
        int doubleQuoteCount = 0;

        for (char c : input.toCharArray()) {
            if (c == '\'') singleQuoteCount++;
            else if (c == '"') doubleQuoteCount++;
        }

        int threshold = getMaxQuotesThreshold();
        if (singleQuoteCount > threshold || doubleQuoteCount > threshold) {
            LoggingUtil.warn(logger, "检测到过量引号，单引号: {}, 双引号: {}, 阈值: {}",
                singleQuoteCount, doubleQuoteCount, threshold);
            LoggingUtil.warn(securityAuditLogger, "过量引号检测 - 单引号: {} | 双引号: {} | 阈值: {} | 输入长度: {}",
                singleQuoteCount, doubleQuoteCount, threshold, input.length());
            return true;
        }

        return false;
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String input) {
        // 使用输入的哈希值作为缓存键，节省内存
        return String.valueOf(input.hashCode());
    }

    /**
     * 获取最大引号阈值
     */
    private int getMaxQuotesThreshold() {
        if (securityDetectionProperties != null) {
            return securityDetectionProperties.getMaxQuotesThreshold();
        }
        return 4; // 默认值
    }

    /**
     * 检查安全检测是否启用
     */
    private boolean isSecurityDetectionEnabled() {
        if (securityDetectionProperties != null) {
            return securityDetectionProperties.isEnabled();
        }
        return true; // 默认启用
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        detectionCache.clear();
        LoggingUtil.info(logger, "安全检测缓存已清理");
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        return String.format("缓存大小: %d/%d, 命中率估算: %.2f%%",
            detectionCache.size(), MAX_CACHE_SIZE,
            detectionCache.size() > 0 ? (detectionCache.size() * 100.0 / MAX_CACHE_SIZE) : 0.0);
    }

    /**
     * 获取优化检测统计信息
     */
    public String getOptimizedDetectionInfo() {
        StringBuilder info = new StringBuilder();
        info.append("优化安全检测状态: ").append(isSecurityDetectionEnabled() ? "启用" : "禁用").append("\n");
        info.append("最大引号阈值: ").append(getMaxQuotesThreshold()).append("\n");
        info.append("缓存统计: ").append(getCacheStats()).append("\n");
        info.append("异步执行器状态: 活跃").append("\n");
        return info.toString();
    }
}
