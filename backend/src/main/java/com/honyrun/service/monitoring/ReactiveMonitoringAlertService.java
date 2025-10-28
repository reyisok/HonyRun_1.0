package com.honyrun.service.monitoring;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 响应式监控告警服务接口
 * 
 * 提供完整的监控告警功能，包括：
 * - 告警规则管理
 * - 告警触发和处理
 * - 告警通知发送
 * - 告警历史记录
 * - 告警统计分析
 * - 告警抑制机制
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-02 当前时间
 * @version 1.0.0 - 初始版本
 */
public interface ReactiveMonitoringAlertService {

    // ==================== 告警规则管理 ====================

    /**
     * 创建告警规则
     * 
     * @param alertRule 告警规则
     * @return 创建结果的Mono
     */
    Mono<Boolean> createAlertRule(AlertRule alertRule);

    /**
     * 更新告警规则
     * 
     * @param ruleId 规则ID
     * @param alertRule 更新的告警规则
     * @return 更新结果的Mono
     */
    Mono<Boolean> updateAlertRule(String ruleId, AlertRule alertRule);

    /**
     * 删除告警规则
     * 
     * @param ruleId 规则ID
     * @return 删除结果的Mono
     */
    Mono<Boolean> deleteAlertRule(String ruleId);

    /**
     * 启用/禁用告警规则
     * 
     * @param ruleId 规则ID
     * @param enabled 是否启用
     * @return 操作结果的Mono
     */
    Mono<Boolean> toggleAlertRule(String ruleId, boolean enabled);

    /**
     * 获取所有告警规则
     * 
     * @return 告警规则列表的Flux
     */
    Flux<AlertRule> getAllAlertRules();

    /**
     * 根据ID获取告警规则
     * 
     * @param ruleId 规则ID
     * @return 告警规则的Mono
     */
    Mono<AlertRule> getAlertRule(String ruleId);

    // ==================== 告警触发和处理 ====================

    /**
     * 检查告警条件
     * 
     * @return 检查结果的Mono
     */
    Mono<Void> checkAlertConditions();

    /**
     * 触发告警
     * 
     * @param ruleId 规则ID
     * @param metricValue 指标值
     * @param context 上下文信息
     * @return 触发结果的Mono
     */
    Mono<AlertEvent> triggerAlert(String ruleId, double metricValue, Map<String, Object> context);

    /**
     * 手动触发告警
     * 
     * @param alertType 告警类型
     * @param severity 严重级别
     * @param message 告警消息
     * @param context 上下文信息
     * @return 触发结果的Mono
     */
    Mono<AlertEvent> manualTriggerAlert(AlertType alertType, AlertSeverity severity, String message, Map<String, Object> context);

    /**
     * 确认告警
     * 
     * @param alertId 告警ID
     * @param acknowledgedBy 确认人
     * @param comment 确认备注
     * @return 确认结果的Mono
     */
    Mono<Boolean> acknowledgeAlert(String alertId, String acknowledgedBy, String comment);

    /**
     * 解决告警
     * 
     * @param alertId 告警ID
     * @param resolvedBy 解决人
     * @param resolution 解决方案
     * @return 解决结果的Mono
     */
    Mono<Boolean> resolveAlert(String alertId, String resolvedBy, String resolution);

    // ==================== 告警通知 ====================

    /**
     * 发送告警通知
     * 
     * @param alertEvent 告警事件
     * @return 发送结果的Mono
     */
    Mono<Boolean> sendAlertNotification(AlertEvent alertEvent);

    /**
     * 添加通知渠道
     * 
     * @param notifier 通知器
     * @return 添加结果的Mono
     */
    Mono<Boolean> addNotifier(AlertNotifier notifier);

    /**
     * 移除通知渠道
     * 
     * @param notifierName 通知器名称
     * @return 移除结果的Mono
     */
    Mono<Boolean> removeNotifier(String notifierName);

    // ==================== 告警查询 ====================

    /**
     * 获取活跃告警
     * 
     * @return 活跃告警列表的Flux
     */
    Flux<AlertEvent> getActiveAlerts();

    /**
     * 获取告警历史
     * 
     * @param hours 查询小时数
     * @return 告警历史的Flux
     */
    Flux<AlertEvent> getAlertHistory(int hours);

    /**
     * 根据类型获取告警
     * 
     * @param alertType 告警类型
     * @param hours 查询小时数
     * @return 告警列表的Flux
     */
    Flux<AlertEvent> getAlertsByType(AlertType alertType, int hours);

    /**
     * 根据严重级别获取告警
     * 
     * @param severity 严重级别
     * @param hours 查询小时数
     * @return 告警列表的Flux
     */
    Flux<AlertEvent> getAlertsBySeverity(AlertSeverity severity, int hours);

    // ==================== 告警抑制 ====================

    /**
     * 抑制告警规则
     * 
     * @param ruleId 规则ID
     * @param reason 抑制原因
     * @param duration 抑制时长
     * @param suppressedBy 抑制人
     * @return 抑制结果的Mono
     */
    Mono<Boolean> suppressAlertRule(String ruleId, String reason, Duration duration, String suppressedBy);

    /**
     * 取消告警规则抑制
     * 
     * @param ruleId 规则ID
     * @param unsuppressedBy 取消抑制人
     * @return 取消结果的Mono
     */
    Mono<Boolean> unsuppressAlertRule(String ruleId, String unsuppressedBy);

    /**
     * 获取抑制的告警规则
     * 
     * @return 抑制规则列表的Flux
     */
    Flux<AlertSuppression> getSuppressedRules();

    // ==================== 告警统计 ====================

    /**
     * 获取告警统计
     * 
     * @return 告警统计的Mono
     */
    Mono<AlertStatistics> getAlertStatistics();

    /**
     * 获取告警趋势
     * 
     * @param days 查询天数
     * @return 告警趋势的Mono
     */
    Mono<AlertTrend> getAlertTrend(int days);

    /**
     * 获取告警效率统计
     * 
     * @return 告警效率统计的Mono
     */
    Mono<AlertEfficiency> getAlertEfficiency();

    // ==================== 内部类定义 ====================

    /**
     * 告警规则
     */
    class AlertRule {
        private String id;
        private String name;
        private String description;
        private AlertType alertType;
        private String metricName;
        private String operator; // >, <, >=, <=, ==, !=
        private double threshold;
        private AlertSeverity severity;
        private boolean enabled;
        private Duration cooldownPeriod;
        private String message;
        private Map<String, Object> metadata;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public AlertType getAlertType() { return alertType; }
        public void setAlertType(AlertType alertType) { this.alertType = alertType; }
        public String getMetricName() { return metricName; }
        public void setMetricName(String metricName) { this.metricName = metricName; }
        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }
        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }
        public AlertSeverity getSeverity() { return severity; }
        public void setSeverity(AlertSeverity severity) { this.severity = severity; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Duration getCooldownPeriod() { return cooldownPeriod; }
        public void setCooldownPeriod(Duration cooldownPeriod) { this.cooldownPeriod = cooldownPeriod; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    }

    /**
     * 告警事件
     */
    class AlertEvent {
        private String id;
        private String ruleId;
        private String ruleName;
        private AlertType alertType;
        private AlertSeverity severity;
        private AlertStatus status;
        private String message;
        private String metricName;
        private double metricValue;
        private double threshold;
        private Map<String, Object> context;
        private LocalDateTime triggeredAt;
        private LocalDateTime acknowledgedAt;
        private LocalDateTime resolvedAt;
        private String acknowledgedBy;
        private String resolvedBy;
        private String acknowledgmentComment;
        private String resolution;
        private Duration responseTime;
        private Duration resolutionTime;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public AlertType getAlertType() { return alertType; }
        public void setAlertType(AlertType alertType) { this.alertType = alertType; }
        public AlertSeverity getSeverity() { return severity; }
        public void setSeverity(AlertSeverity severity) { this.severity = severity; }
        public AlertStatus getStatus() { return status; }
        public void setStatus(AlertStatus status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getMetricName() { return metricName; }
        public void setMetricName(String metricName) { this.metricName = metricName; }
        public double getMetricValue() { return metricValue; }
        public void setMetricValue(double metricValue) { this.metricValue = metricValue; }
        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }
        public Map<String, Object> getContext() { return context; }
        public void setContext(Map<String, Object> context) { this.context = context; }
        public LocalDateTime getTriggeredAt() { return triggeredAt; }
        public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }
        public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
        public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
        public LocalDateTime getResolvedAt() { return resolvedAt; }
        public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
        public String getAcknowledgedBy() { return acknowledgedBy; }
        public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
        public String getResolvedBy() { return resolvedBy; }
        public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
        public String getAcknowledgmentComment() { return acknowledgmentComment; }
        public void setAcknowledgmentComment(String acknowledgmentComment) { this.acknowledgmentComment = acknowledgmentComment; }
        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }
        public Duration getResponseTime() { return responseTime; }
        public void setResponseTime(Duration responseTime) { this.responseTime = responseTime; }
        public Duration getResolutionTime() { return resolutionTime; }
        public void setResolutionTime(Duration resolutionTime) { this.resolutionTime = resolutionTime; }
    }

    /**
     * 告警通知器接口
     */
    interface AlertNotifier {
        String getName();
        boolean isEnabled();
        Mono<Boolean> sendNotification(AlertEvent alertEvent);
        boolean supportsAlertType(AlertType alertType);
        boolean supportsSeverity(AlertSeverity severity);
    }

    /**
     * 告警抑制
     */
    class AlertSuppression {
        private String id;
        private String ruleId;
        private String ruleName;
        private String reason;
        private Duration duration;
        private LocalDateTime suppressedAt;
        private LocalDateTime expiresAt;
        private String suppressedBy;
        private boolean active;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public Duration getDuration() { return duration; }
        public void setDuration(Duration duration) { this.duration = duration; }
        public LocalDateTime getSuppressedAt() { return suppressedAt; }
        public void setSuppressedAt(LocalDateTime suppressedAt) { this.suppressedAt = suppressedAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
        public String getSuppressedBy() { return suppressedBy; }
        public void setSuppressedBy(String suppressedBy) { this.suppressedBy = suppressedBy; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    /**
     * 告警统计
     */
    class AlertStatistics {
        private int totalAlerts;
        private int activeAlerts;
        private int acknowledgedAlerts;
        private int resolvedAlerts;
        private int criticalAlerts;
        private int highAlerts;
        private int mediumAlerts;
        private int lowAlerts;
        private double averageResponseTime;
        private double averageResolutionTime;
        private LocalDateTime lastUpdateTime;

        // Getters and Setters
        public int getTotalAlerts() { return totalAlerts; }
        public void setTotalAlerts(int totalAlerts) { this.totalAlerts = totalAlerts; }
        public int getActiveAlerts() { return activeAlerts; }
        public void setActiveAlerts(int activeAlerts) { this.activeAlerts = activeAlerts; }
        public int getAcknowledgedAlerts() { return acknowledgedAlerts; }
        public void setAcknowledgedAlerts(int acknowledgedAlerts) { this.acknowledgedAlerts = acknowledgedAlerts; }
        public int getResolvedAlerts() { return resolvedAlerts; }
        public void setResolvedAlerts(int resolvedAlerts) { this.resolvedAlerts = resolvedAlerts; }
        public int getCriticalAlerts() { return criticalAlerts; }
        public void setCriticalAlerts(int criticalAlerts) { this.criticalAlerts = criticalAlerts; }
        public int getHighAlerts() { return highAlerts; }
        public void setHighAlerts(int highAlerts) { this.highAlerts = highAlerts; }
        public int getMediumAlerts() { return mediumAlerts; }
        public void setMediumAlerts(int mediumAlerts) { this.mediumAlerts = mediumAlerts; }
        public int getLowAlerts() { return lowAlerts; }
        public void setLowAlerts(int lowAlerts) { this.lowAlerts = lowAlerts; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        public double getAverageResolutionTime() { return averageResolutionTime; }
        public void setAverageResolutionTime(double averageResolutionTime) { this.averageResolutionTime = averageResolutionTime; }
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    }

    /**
     * 告警趋势
     */
    class AlertTrend {
        private List<DailyAlertCount> dailyCounts;
        private Map<AlertType, Integer> alertsByType;
        private Map<AlertSeverity, Integer> alertsBySeverity;
        private double trendDirection; // 正数表示上升趋势，负数表示下降趋势
        private LocalDateTime analysisTime;

        // Getters and Setters
        public List<DailyAlertCount> getDailyCounts() { return dailyCounts; }
        public void setDailyCounts(List<DailyAlertCount> dailyCounts) { this.dailyCounts = dailyCounts; }
        public Map<AlertType, Integer> getAlertsByType() { return alertsByType; }
        public void setAlertsByType(Map<AlertType, Integer> alertsByType) { this.alertsByType = alertsByType; }
        public Map<AlertSeverity, Integer> getAlertsBySeverity() { return alertsBySeverity; }
        public void setAlertsBySeverity(Map<AlertSeverity, Integer> alertsBySeverity) { this.alertsBySeverity = alertsBySeverity; }
        public double getTrendDirection() { return trendDirection; }
        public void setTrendDirection(double trendDirection) { this.trendDirection = trendDirection; }
        public LocalDateTime getAnalysisTime() { return analysisTime; }
        public void setAnalysisTime(LocalDateTime analysisTime) { this.analysisTime = analysisTime; }
    }

    /**
     * 每日告警数量
     */
    class DailyAlertCount {
        private LocalDateTime date;
        private int count;

        public DailyAlertCount(LocalDateTime date, int count) {
            this.date = date;
            this.count = count;
        }

        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    /**
     * 告警效率
     */
    class AlertEfficiency {
        private double averageResponseTime;
        private double averageResolutionTime;
        private double resolutionRate;
        private int totalProcessedAlerts;
        private int autoResolvedAlerts;
        private int manualResolvedAlerts;
        private LocalDateTime calculationTime;

        // Getters and Setters
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        public double getAverageResolutionTime() { return averageResolutionTime; }
        public void setAverageResolutionTime(double averageResolutionTime) { this.averageResolutionTime = averageResolutionTime; }
        public double getResolutionRate() { return resolutionRate; }
        public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }
        public int getTotalProcessedAlerts() { return totalProcessedAlerts; }
        public void setTotalProcessedAlerts(int totalProcessedAlerts) { this.totalProcessedAlerts = totalProcessedAlerts; }
        public int getAutoResolvedAlerts() { return autoResolvedAlerts; }
        public void setAutoResolvedAlerts(int autoResolvedAlerts) { this.autoResolvedAlerts = autoResolvedAlerts; }
        public int getManualResolvedAlerts() { return manualResolvedAlerts; }
        public void setManualResolvedAlerts(int manualResolvedAlerts) { this.manualResolvedAlerts = manualResolvedAlerts; }
        public LocalDateTime getCalculationTime() { return calculationTime; }
        public void setCalculationTime(LocalDateTime calculationTime) { this.calculationTime = calculationTime; }
    }

    /**
     * 告警类型枚举
     */
    enum AlertType {
        SYSTEM_PERFORMANCE,    // 系统性能
        MEMORY_USAGE,         // 内存使用
        CPU_USAGE,            // CPU使用
        DISK_USAGE,           // 磁盘使用
        NETWORK_LATENCY,      // 网络延迟
        DATABASE_PERFORMANCE, // 数据库性能
        CACHE_PERFORMANCE,    // 缓存性能
        APPLICATION_ERROR,    // 应用错误
        SECURITY_THREAT,      // 安全威胁
        BUSINESS_METRIC,      // 业务指标
        CUSTOM                // 自定义
    }

    /**
     * 告警严重级别枚举
     */
    enum AlertSeverity {
        LOW,        // 低
        MEDIUM,     // 中
        HIGH,       // 高
        CRITICAL    // 严重
    }

    /**
     * 告警状态枚举
     */
    enum AlertStatus {
        ACTIVE,         // 活跃
        ACKNOWLEDGED,   // 已确认
        RESOLVED,       // 已解决
        SUPPRESSED      // 已抑制
    }
}

