package com.honyrun.constant;

import java.time.Duration;

/**
 * 安全常量类
 * 提供JWT配置、权限常量、安全策略
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  16:10:00
 * @modified 2025-07-01 16:10:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // 常量类，禁止实例化
    }

    // ==================== JWT配置常量 ====================
    // 注意：JWT的issuer、audience、subject现在通过JwtProperties配置
    // 以下常量仅作为默认值参考，实际使用时应从JwtProperties读取

    /**
     * JWT密钥配置键 - 统一使用honyrun.jwt.*前缀
     */
    public static final String JWT_SECRET_KEY = "honyrun.jwt.secret";
    
    /**
     * JWT密钥 - 默认值，实际使用时从配置文件读取
     */
    public static final String JWT_SECRET_DEFAULT = "honyrun-reactive-jwt-secret-key-2025-must-be-at-least-512-bits-long-for-hs512-algorithm-security-requirements-compliance";

    /**
     * JWT访问令牌过期时间（小时）
     */
    public static final int JWT_ACCESS_TOKEN_EXPIRE_HOURS = 2;

    /**
     * JWT刷新令牌过期时间（天）
     */
    public static final int JWT_REFRESH_TOKEN_EXPIRE_DAYS = 7;

    /**
     * JWT访问令牌过期时间（毫秒）
     */
    public static final long JWT_ACCESS_TOKEN_EXPIRE_MS = JWT_ACCESS_TOKEN_EXPIRE_HOURS * 60 * 60 * 1000L;

    /**
     * JWT访问令牌过期时间（毫秒）- 兼容性别名
     */
    public static final long JWT_ACCESS_TOKEN_EXPIRATION = JWT_ACCESS_TOKEN_EXPIRE_MS;

    /**
     * JWT刷新令牌过期时间（毫秒）
     */
    public static final long JWT_REFRESH_TOKEN_EXPIRE_MS = JWT_REFRESH_TOKEN_EXPIRE_DAYS * 24 * 60 * 60 * 1000L;

    /**
     * JWT访问令牌过期时间（Duration）
     */
    public static final Duration JWT_ACCESS_TOKEN_DURATION = Duration.ofHours(JWT_ACCESS_TOKEN_EXPIRE_HOURS);

    /**
     * JWT刷新令牌过期时间（Duration）
     */
    public static final Duration JWT_REFRESH_TOKEN_DURATION = Duration.ofDays(JWT_REFRESH_TOKEN_EXPIRE_DAYS);

    // ==================== JWT声明常量 ====================

    /**
     * 用户ID声明
     */
    public static final String JWT_CLAIM_USER_ID = "userId";

    /**
     * 用户名声明
     */
    public static final String JWT_CLAIM_USERNAME = "username";

    /**
     * 用户类型声明
     */
    public static final String JWT_CLAIM_USER_TYPE = "userType";

    /**
     * 权限声明
     */
    public static final String JWT_CLAIM_AUTHORITIES = "authorities";

    /**
     * 令牌类型声明
     */
    public static final String JWT_CLAIM_TOKEN_TYPE = "tokenType";

    /**
     * 设备ID声明
     */
    public static final String JWT_CLAIM_DEVICE_ID = "deviceId";

    /**
     * IP地址声明
     */
    public static final String JWT_CLAIM_IP_ADDRESS = "ipAddress";

    // ==================== 令牌类型常量 ====================

    /**
     * 访问令牌类型
     */
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";

    /**
     * 刷新令牌类型
     */
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    /**
     * Bearer令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌头名称
     */
    public static final String TOKEN_HEADER = "Authorization";

    // ==================== 权限常量 ====================

    /**
     * 超级管理员权限
     */
    public static final String AUTHORITY_SUPER_ADMIN = "SUPER_ADMIN";

    /**
     * 系统管理员权限
     */
    public static final String AUTHORITY_SYSTEM_ADMIN = "SYSTEM_ADMIN";

    /**
     * 用户管理权限
     */
    public static final String AUTHORITY_USER_MANAGE = "USER_MANAGE";

    /**
     * 用户查看权限
     */
    public static final String AUTHORITY_USER_VIEW = "USER_VIEW";

    /**
     * 用户创建权限
     */
    public static final String AUTHORITY_USER_CREATE = "USER_CREATE";

    /**
     * 用户更新权限
     */
    public static final String AUTHORITY_USER_UPDATE = "USER_UPDATE";

    /**
     * 用户删除权限
     */
    public static final String AUTHORITY_USER_DELETE = "USER_DELETE";

    /**
     * 系统设置权限
     */
    public static final String AUTHORITY_SYSTEM_SETTING = "SYSTEM_SETTING";

    /**
     * 系统监控权限
     */
    public static final String AUTHORITY_SYSTEM_MONITOR = "SYSTEM_MONITOR";

    /**
     * 日志查看权限
     */
    public static final String AUTHORITY_LOG_VIEW = "LOG_VIEW";

    /**
     * 业务操作权限
     */
    public static final String AUTHORITY_BUSINESS_OPERATE = "BUSINESS_OPERATE";

    /**
     * 业务查看权限
     */
    public static final String AUTHORITY_BUSINESS_VIEW = "BUSINESS_VIEW";

    /**
     * 文件上传权限
     */
    public static final String AUTHORITY_FILE_UPLOAD = "FILE_UPLOAD";

    // 统一权限字段名

    /**
     * JWT声明中的权限字段名
     * 【权限模型】：采用用户-权限直接映射，基于用户类型控制权限范围
     */
    public static final String JWT_CLAIM_PERMISSIONS = "permissions";

    // ==================== 用户类型常量 ====================
    // 【用户类型标准化】：严格使用SYSTEM_USER、NORMAL_USER、GUEST，禁止role概念

    /**
     * 系统用户类型 - 拥有系统管理权限
     */
    public static final String USER_TYPE_SYSTEM = "SYSTEM_USER";

    /**
     * 普通用户类型 - 拥有基础业务权限
     */
    public static final String USER_TYPE_NORMAL = "NORMAL_USER";

    /**
     * 访客用户类型 - 仅拥有只读权限
     */
    public static final String USER_TYPE_GUEST = "GUEST";

    // ==================== 用户状态常量 ====================

    /**
     * 用户状态：正常
     */
    public static final String USER_STATUS_ACTIVE = "ACTIVE";

    /**
     * 用户状态：禁用
     */
    public static final String USER_STATUS_DISABLED = "DISABLED";

    /**
     * 用户状态：锁定
     */
    public static final String USER_STATUS_LOCKED = "LOCKED";

    /**
     * 用户状态：过期
     */
    public static final String USER_STATUS_EXPIRED = "EXPIRED";

    /**
     * 用户状态：待激活
     */
    public static final String USER_STATUS_PENDING = "PENDING";

    // ==================== 密码策略常量 ====================

    /**
     * 密码最小长度
     */
    public static final int PASSWORD_MIN_LENGTH = 6;

    /**
     * 密码最大长度
     */
    public static final int PASSWORD_MAX_LENGTH = 20;

    /**
     * 密码必须包含数字
     */
    public static final boolean PASSWORD_REQUIRE_DIGIT = true;

    /**
     * 密码必须包含字母
     */
    public static final boolean PASSWORD_REQUIRE_LETTER = true;

    /**
     * 密码必须包含特殊字符
     */
    public static final boolean PASSWORD_REQUIRE_SPECIAL = false;

    /**
     * 密码历史记录数量
     */
    public static final int PASSWORD_HISTORY_COUNT = 5;

    /**
     * 密码过期天数
     */
    public static final int PASSWORD_EXPIRE_DAYS = 90;

    // ==================== 登录安全常量 ====================

    /**
     * 最大登录失败次数
     */
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    /**
     * 账户锁定时间（分钟）
     */
    public static final int ACCOUNT_LOCK_MINUTES = 30;

    /**
     * 登录失败重置时间（分钟）
     */
    public static final int LOGIN_ATTEMPT_RESET_MINUTES = 60;

    /**
     * 会话超时时间（分钟）
     */
    public static final int SESSION_TIMEOUT_MINUTES = 120;

    /**
     * 记住我功能过期时间（天）
     */
    public static final int REMEMBER_ME_EXPIRE_DAYS = 30;

    // ==================== 加密常量 ====================

    /**
     * 密码加密算法
     */
    public static final String PASSWORD_ENCODER = "BCrypt";

    /**
     * BCrypt强度
     */
    public static final int BCRYPT_STRENGTH = 12;

    /**
     * AES加密算法
     */
    public static final String AES_ALGORITHM = "AES";

    /**
     * AES加密模式
     */
    public static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    /**
     * RSA加密算法
     */
    public static final String RSA_ALGORITHM = "RSA";

    /**
     * RSA密钥长度
     */
    public static final int RSA_KEY_SIZE = 2048;

    /**
     * MD5算法
     */
    public static final String MD5_ALGORITHM = "MD5";

    /**
     * SHA256算法
     */
    public static final String SHA256_ALGORITHM = "SHA-256";

    // ==================== 安全头常量 ====================

    /**
     * 内容安全策略头
     */
    public static final String CSP_HEADER = "Content-Security-Policy";

    /**
     * X-Frame-Options头
     */
    public static final String X_FRAME_OPTIONS_HEADER = "X-Frame-Options";

    /**
     * X-Content-Type-Options头
     */
    public static final String X_CONTENT_TYPE_OPTIONS_HEADER = "X-Content-Type-Options";

    /**
     * X-XSS-Protection头
     */
    public static final String X_XSS_PROTECTION_HEADER = "X-XSS-Protection";

    /**
     * Strict-Transport-Security头
     */
    public static final String HSTS_HEADER = "Strict-Transport-Security";

    /**
     * Referrer-Policy头
     */
    public static final String REFERRER_POLICY_HEADER = "Referrer-Policy";

    /**
     * 默认CSP策略
     */
    public static final String DEFAULT_CSP_POLICY = "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'";

    /**
     * 默认X-Frame-Options值
     */
    public static final String DEFAULT_X_FRAME_OPTIONS = "DENY";

    /**
     * 默认X-Content-Type-Options值
     */
    public static final String DEFAULT_X_CONTENT_TYPE_OPTIONS = "nosniff";

    /**
     * 默认X-XSS-Protection值
     */
    public static final String DEFAULT_X_XSS_PROTECTION = "1; mode=block";

    /**
     * 默认HSTS值
     */
    public static final String DEFAULT_HSTS = "max-age=31536000; includeSubDomains";

    /**
     * 默认Referrer-Policy值
     */
    public static final String DEFAULT_REFERRER_POLICY = "strict-origin-when-cross-origin";

    // ==================== 限流常量 ====================

    /**
     * 默认限流次数（每分钟）
     */
    public static final int DEFAULT_RATE_LIMIT_PER_MINUTE = 60;

    /**
     * 登录限流次数（每分钟）
     */
    public static final int LOGIN_RATE_LIMIT_PER_MINUTE = 10;

    /**
     * API限流次数（每分钟）
     */
    public static final int API_RATE_LIMIT_PER_MINUTE = 100;

    /**
     * 文件上传限流次数（每分钟）
     */
    public static final int UPLOAD_RATE_LIMIT_PER_MINUTE = 20;

    // ==================== CORS安全常量 ====================

    /**
     * CORS配置键名常量 - 用于从配置文件读取
     * 注意：不再使用硬编码值，所有配置从环境配置文件获取
     */
    public static final String CORS_ALLOWED_ORIGINS_KEY = "honyrun.security.cors.allowed-origins";
    public static final String CORS_ALLOWED_METHODS_KEY = "honyrun.security.cors.allowed-methods";
    public static final String CORS_ALLOWED_HEADERS_KEY = "honyrun.security.cors.allowed-headers";
    public static final String CORS_EXPOSED_HEADERS_KEY = "honyrun.security.cors.exposed-headers";
    public static final String CORS_PREFLIGHT_MAX_AGE_KEY = "honyrun.security.cors.preflight-max-age";

    /**
     * 默认CORS方法（仅作为配置缺失时的后备值）
     */
    public static final String[] DEFAULT_ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};

    /**
     * 默认CORS头部（仅作为配置缺失时的后备值）
     */
    public static final String[] DEFAULT_ALLOWED_HEADERS = {"Content-Type", "Authorization", "X-Requested-With"};

    /**
     * 默认暴露头部（仅作为配置缺失时的后备值）
     */
    public static final String[] DEFAULT_EXPOSED_HEADERS = {"X-Total-Count", "X-Total-Pages"};

    /**
     * 默认预检请求缓存时间（秒）（仅作为配置缺失时的后备值）
     */
    public static final long DEFAULT_PREFLIGHT_MAX_AGE = 3600;

    // ==================== 审计常量 ====================

    /**
     * 审计事件：登录成功
     */
    public static final String AUDIT_LOGIN_SUCCESS = "LOGIN_SUCCESS";

    /**
     * 审计事件：登录失败
     */
    public static final String AUDIT_LOGIN_FAILURE = "LOGIN_FAILURE";

    /**
     * 审计事件：登出
     */
    public static final String AUDIT_LOGOUT = "LOGOUT";

    /**
     * 审计事件：权限变更
     */
    public static final String AUDIT_PERMISSION_CHANGE = "PERMISSION_CHANGE";

    /**
     * 审计事件：密码变更
     */
    public static final String AUDIT_PASSWORD_CHANGE = "PASSWORD_CHANGE";

    /**
     * 审计事件：账户锁定
     */
    public static final String AUDIT_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";

    /**
     * 审计事件：账户解锁
     */
    public static final String AUDIT_ACCOUNT_UNLOCKED = "ACCOUNT_UNLOCKED";

    /**
     * 审计事件：敏感操作
     */
    public static final String AUDIT_SENSITIVE_OPERATION = "SENSITIVE_OPERATION";
}



