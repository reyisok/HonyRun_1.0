package com.honyrun.service.reactive;

import com.honyrun.model.entity.system.SystemSetting;
import com.honyrun.model.entity.system.SystemLog;
import com.honyrun.model.entity.system.SystemConfig;
import com.honyrun.model.entity.system.PerformanceMetrics;
import com.honyrun.model.dto.request.SystemSettingRequest;
import com.honyrun.model.dto.response.SystemSettingResponse;
import com.honyrun.model.dto.response.SystemStatusResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 响应式系统服务接口
 *
 * 提供系统设置、日志管理、监控功能的响应式服务接口
 * 支持系统配置管理、日志查询、性能监控等核心系统管理功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  22:00:00
 * @modified 2025-07-01 22:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveSystemService {

    // ==================== 系统设置管理 ====================

    /**
     * 获取所有系统设置
     *
     * @return 系统设置列表的响应式流
     */
    Flux<SystemSetting> getAllSettings();

    /**
     * 根据分类获取系统设置
     *
     * @param category 设置分类
     * @return 系统设置列表的响应式流
     */
    Flux<SystemSetting> getSettingsByCategory(String category);

    /**
     * 根据键名获取系统设置
     *
     * @param settingKey 设置键名
     * @return 系统设置的响应式单值
     */
    Mono<SystemSetting> getSettingByKey(String settingKey);

    /**
     * 创建系统设置
     *
     * @param request 系统设置请求对象
     * @return 创建结果的响应式单值
     */
    Mono<SystemSettingResponse> createSetting(SystemSettingRequest request);

    /**
     * 更新系统设置
     *
     * @param settingKey 设置键名
     * @param request 系统设置请求对象
     * @return 更新结果的响应式单值
     */
    Mono<SystemSettingResponse> updateSetting(String settingKey, SystemSettingRequest request);

    /**
     * 删除系统设置
     *
     * @param settingKey 设置键名
     * @return 删除操作的响应式单值
     */
    Mono<Void> deleteSetting(String settingKey);

    /**
     * 批量更新系统设置
     *
     * @param settings 设置键值对映射
     * @return 批量更新结果的响应式流
     */
    Flux<SystemSettingResponse> batchUpdateSettings(Map<String, String> settings);

    /**
     * 获取设置分类列表
     *
     * @return 设置分类的响应式流
     */
    Flux<String> getSettingCategories();

    /**
     * 导出系统设置
     *
     * @param category 设置分类（可选）
     * @return 导出结果的响应式单值
     */
    Mono<Map<String, Object>> exportSettings(String category);

    /**
     * 导入系统设置
     *
     * @param settings 设置数据
     * @return 导入结果的响应式单值
     */
    Mono<Map<String, Object>> importSettings(Map<String, Object> settings);

    // ==================== 系统日志管理 ====================

    /**
     * 获取系统日志列表
     *
     * @param logType 日志类型（可选）
     * @param logLevel 日志级别（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码
     * @param size 页大小
     * @return 系统日志列表的响应式流
     */
    Flux<SystemLog> getSystemLogs(String logType, String logLevel,
                                  LocalDateTime startTime, LocalDateTime endTime,
                                  int page, int size);

    /**
     * 根据用户获取操作日志
     *
     * @param username 用户名
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码
     * @param size 页大小
     * @return 操作日志列表的响应式流
     */
    Flux<SystemLog> getOperationLogsByUser(String username, LocalDateTime startTime,
                                           LocalDateTime endTime, int page, int size);

    /**
     * 根据模块获取日志
     *
     * @param module 模块名称
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码
     * @param size 页大小
     * @return 模块日志列表的响应式流
     */
    Flux<SystemLog> getLogsByModule(String module, LocalDateTime startTime,
                                    LocalDateTime endTime, int page, int size);

    /**
     * 创建系统日志
     *
     * @param systemLog 系统日志对象
     * @return 创建结果的响应式单值
     */
    Mono<SystemLog> createSystemLog(SystemLog systemLog);

    /**
     * 批量创建系统日志
     *
     * @param logs 日志列表
     * @return 批量创建结果的响应式流
     */
    Flux<SystemLog> batchCreateSystemLogs(Flux<SystemLog> logs);

    /**
     * 归档过期日志（替代删除）
     *
     * @param beforeDate 归档此日期之前的日志
     * @param compressEnabled 是否启用压缩
     * @return 归档的日志数量的响应式单值
     */
    Mono<Long> archiveExpiredLogs(LocalDateTime beforeDate, boolean compressEnabled);

    /**
     * 归档应用日志（替代删除）
     *
     * @param beforeDate 归档此日期之前的应用日志
     * @param compressEnabled 是否启用压缩
     * @return 归档的日志数量的响应式单值
     */
    Mono<Long> archiveApplicationLogs(LocalDateTime beforeDate, boolean compressEnabled);

    /**
     * 归档错误日志（替代删除）
     *
     * @param beforeDate 归档此日期之前的错误日志
     * @param compressEnabled 是否启用压缩
     * @return 归档的日志数量的响应式单值
     */
    Mono<Long> archiveErrorLogs(LocalDateTime beforeDate, boolean compressEnabled);

    /**
     * 归档访问日志（替代删除）
     *
     * @param beforeDate 归档此日期之前的访问日志
     * @param compressEnabled 是否启用压缩
     * @return 归档的日志数量的响应式单值
     */
    Mono<Long> archiveAccessLogs(LocalDateTime beforeDate, boolean compressEnabled);

    /**
     * 归档业务日志（替代删除）
     *
     * @param beforeDate 归档此日期之前的业务日志
     * @param compressEnabled 是否启用压缩
     * @return 归档的日志数量的响应式单值
     */
    Mono<Long> archiveBusinessLogs(LocalDateTime beforeDate, boolean compressEnabled);

    /**
     * 归档系统日志（替代删除）
     *
     * @param beforeDate 归档此日期之前的系统日志
     * @param compressEnabled 是否启用压缩
     * @return 归档的日志数量的响应式单值
     */
    Mono<Long> archiveSystemLogs(LocalDateTime beforeDate, boolean compressEnabled);

    /**
     * 归档过期的操作日志（替代删除）
     * 专门用于归档系统操作日志（如用户操作记录、API调用记录等）
     *
     * @param beforeDate 归档此时间之前的操作日志
     * @param compressEnabled 是否启用压缩
     * @return 归档结果的响应式单值
     * @author Mr.Rey Copyright © 2025
     * @created 2025-09-29 15:20:00
     * @modified 2025-09-29 15:20:00
     * @version 1.0.0
     */
    Mono<Map<String, Object>> archiveExpiredOperationLogs(LocalDateTime beforeDate, boolean compressEnabled);

    /**
     * 执行统一日志归档
     *
     * @param compressEnabled 是否启用压缩
     * @return 归档结果的响应式单值
     */
    Mono<Map<String, Object>> performUnifiedLogArchive(boolean compressEnabled);

    /**
     * 获取日志统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志统计信息的响应式单值
     */
    Mono<Map<String, Object>> getLogStatistics(LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 系统监控功能 ====================

    /**
     * 获取系统状态
     *
     * @return 系统状态的响应式单值
     */
    Mono<SystemStatusResponse> getSystemStatus();

    /**
     * 初始化系统状态
     *
     * @return 初始化结果的响应式单值
     */
    Mono<Void> initializeSystemStatus();

    /**
     * 获取系统配置信息
     *
     * @return 系统配置列表的响应式流
     */
    Flux<SystemConfig> getSystemConfigs();

    /**
     * 更新系统配置
     *
     * @param configKey 配置键名
     * @param configValue 配置值
     * @return 更新结果的响应式单值
     */
    Mono<SystemConfig> updateSystemConfig(String configKey, String configValue);

    /**
     * 获取性能指标
     *
     * @param metricType 指标类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 性能指标列表的响应式流
     */
    Flux<PerformanceMetrics> getPerformanceMetrics(String metricType,
                                                   LocalDateTime startTime,
                                                   LocalDateTime endTime);

    /**
     * 记录性能指标
     *
     * @param metrics 性能指标对象
     * @return 记录结果的响应式单值
     */
    Mono<PerformanceMetrics> recordPerformanceMetrics(PerformanceMetrics metrics);

    /**
     * 获取系统健康检查结果
     *
     * @return 健康检查结果的响应式单值
     */
    Mono<Map<String, Object>> getHealthCheck();

    /**
     * 获取系统资源使用情况
     *
     * @return 资源使用情况的响应式单值
     */
    Mono<Map<String, Object>> getResourceUsage();

    /**
     * 获取并发用户统计
     *
     * @return 并发用户统计的响应式单值
     */
    Mono<Map<String, Object>> getConcurrentUserStats();

    /**
     * 触发系统告警
     *
     * @param alertType 告警类型
     * @param message 告警消息
     * @param level 告警级别
     * @return 告警处理结果的响应式单值
     */
    Mono<Void> triggerSystemAlert(String alertType, String message, String level);

    // ==================== 缓存管理功能 ====================

    /**
     * 清理系统缓存
     *
     * @param cacheType 缓存类型（可选，为空则清理所有缓存）
     * @return 清理结果的响应式单值
     */
    Mono<Map<String, Object>> clearCache(String cacheType);

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息的响应式单值
     */
    Mono<Map<String, Object>> getCacheStatistics();

    /**
     * 预热系统缓存
     *
     * @return 预热结果的响应式单值
     */
    Mono<Map<String, Object>> warmupCache();

    /**
     * 刷新用户缓存
     *
     * @param batchSize 批处理大小
     * @return 刷新的记录数
     */
    Mono<Integer> refreshUserCache(int batchSize);

    /**
     * 刷新权限缓存
     *
     * @param batchSize 批处理大小
     * @return 刷新的记录数
     */
    Mono<Integer> refreshPermissionCache(int batchSize);

    /**
     * 刷新配置缓存
     *
     * @return 刷新的记录数
     */
    Mono<Integer> refreshConfigCache();

    /**
     * 刷新业务缓存
     *
     * @param batchSize 批处理大小
     * @return 刷新的记录数
     */
    Mono<Integer> refreshBusinessCache(int batchSize);

    /**
     * 预加载用户缓存
     * @param batchSize 批处理大小
     * @return 预加载结果
     */
    Mono<Void> preloadUserCache(int batchSize);

    /**
     * 预加载权限缓存
     * @return 预加载结果
     */
    Mono<Void> preloadPermissionCache();

    /**
     * 预加载配置缓存
     * @return 预加载结果
     */
    Mono<Void> preloadConfigCache();

    /**
     * 预加载业务缓存
     * @param batchSize 批处理大小
     * @return 预加载结果
     */
    Mono<Void> preloadBusinessCache(int batchSize);

    /**
     * 执行缓存优化
     * @return 优化结果
     */
    Mono<Void> performCacheOptimization();

    /**
     * 验证系统配置
     * @return 验证结果
     */
    Mono<Void> validateSystemConfiguration();

    // ==================== 系统事件管理功能 ====================

    /**
     * 记录系统事件
     *
     * @param eventType 事件类型
     * @param logLevel 日志级别
     * @param module 模块名称
     * @param message 事件消息
     * @return 记录结果的响应式单值
     */
    Mono<SystemLog> recordSystemEvent(String eventType, String logLevel, String module, String message);

    /**
     * 根据事件更新系统状态
     *
     * @param eventType 事件类型
     * @return 更新结果的响应式单值
     */
    Mono<Void> updateSystemStatusByEvent(String eventType);

    /**
     * 获取系统状态详情
     *
     * @return 系统状态详情的响应式单值
     */
    Mono<Map<String, Object>> getDetails();

    // ==================== 用户登录管理功能 ====================

    /**
     * 更新用户最后登录时间
     *
     * @param username 用户名
     * @param loginTime 登录时间
     * @return 更新结果的响应式单值
     */
    Mono<Void> updateUserLastLoginTime(String username, LocalDateTime loginTime);

    /**
     * 检查用户登录安全性
     *
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 安全检查结果的响应式单值
     */
    Mono<Map<String, Object>> checkUserLoginSecurity(String username, String ipAddress, String userAgent);

    /**
     * 增加用户登录次数
     *
     * @param username 用户名
     * @return 更新结果的响应式单值
     */
    Mono<Void> incrementUserLoginCount(String username);

    // ==================== 性能监控方法 ====================

    /**
     * 收集WebFlux性能指标
     *
     * @return WebFlux性能指标
     */
    Mono<Map<String, Object>> collectWebfluxMetrics();

    /**
     * 收集R2DBC性能指标
     *
     * @return R2DBC性能指标
     */
    Mono<Map<String, Object>> collectR2dbcMetrics();

    /**
     * 收集Redis性能指标
     *
     * @return Redis性能指标
     */
    Mono<Map<String, Object>> collectRedisMetrics();

    /**
     * 收集系统资源指标
     *
     * @return 系统资源指标
     */
    Mono<Map<String, Object>> collectSystemResourceMetrics();

    /**
     * 收集响应式流指标
     *
     * @return 响应式流指标
     */
    Mono<Map<String, Object>> collectReactiveStreamMetrics();

    /**
     * 分析性能趋势
     *
     * @return 性能趋势分析结果
     */
    Mono<Map<String, Object>> analyzePerformanceTrends();

    /**
     * 检查性能告警
     *
     * @param responseTime 响应时间
     * @param memoryUsage 内存使用率
     * @param cpuUsage CPU使用率
     * @return 性能告警检查结果
     */
    Mono<Map<String, Object>> checkPerformanceAlerts(long responseTime, double memoryUsage, double cpuUsage);

    /**
     * 检查资源告警
     *
     * @param memoryUsage 内存使用率
     * @param cpuUsage CPU使用率
     * @return 资源告警检查结果
     */
    Mono<Map<String, Object>> checkResourceAlerts(double memoryUsage, double cpuUsage);

    // ==================== 系统维护功能 ====================

    /**
     * 清理过期日志
     *
     * @param beforeDate 清理此日期之前的日志
     * @return 清理数量的响应式单值
     */
    Mono<Long> cleanupExpiredLogs(LocalDateTime beforeDate);

    /**
     * 清理过期数据
     *
     * @param beforeDate 清理此日期之前的数据
     * @return 清理数量的响应式单值
     */
    Mono<Long> cleanupExpiredData(LocalDateTime beforeDate);

    /**
     * 清理临时文件
     *
     * @param beforeDate 清理此日期之前的临时文件
     * @return 清理数量的响应式单值
     */
    Mono<Long> cleanupTempFiles(LocalDateTime beforeDate);

    /**
     * 执行数据库维护
     *
     * @return 维护结果的响应式单值
     */
    Mono<Map<String, Object>> performDatabaseMaintenance();

    // ==================== 用户过期管理功能 ====================

    /**
     * 查找过期用户
     *
     * @param batchSize 批处理大小
     * @return 过期用户列表的响应式单值
     */
    Mono<java.util.List<com.honyrun.model.entity.business.User>> findExpiredUsers(int batchSize);

    /**
     * 查找即将在指定天数内过期的用户
     *
     * @param warningDays 提前警告天数
     * @param batchSize 批处理大小
     * @return 即将过期用户列表的响应式单值
     */
    Mono<java.util.List<com.honyrun.model.entity.business.User>> findUsersExpiringInDays(int warningDays, int batchSize);

    /**
     * 禁用过期用户
     *
     * @param batchSize 批处理大小
     * @return 禁用的用户数量
     */
    Mono<Integer> disableExpiredUsers(int batchSize);

    /**
     * 发送过期警告通知
     *
     * @param warningDays 提前警告天数
     * @param batchSize 批处理大小
     * @return 发送的通知数量
     */
    Mono<Integer> sendExpiryWarningNotifications(int warningDays, int batchSize);

    /**
     * 清理长期禁用的用户
     *
     * @param cleanupDisabledDays 清理禁用天数
     * @param batchSize 批处理大小
     * @return 清理的用户数量
     */
    Mono<Integer> cleanupLongTermDisabledUsers(int cleanupDisabledDays, int batchSize);

    /**
     * 生成用户过期报告
     *
     * @return 报告生成结果
     */
    Mono<Void> generateUserExpiryReport();

    /**
     * 更新用户过期统计信息
     *
     * @return 统计更新结果
     */
    Mono<Void> updateUserExpiryStatistics();

    // ==================== 系统控制功能 ====================

    /**
     * 获取Redis状态
     *
     * @return Redis状态信息
     */
    Mono<String> getRedisStatus();

    /**
     * 重启应用程序
     *
     * @return 重启操作结果
     */
    Mono<String> restartApplication();

    /**
     * 关闭应用程序
     *
     * @return 关闭操作结果
     */
    Mono<String> shutdownApplication();

    /**
     * 获取数据库状态
     *
     * @return 数据库状态信息
     */
    Mono<String> getDatabaseStatus();

    /**
     * 刷新系统配置
     *
     * @return 刷新操作结果
     */
    Mono<Map<String, Object>> refreshConfiguration();

    // ==================== 系统监控功能 ====================

    /**
     * 获取应用运行时间
     *
     * @return 运行时间（毫秒）
     */
    Mono<Long> getApplicationUptime();

    /**
     * 获取系统信息
     *
     * @return 系统信息响应
     */
    Mono<SystemStatusResponse> getSystemInfo();

    /**
     * 获取系统指标
     *
     * @return 系统指标信息
     */
    Mono<Map<String, Object>> getSystemMetrics();

    /**
     * 获取内存信息
     *
     * @return 内存信息
     */
    Mono<Map<String, Object>> getMemoryInfo();

    /**
     * 获取系统属性
     *
     * @return 系统属性信息
     */
    Mono<Map<String, Object>> getSystemProperties();
}


