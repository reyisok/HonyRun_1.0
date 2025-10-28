package com.honyrun.security.jwt;

/**
 * JWT验证结果类
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-18 05:15:00
 * @version 1.0.0
 */
public class JwtValidationResult {

    private final boolean valid;
    private final String errorMessage;
    private final String token;

    /**
     * 构造函数
     *
     * @param valid 是否有效
     * @param errorMessage 错误信息
     * @param token JWT令牌
     */
    public JwtValidationResult(boolean valid, String errorMessage, String token) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.token = token;
    }

    /**
     * 创建有效结果
     *
     * @param token JWT令牌
     * @return 验证结果
     */
    public static JwtValidationResult valid(String token) {
        return new JwtValidationResult(true, null, token);
    }

    /**
     * 创建无效结果
     *
     * @param errorMessage 错误信息
     * @return 验证结果
     */
    public static JwtValidationResult invalid(String errorMessage) {
        return new JwtValidationResult(false, errorMessage, null);
    }

    /**
     * 是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * 获取错误信息
     *
     * @return 错误信息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 获取JWT令牌
     *
     * @return JWT令牌
     */
    public String getToken() {
        return token;
    }
}
