package com.xtsoft.common.utils;


/**
 * 转换行政区划工具类
 *
 * @author cheleilei
 * @date 2022/01/21 14:25
 */
public class DealOrganUtil {

    private static String sjxzqh = "371300";

    /**
     * @description: 根据税务机关代码，判断市局、县区局
     * @author: cll
     * @date: 2023/3/16 11:06
     * @param: organCode
     * @return: java.lang.String
     */
    public static String judgeSJByOrganCode(String organCode) {
        if (organCode.length() < 11) {
            return "错误数据";
        }
        String xzqh = organCode.substring(1, 7);
        if (sjxzqh.equals(xzqh)) {
            return "市局";
        } else {
            return "县区局";
        }
    }

    /**
     * @author yft
     * @date 2023/05/04
     * @description 校验是否是市局角色
     */
    public static Boolean checkIsSJByOrganCode(String organCode) {
        if (organCode.length() < 11) {
            return false;
        }
        String xzqh = organCode.substring(1, 7);
        if (sjxzqh.equals(xzqh)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @description: 去除税务机关代码后面的0
     * @author: cll
     * @date: 2023/3/16 11:06
     * @param: organCode
     * @return: java.lang.String
     */
    public static String delZero(String organCode) {
        if (organCode.length() < 11) {
            return "错误数据";
        }
        return organCode.replaceAll("0+$", "");
    }

    /**
     * @description: 截取税务机关代码前五位
     * @author: cll
     * @date: 2023/3/16 11:06
     * @param: organCode
     * @return: java.lang.String
     */
    public static String cutOutTopFive(String organCode) {
        if (organCode.length() < 11) {
            return "错误数据";
        }
        return organCode.substring(0, 5);
    }

    /**
     * @description: 根据税务机关代码截取行政区划
     * @author: cll
     * @date: 2023/3/16 11:06
     * @param: organCode
     * @return: java.lang.String
     */
    public static String cutOutForXzqh(String organCode) {
        if (organCode.length() < 11) {
            return "错误数据";
        }
        return organCode.substring(1, 7);
    }

    /**
     * @description: 根据税务机关获取市局税务机关代码
     * @author: cll
     * @date: 2023/10/17 15:54
     * @param: organCode
     * @return: java.lang.String
     */
    public static String getSjswjgdm(String organCode) {
        if (organCode.length() < 11) {
            return "错误数据";
        }
        return organCode.substring(0, 5) + "000000";
    }

    /**
     * @description: 去除税务机关代码后面的0
     * @author: cll
     * @date: 2023/3/16 11:06
     * @param: organCode
     * @return: java.lang.String
     */
    public static String delZeroXzqh(String xzqh) {
        if (xzqh.length() < 6) {
            return "错误数据";
        }
        return xzqh.replaceAll("0+$", "");
    }
}