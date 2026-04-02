package com.xtsoft.common.mq.thread;

import com.xtsoft.common.mq.vo.ConsumeMode;
import com.xtsoft.common.mq.queue.PersistentQueue;
import com.xtsoft.common.mq.msg.Message;
import com.xtsoft.common.mq.topic.Topic;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: cuizhaojin
 * @date: 2024/8/14 19:12
 * @description:
 */
@Slf4j
public class Monitor  implements Runnable {
    private final ExecutorService executorService;
    private final Topic topic;
    private final BlockingQueue<Message> deadLetterQueue;
    private ConsumeMode consumeMode;

    public Monitor(ExecutorService executorService, Topic topic, BlockingQueue<Message> deadLetterQueue,ConsumeMode consumeMode) {
        this.executorService = executorService;
        this.topic = topic;
        this.consumeMode = consumeMode;
        this.deadLetterQueue = deadLetterQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                List<PersistentQueue> queues = topic.getQueues();
                for (PersistentQueue queue : queues){
                    log.debug("监控: "+queue.getName()+": 队列大小 = " + queue.getQueue().size() + ", 死信队列大小 = " + deadLetterQueue.size());
                }
                if(consumeMode.equals(ConsumeMode.CONCURRENTLY)){
                    ThreadPoolExecutor threadPool = (ThreadPoolExecutor) executorService;
                    log.debug("线程池: 活跃线程 = " + threadPool.getActiveCount() +
                            ", 完成任务数量 = " + threadPool.getCompletedTaskCount() +
                            ", 当前排队任务数量 = " + threadPool.getQueue().size() +
                            ", 线程池中实际存在的线程总数 = " + threadPool.getPoolSize() +
                            ", 核心线程数 = " + threadPool.getCorePoolSize() +
                            ", 最大线程数 = " + threadPool.getMaximumPoolSize() +
                            ", 任务总数 = " + threadPool.getTaskCount());
                }
                Thread.sleep(6 * 1000); // 每隔10分钟监控一次
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("监控线程被中断: " + e.getMessage(),e);
                break;
            }
        }
    }
}
