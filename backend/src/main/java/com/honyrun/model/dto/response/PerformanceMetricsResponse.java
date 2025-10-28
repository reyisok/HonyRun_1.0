package com.honyrun.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 性能指标响应DTO
 *
 * 用于返回系统性能指标信息的响应对象，包含各种性能数据和监控指标
 * 支持响应式数据传输和JSON序列化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  22:25:00
 * @modified 2025-07-01 22:25:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PerformanceMetricsResponse {

    /**
     * 指标ID
     */
    @JsonProperty("id")
    private Long id;

    /**
     * 指标类型
     * CPU、MEMORY、DISK、NETWORK、DATABASE、REDIS、JVM、CUSTOM等
     */
    @JsonProperty("metricType")
    private String metricType;

    /**
     * 指标名称
     */
    @JsonProperty("metricName")
    private String metricName;

    /**
     * 指标值
     */
    @JsonProperty("metricValue")
    private Double metricValue;

    /**
     * 指标单位
     */
    @JsonProperty("unit")
    private String unit;

    /**
     * 指标描述
     */
    @JsonProperty("description")
    private String description;

    /**
     * 阈值（警告）
     */
    @JsonProperty("warningThreshold")
    private Double warningThreshold;

    /**
     * 阈值（严重）
     */
    @JsonProperty("criticalThreshold")
    private Double criticalThreshold;

    /**
     * 指标状态
     * NORMAL-正常，WARNING-警告，CRITICAL-严重，UNKNOWN-未知
     */
    @JsonProperty("status")
    private String status;

    /**
     * 标签信息
     */
    @JsonProperty("tags")
    private Map<String, String> tags;

    /**
     * 采集时间
     */
    @JsonProperty("collectionTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectionTime;

    /**
     * 数据来源
     */
    @JsonProperty("source")
    private String source;

    /**
     * 采集间隔（秒）
     */
    @JsonProperty("interval")
    private Integer interval;

    /**
     * 最小值
     */
    @JsonProperty("minValue")
    private Double minValue;

    /**
     * 最大值
     */
    @JsonProperty("maxValue")
    private Double maxValue;

    /**
     * 平均值
     */
    @JsonProperty("avgValue")
    private Double avgValue;

    /**
     * 趋势
     * UP-上升，DOWN-下降，STABLE-稳定，FLUCTUATING-波动
     */
    @JsonProperty("trend")
    private String trend;

    /**
     * 变化率（百分比）
     */
    @JsonProperty("changeRate")
    private Double changeRate;

    /**
     * 历史数据点数
     */
    @JsonProperty("dataPoints")
    private Integer dataPoints;

    /**
     * 扩展属性
     */
    @JsonProperty("properties")
    private Map<String, Object> properties;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public PerformanceMetricsResponse() {
        this.collectionTime = LocalDateTime.now();
        this.status = "NORMAL";
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取指标ID
     *
     * @return 指标ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置指标ID
     *
     * @param id 指标ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取指标类型
     *
     * @return 指标类型
     */
    public String getMetricType() {
        return metricType;
    }

    /**
     * 设置指标类型
     *
     * @param metricType 指标类型
     */
    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    /**
     * 获取指标名称
     *
     * @return 指标名称
     */
    public String getMetricName() {
        return metricName;
    }

    /**
     * 设置指标名称
     *
     * @param metricName 指标名称
     */
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    /**
     * 获取指标值
     *
     * @return 指标值
     */
    public Double getMetricValue() {
        return metricValue;
    }

    /**
     * 设置指标值
     *
     * @param metricValue 指标值
     */
    public void setMetricValue(Double metricValue) {
        this.metricValue = metricValue;
        // 自动计算状态
        this.status = calculateStatus(metricValue);
    }

    /**
     * 获取指标单位
     *
     * @return 指标单位
     */
    public String getUnit() {
        return unit;
    }

    /**
     * 设置指标单位
     *
     * @param unit 指标单位
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * 获取指标描述
     *
     * @return 指标描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置指标描述
     *
     * @param description 指标描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取警告阈值
     *
     * @return 警告阈值
     */
    public Double getWarningThreshold() {
        return warningThreshold;
    }

    /**
     * 设置警告阈值
     *
     * @param warningThreshold 警告阈值
     */
    public void setWarningThreshold(Double warningThreshold) {
        this.warningThreshold = warningThreshold;
    }

    /**
     * 获取严重阈值
     *
     * @return 严重阈值
     */
    public Double getCriticalThreshold() {
        return criticalThreshold;
    }

    /**
     * 设置严重阈值
     *
     * @param criticalThreshold 严重阈值
     */
    public void setCriticalThreshold(Double criticalThreshold) {
        this.criticalThreshold = criticalThreshold;
    }

    /**
     * 获取指标状态
     *
     * @return 指标状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置指标状态
     *
     * @param status 指标状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取标签信息
     *
     * @return 标签信息
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * 设置标签信息
     *
     * @param tags 标签信息
     */
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * 获取采集时间
     *
     * @return 采集时间
     */
    public LocalDateTime getCollectionTime() {
        return collectionTime;
    }

    /**
     * 设置采集时间
     *
     * @param collectionTime 采集时间
     */
    public void setCollectionTime(LocalDateTime collectionTime) {
        this.collectionTime = collectionTime;
    }

    /**
     * 获取数据来源
     *
     * @return 数据来源
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置数据来源
     *
     * @param source 数据来源
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 获取采集间隔
     *
     * @return 采集间隔（秒）
     */
    public Integer getInterval() {
        return interval;
    }

    /**
     * 设置采集间隔
     *
     * @param interval 采集间隔（秒）
     */
    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    /**
     * 获取最小值
     *
     * @return 最小值
     */
    public Double getMinValue() {
        return minValue;
    }

    /**
     * 设置最小值
     *
     * @param minValue 最小值
     */
    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    /**
     * 获取最大值
     *
     * @return 最大值
     */
    public Double getMaxValue() {
        return maxValue;
    }

    /**
     * 设置最大值
     *
     * @param maxValue 最大值
     */
    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * 获取平均值
     *
     * @return 平均值
     */
    public Double getAvgValue() {
        return avgValue;
    }

    /**
     * 设置平均值
     *
     * @param avgValue 平均值
     */
    public void setAvgValue(Double avgValue) {
        this.avgValue = avgValue;
    }

    /**
     * 获取趋势
     *
     * @return 趋势
     */
    public String getTrend() {
        return trend;
    }

    /**
     * 设置趋势
     *
     * @param trend 趋势
     */
    public void setTrend(String trend) {
        this.trend = trend;
    }

    /**
     * 获取变化率
     *
     * @return 变化率（百分比）
     */
    public Double getChangeRate() {
        return changeRate;
    }

    /**
     * 设置变化率
     *
     * @param changeRate 变化率（百分比）
     */
    public void setChangeRate(Double changeRate) {
        this.changeRate = changeRate;
    }

    /**
     * 获取历史数据点数
     *
     * @return 历史数据点数
     */
    public Integer getDataPoints() {
        return dataPoints;
    }

    /**
     * 设置历史数据点数
     *
     * @param dataPoints 历史数据点数
     */
    public void setDataPoints(Integer dataPoints) {
        this.dataPoints = dataPoints;
    }

    /**
     * 获取扩展属性
     *
     * @return 扩展属性
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * 设置扩展属性
     *
     * @param properties 扩展属性
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    // ==================== 工具方法 ====================

    /**
     * 判断指标是否正常
     *
     * @return true-正常，false-异常
     */
    public boolean isNormal() {
        return "NORMAL".equalsIgnoreCase(status);
    }

    /**
     * 判断指标是否警告
     *
     * @return true-警告，false-非警告
     */
    public boolean isWarning() {
        return "WARNING".equalsIgnoreCase(status);
    }

    /**
     * 判断指标是否严重
     *
     * @return true-严重，false-非严重
     */
    public boolean isCritical() {
        return "CRITICAL".equalsIgnoreCase(status);
    }

    /**
     * 根据指标值计算状态
     *
     * @param value 指标值
     * @return 状态
     */
    private String calculateStatus(Double value) {
        if (value == null) {
            return "UNKNOWN";
        }

        if (criticalThreshold != null && value >= criticalThreshold) {
            return "CRITICAL";
        }

        if (warningThreshold != null && value >= warningThreshold) {
            return "WARNING";
        }

        return "NORMAL";
    }

    /**
     * 获取格式化的指标值
     *
     * @return 格式化的指标值
     */
    public String getFormattedValue() {
        if (metricValue == null) {
            return "N/A";
        }

        if (unit != null) {
            if ("%".equals(unit)) {
                return String.format("%.2f%%", metricValue);
            } else if ("MB".equals(unit) || "GB".equals(unit)) {
                return String.format("%.2f %s", metricValue, unit);
            } else if ("ms".equals(unit)) {
                return String.format("%.0f %s", metricValue, unit);
            } else {
                return String.format("%.2f %s", metricValue, unit);
            }
        }

        return String.format("%.2f", metricValue);
    }

    /**
     * 获取状态颜色（用于前端显示）
     *
     * @return 状态颜色
     */
    public String getStatusColor() {
        switch (status != null ? status.toUpperCase() : "UNKNOWN") {
            case "NORMAL":
                return "green";
            case "WARNING":
                return "orange";
            case "CRITICAL":
                return "red";
            default:
                return "gray";
        }
    }

    /**
     * 获取趋势图标（用于前端显示）
     *
     * @return 趋势图标
     */
    public String getTrendIcon() {
        switch (trend != null ? trend.toUpperCase() : "STABLE") {
            case "UP":
                return "↗";
            case "DOWN":
                return "↘";
            case "FLUCTUATING":
                return "↕";
            case "STABLE":
            default:
                return "→";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerformanceMetricsResponse that = (PerformanceMetricsResponse) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(metricType, that.metricType) &&
               Objects.equals(metricName, that.metricName) &&
               Objects.equals(metricValue, that.metricValue) &&
               Objects.equals(collectionTime, that.collectionTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, metricType, metricName, metricValue, collectionTime);
    }

    @Override
    public String toString() {
        return "PerformanceMetricsResponse{" +
               "id=" + id +
               ", metricType='" + metricType + '\'' +
               ", metricName='" + metricName + '\'' +
               ", metricValue=" + metricValue +
               ", unit='" + unit + '\'' +
               ", status='" + status + '\'' +
               ", collectionTime=" + collectionTime +
               ", trend='" + trend + '\'' +
               ", changeRate=" + changeRate +
               '}';
    }
}


