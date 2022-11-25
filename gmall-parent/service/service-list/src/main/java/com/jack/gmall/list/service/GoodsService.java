package com.jack.gmall.list.service;

/**
 * 商品操作相关的接口
 */
public interface GoodsService {

    /**
     * 将数据库的商品写入到es中
     * @param skuId
     */
    public void addSkuIntoEs(Long skuId);

    /**
     * 将es的数据移除
     * @param goodsId
     */
    public void removeSkuFromEs(Long goodsId);

}
