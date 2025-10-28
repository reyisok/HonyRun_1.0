package com.honyrun.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 核验请求DTO
 *
 * 用于处理核验业务的请求参数，包含验证注解和数据校验规则
 * 支持响应式数据绑定和参数验证，用于Handler中的请求处理
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  17:00:00
 * @modified 2025-07-01 17:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class VerificationRequest {

    /**
     * 请求ID（更新时使用）
     */
    private Long id;

    /**
     * 请求类型
     * 必填，长度2-50字符
     */
    @NotBlank(message = "请求类型不能为空")
    @Size(min = 2, max = 50, message = "请求类型长度必须在2-50字符之间")
    private String requestType;

    /**
     * 请求标题
     * 必填，长度5-200字符
     */
    @NotBlank(message = "请求标题不能为空")
    @Size(min = 5, max = 200, message = "请求标题长度必须在5-200字符之间")
    private String title;

    /**
     * 请求内容
     * 必填，长度10-2000字符
     */
    @NotBlank(message = "请求内容不能为空")
    @Size(min = 10, max = 2000, message = "请求内容长度必须在10-2000字符之间")
    private String content;

    /**
     * 请求参数（JSON格式）
     * 可选，最大5000字符
     */
    @Size(max = 5000, message = "请求参数长度不能超过5000字符")
    private String requestParams;

    /**
     * 请求人ID
     * 必填，必须大于0
     */
    @NotNull(message = "请求人ID不能为空")
    @Min(value = 1, message = "请求人ID必须大于0")
    private Long requesterId;

    /**
     * 请求人姓名
     * 必填，长度2-50字符
     */
    @NotBlank(message = "请求人姓名不能为空")
    @Size(min = 2, max = 50, message = "请求人姓名长度必须在2-50字符之间")
    private String requesterName;

    /**
     * 优先级
     * 1-紧急，2-高，3-中，4-低，默认为2（中等）
     */
    @Min(value = 1, message = "优先级最小值为1")
    @Max(value = 4, message = "优先级最大值为4")
    private Integer priority = 2;

    /**
     * 期望完成时间
     * 可选
     */
    private LocalDateTime expectedCompletionTime;

    /**
     * 附件信息（JSON格式）
     * 可选，最大2000字符
     */
    @Size(max = 2000, message = "附件信息长度不能超过2000字符")
    private String attachments;

    /**
     * 标签
     * 可选，最大200字符
     */
    @Size(max = 200, message = "标签长度不能超过200字符")
    private String tags;

    /**
     * 备注信息
     * 可选，最大500字符
     */
    @Size(max = 500, message = "备注信息长度不能超过500字符")
    private String remark;

    /**
     * 默认构造函数
     */
    public VerificationRequest() {
        this.priority = 2; // 默认中等优先级
    }

    /**
     * 带标题的构造函数
     *
     * @param title 请求标题
     */
    public VerificationRequest(String title) {
        this();
        this.title = title;
    }

    // ==================== Getter和Setter方法 ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDateTime getExpectedCompletionTime() {
        return expectedCompletionTime;
    }

    public void setExpectedCompletionTime(LocalDateTime expectedCompletionTime) {
        this.expectedCompletionTime = expectedCompletionTime;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    // ==================== 业务方法 ====================

    /**
     * 检查优先级是否有效
     *
     * @return 优先级是否在有效范围内
     */
    public boolean isValidPriority() {
        return priority != null && priority >= 1 && priority <= 4;
    }

    /**
     * 检查期望完成时间是否有效
     *
     * @return 期望完成时间是否在未来
     */
    public boolean isValidExpectedTime() {
        return expectedCompletionTime == null || expectedCompletionTime.isAfter(LocalDateTime.now());
    }

    /**
     * 是否为高优先级
     *
     * @return 是否为高优先级（1-紧急，2-高）
     */
    public boolean isHighPriority() {
        return priority != null && priority <= 2;
    }

    /**
     * 是否为紧急请求
     *
     * @return 是否为紧急请求
     */
    public boolean isUrgent() {
        return priority != null && priority == 1;
    }

    /**
     * 获取优先级描述
     *
     * @return 优先级描述文字
     */
    public String getPriorityDescription() {
        if (priority == null) {
            return "未设置";
        }
        switch (priority) {
            case 1: return "紧急";
            case 2: return "高";
            case 3: return "中";
            case 4: return "低";
            default: return "未知";
        }
    }

    // ==================== Object方法重写 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VerificationRequest that = (VerificationRequest) obj;
        return Objects.equals(id, that.id) &&
               Objects.equals(requestType, that.requestType) &&
               Objects.equals(title, that.title) &&
               Objects.equals(content, that.content) &&
               Objects.equals(requesterId, that.requesterId) &&
               Objects.equals(priority, that.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, requestType, title, content, requesterId, priority);
    }

    @Override
    public String toString() {
        return "VerificationRequest{" +
                "id=" + id +
                ", requestType='" + requestType + '\'' +
                ", title='" + title + '\'' +
                ", requesterId=" + requesterId +
                ", requesterName='" + requesterName + '\'' +
                ", priority=" + priority +
                ", expectedCompletionTime=" + expectedCompletionTime +
                '}';
    }
}


