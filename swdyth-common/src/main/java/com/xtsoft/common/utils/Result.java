package com.xtsoft.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * 结果类
 * @author cheleilei
 * @date 2022/01/21 14:25
 */
public class Result<T> {
    /**
     * 返回状态
     */
    private String status;
    /**
     * 返回信息
     */
    private String msg;
    /**
     * 返回数据
     */
    private T data;
    /**
     * 分页总数
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long totalCount;

    /**
     * 返回表头
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<TableKey> tableKey;

    /**
     * 其他
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> other;

    /**
     * 合计
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> total;

    public Result() {
        super();
    }

    public Result(String status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<TableKey> getTableKey() {
        return tableKey;
    }

    public void setTableKey(List<TableKey> tableKey) {
        this.tableKey = tableKey;
    }

    public Map<String, Object> getOther() {
        return other;
    }

    public void setOther(Map<String, Object> other) {
        this.other = other;
    }

    public Map<String, Object> getTotal() {
        return total;
    }

    public void setTotal(Map<String, Object> total) {
        this.total = total;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return "Result{" +
                "status='" + status + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", tableKey=" + tableKey +
                ", other=" + other +
                ", total=" + total +
                '}';
    }
}
