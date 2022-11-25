package com.jack.gmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/8
 * @Description : 购物车模块启动类
 **/
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.jack.gmall")
@EnableFeignClients(basePackages = "com.jack.gmall.product.feign")
@ServletComponentScan("com.jack.gmall.cart.filter")
public class CartInfoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartInfoApplication.class,args);
    }

}
