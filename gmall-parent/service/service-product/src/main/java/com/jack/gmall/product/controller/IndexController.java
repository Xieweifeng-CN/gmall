package com.jack.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.jack.gmall.common.cache.Java0509Cache;
import com.jack.gmall.product.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/31
 * @Description : 前台首页控制层
 **/
@RestController
@RequestMapping("/api/index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    /**
     * 获取首页的分类信息
     * @return
     */
    @Java0509Cache(prefix = "getIndexCategory:")
    @GetMapping("/getIndexCategory")
    public List<JSONObject> getIndexCategory(){
        return indexService.indexCategory();
    }

}
