package com.jack.gmall.item.controller;

import com.jack.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/27
 * @Description :
 **/
@RestController
@RequestMapping("/api/info")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 查询sku的详细信息,生成sku的详情页面
     * @param skuId
     * @return
     */
    @GetMapping("/getItemInfo/{skuId}")
    public Map<String, Object> getItemInfo(@PathVariable("skuId") Long skuId){
        return itemService.getItemInfo(skuId);
    }

}
