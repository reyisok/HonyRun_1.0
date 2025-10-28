package com.honyrun.model.entity.system;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 外部接口实体类
 *
 * 用于存储外部API接口配置信息，支持R2DBC响应式数据访问。
 * 该实体管理外部接口的调用配置、监控和统计信息。
 *
 * 特性：
 * - 支持R2DBC响应式数据访问
 * - 继承审计功能
 * - 接口监控和统计
 * - 健康检查支持
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  16:30:00
 * @modified 2025-07-01 16:30:00
 * @version 2.0.0
 */
@Table("sys_external_interface")
public class ExternalInterface extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 接口名称
     * 外部接口的显示名称
     */
    @Column("interface_name")
    private String interfaceName;

    /**
     * 接口URL
     * 外部接口的完整URL地址
     */
    @Column("interface_url")
    private String interfaceUrl;

    /**
     * HTTP方法
     * 调用外部接口使用的HTTP方法
     */
    @Column("http_method")
    private String httpMethod = "GET";

    /**
     * 接口描述
     * 外部接口的详细描述
     */
    @Column("description")
    private String description;

    /**
     * 接口类型
     * REST-REST接口, SOAP-SOAP接口, GRAPHQL-GraphQL接口
     */
    @Column("interface_type")
    private InterfaceType interfaceType = InterfaceType.REST;

    /**
     * 接口状态
     * ACTIVE-活跃, INACTIVE-非活跃, DEPRECATED-已弃用, MAINTENANCE-维护中
     */
    @Column("status")
    private InterfaceStatus status = InterfaceStatus.ACTIVE;

    /**
     * 服务提供商
     * 外部接口的服务提供商
     */
    @Column("provider")
    private String provider;

    /**
     * 认证类型
     * NONE-无认证, API_KEY-API密钥, BEARER-Bearer令牌, BASIC-基础认证, OAUTH2-OAuth2认证
     */
    @Column("auth_type")
    private AuthType authType = AuthType.NONE;

    /**
     * 认证配置
     * 认证相关的配置信息（JSON格式，敏感信息需加密）
     */
    @Column("auth_config")
    private String authConfig;

    /**
     * 请求头配置
     * 默认的请求头配置（JSON格式）
     */
    @Column("default_headers")
    private String defaultHeaders;

    /**
     * 超时时间（毫秒）
     * 接口调用的超时时间
     */
    @Column("timeout_ms")
    private Integer timeoutMs = 30000;

    /**
     * 重试次数
     * 接口调用失败时的重试次数
     */
    @Column("retry_count")
    private Integer retryCount = 3;

    /**
     * 重试间隔（毫秒）
     * 重试之间的间隔时间
     */
    @Column("retry_interval_ms")
    private Integer retryIntervalMs = 1000;

    /**
     * 是否启用
     * 标识外部接口是否启用
     */
    @Column("is_enabled")
    private Boolean isEnabled = true;

    /**
     * 是否启用监控
     * 标识是否启用接口监控
     */
    @Column("is_monitoring_enabled")
    private Boolean isMonitoringEnabled = true;

    /**
     * 健康检查URL
     * 用于健康检查的URL（可选）
     */
    @Column("health_check_url")
    private String healthCheckUrl;

    /**
     * 健康检查间隔（秒）
     * 健康检查的执行间隔
     */
    @Column("health_check_interval")
    private Integer healthCheckInterval = 300;

    /**
     * 最后健康检查时间
     * 最后一次健康检查的时间
     */
    @Column("last_health_check_time")
    private LocalDateTime lastHealthCheckTime;

    /**
     * 健康状态
     * HEALTHY-健康, UNHEALTHY-不健康, UNKNOWN-未知
     */
    @Column("health_status")
    private HealthStatus healthStatus = HealthStatus.UNKNOWN;

    /**
     * 调用总次数
     * 接口被调用的总次数
     */
    @Column("total_calls")
    private Long totalCalls = 0L;

    /**
     * 成功调用次数
     * 接口调用成功的次数
     */
    @Column("success_calls")
    private Long successCalls = 0L;

    /**
     * 失败调用次数
     * 接口调用失败的次数
     */
    @Column("failed_calls")
    private Long failedCalls = 0L;

    /**
     * 平均响应时间（毫秒）
     * 接口调用的平均响应时间
     */
    @Column("avg_response_time")
    private Double avgResponseTime = 0.0;

    /**
     * 最后调用时间
     * 接口最后一次被调用的时间
     */
    @Column("last_call_time")
    private LocalDateTime lastCallTime;

    /**
     * 最后成功时间
     * 接口最后一次调用成功的时间
     */
    @Column("last_success_time")
    private LocalDateTime lastSuccessTime;

    /**
     * 最后失败时间
     * 接口最后一次调用失败的时间
     */
    @Column("last_failure_time")
    private LocalDateTime lastFailureTime;

    /**
     * 最后错误信息
     * 接口最后一次调用失败的错误信息
     */
    @Column("last_error_message")
    private String lastErrorMessage;

    /**
     * 接口分类
     * 外部接口的分类标识
     */
    @Column("category")
    private String category;

    /**
     * 标签
     * 外部接口的标签（多个标签用逗号分隔）
     */
    @Column("tags")
    private String tags;

    /**
     * 优先级
     * 接口的优先级（1-10，数值越大优先级越高）
     */
    @Column("priority")
    private Integer priority = 5;

    /**
     * 默认构造函数
     */
    public ExternalInterface() {
        super();
    }

    /**
     * 带参数的构造函数
     *
     * @param interfaceName 接口名称
     * @param interfaceUrl 接口URL
     * @param httpMethod HTTP方法
     * @param provider 服务提供商
     */
    public ExternalInterface(String interfaceName, String interfaceUrl, String httpMethod, String provider) {
        super();
        this.interfaceName = interfaceName;
        this.interfaceUrl = interfaceUrl;
        this.httpMethod = httpMethod;
        this.provider = provider;
    }

    // Getter and Setter methods

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterfaceUrl() {
        return interfaceUrl;
    }

    public void setInterfaceUrl(String interfaceUrl) {
        this.interfaceUrl = interfaceUrl;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InterfaceType getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(InterfaceType interfaceType) {
        this.interfaceType = interfaceType;
    }

    public InterfaceStatus getStatus() {
        return status;
    }

    public void setStatus(InterfaceStatus status) {
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public String getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(String authConfig) {
        this.authConfig = authConfig;
    }

    public String getDefaultHeaders() {
        return defaultHeaders;
    }

    public void setDefaultHeaders(String defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getRetryIntervalMs() {
        return retryIntervalMs;
    }

    public void setRetryIntervalMs(Integer retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Boolean getIsMonitoringEnabled() {
        return isMonitoringEnabled;
    }

    public void setIsMonitoringEnabled(Boolean isMonitoringEnabled) {
        this.isMonitoringEnabled = isMonitoringEnabled;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public Integer getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public void setHealthCheckInterval(Integer healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
    }

    public LocalDateTime getLastHealthCheckTime() {
        return lastHealthCheckTime;
    }

    public void setLastHealthCheckTime(LocalDateTime lastHealthCheckTime) {
        this.lastHealthCheckTime = lastHealthCheckTime;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public Long getTotalCalls() {
        return totalCalls;
    }

    public void setTotalCalls(Long totalCalls) {
        this.totalCalls = totalCalls;
    }

    public Long getSuccessCalls() {
        return successCalls;
    }

    public void setSuccessCalls(Long successCalls) {
        this.successCalls = successCalls;
    }

    public Long getFailedCalls() {
        return failedCalls;
    }

    public void setFailedCalls(Long failedCalls) {
        this.failedCalls = failedCalls;
    }

    public Double getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(Double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public LocalDateTime getLastCallTime() {
        return lastCallTime;
    }

    public void setLastCallTime(LocalDateTime lastCallTime) {
        this.lastCallTime = lastCallTime;
    }

    public LocalDateTime getLastSuccessTime() {
        return lastSuccessTime;
    }

    public void setLastSuccessTime(LocalDateTime lastSuccessTime) {
        this.lastSuccessTime = lastSuccessTime;
    }

    public LocalDateTime getLastFailureTime() {
        return lastFailureTime;
    }

    public void setLastFailureTime(LocalDateTime lastFailureTime) {
        this.lastFailureTime = lastFailureTime;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    // Business methods

    /**
     * 判断接口是否启用
     *
     * @return 如果启用返回true，否则返回false
     */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.isEnabled);
    }

    /**
     * 判断是否启用监控
     *
     * @return 如果启用监控返回true，否则返回false
     */
    public boolean isMonitoringEnabled() {
        return Boolean.TRUE.equals(this.isMonitoringEnabled);
    }

    /**
     * 判断接口是否健康
     *
     * @return 如果健康返回true，否则返回false
     */
    public boolean isHealthy() {
        return this.healthStatus == HealthStatus.HEALTHY;
    }

    /**
     * 判断接口是否活跃
     *
     * @return 如果活跃返回true，否则返回false
     */
    public boolean isActive() {
        return this.status == InterfaceStatus.ACTIVE;
    }

    /**
     * 计算成功率
     *
     * @return 成功率百分比
     */
    public double getSuccessRate() {
        if (totalCalls == null || totalCalls == 0) {
            return 0.0;
        }
        if (successCalls == null) {
            return 0.0;
        }
        return (double) successCalls / totalCalls * 100;
    }

    /**
     * 计算失败率
     *
     * @return 失败率百分比
     */
    public double getFailureRate() {
        if (totalCalls == null || totalCalls == 0) {
            return 0.0;
        }
        if (failedCalls == null) {
            return 0.0;
        }
        return (double) failedCalls / totalCalls * 100;
    }

    /**
     * 记录成功调用
     *
     * @param responseTime 响应时间
     */
    public void recordSuccessCall(long responseTime) {
        if (this.totalCalls == null) this.totalCalls = 0L;
        if (this.successCalls == null) this.successCalls = 0L;

        this.totalCalls++;
        this.successCalls++;
        this.lastCallTime = LocalDateTime.now();
        this.lastSuccessTime = LocalDateTime.now();

        // 更新平均响应时间
        if (this.avgResponseTime == null) this.avgResponseTime = 0.0;
        this.avgResponseTime = (this.avgResponseTime * (this.successCalls - 1) + responseTime) / this.successCalls;
    }

    /**
     * 记录失败调用
     *
     * @param errorMessage 错误信息
     */
    public void recordFailedCall(String errorMessage) {
        if (this.totalCalls == null) this.totalCalls = 0L;
        if (this.failedCalls == null) this.failedCalls = 0L;

        this.totalCalls++;
        this.failedCalls++;
        this.lastCallTime = LocalDateTime.now();
        this.lastFailureTime = LocalDateTime.now();
        this.lastErrorMessage = errorMessage;
    }

    /**
     * 更新健康状态
     *
     * @param status 健康状态
     */
    public void updateHealthStatus(HealthStatus status) {
        this.healthStatus = status;
        this.lastHealthCheckTime = LocalDateTime.now();
    }

    /**
     * 接口类型枚举
     */
    public enum InterfaceType {
        /** REST接口 */
        REST("REST接口"),
        /** SOAP接口 */
        SOAP("SOAP接口"),
        /** GraphQL接口 */
        GRAPHQL("GraphQL接口"),
        /** WebSocket接口 */
        WEBSOCKET("WebSocket接口"),
        /** 其他 */
        OTHER("其他");

        private final String description;

        InterfaceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 接口状态枚举
     */
    public enum InterfaceStatus {
        /** 活跃 */
        ACTIVE("活跃"),
        /** 非活跃 */
        INACTIVE("非活跃"),
        /** 已弃用 */
        DEPRECATED("已弃用"),
        /** 维护中 */
        MAINTENANCE("维护中");

        private final String description;

        InterfaceStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 认证类型枚举
     */
    public enum AuthType {
        /** 无认证 */
        NONE("无认证"),
        /** API密钥 */
        API_KEY("API密钥"),
        /** Bearer令牌 */
        BEARER("Bearer令牌"),
        /** 基础认证 */
        BASIC("基础认证"),
        /** OAuth2认证 */
        OAUTH2("OAuth2认证");

        private final String description;

        AuthType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 健康状态枚举
     */
    public enum HealthStatus {
        /** 健康 */
        HEALTHY("健康"),
        /** 不健康 */
        UNHEALTHY("不健康"),
        /** 未知 */
        UNKNOWN("未知");

        private final String description;

        HealthStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return String.format("ExternalInterface{id=%d, name='%s', url='%s', status=%s, health=%s, successRate=%.2f%%}",
                getId(), interfaceName, interfaceUrl, status, healthStatus, getSuccessRate());
    }
}


