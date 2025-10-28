package com.honyrun.security.jwt;

import com.honyrun.config.properties.JwtProperties;
import com.honyrun.constant.SecurityConstants;
import com.honyrun.model.enums.UserType;
import com.honyrun.service.reactive.ReactiveTokenBlacklistService;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * 响应式令牌自动刷新服务
 *
 * 提供令牌自动刷新功能，包括：
 * - 检查令牌是否需要刷新
 * - 自动刷新即将过期的令牌
 * - 管理令牌刷新策略
 * - 处理令牌黑名单
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-06-28 17:30:00
 * @modified 2025-06-29 17:30:00
 * @version 1.0.0
 */
@Service
public class ReactiveTokenRefreshService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveTokenRefreshService.class);

    private final ReactiveJwtTokenProvider jwtTokenProvider;
    private final ReactiveTokenBlacklistService tokenBlacklistService;
    private final JwtProperties jwtProperties;

    /**
     * 构造函数
     *
     * @param jwtTokenProvider JWT令牌提供者
     * @param tokenBlacklistService 令牌黑名单服务
     * @param jwtProperties JWT配置属性
     */
    public ReactiveTokenRefreshService(ReactiveJwtTokenProvider jwtTokenProvider,
                                     ReactiveTokenBlacklistService tokenBlacklistService,
                                     JwtProperties jwtProperties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtProperties = jwtProperties;

        LoggingUtil.info(logger, "ReactiveTokenRefreshService initialized with auto-refresh: {}, threshold: {} ms",
                jwtProperties.isAutoRefreshEnabled(), jwtProperties.getRefreshThreshold());
    }

    /**
     * 检查令牌是否需要刷新
     *
     * @param token 访问令牌
     * @return 是否需要刷新的Mono
     */
    public Mono<Boolean> shouldRefreshToken(String token) {
        if (!jwtProperties.isAutoRefreshEnabled()) {
            return Mono.just(false);
        }

        return jwtTokenProvider.parseToken(token)
                .map(claims -> {
                    Date expiration = claims.getExpiration();
                    Instant expirationInstant = expiration.toInstant();
                    Instant now = Instant.now();

                    // 计算剩余时间
                    Duration remainingTime = Duration.between(now, expirationInstant);
                    Duration threshold = jwtProperties.getRefreshThresholdDuration();

                    boolean shouldRefresh = remainingTime.compareTo(threshold) <= 0 && remainingTime.isPositive();

                    if (shouldRefresh) {
                        LoggingUtil.info(logger, "Token should be refreshed - remaining time: {} ms, threshold: {} ms",
                                remainingTime.toMillis(), threshold.toMillis());
                    }

                    return shouldRefresh;
                })
                .onErrorReturn(false);
    }

    /**
     * 自动刷新令牌
     *
     * @param accessToken 当前访问令牌
     * @param refreshToken 刷新令牌
     * @return 新的令牌对的Mono
     */
    public Mono<TokenPair> refreshTokens(String accessToken, String refreshToken) {
        return validateRefreshRequest(accessToken, refreshToken)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new com.honyrun.exception.AuthenticationException("Invalid refresh token"));
                    }

                    return performTokenRefresh(accessToken, refreshToken);
                });
    }

    /**
     * 验证刷新请求
     *
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @return 验证结果的Mono
     */
    private Mono<Boolean> validateRefreshRequest(String accessToken, String refreshToken) {
        // 1. 验证刷新令牌有效性
        return jwtTokenProvider.validateRefreshToken(refreshToken)
                .flatMap(isRefreshValid -> {
                    if (!isRefreshValid) {
                        LoggingUtil.warn(logger, "Invalid refresh token provided");
                        return Mono.just(false);
                    }

                    // 2. 检查访问令牌是否在黑名单中
                    return tokenBlacklistService.isTokenBlacklisted(accessToken)
                            .map(isBlacklisted -> {
                                if (isBlacklisted) {
                                    LoggingUtil.warn(logger, "Access token is blacklisted, cannot refresh");
                                    return false;
                                }
                                return true;
                            });
                })
                .onErrorReturn(false);
    }

    /**
     * 执行令牌刷新
     *
     * @param accessToken 当前访问令牌
     * @param refreshToken 刷新令牌
     * @return 新的令牌对的Mono
     */
    private Mono<TokenPair> performTokenRefresh(String accessToken, String refreshToken) {
        return jwtTokenProvider.parseToken(refreshToken)
                .flatMap(refreshClaims -> {
                    // 提取用户信息
                    Long userId = refreshClaims.get(SecurityConstants.JWT_CLAIM_USER_ID, Long.class);
                    String username = refreshClaims.get(SecurityConstants.JWT_CLAIM_USERNAME, String.class);
                    String userTypeStr = refreshClaims.get(SecurityConstants.JWT_CLAIM_USER_TYPE, String.class);
                    String authorities = refreshClaims.get(SecurityConstants.JWT_CLAIM_AUTHORITIES, String.class);
                    String deviceId = refreshClaims.get(SecurityConstants.JWT_CLAIM_DEVICE_ID, String.class);
                    String ipAddress = refreshClaims.get(SecurityConstants.JWT_CLAIM_IP_ADDRESS, String.class);

                    UserType userType = UserType.valueOf(userTypeStr);

                    // 生成新的访问令牌
                    return jwtTokenProvider.generateAccessToken(userId, username, userType, authorities, deviceId, ipAddress)
                            .flatMap(newAccessToken -> {
                                // 生成新的刷新令牌
                                return jwtTokenProvider.generateRefreshToken(userId, username, userType, authorities, deviceId)
                                        .flatMap(newRefreshToken -> {
                                            // 将旧的访问令牌加入黑名单
                                            return tokenBlacklistService.addToBlacklist(accessToken, "Token refreshed", jwtProperties.getBlacklistCacheTime())
                                                    .then(Mono.fromCallable(() -> {
                                                        LoggingUtil.info(logger, "Successfully refreshed tokens for user: {}, device: {}", username, deviceId);
                                                        return new TokenPair(newAccessToken, newRefreshToken);
                                                    }));
                                        });
                            });
                })
                .onErrorMap(e -> {
                    LoggingUtil.error(logger, "Failed to refresh tokens", e);
                    return new RuntimeException("Token refresh failed", e);
                });
    }

    /**
     * 检查令牌是否即将过期
     *
     * @param token JWT令牌
     * @return 是否即将过期的Mono
     */
    public Mono<Boolean> isTokenNearExpiry(String token) {
        return jwtTokenProvider.parseToken(token)
                .map(claims -> {
                    Date expiration = claims.getExpiration();
                    Instant expirationInstant = expiration.toInstant();
                    Instant now = Instant.now();

                    Duration remainingTime = Duration.between(now, expirationInstant);
                    Duration threshold = jwtProperties.getRefreshThresholdDuration();

                    return remainingTime.compareTo(threshold) <= 0 && remainingTime.isPositive();
                })
                .onErrorReturn(true); // 解析失败时认为已过期
    }

    /**
     * 获取令牌剩余时间
     *
     * @param token JWT令牌
     * @return 剩余时间的Mono
     */
    public Mono<Duration> getTokenRemainingTime(String token) {
        return jwtTokenProvider.parseToken(token)
                .map(claims -> {
                    Date expiration = claims.getExpiration();
                    Instant expirationInstant = expiration.toInstant();
                    Instant now = Instant.now();

                    Duration remainingTime = Duration.between(now, expirationInstant);
                    return remainingTime.isNegative() ? Duration.ZERO : remainingTime;
                })
                .onErrorReturn(Duration.ZERO);
    }

    /**
     * 令牌对数据类
     */
    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        @Override
        public String toString() {
            return "TokenPair{" +
                    "accessToken='[PROTECTED]'" +
                    ", refreshToken='[PROTECTED]'" +
                    '}';
        }
    }
}


