package com.jack.gmall.product.controller;

import com.jack.gmall.common.cache.Java0509Cache;
import com.jack.gmall.model.product.*;
import com.jack.gmall.product.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/27
 * @Description : 获取商品详情页信息
 **/
@RestController
@RequestMapping("/api/product")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 根据skuId获取商品sku信息
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuInfo/{skuId}")
    @Java0509Cache(prefix = "getSkuInfo:")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId){
        return itemService.getSkuInfo(skuId);
    }

    /**
     * 查询商品全部分类视图
     * @param category3Id
     * @return
     */
    @GetMapping("/getCategoryView/{category3Id}")
    @Java0509Cache(prefix = "getCategoryView:")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id){
        return itemService.getCategoryViewByCategory3Id(category3Id);
    }

    /**
     * 根据skuId获取商品sku图片
     * @param skuId
     * @return
     */
    @GetMapping("/getImageList/{skuId}")
    @Java0509Cache(prefix = "getImageList:")
    public List<SkuImage> getImageList(@PathVariable("skuId") Long skuId){
        return itemService.getImageList(skuId);
    }

    /**
     * 获取商品sku最新价格
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuPrice/{skuId}")
    @Java0509Cache(prefix = "getSkuPrice:")
    public BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId){
        return itemService.getSkuPrice(skuId);
    }

    /**
     * 根据spuId和skuId查询销售属性
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    @Java0509Cache(prefix = "getSpuSaleAttrListCheckBySku:")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(
            @PathVariable("skuId") Long skuId,
            @PathVariable("spuId") Long spuId){
        return itemService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    /**
     * 根据spuId查询所有销售值与sku的对应关系
     * @param spuId
     * @return
     */
    @GetMapping("/getSkuValueIdsMap/{spuId}")
    @Java0509Cache(prefix = "getSkuValueIdsMap:")
    public Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId){
        return itemService.getSkuValueIdsMap(spuId);
    }

    /**
     * 根据id获取品牌信息
     * @param id
     * @return
     */
    @GetMapping("/getBaseTradeMark/{id}")
    @Java0509Cache(prefix = "getBaseTradeMark:")
    public BaseTrademark getBaseTradeMark(@PathVariable("id") Long id){
        return itemService.getBaseTrademark(id);
    }

    /**
     * 根据skuId获取平台属性名称和值
     * @param skuId
     * @return
     */
    @GetMapping("/listBaseAttrInfoBySkuId/{skuId}")
    @Java0509Cache(prefix = "listBaseAttrInfoBySkuId:")
    public List<BaseAttrInfo> listBaseAttrInfoBySkuId(@PathVariable("skuId") Long skuId){
        return itemService.listBaseAttrInfoBySkuId(skuId);
    }

    /**
     * 扣减商品库存
     * @param deCountMap
     */
    @PostMapping("/deCountCartStock")
    public void deCountCartStock(@RequestParam Map<String, String> deCountMap){
        itemService.deCountSkuStock(deCountMap);
    }

    /**
     * 商品回滚
     * @param deCountMap
     */
    @PostMapping("/rollbackStock")
    public void rollbackStock(@RequestParam Map<String, String> deCountMap){
        itemService.rollbackStock(deCountMap);
    }
}
