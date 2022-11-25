package com.jack.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayUtil;
import com.jack.gmall.payment.service.WxPayService;
import com.jack.gmall.payment.util.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/13
 * @Description :
 **/
@Service
public class WxPayServiceImpl implements WxPayService {

    @Value("${weixin.pay.appid}")
    private String appId;

    @Value("${weixin.pay.partner}")
    private String partner;

    @Value("${weixin.pay.partnerkey}")
    private String partnerkey;

    @Value("${weixin.pay.notifyUrl}")
    private String notifyUrl;

    /**
     * 获取支付二维码
     *
     * @param payParams
     * @return
     */
    @Override
    public Map<String, String> getPageCode(Map<String, String> payParams) {
        //校验参数
        if (StringUtils.isEmpty(payParams.get("orderId")) ||
                StringUtils.isEmpty(payParams.get("money")) ||
                StringUtils.isEmpty(payParams.get("desc"))){
            return null;
        }
        try {
            //第三方api支付下单url
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            //包装请求参数,xml数据
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("appid",appId);
            paramMap.put("mch_id",partner);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("body",payParams.get("desc"));
            paramMap.put("out_trade_no",payParams.get("orderId"));
            paramMap.put("total_fee",payParams.get("money"));
            paramMap.put("spbill_create_ip","192.168.200.1");
            paramMap.put("notify_url",notifyUrl);
            paramMap.put("trade_type","NATIVE");
            //包装附加参数
            HashMap<String, String> attach = new HashMap<>();
            attach.put("exchange",payParams.get("exchange"));
            attach.put("routingKey",payParams.get("routingKey"));
            //若用户名不为空, 也保存用户名
            if (!StringUtils.isEmpty(payParams.get("username"))){
                attach.put("username",payParams.get("username"));
            }
            paramMap.put("attach", JSONObject.toJSONString(attach));
            String xmlParams = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            //向这个地址发起post请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParams);
            //向这个地址发起post请求
            httpClient.post();
            //获取结果
            String contentXml = httpClient.getContent();
            //解析结果,微信返回的xml数据
            Map<String, String> map = WXPayUtil.xmlToMap(contentXml);
            if (map.get("return_code").equals("SUCCESS") && map.get("result_code").equals("SUCCESS")){
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取订单的支付结果
     *
     * @param orderId
     * @return
     */
    @Override
    public Map<String, String> getPayResult(String orderId) {
        if (StringUtils.isEmpty(orderId)){
            return null;
        }
        try {
            //第三方api支付结果查询url
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            //包装请求参数
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("appid", appId);
            paramMap.put("mch_id", partner);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("out_trade_no", "java0509_jack_00000" + orderId);
            String paramXml = WXPayUtil.generateSignedXml(paramMap,partnerkey);
            //向这个地址发送post请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setXmlParam(paramXml);
            httpClient.setHttps(true);
            httpClient.post();
            //获取请求结果
            String contentXml = httpClient.getContent();
            //解析结果: 微信返回给我们的是xml数据
            return WXPayUtil.xmlToMap(contentXml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
