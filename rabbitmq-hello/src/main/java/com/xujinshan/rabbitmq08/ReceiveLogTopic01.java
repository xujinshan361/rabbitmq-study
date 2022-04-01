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
