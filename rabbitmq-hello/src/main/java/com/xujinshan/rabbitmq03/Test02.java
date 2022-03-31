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
