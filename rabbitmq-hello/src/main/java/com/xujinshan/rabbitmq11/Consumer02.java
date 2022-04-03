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
