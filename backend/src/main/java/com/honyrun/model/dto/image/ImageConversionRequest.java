package com.honyrun.model.dto.image;

/**
 * 图片转换请求DTO
 * 封装图片转换操作的请求参数
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 请求参数:
 * - 目标格式设置
 * - 图片质量控制
 * - 尺寸调整参数
 * - 处理选项配置
 */
public class ImageConversionRequest {

    /**
     * 目标图片格式
     * 支持: jpeg, png, gif, bmp, webp
     * 验证规则: 不能为空，必须是支持的格式之一
     */
    private String targetFormat;

    /**
     * 图片质量 (0.0-1.0)
     * 仅对JPEG格式有效
     * 验证规则: 范围0.0-1.0
     */
    private Float quality;

    /**
     * 目标宽度（像素）
     * null表示保持原始宽度或按比例计算
     * 验证规则: 大于0，不超过8192像素
     */
    private Integer width;

    /**
     * 目标高度（像素）
     * null表示保持原始高度或按比例计算
     * 验证规则: 大于0，不超过8192像素
     */
    private Integer height;

    /**
     * 是否保持宽高比
     * 默认为true
     */
    private Boolean keepAspectRatio = true;

    /**
     * 是否进行图片优化
     * 包括去除EXIF信息等
     */
    private Boolean optimize = false;

    /**
     * 旋转角度
     * 支持: 0, 90, 180, 270
     * 验证规则: 范围0-270，必须是90的倍数
     */
    private Integer rotationAngle = 0;

    /**
     * 是否水平翻转
     */
    private Boolean flipHorizontal = false;

    /**
     * 是否垂直翻转
     */
    private Boolean flipVertical = false;

    /**
     * 水印文字
     * 验证规则: 长度不超过100个字符
     */
    private String watermarkText;

    /**
     * 水印X坐标
     * 验证规则: 不能小于0
     */
    private Integer watermarkX;

    /**
     * 水印Y坐标
     * 验证规则: 不能小于0
     */
    private Integer watermarkY;

    /**
     * 水印字体大小
     * 验证规则: 范围8-72
     */
    private Integer watermarkFontSize = 16;

    /**
     * 水印透明度 (0.0-1.0)
     * 验证规则: 范围0.0-1.0
     */
    private Float watermarkAlpha = 0.5f;

    /**
     * 输出文件名前缀
     * 验证规则: 长度不超过50个字符
     */
    private String outputPrefix;

    /**
     * 是否批量处理
     */
    private Boolean batchMode = false;

    // 构造函数
    public ImageConversionRequest() {}

    public ImageConversionRequest(String targetFormat) {
        this.targetFormat = targetFormat;
    }

    public ImageConversionRequest(String targetFormat, Float quality, Integer width, Integer height) {
        this.targetFormat = targetFormat;
        this.quality = quality;
        this.width = width;
        this.height = height;
    }

    // Getter和Setter方法
    public String getTargetFormat() {
        return targetFormat;
    }

    public void setTargetFormat(String targetFormat) {
        this.targetFormat = targetFormat;
    }

    public Float getQuality() {
        return quality;
    }

    public void setQuality(Float quality) {
        this.quality = quality;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Boolean getKeepAspectRatio() {
        return keepAspectRatio;
    }

    public void setKeepAspectRatio(Boolean keepAspectRatio) {
        this.keepAspectRatio = keepAspectRatio;
    }

    public Boolean getOptimize() {
        return optimize;
    }

    public void setOptimize(Boolean optimize) {
        this.optimize = optimize;
    }

    public Integer getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(Integer rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public Boolean getFlipHorizontal() {
        return flipHorizontal;
    }

    public void setFlipHorizontal(Boolean flipHorizontal) {
        this.flipHorizontal = flipHorizontal;
    }

    public Boolean getFlipVertical() {
        return flipVertical;
    }

    public void setFlipVertical(Boolean flipVertical) {
        this.flipVertical = flipVertical;
    }

    public String getWatermarkText() {
        return watermarkText;
    }

    public void setWatermarkText(String watermarkText) {
        this.watermarkText = watermarkText;
    }

    public Integer getWatermarkX() {
        return watermarkX;
    }

    public void setWatermarkX(Integer watermarkX) {
        this.watermarkX = watermarkX;
    }

    public Integer getWatermarkY() {
        return watermarkY;
    }

    public void setWatermarkY(Integer watermarkY) {
        this.watermarkY = watermarkY;
    }

    public Integer getWatermarkFontSize() {
        return watermarkFontSize;
    }

    public void setWatermarkFontSize(Integer watermarkFontSize) {
        this.watermarkFontSize = watermarkFontSize;
    }

    public Float getWatermarkAlpha() {
        return watermarkAlpha;
    }

    public void setWatermarkAlpha(Float watermarkAlpha) {
        this.watermarkAlpha = watermarkAlpha;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public void setOutputPrefix(String outputPrefix) {
        this.outputPrefix = outputPrefix;
    }

    public Boolean getBatchMode() {
        return batchMode;
    }

    public void setBatchMode(Boolean batchMode) {
        this.batchMode = batchMode;
    }

    /**
     * 验证旋转角度是否有效
     */
    public boolean isValidRotationAngle() {
        return rotationAngle != null && rotationAngle % 90 == 0 && rotationAngle >= 0 && rotationAngle <= 270;
    }

    /**
     * 是否需要调整尺寸
     */
    public boolean needsResize() {
        return width != null || height != null;
    }

    /**
     * 是否需要旋转
     */
    public boolean needsRotation() {
        return rotationAngle != null && rotationAngle > 0;
    }

    /**
     * 是否需要翻转
     */
    public boolean needsFlip() {
        return (flipHorizontal != null && flipHorizontal) || (flipVertical != null && flipVertical);
    }

    /**
     * 是否需要添加水印
     */
    public boolean needsWatermark() {
        return watermarkText != null && !watermarkText.trim().isEmpty() &&
               watermarkX != null && watermarkY != null;
    }

    @Override
    public String toString() {
        return "ImageConversionRequest{" +
                "targetFormat='" + targetFormat + '\'' +
                ", quality=" + quality +
                ", width=" + width +
                ", height=" + height +
                ", keepAspectRatio=" + keepAspectRatio +
                ", optimize=" + optimize +
                ", rotationAngle=" + rotationAngle +
                ", flipHorizontal=" + flipHorizontal +
                ", flipVertical=" + flipVertical +
                ", watermarkText='" + watermarkText + '\'' +
                ", watermarkX=" + watermarkX +
                ", watermarkY=" + watermarkY +
                ", watermarkFontSize=" + watermarkFontSize +
                ", watermarkAlpha=" + watermarkAlpha +
                ", outputPrefix='" + outputPrefix + '\'' +
                ", batchMode=" + batchMode +
                '}';
    }
}

