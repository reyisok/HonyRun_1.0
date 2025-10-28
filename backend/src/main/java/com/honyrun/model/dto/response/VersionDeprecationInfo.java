package com.honyrun.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * 版本弃用信息响应DTO
 * 
 * 包含API版本弃用的详细信息，包括弃用原因、迁移指导和时间窗口
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-01-11 14:35:00
 * @modified 2025-01-11 14:35:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class VersionDeprecationInfo {

    /**
     * 版本号
     */
    @JsonProperty("version")
    private String version;

    /**
     * 是否已弃用
     */
    @JsonProperty("deprecated")
    private Boolean deprecated;

    /**
     * 弃用消息
     */
    @JsonProperty("deprecation_message")
    private String deprecationMessage;

    /**
     * 弃用时间
     */
    @JsonProperty("deprecated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deprecatedAt;

    /**
     * 迁移截止时间
     */
    @JsonProperty("migration_deadline")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime migrationDeadline;

    /**
     * 推荐迁移版本
     */
    @JsonProperty("recommended_version")
    private String recommendedVersion;

    /**
     * 迁移指导URL
     */
    @JsonProperty("migration_guide_url")
    private String migrationGuideUrl;

    /**
     * 弃用严重程度
     */
    @JsonProperty("severity")
    private String severity;

    /**
     * 影响范围
     */
    @JsonProperty("impact_scope")
    private String impactScope;

    /**
     * 替代方案
     */
    @JsonProperty("alternatives")
    private String alternatives;

    // 构造函数
    public VersionDeprecationInfo() {}

    public VersionDeprecationInfo(String version, Boolean deprecated, String deprecationMessage) {
        this.version = version;
        this.deprecated = deprecated;
        this.deprecationMessage = deprecationMessage;
    }

    // Getter和Setter方法
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getDeprecationMessage() {
        return deprecationMessage;
    }

    public void setDeprecationMessage(String deprecationMessage) {
        this.deprecationMessage = deprecationMessage;
    }

    public LocalDateTime getDeprecatedAt() {
        return deprecatedAt;
    }

    public void setDeprecatedAt(LocalDateTime deprecatedAt) {
        this.deprecatedAt = deprecatedAt;
    }

    public LocalDateTime getMigrationDeadline() {
        return migrationDeadline;
    }

    public void setMigrationDeadline(LocalDateTime migrationDeadline) {
        this.migrationDeadline = migrationDeadline;
    }

    public String getRecommendedVersion() {
        return recommendedVersion;
    }

    public void setRecommendedVersion(String recommendedVersion) {
        this.recommendedVersion = recommendedVersion;
    }

    public String getMigrationGuideUrl() {
        return migrationGuideUrl;
    }

    public void setMigrationGuideUrl(String migrationGuideUrl) {
        this.migrationGuideUrl = migrationGuideUrl;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getImpactScope() {
        return impactScope;
    }

    public void setImpactScope(String impactScope) {
        this.impactScope = impactScope;
    }

    public String getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(String alternatives) {
        this.alternatives = alternatives;
    }

    @Override
    public String toString() {
        return "VersionDeprecationInfo{" +
                "version='" + version + '\'' +
                ", deprecated=" + deprecated +
                ", deprecationMessage='" + deprecationMessage + '\'' +
                ", deprecatedAt=" + deprecatedAt +
                ", migrationDeadline=" + migrationDeadline +
                ", recommendedVersion='" + recommendedVersion + '\'' +
                ", migrationGuideUrl='" + migrationGuideUrl + '\'' +
                ", severity='" + severity + '\'' +
                ", impactScope='" + impactScope + '\'' +
                ", alternatives='" + alternatives + '\'' +
                '}';
    }
}
