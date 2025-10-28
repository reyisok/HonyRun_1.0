package com.honyrun.config.validation;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Bean冲突检测器
 * 用于检测和分析Spring容器中的Bean冲突问题
 *
 * @author: Mr.Rey Copyright © 2025
 * @created: 2025-06-28 16:57:03
 * @version: 1.0.3 - 32G高性能环境优化版本
 */
@Component
public class BeanConflictDetector {

    private static final Logger logger = LoggerFactory.getLogger(BeanConflictDetector.class);

    private final ApplicationContext applicationContext;
    private final Environment environment;

    public BeanConflictDetector(ApplicationContext applicationContext, Environment environment) {
        this.applicationContext = applicationContext;
        this.environment = environment;
    }

    /**
     * 检测Bean冲突
     *
     * @return 冲突检测结果
     */
    public ConflictDetectionResult detectBeanConflicts() {
        LoggingUtil.info(logger, "开始检测Bean冲突");

        ConflictDetectionResult result = new ConflictDetectionResult();

        try {
            // 1. 检测类型冲突
            detectTypeConflicts(result);

            // 2. 检测@Primary冲突
            detectPrimaryConflicts(result);

            // 3. 检测环境隔离冲突
            detectEnvironmentConflicts(result);

            LoggingUtil.info(logger, "Bean冲突检测完成");
            return result;

        } catch (Exception e) {
            LoggingUtil.error(logger, "Bean冲突检测失败", e);
            result.addError("冲突检测异常: " + e.getMessage());
            return result;
        }
    }

    /**
     * 检测类型冲突
     */
    private void detectTypeConflicts(ConflictDetectionResult result) {
        LoggingUtil.info(logger, "检测类型冲突");

        Map<Class<?>, List<String>> typeToBeansMap = new HashMap<>();

        // 收集所有Bean的类型信息
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            try {
                Class<?> beanType = applicationContext.getType(beanName);
                if (beanType != null && !isSystemType(beanType)) {
                    typeToBeansMap.computeIfAbsent(beanType, k -> new ArrayList<>()).add(beanName);
                }
            } catch (Exception e) {
                LoggingUtil.debug(logger, "获取Bean类型失败: " + beanName, e);
            }
        }

        // 检查类型冲突
        for (Map.Entry<Class<?>, List<String>> entry : typeToBeansMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                Class<?> type = entry.getKey();
                List<String> conflictingBeans = entry.getValue();

                // 检查是否有@Primary解决冲突
                boolean hasPrimary = conflictingBeans.stream()
                    .anyMatch(this::isPrimaryBean);

                if (!hasPrimary) {
                    result.addTypeConflict(type, conflictingBeans);
                } else {
                    result.addResolvedConflict(type, conflictingBeans, "通过@Primary解决");
                }
            }
        }
    }

    /**
     * 检测@Primary冲突
     */
    private void detectPrimaryConflicts(ConflictDetectionResult result) {
        LoggingUtil.info(logger, "检测@Primary冲突");

        Map<Class<?>, List<String>> primaryBeansMap = new HashMap<>();

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            if (isPrimaryBean(beanName)) {
                try {
                    Class<?> beanType = applicationContext.getType(beanName);
                    if (beanType != null && !isSystemType(beanType)) {
                        primaryBeansMap.computeIfAbsent(beanType, k -> new ArrayList<>()).add(beanName);
                    }
                } catch (Exception e) {
                    LoggingUtil.debug(logger, "获取Primary Bean类型失败: " + beanName, e);
                }
            }
        }

        // 检查同类型多个@Primary
        for (Map.Entry<Class<?>, List<String>> entry : primaryBeansMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.addPrimaryConflict(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 检测环境隔离冲突
     */
    private void detectEnvironmentConflicts(ConflictDetectionResult result) {
        LoggingUtil.info(logger, "检测环境隔离冲突");

        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            result.addWarning("未检测到活跃的Profile");
            return;
        }

        String currentProfile = activeProfiles[0];
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            if (isApplicationBean(beanName)) {
                String beanProfile = extractProfileFromBeanName(beanName);
                if (beanProfile != null && !beanProfile.equals(currentProfile)) {
                    result.addEnvironmentConflict(beanName, currentProfile, beanProfile);
                }
            }
        }
    }

    /**
     * 判断是否为Primary Bean
     */
    private boolean isPrimaryBean(String beanName) {
        try {
            if (!applicationContext.containsBeanDefinition(beanName)) {
                return false;
            }
            return ((ConfigurableApplicationContext) applicationContext).getBeanFactory().getBeanDefinition(beanName).isPrimary();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是否为系统类型
     */
    private boolean isSystemType(Class<?> type) {
        String packageName = type.getPackage() != null ? type.getPackage().getName() : "";
        return packageName.startsWith("org.springframework") ||
               packageName.startsWith("java.") ||
               packageName.startsWith("javax.") ||
               packageName.startsWith("com.sun.");
    }

    /**
     * 判断是否为应用Bean
     */
    private boolean isApplicationBean(String beanName) {
        return !beanName.startsWith("org.springframework") &&
               !beanName.startsWith("spring") &&
               !beanName.contains("Internal") &&
               !beanName.contains("Auto");
    }

    /**
     * 从Bean名称中提取Profile信息
     */
    private String extractProfileFromBeanName(String beanName) {
        if (beanName.startsWith("prod") || beanName.contains("Production")) {
            return "prod";
        } else if (beanName.startsWith("dev") || beanName.contains("Development")) {
            return "dev";
        } else if (beanName.startsWith("test") || beanName.contains("Test")) {
            return "test";
        }
        return null;
    }

    /**
     * 冲突检测结果
     */
    public static class ConflictDetectionResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final Map<Class<?>, List<String>> typeConflicts = new HashMap<>();
        private final Map<Class<?>, List<String>> primaryConflicts = new HashMap<>();
        private final List<String> environmentConflicts = new ArrayList<>();
        private final Map<Class<?>, ConflictResolution> resolvedConflicts = new HashMap<>();

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public void addTypeConflict(Class<?> type, List<String> beans) {
            typeConflicts.put(type, beans);
        }

        public void addPrimaryConflict(Class<?> type, List<String> beans) {
            primaryConflicts.put(type, beans);
        }

        public void addEnvironmentConflict(String beanName, String currentProfile, String beanProfile) {
            environmentConflicts.add(String.format("Bean '%s' 属于环境 '%s'，但当前环境为 '%s'",
                beanName, beanProfile, currentProfile));
        }

        public void addResolvedConflict(Class<?> type, List<String> beans, String resolution) {
            resolvedConflicts.put(type, new ConflictResolution(beans, resolution));
        }

        public boolean hasConflicts() {
            return !typeConflicts.isEmpty() || !primaryConflicts.isEmpty() || !environmentConflicts.isEmpty();
        }

        // Getters
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public Map<Class<?>, List<String>> getTypeConflicts() { return typeConflicts; }
        public Map<Class<?>, List<String>> getPrimaryConflicts() { return primaryConflicts; }
        public List<String> getEnvironmentConflicts() { return environmentConflicts; }
        public Map<Class<?>, ConflictResolution> getResolvedConflicts() { return resolvedConflicts; }
    }

    /**
     * 冲突解决方案
     */
    public static class ConflictResolution {
        private final List<String> conflictingBeans;
        private final String resolutionMethod;

        public ConflictResolution(List<String> conflictingBeans, String resolutionMethod) {
            this.conflictingBeans = conflictingBeans;
            this.resolutionMethod = resolutionMethod;
        }

        public List<String> getConflictingBeans() { return conflictingBeans; }
        public String getResolutionMethod() { return resolutionMethod; }
    }
}

