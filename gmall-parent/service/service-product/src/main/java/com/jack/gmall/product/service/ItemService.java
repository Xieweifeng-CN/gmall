package com.jack.gmall.product.service;

import com.jack.gmall.model.product.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 提供给商品详情微服务内部调用的接口类
 */
public interface ItemService {

    /**
     * 根据id查询商品sku信息
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * 使用缓存优化查询
     */
    SkuInfo getSkuInfoFromRedisOrMysql(Long skuId);

    /**
     * 查询全部分类视图
     * @return
     */
    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);

    /**
     * 根据skuId查询商品sku图片
     * @param skuId
     * @return
     */
    List<SkuImage> getImageList(Long skuId);

    /**
     * 根据skuId获取价格信息
     */
    BigDecimal getSkuPrice(Long skuId);

    /**
     * 根据spuId和skuId获取销售信息
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    /**
     * 查询指定spu所有销售属性值与sku对应关系
     */
    public Map getSkuValueIdsMap(Long spuId);

    /**
     * 根据商品spuId查询品牌
     * @param id
     * @return
     */
    public BaseTrademark getBaseTrademark(Long id);

    /**
     * 根据skuId获取平台属性
     * @param skuId
     * @return
     */
    public List<BaseAttrInfo> listBaseAttrInfoBySkuId(Long skuId);

    /**
     * 扣减商品库存
     * @param deCountMap
     */
    void deCountSkuStock(Map<String, String> deCountMap);

    /**
     * 回滚商品
     * @param deCountMap
     */
    void rollbackStock(Map<String, String> deCountMap);
}
