package com.honyrun.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;

/**
 * API版本管理配置类
 *
 * 提供API版本控制机制，支持向后兼容和版本路由
 * 支持通过URL路径、请求头、查询参数等方式进行版本控制
 *
 * 版本控制策略：
 * - URL路径版本控制：/api/v1/users, /api/v2/users
 * - 请求头版本控制：Accept: application/vnd.honyrun.v1+json
 * - 查询参数版本控制：?version=v1
 * - 默认版本：v1（向后兼容）
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 12:15:00
 * @modified 2025-07-01 12:15:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Configuration
public class ApiVersionConfig {

    /**
     * 当前支持的API版本
     */
    public static final String CURRENT_VERSION = "v1";
    public static final String NEXT_VERSION = "v2";
    
    /**
     * 默认API版本（用于向后兼容）
     */
    public static final String DEFAULT_VERSION = CURRENT_VERSION;
    
    /**
     * API版本前缀
     */
    public static final String API_VERSION_PREFIX = "/api";
    
    /**
     * 版本请求头名称
     */
    public static final String VERSION_HEADER = "API-Version";
    
    /**
     * 版本查询参数名称
     */
    public static final String VERSION_PARAM = "version";

    /**
     * V1版本路径匹配器
     * 
     * <p>用于匹配V1版本的API路径请求。</p>
     * 
     * @return V1版本路径匹配器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("v1PathPredicate")
    public RequestPredicate v1PathPredicate() {
        return RequestPredicates.path(API_VERSION_PREFIX + "/v1/**");
    }

    /**
     * V2版本路径匹配器
     * 
     * <p>用于匹配V2版本的API路径请求。</p>
     * 
     * @return V2版本路径匹配器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("v2PathPredicate")
    public RequestPredicate v2PathPredicate() {
        return RequestPredicates.path(API_VERSION_PREFIX + "/v2/**");
    }

    /**
     * 默认版本路径匹配器（无版本号时使用默认版本）
     * 
     * <p>用于匹配没有明确版本号的API路径请求，使用默认版本。</p>
     * 
     * @return 默认版本路径匹配器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("defaultVersionPredicate")
    public RequestPredicate defaultVersionPredicate() {
        return RequestPredicates.path(API_VERSION_PREFIX + "/**")
                .and(RequestPredicates.path(API_VERSION_PREFIX + "/v*/**").negate());
    }

    /**
     * V1版本请求头匹配器
     * 
     * <p>用于匹配通过请求头指定V1版本的API请求。</p>
     * 
     * @return V1版本请求头匹配器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("v1HeaderPredicate")
    public RequestPredicate v1HeaderPredicate() {
        return RequestPredicates.headers(headers -> 
            "v1".equals(headers.firstHeader(VERSION_HEADER)) ||
            "application/vnd.honyrun.v1+json".equals(headers.firstHeader("Accept"))
        );
    }

    /**
     * V2版本请求头匹配器
     * 
     * <p>用于匹配通过请求头指定V2版本的API请求。</p>
     * 
     * @return V2版本请求头匹配器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("v2HeaderPredicate")
    public RequestPredicate v2HeaderPredicate() {
        return RequestPredicates.headers(headers -> 
            "v2".equals(headers.firstHeader(VERSION_HEADER)) ||
            "application/vnd.honyrun.v2+json".equals(headers.firstHeader("Accept"))
        );
    }

    /**
     * 版本查询参数匹配器
     * 
     * <p>用于匹配通过查询参数指定版本的API请求。</p>
     * 
     * @return 版本查询参数匹配器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("versionParamPredicate")
    public RequestPredicate versionParamPredicate() {
        return RequestPredicates.queryParam(VERSION_PARAM, DEFAULT_VERSION::equals);
    }

    /**
     * 获取API版本信息
     */
    public static class ApiVersionInfo {
        private final String version;
        private final String description;
        private final boolean deprecated;
        private final String deprecationMessage;

        public ApiVersionInfo(String version, String description, boolean deprecated, String deprecationMessage) {
            this.version = version;
            this.description = description;
            this.deprecated = deprecated;
            this.deprecationMessage = deprecationMessage;
        }

        public String getVersion() {
            return version;
        }

        public String getDescription() {
            return description;
        }

        public boolean isDeprecated() {
            return deprecated;
        }

        public String getDeprecationMessage() {
            return deprecationMessage;
        }
    }

    /**
     * 获取支持的API版本列表
     * 
     * <p>返回系统支持的所有API版本信息，包括版本号、描述和弃用状态。</p>
     * 
     * @return 支持的API版本信息数组
     * @author Mr.Rey Copyright © 2025
     * @created 2025-07-02 10:00:00
     * @version 1.0.0
     */
    @Bean("supportedApiVersions")
    public ApiVersionInfo[] supportedVersions() {
        return new ApiVersionInfo[]{
            new ApiVersionInfo("v1", "Initial API version", false, null),
            new ApiVersionInfo("v2", "Enhanced API version with additional features", false, null)
        };
    }
}


