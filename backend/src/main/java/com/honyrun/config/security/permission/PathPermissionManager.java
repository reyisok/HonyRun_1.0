package com.honyrun.config.security.permission;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;

import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;

/**
 * 路径权限管理器
 * 
 * 统一管理所有路径权限检查策略，根据优先级和支持情况选择合适的策略
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-17 16:42:00
 * @version 1.0.0 - 策略模式重构版本
 */
@Component
public class PathPermissionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(PathPermissionManager.class);
    
    private final List<PathPermissionStrategy> strategies;
    
    /**
     * 构造函数注入所有权限策略
     * 
     * @param strategies 权限策略列表
     */
    public PathPermissionManager(List<PathPermissionStrategy> strategies) {
        // 按优先级排序策略
        this.strategies = strategies.stream()
                .sorted(Comparator.comparingInt(PathPermissionStrategy::getPriority))
                .toList();
        
        LoggingUtil.info(logger, "Initialized PathPermissionManager with {} strategies", strategies.size());
        for (PathPermissionStrategy strategy : this.strategies) {
            LoggingUtil.info(logger, "Registered strategy: {} with priority: {}", 
                    strategy.getClass().getSimpleName(), strategy.getPriority());
        }
    }
    
    /**
     * 检查路径权限
     * 
     * @param path 请求路径
     * @param context 授权上下文
     * @return 权限检查结果
     */
    public Mono<AuthorizationDecision> checkPathPermission(String path, AuthorizationContext context) {
        LoggingUtil.info(logger, "Checking path permission for: {}", path);
        
        // 按优先级查找支持该路径的策略
        for (PathPermissionStrategy strategy : strategies) {
            if (strategy.supports(path)) {
                LoggingUtil.info(logger, "Using strategy: {} for path: {}", 
                        strategy.getClass().getSimpleName(), path);
                return strategy.checkPermission(path, context);
            }
        }
        
        // 没有找到支持的策略，默认拒绝访问
        LoggingUtil.warn(logger, "No strategy found for path: {}, denying access", path);
        return Mono.just(new AuthorizationDecision(false));
    }
}
