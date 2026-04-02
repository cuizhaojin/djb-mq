### 这是一个自定义的持久化消息队列中间件，参考了 RocketMQ 的设计思想。以下是详细分析：
 
### 🏗️ 整体架构
### 该 MQ 模块采用了经典的生产者 - 消费者模型，支持并发消费和顺序消费两种模式，具备消息持久化、重试机制、死信队列等功能。
### 目录结构：
```
  mq/
  ├── consumer/          # 消费者实现
  ├── listener/          # 监听器注解
  ├── msg/              # 消息定义
  ├── queue/            # 持久化队列
  ├── thread/           # 线程池管理
  ├── topic/            # 主题管理
  ├── utils/            # 序列化工具
  ├── vo/               # 值对象
  └── TestMq.java       # 测试类
```
## 🔑 核心组件
### 1. 消息模型 (msg/)
  Message: 基础消息类，包含 uuid、body、topic，支持序列化  
  MqMsg: 扩展消息类，增加业务字段（tag、项目编号、主键列表、表名）  
### 2. 主题管理 (topic/)
  Topic: 主题类，管理多个队列（支持多队列和单队列模式）  
  TopicManager: Spring 组件，管理所有 Topic 和 Consumer 的映射关系  
### 3. 持久化队列 (queue/PersistentQueue)  
  核心功能：  
  基于 BlockingQueue 的内存队列  
  4 个持久化文件：
commit.log: 存储完整消息内容  
consumeQueue.dat: 存储消息索引（uuid + 位置 + 大小）  
index.idx: 索引文件  
offset.dat: 记录消费进度  
### 发送消息流程：
  -1. 生成 UUID
  -2. 写入 commit.log（追加写）
  -3. 更新 consumeQueue（记录位置信息）
  -4. 更新 index 文件
  -5. 放入内存队列
消费进度管理：  
消费成功后调用 updateOffsetFile() 记录已消费的消息 UUID  
重启时从 offset 文件加载已消费记录，跳过已处理消息  
### 4. 消费者 (consumer/CustomConsumer)
  -两种消费模式：
  -模式
  -实现方式
 - 适用场景
 - ORDERLY
 - 单线程顺序消费
 - 需要保证消息顺序
 - CONCURRENTLY
  -多线程并发消费
  -追求吞吐量
  -关键特性：
  -✅ 重试机制: 最多重试 3 次（可配置）
  -✅ 死信队列: 失败消息移入死信队列
  -✅ 线程池管理: 支持自定义线程池参数
  -✅ 性能监控: 记录处理耗时
  -✅ 优雅关闭: 通过 ExecutorManager 统一管理线程池  
### 5. 监听器 (listener/CustomMessageListener)
注解定义：
```
@CustomMessageListener(
    topic = "myTopic",           // 订阅主题
    consumerGroup = "group1",     // 消费组
    consumeMode = ConsumeMode.ORDERLY,  // 消费模式
    selectorExpression = "*"      // 选择器表达式
)
```
### 6. 序列化工具 (utils/)
  ConcurrentMessageSerializerUtils:
  使用 ReentrantReadWriteLock 保证并发安全
  支持对象序列化/反序列化
  支持按位置读取消息
  使用 DataOutputStream 追加写索引和偏移量
  AppendObjectOutputStream:
  解决 ObjectOutputStream 不能追加写入的问题
  通过重写 writeStreamHeader() 实现
  RoundRobinScheduler:
  轮询调度器，用于将消息均匀分发到不同队列
### 7. 监控线程 (thread/Monitor)
  -监控指标：
  -队列大小
  -死信队列大小
  -线程池状态（活跃线程数、排队任务数、完成数等）
  -每 6 秒输出一次日志
### ⚠️ 潜在问题
-Monitor 类注释错误: 第 51 行注释说"每隔 10 分钟"，实际代码是 6 秒
-序列化性能: Java 原生序列化效率较低，可考虑使用 Protobuf 等
-文件锁竞争: 所有写操作共用一把锁，高并发时可能成为瓶颈
-缺少事务支持: 没有实现事务消息机制
-offset 文件读取: readOffsets() 方法存在代码重复（第 194-215 行）
 
### 💡 设计亮点
-✅ 持久化设计: 借鉴 RocketMQ 的 CommitLog + ConsumeQueue 分离思想
-✅ 消费进度管理: 基于 UUID 精确追踪每条消息
-✅ 灵活消费模式: 支持并发和顺序两种模式
-✅ 完善的错误处理: 重试 + 死信队列机制
-✅ 线程安全: 使用读写锁分离，提升读性能
-✅ Spring 集成: TopicManager 和 RoundRobinScheduler 注册为 Spring Bean
 
这个 MQ 模块是一个轻量级的嵌入式消息队列，适合在单机或小型集群中使用，特别适用于需要消息持久化和可靠投递的场景。 
