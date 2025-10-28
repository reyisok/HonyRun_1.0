package com.honyrun.config.properties;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 压缩配置属性类
 * 管理HTTP响应压缩相关配置参数，支持多种压缩算法和MIME类型配置
 *
 * 【重要】：所有配置值必须从环境配置文件中读取
 * 配置文件路径：
 * - 开发环境：application-dev.properties
 * - 测试环境：application-test.properties
 * - 生产环境：application-prod.properties
 *
 * @author Mr.Rey
 * @since 2025-07-01 16:00:00
 * @version 1.0.1 - 移除硬编码，严格遵循统一配置管理规范
 *          Copyright © 2025 HonyRun. All rights reserved.
 * @created 2025-07-01 16:00:00
 * @modified 2025-10-27 12:26:43
 */
@Component
@ConfigurationProperties(prefix = "honyrun.compression")
public class CompressionProperties {

    /**
     * 是否启用压缩功能
     * 必须从配置文件获取：honyrun.compression.enabled
     */
    private boolean enabled;

    /**
     * 压缩算法配置
     * 必须从配置文件获取：honyrun.compression.algorithm.*
     */
    private AlgorithmConfig algorithm;

    /**
     * 响应大小配置
     * 必须从配置文件获取：honyrun.compression.size.*
     */
    private SizeConfig size;

    /**
     * MIME类型配置
     * 必须从配置文件获取：honyrun.compression.mime-types.*
     */
    private MimeTypeConfig mimeTypes;

    /**
     * 用户代理配置
     * 必须从配置文件获取：honyrun.compression.user-agent.*
     */
    private UserAgentConfig userAgent;

    /**
     * 性能配置
     * 必须从配置文件获取：honyrun.compression.performance.*
     */
    private PerformanceConfig performance;

    /**
     * 压缩算法配置类
     */
    public static class AlgorithmConfig {
        /**
         * 默认压缩算法
         * 必须从配置文件获取：honyrun.compression.algorithm.default-algorithm
         */
        private CompressionAlgorithm defaultAlgorithm;

        /**
         * 支持的压缩算法列表
         * 必须从配置文件获取：honyrun.compression.algorithm.supported-algorithms
         */
        private Set<CompressionAlgorithm> supportedAlgorithms;

        /**
         * GZIP压缩级别（1-9，9为最高压缩率）
         * 必须从配置文件获取：honyrun.compression.algorithm.gzip-level
         */
        private int gzipLevel;

        /**
         * Brotli压缩级别（0-11，11为最高压缩率）
         * 必须从配置文件获取：honyrun.compression.algorithm.brotli-level
         */
        private int brotliLevel;

        // Getter和Setter方法
        public CompressionAlgorithm getDefaultAlgorithm() {
            return defaultAlgorithm;
        }

        public void setDefaultAlgorithm(CompressionAlgorithm defaultAlgorithm) {
            this.defaultAlgorithm = defaultAlgorithm;
        }

        public Set<CompressionAlgorithm> getSupportedAlgorithms() {
            return supportedAlgorithms;
        }

        public void setSupportedAlgorithms(Set<CompressionAlgorithm> supportedAlgorithms) {
            this.supportedAlgorithms = supportedAlgorithms;
        }

        public int getGzipLevel() {
            return gzipLevel;
        }

        public void setGzipLevel(int gzipLevel) {
            this.gzipLevel = Math.max(1, Math.min(9, gzipLevel));
        }

        public int getBrotliLevel() {
            return brotliLevel;
        }

        public void setBrotliLevel(int brotliLevel) {
            this.brotliLevel = Math.max(0, Math.min(11, brotliLevel));
        }
    }

    /**
     * 响应大小配置类
     */
    public static class SizeConfig {
        /**
         * 最小压缩响应大小（字节）
         * 必须从配置文件获取：honyrun.compression.size.min-response-size
         */
        private long minResponseSize;

        /**
         * 最大压缩响应大小（字节）
         * 必须从配置文件获取：honyrun.compression.size.max-response-size
         */
        private long maxResponseSize;

        /**
         * 是否启用响应大小检查
         * 必须从配置文件获取：honyrun.compression.size.size-check-enabled
         */
        private boolean sizeCheckEnabled;

        // Getter和Setter方法
        public long getMinResponseSize() {
            return minResponseSize;
        }

        public void setMinResponseSize(long minResponseSize) {
            this.minResponseSize = minResponseSize;
        }

        public long getMaxResponseSize() {
            return maxResponseSize;
        }

        public void setMaxResponseSize(long maxResponseSize) {
            this.maxResponseSize = maxResponseSize;
        }

        public boolean isSizeCheckEnabled() {
            return sizeCheckEnabled;
        }

        public void setSizeCheckEnabled(boolean sizeCheckEnabled) {
            this.sizeCheckEnabled = sizeCheckEnabled;
        }
    }

    /**
     * MIME类型配置类
     */
    public static class MimeTypeConfig {
        /**
         * 支持压缩的MIME类型
         * 必须从配置文件获取：honyrun.compression.mime-types.compressible-types
         */
        private Set<String> compressibleTypes;

        /**
         * 排除压缩的MIME类型
         * 必须从配置文件获取：honyrun.compression.mime-types.excluded-types
         */
        private Set<String> excludedTypes;

        // Getter和Setter方法
        public Set<String> getCompressibleTypes() {
            return compressibleTypes;
        }

        public void setCompressibleTypes(Set<String> compressibleTypes) {
            this.compressibleTypes = compressibleTypes;
        }

        public Set<String> getExcludedTypes() {
            return excludedTypes;
        }

        public void setExcludedTypes(Set<String> excludedTypes) {
            this.excludedTypes = excludedTypes;
        }

        /**
         * 检查MIME类型是否可压缩
         *
         * @param mimeType MIME类型
         * @return 是否可压缩
         */
        public boolean isCompressible(String mimeType) {
            if (mimeType == null) {
                return false;
            }

            // 检查是否在排除列表中
            for (String excluded : excludedTypes) {
                if (excluded.endsWith("*")) {
                    String prefix = excluded.substring(0, excluded.length() - 1);
                    if (mimeType.startsWith(prefix)) {
                        return false;
                    }
                } else if (mimeType.equals(excluded)) {
                    return false;
                }
            }

            // 检查是否在可压缩列表中
            return compressibleTypes.contains(mimeType) || mimeType.startsWith("text/");
        }
    }

    /**
     * 用户代理配置类
     */
    public static class UserAgentConfig {
        /**
         * 排除压缩的用户代理列表
         * 必须从配置文件获取：honyrun.compression.user-agent.excluded-user-agents
         */
        private Set<String> excludedUserAgents;

        /**
         * 是否启用用户代理检查
         * 必须从配置文件获取：honyrun.compression.user-agent.user-agent-check-enabled
         */
        private boolean userAgentCheckEnabled;

        // Getter和Setter方法
        public Set<String> getExcludedUserAgents() {
            return excludedUserAgents;
        }

        public void setExcludedUserAgents(Set<String> excludedUserAgents) {
            this.excludedUserAgents = excludedUserAgents;
        }

        public boolean isUserAgentCheckEnabled() {
            return userAgentCheckEnabled;
        }

        public void setUserAgentCheckEnabled(boolean userAgentCheckEnabled) {
            this.userAgentCheckEnabled = userAgentCheckEnabled;
        }

        /**
         * 检查用户代理是否应该排除压缩
         *
         * @param userAgent 用户代理字符串
         * @return 是否应该排除压缩
         */
        public boolean shouldExclude(String userAgent) {
            if (!userAgentCheckEnabled || userAgent == null) {
                return false;
            }

            return excludedUserAgents.stream()
                    .anyMatch(excluded -> userAgent.contains(excluded));
        }
    }

    /**
     * 性能配置类
     */
    public static class PerformanceConfig {
        /**
         * 压缩缓冲区大小（字节）
         * 必须从配置文件获取：honyrun.compression.performance.buffer-size
         */
        private int bufferSize;

        /**
         * 压缩超时时间（毫秒）
         * 必须从配置文件获取：honyrun.compression.performance.compression-timeout
         */
        private long compressionTimeout;

        /**
         * 是否启用压缩缓存
         * 必须从配置文件获取：honyrun.compression.performance.cache-enabled
         */
        private boolean cacheEnabled;

        /**
         * 缓存大小
         * 必须从配置文件获取：honyrun.compression.performance.cache-size
         */
        private int cacheSize;

        /**
         * 缓存TTL（秒）
         * 必须从配置文件获取：honyrun.compression.performance.cache-ttl
         */
        private long cacheTtl;

        // Getter和Setter方法
        public int getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        public long getCompressionTimeout() {
            return compressionTimeout;
        }

        public void setCompressionTimeout(long compressionTimeout) {
            this.compressionTimeout = compressionTimeout;
        }

        public boolean isCacheEnabled() {
            return cacheEnabled;
        }

        public void setCacheEnabled(boolean cacheEnabled) {
            this.cacheEnabled = cacheEnabled;
        }

        public int getCacheSize() {
            return cacheSize;
        }

        public void setCacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
        }

        public long getCacheTtl() {
            return cacheTtl;
        }

        public void setCacheTtl(long cacheTtl) {
            this.cacheTtl = cacheTtl;
        }
    }

    /**
     * 压缩算法枚举
     */
    public enum CompressionAlgorithm {
        /**
         * GZIP压缩
         */
        GZIP("gzip"),

        /**
         * DEFLATE压缩
         */
        DEFLATE("deflate"),

        /**
         * Brotli压缩
         */
        BROTLI("br");

        private final String encoding;

        CompressionAlgorithm(String encoding) {
            this.encoding = encoding;
        }

        public String getEncoding() {
            return encoding;
        }
    }

    // 主类的Getter和Setter方法
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public AlgorithmConfig getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(AlgorithmConfig algorithm) {
        this.algorithm = algorithm;
    }

    public SizeConfig getSize() {
        return size;
    }

    public void setSize(SizeConfig size) {
        this.size = size;
    }

    public MimeTypeConfig getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(MimeTypeConfig mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public UserAgentConfig getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(UserAgentConfig userAgent) {
        this.userAgent = userAgent;
    }

    public PerformanceConfig getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceConfig performance) {
        this.performance = performance;
    }

    /**
     * 验证配置的有效性
     *
     * @return 配置是否有效
     */
    public boolean isValid() {
        return size.getMinResponseSize() >= 0 &&
                size.getMaxResponseSize() > size.getMinResponseSize() &&
                algorithm.getGzipLevel() >= 1 && algorithm.getGzipLevel() <= 9 &&
                algorithm.getBrotliLevel() >= 0 && algorithm.getBrotliLevel() <= 11 &&
                performance.getBufferSize() > 0 &&
                performance.getCompressionTimeout() > 0;
    }

    @Override
    public String toString() {
        return "CompressionProperties{" +
                "enabled=" + enabled +
                ", algorithm=" + algorithm.getDefaultAlgorithm() +
                ", minSize=" + size.getMinResponseSize() +
                ", maxSize=" + size.getMaxResponseSize() +
                ", compressibleTypes=" + mimeTypes.getCompressibleTypes().size() +
                '}';
    }
}
