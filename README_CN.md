# HonyRun System

<div align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.7-green.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/JDK-21-blue.svg" alt="JDK">
  <img src="https://img.shields.io/badge/MySQL-9.4.0-orange.svg" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-8.2.1-red.svg" alt="Redis">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</div>

## 项目概述
HonyRun System是基于Spring WebFlux响应式架构的面向内部管理的业务支撑平台，提供便捷的业务处理、数据查询和系统管理功能。系统采用非阻塞I/O和事件驱动编程模型，支持多用户并发访问，包含响应式业务功能模块和系统管理模块，并提供全面的响应式日志和异常告警机制。

**仅个人参考和学习用途，项目还在完善中**

## 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [系统要求](#系统要求)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [构建与运行](#构建与运行)
- [API 文档](#api-文档)
- [安全机制](#安全机制)
- [测试](#测试)
- [许可证](#许可证)
- [版权](#版权)
- [联系方式](#联系方式)

## 功能特性

- **响应式架构**：基于 Spring WebFlux 构建，支持非阻塞、事件驱动编程
- **全面的认证机制**：基于 JWT 的认证系统，提供安全的令牌管理
- **统一响应格式**：使用 ApiResponse 模型标准化 API 响应
- **高性能设计**：针对 32GB 内存环境优化，支持并行处理能力
- **监控系统**：集成 Prometheus 和 Grafana 进行系统监控
- **日志系统**：结构化日志，支持跟踪 ID 传播，便于诊断
- **统一配置管理**：集中式配置管理系统
- **健康检查**：全面的系统健康验证机制

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| **框架** | Spring Boot | 3.5.7 |
| | Spring WebFlux | - |
| **JDK** | Oracle JDK / OpenJDK | 21 |
| **数据库** | MySQL / MariaDB | 9.4.0 |
| **缓存** | Redis | 8.2.1 |
| | Lettuce (Redis 客户端) | 6.8.0 |
| **安全框架** | Spring Security | - |
| | JWT (JJWT) | 0.12.6 |
| **构建工具** | Maven | 3.8.0+ |
| **测试框架** | JUnit 5 | - |
| | WebTestClient | - |
| | Mockito | - |
| | Testcontainers | - |
| **监控工具** | Prometheus | - |
| | Grafana | - |

## 系统要求

### 硬件要求

- **内存**：32GB RAM（开发环境最佳配置）
- **磁盘空间**：至少 10GB（用于应用程序、依赖和日志）

### 软件要求

- **JDK**：21
- **操作系统**：Windows 11（开发环境）
- **数据库**：MySQL 9.4.0/MariaDB（端口 8906）
- **缓存**：Redis 8.2.1（端口 8902）
- **构建工具**：Maven 3.8.0 或更高版本

## 快速开始

### 前置条件
1. 安装 JDK 21 或更高版本
2. 安装 MySQL 9.4.0 和 Redis 8.2.1
3. 配置 MySQL：
   - 用户名：`honyrunMysql` 或 `root`
   - 密码：`honyrun@sys` 或 `rootpwd`
   - 数据库：`honyrundb`
   - 端口：8906
4. 配置 Redis：
   - 端口：8902

### 安装步骤

1. **克隆代码库**：
   ```bash
   git clone https://github.com/reyiosk/honyrun.git
   cd honyrun/backend
   ```

2. **配置环境**：根据需要更新配置文件（参见 [配置说明](#配置说明) 部分）

3. **构建应用程序**：
   ```bash
   ./mvnw clean package
   ```

4. **运行应用程序**：
   ```bash
   java -jar target/honyrun-*.jar
   ```

## 配置说明

### 环境变量

应用程序可通过以下环境变量进行配置：

| 环境变量 | 描述 | 默认值 |
|----------|------|--------|
| `SERVER_PORT` | 应用程序端口 | 8901 |
| `SPRING_DATASOURCE_URL` | MySQL 连接 URL | - |
| `SPRING_DATASOURCE_USERNAME` | MySQL 用户名 | - |
| `SPRING_DATASOURCE_PASSWORD` | MySQL 密码 | - |
| `SPRING_REDIS_HOST` | Redis 主机 | localhost |
| `SPRING_REDIS_PORT` | Redis 端口 | 8902 |
| `JWT_SECRET` | JWT 令牌生成的密钥 | - |
| `JWT_EXPIRATION` | JWT 令牌过期时间（毫秒） | - |

### 配置文件

应用程序使用以下配置文件：
- `application.properties` - 基础配置
- `application-dev.properties` - 开发环境配置
- `application-prod.properties` - 生产环境配置
- `application-test.properties` - 测试环境配置（仅在 test 目录中）

### JVM 配置

在 32GB RAM 环境中，推荐使用以下 JVM 设置以获得最佳性能：

```
-Xms8g -Xmx16g -XX:+UseG1GC -XX:+UseStringDeduplication
```

## 构建与运行

### 使用 Maven 构建

```bash
# 使用 Maven 包装器构建
./mvnw clean package

# 并行构建（推荐用于 32GB 环境）
./mvnw clean package -T 8C
```

### 运行应用程序

```bash
# 使用默认配置运行
java -jar target/honyrun-*.jar

# 使用特定配置文件运行
java -jar -Dspring.profiles.active=dev target/honyrun-*.jar

# 使用自定义 JVM 参数运行
java -Xms8g -Xmx16g -jar target/honyrun-*.jar
```

## API 文档

API 文档可通过以下地址访问：

- **Swagger UI**：[http://localhost:8901/swagger-ui.html](http://localhost:8901/swagger-ui.html)
- **API 规范**：[http://localhost:8901/v3/api-docs](http://localhost:8901/v3/api-docs)

## 安全机制

### 认证

- 所有 API 端点（除登录端点外）均使用基于 JWT 的认证
- 测试用户：
  - **系统用户**：`honyrun-sys`、`honyrunsys2`（密码：`honyrun@sys`）
  - **普通用户**：`user1`、`user2`（密码：`honyrun@sys`）

### 授权

- 严格执行用户类型：SYSTEM_USER、NORMAL_USER、GUEST
- 所有非认证端点均需适当授权
- 安全测试环境使用 TestJwtSecurityConfig 进行标准化配置

### 密码存储

- 密码使用 BCrypt 进行哈希，成本因子可配置
- 维护密码历史记录，用于安全审计

## 测试

### 运行测试

```bash
# 运行所有测试
./mvnw test

# 运行测试并生成覆盖率报告
./mvnw test jacoco:report
```

### 测试指南

- 遵循最小化模拟原则
- 使用真实的外部 Redis 进行测试
- 仅对不可控的外部依赖使用模拟
- 测试日志位于 `backend/logs` 目录中

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE.txt](LICENSE.txt) 文件。

## 版权

© 2025 Mr.Rey - 保留所有权利。

## 联系方式

- **GitHub**：[https://github.com/reyiosk](https://github.com/reyiosk)
- **邮箱**：reyisok@live.com
