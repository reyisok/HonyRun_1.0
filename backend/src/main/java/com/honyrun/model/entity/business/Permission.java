package com.honyrun.model.entity.business;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 权限实体类 - 简化权限模型
 * 【权限架构】：采用用户-权限直接映射模型，去除角色中间层
 * 【权限分类】：系统权限、业务权限、功能权限三大类
 * 【用户类型标准化】：严格使用SYSTEM_USER、NORMAL_USER、GUEST，禁止role概念
 *
 * @author Mr.Rey
 * @version 1.0.0
 * @created 2025-01-15
 * @modified 2025-01-15
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("permissions")
public class Permission extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 权限名称 - 唯一标识符
     * 如：SYSTEM_MANAGEMENT、USER_MANAGEMENT、BUSINESS_FUNCTION_1等
     */
    @Column("name")
    private String name;

    /**
     * 资源名称
     * 权限所控制的资源，如：system、user、business等
     */
    @Column("resource")
    private String resource;

    /**
     * 操作类型
     * 对资源的操作类型，如：manage、read、write、delete等
     */
    @Column("action")
    private String action;

    /**
     * 权限描述
     * 权限的详细描述信息
     */
    @Column("description")
    private String description;

    // 构造函数
    public Permission() {
        super();
    }

    public Permission(String name, String resource, String action) {
        this();
        this.name = name;
        this.resource = resource;
        this.action = action;
    }

    public Permission(String name, String resource, String action, String description) {
        this(name, resource, action);
        this.description = description;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                ", description='" + description + '\'' +
                ", createdDate=" + getCreatedDate() +
                ", lastModifiedDate=" + getLastModifiedDate() +
                '}';
    }
}
