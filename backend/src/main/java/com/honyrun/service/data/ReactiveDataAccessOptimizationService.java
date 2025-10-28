package com.honyrun.service.data;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

/**
 * 响应式数据访问优化服务接口
 * 
 * 提供数据访问层的性能优化功能：
 * - 批量数据操作
 * - 事务优化管理
 * - 连接池监控
 * - 查询性能分析
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01 15:00:00
 * @modified 2025-07-01 15:00:00
 * @version 1.0.0
 */
public interface ReactiveDataAccessOptimizationService {

    // ==================== 批量操作优化 ====================

    /**
     * 批量插入数据
     * 
     * @param entities 实体列表
     * @param batchSize 批次大小
     * @param <T> 实体类型
     * @return 插入结果的Flux
     */
    <T> Flux<T> batchInsert(List<T> entities, int batchSize);

    /**
     * 批量更新数据
     * 
     * @param entities 实体列表
     * @param batchSize 批次大小
     * @param <T> 实体类型
     * @return 更新结果的Flux
     */
    <T> Flux<T> batchUpdate(List<T> entities, int batchSize);

    /**
     * 批量删除数据
     * 
     * @param ids ID列表
     * @param batchSize 批次大小
     * @param entityClass 实体类型
     * @param <T> 实体类型
     * @return 删除数量的Mono
     */
    <T> Mono<Long> batchDelete(List<Long> ids, int batchSize, Class<T> entityClass);

    // ==================== 事务优化管理 ====================

    /**
     * 执行只读事务
     * 
     * @param operation 操作函数
     * @param <T> 返回类型
     * @return 操作结果的Mono
     */
    <T> Mono<T> executeInReadOnlyTransaction(Function<Void, Mono<T>> operation);

    /**
     * 执行批量事务
     * 
     * @param operation 操作函数
     * @param <T> 返回类型
     * @return 操作结果的Mono
     */
    <T> Mono<T> executeInBatchTransaction(Function<Void, Mono<T>> operation);

    /**
     * 执行优化事务
     * 
     * @param operation 操作函数
     * @param <T> 返回类型
     * @return 操作结果的Mono
     */
    <T> Mono<T> executeInOptimizedTransaction(Function<Void, Mono<T>> operation);

    // ==================== 连接池监控 ====================

    /**
     * 获取连接池状态
     * 
     * @return 连接池状态的Mono
     */
    Mono<ConnectionPoolStats> getConnectionPoolStats();

    /**
     * 获取活跃连接数
     * 
     * @return 活跃连接数的Mono
     */
    Mono<Integer> getActiveConnections();

    /**
     * 获取空闲连接数
     * 
     * @return 空闲连接数的Mono
     */
    Mono<Integer> getIdleConnections();

    // ==================== 查询性能分析 ====================

    /**
     * 执行带性能监控的查询
     * 
     * @param queryName 查询名称
     * @param operation 查询操作
     * @param <T> 返回类型
     * @return 查询结果的Mono
     */
    <T> Mono<T> executeWithPerformanceMonitoring(String queryName, Mono<T> operation);

    /**
     * 获取查询性能统计
     * 
     * @param queryName 查询名称
     * @return 性能统计的Mono
     */
    Mono<QueryPerformanceStats> getQueryPerformanceStats(String queryName);

    /**
     * 清理性能统计数据
     * 
     * @return 操作结果的Mono
     */
    Mono<Boolean> clearPerformanceStats();

    // ==================== 数据传输对象 ====================

    /**
     * 连接池状态信息
     */
    class ConnectionPoolStats {
        private final int totalConnections;
        private final int activeConnections;
        private final int idleConnections;
        private final int pendingAcquireCount;
        private final long maxAcquireTime;
        private final long maxIdleTime;

        public ConnectionPoolStats(int totalConnections, int activeConnections, 
                                 int idleConnections, int pendingAcquireCount,
                                 long maxAcquireTime, long maxIdleTime) {
            this.totalConnections = totalConnections;
            this.activeConnections = activeConnections;
            this.idleConnections = idleConnections;
            this.pendingAcquireCount = pendingAcquireCount;
            this.maxAcquireTime = maxAcquireTime;
            this.maxIdleTime = maxIdleTime;
        }

        public int getTotalConnections() { return totalConnections; }
        public int getActiveConnections() { return activeConnections; }
        public int getIdleConnections() { return idleConnections; }
        public int getPendingAcquireCount() { return pendingAcquireCount; }
        public long getMaxAcquireTime() { return maxAcquireTime; }
        public long getMaxIdleTime() { return maxIdleTime; }
    }

    /**
     * 查询性能统计信息
     */
    class QueryPerformanceStats {
        private final String queryName;
        private final long executionCount;
        private final long totalExecutionTime;
        private final long averageExecutionTime;
        private final long minExecutionTime;
        private final long maxExecutionTime;
        private final long errorCount;

        public QueryPerformanceStats(String queryName, long executionCount,
                                   long totalExecutionTime, long averageExecutionTime,
                                   long minExecutionTime, long maxExecutionTime,
                                   long errorCount) {
            this.queryName = queryName;
            this.executionCount = executionCount;
            this.totalExecutionTime = totalExecutionTime;
            this.averageExecutionTime = averageExecutionTime;
            this.minExecutionTime = minExecutionTime;
            this.maxExecutionTime = maxExecutionTime;
            this.errorCount = errorCount;
        }

        public String getQueryName() { return queryName; }
        public long getExecutionCount() { return executionCount; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public long getAverageExecutionTime() { return averageExecutionTime; }
        public long getMinExecutionTime() { return minExecutionTime; }
        public long getMaxExecutionTime() { return maxExecutionTime; }
        public long getErrorCount() { return errorCount; }
    }
}

