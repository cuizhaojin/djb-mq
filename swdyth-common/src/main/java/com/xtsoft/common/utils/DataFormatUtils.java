package com.xtsoft.common.utils;

import org.apache.commons.beanutils.PropertyUtilsBean;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName DataFormat
 * @Description TODO
 * @Author ZQJ
 * @Date 2023/4/8 10:12
 * @Version 1.0
 */
public class DataFormatUtils {

    /**
     * @Author ZQJ
     * @Description bean转化为map
     * @Date 10:13 2023/4/8
     * @Param
     * @param obj
     * @return
     * @return java.util.Map<java.lang.String,java.lang.Object>
    **/
    public static Map<String, Object> beanToMap(Object obj) {
        Map<String, Object> params = new HashMap<String, Object>(0);
        try {
            PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
            PropertyDescriptor[] descriptors = propertyUtilsBean.getPropertyDescriptors(obj);
            for (int i = 0; i < descriptors.length; i++) {
                String name = descriptors[i].getName();
                if (!"class".equals(name)) {
                    params.put(name, propertyUtilsBean.getNestedProperty(obj, name));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }
}
