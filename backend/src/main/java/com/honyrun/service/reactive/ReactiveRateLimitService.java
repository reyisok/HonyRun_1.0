package com.honyrun.service.reactive;

import reactor.core.publisher.Mono;

/**
 * 响应式速率限制服务接口
 * 
 * 提供多维度的API速率限制功能，支持：
 * - 全局速率限制：整个系统的总体请求限制
 * - IP级速率限制：基于客户端IP地址的限制
 * - 用户级速率限制：基于认证用户的限制
 * - 端点级速率限制：基于特定API端点的限制
 * 
 * 使用响应式编程模型，确保非阻塞的性能表现
 * 
 * @author Mr.Rey
 * @created 2025-07-01 17:10:00
 * @modified 2025-07-01 17:10:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveRateLimitService {

    /**
     * 检查全局速率限制
     * 
     * @return 是否允许请求通过
     */
    Mono<Boolean> checkGlobalRateLimit();

    /**
     * 检查IP级速率限制
     * 
     * @param clientIp 客户端IP地址
     * @return 是否允许请求通过
     */
    Mono<Boolean> checkIpRateLimit(String clientIp);

    /**
     * 检查用户级速率限制
     * 
     * @param userId 用户ID
     * @return 是否允许请求通过
     */
    Mono<Boolean> checkUserRateLimit(Long userId);

    /**
     * 检查端点级速率限制
     * 
     * @param endpoint API端点路径
     * @return 是否允许请求通过
     */
    Mono<Boolean> checkEndpointRateLimit(String endpoint);

    /**
     * 综合检查所有维度的速率限制
     * 
     * @param clientIp 客户端IP地址
     * @param userId 用户ID（可为null，表示未认证用户）
     * @param endpoint API端点路径
     * @return 是否允许请求通过
     */
    Mono<Boolean> checkAllRateLimits(String clientIp, Long userId, String endpoint);

    /**
     * 获取指定维度的剩余配额
     * 
     * @param dimension 限制维度（global、ip、user、endpoint）
     * @param key 限制键值（IP地址、用户ID、端点路径等）
     * @return 剩余配额数量
     */
    Mono<Long> getRemainingQuota(String dimension, String key);

    /**
     * 重置指定维度的速率限制计数器
     * 
     * @param dimension 限制维度
     * @param key 限制键值
     * @return 重置操作是否成功
     */
    Mono<Boolean> resetRateLimit(String dimension, String key);

    /**
     * 获取速率限制统计信息
     * 
     * @param dimension 限制维度
     * @param key 限制键值
     * @return 速率限制统计信息
     */
    Mono<RateLimitStats> getRateLimitStats(String dimension, String key);

    /**
     * 速率限制统计信息类
     */
    class RateLimitStats {
        private final long totalRequests;
        private final long allowedRequests;
        private final long blockedRequests;
        private final long remainingQuota;
        private final long resetTime;

        public RateLimitStats(long totalRequests, long allowedRequests, long blockedRequests, 
                             long remainingQuota, long resetTime) {
            this.totalRequests = totalRequests;
            this.allowedRequests = allowedRequests;
            this.blockedRequests = blockedRequests;
            this.remainingQuota = remainingQuota;
            this.resetTime = resetTime;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getAllowedRequests() {
            return allowedRequests;
        }

        public long getBlockedRequests() {
            return blockedRequests;
        }

        public long getRemainingQuota() {
            return remainingQuota;
        }

        public long getResetTime() {
            return resetTime;
        }

        @Override
        public String toString() {
            return "RateLimitStats{" +
                    "totalRequests=" + totalRequests +
                    ", allowedRequests=" + allowedRequests +
                    ", blockedRequests=" + blockedRequests +
                    ", remainingQuota=" + remainingQuota +
                    ", resetTime=" + resetTime +
                    '}';
        }
    }
}

