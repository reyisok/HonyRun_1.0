package com.honyrun.config;

import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * OpenAPI配置类
 *
 * 配置Swagger/OpenAPI文档生成，提供API文档和测试界面
 * 支持多版本API文档、安全认证、分组管理等功能
 *
 * 访问地址：
 * - Swagger UI: http://localhost:{server.port}/swagger-ui.html
 * - OpenAPI JSON: http://localhost:{server.port}/v3/api-docs
 * - OpenAPI YAML: http://localhost:{server.port}/v3/api-docs.yaml
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 12:40:00
 * @modified 2025-10-26 01:36:12
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Configuration
public class OpenApiConfig {

    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入UnifiedConfigManager
     *
     * @param unifiedConfigManager 统一配置管理器
     */
    public OpenApiConfig(UnifiedConfigManager unifiedConfigManager) {
        this.unifiedConfigManager = unifiedConfigManager;
    }

    /**
     * 配置OpenAPI基本信息
     *
     * <p>
     * 配置OpenAPI的基本信息，包括API标题、描述、版本、联系人信息等。
     * 同时配置JWT认证方案和服务器信息。
     * </p>
     *
     * @return OpenAPI配置实例
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // 使用统一配置管理器获取服务器端口
        String serverPort = unifiedConfigManager.getProperty(
                com.honyrun.constant.SystemConstants.SERVER_PORT_CONFIG_KEY, "8080");
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("本地开发环境"),
                        new Server()
                                .url("https://api.honyrun.com")
                                .description("生产环境")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    /**
     * API基本信息
     */
    private Info apiInfo() {
        return new Info()
                .title("HonyRun响应式业务支撑平台API")
                .description("""
                        基于Spring WebFlux的响应式业务支撑平台API文档

                        ## 功能特性
                        - 响应式编程模型，支持高并发
                        - JWT认证和权限管理
                        - 多版本API支持
                        - 实时监控和指标
                        - 缓存优化和性能调优

                        ## 认证方式
                        使用JWT Bearer Token进行认证，请在请求头中添加：
                        ```
                        Authorization: Bearer <your-jwt-token>
                        ```

                        ## 版本控制
                        支持多种版本控制方式：
                        - URL路径：/api/v1/users, /api/v2/users
                        - 请求头：API-Version: v1
                        - 查询参数：?version=v1
                        """)
                .version("2.0.0")
                .contact(new Contact()
                        .name("Mr.Rey")
                        .email("reyiosk@example.com")
                        .url("https://github.com/reyiosk"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * 创建JWT认证方案
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("请输入JWT Token，格式：Bearer <token>");
    }

    /**
     * 用户管理API分组
     *
     * <p>
     * 包含用户管理和认证相关的API接口分组。
     * </p>
     *
     * @return 用户管理API分组配置
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("userApiGroup")
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户管理")
                .pathsToMatch("/api/**/users/**", "/api/**/auth/**")
                .build();
    }

    /**
     * 系统管理API分组
     *
     * <p>
     * 包含系统管理和设置相关的API接口分组。
     * </p>
     *
     * @return 系统管理API分组配置
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("systemApiGroup")
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("系统管理")
                .pathsToMatch("/api/**/system/**", "/api/**/settings/**")
                .build();
    }

    /**
     * 核验服务API分组
     *
     * <p>
     * 包含核验和验证相关的API接口分组。
     * </p>
     *
     * @return 核验服务API分组配置
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("verificationApiGroup")
    public GroupedOpenApi verificationApi() {
        return GroupedOpenApi.builder()
                .group("核验服务")
                .pathsToMatch("/api/**/verification/**", "/api/**/validate/**")
                .build();
    }

    /**
     * 模拟接口API分组
     *
     * <p>
     * 包含模拟接口相关的API接口分组。
     * </p>
     *
     * @return 模拟接口API分组配置
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("mockApiGroup")
    public GroupedOpenApi mockApi() {
        return GroupedOpenApi.builder()
                .group("模拟接口")
                .pathsToMatch("/api/**/mock/**", "/api/**/interfaces/**")
                .build();
    }

    /**
     * 版本管理API分组
     *
     * <p>
     * 包含版本管理相关的API接口分组。
     * </p>
     *
     * @return 版本管理API分组配置
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("versionApiGroup")
    public GroupedOpenApi versionApi() {
        return GroupedOpenApi.builder()
                .group("版本管理")
                .pathsToMatch("/api/**/version/**", "/api/**/versions/**")
                .build();
    }

    /**
     * 监控和指标API分组
     *
     * <p>
     * 包含监控和指标相关的API接口分组。
     * </p>
     *
     * @return 监控指标API分组配置
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("monitoringApiGroup")
    public GroupedOpenApi monitoringApi() {
        return GroupedOpenApi.builder()
                .group("监控指标")
                .pathsToMatch("/actuator/**")
                .build();
    }

    /**
     * V1版本API分组
     *
     * <p>
     * 包含V1版本的所有API接口分组。
     * </p>
     *
     * @return V1版本API分组配置
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("v1ApiGroup")
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder()
                .group("API v1")
                .pathsToMatch("/api/v1/**")
                .build();
    }

    /**
     * V2版本API分组
     *
     * <p>
     * 包含V2版本的所有API接口分组。
     * </p>
     *
     * @return V2版本API分组配置
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("v2ApiGroup")
    public GroupedOpenApi v2Api() {
        return GroupedOpenApi.builder()
                .group("API v2")
                .pathsToMatch("/api/v2/**")
                .build();
    }

    /**
     * 所有API分组
     *
     * <p>
     * 包含所有API接口的分组。
     * </p>
     *
     * @return 所有API分组配置
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("allApiGroup")
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("所有API")
                .pathsToMatch("/api/**")
                .build();
    }
}
