package com.honyrun.model.entity.system;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 系统通知实体类
 *
 * 系统通知管理实体，支持通知内容、接收者、发送状态等功能
 * 继承AuditableEntity，支持R2DBC响应式数据访问和审计功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  19:05:00
 * @modified 2025-07-01 19:05:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("sys_system_notification")
public class SystemNotification extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 通知标题
     * 通知的标题
     */
    @Column("title")
    private String title;

    /**
     * 通知内容
     * 通知的详细内容
     */
    @Column("content")
    private String content;

    /**
     * 通知类型
     * SYSTEM-系统通知，ALERT-告警通知，MAINTENANCE-维护通知，ANNOUNCEMENT-公告通知
     */
    @Column("notification_type")
    private String notificationType;

    /**
     * 通知级别
     * INFO-信息，WARNING-警告，ERROR-错误，URGENT-紧急
     */
    @Column("notification_level")
    private String notificationLevel;

    /**
     * 发送方式
     * EMAIL-邮件，SMS-短信，PUSH-推送，WEBHOOK-网络钩子
     */
    @Column("send_method")
    private String sendMethod;

    /**
     * 接收者ID
     * 接收通知的用户ID
     */
    @Column("receiver_id")
    private Long receiverId;

    /**
     * 接收者姓名
     * 接收通知的用户姓名
     */
    @Column("receiver_name")
    private String receiverName;

    /**
     * 接收者地址
     * 接收通知的地址（邮箱、手机号等）
     */
    @Column("receiver_address")
    private String receiverAddress;

    /**
     * 发送状态
     * PENDING-待发送，SENDING-发送中，SUCCESS-发送成功，FAILED-发送失败
     */
    @Column("send_status")
    private String sendStatus;

    /**
     * 发送时间
     * 通知发送的时间
     */
    @Column("send_time")
    private LocalDateTime sendTime;

    /**
     * 计划发送时间
     * 计划发送通知的时间
     */
    @Column("scheduled_time")
    private LocalDateTime scheduledTime;

    /**
     * 重试次数
     * 发送失败后的重试次数
     */
    @Column("retry_count")
    private Integer retryCount;

    /**
     * 最大重试次数
     * 允许的最大重试次数
     */
    @Column("max_retry_count")
    private Integer maxRetryCount;

    /**
     * 失败原因
     * 发送失败的原因
     */
    @Column("failure_reason")
    private String failureReason;

    /**
     * 是否已读
     * 0-未读，1-已读
     */
    @Column("is_read")
    private Integer isRead;

    /**
     * 阅读时间
     * 通知被阅读的时间
     */
    @Column("read_time")
    private LocalDateTime readTime;

    /**
     * 过期时间
     * 通知的过期时间
     */
    @Column("expire_time")
    private LocalDateTime expireTime;

    /**
     * 模板ID
     * 使用的通知模板ID
     */
    @Column("template_id")
    private Long templateId;

    /**
     * 模板参数
     * 通知模板的参数（JSON格式）
     */
    @Column("template_params")
    private String templateParams;

    /**
     * 相关业务ID
     * 关联的业务数据ID
     */
    @Column("business_id")
    private Long businessId;

    /**
     * 相关业务类型
     * 关联的业务类型
     */
    @Column("business_type")
    private String businessType;

    /**
     * 通知标签
     * 通知的标签信息，多个标签用逗号分隔
     */
    @Column("tags")
    private String tags;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public SystemNotification() {
        super();
        this.sendStatus = "PENDING";
        this.retryCount = 0;
        this.maxRetryCount = 3;
        this.isRead = 0;
    }

    /**
     * 带参数的构造函数
     *
     * @param title 通知标题
     * @param content 通知内容
     * @param notificationType 通知类型
     * @param receiverId 接收者ID
     */
    public SystemNotification(String title, String content, String notificationType, Long receiverId) {
        this();
        this.title = title;
        this.content = content;
        this.notificationType = notificationType;
        this.receiverId = receiverId;
    }

    // ==================== Getter和Setter方法 ====================

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

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getNotificationLevel() {
        return notificationLevel;
    }

    public void setNotificationLevel(String notificationLevel) {
        this.notificationLevel = notificationLevel;
    }

    public String getSendMethod() {
        return sendMethod;
    }

    public void setSendMethod(String sendMethod) {
        this.sendMethod = sendMethod;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus) {
        this.sendStatus = sendStatus;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateParams() {
        return templateParams;
    }

    public void setTemplateParams(String templateParams) {
        this.templateParams = templateParams;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断是否为紧急通知
     */
    public boolean isUrgent() {
        return "URGENT".equalsIgnoreCase(this.notificationLevel);
    }

    /**
     * 判断是否已读
     */
    public boolean isRead() {
        return this.isRead != null && this.isRead == 1;
    }

    /**
     * 判断是否发送成功
     */
    public boolean isSendSuccess() {
        return "SUCCESS".equalsIgnoreCase(this.sendStatus);
    }

    /**
     * 判断是否发送失败
     */
    public boolean isSendFailed() {
        return "FAILED".equalsIgnoreCase(this.sendStatus);
    }

    /**
     * 判断是否已过期
     */
    public boolean isExpired() {
        return this.expireTime != null && LocalDateTime.now().isAfter(this.expireTime);
    }

    /**
     * 判断是否可以重试
     */
    public boolean canRetry() {
        return this.retryCount < this.maxRetryCount;
    }

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = 1;
        this.readTime = LocalDateTime.now();
    }

    /**
     * 标记发送成功
     */
    public void markSendSuccess() {
        this.sendStatus = "SUCCESS";
        this.sendTime = LocalDateTime.now();
    }

    /**
     * 标记发送失败
     */
    public void markSendFailed(String reason) {
        this.sendStatus = "FAILED";
        this.failureReason = reason;
        this.sendTime = LocalDateTime.now();
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        this.retryCount++;
    }

    /**
     * 开始发送
     */
    public void startSending() {
        this.sendStatus = "SENDING";
        this.sendTime = LocalDateTime.now();
    }

    /**
     * 获取通知摘要
     */
    public String getNotificationSummary() {
        return String.format("[%s] %s - To: %s (Status: %s)",
                            this.notificationLevel,
                            this.title,
                            this.receiverName,
                            this.sendStatus);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        SystemNotification that = (SystemNotification) obj;
        return Objects.equals(title, that.title) &&
               Objects.equals(receiverId, that.receiverId) &&
               Objects.equals(getCreatedDate(), that.getCreatedDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, receiverId, getCreatedDate());
    }

    @Override
    public String toString() {
        return "SystemNotification{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", notificationLevel='" + notificationLevel + '\'' +
                ", sendMethod='" + sendMethod + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", sendStatus='" + sendStatus + '\'' +
                ", sendTime=" + sendTime +
                ", isRead=" + isRead +
                '}';
    }
}


