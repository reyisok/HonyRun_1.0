package com.honyrun.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;

import com.honyrun.annotation.ReactiveService;
import com.honyrun.config.MonitoringProperties;
import com.honyrun.constant.PathConstants;
import com.honyrun.exception.BusinessException;
import com.honyrun.model.dto.request.SystemLogQueryRequest;
import com.honyrun.model.dto.response.SystemLogExportResponse;
import com.honyrun.model.dto.response.SystemLogResponse;
import com.honyrun.model.entity.system.SystemLog;
import com.honyrun.repository.r2dbc.ReactiveSystemLogRepository;
import com.honyrun.service.reactive.ReactiveSystemLogService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.MonitoringLogUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 响应式系统日志服务实现类
 *
 * 基于WebFlux的响应式系统日志服务实现，提供非阻塞的日志管理功能
 * 支持日志查询、过滤、导出、统计等运维功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 10:50:00
 * @modified 2025-07-01 10:50:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
@ReactiveService
public class ReactiveSystemLogServiceImpl implements ReactiveSystemLogService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveSystemLogServiceImpl.class);

    private final ReactiveSystemLogRepository systemLogRepository;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final MonitoringProperties monitoringProperties;

    // 导出任务缓存
    private final Map<String, SystemLogExportResponse> exportTasks = new ConcurrentHashMap<>();

    public ReactiveSystemLogServiceImpl(ReactiveSystemLogRepository systemLogRepository,
            R2dbcEntityTemplate r2dbcEntityTemplate,
            MonitoringProperties monitoringProperties) {
        this.systemLogRepository = systemLogRepository;
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
        this.monitoringProperties = monitoringProperties;
    }

    // ==================== 基础CRUD操作 ====================

    @Override
    public Mono<SystemLog> createLog(SystemLog systemLog) {
        if (systemLog == null) {
            LoggingUtil.warn(logger, "系统日志对象不能为空");
            return Mono.error(new BusinessException("系统日志对象不能为空"));
        }

        LoggingUtil.info(logger, "创建系统日志: {}", systemLog.getDescription());

        // 记录系统日志到专用监控日志
        MonitoringLogUtil.logSystemEvent("SYSTEM_LOG", "CREATE", systemLog.getDescription());

        // 使用文件日志替代数据库写入，符合监控数据文件日志规范
        return Mono.fromRunnable(() -> {
            try {
                // 使用MonitoringLogUtil记录系统日志事件到文件
                MonitoringLogUtil.logSystemEvent(
                        systemLog.getLogType() != null ? systemLog.getLogType() : "SYSTEM_LOG",
                        systemLog.getModule() != null ? systemLog.getModule() : "SYSTEM",
                        systemLog.getDescription() != null ? systemLog.getDescription() : "系统日志事件");
                LoggingUtil.info(logger, "系统日志文件记录成功: {}", systemLog.getDescription());
            } catch (Exception e) {
                LoggingUtil.error(logger, "系统日志文件记录失败", e);
                throw new BusinessException("系统日志文件记录失败: " + e.getMessage());
            }
        })
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(monitoringProperties.getDurations().getMediumTimeout())
                .retry(3)
                .then(Mono.just(systemLog))
                .doOnSuccess(saved -> LoggingUtil.info(logger, "系统日志创建成功"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "系统日志创建失败", error);
                    return Mono.error(new BusinessException("系统日志创建失败: " + error.getMessage()));
                });
    }

    @Override
    public Mono<SystemLogResponse> getLogById(Long id) {
        LoggingUtil.info(logger, "根据ID获取系统日志: {}", id);

        return systemLogRepository.findById(id)
                .timeout(monitoringProperties.getDurations().getShortTimeout())
                .retry(2)
                .map(this::convertToResponse)
                .doOnSuccess(response -> LoggingUtil.info(logger, "系统日志获取成功，ID: {}", id))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "系统日志获取失败，ID: " + id, error);
                    return Mono.error(new BusinessException("系统日志获取失败: " + error.getMessage()));
                });
    }

    @Override
    public Mono<Void> deleteLog(Long id) {
        LoggingUtil.info(logger, "删除系统日志: {}", id);

        return systemLogRepository.deleteById(id)
                .timeout(monitoringProperties.getDurations().getMediumTimeout())
                .retry(3)
                .doOnSuccess(unused -> LoggingUtil.info(logger, "系统日志删除成功，ID: {}", id))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "系统日志删除失败，ID: " + id, error);
                    return Mono.error(new BusinessException("系统日志删除失败: " + error.getMessage()));
                });
    }

    @Override
    public Mono<Void> deleteLogs(List<Long> ids) {
        LoggingUtil.info(logger, "批量删除系统日志，数量: {}", ids.size());

        return Flux.fromIterable(ids)
                .flatMap(id -> systemLogRepository.deleteById(id)
                        .timeout(monitoringProperties.getDurations().getShortTimeout())
                        .retry(2)
                        .onErrorResume(error -> {
                            LoggingUtil.error(logger, "删除日志失败，ID: " + id, error);
                            return Mono.empty(); // 继续处理其他删除操作
                        }))
                .then()
                .doOnSuccess(unused -> LoggingUtil.info(logger, "批量删除系统日志成功，数量: {}", ids.size()))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "批量删除系统日志失败", error);
                    return Mono.error(new BusinessException("批量删除系统日志失败: " + error.getMessage()));
                });
    }

    // ==================== 日志查询功能 ====================

    @Override
    public Mono<Page<SystemLogResponse>> queryLogs(SystemLogQueryRequest queryRequest) {
        LoggingUtil.info(logger, "分页查询系统日志: {}", queryRequest);

        // 构建查询条件
        Query query = buildQuery(queryRequest);

        // 获取总数和数据
        Mono<Long> totalMono = r2dbcEntityTemplate.count(query, SystemLog.class);

        // 添加分页和排序
        Pageable pageable = PageRequest.of(queryRequest.getPage() - 1, queryRequest.getSize());
        query = query.with(pageable);

        Flux<SystemLogResponse> dataFlux = r2dbcEntityTemplate.select(query, SystemLog.class)
                .timeout(Duration.ofSeconds(15))
                .retry(2)
                .map(this::convertToResponse)
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "查询系统日志数据失败", error);
                    return Flux.empty();
                });

        return Mono.zip(totalMono, dataFlux.collectList())
                .map(tuple -> (Page<SystemLogResponse>) new PageImpl<>(tuple.getT2(), pageable, tuple.getT1()))
                .doOnSuccess(page -> LoggingUtil.info(logger, "系统日志查询成功，总数: {}, 当前页: {}",
                        page.getTotalElements(), page.getNumber() + 1))
                .doOnError(error -> LoggingUtil.error(logger, "系统日志查询失败", error));
    }

    @Override
    public Flux<SystemLogResponse> findLogs(SystemLogQueryRequest queryRequest) {
        LoggingUtil.info(logger, "查询系统日志列表: {}", queryRequest);

        Query query = buildQuery(queryRequest);

        return r2dbcEntityTemplate.select(query, SystemLog.class)
                .timeout(Duration.ofSeconds(10))
                .retry(2)
                .map(this::convertToResponse)
                .doOnComplete(() -> LoggingUtil.info(logger, "系统日志列表查询完成"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "系统日志列表查询失败", error);
                    return Flux.empty();
                });
    }

    @Override
    public Flux<SystemLogResponse> findLogsByType(String logType, int page, int size) {
        LoggingUtil.info(logger, "根据类型查询日志: {}, 页码: {}, 大小: {}", logType, page, size);

        return systemLogRepository.findByLogType(logType)
                .skip((long) (page - 1) * size)
                .take(size)
                .map(this::convertToResponse)
                .doOnComplete(() -> LoggingUtil.info(logger, "按类型查询日志完成"))
                .doOnError(error -> LoggingUtil.error(logger, "按类型查询日志失败", error));
    }

    @Override
    public Flux<SystemLogResponse> findLogsByLevel(String logLevel, int page, int size) {
        LoggingUtil.info(logger, "根据级别查询日志: {}, 页码: {}, 大小: {}", logLevel, page, size);

        return systemLogRepository.findByLogLevel(logLevel)
                .skip((long) (page - 1) * size)
                .take(size)
                .map(this::convertToResponse)
                .doOnComplete(() -> LoggingUtil.info(logger, "按级别查询日志完成"))
                .doOnError(error -> LoggingUtil.error(logger, "按级别查询日志失败", error));
    }

    @Override
    public Flux<SystemLogResponse> findUserOperationLogs(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        LoggingUtil.info(logger, "查询用户操作日志: userId={}, startTime={}, endTime={}", userId, startTime, endTime);

        return systemLogRepository.findUserOperationLogs(userId, startTime, endTime)
                .map(this::convertToResponse)
                .doOnComplete(() -> LoggingUtil.info(logger, "用户操作日志查询完成"))
                .doOnError(error -> LoggingUtil.error(logger, "用户操作日志查询失败", error));
    }

    @Override
    public Flux<SystemLogResponse> findErrorLogs(int page, int size) {
        LoggingUtil.info(logger, "查询错误日志: 页码={}, 大小={}", page, size);

        return systemLogRepository.findErrorLogs()
                .skip((long) (page - 1) * size)
                .take(size)
                .map(this::convertToResponse)
                .doOnComplete(() -> LoggingUtil.info(logger, "错误日志查询完成"))
                .doOnError(error -> LoggingUtil.error(logger, "错误日志查询失败", error));
    }

    @Override
    public Flux<SystemLogResponse> findSecurityLogs(int page, int size) {
        LoggingUtil.info(logger, "查询安全日志: 页码={}, 大小={}", page, size);

        return systemLogRepository.findSecurityLogs()
                .skip((long) (page - 1) * size)
                .take(size)
                .map(this::convertToResponse)
                .doOnComplete(() -> LoggingUtil.info(logger, "安全日志查询完成"))
                .doOnError(error -> LoggingUtil.error(logger, "安全日志查询失败", error));
    }

    @Override
    public Flux<SystemLogResponse> searchLogs(String keyword, int page, int size) {
        LoggingUtil.info(logger, "关键词搜索日志: keyword={}, 页码={}, 大小={}", keyword, page, size);

        // 如果关键字为空或null，返回空结果
        if (keyword == null || keyword.trim().isEmpty()) {
            LoggingUtil.info(logger, "关键词为空，返回空结果");
            return Flux.empty();
        }

        Criteria criteria = Criteria.where("description").like("%" + keyword + "%")
                .or("request_params").like("%" + keyword + "%")
                .or("response_result").like("%" + keyword + "%");

        Query query = Query.query(criteria);

        return r2dbcEntityTemplate.select(query, SystemLog.class)
                .skip((long) (page - 1) * size)
                .take(size)
                .map(this::convertToResponse)
                .doOnComplete(() -> LoggingUtil.info(logger, "关键词搜索日志完成"))
                .doOnError(error -> LoggingUtil.error(logger, "关键词搜索日志失败", error));
    }

    // ==================== 日志统计功能 ====================

    @Override
    public Mono<Long> countTotalLogs() {
        LoggingUtil.info(logger, "统计日志总数");

        return systemLogRepository.count()
                .doOnSuccess(count -> LoggingUtil.info(logger, "日志总数统计完成: {}", count))
                .doOnError(error -> LoggingUtil.error(logger, "日志总数统计失败", error));
    }

    @Override
    public Mono<Long> countLogsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        LoggingUtil.info(logger, "统计时间范围内日志数量: {} - {}", startTime, endTime);

        return systemLogRepository.countByDateRange(startTime, endTime)
                .doOnSuccess(count -> LoggingUtil.info(logger, "时间范围日志统计完成: {}", count))
                .doOnError(error -> LoggingUtil.error(logger, "时间范围日志统计失败", error));
    }

    @Override
    public Mono<Map<String, Long>> countLogsByLevel() {
        LoggingUtil.info(logger, "统计各日志级别数量");

        // 使用自定义查询统计日志级别
        String sql = "SELECT log_level, COUNT(*) as count FROM sys_system_logs WHERE deleted = 0 GROUP BY log_level ORDER BY count DESC";

        return r2dbcEntityTemplate.getDatabaseClient()
                .sql(sql)
                .fetch()
                .all()
                .collectMap(
                        row -> (String) row.get("log_level"),
                        row -> ((Number) row.get("count")).longValue())
                .doOnSuccess(stats -> LoggingUtil.info(logger, "日志级别统计完成: {}", stats))
                .doOnError(error -> LoggingUtil.error(logger, "日志级别统计失败", error));
    }

    @Override
    public Mono<Map<String, Long>> countLogsByType() {
        LoggingUtil.info(logger, "统计各日志类型数量");

        // 使用自定义查询统计日志类型
        String sql = "SELECT log_type, COUNT(*) as count FROM sys_system_logs WHERE deleted = 0 GROUP BY log_type ORDER BY count DESC";

        return r2dbcEntityTemplate.getDatabaseClient()
                .sql(sql)
                .fetch()
                .all()
                .collectMap(
                        row -> (String) row.get("log_type"),
                        row -> ((Number) row.get("count")).longValue())
                .doOnSuccess(stats -> LoggingUtil.info(logger, "日志类型统计完成: {}", stats))
                .doOnError(error -> LoggingUtil.error(logger, "日志类型统计失败", error));
    }

    @Override
    public Mono<Map<String, Long>> countLogsByOperationType() {
        LoggingUtil.info(logger, "统计各操作类型数量");

        String sql = "SELECT operation_type, COUNT(*) as count FROM sys_system_logs WHERE deleted = 0 GROUP BY operation_type ORDER BY count DESC";

        return r2dbcEntityTemplate.getDatabaseClient()
                .sql(sql)
                .fetch()
                .all()
                .collectMap(
                        row -> (String) row.get("operation_type"),
                        row -> ((Number) row.get("count")).longValue())
                .doOnSuccess(stats -> LoggingUtil.info(logger, "操作类型统计完成: {}", stats))
                .doOnError(error -> LoggingUtil.error(logger, "操作类型统计失败", error));
    }

    @Override
    public Mono<Map<String, Long>> countUserOperations(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        LoggingUtil.info(logger, "统计用户操作频次: {} - {}, limit={}", startTime, endTime, limit);

        String sql = "SELECT username, COUNT(*) as count FROM sys_system_logs " +
                "WHERE created_at >= ? AND created_at <= ? AND deleted = 0 " +
                "GROUP BY username ORDER BY count DESC LIMIT ?";

        return r2dbcEntityTemplate.getDatabaseClient()
                .sql(sql)
                .bind(0, startTime)
                .bind(1, endTime)
                .bind(2, limit)
                .fetch()
                .all()
                .collectMap(
                        row -> (String) row.get("username"),
                        row -> ((Number) row.get("count")).longValue())
                .doOnSuccess(stats -> LoggingUtil.info(logger, "用户操作统计完成: {}", stats))
                .doOnError(error -> LoggingUtil.error(logger, "用户操作统计失败", error));
    }

    @Override
    public Mono<Map<String, Object>> getTodayLogSummary() {
        LoggingUtil.info(logger, "获取今日日志概况");

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        Mono<Long> totalCount = countLogsByDateRange(startOfDay, endOfDay);
        Mono<Map<String, Long>> levelStats = countLogsByLevel();
        Mono<Map<String, Long>> typeStats = countLogsByType();

        return Mono.zip(totalCount, levelStats, typeStats)
                .map(tuple -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("totalCount", tuple.getT1());
                    summary.put("levelStats", tuple.getT2());
                    summary.put("typeStats", tuple.getT3());
                    summary.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    return summary;
                })
                .doOnSuccess(summary -> LoggingUtil.info(logger, "今日日志概况获取完成"))
                .doOnError(error -> LoggingUtil.error(logger, "今日日志概况获取失败", error));
    }

    // ==================== 日志导出功能 ====================

    @Override
    public Mono<SystemLogExportResponse> exportLogsToExcel(SystemLogQueryRequest queryRequest, Long userId,
            String username) {
        String taskId = generateTaskId("excel");
        LoggingUtil.info(logger, "开始导出Excel日志，任务ID: {}, 用户: {}", taskId, username);

        SystemLogExportResponse response = new SystemLogExportResponse(taskId, "EXCEL", userId, username);
        exportTasks.put(taskId, response);

        // 异步执行导出任务
        performExportAsync(taskId, queryRequest, "EXCEL")
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.just(response);
    }

    @Override
    public Mono<SystemLogExportResponse> exportLogsToCSV(SystemLogQueryRequest queryRequest, Long userId,
            String username) {
        String taskId = generateTaskId("csv");
        LoggingUtil.info(logger, "开始导出CSV日志，任务ID: {}, 用户: {}", taskId, username);

        SystemLogExportResponse response = new SystemLogExportResponse(taskId, "CSV", userId, username);
        exportTasks.put(taskId, response);

        // 异步执行导出任务
        performExportAsync(taskId, queryRequest, "CSV")
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.just(response);
    }

    @Override
    public Mono<SystemLogExportResponse> exportLogsToJSON(SystemLogQueryRequest queryRequest, Long userId,
            String username) {
        String taskId = generateTaskId("json");
        LoggingUtil.info(logger, "开始导出JSON日志，任务ID: {}, 用户: {}", taskId, username);

        SystemLogExportResponse response = new SystemLogExportResponse(taskId, "JSON", userId, username);
        exportTasks.put(taskId, response);

        // 异步执行导出任务
        performExportAsync(taskId, queryRequest, "JSON")
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.just(response);
    }

    @Override
    public Mono<SystemLogExportResponse> getExportTaskStatus(String taskId) {
        LoggingUtil.info(logger, "获取导出任务状态: {}", taskId);

        SystemLogExportResponse response = exportTasks.get(taskId);
        if (response == null) {
            return Mono.empty();
        }

        return Mono.just(response);
    }

    @Override
    public Mono<byte[]> downloadExportFile(String taskId) {
        LoggingUtil.info(logger, "下载导出文件: {}", taskId);

        // 这里应该从文件系统读取文件
        // 为了演示，返回空字节数组
        return Mono.just(new byte[0])
                .doOnSuccess(bytes -> LoggingUtil.info(logger, "导出文件下载完成，大小: {} bytes", bytes.length))
                .doOnError(error -> LoggingUtil.error(logger, "导出文件下载失败", error));
    }

    @Override
    public Mono<Void> cleanupExpiredExportFiles() {
        LoggingUtil.info(logger, "清理过期导出文件");

        return Mono.fromRunnable(() -> {
            List<String> expiredTasks = exportTasks.entrySet().stream()
                    .filter(entry -> entry.getValue().isExpired())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            expiredTasks.forEach(exportTasks::remove);
            LoggingUtil.info(logger, "清理过期导出文件完成，数量: {}", expiredTasks.size());
        });
    }

    // ==================== 日志维护功能 ====================

    @Override
    public Mono<Long> cleanupSystemLogs(LocalDateTime beforeTime) {
        LoggingUtil.info(logger, "清理系统日志，截止时间: {}", beforeTime);

        if (beforeTime == null) {
            return Mono.error(new BusinessException("清理截止时间不能为空"));
        }

        return systemLogRepository.deleteLogsBefore(beforeTime)
                .map(Long::valueOf)
                .doOnSuccess(count -> LoggingUtil.info(logger, "系统日志清理完成，删除数量: {}", count))
                .doOnError(error -> LoggingUtil.error(logger, "系统日志清理失败", error));
    }

    @Override
    public Mono<Integer> archiveLogsBefore(LocalDateTime beforeTime) {
        LoggingUtil.info(logger, "归档指定时间之前的日志: {}", beforeTime);

        // 这里应该实现日志归档逻辑
        // 为了演示，返回0
        return Mono.just(0)
                .doOnSuccess(count -> LoggingUtil.info(logger, "日志归档完成，归档数量: {}", count))
                .doOnError(error -> LoggingUtil.error(logger, "日志归档失败", error));
    }

    @Override
    public Mono<Void> compressHistoryLogs(LocalDateTime beforeTime) {
        LoggingUtil.info(logger, "压缩历史日志: {}", beforeTime);

        // 这里应该实现日志压缩逻辑
        return Mono.<Void>empty()
                .doOnSuccess(unused -> LoggingUtil.info(logger, "历史日志压缩完成"))
                .doOnError(error -> LoggingUtil.error(logger, "历史日志压缩失败", error));
    }

    @Override
    public Mono<Map<String, Object>> getLogStorageStats() {
        LoggingUtil.info(logger, "获取日志存储统计信息");

        return countTotalLogs()
                .map(totalCount -> {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("totalLogs", totalCount);
                    stats.put("estimatedSize", totalCount * 1024); // 估算大小
                    stats.put("lastUpdated", LocalDateTime.now());
                    return stats;
                })
                .doOnSuccess(stats -> LoggingUtil.info(logger, "日志存储统计获取完成"))
                .doOnError(error -> LoggingUtil.error(logger, "日志存储统计获取失败", error));
    }

    // ==================== 日志分析功能 ====================

    @Override
    public Flux<SystemLogResponse> analyzeSlowLogs(long threshold, LocalDateTime startTime, LocalDateTime endTime) {
        LoggingUtil.info(logger, "分析慢查询日志: threshold={}, startTime={}, endTime={}", threshold, startTime, endTime);

        Criteria criteria = Criteria.where("execution_time").greaterThan(threshold)
                .and("created_at").greaterThanOrEquals(startTime)
                .and("created_at").lessThanOrEquals(endTime);

        Query query = Query.query(criteria);

        return r2dbcEntityTemplate.select(query, SystemLog.class)
                .map(this::convertToResponse)
                .doOnComplete(() -> LoggingUtil.info(logger, "慢查询日志分析完成"))
                .doOnError(error -> LoggingUtil.error(logger, "慢查询日志分析失败", error));
    }

    @Override
    public Mono<Map<String, Object>> analyzeErrorTrends(LocalDateTime startTime, LocalDateTime endTime) {
        LoggingUtil.info(logger, "分析异常日志趋势: {} - {}", startTime, endTime);

        // 这里应该实现异常趋势分析逻辑
        return Mono.just((Map<String, Object>) new HashMap<String, Object>())
                .doOnSuccess(trends -> LoggingUtil.info(logger, "异常日志趋势分析完成"))
                .doOnError(error -> LoggingUtil.error(logger, "异常日志趋势分析失败", error));
    }

    @Override
    public Mono<Map<String, Object>> analyzeUserActivity(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        LoggingUtil.info(logger, "分析用户活动模式: userId={}, {} - {}", userId, startTime, endTime);

        return findUserOperationLogs(userId, startTime, endTime)
                .collectList()
                .map(logs -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("totalOperations", logs.size());
                    activity.put("operationTypes", logs.stream()
                            .collect(Collectors.groupingBy(
                                    SystemLogResponse::getOperationType,
                                    Collectors.counting())));
                    return activity;
                })
                .doOnSuccess(activity -> LoggingUtil.info(logger, "用户活动分析完成"))
                .doOnError(error -> LoggingUtil.error(logger, "用户活动分析失败", error));
    }

    @Override
    public Mono<Map<String, Object>> generateLogReport(LocalDateTime startTime, LocalDateTime endTime) {
        LoggingUtil.info(logger, "生成日志报告: {} - {}", startTime, endTime);

        Mono<Long> totalCount = countLogsByDateRange(startTime, endTime);
        Mono<Map<String, Long>> levelStats = countLogsByLevel();
        Mono<Map<String, Long>> typeStats = countLogsByType();

        return Mono.zip(totalCount, levelStats, typeStats)
                .map(tuple -> {
                    Map<String, Object> report = new HashMap<>();
                    report.put("period", startTime + " - " + endTime);
                    report.put("totalLogs", tuple.getT1());
                    report.put("levelDistribution", tuple.getT2());
                    report.put("typeDistribution", tuple.getT3());
                    report.put("generatedAt", LocalDateTime.now());
                    return report;
                })
                .doOnSuccess(report -> LoggingUtil.info(logger, "日志报告生成完成"))
                .doOnError(error -> LoggingUtil.error(logger, "日志报告生成失败", error));
    }

    // ==================== 实时日志功能 ====================

    @Override
    public Flux<SystemLogResponse> getRealtimeLogs(List<String> logTypes, List<String> logLevels) {
        LoggingUtil.info(logger, "获取实时日志流: types={}, levels={}", logTypes, logLevels);

        // 这里应该实现实时日志流
        // 为了演示，返回空流
        return Flux.empty();
    }

    @Override
    public Flux<SystemLogResponse> getRealtimeErrorLogs() {
        LoggingUtil.info(logger, "获取实时错误日志流");

        return getRealtimeLogs(null, Arrays.asList("ERROR", "FATAL"));
    }

    @Override
    public Flux<SystemLogResponse> getRealtimeSecurityLogs() {
        LoggingUtil.info(logger, "获取实时安全日志流");

        return getRealtimeLogs(Arrays.asList("SECURITY"), null);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建查询条件
     *
     * @param queryRequest 查询请求
     * @return 查询对象
     */
    private Query buildQuery(SystemLogQueryRequest queryRequest) {
        Criteria criteria = Criteria.where("deleted").is(0);

        // 日志类型
        if (queryRequest.getLogTypes() != null && !queryRequest.getLogTypes().isEmpty()) {
            criteria = criteria.and("log_type").in(queryRequest.getLogTypes());
        }

        // 日志级别
        if (queryRequest.getLogLevels() != null && !queryRequest.getLogLevels().isEmpty()) {
            criteria = criteria.and("log_level").in(queryRequest.getLogLevels());
        }

        // 操作类型
        if (queryRequest.getOperationTypes() != null && !queryRequest.getOperationTypes().isEmpty()) {
            criteria = criteria.and("operation_type").in(queryRequest.getOperationTypes());
        }

        // 模块
        if (queryRequest.getModule() != null && !queryRequest.getModule().isEmpty()) {
            criteria = criteria.and("module").like("%" + queryRequest.getModule() + "%");
        }

        // 用户ID
        if (queryRequest.getUserId() != null) {
            criteria = criteria.and("user_id").is(queryRequest.getUserId());
        }

        // 用户名
        if (queryRequest.getUsername() != null && !queryRequest.getUsername().isEmpty()) {
            criteria = criteria.and("username").like("%" + queryRequest.getUsername() + "%");
        }

        // 用户类型
        if (queryRequest.getUserType() != null && !queryRequest.getUserType().isEmpty()) {
            criteria = criteria.and("user_type").is(queryRequest.getUserType());
        }

        // 客户端IP
        if (queryRequest.getClientIp() != null && !queryRequest.getClientIp().isEmpty()) {
            criteria = criteria.and("client_ip").like("%" + queryRequest.getClientIp() + "%");
        }

        // 操作状态
        if (queryRequest.getStatus() != null) {
            criteria = criteria.and("status").is(queryRequest.getStatus());
        }

        // 时间范围
        if (queryRequest.getStartTime() != null) {
            criteria = criteria.and("created_at").greaterThanOrEquals(queryRequest.getStartTime());
        }
        if (queryRequest.getEndTime() != null) {
            criteria = criteria.and("created_at").lessThanOrEquals(queryRequest.getEndTime());
        }

        // 执行时间范围
        if (queryRequest.getMinExecutionTime() != null) {
            criteria = criteria.and("execution_time").greaterThanOrEquals(queryRequest.getMinExecutionTime());
        }
        if (queryRequest.getMaxExecutionTime() != null) {
            criteria = criteria.and("execution_time").lessThanOrEquals(queryRequest.getMaxExecutionTime());
        }

        // 关键词搜索
        if (queryRequest.getKeyword() != null && !queryRequest.getKeyword().isEmpty()) {
            String keyword = "%" + queryRequest.getKeyword() + "%";
            criteria = criteria.and(
                    Criteria.where("description").like(keyword)
                            .or("request_params").like(keyword)
                            .or("response_result").like(keyword));
        }

        return Query.query(criteria);
    }

    /**
     * 转换为响应DTO
     *
     * @param systemLog 系统日志实体
     * @return 系统日志响应DTO
     */
    private SystemLogResponse convertToResponse(SystemLog systemLog) {
        SystemLogResponse response = new SystemLogResponse();
        BeanUtils.copyProperties(systemLog, response);
        return response;
    }

    /**
     * 生成任务ID
     *
     * @param format 导出格式
     * @return 任务ID
     */
    private String generateTaskId(String format) {
        return "export_" + format + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    /**
     * 异步执行导出任务
     *
     * @param taskId       任务ID
     * @param queryRequest 查询请求
     * @param format       导出格式
     * @return 导出结果
     */
    private Mono<Void> performExportAsync(String taskId, SystemLogQueryRequest queryRequest, String format) {
        return Mono.fromRunnable(() -> {
            try {
                SystemLogExportResponse response = exportTasks.get(taskId);
                if (response != null) {
                    response.markProcessing(Integer.valueOf(10));
                }
            } catch (Exception e) {
                SystemLogExportResponse response = exportTasks.get(taskId);
                if (response != null) {
                    response.markFailed(e.getMessage());
                }
                LoggingUtil.error(logger, "导出任务初始化失败: " + taskId, e);
            }
        })
                .then(performActualExport(taskId, queryRequest, format))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    /**
     * 执行实际的日志导出操作
     *
     * @param taskId       任务ID
     * @param queryRequest 查询请求
     * @param format       导出格式
     * @return 导出结果
     */
    private Mono<Void> performActualExport(String taskId, SystemLogQueryRequest queryRequest, String format) {
        return findLogs(queryRequest)
                .collectList()
                .flatMap(logs -> {
                    SystemLogExportResponse response = exportTasks.get(taskId);
                    if (response == null) {
                        return Mono.error(new BusinessException("导出任务不存在: " + taskId));
                    }

                    try {
                        // 更新进度到50%
                        response.markProcessing(Integer.valueOf(50));

                        // 生成导出文件
                        String fileName = generateExportFileName(format);
                        long fileSize = calculateExportFileSize(logs, format);
                        String downloadUrl = PathConstants.SYSTEM_LOGS + "/export/download/" + taskId;

                        // 更新进度到90%
                        response.markProcessing(Integer.valueOf(90));

                        // 模拟文件生成时间
                        return Mono.delay(Duration.ofMillis(500))
                                .then(Mono.fromRunnable(() -> {
                                    // 标记完成
                                    response.markCompleted(fileName, fileSize, (long) logs.size(), downloadUrl);
                                    LoggingUtil.info(logger, "导出任务完成: {}, 文件: {}, 记录数: {}",
                                            taskId, fileName, logs.size());
                                }));

                    } catch (Exception e) {
                        response.markFailed(e.getMessage());
                        LoggingUtil.error(logger, "导出任务处理失败: " + taskId, e);
                        return Mono.error(e);
                    }
                })
                .onErrorResume(error -> {
                    SystemLogExportResponse response = exportTasks.get(taskId);
                    if (response != null) {
                        response.markFailed(error.getMessage());
                    }
                    LoggingUtil.error(logger, "导出任务失败: " + taskId, error);
                    return Mono.empty();
                })
                .then();
    }

    /**
     * 生成导出文件名
     *
     * @param format 导出格式
     * @return 文件名
     */
    private String generateExportFileName(String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "sys_system_logs_" + timestamp + "." + format.toLowerCase();
    }

    /**
     * 计算导出文件大小
     *
     * @param logs   日志列表
     * @param format 导出格式
     * @return 文件大小（字节）
     */
    private long calculateExportFileSize(List<SystemLogResponse> logs, String format) {
        if (logs.isEmpty()) {
            return 1024L; // 最小文件大小
        }

        // 根据格式和记录数估算文件大小
        long baseSize = switch (format.toUpperCase()) {
            case "EXCEL" -> logs.size() * 200L; // 每条记录约200字节
            case "CSV" -> logs.size() * 150L; // 每条记录约150字节
            case "JSON" -> logs.size() * 300L; // 每条记录约300字节
            default -> logs.size() * 200L;
        };

        return Math.max(baseSize, 1024L); // 最小1KB
    }
}
