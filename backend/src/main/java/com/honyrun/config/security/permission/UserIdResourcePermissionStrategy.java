package com.honyrun.config.security.permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;

import com.honyrun.constant.PathConstants;
import com.honyrun.security.jwt.ReactiveJwtTokenProvider;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;

/**
 * 用户ID资源权限策略
 * 
 * 处理用户ID相关路径的权限检查，检查资源所有权
 * 普通用户只能访问自己的资源
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-17 16:40:00
 * @version 1.0.0 - 策略模式重构版本
 */
@Component
public class UserIdResourcePermissionStrategy implements PathPermissionStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(UserIdResourcePermissionStrategy.class);
    
    private final ReactiveJwtTokenProvider jwtTokenProvider;
    
    /**
     * 构造函数注入依赖
     * 
     * @param jwtTokenProvider JWT令牌提供器
     */
    public UserIdResourcePermissionStrategy(ReactiveJwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public boolean supports(String path) {
        return path.matches(PathConstants.USER_BASE + "/\\d+.*");
    }
    
    @Override
    public Mono<AuthorizationDecision> checkPermission(String path, AuthorizationContext context) {
        // 用户ID相关路径 - 检查资源所有权
        return authorizeSelfAccessByPath(context, 4);
    }
    
    @Override
    public int getPriority() {
        return 600; // 较低优先级，因为需要正则匹配
    }
    
    /**
     * 统一的"本人资源访问"复用方法
     * 根据路径段中的用户ID与JWT中的用户ID进行比对，仅本人可访问
     *
     * @param context            授权上下文
     * @param userIdSegmentIndex 路径数组中用户ID所在的下标（例如 /api/v1/users/{id} 为 4）
     * @return 授权决策（本人访问为true，否则false）
     */
    private Mono<AuthorizationDecision> authorizeSelfAccessByPath(AuthorizationContext context,
            int userIdSegmentIndex) {
        String path = context.getExchange().getRequest().getPath().value();
        String[] pathSegments = path.split("/");
        if (pathSegments.length <= userIdSegmentIndex) {
            LoggingUtil.warn(logger, "Invalid path format for user resource: {}", path);
            return Mono.just(new AuthorizationDecision(false));
        }

        try {
            Long resourceUserId = Long.parseLong(pathSegments[userIdSegmentIndex]);
            LoggingUtil.info(logger, "Checking access to resource user ID: {}", resourceUserId);

            String authHeader = context.getExchange().getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                LoggingUtil.info(logger, "Extracted token from Authorization header");

                return jwtTokenProvider.getUserIdFromToken(token)
                        .map(currentUserId -> {
                            LoggingUtil.info(logger, "Current user ID from token: {}, Resource user ID: {}",
                                    currentUserId, resourceUserId);
                            boolean hasAccess = resourceUserId.equals(currentUserId);
                            LoggingUtil.info(logger, "Access decision: {}", hasAccess);
                            return new AuthorizationDecision(hasAccess);
                        })
                        .onErrorResume(error -> {
                            LoggingUtil.error(logger, "Error extracting user ID from token: {}", error,
                                    error.getMessage());
                            return Mono.just(new AuthorizationDecision(false));
                        });
            }

            LoggingUtil.warn(logger, "No Authorization header found or invalid format");
            return Mono.just(new AuthorizationDecision(false));
        } catch (NumberFormatException e) {
            LoggingUtil.error(logger, "Invalid user ID format in path: {}", e, pathSegments[userIdSegmentIndex]);
            return Mono.just(new AuthorizationDecision(false));
        }
    }
}
