package com.xujinshan.rabbitmq.springbootrabbitmq.controller;

import com.xujinshan.rabbitmq.springbootrabbitmq.config.ConfirmConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: xujinshan361@163.com
 *  测试确认
 */
@Slf4j
@Controller
@RequestMapping("/confirm")
public class ProducerController {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    // 发消息
    @GetMapping("/sendMessage/{message}")
    public void sendMessage(@PathVariable String message){
        //指定消息 id 为 1
        CorrelationData correlationData1=new CorrelationData("1");
        String routingKey="key1";
        rabbitTemplate.convertAndSend(ConfirmConfig.CONFIRM_EXCHANGE_NAME,routingKey,
                message+routingKey,correlationData1);
        log.info("发送消息内容:{}",message+routingKey);
        CorrelationData correlationData2=new CorrelationData("2");
        routingKey="key2";
        rabbitTemplate.convertAndSend(ConfirmConfig.CONFIRM_EXCHANGE_NAME,
                routingKey,message+routingKey,correlationData2);
        log.info("发送消息内容:{}",message+routingKey);
    }
}

