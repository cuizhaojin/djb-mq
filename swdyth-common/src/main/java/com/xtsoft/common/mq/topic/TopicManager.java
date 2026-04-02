package com.xtsoft.common.mq.topic;

/**
 * @author: cuizhaojin
 * @date: 2024/8/14 16:45
 * @description:
 */
import com.xtsoft.common.mq.consumer.CustomConsumer;
import com.xtsoft.common.mq.thread.ExecutorManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class TopicManager {
    private final Map<String, Topic> topics = new HashMap<>();
    private final Map<String, CustomConsumer> consumers = new HashMap<>();
    public void addTopic(ExecutorManager executorManager, String topic, int numberOfQueues, String storagePath,boolean persistenceEnabled) {
        topics.put(topic, new Topic(executorManager,topic, numberOfQueues, storagePath,persistenceEnabled));
    }

    public void addOrderTopic(ExecutorManager executorManager,String topic,String storagePath,boolean persistenceEnabled) {
        topics.put(topic, new Topic(executorManager,topic, 1,storagePath,persistenceEnabled));
    }

    public void addConsumer(String topic, CustomConsumer consumer) {
        consumers.put(topic, consumer);
    }

    public Topic getTopic(String topic) {
        return topics.get(topic);
    }

    public CustomConsumer getConsumer(String topic) {
        return consumers.get(topic);
    }
}
