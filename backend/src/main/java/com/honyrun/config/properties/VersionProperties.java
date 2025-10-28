package com.honyrun.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 版本配置属性类
 *
 * 用于管理应用程序版本相关的配置属性，支持从配置文件中读取版本信息。
 * 该类提供了版本号、构建信息、发布信息等配置的统一管理。
 *
 * 【重要】：所有配置值必须从环境配置文件中读取
 * 配置文件路径：
 * - 开发环境：application-dev.properties
 * - 测试环境：application-test.properties
 * - 生产环境：application-prod.properties
 *
 * 主要功能：
 * - 版本号配置管理
 * - 构建信息配置
 * - 发布信息配置
 * - 8位编译版本号配置
 *
 * 配置前缀：honyrun.app.version
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01 10:30:00
 * @modified 2025-10-27 12:26:43
 * @version 2.0.1 - 移除硬编码，严格遵循统一配置管理规范
 */
@Component
@ConfigurationProperties(prefix = "honyrun.app.version")
public class VersionProperties {

    /**
     * 应用程序主版本号
     * 必须从配置文件获取：honyrun.app.version.major
     */
    private int major;

    /**
     * 应用程序次版本号
     * 必须从配置文件获取：honyrun.app.version.minor
     */
    private int minor;

    /**
     * 应用程序修订版本号
     * 必须从配置文件获取：honyrun.app.version.patch
     */
    private int patch;

    /**
     * 构建版本号（8位格式：YYYYMMDD）
     * 必须从配置文件获取：honyrun.app.version.build
     */
    private String build;

    /**
     * 发布版本号
     * 必须从配置文件获取：honyrun.app.version.release
     */
    private String release;

    /**
     * 完整版本号
     * 必须从配置文件获取：honyrun.app.version.full
     */
    private String full;

    /**
     * 版本名称
     * 必须从配置文件获取：honyrun.app.version.name
     */
    private String name;

    /**
     * 版本描述
     * 必须从配置文件获取：honyrun.app.version.description
     */
    private String description;

    /**
     * 是否启用自动版本递增
     * 必须从配置文件获取：honyrun.app.version.auto-increment
     */
    private boolean autoIncrement;

    /**
     * 版本文件路径
     * 必须从配置文件获取：honyrun.app.version.version-file
     */
    private String versionFile;

    /**
     * 是否启用版本缓存
     * 必须从配置文件获取：honyrun.app.version.enable-cache
     */
    private boolean enableCache;

    /**
     * 缓存过期时间（秒）
     * 必须从配置文件获取：honyrun.app.version.cache-expire-seconds
     */
    private long cacheExpireSeconds;

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

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getFull() {
        return full;
    }

    public void setFull(String full) {
        this.full = full;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public String getVersionFile() {
        return versionFile;
    }

    public void setVersionFile(String versionFile) {
        this.versionFile = versionFile;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    public long getCacheExpireSeconds() {
        return cacheExpireSeconds;
    }

    public void setCacheExpireSeconds(long cacheExpireSeconds) {
        this.cacheExpireSeconds = cacheExpireSeconds;
    }

    /**
     * 获取语义化版本号
     *
     * @return 语义化版本号（如：2.0.0）
     */
    public String getSemanticVersion() {
        return String.format("%d.%d.%d", major, minor, patch);
    }

    /**
     * 获取完整版本号（包含构建版本）
     *
     * @return 完整版本号（如：2.0.0-20250128）
     */
    public String getFullVersionWithBuild() {
        return String.format("%s-%s", getSemanticVersion(), build);
    }

    @Override
    public String toString() {
        return String.format("VersionProperties{version=%s, build=%s, name='%s'}",
                getSemanticVersion(), build, name);
    }
}
