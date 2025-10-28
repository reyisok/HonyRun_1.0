package com.honyrun.security.jwt;

/**
 * 令牌对类，包含访问令牌和刷新令牌
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-18 05:16:00
 * @version 1.0.0
 */
public class TokenPair {

    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;

    /**
     * 构造函数
     *
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @param expiresIn 过期时间（秒）
     */
    public TokenPair(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    /**
     * 获取访问令牌
     *
     * @return 访问令牌
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * 获取刷新令牌
     *
     * @return 刷新令牌
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * 获取过期时间
     *
     * @return 过期时间（秒）
     */
    public long getExpiresIn() {
        return expiresIn;
    }
}
