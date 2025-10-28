package com.honyrun.config.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

/**
 * Integer到Boolean转换器
 * 修复R2DBC中Integer到Boolean的类型转换错误
 * 
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-24 16:01:03
 * @version 1.0.0
 */
@WritingConverter
public class IntegerToBooleanConverter implements Converter<Integer, Boolean> {

    /**
     * 将Integer值转换为Boolean
     * 
     * @param source Integer源值
     * @return Boolean目标值，1转换为true，0转换为false
     */
    @Override
    public Boolean convert(@NonNull Integer source) {
        return source != null && source == 1;
    }
}
