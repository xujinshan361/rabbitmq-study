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
