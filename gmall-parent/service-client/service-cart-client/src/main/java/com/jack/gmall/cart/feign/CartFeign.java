package com.jack.gmall.cart.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * 购物车远程调用feign接口
 */
@FeignClient(name = "service-cart",path = "/api/cart",contextId = "cartFeign")
public interface CartFeign {

    /**
     * 查询用户本次购买的购物车商品
     * @return
     */
    @GetMapping("/getOrderAddInfo")
    public Map<String, Object> getOrderAddInfo();

    /**
     * 删除选中的购物车
     */
    @GetMapping("/deleteCart")
    public void deleteCart();
}
