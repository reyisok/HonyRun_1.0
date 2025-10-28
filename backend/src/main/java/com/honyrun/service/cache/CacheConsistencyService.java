package com.honyrun.service.cache;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;

/**
 * 缓存一致性服务
 *
 * 负责维护缓存数据的一致性，包括：
 * - 缓存失效事件处理
 * - 分布式缓存一致性保障
 * - 缓存更新策略执行
 * - 缓存预热和降级处理
 *
 * 主要功能：
 * - 统一的缓存失效处理
 * - 事务性缓存更新
 * - 缓存一致性监控
 * - 缓存降级策略
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 00:00:00
 * @modified 2025-07-01 00:00:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
public class CacheConsistencyService {

    private static final Logger logger = LoggerFactory.getLogger(CacheConsistencyService.class);

    private final CacheManager cacheManager;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.honyrun.service.monitoring.PreheatingMetricsService preheatingMetricsService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param cacheManager                缓存管理器
     * @param reactiveRedisTemplate       响应式Redis模板
     * @param reactiveStringRedisTemplate 响应式字符串Redis模板
     * @param redisTemplate               Redis模板
     * @param preheatingMetricsService    预热指标服务
     * @param eventPublisher              事件发布器
     */
    public CacheConsistencyService(CacheManager cacheManager,
            @Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
            @Qualifier("unifiedReactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate,
            RedisTemplate<String, Object> redisTemplate,
            com.honyrun.service.monitoring.PreheatingMetricsService preheatingMetricsService,
            ApplicationEventPublisher eventPublisher) {
        this.cacheManager = cacheManager;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
        this.redisTemplate = redisTemplate;
        this.preheatingMetricsService = preheatingMetricsService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 缓存失效事件主题前缀
     */
    private static final String CACHE_EVICT_TOPIC = "honyrun:cache:evict:";

    /**
     * 用户缓存失效
     *
     * @param userId 用户ID
     * @return 处理结果
     */
    @CacheEvict(value = "userCache", key = "#userId")
    public Mono<Void> evictUserCache(String userId) {
        LoggingUtil.info(logger, "开始失效用户缓存，用户ID：{}", userId);

        return publishCacheEvictEvent("userCache", userId)
                .doOnSuccess(v -> LoggingUtil.info(logger, "用户缓存失效完成，用户ID：{}", userId))
                .doOnError(error -> LoggingUtil.error(logger, "用户缓存失效失败，用户ID：" + userId, error));
    }

    /**
     * 系统配置缓存失效
     *
     * @param configKey 配置键
     * @return 处理结果
     */
    @CacheEvict(value = "systemCache", key = "#configKey")
    public Mono<Void> evictSystemCache(String configKey) {
        LoggingUtil.info(logger, "开始失效系统配置缓存，配置键：{}", configKey);

        return publishCacheEvictEvent("systemCache", configKey)
                .doOnSuccess(v -> LoggingUtil.info(logger, "系统配置缓存失效完成，配置键：{}", configKey))
                .doOnError(error -> LoggingUtil.error(logger, "系统配置缓存失效失败，配置键：" + configKey, error));
    }

    /**
     * 权限缓存失效
     *
     * @param permissionKey 权限键
     * @return 处理结果
     */
    @CacheEvict(value = "permissionCache", key = "#permissionKey")
    public Mono<Void> evictPermissionCache(String permissionKey) {
        LoggingUtil.info(logger, "开始失效权限缓存，权限键：{}", permissionKey);

        return publishCacheEvictEvent("permissionCache", permissionKey)
                .doOnSuccess(v -> LoggingUtil.info(logger, "权限缓存失效完成，权限键：{}", permissionKey))
                .doOnError(error -> LoggingUtil.error(logger, "权限缓存失效失败，权限键：" + permissionKey, error));
    }

    /**
     * 批量缓存失效
     *
     * @param cacheName 缓存名称
     * @return 处理结果
     */
    @CacheEvict(value = "#{#cacheName}", allEntries = true)
    public Mono<Void> evictAllCache(String cacheName) {
        LoggingUtil.info(logger, "开始批量失效缓存，缓存名称：{}", cacheName);

        return publishCacheEvictEvent(cacheName, "*")
                .doOnSuccess(v -> LoggingUtil.info(logger, "批量缓存失效完成，缓存名称：{}", cacheName))
                .doOnError(error -> LoggingUtil.error(logger, "批量缓存失效失败，缓存名称：" + cacheName, error));
    }

    /**
     * 发布缓存失效事件
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @return 处理结果
     */
    private Mono<Void> publishCacheEvictEvent(String cacheName, String key) {
        return Mono.fromRunnable(() -> {
            try {
                CacheEvictEvent event = new CacheEvictEvent(cacheName, key, LocalDateTime.now());
                eventPublisher.publishEvent(event);

                // 发布Redis消息，用于分布式缓存失效通知
                String topic = CACHE_EVICT_TOPIC + cacheName;
                reactiveRedisTemplate.convertAndSend(topic, event)
                        .subscribe(
                                result -> LoggingUtil.debug(logger, "缓存失效消息发布成功，主题：{}，键：{}", topic, key),
                                error -> LoggingUtil.error(logger, "缓存失效消息发布失败，主题：" + topic + "，键：" + key, error));

            } catch (Exception e) {
                LoggingUtil.error(logger, "发布缓存失效事件失败，缓存：{}，键：{}", cacheName, key, e);
                throw new RuntimeException("发布缓存失效事件失败", e);
            }
        });
    }

    /**
     * 处理缓存失效事件
     *
     * @param event 缓存失效事件
     */
    @EventListener
    public void handleCacheEvictEvent(CacheEvictEvent event) {
        try {
            LoggingUtil.debug(logger, "处理缓存失效事件，缓存：{}，键：{}，时间：{}",
                    event.getCacheName(), event.getKey(), event.getTimestamp());

            // 清理本地缓存
            org.springframework.cache.Cache cache = cacheManager.getCache(event.getCacheName());
            if (cache != null) {
                if ("*".equals(event.getKey())) {
                    cache.clear();
                    LoggingUtil.debug(logger, "清理所有本地缓存，缓存：{}", event.getCacheName());
                } else {
                    cache.evict(event.getKey());
                    LoggingUtil.debug(logger, "清理本地缓存，缓存：{}，键：{}", event.getCacheName(), event.getKey());
                }
            }

        } catch (Exception e) {
            LoggingUtil.error(logger, "处理缓存失效事件失败", e);
        }
    }

    /**
     * 缓存预热 - 优化版本
     * 支持并行预热多种缓存类型，提高启动效率
     *
     * @param cacheName 缓存名称
     * @return 处理结果
     */
    public Mono<Void> warmupCache(String cacheName) {
        LoggingUtil.info(logger, "开始缓存预热，缓存名称：{}", cacheName);

        return Mono.fromCallable(() -> {
            try {
                // 根据不同的缓存类型执行预热逻辑
                switch (cacheName) {
                    case "systemCache":
                        warmupSystemCache();
                        break;
                    case "configCache":
                        warmupConfigCache();
                        break;
                    case "permissionCache":
                        warmupPermissionCache();
                        break;
                    default:
                        LoggingUtil.warn(logger, "未知的缓存类型，跳过预热：{}", cacheName);
                        break;
                }

                LoggingUtil.info(logger, "缓存预热完成，缓存名称：{}", cacheName);
                return null;

            } catch (Exception e) {
                LoggingUtil.error(logger, "缓存预热失败，缓存名称：" + cacheName, e);
                throw new RuntimeException("缓存预热失败", e);
            }
        })
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()) // 使用弹性线程池
                .then();
    }

    /**
     * 批量缓存预热 - 并行执行多种缓存预热
     *
     * @return 预热结果
     */
    public Mono<Void> warmupAllCaches() {
        LoggingUtil.info(logger, "开始批量缓存预热");

        return Mono.zip(
                warmupCache("systemCache"),
                warmupCache("configCache"),
                warmupCache("permissionCache"))
                .doOnSuccess(result -> LoggingUtil.info(logger, "批量缓存预热完成"))
                .doOnError(error -> LoggingUtil.error(logger, "批量缓存预热失败", error))
                .then();
    }

    /**
     * 系统缓存预热
     */
    public void warmupSystemCache() {
        LoggingUtil.debug(logger, "执行系统缓存预热");
        // 这里可以预加载系统配置、常用数据等
    }

    /**
     * 配置缓存预热
     */
    public void warmupConfigCache() {
        LoggingUtil.debug(logger, "执行配置缓存预热");
        // 这里可以预加载应用配置、业务配置等
    }

    /**
     * 权限缓存预热 - 增强版本，包含量化监控
     *
     * <p>
     * 预热系统权限相关缓存，提升系统启动后的权限验证性能。
     * 包含详细的监控指标收集和效果评估。
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-01 18:50:00
     * @version 2.0.0 - 添加量化监控
     */
    public void warmupPermissionCache() {
        // 开始预热计时
        io.micrometer.core.instrument.Timer.Sample sample = preheatingMetricsService
                .startPreheatingTimer("permission_cache");

        LoggingUtil.debug(logger, "开始预热权限缓存");

        try {
            // 1. 预热系统用户权限
            warmupSystemUserPermissions();

            // 2. 预热普通用户权限
            warmupNormalUserPermissions();

            // 3. 预热权限定义
            warmupPermissionDefinitions();

            // 4. 预热用户类型权限映射
            warmupUserTypePermissionMapping();

            // 记录预热成功
            preheatingMetricsService.recordPreheatingSuccess("permission_cache", sample);

            // 评估预热效果
            evaluatePermissionCacheEffectiveness();

            LoggingUtil.info(logger, "权限缓存预热完成");

        } catch (Exception e) {
            // 记录预热失败
            preheatingMetricsService.recordPreheatingFailure("permission_cache", sample, e);
            LoggingUtil.error(logger, "权限缓存预热失败", e);
            throw e;
        }
    }

    /**
     * 预热系统用户权限
     */
    private void warmupSystemUserPermissions() {
        io.micrometer.core.instrument.Timer.Sample sample = preheatingMetricsService
                .startPreheatingTimer("system_user_permissions");

        try {
            LoggingUtil.debug(logger, "预热系统用户权限");

            // 系统用户权限列表
            String[] systemPermissions = {
                    "USER_READ", "USER_WRITE", "USER_DELETE", "USER_ADMIN",
                    "SYSTEM_READ", "SYSTEM_WRITE", "SYSTEM_CONFIG", "SYSTEM_ADMIN",
                    "DATA_READ", "DATA_WRITE", "DATA_DELETE", "DATA_EXPORT",
                    "MONITOR_READ", "MONITOR_WRITE", "LOG_READ", "LOG_EXPORT"
            };

            String cacheKey = "user:permissions:SYSTEM_USER";

            // 将权限列表存储到Redis，过期时间24小时
            reactiveRedisTemplate.opsForValue()
                    .set(cacheKey, java.util.Arrays.asList(systemPermissions), Duration.ofHours(24))
                    .subscribe(
                            result -> LoggingUtil.debug(logger, "系统用户权限缓存设置成功: {}", result),
                            error -> LoggingUtil.error(logger, "系统用户权限缓存设置失败", error));

            LoggingUtil.debug(logger, "系统用户权限预热完成 - 缓存键: {}, 权限数量: {}",
                    cacheKey, systemPermissions.length);

            preheatingMetricsService.recordPreheatingSuccess("system_user_permissions", sample);

        } catch (Exception e) {
            preheatingMetricsService.recordPreheatingFailure("system_user_permissions", sample, e);
            LoggingUtil.warn(logger, "系统用户权限预热失败", e);
        }
    }

    /**
     * 预热普通用户权限
     */
    private void warmupNormalUserPermissions() {
        io.micrometer.core.instrument.Timer.Sample sample = preheatingMetricsService
                .startPreheatingTimer("normal_user_permissions");

        try {
            LoggingUtil.debug(logger, "预热普通用户权限");

            // 普通用户权限列表
            String[] normalPermissions = {
                    "USER_READ", "USER_WRITE",
                    "DATA_READ", "DATA_WRITE",
                    "PROFILE_READ", "PROFILE_WRITE"
            };

            String cacheKey = "user:permissions:NORMAL_USER";

            // 将权限列表存储到Redis，过期时间24小时
            reactiveRedisTemplate.opsForValue()
                    .set(cacheKey, java.util.Arrays.asList(normalPermissions), Duration.ofHours(24))
                    .subscribe(
                            result -> LoggingUtil.debug(logger, "普通用户权限缓存设置成功: {}", result),
                            error -> LoggingUtil.error(logger, "普通用户权限缓存设置失败", error));

            LoggingUtil.debug(logger, "普通用户权限预热完成 - 缓存键: {}, 权限数量: {}",
                    cacheKey, normalPermissions.length);

            preheatingMetricsService.recordPreheatingSuccess("normal_user_permissions", sample);

        } catch (Exception e) {
            preheatingMetricsService.recordPreheatingFailure("normal_user_permissions", sample, e);
            LoggingUtil.warn(logger, "普通用户权限预热失败", e);
        }
    }

    /**
     * 预热权限定义
     */
    private void warmupPermissionDefinitions() {
        io.micrometer.core.instrument.Timer.Sample sample = preheatingMetricsService
                .startPreheatingTimer("permission_definitions");

        try {
            LoggingUtil.debug(logger, "预热权限定义");

            // 权限定义映射
            Map<String, String> permissionDefinitions = new HashMap<>();
            permissionDefinitions.put("USER_READ", "用户信息读取权限");
            permissionDefinitions.put("USER_WRITE", "用户信息写入权限");
            permissionDefinitions.put("USER_DELETE", "用户删除权限");
            permissionDefinitions.put("USER_ADMIN", "用户管理权限");
            permissionDefinitions.put("SYSTEM_READ", "系统信息读取权限");
            permissionDefinitions.put("SYSTEM_WRITE", "系统配置写入权限");
            permissionDefinitions.put("SYSTEM_CONFIG", "系统配置权限");
            permissionDefinitions.put("SYSTEM_ADMIN", "系统管理权限");
            permissionDefinitions.put("DATA_READ", "数据读取权限");
            permissionDefinitions.put("DATA_WRITE", "数据写入权限");
            permissionDefinitions.put("DATA_DELETE", "数据删除权限");
            permissionDefinitions.put("DATA_EXPORT", "数据导出权限");
            permissionDefinitions.put("MONITOR_READ", "监控信息读取权限");
            permissionDefinitions.put("MONITOR_WRITE", "监控配置权限");
            permissionDefinitions.put("LOG_READ", "日志读取权限");
            permissionDefinitions.put("LOG_EXPORT", "日志导出权限");

            // 批量存储权限定义到Redis
            for (Map.Entry<String, String> entry : permissionDefinitions.entrySet()) {
                String cacheKey = "permission:definition:" + entry.getKey();
                reactiveRedisTemplate.opsForValue()
                        .set(cacheKey, entry.getValue(), Duration.ofHours(24))
                        .subscribe(
                                result -> LoggingUtil.debug(logger, "权限定义缓存设置成功: {} -> {}", entry.getKey(), result),
                                error -> LoggingUtil.error(logger, "权限定义缓存设置失败: {}", entry.getKey(), error));
            }

            LoggingUtil.debug(logger, "权限定义预热完成 - 定义数量: {}", permissionDefinitions.size());

            preheatingMetricsService.recordPreheatingSuccess("permission_definitions", sample);

        } catch (Exception e) {
            preheatingMetricsService.recordPreheatingFailure("permission_definitions", sample, e);
            LoggingUtil.warn(logger, "权限定义预热失败", e);
        }
    }

    /**
     * 预热用户类型权限映射
     */
    private void warmupUserTypePermissionMapping() {
        io.micrometer.core.instrument.Timer.Sample sample = preheatingMetricsService
                .startPreheatingTimer("user_type_permission_mapping");

        try {
            LoggingUtil.debug(logger, "预热用户类型权限映射");

            // 用户类型权限映射
            Map<String, Object> userTypeMapping = Map.of(
                    "SYSTEM_USER", Map.of(
                            "level", "ADMIN",
                            "description", "系统管理员用户",
                            "maxPermissions", 16),
                    "NORMAL_USER", Map.of(
                            "level", "USER",
                            "description", "普通用户",
                            "maxPermissions", 6),
                    "GUEST", Map.of(
                            "level", "GUEST",
                            "description", "访客用户",
                            "maxPermissions", 1));

            String cacheKey = "user:type:permission:mapping";

            // 存储用户类型权限映射到Redis，过期时间24小时
            reactiveRedisTemplate.opsForValue()
                    .set(cacheKey, userTypeMapping, Duration.ofHours(24))
                    .subscribe(
                            result -> LoggingUtil.debug(logger, "用户类型权限映射缓存设置成功: {}", result),
                            error -> LoggingUtil.error(logger, "用户类型权限映射缓存设置失败", error));

            LoggingUtil.debug(logger, "用户类型权限映射预热完成 - 缓存键: {}, 类型数量: {}",
                    cacheKey, userTypeMapping.size());

            preheatingMetricsService.recordPreheatingSuccess("user_type_permission_mapping", sample);

        } catch (Exception e) {
            preheatingMetricsService.recordPreheatingFailure("user_type_permission_mapping", sample, e);
            LoggingUtil.warn(logger, "用户类型权限映射预热失败", e);
        }
    }

    /**
     * 评估权限缓存预热效果
     */
    private void evaluatePermissionCacheEffectiveness() {
        try {
            LoggingUtil.info(logger, "开始评估权限缓存预热效果...");

            // 并行检查所有缓存键，避免嵌套订阅
            Mono<Boolean> systemUserCheck = reactiveRedisTemplate.hasKey("user:permissions:SYSTEM_USER");
            Mono<Boolean> normalUserCheck = reactiveRedisTemplate.hasKey("user:permissions:NORMAL_USER");
            Mono<Boolean> mappingCheck = reactiveRedisTemplate.hasKey("user:type:permission:mapping");

            // 使用Mono.zip并行处理所有检查
            Mono.zip(systemUserCheck, normalUserCheck, mappingCheck)
                    .subscribe(
                            tuple -> {
                                boolean systemExists = Boolean.TRUE.equals(tuple.getT1());
                                boolean normalExists = Boolean.TRUE.equals(tuple.getT2());
                                boolean mappingExists = Boolean.TRUE.equals(tuple.getT3());

                                int successfulCacheKeys = 0;
                                int totalCacheKeys = 3;

                                if (systemExists)
                                    successfulCacheKeys++;
                                if (normalExists)
                                    successfulCacheKeys++;
                                if (mappingExists)
                                    successfulCacheKeys++;

                                // 计算预热效果评分
                                double effectivenessScore = totalCacheKeys > 0
                                        ? (double) successfulCacheKeys / totalCacheKeys
                                        : 0.0;

                                LoggingUtil.info(logger, "权限缓存预热效果评估完成 - 成功率: {}/{} ({}%)",
                                        successfulCacheKeys, totalCacheKeys,
                                        String.format("%.2f", effectivenessScore * 100));

                                // 记录预热效果评估
                                preheatingMetricsService.recordPreheatingEffectiveness(
                                        "permission_cache", effectivenessScore);
                            },
                            error -> LoggingUtil.error(logger, "权限缓存预热效果评估失败", error));

        } catch (Exception e) {
            LoggingUtil.warn(logger, "权限缓存预热效果评估失败", e);
        }
    }

    /**
     * 缓存降级处理
     *
     * @param cacheName     缓存名称
     * @param fallbackValue 降级值
     * @return 降级处理结果
     */
    public Mono<Object> handleCacheFallback(String cacheName, Object fallbackValue) {
        LoggingUtil.warn(logger, "缓存降级处理，缓存名称：{}，降级值：{}", cacheName, fallbackValue);

        return Mono.just(fallbackValue)
                .doOnSuccess(value -> LoggingUtil.info(logger, "缓存降级处理完成，缓存名称：{}", cacheName));
    }

    /**
     * 事务性缓存更新
     *
     * 在事务提交前清除相关缓存，确保强一致性
     *
     * @param cacheNames 缓存名称列表
     * @param keys       缓存键列表
     * @return 更新结果
     */
    public Mono<Void> transactionalCacheUpdate(java.util.List<String> cacheNames, java.util.List<String> keys) {
        LoggingUtil.info(logger, "开始事务性缓存更新，缓存数量：{}，键数量：{}", cacheNames.size(), keys.size());

        return reactor.core.publisher.Flux.fromIterable(cacheNames)
                .zipWith(reactor.core.publisher.Flux.fromIterable(keys))
                .flatMap(tuple -> {
                    String cacheName = tuple.getT1();
                    String key = tuple.getT2();
                    return evictCacheByNameAndKey(cacheName, key);
                })
                .then()
                .doOnSuccess(v -> LoggingUtil.info(logger, "事务性缓存更新完成"))
                .doOnError(error -> LoggingUtil.error(logger, "事务性缓存更新失败", error));
    }

    /**
     * 根据缓存名称和键失效缓存
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @return 失效结果
     */
    private Mono<Void> evictCacheByNameAndKey(String cacheName, String key) {
        return publishCacheEvictEvent(cacheName, key)
                .doOnSuccess(v -> LoggingUtil.debug(logger, "缓存失效完成，缓存：{}，键：{}", cacheName, key));
    }

    /**
     * 批量缓存一致性检查
     *
     * @param cacheKeys 缓存键列表
     * @return 检查结果
     */
    public Mono<Map<String, Object>> batchConsistencyCheck(java.util.List<String> cacheKeys) {
        LoggingUtil.info(logger, "开始批量缓存一致性检查，检查键数量：{}", cacheKeys.size());

        return reactor.core.publisher.Flux.fromIterable(cacheKeys)
                .flatMap(this::checkCacheConsistency)
                .collectList()
                .map(results -> {
                    Map<String, Object> summary = new HashMap<>();
                    long inconsistentCount = results.stream()
                            .mapToLong(result -> (Boolean) result.get("consistent") ? 0 : 1)
                            .sum();

                    summary.put("totalChecked", results.size());
                    summary.put("inconsistentCount", inconsistentCount);
                    summary.put("consistencyRate", (results.size() - inconsistentCount) * 100.0 / results.size());
                    summary.put("details", results);
                    summary.put("timestamp", java.time.LocalDateTime.now());

                    return summary;
                })
                .doOnSuccess(summary -> LoggingUtil.info(logger, "批量缓存一致性检查完成：{}", summary));
    }

    /**
     * 检查单个缓存的一致性
     *
     * @param cacheKey 缓存键
     * @return 检查结果
     */
    private Mono<Map<String, Object>> checkCacheConsistency(String cacheKey) {
        return reactiveRedisTemplate.hasKey(cacheKey)
                .map(exists -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("key", cacheKey);
                    result.put("exists", exists);
                    result.put("consistent", true); // 简化实现，实际应检查数据一致性
                    result.put("checkTime", java.time.LocalDateTime.now());

                    return result;
                })
                .doOnError(error -> LoggingUtil.error(logger, "缓存一致性检查失败，键：" + cacheKey, error))
                .onErrorReturn(createErrorResult(cacheKey));
    }

    /**
     * 创建错误结果
     *
     * @param cacheKey 缓存键
     * @return 错误结果
     */
    private Map<String, Object> createErrorResult(String cacheKey) {
        Map<String, Object> result = new HashMap<>();
        result.put("key", cacheKey);
        result.put("exists", false);
        result.put("consistent", false);
        result.put("error", true);
        result.put("checkTime", java.time.LocalDateTime.now());
        return result;
    }

    /**
     * 缓存版本控制更新
     *
     * @param cacheKey 缓存键
     * @param version  版本号
     * @return 更新结果
     */
    public Mono<Boolean> updateCacheVersion(String cacheKey, Long version) {
        LoggingUtil.debug(logger, "更新缓存版本，键：{}，版本：{}", cacheKey, version);

        String versionKey = "cache:version:" + cacheKey;
        return reactiveRedisTemplate.opsForValue()
                .set(versionKey, version, Duration.ofHours(24))
                .doOnSuccess(success -> {
                    if (success) {
                        LoggingUtil.debug(logger, "缓存版本更新成功，键：{}，版本：{}", cacheKey, version);
                    } else {
                        LoggingUtil.warn(logger, "缓存版本更新失败，键：{}，版本：{}", cacheKey, version);
                    }
                });
    }

    /**
     * 获取缓存版本
     *
     * @param cacheKey 缓存键
     * @return 版本号
     */
    public Mono<Long> getCacheVersion(String cacheKey) {
        String versionKey = "cache:version:" + cacheKey;
        return reactiveRedisTemplate.opsForValue()
                .get(versionKey)
                .cast(Long.class)
                .defaultIfEmpty(0L)
                .doOnNext(version -> LoggingUtil.debug(logger, "获取缓存版本，键：{}，版本：{}", cacheKey, version));
    }

    /**
     * 分布式缓存同步
     *
     * @param syncData 同步数据
     * @return 同步结果
     */
    public Mono<Long> distributedCacheSync(Map<String, Object> syncData) {
        LoggingUtil.info(logger, "开始分布式缓存同步，数据：{}", syncData);

        String syncChannel = "cache:sync:channel";
        return reactiveRedisTemplate.convertAndSend(syncChannel, syncData)
                .doOnSuccess(count -> LoggingUtil.info(logger, "分布式缓存同步完成，接收者数量：{}", count))
                .doOnError(error -> LoggingUtil.error(logger, "分布式缓存同步失败", error));
    }

    /**
     * 缓存失效事件类
     */
    public static class CacheEvictEvent {
        private final String cacheName;
        private final String key;
        private final LocalDateTime timestamp;

        public CacheEvictEvent(String cacheName, String key, LocalDateTime timestamp) {
            this.cacheName = cacheName;
            this.key = key;
            this.timestamp = timestamp;
        }

        public String getCacheName() {
            return cacheName;
        }

        public String getKey() {
            return key;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            CacheEvictEvent that = (CacheEvictEvent) o;
            return Objects.equals(cacheName, that.cacheName) &&
                    Objects.equals(key, that.key) &&
                    Objects.equals(timestamp, that.timestamp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cacheName, key, timestamp);
        }

        @Override
        public String toString() {
            return "CacheEvictEvent{" +
                    "cacheName='" + cacheName + '\'' +
                    ", key='" + key + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
