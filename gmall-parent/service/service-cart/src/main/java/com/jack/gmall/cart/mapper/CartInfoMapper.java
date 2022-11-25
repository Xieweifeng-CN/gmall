package com.jack.gmall.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jack.gmall.model.cart.CartInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 购物车实体映射
 */
@Mapper
public interface CartInfoMapper extends BaseMapper<CartInfo> {

    /**
     * 修改具体用户的具体购物车商品
     * @param username
     * @param skuId
     */
    @Update("update cart_info set sku_num=#{num} where user_id = #{username} and sku_id = #{skuId}")
    public int updateNum(String username,
                                                            Long skuId,
                                                            Integer num);

    /**
     * 修改购物车选择状态: 单个
     * @param username
     * @param id
     * @param status
     * @return
     */
    @Update("update cart_info set is_checked = #{status} where user_id = #{username} and id = #{id}")
    public int updateCheck(String username,
                                         Long id,
                                         Short status);

    /**
     * 修改购物车选择状态: 多个
     * @param username
     * @param status
     * @return
     */
    @Update("update cart_info set is_checked = #{status} where user_id = #{username}")
    public int updateCheckAll(String username,
                                            Short status);
}
