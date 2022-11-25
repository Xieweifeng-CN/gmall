package com.jack.gmall.product.controller;

import com.jack.gmall.common.constant.ProductConst;
import com.jack.gmall.common.result.Result;
import com.jack.gmall.model.product.*;
import com.jack.gmall.product.service.ItemService;
import com.jack.gmall.product.service.ManageService;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/24
 * @Description :
 **/
@RestController
@RequestMapping("/admin/product")
public class ManageController {

    @Autowired
    private ManageService manageService;

    /**
     * 获取一级分类
     * @return
     */
    @GetMapping("/getCategory1")
    public Result getCategory1(){
        return Result.ok(manageService.getCategory1());
    }

    /**
     * 获取二级分类
     * @param category1Id 一级分类id
     * @return
     */
    @GetMapping("/getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id){
        List<BaseCategory2> category2 = manageService.getCategory2(category1Id);
        return Result.ok(category2);
    }

    /**
     * 获取三级分类
     * @param category2Id
     * @return
     */
    @GetMapping("/getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id){
        List<BaseCategory3> category3 = manageService.getCategory3(category2Id);
        return Result.ok(category3);
    }

    /**
     * 根据三级分类id获取平台属性
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(
            @PathVariable("category1Id") Long category1Id,
            @PathVariable("category2Id") Long category2Id,
            @PathVariable("category3Id") Long category3Id){
        List<BaseAttrInfo> baseAttrInfo = manageService.getBaseAttrInfoByCategory(category3Id);
        return Result.ok(baseAttrInfo);
    }

    /**
     * 保存平台属性和平台属性值
     * @param baseAttrInfo
     * @return
     */
    @PostMapping("/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.insertOrUpdateBaseAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 删除平台属性和属性值
     * @param attrId
     * @return
     */
    @DeleteMapping("/deleteBaseAttr/{attrId}")
    public Result deleteBaseAttr(@PathVariable Long attrId){
        manageService.deleteBaseAttrInfoAndValue(attrId);
        return Result.ok();
    }

    /**
     * 获取所有商品品牌
     * @return
     */
    @GetMapping("/baseTrademark/getTrademarkList")
    public Result getTrademarkList(){
        return Result.ok(manageService.getTrademarkList());
    }

    /**
     * 获取所有商品销售属性
     * @return
     */
    @GetMapping("/baseSaleAttrList")
    public Result baseSaleAttrList(){
        return Result.ok(manageService.baseSaleAttrList());
    }

    /**
     * 保存商品spu信息
     * @param spuInfo
     * @return
     */
    @PostMapping("/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    /**
     * 根据三级分类获取商品spu信息分页
     * @param page
     * @param size
     * @param category3Id
     * @return
     */
    @GetMapping("/{page}/{size}")
    public Result pageSpuInfo(
            @PathVariable("page") Integer page,
            @PathVariable("size") Integer size,
            Long category3Id){
        return Result.ok(manageService.pageSpuInfo(page,size,category3Id));
    }

    /**
     * 商品sku信息分页条件查询
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list/{page}/{size}")
    public Result list(
            @PathVariable(value = "page") Integer page,
            @PathVariable(value = "size") Integer size){
        return Result.ok(manageService.listSkuInfo(page, size));
    }

    /**
     * 保存商品sku信息
     * @param skuInfo
     * @return
     */
    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    /**
     * 根据spuId获取销售属性
     * @param spuId
     * @return
     */
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable(value = "spuId") Long spuId){
        return Result.ok(manageService.selectSaleAttrBySpuId(spuId));
    }

    /**
     * 查询所有的图片列表
     * @return
     */
    @GetMapping("/spuImageList/{spuId}")
    public Result spuImageList(@PathVariable(value = "spuId") Long spuId){
        return Result.ok(manageService.spuImageList(spuId));
    }

    /**
     * 商品sku上架
     * @param skuId
     */
    @PutMapping("/onSale/{skuId}")
    public Result onSale(@PathVariable(value = "skuId") Long skuId){
        manageService.onOrDownSku(skuId, ProductConst.SKU_ON_SALE);
        return Result.ok();
    }

    /**
     * 商品sku下架
     * @param skuId
     */
    @PutMapping("/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable(value = "skuId") Long skuId){
        manageService.onOrDownSku(skuId, ProductConst.SKU_CANCEL_SALE);
        return Result.ok();
    }

    /**
     * 获取品牌分页列表
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/baseTrademark/{page}/{size}")
    public Result baseTrademark(
                                @PathVariable Integer page,
                                @PathVariable Integer size){
        return Result.ok(manageService.listBaseTrademark(page,size));
    }

    /**
     * 保存或修改品牌信息
     * @param baseTrademark
     * @return
     */
    @PostMapping("/baseTrademark/save")
    public Result save(@RequestBody BaseTrademark baseTrademark){
        manageService.saveTradeMark(baseTrademark);
        return Result.ok();
    }

    /**
     * 删除商品品牌
     * @param markId
     * @return
     */
    @DeleteMapping("/baseTrademark/remove/{id}")
    public Result remove(@PathVariable("id") Long markId){
        manageService.deleteTradeMark(markId);
        return Result.ok();
    }

    /**
     * 根据id获取品牌信息
     * @param markId
     * @return
     */
    @GetMapping("/baseTrademark/get/{id}")
    public Result get(@PathVariable("id") Long markId){
        return Result.ok(manageService.get(markId));
    }
}
