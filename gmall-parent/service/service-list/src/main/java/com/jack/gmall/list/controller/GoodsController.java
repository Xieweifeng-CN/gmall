package com.jack.gmall.list.controller;

import com.jack.gmall.list.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/2
 * @Description : 商品相关操作控制层
 **/
@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

//    @Autowired
//    private ElasticsearchTemplate elasticsearchTemplate;
//
//    /**
//     * 创建索引创建映射
//     * @return
//     */
//    @GetMapping("/creatIndexAndMapping")
//    public Result creatIndexAndMapping(){
//        elasticsearchTemplate.createIndex(Goods.class);
//        elasticsearchTemplate.putMapping(Goods.class);
//        return Result.ok();
//    }

    /**
     * 写入商品到es中
     * @param skuId
     * @return
     */
    @GetMapping("/add/{skuId}")
    public Boolean add(@PathVariable("skuId") Long skuId){
        goodsService.addSkuIntoEs(skuId);
        return true;
    }

    /**
     * 删除es中的商品
     * @param skuId
     * @return
     */
    @GetMapping("/remove/{skuId}")
    public Boolean remove(@PathVariable("skuId") Long skuId){
        goodsService.removeSkuFromEs(skuId);
        return false;
    }

}
