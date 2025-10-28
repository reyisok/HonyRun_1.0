package com.honyrun.security.reactive;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 响应式安全上下文仓库
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  17:30:00
 * @modified 2025-07-01 17:30:00
 *
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 实现ServerSecurityContextRepository接口，提供响应式安全上下文管理功能
 * 支持JWT令牌解析、安全上下文创建、无状态会话管理等功能
 */
@Component
public class ReactiveSecurityContextRepository implements ServerSecurityContextRepository {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveSecurityContextRepository.class);

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final ReactiveJwtAuthenticationManager authenticationManager;

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param authenticationManager JWT认证管理器
     */
    public ReactiveSecurityContextRepository(ReactiveJwtAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * 保存安全上下文（无状态架构，不需要保存）
     *
     * @param exchange 服务器Web交换对象
     * @param context 安全上下文
     * @return Mono<Void> 空的Mono
     */
    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        // JWT无状态架构，不需要保存上下文
        return Mono.empty();
    }

    /**
     * 加载安全上下文
     *
     * @param exchange 服务器Web交换对象
     * @return Mono<SecurityContext> 安全上下文
     */
    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String requestPath = exchange.getRequest().getPath().value();
        LoggingUtil.debug(logger, "Loading security context for request: {}", requestPath);

        // 检查是否为不需要认证的路径
        if (isPublicPath(requestPath)) {
            LoggingUtil.debug(logger, "Public path detected, skipping authentication: {}", requestPath);
            return Mono.empty();
        }

        return extractAuthToken(exchange.getRequest())
                .flatMap(token -> {
                    LoggingUtil.debug(logger, "Found JWT token, creating authentication...");

                    // 直接创建认证对象，不进行重复验证
                    // JWT验证将由ReactiveJwtAuthenticationManager处理
                    Authentication auth = new UsernamePasswordAuthenticationToken(token, token);

                    return authenticationManager.authenticate(auth)
                            .map(authentication -> {
                                LoggingUtil.info(logger, "Authentication successful for user: {}",
                                        authentication.getName());

                                SecurityContext context = new SecurityContextImpl();
                                context.setAuthentication(authentication);
                                return context;
                            })
                            .doOnError(error -> LoggingUtil.warn(logger, "Authentication failed: {}", error.getMessage()))
                            .onErrorResume(error -> {
                                // 确保认证错误能够传播到authenticationEntryPoint
                                LoggingUtil.debug(logger, "Propagating authentication error to security filter chain");
                                return Mono.error(error);
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    LoggingUtil.debug(logger, "No JWT token found, returning empty context");
                    return Mono.empty();
                }));
    }

    /**
     * 检查是否为公共路径（不需要认证）
     *
     * @param path 请求路径
     * @return boolean 是否为公共路径
     */
    private boolean isPublicPath(String path) {
        // 定义不需要认证的路径模式
        String[] publicPaths = {
                "/api/v1/auth/login",
                "/api/v1/auth/validate",
                "/actuator/health",
                "/favicon.ico",
                "/static/",
                "/public/",
                "/webjars/",
                "/swagger-ui/",
                "/v3/api-docs"
        };

        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 从请求中提取认证令牌
     *
     * @param request HTTP请求对象
     * @return Mono<String> 令牌字符串
     */
    private Mono<String> extractAuthToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            String token = authHeader.substring(TOKEN_PREFIX.length());
            LoggingUtil.debug(logger, "Extracted JWT token from Authorization header");
            return Mono.just(token);
        }

        LoggingUtil.debug(logger, "No JWT token found in Authorization header");
        return Mono.empty();
    }
}
