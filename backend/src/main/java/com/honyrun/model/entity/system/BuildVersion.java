package com.honyrun.model.entity.system;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 编译版本实体类
 *
 * 用于存储系统编译版本信息，支持R2DBC响应式数据访问。
 * 该实体管理8位编译版本号和编译相关的详细信息。
 *
 * 特性：
 * - 支持R2DBC响应式数据访问
 * - 继承审计功能
 * - 8位编译版本号管理
 * - 编译环境信息记录
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  16:15:00
 * @modified 2025-07-01 16:15:00
 * @version 2.0.0
 */
@Table("sys_build_version")
public class BuildVersion extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 8位编译版本号
     * 格式：YYYYMMDD（如：20250127）
     */
    @Column("build_number")
    private String buildNumber;

    /**
     * 编译序号
     * 同一天内的编译序号（从1开始）
     */
    @Column("build_sequence")
    private Integer buildSequence = 1;

    /**
     * 完整编译版本号
     * 格式：YYYYMMDD.序号（如：20250127.001）
     */
    @Column("full_build_number")
    private String fullBuildNumber;

    /**
     * 关联的版本信息ID
     * 外键关联到version_info表
     */
    @Column("version_info_id")
    private Long versionInfoId;

    /**
     * 编译状态
     * BUILDING-编译中, SUCCESS-成功, FAILED-失败, CANCELLED-已取消
     */
    @Column("build_status")
    private BuildStatus buildStatus = BuildStatus.BUILDING;

    /**
     * 编译类型
     * RELEASE-正式版, DEBUG-调试版, SNAPSHOT-快照版
     */
    @Column("build_type")
    private BuildType buildType = BuildType.RELEASE;

    /**
     * 编译环境
     * 编译时的环境标识（dev、test、prod）
     */
    @Column("build_environment")
    private String buildEnvironment;

    /**
     * 编译分支
     * Git分支名称
     */
    @Column("git_branch")
    private String gitBranch;

    /**
     * Git提交哈希
     * Git提交的完整哈希值
     */
    @Column("git_commit_hash")
    private String gitCommitHash;

    /**
     * Git提交哈希（短）
     * Git提交的短哈希值（前8位）
     */
    @Column("git_commit_hash_short")
    private String gitCommitHashShort;

    /**
     * Git提交信息
     * Git提交的描述信息
     */
    @Column("git_commit_message")
    private String gitCommitMessage;

    /**
     * Git提交作者
     * Git提交的作者信息
     */
    @Column("git_commit_author")
    private String gitCommitAuthor;

    /**
     * Git提交时间
     * Git提交的时间
     */
    @Column("git_commit_time")
    private LocalDateTime gitCommitTime;

    /**
     * 编译开始时间
     * 编译过程的开始时间
     */
    @Column("build_start_time")
    private LocalDateTime buildStartTime;

    /**
     * 编译结束时间
     * 编译过程的结束时间
     */
    @Column("build_end_time")
    private LocalDateTime buildEndTime;

    /**
     * 编译耗时（秒）
     * 编译过程的总耗时
     */
    @Column("build_duration")
    private Long buildDuration;

    /**
     * 编译机器
     * 执行编译的机器标识
     */
    @Column("build_machine")
    private String buildMachine;

    /**
     * 编译用户
     * 执行编译的用户
     */
    @Column("build_user")
    private String buildUser;

    /**
     * JDK版本
     * 编译使用的JDK版本
     */
    @Column("jdk_version")
    private String jdkVersion;

    /**
     * Maven版本
     * 编译使用的Maven版本
     */
    @Column("maven_version")
    private String mavenVersion;

    /**
     * 操作系统
     * 编译环境的操作系统信息
     */
    @Column("operating_system")
    private String operatingSystem;

    /**
     * 编译参数
     * 编译时使用的参数
     */
    @Column("build_parameters")
    private String buildParameters;

    /**
     * 编译日志路径
     * 编译日志文件的存储路径
     */
    @Column("build_log_path")
    private String buildLogPath;

    /**
     * 构建产物路径
     * 编译生成的文件路径
     */
    @Column("artifact_path")
    private String artifactPath;

    /**
     * 构建产物大小（字节）
     * 编译生成文件的大小
     */
    @Column("artifact_size")
    private Long artifactSize;

    /**
     * 构建产物校验和
     * 编译生成文件的MD5或SHA256校验和
     */
    @Column("artifact_checksum")
    private String artifactChecksum;

    /**
     * 编译错误信息
     * 编译失败时的错误信息
     */
    @Column("error_message")
    private String errorMessage;

    /**
     * 编译警告数量
     * 编译过程中的警告数量
     */
    @Column("warning_count")
    private Integer warningCount = 0;

    /**
     * 编译错误数量
     * 编译过程中的错误数量
     */
    @Column("error_count")
    private Integer errorCount = 0;

    /**
     * 是否自动编译
     * 标识是否为自动触发的编译
     */
    @Column("is_auto_build")
    private Boolean isAutoBuild = false;

    /**
     * 编译触发器
     * 触发编译的原因或方式
     */
    @Column("build_trigger")
    private String buildTrigger;

    /**
     * 默认构造函数
     */
    public BuildVersion() {
        super();
        this.buildStartTime = LocalDateTime.now();
    }

    /**
     * 带参数的构造函数
     *
     * @param buildNumber 编译版本号
     * @param buildType 编译类型
     * @param buildEnvironment 编译环境
     */
    public BuildVersion(String buildNumber, BuildType buildType, String buildEnvironment) {
        super();
        this.buildNumber = buildNumber;
        this.buildType = buildType;
        this.buildEnvironment = buildEnvironment;
        this.buildStartTime = LocalDateTime.now();
    }

    // Getter and Setter methods

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public Integer getBuildSequence() {
        return buildSequence;
    }

    public void setBuildSequence(Integer buildSequence) {
        this.buildSequence = buildSequence;
    }

    public String getFullBuildNumber() {
        return fullBuildNumber;
    }

    public void setFullBuildNumber(String fullBuildNumber) {
        this.fullBuildNumber = fullBuildNumber;
    }

    public Long getVersionInfoId() {
        return versionInfoId;
    }

    public void setVersionInfoId(Long versionInfoId) {
        this.versionInfoId = versionInfoId;
    }

    public BuildStatus getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(BuildStatus buildStatus) {
        this.buildStatus = buildStatus;
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }

    public String getBuildEnvironment() {
        return buildEnvironment;
    }

    public void setBuildEnvironment(String buildEnvironment) {
        this.buildEnvironment = buildEnvironment;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }

    public String getGitCommitHash() {
        return gitCommitHash;
    }

    public void setGitCommitHash(String gitCommitHash) {
        this.gitCommitHash = gitCommitHash;
        // 自动设置短哈希值
        if (gitCommitHash != null && gitCommitHash.length() >= 8) {
            this.gitCommitHashShort = gitCommitHash.substring(0, 8);
        }
    }

    public String getGitCommitHashShort() {
        return gitCommitHashShort;
    }

    public void setGitCommitHashShort(String gitCommitHashShort) {
        this.gitCommitHashShort = gitCommitHashShort;
    }

    public String getGitCommitMessage() {
        return gitCommitMessage;
    }

    public void setGitCommitMessage(String gitCommitMessage) {
        this.gitCommitMessage = gitCommitMessage;
    }

    public String getGitCommitAuthor() {
        return gitCommitAuthor;
    }

    public void setGitCommitAuthor(String gitCommitAuthor) {
        this.gitCommitAuthor = gitCommitAuthor;
    }

    public LocalDateTime getGitCommitTime() {
        return gitCommitTime;
    }

    public void setGitCommitTime(LocalDateTime gitCommitTime) {
        this.gitCommitTime = gitCommitTime;
    }

    public LocalDateTime getBuildStartTime() {
        return buildStartTime;
    }

    public void setBuildStartTime(LocalDateTime buildStartTime) {
        this.buildStartTime = buildStartTime;
    }

    public LocalDateTime getBuildEndTime() {
        return buildEndTime;
    }

    public void setBuildEndTime(LocalDateTime buildEndTime) {
        this.buildEndTime = buildEndTime;
        // 自动计算编译耗时
        if (this.buildStartTime != null && buildEndTime != null) {
            this.buildDuration = java.time.Duration.between(this.buildStartTime, buildEndTime).getSeconds();
        }
    }

    public Long getBuildDuration() {
        return buildDuration;
    }

    public void setBuildDuration(Long buildDuration) {
        this.buildDuration = buildDuration;
    }

    public String getBuildMachine() {
        return buildMachine;
    }

    public void setBuildMachine(String buildMachine) {
        this.buildMachine = buildMachine;
    }

    public String getBuildUser() {
        return buildUser;
    }

    public void setBuildUser(String buildUser) {
        this.buildUser = buildUser;
    }

    public String getJdkVersion() {
        return jdkVersion;
    }

    public void setJdkVersion(String jdkVersion) {
        this.jdkVersion = jdkVersion;
    }

    public String getMavenVersion() {
        return mavenVersion;
    }

    public void setMavenVersion(String mavenVersion) {
        this.mavenVersion = mavenVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getBuildParameters() {
        return buildParameters;
    }

    public void setBuildParameters(String buildParameters) {
        this.buildParameters = buildParameters;
    }

    public String getBuildLogPath() {
        return buildLogPath;
    }

    public void setBuildLogPath(String buildLogPath) {
        this.buildLogPath = buildLogPath;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public void setArtifactPath(String artifactPath) {
        this.artifactPath = artifactPath;
    }

    public Long getArtifactSize() {
        return artifactSize;
    }

    public void setArtifactSize(Long artifactSize) {
        this.artifactSize = artifactSize;
    }

    public String getArtifactChecksum() {
        return artifactChecksum;
    }

    public void setArtifactChecksum(String artifactChecksum) {
        this.artifactChecksum = artifactChecksum;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(Integer warningCount) {
        this.warningCount = warningCount;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public Boolean getIsAutoBuild() {
        return isAutoBuild;
    }

    public void setIsAutoBuild(Boolean isAutoBuild) {
        this.isAutoBuild = isAutoBuild;
    }

    public String getBuildTrigger() {
        return buildTrigger;
    }

    public void setBuildTrigger(String buildTrigger) {
        this.buildTrigger = buildTrigger;
    }

    // Business methods

    /**
     * 判断编译是否成功
     *
     * @return 如果编译成功返回true，否则返回false
     */
    public boolean isSuccess() {
        return this.buildStatus == BuildStatus.SUCCESS;
    }

    /**
     * 判断编译是否失败
     *
     * @return 如果编译失败返回true，否则返回false
     */
    public boolean isFailed() {
        return this.buildStatus == BuildStatus.FAILED;
    }

    /**
     * 判断是否正在编译
     *
     * @return 如果正在编译返回true，否则返回false
     */
    public boolean isBuilding() {
        return this.buildStatus == BuildStatus.BUILDING;
    }

    /**
     * 判断是否为自动编译
     *
     * @return 如果是自动编译返回true，否则返回false
     */
    public boolean isAutoBuild() {
        return Boolean.TRUE.equals(this.isAutoBuild);
    }

    /**
     * 开始编译
     */
    public void startBuild() {
        this.buildStatus = BuildStatus.BUILDING;
        this.buildStartTime = LocalDateTime.now();
    }

    /**
     * 编译成功
     */
    public void buildSuccess() {
        this.buildStatus = BuildStatus.SUCCESS;
        this.buildEndTime = LocalDateTime.now();
        if (this.buildStartTime != null) {
            this.buildDuration = java.time.Duration.between(this.buildStartTime, this.buildEndTime).getSeconds();
        }
    }

    /**
     * 编译失败
     *
     * @param errorMessage 错误信息
     */
    public void buildFailed(String errorMessage) {
        this.buildStatus = BuildStatus.FAILED;
        this.buildEndTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
        if (this.buildStartTime != null) {
            this.buildDuration = java.time.Duration.between(this.buildStartTime, this.buildEndTime).getSeconds();
        }
    }

    /**
     * 取消编译
     */
    public void cancelBuild() {
        this.buildStatus = BuildStatus.CANCELLED;
        this.buildEndTime = LocalDateTime.now();
        if (this.buildStartTime != null) {
            this.buildDuration = java.time.Duration.between(this.buildStartTime, this.buildEndTime).getSeconds();
        }
    }

    /**
     * 生成完整编译版本号
     */
    public void generateFullBuildNumber() {
        if (this.buildNumber != null && this.buildSequence != null) {
            this.fullBuildNumber = String.format("%s.%03d", this.buildNumber, this.buildSequence);
        }
    }

    /**
     * 获取格式化的编译耗时
     *
     * @return 格式化的编译耗时字符串
     */
    public String getFormattedBuildDuration() {
        if (buildDuration == null) {
            return "未知";
        }

        long hours = buildDuration / 3600;
        long minutes = (buildDuration % 3600) / 60;
        long seconds = buildDuration % 60;

        if (hours > 0) {
            return String.format("%d小时%d分钟%d秒", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    /**
     * 编译状态枚举
     */
    public enum BuildStatus {
        /** 编译中 */
        BUILDING("编译中"),
        /** 成功 */
        SUCCESS("成功"),
        /** 失败 */
        FAILED("失败"),
        /** 已取消 */
        CANCELLED("已取消");

        private final String description;

        BuildStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 编译类型枚举
     */
    public enum BuildType {
        /** 正式版 */
        RELEASE("正式版"),
        /** 调试版 */
        DEBUG("调试版"),
        /** 快照版 */
        SNAPSHOT("快照版");

        private final String description;

        BuildType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return String.format("BuildVersion{id=%d, buildNumber='%s', fullBuildNumber='%s', status=%s, type=%s, duration=%s}",
                getId(), buildNumber, fullBuildNumber, buildStatus, buildType, getFormattedBuildDuration());
    }
}


