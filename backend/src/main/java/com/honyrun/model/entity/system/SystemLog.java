package com.honyrun.model.entity.system;

import com.honyrun.model.entity.base.AuditableEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 系统日志实体类
 *
 * 系统操作日志记录实体，支持多种日志类型、日志级别、操作类型等功能
 * 继承AuditableEntity，支持R2DBC响应式数据访问和审计功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  18:35:00
 * @modified 2025-07-01 18:35:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("sys_system_logs")
public class SystemLog extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 日志类型
     * SYSTEM-系统日志，OPERATION-操作日志，ERROR-错误日志，ACCESS-访问日志，SECURITY-安全日志
     */
    @Column("log_type")
    private String logType;

    /**
     * 日志级别
     * DEBUG、INFO、WARN、ERROR、FATAL
     */
    @Column("log_level")
    private String logLevel;

    /**
     * 操作类型
     * CREATE、UPDATE、DELETE、SELECT、LOGIN、LOGOUT、EXPORT、IMPORT等
     */
    @Column("operation_type")
    private String operationType;

    /**
     * 操作模块
     * 操作所属的功能模块
     */
    @Column("module")
    private String module;

    /**
     * 操作方法
     * 具体的操作方法或接口
     */
    @Column("method")
    private String method;

    /**
     * 请求URI
     * HTTP请求的URI路径
     */
    @Column("request_uri")
    private String requestUri;

    /**
     * 请求方法
     * HTTP请求方法：GET、POST、PUT、DELETE等
     */
    @Column("request_method")
    private String requestMethod;

    /**
     * 操作用户ID
     * 执行操作的用户ID
     */
    @Column("user_id")
    private Long userId;

    /**
     * 操作用户名
     * 执行操作的用户名
     */
    @Column("username")
    private String username;

    /**
     * 用户类型
     * SYSTEM_USER、NORMAL_USER、GUEST
     */
    @Column("user_type")
    private String userType;

    /**
     * 客户端IP地址
     * 操作来源的IP地址
     */
    @Column("client_ip")
    private String clientIp;

    /**
     * 用户代理
     * 客户端的User-Agent信息
     */
    @Column("user_agent")
    private String userAgent;

    /**
     * 操作描述
     * 操作的详细描述
     */
    @Column("description")
    private String description;

    /**
     * 请求参数
     * 请求的参数信息（JSON格式）
     */
    @Column("request_params")
    private String requestParams;

    /**
     * 响应结果
     * 操作的响应结果（JSON格式）
     */
    @Column("response_result")
    private String responseResult;

    /**
     * 异常信息
     * 操作过程中的异常信息
     */
    @Column("exception_info")
    private String exceptionInfo;

    /**
     * 执行时间
     * 操作执行耗时（毫秒）
     */
    @Column("execution_time")
    private Long executionTime;

    /**
     * 操作状态 - 使用Integer类型以保持与数据库INTEGER字段一致
     * 0: 失败, 1: 成功
     */
    @Column("status")
    private Integer status;

    /**
     * 操作时间
     * 具体的操作发生时间
     */
    @Column("operation_time")
    private LocalDateTime operationTime;

    /**
     * 删除标记字段
     * 0-未删除，1-已删除
     * 用于逻辑删除，避免物理删除数据
     */
    @Column("deleted")
    private Integer deleted = 0;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public SystemLog() {
        super();
        this.status = 1;
        this.operationTime = LocalDateTime.now();
    }

    /**
     * 带参数的构造函数
     *
     * @param logType 日志类型
     * @param logLevel 日志级别
     * @param operationType 操作类型
     * @param description 操作描述
     */
    public SystemLog(String logType, String logLevel, String operationType, String description) {
        this();
        this.logType = logType;
        this.logLevel = logLevel;
        this.operationType = operationType;
        this.description = description;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取日志类型
     *
     * @return 日志类型
     */
    public String getLogType() {
        return logType;
    }

    /**
     * 设置日志类型
     *
     * @param logType 日志类型
     */
    public void setLogType(String logType) {
        this.logType = logType;
    }

    /**
     * 获取日志级别
     *
     * @return 日志级别
     */
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * 设置日志级别
     *
     * @param logLevel 日志级别
     */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * 获取操作类型
     *
     * @return 操作类型
     */
    public String getOperationType() {
        return operationType;
    }

    /**
     * 设置操作类型
     *
     * @param operationType 操作类型
     */
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    /**
     * 获取操作模块
     *
     * @return 操作模块
     */
    public String getModule() {
        return module;
    }

    /**
     * 设置操作模块
     *
     * @param module 操作模块
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * 获取操作方法
     *
     * @return 操作方法
     */
    public String getMethod() {
        return method;
    }

    /**
     * 设置操作方法
     *
     * @param method 操作方法
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * 获取请求URI
     *
     * @return 请求URI
     */
    public String getRequestUri() {
        return requestUri;
    }

    /**
     * 设置请求URI
     *
     * @param requestUri 请求URI
     */
    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
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
     * 获取操作用户ID
     *
     * @return 操作用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置操作用户ID
     *
     * @param userId 操作用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取操作用户名
     *
     * @return 操作用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置操作用户名
     *
     * @param username 操作用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    public String getUserType() {
        return userType;
    }

    /**
     * 设置用户类型
     *
     * @param userType 用户类型
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * 获取客户端IP地址
     *
     * @return 客户端IP地址
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * 设置客户端IP地址
     *
     * @param clientIp 客户端IP地址
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    /**
     * 获取用户代理
     *
     * @return 用户代理
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * 设置用户代理
     *
     * @param userAgent 用户代理
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * 获取操作描述
     *
     * @return 操作描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置操作描述
     *
     * @param description 操作描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取请求参数
     *
     * @return 请求参数
     */
    public String getRequestParams() {
        return requestParams;
    }

    /**
     * 设置请求参数
     *
     * @param requestParams 请求参数
     */
    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    /**
     * 获取响应结果
     *
     * @return 响应结果
     */
    public String getResponseResult() {
        return responseResult;
    }

    /**
     * 设置响应结果
     *
     * @param responseResult 响应结果
     */
    public void setResponseResult(String responseResult) {
        this.responseResult = responseResult;
    }

    /**
     * 获取异常信息
     *
     * @return 异常信息
     */
    public String getExceptionInfo() {
        return exceptionInfo;
    }

    /**
     * 设置异常信息
     *
     * @param exceptionInfo 异常信息
     */
    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    /**
     * 获取执行时间
     *
     * @return 执行时间（毫秒）
     */
    public Long getExecutionTime() {
        return executionTime;
    }

    /**
     * 设置执行时间
     *
     * @param executionTime 执行时间（毫秒）
     */
    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    /**
     * 获取操作状态
     *
     * @return 操作状态，0-失败，1-成功
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置操作状态
     *
     * @param status 操作状态，0-失败，1-成功
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取操作时间
     *
     * @return 操作时间
     */
    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    /**
     * 设置操作时间
     *
     * @param operationTime 操作时间
     */
    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    /**
     * 获取删除标记
     * @return 删除标记，0-未删除，1-已删除
     */
    public Integer getDeleted() {
        return deleted;
    }

    /**
     * 设置删除标记
     * @param deleted 删除标记，0-未删除，1-已删除
     */
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
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
     * 判断是否为错误日志
     *
     * @return true-错误日志，false-其他日志
     */
    public boolean isErrorLog() {
        return "ERROR".equalsIgnoreCase(this.logLevel) || "FATAL".equalsIgnoreCase(this.logLevel);
    }

    /**
     * 判断是否为系统日志
     *
     * @return true-系统日志，false-其他日志
     */
    public boolean isSystemLog() {
        return "SYSTEM".equalsIgnoreCase(this.logType);
    }

    /**
     * 判断是否为操作日志
     *
     * @return true-操作日志，false-其他日志
     */
    public boolean isOperationLog() {
        return "OPERATION".equalsIgnoreCase(this.logType);
    }

    /**
     * 判断是否为访问日志
     *
     * @return true-访问日志，false-其他日志
     */
    public boolean isAccessLog() {
        return "ACCESS".equalsIgnoreCase(this.logType);
    }

    /**
     * 判断是否为安全日志
     *
     * @return true-安全日志，false-其他日志
     */
    public boolean isSecurityLog() {
        return "SECURITY".equalsIgnoreCase(this.logType);
    }

    /**
     * 判断是否为登录操作
     *
     * @return true-登录操作，false-其他操作
     */
    public boolean isLoginOperation() {
        return "LOGIN".equalsIgnoreCase(this.operationType);
    }

    /**
     * 判断是否为登出操作
     *
     * @return true-登出操作，false-其他操作
     */
    public boolean isLogoutOperation() {
        return "LOGOUT".equalsIgnoreCase(this.operationType);
    }

    /**
     * 判断是否为数据修改操作
     *
     * @return true-数据修改操作，false-其他操作
     */
    public boolean isDataModificationOperation() {
        return "CREATE".equalsIgnoreCase(this.operationType) ||
               "UPDATE".equalsIgnoreCase(this.operationType) ||
               "DELETE".equalsIgnoreCase(this.operationType);
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
     * 设置操作成功
     */
    public void markSuccess() {
        this.status = 1;
    }

    /**
     * 设置操作失败
     */
    public void markFailure() {
        this.status = 0;
    }

    /**
     * 获取简化的日志信息
     *
     * @return 简化的日志信息
     */
    public String getSimpleLogInfo() {
        return String.format("[%s] %s - %s by %s at %s",
                            this.logLevel,
                            this.operationType,
                            this.description,
                            this.username,
                            this.operationTime);
    }

    /**
     * 判断实体是否已删除
     *
     * @return true-已删除，false-未删除
     */
    public boolean isDeleted() {
        return this.deleted != null && this.deleted == 1;
    }

    /**
     * 标记实体为已删除
     */
    public void markDeleted() {
        this.deleted = 1;
    }

    /**
     * 标记实体为未删除
     */
    public void markUndeleted() {
        this.deleted = 0;
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写equals方法
     * 基于ID和操作时间进行比较
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
        SystemLog systemLog = (SystemLog) obj;
        return Objects.equals(operationTime, systemLog.operationTime) &&
               Objects.equals(userId, systemLog.userId) &&
               Objects.equals(operationType, systemLog.operationType);
    }

    /**
     * 重写hashCode方法
     * 基于操作时间、用户ID和操作类型生成哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operationTime, userId, operationType);
    }

    /**
     * 重写toString方法
     * 提供系统日志实体的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "SystemLog{" +
                "id=" + getId() +
                ", logType='" + logType + '\'' +
                ", logLevel='" + logLevel + '\'' +
                ", operationType='" + operationType + '\'' +
                ", module='" + module + '\'' +
                ", method='" + method + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", userType='" + userType + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", executionTime=" + executionTime +
                ", operationTime=" + operationTime +
                '}';
    }
}


