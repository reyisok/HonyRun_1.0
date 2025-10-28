package com.honyrun.config.security.permission;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;

import com.honyrun.constant.PathConstants;

import reactor.core.publisher.Mono;

/**
 * 当前用户路径权限策略
 * 
 * 处理当前用户端点的权限检查，普通用户可以访问自己的信息
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-17 16:36:00
 * @version 1.0.0 - 策略模式重构版本
 */
@Component
public class CurrentUserPermissionStrategy implements PathPermissionStrategy {
    
    @Override
    public boolean supports(String path) {
        return path.equals(PathConstants.USER_CURRENT);
    }
    
    @Override
    public Mono<AuthorizationDecision> checkPermission(String path, AuthorizationContext context) {
        // 当前用户端点 - 普通用户可以访问
        return Mono.just(new AuthorizationDecision(true));
    }
    
    @Override
    public int getPriority() {
        return 200; // 高优先级
    }
}
