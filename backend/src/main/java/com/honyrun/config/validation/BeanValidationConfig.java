package com.honyrun.config.validation;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.*;

/**
 * Bean配置运行时验证配置类 - 重构版本
 *
 * <p>按照Spring Boot 3最佳实践重新设计的Bean验证配置类。
 * 主要改进：
 * - 简化验证逻辑，移除重复方法
 * - 拆分职责，提高代码可维护性
 * - 利用Spring原生机制进行环境隔离
 * - 减少对Bean名称模式匹配的依赖
 *
 * <p>核心功能：
 * - Bean环境隔离验证
 * - Bean健康状态检查
 * - 关键Bean依赖验证
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-06-28 22:00:00
 * @modified 2025-06-29 22:00:00
 * @version 2.0.0 - 按Spring Boot 3最佳实践重构
 * @since 1.0.0
 */
@Configuration
public class BeanValidationConfig {

    private static final Logger logger = LoggerFactory.getLogger(BeanValidationConfig.class);

    private final ApplicationContext applicationContext;
    private final Environment environment;

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param applicationContext Spring应用上下文
     * @param environment Spring环境对象
     */
    public BeanValidationConfig(ApplicationContext applicationContext, Environment environment) {
        this.applicationContext = applicationContext;
        this.environment = environment;
    }

    /**
     * Bean配置验证器 - 简化版本
     *
     * <p>提供核心Bean验证功能，专注于关键Bean的环境隔离验证。
     *
     * @return Bean配置验证器实例
     * @author Mr.Rey Copyright © 2025
     * @since 2.0.0
     */
    @Bean("devBeanConfigurationValidator")
    @Profile("dev")
    public BeanConfigurationValidator beanConfigurationValidator() {
        LoggingUtil.info(logger, "初始化Bean配置验证器 - 重构版本");
        return new BeanConfigurationValidator(applicationContext, environment);
    }

    /**
     * Bean健康检查器
     *
     * @return Bean健康检查器实例
     * @author Mr.Rey Copyright © 2025
     * @since 2.0.0
     */
    @Bean("devBeanHealthIndicator")
    @Profile("dev")
    public BeanHealthIndicator beanHealthIndicator() {
        LoggingUtil.info(logger, "初始化Bean健康检查器");
        return new BeanHealthIndicator(applicationContext, environment);
    }

    /**
     * Bean配置验证器实现类 - 重构版本
     *
     * <p>按照Spring Boot最佳实践重新设计：
     * - 移除重复的方法重载
     * - 简化验证逻辑
     * - 专注于关键Bean验证
     * - 利用Spring Profile机制
     */
    public static class BeanConfigurationValidator {

        private static final Logger logger = LoggerFactory.getLogger(BeanConfigurationValidator.class);

        private final ApplicationContext applicationContext;
        private final Environment environment;

        // 关键Bean类型 - 需要环境隔离的Bean
        private static final Set<Class<?>> CRITICAL_BEAN_TYPES = Set.of(
            ReactiveRedisConnectionFactory.class,
            ReactiveRedisTemplate.class,
            DatabaseClient.class
        );

        public BeanConfigurationValidator(ApplicationContext applicationContext, Environment environment) {
            this.applicationContext = applicationContext;
            this.environment = environment;
        }

        /**
         * 验证Bean环境隔离配置 - 简化版本
         *
         * <p>专注于关键Bean的环境隔离验证，避免过度验证。
         *
         * @return 验证结果
         * @author Mr.Rey Copyright © 2025
         * @since 2.0.0
         */
        public ValidationResult validateEnvironmentIsolation() {
            LoggingUtil.info(logger, "开始验证Bean环境隔离配置 - 简化版本");

            try {
                String[] activeProfiles = environment.getActiveProfiles();
                String currentProfile = activeProfiles.length > 0 ? activeProfiles[0] : "default";

                LoggingUtil.info(logger, "当前激活的Profile: {}", currentProfile);

                ValidationResult result = new ValidationResult();
                result.setValidationType("环境隔离验证");
                result.setCurrentProfile(currentProfile);

                // 只验证关键Bean的环境隔离
                validateCriticalBeansIsolation(result, currentProfile);

                LoggingUtil.info(logger, "Bean环境隔离验证完成，结果: {}", result.isValid() ? "通过" : "失败");
                return result;

            } catch (Exception e) {
                LoggingUtil.error(logger, "Bean环境隔离验证失败", e);
                ValidationResult errorResult = new ValidationResult();
                errorResult.setValid(false);
                errorResult.addError("验证过程异常: " + e.getMessage());
                return errorResult;
            }
        }

        /**
         * 验证关键Bean的环境隔离 - 简化版本
         *
         * <p>只验证关键Bean是否存在，不再检查命名约定。
         *
         * @param result 验证结果
         * @param currentProfile 当前环境配置
         * @author Mr.Rey Copyright © 2025
         * @since 2.0.0
         */
        private void validateCriticalBeansIsolation(ValidationResult result, String currentProfile) {
            for (Class<?> beanType : CRITICAL_BEAN_TYPES) {
                try {
                    Map<String, ?> beans = applicationContext.getBeansOfType(beanType);

                    LoggingUtil.debug(logger, "验证{}类型的Bean: {}", beanType.getSimpleName(), beans.keySet());

                    if (beans.isEmpty()) {
                        result.addError(String.format("关键Bean类型 '%s' 未找到", beanType.getSimpleName()));
                    } else {
                        for (String beanName : beans.keySet()) {
                            result.addSuccess(String.format("Bean '%s' 存在且可用", beanName));
                        }
                    }

                } catch (Exception e) {
                    result.addError(String.format("验证%s类型Bean失败: %s", beanType.getSimpleName(), e.getMessage()));
                    LoggingUtil.warn(logger, "验证Bean类型{}时发生异常", beanType.getSimpleName(), e);
                }
            }
        }



        /**
         * 验证Bean依赖关系 - 简化版本
         *
         * <p>验证关键Bean是否存在，确保应用正常运行。
         *
         * @return 验证结果
         * @author Mr.Rey Copyright © 2025
         * @since 2.0.0
         */
        public ValidationResult validateBeanDependencies() {
            LoggingUtil.info(logger, "开始验证Bean依赖关系");

            ValidationResult result = new ValidationResult();
            result.setValidationType("Bean依赖验证");

            try {
                for (Class<?> beanType : CRITICAL_BEAN_TYPES) {
                    validateCriticalBeanExists(result, beanType);
                }

                LoggingUtil.info(logger, "Bean依赖验证完成");
                return result;

            } catch (Exception e) {
                LoggingUtil.error(logger, "Bean依赖验证失败", e);
                result.setValid(false);
                result.addError("依赖验证异常: " + e.getMessage());
                return result;
            }
        }

        /**
         * 验证关键Bean是否存在
         *
         * @param result 验证结果
         * @param beanType Bean类型
         * @author Mr.Rey Copyright © 2025
         * @since 2.0.0
         */
        private void validateCriticalBeanExists(ValidationResult result, Class<?> beanType) {
            try {
                Map<String, ?> beans = applicationContext.getBeansOfType(beanType);
                if (beans.isEmpty()) {
                    result.addError(String.format("关键Bean类型 %s 未找到", beanType.getSimpleName()));
                } else {
                    result.addSuccess(String.format("关键Bean类型 %s 存在: %s", beanType.getSimpleName(), beans.keySet()));
                }
            } catch (Exception e) {
                result.addError(String.format("检查Bean类型 %s 时发生异常: %s", beanType.getSimpleName(), e.getMessage()));
            }
        }

        /**
         * 执行完整验证 - 简化版本
         *
         * @return 综合验证结果
         * @author Mr.Rey Copyright © 2025
         * @since 2.0.0
         */
        public ValidationResult performCompleteValidation() {
            LoggingUtil.info(logger, "开始执行完整Bean验证");

            ValidationResult combinedResult = new ValidationResult();
            combinedResult.setValidationType("完整验证");

            // 环境隔离验证
            ValidationResult isolationResult = validateEnvironmentIsolation();
            mergeValidationResults(combinedResult, isolationResult);

            // 依赖关系验证
            ValidationResult dependencyResult = validateBeanDependencies();
            mergeValidationResults(combinedResult, dependencyResult);

            LoggingUtil.info(logger, "完整Bean验证完成，最终结果: {}", combinedResult.isValid() ? "通过" : "失败");
            return combinedResult;
        }

        /**
         * 合并验证结果
         *
         * @param target 目标结果
         * @param source 源结果
         * @author Mr.Rey Copyright © 2025
         * @since 2.0.0
         */
        private void mergeValidationResults(ValidationResult target, ValidationResult source) {
            if (!source.isValid()) {
                target.setValid(false);
            }
            target.getErrors().addAll(source.getErrors());
            target.getWarnings().addAll(source.getWarnings());
            target.getSuccesses().addAll(source.getSuccesses());
        }
    }

    /**
     * Bean健康检查器实现类
     *
     * <p>提供Bean健康状态检查功能，用于监控Bean的运行状态。
     */
    public static class BeanHealthIndicator implements HealthIndicator {

        private static final Logger logger = LoggerFactory.getLogger(BeanHealthIndicator.class);

        private final ApplicationContext applicationContext;
        private final Environment environment;

        public BeanHealthIndicator(ApplicationContext applicationContext, Environment environment) {
            this.applicationContext = applicationContext;
            this.environment = environment;
        }

        @Override
        public Health health() {
            try {
                // 检查关键Bean是否正常
                BeanConfigurationValidator validator = new BeanConfigurationValidator(applicationContext, environment);
                ValidationResult result = validator.validateBeanDependencies();

                if (result.isValid()) {
                    return Health.up()
                        .withDetail("beanCount", applicationContext.getBeanDefinitionCount())
                        .withDetail("activeProfiles", Arrays.toString(environment.getActiveProfiles()))
                        .withDetail("validationStatus", "通过")
                        .build();
                } else {
                    return Health.down()
                        .withDetail("validationErrors", result.getErrors())
                        .withDetail("validationStatus", "失败")
                        .build();
                }

            } catch (Exception e) {
                LoggingUtil.error(logger, "Bean健康检查失败", e);
                return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("validationStatus", "异常")
                    .build();
            }
        }
    }

    /**
     * 验证结果类
     *
     * <p>封装验证结果信息，包括成功、警告、错误等状态。
     */
    public static class ValidationResult {
        private boolean valid = true;
        private String validationType;
        private String currentProfile;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private List<String> successes = new ArrayList<>();

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getValidationType() {
            return validationType;
        }

        public void setValidationType(String validationType) {
            this.validationType = validationType;
        }

        public String getCurrentProfile() {
            return currentProfile;
        }

        public void setCurrentProfile(String currentProfile) {
            this.currentProfile = currentProfile;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void addError(String error) {
            this.errors.add(error);
            this.valid = false;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }

        public List<String> getSuccesses() {
            return successes;
        }

        public void addSuccess(String success) {
            this.successes.add(success);
        }

        @Override
        public String toString() {
            return String.format("ValidationResult{valid=%s, type='%s', profile='%s', errors=%d, warnings=%d, successes=%d}",
                valid, validationType, currentProfile, errors.size(), warnings.size(), successes.size());
        }
    }
}
