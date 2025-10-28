package com.honyrun.exception;

/**
 * 外部服务异常类
 * 实现第三方服务调用异常和网络异常
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class ExternalServiceException extends RuntimeException {

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
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务端点
     */
    private String endpoint;

    /**
     * HTTP状态码
     */
    private Integer httpStatus;

    /**
     * 响应内容
     */
    private String responseBody;

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     */
    public ExternalServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     */
    public ExternalServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param cause 原始异常
     */
    public ExternalServiceException(ErrorCode errorCode, Throwable cause) {
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
    public ExternalServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public ExternalServiceException(String message) {
        super(message);
        this.errorCode = ErrorCode.EXTERNAL_SERVICE_ERROR;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause 原始异常
     */
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.EXTERNAL_SERVICE_ERROR;
    }

    /**
     * 创建外部服务不可用异常
     *
     * @param serviceName 服务名称
     * @param endpoint 服务端点
     * @return 外部服务异常实例
     */
    public static ExternalServiceException serviceUnavailable(String serviceName, String endpoint) {
        ExternalServiceException exception = new ExternalServiceException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
                String.format("外部服务不可用: %s", serviceName));
        exception.setServiceName(serviceName);
        exception.setEndpoint(endpoint);
        return exception;
    }

    /**
     * 创建外部服务超时异常
     *
     * @param serviceName 服务名称
     * @param endpoint 服务端点
     * @param cause 原始异常
     * @return 外部服务异常实例
     */
    public static ExternalServiceException serviceTimeout(String serviceName, String endpoint, Throwable cause) {
        ExternalServiceException exception = new ExternalServiceException(ErrorCode.EXTERNAL_SERVICE_TIMEOUT,
                String.format("外部服务调用超时: %s", serviceName), cause);
        exception.setServiceName(serviceName);
        exception.setEndpoint(endpoint);
        return exception;
    }

    /**
     * 创建网络错误异常
     *
     * @param serviceName 服务名称
     * @param endpoint 服务端点
     * @param cause 原始异常
     * @return 外部服务异常实例
     */
    public static ExternalServiceException networkError(String serviceName, String endpoint, Throwable cause) {
        ExternalServiceException exception = new ExternalServiceException(ErrorCode.NETWORK_ERROR,
                String.format("网络连接错误: %s", serviceName), cause);
        exception.setServiceName(serviceName);
        exception.setEndpoint(endpoint);
        return exception;
    }

    /**
     * 创建HTTP错误异常
     *
     * @param serviceName 服务名称
     * @param endpoint 服务端点
     * @param httpStatus HTTP状态码
     * @param responseBody 响应内容
     * @return 外部服务异常实例
     */
    public static ExternalServiceException httpError(String serviceName, String endpoint,
                                                   int httpStatus, String responseBody) {
        ExternalServiceException exception = new ExternalServiceException(ErrorCode.EXTERNAL_SERVICE_ERROR,
                String.format("外部服务HTTP错误: %s (状态码: %d)", serviceName, httpStatus));
        exception.setServiceName(serviceName);
        exception.setEndpoint(endpoint);
        exception.setHttpStatus(httpStatus);
        exception.setResponseBody(responseBody);
        return exception;
    }

    /**
     * 创建API调用异常
     *
     * @param serviceName 服务名称
     * @param apiPath API路径
     * @param cause 原始异常
     * @return 外部服务异常实例
     */
    public static ExternalServiceException apiCallError(String serviceName, String apiPath, Throwable cause) {
        ExternalServiceException exception = new ExternalServiceException(ErrorCode.EXTERNAL_SERVICE_ERROR,
                String.format("API调用失败: %s%s", serviceName, apiPath), cause);
        exception.setServiceName(serviceName);
        exception.setEndpoint(apiPath);
        return exception;
    }

    /**
     * 创建认证失败异常
     *
     * @param serviceName 服务名称
     * @param endpoint 服务端点
     * @return 外部服务异常实例
     */
    public static ExternalServiceException authenticationFailed(String serviceName, String endpoint) {
        ExternalServiceException exception = new ExternalServiceException(ErrorCode.EXTERNAL_SERVICE_ERROR,
                String.format("外部服务认证失败: %s", serviceName));
        exception.setServiceName(serviceName);
        exception.setEndpoint(endpoint);
        exception.setHttpStatus(401);
        return exception;
    }

    /**
     * 创建权限不足异常
     *
     * @param serviceName 服务名称
     * @param endpoint 服务端点
     * @return 外部服务异常实例
     */
    public static ExternalServiceException accessDenied(String serviceName, String endpoint) {
        ExternalServiceException exception = new ExternalServiceException(ErrorCode.EXTERNAL_SERVICE_ERROR,
                String.format("外部服务访问被拒绝: %s", serviceName));
        exception.setServiceName(serviceName);
        exception.setEndpoint(endpoint);
        exception.setHttpStatus(403);
        return exception;
    }

    /**
     * 创建资源不存在异常
     *
     * @param serviceName 服务名称
     * @param endpoint 服务端点
     * @return 外部服务异常实例
     */
    public static ExternalServiceException resourceNotFound(String serviceName, String endpoint) {
        ExternalServiceException exception = new ExternalServiceException(ErrorCode.EXTERNAL_SERVICE_ERROR,
                String.format("外部服务资源不存在: %s", serviceName));
        exception.setServiceName(serviceName);
        exception.setEndpoint(endpoint);
        exception.setHttpStatus(404);
        return exception;
    }

    /**
     * 创建服务器内部错误异常
     *
     * @param serviceName 服务名称
     * @param endpoint 服务端点
     * @param responseBody 响应内容
     * @return 外部服务异常实例
     */
    public static ExternalServiceException serverError(String serviceName, String endpoint, String responseBody) {
        ExternalServiceException exception = new ExternalServiceException(ErrorCode.EXTERNAL_SERVICE_ERROR,
                String.format("外部服务内部错误: %s", serviceName));
        exception.setServiceName(serviceName);
        exception.setEndpoint(endpoint);
        exception.setHttpStatus(500);
        exception.setResponseBody(responseBody);
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public ExternalServiceException withPath(String path) {
        this.path = path;
        return this;
    }

    public ExternalServiceException withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ExternalServiceException withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public ExternalServiceException withHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public ExternalServiceException withResponseBody(String responseBody) {
        this.responseBody = responseBody;
        return this;
    }

    @Override
    public String toString() {
        return String.format("ExternalServiceException{errorCode=%s, message='%s', serviceName='%s', endpoint='%s', httpStatus=%d}",
                errorCode, getMessage(), serviceName, endpoint, httpStatus);
    }
}

