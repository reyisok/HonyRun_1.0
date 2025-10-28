package com.honyrun.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * 版本迁移指南响应DTO
 * 
 * 提供从一个API版本迁移到另一个版本的详细指导信息
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-01-11 14:40:00
 * @modified 2025-01-11 14:40:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class VersionMigrationGuide {

    /**
     * 源版本
     */
    @JsonProperty("from_version")
    private String fromVersion;

    /**
     * 目标版本
     */
    @JsonProperty("to_version")
    private String toVersion;

    /**
     * 迁移标题
     */
    @JsonProperty("title")
    private String title;

    /**
     * 迁移描述
     */
    @JsonProperty("description")
    private String description;

    /**
     * 迁移步骤
     */
    @JsonProperty("migration_steps")
    private List<MigrationStep> migrationSteps;

    /**
     * 破坏性变更
     */
    @JsonProperty("breaking_changes")
    private List<BreakingChange> breakingChanges;

    /**
     * 新增功能
     */
    @JsonProperty("new_features")
    private List<String> newFeatures;

    /**
     * 弃用功能
     */
    @JsonProperty("deprecated_features")
    private List<String> deprecatedFeatures;

    /**
     * API映射表
     */
    @JsonProperty("api_mappings")
    private Map<String, String> apiMappings;

    /**
     * 代码示例
     */
    @JsonProperty("code_examples")
    private List<CodeExample> codeExamples;

    /**
     * 迁移工具
     */
    @JsonProperty("migration_tools")
    private List<String> migrationTools;

    /**
     * 预估迁移时间
     */
    @JsonProperty("estimated_migration_time")
    private String estimatedMigrationTime;

    /**
     * 迁移复杂度
     */
    @JsonProperty("complexity_level")
    private String complexityLevel;

    /**
     * 相关文档链接
     */
    @JsonProperty("documentation_links")
    private List<String> documentationLinks;

    // 内部类：迁移步骤
    public static class MigrationStep {
        @JsonProperty("step_number")
        private Integer stepNumber;

        @JsonProperty("title")
        private String title;

        @JsonProperty("description")
        private String description;

        @JsonProperty("required")
        private Boolean required;

        @JsonProperty("estimated_time")
        private String estimatedTime;

        // 构造函数和getter/setter
        public MigrationStep() {}

        public MigrationStep(Integer stepNumber, String title, String description, Boolean required) {
            this.stepNumber = stepNumber;
            this.title = title;
            this.description = description;
            this.required = required;
        }

        public Integer getStepNumber() { return stepNumber; }
        public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Boolean getRequired() { return required; }
        public void setRequired(Boolean required) { this.required = required; }
        public String getEstimatedTime() { return estimatedTime; }
        public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }
    }

    // 内部类：破坏性变更
    public static class BreakingChange {
        @JsonProperty("endpoint")
        private String endpoint;

        @JsonProperty("change_type")
        private String changeType;

        @JsonProperty("description")
        private String description;

        @JsonProperty("migration_action")
        private String migrationAction;

        // 构造函数和getter/setter
        public BreakingChange() {}

        public BreakingChange(String endpoint, String changeType, String description, String migrationAction) {
            this.endpoint = endpoint;
            this.changeType = changeType;
            this.description = description;
            this.migrationAction = migrationAction;
        }

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getChangeType() { return changeType; }
        public void setChangeType(String changeType) { this.changeType = changeType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getMigrationAction() { return migrationAction; }
        public void setMigrationAction(String migrationAction) { this.migrationAction = migrationAction; }
    }

    // 内部类：代码示例
    public static class CodeExample {
        @JsonProperty("title")
        private String title;

        @JsonProperty("old_code")
        private String oldCode;

        @JsonProperty("new_code")
        private String newCode;

        @JsonProperty("language")
        private String language;

        // 构造函数和getter/setter
        public CodeExample() {}

        public CodeExample(String title, String oldCode, String newCode, String language) {
            this.title = title;
            this.oldCode = oldCode;
            this.newCode = newCode;
            this.language = language;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getOldCode() { return oldCode; }
        public void setOldCode(String oldCode) { this.oldCode = oldCode; }
        public String getNewCode() { return newCode; }
        public void setNewCode(String newCode) { this.newCode = newCode; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    // 构造函数
    public VersionMigrationGuide() {}

    public VersionMigrationGuide(String fromVersion, String toVersion, String title, String description) {
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.title = title;
        this.description = description;
    }

    // Getter和Setter方法
    public String getFromVersion() { return fromVersion; }
    public void setFromVersion(String fromVersion) { this.fromVersion = fromVersion; }
    public String getToVersion() { return toVersion; }
    public void setToVersion(String toVersion) { this.toVersion = toVersion; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<MigrationStep> getMigrationSteps() { return migrationSteps; }
    public void setMigrationSteps(List<MigrationStep> migrationSteps) { this.migrationSteps = migrationSteps; }
    public List<BreakingChange> getBreakingChanges() { return breakingChanges; }
    public void setBreakingChanges(List<BreakingChange> breakingChanges) { this.breakingChanges = breakingChanges; }
    public List<String> getNewFeatures() { return newFeatures; }
    public void setNewFeatures(List<String> newFeatures) { this.newFeatures = newFeatures; }
    public List<String> getDeprecatedFeatures() { return deprecatedFeatures; }
    public void setDeprecatedFeatures(List<String> deprecatedFeatures) { this.deprecatedFeatures = deprecatedFeatures; }
    public Map<String, String> getApiMappings() { return apiMappings; }
    public void setApiMappings(Map<String, String> apiMappings) { this.apiMappings = apiMappings; }
    public List<CodeExample> getCodeExamples() { return codeExamples; }
    public void setCodeExamples(List<CodeExample> codeExamples) { this.codeExamples = codeExamples; }
    public List<String> getMigrationTools() { return migrationTools; }
    public void setMigrationTools(List<String> migrationTools) { this.migrationTools = migrationTools; }
    public String getEstimatedMigrationTime() { return estimatedMigrationTime; }
    public void setEstimatedMigrationTime(String estimatedMigrationTime) { this.estimatedMigrationTime = estimatedMigrationTime; }
    public String getComplexityLevel() { return complexityLevel; }
    public void setComplexityLevel(String complexityLevel) { this.complexityLevel = complexityLevel; }
    public List<String> getDocumentationLinks() { return documentationLinks; }
    public void setDocumentationLinks(List<String> documentationLinks) { this.documentationLinks = documentationLinks; }

    @Override
    public String toString() {
        return "VersionMigrationGuide{" +
                "fromVersion='" + fromVersion + '\'' +
                ", toVersion='" + toVersion + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", complexityLevel='" + complexityLevel + '\'' +
                ", estimatedMigrationTime='" + estimatedMigrationTime + '\'' +
                '}';
    }
}
