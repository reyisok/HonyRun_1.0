package com.honyrun.config.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.honyrun.config.properties.JwtProperties;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * JWT配置自定义验证器
 * 
 * 提供超越JSR-380标准验证的复杂业务逻辑验证
 * 支持环境相关的验证规则和安全策略检查
 * 
 * 【验证功能】：
 * - 密钥强度验证
 * - 环境相关验证
 * - 安全策略检查
 * - 配置一致性验证
 * - 业务逻辑验证
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-26 01:36:12
 * @version 1.0.0 - 初始版本，提供JWT配置自定义验证
 */
@Component
public class JwtConfigurationValidator {

    private static final Logger logger = LoggerFactory.getLogger(JwtConfigurationValidator.class);
    private final Validator validator;
    
    // 密钥复杂度验证正则表达式
    private static final Pattern PRODUCTION_KEY_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&\\-_+=])[A-Za-z\\d@$!%*?&\\-_+=]{64,}$"
    );
    
    private static final Pattern DEVELOPMENT_KEY_PATTERN = Pattern.compile(
        "^[A-Za-z\\d@$!%*?&\\-_+=]{32,}$"
    );
    
    // 弱密钥模式
    private static final String[] WEAK_KEY_PATTERNS = {
        "password", "123456", "qwerty", "admin", "secret", "default",
        "abcdef", "111111", "000000", "test", "demo", "sample", "key"
    };

    /**
     * 构造函数注入依赖
     *
     * @param validator JSR-380验证器
     */
    public JwtConfigurationValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * 验证JWT配置
     *
     * @param jwtProperties JWT配置属性
     * @return 验证结果
     */
    public ValidationResult validate(JwtProperties jwtProperties) {
        ValidationResult result = new ValidationResult();
        
        try {
            // 1. 执行JSR-380标准验证
            performStandardValidation(jwtProperties, result);
            
            // 2. 执行自定义业务逻辑验证
            performCustomValidation(jwtProperties, result);
            
            // 3. 执行环境相关验证
            performEnvironmentValidation(jwtProperties, result);
            
            // 4. 执行安全策略验证
            performSecurityValidation(jwtProperties, result);
            
            // 5. 执行配置一致性验证
            performConsistencyValidation(jwtProperties, result);
            
            LoggingUtil.debug(logger, "JWT配置验证完成，发现 {} 个错误，{} 个警告", 
                result.getErrors().size(), result.getWarnings().size());
                
        } catch (Exception e) {
            result.addError("配置验证过程中发生异常: " + e.getMessage());
            LoggingUtil.error(logger, "JWT配置验证异常", e);
        }
        
        return result;
    }

    /**
     * 执行JSR-380标准验证
     */
    private void performStandardValidation(JwtProperties jwtProperties, ValidationResult result) {
        var violations = validator.validate(jwtProperties);
        
        for (ConstraintViolation<JwtProperties> violation : violations) {
            result.addError(violation.getPropertyPath() + ": " + violation.getMessage());
        }
    }

    /**
     * 执行自定义业务逻辑验证
     */
    private void performCustomValidation(JwtProperties jwtProperties, ValidationResult result) {
        // 验证密钥强度
        validateKeyStrength(jwtProperties.getSecret(), result);
        
        // 验证时间配置逻辑
        validateTimeConfiguration(jwtProperties, result);
        
        // 验证并发配置
        validateConcurrencyConfiguration(jwtProperties, result);
        
        // 验证密钥轮换配置
        validateKeyRotationConfiguration(jwtProperties, result);
    }

    /**
     * 执行环境相关验证
     */
    private void performEnvironmentValidation(JwtProperties jwtProperties, ValidationResult result) {
        String activeProfile = System.getProperty("spring.profiles.active", "dev");
        
        switch (activeProfile) {
            case "prod":
                validateProductionEnvironment(jwtProperties, result);
                break;
            case "test":
                validateTestEnvironment(jwtProperties, result);
                break;
            case "dev":
                validateDevelopmentEnvironment(jwtProperties, result);
                break;
            default:
                result.addWarning("未知的环境配置: " + activeProfile);
        }
    }

    /**
     * 执行安全策略验证
     */
    private void performSecurityValidation(JwtProperties jwtProperties, ValidationResult result) {
        // 检查过期时间安全性
        validateExpirationSecurity(jwtProperties, result);
        
        // 检查密钥安全性
        validateKeySecurity(jwtProperties, result);
        
        // 检查功能安全性
        validateFeatureSecurity(jwtProperties, result);
    }

    /**
     * 执行配置一致性验证
     */
    private void performConsistencyValidation(JwtProperties jwtProperties, ValidationResult result) {
        // 验证时间配置一致性
        validateTimeConsistency(jwtProperties, result);
        
        // 验证功能配置一致性
        validateFeatureConsistency(jwtProperties, result);
    }

    /**
     * 验证密钥强度
     */
    private void validateKeyStrength(String secret, ValidationResult result) {
        if (secret == null || secret.trim().isEmpty()) {
            result.addError("JWT密钥不能为空");
            return;
        }
        
        // 检查密钥长度
        if (secret.length() < 32) {
            result.addError("JWT密钥长度不足，要求至少32个字符，当前: " + secret.length());
        } else if (secret.length() < 64) {
            result.addWarning("JWT密钥长度建议至少64个字符以提高安全性，当前: " + secret.length());
        }
        
        // 检查弱密钥
        String lowerSecret = secret.toLowerCase();
        for (String weakPattern : WEAK_KEY_PATTERNS) {
            if (lowerSecret.contains(weakPattern)) {
                result.addError("JWT密钥包含弱模式: " + weakPattern);
            }
        }
        
        // 检查重复字符
        if (hasRepeatingPattern(secret)) {
            result.addError("JWT密钥包含重复模式，存在安全风险");
        }
        
        // 检查字符多样性
        if (!hasCharacterDiversity(secret)) {
            result.addWarning("JWT密钥字符类型单一，建议包含大小写字母、数字和特殊字符");
        }
    }

    /**
     * 验证时间配置
     */
    private void validateTimeConfiguration(JwtProperties jwtProperties, ValidationResult result) {
        Long expiration = jwtProperties.getExpiration();
        Long refreshExpiration = jwtProperties.getRefreshExpiration();
        Long refreshThreshold = jwtProperties.getRefreshThreshold();
        Long blacklistCacheTime = jwtProperties.getBlacklistCacheTime();
        
        // 验证时间关系
        if (refreshExpiration != null && expiration != null && refreshExpiration <= expiration) {
            result.addError("刷新令牌过期时间必须大于访问令牌过期时间");
        }
        
        if (refreshThreshold != null && expiration != null && refreshThreshold >= expiration) {
            result.addError("令牌自动刷新阈值必须小于访问令牌过期时间");
        }
        
        // 验证黑名单缓存时间
        if (blacklistCacheTime != null && expiration != null && blacklistCacheTime < expiration) {
            result.addWarning("黑名单缓存时间小于访问令牌过期时间，可能导致安全问题");
        }
    }

    /**
     * 验证并发配置
     */
    private void validateConcurrencyConfiguration(JwtProperties jwtProperties, ValidationResult result) {
        int maxConcurrentActivities = jwtProperties.getMaxConcurrentActivities();
        
        if (maxConcurrentActivities > 100) {
            result.addWarning("最大并发活动数过高 (" + maxConcurrentActivities + ")，可能影响系统性能");
        }
        
        if (maxConcurrentActivities == 0) {
            result.addWarning("未限制最大并发活动数，在高并发场景下可能存在风险");
        }
    }

    /**
     * 验证密钥轮换配置
     */
    private void validateKeyRotationConfiguration(JwtProperties jwtProperties, ValidationResult result) {
        if (!jwtProperties.isKeyRotationEnabled()) {
            result.addWarning("未启用密钥轮换机制，建议在生产环境中启用");
            return;
        }
        
        Long rotationInterval = jwtProperties.getKeyRotationInterval();
        Long retentionTime = jwtProperties.getOldKeyRetentionTime();
        
        if (rotationInterval != null && rotationInterval > 7776000000L) { // 90天
            result.addWarning("密钥轮换间隔过长（超过90天），存在安全风险");
        }
        
        if (retentionTime != null && rotationInterval != null && retentionTime > rotationInterval / 2) {
            result.addWarning("旧密钥保留时间过长，建议不超过轮换间隔的一半");
        }
    }

    /**
     * 验证生产环境配置
     */
    private void validateProductionEnvironment(JwtProperties jwtProperties, ValidationResult result) {
        String secret = jwtProperties.getSecret();
        
        // 生产环境密钥强度要求更严格
        if (secret != null && !PRODUCTION_KEY_PATTERN.matcher(secret).matches()) {
            result.addError("生产环境JWT密钥必须包含大小写字母、数字和特殊字符，且长度至少64个字符");
        }
        
        // 生产环境必须启用某些安全功能
        if (!jwtProperties.isDeviceBindingEnabled()) {
            result.addWarning("生产环境建议启用设备绑定验证");
        }
        
        if (!jwtProperties.isKeyRotationEnabled()) {
            result.addWarning("生产环境强烈建议启用密钥轮换机制");
        }
        
        // 生产环境过期时间不宜过长
        if (jwtProperties.getExpiration() > 14400000L) { // 4小时
            result.addWarning("生产环境访问令牌过期时间过长，存在安全风险");
        }
    }

    /**
     * 验证测试环境配置
     */
    private void validateTestEnvironment(JwtProperties jwtProperties, ValidationResult result) {
        // 测试环境可以使用较简单的密钥
        String secret = jwtProperties.getSecret();
        if (secret != null && !DEVELOPMENT_KEY_PATTERN.matcher(secret).matches()) {
            result.addError("测试环境JWT密钥格式不正确");
        }
        
        // 测试环境令牌可以设置较短的过期时间
        if (jwtProperties.getExpiration() > 86400000L) { // 24小时
            result.addWarning("测试环境访问令牌过期时间过长，建议设置较短时间便于测试");
        }
    }

    /**
     * 验证开发环境配置
     */
    private void validateDevelopmentEnvironment(JwtProperties jwtProperties, ValidationResult result) {
        // 开发环境密钥要求相对宽松
        String secret = jwtProperties.getSecret();
        if (secret != null && secret.length() < 32) {
            result.addError("开发环境JWT密钥长度至少32个字符");
        }
        
        // 开发环境可以禁用某些安全功能
        if (jwtProperties.isIpValidationEnabled()) {
            result.addWarning("开发环境启用IP验证可能影响开发效率");
        }
    }

    /**
     * 验证过期时间安全性
     */
    private void validateExpirationSecurity(JwtProperties jwtProperties, ValidationResult result) {
        Long expiration = jwtProperties.getExpiration();
        Long refreshExpiration = jwtProperties.getRefreshExpiration();
        
        // 访问令牌过期时间安全检查
        if (expiration != null) {
            if (expiration > 86400000L) { // 24小时
                result.addWarning("访问令牌过期时间过长（超过24小时），存在安全风险");
            }
            
            if (expiration < 300000L) { // 5分钟
                result.addWarning("访问令牌过期时间过短（少于5分钟），可能影响用户体验");
            }
        }
        
        // 刷新令牌过期时间安全检查
        if (refreshExpiration != null) {
            if (refreshExpiration > 2592000000L) { // 30天
                result.addWarning("刷新令牌过期时间过长（超过30天），存在安全风险");
            }
        }
    }

    /**
     * 验证密钥安全性
     */
    private void validateKeySecurity(JwtProperties jwtProperties, ValidationResult result) {
        String secret = jwtProperties.getSecret();
        
        if (secret != null) {
            // 检查密钥是否包含敏感信息
            if (containsSensitiveInfo(secret)) {
                result.addError("JWT密钥不应包含敏感信息（如用户名、域名等）");
            }
            
            // 检查密钥是否为默认值
            if (isDefaultKey(secret)) {
                result.addError("JWT密钥不应使用默认值，存在严重安全风险");
            }
        }
    }

    /**
     * 验证功能安全性
     */
    private void validateFeatureSecurity(JwtProperties jwtProperties, ValidationResult result) {
        // 检查安全功能配置
        if (!jwtProperties.isDeviceBindingEnabled() && !jwtProperties.isIpValidationEnabled()) {
            result.addWarning("未启用任何额外的安全验证功能，建议启用设备绑定或IP验证");
        }
        
        // 检查自动刷新配置
        if (jwtProperties.isAutoRefreshEnabled() && jwtProperties.getRefreshThreshold() > jwtProperties.getExpiration() / 2) {
            result.addWarning("自动刷新阈值过高，可能导致频繁刷新");
        }
    }

    /**
     * 验证时间一致性
     */
    private void validateTimeConsistency(JwtProperties jwtProperties, ValidationResult result) {
        Long expiration = jwtProperties.getExpiration();
        Long refreshExpiration = jwtProperties.getRefreshExpiration();
        Long refreshThreshold = jwtProperties.getRefreshThreshold();
        Long blacklistCacheTime = jwtProperties.getBlacklistCacheTime();
        
        // 检查时间配置的合理性
        if (expiration != null && refreshExpiration != null) {
            double ratio = (double) refreshExpiration / expiration;
            if (ratio < 2.0) {
                result.addWarning("刷新令牌过期时间与访问令牌过期时间比例过小，建议至少为2倍");
            } else if (ratio > 100.0) {
                result.addWarning("刷新令牌过期时间与访问令牌过期时间比例过大，可能存在安全风险");
            }
        }
        
        // 检查刷新阈值的合理性
        if (expiration != null && refreshThreshold != null) {
            double thresholdRatio = (double) refreshThreshold / expiration;
            if (thresholdRatio > 0.8) {
                result.addWarning("自动刷新阈值过高，可能导致令牌很快过期");
            } else if (thresholdRatio < 0.1) {
                result.addWarning("自动刷新阈值过低，可能导致频繁刷新");
            }
        }
    }

    /**
     * 验证功能一致性
     */
    private void validateFeatureConsistency(JwtProperties jwtProperties, ValidationResult result) {
        // 检查密钥轮换与版本管理的一致性
        if (jwtProperties.isKeyRotationEnabled()) {
            String keyVersion = jwtProperties.getKeyVersion();
            if (keyVersion == null || !keyVersion.matches("^v\\d+(\\.\\d+)*$")) {
                result.addError("启用密钥轮换时，密钥版本格式必须符合规范（如v1、v1.0等）");
            }
        }
        
        // 检查设备绑定与IP验证的一致性
        if (jwtProperties.isDeviceBindingEnabled() && jwtProperties.isIpValidationEnabled()) {
            result.addWarning("同时启用设备绑定和IP验证可能过于严格，影响用户体验");
        }
    }

    /**
     * 检查是否有重复模式
     */
    private boolean hasRepeatingPattern(String key) {
        // 检查连续重复字符
        for (int i = 0; i < key.length() - 3; i++) {
            char c = key.charAt(i);
            if (key.charAt(i + 1) == c && key.charAt(i + 2) == c && key.charAt(i + 3) == c) {
                return true;
            }
        }
        
        // 检查简单递增或递减序列
        int ascending = 0, descending = 0;
        for (int i = 0; i < key.length() - 1; i++) {
            if (key.charAt(i + 1) == key.charAt(i) + 1) {
                ascending++;
            } else {
                ascending = 0;
            }
            
            if (key.charAt(i + 1) == key.charAt(i) - 1) {
                descending++;
            } else {
                descending = 0;
            }
            
            if (ascending >= 4 || descending >= 4) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 检查字符多样性
     */
    private boolean hasCharacterDiversity(String key) {
        boolean hasLower = false, hasUpper = false, hasDigit = false, hasSpecial = false;
        
        for (char c : key.toCharArray()) {
            if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if ("@$!%*?&-_+=".indexOf(c) >= 0) hasSpecial = true;
        }
        
        int diversityCount = (hasLower ? 1 : 0) + (hasUpper ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
        return diversityCount >= 3; // 至少包含3种字符类型
    }

    /**
     * 检查是否包含敏感信息
     */
    private boolean containsSensitiveInfo(String key) {
        String lowerKey = key.toLowerCase();
        String[] sensitivePatterns = {
            "honyrun", "admin", "user", "localhost", "127.0.0.1", "password",
            "username", "email", "phone", "address", "company"
        };
        
        for (String pattern : sensitivePatterns) {
            if (lowerKey.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 检查是否为默认密钥
     */
    private boolean isDefaultKey(String key) {
        String[] defaultKeys = {
            "honyrun-reactive-jwt-secret-key-2025-must-be-at-least-512-bits-long-for-hs512-algorithm-security-requirements-compliance",
            "default-jwt-secret-key",
            "jwt-secret-key",
            "secret-key"
        };
        
        for (String defaultKey : defaultKeys) {
            if (key.equals(defaultKey)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationResult{");
            sb.append("errors=").append(errors.size());
            sb.append(", warnings=").append(warnings.size());
            sb.append(", valid=").append(isValid());
            sb.append('}');
            return sb.toString();
        }
    }
}
