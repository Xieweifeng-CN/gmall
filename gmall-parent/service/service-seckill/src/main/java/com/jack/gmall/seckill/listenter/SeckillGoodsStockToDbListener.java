package com.jack.gmall.seckill.listenter;

import com.jack.gmall.seckill.mapper.SecKillGoodsMapper;
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
 * @Description : 商品库存数据同步延迟队列
 **/
@Component
@Log4j2
public class SeckillGoodsStockToDbListener {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    /**
     * 秒杀商品库存同步消息监听
     * @param message
     * @param channel
     */
    @RabbitListener(queues = "seckill_goods_normal_queue")
    public void seckillGoodsStockToDb(Message message, Channel channel) {
        //获取消息属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息内容
        String time = new String(message.getBody());
        try {
            //秒杀商品活动结束, 库存数据同步
            seckillGoodsService.updateSeckillGoodsStock(time);
            //确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                //手动拒绝
                if(messageProperties.getRedelivered()){
                    log.error("商品库存数据同步失败,失败的时间段为: " + time + ", 失败的原因为: " + e.getMessage());
                    //第二次拒绝
                    channel.basicReject(deliveryTag,false);
                }else {
                    //第一次拒绝
                    channel.basicReject(deliveryTag,true);
                }
            } catch (Exception ex) {
                log.error("商品库存数据同步失败,失败的时间段为: " + time + ", 失败的原因为: " + ex.getMessage());
            }
        }
    }

}
