package com.honyrun.interceptor;

import com.honyrun.annotation.ApiVersion;
import com.honyrun.config.ApiVersionConfig;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API版本拦截器
 * 
 * 负责处理API版本控制，支持多种版本指定方式：
 * 1. URL路径中的版本号（如 /api/v1/users）
 * 2. HTTP头中的版本号（API-Version: v1）
 * 3. Accept头中的版本号（Accept: application/vnd.honyrun.v1+json）
 * 4. 查询参数中的版本号（?version=v1）
 * 
 * @author: Mr.Rey
 * Copyright © 2025
 * @created 2025-09-28 17:15:00
 * @modified 2025-09-28 17:15:00
 * @version 2.0.0
 */
@Component("apiVersionInterceptor")
public class ApiVersionInterceptor implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiVersionInterceptor.class);

    /**
     * 版本号提取的正则表达式
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("/api/v(\\d+)/.*");

    /**
     * 支持的版本号列表
     */
    private static final String[] SUPPORTED_VERSIONS = {"v1", "v2"};

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String requestPath = exchange.getRequest().getPath().value();
        
        // 只处理API请求
        if (!requestPath.startsWith(ApiVersionConfig.API_VERSION_PREFIX)) {
            return chain.filter(exchange);
        }

        // 对于认证相关路径，采用宽松的版本处理策略，避免影响认证功能
        if (isAuthPath(requestPath)) {
            String requestedVersion = ApiVersionConfig.DEFAULT_VERSION;
            try {
                // 尝试提取版本号，但不进行严格验证
                String extractedVersion = extractApiVersion(exchange);
                if (extractedVersion != null && isVersionInSupportedList(extractedVersion)) {
                    requestedVersion = extractedVersion;
                }
            } catch (Exception e) {
                // 认证路径的版本处理异常不应阻断请求，直接使用默认版本
                LoggingUtil.warn(logger, "认证路径版本处理异常，使用默认版本", e, "path", requestPath);
            }
            
            exchange.getAttributes().put("API_VERSION", requestedVersion);
            // 安全地在响应头中添加版本信息
            addVersionHeaderSafely(exchange, requestedVersion);
            LoggingUtil.debug(logger, "认证路径版本处理", "version", requestedVersion, "path", requestPath);
            return chain.filter(exchange);
        }

        try {
            // 解析请求的API版本
            String requestedVersion = extractApiVersion(exchange);
            
            // 验证版本有效性
            validateApiVersion(requestedVersion);
            
            // 设置版本信息到请求属性中
            exchange.getAttributes().put("API_VERSION", requestedVersion);
            
            // 安全地在响应头中添加版本信息
            addVersionHeaderSafely(exchange, requestedVersion);
            
            LoggingUtil.info(logger, "API版本验证成功", "version", requestedVersion, "path", requestPath);
            
            return chain.filter(exchange);
            
        } catch (ResponseStatusException e) {
            LoggingUtil.error(logger, "API版本验证失败", e, "path", requestPath);
            return Mono.error(e);
        } catch (Exception e) {
            LoggingUtil.error(logger, "API版本处理异常", e, "path", requestPath);
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "版本处理异常"));
        }
    }

    /**
     * 判断是否为认证相关路径
     *
     * @param path 请求路径
     * @return 是否为认证路径
     */
    private boolean isAuthPath(String path) {
        return path.contains("/auth/") || 
               path.endsWith("/login") || 
               path.endsWith("/logout") || 
               path.endsWith("/refresh") || 
               path.contains("/signin") || 
               path.contains("/signout");
    }

    /**
     * 提取API版本号
     *
     * @param exchange 服务器交换对象
     * @return API版本号
     */
    private String extractApiVersion(ServerWebExchange exchange) {
        String version = null;
        
        // 1. 从URL路径提取版本号
        version = extractVersionFromPath(exchange.getRequest().getPath().value());
        if (version != null) {
            LoggingUtil.debug(logger, "从URL路径提取版本号: {}", version);
            return version;
        }
        
        // 2. 从请求头提取版本号
        version = extractVersionFromHeaders(exchange);
        if (version != null) {
            LoggingUtil.debug(logger, "从请求头提取版本号: {}", version);
            return version;
        }
        
        // 3. 从查询参数提取版本号
        version = extractVersionFromQueryParams(exchange);
        if (version != null) {
            LoggingUtil.debug(logger, "从查询参数提取版本号: {}", version);
            return version;
        }
        
        // 4. 使用默认版本
        version = ApiVersionConfig.DEFAULT_VERSION;
        LoggingUtil.debug(logger, "使用默认版本号: {}", version);
        return version;
    }

    /**
     * 从URL路径提取版本号
     *
     * @param path 请求路径
     * @return 版本号或null
     */
    private String extractVersionFromPath(String path) {
        Matcher matcher = VERSION_PATTERN.matcher(path);
        if (matcher.matches()) {
            return "v" + matcher.group(1);
        }
        return null;
    }

    /**
     * 从请求头提取版本号
     *
     * @param exchange 服务器交换对象
     * @return 版本号或null
     */
    private String extractVersionFromHeaders(ServerWebExchange exchange) {
        // 检查API-Version头
        String versionHeader = exchange.getRequest().getHeaders().getFirst(ApiVersionConfig.VERSION_HEADER);
        if (StringUtils.hasText(versionHeader)) {
            return normalizeVersion(versionHeader);
        }
        
        // 检查Accept头中的版本信息
        String acceptHeader = exchange.getRequest().getHeaders().getFirst("Accept");
        if (StringUtils.hasText(acceptHeader) && acceptHeader != null) {
            if (acceptHeader.contains("application/vnd.honyrun.v1+json")) {
                return "v1";
            } else if (acceptHeader.contains("application/vnd.honyrun.v2+json")) {
                return "v2";
            }
        }
        
        return null;
    }

    /**
     * 从查询参数提取版本号
     *
     * @param exchange 服务器交换对象
     * @return 版本号或null
     */
    private String extractVersionFromQueryParams(ServerWebExchange exchange) {
        String versionParam = exchange.getRequest().getQueryParams().getFirst(ApiVersionConfig.VERSION_PARAM);
        if (StringUtils.hasText(versionParam)) {
            return normalizeVersion(versionParam);
        }
        return null;
    }

    /**
     * 标准化版本号格式
     *
     * @param version 原始版本号
     * @return 标准化后的版本号
     */
    private String normalizeVersion(String version) {
        if (version == null) {
            return null;
        }
        
        version = version.trim().toLowerCase();
        
        // 如果版本号不以'v'开头，则添加'v'前缀
        if (!version.startsWith("v")) {
            version = "v" + version;
        }
        
        return version;
    }

    /**
     * 检查版本是否在支持列表中
     *
     * @param version 版本号
     * @return 是否支持
     */
    private boolean isVersionInSupportedList(String version) {
        if (version == null) {
            return false;
        }
        
        for (String supportedVersion : SUPPORTED_VERSIONS) {
            if (supportedVersion.equals(version)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证API版本号
     *
     * @param version 版本号
     * @throws IllegalArgumentException 版本号无效时抛出
     */
    private void validateApiVersion(String version) {
        if (version == null) {
            throw new IllegalArgumentException("API版本号不能为空");
        }
        
        // 检查是否为支持的版本
        for (String supportedVersion : SUPPORTED_VERSIONS) {
            if (supportedVersion.equals(version)) {
                return;
            }
        }
        
        throw new IllegalArgumentException("不支持的API版本: " + version);
    }

    /**
     * 检查方法是否支持指定版本
     *
     * @param handlerMethod 处理方法
     * @param requestedVersion 请求的版本
     * @return 是否支持
     */
    public boolean isVersionSupported(HandlerMethod handlerMethod, String requestedVersion) {
        if (handlerMethod == null || requestedVersion == null) {
            return false;
        }
        
        // 检查方法级别的版本注解
        ApiVersion methodVersion = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), ApiVersion.class);
        if (methodVersion != null) {
            return isVersionInRange(requestedVersion, methodVersion);
        }
        
        // 检查类级别的版本注解
        ApiVersion classVersion = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), ApiVersion.class);
        if (classVersion != null) {
            return isVersionInRange(requestedVersion, classVersion);
        }
        
        // 如果没有版本注解，默认支持所有版本
        return true;
    }

    /**
     * 检查版本是否在支持范围内
     *
     * @param requestedVersion 请求的版本
     * @param apiVersion 版本注解
     * @return 是否在支持范围内
     */
    private boolean isVersionInRange(String requestedVersion, ApiVersion apiVersion) {
        String targetVersion = apiVersion.value();
        String minVersion = apiVersion.minVersion();
        String maxVersion = apiVersion.maxVersion();
        
        // 精确匹配
        if (targetVersion.equals(requestedVersion)) {
            return true;
        }
        
        // 范围匹配
        if (StringUtils.hasText(minVersion) || StringUtils.hasText(maxVersion)) {
            return isVersionInRange(requestedVersion, minVersion, maxVersion);
        }
        
        return false;
    }

    /**
     * 检查版本是否在指定范围内
     *
     * @param version 版本号
     * @param minVersion 最小版本
     * @param maxVersion 最大版本
     * @return 是否在范围内
     */
    private boolean isVersionInRange(String version, String minVersion, String maxVersion) {
        int versionNum = extractVersionNumber(version);
        
        if (StringUtils.hasText(minVersion)) {
            int minVersionNum = extractVersionNumber(minVersion);
            if (versionNum < minVersionNum) {
                return false;
            }
        }
        
        if (StringUtils.hasText(maxVersion)) {
            int maxVersionNum = extractVersionNumber(maxVersion);
            if (versionNum > maxVersionNum) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 安全地添加版本头信息，避免在测试环境中因响应头只读而抛出异常
     * 
     * @param exchange ServerWebExchange
     * @param version API版本
     */
    private void addVersionHeaderSafely(ServerWebExchange exchange, String version) {
        try {
            exchange.getResponse().getHeaders().add(ApiVersionConfig.VERSION_HEADER, version);
        } catch (UnsupportedOperationException e) {
            // 在测试环境中，响应头可能是只读的，忽略此异常
            LoggingUtil.debug(logger, "无法添加响应头版本信息（可能在测试环境中）", "version", version);
        }
    }

    /**
     * 提取版本号数字部分
     * 
     * @param version 版本字符串
     * @return 版本号数字
     */
    private int extractVersionNumber(String version) {
        try {
            return Integer.parseInt(version.replace("v", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
