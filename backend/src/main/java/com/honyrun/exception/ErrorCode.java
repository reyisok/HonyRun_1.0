package com.honyrun.exception;

import org.springframework.http.HttpStatus;

/**
 * 错误码枚举
 * 实现统一错误码定义和错误分类
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public enum ErrorCode {

    // 成功响应码 (2000-2999)
    SUCCESS(2000, "操作成功", HttpStatus.OK),

    // 客户端错误码 (4000-4999)
    BAD_REQUEST(4000, "请求参数错误", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(4001, "无效的请求", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER(4002, "缺少必要参数", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER(4003, "参数格式错误", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(4004, "参数验证失败", HttpStatus.BAD_REQUEST),

    // 认证授权错误码 (4010-4099)
    UNAUTHORIZED(4010, "未授权访问", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(4011, "无效的令牌", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(4012, "令牌已过期", HttpStatus.UNAUTHORIZED),
    TOKEN_BLACKLISTED(4013, "令牌已被禁用", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_FAILED(4014, "认证失败", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(4015, "用户名或密码错误", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(4016, "账户已被锁定", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED(4017, "账户已被禁用", HttpStatus.UNAUTHORIZED),
    ACCOUNT_EXPIRED(4018, "账户已过期", HttpStatus.UNAUTHORIZED),

    // 权限错误码 (4030-4099)
    FORBIDDEN(4030, "访问被拒绝", HttpStatus.FORBIDDEN),
    INSUFFICIENT_PERMISSIONS(4031, "权限不足", HttpStatus.FORBIDDEN),
    ACCESS_DENIED(4032, "拒绝访问", HttpStatus.FORBIDDEN),

    // 资源错误码 (4040-4099)
    NOT_FOUND(4040, "资源不存在", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(4041, "用户不存在", HttpStatus.NOT_FOUND),
    RESOURCE_NOT_FOUND(4042, "请求的资源不存在", HttpStatus.NOT_FOUND),

    // 方法错误码 (4050-4099)
    METHOD_NOT_ALLOWED(4050, "请求方法不被允许", HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE(4051, "不支持的媒体类型", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    // 冲突错误码 (4090-4099)
    CONFLICT(4090, "资源冲突", HttpStatus.CONFLICT),
    USER_ALREADY_EXISTS(4091, "用户已存在", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(4092, "邮箱已存在", HttpStatus.CONFLICT),
    PHONE_ALREADY_EXISTS(4093, "手机号已存在", HttpStatus.CONFLICT),
    DUPLICATE_RESOURCE(4094, "资源重复", HttpStatus.CONFLICT),

    // 限流错误码 (4290-4299)
    TOO_MANY_REQUESTS(4290, "请求过于频繁", HttpStatus.TOO_MANY_REQUESTS),
    RATE_LIMIT_EXCEEDED(4291, "超出请求频率限制", HttpStatus.TOO_MANY_REQUESTS),
    CONCURRENT_LIMIT_EXCEEDED(4292, "超出并发限制", HttpStatus.TOO_MANY_REQUESTS),

    // 服务器错误码 (5000-5999)
    INTERNAL_SERVER_ERROR(5000, "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_ERROR(5001, "系统错误", HttpStatus.INTERNAL_SERVER_ERROR),
    CONFIGURATION_ERROR(5002, "配置错误", HttpStatus.INTERNAL_SERVER_ERROR),
    INITIALIZATION_ERROR(5003, "初始化错误", HttpStatus.INTERNAL_SERVER_ERROR),

    // 数据访问错误码 (5010-5099)
    DATABASE_ERROR(5010, "数据库错误", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_CONNECTION_ERROR(5011, "数据库连接错误", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_TIMEOUT(5012, "数据库操作超时", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_INTEGRITY_ERROR(5013, "数据完整性错误", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSACTION_ERROR(5014, "事务处理错误", HttpStatus.INTERNAL_SERVER_ERROR),
    SQL_EXECUTION_ERROR(5015, "SQL执行错误", HttpStatus.INTERNAL_SERVER_ERROR),

    // 缓存错误码 (5020-5099)
    CACHE_ERROR(5020, "缓存错误", HttpStatus.INTERNAL_SERVER_ERROR),
    CACHE_CONNECTION_ERROR(5021, "缓存连接错误", HttpStatus.INTERNAL_SERVER_ERROR),
    CACHE_TIMEOUT(5022, "缓存操作超时", HttpStatus.INTERNAL_SERVER_ERROR),

    // 外部服务错误码 (5030-5099)
    EXTERNAL_SERVICE_ERROR(5030, "外部服务错误", HttpStatus.SERVICE_UNAVAILABLE),
    EXTERNAL_SERVICE_UNAVAILABLE(5031, "外部服务不可用", HttpStatus.SERVICE_UNAVAILABLE),
    EXTERNAL_SERVICE_TIMEOUT(5032, "外部服务超时", HttpStatus.SERVICE_UNAVAILABLE),
    NETWORK_ERROR(5033, "网络错误", HttpStatus.SERVICE_UNAVAILABLE),

    // 业务错误码 (6000-6999)
    BUSINESS_ERROR(6000, "业务处理错误", HttpStatus.BAD_REQUEST),
    BUSINESS_RULE_VIOLATION(6001, "违反业务规则", HttpStatus.BAD_REQUEST),
    BUSINESS_LOGIC_ERROR(6002, "业务逻辑错误", HttpStatus.BAD_REQUEST),

    // 核验业务错误码 (6100-6199)
    VERIFICATION_ERROR(6100, "核验处理错误", HttpStatus.BAD_REQUEST),
    VERIFICATION_REQUEST_INVALID(6101, "核验请求无效", HttpStatus.BAD_REQUEST),
    VERIFICATION_FAILED(6102, "核验失败", HttpStatus.BAD_REQUEST),
    VERIFICATION_TIMEOUT(6103, "核验超时", HttpStatus.REQUEST_TIMEOUT),
    VERIFICATION_RESULT_NOT_FOUND(6104, "核验结果不存在", HttpStatus.NOT_FOUND),

    // 用户管理错误码 (6200-6299)
    USER_MANAGEMENT_ERROR(6200, "用户管理错误", HttpStatus.BAD_REQUEST),
    USER_CREATION_FAILED(6201, "用户创建失败", HttpStatus.BAD_REQUEST),
    USER_UPDATE_FAILED(6202, "用户更新失败", HttpStatus.BAD_REQUEST),
    USER_DELETION_FAILED(6203, "用户删除失败", HttpStatus.BAD_REQUEST),
    PASSWORD_CHANGE_FAILED(6204, "密码修改失败", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_FORMAT(6205, "密码格式不正确", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_WEAK(6206, "密码强度不足", HttpStatus.BAD_REQUEST),
    PASSWORD_REUSED(6207, "密码不能与历史密码重复", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(6208, "密码错误", HttpStatus.BAD_REQUEST),

    // 系统管理错误码 (6300-6399)
    SYSTEM_MANAGEMENT_ERROR(6300, "系统管理错误", HttpStatus.BAD_REQUEST),
    SYSTEM_SETTING_ERROR(6301, "系统设置错误", HttpStatus.BAD_REQUEST),
    SYSTEM_LOG_ERROR(6302, "系统日志错误", HttpStatus.BAD_REQUEST),
    SYSTEM_MONITORING_ERROR(6303, "系统监控错误", HttpStatus.BAD_REQUEST),
    SYSTEM_MAINTENANCE_ERROR(6304, "系统维护错误", HttpStatus.BAD_REQUEST),

    // 文件处理错误码 (6400-6499)
    FILE_PROCESSING_ERROR(6400, "文件处理错误", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(6401, "文件上传失败", HttpStatus.BAD_REQUEST),
    FILE_DOWNLOAD_FAILED(6402, "文件下载失败", HttpStatus.BAD_REQUEST),
    FILE_FORMAT_ERROR(6403, "文件格式错误", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED(6404, "文件大小超出限制", HttpStatus.BAD_REQUEST),
    FILE_NOT_FOUND(6405, "文件不存在", HttpStatus.NOT_FOUND),

    // 图片处理错误码 (6450-6499)
    IMAGE_PROCESSING_ERROR(6450, "图片处理错误", HttpStatus.BAD_REQUEST),
    IMAGE_CONVERSION_FAILED(6451, "图片转换失败", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_FORMAT(6452, "无效的图片格式", HttpStatus.BAD_REQUEST),
    IMAGE_SIZE_EXCEEDED(6453, "图片大小超出限制", HttpStatus.BAD_REQUEST),

    // 模拟接口错误码 (6500-6599)
    MOCK_INTERFACE_ERROR(6500, "模拟接口错误", HttpStatus.BAD_REQUEST),
    MOCK_INTERFACE_NOT_FOUND(6501, "模拟接口不存在", HttpStatus.NOT_FOUND),
    MOCK_INTERFACE_DISABLED(6502, "模拟接口已禁用", HttpStatus.BAD_REQUEST),
    MOCK_RESPONSE_ERROR(6503, "模拟响应错误", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    /**
     * 构造函数
     *
     * @param code 错误码
     * @param message 错误消息
     * @param httpStatus HTTP状态码
     */
    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取错误消息
     *
     * @return 错误消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取HTTP状态码
     *
     * @return HTTP状态码
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * 根据错误码查找对应的枚举值
     *
     * @param code 错误码
     * @return 对应的ErrorCode枚举值，如果未找到则返回INTERNAL_SERVER_ERROR
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return INTERNAL_SERVER_ERROR;
    }

    /**
     * 判断是否为客户端错误
     *
     * @return 如果是客户端错误返回true，否则返回false
     */
    public boolean isClientError() {
        return code >= 4000 && code < 5000;
    }

    /**
     * 判断是否为服务器错误
     *
     * @return 如果是服务器错误返回true，否则返回false
     */
    public boolean isServerError() {
        return code >= 5000 && code < 7000;
    }

    /**
     * 判断是否为业务错误
     *
     * @return 如果是业务错误返回true，否则返回false
     */
    public boolean isBusinessError() {
        return code >= 6000 && code < 7000;
    }

    @Override
    public String toString() {
        return String.format("ErrorCode{code=%d, message='%s', httpStatus=%s}",
                code, message, httpStatus);
    }
}

