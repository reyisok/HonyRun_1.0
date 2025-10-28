package com.honyrun.util.image;

import com.honyrun.exception.BusinessException;
import com.honyrun.exception.ErrorCode;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * 图片转换工具类
 * 提供图片格式转换、尺寸调整、质量控制等功能
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 工具功能:
 * - 图片格式转换 (JPEG, PNG, GIF, BMP, WEBP)
 * - 图片尺寸调整和缩放
 * - 图片质量压缩
 * - 图片旋转和翻转
 * - 图片水印添加
 * - 图片信息提取
 */
public class ImageConversionUtil {

    private static final Logger logger = LoggerFactory.getLogger(ImageConversionUtil.class);

    // 默认配置常量
    private static final Float DEFAULT_QUALITY = 0.8f;
    private static final String DEFAULT_FORMAT = "jpeg";

    /**
     * 私有构造函数，防止实例化
     */
    private ImageConversionUtil() {
        throw new UnsupportedOperationException("工具类不能被实例化");
    }

    /**
     * 转换图片格式
     *
     * @param imageBytes 原始图片字节数组
     * @param targetFormat 目标格式 (jpeg, png, gif, bmp, webp)
     * @param quality 图片质量 (0.0-1.0)
     * @return 转换后的图片字节数组
     * @throws BusinessException 转换失败时抛出
     */
    public static byte[] convertFormat(byte[] imageBytes, String targetFormat, Float quality) {
        LoggingUtil.debug(logger, "开始转换图片格式: {} -> {}, 质量: {}",
            getImageFormat(imageBytes), targetFormat, quality);

        // 参数验证
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "图片数据不能为空");
        }
        
        if (targetFormat == null || targetFormat.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "目标格式不能为空");
        }
        
        // 验证支持的格式
        String[] supportedFormats = {"jpeg", "jpg", "png", "gif", "bmp", "webp"};
        boolean isSupported = false;
        for (String format : supportedFormats) {
            if (format.equalsIgnoreCase(targetFormat.trim())) {
                isSupported = true;
                break;
            }
        }
        if (!isSupported) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "不支持的图片格式: " + targetFormat);
        }

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片数据");
            }

            return convertBufferedImageToBytes(image, targetFormat, quality != null ? quality : DEFAULT_QUALITY);

        } catch (IOException e) {
            LoggingUtil.error(logger, "图片格式转换失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片格式转换失败: " + e.getMessage());
        }
    }

    /**
     * 调整图片尺寸
     *
     * @param imageBytes 原始图片字节数组
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @param keepAspectRatio 是否保持宽高比
     * @return 调整后的图片字节数组
     * @throws BusinessException 调整失败时抛出
     */
    public static byte[] resizeImage(byte[] imageBytes, int targetWidth, int targetHeight, boolean keepAspectRatio) {
        LoggingUtil.debug(logger, "开始调整图片尺寸: {}x{}, 保持比例: {}", targetWidth, targetHeight, keepAspectRatio);

        // 参数验证
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "图片数据不能为空");
        }
        
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "目标尺寸必须大于0");
        }

        // 限制极端大尺寸，避免资源消耗过大
        if (targetWidth > 5000 || targetHeight > 5000) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "目标尺寸过大，最大允许5000");
        }

        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (originalImage == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片数据");
            }

            BufferedImage resizedImage = resizeBufferedImage(originalImage, targetWidth, targetHeight, keepAspectRatio);
            String originalFormat = getImageFormat(imageBytes);

            return convertBufferedImageToBytes(resizedImage, originalFormat, DEFAULT_QUALITY);

        } catch (IOException e) {
            LoggingUtil.error(logger, "图片尺寸调整失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片尺寸调整失败: " + e.getMessage());
        }
    }

    /**
     * 压缩图片质量
     *
     * @param imageBytes 原始图片字节数组
     * @param quality 压缩质量 (0.0-1.0)
     * @return 压缩后的图片字节数组
     * @throws BusinessException 压缩失败时抛出
     */
    public static byte[] compressImage(byte[] imageBytes, float quality) {
        LoggingUtil.debug(logger, "开始压缩图片，质量: {}", quality);

        // 参数验证
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "图片数据不能为空");
        }
        
        if (quality <= 0.0f || quality > 1.0f) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "图片质量必须在0.0-1.0之间");
        }

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片数据");
            }

            String originalFormat = getImageFormat(imageBytes);
            return convertBufferedImageToBytes(image, originalFormat, quality);

        } catch (IOException e) {
            LoggingUtil.error(logger, "图片质量压缩失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片质量压缩失败: " + e.getMessage());
        }
    }

    /**
     * 旋转图片
     *
     * @param imageBytes 原始图片字节数组
     * @param angle 旋转角度 (90, 180, 270)
     * @return 旋转后的图片字节数组
     * @throws BusinessException 旋转失败时抛出
     */
    public static byte[] rotateImage(byte[] imageBytes, int angle) {
        LoggingUtil.debug(logger, "开始旋转图片: {}度", angle);

        // 参数验证
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "图片数据不能为空");
        }

        if (angle <= 0 || angle % 90 != 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "旋转角度必须是90的倍数");
        }

        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (originalImage == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片数据");
            }

            BufferedImage rotatedImage = rotateBufferedImage(originalImage, angle);
            String originalFormat = getImageFormat(imageBytes);

            return convertBufferedImageToBytes(rotatedImage, originalFormat, DEFAULT_QUALITY);

        } catch (IOException e) {
            LoggingUtil.error(logger, "图片旋转失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片旋转失败: " + e.getMessage());
        }
    }

    /**
     * 翻转图片
     *
     * @param imageBytes 原始图片字节数组
     * @param horizontal 是否水平翻转
     * @param vertical 是否垂直翻转
     * @return 翻转后的图片字节数组
     * @throws BusinessException 翻转失败时抛出
     */
    public static byte[] flipImage(byte[] imageBytes, boolean horizontal, boolean vertical) {
        LoggingUtil.debug(logger, "开始翻转图片: 水平={}, 垂直={}", horizontal, vertical);

        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (originalImage == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片数据");
            }

            BufferedImage flippedImage = flipBufferedImage(originalImage, horizontal, vertical);
            String originalFormat = getImageFormat(imageBytes);

            return convertBufferedImageToBytes(flippedImage, originalFormat, DEFAULT_QUALITY);

        } catch (IOException e) {
            LoggingUtil.error(logger, "图片翻转失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片翻转失败: " + e.getMessage());
        }
    }

    /**
     * 添加文字水印
     *
     * @param imageBytes 原始图片字节数组
     * @param watermarkText 水印文字
     * @param x 水印X坐标
     * @param y 水印Y坐标
     * @param fontSize 字体大小
     * @param alpha 透明度 (0.0-1.0)
     * @return 添加水印后的图片字节数组
     * @throws BusinessException 添加水印失败时抛出
     */
    public static byte[] addTextWatermark(byte[] imageBytes, String watermarkText, int x, int y, int fontSize, float alpha) {
        LoggingUtil.debug(logger, "开始添加文字水印: {}", watermarkText);

        // 参数验证
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "图片数据不能为空");
        }
        
        if (watermarkText == null || watermarkText.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "水印文字不能为空");
        }
        
        if (x < 0 || y < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "水印位置坐标不能为负数");
        }
        
        if (fontSize <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "字体大小必须大于0");
        }
        
        if (alpha < 0.0f || alpha > 1.0f) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "透明度必须在0.0-1.0之间");
        }

        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (originalImage == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片数据");
            }

            BufferedImage watermarkedImage = addTextWatermarkToImage(originalImage, watermarkText, x, y, fontSize, alpha);
            String originalFormat = getImageFormat(imageBytes);

            return convertBufferedImageToBytes(watermarkedImage, originalFormat, DEFAULT_QUALITY);

        } catch (IOException e) {
            LoggingUtil.error(logger, "添加文字水印失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加文字水印失败: " + e.getMessage());
        }
    }

    /**
     * 获取图片信息
     *
     * @param imageBytes 图片字节数组
     * @return 图片信息对象
     * @throws BusinessException 获取信息失败时抛出
     */
    public static ImageInfo getImageInfo(byte[] imageBytes) {
        // 参数验证
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "图片数据不能为空");
        }

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取图片数据");
            }

            String format = getImageFormat(imageBytes);
            return new ImageInfo(
                format,
                image.getWidth(),
                image.getHeight(),
                (long) imageBytes.length,
                null
            );

        } catch (IOException e) {
            LoggingUtil.error(logger, "获取图片信息失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取图片信息失败: " + e.getMessage());
        }
    }

    /**
     * 调整BufferedImage尺寸
     */
    private static BufferedImage resizeBufferedImage(BufferedImage originalImage, int targetWidth, int targetHeight, boolean keepAspectRatio) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        int newWidth = targetWidth;
        int newHeight = targetHeight;

        // 保持宽高比计算
        if (keepAspectRatio) {
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
     * 旋转BufferedImage
     */
    private static BufferedImage rotateBufferedImage(BufferedImage originalImage, int angle) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // 计算旋转后的尺寸
        int newWidth = (angle == 90 || angle == 270) ? height : width;
        int newHeight = (angle == 90 || angle == 270) ? width : height;

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g2d = rotatedImage.createGraphics();

        // 设置旋转中心点
        g2d.translate(newWidth / 2, newHeight / 2);
        g2d.rotate(Math.toRadians(angle));
        g2d.translate(-width / 2, -height / 2);

        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();

        return rotatedImage;
    }

    /**
     * 翻转BufferedImage
     */
    private static BufferedImage flipBufferedImage(BufferedImage originalImage, boolean horizontal, boolean vertical) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage flippedImage = new BufferedImage(width, height, originalImage.getType());
        Graphics2D g2d = flippedImage.createGraphics();

        int x = horizontal ? width : 0;
        int y = vertical ? height : 0;
        int scaleX = horizontal ? -1 : 1;
        int scaleY = vertical ? -1 : 1;

        g2d.translate(x, y);
        g2d.scale(scaleX, scaleY);
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();

        return flippedImage;
    }

    /**
     * 添加文字水印到BufferedImage
     */
    private static BufferedImage addTextWatermarkToImage(BufferedImage originalImage, String watermarkText, int x, int y, int fontSize, float alpha) {
        BufferedImage watermarkedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
        Graphics2D g2d = watermarkedImage.createGraphics();

        // 绘制原始图片
        g2d.drawImage(originalImage, 0, 0, null);

        // 设置水印样式
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setColor(Color.WHITE);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制水印文字
        g2d.drawString(watermarkText, x, y);
        g2d.dispose();

        return watermarkedImage;
    }

    /**
     * 将BufferedImage转换为字节数组
     */
    private static byte[] convertBufferedImageToBytes(BufferedImage image, String format, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 处理JPEG格式的质量设置和颜色空间转换
        if ("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format)) {
            // 如果原图有透明通道，需要转换为RGB格式
            if (image.getColorModel().hasAlpha()) {
                BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = rgbImage.createGraphics();
                g2d.setColor(Color.WHITE); // 设置白色背景
                g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();
                image = rgbImage;
            }
            
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            if (writers.hasNext()) {
                ImageWriter writer = writers.next();
                ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                writer.setOutput(ios);

                // 设置压缩质量
                var writeParam = writer.getDefaultWriteParam();
                writeParam.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(quality);

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
     * 获取图片格式
     */
    private static String getImageFormat(byte[] imageBytes) {
        try {
            // 通过文件头判断格式
            if (imageBytes.length >= 2) {
                // JPEG格式
                if ((imageBytes[0] & 0xFF) == 0xFF && (imageBytes[1] & 0xFF) == 0xD8) {
                    return "jpeg";
                }
                // PNG格式
                if (imageBytes.length >= 8 &&
                    (imageBytes[0] & 0xFF) == 0x89 && imageBytes[1] == 0x50 &&
                    imageBytes[2] == 0x4E && imageBytes[3] == 0x47) {
                    return "png";
                }
                // GIF格式
                if (imageBytes.length >= 6 &&
                    imageBytes[0] == 0x47 && imageBytes[1] == 0x49 && imageBytes[2] == 0x46) {
                    return "gif";
                }
                // BMP格式
                if (imageBytes[0] == 0x42 && imageBytes[1] == 0x4D) {
                    return "bmp";
                }
            }
        } catch (Exception e) {
            LoggingUtil.warn(logger, "无法识别图片格式，使用默认格式: {}", DEFAULT_FORMAT);
        }

        return DEFAULT_FORMAT;
    }

    /**
     * 图片信息类
     */
    public static class ImageInfo {
        private String format;
        private Integer width;
        private Integer height;
        private Long fileSize;
        private String fileName;

        public ImageInfo(String format, Integer width, Integer height, Long fileSize, String fileName) {
            this.format = format;
            this.width = width;
            this.height = height;
            this.fileSize = fileSize;
            this.fileName = fileName;
        }

        // Getter方法
        public String getFormat() { return format; }
        public Integer getWidth() { return width; }
        public Integer getHeight() { return height; }
        public Long getFileSize() { return fileSize; }
        public String getFileName() { return fileName; }

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

