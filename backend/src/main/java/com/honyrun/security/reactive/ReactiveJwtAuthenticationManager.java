package com.honyrun.security.reactive;

import com.honyrun.security.jwt.ReactiveJwtTokenProvider;
import com.honyrun.service.reactive.ReactiveTokenBlacklistService;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 响应式JWT认证管理器
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  17:00:00
 * @modified 2025-07-01 17:00:00
 *
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 实现ReactiveAuthenticationManager接口，提供JWT令牌验证功能
 * 支持响应式认证流程，包括令牌解析、用户信息加载、权限验证等
 */
@Component
public class ReactiveJwtAuthenticationManager implements ReactiveAuthenticationManager {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveJwtAuthenticationManager.class);

    private final ReactiveJwtTokenProvider jwtTokenProvider;
    private final ReactiveUserDetailsServiceImpl userDetailsService;
    private final ReactiveTokenBlacklistService tokenBlacklistService;

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param jwtTokenProvider JWT令牌提供者
     * @param userDetailsService 用户详情服务
     * @param tokenBlacklistService 令牌黑名单服务
     */
    public ReactiveJwtAuthenticationManager(@Qualifier("reactiveJwtTokenProvider") ReactiveJwtTokenProvider jwtTokenProvider,
                                          ReactiveUserDetailsServiceImpl userDetailsService,
                                          ReactiveTokenBlacklistService tokenBlacklistService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * 认证方法
     *
     * @param authentication 认证对象，包含JWT令牌
     * @return Mono<Authentication> 认证结果
     */
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) throws AuthenticationException {
        LoggingUtil.debug(logger, "Starting JWT authentication for: {}", authentication.getName());

        String token = authentication.getCredentials().toString();

        return jwtTokenProvider.validateToken(token)
                .flatMap(isValid -> {
                    if (!isValid) {
                        LoggingUtil.warn(logger, "JWT token validation failed");
                        return Mono.error(new BadCredentialsException("Invalid JWT token"));
                    }

                    // 检查token是否在黑名单中
                    return tokenBlacklistService.isTokenBlacklisted(token)
                            .flatMap(isBlacklisted -> {
                                if (isBlacklisted) {
                                    LoggingUtil.warn(logger, "JWT token is blacklisted");
                                    return Mono.error(new BadCredentialsException("JWT token is blacklisted"));
                                }

                                return jwtTokenProvider.getUsernameFromToken(token)
                                        .flatMap(username -> {
                                            LoggingUtil.debug(logger, "JWT token is valid for user: {}", username);

                                            // 安全最佳实践：从可信数据源（数据库）获取最新权限信息
                                            // JWT token只用于身份验证，权限信息必须从服务端实时获取
                                            // 这样确保：1) 权限变更实时生效 2) 防止权限篡改 3) 符合安全合规要求
                                            return userDetailsService.findByUsername(username)
                                                    .map(userDetails -> {
                                                        LoggingUtil.info(logger, "User authenticated successfully: {} with authorities from database: {}",
                                                                username, userDetails.getAuthorities());

                                                        // 记录JWT token中的权限信息用于审计对比
                                                        jwtTokenProvider.getAuthoritiesFromToken(token)
                                                                .subscribe(tokenAuthorities -> {
                                                                    LoggingUtil.debug(logger, "JWT token authorities for audit: {} vs database authorities: {}",
                                                                            tokenAuthorities, userDetails.getAuthorities());
                                                                });

                                                        return (Authentication) new UsernamePasswordAuthenticationToken(
                                                                userDetails,
                                                                null,
                                                                userDetails.getAuthorities() // 使用数据库中的最新权限
                                                        );
                                                    })
                                                    .switchIfEmpty(Mono.error(new BadCredentialsException("User not found: " + username)));
                                        })
                                        .onErrorMap(throwable -> {
                                            if (throwable instanceof AuthenticationException) {
                                                return throwable;
                                            }
                                            LoggingUtil.error(logger, "Error during token processing", throwable);
                                            return new BadCredentialsException("Token processing failed");
                                        });
                            });
                })
                .doOnError(error -> LoggingUtil.error(logger, "JWT authentication failed", error));
    }
}
