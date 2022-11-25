package com.jack.gmall.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/31
 * @Description : 前端页面工程的启动类
 **/
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.jack.gmall.item.feign",
                                                            "com.jack.gmall.search.feign",
                                                            "com.jack.gmall.product.feign"})
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class,args);
    }
}