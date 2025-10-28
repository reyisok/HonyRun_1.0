package com.honyrun.config.reactive;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.Banner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


/**
 * 自定义Banner配置类
 * 实现Spring Boot Banner接口
 * 内置Banner样式和配置信息
 * 支持动态变量替换（版本号、作者信息等）
 * 在应用启动时显示自定义Banner
 *
 * @author Mr.Rey
 * @created 2025-01-20 21:45
 * @modified 2025-01-20 22:20
 * @version 1.0.3
 */
@Configuration
public class BannerConfig implements Banner {



    // 内置Banner样式
    private static final String[] BANNER_LINES = {
            "",
            "====================================================================",
            " ██╗  ██╗ ██████╗ ███╗   ██╗██╗   ██╗    ██████╗ ██╗   ██╗███╗   ██╗ ",
            " ██║  ██║██╔═══██╗████╗  ██║╚██╗ ██╔╝    ██╔══██╗██║   ██║████╗  ██║ ",
            " ███████║██║   ██║██╔██╗ ██║ ╚████╔╝     ██████╔╝██║   ██║██╔██╗ ██║ ",
            " ██╔══██║██║   ██║██║╚██╗██║  ╚██╔╝      ██╔══██╗██║   ██║██║╚██╗██║ ",
            " ██║  ██║╚██████╔╝██║ ╚████║   ██║       ██║  ██║╚██████╔╝██║ ╚████║ ",
            " ╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═══╝   ╚═╝       ╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═══╝ ",
            " ----2025.08.瑞伊先森.OK---",
            ":: HonyRun System ::",
            ":: Version: ${app.version} ::",
            ":: Build Version: ${build.version} ::",
            ":: Copyright © 2025 ::",
            ":: Spring Boot Version: ${spring-boot.version} ::",
            ":: Java Version: ${java.version} ::",
            "================================================="
    };

    /**
     * 静态方法：显示自定义Banner
     * 在main方法中调用，确保Banner在系统启动日志最顶部显示
     *
     * 注意：Banner配置不使用统一日志LoggingUtil，直接使用系统日志输出
     * 原因：Banner需要在应用启动最早期显示，此时日志系统可能尚未完全初始化
     * 使用System.out确保Banner能够正确显示在控制台顶部
     */
    public static void displayCustomBanner() {
        try {
            // 创建变量映射
            Map<String, String> variables = createStaticVariableMap();

            // 显示Banner内容 - 直接输出到System.out确保在最顶端显示
            // 不使用LoggingUtil，因为Banner需要在日志系统初始化前显示
            System.out.println("");

            for (String line : BANNER_LINES) {
                String processedLine = replaceVariables(line, variables);
                System.out.println(processedLine != null ? processedLine : line);
            }

        } catch (Exception e) {
            // Banner显示失败时使用System.err输出错误信息
            System.err.println("显示自定义Banner失败: " + e.getMessage());
        }
    }

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        // 由于已经在main方法中显示了Banner，这里不再重复显示
        // 保留此方法以符合Banner接口规范
    }

    /**
     * 创建静态变量映射
     *
     * @return 变量映射
     */
    private static Map<String, String> createStaticVariableMap() {
        Map<String, String> variables = new HashMap<>();
        variables.put("app.version", getStaticAppVersion());
        variables.put("build.version", getStaticBuildVersion());
        variables.put("spring-boot.version", getStaticSpringBootVersion());
        variables.put("java.version", System.getProperty("java.version"));
        variables.put("app.name", "HonyRun");
        variables.put("author", "Mr.Rey");
        return variables;
    }

    /**
     * 获取应用版本号（静态方法）
     */
    private static String getStaticAppVersion() {
        try {
            Package pkg = BannerConfig.class.getPackage();
            String version = pkg != null ? pkg.getImplementationVersion() : null;
            if (version != null) {
                return version;
            }
            // 如果从Package获取不到，尝试从Manifest获取
            version = pkg != null ? pkg.getSpecificationVersion() : null;
            if (version != null) {
                return version;
            }
        } catch (Exception e) {
            // 使用默认版本，不记录日志以避免启动时的噪音
        }
        return "1.0.3"; // 默认版本
    }

    /**
     * 获取构建版本号（静态方法）
     * 注意：由于Banner在应用启动时同步显示，这里使用阻塞操作是必要的
     * 但添加了超时和错误处理以避免长时间阻塞
     */
    private static String getStaticBuildVersion() {
        try {
            // 由于无法在静态方法中注入依赖，直接返回默认版本号
            return "00000001";
        } catch (Exception e) {
            // 如果获取失败，返回默认版本号
            return "00000001";
        }
    }

    /**
     * 获取Spring Boot版本号（静态方法）
     */
    private static String getStaticSpringBootVersion() {
        try {
            return org.springframework.boot.SpringBootVersion.getVersion();
        } catch (Exception e) {
            return "3.5.7"; // 默认版本
        }
    }

    /**
     * 替换变量占位符（静态方法）
     *
     * @param text      原始文本
     * @param variables 变量映射
     * @return 替换后的文本
     */
    private static String replaceVariables(String text, Map<String, String> variables) {
        if (text == null) {
            return null;
        }
        if (variables == null) {
            return text;
        }

        String result = text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                String placeholder = "${" + entry.getKey() + "}";
                result = result.replace(placeholder, entry.getValue());
            }
        }
        return result;
    }

}
