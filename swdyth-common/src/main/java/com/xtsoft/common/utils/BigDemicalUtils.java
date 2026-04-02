package com.xtsoft.common.utils;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

/**
 * @ClassName BigDemicalUtils
 * @Description TODO
 * @Author ZQJ
 * @Date 2023/4/21 14:27
 * @Version 1.0
 */
public class BigDemicalUtils {

    /**
     * @Author ZQJ
     * @Description 字符串转BigDecimal
     * @Date 14:30 2023/4/21
     * @Param
     * @param str
     * @return
     * @return java.math.BigDecimal
    **/
    public static BigDecimal bigDemicalFormat(String str){
        if(StringUtils.isNotBlank(str)&&!"null".equals(str)){
            return new BigDecimal(str);
        }
        return null;
    }
}
