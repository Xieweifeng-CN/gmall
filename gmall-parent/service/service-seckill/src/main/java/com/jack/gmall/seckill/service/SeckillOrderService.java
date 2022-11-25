package com.jack.gmall.seckill.service;

import com.jack.gmall.seckill.pojo.UserRecode;

/**
 * 秒杀商品订单相关业务接口
 */
public interface SeckillOrderService {

    /**
     * 生成商品购买订单
     * @param time
     * @param goodsId
     * @param num
     * @return
     */
    public UserRecode addSeckillGoods(String time, String goodsId, Integer num);

    /**
     * 查询秒杀排队状态
     * @return
     */
    public UserRecode getUserRecode();

    /**
     * 秒杀真实下单方法
     * @param userRecordString
     */
    public void listenerAddSeckillOrder(String userRecordString);

    /**
     * 取消订单: 1.主动取消 2.超时取消
     * @param username
     */
    public void cancelSeckillOrder(String username);

    /**
     * 修改支付结果
     * @param payResultJsonString
     */
    public void updateSeckillOrder(String payResultJsonString);

}
