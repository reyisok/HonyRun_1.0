package com.honyrun.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 系统设置响应DTO
 *
 * 用于返回系统设置信息的响应对象，包含完整的配置信息和审计字段
 * 支持响应式数据传输和JSON序列化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  22:15:00
 * @modified 2025-07-01 22:15:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemSettingResponse {

    /**
     * 设置ID
     */
    @JsonProperty("id")
    private Long id;

    /**
     * 配置键名
     */
    @JsonProperty("settingKey")
    private String settingKey;

    /**
     * 配置值
     */
    @JsonProperty("settingValue")
    private String settingValue;

    /**
     * 配置分类
     */
    @JsonProperty("category")
    private String category;

    /**
     * 配置类型
     */
    @JsonProperty("configType")
    private String configType;

    /**
     * 配置描述
     */
    @JsonProperty("description")
    private String description;

    /**
     * 是否启用
     * 0-禁用，1-启用
     */
    @JsonProperty("enabled")
    private Integer enabled;

    /**
     * 状态
     */
    @JsonProperty("status")
    private String status;

    /**
     * 创建时间
     */
    @JsonProperty("createdDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    /**
     * 最后修改时间
     */
    @JsonProperty("lastModifiedDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModifiedDate;

    /**
     * 创建人
     */
    @JsonProperty("createdBy")
    private String createdBy;

    /**
     * 最后修改人
     */
    @JsonProperty("lastModifiedBy")
    private String lastModifiedBy;

    /**
     * 版本号
     */
    @JsonProperty("version")
    private Long version;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public SystemSettingResponse() {
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取设置ID
     *
     * @return 设置ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置设置ID
     *
     * @param id 设置ID
     */
    public void setId(Long id) {
        this.id = id;
    }

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
     * 获取启用状态
     *
     * @return 启用状态
     */
    public Integer getEnabled() {
        return enabled;
    }

    /**
     * 设置启用状态
     *
     * @param enabled 启用状态
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
     * 获取最后修改人
     *
     * @return 最后修改人
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * 设置最后修改人
     *
     * @param lastModifiedBy 最后修改人
     */
    public void setLastModifiedBy(String lastModifiedBy) {
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

    // ==================== 工具方法 ====================

    /**
     * 判断是否为启用状态
     *
     * @return true-启用，false-禁用
     */
    public boolean isEnabled() {
        return enabled != null && enabled == 1;
    }

    /**
     * 判断是否为激活状态
     *
     * @return true-激活，false-非激活
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
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

    /**
     * 获取格式化的配置值（根据配置类型）
     *
     * @return 格式化的配置值
     */
    public String getFormattedValue() {
        if (settingValue == null) {
            return null;
        }

        if ("BOOLEAN".equals(configType)) {
            return Boolean.parseBoolean(settingValue) ? "是" : "否";
        } else if ("INTEGER".equals(configType)) {
            return settingValue;
        } else if ("JSON".equals(configType)) {
            return settingValue.length() > 100 ? settingValue.substring(0, 100) + "..." : settingValue;
        } else {
            return settingValue;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemSettingResponse that = (SystemSettingResponse) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(settingKey, that.settingKey) &&
               Objects.equals(settingValue, that.settingValue) &&
               Objects.equals(category, that.category) &&
               Objects.equals(configType, that.configType) &&
               Objects.equals(description, that.description) &&
               Objects.equals(enabled, that.enabled) &&
               Objects.equals(status, that.status) &&
               Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, settingKey, settingValue, category, configType,
                          description, enabled, status, version);
    }

    @Override
    public String toString() {
        return "SystemSettingResponse{" +
               "id=" + id +
               ", settingKey='" + settingKey + '\'' +
               ", settingValue='" + settingValue + '\'' +
               ", category='" + category + '\'' +
               ", configType='" + configType + '\'' +
               ", description='" + description + '\'' +
               ", enabled=" + enabled +
               ", status='" + status + '\'' +
               ", createdDate=" + createdDate +
               ", lastModifiedDate=" + lastModifiedDate +
               ", version=" + version +
               '}';
    }
}


