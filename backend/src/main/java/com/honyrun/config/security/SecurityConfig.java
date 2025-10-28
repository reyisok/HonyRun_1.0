package com.honyrun.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import com.honyrun.config.router.RouteRegistry;
import com.honyrun.config.security.permission.PathPermissionManager;
import com.honyrun.constant.PathConstants;
import com.honyrun.constant.PermissionConstants;
import com.honyrun.constant.SystemConstants;
import com.honyrun.filter.reactive.ReactiveRequestSignatureFilter;
import com.honyrun.filter.reactive.ReactiveRequestValidationFilter;
import com.honyrun.interceptor.ReactiveRequestValidationInterceptor;
import com.honyrun.security.reactive.ReactiveJwtAuthenticationManager;
import com.honyrun.security.reactive.ReactiveSecurityContextRepository;
import com.honyrun.service.reactive.ReactiveRequestSignatureService;
import com.honyrun.service.security.AccessTypeDetectionService;
import com.honyrun.util.ErrorDetailsUtil;
import com.honyrun.util.LoggingUtil;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

/**
 * 安全配置类
 *
 * @author Mr.Rey
 * @version 2.1.0
 * @created 2025-07-01 16:30:00
 * @modified 2025-01-17 16:57:03
 *
 *           Copyright © 2025 HonyRun. All rights reserved.
 *
 *           配置WebFlux响应式安全策略
 *           包括JWT认证、权限控制、公开端点配置等
 *           CORS配置已分离到ReactiveCorsConfig类中
 *
 *           【新增功能】基于访问类型的认证控制：
 *           - 本地访问（127.0.0.1, localhost）：免认证
 *           - 内部访问（内网IP）：需要认证
 *           - 外部访问（公网IP）：需要认证
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final ReactiveJwtAuthenticationManager jwtAuthenticationManager;
    private final ReactiveSecurityContextRepository securityContextRepository;
    private final CorsConfigurationSource corsConfigurationSource;
    private final ReactiveRequestSignatureService requestSignatureService;
    private final RouteRegistry routeRegistry;
    private final PathPermissionManager pathPermissionManager;
    private final AccessTypeDetectionService accessTypeDetectionService;

    /**
     * 构造函数注入依赖
     *
     * @param jwtAuthenticationManager   JWT认证管理器
     * @param securityContextRepository  安全上下文仓库
     * @param corsConfigurationSource    CORS配置源
     * @param requestSignatureService    请求签名服务
     * @param routeRegistry              路由注册表
     * @param pathPermissionManager      路径权限管理器
     * @param accessTypeDetectionService 访问类型检测服务
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-13 16:57:03
     * @modified 2025-01-17 16:57:03
     * @version 1.1.0
     */
    public SecurityConfig(ReactiveJwtAuthenticationManager jwtAuthenticationManager,
            ReactiveSecurityContextRepository securityContextRepository,
            CorsConfigurationSource corsConfigurationSource,
            ReactiveRequestSignatureService requestSignatureService,
            RouteRegistry routeRegistry,
            PathPermissionManager pathPermissionManager,
            AccessTypeDetectionService accessTypeDetectionService) {
        this.jwtAuthenticationManager = jwtAuthenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.corsConfigurationSource = corsConfigurationSource;
        this.requestSignatureService = requestSignatureService;
        this.routeRegistry = routeRegistry;
        this.pathPermissionManager = pathPermissionManager;
        this.accessTypeDetectionService = accessTypeDetectionService;
    }

    /**
     * 初始化路由注册表
     */
    @PostConstruct
    public void initializeRouteRegistry() {
        routeRegistry.initializeRoutes();
        LoggingUtil.info(logger, "路由注册表初始化完成");
    }

    /**
     * 配置请求签名验证过滤器
     *
     * <p>
     * 提供请求签名验证功能，确保请求的完整性和安全性。
     *
     * @param errorDetailsUtil 错误详情工具类
     * @return 请求签名验证过滤器实例
     * @author Mr.Rey Copyright © 2025
     * @since 1.0.0
     * @version 1.0.0
     */
    /**
     * 配置请求签名验证过滤器
     *
     * @author: Mr.Rey Copyright © 2025
     * @created: 2025-07-01 16:57:03
     * @modified: 2025-07-01 16:57:03
     * @version: 1.0.0
     *
     * @param errorDetailsUtil 错误详情工具类
     * @return ReactiveRequestSignatureFilter 签名验证过滤器实例
     */
    @Bean("reactiveRequestSignatureFilter")
    public ReactiveRequestSignatureFilter reactiveRequestSignatureFilter(ErrorDetailsUtil errorDetailsUtil) {
        LoggingUtil.info(logger, "配置请求签名验证过滤器");
        return new ReactiveRequestSignatureFilter(requestSignatureService, errorDetailsUtil);
    }

    /**
     * 配置统一参数校验过滤器
     * 在签名过滤器之后执行，保障安全签名通过后进行参数与安全校验
     *
     * 重要说明：
     * 1. 此过滤器负责统一的请求参数校验和Content-Type处理
     * 2. 在测试环境中同样需要此过滤器来确保Content-Type正确设置
     * 3. 移除了@Profile("!test")限制，确保所有环境都有统一的请求处理逻辑
     * 4. 测试环境的正确性验证依赖于此过滤器的Content-Type处理功能
     * 5. 禁用此过滤器会导致测试中Content-Type为null的问题
     *
     * 注意：此Bean已被移除以避免循环依赖问题
     * 过滤器现在直接在SecurityWebFilterChain中创建
     *
     * @param interceptor      请求校验拦截器
     * @param errorDetailsUtil 错误详情工具类
     * @return ReactiveRequestValidationFilter 请求参数统一校验过滤器
     */
    /*
     * @Bean
     * public ReactiveRequestValidationFilter reactiveRequestValidationFilter(
     *
     * @Qualifier("reactiveRequestValidationInterceptor")
     * ReactiveRequestValidationInterceptor interceptor,
     * ErrorDetailsUtil errorDetailsUtil) {
     * return new ReactiveRequestValidationFilter(interceptor, errorDetailsUtil);
     * }
     */

    /**
     * 配置安全过滤器链
     *
     * <p>
     * 创建和配置WebFlux安全过滤器链，包含以下核心功能：
     * <ul>
     * <li>JWT认证管理 - 基于JWT令牌的用户认证</li>
     * <li>CORS跨域配置 - 支持跨域请求处理</li>
     * <li>路径授权控制 - 基于路径和用户类型的访问控制</li>
     * <li>安全头部配置 - 防止XSS、点击劫持等安全攻击</li>
     * <li>异常处理 - 统一的认证和授权异常处理</li>
     * </ul>
     *
     * <p>
     * <strong>路径授权规则：</strong>
     * <ul>
     * <li>公开端点：认证相关接口、健康检查、静态资源</li>
     * <li>系统管理：需要SYSTEM_USER权限</li>
     * <li>用户资源：需要相应的用户权限和资源所有权检查</li>
     * <li>其他端点：需要基本认证</li>
     * </ul>
     *
     * <p>
     * <strong>安全特性：</strong>
     * <ul>
     * <li>CSRF保护：禁用（JWT架构不需要）</li>
     * <li>内容安全策略：防止XSS攻击</li>
     * <li>框架选项：防止点击劫持攻击</li>
     * <li>MIME类型保护：防止MIME类型嗅探攻击</li>
     * </ul>
     *
     * @param http             ServerHttpSecurity配置对象，用于构建安全过滤器链
     * @param environment      环境配置，用于判断当前运行环境
     * @param interceptor      请求校验拦截器
     * @param errorDetailsUtil 错误详情工具类
     * @return SecurityWebFilterChain 配置完成的安全过滤器链实例
     * @see SecurityWebFilterChain WebFlux安全过滤器链
     * @see ServerHttpSecurity WebFlux安全配置构建器
     * @see ReactiveJwtAuthenticationManager JWT认证管理器
     * @see ReactiveSecurityContextRepository 安全上下文仓库
     * @author Mr.Rey Copyright © 2025
     * @since 1.0.0
     * @version 1.0.0
     */
    @Bean("securityWebFilterChain")
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            Environment environment,
            @Qualifier("reactiveRequestValidationInterceptor") ReactiveRequestValidationInterceptor interceptor,
            @Qualifier("reactiveRequestSignatureFilter") ReactiveRequestSignatureFilter signatureFilter,
            ErrorDetailsUtil errorDetailsUtil) {

        // 创建请求校验过滤器实例
        ReactiveRequestValidationFilter validationFilter = new ReactiveRequestValidationFilter(interceptor,
                errorDetailsUtil);

        LoggingUtil.info(logger, "配置安全过滤器链");
        LoggingUtil.info(logger, "添加签名验证过滤器到安全过滤器链");
        boolean isDev = environment.acceptsProfiles(Profiles.of("dev"));

        SecurityWebFilterChain filterChain = http
                // 禁用CSRF保护（JWT架构不需要CSRF保护）
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // 配置CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // 配置安全头部
                .headers(headers -> headers
                        // 防止点击劫持攻击
                        .frameOptions(frameOptions -> frameOptions
                                .mode(org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode.DENY))
                        // 防止MIME类型嗅探攻击
                        .contentTypeOptions(contentTypeOptions -> {
                        })
                        // 内容安全策略 - 防止XSS攻击
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: blob:; " +
                                        "font-src 'self'; " +
                                        "connect-src 'self'; " +
                                        "media-src 'self'; " +
                                        "object-src 'none'; " +
                                        "frame-src 'none'; " +
                                        "base-uri 'self'; " +
                                        "form-action 'self'"))
                        // HTTP严格传输安全 - 强制HTTPS连接
                        .hsts(hsts -> hsts
                                .maxAge(java.time.Duration.ofDays(365)) // 1年
                                .includeSubdomains(true))
                        // Referrer策略 - 控制引用信息泄露
                        .referrerPolicy(referrer -> referrer
                                .policy(org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))

                // 配置无状态会话管理
                .authenticationManager(jwtAuthenticationManager)
                .securityContextRepository(securityContextRepository)

                // 添加签名验证过滤器到过滤器链（最高优先级）
                .addFilterBefore(signatureFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                // 添加请求校验过滤器到过滤器链
                .addFilterAfter(validationFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                // 配置异常处理
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((exchange, ex) -> {
                            String path = exchange.getRequest().getPath().value();
                            if (isPublicPath(path)) {
                                return exchange.getResponse().setComplete();
                            }
                            if (isNonExistentRoute(path)) {
                                return com.honyrun.security.response.SecurityErrorResponseWriter.writeNotFound(exchange,
                                        "请求的资源不存在");
                            }
                            return com.honyrun.security.response.SecurityErrorResponseWriter.writeUnauthorized(exchange,
                                    "认证失败，请重新登录");
                        })
                        .accessDeniedHandler((exchange, denied) -> {
                            String path = exchange.getRequest().getPath().value();
                            if (isNonExistentRoute(path)) {
                                return com.honyrun.security.response.SecurityErrorResponseWriter.writeNotFound(exchange,
                                        "请求的资源不存在");
                            }
                            return com.honyrun.security.response.SecurityErrorResponseWriter.writeForbidden(exchange,
                                    "权限不足，拒绝访问");
                        }))

                // 配置授权规则 - 基于用户类型的细粒度权限控制
                .authorizeExchange(exchanges -> {
                    // CORS预检请求允许匿名访问 - 必须在所有其他规则之前
                    exchanges.pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll();

                    // 认证API登录允许匿名访问（精确到POST /auth/login）
                    exchanges.pathMatchers(org.springframework.http.HttpMethod.POST, PathConstants.AUTH_LOGIN)
                            .permitAll();
                    // 兼容旧版本登录路径
                    exchanges.pathMatchers(org.springframework.http.HttpMethod.POST, "/api/auth/login").permitAll();
                    // 令牌刷新端点允许匿名访问
                    exchanges.pathMatchers(org.springframework.http.HttpMethod.POST, PathConstants.AUTH_REFRESH)
                            .permitAll();
                    // 兼容旧版本令牌刷新路径
                    exchanges.pathMatchers(org.springframework.http.HttpMethod.POST, "/api/auth/token/refresh")
                            .permitAll();
                    // 令牌校验端点允许匿名访问（如需）
                    exchanges.pathMatchers(PathConstants.AUTH_VALIDATE).permitAll();

                    // 版本信息API需要认证 - 至少需要普通用户权限
                    // 注意：除了登录、认证接口外，任何接口都不应该是公开的！
                    exchanges.pathMatchers(PathConstants.VERSION_BASE + "/**").authenticated();

                    // 健康检查端点 - 基于访问类型的认证控制
                    exchanges.pathMatchers("/actuator/health", "/actuator/health/**")
                            .access(this::hasHealthCheckAccess);
                    exchanges.pathMatchers(PathConstants.SYSTEM_HEALTH, PathConstants.SYSTEM_HEALTH + "/**")
                            .access(this::hasHealthCheckAccess);

                    // dev环境下开放info匿名访问（优先匹配，避免被/actuator/**覆盖）
                    if (isDev) {
                        exchanges.pathMatchers("/actuator/info").permitAll();
                    }

                    // 注意：根据项目规则，除了登录、认证接口外，任何接口都不应该是公开的！
                    // 其他actuator端点需要认证
                    exchanges.pathMatchers("/actuator/**").authenticated();

                    // 系统信息端点允许匿名访问
                    exchanges.pathMatchers(PathConstants.SYSTEM_BASE + "/info").permitAll();

                    // 静态资源允许匿名访问
                    exchanges.pathMatchers(PathConstants.STATIC_BASE + "/**").permitAll();

                    // 测试边界路径（允许通过，由Handler返回404）- 必须在所有用户路径之前
                    exchanges.pathMatchers(PathConstants.USER_BASE + "/empty", PathConstants.USER_BASE + "/boundary")
                            .permitAll();

                    // 个人资料路径 - 需要认证，支持资源所有权检查
                    exchanges.pathMatchers(PathConstants.ACCOUNT_PROFILE, PathConstants.ACCOUNT_PASSWORD)
                            .access(this::hasUserResourcePermission);

                    // 用户管理路径 - 支持资源所有权检查，普通用户只能访问自己的资源，系统用户可访问所有资源
                    exchanges.pathMatchers(PathConstants.USER_BASE, PathConstants.USER_BASE + "/**")
                            .access(this::hasUserResourcePermission);
                    exchanges.pathMatchers(PathConstants.SYSTEM_CONFIG, PathConstants.SYSTEM_CONFIG + "/**")
                            .access(this::hasSystemUserPermission);
                    exchanges.pathMatchers(PathConstants.SYSTEM_SETTINGS, PathConstants.SYSTEM_SETTINGS + "/**")
                            .access(this::hasSystemUserPermission);
                    exchanges.pathMatchers(PathConstants.SYSTEM_LOGS, PathConstants.SYSTEM_LOGS + "/**")
                            .access(this::hasSystemUserPermission);
                    exchanges.pathMatchers(PathConstants.SYSTEM_STATS, PathConstants.SYSTEM_STATS + "/**")
                            .access(this::hasSystemUserPermission);
                    exchanges.pathMatchers(PathConstants.SYSTEM_PERFORMANCE, PathConstants.SYSTEM_PERFORMANCE + "/**")
                            .access(this::hasSystemUserPermission);
                    exchanges.pathMatchers(PathConstants.SYSTEM_MAINTENANCE, PathConstants.SYSTEM_MAINTENANCE + "/**")
                            .access(this::hasSystemUserPermission);

                    // 其他所有请求需要认证（包括登出等）
                    exchanges.anyExchange().authenticated();
                })

                // 禁用表单登录（与JWT无状态架构冲突）
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                // 禁用HTTP Basic认证
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                // 禁用默认登出（JWT使用黑名单机制）
                .logout(ServerHttpSecurity.LogoutSpec::disable)

                .build();

        LoggingUtil.info(logger, "安全过滤器链配置完成");
        return filterChain;
    }

    /**
     * 检查是否为公共路径（不需要认证）
     *
     * @param path 请求路径
     * @return boolean 是否为公共路径
     */
    private boolean isPublicPath(String path) {
        // 使用PathConstants中的公开路径常量进行判定
        for (String p : PathConstants.PUBLIC_PATHS) {
            if (path.equals(p)) {
                return true;
            }
        }

        // 允许常见公开前缀（兼容性与目录资源）
        String[] publicPrefixes = {
                "/api/auth/", // 兼容旧版本认证路径
                PathConstants.SYSTEM_HEALTH,
                "/actuator/health",
                PathConstants.STATIC_BASE + "/",
                "/webjars/",
                "/swagger-ui/",
                "/v3/api-docs",
                "/favicon.ico"
        };

        for (String prefix : publicPrefixes) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查是否为不存在的路由（应返回404）
     *
     * 使用动态路由注册表进行准确的路由存在性检查，
     * 替代静态路由列表，确保与实际路由配置同步。
     *
     * @param path 请求路径
     * @return boolean 是否为不存在的路由
     */
    private boolean isNonExistentRoute(String path) {
        return routeRegistry.isNonExistentRoute(path);
    }

    /**
     * 检查是否为系统用户权限
     *
     * 仅SYSTEM_USER类型的用户可以访问系统管理功能
     *
     * @param authentication 认证信息
     * @param context        授权上下文
     * @return 权限检查结果
     */
    private Mono<AuthorizationDecision> hasSystemUserPermission(Mono<Authentication> authentication,
            AuthorizationContext context) {
        return authentication
                .cast(Authentication.class)
                .map(auth -> {
                    // 检查用户是否已认证
                    if (!auth.isAuthenticated()) {
                        return new AuthorizationDecision(false);
                    }

                    // 检查权限字符串中是否包含系统用户权限
                    boolean isSystemUser = auth.getAuthorities().stream()
                            .anyMatch(authority -> {
                                String authorityStr = authority.getAuthority();
                                return SystemConstants.USER_TYPE_SYSTEM.equals(authorityStr) ||
                                        PermissionConstants.SYSTEM_MANAGEMENT.equals(authorityStr);
                            });

                    return new AuthorizationDecision(isSystemUser);
                })
                .defaultIfEmpty(new AuthorizationDecision(false));
    }

    /**
     * 检查是否具有用户资源权限
     *
     * 支持资源所有权检查，普通用户只能访问自己的资源
     * 系统用户可以访问所有用户资源
     *
     * @param authentication 认证信息
     * @param context        授权上下文
     * @return 权限检查结果
     */
    private Mono<AuthorizationDecision> hasUserResourcePermission(Mono<Authentication> authentication,
            AuthorizationContext context) {
        return authentication
                .cast(Authentication.class)
                .flatMap(auth -> {
                    // 检查用户是否已认证
                    if (!auth.isAuthenticated()) {
                        return Mono.just(new AuthorizationDecision(false));
                    }

                    String path = context.getExchange().getRequest().getPath().value();

                    // 检查是否为系统用户（系统用户拥有所有权限）
                    if (isSystemUser(auth)) {
                        return Mono.just(new AuthorizationDecision(true));
                    }

                    // 使用策略模式检查路径权限
                    return pathPermissionManager.checkPathPermission(path, context);
                })
                .defaultIfEmpty(new AuthorizationDecision(false));
    }

    /**
     * 检查用户是否为系统用户
     *
     * @param auth 认证信息
     * @return 是否为系统用户
     */
    private boolean isSystemUser(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> {
                    String authorityStr = authority.getAuthority();
                    return SystemConstants.USER_TYPE_SYSTEM.equals(authorityStr) ||
                            PermissionConstants.SYSTEM_MANAGEMENT.equals(authorityStr);
                });
    }

    /**
     * 检查健康检查端点的访问权限
     *
     * 访问控制规则：
     * - 本地访问（127.0.0.1, localhost）：允许匿名访问
     * - 内部访问（内网IP）：需要认证
     * - 外部访问（公网IP）：需要认证
     *
     * @param authentication 认证信息
     * @param context        授权上下文
     * @return 权限检查结果
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-17 16:57:03
     * @version 1.0.0
     */
    private Mono<AuthorizationDecision> hasHealthCheckAccess(Mono<Authentication> authentication,
            AuthorizationContext context) {

        return accessTypeDetectionService.detectAccessType(context.getExchange())
                .flatMap(accessType -> {
                    LoggingUtil.info(logger, "健康检查访问控制 - 访问类型: {}", accessType);

                    // 本地访问允许匿名访问
                    if (AccessTypeDetectionService.AccessType.LOCAL.equals(accessType)) {
                        LoggingUtil.info(logger, "本地访问健康检查端点，允许匿名访问");
                        return Mono.just(new AuthorizationDecision(true));
                    }

                    // 内部和外部访问需要认证
                    return authentication
                            .cast(Authentication.class)
                            .map(auth -> {
                                boolean isAuthenticated = auth.isAuthenticated();
                                LoggingUtil.info(logger, "{}访问健康检查端点，认证状态: {}",
                                        accessType.getDescription(), isAuthenticated);
                                return new AuthorizationDecision(isAuthenticated);
                            })
                            .defaultIfEmpty(new AuthorizationDecision(false));
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "健康检查访问控制检查失败", error);
                    // 出错时默认需要认证
                    return authentication
                            .cast(Authentication.class)
                            .map(auth -> new AuthorizationDecision(auth.isAuthenticated()))
                            .defaultIfEmpty(new AuthorizationDecision(false));
                });
    }

}
