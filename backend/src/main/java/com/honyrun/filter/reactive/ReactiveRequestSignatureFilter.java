package com.honyrun.filter.reactive;

import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.service.reactive.ReactiveRequestSignatureService;
import com.honyrun.util.ErrorDetailsUtil;
import com.honyrun.util.common.JsonUtil;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.NetworkUtil;
import com.honyrun.util.TraceIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 请求签名验证过滤器
 * 
 * 这是系统安全防护的第一道防线，负责在请求到达业务逻辑之前进行签名验证。
 * 该过滤器是系统安全架构中不可或缺的组件，具有以下关键作用：
 * 
 * 1. 安全防护：
 *    - 防止API接口被恶意调用和攻击
 *    - 确保只有经过授权的客户端才能访问系统资源
 *    - 防止数据篡改和中间人攻击
 * 
 * 2. 访问控制：
 *    - 在业务逻辑执行前进行身份验证
 *    - 减少无效请求对系统资源的消耗
 *    - 提供统一的安全检查入口
 * 
 * 3. 异常处理：
 *    - 对签名验证失败的请求返回统一的错误响应
 *    - 提供详细的错误代码便于客户端处理
 *    - 记录安全相关的日志信息
 * 
 * 过滤器执行流程：
 * 1. 检查请求路径是否需要签名验证（跳过登录接口和健康检查接口）
 * 2. 调用签名验证服务进行签名校验
 * 3. 验证成功：继续执行后续过滤器链
 * 4. 验证失败：返回401未授权状态码和错误信息
 * 5. 异常处理：返回500内部服务器错误状态码
 * 
 * 安全注意事项：
 * - 该过滤器的执行优先级设置为最高，确保在其他业务逻辑之前执行
 * - 跳过路径配置需要谨慎，避免绕过安全检查
 * - 错误响应不应泄露敏感的系统信息
 * - 所有安全相关的操作都应记录日志
 * 
 * 重要提醒：
 * 请求签名验证是系统安全的核心机制，禁止在生产环境中禁用此功能！
 * 任何对此过滤器的修改都应经过严格的安全评估和测试！
 * 
 * 注意：此类不使用@Component注解，需要在配置类中手动注册Bean
 * 
 * @author Mr.Rey
 * @since 2025-07-01 11:00:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class ReactiveRequestSignatureFilter implements WebFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveRequestSignatureFilter.class);

    private final ReactiveRequestSignatureService requestSignatureService;
    private final ErrorDetailsUtil errorDetailsUtil;

    /**
     * 构造函数
     *
     * @param requestSignatureService 请求签名服务
     * @param errorDetailsUtil 错误详情工具类
     */
    public ReactiveRequestSignatureFilter(ReactiveRequestSignatureService requestSignatureService,
                                        ErrorDetailsUtil errorDetailsUtil) {
        this.requestSignatureService = requestSignatureService;
        this.errorDetailsUtil = errorDetailsUtil;
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        // 跳过不需要签名验证的路径
        if (shouldSkipSignatureVerification(exchange)) {
            return chain.filter(exchange);
        }

        return requestSignatureService.verifyRequestSignature(exchange)
                .flatMap(isValid -> {
                    if (!isValid) {
                        LoggingUtil.warn(logger, "请求签名验证失败，路径: {}, 客户端IP: {}", 
                                       exchange.getRequest().getPath().value(),
                                       getClientIp(exchange));
                        
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        exchange.getResponse().getHeaders().add("X-Error-Code", "INVALID_SIGNATURE");
                        exchange.getResponse().getHeaders().add("X-Error-Message", "Request signature verification failed");
                        
                        return exchange.getResponse().setComplete();
                    }
                    
                    return chain.filter(exchange);
                })
                .onErrorResume(error -> {
                    String traceId = TraceIdUtil.generateTraceId();
                    
                    return Mono.fromCallable(() -> {
                        String requestPath = exchange.getRequest().getPath().value();
                        Map<String, Object> details = errorDetailsUtil.buildSimpleErrorDetails("SignatureVerificationException", requestPath);
                        
                        LoggingUtil.error(logger, "[{}] 签名验证过滤器处理异常: {}", traceId, error.getMessage(), error);
                        
                        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                                .success(false)
                                .code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                                .message("Signature verification error")
                                .traceId(traceId)
                                .details(details)
                                .path(requestPath)
                                .build();

                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");
                        
                        String errorResponse = JsonUtil.toJson(apiResponse);
                        
                        return exchange.getResponse().writeWith(
                            Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
                        );
                    })
                    .contextWrite(context -> TraceIdUtil.putTraceIdToContext(context, traceId))
                    .flatMap(mono -> mono);
                });
    }

    /**
     * 判断是否应该跳过签名验证
     *
     * @param exchange 服务器交换对象
     * @return 如果应该跳过返回true，否则返回false
     */
    private boolean shouldSkipSignatureVerification(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        
        // 跳过以下路径的签名验证
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/logout") ||
               path.startsWith("/api/version/") ||
               path.startsWith("/api/health") ||
               path.startsWith("/api/system/") ||
               path.startsWith("/api/users") ||  // 修复：移除尾部斜杠，匹配/api/users和/api/users/
               path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/webjars/") ||
               path.startsWith("/favicon.ico") ||
               path.startsWith("/static/") ||
               path.startsWith("/public/");
    }

    /**
     * 获取客户端IP地址
     *
     * @param exchange 服务器交换对象
     * @return 客户端IP地址
     */
    private String getClientIp(ServerWebExchange exchange) {
        return NetworkUtil.getClientIpAddress(exchange);
    }

    @Override
    public int getOrder() {
        // 设置在日志过滤器之后，认证过滤器之前执行
        // 签名验证应该是安全防护的第一道防线
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}

