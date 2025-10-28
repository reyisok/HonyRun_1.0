package com.honyrun.constant;

import java.time.Duration;

/**
 * 系统常量类
 * 提供系统级配置常量、默认值定义
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 16:00:00
 * @modified 2025-07-01 16:00:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
public final class SystemConstants {

    private SystemConstants() {
        // 常量类，禁止实例化
    }

    // ==================== 系统基本信息 ====================

    /**
     * 系统名称
     */
    public static final String SYSTEM_NAME = "HonyRun";

    /**
     * 系统版本
     */
    public static final String SYSTEM_VERSION = "2.0.0";

    /**
     * 系统描述
     */
    public static final String SYSTEM_DESCRIPTION = "HonyRun WebFlux响应式业务支撑平台";

    /**
     * 系统作者
     */
    public static final String SYSTEM_AUTHOR = "Mr.Rey";

    /**
     * 系统版权信息
     */
    public static final String SYSTEM_COPYRIGHT = "Copyright © 2025 HonyRun. All rights reserved.";

    /**
     * 系统官网
     */
    public static final String SYSTEM_WEBSITE = "https://honyrun.com";

    // ==================== 环境配置 ====================

    /**
     * 开发环境
     */
    public static final String ENV_DEV = "dev";

    /**
     * 测试环境
     */
    public static final String ENV_TEST = "test";

    /**
     * 生产环境
     */
    public static final String ENV_PROD = "prod";

    /**
     * 默认环境
     */
    public static final String DEFAULT_ENV = ENV_DEV;

    // ==================== 服务器配置 ====================
    // 注意：以下常量仅用于系统内部逻辑，实际配置值应从配置文件获取

    /**
     * 默认服务器端口配置键 - 严禁硬编码端口号
     *
     * 配置违规修复说明：
     * - 原问题：硬编码端口号 8901 违反统一配置规范
     * - 修复方案：改为配置键，通过环境变量或配置文件获取
     * - 防止再犯：所有端口配置必须通过配置管理器获取，禁止硬编码
     *
     * @see com.honyrun.config.UnifiedConfigManager#getStringConfig(String, String)
     */
    public static final String SERVER_PORT_CONFIG_KEY = "server.port";

    /**
     * Redis端口配置键 - 严禁硬编码端口号
     *
     * 配置违规修复说明：
     * - 防止硬编码Redis端口，统一使用配置键管理
     * - 实际端口值通过环境变量 HONYRUN_REDIS_PORT 获取
     *
     * @see com.honyrun.config.UnifiedConfigManager#getStringConfig(String, String)
     */
    public static final String REDIS_PORT_CONFIG_KEY = "HONYRUN_REDIS_PORT";

    /**
     * 默认上下文路径
     */
    public static final String DEFAULT_CONTEXT_PATH = "";

    /**
     * 默认字符编码
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 默认时区
     */
    public static final String DEFAULT_TIMEZONE = "Asia/Shanghai";

    /**
     * 默认语言
     */
    public static final String DEFAULT_LOCALE = "zh_CN";

    // ==================== 数据库配置 ====================

    /**
     * 默认数据库连接池大小
     */
    public static final int DEFAULT_DB_POOL_SIZE = 10;

    /**
     * 默认数据库连接超时时间（秒）
     */
    public static final int DEFAULT_DB_CONNECTION_TIMEOUT = 30;

    /**
     * 默认数据库查询超时时间（秒）
     */
    public static final int DEFAULT_DB_QUERY_TIMEOUT = 60;

    /**
     * 默认数据库最大空闲时间（分钟）
     */
    public static final int DEFAULT_DB_MAX_IDLE_TIME = 10;

    // ==================== Redis配置 ====================

    /**
     * 默认Redis主机配置键 - 严禁硬编码IP地址
     *
     * 配置违规修复说明：
     * - 原问题：硬编码 "127.0.0.1" 违反统一配置规范
     * - 修复方案：改为配置键，通过环境变量 HONYRUN_REDIS_HOST 获取
     * - 防止再犯：所有网络地址必须通过配置管理器获取，禁止硬编码
     *
     * 代码质量提升说明：
     * - 移除未被引用的 DEFAULT_REDIS_PORT 和 DEFAULT_REDIS_DATABASE 常量
     * - 这些常量标注为"仅内部逻辑参考"但实际未被任何代码引用
     * - 移除冗余代码提高代码质量和维护性
     *
     * @see com.honyrun.config.UnifiedConfigManager#getStringConfig(String, String)
     */
    public static final String REDIS_HOST_CONFIG_KEY = "HONYRUN_REDIS_HOST";

    /**
     * 默认Redis连接超时时间（毫秒）
     */
    public static final int DEFAULT_REDIS_TIMEOUT = 5000;

    /**
     * 默认Redis连接池大小
     */
    public static final int DEFAULT_REDIS_POOL_SIZE = 8;

    // ==================== 日志配置 ====================

    /**
     * 默认日志级别
     */
    public static final String DEFAULT_LOG_LEVEL = "INFO";

    /**
     * 默认日志文件路径
     */
    public static final String DEFAULT_LOG_PATH = "./logs";

    /**
     * 默认日志文件名
     */
    public static final String DEFAULT_LOG_FILENAME = "honyrun";

    /**
     * 默认日志文件最大大小
     */
    public static final String DEFAULT_LOG_MAX_SIZE = "100MB";

    /**
     * 默认日志文件保留天数
     */
    public static final int DEFAULT_LOG_MAX_DAYS = 30;

    /**
     * 默认日志文件最大数量
     */
    public static final int DEFAULT_LOG_MAX_FILES = 10;

    // ==================== 线程池配置 ====================

    /**
     * 默认核心线程数
     */
    public static final int DEFAULT_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * 默认最大线程数
     */
    public static final int DEFAULT_MAX_POOL_SIZE = DEFAULT_CORE_POOL_SIZE * 2;

    /**
     * 默认线程空闲时间（秒）
     */
    public static final int DEFAULT_KEEP_ALIVE_TIME = 60;

    /**
     * 默认队列容量
     */
    public static final int DEFAULT_QUEUE_CAPACITY = 1000;

    // ==================== 文件上传配置 ====================

    /**
     * 默认文件上传路径
     */
    public static final String DEFAULT_UPLOAD_PATH = "./uploads";

    /**
     * 默认文件最大大小（字节）
     */
    public static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * 默认请求最大大小（字节）
     */
    public static final long DEFAULT_MAX_REQUEST_SIZE = 50 * 1024 * 1024; // 50MB

    /**
     * 支持的图片格式
     */
    public static final String[] SUPPORTED_IMAGE_FORMATS = { "jpg", "jpeg", "png", "gif", "bmp", "webp" };

    /**
     * 支持的文档格式
     */
    public static final String[] SUPPORTED_DOCUMENT_FORMATS = { "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt" };

    // ==================== 缓存配置 ====================

    /**
     * 默认缓存过期时间（分钟）
     */
    public static final int DEFAULT_CACHE_EXPIRE_MINUTES = 30;

    /**
     * 默认缓存最大条目数
     */
    public static final int DEFAULT_CACHE_MAX_ENTRIES = 1000;

    /**
     * 默认缓存刷新时间（分钟）
     */
    public static final int DEFAULT_CACHE_REFRESH_MINUTES = 5;

    // ==================== 安全配置 ====================

    /**
     * 默认密码最小长度
     */
    public static final int DEFAULT_PASSWORD_MIN_LENGTH = 6;

    /**
     * 默认密码最大长度
     */
    public static final int DEFAULT_PASSWORD_MAX_LENGTH = 20;

    /**
     * 默认登录失败最大次数
     */
    public static final int DEFAULT_MAX_LOGIN_ATTEMPTS = 5;

    /**
     * 默认账户锁定时间（分钟）
     */
    public static final int DEFAULT_ACCOUNT_LOCK_MINUTES = 30;

    /**
     * 默认会话超时时间（分钟）
     */
    public static final int DEFAULT_SESSION_TIMEOUT_MINUTES = 120;

    // ==================== 响应式配置 ====================

    /**
     * 默认背压缓冲区大小
     */
    public static final int DEFAULT_BACKPRESSURE_BUFFER_SIZE = 256;

    /**
     * 默认预取大小
     */
    public static final int DEFAULT_PREFETCH_SIZE = 32;

    /**
     * 默认响应式超时时间
     */
    public static final Duration DEFAULT_REACTIVE_TIMEOUT = Duration.ofSeconds(30);

    /**
     * 默认重试次数
     */
    public static final int DEFAULT_RETRY_ATTEMPTS = 3;

    /**
     * 默认重试延迟
     */
    public static final Duration DEFAULT_RETRY_DELAY = Duration.ofMillis(500);

    // ==================== 监控配置 ====================

    /**
     * 默认健康检查间隔（秒）
     */
    public static final int DEFAULT_HEALTH_CHECK_INTERVAL = 30;

    /**
     * 默认指标收集间隔（秒）
     */
    public static final int DEFAULT_METRICS_INTERVAL = 60;

    /**
     * 默认性能监控间隔（秒）
     */
    public static final int DEFAULT_PERFORMANCE_MONITOR_INTERVAL = 10;

    /**
     * 默认告警阈值（CPU使用率百分比）
     */
    public static final double DEFAULT_CPU_ALERT_THRESHOLD = 80.0;

    /**
     * 默认告警阈值（内存使用率百分比）
     */
    public static final double DEFAULT_MEMORY_ALERT_THRESHOLD = 85.0;

    /**
     * 默认告警阈值（磁盘使用率百分比）
     */
    public static final double DEFAULT_DISK_ALERT_THRESHOLD = 90.0;

    // ==================== 业务配置 ====================

    /**
     * 默认批处理大小
     */
    public static final int DEFAULT_BATCH_SIZE = 100;

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大分页大小
     */
    public static final int MAX_PAGE_SIZE = 1000;

    /**
     * 默认查询超时时间（秒）
     */
    public static final int DEFAULT_QUERY_TIMEOUT = 30;

    /**
     * 默认导出超时时间（秒）
     */
    public static final int DEFAULT_EXPORT_TIMEOUT = 300;

    // ==================== 系统状态 ====================

    /**
     * 系统状态：正常
     */
    public static final String SYSTEM_STATUS_NORMAL = "NORMAL";

    /**
     * 系统状态：维护中
     */
    public static final String SYSTEM_STATUS_MAINTENANCE = "MAINTENANCE";

    /**
     * 系统状态：升级中
     */
    public static final String SYSTEM_STATUS_UPGRADING = "UPGRADING";

    /**
     * 系统状态：异常
     */
    public static final String SYSTEM_STATUS_ERROR = "ERROR";

    // ==================== 用户类型 ====================

    /**
     * 系统用户类型
     * 具有系统管理权限的用户类型标识
     */
    public static final String USER_TYPE_SYSTEM = "SYSTEM_USER";

    /**
     * 普通用户类型
     * 一般业务用户类型标识
     */
    public static final String USER_TYPE_NORMAL = "NORMAL_USER";

    /**
     * 访客用户类型
     * 临时访问用户或受限用户类型标识
     */
    public static final String USER_TYPE_GUEST = "GUEST";

    // ==================== 预定义用户名 ====================

    /**
     * 系统用户名1
     * 用于系统管理和维护的预定义用户名
     */
    public static final String SYSTEM_USER_NAME = "honyrun-sys";

    /**
     * 系统用户名2
     * 用于系统管理和维护的预定义用户名
     */
    public static final String SYSTEM_USER_NAME_2 = "honyrunsys2";

    /**
     * 普通用户名1
     * 用于测试的预定义普通用户名
     */
    public static final String NORMAL_USER_NAME_1 = "user1";

    /**
     * 普通用户名2
     * 用于测试的预定义普通用户名
     */
    public static final String NORMAL_USER_NAME_2 = "user2";

    // ==================== 数据格式 ====================

    /**
     * 日期时间格式
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 时间格式
     */
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 时间戳格式
     */
    public static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss";

    /**
     * 数字格式
     */
    public static final String NUMBER_FORMAT = "#,##0.00";

    // ==================== 正则表达式 ====================

    /**
     * 用户名正则表达式
     */
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_-]{3,20}$";

    /**
     * 密码正则表达式
     */
    public static final String PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,20}$";

    /**
     * 邮箱正则表达式
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    /**
     * 手机号正则表达式
     */
    public static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    /**
     * IP地址正则表达式
     */
    public static final String IP_REGEX = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
}
