package com.honyrun.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 限流配置属性类
 * 统一管理多维度限流参数配置，支持全局、IP、用户、端点级别的限流设置
 *
 * @author Mr.Rey Copyright © 2025
 * @since 2025-07-01 15:45:00
 * @version 1.0.0
 * @created 2025-07-01 15:45:00
 * @modified 2025-07-01 15:45:00
 */
@Component
@ConfigurationProperties(prefix = "honyrun.rate-limit")
public class RateLimitProperties {

    /**
     * 是否启用限流功能
     * 必须从配置文件获取：honyrun.rate-limit.enabled
     */
    private boolean enabled;

    /**
     * 全局限流配置
     * 必须从配置文件获取：honyrun.rate-limit.global
     */
    private DimensionConfig global;

    /**
     * IP级限流配置
     * 必须从配置文件获取：honyrun.rate-limit.ip
     */
    private DimensionConfig ip;

    /**
     * 用户级限流配置
     * 必须从配置文件获取：honyrun.rate-limit.user
     */
    private DimensionConfig user;

    /**
     * 端点级限流配置
     * 必须从配置文件获取：honyrun.rate-limit.endpoint
     */
    private DimensionConfig endpoint;

    /**
     * 统计信息过期时间（小时）
     * 必须从配置文件获取：honyrun.rate-limit.stats-expiration-hours
     */
    private int statsExpirationHours;

    /**
     * 限流维度配置类
     */
    public static class DimensionConfig {
        /**
         * 是否启用该维度的限流
         * 必须从配置文件获取：honyrun.rate-limit.{dimension}.enabled
         */
        private boolean enabled;

        /**
         * 每秒允许的请求数
         * 必须从配置文件获取：honyrun.rate-limit.{dimension}.requests-per-second
         */
        private int requestsPerSecond;

        /**
         * 突发容量（令牌桶大小）
         * 必须从配置文件获取：honyrun.rate-limit.{dimension}.burst-capacity
         */
        private int burstCapacity;

        /**
         * 时间窗口大小（秒）
         * 必须从配置文件获取：honyrun.rate-limit.{dimension}.time-window-seconds
         */
        private int timeWindowSeconds;

        /**
         * 限流算法类型
         * 必须从配置文件获取：honyrun.rate-limit.{dimension}.algorithm
         */
        private AlgorithmType algorithm;

        /**
         * 默认构造函数
         */
        public DimensionConfig() {}

        /**
         * 带参数构造函数
         *
         * @param enabled 是否启用
         * @param requestsPerSecond 每秒请求数
         * @param burstCapacity 突发容量
         */
        public DimensionConfig(boolean enabled, int requestsPerSecond, int burstCapacity) {
            this.enabled = enabled;
            this.requestsPerSecond = requestsPerSecond;
            this.burstCapacity = burstCapacity;
        }

        // Getter和Setter方法
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getRequestsPerSecond() {
            return requestsPerSecond;
        }

        public void setRequestsPerSecond(int requestsPerSecond) {
            this.requestsPerSecond = requestsPerSecond;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }

        public int getTimeWindowSeconds() {
            return timeWindowSeconds;
        }

        public void setTimeWindowSeconds(int timeWindowSeconds) {
            this.timeWindowSeconds = timeWindowSeconds;
        }

        public AlgorithmType getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(AlgorithmType algorithm) {
            this.algorithm = algorithm;
        }
    }

    /**
     * 限流算法类型枚举
     */
    public enum AlgorithmType {
        /**
         * 令牌桶算法
         */
        TOKEN_BUCKET,

        /**
         * 漏桶算法
         */
        LEAKY_BUCKET,

        /**
         * 滑动窗口算法
         */
        SLIDING_WINDOW,

        /**
         * 固定窗口算法
         */
        FIXED_WINDOW
    }

    // 主类的Getter和Setter方法
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public DimensionConfig getGlobal() {
        return global;
    }

    public void setGlobal(DimensionConfig global) {
        this.global = global;
    }

    public DimensionConfig getIp() {
        return ip;
    }

    public void setIp(DimensionConfig ip) {
        this.ip = ip;
    }

    public DimensionConfig getUser() {
        return user;
    }

    public void setUser(DimensionConfig user) {
        this.user = user;
    }

    public DimensionConfig getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(DimensionConfig endpoint) {
        this.endpoint = endpoint;
    }

    public int getStatsExpirationHours() {
        return statsExpirationHours;
    }

    public void setStatsExpirationHours(int statsExpirationHours) {
        this.statsExpirationHours = statsExpirationHours;
    }

    /**
     * 获取指定维度的配置
     *
     * @param dimension 限流维度
     * @return 对应的配置对象
     */
    public DimensionConfig getDimensionConfig(String dimension) {
        switch (dimension.toLowerCase()) {
            case "global":
                return global;
            case "ip":
                return ip;
            case "user":
                return user;
            case "endpoint":
                return endpoint;
            default:
                throw new IllegalArgumentException("未知的限流维度: " + dimension);
        }
    }

    /**
     * 验证配置的有效性
     *
     * @return 配置是否有效
     */
    public boolean isValid() {
        return validateDimensionConfig(global) &&
               validateDimensionConfig(ip) &&
               validateDimensionConfig(user) &&
               validateDimensionConfig(endpoint);
    }

    /**
     * 验证维度配置的有效性
     *
     * @param config 维度配置
     * @return 配置是否有效
     */
    private boolean validateDimensionConfig(DimensionConfig config) {
        return config.getRequestsPerSecond() > 0 &&
               config.getBurstCapacity() >= config.getRequestsPerSecond() &&
               config.getTimeWindowSeconds() > 0;
    }

    @Override
    public String toString() {
        return "RateLimitProperties{" +
                "enabled=" + enabled +
                ", global=" + global.getRequestsPerSecond() + "/" + global.getBurstCapacity() +
                ", ip=" + ip.getRequestsPerSecond() + "/" + ip.getBurstCapacity() +
                ", user=" + user.getRequestsPerSecond() + "/" + user.getBurstCapacity() +
                ", endpoint=" + endpoint.getRequestsPerSecond() + "/" + endpoint.getBurstCapacity() +
                '}';
    }
}

