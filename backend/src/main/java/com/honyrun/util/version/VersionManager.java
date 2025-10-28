package com.honyrun.util.version;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 版本管理器
 *
 * 提供8位编译版本号管理功能，支持版本信息的创建、查询和更新
 * 采用响应式编程模型，支持非阻塞的版本管理操作
 *
 * 主要功能：
 * - 8位编译版本号生成和管理
 * - 版本信息查询和更新
 * - 版本兼容性检查
 * - 版本历史记录管理
 * - 版本升级和回滚
 *
 * 版本号规范：
 * - 主版本号：X.Y.Z格式（如2.0.0）
 * - 编译版本号：8位数字格式YYYYMMDD（如20250128）
 * - 完整版本：主版本号-编译版本号（如2.0.0-20250128）
 *
 * 响应式特性：
 * - 非阻塞操作：版本管理操作均为非阻塞
 * - 原子更新：版本信息更新采用原子操作
 * - 错误恢复：版本操作失败时提供恢复机制
 * - 缓存支持：版本信息支持缓存优化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 00:30:00
 * @modified 2025-07-01 00:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class VersionManager {

    private static final Logger logger = LoggerFactory.getLogger(VersionManager.class);

    /**
     * 8位编译版本号格式
     */
    private static final DateTimeFormatter BUILD_VERSION_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 当前版本信息缓存
     */
    private final AtomicReference<VersionInfo> currentVersionCache = new AtomicReference<>();

    /**
     * 获取当前版本信息
     *
     * @return 当前版本信息的Mono
     */
    public Mono<VersionInfo> getCurrentVersion() {
        LoggingUtil.debug(logger, "获取当前版本信息");

        return Mono.fromCallable(() -> {
            VersionInfo cachedVersion = currentVersionCache.get();
            if (cachedVersion != null) {
                LoggingUtil.debug(logger, "从缓存获取版本信息: {}", cachedVersion.getFullVersion());
                return cachedVersion;
            }

            // 从配置文件或系统属性加载版本信息
            VersionInfo versionInfo = loadVersionFromProperties();
            currentVersionCache.set(versionInfo);

            LoggingUtil.info(logger, "加载版本信息: {}", versionInfo.getFullVersion());
            return versionInfo;
        })
        .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()) // 在弹性线程池中执行文件I/O操作
        .doOnError(error -> LoggingUtil.error(logger, "获取当前版本信息失败", error))
        .onErrorResume(error -> {
            LoggingUtil.error(logger, "版本信息获取错误，返回默认版本", error);
            return Mono.just(createDefaultVersion());
        });
    }

    /**
     * 获取当前版本字符串
     *
     * @return 当前版本字符串
     */
    public String getCurrentVersionString() {
        LoggingUtil.debug(logger, "获取当前版本字符串");

        try {
            VersionInfo versionInfo = currentVersionCache.get();
            if (versionInfo != null && versionInfo.getFullVersion() != null) {
                String version = versionInfo.getFullVersion();
                LoggingUtil.debug(logger, "获取当前版本: {}", version);
                return version;
            }

            // 如果缓存中没有版本信息，返回默认版本
            String defaultVersion = "2.0.0";
            LoggingUtil.debug(logger, "使用默认版本: {}", defaultVersion);
            return defaultVersion;
        } catch (RuntimeException e) {
            LoggingUtil.error(logger, "获取当前版本失败", e);
            return "2.0.0";
        }
    }

    /**
     * 获取构建时间
     *
     * @return 构建时间字符串
     */
    public String getBuildTime() {
        LoggingUtil.debug(logger, "获取构建时间");

        try {
            VersionInfo versionInfo = currentVersionCache.get();
            if (versionInfo != null && versionInfo.getCreateTime() != null) {
                String buildTime = versionInfo.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                LoggingUtil.debug(logger, "获取构建时间: {}", buildTime);
                return buildTime;
            }

            // 如果缓存中没有版本信息，返回默认构建时间
            String defaultBuildTime = "2025-07-01T10:00:00";
            LoggingUtil.debug(logger, "使用默认构建时间: {}", defaultBuildTime);
            return defaultBuildTime;
        } catch (RuntimeException e) {
            LoggingUtil.error(logger, "获取构建时间失败", e);
            return "2025-07-01T10:00:00";
        }
    }

    /**
     * 生成新的编译版本号
     *
     * @return 新编译版本号的Mono
     */
    public Mono<String> generateBuildVersion() {
        LoggingUtil.debug(logger, "生成新的编译版本号");

        return Mono.fromCallable(() -> {
            String buildVersion = LocalDateTime.now().format(BUILD_VERSION_FORMAT);
            LoggingUtil.info(logger, "生成编译版本号: {}", buildVersion);
            return buildVersion;
        })
        .doOnError(error -> LoggingUtil.error(logger, "生成编译版本号失败", error));
    }

    /**
     * 创建新版本信息
     *
     * @param majorVersion 主版本号
     * @param minorVersion 次版本号
     * @param patchVersion 修订版本号
     * @return 新版本信息的Mono
     */
    public Mono<VersionInfo> createVersion(int majorVersion, int minorVersion, int patchVersion) {
        LoggingUtil.info(logger, "创建新版本信息: {}.{}.{}", majorVersion, minorVersion, patchVersion);

        return generateBuildVersion()
                .map(buildVersion -> {
                    VersionInfo versionInfo = new VersionInfo();
                    versionInfo.setMajorVersion(majorVersion);
                    versionInfo.setMinorVersion(minorVersion);
                    versionInfo.setPatchVersion(patchVersion);
                    versionInfo.setBuildVersion(buildVersion);
                    versionInfo.setReleaseVersion(String.format("%d.%d.%d", majorVersion, minorVersion, patchVersion));
                    versionInfo.setFullVersion(String.format("%d.%d.%d-%s", majorVersion, minorVersion, patchVersion, buildVersion));
                    versionInfo.setCreateTime(LocalDateTime.now());

                    LoggingUtil.info(logger, "创建版本信息成功: {}", versionInfo.getFullVersion());
                    return versionInfo;
                })
                .doOnError(error -> LoggingUtil.error(logger, "创建版本信息失败", error));
    }

    /**
     * 更新版本信息
     *
     * @param versionInfo 新版本信息
     * @return 更新结果的Mono
     */
    public Mono<VersionInfo> updateVersion(VersionInfo versionInfo) {
        return Mono.fromCallable(() -> {
            // 验证版本信息
            validateVersionInfo(versionInfo);
            
            LoggingUtil.info(logger, "更新版本信息: {}", versionInfo.getFullVersion());

            // 更新缓存
            currentVersionCache.set(versionInfo);

            // 更新版本属性文件
            updateVersionProperties(versionInfo);

            LoggingUtil.info(logger, "版本信息更新成功: {}", versionInfo.getFullVersion());
            return versionInfo;
        })
        .doOnError(error -> LoggingUtil.error(logger, "更新版本信息失败", error))
        .onErrorResume(error -> {
            LoggingUtil.error(logger, "版本信息更新错误", error);
            return Mono.error(new VersionException("版本信息更新失败: " + error.getMessage()));
        });
    }

    /**
     * 检查版本兼容性
     *
     * @param targetVersion 目标版本
     * @param currentVersion 当前版本
     * @return 兼容性检查结果的Mono
     */
    public Mono<VersionCompatibility> checkCompatibility(String targetVersion, String currentVersion) {
        LoggingUtil.debug(logger, "检查版本兼容性，目标版本: {}, 当前版本: {}", targetVersion, currentVersion);

        return Mono.fromCallable(() -> {
            VersionCompatibility compatibility = new VersionCompatibility();
            compatibility.setTargetVersion(targetVersion);
            compatibility.setCurrentVersion(currentVersion);

            // 解析版本号
            Version target = parseVersion(targetVersion);
            Version current = parseVersion(currentVersion);

            // 检查兼容性
            boolean isCompatible = checkVersionCompatibility(target, current);
            compatibility.setCompatible(isCompatible);

            // 设置兼容性说明
            if (isCompatible) {
                compatibility.setReason("版本兼容");
            } else {
                compatibility.setReason(generateIncompatibilityReason(target, current));
            }

            LoggingUtil.info(logger, "版本兼容性检查完成，结果: {}", isCompatible);
            return compatibility;
        })
        .doOnError(error -> LoggingUtil.error(logger, "版本兼容性检查失败", error));
    }

    /**
     * 比较版本号
     *
     * @param version1 版本1
     * @param version2 版本2
     * @return 比较结果的Mono（-1: version1 < version2, 0: 相等, 1: version1 > version2）
     */
    public Mono<Integer> compareVersions(String version1, String version2) {
        LoggingUtil.debug(logger, "比较版本号: {} vs {}", version1, version2);

        return Mono.fromCallable(() -> {
            Version v1 = parseVersion(version1);
            Version v2 = parseVersion(version2);

            int result = compareVersion(v1, v2);
            LoggingUtil.debug(logger, "版本比较结果: {}", result);
            return result;
        })
        .doOnError(error -> LoggingUtil.error(logger, "版本比较失败", error));
    }

    /**
     * 获取版本历史
     *
     * @return 版本历史列表的Mono
     */
    public Mono<java.util.List<VersionInfo>> getVersionHistory() {
        LoggingUtil.debug(logger, "获取版本历史");

        return Mono.fromCallable(() -> {
            // 这里应该从数据库或文件系统加载版本历史
            // 暂时返回当前版本作为示例
            java.util.List<VersionInfo> history = new java.util.ArrayList<>();
            VersionInfo currentVersion = currentVersionCache.get();
            if (currentVersion != null) {
                history.add(currentVersion);
            }

            LoggingUtil.debug(logger, "获取版本历史完成，数量: {}", history.size());
            return history;
        })
        .doOnError(error -> LoggingUtil.error(logger, "获取版本历史失败", error));
    }

    /**
     * 清理版本缓存
     *
     * @return 清理结果的Mono
     */
    public Mono<Void> clearVersionCache() {
        LoggingUtil.info(logger, "清理版本缓存");

        return Mono.fromRunnable(() -> {
            currentVersionCache.set(null);
            LoggingUtil.info(logger, "版本缓存清理完成");
        })
        .then();
    }

    // ==================== 私有方法 ====================

    /**
     * 从配置文件加载版本信息
     * 注意：此方法包含同步I/O操作，必须在Mono.fromCallable()中调用并使用弹性线程池
     * 
     * @return 版本信息对象
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-27 12:26:43
     * @modified 2025-10-27 12:26:43
     * @version 1.0.0
     */
    private VersionInfo loadVersionFromProperties() {
        try {
            Properties props = new Properties();
            // 尝试加载build-version.properties文件
            // 注意：getResourceAsStream和props.load是同步I/O操作，会阻塞当前线程
            // 这些操作必须在弹性线程池中执行，以避免阻塞响应式线程
            try (var inputStream = getClass().getClassLoader().getResourceAsStream("build-version.properties")) {
                if (inputStream != null) {
                    props.load(inputStream); // 阻塞I/O操作 - 必须在弹性线程池中执行
                    LoggingUtil.debug(logger, "成功加载版本配置文件");
                } else {
                    LoggingUtil.debug(logger, "版本配置文件不存在，使用默认配置");
                }
            }

            VersionInfo versionInfo = new VersionInfo();
            versionInfo.setMajorVersion(Integer.parseInt(props.getProperty("version.major", "2")));
            versionInfo.setMinorVersion(Integer.parseInt(props.getProperty("version.minor", "0")));
            versionInfo.setPatchVersion(Integer.parseInt(props.getProperty("version.patch", "0")));
            versionInfo.setBuildVersion(props.getProperty("version.build", LocalDateTime.now().format(BUILD_VERSION_FORMAT)));
            versionInfo.setReleaseVersion(props.getProperty("version.release", "2.0.0"));
            versionInfo.setFullVersion(props.getProperty("version.full", "2.0.0-" + versionInfo.getBuildVersion()));
            versionInfo.setCreateTime(LocalDateTime.now());

            return versionInfo;
        } catch (IOException | NumberFormatException e) {
            LoggingUtil.error(logger, "加载版本配置文件失败", e);
            return createDefaultVersion();
        }
    }

    /**
     * 创建默认版本信息
     */
    private VersionInfo createDefaultVersion() {
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setMajorVersion(2);
        versionInfo.setMinorVersion(0);
        versionInfo.setPatchVersion(0);
        versionInfo.setBuildVersion(LocalDateTime.now().format(BUILD_VERSION_FORMAT));
        versionInfo.setReleaseVersion("2.0.0");
        versionInfo.setFullVersion("2.0.0-" + versionInfo.getBuildVersion());
        versionInfo.setCreateTime(LocalDateTime.now());
        return versionInfo;
    }

    /**
     * 验证版本信息
     */
    private void validateVersionInfo(VersionInfo versionInfo) {
        if (versionInfo == null) {
            throw new IllegalArgumentException("版本信息不能为空");
        }
        if (versionInfo.getMajorVersion() < 0 || versionInfo.getMinorVersion() < 0 || versionInfo.getPatchVersion() < 0) {
            throw new IllegalArgumentException("版本号不能为负数");
        }
        if (versionInfo.getBuildVersion() == null || versionInfo.getBuildVersion().length() != 8) {
            throw new IllegalArgumentException("编译版本号必须为8位数字");
        }
    }

    /**
     * 更新版本属性文件
     */
    private void updateVersionProperties(VersionInfo versionInfo) {
        // 这里应该更新build-version.properties文件
        // 暂时只记录日志
        LoggingUtil.info(logger, "更新版本属性文件: {}", versionInfo.getFullVersion());
    }

    /**
     * 解析版本字符串
     */
    private Version parseVersion(String versionString) {
        try {
            String[] parts = versionString.split("-");
            String[] versionParts = parts[0].split("\\.");

            Version version = new Version();
            version.setMajor(Integer.parseInt(versionParts[0]));
            version.setMinor(versionParts.length > 1 ? Integer.parseInt(versionParts[1]) : 0);
            version.setPatch(versionParts.length > 2 ? Integer.parseInt(versionParts[2]) : 0);
            version.setBuild(parts.length > 1 ? parts[1] : "");

            return version;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的版本格式: " + versionString, e);
        }
    }

    /**
     * 检查版本兼容性
     */
    private boolean checkVersionCompatibility(Version target, Version current) {
        // 主版本号不同时不兼容
        if (target.getMajor() != current.getMajor()) {
            return false;
        }

        // 次版本号向后兼容
        if (target.getMinor() > current.getMinor()) {
            return false;
        }

        return true;
    }

    /**
     * 生成不兼容原因
     */
    private String generateIncompatibilityReason(Version target, Version current) {
        if (target.getMajor() != current.getMajor()) {
            return "主版本号不兼容";
        }
        if (target.getMinor() > current.getMinor()) {
            return "次版本号不兼容";
        }
        return "版本不兼容";
    }

    /**
     * 比较版本
     */
    private int compareVersion(Version v1, Version v2) {
        int result = Integer.compare(v1.getMajor(), v2.getMajor());
        if (result != 0) return result;

        result = Integer.compare(v1.getMinor(), v2.getMinor());
        if (result != 0) return result;

        result = Integer.compare(v1.getPatch(), v2.getPatch());
        if (result != 0) return result;

        return v1.getBuild().compareTo(v2.getBuild());
    }

    // ==================== 内部类 ====================

    /**
     * 版本信息类
     */
    public static class VersionInfo {
        private int majorVersion;
        private int minorVersion;
        private int patchVersion;
        private String buildVersion;
        private String releaseVersion;
        private String fullVersion;
        private LocalDateTime createTime;

        // Getters and Setters
        public int getMajorVersion() {
            return majorVersion;
        }

        public void setMajorVersion(int majorVersion) {
            this.majorVersion = majorVersion;
        }

        public int getMinorVersion() {
            return minorVersion;
        }

        public void setMinorVersion(int minorVersion) {
            this.minorVersion = minorVersion;
        }

        public int getPatchVersion() {
            return patchVersion;
        }

        public void setPatchVersion(int patchVersion) {
            this.patchVersion = patchVersion;
        }

        public String getBuildVersion() {
            return buildVersion;
        }

        public void setBuildVersion(String buildVersion) {
            this.buildVersion = buildVersion;
        }

        public String getReleaseVersion() {
            return releaseVersion;
        }

        public void setReleaseVersion(String releaseVersion) {
            this.releaseVersion = releaseVersion;
        }

        public String getFullVersion() {
            return fullVersion;
        }

        public void setFullVersion(String fullVersion) {
            this.fullVersion = fullVersion;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }

    /**
     * 版本兼容性类
     */
    public static class VersionCompatibility {
        private String targetVersion;
        private String currentVersion;
        private boolean compatible;
        private String reason;

        // Getters and Setters
        public String getTargetVersion() {
            return targetVersion;
        }

        public void setTargetVersion(String targetVersion) {
            this.targetVersion = targetVersion;
        }

        public String getCurrentVersion() {
            return currentVersion;
        }

        public void setCurrentVersion(String currentVersion) {
            this.currentVersion = currentVersion;
        }

        public boolean isCompatible() {
            return compatible;
        }

        public void setCompatible(boolean compatible) {
            this.compatible = compatible;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /**
     * 版本类
     */
    private static class Version {
        private int major;
        private int minor;
        private int patch;
        private String build;

        // Getters and Setters
        public int getMajor() {
            return major;
        }

        public void setMajor(int major) {
            this.major = major;
        }

        public int getMinor() {
            return minor;
        }

        public void setMinor(int minor) {
            this.minor = minor;
        }

        public int getPatch() {
            return patch;
        }

        public void setPatch(int patch) {
            this.patch = patch;
        }

        public String getBuild() {
            return build;
        }

        public void setBuild(String build) {
            this.build = build;
        }
    }

    /**
     * 版本异常类
     */
    public static class VersionException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public VersionException(String message) {
            super(message);
        }

        public VersionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}


