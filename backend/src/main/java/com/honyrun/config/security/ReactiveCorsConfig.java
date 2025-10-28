package com.honyrun.config.security;

import com.honyrun.config.UnifiedConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.honyrun.util.LoggingUtil;

/**
 * 响应式跨域配置类 - 安全增强版
 *
 * @author Mr.Rey
 * @version 2.1.0
 * @created 2025-07-01 10:30:00
 * @modified 2025-07-01 11:15:00
 *
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 配置WebFlux响应式跨域访问策略
 * 【安全增强】：
 * 1. 严格限制允许的源，禁止通配符*
 * 2. 限制HTTP方法，仅允许必要的方法
 * 3. 限制请求头，仅允许安全的头部
 * 4. 根据环境动态调整安全级别
 * 5. 添加详细的安全日志记录
 */
@Configuration
public class ReactiveCorsConfig {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveCorsConfig.class);

    private final Environment environment;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入Environment和UnifiedConfigManager
     *
     * @param environment Spring环境对象
     * @param unifiedConfigManager 统一配置管理器
     */
    public ReactiveCorsConfig(Environment environment, UnifiedConfigManager unifiedConfigManager) {
        this.environment = environment;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 配置CORS跨域访问 - 安全增强版
     *
     * 【安全策略】：
     * 1. 开发环境：允许本地域名，便于开发调试
     * 2. 测试环境：限制测试域名，确保测试安全
     * 3. 生产环境：严格限制生产域名，最高安全级别
     * 4. 禁止使用通配符*，防止CSRF攻击
     * 5. 限制HTTP方法和请求头，减少攻击面
     *
     * @return CORS配置源，包含安全的跨域访问规则
     */
    /**
     * 配置CORS配置源
     *
     * <p>
     * 创建和配置跨域资源共享(CORS)的配置源，用于处理跨域请求。
     * 该配置源提供了安全的跨域访问控制，包括：
     * <ul>
     * <li>允许的源域名配置 - 基于环境的动态配置</li>
     * <li>HTTP方法限制 - 仅允许必要的HTTP方法</li>
     * <li>请求头控制 - 严格限制允许的请求头</li>
     * <li>响应头暴露 - 控制客户端可访问的响应头</li>
     * <li>凭证支持 - 安全的Cookie和认证信息传递</li>
     * </ul>
     *
     * <p>
     * <strong>安全特性：</strong>
     * <ul>
     * <li>环境隔离：开发、测试、生产环境使用不同的CORS策略</li>
     * <li>方法限制：仅允许GET、POST、PUT、DELETE、OPTIONS方法</li>
     * <li>头部控制：严格限制允许的请求头，防止恶意请求</li>
     * <li>预检缓存：合理设置预检请求缓存时间，提升性能</li>
     * </ul>
     *
     * <p>
     * <strong>环境配置：</strong>
     * <ul>
     * <li>开发环境：允许localhost和开发域名</li>
     * <li>测试环境：允许测试域名和CI/CD环境</li>
     * <li>生产环境：仅允许生产域名，最严格的安全策略</li>
     * </ul>
     *
     * @return CorsConfigurationSource CORS配置源实例
     * @see CorsConfiguration CORS配置对象
     * @see UrlBasedCorsConfigurationSource 基于URL的CORS配置源
     * @author Mr.Rey Copyright © 2025
     * @since 1.0.0
     * @version 1.0.0
     */
    @Bean("reactiveCorsConfigurationSource")
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // 根据环境配置允许的源
        configureAllowedOrigins(configuration);

        // 配置允许的HTTP方法 - 严格限制
        configureAllowedMethods(configuration);

        // 配置允许的请求头 - 安全限制
        configureAllowedHeaders(configuration);

        // 配置暴露的响应头
        configureExposedHeaders(configuration);

        // 配置凭证和缓存
        // 注意：这里使用.block()是必要的，因为@Bean方法需要同步返回CorsConfigurationSource对象
        boolean allowCredentials = Boolean.parseBoolean(unifiedConfigManager.getProperty("cors.allow-credentials", "true"));
        long maxAge = Long.parseLong(unifiedConfigManager.getProperty("cors.max-age", "3600"));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        // 记录CORS配置信息
        logCorsConfiguration(configuration);

        // 注册到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 配置允许的源 - 根据环境动态调整
     *
     * @param configuration CORS配置对象
     */
    private void configureAllowedOrigins(CorsConfiguration configuration) {
        // 注意：这里使用.block()是必要的，因为配置方法需要同步设置CorsConfiguration对象
        String allowedOriginPatterns = unifiedConfigManager.getProperty("cors.allowed-origin-patterns", "");
        String allowedOrigins = unifiedConfigManager.getProperty("cors.allowed-origins", "");
        
        List<String> originPatterns = splitCsv(allowedOriginPatterns);
        List<String> origins = splitCsv(allowedOrigins);

        // 检查是否为生产环境
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");

        if (!originPatterns.isEmpty()) {
            // 生产环境禁止使用通配符
            if (isProduction && originPatterns.contains("*")) {
                LoggingUtil.warn(logger, "生产环境禁止使用CORS通配符，已移除不安全配置");
                originPatterns = originPatterns.stream()
                    .filter(pattern -> !"*".equals(pattern))
                    .collect(Collectors.toList());
            }
            configuration.setAllowedOriginPatterns(originPatterns);
            LoggingUtil.info(logger, "配置CORS允许的源模式: {}", originPatterns);
        } else if (!origins.isEmpty()) {
            // 生产环境禁止使用通配符
            if (isProduction && origins.contains("*")) {
                LoggingUtil.warn(logger, "生产环境禁止使用CORS通配符，已移除不安全配置");
                origins = origins.stream()
                    .filter(origin -> !"*".equals(origin))
                    .collect(Collectors.toList());
            }

            if (origins.isEmpty()) {
                // 如果生产环境没有配置有效源，使用默认安全配置
                origins = getDefaultSecureOrigins(isProduction);
            }

            configuration.setAllowedOrigins(origins);
            LoggingUtil.info(logger, "配置CORS允许的源: {}", origins);
        } else {
            // 没有配置源时，使用默认安全配置
            List<String> defaultOrigins = getDefaultSecureOrigins(isProduction);
            configuration.setAllowedOrigins(defaultOrigins);
            LoggingUtil.info(logger, "使用默认CORS源配置: {}", defaultOrigins);
        }
    }

    /**
     * 获取默认安全源配置
     *
     * @param isProduction 是否为生产环境
     * @return 默认源列表
     */
    private List<String> getDefaultSecureOrigins(boolean isProduction) {
        if (isProduction) {
            // 生产环境：从配置文件读取HTTPS域名
            String prodOrigins = unifiedConfigManager.getProperty("honyrun.security.cors.production.allowed-origins", "");
            if (!prodOrigins.isEmpty()) {
                return splitCsv(prodOrigins);
            }
            return Collections.emptyList();
        } else {
            // 开发/测试环境：从统一配置管理器读取本地域名
            String devOrigins = unifiedConfigManager.getProperty("honyrun.security.cors.development.allowed-origins", "");
            if (!devOrigins.isEmpty()) {
                return splitCsv(devOrigins);
            }
            return Collections.emptyList();
        }
    }

    /**
     * 配置允许的HTTP方法 - 严格限制
     *
     * @param configuration CORS配置对象
     */
    private void configureAllowedMethods(CorsConfiguration configuration) {
        // 注意：这里使用.block()是必要的，因为配置方法需要同步设置CorsConfiguration对象
        String allowedMethods = unifiedConfigManager.getProperty("cors.allowed-methods", "GET,POST,PUT,DELETE,OPTIONS");
        List<String> methods = splitCsv(allowedMethods);

        // 验证方法安全性
        List<String> safeMethods = methods.stream()
            .filter(this::isSafeHttpMethod)
            .collect(Collectors.toList());

      if (safeMethods.size() != methods.size()) {
            LoggingUtil.debug(logger, "安全机制正常工作 - 已过滤不安全的HTTP方法: {}",
                methods.stream().filter(m -> !safeMethods.contains(m)).collect(Collectors.toList()));
        }

        configuration.setAllowedMethods(safeMethods);
        LoggingUtil.info(logger, "配置CORS允许的方法: {}", safeMethods);
    }

    /**
     * 检查HTTP方法是否安全
     *
     * @param method HTTP方法
     * @return 是否安全
     */
    private boolean isSafeHttpMethod(String method) {
        List<String> safeMethods = Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        );
        return safeMethods.contains(method.toUpperCase());
    }

    /**
     * 配置允许的请求头 - 安全限制
     *
     * @param configuration CORS配置对象
     */
    private void configureAllowedHeaders(CorsConfiguration configuration) {
        // 注意：这里使用.block()是必要的，因为配置方法需要同步设置CorsConfiguration对象
        String allowedHeaders = unifiedConfigManager.getProperty("cors.allowed-headers", "Authorization,Content-Type,X-Requested-With,X-API-Version");
        List<String> headers = splitCsv(allowedHeaders);

        // 验证请求头安全性
        List<String> safeHeaders = headers.stream()
            .filter(this::isSafeHeader)
            .collect(Collectors.toList());

        if (safeHeaders.size() != headers.size()) {
            LoggingUtil.debug(logger, "安全机制正常工作 - 已过滤不安全的请求头: {}",
                headers.stream().filter(h -> !safeHeaders.contains(h)).collect(Collectors.toList()));
        }

        configuration.setAllowedHeaders(safeHeaders);
        LoggingUtil.info(logger, "配置CORS允许的请求头: {}", safeHeaders);
    }

    /**
     * 检查请求头是否安全
     *
     * <p>该方法实现了严格的请求头安全检查机制，用于防止CORS攻击。
     * 通配符(*)等不安全的请求头会被自动过滤，这是正常的安全行为。
     *
     * @param header 请求头名称
     * @return 是否安全
     */
    private boolean isSafeHeader(String header) {
        // 通配符检查 - 禁止通配符以防止安全漏洞
        if ("*".equals(header.trim())) {
            return false;
        }

        // 允许的安全请求头
        List<String> safeHeaders = Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "X-API-Version",
            "Accept", "Accept-Language", "Accept-Encoding", "Cache-Control",
            "X-CSRF-Token", "X-Request-ID", "X-Forwarded-For", "X-Real-IP", "X-Custom-Admin-Token"
        );

        // 禁止的危险请求头
        List<String> dangerousHeaders = Arrays.asList(
            "X-Original-URL", "X-Rewrite-URL", "Host", "Origin", "Referer"
        );

        String headerLower = header.toLowerCase();
        return safeHeaders.stream().anyMatch(safe -> safe.toLowerCase().equals(headerLower)) &&
               dangerousHeaders.stream().noneMatch(dangerous -> dangerous.toLowerCase().equals(headerLower));
    }

    /**
     * 配置暴露的响应头
     *
     * @param configuration CORS配置对象
     */
    private void configureExposedHeaders(CorsConfiguration configuration) {
        // 注意：这里使用.block()是必要的，因为配置方法需要同步设置CorsConfiguration对象
        String exposedHeaders = unifiedConfigManager.getProperty("cors.exposed-headers", "X-Total-Count,X-Total-Pages,X-Current-Page,X-Page-Size");
        List<String> headers = splitCsv(exposedHeaders);
        if (headers.isEmpty()) {
            // 使用默认安全的暴露头
            headers = Arrays.asList(
                "X-Total-Count", "X-Total-Pages", "X-Current-Page", "X-Page-Size",
                "X-Request-ID", "X-Response-Time"
            );
        }

        configuration.setExposedHeaders(headers);
        LoggingUtil.info(logger, "配置CORS暴露的响应头: {}", headers);
    }

    /**
     * 记录CORS配置信息
     *
     * @param configuration CORS配置对象
     */
    private void logCorsConfiguration(CorsConfiguration configuration) {
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        // 注意：这里使用.block()是必要的，因为日志记录方法需要同步获取配置值
        boolean allowCredentials = Boolean.parseBoolean(unifiedConfigManager.getProperty("cors.allow-credentials", "true"));
        long maxAge = Long.parseLong(unifiedConfigManager.getProperty("cors.max-age", "3600"));

        LoggingUtil.info(logger, "CORS配置完成 - 环境: {}, 允许凭证: {}, 缓存时间: {}秒",
            isProduction ? "生产" : "开发", allowCredentials, maxAge);

        if (isProduction) {
            LoggingUtil.info(logger, "生产环境CORS安全策略已启用");
        } else {
            LoggingUtil.info(logger, "开发环境CORS宽松策略已启用");
        }
    }

    private List<String> splitCsv(String csv) {
        if (csv == null) return Collections.emptyList();
        String trimmed = csv.trim();
        if (trimmed.isEmpty()) return Collections.emptyList();
        return Arrays.stream(trimmed.split("\s*,\s*"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}

