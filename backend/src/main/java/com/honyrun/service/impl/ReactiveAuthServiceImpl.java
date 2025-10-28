package com.honyrun.service.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.exception.AuthenticationException;
import com.honyrun.exception.ErrorCode;
import com.honyrun.exception.ValidationException;
import com.honyrun.model.dto.request.AuthRequest;
import com.honyrun.model.dto.response.AuthResponse;
import com.honyrun.model.entity.business.User;
import com.honyrun.repository.r2dbc.ReactiveUserRepository;
import com.honyrun.security.jwt.ReactiveJwtTokenProvider;
import com.honyrun.service.reactive.ReactiveActivityManager;
import com.honyrun.service.reactive.ReactiveAuthService;
import com.honyrun.service.reactive.ReactivePasswordSecurityService;
import com.honyrun.service.reactive.ReactiveTokenBlacklistService;
import com.honyrun.service.security.UnifiedSecurityDetectionService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.common.ValidationUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式认证服务实现类 - 简化权限模型
 * 【权限架构】：采用用户-权限直接映射模型，去除角色中间层
 *
 * 实现ReactiveAuthService接口，提供认证相关的业务逻辑
 * 包含用户认证、令牌管理、权限验证等功能的具体实现
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 10:45:00
 * @modified 2025-01-15 当前时间
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
public class ReactiveAuthServiceImpl implements ReactiveAuthService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveAuthServiceImpl.class);

    private final ReactiveUserRepository userRepository;
    private final ReactiveJwtTokenProvider jwtTokenProvider;
    private final ReactiveTokenBlacklistService tokenBlacklistService;
    private final ReactiveActivityManager activityManager;
    private final PasswordEncoder passwordEncoder;
    private final ReactivePasswordSecurityService passwordSecurityService;
    private final UnifiedSecurityDetectionService unifiedSecurityDetectionService;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入
     *
     * @param userRepository                  用户仓库
     * @param jwtTokenProvider                JWT令牌提供者
     * @param tokenBlacklistService           令牌黑名单服务
     * @param activityManager                 活动管理器
     * @param passwordEncoder                 密码编码器
     * @param passwordSecurityService         密码安全服务
     * @param unifiedSecurityDetectionService 统一安全检测服务
     * @param redisTemplate                   Redis模板（环境适配器提供的统一Bean）
     * @param unifiedConfigManager            统一配置管理器
     */
    public ReactiveAuthServiceImpl(ReactiveUserRepository userRepository,
            ReactiveJwtTokenProvider jwtTokenProvider,
            ReactiveTokenBlacklistService tokenBlacklistService,
            ReactiveActivityManager activityManager,
            PasswordEncoder passwordEncoder,
            ReactivePasswordSecurityService passwordSecurityService,
            UnifiedSecurityDetectionService unifiedSecurityDetectionService,
            @Qualifier("unifiedReactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate,
            UnifiedConfigManager unifiedConfigManager) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
        this.activityManager = activityManager;
        this.passwordEncoder = passwordEncoder;
        this.passwordSecurityService = passwordSecurityService;
        this.unifiedSecurityDetectionService = unifiedSecurityDetectionService;
        this.redisTemplate = redisTemplate;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    // ==================== 用户认证操作 ====================

    @Override
    public Mono<AuthResponse> login(AuthRequest authRequest, ServerWebExchange exchange) {
        LoggingUtil.info(logger, "用户登录请求: {}", authRequest.getUsername());

        return validateAuthRequest(authRequest)
                .then(authenticateUser(authRequest))
                .doOnError(error -> LoggingUtil.error(logger, "用户认证过程失败", error))
                .onErrorResume(error -> Mono.error(new AuthenticationException("用户认证失败: " + error.getMessage())))
                .flatMap(user -> generateAuthResponse(user, authRequest, exchange)
                        .doOnError(error -> LoggingUtil.error(logger, "生成认证响应失败", error))
                        .onErrorResume(
                                error -> Mono.error(new AuthenticationException("生成认证响应失败: " + error.getMessage()))))
                .flatMap(authResponse -> {
                    // 增加并发用户计数器
                    return incrementConcurrentUserCount()
                            .doOnError(error -> LoggingUtil.warn(logger, "增加并发用户计数失败，但不影响登录流程", error))
                            .onErrorResume(error -> Mono.empty())
                            .then(recordUserActivity(authResponse, exchange)
                                    .doOnError(error -> LoggingUtil.error(logger, "记录用户活动失败", error))
                                    .onErrorResume(error -> {
                                        LoggingUtil.warn(logger, "记录用户活动失败，但不影响登录流程", error);
                                        return Mono.empty();
                                    }))
                            .then(Mono.just(authResponse));
                })
                .doOnError(AuthenticationException.class, ex -> LoggingUtil.warn(logger, "用户认证失败: {}", ex.getMessage()))
                .doOnError(ValidationException.class, ex -> LoggingUtil.warn(logger, "登录参数验证失败: {}", ex.getMessage()))
                .doOnError(Exception.class, ex -> LoggingUtil.error(logger, "登录过程发生未知错误", ex));
    }

    @Override
    public Mono<Boolean> logout(ServerWebExchange exchange) {
        return extractTokenFromRequest(exchange)
                .doOnError(error -> LoggingUtil.error(logger, "提取令牌失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.warn(logger, "提取令牌失败，但继续登出流程", error);
                    return Mono.just("invalid-token");
                })
                .flatMap(token -> blacklistToken(token)
                        .doOnError(error -> LoggingUtil.error(logger, "令牌加入黑名单失败", error))
                        .onErrorReturn(false))
                .flatMap(result -> {
                    if (result) {
                        // 减少并发用户计数器
                        return decrementConcurrentUserCount()
                                .doOnError(error -> LoggingUtil.warn(logger, "减少并发用户计数失败，但不影响登出流程", error))
                                .onErrorResume(error -> Mono.empty())
                                .then(Mono.fromRunnable(() -> LoggingUtil.info(logger, "用户登出成功")))
                                .then(Mono.just(true));
                    } else {
                        LoggingUtil.warn(logger, "用户登出失败");
                        return Mono.just(false);
                    }
                })
                .switchIfEmpty(Mono.just(false))
                .doOnError(ex -> LoggingUtil.error(logger, "登出过程发生错误", ex))
                .onErrorReturn(false);
    }

    @Override
    public Mono<AuthResponse> refreshToken(ServerWebExchange exchange) {
        return extractTokenFromRequest(exchange)
                .doOnError(error -> LoggingUtil.error(logger, "提取刷新令牌失败", error))
                .onErrorResume(error -> Mono.error(new AuthenticationException("提取刷新令牌失败: " + error.getMessage())))
                .flatMap(token -> {
                    // 验证刷新令牌并获取用户信息
                    return jwtTokenProvider.validateToken(token)
                            .doOnError(error -> LoggingUtil.error(logger, "验证刷新令牌失败", error))
                            .onErrorResume(
                                    error -> Mono.error(new AuthenticationException("验证刷新令牌失败: " + error.getMessage())))
                            .flatMap(isValid -> {
                                if (!isValid) {
                                    return Mono.error(new AuthenticationException("无效的刷新令牌"));
                                }
                                return jwtTokenProvider.getUsernameFromToken(token)
                                        .doOnError(error -> LoggingUtil.error(logger, "从令牌获取用户名失败", error))
                                        .onErrorResume(error -> Mono.error(
                                                new AuthenticationException("从令牌获取用户名失败: " + error.getMessage())));
                            })
                            .flatMap(username -> userRepository.findByUsername(username)
                                    .doOnError(error -> LoggingUtil.error(logger, "查找用户失败", error))
                                    .onErrorResume(error -> Mono
                                            .error(new AuthenticationException("查找用户失败: " + error.getMessage()))))
                            .switchIfEmpty(Mono.error(new AuthenticationException("用户不存在")))
                            .flatMap(user -> generateRefreshAuthResponse(user, token, exchange)
                                    .doOnError(error -> LoggingUtil.error(logger, "生成刷新认证响应失败", error))
                                    .onErrorResume(error -> Mono
                                            .error(new AuthenticationException("生成刷新认证响应失败: " + error.getMessage()))));
                })
                .doOnError(ex -> LoggingUtil.error(logger, "令牌刷新过程发生错误", ex));
    }

    @Override
    public Mono<AuthResponse.UserInfo> getCurrentUser(ServerWebExchange exchange) {
        return extractTokenFromRequest(exchange)
                .flatMap(token -> jwtTokenProvider.getUsernameFromToken(token))
                .flatMap(username -> userRepository.findByUsername(username))
                .switchIfEmpty(Mono.error(new AuthenticationException("用户不存在")))
                .map(this::createUserInfo)
                .doOnError(ex -> LoggingUtil.error(logger, "获取当前用户信息失败", ex));
    }

    // ==================== 用户验证操作 ====================

    @Override
    public Mono<Void> validateAuthRequest(AuthRequest authRequest) {
        if (authRequest == null) {
            return Mono.error(new ValidationException("登录请求不能为空"));
        }

        return Mono.fromRunnable(() -> {
            if (!ValidationUtil.isValidUsername(authRequest.getUsername())) {
                throw new ValidationException("用户名格式不正确");
            }
            if (!ValidationUtil.isValidPassword(authRequest.getPassword())) {
                throw new ValidationException("密码格式不正确");
            }
            if (authRequest.getDeviceInfo() == null || authRequest.getDeviceInfo().trim().isEmpty()) {
                throw new ValidationException("设备信息不能为空");
            }
        })
                .then(
                        // 检查用户名是否包含恶意内容
                        unifiedSecurityDetectionService.containsMaliciousPatternsReactive(authRequest.getUsername())
                                .flatMap(isMalicious -> {
                                    if (isMalicious) {
                                        LoggingUtil.warn(logger, "检测到恶意登录请求 - 用户名: {}", authRequest.getUsername());
                                        return Mono.error(new ValidationException("登录请求包含恶意内容，已被安全系统拦截"));
                                    }
                                    return Mono.empty();
                                }));
    }

    @Override
    public Mono<User> authenticateUser(AuthRequest authRequest) {
        String username = authRequest.getUsername();

        LoggingUtil.info(logger, "=== ReactiveAuthServiceImpl.authenticateUser 开始 - 用户名: {} ===", username);

        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new AuthenticationException("用户名或密码错误")))
                .flatMap(user -> {
                    LoggingUtil.info(logger, "=== ReactiveAuthServiceImpl.authenticateUser - 用户ID: {}, 用户名: {} ===",
                            user.getId(), username);
                    // 检查账户是否被锁定
                    return passwordSecurityService.isAccountLocked(user.getId())
                            .doOnNext(isLocked -> LoggingUtil.info(logger, "=== 账户锁定检查结果: {} ===", isLocked))
                            .flatMap(isLocked -> {
                                if (isLocked) {
                                    LoggingUtil.info(logger, "=== 账户已被锁定，拒绝登录 ===");
                                    LoggingUtil.warn(logger, "用户账户被锁定，用户名: {}", username);
                                    return Mono.error(new AuthenticationException("账户已被锁定，请稍后再试"));
                                }

                                // 验证密码
                                return validatePassword(user, authRequest.getPassword())
                                        .doOnNext(isValid -> LoggingUtil.info(logger, "=== 密码验证结果: {} ===", isValid))
                                        .flatMap(isValid -> {
                                            if (!isValid) {
                                                LoggingUtil.info(logger, "=== 密码验证失败，记录登录失败 ===");
                                                // 记录登录失败并检查是否需要锁定账户
                                                return passwordSecurityService.recordLoginFailure(user.getId())
                                                        .doOnNext(failureCount -> LoggingUtil.info(logger,
                                                                "=== 当前失败次数: {} ===", failureCount))
                                                        .flatMap(failureCount -> passwordSecurityService
                                                                .hasReachedMaxLoginAttempts(user.getId())
                                                                .doOnNext(hasReached -> LoggingUtil.info(logger,
                                                                        "=== 是否达到最大失败次数: {} ===", hasReached))
                                                                .flatMap(hasReached -> {
                                                                    if (hasReached) {
                                                                        LoggingUtil.info(logger,
                                                                                "=== 达到最大失败次数，锁定账户 ===");
                                                                        // 锁定账户
                                                                        return passwordSecurityService
                                                                                .getLockoutDuration()
                                                                                .flatMap(
                                                                                        duration -> passwordSecurityService
                                                                                                .lockUserAccount(
                                                                                                        user.getId(),
                                                                                                        duration)
                                                                                                .doOnSuccess(
                                                                                                        v -> LoggingUtil
                                                                                                                .info(logger,
                                                                                                                        "=== 账户锁定完成 ==="))
                                                                                                .then(Mono.error(
                                                                                                        new AuthenticationException(
                                                                                                                "登录失败次数过多，账户已被锁定"))));
                                                                    } else {
                                                                        LoggingUtil.info(logger,
                                                                                "=== 未达到最大失败次数，返回密码错误 ===");
                                                                        return Mono.error(new AuthenticationException(
                                                                                "用户名或密码错误"));
                                                                    }
                                                                }));
                                            }

                                            LoggingUtil.info(logger, "=== 密码验证成功，检查账户状态 ===");
                                            // 验证账户状态
                                            if (!"ACTIVE".equals(user.getStatus())) {
                                                return Mono.error(new AuthenticationException(
                                                        ErrorCode.ACCOUNT_DISABLED, "账户已被禁用"));
                                            }
                                            if (!user.isAccountNonExpired()) {
                                                return Mono.error(new AuthenticationException("账户已过期"));
                                            }

                                            // 登录成功，重置失败计数
                                            return passwordSecurityService.resetLoginFailureCount(user.getId())
                                                    .then(Mono.just(user));
                                        });
                            });
                });
    }

    @Override
    public Mono<Boolean> validatePassword(User user, String rawPassword) {
        return Mono.fromCallable(() -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    // ==================== 令牌管理操作 ====================

    @Override
    public Mono<AuthResponse> generateAuthResponse(User user, AuthRequest authRequest, ServerWebExchange exchange) {
        String deviceId = getDeviceId(authRequest);
        String clientIp = getClientIpFromExchange(exchange);
        String activityId = UUID.randomUUID().toString();

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
                            clientIp).flatMap(accessToken -> {
                                // 生成刷新令牌
                                return jwtTokenProvider.generateRefreshToken(
                                        user.getId(),
                                        user.getUsername(),
                                        user.getUserType(),
                                        authoritiesStr,
                                        deviceId).flatMap(refreshToken -> {
                                            // 创建认证响应
                                            return unifiedConfigManager
                                                    .getLongConfig("honyrun.jwt.expiration.seconds", 7200L)
                                                    .flatMap(expiresIn -> {
                                                        AuthResponse authResponse = new AuthResponse();
                                                        authResponse.setAccessToken(accessToken);
                                                        authResponse.setRefreshToken(refreshToken);
                                                        authResponse.setTokenType("Bearer");
                                                        authResponse.setExpiresIn(expiresIn);
                                                        // 填充权限到响应与用户信息
                                                        authResponse.setPermissions(permissions);
                                                        AuthResponse.UserInfo userInfo = createUserInfo(user);
                                                        if (userInfo != null) {
                                                            userInfo.setPermissions(permissions);
                                                            authResponse.setUserInfo(userInfo);
                                                        }
                                                        authResponse.setDeviceId(deviceId);
                                                        authResponse.setActivityId(activityId);

                                                        return Mono.just(authResponse);
                                                    });
                                        });
                            });
                });

    }

    @Override
    public Mono<AuthResponse> generateRefreshAuthResponse(User user, String oldToken, ServerWebExchange exchange) {
        String deviceId = "refresh-device";
        String clientIp = getClientIpFromExchange(exchange);
        String activityId = UUID.randomUUID().toString();

        // 获取用户权限
        return getUserPermissions(user.getId())
                .collectList()
                .flatMap(permissions -> {
                    String authoritiesStr = String.join(",", permissions);

                    // 生成新的访问令牌
                    return jwtTokenProvider.generateAccessToken(
                            user.getId(),
                            user.getUsername(),
                            user.getUserType(),
                            authoritiesStr,
                            deviceId,
                            clientIp).flatMap(newAccessToken -> {
                                // 将旧令牌加入黑名单
                                return unifiedConfigManager.getLongConfig("honyrun.jwt.blacklist.ttl.seconds", 3600L)
                                        .flatMap(blacklistTtl -> tokenBlacklistService.addToBlacklist(oldToken,
                                                "Token refreshed", blacklistTtl))
                                        .then(unifiedConfigManager.getLongConfig("honyrun.jwt.expiration.seconds",
                                                3600L))
                                        .flatMap(expiresIn -> {
                                            // 创建认证响应
                                            AuthResponse authResponse = new AuthResponse();
                                            authResponse.setAccessToken(newAccessToken);
                                            authResponse.setRefreshToken(oldToken); // 保持原刷新令牌
                                            authResponse.setTokenType("Bearer");
                                            authResponse.setExpiresIn(expiresIn);
                                            // 填充权限到响应与用户信息
                                            authResponse.setPermissions(permissions);
                                            AuthResponse.UserInfo userInfo = createUserInfo(user);
                                            if (userInfo != null) {
                                                userInfo.setPermissions(permissions);
                                                authResponse.setUserInfo(userInfo);
                                            }
                                            authResponse.setDeviceId(deviceId);
                                            authResponse.setActivityId(activityId);

                                            return Mono.just(authResponse);
                                        });
                            });
                });
    }

    @Override
    public Mono<String> extractTokenFromRequest(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Mono.just(authHeader.substring(7));
        }
        return Mono.empty();
    }

    @Override
    public Mono<Boolean> blacklistToken(String token) {
        return unifiedConfigManager.getLongConfig("honyrun.jwt.blacklist.ttl.seconds", 3600L)
                .flatMap(ttl -> tokenBlacklistService.addToBlacklist(token, "User logout", ttl))
                .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return jwtTokenProvider.validateToken(token)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.just(false);
                    }
                    // 检查令牌是否在黑名单中
                    return tokenBlacklistService.isTokenBlacklisted(token)
                            .map(isBlacklisted -> !isBlacklisted);
                })
                .onErrorReturn(false);
    }

    // ==================== 权限管理操作 ====================

    @Override
    public Flux<String> getUserPermissions(Long userId) {
        // 【权限获取】：基于用户类型直接获取权限，无需角色中间层
        LoggingUtil.debug(logger, "获取用户权限，用户ID: {}", userId);

        if (userId == null) {
            LoggingUtil.warn(logger, "用户ID为空，返回空权限列表");
            return Flux.empty();
        }

        return userRepository.findById(userId)
                .flatMapMany(user -> {
                    String userType = user.getUserType().name();
                    LoggingUtil.debug(logger, "用户类型: {}, 用户ID: {}", userType, userId);

                    return getPermissionsByUserType(userType);
                })
                .switchIfEmpty(Flux.defer(() -> {
                    LoggingUtil.warn(logger, "用户不存在，用户ID: {}", userId);
                    return Flux.empty();
                }))
                .doOnNext(permission -> LoggingUtil.debug(logger, "用户权限: {}, 用户ID: {}", permission, userId))
                .doOnError(error -> LoggingUtil.error(logger, "获取用户权限失败，用户ID: " + userId, error));
    }

    @Override
    public Mono<Boolean> hasPermission(Long userId, String permissionCode) {
        LoggingUtil.debug(logger, "检查用户权限，用户ID: {}, 权限代码: {}", userId, permissionCode);

        if (userId == null || permissionCode == null || permissionCode.trim().isEmpty()) {
            LoggingUtil.warn(logger, "参数无效，用户ID: {}, 权限代码: {}", userId, permissionCode);
            return Mono.just(false);
        }

        return getUserPermissions(userId)
                .any(permission -> permission.equals(permissionCode))
                .doOnNext(hasPermission -> LoggingUtil.debug(logger,
                        "权限检查结果: {}, 用户ID: {}, 权限代码: {}", hasPermission, userId, permissionCode))
                .doOnError(error -> LoggingUtil.error(logger,
                        "检查用户权限失败，用户ID: " + userId + ", 权限代码: " + permissionCode, error));
    }

    /**
     * 根据用户类型获取权限列表
     * 【权限分配】：基于用户类型直接分配权限，无需角色中间层
     *
     * @param userType 用户类型：SYSTEM_USER、NORMAL_USER、GUEST
     * @return 权限列表
     */
    private Flux<String> getPermissionsByUserType(String userType) {
        LoggingUtil.debug(logger, "根据用户类型获取权限: {}", userType);

        switch (userType) {
            case "SYSTEM_USER":
                // 系统用户拥有所有权限
                return Flux.just(
                        "SYSTEM_MANAGEMENT",
                        "USER_MANAGEMENT",
                        "USER_READ",
                        "USER_CREATE",
                        "USER_UPDATE",
                        "USER_DELETE",
                        "USER_PERMISSION_UPDATE",
                        "USER_STATUS_UPDATE",
                        "USER_ACTIVITY_READ",
                        "USER_STATISTICS_READ",
                        "SYSTEM_SETTING",
                        "LOG_VIEW",
                        "BUSINESS_OPERATE",
                        "FILE_UPLOAD");
            case "NORMAL_USER":
                // 普通用户拥有基础权限
                return Flux.just(
                        "USER_READ",
                        "USER_UPDATE", // 仅限自己的资源
                        "BUSINESS_OPERATE",
                        "FILE_UPLOAD");
            case "GUEST":
                // 访客用户只有只读权限
                return Flux.just(
                        "USER_READ" // 仅限自己的基本信息
                );
            default:
                LoggingUtil.warn(logger, "未知用户类型: {}", userType);
                return Flux.empty();
        }
    }

    // ==================== 活动记录操作 ====================

    @Override
    public Mono<Void> recordUserActivity(AuthResponse authResponse, ServerWebExchange exchange) {
        return Mono.fromRunnable(() -> {
            try {
                // 使用recordUserActivity方法
                activityManager.recordUserActivity(
                        authResponse.getUserInfo().getUserId(),
                        authResponse.getUserInfo().getUsername(),
                        authResponse.getUserInfo().getUserType().name(),
                        authResponse.getActivityId(),
                        authResponse.getAccessToken(),
                        authResponse.getDeviceId(),
                        getClientIpFromExchange(exchange),
                        getUserAgentFromExchange(exchange)).subscribe();
            } catch (Exception e) {
                LoggingUtil.warn(logger, "记录用户活动失败: {}", e.getMessage());
            }
        });
    }

    @Override
    public Mono<Void> recordLoginActivity(Long userId, String clientIp, String userAgent, String deviceId) {
        return Mono.fromRunnable(() -> {
            try {
                // 记录登录活动的具体实现
                LoggingUtil.info(logger, "记录用户登录活动: userId={}, clientIp={}, deviceId={}", userId, clientIp, deviceId);
            } catch (Exception e) {
                LoggingUtil.warn(logger, "记录登录活动失败: {}", e.getMessage());
            }
        });
    }

    @Override
    public Mono<Void> recordLogoutActivity(Long userId, String clientIp) {
        return Mono.fromRunnable(() -> {
            try {
                // 记录登出活动的具体实现
                LoggingUtil.info(logger, "记录用户登出活动: userId={}, clientIp={}", userId, clientIp);
            } catch (Exception e) {
                LoggingUtil.warn(logger, "记录登出活动失败: {}", e.getMessage());
            }
        });
    }

    // ==================== 工具方法 ====================

    @Override
    public String getDeviceId(AuthRequest authRequest) {
        return authRequest.getDeviceId() != null ? authRequest.getDeviceId() : "unknown-device";
    }

    @Override
    public String getClientIpFromExchange(ServerWebExchange exchange) {
        // 尝试从X-Forwarded-For头获取真实IP
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // 尝试从X-Real-IP头获取
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // 修复空指针问题：添加null检查并避免重复调用
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }

        return "unknown";
    }

    @Override
    public String getUserAgentFromExchange(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("User-Agent");
    }

    @Override
    public AuthResponse.UserInfo createUserInfo(User user) {
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getUserType(),
                user.getRealName());

        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(maskPhone(user.getPhone()));
        // 将String状态转换为Integer状态
        Integer statusCode = "ACTIVE".equals(user.getStatus()) ? 1 : 0;
        userInfo.setStatus(statusCode);
        userInfo.setStatusDescription("ACTIVE".equals(user.getStatus()) ? "正常" : "禁用");
        userInfo.setIsAvailable(user.isAccountNonExpired());
        // 数据库表中没有最后登录时间字段，设置为null
        userInfo.setLastLoginTime(null);
        // 设置创建时间（契约字段名为createdAt）
        userInfo.setCreatedAt(user.getCreatedDate());

        return userInfo;
    }

    @Override
    public AuthResponse createErrorAuthResponse(String errorMessage) {
        AuthResponse authResponse = new AuthResponse();
        // AuthResponse没有message字段，这里只返回基本的响应对象
        return authResponse;
    }

    @Override
    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    // ==================== 并发用户计数器管理 ====================

    /**
     * 增加并发用户计数器
     *
     * @return 增加操作的Mono
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-15 当前时间
     * @version 1.0.0
     */
    private Mono<Void> incrementConcurrentUserCount() {
        return redisTemplate.opsForValue()
                .increment("system:concurrent:users:count")
                .doOnNext(count -> LoggingUtil.debug(logger, "并发用户计数器增加，当前值: {}", count))
                .doOnError(error -> LoggingUtil.error(logger, "增加并发用户计数器失败", error))
                .then();
    }

    /**
     * 减少并发用户计数器
     *
     * @return 减少操作的Mono
     * @author Mr.Rey Copyright © 2025
     * @created 2025-01-15 当前时间
     * @version 1.0.0
     */
    private Mono<Void> decrementConcurrentUserCount() {
        return redisTemplate.opsForValue()
                .decrement("system:concurrent:users:count")
                .doOnNext(count -> {
                    // 确保计数器不会变成负数
                    if (count < 0) {
                        redisTemplate.opsForValue().set("system:concurrent:users:count", "0").subscribe();
                        LoggingUtil.warn(logger, "并发用户计数器出现负数，已重置为0");
                    } else {
                        LoggingUtil.debug(logger, "并发用户计数器减少，当前值: {}", count);
                    }
                })
                .doOnError(error -> LoggingUtil.error(logger, "减少并发用户计数器失败", error))
                .then();
    }
}
