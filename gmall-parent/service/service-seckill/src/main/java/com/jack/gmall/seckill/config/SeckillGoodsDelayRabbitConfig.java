package com.jack.gmall.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/15
 * @Description : 秒杀商品库存同步使用的延迟队列配置
 **/
@Configuration
public class SeckillGoodsDelayRabbitConfig {

    /**
     * 创建正常交换机
     * @return
     */
    @Bean("seckillGoodsNormalExchange")
    public Exchange seckillGoodsNormalExchange(){
        return ExchangeBuilder.directExchange("seckill_goods_normal_exchange").build();
    }

    /**
     * 创建死信队列
     * @return
     */
    @Bean("seckillGoodsDeadQueue")
    public Queue seckillGoodsDeadQueue(){
        return QueueBuilder
                .durable("seckill_goods_dead_queue")
                .withArgument("x-dead-letter-exchange","seckill_goods_dead_exchange")
                .withArgument("x-dead-letter-routing-key","seckill.goods.normal")
                .build();
    }

    /**
     * 正常交换机和死信队列绑定
     * @param seckillGoodsNormalExchange
     * @param seckillGoodsDeadQueue
     * @return
     */
    @Bean
    public Binding seckillGoodsDeadBinding(@Qualifier("seckillGoodsNormalExchange") Exchange seckillGoodsNormalExchange,
                                                                   @Qualifier("seckillGoodsDeadQueue") Queue seckillGoodsDeadQueue){
        return BindingBuilder.bind(seckillGoodsDeadQueue).to(seckillGoodsNormalExchange).with("seckill.goods.dead").noargs();
    }

    /**
     * 创建死信交换机
     * @return
     */
    @Bean("seckillGoodsDeadExchange")
    public Exchange seckillGoodsDeadExchange(){
        return ExchangeBuilder.directExchange("seckill_goods_dead_exchange").build();
    }

    /**
     * 创建正常队列
     * @return
     */
    @Bean("seckillGoodsNormalQueue")
    public Queue seckillGoodsNormalQueue(){
        return QueueBuilder.durable("seckill_goods_normal_queue").build();
    }

    /**
     * 死信交换机和正常队列绑定
     * @param seckillGoodsDeadExchange
     * @param seckillGoodsNormalQueue
     * @return
     */
    @Bean
    public Binding seckillGoodsNormalBinding(@Qualifier("seckillGoodsDeadExchange") Exchange seckillGoodsDeadExchange,
                                                                   @Qualifier("seckillGoodsNormalQueue") Queue seckillGoodsNormalQueue){
        return BindingBuilder.bind(seckillGoodsNormalQueue).to(seckillGoodsDeadExchange).with("seckill.goods.normal").noargs();
    }
}
