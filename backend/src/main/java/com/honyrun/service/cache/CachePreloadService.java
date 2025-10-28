package com.honyrun.service.cache;

import com.honyrun.constant.CacheConstants;
import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 缓存预热服务
 *
 * <p>
 * <strong>功能说明：</strong>
 * </p>
 * <ul>
 * <li>系统启动时预热关键缓存数据</li>
 * <li>支持分批预热和异步预热</li>
 * <li>提供预热进度监控</li>
 * <li>支持预热失败重试机制</li>
 * </ul>
 *
 * <p>
 * <strong>预热策略：</strong>
 * </p>
 * <ol>
 * <li>系统配置数据预热</li>
 * <li>用户权限模板预热</li>
 * <li>常用业务数据预热</li>
 * <li>统计数据预热</li>
 * </ol>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-26 01:36:12
 * @version 1.0.0
 */
@Service
public class CachePreloadService {

    private static final Logger logger = LoggerFactory.getLogger(CachePreloadService.class);

    private final ReactiveCacheService cacheService;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final DatabaseClient databaseClient;
    private final UnifiedConfigManager unifiedConfigManager;

    private final AtomicInteger preloadProgress = new AtomicInteger(0);
    private volatile boolean preloadCompleted = false;
    private volatile boolean preloadInProgress = false;

    /**
     * 构造函数注入依赖
     *
     * @param cacheService 响应式缓存服务
     * @param redisTemplate Redis模板
     * @param databaseClient 数据库客户端
     * @param unifiedConfigManager 统一配置管理器
     */
    public CachePreloadService(ReactiveCacheService cacheService,
                              @Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> redisTemplate,
                              DatabaseClient databaseClient,
                              UnifiedConfigManager unifiedConfigManager) {
        this.cacheService = cacheService;
        this.redisTemplate = redisTemplate;
        this.databaseClient = databaseClient;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 应用启动完成后执行缓存预热
     *
     * @param event 应用就绪事件
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        LoggingUtil.info(logger, "应用启动完成，开始执行缓存预热...");

        // 延迟5秒后开始预热，确保所有服务都已就绪
        Mono.delay(Duration.ofSeconds(5))
            .then(executePreload())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                result -> LoggingUtil.info(logger, "缓存预热完成，预热数据条数: {}", result),
                error -> LoggingUtil.error(logger, "缓存预热失败", error)
            );
    }

    /**
     * 执行缓存预热
     *
     * @return 预热数据条数
     */
    public Mono<Integer> executePreload() {
        if (preloadInProgress) {
            LoggingUtil.warn(logger, "缓存预热正在进行中，跳过重复执行");
            return Mono.just(preloadProgress.get());
        }

        preloadInProgress = true;
        preloadProgress.set(0);
        preloadCompleted = false;

        LoggingUtil.info(logger, "开始执行缓存预热...");

        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "缓存预热开始");
            return 0;
        })
        .then(preloadSystemConfigs())
        .then(preloadUserPermissionTemplates())
        .then(preloadBusinessData())
        .then(preloadStatisticsData())
        .doOnSuccess(count -> {
            preloadCompleted = true;
            preloadInProgress = false;
            LoggingUtil.info(logger, "缓存预热完成，总计预热 {} 条数据", count);
        })
        .doOnError(error -> {
            preloadInProgress = false;
            LoggingUtil.error(logger, "缓存预热失败", error);
        })
        .onErrorReturn(0);
    }

    /**
     * 预热系统配置数据
     *
     * @return 预热数据条数
     */
    private Mono<Integer> preloadSystemConfigs() {
        LoggingUtil.info(logger, "开始预热系统配置数据...");

        return databaseClient
            .sql("SELECT config_key, config_value, config_group, config_type, description " +
                 "FROM sys_system_configs WHERE deleted = 0 AND status = 'ACTIVE'")
            .fetch()
            .all()
            .flatMap(config -> {
                String cacheKey = CacheConstants.SYSTEM_CONFIG_KEY_PREFIX + config.get("config_key");

                Map<String, Object> configData = new HashMap<>();
                configData.put("value", config.get("config_value"));
                configData.put("group", config.get("config_group"));
                configData.put("type", config.get("config_type"));
                configData.put("description", config.get("description"));

                return cacheService.set(cacheKey, configData, CacheConstants.SYSTEM_CONFIG_EXPIRE_DURATION)
                    .doOnSuccess(success -> {
                        if (success) {
                            preloadProgress.incrementAndGet();
                            LoggingUtil.debug(logger, "预热系统配置: {}", cacheKey);
                        }
                    })
                    .onErrorResume(error -> {
                        LoggingUtil.warn(logger, "预热系统配置失败: " + cacheKey, error);
                        return Mono.just(false);
                    });
            })
            .count()
            .map(Long::intValue)
            .doOnSuccess(count -> LoggingUtil.info(logger, "系统配置数据预热完成，预热 {} 条", count));
    }

    /**
     * 预热用户权限模板数据
     *
     * @return 预热数据条数
     */
    private Mono<Integer> preloadUserPermissionTemplates() {
        LoggingUtil.info(logger, "开始预热用户权限模板数据...");

        return databaseClient
            .sql("SELECT user_type, permission_codes, permission_names " +
                 "FROM sys_user_type_permissions WHERE deleted = 0")
            .fetch()
            .all()
            .flatMap(permission -> {
                String userType = (String) permission.get("user_type");
                String cacheKey = CacheConstants.USER_PERMISSION_KEY_PREFIX + "template:" + userType;

                Map<String, Object> permissionData = new HashMap<>();
                permissionData.put("userType", userType);
                permissionData.put("permissionCodes", permission.get("permission_codes"));
                permissionData.put("permissionNames", permission.get("permission_names"));

                return cacheService.set(cacheKey, permissionData, CacheConstants.USER_PERMISSION_EXPIRE_DURATION)
                    .doOnSuccess(success -> {
                        if (success) {
                            preloadProgress.incrementAndGet();
                            LoggingUtil.debug(logger, "预热权限模板: {}", cacheKey);
                        }
                    })
                    .onErrorResume(error -> {
                        LoggingUtil.warn(logger, "预热权限模板失败: " + cacheKey, error);
                        return Mono.just(false);
                    });
            })
            .count()
            .map(Long::intValue)
            .doOnSuccess(count -> LoggingUtil.info(logger, "用户权限模板数据预热完成，预热 {} 条", count));
    }

    /**
     * 预热业务数据
     *
     * @return 预热数据条数
     */
    private Mono<Integer> preloadBusinessData() {
        LoggingUtil.info(logger, "开始预热业务数据...");

        // 预热活跃用户数据
        return preloadActiveUsers()
            .then(preloadSystemStatus())
            .then(preloadDictionaryData())
            .map(count -> {
                LoggingUtil.info(logger, "业务数据预热完成");
                return count;
            });
    }

    /**
     * 预热活跃用户数据
     *
     * @return 预热数据条数
     */
    private Mono<Integer> preloadActiveUsers() {
        return databaseClient
            .sql("SELECT id, username, user_type, status " +
                 "FROM sys_users WHERE status = 'ACTIVE' " +
                 "AND last_login_at > DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                 "LIMIT 100")
            .fetch()
            .all()
            .flatMap(user -> {
                String userId = String.valueOf(user.get("id"));
                String cacheKey = CacheConstants.USER_INFO_KEY_PREFIX + userId;

                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", userId);
                userData.put("username", user.get("username"));
                userData.put("userType", user.get("user_type"));
                userData.put("status", user.get("status"));

                return cacheService.set(cacheKey, userData, CacheConstants.USER_INFO_EXPIRE_DURATION)
                    .doOnSuccess(success -> {
                        if (success) {
                            preloadProgress.incrementAndGet();
                            LoggingUtil.debug(logger, "预热用户数据: {}", cacheKey);
                        }
                    })
                    .onErrorResume(error -> {
                        LoggingUtil.warn(logger, "预热用户数据失败: " + cacheKey, error);
                        return Mono.just(false);
                    });
            })
            .count()
            .map(Long::intValue);
    }

    /**
     * 预热系统状态数据
     *
     * @return 预热数据条数
     */
    private Mono<Integer> preloadSystemStatus() {
        Map<String, Object> systemStatus = new HashMap<>();
        systemStatus.put("status", "RUNNING");
        systemStatus.put("version", "1.0.0");
        systemStatus.put("startTime", System.currentTimeMillis());
        systemStatus.put("environment", "production");

        String cacheKey = CacheConstants.SYSTEM_STATUS_KEY_PREFIX + "main";

        return cacheService.set(cacheKey, systemStatus, CacheConstants.SYSTEM_SETTING_EXPIRE_DURATION)
            .doOnSuccess(success -> {
                if (success) {
                    preloadProgress.incrementAndGet();
                    LoggingUtil.debug(logger, "预热系统状态: {}", cacheKey);
                }
            })
            .map(success -> success ? 1 : 0);
    }

    /**
     * 预热字典数据
     *
     * @return 预热数据条数
     */
    private Mono<Integer> preloadDictionaryData() {
        return databaseClient
            .sql("SELECT dict_type, dict_key, dict_value, dict_label " +
                 "FROM sys_dictionary WHERE deleted = 0 AND status = 'ACTIVE'")
            .fetch()
            .all()
            .flatMap(dict -> {
                String dictType = (String) dict.get("dict_type");
                String dictKey = (String) dict.get("dict_key");
                String cacheKey = CacheConstants.SYSTEM_CONFIG_KEY_PREFIX + "dict:" + dictType + ":" + dictKey;

                Map<String, Object> dictData = new HashMap<>();
                dictData.put("type", dictType);
                dictData.put("key", dictKey);
                dictData.put("value", dict.get("dict_value"));
                dictData.put("label", dict.get("dict_label"));

                return cacheService.set(cacheKey, dictData, CacheConstants.DICTIONARY_EXPIRE_DURATION)
                    .doOnSuccess(success -> {
                        if (success) {
                            preloadProgress.incrementAndGet();
                            LoggingUtil.debug(logger, "预热字典数据: {}", cacheKey);
                        }
                    })
                    .onErrorResume(error -> {
                        LoggingUtil.warn(logger, "预热字典数据失败: " + cacheKey, error);
                        return Mono.just(false);
                    });
            })
            .count()
            .map(Long::intValue);
    }

    /**
     * 预热统计数据
     *
     * @return 预热数据条数
     */
    private Mono<Integer> preloadStatisticsData() {
        LoggingUtil.info(logger, "开始预热统计数据...");

        return Flux.just(
                preloadUserStatistics(),
                preloadSystemStatistics(),
                preloadBusinessStatistics()
            )
            .flatMap(mono -> mono)
            .reduce(Integer::sum)
            .doOnSuccess(count -> LoggingUtil.info(logger, "统计数据预热完成，预热 {} 条", count));
    }

    /**
     * 预热用户统计数据
     *
     * @return 预热数据条数
     */
    private Mono<Integer> preloadUserStatistics() {
        return databaseClient
            .sql("SELECT " +
                 "COUNT(*) as total_users, " +
                 "COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_users, " +
                 "COUNT(CASE WHEN last_login_at > DATE_SUB(NOW(), INTERVAL 1 DAY) THEN 1 END) as daily_active_users " +
                 "FROM sys_users WHERE deleted = 0")
            .fetch()
            .first()
            .flatMap(stats -> {
                return unifiedConfigManager.getStringConfig("honyrun.cache.statistics.users-summary-suffix", "users:summary")
                    .map(suffix -> CacheConstants.STATISTICS_KEY_PREFIX + suffix)
                    .flatMap(cacheKey -> {
                        Map<String, Object> userStats = new HashMap<>();
                        userStats.put("totalUsers", stats.get("total_users"));
                        userStats.put("activeUsers", stats.get("active_users"));
                        userStats.put("dailyActiveUsers", stats.get("daily_active_users"));
                        userStats.put("updateTime", System.currentTimeMillis());

                        return cacheService.set(cacheKey, userStats, CacheConstants.STATISTICS_EXPIRE_DURATION)
                            .doOnSuccess(success -> {
                                if (success) {
                                    preloadProgress.incrementAndGet();
                                    LoggingUtil.debug(logger, "预热用户统计: {}", cacheKey);
                                }
                            })
                            .map(success -> success ? 1 : 0);
                    });
            });
    }

    /**
     * 预热系统统计数据
     *
     * @return 预热数据条数
     */
    private Mono<Integer> preloadSystemStatistics() {
        Map<String, Object> systemStats = new HashMap<>();
        systemStats.put("uptime", System.currentTimeMillis());
        systemStats.put("jvmMemoryUsed", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        systemStats.put("jvmMemoryMax", Runtime.getRuntime().maxMemory());
        systemStats.put("updateTime", System.currentTimeMillis());

        return unifiedConfigManager.getStringConfig("honyrun.cache.statistics.system-summary-suffix", "system:summary")
            .map(suffix -> CacheConstants.STATISTICS_KEY_PREFIX + suffix)
            .flatMap(cacheKey -> {
                return cacheService.set(cacheKey, systemStats, CacheConstants.STATISTICS_EXPIRE_DURATION)
                    .doOnSuccess(success -> {
                        if (success) {
                            preloadProgress.incrementAndGet();
                            LoggingUtil.debug(logger, "预热系统统计: {}", cacheKey);
                        }
                    })
                    .map(success -> success ? 1 : 0);
            });
    }

    /**
     * 预热业务统计数据
     *
     * @return 预热数据条数
     */
    private Mono<Integer> preloadBusinessStatistics() {
        // 这里可以根据具体业务需求添加业务统计数据的预热
        Map<String, Object> businessStats = new HashMap<>();
        businessStats.put("placeholder", "业务统计数据");
        businessStats.put("updateTime", System.currentTimeMillis());

        return unifiedConfigManager.getStringConfig("honyrun.cache.statistics.business-summary-suffix", "business:summary")
            .map(suffix -> CacheConstants.STATISTICS_KEY_PREFIX + suffix)
            .flatMap(cacheKey -> {
                return cacheService.set(cacheKey, businessStats, CacheConstants.STATISTICS_EXPIRE_DURATION)
                    .doOnSuccess(success -> {
                        if (success) {
                            preloadProgress.incrementAndGet();
                            LoggingUtil.debug(logger, "预热业务统计: {}", cacheKey);
                        }
                    })
                    .map(success -> success ? 1 : 0);
            });
    }

    /**
     * 获取预热进度
     *
     * @return 预热进度信息
     */
    public Mono<Map<String, Object>> getPreloadProgress() {
        Map<String, Object> progress = new HashMap<>();
        progress.put("progress", preloadProgress.get());
        progress.put("completed", preloadCompleted);
        progress.put("inProgress", preloadInProgress);

        return Mono.just(progress);
    }

    /**
     * 手动触发缓存预热
     *
     * @return 预热结果
     */
    public Mono<Map<String, Object>> manualPreload() {
        LoggingUtil.info(logger, "手动触发缓存预热");

        return executePreload()
            .map(count -> {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("preloadCount", count);
                result.put("message", "缓存预热完成");
                return result;
            })
            .onErrorResume(error -> {
                LoggingUtil.error(logger, "手动缓存预热失败", error);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "缓存预热失败: " + error.getMessage());
                return Mono.just(result);
            });
    }

    /**
     * 清除所有预热缓存
     *
     * @return 清除结果
     */
    public Mono<Map<String, Object>> clearPreloadCache() {
        LoggingUtil.info(logger, "清除所有预热缓存");

        return Flux.just(
                CacheConstants.SYSTEM_CONFIG_KEY_PREFIX + "*",
                CacheConstants.USER_PERMISSION_KEY_PREFIX + "*",
                CacheConstants.USER_INFO_KEY_PREFIX + "*",
                CacheConstants.SYSTEM_STATUS_KEY_PREFIX + "*",
                CacheConstants.STATISTICS_KEY_PREFIX + "*"
            )
            .flatMap(pattern -> cacheService.deleteByPattern(pattern))
            .reduce(Long::sum)
            .map(count -> {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("deletedCount", count);
                result.put("message", "预热缓存清除完成");
                return result;
            })
            .onErrorResume(error -> {
                LoggingUtil.error(logger, "清除预热缓存失败", error);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "清除预热缓存失败: " + error.getMessage());
                return Mono.just(result);
            });
    }
}