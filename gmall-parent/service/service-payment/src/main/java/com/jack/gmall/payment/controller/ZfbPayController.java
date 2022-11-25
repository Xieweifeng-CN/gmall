package com.jack.gmall.payment.controller;

import com.alibaba.fastjson.JSONObject;
import com.jack.gmall.payment.service.ZfbPayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/14
 * @Description : 支付宝支付的控制层
 **/
@RestController
@RequestMapping("/zfb/pay")
public class ZfbPayController {

    @Autowired
    private ZfbPayService zfbPayService;

    /**
     * 获取支付宝支付页面
     * @param orderId
     * @param money
     * @param desc
     * @return
     */
    @GetMapping("/getPayPage")
    public String getPayPage(@RequestParam String orderId,
                                           @RequestParam String money,
                                           @RequestParam String desc){
        return zfbPayService.getPageCode(orderId, money, desc);
    }

    /**
     * 获取订单的支付结果
     * @param orderId
     * @return
     */
    @GetMapping("/getPayResult")
    public Map<String, String> getPayResult(@RequestParam String orderId){
        return zfbPayService.getPayResult(orderId);
    }

    /**
     * 同步回调: 用户在支付宝那边付完钱以后,跳转回来的地址
     * @return
     */
    @RequestMapping(value = "/callback/return")
    public String callbackReturn(@RequestParam Map<String, String> returnMap){
        System.out.println(JSONObject.toJSONString(returnMap));
        return "支付宝支付完成,跳转回到商城里面来了!";
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 异步通知地址: 用户支付完成以后,支付宝通知商城支付结果的接口
     * @param notifyMap
     * @return
     */
    @RequestMapping(value = "/callback/notify")
    public String callbackNotify(@RequestParam Map<String, String> notifyMap){

//        String result = "{\"gmt_create\":\"2022-11-14 09:13:34\",\"charset\":\"utf-8\",\"gmt_payment\":\"2022-11-14 09:13:38\",\"notify_time\":\"2022-11-14 09:13:38\",\"subject\":\"梅旺旺的大玩具4565\",\"sign\":\"A3w3jXIf3GDH8uqbfqqEbmK/o1vGLF/VYnej1hEiJhJs8tg19OSJM7fo6BFbMxT0jRH9L5gm+ppiCicNQZy1338lrhKZuaStiWAWw3kVZP1MVM3LRX9HUZcnsAJs984vcpeDcB7aIxaCJYDn6zd2nBq95CVSu6L2g7O0aS9ZuEzN80A9juvE10B8y4ZH86iCv8A71HinxW5e96ski/fj8QvUWAyG6/RRZnGxGa+HWj3dDJ++oARWf2mE5hLZOlhwW3+oUr2rhHIbM3Lv0YOk168eoY+Q7b99opoYWIofjdAcd77+/fw/xQBSmyKtvrHPpLHe7285nhc7akkuSoHsBQ==\",\"buyer_id\":\"2088512600092751\",\"invoice_amount\":\"0.01\",\"version\":\"1.0\",\"notify_id\":\"2022111401222091338092751417015454\",\"fund_bill_list\":\"[{\\\"amount\\\":\\\"0.01\\\",\\\"fundChannel\\\":\\\"ALIPAYACCOUNT\\\"}]\",\"notify_type\":\"trade_status_sync\",\"out_trade_no\":\"19\",\"total_amount\":\"0.01\",\"trade_status\":\"TRADE_SUCCESS\",\"trade_no\":\"2022111422001492751406232110\",\"auth_app_id\":\"2021001163617452\",\"receipt_amount\":\"0.01\",\"point_amount\":\"0.00\",\"buyer_pay_amount\":\"0.01\",\"app_id\":\"2021001163617452\",\"sign_type\":\"RSA2\",\"seller_id\":\"2088831489324244\"}";
//        rabbitTemplate.convertAndSend("pay_exchange", "pay.order" , result);
        return "success";
    }
}
