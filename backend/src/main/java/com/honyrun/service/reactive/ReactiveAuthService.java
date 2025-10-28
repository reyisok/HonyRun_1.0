package com.honyrun.service.reactive;

import com.honyrun.model.dto.request.AuthRequest;
import com.honyrun.model.dto.response.AuthResponse;
import com.honyrun.model.entity.business.User;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式认证服务接口 - 简化权限模型
 * 【权限架构】：采用用户-权限直接映射模型，去除角色中间层
 * 【用户类型标准化】：严格使用SYSTEM_USER、NORMAL_USER、GUEST，禁止role概念
 * 【项目规则56】：本项目任何地方禁止出现role相关的代码、逻辑、目录、包
 * 【项目规则58】：用户类型必须使用"SYSTEM_USER"、"NORMAL_USER"、"GUEST"，不得出现任何其他简化版本
 *
 * 定义认证相关的业务方法，包括用户认证、令牌管理、权限验证等功能
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理
 * 提供完整的认证业务流程管理和安全控制功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 10:30:00
 * @modified 2025-01-15 当前时间
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveAuthService {

    // ==================== 用户认证操作 ====================

    /**
     * 用户登录认证
     *
     * @param authRequest 登录请求对象，包含用户名和密码
     * @param exchange 服务器交换对象，用于获取请求信息
     * @return 认证响应对象，包含令牌和用户信息
     */
    Mono<AuthResponse> login(AuthRequest authRequest, ServerWebExchange exchange);

    /**
     * 用户登出
     *
     * @param exchange 服务器交换对象，用于获取令牌信息
     * @return 登出操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> logout(ServerWebExchange exchange);

    /**
     * 刷新访问令牌
     *
     * @param exchange 服务器交换对象，用于获取刷新令牌
     * @return 新的认证响应对象，包含新的访问令牌
     */
    Mono<AuthResponse> refreshToken(ServerWebExchange exchange);

    /**
     * 获取当前用户信息
     *
     * @param exchange 服务器交换对象，用于获取用户令牌
     * @return 当前用户信息
     */
    Mono<AuthResponse.UserInfo> getCurrentUser(ServerWebExchange exchange);

    // ==================== 用户验证操作 ====================

    /**
     * 验证登录请求参数
     *
     * @param authRequest 登录请求对象
     * @return 验证结果，验证通过返回空Mono，验证失败抛出异常
     */
    Mono<Void> validateAuthRequest(AuthRequest authRequest);

    /**
     * 用户身份认证
     *
     * @param authRequest 登录请求对象，包含用户名和密码
     * @return 认证成功的用户对象
     */
    Mono<User> authenticateUser(AuthRequest authRequest);

    /**
     * 验证用户密码
     *
     * @param user 用户对象
     * @param rawPassword 原始密码
     * @return 验证结果，密码正确返回true，错误返回false
     */
    Mono<Boolean> validatePassword(User user, String rawPassword);

    // ==================== 令牌管理操作 ====================

    /**
     * 生成认证响应
     *
     * @param user 用户对象
     * @param authRequest 登录请求对象
     * @param exchange 服务器交换对象
     * @return 认证响应对象，包含访问令牌和刷新令牌
     */
    Mono<AuthResponse> generateAuthResponse(User user, AuthRequest authRequest, ServerWebExchange exchange);

    /**
     * 生成刷新认证响应
     *
     * @param user 用户对象
     * @param oldToken 旧的刷新令牌
     * @param exchange 服务器交换对象
     * @return 新的认证响应对象
     */
    Mono<AuthResponse> generateRefreshAuthResponse(User user, String oldToken, ServerWebExchange exchange);

    /**
     * 从请求中提取令牌
     *
     * @param exchange 服务器交换对象
     * @return 提取的令牌字符串
     */
    Mono<String> extractTokenFromRequest(ServerWebExchange exchange);

    /**
     * 将令牌加入黑名单
     *
     * @param token 要加入黑名单的令牌
     * @return 操作结果，成功返回true，失败返回false
     */
    Mono<Boolean> blacklistToken(String token);

    /**
     * 验证令牌有效性
     *
     * @param token 要验证的令牌
     * @return 验证结果，有效返回true，无效返回false
     */
    Mono<Boolean> validateToken(String token);

    // ==================== 权限管理操作 ====================

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 用户权限代码列表
     */
    Flux<String> getUserPermissions(Long userId);

    /**
     * 验证用户是否具有指定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return 验证结果，有权限返回true，无权限返回false
     */
    Mono<Boolean> hasPermission(Long userId, String permissionCode);

    // ==================== 活动记录操作 ====================

    /**
     * 记录用户活动
     *
     * @param authResponse 认证响应对象
     * @param exchange 服务器交换对象
     * @return 记录操作结果
     */
    Mono<Void> recordUserActivity(AuthResponse authResponse, ServerWebExchange exchange);

    /**
     * 记录用户登录活动
     *
     * @param userId 用户ID
     * @param clientIp 客户端IP地址
     * @param userAgent 用户代理信息
     * @param deviceId 设备ID
     * @return 记录操作结果
     */
    Mono<Void> recordLoginActivity(Long userId, String clientIp, String userAgent, String deviceId);

    /**
     * 记录用户登出活动
     *
     * @param userId 用户ID
     * @param clientIp 客户端IP地址
     * @return 记录操作结果
     */
    Mono<Void> recordLogoutActivity(Long userId, String clientIp);

    // ==================== 工具方法 ====================

    /**
     * 获取设备ID
     *
     * @param authRequest 登录请求对象
     * @return 设备ID字符串
     */
    String getDeviceId(AuthRequest authRequest);

    /**
     * 从交换对象中获取客户端IP地址
     *
     * @param exchange 服务器交换对象
     * @return 客户端IP地址
     */
    String getClientIpFromExchange(ServerWebExchange exchange);

    /**
     * 从交换对象中获取用户代理信息
     *
     * @param exchange 服务器交换对象
     * @return 用户代理信息
     */
    String getUserAgentFromExchange(ServerWebExchange exchange);

    /**
     * 创建用户信息对象
     *
     * @param user 用户实体对象
     * @return 用户信息DTO对象
     */
    AuthResponse.UserInfo createUserInfo(User user);

    /**
     * 创建错误认证响应
     *
     * @param errorMessage 错误消息
     * @return 错误认证响应对象
     */
    AuthResponse createErrorAuthResponse(String errorMessage);

    /**
     * 手机号码脱敏处理
     *
     * @param phone 原始手机号码
     * @return 脱敏后的手机号码
     */
    String maskPhone(String phone);
}

