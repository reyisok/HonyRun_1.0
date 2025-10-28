package com.honyrun.config.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.honyrun.constant.PathConstants;
import com.honyrun.handler.UserHandler;
import com.honyrun.util.LoggingUtil;

/**
 * 用户路由功能配置类 - 简化权限模型
 * 【权限架构】：采用用户-权限直接映射模型，去除角色中间层
 * 【用户类型标准化】：严格使用SYSTEM_USER、NORMAL_USER、GUEST，禁止role概念
 * 【项目规则56】：本项目任何地方禁止出现role相关的代码、逻辑、目录、包
 * 【项目规则58】：用户类型必须使用"SYSTEM_USER"、"NORMAL_USER"、"GUEST"，不得出现任何其他简化版本
 *
 * 配置用户相关的响应式路由，使用函数式编程风格定义路由规则
 * 基于用户类型（UserType）进行权限控制，SYSTEM_USER拥有所有用户管理权限。
 * 所有路由都支持响应式数据流处理，提供高性能的用户管理API
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  20:30:00
 * @modified 2025-01-15 当前时间
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Configuration
public class UserRouterFunction {

    private static final Logger logger = LoggerFactory.getLogger(UserRouterFunction.class);

    private final UserHandler userHandler;

    public UserRouterFunction(UserHandler userHandler) {
        this.userHandler = userHandler;
    }

    /**
     * 用户管理路由配置
     *
     * 配置所有用户管理相关的路由映射，严格按照后端详细规划设计文档实现。
     * 包含用户基础CRUD操作、权限管理、状态管理、活动记录和统计功能。
     *
     * 【权限控制优化】移除路由层面的权限验证，统一使用SecurityConfig进行权限控制，
     * 避免双重权限验证导致的冲突和复杂性。
     *
     * @return 用户路由函数
     */
    @Bean("userRoutes")
    // 约定：新增路由保持"静态在前、动态在后"；注册时先添加无参数或具体前缀路径，
    // 再添加包含变量段的路径，避免诸如 `/statistics` 被误匹配为 `/{id}` 的问题。
    public RouterFunction<ServerResponse> userRoutes() {
        LoggingUtil.info(logger, "配置用户管理路由");

        return route()
                // ==================== 基础路由（无参数） ====================
                .POST(PathConstants.USER_CREATE,
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::createUser)
                .GET(PathConstants.USER_LIST,
                        userHandler::getAllUsers)

                // ==================== 具体路径路由（必须在参数化路径之前） ====================
                .GET(PathConstants.USER_CURRENT,
                        userHandler::getCurrentUser)
                .GET(PathConstants.USER_BASE + "/search",
                        userHandler::searchUsers)
                .GET(PathConstants.USER_BASE + "/statistics",
                        userHandler::getUserStatistics)
                // 统计路由（兼容测试所需的命名）
                .GET(PathConstants.USER_STATS_REGISTRATION,
                        userHandler::getUserRegistrationStats)
                .GET(PathConstants.USER_STATS_TYPE_DISTRIBUTION,
                        userHandler::getUserTypeDistribution)
                .GET(PathConstants.USER_STATS_ACTIVE,
                        userHandler::getActiveUserCount)
                .GET(PathConstants.USER_BASE + "/count",
                        userHandler::countUsers)
                .GET(PathConstants.USER_BASE + "/status",
                        userHandler::getUsersByStatus)
                .GET(PathConstants.USER_BASE + "/batch",
                        userHandler::getBatchUsers)
                .DELETE(PathConstants.USER_BASE + "/batch",
                        userHandler::batchDeleteUsers)
                .POST(PathConstants.USER_BASE + "/batch",
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::createBatchUsers)
                .GET(PathConstants.USER_BASE + "/empty",
                        userHandler::getEmptyUsers)
                .GET(PathConstants.USER_BASE + "/boundary",
                        userHandler::getBoundaryUsers)

                // ==================== 嵌套具体路径路由 ====================
                .GET(PathConstants.USER_BASE + "/exists/username/{username}",
                        userHandler::existsByUsername)
                .GET(PathConstants.USER_BASE + "/exists/email/{email}",
                        userHandler::existsByEmail)
                .GET(PathConstants.USER_BASE + "/username/{username}",
                        userHandler::getUserByUsername)
                .GET(PathConstants.USER_BASE + "/type/{userType}",
                        userHandler::getUsersByType)

                // ==================== 参数化路径路由（必须在最后） ====================
                .GET(PathConstants.USER_DETAIL,
                        userHandler::getUserById)
                .PUT(PathConstants.USER_UPDATE,
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::updateUser)
                .DELETE(PathConstants.USER_DELETE,
                        userHandler::deleteUser)
                // 权限管理：分配、撤销、更新、查询
                .POST(PathConstants.USER_PERMISSIONS,
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::assignPermissions)
                .DELETE(PathConstants.USER_PERMISSIONS,
                        userHandler::revokePermissions)
                .PUT(PathConstants.USER_PERMISSIONS,
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::updateUserPermissions)
                .GET(PathConstants.USER_PERMISSIONS,
                        userHandler::getUserPermissions)
                .PUT(PathConstants.USER_BASE + "/{id}/status",
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::updateUserStatus)
                .GET(PathConstants.USER_BASE + "/{id}/permissions/check",
                        userHandler::hasPermission)
                .GET(PathConstants.USER_BASE + "/{id}/activities",
                        userHandler::getUserActivities)
                // 启用/禁用用户（测试使用显式路径）
                .PUT(PathConstants.USER_BASE + "/{id}/enable",
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::enableUser)
                .PUT(PathConstants.USER_BASE + "/{id}/disable",
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::disableUser)
                // 修改/重置密码（管理员针对指定用户）
                .PUT(PathConstants.USER_BASE + "/{id}/change-password",
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::changeUserPasswordById)
                .PUT(PathConstants.USER_BASE + "/{id}/reset-password",
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::resetUserPassword)

                .build();
    }
}

