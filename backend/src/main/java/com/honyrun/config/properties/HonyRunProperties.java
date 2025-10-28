package com.honyrun.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HonyRun自定义属性配置
 *
 * 定义应用程序的自定义配置属性，支持外部化配置
 * 提供类型安全的配置属性绑定和验证
 *
 * 主要配置：
 * - 应用基本信息配置
 * - 安全相关配置
 * - 缓存配置
 * - 日志配置
 * - 性能监控配置
 *
 * 配置特性：
 * - 类型安全：使用强类型配置属性
 * - 外部化配置：支持从配置文件加载
 * - 默认值：提供合理的默认配置值
 * - 验证支持：支持配置属性验证
 *
 * 响应式特性：
 * - 配置热更新：支持配置的动态更新
 * - 非阻塞配置：配置加载不阻塞应用启动
 * - 配置监控：支持配置变更监控
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 01:00:00
 * @modified 2025-07-01 01:00:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
@ConfigurationProperties(prefix = "honyrun")
public class HonyRunProperties {

    /**
     * 应用信息配置
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private App app;

    /**
     * 安全配置
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private Security security;

    /**
     * 缓存配置
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private Cache cache;

    /**
     * 日志配置
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private Logging logging;

    /**
     * 性能监控配置
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private Performance performance;

    /**
     * 调度器配置
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private Scheduler scheduler;

    // Getters and Setters
    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public Logging getLogging() {
        return logging;
    }

    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 应用信息配置类
     *
     * 【重要】：所有配置值必须从环境配置文件中读取
     * 配置文件路径：
     * - 开发环境：application-dev.properties
     * - 测试环境：application-test.properties
     * - 生产环境：application-prod.properties
     *
     * 或通过环境变量设置：
     * - HONYRUN_APP_NAME
     * - HONYRUN_APP_VERSION
     * - HONYRUN_APP_DESCRIPTION
     * - HONYRUN_APP_ENVIRONMENT
     * - HONYRUN_APP_PORT
     */
    public static class App {
        /**
         * 应用名称 - 必须从配置文件获取：honyrun.app.name
         */
        private String name;

        /**
         * 应用版本 - 必须从配置文件获取：honyrun.app.version
         */
        private String version;

        /**
         * 应用描述 - 必须从配置文件获取：honyrun.app.description
         */
        private String description;

        /**
         * 应用环境 - 必须从配置文件获取：honyrun.app.environment
         */
        private String environment;

        /**
         * 应用端口 - 必须从配置文件获取：honyrun.app.port
         */
        private int port;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    /**
     * 安全配置类
     *
     * 【重要】：所有配置值必须从环境配置文件中读取
     * 配置文件路径：
     * - 开发环境：application-dev.properties
     * - 测试环境：application-test.properties
     * - 生产环境：application-prod.properties
     *
     * 或通过环境变量设置：
     * - HONYRUN_SECURITY_JWT_SECRET
     * - HONYRUN_SECURITY_JWT_EXPIRATION
     * - HONYRUN_SECURITY_REFRESH_TOKEN_EXPIRATION
     * - HONYRUN_SECURITY_PASSWORD_STRENGTH
     * - HONYRUN_SECURITY_MAX_LOGIN_ATTEMPTS
     * - HONYRUN_SECURITY_LOCKOUT_DURATION
     */
    public static class Security {
        /**
         * JWT密钥 - 必须从配置文件获取：honyrun.security.jwt-secret
         */
        private String jwtSecret;

        /**
         * JWT过期时间 - 必须从配置文件获取：honyrun.security.jwt-expiration
         */
        private Duration jwtExpiration;

        /**
         * 刷新令牌过期时间 - 必须从配置文件获取：honyrun.security.refresh-token-expiration
         */
        private Duration refreshTokenExpiration;

        /**
         * 密码加密强度 - 必须从配置文件获取：honyrun.security.password-strength
         */
        private int passwordStrength;

        /**
         * 最大登录尝试次数 - 必须从配置文件获取：honyrun.security.max-login-attempts
         */
        private int maxLoginAttempts;

        /**
         * 账户锁定时间 - 必须从配置文件获取：honyrun.security.lockout-duration
         */
        private Duration lockoutDuration;

        // Getters and Setters
        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public Duration getJwtExpiration() {
            return jwtExpiration;
        }

        public void setJwtExpiration(Duration jwtExpiration) {
            this.jwtExpiration = jwtExpiration;
        }

        public Duration getRefreshTokenExpiration() {
            return refreshTokenExpiration;
        }

        public void setRefreshTokenExpiration(Duration refreshTokenExpiration) {
            this.refreshTokenExpiration = refreshTokenExpiration;
        }

        public int getPasswordStrength() {
            return passwordStrength;
        }

        public void setPasswordStrength(int passwordStrength) {
            this.passwordStrength = passwordStrength;
        }

        public int getMaxLoginAttempts() {
            return maxLoginAttempts;
        }

        public void setMaxLoginAttempts(int maxLoginAttempts) {
            this.maxLoginAttempts = maxLoginAttempts;
        }

        public Duration getLockoutDuration() {
            return lockoutDuration;
        }

        public void setLockoutDuration(Duration lockoutDuration) {
            this.lockoutDuration = lockoutDuration;
        }
    }

    /**
     * 缓存配置类
     *
     * 【重要】：所有配置值必须从环境配置文件中读取
     * 配置文件路径：
     * - 开发环境：application-dev.properties
     * - 测试环境：application-test.properties
     * - 生产环境：application-prod.properties
     *
     * 或通过环境变量设置：
     * - HONYRUN_CACHE_TYPE
     * - HONYRUN_CACHE_PREFIX
     * - HONYRUN_CACHE_DEFAULT_EXPIRATION
     * - HONYRUN_CACHE_USER_CACHE_EXPIRATION
     * - HONYRUN_CACHE_SYSTEM_CACHE_EXPIRATION
     */
    public static class Cache {
        /**
         * 缓存类型 - 必须从配置文件获取：honyrun.cache.type
         */
        private String type;

        /**
         * 缓存前缀 - 必须从配置文件获取：honyrun.cache.prefix
         */
        private String prefix;

        /**
         * 默认过期时间 - 必须从配置文件获取：honyrun.cache.default-expiration
         */
        private Duration defaultExpiration;

        /**
         * 用户缓存过期时间 - 必须从配置文件获取：honyrun.cache.user-cache-expiration
         */
        private Duration userCacheExpiration;

        /**
         * 系统缓存过期时间 - 必须从配置文件获取：honyrun.cache.system-cache-expiration
         */
        private Duration systemCacheExpiration;

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public Duration getDefaultExpiration() {
            return defaultExpiration;
        }

        public void setDefaultExpiration(Duration defaultExpiration) {
            this.defaultExpiration = defaultExpiration;
        }

        public Duration getUserCacheExpiration() {
            return userCacheExpiration;
        }

        public void setUserCacheExpiration(Duration userCacheExpiration) {
            this.userCacheExpiration = userCacheExpiration;
        }

        public Duration getSystemCacheExpiration() {
            return systemCacheExpiration;
        }

        public void setSystemCacheExpiration(Duration systemCacheExpiration) {
            this.systemCacheExpiration = systemCacheExpiration;
        }
    }

    /**
     * 日志配置类
     *
     * 【重要】：所有配置值必须从环境配置文件中读取
     * 配置文件路径：
     * - 开发环境：application-dev.properties
     * - 测试环境：application-test.properties
     * - 生产环境：application-prod.properties
     *
     * 或通过环境变量设置：
     * - HONYRUN_LOGGING_LEVEL
     * - HONYRUN_LOGGING_FILE_PATH
     * - HONYRUN_LOGGING_MAX_FILE_SIZE
     * - HONYRUN_LOGGING_MAX_HISTORY
     * - HONYRUN_LOGGING_ACCESS_LOG_ENABLED
     */
    public static class Logging {
        /**
         * 日志级别 - 必须从配置文件获取：honyrun.logging.level
         */
        private String level;

        /**
         * 日志文件路径 - 必须从配置文件获取：honyrun.logging.file-path
         */
        private String filePath;

        /**
         * 日志文件最大大小 - 必须从配置文件获取：honyrun.logging.max-file-size
         */
        private String maxFileSize;

        /**
         * 日志文件保留天数 - 必须从配置文件获取：honyrun.logging.max-history
         */
        private int maxHistory;

        /**
         * 是否启用访问日志 - 必须从配置文件获取：honyrun.logging.access-log-enabled
         */
        private boolean accessLogEnabled;

        // Getters and Setters
        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(String maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public int getMaxHistory() {
            return maxHistory;
        }

        public void setMaxHistory(int maxHistory) {
            this.maxHistory = maxHistory;
        }

        public boolean isAccessLogEnabled() {
            return accessLogEnabled;
        }

        public void setAccessLogEnabled(boolean accessLogEnabled) {
            this.accessLogEnabled = accessLogEnabled;
        }
    }

    /**
     * 性能监控配置类
     *
     * 【重要】：所有配置值必须从环境配置文件中读取
     * 配置文件路径：
     * - 开发环境：application-dev.properties
     * - 测试环境：application-test.properties
     * - 生产环境：application-prod.properties
     *
     * 或通过环境变量设置：
     * - HONYRUN_PERFORMANCE_ENABLED
     * - HONYRUN_PERFORMANCE_RESPONSE_TIME_THRESHOLD
     * - HONYRUN_PERFORMANCE_MEMORY_THRESHOLD
     * - HONYRUN_PERFORMANCE_CPU_THRESHOLD
     * - HONYRUN_PERFORMANCE_MONITOR_INTERVAL
     */
    public static class Performance {
        /**
         * 是否启用性能监控 - 必须从配置文件获取：honyrun.performance.enabled
         */
        private boolean enabled;

        /**
         * 响应时间阈值（毫秒）- 必须从配置文件获取：honyrun.performance.response-time-threshold
         */
        private long responseTimeThreshold;

        /**
         * 内存使用阈值（百分比）- 必须从配置文件获取：honyrun.performance.memory-threshold
         */
        private double memoryThreshold;

        /**
         * CPU使用阈值（百分比）- 必须从配置文件获取：honyrun.performance.cpu-threshold
         */
        private double cpuThreshold;

        /**
         * 监控间隔 - 必须从配置文件获取：honyrun.performance.monitor-interval
         */
        private Duration monitorInterval;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getResponseTimeThreshold() {
            return responseTimeThreshold;
        }

        public void setResponseTimeThreshold(long responseTimeThreshold) {
            this.responseTimeThreshold = responseTimeThreshold;
        }

        public double getMemoryThreshold() {
            return memoryThreshold;
        }

        public void setMemoryThreshold(double memoryThreshold) {
            this.memoryThreshold = memoryThreshold;
        }

        public double getCpuThreshold() {
            return cpuThreshold;
        }

        public void setCpuThreshold(double cpuThreshold) {
            this.cpuThreshold = cpuThreshold;
        }

        public Duration getMonitorInterval() {
            return monitorInterval;
        }

        public void setMonitorInterval(Duration monitorInterval) {
            this.monitorInterval = monitorInterval;
        }
    }

    /**
     * 调度器配置类
     *
     * 【重要】：所有配置值必须从环境配置文件中读取
     * 配置文件路径：
     * - 开发环境：application-dev.properties
     * - 测试环境：application-test.properties
     * - 生产环境：application-prod.properties
     *
     * 或通过环境变量设置
     */
    public static class Scheduler {
        /**
         * 系统维护调度器配置 - 必须从配置文件获取
         */
        private SystemMaintenance systemMaintenance;

        /**
         * 日志清理调度器配置 - 必须从配置文件获取
         */
        private LogCleanup logCleanup;

        /**
         * 缓存刷新调度器配置 - 必须从配置文件获取
         */
        private CacheRefresh cacheRefresh;

        /**
         * 性能监控调度器配置 - 必须从配置文件获取
         */
        private PerformanceMonitor performanceMonitor;

        /**
         * 用户有效期检查调度器配置 - 必须从配置文件获取
         */
        private UserExpiryCheck userExpiryCheck;

        // Getters and Setters
        public SystemMaintenance getSystemMaintenance() {
            return systemMaintenance;
        }

        public void setSystemMaintenance(SystemMaintenance systemMaintenance) {
            this.systemMaintenance = systemMaintenance;
        }

        public LogCleanup getLogCleanup() {
            return logCleanup;
        }

        public void setLogCleanup(LogCleanup logCleanup) {
            this.logCleanup = logCleanup;
        }

        public CacheRefresh getCacheRefresh() {
            return cacheRefresh;
        }

        public void setCacheRefresh(CacheRefresh cacheRefresh) {
            this.cacheRefresh = cacheRefresh;
        }

        public PerformanceMonitor getPerformanceMonitor() {
            return performanceMonitor;
        }

        public void setPerformanceMonitor(PerformanceMonitor performanceMonitor) {
            this.performanceMonitor = performanceMonitor;
        }

        public UserExpiryCheck getUserExpiryCheck() {
            return userExpiryCheck;
        }

        public void setUserExpiryCheck(UserExpiryCheck userExpiryCheck) {
            this.userExpiryCheck = userExpiryCheck;
        }

        /**
         * 系统维护调度器配置
         *
         * 【重要】：所有配置值必须从环境配置文件中读取
         * 配置文件路径：
         * - 开发环境：application-dev.properties
         * - 测试环境：application-test.properties
         * - 生产环境：application-prod.properties
         */
        public static class SystemMaintenance {
            /**
             * 是否启用 - 必须从配置文件获取：honyrun.scheduler.system-maintenance.enabled
             */
            private boolean enabled;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }

        /**
         * 日志清理调度器配置
         *
         * 【重要】：所有配置值必须从环境配置文件中读取
         * 配置文件路径：
         * - 开发环境：application-dev.properties
         * - 测试环境：application-test.properties
         * - 生产环境：application-prod.properties
         */
        public static class LogCleanup {
            /**
             * 是否启用 - 必须从配置文件获取：honyrun.scheduler.log-cleanup.enabled
             */
            private boolean enabled;

            /**
             * 应用日志保留天数 - 必须从配置文件获取：honyrun.scheduler.log-cleanup.application-log-days
             */
            private int applicationLogDays;

            /**
             * 错误日志保留天数 - 必须从配置文件获取：honyrun.scheduler.log-cleanup.error-log-days
             */
            private int errorLogDays;

            /**
             * 访问日志保留天数 - 必须从配置文件获取：honyrun.scheduler.log-cleanup.access-log-days
             */
            private int accessLogDays;

            /**
             * 业务日志保留天数 - 必须从配置文件获取：honyrun.scheduler.log-cleanup.business-log-days
             */
            private int businessLogDays;

            /**
             * 系统日志保留天数 - 必须从配置文件获取：honyrun.scheduler.log-cleanup.system-log-days
             */
            private int systemLogDays;

            /**
             * 是否启用压缩 - 必须从配置文件获取：honyrun.scheduler.log-cleanup.compress-enabled
             */
            private boolean compressEnabled;

            /**
             * 是否启用归档 - 必须从配置文件获取：honyrun.scheduler.log-cleanup.archive-enabled
             */
            private boolean archiveEnabled;

            // Getters and Setters
            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public int getApplicationLogDays() {
                return applicationLogDays;
            }

            public void setApplicationLogDays(int applicationLogDays) {
                this.applicationLogDays = applicationLogDays;
            }

            public int getErrorLogDays() {
                return errorLogDays;
            }

            public void setErrorLogDays(int errorLogDays) {
                this.errorLogDays = errorLogDays;
            }

            public int getAccessLogDays() {
                return accessLogDays;
            }

            public void setAccessLogDays(int accessLogDays) {
                this.accessLogDays = accessLogDays;
            }

            public int getBusinessLogDays() {
                return businessLogDays;
            }

            public void setBusinessLogDays(int businessLogDays) {
                this.businessLogDays = businessLogDays;
            }

            public int getSystemLogDays() {
                return systemLogDays;
            }

            public void setSystemLogDays(int systemLogDays) {
                this.systemLogDays = systemLogDays;
            }

            public boolean isCompressEnabled() {
                return compressEnabled;
            }

            public void setCompressEnabled(boolean compressEnabled) {
                this.compressEnabled = compressEnabled;
            }

            public boolean isArchiveEnabled() {
                return archiveEnabled;
            }

            public void setArchiveEnabled(boolean archiveEnabled) {
                this.archiveEnabled = archiveEnabled;
            }
        }

        /**
         * 缓存刷新调度器配置
         *
         * 【重要】：所有配置值必须从环境配置文件中读取
         * 配置文件路径：
         * - 开发环境：application-dev.properties
         * - 测试环境：application-test.properties
         * - 生产环境：application-prod.properties
         */
        public static class CacheRefresh {
            /**
             * 是否启用 - 必须从配置文件获取：honyrun.scheduler.cache-refresh.enabled
             */
            private boolean enabled;

            /**
             * 用户缓存是否启用 - 必须从配置文件获取：honyrun.scheduler.cache-refresh.user-cache-enabled
             */
            private boolean userCacheEnabled;

            /**
             * 权限缓存是否启用 - 必须从配置文件获取：honyrun.scheduler.cache-refresh.permission-cache-enabled
             */
            private boolean permissionCacheEnabled;

            /**
             * 配置缓存是否启用 - 必须从配置文件获取：honyrun.scheduler.cache-refresh.config-cache-enabled
             */
            private boolean configCacheEnabled;

            /**
             * 业务缓存是否启用 - 必须从配置文件获取：honyrun.scheduler.cache-refresh.business-cache-enabled
             */
            private boolean businessCacheEnabled;

            /**
             * 预加载是否启用 - 必须从配置文件获取：honyrun.scheduler.cache-refresh.preload-enabled
             */
            private boolean preloadEnabled;

            /**
             * 批处理大小 - 必须从配置文件获取：honyrun.scheduler.cache-refresh.batch-size
             */
            private int batchSize;

            // Getters and Setters
            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public boolean isUserCacheEnabled() {
                return userCacheEnabled;
            }

            public void setUserCacheEnabled(boolean userCacheEnabled) {
                this.userCacheEnabled = userCacheEnabled;
            }

            public boolean isPermissionCacheEnabled() {
                return permissionCacheEnabled;
            }

            public void setPermissionCacheEnabled(boolean permissionCacheEnabled) {
                this.permissionCacheEnabled = permissionCacheEnabled;
            }

            public boolean isConfigCacheEnabled() {
                return configCacheEnabled;
            }

            public void setConfigCacheEnabled(boolean configCacheEnabled) {
                this.configCacheEnabled = configCacheEnabled;
            }

            public boolean isBusinessCacheEnabled() {
                return businessCacheEnabled;
            }

            public void setBusinessCacheEnabled(boolean businessCacheEnabled) {
                this.businessCacheEnabled = businessCacheEnabled;
            }

            public boolean isPreloadEnabled() {
                return preloadEnabled;
            }

            public void setPreloadEnabled(boolean preloadEnabled) {
                this.preloadEnabled = preloadEnabled;
            }

            public int getBatchSize() {
                return batchSize;
            }

            public void setBatchSize(int batchSize) {
                this.batchSize = batchSize;
            }
        }

        /**
         * 性能监控调度器配置
         *
         * 【重要】：所有配置值必须从环境配置文件中读取
         * 配置文件路径：
         * - 开发环境：application-dev.properties
         * - 测试环境：application-test.properties
         * - 生产环境：application-prod.properties
         */
        public static class PerformanceMonitor {
            /**
             * 是否启用 - 必须从配置文件获取：honyrun.scheduler.performance-monitor.enabled
             */
            private boolean enabled;

            /**
             * WebFlux监控是否启用 -
             * 必须从配置文件获取：honyrun.scheduler.performance-monitor.webflux-enabled
             */
            private boolean webfluxEnabled;

            /**
             * R2DBC监控是否启用 - 必须从配置文件获取：honyrun.scheduler.performance-monitor.r2dbc-enabled
             */
            private boolean r2dbcEnabled;

            /**
             * Redis监控是否启用 - 必须从配置文件获取：honyrun.scheduler.performance-monitor.redis-enabled
             */
            private boolean redisEnabled;

            /**
             * 系统资源监控是否启用 -
             * 必须从配置文件获取：honyrun.scheduler.performance-monitor.system-resource-enabled
             */
            private boolean systemResourceEnabled;

            /**
             * 响应式流监控是否启用 -
             * 必须从配置文件获取：honyrun.scheduler.performance-monitor.reactive-stream-enabled
             */
            private boolean reactiveStreamEnabled;

            /**
             * 响应时间阈值 -
             * 必须从配置文件获取：honyrun.scheduler.performance-monitor.response-time-threshold
             */
            private long responseTimeThreshold;

            /**
             * 内存阈值 - 必须从配置文件获取：honyrun.scheduler.performance-monitor.memory-threshold
             */
            private double memoryThreshold;

            /**
             * CPU阈值 - 必须从配置文件获取：honyrun.scheduler.performance-monitor.cpu-threshold
             */
            private double cpuThreshold;

            // Getters and Setters
            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public boolean isWebfluxEnabled() {
                return webfluxEnabled;
            }

            public void setWebfluxEnabled(boolean webfluxEnabled) {
                this.webfluxEnabled = webfluxEnabled;
            }

            public boolean isR2dbcEnabled() {
                return r2dbcEnabled;
            }

            public void setR2dbcEnabled(boolean r2dbcEnabled) {
                this.r2dbcEnabled = r2dbcEnabled;
            }

            public boolean isRedisEnabled() {
                return redisEnabled;
            }

            public void setRedisEnabled(boolean redisEnabled) {
                this.redisEnabled = redisEnabled;
            }

            public boolean isSystemResourceEnabled() {
                return systemResourceEnabled;
            }

            public void setSystemResourceEnabled(boolean systemResourceEnabled) {
                this.systemResourceEnabled = systemResourceEnabled;
            }

            public boolean isReactiveStreamEnabled() {
                return reactiveStreamEnabled;
            }

            public void setReactiveStreamEnabled(boolean reactiveStreamEnabled) {
                this.reactiveStreamEnabled = reactiveStreamEnabled;
            }

            public long getResponseTimeThreshold() {
                return responseTimeThreshold;
            }

            public void setResponseTimeThreshold(long responseTimeThreshold) {
                this.responseTimeThreshold = responseTimeThreshold;
            }

            public double getMemoryThreshold() {
                return memoryThreshold;
            }

            public void setMemoryThreshold(double memoryThreshold) {
                this.memoryThreshold = memoryThreshold;
            }

            public double getCpuThreshold() {
                return cpuThreshold;
            }

            public void setCpuThreshold(double cpuThreshold) {
                this.cpuThreshold = cpuThreshold;
            }
        }

        /**
         * 用户有效期检查调度器配置
         *
         * 【重要】：所有配置值必须从环境配置文件中读取
         * 配置文件路径：
         * - 开发环境：application-dev.properties
         * - 测试环境：application-test.properties
         * - 生产环境：application-prod.properties
         */
        public static class UserExpiryCheck {
            /**
             * 是否启用 - 必须从配置文件获取：honyrun.scheduler.user-expiry-check.enabled
             */
            private boolean enabled;

            /**
             * 警告天数 - 必须从配置文件获取：honyrun.scheduler.user-expiry-check.warning-days
             */
            private int warningDays;

            /**
             * 自动禁用是否启用 - 必须从配置文件获取：honyrun.scheduler.user-expiry-check.auto-disable-enabled
             */
            private boolean autoDisableEnabled;

            /**
             * 通知是否启用 - 必须从配置文件获取：honyrun.scheduler.user-expiry-check.notification-enabled
             */
            private boolean notificationEnabled;

            /**
             * 清理禁用用户天数 -
             * 必须从配置文件获取：honyrun.scheduler.user-expiry-check.cleanup-disabled-days
             */
            private int cleanupDisabledDays;

            /**
             * 批处理大小 - 必须从配置文件获取：honyrun.scheduler.user-expiry-check.batch-size
             */
            private int batchSize;

            // Getters and Setters
            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public int getWarningDays() {
                return warningDays;
            }

            public void setWarningDays(int warningDays) {
                this.warningDays = warningDays;
            }

            public boolean isAutoDisableEnabled() {
                return autoDisableEnabled;
            }

            public void setAutoDisableEnabled(boolean autoDisableEnabled) {
                this.autoDisableEnabled = autoDisableEnabled;
            }

            public boolean isNotificationEnabled() {
                return notificationEnabled;
            }

            public void setNotificationEnabled(boolean notificationEnabled) {
                this.notificationEnabled = notificationEnabled;
            }

            public int getCleanupDisabledDays() {
                return cleanupDisabledDays;
            }

            public void setCleanupDisabledDays(int cleanupDisabledDays) {
                this.cleanupDisabledDays = cleanupDisabledDays;
            }

            public int getBatchSize() {
                return batchSize;
            }

            public void setBatchSize(int batchSize) {
                this.batchSize = batchSize;
            }
        }
    }
}
