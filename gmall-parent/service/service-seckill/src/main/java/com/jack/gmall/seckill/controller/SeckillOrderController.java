package com.jack.gmall.seckill.controller;

import com.jack.gmall.common.result.Result;
import com.jack.gmall.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/14
 * @Description :
 **/
@RestController
@RequestMapping("/seckill/order")
public class SeckillOrderController {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 添加秒杀商品订单
     * @param time
     * @param goodsId
     * @param num
     * @return
     */
    @GetMapping("/addSeckillOrder")
    public Result addSeckillOrder(String time, String goodsId, Integer num){
        return Result.ok(seckillOrderService.addSeckillGoods(time, goodsId, num));
    }

    /**
     * 获取用户的秒杀排队状态
     * @return
     */
    @GetMapping("/getUserRecode")
    public Result getUserRecode(){
        return Result.ok(seckillOrderService.getUserRecode());
    }

    /**
     * 取消订单: 主动取消
     * @return
     */
    @GetMapping("/cancelSeckillOrder")
    public Result cancelSeckillOrder(){
        seckillOrderService.cancelSeckillOrder(null);
        return Result.ok();
    }
}
