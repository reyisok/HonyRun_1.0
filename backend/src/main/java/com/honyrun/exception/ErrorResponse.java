package com.honyrun.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 错误响应模型
 * 实现标准错误响应格式和错误详情
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * 错误码
     */
    private int code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 错误发生时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 错误详情（如字段验证错误）
     */
    private Map<String, Object> details;

    /**
     * 请求ID（用于追踪）
     */
    private String requestId;

    /**
     * 错误堆栈信息（仅开发环境）
     */
    private String stackTrace;

    /**
     * 默认构造函数
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造函数
     *
     * @param code 错误码
     * @param message 错误消息
     */
    public ErrorResponse(int code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数
     *
     * @param code 错误码
     * @param message 错误消息
     * @param path 请求路径
     */
    public ErrorResponse(int code, String message, String path) {
        this(code, message);
        this.path = path;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     */
    public ErrorResponse(ErrorCode errorCode) {
        this();
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param path 请求路径
     */
    public ErrorResponse(ErrorCode errorCode, String path) {
        this(errorCode);
        this.path = path;
    }

    /**
     * 创建Builder实例
     *
     * @return Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 创建成功响应
     *
     * @return 成功响应
     */
    public static ErrorResponse success() {
        return new ErrorResponse(ErrorCode.SUCCESS);
    }

    /**
     * 创建错误响应
     *
     * @param errorCode 错误码枚举
     * @return 错误响应
     */
    public static ErrorResponse error(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }

    /**
     * 创建错误响应
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     * @return 错误响应
     */
    public static ErrorResponse error(ErrorCode errorCode, String message) {
        ErrorResponse response = new ErrorResponse(errorCode);
        response.setMessage(message);
        return response;
    }

    /**
     * 创建错误响应
     *
     * @param code 错误码
     * @param message 错误消息
     * @return 错误响应
     */
    public static ErrorResponse error(int code, String message) {
        return new ErrorResponse(code, message);
    }

    // Getter和Setter方法

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * Builder模式构建器
     */
    public static class Builder {
        private final ErrorResponse response;

        public Builder() {
            this.response = new ErrorResponse();
        }

        public Builder code(int code) {
            response.setCode(code);
            return this;
        }

        public Builder code(ErrorCode errorCode) {
            response.setCode(errorCode.getCode());
            return this;
        }

        public Builder message(String message) {
            response.setMessage(message);
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            response.setTimestamp(timestamp);
            return this;
        }

        public Builder path(String path) {
            response.setPath(path);
            return this;
        }

        public Builder details(Map<String, Object> details) {
            response.setDetails(details);
            return this;
        }

        public Builder requestId(String requestId) {
            response.setRequestId(requestId);
            return this;
        }

        public Builder stackTrace(String stackTrace) {
            response.setStackTrace(stackTrace);
            return this;
        }

        public Builder errorCode(ErrorCode errorCode) {
            response.setCode(errorCode.getCode());
            response.setMessage(errorCode.getMessage());
            return this;
        }

        public ErrorResponse build() {
            return response;
        }
    }

    @Override
    public String toString() {
        return String.format("ErrorResponse{code=%d, message='%s', timestamp=%s, path='%s'}",
                code, message, timestamp, path);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ErrorResponse that = (ErrorResponse) obj;
        return code == that.code &&
               message != null ? message.equals(that.message) : that.message == null;
    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}

