package com.honyrun.config.system;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 配置动态加载和刷新机制类
 *
 * 提供配置的动态加载、实时刷新和变更通知功能
 * 支持定时刷新、手动刷新和事件驱动刷新
 *
 * 主要功能：
 * - 配置的定时自动刷新
 * - 配置变更事件监听和处理
 * - 配置刷新状态监控
 * - 配置变更通知机制
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 11:30:00
 * @modified 2025-07-01 11:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class DynamicConfigRefresher {

    private static final Logger logger = LoggerFactory.getLogger(DynamicConfigRefresher.class);

    private final SystemConfigManager systemConfigManager;
    private final EnvironmentConfigProcessor environmentConfigProcessor;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 构造函数注入依赖
     *
     * @param systemConfigManager 系统配置管理器
     * @param environmentConfigProcessor 环境配置处理器
     * @param eventPublisher 应用事件发布器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public DynamicConfigRefresher(SystemConfigManager systemConfigManager,
                                EnvironmentConfigProcessor environmentConfigProcessor,
                                ApplicationEventPublisher eventPublisher) {
        this.systemConfigManager = systemConfigManager;
        this.environmentConfigProcessor = environmentConfigProcessor;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 刷新状态标志
     */
    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

    /**
     * 刷新计数器
     */
    private final AtomicLong refreshCounter = new AtomicLong(0);

    /**
     * 最后刷新时间
     */
    private volatile LocalDateTime lastRefreshTime;

    /**
     * 刷新统计信息
     */
    private final Map<String, Object> refreshStats = new ConcurrentHashMap<>();

    /**
     * 配置变更监听器列表
     */
    private final Map<String, ConfigChangeListener> changeListeners = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        LoggingUtil.info(logger, "初始化动态配置刷新器");
        lastRefreshTime = LocalDateTime.now();
        initializeRefreshStats();
    }

    // ==================== 配置刷新方法 ====================

    /**
     * 手动刷新所有配置
     *
     * @return 刷新结果的Mono包装
     */
    public Mono<RefreshResult> refreshAllConfigs() {
        LoggingUtil.info(logger, "开始手动刷新所有配置");

        if (!isRefreshing.compareAndSet(false, true)) {
            LoggingUtil.warn(logger, "配置刷新正在进行中，跳过本次刷新");
            return Mono.just(RefreshResult.skipped("配置刷新正在进行中"));
        }

        return performRefresh("MANUAL")
                .doFinally(signal -> isRefreshing.set(false));
    }

    /**
     * 定时自动刷新配置
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void scheduledRefresh() {
        if (isRefreshing.get()) {
            LoggingUtil.debug(logger, "配置刷新正在进行中，跳过定时刷新");
            return;
        }

        LoggingUtil.debug(logger, "开始定时配置刷新");

        performRefresh("SCHEDULED")
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    result -> LoggingUtil.debug(logger, "定时配置刷新完成: {}", result.getMessage()),
                    error -> LoggingUtil.error(logger, "定时配置刷新失败", error)
                );
    }

    /**
     * 执行配置刷新
     *
     * @param refreshType 刷新类型
     * @return 刷新结果的Mono包装
     */
    private Mono<RefreshResult> performRefresh(String refreshType) {
        long startTime = System.currentTimeMillis();

        return Mono.fromCallable(() -> {
            isRefreshing.set(true);
            return refreshType;
        })
        .flatMap(type -> {
            // 刷新系统配置缓存
            return systemConfigManager.refreshConfigCache()
                    .then(environmentConfigProcessor.refreshEnvironmentCache())
                    .then(Mono.fromCallable(() -> {
                        long endTime = System.currentTimeMillis();
                        long duration = endTime - startTime;

                        // 更新统计信息
                        updateRefreshStats(type, duration, true);

                        // 发布配置刷新事件
                        publishConfigRefreshEvent(type, duration);

                        LoggingUtil.info(logger, "配置刷新完成，类型: {}，耗时: {}ms", type, duration);
                        return RefreshResult.success(type, duration);
                    }));
        })
        .onErrorResume(error -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // 更新失败统计
            updateRefreshStats(refreshType, duration, false);

            LoggingUtil.error(logger, "配置刷新失败，类型: " + refreshType, error);
            return Mono.just(RefreshResult.failed(refreshType, error.getMessage(), duration));
        })
        .doFinally(signal -> {
            isRefreshing.set(false);
            lastRefreshTime = LocalDateTime.now();
            refreshCounter.incrementAndGet();
        });
    }

    // ==================== 配置变更监听 ====================

    /**
     * 注册配置变更监听器
     *
     * @param configKey 配置键名
     * @param listener 监听器
     */
    public void registerConfigChangeListener(String configKey, ConfigChangeListener listener) {
        changeListeners.put(configKey, listener);
        LoggingUtil.debug(logger, "注册配置变更监听器: {}", configKey);
    }

    /**
     * 移除配置变更监听器
     *
     * @param configKey 配置键名
     */
    public void removeConfigChangeListener(String configKey) {
        changeListeners.remove(configKey);
        LoggingUtil.debug(logger, "移除配置变更监听器: {}", configKey);
    }

    /**
     * 监听配置变更事件
     *
     * @param event 配置变更事件
     */
    @EventListener
    @Async
    public void handleConfigChangeEvent(ConfigChangeEvent event) {
        LoggingUtil.info(logger, "处理配置变更事件: {}", event.getConfigKey());

        ConfigChangeListener listener = changeListeners.get(event.getConfigKey());
        if (listener != null) {
            try {
                listener.onConfigChanged(event.getConfigKey(), event.getOldValue(), event.getNewValue());
                LoggingUtil.debug(logger, "配置变更监听器执行成功: {}", event.getConfigKey());
            } catch (Exception e) {
                LoggingUtil.error(logger, "配置变更监听器执行失败: " + event.getConfigKey(), e);
            }
        }
    }

    // ==================== 事件发布方法 ====================

    /**
     * 发布配置刷新事件
     *
     * @param refreshType 刷新类型
     * @param duration 刷新耗时
     */
    private void publishConfigRefreshEvent(String refreshType, long duration) {
        ConfigRefreshEvent event = new ConfigRefreshEvent(this, refreshType, duration, LocalDateTime.now());
        eventPublisher.publishEvent(event);
        LoggingUtil.debug(logger, "发布配置刷新事件: {}", refreshType);
    }

    /**
     * 发布配置变更事件
     *
     * @param configKey 配置键名
     * @param oldValue 旧值
     * @param newValue 新值
     */
    public void publishConfigChangeEvent(String configKey, String oldValue, String newValue) {
        ConfigChangeEvent event = new ConfigChangeEvent(this, configKey, oldValue, newValue, LocalDateTime.now());
        eventPublisher.publishEvent(event);
        LoggingUtil.debug(logger, "发布配置变更事件: {}", configKey);
    }

    // ==================== 统计和监控方法 ====================

    /**
     * 更新刷新统计信息
     *
     * @param refreshType 刷新类型
     * @param duration 刷新耗时
     * @param success 是否成功
     */
    private void updateRefreshStats(String refreshType, long duration, boolean success) {
        refreshStats.put("lastRefreshType", refreshType);
        refreshStats.put("lastRefreshDuration", duration);
        refreshStats.put("lastRefreshSuccess", success);
        refreshStats.put("lastRefreshTime", LocalDateTime.now());
        refreshStats.put("totalRefreshCount", refreshCounter.get() + 1);

        // 更新成功/失败计数
        String successKey = refreshType + "_success_count";
        String failureKey = refreshType + "_failure_count";

        if (success) {
            refreshStats.merge(successKey, 1L, (old, val) -> (Long) old + (Long) val);
        } else {
            refreshStats.merge(failureKey, 1L, (old, val) -> (Long) old + (Long) val);
        }
    }

    /**
     * 初始化刷新统计信息
     */
    private void initializeRefreshStats() {
        refreshStats.put("totalRefreshCount", 0L);
        refreshStats.put("MANUAL_success_count", 0L);
        refreshStats.put("MANUAL_failure_count", 0L);
        refreshStats.put("SCHEDULED_success_count", 0L);
        refreshStats.put("SCHEDULED_failure_count", 0L);
        refreshStats.put("EVENT_success_count", 0L);
        refreshStats.put("EVENT_failure_count", 0L);
    }

    /**
     * 获取刷新状态信息
     *
     * @return 刷新状态信息
     */
    public Map<String, Object> getRefreshStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>(refreshStats);
        status.put("isRefreshing", isRefreshing.get());
        status.put("refreshCounter", refreshCounter.get());
        status.put("lastRefreshTime", lastRefreshTime);
        status.put("registeredListeners", changeListeners.keySet());
        return status;
    }

    /**
     * 获取刷新统计信息
     *
     * @return 刷新统计信息
     */
    public Map<String, Object> getRefreshStatistics() {
        return new ConcurrentHashMap<>(refreshStats);
    }

    // ==================== 内部类定义 ====================

    /**
     * 刷新结果类
     */
    public static class RefreshResult {
        private final boolean success;
        private final String type;
        private final String message;
        private final long duration;
        private final LocalDateTime timestamp;

        private RefreshResult(boolean success, String type, String message, long duration) {
            this.success = success;
            this.type = type;
            this.message = message;
            this.duration = duration;
            this.timestamp = LocalDateTime.now();
        }

        public static RefreshResult success(String type, long duration) {
            return new RefreshResult(true, type, "配置刷新成功", duration);
        }

        public static RefreshResult failed(String type, String message, long duration) {
            return new RefreshResult(false, type, "配置刷新失败: " + message, duration);
        }

        public static RefreshResult skipped(String message) {
            return new RefreshResult(false, "SKIPPED", message, 0);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getType() { return type; }
        public String getMessage() { return message; }
        public long getDuration() { return duration; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * 配置变更监听器接口
     */
    @FunctionalInterface
    public interface ConfigChangeListener {
        /**
         * 配置变更回调方法
         *
         * @param configKey 配置键名
         * @param oldValue 旧值
         * @param newValue 新值
         */
        void onConfigChanged(String configKey, String oldValue, String newValue);
    }

    /**
     * 配置刷新事件
     */
    public static class ConfigRefreshEvent {
        private final Object source;
        private final String refreshType;
        private final long duration;
        private final LocalDateTime timestamp;

        public ConfigRefreshEvent(Object source, String refreshType, long duration, LocalDateTime timestamp) {
            this.source = source;
            this.refreshType = refreshType;
            this.duration = duration;
            this.timestamp = timestamp;
        }

        // Getters
        public Object getSource() { return source; }
        public String getRefreshType() { return refreshType; }
        public long getDuration() { return duration; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * 配置变更事件
     */
    public static class ConfigChangeEvent {
        private final Object source;
        private final String configKey;
        private final String oldValue;
        private final String newValue;
        private final LocalDateTime timestamp;

        public ConfigChangeEvent(Object source, String configKey, String oldValue, String newValue, LocalDateTime timestamp) {
            this.source = source;
            this.configKey = configKey;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.timestamp = timestamp;
        }

        // Getters
        public Object getSource() { return source; }
        public String getConfigKey() { return configKey; }
        public String getOldValue() { return oldValue; }
        public String getNewValue() { return newValue; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
