package com.honyrun.handler.reactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.service.reactive.ReactiveApiVersionService;
import com.honyrun.util.LoggingUtil;
// 新增导入
import com.honyrun.util.TraceIdUtil;
import reactor.core.publisher.Mono;

/**
 * API版本管理处理器
 *
 * 处理API版本相关的HTTP请求，包括版本查询、变更日志、弃用信息、迁移指导等
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-01-11 14:50:00
 * @modified 2025-01-11 14:50:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class ApiVersionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiVersionHandler.class);

    private final ReactiveApiVersionService apiVersionService;

    /**
     * 构造函数注入依赖
     *
     * @param apiVersionService 响应式API版本服务
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public ApiVersionHandler(ReactiveApiVersionService apiVersionService) {
        this.apiVersionService = apiVersionService;
    }

    /**
     * 获取支持的API版本列表
     *
     * @param request HTTP请求对象
     * @return 包含支持版本列表的响应
     */
    public Mono<ServerResponse> getSupportedVersions(ServerRequest request) {
        LoggingUtil.info(logger, "Processing request to get supported API versions");

        return apiVersionService.getSupportedVersions()
                .collectList()
                .map(versions -> ApiResponse.success(versions, "Supported versions retrieved successfully"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "Successfully returned supported versions"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Failed to get supported versions", error);
                    // 统一错误响应
                    return badRequest(request, "Failed to get supported versions: " + error.getMessage(), error);
                });
    }

    /**
     * 获取指定版本的变更日志
     *
     * @param request HTTP请求对象，包含版本参数
     * @return 包含版本变更日志的响应
     */
    public Mono<ServerResponse> getVersionChangeLog(ServerRequest request) {
        String version = request.pathVariable("version");
        LoggingUtil.info(logger, "Processing request to get change log for version: {}", version);

        return apiVersionService.getVersionChangeLog(version)
                .map(changeLog -> ApiResponse.success(changeLog, "Version change log retrieved successfully"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "Successfully returned change log for version: {}", version))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Failed to get change log for version: " + version, error);
                    // 统一错误响应
                    return badRequest(request, "Failed to get change log: " + error.getMessage(), error);
                });
    }

    /**
     * 获取所有版本的变更日志
     *
     * @param request HTTP请求对象
     * @return 包含所有版本变更日志的响应
     */
    public Mono<ServerResponse> getAllVersionChangeLogs(ServerRequest request) {
        LoggingUtil.info(logger, "Processing request to get all version change logs");

        return apiVersionService.getAllVersionChangeLogs()
                .collectList()
                .map(changeLogs -> ApiResponse.success(changeLogs, "All version change logs retrieved successfully"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "Successfully returned all version change logs"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Failed to get all version change logs", error);
                    // 统一错误响应
                    return badRequest(request, "Failed to get change logs: " + error.getMessage(), error);
                });
    }

    /**
     * 获取版本弃用信息
     *
     * @param request HTTP请求对象，包含版本参数
     * @return 包含版本弃用信息的响应
     */
    public Mono<ServerResponse> getVersionDeprecationInfo(ServerRequest request) {
        String version = request.pathVariable("version");
        LoggingUtil.info(logger, "Processing request to get deprecation info for version: {}", version);

        return apiVersionService.getVersionDeprecationInfo(version)
                .map(deprecationInfo -> ApiResponse.success(deprecationInfo, "Version deprecation info retrieved successfully"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "Successfully returned deprecation info for version: {}", version))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Failed to get deprecation info for version: " + version, error);
                    // 统一错误响应
                    return badRequest(request, "Failed to get deprecation info: " + error.getMessage(), error);
                });
    }

    /**
     * 获取版本迁移指导
     *
     * @param request HTTP请求对象，包含源版本和目标版本参数
     * @return 包含版本迁移指导的响应
     */
    public Mono<ServerResponse> getVersionMigrationGuide(ServerRequest request) {
        String fromVersion = request.queryParam("from").orElse("");
        String toVersion = request.queryParam("to").orElse("");

        LoggingUtil.info(logger, "Processing request to get migration guide from {} to {}", fromVersion, toVersion);

        if (fromVersion.isEmpty() || toVersion.isEmpty()) {
            // 参数校验统一错误响应
            return badRequest(request, "Both 'from' and 'to' version parameters are required", null);
        }

        return apiVersionService.getVersionMigrationGuide(fromVersion, toVersion)
                .map(migrationGuide -> ApiResponse.success(migrationGuide, "Version migration guide retrieved successfully"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "Successfully returned migration guide from {} to {}", fromVersion, toVersion))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Failed to get migration guide from " + fromVersion + " to " + toVersion, error);
                    // 统一错误响应
                    return badRequest(request, "Failed to get migration guide: " + error.getMessage(), error);
                });
    }

    /**
     * 检查版本兼容性
     *
     * @param request HTTP请求对象，包含客户端版本和服务器版本参数
     * @return 包含兼容性检查结果的响应
     */
    public Mono<ServerResponse> checkVersionCompatibility(ServerRequest request) {
        String clientVersion = request.queryParam("client").orElse("");
        String serverVersion = request.queryParam("server").orElse("");

        LoggingUtil.info(logger, "Processing request to check compatibility between client {} and server {}", clientVersion, serverVersion);

        if (clientVersion.isEmpty() || serverVersion.isEmpty()) {
            // 参数校验统一错误响应
            return badRequest(request, "Both 'client' and 'server' version parameters are required", null);
        }

        return apiVersionService.checkVersionCompatibility(clientVersion, serverVersion)
                .map(compatible -> ApiResponse.success(compatible, "Version compatibility checked successfully"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "Successfully checked compatibility between {} and {}", clientVersion, serverVersion))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Failed to check version compatibility", error);
                    // 统一错误响应
                    return badRequest(request, "Failed to check compatibility: " + error.getMessage(), error);
                });
    }

    /**
     * 获取版本使用统计
     *
     * @param request HTTP请求对象
     * @return 包含版本使用统计的响应
     */
    public Mono<ServerResponse> getVersionUsageStats(ServerRequest request) {
        LoggingUtil.info(logger, "Processing request to get version usage statistics");

        return apiVersionService.getVersionUsageStats()
                .collectList()
                .map(stats -> ApiResponse.success(stats, "Version usage statistics retrieved successfully"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "Successfully returned version usage statistics"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Failed to get version usage statistics", error);
                    // 统一错误响应
                    return badRequest(request, "Failed to get usage statistics: " + error.getMessage(), error);
                });
    }

    /**
     * 获取版本时间线
     *
     * @param request HTTP请求对象
     * @return 包含版本时间线的响应
     */
    public Mono<ServerResponse> getVersionTimeline(ServerRequest request) {
        LoggingUtil.info(logger, "Processing request to get version timeline");

        return apiVersionService.getVersionTimeline()
                .collectList()
                .map(timeline -> ApiResponse.success(timeline, "Version timeline retrieved successfully"))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "Successfully returned version timeline"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Failed to get version timeline", error);
                    // 统一错误响应
                    return badRequest(request, "Failed to get timeline: " + error.getMessage(), error);
                });
    }

    /**
     * 弃用指定版本
     *
     * @param request HTTP请求对象，包含版本参数和弃用信息
     * @return 包含弃用操作结果的响应
     */
    public Mono<ServerResponse> deprecateVersion(ServerRequest request) {
        String version = request.pathVariable("version");

        return request.bodyToMono(DeprecationRequest.class)
                .flatMap(deprecationRequest -> {
                    LoggingUtil.info(logger, "Processing request to deprecate version: {}", version);

                    return apiVersionService.deprecateVersion(
                            version,
                            deprecationRequest.getMessage(),
                            deprecationRequest.getMigrationDeadline()
                    );
                })
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> LoggingUtil.info(logger, "Successfully deprecated version: {}", version))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "Failed to deprecate version: " + version, error);
                    // 统一错误响应
                    return badRequest(request, "Failed to deprecate version: " + error.getMessage(), error);
                });
    }

    // 统一构建 400 错误响应（包含traceId与details）
    private Mono<ServerResponse> badRequest(ServerRequest request, String message, Throwable ex) {
        String path = request.path();
        String headerTraceId = request.headers().firstHeader(TraceIdUtil.X_TRACE_ID_HEADER);
        String traceId = (headerTraceId != null && !headerTraceId.isBlank()) ? headerTraceId : TraceIdUtil.generateTraceId();
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        details.put("error", message);
        details.put("path", path);
        if (ex != null) {
            details.put("exception", ex.getClass().getSimpleName());
        }
        ApiResponse<Void> body = ApiResponse.error(String.valueOf(org.springframework.http.HttpStatus.BAD_REQUEST.value()), message, traceId, details, path);
        return ServerResponse.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .header(TraceIdUtil.X_TRACE_ID_HEADER, traceId)
                .bodyValue(body);
    }

    /**
     * 弃用请求DTO
     */
    public static class DeprecationRequest {
        private String message;
        private String migrationDeadline;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMigrationDeadline() {
            return migrationDeadline;
        }

        public void setMigrationDeadline(String migrationDeadline) {
            this.migrationDeadline = migrationDeadline;
        }
    }
}
