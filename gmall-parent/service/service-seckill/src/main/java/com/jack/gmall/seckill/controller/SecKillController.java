package com.jack.gmall.seckill.controller;

import com.jack.gmall.common.result.Result;
import com.jack.gmall.seckill.service.SeckillGoodsService;
import com.jack.gmall.seckill.util.DateUtil;
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
@RequestMapping("/seckill/goods")
public class SecKillController {

    /**
     * 获取最近的5个时间段
     * @return
     */
    @GetMapping("/getMenus")
    public Result getMenus(){
        return Result.ok(DateUtil.getDateMenus());
    }

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    /**
     * 查询指定时间段的商品列表
     * @param time
     * @return
     */
    @GetMapping("/getSeckillGoods")
    public Result getSeckillGoods(String time){
        return Result.ok(seckillGoodsService.getSeckillGoods(time));
    }

    /**
     * 查询指定时间段的指定商品
     * @param time
     * @param orderId
     * @return
     */
    @GetMapping("/getSeckillGood")
    public Result getSeckillGood(String time,String orderId){
        return Result.ok(seckillGoodsService.getSeckillGood(time, orderId));
    }
}
