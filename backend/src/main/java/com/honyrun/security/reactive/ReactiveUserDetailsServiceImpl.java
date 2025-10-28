package com.honyrun.security.reactive;

import java.util.Collection;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.honyrun.model.entity.business.User;
import com.honyrun.repository.custom.CustomUserRepository;
import com.honyrun.repository.r2dbc.ReactiveUserRepository;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;

/**
 * 响应式用户详情服务
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  17:15:00
 * @modified 2025-07-01 17:15:00
 *
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 实现ReactiveUserDetailsService接口，提供响应式用户信息加载功能
 * 支持用户认证、权限加载、账户状态检查等功能
 */
@Service
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveUserDetailsServiceImpl.class);

    private final ReactiveUserRepository userRepository;
    private final transient CustomUserRepository customUserRepository;

    /**
     * 构造函数注入
     *
     * @param userRepository 响应式用户仓库
     * @param customUserRepository 自定义用户仓库
     */
    public ReactiveUserDetailsServiceImpl(ReactiveUserRepository userRepository,
                                        CustomUserRepository customUserRepository) {
        this.userRepository = userRepository;
        this.customUserRepository = customUserRepository;
    }

    /**
     * 根据用户名加载用户详情
     *
     * @param username 用户名
     * @return Mono<UserDetails> 用户详情
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        LoggingUtil.debug(logger, "Loading user details for username: {}", username);

        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.defer(() -> {
                    LoggingUtil.warn(logger, "User not found: {}", username);
                    return Mono.error(new UsernameNotFoundException("User not found: " + username));
                }))
                .flatMap(user -> {
                    LoggingUtil.debug(logger, "User found: {}, loading permissions", user.getUsername());

                    // 检查用户账户状态
                    if (!user.isAccountNonExpired()) {
                        LoggingUtil.warn(logger, "User account is not available: {}", username);
                        return Mono.error(new UsernameNotFoundException("User account is not available: " + username));
                    }

                    // 加载用户权限
                    return loadUserAuthorities(user.getId())
                            .collectList()
                            .map(authorities -> {
                                LoggingUtil.info(logger, "User details loaded successfully for: {}, authorities: {}",
                                        username, authorities.size());

                                return (UserDetails) new ReactiveUserPrincipal(
                                        user.getUsername(),
                                        user.getPassword(),
                                        user.isEnabled(),
                                        user.isAccountNonExpired(),
                                        true, // credentialsNonExpired
                                        !user.isLocked(),
                                        authorities,
                                        user
                                );
                            });
                })
                .doOnError(error -> LoggingUtil.error(logger, "Failed to load user details for: " + username, error));
    }

    /**
     * 根据用户名查找用户实体
     *
     * @param username 用户名
     * @return Mono<User> 用户实体
     */
    public Mono<User> findUserByUsername(String username) {
        LoggingUtil.debug(logger, "Finding user entity for username: {}", username);

        return userRepository.findByUsername(username)
                .doOnNext(user -> LoggingUtil.debug(logger, "User entity found: {}", user.getUsername()))
                .doOnError(error -> LoggingUtil.error(logger, "Failed to find user entity for: " + username, error));
    }

    /**
     * 加载用户权限
     *
     * @param userId 用户ID
     * @return Flux<GrantedAuthority> 用户权限列表
     */
    private reactor.core.publisher.Flux<? extends GrantedAuthority> loadUserAuthorities(Long userId) {
        LoggingUtil.debug(logger, "Loading authorities for user ID: {}", userId);

        return customUserRepository.findUserPermissions(userId)
                .map(permission -> {
                    LoggingUtil.debug(logger, "Loading permission: {}", permission);
                    // 基于用户类型构建权限 - 简化权限模型
                    // 【权限构建】：直接基于用户类型和具体权限，无需ROLE_前缀，禁止使用角色字段
                    return new SimpleGrantedAuthority(permission.toUpperCase());
                })
                .doOnComplete(() -> LoggingUtil.debug(logger, "Completed loading authorities for user ID: {}", userId))
                .doOnError(error -> LoggingUtil.error(logger, "Failed to load authorities for user ID: " + userId, error));
    }

    /**
     * 响应式用户主体类
     * 实现UserDetails接口，封装用户认证信息
     */
    public static class ReactiveUserPrincipal implements UserDetails {

        private static final long serialVersionUID = 1L;

        private final String username;
        private final String password;
        private final boolean enabled;
        private final boolean accountNonExpired;
        private final boolean credentialsNonExpired;
        private final boolean accountNonLocked;
        private final transient Collection<? extends GrantedAuthority> authorities;
        private final transient User user;

        public ReactiveUserPrincipal(String username, String password, boolean enabled,
                                   boolean accountNonExpired, boolean credentialsNonExpired,
                                   boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities,
                                   User user) {
            this.username = username;
            this.password = password;
            this.enabled = enabled;
            this.accountNonExpired = accountNonExpired;
            this.credentialsNonExpired = credentialsNonExpired;
            this.accountNonLocked = accountNonLocked;
            this.authorities = authorities;
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return accountNonExpired;
        }

        @Override
        public boolean isAccountNonLocked() {
            return accountNonLocked;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return credentialsNonExpired;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * 获取用户实体
         *
         * @return 用户实体
         */
        public User getUser() {
            return user;
        }

        @Override
        public String toString() {
            return "ReactiveUserPrincipal{" +
                    "username='" + username + '\'' +
                    ", enabled=" + enabled +
                    ", accountNonExpired=" + accountNonExpired +
                    ", credentialsNonExpired=" + credentialsNonExpired +
                    ", accountNonLocked=" + accountNonLocked +
                    ", authorities=" + authorities.stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(", ")) +
                    '}';
        }
    }
}


