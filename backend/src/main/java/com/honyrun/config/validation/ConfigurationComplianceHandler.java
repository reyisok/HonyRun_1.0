package com.honyrun.config.validation;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置合规性检查处理器
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @version 1.0.0
 */
@Component
public class ConfigurationComplianceHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationComplianceHandler.class);
    
    private final ConfigurationConsistencyValidator validator;
    private final ConfigurationComplianceReportGenerator reportGenerator;
    
    public ConfigurationComplianceHandler(ConfigurationConsistencyValidator validator,
                                        ConfigurationComplianceReportGenerator reportGenerator) {
        this.validator = validator;
        this.reportGenerator = reportGenerator;
    }
    
    /**
     * 执行配置合规性检查
     */
    public Mono<ServerResponse> validateConfiguration(ServerRequest request) {
        return validator.validateConfigurations()
                .flatMap(report -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "配置合规性检查完成");
                    response.put("report", report);
                    response.put("timestamp", LocalDateTime.now());
                    
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "配置合规性检查失败", error);
                    
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "配置合规性检查失败: " + error.getMessage());
                    errorResponse.put("timestamp", LocalDateTime.now());
                    
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(errorResponse);
                });
    }
    
    /**
     * 生成配置合规性检查报告
     */
    public Mono<ServerResponse> generateComplianceReport(ServerRequest request) {
        return reportGenerator.generateReport()
                .flatMap(reportPath -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "配置合规性检查报告生成成功");
                    response.put("reportPath", reportPath);
                    response.put("timestamp", LocalDateTime.now());
                    
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "生成配置合规性检查报告失败", error);
                    
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "生成配置合规性检查报告失败: " + error.getMessage());
                    errorResponse.put("timestamp", LocalDateTime.now());
                    
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(errorResponse);
                });
    }
    
    /**
     * 获取配置合规性检查摘要
     */
    public Mono<ServerResponse> getValidationSummary(ServerRequest request) {
        return validator.validateConfigurations()
                .map(report -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("totalViolations", report.getHardcodedViolations().size());
                    summary.put("highSeverityCount", report.getHardcodedViolations().stream()
                            .mapToInt(v -> v.getSeverity() == ConfigurationConsistencyValidator.ViolationSeverity.HIGH ? 1 : 0)
                            .sum());
                    summary.put("mediumSeverityCount", report.getHardcodedViolations().stream()
                            .mapToInt(v -> v.getSeverity() == ConfigurationConsistencyValidator.ViolationSeverity.MEDIUM ? 1 : 0)
                            .sum());
                    summary.put("lowSeverityCount", report.getHardcodedViolations().stream()
                            .mapToInt(v -> v.getSeverity() == ConfigurationConsistencyValidator.ViolationSeverity.LOW ? 1 : 0)
                            .sum());
                    summary.put("overallStatus", report.getHardcodedViolations().isEmpty() ? "PASS" : "FAIL");
                    summary.put("timestamp", LocalDateTime.now());
                    
                    return summary;
                })
                .flatMap(summary -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(summary))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取配置合规性检查摘要失败", error);
                    
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "获取配置合规性检查摘要失败: " + error.getMessage());
                    errorResponse.put("timestamp", LocalDateTime.now());
                    
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(errorResponse);
                });
    }
}
