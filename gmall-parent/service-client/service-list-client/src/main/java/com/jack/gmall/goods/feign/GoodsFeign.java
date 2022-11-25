package com.jack.gmall.goods.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-list",path = "/api/goods",contextId = "GoodsFeign")
public interface GoodsFeign {

    /**
     * 写入商品到es中
     * @param skuId
     */
    @GetMapping("/add/{skuId}")
    public Boolean add(@PathVariable("skuId") Long skuId);

    /**
     * 删除es中的商品
     * @param skuId
     * @return
     */
    @GetMapping("/remove/{skuId}")
    public Boolean remove(@PathVariable("skuId") Long skuId);

}
