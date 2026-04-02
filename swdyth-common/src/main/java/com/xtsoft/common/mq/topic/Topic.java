package com.xtsoft.common.mq.topic;

import com.xtsoft.common.mq.thread.ExecutorManager;
import com.xtsoft.common.mq.queue.PersistentQueue;

import java.util.ArrayList;
import java.util.List;

public class Topic {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQueues(List<PersistentQueue> queues) {
        this.queues = queues;
    }

    private List<PersistentQueue> queues;

    public Topic(ExecutorManager executorManager, String name, int numberOfQueues, String storagePath,boolean persistenceEnabled) {
        this.name = name;
        this.queues = new ArrayList<>();
        for (int i = 0; i < numberOfQueues; i++) {
            this.queues.add(new PersistentQueue(executorManager,name + "/queue-" + i, storagePath,persistenceEnabled));
        }
    }

    public List<PersistentQueue> getQueues() {
        return queues;
    }

    public PersistentQueue getQueue(int index) {
        return queues.get(index);
    }

    public PersistentQueue getOrderQueue() {
        return queues.get(0);
    }

    public int getQueueCount() {
        return queues.size();
    }
}
