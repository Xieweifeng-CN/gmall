package com.jack.gmall.user.feign;

import com.jack.gmall.common.result.Result;
import com.jack.gmall.model.user.UserAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 用户微服务远程调用feign接口
 */
@FeignClient(name = "service-user",url = "/api/user/feign",contextId = "userFeign")
public interface UserFeign {

    /**
     * 获取用户地址列表
     * @return
     */
    @GetMapping("/listUserAddress")
    public List<UserAddress> listUserAddress();

}
