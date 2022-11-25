package com.jack.gmall.payment.service;

import java.util.Map;

/**
 * 支付宝支付业务接口
 */
public interface ZfbPayService {

    /**
     * 获取支付二维码
     * @param orderId
     * @param money
     * @param desc
     * @return
     */
    public String getPageCode(String orderId, String money, String desc);

    /**
     * 获取订单支付结果
     * @param orderId
     * @return
     */
    Map<String, String> getPayResult(String orderId);
}
