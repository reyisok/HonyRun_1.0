package com.honyrun.converter;

import com.honyrun.model.enums.UserType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

/**
 * UserType枚举R2DBC转换器
 *
 * 提供UserType枚举与数据库字符串值之间的双向转换
 * 解决R2DBC默认枚举映射问题，确保数据库中的'SYSTEM'、'NORMAL'等值
 * 能够正确转换为对应的UserType枚举值
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  20:30:00
 * @modified 2025-07-01 20:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class UserTypeConverter {

    /**
     * 数据库字符串值转UserType枚举
     * 读取转换器：从数据库读取数据时使用
     */
    @ReadingConverter
    public static class StringToUserTypeConverter implements Converter<String, UserType> {

        @Override
        public UserType convert(@NonNull String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            
            // 使用UserType.fromCode方法进行转换
            UserType userType = UserType.fromCode(source.trim());
            
            // 如果fromCode返回null，尝试直接匹配枚举名称
            if (userType == null) {
                try {
                    userType = UserType.valueOf(source.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // 如果都无法匹配，返回null
                    return null;
                }
            }
            
            return userType;
        }
    }

    /**
     * UserType枚举转数据库字符串值
     * 写入转换器：向数据库写入数据时使用
     */
    @WritingConverter
    public static class UserTypeToStringConverter implements Converter<UserType, String> {

        @Override
        public String convert(@NonNull UserType source) {
            // 使用UserType的code值存储到数据库
            return source.getCode();
        }
    }
}


