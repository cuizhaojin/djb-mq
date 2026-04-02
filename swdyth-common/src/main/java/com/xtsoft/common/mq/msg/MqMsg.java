package com.xtsoft.common.mq.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author: cuizhaojin
 * @date: 2023/5/17 15:07
 * @description: 消息队列封装消息体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MqMsg extends Message implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 业务标识，可以作为 topic队列下的细分项，通常用来区分业务
     */
    private String tag;
    /**
     * 项目编号
     */
    private String xmbh;
    /**
     * 主键id
     */
    private List<String> uuidList;
    /**
     * 业务操作的表名称
     */
    private String tableName;

    public MqMsg(String topic, String tag, String xmbh, List<String> uuidList, String tableName) {
        this.topic = topic;
        this.tag = tag;
        this.xmbh = xmbh;
        this.uuidList = uuidList;
        this.tableName = tableName;
    }
}