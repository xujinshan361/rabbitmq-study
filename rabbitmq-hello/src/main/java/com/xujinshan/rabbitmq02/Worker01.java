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
