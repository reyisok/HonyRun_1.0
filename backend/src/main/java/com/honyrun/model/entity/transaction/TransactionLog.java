package com.honyrun.model.entity.transaction;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 事务日志实体类
 *
 * 用于记录系统事务执行的详细信息，支持事务监控和性能分析
 * 继承AuditableEntity，支持R2DBC响应式数据访问和审计功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 01:50:00
 * @modified 2025-07-01 01:50:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("sys_transaction_log")
public class TransactionLog extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 事务ID
     * 唯一标识一个事务
     */
    @Column("transaction_id")
    private String transactionId;

    /**
     * 事务类型
     * 如：USER_CREATION、DATA_UPDATE、BATCH_OPERATION等
     */
    @Column("transaction_type")
    private String transactionType;

    /**
     * 事务状态
     * SUCCESS、FAILED、RUNNING、ROLLBACK等
     */
    @Column("status")
    private String status;

    /**
     * 开始时间
     */
    @Column("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column("end_time")
    private LocalDateTime endTime;

    /**
     * 执行时长（毫秒）
     */
    @Column("duration")
    private Long duration;

    /**
     * 用户ID
     * 执行事务的用户
     */
    @Column("user_id")
    private String userId;

    /**
     * 操作详情
     * 事务执行的具体操作描述
     */
    @Column("operation_details")
    private String operationDetails;

    /**
     * 错误信息
     * 事务失败时的错误描述
     */
    @Column("error_message")
    private String errorMessage;

    /**
     * 影响行数
     * 事务影响的数据行数
     */
    @Column("affected_rows")
    private Integer affectedRows;

    /**
     * 事务级别
     * 事务的隔离级别
     */
    @Column("isolation_level")
    private String isolationLevel;

    /**
     * 传播行为
     * 事务的传播行为
     */
    @Column("propagation_behavior")
    private String propagationBehavior;

    // 构造函数
    public TransactionLog() {
        super();
    }

    public TransactionLog(String transactionId, String transactionType, String status) {
        this();
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.status = status;
        this.startTime = LocalDateTime.now();
    }

    // Getter 和 Setter 方法
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOperationDetails() {
        return operationDetails;
    }

    public void setOperationDetails(String operationDetails) {
        this.operationDetails = operationDetails;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getAffectedRows() {
        return affectedRows;
    }

    public void setAffectedRows(Integer affectedRows) {
        this.affectedRows = affectedRows;
    }

    public String getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(String isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public String getPropagationBehavior() {
        return propagationBehavior;
    }

    public void setPropagationBehavior(String propagationBehavior) {
        this.propagationBehavior = propagationBehavior;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TransactionLog that = (TransactionLog) o;
        return Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(transactionType, that.transactionType) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), transactionId, transactionType, status);
    }

    @Override
    public String toString() {
        return "TransactionLog{" +
                "id=" + getId() +
                ", transactionId='" + transactionId + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", status='" + status + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + duration +
                ", userId='" + userId + '\'' +
                ", operationDetails='" + operationDetails + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", affectedRows=" + affectedRows +
                ", isolationLevel='" + isolationLevel + '\'' +
                ", propagationBehavior='" + propagationBehavior + '\'' +
                '}';
    }
}

