package com.jack.gmall.seckill.service;

import com.jack.gmall.model.activity.SeckillGoods;

import java.util.List;

/**
 * 秒杀商品相关业务接口
 */
public interface SeckillGoodsService {

    /**
     * 查询指定时间段商品
     * @param time
     * @return
     */
    public List<SeckillGoods> getSeckillGoods(String time);

    /**
     * 查询指定时间段的指定商品
     * @param time
     * @param orderId
     * @return
     */
    public SeckillGoods getSeckillGood(String time, String orderId);

    /**
     * 同步指定时间段的商品剩余库存到数据库中
     * @param time
     */
    public void updateSeckillGoodsStock(String time);
}
