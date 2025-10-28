package com.honyrun.config.cache;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.honyrun.util.LoggingUtil;

/**
 * 标准缓存管理器配置类
 *
 * 提供标准的Spring Cache CacheManager实现，支持Redis缓存
 * 解决SystemConfigManager等组件对CacheManager的依赖需求
 *
 * 配置范围：
 * - 标准CacheManager bean定义
 * - Redis缓存配置和序列化
 * - 缓存TTL和键前缀设置
 * - 缓存null值处理策略
 *
 * 依赖关系：
 * - 依赖配置：Redis连接工厂
 * - 提供Bean：标准CacheManager实例
 * - 环境要求：Redis服务可用
 *
 * 使用方式：
 * 自动注入到需要CacheManager的组件中
 *
 * 注意事项：
 * - 使用@Primary确保优先注入
 * - 配置合理的TTL避免内存泄漏
 * - 支持JSON序列化提高可读性
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-27 12:26:43
 * @version 1.1.0 - 移除UnifiedConfigManager依赖，解决循环依赖问题，直接使用Environment获取配置
 */
@Configuration
@EnableCaching
public class StandardCacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(StandardCacheConfig.class);

    private final Environment environment;

    /**
     * 构造函数 - 移除UnifiedConfigManager依赖以解决循环依赖
     *
     * @param environment Spring环境配置
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-27 12:26:43
     * @modified 2025-10-27 12:26:43
     * @version 1.0.0
     */
    public StandardCacheConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * 标准缓存管理器配置
     *
     * <p>
     * <strong>功能说明：</strong>
     * </p>
     * <ul>
     * <li>提供标准的Spring Cache Manager实现</li>
     * <li>使用Redis作为缓存存储</li>
     * <li>配置缓存TTL和序列化</li>
     * <li>支持SystemConfigManager的依赖注入</li>
     * </ul>
     *
     * 注意：直接使用Environment获取配置以避免循环依赖
     *
     * @param connectionFactory Redis连接工厂
     * @return 标准缓存管理器实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-27 12:26:43
     * @version 1.1.0 - 移除UnifiedConfigManager依赖，直接使用Environment
     */
    @Bean("standardCacheManager")
    public CacheManager standardCacheManager(
            @Qualifier("devRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        LoggingUtil.info(logger, "正在初始化标准缓存管理器...");

        try {
            // 直接从Environment获取缓存TTL配置，避免循环依赖
            String ttlStr = environment.getProperty("honyrun.cache.standard.ttl-minutes", "30");
            long standardCacheTtlMinutes = Long.parseLong(ttlStr);
            
            // 配置Redis缓存
            RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(standardCacheTtlMinutes)) // 外部化TTL配置
                    .serializeKeysWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                    .computePrefixWith(cacheName -> "honyrun:cache:" + cacheName + ":")
                    .disableCachingNullValues(); // 不缓存null值

            // 创建缓存管理器
            RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(cacheConfig)
                    .transactionAware()
                    .build();

            LoggingUtil.info(logger, "标准缓存管理器初始化成功 - TTL: {}分钟, 键前缀: honyrun:cache:", standardCacheTtlMinutes);
            return cacheManager;

        } catch (Exception e) {
            LoggingUtil.error(logger, "初始化标准缓存管理器失败", e);
            throw new RuntimeException("标准缓存管理器初始化失败", e);
        }
    }
}
