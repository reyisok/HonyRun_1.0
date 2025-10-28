package com.honyrun.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 简单密码哈希生成器
 * 用于生成BCrypt强度12的密码哈希值
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-27 12:26:43
 * @modified 2025-10-27 12:26:43
 * @version 1.0.0
 */
public class SimplePasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        System.out.println("=== 生成BCrypt强度12密码哈希值 ===");

        // 为honyrun@sys密码生成哈希值
        String honyrunSysHash = encoder.encode("honyrun@sys");
        System.out.println("honyrun@sys: " + honyrunSysHash);

        // 验证生成的哈希值
        boolean matches = encoder.matches("honyrun@sys", honyrunSysHash);
        System.out.println("验证结果: " + matches);

        System.out.println("\n=== 数据库更新SQL ===");
        System.out.println("UPDATE sys_users SET password_hash = '" + honyrunSysHash + "' WHERE username = 'honyrun-sys';");
        System.out.println("UPDATE sys_users SET password_hash = '" + honyrunSysHash + "' WHERE username = 'honyrunsys2';");
        System.out.println("UPDATE sys_users SET password_hash = '" + honyrunSysHash + "' WHERE username = 'user1';");
        System.out.println("UPDATE sys_users SET password_hash = '" + honyrunSysHash + "' WHERE username = 'user2';");
    }
}