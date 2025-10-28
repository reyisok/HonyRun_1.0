package com.honyrun.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 安全输入限制配置属性类
 * 统一管理所有输入长度限制和安全验证参数，确保不同环境配置的一致性
 *
 * @author Mr.Rey
 * @since 2025-07-01 16:00:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 * @created 2025-07-01 16:00:00
 * @modified 2025-07-01 16:00:00
 */
@Component
@ConfigurationProperties(prefix = "honyrun.security.input-limits")
public class SecurityInputLimitProperties {

    /**
     * 请求相关限制
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private RequestLimits request;

    /**
     * 认证相关限制
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private AuthLimits auth;

    /**
     * 文件上传相关限制
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private FileLimits file;

    /**
     * HTTP相关限制
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private HttpLimits http;

    /**
     * 内容相关限制
     * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
     */
    private ContentLimits content;

    // Getter和Setter方法
    public RequestLimits getRequest() {
        return request;
    }

    public void setRequest(RequestLimits request) {
        this.request = request;
    }

    public AuthLimits getAuth() {
        return auth;
    }

    public void setAuth(AuthLimits auth) {
        this.auth = auth;
    }

    public FileLimits getFile() {
        return file;
    }

    public void setFile(FileLimits file) {
        this.file = file;
    }

    public HttpLimits getHttp() {
        return http;
    }

    public void setHttp(HttpLimits http) {
        this.http = http;
    }

    public ContentLimits getContent() {
        return content;
    }

    public void setContent(ContentLimits content) {
        this.content = content;
    }

    /**
     * 请求相关限制配置类
     *
     * 单位约定：
     * - 体积相关（如请求体大小、扫描大小）统一为字节（bytes）
     * - 长度相关（如路径、参数）统一为字符（characters），按 UTF-16 字符数计算
     */
    public static class RequestLimits {
        /**
         * 最大请求体大小（字节，bytes）。用于限制可接受的请求体总体大小。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Long maxRequestSize;

        /**
         * 最大查询参数数量（个）。用于限制查询参数的总个数。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxQueryParams;

        /**
         * 单个参数最大长度（字符，characters）。按 UTF-16 字符数计算。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxParamLength;

        /**
         * 最大路径长度（字符，characters）。包含完整路径的总长度。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxPathLength;

        /**
         * 单个路径段最大长度（字符，characters）。用于限制每个路径段的长度。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxPathSegmentLength;

        /**
         * 请求体安全扫描最大大小（字节，bytes）。超过该阈值的部分跳过深度扫描以保障性能。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Long maxBodyScanSize;

        // Getter和Setter方法
        public long getMaxRequestSize() {
            return maxRequestSize;
        }

        public void setMaxRequestSize(long maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
        }

        public int getMaxQueryParams() {
            return maxQueryParams;
        }

        public void setMaxQueryParams(int maxQueryParams) {
            this.maxQueryParams = maxQueryParams;
        }

        public int getMaxParamLength() {
            return maxParamLength;
        }

        public void setMaxParamLength(int maxParamLength) {
            this.maxParamLength = maxParamLength;
        }

        public int getMaxPathLength() {
            return maxPathLength;
        }

        public void setMaxPathLength(int maxPathLength) {
            this.maxPathLength = maxPathLength;
        }

        public int getMaxPathSegmentLength() {
            return maxPathSegmentLength;
        }

        public void setMaxPathSegmentLength(int maxPathSegmentLength) {
            this.maxPathSegmentLength = maxPathSegmentLength;
        }

        public long getMaxBodyScanSize() {
            return maxBodyScanSize;
        }

        public void setMaxBodyScanSize(long maxBodyScanSize) {
            this.maxBodyScanSize = maxBodyScanSize;
        }
    }

    /**
     * 认证相关限制配置类
     *
     * 单位约定：除体积外，长度类字段统一为字符（characters）。
     */
    public static class AuthLimits {
        /**
         * JWT Token 最大长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxTokenLength;

        /**
         * 用户名最小长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer minUsernameLength;

        /**
         * 用户名最大长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxUsernameLength;

        /**
         * 密码最小长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer minPasswordLength;

        /**
         * 密码最大长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxPasswordLength;

        /**
         * JWT Subject 最大长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxSubjectLength;

        /**
         * 设备 ID 最大长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxDeviceIdLength;

        // Getter和Setter方法
        public int getMaxTokenLength() {
            return maxTokenLength;
        }

        public void setMaxTokenLength(int maxTokenLength) {
            this.maxTokenLength = maxTokenLength;
        }

        public int getMinUsernameLength() {
            return minUsernameLength;
        }

        public void setMinUsernameLength(int minUsernameLength) {
            this.minUsernameLength = minUsernameLength;
        }

        public int getMaxUsernameLength() {
            return maxUsernameLength;
        }

        public void setMaxUsernameLength(int maxUsernameLength) {
            this.maxUsernameLength = maxUsernameLength;
        }

        public int getMinPasswordLength() {
            return minPasswordLength;
        }

        public void setMinPasswordLength(int minPasswordLength) {
            this.minPasswordLength = minPasswordLength;
        }

        public int getMaxPasswordLength() {
            return maxPasswordLength;
        }

        public void setMaxPasswordLength(int maxPasswordLength) {
            this.maxPasswordLength = maxPasswordLength;
        }

        public int getMaxSubjectLength() {
            return maxSubjectLength;
        }

        public void setMaxSubjectLength(int maxSubjectLength) {
            this.maxSubjectLength = maxSubjectLength;
        }

        public int getMaxDeviceIdLength() {
            return maxDeviceIdLength;
        }

        public void setMaxDeviceIdLength(int maxDeviceIdLength) {
            this.maxDeviceIdLength = maxDeviceIdLength;
        }
    }

    /**
     * 文件上传相关限制配置类
     *
     * 单位约定：体积相关统一为字节（bytes），名称长度为字符（characters）。
     */
    public static class FileLimits {
        /**
         * 单个文件最大大小（字节，bytes）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Long maxFileSize;

        /**
         * 内存中文件最大大小（字节，bytes）。用于限制内存中持有的文件部件大小。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Long maxInMemorySize;

        /**
         * 磁盘使用最大大小（字节，bytes）。单个部件在磁盘的最大占用。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Long maxDiskUsagePerPart;

        /**
         * 最大文件数量（个）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxParts;

        /**
         * 文件名最大长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxFilenameLength;

        /**
         * 文件扩展名最大长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxExtensionLength;

        // Getter和Setter方法
        public long getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(long maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public long getMaxInMemorySize() {
            return maxInMemorySize;
        }

        public void setMaxInMemorySize(long maxInMemorySize) {
            this.maxInMemorySize = maxInMemorySize;
        }

        public long getMaxDiskUsagePerPart() {
            return maxDiskUsagePerPart;
        }

        public void setMaxDiskUsagePerPart(long maxDiskUsagePerPart) {
            this.maxDiskUsagePerPart = maxDiskUsagePerPart;
        }

        public int getMaxParts() {
            return maxParts;
        }

        public void setMaxParts(int maxParts) {
            this.maxParts = maxParts;
        }

        public int getMaxFilenameLength() {
            return maxFilenameLength;
        }

        public void setMaxFilenameLength(int maxFilenameLength) {
            this.maxFilenameLength = maxFilenameLength;
        }

        public int getMaxExtensionLength() {
            return maxExtensionLength;
        }

        public void setMaxExtensionLength(int maxExtensionLength) {
            this.maxExtensionLength = maxExtensionLength;
        }
    }

    /**
     * HTTP相关限制配置类
     *
     * 单位约定：
     * - 头部总大小、Cookie 总大小为字节（bytes）
     * - 单个头部值、User-Agent、Referer、单个 Cookie 项为字符（characters）
     */
    public static class HttpLimits {
        /**
         * HTTP 头部总大小限制（字节，bytes）。所有头部序列化后的总体大小。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Long maxHeadersSize;

        /**
         * 单个 HTTP 头部值最大长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxHeaderLength;

        /**
         * Cookie 总大小限制（字节，bytes）。指 `Cookie` 头部的整体大小。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Long maxCookieSize;

        /**
         * 单个 Cookie 项最大长度（字符，characters）。单个名值对的最大长度。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxSingleCookieLength;

        /**
         * User-Agent 最大长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxUserAgentLength;

        /**
         * Referer 最大长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxRefererLength;

        // Getter和Setter方法
        public long getMaxHeadersSize() {
            return maxHeadersSize;
        }

        public void setMaxHeadersSize(long maxHeadersSize) {
            this.maxHeadersSize = maxHeadersSize;
        }

        public int getMaxHeaderLength() {
            return maxHeaderLength;
        }

        public void setMaxHeaderLength(int maxHeaderLength) {
            this.maxHeaderLength = maxHeaderLength;
        }

        public long getMaxCookieSize() {
            return maxCookieSize;
        }

        public void setMaxCookieSize(long maxCookieSize) {
            this.maxCookieSize = maxCookieSize;
        }

        public int getMaxSingleCookieLength() {
            return maxSingleCookieLength;
        }

        public void setMaxSingleCookieLength(int maxSingleCookieLength) {
            this.maxSingleCookieLength = maxSingleCookieLength;
        }

        public int getMaxUserAgentLength() {
            return maxUserAgentLength;
        }

        public void setMaxUserAgentLength(int maxUserAgentLength) {
            this.maxUserAgentLength = maxUserAgentLength;
        }

        public int getMaxRefererLength() {
            return maxRefererLength;
        }

        public void setMaxRefererLength(int maxRefererLength) {
            this.maxRefererLength = maxRefererLength;
        }
    }

    /**
     * 内容相关限制配置类
     *
     * 单位约定：
     * - 深度与数量为计数（count）
     * - 字符串类长度为字符（characters）
     */
    public static class ContentLimits {
        /**
         * JSON 内容最大深度（层级计数，count）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxJsonDepth;

        /**
         * JSON 数组最大元素数量（count）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxJsonArraySize;

        /**
         * JSON 对象最大属性数量（count）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxJsonObjectProperties;

        /**
         * 字符串字段最大长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxStringFieldLength;

        /**
         * 文本内容最大长度（字符，characters）。
         * 注意：必须从环境特定的.properties文件或环境变量中读取配置值
         */
        private Integer maxTextContentLength;

        // Getter和Setter方法
        public int getMaxJsonDepth() {
            return maxJsonDepth;
        }

        public void setMaxJsonDepth(int maxJsonDepth) {
            this.maxJsonDepth = maxJsonDepth;
        }

        public int getMaxJsonArraySize() {
            return maxJsonArraySize;
        }

        public void setMaxJsonArraySize(int maxJsonArraySize) {
            this.maxJsonArraySize = maxJsonArraySize;
        }

        public int getMaxJsonObjectProperties() {
            return maxJsonObjectProperties;
        }

        public void setMaxJsonObjectProperties(int maxJsonObjectProperties) {
            this.maxJsonObjectProperties = maxJsonObjectProperties;
        }

        public int getMaxStringFieldLength() {
            return maxStringFieldLength;
        }

        public void setMaxStringFieldLength(int maxStringFieldLength) {
            this.maxStringFieldLength = maxStringFieldLength;
        }

        public int getMaxTextContentLength() {
            return maxTextContentLength;
        }

        public void setMaxTextContentLength(int maxTextContentLength) {
            this.maxTextContentLength = maxTextContentLength;
        }
    }

    /**
     * 验证配置的有效性
     *
     * @return 配置是否有效
     */
    public boolean isValid() {
        return request.getMaxRequestSize() > 0 &&
               request.getMaxQueryParams() > 0 &&
               request.getMaxParamLength() > 0 &&
               auth.getMaxTokenLength() > 0 &&
               auth.getMinUsernameLength() > 0 &&
               auth.getMaxUsernameLength() > auth.getMinUsernameLength() &&
               auth.getMinPasswordLength() > 0 &&
               auth.getMaxPasswordLength() > auth.getMinPasswordLength() &&
               file.getMaxFileSize() > 0 &&
               http.getMaxHeadersSize() > 0 &&
               content.getMaxJsonDepth() > 0;
    }

    @Override
    public String toString() {
        return "SecurityInputLimitProperties{" +
                "request=" + request +
                ", auth=" + auth +
                ", file=" + file +
                ", http=" + http +
                ", content=" + content +
                '}';
    }
}

