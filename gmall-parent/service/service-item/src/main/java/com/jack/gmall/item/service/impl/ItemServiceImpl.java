package com.jack.gmall.item.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jack.gmall.item.service.ItemService;
import com.jack.gmall.model.product.*;
import com.jack.gmall.product.feign.ProductFeign;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/26
 * @Description :
 **/
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeign productFeign;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 根据skuId查询sku的详细信息,用于生成sku的详情页面
     *
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getItemInfo(Long skuId) {
        //线程安全的map集合
        Map<String, Object> result = new ConcurrentHashMap<>();
        if (skuId == null) {
            return result;
        }
        //核心线程先走,返回结果再走后续线程
        CompletableFuture<SkuInfo> future1 = CompletableFuture.supplyAsync(() -> {
            //远程调用商品微服务service-product, 获取sku的info
            SkuInfo skuInfo = productFeign.getSkuInfo(skuId);
            if (skuInfo == null || skuInfo.getId() == null) {
                return null;
            }
            result.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);

        //后续线程
        //对应的一、二、三级类别
        CompletableFuture<Void> future2 = future1.thenAcceptAsync((skuInfo) -> {
            if (skuInfo == null || skuInfo.getId() == null) {
                return;
            }
            BaseCategoryView categoryView = productFeign.getCategoryView(skuInfo.getCategory3Id());
            result.put("categoryView", categoryView);
        }, threadPoolExecutor);

        //图片列表
        CompletableFuture<Void> future3 = future1.thenAcceptAsync((skuInfo) -> {
            if (skuInfo == null || skuInfo.getId() == null) {
                return;
            }
            List<SkuImage> imageList = productFeign.getImageList(skuInfo.getId());
            result.put("imageList", imageList);
        }, threadPoolExecutor);

        //最新价格
        CompletableFuture<Void> future4 = future1.thenAcceptAsync((skuInfo) -> {
            if (skuInfo == null || skuInfo.getId() == null) {
                return;
            }
            BigDecimal skuPrice = productFeign.getSkuPrice(skuInfo.getId());
            result.put("skuPrice", skuPrice);
        }, threadPoolExecutor);

        //获取所有销售属性值与sku的键值对
        CompletableFuture<Void> future5 = future1.thenAcceptAsync((skuInfo) -> {
            if (skuInfo == null || skuInfo.getId() == null) {
                return;
            }
            Map skuValueIdsMap = productFeign.getSkuValueIdsMap(skuInfo.getSpuId());
            //map类型进行json串转换,方便前端进行处理
            result.put("skuValueIdsMap", JSONObject.toJSONString(skuValueIdsMap));
        }, threadPoolExecutor);

        //获取指定spu与skud商品的销售属性值信息
        CompletableFuture<Void> future6 = future1.thenAcceptAsync((skuInfo) -> {
            if (skuInfo == null || skuInfo.getId() == null) {
                return;
            }
            List<SpuSaleAttr> spuSaleAttrs = productFeign.getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
            result.put("spuSaleAttrs", spuSaleAttrs);
        }, threadPoolExecutor);

        //全部任务执行成功才返回结果,若只看结果成功与否可不join()
        CompletableFuture.allOf(future1, future2, future3, future4, future5, future6).join();
        //返回结果, 数据预热, 上架的时候就走本方法加入缓存
        return result;
    }
}
