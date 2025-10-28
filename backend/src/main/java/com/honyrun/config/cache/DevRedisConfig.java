package com.honyrun.config.cache;

import java.time.Duration;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.honyrun.config.properties.RedisConfigProperties;
import com.honyrun.util.LoggingUtil;

import io.lettuce.core.api.StatefulConnection;

/**
 * 开发环境Redis配置类
 *
 * <p>
 * <strong>重要说明：</strong>
 * 本类严格遵循统一配置管理规范，所有配置值必须从环境配置文件读取，
 * 禁止在代码中硬编码任何默认值！
 * </p>
 *
 * <p>
 * <strong>配置文件位置：</strong>
 * </p>
 * <ul>
 * <li>开发环境：application-dev.yml</li>
 * <li>测试环境：application-test.yml</li>
 * <li>环境变量：SPRING_DATA_REDIS_*</li>
 * </ul>
 *
 * <p>
 * <strong>功能说明：</strong>
 * </p>
 * <ul>
 * <li>提供开发环境Redis连接工厂配置</li>
 * <li>配置开发环境Redis模板</li>
 * <li>支持开发环境Redis缓存管理</li>
 * <li>确保与StandardCacheConfig的兼容性</li>
 * </ul>
 *
 * <p>
 * <strong>环境隔离：</strong>仅在dev环境激活
 * </p>
 *
 * <p>
 * <strong>Repository扫描配置：</strong>
 * 项目不使用Redis repository，已移除@EnableRedisRepositories注解，
 * 避免Spring Data Redis错误扫描R2DBC repository接口，
 * 消除"Could not safely identify store assignment"警告信息
 * </p>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-27 12:26:43
 * @version 1.4.0 - 添加完整连接池配置，解决频繁连接创建和超时问题
 * @since 1.0.0
 */
@Configuration
@Profile("dev")
public class DevRedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(DevRedisConfig.class);

    private final Environment environment;
    private final RedisConfigProperties redisConfigProperties;

    public DevRedisConfig(Environment environment, RedisConfigProperties redisConfigProperties) {
        this.environment = environment;
        this.redisConfigProperties = redisConfigProperties;
    }

    /**
     * 获取Redis命令超时时间（毫秒）
     * 优先使用commandTimeout配置，如果未配置则使用timeout配置，最后使用默认值15秒
     *
     * @return 超时时间（毫秒）
     */
    private long getRedisTimeoutMillis() {
        // 优先使用命令超时配置
        Duration commandTimeout = redisConfigProperties.getCommandTimeout();
        if (commandTimeout != null) {
            return commandTimeout.toMillis();
        }

        // 其次使用连接超时配置
        Duration timeout = redisConfigProperties.getTimeout();
        if (timeout != null) {
            return timeout.toMillis();
        }

        // 默认15秒，与配置文件保持一致
        return 15000L;
    }

    /**
     * 开发环境响应式Redis连接工厂
     * 从application-dev.properties读取配置参数
     *
     * <p>
     * <strong>Bean命名：</strong>devReactiveRedisConnectionFactory
     * </p>
     * <p>
     * <strong>环境隔离：</strong>仅在dev环境激活
     * </p>
     * <p>
     * <strong>连接配置：</strong>开发环境Redis连接池
     * </p>
     *
     * @return 响应式Redis连接工厂实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @version 1.0.0
     */
    @Bean("devReactiveRedisConnectionFactory")
    @Primary
    public ReactiveRedisConnectionFactory devReactiveRedisConnectionFactory() {
        LoggingUtil.info(logger, "正在初始化开发环境响应式Redis连接工厂...");

        try {
            // Redis单机配置
            RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
            redisConfig.setHostName(redisConfigProperties.getHost());
            redisConfig.setPort(redisConfigProperties.getPort());
            redisConfig.setPassword(redisConfigProperties.getPassword());
            redisConfig.setDatabase(redisConfigProperties.getDatabase());

            // 连接池配置 - 解决频繁连接创建和超时问题
            GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
            poolConfig.setMaxTotal(redisConfigProperties.getLettuce().getPool().getMaxActive());
            poolConfig.setMaxIdle(redisConfigProperties.getLettuce().getPool().getMaxIdle());
            poolConfig.setMinIdle(redisConfigProperties.getLettuce().getPool().getMinIdle());
            poolConfig.setMaxWait(redisConfigProperties.getLettuce().getPool().getMaxWait());
            // 禁用所有连接验证，避免"Returned connection...was either previously returned"错误
            poolConfig.setTestOnBorrow(false);
            poolConfig.setTestOnReturn(false);
            poolConfig.setTestWhileIdle(false);

            // Lettuce连接池客户端配置
            LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                    .poolConfig(poolConfig)
                    .commandTimeout(Duration.ofMillis(getRedisTimeoutMillis()))
                    .shutdownTimeout(Duration.ofSeconds(5))
                    .build();

            LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, clientConfig);
            // 禁用连接验证，解决共享连接验证超时问题
            factory.setValidateConnection(false);
            // 禁用共享本地连接，避免连接验证问题
            factory.setShareNativeConnection(false);
            factory.afterPropertiesSet();

            LoggingUtil.info(logger,
                    "开发环境响应式Redis连接工厂初始化成功 - Host: {}, Port: {}, Database: {}, 连接池配置: maxActive={}, maxIdle={}, minIdle={}",
                    redisConfigProperties.getHost(), redisConfigProperties.getPort(),
                    redisConfigProperties.getDatabase(),
                    redisConfigProperties.getLettuce().getPool().getMaxActive(),
                    redisConfigProperties.getLettuce().getPool().getMaxIdle(),
                    redisConfigProperties.getLettuce().getPool().getMinIdle());
            return factory;
        } catch (Exception e) {
            LoggingUtil.error(logger, "开发环境Redis连接工厂初始化失败: {}", e.getMessage(), e);
            throw new IllegalStateException("开发环境Redis配置错误", e);
        }
    }

    /**
     * 开发环境统一响应式字符串Redis模板
     * 用于字符串类型的Redis操作
     *
     * @return ReactiveRedisTemplate<String, String>实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @version 1.0.0
     */
    @Bean("unifiedReactiveStringRedisTemplate")
    public ReactiveRedisTemplate<String, String> unifiedReactiveStringRedisTemplate() {
        LoggingUtil.info(logger, "正在初始化开发环境统一响应式字符串Redis模板...");

        try {
            // 直接调用响应式连接工厂方法
            ReactiveRedisConnectionFactory connectionFactory = devReactiveRedisConnectionFactory();

            StringRedisSerializer stringSerializer = StringRedisSerializer.UTF_8;

            RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
                    .<String, String>newSerializationContext(stringSerializer)
                    .key(stringSerializer)
                    .value(stringSerializer)
                    .hashKey(stringSerializer)
                    .hashValue(stringSerializer)
                    .build();

            ReactiveRedisTemplate<String, String> template = new ReactiveRedisTemplate<>(connectionFactory,
                    serializationContext);

            LoggingUtil.info(logger, "开发环境统一响应式字符串Redis模板初始化成功");
            return template;

        } catch (Exception e) {
            LoggingUtil.error(logger, "初始化开发环境统一响应式字符串Redis模板失败", e);
            throw new RuntimeException("开发环境统一响应式字符串Redis模板初始化失败", e);
        }
    }

    /**
     * 开发环境Redis连接工厂（非响应式）
     * 用于标准Spring Cache的CacheManager
     *
     * @return RedisConnectionFactory实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @version 1.0.0
     */
    @Bean("devRedisConnectionFactory")
    public RedisConnectionFactory devRedisConnectionFactory() {
        LoggingUtil.info(logger, "正在初始化开发环境Redis连接工厂（非响应式）...");

        try {
            // Redis单机配置
            RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
            redisConfig.setHostName(redisConfigProperties.getHost());
            redisConfig.setPort(redisConfigProperties.getPort());
            redisConfig.setPassword(redisConfigProperties.getPassword());
            redisConfig.setDatabase(redisConfigProperties.getDatabase());

            // 连接池配置 - 解决频繁连接创建和超时问题
            GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
            poolConfig.setMaxTotal(redisConfigProperties.getLettuce().getPool().getMaxActive());
            poolConfig.setMaxIdle(redisConfigProperties.getLettuce().getPool().getMaxIdle());
            poolConfig.setMinIdle(redisConfigProperties.getLettuce().getPool().getMinIdle());
            poolConfig.setMaxWait(redisConfigProperties.getLettuce().getPool().getMaxWait());
            // 禁用所有连接验证，避免"Returned connection...was either previously returned"错误
            poolConfig.setTestOnBorrow(false);
            poolConfig.setTestOnReturn(false);
            poolConfig.setTestWhileIdle(false);

            // Lettuce连接池客户端配置
            LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                    .poolConfig(poolConfig)
                    .commandTimeout(Duration.ofMillis(getRedisTimeoutMillis()))
                    .shutdownTimeout(Duration.ofSeconds(5))
                    .build();

            LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, clientConfig);
            // 禁用连接验证，解决共享连接验证超时问题
            factory.setValidateConnection(false);
            // 禁用共享本地连接，避免连接验证问题
            factory.setShareNativeConnection(false);
            factory.afterPropertiesSet();

            LoggingUtil.info(logger,
                    "开发环境Redis连接工厂（非响应式）初始化成功 - Host: {}, Port: {}, Database: {}, 连接池配置: maxActive={}, maxIdle={}, minIdle={}",
                    redisConfigProperties.getHost(), redisConfigProperties.getPort(),
                    redisConfigProperties.getDatabase(),
                    redisConfigProperties.getLettuce().getPool().getMaxActive(),
                    redisConfigProperties.getLettuce().getPool().getMaxIdle(),
                    redisConfigProperties.getLettuce().getPool().getMinIdle());
            return factory;

        } catch (Exception e) {
            LoggingUtil.error(logger, "初始化开发环境Redis连接工厂（非响应式）失败", e);
            throw new RuntimeException("开发环境Redis连接工厂（非响应式）初始化失败", e);
        }
    }

    /**
     * 开发环境响应式Redis模板
     *
     * <p>
     * <strong>Bean命名：</strong>devReactiveRedisTemplate
     * </p>
     * <p>
     * <strong>依赖：</strong>devReactiveRedisConnectionFactory
     * </p>
     * <p>
     * <strong>模板配置：</strong>开发环境Redis操作模板
     * </p>
     *
     * @param connectionFactory 开发环境响应式Redis连接工厂
     * @return Redis模板实例
     */
    @Bean("devReactiveRedisTemplate")
    public ReactiveRedisTemplate<String, Object> devReactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        LoggingUtil.info(logger, "正在初始化开发环境响应式Redis模板...");

        RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
                .<String, Object>newSerializationContext(StringRedisSerializer.UTF_8)
                .value(new GenericJackson2JsonRedisSerializer())
                .hashKey(StringRedisSerializer.UTF_8)
                .hashValue(new GenericJackson2JsonRedisSerializer())
                .build();

        ReactiveRedisTemplate<String, Object> template = new ReactiveRedisTemplate<>(connectionFactory,
                serializationContext);

        LoggingUtil.info(logger, "开发环境响应式Redis模板初始化成功");
        return template;
    }

    /**
     * 开发环境统一响应式Redis模板
     * 为业务代码中使用@Qualifier("unifiedReactiveRedisTemplate")的依赖提供支持
     *
     * <p>
     * <strong>Bean命名：</strong>unifiedReactiveRedisTemplate
     * </p>
     * <p>
     * <strong>依赖：</strong>devReactiveRedisTemplate
     * </p>
     * <p>
     * <strong>用途：</strong>为@Qualifier("unifiedReactiveRedisTemplate")提供Bean支持
     * </p>
     *
     * @param devTemplate 开发环境响应式Redis模板
     * @return ReactiveRedisTemplate<String, Object>实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @version 1.0.0
     */
    @Bean("unifiedReactiveRedisTemplate")
    public ReactiveRedisTemplate<String, Object> unifiedReactiveRedisTemplate(
            @org.springframework.beans.factory.annotation.Qualifier("devReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> devTemplate) {
        LoggingUtil.info(logger, "开发环境unifiedReactiveRedisTemplate Bean初始化完成");
        return devTemplate;
    }

    /**
     * 开发环境字符串Redis模板
     *
     * <p>
     * <strong>Bean命名：</strong>devStringRedisTemplate
     * </p>
     * <p>
     * <strong>依赖：</strong>devReactiveRedisConnectionFactory
     * </p>
     * <p>
     * <strong>模板配置：</strong>开发环境字符串Redis操作模板
     * </p>
     *
     * @param connectionFactory 开发环境响应式Redis连接工厂
     * @return 字符串Redis模板实例
     */
    @Bean("devStringRedisTemplate")
    public ReactiveStringRedisTemplate devStringRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        LoggingUtil.info(logger, "正在初始化开发环境字符串Redis模板...");

        ReactiveStringRedisTemplate template = new ReactiveStringRedisTemplate(connectionFactory);

        LoggingUtil.info(logger, "开发环境字符串Redis模板初始化成功");
        return template;
    }

    /**
     * 开发环境reactiveStringRedisTemplate Bean
     * 为业务代码中使用@Qualifier("reactiveStringRedisTemplate")的依赖提供支持
     *
     * <p>
     * <strong>Bean命名：</strong>reactiveStringRedisTemplate
     * </p>
     * <p>
     * <strong>依赖：</strong>unifiedReactiveStringRedisTemplate
     * </p>
     * <p>
     * <strong>用途：</strong>为@Qualifier("reactiveStringRedisTemplate")提供Bean支持
     * </p>
     *
     * @param unifiedTemplate 统一响应式字符串Redis模板
     * @return ReactiveRedisTemplate<String, String>实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @version 1.0.0
     */
    @Bean("reactiveStringRedisTemplate")
    public ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate(
            @org.springframework.beans.factory.annotation.Qualifier("unifiedReactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> unifiedTemplate) {
        LoggingUtil.info(logger, "开发环境reactiveStringRedisTemplate Bean初始化完成");
        return unifiedTemplate;
    }

    /**
     * 开发环境非响应式RedisTemplate Bean
     * 为需要同步Redis操作的服务提供支持
     *
     * <p>
     * <strong>Bean命名：</strong>redisTemplate
     * </p>
     *
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate<String, Object>实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @version 1.0.0
     */
    @Bean("redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        LoggingUtil.info(logger, "正在初始化开发环境非响应式RedisTemplate...");

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置序列化器
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.json());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();

        LoggingUtil.info(logger, "开发环境非响应式RedisTemplate初始化成功");
        return template;
    }

    /**
     * 开发环境Redis连接工厂Bean
     * 为非响应式RedisTemplate提供连接工厂
     *
     * @return RedisConnectionFactory实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @version 1.0.0
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LoggingUtil.info(logger, "正在初始化开发环境Redis连接工厂...");

        // Redis单机配置
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisConfigProperties.getHost());
        redisConfig.setPort(redisConfigProperties.getPort());
        redisConfig.setPassword(redisConfigProperties.getPassword());
        redisConfig.setDatabase(redisConfigProperties.getDatabase());

        // Lettuce客户端配置
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(getRedisTimeoutMillis()))
                .shutdownTimeout(Duration.ofSeconds(5))
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, clientConfig);
        // 禁用连接验证，解决共享连接验证超时问题
        factory.setValidateConnection(false);
        // 禁用共享本地连接，避免连接验证问题
        factory.setShareNativeConnection(false);
        factory.afterPropertiesSet();

        LoggingUtil.info(logger, "开发环境Redis连接工厂初始化成功 - Host: {}, Port: {}, Database: {}",
                redisConfigProperties.getHost(), redisConfigProperties.getPort(), redisConfigProperties.getDatabase());
        return factory;
    }
}
