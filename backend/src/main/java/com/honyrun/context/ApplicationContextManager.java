package com.honyrun.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * ApplicationContext管理工具类
 *
 * 提供全局ApplicationContext访问功能，解决项目中上下文管理问题：
 * - 统一ApplicationContext访问入口
 * - 支持Bean动态获取和检查
 * - 提供上下文生命周期管理
 * - 确保上下文初始化的安全性
 *
 * 主要功能：
 * - ApplicationContext的全局访问
 * - Bean存在性检查
 * - Bean实例动态获取
 * - 上下文状态监控
 *
 * 使用场景：
 * - 需要在非Spring管理的类中访问Bean
 * - 动态Bean获取和检查
 * - 测试环境中的上下文管理
 * - 上下文初始化状态验证
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-06-28 10:30:00
 * @modified 2025-06-29 10:30:00
 * @version 1.0.0 - ApplicationContext统一管理
 */
@Component
public class ApplicationContextManager implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextManager.class);

    private static ApplicationContext applicationContext;
    private static volatile boolean initialized = false;

    /**
     * Spring框架回调方法，设置ApplicationContext
     *
     * @param context Spring应用上下文
     * @throws BeansException Bean异常
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        ApplicationContextManager.applicationContext = context;
        ApplicationContextManager.initialized = true;
        logger.info("ApplicationContext已初始化完成");
    }

    /**
     * 监听上下文刷新事件
     *
     * @param event 上下文刷新事件
     */
    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        ApplicationContextManager.applicationContext = event.getApplicationContext();
        ApplicationContextManager.initialized = true;
        logger.info("ApplicationContext刷新完成，Bean总数: {}",
                applicationContext.getBeanDefinitionCount());
    }

    /**
     * 获取ApplicationContext
     *
     * @return 应用上下文
     * @throws IllegalStateException 如果ApplicationContext未初始化
     */
    public static ApplicationContext getApplicationContext() {
        if (!initialized || applicationContext == null) {
            throw new IllegalStateException("ApplicationContext尚未初始化，请确保Spring容器已启动");
        }
        return applicationContext;
    }

    /**
     * 检查ApplicationContext是否已初始化
     *
     * @return 是否已初始化
     */
    public static boolean isInitialized() {
        return initialized && applicationContext != null;
    }

    /**
     * 检查Bean是否存在
     *
     * @param beanName Bean名称
     * @return 是否存在
     */
    public static boolean containsBean(String beanName) {
        if (!isInitialized()) {
            logger.warn("ApplicationContext未初始化，无法检查Bean: {}", beanName);
            return false;
        }
        return applicationContext.containsBean(beanName);
    }

    /**
     * 获取Bean实例（按类型）
     *
     * @param beanClass Bean类型
     * @param <T>       Bean类型泛型
     * @return Bean实例
     * @throws IllegalStateException 如果ApplicationContext未初始化
     * @throws BeansException        如果Bean获取失败
     */
    public static <T> T getBean(Class<T> beanClass) {
        if (!isInitialized()) {
            throw new IllegalStateException("ApplicationContext未初始化，无法获取Bean: " + beanClass.getName());
        }
        try {
            return applicationContext.getBean(beanClass);
        } catch (BeansException e) {
            logger.error("获取Bean失败: {}", beanClass.getName(), e);
            throw e;
        }
    }

    /**
     * 获取Bean实例（按名称）
     *
     * @param beanName Bean名称
     * @return Bean实例
     * @throws IllegalStateException 如果ApplicationContext未初始化
     * @throws BeansException        如果Bean获取失败
     */
    public static Object getBean(String beanName) {
        if (!isInitialized()) {
            throw new IllegalStateException("ApplicationContext未初始化，无法获取Bean: " + beanName);
        }
        try {
            return applicationContext.getBean(beanName);
        } catch (BeansException e) {
            logger.error("获取Bean失败: {}", beanName, e);
            throw e;
        }
    }

    /**
     * 获取Bean实例（按名称和类型）
     *
     * @param beanName  Bean名称
     * @param beanClass Bean类型
     * @param <T>       Bean类型泛型
     * @return Bean实例
     * @throws IllegalStateException 如果ApplicationContext未初始化
     * @throws BeansException        如果Bean获取失败
     */
    public static <T> T getBean(String beanName, Class<T> beanClass) {
        if (!isInitialized()) {
            throw new IllegalStateException("ApplicationContext未初始化，无法获取Bean: " + beanName);
        }
        try {
            return applicationContext.getBean(beanName, beanClass);
        } catch (BeansException e) {
            logger.error("获取Bean失败: {} ({})", beanName, beanClass.getName(), e);
            throw e;
        }
    }

    /**
     * 获取指定类型的所有Bean
     *
     * @param beanClass Bean类型
     * @param <T>       Bean类型泛型
     * @return Bean实例映射（名称 -> 实例）
     * @throws IllegalStateException 如果ApplicationContext未初始化
     */
    public static <T> java.util.Map<String, T> getBeansOfType(Class<T> beanClass) {
        if (!isInitialized()) {
            throw new IllegalStateException("ApplicationContext未初始化，无法获取Bean类型: " + beanClass.getName());
        }
        return applicationContext.getBeansOfType(beanClass);
    }

    /**
     * 获取Bean定义总数
     *
     * @return Bean定义总数
     */
    public static int getBeanDefinitionCount() {
        if (!isInitialized()) {
            return 0;
        }
        return applicationContext.getBeanDefinitionCount();
    }

    /**
     * 获取所有Bean定义名称
     *
     * @return Bean定义名称数组
     */
    public static String[] getBeanDefinitionNames() {
        if (!isInitialized()) {
            return new String[0];
        }
        return applicationContext.getBeanDefinitionNames();
    }

    /**
     * 清理ApplicationContext（主要用于测试环境）
     *
     * 注意：此方法仅应在测试环境或特殊场景下使用
     */
    public static void clear() {
        applicationContext = null;
        initialized = false;
        logger.info("ApplicationContext已清理");
    }
}


