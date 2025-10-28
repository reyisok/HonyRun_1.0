package com.honyrun.constant;

/**
 * 响应常量类
 * 提供响应码定义、消息模板
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  15:55:00
 * @modified 2025-07-01 15:55:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public final class ResponseConstants {

    private ResponseConstants() {
        // 常量类，禁止实例化
    }

    // ==================== HTTP状态码 ====================

    /**
     * 成功状态码
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 创建成功状态码
     */
    public static final int CREATED_CODE = 201;

    /**
     * 无内容状态码
     */
    public static final int NO_CONTENT_CODE = 204;

    /**
     * 客户端错误状态码
     */
    public static final int BAD_REQUEST_CODE = 400;

    /**
     * 未授权状态码
     */
    public static final int UNAUTHORIZED_CODE = 401;

    /**
     * 禁止访问状态码
     */
    public static final int FORBIDDEN_CODE = 403;

    /**
     * 资源未找到状态码
     */
    public static final int NOT_FOUND_CODE = 404;

    /**
     * 方法不允许状态码
     */
    public static final int METHOD_NOT_ALLOWED_CODE = 405;

    /**
     * 请求超时状态码
     */
    public static final int REQUEST_TIMEOUT_CODE = 408;

    /**
     * 冲突状态码
     */
    public static final int CONFLICT_CODE = 409;

    /**
     * 请求实体过大状态码
     */
    public static final int PAYLOAD_TOO_LARGE_CODE = 413;

    /**
     * 请求过于频繁状态码
     */
    public static final int TOO_MANY_REQUESTS_CODE = 429;

    /**
     * 服务器内部错误状态码
     */
    public static final int INTERNAL_SERVER_ERROR_CODE = 500;

    /**
     * 服务不可用状态码
     */
    public static final int SERVICE_UNAVAILABLE_CODE = 503;

    /**
     * 网关超时状态码
     */
    public static final int GATEWAY_TIMEOUT_CODE = 504;

    // ==================== 业务响应码 ====================

    /**
     * 操作成功
     */
    public static final String SUCCESS = "SUCCESS";

    /**
     * 操作失败
     */
    public static final String FAILURE = "FAILURE";

    /**
     * 参数错误
     */
    public static final String INVALID_PARAMETER = "INVALID_PARAMETER";

    /**
     * 数据不存在
     */
    public static final String DATA_NOT_FOUND = "DATA_NOT_FOUND";

    /**
     * 数据已存在
     */
    public static final String DATA_ALREADY_EXISTS = "DATA_ALREADY_EXISTS";

    /**
     * 权限不足
     */
    public static final String INSUFFICIENT_PERMISSION = "INSUFFICIENT_PERMISSION";

    /**
     * 认证失败
     */
    public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";

    /**
     * 令牌无效
     */
    public static final String INVALID_TOKEN = "INVALID_TOKEN";

    /**
     * 令牌过期
     */
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";

    /**
     * 系统错误
     */
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";

    /**
     * 网络错误
     */
    public static final String NETWORK_ERROR = "NETWORK_ERROR";

    /**
     * 数据库错误
     */
    public static final String DATABASE_ERROR = "DATABASE_ERROR";

    /**
     * 外部服务错误
     */
    public static final String EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR";

    /**
     * 限流错误
     */
    public static final String RATE_LIMIT_ERROR = "RATE_LIMIT_ERROR";

    /**
     * 验证错误
     */
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    // ==================== 响应消息 ====================

    /**
     * 操作成功消息
     */
    public static final String SUCCESS_MESSAGE = "操作成功";

    /**
     * 操作失败消息
     */
    public static final String FAILURE_MESSAGE = "操作失败";

    /**
     * 参数错误消息
     */
    public static final String INVALID_PARAMETER_MESSAGE = "参数错误";

    /**
     * 数据不存在消息
     */
    public static final String DATA_NOT_FOUND_MESSAGE = "数据不存在";

    /**
     * 数据已存在消息
     */
    public static final String DATA_ALREADY_EXISTS_MESSAGE = "数据已存在";

    /**
     * 权限不足消息
     */
    public static final String INSUFFICIENT_PERMISSION_MESSAGE = "权限不足";

    /**
     * 认证失败消息
     */
    public static final String AUTHENTICATION_FAILED_MESSAGE = "认证失败";

    /**
     * 令牌无效消息
     */
    public static final String INVALID_TOKEN_MESSAGE = "令牌无效";

    /**
     * 令牌过期消息
     */
    public static final String TOKEN_EXPIRED_MESSAGE = "令牌已过期";

    /**
     * 系统错误消息
     */
    public static final String SYSTEM_ERROR_MESSAGE = "系统错误";

    /**
     * 网络错误消息
     */
    public static final String NETWORK_ERROR_MESSAGE = "网络错误";

    /**
     * 数据库错误消息
     */
    public static final String DATABASE_ERROR_MESSAGE = "数据库错误";

    /**
     * 外部服务错误消息
     */
    public static final String EXTERNAL_SERVICE_ERROR_MESSAGE = "外部服务错误";

    /**
     * 限流错误消息
     */
    public static final String RATE_LIMIT_ERROR_MESSAGE = "请求过于频繁，请稍后再试";

    /**
     * 验证错误消息
     */
    public static final String VALIDATION_ERROR_MESSAGE = "数据验证失败";

    // ==================== 用户相关消息 ====================

    /**
     * 用户创建成功消息
     */
    public static final String USER_CREATE_SUCCESS = "用户创建成功";

    /**
     * 用户更新成功消息
     */
    public static final String USER_UPDATE_SUCCESS = "用户更新成功";

    /**
     * 用户删除成功消息
     */
    public static final String USER_DELETE_SUCCESS = "用户删除成功";

    /**
     * 批量创建用户成功消息
     */
    public static final String USER_BATCH_CREATE_SUCCESS = "批量创建用户成功";

    /**
     * 批量删除用户成功消息
     */
    public static final String USER_BATCH_DELETE_SUCCESS = "批量删除用户成功";

    /**
     * 用户启用成功消息
     */
    public static final String USER_ENABLE_SUCCESS = "用户启用成功";

    /**
     * 用户禁用成功消息
     */
    public static final String USER_DISABLE_SUCCESS = "用户禁用成功";

    /**
     * 批量启用用户成功消息
     */
    public static final String USER_BATCH_ENABLE_SUCCESS = "批量启用用户成功";

    /**
     * 批量禁用用户成功消息
     */
    public static final String USER_BATCH_DISABLE_SUCCESS = "批量禁用用户成功";

    /**
     * 密码重置成功消息
     */
    public static final String PASSWORD_RESET_SUCCESS = "密码重置成功";

    /**
     * 密码修改成功消息
     */
    public static final String PASSWORD_CHANGE_SUCCESS = "密码修改成功";

    /**
     * 权限更新成功消息
     */
    public static final String PERMISSION_UPDATE_SUCCESS = "权限更新成功";

    /**
     * 权限分配成功消息
     */
    public static final String PERMISSION_ASSIGN_SUCCESS = "权限分配成功";

    /**
     * 权限撤销成功消息
     */
    public static final String PERMISSION_REVOKE_SUCCESS = "权限撤销成功";

    /**
     * 权限清空成功消息
     */
    public static final String PERMISSION_CLEAR_SUCCESS = "权限清空成功";

    /**
     * 用户创建成功消息
     */
    public static final String USER_CREATED_MESSAGE = "用户创建成功";

    /**
     * 用户更新成功消息
     */
    public static final String USER_UPDATED_MESSAGE = "用户更新成功";

    /**
     * 用户删除成功消息
     */
    public static final String USER_DELETED_MESSAGE = "用户删除成功";

    /**
     * 用户不存在消息
     */
    public static final String USER_NOT_FOUND_MESSAGE = "用户不存在";

    /**
     * 用户已存在消息
     */
    public static final String USER_ALREADY_EXISTS_MESSAGE = "用户已存在";

    /**
     * 用户名或密码错误消息
     */
    public static final String INVALID_CREDENTIALS_MESSAGE = "用户名或密码错误";

    /**
     * 用户已被禁用消息
     */
    public static final String USER_DISABLED_MESSAGE = "用户已被禁用";

    /**
     * 用户已过期消息
     */
    public static final String USER_EXPIRED_MESSAGE = "用户已过期";

    /**
     * 登出成功消息
     */
    public static final String LOGOUT_SUCCESS_MESSAGE = "登出成功";

    /**
     * 登出成功
     */
    public static final String LOGOUT_SUCCESS = "登出成功";

    /**
     * 用户状态更新成功
     */
    public static final String USER_STATUS_UPDATE_SUCCESS = "用户状态更新成功";

    // ==================== 系统相关消息 ====================

    /**
     * 系统配置更新成功消息
     */
    public static final String SYSTEM_CONFIG_UPDATED_MESSAGE = "系统配置更新成功";

    /**
     * 系统状态正常消息
     */
    public static final String SYSTEM_STATUS_NORMAL_MESSAGE = "系统状态正常";

    /**
     * 系统维护中消息
     */
    public static final String SYSTEM_MAINTENANCE_MESSAGE = "系统维护中，请稍后访问";

    /**
     * 系统升级中消息
     */
    public static final String SYSTEM_UPGRADING_MESSAGE = "系统升级中，请稍后访问";

    // ==================== 业务相关消息 ====================

    /**
     * 核验请求提交成功消息
     */
    public static final String VERIFICATION_REQUEST_SUBMITTED_MESSAGE = "核验请求提交成功";

    /**
     * 核验处理完成消息
     */
    public static final String VERIFICATION_COMPLETED_MESSAGE = "核验处理完成";

    /**
     * 核验失败消息
     */
    public static final String VERIFICATION_FAILED_MESSAGE = "核验失败";

    /**
     * 图片转换成功消息
     */
    public static final String IMAGE_CONVERTED_MESSAGE = "图片转换成功";

    /**
     * 图片上传成功消息
     */
    public static final String IMAGE_UPLOADED_MESSAGE = "图片上传成功";

    /**
     * 图片格式不支持消息
     */
    public static final String IMAGE_FORMAT_NOT_SUPPORTED_MESSAGE = "图片格式不支持";

    /**
     * 文件大小超限消息
     */
    public static final String FILE_SIZE_EXCEEDED_MESSAGE = "文件大小超出限制";

    // ==================== 响应头常量 ====================

    /**
     * 内容类型头
     */
    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * JSON内容类型
     */
    public static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

    /**
     * 文本内容类型
     */
    public static final String TEXT_CONTENT_TYPE = "text/plain;charset=UTF-8";

    /**
     * HTML内容类型
     */
    public static final String HTML_CONTENT_TYPE = "text/html;charset=UTF-8";

    /**
     * 授权头
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Bearer令牌前缀
     */
    public static final String BEARER_TOKEN_PREFIX = "Bearer ";

    /**
     * 请求ID头
     */
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    /**
     * 用户代理头
     */
    public static final String USER_AGENT_HEADER = "User-Agent";

    /**
     * 客户端IP头
     */
    public static final String CLIENT_IP_HEADER = "X-Real-IP";

    /**
     * 转发IP头
     */
    public static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    // ==================== 分页相关常量 ====================

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE = 0;

    /**
     * 默认页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大页大小
     */
    public static final int MAX_PAGE_SIZE = 1000;

    /**
     * 页码参数名
     */
    public static final String PAGE_PARAM = "page";

    /**
     * 页大小参数名
     */
    public static final String SIZE_PARAM = "size";

    /**
     * 排序参数名
     */
    public static final String SORT_PARAM = "sort";
}


