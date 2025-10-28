package com.honyrun.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 版本信息响应DTO
 *
 * 用于返回系统版本信息，包含版本号、构建信息、变更日志等
 * 支持响应式数据传输和JSON序列化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  17:20:00
 * @modified 2025-07-01 17:20:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class VersionInfo {

    // ==================== 基本版本信息 ====================

    /**
     * 版本号
     * 格式：主版本.次版本.修订版本 (如: 2.0.0)
     */
    private String version;

    /**
     * 版本名称
     */
    private String versionName;

    /**
     * 版本描述
     */
    private String description;

    /**
     * 版本类型
     * RELEASE - 正式版, BETA - 测试版, ALPHA - 内测版, SNAPSHOT - 快照版
     */
    private String versionType;

    /**
     * 是否为最新版本
     */
    private Boolean isLatest;

    // ==================== 构建信息 ====================

    /**
     * 构建版本号（8位数字）
     */
    private String buildVersion;

    /**
     * 构建时间
     */
    private LocalDateTime buildTime;

    /**
     * 构建分支
     */
    private String buildBranch;

    /**
     * 构建提交ID
     */
    private String buildCommit;

    /**
     * 构建环境
     */
    private String buildEnvironment;

    /**
     * 构建者
     */
    private String buildBy;

    // ==================== 技术信息 ====================

    /**
     * Java版本
     */
    private String javaVersion;

    /**
     * Spring Boot版本
     */
    private String springBootVersion;

    /**
     * Spring WebFlux版本
     */
    private String springWebFluxVersion;

    /**
     * 项目架构
     */
    private String architecture;

    // ==================== 发布信息 ====================

    /**
     * 发布时间
     */
    private LocalDateTime releaseTime;

    /**
     * 发布者
     */
    private String releasedBy;

    /**
     * 发布说明
     */
    private String releaseNotes;

    /**
     * 变更日志
     */
    private List<ChangeLogEntry> changeLog;

    // ==================== 兼容性信息 ====================

    /**
     * 最低兼容版本
     */
    private String minimumCompatibleVersion;

    /**
     * 数据库版本要求
     */
    private String databaseVersionRequirement;

    /**
     * API版本
     */
    private String apiVersion;

    /**
     * 是否向后兼容
     */
    private Boolean backwardCompatible;

    // ==================== 内部类：变更日志条目 ====================

    /**
     * 变更日志条目
     */
    public static class ChangeLogEntry {
        /**
         * 变更类型
         * FEATURE - 新功能, BUGFIX - 错误修复, IMPROVEMENT - 改进, BREAKING - 破坏性变更
         */
        private String type;

        /**
         * 变更描述
         */
        private String description;

        /**
         * 变更作者
         */
        private String author;

        /**
         * 变更时间
         */
        private LocalDateTime changeTime;

        /**
         * 影响范围
         */
        private String scope;

        public ChangeLogEntry() {}

        public ChangeLogEntry(String type, String description) {
            this.type = type;
            this.description = description;
            this.changeTime = LocalDateTime.now();
        }

        // Getter和Setter方法
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public LocalDateTime getChangeTime() { return changeTime; }
        public void setChangeTime(LocalDateTime changeTime) { this.changeTime = changeTime; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
    }

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public VersionInfo() {
        this.architecture = "Spring WebFlux Reactive";
        this.apiVersion = "v1";
        this.backwardCompatible = true;
    }

    /**
     * 带版本号的构造函数
     *
     * @param version 版本号
     */
    public VersionInfo(String version) {
        this();
        this.version = version;
    }

    /**
     * 完整构造函数
     *
     * @param version 版本号
     * @param versionName 版本名称
     * @param buildVersion 构建版本号
     */
    public VersionInfo(String version, String versionName, String buildVersion) {
        this();
        this.version = version;
        this.versionName = versionName;
        this.buildVersion = buildVersion;
    }

    // ==================== Getter和Setter方法 ====================

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public Boolean getIsLatest() {
        return isLatest;
    }

    public void setIsLatest(Boolean isLatest) {
        this.isLatest = isLatest;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    public LocalDateTime getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(LocalDateTime buildTime) {
        this.buildTime = buildTime;
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

    public String getBuildEnvironment() {
        return buildEnvironment;
    }

    public void setBuildEnvironment(String buildEnvironment) {
        this.buildEnvironment = buildEnvironment;
    }

    public String getBuildBy() {
        return buildBy;
    }

    public void setBuildBy(String buildBy) {
        this.buildBy = buildBy;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getSpringBootVersion() {
        return springBootVersion;
    }

    public void setSpringBootVersion(String springBootVersion) {
        this.springBootVersion = springBootVersion;
    }

    public String getSpringWebFluxVersion() {
        return springWebFluxVersion;
    }

    public void setSpringWebFluxVersion(String springWebFluxVersion) {
        this.springWebFluxVersion = springWebFluxVersion;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public LocalDateTime getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(LocalDateTime releaseTime) {
        this.releaseTime = releaseTime;
    }

    public String getReleasedBy() {
        return releasedBy;
    }

    public void setReleasedBy(String releasedBy) {
        this.releasedBy = releasedBy;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public List<ChangeLogEntry> getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(List<ChangeLogEntry> changeLog) {
        this.changeLog = changeLog;
    }

    public String getMinimumCompatibleVersion() {
        return minimumCompatibleVersion;
    }

    public void setMinimumCompatibleVersion(String minimumCompatibleVersion) {
        this.minimumCompatibleVersion = minimumCompatibleVersion;
    }

    public String getDatabaseVersionRequirement() {
        return databaseVersionRequirement;
    }

    public void setDatabaseVersionRequirement(String databaseVersionRequirement) {
        this.databaseVersionRequirement = databaseVersionRequirement;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public Boolean getBackwardCompatible() {
        return backwardCompatible;
    }

    public void setBackwardCompatible(Boolean backwardCompatible) {
        this.backwardCompatible = backwardCompatible;
    }

    // ==================== 业务方法 ====================

    /**
     * 是否为正式版本
     *
     * @return 是否为正式版本
     */
    public boolean isReleaseVersion() {
        return "RELEASE".equals(versionType);
    }

    /**
     * 是否为测试版本
     *
     * @return 是否为测试版本
     */
    public boolean isBetaVersion() {
        return "BETA".equals(versionType);
    }

    /**
     * 是否为快照版本
     *
     * @return 是否为快照版本
     */
    public boolean isSnapshotVersion() {
        return "SNAPSHOT".equals(versionType);
    }

    /**
     * 获取完整版本字符串
     *
     * @return 完整版本字符串
     */
    public String getFullVersionString() {
        StringBuilder sb = new StringBuilder();
        if (version != null) {
            sb.append(version);
        }
        if (versionType != null && !"RELEASE".equals(versionType)) {
            sb.append("-").append(versionType);
        }
        if (buildVersion != null) {
            sb.append(" (").append(buildVersion).append(")");
        }
        return sb.toString();
    }

    /**
     * 获取版本年龄（天数）
     *
     * @return 版本年龄（天数）
     */
    public Long getVersionAge() {
        if (releaseTime == null) {
            return null;
        }
        return java.time.Duration.between(releaseTime, LocalDateTime.now()).toDays();
    }

    /**
     * 添加变更日志条目
     *
     * @param type 变更类型
     * @param description 变更描述
     */
    public void addChangeLogEntry(String type, String description) {
        if (changeLog == null) {
            changeLog = new java.util.ArrayList<>();
        }
        changeLog.add(new ChangeLogEntry(type, description));
    }

    /**
     * 获取新功能数量
     *
     * @return 新功能数量
     */
    public long getFeatureCount() {
        if (changeLog == null) {
            return 0;
        }
        return changeLog.stream()
                .filter(entry -> "FEATURE".equals(entry.getType()))
                .count();
    }

    /**
     * 获取错误修复数量
     *
     * @return 错误修复数量
     */
    public long getBugfixCount() {
        if (changeLog == null) {
            return 0;
        }
        return changeLog.stream()
                .filter(entry -> "BUGFIX".equals(entry.getType()))
                .count();
    }

    /**
     * 是否有破坏性变更
     *
     * @return 是否有破坏性变更
     */
    public boolean hasBreakingChanges() {
        if (changeLog == null) {
            return false;
        }
        return changeLog.stream()
                .anyMatch(entry -> "BREAKING".equals(entry.getType()));
    }

    // ==================== Object方法重写 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VersionInfo that = (VersionInfo) obj;
        return Objects.equals(version, that.version) &&
               Objects.equals(buildVersion, that.buildVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, buildVersion);
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "version='" + version + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionType='" + versionType + '\'' +
                ", buildVersion='" + buildVersion + '\'' +
                ", buildTime=" + buildTime +
                ", architecture='" + architecture + '\'' +
                ", isLatest=" + isLatest +
                '}';
    }
}


