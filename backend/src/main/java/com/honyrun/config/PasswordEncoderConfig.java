package com.honyrun.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.honyrun.security.ValidatingPasswordEncoder;

/**
 * PasswordEncoder 配置类
 *
 * 【@Primary注解说明】：
 * PasswordEncoder是Spring Security的核心组件，全局唯一性是必需的。
 * 使用@Primary注解确保在整个应用中只有一个默认的密码编码器实例，
 * 避免在注入时产生歧义。这是Spring Security推荐的最佳实践。
 *
 * 【安全特性】：
 * - 使用BCryptPasswordEncoder(强度12)提供高强度密码加密
 * - ValidatingPasswordEncoder包装器确保输入验证的一致性
 * - 对空值和非法输入抛出统一的异常消息
 *
 * 【性能考虑】：
 * - BCrypt强度12在安全性和性能之间取得平衡
 * - 单例模式减少对象创建开销
 *
 * @author Mr.Rey Copyright © 2025
 * @version 2.1.0
 * @created 2025-07-01 00:00:00
 * @modified 2025-07-01 优化@Primary注解使用说明
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 配置全局密码编码器
     *
     * 【@Primary合理性分析】：
     * 1. 全局唯一性：PasswordEncoder在整个应用中应该保持一致
     * 2. Spring Security集成：框架期望有一个默认的密码编码器
     * 3. 避免注入歧义：防止多个PasswordEncoder实现导致的Bean选择问题
     * 4. 标准实践：符合Spring Security官方推荐的配置方式
     *
     * 【不建议使用@Qualifier的原因】：
     * - PasswordEncoder是基础设施组件，不需要多个实现
     * - 使用@Qualifier会增加配置复杂性，违背简单性原则
     * - Spring Security自动配置期望有默认的PasswordEncoder
     *
     * 【环境前缀说明】：
     * - 添加"production"前缀以符合Bean命名规范
     * - 保持@Primary注解确保全局唯一性
     * - 在测试环境中会被TestJwtSecurityConfig中的测试Bean覆盖
     *
     * @return ValidatingPasswordEncoder包装的BCryptPasswordEncoder
     * @author Mr.Rey Copyright © 2025
     * @since 2025-07-01 优化注释说明
     * @version 2.1.0
     * @modified 2025-07-02 添加环境前缀提升命名规范
     */
    @Bean("productionPasswordEncoder")
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new ValidatingPasswordEncoder(new BCryptPasswordEncoder(12));
    }
}


