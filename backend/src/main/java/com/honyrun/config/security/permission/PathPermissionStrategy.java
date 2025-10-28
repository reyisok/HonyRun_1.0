package com.honyrun.config.security.permission;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

/**
 * 路径权限检查策略接口
 * 
 * 用于将复杂的路径权限检查逻辑分解为独立的策略实现，
 * 提高代码的可维护性和扩展性
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-17 16:30:00
 * @version 1.0.0 - 策略模式重构版本
 */
public interface PathPermissionStrategy {
    
    /**
     * 检查是否支持该路径的权限验证
     * 
     * @param path 请求路径
     * @return 是否支持该路径
     */
    boolean supports(String path);
    
    /**
     * 执行路径权限检查
     * 
     * @param path 请求路径
     * @param context 授权上下文
     * @return 权限检查结果
     */
    Mono<AuthorizationDecision> checkPermission(String path, AuthorizationContext context);
    
    /**
     * 获取策略优先级
     * 数值越小优先级越高
     * 
     * @return 优先级数值
     */
    default int getPriority() {
        return Integer.MAX_VALUE;
    }
}
