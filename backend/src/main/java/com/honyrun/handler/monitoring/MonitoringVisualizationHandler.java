package com.honyrun.handler.monitoring;

import com.honyrun.service.monitoring.ReactiveMonitoringVisualizationService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.model.dto.response.ApiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 监控数据可视化处理器
 *
 * 提供监控数据可视化相关的HTTP接口处理，包括：
 * - 实时仪表板数据接口
 * - 系统概览数据接口
 * - 性能概览数据接口
 * - 各类趋势图表数据接口
 * - 告警统计和分布接口
 * - 性能监控图表接口
 * - 自定义图表管理接口
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-02 17:00:00
 * @modified 2025-07-02 17:00:00
 * @version 1.0.0
 */
@Component
public class MonitoringVisualizationHandler {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringVisualizationHandler.class);

    private final ReactiveMonitoringVisualizationService visualizationService;

    /**
     * 构造函数注入
     *
     * @param visualizationService 监控可视化服务
     */
    public MonitoringVisualizationHandler(ReactiveMonitoringVisualizationService visualizationService) {
        this.visualizationService = visualizationService;
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 仪表板数据接口 ====================

    /**
     * 获取实时监控仪表板数据
     *
     * @param request HTTP请求
     * @return 实时仪表板数据流
     */
    public Mono<ServerResponse> getRealTimeDashboardData(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取实时监控仪表板数据请求");

        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(visualizationService.getRealTimeDashboardData()
                        .map(data -> ApiResponse.success(data, "实时仪表板数据获取成功"))
                        .doOnNext(response -> LoggingUtil.debug(logger, "发送实时仪表板数据"))
                        .doOnError(error -> LoggingUtil.error(logger, "获取实时仪表板数据失败", error)),
                        ApiResponse.class)
                .doOnSuccess(response -> LoggingUtil.info(logger, "实时仪表板数据流建立成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "建立实时仪表板数据流失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取实时仪表板数据失败: " + error.getMessage()));
                });
    }

    /**
     * 获取系统概览数据
     *
     * @param request HTTP请求
     * @return 系统概览数据
     */
    public Mono<ServerResponse> getSystemOverview(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取系统概览数据请求");

        return visualizationService.getSystemOverview()
                .map(data -> ApiResponse.success(data, "系统概览数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "系统概览数据获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取系统概览数据失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取系统概览数据失败: " + error.getMessage()));
                });
    }

    /**
     * 获取性能概览数据
     *
     * @param request HTTP请求
     * @return 性能概览数据
     */
    public Mono<ServerResponse> getPerformanceOverview(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取性能概览数据请求");

        return visualizationService.getPerformanceOverview()
                .map(data -> ApiResponse.success(data, "性能概览数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "性能概览数据获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取性能概览数据失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取性能概览数据失败: " + error.getMessage()));
                });
    }

    // ==================== 趋势图表数据接口 ====================

    /**
     * 获取CPU使用率趋势图数据
     *
     * @param request HTTP请求
     * @return CPU使用率趋势数据
     */
    public Mono<ServerResponse> getCpuUsageTrend(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取CPU使用率趋势数据请求");

        return parseTrendRequest(request)
                .flatMap(params -> visualizationService.getCpuUsageTrend(
                        params.startTime, params.endTime, params.interval))
                .map(data -> ApiResponse.success(data, "CPU使用率趋势数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "CPU使用率趋势数据获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取CPU使用率趋势数据失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取CPU使用率趋势数据失败: " + error.getMessage()));
                });
    }

    /**
     * 获取内存使用率趋势图数据
     *
     * @param request HTTP请求
     * @return 内存使用率趋势数据
     */
    public Mono<ServerResponse> getMemoryUsageTrend(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取内存使用率趋势数据请求");

        return parseTrendRequest(request)
                .flatMap(params -> visualizationService.getMemoryUsageTrend(
                        params.startTime, params.endTime, params.interval))
                .map(data -> ApiResponse.success(data, "内存使用率趋势数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "内存使用率趋势数据获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取内存使用率趋势数据失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取内存使用率趋势数据失败: " + error.getMessage()));
                });
    }

    /**
     * 获取响应时间趋势图数据
     *
     * @param request HTTP请求
     * @return 响应时间趋势数据
     */
    public Mono<ServerResponse> getResponseTimeTrend(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取响应时间趋势数据请求");

        return parseTrendRequest(request)
                .flatMap(params -> visualizationService.getResponseTimeTrend(
                        params.startTime, params.endTime, params.interval))
                .map(data -> ApiResponse.success(data, "响应时间趋势数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "响应时间趋势数据获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取响应时间趋势数据失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取响应时间趋势数据失败: " + error.getMessage()));
                });
    }

    /**
     * 获取请求量趋势图数据
     *
     * @param request HTTP请求
     * @return 请求量趋势数据
     */
    public Mono<ServerResponse> getRequestCountTrend(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取请求量趋势数据请求");

        return parseTrendRequest(request)
                .flatMap(params -> visualizationService.getRequestCountTrend(
                        params.startTime, params.endTime, params.interval))
                .map(data -> ApiResponse.success(data, "请求量趋势数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "请求量趋势数据获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取请求量趋势数据失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取请求量趋势数据失败: " + error.getMessage()));
                });
    }

    /**
     * 获取错误率趋势图数据
     *
     * @param request HTTP请求
     * @return 错误率趋势数据
     */
    public Mono<ServerResponse> getErrorRateTrend(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取错误率趋势数据请求");

        return parseTrendRequest(request)
                .flatMap(params -> visualizationService.getErrorRateTrend(
                        params.startTime, params.endTime, params.interval))
                .map(data -> ApiResponse.success(data, "错误率趋势数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "错误率趋势数据获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取错误率趋势数据失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取错误率趋势数据失败: " + error.getMessage()));
                });
    }

    // ==================== 告警可视化接口 ====================

    /**
     * 获取告警统计图表数据
     *
     * @param request HTTP请求
     * @return 告警统计数据
     */
    public Mono<ServerResponse> getAlertStatistics(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取告警统计数据请求");

        int days = request.queryParam("days")
                .map(Integer::parseInt)
                .orElse(7);

        return visualizationService.getAlertStatistics(days)
                .map(data -> ApiResponse.success(data, "告警统计数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "告警统计数据获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取告警统计数据失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取告警统计数据失败: " + error.getMessage()));
                });
    }

    /**
     * 获取告警趋势图数据
     *
     * @param request HTTP请求
     * @return 告警趋势数据
     */
    public Mono<ServerResponse> getAlertTrend(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取告警趋势数据请求");

        return parseTimeRangeRequest(request)
                .flatMap(params -> visualizationService.getAlertTrend(params.startTime, params.endTime))
                .map(data -> ApiResponse.success(data, "告警趋势数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "告警趋势数据获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取告警趋势数据失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取告警趋势数据失败: " + error.getMessage()));
                });
    }

    /**
     * 获取告警分布饼图数据
     *
     * @param request HTTP请求
     * @return 告警分布数据
     */
    public Mono<ServerResponse> getAlertDistribution(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取告警分布数据请求");

        int days = request.queryParam("days")
                .map(Integer::parseInt)
                .orElse(7);

        return visualizationService.getAlertDistribution(days)
                .map(data -> ApiResponse.success(data, "告警分布数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "告警分布数据获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取告警分布数据失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取告警分布数据失败: " + error.getMessage()));
                });
    }

    // ==================== 性能可视化接口 ====================

    /**
     * 获取JVM性能图表数据
     *
     * @param request HTTP请求
     * @return JVM性能数据
     */
    public Mono<ServerResponse> getJvmPerformanceData(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取JVM性能数据请求");

        return parseTimeRangeRequest(request)
                .flatMap(params -> visualizationService.getJvmPerformanceData(params.startTime, params.endTime))
                .map(data -> ApiResponse.success(data, "JVM性能数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "JVM性能数据获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取JVM性能数据失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取JVM性能数据失败: " + error.getMessage()));
                });
    }

    /**
     * 获取数据库性能图表数据
     *
     * @param request HTTP请求
     * @return 数据库性能数据
     */
    public Mono<ServerResponse> getDatabasePerformanceData(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取数据库性能数据请求");

        return parseTimeRangeRequest(request)
                .flatMap(params -> visualizationService.getDatabasePerformanceData(params.startTime, params.endTime))
                .map(data -> ApiResponse.success(data, "数据库性能数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "数据库性能数据获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取数据库性能数据失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取数据库性能数据失败: " + error.getMessage()));
                });
    }

    /**
     * 获取缓存性能图表数据
     *
     * @param request HTTP请求
     * @return 缓存性能数据
     */
    public Mono<ServerResponse> getCachePerformanceData(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取缓存性能数据请求");

        return parseTimeRangeRequest(request)
                .flatMap(params -> visualizationService.getCachePerformanceData(params.startTime, params.endTime))
                .map(data -> ApiResponse.success(data, "缓存性能数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "缓存性能数据获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取缓存性能数据失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取缓存性能数据失败: " + error.getMessage()));
                });
    }

    // ==================== 自定义图表管理接口 ====================

    /**
     * 创建自定义图表
     *
     * @param request HTTP请求
     * @return 创建结果
     */
    public Mono<ServerResponse> createCustomChart(ServerRequest request) {
        LoggingUtil.info(logger, "处理创建自定义图表请求");

        return request.bodyToMono(ReactiveMonitoringVisualizationService.ChartConfig.class)
                .flatMap(config -> visualizationService.createCustomChart(config))
                .map(chartId -> ApiResponse.success(Map.of("chartId", chartId), "自定义图表创建成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "自定义图表创建成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "创建自定义图表失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("创建自定义图表失败: " + error.getMessage()));
                });
    }

    /**
     * 获取自定义图表数据
     *
     * @param request HTTP请求
     * @return 图表数据
     */
    public Mono<ServerResponse> getCustomChartData(ServerRequest request) {
        String chartId = request.pathVariable("chartId");
        LoggingUtil.info(logger, "处理获取自定义图表数据请求: {}", chartId);

        return request.bodyToMono(Map.class)
                .cast(Map.class)
                .defaultIfEmpty(Map.of())
                .flatMap(parameters -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typedParameters = (Map<String, Object>) parameters;
                    return visualizationService.getCustomChartData(chartId, typedParameters);
                })
                .map(data -> ApiResponse.success(data, "自定义图表数据获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "自定义图表数据获取成功: {}", chartId))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取自定义图表数据失败: {}", chartId, error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取自定义图表数据失败: " + error.getMessage()));
                });
    }

    /**
     * 获取所有自定义图表配置
     *
     * @param request HTTP请求
     * @return 图表配置列表
     */
    public Mono<ServerResponse> getAllCustomCharts(ServerRequest request) {
        LoggingUtil.info(logger, "处理获取所有自定义图表配置请求");

        return visualizationService.getAllCustomCharts()
                .collectList()
                .map(charts -> ApiResponse.success(charts, "自定义图表配置获取成功"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "所有自定义图表配置获取成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取自定义图表配置失败", error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("获取自定义图表配置失败: " + error.getMessage()));
                });
    }

    /**
     * 删除自定义图表
     *
     * @param request HTTP请求
     * @return 删除结果
     */
    public Mono<ServerResponse> deleteCustomChart(ServerRequest request) {
        String chartId = request.pathVariable("chartId");
        LoggingUtil.info(logger, "处理删除自定义图表请求: {}", chartId);

        return visualizationService.deleteCustomChart(chartId)
                .map(success -> success ?
                        ApiResponse.success(null, "自定义图表删除成功") :
                        ApiResponse.error("自定义图表不存在"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "自定义图表删除处理完成: {}", chartId))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "删除自定义图表失败: {}", chartId, error);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("删除自定义图表失败: " + error.getMessage()));
                });
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 解析趋势请求参数
     */
    private Mono<TrendRequestParams> parseTrendRequest(ServerRequest request) {
        return Mono.fromCallable(() -> {
            String startTimeStr = request.queryParam("startTime")
                    .orElse(LocalDateTime.now().minusHours(1).format(DATE_TIME_FORMATTER));
            String endTimeStr = request.queryParam("endTime")
                    .orElse(LocalDateTime.now().format(DATE_TIME_FORMATTER));
            int interval = request.queryParam("interval")
                    .map(Integer::parseInt)
                    .orElse(5);

            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DATE_TIME_FORMATTER);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DATE_TIME_FORMATTER);

            return new TrendRequestParams(startTime, endTime, interval);
        });
    }

    /**
     * 解析时间范围请求参数
     */
    private Mono<TimeRangeParams> parseTimeRangeRequest(ServerRequest request) {
        return Mono.fromCallable(() -> {
            String startTimeStr = request.queryParam("startTime")
                    .orElse(LocalDateTime.now().minusHours(1).format(DATE_TIME_FORMATTER));
            String endTimeStr = request.queryParam("endTime")
                    .orElse(LocalDateTime.now().format(DATE_TIME_FORMATTER));

            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DATE_TIME_FORMATTER);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DATE_TIME_FORMATTER);

            return new TimeRangeParams(startTime, endTime);
        });
    }

    // ==================== 内部类 ====================

    /**
     * 趋势请求参数
     */
    private static class TrendRequestParams {
        final LocalDateTime startTime;
        final LocalDateTime endTime;
        final int interval;

        TrendRequestParams(LocalDateTime startTime, LocalDateTime endTime, int interval) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.interval = interval;
        }
    }

    /**
     * 时间范围参数
     */
    private static class TimeRangeParams {
        final LocalDateTime startTime;
        final LocalDateTime endTime;

        TimeRangeParams(LocalDateTime startTime, LocalDateTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
