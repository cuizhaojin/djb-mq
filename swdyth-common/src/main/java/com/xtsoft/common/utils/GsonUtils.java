/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.xtsoft.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Json工具类.
 */
public class GsonUtils {


    public static String toJson(Object value) {
        return JSON.toJSONString(value);
    }

    //格式化时保留null
    public static String toJsonRetainNull(Object value) {
        return JSON.toJSONString(value, SerializerFeature.WriteMapNullValue);
    }

    public static <T> T fromJson(String json, Class<T> classOfT)  {
        return JSON.parseObject(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return (T) JSON.parseObject(json, typeOfT);
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
}
