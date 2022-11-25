package com.jack.gmall.list.listener;

import com.jack.gmall.common.constant.ProductConst;
import com.jack.gmall.list.service.GoodsService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @Author :Jack
 * @CreatTime : 2022/11/11
 * @Description : 商品上下架消费监听对象
 **/
@Component
@Log4j2
public class SkuUpperOrDownListenter {

    @Autowired
    private GoodsService goodsService;

    /**
     * 商品上架消息监听
     * @param message
     * @param channel
     */
    @RabbitListener(queues = {"product_upper_queue","product_down_queue"})
    public void productUpperOrDown(Message message, Channel channel){
        //获取消息属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息内容
        String[] split = new String(message.getBody()).split(":");
        Long skuId = Long.valueOf(split[0]);
        Short status = Short.valueOf(split[1]);
        try {
            if (status.equals(ProductConst.SKU_ON_SALE)){
                //上架商品
                goodsService.addSkuIntoEs(skuId);
            }
            if (status.equals(ProductConst.SKU_CANCEL_SALE)){
                //下架商品
                goodsService.removeSkuFromEs(skuId);
            }
            //确认消息
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            try {
                if(messageProperties.getRedelivered()){
                    //第二次拒绝, 死信
                    e.printStackTrace();
                    channel.basicReject(deliveryTag,true);
                    log.error("商品上下架失败, 商品id为: " + skuId + ", 状态为: "+status);
                }else {
                    //第一次拒绝, 重新放回队列
                    channel.basicReject(deliveryTag,false);
                }
            } catch (Exception ex) {
                log.error("商品上下架拒绝消息失败,失败的商品id为: "+skuId+", 状态为: "+status+", 失败的原因为: "+ex.getMessage());
            }
        }
    }

//    /**
//     * 商品下架消息监听
//     * @param message
//     * @param channel
//     */
//    @RabbitListener(queues = "product_down_queue")
//    public void productDown(Message message, Channel channel) {
//        //获取消息属性
//        MessageProperties messageProperties = message.getMessageProperties();
//        //获取消息编号
//        Long deliveryTag = messageProperties.getDeliveryTag();
//        //获取消息内容
//        Long skuId = Long.valueOf(new String(message.getBody()));
//        try {
//            //下架商品
//            goodsService.removeSkuFromEs(skuId);
//            //确认消息
//            channel.basicAck(deliveryTag, false);
//        } catch (Exception e) {
//            try {
//                if (messageProperties.getRedelivered()) {
//                    //第二次拒绝, 死信
//                    e.printStackTrace();
//                    channel.basicReject(deliveryTag, true);
//                    log.error("商品下架失败, 商品id为: " + skuId);
//                } else {
//                    //第一次拒绝, 重新放回队列
//                    channel.basicReject(deliveryTag, false);
//                }
//            } catch (Exception ex) {
//                log.error("商品下架拒绝消息失败,失败的商品id为: " + skuId + ", 失败的原因为: " + ex.getMessage());
//            }
//        }
//    }
}
