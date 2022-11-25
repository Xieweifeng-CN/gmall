package com.jack.gmall.cart.controller;

import com.jack.gmall.cart.service.CartInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/8
 * @Description : 购物车控制类
 **/
@RestController
@RequestMapping("/api/cart")
public class CartControllerFeign {

    @Autowired
    private CartInfoService cartInfoService;

    /**
     * 查询用户本次购买的购物车商品
     * @return
     */
    @GetMapping("/getOrderAddInfo")
    public Map<String, Object> getOrderAddInfo(){
        return cartInfoService.getOrderConfirmCart();
    }

    /**
     * 删除选中的购物车
     */
    @GetMapping("/deleteCart")
    public void deleteCart(){
        cartInfoService.deleteCart();
    }
}
