package com.jack.gmall.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jack.gmall.cart.feign.CartFeign;
import com.jack.gmall.model.base.BaseEntity;
import com.jack.gmall.model.cart.CartInfo;
import com.jack.gmall.model.enums.OrderStatus;
import com.jack.gmall.model.enums.ProcessStatus;
import com.jack.gmall.model.order.OrderDetail;
import com.jack.gmall.model.order.OrderInfo;
import com.jack.gmall.order.mapper.OrderDetailMapper;
import com.jack.gmall.order.mapper.OrderInfoMapper;
import com.jack.gmall.order.service.OrderInfoService;
import com.jack.gmall.order.util.OrderThreadLocalUtil;
import com.jack.gmall.product.feign.ProductFeign;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * @Author :Jack
 * @CreatTime : 2022/11/9
 * @Description : 订单相关业务接口实现类
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderInfoServiceImpl implements OrderInfoService {

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProductFeign productFeign;

    @Autowired
    private CartFeign cartFeign;

    /**
     * 添加订单信息
     */
    @Override
    public void addOrderInfo(OrderInfo orderInfo) {
        //参数校验
        if (orderInfo == null) {
            return;
        }
        //使用redis进行记录, 相当于加锁
        Long increment =
                redisTemplate.opsForValue().increment("User_Order_Add_Count_" + OrderThreadLocalUtil.get(), 1);
        if (increment > 1) {
            throw new RuntimeException("重复提交, 订单新增失败!");
        }
        try {
            //设置锁的期限, 避免死锁
            redisTemplate.expire("User_Order_Add_Count_" + OrderThreadLocalUtil.get(), 10, TimeUnit.SECONDS);
            //补全订单
            //远程调用购物车微服务,获取订单总价格和商品总数
            Map<String, Object> cartInfoResult = cartFeign.getOrderAddInfo();
            if (cartInfoResult == null || cartInfoResult.isEmpty()) {
                throw new RuntimeException("订单新增失败,购物车未勾选商品!");
            }
            orderInfo.setTotalAmount(new BigDecimal(cartInfoResult.get("price").toString()));
            orderInfo.setOrderStatus(OrderStatus.UNPAID.getComment());
            orderInfo.setUserId("jack");
            orderInfo.setCreateTime(new Date());
            orderInfo.setExpireTime(new Date(System.currentTimeMillis() + 30 * 60 * 1000));
            orderInfo.setProcessStatus(OrderStatus.UNPAID.getComment());
            //将订单保存到数据库中
            int insert = orderInfoMapper.insert(orderInfo);
            if (insert <= 0) {
                throw new RuntimeException("订单新增失败,请重试!");
            }
            //获取订单id
            Long orderId = orderInfo.getId();
            //根据购物车数据包装订单详情, (泛型擦除)
            List cartInfoList = (List) cartInfoResult.get("cartInfoList");
            //保存订单详情, 返回商品扣减数量集合
            Map<String, String> deCountMap = addOrderDetail(orderId, cartInfoList);
            //清空购物车
//            cartFeign.deleteCart();
            //扣减库存
            productFeign.deCountCartStock(deCountMap);
            //对订单进行30分钟计时, 延迟队列
            rabbitTemplate.convertAndSend(
                    "order_normal_exchange",
                    "order.normal",
                    orderId + "",
                    (message -> {
                        //获取消息的属性
                        MessageProperties messageProperties = message.getMessageProperties();
                        //设置消息的过期时间
                        messageProperties.setExpiration("20000");
                        //返回
                        return message;
                    }));
        } catch (Exception e) {
            //spring的事务回滚需要有异常才会执行
            throw new RuntimeException("订单新增失败, 失败原因: " + e.getMessage());
        } finally {
            //删除锁
            redisTemplate.delete("User_Order_Add_Count_" + OrderThreadLocalUtil.get());
        }
    }

    /**
     * 保存订单详情
     *
     * @param orderId
     * @param cartInfoList
     * @return
     */
    private Map<String, String> addOrderDetail(Long orderId, List cartInfoList) {
        OrderDetail orderDetail = new OrderDetail();
        //库存记录map集合
        Map<String, String> deCountMap = new ConcurrentHashMap<>();
        cartInfoList.stream().forEach(o -> {
            //序列化
            String s = JSONObject.toJSONString(o);
            //反序列化
            CartInfo cartInfo = JSONObject.parseObject(s, CartInfo.class);
            //初始化订单详情
            orderDetail.setOrderId(orderId);
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            //将订单详情保存到数据库中
            int i = orderDetailMapper.insert(orderDetail);
            if (i <= 0) {
                throw new RuntimeException("订单详情保存失败!");
            }
            //每一个订单详情存储一次 skuId:扣减数量
            deCountMap.put(cartInfo.getSkuId() + "", cartInfo.getSkuNum() + "");
        });
        return deCountMap;
    }

    /**
     * 取消订单
     *
     * @param orderId
     */
    @Override
    public void cancelOrder(Long orderId) {
        if (orderId == null) {
            return;
        }
        Long increment =
                redisTemplate.opsForValue().increment("User_Order_Cancel_Count_" + orderId, 1);
        if (increment > 1) {
            throw new RuntimeException("取消订单失败,重复取消!");
        }
        try {
            //设置锁的过期时间
            redisTemplate.expire("User_Order_Cancel_Count_" + orderId, 10, TimeUnit.SECONDS);
            //取消订单通用查询条件
            LambdaQueryWrapper<OrderInfo> wrapper =
                    new LambdaQueryWrapper<OrderInfo>()
                            .eq(BaseEntity::getId, orderId)
                            .eq(OrderInfo::getOrderStatus, OrderStatus.UNPAID.getComment());
            String username = OrderThreadLocalUtil.get();
            //true为超时取消, false为主动取消
            boolean flag = true;
            if (!StringUtils.isEmpty(username)) {
                //需要有用户名, 避免错取消
                flag = false;
                wrapper.eq(OrderInfo::getUserId, username);
            }
            //获取订单对象
            OrderInfo orderInfo = orderInfoMapper.selectOne(wrapper);
            if (orderInfo == null || orderInfo.getId() == null) {
                //不存在未支付的订单
                return;
            }
            if (flag) {
                //超时取消
                //设置修改属性
                orderInfo.setOrderStatus(OrderStatus.TIMEOUT.getComment());
                orderInfo.setProcessStatus(OrderStatus.TIMEOUT.getComment());
            } else {
                //主动取消,
                //设置修改属性
                orderInfo.setOrderStatus(OrderStatus.CANCEL.getComment());
                orderInfo.setProcessStatus(OrderStatus.CANCEL.getComment());
            }
            //修改订单
            int update = orderInfoMapper.update(orderInfo, wrapper);
            if (update < 0) {
                throw new RuntimeException("取消订单失败,请重试");
            }
            //库存回滚
            rollbackStock(orderId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("取消订单失败,让事务回滚!");
        } finally {
            //删除锁
            redisTemplate.delete("User_Order_Cancel_Count_" + orderId);
        }
    }

    /**
     * 回滚库存
     *
     * @param orderId
     */
    private void rollbackStock(Long orderId) {
        Map<String, String> result = new ConcurrentHashMap<>();
        //查询需要订单下需要回滚的商品
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>()
                        .eq(OrderDetail::getOrderId, orderId));
        orderDetailList.stream().forEach(orderDetail -> {
            //商品id
            Long skuId = orderDetail.getSkuId();
            //回滚数量
            Integer deCount = orderDetail.getSkuNum();
            //记录
            result.put(skuId + "", deCount + "");
        });
        //远程调用product微服务,进行库存回滚
        productFeign.rollbackStock(result);
    }

    /**
     * 修改订单状态: 已支付
     *
     * @param resultJsonString
     */
    @Override
    public void updateOrder(String resultJsonString) {
        //将微信通知返回结果反序列化
        Map<String, String> result = JSONObject.parseObject(resultJsonString, Map.class);
        //判断
        if (result.get("return_code").equals("SUCCESS") &&
                result.get("result_code").equals("SUCCESS")) {
            //获取订单号
            String orderId = result.get("out_trade_no");
            //查询这笔订单, 状态为未支付
            OrderInfo orderInfo = orderInfoMapper.selectOne(
                    new LambdaQueryWrapper<OrderInfo>()
                            .eq(BaseEntity::getId, orderId)
                            .eq(OrderInfo::getOrderStatus, OrderStatus.UNPAID.getComment()));
            if (orderInfo == null || orderInfo.getId() == null) {
                return;
            }
            //修改订单的状态
            orderInfo.setOrderStatus(OrderStatus.PAID.getComment());
            orderInfo.setProcessStatus(ProcessStatus.PAID.getComment());
            orderInfo.setOutTradeNo(result.get("transaction_id"));
            orderInfo.setTradeBody(resultJsonString);
            //修改
            int i = orderInfoMapper.updateById(orderInfo);
            if (i < 0){
                throw new RuntimeException("修改订单的支付结果失败,操作数据库失败!");
            }
            //通知商家发货
        }
    }
}
