package com.honyrun.util.version;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 编译版本工具类
 *
 * 处理编译版本信息和构建版本，支持8位编译版本号的生成和管理
 * 提供构建信息的读取、更新和验证功能
 *
 * 主要功能：
 * - 8位编译版本号生成
 * - 构建信息读取和管理
 * - 编译时间戳处理
 * - 构建环境信息获取
 * - 版本文件操作
 *
 * 编译版本规范：
 * - 8位编译版本号：YYYYMMDD格式
 * - 构建时间戳：精确到秒的构建时间
 * - 构建环境：开发、测试、生产环境标识
 * - 构建标识：唯一的构建标识符
 *
 * 响应式特性：
 * - 非阻塞操作：所有版本操作均为非阻塞
 * - 缓存优化：构建信息支持缓存
 * - 错误处理：构建信息读取失败时的恢复机制
 * - 异步更新：支持构建信息的异步更新
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 00:35:00
 * @modified 2025-07-01 00:35:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class BuildVersionUtil {

    private static final Logger logger = LoggerFactory.getLogger(BuildVersionUtil.class);

    /**
     * 8位编译版本号格式
     */
    private static final DateTimeFormatter BUILD_VERSION_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 构建时间戳格式
     */
    private static final DateTimeFormatter BUILD_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 构建版本属性文件名
     */
    private static final String BUILD_VERSION_PROPERTIES = "build-version.properties";

    /**
     * 构建信息缓存
     */
    private final AtomicReference<BuildInfo> buildInfoCache = new AtomicReference<>();
    private final Environment environment;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数
     *
     * @param environment 环境配置
     * @param unifiedConfigManager 统一配置管理器
     */
    public BuildVersionUtil(Environment environment, UnifiedConfigManager unifiedConfigManager) {
        this.environment = environment;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 生成当前8位编译版本号
     *
     * @return 8位编译版本号的Mono
     */
    public Mono<String> generateCurrentBuildVersion() {
        LoggingUtil.debug(logger, "生成当前8位编译版本号");

        return Mono.fromCallable(() -> {
            String buildVersion = LocalDateTime.now().format(BUILD_VERSION_FORMAT);
            LoggingUtil.info(logger, "生成8位编译版本号: {}", buildVersion);
            return buildVersion;
        })
        .doOnError(error -> LoggingUtil.error(logger, "生成编译版本号失败", error));
    }

    /**
     * 生成指定日期的8位编译版本号
     *
     * @param dateTime 指定日期时间
     * @return 8位编译版本号的Mono
     */
    public Mono<String> generateBuildVersion(LocalDateTime dateTime) {
        LoggingUtil.debug(logger, "生成指定日期的8位编译版本号: {}", dateTime);

        return Mono.fromCallable(() -> {
            String buildVersion = dateTime.format(BUILD_VERSION_FORMAT);
            LoggingUtil.debug(logger, "生成指定日期编译版本号: {}", buildVersion);
            return buildVersion;
        })
        .doOnError(error -> LoggingUtil.error(logger, "生成指定日期编译版本号失败", error));
    }

    /**
     * 获取构建信息
     *
     * @return 构建信息的Mono
     */
    public Mono<BuildInfo> getBuildInfo() {
        LoggingUtil.debug(logger, "获取构建信息");

        return Mono.fromCallable(() -> {
            BuildInfo cachedInfo = buildInfoCache.get();
            if (cachedInfo != null) {
                LoggingUtil.debug(logger, "从缓存获取构建信息");
                return cachedInfo;
            }

            BuildInfo buildInfo = loadBuildInfoFromProperties();
            buildInfoCache.set(buildInfo);

            LoggingUtil.info(logger, "加载构建信息: 版本={}, 时间={}",
                    buildInfo.getBuildVersion(), buildInfo.getBuildTimestamp());
            return buildInfo;
        })
        .doOnError(error -> LoggingUtil.error(logger, "获取构建信息失败", error))
        .onErrorResume(error -> {
            LoggingUtil.error(logger, "构建信息获取错误，返回默认构建信息", error);
            return Mono.just(createDefaultBuildInfo());
        });
    }

    /**
     * 更新构建信息
     *
     * @param buildInfo 新构建信息
     * @return 更新结果的Mono
     */
    public Mono<BuildInfo> updateBuildInfo(BuildInfo buildInfo) {
        LoggingUtil.info(logger, "更新构建信息: {}", buildInfo.getBuildVersion());

        return Mono.fromCallable(() -> {
            // 验证构建信息
            validateBuildInfo(buildInfo);

            // 更新缓存
            buildInfoCache.set(buildInfo);

            // 这里可以添加持久化逻辑
            LoggingUtil.info(logger, "构建信息更新成功");
            return buildInfo;
        })
        .doOnError(error -> LoggingUtil.error(logger, "更新构建信息失败", error));
    }

    /**
     * 验证8位编译版本号格式
     *
     * @param buildVersion 编译版本号
     * @return 验证结果的Mono
     */
    public Mono<Boolean> validateBuildVersionFormat(String buildVersion) {
        LoggingUtil.debug(logger, "验证8位编译版本号格式: {}", buildVersion);

        return Mono.fromCallable(() -> {
            if (buildVersion == null || buildVersion.length() != 8) {
                LoggingUtil.warn(logger, "编译版本号长度不正确: {}", buildVersion);
                return false;
            }

            try {
                // 尝试解析为日期
                LocalDateTime.parse(buildVersion + "0000", DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
                LoggingUtil.debug(logger, "编译版本号格式验证通过: {}", buildVersion);
                return true;
            } catch (Exception e) {
                LoggingUtil.warn(logger, "编译版本号格式验证失败: {}", buildVersion, e);
                return false;
            }
        })
        .doOnError(error -> LoggingUtil.error(logger, "验证编译版本号格式失败", error));
    }

    /**
     * 比较编译版本号
     *
     * @param version1 版本1
     * @param version2 版本2
     * @return 比较结果的Mono（-1: version1 < version2, 0: 相等, 1: version1 > version2）
     */
    public Mono<Integer> compareBuildVersions(String version1, String version2) {
        LoggingUtil.debug(logger, "比较编译版本号: {} vs {}", version1, version2);

        return Mono.fromCallable(() -> {
            // 验证版本号格式
            if (!isValidBuildVersion(version1) || !isValidBuildVersion(version2)) {
                throw new IllegalArgumentException("无效的编译版本号格式");
            }

            int result = version1.compareTo(version2);
            LoggingUtil.debug(logger, "编译版本号比较结果: {}", result);
            return Integer.signum(result);
        })
        .doOnError(error -> LoggingUtil.error(logger, "比较编译版本号失败", error));
    }

    /**
     * 获取构建环境信息
     *
     * @return 构建环境信息的Mono
     */
    public Mono<BuildEnvironment> getBuildEnvironment() {
        LoggingUtil.debug(logger, "获取构建环境信息");

        return Mono.fromCallable(() -> {
            BuildEnvironment environment = new BuildEnvironment();

            // 获取系统属性
            environment.setJavaVersion(System.getProperty("java.version"));
            environment.setJavaVendor(System.getProperty("java.vendor"));
            environment.setOsName(System.getProperty("os.name"));
            environment.setOsVersion(System.getProperty("os.version"));
            environment.setOsArch(System.getProperty("os.arch"));
            environment.setUserName(System.getProperty("user.name"));
            environment.setUserDir(System.getProperty("user.dir"));

            // 获取环境变量
            String profile = System.getProperty("spring.profiles.active", "dev");
            environment.setProfile(profile);

            LoggingUtil.debug(logger, "构建环境信息获取完成: {}", profile);
            return environment;
        })
        .doOnError(error -> LoggingUtil.error(logger, "获取构建环境信息失败", error));
    }

    /**
     * 生成构建标识符
     *
     * @return 构建标识符的Mono
     */
    public Mono<String> generateBuildIdentifier() {
        LoggingUtil.debug(logger, "生成构建标识符");

        return Mono.fromCallable(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String identifier = "BUILD-" + timestamp;
            LoggingUtil.debug(logger, "生成构建标识符: {}", identifier);
            return identifier;
        })
        .doOnError(error -> LoggingUtil.error(logger, "生成构建标识符失败", error));
    }

    /**
     * 清理构建信息缓存
     *
     * @return 清理结果的Mono
     */
    public Mono<Void> clearBuildInfoCache() {
        LoggingUtil.info(logger, "清理构建信息缓存");

        return Mono.fromRunnable(() -> {
            buildInfoCache.set(null);
            LoggingUtil.info(logger, "构建信息缓存清理完成");
        })
        .then();
    }

    // ==================== 私有方法 ====================

    /**
     * 从属性文件加载构建信息
     */
    private BuildInfo loadBuildInfoFromProperties() {
        try {
            Properties props = new Properties();

            // 尝试从类路径加载属性文件
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(BUILD_VERSION_PROPERTIES)) {
                if (inputStream != null) {
                    props.load(inputStream);
                    LoggingUtil.debug(logger, "成功加载构建版本属性文件");
                } else {
                    LoggingUtil.warn(logger, "构建版本属性文件不存在，使用默认值");
                }
            }

            BuildInfo buildInfo = new BuildInfo();
            buildInfo.setBuildVersion(props.getProperty("build.version", generateCurrentBuildVersionSync()));
            buildInfo.setBuildTimestamp(props.getProperty("build.timestamp", LocalDateTime.now().format(BUILD_TIMESTAMP_FORMAT)));
            buildInfo.setBuildNumber(props.getProperty("build.number", "1"));
            buildInfo.setBuildBranch(props.getProperty("build.branch", "main"));
            buildInfo.setBuildCommit(props.getProperty("build.commit", "unknown"));
            buildInfo.setBuildUser(props.getProperty("build.user", System.getProperty("user.name")));
            buildInfo.setBuildMachine(props.getProperty("build.machine", 
                unifiedConfigManager.getProperty("HONYRUN_BUILD_MACHINE", "localhost")));

            return buildInfo;
        } catch (IOException e) {
            LoggingUtil.error(logger, "加载构建版本属性文件失败", e);
            return createDefaultBuildInfo();
        }
    }

    /**
     * 创建默认构建信息
     */
    private BuildInfo createDefaultBuildInfo() {
        BuildInfo buildInfo = new BuildInfo();
        buildInfo.setBuildVersion(generateCurrentBuildVersionSync());
        buildInfo.setBuildTimestamp(LocalDateTime.now().format(BUILD_TIMESTAMP_FORMAT));
        buildInfo.setBuildNumber("1");
        buildInfo.setBuildBranch("main");
        buildInfo.setBuildCommit("unknown");
        buildInfo.setBuildUser(System.getProperty("user.name", "unknown"));
        buildInfo.setBuildMachine(unifiedConfigManager.getProperty("HONYRUN_BUILD_MACHINE", "localhost"));
        return buildInfo;
    }

    /**
     * 同步生成当前编译版本号
     */
    private String generateCurrentBuildVersionSync() {
        return LocalDateTime.now().format(BUILD_VERSION_FORMAT);
    }

    /**
     * 验证构建信息
     */
    private void validateBuildInfo(BuildInfo buildInfo) {
        if (buildInfo == null) {
            throw new IllegalArgumentException("构建信息不能为空");
        }
        if (!isValidBuildVersion(buildInfo.getBuildVersion())) {
            throw new IllegalArgumentException("无效的编译版本号格式: " + buildInfo.getBuildVersion());
        }
    }

    /**
     * 检查是否为有效的编译版本号
     */
    private boolean isValidBuildVersion(String buildVersion) {
        if (buildVersion == null || buildVersion.length() != 8) {
            return false;
        }

        try {
            LocalDateTime.parse(buildVersion + "0000", DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 内部类 ====================

    /**
     * 构建信息类
     */
    public static class BuildInfo {
        private String buildVersion;
        private String buildTimestamp;
        private String buildNumber;
        private String buildBranch;
        private String buildCommit;
        private String buildUser;
        private String buildMachine;

        // Getters and Setters
        public String getBuildVersion() {
            return buildVersion;
        }

        public void setBuildVersion(String buildVersion) {
            this.buildVersion = buildVersion;
        }

        public String getBuildTimestamp() {
            return buildTimestamp;
        }

        public void setBuildTimestamp(String buildTimestamp) {
            this.buildTimestamp = buildTimestamp;
        }

        public String getBuildNumber() {
            return buildNumber;
        }

        public void setBuildNumber(String buildNumber) {
            this.buildNumber = buildNumber;
        }

        public String getBuildBranch() {
            return buildBranch;
        }

        public void setBuildBranch(String buildBranch) {
            this.buildBranch = buildBranch;
        }

        public String getBuildCommit() {
            return buildCommit;
        }

        public void setBuildCommit(String buildCommit) {
            this.buildCommit = buildCommit;
        }

        public String getBuildUser() {
            return buildUser;
        }

        public void setBuildUser(String buildUser) {
            this.buildUser = buildUser;
        }

        public String getBuildMachine() {
            return buildMachine;
        }

        public void setBuildMachine(String buildMachine) {
            this.buildMachine = buildMachine;
        }

        @Override
        public String toString() {
            return String.format("BuildInfo{version='%s', timestamp='%s', number='%s', branch='%s', commit='%s', user='%s', machine='%s'}",
                    buildVersion, buildTimestamp, buildNumber, buildBranch, buildCommit, buildUser, buildMachine);
        }
    }

    /**
     * 构建环境类
     */
    public static class BuildEnvironment {
        private String javaVersion;
        private String javaVendor;
        private String osName;
        private String osVersion;
        private String osArch;
        private String userName;
        private String userDir;
        private String profile;

        // Getters and Setters
        public String getJavaVersion() {
            return javaVersion;
        }

        public void setJavaVersion(String javaVersion) {
            this.javaVersion = javaVersion;
        }

        public String getJavaVendor() {
            return javaVendor;
        }

        public void setJavaVendor(String javaVendor) {
            this.javaVendor = javaVendor;
        }

        public String getOsName() {
            return osName;
        }

        public void setOsName(String osName) {
            this.osName = osName;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        public String getOsArch() {
            return osArch;
        }

        public void setOsArch(String osArch) {
            this.osArch = osArch;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserDir() {
            return userDir;
        }

        public void setUserDir(String userDir) {
            this.userDir = userDir;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }

        @Override
        public String toString() {
            return String.format("BuildEnvironment{java='%s %s', os='%s %s %s', user='%s', dir='%s', profile='%s'}",
                    javaVersion, javaVendor, osName, osVersion, osArch, userName, userDir, profile);
        }
    }
}

