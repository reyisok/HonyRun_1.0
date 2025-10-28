package com.honyrun.service.health;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康状态数据结构
 * 
 * <p>统一的健康状态表示类，用于封装组件和系统的健康检查结果。
 * 
 * <p><strong>设计特性：</strong>
 * <ul>
 *   <li>状态枚举：使用枚举类型确保状态值的一致性</li>
 *   <li>时间戳：记录健康检查的执行时间</li>
 *   <li>详细信息：支持附加详细的健康信息</li>
 *   <li>JSON序列化：支持REST API响应</li>
 * </ul>
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthStatus {
    
    /**
     * 健康状态枚举
     */
    public enum Status {
        /** 健康状态 */
        UP("UP", "健康"),
        /** 不健康状态 */
        DOWN("DOWN", "不健康"),
        /** 未知状态 */
        UNKNOWN("UNKNOWN", "未知"),
        /** 部分健康状态 */
        DEGRADED("DEGRADED", "部分健康");
        
        private final String code;
        private final String description;
        
        Status(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /** 健康状态 */
    private Status status;
    
    /** 组件名称 */
    private String component;
    
    /** 检查时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    /** 响应时间(毫秒) */
    private Long responseTime;
    
    /** 错误信息 */
    private String error;
    
    /** 详细信息 */
    private Map<String, Object> details;
    
    /**
     * 默认构造函数
     */
    public HealthStatus() {
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
        this.responseTime = 0L; // 默认响应时间为0，避免null值
    }
    
    /**
     * 构造函数
     * 
     * @param status 健康状态
     * @param component 组件名称
     */
    public HealthStatus(Status status, String component) {
        this();
        this.status = status;
        this.component = component;
    }
    
    /**
     * 创建健康状态
     * 
     * @param component 组件名称
     * @return 健康状态实例
     */
    public static HealthStatus up(String component) {
        return new HealthStatus(Status.UP, component);
    }
    
    /**
     * 创建不健康状态
     * 
     * @param component 组件名称
     * @return 不健康状态实例
     */
    public static HealthStatus down(String component) {
        return new HealthStatus(Status.DOWN, component);
    }
    
    /**
     * 创建未知状态
     * 
     * @param component 组件名称
     * @return 未知状态实例
     */
    public static HealthStatus unknown(String component) {
        return new HealthStatus(Status.UNKNOWN, component);
    }
    
    /**
     * 创建部分健康状态
     * 
     * @param component 组件名称
     * @return 部分健康状态实例
     */
    public static HealthStatus degraded(String component) {
        return new HealthStatus(Status.DEGRADED, component);
    }
    
    /**
     * 添加详细信息
     * 
     * @param key 键
     * @param value 值
     * @return 当前实例，支持链式调用
     */
    public HealthStatus withDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }
    
    /**
     * 设置错误信息
     * 
     * @param error 错误信息
     * @return 当前实例，支持链式调用
     */
    public HealthStatus withError(String error) {
        this.error = error;
        return this;
    }
    
    /**
     * 设置响应时间
     * 
     * @param responseTime 响应时间(毫秒)
     * @return 当前实例，支持链式调用
     */
    public HealthStatus withResponseTime(Long responseTime) {
        this.responseTime = responseTime;
        return this;
    }
    
    /**
     * 判断是否健康
     * 
     * @return true表示健康，false表示不健康
     */
    public boolean isHealthy() {
        return Status.UP.equals(this.status);
    }
    
    // Getters and Setters
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getComponent() {
        return component;
    }
    
    public void setComponent(String component) {
        this.component = component;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }
    
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
    
    @Override
    public String toString() {
        return "HealthStatus{" +
                "status=" + status +
                ", component='" + component + '\'' +
                ", timestamp=" + timestamp +
                ", responseTime=" + responseTime +
                ", error='" + error + '\'' +
                ", details=" + details +
                '}';
    }
}
