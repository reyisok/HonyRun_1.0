package com.honyrun.service.security;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.honyrun.config.properties.SecurityDetectionProperties;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.util.ErrorDetailsUtil;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.TraceIdUtil;

import reactor.core.publisher.Mono;

/**
 * 统一安全检测服务
 *
 * 该服务整合了系统中所有的恶意输入检测逻辑，提供统一的安全检测接口。
 * 主要功能包括：
 * 1. SQL注入检测
 * 2. XSS攻击检测
 * 3. 路径遍历检测
 * 4. 命令注入检测
 * 5. 脚本注入检测
 * 6. 过量引号检测
 *
 * 设计原则：
 * - 统一检测逻辑，避免代码重复
 * - 可配置的检测规则
 * - 响应式编程支持
 * - 详细的日志记录
 * - 高性能的模式匹配
 * - 统一错误处理格式
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-13 10:30:00
 * @modified 2025-07-02 20:30:00
 * @version 2.0.0 - 集成ApiResponse和ErrorDetailsUtil
 */
@Service
public class UnifiedSecurityDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedSecurityDetectionService.class);

    // 安全审计专用日志记录器
    private static final Logger securityAuditLogger = LoggerFactory.getLogger("SECURITY_AUDIT");

    private final SecurityDetectionProperties securityDetectionProperties;
    private final ErrorDetailsUtil errorDetailsUtil;

    /**
     * 构造函数注入依赖
     *
     * @param securityDetectionProperties 安全检测配置属性（可选）
     * @param errorDetailsUtil 错误详情工具
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public UnifiedSecurityDetectionService(SecurityDetectionProperties securityDetectionProperties,
                                         ErrorDetailsUtil errorDetailsUtil) {
        this.securityDetectionProperties = securityDetectionProperties;
        this.errorDetailsUtil = errorDetailsUtil;
    }

    // 预编译的正则表达式模式，提高性能
    // 修正：移除与XSS相关的通用"script/javascript/vbscript"词项，避免正常文本（如"description"）误报
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i).*(\\bunion\\b|\\bselect\\b|\\binsert\\b|\\bupdate\\b|\\bdelete\\b|\\bdrop\\b|\\bcreate\\b|\\balter\\b|\\bexec\\b|or\\s+1\\s*=\\s*1|'\\s*or\\s*'1'\\s*=\\s*'1|--|;--|/\\*.*\\*/).*"
    );

    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i).*(<script|</script|javascript:|vbscript:|onload=|onerror=|onclick=|onmouseover=|onfocus=|onblur=|onchange=|onsubmit=|alert\\(|eval\\(|expression\\().*"
    );

    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "(?i).*(\\.\\.[\\/\\\\]|%2e%2e%2f|%2e%2e%5c|etc[\\/\\\\]passwd|%65%74%63%2f%70%61%73%73%77%64|windows[\\/\\\\]system32|cmd\\.exe|powershell|\\.\\.\\.\\.[\\/\\\\]).*"
    );

    // 扩展：支持更多常见命令注入载荷（nc/netcat/curl/wget/bash/sh/powershell/cmd 等）
    private static final Pattern COMMAND_INJECTION_PATTERN = Pattern.compile(
        "(?i).*(;\\s*(rm|del|rmdir|rd)|\\|\\s*(cat|type|more|less|nc|netcat|curl|wget|bash|sh|powershell|cmd)|&&|\\|\\||`|\\$\\(|\\$\\{|exec\\s*\\(|system\\s*\\(|eval\\s*\\().*"
    );

    // 新增：JSON注入检测模式
    private static final Pattern JSON_INJECTION_PATTERN = Pattern.compile(
        "(?i).*(\\{\\s*[\"']\\s*\\$\\s*[\"']|\\[\\s*\\{\\s*[\"']\\s*\\$|__proto__|constructor|prototype|\\\\u[0-9a-f]{4}|\\\\x[0-9a-f]{2}).*"
    );

    // 新增：LDAP注入检测模式
    private static final Pattern LDAP_INJECTION_PATTERN = Pattern.compile(
        "(?i).*(\\*\\)|\\(\\*|\\)\\(|\\*\\(|\\&\\(|\\|\\(|!\\(|\\(\\&|\\(\\||\\(!).*"
    );

    // 新增：Unicode字符恶意模式检测 - 修正：排除正常URL编码字符，避免误判
    // 只检测真正的恶意Unicode字符，如控制字符、脚本标签的Unicode编码等
   // 新增：Unicode恶意字符检测模式（支持更多编码格式）
    private static final Pattern UNICODE_MALICIOUS_PATTERN = Pattern.compile(
        ".*(\\\\u003[cC]|\\\\u003[eE]|\\\\u0022|\\\\u0027|\\\\u003d|\\\\u0026|\\\\u007c|\\\\u003b|\\\\u002d\\\\u002d|\\\\x3[cCeE]|\\\\x2[27]|\\\\x3[d=]|\\\\x2[6&]|\\\\x7[cC]|\\\\x3[bB]|%3[cCeE]|%2[27]|%3[d=]|%2[6&]|%7[cC]|%3[bB]|&#[0-9]+;|&#x[0-9a-fA-F]+;).*"
    );

    // 新增：NoSQL注入检测模式
    private static final Pattern NOSQL_INJECTION_PATTERN = Pattern.compile(
        "(?i).*(\\$where|\\$ne|\\$gt|\\$lt|\\$gte|\\$lte|\\$in|\\$nin|\\$regex|\\$exists|\\$type|\\$mod|\\$all|\\$size|\\$elemMatch).*"
    );

    // 新增：XML注入检测模式
    private static final Pattern XML_INJECTION_PATTERN = Pattern.compile(
        "(?i).*(<\\?xml|<!DOCTYPE|<!ENTITY|SYSTEM\\s+[\"']|PUBLIC\\s+[\"']|\\&[a-zA-Z]+;|<\\!\\[CDATA\\[).*"
    );

    // 新增：图片尺寸限制常量
    private static final int MAX_IMAGE_WIDTH = 2500;
    private static final int MAX_IMAGE_HEIGHT = 2500;

    /**
     * 检测输入是否包含恶意模式
     *
     * @param input 待检测的输入字符串
     * @return 如果包含恶意模式返回true，否则返回false
     */
    public boolean containsMaliciousPatterns(String input) {
        if (input == null || input.trim().isEmpty()) {
            LoggingUtil.debug(logger, "输入为空或null，跳过恶意检测");
            return false;
        }

        // 检查是否启用安全检测
        if (!isSecurityDetectionEnabled()) {
            LoggingUtil.debug(logger, "安全检测已禁用");
            return false;
        }

        // 检查是否为JWT token格式，如果是则跳过恶意模式检测
        if (isJwtTokenFormat(input)) {
            LoggingUtil.debug(logger, "检测到JWT token格式，跳过恶意模式检测");
            return false;
        }

        LoggingUtil.debug(logger, "开始检查恶意模式，输入长度: {}", input.length());

        // 1. 检查配置的恶意模式
        if (containsConfiguredPatterns(input)) {
            return true;
        }

        // 2. 检查SQL注入模式
        if (containsSqlInjection(input)) {
            return true;
        }

        // 3. 检查XSS攻击模式
        if (containsXssAttack(input)) {
            return true;
        }

        // 4. 检查路径遍历模式
        if (containsPathTraversal(input)) {
            return true;
        }

        // 5. 检查命令注入模式
        if (containsCommandInjection(input)) {
            return true;
        }

        // 6. 检查过量引号
        if (containsExcessiveQuotes(input)) {
            return true;
        }

        // 7. 检查JSON注入模式
        if (containsJsonInjection(input)) {
            return true;
        }

        // 8. 检查LDAP注入模式
        if (containsLdapInjection(input)) {
            return true;
        }

        // 9. 检查Unicode恶意字符
        if (containsUnicodeMalicious(input)) {
            return true;
        }

        // 10. 检查NoSQL注入模式
        if (containsNoSqlInjection(input)) {
            return true;
        }

        // 11. 检查XML注入模式
        if (containsXmlInjection(input)) {
            return true;
        }

        // 12. 检查图片尺寸限制（针对包含图片数据的请求）
        if (containsOversizedImage(input)) {
            return true;
        }

        LoggingUtil.debug(logger, "未检测到恶意模式");
        return false;
    }

    /**
     * 响应式版本的恶意模式检测
     *
     * @param input 待检测的输入字符串
     * @return 包含检测结果的Mono
     */
    public Mono<Boolean> containsMaliciousPatternsReactive(String input) {
        return Mono.fromCallable(() -> containsMaliciousPatterns(input))
                .doOnNext(result -> {
                    if (result) {
                        LoggingUtil.warn(logger, "响应式检测发现恶意模式，输入长度: {} 字符", input != null ? input.length() : 0);
                        // 记录到安全审计日志
                        LoggingUtil.warn(securityAuditLogger, "响应式恶意模式检测 - 输入长度: {} | 完整输入: {}", input != null ? input.length() : 0, input);
                    }
                })
                .onErrorReturn(false);
    }

    /**
     * 检查配置的恶意模式
     */
    private boolean containsConfiguredPatterns(String input) {
        List<String> patterns = getConfiguredPatterns();
        String lowerInput = input.toLowerCase();

        for (String pattern : patterns) {
            String lowerPattern = pattern.toLowerCase();
            if (lowerInput.contains(lowerPattern)) {
                LoggingUtil.warn(logger, "检测到配置的恶意模式: {}，输入长度: {} 字符，匹配模式: CONFIGURED_PATTERNS，输入内容: {}",
                    pattern, input.length(), input);
                // 记录到安全审计日志，包含详细的匹配信息
                LoggingUtil.warn(securityAuditLogger, "配置恶意模式检测 - 匹配模式: {} | 输入长度: {} | 完整输入: {} | 检测规则: 基于配置文件的恶意模式列表",
                    pattern, input.length(), input);
                return true;
            }
        }
        LoggingUtil.debug(logger, "配置恶意模式检测通过，输入长度: {} 字符", input.length());
        return false;
    }

    /**
     * 检查SQL注入模式
     */
    private boolean containsSqlInjection(String input) {
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到SQL注入模式，输入长度: {} 字符，匹配模式: SQL_INJECTION_PATTERN，输入内容: {}",
                input.length(), input);
            // 记录到安全审计日志，包含详细的匹配信息
            LoggingUtil.warn(securityAuditLogger, "SQL注入检测 - 输入长度: {} | 匹配模式: SQL_INJECTION_PATTERN | 完整输入: {} | 检测规则: union/select/insert/update/delete/drop等SQL关键字",
                input.length(), input);
            return true;
        }
        LoggingUtil.debug(logger, "SQL注入检测通过，输入长度: {} 字符", input.length());
        return false;
    }

    /**
     * 检查XSS攻击模式
     */
    private boolean containsXssAttack(String input) {
        if (XSS_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到XSS攻击模式，输入长度: {} 字符，匹配模式: XSS_PATTERN，输入内容: {}",
                input.length(), input);
            // 记录到安全审计日志，包含详细的匹配信息
            LoggingUtil.warn(securityAuditLogger, "XSS攻击检测 - 输入长度: {} | 匹配模式: XSS_PATTERN | 完整输入: {} | 检测规则: script标签/javascript/事件处理器等",
                input.length(), input);
            return true;
        }
        LoggingUtil.debug(logger, "XSS攻击检测通过，输入长度: {} 字符", input.length());
        return false;
    }

    /**
     * 检查路径遍历模式
     */
    private boolean containsPathTraversal(String input) {
        if (PATH_TRAVERSAL_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到路径遍历模式，输入长度: {} 字符，匹配模式: PATH_TRAVERSAL_PATTERN，输入内容: {}",
                input.length(), input);
            // 记录到安全审计日志，包含详细的匹配信息
            LoggingUtil.warn(securityAuditLogger, "路径遍历攻击检测 - 输入长度: {} | 匹配模式: PATH_TRAVERSAL_PATTERN | 完整输入: {} | 检测规则: ../等路径遍历字符",
                input.length(), input);
            return true;
        }
        LoggingUtil.debug(logger, "路径遍历检测通过，输入长度: {} 字符", input.length());
        return false;
    }

    /**
     * 检查命令注入模式
     */
    private boolean containsCommandInjection(String input) {
        if (COMMAND_INJECTION_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到命令注入模式，输入长度: {} 字符，匹配模式: COMMAND_INJECTION_PATTERN，输入内容: {}",
                input.length(), input);
            // 记录到安全审计日志，包含详细的匹配信息
            LoggingUtil.warn(securityAuditLogger, "命令注入攻击检测 - 输入长度: {} | 匹配模式: COMMAND_INJECTION_PATTERN | 完整输入: {} | 检测规则: 命令分隔符和系统命令",
                input.length(), input);
            return true;
        }
        LoggingUtil.debug(logger, "命令注入检测通过，输入长度: {} 字符", input.length());
        return false;
    }

    /**
     * 检查过量引号
     */
    private boolean containsExcessiveQuotes(String input) {
        int quoteCount = (int) input.chars().filter(ch -> ch == '\'' || ch == '"').count();
        int maxQuotes = getMaxQuotesThreshold();

        if (quoteCount > maxQuotes) {
            LoggingUtil.warn(logger, "检测到过多引号，引号数量: {}, 最大允许: {}，输入内容: {}",
                quoteCount, maxQuotes, input);
            // 记录到安全审计日志，包含详细的匹配信息
            LoggingUtil.warn(securityAuditLogger, "过量引号检测 - 引号数量: {} | 最大允许: {} | 完整输入: {} | 检测规则: 单双引号数量超过阈值",
                quoteCount, maxQuotes, input);
            return true;
        }
        LoggingUtil.debug(logger, "过量引号检测通过，引号数量: {}, 最大允许: {}", quoteCount, maxQuotes);
        return false;
    }

    /**
     * 获取配置的恶意模式列表
     */
    private List<String> getConfiguredPatterns() {
        if (securityDetectionProperties != null && securityDetectionProperties.getPatterns() != null) {
            return securityDetectionProperties.getPatterns();
        }

        // 默认恶意模式列表
        return List.of(
            "<script", "</script", "javascript:", "onerror=", "onload=", "onclick=", "onmouseover=",
            "union select", "select * from", "drop table", "or 1=1", "--", "/*", "*/", ";--",
            "../", "..\\", "etc/passwd", "cmd.exe", "powershell", "rm -rf", "del /f",
            "eval(", "alert(", "confirm(", "prompt(", "document.cookie", "window.location"
        );
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
     * 检查是否为JWT token格式
     * JWT token格式为: header.payload.signature，每部分都是Base64URL编码
     *
     * @param input 待检测的输入字符串
     * @return 如果是JWT token格式返回true，否则返回false
     */
    private boolean isJwtTokenFormat(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // JWT token应该包含两个点号，分为三部分
        String[] parts = input.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        // 检查每部分是否为有效的Base64URL编码
        for (String part : parts) {
            if (!isValidBase64Url(part)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查字符串是否为有效的Base64URL编码
     * Base64URL编码只包含: A-Z, a-z, 0-9, -, _
     *
     * @param input 待检测的字符串
     * @return 如果是有效的Base64URL编码返回true，否则返回false
     */
    private boolean isValidBase64Url(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        // Base64URL字符集: A-Z, a-z, 0-9, -, _
        // 不包含填充字符=（JWT通常不使用填充）
        return input.matches("^[A-Za-z0-9_-]+$");
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
     * 检查JSON注入模式
     */
    private boolean containsJsonInjection(String input) {
        if (JSON_INJECTION_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到JSON注入模式，输入长度: {} 字符，匹配模式: JSON_INJECTION_PATTERN，输入内容: {}",
                input.length(), input);
            // 记录到安全审计日志，包含详细的匹配信息
            LoggingUtil.warn(securityAuditLogger, "JSON注入攻击检测 - 输入长度: {} | 匹配模式: JSON_INJECTION_PATTERN | 完整输入: {} | 检测规则: $符号/原型污染/__proto__等",
                input.length(), input);
            return true;
        }
        LoggingUtil.debug(logger, "JSON注入检测通过，输入长度: {} 字符", input.length());
        return false;
    }

    /**
     * 检查LDAP注入模式
     */
    private boolean containsLdapInjection(String input) {
        if (LDAP_INJECTION_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到LDAP注入模式，输入长度: {} 字符，匹配模式: LDAP_INJECTION_PATTERN，输入内容: {}",
                input.length(), input);
            // 记录到安全审计日志，包含详细的匹配信息
            LoggingUtil.warn(securityAuditLogger, "LDAP注入攻击检测 - 输入长度: {} | 匹配模式: LDAP_INJECTION_PATTERN | 完整输入: {} | 检测规则: LDAP查询特殊字符组合",
                input.length(), input);
            return true;
        }
        LoggingUtil.debug(logger, "LDAP注入检测通过，输入长度: {} 字符", input.length());
        return false;
    }

    /**
     * 检查Unicode恶意字符
     */
    private boolean containsUnicodeMalicious(String input) {
        if (UNICODE_MALICIOUS_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到Unicode恶意字符，输入长度: {} 字符，匹配模式: UNICODE_MALICIOUS_PATTERN，输入内容: {}",
                input.length(), input);
            // 记录到安全审计日志，包含详细的匹配信息
            LoggingUtil.warn(securityAuditLogger, "Unicode恶意字符检测 - 输入长度: {} | 匹配模式: UNICODE_MALICIOUS_PATTERN | 完整输入: {} | 检测规则: 仅检测真正恶意的Unicode字符如脚本标签编码",
                input.length(), input);
            return true;
        }
        LoggingUtil.debug(logger, "Unicode恶意字符检测通过，输入长度: {} 字符", input.length());
        return false;
    }

    /**
     * 检查NoSQL注入模式
     */
    private boolean containsNoSqlInjection(String input) {
        if (NOSQL_INJECTION_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到NoSQL注入模式，输入长度: {} 字符，匹配模式: NOSQL_INJECTION_PATTERN，输入内容: {}",
                input.length(), input);
            // 记录到安全审计日志，包含详细的匹配信息
            LoggingUtil.warn(securityAuditLogger, "NoSQL注入攻击检测 - 输入长度: {} | 匹配模式: NOSQL_INJECTION_PATTERN | 完整输入: {} | 检测规则: MongoDB查询操作符$where/$ne/$gt等",
                input.length(), input);
            return true;
        }
        LoggingUtil.debug(logger, "NoSQL注入检测通过，输入长度: {} 字符", input.length());
        return false;
    }

    /**
     * 检查XML注入模式
     */
    private boolean containsXmlInjection(String input) {
        if (XML_INJECTION_PATTERN.matcher(input).find()) {
            LoggingUtil.warn(logger, "检测到XML注入模式，输入长度: {} 字符，匹配模式: XML_INJECTION_PATTERN，输入内容: {}",
                input.length(), input);
            // 记录到安全审计日志，包含详细的匹配信息
            LoggingUtil.warn(securityAuditLogger, "XML注入模式检测 - 输入长度: {} | 匹配模式: XML_INJECTION_PATTERN | 完整输入: {} | 检测规则: XML声明/DOCTYPE/ENTITY/CDATA等",
                input.length(), input);
            return true;
        }
        LoggingUtil.debug(logger, "XML注入检测通过，输入长度: {} 字符", input.length());
        return false;
    }



    /**
      * 批量检测多个输入
      *
      * @param inputs 输入字符串列表
      * @return 如果任何一个输入包含恶意模式返回true
      */
     public boolean containsMaliciousPatternsInBatch(List<String> inputs) {
         if (inputs == null || inputs.isEmpty()) {
             return false;
         }

         for (String input : inputs) {
             if (containsMaliciousPatterns(input)) {
                 return true;
             }
         }
         return false;
     }

     /**
      * 响应式批量检测
      *
      * @param inputs 输入字符串列表
      * @return 包含检测结果的Mono
      */
     public Mono<Boolean> containsMaliciousPatternsInBatchReactive(List<String> inputs) {
         return Mono.fromCallable(() -> containsMaliciousPatternsInBatch(inputs))
                 .onErrorReturn(false);
     }

     /**
      * 获取检测统计信息
      *
      * @return 检测配置信息
      */
     public String getDetectionInfo() {
         StringBuilder info = new StringBuilder();
         info.append("统一安全检测服务配置信息:\n");
         info.append("- 检测状态: ").append(isSecurityDetectionEnabled() ? "启用" : "禁用").append("\n");
         info.append("- 配置模式数量: ").append(getConfiguredPatterns().size()).append("\n");
         info.append("- 最大引号阈值: ").append(getMaxQuotesThreshold()).append("\n");
         return info.toString();
     }

    /**
     * 检查输入是否包含超限图片
     *
     * @param input 输入字符串，可能包含Base64编码的图片数据
     * @return 如果包含超限图片返回true，否则返回false
     */
    private boolean containsOversizedImage(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        try {
            // 检查是否包含imageData字段（JSON格式）
            if (input.contains("\"imageData\"") || input.contains("'imageData'")) {
                // 尝试提取Base64图片数据
                String base64Data = extractBase64ImageData(input);
                if (base64Data != null && !base64Data.isEmpty()) {
                    return isImageOversized(base64Data);
                }
            }

            // 检查是否为纯Base64图片数据
            if (isBase64ImageData(input)) {
                return isImageOversized(input);
            }

        } catch (Exception e) {
            LoggingUtil.debug(logger, "图片尺寸检测过程中出现异常，跳过检测: {}", e.getMessage());
        }

        return false;
    }

    /**
     * 从JSON字符串中提取Base64图片数据
     *
     * @param jsonInput JSON格式的输入字符串
     * @return Base64图片数据，如果未找到返回null
     */
    private String extractBase64ImageData(String jsonInput) {
        try {
            // 简单的正则提取，匹配 "imageData":"data:image/...;base64,..." 或 "imageData":"iVBORw0KGgo..."
            Pattern imageDataPattern = Pattern.compile("\"imageData\"\\s*:\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher matcher = imageDataPattern.matcher(jsonInput);

            if (matcher.find()) {
                String imageData = matcher.group(1);

                // 如果是data URL格式，提取base64部分
                if (imageData.startsWith("data:image/")) {
                    int base64Index = imageData.indexOf("base64,");
                    if (base64Index != -1) {
                        return imageData.substring(base64Index + 7);
                    }
                } else {
                    // 直接是base64数据
                    return imageData;
                }
            }
        } catch (Exception e) {
            LoggingUtil.debug(logger, "提取Base64图片数据失败: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 检查字符串是否为Base64图片数据
     *
     * @param input 输入字符串
     * @return 如果是Base64图片数据返回true
     */
    private boolean isBase64ImageData(String input) {
        if (input == null || input.length() < 100) {
            return false;
        }

        // 检查是否以常见图片格式的Base64开头
        String trimmed = input.trim();
        return trimmed.startsWith("iVBORw0KGgo") || // PNG
               trimmed.startsWith("/9j/") ||         // JPEG
               trimmed.startsWith("R0lGODlh") ||     // GIF
               trimmed.startsWith("Qk") ||           // BMP
               (trimmed.startsWith("data:image/") && trimmed.contains("base64,"));
    }

    /**
     * 检查Base64图片数据是否超限
     *
     * @param base64Data Base64编码的图片数据
     * @return 如果图片尺寸超限返回true
     */
    private boolean isImageOversized(String base64Data) {
        try {
            // 解码Base64数据
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // 读取图片尺寸
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                LoggingUtil.debug(logger, "无法解析图片数据，跳过尺寸检测");
                return false;
            }

            int width = image.getWidth();
            int height = image.getHeight();

            // 检查是否超过限制
            if (width > MAX_IMAGE_WIDTH || height > MAX_IMAGE_HEIGHT) {
                LoggingUtil.warn(logger, "检测到超限图片尺寸: {}x{} (限制: {}x{})，输入数据长度: {} 字符",
                    width, height, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT, base64Data.length());

                // 记录到安全审计日志
                LoggingUtil.warn(securityAuditLogger, "图片尺寸超限检测 - 实际尺寸: {}x{} | 最大限制: {}x{} | Base64数据长度: {} | 检测规则: 图片尺寸不得超过{}x{}像素",
                    width, height, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT, base64Data.length(), MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
                return true;
            }

            LoggingUtil.debug(logger, "图片尺寸检测通过: {}x{}", width, height);
            return false;

        } catch (IllegalArgumentException e) {
            LoggingUtil.debug(logger, "Base64解码失败，跳过图片尺寸检测: {}", e.getMessage());
            return false;
        } catch (IOException e) {
            LoggingUtil.debug(logger, "图片读取失败，跳过图片尺寸检测: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            LoggingUtil.warn(logger, "图片尺寸检测过程中出现异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 创建安全检测错误响应
     *
     * @param detectionType 检测类型
     * @param input 输入内容
     * @param requestPath 请求路径
     * @return 统一格式的错误响应
     */
    public ApiResponse<Object> createSecurityErrorResponse(String detectionType, String input, String requestPath) {
        String traceId = TraceIdUtil.generateTraceId();
        Map<String, Object> details = errorDetailsUtil.buildSimpleErrorDetails(
            "SECURITY_DETECTION_FAILURE", requestPath);

        // 添加安全检测特定信息
        details.put("detectionType", detectionType);
        details.put("inputLength", input != null ? input.length() : 0);
        details.put("timestamp", java.time.LocalDateTime.now());

        LoggingUtil.warn(logger, "安全检测失败 - 类型: {}, 路径: {}, 追踪ID: {}",
            detectionType, requestPath, traceId);

        return ApiResponse.error("400", "输入包含恶意内容，安全检测失败", traceId, details, requestPath);
    }

    /**
     * 创建安全检测成功响应
     *
     * @param message 成功消息
     * @return 统一格式的成功响应
     */
    public ApiResponse<String> createSecuritySuccessResponse(String message) {
        return ApiResponse.success(message);
    }

    /**
     * 响应式安全检测错误处理
     *
     * @param detectionType 检测类型
     * @param input 输入内容
     * @param requestPath 请求路径
     * @return 包含错误响应的Mono
     */
    public Mono<ApiResponse<Object>> createSecurityErrorResponseReactive(String detectionType, String input, String requestPath) {
        return Mono.fromCallable(() -> createSecurityErrorResponse(detectionType, input, requestPath))
                .doOnNext(response -> LoggingUtil.debug(logger, "创建安全检测错误响应完成"))
                .onErrorResume(ex -> {
                    LoggingUtil.error(logger, "创建安全检测错误响应失败", ex);
                    return Mono.just(ApiResponse.error("500", "系统内部错误"));
                });
    }

    /**
     * 批量安全检测结果处理
     *
     * @param inputs 输入列表
     * @param requestPath 请求路径
     * @return 检测结果响应
     */
    public ApiResponse<Map<String, Object>> processBatchSecurityDetection(List<String> inputs, String requestPath) {
        if (inputs == null || inputs.isEmpty()) {
            return ApiResponse.success("批量安全检测完成：无输入内容");
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalInputs", inputs.size());
        result.put("detectionTime", java.time.LocalDateTime.now());

        boolean hasMalicious = false;
        for (int i = 0; i < inputs.size(); i++) {
            String input = inputs.get(i);
            if (containsMaliciousPatterns(input)) {
                hasMalicious = true;
                result.put("maliciousInputIndex", i);
                result.put("maliciousInputLength", input.length());
                break;
            }
        }

        if (hasMalicious) {
            String traceId = TraceIdUtil.generateTraceId();
            Map<String, Object> details = errorDetailsUtil.buildSimpleErrorDetails(
                "BATCH_SECURITY_DETECTION_FAILURE", requestPath);
            details.putAll(result);

            return ApiResponse.error("400", "批量输入中包含恶意内容", traceId, details, requestPath);
        }

        result.put("status", "PASSED");
        return ApiResponse.success(result, "批量安全检测通过");
    }

    /**
     * 公开的SQL注入检测方法
     *
     * @param input 待检测的输入字符串
     * @return 如果检测到SQL注入返回true，否则返回false
     */
    public boolean detectSqlInjection(String input) {
        return containsSqlInjection(input);
    }

    /**
     * 公开的XSS攻击检测方法
     *
     * @param input 待检测的输入字符串
     * @return 如果检测到XSS攻击返回true，否则返回false
     */
    public boolean detectXssAttack(String input) {
        return containsXssAttack(input);
    }

    /**
     * 公开的路径遍历检测方法
     *
     * @param input 待检测的输入字符串
     * @return 如果检测到路径遍历攻击返回true，否则返回false
     */
    public boolean detectPathTraversal(String input) {
        return containsPathTraversal(input);
    }

    /**
     * 公开的命令注入检测方法
     *
     * @param input 待检测的输入字符串
     * @return 如果检测到命令注入攻击返回true，否则返回false
     */
    public boolean detectCommandInjection(String input) {
        return containsCommandInjection(input);
    }

    /**
     * 公开的NoSQL注入检测方法
     *
     * @param input 待检测的输入字符串
     * @return 如果检测到NoSQL注入攻击返回true，否则返回false
     */
    public boolean detectNoSqlInjection(String input) {
        return containsNoSqlInjection(input);
    }

    /**
     * 输入验证方法
     * 验证输入是否符合安全要求
     *
     * @param input 待验证的输入字符串
     * @return 如果输入有效返回true，否则返回false
     */
    public boolean validateInput(String input) {
        if (input == null) {
            LoggingUtil.warn(logger, "输入验证失败：输入为null");
            return false;
        }

        // 检查是否包含恶意模式
        if (containsMaliciousPatterns(input)) {
            LoggingUtil.warn(logger, "输入验证失败：包含恶意模式");
            return false;
        }

        // 检查长度限制（可配置）
        int maxLength = getMaxInputLength();
        if (input.length() > maxLength) {
            LoggingUtil.warn(logger, "输入验证失败：长度超过限制 {} > {}", input.length(), maxLength);
            return false;
        }

        LoggingUtil.debug(logger, "输入验证通过");
        return true;
    }

    /**
     * 输入清理方法
     * 清理输入中的潜在恶意内容
     *
     * @param input 待清理的输入字符串
     * @return 清理后的安全字符串
     */
    public String sanitizeInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        String sanitized = input;

        // 移除HTML标签
        sanitized = sanitized.replaceAll("<[^>]*>", "");

        // 转义特殊字符
        sanitized = sanitized.replace("&", "&amp;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;")
                            .replace("\"", "&quot;")
                            .replace("'", "&#x27;")
                            .replace("/", "&#x2F;");

        // 移除SQL注入相关字符
        sanitized = sanitized.replaceAll("(?i)(union|select|insert|update|delete|drop|create|alter|exec)", "");

        // 移除脚本相关内容
        sanitized = sanitized.replaceAll("(?i)(javascript:|vbscript:|onload=|onerror=|onclick=)", "");

        // 移除路径遍历字符
        sanitized = sanitized.replace("../", "").replace("..\\", "");

        // 限制长度
        int maxLength = getMaxInputLength();
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }

        LoggingUtil.debug(logger, "输入清理完成，原长度: {}, 清理后长度: {}", input.length(), sanitized.length());
        return sanitized;
    }

    /**
     * 获取最大输入长度限制
     *
     * @return 最大输入长度
     */
    private int getMaxInputLength() {
        if (securityDetectionProperties != null && securityDetectionProperties.getMaxInputLength() != null) {
            return securityDetectionProperties.getMaxInputLength();
        }
        return 10000; // 默认最大长度
    }

}
