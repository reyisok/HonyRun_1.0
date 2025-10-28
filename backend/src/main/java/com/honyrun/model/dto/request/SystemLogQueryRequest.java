package com.honyrun.model.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 系统日志查询请求DTO
 *
 * 用于系统日志查询的请求参数封装，支持多条件组合查询和分页
 * 提供灵活的日志筛选条件，满足运维人员的日志查看需求
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 10:30:00
 * @modified 2025-07-01 10:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Schema(description = "系统日志查询请求")
public class SystemLogQueryRequest {

    /**
     * 页码，从1开始
     */
    @Schema(description = "页码，从1开始", example = "1")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    private Integer size = 20;

    /**
     * 日志类型列表
     * SYSTEM-系统日志，OPERATION-操作日志，ERROR-错误日志，ACCESS-访问日志，SECURITY-安全日志
     */
    @Schema(description = "日志类型列表", example = "[\"SYSTEM\", \"OPERATION\"]")
    private List<String> logTypes;

    /**
     * 日志级别列表
     * DEBUG、INFO、WARN、ERROR、FATAL
     */
    @Schema(description = "日志级别列表", example = "[\"INFO\", \"ERROR\"]")
    private List<String> logLevels;

    /**
     * 操作类型列表
     * CREATE、UPDATE、DELETE、SELECT、LOGIN、LOGOUT、EXPORT、IMPORT等
     */
    @Schema(description = "操作类型列表", example = "[\"LOGIN\", \"LOGOUT\"]")
    private List<String> operationTypes;

    /**
     * 操作模块
     */
    @Schema(description = "操作模块", example = "用户管理")
    private String module;

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
     * SYSTEM_USER、NORMAL_USER、GUEST
     */
    @Schema(description = "用户类型", example = "SYSTEM_USER")
    private String userType;

    /**
     * 客户端IP地址
     */
    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    private String clientIp;

    /**
     * 操作状态 - 使用Integer类型以保持与SystemLog实体一致
     * 0: 失败, 1: 成功, null: 全部
     */
    @Schema(description = "操作状态，0-失败，1-成功", example = "1")
    private Integer status;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2025-07-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间", example = "2025-07-01 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 关键词搜索
     * 在描述、请求参数、响应结果中搜索
     */
    @Schema(description = "关键词搜索", example = "登录")
    private String keyword;

    /**
     * 最小执行时间（毫秒）
     */
    @Schema(description = "最小执行时间（毫秒）", example = "1000")
    private Long minExecutionTime;

    /**
     * 最大执行时间（毫秒）
     */
    @Schema(description = "最大执行时间（毫秒）", example = "5000")
    private Long maxExecutionTime;

    /**
     * 排序字段
     * operation_time、execution_time、created_at等
     */
    @Schema(description = "排序字段", example = "operation_time")
    private String sortField = "operation_time";

    /**
     * 排序方向
     * ASC-升序，DESC-降序
     */
    @Schema(description = "排序方向", example = "DESC")
    private String sortDirection = "DESC";

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public SystemLogQueryRequest() {
    }

    // ==================== Getter和Setter方法 ====================

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public List<String> getLogTypes() {
        return logTypes;
    }

    public void setLogTypes(List<String> logTypes) {
        this.logTypes = logTypes;
    }

    public List<String> getLogLevels() {
        return logLevels;
    }

    public void setLogLevels(List<String> logLevels) {
        this.logLevels = logLevels;
    }

    public List<String> getOperationTypes() {
        return operationTypes;
    }

    public void setOperationTypes(List<String> operationTypes) {
        this.operationTypes = operationTypes;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getMinExecutionTime() {
        return minExecutionTime;
    }

    public void setMinExecutionTime(Long minExecutionTime) {
        this.minExecutionTime = minExecutionTime;
    }

    public Long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    public void setMaxExecutionTime(Long maxExecutionTime) {
        this.maxExecutionTime = maxExecutionTime;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    // ==================== 业务方法 ====================

    /**
     * 获取偏移量
     *
     * @return 偏移量
     */
    public long getOffset() {
        return (long) (page - 1) * size;
    }

    /**
     * 获取限制数量
     *
     * @return 限制数量
     */
    public long getLimit() {
        return size;
    }

    /**
     * 验证分页参数
     *
     * @return 是否有效
     */
    public boolean isValidPagination() {
        return page != null && page > 0 && size != null && size > 0 && size <= 100;
    }

    /**
     * 验证时间范围
     *
     * @return 是否有效
     */
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return !startTime.isAfter(endTime);
    }

    /**
     * 验证执行时间范围
     *
     * @return 是否有效
     */
    public boolean isValidExecutionTimeRange() {
        if (minExecutionTime == null || maxExecutionTime == null) {
            return true;
        }
        return minExecutionTime <= maxExecutionTime;
    }

    @Override
    public String toString() {
        return "SystemLogQueryRequest{" +
                "page=" + page +
                ", size=" + size +
                ", logTypes=" + logTypes +
                ", logLevels=" + logLevels +
                ", operationTypes=" + operationTypes +
                ", module='" + module + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", userType='" + userType + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", status=" + status +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", keyword='" + keyword + '\'' +
                ", minExecutionTime=" + minExecutionTime +
                ", maxExecutionTime=" + maxExecutionTime +
                ", sortField='" + sortField + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}

