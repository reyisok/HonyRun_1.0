-- ========================================
-- HonyRun 统一数据库初始化脚本
-- 版本: 1.0.0
-- 创建日期: 2025-10-27 12:26:43
-- 作者: Mr.Rey Copyright © 2025
-- 描述: 统一的数据库架构脚本，包含所有表结构、索引和基础数据
-- ========================================

-- 删除已存在的表（开发环境重新初始化）
-- 注意：先删除有外键约束的表，再删除被引用的表
DROP TABLE IF EXISTS user_permissions;
DROP TABLE IF EXISTS sys_system_logs;
DROP TABLE IF EXISTS sys_system_configs;
DROP TABLE IF EXISTS sys_users;

-- ========================================
-- 用户表
-- ========================================
CREATE TABLE sys_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    real_name VARCHAR(100) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    user_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL_USER' COMMENT '用户类型：SYSTEM_USER, NORMAL_USER, GUEST',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态：ACTIVE, INACTIVE, LOCKED',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '账户启用状态：1-启用，0-禁用',
    valid_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '账户有效期开始时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '创建人ID',
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    last_modified_by BIGINT COMMENT '最后修改人ID',
    version BIGINT DEFAULT 0 COMMENT '版本号',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间'
) COMMENT='系统用户表';

-- 用户表索引
CREATE INDEX idx_users_username ON sys_users(username);
CREATE INDEX idx_users_email ON sys_users(email);
CREATE INDEX idx_users_user_type ON sys_users(user_type);
CREATE INDEX idx_users_status ON sys_users(status);
CREATE INDEX idx_users_deleted ON sys_users(deleted);
CREATE INDEX idx_users_created_at ON sys_users(created_at);

-- ========================================
-- 用户权限表
-- ========================================
CREATE TABLE user_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    permission VARCHAR(100) NOT NULL COMMENT '权限代码',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活：1-激活，0-未激活',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES sys_users(id) ON DELETE CASCADE
) COMMENT='用户权限表';

-- 用户权限表索引
CREATE INDEX idx_user_permissions_user_id ON user_permissions(user_id);
CREATE INDEX idx_user_permissions_permission ON user_permissions(permission);
CREATE INDEX idx_user_permissions_is_active ON user_permissions(is_active);

-- ========================================
-- 系统日志表
-- ========================================
CREATE TABLE sys_system_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    log_type VARCHAR(50) NOT NULL COMMENT '日志类型',
    log_level VARCHAR(20) NOT NULL COMMENT '日志级别',
    module VARCHAR(50) COMMENT '模块名称',
    message TEXT NOT NULL COMMENT '日志消息',
    description TEXT COMMENT '日志描述',
    user_id BIGINT COMMENT '用户ID',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    request_uri VARCHAR(500) COMMENT '请求URI',
    method VARCHAR(10) COMMENT 'HTTP方法',
    parameters TEXT COMMENT '请求参数',
    execution_time BIGINT COMMENT '执行时间(毫秒)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='系统日志表';

-- 系统日志表索引
CREATE INDEX idx_logs_log_type ON sys_system_logs(log_type);
CREATE INDEX idx_logs_log_level ON sys_system_logs(log_level);
CREATE INDEX idx_logs_module ON sys_system_logs(module);
CREATE INDEX idx_logs_user_id ON sys_system_logs(user_id);
CREATE INDEX idx_logs_created_at ON sys_system_logs(created_at);
CREATE INDEX idx_logs_ip_address ON sys_system_logs(ip_address);

-- ========================================
-- 系统配置表
-- ========================================
CREATE TABLE sys_system_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_group VARCHAR(50) COMMENT '配置分组',
    config_type VARCHAR(20) DEFAULT 'STRING' COMMENT '配置类型：STRING, INTEGER, BOOLEAN, JSON',
    description TEXT COMMENT '配置描述',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, INACTIVE',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：1-删除，0-未删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '创建人ID',
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    last_modified_by BIGINT COMMENT '最后修改人ID',
    version BIGINT DEFAULT 0 COMMENT '版本号（乐观锁）'
) COMMENT='系统配置表';

-- 系统配置表索引
CREATE INDEX idx_configs_config_key ON sys_system_configs(config_key);
CREATE INDEX idx_configs_config_group ON sys_system_configs(config_group);
CREATE INDEX idx_configs_status ON sys_system_configs(status);
CREATE INDEX idx_configs_enabled ON sys_system_configs(enabled);
CREATE INDEX idx_configs_deleted ON sys_system_configs(deleted);

-- ========================================
-- 用户类型权限模板表
-- ========================================
CREATE TABLE IF NOT EXISTS sys_user_type_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_type VARCHAR(50) NOT NULL COMMENT '用户类型：SYSTEM_USER、NORMAL_USER、GUEST',
    permission_codes TEXT NOT NULL COMMENT '权限代码列表，JSON格式',
    permission_names TEXT NOT NULL COMMENT '权限名称列表，JSON格式',
    description VARCHAR(500) COMMENT '权限模板描述',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE、INACTIVE',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(100) COMMENT '创建人',
    updated_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_user_type (user_type),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户类型权限模板表';

-- 系统字典表
CREATE TABLE IF NOT EXISTS sys_dictionary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_key VARCHAR(100) NOT NULL COMMENT '字典键',
    dict_value VARCHAR(500) NOT NULL COMMENT '字典值',
    dict_label VARCHAR(200) NOT NULL COMMENT '字典标签',
    description VARCHAR(500) COMMENT '字典描述',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE、INACTIVE',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(100) COMMENT '创建人',
    updated_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 1 COMMENT '版本号',
    UNIQUE KEY uk_dict_type_key (dict_type, dict_key, deleted),
    INDEX idx_dict_type (dict_type),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    INDEX idx_sort_order (sort_order),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典表';

-- ========================================
-- 基础数据插入
-- ========================================

-- 插入系统用户数据 (使用BCrypt强度12)
INSERT INTO sys_users (id, username, password_hash, user_type, status, created_at, updated_at) VALUES
(1, 'honyrun-sys', '$2a$12$XK2tD7NcrhAOwAJjh.vPiu6xmV3m5/Xnw0xm2Bf3ytYsPwEfcVfli', 'SYSTEM_USER', 'ACTIVE', NOW(), NOW()),
(2, 'honyrunsys2', '$2a$12$XK2tD7NcrhAOwAJjh.vPiu6xmV3m5/Xnw0xm2Bf3ytYsPwEfcVfli', 'SYSTEM_USER', 'ACTIVE', NOW(), NOW()),
(3, 'user1', '$2a$12$XK2tD7NcrhAOwAJjh.vPiu6xmV3m5/Xnw0xm2Bf3ytYsPwEfcVfli', 'NORMAL_USER', 'ACTIVE', NOW(), NOW()),
(4, 'user2', '$2a$12$XK2tD7NcrhAOwAJjh.vPiu6xmV3m5/Xnw0xm2Bf3ytYsPwEfcVfli', 'NORMAL_USER', 'ACTIVE', NOW(), NOW());

-- 插入系统初始化日志
INSERT INTO sys_system_logs (log_type, log_level, module, message, user_id) VALUES
('SYSTEM_INIT', 'INFO', 'DATABASE', '数据库初始化完成', NULL),
('USER_INIT', 'INFO', 'USER_MANAGEMENT', '系统用户初始化完成', NULL);

-- 插入系统配置数据
INSERT INTO sys_system_configs (config_key, config_value, config_group, config_type, description, status, enabled, deleted) VALUES
('honyrun.health.cache-duration', '300', 'HEALTH', 'INTEGER', '健康检查缓存持续时间（秒）', 'ACTIVE', 1, 0),
('honyrun.monitoring.enabled', 'true', 'MONITORING', 'BOOLEAN', '是否启用监控功能', 'ACTIVE', 1, 0),
('honyrun.cache.default-ttl', '3600', 'CACHE', 'INTEGER', '默认缓存过期时间（秒）', 'ACTIVE', 1, 0),
('honyrun.security.jwt.expiration', '86400', 'SECURITY', 'INTEGER', 'JWT令牌过期时间（秒）', 'ACTIVE', 1, 0),
('honyrun.database.pool.max-size', '20', 'DATABASE', 'INTEGER', '数据库连接池最大连接数', 'ACTIVE', 1, 0);

-- ========================================
-- 脚本执行完成标记
-- ========================================
SELECT 'HonyRun unified database schema initialization completed successfully' as result;
