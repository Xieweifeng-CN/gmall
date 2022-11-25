package com.jack.gmall.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/14
 * @Description : 秒杀微服务线程池配置类
 **/
@Configuration
public class SeckillThreadPoolConfig {

    /**
     * 自定义线程池
     * @return
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        return new ThreadPoolExecutor(
                8,
                8,
                10,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000));
    }

}
