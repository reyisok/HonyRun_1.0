package com.honyrun.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 核验响应DTO
 *
 * 用于返回核验请求的响应数据，包含核验请求和结果的完整信息
 * 支持响应式数据传输和JSON序列化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  21:12:00
 * @modified 2025-07-01 21:12:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class VerificationResponse {

    // ==================== 核验请求基本信息 ====================

    /**
     * 请求ID
     */
    private Long id;

    /**
     * 请求编号
     */
    private String requestNo;

    /**
     * 请求类型
     */
    private String requestType;

    /**
     * 请求标题
     */
    private String title;

    /**
     * 请求内容
     */
    private String content;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 请求人ID
     */
    private Long requesterId;

    /**
     * 请求人姓名
     */
    private String requesterName;

    /**
     * 处理状态 - 使用Integer类型以保持与业务实体VerificationRequest一致
     * 0: 待处理, 1: 处理中, 2: 已完成, 3: 已取消, 4: 处理失败
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 优先级
     * 1-低，2-中，3-高，4-紧急
     */
    private Integer priority;

    /**
     * 优先级描述
     */
    private String priorityDescription;

    /**
     * 预期完成时间
     */
    private LocalDateTime expectedCompletionTime;

    /**
     * 实际开始时间
     */
    private LocalDateTime actualStartTime;

    /**
     * 实际完成时间
     */
    private LocalDateTime actualCompletionTime;

    /**
     * 处理人ID
     */
    private Long processorId;

    /**
     * 处理人姓名
     */
    private String processorName;

    /**
     * 处理结果
     */
    private String processResult;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 附件信息
     */
    private String attachments;

    /**
     * 标签
     */
    private String tags;

    /**
     * 创建时间
     */
    private LocalDateTime createdDate;

    /**
     * 最后修改时间
     */
    private LocalDateTime lastModifiedDate;

    /**
     * 版本号
     */
    private Long version;

    // ==================== 核验结果信息 ====================

    /**
     * 结果ID
     */
    private Long resultId;

    /**
     * 结果编号
     */
    private String resultNo;

    /**
     * 核验结果状态
     * 1-通过，2-不通过，3-部分通过，4-需要补充材料，5-异常
     */
    private Integer resultStatus;

    /**
     * 结果状态描述
     */
    private String resultStatusDescription;

    /**
     * 核验结果描述
     */
    private String resultDescription;

    /**
     * 核验得分
     */
    private BigDecimal score;

    /**
     * 得分等级
     */
    private String scoreGrade;

    /**
     * 核验详情
     */
    private String resultDetails;

    /**
     * 核验开始时间
     */
    private LocalDateTime verificationStartTime;

    /**
     * 核验结束时间
     */
    private LocalDateTime verificationEndTime;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTimeMs;

    /**
     * 核验人员ID
     */
    private Long verifierId;

    /**
     * 核验人员姓名
     */
    private String verifierName;

    /**
     * 风险等级
     * 1-低风险，2-中风险，3-高风险，4-极高风险
     */
    private Integer riskLevel;

    /**
     * 风险等级描述
     */
    private String riskLevelDescription;

    /**
     * 风险描述
     */
    private String riskDescription;

    /**
     * 建议措施
     */
    private String recommendations;

    /**
     * 有效期
     */
    private LocalDateTime validityPeriod;

    /**
     * 是否需要复核
     */
    private Boolean requiresRecheck;

    /**
     * 复核原因
     */
    private String recheckReason;

    /**
     * 默认构造函数
     */
    public VerificationResponse() {
    }

    /**
     * 带ID的构造函数
     *
     * @param id 请求ID
     */
    public VerificationResponse(Long id) {
        this.id = id;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取请求ID
     *
     * @return 请求ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置请求ID
     *
     * @param id 请求ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取请求编号
     *
     * @return 请求编号
     */
    public String getRequestNo() {
        return requestNo;
    }

    /**
     * 设置请求编号
     *
     * @param requestNo 请求编号
     */
    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    /**
     * 获取请求类型
     *
     * @return 请求类型
     */
    public String getRequestType() {
        return requestType;
    }

    /**
     * 设置请求类型
     *
     * @param requestType 请求类型
     */
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    /**
     * 获取请求标题
     *
     * @return 请求标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置请求标题
     *
     * @param title 请求标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取请求内容
     *
     * @return 请求内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置请求内容
     *
     * @param content 请求内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取请求参数
     *
     * @return 请求参数
     */
    public String getRequestParams() {
        return requestParams;
    }

    /**
     * 设置请求参数
     *
     * @param requestParams 请求参数
     */
    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    /**
     * 获取请求人ID
     *
     * @return 请求人ID
     */
    public Long getRequesterId() {
        return requesterId;
    }

    /**
     * 设置请求人ID
     *
     * @param requesterId 请求人ID
     */
    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    /**
     * 获取请求人姓名
     *
     * @return 请求人姓名
     */
    public String getRequesterName() {
        return requesterName;
    }

    /**
     * 设置请求人姓名
     *
     * @param requesterName 请求人姓名
     */
    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    /**
     * 获取处理状态
     *
     * @return 处理状态
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置处理状态
     *
     * @param status 处理状态
     */
    public void setStatus(Integer status) {
        this.status = status;
        this.statusDescription = getStatusDescriptionByCode(status);
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
     */
    public String getStatusDescription() {
        return statusDescription;
    }

    /**
     * 设置状态描述
     *
     * @param statusDescription 状态描述
     */
    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    /**
     * 获取优先级
     *
     * @return 优先级
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * 设置优先级
     *
     * @param priority 优先级
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
        this.priorityDescription = getPriorityDescriptionByCode(priority);
    }

    /**
     * 获取优先级描述
     *
     * @return 优先级描述
     */
    public String getPriorityDescription() {
        return priorityDescription;
    }

    /**
     * 设置优先级描述
     *
     * @param priorityDescription 优先级描述
     */
    public void setPriorityDescription(String priorityDescription) {
        this.priorityDescription = priorityDescription;
    }

    /**
     * 获取预期完成时间
     *
     * @return 预期完成时间
     */
    public LocalDateTime getExpectedCompletionTime() {
        return expectedCompletionTime;
    }

    /**
     * 设置预期完成时间
     *
     * @param expectedCompletionTime 预期完成时间
     */
    public void setExpectedCompletionTime(LocalDateTime expectedCompletionTime) {
        this.expectedCompletionTime = expectedCompletionTime;
    }

    /**
     * 获取实际开始时间
     *
     * @return 实际开始时间
     */
    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }

    /**
     * 设置实际开始时间
     *
     * @param actualStartTime 实际开始时间
     */
    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    /**
     * 获取实际完成时间
     *
     * @return 实际完成时间
     */
    public LocalDateTime getActualCompletionTime() {
        return actualCompletionTime;
    }

    /**
     * 设置实际完成时间
     *
     * @param actualCompletionTime 实际完成时间
     */
    public void setActualCompletionTime(LocalDateTime actualCompletionTime) {
        this.actualCompletionTime = actualCompletionTime;
    }

    /**
     * 获取处理人ID
     *
     * @return 处理人ID
     */
    public Long getProcessorId() {
        return processorId;
    }

    /**
     * 设置处理人ID
     *
     * @param processorId 处理人ID
     */
    public void setProcessorId(Long processorId) {
        this.processorId = processorId;
    }

    /**
     * 获取处理人姓名
     *
     * @return 处理人姓名
     */
    public String getProcessorName() {
        return processorName;
    }

    /**
     * 设置处理人姓名
     *
     * @param processorName 处理人姓名
     */
    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    /**
     * 获取处理结果
     *
     * @return 处理结果
     */
    public String getProcessResult() {
        return processResult;
    }

    /**
     * 设置处理结果
     *
     * @param processResult 处理结果
     */
    public void setProcessResult(String processResult) {
        this.processResult = processResult;
    }

    /**
     * 获取失败原因
     *
     * @return 失败原因
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * 设置失败原因
     *
     * @param failureReason 失败原因
     */
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    /**
     * 获取附件信息
     *
     * @return 附件信息
     */
    public String getAttachments() {
        return attachments;
    }

    /**
     * 设置附件信息
     *
     * @param attachments 附件信息
     */
    public void setAttachments(String attachments) {
        this.attachments = attachments;
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

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    /**
     * 设置创建时间
     *
     * @param createdDate 创建时间
     */
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * 获取最后修改时间
     *
     * @return 最后修改时间
     */
    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * 设置最后修改时间
     *
     * @param lastModifiedDate 最后修改时间
     */
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * 获取版本号
     *
     * @return 版本号
     */
    public Long getVersion() {
        return version;
    }

    /**
     * 设置版本号
     *
     * @param version 版本号
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    // ==================== 核验结果相关Getter和Setter ====================

    /**
     * 获取结果ID
     *
     * @return 结果ID
     */
    public Long getResultId() {
        return resultId;
    }

    /**
     * 设置结果ID
     *
     * @param resultId 结果ID
     */
    public void setResultId(Long resultId) {
        this.resultId = resultId;
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
        this.resultStatusDescription = getResultStatusDescriptionByCode(resultStatus);
    }

    /**
     * 获取结果状态描述
     *
     * @return 结果状态描述
     */
    public String getResultStatusDescription() {
        return resultStatusDescription;
    }

    /**
     * 设置结果状态描述
     *
     * @param resultStatusDescription 结果状态描述
     */
    public void setResultStatusDescription(String resultStatusDescription) {
        this.resultStatusDescription = resultStatusDescription;
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
        this.scoreGrade = getScoreGradeByScore(score);
    }

    /**
     * 获取得分等级
     *
     * @return 得分等级
     */
    public String getScoreGrade() {
        return scoreGrade;
    }

    /**
     * 设置得分等级
     *
     * @param scoreGrade 得分等级
     */
    public void setScoreGrade(String scoreGrade) {
        this.scoreGrade = scoreGrade;
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
        this.riskLevelDescription = getRiskLevelDescriptionByCode(riskLevel);
    }

    /**
     * 获取风险等级描述
     *
     * @return 风险等级描述
     */
    public String getRiskLevelDescription() {
        return riskLevelDescription;
    }

    /**
     * 设置风险等级描述
     *
     * @param riskLevelDescription 风险等级描述
     */
    public void setRiskLevelDescription(String riskLevelDescription) {
        this.riskLevelDescription = riskLevelDescription;
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

    // ==================== 业务方法 ====================

    /**
     * 判断请求是否已完成
     *
     * @return true-已完成，false-未完成
     */
    public boolean isCompleted() {
        return this.status != null && this.status == 2;
    }

    /**
     * 判断请求是否为高优先级
     *
     * @return true-高优先级，false-非高优先级
     */
    public boolean isHighPriority() {
        return this.priority != null && this.priority >= 3;
    }

    /**
     * 判断是否有核验结果
     *
     * @return true-有结果，false-无结果
     */
    public boolean hasResult() {
        return this.resultId != null;
    }

    /**
     * 判断核验结果是否通过
     *
     * @return true-通过，false-未通过或无结果
     */
    public boolean isResultPassed() {
        return this.resultStatus != null && this.resultStatus == 1;
    }

    /**
     * 判断是否为高风险
     *
     * @return true-高风险，false-非高风险或无结果
     */
    public boolean isHighRisk() {
        return this.riskLevel != null && this.riskLevel >= 3;
    }

    /**
     * 获取处理耗时（秒）
     *
     * @return 处理耗时，如果未开始或未完成返回0
     */
    public long getProcessingTimeInSeconds() {
        if (this.actualStartTime == null) {
            return 0;
        }
        LocalDateTime endTime = this.actualCompletionTime != null ?
                               this.actualCompletionTime : LocalDateTime.now();
        return java.time.Duration.between(this.actualStartTime, endTime).getSeconds();
    }

    // ==================== 私有辅助方法 ====================

    private String getStatusDescriptionByCode(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待处理";
            case 1: return "处理中";
            case 2: return "已完成";
            case 3: return "已取消";
            case 4: return "处理失败";
            default: return "未知状态";
        }
    }

    private String getPriorityDescriptionByCode(Integer priority) {
        if (priority == null) {
            return "未知";
        }
        switch (priority) {
            case 1: return "低";
            case 2: return "中";
            case 3: return "高";
            case 4: return "紧急";
            default: return "未知优先级";
        }
    }

    private String getResultStatusDescriptionByCode(Integer resultStatus) {
        if (resultStatus == null) {
            return "未知";
        }
        switch (resultStatus) {
            case 1: return "通过";
            case 2: return "不通过";
            case 3: return "部分通过";
            case 4: return "需要补充材料";
            case 5: return "异常";
            default: return "未知状态";
        }
    }

    private String getRiskLevelDescriptionByCode(Integer riskLevel) {
        if (riskLevel == null) {
            return "未知";
        }
        switch (riskLevel) {
            case 1: return "低风险";
            case 2: return "中风险";
            case 3: return "高风险";
            case 4: return "极高风险";
            default: return "未知风险等级";
        }
    }

    private String getScoreGradeByScore(BigDecimal score) {
        if (score == null) {
            return "未评分";
        }
        if (score.compareTo(new BigDecimal("90")) >= 0) {
            return "优秀";
        } else if (score.compareTo(new BigDecimal("80")) >= 0) {
            return "良好";
        } else if (score.compareTo(new BigDecimal("70")) >= 0) {
            return "中等";
        } else if (score.compareTo(new BigDecimal("60")) >= 0) {
            return "及格";
        } else {
            return "不及格";
        }
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写equals方法
     * 基于请求ID进行比较
     *
     * @param obj 比较对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        VerificationResponse that = (VerificationResponse) obj;
        return Objects.equals(this.id, that.id);
    }

    /**
     * 重写hashCode方法
     * 基于请求ID生成哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    /**
     * 重写toString方法
     * 提供响应DTO的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "VerificationResponse{" +
                "id=" + id +
                ", requestNo='" + requestNo + '\'' +
                ", requestType='" + requestType + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", statusDescription='" + statusDescription + '\'' +
                ", priority=" + priority +
                ", priorityDescription='" + priorityDescription + '\'' +
                ", requesterName='" + requesterName + '\'' +
                ", processorName='" + processorName + '\'' +
                ", resultId=" + resultId +
                ", resultStatus=" + resultStatus +
                ", resultStatusDescription='" + resultStatusDescription + '\'' +
                ", score=" + score +
                ", scoreGrade='" + scoreGrade + '\'' +
                ", riskLevel=" + riskLevel +
                ", riskLevelDescription='" + riskLevelDescription + '\'' +
                ", completed=" + isCompleted() +
                ", highPriority=" + isHighPriority() +
                ", hasResult=" + hasResult() +
                ", resultPassed=" + isResultPassed() +
                ", highRisk=" + isHighRisk() +
                '}';
    }
}


