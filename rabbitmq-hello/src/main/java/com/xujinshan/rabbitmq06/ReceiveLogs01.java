package com.xujinshan.rabbitmq06;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

/**
 * @Author: xujinshan361@163.
 * ReceiveLogs01 将接收到的消息打印给到控制台
 *
 */
public class ReceiveLogs01 {
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        // 声明交换机-- fanout类型
        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
        /**
         * 生成一个临时的队列 队列的名称是随机的
         * 当消费者断开和该队列的连接  队列自动删除
         */
        String queueName = channel.queueDeclare().getQueue();
        // 把该临时队列绑定我们的exchange，其中routingKey(也称为bindings key) 为空串
        channel.queueBind(queueName,EXCHANGE_NAME,"");
        System.out.println("等待接收消息，把接收到的消息打印到屏幕....");
        // 接收消息
        DeliverCallback deliverCallback = (consumerTag,delivery)->{
            String message = new String(delivery.getBody(),"UTF-8");
            System.out.println("控制台打印接收到的消息："+ message);
        };

        channel.basicConsume(queueName,true,deliverCallback,consumerTag->{});
    }
}
