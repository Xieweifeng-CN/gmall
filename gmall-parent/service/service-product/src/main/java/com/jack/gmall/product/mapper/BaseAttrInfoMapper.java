package com.jack.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jack.gmall.model.product.BaseAttrInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 平台属性实体映射
 */
@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {

    /**
     * 根据分类id获取平台属性
     * @return
     */
    List<BaseAttrInfo> listBaseAttrInfoByCategory(@Param("categoryId") Long categoryId);

    /**
     * 根据skuId获取平台属性
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> listBaseAttrInfoBySkuId(@Param("skuId") Long skuId);

}
