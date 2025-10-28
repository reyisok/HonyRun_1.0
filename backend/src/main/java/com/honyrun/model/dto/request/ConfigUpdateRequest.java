package com.honyrun.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * 配置更新请求DTO
 *
 * 用于系统配置动态更新的请求参数封装
 * 支持单个配置项更新和批量配置更新
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 12:10:00
 * @modified 2025-07-01 12:10:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Schema(description = "配置更新请求")
public class ConfigUpdateRequest {

    /**
     * 配置键名
     */
    @Schema(description = "配置键名", example = "system.monitor.enabled")
    private String configKey;

    /**
     * 配置值
     */
    @Schema(description = "配置值", example = "true")
    private String configValue;

    /**
     * 配置分组
     */
    @Schema(description = "配置分组", example = "SYSTEM")
    private String configGroup;

    /**
     * 配置类型
     */
    @Schema(description = "配置类型", example = "BOOLEAN")
    private String configType;

    /**
     * 配置描述
     */
    @Schema(description = "配置描述", example = "系统监控开关")
    private String configDescription;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled = true;

    /**
     * 是否需要重启
     */
    @Schema(description = "是否需要重启", example = "false")
    private Boolean requireRestart = false;

    /**
     * 更新原因
     */
    @Schema(description = "更新原因", example = "运维调整")
    private String updateReason;

    /**
     * 批量配置更新列表
     */
    @Schema(description = "批量配置更新列表")
    private List<ConfigItem> configItems;

    /**
     * 更新模式
     * SINGLE - 单个配置更新
     * BATCH - 批量配置更新
     * MERGE - 合并更新（保留未指定的配置）
     * REPLACE - 替换更新（清除未指定的配置）
     */
    @Schema(description = "更新模式", example = "SINGLE")
    private String updateMode = "SINGLE";

    /**
     * 是否验证配置
     */
    @Schema(description = "是否验证配置", example = "true")
    private Boolean validateConfig = true;

    /**
     * 是否立即生效
     */
    @Schema(description = "是否立即生效", example = "true")
    private Boolean immediateEffect = true;

    /**
     * 备份当前配置
     */
    @Schema(description = "是否备份当前配置", example = "true")
    private Boolean backupCurrent = true;

    /**
     * 配置项内部类
     */
    @Schema(description = "配置项")
    public static class ConfigItem {
        
        /**
         * 配置键名
         */
        @Schema(description = "配置键名", requiredMode = Schema.RequiredMode.REQUIRED)
        private String configKey;

        /**
         * 配置值
         */
        @Schema(description = "配置值", requiredMode = Schema.RequiredMode.REQUIRED)
        private String configValue;

        /**
         * 配置分组
         */
        @Schema(description = "配置分组")
        private String configGroup;

        /**
         * 配置类型
         */
        @Schema(description = "配置类型")
        private String configType;

        /**
         * 配置描述
         */
        @Schema(description = "配置描述")
        private String configDescription;

        /**
         * 是否启用
         */
        @Schema(description = "是否启用")
        private Boolean enabled = true;

        /**
         * 是否需要重启
         */
        @Schema(description = "是否需要重启")
        private Boolean requireRestart = false;

        /**
         * 操作类型
         * CREATE - 创建新配置
         * UPDATE - 更新现有配置
         * DELETE - 删除配置
         */
        @Schema(description = "操作类型", example = "UPDATE")
        private String operation = "UPDATE";

        // ==================== Getter和Setter方法 ====================

        public String getConfigKey() {
            return configKey;
        }

        public void setConfigKey(String configKey) {
            this.configKey = configKey;
        }

        public String getConfigValue() {
            return configValue;
        }

        public void setConfigValue(String configValue) {
            this.configValue = configValue;
        }

        public String getConfigGroup() {
            return configGroup;
        }

        public void setConfigGroup(String configGroup) {
            this.configGroup = configGroup;
        }

        public String getConfigType() {
            return configType;
        }

        public void setConfigType(String configType) {
            this.configType = configType;
        }

        public String getConfigDescription() {
            return configDescription;
        }

        public void setConfigDescription(String configDescription) {
            this.configDescription = configDescription;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Boolean getRequireRestart() {
            return requireRestart;
        }

        public void setRequireRestart(Boolean requireRestart) {
            this.requireRestart = requireRestart;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        @Override
        public String toString() {
            return "ConfigItem{" +
                    "configKey='" + configKey + '\'' +
                    ", configValue='" + configValue + '\'' +
                    ", configGroup='" + configGroup + '\'' +
                    ", configType='" + configType + '\'' +
                    ", enabled=" + enabled +
                    ", requireRestart=" + requireRestart +
                    ", operation='" + operation + '\'' +
                    '}';
        }
    }

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public ConfigUpdateRequest() {
    }

    /**
     * 单个配置更新构造函数
     *
     * @param configKey 配置键名
     * @param configValue 配置值
     */
    public ConfigUpdateRequest(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.updateMode = "SINGLE";
    }

    /**
     * 批量配置更新构造函数
     *
     * @param configItems 配置项列表
     */
    public ConfigUpdateRequest(List<ConfigItem> configItems) {
        this.configItems = configItems;
        this.updateMode = "BATCH";
    }

    // ==================== Getter和Setter方法 ====================

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigGroup() {
        return configGroup;
    }

    public void setConfigGroup(String configGroup) {
        this.configGroup = configGroup;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getConfigDescription() {
        return configDescription;
    }

    public void setConfigDescription(String configDescription) {
        this.configDescription = configDescription;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getRequireRestart() {
        return requireRestart;
    }

    public void setRequireRestart(Boolean requireRestart) {
        this.requireRestart = requireRestart;
    }

    public String getUpdateReason() {
        return updateReason;
    }

    public void setUpdateReason(String updateReason) {
        this.updateReason = updateReason;
    }

    public List<ConfigItem> getConfigItems() {
        return configItems;
    }

    public void setConfigItems(List<ConfigItem> configItems) {
        this.configItems = configItems;
    }

    public String getUpdateMode() {
        return updateMode;
    }

    public void setUpdateMode(String updateMode) {
        this.updateMode = updateMode;
    }

    public Boolean getValidateConfig() {
        return validateConfig;
    }

    public void setValidateConfig(Boolean validateConfig) {
        this.validateConfig = validateConfig;
    }

    public Boolean getImmediateEffect() {
        return immediateEffect;
    }

    public void setImmediateEffect(Boolean immediateEffect) {
        this.immediateEffect = immediateEffect;
    }

    public Boolean getBackupCurrent() {
        return backupCurrent;
    }

    public void setBackupCurrent(Boolean backupCurrent) {
        this.backupCurrent = backupCurrent;
    }

    // ==================== 业务方法 ====================

    /**
     * 验证请求参数
     *
     * @return 验证结果
     */
    public boolean isValid() {
        if ("SINGLE".equals(updateMode)) {
            return configKey != null && !configKey.trim().isEmpty() &&
                   configValue != null;
        } else if ("BATCH".equals(updateMode)) {
            return configItems != null && !configItems.isEmpty() &&
                   configItems.stream().allMatch(item -> 
                           item.getConfigKey() != null && !item.getConfigKey().trim().isEmpty() &&
                           item.getConfigValue() != null);
        }
        return false;
    }

    /**
     * 判断是否为单个配置更新
     *
     * @return true-单个更新，false-批量更新
     */
    public boolean isSingleUpdate() {
        return "SINGLE".equals(updateMode);
    }

    /**
     * 判断是否为批量配置更新
     *
     * @return true-批量更新，false-单个更新
     */
    public boolean isBatchUpdate() {
        return "BATCH".equals(updateMode);
    }

    /**
     * 判断是否为合并更新
     *
     * @return true-合并更新，false-其他更新
     */
    public boolean isMergeUpdate() {
        return "MERGE".equals(updateMode);
    }

    /**
     * 判断是否为替换更新
     *
     * @return true-替换更新，false-其他更新
     */
    public boolean isReplaceUpdate() {
        return "REPLACE".equals(updateMode);
    }

    /**
     * 获取配置项数量
     *
     * @return 配置项数量
     */
    public int getConfigItemCount() {
        if (isSingleUpdate()) {
            return 1;
        } else if (configItems != null) {
            return configItems.size();
        }
        return 0;
    }

    /**
     * 获取需要重启的配置项数量
     *
     * @return 需要重启的配置项数量
     */
    public int getRestartRequiredCount() {
        if (isSingleUpdate()) {
            return Boolean.TRUE.equals(requireRestart) ? 1 : 0;
        } else if (configItems != null) {
            return (int) configItems.stream()
                    .filter(item -> Boolean.TRUE.equals(item.getRequireRestart()))
                    .count();
        }
        return 0;
    }

    /**
     * 判断是否有配置需要重启
     *
     * @return true-需要重启，false-不需要重启
     */
    public boolean hasRestartRequired() {
        return getRestartRequiredCount() > 0;
    }

    /**
     * 获取所有配置键名
     *
     * @return 配置键名列表
     */
    public List<String> getAllConfigKeys() {
        if (isSingleUpdate()) {
            return List.of(configKey);
        } else if (configItems != null) {
            return configItems.stream()
                    .map(ConfigItem::getConfigKey)
                    .toList();
        }
        return List.of();
    }

    /**
     * 转换为配置映射
     *
     * @return 配置映射
     */
    public Map<String, String> toConfigMap() {
        if (isSingleUpdate()) {
            return Map.of(configKey, configValue);
        } else if (configItems != null) {
            return configItems.stream()
                    .collect(java.util.stream.Collectors.toMap(
                            ConfigItem::getConfigKey,
                            ConfigItem::getConfigValue,
                            (existing, replacement) -> replacement
                    ));
        }
        return Map.of();
    }

    @Override
    public String toString() {
        return "ConfigUpdateRequest{" +
                "updateMode='" + updateMode + '\'' +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                ", configGroup='" + configGroup + '\'' +
                ", configType='" + configType + '\'' +
                ", enabled=" + enabled +
                ", requireRestart=" + requireRestart +
                ", updateReason='" + updateReason + '\'' +
                ", configItemCount=" + getConfigItemCount() +
                ", validateConfig=" + validateConfig +
                ", immediateEffect=" + immediateEffect +
                ", backupCurrent=" + backupCurrent +
                ", restartRequiredCount=" + getRestartRequiredCount() +
                '}';
    }
}

