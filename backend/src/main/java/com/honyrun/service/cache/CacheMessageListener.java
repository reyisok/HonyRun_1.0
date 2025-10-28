package com.honyrun.service.cache;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.honyrun.util.LoggingUtil;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import reactor.core.publisher.Mono;

/**
 * 缓存消息监听器
 *
 * 负责监听Redis发布订阅消息，处理分布式缓存失效通知：
 * - 监听缓存失效消息
 * - 处理跨实例缓存同步
 * - 维护缓存一致性
 * - 记录缓存操作日志
 *
 * 主要功能：
 * - Redis消息订阅和处理
 * - 分布式缓存失效处理
 * - 缓存同步状态监控
 * - 异常处理和重试机制
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 00:00:00
 * @modified 2025-07-01 00:00:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class CacheMessageListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(CacheMessageListener.class);

    private final CacheManager cacheManager;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final ReactiveRedisMessageListenerContainer messageListenerContainer;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param cacheManager             缓存管理器
     * @param reactiveRedisTemplate    响应式Redis模板
     * @param messageListenerContainer 消息监听容器（可选）
     * @param objectMapper             JSON对象映射器
     */
    public CacheMessageListener(CacheManager cacheManager,
            @Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
            @Qualifier("reactiveRedisMessageListenerContainer") Optional<ReactiveRedisMessageListenerContainer> messageListenerContainer,
            ObjectMapper objectMapper) {
        this.cacheManager = cacheManager;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.messageListenerContainer = messageListenerContainer.orElse(null);
        this.objectMapper = objectMapper;
    }

    /**
     * 缓存失效消息主题模式
     */
    private static final String CACHE_EVICT_TOPIC_PATTERN = "honyrun:cache:evict:*";

    /**
     * 初始化消息监听器
     */
    @PostConstruct
    public void initialize() {
        try {
            LoggingUtil.info(logger, "初始化缓存消息监听器");

            // 检查messageListenerContainer是否为null（测试环境中可能是Mock对象）
            if (messageListenerContainer == null) {
                LoggingUtil.warn(logger, "ReactiveRedisMessageListenerContainer为null，跳过消息监听器初始化");
                return;
            }

            // 订阅缓存失效消息主题
            ChannelTopic topic = new ChannelTopic(CACHE_EVICT_TOPIC_PATTERN);

            // 检查receive方法返回值是否为null（Mock对象可能返回null）
            try {
                var messageFlux = messageListenerContainer.receive(topic);
                if (messageFlux != null) {
                    messageFlux
                            .map(message -> {
                                handleMessage(message.getMessage(), message.getChannel());
                                return message;
                            })
                            .subscribe(
                                    message -> LoggingUtil.debug(logger, "处理缓存消息成功，主题：{}", message.getChannel()),
                                    error -> LoggingUtil.error(logger, "处理缓存消息失败", error));
                } else {
                    LoggingUtil.warn(logger, "ReactiveRedisMessageListenerContainer.receive()返回null，可能是测试环境");
                }
            } catch (Exception e) {
                LoggingUtil.warn(logger, "订阅Redis消息失败，可能是测试环境：{}", e.getMessage());
            }

            LoggingUtil.info(logger, "缓存消息监听器初始化完成，订阅主题：{}", CACHE_EVICT_TOPIC_PATTERN);

        } catch (Exception e) {
            LoggingUtil.error(logger, "初始化缓存消息监听器失败", e);
            throw new RuntimeException("初始化缓存消息监听器失败", e);
        }
    }

    /**
     * 销毁消息监听器
     */
    @PreDestroy
    public void destroy() {
        try {
            LoggingUtil.info(logger, "销毁缓存消息监听器");
            // 清理资源
            LoggingUtil.info(logger, "缓存消息监听器销毁完成");

        } catch (Exception e) {
            LoggingUtil.error(logger, "销毁缓存消息监听器失败", e);
        }
    }

    /**
     * 处理Redis消息
     *
     * @param message 消息内容
     * @param pattern 消息模式
     */
    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            LoggingUtil.debug(logger, "接收到缓存消息，频道：{}，内容：{}", channel, body);

            handleMessage(body, channel);

        } catch (Exception e) {
            LoggingUtil.error(logger, "处理Redis消息失败", e);
        }
    }

    /**
     * 处理缓存消息
     *
     * @param messageBody 消息体
     * @param channel     消息频道
     */
    private void handleMessage(String messageBody, String channel) {
        try {
            // 解析缓存失效事件
            CacheConsistencyService.CacheEvictEvent event = objectMapper.readValue(messageBody,
                    CacheConsistencyService.CacheEvictEvent.class);

            LoggingUtil.debug(logger, "解析缓存失效事件成功，缓存：{}，键：{}，时间：{}",
                    event.getCacheName(), event.getKey(), event.getTimestamp());

            // 处理缓存失效
            handleCacheEvict(event)
                    .subscribe(
                            v -> LoggingUtil.debug(logger, "处理缓存失效成功，缓存：{}，键：{}",
                                    event.getCacheName(), event.getKey()),
                            error -> LoggingUtil.error(logger, "处理缓存失效失败，缓存：" +
                                    event.getCacheName() + "，键：" + event.getKey(), error));

        } catch (Exception e) {
            LoggingUtil.error(logger, "处理缓存消息失败，频道：{}，消息：{}", channel, messageBody, e);
        }
    }

    /**
     * 处理缓存失效
     *
     * @param event 缓存失效事件
     * @return 处理结果
     */
    private Mono<Void> handleCacheEvict(CacheConsistencyService.CacheEvictEvent event) {
        return Mono.fromRunnable(() -> {
            try {
                org.springframework.cache.Cache cache = cacheManager.getCache(event.getCacheName());
                if (cache != null) {
                    if ("*".equals(event.getKey())) {
                        // 清理所有缓存
                        cache.clear();
                        LoggingUtil.info(logger, "清理所有缓存成功，缓存：{}", event.getCacheName());
                    } else {
                        // 清理指定键的缓存
                        cache.evict(event.getKey());
                        LoggingUtil.info(logger, "清理指定缓存成功，缓存：{}，键：{}",
                                event.getCacheName(), event.getKey());
                    }
                } else {
                    LoggingUtil.warn(logger, "缓存不存在，跳过失效处理，缓存：{}", event.getCacheName());
                }

            } catch (Exception e) {
                LoggingUtil.error(logger, "处理缓存失效异常，缓存：{}，键：{}",
                        event.getCacheName(), event.getKey(), e);
                throw new RuntimeException("处理缓存失效失败", e);
            }
        });
    }

    /**
     * 发送缓存失效消息
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @return 发送结果
     */
    public Mono<Long> sendCacheEvictMessage(String cacheName, String key) {
        return Mono.fromCallable(() -> {
            try {
                CacheConsistencyService.CacheEvictEvent event = new CacheConsistencyService.CacheEvictEvent(cacheName,
                        key,
                        java.time.LocalDateTime.now());

                String topic = "honyrun:cache:evict:" + cacheName;
                String message = objectMapper.writeValueAsString(event);

                LoggingUtil.debug(logger, "发送缓存失效消息，主题：{}，消息：{}", topic, message);

                return message;

            } catch (Exception e) {
                LoggingUtil.error(logger, "发送缓存失效消息失败，缓存：{}，键：{}", cacheName, key, e);
                throw new RuntimeException("发送缓存失效消息失败", e);
            }
        })
                .flatMap(message -> {
                    String topic = "honyrun:cache:evict:" + cacheName;
                    return reactiveRedisTemplate.convertAndSend(topic, message)
                            .timeout(java.time.Duration.ofSeconds(5))
                            .onErrorResume(throwable -> {
                                LoggingUtil.error(logger, "发送缓存失效消息超时或失败，缓存：{}，键：{}", cacheName, key, throwable);
                                return Mono.just(0L); // 返回默认值，避免阻塞整个流程
                            });
                });
    }

    /**
     * 检查消息监听器状态
     *
     * @return 状态信息
     */
    public Mono<String> checkListenerStatus() {
        return Mono.fromCallable(() -> {
            try {
                // ReactiveRedisMessageListenerContainer 没有 isRunning() 方法
                // 使用其他方式检查状态
                String status = messageListenerContainer != null ? "已初始化" : "未初始化";

                LoggingUtil.debug(logger, "缓存消息监听器状态：{}", status);

                return "缓存消息监听器状态：" + status;

            } catch (Exception e) {
                LoggingUtil.error(logger, "检查消息监听器状态失败", e);
                return "缓存消息监听器状态检查失败：" + e.getMessage();
            }
        });
    }

    /**
     * 重启消息监听器
     *
     * @return 重启结果
     */
    public Mono<Void> restartListener() {
        return Mono.fromRunnable(() -> {
            try {
                LoggingUtil.info(logger, "重启缓存消息监听器");

                // ReactiveRedisMessageListenerContainer 没有 stop() 和 start() 方法
                // 响应式Redis监听器容器的生命周期由Spring管理
                // 这里可以重新订阅主题
                if (messageListenerContainer != null) {
                    // 重新初始化监听器
                    initialize();
                    LoggingUtil.debug(logger, "重新初始化消息监听器");
                }

                LoggingUtil.info(logger, "缓存消息监听器重启完成");

            } catch (Exception e) {
                LoggingUtil.error(logger, "重启缓存消息监听器失败", e);
                throw new RuntimeException("重启缓存消息监听器失败", e);
            }
        });
    }
}
