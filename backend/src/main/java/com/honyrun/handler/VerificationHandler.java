package com.honyrun.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.honyrun.service.reactive.ReactiveVerificationService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.validation.ReactiveValidator;

import reactor.core.publisher.Mono;

/**
 * 响应式核验业务处理器
 *
 * 基于Spring WebFlux的函数式编程模型，提供核验业务功能的非阻塞处理。
 * 包括核验请求管理、核验结果查询、核验统计等功能。
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 *
 *          Copyright © 2025 HonyRun. All rights reserved.
 *          Created: 2025-07-01 16:40:00
 *          Modified: 2025-07-02 优化Bean命名规范
 */
@Component("reactiveVerificationHandler")
public class VerificationHandler {

    private static final Logger logger = LoggerFactory.getLogger(VerificationHandler.class);
    private final ReactiveVerificationService verificationService;
    private final ReactiveValidator reactiveValidator;

    public VerificationHandler(ReactiveVerificationService verificationService,
            ReactiveValidator reactiveValidator) {
        this.verificationService = verificationService;
        this.reactiveValidator = reactiveValidator;
    }

    /**
     * 查询核验业务数据
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含核验数据列表
     */
    public Mono<ServerResponse> getVerifications(ServerRequest request) {
        LoggingUtil.info(logger, "开始查询核验业务数据");

        return verificationService.getAllVerificationRequests(0, 100)
                .collectList()
                .flatMap(verifications -> {
                    LoggingUtil.info(logger, "成功查询到 {} 条核验数据", verifications.size());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(com.honyrun.model.dto.response.ApiResponse.success(verifications));
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "查询核验数据失败", error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = new java.util.HashMap<>();
                        details.put("exception", error.getClass().getSimpleName());
                        details.put("message", error.getMessage());
                        details.put("path", path);
                        details.put("timestamp", java.time.LocalDateTime.now());
                        return ServerResponse.status(500)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("500",
                                        "查询核验数据失败: " + error.getMessage(), traceId, details, path));
                    });
                });
    }

    /**
     * 创建核验请求
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含创建结果
     */
    public Mono<ServerResponse> createVerification(ServerRequest request) {
        LoggingUtil.info(logger, "开始创建核验请求");

        final String path = request.path();
        return reactiveValidator
                .validate(request.bodyToMono(com.honyrun.model.dto.request.VerificationCreateRequest.class))
                .flatMap(result -> {
                    if (!result.isValid()) {
                        return reactor.core.publisher.Mono.deferContextual(ctxView -> {
                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                            java.util.Map<String, Object> details = new java.util.HashMap<>();
                            java.util.List<java.util.Map<String, Object>> violations = result.getViolations() == null
                                    ? java.util.List.of()
                                    : result.getViolations().stream()
                                            .map(v -> java.util.Map.<String, Object>of(
                                                    "field",
                                                    v.getPropertyPath() != null ? v.getPropertyPath().toString() : "",
                                                    "message", v.getMessage()))
                                            .collect(java.util.stream.Collectors.toList());
                            details.put("violations", violations);
                            LoggingUtil.warn(logger, "核验创建输入验证失败: violations={}", violations);
                            return ServerResponse.badRequest()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("400", "输入验证失败",
                                            traceId, details, path));
                        });
                    }
                    LoggingUtil.info(logger, "接收到核验请求: {}", result.getObject().toString());
                    return Mono.just(result.getObject())
                            .map(this::convertToEntity)
                            .flatMap(verificationService::createVerificationRequest)
                            .flatMap(response -> {
                                LoggingUtil.info(logger, "核验请求创建成功，ID: {}", response.getId());
                                java.net.URI location = java.net.URI.create(path + "/" + response.getId());
                                return ServerResponse.created(location)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(com.honyrun.model.dto.response.ApiResponse.success(response));
                            });
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "创建核验请求失败", error);
                    return ServerResponse.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("400",
                                    "创建核验请求失败: " + error.getMessage()));
                });
    }

    /**
     * 更新核验请求
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含更新结果
     */
    public Mono<ServerResponse> updateVerification(ServerRequest request) {
        String idStr;
        try {
            idStr = request.pathVariable("id");
        } catch (IllegalArgumentException ex) {
            LoggingUtil.warn(logger, "更新核验请求失败，缺少ID路径参数");
            return Mono.deferContextual(ctxView -> {
                String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                String path = request.path();
                Map<String, Object> details = new HashMap<>();
                details.put("error", "缺少请求ID");
                return ServerResponse.status(400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("400", "缺少请求ID", traceId, details,
                                path));
            });
        }

        Long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException ex) {
            LoggingUtil.warn(logger, "更新核验请求失败，ID格式无效: {}", idStr);
            return Mono.deferContextual(ctxView -> {
                String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                String path = request.path();
                Map<String, Object> details = new HashMap<>();
                details.put("invalidId", idStr);
                return ServerResponse.status(400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("400", "无效的请求ID格式", traceId,
                                details, path));
            });
        }

        LoggingUtil.info(logger, "开始更新核验请求，ID: {}", idStr);

        final String path = request.path();
        return reactiveValidator.validate(request.bodyToMono(com.honyrun.model.dto.request.VerificationRequest.class))
                .flatMap(result -> {
                    if (!result.isValid()) {
                        return reactor.core.publisher.Mono.deferContextual(ctxView -> {
                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                            java.util.Map<String, Object> details = new java.util.HashMap<>();
                            java.util.List<java.util.Map<String, Object>> violations = result.getViolations() == null
                                    ? java.util.List.of()
                                    : result.getViolations().stream()
                                            .map(v -> java.util.Map.<String, Object>of(
                                                    "field",
                                                    v.getPropertyPath() != null ? v.getPropertyPath().toString() : "",
                                                    "message", v.getMessage()))
                                            .collect(java.util.stream.Collectors.toList());
                            details.put("violations", violations);
                            LoggingUtil.warn(logger, "核验更新输入验证失败: violations={}", violations);
                            return ServerResponse.badRequest()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("400", "输入验证失败",
                                            traceId, details, path));
                        });
                    }
                    LoggingUtil.info(logger, "接收到更新请求: {}", result.getObject().toString());
                    return Mono.just(result.getObject())
                            .map(this::convertToEntity)
                            .flatMap(verificationRequest -> verificationService.updateVerificationRequest(id,
                                    verificationRequest))
                            .flatMap(response -> {
                                LoggingUtil.info(logger, "核验请求更新成功，ID: {}", idStr);
                                return ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(com.honyrun.model.dto.response.ApiResponse.success(response));
                            });
                })
                .onErrorResume(error -> {
                    if (error instanceof com.honyrun.exception.BusinessException) {
                        LoggingUtil.info(logger, "核验请求不存在，ID: {}", idStr);
                        return Mono.deferContextual(ctxView -> {
                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                            Map<String, Object> details = new HashMap<>();
                            details.put("id", idStr);
                            return ServerResponse.status(404)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("404", "核验请求不存在",
                                            traceId, details, path));
                        });
                    }
                    LoggingUtil.error(logger, "更新核验数据失败", error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        Map<String, Object> details = new java.util.HashMap<>();
                        details.put("exception", error.getClass().getSimpleName());
                        details.put("message", error.getMessage());
                        details.put("path", path);
                        details.put("timestamp", java.time.LocalDateTime.now());
                        return ServerResponse.status(500)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("500",
                                        "更新核验数据失败: " + error.getMessage(), traceId, details, path));
                    });
                });
    }

    /**
     * 删除核验请求
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含删除结果
     */
    public Mono<ServerResponse> deleteVerification(ServerRequest request) {
        String idStr;
        try {
            idStr = request.pathVariable("id");
        } catch (IllegalArgumentException ex) {
            LoggingUtil.warn(logger, "删除核验请求失败，缺少ID路径参数");
            return Mono.deferContextual(ctxView -> {
                String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                String path = request.path();
                Map<String, Object> details = new HashMap<>();
                details.put("error", "缺少请求ID");
                return ServerResponse.status(400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("400", "缺少请求ID", traceId, details,
                                path));
            });
        }

        Long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException ex) {
            LoggingUtil.warn(logger, "删除核验请求失败，ID格式无效: {}", idStr);
            return Mono.deferContextual(ctxView -> {
                String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                String path = request.path();
                Map<String, Object> details = new HashMap<>();
                details.put("invalidId", idStr);
                return ServerResponse.status(400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("400", "无效的请求ID格式", traceId,
                                details, path));
            });
        }

        LoggingUtil.info(logger, "开始删除核验请求，ID: {}", idStr);

        return verificationService.deleteVerificationRequest(id)
                .then(Mono.fromRunnable(() -> LoggingUtil.info(logger, "核验请求删除成功，ID: {}", idStr)))
                .then(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(com.honyrun.model.dto.response.ApiResponse.success(null, "核验请求删除成功")))
                .onErrorResume(error -> {
                    if (error instanceof com.honyrun.exception.BusinessException) {
                        LoggingUtil.info(logger, "核验请求不存在，ID: {}", idStr);
                        return Mono.deferContextual(ctxView -> {
                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                            String path = request.path();
                            Map<String, Object> details = new HashMap<>();
                            details.put("id", idStr);
                            return ServerResponse.status(404)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("404", "核验请求不存在",
                                            traceId, details, path));
                        });
                    }
                    LoggingUtil.error(logger, "删除核验数据失败", error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = new java.util.HashMap<>();
                        details.put("exception", error.getClass().getSimpleName());
                        details.put("message", error.getMessage());
                        details.put("path", path);
                        details.put("timestamp", java.time.LocalDateTime.now());
                        return ServerResponse.status(500)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("500",
                                        "删除核验数据失败: " + error.getMessage(), traceId, details, path));
                    });
                });
    }

    /**
     * 获取核验结果
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含核验结果
     */
    public Mono<ServerResponse> getVerificationResult(ServerRequest request) {
        String idStr;
        try {
            idStr = request.pathVariable("id");
        } catch (IllegalArgumentException ex) {
            // 路径中缺少ID参数 -> 400
            LoggingUtil.warn(logger, "获取核验结果失败，缺少ID路径参数");
            return Mono.deferContextual(ctxView -> {
                String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                String path = request.path();
                Map<String, Object> details = new HashMap<>();
                details.put("error", "缺少请求ID");
                return ServerResponse.status(400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("400", "缺少请求ID", traceId, details,
                                path));
            });
        }

        LoggingUtil.info(logger, "开始获取核验结果，ID: {}", idStr);

        Long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException ex) {
            // 非数字ID -> 400
            LoggingUtil.warn(logger, "获取核验结果失败，ID格式无效: {}", idStr);
            return Mono.deferContextual(ctxView -> {
                String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                String path = request.path();
                Map<String, Object> details = new HashMap<>();
                details.put("invalidId", idStr);
                return ServerResponse.status(400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("400", "无效的请求ID格式", traceId,
                                details, path));
            });
        }

        return verificationService.getVerificationResultById(id)
                .flatMap(result -> {
                    LoggingUtil.info(logger, "成功获取核验结果，ID: {}", idStr);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(com.honyrun.model.dto.response.ApiResponse.success(result));
                })
                .switchIfEmpty(Mono.deferContextual(ctxView -> {
                    String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                    String path = request.path();
                    Map<String, Object> details = new HashMap<>();
                    details.put("id", idStr);
                    return ServerResponse.status(404)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("404", "核验结果不存在", traceId,
                                    details, path));
                }))
                .onErrorResume(error -> {
                    if (error instanceof com.honyrun.exception.BusinessException) {
                        LoggingUtil.info(logger, "核验结果不存在，ID: {}", idStr);
                        return Mono.deferContextual(ctxView -> {
                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                            String path = request.path();
                            Map<String, Object> details = new HashMap<>();
                            details.put("id", idStr);
                            return ServerResponse.status(404)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("404", "核验结果不存在",
                                            traceId, details, path));
                        });
                    }
                    LoggingUtil.error(logger, "获取核验结果失败，ID: {}", idStr, error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = new HashMap<>();
                        details.put("error", error.getMessage());
                        details.put("path", path);
                        return ServerResponse.status(500)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("500",
                                        "获取核验结果失败: " + error.getMessage(), traceId, details, path));
                    });
                });
    }

    /**
     * 获取核验统计信息
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含统计信息
     */
    public Mono<ServerResponse> getVerificationStats(ServerRequest request) {
        LoggingUtil.info(logger, "开始获取核验统计信息");

        return Mono.zip(
                verificationService.countVerificationRequests(),
                verificationService.countPendingRequests(),
                verificationService.countOverdueRequests(),
                verificationService.countHighPriorityRequests()).map(tuple -> {
                    Map<String, Object> statistics = Map.<String, Object>of(
                            "totalRequests", tuple.getT1(),
                            "pendingRequests", tuple.getT2(),
                            "overdueRequests", tuple.getT3(),
                            "highPriorityRequests", tuple.getT4());
                    return statistics;
                })
                .flatMap(stats -> {
                    LoggingUtil.info(logger, "成功获取核验统计信息");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(com.honyrun.model.dto.response.ApiResponse.success(stats));
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取核验统计信息失败", error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = new HashMap<>();
                        details.put("error", error.getMessage());
                        details.put("path", path);
                        return ServerResponse.status(500)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("500",
                                        "获取核验统计信息失败: " + error.getMessage(), traceId, details, path));
                    });
                });
    }

    /**
     * 完成核验处理
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含处理结果
     */
    public Mono<ServerResponse> completeVerification(ServerRequest request) {
        String requestIdStr;
        try {
            requestIdStr = request.pathVariable("requestId");
        } catch (IllegalArgumentException ex) {
            LoggingUtil.warn(logger, "完成核验处理失败，缺少requestId路径参数");
            return Mono.deferContextual(ctxView -> {
                String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                String path = request.path();
                Map<String, Object> details = new HashMap<>();
                details.put("error", "缺少请求ID");
                return ServerResponse.status(400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("400", "缺少请求ID", traceId, details,
                                path));
            });
        }

        Long requestId;
        try {
            requestId = Long.parseLong(requestIdStr);
        } catch (NumberFormatException ex) {
            LoggingUtil.warn(logger, "完成核验处理失败，requestId格式无效: {}", requestIdStr);
            return Mono.deferContextual(ctxView -> {
                String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                String path = request.path();
                Map<String, Object> details = new HashMap<>();
                details.put("invalidId", requestIdStr);
                return ServerResponse.status(400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("400", "无效的请求ID格式", traceId,
                                details, path));
            });
        }

        String processResult = request.queryParam("processResult").orElse("");
        LoggingUtil.info(logger, "开始完成核验处理，请求ID: {}, 处理结果: {}", requestIdStr, processResult);

        return verificationService.completeProcessing(requestId, processResult)
                .flatMap(result -> {
                    LoggingUtil.info(logger, "核验处理完成，请求ID: {}", requestIdStr);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(com.honyrun.model.dto.response.ApiResponse.success(result));
                })
                .onErrorResume(error -> {
                    if (error instanceof com.honyrun.exception.BusinessException) {
                        LoggingUtil.info(logger, "核验请求不存在，请求ID: {}", requestIdStr);
                        return Mono.deferContextual(ctxView -> {
                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                            String path = request.path();
                            Map<String, Object> details = new HashMap<>();
                            details.put("requestId", requestIdStr);
                            return ServerResponse.status(404)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("404", "核验请求不存在",
                                            traceId, details, path));
                        });
                    }
                    LoggingUtil.error(logger, "完成核验处理失败，请求ID: {}", requestIdStr, error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = new HashMap<>();
                        details.put("error", error.getMessage());
                        details.put("path", path);
                        return ServerResponse.status(500)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("500",
                                        "完成核验处理失败: " + error.getMessage(), traceId, details, path));
                    });
                });
    }

    /**
     * 取消核验请求
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含取消结果
     */
    public Mono<ServerResponse> cancelVerification(ServerRequest request) {
        String requestIdStr;
        try {
            requestIdStr = request.pathVariable("requestId");
        } catch (IllegalArgumentException ex) {
            LoggingUtil.warn(logger, "取消核验请求失败，缺少requestId路径参数");
            return Mono.deferContextual(ctxView -> {
                String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                String path = request.path();
                Map<String, Object> details = new HashMap<>();
                details.put("error", "缺少请求ID");
                return ServerResponse.status(400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("400", "缺少请求ID", traceId, details,
                                path));
            });
        }

        Long requestId;
        try {
            requestId = Long.parseLong(requestIdStr);
        } catch (NumberFormatException ex) {
            LoggingUtil.warn(logger, "取消核验请求失败，requestId格式无效: {}", requestIdStr);
            return Mono.deferContextual(ctxView -> {
                String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                String path = request.path();
                Map<String, Object> details = new HashMap<>();
                details.put("invalidId", requestIdStr);
                return ServerResponse.status(400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("400", "无效的请求ID格式", traceId,
                                details, path));
            });
        }

        String reason = request.queryParam("reason").orElse("");
        LoggingUtil.info(logger, "开始取消核验请求，请求ID: {}, 取消原因: {}", requestIdStr, reason);

        return verificationService.cancelVerificationRequest(requestId, reason)
                .flatMap(result -> {
                    LoggingUtil.info(logger, "核验请求取消成功，请求ID: {}", requestIdStr);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(com.honyrun.model.dto.response.ApiResponse.success(result));
                })
                .onErrorResume(error -> {
                    if (error instanceof com.honyrun.exception.BusinessException) {
                        LoggingUtil.info(logger, "核验请求不存在，请求ID: {}", requestIdStr);
                        return Mono.deferContextual(ctxView -> {
                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                            String path = request.path();
                            Map<String, Object> details = new HashMap<>();
                            details.put("requestId", requestIdStr);
                            return ServerResponse.status(404)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("404", "核验请求不存在",
                                            traceId, details, path));
                        });
                    }
                    LoggingUtil.error(logger, "取消核验请求失败，请求ID: {}", requestIdStr, error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = new HashMap<>();
                        details.put("error", error.getMessage());
                        details.put("path", path);
                        return ServerResponse.status(500)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(com.honyrun.model.dto.response.ApiResponse.error("500",
                                        "取消核验请求失败: " + error.getMessage(), traceId, details, path));
                    });
                });
    }

    /**
     * 将DTO转换为实体对象
     *
     * @param dto 核验请求DTO
     * @return 核验请求实体
     */
    private com.honyrun.model.entity.business.VerificationRequest convertToEntity(
            com.honyrun.model.dto.request.VerificationCreateRequest dto) {
        com.honyrun.model.entity.business.VerificationRequest entity = new com.honyrun.model.entity.business.VerificationRequest();
        entity.setRequestType(dto.getRequestType());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setRequestParams(dto.getRequestParams());
        entity.setRequesterId(dto.getRequesterId());
        entity.setRequesterName(dto.getRequesterName());
        entity.setPriority(dto.getPriority());
        entity.setExpectedCompletionTime(dto.getExpectedCompletionTime());
        entity.setAttachments(dto.getAttachments());
        entity.setTags(dto.getTags());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    /**
     * 将更新DTO转换为实体对象
     *
     * @param dto 更新用的核验请求DTO
     * @return 核验请求实体
     */
    private com.honyrun.model.entity.business.VerificationRequest convertToEntity(
            com.honyrun.model.dto.request.VerificationRequest dto) {
        com.honyrun.model.entity.business.VerificationRequest entity = new com.honyrun.model.entity.business.VerificationRequest();
        entity.setRequestType(dto.getRequestType());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setRequestParams(dto.getRequestParams());
        entity.setRequesterId(dto.getRequesterId());
        entity.setRequesterName(dto.getRequesterName());
        entity.setPriority(dto.getPriority());
        entity.setExpectedCompletionTime(dto.getExpectedCompletionTime());
        entity.setAttachments(dto.getAttachments());
        entity.setTags(dto.getTags());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    /**
     * 根据ID获取核验请求
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应
     */
    public Mono<ServerResponse> getVerificationById(ServerRequest request) {
        String idStr;
        try {
            idStr = request.pathVariable("id");
        } catch (IllegalArgumentException ex) {
            LoggingUtil.warn(logger, "获取核验请求失败，缺少ID路径参数");
            return ServerResponse.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("缺少请求ID");
        }

        Long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException ex) {
            LoggingUtil.warn(logger, "获取核验请求失败，ID格式无效: {}", idStr);
            return ServerResponse.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("无效的请求ID格式");
        }

        LoggingUtil.info(logger, "开始获取核验请求，ID: {}", idStr);

        return verificationService.getVerificationRequestById(id)
                .flatMap(record -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(record))
                .switchIfEmpty(ServerResponse.status(404)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("核验请求不存在"))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取核验请求失败，ID: {}", idStr, error);
                    return ServerResponse.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("获取核验请求失败: " + error.getMessage());
                });
    }
}
