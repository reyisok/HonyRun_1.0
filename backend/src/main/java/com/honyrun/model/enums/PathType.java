package com.honyrun.model.enums;

import java.util.Optional;

/**
 * 路径类型枚举
 * 提供类型安全的路径管理，避免硬编码路径字符串
 * 每个枚举值包含配置键、默认路径和描述信息
 *
 * @author Mr.Rey Copyright © 2025
 * @version 1.0.1
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-26 01:36:12
 */
public enum PathType {

    /**
     * 源代码基础目录
     */
    SRC_BASE("honyrun.path.src-base", "src/main/java", "项目源代码基础目录"),

    /**
     * 日志目录
     */
    LOGS_DIR("honyrun.path.logs-dir", "logs", "应用日志文件存储目录"),

    /**
     * 临时文件目录
     */
    TEMP_DIR("honyrun.path.temp-dir", "target/temp", "临时文件存储目录"),

    /**
     * 监控数据目录
     */
    MONITORING_DIR("honyrun.path.monitoring-dir", "target/monitoring", "系统监控数据存储目录"),

    /**
     * 报告输出目录
     */
    REPORTS_DIR("honyrun.path.reports-dir", "target/reports", "各类报告文件输出目录"),

    /**
     * 资源文件目录
     */
    RESOURCES_DIR("honyrun.path.resources-dir", "src/main/resources", "主要资源文件目录"),

    /**
     * 测试资源目录
     */
    TEST_RESOURCES_DIR("honyrun.path.test-resources-dir", "src/test/resources", "测试资源文件目录"),

    /**
     * 编译输出目录
     */
    CLASSES_DIR("honyrun.path.classes-dir", "target/classes", "编译后的类文件输出目录"),

    /**
     * 测试编译输出目录
     */
    TEST_CLASSES_DIR("honyrun.path.test-classes-dir", "target/test-classes", "测试类编译输出目录"),

    /**
     * 目标目录
     */
    TARGET_DIR("honyrun.path.target-dir", "target", "Maven构建目标目录"),

    /**
     * 配置目录
     */
    CONFIG_DIR("honyrun.path.config-dir", "config", "应用配置文件目录"),

    /**
     * 环境配置目录
     */
    ENV_CONFIG_DIR("honyrun.path.env-config-dir", "config/env", "环境特定配置文件目录");

    private final String configKey;
    private final String defaultPath;
    private final String description;

    /**
     * 构造函数
     *
     * @param configKey   配置键名
     * @param defaultPath 默认路径
     * @param description 路径描述
     */
    PathType(String configKey, String defaultPath, String description) {
        this.configKey = configKey;
        this.defaultPath = defaultPath;
        this.description = description;
    }

    /**
     * 获取配置键名
     *
     * @return 配置键名
     */
    public String getConfigKey() {
        return configKey;
    }

    /**
     * 获取默认路径
     *
     * @return 默认路径
     */
    public String getDefaultPath() {
        return defaultPath;
    }

    /**
     * 获取路径描述
     *
     * @return 路径描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取完整的配置属性名
     * 格式：honyrun.path.{configKey}
     *
     * @return 完整的配置属性名
     */
    public String getFullConfigKey() {
        return "honyrun.path." + configKey;
    }

    /**
     * 根据配置键查找路径类型
     *
     * @param configKey 配置键
     * @return 对应的路径类型，如果未找到返回Optional.empty()
     */
    public static Optional<PathType> findByConfigKey(String configKey) {
        if (configKey == null) {
            return Optional.empty();
        }
        for (PathType pathType : values()) {
            if (pathType.configKey.equals(configKey)) {
                return Optional.of(pathType);
            }
        }
        return Optional.empty();
    }

    /**
     * 根据默认路径查找路径类型
     *
     * @param defaultPath 默认路径
     * @return 对应的路径类型，如果未找到返回Optional.empty()
     */
    public static Optional<PathType> findByDefaultPath(String defaultPath) {
        if (defaultPath == null) {
            return Optional.empty();
        }
        for (PathType pathType : values()) {
            if (pathType.defaultPath.equals(defaultPath)) {
                return Optional.of(pathType);
            }
        }
        return Optional.empty();
    }
}
