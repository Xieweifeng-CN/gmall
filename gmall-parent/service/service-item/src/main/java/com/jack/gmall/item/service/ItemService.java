package com.jack.gmall.item.service;

import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/26
 * @Description : 商品详情service接口类
 **/
public interface ItemService {

    /**
     * 根据skuId查询sku的详细信息,用于生成sku的详情页面
     * @param skuId
     * @return
     */
    public Map<String,Object> getItemInfo(Long skuId);

}
