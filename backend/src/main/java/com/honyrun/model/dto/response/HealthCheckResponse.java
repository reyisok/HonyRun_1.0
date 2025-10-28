package com.honyrun.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查响应DTO
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-13 16:57:03
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthCheckResponse {
    
    private String status;
    private Map<String, Object> components;
    private LocalDateTime timestamp;
    private String error;
    private Map<String, Object> details;
    private Long responseTime;
    private String version;
    private String environment;
    
    public HealthCheckResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public HealthCheckResponse(String status, Map<String, Object> components) {
        this();
        this.status = status;
        this.components = components;
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Map<String, Object> getComponents() {
        return components;
    }
    
    public void setComponents(Map<String, Object> components) {
        this.components = components;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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
    
    public Long getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}

