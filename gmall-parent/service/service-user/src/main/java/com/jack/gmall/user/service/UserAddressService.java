package com.jack.gmall.user.service;

import com.jack.gmall.model.user.UserAddress;

import java.util.List;

/**
 * 用户地址相关业务接口
 */
public interface UserAddressService {

    /**
     * 获取用户地址列表
     * @return
     */
    public List<UserAddress> getUserAddress();

}
