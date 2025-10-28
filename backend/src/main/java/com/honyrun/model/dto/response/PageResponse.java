package com.honyrun.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 统一分页响应DTO
 *
 * 提供标准的分页数据结构，用于所有分页查询的响应格式统一
 * 支持响应式数据传输和JSON序列化
 *
 * @param <T> 分页数据类型
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 16:57:03
 * @modified 2025-07-01 16:57:03
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "分页响应")
public class PageResponse<T> {

    /**
     * 分页数据列表
     */
    @JsonProperty("list")
    @Schema(description = "分页数据列表")
    private List<T> list;

    /**
     * 总记录数
     */
    @JsonProperty("total")
    @Schema(description = "总记录数", example = "100")
    private Long total;

    /**
     * 当前页码（从1开始）
     */
    @JsonProperty("pageNum")
    @Schema(description = "当前页码（从1开始）", example = "1")
    private Integer pageNum;

    /**
     * 每页大小
     */
    @JsonProperty("pageSize")
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize;

    /**
     * 总页数
     */
    @JsonProperty("pages")
    @Schema(description = "总页数", example = "10")
    private Integer pages;

    /**
     * 是否为第一页
     */
    @JsonProperty("isFirst")
    @Schema(description = "是否为第一页", example = "true")
    private Boolean isFirst;

    /**
     * 是否为最后一页
     */
    @JsonProperty("isLast")
    @Schema(description = "是否为最后一页", example = "false")
    private Boolean isLast;

    /**
     * 是否有上一页
     */
    @JsonProperty("hasPrevious")
    @Schema(description = "是否有上一页", example = "false")
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    @JsonProperty("hasNext")
    @Schema(description = "是否有下一页", example = "true")
    private Boolean hasNext;

    /**
     * 默认构造函数
     */
    public PageResponse() {
    }

    /**
     * 构造函数
     *
     * @param list 分页数据列表
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     */
    public PageResponse(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = calculatePages(total, pageSize);
        this.isFirst = pageNum == 1;
        this.isLast = pageNum.equals(this.pages);
        this.hasPrevious = pageNum > 1;
        this.hasNext = pageNum < this.pages;
    }

    /**
     * 计算总页数
     *
     * @param total 总记录数
     * @param pageSize 每页大小
     * @return 总页数
     */
    private Integer calculatePages(Long total, Integer pageSize) {
        if (total == null || total == 0 || pageSize == null || pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 创建空的分页响应
     *
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param <T> 数据类型
     * @return 空的分页响应
     */
    public static <T> PageResponse<T> empty(Integer pageNum, Integer pageSize) {
        return new PageResponse<>(List.of(), 0L, pageNum, pageSize);
    }

    /**
     * 创建分页响应
     *
     * @param list 分页数据列表
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param <T> 数据类型
     * @return 分页响应
     */
    public static <T> PageResponse<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        return new PageResponse<>(list, total, pageNum, pageSize);
    }

    // ==================== Getter和Setter方法 ====================

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
        this.pages = calculatePages(total, pageSize);
        updatePageFlags();
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        updatePageFlags();
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        this.pages = calculatePages(total, pageSize);
        updatePageFlags();
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Boolean getIsFirst() {
        return isFirst;
    }

    public void setIsFirst(Boolean isFirst) {
        this.isFirst = isFirst;
    }

    public Boolean getIsLast() {
        return isLast;
    }

    public void setIsLast(Boolean isLast) {
        this.isLast = isLast;
    }

    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    /**
     * 更新分页标志
     */
    private void updatePageFlags() {
        if (pageNum != null && pages != null) {
            this.isFirst = pageNum == 1;
            this.isLast = pageNum.equals(pages);
            this.hasPrevious = pageNum > 1;
            this.hasNext = pageNum < pages;
        }
    }

    @Override
    public String toString() {
        return "PageResponse{" +
                "list=" + (list != null ? list.size() : 0) + " items" +
                ", total=" + total +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", pages=" + pages +
                ", isFirst=" + isFirst +
                ", isLast=" + isLast +
                ", hasPrevious=" + hasPrevious +
                ", hasNext=" + hasNext +
                '}';
    }
}


