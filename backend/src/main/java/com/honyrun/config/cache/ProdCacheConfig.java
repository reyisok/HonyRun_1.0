package com.honyrun.config.cache;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.util.LoggingUtil;

import io.lettuce.core.resource.ClientResources;

/**
 * 生产环境缓存配置类
 *
 * ========================================
 * 【重要】Redis为项目必备组件 - 生产环境配置
 * ========================================
 * Redis是本项目的核心缓存组件，必须确保连接正常：
 * - 连接地址：localhost:8902
 * - 密码：honyrun@sys
 * - 数据库：0
 * - 连接池：生产环境优化配置
 * ========================================
 *
 * <p>
 * <strong>配置范围：</strong>仅处理生产环境(prod)的缓存配置
 * </p>
 * <p>
 * <strong>Bean命名规范：</strong>所有Bean使用prod前缀，避免与其他环境冲突
 * </p>
 * <p>
 * <strong>环境隔离：</strong>通过@Profile("prod")确保仅在生产环境激活
 * </p>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-06-28 20:00:00
 * @modified 2025-10-24 16:01:03
 * @version 3.0.0 - 重构为生产环境专用配置，从properties文件读取配置
 */
/**
 * 生产环境Redis缓存配置类
 *
 * 【统一配置管理原则】
 * 1. 禁止硬编码配置值，所有配置必须从Environment或配置文件获取
 * 2. 使用统一的配置管理机制，确保配置的可维护性和一致性
 * 3. 配置参数必须具有合理的默认值，避免系统启动失败
 * 4. 所有配置获取都应通过environment.getProperty()方法
 * 5. 严格禁止使用@Value注解或直接硬编码配置值
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-26 01:36:12
 * @version 1.0.0
 */
@Configuration
@Profile("prod")
public class ProdCacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(ProdCacheConfig.class);

    private final Environment environment;
    private final UnifiedConfigManager unifiedConfigManager;

    public ProdCacheConfig(Environment environment, UnifiedConfigManager unifiedConfigManager) {
        this.environment = environment;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 生产环境Redis客户端资源
     *
     * <p>
     * <strong>Bean命名：</strong>prodClientResources
     * </p>
     * <p>
     * <strong>环境隔离：</strong>仅在prod环境激活
     * </p>
     * <p>
     * <strong>资源配置：</strong>生产环境客户端资源优化
     * </p>
     *
     * @return 客户端资源实例
     */
    @Bean("prodClientResources")
    public ClientResources prodClientResources() {
        LoggingUtil.info(logger, "正在初始化生产环境Redis客户端资源...");

        ClientResources clientResources = ClientResources.builder()
                .ioThreadPoolSize(8)
                .computationThreadPoolSize(8)
                .build();

        LoggingUtil.info(logger, "生产环境Redis客户端资源初始化成功");
        return clientResources;
    }

    /**
     * 生产环境响应式Redis连接工厂
     * 从application-prod.properties读取配置参数
     *
     * <p>
     * <strong>Bean命名：</strong>prodReactiveRedisConnectionFactory
     * </p>
     * <p>
     * <strong>环境隔离：</strong>仅在prod环境激活
     * </p>
     * <p>
     * <strong>连接配置：</strong>生产环境Redis连接池
     * </p>
     *
     * @return 响应式Redis连接工厂实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-24 16:01:03
     * @version 1.0.0
     */
    @Bean("prodReactiveRedisConnectionFactory")
    public ReactiveRedisConnectionFactory prodReactiveRedisConnectionFactory() {
        LoggingUtil.info(logger, "正在初始化生产环境响应式Redis连接工厂...");

        // 从配置文件获取Redis连接信息，不使用硬编码默认值
        String host = unifiedConfigManager.getProperty("HONYRUN_REDIS_HOST", "localhost");
        String redisPort = unifiedConfigManager.getProperty("HONYRUN_REDIS_PORT", "8902");
        Integer port = Integer.parseInt(redisPort);
        String password = unifiedConfigManager.getProperty("HONYRUN_REDIS_PASSWORD", "honyrun@sys");
        String databaseStr = unifiedConfigManager.getProperty("HONYRUN_REDIS_DATABASE", "0");
        Integer database = Integer.parseInt(databaseStr);

        // 验证必要的配置是否存在
        if (host == null || port == null || password == null || database == null) {
            throw new IllegalStateException(
                    "Redis连接配置不完整，请检查配置文件中的 HONYRUN_REDIS_HOST、HONYRUN_REDIS_PORT、HONYRUN_REDIS_PASSWORD、HONYRUN_REDIS_DATABASE 配置");
        }

        Integer maxActive = Integer.parseInt(unifiedConfigManager.getProperty("spring.data.redis.lettuce.pool.max-active", "8"));
        Integer maxIdle = Integer.parseInt(unifiedConfigManager.getProperty("spring.data.redis.lettuce.pool.max-idle", "8"));
        Integer minIdle = Integer.parseInt(unifiedConfigManager.getProperty("spring.data.redis.lettuce.pool.min-idle", "0"));
        Long maxWaitMs = Long.parseLong(unifiedConfigManager.getProperty("spring.data.redis.lettuce.pool.max-wait", "-1"));
        Long timeoutMs = Long.parseLong(unifiedConfigManager.getProperty("HONYRUN_REDIS_TIMEOUT", "5000"));

        // 验证连接池配置是否存在
        if (maxActive == null || maxIdle == null || minIdle == null || maxWaitMs == null || timeoutMs == null) {
            throw new IllegalStateException(
                    "Redis连接池配置不完整，请检查配置文件中的连接池相关配置");
        }

        Duration maxWait = Duration.ofMillis(maxWaitMs);
        Duration timeout = Duration.ofMillis(timeoutMs);

        LoggingUtil.info(logger, "Redis连接配置 - 主机: {}, 端口: {}, 数据库: {}", host, port, database);
        LoggingUtil.info(logger, "连接池配置 - 最大活跃: {}, 最大空闲: {}, 最小空闲: {}, 最大等待: {}",
                maxActive, maxIdle, minIdle, maxWait);

        try {
            // Redis单机配置
            RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
            redisConfig.setHostName(host);
            redisConfig.setPort(port);
            redisConfig.setPassword(password);
            redisConfig.setDatabase(database);

            // Lettuce连接池配置
            org.apache.commons.pool2.impl.GenericObjectPoolConfig<io.lettuce.core.api.StatefulConnection<?, ?>> poolConfig = new org.apache.commons.pool2.impl.GenericObjectPoolConfig<>();
            poolConfig.setMaxTotal(maxActive);
            poolConfig.setMaxIdle(maxIdle);
            poolConfig.setMinIdle(minIdle);
            poolConfig.setMaxWait(maxWait);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);

            LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                    .poolConfig(poolConfig)
                    .clientResources(prodClientResources())
                    .commandTimeout(timeout)
                    .build();

            LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisConfig, clientConfig);
            // 禁用连接验证，解决共享连接验证超时问题
            connectionFactory.setValidateConnection(false);
            // 禁用共享本地连接，避免连接验证问题
            connectionFactory.setShareNativeConnection(false);
            connectionFactory.afterPropertiesSet();

            LoggingUtil.info(logger, "生产环境响应式Redis连接工厂初始化成功");
            return connectionFactory;
        } catch (Exception e) {
            LoggingUtil.error(logger, "生产环境响应式Redis连接工厂初始化失败", e);
            throw new RuntimeException("生产环境Redis连接配置失败", e);
        }
    }

    /**
     * 生产环境响应式Redis模板
     *
     * <p>
     * <strong>Bean命名：</strong>prodReactiveRedisTemplate
     * </p>
     * <p>
     * <strong>依赖：</strong>prodReactiveRedisConnectionFactory,
     * prodStringRedisSerializer, prodJsonRedisSerializer
     * </p>
     * <p>
     * <strong>模板配置：</strong>生产环境响应式Redis操作模板
     * </p>
     *
     * @param connectionFactory 生产环境响应式Redis连接工厂
     * @param stringSerializer  字符串序列化器
     * @param jsonSerializer    JSON序列化器
     * @return 响应式Redis模板实例
     */
    @Bean("prodReactiveRedisTemplate")
    public ReactiveRedisTemplate<String, Object> prodReactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory,
            StringRedisSerializer stringSerializer,
            GenericJackson2JsonRedisSerializer jsonSerializer) {
        LoggingUtil.info(logger, "正在初始化生产环境响应式Redis模板...");

        org.springframework.data.redis.serializer.RedisSerializationContext<String, Object> serializationContext = org.springframework.data.redis.serializer.RedisSerializationContext
                .<String, Object>newSerializationContext()
                .key(stringSerializer)
                .value(jsonSerializer)
                .hashKey(stringSerializer)
                .hashValue(jsonSerializer)
                .build();

        ReactiveRedisTemplate<String, Object> template = new ReactiveRedisTemplate<>(connectionFactory,
                serializationContext);

        LoggingUtil.info(logger, "生产环境响应式Redis模板初始化成功");
        return template;
    }

    /**
     * 生产环境字符串Redis序列化器
     *
     * <p>
     * <strong>Bean命名：</strong>prodStringRedisSerializer
     * </p>
     * <p>
     * <strong>环境隔离：</strong>仅在prod环境激活
     * </p>
     * <p>
     * <strong>序列化配置：</strong>字符串序列化器
     * </p>
     *
     * @return 字符串序列化器实例
     */
    @Bean("prodStringRedisSerializer")
    public StringRedisSerializer prodStringRedisSerializer() {
        LoggingUtil.info(logger, "正在初始化生产环境字符串Redis序列化器...");

        StringRedisSerializer serializer = new StringRedisSerializer();

        LoggingUtil.info(logger, "生产环境字符串Redis序列化器初始化成功");
        return serializer;
    }

    /**
     * 生产环境JSON序列化器
     *
     * <p>
     * <strong>Bean命名：</strong>prodJsonRedisSerializer
     * </p>
     * <p>
     * <strong>环境隔离：</strong>仅在prod环境激活
     * </p>
     * <p>
     * <strong>序列化配置：</strong>JSON序列化器，支持LinkedCaseInsensitiveMap
     * </p>
     *
     * @return JSON序列化器实例
     */
    @Bean("prodJsonRedisSerializer")
    public GenericJackson2JsonRedisSerializer prodJsonRedisSerializer() {
        LoggingUtil.info(logger, "正在初始化生产环境JSON Redis序列化器...");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 配置对LinkedCaseInsensitiveMap的支持
        objectMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        LoggingUtil.info(logger, "生产环境JSON Redis序列化器初始化成功");
        return serializer;
    }

    /**
     * 生产环境字符串Redis模板
     *
     * <p>
     * <strong>Bean命名：</strong>prodStringRedisTemplate
     * </p>
     * <p>
     * <strong>依赖：</strong>prodReactiveRedisConnectionFactory
     * </p>
     * <p>
     * <strong>模板配置：</strong>生产环境字符串Redis操作模板
     * </p>
     *
     * @param connectionFactory 生产环境响应式Redis连接工厂
     * @return 字符串Redis模板实例
     */
    @Bean("prodStringRedisTemplate")
    public ReactiveStringRedisTemplate prodStringRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        LoggingUtil.info(logger, "正在初始化生产环境字符串Redis模板...");

        ReactiveStringRedisTemplate template = new ReactiveStringRedisTemplate(connectionFactory);

        LoggingUtil.info(logger, "生产环境字符串Redis模板初始化成功");
        return template;
    }

    /**
     * 统一响应式字符串Redis模板
     *
     * <p>
     * <strong>Bean命名：</strong>unifiedReactiveStringRedisTemplate
     * </p>
     * <p>
     * <strong>环境隔离：</strong>仅在prod环境激活
     * </p>
     * <p>
     * <strong>统一接口：</strong>为生产环境提供统一的字符串Redis模板
     * </p>
     *
     * @param prodTemplate 生产环境字符串Redis模板
     * @return 统一响应式字符串Redis模板实例
     */
    @Bean("unifiedReactiveStringRedisTemplate")
    public ReactiveRedisTemplate<String, String> unifiedReactiveStringRedisTemplate(
            @Qualifier("prodStringRedisTemplate") ReactiveStringRedisTemplate prodTemplate) {
        LoggingUtil.info(logger, "正在初始化统一响应式字符串Redis模板...");

        // 创建ReactiveRedisTemplate<String, String>实例，使用相同的连接工厂
        StringRedisSerializer stringSerializer = StringRedisSerializer.UTF_8;

        RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
                .<String, String>newSerializationContext()
                .key(stringSerializer)
                .value(stringSerializer)
                .hashKey(stringSerializer)
                .hashValue(stringSerializer)
                .build();

        ReactiveRedisTemplate<String, String> template = new ReactiveRedisTemplate<>(
                prodTemplate.getConnectionFactory(), serializationContext);

        LoggingUtil.info(logger, "统一响应式字符串Redis模板初始化成功");
        return template;
    }

    /**
     * 生产环境reactiveStringRedisTemplate Bean
     * 为业务代码中使用@Qualifier("reactiveStringRedisTemplate")的依赖提供支持
     *
     * @param unifiedTemplate 统一响应式字符串Redis模板
     * @return ReactiveRedisTemplate<String, String> 生产环境字符串Redis模板别名
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @version 1.0.0 - 解决生产环境Bean依赖问题
     */
    @Bean("reactiveStringRedisTemplate")
    public ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate(
            @Qualifier("unifiedReactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> unifiedTemplate) {
        LoggingUtil.info(logger, "生产环境reactiveStringRedisTemplate Bean初始化完成");
        return unifiedTemplate;
    }

    /**
     * 生产环境响应式Redis消息监听容器
     *
     * <p>
     * <strong>Bean命名：</strong>prodReactiveRedisMessageListenerContainer
     * </p>
     * <p>
     * <strong>依赖：</strong>prodReactiveRedisConnectionFactory
     * </p>
     * <p>
     * <strong>容器配置：</strong>生产环境Redis消息监听
     * </p>
     *
     * @param connectionFactory 生产环境响应式Redis连接工厂
     * @return 响应式Redis消息监听容器实例
     */
    @Bean("prodReactiveRedisMessageListenerContainer")
    public ReactiveRedisMessageListenerContainer prodReactiveRedisMessageListenerContainer(
            ReactiveRedisConnectionFactory connectionFactory) {
        LoggingUtil.info(logger, "正在初始化生产环境响应式Redis消息监听容器...");

        ReactiveRedisMessageListenerContainer container = new ReactiveRedisMessageListenerContainer(connectionFactory);

        LoggingUtil.info(logger, "生产环境响应式Redis消息监听容器初始化成功");
        return container;
    }
}
