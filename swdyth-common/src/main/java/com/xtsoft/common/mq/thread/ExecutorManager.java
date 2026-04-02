package com.xtsoft.common.mq.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author: cuizhaojin
 * @date: 2024/8/16 22:23
 * @description:
 */
@Slf4j
public class ExecutorManager {
    private final ArrayList<ExecutorService> executors = new ArrayList<>();

    public void registerExecutor(ExecutorService executor) {
        executors.add(executor);
    }

    public void shutdownAll() {
        for (ExecutorService executor : executors) {
            log.info("shutdown executor: " + executor.toString());
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownAll));
    }

}
