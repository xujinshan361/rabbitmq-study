package com.xujinshan.rabbitmq.springbootrabbitmq.consumer;

import com.xujinshan.rabbitmq.springbootrabbitmq.config.DelayedQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author: xujinshan361@163.com
 * 消费者-- 基于插件的延迟
 */
@Slf4j
@Component
public class DelayedQueueConsumer {
    // 监听消息
    @RabbitListener(queues = DelayedQueueConfig.DELAYED_QUEUE_NAME)
    public void receiveDelayedQueue(Message message){
        String msg = new String(message.getBody());
        System.out.println(message.getBody());
        log.info("当前时间：{}，收到延迟队列消息：{}",new Date().toString(),msg);
    }

}
