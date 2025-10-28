package com.honyrun.constant;

/**
 * 路径常量类
 * 提供API路径统一管理、版本控制
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  15:50:00
 * @modified 2025-07-01 15:50:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public final class PathConstants {

    private PathConstants() {
        // 常量类，禁止实例化
    }

    // ==================== API版本 ====================

    /**
     * API版本前缀
     */
    public static final String API_VERSION_PREFIX = "/api";

    /**
     * API版本1
     */
    public static final String API_V1 = API_VERSION_PREFIX + "/v1";

    /**
     * API版本2
     */
    public static final String API_V2 = API_VERSION_PREFIX + "/v2";

    /**
     * 当前API版本
     */
    public static final String API_CURRENT = API_V1;

    // ==================== 认证相关路径 ====================

    /**
     * 认证基础路径
     */
    public static final String AUTH_BASE = API_CURRENT + "/auth";

    /**
     * 登录路径
     */
    public static final String AUTH_LOGIN = AUTH_BASE + "/login";

    /**
     * 登出路径
     */
    public static final String AUTH_LOGOUT = AUTH_BASE + "/logout";

    /**
     * 刷新令牌路径
     */
    public static final String AUTH_REFRESH = AUTH_BASE + "/refresh";

    /**
     * 验证令牌路径
     */
    public static final String AUTH_VALIDATE = AUTH_BASE + "/validate";

    /**
     * 当前认证用户信息路径
     */
    public static final String AUTH_ME = AUTH_BASE + "/me";

    /**
     * 当前认证用户信息路径（别名）
     * 兼容测试与历史文档使用的 /current-user
     */
    public static final String AUTH_CURRENT_USER = AUTH_BASE + "/current-user";

    /**
     * 系统认证基础路径
     */
    public static final String SYSTEM_AUTH_BASE = API_CURRENT + "/system/auth";

    /**
     * 系统强制登出路径
     */
    public static final String SYSTEM_AUTH_FORCE_LOGOUT = SYSTEM_AUTH_BASE + "/force-logout";

    /**
     * 系统获取在线用户路径
     */
    public static final String SYSTEM_AUTH_ACTIVE_USERS = SYSTEM_AUTH_BASE + "/active-users";

    /**
     * 认证统计基础路径
     */
    public static final String AUTH_STATS_BASE = API_CURRENT + "/stats/auth";

    /**
     * 登录统计路径
     */
    public static final String AUTH_STATS_LOGIN = AUTH_STATS_BASE + "/login";

    /**
     * 在线用户统计路径
     */
    public static final String AUTH_STATS_ONLINE = AUTH_STATS_BASE + "/online";

    /**
     * 登录失败统计路径
     */
    public static final String AUTH_STATS_FAILURES = AUTH_STATS_BASE + "/failures";

    // ==================== 用户管理路径 ====================

    /**
     * 用户管理基础路径
     */
    public static final String USER_BASE = API_CURRENT + "/users";

    /**
     * 用户列表路径
     */
    public static final String USER_LIST = USER_BASE;

    /**
     * 用户详情路径
     */
    public static final String USER_DETAIL = USER_BASE + "/{id}";

    /**
     * 用户创建路径
     */
    public static final String USER_CREATE = USER_BASE;

    /**
     * 用户更新路径
     */
    public static final String USER_UPDATE = USER_BASE + "/{id}";

    /**
     * 用户删除路径
     */
    public static final String USER_DELETE = USER_BASE + "/{id}";

    /**
     * 用户权限路径
     */
    public static final String USER_PERMISSIONS = USER_BASE + "/{id}/permissions";

    /**
     * 用户统计基础路径（兼容测试命名）
     */
    public static final String USER_STATS_BASE = USER_BASE + "/stats";

    /**
     * 用户注册统计路径
     */
    public static final String USER_STATS_REGISTRATION = USER_STATS_BASE + "/registration";

    /**
     * 用户类型分布统计路径
     */
    public static final String USER_STATS_TYPE_DISTRIBUTION = USER_STATS_BASE + "/type-distribution";

    /**
     * 活跃用户统计路径
     */
    public static final String USER_STATS_ACTIVE = USER_STATS_BASE + "/active";

    /**
     * 当前用户信息路径
     */
    public static final String USER_CURRENT = USER_BASE + "/current";

    /**
     * 用户搜索路径
     */
    public static final String USER_SEARCH = USER_BASE + "/search";

    /**
     * 用户统计路径
     */
    public static final String USER_STATISTICS = USER_BASE + "/statistics";

    /**
     * 用户批量处理路径
     */
    public static final String USER_BATCH = USER_BASE + "/batch";

    // ==================== 系统管理路径 ====================

    /**
     * 系统管理基础路径
     */
    public static final String SYSTEM_BASE = API_CURRENT + "/system";

    /**
     * 系统设置路径
     */
    public static final String SYSTEM_SETTINGS = SYSTEM_BASE + "/settings";

    /**
     * 系统设置分类列表路径
     */
    public static final String SYSTEM_SETTINGS_CATEGORIES = SYSTEM_SETTINGS + "/categories";

    /**
     * 系统设置批量处理路径
     */
    public static final String SYSTEM_SETTINGS_BATCH = SYSTEM_SETTINGS + "/batch";

    /**
     * 系统设置导出路径
     */
    public static final String SYSTEM_SETTINGS_EXPORT = SYSTEM_SETTINGS + "/export";

    /**
     * 系统设置导入路径
     */
    public static final String SYSTEM_SETTINGS_IMPORT = SYSTEM_SETTINGS + "/import";

    /**
     * 系统日志路径
     */
    public static final String SYSTEM_LOGS = SYSTEM_BASE + "/logs";

    /**
     * 系统状态路径
     */
    public static final String SYSTEM_STATUS = SYSTEM_BASE + "/status";

    /**
     * 系统健康检查路径
     */
    public static final String SYSTEM_HEALTH = SYSTEM_BASE + "/health";

    /**
     * 系统指标路径
     */
    public static final String SYSTEM_METRICS = SYSTEM_BASE + "/metrics";

    /**
     * 系统配置路径
     */
    public static final String SYSTEM_CONFIG = SYSTEM_BASE + "/config";

    /**
     * 系统配置热更新路径
     */
    public static final String SYSTEM_CONFIG_HOT_UPDATE = SYSTEM_CONFIG + "/hot-update";

    /**
     * 系统配置刷新路径
     */
    public static final String SYSTEM_CONFIG_REFRESH = SYSTEM_CONFIG + "/refresh";

    /**
     * 系统配置重载路径
     */
    public static final String SYSTEM_CONFIG_RELOAD = SYSTEM_CONFIG + "/reload";

    /**
     * 系统信息路径
     */
    public static final String SYSTEM_INFO = SYSTEM_BASE + "/info";

    /**
     * 系统统计路径
     */
    public static final String SYSTEM_STATS = SYSTEM_BASE + "/stats";

    /**
     * 系统性能监控路径
     */
    public static final String SYSTEM_PERFORMANCE = SYSTEM_BASE + "/performance";

    /**
     * 系统维护路径
     */
    public static final String SYSTEM_MAINTENANCE = SYSTEM_BASE + "/maintenance";

    /**
     * 系统通知路径
     */
    public static final String SYSTEM_NOTIFICATIONS = SYSTEM_BASE + "/notifications";

    // ==================== 业务功能路径 ====================

    /**
     * 业务功能基础路径
     */
    public static final String BUSINESS_BASE = API_CURRENT + "/business";

    /**
     * 业务功能1路径
     */
    public static final String BUSINESS_FUNCTION_1 = BUSINESS_BASE + "/function1";

    /**
     * 业务功能2路径
     */
    public static final String BUSINESS_FUNCTION_2 = BUSINESS_BASE + "/function2";

    /**
     * 业务功能3路径
     */
    public static final String BUSINESS_FUNCTION_3 = BUSINESS_BASE + "/function3";

    /**
     * 核验业务路径
     */
    public static final String VERIFICATION_BASE = BUSINESS_BASE + "/verification";

    /**
     * 核验请求路径
     */
    public static final String VERIFICATION_REQUEST = VERIFICATION_BASE + "/request";

    /**
     * 核验结果路径
     */
    public static final String VERIFICATION_RESULT = VERIFICATION_BASE + "/result";

    /**
     * 核验统计路径
     */
    public static final String VERIFICATION_STATS = VERIFICATION_BASE + "/stats";

    // ==================== 图片处理路径 ====================

    /**
     * 图片处理基础路径
     */
    public static final String IMAGE_BASE = API_CURRENT + "/image";

    /**
     * 图片转换路径
     */
    public static final String IMAGE_CONVERT = IMAGE_BASE + "/convert";

    /**
     * 图片批量转换路径
     */
    public static final String IMAGE_BATCH_CONVERT = IMAGE_BASE + "/batch-convert";

    /**
     * 图片上传路径
     */
    public static final String IMAGE_UPLOAD = IMAGE_BASE + "/upload";

    /**
     * 图片下载路径
     */
    public static final String IMAGE_DOWNLOAD = IMAGE_BASE + "/download/{id}";

    /**
     * 图片验证路径
     */
    public static final String IMAGE_VALIDATE = IMAGE_BASE + "/validate";

    // ==================== 模拟接口路径 ====================

    /**
     * 模拟接口基础路径
     */
    public static final String MOCK_BASE = API_CURRENT + "/mock";

    /**
     * 模拟接口管理路径
     */
    public static final String MOCK_INTERFACES = MOCK_BASE + "/interfaces";

    /**
     * 模拟接口管理路径（别名）
     */
    public static final String MOCK_MANAGE = MOCK_BASE + "/manage";

    /**
     * 模拟接口执行路径
     */
    public static final String MOCK_EXECUTE = MOCK_BASE + "/execute/{id}";

    // ==================== 外部接口路径 ====================

    /**
     * 外部接口基础路径
     */
    public static final String EXTERNAL_BASE = API_CURRENT + "/external";

    /**
     * 外部接口管理路径
     */
    public static final String EXTERNAL_INTERFACES = EXTERNAL_BASE + "/interfaces";

    // ==================== 版本信息路径 ====================

    /**
     * 版本信息基础路径
     */
    public static final String VERSION_BASE = API_CURRENT + "/version";

    /**
     * 版本信息路径
     */
    public static final String VERSION_INFO = VERSION_BASE + "/info";

    /**
     * 构建信息路径
     */
    public static final String VERSION_BUILD = VERSION_BASE + "/build";

    /**
     * 版本历史路径
     */
    public static final String VERSION_HISTORY = VERSION_BASE + "/history";

    // ==================== 账户与个人资料路径 ====================

    /**
     * 账户基础路径（当前用户）
     */
    public static final String ACCOUNT_BASE = API_CURRENT + "/user";

    /**
     * 账户资料路径
     */
    public static final String ACCOUNT_PROFILE = ACCOUNT_BASE + "/profile";

    /**
     * 账户密码路径
     */
    public static final String ACCOUNT_PASSWORD = ACCOUNT_BASE + "/password";

    // ==================== 静态资源路径 ====================

    /**
     * 静态资源基础路径
     */
    public static final String STATIC_BASE = "/static";

    /**
     * 静态资源根路径别名（兼容性）
     */
    public static final String STATIC_ASSETS = STATIC_BASE;

    /**
     * CSS资源路径
     */
    public static final String STATIC_CSS = STATIC_BASE + "/css/**";

    /**
     * JavaScript资源路径
     */
    public static final String STATIC_JS = STATIC_BASE + "/js/**";

    /**
     * 图片资源路径
     */
    public static final String STATIC_IMAGES = STATIC_BASE + "/images/**";

    /**
     * 字体资源路径
     */
    public static final String STATIC_FONTS = STATIC_BASE + "/fonts/**";

    // ==================== WebSocket路径 ====================

    /**
     * WebSocket基础路径
     */
    public static final String WEBSOCKET_BASE = "/ws";

    /**
     * 系统通知WebSocket路径
     */
    public static final String WEBSOCKET_NOTIFICATIONS = WEBSOCKET_BASE + "/notifications";

    /**
     * 系统监控WebSocket路径
     */
    public static final String WEBSOCKET_MONITORING = WEBSOCKET_BASE + "/monitoring";

    // ==================== 公共路径 ====================

    /**
     * 公共访问路径数组
     * 这些路径不需要认证
     */
    public static final String[] PUBLIC_PATHS = {
            AUTH_LOGIN,
            AUTH_VALIDATE,
            SYSTEM_HEALTH,
            VERSION_INFO,
            STATIC_CSS,
            STATIC_JS,
            STATIC_IMAGES,
            STATIC_FONTS
    };

    /**
     * 管理员专用路径数组
     * 这些路径需要管理员权限
     */
    public static final String[] ADMIN_PATHS = {
            USER_BASE + "/**",
            SYSTEM_BASE + "/**",
            MOCK_MANAGE
    };

    /**
     * 用户路径数组
     * 这些路径普通用户可以访问
     */
    public static final String[] USER_PATHS = {
            USER_CURRENT,
            VERIFICATION_BASE + "/**",
            IMAGE_BASE + "/**",
            VERSION_BASE + "/**"
    };

    // ==================== 路径模式 ====================

    /**
     * 所有路径模式
     */
    public static final String ALL_PATHS = "/**";

    /**
     * API路径模式
     */
    public static final String API_PATHS = API_VERSION_PREFIX + "/**";

    /**
     * 静态资源路径模式
     */
    public static final String STATIC_PATHS = STATIC_BASE + "/**";

    /**
     * WebSocket路径模式
     */
    public static final String WEBSOCKET_PATHS = WEBSOCKET_BASE + "/**";
}


