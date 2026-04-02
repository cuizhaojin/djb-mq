package com.xtsoft.common.utils;

import java.util.Arrays;
import java.util.List;

/**
 * 解析工具
 *
 * @author cheleilei
 * @date 2022/01/21 14:25
 */
public class AnalysisUtil {

    /**
     * 把字符串规则转换成List
     *
     * @param stringRule 字符串规则
     * @return
     */
    public static List<String> getRuleList(String stringRule) {
        return Arrays.asList(stringRule.split(";"));
    }
}