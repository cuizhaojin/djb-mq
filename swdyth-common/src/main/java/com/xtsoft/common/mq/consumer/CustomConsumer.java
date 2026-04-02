package com.xtsoft.common.mq.consumer;

/**
 * @author: cuizhaojin
 * @date: 2024/8/14 16:43
 * @description:
 */

import com.xtsoft.common.mq.thread.ExecutorManager;
import com.xtsoft.common.mq.msg.Message;
import com.xtsoft.common.mq.queue.PersistentQueue;
import com.xtsoft.common.mq.thread.Monitor;
import com.xtsoft.common.mq.topic.Topic;
import com.xtsoft.common.mq.vo.ConsumeMode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@Data
public class CustomConsumer implements Runnable {
    private Topic topic;
    private Object listener;
    private Method onMessageMethod;

    private ConsumeMode consumeMode;
    private ExecutorService simpleScheduler;
    private ExecutorService orderScheduler;
    private volatile boolean running = true;
    @Value(value = "${mq.topic.maxRetryAttempts}")
    private int MAX_RETRY_ATTEMPTS = 3;
    private BlockingQueue<Message> deadLetterQueue = new LinkedBlockingQueue<>();

    // Parameters for the thread pool
    private int corePoolSize;
    private int maximumPoolSize;
    private long keepAliveTime;
    private boolean persistenceEnabled;
    private TimeUnit timeUnit;
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    private ExecutorManager executorManager;

    public CustomConsumer(ExecutorManager executorManager, Topic topic, Object listener, Method onMessageMethod, ConsumeMode consumeMode,boolean persistenceEnabled) {

        if (topic == null || listener == null || onMessageMethod == null || consumeMode == null) {
            throw new IllegalArgumentException("topic, listener, consumeMode, and onMessageMethod cannot be null.");
        }
        if (!onMessageMethod.getParameterTypes()[0].equals(Message.class)) {
            throw new IllegalArgumentException("onMessageMethod must accept a Message parameter.");
        }
        this.executorManager = executorManager;
        this.topic = topic;
        this.listener = listener;
        this.onMessageMethod = onMessageMethod;
        this.consumeMode = consumeMode;
        this.deadLetterQueue = new LinkedBlockingQueue<>();
        this.persistenceEnabled = persistenceEnabled;
        // 创建一个自定义的 ThreadFactory
        ThreadFactory namedThreadFactory = new ThreadFactory() {
            private final String baseName = "OrderlyConsumer-orderTopic-queue-0-manager";

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                // 给线程设置名字
                thread.setName(baseName);
                return thread;
            }
        };

        // 使用自定义的 ThreadFactory 创建 ScheduledExecutorService
        orderScheduler = Executors.newFixedThreadPool(1, namedThreadFactory);
        executorManager.registerExecutor(orderScheduler);
    }


    public CustomConsumer(ExecutorManager executorManager,
                          Topic topic,
                          Object listener,
                          Method onMessageMethod,
                          ConsumeMode consumeMode,
                          int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit,boolean persistenceEnabled) {

        if (topic == null || listener == null || onMessageMethod == null || consumeMode == null) {
            throw new IllegalArgumentException("topic, listener, consumeMode, and onMessageMethod cannot be null.");
        }
        if (!onMessageMethod.getParameterTypes()[0].equals(Message.class)) {
            throw new IllegalArgumentException("onMessageMethod must accept a Message parameter.");
        }
        this.topic = topic;
        this.listener = listener;
        this.onMessageMethod = onMessageMethod;
        this.consumeMode = consumeMode;
        this.deadLetterQueue = new LinkedBlockingQueue<>();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.persistenceEnabled = persistenceEnabled;
        if (consumeMode == ConsumeMode.CONCURRENTLY) {
            // 创建一个自定义的 ThreadFactory
            ThreadFactory namedThreadFactory = new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(1);
                private final String baseName = "SimpleConsumer-";

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    // 给线程设置名字
                    thread.setName(baseName + counter.getAndIncrement());
                    return thread;
                }
            };
            // 使用自定义的 ThreadFactory 创建 ScheduledExecutorService
            simpleScheduler = new ThreadPoolExecutor(corePoolSize,
                    maximumPoolSize,
                    keepAliveTime,
                    timeUnit,
                    new LinkedBlockingQueue<>(),
                    namedThreadFactory);
        }
        executorManager.registerExecutor(simpleScheduler);
    }

    @Override
    public void run() {
        //开启监控线程
        startMonitoring();
        if (consumeMode == ConsumeMode.ORDERLY) {
            handleOrderlyConsumption();
        } else {
            handleConcurrentConsumption();
        }
    }

    private void startMonitoring() {
        Monitor monitor = new Monitor(simpleScheduler, topic, deadLetterQueue, consumeMode);
        Thread monitorThread = new Thread(monitor);
        //设置标识name
        monitorThread.setName("Monitor-" + topic.getName());
        monitorThread.setDaemon(true); // 设置为守护线程，确保 JVM 退出时自动停止
        monitorThread.start();
    }


    private void handleOrderlyConsumption() {
        orderScheduler.submit(() -> {
            while (running) {
                try {
                    PersistentQueue queue = topic.getQueue(0);
                    Message message = topic.getOrderQueue().take();
                    boolean success = false;
                    int retries = 0;
                    while (!success && retries < MAX_RETRY_ATTEMPTS) {
                        try {
                            long startReadTime = System.currentTimeMillis();
                            onMessageMethod.invoke(listener, message);
                            long endReadTime = System.currentTimeMillis();
                            log.info("Processing time: " + ((endReadTime - startReadTime) / 1000) + "s");
                            if (this.persistenceEnabled){
                                queue.updateOffsetFile(message);  // 更新消费进度
                            }
                            success = true;
                        } catch (Exception e) {
                            retries++;
                            if (retries >= MAX_RETRY_ATTEMPTS) {
                                // 如果达到最大重试次数，将消息移至死信队列
                                moveToDeadLetterQueue(message, e);
                                log.error("处理消息失败，重试次数: " + retries + " 次: " + e.getMessage(), e);
                            } else {
                                log.info("重试处理消息... 尝试 " + retries);
                            }

                        }
                    }
                } catch (InterruptedException e) {
                    log.error("消费者线程被中断: ", e);
                    Thread.currentThread().interrupt();
                }
            }
        });

    }

    private void moveToDeadLetterQueue(Message message, Exception e) {
        try {
            deadLetterQueue.put(message);
            // 可选记录失败
            log.info("将消息移至死信队列: " + message);
            logAndAlert(message, e);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("无法将消息移至死信队列: ", ie);
        }
    }

    private void logAndAlert(Message message, Exception e) {
        // 实现日志记录和报警逻辑
        log.error("消息处理失败: " + message, e);
    }


    private void handleConcurrentConsumption() {
        for (PersistentQueue queue : topic.getQueues()) {
            Thread consumerThread = new Thread(() -> {
                while (running) {
                    try {
                        Message message = queue.take(); // 阻塞直到有新消息
                        if (message != null) {
                            simpleScheduler.submit(() -> {
                                try {
                                    boolean success = false;
                                    int retries = 0;

                                    while (!success && retries < MAX_RETRY_ATTEMPTS) {
                                        try {
                                            long startReadTime = System.currentTimeMillis();
                                            onMessageMethod.invoke(listener, message);
                                            long endReadTime = System.currentTimeMillis();
                                            log.info("Processing time: " + ((endReadTime - startReadTime) / 1000) + "s");
                                            if (this.persistenceEnabled){
                                                queue.updateOffsetFile(message);  // 更新消费进度
                                            }
                                            success = true;
                                        } catch (Exception e) {
                                            retries++;
                                            if (retries >= MAX_RETRY_ATTEMPTS) {
                                                // 如果达到最大重试次数，将消息移至死信队列
                                                moveToDeadLetterQueue(message, e);
                                                log.error("处理消息失败，重试次数: " + retries + " 次: " + e.getMessage());
                                            } else {
                                                log.info("重试处理消息... 尝试 " + retries);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("处理消息时发生错误: ", e);
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        log.error("消费者线程被中断: ", e);
                        if (!running) {
                            // Thread interrupted due to stopping, so exit loop
                            break;
                        }
                        Thread.currentThread().interrupt(); // Restore the interrupted status
                    }
                }
            });
            // 给线程设置名字
            consumerThread.setName("SimpleConsumer-" + queue.getName() + "-manager");
            consumerThread.start();
        }
    }

}
