package com.jack.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jack.gmall.model.product.SkuInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    /**
     * 根据skuId扣减商品库存,
     * 使用了乐观锁, cas机制和volatile(可见性), 加上特殊的version控制(stock)
     * @param skuId
     * @param num
     */
    @Update("update sku_info set stock = stock - #{num} where id = #{skuId} and stock >= #{num}")
    int deCountCartStock(Long skuId, Integer num);

    /**
     * 回滚商品库存
     * @param skuId
     * @param num
     * @return
     */
    @Update("update sku_info set stock = stock + #{num} where id = #{skuId}")
    int rollbackStock(Long skuId, Integer num);
}
