package com.honyrun.model.entity.business;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 业务功能2实体类
 *
 * 用于存储预留业务功能模块2的数据信息，支持R2DBC响应式数据访问。
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
 * @created 2025-07-01  15:50:00
 * @modified 2025-07-01 15:50:00
 * @version 2.0.0
 */
@Table("biz_business_function2")
public class BusinessFunction2 extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 业务编号
     * 唯一标识业务功能2的业务编号
     */
    @Column("business_no")
    private String businessNo;

    /**
     * 业务名称
     * 业务功能2的名称标识
     */
    @Column("business_name")
    private String businessName;

    /**
     * 业务类型
     * 区分不同类型的业务功能2
     */
    @Column("business_type")
    private String businessType;

    /**
     * 业务状态
     * ACTIVE-活跃, INACTIVE-非活跃, SUSPENDED-暂停, ARCHIVED-归档
     */
    @Column("status")
    private BusinessStatus status = BusinessStatus.ACTIVE;

    /**
     * 业务描述
     * 详细的业务功能描述
     */
    @Column("description")
    private String description;

    /**
     * 业务数据
     * JSON格式的业务相关数据
     */
    @Column("business_data")
    private String businessData;

    /**
     * 配置信息
     * JSON格式的业务配置参数
     */
    @Column("config_data")
    private String configData;

    /**
     * 优先级
     * HIGH-高, MEDIUM-中, LOW-低
     */
    @Column("priority")
    private Priority priority = Priority.MEDIUM;

    /**
     * 分类标签
     * 用于业务分类和检索
     */
    @Column("category")
    private String category;

    /**
     * 标签
     * 多个标签用逗号分隔
     */
    @Column("tags")
    private String tags;

    /**
     * 开始时间
     * 业务功能的开始生效时间
     */
    @Column("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     * 业务功能的结束时间
     */
    @Column("end_time")
    private LocalDateTime endTime;

    /**
     * 处理次数
     * 业务功能被处理的次数统计
     */
    @Column("process_count")
    private Long processCount = 0L;

    /**
     * 最后处理时间
     * 最近一次处理的时间
     */
    @Column("last_process_time")
    private LocalDateTime lastProcessTime;

    /**
     * 扩展字段1
     * 预留的扩展字段
     */
    @Column("ext_field1")
    private String extField1;

    /**
     * 扩展字段2
     * 预留的扩展字段
     */
    @Column("ext_field2")
    private String extField2;

    /**
     * 扩展字段3
     * 预留的扩展字段
     */
    @Column("ext_field3")
    private String extField3;

    /**
     * 默认构造函数
     */
    public BusinessFunction2() {
        super();
    }

    /**
     * 带参数的构造函数
     *
     * @param businessNo 业务编号
     * @param businessName 业务名称
     * @param businessType 业务类型
     */
    public BusinessFunction2(String businessNo, String businessName, String businessType) {
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

    public String getBusinessData() {
        return businessData;
    }

    public void setBusinessData(String businessData) {
        this.businessData = businessData;
    }

    public String getConfigData() {
        return configData;
    }

    public void setConfigData(String configData) {
        this.configData = configData;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
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

    public Long getProcessCount() {
        return processCount;
    }

    public void setProcessCount(Long processCount) {
        this.processCount = processCount;
    }

    public LocalDateTime getLastProcessTime() {
        return lastProcessTime;
    }

    public void setLastProcessTime(LocalDateTime lastProcessTime) {
        this.lastProcessTime = lastProcessTime;
    }

    public String getExtField1() {
        return extField1;
    }

    public void setExtField1(String extField1) {
        this.extField1 = extField1;
    }

    public String getExtField2() {
        return extField2;
    }

    public void setExtField2(String extField2) {
        this.extField2 = extField2;
    }

    public String getExtField3() {
        return extField3;
    }

    public void setExtField3(String extField3) {
        this.extField3 = extField3;
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
     * 判断业务是否已归档
     *
     * @return 如果已归档返回true，否则返回false
     */
    public boolean isArchived() {
        return this.status == BusinessStatus.ARCHIVED;
    }

    /**
     * 判断业务是否在有效期内
     *
     * @return 如果在有效期内返回true，否则返回false
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        if (startTime != null && now.isBefore(startTime)) {
            return false;
        }
        if (endTime != null && now.isAfter(endTime)) {
            return false;
        }
        return true;
    }

    /**
     * 激活业务
     */
    public void activate() {
        this.status = BusinessStatus.ACTIVE;
    }

    /**
     * 停用业务
     */
    public void deactivate() {
        this.status = BusinessStatus.INACTIVE;
    }

    /**
     * 暂停业务
     */
    public void suspend() {
        this.status = BusinessStatus.SUSPENDED;
    }

    /**
     * 归档业务
     */
    public void archive() {
        this.status = BusinessStatus.ARCHIVED;
    }

    /**
     * 增加处理次数
     */
    public void incrementProcessCount() {
        if (this.processCount == null) {
            this.processCount = 0L;
        }
        this.processCount++;
        this.lastProcessTime = LocalDateTime.now();
    }

    /**
     * 业务状态枚举
     */
    public enum BusinessStatus {
        /** 活跃 */
        ACTIVE("活跃"),
        /** 非活跃 */
        INACTIVE("非活跃"),
        /** 暂停 */
        SUSPENDED("暂停"),
        /** 归档 */
        ARCHIVED("归档");

        private final String description;

        BusinessStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 优先级枚举
     */
    public enum Priority {
        /** 高优先级 */
        HIGH("高"),
        /** 中优先级 */
        MEDIUM("中"),
        /** 低优先级 */
        LOW("低");

        private final String description;

        Priority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return String.format("BusinessFunction2{id=%d, businessNo='%s', businessName='%s', status=%s, priority=%s}",
                getId(), businessNo, businessName, status, priority);
    }
}


