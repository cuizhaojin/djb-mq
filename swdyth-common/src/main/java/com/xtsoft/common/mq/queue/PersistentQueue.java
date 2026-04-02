package com.xtsoft.common.mq.queue;

import com.xtsoft.common.mq.msg.Message;
import com.xtsoft.common.mq.msg.MqMsg;
import com.xtsoft.common.mq.thread.ExecutorManager;
import com.xtsoft.common.mq.utils.ConcurrentMessageSerializerUtils;
import com.xtsoft.common.mq.vo.ConsumeQueueEntry;
import com.xtsoft.common.mq.vo.Offset;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Data
@Slf4j
public class PersistentQueue {
    private final BlockingQueue<Message> queue;
    private final String name;
    private final File commitLogFile;
    private final File consumeQueueFile;
    private final File indexFile;
    private final File offsetFile;
    private final boolean persistenceEnabled;
    private final ExecutorManager executorManager;
    private final ReentrantLock fileLock = new ReentrantLock();

    public PersistentQueue(ExecutorManager executorManager, String name, String storagePath,boolean persistenceEnabled) {
        this.executorManager = executorManager;
        this.name = name;
        this.persistenceEnabled = persistenceEnabled;
        this.queue = new LinkedBlockingQueue<>();
        this.commitLogFile = new File(storagePath + "/" + name + "/commit.log");
        this.consumeQueueFile = new File(storagePath + "/" + name + "/consumeQueue.dat");
        this.indexFile = new File(storagePath + "/" + name + "/index.idx");
        this.offsetFile = new File(storagePath + "/" + name + "/offset.dat");
        if (this.persistenceEnabled){
            File parentFile = new File(storagePath + "/" + name);
            if (!parentFile.exists()){
                parentFile.mkdirs();
            }
            //预加载本地未消费消息到内存中
            loadMessages();
        }
    }

    // 同步发送消息
    public String sendSyncMsg(MqMsg message) throws InterruptedException {
        return sendMessage(message);
    }

    // 异步发送消息
    public String send(Message message) throws InterruptedException {
        return sendMessage(message);
    }

    private String sendMessage(Message message) throws InterruptedException {
        message.setUuid(java.util.UUID.randomUUID().toString());
        if (persistenceEnabled){
            appendToCommitLog(message);
            updateConsumeQueue(message);
            // 更新索引文件
            updateIndexFile(message);
            queue.put(message);
        }else{
            queue.put(message);
        }
        return message.getUuid();
    }

    // 消费消息
    public Message take() throws InterruptedException {
        return queue.take();
    }

    // 追加消息到CommitLog
    private void appendToCommitLog(Message message) {
        fileLock.lock();
        try {
            ConcurrentMessageSerializerUtils serializer = new ConcurrentMessageSerializerUtils(commitLogFile);
            serializer.serializeMessage(message);
        } catch (IOException e) {
            log.error("Error writing to file ={}", commitLogFile.getName(), e);
        }finally {
            fileLock.unlock();
        }
    }

    // 更新ConsumeQueue文件
    private void updateConsumeQueue(Message message) {
        fileLock.lock();
        try {
            long position = commitLogFile.length()-message.getSize();
            log.info("Updating ConsumeQueue file ={}, commitFilePosition = {},commitFile.getSize() = {}", consumeQueueFile.getName(), position, message.getSize());
            ConcurrentMessageSerializerUtils serializer = new ConcurrentMessageSerializerUtils(consumeQueueFile);
            serializer.serializeIndex(message.getUuid(), position, message.getSize());
        } catch (IOException e) {
            log.error("Error updating ConsumeQueue file ={}", consumeQueueFile.getName(), e);
        }finally {
            fileLock.unlock();
        }
    }

    public void updateIndexFile(Message message) {
        fileLock.lock();
        try {
            long position = commitLogFile.length()-message.getSize();
            log.info("Updating indexFile file ={}, commitFilePosition = {},commitFile.getSize() = {}", indexFile.getName(), position, message.getSize());
            ConcurrentMessageSerializerUtils serializer = new ConcurrentMessageSerializerUtils(indexFile);
            serializer.serializeIndex(message.getUuid(), position, message.getSize());
        } catch (IOException e) {
            log.error("Error updating index file", e);
        }finally {
            fileLock.unlock();
        }
    }

    // 更新OffsetFile文件，记录当前消费进度
    public void updateOffsetFile(Message message) {
        fileLock.lock();
        try {
            ConcurrentMessageSerializerUtils serializer = new ConcurrentMessageSerializerUtils(offsetFile);
            serializer.serializeOffset(message.getUuid(), System.currentTimeMillis());
        } catch (IOException e) {
            log.error("Error updating OffsetFile file ={}", offsetFile.getName(), e);
        }finally {
            fileLock.unlock();
        }
    }

    // 加载消息到内存队列
    private void loadMessages() {
        ConcurrentMessageSerializerUtils offsetSerializer = new ConcurrentMessageSerializerUtils(offsetFile);
        ConcurrentMessageSerializerUtils commitLogSerializer = new ConcurrentMessageSerializerUtils(commitLogFile);
        ConcurrentMessageSerializerUtils consumeQueueSerializer = new ConcurrentMessageSerializerUtils(consumeQueueFile);

        try {
            // 从偏移量文件读取所有的偏移量记录
            if (!offsetFile.exists()) {
                return;
            }
            List<Offset> offsets = offsetSerializer.readOffsets(); // 修改为可以读取所有偏移量记录的方法
            Set<String> consumedUuids = new HashSet<>();

            // 如果偏移量记录为空，说明没有记录，直接返回
            if (offsets.isEmpty()) {
                return;
            }

            // 从偏移量记录中提取已消费的 UUID
            for (Offset offset : offsets) {
                consumedUuids.add(offset.getUuid());
            }

            List<ConsumeQueueEntry> consumeQueueEntries = consumeQueueSerializer.readIndex();
            boolean startLoading = false;

            for (Object obj : consumeQueueEntries) {
                if (obj instanceof ConsumeQueueEntry) {
                    ConsumeQueueEntry consumeEntry = (ConsumeQueueEntry) obj;

                    // 根据偏移量记录判断是否开始加载消息
                    if (startLoading || !consumedUuids.contains(consumeEntry.getUuid())) {
                        startLoading = true;
                        Message message = commitLogSerializer.readMessageAt(consumeEntry.getPosition(), consumeEntry.getSize());
                        if (message != null) {
                            queue.add(message);
                        }
                    }
                } else {
                    log.warn("Unexpected object type in consumeQueueEntries: {}", obj.getClass().getName());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error loading messages into memory queue", e);
        }
    }
}
