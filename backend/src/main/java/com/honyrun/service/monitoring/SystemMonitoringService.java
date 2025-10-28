package com.honyrun.service.monitoring;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.honyrun.config.MonitoringProperties;
import com.honyrun.util.LoggingUtil;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

/**
 * 系统监控服务
 *
 * 提供全面的系统监控功能，包括：
 * - 系统资源监控（CPU、内存、磁盘）
 * - JVM性能监控（堆内存、GC、线程）
 * - 应用健康状态监控
 * - 性能指标收集和暴露
 * - 监控数据缓存和历史记录
 *
 * 监控策略：
 * - 定时收集系统指标，避免实时查询的性能开销
 * - 使用Micrometer集成，支持多种监控系统
 * - 提供健康检查接口，支持容器化部署
 * - 监控数据本地缓存，提高查询性能
 *
 * 性能优化：
 * - 异步收集监控数据，不阻塞主业务流程
 * - 合理设置监控频率，平衡准确性和性能
 * - 监控数据压缩存储，减少内存占用
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-26 01:36:12
 * @version 1.0.0
 */
@Service
public class SystemMonitoringService implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(SystemMonitoringService.class);

    private final MeterRegistry meterRegistry;
    private final MonitoringProperties monitoringProperties;

    // 系统监控指标
    private final Counter systemErrorCounter;
    private final Timer systemOperationTimer;
    private final AtomicLong lastUpdateTime = new AtomicLong(0);

    // 监控数据缓存
    private final Map<String, Object> monitoringCache = new ConcurrentHashMap<>();

    // JMX Bean引用
    private final OperatingSystemMXBean osBean;
    private final MemoryMXBean memoryBean;
    private final RuntimeMXBean runtimeBean;

    public SystemMonitoringService(MeterRegistry meterRegistry, MonitoringProperties monitoringProperties) {
        this.meterRegistry = meterRegistry;
        this.monitoringProperties = monitoringProperties;
        this.osBean = ManagementFactory.getOperatingSystemMXBean();
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.runtimeBean = ManagementFactory.getRuntimeMXBean();

        // 初始化监控指标
        this.systemErrorCounter = Counter.builder("honyrun.system.errors")
                .description("系统错误计数")
                .register(meterRegistry);

        this.systemOperationTimer = Timer.builder("honyrun.system.operations")
                .description("系统操作耗时")
                .register(meterRegistry);

        LoggingUtil.info(logger, "系统监控服务初始化完成");
    }

    /**
     * 初始化后处理 - 注册系统指标
     * 使用@PostConstruct确保在对象完全初始化后执行，避免this逃逸
     */
    @PostConstruct
    private void initializeSystemMetrics() {
        registerSystemMetrics();
        LoggingUtil.info(logger, "系统监控指标注册完成");
    }

    /**
     * 注册系统监控指标
     *
     * 注册各种系统资源和JVM性能指标到Micrometer注册表中，
     * 这些指标将被自动收集并暴露给监控系统。
     */
    private void registerSystemMetrics() {
        // CPU使用率指标
        Gauge.builder("honyrun.system.cpu.usage", this, SystemMonitoringService::getCpuUsage)
                .description("系统CPU使用率")
                .register(meterRegistry);

        // 内存使用指标
        Gauge.builder("honyrun.system.memory.used", this, SystemMonitoringService::getUsedMemory)
                .description("已使用内存（字节）")
                .register(meterRegistry);

        Gauge.builder("honyrun.system.memory.max", this, SystemMonitoringService::getMaxMemory)
                .description("最大可用内存（字节）")
                .register(meterRegistry);

        // JVM堆内存指标
        Gauge.builder("honyrun.jvm.heap.used", this, SystemMonitoringService::getHeapUsed)
                .description("JVM堆内存使用量（字节）")
                .register(meterRegistry);

        Gauge.builder("honyrun.jvm.heap.max", this, SystemMonitoringService::getHeapMax)
                .description("JVM堆内存最大值（字节）")
                .register(meterRegistry);

        // 系统负载指标
        Gauge.builder("honyrun.system.load.average", this, SystemMonitoringService::getSystemLoadAverage)
                .description("系统平均负载")
                .register(meterRegistry);

        // JVM运行时间指标
        Gauge.builder("honyrun.jvm.uptime", this, SystemMonitoringService::getJvmUptime)
                .description("JVM运行时间（毫秒）")
                .register(meterRegistry);
    }

    /**
     * 定时收集系统监控数据
     *
     * 使用配置的频率执行，收集系统资源使用情况并更新缓存。
     * 使用定时任务避免实时查询对性能的影响。
     */
    @Scheduled(fixedRateString = "#{@monitoringProperties.system.fixedRate}")
    public void collectSystemMetrics() {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            LoggingUtil.debug(logger, "开始收集系统监控数据");

            // 收集CPU使用率
            double cpuUsage = getCpuUsage();
            monitoringCache.put(monitoringProperties.getRedisKeys().getCpuUsage(), cpuUsage);

            // 收集内存使用情况
            long usedMemory = getUsedMemory();
            long maxMemory = getMaxMemory();
            double memoryUsagePercent = maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0;

            monitoringCache.put(monitoringProperties.getRedisKeys().getMemoryUsed(), usedMemory);
            monitoringCache.put(monitoringProperties.getRedisKeys().getMemoryMax(), maxMemory);
            monitoringCache.put(monitoringProperties.getRedisKeys().getMemoryUsagePercent(), memoryUsagePercent);

            // 收集JVM堆内存使用情况
            long heapUsed = getHeapUsed();
            long heapMax = getHeapMax();
            double heapUsagePercent = heapMax > 0 ? (double) heapUsed / heapMax * 100 : 0;

            monitoringCache.put(monitoringProperties.getRedisKeys().getHeapUsed(), heapUsed);
            monitoringCache.put(monitoringProperties.getRedisKeys().getHeapMax(), heapMax);
            monitoringCache.put(monitoringProperties.getRedisKeys().getHeapUsagePercent(), heapUsagePercent);

            // 收集系统负载
            double loadAverage = getSystemLoadAverage();
            monitoringCache.put(monitoringProperties.getRedisKeys().getLoadAverage(), loadAverage);

            // 更新最后更新时间
            lastUpdateTime.set(System.currentTimeMillis());

            // 检查告警阈值
            checkAlertThresholds(cpuUsage, memoryUsagePercent, heapUsagePercent, loadAverage);

            LoggingUtil.debug(logger, "系统监控数据收集完成 - CPU: {}%, 内存: {}%, 堆内存: {}%, 负载: {}",
                    String.format("%.2f", cpuUsage), String.format("%.2f", memoryUsagePercent),
                    String.format("%.2f", heapUsagePercent), String.format("%.2f", loadAverage));

        } catch (Exception e) {
            systemErrorCounter.increment();
            LoggingUtil.error(logger, "收集系统监控数据失败", e);
        } finally {
            sample.stop(systemOperationTimer);
        }
    }

    /**
     * 检查告警阈值
     *
     * 根据配置的阈值检查系统资源使用情况，
     * 超过阈值时记录告警日志。
     *
     * @param cpuUsage           CPU使用率
     * @param memoryUsagePercent 内存使用率
     * @param heapUsagePercent   堆内存使用率
     * @param loadAverage        系统负载
     */
    private void checkAlertThresholds(double cpuUsage, double memoryUsagePercent,
            double heapUsagePercent, double loadAverage) {
        // CPU使用率告警
        if (cpuUsage > monitoringProperties.getAlert().getCpuThreshold()) {
            LoggingUtil.warn(logger, "CPU使用率告警: {}% (阈值: {}%)",
                    String.format("%.2f", cpuUsage),
                    String.format("%.2f", monitoringProperties.getAlert().getCpuThreshold()));
        }

        // 内存使用率告警
        if (memoryUsagePercent > monitoringProperties.getAlert().getMemoryThreshold()) {
            LoggingUtil.warn(logger, "内存使用率告警: {}% (阈值: {}%)",
                    String.format("%.2f", memoryUsagePercent),
                    String.format("%.2f", monitoringProperties.getAlert().getMemoryThreshold()));
        }

        // 堆内存使用率告警
        if (heapUsagePercent > monitoringProperties.getAlert().getHeapThreshold()) {
            LoggingUtil.warn(logger, "JVM堆内存使用率告警: {}% (阈值: {}%)",
                    String.format("%.2f", heapUsagePercent),
                    String.format("%.2f", monitoringProperties.getAlert().getHeapThreshold()));
        }

        // 系统负载告警
        int availableProcessors = osBean.getAvailableProcessors();
        double loadThreshold = availableProcessors * monitoringProperties.getAlert().getLoadMultiplier();
        if (loadAverage > loadThreshold) {
            LoggingUtil.warn(logger, "系统负载告警: {} (阈值: {}, CPU核心数: {})",
                    String.format("%.2f", loadAverage), String.format("%.2f", loadThreshold), availableProcessors);
        }
    }

    /**
     * 获取系统监控概览
     *
     * @return 系统监控数据的响应式流
     */
    public Mono<Map<String, Object>> getSystemOverview() {
        return Mono.fromCallable(() -> {
            Map<String, Object> overview = new ConcurrentHashMap<>(monitoringCache);
            overview.put("last.update.time", lastUpdateTime.get());
            overview.put("jvm.uptime", getJvmUptime());
            overview.put("available.processors", osBean.getAvailableProcessors());
            return overview;
        });
    }

    /**
     * 获取详细的系统信息
     *
     * @return 详细系统信息的响应式流
     */
    public Mono<Map<String, Object>> getDetailedSystemInfo() {
        return Mono.fromCallable(() -> {
            Map<String, Object> details = new ConcurrentHashMap<>();

            // 操作系统信息
            details.put("os.name", System.getProperty("os.name"));
            details.put("os.version", System.getProperty("os.version"));
            details.put("os.arch", System.getProperty("os.arch"));

            // Java运行时信息
            details.put("java.version", System.getProperty("java.version"));
            details.put("java.vendor", System.getProperty("java.vendor"));
            details.put("java.home", System.getProperty("java.home"));

            // JVM信息
            details.put("jvm.name", runtimeBean.getVmName());
            details.put("jvm.version", runtimeBean.getVmVersion());
            details.put("jvm.vendor", runtimeBean.getVmVendor());

            // 系统资源信息
            details.put("available.processors", osBean.getAvailableProcessors());
            details.put("system.load.average", getSystemLoadAverage());

            // 内存信息
            details.put("heap.memory.used", getHeapUsed());
            details.put("heap.memory.max", getHeapMax());
            details.put("non.heap.memory.used", memoryBean.getNonHeapMemoryUsage().getUsed());
            details.put("non.heap.memory.max", memoryBean.getNonHeapMemoryUsage().getMax());

            return details;
        });
    }

    /**
     * 健康检查实现
     *
     * 实现Spring Boot Actuator的HealthIndicator接口，
     * 提供系统健康状态检查功能。
     */
    @Override
    public Health health() {
        try {
            // 检查系统资源使用情况
            double cpuUsage = getCpuUsage();
            double memoryUsagePercent = (double) getUsedMemory() / getMaxMemory() * 100;
            double heapUsagePercent = (double) getHeapUsed() / getHeapMax() * 100;

            // 判断系统健康状态
            boolean isHealthy = cpuUsage < monitoringProperties.getHealth().getCpuThreshold() &&
                    memoryUsagePercent < monitoringProperties.getHealth().getMemoryThreshold() &&
                    heapUsagePercent < monitoringProperties.getHealth().getHeapThreshold();

            Health.Builder healthBuilder = isHealthy ? Health.up() : Health.down();

            return healthBuilder
                    .withDetail("cpu.usage", String.format("%.2f%%", cpuUsage))
                    .withDetail("memory.usage", String.format("%.2f%%", memoryUsagePercent))
                    .withDetail("heap.usage", String.format("%.2f%%", heapUsagePercent))
                    .withDetail("load.average", getSystemLoadAverage())
                    .withDetail("uptime", getJvmUptime())
                    .withDetail("last.check", System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            LoggingUtil.error(logger, "系统健康检查失败", e);
            return Health.down(e)
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        }
    }

    // ========================================
    // 系统指标获取方法
    // ========================================

    /**
     * 获取CPU使用率
     *
     * @return CPU使用率（0-100）
     */
    private double getCpuUsage() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return sunOsBean.getProcessCpuLoad() * 100;
        }
        return osBean.getSystemLoadAverage();
    }

    /**
     * 获取已使用内存
     *
     * @return 已使用内存字节数
     */
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * 获取最大可用内存
     *
     * @return 最大可用内存字节数
     */
    private long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * 获取JVM堆内存使用量
     *
     * @return 堆内存使用字节数
     */
    private long getHeapUsed() {
        return memoryBean.getHeapMemoryUsage().getUsed();
    }

    /**
     * 获取JVM堆内存最大值
     *
     * @return 堆内存最大字节数
     */
    private long getHeapMax() {
        return memoryBean.getHeapMemoryUsage().getMax();
    }

    /**
     * 获取系统平均负载
     *
     * @return 系统平均负载
     */
    private double getSystemLoadAverage() {
        return osBean.getSystemLoadAverage();
    }

    /**
     * 获取JVM运行时间
     *
     * @return JVM运行时间（毫秒）
     */
    private long getJvmUptime() {
        return runtimeBean.getUptime();
    }
}
