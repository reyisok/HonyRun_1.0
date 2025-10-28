package com.honyrun.request;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 事务监控请求类
 *
 * 用于事务监控查询的请求参数封装
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 01:55:00
 * @modified 2025-07-01 01:55:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class TransactionMonitoringRequest {

    /**
     * 事务类型
     */
    private String transactionType;

    /**
     * 事务状态
     */
    private String status;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 开始时间（查询范围）
     */
    private LocalDateTime startTime;

    /**
     * 结束时间（查询范围）
     */
    private LocalDateTime endTime;

    /**
     * 最小执行时长（毫秒）
     */
    private Long minDuration;

    /**
     * 最大执行时长（毫秒）
     */
    private Long maxDuration;

    /**
     * 页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向
     */
    private String sortDirection;

    // 构造函数
    public TransactionMonitoringRequest() {
        this.page = 0;
        this.size = 10;
        this.sortBy = "startTime";
        this.sortDirection = "DESC";
    }

    // Getter 和 Setter 方法
    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public Long getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(Long minDuration) {
        this.minDuration = minDuration;
    }

    public Long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(Long maxDuration) {
        this.maxDuration = maxDuration;
    }

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

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionMonitoringRequest that = (TransactionMonitoringRequest) o;
        return Objects.equals(transactionType, that.transactionType) &&
                Objects.equals(status, that.status) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(minDuration, that.minDuration) &&
                Objects.equals(maxDuration, that.maxDuration) &&
                Objects.equals(page, that.page) &&
                Objects.equals(size, that.size) &&
                Objects.equals(sortBy, that.sortBy) &&
                Objects.equals(sortDirection, that.sortDirection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionType, status, userId, startTime, endTime, 
                minDuration, maxDuration, page, size, sortBy, sortDirection);
    }

    @Override
    public String toString() {
        return "TransactionMonitoringRequest{" +
                "transactionType='" + transactionType + '\'' +
                ", status='" + status + '\'' +
                ", userId='" + userId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", minDuration=" + minDuration +
                ", maxDuration=" + maxDuration +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}

