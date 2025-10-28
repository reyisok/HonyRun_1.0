package com.honyrun.model.entity.business;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.honyrun.model.entity.base.AuditableEntity;

/**
 * 用户权限实体类 - 简化权限模型
 * 【权限架构】：采用用户-权限直接映射模型，去除角色中间层
 * 【用户类型标准化】：严格使用SYSTEM_USER、NORMAL_USER、GUEST，禁止role概念
 *
 * 用户权限关联表，记录用户与权限的对应关系
 * 继承AuditableEntity，支持R2DBC响应式数据访问和审计功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 17:05:00
 * @modified 2025-01-15 当前时间
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("user_permissions")
public class UserPermission extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     * 外键关联sys_user表的id字段
     */
    @Column("user_id")
    private Long userId;

    /**
     * 权限名称
     * 权限的唯一标识符，如：USER_MANAGE、SYSTEM_SETTING等
     */
    @Column("permission")
    private String permission;

    /**
     * 资源标识（冗余字段）
     * 权限所控制的资源，如：user、system、business等
     * 冗余字段，提高查询性能，避免JOIN操作
     */
    @Column("resource")
    private String resource;

    /**
     * 是否激活
     * true: 激活, false: 未激活
     */
    @Column("is_active")
    private Boolean isActive = true;

    /**
     * 授权人ID
     * 授予该权限的用户ID
     */
    @Column("granted_by")
    private Long grantedBy;

    /**
     * 授权时间
     * 权限被授予的时间
     */
    @Column("granted_at")
    private LocalDateTime grantedAt;

    /**
     * 删除标记
     * 0: 未删除, 1: 已删除
     */
    @Column("deleted")
    private Integer deleted = 0;

    /**
     * 默认构造函数
     */
    public UserPermission() {
        super();
        this.grantedAt = LocalDateTime.now();
        this.isActive = true;
    }

    /**
     * 带用户ID和权限名称的构造函数
     *
     * @param userId     用户ID
     * @param permission 权限名称
     */
    public UserPermission(Long userId, String permission) {
        this();
        this.userId = userId;
        this.permission = permission;
    }

    /**
     * 完整构造函数
     *
     * @param userId     用户ID
     * @param permission 权限名称
     * @param grantedBy  授权人ID
     */
    public UserPermission(Long userId, String permission, Long grantedBy) {
        this(userId, permission);
        this.grantedBy = grantedBy;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取权限名称
     *
     * @return 权限名称
     */
    public String getPermission() {
        return permission;
    }

    /**
     * 设置权限名称
     *
     * @param permission 权限名称
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * 获取资源标识（冗余字段）
     *
     * @return 资源标识
     */
    public String getResource() {
        return resource;
    }

    /**
     * 设置资源标识（冗余字段）
     *
     * @param resource 资源标识
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * 获取是否激活状态
     *
     * @return 是否激活
     */
    public Boolean getIsActive() {
        return isActive;
    }

    /**
     * 设置是否激活状态
     *
     * @param isActive 是否激活
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * 获取授权时间
     *
     * @return 授权时间
     */
    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }

    /**
     * 设置授权时间
     *
     * @param grantedAt 授权时间
     */
    public void setGrantedAt(LocalDateTime grantedAt) {
        this.grantedAt = grantedAt;
    }

    /**
     * 获取授权人ID
     *
     * @return 授权人ID
     */
    public Long getGrantedBy() {
        return grantedBy;
    }

    /**
     * 设置授权人ID
     *
     * @param grantedBy 授权人ID
     */
    public void setGrantedBy(Long grantedBy) {
        this.grantedBy = grantedBy;
    }

    /**
     * 获取删除标记
     *
     * @return 删除标记，0-未删除，1-已删除
     */
    public Integer getDeleted() {
        return deleted;
    }

    /**
     * 设置删除标记
     *
     * @param deleted 删除标记，0-未删除，1-已删除
     */
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断实体是否已删除
     *
     * @return true-已删除，false-未删除
     */
    public boolean isDeleted() {
        return this.deleted != null && this.deleted == 1;
    }

    /**
     * 标记实体为已删除
     */
    public void markDeleted() {
        this.deleted = 1;
    }

    /**
     * 标记实体为未删除
     */
    public void markUndeleted() {
        this.deleted = 0;
    }

    /**
     * 判断权限是否启用
     *
     * @return true-启用，false-禁用
     */
    public boolean isEnabled() {
        return this.isActive != null && this.isActive;
    }

    /**
     * 判断权限是否有效（未删除且启用）
     *
     * @return true-有效，false-无效
     */
    public boolean isValid() {
        return !isDeleted() && isEnabled();
    }

    /**
     * 判断权限是否可用（有效且未过期）
     *
     * @return true-可用，false-不可用
     */
    public boolean isAvailable() {
        return isValid();
    }

    /**
     * 启用权限
     */
    public void enable() {
        this.isActive = true;
    }

    /**
     * 禁用权限
     */
    public void disable() {
        this.isActive = false;
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写equals方法
     * 基于用户ID和权限代码进行比较
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

        UserPermission that = (UserPermission) obj;

        // 如果ID都不为空，则基于ID比较
        if (this.getId() != null && that.getId() != null) {
            return Objects.equals(this.getId(), that.getId());
        }

        // 基于用户ID和权限代码组合比较
        return Objects.equals(this.userId, that.userId) &&
                Objects.equals(this.permission, that.permission);
    }

    /**
     * 重写hashCode方法
     * 基于用户ID和权限代码生成哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        if (this.getId() != null) {
            return Objects.hash(this.getId());
        }
        return Objects.hash(this.userId, this.permission);
    }

    /**
     * 重写toString方法
     * 提供用户权限实体的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "UserPermission{" +
                "id=" + getId() +
                ", userId=" + userId +
                ", permission='" + permission + '\'' +
                ", resource='" + resource + '\'' +
                ", isActive=" + isActive +
                ", grantedBy=" + grantedBy +
                ", grantedAt=" + grantedAt +
                ", available=" + isAvailable() +
                ", deleted=" + getDeleted() +
                '}';
    }
}
