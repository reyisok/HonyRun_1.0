package com.honyrun.model.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 核验统计信息响应DTO
 *
 * 用于返回核验业务的统计数据，包含各种统计指标和分析结果
 * 支持响应式数据传输和JSON序列化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  17:05:00
 * @modified 2025-07-01 17:05:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class VerificationStatistics {

    // ==================== 基础统计信息 ====================

    /**
     * 统计时间范围开始
     */
    private LocalDateTime statisticsStartTime;

    /**
     * 统计时间范围结束
     */
    private LocalDateTime statisticsEndTime;

    /**
     * 总请求数量
     */
    private Long totalRequests;

    /**
     * 待处理请求数量
     */
    private Long pendingRequests;

    /**
     * 处理中请求数量
     */
    private Long processingRequests;

    /**
     * 已完成请求数量
     */
    private Long completedRequests;

    /**
     * 已取消请求数量
     */
    private Long cancelledRequests;

    /**
     * 失败请求数量
     */
    private Long failedRequests;

    // ==================== 核验结果统计 ====================

    /**
     * 核验通过数量
     */
    private Long passedResults;

    /**
     * 核验不通过数量
     */
    private Long failedResults;

    /**
     * 部分通过数量
     */
    private Long partiallyPassedResults;

    /**
     * 需要补充材料数量
     */
    private Long needsAdditionalMaterialsResults;

    /**
     * 异常结果数量
     */
    private Long abnormalResults;

    // ==================== 性能统计 ====================

    /**
     * 平均处理时间（毫秒）
     */
    private Long averageProcessingTimeMs;

    /**
     * 最快处理时间（毫秒）
     */
    private Long fastestProcessingTimeMs;

    /**
     * 最慢处理时间（毫秒）
     */
    private Long slowestProcessingTimeMs;

    /**
     * 平均核验分数
     */
    private BigDecimal averageScore;

    /**
     * 最高核验分数
     */
    private BigDecimal highestScore;

    /**
     * 最低核验分数
     */
    private BigDecimal lowestScore;

    // ==================== 风险统计 ====================

    /**
     * 高风险结果数量
     */
    private Long highRiskResults;

    /**
     * 中风险结果数量
     */
    private Long mediumRiskResults;

    /**
     * 低风险结果数量
     */
    private Long lowRiskResults;

    /**
     * 需要复核数量
     */
    private Long requiresRecheckResults;

    // ==================== 分类统计 ====================

    /**
     * 按请求类型统计
     * Key: 请求类型, Value: 数量
     */
    private Map<String, Long> requestTypeStatistics;

    /**
     * 按优先级统计
     * Key: 优先级, Value: 数量
     */
    private Map<Integer, Long> priorityStatistics;

    /**
     * 按处理人统计
     * Key: 处理人姓名, Value: 数量
     */
    private Map<String, Long> processorStatistics;

    /**
     * 按核验人统计
     * Key: 核验人姓名, Value: 数量
     */
    private Map<String, Long> verifierStatistics;

    // ==================== 时间趋势统计 ====================

    /**
     * 每日请求数量趋势
     * Key: 日期字符串(yyyy-MM-dd), Value: 数量
     */
    private Map<String, Long> dailyRequestTrend;

    /**
     * 每日完成数量趋势
     * Key: 日期字符串(yyyy-MM-dd), Value: 数量
     */
    private Map<String, Long> dailyCompletionTrend;

    /**
     * 每日通过率趋势
     * Key: 日期字符串(yyyy-MM-dd), Value: 通过率(百分比)
     */
    private Map<String, BigDecimal> dailyPassRateTrend;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public VerificationStatistics() {
        this.statisticsEndTime = LocalDateTime.now();
    }

    /**
     * 带时间范围的构造函数
     *
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     */
    public VerificationStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        this.statisticsStartTime = startTime;
        this.statisticsEndTime = endTime;
    }

    // ==================== Getter和Setter方法 ====================

    public LocalDateTime getStatisticsStartTime() {
        return statisticsStartTime;
    }

    public void setStatisticsStartTime(LocalDateTime statisticsStartTime) {
        this.statisticsStartTime = statisticsStartTime;
    }

    public LocalDateTime getStatisticsEndTime() {
        return statisticsEndTime;
    }

    public void setStatisticsEndTime(LocalDateTime statisticsEndTime) {
        this.statisticsEndTime = statisticsEndTime;
    }

    public Long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public Long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(Long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public Long getProcessingRequests() {
        return processingRequests;
    }

    public void setProcessingRequests(Long processingRequests) {
        this.processingRequests = processingRequests;
    }

    public Long getCompletedRequests() {
        return completedRequests;
    }

    public void setCompletedRequests(Long completedRequests) {
        this.completedRequests = completedRequests;
    }

    public Long getCancelledRequests() {
        return cancelledRequests;
    }

    public void setCancelledRequests(Long cancelledRequests) {
        this.cancelledRequests = cancelledRequests;
    }

    public Long getFailedRequests() {
        return failedRequests;
    }

    public void setFailedRequests(Long failedRequests) {
        this.failedRequests = failedRequests;
    }

    public Long getPassedResults() {
        return passedResults;
    }

    public void setPassedResults(Long passedResults) {
        this.passedResults = passedResults;
    }

    public Long getFailedResults() {
        return failedResults;
    }

    public void setFailedResults(Long failedResults) {
        this.failedResults = failedResults;
    }

    public Long getPartiallyPassedResults() {
        return partiallyPassedResults;
    }

    public void setPartiallyPassedResults(Long partiallyPassedResults) {
        this.partiallyPassedResults = partiallyPassedResults;
    }

    public Long getNeedsAdditionalMaterialsResults() {
        return needsAdditionalMaterialsResults;
    }

    public void setNeedsAdditionalMaterialsResults(Long needsAdditionalMaterialsResults) {
        this.needsAdditionalMaterialsResults = needsAdditionalMaterialsResults;
    }

    public Long getAbnormalResults() {
        return abnormalResults;
    }

    public void setAbnormalResults(Long abnormalResults) {
        this.abnormalResults = abnormalResults;
    }

    public Long getAverageProcessingTimeMs() {
        return averageProcessingTimeMs;
    }

    public void setAverageProcessingTimeMs(Long averageProcessingTimeMs) {
        this.averageProcessingTimeMs = averageProcessingTimeMs;
    }

    public Long getFastestProcessingTimeMs() {
        return fastestProcessingTimeMs;
    }

    public void setFastestProcessingTimeMs(Long fastestProcessingTimeMs) {
        this.fastestProcessingTimeMs = fastestProcessingTimeMs;
    }

    public Long getSlowestProcessingTimeMs() {
        return slowestProcessingTimeMs;
    }

    public void setSlowestProcessingTimeMs(Long slowestProcessingTimeMs) {
        this.slowestProcessingTimeMs = slowestProcessingTimeMs;
    }

    public BigDecimal getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(BigDecimal averageScore) {
        this.averageScore = averageScore;
    }

    public BigDecimal getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(BigDecimal highestScore) {
        this.highestScore = highestScore;
    }

    public BigDecimal getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(BigDecimal lowestScore) {
        this.lowestScore = lowestScore;
    }

    public Long getHighRiskResults() {
        return highRiskResults;
    }

    public void setHighRiskResults(Long highRiskResults) {
        this.highRiskResults = highRiskResults;
    }

    public Long getMediumRiskResults() {
        return mediumRiskResults;
    }

    public void setMediumRiskResults(Long mediumRiskResults) {
        this.mediumRiskResults = mediumRiskResults;
    }

    public Long getLowRiskResults() {
        return lowRiskResults;
    }

    public void setLowRiskResults(Long lowRiskResults) {
        this.lowRiskResults = lowRiskResults;
    }

    public Long getRequiresRecheckResults() {
        return requiresRecheckResults;
    }

    public void setRequiresRecheckResults(Long requiresRecheckResults) {
        this.requiresRecheckResults = requiresRecheckResults;
    }

    public Map<String, Long> getRequestTypeStatistics() {
        return requestTypeStatistics;
    }

    public void setRequestTypeStatistics(Map<String, Long> requestTypeStatistics) {
        this.requestTypeStatistics = requestTypeStatistics;
    }

    public Map<Integer, Long> getPriorityStatistics() {
        return priorityStatistics;
    }

    public void setPriorityStatistics(Map<Integer, Long> priorityStatistics) {
        this.priorityStatistics = priorityStatistics;
    }

    public Map<String, Long> getProcessorStatistics() {
        return processorStatistics;
    }

    public void setProcessorStatistics(Map<String, Long> processorStatistics) {
        this.processorStatistics = processorStatistics;
    }

    public Map<String, Long> getVerifierStatistics() {
        return verifierStatistics;
    }

    public void setVerifierStatistics(Map<String, Long> verifierStatistics) {
        this.verifierStatistics = verifierStatistics;
    }

    public Map<String, Long> getDailyRequestTrend() {
        return dailyRequestTrend;
    }

    public void setDailyRequestTrend(Map<String, Long> dailyRequestTrend) {
        this.dailyRequestTrend = dailyRequestTrend;
    }

    public Map<String, Long> getDailyCompletionTrend() {
        return dailyCompletionTrend;
    }

    public void setDailyCompletionTrend(Map<String, Long> dailyCompletionTrend) {
        this.dailyCompletionTrend = dailyCompletionTrend;
    }

    public Map<String, BigDecimal> getDailyPassRateTrend() {
        return dailyPassRateTrend;
    }

    public void setDailyPassRateTrend(Map<String, BigDecimal> dailyPassRateTrend) {
        this.dailyPassRateTrend = dailyPassRateTrend;
    }

    // ==================== 业务方法 ====================

    /**
     * 计算完成率
     *
     * @return 完成率（百分比）
     */
    public BigDecimal getCompletionRate() {
        if (totalRequests == null || totalRequests == 0) {
            return BigDecimal.ZERO;
        }
        if (completedRequests == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(completedRequests)
                .divide(BigDecimal.valueOf(totalRequests), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 计算通过率
     *
     * @return 通过率（百分比）
     */
    public BigDecimal getPassRate() {
        Long totalResults = getTotalResults();
        if (totalResults == 0) {
            return BigDecimal.ZERO;
        }
        if (passedResults == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(passedResults)
                .divide(BigDecimal.valueOf(totalResults), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 获取总结果数量
     *
     * @return 总结果数量
     */
    public Long getTotalResults() {
        long total = 0;
        if (passedResults != null) total += passedResults;
        if (failedResults != null) total += failedResults;
        if (partiallyPassedResults != null) total += partiallyPassedResults;
        if (needsAdditionalMaterialsResults != null) total += needsAdditionalMaterialsResults;
        if (abnormalResults != null) total += abnormalResults;
        return total;
    }

    /**
     * 计算高风险比例
     *
     * @return 高风险比例（百分比）
     */
    public BigDecimal getHighRiskRate() {
        Long totalResults = getTotalResults();
        if (totalResults == 0) {
            return BigDecimal.ZERO;
        }
        if (highRiskResults == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(highRiskResults)
                .divide(BigDecimal.valueOf(totalResults), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    // ==================== Object方法重写 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VerificationStatistics that = (VerificationStatistics) obj;
        return Objects.equals(statisticsStartTime, that.statisticsStartTime) &&
               Objects.equals(statisticsEndTime, that.statisticsEndTime) &&
               Objects.equals(totalRequests, that.totalRequests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statisticsStartTime, statisticsEndTime, totalRequests);
    }

    @Override
    public String toString() {
        return "VerificationStatistics{" +
                "statisticsStartTime=" + statisticsStartTime +
                ", statisticsEndTime=" + statisticsEndTime +
                ", totalRequests=" + totalRequests +
                ", completedRequests=" + completedRequests +
                ", passedResults=" + passedResults +
                ", completionRate=" + getCompletionRate() + "%" +
                ", passRate=" + getPassRate() + "%" +
                '}';
    }
}


