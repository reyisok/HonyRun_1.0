package com.honyrun.model.entity.system;

import java.util.Objects;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.honyrun.model.entity.base.AuditableEntity;

/**
 * 系统设置实体类
 *
 * 系统配置管理实体，支持键值对配置、分类管理、数据类型定义等功能
 * 继承AuditableEntity，支持R2DBC响应式数据访问和审计功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 18:30:00
 * @modified 2025-07-01 18:30:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("sys_system_configs")
public class SystemSetting extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 配置键名
     * 配置项的唯一标识，不可重复
     */
    @Column("config_key")
    private String settingKey;

    /**
     * 配置值
     * 配置项的具体值，支持各种数据类型
     */
    @Column("config_value")
    private String settingValue;

    /**
     * 配置分类
     * 用于对配置项进行分组管理
     */
    @Column("config_group")
    private String category;

    /**
     * 配置描述
     * 配置项的详细说明
     */
    @Column("description")
    private String description;

    /**
     * 是否启用
     * 0-禁用，1-启用
     */
    @Column("enabled")
    private Integer enabled;

    /**
     * 状态
     * 状态：ACTIVE, INACTIVE
     */
    @Column("status")
    private String status;

    /**
     * 配置类型
     * 配置类型：STRING, INTEGER, BOOLEAN, JSON
     */
    @Column("config_type")
    private String configType;

    /**
     * 删除标记字段
     * 0-未删除，1-已删除
     * 用于逻辑删除，避免物理删除数据
     */
    @Column("deleted")
    private Integer deleted = 0;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     *
     * @author: Mr.Rey Copyright © 2025
     * @created: 2025-10-27 12:26:43
     * @version: 1.0.0
     */
    public SystemSetting() {
        super();
        this.enabled = 1;
        this.status = "ACTIVE";
        this.configType = "STRING";
    }

    /**
     * 带参数的构造函数
     *
     * @author: Mr.Rey Copyright © 2025
     * @created: 2025-10-27 12:26:43
     * @version: 1.0.0
     * @param settingKey   配置键名
     * @param settingValue 配置值
     * @param category     配置分类
     * @param configType   配置类型
     */
    public SystemSetting(String settingKey, String settingValue, String category, String configType) {
        this();
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.category = category;
        this.configType = configType;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取配置键名
     *
     * @return 配置键名
     */
    public String getSettingKey() {
        return settingKey;
    }

    /**
     * 设置配置键名
     *
     * @param settingKey 配置键名
     */
    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    /**
     * 获取配置值
     *
     * @return 配置值
     */
    public String getSettingValue() {
        return settingValue;
    }

    /**
     * 设置配置值
     *
     * @param settingValue 配置值
     */
    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    /**
     * 获取配置分类
     *
     * @return 配置分类
     */
    public String getCategory() {
        return category;
    }

    /**
     * 设置配置分类
     *
     * @param category 配置分类
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 获取配置描述
     *
     * @return 配置描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置配置描述
     *
     * @param description 配置描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取是否启用
     *
     * @return 是否启用，0-禁用，1-启用
     */
    public Integer getEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用
     *
     * @param enabled 是否启用，0-禁用，1-启用
     */
    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态
     *
     * @param status 状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取配置类型
     *
     * @return 配置类型
     */
    public String getConfigType() {
        return configType;
    }

    /**
     * 设置配置类型
     *
     * @param configType 配置类型
     */
    public void setConfigType(String configType) {
        this.configType = configType;
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
     * 判断配置是否启用
     *
     * @return true-启用，false-禁用
     */
    public boolean isEnabled() {
        return this.enabled != null && this.enabled == 1;
    }

    /**
     * 启用配置
     */
    public void enable() {
        this.enabled = 1;
        this.status = "ACTIVE";
    }

    /**
     * 禁用配置
     */
    public void disable() {
        this.enabled = 0;
        this.status = "INACTIVE";
    }

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
     * 获取配置值作为字符串
     *
     * @return 配置值字符串
     */
    public String getValueAsString() {
        return this.settingValue;
    }

    /**
     * 获取配置值作为整数
     *
     * @return 配置值整数，如果转换失败返回null
     */
    public Integer getValueAsInteger() {
        try {
            String value = getValueAsString();
            return value != null ? Integer.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取配置值作为布尔值
     *
     * @return 配置值布尔值
     */
    public Boolean getValueAsBoolean() {
        String value = getValueAsString();
        if (value == null) {
            return null;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * 获取配置值作为双精度浮点数
     *
     * @return 配置值双精度浮点数，如果转换失败返回null
     */
    public Double getValueAsDouble() {
        try {
            String value = getValueAsString();
            return value != null ? Double.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 判断是否为系统级配置
     *
     * @return true-系统级配置，false-普通配置
     */
    public boolean isSystemLevel() {
        return "SYSTEM".equalsIgnoreCase(this.category);
    }

    /**
     * 判断是否为安全相关配置
     *
     * @return true-安全配置，false-普通配置
     */
    public boolean isSecurityRelated() {
        return "SECURITY".equalsIgnoreCase(this.category);
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写equals方法
     * 基于配置键名进行比较
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
        if (!super.equals(obj)) {
            return false;
        }
        SystemSetting that = (SystemSetting) obj;
        return Objects.equals(settingKey, that.settingKey);
    }

    /**
     * 重写hashCode方法
     * 基于配置键名生成哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), settingKey);
    }

    /**
     * 重写toString方法
     * 提供系统设置实体的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "SystemSetting{" +
                "id=" + getId() +
                ", settingKey='" + settingKey + '\'' +
                ", settingValue='" + settingValue + '\'' +
                ", category='" + category + '\'' +
                ", configType='" + configType + '\'' +
                ", description='" + description + '\'' +
                ", enabled=" + enabled +
                ", status='" + status + '\'' +
                ", deleted=" + deleted +
                ", createdDate=" + getCreatedDate() +
                ", lastModifiedDate=" + getLastModifiedDate() +
                '}';
    }
}
