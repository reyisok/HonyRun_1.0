package com.honyrun.model.entity.system;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 性能指标实体类
 *
 * 系统性能指标管理实体，支持性能数据、监控指标、统计信息等功能
 * 继承AuditableEntity，支持R2DBC响应式数据访问和审计功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  18:55:00
 * @modified 2025-07-01 18:55:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("sys_performance_metrics")
public class PerformanceMetrics extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 指标名称
     * 性能指标的名称标识
     */
    @Column("metric_name")
    private String metricName;

    /**
     * 指标类型
     * COUNTER-计数器，GAUGE-仪表，HISTOGRAM-直方图，TIMER-计时器
     */
    @Column("metric_type")
    private String metricType;

    /**
     * 指标值
     * 性能指标的数值
     */
    @Column("metric_value")
    private Double metricValue;

    /**
     * 指标单位
     * 性能指标的单位（如：ms、%、MB等）
     */
    @Column("metric_unit")
    private String metricUnit;

    /**
     * 指标分类
     * 性能指标的分类（如：CPU、MEMORY、NETWORK等）
     */
    @Column("category")
    private String category;

    /**
     * 采集时间
     * 指标采集的时间
     */
    @Column("collect_time")
    private LocalDateTime collectTime;

    /**
     * 最小值
     * 统计周期内的最小值
     */
    @Column("min_value")
    private Double minValue;

    /**
     * 最大值
     * 统计周期内的最大值
     */
    @Column("max_value")
    private Double maxValue;

    /**
     * 平均值
     * 统计周期内的平均值
     */
    @Column("avg_value")
    private Double avgValue;

    /**
     * 采样次数
     * 统计周期内的采样次数
     */
    @Column("sample_count")
    private Long sampleCount;

    /**
     * 标签信息
     * 指标的标签信息（JSON格式）
     */
    @Column("tags")
    private String tags;

    /**
     * 阈值配置
     * 指标的阈值配置（JSON格式）
     */
    @Column("threshold_config")
    private String thresholdConfig;

    /**
     * 告警状态
     * NORMAL-正常，WARNING-警告，CRITICAL-严重
     */
    @Column("alert_status")
    private String alertStatus;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public PerformanceMetrics() {
        super();
        this.collectTime = LocalDateTime.now();
        this.alertStatus = "NORMAL";
        this.sampleCount = 1L;
    }

    /**
     * 带参数的构造函数
     *
     * @param metricName 指标名称
     * @param metricType 指标类型
     * @param metricValue 指标值
     * @param category 指标分类
     */
    public PerformanceMetrics(String metricName, String metricType, Double metricValue, String category) {
        this();
        this.metricName = metricName;
        this.metricType = metricType;
        this.metricValue = metricValue;
        this.category = category;
    }

    // ==================== Getter和Setter方法 ====================

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public Double getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Double metricValue) {
        this.metricValue = metricValue;
    }

    public String getMetricUnit() {
        return metricUnit;
    }

    public void setMetricUnit(String metricUnit) {
        this.metricUnit = metricUnit;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(LocalDateTime collectTime) {
        this.collectTime = collectTime;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Double getAvgValue() {
        return avgValue;
    }

    public void setAvgValue(Double avgValue) {
        this.avgValue = avgValue;
    }

    public Long getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Long sampleCount) {
        this.sampleCount = sampleCount;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getThresholdConfig() {
        return thresholdConfig;
    }

    public void setThresholdConfig(String thresholdConfig) {
        this.thresholdConfig = thresholdConfig;
    }

    public String getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        this.alertStatus = alertStatus;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断是否为计数器类型
     */
    public boolean isCounter() {
        return "COUNTER".equalsIgnoreCase(this.metricType);
    }

    /**
     * 判断是否为仪表类型
     */
    public boolean isGauge() {
        return "GAUGE".equalsIgnoreCase(this.metricType);
    }

    /**
     * 判断是否有告警
     */
    public boolean hasAlert() {
        return !"NORMAL".equalsIgnoreCase(this.alertStatus);
    }

    /**
     * 更新统计值
     */
    public void updateStatistics(Double newValue) {
        if (newValue == null) return;

        if (this.minValue == null || newValue < this.minValue) {
            this.minValue = newValue;
        }
        if (this.maxValue == null || newValue > this.maxValue) {
            this.maxValue = newValue;
        }

        if (this.avgValue == null) {
            this.avgValue = newValue;
        } else {
            this.avgValue = (this.avgValue * this.sampleCount + newValue) / (this.sampleCount + 1);
        }

        this.sampleCount++;
        this.metricValue = newValue;
        this.collectTime = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        PerformanceMetrics that = (PerformanceMetrics) obj;
        return Objects.equals(metricName, that.metricName) &&
               Objects.equals(collectTime, that.collectTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), metricName, collectTime);
    }

    @Override
    public String toString() {
        return "PerformanceMetrics{" +
                "id=" + getId() +
                ", metricName='" + metricName + '\'' +
                ", metricType='" + metricType + '\'' +
                ", metricValue=" + metricValue +
                ", metricUnit='" + metricUnit + '\'' +
                ", category='" + category + '\'' +
                ", collectTime=" + collectTime +
                ", alertStatus='" + alertStatus + '\'' +
                '}';
    }
}


