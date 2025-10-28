package com.honyrun.exception;

/**
 * 系统异常类
 * 实现系统级错误和技术异常处理
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class SystemException extends RuntimeException {

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
     * 系统组件名称
     */
    private String component;

    /**
     * 错误详细信息
     */
    private String details;

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     */
    public SystemException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     */
    public SystemException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param cause 原始异常
     */
    public SystemException(ErrorCode errorCode, Throwable cause) {
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
    public SystemException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public SystemException(String message) {
        super(message);
        this.errorCode = ErrorCode.SYSTEM_ERROR;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause 原始异常
     */
    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.SYSTEM_ERROR;
    }

    /**
     * 创建系统异常
     *
     * @param errorCode 错误码枚举
     * @return 系统异常实例
     */
    public static SystemException of(ErrorCode errorCode) {
        return new SystemException(errorCode);
    }

    /**
     * 创建系统异常
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     * @return 系统异常实例
     */
    public static SystemException of(ErrorCode errorCode, String message) {
        return new SystemException(errorCode, message);
    }

    /**
     * 创建系统异常
     *
     * @param errorCode 错误码枚举
     * @param cause 原始异常
     * @return 系统异常实例
     */
    public static SystemException of(ErrorCode errorCode, Throwable cause) {
        return new SystemException(errorCode, cause);
    }

    /**
     * 创建系统异常
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     * @param cause 原始异常
     * @return 系统异常实例
     */
    public static SystemException of(ErrorCode errorCode, String message, Throwable cause) {
        return new SystemException(errorCode, message, cause);
    }

    /**
     * 创建配置错误异常
     *
     * @param configKey 配置键
     * @param reason 错误原因
     * @return 系统异常实例
     */
    public static SystemException configurationError(String configKey, String reason) {
        SystemException exception = new SystemException(ErrorCode.CONFIGURATION_ERROR,
                String.format("配置错误 [%s]: %s", configKey, reason));
        exception.setComponent("Configuration");
        exception.setDetails(String.format("配置键: %s, 原因: %s", configKey, reason));
        return exception;
    }

    /**
     * 创建初始化错误异常
     *
     * @param component 组件名称
     * @param reason 错误原因
     * @return 系统异常实例
     */
    public static SystemException initializationError(String component, String reason) {
        SystemException exception = new SystemException(ErrorCode.INITIALIZATION_ERROR,
                String.format("初始化失败 [%s]: %s", component, reason));
        exception.setComponent(component);
        exception.setDetails(reason);
        return exception;
    }

    /**
     * 创建数据库连接错误异常
     *
     * @param database 数据库名称
     * @param cause 原始异常
     * @return 系统异常实例
     */
    public static SystemException databaseConnectionError(String database, Throwable cause) {
        SystemException exception = new SystemException(ErrorCode.DATABASE_CONNECTION_ERROR,
                String.format("数据库连接失败: %s", database), cause);
        exception.setComponent("Database");
        exception.setDetails(String.format("数据库: %s", database));
        return exception;
    }

    /**
     * 创建缓存连接错误异常
     *
     * @param cacheType 缓存类型
     * @param cause 原始异常
     * @return 系统异常实例
     */
    public static SystemException cacheConnectionError(String cacheType, Throwable cause) {
        SystemException exception = new SystemException(ErrorCode.CACHE_CONNECTION_ERROR,
                String.format("缓存连接失败: %s", cacheType), cause);
        exception.setComponent("Cache");
        exception.setDetails(String.format("缓存类型: %s", cacheType));
        return exception;
    }

    /**
     * 创建外部服务不可用异常
     *
     * @param serviceName 服务名称
     * @param endpoint 服务端点
     * @return 系统异常实例
     */
    public static SystemException externalServiceUnavailable(String serviceName, String endpoint) {
        SystemException exception = new SystemException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
                String.format("外部服务不可用: %s", serviceName));
        exception.setComponent("ExternalService");
        exception.setDetails(String.format("服务: %s, 端点: %s", serviceName, endpoint));
        return exception;
    }

    /**
     * 创建网络错误异常
     *
     * @param operation 操作描述
     * @param cause 原始异常
     * @return 系统异常实例
     */
    public static SystemException networkError(String operation, Throwable cause) {
        SystemException exception = new SystemException(ErrorCode.NETWORK_ERROR,
                String.format("网络错误: %s", operation), cause);
        exception.setComponent("Network");
        exception.setDetails(operation);
        return exception;
    }

    /**
     * 创建SQL执行错误异常
     *
     * @param sql SQL语句
     * @param cause 原始异常
     * @return 系统异常实例
     */
    public static SystemException sqlExecutionError(String sql, Throwable cause) {
        SystemException exception = new SystemException(ErrorCode.SQL_EXECUTION_ERROR,
                "SQL执行错误", cause);
        exception.setComponent("Database");
        exception.setDetails(String.format("SQL: %s", sql));
        return exception;
    }

    /**
     * 创建事务错误异常
     *
     * @param operation 事务操作
     * @param cause 原始异常
     * @return 系统异常实例
     */
    public static SystemException transactionError(String operation, Throwable cause) {
        SystemException exception = new SystemException(ErrorCode.TRANSACTION_ERROR,
                String.format("事务错误: %s", operation), cause);
        exception.setComponent("Transaction");
        exception.setDetails(operation);
        return exception;
    }

    /**
     * 设置请求路径
     *
     * @param path 请求路径
     * @return 当前异常实例（支持链式调用）
     */
    public SystemException withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * 设置系统组件
     *
     * @param component 系统组件名称
     * @return 当前异常实例（支持链式调用）
     */
    public SystemException withComponent(String component) {
        this.component = component;
        return this;
    }

    /**
     * 设置错误详情
     *
     * @param details 错误详细信息
     * @return 当前异常实例（支持链式调用）
     */
    public SystemException withDetails(String details) {
        this.details = details;
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
     * 获取系统组件名称
     *
     * @return 系统组件名称
     */
    public String getComponent() {
        return component;
    }

    /**
     * 设置系统组件名称
     *
     * @param component 系统组件名称
     */
    public void setComponent(String component) {
        this.component = component;
    }

    /**
     * 获取错误详细信息
     *
     * @return 错误详细信息
     */
    public String getDetails() {
        return details;
    }

    /**
     * 设置错误详细信息
     *
     * @param details 错误详细信息
     */
    public void setDetails(String details) {
        this.details = details;
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
     * 判断是否为服务器错误
     *
     * @return 如果是服务器错误返回true，否则返回false
     */
    public boolean isServerError() {
        return errorCode.isServerError();
    }

    @Override
    public String toString() {
        return String.format("SystemException{errorCode=%s, message='%s', component='%s', path='%s'}",
                errorCode, getMessage(), component, path);
    }
}

