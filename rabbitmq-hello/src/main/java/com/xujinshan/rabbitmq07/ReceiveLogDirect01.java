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
