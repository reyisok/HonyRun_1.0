package com.honyrun.constant;

import java.time.Duration;

/**
 * 缓存常量类
 * 提供缓存键前缀、过期时间、缓存策略
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  16:15:00
 * @modified 2025-07-01 16:15:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public final class CacheConstants {

    private CacheConstants() {
        // 常量类，禁止实例化
    }

    // ==================== 缓存名称常量 ====================

    /**
     * 用户缓存名称
     */
    public static final String CACHE_USER = "user";

    /**
     * 系统设置缓存名称
     */
    public static final String CACHE_SYSTEM_SETTING = "systemSetting";

    /**
     * 权限缓存名称
     */
    public static final String CACHE_PERMISSION = "permission";

    /**
     * 用户类型缓存名称
     * 注意：项目严格使用用户类型(UserType)概念，禁止使用角色(Role)概念
     */
    public static final String CACHE_USER_TYPE = "userType";

    /**
     * 令牌黑名单缓存名称
     */
    public static final String CACHE_TOKEN_BLACKLIST = "tokenBlacklist";

    /**
     * 验证码缓存名称
     */
    public static final String CACHE_CAPTCHA = "captcha";

    /**
     * 限流缓存名称
     */
    public static final String CACHE_RATE_LIMIT = "rateLimit";

    /**
     * 会话缓存名称
     */
    public static final String CACHE_SESSION = "session";

    /**
     * 业务数据缓存名称
     */
    public static final String CACHE_BUSINESS_DATA = "businessData";

    /**
     * 统计数据缓存名称
     */
    public static final String CACHE_STATISTICS = "statistics";

    // ==================== 缓存键前缀常量 ====================

    /**
     * 应用缓存键前缀
     */
    public static final String KEY_PREFIX = "honyrun:";

    /**
     * 用户缓存键前缀
     */
    public static final String USER_KEY_PREFIX = KEY_PREFIX + "user:";

    /**
     * 用户信息缓存键前缀
     */
    public static final String USER_INFO_KEY_PREFIX = USER_KEY_PREFIX + "info:";

    /**
     * 用户权限缓存键前缀
     */
    public static final String USER_PERMISSION_KEY_PREFIX = USER_KEY_PREFIX + "permission:";

    /**
     * 权限缓存键前缀
     */
    public static final String PERMISSION_KEY_PREFIX = KEY_PREFIX + "permission:";

    /**
     * 用户类型缓存键前缀
     * 注意：项目严格使用用户类型(UserType)概念，禁止使用角色(Role)概念
     */
    public static final String USER_USER_TYPE_KEY_PREFIX = USER_KEY_PREFIX + "userType:";

    /**
     * 用户会话缓存键前缀
     */
    public static final String USER_SESSION_KEY_PREFIX = USER_KEY_PREFIX + "session:";

    /**
     * 系统缓存键前缀
     */
    public static final String SYSTEM_KEY_PREFIX = KEY_PREFIX + "system:";

    /**
     * 系统设置缓存键前缀
     */
    public static final String SYSTEM_SETTING_KEY_PREFIX = SYSTEM_KEY_PREFIX + "setting:";

    /**
     * 系统配置缓存键前缀
     */
    public static final String SYSTEM_CONFIG_KEY_PREFIX = SYSTEM_KEY_PREFIX + "config:";

    /**
     * 系统状态缓存键前缀
     */
    public static final String SYSTEM_STATUS_KEY_PREFIX = SYSTEM_KEY_PREFIX + "status:";

    /**
     * 安全缓存键前缀
     */
    public static final String SECURITY_KEY_PREFIX = KEY_PREFIX + "security:";

    /**
     * 安全检测缓存键前缀
     */
    public static final String SECURITY_DETECTION_KEY_PREFIX = SECURITY_KEY_PREFIX + "detection:";

    /**
     * 令牌黑名单缓存键前缀
     */
    public static final String TOKEN_BLACKLIST_KEY_PREFIX = SECURITY_KEY_PREFIX + "blacklist:";

    /**
     * 登录失败缓存键前缀
     */
    public static final String LOGIN_FAILURE_KEY_PREFIX = SECURITY_KEY_PREFIX + "login:failure:";

    /**
     * 账户锁定缓存键前缀
     */
    public static final String ACCOUNT_LOCK_KEY_PREFIX = SECURITY_KEY_PREFIX + "lock:";

    /**
     * 验证码缓存键前缀
     */
    public static final String CAPTCHA_KEY_PREFIX = SECURITY_KEY_PREFIX + "captcha:";

    /**
     * 限流缓存键前缀
     */
    public static final String RATE_LIMIT_KEY_PREFIX = SECURITY_KEY_PREFIX + "ratelimit:";

    /**
     * 业务缓存键前缀
     */
    public static final String BUSINESS_KEY_PREFIX = KEY_PREFIX + "business:";

    /**
     * 核验业务缓存键前缀
     */
    public static final String VERIFICATION_KEY_PREFIX = BUSINESS_KEY_PREFIX + "verification:";

    /**
     * 图片处理缓存键前缀
     */
    public static final String IMAGE_KEY_PREFIX = BUSINESS_KEY_PREFIX + "image:";

    /**
     * 统计数据缓存键前缀
     */
    public static final String STATISTICS_KEY_PREFIX = KEY_PREFIX + "stats:";

    /**
     * 监控数据缓存键前缀
     */
    public static final String MONITOR_KEY_PREFIX = KEY_PREFIX + "monitor:";

    // ==================== 缓存过期时间常量（秒） ====================

    /**
     * 默认缓存过期时间（30分钟）
     */
    public static final long DEFAULT_EXPIRE_SECONDS = 30 * 60;

    /**
     * 短期缓存过期时间（5分钟）
     */
    public static final long SHORT_EXPIRE_SECONDS = 5 * 60;

    /**
     * 中期缓存过期时间（1小时）
     */
    public static final long MEDIUM_EXPIRE_SECONDS = 60 * 60;

    /**
     * 长期缓存过期时间（24小时）
     */
    public static final long LONG_EXPIRE_SECONDS = 24 * 60 * 60;

    /**
     * 用户信息缓存过期时间（2小时）
     */
    public static final long USER_INFO_EXPIRE_SECONDS = 2 * 60 * 60;

    /**
     * 用户权限缓存过期时间（1小时）
     */
    public static final long USER_PERMISSION_EXPIRE_SECONDS = 60 * 60;

    /**
     * 系统设置缓存过期时间（12小时）
     */
    public static final long SYSTEM_SETTING_EXPIRE_SECONDS = 12 * 60 * 60;

    /**
     * 令牌黑名单缓存过期时间（7天）
     */
    public static final long TOKEN_BLACKLIST_EXPIRE_SECONDS = 7 * 24 * 60 * 60;

    /**
     * 验证码缓存过期时间（5分钟）
     */
    public static final long CAPTCHA_EXPIRE_SECONDS = 5 * 60;

    /**
     * 限流缓存过期时间（1分钟）
     */
    public static final long RATE_LIMIT_EXPIRE_SECONDS = 60;

    /**
     * 登录失败缓存过期时间（1小时）
     */
    public static final long LOGIN_FAILURE_EXPIRE_SECONDS = 60 * 60;

    /**
     * 账户锁定缓存过期时间（30分钟）
     */
    public static final long ACCOUNT_LOCK_EXPIRE_SECONDS = 30 * 60;

    /**
     * 业务数据缓存过期时间（15分钟）
     */
    public static final long BUSINESS_DATA_EXPIRE_SECONDS = 15 * 60;

    /**
     * 统计数据缓存过期时间（10分钟）
     */
    public static final long STATISTICS_EXPIRE_SECONDS = 10 * 60;

    /**
     * 监控数据缓存过期时间（1分钟）
     */
    public static final long MONITOR_EXPIRE_SECONDS = 60;

    // ==================== 缓存过期时间Duration常量 ====================

    /**
     * 默认缓存过期时间Duration
     */
    public static final Duration DEFAULT_EXPIRE_DURATION = Duration.ofSeconds(DEFAULT_EXPIRE_SECONDS);

    /**
     * 短期缓存过期时间Duration
     */
    public static final Duration SHORT_EXPIRE_DURATION = Duration.ofSeconds(SHORT_EXPIRE_SECONDS);

    /**
     * 中期缓存过期时间Duration
     */
    public static final Duration MEDIUM_EXPIRE_DURATION = Duration.ofSeconds(MEDIUM_EXPIRE_SECONDS);

    /**
     * 长期缓存过期时间Duration
     */
    public static final Duration LONG_EXPIRE_DURATION = Duration.ofSeconds(LONG_EXPIRE_SECONDS);

    /**
     * 用户信息缓存过期时间Duration
     */
    public static final Duration USER_INFO_EXPIRE_DURATION = Duration.ofSeconds(USER_INFO_EXPIRE_SECONDS);

    /**
     * 用户权限缓存过期时间Duration
     */
    public static final Duration USER_PERMISSION_EXPIRE_DURATION = Duration.ofSeconds(USER_PERMISSION_EXPIRE_SECONDS);

    /**
     * 权限缓存过期时间Duration
     */
    public static final Duration PERMISSION_EXPIRE_DURATION = Duration.ofSeconds(USER_PERMISSION_EXPIRE_SECONDS);

    /**
     * 会话缓存过期时间Duration
     */
    public static final Duration SESSION_EXPIRE_DURATION = Duration.ofSeconds(USER_INFO_EXPIRE_SECONDS);

    /**
     * 业务数据缓存过期时间Duration
     */
    public static final Duration BUSINESS_DATA_EXPIRE_DURATION = Duration.ofSeconds(USER_INFO_EXPIRE_SECONDS);

    /**
     * 统计数据缓存过期时间Duration
     */
    public static final Duration STATISTICS_EXPIRE_DURATION = Duration.ofSeconds(SYSTEM_SETTING_EXPIRE_SECONDS);

    /**
     * 系统配置缓存过期时间Duration
     */
    public static final Duration SYSTEM_CONFIG_EXPIRE_DURATION = Duration.ofSeconds(SYSTEM_SETTING_EXPIRE_SECONDS);

    /**
     * 字典数据缓存过期时间Duration
     */
    public static final Duration DICTIONARY_EXPIRE_DURATION = Duration.ofSeconds(SYSTEM_SETTING_EXPIRE_SECONDS);

    /**
     * 系统设置缓存过期时间Duration
     */
    public static final Duration SYSTEM_SETTING_EXPIRE_DURATION = Duration.ofSeconds(SYSTEM_SETTING_EXPIRE_SECONDS);

    /**
     * 令牌黑名单缓存过期时间Duration
     */
    public static final Duration TOKEN_BLACKLIST_EXPIRE_DURATION = Duration.ofSeconds(TOKEN_BLACKLIST_EXPIRE_SECONDS);

    /**
     * 验证码缓存过期时间Duration
     */
    public static final Duration CAPTCHA_EXPIRE_DURATION = Duration.ofSeconds(CAPTCHA_EXPIRE_SECONDS);

    /**
     * 限流缓存过期时间Duration
     */
    public static final Duration RATE_LIMIT_EXPIRE_DURATION = Duration.ofSeconds(RATE_LIMIT_EXPIRE_SECONDS);

    // ==================== 缓存策略常量 ====================

    /**
     * 缓存策略：写入时更新
     */
    public static final String CACHE_STRATEGY_WRITE_THROUGH = "WRITE_THROUGH";

    /**
     * 缓存策略：写入后更新
     */
    public static final String CACHE_STRATEGY_WRITE_BEHIND = "WRITE_BEHIND";

    /**
     * 缓存策略：写入时失效
     */
    public static final String CACHE_STRATEGY_WRITE_AROUND = "WRITE_AROUND";

    /**
     * 缓存策略：只读缓存
     */
    public static final String CACHE_STRATEGY_READ_ONLY = "READ_ONLY";

    /**
     * 缓存策略：读写缓存
     */
    public static final String CACHE_STRATEGY_READ_WRITE = "READ_WRITE";

    // ==================== 缓存淘汰策略常量 ====================

    /**
     * 淘汰策略：最近最少使用
     */
    public static final String EVICTION_POLICY_LRU = "LRU";

    /**
     * 淘汰策略：最不经常使用
     */
    public static final String EVICTION_POLICY_LFU = "LFU";

    /**
     * 淘汰策略：先进先出
     */
    public static final String EVICTION_POLICY_FIFO = "FIFO";

    /**
     * 淘汰策略：随机淘汰
     */
    public static final String EVICTION_POLICY_RANDOM = "RANDOM";

    /**
     * 淘汰策略：基于时间
     */
    public static final String EVICTION_POLICY_TTL = "TTL";

    // ==================== 缓存配置常量 ====================

    /**
     * 默认缓存最大条目数
     */
    public static final int DEFAULT_MAX_ENTRIES = 1000;

    /**
     * 用户缓存最大条目数
     */
    public static final int USER_CACHE_MAX_ENTRIES = 5000;

    /**
     * 系统设置缓存最大条目数
     */
    public static final int SYSTEM_SETTING_MAX_ENTRIES = 500;

    /**
     * 权限缓存最大条目数
     */
    public static final int PERMISSION_CACHE_MAX_ENTRIES = 1000;

    /**
     * 业务数据缓存最大条目数
     */
    public static final int BUSINESS_DATA_MAX_ENTRIES = 2000;

    /**
     * 统计数据缓存最大条目数
     */
    public static final int STATISTICS_MAX_ENTRIES = 500;

    // ==================== 缓存刷新策略常量 ====================

    /**
     * 刷新策略：定时刷新
     */
    public static final String REFRESH_STRATEGY_SCHEDULED = "SCHEDULED";

    /**
     * 刷新策略：访问时刷新
     */
    public static final String REFRESH_STRATEGY_ON_ACCESS = "ON_ACCESS";

    /**
     * 刷新策略：写入时刷新
     */
    public static final String REFRESH_STRATEGY_ON_WRITE = "ON_WRITE";

    /**
     * 刷新策略：手动刷新
     */
    public static final String REFRESH_STRATEGY_MANUAL = "MANUAL";

    // ==================== 缓存监控常量 ====================

    /**
     * 缓存命中率监控键
     */
    public static final String CACHE_HIT_RATE_KEY = MONITOR_KEY_PREFIX + "cache:hitrate";

    /**
     * 缓存大小监控键
     */
    public static final String CACHE_SIZE_KEY = MONITOR_KEY_PREFIX + "cache:size";

    /**
     * 缓存操作计数监控键
     */
    public static final String CACHE_OPERATION_COUNT_KEY = MONITOR_KEY_PREFIX + "cache:operations";

    /**
     * 缓存错误计数监控键
     */
    public static final String CACHE_ERROR_COUNT_KEY = MONITOR_KEY_PREFIX + "cache:errors";

    // ==================== 分布式缓存常量 ====================

    /**
     * 分布式锁前缀
     */
    public static final String DISTRIBUTED_LOCK_PREFIX = KEY_PREFIX + "lock:";

    /**
     * 分布式锁默认过期时间（秒）
     */
    public static final long DISTRIBUTED_LOCK_EXPIRE_SECONDS = 30;

    /**
     * 分布式锁等待时间（毫秒）
     */
    public static final long DISTRIBUTED_LOCK_WAIT_MS = 100;

    /**
     * 分布式锁重试次数
     */
    public static final int DISTRIBUTED_LOCK_RETRY_COUNT = 3;
}


