package com.honyrun.service.reactive;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式图片服务接口
 * 提供图片转换、格式支持、批处理等核心功能
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 功能特性:
 * - 支持多种图片格式转换 (JPEG, PNG, GIF, BMP, WEBP)
 * - 响应式文件处理，支持大文件流式处理
 * - 批量图片处理能力
 * - 图片质量和尺寸调整
 * - 异步处理和错误恢复
 */
public interface ReactiveImageService {

    /**
     * 单个图片格式转换
     * 将上传的图片文件转换为指定格式
     *
     * @param filePart 上传的图片文件部分
     * @param targetFormat 目标格式 (jpeg, png, gif, bmp, webp)
     * @param quality 图片质量 (0.0-1.0)
     * @param width 目标宽度 (可选，null表示保持原始比例)
     * @param height 目标高度 (可选，null表示保持原始比例)
     * @return 转换后的图片数据流
     */
    Mono<DataBuffer> convertImage(FilePart filePart, String targetFormat, Float quality, Integer width, Integer height);

    /**
     * 批量图片格式转换
     * 批量处理多个图片文件的格式转换
     *
     * @param fileParts 上传的图片文件流
     * @param targetFormat 目标格式
     * @param quality 图片质量
     * @param width 目标宽度
     * @param height 目标高度
     * @return 转换后的图片数据流
     */
    Flux<DataBuffer> convertImagesBatch(Flux<FilePart> fileParts, String targetFormat, Float quality, Integer width, Integer height);

    /**
     * 验证图片文件格式
     * 检查上传的文件是否为支持的图片格式
     *
     * @param filePart 上传的文件部分
     * @return 验证结果，true表示格式支持
     */
    Mono<Boolean> validateImageFormat(FilePart filePart);

    /**
     * 获取图片文件信息
     * 提取图片的基本信息（尺寸、格式、大小等）
     *
     * @param filePart 图片文件部分
     * @return 图片信息对象
     */
    Mono<ImageInfo> getImageInfo(FilePart filePart);

    /**
     * 调整图片尺寸
     * 按指定尺寸调整图片大小，支持保持比例
     *
     * @param filePart 原始图片文件
     * @param width 目标宽度
     * @param height 目标高度
     * @param keepAspectRatio 是否保持宽高比
     * @return 调整后的图片数据
     */
    Mono<DataBuffer> resizeImage(FilePart filePart, Integer width, Integer height, Boolean keepAspectRatio);

    /**
     * 压缩图片质量
     * 调整图片的压缩质量以减小文件大小
     *
     * @param filePart 原始图片文件
     * @param quality 压缩质量 (0.0-1.0)
     * @return 压缩后的图片数据
     */
    Mono<DataBuffer> compressImage(FilePart filePart, Float quality);

    /**
     * 获取支持的图片格式列表
     * 返回系统支持的所有图片格式
     *
     * @return 支持的格式列表
     */
    Flux<String> getSupportedFormats();

    /**
     * 添加图片水印
     * 为图片添加文字水印
     *
     * @param filePart 原始图片文件
     * @param watermarkRequest 水印配置请求
     * @return 添加水印后的图片数据
     */
    Mono<DataBuffer> addWatermark(FilePart filePart, com.honyrun.model.dto.image.ImageConversionRequest watermarkRequest);

    /**
     * 检查图片文件大小限制
     * 验证上传的图片文件是否超过大小限制
     *
     * @param filePart 图片文件部分
     * @param maxSizeBytes 最大文件大小（字节）
     * @return 检查结果，true表示文件大小合规
     */
    Mono<Boolean> checkFileSizeLimit(FilePart filePart, Long maxSizeBytes);

    /**
     * 旋转图片
     * 按指定角度旋转图片
     *
     * @param filePart 原始图片文件
     * @param angle 旋转角度（度数）
     * @return 旋转后的图片数据
     */
    Mono<DataBuffer> rotateImage(FilePart filePart, Integer angle);

    /**
     * 裁剪图片
     * 按指定区域裁剪图片
     *
     * @param filePart 原始图片文件
     * @param x 裁剪起始X坐标
     * @param y 裁剪起始Y坐标
     * @param width 裁剪宽度
     * @param height 裁剪高度
     * @return 裁剪后的图片数据
     */
    Mono<DataBuffer> cropImage(FilePart filePart, Integer x, Integer y, Integer width, Integer height);

    /**
     * 翻转图片
     * 水平或垂直翻转图片
     *
     * @param filePart 原始图片文件
     * @param horizontal 是否水平翻转
     * @return 翻转后的图片数据
     */
    Mono<DataBuffer> flipImage(FilePart filePart, Boolean horizontal);

    /**
     * 调整图片质量
     * 调整图片的压缩质量
     *
     * @param filePart 原始图片文件
     * @param quality 图片质量 (0.0-1.0)
     * @return 调整后的图片数据
     */
    Mono<DataBuffer> adjustImageQuality(FilePart filePart, Float quality);

    /**
     * 检查是否支持指定格式
     * 验证指定的图片格式是否被系统支持
     *
     * @param format 图片格式
     * @return 是否支持该格式
     */
    Mono<Boolean> isSupportedFormat(String format);

    /**
     * 图片信息内部类
     * 封装图片的基本信息
     */
    class ImageInfo {
        private String format;
        private Integer width;
        private Integer height;
        private Long fileSize;
        private String fileName;

        // 构造函数
        public ImageInfo() {}

        public ImageInfo(String format, Integer width, Integer height, Long fileSize, String fileName) {
            this.format = format;
            this.width = width;
            this.height = height;
            this.fileSize = fileSize;
            this.fileName = fileName;
        }

        // Getter和Setter方法
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }

        public Integer getWidth() { return width; }
        public void setWidth(Integer width) { this.width = width; }

        public Integer getHeight() { return height; }
        public void setHeight(Integer height) { this.height = height; }

        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        @Override
        public String toString() {
            return "ImageInfo{" +
                    "format='" + format + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    ", fileSize=" + fileSize +
                    ", fileName='" + fileName + '\'' +
                    '}';
        }
    }
}

