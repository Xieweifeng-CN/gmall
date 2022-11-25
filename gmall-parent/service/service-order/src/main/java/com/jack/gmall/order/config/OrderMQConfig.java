package com.jack.gmall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/11
 * @Description : 订单延迟队列配置类
 **/
@Configuration
public class OrderMQConfig {

    /**
     * 正常交换机
     * @return
     */
    @Bean("normalExchange")
    public Exchange normalExchange(){
        return ExchangeBuilder.directExchange("order_normal_exchange").build();
    }

    /**
     * 死信队列
     * @return
     */
    @Bean("deadQueue")
    public Queue deadQueue(){
        return QueueBuilder
                .durable("order_dead_queue")
                .withArgument("x-dead-letter-exchange","order_dead_exchange")
                .withArgument("x-dead-letter-routing-key","order.dead")
                .build();
    }

    /**
     * 死信队列绑定正常交换机
     * @param normalExchange
     * @param deadQueue
     * @return
     */
    @Bean
    public Binding normalBinding(@Qualifier("normalExchange") Exchange normalExchange,
                                                   @Qualifier("deadQueue") Queue deadQueue){
        return BindingBuilder.bind(deadQueue).to(normalExchange).with("order.normal").noargs();
    }

    /**
     * 死信交换机
     * @return
     */
    @Bean("deadExchange")
    public Exchange deadExchange(){
        return ExchangeBuilder.directExchange("order_dead_exchange").build();
    }

    /**
     * 正常队列
     * @return
     */
    @Bean("normalQueue")
    public Queue normalQueue(){
        return QueueBuilder.durable("order_normal_queue").build();
    }

    /**
     * 正常队列与死信交换机绑定
     * @param deadExchange
     * @param normalQueue
     * @return
     */
    @Bean
    public Binding deadBinding(@Qualifier("deadExchange") Exchange deadExchange,
                                                @Qualifier("normalQueue") Queue normalQueue){
        return BindingBuilder.bind(normalQueue).to(deadExchange).with("order.dead").noargs();
    }
}
