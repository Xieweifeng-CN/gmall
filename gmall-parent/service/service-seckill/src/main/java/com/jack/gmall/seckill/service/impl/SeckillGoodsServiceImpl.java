package com.jack.gmall.seckill.service.impl;

import com.jack.gmall.model.activity.SeckillGoods;
import com.jack.gmall.seckill.mapper.SecKillGoodsMapper;
import com.jack.gmall.seckill.service.SeckillGoodsService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/14
 * @Description :
 **/
@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询指定时间段商品
     *
     * @param time
     * @return
     */
    @Override
    public List<SeckillGoods> getSeckillGoods(String time) {
        return redisTemplate.opsForHash().values(time);
    }

    /**
     * 查询指定时间段的指定商品
     *
     * @param time
     * @param orderId
     * @return
     */
    @Override
    public SeckillGoods getSeckillGood(String time, String orderId) {
        return (SeckillGoods) redisTemplate.opsForHash().get(time, orderId);
    }

    @Resource
    private SecKillGoodsMapper secKillGoodsMapper;

    /**
     * 同步指定时间段的商品剩余库存到数据库中
     *
     * @param time
     */
    @Override
    public void updateSeckillGoodsStock(String time) {
        //从redis中获取该时间段的全部商品的id数据
        Set keys = redisTemplate.opsForHash().keys("Seckill_Goods_Increment_" + time);
        if (keys != null && keys.size() > 0) {
            //遍历每一个待同步商品
            keys.stream().forEach(goodsId -> {
                //获取redis中的剩余库存
                Integer stockCount =
                        (Integer) redisTemplate.opsForHash().get("Seckill_Goods_Increment_" + time, goodsId);
                //同步数据到数据库
                int i = secKillGoodsMapper.updateSeckillGoodsStock((String) goodsId, stockCount);
                if (i < 0){
                    //日志使用:
                    //使用定时任务扫描同步失败的日志,
                    //当重复15-20次都没成功, 就发送短信/邮件通知人工处理
                    log.error("商品库存同步失败,所属的时间段为:" + time + ", 失败的商品id为: " + goodsId + ", 应该同步的剩余库存为: " + stockCount);
                }
                //删除同步完成后的库存标识位
                redisTemplate.opsForHash().delete(goodsId);
            });
        }
    }
}
