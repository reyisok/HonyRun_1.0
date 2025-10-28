package com.honyrun.config.monitoring;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.honyrun.config.properties.JwtProperties;
import com.honyrun.util.LoggingUtil;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;

/**
 * JWT配置监控服务
 * 
 * 提供JWT配置的健康检查、指标监控和配置验证功能
 * 支持实时监控配置状态、性能指标和安全警告
 * 
 * 【监控功能】：
 * - 配置有效性检查
 * - 密钥强度监控
 * - 过期时间监控
 * - 配置变更检测
 * - 性能指标收集
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-26 01:36:12
 * @version 1.0.0 - 初始版本，提供JWT配置监控功能
 */
@Component
public class JwtConfigurationMonitor implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(JwtConfigurationMonitor.class);
    
    private final JwtProperties jwtProperties;
    private final MeterRegistry meterRegistry;
    
    // 监控指标
    private final Counter configValidationCounter;
    private final Counter configErrorCounter;
    private final Timer configValidationTimer;
    private final AtomicLong lastValidationTime = new AtomicLong(0);
    private final AtomicLong configChangeCount = new AtomicLong(0);
    
    // 配置状态
    private volatile boolean configurationValid = true;
    private volatile String lastValidationError = null;
    private volatile LocalDateTime lastConfigCheck = LocalDateTime.now();
    private volatile String previousConfigHash = null;

    /**
     * 构造函数注入依赖
     *
     * @param jwtProperties JWT配置属性
     * @param meterRegistry 指标注册器
     */
    public JwtConfigurationMonitor(JwtProperties jwtProperties, MeterRegistry meterRegistry) {
        this.jwtProperties = jwtProperties;
        this.meterRegistry = meterRegistry;
        
        // 初始化监控指标
        this.configValidationCounter = Counter.builder("jwt.config.validation.total")
                .description("JWT配置验证总次数")
                .register(meterRegistry);
                
        this.configErrorCounter = Counter.builder("jwt.config.error.total")
                .description("JWT配置错误总次数")
                .register(meterRegistry);
                
        this.configValidationTimer = Timer.builder("jwt.config.validation.duration")
                .description("JWT配置验证耗时")
                .register(meterRegistry);
    }

    /**
     * 初始化组件后的回调方法，用于注册需要 this 引用的指标
     * 使用 @PostConstruct 避免 this-escape 警告
     */
    @PostConstruct
    private void initializeGaugeMetrics() {
        // 注册配置状态指标
        Gauge.builder("jwt.config.valid", this, monitor -> monitor.configurationValid ? 1 : 0)
                .description("JWT配置是否有效 (1=有效, 0=无效)")
                .register(meterRegistry);
                
        Gauge.builder("jwt.config.expiration.hours", this, monitor -> 
                monitor.jwtProperties.getExpiration() / 1000.0 / 3600.0)
                .description("JWT访问令牌过期时间（小时）")
                .register(meterRegistry);
                    
        Gauge.builder("jwt.config.refresh.expiration.days", this, monitor -> 
                monitor.jwtProperties.getRefreshExpiration() / 1000.0 / 86400.0)
                .description("JWT刷新令牌过期时间（天）")
                .register(meterRegistry);
                    
        Gauge.builder("jwt.config.secret.length", this, monitor -> 
                monitor.jwtProperties.getSecret() != null ? 
                monitor.jwtProperties.getSecret().length() : 0)
                .description("JWT密钥长度")
                .register(meterRegistry);
                    
        Gauge.builder("jwt.config.change.count", this, monitor -> monitor.configChangeCount.get())
                .description("JWT配置变更次数")
                .register(meterRegistry);
    }

    /**
     * 应用启动后初始化监控
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        LoggingUtil.info(logger, "JWT配置监控服务启动，开始监控配置状态");
        validateConfiguration();
        updateConfigHash();
    }

    /**
     * 定期验证JWT配置
     * 每5分钟执行一次配置检查
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void scheduleConfigurationValidation() {
        validateConfiguration();
        checkConfigurationChanges();
    }

    /**
     * 验证JWT配置
     */
    public void validateConfiguration() {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            configValidationCounter.increment();
            lastConfigCheck = LocalDateTime.now();
            
            // 验证基本配置
            boolean isValid = validateBasicConfiguration();
            
            // 验证安全配置
            if (isValid) {
                isValid = validateSecurityConfiguration();
            }
            
            // 验证时间配置
            if (isValid) {
                isValid = validateTimeConfiguration();
            }
            
            // 验证密钥轮换配置
            if (isValid) {
                isValid = validateKeyRotationConfiguration();
            }
            
            configurationValid = isValid;
            lastValidationError = null;
            lastValidationTime.set(System.currentTimeMillis());
            
            if (isValid) {
                LoggingUtil.debug(logger, "JWT配置验证通过");
            }
            
        } catch (Exception e) {
            configurationValid = false;
            lastValidationError = e.getMessage();
            configErrorCounter.increment();
            LoggingUtil.error(logger, "JWT配置验证失败: " + e.getMessage(), e);
        } finally {
            sample.stop(configValidationTimer);
        }
    }

    /**
     * 验证基本配置
     */
    private boolean validateBasicConfiguration() {
        if (jwtProperties.getSecret() == null || jwtProperties.getSecret().trim().isEmpty()) {
            lastValidationError = "JWT密钥不能为空";
            return false;
        }
        
        if (jwtProperties.getIssuer() == null || jwtProperties.getIssuer().trim().isEmpty()) {
            lastValidationError = "JWT发行者不能为空";
            return false;
        }
        
        if (jwtProperties.getAudience() == null || jwtProperties.getAudience().trim().isEmpty()) {
            lastValidationError = "JWT受众不能为空";
            return false;
        }
        
        return true;
    }

    /**
     * 验证安全配置
     */
    private boolean validateSecurityConfiguration() {
        String secret = jwtProperties.getSecret();
        
        // 检查密钥长度
        if (secret.length() < 64) {
            lastValidationError = "JWT密钥长度不足，要求至少64个字符，当前: " + secret.length();
            LoggingUtil.warn(logger, "JWT密钥长度不足，存在安全风险");
            return false;
        }
        
        // 检查密钥复杂度（生产环境）
        String activeProfile = System.getProperty("spring.profiles.active", "dev");
        if ("prod".equals(activeProfile)) {
            if (!isSecureKey(secret)) {
                lastValidationError = "生产环境JWT密钥复杂度不足";
                LoggingUtil.warn(logger, "生产环境JWT密钥复杂度不足，存在安全风险");
                return false;
            }
        }
        
        return true;
    }

    /**
     * 验证时间配置
     */
    private boolean validateTimeConfiguration() {
        Long expiration = jwtProperties.getExpiration();
        Long refreshExpiration = jwtProperties.getRefreshExpiration();
        Long refreshThreshold = jwtProperties.getRefreshThreshold();
        
        if (expiration == null || expiration <= 0) {
            lastValidationError = "访问令牌过期时间必须大于0";
            return false;
        }
        
        if (refreshExpiration == null || refreshExpiration <= 0) {
            lastValidationError = "刷新令牌过期时间必须大于0";
            return false;
        }
        
        if (refreshThreshold == null || refreshThreshold <= 0) {
            lastValidationError = "令牌自动刷新阈值必须大于0";
            return false;
        }
        
        if (refreshExpiration <= expiration) {
            lastValidationError = "刷新令牌过期时间必须大于访问令牌过期时间";
            return false;
        }
        
        if (refreshThreshold >= expiration) {
            lastValidationError = "令牌自动刷新阈值必须小于访问令牌过期时间";
            return false;
        }
        
        // 检查过期时间是否过长（安全建议）
        if (expiration > 86400000L) { // 24小时
            LoggingUtil.warn(logger, "访问令牌过期时间过长（超过24小时），存在安全风险");
        }
        
        if (refreshExpiration > 2592000000L) { // 30天
            LoggingUtil.warn(logger, "刷新令牌过期时间过长（超过30天），存在安全风险");
        }
        
        return true;
    }

    /**
     * 验证密钥轮换配置
     */
    private boolean validateKeyRotationConfiguration() {
        if (!jwtProperties.isKeyRotationEnabled()) {
            return true;
        }
        
        Long rotationInterval = jwtProperties.getKeyRotationInterval();
        Long retentionTime = jwtProperties.getOldKeyRetentionTime();
        String keyVersion = jwtProperties.getKeyVersion();
        
        if (rotationInterval == null || rotationInterval < 86400000L) {
            lastValidationError = "密钥轮换间隔不能少于1天";
            return false;
        }
        
        if (retentionTime == null || retentionTime < 3600000L) {
            lastValidationError = "旧密钥保留时间不能少于1小时";
            return false;
        }
        
        if (keyVersion == null || keyVersion.trim().isEmpty()) {
            lastValidationError = "启用密钥轮换时必须指定密钥版本";
            return false;
        }
        
        if (retentionTime >= rotationInterval) {
            lastValidationError = "旧密钥保留时间必须小于轮换间隔";
            return false;
        }
        
        return true;
    }

    /**
     * 检查配置变更
     */
    private void checkConfigurationChanges() {
        String currentConfigHash = calculateConfigHash();
        
        if (previousConfigHash != null && !previousConfigHash.equals(currentConfigHash)) {
            configChangeCount.incrementAndGet();
            LoggingUtil.info(logger, "检测到JWT配置变更，配置哈希从 {} 变更为 {}", 
                previousConfigHash, currentConfigHash);
        }
        
        previousConfigHash = currentConfigHash;
    }

    /**
     * 更新配置哈希
     */
    private void updateConfigHash() {
        previousConfigHash = calculateConfigHash();
    }

    /**
     * 计算配置哈希值
     */
    private String calculateConfigHash() {
        StringBuilder sb = new StringBuilder();
        sb.append(jwtProperties.getSecret());
        sb.append(jwtProperties.getIssuer());
        sb.append(jwtProperties.getAudience());
        sb.append(jwtProperties.getSubject());
        sb.append(jwtProperties.getExpiration());
        sb.append(jwtProperties.getRefreshExpiration());
        sb.append(jwtProperties.getRefreshThreshold());
        sb.append(jwtProperties.isAutoRefreshEnabled());
        sb.append(jwtProperties.getBlacklistCacheTime());
        sb.append(jwtProperties.isDeviceBindingEnabled());
        sb.append(jwtProperties.isIpValidationEnabled());
        sb.append(jwtProperties.getMaxConcurrentActivities());
        sb.append(jwtProperties.isKeyRotationEnabled());
        sb.append(jwtProperties.getKeyRotationInterval());
        sb.append(jwtProperties.getKeyVersion());
        sb.append(jwtProperties.getOldKeyRetentionTime());
        
        return String.valueOf(sb.toString().hashCode());
    }

    /**
     * 检查密钥是否安全
     */
    private boolean isSecureKey(String key) {
        // 检查长度
        if (key.length() < 64) {
            return false;
        }
        
        // 检查字符类型
        boolean hasLower = false, hasUpper = false, hasDigit = false, hasSpecial = false;
        
        for (char c : key.toCharArray()) {
            if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if ("@$!%*?&-_+=".indexOf(c) >= 0) hasSpecial = true;
        }
        
        return hasLower && hasUpper && hasDigit && hasSpecial;
    }

    /**
     * 健康检查实现
     */
    @Override
    public Health health() {
        Health.Builder builder = configurationValid ? Health.up() : Health.down();
        
        builder.withDetail("configurationValid", configurationValid)
               .withDetail("lastValidationTime", lastValidationTime.get())
               .withDetail("lastConfigCheck", lastConfigCheck.toString())
               .withDetail("configChangeCount", configChangeCount.get())
               .withDetail("secretLength", jwtProperties.getSecret() != null ? 
                   jwtProperties.getSecret().length() : 0)
               .withDetail("expirationHours", jwtProperties.getExpiration() / 1000.0 / 3600.0)
               .withDetail("refreshExpirationDays", jwtProperties.getRefreshExpiration() / 1000.0 / 86400.0)
               .withDetail("keyRotationEnabled", jwtProperties.isKeyRotationEnabled())
               .withDetail("deviceBindingEnabled", jwtProperties.isDeviceBindingEnabled())
               .withDetail("ipValidationEnabled", jwtProperties.isIpValidationEnabled());
        
        if (lastValidationError != null) {
            builder.withDetail("lastError", lastValidationError);
        }
        
        return builder.build();
    }

    // ==================== Getter方法 ====================

    public boolean isConfigurationValid() {
        return configurationValid;
    }

    public String getLastValidationError() {
        return lastValidationError;
    }

    public LocalDateTime getLastConfigCheck() {
        return lastConfigCheck;
    }

    public long getConfigChangeCount() {
        return configChangeCount.get();
    }
}
