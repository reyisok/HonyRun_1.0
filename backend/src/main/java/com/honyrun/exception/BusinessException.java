package com.honyrun.exception;

/**
 * 业务异常类
 * 实现业务错误码、错误消息和异常链
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class BusinessException extends RuntimeException {

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
     * 用户ID（用于审计）
     */
    private String userId;

    /**
     * 业务上下文信息
     */
    private transient Object context;

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param cause 原始异常
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     * @param cause 原始异常
     */
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = ErrorCode.BUSINESS_ERROR;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.BUSINESS_ERROR;
    }

    /**
     * 创建业务异常
     *
     * @param errorCode 错误码枚举
     * @return 业务异常实例
     */
    public static BusinessException of(ErrorCode errorCode) {
        return new BusinessException(errorCode);
    }

    /**
     * 创建业务异常
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     * @return 业务异常实例
     */
    public static BusinessException of(ErrorCode errorCode, String message) {
        return new BusinessException(errorCode, message);
    }

    /**
     * 创建业务异常
     *
     * @param errorCode 错误码枚举
     * @param cause 原始异常
     * @return 业务异常实例
     */
    public static BusinessException of(ErrorCode errorCode, Throwable cause) {
        return new BusinessException(errorCode, cause);
    }

    /**
     * 创建业务异常
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     * @param cause 原始异常
     * @return 业务异常实例
     */
    public static BusinessException of(ErrorCode errorCode, String message, Throwable cause) {
        return new BusinessException(errorCode, message, cause);
    }

    /**
     * 创建用户不存在异常
     *
     * @param userId 用户ID
     * @return 业务异常实例
     */
    public static BusinessException userNotFound(String userId) {
        BusinessException exception = new BusinessException(ErrorCode.USER_NOT_FOUND,
                String.format("用户不存在: %s", userId));
        exception.setUserId(userId);
        return exception;
    }

    /**
     * 创建用户已存在异常
     *
     * @param username 用户名
     * @return 业务异常实例
     */
    public static BusinessException userAlreadyExists(String username) {
        return new BusinessException(ErrorCode.USER_ALREADY_EXISTS,
                String.format("用户已存在: %s", username));
    }

    /**
     * 创建业务规则违反异常
     *
     * @param rule 业务规则描述
     * @return 业务异常实例
     */
    public static BusinessException businessRuleViolation(String rule) {
        return new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                String.format("违反业务规则: %s", rule));
    }

    /**
     * 创建核验失败异常
     *
     * @param reason 失败原因
     * @return 业务异常实例
     */
    public static BusinessException verificationFailed(String reason) {
        return new BusinessException(ErrorCode.VERIFICATION_FAILED,
                String.format("核验失败: %s", reason));
    }

    /**
     * 创建资源冲突异常
     *
     * @param resource 资源名称
     * @return 业务异常实例
     */
    public static BusinessException resourceConflict(String resource) {
        return new BusinessException(ErrorCode.CONFLICT,
                String.format("资源冲突: %s", resource));
    }

    /**
     * 设置请求路径
     *
     * @param path 请求路径
     * @return 当前异常实例（支持链式调用）
     */
    public BusinessException withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     * @return 当前异常实例（支持链式调用）
     */
    public BusinessException withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * 设置业务上下文
     *
     * @param context 业务上下文信息
     * @return 当前异常实例（支持链式调用）
     */
    public BusinessException withContext(Object context) {
        this.context = context;
        return this;
    }

    /**
     * 获取错误码
     *
     * @return 错误码枚举
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 获取请求路径
     *
     * @return 请求路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置请求路径
     *
     * @param path 请求路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取业务上下文
     *
     * @return 业务上下文信息
     */
    public Object getContext() {
        return context;
    }

    /**
     * 设置业务上下文
     *
     * @param context 业务上下文信息
     */
    public void setContext(Object context) {
        this.context = context;
    }

    /**
     * 判断是否为特定错误码
     *
     * @param errorCode 要比较的错误码
     * @return 如果匹配返回true，否则返回false
     */
    public boolean isErrorCode(ErrorCode errorCode) {
        return this.errorCode == errorCode;
    }

    /**
     * 判断是否为业务错误
     *
     * @return 如果是业务错误返回true，否则返回false
     */
    public boolean isBusinessError() {
        return errorCode.isBusinessError();
    }

    @Override
    public String toString() {
        return String.format("BusinessException{errorCode=%s, message='%s', path='%s', userId='%s'}",
                errorCode, getMessage(), path, userId);
    }
}

