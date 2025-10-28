package com.honyrun.service.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * 响应式令牌黑名单服务接口
 *
 * 提供令牌黑名单管理功能的响应式服务接口。
 * 支持令牌黑名单的添加、查询、删除和过期处理等操作。
 *
 * 主要功能：
 * - 令牌黑名单添加和移除
 * - 令牌黑名单状态查询
 * - 批量令牌黑名单操作
 * - 过期令牌清理
 * - 黑名单统计信息
 *
 * 响应式特性：
 * - 非阻塞操作：所有操作均为非阻塞
 * - 流式处理：支持令牌的流式查询
 * - 错误处理：提供令牌操作失败的恢复机制
 * - 异步更新：支持令牌状态的异步更新
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 10:30:00
 * @modified 2025-07-01 10:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveTokenBlacklistService {

    /**
     * 将令牌添加到黑名单
     *
     * @param token JWT令牌
     * @param reason 加入黑名单的原因
     * @param expireSeconds 过期时间（秒）
     * @return 添加结果的Mono包装
     */
    Mono<Boolean> addToBlacklist(String token, String reason, long expireSeconds);

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token JWT令牌
     * @return 检查结果的Mono包装，true表示在黑名单中
     */
    Mono<Boolean> isTokenBlacklisted(String token);

    /**
     * 获取令牌的黑名单信息
     *
     * @param token JWT令牌
     * @return 黑名单信息的Mono包装，如果不在黑名单中则返回空
     */
    Mono<String> getBlacklistInfo(String token);

    /**
     * 从黑名单中移除令牌
     *
     * @param token JWT令牌
     * @return 移除结果的Mono包装
     */
    Mono<Boolean> removeFromBlacklist(String token);

    /**
     * 获取所有黑名单令牌
     *
     * @return 黑名单令牌集合的Flux包装
     */
    Flux<String> getAllBlacklistedTokens();

    /**
     * 清理过期的黑名单令牌
     *
     * @return 清理数量的Mono包装
     */
    Mono<Long> cleanupExpiredTokens();

    /**
     * 获取黑名单令牌数量
     *
     * @return 令牌数量的Mono包装
     */
    Mono<Long> getBlacklistCount();

    /**
     * 批量添加令牌到黑名单
     *
     * @param tokens 令牌集合
     * @param reason 加入黑名单的原因
     * @param expireSeconds 过期时间（秒）
     * @return 添加结果的Mono包装，返回成功添加的数量
     */
    Mono<Long> batchAddToBlacklist(Set<String> tokens, String reason, long expireSeconds);
}

