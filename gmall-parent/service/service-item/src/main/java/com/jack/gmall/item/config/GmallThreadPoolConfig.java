package com.jack.gmall.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/31
 * @Description : 自定义线程池
 **/
@Configuration
public class GmallThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        return new ThreadPoolExecutor(
                8,
                8,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000));
    }
}
