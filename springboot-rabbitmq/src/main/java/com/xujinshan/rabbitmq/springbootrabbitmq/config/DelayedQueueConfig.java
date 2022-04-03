package com.xujinshan.rabbitmq.springbootrabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xujinshan361@163.com
 */
@Configuration
public class DelayedQueueConfig {

    // 交换机
    public static final String DELAYED_QUEUE_NAME = "delayed.queue";
    // 队列
    public static final String DELAYED_EXCHANGE_NAME = "delayed.exchange";
    // routingKey
    public static final String DELAYED_ROUTING_KEY = "delayed.routingKey";

    // 声明交换机-自定义交换机(基于插件的)
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-delayed-type", "direct");
        return new CustomExchange(DELAYED_EXCHANGE_NAME, "x-delayed-message",
                false, false, arguments);
    }

    // 声明队列
    @Bean
    public Queue delayedQueue() {
        return new Queue(DELAYED_QUEUE_NAME);
    }

    // 绑定
    @Bean
    public Binding delayedQueueBindingDelayedExchange(@Qualifier("delayedQueue") Queue delayedQueue,
                                                      @Qualifier("delayedExchange") CustomExchange delayedExchange) {
        return BindingBuilder.bind(delayedQueue).to(delayedExchange).with(DELAYED_ROUTING_KEY).noargs();
    }
}
