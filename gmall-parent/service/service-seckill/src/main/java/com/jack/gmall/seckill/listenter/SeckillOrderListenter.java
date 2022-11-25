package com.jack.gmall.seckill.listenter;

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
 * @CreatTime : 2022/11/14
 * @Description : 监听秒杀商品下单的消费者
 **/
@Component
@Log4j2
public class SeckillOrderListenter {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 秒杀商品下单消息监听
     * @param message
     * @param channel
     */
    @RabbitListener(queues = "seckill_order_queue")
    public void seckillOrderAdd(Message message, Channel channel) {
        //获取消息属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息内容
        String s = new String(message.getBody());
        try {
            //秒杀商品下单
            seckillOrderService.listenerAddSeckillOrder(s);
            //确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                //直接拒绝, 只尝试一次
                channel.basicReject(deliveryTag,false);
            } catch (Exception ex) {
                log.error("秒杀下单失败,失败的信息为: " + s + ", 失败的原因为: " + ex.getMessage());
            }
        }
    }

}
