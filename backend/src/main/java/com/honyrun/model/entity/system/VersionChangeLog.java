package com.honyrun.model.entity.system;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 版本变更日志实体类
 *
 * 用于存储系统版本变更的详细日志信息，支持R2DBC响应式数据访问。
 * 该实体记录版本变更的具体内容、影响范围和相关信息。
 *
 * 特性：
 * - 支持R2DBC响应式数据访问
 * - 继承审计功能
 * - 变更类型分类
 * - 影响范围记录
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  16:20:00
 * @modified 2025-07-01 16:20:00
 * @version 2.0.0
 */
@Table("sys_version_change_log")
public class VersionChangeLog extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 关联的版本信息ID
     * 外键关联到version_info表
     */
    @Column("version_info_id")
    private Long versionInfoId;

    /**
     * 关联的编译版本ID
     * 外键关联到build_version表
     */
    @Column("build_version_id")
    private Long buildVersionId;

    /**
     * 变更序号
     * 同一版本内的变更序号
     */
    @Column("change_sequence")
    private Integer changeSequence;

    /**
     * 变更类型
     * FEATURE-新功能, BUGFIX-错误修复, IMPROVEMENT-改进优化, SECURITY-安全修复, BREAKING-破坏性变更
     */
    @Column("change_type")
    private ChangeType changeType;

    /**
     * 变更标题
     * 变更的简要标题
     */
    @Column("title")
    private String title;

    /**
     * 变更描述
     * 变更的详细描述
     */
    @Column("description")
    private String description;

    /**
     * 变更内容
     * 具体的变更内容
     */
    @Column("content")
    private String content;

    /**
     * 影响模块
     * 变更影响的系统模块
     */
    @Column("affected_modules")
    private String affectedModules;

    /**
     * 影响范围
     * MINOR-轻微, MODERATE-中等, MAJOR-重大, CRITICAL-关键
     */
    @Column("impact_level")
    private ImpactLevel impactLevel = ImpactLevel.MINOR;

    /**
     * 优先级
     * HIGH-高, MEDIUM-中, LOW-低
     */
    @Column("priority")
    private Priority priority = Priority.MEDIUM;

    /**
     * 变更原因
     * 进行此变更的原因
     */
    @Column("reason")
    private String reason;

    /**
     * 解决方案
     * 变更采用的解决方案
     */
    @Column("solution")
    private String solution;

    /**
     * 测试说明
     * 变更的测试说明
     */
    @Column("test_description")
    private String testDescription;

    /**
     * 回滚方案
     * 变更的回滚方案
     */
    @Column("rollback_plan")
    private String rollbackPlan;

    /**
     * 相关Issue编号
     * 关联的Issue或Bug编号
     */
    @Column("related_issues")
    private String relatedIssues;

    /**
     * 相关PR编号
     * 关联的Pull Request编号
     */
    @Column("related_prs")
    private String relatedPrs;

    /**
     * Git提交哈希
     * 相关的Git提交哈希
     */
    @Column("git_commit_hash")
    private String gitCommitHash;

    /**
     * 变更作者
     * 进行变更的开发者
     */
    @Column("author")
    private String author;

    /**
     * 审核者
     * 变更的审核者
     */
    @Column("reviewer")
    private String reviewer;

    /**
     * 变更时间
     * 变更的实际时间
     */
    @Column("change_time")
    private LocalDateTime changeTime;

    /**
     * 审核时间
     * 变更审核的时间
     */
    @Column("review_time")
    private LocalDateTime reviewTime;

    /**
     * 是否向后兼容
     * 标识变更是否向后兼容
     */
    @Column("is_backward_compatible")
    private Boolean isBackwardCompatible = true;

    /**
     * 是否需要数据迁移
     * 标识变更是否需要数据迁移
     */
    @Column("requires_migration")
    private Boolean requiresMigration = false;

    /**
     * 迁移脚本路径
     * 数据迁移脚本的路径
     */
    @Column("migration_script_path")
    private String migrationScriptPath;

    /**
     * 文档链接
     * 相关文档的链接
     */
    @Column("documentation_link")
    private String documentationLink;

    /**
     * 标签
     * 变更的标签（多个标签用逗号分隔）
     */
    @Column("tags")
    private String tags;

    /**
     * 默认构造函数
     */
    public VersionChangeLog() {
        super();
        this.changeTime = LocalDateTime.now();
    }

    /**
     * 带参数的构造函数
     *
     * @param versionInfoId 版本信息ID
     * @param changeType 变更类型
     * @param title 变更标题
     * @param description 变更描述
     */
    public VersionChangeLog(Long versionInfoId, ChangeType changeType, String title, String description) {
        super();
        this.versionInfoId = versionInfoId;
        this.changeType = changeType;
        this.title = title;
        this.description = description;
        this.changeTime = LocalDateTime.now();
    }

    // Getter and Setter methods

    public Long getVersionInfoId() {
        return versionInfoId;
    }

    public void setVersionInfoId(Long versionInfoId) {
        this.versionInfoId = versionInfoId;
    }

    public Long getBuildVersionId() {
        return buildVersionId;
    }

    public void setBuildVersionId(Long buildVersionId) {
        this.buildVersionId = buildVersionId;
    }

    public Integer getChangeSequence() {
        return changeSequence;
    }

    public void setChangeSequence(Integer changeSequence) {
        this.changeSequence = changeSequence;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAffectedModules() {
        return affectedModules;
    }

    public void setAffectedModules(String affectedModules) {
        this.affectedModules = affectedModules;
    }

    public ImpactLevel getImpactLevel() {
        return impactLevel;
    }

    public void setImpactLevel(ImpactLevel impactLevel) {
        this.impactLevel = impactLevel;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    public String getRollbackPlan() {
        return rollbackPlan;
    }

    public void setRollbackPlan(String rollbackPlan) {
        this.rollbackPlan = rollbackPlan;
    }

    public String getRelatedIssues() {
        return relatedIssues;
    }

    public void setRelatedIssues(String relatedIssues) {
        this.relatedIssues = relatedIssues;
    }

    public String getRelatedPrs() {
        return relatedPrs;
    }

    public void setRelatedPrs(String relatedPrs) {
        this.relatedPrs = relatedPrs;
    }

    public String getGitCommitHash() {
        return gitCommitHash;
    }

    public void setGitCommitHash(String gitCommitHash) {
        this.gitCommitHash = gitCommitHash;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public LocalDateTime getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(LocalDateTime changeTime) {
        this.changeTime = changeTime;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    public Boolean getIsBackwardCompatible() {
        return isBackwardCompatible;
    }

    public void setIsBackwardCompatible(Boolean isBackwardCompatible) {
        this.isBackwardCompatible = isBackwardCompatible;
    }

    public Boolean getRequiresMigration() {
        return requiresMigration;
    }

    public void setRequiresMigration(Boolean requiresMigration) {
        this.requiresMigration = requiresMigration;
    }

    public String getMigrationScriptPath() {
        return migrationScriptPath;
    }

    public void setMigrationScriptPath(String migrationScriptPath) {
        this.migrationScriptPath = migrationScriptPath;
    }

    public String getDocumentationLink() {
        return documentationLink;
    }

    public void setDocumentationLink(String documentationLink) {
        this.documentationLink = documentationLink;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    // Business methods

    /**
     * 判断是否为破坏性变更
     *
     * @return 如果是破坏性变更返回true，否则返回false
     */
    public boolean isBreakingChange() {
        return this.changeType == ChangeType.BREAKING;
    }

    /**
     * 判断是否为安全修复
     *
     * @return 如果是安全修复返回true，否则返回false
     */
    public boolean isSecurityFix() {
        return this.changeType == ChangeType.SECURITY;
    }

    /**
     * 判断是否向后兼容
     *
     * @return 如果向后兼容返回true，否则返回false
     */
    public boolean isBackwardCompatible() {
        return Boolean.TRUE.equals(this.isBackwardCompatible);
    }

    /**
     * 判断是否需要数据迁移
     *
     * @return 如果需要数据迁移返回true，否则返回false
     */
    public boolean requiresMigration() {
        return Boolean.TRUE.equals(this.requiresMigration);
    }

    /**
     * 判断是否为高影响变更
     *
     * @return 如果是高影响变更返回true，否则返回false
     */
    public boolean isHighImpact() {
        return this.impactLevel == ImpactLevel.MAJOR || this.impactLevel == ImpactLevel.CRITICAL;
    }

    /**
     * 判断是否为高优先级变更
     *
     * @return 如果是高优先级变更返回true，否则返回false
     */
    public boolean isHighPriority() {
        return this.priority == Priority.HIGH;
    }

    /**
     * 设置审核信息
     *
     * @param reviewer 审核者
     */
    public void setReviewInfo(String reviewer) {
        this.reviewer = reviewer;
        this.reviewTime = LocalDateTime.now();
    }

    /**
     * 变更类型枚举
     */
    public enum ChangeType {
        /** 新功能 */
        FEATURE("新功能"),
        /** 错误修复 */
        BUGFIX("错误修复"),
        /** 改进优化 */
        IMPROVEMENT("改进优化"),
        /** 安全修复 */
        SECURITY("安全修复"),
        /** 破坏性变更 */
        BREAKING("破坏性变更"),
        /** 文档更新 */
        DOCUMENTATION("文档更新"),
        /** 重构 */
        REFACTOR("重构"),
        /** 性能优化 */
        PERFORMANCE("性能优化"),
        /** 测试 */
        TEST("测试"),
        /** 构建 */
        BUILD("构建"),
        /** 其他 */
        OTHER("其他");

        private final String description;

        ChangeType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 影响范围枚举
     */
    public enum ImpactLevel {
        /** 轻微 */
        MINOR("轻微"),
        /** 中等 */
        MODERATE("中等"),
        /** 重大 */
        MAJOR("重大"),
        /** 关键 */
        CRITICAL("关键");

        private final String description;

        ImpactLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 优先级枚举
     */
    public enum Priority {
        /** 高优先级 */
        HIGH("高"),
        /** 中优先级 */
        MEDIUM("中"),
        /** 低优先级 */
        LOW("低");

        private final String description;

        Priority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return String.format("VersionChangeLog{id=%d, versionId=%d, type=%s, title='%s', impact=%s, priority=%s}",
                getId(), versionInfoId, changeType, title, impactLevel, priority);
    }
}


