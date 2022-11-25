package com.jack.gmall.list;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/2
 * @Description : 搜索微服务的启动类
 **/
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
@ComponentScan("com.jack.gmall")
@EnableElasticsearchRepositories("com.jack.gmall.list.dao")
@EnableFeignClients(basePackages = "com.jack.gmall.product.feign")
public class ListApplication {

    public static void main(String[] args) {
        SpringApplication.run(ListApplication.class,args);
    }

}
