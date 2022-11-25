package com.jack.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jack.gmall.model.product.SpuSaleAttr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品销售属性mapper映射
 */
@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    /**
     * 根据spuId获取销售属性
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> selectSaleAttrBySpuId(@Param("spuId") Long spuId);

    /**
     * 根据skuId和spuId获取销售属性与值
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> selectSpuAttrBySpuIdAndSkuId(@Param("skuId") Long skuId,@Param("spuId") Long spuId);
}
