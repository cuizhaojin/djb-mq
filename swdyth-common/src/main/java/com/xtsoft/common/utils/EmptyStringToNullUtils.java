package com.xtsoft.common.utils;

import com.xtsoft.common.annotation.EmptyStringToNull;

import java.lang.reflect.Field;

/**
 * @ClassName EmptyStringToNullUtils
 * @Description 将空字符串转为null
 * @Author ZQJ
 * @Date 2023/8/25 16:08
 * @Version 1.0
 */
public class EmptyStringToNullUtils {

    public static void process(Object obj) throws IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(EmptyStringToNull.class)) {
                field.setAccessible(true);
                if (field.get(obj) instanceof String) {
                    String value = (String) field.get(obj);
                    if (value != null && value.isEmpty()) {
                        field.set(obj, null);
                    }
                }
            }
        }
    }
}
