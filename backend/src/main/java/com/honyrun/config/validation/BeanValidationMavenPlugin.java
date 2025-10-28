package com.honyrun.config.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import com.honyrun.HonyRunReactiveApplication;
import com.honyrun.util.LoggingUtil;

/**
 * Bean验证Maven插件集成
 * 将Bean冲突检查集成到Maven构建流程中
 *
 * @author: Mr.Rey Copyright © 2025
 * @created: 2025-06-28 16:57:03
 * @modified: 2025-07-01 18:45:00
 * @version: 1.0.3 - 配置重构后更新版本
 */
public class BeanValidationMavenPlugin {

    private static final Logger logger = LoggerFactory.getLogger(BeanValidationMavenPlugin.class);

    /**
     * Maven构建时的Bean验证入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        LoggingUtil.info(logger, "开始Maven构建Bean验证");

        try {
            // 启动Spring应用上下文，使用主应用类
            SpringApplication app = new SpringApplication(HonyRunReactiveApplication.class);
            app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);

            // 设置测试环境 - 从配置文件读取，避免硬编码
            app.setAdditionalProfiles("test");

            ConfigurableApplicationContext context = app.run(args);

            try {
                // 执行Bean验证
                performBeanValidation(context);

                LoggingUtil.info(logger, "Maven构建Bean验证完成");
                System.exit(0);

            } finally {
                context.close();
            }

        } catch (Exception e) {
            LoggingUtil.error(logger, "Maven构建Bean验证失败", e);
            System.exit(1);
        }
    }

    /**
     * 执行Bean验证
     */
    private static void performBeanValidation(ConfigurableApplicationContext context) {
        LoggingUtil.info(logger, "执行Bean配置验证");

        Environment environment = context.getEnvironment();

        // 1. Bean配置验证
        BeanValidationConfig.BeanConfigurationValidator validator = new BeanValidationConfig.BeanConfigurationValidator(
                context, environment);

        BeanValidationConfig.ValidationResult validationResult = validator.performCompleteValidation();

        // 2. Bean冲突检测
        BeanConflictDetector conflictDetector = new BeanConflictDetector(context, environment);
        BeanConflictDetector.ConflictDetectionResult conflictResult = conflictDetector.detectBeanConflicts();

        // 3. 输出验证结果
        outputValidationResults(validationResult, conflictResult);

        // 4. 检查是否有严重错误
        if (!validationResult.isValid() || conflictResult.hasConflicts()) {
            throw new RuntimeException("Bean验证失败，存在配置错误或冲突");
        }
    }

    /**
     * 输出验证结果
     */
    private static void outputValidationResults(
            BeanValidationConfig.ValidationResult validationResult,
            BeanConflictDetector.ConflictDetectionResult conflictResult) {

        LoggingUtil.info(logger, "=== Bean配置验证结果 ===");
        LoggingUtil.info(logger, "验证类型: " + validationResult.getValidationType());
        LoggingUtil.info(logger, "验证状态: " + (validationResult.isValid() ? "通过" : "失败"));

        if (!validationResult.getErrors().isEmpty()) {
            LoggingUtil.error(logger, "验证错误:");
            validationResult.getErrors().forEach(error -> LoggingUtil.error(logger, "  - " + error));
        }

        if (!validationResult.getWarnings().isEmpty()) {
            LoggingUtil.warn(logger, "验证警告:");
            validationResult.getWarnings().forEach(warning -> LoggingUtil.warn(logger, "  - " + warning));
        }

        if (!validationResult.getSuccesses().isEmpty()) {
            LoggingUtil.info(logger, "验证成功项:");
            validationResult.getSuccesses().forEach(success -> LoggingUtil.info(logger, "  - " + success));
        }

        LoggingUtil.info(logger, "=== Bean冲突检测结果 ===");
        LoggingUtil.info(logger, "冲突状态: " + (conflictResult.hasConflicts() ? "存在冲突" : "无冲突"));

        if (!conflictResult.getTypeConflicts().isEmpty()) {
            LoggingUtil.error(logger, "类型冲突:");
            conflictResult.getTypeConflicts().forEach((type, beans) -> LoggingUtil.error(logger,
                    "  - 类型 " + type.getSimpleName() + " 存在冲突Bean: " + beans));
        }

        if (!conflictResult.getPrimaryConflicts().isEmpty()) {
            LoggingUtil.error(logger, "@Primary冲突:");
            conflictResult.getPrimaryConflicts().forEach((type, beans) -> LoggingUtil.error(logger,
                    "  - 类型 " + type.getSimpleName() + " 存在多个@Primary Bean: " + beans));
        }

        if (!conflictResult.getEnvironmentConflicts().isEmpty()) {
            LoggingUtil.error(logger, "环境隔离冲突:");
            conflictResult.getEnvironmentConflicts().forEach(conflict -> LoggingUtil.error(logger, "  - " + conflict));
        }

        if (!conflictResult.getResolvedConflicts().isEmpty()) {
            LoggingUtil.info(logger, "已解决的冲突:");
            conflictResult.getResolvedConflicts().forEach((type, resolution) -> LoggingUtil.info(logger,
                    "  - 类型 " + type.getSimpleName() + " 冲突已解决: " + resolution.getResolutionMethod()));
        }

        if (!conflictResult.getErrors().isEmpty()) {
            LoggingUtil.error(logger, "检测错误:");
            conflictResult.getErrors().forEach(error -> LoggingUtil.error(logger, "  - " + error));
        }

        if (!conflictResult.getWarnings().isEmpty()) {
            LoggingUtil.warn(logger, "检测警告:");
            conflictResult.getWarnings().forEach(warning -> LoggingUtil.warn(logger, "  - " + warning));
        }
    }
}
