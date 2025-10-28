package com.honyrun.model.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 系统日志响应DTO
 *
 * 用于系统日志查询结果的响应数据封装
 * 提供完整的日志信息展示，支持前端日志管理界面
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 10:35:00
 * @modified 2025-07-01 10:35:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Schema(description = "系统日志响应")
public class SystemLogResponse {

    /**
     * 日志ID
     */
    @Schema(description = "日志ID", example = "1")
    private Long id;

    /**
     * 日志类型
     */
    @Schema(description = "日志类型", example = "OPERATION")
    private String logType;

    /**
     * 日志级别
     */
    @Schema(description = "日志级别", example = "INFO")
    private String logLevel;

    /**
     * 操作类型
     */
    @Schema(description = "操作类型", example = "LOGIN")
    private String operationType;

    /**
     * 操作模块
     */
    @Schema(description = "操作模块", example = "用户管理")
    private String module;

    /**
     * 操作方法
     */
    @Schema(description = "操作方法", example = "login")
    private String method;

    /**
     * 请求URI
     */
    @Schema(description = "请求URI", example = "/api/v1/auth/login")
    private String requestUri;

    /**
     * 请求方法
     */
    @Schema(description = "请求方法", example = "POST")
    private String requestMethod;

    /**
     * 操作用户ID
     */
    @Schema(description = "操作用户ID", example = "1")
    private Long userId;

    /**
     * 操作用户名
     */
    @Schema(description = "操作用户名", example = "honyrun-sys")
    private String username;

    /**
     * 用户类型
     */
    @Schema(description = "用户类型", example = "SYSTEM_USER")
    private String userType;

    /**
     * 客户端IP地址
     */
    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    private String clientIp;

    /**
     * 用户代理
     */
    @Schema(description = "用户代理", example = "Mozilla/5.0")
    private String userAgent;

    /**
     * 操作描述
     */
    @Schema(description = "操作描述", example = "用户登录成功")
    private String description;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数", example = "{\"username\":\"honyrun-sys\"}")
    private String requestParams;

    /**
     * 响应结果
     */
    @Schema(description = "响应结果", example = "{\"code\":200,\"message\":\"success\"}")
    private String responseResult;

    /**
     * 异常信息
     */
    @Schema(description = "异常信息")
    private String exceptionInfo;

    /**
     * 执行时间（毫秒）
     */
    @Schema(description = "执行时间（毫秒）", example = "150")
    private Long executionTime;

    /**
     * 操作状态 - 使用Integer类型以保持与业务实体SystemLog一致
     * 0: 失败, 1: 成功
     */
    @Schema(description = "操作状态，0-失败，1-成功", example = "1")
    private Integer status;

    /**
     * 操作时间
     */
    @Schema(description = "操作时间", example = "2025-07-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-07-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-07-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModifiedDate;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public SystemLogResponse() {
    }

    // ==================== Getter和Setter方法 ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getResponseResult() {
        return responseResult;
    }

    public void setResponseResult(String responseResult) {
        this.responseResult = responseResult;
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断操作是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return this.status != null && this.status == 1;
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
     */
    public String getStatusDescription() {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "成功" : "失败";
    }

    /**
     * 判断是否为错误日志
     *
     * @return true-错误日志，false-其他日志
     */
    public boolean isErrorLog() {
        return "ERROR".equalsIgnoreCase(this.logLevel) || "FATAL".equalsIgnoreCase(this.logLevel);
    }

    /**
     * 判断执行时间是否过长
     *
     * @param threshold 阈值（毫秒）
     * @return true-执行时间过长，false-正常
     */
    public boolean isSlowExecution(long threshold) {
        return this.executionTime != null && this.executionTime > threshold;
    }

    /**
     * 获取简化的用户代理信息
     *
     * @return 简化的用户代理信息
     */
    public String getSimpleUserAgent() {
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知";
        }

        if (userAgent.contains("Chrome")) {
            return "Chrome";
        } else if (userAgent.contains("Firefox")) {
            return "Firefox";
        } else if (userAgent.contains("Safari")) {
            return "Safari";
        } else if (userAgent.contains("Edge")) {
            return "Edge";
        } else {
            return "其他";
        }
    }

    /**
     * 获取格式化的执行时间
     *
     * @return 格式化的执行时间
     */
    public String getFormattedExecutionTime() {
        if (executionTime == null) {
            return "未知";
        }

        if (executionTime < 1000) {
            return executionTime + "ms";
        } else if (executionTime < 60000) {
            return String.format("%.2fs", executionTime / 1000.0);
        } else {
            long minutes = executionTime / 60000;
            long seconds = (executionTime % 60000) / 1000;
            return String.format("%dm%ds", minutes, seconds);
        }
    }

    @Override
    public String toString() {
        return "SystemLogResponse{" +
                "id=" + id +
                ", logType='" + logType + '\'' +
                ", logLevel='" + logLevel + '\'' +
                ", operationType='" + operationType + '\'' +
                ", module='" + module + '\'' +
                ", username='" + username + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", executionTime=" + executionTime +
                ", operationTime=" + operationTime +
                '}';
    }
}

