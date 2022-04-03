package com.xujinshan.rabbitmq.springbootrabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author: xujinshan361@163.com
 * 回调接口
 */
@Slf4j
@Component
public class MyCallBack implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {


    @Autowired
    private RabbitTemplate rabbitTemplate;
    // 注入
    @PostConstruct  // 在其他注解执行后执行
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }
    /**
     * 交换机确认回调方法
     * 发消息 交换机接收到了 回调
     * 发消息 交换机接收失败 回调
     * @param correlationData  保存回调信息的ID及相关信息
     * @param b     交换机收到消息为true
     * @param s     失败原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        String id = correlationData!=null?correlationData.getId():"";
        if(b){
            log.info("交换机已经收到ID为：{}的消息",id);
        }else {
            log.info("交换机还未收到ID为：{}的消息，由于原因：{}",id,s);
        }
    }

    /**
     * 高版本已经被弃用
     * 可以在消息传递过程中不可达目的地时将消息返回给生产者
     * @param
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.info("消息：{}，被交换机:{}退回,退回原因：{}，routingKey:{}",
                new String(message.getBody()),
                exchange,replyText,routingKey);
        RabbitTemplate.ReturnsCallback.super.returnedMessage(message, replyCode, replyText, exchange, routingKey);
    }

    /**
     * 高版本returnMessage已经被封装了
     * @param returnedMessage
     */
    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        log.info("消息：{}，被交换机:{}退回,退回原因：{}，routingKey:{}",new String(returnedMessage.getMessage().getBody()),
                returnedMessage.getExchange(),returnedMessage.getReplyText(),returnedMessage.getRoutingKey());
    }

}
