package com.honyrun.config.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.honyrun.filter.reactive.ReactiveGlobalExceptionFilter;
import com.honyrun.handler.UserHandler;
import com.honyrun.handler.reactive.ReactiveSystemHandler;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.service.monitoring.RouteLoggingService;
import com.honyrun.util.ErrorDetailsUtil;
import com.honyrun.util.LoggingUtil;

/**
 * 统一路由配置管理
 *
 * 按照Spring Boot 3最佳实践，统一管理所有路由配置：
 * 1. 优先使用函数式路由（RouterFunction）
 * 2. 避免注解式路由与函数式路由混用
 * 3. 统一路由优先级和冲突管理
 * 4. 提供清晰的路由层次结构
 *
 * 【统一规则】根据HonyRun后端统一接口检查报告要求：
 * 1. 完全迁移到函数式路由，禁用所有@RestController注解
 * 2. 统一使用ApiResponse<T>格式返回响应
 * 3. 所有异常处理统一使用ReactiveGlobalExceptionFilter
 * 4. 遵循Spring Boot 3最佳实践和响应式编程规范
 * 5. 确保路由配置的一致性和标准化
 *
 * 路由优先级（从高到低）：
 * 1. 认证相关路由 - 最高优先级，处理登录、登出、令牌验证
 * 2. 系统管理路由 - 高优先级，处理系统配置、监控、日志
 * 3. 用户管理路由 - 中优先级，处理用户CRUD、权限管理
 * 4. 业务功能路由 - 普通优先级，处理核心业务逻辑
 * 5. 静态资源路由 - 最低优先级，处理静态文件和错误页面
 *
 * 最佳实践说明：
 * - 由Spring Boot自动聚合所有RouterFunction<ServerResponse> Bean，无需@Primary
 * - 按优先级顺序组合路由，避免路由冲突
 * - 统一异常处理和响应格式
 * - 支持响应式编程模型
 * - 提供清晰的路由层次结构和文档
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-02 18:30:00
 * @modified 2025-07-02 20:00:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Configuration
public class UnifiedRouterConfig {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedRouterConfig.class);

    private final AuthRouterFunction authRouterFunction;
    private final SystemRouterFunction systemRouterFunction;
    private final UserRouterFunction userRouterFunction;
    private final ConfigurationComplianceRouterFunction configurationComplianceRouterFunction;
    private final UserHandler userHandler;
    private final ReactiveSystemHandler reactiveSystemHandler;
    private final RouteRegistry routeRegistry;
    private final Environment environment;
    private final ApplicationContext applicationContext;

    /**
     * 统一路由配置构造函数
     *
     * @param authRouterFunction    认证路由函数
     * @param systemRouterFunction  系统路由函数
     * @param userRouterFunction    用户路由函数
     * @param configurationComplianceRouterFunction 配置合规性检查路由函数
     * @param userHandler           用户处理器
     * @param reactiveSystemHandler 响应式系统处理器
     * @param routeRegistry         路由注册表
     * @param environment           Spring环境配置
     * @param applicationContext    Spring应用上下文
     */
    public UnifiedRouterConfig(
            AuthRouterFunction authRouterFunction,
            SystemRouterFunction systemRouterFunction,
            UserRouterFunction userRouterFunction,
            ConfigurationComplianceRouterFunction configurationComplianceRouterFunction,
            UserHandler userHandler,
            ReactiveSystemHandler reactiveSystemHandler,
            RouteRegistry routeRegistry,
            Environment environment,
            ApplicationContext applicationContext) {
        this.authRouterFunction = authRouterFunction;
        this.systemRouterFunction = systemRouterFunction;
        this.userRouterFunction = userRouterFunction;
        this.configurationComplianceRouterFunction = configurationComplianceRouterFunction;
        this.userHandler = userHandler;
        this.reactiveSystemHandler = reactiveSystemHandler;
        this.routeRegistry = routeRegistry;
        this.environment = environment;
        this.applicationContext = applicationContext;
    }

    // 注意：ReactiveUserHandler已弃用，功能已迁移至UserHandler
    // 已移除ReactiveUserHandler的注入，使用UserHandler替代

    /**
     * 主路由配置 - 统一管理所有路由
     *
     * 【最佳实践】由Spring Boot自动聚合所有 RouterFunction<ServerResponse> Bean，无需 @Primary
     * 按照优先级顺序组合所有路由，避免路由冲突
     * 统一异常处理和响应格式，确保接口一致性
     *
     * @return 统一的路由函数，包含所有业务路由和系统路由
     */
    @Bean("unifiedRouterFunction")
    public RouterFunction<ServerResponse> unifiedRouterFunction() {
        LoggingUtil.info(logger, "初始化统一路由配置");

        try {
            RouterFunction<ServerResponse> combinedRoutes = RouterFunctions.route()
                    // 1. 认证路由 - 最高优先级
                    // 处理用户登录、登出、令牌刷新等认证相关操作
                    .add(getAuthRoutes())

                    // 2. 系统管理路由 - 高优先级
                    // 处理系统配置、监控、日志管理等系统级操作
                    .add(getSystemRoutes())

                    // 3. 配置合规性检查路由 - 高优先级
                    // 处理配置一致性验证和合规性检查报告
                    .add(configurationComplianceRouterFunction.configurationComplianceRoutes())

                    // 4. 用户管理路由 - 中优先级
                    // 处理用户CRUD、权限管理、用户状态管理等操作
                    .add(getUserRoutes())

                    // 5. 业务功能路由 - 普通优先级
                    // 处理核心业务逻辑，如核验业务、业务功能等
                    .add(getVerificationRoutes())

                    // 6. 静态资源和错误处理路由 - 最低优先级
                    // 处理静态文件访问和全局错误处理
                    .add(getStaticAndErrorRoutes())

                    // 全局过滤器 - 统一处理请求日志、响应格式等
                    .filter((request, next) -> {
                        LoggingUtil.debug(logger, "处理请求: {} {}",
                                request.method(), request.uri());
                        return next.handle(request);
                    })

                    .build();

            // 将统一路由注册到路由注册表，减少与真实路由配置的漂移
            try {
                routeRegistry.clearRoutes();
                routeRegistry.initializeRoutes();
                LoggingUtil.info(logger, "路由注册表已同步统一路由配置");
            } catch (Exception e) {
                LoggingUtil.warn(logger, "同步路由注册表失败: {}", e.getMessage());
            }

            LoggingUtil.info(logger, "统一路由配置初始化成功");
            return combinedRoutes;

        } catch (Exception e) {
            LoggingUtil.error(logger, "统一路由配置初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("统一路由配置初始化失败", e);
        }
    }

    /**
     * 认证相关路由
     * 包括登录、登出、令牌刷新等
     */
    private RouterFunction<ServerResponse> getAuthRoutes() {
        LoggingUtil.debug(logger, "加载认证路由");

        return RouterFunctions.route()
                // 使用 AuthRouterFunction 中定义的功能路由
                .add(authRouterFunction.authRoutes())
                .add(authRouterFunction.authCompatibilityRoutes())
                .add(authRouterFunction.adminAuthRoutes())
                .add(authRouterFunction.authStatsRoutes())
                .build();
    }

    /**
     * 系统管理路由
     * 包括系统信息、健康检查、配置管理等
     */
    private RouterFunction<ServerResponse> getSystemRoutes() {
        LoggingUtil.debug(logger, "加载系统管理路由");

        return RouterFunctions.route()
                // 系统信息 - 公开访问
                .GET("/api/v1/system/info",
                        accept(MediaType.APPLICATION_JSON),
                        reactiveSystemHandler::getSystemInfo)

                // 系统状态 - 需要认证
                .GET("/api/v1/system/status",
                        accept(MediaType.APPLICATION_JSON),
                        reactiveSystemHandler::getSystemStatus)

                // 【优化】移除重复的系统配置路由定义，统一使用SystemRouterFunction管理
                // 原有的 /api/v1/system/config 相关路由已迁移至 SystemRouterFunction.systemConfigRoutes()
                // 避免路由重复定义和冲突

                // 添加SystemRouterFunction中的路由方法
                .add(systemRouterFunction.systemHealthRoutes())
                .add(systemRouterFunction.systemSettingsRoutes())
                .add(systemRouterFunction.systemVersionRoutes())
                .add(systemRouterFunction.systemImageRoutes())
                .add(systemRouterFunction.systemMockRoutes())
                .add(systemRouterFunction.systemMonitoringRoutes())
                .add(systemRouterFunction.systemConfigRoutes())
                .add(systemRouterFunction.systemMaintenanceRoutes())

                .build();
    }

    /**
     * 用户管理路由
     * 包括用户CRUD、权限管理等
     *
     * 【路由优先级修复】确保具体路径在通用路径之前定义，避免路由冲突：
     * 1. 先定义需要权限验证的管理路由 (/api/v1/users)
     * 2. 后定义个人资料相关路由 (/api/v1/user/profile)
     * 3. 避免路径匹配冲突和权限绕过问题
     */
    private RouterFunction<ServerResponse> getUserRoutes() {
        LoggingUtil.debug(logger, "加载用户管理路由");

        return RouterFunctions.route()
                // 【优先级1】用户管理路由 - 需要系统用户权限
                // 必须在个人资料路由之前定义，避免路由冲突
                .add(userRouterFunction.userRoutes())

                // 【优先级2】个人资料路由 - 普通用户可访问
                // 获取用户资料
                .GET("/api/v1/user/profile",
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::getCurrentUser)

                // 更新用户资料
                .PUT("/api/v1/user/profile",
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::updateProfile)

                // 修改密码
                .PUT("/api/v1/user/password",
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::changePassword)

                .build();
    }

    /**
     * 获取验证相关路由
     *
     * @return 验证路由函数
     */
    private RouterFunction<ServerResponse> getVerificationRoutes() {
        try {
            LoggingUtil.info(logger, "配置验证路由");

            // 根据当前激活的profile选择对应的验证路由Bean
            String[] activeProfiles = environment.getActiveProfiles();
            String verificationBeanName = "devVerificationRoutes"; // 默认使用dev

            for (String profile : activeProfiles) {
                if ("test".equals(profile)) {
                    verificationBeanName = "testVerificationRoutes";
                    break;
                } else if ("prod".equals(profile)) {
                    verificationBeanName = "prodVerificationRoutes";
                    break;
                }
            }

            @SuppressWarnings("unchecked")
            RouterFunction<ServerResponse> verificationRoutes = (RouterFunction<ServerResponse>) applicationContext
                    .getBean(verificationBeanName, RouterFunction.class);

            return verificationRoutes;
        } catch (Exception e) {
            LoggingUtil.error(logger, "验证路由配置失败", e);
            throw new RuntimeException("验证路由配置失败", e);
        }
    }

    /**
     * 静态资源和错误处理路由
     */
    private RouterFunction<ServerResponse> getStaticAndErrorRoutes() {
        LoggingUtil.debug(logger, "加载静态资源和错误处理路由");

        return RouterFunctions.route()
                // 根路径重定向
                .GET("/", request -> ServerResponse.temporaryRedirect(
                        java.net.URI.create("/api-docs")).build())

                // API文档路由
                .GET("/api-docs",
                        request -> ServerResponse.ok()
                                .header("Content-Type", MediaType.TEXT_HTML_VALUE)
                                .bodyValue(
                                        "<!DOCTYPE html><html><head><title>HonyRun API Documentation</title></head><body><h1>HonyRun API Documentation</h1><p>API documentation is available.</p></body></html>"))

                // 错误处理路由
                .GET("/error/404",
                        request -> ServerResponse.notFound()
                                .build())

                .GET("/error/500",
                        request -> ServerResponse.status(500)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("500", "服务器内部错误")))

                .GET("/error/403",
                        request -> ServerResponse.status(403)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("403", "访问被拒绝")))

                .GET("/error/401",
                        request -> ServerResponse.status(401)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("401", "未授权访问")))

                .build();
    }

    // ========================================
    // WebFilter配置 - 符合函数式路由最佳实践
    // ========================================

    /**
     * 路由监控过滤器
     *
     * 集成RouteLoggingService，提供详细的路由性能监控和统计。
     * 记录路由访问统计、性能指标和错误信息。
     * 设置较低优先级，确保在异常处理过滤器之后执行。
     */
    @Bean
    @Order(1)
    public WebFilter routeMonitoringFilter(RouteLoggingService routeLoggingService) {
        return (exchange, chain) -> {
            String requestPath = exchange.getRequest().getURI().getPath();
            String method = exchange.getRequest().getMethod().name();
            String clientIp = getClientIp(exchange.getRequest());
            String routeKey = method + " " + requestPath;

            LoggingUtil.info(logger, "请求开始 - 方法: {}, 路径: {}, 客户端IP: {}", method, requestPath, clientIp);

            // 记录请求开始并获取Timer.Sample
            return routeLoggingService.recordRequestStart(routeKey, method)
                    .flatMap(sample -> {
                        long startTime = System.currentTimeMillis();
                        return chain.filter(exchange)
                                .doOnSuccess(result -> {
                                    long duration = System.currentTimeMillis() - startTime;
                                    int statusCode = exchange.getResponse().getStatusCode() != null 
                                        ? exchange.getResponse().getStatusCode().value() : 200;
                                    
                                    // 记录请求完成
                                    routeLoggingService.recordRequestComplete(routeKey, sample, statusCode, 
                                            Duration.ofMillis(duration)).subscribe();
                                    
                                    LoggingUtil.info(logger, "请求成功 - 方法: {}, 路径: {}, 耗时: {}ms, 状态码: {}",
                                            method, requestPath, duration, statusCode);
                                })
                                .doOnError(error -> {
                                    // 记录请求错误
                                    routeLoggingService.recordRequestError(routeKey, sample, error).subscribe();
                                    
                                    LoggingUtil.error(logger, "请求失败 - 方法: {}, 路径: {}, 错误: {}",
                                            method, requestPath, error.getMessage());
                                })
                                .doFinally(signalType -> {
                                    long duration = System.currentTimeMillis() - startTime;
                                    LoggingUtil.debug(logger, "请求结束 - 方法: {}, 路径: {}, 耗时: {}ms, 信号类型: {}",
                                            method, requestPath, duration, signalType);
                                });
                    });
        };
    }

    /**
     * 注册全局异常处理过滤器
     *
     * 符合函数式路由配置的异常处理机制，替代@RestControllerAdvice。
     * 设置最高优先级确保能捕获所有异常。
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ReactiveGlobalExceptionFilter reactiveGlobalExceptionFilter(ErrorDetailsUtil errorDetailsUtil,
            ObjectMapper objectMapper) {
        return new ReactiveGlobalExceptionFilter(errorDetailsUtil, objectMapper);
    }

    /**
     * 获取客户端真实IP地址
     *
     * 支持代理和负载均衡环境下的IP获取，按优先级检查各种头信息。
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        String remoteAddr = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        return remoteAddr;
    }
}
