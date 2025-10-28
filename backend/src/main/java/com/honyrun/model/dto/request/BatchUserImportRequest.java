package com.honyrun.model.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 批量用户导入请求DTO
 *
 * 用于批量导入用户的请求参数封装
 * 支持从Excel、CSV等文件格式导入用户数据
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 11:35:00
 * @modified 2025-07-01 11:35:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Schema(description = "批量用户导入请求")
public class BatchUserImportRequest {

    /**
     * 导入文件
     */
    @Schema(description = "导入文件", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;

    /**
     * 文件格式
     * EXCEL - Excel文件(.xlsx, .xls)
     * CSV - CSV文件(.csv)
     */
    @Schema(description = "文件格式", example = "EXCEL")
    private String fileFormat;

    /**
     * 是否包含标题行
     */
    @Schema(description = "是否包含标题行", example = "true")
    private Boolean hasHeader = true;

    /**
     * 工作表名称（Excel文件时使用）
     */
    @Schema(description = "工作表名称", example = "用户数据")
    private String sheetName;

    /**
     * 工作表索引（Excel文件时使用，从0开始）
     */
    @Schema(description = "工作表索引", example = "0")
    private Integer sheetIndex = 0;

    /**
     * 字段映射配置
     */
    @Schema(description = "字段映射配置")
    private FieldMapping fieldMapping;

    /**
     * 默认密码 - 从配置文件读取，避免硬编码
     */
    @Schema(description = "默认密码", example = "123")
    private String defaultPassword;

    /**
     * 默认用户类型
     */
    @Schema(description = "默认用户类型", example = "NORMAL_USER")
    private String defaultUserType = "NORMAL_USER";

    /**
     * 默认有效期
     */
    @Schema(description = "默认有效期")
    private LocalDateTime defaultValidUntil;

    /**
     * 默认权限列表
     */
    @Schema(description = "默认权限列表")
    private List<String> defaultPermissions;

    /**
     * 导入模式
     * SKIP_EXISTING - 跳过已存在用户
     * UPDATE_EXISTING - 更新已存在用户
     * FAIL_ON_EXISTING - 遇到已存在用户时失败
     */
    @Schema(description = "导入模式", example = "SKIP_EXISTING")
    private String importMode = "SKIP_EXISTING";

    /**
     * 是否验证数据
     */
    @Schema(description = "是否验证数据", example = "true")
    private Boolean validateData = true;

    /**
     * 是否发送通知
     */
    @Schema(description = "是否发送通知", example = "false")
    private Boolean sendNotification = false;

    /**
     * 批次大小
     */
    @Schema(description = "批次大小", example = "100")
    private Integer batchSize = 100;

    /**
     * 字段映射配置内部类
     */
    @Schema(description = "字段映射配置")
    public static class FieldMapping {

        /**
         * 用户名字段索引或名称
         */
        @Schema(description = "用户名字段", example = "0")
        private String usernameField = "0";

        /**
         * 密码字段索引或名称
         */
        @Schema(description = "密码字段", example = "1")
        private String passwordField;

        /**
         * 姓名字段索引或名称
         */
        @Schema(description = "姓名字段", example = "2")
        private String realNameField;

        /**
         * 手机号字段索引或名称
         */
        @Schema(description = "手机号字段", example = "3")
        private String phoneNumberField;

        /**
         * 邮箱字段索引或名称
         */
        @Schema(description = "邮箱字段", example = "4")
        private String emailField;

        /**
         * 部门字段索引或名称
         */
        @Schema(description = "部门字段", example = "5")
        private String departmentField;

        /**
         * 职位字段索引或名称
         */
        @Schema(description = "职位字段", example = "6")
        private String positionField;

        /**
         * 用户类型字段索引或名称
         */
        @Schema(description = "用户类型字段", example = "7")
        private String userTypeField;

        /**
         * 有效期字段索引或名称
         */
        @Schema(description = "有效期字段", example = "8")
        private String validUntilField;

        /**
         * 备注字段索引或名称
         */
        @Schema(description = "备注字段", example = "9")
        private String remarkField;

        // ==================== Getter和Setter方法 ====================

        public String getUsernameField() {
            return usernameField;
        }

        public void setUsernameField(String usernameField) {
            this.usernameField = usernameField;
        }

        public String getPasswordField() {
            return passwordField;
        }

        public void setPasswordField(String passwordField) {
            this.passwordField = passwordField;
        }

        public String getRealNameField() {
            return realNameField;
        }

        public void setRealNameField(String realNameField) {
            this.realNameField = realNameField;
        }

        public String getPhoneNumberField() {
            return phoneNumberField;
        }

        public void setPhoneNumberField(String phoneNumberField) {
            this.phoneNumberField = phoneNumberField;
        }

        public String getEmailField() {
            return emailField;
        }

        public void setEmailField(String emailField) {
            this.emailField = emailField;
        }

        public String getDepartmentField() {
            return departmentField;
        }

        public void setDepartmentField(String departmentField) {
            this.departmentField = departmentField;
        }

        public String getPositionField() {
            return positionField;
        }

        public void setPositionField(String positionField) {
            this.positionField = positionField;
        }

        public String getUserTypeField() {
            return userTypeField;
        }

        public void setUserTypeField(String userTypeField) {
            this.userTypeField = userTypeField;
        }

        public String getValidUntilField() {
            return validUntilField;
        }

        public void setValidUntilField(String validUntilField) {
            this.validUntilField = validUntilField;
        }

        public String getRemarkField() {
            return remarkField;
        }

        public void setRemarkField(String remarkField) {
            this.remarkField = remarkField;
        }

        @Override
        public String toString() {
            return "FieldMapping{" +
                    "usernameField='" + usernameField + '\'' +
                    ", realNameField='" + realNameField + '\'' +
                    ", phoneNumberField='" + phoneNumberField + '\'' +
                    ", emailField='" + emailField + '\'' +
                    ", departmentField='" + departmentField + '\'' +
                    ", positionField='" + positionField + '\'' +
                    '}';
        }
    }

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public BatchUserImportRequest() {
        this.fieldMapping = new FieldMapping();
    }

    // ==================== Getter和Setter方法 ====================

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public Boolean getHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(Boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public Integer getSheetIndex() {
        return sheetIndex;
    }

    public void setSheetIndex(Integer sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    public FieldMapping getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(FieldMapping fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getDefaultUserType() {
        return defaultUserType;
    }

    public void setDefaultUserType(String defaultUserType) {
        this.defaultUserType = defaultUserType;
    }

    public LocalDateTime getDefaultValidUntil() {
        return defaultValidUntil;
    }

    public void setDefaultValidUntil(LocalDateTime defaultValidUntil) {
        this.defaultValidUntil = defaultValidUntil;
    }

    public List<String> getDefaultPermissions() {
        return defaultPermissions;
    }

    public void setDefaultPermissions(List<String> defaultPermissions) {
        this.defaultPermissions = defaultPermissions;
    }

    public String getImportMode() {
        return importMode;
    }

    public void setImportMode(String importMode) {
        this.importMode = importMode;
    }

    public Boolean getValidateData() {
        return validateData;
    }

    public void setValidateData(Boolean validateData) {
        this.validateData = validateData;
    }

    public Boolean getSendNotification() {
        return sendNotification;
    }

    public void setSendNotification(Boolean sendNotification) {
        this.sendNotification = sendNotification;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    // ==================== 业务方法 ====================

    /**
     * 验证请求参数
     *
     * @return 验证结果
     */
    public boolean isValid() {
        if (file == null || file.isEmpty()) {
            return false;
        }

        if (fileFormat == null || fileFormat.trim().isEmpty()) {
            return false;
        }

        if (fieldMapping == null || fieldMapping.getUsernameField() == null) {
            return false;
        }

        return true;
    }

    /**
     * 获取文件大小
     *
     * @return 文件大小（字节）
     */
    public long getFileSize() {
        return file != null ? file.getSize() : 0;
    }

    /**
     * 获取文件名
     *
     * @return 文件名
     */
    public String getFileName() {
        return file != null ? file.getOriginalFilename() : null;
    }

    /**
     * 判断是否为Excel文件
     *
     * @return true-Excel文件，false-其他文件
     */
    public boolean isExcelFile() {
        return "EXCEL".equalsIgnoreCase(fileFormat);
    }

    /**
     * 判断是否为CSV文件
     *
     * @return true-CSV文件，false-其他文件
     */
    public boolean isCsvFile() {
        return "CSV".equalsIgnoreCase(fileFormat);
    }

    /**
     * 获取格式化的文件大小
     *
     * @return 格式化的文件大小
     */
    public String getFormattedFileSize() {
        long size = getFileSize();
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2fKB", size / 1024.0);
        } else {
            return String.format("%.2fMB", size / (1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return "BatchUserImportRequest{" +
                "fileName='" + getFileName() + '\'' +
                ", fileSize=" + getFormattedFileSize() +
                ", fileFormat='" + fileFormat + '\'' +
                ", hasHeader=" + hasHeader +
                ", defaultUserType='" + defaultUserType + '\'' +
                ", importMode='" + importMode + '\'' +
                ", validateData=" + validateData +
                ", batchSize=" + batchSize +
                '}';
    }
}

