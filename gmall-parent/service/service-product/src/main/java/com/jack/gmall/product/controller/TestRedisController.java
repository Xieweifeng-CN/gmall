package com.jack.gmall.product.controller;

import com.jack.gmall.common.result.Result;
import com.jack.gmall.product.service.impl.TestRedisServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/28
 * @Description :
 **/
@RestController
@RequestMapping("/redis/product")
public class TestRedisController {

    @Autowired
    private TestRedisServiceImpl testRedisService;

    /**
     * 分布式锁测试
     * @return
     */
    @GetMapping
    public Result testSendRedis(){
        testRedisService.sendRedis();
        return Result.ok();
    }
}
