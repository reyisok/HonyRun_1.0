package com.honyrun.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * 刷新令牌请求DTO
 *
 * 用于接收刷新令牌的请求参数，包含验证注解和数据校验规则
 * 支持响应式数据绑定和参数验证
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  21:30:00
 * @modified 2025-07-01 21:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class RefreshTokenRequest {

    /**
     * 刷新令牌
     * 必填，用于获取新的访问令牌
     */
    @NotBlank(message = "刷新令牌不能为空")
    @JsonProperty("refreshToken")
    private String refreshToken;

    /**
     * 设备ID（可选）
     * 用于设备识别和多设备管理
     */
    @JsonProperty("deviceId")
    private String deviceId;

    /**
     * 客户端IP地址（可选）
     * 用于安全审计和地理位置验证
     */
    @JsonProperty("clientIp")
    private String clientIp;

    /**
     * 默认构造函数
     */
    public RefreshTokenRequest() {
    }

    /**
     * 构造函数
     *
     * @param refreshToken 刷新令牌
     */
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * 完整构造函数
     *
     * @param refreshToken 刷新令牌
     * @param deviceId 设备ID
     * @param clientIp 客户端IP
     */
    public RefreshTokenRequest(String refreshToken, String deviceId, String clientIp) {
        this.refreshToken = refreshToken;
        this.deviceId = deviceId;
        this.clientIp = clientIp;
    }

    // Getter和Setter方法

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
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
     * Builder内部类
     */
    public static class Builder {
        private String refreshToken;
        private String deviceId;
        private String clientIp;

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public RefreshTokenRequest build() {
            return new RefreshTokenRequest(refreshToken, deviceId, clientIp);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshTokenRequest that = (RefreshTokenRequest) o;
        return Objects.equals(refreshToken, that.refreshToken) &&
                Objects.equals(deviceId, that.deviceId) &&
                Objects.equals(clientIp, that.clientIp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(refreshToken, deviceId, clientIp);
    }

    @Override
    public String toString() {
        return "RefreshTokenRequest{" +
                "refreshToken='[PROTECTED]'" +
                ", deviceId='" + deviceId + '\'' +
                ", clientIp='" + clientIp + '\'' +
                '}';
    }
}


