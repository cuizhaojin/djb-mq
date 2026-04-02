package com.xtsoft.common.mq;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author: cuizhaojin
 * @date: 2024/8/14 16:48
 * @description:
 */
public class TestMq {


    public static void main(String[] args) throws Exception {
        /*//持久化地址
        String storagePath = "D:/mq_storage";
        TopicManager topicManager = new TopicManager();
        *//**
         * 测试顺序消息
         *//*
        SimpleMessageListener simpleListener =  new SimpleMessageListener();
        Class<?> listenerClass = simpleListener.getClass();

        if (listenerClass.isAnnotationPresent(CustomMessageListener.class)) {
            CustomMessageListener annotation = listenerClass.getAnnotation(CustomMessageListener.class);
            String topicfix = annotation.topic();
            String consumerGroup = annotation.consumerGroup();
            ConsumeMode consumeMode = annotation.consumeMode();

            // 获取 onMessage 方法
            Method onMessageMethod = listenerClass.getMethod("onMessage", Message.class);
            topicManager.addTopic(topicfix, 4,storagePath);
            // 创建 CustomConsumer 实例，传递线程池参数
            CustomConsumer consumer = new CustomConsumer(
                    topicManager.getTopic(topicfix), simpleListener, onMessageMethod, consumeMode,
                    5,  // corePoolSize
                    10, // maximumPoolSize
                    60, // keepAliveTime
                    TimeUnit.SECONDS // timeUnit
            );

            //普通消息队列
            topicManager.addConsumer(topicfix, consumer);
            // 启动消费者线程
            new Thread(consumer).start();

            // 模拟普通消息到达
            Topic topic = topicManager.getTopic(topicfix);
            Message message = new Message(topicfix, "Hello, world!");
            int queueIndex = (message.hashCode() & Integer.MAX_VALUE) % topic.getQueueCount();
            PersistentQueue queue = topic.getQueue(queueIndex);
            for (int i = 0; i < 10000; i++){
                queue.send(message);
            }
        }


        *//**
         * 测试顺序消息
         *//*
        OrderMessageListener orderListener = new OrderMessageListener();
        Class<?> orderListenerClass = orderListener.getClass();

        if (orderListenerClass.isAnnotationPresent(CustomMessageListener.class)) {
            CustomMessageListener annotation = orderListenerClass.getAnnotation(CustomMessageListener.class);
            String topicfix = annotation.topic();
            ConsumeMode consumeMode = annotation.consumeMode();
            // 获取 onMessage 方法
            Method onMessageMethod = orderListenerClass.getMethod("onMessage", Message.class);
            // 创建 CustomConsumer 实例，传递线程池参数
            topicManager.addOrderTopic(topicfix, storagePath);
            CustomConsumer orderConsumer = new CustomConsumer(topicManager.getTopic(topicfix), orderListener, onMessageMethod, consumeMode);
            //普通消息队列
            topicManager.addConsumer(topicfix, orderConsumer);
            //顺序消息队列
            // 启动消费者线程
            new Thread(orderConsumer).start();

            // 模拟顺序消息到达
            Topic topic = topicManager.getTopic(topicfix);
            Message message = new Message(topicfix, "Hello, world!");
            PersistentQueue queue = topic.getQueue(0);
            for (int i = 0; i < 10000; i++){
                queue.send(message);
            }
        }*/
    }
}

