package com.honyrun.model.entity.system;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 版本信息实体类
 *
 * 用于存储系统版本信息，支持R2DBC响应式数据访问。
 * 该实体管理系统的版本历史记录，包括版本号、发布时间、更新内容等信息。
 *
 * 特性：
 * - 支持R2DBC响应式数据访问
 * - 继承审计功能
 * - 版本历史管理
 * - 语义化版本控制
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  16:10:00
 * @modified 2025-07-01 16:10:00
 * @version 2.0.0
 */
@Table("sys_version_info")
public class VersionInfo extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 版本号
     * 语义化版本号（如：2.0.0）
     */
    @Column("version_number")
    private String versionNumber;

    /**
     * 版本名称
     * 版本的显示名称
     */
    @Column("version_name")
    private String versionName;

    /**
     * 版本类型
     * MAJOR-主版本, MINOR-次版本, PATCH-修订版本, BETA-测试版本, ALPHA-内测版本
     */
    @Column("version_type")
    private VersionType versionType = VersionType.PATCH;

    /**
     * 版本状态
     * DEVELOPMENT-开发中, TESTING-测试中, RELEASED-已发布, DEPRECATED-已弃用, ARCHIVED-已归档
     */
    @Column("version_status")
    private VersionStatus versionStatus = VersionStatus.DEVELOPMENT;

    /**
     * 版本描述
     * 版本的详细描述信息
     */
    @Column("description")
    private String description;

    /**
     * 更新内容
     * 版本更新的具体内容
     */
    @Column("update_content")
    private String updateContent;

    /**
     * 新增功能
     * 版本新增的功能列表
     */
    @Column("new_features")
    private String newFeatures;

    /**
     * 修复问题
     * 版本修复的问题列表
     */
    @Column("bug_fixes")
    private String bugFixes;

    /**
     * 改进优化
     * 版本的改进和优化内容
     */
    @Column("improvements")
    private String improvements;

    /**
     * 破坏性变更
     * 不兼容的变更内容
     */
    @Column("breaking_changes")
    private String breakingChanges;

    /**
     * 发布时间
     * 版本的发布时间
     */
    @Column("release_date")
    private LocalDateTime releaseDate;

    /**
     * 计划发布时间
     * 版本计划的发布时间
     */
    @Column("planned_release_date")
    private LocalDateTime plannedReleaseDate;

    /**
     * 开发开始时间
     * 版本开发的开始时间
     */
    @Column("development_start_date")
    private LocalDateTime developmentStartDate;

    /**
     * 开发结束时间
     * 版本开发的结束时间
     */
    @Column("development_end_date")
    private LocalDateTime developmentEndDate;

    /**
     * 是否当前版本
     * 标识是否为当前使用的版本
     */
    @Column("is_current")
    private Boolean isCurrent = false;

    /**
     * 是否稳定版本
     * 标识是否为稳定版本
     */
    @Column("is_stable")
    private Boolean isStable = false;

    /**
     * 是否LTS版本
     * 标识是否为长期支持版本
     */
    @Column("is_lts")
    private Boolean isLts = false;

    /**
     * 支持结束时间
     * 版本支持的结束时间
     */
    @Column("support_end_date")
    private LocalDateTime supportEndDate;

    /**
     * 下载次数
     * 版本的下载次数统计
     */
    @Column("download_count")
    private Long downloadCount = 0L;

    /**
     * 文件大小（字节）
     * 版本文件的大小
     */
    @Column("file_size")
    private Long fileSize;

    /**
     * 文件路径
     * 版本文件的存储路径
     */
    @Column("file_path")
    private String filePath;

    /**
     * 校验和
     * 版本文件的MD5或SHA256校验和
     */
    @Column("checksum")
    private String checksum;

    /**
     * 发布说明
     * 版本发布的详细说明
     */
    @Column("release_notes")
    private String releaseNotes;

    /**
     * 默认构造函数
     */
    public VersionInfo() {
        super();
    }

    /**
     * 带参数的构造函数
     *
     * @param versionNumber 版本号
     * @param versionName 版本名称
     * @param versionType 版本类型
     */
    public VersionInfo(String versionNumber, String versionName, VersionType versionType) {
        super();
        this.versionNumber = versionNumber;
        this.versionName = versionName;
        this.versionType = versionType;
    }

    // Getter and Setter methods

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public VersionType getVersionType() {
        return versionType;
    }

    public void setVersionType(VersionType versionType) {
        this.versionType = versionType;
    }

    public VersionStatus getVersionStatus() {
        return versionStatus;
    }

    public void setVersionStatus(VersionStatus versionStatus) {
        this.versionStatus = versionStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUpdateContent() {
        return updateContent;
    }

    public void setUpdateContent(String updateContent) {
        this.updateContent = updateContent;
    }

    public String getNewFeatures() {
        return newFeatures;
    }

    public void setNewFeatures(String newFeatures) {
        this.newFeatures = newFeatures;
    }

    public String getBugFixes() {
        return bugFixes;
    }

    public void setBugFixes(String bugFixes) {
        this.bugFixes = bugFixes;
    }

    public String getImprovements() {
        return improvements;
    }

    public void setImprovements(String improvements) {
        this.improvements = improvements;
    }

    public String getBreakingChanges() {
        return breakingChanges;
    }

    public void setBreakingChanges(String breakingChanges) {
        this.breakingChanges = breakingChanges;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public LocalDateTime getPlannedReleaseDate() {
        return plannedReleaseDate;
    }

    public void setPlannedReleaseDate(LocalDateTime plannedReleaseDate) {
        this.plannedReleaseDate = plannedReleaseDate;
    }

    public LocalDateTime getDevelopmentStartDate() {
        return developmentStartDate;
    }

    public void setDevelopmentStartDate(LocalDateTime developmentStartDate) {
        this.developmentStartDate = developmentStartDate;
    }

    public LocalDateTime getDevelopmentEndDate() {
        return developmentEndDate;
    }

    public void setDevelopmentEndDate(LocalDateTime developmentEndDate) {
        this.developmentEndDate = developmentEndDate;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public Boolean getIsStable() {
        return isStable;
    }

    public void setIsStable(Boolean isStable) {
        this.isStable = isStable;
    }

    public Boolean getIsLts() {
        return isLts;
    }

    public void setIsLts(Boolean isLts) {
        this.isLts = isLts;
    }

    public LocalDateTime getSupportEndDate() {
        return supportEndDate;
    }

    public void setSupportEndDate(LocalDateTime supportEndDate) {
        this.supportEndDate = supportEndDate;
    }

    public Long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    // Business methods

    /**
     * 判断是否为当前版本
     *
     * @return 如果是当前版本返回true，否则返回false
     */
    public boolean isCurrent() {
        return Boolean.TRUE.equals(this.isCurrent);
    }

    /**
     * 判断是否为稳定版本
     *
     * @return 如果是稳定版本返回true，否则返回false
     */
    public boolean isStable() {
        return Boolean.TRUE.equals(this.isStable);
    }

    /**
     * 判断是否为LTS版本
     *
     * @return 如果是LTS版本返回true，否则返回false
     */
    public boolean isLts() {
        return Boolean.TRUE.equals(this.isLts);
    }

    /**
     * 判断版本是否已发布
     *
     * @return 如果已发布返回true，否则返回false
     */
    public boolean isReleased() {
        return this.versionStatus == VersionStatus.RELEASED;
    }

    /**
     * 判断版本是否已弃用
     *
     * @return 如果已弃用返回true，否则返回false
     */
    public boolean isDeprecated() {
        return this.versionStatus == VersionStatus.DEPRECATED;
    }

    /**
     * 判断版本是否仍在支持期内
     *
     * @return 如果仍在支持期内返回true，否则返回false
     */
    public boolean isSupported() {
        if (supportEndDate == null) {
            return true; // 没有设置支持结束时间，认为一直支持
        }
        return LocalDateTime.now().isBefore(supportEndDate);
    }

    /**
     * 发布版本
     */
    public void release() {
        this.versionStatus = VersionStatus.RELEASED;
        this.releaseDate = LocalDateTime.now();
        this.isStable = true;
    }

    /**
     * 弃用版本
     */
    public void deprecate() {
        this.versionStatus = VersionStatus.DEPRECATED;
        this.isCurrent = false;
    }

    /**
     * 设置为当前版本
     */
    public void setAsCurrent() {
        this.isCurrent = true;
        if (this.versionStatus == VersionStatus.DEVELOPMENT) {
            this.versionStatus = VersionStatus.RELEASED;
            this.releaseDate = LocalDateTime.now();
        }
    }

    /**
     * 增加下载次数
     */
    public void incrementDownloadCount() {
        if (this.downloadCount == null) {
            this.downloadCount = 0L;
        }
        this.downloadCount++;
    }

    /**
     * 获取格式化的文件大小
     *
     * @return 格式化的文件大小字符串
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "未知";
        }

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 版本类型枚举
     */
    public enum VersionType {
        /** 主版本 */
        MAJOR("主版本"),
        /** 次版本 */
        MINOR("次版本"),
        /** 修订版本 */
        PATCH("修订版本"),
        /** 测试版本 */
        BETA("测试版本"),
        /** 内测版本 */
        ALPHA("内测版本");

        private final String description;

        VersionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 版本状态枚举
     */
    public enum VersionStatus {
        /** 开发中 */
        DEVELOPMENT("开发中"),
        /** 测试中 */
        TESTING("测试中"),
        /** 已发布 */
        RELEASED("已发布"),
        /** 已弃用 */
        DEPRECATED("已弃用"),
        /** 已归档 */
        ARCHIVED("已归档");

        private final String description;

        VersionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return String.format("VersionInfo{id=%d, version='%s', name='%s', type=%s, status=%s, current=%s}",
                getId(), versionNumber, versionName, versionType, versionStatus, isCurrent);
    }
}


