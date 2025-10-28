package com.honyrun.config.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;

/**
 * 配置管理规范符合性检查报告生成器
 *
 * <p>
 * 生成详细的配置管理规范符合性检查报告，包括：
 * </p>
 * <ul>
 * <li>硬编码配置违规统计</li>
 * <li>环境变量配置完整性检查</li>
 * <li>配置文件一致性验证</li>
 * <li>合规性评分和改进建议</li>
 * </ul>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-26 01:36:12
 * @version 1.0.0
 */
@Component
public class ConfigurationComplianceReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationComplianceReportGenerator.class);

    private final Environment environment;
    private final ConfigurationConsistencyValidator validator;

    /**
     * 构造函数注入
     *
     * @param environment 环境配置
     * @param validator   配置一致性验证器
     */
    public ConfigurationComplianceReportGenerator(Environment environment,
            ConfigurationConsistencyValidator validator) {
        this.environment = environment;
        this.validator = validator;
    }

    /**
     * 生成配置合规性检查报告
     *
     * @return 报告文件路径
     */
    public Mono<String> generateReport() {
        return validator.validateConfigurations()
                .map(this::generateDetailedReport)
                .map(this::saveReport);
    }

    /**
     * 生成详细报告
     *
     * @param validationReport 验证报告
     * @return 报告文件路径
     */
    private String generateDetailedReport(ConfigurationConsistencyValidator.ValidationReport validationReport) {
        StringBuilder report = new StringBuilder();

        // 报告头部
        appendReportHeader(report);

        // 执行摘要
        appendExecutiveSummary(report, validationReport);

        // 硬编码配置违规详情
        appendHardcodedViolations(report, validationReport.getHardcodedViolations());

        // 环境变量配置检查
        appendEnvironmentVariableCheck(report, validationReport.getMissingEnvironmentVariables());

        // 配置文件一致性检查
        appendConfigFileConsistency(report, validationReport.getConfigFileIssues());

        // 合规性评分
        appendComplianceScore(report, validationReport);

        // 改进建议
        appendImprovementRecommendations(report, validationReport);

        // 附录
        appendAppendix(report);

        // 保存报告
        return saveReport(report.toString());
    }

    /**
     * 添加报告头部
     */
    private void appendReportHeader(StringBuilder report) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        report.append("# HonyRun项目配置管理规范符合性检查报告\n\n");
        report.append("## 报告信息\n\n");
        report.append("| 项目 | 信息 |\n");
        report.append("|------|------|\n");
        report.append("| **项目名称** | HonyRun响应式Web应用 |\n");
        report.append("| **报告类型** | 配置管理规范符合性检查 |\n");
        report.append("| **生成时间** | ").append(timestamp).append(" |\n");
        report.append("| **报告版本** | 1.0.0 |\n");
        report.append("| **检查范围** | 全项目配置文件和Java源代码 |\n");
        report.append("| **合规标准** | 32G高性能开发测试环境项目规则 |\n\n");

        report.append("---\n\n");
    }

    /**
     * 添加执行摘要
     */
    private void appendExecutiveSummary(StringBuilder report,
            ConfigurationConsistencyValidator.ValidationReport validationReport) {
        report.append("## 执行摘要\n\n");

        int totalViolations = validationReport.getHardcodedViolations().size();
        int missingEnvVars = validationReport.getMissingEnvironmentVariables().size();
        int configIssues = validationReport.getConfigFileIssues().size();

        report.append("### 检查结果概览\n\n");
        report.append("| 检查项目 | 数量 | 状态 |\n");
        report.append("|----------|------|------|\n");
        report.append("| **硬编码配置违规** | ").append(totalViolations).append(" | ")
                .append(totalViolations == 0 ? "✅ 通过" : "❌ 不通过").append(" |\n");
        report.append("| **缺失环境变量** | ").append(missingEnvVars).append(" | ")
                .append(missingEnvVars == 0 ? "✅ 通过" : "❌ 不通过").append(" |\n");
        report.append("| **配置文件问题** | ").append(configIssues).append(" | ")
                .append(configIssues == 0 ? "✅ 通过" : "❌ 不通过").append(" |\n");
        report.append("| **总体合规分数** | ").append(String.format("%.1f", validationReport.getComplianceScore()))
                .append("/100 | ").append(getComplianceStatus(validationReport.getOverallStatus())).append(" |\n\n");

        // 合规性评估
        report.append("### 合规性评估\n\n");
        if (validationReport.getComplianceScore() >= 90) {
            report.append("🟢 **优秀** - 项目配置管理符合规范要求，仅有少量轻微问题需要修复。\n\n");
        } else if (validationReport.getComplianceScore() >= 70) {
            report.append("🟡 **良好** - 项目配置管理基本符合规范，存在一些需要改进的问题。\n\n");
        } else if (validationReport.getComplianceScore() >= 50) {
            report.append("🟠 **一般** - 项目配置管理存在较多问题，需要重点关注和改进。\n\n");
        } else {
            report.append("🔴 **不合格** - 项目配置管理严重不符合规范，需要立即整改。\n\n");
        }
    }

    /**
     * 添加硬编码配置违规详情
     */
    private void appendHardcodedViolations(StringBuilder report,
            List<ConfigurationConsistencyValidator.ConfigurationViolation> violations) {
        report.append("## 2. 硬编码配置违规详情\n\n");

        if (violations.isEmpty()) {
            report.append("✅ **未发现硬编码配置违规**\n\n");
        } else {
            report.append("⚠️ **发现 ").append(violations.size()).append(" 个硬编码配置违规**\n\n");

            // 按严重程度分组
            Map<String, List<ConfigurationConsistencyValidator.ConfigurationViolation>> violationsByType = violations
                    .stream()
                    .collect(Collectors.groupingBy(v -> v.getPattern()));

            for (Map.Entry<String, List<ConfigurationConsistencyValidator.ConfigurationViolation>> entry : violationsByType
                    .entrySet()) {
                String violationType = entry.getKey();
                List<ConfigurationConsistencyValidator.ConfigurationViolation> typeViolations = entry.getValue();

                report.append("### ").append(violationType).append(" 违规 (").append(typeViolations.size())
                        .append(" 个)\n\n");

                for (ConfigurationConsistencyValidator.ConfigurationViolation violation : typeViolations) {
                    report.append("- **文件**: `").append(violation.getFile()).append("`\n");
                    report.append("  - **行号**: ").append(violation.getLine()).append("\n");
                    report.append("  - **内容**: `").append(violation.getContent()).append("`\n");
                    report.append("  - **严重程度**: ").append(getSeverityIcon(violation.getSeverity().toString()))
                            .append(" ").append(violation.getSeverity()).append("\n");
                    report.append("  - **描述**: ").append(violation.getDescription()).append("\n\n");
                }
            }
        }
    }

    /**
     * 添加环境变量配置检查
     */
    private void appendEnvironmentVariableCheck(StringBuilder report, List<String> missingEnvVars) {
        report.append("## 环境变量配置检查\n\n");

        if (missingEnvVars.isEmpty()) {
            report.append("✅ **环境变量配置完整** - 所有必需的环境变量均已配置。\n\n");
        } else {
            report.append("❌ **缺失环境变量配置** - 发现 ").append(missingEnvVars.size()).append(" 个缺失的环境变量。\n\n");

            report.append("### 缺失的环境变量\n\n");
            report.append("| 环境变量名称 | 用途说明 | 优先级 |\n");
            report.append("|--------------|----------|--------|\n");

            for (String envVar : missingEnvVars) {
                report.append("| `").append(envVar).append("` | ").append(getEnvVarDescription(envVar))
                        .append(" | ").append(getEnvVarPriority(envVar)).append(" |\n");
            }
            report.append("\n");

            report.append("### 修复建议\n\n");
            report.append("1. 在 `application-env-template.properties` 中添加缺失的环境变量配置\n");
            report.append("2. 在系统环境变量或IDE运行配置中设置相应的值\n");
            report.append("3. 确保所有环境（开发、测试、生产）都配置了相同的环境变量\n\n");
        }
    }

    /**
     * 添加配置文件一致性检查
     */
    private void appendConfigFileConsistency(StringBuilder report,
            List<String> configIssues) {
        report.append("## 4. 配置文件一致性检查\n\n");

        if (configIssues.isEmpty()) {
            report.append("✅ **配置文件一致性检查通过**\n\n");
        } else {
            report.append("⚠️ **发现 ").append(configIssues.size()).append(" 个配置文件问题**\n\n");

            for (String issue : configIssues) {
                report.append("- ").append(issue).append("\n");
            }
            report.append("\n");
        }
    }

    /**
     * 添加合规性评分
     */
    private void appendComplianceScore(StringBuilder report,
            ConfigurationConsistencyValidator.ValidationReport validationReport) {
        report.append("## 合规性评分详情\n\n");

        double score = validationReport.getComplianceScore();
        String status = validationReport.getOverallStatus();

        report.append("### 评分标准\n\n");
        report.append("| 分数范围 | 等级 | 状态 | 说明 |\n");
        report.append("|----------|------|------|------|\n");
        report.append("| 90-100 | 🟢 优秀 | COMPLIANT | 完全符合配置管理规范 |\n");
        report.append("| 70-89 | 🟡 良好 | MOSTLY_COMPLIANT | 基本符合规范，少量问题 |\n");
        report.append("| 50-69 | 🟠 一般 | PARTIALLY_COMPLIANT | 部分符合规范，需要改进 |\n");
        report.append("| 0-49 | 🔴 不合格 | NON_COMPLIANT | 严重不符合规范，需要整改 |\n\n");

        report.append("### 当前评分\n\n");
        report.append("**总分:** ").append(String.format("%.1f", score)).append("/100\n");
        report.append("**等级:** ").append(getComplianceGrade(score)).append("\n");
        report.append("**状态:** ").append(getComplianceStatus(status)).append("\n\n");

        // 评分构成
        report.append("### 评分构成分析\n\n");
        report.append("评分基于以下因素的加权计算：\n\n");
        report.append("- **硬编码配置违规** (权重最高)\n");
        report.append("  - CRITICAL级: -20分/项\n");
        report.append("  - HIGH级: -10分/项\n");
        report.append("  - MEDIUM级: -5分/项\n");
        report.append("  - LOW级: -2分/项\n\n");
        report.append("- **缺失环境变量** (权重中等): -8分/项\n\n");
        report.append("- **配置文件问题** (权重中等)\n");
        report.append("  - HIGH级: -15分/项\n");
        report.append("  - MEDIUM级: -8分/项\n");
        report.append("  - LOW级: -3分/项\n\n");
    }

    /**
     * 添加改进建议
     */
    private void appendImprovementRecommendations(StringBuilder report,
            ConfigurationConsistencyValidator.ValidationReport validationReport) {
        report.append("## 改进建议\n\n");

        List<String> recommendations = generateRecommendations(validationReport);

        if (recommendations.isEmpty()) {
            report.append("🎉 **恭喜！** 项目配置管理已达到最佳实践标准，无需进一步改进。\n\n");
        } else {
            report.append("### 优先级改进建议\n\n");

            for (int i = 0; i < recommendations.size(); i++) {
                report.append(i + 1).append(". ").append(recommendations.get(i)).append("\n\n");
            }
        }

        // 最佳实践建议
        report.append("### 配置管理最佳实践\n\n");
        report.append("1. **统一配置管理**\n");
        report.append("   - 所有配置通过环境变量或配置文件管理\n");
        report.append("   - 禁止在代码中硬编码任何配置值\n");
        report.append("   - 使用 `UnifiedConfigManager` 统一管理配置\n\n");

        report.append("2. **环境变量规范**\n");
        report.append("   - 使用 `HONYRUN_` 前缀统一命名\n");
        report.append("   - 提供合理的默认值\n");
        report.append("   - 在 `application-env-template.properties` 中文档化\n\n");

        report.append("3. **配置验证机制**\n");
        report.append("   - 定期运行配置一致性验证\n");
        report.append("   - 在CI/CD流程中集成配置检查\n");
        report.append("   - 建立配置变更审查机制\n\n");
    }

    /**
     * 添加附录
     */
    private void appendAppendix(StringBuilder report) {
        report.append("## 附录\n\n");

        report.append("### A. 相关文档\n\n");
        report.append("- [32G高性能开发测试环境项目规则](backend/docs/standard/)\n");
        report.append("- [统一配置管理规范](backend/docs/standard/)\n");
        report.append("- [环境变量配置模板](backend/src/main/resources/application-env-template.properties)\n\n");

        report.append("### B. 工具和脚本\n\n");
        report.append("- **配置一致性验证器**: `ConfigurationConsistencyValidator.java`\n");
        report.append("- **统一配置管理器**: `UnifiedConfigManager.java`\n");
        report.append("- **配置验证脚本**: 集成在应用启动流程中\n\n");

        report.append("### C. 联系信息\n\n");
        report.append("- **项目负责人**: Mr.Rey\n");
        report.append("- **邮箱**: reyisok@live.com\n");
        report.append("- **GitHub**: reyiosk\n\n");

        report.append("---\n\n");
        report.append("*本报告由HonyRun项目配置管理规范符合性检查系统自动生成*\n");
        report.append("*生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("*\n");
    }

    /**
     * 生成改进建议
     */
    private List<String> generateRecommendations(ConfigurationConsistencyValidator.ValidationReport validationReport) {
        List<String> recommendations = new ArrayList<>();

        // 基于硬编码违规的建议
        if (!validationReport.getHardcodedViolations().isEmpty()) {
            recommendations.add("**立即修复硬编码配置违规** - 将所有硬编码值替换为环境变量配置，确保配置的灵活性和可维护性");
        }

        // 基于缺失环境变量的建议
        if (!validationReport.getMissingEnvironmentVariables().isEmpty()) {
            recommendations.add("**补充缺失的环境变量配置** - 在配置文件中添加所有必需的环境变量，并提供合理的默认值");
        }

        // 基于配置文件问题的建议
        if (!validationReport.getConfigFileIssues().isEmpty()) {
            recommendations.add("**修复配置文件问题** - 检查并修复配置文件中的格式错误和不一致问题");
        }

        // 基于合规分数的建议
        if (validationReport.getComplianceScore() < 90) {
            recommendations.add("**建立配置管理流程** - 制定配置变更审查流程，确保所有配置变更都经过验证");
            recommendations.add("**集成自动化检查** - 在CI/CD流程中集成配置一致性检查，防止不合规配置进入生产环境");
        }

        return recommendations;
    }

    /**
     * 保存报告到文件
     */
    private String saveReport(String reportContent) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("配置管理规范符合性检查报告_%s.md", timestamp);
            Path reportPath = Paths.get("backend/docs/compliance", fileName);
            Files.createDirectories(reportPath.getParent());
            Files.write(reportPath, reportContent.getBytes());

            LoggingUtil.info(logger, "配置合规性检查报告已保存: {}", reportPath);
            return reportPath.toString();
        } catch (IOException e) {
            LoggingUtil.error(logger, "保存配置合规性检查报告失败", e);
            throw new RuntimeException("保存报告失败", e);
        }
    }

    /**
     * 获取严重程度图标
     */
    private String getSeverityIcon(String severity) {
        switch (severity.toUpperCase()) {
            case "CRITICAL":
                return "🔴";
            case "HIGH":
                return "🟠";
            case "MEDIUM":
                return "🟡";
            case "LOW":
                return "🟢";
            default:
                return "⚪";
        }
    }

    /**
     * 获取合规状态显示
     */
    private String getComplianceStatus(String status) {
        switch (status) {
            case "COMPLIANT":
                return "✅ 完全合规";
            case "MOSTLY_COMPLIANT":
                return "🟡 基本合规";
            case "PARTIALLY_COMPLIANT":
                return "🟠 部分合规";
            case "NON_COMPLIANT":
                return "❌ 不合规";
            default:
                return "❓ 未知";
        }
    }

    /**
     * 获取合规等级
     */
    private String getComplianceGrade(double score) {
        if (score >= 90)
            return "🟢 优秀";
        if (score >= 70)
            return "🟡 良好";
        if (score >= 50)
            return "🟠 一般";
        return "🔴 不合格";
    }

    /**
     * 获取环境变量描述
     */
    private String getEnvVarDescription(String envVar) {
        switch (envVar) {
            case "HONYRUN_APP_NAME":
                return "应用程序名称";
            case "HONYRUN_SERVER_PORT":
                return "服务器端口";
            case "HONYRUN_MYSQL_R2DBC_URL":
                return "MySQL R2DBC连接URL";
            case "HONYRUN_MYSQL_USERNAME":
                return "MySQL用户名";
            case "HONYRUN_MYSQL_PASSWORD":
                return "MySQL密码";
            case "HONYRUN_REDIS_HOST":
                return "Redis主机地址";
            case "HONYRUN_REDIS_PORT":
                return "Redis端口";
            case "HONYRUN_REDIS_PASSWORD":
                return "Redis密码";
            case "HONYRUN_JWT_SECRET":
                return "JWT签名密钥";
            default:
                return "配置参数";
        }
    }

    /**
     * 获取环境变量优先级
     */
    private String getEnvVarPriority(String envVar) {
        switch (envVar) {
            case "HONYRUN_JWT_SECRET":
            case "HONYRUN_MYSQL_PASSWORD":
            case "HONYRUN_REDIS_PASSWORD":
                return "🔴 高";
            case "HONYRUN_MYSQL_R2DBC_URL":
            case "HONYRUN_SERVER_PORT":
                return "🟠 中";
            default:
                return "🟡 低";
        }
    }
}
