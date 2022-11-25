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
 * @Description : 订单取消队列监听类
 **/
@Component
@Log4j2
public class OrderMQListener {

    @Autowired
    private OrderInfoService orderInfoService;

    @RabbitListener(queues = {"order_normal_queue"})
    public void orderCancel(Message message, Channel channel){
        //获取消息属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息内容
        Long orderId = Long.valueOf(new String(message.getBody()));
        try {
            //超时取消订单
            orderInfoService.cancelOrder(orderId);
            //消息手动确认
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            try {
                if (messageProperties.getRedelivered()){
                    //第二次拒绝, 消息转为死信
                    e.printStackTrace();
                    channel.basicReject(deliveryTag,true);
                    log.error("订单取消失败, 失败订单id为: "+orderId);
                }else {
                    //第一次拒绝, 重新回到队列
                    channel.basicReject(deliveryTag,false);
                }
            } catch (IOException ex) {
                log.error("订单取消拒绝确认失败, 失败订单id为: "+orderId+", 失败原因为: "+e.getMessage());
            }
        }
    }

}
