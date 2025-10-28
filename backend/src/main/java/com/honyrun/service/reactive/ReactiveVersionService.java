package com.honyrun.service.reactive;

import com.honyrun.model.entity.system.VersionChangeLog;
import com.honyrun.model.entity.system.VersionInfo;
import com.honyrun.util.version.VersionChangelogUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式版本管理服务接口
 *
 * 提供版本信息管理、版本变更日志、版本恢复等功能的响应式服务接口。
 * 支持8位编译版本号管理和版本历史记录管理。
 *
 * 主要功能：
 * - 版本信息查询和管理
 * - 版本变更日志管理
 * - 版本恢复和回滚
 * - 版本兼容性检查
 * - 版本统计和报告
 *
 * 响应式特性：
 * - 非阻塞操作：所有操作均为非阻塞
 * - 流式处理：支持版本信息的流式查询
 * - 错误处理：提供版本操作失败的恢复机制
 * - 异步更新：支持版本信息的异步更新
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01 10:45:00
 * @modified 2025-07-01 10:30:00
 * @version 2.0.0
 */
public interface ReactiveVersionService {

    /**
     * 获取当前版本信息
     *
     * @return 当前版本信息的Mono
     */
    Mono<VersionInfo> getCurrentVersion();

    /**
     * 获取版本历史列表
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 版本历史列表的Flux
     */
    Flux<VersionInfo> getVersionHistory(int page, int size);

    /**
     * 根据版本号获取版本信息
     *
     * @param versionNumber 版本号
     * @return 版本信息的Mono
     */
    Mono<VersionInfo> getVersionByNumber(String versionNumber);

    /**
     * 创建新版本
     *
     * @param versionInfo 版本信息
     * @return 创建的版本信息的Mono
     */
    Mono<VersionInfo> createVersion(VersionInfo versionInfo);

    /**
     * 更新版本信息
     *
     * @param versionInfo 版本信息
     * @return 更新后的版本信息的Mono
     */
    Mono<VersionInfo> updateVersion(VersionInfo versionInfo);

    /**
     * 删除版本
     *
     * @param versionId 版本ID
     * @return 删除操作的Mono
     */
    Mono<Void> deleteVersion(Long versionId);

    /**
     * 获取版本变更日志
     *
     * @param versionNumber 版本号
     * @return 版本变更日志的Mono
     */
    Mono<VersionChangeLog> getVersionChangeLog(String versionNumber);

    /**
     * 获取版本变更日志分页列表
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 版本变更日志分页结果的Mono
     */
    Mono<VersionChangelogUtil.ChangelogPage> getVersionChangeLogPage(int page, int size);

    /**
     * 搜索版本变更日志
     *
     * @param keyword 搜索关键词
     * @param changeType 变更类型
     * @return 搜索结果的Flux
     */
    Flux<VersionChangeLog> searchVersionChangeLog(String keyword, VersionChangelogUtil.ChangeType changeType);

    /**
     * 添加版本变更日志
     *
     * @param changeLog 变更日志
     * @return 添加的变更日志的Mono
     */
    Mono<VersionChangeLog> addVersionChangeLog(VersionChangeLog changeLog);

    /**
     * 获取构建版本统计信息
     *
     * @return 构建统计信息的Mono
     */
    Mono<BuildStatistics> getBuildStatistics();

    /**
     * 检查版本兼容性
     *
     * @param targetVersion 目标版本
     * @param currentVersion 当前版本
     * @return 兼容性检查结果的Mono
     */
    Mono<VersionCompatibility> checkVersionCompatibility(String targetVersion, String currentVersion);

    /**
     * 恢复到指定版本
     *
     * @param targetVersion 目标版本号
     * @param recoveryType 恢复类型
     * @return 恢复操作结果的Mono
     */
    Mono<RecoveryResult> recoverToVersion(String targetVersion, String recoveryType);

    /**
     * 获取版本恢复历史
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 恢复历史列表的Flux
     */
    Flux<RecoveryRecord> getRecoveryHistory(int page, int size);

    /**
     * 验证版本完整性
     *
     * @param versionNumber 版本号
     * @return 验证结果的Mono
     */
    Mono<ValidationResult> validateVersionIntegrity(String versionNumber);

    /**
     * 生成版本报告
     *
     * @param startVersion 开始版本
     * @param endVersion 结束版本
     * @return 版本报告的Mono
     */
    Mono<VersionReport> generateVersionReport(String startVersion, String endVersion);

    // ==================== 内部类定义 ====================

    /**
     * 构建统计信息
     */
    class BuildStatistics {
        private long totalBuilds;
        private String currentVersion;
        private String lastSuccessfulVersion;
        private long successfulBuilds;
        private long failedBuilds;
        private double successRate;

        // Getters and Setters
        public long getTotalBuilds() {
            return totalBuilds;
        }

        public void setTotalBuilds(long totalBuilds) {
            this.totalBuilds = totalBuilds;
        }

        public String getCurrentVersion() {
            return currentVersion;
        }

        public void setCurrentVersion(String currentVersion) {
            this.currentVersion = currentVersion;
        }

        public String getLastSuccessfulVersion() {
            return lastSuccessfulVersion;
        }

        public void setLastSuccessfulVersion(String lastSuccessfulVersion) {
            this.lastSuccessfulVersion = lastSuccessfulVersion;
        }

        public long getSuccessfulBuilds() {
            return successfulBuilds;
        }

        public void setSuccessfulBuilds(long successfulBuilds) {
            this.successfulBuilds = successfulBuilds;
        }

        public long getFailedBuilds() {
            return failedBuilds;
        }

        public void setFailedBuilds(long failedBuilds) {
            this.failedBuilds = failedBuilds;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(double successRate) {
            this.successRate = successRate;
        }
    }

    /**
     * 版本兼容性检查结果
     */
    class VersionCompatibility {
        private String targetVersion;
        private String currentVersion;
        private boolean compatible;
        private String reason;
        private String recommendation;

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

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }
    }

    /**
     * 恢复操作结果
     */
    class RecoveryResult {
        private boolean success;
        private String message;
        private String targetVersion;
        private String previousVersion;
        private String recoveryId;

        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTargetVersion() {
            return targetVersion;
        }

        public void setTargetVersion(String targetVersion) {
            this.targetVersion = targetVersion;
        }

        public String getPreviousVersion() {
            return previousVersion;
        }

        public void setPreviousVersion(String previousVersion) {
            this.previousVersion = previousVersion;
        }

        public String getRecoveryId() {
            return recoveryId;
        }

        public void setRecoveryId(String recoveryId) {
            this.recoveryId = recoveryId;
        }
    }

    /**
     * 恢复记录
     */
    class RecoveryRecord {
        private String recoveryId;
        private String targetVersion;
        private String previousVersion;
        private String recoveryType;
        private boolean success;
        private String message;
        private java.time.LocalDateTime recoveryTime;

        // Getters and Setters
        public String getRecoveryId() {
            return recoveryId;
        }

        public void setRecoveryId(String recoveryId) {
            this.recoveryId = recoveryId;
        }

        public String getTargetVersion() {
            return targetVersion;
        }

        public void setTargetVersion(String targetVersion) {
            this.targetVersion = targetVersion;
        }

        public String getPreviousVersion() {
            return previousVersion;
        }

        public void setPreviousVersion(String previousVersion) {
            this.previousVersion = previousVersion;
        }

        public String getRecoveryType() {
            return recoveryType;
        }

        public void setRecoveryType(String recoveryType) {
            this.recoveryType = recoveryType;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public java.time.LocalDateTime getRecoveryTime() {
            return recoveryTime;
        }

        public void setRecoveryTime(java.time.LocalDateTime recoveryTime) {
            this.recoveryTime = recoveryTime;
        }
    }

    /**
     * 验证结果
     */
    class ValidationResult {
        private boolean valid;
        private String message;
        private String versionNumber;
        private java.util.List<String> issues;

        // Getters and Setters
        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getVersionNumber() {
            return versionNumber;
        }

        public void setVersionNumber(String versionNumber) {
            this.versionNumber = versionNumber;
        }

        public java.util.List<String> getIssues() {
            return issues;
        }

        public void setIssues(java.util.List<String> issues) {
            this.issues = issues;
        }
    }

    /**
     * 版本报告
     */
    class VersionReport {
        private String startVersion;
        private String endVersion;
        private int totalVersions;
        private java.util.List<VersionChangeLog> changes;
        private java.time.LocalDateTime generatedTime;

        // Getters and Setters
        public String getStartVersion() {
            return startVersion;
        }

        public void setStartVersion(String startVersion) {
            this.startVersion = startVersion;
        }

        public String getEndVersion() {
            return endVersion;
        }

        public void setEndVersion(String endVersion) {
            this.endVersion = endVersion;
        }

        public int getTotalVersions() {
            return totalVersions;
        }

        public void setTotalVersions(int totalVersions) {
            this.totalVersions = totalVersions;
        }

        public java.util.List<VersionChangeLog> getChanges() {
            return changes;
        }

        public void setChanges(java.util.List<VersionChangeLog> changes) {
            this.changes = changes;
        }

        public java.time.LocalDateTime getGeneratedTime() {
            return generatedTime;
        }

        public void setGeneratedTime(java.time.LocalDateTime generatedTime) {
            this.generatedTime = generatedTime;
        }
    }
}

