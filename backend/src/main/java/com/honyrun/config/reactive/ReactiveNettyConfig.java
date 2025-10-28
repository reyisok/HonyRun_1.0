package com.honyrun.config.reactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.honyrun.config.properties.NettyProperties;
import com.honyrun.util.LoggingUtil;

import reactor.netty.http.server.HttpServer;

/**
 * Netty服务器配置
 *
 * 配置Netty服务器参数，优化响应式Web服务器性能
 * 支持HTTP/2协议、连接超时、背压控制等高级特性
 *
 * 主要配置：
 * - 连接超时设置
 * - HTTP/2协议支持
 * - 背压控制配置
 * - 缓冲区大小优化
 * - 线程池配置
 *
 * 性能优化：
 * - 连接池管理
 * - 内存使用优化
 * - 并发连接控制
 * - 请求处理优化
 *
 * 响应式特性：
 * - 非阻塞I/O：支持高并发非阻塞连接
 * - 背压支持：自动处理背压，防止内存溢出
 * - 事件循环：优化事件循环线程配置
 * - 流式处理：支持流式数据传输
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  16:00:00
 * @modified 2025-07-01 11:00:00
 *
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Configuration
public class ReactiveNettyConfig {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveNettyConfig.class);

    private final NettyProperties nettyProperties;

    /**
     * 构造函数注入NettyProperties
     *
     * @param nettyProperties Netty配置属性
     */
    public ReactiveNettyConfig(NettyProperties nettyProperties) {
        this.nettyProperties = nettyProperties;
    }

    /**
     * 开发环境Netty响应式Web服务器工厂
     *
     * <p>
     * 专为开发环境优化的Netty服务器配置，提供快速启动和调试支持
     * </p>
     *
     * @return NettyReactiveWebServerFactory实例
     */
    @Bean("devNettyReactiveWebServerFactory")
    @Profile("dev") // 仅在开发环境激活，提供开发环境专用的Netty服务器配置
    public NettyReactiveWebServerFactory devNettyReactiveWebServerFactory() {
        LoggingUtil.info(logger, "配置开发环境Netty响应式Web服务器工厂");

        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();

        // 设置服务器端口
        factory.setPort(nettyProperties.getServer().getPort());

        // 添加Netty服务器自定义配置
        factory.addServerCustomizers(nettyServerCustomizer());

        LoggingUtil.info(logger, "开发环境Netty响应式Web服务器工厂配置完成，端口: {}", nettyProperties.getServer().getPort());
        return factory;
    }

    /**
     * 生产环境Netty响应式Web服务器工厂
     *
     * <p>
     * 专为生产环境优化的Netty服务器配置，提供高性能和稳定性保障
     * </p>
     *
     * @return NettyReactiveWebServerFactory实例
     */
    @Bean("prodNettyReactiveWebServerFactory")
    @Profile("prod") // 仅在生产环境激活，提供生产环境专用的Netty服务器配置
    public NettyReactiveWebServerFactory prodNettyReactiveWebServerFactory() {
        LoggingUtil.info(logger, "配置生产环境Netty响应式Web服务器工厂");

        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();

        // 设置服务器端口
        factory.setPort(nettyProperties.getServer().getPort());

        // 添加Netty服务器自定义配置
        factory.addServerCustomizers(nettyServerCustomizer());

        LoggingUtil.info(logger, "生产环境Netty响应式Web服务器工厂配置完成，端口: {}", nettyProperties.getServer().getPort());
        return factory;
    }

    /**
     * Netty服务器自定义配置
     *
     * @return NettyServerCustomizer实例
     */
    @Bean("nettyServerCustomizer")
    public NettyServerCustomizer nettyServerCustomizer() {
        LoggingUtil.info(logger, "配置Netty服务器自定义参数");

        return httpServer -> {
            LoggingUtil.info(logger, "应用Netty服务器自定义配置");

            return httpServer
                    // 配置连接超时（适用于服务器通道）
                    .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                            nettyProperties.getConnection().getConnectTimeoutMillis())

                    // 配置SO_REUSEADDR（适用于服务器通道）
                    .option(io.netty.channel.ChannelOption.SO_REUSEADDR,
                            nettyProperties.getConnection().isReuseAddress())

                    // 配置接收缓冲区大小（适用于服务器通道）
                    .option(io.netty.channel.ChannelOption.SO_RCVBUF,
                            nettyProperties.getBuffer().getReceiveBufferSize())

                    // 配置子通道选项（适用于客户端连接通道）
                    .childOption(io.netty.channel.ChannelOption.SO_KEEPALIVE,
                            nettyProperties.getConnection().isKeepAlive())
                    .childOption(io.netty.channel.ChannelOption.TCP_NODELAY,
                            nettyProperties.getConnection().isTcpNoDelay())
                    .childOption(io.netty.channel.ChannelOption.SO_SNDBUF,
                            nettyProperties.getBuffer().getSendBufferSize())
                    .childOption(io.netty.channel.ChannelOption.SO_RCVBUF,
                            nettyProperties.getBuffer().getReceiveBufferSize())

                    // 配置空闲超时
                    .idleTimeout(nettyProperties.getConnection().getIdleTimeout())

                    // 配置请求超时
                    .requestTimeout(nettyProperties.getConnection().getRequestTimeout())

                    // 配置HTTP/2支持
                    .protocol(reactor.netty.http.HttpProtocol.H2C, reactor.netty.http.HttpProtocol.HTTP11)

                    // 配置访问日志
                    .accessLog(nettyProperties.getServer().isAccessLog())

                    // 配置请求解码器
                    .httpRequestDecoder(spec -> spec
                            .maxInitialLineLength(nettyProperties.getHttp().getMaxInitialLineLength())
                            .maxHeaderSize(nettyProperties.getHttp().getMaxHeaderSize())
                            // 移除过时的 maxChunkSize 配置，Netty 5 不再支持此配置
                            // .maxChunkSize(16384) - 已过时，将在 2.0.0 版本中移除
                            .validateHeaders(nettyProperties.getHttp().isValidateHeaders())
                            .initialBufferSize(nettyProperties.getHttp().getInitialBufferSize()))

                    // 配置压缩
                    .compress(nettyProperties.getServer().isCompression())

                    // 配置转发头处理
                    .forwarded(nettyProperties.getServer().isForwarded())

                    // 配置错误处理
                    .doOnConnection(connection -> {
                        LoggingUtil.debug(logger, "新连接建立: {}", connection.channel().remoteAddress());

                        // 配置连接级别的处理器
                        connection.addHandlerLast("connection-logger", new io.netty.handler.logging.LoggingHandler());
                    })

                    // 配置绑定完成处理
                    .doOnBound(server -> {
                        LoggingUtil.info(logger, "Netty服务器绑定完成，地址: {}", server.address());
                    })

                    // 配置启动完成处理
                    .doOnChannelInit((observer, channel, remoteAddress) -> {
                        LoggingUtil.debug(logger, "通道初始化完成，远程地址: {}", remoteAddress);
                    });
        };
    }

    /**
     * 配置HTTP服务器
     *
     * @return HttpServer实例
     */
    @Bean("nettyHttpServer")
    public HttpServer httpServer() {
        LoggingUtil.info(logger, "配置HTTP服务器");

        return HttpServer.create()
                .port(nettyProperties.getServer().getPort())

                // 配置Wiretap用于调试
                .wiretap(nettyProperties.getServer().isWiretap())

                // 配置事件循环组
                .runOn(reactor.netty.resources.LoopResources.create(
                        nettyProperties.getThreadPool().getHttpThreadNamePrefix(),
                        nettyProperties.getThreadPool().getHttpEventLoopThreads(),
                        nettyProperties.getThreadPool().isDaemon()))

                // 配置内存分配器
                .option(io.netty.channel.ChannelOption.ALLOCATOR, io.netty.buffer.PooledByteBufAllocator.DEFAULT)

                // 配置写缓冲区水位
                .option(io.netty.channel.ChannelOption.WRITE_BUFFER_WATER_MARK,
                        new io.netty.channel.WriteBufferWaterMark(
                                nettyProperties.getBuffer().getWriteBufferLowWaterMark(),
                                nettyProperties.getBuffer().getWriteBufferHighWaterMark()))

                // 配置最大连接数
                .option(io.netty.channel.ChannelOption.SO_BACKLOG, nettyProperties.getConnection().getBacklog())

                // 配置处理器
                .doOnConnection(connection -> {
                    // 添加读超时处理器
                    connection.addHandlerLast("read-timeout",
                            new io.netty.handler.timeout.ReadTimeoutHandler(
                                    nettyProperties.getConnection().getReadTimeoutSeconds()));

                    // 添加写超时处理器
                    connection.addHandlerLast("write-timeout",
                            new io.netty.handler.timeout.WriteTimeoutHandler(
                                    nettyProperties.getConnection().getWriteTimeoutSeconds()));

                    LoggingUtil.debug(logger, "连接处理器配置完成");
                })

                // 配置错误处理
                .doOnChannelInit((observer, channel, remoteAddress) -> {
                    LoggingUtil.debug(logger, "HTTP服务器通道初始化，远程地址: {}", remoteAddress);
                });
    }

    /**
     * 配置连接提供者
     *
     * @return ConnectionProvider实例
     */
    @Bean("nettyConnectionProvider")
    public reactor.netty.resources.ConnectionProvider nettyConnectionProvider() {
        LoggingUtil.info(logger, "配置连接提供者");

        return reactor.netty.resources.ConnectionProvider.builder("honyrun-connection-pool")
                // 最大连接数
                .maxConnections(nettyProperties.getConnection().getMaxConnections())

                // 等待连接的最大时间
                .pendingAcquireTimeout(nettyProperties.getConnection().getPendingAcquireTimeout())

                // 连接的最大空闲时间
                .maxIdleTime(nettyProperties.getConnection().getMaxIdleTime())

                // 连接的最大生存时间
                .maxLifeTime(nettyProperties.getConnection().getMaxLifeTime())

                // 连接获取重试次数
                .pendingAcquireMaxCount(nettyProperties.getConnection().getPendingAcquireMaxCount())

                // 启用连接池指标
                .metrics(nettyProperties.getConnection().isMetrics())

                // 连接池清理间隔
                .evictInBackground(nettyProperties.getConnection().getEvictInBackground())

                .build();
    }

    /**
     * 配置循环资源
     *
     * @return LoopResources实例
     */
    @Bean("nettyLoopResources")
    public reactor.netty.resources.LoopResources nettyLoopResources() {
        LoggingUtil.info(logger, "配置循环资源");

        return reactor.netty.resources.LoopResources.create(
                nettyProperties.getThreadPool().getThreadNamePrefix(),
                // 工作线程数，通常设置为CPU核心数的2倍
                nettyProperties.getThreadPool().getEventLoopThreads(),
                // 是否使用守护线程
                nettyProperties.getThreadPool().isDaemon());
    }
}


