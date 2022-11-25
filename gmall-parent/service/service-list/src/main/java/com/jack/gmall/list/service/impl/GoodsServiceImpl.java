package com.jack.gmall.list.service.impl;

import com.jack.gmall.list.dao.GoodsDao;
import com.jack.gmall.list.service.GoodsService;
import com.jack.gmall.model.list.Goods;
import com.jack.gmall.model.list.SearchAttr;
import com.jack.gmall.model.product.BaseAttrInfo;
import com.jack.gmall.model.product.BaseCategoryView;
import com.jack.gmall.model.product.BaseTrademark;
import com.jack.gmall.model.product.SkuInfo;
import com.jack.gmall.product.feign.ProductFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/2
 * @Description : 商品操作相关的接口实现类
 **/
@Service
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private GoodsDao goodsDao;

    @Autowired
    private ProductFeign productFeign;

    /**
     * 将数据库的商品写入到es中
     *
     * @param skuId
     */
    @Override
    public void addSkuIntoEs(Long skuId) {
        //参数校验
        if (skuId == null){
            throw new RuntimeException("参数异常");
        }
        //查询商品信息
        SkuInfo skuInfo = productFeign.getSkuInfo(skuId);
        //防止缓存穿透会存空的对象
        if (skuInfo==null || skuInfo.getId()==null){
            throw new RuntimeException("商品不存在");
        }
        //将商品skuInfo转换为es的goods类型
        Goods goods = new Goods();
        goods.setId(skuInfo.getId());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTitle(skuInfo.getSkuName());
        //最新的价格
        BigDecimal skuPrice = productFeign.getSkuPrice(skuId);
        goods.setPrice(skuPrice.doubleValue());
        goods.setCreateTime(new Date());
        //品牌
        goods.setTmId(skuInfo.getTmId());
        BaseTrademark baseTradeMark = productFeign.getBaseTradeMark(skuInfo.getTmId());
        goods.setTmName(baseTradeMark.getTmName());
        goods.setTmLogoUrl(baseTradeMark.getLogoUrl());
        //分类视图
        BaseCategoryView categoryView =
                productFeign.getCategoryView(skuInfo.getCategory3Id());
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory3Name(categoryView.getCategory3Name());
        //平台属性集合对象
        List<BaseAttrInfo> attrInfoList = productFeign.listBaseAttrInfoBySkuId(skuId);
        List<SearchAttr> attrs = attrInfoList.stream().map(baseAttrInfo -> {
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(attrs);
        //保存
        goodsDao.save(goods);
    }

    /**
     * 将es的数据移除
     *
     * @param goodsId
     */
    @Override
    public void removeSkuFromEs(Long goodsId) {
        //根据id删除
        goodsDao.deleteById(goodsId);
    }
}
