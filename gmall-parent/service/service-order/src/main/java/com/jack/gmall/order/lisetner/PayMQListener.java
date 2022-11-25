package com.jack.gmall.order.lisetner;

import com.jack.gmall.order.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/11
 * @Description : 支付结果通知队列监听类
 **/
@Component
@Log4j2
public class PayMQListener {

    @Autowired
    private OrderInfoService orderInfoService;

    @RabbitListener(queues = {"order_pay_queue"})
    public void orderCancel(Message message, Channel channel){
        //获取消息属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息内容
        String result = new String(message.getBody());
        try {
            //修改订单的状态为: 已支付
            orderInfoService.updateOrder(result);
            //消息手动确认
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            try {
                if (messageProperties.getRedelivered()){
                    //第二次拒绝, 消息转为死信
                    e.printStackTrace();
                    channel.basicReject(deliveryTag,true);
                    log.error("支付结果通知失败, 失败内容为: "+result);
                }else {
                    //第一次拒绝, 重新回到队列
                    channel.basicReject(deliveryTag,false);
                }
            } catch (IOException ex) {
                log.error("支付结果通知拒绝确认失败, 失败内容为: "+result+", 失败原因为: "+e.getMessage());
            }
        }
    }

}
