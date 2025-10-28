package com.honyrun.service.reactive;

import com.honyrun.model.entity.system.MockInterface;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 响应式模拟接口服务接口
 * 提供模拟接口管理、动态路由、响应生成等核心功能
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 *
 * 功能特性:
 * - 动态创建和管理模拟接口
 * - 支持多种HTTP方法 (GET, POST, PUT, DELETE)
 * - 可配置响应延迟和状态码
 * - 支持响应模板和动态参数替换
 * - 接口访问统计和监控
 * - 响应式路由处理
 */
public interface ReactiveMockInterfaceService {

    /**
     * 创建模拟接口
     * 创建一个新的模拟接口配置
     *
     * @param mockInterface 模拟接口实体
     * @return 创建的模拟接口
     */
    Mono<MockInterface> createMockInterface(MockInterface mockInterface);

    /**
     * 更新模拟接口
     * 更新现有的模拟接口配置
     *
     * @param id 接口ID
     * @param mockInterface 更新的模拟接口实体
     * @return 更新后的模拟接口
     */
    Mono<MockInterface> updateMockInterface(Long id, MockInterface mockInterface);

    /**
     * 删除模拟接口
     * 删除指定的模拟接口
     *
     * @param id 接口ID
     * @return 删除操作结果
     */
    Mono<Void> deleteMockInterface(Long id);

    /**
     * 根据ID获取模拟接口
     * 获取指定ID的模拟接口详情
     *
     * @param id 接口ID
     * @return 模拟接口实体
     */
    Mono<MockInterface> getMockInterfaceById(Long id);

    /**
     * 根据路径和方法获取模拟接口
     * 根据接口路径和HTTP方法查找模拟接口
     *
     * @param path 接口路径
     * @param method HTTP方法
     * @return 模拟接口实体
     */
    Mono<MockInterface> getMockInterfaceByPathAndMethod(String path, String method);

    /**
     * 获取所有模拟接口
     * 获取系统中所有的模拟接口列表
     *
     * @return 模拟接口流
     */
    Flux<MockInterface> getAllMockInterfaces();

    /**
     * 根据分类获取模拟接口
     * 获取指定分类下的所有模拟接口
     *
     * @param category 接口分类
     * @return 模拟接口流
     */
    Flux<MockInterface> getMockInterfacesByCategory(String category);

    /**
     * 获取启用的模拟接口
     * 获取所有启用状态的模拟接口
     *
     * @return 启用的模拟接口流
     */
    Flux<MockInterface> getEnabledMockInterfaces();

    /**
     * 启用模拟接口
     * 启用指定的模拟接口
     *
     * @param id 接口ID
     * @return 操作结果
     */
    Mono<MockInterface> enableMockInterface(Long id);

    /**
     * 禁用模拟接口
     * 禁用指定的模拟接口
     *
     * @param id 接口ID
     * @return 操作结果
     */
    Mono<MockInterface> disableMockInterface(Long id);

    /**
     * 处理模拟接口请求
     * 根据配置处理模拟接口的请求并生成响应
     *
     * @param mockInterface 模拟接口配置
     * @param request 服务器请求
     * @return 服务器响应
     */
    Mono<ServerResponse> handleMockRequest(MockInterface mockInterface, ServerRequest request);

    /**
     * 生成模拟响应
     * 根据模拟接口配置生成响应内容
     *
     * @param mockInterface 模拟接口配置
     * @param requestParams 请求参数
     * @return 响应内容
     */
    Mono<String> generateMockResponse(MockInterface mockInterface, Map<String, Object> requestParams);

    /**
     * 更新接口访问统计
     * 更新模拟接口的访问次数和统计信息
     *
     * @param id 接口ID
     * @param success 是否成功访问
     * @param responseTime 响应时间（毫秒）
     * @return 更新操作结果
     */
    Mono<Void> updateAccessStatistics(Long id, boolean success, long responseTime);

    /**
     * 获取接口访问统计
     * 获取指定模拟接口的访问统计信息
     *
     * @param id 接口ID
     * @return 统计信息
     */
    Mono<MockInterfaceStatistics> getAccessStatistics(Long id);

    /**
     * 验证接口路径唯一性
     * 检查接口路径和方法组合是否已存在
     *
     * @param path 接口路径
     * @param method HTTP方法
     * @param excludeId 排除的接口ID（用于更新时检查）
     * @return 验证结果，true表示路径唯一
     */
    Mono<Boolean> validatePathUniqueness(String path, String method, Long excludeId);

    /**
     * 批量导入模拟接口
     * 批量创建多个模拟接口
     *
     * @param mockInterfaces 模拟接口流
     * @return 导入结果流
     */
    Flux<MockInterface> batchImportMockInterfaces(Flux<MockInterface> mockInterfaces);

    /**
     * 导出模拟接口配置
     * 导出指定分类或所有模拟接口的配置
     *
     * @param category 分类（可选，null表示导出所有）
     * @return 模拟接口配置流
     */
    Flux<MockInterface> exportMockInterfaces(String category);

    /**
     * 搜索模拟接口
     * 根据关键词搜索模拟接口
     *
     * @param keyword 搜索关键词
     * @return 匹配的模拟接口流
     */
    Flux<MockInterface> searchMockInterfaces(String keyword);

    /**
     * 模拟接口统计信息内部类
     * 封装模拟接口的访问统计数据
     */
    class MockInterfaceStatistics {
        private Long interfaceId;
        private String interfaceName;
        private String interfacePath;
        private Long totalAccess;
        private Long successCount;
        private Long failureCount;
        private Double successRate;
        private Long avgResponseTime;
        private Long maxResponseTime;
        private Long minResponseTime;

        // 构造函数
        public MockInterfaceStatistics() {}

        public MockInterfaceStatistics(Long interfaceId, String interfaceName, String interfacePath) {
            this.interfaceId = interfaceId;
            this.interfaceName = interfaceName;
            this.interfacePath = interfacePath;
            this.totalAccess = 0L;
            this.successCount = 0L;
            this.failureCount = 0L;
            this.successRate = 0.0;
            this.avgResponseTime = 0L;
            this.maxResponseTime = 0L;
            this.minResponseTime = 0L;
        }

        // Getter和Setter方法
        public Long getInterfaceId() { return interfaceId; }
        public void setInterfaceId(Long interfaceId) { this.interfaceId = interfaceId; }

        public String getInterfaceName() { return interfaceName; }
        public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }

        public String getInterfacePath() { return interfacePath; }
        public void setInterfacePath(String interfacePath) { this.interfacePath = interfacePath; }

        public Long getTotalAccess() { return totalAccess; }
        public void setTotalAccess(Long totalAccess) { this.totalAccess = totalAccess; }

        public Long getSuccessCount() { return successCount; }
        public void setSuccessCount(Long successCount) { this.successCount = successCount; }

        public Long getFailureCount() { return failureCount; }
        public void setFailureCount(Long failureCount) { this.failureCount = failureCount; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }

        public Long getAvgResponseTime() { return avgResponseTime; }
        public void setAvgResponseTime(Long avgResponseTime) { this.avgResponseTime = avgResponseTime; }

        public Long getMaxResponseTime() { return maxResponseTime; }
        public void setMaxResponseTime(Long maxResponseTime) { this.maxResponseTime = maxResponseTime; }

        public Long getMinResponseTime() { return minResponseTime; }
        public void setMinResponseTime(Long minResponseTime) { this.minResponseTime = minResponseTime; }

        @Override
        public String toString() {
            return "MockInterfaceStatistics{" +
                    "interfaceId=" + interfaceId +
                    ", interfaceName='" + interfaceName + '\'' +
                    ", interfacePath='" + interfacePath + '\'' +
                    ", totalAccess=" + totalAccess +
                    ", successCount=" + successCount +
                    ", failureCount=" + failureCount +
                    ", successRate=" + successRate +
                    ", avgResponseTime=" + avgResponseTime +
                    ", maxResponseTime=" + maxResponseTime +
                    ", minResponseTime=" + minResponseTime +
                    '}';
        }
    }
}

