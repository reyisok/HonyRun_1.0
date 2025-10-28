package com.honyrun.security.jwt;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.honyrun.constant.SecurityConstants;
import com.honyrun.config.properties.JwtProperties;
import org.springframework.security.authentication.BadCredentialsException;
import com.honyrun.util.LoggingUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import reactor.core.publisher.Mono;

/**
 * 响应式JWT解码器
 *
 * @author: Mr.Rey
 * @created: 2025-07-01 16:00:00
 * @modified: 2025-01-30 17:30:00
 * @version: 2.1.0 - 使用JwtProperties外部化配置
 */
@Component
public class ReactiveJwtDecoder {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveJwtDecoder.class);
    
    private final JwtProperties jwtProperties;

    /**
     * 构造函数
     *
     * @param jwtProperties JWT配置属性
     */
    public ReactiveJwtDecoder(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * 解码JWT令牌
     *
     * @param token JWT令牌
     * @return 用户信息的Mono
     */
    public Mono<JwtUserInfo> decode(String token) {
        LoggingUtil.info(logger, "Starting JWT token decoding for token: {}...{}",
                token.substring(0, Math.min(10, token.length())),
                token.substring(Math.max(0, token.length() - 10)));

        return Mono.fromCallable(() -> {
            try {
                LoggingUtil.debug(logger, "Parsing JWT claims");

                Claims claims = Jwts.parser()
                        .verifyWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()))
                        .requireIssuer(jwtProperties.getIssuer())
                        .requireAudience(jwtProperties.getAudience())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                LoggingUtil.debug(logger, "JWT claims parsed successfully, subject: {}, expiration: {}",
                        claims.getSubject(), claims.getExpiration());

                // 验证令牌
                if (!validateClaims(claims)) {
                    LoggingUtil.warn(logger, "JWT claims validation failed for token: {}", token.substring(0, 10));
                    throw new BadCredentialsException("Invalid JWT claims");
                }

                return extractUserInfo(claims);
            } catch (SignatureException e) {
                LoggingUtil.warn(logger, "Invalid JWT signature: {}", e.getMessage());
                throw new BadCredentialsException("Invalid JWT signature");
            } catch (MalformedJwtException e) {
                LoggingUtil.warn(logger, "Invalid JWT token format: {}", e.getMessage());
                throw new BadCredentialsException("Invalid JWT token format");
            } catch (ExpiredJwtException e) {
                LoggingUtil.warn(logger, "JWT token has expired: {}", e.getMessage());
                throw new BadCredentialsException("JWT token has expired");
            } catch (UnsupportedJwtException e) {
                LoggingUtil.warn(logger, "Unsupported JWT token: {}", e.getMessage());
                throw new BadCredentialsException("Unsupported JWT token");
            } catch (IllegalArgumentException e) {
                LoggingUtil.warn(logger, "JWT claims string is empty: {}", e.getMessage());
                throw new BadCredentialsException("JWT claims string is empty");
            } catch (Exception e) {
                LoggingUtil.error(logger, "JWT token decoding failed", e);
                throw new BadCredentialsException("JWT token decoding failed");
            }
        });
    }

    /**
     * 验证JWT令牌
     *
     * @param token JWT令牌
     * @return 验证结果的Mono
     */
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                LoggingUtil.debug(logger, "Validating JWT token");

                Claims claims = Jwts.parser()
                        .verifyWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()))
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                LoggingUtil.debug(logger, "JWT token validation successful");
                return validateClaims(claims);
            } catch (SignatureException e) {
                LoggingUtil.warn(logger, "Invalid JWT signature: {}", e.getMessage());
                return false;
            } catch (MalformedJwtException e) {
                LoggingUtil.warn(logger, "Invalid JWT token format: {}", e.getMessage());
                return false;
            } catch (ExpiredJwtException e) {
                LoggingUtil.warn(logger, "JWT token has expired: {}", e.getMessage());
                return false;
            } catch (UnsupportedJwtException e) {
                LoggingUtil.warn(logger, "Unsupported JWT token: {}", e.getMessage());
                return false;
            } catch (IllegalArgumentException e) {
                LoggingUtil.warn(logger, "JWT claims string is empty: {}", e.getMessage());
                return false;
            } catch (Exception e) {
                LoggingUtil.error(logger, "JWT token validation error", e);
                return false;
            }
        });
    }

    /**
     * 验证JWT声明
     *
     * @param claims JWT声明
     * @return 验证结果
     */
    private boolean validateClaims(Claims claims) {
        // 验证过期时间
        if (!validateExpiration(claims)) {
            return false;
        }

        // 验证发行者
        if (!validateIssuer(claims)) {
            return false;
        }

        // 验证受众
        if (!validateAudience(claims)) {
            return false;
        }

        // 验证主题
        if (!validateSubject(claims)) {
            return false;
        }

        return true;
    }

    /**
     * 验证令牌过期时间
     *
     * @param claims JWT声明
     * @return 验证结果
     */
    private boolean validateExpiration(Claims claims) {
        Date expiration = claims.getExpiration();
        Date now = new Date();

        if (expiration == null) {
            LoggingUtil.debug(logger, "JWT token has no expiration time, current time: {}", now);
            return false;
        }

        if (expiration.before(now)) {
            LoggingUtil.warn(logger, "JWT token has expired: expiration={}, current={}", expiration, now);
            return false;
        }

        LoggingUtil.debug(logger, "JWT token expiration validation passed: expiration={}, current={}", expiration, now);
        return true;
    }

    /**
     * 验证发行者
     *
     * @param claims JWT声明
     * @return 验证结果
     */
    private boolean validateIssuer(Claims claims) {
        String issuer = claims.getIssuer();
        String expectedIssuer = jwtProperties.getIssuer();

        if (issuer == null || !issuer.equals(expectedIssuer)) {
            LoggingUtil.debug(logger, "JWT issuer validation failed: expected={}, actual={}", expectedIssuer, issuer);
            return false;
        }

        LoggingUtil.debug(logger, "JWT issuer validation passed: {}", expectedIssuer);
        return true;
    }

    /**
     * 验证受众
     *
     * @param claims JWT声明
     * @return 验证结果
     */
    private boolean validateAudience(Claims claims) {
        Set<String> audience = claims.getAudience();
        String expectedAudience = jwtProperties.getAudience();

        if (audience == null || !audience.contains(expectedAudience)) {
            LoggingUtil.warn(logger, "JWT audience validation failed: expected={}, actual={}", expectedAudience,
                    audience);
            return false;
        }

        LoggingUtil.debug(logger, "JWT audience validation passed: {}", expectedAudience);
        return true;
    }

    /**
     * 验证主题
     *
     * @param claims JWT声明
     * @return 验证结果
     */
    private boolean validateSubject(Claims claims) {
        String subject = claims.getSubject();

        // 验证subject不为空
        if (subject == null || subject.trim().isEmpty()) {
            LoggingUtil.warn(logger, "JWT subject validation failed: subject is null or empty");
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
            
            LoggingUtil.debug(logger, "JWT subject validation passed: {}", subject);
            return true;
        } catch (NumberFormatException e) {
            LoggingUtil.warn(logger, "Subject is not a valid user ID: {}", subject);
            return false;
        }
    }

    /**
     * 从JWT声明中提取用户信息
     * 【权限获取】：基于用户类型直接获取权限，无需角色中间层
     * 权限应该从数据库动态加载，而非从token静态解析
     *
     * @param claims JWT声明
     * @return 用户信息
     */
    private JwtUserInfo extractUserInfo(Claims claims) {
        Long userId = claims.get(SecurityConstants.JWT_CLAIM_USER_ID, Long.class);
        String username = claims.get(SecurityConstants.JWT_CLAIM_USERNAME, String.class);
        String userType = claims.get(SecurityConstants.JWT_CLAIM_USER_TYPE, String.class);
        
        // 【修复】：权限不再从token中静态解析，而是通过用户类型动态获取
        // 符合《后端详细规划设计-WebFlux.md》和《统一用户类型和直接权限模型规范.md》
        // JWT token仅存储基础用户信息，权限通过ReactiveAuthService动态加载
        Collection<? extends GrantedAuthority> authorities = null; // 权限将通过数据库动态加载

        String deviceId = claims.get(SecurityConstants.JWT_CLAIM_DEVICE_ID, String.class);
        String ipAddress = claims.get(SecurityConstants.JWT_CLAIM_IP_ADDRESS, String.class);
        String activityId = claims.get("activity_id", String.class);

        return new JwtUserInfo(userId, username, userType, authorities, deviceId, ipAddress, activityId);
    }

    /**
     * JWT用户信息类
     * 封装从JWT令牌中解析出的用户信息
     * 
     * 【重要】：权限信息不再从JWT token中静态解析，而是通过数据库动态加载
     * 这符合《后端详细规划设计-WebFlux.md》和《统一用户类型和直接权限模型规范.md》的要求
     */
    public static class JwtUserInfo {
        private final Long userId;
        private final String username;
        private final String userType;
        private final Collection<? extends GrantedAuthority> authorities;
        // 移除静态权限存储 - 权限应通过数据库动态加载
        // private final Set<String> permissions; // 已移除：违反动态权限加载原则
        private final String deviceId;
        private final String ipAddress;
        private final String activityId;

        public JwtUserInfo(Long userId, String username, String userType,
                Collection<? extends GrantedAuthority> authorities, String deviceId, String ipAddress, String activityId) {
            this.userId = userId;
            this.username = username;
            this.userType = userType;
            this.authorities = authorities;
            this.deviceId = deviceId;
            this.ipAddress = ipAddress;
            this.activityId = activityId;
        }

        // Getters
        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getUserType() {
            return userType;
        }

        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        // 移除getPermissions()方法 - 权限应通过数据库动态加载
        // public Set<String> getPermissions() { return permissions; } // 已移除

        public String getDeviceId() {
            return deviceId;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getActivityId() {
            return activityId;
        }

        @Override
        public String toString() {
            return String.format("JwtUserInfo{userId=%d, username='%s', userType='%s', activityId='%s'}",
                    userId, username, userType, activityId);
        }
    }
}

