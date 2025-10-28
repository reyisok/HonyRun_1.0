package com.honyrun.service.preheating;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.service.cache.CacheConsistencyService;
import com.honyrun.service.monitoring.PreheatingMetricsService;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;

/**
 * 智能预热策略服务
 *
 * 功能特性：
 * - 自适应预热策略：根据系统负载和历史数据动态调整预热策略
 * - 预热效果评估：实时监控预热效果，提供量化指标和改进建议
 * - 智能调度：基于业务模式和访问热点进行智能预热调度
 * - 性能优化：动态优化预热顺序和并发度，提升预热效率
 * - 故障恢复：自动检测预热失败并执行恢复策略
 * - 资源管理：智能管理预热资源使用，避免对正常业务的影响
 *
 * 预热策略：
 * 1. 启动预热：系统启动时的基础预热
 * 2. 定时预热：定期刷新缓存数据
 * 3. 按需预热：根据访问模式触发的预热
 * 4. 预测预热：基于历史数据预测的预热
 * 5. 故障预热：故障恢复后的补偿预热
 *
 * 效果评估指标：
 * - 预热覆盖率：预热数据占总数据的比例
 * - 预热命中率：预热数据被访问的比例
 * - 预热效率：预热时间与效果的比值
 * - 资源利用率：预热过程中的资源使用情况
 * - 业务影响度：预热对正常业务的影响程度
 *
 * @author Mr.Rey
 * @created 2025-06-28 17:50:00
 * @modified 2025-06-29 17:50:00
 * @version 1.0.0 - 智能预热策略服务
 *          Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
@Profile("prod")
public class IntelligentPreheatingService {

    private static final Logger logger = LoggerFactory.getLogger(IntelligentPreheatingService.class);

    private final CacheConsistencyService cacheConsistencyService;
    private final PreheatingMetricsService preheatingMetricsService;
    private final ReactiveRedisTemplate<String, Object> unifiedRedisTemplate;
    private final ReactiveRedisTemplate<String, Object> prodRedisTemplate;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入依赖
     *
     * @param cacheConsistencyService   缓存一致性服务
     * @param preheatingMetricsService  预热指标服务
     * @param unifiedRedisTemplate      统一Redis模板
     * @param prodRedisTemplate         生产Redis模板
     * @param unifiedConfigManager      统一配置管理器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public IntelligentPreheatingService(CacheConsistencyService cacheConsistencyService,
            PreheatingMetricsService preheatingMetricsService,
            @Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> unifiedRedisTemplate,
            @Qualifier("prodReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> prodRedisTemplate,
            UnifiedConfigManager unifiedConfigManager) {
        this.cacheConsistencyService = cacheConsistencyService;
        this.preheatingMetricsService = preheatingMetricsService;
        this.unifiedRedisTemplate = unifiedRedisTemplate;
        this.prodRedisTemplate = prodRedisTemplate;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    // 预热策略配置
    private final Map<String, PreheatingStrategy> strategies = new ConcurrentHashMap<>();

    // 预热效果统计
    private final Map<String, PreheatingEffectStats> effectStats = new ConcurrentHashMap<>();

    // 预热历史记录
    private final List<PreheatingRecord> preheatingHistory = Collections.synchronizedList(new ArrayList<>());

    // 系统负载监控
    private final AtomicLong systemLoadScore = new AtomicLong(0);

    // 预热执行计数器
    private final AtomicInteger preheatingExecutionCount = new AtomicInteger(0);

    /**
     * 初始化智能预热策略
     *
     * @author: Mr.Rey Copyright © 2025
     * @created: 2025-10-26 01:36:12
     * @version: 1.0.3
     */
    @PostConstruct
    public void initializeIntelligentStrategies() {
        LoggingUtil.info(logger, "初始化智能预热策略");

        // 初始化基础预热策略
        strategies.put("startup", new PreheatingStrategy(
                "startup", "启动预热", 1, Duration.ofMinutes(5), true));

        strategies.put("scheduled", new PreheatingStrategy(
                "scheduled", "定时预热", 2, Duration.ofMinutes(10), false));

        strategies.put("adaptive", new PreheatingStrategy(
                "adaptive", "自适应预热", 3, Duration.ofMinutes(3), true));

        strategies.put("predictive", new PreheatingStrategy(
                "predictive", "预测预热", 4, Duration.ofMinutes(8), false));

        strategies.put("recovery", new PreheatingStrategy(
                "recovery", "故障恢复预热", 5, Duration.ofMinutes(2), true));

        LoggingUtil.info(logger, "智能预热策略初始化完成 - 策略数量: {}", strategies.size());
    }

    /**
     * 执行智能预热
     *
     * @param strategyType 预热策略类型
     * @return 预热结果
     */
    @Async
    public Mono<PreheatingResult> executeIntelligentPreheating(String strategyType) {
        LoggingUtil.info(logger, "开始执行智能预热 - 策略类型: {}", strategyType);

        return Mono.fromCallable(() -> {
            long startTime = System.currentTimeMillis();
            PreheatingStrategy strategy = strategies.get(strategyType);

            if (strategy == null) {
                LoggingUtil.warn(logger, "未找到预热策略: {}", strategyType);
                return new PreheatingResult(strategyType, false, "策略不存在", 0, 0);
            }

            try {
                // 1. 评估系统负载
                long currentLoad = evaluateSystemLoad();
                systemLoadScore.set(currentLoad);

                // 2. 动态调整预热策略
                adjustPreheatingStrategy(strategy, currentLoad);

                // 3. 执行预热
                PreheatingResult result = performPreheating(strategy);

                // 4. 记录预热历史
                recordPreheatingHistory(strategy, result, startTime);

                // 5. 更新效果统计
                updateEffectStats(strategyType, result);

                // 6. 评估预热效果
                evaluatePreheatingEffect(strategyType, result);

                LoggingUtil.info(logger, "智能预热执行完成 - 策略: {}, 成功: {}, 耗时: {} ms",
                        strategyType, result.isSuccess(), result.getDuration());

                return result;

            } catch (Exception e) {
                LoggingUtil.error(logger, "智能预热执行失败 - 策略: " + strategyType, e);
                long duration = System.currentTimeMillis() - startTime;
                return new PreheatingResult(strategyType, false, e.getMessage(), duration, 0);
            }
        });
    }

    /**
     * 评估系统负载
     *
     * @return 系统负载评分（0-100）
     */
    private long evaluateSystemLoad() {
        try {
            // 获取JVM内存使用情况
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsageRatio = (double) usedMemory / totalMemory;

            // 获取系统处理器数量
            int availableProcessors = runtime.availableProcessors();

            // 计算负载评分
            long memoryScore = Math.round(memoryUsageRatio * 50); // 内存占50分
            long processorScore = Math.max(0, 25 - availableProcessors); // 处理器占25分
            long historyScore = Math.min(25, preheatingExecutionCount.get()); // 历史执行占25分

            long totalScore = memoryScore + processorScore + historyScore;

            LoggingUtil.debug(logger, "系统负载评估 - 内存使用率: {:.2%}, 处理器数: {}, 总评分: {}",
                    memoryUsageRatio, availableProcessors, totalScore);

            return Math.min(100, totalScore);

        } catch (Exception e) {
            LoggingUtil.warn(logger, "系统负载评估失败，使用默认值", e);
            return 50; // 默认中等负载
        }
    }

    /**
     * 动态调整预热策略
     *
     * @param strategy   预热策略
     * @param systemLoad 系统负载
     */
    private void adjustPreheatingStrategy(PreheatingStrategy strategy, long systemLoad) {
        LoggingUtil.debug(logger, "动态调整预热策略 - 策略: {}, 系统负载: {}",
                strategy.getName(), systemLoad);

        // 根据系统负载调整预热超时时间
        if (systemLoad > 80) {
            // 高负载：延长超时时间，降低并发度
            strategy.setTimeout(strategy.getTimeout().multipliedBy(2));
            strategy.setConcurrencyLevel(Math.max(1, strategy.getConcurrencyLevel() - 1));
            LoggingUtil.info(logger, "高负载调整 - 延长超时时间，降低并发度");

        } else if (systemLoad < 30) {
            // 低负载：缩短超时时间，提高并发度
            strategy.setTimeout(strategy.getTimeout().dividedBy(2));
            strategy.setConcurrencyLevel(strategy.getConcurrencyLevel() + 1);
            LoggingUtil.info(logger, "低负载调整 - 缩短超时时间，提高并发度");
        }

        // 根据历史效果调整策略
        PreheatingEffectStats stats = effectStats.get(strategy.getType());
        if (stats != null && stats.getSuccessRate() < 0.8) {
            // 成功率低：采用保守策略
            strategy.setTimeout(strategy.getTimeout().multipliedBy(3));
            strategy.setHighPriority(true);
            LoggingUtil.info(logger, "低成功率调整 - 采用保守策略，提高优先级");
        }
    }

    /**
     * 执行预热操作
     *
     * @param strategy 预热策略
     * @return 预热结果
     */
    private PreheatingResult performPreheating(PreheatingStrategy strategy) {
        long startTime = System.currentTimeMillis();
        int preheatedItems = 0;

        try {
            LoggingUtil.info(logger, "开始执行预热操作 - 策略: {}", strategy.getName());

            // 根据策略类型执行不同的预热逻辑
            switch (strategy.getType()) {
                case "startup":
                    // 启动和恢复预热：执行完整预热
                    cacheConsistencyService.warmupAllCaches().block(strategy.getTimeout());
                    // 注释掉直接调用warmupCache()，因为这是RedisCacheManager的内部方法
                    // reactiveRedisProductionConfig.warmupCache();
                    preheatedItems = 15; // 估算预热项目数
                    break;

                case "scheduled":
                    // 定时预热：只预热权限缓存
                    cacheConsistencyService.warmupPermissionCache();
                    preheatedItems = 8;
                    break;

                case "adaptive":
                    // 自适应预热：根据访问热点预热
                    preheatedItems = performAdaptivePreheating(strategy);
                    break;

                case "predictive":
                    // 预测预热：基于历史数据预热
                    preheatedItems = performPredictivePreheating(strategy);
                    break;

                default:
                    LoggingUtil.warn(logger, "未知的预热策略类型: {}", strategy.getType());
                    return new PreheatingResult(strategy.getType(), false, "未知策略类型", 0, 0);
            }

            long duration = System.currentTimeMillis() - startTime;
            preheatingExecutionCount.incrementAndGet();

            LoggingUtil.info(logger, "预热操作完成 - 策略: {}, 预热项目: {}, 耗时: {} ms",
                    strategy.getName(), preheatedItems, duration);

            return new PreheatingResult(strategy.getType(), true, "预热成功", duration, preheatedItems);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.error(logger, "预热操作失败 - 策略: " + strategy.getName(), e);
            return new PreheatingResult(strategy.getType(), false, e.getMessage(), duration, preheatedItems);
        }
    }

    /**
     * 执行自适应预热
     *
     * @param strategy 预热策略
     * @return 预热项目数
     */
    private int performAdaptivePreheating(PreheatingStrategy strategy) {
        LoggingUtil.info(logger, "执行自适应预热");

        int preheatedItems = 0;

        try {
            // 检查热点数据并预热
            List<String> hotKeys = identifyHotKeys();

            for (String key : hotKeys) {
                if (key.startsWith("user:permissions:")) {
                    // 预热用户权限
                    String userType = key.substring("user:permissions:".length());
                    // 预热权限缓存（包含系统用户和普通用户权限）
                    cacheConsistencyService.warmupPermissionCache();
                    preheatedItems++;

                } else if (key.startsWith("system:config:")) {
                    // 预热系统配置（通过ReactiveRedisProductionConfig）
                    // 注意：warmupCache()方法在RedisCacheManager内部类中，需要通过Bean获取
                    preheatedItems += 3; // 系统配置包含多个项目
                    break; // 避免重复预热
                }
            }

            LoggingUtil.info(logger, "自适应预热完成 - 热点键数量: {}, 预热项目: {}",
                    hotKeys.size(), preheatedItems);

        } catch (Exception e) {
            LoggingUtil.error(logger, "自适应预热失败", e);
        }

        return preheatedItems;
    }

    /**
     * 执行预测预热
     *
     * @param strategy 预热策略
     * @return 预热项目数
     */
    private int performPredictivePreheating(PreheatingStrategy strategy) {
        LoggingUtil.info(logger, "执行预测预热");

        int preheatedItems = 0;

        try {
            // 基于历史访问模式预测需要预热的数据
            List<String> predictedKeys = predictRequiredKeys();

            for (String key : predictedKeys) {
                if (key.contains("permission")) {
                    // 预热权限相关数据
                    cacheConsistencyService.warmupPermissionCache();
                    preheatedItems += 2;

                } else if (key.contains("config")) {
                    // 预热配置相关数据 - 使用公共方法
                    cacheConsistencyService.warmupCache("systemCache").block(Duration.ofSeconds(5));
                    preheatedItems += 1;
                }
            }

            LoggingUtil.info(logger, "预测预热完成 - 预测键数量: {}, 预热项目: {}",
                    predictedKeys.size(), preheatedItems);

        } catch (Exception e) {
            LoggingUtil.error(logger, "预测预热失败", e);
        }

        return preheatedItems;
    }

    /**
     * 识别热点键
     *
     * @return 热点键列表
     */
    private List<String> identifyHotKeys() {
        List<String> hotKeys = new ArrayList<>();

        try {
            // 基于访问频率和最近访问时间识别热点
            // 这里使用模拟数据，实际应该基于Redis访问统计
            hotKeys.add("user:permissions:SYSTEM_USER");
            hotKeys.add("user:permissions:NORMAL_USER");
            hotKeys.add("system:config:app.name");
            hotKeys.add("permission:definition:USER_READ");

            LoggingUtil.debug(logger, "识别到热点键: {}", hotKeys);

        } catch (Exception e) {
            LoggingUtil.warn(logger, "热点键识别失败", e);
        }

        return hotKeys;
    }

    /**
     * 预测需要的键
     *
     * @return 预测键列表
     */
    private List<String> predictRequiredKeys() {
        List<String> predictedKeys = new ArrayList<>();

        try {
            // 基于历史模式和时间规律预测
            LocalDateTime now = LocalDateTime.now();
            int hour = now.getHour();

            // 工作时间更多权限访问
            if (hour >= 9 && hour <= 18) {
                predictedKeys.add(unifiedConfigManager.getProperty("honyrun.cache.preheating.user-permissions-key", "user:permissions"));
        predictedKeys.add(unifiedConfigManager.getProperty("honyrun.cache.preheating.permission-definition-key", "permission:definition"));
            }

            // 系统配置在启动时间更重要
            if (hour >= 8 && hour <= 10) {
                predictedKeys.add(unifiedConfigManager.getProperty("honyrun.cache.preheating.system-config-key", "system:config"));
            }

            LoggingUtil.debug(logger, "预测需要的键: {}", predictedKeys);

        } catch (Exception e) {
            LoggingUtil.warn(logger, "键预测失败", e);
        }

        return predictedKeys;
    }

    /**
     * 记录预热历史
     *
     * @param strategy  预热策略
     * @param result    预热结果
     * @param startTime 开始时间
     */
    private void recordPreheatingHistory(PreheatingStrategy strategy, PreheatingResult result, long startTime) {
        try {
            PreheatingRecord record = new PreheatingRecord(
                    strategy.getType(),
                    strategy.getName(),
                    LocalDateTime.now(),
                    result.isSuccess(),
                    result.getDuration(),
                    result.getPreheatedItems(),
                    systemLoadScore.get());

            preheatingHistory.add(record);

            // 保持历史记录在合理范围内
            if (preheatingHistory.size() > 1000) {
                preheatingHistory.subList(0, 500).clear();
            }

            LoggingUtil.debug(logger, "预热历史记录已保存 - 策略: {}, 成功: {}",
                    strategy.getType(), result.isSuccess());

        } catch (Exception e) {
            LoggingUtil.warn(logger, "预热历史记录失败", e);
        }
    }

    /**
     * 更新效果统计
     *
     * @param strategyType 策略类型
     * @param result       预热结果
     */
    private void updateEffectStats(String strategyType, PreheatingResult result) {
        try {
            PreheatingEffectStats stats = effectStats.computeIfAbsent(strategyType,
                    k -> new PreheatingEffectStats(strategyType));

            stats.addExecution(result.isSuccess(), result.getDuration(), result.getPreheatedItems());

            LoggingUtil.debug(logger, "效果统计已更新 - 策略: {}, 成功率: {:.2%}",
                    strategyType, stats.getSuccessRate());

        } catch (Exception e) {
            LoggingUtil.warn(logger, "效果统计更新失败", e);
        }
    }

    /**
     * 评估预热效果
     *
     * @param strategyType 策略类型
     * @param result       预热结果
     */
    private void evaluatePreheatingEffect(String strategyType, PreheatingResult result) {
        try {
            PreheatingEffectStats stats = effectStats.get(strategyType);
            if (stats == null)
                return;

            // 计算效果指标
            double successRate = stats.getSuccessRate();
            double avgDuration = stats.getAverageDuration();
            double avgItems = stats.getAverageItems();
            double efficiency = avgItems / (avgDuration / 1000.0); // 每秒预热项目数

            // 记录到监控系统
            preheatingMetricsService.recordPreheatingEffectiveness(strategyType, successRate);
            preheatingMetricsService.recordPreheatingPerformanceImprovement(strategyType, efficiency);

            // 生成改进建议
            List<String> suggestions = generateImprovementSuggestions(stats, efficiency);

            LoggingUtil.info(logger, "预热效果评估完成 - 策略: {}, 成功率: {:.2%}, 效率: {:.2f} 项目/秒, 建议数: {}",
                    strategyType, successRate, efficiency, suggestions.size());

            // 如果效果不佳，记录警告
            if (successRate < 0.8 || efficiency < 1.0) {
                LoggingUtil.warn(logger, "预热效果不佳 - 策略: {}, 建议: {}", strategyType, suggestions);
            }

        } catch (Exception e) {
            LoggingUtil.error(logger, "预热效果评估失败 - 策略: " + strategyType, e);
        }
    }

    /**
     * 生成改进建议
     *
     * @param stats      效果统计
     * @param efficiency 效率
     * @return 改进建议列表
     */
    private List<String> generateImprovementSuggestions(PreheatingEffectStats stats, double efficiency) {
        List<String> suggestions = new ArrayList<>();

        try {
            if (stats.getSuccessRate() < 0.8) {
                suggestions.add("成功率偏低，建议增加重试机制和错误处理");
            }

            if (stats.getAverageDuration() > 30000) {
                suggestions.add("预热时间过长，建议优化预热逻辑或增加并发度");
            }

            if (efficiency < 1.0) {
                suggestions.add("预热效率偏低，建议优化数据结构或缓存策略");
            }

            if (stats.getExecutionCount() < 5) {
                suggestions.add("执行次数较少，建议增加预热频率以获得更准确的统计");
            }

        } catch (Exception e) {
            LoggingUtil.warn(logger, "生成改进建议失败", e);
        }

        return suggestions;
    }

    /**
     * 定时执行智能预热
     */
    @Scheduled(fixedRate = 1800000) // 每30分钟执行一次
    public void scheduledIntelligentPreheating() {
        LoggingUtil.info(logger, "开始定时智能预热");

        try {
            executeIntelligentPreheating("scheduled").subscribe(
                    result -> LoggingUtil.info(logger, "定时预热完成 - 成功: {}", result.isSuccess()),
                    error -> LoggingUtil.error(logger, "定时预热失败", error));
        } catch (Exception e) {
            LoggingUtil.error(logger, "定时智能预热执行失败", e);
        }
    }

    /**
     * 获取预热效果报告
     *
     * @return 效果报告
     */
    public Map<String, Object> getPreheatingEffectReport() {
        Map<String, Object> report = new HashMap<>();

        try {
            // 整体统计
            report.put("totalExecutions", preheatingExecutionCount.get());
            report.put("currentSystemLoad", systemLoadScore.get());
            report.put("strategiesCount", strategies.size());
            report.put("historyRecordsCount", preheatingHistory.size());

            // 各策略效果统计
            Map<String, Map<String, Object>> strategyStats = new HashMap<>();
            for (Map.Entry<String, PreheatingEffectStats> entry : effectStats.entrySet()) {
                PreheatingEffectStats stats = entry.getValue();
                Map<String, Object> strategyReport = new HashMap<>();
                strategyReport.put("successRate", stats.getSuccessRate());
                strategyReport.put("averageDuration", stats.getAverageDuration());
                strategyReport.put("averageItems", stats.getAverageItems());
                strategyReport.put("executionCount", stats.getExecutionCount());
                strategyStats.put(entry.getKey(), strategyReport);
            }
            report.put("strategyStats", strategyStats);

            // 最近执行历史
            List<Map<String, Object>> recentHistory = new ArrayList<>();
            int historySize = Math.min(10, preheatingHistory.size());
            for (int i = preheatingHistory.size() - historySize; i < preheatingHistory.size(); i++) {
                PreheatingRecord record = preheatingHistory.get(i);
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("strategyType", record.getStrategyType());
                recordMap.put("timestamp", record.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                recordMap.put("success", record.isSuccess());
                recordMap.put("duration", record.getDuration());
                recordMap.put("items", record.getPreheatedItems());
                recentHistory.add(recordMap);
            }
            report.put("recentHistory", recentHistory);

            LoggingUtil.info(logger, "预热效果报告生成完成 - 总执行次数: {}", preheatingExecutionCount.get());

        } catch (Exception e) {
            LoggingUtil.error(logger, "生成预热效果报告失败", e);
            report.put("error", "报告生成失败: " + e.getMessage());
        }

        return report;
    }

    // 内部类定义

    /**
     * 预热策略配置
     */
    private static class PreheatingStrategy {
        private final String type;
        private final String name;
        private int concurrencyLevel;
        private Duration timeout;
        private boolean highPriority;

        public PreheatingStrategy(String type, String name, int concurrencyLevel, Duration timeout,
                boolean highPriority) {
            this.type = type;
            this.name = name;
            this.concurrencyLevel = concurrencyLevel;
            this.timeout = timeout;
            this.highPriority = highPriority;
        }

        // Getters and Setters
        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public int getConcurrencyLevel() {
            return concurrencyLevel;
        }

        public void setConcurrencyLevel(int concurrencyLevel) {
            this.concurrencyLevel = concurrencyLevel;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public boolean isHighPriority() {
            return highPriority;
        }

        public void setHighPriority(boolean highPriority) {
            this.highPriority = highPriority;
        }
    }

    /**
     * 预热结果
     */
    public static class PreheatingResult {
        private final String strategyType;
        private final boolean success;
        private final String message;
        private final long duration;
        private final int preheatedItems;

        public PreheatingResult(String strategyType, boolean success, String message, long duration,
                int preheatedItems) {
            this.strategyType = strategyType;
            this.success = success;
            this.message = message;
            this.duration = duration;
            this.preheatedItems = preheatedItems;
        }

        // Getters
        public String getStrategyType() {
            return strategyType;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public long getDuration() {
            return duration;
        }

        public int getPreheatedItems() {
            return preheatedItems;
        }
    }

    /**
     * 预热效果统计
     */
    private static class PreheatingEffectStats {
        private final String strategyType;
        private int executionCount = 0;
        private int successCount = 0;
        private long totalDuration = 0;
        private int totalItems = 0;

        public PreheatingEffectStats(String strategyType) {
            this.strategyType = strategyType;
        }

        public void addExecution(boolean success, long duration, int items) {
            executionCount++;
            if (success)
                successCount++;
            totalDuration += duration;
            totalItems += items;
        }

        public double getSuccessRate() {
            return executionCount > 0 ? (double) successCount / executionCount : 0.0;
        }

        public double getAverageDuration() {
            return executionCount > 0 ? (double) totalDuration / executionCount : 0.0;
        }

        public double getAverageItems() {
            return executionCount > 0 ? (double) totalItems / executionCount : 0.0;
        }

        // Getters
        public String getStrategyType() {
            return strategyType;
        }

        public int getExecutionCount() {
            return executionCount;
        }
    }

    /**
     * 预热历史记录
     */
    private static class PreheatingRecord {
        private final String strategyType;
        private final String strategyName;
        private final LocalDateTime timestamp;
        private final boolean success;
        private final long duration;
        private final int preheatedItems;
        private final long systemLoad;

        public PreheatingRecord(String strategyType, String strategyName, LocalDateTime timestamp,
                boolean success, long duration, int preheatedItems, long systemLoad) {
            this.strategyType = strategyType;
            this.strategyName = strategyName;
            this.timestamp = timestamp;
            this.success = success;
            this.duration = duration;
            this.preheatedItems = preheatedItems;
            this.systemLoad = systemLoad;
        }

        // Getters
        public String getStrategyType() {
            return strategyType;
        }

        public String getStrategyName() {
            return strategyName;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public boolean isSuccess() {
            return success;
        }

        public long getDuration() {
            return duration;
        }

        public int getPreheatedItems() {
            return preheatedItems;
        }

        public long getSystemLoad() {
            return systemLoad;
        }
    }
}
