package com.xtsoft.common.utils;

import java.math.BigDecimal;

/**
 * 数值工具
 *
 * @author cheleilei
 * @date 2022/11/23 14:25
 */
public class IntegerUtil {

    /**
     * @description: 判断数值是否为null
     * @author: cll
     * @date: 2022/11/23 9:50
     * @param: data
     * @return: java.lang.Integer
     */
    public static Integer handleAccuracy(Integer data) {
        if (data != null && data > 0) {
            return data;
        } else {
            return 6;
        }
    }
    /**
     * @description: 判断四舍五入设置是否为null
     * @author: cll
     * @date: 2022/11/23 14:14
     * @param: data
     * @return: int
     */
    public static int handleRounding(Integer data) {
        if (data != null) {
            return data;
        } else {
            return BigDecimal.ROUND_HALF_UP;
        }
    }
}
