package com.jack.gmall.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/15
 * @Description : 秒杀订单超时取消支付的延迟队列配置
 **/
@Configuration
public class SeckillOrderDelayRabbitConfig {

    /**
     * 创建正常交换机
     * @return
     */
    @Bean("seckillOrderNormalExchange")
    public Exchange seckillOrderNormalExchange(){
        return ExchangeBuilder.directExchange("seckill_order_normal_exchange").build();
    }

    /**
     * 创建死信队列
     * @return
     */
    @Bean("seckillOrderDeadQueue")
    public Queue seckillOrderDeadQueue(){
        return QueueBuilder
                .durable("seckill_order_dead_queue")
                .withArgument("x-dead-letter-exchange","seckill_order_dead_exchange")
                .withArgument("x-dead-letter-routing-key","seckill.order.normal")
                .build();
    }

    /**
     * 正常交换机和死信队列绑定
     * @param seckillOrderNormalExchange
     * @param seckillOrderDeadQueue
     * @return
     */
    @Bean
    public Binding seckillOrderDeadBinding(@Qualifier("seckillOrderNormalExchange") Exchange seckillOrderNormalExchange,
                                                                  @Qualifier("seckillOrderDeadQueue") Queue seckillOrderDeadQueue){
        return BindingBuilder.bind(seckillOrderDeadQueue).to(seckillOrderNormalExchange).with("seckill.order.dead").noargs();
    }

    /**
     * 创建死信交换机
     * @return
     */
    @Bean("seckillOrderDeadExchange")
    public Exchange seckillOrderDeadExchange(){
        return ExchangeBuilder.directExchange("seckill_order_dead_exchange").build();
    }

    /**
     * 创建正常队列
     * @return
     */
    @Bean("seckillOrderNormalQueue")
    public Queue seckillOrderNormalQueue(){
        return QueueBuilder.durable("seckill_order_normal_queue").build();
    }

    /**
     * 死信交换机和正常队列绑定
     * @param seckillOrderDeadExchange
     * @param seckillOrderNormalQueue
     * @return
     */
    @Bean
    public Binding seckillOrderNormalBinding(@Qualifier("seckillOrderDeadExchange") Exchange seckillOrderDeadExchange,
                                                                   @Qualifier("seckillOrderNormalQueue") Queue seckillOrderNormalQueue){
        return BindingBuilder.bind(seckillOrderNormalQueue).to(seckillOrderDeadExchange).with("seckill.order.normal").noargs();
    }
}
