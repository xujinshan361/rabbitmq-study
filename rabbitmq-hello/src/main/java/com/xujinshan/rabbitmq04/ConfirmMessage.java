package com.xujinshan.rabbitmq04;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @Author: xujinshan361@163.com
 * 发布确认
 * 使用哪个时间，比较哪种确认方式是最好的
 * 1.单个确认发布       耗时：598ms
 * 2.批量确认发布       耗时：119ms
 * 3.异步确认发布       耗时：90ms
 */
public class ConfirmMessage {
    // 批量发消息的个数
    public static final Integer MESSAGE_COUNT =1000;
    public static void main(String[] args) throws Exception {
        // 1.单个确认发布
      //  ConfirmMessage.publishMessageIndividually();

        // 2.批量确认发布
//        ConfirmMessage.publishMessageBatch();
        // 3.异步确认发布
        ConfirmMessage.publishMessageAsync();
    }

    /**
     * 单个确认发布
     * @throws Exception
     */
    public static void publishMessageIndividually() throws Exception{
        Channel channel = RabbitMQUtils.getChannel();
        // 队列的声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName,true,false,false,null);
        // 开启发布确认
        channel.confirmSelect();
        // 开始时间
        long begin = System.currentTimeMillis();

        // 批量发消息
        for (Integer i = 0; i < MESSAGE_COUNT; i++) {
            String message = i+"";
            channel.basicPublish("",queueName,null,message.getBytes(StandardCharsets.UTF_8));
            // 单个消息就马上进行发布确认
            boolean b = channel.waitForConfirms();
            if(b){
                System.out.println("消息发送成功");
            }
        }
        // 结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个单独确认消息，耗时：" + (end - begin) + "ms");
    }

    /**
     * 批量确认发布
     * @throws Exception
     */
    public static void publishMessageBatch() throws Exception{
        Channel channel = RabbitMQUtils.getChannel();
        // 队列的声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName,true,false,false,null);
        // 开启发布确认
        channel.confirmSelect();
        // 开始时间
        long begin = System.currentTimeMillis();

        // 批量确认消息大小
        int batchSize =100;
        // 批量发送消息 批量确认发布
        for (Integer i = 0; i < MESSAGE_COUNT; i++) {
            String message = i+"";
            channel.basicPublish("",queueName,null,message.getBytes(StandardCharsets.UTF_8));
            // 判断达到100条消息，批量确认一次
            if(i%batchSize ==0){
                // 发布确认
                channel.waitForConfirms();
            }
        }
        // 结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个批量确认消息，耗时：" + (end - begin) + "ms");
    }

    /**
     * 异步确认发布
     * @throws Exception
     */
    public static void publishMessageAsync() throws Exception{
        Channel channel = RabbitMQUtils.getChannel();
        // 队列的声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName,true,false,false,null);
        // 开启发布确认
        channel.confirmSelect();
        // 开始时间
        long begin = System.currentTimeMillis();
        // 消息确认成功回调函数
        ConfirmCallback ackCallback = (deliveryTag,multiple)->{
            System.out.println("确认消息：" + deliveryTag);
        };
        /**
         * 消息确认失败回调函数
         * 参数：
         *      1.消息的标记
         *      2.是否为批量确认
         */
        ConfirmCallback nackCallback = (deliveryTag,multiple)->{
            System.out.println("未确认消息：" + deliveryTag);
        };
        // 准备消息的监听器，监听哪些消息成功，哪些消息失败
        /**
         * 1.监听哪些消息成功了
         * 2.监听哪些消息失败了
         */
        channel.addConfirmListener(ackCallback,nackCallback);  //异步通知

        // 批量发送消息 异步批量确认
        for (Integer i = 0; i < MESSAGE_COUNT; i++) {
            String message = i+"";
            channel.basicPublish("",queueName,null,message.getBytes(StandardCharsets.UTF_8));

        }
        // 结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个异步发布确认消息，耗时：" + (end - begin) + "ms");
    }
}
