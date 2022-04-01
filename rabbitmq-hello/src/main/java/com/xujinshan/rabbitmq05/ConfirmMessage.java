package com.xujinshan.rabbitmq05;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.xujinshan.rabbitmqutils.RabbitMQUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @Author: xujinshan361@163.com
 * 解决发布未确认问题
 */
public class ConfirmMessage {
    // 批量发消息的个数
    public static final Integer MESSAGE_COUNT =1000;
    public static void main(String[] args) throws Exception {

        // 异步确认发布
        ConfirmMessage.publishMessageAsync();
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

        /**
         * 处理并发的有序map集合，ConcurrentHashMap是无序的
         * 1、轻松的将序号与消息进行关联
         * 2.轻松批量删除条目，只要给到序号
         * 3.支持高并发（多线程）
         */
        ConcurrentSkipListMap<Long,String> map = new ConcurrentSkipListMap<>();
        // 开始时间
        long begin = System.currentTimeMillis();
        // 消息确认成功回调函数
        ConfirmCallback ackCallback = (deliveryTag,multiple)->{
            // 2、删除掉已经确认的消息，剩下的都是未确认消息
            System.out.println("确认消息：" + deliveryTag);
            // 返回消息的时候是批量确认的，批量确认需要判断
            if(multiple){
                ConcurrentNavigableMap<Long,String> confirmed = map.headMap(deliveryTag,true);
                confirmed.clear();
            }else {
                map.remove(deliveryTag);
            }
            if(map.isEmpty()){
                System.out.println("所有消息发送成功！");
            }
        };
        /**
         * 消息确认失败回调函数
         * 参数：
         *      1.消息的标记
         *      2.是否为批量确认
         */
        ConfirmCallback nackCallback = (deliveryTag,multiple)->{
            // 3、打印一下未确认的消息有哪些
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
            // 1、记录下所有的要发送的消息 消息总和
            // channel.getNextPublishSeqNo()  获得的是下一个发布的序列号，当前序列号需要减一！
            map.put(channel.getNextPublishSeqNo()-1,message);
        }
        // 结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个异步发布确认消息，耗时：" + (end - begin) + "ms");
    }
}
