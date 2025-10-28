package com.honyrun.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.config.properties.JwtProperties;
import com.honyrun.config.properties.SecurityDetectionProperties;
import com.honyrun.constant.SecurityConstants;
import com.honyrun.exception.AuthenticationException;
import com.honyrun.model.enums.UserType;
import com.honyrun.service.reactive.ReactiveTokenBlacklistService;
import com.honyrun.service.security.UnifiedSecurityDetectionService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.TraceIdUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

/**
 * 响应式JWT令牌提供者
 *
 * @author: Mr.Rey
 * @created: 2025-07-01 15:30:00
 * @modified: 2025-01-30 17:00:00
 * @version: 2.1.0 - 使用JwtProperties外部化配置
 */
public class ReactiveJwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveJwtTokenProvider.class);

    private final SecretKey secretKey;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;
    private final JwtProperties jwtProperties;
    private final SecurityDetectionProperties securityDetectionProperties;
    private final UnifiedSecurityDetectionService unifiedSecurityDetectionService;
    private final ReactiveTokenBlacklistService tokenBlacklistService;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数
     *
     * 统一使用JwtProperties配置，移除@Value兼容性代码
     * 符合WebFlux规范：使用Optional处理可选依赖，避免null值
     *
     * @param jwtProperties                   JWT配置属性
     * @param securityDetectionProperties     安全检测配置属性
     * @param unifiedSecurityDetectionService 统一安全检测服务
     * @param tokenBlacklistService           令牌黑名单服务（测试环境必须测试令牌黑名单）
     * @param unifiedConfigManager            统一配置管理器
     */
    public ReactiveJwtTokenProvider(
            JwtProperties jwtProperties,
            SecurityDetectionProperties securityDetectionProperties,
            UnifiedSecurityDetectionService unifiedSecurityDetectionService,
            Optional<ReactiveTokenBlacklistService> tokenBlacklistService,
            UnifiedConfigManager unifiedConfigManager) {

        // 使用JwtProperties配置
        String effectiveSecret = jwtProperties.getSecret();
        if (effectiveSecret == null || effectiveSecret.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret cannot be null or empty");
        }

        // 确保密钥长度足够
        String processedSecret = effectiveSecret;
        if (effectiveSecret.getBytes(StandardCharsets.UTF_8).length < 64) {
            // 如果密钥长度不足，进行填充
            StringBuilder sb = new StringBuilder(effectiveSecret);
            while (sb.length() < 64) {
                sb.append(effectiveSecret);
            }
            processedSecret = sb.substring(0, 64);
        }

        this.secretKey = Keys.hmacShaKeyFor(processedSecret.getBytes(StandardCharsets.UTF_8));

        // 优先使用JwtProperties配置
        this.accessTokenExpiration = jwtProperties.getExpirationDuration() != null
                ? jwtProperties.getExpirationDuration()
                : Duration.ofMillis(SecurityConstants.JWT_ACCESS_TOKEN_EXPIRE_MS);
        this.refreshTokenExpiration = jwtProperties.getRefreshExpirationDuration() != null
                ? jwtProperties.getRefreshExpirationDuration()
                : Duration.ofMillis(SecurityConstants.JWT_REFRESH_TOKEN_EXPIRE_MS);

        this.jwtProperties = jwtProperties;
        this.securityDetectionProperties = securityDetectionProperties;
        this.unifiedSecurityDetectionService = unifiedSecurityDetectionService;
        this.tokenBlacklistService = tokenBlacklistService.orElse(null);
        this.unifiedConfigManager = unifiedConfigManager;

        LoggingUtil.info(logger,
                "ReactiveJwtTokenProvider initialized with JwtProperties configuration - issuer: {}, audience: {}, access token expiration: {} ms, refresh token expiration: {} ms, blacklist service: {}",
                jwtProperties.getIssuer(), jwtProperties.getAudience(), this.accessTokenExpiration.toMillis(),
                this.refreshTokenExpiration.toMillis(),
                tokenBlacklistService.isPresent() ? "enabled" : "disabled");
    }

    /**
     * 生成访问令牌
     *
     * @param userId      用户ID
     * @param username    用户名
     * @param userType    用户类型
     * @param authorities 权限列表
     * @param deviceId    设备ID
     * @param ipAddress   IP地址
     * @return 访问令牌的Mono
     */
    public Mono<String> generateAccessToken(Long userId, String username, UserType userType,
            String authorities, String deviceId, String ipAddress) {
        return Mono.fromCallable(() -> {
            try {
                // 验证必需参数
                if (username == null || username.trim().isEmpty()) {
                    throw new IllegalArgumentException("Username cannot be null or empty");
                }
                if (userType == null) {
                    throw new IllegalArgumentException("User type cannot be null");
                }

                // 恶意内容检测：用户名、权限字符串、设备ID与IP地址
                if (unifiedSecurityDetectionService.containsMaliciousPatterns(username)) {
                    LoggingUtil.warn(logger, "Malicious content detected in username: {}", username);
                    throw new AuthenticationException("Invalid username content");
                }
                if (authorities != null && unifiedSecurityDetectionService.containsMaliciousPatterns(authorities)) {
                    LoggingUtil.warn(logger, "Malicious content detected in authorities: {}", authorities);
                    throw new AuthenticationException("Invalid authorities content");
                }
                if (deviceId != null && unifiedSecurityDetectionService.containsMaliciousPatterns(deviceId)) {
                    LoggingUtil.warn(logger, "Malicious content detected in deviceId: {}", deviceId);
                    throw new AuthenticationException("Invalid deviceId content");
                }
                if (ipAddress != null && unifiedSecurityDetectionService.containsMaliciousPatterns(ipAddress)) {
                    LoggingUtil.warn(logger, "Malicious content detected in ipAddress: {}", ipAddress);
                    throw new AuthenticationException("Invalid ipAddress content");
                }

                Instant now = Instant.now();
                Instant expiration = now.plus(accessTokenExpiration);

                // 生成安全相关的声明
                String activityId = TraceIdUtil.generateTraceId();
                String tokenId = TraceIdUtil.generateTraceId();
                String clientFingerprint = generateClientFingerprint(deviceId, ipAddress);

                String token = Jwts.builder()
                        .issuer(jwtProperties.getIssuer())
                        .audience().add(jwtProperties.getAudience()).and()
                        .subject(String.valueOf(userId))
                        .issuedAt(Date.from(now))
                        .expiration(Date.from(expiration))
                        .claim(SecurityConstants.JWT_CLAIM_USER_ID, userId)
                        .claim(SecurityConstants.JWT_CLAIM_USERNAME, username)
                        .claim(SecurityConstants.JWT_CLAIM_USER_TYPE, userType.name())
                        .claim(SecurityConstants.JWT_CLAIM_AUTHORITIES, authorities)
                        .claim(SecurityConstants.JWT_CLAIM_PERMISSIONS, authorities)
                        .claim(SecurityConstants.JWT_CLAIM_TOKEN_TYPE, SecurityConstants.TOKEN_TYPE_ACCESS)
                        .claim(SecurityConstants.JWT_CLAIM_DEVICE_ID, deviceId)
                        .claim(SecurityConstants.JWT_CLAIM_IP_ADDRESS, ipAddress)
                        // 新增安全声明
                        .claim("activity_id", activityId)
                        .claim("token_id", tokenId)
                        .claim("client_fingerprint", clientFingerprint)
                        .claim("security_level", determineSecurityLevel(userType))
                        .claim("token_version", "2.0")
                        .claim("auth_method", "password")
                        .claim("last_activity", now.getEpochSecond())
                        .signWith(secretKey)
                        .compact();

                LoggingUtil.info(logger,
                        "Generated enhanced access token for user: {}, type: {}, expires at: {}, activity: {}",
                        username, userType, expiration, activityId);
                return token;
            } catch (Exception e) {
                LoggingUtil.error(logger, "Failed to generate access token for user: {}", e, username);
                throw new AuthenticationException("Failed to generate access token");
            }
        });
    }

    /**
     * 生成客户端指纹
     *
     * @param deviceId  设备ID
     * @param ipAddress IP地址
     * @return 客户端指纹
     */
    private String generateClientFingerprint(String deviceId, String ipAddress) {
        String combined = (deviceId != null ? deviceId : "unknown") + "|" +
                (ipAddress != null ? ipAddress : "unknown");
        return java.util.Base64.getEncoder().encodeToString(combined.getBytes());
    }

    /**
     * 确定安全级别
     *
     * @param userType 用户类型
     * @return 安全级别
     */
    private String determineSecurityLevel(UserType userType) {
        switch (userType) {
            case SYSTEM_USER:
                return "HIGH";
            case NORMAL_USER:
                return "MEDIUM";
            case GUEST:
                return "LOW";
            default:
                return "MEDIUM";
        }
    }

    /**
     * 根据用户实体生成访问令牌
     *
     * @param user 用户实体
     * @return 访问令牌的Mono
     */
    public Mono<String> generateToken(com.honyrun.model.entity.business.User user) {
        return Mono.fromCallable(() -> {
            // 验证用户对象
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            // 验证用户名
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                throw new IllegalArgumentException("Username cannot be null or empty");
            }

            // 验证用户类型
            if (user.getUserType() == null) {
                throw new IllegalArgumentException("User type cannot be null");
            }

            return user;
        }).flatMap(validUser -> {
            // 使用同步方法获取默认IP配置
            String defaultIp = unifiedConfigManager.getProperty("honyrun.jwt.default-ip", "127.0.0.1");
            return generateAccessToken(
                    validUser.getId(),
                    validUser.getUsername(),
                    validUser.getUserType(),
                    "USER", // 默认权限
                    "default", // 默认设备ID
                    defaultIp // 默认IP地址
            );
        });
    }

    /**
     * 生成刷新令牌
     *
     * @param userId      用户ID
     * @param username    用户名
     * @param userType    用户类型
     * @param authorities 权限列表
     * @param deviceId    设备ID
     * @return 刷新令牌的Mono
     */
    public Mono<String> generateRefreshToken(Long userId, String username, UserType userType,
            String authorities, String deviceId) {
        return Mono.fromCallable(() -> {
            try {
                // 验证必需参数
                if (username == null || username.trim().isEmpty()) {
                    throw new IllegalArgumentException("Username cannot be null or empty");
                }
                if (userType == null) {
                    throw new IllegalArgumentException("User type cannot be null");
                }

                // 恶意内容检测：用户名、权限字符串与设备ID
                if (unifiedSecurityDetectionService.containsMaliciousPatterns(username)) {
                    LoggingUtil.warn(logger, "Malicious content detected in username: {}", username);
                    throw new AuthenticationException("Invalid username content");
                }
                if (authorities != null && unifiedSecurityDetectionService.containsMaliciousPatterns(authorities)) {
                    LoggingUtil.warn(logger, "Malicious content detected in authorities: {}", authorities);
                    throw new AuthenticationException("Invalid authorities content");
                }
                if (deviceId != null && unifiedSecurityDetectionService.containsMaliciousPatterns(deviceId)) {
                    LoggingUtil.warn(logger, "Malicious content detected in deviceId: {}", deviceId);
                    throw new AuthenticationException("Invalid deviceId content");
                }

                Instant now = Instant.now();
                Instant expiration = now.plus(refreshTokenExpiration);

                // 生成安全相关的声明
                String activityId = TraceIdUtil.generateTraceId();
                String tokenId = TraceIdUtil.generateTraceId();
                String clientFingerprint = generateClientFingerprint(deviceId, "unknown");

                String token = Jwts.builder()
                        .issuer(jwtProperties.getIssuer())
                        .audience().add(jwtProperties.getAudience()).and()
                        .subject(String.valueOf(userId))
                        .issuedAt(Date.from(now))
                        .expiration(Date.from(expiration))
                        .claim(SecurityConstants.JWT_CLAIM_USER_ID, userId)
                        .claim(SecurityConstants.JWT_CLAIM_USERNAME, username)
                        .claim(SecurityConstants.JWT_CLAIM_USER_TYPE, userType.name())
                        .claim(SecurityConstants.JWT_CLAIM_AUTHORITIES, authorities)
                        .claim(SecurityConstants.JWT_CLAIM_PERMISSIONS, authorities)
                        .claim(SecurityConstants.JWT_CLAIM_TOKEN_TYPE, SecurityConstants.TOKEN_TYPE_REFRESH)
                        .claim(SecurityConstants.JWT_CLAIM_DEVICE_ID, deviceId)
                        // 新增安全声明
                        .claim("activity_id", activityId)
                        .claim("token_id", tokenId)
                        .claim("client_fingerprint", clientFingerprint)
                        .claim("security_level", determineSecurityLevel(userType))
                        .claim("token_version", "2.0")
                        .claim("auth_method", "password")
                        .claim("last_activity", now.getEpochSecond())
                        .signWith(secretKey)
                        .compact();

                LoggingUtil.info(logger,
                        "Generated enhanced refresh token for user: {}, type: {}, expires at: {}, activity: {}",
                        username, userType, expiration, activityId);
                return token;
            } catch (Exception e) {
                LoggingUtil.error(logger, "Failed to generate refresh token for user: {}", e, username);
                throw new AuthenticationException("Failed to generate refresh token");
            }
        });
    }

    /**
     * 根据用户实体生成刷新令牌
     *
     * @param user 用户实体
     * @return 刷新令牌的Mono
     */
    public Mono<String> generateRefreshToken(com.honyrun.model.entity.business.User user) {
        return generateRefreshToken(
                user.getId(),
                user.getUsername(),
                user.getUserType(),
                "USER", // 默认权限
                "default" // 默认设备ID
        );
    }

    /**
     * 验证刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 验证结果的Mono
     */
    public Mono<Boolean> validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.just(false);
                    }
                    // 验证是否为刷新令牌类型
                    return getTokenTypeFromToken(refreshToken)
                            .map(tokenType -> SecurityConstants.TOKEN_TYPE_REFRESH.equals(tokenType))
                            .onErrorReturn(false);
                });
    }

    /**
     * 从刷新令牌获取用户信息
     *
     * @param refreshToken 刷新令牌
     * @return 用户实体的Mono
     */
    public Mono<com.honyrun.model.entity.business.User> getUserFromRefreshToken(String refreshToken) {
        return validateRefreshToken(refreshToken)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new AuthenticationException("Invalid refresh token"));
                    }

                    return parseToken(refreshToken)
                            .map(claims -> {
                                com.honyrun.model.entity.business.User user = new com.honyrun.model.entity.business.User();
                                user.setId(claims.get(SecurityConstants.JWT_CLAIM_USER_ID, Long.class));
                                user.setUsername(claims.get(SecurityConstants.JWT_CLAIM_USERNAME, String.class));

                                String userTypeStr = claims.get(SecurityConstants.JWT_CLAIM_USER_TYPE, String.class);
                                if (userTypeStr != null) {
                                    user.setUserType(UserType.valueOf(userTypeStr));
                                }

                                return user;
                            });
                });
    }

    /**
     * 验证令牌（增强版本）
     *
     * @param token JWT令牌
     * @return 验证结果的Mono
     */
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                // 0. 首先检查Token长度，直接拒绝超长Token
                if (token == null || token.trim().isEmpty()) {
                    LoggingUtil.warn(logger, "Token is null or empty");
                    return false;
                }

                // 设置合理的Token长度限制（一般JWT Token不会超过2048字符）
                final int MAX_TOKEN_LENGTH = 2048;
                if (token.length() > MAX_TOKEN_LENGTH) {
                    LoggingUtil.warn(logger, "Token length {} exceeds maximum allowed length {}, rejecting token",
                            token.length(), MAX_TOKEN_LENGTH);
                    return false;
                }

                // 1. 检查token是否包含恶意模式
                LoggingUtil.debug(logger, "检查Token恶意模式");
                if (unifiedSecurityDetectionService.containsMaliciousPatterns(token)) {
                    LoggingUtil.warn(logger, "Token contains malicious patterns, validation failed");
                    return false;
                }

                // 2. 解析并验证JWT
                Claims claims = Jwts.parser()
                        .verifyWith(secretKey)
                        .requireIssuer(jwtProperties.getIssuer())
                        .requireAudience(jwtProperties.getAudience())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // 3. 增强的subject验证 - 灵活验证策略
                if (!validateSubject(claims)) {
                    LoggingUtil.warn(logger, "Subject validation failed");
                    return false;
                }

                // 4. 验证token类型
                if (!validateTokenType(claims)) {
                    LoggingUtil.warn(logger, "Token type validation failed");
                    return false;
                }

                // 5. 验证用户类型
                if (!validateUserType(claims)) {
                    LoggingUtil.warn(logger, "User type validation failed");
                    return false;
                }

                // 6. 验证设备ID（如果存在）
                if (!validateDeviceId(claims)) {
                    LoggingUtil.warn(logger, "Device ID validation failed");
                    return false;
                }

                LoggingUtil.debug(logger, "Token validation successful");
                return true;
            } catch (ExpiredJwtException e) {
                LoggingUtil.warn(logger, "Token has expired: {}", e.getMessage());
                return false;
            } catch (UnsupportedJwtException e) {
                LoggingUtil.warn(logger, "Unsupported JWT token: {}", e.getMessage());
                return false;
            } catch (MalformedJwtException e) {
                LoggingUtil.warn(logger, "Malformed JWT token: {}", e.getMessage());
                return false;
            } catch (SecurityException e) {
                LoggingUtil.warn(logger, "Invalid JWT signature: {}", e.getMessage());
                return false;
            } catch (IllegalArgumentException e) {
                LoggingUtil.warn(logger, "JWT token compact of handler are invalid: {}", e.getMessage());
                return false;
            } catch (Exception e) {
                LoggingUtil.error(logger, "Token validation failed", e);
                return false;
            }
        });
    }

    /**
     * 验证Subject - 灵活验证策略
     *
     * @param claims JWT声明
     * @return 验证结果
     */
    private boolean validateSubject(Claims claims) {
        String subject = claims.getSubject();

        // Subject不能为空
        if (subject == null || subject.trim().isEmpty()) {
            LoggingUtil.warn(logger, "Subject is null or empty");
            return false;
        }

        // Subject应该是用户ID，验证其为有效的数字格式
        try {
            Long userId = Long.parseLong(subject);
            if (userId <= 0) {
                LoggingUtil.warn(logger, "Invalid user ID in subject: {}", subject);
                return false;
            }

            // 验证subject与claims中的user_id一致
            Object userIdClaim = claims.get(SecurityConstants.JWT_CLAIM_USER_ID);
            if (userIdClaim != null && !userId.equals(((Number) userIdClaim).longValue())) {
                LoggingUtil.warn(logger, "Subject user ID mismatch: subject={}, claim={}", userId, userIdClaim);
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            LoggingUtil.warn(logger, "Subject is not a valid user ID: {}", subject);
            return false;
        }
    }

    /**
     * 验证Token类型
     *
     * @param claims JWT声明
     * @return 验证结果
     */
    private boolean validateTokenType(Claims claims) {
        String tokenType = (String) claims.get("tokenType");

        if (tokenType == null) {
            LoggingUtil.debug(logger, "Token type not specified, assuming access token");
            return true; // 兼容旧版本token
        }

        // 验证token类型是否有效 - 使用SecurityConstants中定义的常量
        if (!SecurityConstants.TOKEN_TYPE_ACCESS.equals(tokenType)
                && !SecurityConstants.TOKEN_TYPE_REFRESH.equals(tokenType)) {
            LoggingUtil.warn(logger, "Invalid token type: {}", tokenType);
            return false;
        }

        return true;
    }

    /**
     * 验证用户类型
     *
     * @param claims JWT声明
     * @return 验证结果
     */
    private boolean validateUserType(Claims claims) {
        String userTypeStr = (String) claims.get("userType");

        if (userTypeStr == null) {
            LoggingUtil.warn(logger, "User type not specified in token");
            return false;
        }

        try {
            UserType userType = UserType.valueOf(userTypeStr);
            // 验证用户类型是否有效
            if (userType != UserType.SYSTEM_USER && userType != UserType.NORMAL_USER && userType != UserType.GUEST) {
                LoggingUtil.warn(logger, "Invalid user type: {}", userType);
                return false;
            }
        } catch (IllegalArgumentException e) {
            LoggingUtil.warn(logger, "Invalid user type format: {}", userTypeStr);
            return false;
        }

        return true;
    }

    /**
     * 验证设备ID
     *
     * @param claims JWT声明
     * @return 验证结果
     */
    private boolean validateDeviceId(Claims claims) {
        String deviceId = (String) claims.get("deviceId");

        if (deviceId == null) {
            LoggingUtil.debug(logger, "Device ID not specified in token");
            return true; // 设备ID是可选的
        }

        // 设备ID长度检查
        if (deviceId.length() > 100) {
            LoggingUtil.warn(logger, "Device ID too long: {}", deviceId.length());
            return false;
        }

        // 设备ID恶意模式检查
        if (unifiedSecurityDetectionService.containsMaliciousPatterns(deviceId)) {
            LoggingUtil.warn(logger, "Device ID contains malicious patterns: {}", deviceId);
            return false;
        }

        return true;
    }

    /**
     * 解析令牌获取声明
     *
     * @param token JWT令牌
     * @return 声明的Mono
     */
    public Mono<Claims> parseToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(secretKey)
                        .requireIssuer(jwtProperties.getIssuer())
                        .requireAudience(jwtProperties.getAudience())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                LoggingUtil.debug(logger, "Token parsed successfully, user: {}",
                        claims.get(SecurityConstants.JWT_CLAIM_USERNAME));
                return claims;
            } catch (Exception e) {
                LoggingUtil.error(logger, "Failed to parse token", e);
                throw new AuthenticationException("Failed to parse token");
            }
        });
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID的Mono
     */
    public Mono<Long> getUserIdFromToken(String token) {
        return parseToken(token)
                .map(claims -> {
                    Object userIdObj = claims.get(SecurityConstants.JWT_CLAIM_USER_ID);
                    if (userIdObj instanceof Integer) {
                        return ((Integer) userIdObj).longValue();
                    } else if (userIdObj instanceof Long) {
                        return (Long) userIdObj;
                    } else {
                        throw new AuthenticationException("Invalid user ID in token");
                    }
                });
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名的Mono
     */
    public Mono<String> getUsernameFromToken(String token) {
        return parseToken(token)
                .map(claims -> claims.get(SecurityConstants.JWT_CLAIM_USERNAME, String.class));
    }

    /**
     * 从令牌中获取用户类型
     *
     * @param token JWT令牌
     * @return 用户类型的Mono
     */
    public Mono<UserType> getUserTypeFromToken(String token) {
        return parseToken(token)
                .map(claims -> {
                    String userTypeStr = claims.get(SecurityConstants.JWT_CLAIM_USER_TYPE, String.class);
                    return UserType.valueOf(userTypeStr);
                });
    }

    /**
     * 从令牌中获取权限
     *
     * @param token JWT令牌
     * @return 权限集合的Mono
     */
    public Mono<java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>> getAuthoritiesFromToken(
            String token) {
        return parseToken(token)
                .map(claims -> {
                    String authoritiesStr = claims.get(SecurityConstants.JWT_CLAIM_AUTHORITIES, String.class);
                    if (authoritiesStr == null || authoritiesStr.trim().isEmpty()) {
                        return java.util.Collections.emptyList();
                    }

                    // 将逗号分隔的权限字符串转换为GrantedAuthority集合
                    return java.util.Arrays.stream(authoritiesStr.split(","))
                            .map(String::trim)
                            .filter(auth -> !auth.isEmpty())
                            .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                });
    }

    /**
     * 从令牌中获取权限字符串（原始方法保留用于兼容性）
     *
     * @param token JWT令牌
     * @return 权限字符串的Mono
     */
    public Mono<String> getAuthoritiesStringFromToken(String token) {
        return parseToken(token)
                .map(claims -> claims.get(SecurityConstants.JWT_CLAIM_AUTHORITIES, String.class));
    }

    /**
     * 从令牌中获取权限（permissions claim）
     *
     * @param token JWT令牌
     * @return 权限的Mono
     */
    public Mono<String> getPermissionsFromToken(String token) {
        return parseToken(token)
                .map(claims -> claims.get(SecurityConstants.JWT_CLAIM_PERMISSIONS, String.class));
    }

    /**
     * 从令牌中获取令牌类型
     *
     * @param token JWT令牌
     * @return 令牌类型的Mono
     */
    public Mono<String> getTokenTypeFromToken(String token) {
        return parseToken(token)
                .map(claims -> claims.get(SecurityConstants.JWT_CLAIM_TOKEN_TYPE, String.class));
    }

    /**
     * 从令牌中获取设备ID
     *
     * @param token JWT令牌
     * @return 设备ID的Mono
     */
    public Mono<String> getDeviceIdFromToken(String token) {
        return parseToken(token)
                .map(claims -> claims.get(SecurityConstants.JWT_CLAIM_DEVICE_ID, String.class));
    }

    /**
     * 从令牌中获取IP地址
     *
     * @param token JWT令牌
     * @return IP地址的Mono
     */
    public Mono<String> getIpAddressFromToken(String token) {
        return parseToken(token)
                .map(claims -> claims.get(SecurityConstants.JWT_CLAIM_IP_ADDRESS, String.class));
    }

    /**
     * 获取安全检测配置
     *
     * @return 安全检测配置
     */
    public SecurityDetectionProperties getSecurityDetectionProperties() {
        return securityDetectionProperties;
    }

    /**
     * 检查令牌是否过期
     *
     * @param token JWT令牌
     * @return 是否过期的Mono
     */
    public Mono<Boolean> isTokenExpired(String token) {
        return parseToken(token)
                .map(claims -> {
                    Date expiration = claims.getExpiration();
                    return expiration.before(new Date());
                })
                .onErrorReturn(true);
    }

    /**
     * 获取令牌剩余有效时间
     *
     * @param token JWT令牌
     * @return 剩余有效时间的Mono（秒）
     */
    public Mono<Long> getTokenRemainingTime(String token) {
        return parseToken(token)
                .map(claims -> {
                    Date expiration = claims.getExpiration();
                    long remainingTime = (expiration.getTime() - System.currentTimeMillis()) / 1000;
                    return Math.max(0, remainingTime);
                })
                .onErrorReturn(0L);
    }

    /**
     * 从令牌中提取用户名
     *
     * @param token JWT令牌
     * @return 用户名的Mono
     */
    public Mono<String> extractUsername(String token) {
        return getUsernameFromToken(token);
    }

    /**
     * 从令牌中提取用户类型
     *
     * @param token JWT令牌
     * @return 用户类型的Mono
     */
    public Mono<UserType> extractUserType(String token) {
        return getUserTypeFromToken(token);
    }

    /**
     * 从令牌中提取Claims
     *
     * @param token JWT令牌
     * @return Claims的Mono
     */
    public Mono<Claims> extractClaims(String token) {
        return parseToken(token);
    }

    /**
     * 将令牌添加到黑名单
     * 符合WebFlux规范：当tokenBlacklistService为null时返回Mono.just(false)，避免null异常
     *
     * @param token  JWT令牌
     * @param reason 加入黑名单的原因
     * @return 操作结果的Mono
     */
    public Mono<Boolean> addToBlacklist(String token, String reason) {
        if (token == null || token.trim().isEmpty()) {
            LoggingUtil.warn(logger, "Token is null or empty, cannot add to blacklist");
            return Mono.just(false);
        }

        // 符合WebFlux规范：当tokenBlacklistService为null时，返回Mono.just(false)而不是抛出异常
        if (tokenBlacklistService == null) {
            LoggingUtil.debug(logger,
                    "TokenBlacklistService is not available, skipping blacklist operation for token: {}",
                    maskToken(token));
            return Mono.just(false);
        }

        final String finalReason = (reason == null || reason.trim().isEmpty()) ? "No reason provided" : reason;

        // 使用JWT配置中的黑名单缓存时间，如果未配置则使用默认值
        long expireSeconds = jwtProperties.getBlacklistCacheTime() != null
                ? jwtProperties.getBlacklistCacheTime()
                : 3600L; // 默认1小时

        return tokenBlacklistService.addToBlacklist(token, finalReason, expireSeconds)
                .doOnNext(result -> {
                    if (result) {
                        LoggingUtil.info(logger, "Token successfully added to blacklist: {}, reason: {}",
                                maskToken(token), finalReason);
                    } else {
                        LoggingUtil.warn(logger, "Failed to add token to blacklist: {}, reason: {}",
                                maskToken(token), finalReason);
                    }
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Error adding token to blacklist: {}, reason: {}, error: {}",
                            maskToken(token), finalReason, error.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * 检查令牌是否在黑名单中
     * 符合WebFlux规范：当tokenBlacklistService为null时返回Mono.just(false)，避免null异常
     *
     * @param token JWT令牌
     * @return 是否在黑名单中
     */
    public Mono<Boolean> isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            LoggingUtil.warn(logger, "Token is null or empty, returning false for blacklist check");
            return Mono.just(false);
        }

        // 符合WebFlux规范：当tokenBlacklistService为null时，返回Mono.just(false)而不是抛出异常
        if (tokenBlacklistService == null) {
            LoggingUtil.debug(logger, "TokenBlacklistService is not available, skipping blacklist check for token: {}",
                    maskToken(token));
            return Mono.just(false);
        }

        return tokenBlacklistService.isTokenBlacklisted(token)
                .doOnNext(isBlacklisted -> {
                    if (isBlacklisted) {
                        LoggingUtil.warn(logger, "Token is blacklisted: {}", maskToken(token));
                    } else {
                        LoggingUtil.debug(logger, "Token is not blacklisted: {}", maskToken(token));
                    }
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Error checking token blacklist status: {}", error.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * 生成测试令牌
     * 用于健康检查和测试环境
     *
     * @return 测试JWT令牌
     */
    public String generateTestToken() {
        try {
            // 注意：在测试token生成中使用.block()是必要的，因为这是同步方法需要返回String
            String defaultIp = unifiedConfigManager.getProperty("honyrun.jwt.default-ip", "127.0.0.1");
            return generateAccessToken(
                    1L,
                    "test-user",
                    UserType.NORMAL_USER,
                    "USER_READ",
                    "test-device",
                    defaultIp).block();
        } catch (Exception e) {
            LoggingUtil.error(logger, "Failed to generate test token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 掩码令牌用于日志记录
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
