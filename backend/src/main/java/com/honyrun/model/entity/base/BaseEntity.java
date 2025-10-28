package com.honyrun.model.entity.base;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;

import java.io.Serializable;
import java.util.Objects;

/**
 * 响应式基础实体类
 *
 * 提供所有实体类的基础字段和通用方法，支持R2DBC响应式数据访问
 * 实现Persistable接口以正确处理R2DBC的ID生成策略
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  16:15:00
 * @modified 2025-10-02 20:50:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public abstract class BaseEntity implements Serializable, Persistable<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID - 64位长整型唯一标识
     *
     * 重要说明：
     * 1. 使用雪花算法（Snowflake）生成64位长整型唯一ID
     * 2. 确保分布式环境下的全局唯一性和有序性
     * 3. ID结构：1位符号位 + 41位时间戳 + 10位机器ID + 12位序列号
     * 4. 支持高并发场景，单机每秒可生成400万个ID
     * 5. 生成的ID按时间递增，便于数据库索引和查询优化
     *
     * 技术实现：
     * - 使用@Id注解标识主键字段，支持R2DBC自动映射
     * - R2DBC不支持@GeneratedValue注解，ID生成由SnowflakeIdGenerator处理
     * - 在业务层通过SnowflakeIdGenerator.generateUserId()生成用户ID
     * - 数据库字段类型：BIGINT，对应Java Long类型
     *
     * 注意事项：
     * - 所有用户相关实体必须使用64位长整型ID
     * - 测试环境和生产环境必须保持ID类型一致
     * - 禁止使用自增ID或其他ID生成策略
     * - ID一旦生成不可修改，确保数据一致性
     *
     * @see com.honyrun.util.SnowflakeIdGenerator
     */
    @Id
    @Column("id")
    private Long id;

    // deleted字段已移至具体实体类中定义，避免重复定义冲突

    /**
     * 备注信息
     * 用于存储实体的额外说明信息
     * 注意：此字段在users表中不存在，使用@Transient排除持久化
     */
    @Transient
    private String remark;

    /**
     * 标记实体是否为新实体（用于R2DBC Persistable接口）
     * 该字段不会持久化到数据库
     */
    @Transient
    private boolean isNew = true;

    /**
     * 默认构造函数
     */
    protected BaseEntity() {
        // 默认构造函数，供子类使用
    }

    /**
     * 带ID的构造函数
     *
     * @param id 主键ID
     */
    protected BaseEntity(Long id) {
        this.id = id;
        this.isNew = (id == null);
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取主键ID
     *
     * @return 主键ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键ID
     *
     * @param id 主键ID
     */
    public void setId(Long id) {
        this.id = id;
        this.isNew = (id == null);
    }

    // deleted字段相关方法已移至具体实体类中实现，避免重复定义冲突

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

    // ==================== 通用方法 ====================

    /**
     * 实现Persistable接口的isNew方法
     * 用于R2DBC判断实体是否为新实体
     *
     * @return true-新实体，false-已存在实体
     */
    @Override
    public boolean isNew() {
        // 优先使用手动标记，其次依据ID为空判断
        return this.isNew || this.id == null;
    }

    /**
     * 手动标记实体为新实体
     */
    public void markAsNew() {
        this.isNew = true;
    }

    /**
     * 手动标记实体为已存在实体
     */
    public void markAsExisting() {
        this.isNew = false;
    }



    // ==================== Object方法重写 ====================

    /**
     * 重写equals方法
     * 基于ID进行比较，如果ID为空则使用默认比较
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
        BaseEntity that = (BaseEntity) obj;

        // 如果ID都不为空，则基于ID比较
        if (this.id != null && that.id != null) {
            return Objects.equals(this.id, that.id);
        }

        // 如果ID为空，则使用默认比较
        return super.equals(obj);
    }

    /**
     * 重写hashCode方法
     * 基于ID生成哈希码，如果ID为空则使用默认哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        if (this.id != null) {
            return Objects.hash(this.id);
        }
        return super.hashCode();
    }

    /**
     * 重写toString方法
     * 提供实体的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +

                ", remark='" + remark + '\'' +
                '}';
    }
}

