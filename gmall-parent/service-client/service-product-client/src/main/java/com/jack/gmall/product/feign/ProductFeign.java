package com.jack.gmall.product.feign;

import com.jack.gmall.model.product.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/26
 * @Description : 商品管理远程调用接口
 **/
@FeignClient(name = "service-product",path = "/api/product",contextId = "productFeign")
public interface ProductFeign {

    /**
     * 根据skuId获取sku信息
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId);

    /**
     * 通过三级分类id查询分类信息
     * @param category3Id
     * @return
     */
    @GetMapping("/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id")Long category3Id);

    /**
     * 根据skuId获取商品sku图片
     * @param skuId
     * @return
     */
    @GetMapping("/getImageList/{skuId}")
    public List<SkuImage> getImageList(@PathVariable Long skuId);

    /**
     * 根据spuId，skuId 查询销售属性集合
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId);
    /**
     * 根据spuId 查询map 集合属性
     * @param spuId
     * @return
     */
    @GetMapping("/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId);

    /**
     * 获取sku最新价格
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId);

    /**
     * 根据id获取品牌信息
     * @param id
     * @return
     */
    @GetMapping("/getBaseTradeMark/{id}")
    public BaseTrademark getBaseTradeMark(@PathVariable("id") Long id);

    /**
     * 根据skuId获取平台属性名称和值
     * @param skuId
     * @return
     */
    @GetMapping("/listBaseAttrInfoBySkuId/{skuId}")
    public List<BaseAttrInfo> listBaseAttrInfoBySkuId(@PathVariable("skuId") Long skuId);

    /**
     * 扣减商品库存
     * @param deCountMap
     */
    @PostMapping("/deCountCartStock")
    public void deCountCartStock(@RequestParam Map<String, String> deCountMap);

    /**
     * 商品回滚
     * @param deCountMap
     */
    @PostMapping("/rollbackStock")
    public void rollbackStock(@RequestParam Map<String, String> deCountMap);
}
