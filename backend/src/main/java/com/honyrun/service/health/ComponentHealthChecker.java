package com.honyrun.service.health;

import reactor.core.publisher.Mono;

/**
 * 组件健康检查器接口
 * 
 * <p>定义统一的组件健康检查标准，所有组件健康检查器必须实现此接口。
 * 
 * <p><strong>设计原则：</strong>
 * <ul>
 *   <li>响应式编程：返回Mono类型，支持非阻塞操作</li>
 *   <li>超时控制：每个检查器可自定义超时时间</li>
 *   <li>统一接口：提供标准化的健康检查方法</li>
 *   <li>组件标识：明确标识检查的组件名称</li>
 * </ul>
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @version 1.0.0
 */
public interface ComponentHealthChecker {
    
    /**
     * 执行健康检查
     * 
     * <p>检查组件的健康状态，返回布尔值表示是否健康。
     * 实现类应该：
     * <ul>
     *   <li>执行具体的健康检查逻辑</li>
     *   <li>设置合理的超时时间</li>
     *   <li>提供优雅的错误处理</li>
     *   <li>避免阻塞操作</li>
     * </ul>
     * 
     * @return 健康状态的Mono包装，true表示健康，false表示不健康
     */
    Mono<Boolean> checkHealth();
    
    /**
     * 获取组件名称
     * 
     * <p>返回当前健康检查器负责检查的组件名称，用于：
     * <ul>
     *   <li>日志记录和监控</li>
     *   <li>健康状态缓存键</li>
     *   <li>错误报告和告警</li>
     *   <li>统计和分析</li>
     * </ul>
     * 
     * @return 组件名称，建议使用小写字母和连字符格式，如"redis"、"database"
     */
    String getComponentName();
    
    /**
     * 获取检查超时时间(秒)
     * 
     * <p>返回健康检查的超时时间，默认为5秒。
     * 实现类可以根据组件特性重写此方法：
     * <ul>
     *   <li>快速检查组件：1-3秒</li>
     *   <li>标准检查组件：5秒</li>
     *   <li>复杂检查组件：10秒以内</li>
     * </ul>
     * 
     * @return 超时时间(秒)，必须大于0
     */
    default int getTimeoutSeconds() {
        return 5;
    }
    
    /**
     * 获取组件描述信息
     * 
     * <p>返回组件的描述信息，用于健康检查报告和监控界面显示。
     * 
     * @return 组件描述信息
     */
    default String getComponentDescription() {
        return getComponentName() + "组件健康检查";
    }
    
    /**
     * 检查是否启用健康检查
     * 
     * <p>返回当前组件是否启用健康检查，默认启用。
     * 实现类可以根据配置或环境条件动态决定是否启用。
     * 
     * @return true表示启用，false表示禁用
     */
    default boolean isEnabled() {
        return true;
    }
}
