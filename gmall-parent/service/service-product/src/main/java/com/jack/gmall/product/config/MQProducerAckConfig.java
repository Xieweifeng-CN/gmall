package com.jack.gmall.product.config;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/11
 * @Description : rabbitMQ的消息确认配置类
 **/
@Configuration
@Log4j2
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this::confirm);
        rabbitTemplate.setReturnCallback(this::returnedMessage);
    }

    /**
     * 消息未抵达交换机确认
     * @param correlationData
     * @param b
     * @param s
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
            if(!b){
                log.info("消息抵达交换机失败,原因为: " + s + ", 消息的内容为: " + JSONObject.toJSONString(correlationData));
            }
    }

    /**
     * 消息未抵达队列确认
     * @param message
     * @param i
     * @param s
     * @param s1
     * @param s2
     */
    @Override
    public void returnedMessage(Message message, int i, String s, String s1, String s2) {
        log.error("消息抵达队列失败,内容为: " + new String(message.getBody()));
        log.error("消息抵达队列失败,状态码为: " + i);
        log.error("消息抵达队列失败,响应内容为: " + s);
        log.error("消息抵达队列失败,交换机名字为: " + s1);
        log.error("消息抵达队列失败,转发routingKey名字为: " + s2);
    }
}
