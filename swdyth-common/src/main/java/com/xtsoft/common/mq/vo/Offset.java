package com.xtsoft.common.mq.vo;

import lombok.Data;

/**
 * @author: cuizhaojin
 * @date: 2024/8/18 23:37
 * @description:
 */
@Data
public class Offset {
    private String uuid;
    private long timestamp;

    public Offset(String uuid, long timestamp) {
        this.uuid = uuid;
        this.timestamp = timestamp;
    }
}