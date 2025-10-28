package com.honyrun.service.reactive.impl;

import com.honyrun.model.entity.security.UserPasswordHistory;
import com.honyrun.repository.r2dbc.ReactiveUserPasswordHistoryRepository;
import com.honyrun.repository.r2dbc.ReactiveUserRepository;
import com.honyrun.service.reactive.ReactivePasswordSecurityService;
import com.honyrun.service.reactive.ReactiveSystemConfigService;
import com.honyrun.service.reactive.ReactiveOptimisticLockingService;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * 密码安全服务实现类
 * 提供密码历史记录检查、账户锁定等安全功能的具体实现
 *
 * @author Mr.Rey
 * @since 2025-07-01 10:30:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
public class ReactivePasswordSecurityServiceImpl implements ReactivePasswordSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(ReactivePasswordSecurityServiceImpl.class);

    private final ReactiveUserPasswordHistoryRepository passwordHistoryRepository;
    private final ReactiveUserRepository userRepository;
    private final ReactiveSystemConfigService systemConfigService;
    private final PasswordEncoder passwordEncoder;
    private final ReactiveOptimisticLockingService optimisticLockingService;

    /**
     * 构造函数注入
     *
     * @param passwordHistoryRepository 密码历史记录仓库
     * @param userRepository 用户仓库
     * @param systemConfigService 系统配置服务
     * @param passwordEncoder 密码编码器
     * @param optimisticLockingService 乐观锁服务
     */
    public ReactivePasswordSecurityServiceImpl(ReactiveUserPasswordHistoryRepository passwordHistoryRepository,
                                             ReactiveUserRepository userRepository,
                                             ReactiveSystemConfigService systemConfigService,
                                             PasswordEncoder passwordEncoder,
                                             ReactiveOptimisticLockingService optimisticLockingService) {
        this.passwordHistoryRepository = passwordHistoryRepository;
        this.userRepository = userRepository;
        this.systemConfigService = systemConfigService;
        this.passwordEncoder = passwordEncoder;
        this.optimisticLockingService = optimisticLockingService;
    }

    @Override
    public Mono<Boolean> isPasswordUsedBefore(Long userId, String newPassword) {
        LoggingUtil.info(logger, "检查用户密码历史记录，用户ID: {}", userId);
        
        return isPasswordHistoryEnabled()
                .flatMap(enabled -> {
                    if (!enabled) {
                        LoggingUtil.debug(logger, "密码历史记录功能未启用");
                        return Mono.just(false);
                    }
                    
                    return getPasswordHistoryCount()
                            .flatMap(historyCount -> 
                                passwordHistoryRepository.findRecentPasswordsByUserId(userId, historyCount)
                                        .any(history -> passwordEncoder.matches(newPassword, history.getPasswordHash()))
                            );
                })
                .doOnNext(isUsed -> {
                    if (isUsed) {
                        LoggingUtil.warn(logger, "用户尝试使用历史密码，用户ID: {}", userId);
                    } else {
                        LoggingUtil.debug(logger, "密码未在历史记录中找到，用户ID: {}", userId);
                    }
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "检查密码历史记录时发生错误，用户ID: {}", userId, error);
                    return Mono.just(false);
                });
    }

    @Override
    public Mono<Void> savePasswordHistory(Long userId, String passwordHash, String createdBy, String changeReason) {
        LoggingUtil.info(logger, "保存密码历史记录，用户ID: {}, 创建人: {}, 变更原因: {}", userId, createdBy, changeReason);
        
        return isPasswordHistoryEnabled()
                .flatMap(enabled -> {
                    if (!enabled) {
                        LoggingUtil.debug(logger, "密码历史记录功能未启用，跳过保存");
                        return Mono.empty();
                    }
                    
                    UserPasswordHistory history = new UserPasswordHistory(userId, passwordHash, createdBy, changeReason);
                    return passwordHistoryRepository.save(history)
                            .doOnSuccess(saved -> LoggingUtil.info(logger, "密码历史记录保存成功，记录ID: {}", saved.getId()))
                            .then(cleanupOldPasswordHistory(userId));
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "保存密码历史记录时发生错误，用户ID: {}", userId, error);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> cleanupOldPasswordHistory(Long userId) {
        LoggingUtil.debug(logger, "清理用户旧密码历史记录，用户ID: {}", userId);
        
        return getPasswordHistoryCount()
                .flatMap(keepCount -> 
                    passwordHistoryRepository.deleteOldPasswordHistory(userId, keepCount)
                            .doOnNext(deletedCount -> {
                                if (deletedCount > 0) {
                                    LoggingUtil.info(logger, "清理了 {} 条旧密码历史记录，用户ID: {}", deletedCount, userId);
                                }
                            })
                )
                .then()
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "清理密码历史记录时发生错误，用户ID: {}", userId, error);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Integer> recordLoginFailure(Long userId) {
        LoggingUtil.info(logger, "记录登录失败尝试，用户ID: {}", userId);
        
        // 使用统一的乐观锁服务处理并发更新
        return optimisticLockingService.executeWithOptimisticLockRetry(
                userRepository.findById(userId)
                        .flatMap(user -> {
                            // 数据库表中没有登录失败次数字段，通过日志记录登录失败
                            LoggingUtil.warn(logger, "用户登录失败，用户ID: {}", userId);
                            user.setLastModifiedDate(LocalDateTime.now());
                            
                            return userRepository.save(user)
                                    .map(savedUser -> {
                                        LoggingUtil.info(logger, "用户登录失败记录已更新，用户ID: {}", userId);
                                        return 1; // 返回固定值，因为数据库中没有登录失败次数字段
                                    });
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            LoggingUtil.warn(logger, "未找到用户，无法记录登录失败，用户ID: {}", userId);
                            return Mono.just(0);
                        })),
                "recordLoginFailure"
        ).onErrorResume(error -> {
            LoggingUtil.error(logger, "记录登录失败时发生错误，用户ID: {}", userId, error);
            return Mono.just(0);
        });
    }

    @Override
    public Mono<Void> resetLoginFailureCount(Long userId) {
        LoggingUtil.info(logger, "重置登录失败计数，用户ID: {}", userId);
        
        // 数据库表中没有登录失败次数字段，直接返回成功
        return Mono.<Void>empty()
                .doOnSuccess(unused -> LoggingUtil.info(logger, "登录失败计数已重置（数据库中无此字段），用户ID: {}", userId))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "重置登录失败计数时发生错误，用户ID: {}", userId, error);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> lockUserAccount(Long userId, int lockDurationMinutes) {
        LoggingUtil.info(logger, "锁定用户账户，用户ID: {}, 锁定时长: {} 分钟", userId, lockDurationMinutes);
        
        // 使用统一的乐观锁服务处理并发更新
        return optimisticLockingService.executeWithOptimisticLockRetry(
                userRepository.findById(userId)
                        .flatMap(user -> {
                            user.setStatus("LOCKED"); // 使用String类型的状态值，LOCKED表示锁定
                            user.setLastModifiedDate(LocalDateTime.now());
                            
                            return userRepository.save(user)
                                    .doOnSuccess(savedUser -> LoggingUtil.warn(logger, "用户账户已锁定，用户ID: {}", userId));
                        })
                        .then(),
                "lockUserAccount"
        ).onErrorResume(error -> {
            LoggingUtil.error(logger, "锁定用户账户时发生错误，用户ID: {}", userId, error);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> unlockUserAccount(Long userId) {
        LoggingUtil.info(logger, "解锁用户账户，用户ID: {}", userId);
        
        // 使用统一的乐观锁服务处理并发更新
        return optimisticLockingService.executeWithOptimisticLockRetry(
                userRepository.findById(userId)
                        .flatMap(user -> {
                            user.setStatus("ACTIVE"); // ACTIVE表示激活状态
                            // user.setLockedTime(null); // lockedTime字段已移除
                            // user.setLoginFailureCount(0); // loginFailureCount字段已移除
                            user.setLastModifiedDate(LocalDateTime.now());
                            
                            return userRepository.save(user)
                                    .doOnSuccess(savedUser -> LoggingUtil.info(logger, "用户账户已解锁，用户ID: {}", userId));
                        })
                        .then(),
                "unlockUserAccount"
        ).onErrorResume(error -> {
            LoggingUtil.error(logger, "解锁用户账户时发生错误，用户ID: {}", userId, error);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Boolean> isAccountLocked(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    boolean isLocked = user.isLocked();
                    
                    if (isLocked) {
                        LoggingUtil.debug(logger, "用户账户处于锁定状态，用户ID: {}", userId);
                    }
                    
                    return isLocked;
                })
                .switchIfEmpty(Mono.just(false))
                .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> hasReachedMaxLoginAttempts(Long userId) {
        return getMaxLoginAttempts()
        .map(maxAttempts -> {
            // 数据库表中没有登录失败次数字段，暂时返回false
            // 可以通过其他方式（如日志分析、缓存等）来实现登录失败次数限制
            LoggingUtil.debug(logger, "检查用户登录失败次数，用户ID: {}, 最大次数: {}", userId, maxAttempts);
            return false;
        })
        .onErrorReturn(false);
    }

    @Override
    public Mono<Integer> getPasswordHistoryCount() {
        return systemConfigService.getConfigValue("security.password.history.count", "5")
                .map(Integer::parseInt)
                .onErrorReturn(5);
    }

    @Override
    public Mono<Integer> getMaxLoginAttempts() {
        return systemConfigService.getConfigValue("security.account.lockout.max_attempts", "5")
                .map(Integer::parseInt)
                .onErrorReturn(5);
    }

    @Override
    public Mono<Integer> getLockoutDuration() {
        return systemConfigService.getConfigValue("security.account.lockout.duration", "15")
                .map(Integer::parseInt)
                .onErrorReturn(15);
    }

    @Override
    public Mono<Boolean> isPasswordHistoryEnabled() {
        return systemConfigService.getConfigValue("security.password.history.enabled", "true")
                .map(Boolean::parseBoolean)
                .onErrorReturn(true);
    }

    @Override
    public Mono<Boolean> isAccountLockoutEnabled() {
        return systemConfigService.getConfigValue("security.account.lockout.enabled", "true")
                .map(Boolean::parseBoolean)
                .onErrorReturn(true);
    }
}

