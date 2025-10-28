package com.honyrun.config.cache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.config.retry.IntelligentRetryStrategy;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Redis连接健康检查器
 *
 * <p>
 * <strong>核心功能：</strong>
 * <ul>
 * <li><strong>启动前检查</strong> - 在Bean初始化前检查Redis服务可用性</li>
 * <li><strong>重试机制</strong> - 支持连接失败时的自动重试</li>
 * <li><strong>超时控制</strong> - 避免长时间等待阻塞启动</li>
 * <li><strong>故障快速失败</strong> - 默认情况下Redis故障立即停止应用启动</li>
 * <li><strong>可配置降级</strong> - 仅在用户明确启用时提供降级方案</li>
 * </ul>
 *
 * <p>
 * <strong>解决问题：</strong>
 * <ul>
 * <li>启动时序依赖问题</li>
 * <li>Redis服务未就绪时的连接失败</li>
 * <li>缺乏重试机制导致的启动失败</li>
 * <li>Redis故障时的快速失败机制</li>
 * </ul>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @modified 2025-10-25 19:52:56
 * @version 1.1.0 - 添加可配置降级和故障快速失败机制
 * @since 1.0.0
 */
@Component
public class RedisConnectionHealthChecker {

    private static final Logger logger = LoggerFactory.getLogger(RedisConnectionHealthChecker.class);

    private final IntelligentRetryStrategy retryStrategy;
    private final Environment environment;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入依赖
     *
     * @param retryStrategy        智能重试策略
     * @param environment          Spring环境对象
     * @param unifiedConfigManager 统一配置管理器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public RedisConnectionHealthChecker(IntelligentRetryStrategy retryStrategy,
            Environment environment,
            UnifiedConfigManager unifiedConfigManager) {
        this.retryStrategy = retryStrategy;
        this.environment = environment;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 检查Redis连接健康状态
     *
     * <p>
     * <strong>功能特性：</strong>
     * <ul>
     * <li>TCP连接测试</li>
     * <li>智能重试机制</li>
     * <li>优雅降级支持</li>
     * <li>详细日志记录</li>
     * </ul>
     *
     * @return 连接健康状态的Mono
     */
    public Mono<Boolean> checkRedisHealth() {
        // 配置标准化说明：使用统一配置管理器获取Redis连接信息，符合最佳实践
        // 优先级：环境变量 HONYRUN_REDIS_HOST > 默认值 localhost
        // 使用响应式方式获取配置，避免阻塞调用
        return unifiedConfigManager.getStringConfig("spring.data.redis.host", 
                unifiedConfigManager.getProperty("HONYRUN_REDIS_HOST", "localhost"))
                .flatMap(redisHost -> {

        return unifiedConfigManager.getIntegerConfig("honyrun.redis.port", 8902)
                .flatMap(port -> unifiedConfigManager.getIntegerConfig("HONYRUN_REDIS_PORT", port)
                        .flatMap(redisPort -> {
                            return unifiedConfigManager.getBooleanConfig("honyrun.redis.graceful-degradation.enabled", true)
                                    .flatMap(gracefulDegradationEnabled -> {

                                        LoggingUtil.info(logger, "开始检查Redis连接健康状态 - {}:{}", redisHost, redisPort);

                                        return Mono.fromCallable(() -> testTcpConnection(redisHost, redisPort))
                                                .retryWhen(retryStrategy.getRedisConnectionRetry())
                                                .timeout(Duration.ofSeconds(20))
                                                .doOnSuccess(success -> {
                                                    if (success) {
                                                        LoggingUtil.info(logger, "Redis连接健康检查成功 - {}:{}", redisHost, redisPort);
                                                    } else {
                                                        LoggingUtil.error(logger, "Redis连接健康检查失败 - {}:{}", redisHost, redisPort);
                                                        if (!gracefulDegradationEnabled) {
                                                            handleRedisConnectionFailure(redisHost, redisPort);
                                                        }
                                                    }
                                                })
                                                .doOnError(error -> {
                                                    LoggingUtil.error(logger, "Redis连接健康检查异常 - {}:{}, 错误: {}",
                                                            redisHost, redisPort, error.getMessage());
                                                    if (!gracefulDegradationEnabled) {
                                                        handleRedisConnectionFailure(redisHost, redisPort);
                                                    }
                                                })
                                                .onErrorReturn(false);
                                    });
                        }));
        });
    }

    /**
     * 等待Redis服务就绪
     *
     * <p>
     * <strong>等待策略：</strong>
     * <ul>
     * <li>TCP连接测试</li>
     * <li>固定延迟重试</li>
     * <li>支持优雅降级模式</li>
     * </ul>
     *
     * @return 等待结果的Mono
     */
    public Mono<Boolean> waitForRedisReady() {
        // 配置标准化说明：使用统一配置管理器获取Redis连接信息，符合最佳实践
        // 优先级：配置文件 spring.data.redis.host > 默认值 localhost
        // 使用响应式方式获取配置，避免阻塞调用
        return Mono.zip(
                unifiedConfigManager.getStringConfig("spring.data.redis.host", 
                        unifiedConfigManager.getProperty("HONYRUN_REDIS_HOST", "localhost")),
                unifiedConfigManager.getIntegerConfig("spring.data.redis.port", 
                        Integer.parseInt(unifiedConfigManager.getProperty("honyrun.redis.port", "8902")))
        ).flatMap(tuple -> {
            String redisHost = tuple.getT1();
            int redisPort = tuple.getT2();

        LoggingUtil.info(logger, "等待Redis服务就绪 - {}:{}", redisHost, redisPort);

        return Mono.fromCallable(() -> testTcpConnection(redisHost, redisPort))
                .retryWhen(Retry.fixedDelay(6, Duration.ofSeconds(3))
                        .doBeforeRetry(retrySignal -> LoggingUtil.info(logger, "Redis服务未就绪，等待中... 第{}次检查",
                                retrySignal.totalRetries() + 1)))
                .timeout(Duration.ofSeconds(20))
                .switchIfEmpty(Mono.fromCallable(() -> {
                    LoggingUtil.warn(logger, "Redis服务等待超时 - {}:{}, 启用优雅降级模式", redisHost, redisPort);
                    return false;
                }))
                .doOnSuccess(success -> {
                    if (success) {
                        LoggingUtil.info(logger, "Redis服务已就绪 - {}:{}", redisHost, redisPort);
                    } else {
                        LoggingUtil.warn(logger, "Redis服务未就绪，将使用内存缓存 - {}:{}", redisHost, redisPort);
                    }
                })
                .doOnError(error -> LoggingUtil.error(logger, "等待Redis服务异常 - {}:{}, 错误: {}",
                        redisHost, redisPort, error.getMessage()))
                .onErrorReturn(false);
        });
    }

    /**
     * 处理Redis连接失败
     *
     * <p>
     * <strong>处理策略：</strong>
     * <ul>
     * <li>默认情况下（降级未启用）：立即停止应用启动</li>
     * <li>降级启用时：记录警告日志，允许继续启动</li>
     * </ul>
     */
    private void handleRedisConnectionFailure(String redisHost, int redisPort) {
        Mono.zip(
                unifiedConfigManager.getBooleanConfig("honyrun.redis.graceful-degradation.enabled", true),
                unifiedConfigManager.getStringConfig("honyrun.redis.failure-strategy", "FAIL_FAST"))
                .subscribe(tuple -> {
                    boolean gracefulDegradationEnabled = tuple.getT1();
                    String failureStrategy = tuple.getT2();

                    if (!gracefulDegradationEnabled) {
                        // 默认行为：快速失败
                        String errorMessage = String.format("Redis连接失败 - %s:%d，应用启动终止", redisHost, redisPort);
                        LoggingUtil.error(logger, errorMessage);
                        throw new IllegalStateException(errorMessage);
                    } else {
                        // 降级模式：记录警告并继续
                        LoggingUtil.warn(logger, "Redis连接失败 - {}:{}, 启用降级模式，使用内存缓存", redisHost, redisPort);
                        LoggingUtil.info(logger, "故障处理策略: {}", failureStrategy);
                    }
                }, error -> {
                    LoggingUtil.error(logger, "获取Redis故障处理配置失败: {}", error.getMessage(), error);
                    // 配置获取失败时，默认快速失败
                    String errorMessage = String.format("Redis连接失败且配置获取异常 - %s:%d，应用启动终止", redisHost, redisPort);
                    throw new IllegalStateException(errorMessage);
                });
    }

    /**
     * 测试TCP连接
     *
     * @param redisHost Redis主机地址
     * @param redisPort Redis端口号
     * @return 连接是否成功
     */
    private boolean testTcpConnection(String redisHost, int redisPort) {
        try (Socket socket = new Socket()) {
            // 使用默认超时值，避免阻塞调用
            int tcpConnectionTimeout = Integer.parseInt(unifiedConfigManager.getProperty("honyrun.redis.tcp-connection-timeout", "3000"));
            socket.connect(new InetSocketAddress(redisHost, redisPort), tcpConnectionTimeout);
            return socket.isConnected();
        } catch (IOException e) {
            LoggingUtil.debug(logger, "TCP连接测试失败 - {}:{}, 错误: {}",
                    redisHost, redisPort, e.getMessage());
            return false;
        }
    }

    /**
     * 获取Redis连接信息
     *
     * @return Redis连接信息字符串的Mono
     */
    public Mono<String> getRedisConnectionInfo() {
        // 配置标准化说明：使用统一配置管理器获取Redis连接信息，符合最佳实践
        // 优先级：配置文件 spring.data.redis.host > 默认值 localhost，端口默认 8902
        return Mono.zip(
                unifiedConfigManager.getStringConfig("spring.data.redis.host", 
                        unifiedConfigManager.getProperty("HONYRUN_REDIS_HOST", "localhost")),
                unifiedConfigManager.getIntegerConfig("spring.data.redis.port", 8902))
                .map(tuple -> String.format("%s:%d", tuple.getT1(), tuple.getT2()));
    }

    /**
     * 检查是否启用了降级模式
     *
     * @return 降级模式是否启用的Mono
     */
    public Mono<Boolean> isGracefulDegradationEnabled() {
        return unifiedConfigManager.getBooleanConfig("honyrun.redis.graceful-degradation.enabled", true);
    }

    /**
     * 获取故障处理策略
     *
     * @return 故障处理策略的Mono
     */
    public Mono<String> getFailureStrategy() {
        return unifiedConfigManager.getStringConfig("honyrun.redis.failure-strategy", "FAIL_FAST");
    }
}
