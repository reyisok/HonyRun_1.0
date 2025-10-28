package com.honyrun.service.reactive;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求签名验证服务接口
 * 提供API请求签名生成和验证功能，确保请求的完整性和真实性
 *
 * @author Mr.Rey
 * @since 2025-07-01 11:00:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveRequestSignatureService {

    /**
     * 验证请求签名
     *
     * @param exchange 服务器交换对象
     * @return 如果签名验证通过返回true，否则返回false
     */
    Mono<Boolean> verifyRequestSignature(ServerWebExchange exchange);

    /**
     * 生成请求签名
     *
     * @param method HTTP方法
     * @param uri 请求URI
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param body 请求体内容
     * @param secretKey 密钥
     * @return 生成的签名
     */
    Mono<String> generateSignature(String method, String uri, String timestamp, String nonce, String body, String secretKey);

    /**
     * 验证时间戳是否在有效期内
     *
     * @param timestamp 时间戳
     * @return 如果时间戳有效返回true，否则返回false
     */
    Mono<Boolean> isTimestampValid(String timestamp);

    /**
     * 检查nonce是否已使用过（防重放攻击）
     *
     * @param nonce 随机数
     * @return 如果nonce未使用过返回true，否则返回false
     */
    Mono<Boolean> isNonceUnused(String nonce);

    /**
     * 记录已使用的nonce
     *
     * @param nonce 随机数
     * @param ttl 生存时间（秒）
     * @return 记录结果
     */
    Mono<Void> recordUsedNonce(String nonce, long ttl);

    /**
     * 获取客户端的API密钥
     *
     * @param clientId 客户端ID
     * @return API密钥
     */
    Mono<String> getClientSecret(String clientId);

    /**
     * 检查请求签名功能是否启用
     *
     * @return 如果启用返回true，否则返回false
     */
    Mono<Boolean> isSignatureVerificationEnabled();

    /**
     * 获取签名有效期配置（秒）
     *
     * @return 签名有效期
     */
    Mono<Long> getSignatureValidityPeriod();

    /**
     * 从请求中提取签名信息
     *
     * @param exchange 服务器交换对象
     * @return 签名信息对象
     */
    Mono<SignatureInfo> extractSignatureInfo(ServerWebExchange exchange);

    /**
     * 签名信息封装类
     */
    class SignatureInfo {
        private String clientId;
        private String timestamp;
        private String nonce;
        private String signature;

        public SignatureInfo() {}

        public SignatureInfo(String clientId, String timestamp, String nonce, String signature) {
            this.clientId = clientId;
            this.timestamp = timestamp;
            this.nonce = nonce;
            this.signature = signature;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        @Override
        public String toString() {
            return "SignatureInfo{" +
                    "clientId='" + clientId + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    ", nonce='" + nonce + '\'' +
                    ", signature='[PROTECTED]'" +
                    '}';
        }
    }
}

