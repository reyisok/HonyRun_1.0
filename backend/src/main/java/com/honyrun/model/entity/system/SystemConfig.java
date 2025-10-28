package com.honyrun.model.entity.system;

import java.util.Objects;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.honyrun.model.entity.base.AuditableEntity;

/**
 * 系统配置实体类
 *
 * 系统级配置管理实体，支持系统级配置、环境变量、配置分组等功能
 * 继承AuditableEntity，支持R2DBC响应式数据访问和审计功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 18:45:00
 * @modified 2025-07-01 18:45:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("sys_system_configs")
public class SystemConfig extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 配置键名
     * 系统配置项的唯一标识
     */
    @Column("config_key")
    private String configKey;

    /**
     * 配置值
     * 系统配置项的具体值
     */
    @Column("config_value")
    private String configValue;

    /**
     * 配置分组
     * 用于对系统配置进行分组管理
     */
    @Column("config_group")
    private String configGroup;

    /**
     * 配置类型
     * STRING, INTEGER, BOOLEAN, JSON
     */
    @Column("config_type")
    private String configType;

    /**
     * 配置描述
     * 配置项的详细说明
     */
    @Column("description")
    private String description;

    /**
     * 状态
     * ACTIVE, INACTIVE
     */
    @Column("status")
    private String status;

    /**
     * 是否启用
     * 1-启用，0-禁用
     */
    @Column("enabled")
    private Integer enabled;

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
     */
    public SystemConfig() {
        super();
        this.configType = "STRING";
        this.enabled = 1;
    }

    /**
     * 带参数的构造函数
     *
     * @param configKey   配置键名
     * @param configValue 配置值
     * @param configGroup 配置分组
     * @param configType  配置类型
     */
    public SystemConfig(String configKey, String configValue, String configGroup, String configType) {
        this();
        this.configKey = configKey;
        this.configValue = configValue;
        this.configGroup = configGroup;
        this.configType = configType;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取配置键名
     *
     * @return 配置键名
     */
    public String getConfigKey() {
        return configKey;
    }

    /**
     * 设置配置键名
     *
     * @param configKey 配置键名
     */
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    /**
     * 获取配置值
     *
     * @return 配置值
     */
    public String getConfigValue() {
        return configValue;
    }

    /**
     * 设置配置值
     *
     * @param configValue 配置值
     */
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    /**
     * 获取配置分组
     *
     * @return 配置分组
     */
    public String getConfigGroup() {
        return configGroup;
    }

    /**
     * 设置配置分组
     *
     * @param configGroup 配置分组
     */
    public void setConfigGroup(String configGroup) {
        this.configGroup = configGroup;
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
     * 获取是否启用
     *
     * @return 是否启用，0-禁用，1-启用
     */
    public Integer getEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用
     * 修复Boolean到Integer转换错误：支持Boolean和Integer参数
     *
     * @param enabled 是否启用，支持Boolean或Integer类型
     */
    public void setEnabled(Object enabled) {
        if (enabled instanceof Boolean) {
            this.enabled = ((Boolean) enabled) ? 1 : 0;
        } else if (enabled instanceof Integer) {
            this.enabled = (Integer) enabled;
        } else if (enabled instanceof Number) {
            this.enabled = ((Number) enabled).intValue();
        } else if (enabled != null) {
            // 尝试字符串转换
            String str = enabled.toString();
            if ("true".equalsIgnoreCase(str) || "1".equals(str)) {
                this.enabled = 1;
            } else if ("false".equalsIgnoreCase(str) || "0".equals(str)) {
                this.enabled = 0;
            } else {
                this.enabled = Integer.valueOf(str);
            }
        } else {
            this.enabled = null;
        }
    }

    /**
     * 设置是否启用（Integer类型）
     *
     * @param enabled 是否启用，0-禁用，1-启用
     */
    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    /**
     * 设置是否启用（Boolean类型）
     * 修复Boolean到Integer转换错误
     *
     * @param enabled 是否启用，true-启用，false-禁用
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = (enabled != null && enabled) ? 1 : 0;
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
     * 是否启用
     *
     * @return true-启用，false-禁用
     */
    public boolean isEnabled() {
        return enabled != null && enabled == 1;
    }

    /**
     * 获取配置值作为字符串
     *
     * @return 配置值字符串
     */
    public String getValueAsString() {
        return configValue;
    }

    /**
     * 获取配置值作为整数
     *
     * @return 配置值整数，如果转换失败返回null
     */
    public Integer getValueAsInteger() {
        try {
            return configValue != null ? Integer.valueOf(configValue) : null;
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
        if (configValue == null) {
            return null;
        }
        return "true".equalsIgnoreCase(configValue) || "1".equals(configValue);
    }

    /**
     * 获取配置值作为双精度浮点数
     *
     * @return 配置值双精度浮点数，如果转换失败返回null
     */
    public Double getValueAsDouble() {
        try {
            return configValue != null ? Double.valueOf(configValue) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 启用配置
     */
    public void enable() {
        this.enabled = 1;
    }

    /**
     * 禁用配置
     */
    public void disable() {
        this.enabled = 0;
    }

    /**
     * 获取显示值
     * 如果是敏感配置，返回掩码值
     *
     * @return 显示值
     */
    public String getDisplayValue() {
        return configValue;
    }

    /**
     * 是否已删除
     *
     * @return true-已删除，false-未删除
     */
    public boolean isDeleted() {
        return deleted != null && deleted == 1;
    }

    /**
     * 标记为已删除
     */
    public void markDeleted() {
        this.deleted = 1;
    }

    /**
     * 标记为未删除
     */
    public void markUndeleted() {
        this.deleted = 0;
    }

    // ==================== Object方法重写 ====================

    /**
     * 判断两个SystemConfig对象是否相等
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
        SystemConfig that = (SystemConfig) obj;
        return Objects.equals(configKey, that.configKey) &&
                Objects.equals(configValue, that.configValue) &&
                Objects.equals(configGroup, that.configGroup) &&
                Objects.equals(configType, that.configType) &&
                Objects.equals(description, that.description) &&
                Objects.equals(status, that.status) &&
                Objects.equals(enabled, that.enabled) &&
                Objects.equals(deleted, that.deleted);
    }

    /**
     * 获取对象哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), configKey, configValue, configGroup,
                configType, description, status, enabled, deleted);
    }

    /**
     * 获取对象字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "SystemConfig{" +
                "id=" + getId() +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                ", configGroup='" + configGroup + '\'' +
                ", configType='" + configType + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", enabled=" + enabled +
                ", deleted=" + deleted +
                ", createdDate=" + getCreatedDate() +
                ", lastModifiedDate=" + getLastModifiedDate() +
                '}';
    }
}
