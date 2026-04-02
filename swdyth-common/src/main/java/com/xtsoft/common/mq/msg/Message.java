package com.xtsoft.common.mq.msg;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author: cuizhaojin
 * @date: 2024/8/14 16:46
 * @description:
 */
@Data
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    public String uuid;
    public String body;
    public String topic;

    // 计算消息序列化后的大小
    public int getSize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(this);
            oos.flush();
            return baos.size();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
