package com.xtsoft.common.mq.vo;

/**
 * @author: cuizhaojin
 * @date: 2024/8/18 23:49
 * @description:
 */
import lombok.Data;

@Data
public class ConsumeQueueEntry {
    private String uuid;  // 消息的唯一标识符
    private long position; // 消息在commitLogFile中的位置
    private int size;      // 消息的大小

    public ConsumeQueueEntry(String uuid, long position, int size) {
        this.uuid = uuid;
        this.position = position;
        this.size = size;
    }
}
