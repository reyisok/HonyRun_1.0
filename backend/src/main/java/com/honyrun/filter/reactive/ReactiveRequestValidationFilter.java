package com.honyrun.filter.reactive;

import com.honyrun.exception.AuthenticationException;
import com.honyrun.exception.ValidationException;
import com.honyrun.interceptor.ReactiveRequestValidationInterceptor;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.util.ErrorDetailsUtil;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.TraceIdUtil;
import com.honyrun.util.common.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.nio.charset.StandardCharsets;

/**
 * 响应式请求参数统一校验过滤器
 *
 * 结合 ReactiveRequestValidationInterceptor，在进入业务处理链之前统一进行参数与安全校验，
 * 对非法请求返回 400，并记录 traceId / path / details / 响应体。
 */
public class ReactiveRequestValidationFilter implements WebFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveRequestValidationFilter.class);

    private final ReactiveRequestValidationInterceptor interceptor;
    private final ErrorDetailsUtil errorDetailsUtil;

    public ReactiveRequestValidationFilter(ReactiveRequestValidationInterceptor interceptor,
                                           ErrorDetailsUtil errorDetailsUtil) {
        this.interceptor = interceptor;
        this.errorDetailsUtil = errorDetailsUtil;
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return interceptor.intercept(exchange)
                .flatMap(valid -> {
                    if (Boolean.TRUE.equals(valid)) {
                        // 如果拦截器已缓存请求体，则在通过验证后重包装请求体，确保下游可再次读取
                        Object cachedBodyObj = exchange.getAttribute("CACHED_REQUEST_BODY");
                        if (cachedBodyObj instanceof String) {
                            String cachedBody = (String) cachedBodyObj;
                            if (cachedBody != null && !cachedBody.isEmpty()) {
                                ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                                    @Override
                                    public Flux<DataBuffer> getBody() {
                                        return Flux.just(exchange.getResponse().bufferFactory().wrap(cachedBody.getBytes(StandardCharsets.UTF_8)));
                                    }
                                };
                                ServerWebExchange mutated = exchange.mutate().request(decoratedRequest).build();
                                return chain.filter(mutated);
                            }
                        }
                        return chain.filter(exchange);
                    }

                    return Mono.deferContextual(ctxView -> Mono.fromCallable(() -> {
                        String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String requestPath = exchange.getRequest().getPath().value();
                        Map<String, Object> details = errorDetailsUtil.buildSimpleErrorDetails("ValidationError", requestPath);

                        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                                .success(false)
                                .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                                .message("请求参数验证失败")
                                .traceId(traceId)
                                .details(details)
                                .path(requestPath)
                                .build();

                        String body = JsonUtil.toJson(apiResponse);
                        LoggingUtil.warn(logger, "请求参数验证失败 - Path: {}, Details: {}, Body: {}",
                                requestPath, details, body);

                        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        return exchange.getResponse().writeWith(
                                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
                        );
                    }))
                    .flatMap(mono -> mono);
                })
                .onErrorResume(AuthenticationException.class, ex -> {
                    return Mono.deferContextual(ctxView -> Mono.fromCallable(() -> {
                        String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String requestPath = exchange.getRequest().getPath().value();
                        Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, requestPath);

                        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                                .success(false)
                                .code(String.valueOf(HttpStatus.UNAUTHORIZED.value()))
                                .message(ex.getMessage())
                                .traceId(traceId)
                                .details(details)
                                .path(requestPath)
                                .build();

                        String body = JsonUtil.toJson(apiResponse);
                        LoggingUtil.warn(logger, "认证异常 - Path: {}, Message: {}, Details: {}, Body: {}",
                                requestPath, ex.getMessage(), details, body);

                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        return exchange.getResponse().writeWith(
                                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
                        );
                    }))
                    .flatMap(mono -> mono);
                })
                .onErrorResume(ValidationException.class, ex -> {
                    return Mono.deferContextual(ctxView -> Mono.fromCallable(() -> {
                        String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String requestPath = exchange.getRequest().getPath().value();
                        Map<String, Object> details = errorDetailsUtil.buildErrorDetails(ex, requestPath);

                        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                                .success(false)
                                .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                                .message(ex.getMessage())
                                .traceId(traceId)
                                .details(details)
                                .path(requestPath)
                                .build();

                        String body = JsonUtil.toJson(apiResponse);
                        LoggingUtil.warn(logger, "验证异常 - Path: {}, Message: {}, Details: {}, Body: {}",
                                requestPath, ex.getMessage(), details, body);

                        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        return exchange.getResponse().writeWith(
                                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
                        );
                    }))
                    .flatMap(mono -> mono);
                })
                .onErrorResume(error -> {
                    return Mono.deferContextual(ctxView -> Mono.fromCallable(() -> {
                        String traceId = TraceIdUtil.getOrGenerateTraceId(ctxView);
                        String requestPath = exchange.getRequest().getPath().value();
                        Map<String, Object> details = errorDetailsUtil.buildSimpleErrorDetails("ValidationFilterException", requestPath);

                        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                                .success(false)
                                .code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                                .message("请求校验过滤器异常")
                                .traceId(traceId)
                                .details(details)
                                .path(requestPath)
                                .build();

                        String body = JsonUtil.toJson(apiResponse);
                        LoggingUtil.error(logger, "校验过滤器异常 - Path: {}, Error: {}, Body: {}",
                                requestPath, error.getMessage(), body);

                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        return exchange.getResponse().writeWith(
                                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
                        );
                    }))
                    .flatMap(mono -> mono);
                });
    }

    @Override
    public int getOrder() {
        // 在签名过滤器之后执行，确保先进行安全签名校验
        return Ordered.HIGHEST_PRECEDENCE + 15;
    }
}
