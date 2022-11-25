package com.jack.gmall.list.controller;

import com.jack.gmall.list.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/3
 * @Description :
 **/
@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public Map<String, Object> search(@RequestParam Map<String, String> searchData){
        return searchService.search(searchData);
    }
}
