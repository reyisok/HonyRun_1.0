package com.honyrun.config.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;

/**
 * Boolean到Integer转换器
 * 修复R2DBC中Boolean到Integer的类型转换错误
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-24 16:01:03
 * @version 1.0.0
 */
@ReadingConverter
public class BooleanToIntegerConverter implements Converter<Boolean, Integer> {

    /**
     * 将Boolean值转换为Integer
     * 
     * @param source Boolean源值
     * @return Integer目标值，true转换为1，false转换为0
     */
    @Override
    public Integer convert(@NonNull Boolean source) {
        return source ? 1 : 0;
    }
}
