package com.jack.gmall.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.jack.gmall.common.result.Result;
import com.jack.gmall.product.feign.IndexFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/1
 * @Description : 首页信息控制层
 **/
@Controller
@RequestMapping("/page/index")
public class IndexController {

    @Autowired
    private IndexFeign indexFeign;

    /**
     * 获取首页分类信息
     * @return
     */
    @GetMapping
    public String getIndexCategory(Model model){
        List<JSONObject> categoryList = indexFeign.getIndexCategory();
        model.addAttribute("categoryList",categoryList);
        return "index";
    }

}
