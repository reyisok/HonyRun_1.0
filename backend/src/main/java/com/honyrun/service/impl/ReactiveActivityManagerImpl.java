package com.honyrun.service.impl;

import com.honyrun.config.MonitoringProperties;
import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.constant.CacheConstants;
import com.honyrun.exception.BusinessException;
import com.honyrun.exception.ErrorCode;
import com.honyrun.model.dto.reactive.ReactiveActiveActivity;
import com.honyrun.service.reactive.ReactiveTokenBlacklistService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.common.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.honyrun.service.reactive.ReactiveActivityManager;

import java.time.Duration;

/**
 * 响应式活动状态管理器实现类
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  20:50:00
 * @modified 2025-10-26 01:36:12
 *
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 实现基于Redis的响应式用户活动状态管理功能
 * 包含用户活动追踪、并发控制、强制下线等功能
 */
@Service
public class ReactiveActivityManagerImpl implements ReactiveActivityManager {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveActivityManagerImpl.class);

    private final ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate;
    private final ReactiveTokenBlacklistService tokenBlacklistService;
    private final UnifiedConfigManager unifiedConfigManager;
    private final MonitoringProperties monitoringProperties;

    /**
     * 构造函数注入依赖
     *
     * @param reactiveStringRedisTemplate Redis模板
     * @param tokenBlacklistService 令牌黑名单服务
     * @param unifiedConfigManager 统一配置管理器
     */
    public ReactiveActivityManagerImpl(
            @Qualifier("reactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate,
            ReactiveTokenBlacklistService tokenBlacklistService,
            UnifiedConfigManager unifiedConfigManager,
            MonitoringProperties monitoringProperties) {
        this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
        this.tokenBlacklistService = tokenBlacklistService;
        this.unifiedConfigManager = unifiedConfigManager;
        this.monitoringProperties = monitoringProperties;
    }

    /**
     * 记录用户活动
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param userType 用户类型
     * @param sessionId 会话ID
     * @param tokenId 令牌ID
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     * @return 活动记录结果的Mono包装
     */
    public Mono<ReactiveActiveActivity> recordActivity(Long userId, String username, String userType,
                                                      String activityId, String tokenId, String clientIp, String userAgent) {
        LoggingUtil.info(logger, "记录用户活动: userId={}, username={}, activityId={}, clientIp={}",
                         userId, username, activityId, clientIp);

        return Mono.fromCallable(() -> {
            if (userId == null || username == null || activityId == null) {
                throw new IllegalArgumentException("用户ID、用户名和活动ID不能为空");
            }
            return new ReactiveActiveActivity(userId, username, activityId, tokenId, clientIp);
        })
        .flatMap(activity -> {
            activity.setUserType(userType);
            activity.setUserAgent(userAgent);
            activity.setDeviceType(parseDeviceType(userAgent));

            String activityKey = CacheConstants.USER_SESSION_KEY_PREFIX + activityId;
            String activityJson = JsonUtil.toJson(activity);
            int activityTimeoutMinutes = 120; // 直接使用默认值，避免阻塞调用
            Duration expireDuration = monitoringProperties.getDurations().getLongTimeout();

            return reactiveStringRedisTemplate.opsForValue()
                    .set(activityKey, activityJson, expireDuration)
                    .doOnError(error -> LoggingUtil.error(logger, "Redis设置活动记录失败", error))
                    .onErrorResume(error -> {
                        LoggingUtil.warn(logger, "Redis设置活动记录失败，返回false", error);
                        return Mono.just(false);
                    })
                    .flatMap(success -> {
                        if (Boolean.TRUE.equals(success)) {
                            LoggingUtil.info(logger, "用户活动记录成功: userId={}, activityId={}", userId, activityId);
                            return updateUserActivityIndex(userId, activityId)
                                    .doOnError(error -> LoggingUtil.error(logger, "更新用户活动索引失败", error))
                                    .onErrorResume(error -> {
                                        LoggingUtil.warn(logger, "更新用户活动索引失败，但继续流程", error);
                                        return Mono.just(false);
                                    })
                                    .then(Mono.just(activity));
                        } else {
                            LoggingUtil.warn(logger, "用户活动记录失败: userId={}, activityId={}", userId, activityId);
                            return Mono.error(new RuntimeException("活动记录失败"));
                        }
                    });
        })
        .doOnError(error -> {
            LoggingUtil.error(logger, "记录用户活动时发生错误: userId={}, error={}", userId, error.getMessage());
        });
    }

    /**
     * 记录用户活动（兼容性方法）
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param userType 用户类型
     * @param sessionId 会话ID
     * @param tokenId 令牌ID
     * @param deviceId 设备ID
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     * @return 活动记录结果的Mono包装
     */
    @Override
    public Mono<Void> recordUserActivity(Long userId, String username, String userType,
                                        String activityId, String tokenId, String deviceId,
                                        String clientIp, String userAgent) {
        // 添加null检查，确保符合响应式编程最佳实践
        if (userId == null || username == null || activityId == null) {
            LoggingUtil.warn(logger, "记录用户活动参数不能为空: userId={}, username={}, activityId={}",
                           userId, username, activityId);
            return Mono.error(new BusinessException(ErrorCode.INVALID_PARAMETER));
        }
        
        try {
            Mono<ReactiveActiveActivity> activityMono = recordActivity(userId, username, userType, activityId, tokenId, clientIp, userAgent);
            
            // 确保recordActivity不返回null
            if (activityMono == null) {
                LoggingUtil.warn(logger, "recordActivity返回null，使用空Mono替代");
                return Mono.empty();
            }
            
            return activityMono
                    .then()
                    .onErrorResume(error -> {
                        LoggingUtil.warn(logger, "记录用户活动失败: userId={}, error={}", userId, error.getMessage());
                        return Mono.empty(); // 错误时返回空Mono，不中断流程
                    });
        } catch (Exception e) {
            LoggingUtil.error(logger, "记录用户活动异常: userId={}, error={}", userId, e.getMessage());
            return Mono.empty(); // 异常时返回空Mono
        }
    }

    /**
     * 记录用户登录活动
     *
     * @param userId 用户ID
     * @param clientIp 客户端IP地址
     * @param userAgent 用户代理信息
     * @param deviceId 设备ID
     * @return 记录操作结果
     */
    @Override
    public Mono<Void> recordLoginActivity(Long userId, String clientIp, String userAgent, String deviceId) {
        return Mono.fromRunnable(() -> {
            try {
                LoggingUtil.info(logger, "记录用户登录活动: userId={}, clientIp={}, deviceId={}", userId, clientIp, deviceId);
            } catch (Exception e) {
                LoggingUtil.warn(logger, "记录登录活动失败: {}", e.getMessage());
            }
        });
    }

    /**
     * 移除用户活动（通过用户名）
     *
     * @param username 用户名
     * @return 移除结果的Mono包装
     */
    public Mono<Void> removeUserActivity(String username) {
        LoggingUtil.info(logger, "移除用户活动: username={}", username);

        // 查找该用户的所有活跃会话
        String pattern = CacheConstants.USER_SESSION_KEY_PREFIX + "*";

        return reactiveStringRedisTemplate.keys(pattern)
                .doOnError(error -> LoggingUtil.error(logger, "获取Redis键失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "获取Redis键失败，返回空流", error);
                    return Flux.empty();
                })
                .flatMap(key -> reactiveStringRedisTemplate.opsForValue().get(key)
                        .doOnError(error -> LoggingUtil.error(logger, "获取活动数据失败: key={}", key, error))
                        .onErrorResume(error -> {
                            LoggingUtil.warn(logger, "获取活动数据失败，跳过该键: key={}", key, error);
                            return Mono.empty();
                        }))
                .map(activityJson -> JsonUtil.fromJson(activityJson, ReactiveActiveActivity.class))
                .filter(activity -> username.equals(activity.getUsername()))
                .flatMap(activity -> {
                    String activityKey = CacheConstants.USER_SESSION_KEY_PREFIX + activity.getActivityId();
                    return reactiveStringRedisTemplate.delete(activityKey)
                            .doOnError(error -> LoggingUtil.error(logger, "删除活动记录失败", error))
                            .onErrorResume(error -> {
                                LoggingUtil.warn(logger, "删除活动记录失败，但继续流程", error);
                                return Mono.just(0L);
                            })
                            .flatMap(deleted -> removeFromUserActivityIndex(activity.getUserId(), activity.getActivityId())
                                    .doOnError(error -> LoggingUtil.error(logger, "移除用户活动索引失败", error))
                                    .onErrorResume(error -> {
                                        LoggingUtil.warn(logger, "移除用户活动索引失败，但继续流程", error);
                                        return Mono.just(false);
                                    }))
                            .then();
                })
                .then()
                .doOnSuccess(v -> LoggingUtil.info(logger, "用户活动移除成功: username={}", username))
                .doOnError(error -> LoggingUtil.error(logger, "移除用户活动时发生错误: username={}, error={}",
                        username, error.getMessage()));
    }

    /**
     * 更新用户活动状态
     *
     * @param activityId 活动ID
     * @return 更新结果的Mono包装
     */
    public Mono<Boolean> updateActivity(String activityId) {
        LoggingUtil.debug(logger, "更新用户活动状态: activityId={}", activityId);

        return getActivity(activityId)
                .flatMap(activity -> {
                    activity.updateLastActivity();

                    String activityKey = CacheConstants.USER_SESSION_KEY_PREFIX + activityId;
                    String activityJson = JsonUtil.toJson(activity);
                    int activityTimeoutMinutes = 120; // 直接使用默认值，避免阻塞调用
                    Duration expireDuration = Duration.ofMinutes(activityTimeoutMinutes);

                    return reactiveStringRedisTemplate.opsForValue()
                            .set(activityKey, activityJson, expireDuration)
                            .map(success -> {
                                if (Boolean.TRUE.equals(success)) {
                                    LoggingUtil.debug(logger, "用户活动状态更新成功: activityId={}", activityId);
                                } else {
                                    LoggingUtil.warn(logger, "用户活动状态更新失败: activityId={}", activityId);
                                }
                                return Boolean.TRUE.equals(success);
                            });
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "更新用户活动状态时发生错误: activityId={}, error={}", activityId, error.getMessage());
                });
    }

    /**
     * 获取用户活动信息
     *
     * @param activityId 活动ID
     * @return 活动信息的Mono包装
     */
    public Mono<ReactiveActiveActivity> getActivity(String activityId) {
        LoggingUtil.debug(logger, "获取用户活动信息: activityId={}", activityId);

        if (activityId == null || activityId.trim().isEmpty()) {
            return Mono.empty();
        }

        String activityKey = CacheConstants.USER_SESSION_KEY_PREFIX + activityId;

        return reactiveStringRedisTemplate.opsForValue()
                .get(activityKey)
                .map(activityJson -> JsonUtil.fromJson(activityJson, ReactiveActiveActivity.class))
                .doOnNext(activity -> {
                    LoggingUtil.debug(logger, "获取到用户活动信息: activityId={}, userId={}", activityId, activity.getUserId());
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "获取用户活动信息时发生错误: activityId={}, error={}", activityId, error.getMessage());
                })
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "获取用户活动信息失败，返回空: activityId={}", activityId);
                    return Mono.empty();
                });
    }

    /**
     * 强制用户下线
     *
     * @param userId 用户ID
     * @param reason 下线原因
     * @return 下线结果的Mono包装
     */
    public Mono<Boolean> forceLogoutUser(Long userId, String reason) {
        LoggingUtil.info(logger, "强制用户下线: userId={}, reason={}", userId, reason);

        return getUserActiveSessions(userId)
                .flatMap(sessionId -> forceLogoutSession(sessionId, reason))
                .filter(Boolean::booleanValue)
                .count()
                .map(count -> {
                    LoggingUtil.info(logger, "强制用户下线完成: userId={}, 下线会话数={}", userId, count);
                    return count > 0;
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "强制用户下线时发生错误: userId={}, error={}", userId, error.getMessage());
                });
    }

    /**
     * 强制会话下线
     *
     * @param activityId 活动ID
     * @param reason 下线原因
     * @return 下线结果的Mono包装
     */
    public Mono<Boolean> forceLogoutSession(String activityId, String reason) {
        LoggingUtil.info(logger, "强制会话下线: activityId={}, reason={}", activityId, reason);

        return getActivity(activityId)
                .flatMap(activity -> {
                    // 标记为强制下线
                    activity.markForceLogout(reason);

                    // 将令牌加入黑名单
                    return tokenBlacklistService.addToBlacklist(activity.getTokenId(), reason,
                                                              CacheConstants.TOKEN_BLACKLIST_EXPIRE_SECONDS)
                            .flatMap(blacklistResult -> {
                                // 删除活动记录
                                String activityKey = CacheConstants.USER_SESSION_KEY_PREFIX + activityId;
                                return reactiveStringRedisTemplate.delete(activityKey)
                                        .flatMap(deleteResult -> {
                                            // 从用户活动索引中移除
                                            return removeFromUserActivityIndex(activity.getUserId(), activityId)
                                                    .then(Mono.just(deleteResult > 0 && Boolean.TRUE.equals(blacklistResult)));
                                        });
                            });
                })
                .defaultIfEmpty(false)
                .doOnSuccess(result -> {
                    if (Boolean.TRUE.equals(result)) {
                        LoggingUtil.info(logger, "会话强制下线成功: activityId={}", activityId);
                    } else {
                        LoggingUtil.warn(logger, "会话强制下线失败: activityId={}", activityId);
                    }
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "强制会话下线时发生错误: activityId={}, error={}", activityId, error.getMessage());
                })
                .onErrorReturn(false);
    }

    /**
     * 获取所有活跃用户
     *
     * @return 活跃用户列表的Flux包装
     */
    public Flux<ReactiveActiveActivity> getAllActiveUsers() {
        LoggingUtil.debug(logger, "获取所有活跃用户");

        String pattern = CacheConstants.USER_SESSION_KEY_PREFIX + "*";

        return reactiveStringRedisTemplate.keys(pattern)
                .flatMap(key -> reactiveStringRedisTemplate.opsForValue().get(key))
                .map(activityJson -> JsonUtil.fromJson(activityJson, ReactiveActiveActivity.class))
                .filter(activity -> !activity.getForceLogout() && "ACTIVE".equals(activity.getActivityStatus()))
                .doOnNext(activity -> {
                    LoggingUtil.debug(logger, "找到活跃用户: userId={}, username={}",
                                    activity.getUserId(), activity.getUsername());
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "获取所有活跃用户时发生错误: error={}", error.getMessage());
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取活跃用户失败，返回空流: error={}", error.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * 获取用户的活跃会话
     *
     * @param userId 用户ID
     * @return 会话ID列表的Flux包装
     */
    public Flux<String> getUserActiveSessions(Long userId) {
        LoggingUtil.debug(logger, "获取用户的活跃会话: userId={}", userId);

        String indexKey = CacheConstants.USER_KEY_PREFIX + "sessions:" + userId;

        return reactiveStringRedisTemplate.opsForSet()
                .members(indexKey)
                .doOnNext(sessionId -> {
                    LoggingUtil.debug(logger, "找到用户会话: userId={}, sessionId={}", userId, sessionId);
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "获取用户活跃会话时发生错误: userId={}, error={}", userId, error.getMessage());
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取用户会话失败，返回空流: userId={}", userId);
                    return Flux.empty();
                });
    }

    /**
     * 获取当前活跃用户数量
     *
     * @return 活跃用户数量的Mono包装
     */
    public Mono<Long> getActiveUserCount() {
        LoggingUtil.debug(logger, "获取当前活跃用户数量");

        return getAllActiveUsers()
                .count()
                .doOnSuccess(count -> {
                    LoggingUtil.debug(logger, "当前活跃用户数量: {}", count);
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "获取活跃用户数量时发生错误: error={}", error.getMessage());
                })
                .onErrorReturn(0L);
    }

    /**
     * 检查并发用户限制
     *
     * @return 是否超过限制的Mono包装
     */
    public Mono<Boolean> checkConcurrentLimit() {
        int maxConcurrentUsers = 100; // 直接使用默认值，避免阻塞调用
        LoggingUtil.debug(logger, "检查并发用户限制，最大并发数: {}", maxConcurrentUsers);

        return getActiveUserCount()
                .map(count -> count >= maxConcurrentUsers)
                .doOnSuccess(exceeded -> {
                    if (Boolean.TRUE.equals(exceeded)) {
                        LoggingUtil.warn(logger, "并发用户数已达到限制: 当前用户数可能超过 {}", maxConcurrentUsers);
                    }
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "检查并发用户限制时发生错误: error={}", error.getMessage());
                })
                .onErrorReturn(false);
    }

    /**
     * 清理过期的活动记录
     *
     * @return 清理数量的Mono包装
     */
    public Mono<Long> cleanupExpiredActivities() {
        LoggingUtil.info(logger, "开始清理过期的活动记录");

        String pattern = CacheConstants.USER_SESSION_KEY_PREFIX + "*";
        int activityTimeoutMinutes = 120; // 直接使用默认值，避免阻塞调用

        return reactiveStringRedisTemplate.keys(pattern)
                .flatMap(key -> {
                    return reactiveStringRedisTemplate.opsForValue()
                            .get(key)
                            .map(activityJson -> JsonUtil.fromJson(activityJson, ReactiveActiveActivity.class))
                            .filter(activity -> activity.isExpired(activityTimeoutMinutes))
                            .flatMap(expiredActivity -> {
                                LoggingUtil.debug(logger, "清理过期活动: userId={}, activityId={}",
                                                expiredActivity.getUserId(), expiredActivity.getActivityId());

                                // 删除活动记录
                                return reactiveStringRedisTemplate.delete(key)
                                        .flatMap(deleteResult -> {
                                            // 从用户活动索引中移除
                                            return removeFromUserActivityIndex(expiredActivity.getUserId(),
                                                                             expiredActivity.getActivityId())
                                                    .then(Mono.just(deleteResult));
                                        });
                            })
                            .defaultIfEmpty(0L);
                })
                .reduce(0L, Long::sum)
                .doOnSuccess(count -> {
                    LoggingUtil.info(logger, "清理过期活动记录完成，清理数量: {}", count);
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "清理过期活动记录时发生错误: error={}", error.getMessage());
                })
                .onErrorReturn(0L);
    }

    /**
     * 更新用户活动索引
     *
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 更新结果的Mono包装
     */
    @Override
    public Mono<Boolean> updateUserActivityIndex(Long userId, String activityId) {
        String indexKey = CacheConstants.USER_KEY_PREFIX + "sessions:" + userId;
        int activityTimeoutMinutes = 120; // 直接使用默认值，避免阻塞调用
        Duration expireDuration = Duration.ofMinutes(activityTimeoutMinutes + 10); // 比活动记录稍长

        return reactiveStringRedisTemplate.opsForSet()
                .add(indexKey, activityId)
                .flatMap(added -> reactiveStringRedisTemplate.expire(indexKey, expireDuration))
                .map(expired -> true)
                .onErrorReturn(false);
    }

    /**
     * 从用户活动索引中移除
     *
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 移除结果的Mono包装
     */
    @Override
    public Mono<Boolean> removeFromUserActivityIndex(Long userId, String activityId) {
        String indexKey = CacheConstants.USER_KEY_PREFIX + "sessions:" + userId;

        return reactiveStringRedisTemplate.opsForSet()
                .remove(indexKey, activityId)
                .map(removed -> removed > 0)
                .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> removeActivity(String activityId) {
        LoggingUtil.info(logger, "移除用户活动: activityId={}", activityId);
        
        return getActivity(activityId)
                .flatMap((ReactiveActiveActivity activity) -> {
                    // 从用户活动索引中移除
                    return removeFromUserActivityIndex(activity.getUserId(), activityId)
                            .then(reactiveStringRedisTemplate.delete(CacheConstants.USER_SESSION_KEY_PREFIX + activityId))
                            .map(deleted -> deleted > 0);
                })
                .defaultIfEmpty(false)
                .doOnSuccess(result -> {
                    if (result) {
                        LoggingUtil.info(logger, "用户活动移除成功: activityId={}", activityId);
                    } else {
                        LoggingUtil.warn(logger, "用户活动移除失败: activityId={}", activityId);
                    }
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "移除用户活动时发生错误: activityId={}, error={}", activityId, error.getMessage());
                });
    }

    @Override
    public Mono<Boolean> isUserOnline(Long userId) {
        LoggingUtil.debug(logger, "检查用户是否在线: userId={}", userId);
        
        return getUserActiveSessions(userId)
                .hasElements()
                .doOnSuccess(online -> {
                    LoggingUtil.debug(logger, "用户在线状态: userId={}, online={}", userId, online);
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "检查用户在线状态时发生错误: userId={}, error={}", userId, error.getMessage());
                });
    }

    @Override
    public Mono<Long> getUserActiveSessionCount(Long userId) {
        LoggingUtil.debug(logger, "获取用户活跃会话数量: userId={}", userId);
        
        return getUserActiveSessions(userId)
                .count()
                .doOnSuccess(count -> {
                    LoggingUtil.debug(logger, "用户活跃会话数量: userId={}, count={}", userId, count);
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "获取用户活跃会话数量时发生错误: userId={}, error={}", userId, error.getMessage());
                });
    }

    @Override
    public Mono<Long> getOnlineUserCount() {
        LoggingUtil.debug(logger, "获取在线用户数量");
        
        return getAllActiveUsers()
                .map(ReactiveActiveActivity::getUserId)
                .distinct()
                .count()
                .doOnSuccess(count -> {
                    LoggingUtil.debug(logger, "在线用户数量: {}", count);
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "获取在线用户数量时发生错误: error={}", error.getMessage());
                });
    }

    /**
     * 解析设备类型
     *
     * @param userAgent 用户代理字符串
     * @return 设备类型
     */
    private String parseDeviceType(String userAgent) {
        if (userAgent == null) {
            return "UNKNOWN";
        }

        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "MOBILE";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "TABLET";
        } else {
            return "DESKTOP";
        }
    }
}



