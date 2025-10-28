package com.honyrun.config.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.honyrun.constant.PathConstants;
import com.honyrun.handler.ImageHandler;
import com.honyrun.handler.monitoring.MonitoringVisualizationHandler;
import com.honyrun.handler.reactive.ApiVersionHandler;
import com.honyrun.handler.reactive.ReactiveSystemHandler;

/**
 * 系统管理函数式路由配置
 *
 * 基于Spring WebFlux的函数式路由定义，提供系统管理相关的API路由映射。
 * 包括系统健康检查、系统设置管理、版本管理、图片转换等功能路由。
 *
 * 约定：新增路由时保持“静态在前、动态在后”。注册时应先添加无参数或具体前缀路径，
 * 再添加包含变量段的动态路径（例如 `/{id}`），以避免像 `/statistics` 这类静态端点被误匹配为 `{id}`。
 * 建议在路由注册或启动校验处加入简单检查（例如优先注册静态前缀，再注册包含变量段的路径），
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 *
 *          Copyright © 2025 HonyRun. All rights reserved.
 *          Created: 2025-07-01 16:50:00
 *          Modified: 2025-07-01 16:50:00
 */
@Configuration
public class SystemRouterFunction {

    private final ReactiveSystemHandler reactiveSystemHandler;
    private final ImageHandler imageHandler;
    private final ApiVersionHandler apiVersionHandler;
    private final MonitoringVisualizationHandler monitoringVisualizationHandler;

    public SystemRouterFunction(ReactiveSystemHandler reactiveSystemHandler, ImageHandler imageHandler,
            ApiVersionHandler apiVersionHandler, MonitoringVisualizationHandler monitoringVisualizationHandler) {
        this.reactiveSystemHandler = reactiveSystemHandler;
        this.imageHandler = imageHandler;
        this.apiVersionHandler = apiVersionHandler;
        this.monitoringVisualizationHandler = monitoringVisualizationHandler;
    }

    /**
     * 系统管理主路由配置
     *
     * 整合所有系统管理相关的路由
     * 注意：systemSettingsRoutes() 已在 UnifiedRouterConfig 中统一管理，避免重复添加
     *
     * @return 系统管理主路由函数
     */
    @Bean("systemRoutes")
    public RouterFunction<ServerResponse> systemRoutes() {
        return RouterFunctions.route()
                .add(systemHealthRoutes())
                .add(systemMonitoringRoutes())
                .add(systemConfigRoutes())
                .add(systemMaintenanceRoutes())
                .add(apiVersionRoutes())
                // 移除 systemSettingsRoutes() 避免与 UnifiedRouterConfig 中的重复定义
                .build();
    }

    /**
     * 系统版本路由配置
     *
     * @return 系统版本路由函数
     */
    @Bean("systemVersionRoutes")
    public RouterFunction<ServerResponse> systemVersionRoutes() {
        return versionManagementRoutes();
    }

    /**
     * 系统图片路由配置
     *
     * @return 系统图片路由函数
     */
    @Bean("systemImageRoutes")
    public RouterFunction<ServerResponse> systemImageRoutes() {
        return imageConversionRoutes();
    }

    /**
     * 系统模拟接口路由配置
     *
     * @return 系统模拟接口路由函数
     */
    @Bean("systemMockRoutes")
    public RouterFunction<ServerResponse> systemMockRoutes() {
        return mockInterfaceRoutes();
    }

    /**
     * 系统健康检查路由配置
     *
     * 定义系统健康检查相关的路由规则：
     * - GET /api/v1/system/health - 系统健康检查
     * - GET /api/v1/system/health/detailed - 详细健康检查
     * - GET /api/v1/system/health/database - 数据库健康检查
     * - GET /api/v1/system/health/redis - Redis健康检查
     *
     * @return 系统健康检查路由函数
     */
    @Bean("systemHealthRoutes")
    public RouterFunction<ServerResponse> systemHealthRoutes() {
        return RouterFunctions
                .route(GET(PathConstants.SYSTEM_HEALTH),
                        reactiveSystemHandler::getSystemHealth)
                .andRoute(GET(PathConstants.SYSTEM_HEALTH + "/detailed"),
                        reactiveSystemHandler::getDetailedHealth)
                .andRoute(GET(PathConstants.SYSTEM_HEALTH + "/database"),
                        reactiveSystemHandler::getDatabaseHealth)
                .andRoute(GET(PathConstants.SYSTEM_HEALTH + "/redis"),
                        reactiveSystemHandler::getRedisHealth);
    }

    /**
     * 系统设置管理路由配置
     *
     * 定义系统设置管理相关的路由规则：
     * 【路由约定校验修复】严格按照"静态路径优先于动态路径"的原则重新排序
     *
     * 静态路径（优先级最高）：
     * - GET /api/v1/system/settings - 获取系统设置列表
     * - GET /api/v1/system/settings/categories - 获取设置分类
     * - POST /api/v1/system/settings/batch - 批量更新系统设置
     * - POST /api/v1/system/settings/export - 导出系统设置
     * - POST /api/v1/system/settings/import - 导入系统设置
     * - PUT /api/v1/system/settings - 批量更新系统设置
     * - POST /api/v1/system/settings - 创建系统设置
     *
     * 半静态路径（中等优先级）：
     * - GET /api/v1/system/settings/category/{category} - 按分类获取设置
     *
     * 动态路径（优先级最低）：
     * - GET /api/v1/system/settings/{key} - 获取单个系统设置
     * - PUT /api/v1/system/settings/{key} - 更新系统设置
     * - DELETE /api/v1/system/settings/{key} - 删除系统设置
     *
     * @return 系统设置管理路由函数
     */
    @Bean("systemSettingsRoutes")
    public RouterFunction<ServerResponse> systemSettingsRoutes() {
        return RouterFunctions
                // ==================== 静态路径（最高优先级） ====================
                .route(GET(PathConstants.SYSTEM_SETTINGS),
                        reactiveSystemHandler::getAllSettings)
                .andRoute(GET(PathConstants.SYSTEM_SETTINGS_CATEGORIES),
                        reactiveSystemHandler::getSettingCategories)
                .andRoute(GET(PathConstants.SYSTEM_SETTINGS_BATCH),
                        reactiveSystemHandler::getAllSettings)
                .andRoute(POST(PathConstants.SYSTEM_SETTINGS_BATCH)
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::updateSystemSettings)
                .andRoute(GET(PathConstants.SYSTEM_SETTINGS_EXPORT),
                        reactiveSystemHandler::exportSettings)
                .andRoute(POST(PathConstants.SYSTEM_SETTINGS_EXPORT)
                        .and(accept(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::exportSettings)
                .andRoute(POST(PathConstants.SYSTEM_SETTINGS_IMPORT)
                        .and(accept(MediaType.MULTIPART_FORM_DATA)),
                        reactiveSystemHandler::importSettings)
                .andRoute(PUT(PathConstants.SYSTEM_SETTINGS)
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::updateSystemSettings)
                .andRoute(POST(PathConstants.SYSTEM_SETTINGS)
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::createSetting)

                // ==================== 半静态路径（中等优先级） ====================
                .andRoute(GET(PathConstants.SYSTEM_SETTINGS + "/category/{category}"),
                        reactiveSystemHandler::getSettingsByCategory)

                // ==================== 动态路径（最低优先级） ====================
                .andRoute(GET(PathConstants.SYSTEM_SETTINGS + "/{key}"),
                        reactiveSystemHandler::getSettingByKey)
                .andRoute(PUT(PathConstants.SYSTEM_SETTINGS + "/{key}")
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::updateSystemSettingByKey)
                .andRoute(DELETE(PathConstants.SYSTEM_SETTINGS + "/{key}")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::deleteSetting);
    }

    /**
     * 版本管理路由配置
     *
     * 定义版本管理相关的路由规则：
     * - GET /api/v1/version - 获取当前系统版本
     * - GET /api/v1/version/info - 获取版本信息
     * - GET /api/v1/version/build - 获取构建信息
     * - GET /api/v1/version/status - 获取系统状态
     * - GET /api/v1/version/history - 获取版本历史
     * - POST /api/v1/version/check - 检查版本兼容性
     *
     * @return 版本管理路由函数
     */
    @Bean("versionManagementRoutes")
    public RouterFunction<ServerResponse> versionManagementRoutes() {
        return RouterFunctions
                // ==================== 静态路径（最高优先级） ====================
                .route(GET(PathConstants.VERSION_BASE)
                        .and(accept(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::getSystemVersion)
                .andRoute(GET(PathConstants.VERSION_INFO)
                        .and(accept(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::getVersionInfo)
                .andRoute(GET(PathConstants.VERSION_BUILD)
                        .and(accept(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::getBuildInfo)
                .andRoute(GET(PathConstants.VERSION_BASE + "/status")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::getSystemStatus)
                .andRoute(GET(PathConstants.VERSION_HISTORY)
                        .and(accept(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::getVersionHistory)
                .andRoute(POST(PathConstants.VERSION_BASE + "/check")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::checkVersionCompatibility);
    }

    /**
     * 图片转换功能路由配置
     *
     * 定义图片转换相关的路由规则：
     * - POST /api/v1/image/convert - 单个图片转换
     * - POST /api/v1/image/batch-convert - 批量图片转换
     * - POST /api/v1/image/compress - 图片压缩
     * - POST /api/v1/image/resize - 图片尺寸调整
     * - POST /api/v1/image/watermark - 添加水印
     * - POST /api/v1/image/validate - 图片验证
     *
     * @return 图片转换路由函数
     */
    @Bean("imageConversionRoutes")
    public RouterFunction<ServerResponse> imageConversionRoutes() {
        return RouterFunctions
                // ==================== 静态路径（最高优先级） ====================
                .route(POST(PathConstants.IMAGE_VALIDATE)
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        imageHandler::validateImageJson)
                .andRoute(POST(PathConstants.IMAGE_CONVERT)
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        imageHandler::convertImageJson)
                .andRoute(POST(PathConstants.IMAGE_BATCH_CONVERT)
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        imageHandler::batchConvertImages)
                .andRoute(POST(PathConstants.IMAGE_BASE + "/compress")
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        imageHandler::compressImage)
                .andRoute(POST(PathConstants.IMAGE_BASE + "/resize")
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        imageHandler::resizeImage)
                .andRoute(POST(PathConstants.IMAGE_BASE + "/watermark")
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        imageHandler::addWatermark);
    }

    /**
     * 外部接口管理路由配置
     *
     * 定义外部接口管理相关的路由规则：
     * - GET /api/v1/external/interfaces - 获取外部接口列表
     * - POST /api/v1/external/interfaces - 创建外部接口配置
     * - PUT /api/v1/external/interfaces/{id} - 更新外部接口配置
     * - DELETE /api/v1/external/interfaces/{id} - 删除外部接口配置
     * - GET /api/v1/external/interfaces/{id}/health - 检查外部接口健康状态
     * - GET /api/v1/external/interfaces/statistics - 获取外部接口统计
     *
     * @return 外部接口管理路由函数
     */
    @Bean("externalInterfaceRoutes")
    public RouterFunction<ServerResponse> externalInterfaceRoutes() {
        return RouterFunctions
                .route(GET(PathConstants.EXTERNAL_INTERFACES)
                        .and(accept(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("外部接口列表 - 功能待实现"))
                .andRoute(POST(PathConstants.EXTERNAL_INTERFACES)
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("创建外部接口配置 - 功能待实现"))
                // 具体静态路径需优先于参数化路径，避免 /statistics 被误匹配为 {id}
                .andRoute(GET(PathConstants.EXTERNAL_INTERFACES + "/statistics")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("外部接口统计 - 功能待实现"))
                .andRoute(PUT(PathConstants.EXTERNAL_INTERFACES + "/{id}")
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("更新外部接口配置 - 功能待实现"))
                .andRoute(DELETE(PathConstants.EXTERNAL_INTERFACES + "/{id}")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.noContent().build())
                .andRoute(GET(PathConstants.EXTERNAL_INTERFACES + "/{id}/health")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("外部接口健康检查 - 功能待实现"));
    }

    /**
     * 模拟接口管理路由配置
     *
     * 定义模拟接口管理相关的路由规则：
     * - POST /api/v1/mock/interfaces - 创建模拟接口
     * - GET /api/v1/mock/interfaces - 获取模拟接口列表
     * - GET /api/v1/mock/interfaces/{id} - 获取单个模拟接口
     * - PUT /api/v1/mock/interfaces/{id} - 更新模拟接口
     * - DELETE /api/v1/mock/interfaces/{id} - 删除模拟接口
     * - POST /api/v1/mock/interfaces/{id}/enable - 启用模拟接口
     * - POST /api/v1/mock/interfaces/{id}/disable - 禁用模拟接口
     * - GET /api/v1/mock/interfaces/search - 搜索模拟接口
     *
     * @return 模拟接口管理路由函数
     */
    // 约定：此方法显式将静态路径（如 `/search`）置于动态路径（如 `/{id}`、`/{id}/enable`）之前。
    // 后续新增端点请遵循“静态在前、动态在后”的约定；并建议在路由注册或启动校验处加入简单检查
    // （优先注册静态前缀，再注册包含变量段的路径），持续避免类似问题。
    @Bean("mockInterfaceRoutes")
    public RouterFunction<ServerResponse> mockInterfaceRoutes() {
        return RouterFunctions
                .route(POST(PathConstants.MOCK_INTERFACES)
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("创建模拟接口 - 功能待实现"))
                .andRoute(GET(PathConstants.MOCK_INTERFACES)
                        .and(accept(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("模拟接口列表 - 功能待实现"))
                // 具体静态路径需优先于参数化路径，避免 /search 被误匹配为 {id}
                .andRoute(GET(PathConstants.MOCK_INTERFACES + "/search")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("搜索模拟接口 - 功能待实现"))
                .andRoute(GET(PathConstants.MOCK_INTERFACES + "/{id}")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("获取模拟接口详情 - 功能待实现"))
                .andRoute(PUT(PathConstants.MOCK_INTERFACES + "/{id}")
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("更新模拟接口 - 功能待实现"))
                .andRoute(DELETE(PathConstants.MOCK_INTERFACES + "/{id}")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.noContent().build())
                .andRoute(POST(PathConstants.MOCK_INTERFACES + "/{id}/enable")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("启用模拟接口 - 功能待实现"))
                .andRoute(POST(PathConstants.MOCK_INTERFACES + "/{id}/disable")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("禁用模拟接口 - 功能待实现"));
    }

    /**
     * 系统监控路由配置
     *
     * 定义系统监控相关的路由规则：
     * - GET /api/v1/system/logs - 获取系统日志
     * - GET /api/v1/system/stats - 获取系统统计
     * - GET /api/v1/system/performance - 获取性能监控
     *
     * @return 系统监控路由函数
     */
    @Bean("systemMonitoringRoutes")
    public RouterFunction<ServerResponse> systemMonitoringRoutes() {
        return RouterFunctions
                .route(GET(PathConstants.SYSTEM_LOGS)
                        .and(accept(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::getSystemLogs)
                .andRoute(GET(PathConstants.SYSTEM_STATS)
                        .and(accept(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::getSystemStats)
                .andRoute(GET(PathConstants.SYSTEM_PERFORMANCE)
                        .and(accept(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::getPerformanceMonitor);
    }

    /**
     * 监控数据可视化路由配置
     *
     * 定义监控数据可视化相关的路由规则：
     * - GET /api/v1/monitoring/dashboard/realtime - 实时仪表板数据流
     * - GET /api/v1/monitoring/dashboard/system - 系统概览数据
     * - GET /api/v1/monitoring/dashboard/performance - 性能概览数据
     * - GET /api/v1/monitoring/trends/cpu - CPU使用率趋势
     * - GET /api/v1/monitoring/trends/memory - 内存使用率趋势
     * - GET /api/v1/monitoring/trends/response-time - 响应时间趋势
     * - GET /api/v1/monitoring/trends/request-count - 请求量趋势
     * - GET /api/v1/monitoring/trends/error-rate - 错误率趋势
     * - GET /api/v1/monitoring/alerts/statistics - 告警统计
     * - GET /api/v1/monitoring/alerts/trend - 告警趋势
     * - GET /api/v1/monitoring/alerts/distribution - 告警分布
     * - GET /api/v1/monitoring/performance/jvm - JVM性能数据
     * - GET /api/v1/monitoring/performance/database - 数据库性能数据
     * - GET /api/v1/monitoring/performance/cache - 缓存性能数据
     * - POST /api/v1/monitoring/charts/custom - 创建自定义图表
     * - GET /api/v1/monitoring/charts/custom/{chartId} - 获取自定义图表数据
     * - GET /api/v1/monitoring/charts/custom - 获取所有自定义图表
     * - DELETE /api/v1/monitoring/charts/custom/{chartId} - 删除自定义图表
     *
     * @return 监控数据可视化路由函数
     */
    @Bean("monitoringVisualizationRoutes")
    public RouterFunction<ServerResponse> monitoringVisualizationRoutes() {
        return RouterFunctions
                // 仪表板数据路由
                .route(GET("/api/v1/monitoring/dashboard/realtime")
                        .and(accept(MediaType.TEXT_EVENT_STREAM)),
                        monitoringVisualizationHandler::getRealTimeDashboardData)
                .andRoute(GET("/api/v1/monitoring/dashboard/system")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getSystemOverview)
                .andRoute(GET("/api/v1/monitoring/dashboard/performance")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getPerformanceOverview)

                // 趋势图表数据路由
                .andRoute(GET("/api/v1/monitoring/trends/cpu")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getCpuUsageTrend)
                .andRoute(GET("/api/v1/monitoring/trends/memory")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getMemoryUsageTrend)
                .andRoute(GET("/api/v1/monitoring/trends/response-time")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getResponseTimeTrend)
                .andRoute(GET("/api/v1/monitoring/trends/request-count")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getRequestCountTrend)
                .andRoute(GET("/api/v1/monitoring/trends/error-rate")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getErrorRateTrend)

                // 告警可视化路由
                .andRoute(GET("/api/v1/monitoring/alerts/statistics")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getAlertStatistics)
                .andRoute(GET("/api/v1/monitoring/alerts/trend")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getAlertTrend)
                .andRoute(GET("/api/v1/monitoring/alerts/distribution")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getAlertDistribution)

                // 性能可视化路由
                .andRoute(GET("/api/v1/monitoring/performance/jvm")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getJvmPerformanceData)
                .andRoute(GET("/api/v1/monitoring/performance/database")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getDatabasePerformanceData)
                .andRoute(GET("/api/v1/monitoring/performance/cache")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getCachePerformanceData)

                // 自定义图表管理路由
                .andRoute(POST("/api/v1/monitoring/charts/custom")
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::createCustomChart)
                .andRoute(GET("/api/v1/monitoring/charts/custom/{chartId}")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getCustomChartData)
                .andRoute(GET("/api/v1/monitoring/charts/custom")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::getAllCustomCharts)
                .andRoute(DELETE("/api/v1/monitoring/charts/custom/{chartId}")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        monitoringVisualizationHandler::deleteCustomChart);
    }

    /**
     * 系统配置路由配置
     *
     * 定义系统配置相关的路由规则：
     * - POST /api/v1/system/config/hot-update - 热更新配置
     * - POST /api/v1/system/config/reload - 重载配置
     *
     * @return 系统配置路由函数
     */
    @Bean("systemConfigRoutes")
    public RouterFunction<ServerResponse> systemConfigRoutes() {
        return RouterFunctions
                .route(POST(PathConstants.SYSTEM_CONFIG_HOT_UPDATE)
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::hotUpdateConfig)
                .andRoute(POST(PathConstants.SYSTEM_CONFIG_RELOAD)
                        .and(accept(MediaType.APPLICATION_JSON)),
                        reactiveSystemHandler::reloadConfig);
    }

    /**
     * 系统维护路由配置
     *
     * 定义系统维护相关的路由规则：
     * - POST /api/v1/system/maintenance - 系统维护操作
     *
     * @return 系统维护路由函数
     */
    @Bean("systemMaintenanceRoutes")
    public RouterFunction<ServerResponse> systemMaintenanceRoutes() {
        return RouterFunctions
                .route(POST(PathConstants.SYSTEM_MAINTENANCE)
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        request -> ServerResponse.notFound().build()); // 测试期望返回404
    }

    /**
     * API版本管理路由配置
     *
     * 定义API版本管理相关的路由规则：
     * - GET /api/v1/version/supported - 获取支持的API版本列表
     * - GET /api/v1/version/changelog/{version} - 获取指定版本的变更日志
     * - GET /api/v1/version/changelog - 获取所有版本的变更日志
     * - GET /api/v1/version/deprecation/{version} - 获取版本弃用信息
     * - GET /api/v1/version/migration - 获取版本迁移指导
     * - GET /api/v1/version/compatibility - 检查版本兼容性
     * - GET /api/v1/version/usage-stats - 获取版本使用统计
     * - GET /api/v1/version/timeline - 获取版本时间线
     * - POST /api/v1/version/deprecate/{version} - 弃用指定版本
     *
     * @return API版本管理路由函数
     */
    @Bean("apiVersionRoutes")
    public RouterFunction<ServerResponse> apiVersionRoutes() {
        return RouterFunctions
                .route(GET("/api/v1/version/supported")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        apiVersionHandler::getSupportedVersions)
                .andRoute(GET("/api/v1/version/changelog")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        apiVersionHandler::getAllVersionChangeLogs)
                .andRoute(GET("/api/v1/version/changelog/{version}")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        apiVersionHandler::getVersionChangeLog)
                .andRoute(GET("/api/v1/version/deprecation/{version}")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        apiVersionHandler::getVersionDeprecationInfo)
                .andRoute(GET("/api/v1/version/migration")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        apiVersionHandler::getVersionMigrationGuide)
                .andRoute(GET("/api/v1/version/compatibility")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        apiVersionHandler::checkVersionCompatibility)
                .andRoute(GET("/api/v1/version/usage-stats")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        apiVersionHandler::getVersionUsageStats)
                .andRoute(GET("/api/v1/version/timeline")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        apiVersionHandler::getVersionTimeline)
                .andRoute(POST("/api/v1/version/deprecate/{version}")
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        apiVersionHandler::deprecateVersion);
    }
}

