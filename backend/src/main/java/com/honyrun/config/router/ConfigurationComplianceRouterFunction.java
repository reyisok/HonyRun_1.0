package com.honyrun.config.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.honyrun.config.validation.ConfigurationComplianceHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * 配置合规性检查路由函数
 *
 * <p>
 * 定义配置管理规范符合性检查相关的路由配置
 * </p>
 *
 * <p>
 * <strong>路由映射：</strong>
 * </p>
 * <ul>
 * <li>GET /api/config/compliance/validate - 执行配置一致性验证</li>
 * <li>GET /api/config/compliance/report - 生成配置合规性检查报告</li>
 * <li>GET /api/config/compliance/summary - 获取配置验证摘要</li>
 * </ul>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-27 07:42:30
 * @version 1.0.0
 */
@Configuration
public class ConfigurationComplianceRouterFunction {

    private final ConfigurationComplianceHandler configurationComplianceHandler;

    /**
     * 构造函数注入
     *
     * @param configurationComplianceHandler 配置合规性检查处理器
     */
    public ConfigurationComplianceRouterFunction(ConfigurationComplianceHandler configurationComplianceHandler) {
        this.configurationComplianceHandler = configurationComplianceHandler;
    }

    /**
     * 配置合规性检查路由
     *
     * @return 路由函数
     */
    @Bean
    public RouterFunction<ServerResponse> configurationComplianceRoutes() {
        return RouterFunctions.route()
                .path("/api/config/compliance", builder -> builder
                        .GET("/validate", configurationComplianceHandler::validateConfiguration)
                        .GET("/report", configurationComplianceHandler::generateComplianceReport)
                        .GET("/summary", configurationComplianceHandler::getValidationSummary))
                .build();
    }
}
