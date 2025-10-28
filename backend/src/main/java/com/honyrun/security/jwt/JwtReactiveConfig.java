package com.honyrun.security.jwt;

import com.honyrun.constant.SecurityConstants;
import com.honyrun.config.properties.JwtProperties;
import com.honyrun.config.properties.SecurityDetectionProperties;
import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.service.security.UnifiedSecurityDetectionService;
import com.honyrun.service.reactive.ReactiveTokenBlacklistService;
import com.honyrun.util.LoggingUtil;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * JWT响应式配置类
 * 
 * 负责配置JWT相关的Bean，包括：
 * - ReactiveJwtTokenProvider：JWT令牌提供者
 * - SecretKey：JWT签名密钥
 * - MacAlgorithm：JWT签名算法
 * - JWT配置参数：过期时间、发行者、受众等
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-13 16:57:03
 * @version 1.0.0 - 统一JWT配置，移除兼容性代码
 */
@Configuration
public class JwtReactiveConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtReactiveConfig.class);

    private final JwtProperties jwtProperties;
    private final UnifiedConfigManager unifiedConfigManager;

    public JwtReactiveConfig(JwtProperties jwtProperties, UnifiedConfigManager unifiedConfigManager) {
        this.jwtProperties = jwtProperties;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 创建ReactiveJwtTokenProvider Bean
     *
     * @param securityDetectionProperties 安全检测配置属性
     * @param unifiedSecurityDetectionService 统一安全检测服务
     * @param tokenBlacklistService 令牌黑名单服务
     * @return ReactiveJwtTokenProvider实例
     */
    @Bean("reactiveJwtTokenProvider")
    @Profile("!test")
    public ReactiveJwtTokenProvider reactiveJwtTokenProvider(
            SecurityDetectionProperties securityDetectionProperties,
            UnifiedSecurityDetectionService unifiedSecurityDetectionService,
            ReactiveTokenBlacklistService tokenBlacklistService) {

        LoggingUtil.info(logger, "Creating ReactiveJwtTokenProvider with issuer: {}, audience: {}", 
                jwtProperties.getIssuer(), jwtProperties.getAudience());

        return new ReactiveJwtTokenProvider(jwtProperties, securityDetectionProperties, unifiedSecurityDetectionService, Optional.of(tokenBlacklistService), unifiedConfigManager);
    }

    /**
     * 生产环境JWT签名密钥Bean
     * 按照Bean冲突解决规范，移除@Primary注解，使用环境隔离策略
     *
     * @return JWT签名密钥
     */
    @Bean("jwtSigningKey")
    @Profile("!test")
    public SecretKey jwtSigningKey() {
        String secret = getJwtSecret();

        // 确保密钥长度符合HS512要求（至少64字节）
        if (secret.getBytes(StandardCharsets.UTF_8).length < 64) {
            LoggingUtil.warn(logger, "JWT secret is shorter than recommended 64 bytes, padding with repeated pattern");
            // 重复密钥直到达到64字节
            StringBuilder paddedSecret = new StringBuilder(secret);
            while (paddedSecret.length() < 64) {
                paddedSecret.append(secret);
            }
            secret = paddedSecret.substring(0, 64);
        }

        LoggingUtil.info(logger, "Creating JWT signing key with HS512 algorithm");
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT签名算法Bean
     *
     * @return MacAlgorithm实例
     */
    @Bean("jwtSignatureAlgorithm")
    public MacAlgorithm jwtSignatureAlgorithm() {
        LoggingUtil.info(logger, "Using HS512 signature algorithm for JWT");
        return Jwts.SIG.HS512;
    }

    /**
     * JWT访问令牌过期时间Bean
     *
     * @return 访问令牌过期时间（毫秒）
     */
    @Bean("jwtAccessTokenExpiration")
    public Long jwtAccessTokenExpiration() {
        Long expiration = getJwtAccessTokenExpiration();
        LoggingUtil.info(logger, "JWT access token expiration: {} ms", expiration);
        return expiration;
    }

    /**
     * JWT刷新令牌过期时间Bean
     *
     * @return 刷新令牌过期时间（毫秒）
     */
    @Bean("jwtRefreshTokenExpiration")
    public Long jwtRefreshTokenExpiration() {
        Long expiration = getJwtRefreshTokenExpiration();
        LoggingUtil.info(logger, "JWT refresh token expiration: {} ms", expiration);
        return expiration;
    }

    /**
     * JWT发行者Bean
     *
     * @return JWT发行者
     */
    @Bean("jwtIssuer")
    public String jwtIssuer() {
        String issuer = jwtProperties.getIssuer();
        LoggingUtil.info(logger, "JWT issuer: {}", issuer);
        return issuer;
    }

    /**
     * JWT受众Bean
     *
     * @return JWT受众
     */
    @Bean("jwtAudience")
    public String jwtAudience() {
        String audience = jwtProperties.getAudience();
        LoggingUtil.info(logger, "JWT audience: {}", audience);
        return audience;
    }



    /**
     * JWT访问令牌持续时间Bean
     *
     * @return Duration实例
     */
    @Bean("jwtAccessTokenDuration")
    @org.springframework.context.annotation.Profile({"dev", "prod"})
    public Duration jwtAccessTokenDuration() {
        Duration duration = Duration.ofMillis(getJwtAccessTokenExpiration());
        LoggingUtil.info(logger, "JWT access token duration: {}", duration);
        return duration;
    }

    /**
     * JWT刷新令牌持续时间Bean
     *
     * @return Duration实例
     */
    @Bean("jwtRefreshTokenDuration")
    @org.springframework.context.annotation.Profile({"dev", "prod"})
    public Duration jwtRefreshTokenDuration() {
        Duration duration = Duration.ofMillis(getJwtRefreshTokenExpiration());
        LoggingUtil.info(logger, "JWT refresh token duration: {}", duration);
        return duration;
    }

    /**
     * 获取JWT签名密钥
     * 统一使用JwtProperties配置，移除@Value兼容性代码
     *
     * @return JWT签名密钥
     */
    private String getJwtSecret() {
        String secret = jwtProperties.getSecret();
        if (secret != null && !secret.trim().isEmpty()) {
            return secret.trim();
        }

        LoggingUtil.warn(logger, "JWT secret not configured in JwtProperties, using default from SecurityConstants");
        return SecurityConstants.JWT_SECRET_DEFAULT;
    }

    /**
     * 获取JWT访问令牌过期时间
     * 统一使用JwtProperties配置，移除@Value兼容性代码
     *
     * @return 访问令牌过期时间（毫秒）
     */
    private Long getJwtAccessTokenExpiration() {
        Long expiration = jwtProperties.getExpiration();
        if (expiration != null && expiration > 0) {
            return expiration;
        }

        LoggingUtil.warn(logger, "JWT access token expiration not configured in JwtProperties, using default from SecurityConstants");
        return SecurityConstants.JWT_ACCESS_TOKEN_EXPIRE_MS;
    }

    /**
     * 获取JWT刷新令牌过期时间
     * 统一使用JwtProperties配置，移除@Value兼容性代码
     *
     * @return 刷新令牌过期时间（毫秒）
     */
    private Long getJwtRefreshTokenExpiration() {
        Long expiration = jwtProperties.getRefreshExpiration();
        if (expiration != null && expiration > 0) {
            return expiration;
        }

        LoggingUtil.warn(logger, "JWT refresh token expiration not configured in JwtProperties, using default from SecurityConstants");
        return SecurityConstants.JWT_REFRESH_TOKEN_EXPIRE_MS;
    }

    /**
     * JWT配置信息内部类
     * 用于封装JWT配置信息，便于调试和监控
     */
    public static class JwtConfigInfo {
        private final String algorithm;
        private final Long accessTokenExpiration;
        private final Long refreshTokenExpiration;
        private final String issuer;
        private final String audience;
        private final String subject;

        public JwtConfigInfo(String algorithm, Long accessTokenExpiration,
                           Long refreshTokenExpiration, String issuer,
                           String audience, String subject) {
            this.algorithm = algorithm;
            this.accessTokenExpiration = accessTokenExpiration;
            this.refreshTokenExpiration = refreshTokenExpiration;
            this.issuer = issuer;
            this.audience = audience;
            this.subject = subject;
        }

        // Getters
        public String getAlgorithm() { return algorithm; }
        public Long getAccessTokenExpiration() { return accessTokenExpiration; }
        public Long getRefreshTokenExpiration() { return refreshTokenExpiration; }
        public String getIssuer() { return issuer; }
        public String getAudience() { return audience; }
        public String getSubject() { return subject; }

        @Override
        public String toString() {
            return "JwtConfigInfo{" +
                    "algorithm='" + algorithm + '\'' +
                    ", accessTokenExpiration=" + accessTokenExpiration +
                    ", refreshTokenExpiration=" + refreshTokenExpiration +
                    ", issuer='" + issuer + '\'' +
                    ", audience='" + audience + '\'' +
                    ", subject='" + subject + '\'' +
                    '}';
        }
    }

    /**
     * JWT配置信息Bean
     * 用于调试和监控JWT配置
     *
     * @return JwtConfigInfo实例
     */
    @Bean("jwtConfigInfo")
    public JwtConfigInfo jwtConfigInfo() {
        return new JwtConfigInfo(
                "HS512",
                getJwtAccessTokenExpiration(),
                getJwtRefreshTokenExpiration(),
                jwtProperties.getIssuer(),
                jwtProperties.getAudience(),
                jwtProperties.getSubject()
        );
    }
}

