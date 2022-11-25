package com.jack.gmall.product.service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * 首页使用的接口类
 */
public interface IndexService {

    /**
     * 获取首页的分类信息
     * @return
     */
    public List<JSONObject> indexCategory();

}
