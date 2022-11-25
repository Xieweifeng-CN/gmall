package com.jack.gmall.cart.controller;

import com.jack.gmall.cart.service.CartInfoService;
import com.jack.gmall.common.result.Result;
import com.jack.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/8
 * @Description : 购物车控制类
 **/
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartInfoService cartInfoService;

    /**
     * 新增购物车
     *
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/add")
    public Result add(Long skuId, Integer num) {
        cartInfoService.addCart(skuId, num);
        return Result.ok();
    }

    /**
     * 获取购物车列表
     * @return
     */
    @GetMapping("/list")
    public Result list(){
        return Result.ok(cartInfoService.getCartInfo());
    }

    /**
     * 删除特定购物车
     * @return
     */
    @GetMapping("/remove")
    public Result remove(Long id){
        cartInfoService.deleteCartInfo(id);
        return Result.ok();
    }

    /**
     * 修改购物车
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/update")
    public Result update(Long skuId, Integer num){
        cartInfoService.updateCartInfo(skuId,num);
        return Result.ok();
    }

    /**
     * 修改购物车商品为: 选中状态
     * @param id
     * @return
     */
    @GetMapping("/onCheck")
    public Result onCheck(Long id){
        cartInfoService.checkStatus(id, (short) 1);
        return Result.ok();
    }

    /**
     * 修改购物车商品为: 未选中状态
     * @param id
     * @return
     */
    @GetMapping("/unCheck")
    public Result unCheck(Long id){
        cartInfoService.checkStatus(id, (short) 0);
        return Result.ok();
    }

    /**
     * 合并购物车
     * @return
     */
    @PostMapping("/mergeCart")
    public Result mergeCart(@RequestBody List<CartInfo> cartInfoList){
        cartInfoService.mergeCart(cartInfoList);
        return Result.ok();
    }

    /**
     * 查询用户本次购买的购物车商品
     * @return
     */
    @GetMapping("/getOrderConfirmCart")
    public Result getOrderConfirmCart(){
        return Result.ok(cartInfoService.getOrderConfirmCart());
    }
}
