package com.honyrun.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 统一分页请求DTO
 *
 * 提供标准的分页参数结构，用于所有分页查询的请求参数统一
 * 支持参数验证和JSON序列化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 14:35:00
 * @modified 2025-07-01 14:35:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Schema(description = "分页请求")
public class PageRequest {

    /**
     * 页码（从1开始）
     */
    @JsonProperty("pageNum")
    @Schema(description = "页码（从1开始）", example = "1", minimum = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @JsonProperty("pageSize")
    @Schema(description = "每页大小", example = "10", minimum = "1", maximum = "100")
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    @JsonProperty("sortBy")
    @Schema(description = "排序字段", example = "createdDate")
    private String sortBy;

    /**
     * 排序方向
     * ASC - 升序，DESC - 降序
     */
    @JsonProperty("sortDirection")
    @Schema(description = "排序方向", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";

    /**
     * 默认构造函数
     */
    public PageRequest() {
    }

    /**
     * 构造函数
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     */
    public PageRequest(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    /**
     * 构造函数
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     */
    public PageRequest(Integer pageNum, Integer pageSize, String sortBy, String sortDirection) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    /**
     * 获取偏移量（用于数据库查询）
     *
     * @return 偏移量
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取限制数量（用于数据库查询）
     *
     * @return 限制数量
     */
    public int getLimit() {
        return pageSize;
    }

    /**
     * 验证分页参数
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return pageNum != null && pageNum > 0 && 
               pageSize != null && pageSize > 0 && pageSize <= 100;
    }

    /**
     * 验证排序参数
     *
     * @return 是否有效
     */
    public boolean isValidSort() {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return true; // 排序参数可选
        }
        return sortDirection != null && 
               ("ASC".equalsIgnoreCase(sortDirection) || "DESC".equalsIgnoreCase(sortDirection));
    }

    /**
     * 是否有排序
     *
     * @return 是否有排序
     */
    public boolean hasSort() {
        return sortBy != null && !sortBy.trim().isEmpty();
    }

    /**
     * 是否升序
     *
     * @return 是否升序
     */
    public boolean isAscending() {
        return "ASC".equalsIgnoreCase(sortDirection);
    }

    /**
     * 是否降序
     *
     * @return 是否降序
     */
    public boolean isDescending() {
        return "DESC".equalsIgnoreCase(sortDirection);
    }

    // ==================== Getter和Setter方法 ====================

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
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
    public String toString() {
        return "PageRequest{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}

