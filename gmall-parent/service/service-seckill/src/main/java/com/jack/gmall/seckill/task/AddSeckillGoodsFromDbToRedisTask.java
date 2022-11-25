package com.jack.gmall.seckill.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jack.gmall.model.activity.SeckillGoods;
import com.jack.gmall.model.base.BaseEntity;
import com.jack.gmall.seckill.mapper.SecKillGoodsMapper;
import com.jack.gmall.seckill.util.DateUtil;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/14
 * @Description : 从数据库添加秒杀商品到redis的定时任务
 **/
@Component
public class AddSeckillGoodsFromDbToRedisTask {

    @Resource
    private SecKillGoodsMapper secKillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置秒杀商品缓存定时任务
     * 秒 分 时 日 月 周 年
     * *任意时间
     * ?忽略这个时间
     * fixedDelay 上一次执行完毕后算间隔(每次只有一个任务在执行)
     * fixedRate 上一次开始执行时算间隔
     * initialDelay 只影响第一次执行时间
     */
    @Scheduled(cron = "1/20 * * * * *")
    public void addSeckillGoodsFromDbToRedis() {
        //计算当前系统时间段,以及后面4个时间段
        List<Date> dateMenus = DateUtil.getDateMenus();
        dateMenus.stream().forEach(date -> {
            //计算当前时间段的开始时间
            String startTime = DateUtil.data2str(date, DateUtil.PATTERN_YYYY_MM_DDHHMM);
            //计算当前时间段的截止时间
            String endTime = DateUtil.data2str(DateUtil.addDateHour(date, 2), DateUtil.PATTERN_YYYY_MM_DDHHMM);
            //计算当前截至时间
            Date endTimes = DateUtil.addDateHour(date, 2);
            //计算商品数据在redis中的有效时间
            long liveTime = endTimes.getTime() - System.currentTimeMillis();
            //商品缓存主键
            String key = DateUtil.data2str(date, DateUtil.PATTERN_YYYYMMDDHH);
            //拼接查询商品条件
            LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
            //商品必须是审核通过的 char "1"
            wrapper.eq(SeckillGoods::getStatus, "1");
            //商品必须在活动时间以内 大于等于startTime
            wrapper.ge(SeckillGoods::getStartTime, startTime);
            //商品必须在活动时间以内 小于endTime
            wrapper.le(SeckillGoods::getEndTime, endTime);
            //剩余库存必须大于0
            wrapper.gt(SeckillGoods::getStockCount, 0);
            //redis中没有的商品数据
            Set keys = redisTemplate.opsForHash().keys(key);
            if (keys != null || keys.size() > 0) {
                wrapper.notIn(BaseEntity::getId, keys);
            }
            //查询
            List<SeckillGoods> seckillGoods = secKillGoodsMapper.selectList(wrapper);
            //遍历将商品写入redis中
            seckillGoods.stream().forEach(seckillGood -> {
                //写入redis TODO---- 2.商品活动到期需要将剩余库存同步到数据库
                redisTemplate.opsForHash().put(key, seckillGood.getId() + "", seckillGood);
                //构建一个商品库存剩余个数长度的队列
                Integer stockCount = seckillGood.getStockCount();
                //构建好商品库存剩余个数的数组,并且数组的每一个位置都存储一个商品id
                String[] ids = getIds(stockCount, seckillGood.getId() + "");
                //下单是否能进行的依据
                redisTemplate.opsForList().leftPushAll("Seckill_Goods_Stock_Queue_" + seckillGood.getId(), ids);
                redisTemplate.expire("Seckill_Goods_Stock_Queue_" + seckillGood.getId(),liveTime,TimeUnit.MILLISECONDS);
                //构建一个商品库存的自增值: 记录商品的剩余库存!
                redisTemplate.opsForHash().increment("Seckill_Goods_Increment_" + key, seckillGood.getId() + "", stockCount);
            });
            //标识位问题一: 1.商品活动到期需要清理掉
            //设置商品相关key的过期时间
            setSeckillGoodsKeyRedisExpire(key, liveTime);
        });
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 设置商品数据过期和设置商品库存自增值数据过期
     *
     * @param key
     * @param liveTime
     */
    private void setSeckillGoodsKeyRedisExpire(String key, long liveTime) {
        //保证每个时间段的数据只设置一次过期时间
        Long increment =
                redisTemplate.opsForHash().increment("Seckill_Goods_Expire_Times", key, 1);
        if (increment > 1){
            return;
        }
        //设置商品数据过期
        redisTemplate.expire(key, liveTime, TimeUnit.MILLISECONDS);
        //设置商品库存自增值数据过期 (库存保存的时候清理)
        rabbitTemplate.convertAndSend(
                "seckill_goods_normal_exchange",
                "seckill.goods.dead",
                key,
                (message -> {
                    //获取消息属性
                    MessageProperties messageProperties = message.getMessageProperties();
                    //设置过期时间, 活动结束后半个小时 (订单倒计时15分钟,再给足够的预留)
//                    messageProperties.setExpiration((liveTime+1800000) + "");
                    messageProperties.setExpiration(20000+ "");
                    return message;
                }));
    }


    /**
     * 构建商品库存数组
     *
     * @param stockCount
     * @param goodsId
     * @return
     */
    private String[] getIds(Integer stockCount, String goodsId) {
        //声明一个库存长度的数组
        String[] ids = new String[stockCount];
        //为每个元素赋值
        for (Integer i = 0; i < stockCount; i++) {
            ids[i] = goodsId;
        }
        return ids;
    }

}
