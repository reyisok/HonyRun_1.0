package com.honyrun.model.entity.business;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 业务功能3实体类
 *
 * 用于存储预留业务功能模块3的数据信息，支持R2DBC响应式数据访问。
 * 该实体为未来业务扩展预留，支持灵活的业务数据存储和管理。
 *
 * 特性：
 * - 支持R2DBC响应式数据访问
 * - 继承审计功能
 * - 预留业务扩展
 * - 灵活数据结构
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  15:55:00
 * @modified 2025-07-01 15:55:00
 * @version 2.0.0
 */
@Table("biz_business_function3")
public class BusinessFunction3 extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 业务编号
     * 唯一标识业务功能3的业务编号
     */
    @Column("business_no")
    private String businessNo;

    /**
     * 业务名称
     * 业务功能3的名称标识
     */
    @Column("business_name")
    private String businessName;

    /**
     * 业务类型
     * 区分不同类型的业务功能3
     */
    @Column("business_type")
    private String businessType;

    /**
     * 业务状态
     * DRAFT-草稿, ACTIVE-活跃, INACTIVE-非活跃, COMPLETED-已完成, CANCELLED-已取消
     */
    @Column("status")
    private BusinessStatus status = BusinessStatus.DRAFT;

    /**
     * 业务描述
     * 详细的业务功能描述
     */
    @Column("description")
    private String description;

    /**
     * 业务内容
     * 具体的业务处理内容
     */
    @Column("content")
    private String content;

    /**
     * 业务参数
     * JSON格式的业务处理参数
     */
    @Column("parameters")
    private String parameters;

    /**
     * 业务规则
     * JSON格式的业务规则配置
     */
    @Column("rules")
    private String rules;

    /**
     * 执行模式
     * MANUAL-手动, AUTO-自动, SCHEDULED-定时
     */
    @Column("execution_mode")
    private ExecutionMode executionMode = ExecutionMode.MANUAL;

    /**
     * 重要程度
     * CRITICAL-关键, IMPORTANT-重要, NORMAL-普通, LOW-低
     */
    @Column("importance")
    private Importance importance = Importance.NORMAL;

    /**
     * 业务分组
     * 用于业务分组管理
     */
    @Column("business_group")
    private String businessGroup;

    /**
     * 负责人ID
     * 业务负责人的用户ID
     */
    @Column("owner_id")
    private Long ownerId;

    /**
     * 计划开始时间
     * 业务计划开始执行的时间
     */
    @Column("planned_start_time")
    private LocalDateTime plannedStartTime;

    /**
     * 计划结束时间
     * 业务计划完成的时间
     */
    @Column("planned_end_time")
    private LocalDateTime plannedEndTime;

    /**
     * 实际开始时间
     * 业务实际开始执行的时间
     */
    @Column("actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     * 业务实际完成的时间
     */
    @Column("actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * 进度百分比
     * 业务完成进度（0-100）
     */
    @Column("progress_percentage")
    private Integer progressPercentage = 0;

    /**
     * 质量评分
     * 业务执行质量评分（0-100）
     */
    @Column("quality_score")
    private Integer qualityScore;

    /**
     * 执行结果
     * 业务执行的结果信息
     */
    @Column("execution_result")
    private String executionResult;

    /**
     * 备注信息
     * 额外的备注说明
     */
    @Column("notes")
    private String notes;

    /**
     * 默认构造函数
     */
    public BusinessFunction3() {
        super();
    }

    /**
     * 带参数的构造函数
     *
     * @param businessNo 业务编号
     * @param businessName 业务名称
     * @param businessType 业务类型
     */
    public BusinessFunction3(String businessNo, String businessName, String businessType) {
        super();
        this.businessNo = businessNo;
        this.businessName = businessName;
        this.businessType = businessType;
    }

    // Getter and Setter methods

    public String getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(String businessNo) {
        this.businessNo = businessNo;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public BusinessStatus getStatus() {
        return status;
    }

    public void setStatus(BusinessStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }

    public Importance getImportance() {
        return importance;
    }

    public void setImportance(Importance importance) {
        this.importance = importance;
    }

    public String getBusinessGroup() {
        return businessGroup;
    }

    public void setBusinessGroup(String businessGroup) {
        this.businessGroup = businessGroup;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDateTime getPlannedStartTime() {
        return plannedStartTime;
    }

    public void setPlannedStartTime(LocalDateTime plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }

    public LocalDateTime getPlannedEndTime() {
        return plannedEndTime;
    }

    public void setPlannedEndTime(LocalDateTime plannedEndTime) {
        this.plannedEndTime = plannedEndTime;
    }

    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public Integer getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(Integer qualityScore) {
        this.qualityScore = qualityScore;
    }

    public String getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(String executionResult) {
        this.executionResult = executionResult;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Business methods

    /**
     * 判断业务是否活跃
     *
     * @return 如果活跃返回true，否则返回false
     */
    public boolean isActive() {
        return this.status == BusinessStatus.ACTIVE;
    }

    /**
     * 判断业务是否已完成
     *
     * @return 如果已完成返回true，否则返回false
     */
    public boolean isCompleted() {
        return this.status == BusinessStatus.COMPLETED;
    }

    /**
     * 判断业务是否已取消
     *
     * @return 如果已取消返回true，否则返回false
     */
    public boolean isCancelled() {
        return this.status == BusinessStatus.CANCELLED;
    }

    /**
     * 判断是否为自动执行模式
     *
     * @return 如果是自动执行返回true，否则返回false
     */
    public boolean isAutoExecution() {
        return this.executionMode == ExecutionMode.AUTO;
    }

    /**
     * 判断是否为关键业务
     *
     * @return 如果是关键业务返回true，否则返回false
     */
    public boolean isCritical() {
        return this.importance == Importance.CRITICAL;
    }

    /**
     * 开始执行业务
     */
    public void start() {
        this.status = BusinessStatus.ACTIVE;
        this.actualStartTime = LocalDateTime.now();
        this.progressPercentage = 0;
    }

    /**
     * 完成业务
     *
     * @param result 执行结果
     */
    public void complete(String result) {
        this.status = BusinessStatus.COMPLETED;
        this.actualEndTime = LocalDateTime.now();
        this.progressPercentage = 100;
        this.executionResult = result;
    }

    /**
     * 取消业务
     *
     * @param reason 取消原因
     */
    public void cancel(String reason) {
        this.status = BusinessStatus.CANCELLED;
        this.actualEndTime = LocalDateTime.now();
        this.executionResult = "已取消: " + reason;
    }

    /**
     * 更新进度
     *
     * @param percentage 进度百分比
     */
    public void updateProgress(Integer percentage) {
        if (percentage != null && percentage >= 0 && percentage <= 100) {
            this.progressPercentage = percentage;
            if (percentage == 100 && this.status == BusinessStatus.ACTIVE) {
                this.status = BusinessStatus.COMPLETED;
                this.actualEndTime = LocalDateTime.now();
            }
        }
    }

    /**
     * 业务状态枚举
     */
    public enum BusinessStatus {
        /** 草稿 */
        DRAFT("草稿"),
        /** 活跃 */
        ACTIVE("活跃"),
        /** 非活跃 */
        INACTIVE("非活跃"),
        /** 已完成 */
        COMPLETED("已完成"),
        /** 已取消 */
        CANCELLED("已取消");

        private final String description;

        BusinessStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 执行模式枚举
     */
    public enum ExecutionMode {
        /** 手动 */
        MANUAL("手动"),
        /** 自动 */
        AUTO("自动"),
        /** 定时 */
        SCHEDULED("定时");

        private final String description;

        ExecutionMode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 重要程度枚举
     */
    public enum Importance {
        /** 关键 */
        CRITICAL("关键"),
        /** 重要 */
        IMPORTANT("重要"),
        /** 普通 */
        NORMAL("普通"),
        /** 低 */
        LOW("低");

        private final String description;

        Importance(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return String.format("BusinessFunction3{id=%d, businessNo='%s', businessName='%s', status=%s, progress=%d%%}",
                getId(), businessNo, businessName, status, progressPercentage);
    }
}


