package com.honyrun.model.entity.business;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 核验请求实体类
 *
 * 用于存储核验业务的请求信息，包含请求参数、状态管理、处理结果等
 * 支持R2DBC响应式数据访问和审计功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  20:54:00
 * @modified 2025-07-01 20:54:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("biz_verification_request")
public class VerificationRequest extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 请求编号
     * 唯一标识核验请求，格式：VR + yyyyMMddHHmmss + 4位随机数
     */
    @Column("request_no")
    private String requestNo;

    /**
     * 请求类型
     * 核验业务的类型分类，如：身份核验、资质核验等
     */
    @Column("request_type")
    private String requestType;

    /**
     * 请求标题
     * 核验请求的简要描述
     */
    @Column("title")
    private String title;

    /**
     * 请求内容
     * 详细的核验请求内容和参数
     */
    @Column("content")
    private String content;

    /**
     * 请求参数
     * JSON格式存储的请求参数
     */
    @Column("request_params")
    private String requestParams;

    /**
     * 请求人ID
     * 发起核验请求的用户ID
     */
    @Column("requester_id")
    private Long requesterId;

    /**
     * 请求人姓名
     * 发起核验请求的用户姓名
     */
    @Column("requester_name")
    private String requesterName;

    /**
     * 处理状态 - 使用Integer类型以保持与数据库INTEGER字段一致
     * 0: 待处理, 1: 处理中, 2: 已完成, 3: 已取消, 4: 处理失败
     */
    @Column("status")
    private Integer status;

    /**
     * 优先级
     * 1-低，2-中，3-高，4-紧急
     */
    @Column("priority")
    private Integer priority;

    /**
     * 预期完成时间
     * 期望的核验完成时间
     */
    @Column("expected_completion_time")
    private LocalDateTime expectedCompletionTime;

    /**
     * 实际开始时间
     * 核验处理的实际开始时间
     */
    @Column("actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * 实际完成时间
     * 核验处理的实际完成时间
     */
    @Column("actual_completion_time")
    private LocalDateTime actualCompletionTime;

    /**
     * 处理人ID
     * 处理核验请求的用户ID
     */
    @Column("processor_id")
    private Long processorId;

    /**
     * 处理人姓名
     * 处理核验请求的用户姓名
     */
    @Column("processor_name")
    private String processorName;

    /**
     * 处理结果
     * 核验处理的结果描述
     */
    @Column("process_result")
    private String processResult;

    /**
     * 失败原因
     * 如果处理失败，记录失败原因
     */
    @Column("failure_reason")
    private String failureReason;

    /**
     * 附件信息
     * JSON格式存储的附件信息
     */
    @Column("attachments")
    private String attachments;

    /**
     * 标签
     * 用于分类和检索的标签，多个标签用逗号分隔
     */
    @Column("tags")
    private String tags;

    /**
     * 默认构造函数
     */
    public VerificationRequest() {
        super();
        this.status = 0; // 默认状态为待处理
        this.priority = 2; // 默认优先级为中等
    }

    /**
     * 带ID的构造函数
     *
     * @param id 主键ID
     */
    public VerificationRequest(Long id) {
        super(id);
        this.status = 0;
        this.priority = 2;
    }

    // ==================== Getter和Setter方法 ====================

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

    // ==================== 业务方法 ====================

    /**
     * 判断请求是否为待处理状态
     *
     * @return true-待处理，false-非待处理
     */
    public boolean isPending() {
        return this.status != null && this.status == 0;
    }

    /**
     * 判断请求是否为处理中状态
     *
     * @return true-处理中，false-非处理中
     */
    public boolean isProcessing() {
        return this.status != null && this.status == 1;
    }

    /**
     * 判断请求是否已完成
     *
     * @return true-已完成，false-未完成
     */
    public boolean isCompleted() {
        return this.status != null && this.status == 2;
    }

    /**
     * 判断请求是否已取消
     *
     * @return true-已取消，false-未取消
     */
    public boolean isCancelled() {
        return this.status != null && this.status == 3;
    }

    /**
     * 判断请求是否处理失败
     *
     * @return true-处理失败，false-非处理失败
     */
    public boolean isFailed() {
        return this.status != null && this.status == 4;
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
     * 判断请求是否超时
     *
     * @return true-超时，false-未超时
     */
    public boolean isOverdue() {
        if (this.expectedCompletionTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.expectedCompletionTime) && !isCompleted();
    }

    /**
     * 获取处理耗时（分钟）
     *
     * @return 处理耗时，如果未开始或未完成返回0
     */
    public long getProcessingTimeInMinutes() {
        if (this.actualStartTime == null) {
            return 0;
        }
        LocalDateTime endTime = this.actualCompletionTime != null ?
                               this.actualCompletionTime : LocalDateTime.now();
        return java.time.Duration.between(this.actualStartTime, endTime).toMinutes();
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
     */
    public String getStatusDescription() {
        if (this.status == null) {
            return "未知";
        }
        switch (this.status) {
            case 0: return "待处理";
            case 1: return "处理中";
            case 2: return "已完成";
            case 3: return "已取消";
            case 4: return "处理失败";
            default: return "未知状态";
        }
    }

    /**
     * 获取优先级描述
     *
     * @return 优先级描述
     */
    public String getPriorityDescription() {
        if (this.priority == null) {
            return "未知";
        }
        switch (this.priority) {
            case 1: return "低";
            case 2: return "中";
            case 3: return "高";
            case 4: return "紧急";
            default: return "未知优先级";
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
        return "VerificationRequest{" +
                "id=" + getId() +
                ", requestNo='" + requestNo + '\'' +
                ", requestType='" + requestType + '\'' +
                ", title='" + title + '\'' +
                ", requesterId=" + requesterId +
                ", requesterName='" + requesterName + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", expectedCompletionTime=" + expectedCompletionTime +
                ", actualStartTime=" + actualStartTime +
                ", actualCompletionTime=" + actualCompletionTime +
                ", processorId=" + processorId +
                ", processorName='" + processorName + '\'' +
                ", createdDate=" + getCreatedDate() +
                ", version=" + getVersion() +
                '}';
    }
}


