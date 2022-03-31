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
