package com.honyrun.model.entity.business;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 核验结果实体类
 *
 * 用于存储核验业务的处理结果，包含结果数据、关联关系、处理详情等
 * 支持R2DBC响应式数据访问和审计功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  20:55:00
 * @modified 2025-07-01 20:55:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("biz_verification_result")
public class VerificationResult extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 关联的核验请求ID
     * 外键关联到biz_verification_request表
     */
    @Column("request_id")
    private Long requestId;

    /**
     * 结果编号
     * 唯一标识核验结果，格式：VRS + yyyyMMddHHmmss + 4位随机数
     */
    @Column("result_no")
    private String resultNo;

    /**
     * 核验结果状态
     * 1-通过，2-不通过，3-部分通过，4-需要补充材料，5-异常
     */
    @Column("result_status")
    private Integer resultStatus;

    /**
     * 核验结果描述
     * 详细的核验结果说明
     */
    @Column("result_description")
    private String resultDescription;

    /**
     * 核验得分
     * 核验的量化得分，满分100分
     */
    @Column("score")
    private BigDecimal score;

    /**
     * 核验详情
     * JSON格式存储的详细核验结果数据
     */
    @Column("result_details")
    private String resultDetails;

    /**
     * 核验规则
     * 应用的核验规则和标准
     */
    @Column("verification_rules")
    private String verificationRules;

    /**
     * 核验方法
     * 使用的核验方法和技术
     */
    @Column("verification_method")
    private String verificationMethod;

    /**
     * 数据源
     * 核验数据的来源
     */
    @Column("data_source")
    private String dataSource;

    /**
     * 核验开始时间
     * 核验处理的开始时间
     */
    @Column("verification_start_time")
    private LocalDateTime verificationStartTime;

    /**
     * 核验结束时间
     * 核验处理的结束时间
     */
    @Column("verification_end_time")
    private LocalDateTime verificationEndTime;

    /**
     * 处理耗时（毫秒）
     * 核验处理的总耗时
     */
    @Column("processing_time_ms")
    private Long processingTimeMs;

    /**
     * 核验人员ID
     * 执行核验的人员ID
     */
    @Column("verifier_id")
    private Long verifierId;

    /**
     * 核验人员姓名
     * 执行核验的人员姓名
     */
    @Column("verifier_name")
    private String verifierName;

    /**
     * 审核人员ID
     * 审核核验结果的人员ID
     */
    @Column("reviewer_id")
    private Long reviewerId;

    /**
     * 审核人员姓名
     * 审核核验结果的人员姓名
     */
    @Column("reviewer_name")
    private String reviewerName;

    /**
     * 审核时间
     * 结果审核的时间
     */
    @Column("review_time")
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     * 审核人员的意见和建议
     */
    @Column("review_comments")
    private String reviewComments;

    /**
     * 风险等级
     * 1-低风险，2-中风险，3-高风险，4-极高风险
     */
    @Column("risk_level")
    private Integer riskLevel;

    /**
     * 风险描述
     * 识别的风险点和风险描述
     */
    @Column("risk_description")
    private String riskDescription;

    /**
     * 建议措施
     * 针对核验结果的建议和措施
     */
    @Column("recommendations")
    private String recommendations;

    /**
     * 证据文件
     * JSON格式存储的证据文件信息
     */
    @Column("evidence_files")
    private String evidenceFiles;

    /**
     * 有效期
     * 核验结果的有效期
     */
    @Column("validity_period")
    private LocalDateTime validityPeriod;

    /**
     * 是否需要复核
     * true-需要复核，false-不需要复核
     */
    @Column("requires_recheck")
    private Boolean requiresRecheck;

    /**
     * 复核原因
     * 需要复核的原因说明
     */
    @Column("recheck_reason")
    private String recheckReason;

    /**
     * 标签
     * 用于分类和检索的标签，多个标签用逗号分隔
     */
    @Column("tags")
    private String tags;

    /**
     * 默认构造函数
     */
    public VerificationResult() {
        super();
        this.resultStatus = 5; // 默认状态为异常
        this.riskLevel = 1; // 默认风险等级为低风险
        this.requiresRecheck = false; // 默认不需要复核
    }

    /**
     * 带ID的构造函数
     *
     * @param id 主键ID
     */
    public VerificationResult(Long id) {
        super(id);
        this.resultStatus = 5;
        this.riskLevel = 1;
        this.requiresRecheck = false;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取关联的核验请求ID
     *
     * @return 核验请求ID
     */
    public Long getRequestId() {
        return requestId;
    }

    /**
     * 设置关联的核验请求ID
     *
     * @param requestId 核验请求ID
     */
    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    /**
     * 获取结果编号
     *
     * @return 结果编号
     */
    public String getResultNo() {
        return resultNo;
    }

    /**
     * 设置结果编号
     *
     * @param resultNo 结果编号
     */
    public void setResultNo(String resultNo) {
        this.resultNo = resultNo;
    }

    /**
     * 获取核验结果状态
     *
     * @return 核验结果状态
     */
    public Integer getResultStatus() {
        return resultStatus;
    }

    /**
     * 设置核验结果状态
     *
     * @param resultStatus 核验结果状态
     */
    public void setResultStatus(Integer resultStatus) {
        this.resultStatus = resultStatus;
    }

    /**
     * 获取核验结果描述
     *
     * @return 核验结果描述
     */
    public String getResultDescription() {
        return resultDescription;
    }

    /**
     * 设置核验结果描述
     *
     * @param resultDescription 核验结果描述
     */
    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
    }

    /**
     * 获取核验得分
     *
     * @return 核验得分
     */
    public BigDecimal getScore() {
        return score;
    }

    /**
     * 设置核验得分
     *
     * @param score 核验得分
     */
    public void setScore(BigDecimal score) {
        this.score = score;
    }

    /**
     * 获取核验详情
     *
     * @return 核验详情
     */
    public String getResultDetails() {
        return resultDetails;
    }

    /**
     * 设置核验详情
     *
     * @param resultDetails 核验详情
     */
    public void setResultDetails(String resultDetails) {
        this.resultDetails = resultDetails;
    }

    /**
     * 获取核验规则
     *
     * @return 核验规则
     */
    public String getVerificationRules() {
        return verificationRules;
    }

    /**
     * 设置核验规则
     *
     * @param verificationRules 核验规则
     */
    public void setVerificationRules(String verificationRules) {
        this.verificationRules = verificationRules;
    }

    /**
     * 获取核验方法
     *
     * @return 核验方法
     */
    public String getVerificationMethod() {
        return verificationMethod;
    }

    /**
     * 设置核验方法
     *
     * @param verificationMethod 核验方法
     */
    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    /**
     * 获取数据源
     *
     * @return 数据源
     */
    public String getDataSource() {
        return dataSource;
    }

    /**
     * 设置数据源
     *
     * @param dataSource 数据源
     */
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 获取核验开始时间
     *
     * @return 核验开始时间
     */
    public LocalDateTime getVerificationStartTime() {
        return verificationStartTime;
    }

    /**
     * 设置核验开始时间
     *
     * @param verificationStartTime 核验开始时间
     */
    public void setVerificationStartTime(LocalDateTime verificationStartTime) {
        this.verificationStartTime = verificationStartTime;
    }

    /**
     * 获取核验结束时间
     *
     * @return 核验结束时间
     */
    public LocalDateTime getVerificationEndTime() {
        return verificationEndTime;
    }

    /**
     * 设置核验结束时间
     *
     * @param verificationEndTime 核验结束时间
     */
    public void setVerificationEndTime(LocalDateTime verificationEndTime) {
        this.verificationEndTime = verificationEndTime;
    }

    /**
     * 获取处理耗时（毫秒）
     *
     * @return 处理耗时
     */
    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    /**
     * 设置处理耗时（毫秒）
     *
     * @param processingTimeMs 处理耗时
     */
    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    /**
     * 获取核验人员ID
     *
     * @return 核验人员ID
     */
    public Long getVerifierId() {
        return verifierId;
    }

    /**
     * 设置核验人员ID
     *
     * @param verifierId 核验人员ID
     */
    public void setVerifierId(Long verifierId) {
        this.verifierId = verifierId;
    }

    /**
     * 获取核验人员姓名
     *
     * @return 核验人员姓名
     */
    public String getVerifierName() {
        return verifierName;
    }

    /**
     * 设置核验人员姓名
     *
     * @param verifierName 核验人员姓名
     */
    public void setVerifierName(String verifierName) {
        this.verifierName = verifierName;
    }

    /**
     * 获取审核人员ID
     *
     * @return 审核人员ID
     */
    public Long getReviewerId() {
        return reviewerId;
    }

    /**
     * 设置审核人员ID
     *
     * @param reviewerId 审核人员ID
     */
    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    /**
     * 获取审核人员姓名
     *
     * @return 审核人员姓名
     */
    public String getReviewerName() {
        return reviewerName;
    }

    /**
     * 设置审核人员姓名
     *
     * @param reviewerName 审核人员姓名
     */
    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    /**
     * 获取审核时间
     *
     * @return 审核时间
     */
    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    /**
     * 设置审核时间
     *
     * @param reviewTime 审核时间
     */
    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    /**
     * 获取审核意见
     *
     * @return 审核意见
     */
    public String getReviewComments() {
        return reviewComments;
    }

    /**
     * 设置审核意见
     *
     * @param reviewComments 审核意见
     */
    public void setReviewComments(String reviewComments) {
        this.reviewComments = reviewComments;
    }

    /**
     * 获取风险等级
     *
     * @return 风险等级
     */
    public Integer getRiskLevel() {
        return riskLevel;
    }

    /**
     * 设置风险等级
     *
     * @param riskLevel 风险等级
     */
    public void setRiskLevel(Integer riskLevel) {
        this.riskLevel = riskLevel;
    }

    /**
     * 获取风险描述
     *
     * @return 风险描述
     */
    public String getRiskDescription() {
        return riskDescription;
    }

    /**
     * 设置风险描述
     *
     * @param riskDescription 风险描述
     */
    public void setRiskDescription(String riskDescription) {
        this.riskDescription = riskDescription;
    }

    /**
     * 获取建议措施
     *
     * @return 建议措施
     */
    public String getRecommendations() {
        return recommendations;
    }

    /**
     * 设置建议措施
     *
     * @param recommendations 建议措施
     */
    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    /**
     * 获取证据文件
     *
     * @return 证据文件
     */
    public String getEvidenceFiles() {
        return evidenceFiles;
    }

    /**
     * 设置证据文件
     *
     * @param evidenceFiles 证据文件
     */
    public void setEvidenceFiles(String evidenceFiles) {
        this.evidenceFiles = evidenceFiles;
    }

    /**
     * 获取有效期
     *
     * @return 有效期
     */
    public LocalDateTime getValidityPeriod() {
        return validityPeriod;
    }

    /**
     * 设置有效期
     *
     * @param validityPeriod 有效期
     */
    public void setValidityPeriod(LocalDateTime validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    /**
     * 获取是否需要复核
     *
     * @return 是否需要复核
     */
    public Boolean getRequiresRecheck() {
        return requiresRecheck;
    }

    /**
     * 设置是否需要复核
     *
     * @param requiresRecheck 是否需要复核
     */
    public void setRequiresRecheck(Boolean requiresRecheck) {
        this.requiresRecheck = requiresRecheck;
    }

    /**
     * 获取复核原因
     *
     * @return 复核原因
     */
    public String getRecheckReason() {
        return recheckReason;
    }

    /**
     * 设置复核原因
     *
     * @param recheckReason 复核原因
     */
    public void setRecheckReason(String recheckReason) {
        this.recheckReason = recheckReason;
    }

    /**
     * 获取标签
     *
     * @return 标签
     */
    public String getTags() {
        return tags;
    }

    /**
     * 设置标签
     *
     * @param tags 标签
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断核验结果是否通过
     *
     * @return true-通过，false-未通过
     */
    public boolean isPassed() {
        return this.resultStatus != null && this.resultStatus == 1;
    }

    /**
     * 判断核验结果是否不通过
     *
     * @return true-不通过，false-非不通过
     */
    public boolean isFailed() {
        return this.resultStatus != null && this.resultStatus == 2;
    }

    /**
     * 判断核验结果是否部分通过
     *
     * @return true-部分通过，false-非部分通过
     */
    public boolean isPartiallyPassed() {
        return this.resultStatus != null && this.resultStatus == 3;
    }

    /**
     * 判断是否需要补充材料
     *
     * @return true-需要补充材料，false-不需要补充材料
     */
    public boolean needsAdditionalMaterials() {
        return this.resultStatus != null && this.resultStatus == 4;
    }

    /**
     * 判断核验结果是否异常
     *
     * @return true-异常，false-正常
     */
    public boolean isAbnormal() {
        return this.resultStatus != null && this.resultStatus == 5;
    }

    /**
     * 判断是否为高风险
     *
     * @return true-高风险，false-非高风险
     */
    public boolean isHighRisk() {
        return this.riskLevel != null && this.riskLevel >= 3;
    }

    /**
     * 判断核验结果是否已过期
     *
     * @return true-已过期，false-未过期
     */
    public boolean isExpired() {
        if (this.validityPeriod == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.validityPeriod);
    }

    /**
     * 判断是否已审核
     *
     * @return true-已审核，false-未审核
     */
    public boolean isReviewed() {
        return this.reviewTime != null;
    }

    /**
     * 获取核验耗时（秒）
     *
     * @return 核验耗时，如果未开始或未结束返回0
     */
    public long getVerificationTimeInSeconds() {
        if (this.verificationStartTime == null || this.verificationEndTime == null) {
            return 0;
        }
        return java.time.Duration.between(this.verificationStartTime, this.verificationEndTime).getSeconds();
    }

    /**
     * 获取结果状态描述
     *
     * @return 结果状态描述
     */
    public String getResultStatusDescription() {
        if (this.resultStatus == null) {
            return "未知";
        }
        switch (this.resultStatus) {
            case 1: return "通过";
            case 2: return "不通过";
            case 3: return "部分通过";
            case 4: return "需要补充材料";
            case 5: return "异常";
            default: return "未知状态";
        }
    }

    /**
     * 获取风险等级描述
     *
     * @return 风险等级描述
     */
    public String getRiskLevelDescription() {
        if (this.riskLevel == null) {
            return "未知";
        }
        switch (this.riskLevel) {
            case 1: return "低风险";
            case 2: return "中风险";
            case 3: return "高风险";
            case 4: return "极高风险";
            default: return "未知风险等级";
        }
    }

    /**
     * 获取得分等级
     *
     * @return 得分等级
     */
    public String getScoreGrade() {
        if (this.score == null) {
            return "未评分";
        }
        BigDecimal scoreValue = this.score;
        if (scoreValue.compareTo(new BigDecimal("90")) >= 0) {
            return "优秀";
        } else if (scoreValue.compareTo(new BigDecimal("80")) >= 0) {
            return "良好";
        } else if (scoreValue.compareTo(new BigDecimal("70")) >= 0) {
            return "中等";
        } else if (scoreValue.compareTo(new BigDecimal("60")) >= 0) {
            return "及格";
        } else {
            return "不及格";
        }
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写toString方法
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "VerificationResult{" +
                "id=" + getId() +
                ", requestId=" + requestId +
                ", resultNo='" + resultNo + '\'' +
                ", resultStatus=" + resultStatus +
                ", score=" + score +
                ", verificationStartTime=" + verificationStartTime +
                ", verificationEndTime=" + verificationEndTime +
                ", processingTimeMs=" + processingTimeMs +
                ", verifierId=" + verifierId +
                ", verifierName='" + verifierName + '\'' +
                ", riskLevel=" + riskLevel +
                ", requiresRecheck=" + requiresRecheck +
                ", createdDate=" + getCreatedDate() +
                ", version=" + getVersion() +
                '}';
    }
}


