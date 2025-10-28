package com.honyrun.util.image;

import com.honyrun.exception.BusinessException;
import com.honyrun.exception.ErrorCode;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 图片验证工具类
 * 提供图片格式验证、大小检查、安全检查等功能
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 验证功能:
 * - 图片格式验证
 * - 文件大小限制检查
 * - 图片尺寸验证
 * - 文件名安全检查
 * - 图片内容安全验证
 * - MIME类型验证
 */
public class ImageValidationUtil {

    private static final Logger logger = LoggerFactory.getLogger(ImageValidationUtil.class);

    // 支持的图片格式
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList(
        "jpeg", "jpg", "png", "gif", "bmp", "webp"
    );

    // 支持的MIME类型
    private static final List<String> SUPPORTED_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp"
    );

    // 默认限制配置
    private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024L; // 10MB
    private static final int DEFAULT_MAX_WIDTH = 2500;
    private static final int DEFAULT_MAX_HEIGHT = 2500;
    private static final int DEFAULT_MIN_WIDTH = 1;
    private static final int DEFAULT_MIN_HEIGHT = 1;

    // 文件名安全检查正则表达式
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList(
        "exe", "bat", "cmd", "com", "pif", "scr", "vbs", "js", "jar", "php", "asp", "jsp"
    );

    /**
     * 私有构造函数，防止实例化
     */
    private ImageValidationUtil() {
        throw new UnsupportedOperationException("工具类不能被实例化");
    }

    /**
     * 验证图片格式是否支持
     *
     * @param filename 文件名
     * @return 验证结果
     */
    public static boolean isValidImageFormat(String filename) {
        if (filename == null || filename.isEmpty()) {
            LoggingUtil.warn(logger, "文件名为空");
            return false;
        }

        String extension = getFileExtension(filename).toLowerCase();

        LoggingUtil.debug(logger, "图片格式验证: {} - {}", filename, SUPPORTED_FORMATS.contains(extension) ? "支持" : "不支持");
        return SUPPORTED_FORMATS.contains(extension);
    }

    /**
     * 验证MIME类型是否支持
     *
     * @param mimeType MIME类型
     * @return 验证结果
     */
    public static boolean isValidMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            LoggingUtil.warn(logger, "MIME类型为空");
            return false;
        }

        LoggingUtil.debug(logger, "MIME类型验证: {} - {}", mimeType, SUPPORTED_MIME_TYPES.contains(mimeType.toLowerCase()) ? "支持" : "不支持");
        return SUPPORTED_MIME_TYPES.contains(mimeType.toLowerCase());
    }

    /**
     * 验证文件大小是否在限制范围内
     *
     * @param fileSize 文件大小（字节）
     * @param maxSize 最大文件大小（字节，null使用默认值）
     * @return 验证结果
     */
    public static boolean isValidFileSize(long fileSize, Long maxSize) {
        long maxSizeLimit = maxSize != null ? maxSize : DEFAULT_MAX_FILE_SIZE;

        LoggingUtil.debug(logger, "文件大小验证: {}字节 / {}字节 - {}",
            fileSize, maxSizeLimit, (fileSize >= 0 && fileSize <= maxSizeLimit) ? "合规" : "超限");
        return fileSize >= 0 && fileSize <= maxSizeLimit;
    }

    /**
     * 验证图片尺寸是否在限制范围内
     *
     * @param imageBytes 图片字节数组
     * @param maxWidth 最大宽度（null使用默认值）
     * @param maxHeight 最大高度（null使用默认值）
     * @param minWidth 最小宽度（null使用默认值）
     * @param minHeight 最小高度（null使用默认值）
     * @return 验证结果
     * @throws BusinessException 读取图片失败时抛出
     */
    public static boolean isValidImageDimensions(byte[] imageBytes, Integer maxWidth, Integer maxHeight,
                                                Integer minWidth, Integer minHeight) {
        if (imageBytes == null || imageBytes.length == 0) {
            LoggingUtil.warn(logger, "图片数据为空，无法进行尺寸验证");
            return false;
        }
        
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                LoggingUtil.warn(logger, "无法读取图片数据进行尺寸验证");
                return false;
            }

            int width = image.getWidth();
            int height = image.getHeight();

            int maxW = maxWidth != null ? maxWidth : DEFAULT_MAX_WIDTH;
            int maxH = maxHeight != null ? maxHeight : DEFAULT_MAX_HEIGHT;
            int minW = minWidth != null ? minWidth : DEFAULT_MIN_WIDTH;
            int minH = minHeight != null ? minHeight : DEFAULT_MIN_HEIGHT;

            LoggingUtil.debug(logger, "图片尺寸验证: {}x{} (限制: {}x{} - {}x{}) - {}",
                width, height, minW, minH, maxW, maxH, (width >= minW && width <= maxW && height >= minH && height <= maxH) ? "合规" : "超限");

            return width >= minW && width <= maxW && height >= minH && height <= maxH;

        } catch (IOException e) {
            LoggingUtil.error(logger, "读取图片进行尺寸验证失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取图片进行尺寸验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证文件名是否安全
     *
     * @param filename 文件名
     * @return 验证结果
     */
    public static boolean isSafeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            LoggingUtil.warn(logger, "文件名为空");
            return false;
        }

        // 检查文件名长度
        if (filename.length() > 255) {
            LoggingUtil.warn(logger, "文件名过长: {}", filename.length());
            return false;
        }

        // 检查危险字符
        if (!SAFE_FILENAME_PATTERN.matcher(filename).matches()) {
            LoggingUtil.warn(logger, "文件名包含危险字符: {}", filename);
            return false;
        }

        // 检查危险扩展名
        String extension = getFileExtension(filename).toLowerCase();
        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            LoggingUtil.warn(logger, "文件扩展名存在安全风险: {}", extension);
            return false;
        }

        // 检查保留文件名（Windows）
        String nameWithoutExtension = filename.substring(0, filename.lastIndexOf('.') >= 0 ? filename.lastIndexOf('.') : filename.length());
        List<String> reservedNames = Arrays.asList(
            "CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
        );

        if (reservedNames.contains(nameWithoutExtension.toUpperCase())) {
            LoggingUtil.warn(logger, "文件名使用了系统保留名称: {}", nameWithoutExtension);
            return false;
        }

        LoggingUtil.debug(logger, "文件名安全检查通过: {}", filename);
        return true;
    }

    /**
     * 验证图片内容是否为真实图片
     *
     * @param imageBytes 图片字节数组
     * @return 验证结果
     */
    public static boolean isValidImageContent(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            LoggingUtil.warn(logger, "图片数据为空");
            return false;
        }

        try {
            // 尝试读取图片
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                LoggingUtil.warn(logger, "无法解析图片内容");
                return false;
            }

            // 检查图片基本属性
            int width = image.getWidth();
            int height = image.getHeight();

            if (width <= 0 || height <= 0) {
                LoggingUtil.warn(logger, "图片尺寸无效: {}x{}", width, height);
                return false;
            }

            // 验证文件头
            if (!isValidImageHeader(imageBytes)) {
                LoggingUtil.warn(logger, "图片文件头验证失败");
                return false;
            }

            LoggingUtil.debug(logger, "图片内容验证通过: {}x{}", width, height);
            return true;

        } catch (IOException e) {
            LoggingUtil.warn(logger, "图片内容验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证图片文件头
     *
     * @param imageBytes 图片字节数组
     * @return 验证结果
     */
    public static boolean isValidImageHeader(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 8) {
            return false;
        }

        // JPEG文件头
        if (imageBytes.length >= 2 &&
            (imageBytes[0] & 0xFF) == 0xFF && (imageBytes[1] & 0xFF) == 0xD8) {
            return true;
        }

        // PNG文件头
        if (imageBytes.length >= 8 &&
            (imageBytes[0] & 0xFF) == 0x89 && imageBytes[1] == 0x50 &&
            imageBytes[2] == 0x4E && imageBytes[3] == 0x47 &&
            imageBytes[4] == 0x0D && imageBytes[5] == 0x0A &&
            imageBytes[6] == 0x1A && imageBytes[7] == 0x0A) {
            return true;
        }

        // GIF文件头
        if (imageBytes.length >= 6 &&
            imageBytes[0] == 0x47 && imageBytes[1] == 0x49 && imageBytes[2] == 0x46 &&
            imageBytes[3] == 0x38 && (imageBytes[4] == 0x37 || imageBytes[4] == 0x39) && imageBytes[5] == 0x61) {
            return true;
        }

        // BMP文件头
        if (imageBytes.length >= 2 &&
            imageBytes[0] == 0x42 && imageBytes[1] == 0x4D) {
            return true;
        }

        // WEBP文件头
        if (imageBytes.length >= 12 &&
            imageBytes[0] == 0x52 && imageBytes[1] == 0x49 && imageBytes[2] == 0x46 && imageBytes[3] == 0x46 &&
            imageBytes[8] == 0x57 && imageBytes[9] == 0x45 && imageBytes[10] == 0x42 && imageBytes[11] == 0x50) {
            return true;
        }

        return false;
    }

    /**
     * 综合验证图片文件
     *
     * @param filename 文件名
     * @param mimeType MIME类型
     * @param imageBytes 图片字节数组
     * @param maxFileSize 最大文件大小（可选）
     * @param maxWidth 最大宽度（可选）
     * @param maxHeight 最大高度（可选）
     * @return 验证结果
     */
    public static ValidationResult validateImage(String filename, String mimeType, byte[] imageBytes,
                                               Long maxFileSize, Integer maxWidth, Integer maxHeight) {
        ValidationResult result = new ValidationResult();

        LoggingUtil.info(logger, "开始综合验证图片文件: {}", filename);

        // 验证文件名
        if (!isSafeFilename(filename)) {
            result.addError("文件名不安全或格式不正确");
        }

        // 验证格式
        if (!isValidImageFormat(filename)) {
            result.addError("不支持的文件格式");
        }

        // 验证MIME类型
        if (mimeType != null && !isValidMimeType(mimeType)) {
            result.addError("不支持的MIME类型: " + mimeType);
        }

        // 验证文件大小
        if (imageBytes != null) {
            if (!isValidFileSize(imageBytes.length, maxFileSize)) {
                result.addError("文件大小超出限制");
            }

            // 验证图片内容
            if (!isValidImageContent(imageBytes)) {
                result.addError("图片内容无效或损坏");
            }

            // 验证图片尺寸
            try {
                if (!isValidImageDimensions(imageBytes, maxWidth, maxHeight, null, null)) {
                    result.addError("图片尺寸超出限制");
                }
            } catch (BusinessException e) {
                result.addError("无法验证图片尺寸: " + e.getMessage());
            }
        }

        LoggingUtil.info(logger, "图片文件验证完成: {} - {}", filename, result.isValid() ? "通过" : "失败");
        return result;
    }

    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid = true;
        private StringBuilder errors = new StringBuilder();

        public void addError(String error) {
            this.valid = false;
            if (errors.length() > 0) {
                errors.append("; ");
            }
            errors.append(error);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrors() {
            return errors.toString();
        }

        @Override
        public String toString() {
            return "ValidationResult{" +
                    "valid=" + valid +
                    ", errors='" + errors + '\'' +
                    '}';
        }
    }
}

