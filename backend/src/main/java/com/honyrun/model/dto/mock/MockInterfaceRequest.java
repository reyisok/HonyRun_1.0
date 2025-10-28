package com.honyrun.model.dto.mock;

import java.util.Map;

/**
 * 模拟接口请求DTO
 * 封装模拟接口创建和配置的请求参数
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 请求参数:
 * - 接口基本信息配置
 * - 响应模板和参数
 * - 行为控制选项
 * - 统计和监控设置
 */
public class MockInterfaceRequest {

    /**
     * 接口名称
     * 验证规则: 不能为空，长度不超过100个字符
     */
    private String interfaceName;

    /**
     * 接口路径
     * 验证规则: 不能为空，长度不超过200个字符，必须以/开头
     */
    private String interfacePath;

    /**
     * 请求方法
     * 验证规则: 不能为空，必须是GET、POST、PUT、DELETE、PATCH、HEAD、OPTIONS中的一种
     */
    private String requestMethod;

    /**
     * 接口描述
     * 验证规则: 长度不超过500个字符
     */
    private String description;

    /**
     * 响应模板
     * 验证规则: 不能为空，长度不超过10000个字符
     */
    private String responseTemplate;

    /**
     * 响应状态码
     * 验证规则: 范围100-599
     */
    private Integer responseStatus = 200;

    /**
     * 响应延迟时间（毫秒）
     * 验证规则: 范围0-60000
     */
    private Integer delayTime = 0;

    /**
     * 是否启用接口
     */
    private Boolean enabled = true;

    /**
     * 接口状态
     * 验证规则: 必须是ACTIVE、INACTIVE、MAINTENANCE中的一种
     */
    private String status = "ACTIVE";

    /**
     * 接口分类
     * 验证规则: 长度不超过50个字符
     */
    private String category;

    /**
     * 是否启用日志记录
     */
    private Boolean logEnabled = true;

    /**
     * 响应内容类型
     * 验证规则: 长度不超过100个字符
     */
    private String contentType = "application/json";

    /**
     * 自定义响应头
     */
    private Map<String, String> customHeaders;

    /**
     * 请求参数验证规则
     */
    private Map<String, ParameterValidation> parameterValidations;

    /**
     * 条件响应配置
     * 根据请求参数返回不同的响应
     */
    private Map<String, ConditionalResponse> conditionalResponses;

    /**
     * 是否启用参数替换
     * 在响应模板中替换请求参数
     */
    private Boolean enableParameterReplacement = false;

    /**
     * 错误响应配置
     */
    private ErrorResponseConfig errorResponseConfig;

    /**
     * 限流配置
     */
    private RateLimitConfig rateLimitConfig;

    /**
     * 缓存配置
     */
    private CacheConfig cacheConfig;

    // 构造函数
    public MockInterfaceRequest() {}

    public MockInterfaceRequest(String interfaceName, String interfacePath, String requestMethod, String responseTemplate) {
        this.interfaceName = interfaceName;
        this.interfacePath = interfacePath;
        this.requestMethod = requestMethod;
        this.responseTemplate = responseTemplate;
    }

    // Getter和Setter方法
    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterfacePath() {
        return interfacePath;
    }

    public void setInterfacePath(String interfacePath) {
        this.interfacePath = interfacePath;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResponseTemplate() {
        return responseTemplate;
    }

    public void setResponseTemplate(String responseTemplate) {
        this.responseTemplate = responseTemplate;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Integer getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Integer delayTime) {
        this.delayTime = delayTime;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(Boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, String> getCustomHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }

    public Map<String, ParameterValidation> getParameterValidations() {
        return parameterValidations;
    }

    public void setParameterValidations(Map<String, ParameterValidation> parameterValidations) {
        this.parameterValidations = parameterValidations;
    }

    public Map<String, ConditionalResponse> getConditionalResponses() {
        return conditionalResponses;
    }

    public void setConditionalResponses(Map<String, ConditionalResponse> conditionalResponses) {
        this.conditionalResponses = conditionalResponses;
    }

    public Boolean getEnableParameterReplacement() {
        return enableParameterReplacement;
    }

    public void setEnableParameterReplacement(Boolean enableParameterReplacement) {
        this.enableParameterReplacement = enableParameterReplacement;
    }

    public ErrorResponseConfig getErrorResponseConfig() {
        return errorResponseConfig;
    }

    public void setErrorResponseConfig(ErrorResponseConfig errorResponseConfig) {
        this.errorResponseConfig = errorResponseConfig;
    }

    public RateLimitConfig getRateLimitConfig() {
        return rateLimitConfig;
    }

    public void setRateLimitConfig(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    public void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
    }

    /**
     * 参数验证配置类
     */
    public static class ParameterValidation {
        private Boolean required = false;
        private String type; // string, number, boolean, array, object
        private String pattern; // 正则表达式
        private Object defaultValue;
        private String description;

        // 构造函数和getter/setter方法
        public ParameterValidation() {}

        public Boolean getRequired() { return required; }
        public void setRequired(Boolean required) { this.required = required; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }

        public Object getDefaultValue() { return defaultValue; }
        public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 条件响应配置类
     */
    public static class ConditionalResponse {
        private String condition; // 条件表达式
        private String responseTemplate;
        private Integer responseStatus;
        private Map<String, String> headers;

        // 构造函数和getter/setter方法
        public ConditionalResponse() {}

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public String getResponseTemplate() { return responseTemplate; }
        public void setResponseTemplate(String responseTemplate) { this.responseTemplate = responseTemplate; }

        public Integer getResponseStatus() { return responseStatus; }
        public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }

        public Map<String, String> getHeaders() { return headers; }
        public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    }

    /**
     * 错误响应配置类
     */
    public static class ErrorResponseConfig {
        private Double errorRate = 0.0; // 错误率 (0.0-1.0)
        private String errorResponseTemplate;
        private Integer errorStatus = 500;

        // 构造函数和getter/setter方法
        public ErrorResponseConfig() {}

        public Double getErrorRate() { return errorRate; }
        public void setErrorRate(Double errorRate) { this.errorRate = errorRate; }

        public String getErrorResponseTemplate() { return errorResponseTemplate; }
        public void setErrorResponseTemplate(String errorResponseTemplate) { this.errorResponseTemplate = errorResponseTemplate; }

        public Integer getErrorStatus() { return errorStatus; }
        public void setErrorStatus(Integer errorStatus) { this.errorStatus = errorStatus; }
    }

    /**
     * 限流配置类
     */
    public static class RateLimitConfig {
        private Boolean enabled = false;
        private Integer requestsPerMinute = 60;
        private String limitExceededResponse;

        // 构造函数和getter/setter方法
        public RateLimitConfig() {}

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public Integer getRequestsPerMinute() { return requestsPerMinute; }
        public void setRequestsPerMinute(Integer requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }

        public String getLimitExceededResponse() { return limitExceededResponse; }
        public void setLimitExceededResponse(String limitExceededResponse) { this.limitExceededResponse = limitExceededResponse; }
    }

    /**
     * 缓存配置类
     */
    public static class CacheConfig {
        private Boolean enabled = false;
        private Integer ttlSeconds = 300; // 5分钟
        private String cacheKey; // 缓存键模板

        // 构造函数和getter/setter方法
        public CacheConfig() {}

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public Integer getTtlSeconds() { return ttlSeconds; }
        public void setTtlSeconds(Integer ttlSeconds) { this.ttlSeconds = ttlSeconds; }

        public String getCacheKey() { return cacheKey; }
        public void setCacheKey(String cacheKey) { this.cacheKey = cacheKey; }
    }

    @Override
    public String toString() {
        return "MockInterfaceRequest{" +
                "interfaceName='" + interfaceName + '\'' +
                ", interfacePath='" + interfacePath + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", description='" + description + '\'' +
                ", responseStatus=" + responseStatus +
                ", delayTime=" + delayTime +
                ", enabled=" + enabled +
                ", status='" + status + '\'' +
                ", category='" + category + '\'' +
                ", logEnabled=" + logEnabled +
                ", contentType='" + contentType + '\'' +
                ", enableParameterReplacement=" + enableParameterReplacement +
                '}';
    }
}

