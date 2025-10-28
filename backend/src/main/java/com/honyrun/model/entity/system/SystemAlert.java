package com.honyrun.model.entity.system;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 系统告警实体类
 *
 * 系统告警管理实体，支持告警信息、级别分类、处理状态等功能
 * 继承AuditableEntity，支持R2DBC响应式数据访问和审计功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  21:03:21
 * @modified 2025-07-01 21:03:21
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("sys_system_alert")
public class SystemAlert extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 告警标题
     * 告警的简要标题
     */
    @Column("alert_title")
    private String alertTitle;

    /**
     * 告警内容
     * 告警的详细内容描述
     */
    @Column("alert_content")
    private String alertContent;

    /**
     * 告警级别
     * INFO-信息，WARNING-警告，ERROR-错误，CRITICAL-严重
     */
    @Column("alert_level")
    private String alertLevel;

    /**
     * 告警类型
     * SYSTEM-系统告警，PERFORMANCE-性能告警，SECURITY-安全告警，BUSINESS-业务告警
     */
    @Column("alert_type")
    private String alertType;

    /**
     * 告警来源
     * 产生告警的系统模块或组件
     */
    @Column("alert_source")
    private String alertSource;

    /**
     * 告警时间
     * 告警产生的时间
     */
    @Column("alert_time")
    private LocalDateTime alertTime;

    /**
     * 处理状态
     * PENDING-待处理，PROCESSING-处理中，RESOLVED-已解决，IGNORED-已忽略
     */
    @Column("status")
    private String status;

    /**
     * 处理人ID
     * 处理告警的用户ID
     */
    @Column("handler_id")
    private Long handlerId;

    /**
     * 处理人姓名
     * 处理告警的用户姓名
     */
    @Column("handler_name")
    private String handlerName;

    /**
     * 处理时间
     * 告警处理的时间
     */
    @Column("handle_time")
    private LocalDateTime handleTime;

    /**
     * 处理备注
     * 告警处理的备注信息
     */
    @Column("handle_remark")
    private String handleRemark;

    /**
     * 解决时间
     * 告警解决的时间
     */
    @Column("resolve_time")
    private LocalDateTime resolveTime;

    /**
     * 解决方案
     * 告警的解决方案描述
     */
    @Column("solution")
    private String solution;

    /**
     * 是否已通知
     * 0-未通知，1-已通知
     */
    @Column("notified")
    private Integer notified;

    /**
     * 通知方式
     * 告警通知的方式（EMAIL、SMS、WEBHOOK等）
     */
    @Column("notification_method")
    private String notificationMethod;

    /**
     * 通知时间
     * 告警通知的时间
     */
    @Column("notification_time")
    private LocalDateTime notificationTime;

    /**
     * 重复次数
     * 相同告警的重复次数
     */
    @Column("repeat_count")
    private Integer repeatCount;

    /**
     * 最后重复时间
     * 最后一次重复告警的时间
     */
    @Column("last_repeat_time")
    private LocalDateTime lastRepeatTime;

    /**
     * 告警规则ID
     * 触发告警的规则ID
     */
    @Column("rule_id")
    private Long ruleId;

    /**
     * 告警规则名称
     * 触发告警的规则名称
     */
    @Column("rule_name")
    private String ruleName;

    /**
     * 相关数据
     * 告警相关的数据信息（JSON格式）
     */
    @Column("related_data")
    private String relatedData;

    /**
     * 告警标签
     * 告警的标签信息，多个标签用逗号分隔
     */
    @Column("tags")
    private String tags;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public SystemAlert() {
        super();
        this.alertTime = LocalDateTime.now();
        this.status = "PENDING";
        this.notified = 0;
        this.repeatCount = 1;
    }

    /**
     * 带参数的构造函数
     *
     * @param alertTitle 告警标题
     * @param alertContent 告警内容
     * @param alertLevel 告警级别
     * @param alertType 告警类型
     */
    public SystemAlert(String alertTitle, String alertContent, String alertLevel, String alertType) {
        this();
        this.alertTitle = alertTitle;
        this.alertContent = alertContent;
        this.alertLevel = alertLevel;
        this.alertType = alertType;
    }

    // ==================== Getter和Setter方法 ====================

    public String getAlertTitle() {
        return alertTitle;
    }

    public void setAlertTitle(String alertTitle) {
        this.alertTitle = alertTitle;
    }

    public String getAlertContent() {
        return alertContent;
    }

    public void setAlertContent(String alertContent) {
        this.alertContent = alertContent;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getAlertSource() {
        return alertSource;
    }

    public void setAlertSource(String alertSource) {
        this.alertSource = alertSource;
    }

    public LocalDateTime getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(LocalDateTime alertTime) {
        this.alertTime = alertTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(Long handlerId) {
        this.handlerId = handlerId;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public LocalDateTime getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(LocalDateTime handleTime) {
        this.handleTime = handleTime;
    }

    public String getHandleRemark() {
        return handleRemark;
    }

    public void setHandleRemark(String handleRemark) {
        this.handleRemark = handleRemark;
    }

    public LocalDateTime getResolveTime() {
        return resolveTime;
    }

    public void setResolveTime(LocalDateTime resolveTime) {
        this.resolveTime = resolveTime;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public Integer getNotified() {
        return notified;
    }

    public void setNotified(Integer notified) {
        this.notified = notified;
    }

    public String getNotificationMethod() {
        return notificationMethod;
    }

    public void setNotificationMethod(String notificationMethod) {
        this.notificationMethod = notificationMethod;
    }

    public LocalDateTime getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(LocalDateTime notificationTime) {
        this.notificationTime = notificationTime;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public LocalDateTime getLastRepeatTime() {
        return lastRepeatTime;
    }

    public void setLastRepeatTime(LocalDateTime lastRepeatTime) {
        this.lastRepeatTime = lastRepeatTime;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRelatedData() {
        return relatedData;
    }

    public void setRelatedData(String relatedData) {
        this.relatedData = relatedData;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断是否为严重告警
     */
    public boolean isCritical() {
        return "CRITICAL".equalsIgnoreCase(this.alertLevel);
    }

    /**
     * 判断是否为错误告警
     */
    public boolean isError() {
        return "ERROR".equalsIgnoreCase(this.alertLevel);
    }

    /**
     * 判断是否为警告告警
     */
    public boolean isWarning() {
        return "WARNING".equalsIgnoreCase(this.alertLevel);
    }

    /**
     * 判断是否已处理
     */
    public boolean isResolved() {
        return "RESOLVED".equalsIgnoreCase(this.status);
    }

    /**
     * 判断是否待处理
     */
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(this.status);
    }

    /**
     * 判断是否已通知
     */
    public boolean isNotified() {
        return this.notified != null && this.notified == 1;
    }

    /**
     * 开始处理告警
     */
    public void startHandle(Long handlerId, String handlerName) {
        this.status = "PROCESSING";
        this.handlerId = handlerId;
        this.handlerName = handlerName;
        this.handleTime = LocalDateTime.now();
    }

    /**
     * 解决告警
     */
    public void resolve(String solution) {
        this.status = "RESOLVED";
        this.solution = solution;
        this.resolveTime = LocalDateTime.now();
    }

    /**
     * 忽略告警
     */
    public void ignore(String remark) {
        this.status = "IGNORED";
        this.handleRemark = remark;
        this.handleTime = LocalDateTime.now();
    }

    /**
     * 标记已通知
     */
    public void markNotified(String method) {
        this.notified = 1;
        this.notificationMethod = method;
        this.notificationTime = LocalDateTime.now();
    }

    /**
     * 增加重复次数
     */
    public void incrementRepeatCount() {
        if (this.repeatCount == null) {
            this.repeatCount = 1;
        } else {
            this.repeatCount++;
        }
        this.lastRepeatTime = LocalDateTime.now();
    }

    /**
     * 计算告警持续时间（分钟）
     */
    public long getDurationMinutes() {
        LocalDateTime endTime = this.resolveTime != null ? this.resolveTime : LocalDateTime.now();
        return java.time.Duration.between(this.alertTime, endTime).toMinutes();
    }

    /**
     * 获取告警摘要
     */
    public String getAlertSummary() {
        return String.format("[%s] %s - %s (Status: %s, Duration: %d min)",
                            this.alertLevel,
                            this.alertTitle,
                            this.alertSource,
                            this.status,
                            getDurationMinutes());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        SystemAlert that = (SystemAlert) obj;
        return Objects.equals(alertTitle, that.alertTitle) &&
               Objects.equals(alertTime, that.alertTime) &&
               Objects.equals(alertSource, that.alertSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), alertTitle, alertTime, alertSource);
    }

    @Override
    public String toString() {
        return "SystemAlert{" +
                "id=" + getId() +
                ", alertTitle='" + alertTitle + '\'' +
                ", alertLevel='" + alertLevel + '\'' +
                ", alertType='" + alertType + '\'' +
                ", alertSource='" + alertSource + '\'' +
                ", alertTime=" + alertTime +
                ", status='" + status + '\'' +
                ", handlerName='" + handlerName + '\'' +
                ", repeatCount=" + repeatCount +
                '}';
    }
}



