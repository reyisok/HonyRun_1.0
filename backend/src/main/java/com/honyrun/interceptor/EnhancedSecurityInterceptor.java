package com.honyrun.interceptor;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.honyrun.config.properties.SecurityInputLimitProperties;
import com.honyrun.service.security.EnhancedInputValidationService;
import com.honyrun.service.security.OptimizedSecurityDetectionService;
import com.honyrun.service.security.UnifiedSecurityDetectionService;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;

/**
 * 增强的安全拦截器
 *
 * 该拦截器集成了所有安全检测功能，提供全面的请求安全验证：
 * 1. 输入长度验证（HTTP头部、Cookie、文件名等）
 * 2. 恶意模式检测（SQL注入、XSS、路径遍历等）
 * 3. 性能优化的安全检测
 * 4. 异步安全检测支持
 * 5. 详细的安全审计日志
 *
 * 安全检测流程：
 * - 请求基本验证（URL、方法、头部大小）
 * - 输入长度限制检查
 * - 恶意模式检测（优化版本优先）
 * - 安全审计日志记录
 * - 异常情况降级处理
 *
 * @author Mr.Rey
 * @created 2025-07-01 16:30:00
 * @modified 2025-07-01 16:30:00
 * @version 1.0.0
 *          Copyright © 2025 HonyRun. All rights reserved.
 */
@Component("enhancedSecurityInterceptor")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Profile("!test")  // 在非测试环境激活，测试环境使用专用的安全配置以避免干扰测试执行
public class EnhancedSecurityInterceptor implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedSecurityInterceptor.class);

    // 安全审计专用日志记录器
    private static final Logger securityAuditLogger = LoggerFactory.getLogger("SECURITY_AUDIT");

    private final OptimizedSecurityDetectionService optimizedSecurityDetectionService;
    private final UnifiedSecurityDetectionService unifiedSecurityDetectionService;
    private final EnhancedInputValidationService inputValidationService;
    private final SecurityInputLimitProperties securityInputLimitProperties;

    /**
     * 构造器注入依赖
     *
     * @param optimizedSecurityDetectionService 优化安全检测服务
     * @param unifiedSecurityDetectionService 统一安全检测服务
     * @param inputValidationService 增强输入验证服务
     * @param securityInputLimitProperties 安全输入限制属性
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-22 04:40:00
     * @version 1.0.0
     */
    public EnhancedSecurityInterceptor(
            OptimizedSecurityDetectionService optimizedSecurityDetectionService,
            UnifiedSecurityDetectionService unifiedSecurityDetectionService,
            @Qualifier("enhancedInputValidationService") EnhancedInputValidationService inputValidationService,
            SecurityInputLimitProperties securityInputLimitProperties) {
        this.optimizedSecurityDetectionService = optimizedSecurityDetectionService;
        this.unifiedSecurityDetectionService = unifiedSecurityDetectionService;
        this.inputValidationService = inputValidationService;
        this.securityInputLimitProperties = securityInputLimitProperties;
    }

    // 白名单路径，跳过安全检测
    private static final List<String> WHITELIST_PATHS = List.of(
            "/actuator/health",
            "/favicon.ico",
            "/static/",
            "/css/",
            "/js/",
            "/images/",
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/token/refresh",
            "/api/auth/login",
            "/api/auth/logout",
            "/api/auth/token/refresh",
            "/api/v1/system/health",
            "/api/v1/system/info");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 记录请求开始
        LoggingUtil.debug(logger, "开始增强安全检测，请求路径: {}", request.getPath().value());

        // 检查白名单
        if (isWhitelistedPath(request.getPath().value())) {
            LoggingUtil.debug(logger, "路径在白名单中，跳过安全检测: {}", request.getPath().value());
            return chain.filter(exchange);
        }

        // 执行安全检测
        return performSecurityValidation(request)
                .flatMap(isValid -> {
                    if (!isValid) {
                        // 安全检测失败，返回403
                        LoggingUtil.warn(logger, "安全检测失败，拒绝请求: {}", request.getPath().value());
                        LoggingUtil.warn(securityAuditLogger, "请求被拒绝 - 路径: {} | 方法: {} | 客户端IP: {} | User-Agent: {}",
                                request.getPath().value(),
                                request.getMethod(),
                                getClientIp(request),
                                request.getHeaders().getFirst(HttpHeaders.USER_AGENT));

                        response.setStatusCode(HttpStatus.FORBIDDEN);
                        return response.setComplete();
                    }

                    // 安全检测通过，继续处理
                    LoggingUtil.debug(logger, "安全检测通过，继续处理请求: {}", request.getPath().value());
                    return chain.filter(exchange);
                })
                .onErrorResume(throwable -> {
                    // 安全检测异常，记录日志并使用降级策略
                    LoggingUtil.error(logger, "安全检测过程中发生异常", throwable);
                    LoggingUtil.error(securityAuditLogger, "安全检测异常 - 路径: {} | 异常: {}",
                            request.getPath().value(), throwable.getMessage());

                    // 降级策略：执行基本安全检测
                    return performFallbackValidation(request)
                            .flatMap(isValid -> {
                                if (!isValid) {
                                    response.setStatusCode(HttpStatus.FORBIDDEN);
                                    return response.setComplete();
                                }
                                return chain.filter(exchange);
                            });
                });
    }

    /**
     * 执行完整的安全验证
     */
    private Mono<Boolean> performSecurityValidation(ServerHttpRequest request) {
        return Mono.fromCallable(() -> {
            LoggingUtil.debug(logger, "开始执行完整安全验证");

            // 1. 基本请求验证
            if (!validateBasicRequest(request)) {
                return false;
            }

            // 2. 输入长度验证
            if (!validateInputLengths(request)) {
                return false;
            }

            // 3. HTTP头部验证 - 使用同步版本
            if (!validateHttpHeaders(request)) {
                return false;
            }

            // 4. URL参数验证
            if (!validateUrlParameters(request)) {
                return false;
            }

            LoggingUtil.debug(logger, "基础验证通过，开始恶意模式检测");
            return true;
        })
                .flatMap(basicValid -> {
                    if (!basicValid) {
                        return Mono.just(false);
                    }

                    // 5. 异步恶意模式检测
                    return performMaliciousPatternDetection(request);
                });
    }

    /**
     * 基本请求验证
     */
    private boolean validateBasicRequest(ServerHttpRequest request) {
        URI uri = request.getURI();

        // 检查URL长度
        if (uri.toString().length() > securityInputLimitProperties.getRequest().getMaxPathLength()) {
            LoggingUtil.warn(logger, "URL长度超过限制: {} > {}",
                    uri.toString().length(), securityInputLimitProperties.getRequest().getMaxPathLength());
            return false;
        }

        // 检查查询参数数量
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        if (queryParams.size() > securityInputLimitProperties.getRequest().getMaxQueryParams()) {
            LoggingUtil.warn(logger, "查询参数数量超过限制: {} > {}",
                    queryParams.size(), securityInputLimitProperties.getRequest().getMaxQueryParams());
            return false;
        }

        return true;
    }

    /**
     * 输入长度验证
     */
    /**
     * 输入长度验证
     */
    private boolean validateInputLengths(ServerHttpRequest request) {
        try {
            // 验证URL长度
            URI uri = request.getURI();
            if (uri.toString().length() > securityInputLimitProperties.getRequest().getMaxPathLength()) {
                LoggingUtil.warn(logger, "URL长度超限: {}", uri.toString().length());
                return false;
            }

            // 验证查询参数长度
            MultiValueMap<String, String> queryParams = request.getQueryParams();
            for (String key : queryParams.keySet()) {
                if (key.length() > securityInputLimitProperties.getRequest().getMaxParamLength()) {
                    LoggingUtil.warn(logger, "查询参数名长度超限: {}", key.length());
                    return false;
                }

                List<String> values = queryParams.get(key);
                if (values != null) {
                    for (String value : values) {
                        if (value != null
                                && value.length() > securityInputLimitProperties.getRequest().getMaxParamLength()) {
                            LoggingUtil.warn(logger, "查询参数值长度超限: {}", value.length());
                            return false;
                        }
                    }
                }
            }

            return true;
        } catch (Exception e) {
            LoggingUtil.error(logger, "输入长度验证异常", e);
            return false;
        }
    }



    /**
     * HTTP头部验证（同步版本）
     */
    private boolean validateHttpHeaders(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();

        // 计算所有头部的总大小
        int totalHeaderSize = 0;
        for (String headerName : headers.keySet()) {
            List<String> headerValues = headers.get(headerName);
            if (headerValues != null) {
                for (String headerValue : headerValues) {
                    totalHeaderSize += headerName.length() + (headerValue != null ? headerValue.length() : 0) + 4; // 加上分隔符
                }
            }
        }

        if (totalHeaderSize > securityInputLimitProperties.getHttp().getMaxHeadersSize()) {
            LoggingUtil.warn(logger, "HTTP头部总大小超过限制: {} > {}",
                    totalHeaderSize, securityInputLimitProperties.getHttp().getMaxHeadersSize());
            return false;
        }

        // 验证单个头部长度
        for (String headerName : headers.keySet()) {
            List<String> headerValues = headers.get(headerName);
            if (headerValues != null) {
                for (String headerValue : headerValues) {
                    if (!inputValidationService.validateHttpHeader(headerName, headerValue)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * URL参数验证
     */
    private boolean validateUrlParameters(ServerHttpRequest request) {
        MultiValueMap<String, String> queryParams = request.getQueryParams();

        for (String paramName : queryParams.keySet()) {
            List<String> paramValues = queryParams.get(paramName);
            if (paramValues != null) {
                for (String paramValue : paramValues) {
                    if (!inputValidationService.validateUrlParameter(paramName, paramValue)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * 恶意模式检测
     */
    private Mono<Boolean> performMaliciousPatternDetection(ServerHttpRequest request) {
        // 收集所有需要检测的输入
        StringBuilder allInputs = new StringBuilder();

        // 添加URL路径
        allInputs.append(request.getPath().value()).append(" ");

        // 添加查询参数
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        for (String paramName : queryParams.keySet()) {
            List<String> paramValues = queryParams.get(paramName);
            if (paramValues != null) {
                for (String paramValue : paramValues) {
                    allInputs.append(paramName).append("=").append(paramValue).append(" ");
                }
            }
        }

        // 添加关键HTTP头部
        HttpHeaders headers = request.getHeaders();
        String userAgent = headers.getFirst(HttpHeaders.USER_AGENT);
        if (userAgent != null) {
            allInputs.append(userAgent).append(" ");
        }

        String referer = headers.getFirst(HttpHeaders.REFERER);
        if (referer != null) {
            allInputs.append(referer).append(" ");
        }

        String inputToCheck = allInputs.toString();

        // 优先使用优化的检测服务
        return optimizedSecurityDetectionService.containsMaliciousPatternsReactiveOptimized(inputToCheck)
                .onErrorResume(throwable -> {
                    LoggingUtil.warn(logger, "优化检测服务异常，使用标准检测服务，异常信息: {}", throwable.getMessage());
                    securityAuditLogger.warn("安全检测服务降级 - 输入长度: {} | 异常: {} | 降级到标准检测服务",
                            inputToCheck.length(), throwable.getMessage());
                    // 降级到标准检测服务
                    return unifiedSecurityDetectionService.containsMaliciousPatternsReactive(inputToCheck);
                })
                .doOnNext(hasMalicious -> {
                    if (hasMalicious) {
                        LoggingUtil.warn(logger, "恶意模式检测发现威胁，输入长度: {} 字符，输入内容: {}",
                                inputToCheck.length(), inputToCheck);
                        securityAuditLogger.warn("恶意模式检测发现威胁 - 路径: {} | 输入长度: {} | 完整输入: {} | 检测结果: 发现恶意模式",
                                request.getPath().value(), inputToCheck.length(), inputToCheck);
                    } else {
                        LoggingUtil.debug(logger, "恶意模式检测通过，输入长度: {} 字符", inputToCheck.length());
                        securityAuditLogger.debug("恶意模式检测通过 - 路径: {} | 输入长度: {} | 检测结果: 未发现恶意模式",
                                request.getPath().value(), inputToCheck.length());
                    }
                })
                .map(hasMalicious -> !hasMalicious); // 返回是否安全（取反）
    }

    /**
     * 降级验证策略
     */
    private Mono<Boolean> performFallbackValidation(ServerHttpRequest request) {
        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "执行降级安全验证");

            // 简单的关键字检测
            String path = request.getPath().value().toLowerCase();
            String[] dangerousKeywords = {
                    "script", "javascript", "select", "union", "drop", "delete",
                    "insert", "update", "exec", "eval", "alert", "../", "cmd"
            };

            for (String keyword : dangerousKeywords) {
                if (path.contains(keyword)) {
                    LoggingUtil.warn(logger, "降级检测发现危险关键字: {}", keyword);
                    return false;
                }
            }

            // 检查查询参数
            MultiValueMap<String, String> queryParams = request.getQueryParams();
            for (String paramName : queryParams.keySet()) {
                List<String> paramValues = queryParams.get(paramName);
                if (paramValues != null) {
                    for (String paramValue : paramValues) {
                        if (paramValue != null) {
                            String lowerValue = paramValue.toLowerCase();
                            for (String keyword : dangerousKeywords) {
                                if (lowerValue.contains(keyword)) {
                                    LoggingUtil.warn(logger, "降级检测在参数中发现危险关键字: {}", keyword);
                                    return false;
                                }
                            }
                        }
                    }
                }
            }

            return true;
        });
    }

    /**
     * 检查是否为白名单路径
     */
    private boolean isWhitelistedPath(String path) {
        return WHITELIST_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    /**
     * 获取拦截器统计信息
     */
    public String getInterceptorStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("增强安全拦截器状态: 活跃\n");
        stats.append("白名单路径数量: ").append(WHITELIST_PATHS.size()).append("\n");
        stats.append("优化检测服务状态: ").append(optimizedSecurityDetectionService != null ? "可用" : "不可用").append("\n");
        stats.append("标准检测服务状态: ").append(unifiedSecurityDetectionService != null ? "可用" : "不可用").append("\n");
        stats.append("输入验证服务状态: ").append(inputValidationService != null ? "可用" : "不可用").append("\n");

        if (optimizedSecurityDetectionService != null) {
            stats.append("缓存统计: ").append(optimizedSecurityDetectionService.getCacheStats()).append("\n");
        }

        return stats.toString();
    }
}

