package com.xtsoft.common.mq.utils;

import com.xtsoft.common.mq.msg.Message;
import com.xtsoft.common.mq.msg.MqMsg;
import com.xtsoft.common.mq.vo.ConsumeQueueEntry;
import com.xtsoft.common.mq.vo.Offset;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author: cuizhaojin
 * @date: 2024/8/17 19:38
 * @description:
 */
@Slf4j
public class ConcurrentMessageSerializerUtils {

    private final File file;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ConcurrentMessageSerializerUtils(File file) {
        this.file = file;
    }

    // 序列化消息对象到文件
    public void serializeMessage(Object message) throws IOException {
        lock.writeLock().lock();
        try (FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(message);
                oos.flush();
                byte[] data = baos.toByteArray();
                ByteBuffer buffer = ByteBuffer.allocate(data.length + Integer.BYTES);
                buffer.putInt(data.length); // 写入消息长度
                buffer.put(data); // 写入消息内容
                buffer.flip();
                fileChannel.position(fileChannel.size());
                fileChannel.write(buffer);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 反序列化 byte[] 数组中的对象
    public Object deserializeMessage(byte[] data) throws IOException, ClassNotFoundException {
        lock.readLock().lock();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        } finally {
            lock.readLock().unlock();
        }
    }

    // 从文件中循环读取对象
    public List<Object> readObjects() {
        List<Object> objects = new ArrayList<>();
        lock.readLock().lock();  // 加读锁
        try (FileChannel fileChannel = new RandomAccessFile(file, "r").getChannel()) {
            MappedByteBuffer buffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
            while (buffer.hasRemaining()) {
                try {
                    int length = buffer.getInt();
                    if (buffer.remaining() < length) {
                        log.error("Insufficient data in buffer, expected: {}, remaining: {}", length, buffer.remaining());
                        break; // Or throw a custom exception
                    }
                    byte[] data = new byte[length];
                    buffer.get(data);
                    Object obj = deserializeMessage(data);
                    objects.add(obj);
                } catch (BufferUnderflowException e) {
                    log.error("Buffer underflow at position: {}, remaining: {}", buffer.position(), buffer.remaining(), e);
                    break; // Handle the underflow, maybe skip or terminate reading
                }

            }
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error reading objects from file", e);
        } finally {
            lock.readLock().unlock();
        }
        return objects;
    }

    // 读取指定位置和大小的消息
    public Message readMessageAt(long position, int size) {
        lock.readLock().lock();
        try (FileChannel fileChannel = new RandomAccessFile(file, "r").getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(size);
            fileChannel.position(position);
            fileChannel.read(buffer);
            buffer.flip();
            return (Message) deserializeMessage(buffer.array());
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error reading message at position " + position, e);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    // 清理文件中的所有对象
    public void clearFile() throws IOException {
        lock.writeLock().lock();
        try (FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel()) {
            fileChannel.truncate(0);
        } finally {
            lock.writeLock().unlock();
        }
    }


    // 序列化索引
    public void serializeIndex(String uuid, long commitLogPosition, int messageSize) throws IOException {
        lock.writeLock().lock();
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, true))) {
            dos.writeUTF(uuid);
            dos.writeLong(commitLogPosition);
            dos.writeInt(messageSize);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<ConsumeQueueEntry> readIndex() throws IOException {
        lock.readLock().lock();
        List<ConsumeQueueEntry> indexEntries = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            while (true) {
                try {
                    String uuid = dis.readUTF();
                    long position = dis.readLong();
                    int size = dis.readInt();
                    indexEntries.add(new ConsumeQueueEntry(uuid, position, size));
                } catch (EOFException e) {
                    // 文件结束，退出循环
                    break;
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return indexEntries;
    }

    public void serializeOffset(String uuid, long timestamp) throws IOException {
        lock.writeLock().lock();
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, true))) {
            dos.writeUTF(uuid);
            dos.writeLong(timestamp);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 读取消费进度对象
     *
     * @return
     * @throws IOException
     */
    public Offset readOffset() throws IOException {
        lock.readLock().lock();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            String uuid = dis.readUTF();
            long timestamp = dis.readLong();
            return new Offset(uuid, timestamp);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 读取消费进度对象列表
     *
     * @return
     * @throws IOException
     */
    public List<Offset> readOffsets() throws IOException, ClassNotFoundException {
        List<Offset> offsets = new ArrayList<>();
        lock.readLock().lock();
        // 使用 FileInputStream 和 ObjectInputStream 来读取对象
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            // 循环读取 Offset 对象
            while (true) {
                try {
                    // 读取 UUID 和 timestamp，假设 Offset 对象是用 writeUTF 和 writeLong 方法序列化的
                    String uuid = dis.readUTF();
                    long timestamp = dis.readLong();
                    offsets.add(new Offset(uuid, timestamp));
                } catch (EOFException e) {
                    // 到达文件末尾，退出循环
                    break;
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return offsets;
    }


    public static void main1(String[] args) throws IOException, ClassNotFoundException {
        File commitLogFile = new File("C:\\Users\\86186\\Desktop\\mq\\simpleTopic-queue-1-commit.log");
        File queueFile = new File("C:\\Users\\86186\\Desktop\\mq\\simpleTopic-queue-1-consumeQueue.dat");
        File idxFile = new File("C:\\Users\\86186\\Desktop\\mq\\simpleTopic-simpleTopic-queue-1-index.idx");
        File offsetFile = new File("C:\\Users\\86186\\Desktop\\mq\\simpleTopic-queue-1-offset.dat");
        ConcurrentMessageSerializerUtils commitlogfileSerializerUtils = new ConcurrentMessageSerializerUtils(commitLogFile);
        ConcurrentMessageSerializerUtils queuefileSerializer = new ConcurrentMessageSerializerUtils(queueFile);
        ConcurrentMessageSerializerUtils idxFileSerializer = new ConcurrentMessageSerializerUtils(idxFile);
        ConcurrentMessageSerializerUtils offsetFileSerializer = new ConcurrentMessageSerializerUtils(offsetFile);
        MqMsg mqMsg = new MqMsg("simpleTopic", "tag", "xmbh", Arrays.asList("uuid1", "uuid2"), "tableName");
        mqMsg.setUuid("uuid----111");
        // 序列化消息
        commitlogfileSerializerUtils.serializeMessage(mqMsg);
        commitlogfileSerializerUtils.serializeMessage(mqMsg);
        // 序列化索引
        queuefileSerializer.serializeIndex("1", commitLogFile.length(), mqMsg.getSize());
        queuefileSerializer.serializeIndex("2", commitLogFile.length(), mqMsg.getSize());
        // 序列化索引
        long position = commitLogFile.length() - mqMsg.getSize();
        idxFileSerializer.serializeIndex(mqMsg.getUuid(), position, mqMsg.getSize());

        offsetFileSerializer.serializeOffset("1", System.currentTimeMillis());
        offsetFileSerializer.serializeOffset("2", System.currentTimeMillis());
        offsetFileSerializer.serializeOffset("3", System.currentTimeMillis());
        System.out.println(commitlogfileSerializerUtils.readMessageAt(position, mqMsg.getSize()));
        System.out.println(queuefileSerializer.readIndex());
        System.out.println(idxFileSerializer.readIndex());
        System.out.println(commitlogfileSerializerUtils.readObjects());
        System.out.println(offsetFileSerializer.readOffsets());
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        File offsetFile = new File("C:\\Users\\86186\\Desktop\\mq\\simpleTopic-queue-1-offset.dat");
        ConcurrentMessageSerializerUtils offsetFileSerializer = new ConcurrentMessageSerializerUtils(offsetFile);
        //模拟多个线程
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 50; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 200; i++) {
                        try {
                            System.out.println(Thread.currentThread().getName() + ":" + i);
                            offsetFileSerializer.serializeOffset("1", System.currentTimeMillis());
                        } catch (IOException e) {
                            throw new RuntimeException();
                        }
                    }
                }
            });
        }
        Thread.sleep(10000);
        List<Offset> offsets = offsetFileSerializer.readOffsets();
        System.out.println(offsets);
    }
}
