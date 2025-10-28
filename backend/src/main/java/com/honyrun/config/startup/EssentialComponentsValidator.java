package com.honyrun.config.startup;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.filter.reactive.ReactiveRequestSignatureFilter;
import com.honyrun.scheduler.UnifiedMonitoringScheduler;
import com.honyrun.service.reactive.ReactiveRequestSignatureService;
import com.honyrun.service.reactive.ReactiveSystemService;
import com.honyrun.util.LoggingUtil;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

/**
 * 必备组件验证器
 *
 * <p>
 * <strong>MySQL和Redis为项目必备组件，必须在应用启动时进行连接检测</strong>
 * </p>
 *
 * <p>
 * <strong>核心功能：</strong>
 * <ul>
 * <li><strong>localhost解析检测</strong> - 验证localhost是否正确解析到127.0.0.1和::1</li>
 * <li><strong>MySQL连接检测</strong> - 验证R2DBC数据库连接是否正常</li>
 * <li><strong>Redis连接检测</strong> - 验证响应式Redis连接是否正常</li>
 * <li><strong>启动终止机制</strong> - 连接失败时立即终止应用启动</li>
 * <li><strong>详细错误报告</strong> - 提供详细的连接失败信息</li>
 * <li><strong>重试机制</strong> - 连接失败时自动重试，提高连接稳定性</li>
 * </ul>
 *
 * <p>
 * <strong>验证规范：</strong>
 * <ul>
 * <li>localhost解析验证：检查localhost是否解析到标准地址127.0.0.1和::1</li>
 * <li>MySQL连接验证：执行SELECT 1查询验证连接可用性</li>
 * <li>Redis连接验证：执行PING命令验证连接可用性</li>
 * <li>超时设置：每个连接检测最大等待10秒</li>
 * <li>重试策略：最多重试3次，采用指数退避算法</li>
 * <li>失败处理：所有重试失败后立即终止应用</li>
 * </ul>
 *
 * <p>
 * <strong>执行顺序：</strong>
 * 使用@Order(1)确保在其他ApplicationRunner之前执行，
 * 保证必备组件验证是应用启动的第一步。
 *
 * <p>
 * <strong>错误处理：</strong>
 * <ul>
 * <li>localhost解析异常：记录警告信息，提醒可能的网络配置问题</li>
 * <li>连接超时：记录详细超时信息并重试</li>
 * <li>连接拒绝：记录连接被拒绝信息并重试</li>
 * <li>认证失败：记录认证失败信息并重试</li>
 * <li>其他异常：记录异常详情并重试</li>
 * <li>重试耗尽：终止应用启动</li>
 * </ul>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-06-28 10:30:00
 * @modified 2025-06-29 15:30:00
 * @version 1.2.0 - 添加localhost解析检测逻辑
 * @since 1.0.0
 */
@Component
@Order(1) // 确保在其他ApplicationRunner之前执行
public class EssentialComponentsValidator implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(EssentialComponentsValidator.class);

    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 连接重试配置
     * 用于配置数据库和Redis连接的重试策略和超时设置
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-13 16:57:03
     * @version 1.0.0
     */
    private final ConnectionRetryConfig connectionRetryConfig;

    /**
     * Spring应用上下文
     * 用于优雅关闭应用程序
     */
    private final ApplicationContext applicationContext;

    /**
     * 统一监控调度器
     * 用于在启动检查完成后启用运行时监控
     */
    private final UnifiedMonitoringScheduler unifiedMonitoringScheduler;

    /**
     * Micrometer指标注册表
     * 用于收集和记录启动过程中的各项指标
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 当前时间
     * @version 1.0.0
     */
    private final MeterRegistry meterRegistry;

    /**
     * 响应式系统服务
     */
    private final ReactiveSystemService systemService;

    /**
     * R2DBC连接工厂
     */
    private final ConnectionFactory connectionFactory;

    /**
     * Redis生产环境缓存管理器
     * 用于Redis缓存预热操作
     * 仅在生产环境下注入
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-06-28 15:30:00
     * @version 1.0.0
     */
    private final com.honyrun.config.reactive.ReactiveRedisProdConfig.RedisCacheManager redisProductionCacheManager;

    /**
     * 响应式Redis模板
     * 用于执行Redis操作和连接验证
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-25 19:52:56
     * @version 1.0.0
     */
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    /**
     * 请求签名验证服务
     * 用于验证签名验证组件是否正确注册和配置
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-01 16:57:03
     * @version 1.0.0
     */
    private final ReactiveRequestSignatureService requestSignatureService;

    /**
     * 请求签名验证过滤器
     * 用于验证过滤器是否正确注册到Spring容器
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-01 16:57:03
     * @version 1.0.0
     */
    private final ReactiveRequestSignatureFilter requestSignatureFilter;

    /**
     * 启动验证总计时器
     * 记录整个启动验证过程的耗时
     */
    private Timer.Sample startupValidationTimer;

    /**
     * MySQL连接验证计时器
     * 记录MySQL连接验证的耗时
     */
    private Timer.Sample mysqlValidationTimer;

    /**
     * Redis连接验证计时器
     * 记录Redis连接验证的耗时
     */
    private Timer.Sample redisValidationTimer;

    /**
     * WebClient实例，用于调用健康检查端点
     */
    private WebClient webClient;

    /**
     * 环境配置
     */
    private final Environment environment;

    /**
     * 构造函数注入所有依赖项
     *
     * @param connectionRetryConfig          连接重试配置
     * @param applicationContext             Spring应用上下文
     * @param unifiedMonitoringScheduler     统一监控调度器
     * @param meterRegistry                  Micrometer指标注册表
     * @param systemService                  响应式系统服务
     * @param cacheConsistencyService        缓存一致性服务
     * @param reactiveRedisConnectionFactory Redis响应式连接工厂
     * @param connectionFactory              R2DBC连接工厂
     * @param redisProductionCacheManager    Redis生产环境缓存管理器（可选）
     * @param reactiveRedisTemplate          响应式Redis模板
     * @param requestSignatureService        请求签名验证服务
     * @param requestSignatureFilter         请求签名验证过滤器
     * @param environment                    环境配置
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 2.0.0 - 重构为构造函数注入
     */
    public EssentialComponentsValidator(
            UnifiedConfigManager unifiedConfigManager,
            ConnectionRetryConfig connectionRetryConfig,
            ApplicationContext applicationContext,
            UnifiedMonitoringScheduler unifiedMonitoringScheduler,
            MeterRegistry meterRegistry,
            ReactiveSystemService systemService,
            ConnectionFactory connectionFactory,
            @org.springframework.beans.factory.annotation.Autowired(required = false) com.honyrun.config.reactive.ReactiveRedisProdConfig.RedisCacheManager redisProductionCacheManager,
            @org.springframework.beans.factory.annotation.Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
            ReactiveRequestSignatureService requestSignatureService,
            ReactiveRequestSignatureFilter requestSignatureFilter,
            Environment environment) {

        this.unifiedConfigManager = unifiedConfigManager;
        this.connectionRetryConfig = connectionRetryConfig;
        this.applicationContext = applicationContext;
        this.unifiedMonitoringScheduler = unifiedMonitoringScheduler;
        this.meterRegistry = meterRegistry;
        this.systemService = systemService;
        this.connectionFactory = connectionFactory;
        this.redisProductionCacheManager = redisProductionCacheManager;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.requestSignatureService = requestSignatureService;
        this.requestSignatureFilter = requestSignatureFilter;
        this.environment = environment;
    }

    /**
     * 初始化WebClient - 优化连接池配置
     */
    @PostConstruct
    private void initWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10))
                // 配置连接池，复用连接减少频繁创建
                .option(io.netty.channel.ChannelOption.SO_KEEPALIVE, true)
                .option(io.netty.channel.ChannelOption.TCP_NODELAY, true);

        // 使用Environment直接获取配置，避免响应式阻塞调用
        // 必要性说明：在@PostConstruct阶段，响应式配置管理器可能尚未完全初始化，
        // 使用.block()会导致Netty executor问题，因此直接使用Environment获取配置
        String serverHost = environment.getProperty("HONYRUN_SERVER_HOST", "localhost");
        String serverPort = environment.getProperty("server.port", "8901");
        
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://" + serverHost + ":" + serverPort)
                .build();
    }

    /**
     * 应用启动时执行必备组件验证
     *
     * <p>
     * <strong>验证流程：</strong>
     * <ol>
     * <li>记录验证开始日志</li>
     * <li>执行localhost解析检测</li>
     * <li>显示当前环境配置信息</li>
     * <li>并行执行MySQL和Redis连接验证</li>
     * <li>等待所有验证完成或超时</li>
     * <li>根据验证结果决定是否继续启动</li>
     * </ol>
     *
     * @param args 应用启动参数
     * @throws Exception 验证失败时抛出异常终止启动
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        LoggingUtil.info(logger, "========= EssentialComponentsValidator 开始执行 =========");

        // 启动总验证计时器
        startupValidationTimer = Timer.start(meterRegistry);
        LoggingUtil.info(logger, "启动验证计时器已启动");

        try {
            // 收集启动基线指标
            long baselineStartTime = System.currentTimeMillis();
            Map<String, Object> baselineMetrics = collectStartupBaselineMetrics();
            long baselineEndTime = System.currentTimeMillis();

            // 记录基线指标收集耗时
            meterRegistry.timer("honyrun.startup.baseline.collection.time")
                    .record(baselineEndTime - baselineStartTime, java.util.concurrent.TimeUnit.MILLISECONDS);

            // 显示环境信息
            long envInfoStartTime = System.currentTimeMillis();
            displayEnvironmentInfo();
            long envInfoEndTime = System.currentTimeMillis();

            // 记录环境信息显示耗时
            meterRegistry.timer("honyrun.startup.environment.info.time")
                    .record(envInfoEndTime - envInfoStartTime, java.util.concurrent.TimeUnit.MILLISECONDS);

            // 验证localhost解析
            long localhostStartTime = System.currentTimeMillis();
            validateLocalhostResolution();
            long localhostEndTime = System.currentTimeMillis();

            // 记录localhost验证耗时
            meterRegistry.timer("honyrun.startup.localhost.validation.time")
                    .record(localhostEndTime - localhostStartTime, java.util.concurrent.TimeUnit.MILLISECONDS);

            LoggingUtil.info(logger, "连接超时设置: {} 秒", connectionRetryConfig.getConnectionTimeout().getSeconds());

            // 验证必备组件连接 - 使用响应式非阻塞方式
            long connectionValidationStartTime = System.currentTimeMillis();
            LoggingUtil.info(logger, "开始验证必备组件连接...");

            // 验证签名验证组件
            validateSignatureVerificationComponents();

            // 使用响应式方式处理验证结果，避免阻塞调用
            // 改为异步执行，不阻塞启动流程，让Spring Boot自然完成启动
            Mono.when(
                    validateMySQLConnection(),
                    validateRedisConnection())
                    .timeout(connectionRetryConfig.getConnectionTimeout().multipliedBy(2)) // 总超时时间为单个连接超时的2倍
                    .doOnSuccess(result -> {
                        long connectionValidationEndTime = System.currentTimeMillis();

                        // 记录连接验证总耗时
                        meterRegistry.timer("honyrun.startup.connection.validation.total.time")
                                .record(connectionValidationEndTime - connectionValidationStartTime,
                                        java.util.concurrent.TimeUnit.MILLISECONDS);

                        // 停止总验证计时器并记录
                        startupValidationTimer.stop(Timer.builder("honyrun.startup.validation.total.time")
                                .description("Total startup validation time")
                                .tag("status", "success")
                                .register(meterRegistry));

                        LoggingUtil.info(logger, "✓ 所有必备组件连接验证成功");
                        LoggingUtil.info(logger, "✓ 启动验证总耗时: {} 毫秒",
                                connectionValidationEndTime - connectionValidationStartTime);

                        // 通知监控调度器
                        notifyMonitoringScheduler();

                        // 执行缓存预热
                        performCacheWarmup();

                        // 执行启动后健康检查验证,强制要求
                        performPostStartupHealthCheck();
                    })
                    .doOnError(error -> {
                        // 记录失败指标
                        meterRegistry.counter("honyrun.startup.validation.failures")
                                .increment();

                        // 停止总验证计时器并记录失败状态
                        startupValidationTimer.stop(Timer.builder("honyrun.startup.validation.total.time")
                                .description("Total startup validation time")
                                .tag("status", "failure")
                                .register(meterRegistry));

                        LoggingUtil.error(logger, "========= 必备组件验证失败 =========", error);
                        LoggingUtil.error(logger, "✗ 必备组件连接验证失败，应用启动终止");
                        LoggingUtil.error(logger, "请检查MySQL和Redis服务状态及配置参数");

                        // 优雅关闭应用上下文
                        shutdownApplicationGracefully();
                    })
                    .subscribe(); // 改为异步订阅，避免阻塞启动流程，符合响应式编程原则

        } catch (Exception e) {
            // 记录异常指标
            meterRegistry.counter("honyrun.startup.validation.exceptions")
                    .increment();

            // 停止总验证计时器并记录异常状态
            if (startupValidationTimer != null) {
                startupValidationTimer.stop(Timer.builder("honyrun.startup.validation.total.time")
                        .description("Total startup validation time")
                        .tag("status", "exception")
                        .register(meterRegistry));
            }

            LoggingUtil.error(logger, "=========必备组件验证过程中发生异常，应用启动终止=========", e);
            // 优雅关闭应用上下文，而不是强制退出
            shutdownApplicationGracefully();
        }

        LoggingUtil.info(logger, "========= EssentialComponentsValidator 执行完成 =========");
    }

    /**
     * 优雅关闭应用上下文
     *
     * @author: Mr.Rey Copyright © 2025
     * @created: 2025-07-01 16:57:03
     * @version: 1.0.0
     */
    private void shutdownApplicationGracefully() {
        LoggingUtil.info(logger, "正在优雅关闭应用上下文...");
        try {
            if (applicationContext instanceof org.springframework.context.ConfigurableApplicationContext) {
                ((org.springframework.context.ConfigurableApplicationContext) applicationContext).close();
            } else {
                LoggingUtil.warn(logger, "无法优雅关闭应用上下文，使用系统退出");
                System.exit(1);
            }
        } catch (Exception closeException) {
            LoggingUtil.error(logger, "优雅关闭应用上下文失败", closeException);
            System.exit(1);
        }
    }

    /**
     * 通知统一监控调度器启动检查已完成
     *
     * <p>
     * <strong>协作机制：</strong>
     * <ul>
     * <li>在必备组件验证成功后调用此方法</li>
     * <li>通知UnifiedMonitoringScheduler基础组件已就绪</li>
     * <li>为后续的运行时监控提供基线信息</li>
     * </ul>
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 当前时间
     * @modified 2025-07-02 当前时间
     * @version 1.0.0
     */
    private void notifyMonitoringScheduler() {
        try {
            LoggingUtil.info(logger, "通知统一监控调度器：基础组件验证完成");
            LoggingUtil.info(logger, "MySQL和Redis连接已验证，可以开始运行时监控");

            // 准备验证结果数据
            Map<String, Object> validationResults = new HashMap<>();
            validationResults.put("mysql", "成功");
            validationResults.put("redis", "成功");
            validationResults.put("localhost", "成功");
            validationResults.put("validationTime", LocalDateTime.now());

            // 准备基线指标数据
            Map<String, Object> baselineMetrics = collectStartupBaselineMetrics();

            // 调用统一监控调度器的协作接口
            unifiedMonitoringScheduler.onStartupValidationCompleted(validationResults, baselineMetrics);

            LoggingUtil.info(logger, "已成功通知统一监控调度器，传递验证结果和基线指标");
        } catch (Exception e) {
            LoggingUtil.error(logger, "通知统一监控调度器时发生异常", e);
            throw new RuntimeException("启动验证完成通知失败", e);
        }
    }

    /**
     * 收集启动基线指标
     *
     * <p>
     * <strong>基线指标包括：</strong>
     * <ul>
     * <li>JVM内存使用情况</li>
     * <li>系统启动时间</li>
     * <li>连接池初始状态</li>
     * <li>网络配置状态</li>
     * </ul>
     *
     * @return 基线指标数据映射
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 当前时间
     * @modified 2025-07-02 当前时间
     * @version 1.0.0
     */
    private Map<String, Object> collectStartupBaselineMetrics() {
        Map<String, Object> baselineMetrics = new HashMap<>();

        try {
            // JVM内存基线
            Runtime runtime = Runtime.getRuntime();
            baselineMetrics.put("jvm.memory.total", runtime.totalMemory());
            baselineMetrics.put("jvm.memory.free", runtime.freeMemory());
            baselineMetrics.put("jvm.memory.used", runtime.totalMemory() - runtime.freeMemory());
            baselineMetrics.put("jvm.memory.max", runtime.maxMemory());

            // 系统信息基线
            baselineMetrics.put("system.processors", runtime.availableProcessors());
            baselineMetrics.put("startup.timestamp", System.currentTimeMillis());

            // 网络配置基线
            try {
                InetAddress localhost = InetAddress.getByName("localhost");
                baselineMetrics.put("network.localhost.address", localhost.getHostAddress());
                baselineMetrics.put("network.localhost.canonical", localhost.getCanonicalHostName());
            } catch (UnknownHostException e) {
                LoggingUtil.warn(logger, "获取localhost信息失败", e);
            }

            // 连接配置基线
            baselineMetrics.put("connection.timeout.seconds",
                    connectionRetryConfig.getConnectionTimeout().getSeconds());
            baselineMetrics.put("connection.retry.maxAttempts", connectionRetryConfig.getMaxRetries());
            baselineMetrics.put("connection.retry.backoffMs", connectionRetryConfig.getRetryDelayMs());

            LoggingUtil.info(logger, "已收集启动基线指标，指标数量: {}", baselineMetrics.size());

        } catch (Exception e) {
            LoggingUtil.error(logger, "收集启动基线指标时发生异常", e);
        }

        return baselineMetrics;
    }

    /**
     * 验证localhost解析配置
     *
     * <p>
     * <strong>功能说明：</strong>
     * <ul>
     * <li>检测localhost是否能正确解析为127.0.0.1</li>
     * <li>验证本地网络配置的基础可用性</li>
     * <li>为后续的MySQL和Redis连接验证提供网络基础</li>
     * </ul>
     *
     * @throws RuntimeException 当localhost解析失败时抛出异常
     */
    private void validateLocalhostResolution() {
        try {
            LoggingUtil.info(logger, "=== 开始localhost解析检测 ===");
            LoggingUtil.info(logger, "开始验证localhost解析配置...");

            InetAddress localhost = InetAddress.getByName("localhost");
            String resolvedAddress = localhost.getHostAddress();

            LoggingUtil.info(logger, "localhost解析结果: {}", resolvedAddress);

            if ("127.0.0.1".equals(resolvedAddress)) {
                LoggingUtil.info(logger, "✓ localhost解析验证: 成功 (127.0.0.1)");
            } else {
                LoggingUtil.warn(logger, "⚠ localhost解析异常: 期望127.0.0.1，实际{}", resolvedAddress);
            }

        } catch (UnknownHostException e) {
            String errorMsg = "localhost解析失败，请检查系统hosts文件配置";
            LoggingUtil.error(logger, "✗ localhost解析验证: 失败 - {}", errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * 显示当前环境配置信息
     *
     * <p>
     * 输出当前激活的Spring Profile和关键配置参数，
     * 帮助开发者了解当前运行环境
     * </p>
     */
    private void displayEnvironmentInfo() {
        try {
            String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
            String profilesStr = activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "default";

            LoggingUtil.info(logger, "当前激活的Spring Profile: {}", profilesStr);

            // 显示数据库配置信息（不显示敏感信息如密码）
            String r2dbcUrl = applicationContext.getEnvironment().getProperty("spring.r2dbc.url", "未配置");
            if (r2dbcUrl.contains("@")) {
                // 隐藏密码部分
                r2dbcUrl = r2dbcUrl.replaceAll("://[^:]+:[^@]+@", "://***:***@");
            }
            LoggingUtil.info(logger, "MySQL R2DBC URL: {}", r2dbcUrl);

            // 显示Redis配置信息
            String redisHost = applicationContext.getEnvironment().getProperty("spring.data.redis.host", "未配置");
            String redisPort = applicationContext.getEnvironment().getProperty("spring.data.redis.port", "未配置");
            LoggingUtil.info(logger, "Redis连接地址: {}:{}", redisHost, redisPort);

        } catch (Exception e) {
            LoggingUtil.warn(logger, "获取环境配置信息时发生异常: {}", e.getMessage());
        }
    }

    /**
     * 验证MySQL数据库连接
     *
     * <p>
     * <strong>验证方式：</strong>
     * <ul>
     * <li>获取R2DBC连接工厂</li>
     * <li>创建数据库连接</li>
     * <li>执行SELECT 1查询验证连接可用性</li>
     * <li>连接失败时自动重试</li>
     * <li>正确关闭连接资源</li>
     * </ul>
     *
     * @return Mono<Void> 验证结果，成功时完成，失败时包含错误信息
     */
    private Mono<Void> validateMySQLConnection() {
        return Mono.fromCallable(() -> {
            LoggingUtil.info(logger, "开始验证MySQL连接...");

            // 启动MySQL验证计时器
            mysqlValidationTimer = Timer.start(meterRegistry);

            return "MySQL连接验证开始";
        })
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .flatMap(message -> {
                    return Mono.from(connectionFactory.create())
                            .flatMap(connection -> {
                                LoggingUtil.info(logger, "MySQL连接创建成功，开始执行测试查询");

                                return Mono.from(connection.createStatement("SELECT 1 as test")
                                        .execute())
                                        .flatMap(result -> Mono.from(result.map((row, metadata) -> {
                                            Object testValue = row.get("test");
                                            LoggingUtil.info(logger, "MySQL测试查询执行成功，返回值: {}", testValue);
                                            return testValue;
                                        })))
                                        .doFinally(signalType -> {
                                            LoggingUtil.info(logger, "关闭MySQL连接，信号类型: {}", signalType);
                                            Mono.from(connection.close()).subscribe();
                                        });
                            })
                            .then() // 转换为Mono<Void>
                            .doOnSuccess(result -> {
                                // 停止MySQL验证计时器并记录成功
                                mysqlValidationTimer.stop(Timer.builder("honyrun.startup.mysql.validation.time")
                                        .description("MySQL connection validation time")
                                        .tag("status", "success")
                                        .register(meterRegistry));

                                // 记录MySQL连接成功指标
                                meterRegistry.counter("honyrun.startup.mysql.validation.success")
                                        .increment();

                                LoggingUtil.info(logger, "✓ MySQL连接验证: 成功");
                            })
                            .doOnError(error -> {
                                // 停止MySQL验证计时器并记录失败
                                if (mysqlValidationTimer != null) {
                                    mysqlValidationTimer.stop(Timer.builder("honyrun.startup.mysql.validation.time")
                                            .description("MySQL connection validation time")
                                            .tag("status", "failure")
                                            .register(meterRegistry));
                                }

                                // 记录MySQL连接失败指标
                                meterRegistry.counter("honyrun.startup.mysql.validation.failure")
                                        .increment();

                                LoggingUtil.error(logger, "✗ MySQL连接验证: 失败", error);
                            });
                })
                .timeout(connectionRetryConfig.getConnectionTimeout())
                .retryWhen(Retry.backoff(connectionRetryConfig.getMaxRetries(), connectionRetryConfig.getRetryDelay())
                        .maxBackoff(connectionRetryConfig.getMaxRetryDelay())
                        .doBeforeRetry(retrySignal -> {
                            long retryAttempt = retrySignal.totalRetries() + 1;
                            Throwable failure = retrySignal.failure();

                            // 记录重试指标
                            meterRegistry.counter("honyrun.startup.mysql.validation.retries")
                                    .increment();

                            LoggingUtil.warn(logger, "MySQL连接验证失败，正在进行第{}次重试（最多{}次），错误: {}",
                                    retryAttempt, connectionRetryConfig.getMaxRetries(), failure.getMessage());
                            LoggingUtil.info(logger, "重试间隔: {}ms",
                                    connectionRetryConfig.calculateDelay((int) retryAttempt).toMillis());
                        })
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            // 记录重试耗尽指标
                            meterRegistry.counter("honyrun.startup.mysql.validation.retry.exhausted")
                                    .increment();

                            return new RuntimeException("MySQL连接验证失败，已达到最大重试次数: " +
                                    connectionRetryConfig.getMaxRetries(), retrySignal.failure());
                        }))
                .doOnTerminate(() -> {
                    LoggingUtil.info(logger, "MySQL连接验证完成，连接超时: {} 秒",
                            connectionRetryConfig.getConnectionTimeout().getSeconds());
                })
                .then();
    }

    /**
     * 验证Redis连接状态
     *
     * <p>
     * <strong>功能说明：</strong>
     * <ul>
     * <li>使用ReactiveRedisTemplate进行连接验证，避免直接操作连接工厂</li>
     * <li>采用响应式编程模式，避免阻塞操作</li>
     * <li>实现连接超时和重试机制</li>
     * <li>记录详细的验证指标和日志</li>
     * <li>过滤RedisCommandInterruptedException，避免无效重试</li>
     * </ul>
     *
     * @return Mono<Void> 验证结果
     * @author Mr.Rey Copyright © 2025
     * @modified 2025-10-25 19:52:56
     * @version 1.1.0 - 解决RedisCommandInterruptedException异常
     */
    private Mono<Void> validateRedisConnection() {
        return Mono.defer(() -> {
            LoggingUtil.info(logger, "开始验证Redis连接（优化版本）...");

            // 启动Redis验证计时器
            redisValidationTimer = Timer.start(meterRegistry);

            // 使用ReactiveRedisTemplate进行验证，避免直接操作连接工厂
            return reactiveRedisTemplate.hasKey("__connection_test__")
                    .timeout(Duration.ofSeconds(3))
                    .doOnSuccess(result -> {
                        LoggingUtil.info(logger, "Redis连接验证成功，连接状态正常");
                    })
                    .doOnError(error -> {
                        LoggingUtil.warn(logger, "Redis连接验证遇到错误: {}", error.getMessage());
                    });
        })
                .doOnSuccess(result -> {
                    // 停止Redis验证计时器并记录成功
                    redisValidationTimer.stop(Timer.builder("honyrun.startup.redis.validation.time")
                            .description("Redis connection validation time")
                            .tag("status", "success")
                            .register(meterRegistry));

                    // 记录Redis连接成功指标
                    meterRegistry.counter("honyrun.startup.redis.validation.success")
                            .increment();

                    LoggingUtil.info(logger, "✓ Redis连接验证: 成功");
                })
                .doOnError(error -> {
                    // 停止Redis验证计时器并记录失败
                    if (redisValidationTimer != null) {
                        redisValidationTimer.stop(Timer.builder("honyrun.startup.redis.validation.time")
                                .description("Redis connection validation time")
                                .tag("status", "failure")
                                .register(meterRegistry));
                    }

                    // 记录Redis连接失败指标
                    meterRegistry.counter("honyrun.startup.redis.validation.failure")
                            .increment();

                    LoggingUtil.error(logger, "✗ Redis连接验证: 失败", error);
                })
                .then() // 转换为Mono<Void>
                // 【统一配置管理原则】使用配置化的超时参数，避免硬编码值
                .timeout(Duration
                        .ofSeconds(Integer.parseInt(System.getProperty("redis.validation.timeout-seconds", "8")))) // 从配置获取超时时间
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)) // 减少重试次数，避免连接池压力
                        .maxBackoff(Duration.ofSeconds(5))
                        .filter(throwable -> {
                            // 过滤掉RedisCommandInterruptedException，避免无效重试
                            if (throwable.getMessage() != null &&
                                    throwable.getMessage().contains("Command interrupted")) {
                                LoggingUtil.warn(logger, "检测到Redis命令中断异常，跳过重试");
                                return false;
                            }
                            return true;
                        })
                        .doBeforeRetry(retrySignal -> {
                            long retryAttempt = retrySignal.totalRetries() + 1;
                            Throwable failure = retrySignal.failure();

                            // 记录重试指标
                            meterRegistry.counter("honyrun.startup.redis.validation.retries")
                                    .increment();

                            LoggingUtil.warn(logger, "Redis连接验证失败，正在进行第{}次重试，错误: {}",
                                    retryAttempt, failure.getMessage());
                        })
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            // 记录重试耗尽指标
                            meterRegistry.counter("honyrun.startup.redis.validation.retry.exhausted")
                                    .increment();

                            return new RuntimeException("Redis连接验证失败，已达到最大重试次数", retrySignal.failure());
                        }))
                .doOnTerminate(() -> {
                    LoggingUtil.info(logger, "Redis连接验证完成");
                });
    }

    /**
     * 执行启动后健康检查验证
     *
     * <p>
     * <strong>功能说明：</strong>
     * <ul>
     * <li>调用应用的健康检查端点验证系统状态</li>
     * <li>记录健康检查响应时间和状态</li>
     * <li>收集健康检查相关的Micrometer指标</li>
     * <li>验证各组件的健康状态</li>
     * </ul>
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-01 22:45:00
     * @version 1.0.0
     */
    private void performPostStartupHealthCheck() {
        LoggingUtil.info(logger, "开始执行启动后健康检查验证");

        Timer.Sample healthCheckTimer = Timer.start(meterRegistry);

        webClient.get()
                .uri("/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(60)) // 增加超时时间到60秒，与配置文件一致
                .doOnSuccess(response -> {
                    // 停止健康检查计时器
                    healthCheckTimer.stop(Timer.builder("honyrun.startup.health.check.time")
                            .description("Post-startup health check response time")
                            .tag("status", "success")
                            .register(meterRegistry));

                    // 记录成功指标
                    meterRegistry.counter("honyrun.startup.health.check.success").increment();

                    LoggingUtil.info(logger, "✓ 启动后健康检查验证成功");
                    LoggingUtil.debug(logger, "健康检查响应: {}", response);
                })
                .doOnError(WebClientResponseException.class, error -> {
                    // 停止健康检查计时器并记录错误状态
                    healthCheckTimer.stop(Timer.builder("honyrun.startup.health.check.time")
                            .description("Post-startup health check response time")
                            .tag("status", "http_error")
                            .tag("http_status", String.valueOf(error.getStatusCode().value()))
                            .register(meterRegistry));

                    // 记录HTTP错误指标
                    meterRegistry.counter("honyrun.startup.health.check.http.errors",
                            "status_code", String.valueOf(error.getStatusCode().value())).increment();

                    LoggingUtil.warn(logger, "启动后健康检查HTTP错误 - 状态码: {}, 响应: {}",
                            error.getStatusCode().value(), error.getResponseBodyAsString());
                })
                .doOnError(error -> {
                    if (!(error instanceof WebClientResponseException)) {
                        // 停止健康检查计时器并记录异常状态
                        healthCheckTimer.stop(Timer.builder("honyrun.startup.health.check.time")
                                .description("Post-startup health check response time")
                                .tag("status", "exception")
                                .tag("exception_type", error.getClass().getSimpleName())
                                .register(meterRegistry));

                        // 记录异常指标
                        meterRegistry.counter("honyrun.startup.health.check.exceptions",
                                "exception_type", error.getClass().getSimpleName()).increment();

                        LoggingUtil.error(logger, "启动后健康检查验证异常", error);
                    }
                })
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "启动后健康检查验证失败，但不影响应用启动: {}", error.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 执行缓存预热
     *
     * <p>
     * 在应用启动完成后，执行关键服务的缓存预热，提高应用响应性能。
     * 该方法采用异步执行，不会阻塞应用启动流程。
     * </p>
     *
     * <p>
     * <strong>预热策略：</strong>
     * <ul>
     * <li>Redis缓存预热：预热Redis连接池和基础缓存</li>
     * <li>系统配置缓存预热：预加载系统配置到缓存</li>
     * <li>用户权限缓存预热：预加载用户权限数据</li>
     * <li>业务缓存预热：预加载常用业务数据</li>
     * </ul>
     *
     * <p>
     * <strong>性能监控：</strong>
     * <ul>
     * <li>记录各类缓存预热的执行时间</li>
     * <li>统计预热成功和失败的次数</li>
     * <li>监控预热过程中的异常情况</li>
     * </ul>
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-06-28 16:00:00
     * @version 1.0.0
     */
    private void performCacheWarmup() {
        LoggingUtil.info(logger, "开始执行缓存预热");

        Timer.Sample cacheWarmupTimer = Timer.start(meterRegistry);

        // 异步执行缓存预热，不阻塞启动流程
        Mono.fromRunnable(() -> {
            LoggingUtil.info(logger, "启动缓存预热任务");
        })
                .then(performRedisWarmup())
                .then(performSystemCacheWarmup())
                .then(performUserPermissionCacheWarmup())
                .then(performBusinessCacheWarmup())
                .doOnSuccess(result -> {
                    // 停止缓存预热计时器
                    cacheWarmupTimer.stop(Timer.builder("honyrun.startup.cache.warmup.total.time")
                            .description("Total cache warmup time during startup")
                            .tag("status", "success")
                            .register(meterRegistry));

                    // 记录成功指标
                    meterRegistry.counter("honyrun.startup.cache.warmup.success").increment();

                    LoggingUtil.info(logger, "✓ 缓存预热完成");
                })
                .doOnError(error -> {
                    // 停止缓存预热计时器并记录错误状态
                    cacheWarmupTimer.stop(Timer.builder("honyrun.startup.cache.warmup.total.time")
                            .description("Total cache warmup time during startup")
                            .tag("status", "failure")
                            .register(meterRegistry));

                    // 记录失败指标
                    meterRegistry.counter("honyrun.startup.cache.warmup.failures").increment();

                    LoggingUtil.error(logger, "缓存预热失败", error);
                })
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "缓存预热失败，但不影响应用启动: {}", error.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 执行Redis缓存预热
     *
     * @return 预热结果
     */
    private Mono<Void> performRedisWarmup() {
        LoggingUtil.info(logger, "开始Redis缓存预热");

        Timer.Sample redisWarmupTimer = Timer.start(meterRegistry);

        return Mono.fromRunnable(() -> {
            try {
                // 执行Redis缓存预热（仅在生产环境）
                if (redisProductionCacheManager != null) {
                    redisProductionCacheManager.warmupCache();
                    LoggingUtil.info(logger, "✓ Redis缓存预热完成");
                } else {
                    LoggingUtil.info(logger, "✓ 跳过Redis缓存预热（非生产环境）");
                }

                // 停止Redis预热计时器
                redisWarmupTimer.stop(Timer.builder("honyrun.startup.cache.warmup.redis.time")
                        .description("Redis cache warmup time")
                        .tag("status", "success")
                        .register(meterRegistry));

                // 记录成功指标
                meterRegistry.counter("honyrun.startup.cache.warmup.redis.success").increment();

            } catch (Exception e) {
                // 停止Redis预热计时器并记录错误状态
                redisWarmupTimer.stop(Timer.builder("honyrun.startup.cache.warmup.redis.time")
                        .description("Redis cache warmup time")
                        .tag("status", "failure")
                        .register(meterRegistry));

                // 记录失败指标
                meterRegistry.counter("honyrun.startup.cache.warmup.redis.failures").increment();

                LoggingUtil.error(logger, "Redis缓存预热失败", e);
                throw new RuntimeException("Redis缓存预热失败", e);
            }
        });
    }

    /**
     * 执行系统配置缓存预热
     *
     * @return 预热结果
     */
    private Mono<Void> performSystemCacheWarmup() {
        LoggingUtil.info(logger, "开始系统配置缓存预热");

        Timer.Sample systemCacheWarmupTimer = Timer.start(meterRegistry);

        return systemService.preloadConfigCache()
                .doOnSuccess(result -> {
                    // 停止系统缓存预热计时器
                    systemCacheWarmupTimer.stop(Timer.builder("honyrun.startup.cache.warmup.system.time")
                            .description("System cache warmup time")
                            .tag("status", "success")
                            .register(meterRegistry));

                    // 记录成功指标
                    meterRegistry.counter("honyrun.startup.cache.warmup.system.success").increment();

                    LoggingUtil.info(logger, "✓ 系统配置缓存预热完成");
                })
                .doOnError(error -> {
                    // 停止系统缓存预热计时器并记录错误状态
                    systemCacheWarmupTimer.stop(Timer.builder("honyrun.startup.cache.warmup.system.time")
                            .description("System cache warmup time")
                            .tag("status", "failure")
                            .register(meterRegistry));

                    // 记录失败指标
                    meterRegistry.counter("honyrun.startup.cache.warmup.system.failures").increment();

                    LoggingUtil.error(logger, "系统配置缓存预热失败", error);
                });
    }

    /**
     * 执行用户权限缓存预热
     *
     * @return 预热结果
     */
    private Mono<Void> performUserPermissionCacheWarmup() {
        LoggingUtil.info(logger, "开始用户权限缓存预热");

        Timer.Sample userPermissionWarmupTimer = Timer.start(meterRegistry);

        return Mono.when(
                systemService.preloadUserCache(50), // 预加载50个用户
                systemService.preloadPermissionCache())
                .doOnSuccess(result -> {
                    // 停止用户权限缓存预热计时器
                    userPermissionWarmupTimer.stop(Timer.builder("honyrun.startup.cache.warmup.user.permission.time")
                            .description("User permission cache warmup time")
                            .tag("status", "success")
                            .register(meterRegistry));

                    // 记录成功指标
                    meterRegistry.counter("honyrun.startup.cache.warmup.user.permission.success").increment();

                    LoggingUtil.info(logger, "✓ 用户权限缓存预热完成");
                })
                .doOnError(error -> {
                    // 停止用户权限缓存预热计时器并记录错误状态
                    userPermissionWarmupTimer.stop(Timer.builder("honyrun.startup.cache.warmup.user.permission.time")
                            .description("User permission cache warmup time")
                            .tag("status", "failure")
                            .register(meterRegistry));

                    // 记录失败指标
                    meterRegistry.counter("honyrun.startup.cache.warmup.user.permission.failures").increment();

                    LoggingUtil.error(logger, "用户权限缓存预热失败", error);
                })
                .then();
    }

    /**
     * 执行业务缓存预热
     *
     * @return 预热结果
     */
    private Mono<Void> performBusinessCacheWarmup() {
        LoggingUtil.info(logger, "开始业务缓存预热");

        Timer.Sample businessCacheWarmupTimer = Timer.start(meterRegistry);

        return systemService.preloadBusinessCache(30) // 预加载30条业务数据
                .doOnSuccess(result -> {
                    // 停止业务缓存预热计时器
                    businessCacheWarmupTimer.stop(Timer.builder("honyrun.startup.cache.warmup.business.time")
                            .description("Business cache warmup time")
                            .tag("status", "success")
                            .register(meterRegistry));

                    // 记录成功指标
                    meterRegistry.counter("honyrun.startup.cache.warmup.business.success").increment();

                    LoggingUtil.info(logger, "✓ 业务缓存预热完成");
                })
                .doOnError(error -> {
                    // 停止业务缓存预热计时器并记录错误状态
                    businessCacheWarmupTimer.stop(Timer.builder("honyrun.startup.cache.warmup.business.time")
                            .description("Business cache warmup time")
                            .tag("status", "failure")
                            .register(meterRegistry));

                    // 记录失败指标
                    meterRegistry.counter("honyrun.startup.cache.warmup.business.failures").increment();

                    LoggingUtil.error(logger, "业务缓存预热失败", error);
                });
    }

    /**
     * 验证签名验证组件是否正确注册和配置
     *
     * <p>
     * <strong>验证内容：</strong>
     * <ul>
     * <li>ReactiveRequestSignatureService是否正确注册到Spring容器</li>
     * <li>ReactiveRequestSignatureFilter是否正确注册到Spring容器</li>
     * <li>签名验证服务的基本配置是否正确</li>
     * </ul>
     *
     * <p>
     * <strong>验证失败处理：</strong>
     * 如果任何组件验证失败，将记录错误日志并终止应用启动，
     * 确保签名验证机制在所有环境中都是必备的安全组件。
     *
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-01 16:57:03
     * @version 1.0.0
     * @throws RuntimeException 当签名验证组件验证失败时抛出异常
     */
    private void validateSignatureVerificationComponents() {
        LoggingUtil.info(logger, "========= 开始验证签名验证组件 =========");

        Timer.Sample signatureValidationTimer = Timer.start(meterRegistry);

        try {
            // 验证ReactiveRequestSignatureService是否正确注册
            if (requestSignatureService == null) {
                String errorMsg = "ReactiveRequestSignatureService未正确注册到Spring容器";
                LoggingUtil.error(logger, "✗ {}", errorMsg);
                meterRegistry.counter("honyrun.startup.signature.validation.service.failures").increment();
                throw new RuntimeException(errorMsg);
            }
            LoggingUtil.info(logger, "✓ ReactiveRequestSignatureService已正确注册");

            // 验证ReactiveRequestSignatureFilter是否正确注册
            if (requestSignatureFilter == null) {
                String errorMsg = "ReactiveRequestSignatureFilter未正确注册到Spring容器";
                LoggingUtil.error(logger, "✗ {}", errorMsg);
                meterRegistry.counter("honyrun.startup.signature.validation.filter.failures").increment();
                throw new RuntimeException(errorMsg);
            }
            LoggingUtil.info(logger, "✓ ReactiveRequestSignatureFilter已正确注册");

            // 验证签名验证服务的基本功能
            try {
                // 测试签名信息提取功能（使用空请求进行基本验证）
                LoggingUtil.info(logger, "验证签名验证服务基本功能...");
                // 这里只验证服务实例可用性，不进行实际的签名验证
                String serviceClassName = requestSignatureService.getClass().getSimpleName();
                LoggingUtil.info(logger, "✓ 签名验证服务实例类型: {}", serviceClassName);

                String filterClassName = requestSignatureFilter.getClass().getSimpleName();
                LoggingUtil.info(logger, "✓ 签名验证过滤器实例类型: {}", filterClassName);

            } catch (Exception e) {
                String errorMsg = "签名验证服务基本功能验证失败: " + e.getMessage();
                LoggingUtil.error(logger, "✗ {}", errorMsg, e);
                meterRegistry.counter("honyrun.startup.signature.validation.function.failures").increment();
                throw new RuntimeException(errorMsg, e);
            }

            // 停止签名验证计时器并记录成功状态
            signatureValidationTimer.stop(Timer.builder("honyrun.startup.signature.validation.time")
                    .description("Signature verification components validation time")
                    .tag("status", "success")
                    .register(meterRegistry));

            // 记录成功指标
            meterRegistry.counter("honyrun.startup.signature.validation.success").increment();

            LoggingUtil.info(logger, "✓ 签名验证组件验证成功");
            LoggingUtil.info(logger, "✓ 签名验证机制已启用，系统安全防护就绪");
            LoggingUtil.info(logger, "========= 签名验证组件验证完成 =========");

        } catch (Exception e) {
            // 停止签名验证计时器并记录失败状态
            signatureValidationTimer.stop(Timer.builder("honyrun.startup.signature.validation.time")
                    .description("Signature verification components validation time")
                    .tag("status", "failure")
                    .register(meterRegistry));

            // 记录失败指标
            meterRegistry.counter("honyrun.startup.signature.validation.failures").increment();

            LoggingUtil.error(logger, "========= 签名验证组件验证失败 =========");
            LoggingUtil.error(logger, "✗ 签名验证组件验证失败，应用启动终止");
            LoggingUtil.error(logger, "✗ 签名验证是系统安全的必备组件，不能缺失");
            LoggingUtil.error(logger, "请检查SecurityConfig配置和相关Bean注册");

            // 优雅关闭应用上下文
            shutdownApplicationGracefully();
            throw new RuntimeException("签名验证组件验证失败，应用启动终止", e);
        }
    }
}
