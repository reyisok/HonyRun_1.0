package com.honyrun.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * 网络工具类
 * 提供安全的客户端IP地址提取功能
 *
 * @author Mr.Rey
 * @created 2025-07-01 19:15:00
 * @modified 2025-07-01 19:15:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public final class NetworkUtil {

    private static final Logger logger = LoggerFactory.getLogger(NetworkUtil.class);

    private NetworkUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 安全地获取客户端IP地址
     * 按优先级顺序检查各种可能的IP来源
     *
     * @param exchange ServerWebExchange对象
     * @return 客户端IP地址，如果无法获取则返回"unknown"
     */
    public static String getClientIpAddress(ServerWebExchange exchange) {
        if (exchange == null || exchange.getRequest() == null) {
            return "unknown";
        }

        // 1. 检查 X-Forwarded-For 头（代理服务器设置）
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (isValidIp(xForwardedFor) && xForwardedFor != null) {
            // X-Forwarded-For 可能包含多个IP，取第一个
            return xForwardedFor.split(",")[0].trim();
        }

        // 2. 检查 X-Real-IP 头（Nginx等反向代理设置）
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (isValidIp(xRealIp) && xRealIp != null) {
            return xRealIp.trim();
        }

        // 3. 检查 Proxy-Client-IP 头
        String proxyClientIp = exchange.getRequest().getHeaders().getFirst("Proxy-Client-IP");
        if (isValidIp(proxyClientIp) && proxyClientIp != null) {
            return proxyClientIp.trim();
        }

        // 4. 检查 WL-Proxy-Client-IP 头（WebLogic）
        String wlProxyClientIp = exchange.getRequest().getHeaders().getFirst("WL-Proxy-Client-IP");
        if (isValidIp(wlProxyClientIp) && wlProxyClientIp != null) {
            return wlProxyClientIp.trim();
        }

        // 5. 检查 HTTP_CLIENT_IP 头
        String httpClientIp = exchange.getRequest().getHeaders().getFirst("HTTP_CLIENT_IP");
        if (isValidIp(httpClientIp) && httpClientIp != null) {
            return httpClientIp.trim();
        }

        // 6. 检查 HTTP_X_FORWARDED_FOR 头
        String httpXForwardedFor = exchange.getRequest().getHeaders().getFirst("HTTP_X_FORWARDED_FOR");
        if (isValidIp(httpXForwardedFor) && httpXForwardedFor != null) {
            return httpXForwardedFor.split(",")[0].trim();
        }

        // 7. 最后使用远程地址（使用Optional避免空指针）
        return Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(InetSocketAddress::getAddress)
                .map(address -> address.getHostAddress())
                .orElse("unknown");
    }

    /**
     * 验证IP地址是否有效
     *
     * @param ip IP地址字符串
     * @return 如果IP有效返回true，否则返回false
     */
    private static boolean isValidIp(String ip) {
        return ip != null && 
               !ip.trim().isEmpty() && 
               !"unknown".equalsIgnoreCase(ip.trim()) &&
               !"0:0:0:0:0:0:0:1".equals(ip.trim()) &&
               !"127.0.0.1".equals(ip.trim());
    }

    /**
     * 检查IP地址是否为内网地址
     *
     * @param ip IP地址字符串
     * @return 如果是内网地址返回true，否则返回false
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }

        String trimmedIp = ip.trim();
        
        // IPv6本地地址
        if ("0:0:0:0:0:0:0:1".equals(trimmedIp) || "::1".equals(trimmedIp)) {
            return true;
        }

        // IPv4本地地址和内网地址
        return trimmedIp.startsWith("127.") ||
               trimmedIp.startsWith("10.") ||
               trimmedIp.startsWith("192.168.") ||
               (trimmedIp.startsWith("172.") && isInRange172(trimmedIp));
    }

    /**
     * 检查172.x.x.x是否在内网范围内（172.16.0.0 - 172.31.255.255）
     *
     * @param ip IP地址字符串
     * @return 如果在范围内返回true，否则返回false
     */
    private static boolean isInRange172(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                int secondOctet = Integer.parseInt(parts[1]);
                return secondOctet >= 16 && secondOctet <= 31;
            }
        } catch (NumberFormatException e) {
            LoggingUtil.warn(logger, "Failed to parse IP address for private range check: {}", ip);
        }
        return false;
    }
}

