package com.xtsoft.common.utils;

import java.util.*;

/**
 * 从集合中移除指定集合工具
 *
 * @author cheleilei
 * @date 2022/01/21 14:25
 */
public class RemoveListUtil {

    /**
     * 从List<Map<String, Object>>中移除指定集合
     *
     * @param data     源数据
     * @param beDelete 要移除的集合
     * @return
     */
    public static List<Map<String, Object>> removeAll(List<Map<String, Object>> data, List<Map<String, Object>> beDelete) {
        LinkedList<Map<String, Object>> linkedList = new LinkedList<Map<String, Object>>(data);
        HashSet<Map<String, Object>> hashSet = new HashSet<Map<String, Object>>(beDelete);
        Iterator it = linkedList.iterator();
        while (it.hasNext()) {
            if (hashSet.contains(it.next())) {
                it.remove();
            }
        }
        return linkedList;
    }


}