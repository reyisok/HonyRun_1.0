package com.honyrun.model.entity.base;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

/**
 * 响应式可审计实体类
 *
 * 继承BaseEntity，提供审计功能支持，包括创建时间、修改时间、版本控制等
 * 支持R2DBC响应式数据访问和自动审计字段填充
 *
 * ==================== 字段映射最佳实践说明 ====================
 *
 * 本项目统一使用以下时间字段命名规范：
 * 1. created_at: 创建时间（对应@CreatedDate注解）
 * 2. last_modified_date: 最后修改时间（对应@LastModifiedDate注解）
 * 3. created_by: 创建人ID（对应@CreatedBy注解）
 * 4. last_modified_by: 最后修改人ID（对应@LastModifiedBy注解）
 *
 * 这样的命名规范：
 * - 符合Spring Data R2DBC的注解标准
 * - 与AuditableEntity基类保持一致
 * - 避免字段映射混乱和SQL语法错误
 * - 遵循Spring Boot 3最佳实践
 *
 * 注意：避免使用created_time/updated_time等非标准命名
 *
 * ==================== 字段映射最佳实践说明 ====================
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  16:20:00
 * @modified 2025-07-01 15:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public abstract class AuditableEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 创建时间
     * 使用@CreatedDate注解，支持R2DBC自动填充
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdDate;

    /**
     * 创建人ID
     * 使用@CreatedBy注解，支持R2DBC自动填充
     */
    @CreatedBy
    @Column("created_by")
    private Long createdBy;

    /**
     * 最后修改时间
     * 使用@LastModifiedDate注解，支持R2DBC自动填充
     */
    @LastModifiedDate
    @Column("last_modified_date")
    private LocalDateTime lastModifiedDate;

    /**
     * 最后修改人ID
     * 使用@LastModifiedBy注解，支持R2DBC自动填充
     */
    @LastModifiedBy
    @Column("last_modified_by")
    private Long lastModifiedBy;

    /**
     * 版本号
     * 使用@Version注解，支持乐观锁控制
     * 每次更新时自动递增
     */
    @Version
    @Column("version")
    private Long version;

    /**
     * 默认构造函数
     */
    protected AuditableEntity() {
        super();
    }

    /**
     * 带ID的构造函数
     *
     * @param id 主键ID
     */
    protected AuditableEntity(Long id) {
        super(id);
    }

    // ==================== Getter和Setter方法 ====================

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
     * 获取创建人ID
     *
     * @return 创建人ID
     */
    public Long getCreatedBy() {
        return createdBy;
    }

    /**
     * 设置创建人ID
     *
     * @param createdBy 创建人ID
     */
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
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
     * 获取最后修改人ID
     *
     * @return 最后修改人ID
     */
    public Long getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * 设置最后修改人ID
     *
     * @param lastModifiedBy 最后修改人ID
     */
    public void setLastModifiedBy(Long lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
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

    // ==================== 审计相关方法 ====================

    /**
     * 判断实体是否已创建（有创建时间）
     *
     * @return true-已创建，false-未创建
     */
    public boolean isCreated() {
        return this.createdDate != null;
    }

    /**
     * 判断实体是否已修改（修改时间晚于创建时间）
     *
     * @return true-已修改，false-未修改
     */
    public boolean isModified() {
        return this.lastModifiedDate != null &&
               this.createdDate != null &&
               this.lastModifiedDate.isAfter(this.createdDate);
    }

    /**
     * 获取实体的年龄（从创建到现在的时间）
     *
     * @return 实体年龄（秒），如果未创建返回0
     */
    public long getAgeInSeconds() {
        if (this.createdDate == null) {
            return 0;
        }
        return java.time.Duration.between(this.createdDate, LocalDateTime.now()).getSeconds();
    }

    /**
     * 获取最后修改距离现在的时间
     *
     * @return 最后修改距离现在的时间（秒），如果未修改返回创建时间距离现在的时间
     */
    public long getLastModifiedAgeInSeconds() {
        LocalDateTime referenceTime = this.lastModifiedDate != null ?
                                    this.lastModifiedDate : this.createdDate;
        if (referenceTime == null) {
            return 0;
        }
        return java.time.Duration.between(referenceTime, LocalDateTime.now()).getSeconds();
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写toString方法
     * 包含审计字段信息
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + getId() +
                ", createdDate=" + createdDate +
                ", createdBy=" + createdBy +
                ", lastModifiedDate=" + lastModifiedDate +
                ", lastModifiedBy=" + lastModifiedBy +
                ", version=" + version +
                ", remark='" + getRemark() + '\'' +
                '}';
    }
}


