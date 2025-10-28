package com.honyrun.exception;

/**
 * 认证异常类
 * 实现认证失败、权限不足、令牌异常
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class AuthenticationException extends RuntimeException {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final ErrorCode errorCode;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 用户名
     */
    private String username;

    /**
     * 令牌信息
     */
    private String token;

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     */
    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     */
    public AuthenticationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param cause 原始异常
     */
    public AuthenticationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public AuthenticationException(String message) {
        super(message);
        this.errorCode = ErrorCode.AUTHENTICATION_FAILED;
    }

    /**
     * 创建认证失败异常
     *
     * @param username 用户名
     * @return 认证异常实例
     */
    public static AuthenticationException authenticationFailed(String username) {
        AuthenticationException exception = new AuthenticationException(ErrorCode.AUTHENTICATION_FAILED,
                "用户认证失败");
        exception.setUsername(username);
        return exception;
    }

    /**
     * 创建无效凭据异常
     *
     * @param username 用户名
     * @return 认证异常实例
     */
    public static AuthenticationException invalidCredentials(String username) {
        AuthenticationException exception = new AuthenticationException(ErrorCode.INVALID_CREDENTIALS,
                "用户名或密码错误");
        exception.setUsername(username);
        return exception;
    }

    /**
     * 创建无效令牌异常
     *
     * @param token 令牌
     * @return 认证异常实例
     */
    public static AuthenticationException invalidToken(String token) {
        AuthenticationException exception = new AuthenticationException(ErrorCode.INVALID_TOKEN,
                "无效的访问令牌");
        exception.setToken(token);
        return exception;
    }

    /**
     * 创建令牌过期异常
     *
     * @param token 令牌
     * @return 认证异常实例
     */
    public static AuthenticationException tokenExpired(String token) {
        AuthenticationException exception = new AuthenticationException(ErrorCode.TOKEN_EXPIRED,
                "访问令牌已过期");
        exception.setToken(token);
        return exception;
    }

    /**
     * 创建令牌被禁用异常
     *
     * @param token 令牌
     * @return 认证异常实例
     */
    public static AuthenticationException tokenBlacklisted(String token) {
        AuthenticationException exception = new AuthenticationException(ErrorCode.TOKEN_BLACKLISTED,
                "访问令牌已被禁用");
        exception.setToken(token);
        return exception;
    }

    /**
     * 创建账户锁定异常
     *
     * @param username 用户名
     * @return 认证异常实例
     */
    public static AuthenticationException accountLocked(String username) {
        AuthenticationException exception = new AuthenticationException(ErrorCode.ACCOUNT_LOCKED,
                "账户已被锁定");
        exception.setUsername(username);
        return exception;
    }

    /**
     * 创建账户禁用异常
     *
     * @param username 用户名
     * @return 认证异常实例
     */
    public static AuthenticationException accountDisabled(String username) {
        AuthenticationException exception = new AuthenticationException(ErrorCode.ACCOUNT_DISABLED,
                "账户已被禁用");
        exception.setUsername(username);
        return exception;
    }

    /**
     * 创建账户过期异常
     *
     * @param username 用户名
     * @return 认证异常实例
     */
    public static AuthenticationException accountExpired(String username) {
        AuthenticationException exception = new AuthenticationException(ErrorCode.ACCOUNT_EXPIRED,
                "账户已过期");
        exception.setUsername(username);
        return exception;
    }

    // Getter和Setter方法
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AuthenticationException withPath(String path) {
        this.path = path;
        return this;
    }

    public AuthenticationException withUsername(String username) {
        this.username = username;
        return this;
    }

    public AuthenticationException withToken(String token) {
        this.token = token;
        return this;
    }

    @Override
    public String toString() {
        return String.format("AuthenticationException{errorCode=%s, message='%s', username='%s'}",
                errorCode, getMessage(), username);
    }
}

