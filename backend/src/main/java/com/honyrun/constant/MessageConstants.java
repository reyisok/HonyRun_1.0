package com.honyrun.constant;

/**
 * 消息常量类
 * 提供错误消息、提示信息、国际化键值
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  16:20:00
 * @modified 2025-07-01 16:20:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public final class MessageConstants {

    private MessageConstants() {
        // 常量类，禁止实例化
    }

    // ==================== 通用消息 ====================

    /**
     * 操作成功
     */
    public static final String SUCCESS = "操作成功";

    /**
     * 操作失败
     */
    public static final String FAILURE = "操作失败";

    /**
     * 系统错误
     */
    public static final String SYSTEM_ERROR = "系统错误，请稍后重试";

    /**
     * 网络错误
     */
    public static final String NETWORK_ERROR = "网络连接异常，请检查网络设置";

    /**
     * 服务不可用
     */
    public static final String SERVICE_UNAVAILABLE = "服务暂时不可用，请稍后重试";

    /**
     * 请求超时
     */
    public static final String REQUEST_TIMEOUT = "请求超时，请重新尝试";

    /**
     * 数据处理中
     */
    public static final String PROCESSING = "数据处理中，请稍候...";

    /**
     * 加载中
     */
    public static final String LOADING = "加载中...";

    // ==================== 参数验证消息 ====================

    /**
     * 参数不能为空
     */
    public static final String PARAMETER_NOT_NULL = "参数不能为空";

    /**
     * 参数格式错误
     */
    public static final String PARAMETER_FORMAT_ERROR = "参数格式错误";

    /**
     * 参数长度超限
     */
    public static final String PARAMETER_LENGTH_EXCEEDED = "参数长度超出限制";

    /**
     * 参数值无效
     */
    public static final String PARAMETER_INVALID = "参数值无效";

    /**
     * 缺少必需参数
     */
    public static final String MISSING_REQUIRED_PARAMETER = "缺少必需参数";

    /**
     * 参数类型错误
     */
    public static final String PARAMETER_TYPE_ERROR = "参数类型错误";

    /**
     * 参数范围错误
     */
    public static final String PARAMETER_RANGE_ERROR = "参数值超出有效范围";

    // ==================== 用户相关消息 ====================

    /**
     * 用户不存在
     */
    public static final String USER_NOT_FOUND = "用户不存在";

    /**
     * 用户已存在
     */
    public static final String USER_ALREADY_EXISTS = "用户已存在";

    /**
     * 用户名不能为空
     */
    public static final String USERNAME_NOT_EMPTY = "用户名不能为空";

    /**
     * 用户名格式错误
     */
    public static final String USERNAME_FORMAT_ERROR = "用户名格式错误，只能包含字母、数字、下划线和连字符";

    /**
     * 用户名长度错误
     */
    public static final String USERNAME_LENGTH_ERROR = "用户名长度必须在3-20个字符之间";

    /**
     * 密码不能为空
     */
    public static final String PASSWORD_NOT_EMPTY = "密码不能为空";

    /**
     * 密码格式错误
     */
    public static final String PASSWORD_FORMAT_ERROR = "密码必须包含字母和数字，长度6-20位";

    /**
     * 密码长度错误
     */
    public static final String PASSWORD_LENGTH_ERROR = "密码长度必须在6-20个字符之间";

    /**
     * 两次密码不一致
     */
    public static final String PASSWORD_NOT_MATCH = "两次输入的密码不一致";

    /**
     * 原密码错误
     */
    public static final String OLD_PASSWORD_ERROR = "原密码错误";

    /**
     * 用户已被禁用
     */
    public static final String USER_DISABLED = "用户已被禁用";

    /**
     * 用户已被锁定
     */
    public static final String USER_LOCKED = "用户已被锁定";

    /**
     * 用户已过期
     */
    public static final String USER_EXPIRED = "用户已过期";

    /**
     * 用户创建成功
     */
    public static final String USER_CREATE_SUCCESS = "用户创建成功";

    /**
     * 用户更新成功
     */
    public static final String USER_UPDATE_SUCCESS = "用户信息更新成功";

    /**
     * 用户删除成功
     */
    public static final String USER_DELETE_SUCCESS = "用户删除成功";

    /**
     * 密码修改成功
     */
    public static final String PASSWORD_CHANGE_SUCCESS = "密码修改成功";

    // ==================== 认证授权消息 ====================

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "登录成功";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAILURE = "登录失败";

    /**
     * 用户名或密码错误
     */
    public static final String INVALID_CREDENTIALS = "用户名或密码错误";

    /**
     * 登录失败次数过多
     */
    public static final String TOO_MANY_LOGIN_ATTEMPTS = "登录失败次数过多，账户已被锁定";

    /**
     * 账户锁定
     */
    public static final String ACCOUNT_LOCKED = "账户已被锁定，请稍后重试";

    /**
     * 登出成功
     */
    public static final String LOGOUT_SUCCESS = "登出成功";

    /**
     * 未登录
     */
    public static final String NOT_AUTHENTICATED = "请先登录";

    /**
     * 权限不足
     */
    public static final String INSUFFICIENT_PERMISSION = "权限不足，无法执行此操作";

    /**
     * 令牌无效
     */
    public static final String INVALID_TOKEN = "令牌无效";

    /**
     * 令牌已过期
     */
    public static final String TOKEN_EXPIRED = "令牌已过期，请重新登录";

    /**
     * 令牌刷新成功
     */
    public static final String TOKEN_REFRESH_SUCCESS = "令牌刷新成功";

    /**
     * 会话已过期
     */
    public static final String SESSION_EXPIRED = "会话已过期，请重新登录";

    // ==================== 数据操作消息 ====================

    /**
     * 数据不存在
     */
    public static final String DATA_NOT_FOUND = "数据不存在";

    /**
     * 数据已存在
     */
    public static final String DATA_ALREADY_EXISTS = "数据已存在";

    /**
     * 数据保存成功
     */
    public static final String DATA_SAVE_SUCCESS = "数据保存成功";

    /**
     * 数据更新成功
     */
    public static final String DATA_UPDATE_SUCCESS = "数据更新成功";

    /**
     * 数据删除成功
     */
    public static final String DATA_DELETE_SUCCESS = "数据删除成功";

    /**
     * 数据查询成功
     */
    public static final String DATA_QUERY_SUCCESS = "数据查询成功";

    /**
     * 数据导入成功
     */
    public static final String DATA_IMPORT_SUCCESS = "数据导入成功";

    /**
     * 数据导出成功
     */
    public static final String DATA_EXPORT_SUCCESS = "数据导出成功";

    /**
     * 数据库连接失败
     */
    public static final String DATABASE_CONNECTION_FAILED = "数据库连接失败";

    /**
     * 数据库操作失败
     */
    public static final String DATABASE_OPERATION_FAILED = "数据库操作失败";

    /**
     * 数据完整性约束违反
     */
    public static final String DATA_INTEGRITY_VIOLATION = "数据完整性约束违反";

    // ==================== 文件操作消息 ====================

    /**
     * 文件上传成功
     */
    public static final String FILE_UPLOAD_SUCCESS = "文件上传成功";

    /**
     * 文件上传失败
     */
    public static final String FILE_UPLOAD_FAILED = "文件上传失败";

    /**
     * 文件下载成功
     */
    public static final String FILE_DOWNLOAD_SUCCESS = "文件下载成功";

    /**
     * 文件不存在
     */
    public static final String FILE_NOT_FOUND = "文件不存在";

    /**
     * 文件格式不支持
     */
    public static final String FILE_FORMAT_NOT_SUPPORTED = "文件格式不支持";

    /**
     * 文件大小超限
     */
    public static final String FILE_SIZE_EXCEEDED = "文件大小超出限制";

    /**
     * 文件名不能为空
     */
    public static final String FILENAME_NOT_EMPTY = "文件名不能为空";

    /**
     * 文件名包含非法字符
     */
    public static final String FILENAME_INVALID_CHARACTERS = "文件名包含非法字符";

    /**
     * 文件删除成功
     */
    public static final String FILE_DELETE_SUCCESS = "文件删除成功";

    /**
     * 文件删除失败
     */
    public static final String FILE_DELETE_FAILED = "文件删除失败";

    // ==================== 业务操作消息 ====================

    /**
     * 核验请求提交成功
     */
    public static final String VERIFICATION_REQUEST_SUCCESS = "核验请求提交成功";

    /**
     * 核验处理完成
     */
    public static final String VERIFICATION_COMPLETED = "核验处理完成";

    /**
     * 核验失败
     */
    public static final String VERIFICATION_FAILED = "核验失败";

    /**
     * 核验结果不存在
     */
    public static final String VERIFICATION_RESULT_NOT_FOUND = "核验结果不存在";

    /**
     * 图片转换成功
     */
    public static final String IMAGE_CONVERT_SUCCESS = "图片转换成功";

    /**
     * 图片转换失败
     */
    public static final String IMAGE_CONVERT_FAILED = "图片转换失败";

    /**
     * 图片格式不支持
     */
    public static final String IMAGE_FORMAT_NOT_SUPPORTED = "图片格式不支持";

    /**
     * 业务处理成功
     */
    public static final String BUSINESS_PROCESS_SUCCESS = "业务处理成功";

    /**
     * 业务处理失败
     */
    public static final String BUSINESS_PROCESS_FAILED = "业务处理失败";

    // ==================== 系统管理消息 ====================

    /**
     * 系统配置更新成功
     */
    public static final String SYSTEM_CONFIG_UPDATE_SUCCESS = "系统配置更新成功";

    /**
     * 系统状态正常
     */
    public static final String SYSTEM_STATUS_NORMAL = "系统状态正常";

    /**
     * 系统维护中
     */
    public static final String SYSTEM_MAINTENANCE = "系统维护中，请稍后访问";

    /**
     * 系统升级中
     */
    public static final String SYSTEM_UPGRADING = "系统升级中，请稍后访问";

    /**
     * 缓存清理成功
     */
    public static final String CACHE_CLEAR_SUCCESS = "缓存清理成功";

    /**
     * 日志清理成功
     */
    public static final String LOG_CLEAR_SUCCESS = "日志清理成功";

    /**
     * 系统重启成功
     */
    public static final String SYSTEM_RESTART_SUCCESS = "系统重启成功";

    // ==================== 限流和安全消息 ====================

    /**
     * 请求过于频繁
     */
    public static final String TOO_MANY_REQUESTS = "请求过于频繁，请稍后再试";

    /**
     * IP被封禁
     */
    public static final String IP_BLOCKED = "IP地址已被封禁";

    /**
     * 访问被拒绝
     */
    public static final String ACCESS_DENIED = "访问被拒绝";

    /**
     * 非法请求
     */
    public static final String ILLEGAL_REQUEST = "非法请求";

    /**
     * 验证码错误
     */
    public static final String CAPTCHA_ERROR = "验证码错误";

    /**
     * 验证码已过期
     */
    public static final String CAPTCHA_EXPIRED = "验证码已过期";

    // ==================== 国际化键值 ====================

    /**
     * 国际化键前缀
     */
    public static final String I18N_PREFIX = "message.";

    /**
     * 成功消息国际化键
     */
    public static final String I18N_SUCCESS = I18N_PREFIX + "success";

    /**
     * 失败消息国际化键
     */
    public static final String I18N_FAILURE = I18N_PREFIX + "failure";

    /**
     * 系统错误国际化键
     */
    public static final String I18N_SYSTEM_ERROR = I18N_PREFIX + "system.error";

    /**
     * 参数错误国际化键
     */
    public static final String I18N_PARAMETER_ERROR = I18N_PREFIX + "parameter.error";

    /**
     * 用户不存在国际化键
     */
    public static final String I18N_USER_NOT_FOUND = I18N_PREFIX + "user.not.found";

    /**
     * 权限不足国际化键
     */
    public static final String I18N_INSUFFICIENT_PERMISSION = I18N_PREFIX + "insufficient.permission";

    /**
     * 令牌无效国际化键
     */
    public static final String I18N_INVALID_TOKEN = I18N_PREFIX + "invalid.token";

    /**
     * 数据不存在国际化键
     */
    public static final String I18N_DATA_NOT_FOUND = I18N_PREFIX + "data.not.found";

    // ==================== 提示信息 ====================

    /**
     * 确认删除提示
     */
    public static final String CONFIRM_DELETE = "确认要删除此项吗？";

    /**
     * 确认重置提示
     */
    public static final String CONFIRM_RESET = "确认要重置吗？";

    /**
     * 确认退出提示
     */
    public static final String CONFIRM_EXIT = "确认要退出吗？";

    /**
     * 数据已修改提示
     */
    public static final String DATA_MODIFIED = "数据已修改，是否保存？";

    /**
     * 操作不可逆提示
     */
    public static final String OPERATION_IRREVERSIBLE = "此操作不可逆，请谨慎操作";

    /**
     * 请选择数据提示
     */
    public static final String PLEASE_SELECT_DATA = "请选择要操作的数据";

    /**
     * 请填写必填项提示
     */
    public static final String PLEASE_FILL_REQUIRED = "请填写必填项";

    /**
     * 正在处理提示
     */
    public static final String PROCESSING_PLEASE_WAIT = "正在处理，请稍候...";
}


