package com.jack.gmall.web.controller;

import com.jack.gmall.item.feign.ItemFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/31
 * @Description : 前端商品详情控制层
 **/
@Controller
@RequestMapping("/page/item")
public class ItemController {

    @Autowired
    private ItemFeign itemFeign;

    /**
     * 展示商品列表接口
     * @param skuId
     * @return
     */
    @GetMapping
    public String item(Long skuId,
                                Model model){
        //远程调用 item-service 中的获取商品详情接口
        Map<String, Object> result = itemFeign.getItemInfo(skuId);
        //将结果存入model
        model.addAllAttributes(result);
        //打开页面
        return "item";
    }

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 根据模板生成商品详情静态页
     * @param skuId
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/createSkuHtml")
    public String createSkuHtml(Long skuId) throws Exception{
        //远程调用item-service查询商品详情数据
        Map<String, Object> result = itemFeign.getItemInfo(skuId);
        //初始化数据容器
        Context context = new Context();
        context.setVariables(result);
        //初始化文件对象(静态页面保存位置)
        File file = new File("F:\\",skuId+".html");
        //初始化数据输出流
        PrintWriter writer = new PrintWriter(file,"UTF-8");
        /**
         * 生成静态页面(参数):
         * 1. 使用哪个页面作为模板
         * 2. 使用哪个容器装数据
         * 3. 保存到哪里
         */
        templateEngine.process("itemModel",context,writer);
        //关闭输出流
        writer.flush();
        writer.close();
        return skuId+"生成静态页面成功";
    }
}
