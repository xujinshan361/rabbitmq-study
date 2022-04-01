package com.xujinshan.rabbitmq07;

import com.rabbitmq.client.Channel;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @Author: xujinshan361@163.com
 */
public class DirectLogs {
    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQUtils.getChannel();

        Scanner sc = new Scanner(System.in);
        System.out.println("输入信息：");
        while (sc.hasNext()) {
            String message = sc.nextLine();
            String routingKey = "error";   // 通过改变值进行测试
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("生产者发出消息：" + message);
        }
    }
}
