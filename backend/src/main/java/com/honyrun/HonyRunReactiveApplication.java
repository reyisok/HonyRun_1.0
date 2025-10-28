package com.honyrun;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.honyrun.config.reactive.BannerConfig;

/**
 * HonyRun响应式应用主启动类
 *
 * <p>
 * <strong>【重要】MySQL和Redis为项目必备组件</strong>
 * </p>
 * <ul>
 * <li><strong>MySQL数据库</strong> - 项目核心数据存储，必须正常连接</li>
 * <li><strong>Redis缓存</strong> - 项目核心缓存服务，必须正常连接</li>
 * <li><strong>启动检测</strong> - 应用启动时自动检测MySQL和Redis连接状态</li>
 * <li><strong>失败终止</strong> - 任一组件连接失败将立即终止应用启动</li>
 * </ul>
 *
 * <p>
 * <strong>必备组件配置要求：</strong>
 * <ul>
 * <li>MySQL: 端口8906, 用户honyrunMysql, 密码honyrun@sys, 数据库honyrundb</li>
 * <li>Redis: 端口8902, 密码honyrun@sys, 数据库DB1</li>
 * <li>连接检测: 启动时执行SELECT 1和PING命令验证连接</li>
 * <li>超时设置: 每个组件连接检测最大等待10秒</li>
 * </ul>
 *
 * @author Mr.Rey
 * @version 2.2.0
 * @created 2025-07-01 16:00:00
 * @modified 2025-10-27 12:26:43
 *
 *           Copyright © 2025 HonyRun. All rights reserved.
 *
 *           基于Spring WebFlux的响应式架构主启动类
 *           启用响应式Web、R2DBC数据访问、审计功能等核心特性
 *
 *           集成EssentialComponentsValidator确保MySQL和Redis必备组件正常运行
 */
@SpringBootApplication(exclude = {
        // 保留基础Redis配置，仅排除Repository配置以避免与R2DBC Repository冲突
        org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class
})
@EnableScheduling
@ComponentScan(basePackages = {
        "com.honyrun.config",
        "com.honyrun.config.startup", // 明确包含startup包，确保EssentialComponentsValidator被扫描
        "com.honyrun.config.cache", // 添加缓存配置包，确保TestProfileCacheConfig被扫描
        "com.honyrun.config.repository", // 添加repository配置包，确保分层Repository配置被扫描
        "com.honyrun.service",
        "com.honyrun.service.startup", // 添加service.startup包，确保UnifiedStartupManager被扫描
        "com.honyrun.controller",
        "com.honyrun.handler",
        "com.honyrun.scheduler",
        "com.honyrun.security",
        "com.honyrun.util",
        "com.honyrun.interceptor",
        "com.honyrun.listener"
// Repository包通过独立的配置类管理，实现分层Repository模式
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.honyrun\\.repository\\..*") // 排除所有repository包的直接扫描
})
public class HonyRunReactiveApplication {

    /**
     * 应用程序主入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        BannerConfig.displayCustomBanner();
        SpringApplication application = new SpringApplication(HonyRunReactiveApplication.class);
        // 关闭Spring Boot默认Banner
        application.setBannerMode(Banner.Mode.OFF);

        application.run(args);
    }
}
