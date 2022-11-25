package com.jack.gmall.seckill.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jack.gmall.model.activity.SeckillGoods;
import com.jack.gmall.seckill.mapper.SeckillOrderMapper;
import com.jack.gmall.seckill.pojo.SeckillOrder;
import com.jack.gmall.seckill.pojo.UserRecode;
import com.jack.gmall.seckill.service.SeckillOrderService;
import com.jack.gmall.seckill.util.DateUtil;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/14
 * @Description :
 **/
@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 秒杀下单 : 排队
     *
     * @param time
     * @param goodsId
     * @param num
     * @return
     */
    @Override
    public UserRecode addSeckillGoods(String time, String goodsId, Integer num) {
        String username = "jack";
        UserRecode userRecode = new UserRecode();
        //记录用户的排队次数, 每个用户只进一次秒杀排队的机会, 除非: ①主动取消/超时取消 ②支付 ③秒杀失败
        Long increment = redisTemplate.opsForValue().increment("User_Queue_Count_" + username, 1);
        if (increment > 1) {
            userRecode.setStatus(3);
            userRecode.setMsg("秒杀失败,请勿重复提交!");
            return userRecode;
        }
        //记录用户商品秒杀信息
        userRecode.setUsername(username);
        userRecode.setCreateTime(new Date());
        userRecode.setStatus(1);
        userRecode.setGoodsId(goodsId);
        userRecode.setTime(time);
        userRecode.setNum(num);
        userRecode.setMsg("排队中!");
        //异步编排
        CompletableFuture.runAsync(() -> {
            //将用户的排队状态信息保存到redis中
            redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
            //下单需要保证相对公平MQ
            rabbitTemplate.convertAndSend(
                    "seckill_order_exchange",
                    "seckill_order_add",
                    JSONObject.toJSONString(userRecode));
        }, threadPoolExecutor).whenCompleteAsync((a, b) -> {
            if (b != null) {
                //更新用户的状态: 失败
                userRecode.setStatus(3);
                userRecode.setMsg("秒杀失败,请重试!");
                redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
                //保存用户排队或下单出现异常, 需要将用户的排队标识位清理,避免用户无法再次下单
                redisTemplate.delete("User_Queue_Count_" + username);
            }
        });
        //返回给用户
        return userRecode;
    }

    /**
     * 查询秒杀排队状态
     *
     * @return
     */
    @Override
    public UserRecode getUserRecode() {
        String username = "jack";
        return (UserRecode) redisTemplate.opsForValue().get("User_Recode_" + username);
    }

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    /**
     * 秒杀真实下单方法
     *
     * @param userRecordString
     */
    @Override
    public void listenerAddSeckillOrder(String userRecordString) {
        //反序列化
        UserRecode userRecode = JSONObject.parseObject(userRecordString, UserRecode.class);
        //获取时间段
        String time = userRecode.getTime();
        //获取用户名
        String username = userRecode.getUsername();
        //获取商品id
        String goodsId = userRecode.getGoodsId();
        //获取购买数量
        Integer num = userRecode.getNum();
        //判断时间段是否满足活动要求
        String nowTime =
                DateUtil.data2str(DateUtil.getDateMenus().get(0), DateUtil.PATTERN_YYYYMMDDHH);
        if (!time.equals(nowTime)) {
            //用户购买的商品还没有开始或已经结束,秒杀失败
            //更新用户的状态
            userRecode.setStatus(3);
            userRecode.setMsg("秒杀失败! 用户购买的商品还没有开始或已经结束");
            redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
            //保存用户排队或下单出现异常, 需要将用户的排队标识位清理,避免用户无法再次下单
            redisTemplate.delete("User_Queue_Count_" + username);
            return;
        }
        //获取缓存中的商品对象
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.opsForHash().get(time, goodsId);
        if (seckillGoods == null) {
            //商品不存在
            userRecode.setStatus(3);
            userRecode.setMsg("秒杀失败! 商品不存在");
            redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
            //保存用户排队或下单出现异常, 需要将用户的排队标识位清理,避免用户无法再次下单
            redisTemplate.delete("User_Queue_Count_" + username);
            return;
        }
        //获取商品的限购
        Integer seckillLimit = seckillGoods.getSeckillLimit();
        if (seckillLimit < num) {
            //超出限购
            userRecode.setStatus(3);
            userRecode.setMsg("秒杀失败! 超出商品的限购, 每个商品限购: " + seckillLimit + "件");
            redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
            //保存用户排队或下单出现异常, 需要将用户的排队标识位清理,避免用户无法再次下单
            redisTemplate.delete("User_Queue_Count_" + username);
            return;
        }
        //库存是否足够
        //库存问题一.解决超卖问题
        //方案一
        for (Integer i = 0; i < num; i++) {
            Object o = redisTemplate.opsForList().rightPop("Seckill_Goods_Stock_Queue_" + goodsId);
            if (o == null){
                //判断是否需要回滚
                if (i > 0){
                    //构建好商品库存剩余个数的数组,并且数组的每一个位置都存储一个商品id
                    String[] ids = getIds(num, goodsId);
                    //回滚
                    redisTemplate.opsForList().leftPushAll("Seckill_Goods_Stock_Queue_" + goodsId);
                }
                //库存不足
                userRecode.setStatus(3);
                userRecode.setMsg("秒杀失败, 库存不足!");
                redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
                //保存用户排队或下单出现异常, 需要将用户的排队标识位清理,避免用户无法再次下单
                redisTemplate.delete("User_Queue_Count_" + username);
                return;
            }
        }
        //库存问题二.库存减了以后,商品数据没有更新,库存足够
        // 做库存自增
        Long increment =
                redisTemplate.opsForHash().increment("Seckill_Goods_Increment_" + time, goodsId, -num);
        //库存问题三.用户查询的商品库存是有可能错的
        //修改为最新库存,重新保存redis
        seckillGoods.setStockCount(increment.intValue());
        redisTemplate.opsForHash().put(time,goodsId,seckillGoods);

        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        seckillOrder.setGoodsId(goodsId);
        seckillOrder.setNum(num);
        seckillOrder.setMoney(seckillGoods.getPrice().multiply(new BigDecimal(num)).doubleValue() + "");
        seckillOrder.setUserId(username);
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");
        //将数据写入redis
        redisTemplate.opsForHash().put("Seckill_Order_" + time, seckillOrder.getId(), seckillOrder);
        //修改排队状态
        userRecode.setStatus(2);
        userRecode.setMsg("秒杀等待支付");
        userRecode.setMoney(seckillOrder.getMoney());
        userRecode.setOrderId(seckillOrder.getId());
        //更新
        redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
        //发送延迟消息, 15分钟倒计时
        rabbitTemplate.convertAndSend(
                "seckill_order_normal_exchange",
                "seckill.order.dead",
                username,
                (message -> {
                    //获取消息属性
                    MessageProperties messageProperties = message.getMessageProperties();
                    //设置消息延迟时间
                    messageProperties.setExpiration("900000");
                    return message;
                }));
        //TODO--订单的后续处理: 付钱(通用化改造)
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

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 取消订单: 1.主动取消 2.超时取消
     *
     * @param username
     */
    @Override
    public void cancelSeckillOrder(String username) {
        //状态初始化
        String msg = "超时取消";
        if (StringUtils.isEmpty(username)){
            //主动取消
            username = "jack";
            msg = "主动取消";
        }
        //控制取消次数,只能有一次
        RLock lock = redissonClient.getLock("Cancel_Seckill_Order_Lock_" + username);
        //抢锁
        if (lock.tryLock()){
            try {
                //从redis中获取用户的排队状态
                UserRecode userRecode =
                        (UserRecode)redisTemplate.opsForValue().get("User_Recode_" + username);
                if (userRecode != null){
                    //获取时间段
                    String time = userRecode.getTime();
                    //获取订单号
                    String orderId = userRecode.getOrderId();
                    //从redis获取订单信息
                    SeckillOrder seckillOrder =
                            (SeckillOrder)redisTemplate.opsForHash().get("Seckill_Order_" + time, orderId);
                    //取消订单,将订单的状态修改为: 取消: a b
                    seckillOrder.setStatus(msg);
                    //将数据写入数据库
                    int insert = seckillOrderMapper.insert(seckillOrder);
                    if (insert <= 0){
                        throw new RuntimeException(msg + "订单保存失败");
                    }
                    //回滚库存
                    rollbackSeckillGoodsStock(time,userRecode.getGoodsId(),userRecode.getNum());
                    //清理redis中的用户产生的所有key
                    clearRedisUserFlag(username,orderId,time);
                }
            } catch (Exception e) {
                log.error("订单取消失败,原因: "+ e.getMessage());
            } finally {
                //解锁
                lock.unlock();
            }
        }else {
            //重复提交
        }
    }

    /**
     * 回滚缓存中的商品库存
     * @param time
     * @param goodsId
     * @param num
     */
    private void rollbackSeckillGoodsStock(String time,String goodsId,Integer num) {
        //从redis中获取商品的数据
        SeckillGoods seckillGoods = (SeckillGoods)redisTemplate.opsForHash().get(time, goodsId);
        //商品库存自增值
        Long increment =
                redisTemplate.opsForHash().increment("Seckill_Goods_Increment_" + time, goodsId, num);
        if (seckillGoods != null){
            //商品活动没有结束: 回滚商品数据
            seckillGoods.setStockCount(increment.intValue());
            redisTemplate.opsForHash().put(time,goodsId,seckillGoods);
            //商品队列
            String[] ids = getIds(num, goodsId);
            redisTemplate.opsForList().leftPushAll("Seckill_Goods_Stock_Queue_" + goodsId,ids);
        }
    }

    /**
     * 清理redis中的用户产生的所有key
     * @param username
     * @param orderId
     * @param time
     */
    private void clearRedisUserFlag(String username,String orderId,String time) {
        //排队计数器
        redisTemplate.delete("User_Queue_Count_" + username);
        //排队状态
        redisTemplate.delete("User_Recode_" + username);
        //订单信息
        redisTemplate.opsForHash().delete("Seckill_Order_" + time, orderId);
    }

    /**
     * 修改支付结果
     *
     * @param payResultJsonString
     */
    @Override
    public void updateSeckillOrder(String payResultJsonString) {
        //获取支付结果反序列化
        Map<String, String> payResultMap =
                JSONObject.parseObject(payResultJsonString, Map.class);
        //获取附加参数
        String attachString = payResultMap.get("attach");
        Map<String, String> attach =
                JSONObject.parseObject(attachString, Map.class);
        //获取用户名
        String username = attach.get("username");
        //从redis中获取用户的排队状态
        UserRecode userRecode =
                (UserRecode)redisTemplate.opsForValue().get("User_Recode_" + username);
        //防止重复操作,订单若已经被处理了,redis中不会有订单信息
        if (userRecode != null){
            //获取时间段
            String time = userRecode.getTime();
            //获取订单号
            String orderId = userRecode.getOrderId();
            //从redis中获取订单信息
            SeckillOrder seckillOrder =
                    (SeckillOrder) redisTemplate.opsForHash().get("Seckill_Order_" + time, orderId);
            if (seckillOrder != null){
                //将订单的信息进行修改
                seckillOrder.setPayTime(new Date());
                seckillOrder.setStatus("已支付");
                seckillOrder.setOutTradeNo(payResultMap.get("transaction_id"));
                int insert = seckillOrderMapper.insert(seckillOrder);
                if (insert <= 0){
                    throw new RuntimeException("修改订单的支付结果失败!");
                }
                //清理标识位
                clearRedisUserFlag(username,orderId,time);
            }
        }else {
            //获取支付结果的订单号
            String orderId = payResultMap.get("out_trade_no");
            //订单在数据库, 查询数据库中的订单信息
            SeckillOrder seckillOrder = seckillOrderMapper.selectById(orderId);
            //数据库中订单已经支付,但是是两个流水号
            if (seckillOrder.getStatus().equals("已支付")){
                if (!seckillOrder.getOutTradeNo().equals(payResultMap.get("transaction_id"))){
                    //重复支付,退款
                }
            }else {
                //取消: a.主动 b.超时---->退款
            }

        }
    }

}