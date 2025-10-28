package com.honyrun.util.version;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.common.DateUtil;
import com.honyrun.util.common.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * 构建版本管理器
 * 负责8位版本号生成、版本文件管理、构建集成等功能
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01  15:30:00
 * @modified 2025-10-26 01:36:12
 * @version 2.0.0
 */
@Component
public class BuildVersionManager {

    private static final Logger logger = LoggerFactory.getLogger(BuildVersionManager.class);

    private static final String VERSION_FILE = "build-version.properties";
    private static final String VERSION_KEY = "build.version";
    private static final String BUILD_COUNT_KEY = "build.count";
    private static final String LAST_BUILD_TIME_KEY = "build.last.time";
    private static final String BUILD_HISTORY_KEY = "build.history";

    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     * 
     * @param unifiedConfigManager 统一配置管理器
     */
    public BuildVersionManager(UnifiedConfigManager unifiedConfigManager) {
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 获取当前8位构建版本号
     * 格式：YYYYMMDD（年月日8位数字）
     *
     * @return 响应式的8位版本号
     */
    public Mono<String> getCurrentBuildVersion() {
        return Mono.fromCallable(() -> {
            try {
                Properties props = loadVersionProperties();
                String currentVersion = props.getProperty(VERSION_KEY);

                if (StringUtil.isEmpty(currentVersion)) {
                    // 如果没有版本号，生成新的8位版本号
                    currentVersion = generateNewBuildVersion();
                    LoggingUtil.info(logger, "Generated new build version: {}", currentVersion);
                }

                return currentVersion;
            } catch (Exception e) {
                LoggingUtil.error(logger, "Failed to get current build version", e);
                // 返回默认版本号
                return generateNewBuildVersion();
            }
        });
    }

    /**
     * 生成新的8位构建版本号
     * 格式：00000001（8位数字，左侧补零）
     *
     * @return 新的8位版本号
     */
    public String generateNewBuildVersion() {
        // 返回初始版本号：00000001
        return "00000001";
    }

    /**
     * 递增构建版本号
     * 如果是同一天，在8位日期后添加序号
     *
     * @return 响应式的新版本号
     */
    public Mono<String> incrementBuildVersion() {
        // 在响应式环境中避免阻塞调用，直接返回默认值
        boolean autoIncrement = true;
        
        if (!autoIncrement) {
            LoggingUtil.info(logger, "Auto increment is disabled");
            return getCurrentBuildVersion();
        }

        return Mono.fromCallable(() -> {
            try {
                Properties props = loadVersionProperties();
                String currentVersion = props.getProperty(VERSION_KEY);
                String newVersion = generateIncrementedVersion(currentVersion);

                // 更新构建计数
                int buildCount = Integer.parseInt(props.getProperty(BUILD_COUNT_KEY, "0")) + 1;

                // 保存新版本信息
                props.setProperty(VERSION_KEY, newVersion);
                props.setProperty(BUILD_COUNT_KEY, String.valueOf(buildCount));
                props.setProperty(LAST_BUILD_TIME_KEY, DateUtil.format(LocalDateTime.now()));

                // 添加到构建历史
                addToBuildHistory(props, newVersion);

                saveVersionProperties(props);

                LoggingUtil.info(logger, "Build version incremented to: {}, build count: {}", newVersion, buildCount);
                return newVersion;

            } catch (Exception e) {
                LoggingUtil.error(logger, "Failed to increment build version", e);
                throw new RuntimeException("Failed to increment build version", e);
            }
        });
    }

    /**
     * 生成递增的版本号
     * 格式：00000001 -> 00000002（8位数字，严格递增）
     *
     * @param currentVersion 当前版本号
     * @return 新的版本号
     */
    private String generateIncrementedVersion(String currentVersion) {
        if (StringUtil.isEmpty(currentVersion)) {
            // 如果没有当前版本，返回初始版本
            return "00000001";
        }

        try {
            // 将当前版本号转换为整数，然后递增
            int versionNumber = Integer.parseInt(currentVersion);
            int newVersionNumber = versionNumber + 1;

            // 确保不超过8位数字的最大值（99999999）
            if (newVersionNumber > 99999999) {
                throw new RuntimeException("Version number exceeded maximum value (99999999)");
            }

            // 格式化为8位数字，左侧补零
            return String.format("%08d", newVersionNumber);

        } catch (NumberFormatException e) {
            LoggingUtil.error(logger, "Invalid version format: {}, resetting to 00000001", currentVersion);
            return "00000001";
        }
    }

    /**
     * 添加到构建历史
     *
     * @param props 属性对象
     * @param version 版本号
     */
    private void addToBuildHistory(Properties props, String version) {
        String history = props.getProperty(BUILD_HISTORY_KEY, "");
        String timestamp = DateUtil.format(LocalDateTime.now());
        String newEntry = version + ":" + timestamp;

        if (StringUtil.isEmpty(history)) {
            history = newEntry;
        } else {
            // 保留最近10次构建历史
            String[] entries = history.split(",");
            StringBuilder newHistory = new StringBuilder(newEntry);

            int maxEntries = Math.min(entries.length, 9);
            for (int i = 0; i < maxEntries; i++) {
                newHistory.append(",").append(entries[i]);
            }

            history = newHistory.toString();
        }

        props.setProperty(BUILD_HISTORY_KEY, history);
    }

    /**
     * 获取构建统计信息
     *
     * @return 响应式的构建统计信息
     */
    public Mono<BuildStatistics> getBuildStatistics() {
        return Mono.fromCallable(() -> {
            try {
                LoggingUtil.debug(logger, "Starting to get build statistics");
                Properties props = loadVersionProperties();
                LoggingUtil.debug(logger, "Loaded properties: {}", props);

                BuildStatistics stats = new BuildStatistics();
                String currentVersion = props.getProperty(VERSION_KEY, "");
                LoggingUtil.debug(logger, "Current version from properties: {}", currentVersion);

                // 如果版本号为空，使用默认版本号
                if (StringUtil.isEmpty(currentVersion)) {
                    currentVersion = generateNewBuildVersion();
                    LoggingUtil.info(logger, "Using default build version: {}", currentVersion);
                }

                stats.setCurrentVersion(currentVersion);

                String buildCountStr = props.getProperty(BUILD_COUNT_KEY, "0");
                LoggingUtil.debug(logger, "Build count string: {}", buildCountStr);
                stats.setBuildCount(Integer.parseInt(buildCountStr));

                stats.setLastBuildTime(props.getProperty(LAST_BUILD_TIME_KEY, ""));
                stats.setBuildHistory(props.getProperty(BUILD_HISTORY_KEY, ""));
                // 在响应式环境中避免阻塞调用，直接返回默认值
        stats.setAppVersion("2.0.0");

                LoggingUtil.debug(logger, "Build statistics created successfully: version={}, count={}",
                    stats.getCurrentVersion(), stats.getBuildCount());
                return stats;

            } catch (IOException e) {
                LoggingUtil.error(logger, "IO error while loading version properties: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to load version properties", e);
            } catch (NumberFormatException e) {
                LoggingUtil.error(logger, "Number format error while parsing build count: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to parse build count", e);
            } catch (Exception e) {
                LoggingUtil.error(logger, "Unexpected error while getting build statistics: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to get build statistics", e);
            }
        });
    }

    /**
     * 重置版本信息
     *
     * @return 响应式的操作结果
     */
    public Mono<Void> resetVersionInfo() {
        return Mono.fromRunnable(() -> {
            try {
                Properties props = new Properties();
                props.setProperty(VERSION_KEY, generateNewBuildVersion());
                props.setProperty(BUILD_COUNT_KEY, "0");
                props.setProperty(LAST_BUILD_TIME_KEY, "");
                props.setProperty(BUILD_HISTORY_KEY, "");

                saveVersionProperties(props);
                LoggingUtil.info(logger, "Version information has been reset");

            } catch (Exception e) {
                LoggingUtil.error(logger, "Failed to reset version information", e);
                throw new RuntimeException("Failed to reset version information", e);
            }
        });
    }

    /**
     * 加载版本属性文件
     *
     * @return 属性对象
     * @throws IOException IO异常
     */
    private Properties loadVersionProperties() throws IOException {
        Properties props = new Properties();

        try {
            // 首先尝试从项目根目录加载
            Path versionFile = Paths.get("build-version.properties");
            LoggingUtil.debug(logger, "Checking version file at: {}", versionFile.toAbsolutePath());

            if (Files.exists(versionFile)) {
                try (InputStream is = Files.newInputStream(versionFile)) {
                    props.load(is);
                    LoggingUtil.info(logger, "Loaded version properties from: {}", versionFile.toAbsolutePath());
                    LoggingUtil.debug(logger, "Properties loaded: {}", props);
                }
            } else {
                LoggingUtil.warn(logger, "Version file not found at: {}", versionFile.toAbsolutePath());
                // 尝试从类路径加载
                Resource resource = new ClassPathResource(VERSION_FILE);
                if (resource.exists()) {
                    try (InputStream is = resource.getInputStream()) {
                        props.load(is);
                        LoggingUtil.info(logger, "Loaded version properties from classpath");
                        LoggingUtil.debug(logger, "Properties loaded: {}", props);
                    }
                } else {
                    LoggingUtil.warn(logger, "Version properties file not found in classpath either, using defaults");
                }
            }
        } catch (Exception e) {
            LoggingUtil.error(logger, "Failed to load version properties: {}", e.getMessage(), e);
            throw new IOException("Failed to load version properties", e);
        }

        return props;
    }

    /**
     * 保存版本属性文件
     *
     * @param props 属性对象
     * @throws IOException IO异常
     */
    private void saveVersionProperties(Properties props) throws IOException {
        // 保存到项目根目录
        Path versionFile = Paths.get("build-version.properties");

        try (OutputStream os = Files.newOutputStream(versionFile)) {
            props.store(os, "Build Version Information - Generated by BuildVersionManager");
        }

        LoggingUtil.debug(logger, "Version properties saved to: {}", versionFile.toAbsolutePath());
    }

    /**
     * 构建统计信息内部类
     */
    public static class BuildStatistics {
        private String currentVersion;
        private int buildCount;
        private String lastBuildTime;
        private String buildHistory;
        private String appVersion;

        // Getters and Setters
        public String getCurrentVersion() {
            return currentVersion;
        }

        public void setCurrentVersion(String currentVersion) {
            this.currentVersion = currentVersion;
        }

        public int getBuildCount() {
            return buildCount;
        }

        public void setBuildCount(int buildCount) {
            this.buildCount = buildCount;
        }

        public String getLastBuildTime() {
            return lastBuildTime;
        }

        public void setLastBuildTime(String lastBuildTime) {
            this.lastBuildTime = lastBuildTime;
        }

        public String getBuildHistory() {
            return buildHistory;
        }

        public void setBuildHistory(String buildHistory) {
            this.buildHistory = buildHistory;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }
    }
}



