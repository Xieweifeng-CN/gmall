package com.jack.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jack.gmall.list.service.SearchService;
import com.jack.gmall.model.list.Goods;
import com.jack.gmall.model.list.SearchResponseAttrVo;
import com.jack.gmall.model.list.SearchResponseTmVo;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/3
 * @Description : 商品搜索接口实现类
 **/
@Service
@Log4j2
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 获取搜索条件数据信息
     *
     * @param searchData
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchData) {
        try {
            //拼接请求参数
            SearchRequest searchRequest = buildQueryParams(searchData);
            //执行搜索
            SearchResponse search = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
            //解析请求
            return getSearchResult(search);
        } catch (IOException e) {
            log.error("搜索商品发生异常,异常内容为: " + e.getMessage());
        }
        return null;
    }

    /**
     * 构建搜索条件
     * @param searchData
     * @return
     */
    private SearchRequest buildQueryParams(Map<String, String> searchData) {
        //初始化搜索请求体
        SearchRequest searchRequest = new SearchRequest("goods_java0107");
        //初始化条件构造体
        SearchSourceBuilder builder = SearchSourceBuilder.searchSource();
        //构造组合查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //1.设置关键字查询条件
        String keywords = searchData.get("keywords");
        //关键字查询
        if (!StringUtils.isEmpty(keywords)){
            boolQuery.must(QueryBuilders.matchQuery("title",keywords));
        }

        //2.设置三级分类查询条件
        String category = searchData.get("category");
        //分类查询 category=30:颜色
        if (!StringUtils.isEmpty(category)){
            String[] split = category.split(":");
            boolQuery.must(QueryBuilders.termQuery("category3Id",split[0]));
        }

        //设置品牌查询
        // select tm_id as (aggTmId) from sku_info where sku_name like %手机% group by (tm_id)
        // tradeMark=2:华为
        String tradeMark = searchData.get("tradeMark");
        if (!StringUtils.isEmpty(tradeMark)){
            //3.设置品牌查询条件
            String[] split = tradeMark.split(":");
            boolQuery.must(QueryBuilders.termQuery("tmId",split[0]));
        }else {
            //聚合条件: 搜索时展出, 若品牌作为条件查询则不需聚合
            builder.aggregation(
                    AggregationBuilders.terms("aggTmId").field("tmId")
                            .subAggregation(AggregationBuilders.terms("aggTmName").field("tmName"))
                            .subAggregation(AggregationBuilders.terms("aggTmLogoUrl").field("tmLogoUrl"))
            );
        }

        //设置平台属性聚合
        builder.aggregation(
                AggregationBuilders.nested("aggAttrs","attrs")
                        .subAggregation(
                                AggregationBuilders.terms("aggAttrId").field("attrs.attrId")
                                        .subAggregation(AggregationBuilders.terms("aggAttrName").field("attrs.attrName"))
                                        .subAggregation(AggregationBuilders.terms("aggAttrValue").field("attrs.attrValue"))
                                        .size(50)
                        )
        );

        //4.设置平台属性查询条件
        //attr_颜色=23:红色  attr_操作系统=209:鸿蒙
        searchData.entrySet().stream().forEach(attrs->{
            String key = attrs.getKey();
            if (key.startsWith("attr_")){
                //分开获取平台属性id和属性值
                String value = attrs.getValue();
                if (!StringUtils.isEmpty(value)){
                    String[] split = value.split(":");
                    //构建平台属性组合查询
                    BoolQueryBuilder nestBoolQueryBuilder = QueryBuilders.boolQuery();
                    //获取参数中的平台属性id
                    nestBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                    //获取参数中的平台属性值
                    nestBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrValue",split[1]));
                    //把平台属性的组合查询放到最外层的组合查询中
                    //参数一: 域名  参数二: 查询条件  参数三: 默认
                    boolQuery.must(QueryBuilders.nestedQuery("attrs",nestBoolQueryBuilder, ScoreMode.None));
                }
            }
        });

        //5.设置价格查询条件
        // 0-500元  10000元以上
        String price = searchData.get("price");
        if (!StringUtils.isEmpty(price)){
            price = price.replaceAll("元","");
            price = price.replaceAll("以上","");
            //分开范围值
            String[] split = price.split("-");
            boolQuery.must(QueryBuilders.rangeQuery("price").gte(split[0]));
            if (split.length>1){
                boolQuery.must(QueryBuilders.rangeQuery("price").lt(split[1]));
            }
        }
        //6.排序:降序
        String sortField = searchData.get("sortField");
        String sortRule = searchData.get("sortRule");
        if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)){
            //自定义排序
            builder.sort(sortField,SortOrder.fromString(sortRule));
        }else {
            //默认排序, 新品展示排序
            builder.sort("id", SortOrder.DESC);
        }
        //7.分页排序
        //每页展示条数
        Integer size = 50;
        builder.size(size);
        //当前页
        String pageNum = searchData.get("pageNum");
        if (!StringUtils.isEmpty(pageNum)){
            int page = getPage(pageNum);
            //从下标几开始
            builder.from(size*(page-1));
        }

        //存储组合查询
        builder.query(boolQuery);
        //设置总的查询条件
        searchRequest.source(builder);
        //返回
        return searchRequest;
    }

    /**
     * 获取当前页
     * @param pageNum
     * @return
     */
    private int getPage(String pageNum) {
        try {
            int i = Integer.parseInt(pageNum);
            if (i >= 200 || i <= 0){
                return 1;
            }
            return i;
        } catch (NumberFormatException e) {
            //避免传的值有异常
            return 1;
        }
    }

    /**
     * 解析商品查询结果
     * @param search
     * @return
     */
    private Map<String, Object> getSearchResult(SearchResponse search) {
        Map<String,Object> result = new HashMap<>();
        ArrayList<Goods> goodsList = new ArrayList<>();
        //搜索命中的数据
        SearchHits hits = search.getHits();
        long totalHits = hits.getTotalHits();
        result.put("totalCount",totalHits);
        Iterator<SearchHit> iterator = hits.iterator();
        while (iterator.hasNext()){
            //搜索的每条信息
            SearchHit next = iterator.next();
            //原始信息
            String sourceAsString = next.getSourceAsString();
            //反序列化
            Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
            //存入集合
            goodsList.add(goods);
        }
        //保存商品列表
        result.put("goodsList",goodsList);
        //全部的聚合结果
        Aggregations aggregations = search.getAggregations();
        //解析品牌的聚合结果
        List<SearchResponseTmVo> searchResponseTmVoList = getTmAggResult(aggregations);
        //保存品牌列表
        result.put("searchResponseTmVoList",searchResponseTmVoList);
        //解析平台属性的结果
        List<SearchResponseAttrVo> searchAttrAggResult = getSearchAttrAggResult(aggregations);
        result.put("searchAttrAggResult",searchAttrAggResult);
        return result;
    }

    /**
     * 解析平台属性的结果
     * @param aggregations
     * @return
     */
    private List<SearchResponseAttrVo> getSearchAttrAggResult(Aggregations aggregations) {
        //通过别名获取nested类型的聚合结果
        ParsedNested aggAttrs = aggregations.get("aggAttrs");
        //获取子聚合的结果
        Aggregations subAggAttrs = aggAttrs.getAggregations();
        ParsedLongTerms aggAttrIds = subAggAttrs.get("aggAttrId");
        //获取平台属性id的聚合结果
        return aggAttrIds.getBuckets().stream().map(aggAttrId->{
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //获取每个属性id
            long attrId = aggAttrId.getKeyAsNumber().longValue();
            //保存属性id
            searchResponseAttrVo.setAttrId(attrId);
            //获取平台属性聚合
            Aggregations attrIdAggregations = aggAttrId.getAggregations();
            //获取聚合下的平台属性名称
            ParsedStringTerms aggAttrName = attrIdAggregations.get("aggAttrName");
            List<? extends Terms.Bucket> attrNameBuckets = aggAttrName.getBuckets();
            if (attrNameBuckets != null && !StringUtils.isEmpty(attrNameBuckets)){
                String attrName = attrNameBuckets.get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);
            }
            //获取聚合下的平台属性值
            ParsedStringTerms aggAttrValue = attrIdAggregations.get("aggAttrValue");
            List<? extends Terms.Bucket> attrValueBuckets = aggAttrValue.getBuckets();
            if (attrValueBuckets != null && !StringUtils.isEmpty(attrValueBuckets)){
                List<String> attrValueList = attrValueBuckets.stream().map(attrValues -> {
                    String attrValue = attrValues.getKeyAsString();
                    //返回属性值结果
                    return attrValue;
                }).collect(Collectors.toList());
                searchResponseAttrVo.setAttrValueList(attrValueList);
            }
            //返回平台属性聚合结果
            return searchResponseAttrVo;
        }).collect(Collectors.toList());

    }

    /**
     * 解析品牌的聚合结果
     * @param aggregations
     * @return
     */
    private List<SearchResponseTmVo> getTmAggResult(Aggregations aggregations) {
        //通过别名获取品牌id的聚合结果
        ParsedLongTerms aggTmId = aggregations.get("aggTmId");
        if (aggTmId == null){
            return null;
        }
        //遍历获取每个品牌的id和子聚合的结果
        return aggTmId.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //获取品牌的id
            long tmId = bucket.getKeyAsNumber().longValue();
            searchResponseTmVo.setTmId(tmId);
            //获取子聚合的结果
            ParsedStringTerms aggTmName = bucket.getAggregations().get("aggTmName");
            //确认品牌的名字不为空
            List<? extends Terms.Bucket> tmNameBuckets = aggTmName.getBuckets();
            if (tmNameBuckets != null && !tmNameBuckets.isEmpty()){
                //获取品牌的名字
                String tmName = tmNameBuckets.get(0).getKeyAsString();
                searchResponseTmVo.setTmName(tmName);
            }
            ParsedStringTerms aggTmLogoUrl = bucket.getAggregations().get("aggTmLogoUrl");
            //确认logo不为空
            List<? extends Terms.Bucket> logoUrlBuckets = aggTmLogoUrl.getBuckets();
            if (logoUrlBuckets != null && !logoUrlBuckets.isEmpty()){
                String tmLogoUrl = logoUrlBuckets.get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            }
            return searchResponseTmVo;
        }).collect(Collectors.toList());
    }
}
