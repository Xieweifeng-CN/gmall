package com.jack.gmall.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.util.concurrent.AtomicDouble;
import com.jack.gmall.cart.mapper.CartInfoMapper;
import com.jack.gmall.cart.service.CartInfoService;
import com.jack.gmall.cart.util.CartThreadLocalUtil;
import com.jack.gmall.common.constant.CartConst;
import com.jack.gmall.model.base.BaseEntity;
import com.jack.gmall.model.cart.CartInfo;
import com.jack.gmall.model.product.SkuInfo;
import com.jack.gmall.product.feign.ProductFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/8
 * @Description : 购物车相关操作接口实现类
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class CartInfoServiceImpl implements CartInfoService {

    @Resource
    private CartInfoMapper cartInfoMapper;

    @Autowired
    private ProductFeign productFeign;

    /**
     * 添加购物车
     *
     * @param skuId
     * @param num
     */
    @Override
    public void addCart( Long skuId, Integer num) {
        //校验参数
        if (skuId == null || num == null){
            throw new RuntimeException("购物车参数错误!");
        }
        //查询商品是否存在
        SkuInfo skuInfo = productFeign.getSkuInfo(skuId);
        if (skuInfo == null || skuInfo.getId() == null){
            throw new RuntimeException("商品不存在!");
        }
        //判断添加的商品购物车是否存在
        CartInfo cartInfo = cartInfoMapper.selectOne(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, CartThreadLocalUtil.get())
                        .eq(CartInfo::getSkuId, skuId));

        if (cartInfo == null){
            //新增
            if (num <= 0){
                return;
            }
            //初始化购物车对象
            cartInfo = new CartInfo();
            //加入购物车属性
            cartInfo.setUserId(CartThreadLocalUtil.get());
            cartInfo.setSkuId(skuId);
            //获取实时价格
            BigDecimal price = productFeign.getSkuPrice(skuId);
            cartInfo.setCartPrice(price);
            cartInfo.setSkuNum(num);
            //根据skuId获取商品信息
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            //插入购物车数据
            int insert = cartInfoMapper.insert(cartInfo);
            //返回结果判断
            if (insert <= 0){
                throw new RuntimeException("购物车新增失败!");
            }
        }else {
            num = cartInfo.getSkuNum()+num;
            if (num <= 0){
                //删除, 总数小于0
                int i = cartInfoMapper.deleteById(cartInfo.getId());
                if (i < 0){
                    throw new RuntimeException("购物车修改失败!");
                }
            }else {
                //修改
                cartInfo.setSkuNum(num);
                int i = cartInfoMapper.updateById(cartInfo);
                if (i < 0){
                    throw new RuntimeException("购物车修改失败!");
                }
            }
        }
    }

    /**
     * 获取所有购物车信息
     * @return
     */
    @Override
    public List<CartInfo> getCartInfo() {
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, CartThreadLocalUtil.get()));
        return cartInfoList;
    }

    /**
     * 删除购物车信息
     *
     * @param id
     */
    @Override
    public void deleteCartInfo(Long id) {
        int i = cartInfoMapper.delete(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(BaseEntity::getId,id)
                        .eq(CartInfo::getUserId,CartThreadLocalUtil.get()));
        if (i < 0){
            throw new RuntimeException("删除购物车失败!");
        }
    }

    /**
     * 修改购物车商品数量
     *
     * @param skuId
     * @param num
     */
    @Override
    public void updateCartInfo(Long skuId, Integer num) {
        if (skuId == null || num == null){
            throw new RuntimeException("购物车参数错误!");
        }
        if (num <= 0){
            //删除
            int delete = cartInfoMapper.delete(
                    new LambdaQueryWrapper<CartInfo>()
                            .eq(CartInfo::getSkuId, skuId)
                            .eq(CartInfo::getUserId, CartThreadLocalUtil.get()));
            if (delete < 0){
                throw new RuntimeException("购物车删除失败!");
            }
        }else {
            //修改
            int i = cartInfoMapper.updateNum(CartThreadLocalUtil.get(), skuId,num);
            if (i < 0){
                throw new RuntimeException("购物车删除失败");
            }
        }
    }

    /**
     * 修改购物车选择状态
     *
     * @param id
     * @param status
     */
    @Override
    public void checkStatus(Long id, Short status) {
        int i = 0;
        //判断是单个操作还是多个操作
        if (id == null){
            //多个操作
            i = cartInfoMapper.updateCheckAll(CartThreadLocalUtil.get(), status);
        }else {
            //单个操作
            i = cartInfoMapper.updateCheck(CartThreadLocalUtil.get(), id, status);
        }
        if (i < 0){
            throw new RuntimeException("选中状态修改失败");
        }
    }

    /**
     * 合并购物车
     *
     * @param cartInfoList
     */
    @Override
    public void mergeCart(List<CartInfo> cartInfoList) {
        //批量添加
        cartInfoList.stream().forEach(cartInfo->{
            addCart(cartInfo.getSkuId(), cartInfo.getSkuNum());
        });
    }

    /**
     * 查询本次提交的购物车商品
     *
     * @return
     */
    @Override
    public Map<String, Object> getOrderConfirmCart() {
        //选中购物车对象集合初始化
        Map<String,Object> result = new HashMap<>();
        //查询
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, CartThreadLocalUtil.get())
                        .eq(CartInfo::getIsChecked, CartConst.CART_ON_CHECK));
        if (cartInfoList == null || cartInfoList.isEmpty()){
            throw new RuntimeException("用户没有选中购买商品!");
        }
        //商品总数量初始化
        AtomicInteger totalNum = new AtomicInteger(0);
        //商品总价格初始化
        AtomicDouble price = new AtomicDouble(0);
        //遍历每一个购物车商品
        List<CartInfo> cartInfoListNew = cartInfoList.stream().map(cartInfo -> {
            //商品购买数量
            Integer skuNum = cartInfo.getSkuNum();
            totalNum.getAndAdd(skuNum);
            //商品实时价格
            BigDecimal skuPrice = productFeign.getSkuPrice(cartInfo.getSkuId());
            cartInfo.setSkuPrice(skuPrice);
            price.getAndAdd(skuPrice.doubleValue() * skuNum);
            return cartInfo;
        }).collect(Collectors.toList());
        //保存数据
        result.put("totalNum",totalNum);
        result.put("price",price);
        result.put("cartInfoList",cartInfoListNew);
        //返回
        return result;
    }

    /**
     * 删除选中的购物车
     */
    @Override
    public void deleteCart() {
        int delete = cartInfoMapper.delete(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, CartThreadLocalUtil.get())
                        .eq(CartInfo::getIsChecked, CartConst.CART_ON_CHECK));
        if (delete < 0){
            throw new RuntimeException("购物车删除失败!");
        }
    }
}
