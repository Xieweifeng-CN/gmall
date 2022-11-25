package com.jack.gmall.product.feign;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 前台首页远程调用feign接口
 */
@FeignClient(name = "service-product",path = "/api/index",contextId = "indexFeign")
public interface IndexFeign {

    /**
     * 获取首页的分类信息
     * @return
     */
    @GetMapping("/getIndexCategory")
    public List<JSONObject> getIndexCategory();

}
