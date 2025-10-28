package com.honyrun.model.entity.security;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 用户密码历史记录实体类
 * 用于存储用户的历史密码，防止密码重复使用
 *
 * @author Mr.Rey
 * @since 2025-07-01 10:30:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("user_password_history")
public class UserPasswordHistory {

    /**
     * 历史记录ID
     */
    @Id
    private Long id;

    /**
     * 用户ID
     */
    @Column("user_id")
    private Long userId;

    /**
     * 密码哈希值（BCrypt加密）
     */
    @Column("password_hash")
    private String passwordHash;

    /**
     * 创建时间
     * 注意：统一使用Spring Boot 3最佳实践命名规范
     * 数据库字段：created_at（对应@CreatedDate注解）
     * 实体字段：createdTime（Java驼峰命名）
     */
    @Column("created_at")
    private LocalDateTime createdTime;

    /**
     * 创建人（通常是用户自己或管理员）
     */
    @Column("created_by")
    private String createdBy;

    /**
     * 密码变更原因
     * USER_CHANGE-用户主动修改，ADMIN_RESET-管理员重置，EXPIRED-密码过期，SECURITY-安全要求
     */
    @Column("change_reason")
    private String changeReason;

    /**
     * 默认构造函数
     */
    public UserPasswordHistory() {
    }

    /**
     * 带参数的构造函数
     *
     * @param userId 用户ID
     * @param passwordHash 密码哈希值
     * @param createdBy 创建人
     * @param changeReason 变更原因
     */
    public UserPasswordHistory(Long userId, String passwordHash, String createdBy, String changeReason) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.createdBy = createdBy;
        this.changeReason = changeReason;
        this.createdTime = LocalDateTime.now();
    }

    /**
     * 获取历史记录ID
     *
     * @return 历史记录ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置历史记录ID
     *
     * @param id 历史记录ID
     */
    public void setId(Long id) {
        this.id = id;
    }

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
     * 获取密码哈希值
     *
     * @return 密码哈希值
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * 设置密码哈希值
     *
     * @param passwordHash 密码哈希值
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    /**
     * 设置创建时间
     *
     * @param createdTime 创建时间
     */
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * 获取创建人
     *
     * @return 创建人
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * 设置创建人
     *
     * @param createdBy 创建人
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * 获取变更原因
     *
     * @return 变更原因
     */
    public String getChangeReason() {
        return changeReason;
    }

    /**
     * 设置变更原因
     *
     * @param changeReason 变更原因
     */
    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    @Override
    public String toString() {
        return "UserPasswordHistory{" +
                "id=" + id +
                ", userId=" + userId +
                ", passwordHash='[PROTECTED]'" +
                ", createdTime=" + createdTime +
                ", createdBy='" + createdBy + '\'' +
                ", changeReason='" + changeReason + '\'' +
                '}';
    }
}

