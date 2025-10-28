package com.honyrun.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 核验请求创建DTO
 *
 * 用于接收创建核验请求的请求参数，包含验证注解和数据校验规则
 * 支持响应式数据绑定和参数验证
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  21:10:00
 * @modified 2025-07-01 21:10:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class VerificationCreateRequest {

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
     * 请求参数
     * 可选，JSON格式的请求参数，长度不超过5000字符
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
     * 可选，1-低，2-中，3-高，4-紧急，默认为2
     */
    @Min(value = 1, message = "优先级最小值为1")
    @Max(value = 4, message = "优先级最大值为4")
    private Integer priority = 2;

    /**
     * 预期完成时间
     * 可选，必须是未来时间
     */
    private LocalDateTime expectedCompletionTime;

    /**
     * 附件信息
     * 可选，JSON格式的附件信息，长度不超过2000字符
     */
    @Size(max = 2000, message = "附件信息长度不能超过2000字符")
    private String attachments;

    /**
     * 标签
     * 可选，多个标签用逗号分隔，长度不超过200字符
     */
    @Size(max = 200, message = "标签长度不能超过200字符")
    private String tags;

    /**
     * 备注信息
     * 可选，长度不超过500字符
     */
    @Size(max = 500, message = "备注信息长度不能超过500字符")
    private String remark;

    /**
     * 默认构造函数
     */
    public VerificationCreateRequest() {
        this.priority = 2; // 默认中等优先级
    }

    /**
     * 带标题的构造函数
     *
     * @param title 请求标题
     */
    public VerificationCreateRequest(String title) {
        this();
        this.title = title;
    }

    // ==================== Getter和Setter方法 ====================

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
     * 获取备注信息
     *
     * @return 备注信息
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置备注信息
     *
     * @param remark 备注信息
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    // ==================== 验证方法 ====================

    /**
     * 验证优先级是否有效
     *
     * @return true-有效，false-无效
     */
    public boolean isValidPriority() {
        return this.priority != null && this.priority >= 1 && this.priority <= 4;
    }

    /**
     * 验证预期完成时间是否合理
     *
     * @return true-合理，false-不合理
     */
    public boolean isValidExpectedTime() {
        if (this.expectedCompletionTime == null) {
            return true; // 可选字段，null是有效的
        }
        return this.expectedCompletionTime.isAfter(LocalDateTime.now());
    }

    /**
     * 判断是否为高优先级请求
     *
     * @return true-高优先级，false-非高优先级
     */
    public boolean isHighPriority() {
        return this.priority != null && this.priority >= 3;
    }

    /**
     * 判断是否为紧急请求
     *
     * @return true-紧急，false-非紧急
     */
    public boolean isUrgent() {
        return this.priority != null && this.priority == 4;
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

    /**
     * 获取所有验证错误信息
     *
     * @return 验证错误信息列表
     */
    public java.util.List<String> getValidationErrors() {
        java.util.List<String> errors = new java.util.ArrayList<>();

        if (!isValidPriority()) {
            errors.add("优先级无效，必须在1-4之间");
        }

        if (!isValidExpectedTime()) {
            errors.add("预期完成时间必须是未来时间");
        }

        return errors;
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写equals方法
     * 基于标题和请求人ID进行比较
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

        VerificationCreateRequest that = (VerificationCreateRequest) obj;
        return Objects.equals(this.title, that.title) &&
               Objects.equals(this.requesterId, that.requesterId);
    }

    /**
     * 重写hashCode方法
     * 基于标题和请求人ID生成哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.title, this.requesterId);
    }

    /**
     * 重写toString方法
     * 提供请求DTO的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "VerificationCreateRequest{" +
                "requestType='" + requestType + '\'' +
                ", title='" + title + '\'' +
                ", requesterId=" + requesterId +
                ", requesterName='" + requesterName + '\'' +
                ", priority=" + priority +
                ", priorityDescription='" + getPriorityDescription() + '\'' +
                ", expectedCompletionTime=" + expectedCompletionTime +
                ", tags='" + tags + '\'' +
                ", validPriority=" + isValidPriority() +
                ", validExpectedTime=" + isValidExpectedTime() +
                ", highPriority=" + isHighPriority() +
                ", urgent=" + isUrgent() +
                '}';
    }
}


