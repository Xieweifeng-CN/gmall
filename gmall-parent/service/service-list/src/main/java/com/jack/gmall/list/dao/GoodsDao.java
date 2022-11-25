package com.jack.gmall.list.dao;

import com.jack.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 商品的实体映射
 * 类型: 实体类, 主键类型
 */
@Repository
public interface GoodsDao extends ElasticsearchRepository<Goods,Long> {
}
