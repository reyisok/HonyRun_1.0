package com.honyrun.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.honyrun.constant.SecurityConstants;
import com.honyrun.service.reactive.ReactiveAuditLogService;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;

/**
 * 响应式审计日志服务实现类
 *
 * 该服务实现了系统中各种审计事件的记录功能，特别是认证相关的事件。
 * 主要功能包括：
 * 1. 结构化日志记录
 * 2. 异步日志处理
 * 3. 安全事件追踪
 * 4. 审计日志格式化
 * 5. 高性能日志写入
 *
 * 日志格式：
 * - 使用专用的SECURITY_AUDIT日志记录器
 * - 结构化的JSON格式日志
 * - 包含完整的上下文信息
 * - 支持日志聚合和分析
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-13 16:57:03
 * @modified 2025-01-13 16:57:03
 * @version 1.0.0
 */
@Service
public class ReactiveAuditLogServiceImpl implements ReactiveAuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveAuditLogServiceImpl.class);
    
    // 安全审计专用日志记录器
    private static final Logger securityAuditLogger = LoggerFactory.getLogger("SECURITY_AUDIT");

    @Override
    public Mono<Void> logAuthenticationSuccess(String username, Long userId, String ipAddress, String userAgent, String loginSource) {
        return Mono.fromRunnable(() -> {
            try {
                String auditMessage = String.format(
                    "认证成功 - 用户: %s | 用户ID: %s | IP: %s | User-Agent: %s | 登录来源: %s | 事件类型: %s | 时间戳: %d",
                    username, userId, ipAddress, userAgent, loginSource, SecurityConstants.AUDIT_LOGIN_SUCCESS, System.currentTimeMillis()
                );
                
                LoggingUtil.info(securityAuditLogger, auditMessage);
                LoggingUtil.info(logger, "记录认证成功审计日志: 用户={}, IP={}", username, ipAddress);
                
            } catch (Exception e) {
                LoggingUtil.error(logger, "记录认证成功审计日志失败", e);
            }
        });
    }

    @Override
    public Mono<Void> logAuthenticationFailure(String username, String ipAddress, String userAgent, String failureReason, String loginSource) {
        return Mono.fromRunnable(() -> {
            try {
                String auditMessage = String.format(
                    "认证失败 - 用户: %s | IP: %s | User-Agent: %s | 失败原因: %s | 登录来源: %s | 事件类型: %s | 时间戳: %d",
                    username != null ? username : "未知", ipAddress, userAgent, failureReason, loginSource, SecurityConstants.AUDIT_LOGIN_FAILURE, System.currentTimeMillis()
                );
                
                LoggingUtil.warn(securityAuditLogger, auditMessage);
                LoggingUtil.warn(logger, "记录认证失败审计日志: 用户={}, IP={}, 原因={}", username, ipAddress, failureReason);
                
            } catch (Exception e) {
                LoggingUtil.error(logger, "记录认证失败审计日志失败", e);
            }
        });
    }

    @Override
    public Mono<Void> logLogout(String username, Long userId, String ipAddress, String userAgent, String logoutReason) {
        return Mono.fromRunnable(() -> {
            try {
                String auditMessage = String.format(
                    "用户登出 - 用户: %s | 用户ID: %s | IP: %s | User-Agent: %s | 登出原因: %s | 事件类型: %s | 时间戳: %d",
                    username, userId, ipAddress, userAgent, logoutReason, SecurityConstants.AUDIT_LOGOUT, System.currentTimeMillis()
                );
                
                LoggingUtil.info(securityAuditLogger, auditMessage);
                LoggingUtil.info(logger, "记录用户登出审计日志: 用户={}, IP={}, 原因={}", username, ipAddress, logoutReason);
                
            } catch (Exception e) {
                LoggingUtil.error(logger, "记录用户登出审计日志失败", e);
            }
        });
    }

    @Override
    public Mono<Void> logPermissionChange(String targetUsername, Long targetUserId, String operatorUsername, Long operatorUserId, String permissionChange, String ipAddress) {
        return Mono.fromRunnable(() -> {
            try {
                String auditMessage = String.format(
                    "权限变更 - 目标用户: %s | 目标用户ID: %s | 操作者: %s | 操作者ID: %s | 权限变更: %s | IP: %s | 事件类型: %s | 时间戳: %d",
                    targetUsername, targetUserId, operatorUsername, operatorUserId, permissionChange, ipAddress, SecurityConstants.AUDIT_PERMISSION_CHANGE, System.currentTimeMillis()
                );
                
                LoggingUtil.warn(securityAuditLogger, auditMessage);
                LoggingUtil.warn(logger, "记录权限变更审计日志: 目标用户={}, 操作者={}, 变更={}", targetUsername, operatorUsername, permissionChange);
                
            } catch (Exception e) {
                LoggingUtil.error(logger, "记录权限变更审计日志失败", e);
            }
        });
    }

    @Override
    public Mono<Void> logSensitiveOperation(String username, Long userId, String operation, String operationDetails, String ipAddress, String userAgent) {
        return Mono.fromRunnable(() -> {
            try {
                String auditMessage = String.format(
                    "敏感操作 - 用户: %s | 用户ID: %s | 操作: %s | 操作详情: %s | IP: %s | User-Agent: %s | 事件类型: %s | 时间戳: %d",
                    username, userId, operation, operationDetails, ipAddress, userAgent, SecurityConstants.AUDIT_SENSITIVE_OPERATION, System.currentTimeMillis()
                );
                
                LoggingUtil.warn(securityAuditLogger, auditMessage);
                LoggingUtil.warn(logger, "记录敏感操作审计日志: 用户={}, 操作={}, IP={}", username, operation, ipAddress);
                
            } catch (Exception e) {
                LoggingUtil.error(logger, "记录敏感操作审计日志失败", e);
            }
        });
    }

    @Override
    public Mono<Void> logAccountLocked(String username, Long userId, String lockReason, String ipAddress, String operatorUsername) {
        return Mono.fromRunnable(() -> {
            try {
                String auditMessage = String.format(
                    "账户锁定 - 用户: %s | 用户ID: %s | 锁定原因: %s | IP: %s | 操作者: %s | 事件类型: %s | 时间戳: %d",
                    username, userId, lockReason, ipAddress, operatorUsername != null ? operatorUsername : "系统自动", SecurityConstants.AUDIT_ACCOUNT_LOCKED, System.currentTimeMillis()
                );
                
                LoggingUtil.warn(securityAuditLogger, auditMessage);
                LoggingUtil.warn(logger, "记录账户锁定审计日志: 用户={}, 原因={}, IP={}", username, lockReason, ipAddress);
                
            } catch (Exception e) {
                LoggingUtil.error(logger, "记录账户锁定审计日志失败", e);
            }
        });
    }

    @Override
    public Mono<Void> logAccountUnlocked(String username, Long userId, String unlockReason, String ipAddress, String operatorUsername) {
        return Mono.fromRunnable(() -> {
            try {
                String auditMessage = String.format(
                    "账户解锁 - 用户: %s | 用户ID: %s | 解锁原因: %s | IP: %s | 操作者: %s | 事件类型: %s | 时间戳: %d",
                    username, userId, unlockReason, ipAddress, operatorUsername, SecurityConstants.AUDIT_ACCOUNT_UNLOCKED, System.currentTimeMillis()
                );
                
                LoggingUtil.info(securityAuditLogger, auditMessage);
                LoggingUtil.info(logger, "记录账户解锁审计日志: 用户={}, 原因={}, 操作者={}", username, unlockReason, operatorUsername);
                
            } catch (Exception e) {
                LoggingUtil.error(logger, "记录账户解锁审计日志失败", e);
            }
        });
    }

    @Override
    public Mono<Void> logPasswordChange(String username, Long userId, String changeType, String ipAddress, String userAgent) {
        return Mono.fromRunnable(() -> {
            try {
                String auditMessage = String.format(
                    "密码变更 - 用户: %s | 用户ID: %s | 变更类型: %s | IP: %s | User-Agent: %s | 事件类型: %s | 时间戳: %d",
                    username, userId, changeType, ipAddress, userAgent, SecurityConstants.AUDIT_PASSWORD_CHANGE, System.currentTimeMillis()
                );
                
                LoggingUtil.warn(securityAuditLogger, auditMessage);
                LoggingUtil.warn(logger, "记录密码变更审计日志: 用户={}, 类型={}, IP={}", username, changeType, ipAddress);
                
            } catch (Exception e) {
                LoggingUtil.error(logger, "记录密码变更审计日志失败", e);
            }
        });
    }

    @Override
    public Mono<Void> logTokenRefresh(String username, Long userId, String ipAddress, String userAgent, String refreshReason) {
        return Mono.fromRunnable(() -> {
            try {
                String auditMessage = String.format(
                    "令牌刷新 - 用户: %s | 用户ID: %s | IP: %s | User-Agent: %s | 刷新原因: %s | 事件类型: TOKEN_REFRESH | 时间戳: %d",
                    username, userId, ipAddress, userAgent, refreshReason, System.currentTimeMillis()
                );
                
                LoggingUtil.info(securityAuditLogger, auditMessage);
                LoggingUtil.info(logger, "记录令牌刷新审计日志: 用户={}, IP={}, 原因={}", username, ipAddress, refreshReason);
                
            } catch (Exception e) {
                LoggingUtil.error(logger, "记录令牌刷新审计日志失败", e);
            }
        });
    }
}

