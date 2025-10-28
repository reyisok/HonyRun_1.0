package com.honyrun.service.security;

import com.honyrun.util.LoggingUtil;
import com.honyrun.util.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 访问类型检测服务
 * 
 * 负责识别请求来源类型，区分本地访问、内网访问和外网访问
 * 为统一日志记录和安全控制提供基础数据
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-23 22:30:00
 * @version 1.0.0
 */
@Service
public class AccessTypeDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(AccessTypeDetectionService.class);

    /**
     * 访问类型枚举
     */
    public enum AccessType {
        LOCAL("本地访问"),
        INTERNAL("内网访问"), 
        EXTERNAL("外网访问"),
        UNKNOWN("未知访问");

        private final String description;

        AccessType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检测访问类型
     *
     * @param exchange ServerWebExchange对象
     * @return 访问类型
     */
    public Mono<AccessType> detectAccessType(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            String clientIp = NetworkUtil.getClientIpAddress(exchange);
            AccessType accessType = determineAccessType(clientIp);
            
            LoggingUtil.debug(logger, "检测到访问类型: {} - IP: {}", accessType.getDescription(), clientIp);
            return accessType;
        });
    }

    /**
     * 获取客户端IP地址
     *
     * @param exchange ServerWebExchange对象
     * @return 客户端IP地址
     */
    public String getClientIp(ServerWebExchange exchange) {
        return NetworkUtil.getClientIpAddress(exchange);
    }

    /**
     * 判断是否为本地访问
     *
     * @param exchange ServerWebExchange对象
     * @return 是否为本地访问
     */
    public Mono<Boolean> isLocalAccess(ServerWebExchange exchange) {
        return detectAccessType(exchange)
                .map(accessType -> accessType == AccessType.LOCAL);
    }

    /**
     * 判断是否为外部访问
     *
     * @param exchange ServerWebExchange对象
     * @return 是否为外部访问
     */
    public Mono<Boolean> isExternalAccess(ServerWebExchange exchange) {
        return detectAccessType(exchange)
                .map(accessType -> accessType == AccessType.EXTERNAL);
    }

    /**
     * 根据IP地址确定访问类型
     *
     * @param ip IP地址
     * @return 访问类型
     */
    private AccessType determineAccessType(String ip) {
        if (ip == null || "unknown".equals(ip)) {
            return AccessType.UNKNOWN;
        }

        // 本地访问判断
        if (isLocalIp(ip)) {
            return AccessType.LOCAL;
        }

        // 内网访问判断
        if (NetworkUtil.isInternalIp(ip)) {
            return AccessType.INTERNAL;
        }

        // 其他情况视为外网访问
        return AccessType.EXTERNAL;
    }

    /**
     * 判断是否为本地IP
     *
     * @param ip IP地址
     * @return 是否为本地IP
     */
    private boolean isLocalIp(String ip) {
        return "127.0.0.1".equals(ip) || 
               "localhost".equals(ip) || 
               "0:0:0:0:0:0:0:1".equals(ip) || 
               "::1".equals(ip);
    }

    /**
     * 获取访问类型描述信息
     *
     * @param exchange ServerWebExchange对象
     * @return 访问类型描述
     */
    public Mono<String> getAccessTypeDescription(ServerWebExchange exchange) {
        return detectAccessType(exchange)
                .map(AccessType::getDescription);
    }
}
