package com.honyrun.repository.r2dbc;

import org.springframework.stereotype.Repository;

/**
 * 响应式系统仓库接口
 *
 * 基于R2DBC的响应式系统数据访问层，提供非阻塞的系统管理相关数据库操作
 * 支持系统设置、系统日志、系统配置、系统状态等实体的响应式数据访问
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理
 *
 * 注意：具体的仓库接口已拆分为独立文件：
 * - ReactiveSystemSettingRepository.java
 * - ReactiveSystemLogRepository.java  
 * - ReactiveSystemConfigRepository.java
 * - ReactiveSystemStatusRepository.java
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  18:45:00
 * @modified 2025-07-01 10:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Repository
public interface ReactiveSystemRepository {
    // 此接口作为系统仓库的标识接口
    // 具体的仓库方法已拆分到独立的接口文件中
}


