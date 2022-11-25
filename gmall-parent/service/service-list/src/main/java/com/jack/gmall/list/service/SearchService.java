package com.jack.gmall.list.service;

import java.util.Map;

/**
 * 商品搜索接口
 */
public interface SearchService {

    /**
     * 获取搜索页面展示数据信息
     * @return
     */
    public Map<String, Object> search(Map<String, String> searchData);

}
