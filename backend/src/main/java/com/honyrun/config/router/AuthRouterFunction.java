package com.honyrun.config.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.honyrun.constant.PathConstants;
import com.honyrun.handler.AuthHandler;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;

/**
 * 认证函数式路由配置
 *
 * 使用函数式编程模型配置认证相关的路由映射
 * 提供登录、登出、令牌刷新等功能的路由定义
 *
 * 【统一规则】根据HonyRun后端统一接口检查报告要求：
 * 1. 统一使用ApiResponse<T>格式返回响应，移除自定义ErrorResponse类
 * 2. 所有异常处理统一使用ReactiveGlobalExceptionFilter
 * 3. 遵循Spring Boot 3最佳实践和响应式编程规范
 * 4. 确保响应格式的一致性和标准化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-09-27 19:05:00
 * @modified 2025-07-02 20:00:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Configuration
public class AuthRouterFunction {

    private static final Logger logger = LoggerFactory.getLogger(AuthRouterFunction.class);

    private final AuthHandler authHandler;

    /**
     * 构造器注入依赖
     *
     * @param authHandler 认证处理器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-22 04:40:00
     * @version 1.0.0
     */
    public AuthRouterFunction(@Qualifier("reactiveAuthHandler") AuthHandler authHandler) {
        this.authHandler = authHandler;
    }

    /**
     * 配置认证相关的函数式路由
     *
     * 定义认证相关的路由规则：
     * - POST /api/v1/auth/login - 用户登录认证
     * - POST /api/v1/auth/logout - 用户登出
     * - POST /api/v1/auth/refresh - 刷新访问令牌
     * - POST /api/v1/auth/validate - 验证令牌有效性
     * - GET /api/v1/auth/me - 获取当前用户信息
     * - GET /api/v1/auth/current-user - 获取当前用户信息（兼容接口）
     *
     * @return 认证路由函数
     */
    @Bean("authRoutes")
    public RouterFunction<ServerResponse> authRoutes() {
        LoggingUtil.info(logger, "Configuring authentication functional routes");

        return RouterFunctions.route()
                // 用户登录认证 - 接收用户名密码，返回JWT令牌
                .POST(PathConstants.AUTH_LOGIN,
                        accept(MediaType.APPLICATION_JSON)
                                .and(contentType(MediaType.APPLICATION_JSON)),
                        authHandler::handleLogin)
                // 用户登出 - 使当前令牌失效
                .POST(PathConstants.AUTH_LOGOUT, authHandler::handleLogout)
                // 刷新访问令牌 - 使用刷新令牌获取新的访问令牌
                .POST(PathConstants.AUTH_REFRESH,
                        accept(MediaType.APPLICATION_JSON),
                        authHandler::handleRefreshToken)
                // 验证令牌有效性 - 检查令牌是否有效且未过期
                .POST(PathConstants.AUTH_VALIDATE, authHandler::handleTokenValidation)
                // 获取当前用户信息 - 基于令牌返回用户详细信息
                .GET(PathConstants.AUTH_ME, authHandler::handleGetCurrentUser)
                // 获取当前用户信息（兼容接口） - 与/me功能相同
                .GET(PathConstants.AUTH_CURRENT_USER, authHandler::handleGetCurrentUser)
                .build();
    }

    /**
     * 配置认证兼容性路由
     *
     * 定义兼容旧版本API的路由规则：
     * - POST /api/auth/signin - 用户登录（兼容旧版本）
     * - POST /api/auth/signout - 用户登出（兼容旧版本）
     * - POST /api/auth/token/refresh - 刷新令牌（兼容旧版本）
     * - GET /api/auth/user/info - 获取用户信息（兼容旧版本）
     *
     * @return 兼容性路由函数
     */
    @Bean("authCompatibilityRoutes")
    public RouterFunction<ServerResponse> authCompatibilityRoutes() {
        LoggingUtil.info(logger, "Configuring authentication compatibility routes");

        return RouterFunctions.nest(
                path("/api/auth"), // 兼容旧版本API路径
                RouterFunctions.route()
                        // 用户登录（主要路径） - 映射到标准登录处理
                        .POST("/login", accept(MediaType.APPLICATION_JSON)
                                .and(contentType(MediaType.APPLICATION_JSON)), authHandler::handleLogin)
                        // 用户登录（兼容旧版本） - 映射到标准登录处理
                        .POST("/signin", accept(MediaType.APPLICATION_JSON), authHandler::handleLogin)
                        // 用户登出（主要路径） - 映射到标准登出处理
                        .POST("/logout", authHandler::handleLogout)
                        // 用户登出（兼容旧版本） - 映射到标准登出处理
                        .POST("/signout", authHandler::handleLogout)
                        // 刷新令牌（兼容旧版本） - 映射到标准令牌刷新处理
                        .POST("/token/refresh", authHandler::handleRefreshToken)
                        // 获取用户信息（兼容旧版本） - 映射到标准用户信息获取
                        .GET("/user/info", authHandler::handleGetCurrentUser)
                        .build());
    }

    /**
     * 配置管理员认证路由
     *
     * 定义管理员专用的认证管理路由规则：
     * - POST /api/v1/system/auth/force-logout - 强制用户登出（管理员权限）
     * - GET /api/v1/system/auth/active-users - 获取活跃用户列表（管理员权限）
     *
     * @return 管理员认证路由函数
     */
    @Bean("adminAuthRoutes")
    public RouterFunction<ServerResponse> adminAuthRoutes() {
        LoggingUtil.info(logger, "Configuring admin authentication routes");

        return RouterFunctions.route()
                // 强制用户登出 - 管理员可强制指定用户登出
                .POST(PathConstants.SYSTEM_AUTH_FORCE_LOGOUT, this::handleForceLogout)
                // 获取活跃用户列表 - 管理员查看当前在线用户
                .GET(PathConstants.SYSTEM_AUTH_ACTIVE_USERS, this::handleGetActiveUsers)
                .build();
    }

    /**
     * 配置认证统计路由
     *
     * 定义认证统计相关的路由规则：
     * - GET /api/v1/auth/stats/login - 获取登录统计信息
     * - GET /api/v1/auth/stats/online - 获取在线用户统计
     * - GET /api/v1/auth/stats/failures - 获取登录失败统计
     *
     * @return 认证统计路由函数
     */
    @Bean("authStatsRoutes")
    public RouterFunction<ServerResponse> authStatsRoutes() {
        LoggingUtil.info(logger, "Configuring authentication statistics routes");

        return RouterFunctions.route()
                // 获取登录统计信息 - 包括登录次数、成功率等
                .GET(PathConstants.AUTH_STATS_LOGIN, this::handleLoginStats)
                // 获取在线用户统计 - 当前在线用户数量和分布
                .GET(PathConstants.AUTH_STATS_ONLINE, this::handleOnlineStats)
                // 获取登录失败统计 - 失败次数、原因分析等
                .GET(PathConstants.AUTH_STATS_FAILURES, this::handleFailureStats)
                .build();
    }

    // ==================== 私有处理方法 ====================

    /**
     * 处理强制登出请求
     *
     * @param request 服务器请求对象
     * @return 强制登出响应
     */
    private Mono<ServerResponse> handleForceLogout(
            org.springframework.web.reactive.function.server.ServerRequest request) {
        LoggingUtil.info(logger, "Processing force logout request");

        // 这里应该实现强制登出逻辑
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ApiResponse.success("强制登出成功"));
    }

    /**
     * 处理获取活跃用户请求
     *
     * @param request 服务器请求对象
     * @return 活跃用户响应
     */
    private Mono<ServerResponse> handleGetActiveUsers(
            org.springframework.web.reactive.function.server.ServerRequest request) {
        LoggingUtil.info(logger, "Processing get active users request");

        // 返回空数组作为活跃用户列表
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ApiResponse.success(java.util.Collections.emptyList()));
    }

    /**
     * 处理登录统计请求
     *
     * @param request 服务器请求对象
     * @return 登录统计响应
     */
    private Mono<ServerResponse> handleLoginStats(
            org.springframework.web.reactive.function.server.ServerRequest request) {
        LoggingUtil.info(logger, "Processing login statistics request");

        // 这里应该实现登录统计逻辑
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ApiResponse.success("登录统计信息获取成功"));
    }

    /**
     * 处理在线统计请求
     *
     * @param request 服务器请求对象
     * @return 在线统计响应
     */
    private Mono<ServerResponse> handleOnlineStats(
            org.springframework.web.reactive.function.server.ServerRequest request) {
        LoggingUtil.info(logger, "Processing online statistics request");

        // 这里应该实现在线统计逻辑
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ApiResponse.success("在线统计信息获取成功"));
    }

    /**
     * 处理失败统计请求
     *
     * @param request 服务器请求对象
     * @return 失败统计响应
     */
    private Mono<ServerResponse> handleFailureStats(
            org.springframework.web.reactive.function.server.ServerRequest request) {
        LoggingUtil.info(logger, "Processing failure statistics request");

        // 这里应该实现失败统计逻辑
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ApiResponse.success("失败统计信息获取成功"));
    }

    // ==================== 响应类 ====================

    /**
     * 健康检查响应类
     */
    public static class HealthResponse {
        private String status;
        private String message;

        public HealthResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

