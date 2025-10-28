package com.honyrun.config.reactive;

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import reactor.netty.resources.LoopResources;

/**
 * WebFlux响应式配置类
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 16:05:00
 * @modified 2025-07-01 16:05:00
 *
 *           Copyright © 2025 HonyRun. All rights reserved.
 *
 *           配置WebFlux响应式Web框架的核心参数
 *           包括Netty服务器配置、背压策略、CORS跨域配置等
 */
@Configuration
public class WebFluxConfig {

    /**
     * 创建配置好的ObjectMapper
     *
     * 【@Primary合理性分析】：
     * 1. 全局唯一性：ObjectMapper在整个应用中应该保持一致的序列化配置
     * 2. Spring Boot集成：WebFlux框架期望有一个默认的ObjectMapper
     * 3. 避免注入歧义：防止多个ObjectMapper实现导致的Bean选择问题
     * 4. 标准实践：符合Spring WebFlux官方推荐的配置方式
     *
     * 【保留@Primary的原因】：
     * - ObjectMapper是基础设施组件，需要全局统一配置
     * - WebFlux自动配置期望有默认的ObjectMapper
     * - 使用@Qualifier会增加配置复杂性，违背简单性原则
     *
     * @return 配置好的ObjectMapper实例
     */
    @Bean("webFluxObjectMapper")
    @Primary
    public ObjectMapper objectMapper() {
        return createObjectMapper();
    }

    /**
     * 创建ObjectMapper实例
     *
     * @return ObjectMapper实例
     */
    private ObjectMapper createObjectMapper() {
        // 禁用Unicode转义，确保中文字符正确显示
        JsonFactory factory = JsonFactory.builder()
                .disable(JsonWriteFeature.ESCAPE_NON_ASCII)
                .build();
        ObjectMapper mapper = new ObjectMapper(factory);

        // 注册Java时间模块
        mapper.registerModule(new JavaTimeModule());

        // 配置序列化特性
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);

        // 配置反序列化特性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        return mapper;
    }

    /**
     * 配置Netty Web服务器定制器
     *
     * @return Netty Web服务器工厂定制器
     */
    @Bean("nettyCustomizer")
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> nettyCustomizer() {
        return factory -> {
            factory.addServerCustomizers(httpServer -> {
                // 配置事件循环组
                LoopResources loopResources = LoopResources.create("honyrun-http",
                        Runtime.getRuntime().availableProcessors(), true);

                return httpServer
                        .runOn(loopResources)
                        // 配置TCP参数
                        .option(io.netty.channel.ChannelOption.SO_KEEPALIVE, true)
                        .option(io.netty.channel.ChannelOption.SO_BACKLOG, 1024)
                        // 配置连接超时
                        .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                        // 配置接收缓冲区
                        .option(io.netty.channel.ChannelOption.SO_RCVBUF, 64 * 1024)
                        // 配置发送缓冲区
                        .option(io.netty.channel.ChannelOption.SO_SNDBUF, 64 * 1024);
            });
        };
    }

}
