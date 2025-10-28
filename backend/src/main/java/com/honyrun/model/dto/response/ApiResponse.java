package com.honyrun.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * 统一API响应格式
 * 
 * 根据异常处理与错误响应规范要求，统一错误模型字段结构：
 * - code: 错误码
 * - message: 错误消息
 * - traceId: 追踪ID，用于问题定位
 * - details: 详细错误信息（开发环境可包含堆栈信息）
 * - timestamp: 时间戳
 * - path: 请求路径
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01  15:55:00
 * @modified 2025-07-02 20:15:00
 * @version 2.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String code;
    private String message;
    private T data;
    private Map<String, Object> error;  // 错误信息对象，包含code与message
    private LocalDateTime timestamp;
    private String traceId;  // 追踪ID，用于问题定位
    private Map<String, Object> details;  // 详细错误信息
    private String path;  // 请求路径
    private String requestId;  // 请求ID，用于请求追踪
    private String version;  // API版本号

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.error = success ? null : buildError(this.code, this.message);  // 失败时设置error对象
        this.timestamp = LocalDateTime.now();
        embedDetailsIntoErrorIfPresent();
    }

    public ApiResponse(boolean success, String code, String message, T data, String traceId, Map<String, Object> details, String path) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.error = success ? null : buildError(this.code, this.message);  // 失败时设置error对象
        this.timestamp = LocalDateTime.now();
        this.traceId = traceId;
        this.details = details;
        this.path = path;
        embedDetailsIntoErrorIfPresent();
    }

    public ApiResponse(boolean success, String code, String message, T data, String traceId, Map<String, Object> details, String path, String requestId, String version) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.error = success ? null : buildError(this.code, this.message);  // 失败时设置error对象
        this.timestamp = LocalDateTime.now();
        this.traceId = traceId;
        this.details = details;
        this.path = path;
        this.requestId = requestId;
        this.version = version;
        embedDetailsIntoErrorIfPresent();
    }

    /**
     * 创建Builder实例
     *
     * @param <T> 数据类型
     * @return Builder实例
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * 创建成功响应
     *
     * @param data 响应数据
     * @param message 响应消息
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, "200", message, data);
    }

    /**
     * 创建成功响应（仅数据）
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "200", "操作成功", data);
    }

    /**
     * 创建成功响应（无数据）
     *
     * @param message 响应消息
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, "200", message, null);
    }

    /**
     * 创建错误响应
     *
     * @param message 错误消息
     * @param code 错误码
     * @param <T> 数据类型
     * @return 错误响应
     */
    public static <T> ApiResponse<T> error(String message, int code) {
        return new ApiResponse<>(false, String.valueOf(code), message, null);
    }

    /**
     * 创建错误响应
     *
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    /**
     * 创建错误响应（带数据）
     *
     * @param code 错误码
     * @param message 错误消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 错误响应
     */
    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }

    /**
     * 创建错误响应（完整字段）
     *
     * @param code 错误码
     * @param message 错误消息
     * @param traceId 追踪ID
     * @param details 详细错误信息
     * @param path 请求路径
     * @param <T> 数据类型
     * @return 错误响应
     */
    public static <T> ApiResponse<T> error(String code, String message, String traceId, Map<String, Object> details, String path) {
        return new ApiResponse<>(false, code, message, null, traceId, details, path);
    }

    /**
     * 创建错误响应（带追踪ID）
     *
     * @param code 错误码
     * @param message 错误消息
     * @param traceId 追踪ID
     * @param <T> 数据类型
     * @return 错误响应
     */
    public static <T> ApiResponse<T> error(String code, String message, String traceId) {
        return new ApiResponse<>(false, code, message, null, traceId, null, null);
    }

    /**
     * 创建错误响应（默认错误码）
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, "500", message, null);
    }

    /**
     * 创建分页成功响应
     *
     * @param pageResponse 分页响应数据
     * @param <T> 数据类型
     * @return 分页成功响应
     */
    public static <T> ApiResponse<PageResponse<T>> pageSuccess(PageResponse<T> pageResponse) {
        return new ApiResponse<>(true, "200", "查询成功", pageResponse);
    }

    /**
     * 创建分页成功响应（带消息）
     *
     * @param pageResponse 分页响应数据
     * @param message 响应消息
     * @param <T> 数据类型
     * @return 分页成功响应
     */
    public static <T> ApiResponse<PageResponse<T>> pageSuccess(PageResponse<T> pageResponse, String message) {
        return new ApiResponse<>(true, "200", message, pageResponse);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getError() {
        return error;
    }

    public void setError(Map<String, Object> error) {
        this.error = error;
        embedDetailsIntoErrorIfPresent();
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
        embedDetailsIntoErrorIfPresent();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Builder模式构建器
     */
    public static class Builder<T> {
        private final ApiResponse<T> response;

        public Builder() {
            this.response = new ApiResponse<>();
        }

        public Builder<T> success(boolean success) {
            response.setSuccess(success);
            // 在未设置code或message前避免构造包含空值的error对象
            response.setError(success ? null : buildError(response.getCode(), response.getMessage()));
            return this;
        }

        public Builder<T> code(String code) {
            response.setCode(code);
            // 保持错误对象与最新code一致，避免空值导致的NPE
            if (!response.isSuccess()) {
                response.setError(buildError(code, response.getMessage()));
            }
            return this;
        }

        public Builder<T> message(String message) {
            response.setMessage(message);
            // message可能为空，构造错误对象时应避免空值
            response.setError(response.isSuccess() ? null : buildError(response.getCode(), message));
            response.embedDetailsIntoErrorIfPresent();
            return this;
        }

        public Builder<T> data(T data) {
            response.setData(data);
            return this;
        }

        public Builder<T> timestamp(LocalDateTime timestamp) {
            response.setTimestamp(timestamp);
            return this;
        }

        public Builder<T> traceId(String traceId) {
            response.setTraceId(traceId);
            return this;
        }

        public Builder<T> details(Map<String, Object> details) {
            response.setDetails(details);
            return this;
        }

        public Builder<T> path(String path) {
            response.setPath(path);
            return this;
        }

        public Builder<T> requestId(String requestId) {
            response.setRequestId(requestId);
            return this;
        }

        public Builder<T> version(String version) {
            response.setVersion(version);
            return this;
        }

        public ApiResponse<T> build() {
            response.embedDetailsIntoErrorIfPresent();
            return response;
        }
    }

    /**
     * 构造错误对象，避免Map.of在包含空值时抛出NPE。
     * 当code或message为null时，仅添加非空字段；若均为空则返回null。
     */
    private static Map<String, Object> buildError(String code, String message) {
        Map<String, Object> err = new HashMap<>();
        if (code != null) {
            err.put("code", code);
        }
        if (message != null) {
            err.put("message", message);
        }
        return err.isEmpty() ? null : err;
    }

    /**
     * 当为错误响应且存在details时，将details嵌入到error对象中，满足契约校验。
     */
    private void embedDetailsIntoErrorIfPresent() {
        if (!this.success && this.details != null) {
            if (this.error == null) {
                this.error = new HashMap<>();
            }
            this.error.put("details", this.details);
        }
    }
}


