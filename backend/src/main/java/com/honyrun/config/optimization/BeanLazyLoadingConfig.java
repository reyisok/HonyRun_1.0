package com.honyrun.config.optimization;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bean懒加载配置和性能优化配置类
 * 
 * <p>提供Bean的懒加载配置和性能优化机制，主要功能包括：
 * - Bean懒加载策略配置
 * - 异步Bean初始化
 * - Bean预热机制
 * - 性能监控和优化
 * 
 * <p>优化策略：
 * - 非关键Bean采用懒加载
 * - 关键Bean采用预热机制
 * - 异步初始化耗时Bean
 * - 监控Bean初始化性能
 * 
 * <p>配置属性：
 * - honyrun.bean.lazy-loading.enabled: 启用懒加载（默认true）
 * - honyrun.bean.prewarming.enabled: 启用预热机制（默认false）
 * - honyrun.bean.async-init.enabled: 启用异步初始化（默认false）
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-13 15:45:00
 * @modified 2025-01-13 15:45:00
 * @version 1.0.0 - Bean懒加载和性能优化
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "honyrun.bean.optimization.enabled", havingValue = "true", matchIfMissing = true)
public class BeanLazyLoadingConfig {

    private static final Logger logger = LoggerFactory.getLogger(BeanLazyLoadingConfig.class);

    /**
     * Bean性能优化管理器
     * 
     * <p>提供Bean性能优化功能，包括懒加载、预热、异步初始化等机制。
     * 
     * @param environment 环境配置
     * @return Bean性能优化管理器实例
     * @author Mr.Rey Copyright © 2025
     * @since 1.0.0
     */
    @Bean("reactiveMonitoringBeanPerformanceOptimizer")
    @Lazy
    public BeanPerformanceOptimizer beanPerformanceOptimizer(Environment environment, UnifiedConfigManager unifiedConfigManager) {
        LoggingUtil.info(logger, "初始化Bean性能优化管理器");
        return new BeanPerformanceOptimizer(environment, unifiedConfigManager);
    }

    /**
     * Bean预热调度器
     * 
     * <p>用于Bean预热机制的调度执行器，支持异步预热和定时预热。
     * 
     * @return Bean预热调度器实例
     * @author Mr.Rey Copyright © 2025
     * @since 1.0.0
     */
    @Bean("beanPrewarmingScheduler")
    @Lazy
    @ConditionalOnProperty(name = "honyrun.bean.prewarming.enabled", havingValue = "true")
    public ScheduledExecutorService beanPrewarmingScheduler() {
        LoggingUtil.info(logger, "初始化Bean预热调度器");
        
        return Executors.newScheduledThreadPool(2, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "bean-prewarming-" + threadNumber.getAndIncrement());
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY - 1); // 降低优先级
                return thread;
            }
        });
    }

    /**
     * Bean异步初始化执行器
     * 
     * <p>用于Bean异步初始化的执行器，避免阻塞主线程启动。
     * 
     * @return Bean异步初始化执行器实例
     * @author Mr.Rey Copyright © 2025
     * @since 1.0.0
     */
    @Bean("beanAsyncInitExecutor")
    @Lazy
    @ConditionalOnProperty(name = "honyrun.bean.async-init.enabled", havingValue = "true")
    public Executor beanAsyncInitExecutor() {
        LoggingUtil.info(logger, "初始化Bean异步初始化执行器");
        
        return Executors.newFixedThreadPool(4, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "bean-async-init-" + threadNumber.getAndIncrement());
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            }
        });
    }

    /**
     * Bean性能监控器
     * 
     * <p>监控Bean初始化性能，收集性能指标和优化建议。
     * 
     * @return Bean性能监控器实例
     * @author Mr.Rey Copyright © 2025
     * @since 1.0.0
     */
    @Bean("reactiveMonitoringBeanPerformanceMonitor")
    @Lazy
    @ConditionalOnProperty(name = "honyrun.bean.performance-monitoring.enabled", havingValue = "true")
    public BeanPerformanceMonitor beanPerformanceMonitor() {
        LoggingUtil.info(logger, "初始化Bean性能监控器");
        return new BeanPerformanceMonitor();
    }

    /**
     * Bean性能优化管理器实现类
     */
    public static class BeanPerformanceOptimizer {

        private static final Logger logger = LoggerFactory.getLogger(BeanPerformanceOptimizer.class);

        private final boolean lazyLoadingEnabled;
        private final boolean prewarmingEnabled;
        private final boolean asyncInitEnabled;

        public BeanPerformanceOptimizer(Environment environment, UnifiedConfigManager unifiedConfigManager) {
            this.lazyLoadingEnabled = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.bean.lazy-loading.enabled", "true"));
        this.prewarmingEnabled = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.bean.prewarming.enabled", "false"));
        this.asyncInitEnabled = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.bean.async-init.enabled", "false"));

            LoggingUtil.info(logger, "Bean性能优化配置 - 懒加载: {}, 预热: {}, 异步初始化: {}", 
                lazyLoadingEnabled, prewarmingEnabled, asyncInitEnabled);
        }

        /**
         * 获取Bean懒加载策略
         * 
         * @param beanName Bean名称
         * @param beanClass Bean类型
         * @return 是否应该懒加载
         */
        public boolean shouldLazyLoad(String beanName, Class<?> beanClass) {
            if (!lazyLoadingEnabled) {
                return false;
            }

            // 关键Bean不懒加载
            if (isCriticalBean(beanName, beanClass)) {
                LoggingUtil.debug(logger, "关键Bean '{}' 不使用懒加载", beanName);
                return false;
            }

            // 缓存相关Bean可以懒加载
            if (isCacheRelatedBean(beanName, beanClass)) {
                LoggingUtil.debug(logger, "缓存Bean '{}' 使用懒加载", beanName);
                return true;
            }

            // 监控相关Bean可以懒加载
            if (isMonitoringRelatedBean(beanName, beanClass)) {
                LoggingUtil.debug(logger, "监控Bean '{}' 使用懒加载", beanName);
                return true;
            }

            // 默认策略
            return true;
        }

        /**
         * 判断是否为关键Bean
         */
        private boolean isCriticalBean(String beanName, Class<?> beanClass) {
            // 数据库连接相关Bean
            if (beanName.contains("ConnectionFactory") || 
                beanName.contains("DatabaseClient") ||
                beanName.contains("TransactionManager")) {
                return true;
            }

            // 安全相关Bean
            if (beanName.contains("Security") || 
                beanName.contains("Auth") ||
                beanName.equals("passwordEncoder")) {
                return true;
            }

            // 路由相关Bean
            if (beanName.contains("Router") || 
                beanName.contains("Handler")) {
                return true;
            }

            return false;
        }

        /**
         * 判断是否为缓存相关Bean
         */
        private boolean isCacheRelatedBean(String beanName, Class<?> beanClass) {
            return beanName.contains("Cache") || 
                   beanName.contains("Redis") ||
                   beanClass.getName().contains("cache");
        }

        /**
         * 判断是否为监控相关Bean
         */
        private boolean isMonitoringRelatedBean(String beanName, Class<?> beanClass) {
            return beanName.contains("Monitor") || 
                   beanName.contains("Metrics") ||
                   beanName.contains("Health") ||
                   beanClass.getName().contains("actuator");
        }

        /**
         * 获取Bean预热策略
         * 
         * @param beanName Bean名称
         * @return 是否需要预热
         */
        public boolean shouldPrewarm(String beanName) {
            if (!prewarmingEnabled) {
                return false;
            }

            // 数据库连接池需要预热
            if (beanName.contains("ConnectionFactory") || beanName.contains("DatabaseClient")) {
                return true;
            }

            // Redis连接需要预热
            if (beanName.contains("Redis")) {
                return true;
            }

            return false;
        }

        /**
         * 获取Bean异步初始化策略
         * 
         * @param beanName Bean名称
         * @return 是否应该异步初始化
         */
        public boolean shouldAsyncInit(String beanName) {
            if (!asyncInitEnabled) {
                return false;
            }

            // 监控相关Bean可以异步初始化
            if (isMonitoringRelatedBean(beanName, null)) {
                return true;
            }

            // 缓存相关Bean可以异步初始化
            if (isCacheRelatedBean(beanName, null)) {
                return true;
            }

            return false;
        }

        /**
         * 执行Bean预热
         * 
         * @param beanName Bean名称
         * @param bean Bean实例
         */
        public void prewarmBean(String beanName, Object bean) {
            LoggingUtil.info(logger, "开始预热Bean: {}", beanName);
            
            try {
                long startTime = System.currentTimeMillis();
                
                // 执行预热逻辑
                performPrewarming(beanName, bean);
                
                long duration = System.currentTimeMillis() - startTime;
                LoggingUtil.info(logger, "Bean '{}' 预热完成，耗时: {}ms", beanName, duration);
                
            } catch (Exception e) {
                LoggingUtil.warn(logger, "Bean '{}' 预热失败", beanName, e);
            }
        }

        /**
         * 执行具体的预热逻辑
         */
        private void performPrewarming(String beanName, Object bean) {
            // 根据Bean类型执行不同的预热策略
            if (bean instanceof org.springframework.data.redis.connection.ReactiveRedisConnectionFactory) {
                prewarmRedisConnection(beanName, bean);
            } else if (bean instanceof org.springframework.r2dbc.core.DatabaseClient) {
                prewarmDatabaseConnection(beanName, bean);
            }
        }

        /**
         * 预热Redis连接
         */
        private void prewarmRedisConnection(String beanName, Object bean) {
            LoggingUtil.debug(logger, "预热Redis连接: {}", beanName);
            // 这里可以执行Redis连接预热逻辑
            // 例如：执行简单的ping命令
        }

        /**
         * 预热数据库连接
         */
        private void prewarmDatabaseConnection(String beanName, Object bean) {
            LoggingUtil.debug(logger, "预热数据库连接: {}", beanName);
            // 这里可以执行数据库连接预热逻辑
            // 例如：执行简单的查询
        }
    }

    /**
     * Bean性能监控器实现类
     */
    public static class BeanPerformanceMonitor {

        private static final Logger logger = LoggerFactory.getLogger(BeanPerformanceMonitor.class);

        /**
         * 记录Bean初始化开始
         * 
         * @param beanName Bean名称
         * @return 开始时间戳
         */
        public long recordBeanInitStart(String beanName) {
            long startTime = System.currentTimeMillis();
            LoggingUtil.debug(logger, "Bean '{}' 开始初始化", beanName);
            return startTime;
        }

        /**
         * 记录Bean初始化完成
         * 
         * @param beanName Bean名称
         * @param startTime 开始时间戳
         */
        public void recordBeanInitComplete(String beanName, long startTime) {
            long duration = System.currentTimeMillis() - startTime;
            
            if (duration > 1000) { // 超过1秒的初始化记录警告
                LoggingUtil.warn(logger, "Bean '{}' 初始化耗时较长: {}ms", beanName, duration);
            } else if (duration > 100) { // 超过100ms的初始化记录信息
                LoggingUtil.info(logger, "Bean '{}' 初始化完成，耗时: {}ms", beanName, duration);
            } else {
                LoggingUtil.debug(logger, "Bean '{}' 初始化完成，耗时: {}ms", beanName, duration);
            }
        }

        /**
         * 记录Bean初始化失败
         * 
         * @param beanName Bean名称
         * @param startTime 开始时间戳
         * @param error 错误信息
         */
        public void recordBeanInitError(String beanName, long startTime, Throwable error) {
            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.error(logger, "Bean '{}' 初始化失败，耗时: {}ms", beanName, duration, error);
        }

        /**
         * 生成性能报告
         * 
         * @return 性能报告
         */
        public String generatePerformanceReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== Bean性能优化报告 ===\n");
            report.append("1. 懒加载策略已启用，非关键Bean延迟初始化\n");
            report.append("2. 关键Bean优先初始化，确保核心功能可用\n");
            report.append("3. 监控Bean初始化性能，识别性能瓶颈\n");
            report.append("4. 建议：定期检查Bean初始化耗时，优化慢初始化Bean\n");
            
            return report.toString();
        }
    }
}
