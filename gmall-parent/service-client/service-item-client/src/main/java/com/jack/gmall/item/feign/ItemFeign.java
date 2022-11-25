package com.jack.gmall.item.feign;

import com.jack.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 商品详情微服务的feign接口
 */
@FeignClient(name = "service-item",path = "/index/info",contextId = "itemFeign")
public interface ItemFeign {

    /**
     * 查询sku的详细信息,生成sku的详情页面
     * @param skuId
     * @return
     */
    @GetMapping("/getItemInfo/{skuId}")
    public Map<String, Object> getItemInfo(@PathVariable("skuId") Long skuId);

}
