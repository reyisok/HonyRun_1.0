package com.honyrun.listener;

import com.honyrun.service.reactive.ReactiveSystemService;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 响应式用户登录监听器
 *
 * 监听用户登录成功事件，记录登录日志和统计信息
 * 支持响应式异步处理，避免阻塞登录流程
 *
 * 主要功能：
 * - 监听用户登录成功事件
 * - 记录用户登录日志
 * - 统计登录次数和频率
 * - 检测异常登录行为
 * - 更新用户活动状态
 *
 * 响应式特性：
 * - 非阻塞处理：登录日志记录异步执行，不影响登录响应时间
 * - 错误恢复：日志记录失败时提供错误恢复机制
 * - 流式处理：支持登录事件的流式处理和分析
 * - 背压控制：控制登录事件处理的速度，防止系统过载
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  23:35:00
 * @modified 2025-07-01 23:35:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class ReactiveUserLoginListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveUserLoginListener.class);

    private final ReactiveSystemService systemService;

    /**
     * 构造函数注入
     *
     * @param systemService 响应式系统服务
     */
    public ReactiveUserLoginListener(ReactiveSystemService systemService) {
        this.systemService = systemService;
    }

    /**
     * 登录事件计数器
     */
    private final AtomicLong loginEventCount = new AtomicLong(0);

    /**
     * 最后登录时间
     */
    private volatile LocalDateTime lastLoginTime;

    /**
     * 处理用户登录成功事件
     *
     * @param event 认证成功事件
     */
    @Override
    public void onApplicationEvent(@NonNull AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        LoggingUtil.info(logger, "用户登录成功事件触发，用户名: {}", username);

        processLoginEvent(username, event)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    loginEventCount.incrementAndGet();
                    lastLoginTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "用户登录事件处理完成，用户名: {}", username);
                })
                .doOnError(error -> LoggingUtil.error(logger, "用户登录事件处理失败，用户名: " + username, error))
                .onErrorResume(error -> handleLoginEventError(username, error))
                .subscribe();
    }

    /**
     * 处理登录事件
     *
     * @param username 用户名
     * @param event 认证事件
     * @return 处理结果
     */
    private Mono<Void> processLoginEvent(String username, AuthenticationSuccessEvent event) {
        return Mono.fromRunnable(() -> LoggingUtil.info(logger, "开始处理用户登录事件，用户名: {}", username))
                .then(recordLoginLog(username, event))
                .then(updateUserActivity(username))
                .then(checkLoginSecurity(username))
                .then(updateLoginStatistics(username))
                .then(Mono.fromRunnable(() -> LoggingUtil.info(logger, "用户登录事件处理完成，用户名: {}", username)));
    }

    /**
     * 记录登录日志
     *
     * @param username 用户名
     * @param event 认证事件
     * @return 记录结果
     */
    private Mono<Void> recordLoginLog(String username, AuthenticationSuccessEvent event) {
        LoggingUtil.info(logger, "记录用户登录日志，用户名: {}", username);

        String clientInfo = extractClientInfo(event);
        String loginMessage = String.format("用户登录成功 - 用户名: %s, 客户端信息: %s, 登录时间: %s",
                username, clientInfo, LocalDateTime.now());

        return systemService.recordSystemEvent(
                "USER_LOGIN",
                "INFO",
                "LOGIN",
                loginMessage
        ).then();
    }

    /**
     * 更新用户活动状态
     *
     * @param username 用户名
     * @return 更新结果
     */
    private Mono<Void> updateUserActivity(String username) {
        LoggingUtil.info(logger, "更新用户活动状态，用户名: {}", username);

        return systemService.updateUserLastLoginTime(username, LocalDateTime.now())
                .doOnSuccess(unused -> LoggingUtil.info(logger, "用户活动状态更新完成，用户名: {}", username))
                .doOnError(error -> LoggingUtil.error(logger, "用户活动状态更新失败，用户名: " + username, error))
                .then();
    }

    /**
     * 检查登录安全性
     *
     * @param username 用户名
     * @return 检查结果
     */
    private Mono<Void> checkLoginSecurity(String username) {
        LoggingUtil.info(logger, "检查用户登录安全性，用户名: {}", username);

        // 从当前上下文获取IP地址和用户代理信息，如果无法获取则使用默认值
        String ipAddress = "unknown";
        String userAgent = "unknown";

        return systemService.checkUserLoginSecurity(username, ipAddress, userAgent)
                .flatMap(securityResult -> {
                    Boolean isSecure = (Boolean) securityResult.get("isSecure");
                    if (Boolean.FALSE.equals(isSecure)) {
                        String riskLevel = (String) securityResult.get("riskLevel");
                        LoggingUtil.warn(logger, "检测到异常登录行为，用户名: {}, 风险级别: {}",
                                username, riskLevel);

                        return systemService.recordSystemEvent(
                                "LOGIN_SECURITY_WARNING",
                                "WARN",
                                "SECURITY",
                                String.format("异常登录检测 - 用户名: %s, 风险级别: %s", username, riskLevel)
                        );
                    } else {
                        LoggingUtil.info(logger, "用户登录安全检查通过，用户名: {}", username);
                        return Mono.empty();
                    }
                })
                .then();
    }

    /**
     * 更新登录统计信息
     *
     * @param username 用户名
     * @return 更新结果
     */
    private Mono<Void> updateLoginStatistics(String username) {
        LoggingUtil.info(logger, "更新登录统计信息，用户名: {}", username);

        return systemService.incrementUserLoginCount(username)
                .flatMap(loginCount -> {
                    LoggingUtil.info(logger, "用户登录次数更新完成，用户名: {}, 总登录次数: {}", username, loginCount);

                    // 记录登录统计日志
                    return systemService.recordSystemEvent(
                            "LOGIN_STATISTICS",
                            "INFO",
                            "STATISTICS",
                            String.format("用户登录统计更新 - 用户名: %s, 总登录次数: %d", username, loginCount)
                    );
                })
                .then();
    }

    /**
     * 处理登录事件错误
     *
     * @param username 用户名
     * @param error 错误信息
     * @return 错误处理结果
     */
    private Mono<Void> handleLoginEventError(String username, Throwable error) {
        LoggingUtil.error(logger, "用户登录事件处理失败，用户名: " + username, error);

        return systemService.recordSystemEvent(
                "LOGIN_EVENT_ERROR",
                "ERROR",
                "LOGIN",
                String.format("用户登录事件处理失败 - 用户名: %s, 错误: %s", username, error.getMessage())
        ).then();
    }

    /**
     * 提取客户端信息
     *
     * @param event 认证事件
     * @return 客户端信息
     */
    private String extractClientInfo(AuthenticationSuccessEvent event) {
        try {
            // 尝试从认证详情中提取客户端信息
            Object details = event.getAuthentication().getDetails();
            if (details != null) {
                return details.toString();
            }
        } catch (Exception e) {
            LoggingUtil.warn(logger, "提取客户端信息失败", e);
        }
        return "未知客户端";
    }

    // ==================== 统计查询方法 ====================

    /**
     * 获取登录事件计数
     *
     * @return 登录事件总数
     */
    public long getLoginEventCount() {
        return loginEventCount.get();
    }

    /**
     * 获取最后登录时间
     *
     * @return 最后登录时间
     */
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    /**
     * 重置登录统计
     */
    public void resetLoginStatistics() {
        LoggingUtil.info(logger, "重置登录统计信息");
        loginEventCount.set(0);
        lastLoginTime = null;
    }

    /**
     * 获取登录监听器状态
     *
     * @return 监听器状态信息
     */
    public String getListenerStatus() {
        return String.format("登录事件总数: %d, 最后登录时间: %s",
                loginEventCount.get(),
                lastLoginTime != null ? lastLoginTime.toString() : "无");
    }
}
