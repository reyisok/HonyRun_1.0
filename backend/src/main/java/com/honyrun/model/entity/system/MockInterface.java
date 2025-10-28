package com.honyrun.model.entity.system;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

/**
 * 模拟接口实体类
 *
 * 模拟接口配置管理实体，支持接口配置、响应模板、状态管理、动态配置等功能
 * 继承AuditableEntity，支持R2DBC响应式数据访问和审计功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  18:40:00
 * @modified 2025-07-01 18:40:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("sys_mock_interface")
public class MockInterface extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 接口名称
     * 模拟接口的显示名称
     */
    @Column("interface_name")
    private String interfaceName;

    /**
     * 接口路径
     * 模拟接口的URL路径
     */
    @Column("interface_path")
    private String interfacePath;

    /**
     * 请求方法
     * HTTP请求方法：GET、POST、PUT、DELETE、PATCH等
     */
    @Column("request_method")
    private String requestMethod;

    /**
     * 接口描述
     * 模拟接口的详细描述
     */
    @Column("description")
    private String description;

    /**
     * 请求参数模板
     * 接口请求参数的JSON模板
     */
    @Column("request_template")
    private String requestTemplate;

    /**
     * 响应模板
     * 接口响应数据的JSON模板
     */
    @Column("response_template")
    private String responseTemplate;

    /**
     * 响应状态码
     * HTTP响应状态码，默认200
     */
    @Column("response_status")
    private Integer responseStatus;

    /**
     * 响应头配置
     * HTTP响应头的JSON配置
     */
    @Column("response_headers")
    private String responseHeaders;

    /**
     * 延迟时间
     * 模拟接口响应延迟时间（毫秒）
     */
    @Column("delay_time")
    private Long delayTime;

    /**
     * 是否启用
     * 0-禁用，1-启用
     */
    @Column("enabled")
    private Integer enabled;

    /**
     * 接口状态
     * ACTIVE-活跃，INACTIVE-非活跃，MAINTENANCE-维护中
     */
    @Column("status")
    private String status;

    /**
     * 接口分类
     * 用于对模拟接口进行分组管理
     */
    @Column("category")
    private String category;

    /**
     * 接口标签
     * 用于标记接口特性的标签，多个标签用逗号分隔
     */
    @Column("tags")
    private String tags;

    /**
     * 访问次数
     * 接口被调用的总次数
     */
    @Column("access_count")
    private Long accessCount;

    /**
     * 成功次数
     * 接口成功响应的次数
     */
    @Column("success_count")
    private Long successCount;

    /**
     * 失败次数
     * 接口失败响应的次数
     */
    @Column("failure_count")
    private Long failureCount;

    /**
     * 平均响应时间
     * 接口平均响应时间（毫秒）
     */
    @Column("avg_response_time")
    private Double avgResponseTime;

    /**
     * 是否记录日志
     * 0-不记录，1-记录
     */
    @Column("log_enabled")
    private Integer logEnabled;

    /**
     * 动态配置
     * 接口的动态配置参数（JSON格式）
     */
    @Column("dynamic_config")
    private String dynamicConfig;

    /**
     * 权限配置
     * 接口访问权限配置
     */
    @Column("permission_config")
    private String permissionConfig;

    /**
     * 排序序号
     * 用于接口的显示排序
     */
    @Column("sort_order")
    private Integer sortOrder;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public MockInterface() {
        super();
        this.enabled = 1;
        this.status = "ACTIVE";
        this.responseStatus = 200;
        this.delayTime = 0L;
        this.accessCount = 0L;
        this.successCount = 0L;
        this.failureCount = 0L;
        this.avgResponseTime = 0.0;
        this.logEnabled = 1;
        this.sortOrder = 0;
    }

    /**
     * 带参数的构造函数
     *
     * @param interfaceName 接口名称
     * @param interfacePath 接口路径
     * @param requestMethod 请求方法
     * @param responseTemplate 响应模板
     */
    public MockInterface(String interfaceName, String interfacePath, String requestMethod, String responseTemplate) {
        this();
        this.interfaceName = interfaceName;
        this.interfacePath = interfacePath;
        this.requestMethod = requestMethod;
        this.responseTemplate = responseTemplate;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取接口名称
     *
     * @return 接口名称
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * 设置接口名称
     *
     * @param interfaceName 接口名称
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * 获取接口路径
     *
     * @return 接口路径
     */
    public String getInterfacePath() {
        return interfacePath;
    }

    /**
     * 设置接口路径
     *
     * @param interfacePath 接口路径
     */
    public void setInterfacePath(String interfacePath) {
        this.interfacePath = interfacePath;
    }

    /**
     * 获取请求方法
     *
     * @return 请求方法
     */
    public String getRequestMethod() {
        return requestMethod;
    }

    /**
     * 设置请求方法
     *
     * @param requestMethod 请求方法
     */
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * 获取接口描述
     *
     * @return 接口描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置接口描述
     *
     * @param description 接口描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取请求参数模板
     *
     * @return 请求参数模板
     */
    public String getRequestTemplate() {
        return requestTemplate;
    }

    /**
     * 设置请求参数模板
     *
     * @param requestTemplate 请求参数模板
     */
    public void setRequestTemplate(String requestTemplate) {
        this.requestTemplate = requestTemplate;
    }

    /**
     * 获取响应模板
     *
     * @return 响应模板
     */
    public String getResponseTemplate() {
        return responseTemplate;
    }

    /**
     * 设置响应模板
     *
     * @param responseTemplate 响应模板
     */
    public void setResponseTemplate(String responseTemplate) {
        this.responseTemplate = responseTemplate;
    }

    /**
     * 获取响应状态码
     *
     * @return 响应状态码
     */
    public Integer getResponseStatus() {
        return responseStatus;
    }

    /**
     * 设置响应状态码
     *
     * @param responseStatus 响应状态码
     */
    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    /**
     * 获取响应头配置
     *
     * @return 响应头配置
     */
    public String getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * 设置响应头配置
     *
     * @param responseHeaders 响应头配置
     */
    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    /**
     * 获取延迟时间
     *
     * @return 延迟时间（毫秒）
     */
    public Long getDelayTime() {
        return delayTime;
    }

    /**
     * 设置延迟时间
     *
     * @param delayTime 延迟时间（毫秒）
     */
    public void setDelayTime(Long delayTime) {
        this.delayTime = delayTime;
    }

    /**
     * 获取是否启用
     *
     * @return 是否启用，0-禁用，1-启用
     */
    public Integer getEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用
     *
     * @param enabled 是否启用，0-禁用，1-启用
     */
    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取接口状态
     *
     * @return 接口状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置接口状态
     *
     * @param status 接口状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取接口分类
     *
     * @return 接口分类
     */
    public String getCategory() {
        return category;
    }

    /**
     * 设置接口分类
     *
     * @param category 接口分类
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 获取接口标签
     *
     * @return 接口标签
     */
    public String getTags() {
        return tags;
    }

    /**
     * 设置接口标签
     *
     * @param tags 接口标签
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * 获取访问次数
     *
     * @return 访问次数
     */
    public Long getAccessCount() {
        return accessCount;
    }

    /**
     * 设置访问次数
     *
     * @param accessCount 访问次数
     */
    public void setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
    }

    /**
     * 获取成功次数
     *
     * @return 成功次数
     */
    public Long getSuccessCount() {
        return successCount;
    }

    /**
     * 设置成功次数
     *
     * @param successCount 成功次数
     */
    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    /**
     * 获取失败次数
     *
     * @return 失败次数
     */
    public Long getFailureCount() {
        return failureCount;
    }

    /**
     * 设置失败次数
     *
     * @param failureCount 失败次数
     */
    public void setFailureCount(Long failureCount) {
        this.failureCount = failureCount;
    }

    /**
     * 获取平均响应时间
     *
     * @return 平均响应时间（毫秒）
     */
    public Double getAvgResponseTime() {
        return avgResponseTime;
    }

    /**
     * 设置平均响应时间
     *
     * @param avgResponseTime 平均响应时间（毫秒）
     */
    public void setAvgResponseTime(Double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    /**
     * 获取是否记录日志
     *
     * @return 是否记录日志，0-不记录，1-记录
     */
    public Integer getLogEnabled() {
        return logEnabled;
    }

    /**
     * 设置是否记录日志
     *
     * @param logEnabled 是否记录日志，0-不记录，1-记录
     */
    public void setLogEnabled(Integer logEnabled) {
        this.logEnabled = logEnabled;
    }

    /**
     * 获取动态配置
     *
     * @return 动态配置
     */
    public String getDynamicConfig() {
        return dynamicConfig;
    }

    /**
     * 设置动态配置
     *
     * @param dynamicConfig 动态配置
     */
    public void setDynamicConfig(String dynamicConfig) {
        this.dynamicConfig = dynamicConfig;
    }

    /**
     * 获取权限配置
     *
     * @return 权限配置
     */
    public String getPermissionConfig() {
        return permissionConfig;
    }

    /**
     * 设置权限配置
     *
     * @param permissionConfig 权限配置
     */
    public void setPermissionConfig(String permissionConfig) {
        this.permissionConfig = permissionConfig;
    }

    /**
     * 获取排序序号
     *
     * @return 排序序号
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * 设置排序序号
     *
     * @param sortOrder 排序序号
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断接口是否启用
     *
     * @return true-启用，false-禁用
     */
    public boolean isEnabled() {
        return this.enabled != null && this.enabled == 1;
    }

    /**
     * 判断接口是否活跃
     *
     * @return true-活跃，false-非活跃
     */
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    /**
     * 判断接口是否在维护中
     *
     * @return true-维护中，false-正常
     */
    public boolean isMaintenance() {
        return "MAINTENANCE".equalsIgnoreCase(this.status);
    }

    /**
     * 判断是否记录日志
     *
     * @return true-记录日志，false-不记录
     */
    public boolean isLogEnabled() {
        return this.logEnabled != null && this.logEnabled == 1;
    }

    /**
     * 启用接口
     */
    public void enable() {
        this.enabled = 1;
        this.status = "ACTIVE";
    }

    /**
     * 禁用接口
     */
    public void disable() {
        this.enabled = 0;
        this.status = "INACTIVE";
    }

    /**
     * 设置接口为维护状态
     */
    public void setMaintenance() {
        this.status = "MAINTENANCE";
    }

    /**
     * 启用日志记录
     */
    public void enableLog() {
        this.logEnabled = 1;
    }

    /**
     * 禁用日志记录
     */
    public void disableLog() {
        this.logEnabled = 0;
    }

    /**
     * 增加访问次数
     */
    public void incrementAccessCount() {
        if (this.accessCount == null) {
            this.accessCount = 0L;
        }
        this.accessCount++;
    }

    /**
     * 增加成功次数
     */
    public void incrementSuccessCount() {
        if (this.successCount == null) {
            this.successCount = 0L;
        }
        this.successCount++;
    }

    /**
     * 增加失败次数
     */
    public void incrementFailureCount() {
        if (this.failureCount == null) {
            this.failureCount = 0L;
        }
        this.failureCount++;
    }

    /**
     * 计算成功率
     *
     * @return 成功率（0-1之间的小数）
     */
    public double getSuccessRate() {
        if (this.accessCount == null || this.accessCount == 0) {
            return 0.0;
        }
        long success = this.successCount != null ? this.successCount : 0L;
        return (double) success / this.accessCount;
    }

    /**
     * 计算失败率
     *
     * @return 失败率（0-1之间的小数）
     */
    public double getFailureRate() {
        if (this.accessCount == null || this.accessCount == 0) {
            return 0.0;
        }
        long failure = this.failureCount != null ? this.failureCount : 0L;
        return (double) failure / this.accessCount;
    }

    /**
     * 更新平均响应时间
     *
     * @param responseTime 本次响应时间（毫秒）
     */
    public void updateAvgResponseTime(long responseTime) {
        if (this.avgResponseTime == null) {
            this.avgResponseTime = 0.0;
        }
        if (this.accessCount == null || this.accessCount == 0) {
            this.avgResponseTime = (double) responseTime;
        } else {
            // 计算新的平均响应时间
            this.avgResponseTime = (this.avgResponseTime * (this.accessCount - 1) + responseTime) / this.accessCount;
        }
    }

    /**
     * 判断是否为GET请求
     *
     * @return true-GET请求，false-其他请求
     */
    public boolean isGetRequest() {
        return "GET".equalsIgnoreCase(this.requestMethod);
    }

    /**
     * 判断是否为POST请求
     *
     * @return true-POST请求，false-其他请求
     */
    public boolean isPostRequest() {
        return "POST".equalsIgnoreCase(this.requestMethod);
    }

    /**
     * 判断响应时间是否过长
     *
     * @param threshold 阈值（毫秒）
     * @return true-响应时间过长，false-正常
     */
    public boolean isSlowResponse(double threshold) {
        return this.avgResponseTime != null && this.avgResponseTime > threshold;
    }

    /**
     * 获取接口的完整路径
     *
     * @param baseUrl 基础URL
     * @return 完整的接口路径
     */
    public String getFullPath(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return this.interfacePath;
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = this.interfacePath.startsWith("/") ? this.interfacePath : "/" + this.interfacePath;
        return base + path;
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写equals方法
     * 基于接口路径和请求方法进行比较
     *
     * @param obj 比较对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        MockInterface that = (MockInterface) obj;
        return Objects.equals(interfacePath, that.interfacePath) &&
               Objects.equals(requestMethod, that.requestMethod);
    }

    /**
     * 重写hashCode方法
     * 基于接口路径和请求方法生成哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), interfacePath, requestMethod);
    }

    /**
     * 重写toString方法
     * 提供模拟接口实体的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "MockInterface{" +
                "id=" + getId() +
                ", interfaceName='" + interfaceName + '\'' +
                ", interfacePath='" + interfacePath + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", description='" + description + '\'' +
                ", responseStatus=" + responseStatus +
                ", delayTime=" + delayTime +
                ", enabled=" + enabled +
                ", status='" + status + '\'' +
                ", category='" + category + '\'' +
                ", accessCount=" + accessCount +
                ", successCount=" + successCount +
                ", failureCount=" + failureCount +
                ", avgResponseTime=" + avgResponseTime +
                ", logEnabled=" + logEnabled +
                '}';
    }
}


