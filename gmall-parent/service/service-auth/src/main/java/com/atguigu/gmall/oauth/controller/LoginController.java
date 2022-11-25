package com.atguigu.gmall.oauth.controller;

import com.atguigu.gmall.oauth.service.LoginService;
import com.atguigu.gmall.oauth.util.AuthToken;
import com.jack.gmall.common.result.Result;
import com.jack.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/7
 * @Description : 自定义的登录模块
 **/
@RestController
@RequestMapping("/user/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户登录
     * 正常用post请求
     * @return
     */
    @GetMapping
    public Result login(String username, String password, HttpServletRequest request){
        AuthToken token = loginService.login(username, password);
        //获取请求的ip地址
        String ipAddress = IpUtil.getIpAddress(request);
        //将token和ip地址绑定存放到redis中
        stringRedisTemplate.opsForValue().set(ipAddress,token.getAccessToken());
        //返回
        return Result.ok(token);
    }

}
