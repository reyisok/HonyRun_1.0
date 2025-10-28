package com.honyrun.config.security.permission;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;

import com.honyrun.constant.PathConstants;

import reactor.core.publisher.Mono;

/**
 * 用户列表路径权限策略
 * 
 * 处理用户列表路径的权限检查，普通用户不能访问用户列表
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-17 16:37:00
 * @version 1.0.0 - 策略模式重构版本
 */
@Component
public class UserListPermissionStrategy implements PathPermissionStrategy {
    
    @Override
    public boolean supports(String path) {
        return path.equals(PathConstants.USER_BASE);
    }
    
    @Override
    public Mono<AuthorizationDecision> checkPermission(String path, AuthorizationContext context) {
        // 用户列表路径 - 普通用户不能访问
        return Mono.just(new AuthorizationDecision(false));
    }
    
    @Override
    public int getPriority() {
        return 300; // 中等优先级
    }
}
