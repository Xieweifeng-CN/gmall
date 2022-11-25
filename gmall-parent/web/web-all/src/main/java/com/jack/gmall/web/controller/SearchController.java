package com.jack.gmall.web.controller;

import com.jack.gmall.search.feign.SearchFeign;
import com.jack.gmall.web.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/4
 * @Description :
 **/
@Controller
@RequestMapping("/page/search")
public class SearchController {

    @Autowired
    private SearchFeign searchFeign;

    /**
     * 商品搜索和条件查询
     *
     * @param searchData
     * @param model
     * @return
     */
    @GetMapping
    public String list(@RequestParam Map<String, String> searchData, Model model) {
        //远程调用商品搜索接口
        Map<String, Object> result = searchFeign.search(searchData);
        //返回搜索结果
        model.addAllAttributes(result);
        //返回搜索参数
        model.addAttribute("searchData", searchData);
        //条件查询请求地址
        String searchUrl = getSearchUrl(searchData);
        model.addAttribute("searchUrl",searchUrl);
        //排序查询请求地址
        String sortUrl = getSortUrl(searchData);
        model.addAttribute("sortUrl",sortUrl);
        //获取总记录数
        Object totalCount = result.get("totalCount");
        //获取当前页码
        int pageNum = getPage(searchData.get("pageNum"));
        //获取分页
        Page pageInfo = new Page<>(Long.valueOf(totalCount.toString()),pageNum,50);
        model.addAttribute("pageInfo",pageInfo);
        return "list";
    }

    /**
     * 获取当前页码
     * @param pageNum
     * @return
     */
    private int getPage(String pageNum) {
        try {
            if (!StringUtils.isEmpty(pageNum)){
                int i = Integer.parseInt(pageNum);
                if (i > 0 && i <= 200){
                    return i;
                }
            }
        } catch (Exception e) {
            return 1;
        }
        return 1;
    }

    /**
     * 拼接排序查询地址
     * @param searchData
     * @return
     */
    private String getSortUrl(Map<String, String> searchData) {
        //流式编程需要线程安全
        StringBuffer sb = new StringBuffer("/page/search?");
        searchData.entrySet().stream().forEach(param -> {
            String key = param.getKey();
            String value = param.getValue();
            if (!StringUtils.isEmpty(key) && !"sortField".equals(key) && !"sortRule".equals(key)) {
                sb.append(key).append("=").append(value).append("&");
            }
        });
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

    /**
     * 拼接条件查询请求地址
     *
     * @param searchData
     * @return
     */
    private String getSearchUrl(Map<String, String> searchData) {
        //流式编程需要线程安全
        StringBuffer sb = new StringBuffer("/page/search?");
        searchData.entrySet().stream().forEach(param -> {
            String key = param.getKey();
            String value = param.getValue();
            if (!StringUtils.isEmpty(key) && !"pageNum".equals(key)) {
                sb.append(key).append("=").append(value).append("&");
            }
        });
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

}
