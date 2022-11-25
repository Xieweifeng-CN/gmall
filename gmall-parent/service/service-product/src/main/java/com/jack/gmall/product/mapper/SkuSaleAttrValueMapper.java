package com.jack.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jack.gmall.model.product.SkuSaleAttrValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 商品sku销售属性值映射
 */
@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {

    /**
     * 根据spuId查询所有sku销售属性键值对
     * @param spuId
     * @return
     */
    List<Map> selectSkuMap(@Param("spuId") Long spuId);

}
