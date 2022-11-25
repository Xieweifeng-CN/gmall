package com.jack.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jack.gmall.model.product.BaseCategoryView;
import com.jack.gmall.product.mapper.BaseCategoryViewMapper;
import com.jack.gmall.product.service.IndexService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/31
 * @Description : 前台首页业务层
 **/
@Service
public class IndexServiceImpl implements IndexService {

    @Resource
    private BaseCategoryViewMapper baseCategoryViewMapper;

    /**
     * 获取首页的分类信息
     * @return
     */
    @Override
    public List<JSONObject> indexCategory() {
        //获取所有一级、二级、三级分类
        List<BaseCategoryView> baseCategoryViews =
                baseCategoryViewMapper.selectList(null);
        //分组/分桶
        Map<Long, List<BaseCategoryView>> baseCategory1Views =
                baseCategoryViews.stream().collect(
                        Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        //遍历一级分组
        return baseCategory1Views.entrySet().stream().map(category1Entry -> {
            //一级分类初始化
            JSONObject category1Json = new JSONObject();
            //一级分类的Id
            Long category1Id = category1Entry.getKey();
            category1Json.put("categoryId", category1Id);
            //获取同一个一级分类下的二级、三级分类
            List<BaseCategoryView> baseCategory2Entry = category1Entry.getValue();
            String category1Name = baseCategory2Entry.get(0).getCategory1Name();
            category1Json.put("categoryName", category1Name);
            //分组/分桶
            Map<Long, List<BaseCategoryView>> baseCategory2Views =
                    baseCategory2Entry.stream().collect(
                            Collectors.groupingBy(BaseCategoryView::getCategory2Id));

            //遍历二级分类
            List<JSONObject> category2JsonList =
                    baseCategory2Views.entrySet().stream().map(category2Entry -> {
                        //二级分类初始化
                        JSONObject category2Json = new JSONObject();
                        //二级分类id
                        Long category2Id = category2Entry.getKey();
                        category2Json.put("categoryId", category2Id);
                        //获取同一个一级二级分类下的三级分类
                        List<BaseCategoryView> baseCategory3Entry = category2Entry.getValue();
                        //获取二级分类的名字
                        String category2Name = baseCategory3Entry.get(0).getCategory2Name();
                        category2Json.put("categoryName", category2Name);

                        //遍历三级分类, 需要返回结果: 使用map(), foreach()没有返回结果
                        List<JSONObject> category3JsonList =
                                baseCategory3Entry.stream().map(baseCategoryView -> {
                                    //三级分类对象初始化
                                    JSONObject category3Json = new JSONObject();
                                    //获取三级分类id
                                    Long category3Id = baseCategoryView.getCategory3Id();
                                    category3Json.put("categoryId", category3Id);
                                    //获取三级分类名字
                                    String category3Name = baseCategoryView.getCategory3Name();
                                    category3Json.put("categoryName", category3Name);
                                    return category3Json;
                                }).collect(Collectors.toList());

                        //保存二级分类对应的所有三级分类集合
                        category2Json.put("childCategory", category3JsonList);
                        return category2Json;
                    }).collect(Collectors.toList());

            category1Json.put("childCategory", category2JsonList);
            return category1Json;
        }).collect(Collectors.toList());
    }
}
