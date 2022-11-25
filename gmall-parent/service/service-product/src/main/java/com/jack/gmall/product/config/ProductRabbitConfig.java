package com.jack.gmall.product.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/11
 * @Description : 商品MQ队列配置类
 **/
@Configuration
public class ProductRabbitConfig {

    /**
     * 商品交换机
     * @return
     */
    @Bean("productExchange")
    public Exchange productExchange(){
        return ExchangeBuilder.directExchange("product_exchange").build();
    }

    /**
     * 商品上架队列
     * @return
     */
    @Bean("productUpperQueue")
    public Queue productUpperQueue(){
        return QueueBuilder.durable("product_upper_queue").build();
    }

    /**
     * 商品上架队列绑定
     * @return
     */
    @Bean
    public Binding productUpperBinding(@Qualifier("productExchange") Exchange productExchange,
                                                            @Qualifier("productUpperQueue") Queue productUpperQueue){
        return BindingBuilder.bind(productUpperQueue).to(productExchange).with("product.upper").noargs();
    }

    /**
     * 商品下架队列
     * @return
     */
    @Bean("productDownQueue")
    public Queue productDownQueue(){
        return QueueBuilder.durable("product_down_queue").build();
    }

    /**
     * 商品下架队列绑定
     * @return
     */
    @Bean
    public Binding productDownBinding(@Qualifier("productExchange") Exchange productExchange,
                                                             @Qualifier("productDownQueue") Queue productDownQueue){
        return BindingBuilder.bind(productDownQueue).to(productExchange).with("product.down").noargs();
    }


}
