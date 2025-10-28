package com.honyrun.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import com.honyrun.constant.CacheConstants;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.honyrun.service.reactive.ReactiveTokenBlacklistService;

/**
 * 响应式令牌黑名单服务实现类
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  20:30:00
 * @modified 2025-07-01 10:30:00
 *
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 实现基于Redis的响应式令牌黑名单管理功能
 * 包含令牌黑名单添加、查询、删除和过期处理
 */
@Service
public class ReactiveTokenBlacklistServiceImpl implements ReactiveTokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveTokenBlacklistServiceImpl.class);

    private final ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate;

    /**
     * 构造函数
     *
     * @param reactiveStringRedisTemplate 字符串类型响应式Redis模板
     */
    public ReactiveTokenBlacklistServiceImpl(
            @Qualifier("unifiedReactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate) {
        this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
    }

    /**
     * 将令牌添加到黑名单
     *
     * @param token JWT令牌
     * @param reason 加入黑名单的原因
     * @param expireSeconds 过期时间（秒）
     * @return 添加结果的Mono包装
     */
    public Mono<Boolean> addToBlacklist(String token, String reason, long expireSeconds) {
        LoggingUtil.info(logger, "开始将令牌添加到黑名单: token={}, reason={}, expireSeconds={}",
                         maskToken(token), reason, expireSeconds);

        return Mono.fromCallable(() -> {
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("令牌不能为空");
            }
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("加入黑名单的原因不能为空");
            }
            if (expireSeconds <= 0) {
                throw new IllegalArgumentException("过期时间必须大于0");
            }
            return token.trim();
        })
        .flatMap(validToken -> {
            String blacklistKey = CacheConstants.TOKEN_BLACKLIST_KEY_PREFIX + validToken;
            String blacklistValue = createBlacklistValue(reason);
            Duration expireDuration = Duration.ofSeconds(expireSeconds);

            return reactiveStringRedisTemplate.opsForValue()
                    .set(blacklistKey, blacklistValue, expireDuration)
                    .doOnSuccess(result -> {
                        if (Boolean.TRUE.equals(result)) {
                            LoggingUtil.info(logger, "令牌成功添加到黑名单: token={}, reason={}",
                                           maskToken(validToken), reason);
                        } else {
                            LoggingUtil.warn(logger, "令牌添加到黑名单失败: token={}, reason={}",
                                           maskToken(validToken), reason);
                        }
                    })
                    .doOnError(error -> {
                        LoggingUtil.error(logger, "令牌添加到黑名单时发生错误: token={}, reason={}, error={}",
                                        maskToken(validToken), reason, error.getMessage());
                    });
        })
        .onErrorReturn(false);
    }

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token JWT令牌
     * @return 检查结果的Mono包装，true表示在黑名单中
     */
    public Mono<Boolean> isTokenBlacklisted(String token) {
        LoggingUtil.debug(logger, "检查令牌是否在黑名单中: token={}", maskToken(token));

        return Mono.fromCallable(() -> {
            if (token == null || token.trim().isEmpty()) {
                return "";
            }
            return token.trim();
        })
        .filter(validToken -> !validToken.isEmpty())
        .flatMap(validToken -> {
            String blacklistKey = CacheConstants.TOKEN_BLACKLIST_KEY_PREFIX + validToken;

            return reactiveStringRedisTemplate.hasKey(blacklistKey)
                    .doOnSuccess(exists -> {
                        LoggingUtil.debug(logger, "令牌黑名单检查结果: token={}, exists={}",
                                        maskToken(validToken), exists);
                    })
                    .doOnError(error -> {
                        LoggingUtil.error(logger, "检查令牌黑名单时发生错误: token={}, error={}",
                                        maskToken(validToken), error.getMessage());
                    });
        })
        .defaultIfEmpty(false)
        .onErrorReturn(false);
    }

    /**
     * 获取令牌的黑名单信息
     *
     * @param token JWT令牌
     * @return 黑名单信息的Mono包装，如果不在黑名单中则返回空
     */
    public Mono<String> getBlacklistInfo(String token) {
        LoggingUtil.debug(logger, "获取令牌黑名单信息: token={}", maskToken(token));

        return Mono.fromCallable(() -> {
            if (token == null || token.trim().isEmpty()) {
                return ""; // 返回空字符串而不是null
            }
            return token.trim();
        })
        .flatMap(validToken -> {
            if (validToken.isEmpty()) {
                return Mono.empty(); // 返回空Mono
            }

            String blacklistKey = CacheConstants.TOKEN_BLACKLIST_KEY_PREFIX + validToken;

            return reactiveStringRedisTemplate.opsForValue()
                    .get(blacklistKey)
                    .doOnSuccess(info -> {
                        LoggingUtil.debug(logger, "获取到令牌黑名单信息: token={}, info={}",
                                        maskToken(validToken), info);
                    })
                    .doOnError(error -> {
                        LoggingUtil.error(logger, "获取令牌黑名单信息时发生错误: token={}, error={}",
                                        maskToken(validToken), error.getMessage());
                    });
        })
        .onErrorReturn("");
    }

    /**
     * 从黑名单中移除令牌
     *
     * @param token JWT令牌
     * @return 移除结果的Mono包装
     */
    public Mono<Boolean> removeFromBlacklist(String token) {
        LoggingUtil.info(logger, "开始从黑名单中移除令牌: token={}", maskToken(token));

        return Mono.fromCallable(() -> {
            if (token == null || token.trim().isEmpty()) {
                return ""; // 返回空字符串而不是null
            }
            return token.trim();
        })
        .flatMap(validToken -> {
            if (validToken.isEmpty()) {
                return Mono.just(false); // 返回false而不是null
            }

            String blacklistKey = CacheConstants.TOKEN_BLACKLIST_KEY_PREFIX + validToken;

            return reactiveStringRedisTemplate.delete(blacklistKey)
                    .map(count -> count > 0 ? Boolean.TRUE : Boolean.FALSE)
                    .doOnSuccess(result -> {
                        if (Boolean.TRUE.equals(result)) {
                            LoggingUtil.info(logger, "令牌成功从黑名单中移除: token={}", maskToken(validToken));
                        } else {
                            LoggingUtil.warn(logger, "令牌从黑名单中移除失败或令牌不存在: token={}", maskToken(validToken));
                        }
                    })
                    .doOnError(error -> {
                        LoggingUtil.error(logger, "从黑名单中移除令牌时发生错误: token={}, error={}",
                                        maskToken(validToken), error.getMessage());
                    });
        })
        .onErrorReturn(false);
    }

    /**
     * 获取所有黑名单令牌
     *
     * @return 黑名单令牌集合的Flux包装
     */
    public Flux<String> getAllBlacklistedTokens() {
        LoggingUtil.debug(logger, "获取所有黑名单令牌");

        String pattern = CacheConstants.TOKEN_BLACKLIST_KEY_PREFIX + "*";

        return reactiveStringRedisTemplate.keys(pattern)
                .map(key -> key.substring(CacheConstants.TOKEN_BLACKLIST_KEY_PREFIX.length()))
                .doOnNext(token -> LoggingUtil.debug(logger, "找到黑名单令牌: token={}", maskToken(token)))
                .doOnError(error -> {
                    LoggingUtil.error(logger, "获取所有黑名单令牌时发生错误: error={}", error.getMessage());
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取黑名单令牌失败，返回空流: error={}", error.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * 清理过期的黑名单令牌
     *
     * @return 清理数量的Mono包装
     */
    public Mono<Long> cleanupExpiredTokens() {
        LoggingUtil.info(logger, "开始清理过期的黑名单令牌");

        String pattern = CacheConstants.TOKEN_BLACKLIST_KEY_PREFIX + "*";

        return reactiveStringRedisTemplate.keys(pattern)
                .flatMap(key ->
                    reactiveStringRedisTemplate.getExpire(key)
                            .filter(ttl -> ttl.isNegative() || ttl.isZero())
                            .flatMap(ttl -> reactiveStringRedisTemplate.delete(key))
                            .defaultIfEmpty(0L)
                )
                .reduce(0L, Long::sum)
                .doOnSuccess(count -> {
                    LoggingUtil.info(logger, "清理过期黑名单令牌完成，清理数量: {}", count);
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "清理过期黑名单令牌时发生错误: error={}", error.getMessage());
                })
                .onErrorReturn(0L);
    }

    /**
     * 获取黑名单令牌数量
     *
     * @return 令牌数量的Mono包装
     */
    public Mono<Long> getBlacklistCount() {
        LoggingUtil.debug(logger, "获取黑名单令牌数量");

        String pattern = CacheConstants.TOKEN_BLACKLIST_KEY_PREFIX + "*";

        return reactiveStringRedisTemplate.keys(pattern)
                .count()
                .doOnSuccess(count -> {
                    LoggingUtil.debug(logger, "黑名单令牌数量: {}", count);
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "获取黑名单令牌数量时发生错误: error={}", error.getMessage());
                })
                .onErrorReturn(0L);
    }

    /**
     * 批量添加令牌到黑名单
     *
     * @param tokens 令牌集合
     * @param reason 加入黑名单的原因
     * @param expireSeconds 过期时间（秒）
     * @return 成功添加数量的Mono包装
     */
    public Mono<Long> batchAddToBlacklist(Set<String> tokens, String reason, long expireSeconds) {
        LoggingUtil.info(logger, "批量添加令牌到黑名单: tokenCount={}, reason={}, expireSeconds={}",
                         tokens != null ? tokens.size() : 0, reason, expireSeconds);

        if (tokens == null || tokens.isEmpty()) {
            return Mono.just(0L);
        }

        return Flux.fromIterable(tokens)
                .flatMap(token -> addToBlacklist(token, reason, expireSeconds))
                .filter(Boolean::booleanValue)
                .count()
                .doOnSuccess(count -> {
                    LoggingUtil.info(logger, "批量添加令牌到黑名单完成，成功数量: {}", count);
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "批量添加令牌到黑名单时发生错误: error={}", error.getMessage());
                })
                .onErrorReturn(0L);
    }

    /**
     * 创建黑名单值
     *
     * @param reason 加入黑名单的原因
     * @return 黑名单值字符串
     */
    private String createBlacklistValue(String reason) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format("reason=%s,timestamp=%s", reason, timestamp);
    }

    /**
     * 掩码令牌，用于日志记录时保护敏感信息
     *
     * @param token 原始令牌
     * @return 掩码后的令牌
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "***";
        }
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }
}


