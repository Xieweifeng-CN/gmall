package com.jack.gmall.payment;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/13
 * @Description : 支付微服务启动类
 **/
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.jack.gmall")
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class,args);
    }

    @Value("${alipay_url}")
    private String alipayUrl;

    @Value("${app_id}")
    private String appId;

    @Value("${app_private_key}")
    private String appPrivateKey;

    @Value("${alipay_public_key}")
    private String alipayPublicKey;

    @Bean
    public AlipayClient alipayClient(){
        //支付宝支付的客户端对象初始化(连接初始化)
        return new DefaultAlipayClient(
                alipayUrl,
                appId,
                appPrivateKey,
                "json",
                "utf-8",
                alipayPublicKey,
                "RSA2");
    }
}
