package com.xujinshan.rabbitmq06;

import com.rabbitmq.client.Channel;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @Author: xujinshan361@163.com
 * EmitLog 发送消息给两个消费者接收
 */
public class EmitLog {
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();
        /**
         * 声明一个exchange
         * 1.exchange 的名称
         * 2.exchange 的类型
         */
        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
        Scanner sc = new Scanner(System.in);
        System.out.println("输入信息：");
        while (sc.hasNext()){
            String  message = sc.nextLine();
            channel.basicPublish(EXCHANGE_NAME,"",null,message.getBytes(StandardCharsets.UTF_8));
            System.out.println("生产者发出消息："+message);
        }
    }
}
