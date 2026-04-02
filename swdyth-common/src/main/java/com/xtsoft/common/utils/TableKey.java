package com.xtsoft.common.utils;

import lombok.Data;

import java.io.Serializable;

/**
 * 表头
 * @author cheleilei
 * @date 2022/01/21 14:25
 * @TableName TableKey
 */
@Data
public class TableKey implements Serializable {

    /**
     * 英文字段
     */
    private String colname;

    /**
     * 中文字段
     */
    private String description;

    private static final long serialVersionUID = 1L;
}