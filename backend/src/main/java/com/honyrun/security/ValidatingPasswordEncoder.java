package com.honyrun.security;

import com.honyrun.util.common.StringUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 验证密码编码器包装器
 * 
 * 功能说明：
 * - 在编码前验证密码不能为null或空白
 * - 在匹配前验证参数不能为null
 * - 委托给实际的PasswordEncoder进行编码和匹配
 * 
 * @author Mr.Rey
 * @version 1.0.0
 * @created 2025-07-01 21:00:00
 * @modified 2025-07-01 21:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class ValidatingPasswordEncoder implements PasswordEncoder {

    private final PasswordEncoder delegate;

    /**
     * 构造函数
     *
     * @param delegate 实际的密码编码器
     */
    public ValidatingPasswordEncoder(PasswordEncoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        String password = rawPassword.toString();
        if (StringUtil.isBlank(password)) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // BCrypt限制密码不能超过72字节，如果超过则截断
        byte[] passwordBytes = password.getBytes();
        if (passwordBytes.length > 72) {
            // 截断到72字节，确保不会在多字节字符中间截断
            byte[] truncatedBytes = new byte[72];
            System.arraycopy(passwordBytes, 0, truncatedBytes, 0, 72);
            
            // 从截断点向前查找，确保不在多字节字符中间截断
            int validLength = 72;
            while (validLength > 0) {
                try {
                    String truncated = new String(truncatedBytes, 0, validLength, "UTF-8");
                    // 检查是否有替换字符，如果没有则说明截断位置正确
                    if (!truncated.contains("\uFFFD")) {
                        password = truncated;
                        break;
                    }
                } catch (Exception e) {
                    // 继续尝试更短的长度，这是正常的UTF-8截断处理流程
                }
                validLength--;
            }
            
            // 如果所有尝试都失败，使用前64个字节作为安全回退
            if (validLength <= 0) {
                password = new String(passwordBytes, 0, Math.min(64, passwordBytes.length));
            }
        }
        
        return delegate.encode(password);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        if (encodedPassword == null) {
            throw new IllegalArgumentException("编码密码不能为空");
        }
        
        return delegate.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return delegate.upgradeEncoding(encodedPassword);
    }
}

