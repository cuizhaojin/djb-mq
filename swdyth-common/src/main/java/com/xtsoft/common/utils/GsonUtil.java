package com.xtsoft.common.utils;

import cn.hutool.core.util.StrUtil;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author W
 */
public class GsonUtil {
    /**
     * //线程安全的
     */
    private static final Gson GSON;
    /**
     * // 不过滤空值
     */
    private static final Gson GSON_NULL;

    static {
        //当Map的key为复杂对象时,需要开启该方法
        GSON = new GsonBuilder().enableComplexMapKeySerialization()
                //序列化日期格式  "yyyy-MM-dd"
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .disableHtmlEscaping()
                .create();
        //当Map的key为复杂对象时,需要开启该方法
        GSON_NULL = new GsonBuilder().enableComplexMapKeySerialization()
                .serializeNulls() //当字段值为空或null时，依然对该字段进行转换
                //序列化日期格式  "yyyy-MM-dd"
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .disableHtmlEscaping() //防止特殊字符出现乱码
                .create();
    }

    /**
     * //获取gson解析器
     *
     * @return
     */
    public static Gson getGson() {
        return GSON;
    }

    /**
     * //获取gson解析器 有空值 解析
     *
     * @return
     */
    public static Gson getWriteNullGson() {
        return GSON_NULL;
    }


    /**
     * 根据对象返回json  过滤空值字段
     */
    public static String toJsonString(Object object) {
        return GSON.toJson(object);
    }

    /**
     * 根据对象返回json  不过滤空值字段
     */
    public static String toJsonString(Object object, GsonSerializerFeature ser) {
        if (ser == GsonSerializerFeature.WriteMapNullValue) {
            return GSON_NULL.toJson(object);
        }
        return GSON.toJson(object);
    }


    /**
     * 将字符串转化对象
     *
     * @param json     源字符串
     * @param classOfT 目标对象类型
     * @param <T>
     * @return
     */
    public static <T> T strToJavaBean(String json, Class<T> classOfT) {
        Object o = null;
        try {
            o = classOfT.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, String> map = strToMaps(json);
        Map<String, String> res = new HashMap<String, String>();
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldNames[i] = fields[i].getName();
            String name = fieldNames[i];
            String s2 = fields[i].getType().toString();
            boolean a = s2.endsWith("BigDecimal");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (!entry.getKey().equals(name)) {
                    String s = entry.getKey().toLowerCase();
                    String s1 = fields[i].getName().toLowerCase();
                    if (s.equals(s1)) {
                        if (a && StrUtil.isEmpty(entry.getValue())) {
                            res.put(fields[i].getName(), "0.00");
                        } else {
                            res.put(fields[i].getName(), entry.getValue());
                        }
                    }
                } else {
                    if (a && StrUtil.isEmpty(entry.getValue())) {
                        res.put(fields[i].getName(), "0.00");
                    } else {
                        res.put(fields[i].getName(), entry.getValue());
                    }
                }
            }
        }
        String s = toJsonString(res, GsonSerializerFeature.WriteMapNullValue);
        return GSON.fromJson(s, classOfT);
    }

    /**
     * 将json转化为对应的实体对象
     * new TypeToken<List<T>>() {}.getType()
     * new TypeToken<Map<String, T>>() {}.getType()
     * new TypeToken<List<Map<String, T>>>() {}.getType()
     */
    public static <T> T fromJson(String json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
    }

    /**
     * 转成list
     *
     * @param gsonString
     * @param cls
     * @return
     */
    public static <T> List<T> strToList(String gsonString, Class<T> cls) {
        Object o = null;
        try {
            o = cls.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Map<String, String>> maps = strToListMaps(gsonString);

        List<Map<String, String>> ress = new ArrayList<>();

        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];

        for (Map<String, String> map : maps) {
            Map<String, String> res = new HashMap<String, String>();
            for (int i = 0; i < fields.length; i++) {
                fieldNames[i] = fields[i].getName();
                String name = fieldNames[i];
                String s2 = fields[i].getType().toString();
                boolean a = s2.endsWith("BigDecimal");
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    if (!entry.getKey().equals(name)) {
                        String s = entry.getKey().toLowerCase();
                        String s1 = fields[i].getName().toLowerCase();
                        if (s.equals(s1)) {
                            if (a && StrUtil.isEmpty(entry.getValue())) {
                                res.put(fields[i].getName(), "0.00");
                            } else {
                                res.put(fields[i].getName(), entry.getValue());
                            }
                        }
                    } else {
                        if (a && StrUtil.isEmpty(entry.getValue())) {
                            res.put(fields[i].getName(), "0.00");
                        } else {
                            res.put(fields[i].getName(), entry.getValue());
                        }
                    }
                }
            }
            ress.add(res);
        }
        String s = toJsonString(ress, GsonSerializerFeature.WriteMapNullValue);
        List<T> list = new ArrayList<>();
        if (null == s) {
            return list;
        }
        JsonArray jsonArray = new JsonParser().parse(s).getAsJsonArray();
        Gson gson = new Gson();
        for (JsonElement jsonElement : jsonArray) {
            list.add(gson.fromJson(jsonElement, cls));
        }
        return list;
    }

    public static <T> List<T> strToListDefaultObject(String gsonString, Class<T> cls) {
        List<T> list = new ArrayList<>();
        if (null == gsonString) {
            return list;
        }
        JsonArray jsonArray = new JsonParser().parse(gsonString).getAsJsonArray();
        Gson gson = new Gson();
        for (JsonElement jsonElement : jsonArray) {
            list.add(gson.fromJson(jsonElement, cls));
        }
        return list;
    }

    /**
     * 转成list中有map的
     *
     * @param gsonString
     * @return
     */
    public static <T> List<Map<String, T>> strToListMaps(String gsonString) {
        return GSON.fromJson(gsonString, new TypeToken<List<Map<String, T>>>() {
        }.getType());
    }

    /**
     * 转成map
     *
     * @param gsonString
     * @return
     */
    public static <T> Map<String, T> strToMaps(String gsonString) {
        return GSON.fromJson(gsonString, new TypeToken<Map<String, String>>() {
        }.getType());
    }


    /*public static String pareobj(Object o, String str) {
        Map<String, String> map = strToMaps(str);
        Map<String, String> res = new HashMap<String, String>();
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldNames[i] = fields[i].getName();
            String name = fieldNames[i];
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (!entry.getKey().equals(name)) {
                    String s = entry.getKey().toLowerCase();
                    String s1 = fields[i].getName().toLowerCase();
                    if (s.equals(s1)) {
                        res.put(fields[i].getName(), entry.getValue());
                    }
                } else {
                    res.put(fields[i].getName(), entry.getValue());
                }
            }
        }
        String s = toJsonString(res, GsonSerializerFeature.WriteMapNullValue);
        return s;
    }


    public static String pareobjlist(Object o, String str) {
        List<Map<String, String>> maps = strToListMaps(str);

        List<Map<String, String>> ress = new ArrayList<>();

        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];

        for (Map<String, String> map : maps) {
            Map<String, String> res = new HashMap<String, String>();
            for (int i = 0; i < fields.length; i++) {
                fieldNames[i] = fields[i].getName();
                String name = fieldNames[i];
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    if (!entry.getKey().equals(name)) {
                        String s = entry.getKey().toLowerCase();
                        String s1 = fields[i].getName().toLowerCase();
                        if (s.equals(s1)) {
                            res.put(fields[i].getName(), entry.getValue());
                        }
                    } else {
                        res.put(fields[i].getName(), entry.getValue());
                    }
                }
            }
            ress.add(res);
        }
        String s = toJsonString(ress, GsonSerializerFeature.WriteMapNullValue);
        return s;
    }*/


    public static String pareobj(Object o, String str) {
        return str;
    }


    public static String pareobjlist(Object o, String str) {
        return str;
    }

}
