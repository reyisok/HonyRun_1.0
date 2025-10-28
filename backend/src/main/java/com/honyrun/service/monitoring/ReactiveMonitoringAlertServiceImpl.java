package com.honyrun.service.monitoring;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 响应式监控告警服务实现类
 *
 * 核心功能：
 * - 告警规则管理：创建、更新、删除、启用/禁用告警规则
 * - 告警触发：基于监控指标自动触发告警
 * - 告警通知：支持多种通知方式（邮件、短信、Webhook等）
 * - 告警抑制：支持告警抑制和静默功能
 * - 告警统计：提供告警统计和趋势分析
 *
 * 技术特性：
 * - 响应式编程：基于Reactor实现异步处理
 * - Redis存储：使用Redis存储告警规则和事件
 * - 内存缓存：缓存活跃告警和规则提升性能
 * - 多通知器：支持多种告警通知方式
 * - 自动清理：定期清理过期告警数据
 *
 * 注意：此类不使用@Service注解，通过MonitoringConfig中的@Bean方法创建实例
 *
 * @author Mr.Rey
 * @created 2025-07-01 18:45:00
 * @modified 2025-07-01 18:45:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class ReactiveMonitoringAlertServiceImpl implements ReactiveMonitoringAlertService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveMonitoringAlertServiceImpl.class);

    private static final String ALERT_RULES_KEY = "monitoring:alert:rules";
    private static final String ALERT_EVENTS_KEY = "monitoring:alert:events";
    private static final String ALERT_SUPPRESSIONS_KEY = "monitoring:alert:suppressions";
    private static final String ALERT_STATISTICS_KEY = "monitoring:alert:statistics";

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    // 内存缓存
    private final Map<String, AlertRule> alertRulesCache = new ConcurrentHashMap<>();
    private final Map<String, AlertEvent> activeAlertsCache = new ConcurrentHashMap<>();
    private final Map<String, AlertSuppression> suppressionsCache = new ConcurrentHashMap<>();
    private final List<AlertNotifier> notifiers = new ArrayList<>();

    // 统计计数器
    private final AtomicLong totalAlertsCounter = new AtomicLong(0);
    private final AtomicInteger activeAlertsCounter = new AtomicInteger(0);
    private final AtomicLong lastRuleCheckTime = new AtomicLong(System.currentTimeMillis());

    // 告警历史记录（内存中保留最近1000条）
    private final List<AlertEvent> alertHistory = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_HISTORY_SIZE = 1000;

    /**
     * 构造器注入依赖
     *
     * @param redisTemplate Redis响应式模板
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-22 04:40:00
     * @version 1.0.0
     */
    public ReactiveMonitoringAlertServiceImpl(@Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void initialize() {
        LoggingUtil.info(logger, "初始化监控告警服务");

        // 初始化默认通知器
        initializeDefaultNotifiers();

        // 加载持久化的告警规则
        loadAlertRulesFromRedis()
            .doOnSuccess(count -> LoggingUtil.info(logger,
                "成功加载 {} 个告警规则", count))
            .doOnError(error -> LoggingUtil.error(logger,
                "加载告警规则失败", error))
            .subscribe();

        // 加载抑制规则
        loadSuppressionsFromRedis()
            .doOnSuccess(count -> LoggingUtil.info(logger,
                "成功加载 {} 个抑制规则", count))
            .subscribe();
    }

    // ==================== 告警规则管理 ====================

    @Override
    public Mono<Boolean> createAlertRule(AlertRule alertRule) {
        return Mono.fromCallable(() -> {
            if (alertRule.getId() == null) {
                alertRule.setId(UUID.randomUUID().toString());
            }
            alertRule.setCreatedAt(LocalDateTime.now());
            alertRule.setUpdatedAt(LocalDateTime.now());

            // 缓存到内存
            alertRulesCache.put(alertRule.getId(), alertRule);

            LoggingUtil.info(logger, "创建告警规则: {} (ID: {})", alertRule.getName(), alertRule.getId());

            return alertRule;
        })
        .flatMap(rule -> saveAlertRuleToRedis(rule))
        .map(result -> true)
        .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> updateAlertRule(String ruleId, AlertRule alertRule) {
        if (alertRule == null || ruleId == null || ruleId.trim().isEmpty()) {
            return Mono.just(false);
        }

        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "更新告警规则: {}", ruleId);

            // 验证规则存在
            if (!alertRulesCache.containsKey(ruleId)) {
                return false;
            }
            AlertRule existingRule = alertRulesCache.get(ruleId);
            if (existingRule == null) {
                throw new IllegalArgumentException("告警规则不存在: " + ruleId);
            }

            alertRule.setId(ruleId);
            alertRule.setCreatedAt(existingRule.getCreatedAt());
            alertRule.setUpdatedAt(LocalDateTime.now());

            // 更新缓存
            alertRulesCache.put(ruleId, alertRule);

            LoggingUtil.info(logger, "更新告警规则: {} (ID: {})", alertRule.getName(), ruleId);

            return alertRule;
        })
        .flatMap(rule -> saveAlertRuleToRedis((AlertRule) rule))
        .map(result -> true)
        .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> deleteAlertRule(String ruleId) {
        return Mono.fromCallable(() -> {
            AlertRule rule = alertRulesCache.remove(ruleId);
            if (rule == null) {
                throw new IllegalArgumentException("告警规则不存在: " + ruleId);
            }

            LoggingUtil.info(logger, "删除告警规则: {} (ID: {})", rule.getName(), ruleId);

            return ruleId;
        })
        .flatMap(id -> redisTemplate.opsForHash().remove(ALERT_RULES_KEY, id))
        .map(result -> result > 0L)
        .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> toggleAlertRule(String ruleId, boolean enabled) {
        return Mono.fromCallable(() -> {
            AlertRule rule = alertRulesCache.get(ruleId);
            if (rule == null) {
                throw new IllegalArgumentException("告警规则不存在: " + ruleId);
            }

            rule.setEnabled(enabled);
            rule.setUpdatedAt(LocalDateTime.now());

            LoggingUtil.info(logger, "{}告警规则: {} (ID: {})",
                enabled ? "启用" : "禁用", rule.getName(), ruleId);

            return rule;
        })
        .flatMap(rule -> saveAlertRuleToRedis(rule))
        .map(result -> true)
        .onErrorReturn(false);
    }

    @Override
    public Flux<AlertRule> getAllAlertRules() {
        return Flux.fromIterable(alertRulesCache.values());
    }

    @Override
    public Mono<AlertRule> getAlertRule(String ruleId) {
        return Mono.fromCallable(() -> alertRulesCache.get(ruleId))
            .switchIfEmpty(Mono.empty());
    }

    // ==================== 告警触发和处理 ====================

    @Override
    public Mono<Void> checkAlertConditions() {
        lastRuleCheckTime.set(System.currentTimeMillis());

        return Flux.fromIterable(alertRulesCache.values())
            .filter(AlertRule::isEnabled)
            .filter(rule -> !isSuppressed(rule.getId()))
            .flatMap(this::evaluateAlertRule)
            .then()
            .doOnSuccess(v -> LoggingUtil.debug(logger, "完成告警条件检查"))
            .doOnError(error -> LoggingUtil.error(logger, "告警条件检查失败", error));
    }

    @Override
    public Mono<AlertEvent> triggerAlert(String ruleId, double metricValue, Map<String, Object> context) {
        return Mono.fromCallable(() -> {
            AlertRule rule = alertRulesCache.get(ruleId);
            if (rule == null) {
                throw new IllegalArgumentException("告警规则不存在: " + ruleId);
            }

            // 检查是否在冷却期内
            if (isInCooldownPeriod(ruleId)) {
                return null; // 在冷却期内，不触发告警
            }

            AlertEvent alertEvent = new AlertEvent();
            alertEvent.setId(UUID.randomUUID().toString());
            alertEvent.setRuleId(ruleId);
            alertEvent.setRuleName(rule.getName());
            alertEvent.setAlertType(rule.getAlertType());
            alertEvent.setSeverity(rule.getSeverity());
            alertEvent.setStatus(AlertStatus.ACTIVE);
            alertEvent.setMessage(rule.getMessage());
            alertEvent.setMetricName(rule.getMetricName());
            alertEvent.setMetricValue(metricValue);
            alertEvent.setThreshold(rule.getThreshold());
            alertEvent.setContext(context != null ? context : new HashMap<>());
            alertEvent.setTriggeredAt(LocalDateTime.now());

            // 添加到活跃告警
            activeAlertsCache.put(alertEvent.getId(), alertEvent);
            activeAlertsCounter.incrementAndGet();
            totalAlertsCounter.incrementAndGet();

            // 添加到历史记录
            addToHistory(alertEvent);

            LoggingUtil.warn(logger, "触发告警: {} - {} (指标值: {}, 阈值: {})",
                rule.getName(), rule.getMessage(), metricValue, rule.getThreshold());

            return alertEvent;
        })
        .flatMap(alertEvent -> {
            if (alertEvent == null) {
                return Mono.empty();
            }
            return saveAlertEventToRedis(alertEvent)
                .then(sendAlertNotification(alertEvent))
                .thenReturn(alertEvent);
        });
    }

    @Override
    public Mono<AlertEvent> manualTriggerAlert(AlertType alertType, AlertSeverity severity,
                                             String message, Map<String, Object> context) {
        return Mono.fromCallable(() -> {
            AlertEvent alertEvent = new AlertEvent();
            alertEvent.setId(UUID.randomUUID().toString());
            alertEvent.setRuleId("MANUAL");
            alertEvent.setRuleName("手动触发告警");
            alertEvent.setAlertType(alertType);
            alertEvent.setSeverity(severity);
            alertEvent.setStatus(AlertStatus.ACTIVE);
            alertEvent.setMessage(message);
            alertEvent.setContext(context != null ? context : new HashMap<>());
            alertEvent.setTriggeredAt(LocalDateTime.now());

            // 添加到活跃告警
            activeAlertsCache.put(alertEvent.getId(), alertEvent);
            activeAlertsCounter.incrementAndGet();
            totalAlertsCounter.incrementAndGet();

            // 添加到历史记录
            addToHistory(alertEvent);

            LoggingUtil.warn(logger, "手动触发告警: {} (类型: {}, 级别: {})",
                message, alertType, severity);

            return alertEvent;
        })
        .flatMap(alertEvent -> saveAlertEventToRedis(alertEvent)
            .then(sendAlertNotification(alertEvent))
            .thenReturn(alertEvent));
    }

    @Override
    public Mono<Boolean> acknowledgeAlert(String alertId, String acknowledgedBy, String comment) {
        return Mono.fromCallable(() -> {
            AlertEvent alertEvent = activeAlertsCache.get(alertId);
            if (alertEvent == null) {
                throw new IllegalArgumentException("活跃告警不存在: " + alertId);
            }

            alertEvent.setStatus(AlertStatus.ACKNOWLEDGED);
            alertEvent.setAcknowledgedAt(LocalDateTime.now());
            alertEvent.setAcknowledgedBy(acknowledgedBy);
            alertEvent.setAcknowledgmentComment(comment);

            if (alertEvent.getTriggeredAt() != null) {
                alertEvent.setResponseTime(Duration.between(alertEvent.getTriggeredAt(), LocalDateTime.now()));
            }

            LoggingUtil.info(logger, "确认告警: {} by {} - {}", alertId, acknowledgedBy, comment);

            return alertEvent;
        })
        .flatMap(alertEvent -> saveAlertEventToRedis(alertEvent))
        .map(result -> true)
        .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> resolveAlert(String alertId, String resolvedBy, String resolution) {
        return Mono.fromCallable(() -> {
            AlertEvent alertEvent = activeAlertsCache.remove(alertId);
            if (alertEvent == null) {
                throw new IllegalArgumentException("活跃告警不存在: " + alertId);
            }

            alertEvent.setStatus(AlertStatus.RESOLVED);
            alertEvent.setResolvedAt(LocalDateTime.now());
            alertEvent.setResolvedBy(resolvedBy);
            alertEvent.setResolution(resolution);

            if (alertEvent.getTriggeredAt() != null) {
                alertEvent.setResolutionTime(Duration.between(alertEvent.getTriggeredAt(), LocalDateTime.now()));
            }

            activeAlertsCounter.decrementAndGet();

            LoggingUtil.info(logger, "解决告警: {} by {} - {}", alertId, resolvedBy, resolution);

            return alertEvent;
        })
        .flatMap(alertEvent -> saveAlertEventToRedis(alertEvent))
        .map(result -> true)
        .onErrorReturn(false);
    }

    // ==================== 告警通知 ====================

    @Override
    public Mono<Boolean> sendAlertNotification(AlertEvent alertEvent) {
        return Flux.fromIterable(notifiers)
            .filter(AlertNotifier::isEnabled)
            .filter(notifier -> notifier.supportsAlertType(alertEvent.getAlertType()))
            .filter(notifier -> notifier.supportsSeverity(alertEvent.getSeverity()))
            .flatMap(notifier -> notifier.sendNotification(alertEvent)
                .doOnSuccess(success -> {
                    if (success) {
                        LoggingUtil.info(logger, "通过 {} 发送告警通知成功: {}",
                            notifier.getName(), alertEvent.getId());
                    } else {
                        LoggingUtil.warn(logger, "通过 {} 发送告警通知失败: {}",
                            notifier.getName(), alertEvent.getId());
                    }
                })
                .onErrorReturn(false))
            .reduce(false, (acc, result) -> acc || result)
            .defaultIfEmpty(false);
    }

    @Override
    public Mono<Boolean> addNotifier(AlertNotifier notifier) {
        return Mono.fromCallable(() -> {
            notifiers.add(notifier);
            LoggingUtil.info(logger, "添加告警通知器: {}", notifier.getName());
            return true;
        });
    }

    @Override
    public Mono<Boolean> removeNotifier(String notifierName) {
        return Mono.fromCallable(() -> {
            boolean removed = notifiers.removeIf(notifier -> notifier.getName().equals(notifierName));
            if (removed) {
                LoggingUtil.info(logger, "移除告警通知器: {}", notifierName);
            }
            return removed;
        });
    }

    // ==================== 告警查询 ====================

    @Override
    public Flux<AlertEvent> getActiveAlerts() {
        return Flux.fromIterable(activeAlertsCache.values());
    }

    @Override
    public Flux<AlertEvent> getAlertHistory(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        return Flux.fromIterable(alertHistory)
            .filter(alert -> alert.getTriggeredAt().isAfter(cutoffTime))
            .sort((a1, a2) -> a2.getTriggeredAt().compareTo(a1.getTriggeredAt()));
    }

    @Override
    public Flux<AlertEvent> getAlertsByType(AlertType alertType, int hours) {
        return getAlertHistory(hours)
            .filter(alert -> alert.getAlertType() == alertType);
    }

    @Override
    public Flux<AlertEvent> getAlertsBySeverity(AlertSeverity severity, int hours) {
        return getAlertHistory(hours)
            .filter(alert -> alert.getSeverity() == severity);
    }

    // ==================== 告警抑制 ====================

    @Override
    public Mono<Boolean> suppressAlertRule(String ruleId, String reason, Duration duration, String suppressedBy) {
        return Mono.fromCallable(() -> {
            AlertSuppression suppression = new AlertSuppression();
            suppression.setId(UUID.randomUUID().toString());
            suppression.setRuleId(ruleId);

            AlertRule rule = alertRulesCache.get(ruleId);
            if (rule != null) {
                suppression.setRuleName(rule.getName());
            }

            suppression.setReason(reason);
            suppression.setDuration(duration);
            suppression.setSuppressedAt(LocalDateTime.now());
            suppression.setExpiresAt(LocalDateTime.now().plus(duration));
            suppression.setSuppressedBy(suppressedBy);
            suppression.setActive(true);

            suppressionsCache.put(ruleId, suppression);

            LoggingUtil.info(logger, "抑制告警规则: {} 原因: {} 时长: {}", ruleId, reason, duration);

            return suppression;
        })
        .flatMap(suppression -> saveSuppressionToRedis(suppression))
        .map(result -> true)
        .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> unsuppressAlertRule(String ruleId, String unsuppressedBy) {
        return Mono.fromCallable(() -> {
            AlertSuppression suppression = suppressionsCache.remove(ruleId);
            if (suppression == null) {
                throw new IllegalArgumentException("抑制规则不存在: " + ruleId);
            }

            suppression.setActive(false);

            LoggingUtil.info(logger, "取消抑制告警规则: {} by {}", ruleId, unsuppressedBy);

            return suppression;
        })
        .flatMap(suppression -> redisTemplate.opsForHash().remove(ALERT_SUPPRESSIONS_KEY, ruleId))
        .map(result -> result > 0L)
        .onErrorReturn(false);
    }

    @Override
    public Flux<AlertSuppression> getSuppressedRules() {
        return Flux.fromIterable(suppressionsCache.values())
            .filter(AlertSuppression::isActive)
            .filter(suppression -> suppression.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    // ==================== 告警统计 ====================

    @Override
    public Mono<AlertStatistics> getAlertStatistics() {
        return Mono.fromCallable(() -> calculateCurrentStatistics())
                .flatMap(stats -> {
                    // 保存更新的统计数据到 Redis
                    return redisTemplate.opsForValue().set(ALERT_STATISTICS_KEY, stats)
                            .then(Mono.just(stats));
                });
    }

    /**
     * 计算当前统计数据
     * @return 当前告警统计
     */
    private AlertStatistics calculateCurrentStatistics() {
        AlertStatistics stats = new AlertStatistics();
        stats.setTotalAlerts((int) totalAlertsCounter.get());
        stats.setActiveAlerts(activeAlertsCounter.get());

        // 统计不同状态的告警
        int acknowledged = 0, resolved = 0;
        int critical = 0, high = 0, medium = 0, low = 0;
        double totalResponseTime = 0, totalResolutionTime = 0;
        int responseCount = 0, resolutionCount = 0;

        for (AlertEvent alert : alertHistory) {
                switch (alert.getStatus()) {
                    case ACKNOWLEDGED:
                        acknowledged++;
                        break;
                    case RESOLVED:
                        resolved++;
                        break;
                    case ACTIVE:
                        // 活跃状态告警计数
                        break;
                    case SUPPRESSED:
                        // 抑制状态告警计数
                        break;
                }

                switch (alert.getSeverity()) {
                    case CRITICAL:
                        critical++;
                        break;
                    case HIGH:
                        high++;
                        break;
                    case MEDIUM:
                        medium++;
                        break;
                    case LOW:
                        low++;
                        break;
                }

                if (alert.getResponseTime() != null) {
                    totalResponseTime += alert.getResponseTime().toMillis();
                    responseCount++;
                }

                if (alert.getResolutionTime() != null) {
                    totalResolutionTime += alert.getResolutionTime().toMillis();
                    resolutionCount++;
                }
            }

        stats.setAcknowledgedAlerts(acknowledged);
        stats.setResolvedAlerts(resolved);
        stats.setCriticalAlerts(critical);
        stats.setHighAlerts(high);
        stats.setMediumAlerts(medium);
        stats.setLowAlerts(low);

        if (responseCount > 0) {
            stats.setAverageResponseTime(totalResponseTime / responseCount);
        }

        if (resolutionCount > 0) {
            stats.setAverageResolutionTime(totalResolutionTime / resolutionCount);
        }

        stats.setLastUpdateTime(LocalDateTime.now());

        return stats;
    }

    @Override
    public Mono<AlertTrend> getAlertTrend(int days) {
        return Mono.fromCallable(() -> {
            ReactiveMonitoringAlertService.AlertTrend trend = new ReactiveMonitoringAlertService.AlertTrend();
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);

            // 按天统计告警数量
            Map<String, Integer> dailyCountsMap = new HashMap<>();
            Map<AlertType, Integer> typeCountsMap = new HashMap<>();
            Map<AlertSeverity, Integer> severityCountsMap = new HashMap<>();

            for (AlertEvent alert : alertHistory) {
                if (alert.getTriggeredAt().isAfter(startDate)) {
                    String dateKey = alert.getTriggeredAt().toLocalDate().toString();
                    dailyCountsMap.merge(dateKey, 1, Integer::sum);
                    typeCountsMap.merge(alert.getAlertType(), 1, Integer::sum);
                    severityCountsMap.merge(alert.getSeverity(), 1, Integer::sum);
                }
            }

            List<DailyAlertCount> dailyCounts = dailyCountsMap.entrySet().stream()
                .map(entry -> new DailyAlertCount(
                    LocalDateTime.parse(entry.getKey() + "T00:00:00"),
                    entry.getValue()))
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());

            trend.setDailyCounts(dailyCounts);
            trend.setAlertsByType(typeCountsMap);
            trend.setAlertsBySeverity(severityCountsMap);

            // 计算趋势方向（简单的线性趋势）
            if (dailyCounts.size() >= 2) {
                int firstHalf = dailyCounts.subList(0, dailyCounts.size() / 2).stream()
                    .mapToInt(DailyAlertCount::getCount).sum();
                int secondHalf = dailyCounts.subList(dailyCounts.size() / 2, dailyCounts.size()).stream()
                    .mapToInt(DailyAlertCount::getCount).sum();
                trend.setTrendDirection(secondHalf - firstHalf);
            }

            trend.setAnalysisTime(LocalDateTime.now());

            return trend;
        });
    }

    @Override
    public Mono<AlertEfficiency> getAlertEfficiency() {
        return Mono.fromCallable(() -> {
            AlertEfficiency efficiency = new AlertEfficiency();

            double totalResponseTime = 0, totalResolutionTime = 0;
            int responseCount = 0, resolutionCount = 0;
            int totalProcessed = 0, autoResolved = 0, manualResolved = 0;

            for (AlertEvent alert : alertHistory) {
                if (alert.getStatus() == AlertStatus.RESOLVED || alert.getStatus() == AlertStatus.ACKNOWLEDGED) {
                    totalProcessed++;

                    if (alert.getResponseTime() != null) {
                        totalResponseTime += alert.getResponseTime().toMillis();
                        responseCount++;
                    }

                    if (alert.getResolutionTime() != null) {
                        totalResolutionTime += alert.getResolutionTime().toMillis();
                        resolutionCount++;

                        if (alert.getResolvedBy() != null) {
                            if (alert.getResolvedBy().startsWith("SYSTEM")) {
                                autoResolved++;
                            } else {
                                manualResolved++;
                            }
                        }
                    }
                }
            }

            if (responseCount > 0) {
                efficiency.setAverageResponseTime(totalResponseTime / responseCount);
            }

            if (resolutionCount > 0) {
                efficiency.setAverageResolutionTime(totalResolutionTime / resolutionCount);
            }

            if (totalAlertsCounter.get() > 0) {
                efficiency.setResolutionRate((double) totalProcessed / totalAlertsCounter.get() * 100);
            }

            efficiency.setTotalProcessedAlerts(totalProcessed);
            efficiency.setAutoResolvedAlerts(autoResolved);
            efficiency.setManualResolvedAlerts(manualResolved);
            efficiency.setCalculationTime(LocalDateTime.now());

            return efficiency;
        });
    }

    // ==================== 私有辅助方法 ====================

    private void initializeDefaultNotifiers() {
        // 添加日志通知器
        notifiers.add(new LogAlertNotifier());

        LoggingUtil.info(logger, "初始化默认告警通知器");
    }

    private Mono<Long> loadAlertRulesFromRedis() {
        return redisTemplate.opsForHash().entries(ALERT_RULES_KEY)
            .cast(Map.Entry.class)
            .map(entry -> (AlertRule) entry.getValue())
            .doOnNext(rule -> alertRulesCache.put(rule.getId(), rule))
            .count()
            .onErrorReturn(0L);
    }

    private Mono<Long> loadSuppressionsFromRedis() {
        return redisTemplate.opsForHash().entries(ALERT_SUPPRESSIONS_KEY)
            .cast(Map.Entry.class)
            .map(entry -> (AlertSuppression) entry.getValue())
            .filter(suppression -> suppression.getExpiresAt().isAfter(LocalDateTime.now()))
            .doOnNext(suppression -> suppressionsCache.put(suppression.getRuleId(), suppression))
            .count()
            .onErrorReturn(0L);
    }

    private Mono<Boolean> saveAlertRuleToRedis(AlertRule rule) {
        return redisTemplate.opsForHash().put(ALERT_RULES_KEY, rule.getId(), rule)
            .onErrorReturn(false);
    }

    private Mono<Boolean> saveAlertEventToRedis(AlertEvent event) {
        String key = ALERT_EVENTS_KEY + ":" + event.getTriggeredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return redisTemplate.opsForHash().put(key, event.getId(), event)
            .onErrorReturn(false);
    }

    private Mono<Boolean> saveSuppressionToRedis(AlertSuppression suppression) {
        return redisTemplate.opsForHash().put(ALERT_SUPPRESSIONS_KEY, suppression.getRuleId(), suppression)
            .onErrorReturn(false);
    }

    private Mono<Void> evaluateAlertRule(AlertRule rule) {
        // 这里应该根据规则的指标名称获取实际的指标值
        // 为了演示，我们使用模拟值
        return Mono.fromCallable(() -> {
            // 模拟获取指标值的逻辑
            double metricValue = getMetricValue(rule.getMetricName());

            // 评估告警条件
            boolean shouldAlert = evaluateCondition(metricValue, rule.getOperator(), rule.getThreshold());

            if (shouldAlert) {
                Map<String, Object> context = new HashMap<>();
                context.put("evaluatedAt", LocalDateTime.now());
                context.put("ruleName", rule.getName());

                return triggerAlert(rule.getId(), metricValue, context);
            }

            return Mono.<AlertEvent>empty();
        })
        .flatMap(mono -> mono)
        .then();
    }

    private double getMetricValue(String metricName) {
        // 这里应该从实际的监控系统获取指标值
        // 为了演示，返回随机值
        return Math.random() * 100;
    }

    private boolean evaluateCondition(double value, String operator, double threshold) {
        switch (operator) {
            case ">":
                return value > threshold;
            case "<":
                return value < threshold;
            case ">=":
                return value >= threshold;
            case "<=":
                return value <= threshold;
            case "==":
                return Math.abs(value - threshold) < 0.001;
            case "!=":
                return Math.abs(value - threshold) >= 0.001;
            default:
                return false;
        }
    }

    private boolean isSuppressed(String ruleId) {
        AlertSuppression suppression = suppressionsCache.get(ruleId);
        return suppression != null && suppression.isActive() &&
               suppression.getExpiresAt().isAfter(LocalDateTime.now());
    }

    private boolean isInCooldownPeriod(String ruleId) {
        // 检查该规则是否在冷却期内
        // 这里需要实现冷却期逻辑
        return false;
    }

    private void addToHistory(AlertEvent alertEvent) {
        synchronized (alertHistory) {
            alertHistory.add(alertEvent);
            if (alertHistory.size() > MAX_HISTORY_SIZE) {
                alertHistory.remove(0);
            }
        }
    }

    // ==================== 内部通知器实现 ====================

    /**
     * 日志告警通知器
     */
    private static class LogAlertNotifier implements AlertNotifier {
        @Override
        public String getName() {
            return "LogNotifier";
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public Mono<Boolean> sendNotification(AlertEvent alertEvent) {
            return Mono.fromCallable(() -> {
                String logMessage = String.format(
                    "告警通知 - 类型: %s, 级别: %s, 消息: %s, 指标: %s=%.2f (阈值: %.2f)",
                    alertEvent.getAlertType(),
                    alertEvent.getSeverity(),
                    alertEvent.getMessage(),
                    alertEvent.getMetricName(),
                    alertEvent.getMetricValue(),
                    alertEvent.getThreshold()
                );

                switch (alertEvent.getSeverity()) {
                    case CRITICAL:
                        LoggingUtil.error(logger, logMessage, (Object) null);
                        break;
                    case HIGH:
                        LoggingUtil.warn(logger, logMessage);
                        break;
                    case MEDIUM:
                        LoggingUtil.info(logger, logMessage);
                        break;
                    case LOW:
                        LoggingUtil.debug(logger, logMessage);
                        break;
                }

                return true;
            });
        }

        @Override
        public boolean supportsAlertType(AlertType alertType) {
            return true; // 支持所有告警类型
        }

        @Override
        public boolean supportsSeverity(AlertSeverity severity) {
            return true; // 支持所有严重级别
        }
    }
}

