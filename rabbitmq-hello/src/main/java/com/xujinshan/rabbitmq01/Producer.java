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
