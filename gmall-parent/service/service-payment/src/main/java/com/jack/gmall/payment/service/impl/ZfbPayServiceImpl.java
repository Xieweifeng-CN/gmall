package com.jack.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.jack.gmall.payment.service.ZfbPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/14
 * @Description :
 **/
@Service
public class ZfbPayServiceImpl implements ZfbPayService {

    @Value("${return_payment_url}")
    private String returnPaymentUrl;

    @Value("${notify_payment_url}")
    private String notifyPaymentUrl;

    @Autowired
    private AlipayClient alipayClient;

    /**
     * 获取支付二维码
     *
     * @param orderId
     * @param money
     * @param desc
     * @return
     */
    @Override
    public String getPageCode(String orderId, String money, String desc) {
        //请求体对象初始化
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        //用户支付完成以后,到哪里通知支付结果
        request.setNotifyUrl(notifyPaymentUrl);
        //用户支付完成以后,跳转到哪里
        request.setReturnUrl(returnPaymentUrl);
        //包装请求参数
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", "java0509_jack_0000" + orderId);
        bizContent.put("total_amount", money);
        bizContent.put("subject", desc);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        //设置请求参数
        request.setBizContent(bizContent.toString());
        try {
            //发起请求
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if(response.isSuccess()){
                return response.getBody();
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取订单支付结果
     *
     * @param orderId
     * @return
     */
    @Override
    public Map<String, String> getPayResult(String orderId) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", "java0509_jack_0000" + orderId);
        request.setBizContent(bizContent.toString());
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if(response.isSuccess()){
                System.out.println("调用成功");
            } else {
                System.out.println("调用失败");
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }
}
