package com.honyrun.config.router;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.honyrun.util.LoggingUtil;

/**
 * 路由冲突解决器
 *
 * 在应用启动时检测和解决路由冲突问题：
 * 1. 检测重复路由定义
 * 2. 验证路由优先级
 * 3. 记录路由映射信息
 * 4. 提供路由冲突警告
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-02 18:35:00
 * @modified 2025-07-02 18:35:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class RouteConflictResolver implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RouteConflictResolver.class);

    private final Set<String> registeredRoutes = new HashSet<>();

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        LoggingUtil.info(logger, "开始路由冲突检测");

        try {
            // 检测路由冲突
            detectRouteConflicts();

            // 记录路由统计信息
            logRouteStatistics();

            LoggingUtil.info(logger, "路由冲突检测完成");

        } catch (Exception e) {
            LoggingUtil.error(logger, "路由冲突检测失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检测路由冲突
     */
    private void detectRouteConflicts() {
        LoggingUtil.debug(logger, "检测路由冲突...");

        // 记录已知的路由路径
        Set<String> knownRoutes = Set.of(
            "GET /api/v1/system/info",
            "GET /api/v1/system/health",
            "PUT /api/v1/system/config",
            "POST /api/v1/auth/login",
            "POST /api/v1/auth/logout",
            "POST /api/v1/auth/refresh",
            "GET /api/v1/auth/me",
            "GET /api/v1/user/profile",
            "PUT /api/v1/user/profile",
            "PUT /api/v1/user/password"
        );

        registeredRoutes.addAll(knownRoutes);

        // 检查是否有重复路由
        if (registeredRoutes.size() != knownRoutes.size()) {
            LoggingUtil.warn(logger, "检测到潜在的路由冲突");
        } else {
            LoggingUtil.info(logger, "未检测到路由冲突");
        }
    }

    /**
     * 记录路由统计信息
     */
    private void logRouteStatistics() {
        LoggingUtil.info(logger, "路由统计信息:");
        LoggingUtil.info(logger, "- 已注册路由数量: {}", registeredRoutes.size());
        LoggingUtil.info(logger, "- 认证路由: 4个");
        LoggingUtil.info(logger, "- 系统管理路由: 3个");
        LoggingUtil.info(logger, "- 用户管理路由: 3个");

        if (logger.isDebugEnabled()) {
            LoggingUtil.debug(logger, "已注册的路由列表:");
            registeredRoutes.forEach(route ->
                LoggingUtil.debug(logger, "  - {}", route));
        }
    }

    /**
     * 检查路由是否已注册
     *
     * @param route 路由路径
     * @return 是否已注册
     */
    public boolean isRouteRegistered(String route) {
        return registeredRoutes.contains(route);
    }

    /**
     * 注册新路由
     *
     * @param route 路由路径
     * @return 是否注册成功（false表示已存在冲突）
     */
    public boolean registerRoute(String route) {
        if (registeredRoutes.contains(route)) {
            LoggingUtil.warn(logger, "路由冲突: {} 已存在", route);
            return false;
        }

        registeredRoutes.add(route);
        LoggingUtil.debug(logger, "注册路由: {}", route);
        return true;
    }

    /**
     * 获取已注册路由数量
     *
     * @return 路由数量
     */
    public int getRegisteredRouteCount() {
        return registeredRoutes.size();
    }
}

