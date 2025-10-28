package com.honyrun.service.reactive;

import com.honyrun.model.dto.request.SystemLogQueryRequest;
import com.honyrun.model.dto.response.SystemLogExportResponse;
import com.honyrun.model.dto.response.SystemLogResponse;
import com.honyrun.model.entity.system.SystemLog;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 响应式系统日志服务接口
 *
 * 基于WebFlux的响应式系统日志服务，提供非阻塞的日志管理功能
 * 支持日志查询、过滤、导出、统计等运维功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 10:45:00
 * @modified 2025-07-01 10:45:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveSystemLogService {

    // ==================== 基础CRUD操作 ====================

    /**
     * 创建系统日志
     *
     * @param systemLog 系统日志实体
     * @return 创建的系统日志的Mono包装
     */
    Mono<SystemLog> createLog(SystemLog systemLog);

    /**
     * 根据ID获取系统日志
     *
     * @param id 日志ID
     * @return 系统日志的Mono包装
     */
    Mono<SystemLogResponse> getLogById(Long id);

    /**
     * 删除系统日志
     *
     * @param id 日志ID
     * @return 删除结果的Mono包装
     */
    Mono<Void> deleteLog(Long id);

    /**
     * 批量删除系统日志
     *
     * @param ids 日志ID列表
     * @return 删除结果的Mono包装
     */
    Mono<Void> deleteLogs(List<Long> ids);

    // ==================== 日志查询功能 ====================

    /**
     * 分页查询系统日志
     *
     * @param queryRequest 查询请求参数
     * @return 分页日志结果的Mono包装
     */
    Mono<Page<SystemLogResponse>> queryLogs(SystemLogQueryRequest queryRequest);

    /**
     * 根据条件查询日志列表
     *
     * @param queryRequest 查询请求参数
     * @return 日志列表的Flux包装
     */
    Flux<SystemLogResponse> findLogs(SystemLogQueryRequest queryRequest);

    /**
     * 根据日志类型查询日志
     *
     * @param logType 日志类型
     * @param page 页码
     * @param size 每页大小
     * @return 日志列表的Flux包装
     */
    Flux<SystemLogResponse> findLogsByType(String logType, int page, int size);

    /**
     * 根据日志级别查询日志
     *
     * @param logLevel 日志级别
     * @param page 页码
     * @param size 每页大小
     * @return 日志列表的Flux包装
     */
    Flux<SystemLogResponse> findLogsByLevel(String logLevel, int page, int size);

    /**
     * 根据用户查询操作日志
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作日志列表的Flux包装
     */
    Flux<SystemLogResponse> findUserOperationLogs(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询错误日志
     *
     * @param page 页码
     * @param size 每页大小
     * @return 错误日志列表的Flux包装
     */
    Flux<SystemLogResponse> findErrorLogs(int page, int size);

    /**
     * 查询安全日志
     *
     * @param page 页码
     * @param size 每页大小
     * @return 安全日志列表的Flux包装
     */
    Flux<SystemLogResponse> findSecurityLogs(int page, int size);

    /**
     * 关键词搜索日志
     *
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果的Flux包装
     */
    Flux<SystemLogResponse> searchLogs(String keyword, int page, int size);

    // ==================== 日志统计功能 ====================

    /**
     * 统计日志总数
     *
     * @return 日志总数的Mono包装
     */
    Mono<Long> countTotalLogs();

    /**
     * 统计指定时间范围内的日志数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志数量的Mono包装
     */
    Mono<Long> countLogsByDateRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计各日志级别的数量
     *
     * @return 日志级别统计的Mono包装
     */
    Mono<Map<String, Long>> countLogsByLevel();

    /**
     * 统计各日志类型的数量
     *
     * @return 日志类型统计的Mono包装
     */
    Mono<Map<String, Long>> countLogsByType();

    /**
     * 统计各操作类型的数量
     *
     * @return 操作类型统计的Mono包装
     */
    Mono<Map<String, Long>> countLogsByOperationType();

    /**
     * 统计用户操作频次
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 返回数量限制
     * @return 用户操作统计的Mono包装
     */
    Mono<Map<String, Long>> countUserOperations(LocalDateTime startTime, LocalDateTime endTime, int limit);

    /**
     * 统计今日日志概况
     *
     * @return 今日日志统计的Mono包装
     */
    Mono<Map<String, Object>> getTodayLogSummary();

    // ==================== 日志导出功能 ====================

    /**
     * 导出日志为Excel格式
     *
     * @param queryRequest 查询条件
     * @param userId 导出用户ID
     * @param username 导出用户名
     * @return 导出任务响应的Mono包装
     */
    Mono<SystemLogExportResponse> exportLogsToExcel(SystemLogQueryRequest queryRequest, Long userId, String username);

    /**
     * 导出日志为CSV格式
     *
     * @param queryRequest 查询条件
     * @param userId 导出用户ID
     * @param username 导出用户名
     * @return 导出任务响应的Mono包装
     */
    Mono<SystemLogExportResponse> exportLogsToCSV(SystemLogQueryRequest queryRequest, Long userId, String username);

    /**
     * 导出日志为JSON格式
     *
     * @param queryRequest 查询条件
     * @param userId 导出用户ID
     * @param username 导出用户名
     * @return 导出任务响应的Mono包装
     */
    Mono<SystemLogExportResponse> exportLogsToJSON(SystemLogQueryRequest queryRequest, Long userId, String username);

    /**
     * 获取导出任务状态
     *
     * @param taskId 任务ID
     * @return 导出任务状态的Mono包装
     */
    Mono<SystemLogExportResponse> getExportTaskStatus(String taskId);

    /**
     * 下载导出文件
     *
     * @param taskId 任务ID
     * @return 文件字节流的Mono包装
     */
    Mono<byte[]> downloadExportFile(String taskId);

    /**
     * 清理过期的导出文件
     *
     * @return 清理结果的Mono包装
     */
    Mono<Void> cleanupExpiredExportFiles();

    // ==================== 日志维护功能 ====================

    /**
     * 清理系统日志
     * 专门用于清理系统运行日志（如应用日志、错误日志、调试日志等）
     *
     * @param beforeTime 清理此时间之前的系统日志
     * @return 清理数量的响应式单值
     * @author Mr.Rey Copyright © 2025
     * @created 2025-09-29 15:20:00
     * @modified 2025-09-29 15:20:00
     * @version 1.0.0
     */
    Mono<Long> cleanupSystemLogs(LocalDateTime beforeTime);

    /**
     * 归档指定时间之前的日志
     *
     * @param beforeTime 时间阈值
     * @return 归档的日志数量的Mono包装
     */
    Mono<Integer> archiveLogsBefore(LocalDateTime beforeTime);

    /**
     * 压缩历史日志
     *
     * @param beforeTime 时间阈值
     * @return 压缩结果的Mono包装
     */
    Mono<Void> compressHistoryLogs(LocalDateTime beforeTime);

    /**
     * 获取日志存储统计信息
     *
     * @return 存储统计信息的Mono包装
     */
    Mono<Map<String, Object>> getLogStorageStats();

    // ==================== 日志分析功能 ====================

    /**
     * 分析慢查询日志
     *
     * @param threshold 执行时间阈值（毫秒）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 慢查询日志的Flux包装
     */
    Flux<SystemLogResponse> analyzeSlowLogs(long threshold, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分析异常日志趋势
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 异常趋势统计的Mono包装
     */
    Mono<Map<String, Object>> analyzeErrorTrends(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分析用户活动模式
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户活动分析的Mono包装
     */
    Mono<Map<String, Object>> analyzeUserActivity(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 生成日志报告
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志报告的Mono包装
     */
    Mono<Map<String, Object>> generateLogReport(LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 实时日志功能 ====================

    /**
     * 获取实时日志流
     *
     * @param logTypes 日志类型过滤
     * @param logLevels 日志级别过滤
     * @return 实时日志流的Flux包装
     */
    Flux<SystemLogResponse> getRealtimeLogs(List<String> logTypes, List<String> logLevels);

    /**
     * 获取实时错误日志流
     *
     * @return 实时错误日志流的Flux包装
     */
    Flux<SystemLogResponse> getRealtimeErrorLogs();

    /**
     * 获取实时安全日志流
     *
     * @return 实时安全日志流的Flux包装
     */
    Flux<SystemLogResponse> getRealtimeSecurityLogs();
}

