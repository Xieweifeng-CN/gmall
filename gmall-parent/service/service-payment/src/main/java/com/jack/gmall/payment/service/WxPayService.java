package com.jack.gmall.payment.service;

import java.util.Map;

/**
 * 微信支付业务接口
 */
public interface WxPayService {

    /**
     * 获取支付二维码
     * @param payParams
     * @return
     */
    public Map<String, String> getPageCode(Map<String, String> payParams);

    /**
     * 获取订单的支付结果
     * @param orderId
     * @return
     */
    public Map<String, String> getPayResult(String orderId);
}
