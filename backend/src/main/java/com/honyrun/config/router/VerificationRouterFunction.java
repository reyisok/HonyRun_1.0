package com.honyrun.config.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.honyrun.handler.VerificationHandler;
import com.honyrun.util.LoggingUtil;

/**
 * 验证请求函数式路由配置
 *
 * 使用函数式编程模型配置验证相关的路由映射
 * 提供验证请求的CRUD操作、统计查询等功能的路由定义
 *
 * 【统一规则】根据HonyRun后端统一接口检查报告要求：
 * 1. 统一使用ApiResponse<T>格式返回响应，移除自定义ErrorResponse类
 * 2. 所有异常处理统一使用ReactiveGlobalExceptionFilter
 * 3. 遵循Spring Boot 3最佳实践和响应式编程规范
 * 4. 确保响应格式的一致性和标准化
 * 5. 路由优先级：静态路径优先于动态路径
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-01-15 当前时间
 * @modified 2025-01-15 当前时间
 * @version 2.0.0 - 验证请求函数式路由配置
 */
@Configuration
public class VerificationRouterFunction {

    private static final Logger logger = LoggerFactory.getLogger(VerificationRouterFunction.class);

    private final VerificationHandler verificationHandler;

    /**
     * 构造函数注入依赖
     *
     * @param verificationHandler 验证处理器
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public VerificationRouterFunction(VerificationHandler verificationHandler) {
        this.verificationHandler = verificationHandler;
    }

    /**
     * 配置验证相关的函数式路由
     *
     * 路由优先级规则：
     * 1. 静态路径优先于动态路径
     * 2. 具体路径优先于通配符路径
     * 3. 按照业务逻辑分组组织
     *
     * @return 验证路由函数
     */
    @Bean("devVerificationRoutes")
    @Profile("dev")
    public RouterFunction<ServerResponse> verificationRoutes() {
        LoggingUtil.info(logger, "配置验证请求函数式路由");

        return RouterFunctions.route()
                // 验证请求统计路由 - 静态路径优先
                .GET("/api/v1/verification/statistics",
                        accept(MediaType.APPLICATION_JSON),
                        verificationHandler::getVerificationStats)

                // 验证请求列表查询 - 静态路径
                .GET("/api/v1/verification",
                        accept(MediaType.APPLICATION_JSON),
                        verificationHandler::getVerifications)

                // 根据ID获取验证请求 - 动态路径
                .GET("/api/v1/verification/{id}",
                        accept(MediaType.APPLICATION_JSON),
                        verificationHandler::getVerificationById)

                // 创建验证请求
                .POST("/api/v1/verification",
                        accept(MediaType.APPLICATION_JSON)
                                .and(contentType(MediaType.APPLICATION_JSON)),
                        verificationHandler::createVerification)

                // 更新验证请求
                .PUT("/api/v1/verification/{id}",
                        accept(MediaType.APPLICATION_JSON)
                                .and(contentType(MediaType.APPLICATION_JSON)),
                        verificationHandler::updateVerification)

                // 删除验证请求
                .DELETE("/api/v1/verification/{id}",
                        accept(MediaType.APPLICATION_JSON),
                        verificationHandler::deleteVerification)

                // 完成验证处理
                .POST("/api/v1/verification/{requestId}/complete",
                        accept(MediaType.APPLICATION_JSON),
                        verificationHandler::completeVerification)

                // 取消验证请求
                .POST("/api/v1/verification/{requestId}/cancel",
                        accept(MediaType.APPLICATION_JSON),
                        verificationHandler::cancelVerification)

                .build();
    }

    /**
     * 配置测试环境验证相关的函数式路由
     *
     * @return 验证路由函数
     */
    @Bean("testVerificationRoutes")
    @Profile("test")
    public RouterFunction<ServerResponse> testVerificationRoutes() {
        LoggingUtil.info(logger, "配置测试环境验证请求函数式路由");

        return RouterFunctions.route()
                // 验证请求统计路由 - 静态路径优先
                .GET("/api/v1/verification/statistics",
                        accept(MediaType.APPLICATION_JSON),
                        verificationHandler::getVerificationStats)

                // 验证请求列表查询 - 静态路径
                .GET("/api/v1/verification",
                        accept(MediaType.APPLICATION_JSON),
                        verificationHandler::getVerifications)

                // 根据ID获取验证请求 - 动态路径
                .GET("/api/v1/verification/{id}",
                        accept(MediaType.APPLICATION_JSON),
                        verificationHandler::getVerificationById)

                // 创建验证请求
                .POST("/api/v1/verification",
                        accept(MediaType.APPLICATION_JSON)
                                .and(contentType(MediaType.APPLICATION_JSON)),
                        verificationHandler::createVerification)

                // 更新验证请求
                .PUT("/api/v1/verification/{id}",
                        accept(MediaType.APPLICATION_JSON)
                                .and(contentType(MediaType.APPLICATION_JSON)),
                        verificationHandler::updateVerification)

                // 删除验证请求
                .DELETE("/api/v1/verification/{id}",
                        accept(MediaType.APPLICATION_JSON),
                        verificationHandler::deleteVerification)

                // 完成验证处理
                .POST("/api/v1/verification/{requestId}/complete",
                        accept(MediaType.APPLICATION_JSON),
                        verificationHandler::completeVerification)

                // 取消验证请求
                .POST("/api/v1/verification/{requestId}/cancel",
                        accept(MediaType.APPLICATION_JSON),
                        verificationHandler::cancelVerification)

                .build();
    }

    /**
     * 配置验证扩展路由
     *
     * @return 验证扩展路由函数
     */
    @Bean("devVerificationExtendedRoutes")
    @Profile("dev")
    public RouterFunction<ServerResponse> verificationExtendedRoutes() {
        LoggingUtil.info(logger, "配置验证扩展路由");

        return RouterFunctions.route()
                // 验证结果查询
                .GET("/api/v1/verification/{id}/result",
                        accept(MediaType.APPLICATION_JSON),
                        verificationHandler::getVerificationResult)

                // 删除验证请求
                .DELETE("/api/v1/verification/{id}",
                        verificationHandler::deleteVerification)

                .build();
    }
}
