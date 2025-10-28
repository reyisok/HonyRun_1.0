package com.honyrun.service.monitoring;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.honyrun.util.LoggingUtil;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import reactor.core.publisher.Mono;

/**
 * 路由日志记录服务
 * 提供详细的路由注册日志和性能监控，专注于后端路由管理，无可视化界面
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @version 1.0.0
 */
@Service
public class RouteLoggingService implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RouteLoggingService.class);

    private final MeterRegistry meterRegistry;
    private final Map<String, RouteStats> routeStatsMap = new ConcurrentHashMap<>();

    // Micrometer 指标
    private final Counter requestCounter;
    private final Counter errorCounter;
    private final Counter successCounter;
    private final Timer responseTimer;

    public RouteLoggingService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.requestCounter = Counter.builder("route.requests.total")
                .description("Total number of route requests")
                .register(meterRegistry);
        this.errorCounter = Counter.builder("route.errors.total")
                .description("Total number of route errors")
                .register(meterRegistry);
        this.successCounter = Counter.builder("route.success.total")
                .description("Total number of successful route requests")
                .register(meterRegistry);
        this.responseTimer = Timer.builder("route.response.duration")
                .description("Route response duration")
                .register(meterRegistry);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LoggingUtil.info(logger, "路由监控服务已启动，开始记录路由性能数据");
    }

    /**
     * 记录请求开始
     */
    public Mono<Timer.Sample> recordRequestStart(String path, String method) {
        return Mono.fromCallable(() -> {
            requestCounter.increment();
            RouteStats stats = routeStatsMap.computeIfAbsent(path, k -> new RouteStats(path, method));
            stats.incrementRequests();

            LoggingUtil.debug(logger, "路由请求开始: {} {}", method, path);
            return Timer.start(meterRegistry);
        });
    }

    /**
     * 记录请求完成
     */
    public Mono<Void> recordRequestComplete(String path, Timer.Sample sample, int statusCode, Duration duration) {
        return Mono.fromRunnable(() -> {
            sample.stop(responseTimer);

            RouteStats stats = routeStatsMap.get(path);
            if (stats != null) {
                stats.recordResponse(duration, statusCode >= 200 && statusCode < 400);
                if (statusCode >= 200 && statusCode < 400) {
                    successCounter.increment();
                } else {
                    errorCounter.increment();
                }
            }

            LoggingUtil.debug(logger, "路由请求完成: {} - 状态码: {}, 耗时: {}ms",
                    path, statusCode, duration.toMillis());
        });
    }

    /**
     * 记录请求错误
     */
    public Mono<Void> recordRequestError(String path, Timer.Sample sample, Throwable error) {
        return Mono.fromRunnable(() -> {
            sample.stop(responseTimer);
            errorCounter.increment();

            RouteStats stats = routeStatsMap.get(path);
            if (stats != null) {
                stats.incrementErrors();
            }

            LoggingUtil.error(logger, "路由请求错误: {} - 错误: {}", path, error.getMessage());
        });
    }

    /**
     * 获取路由统计信息
     */
    public Mono<Map<String, RouteStats>> getRouteStats() {
        return Mono.just(new ConcurrentHashMap<>(routeStatsMap));
    }

    /**
     * 获取热点路由（访问量最高的前N个）
     */
    public Mono<List<RouteStats>> getHotRoutes(int limit) {
        return Mono.fromCallable(() -> routeStatsMap.values().stream()
                .sorted((a, b) -> Long.compare(b.getRequestCount(), a.getRequestCount()))
                .limit(limit)
                .collect(Collectors.toList()));
    }

    /**
     * 获取慢路由（平均响应时间最长的前N个）
     */
    public Mono<List<RouteStats>> getSlowRoutes(int limit) {
        return Mono.fromCallable(() -> routeStatsMap.values().stream()
                .filter(stats -> stats.getRequestCount() > 0)
                .sorted((a, b) -> Double.compare(b.getAverageResponseTime(), a.getAverageResponseTime()))
                .limit(limit)
                .collect(Collectors.toList()));
    }

    /**
     * 获取错误路由（错误率最高的前N个）
     */
    public Mono<List<RouteStats>> getErrorRoutes(int limit) {
        return Mono.fromCallable(() -> routeStatsMap.values().stream()
                .filter(stats -> stats.getErrorCount() > 0)
                .sorted((a, b) -> Double.compare(b.getErrorRate(), a.getErrorRate()))
                .limit(limit)
                .collect(Collectors.toList()));
    }

    /**
     * 路由统计数据
     */
    public static class RouteStats {
        private final String path;
        private final String method;
        private final AtomicLong requestCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final LocalDateTime createdAt = LocalDateTime.now();
        private volatile LocalDateTime lastAccessed = LocalDateTime.now();

        public RouteStats(String path, String method) {
            this.path = path;
            this.method = method;
        }

        public void incrementRequests() {
            requestCount.incrementAndGet();
            lastAccessed = LocalDateTime.now();
        }

        public void incrementErrors() {
            errorCount.incrementAndGet();
        }

        public void recordResponse(Duration duration, boolean success) {
            totalResponseTime.addAndGet(duration.toMillis());
            if (!success) {
                incrementErrors();
            }
        }

        public String getPath() {
            return path;
        }

        public String getMethod() {
            return method;
        }

        public long getRequestCount() {
            return requestCount.get();
        }

        public long getErrorCount() {
            return errorCount.get();
        }

        public double getErrorRate() {
            long requests = requestCount.get();
            return requests > 0 ? (double) errorCount.get() / requests * 100 : 0.0;
        }

        public double getAverageResponseTime() {
            long requests = requestCount.get();
            return requests > 0 ? (double) totalResponseTime.get() / requests : 0.0;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public LocalDateTime getLastAccessed() {
            return lastAccessed;
        }
    }
}
