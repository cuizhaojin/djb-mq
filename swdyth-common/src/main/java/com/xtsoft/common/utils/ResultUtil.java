package com.xtsoft.common.utils;



import java.util.List;
import java.util.Map;

/**
 * 返回结果集
 * @author cheleilei
 * @date 2022/01/21 14:25
 */
public class ResultUtil {

    /**
     * 成功-带数据
     * @param obj 返回数据
     * @return com.qyd.module.common.entity.Result<T>
     * @author mayuanbao
     * @date 2021/06/29 14:52
     */
    public static <T> Result<T> success(T obj) {
        Result<T> result = new Result<>();
        result.setStatus(ResultEnum.SUCCESS.getStatus());
        result.setMsg(ResultEnum.SUCCESS.getMsg());
        result.setData(obj);
        return result;
    }

    /**
     * @return com.xtsoft.common.utils.Result<T>
     * @author cuizhaojin
     * @date 2023/7/14 13:18
     * @Description 成功-自定义msg
     */
    public static <T> Result<T> success(String msg) {
        Result<T> result = new Result<>();
        result.setStatus(ResultEnum.SUCCESS.getStatus());
        result.setMsg(msg);
        return result;
    }



    /**
     * 成功-带数据-tk
     * @param obj 返回数据
     * @return com.qyd.module.common.entity.Result<T>
     * @author mayuanbao
     * @date 2021/06/29 14:52
     */
    public static <T> Result<T> success(T obj, List<TableKey> tableKey, Map<String, Object> other) {
        Result<T> result = new Result<>();
        result.setStatus(ResultEnum.SUCCESS.getStatus());
        result.setMsg(ResultEnum.SUCCESS.getMsg());
        result.setData(obj);
        result.setTableKey(tableKey);
        result.setOther(other);
        return result;
    }

    /**
     * 分页结果集
     * @param totalCount
     * @param obj
     * @param msg
     * @return
     * @param <T>
     */
    public static <T> Result<T> success(long totalCount,T obj,String msg){
        Result<T> result = new Result<>();
        result.setStatus(ResultEnum.SUCCESS.getStatus());
        result.setMsg(ResultEnum.SUCCESS.getMsg());
        result.setData(obj);
        result.setTotalCount(totalCount);
        return  result;
    }

    /**
     * 成功-带数据-tk-合计
     * @param obj 返回数据
     * @return com.qyd.module.common.entity.Result<T>
     * @author mayuanbao
     * @date 2021/06/29 14:52
     */
    public static <T> Result<T> success(T obj, Map<String, Object> total, List<TableKey> tableKey) {
        Result<T> result = new Result<>();
        result.setStatus(ResultEnum.SUCCESS.getStatus());
        result.setMsg(ResultEnum.SUCCESS.getMsg());
        result.setData(obj);
        result.setTableKey(tableKey);
        result.setTotal(total);
        return result;
    }

    /**
     * 成功-带数据-自定义信息
     * @param obj 返回数据
     * @param msg msg
     * @return com.qyd.module.common.entity.Result<T>
     * @author mayuanbao
     * @date 2021/06/29 14:52
     */
    public static <T> Result<T> success(T obj, String msg) {
        Result<T> result = new Result<>();
        result.setStatus(ResultEnum.SUCCESS.getStatus());
        result.setMsg(msg);
        result.setData(obj);
        return result;
    }

    /**
     * 成功-带数据-tk=自定义信息
     * @param obj 返回数据
     * @param msg msg
     * @return com.qyd.module.common.entity.Result<T>
     * @author mayuanbao
     * @date 2021/06/29 14:52
     */
    public static <T> Result<T> success(T obj, String msg, List<TableKey> tableKey, Map<String, Object> other) {
        Result<T> result = new Result<>();
        result.setStatus(ResultEnum.SUCCESS.getStatus());
        result.setMsg(msg);
        result.setData(obj);
        result.setTableKey(tableKey);
        result.setOther(other);
        return result;
    }

    /**
     * 成功-不带数据
     * @return com.qyd.module.common.entity.Result<T>
     * @author mayuanbao
     * @date 2021/06/29 14:53
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setStatus(ResultEnum.SUCCESS.getStatus());
        result.setMsg(ResultEnum.SUCCESS.getMsg());
        result.setData(null);
        return result;
    }

    /**
     * 成功-可以自定义状态
     * @return com.qyd.module.common.entity.Result<T>
     * @author mayuanbao
     * @date 2021/06/29 14:53
     */
    public static <T> Result<T> success(String status,String msg,T obj) {
        Result<T> result = new Result<>();
        result.setStatus(status);
        result.setMsg(msg);
        result.setData(obj);
        return result;
    }

    /**
     * 失败-自定义信息
     * @param msg 返回信息
     * @return com.qyd.module.common.entity.Result<T>
     * @author mayuanbao
     * @date 2021/06/29 14:53
     */
    public static <T> Result<T> error(String status, String msg) {
        Result<T> result = new Result<>();
        result.setStatus(status);
        result.setMsg(msg);
        return result;
    }

    /**
     * 失败-默认信息
     * @return com.qyd.module.common.entity.Result<T>
     * @author mayuanbao
     * @date 2021/06/29 14:53
     */
    public static <T> Result<T> error() {
        Result<T> result = new Result<>();
        result.setStatus(ResultEnum.ERROR.getStatus());
        result.setMsg(ResultEnum.ERROR.getMsg());
        return result;
    }

    /**
     * 失败-带结果
     * @return com.qyd.module.common.entity.Result<T>
     * @author mayuanbao
     * @date 2021/06/29 14:53
     */
    public static <T> Result<T> error(T obj) {
        Result<T> result = new Result<>();
        result.setStatus(ResultEnum.ERROR.getStatus());
        result.setMsg(ResultEnum.ERROR.getMsg());
        result.setData(obj);
        return result;
    }


    /**
     * @return com.xtsoft.common.utils.Result<T>
     * @author cuizhaojin
     * @date 2023/7/14 13:28
     * @Description 失败-自定义msg
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setStatus(ResultEnum.ERROR.getStatus());
        result.setMsg(msg);
        return result;
    }

    /**
     * 失败-带结果
     * @return com.qyd.module.common.entity.Result<T>
     * @author mayuanbao
     * @date 2021/06/29 14:53
     */
    public static <T> Result<T> error(T obj,String errorMsg) {
        Result<T> result = new Result<>();
        result.setStatus(ResultEnum.ERROR.getStatus());
        result.setMsg(errorMsg);
        result.setData(obj);
        return result;
    }
}