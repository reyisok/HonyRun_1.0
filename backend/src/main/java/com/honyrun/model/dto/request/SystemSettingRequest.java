package com.honyrun.model.dto.request;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 系统设置请求DTO
 *
 * 用于接收系统设置创建和更新的请求参数，包含验证注解和数据校验规则
 * 支持响应式数据绑定和参数验证
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 22:10:00
 * @modified 2025-07-01 22:10:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
public class SystemSettingRequest {

    /**
     * 配置键名
     * 必填，长度2-100字符，只能包含字母、数字、下划线、点号
     */
    @NotBlank(message = "配置键名不能为空")
    @Size(min = 2, max = 100, message = "配置键名长度必须在2-100字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "配置键名只能包含字母、数字、下划线、点号")
    @JsonProperty("settingKey")
    private String settingKey;

    /**
     * 配置值
     * 必填，最大长度2000字符
     */
    @NotBlank(message = "配置值不能为空")
    @Size(max = 2000, message = "配置值长度不能超过2000字符")
    @JsonProperty("settingValue")
    private String settingValue;

    /**
     * 配置分类
     * 必填，长度2-50字符
     */
    @NotBlank(message = "配置分类不能为空")
    @Size(min = 2, max = 50, message = "配置分类长度必须在2-50字符之间")
    @JsonProperty("category")
    private String category;

    /**
     * 配置类型
     * 必填，支持：STRING、INTEGER、BOOLEAN、JSON
     */
    @NotBlank(message = "配置类型不能为空")
    @Pattern(regexp = "^(STRING|INTEGER|BOOLEAN|JSON)$", message = "配置类型必须是：STRING、INTEGER、BOOLEAN、JSON之一")
    @JsonProperty("configType")
    private String configType;

    /**
     * 配置描述
     * 可选，最大长度500字符
     */
    @Size(max = 500, message = "配置描述长度不能超过500字符")
    @JsonProperty("description")
    private String description;

    /**
     * 是否启用
     * 必填，0-禁用，1-启用
     */
    @NotNull(message = "启用状态不能为空")
    @JsonProperty("enabled")
    private Integer enabled;

    /**
     * 状态
     * 必填，ACTIVE-激活，INACTIVE-非激活
     */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "状态必须是：ACTIVE、INACTIVE之一")
    @JsonProperty("status")
    private String status;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public SystemSettingRequest() {
        this.enabled = 1; // 默认启用
        this.status = "ACTIVE"; // 默认激活
        this.configType = "STRING"; // 默认字符串类型
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

    // ==================== 工具方法 ====================

    /**
     * 验证配置类型和配置值的匹配性
     *
     * @return 是否匹配
     */
    public boolean isValueTypeValid() {
        if (settingValue == null || configType == null) {
            return false;
        }

        try {
            switch (configType) {
                case "INTEGER":
                    Integer.parseInt(settingValue);
                    return true;
                case "BOOLEAN":
                    return "true".equalsIgnoreCase(settingValue) || "false".equalsIgnoreCase(settingValue);
                case "JSON":
                    // 简单的JSON格式检查
                    return settingValue.trim().startsWith("{") && settingValue.trim().endsWith("}") ||
                            settingValue.trim().startsWith("[") && settingValue.trim().endsWith("]");
                case "STRING":
                default:
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SystemSettingRequest that = (SystemSettingRequest) o;
        return Objects.equals(settingKey, that.settingKey) &&
                Objects.equals(settingValue, that.settingValue) &&
                Objects.equals(category, that.category) &&
                Objects.equals(configType, that.configType) &&
                Objects.equals(description, that.description) &&
                Objects.equals(enabled, that.enabled) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(settingKey, settingValue, category, configType,
                description, enabled, status);
    }

    @Override
    public String toString() {
        return "SystemSettingRequest{" +
                "settingKey='" + settingKey + '\'' +
                ", settingValue='" + settingValue + '\'' +
                ", category='" + category + '\'' +
                ", configType='" + configType + '\'' +
                ", description='" + description + '\'' +
                ", enabled=" + enabled +
                ", status='" + status + '\'' +
                '}';
    }
}
