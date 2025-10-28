package com.honyrun.service.reactive;

import reactor.core.publisher.Mono;

/**
 * 响应式审计日志服务接口
 *
 * 该服务负责记录系统中的各种审计事件，特别是认证相关的事件。
 * 主要功能包括：
 * 1. 记录认证成功事件
 * 2. 记录认证失败事件
 * 3. 记录登出事件
 * 4. 记录权限变更事件
 * 5. 记录敏感操作事件
 *
 * 设计原则：
 * - 响应式编程支持
 * - 异步日志记录
 * - 结构化日志格式
 * - 高性能日志处理
 * - 安全事件追踪
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-13 16:57:03
 * @modified 2025-01-13 16:57:03
 * @version 1.0.0
 */
public interface ReactiveAuditLogService {

    /**
     * 记录认证成功事件
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param ipAddress 客户端IP地址
     * @param userAgent 用户代理信息
     * @param loginSource 登录来源
     * @return 记录结果
     */
    Mono<Void> logAuthenticationSuccess(String username, Long userId, String ipAddress, String userAgent, String loginSource);

    /**
     * 记录认证失败事件
     *
     * @param username 用户名（可能为空）
     * @param ipAddress 客户端IP地址
     * @param userAgent 用户代理信息
     * @param failureReason 失败原因
     * @param loginSource 登录来源
     * @return 记录结果
     */
    Mono<Void> logAuthenticationFailure(String username, String ipAddress, String userAgent, String failureReason, String loginSource);

    /**
     * 记录登出事件
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param ipAddress 客户端IP地址
     * @param userAgent 用户代理信息
     * @param logoutReason 登出原因（主动登出、令牌过期等）
     * @return 记录结果
     */
    Mono<Void> logLogout(String username, Long userId, String ipAddress, String userAgent, String logoutReason);

    /**
     * 记录权限变更事件
     *
     * @param targetUsername 目标用户名
     * @param targetUserId 目标用户ID
     * @param operatorUsername 操作者用户名
     * @param operatorUserId 操作者用户ID
     * @param permissionChange 权限变更详情
     * @param ipAddress 客户端IP地址
     * @return 记录结果
     */
    Mono<Void> logPermissionChange(String targetUsername, Long targetUserId, String operatorUsername, Long operatorUserId, String permissionChange, String ipAddress);

    /**
     * 记录敏感操作事件
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param operation 操作类型
     * @param operationDetails 操作详情
     * @param ipAddress 客户端IP地址
     * @param userAgent 用户代理信息
     * @return 记录结果
     */
    Mono<Void> logSensitiveOperation(String username, Long userId, String operation, String operationDetails, String ipAddress, String userAgent);

    /**
     * 记录账户锁定事件
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param lockReason 锁定原因
     * @param ipAddress 客户端IP地址
     * @param operatorUsername 操作者用户名（系统自动锁定时为空）
     * @return 记录结果
     */
    Mono<Void> logAccountLocked(String username, Long userId, String lockReason, String ipAddress, String operatorUsername);

    /**
     * 记录账户解锁事件
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param unlockReason 解锁原因
     * @param ipAddress 客户端IP地址
     * @param operatorUsername 操作者用户名
     * @return 记录结果
     */
    Mono<Void> logAccountUnlocked(String username, Long userId, String unlockReason, String ipAddress, String operatorUsername);

    /**
     * 记录密码变更事件
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param changeType 变更类型（用户主动修改、管理员重置等）
     * @param ipAddress 客户端IP地址
     * @param userAgent 用户代理信息
     * @return 记录结果
     */
    Mono<Void> logPasswordChange(String username, Long userId, String changeType, String ipAddress, String userAgent);

    /**
     * 记录令牌刷新事件
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param ipAddress 客户端IP地址
     * @param userAgent 用户代理信息
     * @param refreshReason 刷新原因
     * @return 记录结果
     */
    Mono<Void> logTokenRefresh(String username, Long userId, String ipAddress, String userAgent, String refreshReason);
}

