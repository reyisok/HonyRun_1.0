package com.honyrun.service.security;

import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import com.honyrun.config.properties.SecurityInputLimitProperties;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;

/**
 * 增强的输入验证服务
 *
 * 该服务提供全面的输入长度和格式验证，包括：
 * 1. HTTP头部长度验证
 * 2. Cookie长度验证
 * 3. 文件名长度和格式验证
 * 4. URL参数验证
 * 5. 请求体大小验证
 * 6. 用户代理和引用页验证
 *
 * 设计原则：
 * - 统一的验证逻辑
 * - 可配置的限制参数
 * - 响应式编程支持
 * - 详细的日志记录
 * - 高性能的验证算法
 *
 * @author Mr.Rey
 * @version 1.0.0
 * @created 2025-07-01 16:30:00
 * @modified 2025-07-01 16:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
public class EnhancedInputValidationService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedInputValidationService.class);

    // 安全审计专用日志记录器
    private static final Logger securityAuditLogger = LoggerFactory.getLogger("SECURITY_AUDIT");

    private final SecurityInputLimitProperties securityInputLimitProperties;

    /**
     * 构造函数注入依赖
     *
     * @param securityInputLimitProperties 安全输入限制配置属性
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public EnhancedInputValidationService(SecurityInputLimitProperties securityInputLimitProperties) {
        this.securityInputLimitProperties = securityInputLimitProperties;
    }

    // 文件名非法字符检测模式
    private static final Pattern ILLEGAL_FILENAME_PATTERN = Pattern.compile(
        ".*[<>:\"/\\\\|?*\\x00-\\x1f].*"
    );

    // 文件扩展名验证模式
    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile(
        "^\\.[a-zA-Z0-9]{1,10}$"
    );

    // Cookie名称验证模式
    private static final Pattern COOKIE_NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_-]+$"
    );

    /**
     * 验证HTTP头部
     *
     * @param request HTTP请求对象
     * @return 验证结果的Mono
     */
    public Mono<Boolean> validateHttpHeaders(ServerHttpRequest request) {
        return Mono.fromCallable(() -> {
            HttpHeaders headers = request.getHeaders();

            // 1. 验证头部总大小
            int totalHeadersSize = calculateHeadersSize(headers);
            if (totalHeadersSize > securityInputLimitProperties.getHttp().getMaxHeadersSize()) {
                LoggingUtil.warn(logger, "HTTP头部总大小超限: {} > {}",
                    totalHeadersSize, securityInputLimitProperties.getHttp().getMaxHeadersSize());
                LoggingUtil.warn(securityAuditLogger, "HTTP头部大小超限 - 实际大小: {} | 最大限制: {}",
                    totalHeadersSize, securityInputLimitProperties.getHttp().getMaxHeadersSize());
                return false;
            }

            // 2. 验证单个头部长度
            for (Map.Entry<String, java.util.List<String>> entry : headers.entrySet()) {
                String headerName = entry.getKey();
                for (String headerValue : entry.getValue()) {
                    if (headerValue.length() > securityInputLimitProperties.getHttp().getMaxHeaderLength()) {
                        LoggingUtil.warn(logger, "HTTP头部 {} 值长度超限: {} > {}",
                            headerName, headerValue.length(), securityInputLimitProperties.getHttp().getMaxHeaderLength());
                        LoggingUtil.warn(securityAuditLogger, "HTTP头部值长度超限 - 头部名: {} | 实际长度: {} | 最大限制: {}",
                            headerName, headerValue.length(), securityInputLimitProperties.getHttp().getMaxHeaderLength());
                        return false;
                    }
                }
            }

            // 3. 验证特定头部
            if (!validateSpecificHeaders(headers)) {
                return false;
            }

            LoggingUtil.debug(logger, "HTTP头部验证通过，总大小: {} 字节", totalHeadersSize);
            return true;
        }).onErrorReturn(false);
    }

    /**
     * 验证Cookie
     *
     * @param request HTTP请求对象
     * @return 验证结果的Mono
     */
    public Mono<Boolean> validateCookies(ServerHttpRequest request) {
        return Mono.fromCallable(() -> {
            String cookieHeader = request.getHeaders().getFirst("Cookie");
            if (cookieHeader == null || cookieHeader.isEmpty()) {
                return true; // 没有Cookie，验证通过
            }

            // 1. 验证Cookie总大小
            if (cookieHeader.length() > securityInputLimitProperties.getHttp().getMaxCookieSize()) {
                LoggingUtil.warn(logger, "Cookie总大小超限: {} > {}",
                    cookieHeader.length(), securityInputLimitProperties.getHttp().getMaxCookieSize());
                LoggingUtil.warn(securityAuditLogger, "Cookie大小超限 - 实际大小: {} | 最大限制: {}",
                    cookieHeader.length(), securityInputLimitProperties.getHttp().getMaxCookieSize());
                return false;
            }

            // 2. 验证单个Cookie长度
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String trimmedCookie = cookie.trim();
                if (trimmedCookie.length() > securityInputLimitProperties.getHttp().getMaxSingleCookieLength()) {
                    LoggingUtil.warn(logger, "单个Cookie长度超限: {} > {}",
                        trimmedCookie.length(), securityInputLimitProperties.getHttp().getMaxSingleCookieLength());
                    LoggingUtil.warn(securityAuditLogger, "单个Cookie长度超限 - 实际长度: {} | 最大限制: {}",
                        trimmedCookie.length(), securityInputLimitProperties.getHttp().getMaxSingleCookieLength());
                    return false;
                }

                // 3. 验证Cookie名称格式
                if (trimmedCookie.contains("=")) {
                    String cookieName = trimmedCookie.split("=")[0].trim();
                    if (!COOKIE_NAME_PATTERN.matcher(cookieName).matches()) {
                        LoggingUtil.warn(logger, "Cookie名称格式非法: {}", cookieName);
                        LoggingUtil.warn(securityAuditLogger, "Cookie名称格式非法 - Cookie名: {}", cookieName);
                        return false;
                    }
                }
            }

            LoggingUtil.debug(logger, "Cookie验证通过，总大小: {} 字节", cookieHeader.length());
            return true;
        }).onErrorReturn(false);
    }

    /**
     * 验证文件名
     *
     * @param filename 文件名
     * @return 验证结果的Mono
     */
    public Mono<Boolean> validateFilename(String filename) {
        return Mono.fromCallable(() -> {
            if (filename == null || filename.trim().isEmpty()) {
                LoggingUtil.debug(logger, "文件名为空，允许通过");
                return true; // 允许null和空值
            }

            String trimmedFilename = filename.trim();

            // 1. 验证文件名长度
            if (trimmedFilename.length() > securityInputLimitProperties.getFile().getMaxFilenameLength()) {
                LoggingUtil.warn(logger, "文件名长度超限: {} > {}",
                    trimmedFilename.length(), securityInputLimitProperties.getFile().getMaxFilenameLength());
                LoggingUtil.warn(securityAuditLogger, "文件名长度超限 - 实际长度: {} | 最大限制: {} | 文件名: {}",
                    trimmedFilename.length(), securityInputLimitProperties.getFile().getMaxFilenameLength(), trimmedFilename);
                return false;
            }

            // 2. 验证文件名非法字符
            if (ILLEGAL_FILENAME_PATTERN.matcher(trimmedFilename).matches()) {
                LoggingUtil.warn(logger, "文件名包含非法字符: {}", trimmedFilename);
                LoggingUtil.warn(securityAuditLogger, "文件名包含非法字符 - 文件名: {}", trimmedFilename);
                return false;
            }

            // 3. 验证文件扩展名
            if (trimmedFilename.contains(".")) {
                String extension = trimmedFilename.substring(trimmedFilename.lastIndexOf("."));
                if (extension.length() > securityInputLimitProperties.getFile().getMaxExtensionLength()) {
                    LoggingUtil.warn(logger, "文件扩展名长度超限: {} > {}",
                        extension.length(), securityInputLimitProperties.getFile().getMaxExtensionLength());
                    LoggingUtil.warn(securityAuditLogger, "文件扩展名长度超限 - 实际长度: {} | 最大限制: {} | 扩展名: {}",
                        extension.length(), securityInputLimitProperties.getFile().getMaxExtensionLength(), extension);
                    return false;
                }

                if (!FILE_EXTENSION_PATTERN.matcher(extension).matches()) {
                    LoggingUtil.warn(logger, "文件扩展名格式非法: {}", extension);
                    LoggingUtil.warn(securityAuditLogger, "文件扩展名格式非法 - 扩展名: {}", extension);
                    return false;
                }
            }

            LoggingUtil.debug(logger, "文件名验证通过: {}", trimmedFilename);
            return true;
        }).onErrorReturn(false);
    }

    /**
     * 验证用户代理字符串
     *
     * @param userAgent 用户代理字符串
     * @return 验证结果的Mono
     */
    public Mono<Boolean> validateUserAgent(String userAgent) {
        return Mono.fromCallable(() -> {
            if (userAgent == null) {
                return true; // 允许空的User-Agent
            }

            if (userAgent.length() > securityInputLimitProperties.getHttp().getMaxUserAgentLength()) {
                LoggingUtil.warn(logger, "User-Agent长度超限: {} > {}",
                    userAgent.length(), securityInputLimitProperties.getHttp().getMaxUserAgentLength());
                LoggingUtil.warn(securityAuditLogger, "User-Agent长度超限 - 实际长度: {} | 最大限制: {}",
                    userAgent.length(), securityInputLimitProperties.getHttp().getMaxUserAgentLength());
                return false;
            }

            LoggingUtil.debug(logger, "User-Agent验证通过，长度: {} 字符", userAgent.length());
            return true;
        }).onErrorReturn(false);
    }

    /**
     * 验证引用页URL
     *
     * @param referer 引用页URL
     * @return 验证结果的Mono
     */
    public Mono<Boolean> validateReferer(String referer) {
        return Mono.fromCallable(() -> {
            if (referer == null) {
                return true; // 允许空的Referer
            }

            if (referer.length() > securityInputLimitProperties.getHttp().getMaxRefererLength()) {
                LoggingUtil.warn(logger, "Referer长度超限: {} > {}",
                    referer.length(), securityInputLimitProperties.getHttp().getMaxRefererLength());
                LoggingUtil.warn(securityAuditLogger, "Referer长度超限 - 实际长度: {} | 最大限制: {}",
                    referer.length(), securityInputLimitProperties.getHttp().getMaxRefererLength());
                return false;
            }

            LoggingUtil.debug(logger, "Referer验证通过，长度: {} 字符", referer.length());
            return true;
        }).onErrorReturn(false);
    }

    /**
     * 综合验证请求
     *
     * @param request HTTP请求对象
     * @return 验证结果的Mono
     */
    public Mono<Boolean> validateRequest(ServerHttpRequest request) {
        return validateHttpHeaders(request)
                .flatMap(headersValid -> {
                    if (!headersValid) {
                        return Mono.just(false);
                    }
                    return validateCookies(request);
                })
                .flatMap(cookiesValid -> {
                    if (!cookiesValid) {
                        return Mono.just(false);
                    }
                    return validateUserAgent(request.getHeaders().getFirst("User-Agent"));
                })
                .flatMap(userAgentValid -> {
                    if (!userAgentValid) {
                        return Mono.just(false);
                    }
                    return validateReferer(request.getHeaders().getFirst("Referer"));
                })
                .doOnNext(result -> {
                    if (result) {
                        LoggingUtil.debug(logger, "请求综合验证通过");
                    } else {
                        LoggingUtil.warn(logger, "请求综合验证失败");
                        LoggingUtil.warn(securityAuditLogger, "请求综合验证失败 - URI: {} | Method: {}",
                            request.getURI(), request.getMethod());
                    }
                });
    }

    /**
     * 计算HTTP头部总大小
     */
    private int calculateHeadersSize(HttpHeaders headers) {
        int totalSize = 0;
        for (Map.Entry<String, java.util.List<String>> entry : headers.entrySet()) {
            totalSize += entry.getKey().length();
            for (String value : entry.getValue()) {
                totalSize += value.length();
            }
        }
        return totalSize;
    }

    /**
     * 验证特定的HTTP头部
     */
    private boolean validateSpecificHeaders(HttpHeaders headers) {
        // 验证User-Agent
        String userAgent = headers.getFirst("User-Agent");
        if (userAgent != null && userAgent.length() > securityInputLimitProperties.getHttp().getMaxUserAgentLength()) {
            LoggingUtil.warn(logger, "User-Agent头部长度超限: {} > {}",
                userAgent.length(), securityInputLimitProperties.getHttp().getMaxUserAgentLength());
            return false;
        }

        // 验证Referer
        String referer = headers.getFirst("Referer");
        if (referer != null && referer.length() > securityInputLimitProperties.getHttp().getMaxRefererLength()) {
            LoggingUtil.warn(logger, "Referer头部长度超限: {} > {}",
                referer.length(), securityInputLimitProperties.getHttp().getMaxRefererLength());
            return false;
        }

        return true;
    }

    /**
     * 验证单个Cookie
     *
     * @param cookie Cookie字符串
     * @return 验证结果
     */
    public boolean validateCookie(String cookie) {
        if (cookie == null) {
            return true; // 允许空Cookie
        }

        if (cookie.length() > securityInputLimitProperties.getHttp().getMaxSingleCookieLength()) {
            LoggingUtil.warn(logger, "Cookie长度超限: {} > {}",
                cookie.length(), securityInputLimitProperties.getHttp().getMaxSingleCookieLength());
            LoggingUtil.warn(securityAuditLogger, "Cookie长度超限 - 实际长度: {} | 最大限制: {}",
                cookie.length(), securityInputLimitProperties.getHttp().getMaxSingleCookieLength());
            return false;
        }

        LoggingUtil.debug(logger, "Cookie验证通过，长度: {} 字符", cookie.length());
        return true;
    }

    /**
     * 验证HTTP头部键值对
     *
     * @param headerName 头部名称
     * @param headerValue 头部值
     * @return 验证结果
     */
    public boolean validateHttpHeader(String headerName, String headerValue) {
        if (headerName == null || headerValue == null) {
            return true; // 允许空值
        }

        if (headerValue.length() > securityInputLimitProperties.getHttp().getMaxHeaderLength()) {
            LoggingUtil.warn(logger, "HTTP头部 {} 值长度超限: {} > {}",
                headerName, headerValue.length(), securityInputLimitProperties.getHttp().getMaxHeaderLength());
            LoggingUtil.warn(securityAuditLogger, "HTTP头部值长度超限 - 头部名: {} | 实际长度: {} | 最大限制: {}",
                headerName, headerValue.length(), securityInputLimitProperties.getHttp().getMaxHeaderLength());
            return false;
        }

        LoggingUtil.debug(logger, "HTTP头部验证通过: {} = {}", headerName, headerValue);
        return true;
    }

    /**
     * 验证URL参数
     *
     * @param paramName 参数名
     * @param paramValue 参数值
     * @return 验证结果
     */
    public boolean validateUrlParameter(String paramName, String paramValue) {
        if (paramName == null || paramValue == null) {
            return true; // 允许空值
        }

        // 验证参数值长度
        if (paramValue.length() > securityInputLimitProperties.getRequest().getMaxParamLength()) {
            LoggingUtil.warn(logger, "URL参数 {} 值长度超限: {} > {}",
                paramName, paramValue.length(), securityInputLimitProperties.getRequest().getMaxParamLength());
            LoggingUtil.warn(securityAuditLogger, "URL参数值长度超限 - 参数名: {} | 实际长度: {} | 最大限制: {}",
                paramName, paramValue.length(), securityInputLimitProperties.getRequest().getMaxParamLength());
            return false;
        }

        LoggingUtil.debug(logger, "URL参数验证通过: {} = {}", paramName, paramValue);
        return true;
    }

    /**
     * 验证请求体大小
     *
     * @param bodySize 请求体大小（字节）
     * @return 验证结果
     */
    public boolean validateRequestBodySize(int bodySize) {
        if (bodySize > securityInputLimitProperties.getRequest().getMaxRequestSize()) {
            LoggingUtil.warn(logger, "请求体大小超限: {} > {}",
                bodySize, securityInputLimitProperties.getRequest().getMaxRequestSize());
            LoggingUtil.warn(securityAuditLogger, "请求体大小超限 - 实际大小: {} | 最大限制: {}",
                bodySize, securityInputLimitProperties.getRequest().getMaxRequestSize());
            return false;
        }

        LoggingUtil.debug(logger, "请求体大小验证通过: {} 字节", bodySize);
        return true;
    }

    /**
     * 获取验证统计信息
     *
     * @return 验证配置信息
     */
    public String getValidationInfo() {
        StringBuilder info = new StringBuilder();
        info.append("输入验证配置信息:\n");
        info.append("最大HTTP头部大小: ").append(securityInputLimitProperties.getHttp().getMaxHeadersSize()).append(" 字节\n");
        info.append("最大单个头部长度: ").append(securityInputLimitProperties.getHttp().getMaxHeaderLength()).append(" 字符\n");
        info.append("最大Cookie大小: ").append(securityInputLimitProperties.getHttp().getMaxCookieSize()).append(" 字节\n");
        info.append("最大文件名长度: ").append(securityInputLimitProperties.getFile().getMaxFilenameLength()).append(" 字符\n");
        info.append("最大User-Agent长度: ").append(securityInputLimitProperties.getHttp().getMaxUserAgentLength()).append(" 字符\n");
        info.append("最大Referer长度: ").append(securityInputLimitProperties.getHttp().getMaxRefererLength()).append(" 字符\n");
        return info.toString();
    }
}
