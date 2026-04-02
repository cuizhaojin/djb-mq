package com.xtsoft.business.configuration;

/**
 * @author: cuizhaojin
 * @date: 2024/8/15 11:46
 * @description:
 */

import com.xtsoft.business.listener.OrderMessageListener;
import com.xtsoft.business.listener.SimpleMessageListener;
import com.xtsoft.common.mq.consumer.CustomConsumer;
import com.xtsoft.common.mq.msg.Message;
import com.xtsoft.common.mq.thread.ExecutorManager;
import com.xtsoft.common.mq.topic.TopicManager;
import com.xtsoft.common.mq.utils.RoundRobinScheduler;
import com.xtsoft.common.mq.vo.ConsumeMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class MqConfig {

    @Value(value = "${mq.thread.corePoolSize}")
    private int corePoolSize;

    @Value(value = "${mq.thread.maximumPoolSize}")
    private int maximumPoolSize;

    @Value(value = "${mq.thread.keepAliveTime}")
    private long keepAliveTime;
    @Value(value = "${mq.topic.storagePath}")
    public String storagePath;

    @Value(value = "${mq.topic.persistenceEnabled}")
    public boolean persistenceEnabled;

    public static String orderTopic;

    public static String simpleTopic;


    @Autowired
    public MqConfig(@Value("${mq.topic.simpleTopic}") String simpleTopic, @Value("${mq.topic.orderTopic}") String orderTopic) {
        MqConfig.simpleTopic = simpleTopic;
        MqConfig.orderTopic = orderTopic;
    }

    @Bean
    public ExecutorManager executorManager() {
        ExecutorManager executorManager = new ExecutorManager();
        executorManager.registerShutdownHook();
        return executorManager;
    }

    @Bean
    public TopicManager topicManager() {
        return new TopicManager();
    }

    @Bean
    public RoundRobinScheduler getRoundRobinScheduler(TopicManager topicManager){
        return new RoundRobinScheduler(topicManager);
    }

    @Bean(name = "simpleConsumer")
    public CustomConsumer simpleConsumer(ExecutorManager executorManager, TopicManager topicManager, SimpleMessageListener simpleListener) throws Exception {
        String topicfix = simpleTopic;
        Class<?> listenerClass = simpleListener.getClass();
        Method onMessageMethod = listenerClass.getMethod("onMessage", Message.class);

        topicManager.addTopic(executorManager, topicfix, 4, storagePath,persistenceEnabled);

        CustomConsumer simpleConsumer = new CustomConsumer(
                executorManager,
                topicManager.getTopic(topicfix),
                simpleListener,
                onMessageMethod,
                ConsumeMode.CONCURRENTLY,
                corePoolSize,  // corePoolSize
                maximumPoolSize, // maximumPoolSize
                keepAliveTime, // keepAliveTime
                TimeUnit.SECONDS, // timeUnit,
                persistenceEnabled
        );
        topicManager.addConsumer(simpleTopic, simpleConsumer);
        new Thread(simpleConsumer).start();
        return simpleConsumer;
    }

    @Bean(name = "orderConsumer")
    public CustomConsumer orderConsumer(ExecutorManager executorManager, TopicManager topicManager, OrderMessageListener orderListener) throws Exception {
        String topicfix = orderTopic;
        Class<?> listenerClass = orderListener.getClass();
        Method onMessageMethod = listenerClass.getMethod("onMessage", Message.class);
        topicManager.addOrderTopic(executorManager, topicfix, storagePath,persistenceEnabled);
        CustomConsumer orderConsumer = new CustomConsumer(
                executorManager,
                topicManager.getTopic(topicfix),
                orderListener,
                onMessageMethod,
                ConsumeMode.ORDERLY,
                persistenceEnabled);
        topicManager.addConsumer(orderTopic, orderConsumer);
        new Thread(orderConsumer).start();
        return orderConsumer;
    }
}