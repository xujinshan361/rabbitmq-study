package com.xujinshan.rabbitmqutils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @Author: xujinshan361@163.com
 * 抽取工具类
 * 连接工厂创建信道的工具类
 */
public class RabbitMQUtils {
    /**
     * 获取信道
     * @return
     * @throws Exception
     */
    public static Channel getChannel() throws Exception{
        // 创建一个工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.1.102");
        factory.setUsername("admin");
        factory.setPassword("admin");
        Connection connection = factory.newConnection();
        return connection.createChannel();
    }
}
