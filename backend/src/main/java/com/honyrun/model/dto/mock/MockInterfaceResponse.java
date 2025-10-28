package com.honyrun.model.dto.mock;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 模拟接口响应DTO
 * 封装模拟接口操作的响应结果和统计信息
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 响应信息:
 * - 接口基本信息
 * - 执行结果状态
 * - 统计和监控数据
 * - 错误信息记录
 */
public class MockInterfaceResponse {

    /**
     * 接口ID
     */
    private Long id;

    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 接口路径
     */
    private String interfacePath;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 响应模板
     */
    private String responseTemplate;

    /**
     * 响应状态码
     */
    private Integer responseStatus;

    /**
     * 响应延迟时间（毫秒）
     */
    private Integer delayTime;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 接口状态
     */
    private String status;

    /**
     * 接口分类
     */
    private String category;

    /**
     * 是否启用日志记录
     */
    private Boolean logEnabled;

    /**
     * 响应内容类型
     */
    private String contentType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 更新者
     */
    private String updatedBy;

    /**
     * 总访问次数
     */
    private Long accessCount = 0L;

    /**
     * 成功访问次数
     */
    private Long successCount = 0L;

    /**
     * 失败访问次数
     */
    private Long failureCount = 0L;

    /**
     * 平均响应时间（毫秒）
     */
    private Long avgResponseTime = 0L;

    /**
     * 最大响应时间（毫秒）
     */
    private Long maxResponseTime = 0L;

    /**
     * 最小响应时间（毫秒）
     */
    private Long minResponseTime = 0L;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 成功率
     */
    private Double successRate = 0.0;

    /**
     * 自定义响应头
     */
    private Map<String, String> customHeaders;

    /**
     * 实际响应内容（用于测试结果）
     */
    private String actualResponse;

    /**
     * 实际响应状态码（用于测试结果）
     */
    private Integer actualResponseStatus;

    /**
     * 实际响应时间（毫秒，用于测试结果）
     */
    private Long actualResponseTime;

    /**
     * 测试执行时间
     */
    private LocalDateTime testExecutionTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 是否为测试响应
     */
    private Boolean isTestResponse = false;

    /**
     * 配置版本号
     */
    private Integer configVersion = 1;

    /**
     * 接口标签
     */
    private String[] tags;

    /**
     * 扩展属性
     */
    private Map<String, Object> extensions;

    // 构造函数
    public MockInterfaceResponse() {}

    public MockInterfaceResponse(Long id, String interfaceName, String interfacePath, String requestMethod) {
        this.id = id;
        this.interfaceName = interfaceName;
        this.interfacePath = interfacePath;
        this.requestMethod = requestMethod;
    }

    // 静态工厂方法
    public static MockInterfaceResponse success(Long id, String interfaceName, String interfacePath, String requestMethod) {
        MockInterfaceResponse response = new MockInterfaceResponse(id, interfaceName, interfacePath, requestMethod);
        response.enabled = true;
        response.status = "ACTIVE";
        return response;
    }

    public static MockInterfaceResponse testResult(String actualResponse, Integer actualResponseStatus, Long actualResponseTime) {
        MockInterfaceResponse response = new MockInterfaceResponse();
        response.actualResponse = actualResponse;
        response.actualResponseStatus = actualResponseStatus;
        response.actualResponseTime = actualResponseTime;
        response.testExecutionTime = LocalDateTime.now();
        response.isTestResponse = true;
        return response;
    }

    public static MockInterfaceResponse error(String errorMessage, String errorCode) {
        MockInterfaceResponse response = new MockInterfaceResponse();
        response.errorMessage = errorMessage;
        response.errorCode = errorCode;
        return response;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Long failureCount) {
        this.failureCount = failureCount;
    }

    public Long getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(Long avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public Long getMaxResponseTime() {
        return maxResponseTime;
    }

    public void setMaxResponseTime(Long maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
    }

    public Long getMinResponseTime() {
        return minResponseTime;
    }

    public void setMinResponseTime(Long minResponseTime) {
        this.minResponseTime = minResponseTime;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public Map<String, String> getCustomHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }

    public String getActualResponse() {
        return actualResponse;
    }

    public void setActualResponse(String actualResponse) {
        this.actualResponse = actualResponse;
    }

    public Integer getActualResponseStatus() {
        return actualResponseStatus;
    }

    public void setActualResponseStatus(Integer actualResponseStatus) {
        this.actualResponseStatus = actualResponseStatus;
    }

    public Long getActualResponseTime() {
        return actualResponseTime;
    }

    public void setActualResponseTime(Long actualResponseTime) {
        this.actualResponseTime = actualResponseTime;
    }

    public LocalDateTime getTestExecutionTime() {
        return testExecutionTime;
    }

    public void setTestExecutionTime(LocalDateTime testExecutionTime) {
        this.testExecutionTime = testExecutionTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Boolean getIsTestResponse() {
        return isTestResponse;
    }

    public void setIsTestResponse(Boolean isTestResponse) {
        this.isTestResponse = isTestResponse;
    }

    public Integer getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(Integer configVersion) {
        this.configVersion = configVersion;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }

    /**
     * 计算成功率
     */
    public void calculateSuccessRate() {
        if (accessCount != null && accessCount > 0 && successCount != null) {
            this.successRate = Math.round((double) successCount / accessCount * 10000.0) / 100.0; // 保留两位小数
        } else {
            this.successRate = 0.0;
        }
    }

    /**
     * 获取格式化的成功率
     */
    public String getFormattedSuccessRate() {
        return String.format("%.2f%%", successRate != null ? successRate : 0.0);
    }

    /**
     * 获取格式化的平均响应时间
     */
    public String getFormattedAvgResponseTime() {
        if (avgResponseTime == null || avgResponseTime == 0) {
            return "0ms";
        }
        if (avgResponseTime < 1000) {
            return avgResponseTime + "ms";
        } else {
            return String.format("%.2fs", avgResponseTime / 1000.0);
        }
    }

    /**
     * 获取接口健康状态
     */
    public String getHealthStatus() {
        if (!enabled) {
            return "DISABLED";
        }
        if (successRate == null || successRate >= 95.0) {
            return "HEALTHY";
        } else if (successRate >= 80.0) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }

    /**
     * 是否有错误
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }

    /**
     * 是否为活跃接口
     */
    public boolean isActive() {
        return enabled != null && enabled && "ACTIVE".equals(status);
    }

    /**
     * 获取接口完整标识
     */
    public String getFullIdentifier() {
        return String.format("%s %s", requestMethod, interfacePath);
    }

    @Override
    public String toString() {
        return "MockInterfaceResponse{" +
                "id=" + id +
                ", interfaceName='" + interfaceName + '\'' +
                ", interfacePath='" + interfacePath + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", responseStatus=" + responseStatus +
                ", delayTime=" + delayTime +
                ", enabled=" + enabled +
                ", status='" + status + '\'' +
                ", category='" + category + '\'' +
                ", accessCount=" + accessCount +
                ", successCount=" + successCount +
                ", failureCount=" + failureCount +
                ", avgResponseTime=" + avgResponseTime +
                ", successRate=" + successRate +
                ", isTestResponse=" + isTestResponse +
                ", hasError=" + hasError() +
                '}';
    }
}

