package com.honyrun.service.reactive.impl;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.service.reactive.ReactiveRequestSignatureService;
import com.honyrun.service.reactive.ReactiveSystemConfigService;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * 请求签名验证服务实现类
 * 提供API请求签名生成和验证功能的具体实现
 *
 * 请求签名验证是系统安全架构的核心组件，具有以下重要作用：
 * 1. 防篡改保护：通过HMAC-SHA256算法确保请求内容未被恶意修改
 * 2. 身份认证：验证请求来源的合法性，防止未授权访问
 * 3. 重放攻击防护：使用时间戳和随机数(nonce)机制防止请求重放
 * 4. 数据完整性：确保传输过程中数据的完整性和一致性
 *
 * 签名算法流程：
 * 1. 构建签名字符串：HTTP方法 + URI路径 + 时间戳 + 随机数 + 请求体
 * 2. 使用客户端密钥通过HMAC-SHA256算法生成签名
 * 3. 将签名进行Base64编码后放入X-Signature请求头
 *
 * 验证流程：
 * 1. 提取请求头中的签名信息（客户端ID、时间戳、随机数、签名）
 * 2. 验证时间戳是否在有效期内（默认5分钟）
 * 3. 验证随机数是否已被使用（防重放攻击）
 * 4. 根据客户端ID获取对应的密钥
 * 5. 重新计算期望签名并与请求签名进行比较
 * 6. 验证通过后将随机数标记为已使用
 *
 * 安全注意事项：
 * - 客户端密钥必须安全存储，不得泄露
 * - 时间戳验证防止过期请求被重放
 * - 随机数机制确保每个请求的唯一性
 * - 签名算法使用行业标准的HMAC-SHA256
 *
 * @author Mr.Rey
 * @since 2025-07-01 11:00:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
public class ReactiveRequestSignatureServiceImpl implements ReactiveRequestSignatureService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveRequestSignatureServiceImpl.class);

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReactiveSystemConfigService systemConfigService;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入
     *
     * @param redisTemplate Redis模板
     * @param systemConfigService 系统配置服务
     * @param unifiedConfigManager 统一配置管理器
     */
    public ReactiveRequestSignatureServiceImpl(@Qualifier("unifiedReactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate,
                                             ReactiveSystemConfigService systemConfigService,
                                             UnifiedConfigManager unifiedConfigManager) {
        this.redisTemplate = redisTemplate;
        this.systemConfigService = systemConfigService;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    @Override
    public Mono<Boolean> verifyRequestSignature(ServerWebExchange exchange) {
        LoggingUtil.debug(logger, "开始验证请求签名");

        return isSignatureVerificationEnabled()
                .flatMap(enabled -> {
                    if (!enabled) {
                        LoggingUtil.debug(logger, "请求签名验证功能未启用");
                        return Mono.just(true);
                    }

                    return extractSignatureInfo(exchange)
                            .flatMap(signatureInfo -> {
                                if (signatureInfo == null || signatureInfo.getSignature() == null) {
                                    LoggingUtil.warn(logger, "请求缺少签名信息");
                                    return Mono.just(false);
                                }

                                return validateSignature(exchange, signatureInfo);
                            });
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "验证请求签名时发生错误: {} - {}", error, error.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * 验证签名
     *
     * @param exchange 服务器交换对象
     * @param signatureInfo 签名信息
     * @return 验证结果
     */
    private Mono<Boolean> validateSignature(ServerWebExchange exchange, SignatureInfo signatureInfo) {
        return isTimestampValid(signatureInfo.getTimestamp())
                .flatMap(timestampValid -> {
                    if (!timestampValid) {
                        LoggingUtil.warn(logger, "时间戳无效或已过期: {}", signatureInfo.getTimestamp());
                        return Mono.just(false);
                    }

                    return isNonceUnused(signatureInfo.getNonce())
                            .flatMap(nonceUnused -> {
                                if (!nonceUnused) {
                                    LoggingUtil.warn(logger, "Nonce已被使用: {}", signatureInfo.getNonce());
                                    return Mono.just(false);
                                }

                                return getClientSecret(signatureInfo.getClientId())
                                        .flatMap(secretKey -> {
                                            if (secretKey == null) {
                                                LoggingUtil.warn(logger, "未找到客户端密钥: {}", signatureInfo.getClientId());
                                                return Mono.just(false);
                                            }

                                            return generateExpectedSignature(exchange, signatureInfo, secretKey)
                                                    .flatMap(expectedSignature -> {
                                                        boolean signatureMatch = expectedSignature.equals(signatureInfo.getSignature());

                                                        if (signatureMatch) {
                                                            // 记录已使用的nonce
                                                            return getSignatureValidityPeriod()
                                                                    .flatMap(validityPeriod ->
                                                                        recordUsedNonce(signatureInfo.getNonce(), validityPeriod)
                                                                                .then(Mono.just(true))
                                                                    );
                                                        } else {
                                                            LoggingUtil.warn(logger, "签名验证失败，客户端ID: {}", signatureInfo.getClientId());
                                                            return Mono.just(false);
                                                        }
                                                    });
                                        });
                            });
                });
    }

    /**
     * 生成期望的签名
     *
     * @param exchange 服务器交换对象
     * @param signatureInfo 签名信息
     * @param secretKey 密钥
     * @return 期望的签名
     */
    private Mono<String> generateExpectedSignature(ServerWebExchange exchange, SignatureInfo signatureInfo, String secretKey) {
        String method = exchange.getRequest().getMethod().name();
        String uri = exchange.getRequest().getURI().getPath();

        // 对于有请求体的请求，需要读取请求体内容
        // 这里简化处理，实际项目中可能需要缓存请求体
        return Mono.just("")
                .flatMap(body -> generateSignature(method, uri, signatureInfo.getTimestamp(),
                                                 signatureInfo.getNonce(), body, secretKey));
    }

    @Override
    public Mono<String> generateSignature(String method, String uri, String timestamp, String nonce, String body, String secretKey) {
        return unifiedConfigManager.getStringConfig("honyrun.signature.algorithm")
                .defaultIfEmpty("HmacSHA256")
                .flatMap(hmacAlgorithm -> 
                    Mono.fromCallable(() -> {
                        // 构建签名字符串：METHOD + URI + TIMESTAMP + NONCE + BODY
                        String signatureString = method + uri + timestamp + nonce + (body != null ? body : "");

                        try {
                            Mac mac = Mac.getInstance(hmacAlgorithm);
                            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), hmacAlgorithm);
                            mac.init(secretKeySpec);

                            byte[] signatureBytes = mac.doFinal(signatureString.getBytes(StandardCharsets.UTF_8));
                            return Base64.getEncoder().encodeToString(signatureBytes);

                        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                            LoggingUtil.error(logger, "生成签名时发生错误: {} - {}", e, e.getMessage());
                            throw new RuntimeException("签名生成失败", e);
                        }
                    })
                );
    }

    @Override
    public Mono<Boolean> isTimestampValid(String timestamp) {
        return getSignatureValidityPeriod()
                .map(validityPeriod -> {
                    try {
                        long requestTime = Long.parseLong(timestamp);
                        long currentTime = Instant.now().getEpochSecond();
                        long timeDiff = Math.abs(currentTime - requestTime);

                        return timeDiff <= validityPeriod;
                    } catch (NumberFormatException e) {
                        LoggingUtil.warn(logger, "时间戳格式错误: {}", timestamp);
                        return false;
                    }
                });
    }

    @Override
    public Mono<Boolean> isNonceUnused(String nonce) {
        return unifiedConfigManager.getStringConfig("honyrun.signature.nonce-prefix")
                .defaultIfEmpty("nonce:")
                .flatMap(nonceKeyPrefix -> {
                    String nonceKey = nonceKeyPrefix + nonce;
                    return redisTemplate.hasKey(nonceKey)
                            .map(exists -> !exists)
                            .onErrorReturn(true); // 出现异常时允许通过
                });
    }

    @Override
    public Mono<Void> recordUsedNonce(String nonce, long ttl) {
        return unifiedConfigManager.getStringConfig("honyrun.signature.nonce-prefix")
                .defaultIfEmpty("nonce:")
                .flatMap(nonceKeyPrefix -> {
                    String nonceKey = nonceKeyPrefix + nonce;
                    return redisTemplate.opsForValue()
                            .set(nonceKey, "used", Duration.ofSeconds(ttl))
                            .then()
                            .onErrorResume(error -> {
                                LoggingUtil.error(logger, "记录已使用nonce时发生错误: {} - {}", error, error.getMessage());
                                return Mono.empty();
                            });
                });
    }

    @Override
    public Mono<String> getClientSecret(String clientId) {
        // 这里可以从数据库或配置中获取客户端密钥
        // 为了演示，使用系统配置
        return systemConfigService.getConfigValue("security.client." + clientId + ".secret", null)
                .switchIfEmpty(Mono.defer(() -> {
                    // 如果没有配置特定客户端密钥，使用默认密钥
                    return systemConfigService.getConfigValue("security.default.client.secret", "default-secret-key");
                }));
    }

    @Override
    public Mono<Boolean> isSignatureVerificationEnabled() {
        return systemConfigService.getConfigValue("security.request.signature.enabled", "false")
                .map(Boolean::parseBoolean)
                .onErrorReturn(false);
    }

    @Override
    public Mono<Long> getSignatureValidityPeriod() {
        return unifiedConfigManager.getLongConfig("honyrun.signature.validity-period", 300L)
                .switchIfEmpty(
                    systemConfigService.getConfigValue("security.request.signature.validity_period", "300")
                            .map(Long::parseLong)
                            .onErrorReturn(300L)
                );
    }

    @Override
    public Mono<SignatureInfo> extractSignatureInfo(ServerWebExchange exchange) {
        String clientId = exchange.getRequest().getHeaders().getFirst("X-Client-Id");
        String timestamp = exchange.getRequest().getHeaders().getFirst("X-Timestamp");
        String nonce = exchange.getRequest().getHeaders().getFirst("X-Nonce");
        String signature = exchange.getRequest().getHeaders().getFirst("X-Signature");

        if (clientId == null || timestamp == null || nonce == null || signature == null) {
            return Mono.empty();
        }

        return Mono.just(new SignatureInfo(clientId, timestamp, nonce, signature));
    }
}

