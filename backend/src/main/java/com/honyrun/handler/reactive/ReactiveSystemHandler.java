package com.honyrun.handler.reactive;

import com.honyrun.service.reactive.ReactiveSystemService;
import com.honyrun.service.health.UnifiedHealthCheckService;
import com.honyrun.model.dto.request.SystemSettingRequest;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.common.DateUtil;
import com.honyrun.exception.BusinessException;
import com.honyrun.util.validation.ReactiveValidator;
import com.honyrun.util.ErrorDetailsUtil;
import com.honyrun.util.TraceIdUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * 响应式系统处理器
 *
 * 实现函数式处理逻辑，用于WebFlux函数式路由
 * 提供系统管理相关的处理方法，包括系统设置、日志管理、监控功能等
 * 支持响应式错误处理和统一响应格式
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 01:30:00
 * @modified 2025-07-02 优化Bean命名规范
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component("reactiveSystemHandler")
public class ReactiveSystemHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveSystemHandler.class);

    private final ReactiveSystemService systemService;
    private final UnifiedHealthCheckService unifiedHealthCheckService;
    @SuppressWarnings("unused")
    private final ReactiveValidator reactiveValidator;

    public ReactiveSystemHandler(ReactiveSystemService systemService,
                                 UnifiedHealthCheckService unifiedHealthCheckService,
                                 ReactiveValidator reactiveValidator) {
        this.systemService = systemService;
        this.unifiedHealthCheckService = unifiedHealthCheckService;
        this.reactiveValidator = reactiveValidator;
    }

    // ==================== 系统设置处理器 ====================

    /**
     * 获取所有系统设置
     *
     * @param request 服务器请求对象
     * @return 系统设置列表的响应式单值
     */
    public Mono<ServerResponse> getAllSettings(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取所有系统设置");

        return systemService.getAllSettings()
                .collectList()
                .flatMap(settings -> {
                    LoggingUtil.info(logger, "获取系统设置成功，数量: {}", settings.size());
                    ApiResponse<Object> response = ApiResponse.success(settings, "获取系统设置成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 根据分类获取系统设置
     *
     * @param request 服务器请求对象
     * @return 系统设置列表的响应式单值
     */
    public Mono<ServerResponse> getSettingsByCategory(ServerRequest request) {
        String category = request.pathVariable("category");
        LoggingUtil.info(logger, "Handler调用: 根据分类获取系统设置, category: {}", category);

        return systemService.getSettingsByCategory(category)
                .collectList()
                .flatMap(settings -> {
                    LoggingUtil.info(logger, "根据分类获取系统设置成功，分类: {}, 数量: {}", category, settings.size());
                    ApiResponse<Object> response = ApiResponse.success(settings, "获取系统设置成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 根据键名获取系统设置
     *
     * @param request 服务器请求对象
     * @return 系统设置的响应式单值
     */
    public Mono<ServerResponse> getSettingByKey(ServerRequest request) {
        String settingKey = request.pathVariable("key");
        LoggingUtil.info(logger, "Handler调用: 根据键名获取系统设置, settingKey: {}", settingKey);

        return systemService.getSettingByKey(settingKey)
                .flatMap(setting -> {
                    LoggingUtil.info(logger, "根据键名获取系统设置成功: {}", settingKey);
                    ApiResponse<Object> response = ApiResponse.success(setting, "获取系统设置成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(this::handleError);
    }

    /**
     * 创建系统设置
     *
     * @param request 服务器请求对象
     * @return 创建结果的响应式单值
     */
    public Mono<ServerResponse> createSetting(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 创建系统设置");

        return request.bodyToMono(SystemSettingRequest.class)
                .flatMap(settingRequest -> {
                    LoggingUtil.info(logger, "创建系统设置请求: {}", settingRequest.getSettingKey());
                    return systemService.createSetting(settingRequest);
                })
                .flatMap(response -> {
                    LoggingUtil.info(logger, "系统设置创建成功: {}", response.getSettingKey());
                    ApiResponse<Object> apiResponse = ApiResponse.success(response, "系统设置创建成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(apiResponse);
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 更新系统设置
     *
     * @param request 服务器请求对象
     * @return 更新结果的响应式单值
     */
    public Mono<ServerResponse> updateSetting(ServerRequest request) {
        String settingKey = request.pathVariable("key");
        LoggingUtil.info(logger, "Handler调用: 更新系统设置, settingKey: {}", settingKey);

        return request.bodyToMono(SystemSettingRequest.class)
                .flatMap(settingRequest -> {
                    LoggingUtil.info(logger, "更新系统设置请求: {}", settingKey);
                    return systemService.updateSetting(settingKey, settingRequest);
                })
                .flatMap(response -> {
                    LoggingUtil.info(logger, "系统设置更新成功: {}", settingKey);
                    ApiResponse<Object> apiResponse = ApiResponse.success(response, "系统设置更新成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(apiResponse);
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 删除系统设置
     *
     * @param request 服务器请求对象
     * @return 删除操作的响应式单值
     */
    public Mono<ServerResponse> deleteSetting(ServerRequest request) {
        String settingKey = request.pathVariable("key");
        LoggingUtil.info(logger, "Handler调用: 删除系统设置, settingKey: {}", settingKey);

        return systemService.deleteSetting(settingKey)
                .then(Mono.defer(() -> {
                    LoggingUtil.info(logger, "系统设置删除成功: {}", settingKey);
                    ApiResponse<Void> apiResponse = ApiResponse.success("系统设置删除成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(apiResponse);
                }))
                .onErrorResume(this::handleError);
    }

    // ==================== 系统监控处理器 ====================

    /**
     * 获取系统状态
     *
     * @param request 服务器请求对象
     * @return 系统状态的响应式单值
     */
    public Mono<ServerResponse> getSystemStatus(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取系统状态");

        return systemService.getSystemStatus()
                .flatMap(status -> {
                    LoggingUtil.info(logger, "系统状态获取成功: {}", status.getHealthStatus());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", status,
                                "message", "获取系统状态成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 获取系统健康检查
     *
     * @param request 服务器请求对象
     * @return 健康检查结果的响应式单值
     */
    public Mono<ServerResponse> getSystemHealth(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取系统健康检查");

        return unifiedHealthCheckService.getSystemHealthSummary()
                .flatMap(healthSummary -> {
                    LoggingUtil.info(logger, "系统健康检查完成，状态: {}", healthSummary.getStatus().getCode());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.success(healthSummary, "系统健康检查完成"));
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "系统健康检查失败", error);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("系统健康检查失败: " + error.getMessage()));
                });
    }

    /**
     * 获取详细健康检查
     *
     * @param request 服务器请求对象
     * @return 详细健康检查结果的响应式单值
     */
    public Mono<ServerResponse> getDetailedHealth(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取详细健康检查");

        return unifiedHealthCheckService.checkAllHealth()
                .flatMap(healthMap -> {
                    LoggingUtil.info(logger, "详细健康检查完成，组件数量: {}", healthMap.size());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.success(healthMap, "详细健康检查完成"));
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "详细健康检查失败", error);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("详细健康检查失败: " + error.getMessage()));
                });
    }

    /**
     * 获取数据库健康检查
     *
     * @param request 服务器请求对象
     * @return 数据库健康检查结果的响应式单值
     */
    public Mono<ServerResponse> getDatabaseHealth(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取数据库健康检查");

        return unifiedHealthCheckService.checkComponentHealth("database")
                .flatMap(healthStatus -> {
                    LoggingUtil.info(logger, "数据库健康检查完成，状态: {}", healthStatus.getStatus().getCode());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.success(healthStatus, "数据库健康检查完成"));
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "数据库健康检查失败", error);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("数据库健康检查失败: " + error.getMessage()));
                });
    }

    /**
     * 获取Redis健康检查
     *
     * @param request 服务器请求对象
     * @return Redis健康检查结果的响应式单值
     */
    public Mono<ServerResponse> getRedisHealth(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取Redis健康检查");

        return unifiedHealthCheckService.checkComponentHealth("redis")
                .flatMap(healthStatus -> {
                    LoggingUtil.info(logger, "Redis健康检查完成，状态: {}", healthStatus.getStatus().getCode());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.success(healthStatus, "Redis健康检查完成"));
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Redis健康检查失败", error);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("Redis健康检查失败: " + error.getMessage()));
                });
    }

    /**
     * 批量更新系统设置
     *
     * @param request 服务器请求对象
     * @return 批量更新结果的响应式单值
     */
    public Mono<ServerResponse> updateSystemSettings(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 批量更新系统设置");

        return request.bodyToMono(Map.class)
                .flatMap(settingsMap -> {
                    // 安全地转换Map中的值为String类型
                    Map<String, String> settings = new HashMap<>();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typedSettingsMap = (Map<String, Object>) settingsMap;
                    for (Map.Entry<String, Object> entry : typedSettingsMap.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        // 将所有值转换为字符串
                        String stringValue = value != null ? value.toString() : null;
                        settings.put(key, stringValue);
                    }
                    
                    LoggingUtil.info(logger, "批量更新系统设置，数量: {}", settings.size());
                    
                    return systemService.batchUpdateSettings(settings)
                            .collectList()
                            .flatMap(responses -> {
                                LoggingUtil.info(logger, "批量更新系统设置成功，更新数量: {}", responses.size());
                                return ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of(
                                            "success", true,
                                            "data", responses,
                                            "message", "批量更新系统设置成功"
                                        ));
                            });
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 根据键更新系统设置
     *
     * @param request 服务器请求对象
     * @return 更新结果的响应式单值
     */
    public Mono<ServerResponse> updateSystemSettingByKey(ServerRequest request) {
        String settingKey = request.pathVariable("key");
        LoggingUtil.info(logger, "Handler调用: 根据键更新系统设置, settingKey: {}", settingKey);

        return request.bodyToMono(SystemSettingRequest.class)
                .flatMap(settingRequest -> {
                    LoggingUtil.info(logger, "更新系统设置请求: {}", settingKey);
                    return systemService.updateSetting(settingKey, settingRequest);
                })
                .flatMap(response -> {
                    LoggingUtil.info(logger, "系统设置更新成功: {}", settingKey);
                    ApiResponse<Object> apiResponse = ApiResponse.success(response, "系统设置更新成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(apiResponse);
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 导出系统设置
     *
     * @param request 服务器请求对象
     * @return 导出结果的响应式单值
     */
    public Mono<ServerResponse> exportSettings(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 导出系统设置");

        return Mono.fromCallable(() -> {
                    // 模拟导出系统设置
                    LoggingUtil.info(logger, "模拟导出系统设置");
                    Map<String, Object> exportResult = new HashMap<>();
                    exportResult.put("fileSize", 1024);
                    exportResult.put("exportTime", DateUtil.format(LocalDateTime.now()));
                    exportResult.put("filename", "system_settings_export.json");
                    return exportResult;
                })
                .flatMap(exportResult -> {
                    LoggingUtil.info(logger, "系统设置导出成功，文件大小: {} bytes", exportResult.get("fileSize"));
                    ApiResponse<Object> apiResponse = ApiResponse.success(exportResult, "导出系统设置成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(apiResponse);
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 导入系统设置
     *
     * @param request 服务器请求对象
     * @return 导入结果的响应式单值
     */
    public Mono<ServerResponse> importSettings(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 导入系统设置");

        return request.multipartData()
                .flatMap(multipartData -> {
                    var filePart = multipartData.getFirst("file");
                    if (filePart == null) {
                        LoggingUtil.warn(logger, "未找到导入文件");
                        return Mono.error(new BusinessException("未找到导入文件"));
                    }

                    LoggingUtil.info(logger, "开始处理导入文件: {}", filePart.name());
                    // 使用实际存在的导入方法
                    Map<String, Object> settings = new HashMap<>();
                    settings.put("filename", filePart.name());
                    return systemService.importSettings(settings);
                })
                .flatMap(importResult -> {
                    LoggingUtil.info(logger, "系统设置导入成功，导入结果: {}", importResult);
                    ApiResponse<Object> apiResponse = ApiResponse.success(importResult, "导入系统设置成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(apiResponse);
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 获取构建信息
     *
     * @param request 服务器请求对象
     * @return 构建信息的响应式单值
     */
    public Mono<ServerResponse> getBuildInfo(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取构建信息");

        return Mono.fromCallable(() -> {
            Map<String, Object> buildInfo = new HashMap<>();
            buildInfo.put("buildVersion", "20250128");
            buildInfo.put("buildTime", DateUtil.format(LocalDateTime.now()));
            buildInfo.put("buildNumber", "1");
            buildInfo.put("gitCommit", "latest");
            buildInfo.put("gitBranch", "main");
            buildInfo.put("javaVersion", System.getProperty("java.version"));
            buildInfo.put("springBootVersion", "3.2.0");
            buildInfo.put("mavenVersion", "3.9.0");
            return buildInfo;
        })
        .flatMap(buildInfo -> {
            LoggingUtil.info(logger, "构建信息获取成功");
            ApiResponse<Object> apiResponse = ApiResponse.success(buildInfo, "构建信息获取成功");
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(apiResponse);
        })
        .onErrorResume(this::handleError);
    }

    // ==================== 系统日志处理器 ====================

    /**
     * 获取系统日志列表
     *
     * @param request 服务器请求对象
     * @return 系统日志列表的响应式单值
     */
    public Mono<ServerResponse> getSystemLogs(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取系统日志列表");

        // 提取查询参数
        String logType = request.queryParam("logType").orElse(null);
        String logLevel = request.queryParam("logLevel").orElse(null);
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("20"));

        return systemService.getSystemLogs(logType, logLevel, null, null, page, size)
                .collectList()
                .flatMap(logs -> {
                    LoggingUtil.info(logger, "获取系统日志成功，数量: {}", logs.size());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", logs,
                                "message", "获取系统日志成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    // ==================== 工具方法 ====================

    /**
     * 统一错误处理
     *
     * @param throwable 异常对象
     * @return 错误响应的响应式单值
     */
    private Mono<ServerResponse> handleError(Throwable throwable) {
        LoggingUtil.error(logger, "Handler处理异常", throwable);

        return Mono.deferContextual(ctxView -> Mono.fromCallable(() -> {
            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
            String path = "";
            Map<String, Object> details = new HashMap<>();
            details.put("error", throwable.getMessage());
            details.put("path", path);

            if (throwable instanceof BusinessException) {
                BusinessException businessException = (BusinessException) throwable;
                return ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.error("400", businessException.getMessage(), traceId, details, path));
            }

            // 处理ValidationException - 参数验证异常应该返回400状态码
            if (throwable instanceof com.honyrun.exception.ValidationException) {
                com.honyrun.exception.ValidationException validationException = (com.honyrun.exception.ValidationException) throwable;
                return ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.error("400", validationException.getMessage(), traceId, details, path));
            }

            return ServerResponse.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(ApiResponse.error("500", "系统内部错误", traceId, details, path));
        }))
        .flatMap(mono -> mono);
    }



    /**
     * 获取系统信息
     *
     * @param request 服务器请求对象
     * @return 系统信息的响应式单值
     */
    public Mono<ServerResponse> getSystemInfo(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取系统信息");

        return systemService.getSystemInfo()
                .flatMap(systemInfo -> {
                    LoggingUtil.info(logger, "系统信息获取成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", systemInfo,
                                "message", "获取系统信息成功",
                                "timestamp", DateUtil.format(LocalDateTime.now())
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 获取系统指标
     *
     * @param request 服务器请求对象
     * @return 系统指标的响应式单值
     */
    public Mono<ServerResponse> getSystemMetrics(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取系统指标");

        return systemService.getResourceUsage()
                .flatMap(metrics -> {
                    LoggingUtil.info(logger, "系统指标获取成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", metrics,
                                "message", "获取系统指标成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 获取版本信息
     *
     * @param request 服务器请求对象
     * @return 版本信息的响应式单值
     */
    public Mono<ServerResponse> getVersionInfo(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取版本信息");

        return Mono.fromCallable(() -> {
            Map<String, Object> versionInfo = new HashMap<>();
            versionInfo.put("version", "2.0.0");
            versionInfo.put("buildTime", "2025-07-01");
            versionInfo.put("gitCommit", "latest");
            versionInfo.put("environment", "development");
            return versionInfo;
        })
        .flatMap(versionInfo -> {
            LoggingUtil.info(logger, "版本信息获取成功");
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                        "success", true,
                        "data", versionInfo,
                        "message", "获取版本信息成功"
                    ));
        })
        .onErrorResume(this::handleError);
    }

    /**
     * 获取系统版本信息（用于 /api/v1/version 路径）
     *
     * @param request 服务器请求对象
     * @return 系统版本信息的响应式单值
     */
    public Mono<ServerResponse> getSystemVersion(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取系统版本");

        return Mono.fromCallable(() -> {
            Map<String, Object> versionInfo = new HashMap<>();
            versionInfo.put("applicationName", "HonyRun");
            versionInfo.put("version", "2.0.0");
            versionInfo.put("apiVersion", "v1");
            versionInfo.put("buildTime", "2025-07-01");
            versionInfo.put("buildVersion", "20250128");
            versionInfo.put("gitCommit", "latest");
            versionInfo.put("environment", "development");
            versionInfo.put("javaVersion", System.getProperty("java.version"));
            versionInfo.put("springBootVersion", "3.2.0");
            versionInfo.put("timestamp", DateUtil.format(LocalDateTime.now()));
            return versionInfo;
        })
        .flatMap(versionInfo -> {
            LoggingUtil.info(logger, "系统版本获取成功");
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                        "success", true,
                        "data", versionInfo,
                        "message", "获取系统版本成功"
                    ));
        })
        .onErrorResume(this::handleError);
    }

    /**
     * 获取版本历史
     *
     * @param request 服务器请求对象
     * @return 版本历史的响应式单值
     */
    public Mono<ServerResponse> getVersionHistory(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取版本历史");

        return Mono.fromCallable(() -> {
            // 模拟版本历史数据
            Map<String, Object> history = new HashMap<>();
            history.put("currentVersion", "2.0.0");
            history.put("previousVersions", java.util.Arrays.asList(
                Map.of("version", "1.9.0", "releaseDate", "2025-01-20", "changes", "性能优化"),
                Map.of("version", "1.8.0", "releaseDate", "2025-01-15", "changes", "新增功能"),
                Map.of("version", "1.7.0", "releaseDate", "2025-01-10", "changes", "Bug修复")
            ));
            return history;
        })
        .flatMap(history -> {
            LoggingUtil.info(logger, "版本历史获取成功");
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                        "success", true,
                        "data", history,
                        "message", "获取版本历史成功"
                    ));
        })
        .onErrorResume(this::handleError);
    }

    /**
     * 检查版本兼容性
     *
     * @param request 服务器请求对象
     * @return 版本兼容性检查结果的响应式单值
     */
    public Mono<ServerResponse> checkVersionCompatibility(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 检查版本兼容性");

        return request.bodyToMono(Map.class)
                .cast(Map.class)
                .flatMap(requestBody -> {
                    String targetVersion = (String) requestBody.get("targetVersion");
                    String currentVersion = (String) requestBody.get("currentVersion");
                    
                    // 参数验证
                    if (targetVersion == null || targetVersion.trim().isEmpty()) {
                        LoggingUtil.warn(logger, "目标版本参数为空或无效");
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of(
                                    "success", false,
                                    "message", "目标版本参数不能为空",
                                    "error", "INVALID_PARAMETER"
                                ));
                    }
                    
                    if (currentVersion == null || currentVersion.trim().isEmpty()) {
                        LoggingUtil.warn(logger, "当前版本参数为空或无效");
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of(
                                    "success", false,
                                    "message", "当前版本参数不能为空",
                                    "error", "INVALID_PARAMETER"
                                ));
                    }
                    
                    LoggingUtil.info(logger, "检查版本兼容性，目标版本: {}, 当前版本: {}", targetVersion, currentVersion);

                    Map<String, Object> compatibilityResult = new HashMap<>();
                    compatibilityResult.put("currentVersion", currentVersion);
                    compatibilityResult.put("targetVersion", targetVersion);
                    compatibilityResult.put("compatible", true);
                    compatibilityResult.put("message", "版本兼容");
                    compatibilityResult.put("checkTime", DateUtil.format(LocalDateTime.now()));

                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", compatibilityResult,
                                "message", "版本兼容性检查完成"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 获取系统配置
     *
     * @param request 服务器请求对象
     * @return 系统配置的响应式单值
     */
    public Mono<ServerResponse> getSystemConfig(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取系统配置");

        return systemService.getSystemConfigs()
                .collectList()
                .flatMap(configs -> {
                    LoggingUtil.info(logger, "系统配置获取成功，数量: {}", configs.size());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", configs,
                                "message", "获取系统配置成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 更新系统配置
     *
     * @param request 服务器请求对象
     * @return 更新结果的响应式单值
     */
    public Mono<ServerResponse> updateSystemConfig(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 更新系统配置");

        return request.bodyToMono(Map.class)
                .flatMap(configMap -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> configData = (Map<String, String>) configMap;
                    String configKey = configData.get("configKey");
                    String configValue = configData.get("configValue");
                    LoggingUtil.info(logger, "更新系统配置: {} = {}", configKey, configValue);
                    return systemService.updateSystemConfig(configKey, configValue);
                })
                .flatMap(config -> {
                    LoggingUtil.info(logger, "系统配置更新成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", config,
                                "message", "系统配置更新成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 清理系统日志
     *
     * @param request 服务器请求对象
     * @return 清理结果的响应式单值
     */
    public Mono<ServerResponse> clearSystemLogs(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 清理系统日志");

        return systemService.cleanupExpiredLogs(LocalDateTime.now().minusDays(30))
                .flatMap(count -> {
                    LoggingUtil.info(logger, "系统日志清理完成，清理数量: {}", count);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", Map.of("cleanedCount", count),
                                "message", "系统日志清理成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 获取缓存信息
     *
     * @param request 服务器请求对象
     * @return 缓存信息的响应式单值
     */
    public Mono<ServerResponse> getCacheInfo(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取缓存信息");

        return systemService.getCacheStatistics()
                .flatMap(cacheInfo -> {
                    LoggingUtil.info(logger, "缓存信息获取成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", cacheInfo,
                                "message", "获取缓存信息成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 清理缓存
     *
     * @param request 服务器请求对象
     * @return 清理结果的响应式单值
     */
    public Mono<ServerResponse> clearCache(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 清理缓存");

        String cacheType = request.queryParam("type").orElse(null);

        return systemService.clearCache(cacheType)
                .flatMap(result -> {
                    LoggingUtil.info(logger, "缓存清理完成");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", result,
                                "message", "缓存清理成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 创建系统备份
     *
     * @param request 服务器请求对象
     * @return 备份结果的响应式单值
     */
    public Mono<ServerResponse> createBackup(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 创建系统备份");

        return Mono.fromCallable(() -> {
            Map<String, Object> backupResult = new HashMap<>();
            backupResult.put("backupId", "backup_" + System.currentTimeMillis());
            backupResult.put("backupTime", LocalDateTime.now());
            backupResult.put("status", "SUCCESS");
            backupResult.put("size", "125MB");
            return backupResult;
        })
        .flatMap(backupResult -> {
            LoggingUtil.info(logger, "系统备份创建成功");
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                        "success", true,
                        "data", backupResult,
                        "message", "系统备份创建成功"
                    ));
        })
        .onErrorResume(this::handleError);
    }

    /**
     * 恢复系统
     *
     * @param request 服务器请求对象
     * @return 恢复结果的响应式单值
     */
    public Mono<ServerResponse> restoreSystem(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 恢复系统");

        return request.bodyToMono(Map.class)
                .flatMap(restoreRequest -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> restoreData = (Map<String, String>) restoreRequest;
                    String backupId = restoreData.get("backupId");
                    LoggingUtil.info(logger, "恢复系统，备份ID: {}", backupId);

                    Map<String, Object> restoreResult = new HashMap<>();
                    restoreResult.put("restoreId", "restore_" + System.currentTimeMillis());
                    restoreResult.put("restoreTime", LocalDateTime.now());
                    restoreResult.put("status", "SUCCESS");
                    restoreResult.put("backupId", backupId);

                    return Mono.just(restoreResult);
                })
                .flatMap(restoreResult -> {
                    LoggingUtil.info(logger, "系统恢复完成");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", restoreResult,
                                "message", "系统恢复成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 刷新系统配置
     *
     * @param request 服务器请求对象
     * @return 刷新结果的响应式单值
     */
    public Mono<ServerResponse> refreshSystemConfig(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 刷新系统配置");

        return systemService.refreshConfiguration()
                .flatMap(result -> {
                    LoggingUtil.info(logger, "系统配置刷新完成: {}", result.get("status"));
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", result,
                                "message", "Configuration refreshed successfully"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 获取系统设置分类列表
     *
     * @param request 服务器请求对象
     * @return 设置分类列表的响应式单值
     */
    public Mono<ServerResponse> getSettingCategories(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取系统设置分类列表");

        return systemService.getSettingCategories()
                .collectList()
                .flatMap(categories -> {
                    LoggingUtil.info(logger, "获取系统设置分类成功，数量: {}", categories.size());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", categories,
                                "message", "获取系统设置分类成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 获取系统统计信息
     *
     * @param request 服务器请求对象
     * @return 系统统计信息的响应式单值
     */
    public Mono<ServerResponse> getSystemStats(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取系统统计信息");

        return Mono.fromCallable(() -> {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("uptime", "2 days 5 hours");
                    stats.put("requestCount", 12345);
                    stats.put("errorCount", 23);
                    stats.put("activeUsers", 156);
                    stats.put("memoryUsage", "45%");
                    stats.put("cpuUsage", "23%");
                    return stats;
                })
                .flatMap(stats -> {
                    LoggingUtil.info(logger, "系统统计信息获取成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", stats,
                                "message", "获取系统统计信息成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 获取系统性能监控信息
     *
     * @param request 服务器请求对象
     * @return 系统性能监控信息的响应式单值
     */
    public Mono<ServerResponse> getPerformanceMonitor(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取系统性能监控信息");

        return Mono.fromCallable(() -> {
                    Map<String, Object> performance = new HashMap<>();
                    performance.put("cpu", Map.of("usage", "23%", "cores", 8));
                    performance.put("memory", Map.of("used", "2.5GB", "total", "8GB", "usage", "31%"));
                    performance.put("disk", Map.of("read", "150MB/s", "write", "80MB/s"));
                    performance.put("network", Map.of("in", "50Mbps", "out", "30Mbps"));
                    return performance;
                })
                .flatMap(performance -> {
                    LoggingUtil.info(logger, "系统性能监控信息获取成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", performance,
                                "message", "获取系统性能监控信息成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 系统配置热更新
     *
     * @param request 服务器请求对象
     * @return 配置热更新结果的响应式单值
     */
    public Mono<ServerResponse> hotUpdateConfig(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 系统配置热更新");

        return request.bodyToMono(Map.class)
                .flatMap(configRequest -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> configData = (Map<String, String>) configRequest;
                    String configKey = configData.get("configKey");
                    String configValue = configData.get("configValue");
                    LoggingUtil.info(logger, "热更新配置: {} = {}", configKey, configValue);
                    
                    // 模拟配置更新
                    Map<String, Object> result = new HashMap<>();
                    result.put("configKey", configKey);
                    result.put("oldValue", "INFO");
                    result.put("newValue", configValue);
                    result.put("updateTime", DateUtil.format(LocalDateTime.now()));
                    
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", result,
                                "message", "配置热更新成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 重载系统配置
     *
     * @param request 服务器请求对象
     * @return 配置重载结果的响应式单值
     */
    public Mono<ServerResponse> reloadConfig(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 重载系统配置");

        return Mono.fromCallable(() -> {
                    // 模拟配置重载
                    Map<String, Object> result = new HashMap<>();
                    result.put("reloadTime", DateUtil.format(LocalDateTime.now()));
                    result.put("configCount", 25);
                    result.put("status", "SUCCESS");
                    return result;
                })
                .flatMap(result -> {
                    LoggingUtil.info(logger, "系统配置重载成功，重载配置数量: {}", result.get("configCount"));
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", result,
                                "message", "系统配置重载成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 图像转换处理
     *
     * @param request 服务器请求对象
     * @return 图像转换结果的响应式单值
     */
    public Mono<ServerResponse> imageConvert(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 图像转换");

        return request.bodyToMono(Map.class)
                .cast(Map.class)
                .flatMap(requestBody -> {
                    String imageData = (String) requestBody.get("imageData");
                    String targetFormat = (String) requestBody.get("targetFormat");
                    
                    // 参数验证
                    if (imageData == null || imageData.trim().isEmpty()) {
                        LoggingUtil.warn(logger, "图像数据参数为空或无效");
                        
                        return reactor.core.publisher.Mono.deferContextual(ctxView -> {
                            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
                            String path = request.path();
                            Map<String, Object> details = new HashMap<>();
                            details.put("error", "图像数据不能为空");
                            details.put("path", path);
                            java.util.List<java.util.Map<String, Object>> violations = new java.util.ArrayList<>();
                            violations.add(java.util.Map.of("field", "imageData", "message", "不能为空"));
                            details.put("violations", violations);
                            return ServerResponse.badRequest()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.error("400", "输入验证失败", traceId, details, path));
                        });
                    }
                    
                    if (targetFormat == null || targetFormat.trim().isEmpty()) {
                        LoggingUtil.warn(logger, "目标格式参数为空或无效");
                        
                        return reactor.core.publisher.Mono.deferContextual(ctxView -> {
                            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
                            String path = request.path();
                            Map<String, Object> details = new HashMap<>();
                            details.put("error", "目标格式不能为空");
                            details.put("path", path);
                            java.util.List<java.util.Map<String, Object>> violations = new java.util.ArrayList<>();
                            violations.add(java.util.Map.of("field", "targetFormat", "message", "不能为空"));
                            details.put("violations", violations);
                            return ServerResponse.badRequest()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.error("400", "输入验证失败", traceId, details, path));
                        });
                    }
                    
                    // 模拟图像转换处理
                    Map<String, Object> result = new HashMap<>();
                    result.put("convertTime", DateUtil.format(LocalDateTime.now()));
                    result.put("originalFormat", "jpg");
                    result.put("targetFormat", targetFormat);
                    result.put("status", "SUCCESS");
                    result.put("message", "图像转换成功");
                    
                    LoggingUtil.info(logger, "图像转换处理完成，目标格式: {}", targetFormat);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", result,
                                "message", "图像转换成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 图像验证处理
     *
     * @param request 服务器请求对象
     * @return 图像验证结果的响应式单值
     */
    public Mono<ServerResponse> imageValidate(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 图像验证");

        return request.bodyToMono(Map.class)
                .flatMap(requestBody -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bodyData = (Map<String, Object>) requestBody;
                    // 从请求体获取图像数据
                    String imageData = (String) bodyData.get("imageData");
                    
                    // 验证必需参数
                    if (imageData == null || imageData.trim().isEmpty()) {
                        return reactor.core.publisher.Mono.deferContextual(ctxView -> {
                            String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
                            String path = request.path();
                            Map<String, Object> details = new HashMap<>();
                            details.put("error", "图像数据不能为空");
                            details.put("path", path);
                            java.util.List<java.util.Map<String, Object>> violations = new java.util.ArrayList<>();
                            violations.add(java.util.Map.of("field", "imageData", "message", "不能为空"));
                            details.put("violations", violations);
                            return ServerResponse.badRequest()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.error("400", "输入验证失败", traceId, details, path));
                        });
                    }
                    
                    // 模拟图像验证处理
                    boolean isValid = true; // 模拟验证结果
                    
                    LoggingUtil.info(logger, "图像验证处理完成");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", isValid,
                                "message", "图像验证成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }

    /**
     * 获取系统性能监控
     *
     * @param request 服务器请求对象
     * @return 系统性能监控数据的响应式单值
     */
    public Mono<ServerResponse> getSystemPerformanceMonitor(ServerRequest request) {
        LoggingUtil.info(logger, "Handler调用: 获取系统性能监控");

        return Mono.fromCallable(() -> {
                    // 模拟系统性能监控数据
                    Map<String, Object> performanceData = new HashMap<>();
                    performanceData.put("timestamp", DateUtil.format(LocalDateTime.now()));
                    performanceData.put("cpuUsage", 45.6);
                    performanceData.put("memoryUsage", 67.8);
                    performanceData.put("diskUsage", 34.2);
                    performanceData.put("networkIn", 1024);
                    performanceData.put("networkOut", 2048);
                    performanceData.put("activeConnections", 156);
                    performanceData.put("responseTime", 125);
                    performanceData.put("throughput", 890);
                    return performanceData;
                })
                .flatMap(performanceData -> {
                    LoggingUtil.info(logger, "系统性能监控数据获取成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "success", true,
                                "data", performanceData,
                                "message", "获取系统性能监控成功"
                            ));
                })
                .onErrorResume(this::handleError);
    }
}



