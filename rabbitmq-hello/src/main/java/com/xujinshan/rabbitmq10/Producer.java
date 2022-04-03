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
