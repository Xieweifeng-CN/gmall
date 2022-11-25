package com.jack.gmall.order.controller;

import com.jack.gmall.common.result.Result;
import com.jack.gmall.model.order.OrderInfo;
import com.jack.gmall.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/9
 * @Description : 订单相关业务控制层
 **/
@RestController
@RequestMapping("/api/order")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    /**
     * 保存订单与订单详情
     * @param orderInfo
     * @return
     */
    @PostMapping("/addOrder")
    public Result addOrder(@RequestBody OrderInfo orderInfo){
        orderInfoService.addOrderInfo(orderInfo);
        return Result.ok();
    }

    /**
     * 取消订单
     * @param orderId
     * @return
     */
    @GetMapping("/cancelOrder")
    public Result cancelOrder(Long orderId){
        orderInfoService.cancelOrder(orderId);
        return Result.ok();
    }
}