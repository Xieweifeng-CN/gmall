package com.jack.gmall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jack.gmall.model.product.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 管理商品使用接口
 */
public interface ManageService {

    /**
     * 获取一级分类
     * @return
     */
    List<BaseCategory1> getCategory1();

    /**
     * 获取二级分类
     */
    List<BaseCategory2> getCategory2(Long category1Id);

    /**
     * 获取三级分类
     */
    List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * 根据三级分类id获取平台属性
     */
    List<BaseAttrInfo> getBaseAttrInfoByCategory(Long category3Id);

    /**
     * 添加或修改平台属性
     */
    void insertOrUpdateBaseAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 删除平台属性和属性值
     */
    void deleteBaseAttrInfoAndValue(Long attrId);

    /**
     * 获取所有商品品牌
     */
    List<BaseTrademark> getTrademarkList();

    /**
     * 获取所有商品销售属性
     */
    List<BaseSaleAttr> baseSaleAttrList();

    /**
     * 保存商品属性spu
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 按条件获取商品spu分页
     */
    IPage<SpuInfo> pageSpuInfo(Integer page, Integer size, Long category3Id);

    /**
     * 查询所有商品sku信息
     * @return
     */
    IPage<SkuInfo> listSkuInfo(Integer page, Integer size);

    /**
     * 保存商品sku信息
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 根据spuId获取销售属性
     */
    List<SpuSaleAttr> selectSaleAttrBySpuId(Long spuId);

    /**
     * 获取所有商品spu图片列表
     */
    List<SpuImage> spuImageList(Long spuId);

    /**
     * 商品sku上架
     */
    void onOrDownSku(Long skuId,Short status);

    /**
     * 获取品牌分页列表
     * @return
     */
    IPage<BaseTrademark> listBaseTrademark(Integer page, Integer size);

    /**
     * 保存品牌
     */
    void saveTradeMark(BaseTrademark baseTrademark);

    /**
     * 删除品牌
     */
    void deleteTradeMark(Long markId);

    /**
     * 根据id获取品牌
     */
    BaseTrademark get(Long markId);
}
