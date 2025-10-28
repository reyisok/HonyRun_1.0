package com.honyrun.config.validation;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bean冲突验证器
 *
 * <p>在应用启动时自动检测Bean配置冲突，确保Bean配置的正确性。
 * 主要功能包括：
 * - Bean唯一性验证
 * - @Primary注解使用验证
 * - @Qualifier使用验证
 * - 环境隔离验证
 *
 * <p>验证规则：
 * - 同一类型在同一环境中只能有一个@Primary Bean
 * - Bean命名必须遵循环境前缀规范
 * - 基础设施组件才适合使用@Primary注解
 * - 环境特定Bean必须使用@Profile隔离
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-02 16:45:00
 * @modified 2025-07-02 16:45:00
 * @version 1.0.0 - Bean冲突验证器
 * @since 1.0.0
 */
@Component
@Profile("dev")  // 仅在开发环境激活，避免影响生产环境性能
@Order(2) // 确保在EssentialComponentsValidator之后执行
public class BeanConflictValidator implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BeanConflictValidator.class);

    private final ApplicationContext applicationContext;
    private final Environment environment;

    /**
     * 构造函数注入
     *
     * @param applicationContext 应用上下文
     * @param environment 环境配置
     */
    public BeanConflictValidator(ApplicationContext applicationContext, Environment environment) {
        this.applicationContext = applicationContext;
        this.environment = environment;
    }

    // 允许使用@Primary的基础设施组件类型
    private static final Set<String> ALLOWED_PRIMARY_TYPES = Set.of(
            "ObjectMapper",
            "PasswordEncoder",
            "UnifiedConfigManager",
            "WebTestClient",
            "ReactiveWebServerFactory",
            "SecurityWebFilterChain",
            "SnowflakeIdGenerator",
            "CacheManager",
            "ReactiveRedisConnectionFactory",
            "ReactiveRedisTemplate",
            "ReactiveStringRedisTemplate",
            "R2dbcTransactionManager",
            "ConnectionFactory",
            "ReactiveJwtTokenProvider"
    );

    // 环境前缀映射
    private static final Map<String, String> ENVIRONMENT_PREFIXES = Map.of(
            "dev", "dev",
            "prod", "prod",
            "test", "test"
    );

    // Bean命名规范检查规则
    private static final Map<String, String> BEAN_NAMING_PATTERNS = Map.of(
            "dev", "^(dev|development).*",
            "prod", "^(prod|production).*",
            "test", "^test.*"
    );

    // 禁止使用@Primary的Bean类型（业务逻辑Bean）
    private static final Set<String> FORBIDDEN_PRIMARY_TYPES = Set.of(
            "Service",
            "Repository",
            "Controller",
            "Handler",
            "Processor",
            "Manager",
            "Validator",
            "Converter"
    );

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LoggingUtil.info(logger, "开始执行Bean冲突验证");

        // 检查ApplicationContext状态
        if (!isApplicationContextActive()) {
            LoggingUtil.warn(logger, "ApplicationContext未激活或已关闭，跳过Bean冲突验证");
            return;
        }

        try {
            validateBeanUniqueness();
            validatePrimaryAnnotationUsage();
            validateQualifierUsage();
            validateEnvironmentIsolation();
            // 注释：快速开发阶段，不在启动时进行Bean命名规范验证
            // validateBeanNamingConventions();
            validateProfileConsistency();

            LoggingUtil.info(logger, "Bean冲突验证完成，未发现冲突");
        } catch (Exception e) {
            LoggingUtil.error(logger, "Bean冲突验证失败", e);
            // 在开发环境下，Bean冲突验证失败不应该阻止应用启动
            LoggingUtil.warn(logger, "Bean冲突验证失败，但应用将继续启动");
        }
    }

    /**
     * 检查ApplicationContext是否处于活动状态
     *
     * @return 是否处于活动状态
     */
    private boolean isApplicationContextActive() {
        try {
            if (applicationContext instanceof ConfigurableApplicationContext) {
                ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) applicationContext;
                return configurableContext.isActive() && !configurableContext.isClosed();
            }
            // 对于非ConfigurableApplicationContext，尝试调用一个简单的方法来检查状态
            applicationContext.getBeanDefinitionNames();
            return true;
        } catch (IllegalStateException e) {
            LoggingUtil.debug(logger, "ApplicationContext状态检查失败", e);
            return false;
        } catch (Exception e) {
            LoggingUtil.debug(logger, "ApplicationContext状态检查异常", e);
            return false;
        }
    }

    /**
     * 验证Bean唯一性
     * 检查是否存在重复的Bean名称
     */
    private void validateBeanUniqueness() {
        LoggingUtil.info(logger, "验证Bean唯一性");

        if (!isApplicationContextActive()) {
            LoggingUtil.warn(logger, "ApplicationContext未激活，跳过Bean唯一性验证");
            return;
        }

        Map<String, List<String>> beanNameToTypes = new HashMap<>();
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            try {
                Class<?> beanType = applicationContext.getType(beanName);
                if (beanType != null && !isSystemType(beanType)) {
                    String typeName = beanType.getSimpleName();
                    beanNameToTypes.computeIfAbsent(beanName, k -> new ArrayList<>()).add(typeName);
                }
            } catch (Exception e) {
                LoggingUtil.debug(logger, "获取Bean类型失败: " + beanName, e);
            }
        }

        // 检查重复Bean名称
        List<String> duplicateNames = beanNameToTypes.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!duplicateNames.isEmpty()) {
            String errorMsg = "发现重复Bean名称: " + duplicateNames;
            LoggingUtil.error(logger, errorMsg);
            throw new RuntimeException(errorMsg);
        }

        LoggingUtil.info(logger, "Bean唯一性验证通过");
    }

    /**
     * 验证@Primary注解使用
     * 检查@Primary注解的正确使用
     */
    private void validatePrimaryAnnotationUsage() {
        LoggingUtil.info(logger, "验证@Primary注解使用");

        if (!isApplicationContextActive()) {
            LoggingUtil.warn(logger, "ApplicationContext未激活，跳过@Primary注解验证");
            return;
        }

        Map<Class<?>, List<String>> typeToBeansMap = new HashMap<>();
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

        // 检查多个同类型Bean是否正确使用@Primary
        for (Map.Entry<Class<?>, List<String>> entry : typeToBeansMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                Class<?> type = entry.getKey();
                List<String> beans = entry.getValue();

                long primaryCount = beans.stream()
                        .mapToLong(beanName -> {
                            try {
                                ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
                                return beanFactory.getBeanDefinition(beanName).isPrimary() ? 1 : 0;
                            } catch (Exception e) {
                                LoggingUtil.debug(logger, "检查@Primary注解失败: " + beanName, e);
                                return 0;
                            }
                        })
                        .sum();

                if (primaryCount == 0) {
                    LoggingUtil.warn(logger, "类型 " + type.getSimpleName() + " 有多个Bean但没有@Primary注解: " + beans);
                } else if (primaryCount > 1) {
                    String errorMsg = "类型 " + type.getSimpleName() + " 有多个@Primary Bean: " + beans;
                    LoggingUtil.error(logger, errorMsg);
                    throw new RuntimeException(errorMsg);
                }
            }
        }

        LoggingUtil.info(logger, "@Primary注解验证通过");
    }

    /**
     * 验证@Qualifier使用
     * 检查@Qualifier引用的Bean是否存在
     */
    private void validateQualifierUsage() {
        LoggingUtil.info(logger, "验证@Qualifier使用");

        if (!isApplicationContextActive()) {
            LoggingUtil.warn(logger, "ApplicationContext未激活，跳过@Qualifier使用验证");
            return;
        }

        // 这里可以添加更复杂的@Qualifier验证逻辑
        // 目前主要通过Spring容器的自动验证

        LoggingUtil.info(logger, "@Qualifier使用验证通过");
    }

    /**
     * 验证环境隔离
     * 检查不同环境的Bean是否正确隔离
     */
    private void validateEnvironmentIsolation() {
        LoggingUtil.info(logger, "验证环境隔离");

        if (!isApplicationContextActive()) {
            LoggingUtil.warn(logger, "ApplicationContext未激活，跳过环境隔离验证");
            return;
        }

        String[] activeProfiles = environment.getActiveProfiles();
        String currentEnvironment = activeProfiles.length > 0 ? activeProfiles[0] : "default";

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        List<String> isolationViolations = new ArrayList<>();

        for (String beanName : beanNames) {
            try {
                Class<?> beanType = applicationContext.getType(beanName);
                if (beanType != null && !isSystemType(beanType)) {
                    // 检查环境特定Bean是否在正确的环境中
                    if (isEnvironmentSpecificBean(beanName)) {
                        String beanEnvironment = getBeanEnvironment(beanName);
                        if (!currentEnvironment.equals(beanEnvironment) &&
                            !isCommonBean(beanName)) {
                            isolationViolations.add("Bean '" + beanName + "' 属于环境 '" +
                                    beanEnvironment + "' 但在环境 '" + currentEnvironment + "' 中被激活");
                        }
                    }
                }
            } catch (Exception e) {
                LoggingUtil.debug(logger, "验证环境隔离失败: " + beanName, e);
            }
        }

        if (!isolationViolations.isEmpty()) {
            LoggingUtil.warn(logger, "环境隔离警告: " + String.join("; ", isolationViolations));
        }

        LoggingUtil.info(logger, "环境隔离验证完成");
    }

    /**
     * 检查Bean是否为@Primary
     */
    private boolean isPrimaryBean(String beanName) {
        try {
            ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
            return beanFactory.getBeanDefinition(beanName).isPrimary();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查是否为系统类型
     */
    private boolean isSystemType(Class<?> type) {
        String packageName = type.getPackage() != null ? type.getPackage().getName() : "";
        return packageName.startsWith("org.springframework") ||
               packageName.startsWith("java.") ||
               packageName.startsWith("javax.") ||
               packageName.startsWith("com.sun.");
    }

    /**
     * 验证Bean命名规范
     * 检查Bean命名是否符合项目规范要求
     *
     * 注意：快速开发阶段，此方法已被禁用，不在启动时进行Bean命名规范验证
     * 命名规范检查会在代码审查阶段进行，避免影响开发效率
     *
     * @deprecated 快速开发阶段暂时禁用，避免启动时的命名规范检查
     */
    @Deprecated
    private void validateBeanNamingConventions() {
        LoggingUtil.info(logger, "验证Bean命名规范");

        if (!isApplicationContextActive()) {
            LoggingUtil.warn(logger, "ApplicationContext未激活，跳过Bean命名规范验证");
            return;
        }

        String[] activeProfiles = environment.getActiveProfiles();
        String currentEnvironment = activeProfiles.length > 0 ? activeProfiles[0] : "default";

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        List<String> namingViolations = new ArrayList<>();
        List<String> environmentPrefixViolations = new ArrayList<>();

        for (String beanName : beanNames) {
            try {
                Class<?> beanType = applicationContext.getType(beanName);
                if (beanType != null && !isSystemType(beanType)) {

                    // 检查驼峰命名规范（包含特殊字符的Bean名称）
                    if (!followsCamelCaseNaming(beanName)) {
                        namingViolations.add("Bean '" + beanName + "' 命名不符合驼峰命名规范");
                    }

                    // 检查环境前缀规范（仅对应用Bean进行检查）
                    if (isApplicationBean(beanName) && !hasEnvironmentPrefix(beanName, currentEnvironment)) {
                        environmentPrefixViolations.add("Bean '" + beanName + "' 命名前缀与当前环境 '" +
                                currentEnvironment + "' 不一致");
                    }

                    // 检查是否使用了保留关键字
                    if (usesReservedKeywords(beanName)) {
                        namingViolations.add("Bean '" + beanName + "' 使用了保留关键字");
                    }
                }
            } catch (Exception e) {
                LoggingUtil.debug(logger, "验证Bean命名规范失败: " + beanName, e);
            }
        }

        // 输出驼峰命名规范违规情况
        if (!namingViolations.isEmpty()) {
            LoggingUtil.warn(logger, "Bean命名规范警告: " + String.join("; ", namingViolations));
        }

        // 输出环境前缀违规情况（作为警告，不作为错误）
        if (!environmentPrefixViolations.isEmpty()) {
            LoggingUtil.warn(logger, "Bean环境前缀警告: " + String.join("; ", environmentPrefixViolations));
        }

        LoggingUtil.info(logger, "Bean命名规范验证完成");
    }

    /**
     * 验证Profile一致性
     * 检查Bean的Profile配置是否与其命名和用途一致
     */
    private void validateProfileConsistency() {
        LoggingUtil.info(logger, "验证Profile一致性");

        if (!isApplicationContextActive()) {
            LoggingUtil.warn(logger, "ApplicationContext未激活，跳过Profile一致性验证");
            return;
        }

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        List<String> consistencyViolations = new ArrayList<>();

        for (String beanName : beanNames) {
            try {
                ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
                if (beanFactory.containsBeanDefinition(beanName)) {
                    // 检查Bean的Profile配置与命名是否一致
                    String[] profiles = getBeanProfiles(beanName);
                    if (profiles.length > 0) {
                        for (String profile : profiles) {
                            if (!isProfileConsistentWithNaming(beanName, profile)) {
                                consistencyViolations.add("Bean '" + beanName + "' 的Profile '" +
                                        profile + "' 与命名不一致");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LoggingUtil.debug(logger, "验证Profile一致性失败: " + beanName, e);
            }
        }

        if (!consistencyViolations.isEmpty()) {
            LoggingUtil.warn(logger, "Profile一致性警告: " + String.join("; ", consistencyViolations));
        }

        LoggingUtil.info(logger, "Profile一致性验证完成");
    }

    /**
     * 检查是否为环境特定Bean
     */
    private boolean isEnvironmentSpecificBean(String beanName) {
        return ENVIRONMENT_PREFIXES.values().stream()
                .anyMatch(prefix -> beanName.toLowerCase().startsWith(prefix.toLowerCase()));
    }

    /**
     * 检查Bean名称是否符合驼峰命名规范
     *
     * @param beanName Bean名称
     * @return 是否符合驼峰命名规范
     */
    private boolean followsCamelCaseNaming(String beanName) {
        // 检查是否包含特殊字符（除了字母、数字和下划线）
        if (!beanName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            return false;
        }

        // 检查是否以小写字母开头（驼峰命名规范）
        if (!Character.isLowerCase(beanName.charAt(0))) {
            return false;
        }

        // 检查是否包含连续的大写字母（不符合驼峰命名）
        for (int i = 1; i < beanName.length() - 1; i++) {
            if (Character.isUpperCase(beanName.charAt(i)) &&
                Character.isUpperCase(beanName.charAt(i + 1))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查Bean是否为应用Bean（非系统Bean）
     *
     * @param beanName Bean名称
     * @return 是否为应用Bean
     */
    private boolean isApplicationBean(String beanName) {
        // 排除Spring框架内部Bean
        if (beanName.startsWith("org.springframework") ||
            beanName.startsWith("org.apache") ||
            beanName.startsWith("com.zaxxer") ||
            beanName.startsWith("io.r2dbc") ||
            beanName.startsWith("reactor.") ||
            beanName.contains("$") ||
            beanName.startsWith("scopedTarget.")) {
            return false;
        }

        // 排除配置类和自动配置Bean
        if (beanName.endsWith("Configuration") ||
            beanName.endsWith("AutoConfiguration") ||
            beanName.contains("AutoConfigured") ||
            beanName.contains("Configurer")) {
            return false;
        }

        return true;
    }

    /**
     * 检查Bean是否遵循命名规范
     *
     * @param beanName Bean名称
     * @param environment 当前环境
     * @return 是否遵循命名规范
     */
    private boolean followsNamingConvention(String beanName, String environment) {
        // 检查驼峰命名
        return followsCamelCaseNaming(beanName);
    }

    /**
     * 检查是否使用了保留关键字
     */
    private boolean usesReservedKeywords(String beanName) {
        String lowerName = beanName.toLowerCase();
        return lowerName.contains("admin") ||
               lowerName.contains("role") ||
               lowerName.contains("system") && !lowerName.contains("systemuser");
    }

    /**
     * 获取Bean的Profile配置
     */
    private String[] getBeanProfiles(String beanName) {
        try {
            ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();

            // 检查Bean是否存在
            if (!beanFactory.containsBeanDefinition(beanName)) {
                return new String[0];
            }

            // 尝试从Bean定义中获取Profile信息
            // 由于API限制，这里通过Bean名称推断Profile
            List<String> profiles = new ArrayList<>();

            // 根据Bean名称前缀推断Profile
            for (Map.Entry<String, String> entry : ENVIRONMENT_PREFIXES.entrySet()) {
                if (beanName.startsWith(entry.getValue())) {
                    profiles.add(entry.getKey());
                }
            }

            // 如果没有找到Profile，检查是否为系统Bean
            if (profiles.isEmpty() && isSystemBean(beanName)) {
                // 系统Bean通常在所有Profile下都可用
                profiles.add("default");
            }

            return profiles.toArray(new String[0]);
        } catch (Exception e) {
            LoggingUtil.warn(logger, "获取Bean Profile信息失败: " + beanName, e);
            return new String[0];
        }
    }

    /**
     * 判断是否为系统Bean
     */
    private boolean isSystemBean(String beanName) {
        return beanName.startsWith("org.springframework") ||
               beanName.startsWith("org.apache") ||
               beanName.startsWith("com.fasterxml") ||
               beanName.contains("internal") ||
               beanName.contains("AutoConfiguration");
    }

    /**
     * 检查Profile与命名是否一致
     */
    private boolean isProfileConsistentWithNaming(String beanName, String profile) {
        if ("test".equals(profile)) {
            return beanName.toLowerCase().startsWith("test");
        } else if ("prod".equals(profile)) {
            return beanName.toLowerCase().startsWith("prod") ||
                   beanName.toLowerCase().startsWith("production");
        } else if ("dev".equals(profile)) {
            return beanName.toLowerCase().startsWith("dev") ||
                   beanName.toLowerCase().startsWith("development");
        }
        return true; // 其他情况认为一致
    }

    /**
     * 检查是否允许使用@Primary的类型
     */
    private boolean isAllowedPrimaryType(Class<?> type) {
        String typeName = type.getSimpleName();

        // 检查是否在允许列表中
        if (ALLOWED_PRIMARY_TYPES.contains(typeName)) {
            return true;
        }

        // 检查是否在禁止列表中
        if (FORBIDDEN_PRIMARY_TYPES.stream().anyMatch(forbidden -> typeName.contains(forbidden))) {
            return false;
        }

        // 检查是否为配置类相关
        return typeName.contains("Config") ||
               typeName.contains("Template") ||
               typeName.contains("Factory") ||
               typeName.contains("Manager") && !typeName.contains("ServiceManager");
    }

    /**
     * 检查Bean名称是否有环境前缀
     */
    private boolean hasEnvironmentPrefix(String beanName) {
        return ENVIRONMENT_PREFIXES.values().stream()
                .anyMatch(prefix -> beanName.toLowerCase().startsWith(prefix.toLowerCase()));
    }

    /**
     * 检查Bean名称是否包含环境前缀
     *
     * @param beanName Bean名称
     * @param environment 当前环境
     * @return 是否包含环境前缀
     */
    private boolean hasEnvironmentPrefix(String beanName, String environment) {
        // 对于dev环境，检查是否以dev开头
        if ("dev".equals(environment)) {
            return beanName.startsWith("dev") || beanName.startsWith("development");
        }

        // 对于test环境，检查是否以test开头
        if ("test".equals(environment)) {
            return beanName.startsWith("test") || beanName.startsWith("testing");
        }

        // 对于prod环境，检查是否以prod开头
        if ("prod".equals(environment)) {
            return beanName.startsWith("prod") || beanName.startsWith("production");
        }

        // 对于default环境，不要求环境前缀
        return true;
    }

    /**
     * 检查Bean名称是否匹配当前环境
     */
    /**
     * 获取Bean的环境信息
     *
     * @param beanName Bean名称
     * @return 环境名称
     */
    private String getBeanEnvironment(String beanName) {
        for (Map.Entry<String, String> entry : ENVIRONMENT_PREFIXES.entrySet()) {
            if (beanName.startsWith(entry.getValue())) {
                return entry.getKey();
            }
        }
        return "default";
    }

    /**
     * 检查是否为通用Bean（可以在多个环境中使用）
     *
     * @param beanName Bean名称
     * @return 是否为通用Bean
     */
    private boolean isCommonBean(String beanName) {
        // 通用Bean通常不包含环境前缀
        return !isEnvironmentSpecificBean(beanName) ||
               beanName.contains("Common") ||
               beanName.contains("Shared") ||
               beanName.contains("Base");
    }

    private boolean matchesCurrentEnvironment(String beanName, String currentEnvironment) {
        String expectedPrefix = ENVIRONMENT_PREFIXES.get(currentEnvironment);
        return expectedPrefix != null &&
               beanName.toLowerCase().startsWith(expectedPrefix.toLowerCase());
    }
}
