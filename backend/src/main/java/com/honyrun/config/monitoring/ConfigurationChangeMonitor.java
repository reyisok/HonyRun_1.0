package com.honyrun.config.monitoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.honyrun.util.LoggingUtil;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;

/**
 * 配置变更监控服务
 * 
 * 监控配置文件的变更情况，提供配置变更检测、通知和健康检查功能
 * 支持实时监控配置文件变化、配置一致性检查和变更历史记录
 * 
 * 【监控功能】：
 * - 配置文件变更检测
 * - 配置一致性监控
 * - 变更历史记录
 * - 配置健康状态检查
 * - 变更通知机制
 * 
 * 【监控范围】：
 * - application.properties
 * - application-dev.properties
 * - application-prod.properties
 * - application-test.properties
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-27 12:26:43
 * @modified 2025-10-27 12:26:43
 * @version 1.0.0 - 初始版本，实现配置变更监控功能
 */
@Component
public class ConfigurationChangeMonitor implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationChangeMonitor.class);
    
    private final Environment environment;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;
    
    // 监控的配置文件
    private static final String[] CONFIG_FILES = {
        "src/main/resources/application.properties",
        "src/main/resources/application-dev.properties", 
        "src/main/resources/application-prod.properties",
        "src/test/resources/application-test.properties"
    };
    
    // 配置文件哈希值缓存
    private final Map<String, String> configFileHashes = new ConcurrentHashMap<>();
    
    // 监控指标
    private final Counter configChangeCounter;
    private final Counter configCheckCounter;
    private final AtomicLong lastCheckTime = new AtomicLong(0);
    private final AtomicLong totalChanges = new AtomicLong(0);
    
    // 监控状态
    private volatile boolean monitoringActive = true;
    private volatile LocalDateTime lastChangeDetected = null;
    private volatile String lastChangedFile = null;

    /**
     * 构造函数注入依赖
     *
     * @param environment Spring环境对象
     * @param eventPublisher 事件发布器
     * @param meterRegistry 指标注册器
     */
    public ConfigurationChangeMonitor(Environment environment, 
                                    ApplicationEventPublisher eventPublisher,
                                    MeterRegistry meterRegistry) {
        this.environment = environment;
        this.eventPublisher = eventPublisher;
        this.meterRegistry = meterRegistry;
        
        // 初始化监控指标
        this.configChangeCounter = Counter.builder("config.change.total")
                .description("配置文件变更总次数")
                .register(meterRegistry);
                
        this.configCheckCounter = Counter.builder("config.check.total")
                .description("配置检查总次数")
                .register(meterRegistry);
    }

    /**
     * 初始化组件后的回调方法，用于注册需要 this 引用的指标
     */
    @PostConstruct
    private void initializeGaugeMetrics() {
        // 注册监控状态指标
        Gauge.builder("config.monitor.active", this, monitor -> monitor.monitoringActive ? 1 : 0)
                .description("配置监控是否激活 (1=激活, 0=未激活)")
                .register(meterRegistry);
                
        Gauge.builder("config.changes.total", this, monitor -> monitor.totalChanges.get())
                .description("配置变更总数")
                .register(meterRegistry);
    }

    /**
     * 应用启动完成后初始化配置文件哈希值
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeConfigurationHashes() {
        LoggingUtil.info(logger, "=== 初始化配置文件监控 ===");
        
        try {
            for (String configFile : CONFIG_FILES) {
                String hash = calculateFileHash(configFile);
                if (hash != null) {
                    configFileHashes.put(configFile, hash);
                    LoggingUtil.debug(logger, "初始化配置文件哈希: {} -> {}", configFile, hash.substring(0, 8));
                }
            }
            
            LoggingUtil.info(logger, "配置文件监控初始化完成，监控 {} 个配置文件", configFileHashes.size());
            
        } catch (Exception e) {
            LoggingUtil.error(logger, "配置文件监控初始化失败", e);
            monitoringActive = false;
        }
    }

    /**
     * 定期检查配置文件变更
     * 每30秒执行一次检查
     */
    @Scheduled(fixedRate = 30000)
    public void checkConfigurationChanges() {
        if (!monitoringActive) {
            return;
        }
        
        LoggingUtil.debug(logger, "开始检查配置文件变更...");
        
        try {
            configCheckCounter.increment();
            lastCheckTime.set(System.currentTimeMillis());
            
            boolean hasChanges = false;
            
            for (String configFile : CONFIG_FILES) {
                String currentHash = calculateFileHash(configFile);
                String previousHash = configFileHashes.get(configFile);
                
                if (currentHash != null && !currentHash.equals(previousHash)) {
                    // 检测到配置文件变更
                    handleConfigurationChange(configFile, previousHash, currentHash);
                    configFileHashes.put(configFile, currentHash);
                    hasChanges = true;
                }
            }
            
            if (!hasChanges) {
                LoggingUtil.debug(logger, "配置文件检查完成，未发现变更");
            }
            
        } catch (Exception e) {
            LoggingUtil.error(logger, "配置文件变更检查失败", e);
        }
    }

    /**
     * 处理配置文件变更
     *
     * @param configFile 变更的配置文件
     * @param previousHash 之前的哈希值
     * @param currentHash 当前的哈希值
     */
    private void handleConfigurationChange(String configFile, String previousHash, String currentHash) {
        LoggingUtil.warn(logger, "检测到配置文件变更: {}", configFile);
        LoggingUtil.debug(logger, "哈希值变更: {} -> {}", 
                         previousHash != null ? previousHash.substring(0, 8) : "null",
                         currentHash.substring(0, 8));
        
        // 更新监控状态
        configChangeCounter.increment();
        totalChanges.incrementAndGet();
        lastChangeDetected = LocalDateTime.now();
        lastChangedFile = configFile;
        
        // 发布配置变更事件
        ConfigurationChangeEvent event = new ConfigurationChangeEvent(
            configFile, previousHash, currentHash, LocalDateTime.now());
        eventPublisher.publishEvent(event);
        
        // 记录变更详情
        logConfigurationChangeDetails(configFile);
    }

    /**
     * 记录配置变更详情
     *
     * @param configFile 变更的配置文件
     */
    private void logConfigurationChangeDetails(String configFile) {
        try {
            Path filePath = Paths.get(configFile);
            if (Files.exists(filePath)) {
                long fileSize = Files.size(filePath);
                LocalDateTime lastModified = LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(filePath).toInstant(),
                    java.time.ZoneId.systemDefault());
                
                LoggingUtil.info(logger, "配置文件变更详情 - 文件: {}, 大小: {} bytes, 修改时间: {}", 
                               configFile, fileSize, lastModified);
            }
        } catch (IOException e) {
            LoggingUtil.warn(logger, "无法获取配置文件详情: {}", configFile, e);
        }
    }

    /**
     * 计算文件哈希值
     *
     * @param filePath 文件路径
     * @return 文件的SHA-256哈希值，如果文件不存在或计算失败则返回null
     */
    private String calculateFileHash(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                LoggingUtil.debug(logger, "配置文件不存在: {}", filePath);
                return null;
            }
            
            byte[] fileContent = Files.readAllBytes(path);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileContent);
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (IOException | NoSuchAlgorithmException e) {
            LoggingUtil.error(logger, "计算文件哈希失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 健康检查实现
     */
    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        
        try {
            if (!monitoringActive) {
                return builder.down()
                        .withDetail("status", "监控未激活")
                        .withDetail("reason", "配置监控初始化失败")
                        .build();
            }
            
            Map<String, Object> details = new HashMap<>();
            details.put("monitoringActive", monitoringActive);
            details.put("monitoredFiles", configFileHashes.size());
            details.put("totalChanges", totalChanges.get());
            details.put("lastCheckTime", lastCheckTime.get() > 0 ? 
                       java.time.Instant.ofEpochMilli(lastCheckTime.get()) : "未检查");
            
            if (lastChangeDetected != null) {
                details.put("lastChangeDetected", lastChangeDetected);
                details.put("lastChangedFile", lastChangedFile);
            }
            
            // 检查配置文件状态
            boolean allFilesAccessible = true;
            for (String configFile : CONFIG_FILES) {
                if (!Files.exists(Paths.get(configFile))) {
                    allFilesAccessible = false;
                    details.put("missingFile", configFile);
                    break;
                }
            }
            
            if (allFilesAccessible) {
                return builder.up()
                        .withDetails(details)
                        .build();
            } else {
                return builder.down()
                        .withDetail("status", "配置文件缺失")
                        .withDetails(details)
                        .build();
            }
            
        } catch (Exception e) {
            return builder.down()
                    .withDetail("status", "健康检查失败")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    /**
     * 获取配置变更统计信息
     *
     * @return 配置变更统计信息
     */
    public Map<String, Object> getChangeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalChanges", totalChanges.get());
        stats.put("monitoredFiles", configFileHashes.size());
        stats.put("lastChangeDetected", lastChangeDetected);
        stats.put("lastChangedFile", lastChangedFile);
        stats.put("monitoringActive", monitoringActive);
        return stats;
    }

    /**
     * 配置变更事件类
     */
    public static class ConfigurationChangeEvent {
        private final String configFile;
        private final String previousHash;
        private final String currentHash;
        private final LocalDateTime changeTime;

        public ConfigurationChangeEvent(String configFile, String previousHash, 
                                      String currentHash, LocalDateTime changeTime) {
            this.configFile = configFile;
            this.previousHash = previousHash;
            this.currentHash = currentHash;
            this.changeTime = changeTime;
        }

        public String getConfigFile() { return configFile; }
        public String getPreviousHash() { return previousHash; }
        public String getCurrentHash() { return currentHash; }
        public LocalDateTime getChangeTime() { return changeTime; }
    }
}