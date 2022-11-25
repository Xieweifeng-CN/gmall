package com.jack.gmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jack.gmall.model.product.*;
import com.jack.gmall.product.mapper.*;
import com.jack.gmall.product.service.ItemService;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/27
 * @Description : 提供给商品详情微服务内部调用接口的实现类
 **/
@Service
@Log4j2
@Transactional(rollbackFor = Exception.class)
public class ItemServiceImpl implements ItemService {

    @Resource
    private SkuInfoMapper skuInfoMapper;

    /**
     * 根据id查询商品sku信息
     *
     * @param skuId
     */
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        return skuInfo;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 使用缓存优化查询
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfoFromRedisOrMysql(Long skuId) {
        //从redis中获取数据  sku:1:info
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get("sku:" + skuId + ":info");
        //若redis中有数据直接返回
        if (skuInfo != null){
            return skuInfo;
        }
        //redis中没有
        RLock lock = redissonClient.getLock("sku:" + skuId + ":lock");
        try {
            //加锁,只有获取锁成功的线程才能去查询数据库,其他的线程等待
            if (lock.tryLock(100,100, TimeUnit.SECONDS)){
                try {
                    //查询数据库
                    skuInfo = skuInfoMapper.selectById(skuId);
                    if (skuInfo == null || skuInfo.getId() == null){
                        //数据库也没有
                        skuInfo = new SkuInfo();
                        //防止缓存穿透
                        redisTemplate.opsForValue().set("sku:" + skuId + ":info",skuInfo,300,TimeUnit.SECONDS);
                    }else {
                        //数据库有,需要将数据写入redis,有效期一天
                        redisTemplate.opsForValue().set("sku:" + skuId + ":info",skuInfo,24*60*60,TimeUnit.SECONDS);
                    }
                    //返回
                    return skuInfo;
                }catch (Exception e){
                    log.error("加锁成功,但业务执行异常,出现异常的商品id为: ",skuId);
                }finally {
                    lock.unlock();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("加锁失败,出现异常的商品id为: ",skuId);
        }
        return null;
    }

    @Resource
    private BaseCategoryViewMapper baseCategoryViewMapper;

    /**
     * 查询全部分类视图
     *
     * @param category3Id
     * @return
     */
    @Override
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Autowired
    private SkuImageMapper skuImageMapper;

    /**
     * 根据skuId查询商品sku图片
     *
     * @param skuId
     * @return
     */
    @Override
    public List<SkuImage> getImageList(Long skuId) {
        return skuImageMapper.selectList(
                new LambdaQueryWrapper<SkuImage>()
                        .eq(SkuImage::getSkuId,skuId));
    }

    /**
     * 根据skuId获取价格信息
     *
     * @param skuId
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        if (skuId == null){
            throw new RuntimeException("skuId异常");
        }
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        return skuInfo.getPrice();
    }

    @Resource
    private SpuSaleAttrMapper spuSaleAttrMapper;

    /**
     * 根据spuId和skuId获取销售信息
     *
     * @param skuId
     * @param spuId
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.selectSpuAttrBySpuIdAndSkuId(skuId,spuId);
    }

    @Resource
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    /**
     * 查询指定spu所有销售属性值与sku对应关系
     *
     * @param spuId
     */
    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        ConcurrentHashMap<Object, Object> result = new ConcurrentHashMap<>();
        List<Map> mapList = skuSaleAttrValueMapper.selectSkuMap(spuId);
        mapList.stream().forEach(map -> {
            result.put(map.get("value_id"),map.get("sku_id"));
        });
        return result;
    }

    @Resource
    private BaseTradeMarkMapper baseTradeMarkMapper;

    /**
     * 根据商品spuId查询品牌
     *
     * @param id
     * @return
     */
    @Override
    public BaseTrademark getBaseTrademark(Long id) {
        return baseTradeMarkMapper.selectById(id);
    }

    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;

    /**
     * 根据skuId获取平台属性
     *
     * @param skuId
     * @return
     */
    @Override
    public List<BaseAttrInfo> listBaseAttrInfoBySkuId(Long skuId) {
        return baseAttrInfoMapper.listBaseAttrInfoBySkuId(skuId);
    }

    /**
     * 扣减商品库存
     *
     * @param deCountMap
     */
    @Override
    public void deCountSkuStock(Map<String, String> deCountMap) {
        deCountMap.entrySet().stream().forEach(o->{
            //待扣减的商品skuId
            String skuId = o.getKey();
            //商品扣减数量
            String deCount = o.getValue();
            //执行数据库扣减
            int i = skuInfoMapper.deCountCartStock(Long.valueOf(skuId), Integer.valueOf(deCount));
            if (i <= 0){
                throw new RuntimeException("商品库存扣减失败!");
            }
        });
    }

    /**
     * 回滚商品库存
     *
     * @param deCountMap
     */
    @Override
    public void rollbackStock(Map<String, String> deCountMap) {
        deCountMap.entrySet().stream().forEach(result->{
            //回滚的商品id
            Long skuId = Long.valueOf(result.getKey());
            //回滚的库存数量
            Integer deCount = Integer.valueOf(result.getValue());
            //回滚库存
            int i = skuInfoMapper.rollbackStock(skuId, deCount);
            if (i < 0){
                throw new RuntimeException("回滚库存失败!");
            }
        });
    }
}
