package com.honyrun.util.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * 路由模式示例路径生成工具
 *
 * 负责将常见正则片段与Spring风格占位符转换为具有代表性的示例路径，
 * 以用于启动后路由一致性动态校验。
 */
public final class RoutePatternExampleUtil {

    private static final Logger logger = LoggerFactory.getLogger(RoutePatternExampleUtil.class);

    private RoutePatternExampleUtil() {}

    /**
     * 从已编译的正则Pattern生成示例路径
     */
    public static String toExample(Pattern pattern) {
        if (pattern == null) return null;
        return toExample(pattern.pattern());
    }

    /**
     * 从正则字符串生成示例路径
     * 支持片段：\d+、[^/]+、[^/]*、.*、/**、/*、{var}
     */
    public static String toExample(String regex) {
        if (regex == null || regex.isBlank()) return null;
        String example = regex;
        try {
            // 去除尾部锚点
            example = example.replaceAll("\\$", "");

            // 处理 Spring 风格占位符（若还存在）
            example = example.replaceAll("\\{[^}]+\\}", "abc");

            // 通配符与量词
            example = example.replace("/\\.\\*", "/test"); // "/.*" -> "/test"
            example = example.replace("\\.\\*", "test");     // ".*" -> "test"
            example = example.replace("\\\\d\\+", "123");  // "\d+" -> "123"
            example = example.replaceAll("\\[\\^/\\]\\+", "abc"); // "[^/]+" -> "abc"
            example = example.replace("/\\[\\^/\\]\\*", "/abc"); // "[^/]*" -> "abc"

            // Spring 路由常见模式
            example = example.replace("/\\*\\*", "/any/sub"); // "/**" -> 多层子路径
            example = example.replace("/\\*", "/any");        // "/*" -> 单层子路径

            // 确保以斜杠开头
            if (!example.startsWith("/")) {
                example = "/" + example;
            }

            // 简单清理：避免出现重复斜杠
            example = example.replaceAll("//+", "/");
            return example;
        } catch (Exception e) {
            logger.warn("示例路径生成异常: regex={}, err={}", regex, e.getMessage());
            return null;
        }
    }
}
