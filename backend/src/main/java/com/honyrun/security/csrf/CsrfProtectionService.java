package com.honyrun.security.csrf;

import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * CSRF防护服务接口
 * 
 * 提供CSRF攻击防护功能：
 * - CSRF令牌生成和验证
 * - 请求来源验证
 * - 双重提交Cookie验证
 * - 同源策略检查
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01 16:30:00
 * @modified 2025-07-01 16:30:00
 * @version 1.0.0
 */
public interface CsrfProtectionService {

    /**
     * 生成CSRF令牌
     * 
     * @param sessionId 会话ID
     * @return CSRF令牌
     */
    Mono<String> generateCsrfToken(String sessionId);

    /**
     * 验证CSRF令牌
     * 
     * @param token 提交的令牌
     * @param sessionId 会话ID
     * @return 验证结果
     */
    Mono<CsrfValidationResult> validateCsrfToken(String token, String sessionId);

    /**
     * 验证请求来源
     * 
     * @param origin 请求来源
     * @param referer 请求引用页
     * @return 验证结果
     */
    Mono<OriginValidationResult> validateOrigin(String origin, String referer);

    /**
     * 生成双重提交Cookie
     * 
     * @param sessionId 会话ID
     * @return Cookie令牌
     */
    Mono<String> generateDoubleCookieToken(String sessionId);

    /**
     * 验证双重提交Cookie
     * 
     * @param cookieToken Cookie中的令牌
     * @param headerToken 请求头中的令牌
     * @param sessionId 会话ID
     * @return 验证结果
     */
    Mono<DoubleCookieValidationResult> validateDoubleCookie(String cookieToken, String headerToken, String sessionId);

    /**
     * 检查同源策略
     * 
     * @param origin 请求来源
     * @param allowedOrigins 允许的来源列表
     * @return 检查结果
     */
    Mono<SameOriginResult> checkSameOrigin(String origin, String[] allowedOrigins);

    /**
     * 刷新CSRF令牌
     * 
     * @param oldToken 旧令牌
     * @param sessionId 会话ID
     * @return 新令牌
     */
    Mono<String> refreshCsrfToken(String oldToken, String sessionId);

    /**
     * 清理过期令牌
     * 
     * @return 清理结果
     */
    Mono<CleanupResult> cleanupExpiredTokens();

    /**
     * 配置CSRF防护策略
     * 
     * @param config 防护配置
     * @return 配置结果
     */
    Mono<Void> configureProtection(CsrfProtectionConfig config);

    /**
     * CSRF验证结果
     */
    class CsrfValidationResult {
        private final boolean isValid;
        private final String message;
        private final CsrfValidationStatus status;
        private final LocalDateTime validatedAt;

        public CsrfValidationResult(boolean isValid, String message, CsrfValidationStatus status, LocalDateTime validatedAt) {
            this.isValid = isValid;
            this.message = message;
            this.status = status;
            this.validatedAt = validatedAt;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getMessage() {
            return message;
        }

        public CsrfValidationStatus getStatus() {
            return status;
        }

        public LocalDateTime getValidatedAt() {
            return validatedAt;
        }
    }

    /**
     * 来源验证结果
     */
    class OriginValidationResult {
        private final boolean isValid;
        private final String message;
        private final String validatedOrigin;
        private final OriginValidationStatus status;

        public OriginValidationResult(boolean isValid, String message, String validatedOrigin, OriginValidationStatus status) {
            this.isValid = isValid;
            this.message = message;
            this.validatedOrigin = validatedOrigin;
            this.status = status;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getMessage() {
            return message;
        }

        public String getValidatedOrigin() {
            return validatedOrigin;
        }

        public OriginValidationStatus getStatus() {
            return status;
        }
    }

    /**
     * 双重Cookie验证结果
     */
    class DoubleCookieValidationResult {
        private final boolean isValid;
        private final String message;
        private final DoubleCookieValidationStatus status;
        private final boolean cookieMatches;
        private final boolean headerMatches;

        public DoubleCookieValidationResult(boolean isValid, String message, DoubleCookieValidationStatus status, 
                                          boolean cookieMatches, boolean headerMatches) {
            this.isValid = isValid;
            this.message = message;
            this.status = status;
            this.cookieMatches = cookieMatches;
            this.headerMatches = headerMatches;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getMessage() {
            return message;
        }

        public DoubleCookieValidationStatus getStatus() {
            return status;
        }

        public boolean isCookieMatches() {
            return cookieMatches;
        }

        public boolean isHeaderMatches() {
            return headerMatches;
        }
    }

    /**
     * 同源检查结果
     */
    class SameOriginResult {
        private final boolean isSameOrigin;
        private final String message;
        private final String checkedOrigin;
        private final String matchedOrigin;

        public SameOriginResult(boolean isSameOrigin, String message, String checkedOrigin, String matchedOrigin) {
            this.isSameOrigin = isSameOrigin;
            this.message = message;
            this.checkedOrigin = checkedOrigin;
            this.matchedOrigin = matchedOrigin;
        }

        public boolean isSameOrigin() {
            return isSameOrigin;
        }

        public String getMessage() {
            return message;
        }

        public String getCheckedOrigin() {
            return checkedOrigin;
        }

        public String getMatchedOrigin() {
            return matchedOrigin;
        }
    }

    /**
     * 清理结果
     */
    class CleanupResult {
        private final int cleanedCount;
        private final String message;
        private final LocalDateTime cleanupTime;

        public CleanupResult(int cleanedCount, String message, LocalDateTime cleanupTime) {
            this.cleanedCount = cleanedCount;
            this.message = message;
            this.cleanupTime = cleanupTime;
        }

        public int getCleanedCount() {
            return cleanedCount;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getCleanupTime() {
            return cleanupTime;
        }
    }

    /**
     * CSRF防护配置
     */
    class CsrfProtectionConfig {
        private boolean enableCsrfProtection = true;
        private boolean enableOriginValidation = true;
        private boolean enableDoubleCookie = false;
        private boolean enableSameOriginCheck = true;
        private int tokenExpirationMinutes = 30;
        private String[] allowedOrigins = {}; // 从配置文件读取，不使用硬编码
        private String tokenHeaderName = "X-CSRF-Token";
        private String cookieName = "CSRF-TOKEN";
        private boolean secureCookie = true;
        private boolean httpOnlyCookie = false;

        public boolean isEnableCsrfProtection() {
            return enableCsrfProtection;
        }

        public void setEnableCsrfProtection(boolean enableCsrfProtection) {
            this.enableCsrfProtection = enableCsrfProtection;
        }

        public boolean isEnableOriginValidation() {
            return enableOriginValidation;
        }

        public void setEnableOriginValidation(boolean enableOriginValidation) {
            this.enableOriginValidation = enableOriginValidation;
        }

        public boolean isEnableDoubleCookie() {
            return enableDoubleCookie;
        }

        public void setEnableDoubleCookie(boolean enableDoubleCookie) {
            this.enableDoubleCookie = enableDoubleCookie;
        }

        public boolean isEnableSameOriginCheck() {
            return enableSameOriginCheck;
        }

        public void setEnableSameOriginCheck(boolean enableSameOriginCheck) {
            this.enableSameOriginCheck = enableSameOriginCheck;
        }

        public int getTokenExpirationMinutes() {
            return tokenExpirationMinutes;
        }

        public void setTokenExpirationMinutes(int tokenExpirationMinutes) {
            this.tokenExpirationMinutes = tokenExpirationMinutes;
        }

        public String[] getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String[] allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public String getTokenHeaderName() {
            return tokenHeaderName;
        }

        public void setTokenHeaderName(String tokenHeaderName) {
            this.tokenHeaderName = tokenHeaderName;
        }

        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
        }

        public boolean isSecureCookie() {
            return secureCookie;
        }

        public void setSecureCookie(boolean secureCookie) {
            this.secureCookie = secureCookie;
        }

        public boolean isHttpOnlyCookie() {
            return httpOnlyCookie;
        }

        public void setHttpOnlyCookie(boolean httpOnlyCookie) {
            this.httpOnlyCookie = httpOnlyCookie;
        }
    }

    /**
     * CSRF验证状态
     */
    enum CsrfValidationStatus {
        VALID("有效"),
        INVALID_TOKEN("无效令牌"),
        EXPIRED_TOKEN("令牌已过期"),
        MISSING_TOKEN("缺少令牌"),
        SESSION_MISMATCH("会话不匹配");

        private final String description;

        CsrfValidationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 来源验证状态
     */
    enum OriginValidationStatus {
        VALID("有效来源"),
        INVALID_ORIGIN("无效来源"),
        MISSING_ORIGIN("缺少来源信息"),
        BLOCKED_ORIGIN("被阻止的来源");

        private final String description;

        OriginValidationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 双重Cookie验证状态
     */
    enum DoubleCookieValidationStatus {
        VALID("验证通过"),
        COOKIE_MISMATCH("Cookie不匹配"),
        HEADER_MISSING("请求头缺失"),
        COOKIE_MISSING("Cookie缺失"),
        TOKEN_MISMATCH("令牌不匹配");

        private final String description;

        DoubleCookieValidationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}

