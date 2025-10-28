package com.honyrun.config.security.permission;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;

import com.honyrun.constant.PathConstants;

import reactor.core.publisher.Mono;

/**
 * 用户搜索路径权限策略
 * 
 * 处理用户搜索路径的权限检查，普通用户可以访问搜索功能
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-17 16:38:00
 * @version 1.0.0 - 策略模式重构版本
 */
@Component
public class UserSearchPermissionStrategy implements PathPermissionStrategy {
    
    @Override
    public boolean supports(String path) {
        return path.equals(PathConstants.USER_SEARCH);
    }
    
    @Override
    public Mono<AuthorizationDecision> checkPermission(String path, AuthorizationContext context) {
        // 搜索用户路径 - 普通用户可以访问
        return Mono.just(new AuthorizationDecision(true));
    }
    
    @Override
    public int getPriority() {
        return 400; // 中等优先级
    }
}
