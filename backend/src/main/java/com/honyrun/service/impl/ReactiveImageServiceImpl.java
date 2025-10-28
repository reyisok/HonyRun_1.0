package com.honyrun.service.impl;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.exception.BusinessException;
import com.honyrun.exception.ErrorCode;
import com.honyrun.service.reactive.ReactiveImageService;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 响应式图片服务实现类
 * 提供图片转换、格式支持、批处理等功能的具体实现
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 *          Copyright © 2025 HonyRun. All rights reserved.
 *
 *          实现特性:
 *          - 基于Java AWT的图片处理
 *          - 响应式流处理，支持大文件
 *          - 异步处理和错误恢复
 *          - 支持多种图片格式转换
 *          - 内置图片质量和尺寸调整
 */
@Service
public class ReactiveImageServiceImpl implements ReactiveImageService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveImageServiceImpl.class);

    private final DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
    private final UnifiedConfigManager unifiedConfigManager;

    // 支持的图片格式
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList(
            "jpeg", "jpg", "png", "gif", "bmp", "webp");

    /**
     * 构造函数注入
     *
     * @param unifiedConfigManager 统一配置管理器
     */
    public ReactiveImageServiceImpl(UnifiedConfigManager unifiedConfigManager) {
        this.unifiedConfigManager = unifiedConfigManager;
    }

    @Override
    public Mono<DataBuffer> convertImage(FilePart filePart, String targetFormat, Float quality, Integer width,
            Integer height) {
        LoggingUtil.info(logger, "开始转换图片: {} -> {}", filePart.filename(), targetFormat);

        // 验证目标格式是否支持
        if (targetFormat == null || !SUPPORTED_FORMATS.contains(targetFormat.toLowerCase())) {
            return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR, "不支持的目标格式: " + targetFormat));
        }

        return validateImageFormat(filePart)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR, "不支持的图片格式"));
                    }
                    return processImageConversion(filePart, targetFormat, quality, width, height);
                })
                .doOnSuccess(result -> LoggingUtil.info(logger, "图片转换完成: {}", filePart.filename()))
                .doOnError(error -> LoggingUtil.error(logger, "图片转换失败: {}", error.getMessage()));
    }

    @Override
    public Flux<DataBuffer> convertImagesBatch(Flux<FilePart> fileParts, String targetFormat, Float quality,
            Integer width, Integer height) {
        LoggingUtil.info(logger, "开始批量转换图片，目标格式: {}", targetFormat);

        // 验证目标格式是否支持
        if (targetFormat == null || !SUPPORTED_FORMATS.contains(targetFormat.toLowerCase())) {
            return Flux.error(new BusinessException(ErrorCode.VALIDATION_ERROR, "不支持的目标格式: " + targetFormat));
        }

        return fileParts
                .flatMap(filePart -> convertImage(filePart, targetFormat, quality, width, height)
                        .onErrorResume(error -> {
                            LoggingUtil.warn(logger, "批量转换中跳过失败的图片: {} - {}", filePart.filename(), error.getMessage());
                            return Mono.empty(); // 跳过失败的图片，继续处理其他图片
                        }))
                .doOnComplete(() -> LoggingUtil.info(logger, "批量图片转换完成"));
    }

    @Override
    public Mono<Boolean> validateImageFormat(FilePart filePart) {
        String filename = filePart.filename();
        if (filename == null || filename.isEmpty()) {
            return Mono.just(false);
        }

        String extension = getFileExtension(filename).toLowerCase();
        boolean isSupported = SUPPORTED_FORMATS.contains(extension);

        if (!isSupported) {
            LoggingUtil.debug(logger, "验证图片格式: {} - 不支持", filename);
            return Mono.just(false);
        }

        // 验证文件大小
        return unifiedConfigManager.getLongConfig("honyrun.image.max-file-size", 10485760L) // 默认10MB
                .flatMap(maxFileSize -> checkFileSizeLimit(filePart, maxFileSize))
                .map(withinLimit -> {
                    if (!withinLimit) {
                        LoggingUtil.warn(logger, "文件大小超出限制: {}", filename);
                        throw new BusinessException(ErrorCode.VALIDATION_ERROR, "文件大小超出限制");
                    }
                    LoggingUtil.debug(logger, "验证图片格式和大小: {} - 通过", filename);
                    return true;
                });
    }

    @Override
    public Mono<ImageInfo> getImageInfo(FilePart filePart) {
        LoggingUtil.debug(logger, "获取图片信息: {}", filePart.filename());

        return filePart.content()
                .collectList()
                .map(dataBuffers -> {
                    try {
                        byte[] imageBytes = combineDataBuffers(dataBuffers);
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

                        if (image == null) {
                            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片文件");
                        }

                        String format = getFileExtension(filePart.filename());
                        ImageInfo info = new ImageInfo(
                                format,
                                image.getWidth(),
                                image.getHeight(),
                                (long) imageBytes.length,
                                filePart.filename());

                        LoggingUtil.debug(logger, "图片信息: {}", info);
                        return info;

                    } catch (IOException e) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取图片信息失败: " + e.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<DataBuffer> resizeImage(FilePart filePart, Integer width, Integer height, Boolean keepAspectRatio) {
        LoggingUtil.info(logger, "调整图片尺寸: {} -> {}x{}", filePart.filename(), width, height);

        return processImageResize(filePart, width, height, keepAspectRatio != null ? keepAspectRatio : true)
                .doOnSuccess(result -> LoggingUtil.info(logger, "图片尺寸调整完成: {}", filePart.filename()));
    }

    @Override
    public Mono<DataBuffer> compressImage(FilePart filePart, Float quality) {
        LoggingUtil.info(logger, "压缩图片质量: {} - 质量: {}", filePart.filename(), quality);

        String originalFormat = getFileExtension(filePart.filename());
        return processImageConversion(filePart, originalFormat, quality, null, null)
                .doOnSuccess(result -> LoggingUtil.info(logger, "图片压缩完成: {}", filePart.filename()));
    }

    @Override
    public Flux<String> getSupportedFormats() {
        return Flux.fromIterable(SUPPORTED_FORMATS);
    }

    @Override
    public Mono<Boolean> checkFileSizeLimit(FilePart filePart, Long maxSizeBytes) {
        return unifiedConfigManager.getLongConfig("honyrun.image.max-file-size", 10485760L) // 默认10MB
                .flatMap(defaultMaxSize -> {
                    Long maxSize = maxSizeBytes != null ? maxSizeBytes : defaultMaxSize;

                    return filePart.content()
                            .map(DataBuffer::readableByteCount)
                            .reduce(0L, Long::sum)
                            .map(totalSize -> {
                                boolean withinLimit = totalSize <= maxSize;
                                LoggingUtil.debug(logger, "文件大小检查: {} - {}字节 - {}",
                                        filePart.filename(), totalSize, withinLimit ? "合规" : "超限");
                                return withinLimit;
                            });
                });
    }

    /**
     * 处理图片转换的核心逻辑
     */
    private Mono<DataBuffer> processImageConversion(FilePart filePart, String targetFormat, Float quality,
            Integer width, Integer height) {
        return filePart.content()
                .collectList()
                .map(dataBuffers -> {
                    try {
                        byte[] imageBytes = combineDataBuffers(dataBuffers);

                        // 验证图片数据的基本完整性
                        if (imageBytes == null || imageBytes.length < 10) {
                            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "图片文件数据不完整");
                        }

                        BufferedImage originalImage;
                        try {
                            originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                        } catch (Exception e) {
                            // 处理损坏的图片文件，包括"Bogus marker length"等JPEG解析错误
                            LoggingUtil.warn(logger, "图片文件可能损坏或格式不正确: {} - {}", filePart.filename(), e.getMessage());
                            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "图片文件损坏或格式不正确，无法处理");
                        }

                        if (originalImage == null) {
                            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片文件，可能是不支持的格式");
                        }

                        // 调整尺寸（如果指定）
                        BufferedImage processedImage = originalImage;
                        if (width != null || height != null) {
                            processedImage = resizeBufferedImage(originalImage, width, height, true);
                        }

                        // 转换格式并应用质量设置
                        byte[] convertedBytes = convertBufferedImageToBytes(processedImage, targetFormat, quality);

                        return dataBufferFactory.wrap(convertedBytes);

                    } catch (BusinessException e) {
                        throw e; // 重新抛出业务异常
                    } catch (IOException e) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片转换失败: " + e.getMessage());
                    } catch (Exception e) {
                        LoggingUtil.error(logger, "图片转换过程中发生未知错误: {}", e.getMessage());
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片转换失败，请检查文件格式");
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 处理图片尺寸调整
     */
    private Mono<DataBuffer> processImageResize(FilePart filePart, Integer width, Integer height,
            Boolean keepAspectRatio) {
        return filePart.content()
                .collectList()
                .map(dataBuffers -> {
                    try {
                        byte[] imageBytes = combineDataBuffers(dataBuffers);
                        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

                        if (originalImage == null) {
                            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片文件");
                        }

                        BufferedImage resizedImage = resizeBufferedImage(originalImage, width, height, keepAspectRatio);
                        String originalFormat = getFileExtension(filePart.filename());

                        // 使用默认图片质量配置，避免阻塞调用
                        Float defaultQuality = 0.8f;
                        try {
                            byte[] resizedBytes = convertBufferedImageToBytes(resizedImage, originalFormat,
                                    defaultQuality);
                            return dataBufferFactory.wrap(resizedBytes);
                        } catch (IOException e) {
                            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                                    "图片尺寸调整失败: " + e.getMessage());
                        }
                    } catch (IOException e) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片尺寸调整失败: " + e.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 调整BufferedImage尺寸
     */
    private BufferedImage resizeBufferedImage(BufferedImage originalImage, Integer targetWidth, Integer targetHeight,
            Boolean keepAspectRatio) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        int newWidth = targetWidth != null ? targetWidth : originalWidth;
        int newHeight = targetHeight != null ? targetHeight : originalHeight;

        // 保持宽高比计算
        if (keepAspectRatio && targetWidth != null && targetHeight != null) {
            double aspectRatio = (double) originalWidth / originalHeight;
            double targetAspectRatio = (double) targetWidth / targetHeight;

            if (aspectRatio > targetAspectRatio) {
                newHeight = (int) (targetWidth / aspectRatio);
            } else {
                newWidth = (int) (targetHeight * aspectRatio);
            }
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();

        // 设置高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    /**
     * 将BufferedImage转换为字节数组
     */
    private byte[] convertBufferedImageToBytes(BufferedImage image, String format, Float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 处理JPEG格式的质量设置
        if ("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format)) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            if (writers.hasNext()) {
                ImageWriter writer = writers.next();
                ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                writer.setOutput(ios);

                // 设置压缩质量
                var writeParam = writer.getDefaultWriteParam();
                writeParam.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(quality != null ? quality : 0.8f);

                writer.write(null, new javax.imageio.IIOImage(image, null, null), writeParam);
                writer.dispose();
                ios.close();
            } else {
                ImageIO.write(image, "jpeg", baos);
            }
        } else {
            ImageIO.write(image, format, baos);
        }

        return baos.toByteArray();
    }

    /**
     * 添加图片水印
     * 为图片添加文字水印
     *
     * @param filePart         原始图片文件
     * @param watermarkRequest 水印配置请求
     * @return 添加水印后的图片数据
     */
    @Override
    public Mono<DataBuffer> addWatermark(FilePart filePart,
            com.honyrun.model.dto.image.ImageConversionRequest watermarkRequest) {
        LoggingUtil.info(logger, "开始为图片添加水印: {}", filePart.filename());

        return filePart.content()
                .collectList()
                .map(this::combineDataBuffers)
                .flatMap(imageBytes -> {
                    try {
                        // 读取原始图片
                        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                        if (originalImage == null) {
                            return Mono.error(new BusinessException(ErrorCode.INVALID_PARAMETER, "无法读取图片文件"));
                        }

                        // 添加水印
                        BufferedImage watermarkedImage = addTextWatermarkToImage(originalImage, watermarkRequest);

                        // 转换为字节数组
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        String format = getImageFormat(filePart.filename());
                        ImageIO.write(watermarkedImage, format, baos);

                        // 创建DataBuffer
                        DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
                        DataBuffer dataBuffer = bufferFactory.wrap(baos.toByteArray());

                        LoggingUtil.info(logger, "成功为图片添加水印: {}", filePart.filename());
                        return Mono.just(dataBuffer);

                    } catch (IOException e) {
                        LoggingUtil.error(logger, "添加水印时发生IO错误: {}", e.getMessage());
                        return Mono.error(new BusinessException(ErrorCode.SYSTEM_ERROR, "图片水印处理失败"));
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 为图片添加文字水印
     */
    private BufferedImage addTextWatermarkToImage(BufferedImage originalImage,
            com.honyrun.model.dto.image.ImageConversionRequest request) {
        // 保持原始图片的类型，避免透明度丢失
        int imageType = originalImage.getType();
        if (imageType == BufferedImage.TYPE_CUSTOM) {
            imageType = BufferedImage.TYPE_INT_RGB;
        }

        BufferedImage watermarkedImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                imageType);

        Graphics2D g2d = watermarkedImage.createGraphics();

        // 绘制原始图片
        g2d.drawImage(originalImage, 0, 0, null);

        // 设置水印属性
        if (request.getWatermarkText() != null && !request.getWatermarkText().isEmpty()) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD,
                    request.getWatermarkFontSize() != null ? request.getWatermarkFontSize() : 24));

            // 设置透明度
            float alpha = request.getWatermarkAlpha() != null ? request.getWatermarkAlpha() : 0.5f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            // 计算水印位置
            int x = request.getWatermarkX() != null ? request.getWatermarkX() : 10;
            int y = request.getWatermarkY() != null ? request.getWatermarkY() : 30;

            // 绘制水印文字
            g2d.drawString(request.getWatermarkText(), x, y);
        }

        g2d.dispose();
        return watermarkedImage;
    }

    /**
     * 获取图片格式
     */
    private String getImageFormat(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "jpeg";
            case "png":
                return "png";
            case "gif":
                return "gif";
            case "bmp":
                return "bmp";
            default:
                return "jpeg"; // 默认格式
        }
    }

    /**
     * 合并DataBuffer列表为字节数组
     */
    private byte[] combineDataBuffers(List<DataBuffer> dataBuffers) {
        int totalSize = dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum();
        byte[] combined = new byte[totalSize];
        int offset = 0;

        for (DataBuffer buffer : dataBuffers) {
            int length = buffer.readableByteCount();
            buffer.read(combined, offset, length);
            offset += length;
        }

        return combined;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    @Override
    public Mono<DataBuffer> rotateImage(FilePart filePart, Integer angle) {
        LoggingUtil.info(logger, "开始旋转图片: {} 角度: {}", filePart.filename(), angle);

        if (angle == null || angle % 90 != 0) {
            return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR, "旋转角度必须是90的倍数"));
        }

        return validateImageFormat(filePart)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR, "不支持的图片格式"));
                    }
                    return processImageRotation(filePart, angle);
                })
                .doOnSuccess(result -> LoggingUtil.info(logger, "图片旋转完成: {}", filePart.filename()))
                .doOnError(error -> LoggingUtil.error(logger, "图片旋转失败: {}", error.getMessage()));
    }

    @Override
    public Mono<DataBuffer> cropImage(FilePart filePart, Integer x, Integer y, Integer width, Integer height) {
        LoggingUtil.info(logger, "开始裁剪图片: {} 区域: ({},{},{},{})", filePart.filename(), x, y, width, height);

        if (x == null || y == null || width == null || height == null ||
                x < 0 || y < 0 || width <= 0 || height <= 0) {
            return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR, "裁剪参数无效"));
        }

        return validateImageFormat(filePart)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR, "不支持的图片格式"));
                    }
                    return processImageCrop(filePart, x, y, width, height);
                })
                .doOnSuccess(result -> LoggingUtil.info(logger, "图片裁剪完成: {}", filePart.filename()))
                .doOnError(error -> LoggingUtil.error(logger, "图片裁剪失败: {}", error.getMessage()));
    }

    @Override
    public Mono<DataBuffer> flipImage(FilePart filePart, Boolean horizontal) {
        LoggingUtil.info(logger, "开始翻转图片: {} 水平翻转: {}", filePart.filename(), horizontal);

        if (horizontal == null) {
            return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR, "翻转方向参数不能为空"));
        }

        return validateImageFormat(filePart)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR, "不支持的图片格式"));
                    }
                    return processImageFlip(filePart, horizontal);
                })
                .doOnSuccess(result -> LoggingUtil.info(logger, "图片翻转完成: {}", filePart.filename()))
                .doOnError(error -> LoggingUtil.error(logger, "图片翻转失败: {}", error.getMessage()));
    }

    @Override
    public Mono<DataBuffer> adjustImageQuality(FilePart filePart, Float quality) {
        LoggingUtil.info(logger, "开始调整图片质量: {} 质量: {}", filePart.filename(), quality);

        if (quality == null || quality < 0.0f || quality > 1.0f) {
            return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR, "图片质量必须在0.0-1.0之间"));
        }

        return validateImageFormat(filePart)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR, "不支持的图片格式"));
                    }
                    return processImageQualityAdjustment(filePart, quality);
                })
                .doOnSuccess(result -> LoggingUtil.info(logger, "图片质量调整完成: {}", filePart.filename()))
                .doOnError(error -> LoggingUtil.error(logger, "图片质量调整失败: {}", error.getMessage()));
    }

    @Override
    public Mono<Boolean> isSupportedFormat(String format) {
        if (format == null || format.trim().isEmpty()) {
            return Mono.just(false);
        }
        return Mono.just(SUPPORTED_FORMATS.contains(format.toLowerCase().trim()));
    }

    /**
     * 处理图片旋转
     */
    private Mono<DataBuffer> processImageRotation(FilePart filePart, Integer angle) {
        return filePart.content()
                .collectList()
                .publishOn(Schedulers.boundedElastic())
                .map(dataBuffers -> {
                    try {
                        byte[] imageBytes = combineDataBuffers(dataBuffers);
                        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

                        if (originalImage == null) {
                            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片数据");
                        }

                        BufferedImage rotatedImage = rotateBufferedImage(originalImage, angle);

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        String format = getImageFormat(filePart.filename());
                        ImageIO.write(rotatedImage, format, outputStream);

                        return dataBufferFactory.wrap(outputStream.toByteArray());
                    } catch (IOException e) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片旋转处理失败: " + e.getMessage());
                    }
                });
    }

    /**
     * 处理图片裁剪
     */
    private Mono<DataBuffer> processImageCrop(FilePart filePart, Integer x, Integer y, Integer width, Integer height) {
        return filePart.content()
                .collectList()
                .publishOn(Schedulers.boundedElastic())
                .map(dataBuffers -> {
                    try {
                        byte[] imageBytes = combineDataBuffers(dataBuffers);
                        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

                        if (originalImage == null) {
                            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片数据");
                        }

                        // 验证裁剪区域是否在图片范围内
                        if (x + width > originalImage.getWidth() || y + height > originalImage.getHeight()) {
                            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "裁剪区域超出图片范围");
                        }

                        BufferedImage croppedImage = originalImage.getSubimage(x, y, width, height);

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        String format = getImageFormat(filePart.filename());
                        ImageIO.write(croppedImage, format, outputStream);

                        return dataBufferFactory.wrap(outputStream.toByteArray());
                    } catch (IOException e) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片裁剪处理失败: " + e.getMessage());
                    }
                });
    }

    /**
     * 处理图片翻转
     */
    private Mono<DataBuffer> processImageFlip(FilePart filePart, Boolean horizontal) {
        return filePart.content()
                .collectList()
                .publishOn(Schedulers.boundedElastic())
                .map(dataBuffers -> {
                    try {
                        byte[] imageBytes = combineDataBuffers(dataBuffers);
                        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

                        if (originalImage == null) {
                            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片数据");
                        }

                        BufferedImage flippedImage = flipBufferedImage(originalImage, horizontal);

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        String format = getImageFormat(filePart.filename());
                        ImageIO.write(flippedImage, format, outputStream);

                        return dataBufferFactory.wrap(outputStream.toByteArray());
                    } catch (IOException e) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片翻转处理失败: " + e.getMessage());
                    }
                });
    }

    /**
     * 处理图片质量调整
     */
    private Mono<DataBuffer> processImageQualityAdjustment(FilePart filePart, Float quality) {
        return filePart.content()
                .collectList()
                .publishOn(Schedulers.boundedElastic())
                .map(dataBuffers -> {
                    try {
                        byte[] imageBytes = combineDataBuffers(dataBuffers);
                        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

                        if (originalImage == null) {
                            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片数据");
                        }

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        String format = getImageFormat(filePart.filename());

                        // 使用ImageWriter来控制质量
                        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
                        if (!writers.hasNext()) {
                            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的图片格式: " + format);
                        }

                        ImageWriter writer = writers.next();
                        ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
                        writer.setOutput(ios);

                        // 设置压缩参数
                        if ("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format)) {
                            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
                            param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                            param.setCompressionQuality(quality);
                            writer.write(null, new javax.imageio.IIOImage(originalImage, null, null), param);
                        } else {
                            writer.write(originalImage);
                        }

                        writer.dispose();
                        ios.close();

                        return dataBufferFactory.wrap(outputStream.toByteArray());
                    } catch (IOException e) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片质量调整失败: " + e.getMessage());
                    }
                });
    }

    /**
     * 旋转BufferedImage
     */
    private BufferedImage rotateBufferedImage(BufferedImage originalImage, Integer angle) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage rotatedImage;
        if (angle == 90 || angle == 270) {
            rotatedImage = new BufferedImage(height, width, originalImage.getType());
        } else {
            rotatedImage = new BufferedImage(width, height, originalImage.getType());
        }

        Graphics2D g2d = rotatedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        switch (angle) {
            case 90:
                g2d.translate(height, 0);
                g2d.rotate(Math.PI / 2);
                break;
            case 180:
                g2d.translate(width, height);
                g2d.rotate(Math.PI);
                break;
            case 270:
                g2d.translate(0, width);
                g2d.rotate(-Math.PI / 2);
                break;
        }

        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();

        return rotatedImage;
    }

    /**
     * 翻转BufferedImage
     */
    private BufferedImage flipBufferedImage(BufferedImage originalImage, Boolean horizontal) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage flippedImage = new BufferedImage(width, height, originalImage.getType());
        Graphics2D g2d = flippedImage.createGraphics();

        if (horizontal) {
            // 水平翻转
            g2d.drawImage(originalImage, width, 0, 0, height, 0, 0, width, height, null);
        } else {
            // 垂直翻转
            g2d.drawImage(originalImage, 0, height, width, 0, 0, 0, width, height, null);
        }

        g2d.dispose();
        return flippedImage;
    }
}
