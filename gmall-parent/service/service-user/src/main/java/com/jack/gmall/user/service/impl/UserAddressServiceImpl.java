package com.jack.gmall.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jack.gmall.model.user.UserAddress;
import com.jack.gmall.user.mapper.UserAddressMapper;
import com.jack.gmall.user.service.UserAddressService;
import com.jack.gmall.user.util.UserThreadLocalUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/9
 * @Description : 用户地址相关业务接口实现类
 **/
@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Resource
    private UserAddressMapper userAddressMapper;

    /**
     * 获取用户地址列表
     *
     * @return
     */
    @Override
    public List<UserAddress> getUserAddress() {
        List<UserAddress> userAddresses = userAddressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, UserThreadLocalUtil.get()));
        return userAddresses;
    }
}
