package com.jack.gmall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/22
 * @Description :
 **/
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.jack.gmall")
@EnableFeignClients(basePackages = "com.jack.gmall.goods.feign")
public class ProductSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductSpringApplication.class,args);
    }

}
