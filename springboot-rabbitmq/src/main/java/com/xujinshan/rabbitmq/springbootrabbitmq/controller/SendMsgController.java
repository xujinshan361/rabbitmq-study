package com.xujinshan.rabbitmq.springbootrabbitmq.controller;

import com.xujinshan.rabbitmq.springbootrabbitmq.config.DelayedQueueConfig;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Author: xujinshan361@163.com
 * TTL 消息生产者
 * 发送延迟消息
 */
@Slf4j
@RestController
@RequestMapping("/ttl")
public class SendMsgController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 开始发送消息
    @GetMapping("/sendMsg/{message}")
    public void sendMessage(@PathVariable String message) {
        log.info("当前时间：{},发送一条消息给两个TTL队列：{}", new Date().toString(), message);
        rabbitTemplate.convertAndSend("X", "XA", "消息来自TTL10s的队列:" + message);
        rabbitTemplate.convertAndSend("X", "XB", "消息来自TTL40s的队列:" + message);
    }

    // 开始发送消息，消息TTL
    @GetMapping("/sendExpriationMsg/{message}/{ttlTime}")
    public void sendMessage(@PathVariable String message, @PathVariable String ttlTime) {
        log.info("当前时间：{},发送一条时长{}毫秒的TTL信息给队列QC:{}", new Date().toString(), ttlTime, message);
        rabbitTemplate.convertAndSend("X", "XC", message, msg -> {
            // 发送消息的延迟时长
            msg.getMessageProperties().setExpiration(ttlTime);
            return msg;
        });
    }

    // 开始发送消息，基于插件的
    @GetMapping("/sendDelayedMsg/{message}/{delayedTime}")
    public void sendMessage(@PathVariable String message, @PathVariable Integer delayedTime) {
        log.info("当前时间：{},发送一条时长{}毫秒的信息给队列delayed.queue:{}", new Date().toString(), delayedTime, message);
        rabbitTemplate.convertAndSend(DelayedQueueConfig.DELAYED_EXCHANGE_NAME, DelayedQueueConfig.DELAYED_ROUTING_KEY, message, msg -> {
            // 发送消息的延迟时长
            msg.getMessageProperties().setDelay(delayedTime);
            return msg;
        });
    }
}
