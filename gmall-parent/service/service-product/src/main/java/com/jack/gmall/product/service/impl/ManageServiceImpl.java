package com.jack.gmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jack.gmall.common.constant.ProductConst;
import com.jack.gmall.goods.feign.GoodsFeign;
import com.jack.gmall.model.product.*;
import com.jack.gmall.product.mapper.*;
import com.jack.gmall.product.service.ManageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/24
 * @Description :
 * 管理商品使用实现类
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class ManageServiceImpl implements ManageService {

    @Resource
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 获取一级分类
     *
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    @Resource
    private BaseCategory2Mapper baseCategory2Mapper;

    /**
     * 根据一级分类id获取二级分类
     *
     * @param category1Id
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        return baseCategory2Mapper.selectList(
                new LambdaQueryWrapper<BaseCategory2>()
                        .eq(BaseCategory2::getCategory1Id, category1Id));
    }

    @Resource
    private BaseCategory3Mapper baseCategory3Mapper;

    /**
     * 根据二级分类id获取三级分类
     *
     * @param category2Id
     */
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        return baseCategory3Mapper.selectList(
                new LambdaQueryWrapper<BaseCategory3>()
                        .eq(BaseCategory3::getCategory2Id, category2Id));
    }

    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;

    /**
     * 根据三级分类id获取平台属性
     *
     * @param category3Id
     */
    @Override
    public List<BaseAttrInfo> getBaseAttrInfoByCategory(Long category3Id) {
        return baseAttrInfoMapper.listBaseAttrInfoByCategory(category3Id);
    }

    @Resource
    private BaseAttrValueMapper baseAttrValueMapper;

    /**
     * 添加或修改平台属性
     *
     * @param baseAttrInfo
     */
    @Override
    public void insertOrUpdateBaseAttrInfo(BaseAttrInfo baseAttrInfo) {
        //校验
        if (baseAttrInfo == null || StringUtils.isEmpty(baseAttrInfo.getAttrName())) {
            throw new RuntimeException("参数异常");
        }
        //若id存在,说明已添加过,则进行修改操作,否则新增
        if (baseAttrInfo.getId()!=null){
            int update = baseAttrInfoMapper.updateById(baseAttrInfo);
            if (update < 0){
                throw new RuntimeException("平台属性修改失败");
            }
            //删除原有的属性值
            baseAttrValueMapper.delete(
                    new LambdaQueryWrapper<BaseAttrValue>()
                            .eq(BaseAttrValue::getAttrId, baseAttrInfo.getId()));
        }else {
            //保存平台属性
            int info = baseAttrInfoMapper.insert(baseAttrInfo);
            if (info < 0) {
                throw new RuntimeException("平台属性保存失败");
            }
        }
        Long attrInfoId = baseAttrInfo.getId();
        //根据平台属性attrValueList集合,获取平台属性值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        attrValueList.stream().forEach(baseAttrValue -> {
            if (!StringUtils.isEmpty(baseAttrValue.getValueName())) {
                //插入属性id,保存属性值
                baseAttrValue.setAttrId(attrInfoId);
                int value = baseAttrValueMapper.insert(baseAttrValue);
                if (value < 0) {
                    throw new RuntimeException("平台属性值保存失败");
                }
            }
        });
    }

    /**
     * 删除平台属性和属性值
     *
     * @param attrId
     */
    @Override
    public void deleteBaseAttrInfoAndValue(Long attrId) {
        baseAttrInfoMapper.deleteById(attrId);
        baseAttrValueMapper.delete(
                new LambdaQueryWrapper<BaseAttrValue>()
                        .eq(BaseAttrValue::getAttrId,attrId));
    }

    @Resource
    private BaseTradeMarkMapper baseTradeMarkMapper;

    /**
     * 获取所有商品品牌
     */
    @Override
    public List<BaseTrademark> getTrademarkList() {
        return baseTradeMarkMapper.selectList(null);
    }

    @Resource
    private BaseSaleAttrMapper baseSaleAttrMapper;

    /**
     * 获取所有商品销售属性
     */
    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    @Resource
    private SpuInfoMapper spuInfoMapper;

    @Resource
    private SpuImageMapper spuImageMapper;

    @Resource
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Resource
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    /**
     * 保存商品属性spu
     */
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //校验
        if (spuInfo == null) {
            throw new RuntimeException("参数错误");
        }
        Long spuId;
        //判断id是否存在
        if (spuInfo.getId() != null) {
            //存在,则进行修改操作
            int update = spuInfoMapper.updateById(spuInfo);
            if (update < 0) {
                throw new RuntimeException("商品spu信息修改失败");
            }
            spuId = spuInfo.getId();
            //删除SpuSaleAttr 及 SpuSaleAttrValue 及 spuImage
            int spuImagedelete = spuImageMapper.delete(
                    new LambdaQueryWrapper<SpuImage>()
                            .eq(SpuImage::getSpuId, spuId));
            int spuSaleAttrdelete = spuSaleAttrMapper.delete(
                    new LambdaQueryWrapper<SpuSaleAttr>()
                            .eq(SpuSaleAttr::getSpuId, spuId));
            int spuSaleAttrValuedelete = spuSaleAttrValueMapper.delete(
                    new LambdaQueryWrapper<SpuSaleAttrValue>()
                            .eq(SpuSaleAttrValue::getSpuId, spuId));
            if (spuImagedelete < 0 || spuSaleAttrdelete < 0 || spuSaleAttrValuedelete < 0){
                throw new RuntimeException("商品spu修改失败");
            }
        } else {
            //不存在,保存
            //保存商品基本信息
            int insert = spuInfoMapper.insert(spuInfo);
            spuId = spuInfo.getId();
            if (insert <= 0) {
                throw new RuntimeException("商品spu保存失败");
            }
        }
        saveSpuImage(spuId, spuInfo);
        saveSpuSaleAttr(spuId, spuInfo);
    }

    /**
     * 按条件获取商品spu分页
     *
     * @param page
     * @param size
     * @param category3Id
     */
    @Override
    public IPage<SpuInfo> pageSpuInfo(Integer page, Integer size, Long category3Id) {
        return spuInfoMapper.selectPage(
                new Page<SpuInfo>(page,size),
                new LambdaQueryWrapper<SpuInfo>()
                        .eq(SpuInfo::getCategory3Id,category3Id));
    }

    @Resource
    private SkuInfoMapper skuInfoMapper;

    /**
     * 查询所有商品sku信息
     * @return
     */
    @Override
    public IPage<SkuInfo> listSkuInfo(Integer page, Integer size) {
        return skuInfoMapper.selectPage(new Page<SkuInfo>(page,size),null);
    }

    @Resource
    private SkuImageMapper skuImageMapper;

    @Resource
    private SkuAttrValueMapper skuAttrValueMapper;

    @Resource
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    /**
     * 保存商品sku信息
     *
     * @param skuInfo
     */
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //校验
        if(skuInfo == null && skuInfo.getIsSale() == null){
            throw new RuntimeException("参数错误");
        }
        //判断是否存在skuId,若不存在,则进行修改操作
        if (skuInfo.getId() != null){
            //修改skuInfo
            int update = skuInfoMapper.updateById(skuInfo);
            if (update < 0){
                throw new RuntimeException("商品sku信息修改失败");
            }
            //删除sku图片, sku平台属性, sku销售属性
            int skuImagedelete = skuImageMapper.delete(
                    new LambdaQueryWrapper<SkuImage>()
                            .eq(SkuImage::getSkuId, skuInfo.getId()));
            int skuAttrValueDelete = skuAttrValueMapper.delete(
                    new LambdaQueryWrapper<SkuAttrValue>()
                            .eq(SkuAttrValue::getSkuId, skuInfo.getId()));
            int skuSaleAttrValueDelete = skuSaleAttrValueMapper.delete(
                    new LambdaQueryWrapper<SkuSaleAttrValue>()
                            .eq(SkuSaleAttrValue::getSkuId, skuInfo.getId()));
            if (skuImagedelete < 0 || skuAttrValueDelete < 0 || skuSaleAttrValueDelete < 0){
                throw new RuntimeException("修改失败");
            }
        }else {
            //保存skuInfo
            int insert = skuInfoMapper.insert(skuInfo);
            if (insert <= 0){
                throw new RuntimeException("商品sku信息保存失败");
            }
        }
        Long skuId = skuInfo.getId();
        saveSkuImage(skuId,skuInfo);
        saveSkuAttrValue(skuId,skuInfo);
        saveSkuSaleAttrValue(skuId,skuInfo);
    }

    /**
     * 根据spuId获取销售属性
     *
     * @param spuId
     */
    @Override
    public List<SpuSaleAttr> selectSaleAttrBySpuId(Long spuId) {
        return spuSaleAttrMapper.selectSaleAttrBySpuId(spuId);
    }

    /**
     * 获取所有商品spu图片列表
     */
    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        return spuImageMapper.selectList(null);
    }

    @Autowired
    private GoodsFeign goodsFeign;

    /**
     * 商品sku上下架
     *
     * @param skuId
     * @param status
     */
    @Override
    public void onOrDownSku(Long skuId, Short status) {
        //参数校验
        if (skuId == null){
            return;
        }
        //查询商品是否存在
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo==null){
            return;
        }
        //设置商品状态
        skuInfo.setIsSale(status);
        //修改商品信息
        int i = skuInfoMapper.updateById(skuInfo);
        if (i < 0){
            throw new RuntimeException("商品状态修改失败");
        }
        /**
         * 1. 交换机
         * 2. 路由规则
         * 3. 发送消息
         */
        //判断消息队列存储情况
        if (status.equals(ProductConst.SKU_ON_SALE)){
            //状态为1,写入上架队列
            rabbitTemplate.convertAndSend(
                    "product_exchange",
                    "product.upper",
                    skuId + ":" + status);
        }else {
            //状态为0,删除es数据
            rabbitTemplate.convertAndSend(
                    "product_exchange",
                    "product.down",
                    skuId + ":" + status);
        }
    }

    /**
     * 获取品牌分页列表
     *  @param page
     * @param size
     * @return
     */
    @Override
    public IPage<BaseTrademark> listBaseTrademark(Integer page, Integer size) {
        return baseTradeMarkMapper.selectPage(new Page<>(page,size),null);
    }

    /**
     * 保存品牌
     *
     * @param baseTrademark
     */
    @Override
    public void saveTradeMark(BaseTrademark baseTrademark) {
        //校验
        if(baseTrademark == null || StringUtils.isEmpty(baseTrademark.getTmName())){
            throw new RuntimeException("品牌参数错误");
        }
        //判断是新增还是修改
        if (baseTrademark.getId() != null){
            //修改
            int update = baseTradeMarkMapper.updateById(baseTrademark);
            if (update < 0){
                throw new RuntimeException("品牌修改失败");
            }
        }else {
            //新增
            int insert = baseTradeMarkMapper.insert(baseTrademark);
            if (insert < 0){
                throw new RuntimeException("品牌保存失败");
            }
        }
    }

    /**
     * 删除品牌
     *
     * @param markId
     */
    @Override
    public void deleteTradeMark(Long markId) {
        if (markId == null){
            return;
        }
        int delete = baseTradeMarkMapper.deleteById(markId);
        if (delete < 0){
            throw new RuntimeException("品牌删除失败");
        }
    }

    /**
     * 根据id获取品牌
     *
     * @param markId
     */
    @Override
    public BaseTrademark get(Long markId) {
        return baseTradeMarkMapper.selectById(markId);
    }


    /**
     * 保存商品sku销售属性和属性值
     * @param skuId
     * @param skuInfo
     */
    private void saveSkuSaleAttrValue(Long skuId, SkuInfo skuInfo) {
        skuInfo.getSkuSaleAttrValueList().stream().forEach(skuSaleAttrValue -> {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            int insert = skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            if (insert <= 0){
                throw new RuntimeException("保存商品sku销售属性信息失败");
            }
        });
    }

    /**
     * 保存商品sku平台属性和属性值
     * @param skuId
     * @param skuInfo
     */
    private void saveSkuAttrValue(Long skuId, SkuInfo skuInfo) {
        skuInfo.getSkuAttrValueList().stream().forEach(skuAttrValue -> {
            skuAttrValue.setSkuId(skuId);
            int insert = skuAttrValueMapper.insert(skuAttrValue);
            if (insert <= 0){
                throw new RuntimeException("保存商品sku平台属性信息失败");
            }
        });
    }

    /**
     * 保存商品sku图片信息
     * @param skuId
     * @param skuInfo
     */
    private void saveSkuImage(Long skuId, SkuInfo skuInfo) {
        skuInfo.getSkuImageList().stream().forEach(skuImage -> {
            skuImage.setSkuId(skuId);
            int insert = skuImageMapper.insert(skuImage);
            if (insert <= 0){
                throw new RuntimeException("保存商品sku图片信息失败");
            }
        });
    }

    /**
     * 保存商品属性值
     * @param spuId
     * @param spuSaleAttr
     */
    private void saveSpuSaleAttrValue(Long spuId, SpuSaleAttr spuSaleAttr) {
        spuSaleAttr.getSpuSaleAttrValueList().stream().forEach(spuSaleAttrValue -> {
            spuSaleAttrValue.setSpuId(spuId);
            spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
            int insert = spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            if (insert <= 0){
                throw new RuntimeException("商品属性值保存失败");
            }
        });
    }

    /**
     * 保存商品属性名称
     * @param spuId
     * @param spuInfo
     */
    private void saveSpuSaleAttr(Long spuId, SpuInfo spuInfo) {
        spuInfo.getSpuSaleAttrList().stream().forEach(spuSaleAttr -> {
            spuSaleAttr.setSpuId(spuId);
            int insert = spuSaleAttrMapper.insert(spuSaleAttr);
            if (insert <= 0){
                throw new RuntimeException("保存商品属性名称失败");
            }
            saveSpuSaleAttrValue(spuId,spuSaleAttr);
        });
    }

    /**
     * 保存商品图片
     * @param spuId
     * @param spuInfo
     */
    private void saveSpuImage(Long spuId, SpuInfo spuInfo) {
        spuInfo.getSpuImageList().stream().forEach(spuImage -> {
            spuImage.setSpuId(spuId);
            int spuImageInsert = spuImageMapper.insert(spuImage);
            if (spuImageInsert <= 0){
                throw new RuntimeException("商品图片保存失败");
            }
        });
    }

}
