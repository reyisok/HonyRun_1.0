package com.honyrun.filter.reactive;

import com.honyrun.util.LoggingUtil;
import com.honyrun.util.TraceIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 响应式 TraceId 过滤器
 *
 * - 统一生成/读取 TraceId（优先读取请求头 X-Trace-Id）
 * - 将 TraceId 传播到 Reactor Context 与响应头，便于后续异常处理与日志追踪
 * - 自动集成 MDC（通过 TraceIdUtil.withTraceId）
 *
 * @author Mr.Rey
 * @since 2025-07-02
 */
@Component("reactiveTraceIdFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReactiveTraceIdFilter implements WebFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveTraceIdFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String incomingTraceId = request.getHeaders().getFirst(TraceIdUtil.X_TRACE_ID_HEADER);
        String traceId = (incomingTraceId != null && !incomingTraceId.isBlank())
                ? incomingTraceId
                : TraceIdUtil.generateTraceId();

        // 将最终 TraceId 写入响应头，便于客户端与日志关联
        response.getHeaders().set(TraceIdUtil.X_TRACE_ID_HEADER, traceId);

        LoggingUtil.debug(logger, "TraceIdFilter - 使用TraceId: {}", traceId);

        // 使用 TraceIdUtil 封装上下文与 MDC
        return TraceIdUtil.withTraceId(chain.filter(exchange), traceId);
    }

    @Override
    public int getOrder() {
        // 最高优先级，保证尽早注入 TraceId
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

