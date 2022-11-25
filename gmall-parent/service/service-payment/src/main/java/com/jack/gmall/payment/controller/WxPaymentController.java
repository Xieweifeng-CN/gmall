package com.jack.gmall.payment.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayUtil;
import com.jack.gmall.payment.service.WxPayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/13
 * @Description :
 **/
@RestController
@RequestMapping("/wx/pay/")
public class WxPaymentController {

    @Autowired
    private WxPayService wxPayService;

    /**
     * 获取微信支付二维码
     * @param payParams
     * @return
     */
    @GetMapping("/getPayCodeUrl")
    public Map<String, String> getPayCodeUrl(@RequestParam Map<String, String> payParams){
        return wxPayService.getPageCode(payParams);
    }

    /**
     * 获取订单的支付结果
     * @param orderId
     * @return
     */
    @GetMapping("/getPayResult")
    public Map<String, String> getPayResult(@RequestParam String orderId){
        return wxPayService.getPayResult(orderId);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 给微信支付调用的通知地址, 获取订单的支付结果
     * @param request
     * @return
     */
    @RequestMapping("/notify/callback")
    public String notifyCallback(HttpServletRequest request){
        try {
//            //获取微信传递过来的数据流
//            ServletInputStream is = request.getInputStream();
//            //定义输出流(读取流)
//            ByteArrayOutputStream os = new ByteArrayOutputStream();
//            //定义缓冲区
//            byte[] buffer = new byte[1024];
//            //定义读取长度
//            int len = 0;
//            while ( (len=is.read(buffer)) != -1 ){
//                os.write(buffer,0,len);
//            }
//            //将流中的字节码取出来
//            String resultXml = new String(os.toByteArray());
            String temp = "{\"transaction_id\":\"4200001658202211163757804266\",\"nonce_str\":\"7763ed4b068643fd98e8b6567492ea0d\",\"bank_type\":\"OTHERS\",\"openid\":\"oHwsHuAwE0-vmrTkuHEpn6fECYlA\",\"sign\":\"11BA876375998DF5B6EBBC8DE6D7A38F\",\"fee_type\":\"CNY\",\"mch_id\":\"1558950191\",\"cash_fee\":\"1\",\"out_trade_no\":\"bd9c8f19642a473381e684549d564323\",\"appid\":\"wx74862e0dfcf69954\",\"total_fee\":\"1\",\"trade_type\":\"NATIVE\",\"result_code\":\"SUCCESS\",\"attach\":\"{\\\"exchange\\\":\\\"pay_exchange\\\",\\\"routingKey\\\":\\\"seckill.pay.order\\\",\\\"username\\\":\\\"jack\\\"}\",\"time_end\":\"20221116162645\",\"is_subscribe\":\"N\",\"return_code\":\"SUCCESS\"}";
//            //将xml数据转换成map数据
//            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
            Map<String, String> resultMap = JSONObject.parseObject(temp,Map.class);
            //获取附加数据
            String attachJsonString = resultMap.get("attach");
            //反序列化
            Map<String, String> attach = JSONObject.parseObject(attachJsonString, Map.class);
            //把结果通知给订单微服务
            rabbitTemplate.convertAndSend(
                    attach.get("exchange"),
                    attach.get("routingKey"),
//                    JSONObject.toJSONString(resultMap));
                    temp);
            //关闭流
//            is.close();
//            os.flush();
//            os.close();
            //返回给微信结果
            Map<String, String> map = new HashMap<>();
            map.put("return_code","SUCCESS");
            map.put("return_msg","OK");
            //返回
            return WXPayUtil.mapToXml(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
