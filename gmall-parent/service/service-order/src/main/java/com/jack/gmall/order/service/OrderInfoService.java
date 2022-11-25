package com.jack.gmall.order.service;

import com.jack.gmall.model.order.OrderInfo;

/**
 * 订单相关业务接口
 */
public interface OrderInfoService {

    /**
     * 添加订单信息
     */
    void addOrderInfo(OrderInfo orderInfo);

    /**
     * 取消订单
     * @param orderId
     */
    void cancelOrder(Long orderId);

    /**
     * 修改订单状态: 已支付
     * @param resultJsonString
     */
    void updateOrder(String resultJsonString);
}
