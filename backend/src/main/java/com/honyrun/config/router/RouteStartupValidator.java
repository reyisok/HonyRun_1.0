package com.honyrun.config.router;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.honyrun.util.router.RoutePatternExampleUtil;

/**
 * 应用启动后路由一致性校验
 *
 * 在应用启动完成后，校验 RouteRegistry 中登记的精确路由是否与统一路由函数保持一致，
 * 以避免遗漏新加入端点导致的404或权限绕行风险。
 *
 * 说明：
 * - 当前版本对"精确路由"进行一致性校验；对于正则模式路由给出统计提示。
 * - 当开启严格模式（honyrun.route.validation.strict=true）时，存在缺失路由将抛出异常。
 *
 * @author: Mr.Rey
 * @created: 2025-06-28 16:35:00
 * @modified: 2025-07-01 16:35:00
 * @version: 1.0.3
 */
@Component
public class RouteStartupValidator {

    private static final Logger logger = LoggerFactory.getLogger(RouteStartupValidator.class);

    private final RouteRegistry routeRegistry;
    private final Environment environment;
    private final UnifiedConfigManager unifiedConfigManager;
    private final List<String> routeValidationWarnings = new ArrayList<>();

    public RouteStartupValidator(
            RouteRegistry routeRegistry,
            Environment environment,
            UnifiedConfigManager unifiedConfigManager
    ) {
        this.routeRegistry = routeRegistry;
        this.environment = environment;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateRoutesOnStartup() {
        LoggingUtil.info(logger, "应用已启动，执行统一路由一致性校验");

        // 建议：执行轻量检查，确认静态前缀未被变量段覆盖；如发现潜在冲突（例如 `/statistics` 与 `/{id}`），
        // 输出告警并引导开发者调整路由顺序与注册策略，持续避免类似问题。

        // 确保注册表已初始化
        routeRegistry.clearRoutes();
        routeRegistry.initializeRoutes();
        // 轻量约定检查：静态在前、动态在后（可严格模式）
        routeValidationWarnings.clear();
        checkStaticBeforeDynamicConventions();

        Set<String> registeredExactRoutes = routeRegistry.getRegisteredRoutes();
        List<String> missingRoutes = new ArrayList<>();

        for (String path : registeredExactRoutes) {
            boolean exists = routeRegistry.isRouteExists(path);
            if (!exists) {
                missingRoutes.add(path);
                LoggingUtil.warn(logger, "登记路由未匹配任一模式或精确路径: {}", path);
            }
        }

        LoggingUtil.info(logger, "路由一致性校验统计：精确路由 {} 个，模式路由 {} 个",
                registeredExactRoutes.size(), routeRegistry.getRoutePatternCount());

        // 进一步：为每个正则模式生成典型示例路径并进行动态检测
        List<String> patternIssues = new ArrayList<>();
        for (java.util.regex.Pattern pattern : routeRegistry.getRoutePatterns()) {
            String regex = pattern.pattern();
            String example = RoutePatternExampleUtil.toExample(regex);
            if (example == null) {
                LoggingUtil.warn(logger, "无法为模式生成示例路径: {}", regex);
                patternIssues.add("示例生成失败:" + regex);
                continue;
            }
            boolean exists = routeRegistry.isRouteExists(example);
            if (!exists) {
                LoggingUtil.warn(logger, "模式示例路径未匹配：regex={}, example={}", regex, example);
                patternIssues.add("未匹配:" + regex + " -> " + example);
            } else {
                LoggingUtil.debug(logger, "模式示例路径匹配成功：regex={}, example={}", regex, example);
            }
        }

        if (!patternIssues.isEmpty()) {
            boolean strict = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.route.validation.strict", "false"));
            if (strict) {
                LoggingUtil.error(logger, "路由模式示例校验失败：{}", patternIssues);
                throw new IllegalStateException("路由模式示例校验失败: " + patternIssues);
            } else {
                LoggingUtil.warn(logger, "路由模式示例校验发现问题 {} 项（非严格模式，仅告警）：{}", patternIssues.size(), patternIssues);
            }
        }

        if (!missingRoutes.isEmpty()) {
            boolean strict = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.route.validation.strict", "false"));
            if (strict) {
                LoggingUtil.error(logger, "统一路由一致性校验失败，共缺失 {} 条精确路由: {}", missingRoutes.size(), missingRoutes);
                throw new IllegalStateException("统一路由一致性校验失败：缺失路由 " + missingRoutes);
            } else {
                LoggingUtil.warn(logger, "统一路由一致性校验发现缺失精确路由 {} 条: {}（非严格模式，仅告警）", missingRoutes.size(), missingRoutes);
            }
        } else {
            LoggingUtil.info(logger, "统一路由一致性校验通过：未发现缺失精确路由");
        }

        // 若存在静态/动态约定预警，根据严格模式决定是否阻断启动
        if (!routeValidationWarnings.isEmpty()) {
            boolean strict = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.route.validation.strict", "false"));
            if (strict) {
                LoggingUtil.error(logger, "静态/动态路由约定冲突或缺失，严格模式阻断启动：{}", routeValidationWarnings);
                throw new IllegalStateException("路由约定校验失败: " + routeValidationWarnings);
            } else {
                LoggingUtil.warn(logger, "静态/动态路由约定预警（非严格模式）：{}", routeValidationWarnings);
            }
        }
    }

    // 示例路径生成已抽取为 RoutePatternExampleUtil

    /**
     * 轻量级路由约定校验：静态在前、动态在后
     * 检查常见业务前缀下是否存在“动态段模式”覆盖而缺失静态端点的情况，
     * 例如存在 /api/v1/users/\d+ 模式但缺失 /api/v1/users/statistics 精确或模式路由时给出预警。
     * 该检查仅日志告警，不会阻断应用启动。
     */
    private void checkStaticBeforeDynamicConventions() {
        Set<String> exact = routeRegistry.getRegisteredRoutes();
        Set<java.util.regex.Pattern> patterns = routeRegistry.getRoutePatterns();

        java.util.Map<String, String[]> staticByBase = new java.util.HashMap<>();
        staticByBase.put(com.honyrun.constant.PathConstants.USER_BASE, new String[]{"/search", "/statistics", "/count", "/status", "/batch", "/empty", "/boundary"});
        staticByBase.put(com.honyrun.constant.PathConstants.MOCK_INTERFACES, new String[]{"/search"});
        staticByBase.put(com.honyrun.constant.PathConstants.SYSTEM_SETTINGS, new String[]{"/categories", "/category", "/batch", "/export", "/import"});
        staticByBase.put(com.honyrun.constant.PathConstants.VERSION_BASE, new String[]{"/info", "/build", "/status", "/history"});
        staticByBase.put(com.honyrun.constant.PathConstants.IMAGE_BASE, new String[]{"/convert", "/validate", "/batch-convert", "/compress", "/resize", "/watermark"});

        // 1) 缺失静态端点但存在动态模式的预警
        for (java.util.Map.Entry<String, String[]> entry : staticByBase.entrySet()) {
            String base = entry.getKey();
            String[] staticSegments = entry.getValue();

            boolean hasDynamic = patterns.stream().anyMatch(p -> {
                String regex = p.pattern();
                return regex.startsWith(base + "/") &&
                        (regex.contains("\\d+") || regex.contains("[^/]+") || regex.contains(".*"));
            });

            if (!hasDynamic) {
                continue;
            }

            for (String seg : staticSegments) {
                String example = base + seg;
                boolean exists = exact.contains(example) || routeRegistry.isRouteExists(example);
                if (!exists) {
                    String msg = String.format("路由约定校验：检测到前缀 %s 存在动态模式，但未发现静态端点 %s 的精确或模式注册。建议优先注册静态前缀/端点，并确保函数式路由中静态路径在动态段之前。", base, example);
                    routeValidationWarnings.add(msg);
                    LoggingUtil.warn(logger, msg);
                }
            }
        }

        // 2) 动态模式可能覆盖现有静态端点的预警（要求静态优先顺序）
        for (String exactPath : exact) {
            for (java.util.regex.Pattern p : patterns) {
                try {
                    if (p.matcher(exactPath).matches()) {
                        String msg = String.format("路由约定校验：检测到动态模式 %s 可匹配既有静态端点 %s，必须保证静态路径在动态段之前以避免误路由。", p.pattern(), exactPath);
                        routeValidationWarnings.add(msg);
                        LoggingUtil.warn(logger, msg);
                    }
                } catch (Exception e) {
                    LoggingUtil.warn(logger, "Failed to validate route pattern {} against path {}: {}", 
                        p.pattern(), exactPath, e.getMessage());
                }
            }
        }
    }

    /**
     * 暴露路由约定预警列表，供健康检查端点汇总展示
     */
    public List<String> getRouteValidationWarnings() {
        return new ArrayList<>(routeValidationWarnings);
    }
}

