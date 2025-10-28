package com.honyrun.handler;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.honyrun.model.dto.image.ImageConversionRequest;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.service.reactive.ReactiveImageService;
import com.honyrun.util.ErrorDetailsUtil;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.image.ImageValidationUtil;
import com.honyrun.util.validation.ReactiveValidator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式图片转换处理器
 *
 * 基于Spring WebFlux的函数式编程模型，提供图片处理和转换的非阻塞处理功能。
 * 支持多格式图片转换、压缩、尺寸调整、水印添加等功能。
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 *
 *          Copyright © 2025 HonyRun. All rights reserved.
 *          Created: 2025-07-01 16:35:00
 *          Modified: 2025-07-02 优化Bean命名规范
 */
@Component("reactiveImageHandler")
public class ImageHandler {

    private static final Logger logger = LoggerFactory.getLogger(ImageHandler.class);
    private final ReactiveImageService imageService;
    @SuppressWarnings("unused")
    private final ReactiveValidator reactiveValidator;
    private final ErrorDetailsUtil errorDetailsUtil;

    public ImageHandler(ReactiveImageService imageService,
            ReactiveValidator reactiveValidator,
            ErrorDetailsUtil errorDetailsUtil) {
        this.imageService = imageService;
        this.reactiveValidator = reactiveValidator;
        this.errorDetailsUtil = errorDetailsUtil;
    }

    /**
     * 图片转换 - JSON格式
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含转换后的图片数据
     */
    public Mono<ServerResponse> convertImageJson(ServerRequest request) {
        LoggingUtil.info(logger, "开始JSON格式图片转换处理");

        return request.bodyToMono(Map.class)
                .flatMap(requestBody -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bodyMap = (Map<String, Object>) requestBody;
                    // 从请求体获取图像数据和参数
                    String imageData = (String) bodyMap.get("imageData");
                    String targetFormat = (String) bodyMap.get("targetFormat");

                    // 验证必需参数
                    if (imageData == null || imageData.trim().isEmpty()) {
                        return Mono.deferContextual(ctxView -> {
                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                            String path = request.path();
                            Map<String, Object> details = new HashMap<>();
                            details.put("error", "图像数据不能为空");
                            details.put("path", path);
                            java.util.List<java.util.Map<String, Object>> violations = new java.util.ArrayList<>();
                            violations.add(java.util.Map.of(
                                    "field", "imageData",
                                    "message", "不能为空"));
                            details.put("violations", violations);
                            return ServerResponse.badRequest()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.error("400", "输入验证失败", traceId, details, path));
                        });
                    }

                    if (targetFormat == null || targetFormat.trim().isEmpty()) {
                        targetFormat = "jpg"; // 默认格式
                    }

                    try {
                        // 解析base64图片数据
                        String base64Data = imageData;
                        if (imageData.contains(",")) {
                            base64Data = imageData.split(",")[1];
                        }

                        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

                        // 模拟图片转换处理
                        String convertedBase64 = Base64.getEncoder().encodeToString(imageBytes);

                        LoggingUtil.info(logger, "JSON格式图片转换完成，目标格式: {}", targetFormat);

                        Map<String, Object> conversionResult = new HashMap<>();
                        conversionResult.put("convertedImage",
                                "data:image/" + targetFormat + ";base64," + convertedBase64);
                        conversionResult.put("format", targetFormat);

                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.success(conversionResult, "图片转换成功"));
                    } catch (Exception e) {
                        LoggingUtil.error(logger, "JSON格式图片转换失败", e);
                        return Mono.deferContextual(ctxView -> {
                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                            String path = request.path();
                            Map<String, Object> details = new HashMap<>();
                            details.put("error", e.getMessage());
                            details.put("path", path);
                            return ServerResponse.status(500)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.error("500", "图片转换失败: " + e.getMessage(), traceId, details,
                                            path));
                        });
                    }
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "JSON格式图片转换失败", error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = new HashMap<>();
                        details.put("error", error.getMessage());
                        details.put("path", path);
                        return ServerResponse.status(500)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("500", "图片转换失败: " + error.getMessage(), traceId, details,
                                        path));
                    });
                });
    }

    /**
     * 图片转换 - Multipart格式
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含转换后的图片数据
     */
    public Mono<ServerResponse> convertImage(ServerRequest request) {
        LoggingUtil.info(logger, "开始单个图片转换处理");

        return request.multipartData()
                .flatMap(multipartData -> {
                    FilePart filePart = (FilePart) multipartData.getFirst("file");
                    if (filePart == null) {
                        LoggingUtil.warn(logger, "未找到上传的图片文件");
                        return Mono.error(new IllegalArgumentException("未找到上传的图片文件"));
                    }

                    String filename = filePart.filename();

                    // 首先进行文件名安全检查
                    if (!ImageValidationUtil.isSafeFilename(filename)) {
                        return Mono.error(new IllegalArgumentException("文件名不安全或包含危险字符"));
                    }

                    // 获取转换参数 - 从multipart form data中获取
                    String targetFormat = null;
                    Integer quality = null;

                    // 从multipart data中获取参数
                    var targetFormatPart = multipartData.getFirst("targetFormat");
                    if (targetFormatPart != null) {
                        targetFormat = ((FormFieldPart) targetFormatPart).value();
                    }
                    if (targetFormat == null) {
                        targetFormat = request.queryParam("format").orElse("PNG");
                    }

                    var qualityPart = multipartData.getFirst("quality");
                    if (qualityPart != null) {
                        try {
                            quality = Integer.parseInt(((FormFieldPart) qualityPart).value());
                        } catch (NumberFormatException e) {
                            quality = 90;
                        }
                    }
                    if (quality == null) {
                        quality = request.queryParam("quality").map(Integer::parseInt).orElse(90);
                    }

                    LoggingUtil.info(logger, "图片转换参数 - 目标格式: {}, 质量: {}", targetFormat, quality);

                    final String finalTargetFormat = targetFormat;
                    final String finalFilename = filename;

                    return imageService.convertImage(filePart, targetFormat, quality.floatValue(), null, null)
                            .flatMap(result -> {
                                LoggingUtil.info(logger, "图片转换成功");

                                // 获取转换后的图片信息
                                Map<String, Object> responseData = new HashMap<>();
                                responseData.put("format", finalTargetFormat.toUpperCase());
                                responseData.put("size", result.readableByteCount());
                                responseData.put("filename", finalFilename);

                                return ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(ApiResponse.success(responseData, "图片转换成功"));
                            });
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "图片转换失败", error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = new HashMap<>();
                        details.put("error", error.getMessage());
                        details.put("path", path);
                        return ServerResponse.status(400)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("400", "图片转换失败: " + error.getMessage(), traceId, details,
                                        path));
                    });
                });
    }

    /**
     * 图片验证 - JSON格式
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含验证结果
     */
    public Mono<ServerResponse> validateImageJson(ServerRequest request) {
        LoggingUtil.info(logger, "开始JSON格式图片验证处理");

        return request.bodyToMono(Map.class)
                .flatMap(requestBody -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bodyMap = (Map<String, Object>) requestBody;
                    // 从请求体获取图像数据和文件名
                    String imageData = (String) bodyMap.get("imageData");
                    String filename = (String) bodyMap.get("filename");

                    // 验证必需参数
                    if (imageData == null || imageData.trim().isEmpty()) {
                        return Mono.deferContextual(ctxView -> {
                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                            String path = request.path();
                            Map<String, Object> details = new HashMap<>();
                            details.put("error", "图像数据不能为空");
                            details.put("path", path);
                            java.util.List<java.util.Map<String, Object>> violations = new java.util.ArrayList<>();
                            violations.add(java.util.Map.of(
                                    "field", "imageData",
                                    "message", "不能为空"));
                            details.put("violations", violations);
                            return ServerResponse.badRequest()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.error("400", "输入验证失败", traceId, details, path));
                        });
                    }

                    try {
                        // 解析base64图片数据
                        String base64Data = imageData;
                        if (imageData.contains(",")) {
                            base64Data = imageData.split(",")[1];
                        }

                        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

                        // 验证文件大小（10MB限制）
                        if (!ImageValidationUtil.isValidFileSize(imageBytes.length, null)) {
                            return Mono.deferContextual(ctxView -> {
                                String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                                String path = request.path();
                                Map<String, Object> details = new HashMap<>();
                                details.put("error", "文件大小超出限制");
                                details.put("path", path);
                                java.util.List<java.util.Map<String, Object>> violations = new java.util.ArrayList<>();
                                violations.add(java.util.Map.of(
                                        "field", "imageData",
                                        "message", "文件大小超出限制"));
                                details.put("violations", violations);
                                return ServerResponse.badRequest()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(ApiResponse.error("400", "文件大小超出限制", traceId, details, path));
                            });
                        }

                        // 验证文件名安全性（如果提供了文件名）
                        if (filename != null && !ImageValidationUtil.isSafeFilename(filename)) {
                            return Mono.deferContextual(ctxView -> {
                                String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                                String path = request.path();
                                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(
                                        new IllegalArgumentException("文件名不安全或包含危险字符"), path);
                                java.util.List<java.util.Map<String, Object>> violations = new java.util.ArrayList<>();
                                violations.add(java.util.Map.of(
                                        "field", "filename",
                                        "message", "不安全或包含危险字符"));
                                details.put("violations", violations);
                                return ServerResponse.badRequest()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(ApiResponse.error("400", "文件名不安全或包含危险字符", traceId, details, path));
                            });
                        }

                        // 验证图片格式
                        boolean isValid = ImageValidationUtil.isValidImageContent(imageBytes);

                        LoggingUtil.info(logger, "JSON格式图片验证结果: {}", isValid ? "有效" : "无效");

                        if (isValid) {
                            return ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.success(isValid, "图片格式验证通过"));
                        } else {
                            return Mono.deferContextual(ctxView -> {
                                String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                                String path = request.path();
                                Map<String, Object> details = errorDetailsUtil.buildErrorDetails(
                                        new IllegalArgumentException("图片格式不支持"), path);
                                java.util.List<java.util.Map<String, Object>> violations = new java.util.ArrayList<>();
                                violations.add(java.util.Map.of(
                                        "field", "imageData",
                                        "message", "格式不支持"));
                                details.put("violations", violations);
                                return ServerResponse.status(400)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(ApiResponse.error("400", "图片格式不支持", traceId, details, path));
                            });
                        }
                    } catch (Exception e) {
                        LoggingUtil.error(logger, "JSON格式图片验证失败", e);
                        return Mono.deferContextual(ctxView -> {
                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                            String path = request.path();
                            Map<String, Object> details = errorDetailsUtil.buildErrorDetails(e, path);
                            return ServerResponse.status(400)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.error("400", "图片验证失败: " + e.getMessage(), traceId, details,
                                            path));
                        });
                    }
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "JSON格式图片验证失败", error);

                    // 如果是请求体读取失败（如文件过大），返回400状态码
                    if (error instanceof org.springframework.web.server.ServerWebInputException ||
                            error.getCause() instanceof org.springframework.core.io.buffer.DataBufferLimitException) {
                        return Mono.deferContextual(ctxView -> {
                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                            String path = request.path();
                            Map<String, Object> details = errorDetailsUtil.buildErrorDetails(error, path);
                            return ServerResponse.status(400)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.error("400", "文件大小超出限制", traceId, details, path));
                        });
                    }

                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = errorDetailsUtil.buildErrorDetails(error, path);
                        return ServerResponse.status(500)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("500", "图片验证失败: " + error.getMessage(), traceId, details,
                                        path));
                    });
                });
    }

    /**
     * 图片验证 - Multipart格式
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含验证结果
     */
    public Mono<ServerResponse> validateImage(ServerRequest request) {
        LoggingUtil.info(logger, "开始图片验证处理");

        return request.multipartData()
                .flatMap(multipartData -> {
                    FilePart filePart = (FilePart) multipartData.getFirst("file");
                    if (filePart == null) {
                        LoggingUtil.warn(logger, "未找到上传的图片文件");
                        return Mono.error(new IllegalArgumentException("未找到上传的图片文件"));
                    }

                    LoggingUtil.info(logger, "验证图片: {}", filePart.filename());

                    String filename = filePart.filename();

                    // 首先进行文件名安全检查
                    if (!ImageValidationUtil.isSafeFilename(filename)) {
                        return Mono.error(new IllegalArgumentException("文件名不安全或包含危险字符"));
                    }

                    // 然后进行格式验证
                    return imageService.validateImageFormat(filePart)
                            .map(isValid -> {
                                // 创建包含验证结果和文件名的响应对象
                                Map<String, Object> validationResult = new HashMap<>();
                                validationResult.put("valid", isValid);
                                validationResult.put("filename", filename);
                                return validationResult;
                            });
                })
                .flatMap(validationResult -> {
                    boolean isValid = (Boolean) validationResult.get("valid");
                    LoggingUtil.info(logger, "图片验证结果: {}", isValid ? "有效" : "无效");
                    if (isValid) {
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.success(validationResult, "图片格式验证通过"));
                    } else {
                        return Mono.deferContextual(ctxView -> {
                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                            String path = request.path();
                            Map<String, Object> details = errorDetailsUtil.buildErrorDetails(
                                    new IllegalArgumentException("图片格式不支持"), path);
                            java.util.List<java.util.Map<String, Object>> violations = new java.util.ArrayList<>();
                            violations.add(java.util.Map.of(
                                    "field", "file",
                                    "message", "格式不支持"));
                            details.put("violations", violations);
                            return ServerResponse.status(400)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.error("400", "图片格式不支持", traceId, details, path));
                        });
                    }
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "图片验证失败", error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = errorDetailsUtil.buildErrorDetails(error, path);
                        return ServerResponse.status(400)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("400", "图片验证失败: " + error.getMessage(), traceId, details,
                                        path));
                    });
                });
    }

    /**
     * 批量图片转换
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含批量转换结果
     */
    public Mono<ServerResponse> batchConvertImages(ServerRequest request) {
        LoggingUtil.info(logger, "开始批量图片转换处理");

        return request.multipartData()
                .flatMap(multipartData -> {
                    var fileParts = multipartData.get("files");
                    if (fileParts == null || fileParts.isEmpty()) {
                        LoggingUtil.warn(logger, "未找到上传的图片文件");
                        return Mono.error(new IllegalArgumentException("未找到上传的图片文件"));
                    }

                    // 获取转换参数
                    String targetFormat = request.queryParam("format").orElse("PNG");
                    Integer quality = request.queryParam("quality").map(Integer::parseInt).orElse(90);

                    LoggingUtil.info(logger, "批量图片转换参数 - 目标格式: {}, 质量: {}, 文件数量: {}", targetFormat, quality,
                            fileParts.size());

                    // 将List<Part>转换为Flux<FilePart>
                    Flux<FilePart> filePartsFlux = Flux.fromIterable(fileParts)
                            .cast(FilePart.class);

                    Flux<DataBuffer> dataBufferFlux = imageService.convertImagesBatch(filePartsFlux, targetFormat,
                            quality.floatValue(), null, null);

                    LoggingUtil.info(logger, "批量图片转换完成");
                    return ServerResponse.ok()
                            .body(dataBufferFlux, DataBuffer.class);
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "批量图片转换失败", error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = errorDetailsUtil.buildErrorDetails(error, path);
                        return ServerResponse.status(400)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("400", "批量图片转换失败: " + error.getMessage(), traceId, details,
                                        path));
                    });
                });
    }

    /**
     * 图片压缩
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含压缩结果
     */
    public Mono<ServerResponse> compressImage(ServerRequest request) {
        LoggingUtil.info(logger, "开始图片压缩处理");

        return request.multipartData()
                .flatMap(multipartData -> {
                    FilePart filePart = (FilePart) multipartData.getFirst("file");
                    if (filePart == null) {
                        LoggingUtil.warn(logger, "未找到上传的图片文件");
                        return Mono.error(new IllegalArgumentException("未找到上传的图片文件"));
                    }

                    // 获取压缩参数
                    Integer quality = request.queryParam("quality").map(Integer::parseInt).orElse(80);
                    Integer maxWidth = request.queryParam("maxWidth").map(Integer::parseInt).orElse(null);
                    Integer maxHeight = request.queryParam("maxHeight").map(Integer::parseInt).orElse(null);

                    LoggingUtil.info(logger, "图片压缩参数 - 质量: {}, 最大宽度: {}, 最大高度: {}", quality, maxWidth, maxHeight);

                    return imageService.compressImage(filePart, quality.floatValue());
                })
                .flatMap(result -> {
                    LoggingUtil.info(logger, "图片压缩成功");
                    return ServerResponse.ok()
                            .body(Mono.just(result), DataBuffer.class);
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "图片压缩失败", error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = errorDetailsUtil.buildErrorDetails(error, path);
                        return ServerResponse.status(400)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("400", "图片压缩失败: " + error.getMessage(), traceId, details,
                                        path));
                    });
                });
    }

    /**
     * 图片尺寸调整
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含调整结果
     */
    public Mono<ServerResponse> resizeImage(ServerRequest request) {
        LoggingUtil.info(logger, "开始图片尺寸调整处理");

        return request.multipartData()
                .flatMap(multipartData -> {
                    FilePart filePart = (FilePart) multipartData.getFirst("file");
                    if (filePart == null) {
                        LoggingUtil.warn(logger, "未找到上传的图片文件");
                        return Mono.error(new IllegalArgumentException("未找到上传的图片文件"));
                    }

                    // 获取尺寸参数
                    Integer width = request.queryParam("width").map(Integer::parseInt).orElse(null);
                    Integer height = request.queryParam("height").map(Integer::parseInt).orElse(null);
                    Boolean keepAspectRatio = request.queryParam("keepAspectRatio").map(Boolean::parseBoolean)
                            .orElse(true);

                    if (width == null && height == null) {
                        LoggingUtil.warn(logger, "未指定目标宽度或高度");
                        return Mono.error(new IllegalArgumentException("必须指定目标宽度或高度"));
                    }

                    LoggingUtil.info(logger, "图片尺寸调整参数 - 宽度: {}, 高度: {}, 保持比例: {}", width, height, keepAspectRatio);

                    return imageService.resizeImage(filePart, width, height, keepAspectRatio);
                })
                .flatMap(result -> {
                    LoggingUtil.info(logger, "图片尺寸调整成功");
                    return ServerResponse.ok()
                            .body(Mono.just(result), DataBuffer.class);
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "图片尺寸调整失败", error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = errorDetailsUtil.buildErrorDetails(error, path);
                        return ServerResponse.status(400)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("400", "图片尺寸调整失败: " + error.getMessage(), traceId, details,
                                        path));
                    });
                });
    }

    /**
     * 添加水印
     *
     * @param request 服务器请求对象
     * @return 响应式服务器响应，包含水印添加结果
     */
    public Mono<ServerResponse> addWatermark(ServerRequest request) {
        LoggingUtil.info(logger, "开始图片水印添加处理");

        return request.multipartData()
                .flatMap(multipartData -> {
                    FilePart filePart = (FilePart) multipartData.getFirst("file");
                    if (filePart == null) {
                        LoggingUtil.warn(logger, "未找到上传的图片文件");
                        return Mono.error(new IllegalArgumentException("未找到上传的图片文件"));
                    }

                    // 获取水印参数
                    String watermarkText = request.queryParam("text").orElse("");
                    String position = request.queryParam("position").orElse("BOTTOM_RIGHT");
                    Float opacity = request.queryParam("opacity").map(Float::parseFloat).orElse(0.5f);

                    LoggingUtil.info(logger, "图片水印参数 - 文字: {}, 位置: {}, 透明度: {}", watermarkText, position, opacity);

                    ImageConversionRequest watermarkRequest = new ImageConversionRequest();
                    watermarkRequest.setWatermarkText(watermarkText);
                    watermarkRequest.setWatermarkAlpha(opacity);

                    return imageService.addWatermark(filePart, watermarkRequest);
                })
                .flatMap(result -> {
                    LoggingUtil.info(logger, "图片水印添加成功");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.success(result, "图片水印添加成功"));
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "图片水印添加失败", error);
                    return Mono.deferContextual(ctxView -> {
                        String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String path = request.path();
                        Map<String, Object> details = errorDetailsUtil.buildErrorDetails(error, path);
                        return ServerResponse.status(400)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("400", "图片水印添加失败: " + error.getMessage(), traceId, details,
                                        path));
                    });
                });
    }
}
