package com.jack.gmall.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jack.gmall.model.activity.SeckillGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 秒杀商品mapper映射
 */
@Mapper
public interface SecKillGoodsMapper extends BaseMapper<SeckillGoods> {

    /**
     * 同步活动结束后的商品库存
     * @param goodsId
     * @param stockCount
     * @return
     */
    @Update("update seckill_goods set stock_count = #{stockCount} where id = #{goodsId}")
    public int updateSeckillGoodsStock(String goodsId, Integer stockCount);

}
