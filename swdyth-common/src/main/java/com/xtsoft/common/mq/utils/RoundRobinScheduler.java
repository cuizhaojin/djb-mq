package com.xtsoft.common.mq.utils;

import com.xtsoft.common.mq.msg.MqMsg;
import com.xtsoft.common.mq.queue.PersistentQueue;
import com.xtsoft.common.mq.topic.Topic;
import com.xtsoft.common.mq.topic.TopicManager;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: cuizhaojin
 * @date: 2024/8/19 21:13
 * @description:
 */
@Component
public class RoundRobinScheduler {
    private final TopicManager topicManager;
    private final AtomicInteger currentQueueIndex = new AtomicInteger(0); // 使用AtomicInteger来记录当前队列的索引

    public RoundRobinScheduler(TopicManager topicManager) {
        this.topicManager = topicManager;
    }

    public PersistentQueue getNextQueue(String simpleTopic) {
        Topic topic = topicManager.getTopic(simpleTopic);
        int queueCount = topic.getQueueCount();

        // 获取并更新当前队列索引
        int index = currentQueueIndex.getAndUpdate(i -> (i + 1) % queueCount);

        return topic.getQueue(index);
    }
}
