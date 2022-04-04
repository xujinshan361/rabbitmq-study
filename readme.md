# 消息队列--RabbitMQ 学习
[TOC]

# 1. 消息队列
## 1.1 MQ的相关概念
### 1.1.1 什么是MQ
- MQ（message queue）,从字面意思上看，本质是个队列，FIFO先进先出，只不过队列中存放的内容是message而已，是一种跨进程的通信机制，用于上下游传递消息。在互联网架构中，MQ是一种非常常见的上下游“逻辑解耦+物理解耦”的消息通信服务。使用了MQ之后，消息发送上游值需要依赖MQ，不用依赖其他服务。

### 1.1.2 为什么用MQ
- **流量消峰**
举个例子，如果订单系统最多能处理一万次订单，这个处理能力应付正常时段的下单时绰绰有余，正常时段我们下单一秒后就能返回结果。但是在高峰期，如果有两万次下单操作系统是处理不了的，只能限制订单超过一万后不允许用户下单。使用消息队列做缓冲，我们可以取消这个限制，把一秒内下的订单分散成一段时间来处理，这时有些用户可能在下单十几秒后才能收到下单成功的操作，但是比不能下单的体验要好。
- **应用解耦**
以电商应用为例，应用中有订单系统、库存系统、物流系统、支付系统。用户创建订单后，如果耦合调用库存系统、物流系统、支付系统，任何一个子系统出了故障，都会造成下单操作异常。当转变成基于消息队列的方式后，系统间调用的问题会减少很多，比如物流系统因为发生故障，需要几分钟来修复。在这几分钟的时间里，物流系统要处理的内存被缓存在消息队列中，用户的下单操作可以正常完成。当物流系统恢复后，继续处理订单信息即可，中单用户感受不到物流系统的故障，提升系统的可用性。
![在这里插入图片描述](https://img-blog.csdnimg.cn/d50d8b63d62841099931917ee2c382da.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
- **异步处理**
有些服务间调用是异步的，例如 A 调用 B，B 需要花费很长时间执行，但是 A 需要知道 B 什么时候可以执行完，以前一般有两种方式，A 过一段时间去调用 B 的查询 api 查询。或者 A 提供一个 callback api， B 执行完之后调用 api 通知 A 服务。这两种方式都不是很优雅，使用消息总线，可以很方便解决这个问题，A 调用 B 服务后，只需要监听 B 处理完成的消息，当 B 处理完成后，会发送一条消息给 MQ，MQ 会将此消息转发给 A 服务。这样 A 服务既不用循环调用 B 的查询 api，也不用提供 callback api。同样B 服务也不用做这些操作。A 服务还能及时的得到异步处理成功的消息。

![在这里插入图片描述](https://img-blog.csdnimg.cn/a6cebe8f1b7744189c50d7b1071a5440.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_12,color_FFFFFF,t_70,g_se,x_16#pic_center)
### 1.1.3 MQ分类
- **ActiveMQ**
	- 优点：单机吞吐量万级，时效性 ms 级，可用性高，基于主从架构实现高可用性，消息可靠性较低的概率丢失数据。
	- 缺点:官方社区现在对 ActiveMQ 5.x 维护越来越少，高吞吐量场景较少使用。

- **Kafka**
	- 大数据的杀手锏，谈到大数据领域内的消息传输，则绕不开 Kafka，这款为**大数据而生**的消息中间件，以其**百万级 TPS** 的吞吐量名声大噪，迅速成为大数据领域的宠儿，在数据采集、传输、存储的过程中发挥着举足轻重的作用。目前已经被 LinkedIn，Uber, Twitter, Netflix 等大公司所采纳。
	- 优点: 性能卓越，单机写入 TPS 约在百万条/秒，最大的优点，就是**吞吐量高**。时效性 ms 级可用性非常高，kafka 是分布式的，一个数据多个副本，少数机器宕机，不会丢失数据，不会导致不可用,消费者采用 Pull 方式获取消息, 消息有序, 通过控制能够保证所有消息被消费且仅被消费一次;有优秀的第三方Kafka Web 管理界面 Kafka-Manager；在日志领域比较成熟，被多家公司和多个开源项目使用；功能支持： 功能较为简单，主要支持简单的 MQ 功能，在大数据领域的实时计算以及**日志采集**被大规模使用。
	- 缺点：Kafka 单机超过 64 个队列/分区，Load 会发生明显的飙高现象，队列越多，load 越高，发送消息响应时间变长，使用短轮询方式，实时性取决于轮询间隔时间，消费失败不支持重试；支持消息顺序，但是一台代理宕机后，就会产生消息乱序，社区更新较慢。

- **RocketMQ**
	- RocketMQ 出自阿里巴巴的开源产品，用 Java 语言实现，在设计时参考了 Kafka，并做出了自己的一些改进。被阿里巴巴广泛应用在订单，交易，充值，流计算，消息推送，日志流式处理，binglog 分发等场景。
	- 优点:单机**吞吐量十万级**,可用性非常高，分布式架构,**消息可以做到 0 丢失**,MQ 功能较为完善，还是分布式的，扩展性好,**支持 10 亿级别的消息堆积**，不会因为堆积导致性能下降,源码是 java 我们可以自己阅读源码，定制自己公司的 MQ。
	-  缺点：**支持的客户端语言不多**，目前是 java 及 c++，其中 c++不成熟；社区活跃度一般,没有在MQ核心中去实现 JMS 等接口,有些系统要迁移需要修改大量代码。

- **RabbitMQ**
	- 2007 年发布，是一个在AMQP(高级消息队列协议)基础上完成的，可复用的企业消息系统，是当前最主流的消息中间件之一。
	-  优点:由于 erlang 语言的**高并发特性**，性能较好；**吞吐量到万级**，MQ 功能比较完备,健壮、稳定、易用、跨平台、**支持多种语言** 如：Python、Ruby、.NET、Java、JMS、C、PHP、ActionScript、XMPP、STOMP等，支持 AJAX 文档齐全；开源提供的管理界面非常棒，用起来很好用,社区活跃度高；更新频率相当高。
	- 缺点：商业版需要收费,学习成本较高。


### 1.1.4 MQ的选择
- **Kafka**
Kafka 主要特点是基于Pull 的模式来处理消息消费，追求高吞吐量，一开始的目的就是用于日志收集和传输，适合产生**大量数据**的互联网服务的数据收集业务。**大型公司**建议可以选用，如果有**日志采集**功能，肯定是首选 kafka 了。

- **RocketMQ**
天生为**金融互联网领域**而生，对于可靠性要求很高的场景，尤其是电商里面的订单扣款，以及业务削峰，在大量交易涌入时，后端可能无法及时处理的情况RoketMQ 在稳定性上可能更值得信赖，这些业务场景在阿里双 11 已经经历了多次考验，如果你的业务有上述并发场景，建议可以选择 RocketMQ。

- **RabbitMQ**
 结合 erlang 语言本身的并发优势，性能好**时效性微秒级，社区活跃度也比较高**，理界面用起来十分方便，如果你的**数据量没有那么大**，中小型公司优先选择功能比较完备的 RabbitMQ。


## 1.2 RabbitMQ
### 1.2.1 RabbitMQ 的概念 
RabbitMQ 是一个消息中间件：它接受并转发消息。你可以把它当做一个快递站点，当你要发送一个包裹时，你把你的包裹放到快递站，快递员最终会把你的快递送到收件人那里，按照这种逻辑 RabbitMQ 是一个快递站，一个快递员帮你传递快件。RabbitMQ 与快递站的主要区别在于，它不处理快件而是接收，存储和转发消息数据。

### 1.2.2 四大核心概念
- **生产者**
生产数据发送消息的程序是生产者
- **交换机**
交换机是RabbitMQ非常重要的一个部件，一方面它接收来自生产者的消息，另一方面它将消息推送到队列中。交换机必须确切知道如何处理它接收到的消息，是将这些消息推送到特定队列还是推动到多个队列，亦或者是把消息丢弃，这个得有交换机类型决定
- **队列**
队列是RabbitMQ内部使用的一种数据结构，尽管消息流经RabbitMQ和应用程序，但它们只能存储在队列中。队列仅受主机的内存和磁盘限制的约束，本质上是一个大的消息缓冲区。许多生产者可以将消息发送到一个队列，许多消费者可以尝试从一个队列接收数据。这就是我们使用队列的方式
- **消费者**
消费者与接收具有相似的含义。消费者大多时候是一个等待接收消息的程序。请注意生产者，消费者和消息中间件很多时候并不在同一机器上。同一个应用程序既可以是生产者又可以是消费者。

### 1.2.3 RabbitMQ 核心部分
![在这里插入图片描述](https://img-blog.csdnimg.cn/459bbb37ce9e4bb0b2d52a487a16eae7.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center) 
### 1.2.4 各个名词解释
![在这里插入图片描述](https://img-blog.csdnimg.cn/c02ad8aaa5b04d3f8aacd064d2212434.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)- **Broker：**接收和分发消息的应用，RabbitMQ Server 就是 Message Broker 

- **Virtual host：**出于多租户和安全因素设计的，把 AMQP 的基本组件划分到一个虚拟的分组中，类似于网络中的 namespace 概念。当多个不同的用户使用同一个RabbitMQ server 提供的服务时，可以划分出多个 vhost，每个用户在自己的 vhost 创建 exchange／queue 等
- **Connection：** publisher／consumer 和 broker 之间的 TCP 连接
- **Channel：** 如果每一次访问 RabbitMQ 都建立一个 Connection，在消息量大的时候建立 TCP Connection 的开销将是巨大的，效率也较低。Channel 是在 connection 内部建立的逻辑连接，如果应用程序支持多线程，通常每个 thread 创建单独的 channel 进行通讯，AMQP method 包含了 channel id 帮助客户端和 message broker 识别 channel，所以 channel 之间是完全隔离的。**Channel 作为轻量级的Connection 极大减少了操作系统建立 TCP connection 的开销** 
- **Exchange：**  message 到达 broker 的第一站，根据分发规则，匹配查询表中的 routing key，分发消息到 queue 中去。常用的类型有：direct (point-to-point), topic (publish-subscribe) and fanout(multicast)
- **Queue：** 消息最终被送到这里等待 consumer 取走
- **Binding：** exchange 和 queue 之间的虚拟连接，binding 中可以包含 routing key，Binding 信息被保存到 exchange 中的查询表中，用于 message 的分发依据


### 1.2.5 安装
[安装方式](https://blog.csdn.net/xu_jin_shan/article/details/123853843)


# 2. Hello World
使用java编程写两个程序。发送单个消息的生产者和接收消息并打印出来的消费者。

在下图中，“ P”是我们的生产者，“ C”是我们的消费者。中间的框是一个队列-RabbitMQ 代表使用者保留的消息缓冲区
![在这里插入图片描述](https://img-blog.csdnimg.cn/c92b52d8b6974b0d86cd61175a12e046.png#pic_center)
## 2.1 依赖

```xml
 <dependencies>
       <!--rabbitmq 依赖客户端-->
           <dependency>
               <groupId>com.rabbitmq</groupId>
               <artifactId>amqp-client</artifactId>
               <version>5.8.0</version>
       </dependency>
       <!--操作文件流的一个依赖-->
           <dependency>
               <groupId>commons-io</groupId>
               <artifactId>commons-io</artifactId>
               <version>2.6</version>
       </dependency>
  </dependencies>
```
## 2.2 消息生产者

```java
package com.xujinshan.rabbitmq01;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * @Author: xujinshan361@163.com
 * 生产者：发消息
 */
public class Producer {
    // 队列名称
    public static final String QUEUE_NAME ="hello";

    // 发消息
    public static void main(String[] args) throws Exception{
        // 创建一个连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置工厂IP，连接RabbitMQ的队列
        factory.setHost("192.168.253.129");
        // 设置用户名
        // 密码
        factory.setUsername("guest");
        factory.setPassword("guest");

        // 创建连接
        Connection connection = factory.newConnection();
        // 获取信道
        Channel channel = connection.createChannel();
        /*
        * 创建一个队列
        * 1.队列名称
        * 2.队列里面的消息是否需要持久化，默认情况消息存储在内存中
        * 3.该队列是否只供一个消费者消费，是否进行消息的共享，true：多个消费者共享；默认false，只能一个消费者消费
        * 4.是否自动删除，最后一个消费者断开连接以后了，该队列是否自动删除true，false表示不自动删除
        * 5.其他参数
        * */
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // 发消息
        String message = "hello world!";

        /*
        * 发送一个消息
        * 1.发送到哪个交换机
        * 2.路由的key值，本次是队列的名称
        * 3.其他参数信息
        * 4.发送消息的消息体
        * */
        channel.basicPublish("",QUEUE_NAME,null,message.getBytes(StandardCharsets.UTF_8));
        System.out.println("消息发送完毕");
    }
}


```
![在这里插入图片描述](https://img-blog.csdnimg.cn/cac63d99ba2248f7859510e915eed49f.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

## 2.3 消息消费者

```java
package com.xujinshan.rabbitmq01;

import com.rabbitmq.client.*;

/**
 * @Author: xujinshan361@163.com
 * 消费者：接收消息
 */
public class Consumer {
    // 队列名名称
    public static final String QUEUE_NAME ="hello";

    //接收消息
    public static void main(String[] args) throws Exception{
        // 创建工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.253.129");
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 声明 接收消息
        DeliverCallback deliverCallback = (consumerTag,message)->{
            System.out.println(new String(message.getBody()));
        };

        // 声明 取消消息时的回调
        CancelCallback cancelCallback =consumerTag->{
            System.out.println("消息消费被中断");
        };
        /**
         * 消费者
         * 1.消费哪个队列
         * 2.消费成功后是否要自动应答，true代表自动应答，false表示手动应答
         * 3.消费者未成功消费的回调
         * 4.消费者取录消息的回调
         */
        channel.basicConsume(QUEUE_NAME,true,deliverCallback,cancelCallback);
    }
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/66ed5eb4462f44bb90b9eed042b859b3.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
# 3. Work Queues
工作队列（又称任务队列）的主要思想是避免立即执行资源密集型任务，而不得不等待它完成。相反我们安排任务在之后执行。我们把任务封装为消息并将其发送给队列。在后台运行的工作进程将弹出任务并最终执行作业。当有多个工作现场时，这这些工作线程将一起处理这些任务。
![在这里插入图片描述](https://img-blog.csdnimg.cn/9a6f35559ee045f49927982e434b018e.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
- 三个工作线程是竞争关系
## 3.1轮询分发消息
在这个案例中启动两个工作线程，一个消息发送线程，两个工作线程

### 3.1.1 抽取工具类

```java
package com.xujinshan.rabbitmqutils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @Author: xujinshan361@163.com
 * 抽取工具类
 * 连接工厂创建信道的工具类
 */
public class RabbitMQUtils {
    /**
     * 获取信道
     * @return
     * @throws Exception
     */
    public static Channel getChannel() throws Exception{
        // 创建一个工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.1.102");
        factory.setUsername("guest");
        factory.setPassword("guest");
        Connection connection = factory.newConnection();
        return connection.createChannel();
    }
}
```

### 3.1.2 启动两个工作线程
- 工作线程1

```java
package com.xujinshan.rabbitmq02;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;


/**
 * @Author: xujinshan361@163.com
 * 一个工作线程，相当于之前的消费者
 */
public class Worker01 {
    // 队列名称
    public static final String QUEUE_NAME ="hello";

    public static void main(String[] args) throws Exception {
        // 通过工具类获得信道
        Channel channel = RabbitMQUtils.getChannel();
        // 消息的接收
        DeliverCallback deliverCallback = (consumerTag,message)->{
            System.out.println("接收到的消息"+ new String(message.getBody()));
        };

        // 消息接收被取消时，执行下面内容
        CancelCallback cancelCallback = (consumerTag)->{
            System.out.println("消息被消费者取消消费接口逻辑");
        };
        /**
         * 消费者消费消息
         * 1.消费哪个队列
         * 2.消费成功之后是否要自动应答，true表示自动应答，false表示手动应答
         * 3.消费者未成功消费的回调
         * 4.消费者取录消息的回调
         */
        System.out.println("C1等待接收消息。。。");
        channel.basicConsume(QUEUE_NAME, true,deliverCallback,consumerTag-> System.out.println("消息被消费者取消消费接口逻辑"));
    }
}
```

- 工作线程2

```java
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

/**
 * @Author: xujinshan361@163.com
 * 一个工作线程，相当于之前的消费者
 */
package com.xujinshan.rabbitmq02;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

/**
 * @Author: xujinshan361@163.com
 * 一个工作线程，相当于之前的消费者
 */
public class Worker02 {
    // 队列名称
    public static final String QUEUE_NAME ="hello";

    public static void main(String[] args) throws Exception {
        // 通过工具类获得信道
        Channel channel = RabbitMQUtils.getChannel();
        // 消息的接收
        DeliverCallback deliverCallback = (consumerTag,message)->{
            System.out.println("接收到的消息"+ new String(message.getBody()));
        };

        // 消息接收被取消时，执行下面内容
        CancelCallback cancelCallback = (consumerTag)->{
            System.out.println("消息被消费者取消消费接口逻辑");
        };
        /**
         * 消费者消费消息
         * 1.消费哪个队列
         * 2.消费成功之后是否要自动应答，true表示自动应答，false表示手动应答
         * 3.消费者未成功消费的回调
         * 4.消费者取录消息的回调
         */
        System.out.println("C2等待接收消息。。。");
        channel.basicConsume(QUEUE_NAME, true,deliverCallback,consumerTag-> System.out.println("消息被消费者取消消费接口逻辑"));
    }
}
```

### 3.1.3 启动一个发送线程
- 通过控制台输入发送消息
```java
package com.xujinshan.rabbitmq02;

import com.rabbitmq.client.Channel;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @Author: xujinshan361@163.com
 * 生成消息
 */
public class Task01 {
    // 队列名称
    public static final String QUEUE_NAME="hello";

    public static void main(String[] args) throws Exception {
        // 准备发送大量连接消息
        Channel channel = RabbitMQUtils.getChannel();
        /*
         * 创建一个队列
         * 1.队列名称
         * 2.队列里面的消息是否需要持久化，默认情况消息存储在内存中
         * 3.该队列是否只供一个消费者消费，是否进行消息的共享，true：多个消费者共享；默认false，只能一个消费者消费
         * 4.是否自动删除，最后一个消费者断开连接以后了，该队列是否自动删除true，false表示不自动删除
         * 5.其他参数
         * */
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // 发送消息 - 从控制台接收消息
        Scanner sc =new Scanner(System.in);
        while (sc.hasNext()){
            String next = sc.next();
            channel.basicPublish("",QUEUE_NAME,null,next.getBytes(StandardCharsets.UTF_8));
            System.out.println("发送完成");
        }
    }
}
```

### 3.1.4 结果展示
启动三个线程，分别是工作线程1，工作线程2，发送线程，并在发送线程输入结果，可以看到工作线程轮询消费输入的结果
![在这里插入图片描述](https://img-blog.csdnimg.cn/6d06656fdf8845d091ca959b64aef4fa.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

![在这里插入图片描述](https://img-blog.csdnimg.cn/8aae26cc3dd24deda6bcfdfada0c72a8.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/fe7fb0d9648f46229165822668d2289e.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
## 3.2 消息应答
### 3.2.1 概念
消费者完成一个任务可能需要一段时间，如果其中一个消费者处理一个长的任务并仅只完成了部分突然它挂掉了，会发生什么情况，RabbitMQ一旦向消费者传递了一条消息，便立即将该消息标记为删除。在这种情况下，突然有个消费者挂掉，我们将丢失正在处理的消息。以及后续发送给该消费的这个消息，因为它无法接收到。

为了保证消息在发送过程中不丢失，rabbitMQ引入了消息应答机制，消息应答机制就是：**消费者在接收到消息并且处理该消息之后，告诉RabbitMQ它已经处理了，RabbitMQ可以把该消息删除了。**


### 3.2.1 自动应答
消息发送后立即被认为已经传送完成。这种模式需要在**高吞吐量和数据传输安全性方面做权衡**，因为这种模式如果消息在接收到之前，消费者那边出现连接或channel关闭，那么消息就丢失了，当然另一方面这种模式消费者那边可以传递过载的消息，**没有对传递的消息数量进行限制**，当然这样有可能使得消费者这边由于接受太多还来不及处理的消息，导致这些消息的积压，最终使得内存耗尽，最终这些消费者线程被操作系统杀死，**所以这种模式仅适用于在消费者可以高效并以某种速率能够处理这些消息的情况下使用。**

### 3.2.3 消息应答的方式
- **A:Channel.basicAck**
	- 用于肯定确认，RabbitMQ已知道该消息并且成功的处理消息，可以将其丢弃了。
- **Channel.basicNack**
	- 用于否定确认
- **Channel.basicReject**
	- 用于否定确认，相比于Channel.basicNack少一个参数，不处理该消息了直接拒绝，可以将其丢弃了


### 3.2.4 Multiple 解释
手动应答的好处是可以批量应答并且减少网络拥堵
 ![在这里插入图片描述](https://img-blog.csdnimg.cn/2e5a1a6f4a1f4de8b4752bfcf24fbe4f.png#pic_center)

**multiple 的 true 和 false 代表不同意思**
- true 代表批量应答 channel 上未应答的消息
 	- 比如说 channel 上有传送 tag 的消息 5,6,7,8 当前 tag 是8 那么此时
	 - 5-8 的这些还未应答的消息都会被确认收到消息应答
- false 同上面相比
 	- 只会应答 tag=8 的消息 5,6,7 这三个消息依然不会被确认收到消息应答

![在这里插入图片描述](https://img-blog.csdnimg.cn/42e574bb58a84199b2170c7cfdf76423.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_19,color_FFFFFF,t_70,g_se,x_16#pic_center)
### 3.2.5 消息自动重新入队
如果消费者由于某些原因失去连接（其通道已关闭，连接已关闭或TCP连接丢失），导致消息未发送ACK确认，RabbitMQ将了解到消息未完全处理，并将对其重新排队。如果此时其他消费者可以处理。它将很快将其重新分发给另外一个消费者。这样即使某个消费者偶尔死亡，也可以确保不会丢失任何消息。

![在这里插入图片描述](https://img-blog.csdnimg.cn/97a31f5d5692418cb3db2fdaa4d34a31.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
### 3.2.6 消息手动应答代码 
默认消息采用的是自动应答，所以要实现消息消费过程中不丢失，需要把自动应答改成手动应答， 消费者在上面代码的基础上增加了了红色框内容.
![在这里插入图片描述](https://img-blog.csdnimg.cn/232ac557b5e84fdfbea68d49e1ba0c64.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
- **消息生产者**

```java
package com.xujinshan.rabbitmq03;

import com.rabbitmq.client.Channel;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @Author: xujinshan361@163.com
 *
 * 消费消息在手动应答时候不丢失，放回队列重新消费
 */
public class Test02 {
    // 队列名称
    public static final String TASK_QUEUE_NAME="ack_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        channel.queueDeclare(TASK_QUEUE_NAME,false,false,false,null);

        // 从控制台输入信息
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()){
            String next = sc.next();
            channel.basicPublish("",TASK_QUEUE_NAME,null,next.getBytes(StandardCharsets.UTF_8));
            System.out.println("生产者发出消息："+next);
        }
    }
}
```

- **消费者1 - 设置睡眠时间短（1秒）**

```java
package com.xujinshan.rabbitmq03;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;
import com.xujinshan.rabbitmqutils.SleepUtils;

/**
 * @Author: xujinshan361@163.com
 * 消费者
 */
public class Worker03 {
    private static final String ACK_QUEUE_NAME = "ack_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        System.out.println("C1 等待接收消息处理时间较短");
        //消息消费的时候如何处理消息
        DeliverCallback deliverCallback = (consumerTag, delivery)-> {
            String message = new String(delivery.getBody());
            SleepUtils.sleep(1);
            System.out.println("接收到消息:" + message);
            /**
             * 1.消息标记 tag
             * 2.是否批量应答未应答消息
             */
             channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        //采用手动应答
        boolean autoAck = false;
        channel.basicConsume(ACK_QUEUE_NAME, autoAck, deliverCallback, (consumerTag) -> {
            System.out.println(consumerTag + "消费者取消消费接口回调逻辑");
        });
    }
}
```
- **消费者2-设置睡眠时间长（30秒）**

```java
package com.xujinshan.rabbitmq03;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;
import com.xujinshan.rabbitmqutils.SleepUtils;

/**
 * @Author: xujinshan361@163.com
 * 消费者
 */
public class Worker04 {
    private static final String ACK_QUEUE_NAME = "ack_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        System.out.println("C2 等待接收消息处理时间较长");
        //消息消费的时候如何处理消息
        DeliverCallback deliverCallback = (consumerTag, delivery)-> {
            String message = new String(delivery.getBody());
            SleepUtils.sleep(30);
            System.out.println("接收到消息:" + message);
            /**
             * 1.消息标记 tag
             * 2.是否批量应答未应答消息
             */
             channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        //采用手动应答
        boolean autoAck = false;
        channel.basicConsume(ACK_QUEUE_NAME, autoAck, deliverCallback, (consumerTag) -> {
            System.out.println(consumerTag + "消费者取消消费接口回调逻辑");
        });
    }
}
```

- **睡眠工具类**

```java
package com.xujinshan.rabbitmqutils;

/**
 * @Author: xujinshan361@163.com
 * 睡眠工具类
 */
public class SleepUtils {
    /**
     * 睡眠多少秒
     * @param second
     */
    public static void sleep(int second){
        try {
            Thread.sleep(1000*second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```


### 3.2.7手动应答结果解释
![在这里插入图片描述](https://img-blog.csdnimg.cn/a8cf3a02a48346a699b17268782a2bfc.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

![在这里插入图片描述](https://img-blog.csdnimg.cn/5d204d178076441f9510b148b6060ba9.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/0469e8c197cc4ad6867a049e07a7be8b.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)**解释：在发送者发送消息dd，发出消息后把C2 停掉，按理说该C2 来处理消息的，但是由于他处理的时间长，在还未处理完，也就是说C还没执行ack代码的时候，C2被停掉了，此时会看到消息被C1接收了，说明消息dd被重新入队，然后分配给能处理消息的C1处理了**
![在这里插入图片描述](https://img-blog.csdnimg.cn/2120ad4242974916b4cd8bb4ccbc7b73.png#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/52dff425ead547a799fd0e39f0f76281.png#pic_center)



## 3.3 RabbitMQ 持久化
### 3.3.1 概念
上面解决了如何处理任务不丢失的情况，但是如何保障当RabbitMQ服务停掉以后消息生产者发送过来的消息不丢失。默认情况下RabbitMQ退出或由于某种原因奔溃时，它忽视队列和消息，除非告知它不要这样做。确保消息不会丢失需要做两件事 ：**需要将队列和消息都标记为持久化**

### 3.3.2 队列如何实现持久化
之前创建的队列都是非持久化的，RabbitMQ 如果重启的话，该队列就被删除掉，如果要实现队列持久化，需要在队列声明的时候把durable参数设置为持久化

![在这里插入图片描述](https://img-blog.csdnimg.cn/2166c56ee7a14e00bbae911fd7f9725f.png#pic_center)但是需要注意的是如果之前声明的队列不是持久化，需要把原来的队列先删了，或者重新创建一个持久化队列，不然会出现错误。
![在这里插入图片描述](https://img-blog.csdnimg.cn/13e8321ec673443db772cdf5ed008f19.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)


- 以下是控制台中持久化和非持久化的UI显示区域
![在这里插入图片描述](https://img-blog.csdnimg.cn/5930d590aa1b4adc8cbf2493c1d3c24e.png#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/295766950cb54fa1901b96529d40760f.png#pic_center)
- **这时候重启RabbitMQ队列依然存在**

### 3.3.3 消息实现持久化
要想让消息实现持久化需要在消息生产者修改代码，MessageProperties.PERSISTENT_TEXT_PLAIN添加这属性。
![在这里插入图片描述](https://img-blog.csdnimg.cn/500c30e7249e4106b0539091a3af56e8.png#pic_center)
- 将消息标记为持久化并不能完全保证不会丢失消息。尽管它告诉RabbitMQ将消息保存到磁盘，但是这里依然存在当消息刚准备存储在磁盘的时候，但是还没存储完，消息还在缓存的一个间隔点。此时并没有真正写入磁盘。持久性保证并不强，但是对于简单任务队列而言，绰绰有余。如果需要更强有力的持久化策略，需要后面的发布确认。
### 3.3.4 不公平分发
在开始的时候学习到的RabbitMQ分发消息采用的轮询分发，但是在某些场景下这种策略并不是很好，比如说有两个消费者在处理任务，其中一个消费者1处理任务的速度非常快，而另一个消费者2处理速度却是很慢，这个时候如果还是采用轮询分发的话就会导致处理速度快的一个消费者很大一部分时间处理空闲状态，而处理慢的那个消费者一直在干活，这种分配方式在这种情况下其实就不太好，但是RabbitMQ并不知道这种情况，它依然公平的进行分发。
为了避免这种情况，设置参数 channel.basicQos(1);
![在这里插入图片描述](https://img-blog.csdnimg.cn/50edfd88257b44bb93408f5c597e5af0.png#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/c10d36857b5c47beb799a03371503549.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)![在这里插入图片描述](https://img-blog.csdnimg.cn/2981ef3d879146f3a2ef37d3e3609b48.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

意思就是如果这个任务还没处理完或者我还没应答你，你先别分配给我，我目前只能处理一个任务，然后RabbitMQ就会把该任务分配给没有那么忙的那个空闲消费者，当如果所有的消费者都没有完成手上的任务，队列还在不停的添加新任务，队列有可能会遇到队列被撑满的情况。这时候就只能添加新的worker或者改变其他存储任务的策略。

- **结果展示：C1处理消息快，C2处理消息慢**

![在这里插入图片描述](https://img-blog.csdnimg.cn/1549b802690c4130a35a667de801f19e.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/d16c580db0aa44508de5147963f89c87.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/939e57a81c6b4409bb66c111d27d3624.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
### 3.3.5 预取值
![在这里插入图片描述](https://img-blog.csdnimg.cn/851d78dba61e43cba2a047f106bfd53e.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16)
- 本身消息的发送就是异步发送的，所以在任何时候，channel上肯定不止只有一个消息。另外来自消费者的手动确认本质也是异步的。
- 这里就存在一个为确认的消息缓冲区，因此希望开发人员能**限制此缓冲区的大小，以避免缓冲区里面无限的未确认消息问题**。这时候就可以通过使用channel.basicQos()方法设置“预取计数器”值来完成。**该值定义通道上允许的未确认消息的最大数量。**
-  一旦数量达到配置的数量，RabbitMQ 将停止在通道上传递更多消息，除非至少有一个未处理的消息被确认。
- 例如，假设在通道上有未确认的消息 5、6、7，8，并且通道的预取计数设置为 4，此时RabbitMQ 将不会在该通道上再传递任何消息，除非至少有一个未应答的消息被 ack。比方说 tag=6 这个消息刚刚被确认 ACK，RabbitMQ 将会感知这个情况到并再发送一条消息。
- 消息应答和 QoS 预取值对用户吞吐量有重大影响。通常，增加预取将提高向消费者传递消息的速度。**虽然自动应答传输消息速率是最佳的，但是，在这种情况下已传递但尚未处理的消息的数量也会增加，从而增加了消费者的 RAM 消耗**(随机存取存储器)应该小心使用具有无限预处理的自动确认模式或手动确认模式，消费者消费了大量的消息如果没有确认的话，会导致消费者连接节点的内存消耗变大，所以找到合适的预取值是一个反复试验的过程，不同的负载该值取值也不同 100 到 300 范围内的值通常可提供最佳的吞吐量，并且不会给消费者带来太大的风险。
- 预取值为 1 是最保守的。当然这将使吞吐量变得很低，特别是消费者连接延迟很严重的情况下，特别是在消费者连接等待时间较长的环境中。对于大多数应用来说，稍微高一点的值将是最佳的。



![在这里插入图片描述](https://img-blog.csdnimg.cn/bd2801b0fd1b4ae0a69cf351a9f1a9ec.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
# 4.发布确认
## 4.1 发布确认原理
 ![在这里插入图片描述](https://img-blog.csdnimg.cn/4ce29dba96454dec8a036b05ea53691e.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
 - 生产者将信道设置成confirm模式，一旦信道进入confirm模式，**所有在该信道上面发布的消息都将会被指派一个唯一的ID（从1开始）**，一旦消息被投递到所有匹配的队列后，broker就会发送一个确认给生产者（包含消息的唯一ID），这就使得生产者知道消息已经正确到达目的队列了。
 - 如果消息和队列都是可持久化的，那么确认消息会在将消息写入磁盘之后发出，broker回传给生产者的确认消息中delivery-tag域中包含了确认消息的序列号。
 - 此外broker 也可以设置basicAck的multiple域，表示到这个序号之前的所有消息都已经得到了处理。
 - confirm模式最大的好处在于它是异步的，一旦发布一条消息，生产者应用程序就可以在等信道返回确认的同时继续发送下一条消息，当消息最终得到确认之后，生产者应用便可以通过回调方法来处理该确认消息，如果RabbitMQ因为自身内部错误导致消息丢失，就会发送一条nack消息，生产者应用程序同样可以在回调方法中处理该nack消息。


## 4.2 发布确认的策略
### 4.2.1 开启发布确认的方法
发布确认默认是没有开启的，如果要开启需要调用方法 confirmSelect，每当你要想使用发布确认，都需要在channel上调用该方法。
![在这里插入图片描述](https://img-blog.csdnimg.cn/c4854c42c49549c3839d129e3034e830.png#pic_center)
### 4.2.2 单个确认发布
- 一种简单的确认方式，它是一种**同步确认发布**的方式，也就是发布一个消息之后只有它
被确认发布，后续的消息才能继续发布,waitForConfirmsOrDie(long)这个方法只有在消息被确认
的时候才返回，如果在指定时间范围内这个消息没有被确认那么它将抛出异常。
- 这种确认方式有一个最大的缺点就是:**发布速度特别的慢**，因为如果没有确认发布的消息就会
阻塞所有后续消息的发布，这种方式最多提供每秒不超过数百条发布消息的吞吐量。当然对于某
些应用程序来说这可能已经足够了。

### 4.2.3 批量确认发布
- 上面那种方式非常慢，与单个等待确认消息相比，先发布一批消息然后一起确认可以极大地
提高吞吐量，当然这种方式的缺点就是:**当发生故障导致发布出现问题时，不知道是哪个消息出现
问题了**，必须将整个批处理保存在内存中，以记录重要的信息而后重新发布消息。当然这种
方案仍然是同步的，也一样阻塞消息的发布。


### 4.2.4 异步确认发布
- 异步确认虽然编程逻辑比上两个要复杂，但是性价比最高，无论是可靠性还是效率都没得说，
是利用回调函数来达到消息可靠性传递的，这个中间件也是通过函数回调来保证是否投递成功，
下面就让我们来详细讲解异步确认是怎么实现的。

![在这里插入图片描述](https://img-blog.csdnimg.cn/4abe3029e9b84e249815d8a33e84b816.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

```java
package com.xujinshan.rabbitmq04;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @Author: xujinshan361@163.com
 * 发布确认
 * 使用哪个时间，比较哪种确认方式是最好的
 * 1.单个确认发布       耗时：598ms
 * 2.批量确认发布       耗时：119ms
 * 3.异步确认发布       耗时：90ms
 */
public class ConfirmMessage {
    // 批量发消息的个数
    public static final Integer MESSAGE_COUNT =1000;
    public static void main(String[] args) throws Exception {
        // 1.单个确认发布
      //  ConfirmMessage.publishMessageIndividually();

        // 2.批量确认发布
//        ConfirmMessage.publishMessageBatch();
        // 3.异步确认发布
        ConfirmMessage.publishMessageAsync();
    }

    /**
     * 单个确认发布
     * @throws Exception
     */
    public static void publishMessageIndividually() throws Exception{
        Channel channel = RabbitMQUtils.getChannel();
        // 队列的声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName,true,false,false,null);
        // 开启发布确认
        channel.confirmSelect();
        // 开始时间
        long begin = System.currentTimeMillis();

        // 批量发消息
        for (Integer i = 0; i < MESSAGE_COUNT; i++) {
            String message = i+"";
            channel.basicPublish("",queueName,null,message.getBytes(StandardCharsets.UTF_8));
            // 单个消息就马上进行发布确认
            boolean b = channel.waitForConfirms();
            if(b){
                System.out.println("消息发送成功");
            }
        }
        // 结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个单独确认消息，耗时：" + (end - begin) + "ms");
    }

    /**
     * 批量确认发布
     * @throws Exception
     */
    public static void publishMessageBatch() throws Exception{
        Channel channel = RabbitMQUtils.getChannel();
        // 队列的声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName,true,false,false,null);
        // 开启发布确认
        channel.confirmSelect();
        // 开始时间
        long begin = System.currentTimeMillis();

        // 批量确认消息大小
        int batchSize =100;
        // 批量发送消息 批量确认发布
        for (Integer i = 0; i < MESSAGE_COUNT; i++) {
            String message = i+"";
            channel.basicPublish("",queueName,null,message.getBytes(StandardCharsets.UTF_8));
            // 判断达到100条消息，批量确认一次
            if(i%batchSize ==0){
                // 发布确认
                channel.waitForConfirms();
            }
        }
        // 结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个批量确认消息，耗时：" + (end - begin) + "ms");
    }

    /**
     * 异步确认发布
     * @throws Exception
     */
    public static void publishMessageAsync() throws Exception{
        Channel channel = RabbitMQUtils.getChannel();
        // 队列的声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName,true,false,false,null);
        // 开启发布确认
        channel.confirmSelect();
        // 开始时间
        long begin = System.currentTimeMillis();
        // 消息确认成功回调函数
        ConfirmCallback ackCallback = (deliveryTag,multiple)->{
            System.out.println("确认消息：" + deliveryTag);
        };
        /**
         * 消息确认失败回调函数
         * 参数：
         *      1.消息的标记
         *      2.是否为批量确认
         */
        ConfirmCallback nackCallback = (deliveryTag,multiple)->{
            System.out.println("未确认消息：" + deliveryTag);
        };
        // 准备消息的监听器，监听哪些消息成功，哪些消息失败
        /**
         * 1.监听哪些消息成功了
         * 2.监听哪些消息失败了
         */
        channel.addConfirmListener(ackCallback,nackCallback);  //异步通知

        // 批量发送消息 异步批量确认
        for (Integer i = 0; i < MESSAGE_COUNT; i++) {
            String message = i+"";
            channel.basicPublish("",queueName,null,message.getBytes(StandardCharsets.UTF_8));

        }
        // 结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个异步发布确认消息，耗时：" + (end - begin) + "ms");
    }
}
```

### 4.2.5 如何处理异步未确认消息
最好的解决方法就是把未确认的消息放到一个基于内存的能被发布线程访问的队列，这个队列在confrim callbacks 与发布线程之间进行消息的传递。

```java
package com.xujinshan.rabbitmq05;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @Author: xujinshan361@163.com
 * 解决发布未确认问题
 */
public class ConfirmMessage {
    // 批量发消息的个数
    public static final Integer MESSAGE_COUNT =1000;
    public static void main(String[] args) throws Exception {

        // 异步确认发布
        ConfirmMessage.publishMessageAsync();
    }

    /**
     * 异步确认发布
     * @throws Exception
     */
    public static void publishMessageAsync() throws Exception{
        Channel channel = RabbitMQUtils.getChannel();
        // 队列的声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName,true,false,false,null);
        // 开启发布确认
        channel.confirmSelect();

        /**
         * 处理并发的有序map集合，ConcurrentHashMap是无序的
         * 1、轻松的将序号与消息进行关联
         * 2.轻松批量删除条目，只要给到序号
         * 3.支持高并发（多线程）
         */
        ConcurrentSkipListMap<Long,String> map = new ConcurrentSkipListMap<>();
        // 开始时间
        long begin = System.currentTimeMillis();
        // 消息确认成功回调函数
        ConfirmCallback ackCallback = (deliveryTag,multiple)->{
            // 2、删除掉已经确认的消息，剩下的都是未确认消息
            System.out.println("确认消息：" + deliveryTag);
            // 返回消息的时候是批量确认的，批量确认需要判断
            if(multiple){
                ConcurrentNavigableMap<Long,String> confirmed = map.headMap(deliveryTag,true);
                confirmed.clear();
            }else {
                map.remove(deliveryTag);
            }
            if(map.isEmpty()){
                System.out.println("所有消息发送成功！");
            }
        };
        /**
         * 消息确认失败回调函数
         * 参数：
         *      1.消息的标记
         *      2.是否为批量确认
         */
        ConfirmCallback nackCallback = (deliveryTag,multiple)->{
            // 3、打印一下未确认的消息有哪些
            System.out.println("未确认消息：" + deliveryTag);
        };
        // 准备消息的监听器，监听哪些消息成功，哪些消息失败
        /**
         * 1.监听哪些消息成功了
         * 2.监听哪些消息失败了
         */
        channel.addConfirmListener(ackCallback,nackCallback);  //异步通知

        // 批量发送消息 异步批量确认
        for (Integer i = 0; i < MESSAGE_COUNT; i++) {
            String message = i+"";
            channel.basicPublish("",queueName,null,message.getBytes(StandardCharsets.UTF_8));
            // 1、记录下所有的要发送的消息 消息总和
            // channel.getNextPublishSeqNo()  获得的是下一个发布的序列号，当前序列号需要减一！
            map.put(channel.getNextPublishSeqNo()-1,message);
        }
        // 结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个异步发布确认消息，耗时：" + (end - begin) + "ms");
    }
}
```


### 4.2.6 以上三种发布确认速度对比
- 单独发布确认：同步等待确认，简单，但吞吐量非常有限
- 批量发布确认：批量同步等待确认，简单，合理的吞吐量，一旦出现问题很难推断出是哪条消息出现了问题
- 异步处理：最佳性能和资源使用，在出现错误的情况下可以很好地控制，但是实现起来稍微麻烦

# 5. 交换机
- 上面创建了一个工作队列，假设的是工作队列背后，每个人任务都恰好交付给一个消费者（工作进程）。在这一部分，将做一些完全不同的事情：将消息传达个多个消费者。这种模式称为“**发布/订阅**”
- 为了说明这种模式，将构建一个简单的日志系统。它将由俩个程序组成：第一个程序将发出日志消息，第二个程序是消费者。其中会启动两个消费者，其中一个消费者接受到消息后把日志存储在磁盘，另一个消费者接受到消息后把消息打印到屏幕，事实上第一个程序发出的日志消息将广播给所有消费者。
## 5.1 Exchange
### 5.1.1 Exchange概念
RabbitMQ消息传递模型的核心思想是：**生产者生产的消息从不会直接发送到队列**。实际上，通常生产者甚至不知道这些消息传递到了哪些队列中。

相反，**生产者只能将消息发送到交换机（Exchange）**，交换机工作的内容非常简单，一方面它接收来自生产者的消息，另一方面将它们推入队列。交换机必须确切知道如何处理收到的消息。是应该把这些消息放到特定的队列还是说把他们放到许多队列中还是说丢弃他们。这就是由交换机的类型来决定的。

![在这里插入图片描述](https://img-blog.csdnimg.cn/9314297eb7684dc599d3cf31e689bd39.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_16,color_FFFFFF,t_70,g_se,x_16#pic_center)
### 5.1.2 Exchange的类型
直接（direct）、主题（topic）、标题（headers）、扇出（fanout）

### 5.1.3 无名 Exchange
前面没有使用到Exchange，但是仍然能够将消息发送到队列。之前能实现的原因是因为我们使用的是默认交换机，通常用空字符串（“”）进行标识。
![在这里插入图片描述](https://img-blog.csdnimg.cn/2bee447666b1438abd92a3614cc652a0.png#pic_center)第一个参数是交换机的名称。空字符串表示默认交换机或无名称交换机：消息能路由发送到队列中其实是由routingKey(bindingKey） 绑定key指定的，如果它存在的话。

## 5.2 临时队列
之前使用的是具有特定名称的队列（hello和ack_queue）。队列的名称至关重要，我们需要指定我们的消费者去消费哪个队列的消息。
每当连接到RabbitMQ时候，都需要一个全新的空队列，为此可以创建一个具有**随机名称的队列**，或能让服务器为我们选择一个随机队列名称那就更好了。其次**一旦我们断开了消费者的联机，队列将被自动删除。**

- 创建的临时队列的方式如下：
```java
String queueName = channel.queueDeclare().getQueue();
```
- 创建出来之后长成这样：
![在这里插入图片描述](https://img-blog.csdnimg.cn/a0c182baa63d4a8abd01287b52bcdf73.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
## 5.3 绑定（bindings）
bindings其实是Exchange和queue之间的桥梁，它告诉我们Exchange和哪个队列进行了绑定关系。 
![在这里插入图片描述](https://img-blog.csdnimg.cn/8725154441894b92a843907fcbd2364c.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
## 5.4 Fanout
### 5.4.1 Fanout介绍
Fanout类型非常简单，它将接收到的所有消息**广播**到它知道的所有队列中，系统中默认有些exchange。

![在这里插入图片描述](https://img-blog.csdnimg.cn/773666c2a0fc4b1fa08d5e23aa6ea0ba.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
### 5.4.2 Fanout 实战
![在这里插入图片描述](https://img-blog.csdnimg.cn/b27dc04b0a504547868c553fe68d531b.png)
- Logs和临时队列的绑定关系如下图
![在这里插入图片描述](https://img-blog.csdnimg.cn/e8d87bba3a9e4b0facc37dca9d9d2fe8.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_19,color_FFFFFF,t_70,g_se,x_16#pic_center)- **ReceiveLogs01 将接收到的消息打印在控制台**

```java
package com.xujinshan.rabbitmq06;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

/**
 * @Author: xujinshan361@163.
 * ReceiveLogs01 将接收到的消息打印给到控制台
 *
 */
public class ReceiveLogs01 {
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        // 声明交换机-- fanout类型
        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
        /**
         * 生成一个临时的队列 队列的名称是随机的
         * 当消费者断开和该队列的连接  队列自动删除
         */
        String queueName = channel.queueDeclare().getQueue();
        // 把该临时队列绑定我们的exchange，其中routingKey(也称为bindings key) 为空串
        channel.queueBind(queueName,EXCHANGE_NAME,"");
        System.out.println("等待接收消息，把接收到的消息打印到屏幕....");
        // 接收消息
        DeliverCallback deliverCallback = (consumerTag,delivery)->{
            String message = new String(delivery.getBody(),"UTF-8");
            System.out.println("控制台打印接收到的消息："+ message);
        };

        channel.basicConsume(queueName,true,deliverCallback,consumerTag->{});
    }
}
```

- **ReceiveLogs02 将接收到的消息存储在磁盘**

```java
package com.xujinshan.rabbitmq06;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * @Author: xujinshan361@163.
 * ReceiveLogs02 将接收到的消息存储在磁盘
 *
 */
public class ReceiveLogs02 {
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        // 声明交换机-- fanout类型
        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
        /**
         * 生成一个临时的队列 队列的名称是随机的
         * 当消费者断开和该队列的连接  队列自动删除
         */
        String queueName = channel.queueDeclare().getQueue();
        // 把该临时队列绑定我们的exchange，其中routingKey(也称为bindings key) 为空串
        channel.queueBind(queueName,EXCHANGE_NAME,"");
        System.out.println("等待接收消息，把接收到的消息写入文件....");
        // 接收消息
        DeliverCallback deliverCallback = (consumerTag,delivery)->{
            String message = new String(delivery.getBody(),"UTF-8");
            File file = new File("E:\\RabbitMQ\\rabbitmq-hello\\a.txt");
            FileUtils.writeStringToFile(file,message,"UTF-8");
            System.out.println("写入数据成功");
        };

        channel.basicConsume(queueName,true,deliverCallback,consumerTag->{});
    }
}
```

- **EmitLog 发送消息给两个消费者接收**

```java
package com.xujinshan.rabbitmq06;

import com.rabbitmq.client.Channel;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @Author: xujinshan361@163.com
 * EmitLog 发送消息给两个消费者接收
 */
public class EmitLog {
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        /**
         * 声明一个exchange
         * 1.exchange 的名称
         * 2.exchange 的类型
         */
        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
        Scanner sc = new Scanner(System.in);
        System.out.println("输入信息：");
        while (sc.hasNext()){
            String  message = sc.nextLine();
            channel.basicPublish(EXCHANGE_NAME,"",null,message.getBytes(StandardCharsets.UTF_8));
            System.out.println("生产者发出消息："+message);
        }
    }
}
```

 - ** 结果展示**

![在这里插入图片描述](https://img-blog.csdnimg.cn/e170540db5594f9fad6ba696b3f10c08.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/d5632579334c40e0b4f76efb81271835.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/a89fdc8960444f988d605f3b439a19d6.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
- 通过结果展示可以看出fanout类型的效果，实现广播功能

## 5.5 Direct Exchange
### 5.5.1 回顾
- 上面，构造了一个简单 日志记录系统。能够向许多接收者广播日志消息。在这里将添加一些特别的功能-比如：只让某个消费者订阅发布的部分消息。例如只把严重错误信息定向到日志文件（以节省磁盘空间），同时仍然能够在控制器上打印所有日志消息。
- bindings是交换机和队列之间的桥梁关系。也可以这么理解：**队列只对它绑定的交换机的消息感兴趣**。绑定用参数routingKey来表示也可以称为binding key，创建绑定用代码：channel.ququeBind(queueName,EXCHANGE_NAME,"routingKey")；**绑定之后的意义由其交换机类型决定**。

### 5.5.2 Direct exchange介绍
上面的日志系统将所有消息广播给所有消费者，这里需要做一些改变。例如希望日志消息写入磁盘的程序仅接受严重错误（errors），而不存储哪些警告（warning）或信息（info）日志消息，避免浪费磁盘空间。Fanout这种交换机类型不能带来很大的灵活性，它只能进行无意识的广播。这里将使用direct 类型进行替换，这种工作方式是：消息只能去到它绑定的routingKey 队列去。

![在这里插入图片描述](https://img-blog.csdnimg.cn/d094e1f2cb5649129ef58f7eb968376d.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_19,color_FFFFFF,t_70,g_se,x_16#pic_center)可以看到X绑定了两个队列，绑定类型是direct。队列Q1 绑定键为orange，队列Q2 绑定键有两个：一个绑定键为black，另一个绑定键为green。
在这种绑定情况下，生产者发布消息到exchange上，绑定键为orange的消息会被发布到对队列Q1。绑定键为black、green的消息被发布到队列Q2 ，其他消息类型的消息将被丢弃。

### 5.5.3 多重绑定
![在这里插入图片描述](https://img-blog.csdnimg.cn/c762aba985a1498b9b25d2b1f7bd6c22.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_17,color_FFFFFF,t_70,g_se,x_16#pic_center)如果exchange的绑定类型是direct，**但是它绑定的多个队列的key如果都相同**，在这种情况下虽然绑定的类型是direct **但是它的表现就和fanout有点类似了** ，和广播差不多。

### 5.5.4 实战
![在这里插入图片描述](https://img-blog.csdnimg.cn/1add818250aa4d6faedb7d2ec66b081d.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_15,color_FFFFFF,t_70,g_se,x_16#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/f96c87c359d84705b02e224086396bb0.png#pic_center)
- **ReceiveLogDirect01绑定info 和warning**
```java
package com.xujinshan.rabbitmq07;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

/**
 * @Author: xujinshan361@163.com
 */
public class ReceiveLogDirect01 {
    public static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        // 声明一个交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        // 声明一个队列
        channel.queueDeclare("console", false, false, false, null);
        // 进行绑定交换机和队列
        channel.queueBind("console", EXCHANGE_NAME, "info");
        channel.queueBind("console", EXCHANGE_NAME, "warning");
        // 接收消息
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("ReceiveLogDirect02控制台打印接收到的消息：" + message);
        };

        channel.basicConsume("console", true, deliverCallback, consumerTag -> {
        });
    }
}

```
- **ReceiveLogDirect02绑定error**

```java
package com.xujinshan.rabbitmq07;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

/**
 * @Author: xujinshan361@163.com
 */
public class ReceiveLogDirect02 {
    public static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        // 声明一个交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        // 声明一个队列
        channel.queueDeclare("disk", false, false, false, null);
        // 进行绑定交换机和队列
        channel.queueBind("disk", EXCHANGE_NAME, "error");
        // 接收消息
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("ReceiveLogDirect02控制台打印接收到的消息：" + message);
        };

        channel.basicConsume("disk", true, deliverCallback, consumerTag -> {
        });
    }
}
```
- **DirectLogs消息发送**
```java
package com.xujinshan.rabbitmq07;

import com.rabbitmq.client.Channel;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @Author: xujinshan361@163.com
 */
public class  {
    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();

        Scanner sc = new Scanner(System.in);
        System.out.println("输入信息：");
        while (sc.hasNext()) {
            String message = sc.nextLine();
            String routingKey = "error";   // 通过改变值进行测试 error、info、warning
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("生产者发出消息：" + message);
        }
    }
}

```

## 5.6 Topic
### 5.6.1 之前类型的问题
- 改进的日志系统，没有使用随意广播的fanout交换机，而是使用了direct交换机，从而实现了选择性接收日志。
- 尽管使用direct交换机改进了系统，但是仍然存在局限性-比如想接收的日志类型有info.base 和info.advantage，某个队列只想info.base的消息，那么这个时候direct就办不到了。这个时候只能使用topic类型


### 5.6.2 Topic要求

- 发送到类型是 topic 交换机的消息的 routing_key 不能随意写，必须满足一定的要求，它**必须是一个单词列表，以点号分隔开**。这些单词可以是任意单词，比如说："stock.usd.nyse", "nyse.vmw", "quick.orange.rabbit".这种类型的。当然这个单词列表最多不能超过 255 个字节。
- 在这个规则列表中，其中有两个替换符是大家需要注意的
- ***(星号)可以代替一个单词**
- **#(井号)可以替代零个或多个单词**


### 5.6.3 Topic 匹配案例
下图绑定关系如下：
- Q1-->绑定的是中间带orange带三个单词的字符串（\*.orange.\*）
- Q2-->绑定的是最后一个单词是rabbit的三个单词（\*.\*.rabbit）和第一个单词是lazy的多个单词（lazy.#）

![在这里插入图片描述](https://img-blog.csdnimg.cn/80c5d15489bb4a508f2bb06623f6eec4.png#pic_center)

- **上图是一个队列绑定关系图，下面看看数据接收情况**
- quick.orange.rabbit 被队列 Q1Q2 接收到
- lazy.orange.elephant 被队列 Q1Q2 接收到
- quick.orange.fox 被队列 Q1 接收到
- lazy.brown.fox 被队列 Q2 接收到
- lazy.pink.rabbit 虽然满足两个绑定但只被队列 Q2 接收一次
- quick.brown.fox 不匹配任何绑定不会被任何队列接收到会被丢弃
- quick.orange.male.rabbit 是四个单词不匹配任何绑定会被丢弃
- lazy.orange.male.rabbit 是四个单词但匹配 Q2

当绑定关系是下列这种情况时，需要引起注意

- **当一个队列绑定键是#，那么这个队列将接收所有的数据，有点像fanout**
- **如果队列绑定键中没有出现# 和\* ，那么该队列绑定类型就是direct了**

### 5.6.4 实战
![在这里插入图片描述](https://img-blog.csdnimg.cn/b99ba350256a42ad9d9ea3576d2cc04d.png#pic_center)
- **EmitLogTopic 生产者**

```java
package com.xujinshan.rabbitmq08;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xujinshan361@163.com
 * topic主题，生产者
 */
public class EmitLogTopic {
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        /**
         * Q1-->绑定的是
         * 中间带 orange 带 3 个单词的字符串(*.orange.*)
         * Q2-->绑定的是
         * 最后一个单词是 rabbit 的 3 个单词(*.*.rabbit)
         * 第一个单词是 lazy 的多个单词(lazy.#)
         *
         */
        Map<String, String> bindingKeyMap = new HashMap<>();
        bindingKeyMap.put("quick.orange.rabbit", "被队列 Q1Q2 接收到");
        bindingKeyMap.put("lazy.orange.elephant", "被队列 Q1Q2 接收到");
        bindingKeyMap.put("quick.orange.fox", "被队列 Q1 接收到");
        bindingKeyMap.put("lazy.brown.fox", "被队列 Q2 接收到");
        bindingKeyMap.put("quick.brown.fox", "不匹配任何绑定不会被任何队列接收到会被丢弃");
        bindingKeyMap.put("quick.orange.male.rabbit", "是四个单词不匹配任何绑定会被丢弃");
        bindingKeyMap.put("lazy.orange.male.rabbit", "是四个单词但匹配 Q2");
        for (Map.Entry<String, String> bindingKeyEntry : bindingKeyMap.entrySet()) {
            String bindingKey = bindingKeyEntry.getKey();
            String message = bindingKeyEntry.getValue();
            channel.basicPublish(EXCHANGE_NAME, bindingKey, null, message.getBytes("UTF-8"));
            System.out.println("生产者发出消息" + message);
        }
    }
}
```
- **ReceiveLogTopic01 第一个消费者**
```java
package com.xujinshan.rabbitmq08;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

/**
 * @Author: xujinshan361@163.com
 * 声明topic主题交换机，及相关队列
 * 消费者C1
 */
public class ReceiveLogTopic01 {
    // 交换机名称
    public static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        // 声明交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        // 声明队列
        String queueName = "Q1";
        channel.queueDeclare(queueName,false,false,false,null);
        channel.queueBind(queueName,EXCHANGE_NAME,"*.orange.*");
        System.out.println("等待接收消息。。。");
        // 接收消息
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("接收队列："+queueName+"绑定键：" + delivery.getEnvelope().getRoutingKey());
        };
        channel.basicConsume(queueName,deliverCallback,consumerTag->{});
    }
}

```
- **ReceiveLogTopic02 第二个消费者**
```java
package com.xujinshan.rabbitmq08;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

/**
 * @Author: xujinshan361@163.com
 * 声明topic主题交换机，及相关队列
 * 消费者C2
 */
public class ReceiveLogTopic02 {
    // 交换机名称
    public static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        // 声明交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        // 声明队列
        String queueName = "Q2";
        channel.queueDeclare(queueName,false,false,false,null);
        channel.queueBind(queueName,EXCHANGE_NAME,"*.*.rabbit");
        channel.queueBind(queueName,EXCHANGE_NAME,"lazy.#");
        System.out.println("等待接收消息。。。");
        // 接收消息
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("接收队列："+queueName+"绑定键：" + delivery.getEnvelope().getRoutingKey());
        };
        channel.basicConsume(queueName,deliverCallback,consumerTag->{});
    }
}

```

### 5.6.5 结果
![在这里插入图片描述](https://img-blog.csdnimg.cn/502060d033ad4316b8c6e1ce0eb0e9b2.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)![在这里插入图片描述](https://img-blog.csdnimg.cn/8a1e9b673bc44ae4b2f21efb5f751758.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/b2ee8e7d875b4489bfa4185367d4e2ed.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
# 6. 死信队列
## 6.1 概念
- 死信顾名思义就是无法被消费的消息，字面意思可以这样理解，一般来说，producer将消息投递到broker或直接到queue里了，consumer从queue取出消息进行消费。但是某些时候由于特定的**原因导致queue中的某些消息无法被消费**，这样的消息如果没有后续的处理，就变成了死心，有了死信就有了死信队列。
- 应用场景：为了保证订单业务的消息数据不丢失，需要使用到RabbitMQ的死信队列机制，当消息消费发生异常时，将消息投入死信队列中。还有如：用户在商城下单成功点击去支付后在指定时间未支付时自动失效。

## 6.2 死信的来源
- 消息TTL过期
- 队列达到最大长度（队列满了，无法再添加数据到MQ中）
- 消息被拒绝（basicReject或basicNack）且requeue=false

## 6.3 死信实战
### 6.3.1 代码架构图

![在这里插入图片描述](https://img-blog.csdnimg.cn/757618b456f644d9b25a2b6611bb1abb.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_18,color_FFFFFF,t_70,g_se,x_16#pic_center)

### 6.3.2 消息TTL过期
- **生产者代码**

```java
package com.xujinshan.rabbitmq09;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.nio.charset.StandardCharsets;

/**
 * @Author: xujinshan361@163.com
 * 死信队列 生产者
 */
public class Producer {
    // 普通交换机名称
    public static final String NORMAL_EXCHANGE = "normal_exchange";
    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        // 延迟消息，死信消息 设置TTL时间 time to live (10000ms)
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().expiration("10000").build();
        for (int i = 0; i < 10; i++) {
            String message = "info" +i;
            channel.basicPublish(NORMAL_EXCHANGE,"zhangsan",properties,message.getBytes(StandardCharsets.UTF_8));
            System.out.println("生产者发送消息："+message);
        }

    }
}
```

- **消费者1代码**（**`启动之后关闭掉，模拟其接收不到消息`**）

```java
package com.xujinshan.rabbitmq09;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xujinshan361@163.com
 * 死信队列
 * 消费者1
 */
public class Consumer01 {
    // 普通交换机名称
    public static final String NORMAL_EXCHANGE = "normal_exchange";
    // 死信交换机名称
    public static final String DEAD_EXCHANGE = "dead_exchange";
    // 普通队列名称
    public static final String NORMAL_QUEUE = "normal_queue";
    // 死信对列名称
    public static final String DEAD_QUEUE = "dead_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        // 声明死信和普通交换机 类型为direct
        channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(DEAD_EXCHANGE, BuiltinExchangeType.DIRECT);

        // 声明死信和普通队列
        Map<String,Object> arguments = new HashMap<>();
        // 过期时间--一般由发送者设定
//        arguments.put("x-message-ttl",10*1000);
        // 正常队列设置死信交换机
        arguments.put("x-dead-letter-exchange",DEAD_EXCHANGE);
        // 设置死信routingKey
        arguments.put("x-dead-letter-routing-key","lisi");
        channel.queueDeclare(NORMAL_QUEUE,false,false,false,arguments);  // 普通队列
        channel.queueDeclare(DEAD_QUEUE,false,false,false,null);    // 死信队列

        // 绑定普通的交换机与队列
        // 绑定死信的交换机与队列
        channel.queueBind(NORMAL_QUEUE,NORMAL_EXCHANGE,"zhangsan");
        channel.queueBind(DEAD_QUEUE,DEAD_EXCHANGE,"lisi");

        System.out.println("等待接收消息。。。。");
        DeliverCallback deliverCallback = (consumerTag,message)->{
            System.out.println("Consumer1接收的消息是："+ new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(NORMAL_QUEUE,true,deliverCallback,consumerTag->{});
    }

}
```


- **生产者未发送消息**
![在这里插入图片描述](https://img-blog.csdnimg.cn/863641ce875a4f95a46ff327692f9a0e.png#pic_center)
- **`生产者发送10条数据 此时正常消息队列有10条未读消息`**

![在这里插入图片描述](https://img-blog.csdnimg.cn/0dadbd19888b4e71ab68133da2694745.png#pic_center)

- **`生产者发送10条数据 此时正常消息队列有10条未读消息`**

![在这里插入图片描述](https://img-blog.csdnimg.cn/ceee2b93cc824d54bf3319a78c356fc9.png#pic_center)
- **消费者C2 代码（以上步骤完成 启动C2 消费者，它消费死信队列里面的消息）**

```java
package com.xujinshan.rabbitmq09;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;


/**
 * @Author: xujinshan361@163.com
 * 死信队列
 * 消费者2
 */
public class Consumer02 {

    // 死信对列名称
    public static final String DEAD_QUEUE = "dead_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();

        System.out.println("等待接收消息。。。。");
        DeliverCallback deliverCallback = (consumerTag,message)->{
            System.out.println("Consumer2接收死信队列的消息是："+ new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(DEAD_QUEUE,true,deliverCallback,consumerTag->{});
    }

}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/e80befc2d551438b89e23968c817ca90.png#pic_center)![在这里插入图片描述](https://img-blog.csdnimg.cn/d5ba5b015b74460d928d7d1222bb39a2.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
### 6.3.3 队列达到最大长度
- 消费者去掉TTL属性

```java
package com.xujinshan.rabbitmq10;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.nio.charset.StandardCharsets;

/**
 * @Author: xujinshan361@163.com
 * 死信队列 生产者
 * 队列达到最大长度
 */
public class Producer {
    // 普通交换机名称
    public static final String NORMAL_EXCHANGE = "normal_exchange";
    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        for (int i = 0; i < 10; i++) {
            String message = "info" +i;
            channel.basicPublish(NORMAL_EXCHANGE,"zhangsan",null,message.getBytes(StandardCharsets.UTF_8));
            System.out.println("生产者发送消息："+message);
        }

    }
}
```
- C1 消费者修改下面代码（**启动之后关闭该消费者，模拟接收不到消息**）

![在这里插入图片描述](https://img-blog.csdnimg.cn/c8a1da01ebd54cdb8b569b3a700a6dc3.png)

```java
package com.xujinshan.rabbitmq10;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xujinshan361@163.com
 * 死信队列 -- 队列达到最大长度
 * 消费者1
 */
public class Consumer01 {
    // 普通交换机名称
    public static final String NORMAL_EXCHANGE = "normal_exchange";
    // 死信交换机名称
    public static final String DEAD_EXCHANGE = "dead_exchange";
    // 普通队列名称
    public static final String NORMAL_QUEUE = "normal_queue";
    // 死信对列名称
    public static final String DEAD_QUEUE = "dead_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        // 声明死信和普通交换机 类型为direct
        channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(DEAD_EXCHANGE, BuiltinExchangeType.DIRECT);

        // 声明死信和普通队列
        Map<String,Object> arguments = new HashMap<>();
        // 过期时间--一般由发送者设定
//        arguments.put("x-message-ttl",10*1000);
        // 正常队列设置死信交换机
        arguments.put("x-dead-letter-exchange",DEAD_EXCHANGE);
        // 设置死信routingKey
        arguments.put("x-dead-letter-routing-key","lisi");
        // 设置正常队列长度的限制
        arguments.put("x-max-length",6);
        channel.queueDeclare(NORMAL_QUEUE,false,false,false,arguments);  // 普通队列
        channel.queueDeclare(DEAD_QUEUE,false,false,false,null);    // 死信队列

        // 绑定普通的交换机与队列
        // 绑定死信的交换机与队列
        channel.queueBind(NORMAL_QUEUE,NORMAL_EXCHANGE,"zhangsan");
        channel.queueBind(DEAD_QUEUE,DEAD_EXCHANGE,"lisi");

        System.out.println("等待接收消息。。。。");
        DeliverCallback deliverCallback = (consumerTag,message)->{
            System.out.println("Consumer1接收的消息是："+ new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(NORMAL_QUEUE,true,deliverCallback,consumerTag->{});
    }

}
```
- 注意：如果启动不成功，需要检查原来的队列是否被删除，因为参数改变了无法创建同名队列。

![在这里插入图片描述](https://img-blog.csdnimg.cn/1484936ec84c49e0a80fabb4c0406bf6.png#pic_center)- C2 消费者代码不变（启动C2 消费）
![在这里插入图片描述](https://img-blog.csdnimg.cn/17d265eecce54a028086e8e7f04a1bc6.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16)

```java
package com.xujinshan.rabbitmq10;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;


/**
 * @Author: xujinshan361@163.com
 * 死信队列  -- 队列达到最大长度
 * 消费者2
 */
public class Consumer02 {

    // 死信对列名称
    public static final String DEAD_QUEUE = "dead_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();

        System.out.println("等待接收消息。。。。");
        DeliverCallback deliverCallback = (consumerTag,message)->{
            System.out.println("Consumer2接收死信队列的消息是："+ new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(DEAD_QUEUE,true,deliverCallback,consumerTag->{});
    }

}
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/6df1a17f98cd4f649c6263dacdc27a7a.png#pic_center)
- 启动C2 后死信队列消息被消费

### 6.3.4 消息被拒
- 消息生产者（代码同上一生产者）

```java
package com.xujinshan.rabbitmq11;

import com.rabbitmq.client.Channel;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.nio.charset.StandardCharsets;

/**
 * @Author: xujinshan361@163.com
 * 死信队列 生产者
 * 消息被拒
 */
public class Producer {
    // 普通交换机名称
    public static final String NORMAL_EXCHANGE = "normal_exchange";
    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        for (int i = 0; i < 10; i++) {
            String message = "info" +i;
            channel.basicPublish(NORMAL_EXCHANGE,"zhangsan",null,message.getBytes(StandardCharsets.UTF_8));
            System.out.println("生产者发送消息："+message);
        }

    }
}
```
- C1 消费者代码（**启动之后关闭该消费者，模拟接收不到信息**）

```java
package com.xujinshan.rabbitmq11;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xujinshan361@163.com
 * 死信队列 -- 消息被拒
 * 消费者1
 */
public class Consumer01 {
    // 普通交换机名称
    public static final String NORMAL_EXCHANGE = "normal_exchange";
    // 死信交换机名称
    public static final String DEAD_EXCHANGE = "dead_exchange";
    // 普通队列名称
    public static final String NORMAL_QUEUE = "normal_queue";
    // 死信对列名称
    public static final String DEAD_QUEUE = "dead_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        // 声明死信和普通交换机 类型为direct
        channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(DEAD_EXCHANGE, BuiltinExchangeType.DIRECT);

        // 声明死信和普通队列
        Map<String,Object> arguments = new HashMap<>();
        // 过期时间--一般由发送者设定
//        arguments.put("x-message-ttl",10*1000);
        // 正常队列设置死信交换机
        arguments.put("x-dead-letter-exchange",DEAD_EXCHANGE);
        // 设置死信routingKey
        arguments.put("x-dead-letter-routing-key","lisi");
        // 设置正常队列长度的限制
//        arguments.put("x-max-length",6);
        channel.queueDeclare(NORMAL_QUEUE,false,false,false,arguments);  // 普通队列
        channel.queueDeclare(DEAD_QUEUE,false,false,false,null);    // 死信队列

        // 绑定普通的交换机与队列
        // 绑定死信的交换机与队列
        channel.queueBind(NORMAL_QUEUE,NORMAL_EXCHANGE,"zhangsan");
        channel.queueBind(DEAD_QUEUE,DEAD_EXCHANGE,"lisi");

        System.out.println("等待接收消息。。。。");
        DeliverCallback deliverCallback = (consumerTag,message)->{
            String msg  = new String(message.getBody(),"UTF-8");
            if(msg.equals("info5")){
                System.out.println("Consumer1接收的消息是："+ msg+":此消息被C1拒收");
                channel.basicReject(message.getEnvelope().getDeliveryTag(),false);  // 拒绝接受，不放回队列，成为死信
            }else{
                System.out.println("Consumer1接收的消息是："+ msg);
                channel.basicAck(message.getEnvelope().getDeliveryTag(),false);
            }
        };
        // 开启手动应答
        channel.basicConsume(NORMAL_QUEUE, false,deliverCallback,consumerTag->{});
    }

}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/a8e4d37e34fb4e61bc11125dd08298ff.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16)
- 模拟拒绝info5消息，同时拒绝的消息不需要放回队列，成为死信消息，需要开启手动应答

消费者C2 代码不变

```java
package com.xujinshan.rabbitmq11;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;


/**
 * @Author: xujinshan361@163.com
 * 死信队列  -- 消息被拒
 * 消费者2
 */
public class Consumer02 {

    // 死信对列名称
    public static final String DEAD_QUEUE = "dead_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();

        System.out.println("等待接收消息。。。。");
        DeliverCallback deliverCallback = (consumerTag,message)->{
            System.out.println("Consumer2接收死信队列的消息是："+ new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(DEAD_QUEUE,true,deliverCallback,consumerTag->{});
    }

}
```

- ** 结果**
- 首先启动C1然后将其关闭模拟接收不到信息，然后启动生产者发送消息，接着启动消费者C1和C2 去消费
- 死信队列消息
![在这里插入图片描述](https://img-blog.csdnimg.cn/e18524b981a84e9bbc7e546f3333cc5d.png#pic_center)
- 生产者![在这里插入图片描述](https://img-blog.csdnimg.cn/5e3ef08f2ce648cb9dd7e1858248bea4.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
- 消费者C1
![在这里插入图片描述](https://img-blog.csdnimg.cn/64ca9b1d19d64d7aa2befa1df1f7e24c.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
- 消费者C2
![在这里插入图片描述](https://img-blog.csdnimg.cn/2f68fbffc80e485fa8bb84b31bd69268.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)


# 7. 延迟队列
## 7.1 延迟队列概念
延迟队列，队列内部有序，最重要的特性体现在它的延时属性上，延迟队列中的元素是有希望在指定时间到了以后或之前取出和处理的，简单来说，延迟队列就是用来存放需要在指定时间被吹了的元素的队列。

## 7.2 延迟队列使用场景
- 订单在十分钟之内未支付自动取消
- 新创建的店铺，如果在十天内都没有上传过商品自动发送消息提醒
- 新用户注册成功后，如果三天内没有登录则进行短信提醒
- 用户发起退款，如果三天内没有得到处理则通知相关运营人员
- 预定会议后，需要在预定的时间点前十分钟通知各个参会人员参加会议


这些场景都有一个特点，需要在某个事件发生之后或者之前的指定时间点完成某一项任务，如：发生订单生成事件，在十分钟之后检查该订单支付状态，然后将未支付的订单进行关闭；看起来似乎使用定时任务，一直轮询数据，每秒查一次，取出需要被处理的数据，然后处理不就完事了吗？如果数据量比较少，确实可以这样做，比如：对于“如果账单一周内未支付则进行自动结算”这样的需求，如果对于时间不是严格限制，而是宽松意义上的一周，那么每天晚上跑个定时任务检查一下所有未支付的账单，确实也是一个可行的方案。但对于数据量比较大，并且时效性较强的场景，如：“订单十分钟内未支付则关闭“，短期内未支付的订单数据可能会有很多，活动期间甚至会达到百万甚至千万
级别，对这么庞大的数据量仍旧使用轮询的方式显然是不可取的，很可能在一秒内无法完成所有订单的检查，同时会给数据库带来很大压力，无法满足业务要求而且性能低下。

![在这里插入图片描述](https://img-blog.csdnimg.cn/1f5b16ecfe79448d810a1780386f3c90.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
## 7.3 整合SpringBoot 
- 创建SpringBoot项目
- 添加依赖

```xml
 <!--添加依赖-->
    <!--RabbitMQ 依赖-->
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-amqp</artifactId>
      </dependency>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-web</artifactId>
      </dependency>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-test</artifactId>
          <scope>test</scope>
      </dependency>
      <dependency>
          <groupId>com.alibaba</groupId>
          <artifactId>fastjson</artifactId>
          <version>1.2.47</version>
      </dependency>
      <dependency>
          <groupId>org.projectlombok</groupId>
          <artifactId>lombok</artifactId>
      </dependency>
          <!--swagger-->
      <dependency>
          <groupId>io.springfox</groupId>
          <artifactId>springfox-swagger2</artifactId>
          <version>2.9.2</version>
      </dependency>
      <dependency>
          <groupId>io.springfox</groupId>
          <artifactId>springfox-swagger-ui</artifactId>
          <version>2.9.2</version>
      </dependency>
          <!--RabbitMQ 测试依赖-->
      <dependency>
          <groupId>org.springframework.amqp</groupId>
          <artifactId>spring-rabbit-test</artifactId>
          <scope>test</scope>
      </dependency>
```
- 修改配置文件

```
spring.rabbitmq.host=192.168.1.102
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=admin
```
- 添加Swagger配置类，方便测试使用

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Author: xujinshan361@163.com
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket webApiConfig(){
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("webApi")
                .apiInfo(webApiInfo())
                .select().build();
    }
    private ApiInfo webApiInfo(){
        return new ApiInfoBuilder()
                .title("rabbitmq 接口文档")
                .description("本文档描述了 rabbitmq 微服务接口定义")
                .version("1.0")
                .contact(new Contact("xujinshan","http://xujinshan.com",
                        "11111111@qq.com")).build();
    }
}
```

## 7.4 对列TTL
### 7.4.1 代码架构图
创建两个队列QA和QB，两个队列TTL分别设置为10S和40S，然后再创建一个交换机X和一个死信交换机Y，它们的类型都是direct，创建一个死信队列QD，绑定关系如下图：
![在这里插入图片描述](https://img-blog.csdnimg.cn/37bd2c9468814799ad9cde26d04a2295.png#pic_center)
### 7.4.2 配置文件类代码（用于构建上图关系）

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * @Author: xujinshan361@163.com
 * TTL 队列，配置文件类
 */
@Configuration
public class TtlQueueConfig {
    // 普通交换机名称
    public static final String X_EXCHANGE = "X";
    // 死信交换机名称
    public static final String Y_DEAD_LETTER_EXCHANGE = "Y";
    // 普通队列名称
    public static final String QUEUE_A = "QA";
    public static final String QUEUE_B = "QB";
    // 死信队列名称
    private static final String DEAD_LETTER_QUEUE = "QD";

    // 声明X交换机 别名
    @Bean("xExchange")
    public DirectExchange xExchange() {
        return new DirectExchange(X_EXCHANGE);
    }

    // 声明y交换机 别名
    @Bean("yExchange")
    public DirectExchange yExchange() {
        return new DirectExchange(Y_DEAD_LETTER_EXCHANGE);
    }

    // 声明普通队列 TTL 10s
    @Bean("queueA")
    public Queue queueA() {
        Map<String, Object> arguments = new HashMap<>(3);
        // 设置死信交换机
        arguments.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
        // 设置死信routingKey
        arguments.put("x-dead-letter-routing-key", "YD");

        // 设置TTL 单位ms
        arguments.put("x-message-ttl", 10000);
        return QueueBuilder.durable(QUEUE_A).withArguments(arguments).build();
        // 可通过这种方式构建
//        return QueueBuilder.durable(QUEUE_A)
//                .deadLetterExchange(Y_DEAD_LETTER_EXCHANGE)
//                .deadLetterRoutingKey("YD")
//                .ttl(10000).build();
    }

    // 声明普通队列 TTL 10s
    @Bean("queueB")
    public Queue queueB() {
        Map<String, Object> arguments = new HashMap<>(3);
        // 设置死信交换机
        arguments.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
        // 设置死信routingKey
        arguments.put("x-dead-letter-routing-key", "YD");

        // 设置TTL 单位ms
        arguments.put("x-message-ttl", 40000);
        return QueueBuilder.durable(QUEUE_B).withArguments(arguments).build();
    }

    // 死信队列
    @Bean("queueD")
    public Queue queueD() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    // 绑定
    @Bean
    public Binding queueABindingX(@Qualifier("queueA") Queue queueA, @Qualifier("xExchange") DirectExchange xExchange) {
        return BindingBuilder.bind(queueA).to(xExchange).with("XA");
    }

    // 绑定
    @Bean
    public Binding queueBBindingX(@Qualifier("queueB") Queue queueB, @Qualifier("xExchange") DirectExchange xExchange) {
        return BindingBuilder.bind(queueB).to(xExchange).with("XB");
    }

    // 绑定
    @Bean
    public Binding queueDBindingX(@Qualifier("queueD") Queue queueD, @Qualifier("yExchange") DirectExchange yExchange) {
        return BindingBuilder.bind(queueD).to(yExchange).with("YD");
    }
}
```

### 7.4.3 生产者消息代码

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Author: xujinshan361@163.com
 * TTL 消息生产者
 * 发送延迟消息
 */
@Slf4j
@RestController
@RequestMapping("/ttl")
public class SendMsgController {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    // 开始发送消息
    @GetMapping("/senMsg/{message}")
    public void sendMessage(@PathVariable String message){
        log.info("当前时间：{},发送一条消息给两个TTL队列：{}",new Date().toString(),message);
        rabbitTemplate.convertAndSend("X","XA","消息来自TTL10s的队列:"+message);
        rabbitTemplate.convertAndSend("X","XB","消息来自TTL40s的队列:"+message);
    }
}
```

### 7.4.4 消费者代码

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


import java.util.Date;

/**
 * @Author: xujinshan361@163.com
 * 队列TLL 消费者
 */
@Slf4j
@Component
public class DeadLetterConsumer {
    // 接收消息
    @RabbitListener(queues = "QD")
    public void resiveD(Message message, Channel channel) throws  Exception{
        String msg = new String(message.getBody());
        log.info("当前时间：{}，收到死信队列的消息：{}",new Date().toString(),msg);
    }
}
```

发起一个请求：http://localhost:8080/ttl/sendMsg/嘻嘻嘻
![在这里插入图片描述](https://img-blog.csdnimg.cn/0a4ba8f33831459382089a0eb1ad95f1.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_18,color_FFFFFF,t_70,g_se,x_16#pic_center)![在这里插入图片描述](https://img-blog.csdnimg.cn/262936957c09442b854270e38e66e131.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)



- 第一条消息在10s后变成死信消息，然后被消费者消费掉，第二条在40s之后变成死信消息，然后被消费掉，这样一个延时队列就完成了。
- 不过如果这样使用的话，岂不是**每增加一个新的时间需求，就要新增加一个队列**，这里有10s和40s两个时间选项，如果需要一个小时候处理，那么就需要增加TTL为1个小时的队列，如果是预定会议室然后提前通知这样的场景，岂不是要增加无数个队列才能满足需求？

## 7.5 延时队列优化
### 7.5.1 代码架构图
- 这里新增一个队列QC，绑定关系如下，该队列不设置TTL时间
![在这里插入图片描述](https://img-blog.csdnimg.cn/960e8932569e40568bdcc752d0eb4255.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
### 7.5.2 配置类文件

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * @Author: xujinshan361@163.com
 * 死信队列优化
 * TTL 队列，配置文件类
 */
@Configuration
public class TtlQueueConfig02 {
    // 普通交换机名称
    public static final String X_EXCHANGE = "X";
    // 死信交换机名称
    public static final String Y_DEAD_LETTER_EXCHANGE = "Y";
    // 普通队列名称
    public static final String QUEUE_A = "QA";
    public static final String QUEUE_B = "QB";
    // 新增普通队列QC
    public static final String QUEUE_C="qc";
    // 死信队列名称
    private static final String DEAD_LETTER_QUEUE = "QD";

    // 声明X交换机 别名
    @Bean("xExchange")
    public DirectExchange xExchange() {
        return new DirectExchange(X_EXCHANGE);
    }

    // 声明y交换机 别名
    @Bean("yExchange")
    public DirectExchange yExchange() {
        return new DirectExchange(Y_DEAD_LETTER_EXCHANGE);
    }

    // 声明普通队列 TTL 10s
    @Bean("queueA")
    public Queue queueA() {
        Map<String, Object> arguments = new HashMap<>(3);
        // 设置死信交换机
        arguments.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
        // 设置死信routingKey
        arguments.put("x-dead-letter-routing-key", "YD");

        // 设置TTL 单位ms
        arguments.put("x-message-ttl", 10000);
        return QueueBuilder.durable(QUEUE_A).withArguments(arguments).build();
        // 可通过这种方式构建
//        return QueueBuilder.durable(QUEUE_A)
//                .deadLetterExchange(Y_DEAD_LETTER_EXCHANGE)
//                .deadLetterRoutingKey("YD")
//                .ttl(10000).build();
    }

    // 声明普通队列 TTL 10s
    @Bean("queueB")
    public Queue queueB() {
        Map<String, Object> arguments = new HashMap<>(3);
        // 设置死信交换机
        arguments.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
        // 设置死信routingKey
        arguments.put("x-dead-letter-routing-key", "YD");

        // 设置TTL 单位ms
        arguments.put("x-message-ttl", 40000);
        return QueueBuilder.durable(QUEUE_B).withArguments(arguments).build();
    }

    // 死信队列
    @Bean("queueD")
    public Queue queueD() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    // 绑定
    @Bean
    public Binding queueABindingX(@Qualifier("queueA") Queue queueA, @Qualifier("xExchange") DirectExchange xExchange) {
        return BindingBuilder.bind(queueA).to(xExchange).with("XA");
    }

    // 绑定
    @Bean
    public Binding queueBBindingX(@Qualifier("queueB") Queue queueB, @Qualifier("xExchange") DirectExchange xExchange) {
        return BindingBuilder.bind(queueB).to(xExchange).with("XB");
    }

    // 绑定
    @Bean
    public Binding queueDBindingX(@Qualifier("queueD") Queue queueD, @Qualifier("yExchange") DirectExchange yExchange) {
        return BindingBuilder.bind(queueD).to(yExchange).with("YD");
    }


    // 新增功能-- 不设置ttl
    @Bean("queueC")
    public Queue queueC(){
        return QueueBuilder.durable(QUEUE_C)
                // 设置死信交换机
                .deadLetterExchange(Y_DEAD_LETTER_EXCHANGE)
                // 设置死信routingKey
                .deadLetterRoutingKey("YD").build();
     }
     @Bean
    public Binding queueCBindingX(@Qualifier ("queueC") Queue queueC, @Qualifier ("xExchange") DirectExchange xExchange){
        return BindingBuilder.bind(queueC).to(xExchange).with("XC");
     }
}
```
### 7.5.3 消息生产者代码

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Author: xujinshan361@163.com
 * TTL 消息生产者
 * 发送延迟消息
 */
@Slf4j
@RestController
@RequestMapping("/ttl")
public class SendMsgController {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    // 开始发送消息
    @GetMapping("/sendMsg/{message}")
    public void sendMessage(@PathVariable String message){
        log.info("当前时间：{},发送一条消息给两个TTL队列：{}",new Date().toString(),message);
        rabbitTemplate.convertAndSend("X","XA","消息来自TTL10s的队列:"+message);
        rabbitTemplate.convertAndSend("X","XB","消息来自TTL40s的队列:"+message);
    }

    // 开始发送消息，消息TTL
    @GetMapping("/sendExpriationMsg/{message}/{ttlTime}")
    public void sendMessage(@PathVariable String message, @PathVariable String ttlTime){
        log.info("当前时间：{},发送一条市场{}毫秒的TTL信息给队列QC:{}",new Date().toString(),ttlTime,message);
        rabbitTemplate.convertAndSend("X","XC",message,msg -> {
            // 发送消息的延迟时长
            msg.getMessageProperties().setExpiration(ttlTime);
            return msg;
        });
    }
}
```

发起请求：
localhost:8080/ttl/sendExpriationMsg/你好1/20000
localhost:8080/ttl/sendExpriationMsg/你好2/2000
![在这里插入图片描述](https://img-blog.csdnimg.cn/fd4579e507f0460e8f27383aa52eb0b2.png#pic_center)
- 看起来似乎没什么问题，但如果使用在消息属性上设置TTL的方式，消息可能并不会按时“死亡”，因为：**RabbitMQ只会检查第一个消息是否过期，如果过期则丢到死信队列，如果第一个消息的延时时长很长，而第二个消息的延时时长很短，第二个消息并不会优先得到执行**

### 7.6 RabbitMQ中的TTL
- TTL是RabbitMQ中的消息或队列的属性，表明一条消息或该队列中的所有消息的最大存活时间。
- 单位毫秒。换句话说，如果一条消息设置了TTL属性或进入TTL属性的队列，那么这条消息如果在TTL设置的时间内没有被消费，则会称为“死信”。如果同时配置了队列的TTL和消息TTL，那么较小的那个值将会被使用，有两种设置TTL。

### 7.6.1 消息设置TTL
![在这里插入图片描述](https://img-blog.csdnimg.cn/e613c38fa50e48aa89c8f6e95036e563.png#pic_center)
### 7.6.2 队列设置TTL
![在这里插入图片描述](https://img-blog.csdnimg.cn/f9542cba87a249b2826be51b454c0b5c.png)
### 7.6.3 区别
- 如果设置了队列TTL属性，一旦消息过期，就会被队列丢弃（如果配置了死信队列被丢弃到死信队列中），而消息TTL，消息即使过期，也不一定马上丢弃，因为**消息是否过期是在即将投递到消费者之前判定的**，如果当队列有严重的消息积压情况，则已过期的消息也许还能存活较长时间；
- 另外，如果不设置TTL，表示消息永远不会过期，如果将TTL设置为0，则表示除非此时可以直接投递该消息到消费者，否则该消息将会被丢弃。

## 7.7 RabbitMQ插件实现延迟队列
之前提及的问题，如果不能实现在消息粒度的TTL，并使其在设置的TTL时间及时死亡，就无法设计成一个通用的延时队列。下面主要为了解决该问题。

### 7.7.1 安装延时队列插件
- 在官网下载rabbitmq_delayed_message_exchange 插件。[下载地址](https://www.rabbitmq.com/community-plugins.html)
- 放入RabbitMQ的安装目录下的plgins目录，执行下列命令安装

```
/usr/lib/rabbitmq/lib/rabbitmq_server-3.8.8/plugins
rabbitmq-plugins enable rabbitmq_delayed_message_exchange

# 重启命令
rabbitmqctl stop
rabbitmq-server restart
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/79d095bdf5584dc28be0e91fd659db61.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16)
- 重启之后（新增x-delayed-message）
![在这里插入图片描述](https://img-blog.csdnimg.cn/bc2c21cab5d84651a79b234c0befde63.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16)
- **基于插件的延迟（延迟时间在交换机，到时间后传递到队列）**
![在这里插入图片描述](https://img-blog.csdnimg.cn/2259ef7df606423dadd9a56373b20f80.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16)

### 7.7.2 代码架构图
在这里新增了一个队列delayed.queue,一个自定义交换机delayed.exchange,绑定关系如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/da37929e42da4381b8b45da61323a7af.png#pic_center)
### 7.7.3 配置文件类代码
在自定义的交换机中，这是一种新的交换机类型，该类型支持延迟投递机制，消息传递后并不会立即投递到目标队列中，而是存储在msesia（一个分布式数据系统）表中，当达到投递时间时，才投递到目标队列中。

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xujinshan361@163.com
 */
@Configuration
public class DelayedQueueConfig {

    // 交换机
    public static final String DELAYED_QUEUE_NAME = "delayed.queue";
    // 队列
    public static final String DELAYED_EXCHANGE_NAME = "delayed.exchange";
    // routingKey
    public static final String DELAYED_ROUTING_KEY = "delayed.routingKey";

    // 声明交换机-自定义交换机(基于插件的)
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-delayed-type", "direct");
        return new CustomExchange(DELAYED_EXCHANGE_NAME, "x-delayed-message",
                false, false, arguments);
    }

    // 声明队列
    @Bean
    public Queue delayedQueue() {
        return new Queue(DELAYED_QUEUE_NAME);
    }

    // 绑定
    @Bean
    public Binding delayedQueueBindingDelayedExchange(@Qualifier("delayedQueue") Queue delayedQueue,
                                                      @Qualifier("delayedExchange") CustomExchange delayedExchange) {
        return BindingBuilder.bind(delayedQueue).to(delayedExchange).with(DELAYED_ROUTING_KEY).noargs();
    }
}
```
### 7.7.4 消息生产者代码

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.controller;

import com.xujinshan.rabbitmq.springbootrabbitmq.config.DelayedQueueConfig;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Author: xujinshan361@163.com
 * TTL 消息生产者
 * 发送延迟消息
 */
@Slf4j
@RestController
@RequestMapping("/ttl")
public class SendMsgController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 开始发送消息，基于插件的
    @GetMapping("/sendDelayedMsg/{message}/{delayedTime}")
    public void sendMessage(@PathVariable String message, @PathVariable Integer delayedTime) {
        log.info("当前时间：{},发送一条时长{}毫秒的信息给队列delayed.queue:{}", new Date().toString(), delayedTime, message);
        rabbitTemplate.convertAndSend(DelayedQueueConfig.DELAYED_EXCHANGE_NAME, DelayedQueueConfig.DELAYED_ROUTING_KEY, message, msg -> {
            // 发送消息的延迟时长
            msg.getMessageProperties().setDelay(delayedTime);
            return msg;
        });
    }
}
```
### 7.7.5 消息消费代码

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.consumer;

import com.xujinshan.rabbitmq.springbootrabbitmq.config.DelayedQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author: xujinshan361@163.com
 * 消费者-- 基于插件的延迟
 */
@Slf4j
@Component
public class DelayedQueueConsumer {
    // 监听消息
    @RabbitListener(queues = DelayedQueueConfig.DELAYED_QUEUE_NAME)
    public void receiveDelayedQueue(Message message){
        String msg = new String(message.getBody());
        System.out.println(message.getBody());
        log.info("当前时间：{}，收到延迟队列消息：{}",new Date().toString(),msg);
    }
}
```

发起请求：
http://localhost:8080/ttl/sendDelayedMsg/come on baby1/20000
http://localhost:8080/ttl/sendDelayedMsg/come on baby2/2000

![在这里插入图片描述](https://img-blog.csdnimg.cn/fd374e6864354fb3b0e06c59b9def5c7.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
- 第二个消息先被消费掉了，符合预期

## 7.8 总结
延迟队列在需要延迟的处理的场景下非常有用，使用RabbitMQ来实现延时队列可以很好的利用RabbitMQ的特性，如：消息可靠发发送，消息可靠投递，死信队列来保障消息至少被消费一次以及未被正确处理的消息不会被丢弃。另外，通过RabbitMQ集群的特性，可以很好的解决单点故障问题，不会因为单个节点挂掉导致延迟对垒不可以或消息丢失。

当然，延迟队列还有很多选择，比如利用java的DelayQueue，利用Redis 的zset，利用Quartz或者kafka 的时间轮，这些方法各有特点，看需要使用的场景。


# 8. 发布确认高级
在生产环境中由于一些不明原因，导致RabbitMQ 重启，在RabbitMQ重启期间生产者消息投递失败，导致消息丢失，需要手动处理和回复。如何进行RabbitMQ消息的可靠投递？特别是极端的情况，RabbitMQ集群不可用的时候，无法投递的消息该如何处理？

## 8.1 发布确认SpringBoot版本
### 8.1.1 确认机制方案

![在这里插入图片描述](https://img-blog.csdnimg.cn/06fc7f35a78d40d3aca9e960965a5593.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
### 8.1.2 代码架构图
![在这里插入图片描述](https://img-blog.csdnimg.cn/c82fd382c11841d680d184612e8bc276.png#pic_center)
### 8.1.3 配置文件
在配置文件中添加

```
spring.rabbitmq.publisher-confirm-type=correlated
```
- NONE :禁用发布确认模式，是默认值
- CORRELATED：发布消息成功到交换机后会触发回调方法
- SIMPLE：
	- 一种效果和CORRELATED一样会触发回调方法
	- 在发布成功后使用rabbitTemplate 调用waitForConfirm或 waifForConfirmsOrDie 方法等待 broker 节点返回发送结果，根据返回结果来判定下一步的逻辑，要注意的点是
	waitForConfirmsOrDie 方法如果返回 false 则会关闭 channel，则接下来无法发送消息到 broker。

### 8.1.4 添加配置类

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: xujinshan361@163.com
 * 配置类，发布确认（高级）
 */
@Configuration
public class ConfirmConfig {
    // 交换机
    public static final String CONFIRM_EXCHANGE_NAME="confirm_exchange";
    // 队列
    public static final String CONFIRM_QUEUE_NAME="confirm_queue";
    // routingKey
    public static final String CONFIRM_ROUTING_KEY="key1";

    // 声明交换机
    @Bean
    public DirectExchange confirmExchange(){
        return new DirectExchange(CONFIRM_EXCHANGE_NAME);
    }

    // 声明队列
    @Bean
    public Queue confirmQueue(){
        return QueueBuilder.durable(CONFIRM_QUEUE_NAME).build();
    }

    // 绑定
    @Bean
    public Binding queueBindingExchange(@Qualifier("confirmQueue") Queue confirmQueue,
                                        @Qualifier("confirmExchange") DirectExchange confirmExchange){
        return BindingBuilder.bind(confirmQueue).to(confirmExchange).with(CONFIRM_ROUTING_KEY);
    }
}
```

### 8.1.5 消息生产者

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.controller;

import com.xujinshan.rabbitmq.springbootrabbitmq.config.ConfirmConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: xujinshan361@163.com
 *  测试确认
 */
@Slf4j
@Controller
@RequestMapping("/confirm")
public class ProducerController {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    // 发消息
    @GetMapping("/sendMessage/{message}")
    public void sendMessage(@PathVariable String message){
        //指定消息 id 为 1
        CorrelationData correlationData1=new CorrelationData("1");
        String routingKey="key1";
        rabbitTemplate.convertAndSend(ConfirmConfig.CONFIRM_EXCHANGE_NAME,routingKey,
                message+routingKey,correlationData1);
        log.info("发送消息内容:{}",message+routingKey);
        CorrelationData correlationData2=new CorrelationData("2");
        routingKey="key2";
        rabbitTemplate.convertAndSend(ConfirmConfig.CONFIRM_EXCHANGE_NAME,
                routingKey,message+routingKey,correlationData2);
        log.info("发送消息内容:{}",message+routingKey);
    }
}


```
### 8.1.6 回调接口

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author: xujinshan361@163.com
 * 回调接口
 */
@Slf4j
@Component
public class MyCallBack implements RabbitTemplate.ConfirmCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    // 注入
    @PostConstruct  // 在其他注解执行后执行
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
    }
    /**
     * 交换机确认回调方法
     * 发消息 交换机接收到了 回调
     * 发消息 交换机接收失败 回调
     * @param correlationData  保存回调信息的ID及相关信息
     * @param b     交换机收到消息为true
     * @param s     失败原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        String id = correlationData!=null?correlationData.getId():"";
        if(b){
            log.info("交换机已经收到ID为：{}的消息",id);
        }else {
            log.info("交换机还未收到ID为：{}的消息，由于原因：{}",id,s);
        }
    }
}
```

### 8.1.7 消息消费者

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.consumer;

import com.xujinshan.rabbitmq.springbootrabbitmq.config.ConfirmConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Author: xujinshan361@163.com
 * 接收消息
 */
@Slf4j
@Component
public class Consumer {
    @RabbitListener(queues = ConfirmConfig.CONFIRM_QUEUE_NAME)
    public void receiveConfirmMessage(Message message){
        String msg = new String(message.getBody());
        log.info("接收到的队列confirm.queue消息：{}",msg);
    }
}
```

### 8.1.8 结果分析
![在这里插入图片描述](https://img-blog.csdnimg.cn/ad8ad473c27344ad97e33c12bf44d85b.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
- 可以看到，发送了两条消息，第一条消息的 RoutingKey 为 "key1"，第二条消息的 RoutingKey 为"key2"，两条消息都成功被交换机接收，也收到了交换机的确认回调，但消费者只收到了一条消息，因为第二条消息的 RoutingKey 与队列的 BindingKey 不一致，也没有其它队列能接收这个消息，所有第二条消息被直接丢弃了。


## 8.2 回退消息
### 8.2.1 Mandatory 参数
**在开启了生产者确认机制的情况下，交换机接收到消息后，会直接给消息生产者发送确认消息，如果发现该消息不可路由，那么该消息会被直接丢弃，此时生产者不知道消息被丢弃这个事件。**那么如何让无法被路由的消息通知生产者。可通过设置mandatory参数可以在当消息传递过程中不可达目的地时将消息返回给生产者。

- 添加配置文件

```
# 开启消息回退
spring.rabbitmq.publisher-returns=true
```

### 8.2.2 消息生产者代码

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.controller;

import com.xujinshan.rabbitmq.springbootrabbitmq.config.ConfirmConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: xujinshan361@163.com
 *  测试确认
 */
@Slf4j
@Controller
@RequestMapping("/confirm")
public class ProducerController {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    // 发消息
    @GetMapping("/sendMessage/{message}")
    public void sendMessage(@PathVariable String message){
        //指定消息 id 为 1
        CorrelationData correlationData1=new CorrelationData("1");
        String routingKey="key1";
        rabbitTemplate.convertAndSend(ConfirmConfig.CONFIRM_EXCHANGE_NAME,routingKey,
                message+routingKey,correlationData1);
        log.info("发送消息内容:{}",message+routingKey);
        CorrelationData correlationData2=new CorrelationData("2");
        routingKey="key2";
        rabbitTemplate.convertAndSend(ConfirmConfig.CONFIRM_EXCHANGE_NAME,
                routingKey,message+routingKey,correlationData2);
        log.info("发送消息内容:{}",message+routingKey);
    }
}
```

### 8.2.3 回调接口

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author: xujinshan361@163.com
 * 回调接口
 */
@Slf4j
@Component
public class MyCallBack implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {


    @Autowired
    private RabbitTemplate rabbitTemplate;
    // 注入
    @PostConstruct  // 在其他注解执行后执行
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }
    /**
     * 交换机确认回调方法
     * 发消息 交换机接收到了 回调
     * 发消息 交换机接收失败 回调
     * @param correlationData  保存回调信息的ID及相关信息
     * @param b     交换机收到消息为true
     * @param s     失败原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        String id = correlationData!=null?correlationData.getId():"";
        if(b){
            log.info("交换机已经收到ID为：{}的消息",id);
        }else {
            log.info("交换机还未收到ID为：{}的消息，由于原因：{}",id,s);
        }
    }

    /**
     * 高版本已经被弃用
     * 可以在消息传递过程中不可达目的地时将消息返回给生产者
     * @param
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.info("消息：{}，被交换机:{}退回,退回原因：{}，routingKey:{}",
                new String(message.getBody()),
                exchange,replyText,routingKey);
        RabbitTemplate.ReturnsCallback.super.returnedMessage(message, replyCode, replyText, exchange, routingKey);
    }

    /**
     * 高版本returnMessage已经被封装了
     * @param returnedMessage
     */
    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        log.info("消息：{}，被交换机:{}退回,退回原因：{}，routingKey:{}",new String(returnedMessage.getMessage().getBody()),
                returnedMessage.getExchange(),returnedMessage.getReplyText(),returnedMessage.getRoutingKey());
    }

}
```

### 8.2.4 结果分析

![在这里插入图片描述](https://img-blog.csdnimg.cn/fb2c5c6703814140ae64fa7ac5a7d50e.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16)
## 8.3 备份交换机
- 有了 mandatory 参数和回退消息，获得了对无法投递消息的感知能力，有机会在生产者的消息无法被投递时发现并处理。
- 但有时候，并不知道该如何处理这些无法路由的消息，最多打个日志，然后触发报警，再来手动处理。而通过日志来处理这些无法路由的消息是很不优雅的做法，特别是当生产者所在的服务有多台机器的时候，手动复制日志会更加麻烦而且容易出错。
- 而且设置 mandatory 参数会增加生产者的复杂性，需要添加处理这些被退回的消息的逻辑。如果既不想丢失消息，又不想增加生产者的复杂性，该怎么做呢？
- 前面在设置死信队列的中，提到，可以为队列设置死信交换机来存储那些处理失败的消息，可是这些不可路由消息根本没有机会进入到队列，因此无法使用死信队列来保存消息。
- 在 RabbitMQ 中，有一种备份交换机的机制存在，可以很好的应对这个问题。什么是备份交换机呢？
- 备份交换机可以理解为 RabbitMQ 中交换机的“备胎”，当为某一个交换机声明一个对应的备份交换机时，就是为它创建一个备胎，当交换机接收到一条不可路由消息时，将会把这条消息转发到备份交换机中，由备份交换机来进行转发和处理，**通常备份交换机的类型为 Fanout** ，这样就能把所有消息都投递到与其绑定的队列中，然后我们在备份交换机下绑定一个队列，这样所有那些原交换机无法被路由的消息，就会都进入这个队列了。当然，还可以建立一个报警队列，用独立的消费者来进行监测和报警。

### 8.3.1 代码架构图
![在这里插入图片描述](https://img-blog.csdnimg.cn/9c186ce39e744d6ea90bbc8d484a4690.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
### 8.3.2 修改配置类

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: xujinshan361@163.com
 * 配置类，发布确认（高级）
 */
@Configuration
public class ConfirmConfig {
    // 交换机
    public static final String CONFIRM_EXCHANGE_NAME = "confirm_exchange";
    // 队列
    public static final String CONFIRM_QUEUE_NAME = "confirm_queue";
    // routingKey
    public static final String CONFIRM_ROUTING_KEY = "key1";

    // 备份交换机
    public static final String BACKUP_EXCHANGE_NAME = "backup_exchange";
    // 备份队列
    public static final String BACKUP_QUEUE_NAME = "backup_queue";
    // 报警队列
    public static final String WARNING_QUEUE_NAME = "warning_queue";

    // 声明交换机
    @Bean
    public DirectExchange confirmExchange() {
//        return new DirectExchange(CONFIRM_EXCHANGE_NAME);
        // 增加备份交换机连接
        return ExchangeBuilder.directExchange(CONFIRM_EXCHANGE_NAME)
                .withArgument("alternate-exchange",BACKUP_EXCHANGE_NAME).build();
    }

    // 声明备份交换机-- fanout交换机
    @Bean
    public FanoutExchange backupExchange() {
        return new FanoutExchange(BACKUP_EXCHANGE_NAME);
    }

    // 声明队列
    @Bean
    public Queue confirmQueue() {
        return QueueBuilder.durable(CONFIRM_QUEUE_NAME).build();
    }

    // 声明备份队列
    @Bean
    public Queue backupQueue() {
        return QueueBuilder.durable(BACKUP_QUEUE_NAME).build();
    }

    // 声明报警队列
    @Bean
    public Queue warningQueue() {
        return QueueBuilder.durable(WARNING_QUEUE_NAME).build();
    }

    // 绑定
    @Bean
    public Binding queueBindingExchange(@Qualifier("confirmQueue") Queue confirmQueue,
                                        @Qualifier("confirmExchange") DirectExchange confirmExchange) {
        return BindingBuilder.bind(confirmQueue).to(confirmExchange).with(CONFIRM_ROUTING_KEY);
    }

    // 绑定
    @Bean
    public Binding backupQueueBindingBackupExchange(@Qualifier("backupQueue") Queue backupQueue,
                                                    @Qualifier("backupExchange") FanoutExchange backupExchange) {
        return BindingBuilder.bind(backupQueue).to(backupExchange);
    }

    // 绑定
    @Bean
    public Binding warningQueueBindingBackupExchange(@Qualifier("warningQueue") Queue warningQueue,
                                                     @Qualifier("backupExchange") FanoutExchange backupExchange) {
        return BindingBuilder.bind(warningQueue).to(backupExchange);
    }
}
```
### 8.3.3 报警消费者

```java
package com.xujinshan.rabbitmq.springbootrabbitmq.consumer;

import com.xujinshan.rabbitmq.springbootrabbitmq.config.ConfirmConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Author: xujinshan361@163.com
 * 报警消费者
 */
@Slf4j
@Component
public class WarningConsumer {
    // 接收报警信息
    @RabbitListener(queues = ConfirmConfig.WARNING_QUEUE_NAME)
    public void receiveWarningMsg(Message message){
        String msg= new String(message.getBody());
        log.error("报警发现不可路由消息：{}",msg);
    }
}
```
### 8.3.4 测试及结果分析
- 测试之前需要将confirm.exchange 交换机删除，否则会报错
![在这里插入图片描述](https://img-blog.csdnimg.cn/651574c8cbb7471b8a0ccccbbb885bba.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)mandatory 参数与备份交换机可以一起使用的时候，如果两者同时开启，消息究竟何去何从？谁优先级高，经过上面结果显示答案是**备份交换机优先级高**。

# 9. RabbitMQ 其它知识

## 9.1 幂等性
### 9.1.1 概念
- 用户对于同一操作发起的一次请求或者多次请求的结果是一致的，不会因为多次点击而产生了副作用。
- 举个最简单的例子，那就是支付，用户购买商品后支付，支付扣款成功，但是返回结果的时候网络异常，此时钱已经扣了，用户再次点击按钮，此时会进行第二次扣款，返回结果成功，用户查询余额发现多扣钱了，流水记录也变成了两条。在以前的单应用系统中，我们只需要把数据操作放入事务中即可，发生错误立即回滚，但是再响应客户端的时候也有可能出现网络中断或者异常等等。


### 9.1.2 消息重复消费
- 消费者在消费 MQ 中的消息时，MQ 已把消息发送给消费者，消费者在给MQ 返回 ack 时网络中断，故 MQ 未收到确认信息，该条消息会重新发给其他的消费者，或者在网络重连后再次发送给该消费者，但实际上该消费者已成功消费了该条消息，造成消费者消费了重复的消息。

### 9.1.3 解决思路
- MQ 消费者的幂等性的解决一般使用全局 ID 或者写个唯一标识比如时间戳 或者 UUID 或者订单消费者消费 MQ 中的消息也可利用 MQ 的该 id 来判断，或者可按自己的规则生成一个全局唯一 id，每次消费消息时用该 id 先判断该消息是否已消费过。

### 9.1.4 消费端的幂等性保障
- 在海量订单生成的业务高峰期，生产端有可能就会重复发生了消息，这时候消费端就要实现幂等性，这就意味着消息永远不会被消费多次，即使收到了一样的消息。业界主流的幂等性有两种操作:a.唯一 ID+指纹码机制,利用数据库主键去重, b.利用 redis 的原子性去实现

### 9.1.5 唯一ID+指纹码机制
- 指纹码:的一些规则或者时间戳加别的服务给到的唯一信息码,它并不一定是系统生成的，基本都是由业务规则拼接而来，但是一定要保证唯一性，然后就利用查询语句进行判断这个 id 是否存在数据库中,优势就是实现简单就一个拼接，然后查询判断是否重复；劣势就是在高并发时，如果是单个数据库就会有写入性能瓶颈当然也可以采用分库分表提升性能，但也不是最推荐的方式。

### 9.1.6 Redis原子性
- 利用 redis 执行 setnx 命令，天然具有幂等性。从而实现不重复消费

## 9.2 优先级队列
### 9.2.1 使用场景

- 在系统中有一个订单催付的场景，客户在天猫下的订单,淘宝会及时将订单推送给用户，如果在用户设定的时间内未付款那么就会给用户推送一条短信提醒，很简单的一个功能对吧，但是天猫商家对客户来说，肯定是要分大客户和小客户的对吧，比如像苹果，小米这样大商家一年起码能创造很大的利润，所以理应当然，他们的订单必须得到优先处理，而曾经我们的后端系统是使用 redis 来存放的定时轮询，大家都知道 redis 只能用 List 做一个简简单单的消息队列，并不能实现一个优先级的场景，所以订单量大了后采用 RabbitMQ 进行改造和优化,如果发现是大客户的订单给一个相对比较高的优先级，否则就是默认优先级。

### 9.2.2 如何添加
- 控制台添加
![在这里插入图片描述](https://img-blog.csdnimg.cn/ae1d50fc4c8449118b6f2f4df47d0446.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAeHVqaW5zaGFuMzYx,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)- 代码添加

```java
Map<String, Object> params = new HashMap<>();
params.put("x-max-priority",10); //官方设置的数0-255， 但是不推荐太大
channel.queueDeclare("hello",true,false,false,params);
```
- 消息中代码添加优先级

```java
AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().priority(5).build();
```

- 注意事项：要让队列实现优先级需要做的事情有如下事情:**队列需要设置为优先级队列，消息需要设置消息的优先级，消费者需要等待消息已经发送到队列中才去消费因为，这样才有机会对消息进行排序。**

## 9.3 惰性队列
### 9.3.1 使用场景
- RabbitMQ 从 3.6.0 版本开始引入了惰性队列的概念。惰性队列会尽可能的将消息存入磁盘中，而在消费者消费到相应的消息时才会被加载到内存中，它的一个重要的设计目标是能够支持更长的队列，即支持更多的消息存储。当消费者由于各种各样的原因(比如消费者下线、宕机亦或者是由于维护而关闭等)而致使长时间内不能消费消息造成堆积时，惰性队列就很有必要了。
- 默认情况下，当生产者将消息发送到 RabbitMQ 的时候，队列中的消息会尽可能的存储在内存之中，这样可以更加快速的将消息发送给消费者。即使是持久化的消息，在被写入磁盘的同时也会在内存中驻留一份备份。
- 当 RabbitMQ 需要释放内存的时候，会将内存中的消息换页至磁盘中，这个操作会耗费较长的时间，也会阻塞队列的操作，进而无法接收新的消息。虽然 RabbitMQ 的开发者们一直在升级相关的算法，但是效果始终不太理想，尤其是在消息量特别大的时候。


### 9.3.2 两种模式
- 队列具备两种模式：default 和 lazy。
- 默认的为default 模式，在3.6.0 之前的版本无需做任何变更。lazy模式即为惰性队列的模式，可以通过调用 channel.queueDeclare 方法的时候在参数中设置，也可以通过Policy 的方式设置，如果一个队列同时使用这两种方式设置的话，那么 Policy 的方式具备更高的优先级。
- 如果要通过声明的方式改变已有队列的模式的话，那么只能先删除队列，然后再重新声明一个新的。
- 在队列声明的时候可以通过“x-queue-mode”参数来设置队列的模式，取值为“default”和“lazy”。下面示例中演示了一个惰性队列的声明细节：


```java
Map<String, Object> args = new HashMap<String, Object>();
args.put("x-queue-mode", "lazy");
channel.queueDeclare("myqueue", false, false, false, args);
```