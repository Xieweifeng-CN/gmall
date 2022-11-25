package com.jack.gmall.user.controller;

import com.jack.gmall.common.result.Result;
import com.jack.gmall.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/9
 * @Description :
 **/
@RestController
@RequestMapping("/api/user")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    /**
     * 获取用户地址列表
     * @return
     */
    @GetMapping("/listUserAddress")
    public Result listUserAddress(){
        return Result.ok(userAddressService.getUserAddress());
    }
}
