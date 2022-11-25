package com.jack.gmall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/9
 * @Description : 订单微服务启动类
 **/
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.jack.gmall")
@EnableFeignClients(basePackages = {"com.jack.gmall.cart.feign","com.jack.gmall.product.feign"})
@ServletComponentScan("com.jack.gmall.order.filter")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class,args);
    }

}
