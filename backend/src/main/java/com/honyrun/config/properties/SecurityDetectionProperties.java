package com.honyrun.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * 统一安全检测配置
 * 支持通过配置文件管理黑名单模式与阈值。
 *
 * 【重要】：所有配置值必须从环境配置文件中读取
 * 配置文件路径：
 * - 开发环境：application-dev.properties
 * - 测试环境：application-test.properties  
 * - 生产环境：application-prod.properties
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-01  16:30:00
 * @modified 2025-10-27 12:26:43
 * @version 1.0.1 - 移除硬编码，严格遵循统一配置管理规范
 */
@Component
@ConfigurationProperties(prefix = "honyrun.security.detection")
@Validated
public class SecurityDetectionProperties {

    /** 
     * 是否启用恶意输入检测
     * 必须从配置文件获取：honyrun.security.detection.enabled
     */
    private boolean enabled;

    /** 
     * 关键字黑名单（小写匹配）
     * 必须从配置文件获取：honyrun.security.detection.patterns
     */
    @NotNull(message = "安全检测模式列表不能为空")
    private List<String> patterns;

    /** 
     * 引号出现次数阈值，超过则视为可疑
     * 必须从配置文件获取：honyrun.security.detection.max-quotes-threshold
     */
    @Min(value = 1, message = "引号阈值必须大于0")
    private int maxQuotesThreshold;

    /** 
     * 最大输入长度限制（字符数）
     * 必须从配置文件获取：honyrun.security.detection.max-input-length
     */
    @Min(value = 100, message = "最大输入长度必须大于100")
    private Integer maxInputLength;

    /**
     * 配置完整性验证
     * 在Bean初始化后验证配置的有效性
     */
    @PostConstruct
    public void validateConfiguration() {
        if (patterns == null || patterns.isEmpty()) {
            throw new IllegalStateException("安全检测模式列表不能为空");
        }

        if (maxQuotesThreshold <= 0) {
            throw new IllegalStateException("引号阈值必须大于0，当前值: " + maxQuotesThreshold);
        }

        // 验证模式列表中不能有空值或空字符串
        for (String pattern : patterns) {
            if (pattern == null || pattern.trim().isEmpty()) {
                throw new IllegalStateException("安全检测模式不能为空或空字符串");
            }
        }
    }

    /**
     * 验证配置的有效性
     *
     * @return 配置是否有效
     */
    public boolean isValid() {
        return patterns != null && !patterns.isEmpty() &&
               maxQuotesThreshold > 0 &&
               patterns.stream().allMatch(pattern -> pattern != null && !pattern.trim().isEmpty());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    public int getMaxQuotesThreshold() {
        return maxQuotesThreshold;
    }

    public void setMaxQuotesThreshold(int maxQuotesThreshold) {
        this.maxQuotesThreshold = maxQuotesThreshold;
    }

    public Integer getMaxInputLength() {
        return maxInputLength;
    }

    public void setMaxInputLength(Integer maxInputLength) {
        this.maxInputLength = maxInputLength;
    }
}


