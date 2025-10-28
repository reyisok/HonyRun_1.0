package com.honyrun.filter.reactive;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 响应大小限制过滤器
 * 限制HTTP响应体的大小，防止内存溢出和资源耗尽
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  18:30:00
 * @modified 2025-07-01 18:30:00
 *
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 功能特性：
 * 1. 监控响应体大小，超出限制时中断响应
 * 2. 支持配置化的大小限制和错误消息
 * 3. 提供详细的日志记录和监控指标
 * 4. 优雅处理超限情况，返回标准错误响应
 */
@Component("responseSizeLimitFilter")
public class ResponseSizeLimitFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseSizeLimitFilter.class);

    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入依赖
     */
    public ResponseSizeLimitFilter(UnifiedConfigManager unifiedConfigManager) {
        this.unifiedConfigManager = unifiedConfigManager;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return unifiedConfigManager.getBooleanConfig("honyrun.response.size-check-enabled", true)
            .flatMap(sizeCheckEnabled -> {
                if (!sizeCheckEnabled) {
                    return chain.filter(exchange);
                }
                
                return Mono.zip(
                    unifiedConfigManager.getIntegerConfig("honyrun.response.max-size", 52428800),
                    unifiedConfigManager.getStringConfig("honyrun.response.size-exceeded-message", "Response size exceeds maximum allowed limit")
                ).flatMap(tuple -> {
                    long maxResponseSize = tuple.getT1();
                    String sizeExceededMessage = tuple.getT2();
                    
                    ServerHttpResponse originalResponse = exchange.getResponse();
                    ResponseSizeLimitDecorator decoratedResponse = new ResponseSizeLimitDecorator(
                            originalResponse, maxResponseSize, sizeExceededMessage);

                    return chain.filter(exchange.mutate().response(decoratedResponse).build());
                });
            });
    }

    /**
     * 响应大小限制装饰器
     * 包装原始响应，监控和限制响应体大小
     */
    private static class ResponseSizeLimitDecorator extends ServerHttpResponseDecorator {

        private final long maxSize;
        private final String exceededMessage;
        private final AtomicLong currentSize = new AtomicLong(0);
        private volatile boolean limitExceeded = false;

        public ResponseSizeLimitDecorator(ServerHttpResponse delegate, long maxSize, String exceededMessage) {
            super(delegate);
            this.maxSize = maxSize;
            this.exceededMessage = exceededMessage;
        }

        @Override
        public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
            if (limitExceeded) {
                return handleSizeExceeded();
            }

            Flux<DataBuffer> flux = Flux.from(body)
                    .cast(DataBuffer.class)
                    .doOnNext(dataBuffer -> {
                        long bufferSize = dataBuffer.readableByteCount();
                        long totalSize = currentSize.addAndGet(bufferSize);
                        
                        LoggingUtil.debug(logger, "响应数据块大小: {} bytes, 累计大小: {} bytes, 限制: {} bytes", 
                                bufferSize, totalSize, maxSize);

                        if (totalSize > maxSize && !limitExceeded) {
                            limitExceeded = true;
                            LoggingUtil.warn(logger, "响应大小超出限制: {} bytes > {} bytes", totalSize, maxSize);
                        }
                    })
                    .takeWhile(dataBuffer -> !limitExceeded)
                    .doOnComplete(() -> {
                        if (!limitExceeded) {
                            LoggingUtil.debug(logger, "响应完成，总大小: {} bytes", currentSize.get());
                        }
                    })
                    .doOnError(error -> LoggingUtil.error(logger, "响应处理出错", error));

            return super.writeWith(flux)
                    .onErrorResume(error -> {
                        if (limitExceeded) {
                            return handleSizeExceeded();
                        }
                        return Mono.error(error);
                    });
        }

        /**
         * 处理响应大小超限情况
         *
         * @return 错误响应的Mono包装
         */
        private Mono<Void> handleSizeExceeded() {
            LoggingUtil.warn(logger, "响应大小超出限制，返回错误响应");

            // 设置错误状态和响应头（不清除原有响应头，避免影响Content-Type）
            setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
            
            // 确保Content-Type正确设置
            if (getHeaders().getContentType() == null) {
                getHeaders().setContentType(MediaType.APPLICATION_JSON);
            }

            // 创建错误响应
            ApiResponse<Void> errorResponse = ApiResponse.error("RESPONSE_TOO_LARGE", exceededMessage);
            String errorJson = convertToJson(errorResponse);

            DataBufferFactory bufferFactory = bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(errorJson.getBytes(StandardCharsets.UTF_8));

            return super.writeWith(Mono.just(buffer))
                    .doFinally(signalType -> DataBufferUtils.release(buffer));
        }

        /**
         * 将对象转换为JSON字符串
         *
         * @param response 响应对象
         * @return JSON字符串
         */
        private String convertToJson(ApiResponse<Void> response) {
            try {
                return String.format(
                        "{\"success\":false,\"code\":\"%s\",\"message\":\"%s\",\"data\":null,\"timestamp\":\"%s\"}",
                        response.getCode(),
                        response.getMessage(),
                        response.getTimestamp()
                );
            } catch (Exception e) {
                LoggingUtil.error(logger, "转换错误响应为JSON失败", e);
                return "{\"success\":false,\"code\":\"RESPONSE_TOO_LARGE\",\"message\":\"Response size exceeds limit\",\"data\":null}";
            }
        }
    }
}


