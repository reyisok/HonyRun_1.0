package com.honyrun.config.router;

import com.honyrun.constant.PathConstants;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 动态路由注册表
 *
 * 用于动态管理和验证系统中的所有路由，提供准确的404路由判定机制。
 * 与UnifiedRouterConfig集成，自动提取函数式路由配置，确保路由验证的准确性。
 *
 * 主要功能：
 * 1. 从UnifiedRouterConfig动态提取路由模式
 * 2. 路由存在性验证
 * 3. 路径匹配检查
 * 4. 支持正则表达式路径模式
 *
 * 最佳实践：
 * - 线程安全的路由存储
 * - 高效的路径匹配算法
 * - 与函数式路由配置同步
 * - 提供详细的调试信息
 *
 * @author Mr.Rey
 * @version 1.0.0
 * @created 2025-07-02 21:00:00
 * @modified 2025-07-02 21:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class RouteRegistry {

    private static final Logger logger = LoggerFactory.getLogger(RouteRegistry.class);

    /**
     * 存储已注册的路由模式
     * 使用ConcurrentHashMap确保线程安全
     */
    private final Set<String> registeredRoutes = ConcurrentHashMap.newKeySet();

    /**
     * 存储编译后的正则表达式模式
     * 用于高效的路径匹配
     */
    private final Set<Pattern> routePatterns = ConcurrentHashMap.newKeySet();

    /**
     * 路由存在性结果缓存
     * key 为请求路径，value 表示该路径是否存在已注册路由
     * 采用并发映射以支持高并发场景下的快速读取
     */
    private final java.util.concurrent.ConcurrentHashMap<String, Boolean> routeExistenceCache = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 初始化路由注册表
     * 从UnifiedRouterConfig提取所有已知的路由模式
     */
    public void initializeRoutes() {
        // 约定：先注册静态前缀与精确路径，再注册包含变量段的模式路由；新增端点需遵循该顺序。
        // 建议：在启动校验中提示潜在冲突（如 `/statistics` 与 `/{id}`），降低静态端点被动态模式误匹配风险。
        LoggingUtil.info(logger, "初始化路由注册表，从UnifiedRouterConfig提取路由");

        // 认证路由 - 对应AuthRouterFunction
        registerRoute(PathConstants.AUTH_LOGIN);
        registerRoute(PathConstants.AUTH_LOGOUT);
        registerRoute(PathConstants.AUTH_REFRESH);
        registerRoute(PathConstants.AUTH_VALIDATE);
        registerRoute(PathConstants.AUTH_ME);
        registerRoute(PathConstants.AUTH_CURRENT_USER);
        // 认证统计路由
        registerRoute(PathConstants.AUTH_STATS_LOGIN);
        registerRoute(PathConstants.AUTH_STATS_ONLINE);
        registerRoute(PathConstants.AUTH_STATS_FAILURES);
        // 系统认证路由
        registerRoute(PathConstants.SYSTEM_AUTH_FORCE_LOGOUT);
        registerRoute(PathConstants.SYSTEM_AUTH_ACTIVE_USERS);
        // 移除通配符模式，改为注册具体的静态路由以避免路由约定校验警告

        // 用户管理路由 - 对应UserRouterFunction，按照静态在前、动态在后的约定注册
        registerRoute(PathConstants.USER_LIST);
        registerRoute(PathConstants.USER_CURRENT);
        registerRoute(PathConstants.USER_SEARCH);
        registerRoute(PathConstants.USER_BASE + "/status");
        registerRoute(PathConstants.USER_STATISTICS);
        // 用户管理路由 - 对应UserRouterFunction，按照静态在前、动态在后的约定注册
        registerRoute(PathConstants.USER_BASE);
        registerRoute(PathConstants.USER_BATCH);
        // 用户统计（兼容测试路径）
        registerRoute(PathConstants.USER_STATS_REGISTRATION);
        registerRoute(PathConstants.USER_STATS_TYPE_DISTRIBUTION);
        registerRoute(PathConstants.USER_STATS_ACTIVE);
        // 测试边界路径 - 对应UserRouterFunction中的测试端点
        registerRoute("/api/v1/users/empty");
        registerRoute("/api/v1/users/boundary");
        // 用户计数端点 - 对应UserRouterFunction中的countUsers方法
        registerRoute(PathConstants.USER_BASE + "/count");
        
        // 动态模式路由放在最后
        registerRoutePattern(PathConstants.USER_BASE + "/\\d+");
        registerRoutePattern(PathConstants.USER_BASE + "/username/[^/]+");
        // 用户存在性检查
        registerRoutePattern(PathConstants.USER_BASE + "/exists/username/[^/]+");
        registerRoutePattern(PathConstants.USER_BASE + "/exists/email/[^/]+");
        registerRoutePattern(PathConstants.USER_BASE + "/\\d+/permissions");
        registerRoutePattern(PathConstants.USER_BASE + "/\\d+/status");
        registerRoutePattern(PathConstants.USER_BASE + "/\\d+/activities");
        // 启用/禁用与密码操作
        registerRoutePattern(PathConstants.USER_BASE + "/\\d+/enable");
        registerRoutePattern(PathConstants.USER_BASE + "/\\d+/disable");
        registerRoutePattern(PathConstants.USER_BASE + "/\\d+/change-password");
        registerRoutePattern(PathConstants.USER_BASE + "/\\d+/reset-password");

        // 个人资料路由 - 对应UserRouterFunction
        registerRoute(PathConstants.ACCOUNT_PROFILE);
        registerRoute(PathConstants.ACCOUNT_PASSWORD);

        // 系统管理路由 - 对应SystemRouterFunction
        registerRoute(PathConstants.SYSTEM_INFO);
        registerRoute(PathConstants.SYSTEM_STATUS);
        registerRoute(PathConstants.SYSTEM_CONFIG);
        registerRoute(PathConstants.SYSTEM_CONFIG_REFRESH);
        registerRoute(PathConstants.SYSTEM_CONFIG_HOT_UPDATE);
        registerRoute(PathConstants.SYSTEM_CONFIG_RELOAD);
        registerRoute(PathConstants.SYSTEM_HEALTH);
        
        // 系统设置路由 - 对应SystemRouterFunction中的systemSettingsRoutes，按照静态在前、动态在后的约定注册
        registerRoute(PathConstants.SYSTEM_SETTINGS);
        registerRoute(PathConstants.SYSTEM_SETTINGS_CATEGORIES);
        registerRoute(PathConstants.SYSTEM_SETTINGS_BATCH);
        registerRoute(PathConstants.SYSTEM_SETTINGS_EXPORT);
        registerRoute(PathConstants.SYSTEM_SETTINGS_IMPORT);
        // 半静态路径
        registerRoutePattern(PathConstants.SYSTEM_SETTINGS + "/category/[^/]+");
        // 动态路径模式 - 使用更精确的模式，避免匹配已注册的静态端点
        registerRoutePattern(PathConstants.SYSTEM_SETTINGS + "/(?!categories|batch|export|import|category/)[^/]+");

        // 系统监控路由 - 对应SystemRouterFunction
        registerRoute(PathConstants.SYSTEM_LOGS);
        registerRoute(PathConstants.SYSTEM_STATS);
        registerRoute(PathConstants.SYSTEM_PERFORMANCE);
        registerRoute(PathConstants.SYSTEM_MAINTENANCE);

        // 版本信息路由 - 对应SystemRouterFunction，按照静态在前、动态在后的约定注册
        registerRoute(PathConstants.VERSION_BASE);
        registerRoute(PathConstants.VERSION_INFO);
        registerRoute(PathConstants.VERSION_BUILD);
        registerRoute(PathConstants.VERSION_BASE + "/status");
        registerRoute(PathConstants.VERSION_BASE + "/history");
        registerRoute(PathConstants.VERSION_BASE + "/check");
        // API版本管理路由 - 对应apiVersionRoutes
        registerRoute("/api/v1/version/supported");
        registerRoute("/api/v1/version/changelog");
        registerRoute("/api/v1/version/migration");
        registerRoute("/api/v1/version/compatibility");
        registerRoute("/api/v1/version/usage-stats");
        registerRoute("/api/v1/version/timeline");
        // 动态模式路由 - 仅匹配特定的参数化路径
        registerRoutePattern("/api/v1/version/changelog/[^/]+");
        registerRoutePattern("/api/v1/version/deprecation/[^/]+");
        registerRoutePattern("/api/v1/version/deprecate/[^/]+");

        // 图片处理路由 - 仅注册实际存在的静态路由，避免广泛的动态模式匹配
        registerRoute(PathConstants.IMAGE_VALIDATE);
        registerRoute(PathConstants.IMAGE_CONVERT);
        registerRoute(PathConstants.IMAGE_BATCH_CONVERT);
        registerRoute(PathConstants.IMAGE_CONVERT.replace("/convert", "/compress"));
        registerRoute(PathConstants.IMAGE_CONVERT.replace("/convert", "/resize"));
        registerRoute(PathConstants.IMAGE_CONVERT.replace("/convert", "/watermark"));
        // 注意：移除了 PathConstants.IMAGE_BASE + "/.*" 广泛动态模式
        // 避免与静态端点产生路由约定冲突

        // 验证业务路由 - 对应VerificationRouterFunction，按照静态在前、动态在后的约定注册
        registerRoute("/api/v1/verification");
        registerRoute("/api/v1/verification/statistics");
        // 动态模式路由放在最后
        registerRoutePattern("/api/v1/verification/\\d+");
        registerRoutePattern("/api/v1/verification/\\d+/complete");
        registerRoutePattern("/api/v1/verification/\\d+/cancel");
        registerRoutePattern("/api/v1/verification/\\d+/result");

        // 业务功能路由 - 仅注册实际存在的静态路由，避免广泛的动态模式匹配
        registerRoute(PathConstants.BUSINESS_FUNCTION_1);
        registerRoute(PathConstants.BUSINESS_BASE + "/function2");
        registerRoute(PathConstants.BUSINESS_BASE + "/function3");
        registerRoute(PathConstants.BUSINESS_BASE + "/statistics");
        // 注意：移除了 PathConstants.BUSINESS_BASE + "/.*" 广泛动态模式
        // 避免与静态端点产生路由约定冲突

        // 健康检查路由 - 对应Actuator
        registerRoute("/actuator/health");
        registerRoute("/actuator/info");
        registerRoutePattern("/actuator/health/.*");

        // 静态资源路由 - 对应StaticResourceRouterFunction
        registerRoute("/");
        registerRoute("/favicon.ico");
        registerRoutePattern(PathConstants.STATIC_BASE + "/.*");
        registerRoutePattern(PathConstants.STATIC_ASSETS + "/.*");
        registerRoutePattern(PathConstants.STATIC_IMAGES + "/.*");
        registerRoutePattern(PathConstants.STATIC_CSS + "/.*");
        registerRoutePattern(PathConstants.STATIC_JS + "/.*");

        LoggingUtil.info(logger, "路由注册表初始化完成，共注册 {} 个精确路由和 {} 个路由模式",
                        registeredRoutes.size(), routePatterns.size());
    }

    /**
     * 注册精确路由
     *
     * @param route 路由路径
     */
    public void registerRoute(String route) {
        registeredRoutes.add(route);
        // 路由变更时清空缓存，避免使用过期结果
        routeExistenceCache.clear();
        LoggingUtil.debug(logger, "注册精确路由: {}", route);
    }

    /**
     * 注册路由模式（支持正则表达式）
     *
     * @param routePattern 路由模式
     */
    public void registerRoutePattern(String routePattern) {
        try {
            // 支持路径模板占位符，例如 /api/v1/users/{userId}/orders/{orderId}
            // 将 {var} 转换为正则片段 [^/]+，以匹配单个路径段
            String regex = routePattern;
            if (routePattern != null) {
                // 处理Spring路径模式中的通配符
                // 将 ** 转换为 .*（匹配任意字符）
                regex = regex.replace("/**", "/.*");
                // 将 * 转换为 [^/]*（匹配单个路径段内的任意字符）
                regex = regex.replace("/*", "/[^/]*");
                // 处理路径变量占位符 {var}
                if (regex.contains("{")) {
                    regex = regex.replaceAll("\\{[^}]+\\}", "[^/]+");
                }
            }
            Pattern pattern = Pattern.compile(regex);
            routePatterns.add(pattern);
            // 路由模式变更时清空缓存，避免使用过期结果
            routeExistenceCache.clear();
            LoggingUtil.debug(logger, "注册路由模式: {} -> {}", routePattern, regex);
        } catch (Exception e) {
            LoggingUtil.error(logger, "注册路由模式失败: [{}]", routePattern);
            LoggingUtil.error(logger, "错误详情: {}", e.getMessage());
        }
    }

    /**
     * 检查路由是否存在
     *
     * @param path 请求路径
     * @return true 如果路由存在，false 否则
     */
    public boolean isRouteExists(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        // 缓存快速路径：若已有判定结果，直接返回
        Boolean cached = routeExistenceCache.get(path);
        if (cached != null) {
            return cached;
        }
        // 首先检查精确匹配
        if (registeredRoutes.contains(path)) {
            LoggingUtil.debug(logger, "路由精确匹配: {}", path);
            routeExistenceCache.put(path, true);
            return true;
        }

        // 然后检查模式匹配
        for (Pattern pattern : routePatterns) {
            if (pattern.matcher(path).matches()) {
                LoggingUtil.debug(logger, "路由模式匹配: {} -> {}", path, pattern.pattern());
                routeExistenceCache.put(path, true);
                return true;
            }
        }

        LoggingUtil.debug(logger, "路由不存在: {}", path);
        routeExistenceCache.put(path, false);
        return false;
    }

    /**
     * 检查是否为不存在的路由（用于404判定）
     *
     * @param path 请求路径
     * @return true 如果路由不存在，false 否则
     */
    public boolean isNonExistentRoute(String path) {
        // 空路径或null视为不存在
        if (path == null || path.trim().isEmpty()) {
            return true;
        }

        // 统一委托到 isRouteExists，避免与统一路由配置不一致导致的误判
        // 包括 /error/*、/api-docs 等在 UnifiedRouterConfig 中定义的路由
        return !isRouteExists(path);
    }

    /**
     * 获取所有已注册的路由
     *
     * @return 已注册的路由集合
     */
    public Set<String> getRegisteredRoutes() {
        return Set.copyOf(registeredRoutes);
    }

    /**
     * 获取已注册的路由模式数量
     *
     * @return 路由模式数量
     */
    public int getRoutePatternCount() {
        return routePatterns.size();
    }

    /**
     * 获取所有已注册的路由模式（Pattern）
     *
     * @return 路由模式集合的副本
     */
    public Set<Pattern> getRoutePatterns() {
        return Set.copyOf(routePatterns);
    }

    /**
     * 获取所有已注册的路由模式字符串表示
     *
     * @return 路由模式字符串集合
     */
    public Set<String> getRoutePatternStrings() {
        return routePatterns.stream()
                .map(Pattern::pattern)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    /**
     * 清空路由注册表
     */
    public void clearRoutes() {
        registeredRoutes.clear();
        routePatterns.clear();
        routeExistenceCache.clear();
        LoggingUtil.info(logger, "路由注册表已清空");
    }

    /**
     * 动态添加路由函数的路由
     *
     * @param routerFunction 路由函数
     */
    public void registerRouterFunction(RouterFunction<ServerResponse> routerFunction) {
        // 这里可以通过反射或其他方式提取路由函数中的路径
        // 当前版本先使用手动注册的方式
        LoggingUtil.debug(logger, "注册路由函数: {}", routerFunction.getClass().getSimpleName());
    }
}

