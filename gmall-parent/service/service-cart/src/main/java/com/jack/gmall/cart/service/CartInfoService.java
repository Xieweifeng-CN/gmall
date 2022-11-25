package com.jack.gmall.cart.service;

import com.jack.gmall.model.cart.CartInfo;

import java.util.List;
import java.util.Map;

/**
 * 购物车相关操作接口
 */
public interface CartInfoService {

    /**
     * 添加购物车
     * @param skuId
     * @param num
     */
    void addCart(Long skuId,Integer num);

    /**
     * 获取所有购物车信息
     * @return
     */
    List<CartInfo> getCartInfo();

    /**
     * 删除购物车信息
     * @param id
     */
    void deleteCartInfo(Long id);

    /**
     * 修改购物车商品数量
     * @param skuId
     * @param num
     */
    void updateCartInfo(Long skuId,Integer num);

    /**
     * 修改购物车选择状态
     * @param id
     * @param status
     */
    void checkStatus(Long id,Short status);

    /**
     * 合并购物车
     * @param cartInfoList
     */
    void mergeCart(List<CartInfo> cartInfoList);

    /**
     * 查询本次提交的购物车商品
     * @return
     */
    Map<String, Object> getOrderConfirmCart();

    /**
     * 删除选中的购物车
     */
    void deleteCart();

}
