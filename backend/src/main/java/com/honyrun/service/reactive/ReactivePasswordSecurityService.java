package com.honyrun.service.reactive;

import reactor.core.publisher.Mono;

/**
 * 密码安全服务接口
 * 提供密码历史记录检查、账户锁定等安全功能
 *
 * @author Mr.Rey
 * @since 2025-07-01 10:30:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactivePasswordSecurityService {

    /**
     * 检查密码是否在历史记录中已使用过
     *
     * @param userId 用户ID
     * @param newPassword 新密码（明文）
     * @return 如果密码已使用过返回true，否则返回false
     */
    Mono<Boolean> isPasswordUsedBefore(Long userId, String newPassword);

    /**
     * 保存密码到历史记录
     *
     * @param userId 用户ID
     * @param passwordHash 密码哈希值
     * @param createdBy 创建人
     * @param changeReason 变更原因
     * @return 保存结果
     */
    Mono<Void> savePasswordHistory(Long userId, String passwordHash, String createdBy, String changeReason);

    /**
     * 清理用户的旧密码历史记录，只保留配置的数量
     *
     * @param userId 用户ID
     * @return 清理结果
     */
    Mono<Void> cleanupOldPasswordHistory(Long userId);

    /**
     * 记录登录失败尝试
     *
     * @param userId 用户ID
     * @return 当前失败次数
     */
    Mono<Integer> recordLoginFailure(Long userId);

    /**
     * 重置登录失败计数
     *
     * @param userId 用户ID
     * @return 重置结果
     */
    Mono<Void> resetLoginFailureCount(Long userId);

    /**
     * 锁定用户账户
     *
     * @param userId 用户ID
     * @param lockDurationMinutes 锁定持续时间（分钟）
     * @return 锁定结果
     */
    Mono<Void> lockUserAccount(Long userId, int lockDurationMinutes);

    /**
     * 解锁用户账户
     *
     * @param userId 用户ID
     * @return 解锁结果
     */
    Mono<Void> unlockUserAccount(Long userId);

    /**
     * 检查用户账户是否被锁定
     *
     * @param userId 用户ID
     * @return 如果账户被锁定返回true，否则返回false
     */
    Mono<Boolean> isAccountLocked(Long userId);

    /**
     * 检查用户是否达到最大登录失败次数
     *
     * @param userId 用户ID
     * @return 如果达到最大次数返回true，否则返回false
     */
    Mono<Boolean> hasReachedMaxLoginAttempts(Long userId);

    /**
     * 获取密码历史记录保留数量配置
     *
     * @return 保留数量
     */
    Mono<Integer> getPasswordHistoryCount();

    /**
     * 获取最大登录失败尝试次数配置
     *
     * @return 最大尝试次数
     */
    Mono<Integer> getMaxLoginAttempts();

    /**
     * 获取账户锁定持续时间配置（分钟）
     *
     * @return 锁定持续时间
     */
    Mono<Integer> getLockoutDuration();

    /**
     * 检查密码安全功能是否启用
     *
     * @return 如果启用返回true，否则返回false
     */
    Mono<Boolean> isPasswordHistoryEnabled();

    /**
     * 检查账户锁定功能是否启用
     *
     * @return 如果启用返回true，否则返回false
     */
    Mono<Boolean> isAccountLockoutEnabled();
}

