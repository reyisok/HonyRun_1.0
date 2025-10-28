package com.honyrun.service.reactive;

import com.honyrun.model.dto.reactive.ReactiveActiveActivity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式活动状态管理器接口
 *
 * 提供用户活动状态管理功能的响应式服务接口。
 * 支持用户活动追踪、并发控制、强制下线等操作。
 *
 * 主要功能：
 * - 用户活动记录和追踪
 * - 活跃用户管理
 * - 会话强制下线
 * - 活动状态查询
 * - 过期活动清理
 *
 * 响应式特性：
 * - 非阻塞操作：所有操作均为非阻塞
 * - 流式处理：支持活动数据的流式查询
 * - 错误处理：提供活动操作失败的恢复机制
 * - 异步更新：支持活动状态的异步更新
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 10:30:00
 * @modified 2025-07-01 10:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveActivityManager {

    /**
     * 记录用户活动
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param userType 用户类型
     * @param activityId 活动ID
     * @param tokenId 令牌ID
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     * @return 活动记录结果的Mono包装
     */
    Mono<ReactiveActiveActivity> recordActivity(Long userId, String username, String userType,
                                               String activityId, String tokenId, String clientIp, String userAgent);

    /**
     * 获取用户活动信息
     *
     * @param activityId 活动ID
     * @return 活动信息的Mono包装
     */
    Mono<ReactiveActiveActivity> getActivity(String activityId);

    /**
     * 更新用户活动状态
     *
     * @param activityId 活动ID
     * @return 更新结果的Mono包装
     */
    Mono<Boolean> updateActivity(String activityId);

    /**
     * 移除用户活动
     *
     * @param activityId 活动ID
     * @return 移除结果的Mono包装
     */
    Mono<Boolean> removeActivity(String activityId);

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 在线状态的Mono包装
     */
    Mono<Boolean> isUserOnline(Long userId);

    /**
     * 获取用户的活跃会话数量
     *
     * @param userId 用户ID
     * @return 活跃会话数量的Mono包装
     */
    Mono<Long> getUserActiveSessionCount(Long userId);

    /**
     * 强制用户下线
     *
     * @param userId 用户ID
     * @param reason 下线原因
     * @return 下线结果的Mono包装
     */
    Mono<Boolean> forceLogoutUser(Long userId, String reason);

    /**
     * 强制会话下线
     *
     * @param activityId 活动ID
     * @param reason 下线原因
     * @return 下线结果的Mono包装
     */
    Mono<Boolean> forceLogoutSession(String activityId, String reason);

    /**
     * 获取所有活跃用户
     *
     * @return 活跃用户列表的Flux包装
     */
    Flux<ReactiveActiveActivity> getAllActiveUsers();

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量的Mono包装
     */
    Mono<Long> getOnlineUserCount();

    /**
     * 记录用户活动
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param userType 用户类型
     * @param activityId 活动ID
     * @param tokenId 令牌ID
     * @param deviceId 设备ID
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     * @return 活动记录结果的Mono包装
     */
    Mono<Void> recordUserActivity(Long userId, String username, String userType,
                                 String activityId, String tokenId, String deviceId,
                                 String clientIp, String userAgent);

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
     * 移除用户活动（通过用户名）
     *
     * @param username 用户名
     * @return 移除结果的Mono包装
     */
    Mono<Void> removeUserActivity(String username);

    /**
     * 清理过期的活动记录
     *
     * @return 清理数量的Mono包装
     */
    Mono<Long> cleanupExpiredActivities();

    /**
     * 更新用户活动索引
     *
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 更新结果的Mono包装
     */
    Mono<Boolean> updateUserActivityIndex(Long userId, String activityId);

    /**
     * 从用户活动索引中移除
     *
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 移除结果的Mono包装
     */
    Mono<Boolean> removeFromUserActivityIndex(Long userId, String activityId);
}

