-- MySQL初始化脚本
-- @author: Mr.Rey Copyright © 2025
-- @created: 2025-10-24 16:01:03
-- @version: 1.0.0

-- 设置root密码
ALTER USER 'root'@'localhost' IDENTIFIED BY 'rootpwd';

-- 创建honyrunMysql用户
CREATE USER 'honyrunMysql'@'%' IDENTIFIED BY 'honyrun@sys';
GRANT ALL PRIVILEGES ON *.* TO 'honyrunMysql'@'%' WITH GRANT OPTION;

-- 创建honyrundb数据库
CREATE DATABASE IF NOT EXISTS honyrundb CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 刷新权限
FLUSH PRIVILEGES;