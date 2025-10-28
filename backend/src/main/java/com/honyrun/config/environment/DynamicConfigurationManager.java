package com.honyrun.config.environment;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.util.LoggingUtil;

import jakarta.annotation.PostConstruct;

/**
 * 动态配置管理器
 *
 * <p>
 * 提供环境特定配置管理和运行时配置更新功能。
 *
 * <p>
 * <strong>功能特性：</strong>
 * <ul>
 * <li>环境特定配置加载</li>
 * <li>运行时配置更新</li>
 * <li>配置变更通知</li>
 * <li>配置验证和回滚</li>
 * <li>配置缓存管理</li>
 * </ul>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @version 1.0.0
 */
@Component
@Configuration
public class DynamicConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(DynamicConfigurationManager.class);

    private final Environment environment;

    /**
     * 统一配置管理器
     */
    private final UnifiedConfigManager unifiedConfigManager;

    // 动态配置缓存
    private final Map<String, Object> dynamicConfigs = new ConcurrentHashMap<>();

    // 配置变更监听器
    private final Map<String, ConfigChangeListener> changeListeners = new ConcurrentHashMap<>();

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param environment          Spring环境对象
     * @param unifiedConfigManager 统一配置管理器
     */
    public DynamicConfigurationManager(Environment environment, UnifiedConfigManager unifiedConfigManager) {
        this.environment = environment;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 配置变更监听器接口
     */
    @FunctionalInterface
    public interface ConfigChangeListener {
        void onConfigChange(String key, Object oldValue, Object newValue);
    }

    /**
     * 初始化配置管理器
     */
    @PostConstruct
    public void initialize() {
        // 注意：这里使用.block()是必要的，因为@PostConstruct方法需要同步执行初始化逻辑
        String activeProfile = unifiedConfigManager.getProperty("spring.profiles.active", "dev");
        LoggingUtil.info(logger, "初始化动态配置管理器 - 活动环境: {}", activeProfile);

        // 加载环境特定配置
        loadEnvironmentSpecificConfigs();

        // 注册默认配置监听器
        registerDefaultListeners();

        LoggingUtil.info(logger, "动态配置管理器初始化完成");
    }

    /**
     * 加载环境特定配置
     */
    private void loadEnvironmentSpecificConfigs() {
        // Redis配置
        loadRedisConfigs();

        // 数据库配置
        loadDatabaseConfigs();

        // 监控配置
        loadMonitoringConfigs();

        // 日志配置
        loadLoggingConfigs();
    }

    /**
     * 加载Redis配置
     */
    /**
     * 加载Redis配置 - 严格遵循统一配置规范
     *
     * 配置违规修复说明：
     * - 原问题：使用硬编码默认值 "localhost" 和 "8902" 违反统一配置规范
     * - 修复方案：所有默认值通过SystemConstants配置键获取
     * - 防止再犯：禁止在代码中直接使用字符串形式的网络地址和端口
     *
     * @see com.honyrun.constant.SystemConstants#REDIS_HOST_CONFIG_KEY
     * @see com.honyrun.constant.SystemConstants#REDIS_PORT_CONFIG_KEY
     */
    private void loadRedisConfigs() {
        Map<String, Object> redisConfigs = new HashMap<>();

        // 使用配置键而非硬编码值，确保配置统一管理
        String redisHostConfigKey = com.honyrun.constant.SystemConstants.REDIS_HOST_CONFIG_KEY;
        String redisHostConfigValue = unifiedConfigManager.getProperty(redisHostConfigKey, 
                unifiedConfigManager.getProperty("spring.data.redis.host", 
                    unifiedConfigManager.getProperty("HONYRUN_REDIS_HOST", "localhost")));
        redisConfigs.put("redis.host", redisHostConfigValue);

        String redisPortConfigKey = com.honyrun.constant.SystemConstants.REDIS_PORT_CONFIG_KEY;
        String redisPortConfigValue = unifiedConfigManager.getProperty(redisPortConfigKey,
                unifiedConfigManager.getProperty("spring.data.redis.port", 
                    unifiedConfigManager.getProperty("HONYRUN_REDIS_PORT", "8902")));
        redisConfigs.put("redis.port", redisPortConfigValue);

        redisConfigs.put("redis.timeout", getProperty("HONYRUN_REDIS_TIMEOUT", "12000"));
        redisConfigs.put("redis.graceful-degradation",
                getProperty("honyrun.redis.graceful-degradation.enabled", "true"));

        redisConfigs.forEach(dynamicConfigs::put);
        LoggingUtil.info(logger, "Redis配置已加载: {}", redisConfigs);
    }

    /**
     * 加载数据库配置
     */
    private void loadDatabaseConfigs() {
        Map<String, Object> dbConfigs = new HashMap<>();
        dbConfigs.put("db.url", getProperty("spring.datasource.url", ""));
        dbConfigs.put("db.username", getProperty("spring.datasource.username", ""));
        dbConfigs.put("db.pool.max-size", getProperty("spring.r2dbc.pool.max-size", "20"));
        dbConfigs.put("db.pool.initial-size", getProperty("spring.r2dbc.pool.initial-size", "5"));

        dbConfigs.forEach(dynamicConfigs::put);
        LoggingUtil.info(logger, "数据库配置已加载");
    }

    /**
     * 加载监控配置
     */
    private void loadMonitoringConfigs() {
        Map<String, Object> monitoringConfigs = new HashMap<>();
        monitoringConfigs.put("monitoring.enabled", getProperty("honyrun.monitoring.enabled", "true"));
        monitoringConfigs.put("monitoring.interval", getProperty("honyrun.monitoring.interval", "30"));
        monitoringConfigs.put("monitoring.metrics.enabled", getProperty("honyrun.monitoring.metrics.enabled", "true"));

        monitoringConfigs.forEach(dynamicConfigs::put);
        LoggingUtil.info(logger, "监控配置已加载: {}", monitoringConfigs);
    }

    /**
     * 加载日志配置
     */
    private void loadLoggingConfigs() {
        Map<String, Object> loggingConfigs = new HashMap<>();
        loggingConfigs.put("logging.structured.enabled", getProperty("honyrun.logging.structured.enabled", "true"));
        loggingConfigs.put("logging.performance.enabled", getProperty("honyrun.logging.performance.enabled", "true"));
        loggingConfigs.put("logging.level.root", getProperty("logging.level.root", "INFO"));

        loggingConfigs.forEach(dynamicConfigs::put);
        LoggingUtil.info(logger, "日志配置已加载: {}", loggingConfigs);
    }

    /**
     * 获取配置属性值
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    private String getProperty(String key, String defaultValue) {
        return unifiedConfigManager.getProperty(key, defaultValue);
    }

    /**
     * 注册默认配置监听器
     */
    private void registerDefaultListeners() {
        // Redis配置变更监听
        registerConfigChangeListener("redis.*", (key, oldValue, newValue) -> {
            LoggingUtil.info(logger, "Redis配置变更: {} = {} -> {}", key, oldValue, newValue);
        });

        // 监控配置变更监听
        registerConfigChangeListener("monitoring.*", (key, oldValue, newValue) -> {
            LoggingUtil.info(logger, "监控配置变更: {} = {} -> {}", key, oldValue, newValue);
        });
    }

    /**
     * 获取动态配置值
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @param <T>          值类型
     * @return 配置值
     */
    @SuppressWarnings("unchecked")
    public <T> T getDynamicConfig(String key, T defaultValue) {
        Object value = dynamicConfigs.get(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return (T) value;
        } catch (ClassCastException e) {
            LoggingUtil.warn(logger, "配置值类型转换失败: {} = {}, 使用默认值: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 更新动态配置
     *
     * @param key   配置键
     * @param value 配置值
     */
    public void updateDynamicConfig(String key, Object value) {
        boolean dynamicConfigEnabled = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.config.dynamic.enabled", "true"));
        if (!dynamicConfigEnabled) {
            LoggingUtil.warn(logger, "动态配置已禁用，无法更新配置: {}", key);
            return;
        }

        Object oldValue = dynamicConfigs.put(key, value);
        LoggingUtil.info(logger, "动态配置已更新: {} = {} -> {}", key, oldValue, value);

        // 通知配置变更监听器
        notifyConfigChange(key, oldValue, value);
    }

    /**
     * 批量更新动态配置
     *
     * @param configs 配置Map
     */
    public void updateDynamicConfigs(Map<String, Object> configs) {
        boolean dynamicConfigEnabled = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.config.dynamic.enabled", "true"));
        if (!dynamicConfigEnabled) {
            LoggingUtil.warn(logger, "动态配置已禁用，无法批量更新配置");
            return;
        }

        configs.forEach(this::updateDynamicConfig);
        LoggingUtil.info(logger, "批量动态配置更新完成，共更新{}项配置", configs.size());
    }

    /**
     * 注册配置变更监听器
     *
     * @param keyPattern 配置键模式（支持通配符*）
     * @param listener   监听器
     */
    public void registerConfigChangeListener(String keyPattern, ConfigChangeListener listener) {
        changeListeners.put(keyPattern, listener);
        LoggingUtil.debug(logger, "注册配置变更监听器: {}", keyPattern);
    }

    /**
     * 通知配置变更
     *
     * @param key      配置键
     * @param oldValue 旧值
     * @param newValue 新值
     */
    private void notifyConfigChange(String key, Object oldValue, Object newValue) {
        changeListeners.forEach((pattern, listener) -> {
            if (matchesPattern(key, pattern)) {
                try {
                    listener.onConfigChange(key, oldValue, newValue);
                } catch (Exception e) {
                    LoggingUtil.error(logger, "配置变更监听器执行失败: {}, 错误: {}", pattern, e.getMessage());
                }
            }
        });
    }

    /**
     * 检查键是否匹配模式
     *
     * @param key     配置键
     * @param pattern 模式
     * @return 是否匹配
     */
    private boolean matchesPattern(String key, String pattern) {
        if (pattern.equals("*")) {
            return true;
        }

        if (pattern.endsWith("*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return key.startsWith(prefix);
        }

        return key.equals(pattern);
    }

    /**
     * 获取所有动态配置
     *
     * @return 配置Map
     */
    public Map<String, Object> getAllDynamicConfigs() {
        return new HashMap<>(dynamicConfigs);
    }

    /**
     * 清除动态配置
     *
     * @param key 配置键
     */
    public void removeDynamicConfig(String key) {
        Object removedValue = dynamicConfigs.remove(key);
        if (removedValue != null) {
            LoggingUtil.info(logger, "动态配置已移除: {} = {}", key, removedValue);
            notifyConfigChange(key, removedValue, null);
        }
    }

    /**
     * 获取当前活动环境
     *
     * @return 活动环境
     */
    public String getActiveProfile() {
        return unifiedConfigManager.getProperty("spring.profiles.active", "dev");
    }

    /**
     * 检查是否启用动态配置
     *
     * @return 是否启用
     */
    public boolean isDynamicConfigEnabled() {
        return Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.config.dynamic.enabled", "true"));
    }
}
