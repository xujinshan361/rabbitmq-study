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
