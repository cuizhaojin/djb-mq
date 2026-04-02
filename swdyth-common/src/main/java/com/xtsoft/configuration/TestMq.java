package com.xtsoft.business.configuration;

import com.xtsoft.common.mq.consumer.CustomConsumer;
import com.xtsoft.common.mq.msg.MqMsg;
import com.xtsoft.common.mq.queue.PersistentQueue;
import com.xtsoft.common.mq.topic.Topic;
import com.xtsoft.common.mq.topic.TopicManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.xtsoft.business.configuration.MqConfig.orderTopic;
import static com.xtsoft.business.configuration.MqConfig.simpleTopic;

/**
 * @author: cuizhaojin
 * @date: 2024/8/17 12:14
 * @description:
 */
@Slf4j
@Component
public class TestMq {
    @Resource(name = "simpleConsumer")
    CustomConsumer simpleConsumer;

    @Resource(name = "orderConsumer")
    CustomConsumer orderConsumer;
    @Autowired
    MqConfig config;
    @PostConstruct
    public void testQueue() throws InterruptedException {

        Topic simpletopic = simpleConsumer.getTopic();
        Topic topic = orderConsumer.getTopic();
        for (int i = 0; i < 10000; i++) {
//            List<String> uuidList = new ArrayList<>();
//            uuidList.add(Integer.toString(i));
//            MqMsg msg = new MqMsg(simpleTopic, "add", "100253142611944224", uuidList, "jcsj_caiji_jsgcjgysbaz");
//            int queueIndex = (msg.hashCode() & Integer.MAX_VALUE) % simpletopic.getQueueCount();
//            PersistentQueue simpleQueue = simpletopic.getQueue(queueIndex);
//            String msgId = simpleQueue.sendSyncMsg(msg);
//            log.info("Syncsend msg = {}", msgId);
        }

        for (int i = 0; i < 10000; i++) {
//            List<String> uuidList = new ArrayList<>();
//            uuidList.add(Integer.toString(i));
//            MqMsg msg = new MqMsg(orderTopic, "add", "100253142611944224", uuidList, "jcsj_caiji_jsgcjgysbaz");
//            PersistentQueue orderQueue = topic.getQueue(0);
//            String msgId = orderQueue.send(msg);
//            log.info("SyncSendOrderly msg = {}", msgId);
        }
    }
}
