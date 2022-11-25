package com.jack.gmall.seckill.listenter;

import com.jack.gmall.seckill.service.SeckillGoodsService;
import com.jack.gmall.seckill.service.SeckillOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/15
 * @Description : 秒杀订单超时消息监听的消费者
 **/
@Component
@Log4j2
public class SeckillOrderTimeoutListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 秒杀商品订单超时未支付的消息监听
     * @param message
     * @param channel
     */
    @RabbitListener(queues = "seckill_order_normal_queue")
    public void seckillOrderTimeout(Message message, Channel channel) {
        //获取消息属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息内容
        String username = new String(message.getBody());
        try {
            //订单超时取消,更改订单状态
            seckillOrderService.cancelSeckillOrder(username);
            //确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                //手动拒绝
                if(messageProperties.getRedelivered()){
                    //第二次拒绝
                    channel.basicReject(deliveryTag,false);
                }else {
                    //第一次拒绝
                    channel.basicReject(deliveryTag,true);
                }
            } catch (Exception ex) {
                log.error("商取消超时秒杀订单失败,订单失败的用户为: " + username + ", 失败的原因为: " + ex.getMessage());
            }
        }
    }

}
