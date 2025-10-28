package com.honyrun.handler;

import com.honyrun.exception.AuthenticationException;
import com.honyrun.exception.ValidationException;
import com.honyrun.exception.ErrorCode;
import com.honyrun.model.dto.request.AuthRequest;
import com.honyrun.model.dto.response.AuthResponse;
import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.model.entity.business.User;
import com.honyrun.repository.r2dbc.ReactiveUserRepository;
import com.honyrun.security.jwt.ReactiveJwtTokenProvider;
import com.honyrun.security.jwt.ReactiveTokenRefreshService;
import com.honyrun.service.reactive.ReactiveTokenBlacklistService;
import com.honyrun.service.reactive.ReactiveActivityManager;
import com.honyrun.service.reactive.ReactiveAuthService;
import com.honyrun.service.reactive.ReactiveUserService;
import com.honyrun.service.reactive.ReactiveAuditLogService;
import com.honyrun.util.validation.ReactiveValidator;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.TraceIdUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.codec.DecodingException;
import org.springframework.web.server.ServerWebInputException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import com.honyrun.config.properties.SecurityDetectionProperties;
import java.util.List;


/**
 * 认证处理器
 *
 * 基于Spring WebFlux的函数式编程模型，提供用户认证功能的非阻塞处理。
 * 支持用户登录、登出、令牌刷新、用户信息获取等认证相关操作。
 *
 * 【统一规则】根据HonyRun后端统一接口检查报告要求：
 * 1. 统一使用ApiResponse<T>格式返回响应，移除自定义ErrorResponse和SuccessResponse
 * 2. 所有异常处理统一使用ReactiveGlobalExceptionFilter
 * 3. 遵循Spring Boot 3最佳实践和响应式编程规范
 * 4. 确保响应格式的一致性和标准化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 08:30:00
 * @modified 2025-07-02 优化Bean命名规范
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component("reactiveAuthHandler")
public class AuthHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);

    private final ReactiveUserRepository userRepository;
    private final ReactiveJwtTokenProvider jwtTokenProvider;
    private final ReactiveTokenRefreshService tokenRefreshService;
    private final ReactiveTokenBlacklistService tokenBlacklistService;
    private final ReactiveActivityManager activityManager;
    private final ReactiveAuthService authService;
    private final ReactiveUserService userService;
    private final ReactiveAuditLogService auditLogService;
    @SuppressWarnings("unused")
    private final PasswordEncoder passwordEncoder;
    private final ReactiveValidator reactiveValidator;
    private final SecurityDetectionProperties securityDetectionProperties;

    /**
     * 构造函数注入
     *
     * @param userRepository 用户仓库
     * @param jwtTokenProvider JWT令牌提供者
     * @param tokenRefreshService 令牌刷新服务
     * @param tokenBlacklistService 令牌黑名单服务
     * @param activityManager 活动管理器
     * @param authService 认证服务
     * @param userService 用户服务
     * @param auditLogService 审计日志服务
     * @param passwordEncoder 密码编码器
     * @param reactiveValidator 响应式验证器
     * @param securityDetectionProperties 安全检测配置
     */
    public AuthHandler(ReactiveUserRepository userRepository,
                       ReactiveJwtTokenProvider jwtTokenProvider,
                       ReactiveTokenRefreshService tokenRefreshService,
                       ReactiveTokenBlacklistService tokenBlacklistService,
                       ReactiveActivityManager activityManager,
                       ReactiveAuthService authService,
                       ReactiveUserService userService,
                       ReactiveAuditLogService auditLogService,
                       PasswordEncoder passwordEncoder,
                       ReactiveValidator reactiveValidator,
                       SecurityDetectionProperties securityDetectionProperties) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenRefreshService = tokenRefreshService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.activityManager = activityManager;
        this.authService = authService;
        this.userService = userService;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
        this.reactiveValidator = reactiveValidator;
        this.securityDetectionProperties = securityDetectionProperties;
    }

    /**
     * 处理用户登录请求
     *
     * @param request 服务器请求对象
     * @return 登录响应
     */
    public Mono<ServerResponse> handleLogin(ServerRequest request) {
        final String path = request.path();

        if (this.reactiveValidator != null) {
            // 先解析请求体并进行恶意输入检测，再进入验证流程
                    return request.bodyToMono(AuthRequest.class)
                            .doOnError(error -> LoggingUtil.error(logger, "解析登录请求体失败", error))
                            .onErrorResume(error -> {
                                LoggingUtil.error(logger, "请求体解析异常", error);
                                return Mono.error(new ValidationException("请求格式错误"));
                            })
                    .flatMap(raw -> {
                        if (isSuspiciousLogin(raw)) {
                            LoggingUtil.warn(logger, "检测到疑似恶意登录输入，返回401");
                            return Mono.error(new AuthenticationException("恶意输入检测失败，认证被拒绝"));
                        }
                        return reactiveValidator.validate(Mono.just(raw))
                                .doOnError(error -> LoggingUtil.error(logger, "验证登录请求失败", error))
                                .onErrorResume(error -> {
                                    LoggingUtil.error(logger, "验证流程异常", error);
                                    return Mono.error(new ValidationException("验证服务异常"));
                                })
                                .flatMap(result -> {
                                    if (!result.isValid()) {
                                        return reactor.core.publisher.Mono.deferContextual(ctxView -> {
                                            String traceId = com.honyrun.util.TraceIdUtil.getOrGenerateTraceId(ctxView);
                                            java.util.Map<String, Object> details = new java.util.HashMap<>();
                                            java.util.List<java.util.Map<String, Object>> violations = result.getViolations() == null
                                                    ? java.util.List.of()
                                                    : result.getViolations().stream()
                                                            .map(v -> java.util.Map.<String, Object>of(
                                                                    "field", v.getPropertyPath() != null ? v.getPropertyPath().toString() : "",
                                                                    "message", v.getMessage()
                                                            ))
                                                            .collect(java.util.stream.Collectors.toList());
                                            details.put("violations", violations);
                                            LoggingUtil.warn(logger, "登录请求输入验证失败: violations={}", violations);
                                            return ServerResponse.badRequest()
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .bodyValue(ApiResponse.error("400", "输入验证失败", traceId, details, path));
                                        });
                                    }

                                    AuthRequest loginRequest = result.getObject();

                                    // 添加调试输出 - 验证Bean注入和请求流程
                                    LoggingUtil.info(logger, "=== AuthHandler.handleLogin 开始 - 用户名: " + loginRequest.getUsername() + " ===");
                                    LoggingUtil.info(logger, "=== AuthService实例类型: " + authService.getClass().getName() + " ===");
                                    LoggingUtil.info(logger, "=== AuthService实例哈希: " + authService.hashCode() + " ===");

                                    // 委托服务层处理认证，确保失败计数与锁定策略生效
                                    return authService.authenticateUser(loginRequest)
                                            .doOnSubscribe(subscription -> LoggingUtil.info(logger, "=== 开始调用authService.authenticateUser ==="))
                                            .doOnNext(user -> LoggingUtil.info(logger, "=== authService.authenticateUser 成功返回用户: " + user.getUsername() + " ==="))
                                            .doOnError(error -> {
                                                LoggingUtil.info(logger, "=== authService.authenticateUser 发生错误: " + error.getClass().getSimpleName() + " - " + error.getMessage() + " ===");
                                                LoggingUtil.error(logger, "用户认证失败", error);
                                            })
                                            .flatMap(user -> generateAuthResponse(user, request)
                                                    .doOnError(error -> LoggingUtil.error(logger, "生成认证响应失败", error))
                                                    .onErrorResume(error -> {
                                                        LoggingUtil.error(logger, "认证响应生成异常", error);
                                                        return Mono.error(new AuthenticationException("认证服务异常"));
                                                    })
                                                    .flatMap(authResponse -> {
                                                        // 记录认证成功审计日志
                                                        String clientIp = getClientIp(request);
                                                        String userAgent = getUserAgent(request);
                                                        return auditLogService.logAuthenticationSuccess(
                                                user.getUsername(),
                                                user.getId(),
                                                clientIp,
                                                userAgent,
                                                "WEB"
                                        ).then(Mono.just(authResponse));
                                                    }))
                                            .flatMap(authResponse -> recordUserActivity(authResponse, request)
                                                    .then(ServerResponse.ok()
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .bodyValue(ApiResponse.success(authResponse))))
                                            .doOnError(AuthenticationException.class, ex -> {
                                                // 记录认证失败审计日志
                                                String clientIp = getClientIp(request);
                                                String userAgent = getUserAgent(request);
                                                auditLogService.logAuthenticationFailure(
                                                        loginRequest.getUsername(),
                                                        clientIp,
                                                        userAgent,
                                                        ex.getMessage(),
                                                        "WEB"
                                                ).subscribe();
                                            })
                                            .switchIfEmpty(Mono.error(new AuthenticationException("认证失败")));
                                });
                    })
                    .onErrorResume(ValidationException.class, ex -> {
                        LoggingUtil.error(logger, "登录验证失败", ex);
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("400", ex.getMessage()));
                    })
                    .onErrorResume(AuthenticationException.class, ex -> {
                        LoggingUtil.error(logger, "认证异常", ex);
                        return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("401", ex.getMessage()));
                    })
                    .onErrorResume(DecodingException.class, ex -> {
                        LoggingUtil.warn(logger, "请求体解析失败，返回400", ex);
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("400", "请求格式错误"));
                    })
                    .onErrorResume(ServerWebInputException.class, ex -> {
                        LoggingUtil.warn(logger, "请求输入无效，返回400", ex);
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("400", "请求格式错误"));
                    })
                    .onErrorResume(Exception.class, ex -> {
                        LoggingUtil.error(logger, "登录处理异常", ex);
                        ex.printStackTrace(); // 添加堆栈跟踪以便调试
                        LoggingUtil.info(logger, "=== 异常详情 ===");
                        LoggingUtil.info(logger, "异常类型: " + ex.getClass().getName());
                        LoggingUtil.info(logger, "异常消息: " + ex.getMessage());
                        if (ex.getCause() != null) {
                            LoggingUtil.info(logger, "根本原因: " + ex.getCause().getClass().getName() + " - " + ex.getCause().getMessage());
                        }
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("500", "登录失败，请稍后重试"));
                    });
        } else {
            // 测试环境或未注入 reactiveValidator 的容错路径：执行基本非空校验
            return request.bodyToMono(AuthRequest.class)
                    .flatMap(loginRequest -> {
                        if (loginRequest == null
                                || !StringUtils.hasText(loginRequest.getUsername())
                                || !StringUtils.hasText(loginRequest.getPassword())
                                || (loginRequest.getUsername() != null && loginRequest.getUsername().length() > 255)) {
                            return ServerResponse.badRequest()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.error("400", "请求格式错误"));
                        }
                        // 安全加固：如检测到明显的注入/XSS等恶意输入，直接按认证失败处理
                        if (isSuspiciousLogin(loginRequest)) {
                            LoggingUtil.warn(logger, "检测到疑似恶意登录输入，返回400");
                            return ServerResponse.badRequest()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.error("400", "请求包含恶意内容，安全检测失败"));
                        }
                        return authService.authenticateUser(loginRequest)
                                .flatMap(user -> generateAuthResponse(user, request)
                                        .flatMap(authResponse -> {
                                            // 记录认证成功审计日志
                                            String clientIp = getClientIp(request);
                                            String userAgent = getUserAgent(request);
                                            // 记录认证成功审计日志
                                            Mono<Void> auditLogMono = auditLogService.logAuthenticationSuccess(
                                                    user.getUsername(),
                                                    user.getId(),
                                                    clientIp,
                                                    userAgent,
                                                    "WEB"
                                            );

                                            // 确保auditLogMono不为null
                                            if (auditLogMono == null) {
                                                LoggingUtil.warn(logger, "auditLogService.logAuthenticationSuccess返回null，使用空Mono");
                                                auditLogMono = Mono.empty();
                                            }

                                            return auditLogMono
                                            .onErrorResume(ex -> {
                                                LoggingUtil.warn(logger, "记录认证成功审计日志失败", ex);
                                                return Mono.empty();
                                            })
                                            .then(Mono.just(authResponse));
                                        }))
                                .flatMap(authResponse -> recordUserActivity(authResponse, request)
                                        .then(ServerResponse.ok()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ApiResponse.success(authResponse))))
                                .onErrorResume(AuthenticationException.class, ex -> {
                                    // 直接处理认证异常，跳过审计日志记录
                                    HttpStatus status = HttpStatus.UNAUTHORIZED;
                                    String message = ex.getMessage() != null ? ex.getMessage() : "认证失败";
                                    // 账户禁用 -> 403；账户锁定 -> 423；其他认证失败 -> 401
                                    if (ex.getErrorCode() == ErrorCode.ACCOUNT_DISABLED) {
                                        status = HttpStatus.FORBIDDEN;
                                    } else if (message.contains("锁定")) {
                                        status = HttpStatus.LOCKED; // 423 Locked
                                    }

                                    return ServerResponse.status(status)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(ApiResponse.error(String.valueOf(status.value()), message));
                                })
                                .switchIfEmpty(ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(ApiResponse.error("401", "认证失败")));
                    })
                    .onErrorResume(ValidationException.class, ex -> {
                        LoggingUtil.error(logger, "登录验证失败", ex);
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("400", ex.getMessage()));
                    })
                    .onErrorResume(DecodingException.class, ex -> {
                        LoggingUtil.warn(logger, "请求体解析失败，返回400", ex);
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("400", "请求格式错误"));
                    })
                    .onErrorResume(ServerWebInputException.class, ex -> {
                        LoggingUtil.warn(logger, "请求输入无效，返回400", ex);
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("400", "请求格式错误"));
                    })
                    .onErrorResume(Exception.class, ex -> {
                        LoggingUtil.error(logger, "登录处理异常", ex);
                        ex.printStackTrace(); // 添加堆栈跟踪以便调试
                        System.out.println("=== 异常详情 ===");
                        System.out.println("异常类型: " + ex.getClass().getName());
                        System.out.println("异常消息: " + ex.getMessage());
                        if (ex.getCause() != null) {
                            System.out.println("根本原因: " + ex.getCause().getClass().getName() + " - " + ex.getCause().getMessage());
                        }
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.error("500", "登录失败，请稍后重试"));
                    });
        }
    }

    /**
     * 处理用户登出请求
     *
     * @param request 服务器请求对象
     * @return 登出响应
     */
    public Mono<ServerResponse> handleLogout(ServerRequest request) {
        return extractTokenFromRequest(request)
                .flatMap(token ->
                        // 先校验令牌有效性
                        jwtTokenProvider.validateToken(token)
                                .flatMap(isValid -> {
                                    if (!isValid) {
                                        return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ApiResponse.error("401", "令牌无效或已过期"));
                                    }
                                    // 再检查是否已在黑名单（已注销的令牌）
                                    return tokenBlacklistService.isTokenBlacklisted(token)
                                            .flatMap(isBlacklisted -> {
                                                if (Boolean.TRUE.equals(isBlacklisted)) {
                                                    return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .bodyValue(ApiResponse.error("401", "令牌已失效"));
                                                }
                                                // 获取用户信息用于审计日志
                                                return jwtTokenProvider.getUsernameFromToken(token)
                                                        .flatMap(username -> jwtTokenProvider.getUserIdFromToken(token)
                                                                .flatMap(userId -> {
                                                                    // 记录登出审计日志
                                                                    String clientIp = getClientIp(request);
                                                                    String userAgent = getUserAgent(request);
                                                                    return auditLogService.logLogout(
                                                                            username,
                                                                            userId,
                                                                            clientIp,
                                                                            userAgent,
                                                                            "用户主动登出"
                                                                    ).then(
                                                                            // 正常登出：加入黑名单并返回成功
                                                                            tokenBlacklistService.addToBlacklist(token, "用户主动登出", 3600L)
                                                                                    .then(ServerResponse.ok()
                                                                                            .contentType(MediaType.APPLICATION_JSON)
                                                                                            .bodyValue(ApiResponse.success("登出成功", "登出成功")))
                                                                    );
                                                                }))
                                                        .switchIfEmpty(
                                                                // 无法获取用户信息时仍然执行登出
                                                                tokenBlacklistService.addToBlacklist(token, "用户主动登出", 3600L)
                                                                        .then(ServerResponse.ok()
                                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                                .bodyValue(ApiResponse.success("登出成功", "登出成功")))
                                                        );
                                            });
                                }))
                .switchIfEmpty(ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.error("401", "缺少认证令牌")))
                .onErrorResume(ex -> {
                    LoggingUtil.error(logger, "登出处理异常", ex);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("500", "登出失败，请稍后重试"));
                });
    }

    /**
     * 处理令牌刷新请求
     *
     * @param request 服务器请求对象
     * @return 刷新响应
     */
    public Mono<ServerResponse> handleRefreshToken(ServerRequest request) {
        return extractRefreshTokenFromRequest(request)
                .flatMap(refreshToken -> {
                    // 从请求头中提取访问令牌（可选）
                    return extractTokenFromRequest(request)
                            .switchIfEmpty(Mono.just("")) // 如果没有访问令牌，使用空字符串
                            .flatMap(accessToken -> {
                                                        return tokenRefreshService.refreshTokens(accessToken, refreshToken)
                                                                .flatMap(tokenPair -> {
                                                                    // 从刷新令牌中获取用户信息
                                                                    return jwtTokenProvider.parseToken(refreshToken)
                                                                            .flatMap(claims -> {
                                                                                String username = claims.get("username", String.class);
                                                                                return userRepository.findByUsername(username)
                                                                                        .flatMap(user -> {
                                                                                            // 创建用户信息，包含权限
                                                                                            return createUserInfo(user)
                                                                                                    .map(userInfo -> {
                                                                                                        // 创建认证响应
                                                                                                        AuthResponse authResponse = new AuthResponse();
                                                                                                        authResponse.setAccessToken(tokenPair.getAccessToken());
                                                                                                        authResponse.setRefreshToken(tokenPair.getRefreshToken());
                                                                                                        authResponse.setTokenType("Bearer");
                                                                                                        authResponse.setExpiresIn(7200L); // 2小时
                                                                                                        authResponse.setUserInfo(userInfo);

                                                                                                        LoggingUtil.info(logger, "Token refreshed successfully for user: {}", username);

                                                                                                        // 记录token刷新审计日志
                                                                                                        String clientIp = getClientIp(request);
                                                                                                        String userAgent = getUserAgent(request);
                                                                                                        auditLogService.logTokenRefresh(
                                                                                                            username,
                                                                                                            user.getId(),
                                                                                                            clientIp,
                                                                                                            userAgent,
                                                                                                            "用户主动刷新令牌"
                                                                                                        ).subscribe();

                                                                                                        return authResponse;
                                                                                                     })
                                                                                                     .flatMap(authResponse ->
                                                                                                         ServerResponse.ok()
                                                                                                                 .contentType(MediaType.APPLICATION_JSON)
                                                                                                                 .bodyValue(ApiResponse.success(authResponse))
                                                                                                     );
                                                                                        })
                                                                                        .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                                                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                                                .bodyValue(ApiResponse.error("404", "用户不存在")));
                                                                            });
                                                                });
                            });
                })
                .switchIfEmpty(ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.error("401", "缺少刷新令牌")))
                .onErrorResume(com.honyrun.exception.AuthenticationException.class, ex -> {
                    LoggingUtil.warn(logger, "Authentication error during token refresh", ex);
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("401", ex.getMessage()));
                })
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    LoggingUtil.warn(logger, "Invalid refresh request", ex);
                    return ServerResponse.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("400", ex.getMessage()));
                })
                .onErrorResume(ex -> {
                    LoggingUtil.error(logger, "令牌刷新异常", ex);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("500", "令牌刷新失败"));
                });
    }

    /**
     * 处理获取当前用户信息请求
     *
     * @param request 服务器请求对象
     * @return 用户信息响应
     */
    public Mono<ServerResponse> handleGetCurrentUser(ServerRequest request) {
        return extractTokenFromRequest(request)
                .flatMap(token -> jwtTokenProvider.validateToken(token)
                        .flatMap(isValid -> {
                            if (isValid) {
                                // 先检查令牌是否已在黑名单（已登出或已失效）
                                return tokenBlacklistService.isTokenBlacklisted(token)
                                        .flatMap(isBlacklisted -> {
                                            if (Boolean.TRUE.equals(isBlacklisted)) {
                                                return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(ApiResponse.error("401", "令牌已失效"));
                                            }
                                            // 未被拉黑，继续解析用户名并返回用户信息
                                            return jwtTokenProvider.getUsernameFromToken(token)
                                                    .flatMap(username -> userRepository.findByUsername(username)
                                                            .flatMap(this::createUserInfo)
                                                            .flatMap(userInfo -> ServerResponse.ok()
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .bodyValue(ApiResponse.success(userInfo)))
                                                            .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .bodyValue(ApiResponse.error("404", "用户不存在"))));
                                        });
                            } else {
                                return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(ApiResponse.error("401", "令牌无效或已过期"));
                            }
                        }))
                .switchIfEmpty(ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.error("401", "缺少认证令牌")))
                .onErrorResume(ex -> {
                    LoggingUtil.error(logger, "获取用户信息异常", ex);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("500", "获取用户信息失败，请稍后重试"));
                });
    }

    /**
     * 处理令牌验证请求
     *
     * @param request 服务器请求对象
     * @return 验证响应
     */
    public Mono<ServerResponse> handleTokenValidation(ServerRequest request) {
        return extractTokenFromRequest(request)
                .flatMap(token -> jwtTokenProvider.validateToken(token)
                        .flatMap(isValid -> {
                            if (isValid) {
                                // 验证是否在黑名单
                                return tokenBlacklistService.isTokenBlacklisted(token)
                                        .flatMap(isBlacklisted -> {
                                            if (Boolean.TRUE.equals(isBlacklisted)) {
                                                return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(ApiResponse.error("401", "令牌已失效"));
                                            }
                                            return jwtTokenProvider.getUsernameFromToken(token)
                                                    .flatMap(username -> userRepository.findByUsername(username)
                                                            .map(user -> {
                                                                TokenValidationResponse response = new TokenValidationResponse();
                                                                response.setValid(true);
                                                                response.setUserId(user.getId().toString());
                                                                response.setUsername(user.getUsername());
                                                                response.setUserType(user.getUserType().name());
                                                                response.setMessage("令牌验证成功");
                                                                return response;
                                                            })
                                                            .flatMap(validationResponse -> ServerResponse.ok()
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .bodyValue(ApiResponse.success(validationResponse)))
                                                            .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .bodyValue(ApiResponse.error("404", "用户不存在"))));
                                        });
                            } else {
                                return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(ApiResponse.error("401", "令牌无效或已过期"));
                            }
                        }))
                .switchIfEmpty(ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.error("401", "缺少认证令牌")))
                .onErrorResume(ex -> {
                    LoggingUtil.error(logger, "令牌验证异常", ex);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.error("500", "令牌验证失败"));
                });
    }

    /**
     * 用户登录请求验证
     *
     * @param loginRequest 登录请求参数
     * @return 验证后的登录请求
     */
    @SuppressWarnings("unused")
    private Mono<AuthRequest> validateLoginRequest(AuthRequest loginRequest) {
        return Mono.fromCallable(() -> {
            if (loginRequest == null) {
                throw new ValidationException("请求体不能为空");
            }
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                throw new ValidationException("用户名不能为空");
            }
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                throw new ValidationException("密码不能为空");
            }
            // 放宽格式校验：允许任何非空用户名/密码，错误凭据交由认证流程返回401
            // 仍保留基本长度限制以避免明显异常输入
            if (loginRequest.getUsername().length() < 1 || loginRequest.getUsername().length() > 64) {
                throw new ValidationException("用户名长度不合法");
            }
            if (loginRequest.getPassword().length() < 1 || loginRequest.getPassword().length() > 128) {
                throw new ValidationException("密码长度不合法");
            }
            return loginRequest;
        });
    }

    /**
     * 用户认证
     *
     * @param loginRequest 登录请求参数
     * @return 认证用户
     */


    /**
     * 生成认证响应
     *
     * @param user 用户对象
     * @param request 服务器请求对象
     * @return 认证响应
     */
    private Mono<AuthResponse> generateAuthResponse(User user, ServerRequest request) {
        String deviceId = getDeviceId(request);
        String clientIp = getClientIp(request);
        String activityId = TraceIdUtil.generateTraceId();

        // 获取用户权限
        return getUserPermissions(user.getId())
                .collectList()
                .flatMap(permissions -> {
                    String authoritiesStr = String.join(",", permissions);

                    // 生成访问令牌
                    return jwtTokenProvider.generateAccessToken(
                            user.getId(),
                            user.getUsername(),
                            user.getUserType(),
                            authoritiesStr,
                            deviceId,
                            clientIp
                    ).flatMap(accessToken -> {
                        // 生成刷新令牌
                        return jwtTokenProvider.generateRefreshToken(
                                user.getId(),
                                user.getUsername(),
                                user.getUserType(),
                                authoritiesStr,
                                deviceId
                        ).flatMap(refreshToken -> {
                            // 创建认证响应
                            AuthResponse authResponse = new AuthResponse();
                            authResponse.setAccessToken(accessToken);
                            authResponse.setRefreshToken(refreshToken);
                            authResponse.setTokenType("Bearer");
                            authResponse.setExpiresIn(7200L);

                            return createUserInfo(user)
                                    .map(userInfo -> {
                                        authResponse.setUserInfo(userInfo);
                                        authResponse.setDeviceId(deviceId);
                                        authResponse.setActivityId(activityId);
                                        return authResponse;
                                    });
                        });
                    });
                });
    }

    /**
     * 记录用户活动
     *
     * @param authResponse 认证响应
     * @param request 服务器请求对象
     * @return 记录结果
     */
    private Mono<Void> recordUserActivity(AuthResponse authResponse, ServerRequest request) {
        // 添加参数验证
        if (authResponse == null || authResponse.getUserInfo() == null) {
            LoggingUtil.warn(logger, "认证响应或用户信息为空，跳过记录用户活动");
            return Mono.empty();
        }

        try {
            Mono<Void> activityMono = activityManager.recordUserActivity(
                    authResponse.getUserInfo().getUserId(),
                    authResponse.getUserInfo().getUsername(),
                    authResponse.getUserInfo().getUserType().name(),
                    authResponse.getActivityId(),
                    authResponse.getAccessToken(),
                    authResponse.getDeviceId(),
                    getClientIp(request),
                    getUserAgent(request)
            );

            // 确保activityMono不为null，符合响应式编程最佳实践
            if (activityMono == null) {
                LoggingUtil.warn(logger, "activityManager.recordUserActivity返回null，跳过记录");
                return Mono.empty();
            }

            // 直接返回异步操作，添加错误处理
            return activityMono
                .doOnSuccess(unused -> LoggingUtil.debug(logger, "用户活动记录成功: userId={}",
                                                       authResponse.getUserInfo().getUserId()))
                .doOnError(error -> LoggingUtil.warn(logger, "用户活动记录失败: userId={}, error={}",
                                                   authResponse.getUserInfo().getUserId(), error.getMessage()))
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "记录用户活动流程异常: {}", error.getMessage());
                    return Mono.empty(); // 确保不影响主流程
                });
        } catch (Exception e) {
            LoggingUtil.warn(logger, "记录用户活动异常: userId={}, error={}",
                           authResponse.getUserInfo().getUserId(), e.getMessage());
            return Mono.empty();
        }
    }

    /**
     * 根据用户ID获取权限列表 - 简化权限模型
     * 【权限获取】：基于用户类型直接获取权限，无需角色中间层
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    private reactor.core.publisher.Flux<String> getUserPermissions(Long userId) {
        // 添加空值检查
        if (userId == null) {
            LoggingUtil.warn(logger, "用户ID为空，返回默认权限");
            return reactor.core.publisher.Flux.just("READ_ONLY");
        }

        // 确保userRepository不为null
        if (userRepository == null) {
            LoggingUtil.error(logger, "ReactiveUserRepository未正确注入，返回默认权限");
            return reactor.core.publisher.Flux.just("READ_ONLY");
        }

        try {
            reactor.core.publisher.Mono<com.honyrun.model.entity.business.User> userMono = userRepository.findById(userId);

            // 检查findById是否返回null
            if (userMono == null) {
                LoggingUtil.error(logger, "ReactiveUserRepository.findById返回null，用户ID: {}", userId);
                return reactor.core.publisher.Flux.just("READ_ONLY");
            }

            return userMono
                    .flatMapMany(user -> {
                        // 基于用户类型直接获取权限
                        return getPermissionsByUserType(user.getUserType().name());
                    })
                    .switchIfEmpty(reactor.core.publisher.Flux.just("READ_ONLY")) // 默认权限
                    .doOnError(error -> LoggingUtil.error(logger, "获取用户权限失败，用户ID: {}", userId, error))
                    .onErrorReturn("READ_ONLY"); // 发生异常时返回最基本权限
        } catch (Exception e) {
            LoggingUtil.error(logger, "获取用户权限时发生异常，用户ID: {}", userId, e);
            return reactor.core.publisher.Flux.just("READ_ONLY");
        }
    }

    /**
     * 根据用户类型获取权限列表 - 简化权限模型
     * 【权限分配】：基于用户类型直接分配权限，无需角色中间层
     *
     * @param userType 用户类型：SYSTEM_USER、NORMAL_USER、GUEST
     * @return 权限列表
     */
    private reactor.core.publisher.Flux<String> getPermissionsByUserType(String userType) {
        LoggingUtil.debug(logger, "Getting permissions for user type: {}", userType);

        switch (userType) {
            case "SYSTEM_USER":
                // 系统用户拥有所有权限
                return reactor.core.publisher.Flux.just(
                    "SYSTEM_MANAGEMENT",
                    "USER_MANAGEMENT",
                    "PERMISSION_MANAGEMENT",
                    "SYSTEM_MONITOR",
                    "BUSINESS_FUNCTION_1",
                    "BUSINESS_FUNCTION_2",
                    "BUSINESS_FUNCTION_3",
                    "EXTERNAL_INTERFACE_MANAGEMENT",
                    "MOCK_INTERFACE_MANAGEMENT",
                    "IMAGE_CONVERSION",
                    "VERSION_MANAGEMENT"
                );
            case "NORMAL_USER":
                // 普通用户拥有基础业务权限
                return reactor.core.publisher.Flux.just(
                    "BUSINESS_FUNCTION_1",
                    "BUSINESS_FUNCTION_2",
                    "BUSINESS_FUNCTION_3",
                    "IMAGE_CONVERSION"
                );
            case "GUEST":
                // 访客仅拥有只读权限
                return reactor.core.publisher.Flux.just("READ_ONLY");
            default:
                // 默认返回最基础权限
                return reactor.core.publisher.Flux.just("READ_ONLY");
        }
    }

    /**
     * 创建用户信息对象
     *
     * @param user 用户实体
     * @return 用户信息
     */
    private Mono<AuthResponse.UserInfo> createUserInfo(User user) {
        return userService.getUserPermissionCodes(user.getId())
                .collectList()
                .map(permissions -> {
                    AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                            user.getId(),
                            user.getUsername(),
                            user.getUserType(),
                            user.getRealName()
                    );

                    userInfo.setEmail(user.getEmail());
                    userInfo.setPhone(maskPhone(user.getPhone()));
                    // 将String状态转换为Integer状态
                    Integer statusCode = "ACTIVE".equals(user.getStatus()) ? 1 : 0;
                    userInfo.setStatus(statusCode);
                    userInfo.setStatusDescription("ACTIVE".equals(user.getStatus()) ? "正常" : "禁用");
                    userInfo.setIsAvailable(user.isAccountNonExpired());
                    // 数据库表中没有最后登录时间字段，设置为null
                    userInfo.setLastLoginTime(null);
                    // 设置创建时间，确保契约字段存在
                    if (user.getCreatedDate() != null) {
                        userInfo.setCreatedAt(user.getCreatedDate());
                    } else {
                        userInfo.setCreatedAt(java.time.LocalDateTime.now());
                    }

                    // 设置用户权限列表
                    userInfo.setPermissions(permissions);

                    return userInfo;
                });
    }

    /**
     * 基础恶意输入检测：识别常见SQL注入/XSS关键字
     * 该方法用于在登录入口处快速拦截明显的攻击尝试，返回401以满足安全契约测试。
     */
    private boolean isSuspiciousLogin(AuthRequest req) {
        if (req == null) return false;

        // 检查用户名和密码长度，直接拒绝超长输入
        final int MAX_USERNAME_LENGTH = 255;
        final int MAX_PASSWORD_LENGTH = 255;

        if (req.getUsername() != null && req.getUsername().length() > MAX_USERNAME_LENGTH) {
            LoggingUtil.warn(logger, "用户名长度 {} 超过最大允许长度 {}, 拒绝请求",
                    req.getUsername().length(), MAX_USERNAME_LENGTH);
            return true;
        }

        if (req.getPassword() != null && req.getPassword().length() > MAX_PASSWORD_LENGTH) {
            LoggingUtil.warn(logger, "密码长度 {} 超过最大允许长度 {}, 拒绝请求",
                    req.getPassword().length(), MAX_PASSWORD_LENGTH);
            return true;
        }

        boolean usernameIsSuspicious = containsMaliciousPatterns(req.getUsername());
        boolean passwordIsSuspicious = containsMaliciousPatterns(req.getPassword());
        boolean isSuspicious = usernameIsSuspicious || passwordIsSuspicious;

        if (isSuspicious) {
            LoggingUtil.debug(logger, "检测到恶意输入 - 用户名: {}, 密码: {}, 用户名可疑: {}, 密码可疑: {}",
                req.getUsername(), "***", usernameIsSuspicious, passwordIsSuspicious);
        }

        return isSuspicious;
    }

    private boolean containsMaliciousPatterns(String input) {
        if (input == null) return false;
        String s = input.toLowerCase();

        // 若未注入配置或未开启，则使用安全默认值
        List<String> patterns;
        int maxQuotesThreshold;
        boolean enabled = true;
        if (securityDetectionProperties != null) {
            enabled = securityDetectionProperties.isEnabled();
            patterns = securityDetectionProperties.getPatterns();
            maxQuotesThreshold = securityDetectionProperties.getMaxQuotesThreshold();
            LoggingUtil.debug(logger, "使用配置的恶意输入检测 - 启用: {}, 模式数量: {}, 引号阈值: {}",
                enabled, patterns != null ? patterns.size() : 0, maxQuotesThreshold);
        } else {
            patterns = java.util.List.of(
                    "<script", "</script", "javascript:", "onerror=", "onload=",
                    "union select", "select * from", "drop table", "or 1=1", "--", "/*", "*/", ";--"
            );
            maxQuotesThreshold = 4;
            LoggingUtil.warn(logger, "SecurityDetectionProperties未注入，使用默认配置");
        }

        if (!enabled) {
            LoggingUtil.debug(logger, "恶意输入检测已禁用");
            return false;
        }

        LoggingUtil.debug(logger, "检查输入: {} (转换为小写: {})", input, s);

        for (String p : patterns) {
            String pattern = p.toLowerCase(); // 确保模式也是小写
            if (s.contains(pattern)) {
                LoggingUtil.debug(logger, "检测到恶意模式: {} 在输入中", pattern);
                return true;
            }
        }

        // 简单的引号/分号组合也可能是注入尝试
        int quoteCount = 0;
        for (char c : s.toCharArray()) {
            if (c == '\'' || c == '"') quoteCount++;
        }
        if (quoteCount >= maxQuotesThreshold) {
            LoggingUtil.debug(logger, "检测到过多引号: {} >= {}", quoteCount, maxQuotesThreshold);
            return true;
        }

        LoggingUtil.debug(logger, "未检测到恶意模式，引号数量: {}", quoteCount);
        return false; // 过多的引号出现则视为可疑
    }

    /**
     * 从请求中提取令牌（仅从Authorization header提取）
     *
     * @param request 服务器请求对象
     * @return 令牌
     */
    private Mono<String> extractTokenFromRequest(ServerRequest request) {
        String authHeader = request.headers().firstHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Mono.just(authHeader.substring(7));
        }
        return Mono.empty();
    }

    /**
     * 从请求中提取refresh token（支持Authorization header和请求体两种方式）
     *
     * @param request 服务器请求对象
     * @return refresh token的Mono
     */
    private Mono<String> extractRefreshTokenFromRequest(ServerRequest request) {
        // 首先尝试从Authorization header提取
        String authHeader = request.headers().firstHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Mono.just(authHeader.substring(7));
        }

        // 如果header中没有，尝试从请求体中提取
        // 对于MockServerRequest，如果没有设置body，会抛出IllegalStateException
        try {
            return request.bodyToMono(String.class)
                    .onErrorResume(IllegalStateException.class, ex -> {
                        // 处理MockServerRequest没有请求体的情况
                        LoggingUtil.debug(logger, "请求中没有请求体: {}", ex.getMessage());
                        return Mono.empty();
                    })
                    .onErrorResume(throwable -> {
                        // 处理其他异常
                        LoggingUtil.debug(logger, "读取请求体时发生异常: {}", throwable.getMessage());
                        return Mono.empty();
                    })
                    .filter(body -> body != null && !body.trim().isEmpty())  // 过滤空字符串
                    .flatMap(body -> {
                        try {
                            // 使用ObjectMapper进行JSON解析
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(body);

                            if (jsonNode.has("refreshToken")) {
                                String token = jsonNode.get("refreshToken").asText();
                                if (token != null && !token.trim().isEmpty()) {
                                    return Mono.just(token);
                                }
                            }

                            LoggingUtil.warn(logger, "请求体中未找到有效的refreshToken字段");
                            return Mono.empty();
                        } catch (Exception e) {
                            LoggingUtil.warn(logger, "解析请求体中的refreshToken失败: {}", e.getMessage());
                            return Mono.empty();
                        }
                    })
                    .switchIfEmpty(Mono.empty());
        } catch (IllegalStateException ex) {
            // 直接在调用bodyToMono时就抛出异常的情况
            LoggingUtil.debug(logger, "请求中没有请求体: {}", ex.getMessage());
            return Mono.empty();
        }
    }

    /**
     * 获取设备ID
     *
     * @param request 服务器请求对象
     * @return 设备ID
     */
    private String getDeviceId(ServerRequest request) {
        String deviceId = request.headers().firstHeader("Device-Id");
        return (deviceId == null || deviceId.isEmpty()) ? "unknown-device" : deviceId;
    }

    /**
     * 获取客户端IP
     *
     * @param request 服务器请求对象
     * @return 客户端IP
     */
    private String getClientIp(ServerRequest request) {
        // 尝试从X-Forwarded-For头获取真实IP
        String xForwardedFor = request.headers().firstHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // 尝试从X-Real-IP头获取
        String xRealIp = request.headers().firstHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return "unknown";
    }

    /**
     * 获取用户代理信息
     *
     * @param request 服务器请求对象
     * @return 用户代理
     */
    private String getUserAgent(ServerRequest request) {
        return request.headers().firstHeader("User-Agent");
    }

    /**
     * 掩码手机号
     *
     * @param phone 手机号
     * @return 掩码后的手机号
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 令牌验证响应类
     */
    public static class TokenValidationResponse {
        private boolean valid;
        private String userId;
        private String username;
        private String userType;
        private String message;

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}


