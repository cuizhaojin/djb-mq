package com.xtsoft.common.utils;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cll
 * @version 1.0
 * @description: 分页
 * @date 2022/08/09 17:06
 */
@Data
public class PageUtil<T> {

    private static final long serialVersionUID = -8741766802354222579L;

    /**
     * 每页显示多少条记录
     */
    private int pageSize;

    /**
     * 当前第几页数据
     */

    private int pageNum;

    /**
     * 一共多少条记录
     */
    private int total;

    /**
     * 一共多少页
     */

    private int pages;

    /**
     * 要显示的数据
     */
    private List<T> list = new ArrayList<>();

    public PageUtil() {
    }

    public PageUtil(int pageSize, int currentPage, int totalRecord, int totalPage, List<T> dataList) {
        super();
        this.pageSize = pageSize;
        this.pageNum = currentPage;
        this.total = totalRecord;
        this.pages = totalPage;
        this.list = dataList;
    }

    /**
     * @return java.util.List<T>
     * @methodName getPageList
     * @Author cll
     * @Date 17:06 2022/08/09
     * @Param [pageSize, list, dataType]
     * @Description 分页数据
     **/
    public PageUtil(int page, int pageSize, List<T> list) {

        // 总记录条数
        this.total = list.size();

        // 每页显示多少条记录
        this.pageSize = pageSize;

        // 获取总页数
        this.pages = this.total / this.pageSize;
        if (this.total % this.pageSize != 0) {
            this.pages = this.pages + 1;
        }

        // 当前第几页数据
        this.pageNum = Math.min(this.pages, pageNum);

        // 起始索引
        int fromIndex = this.pageSize * (this.pageNum - 1);

        // 结束索引
        int toIndex = Math.min(this.pageSize * this.pageNum, this.total);

        this.list = list.stream().skip(pageSize * (page - 1)).limit(pageSize).collect(Collectors.toList());
    }


}
