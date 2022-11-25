package com.atguigu.gmall.oauth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jack.gmall.model.user.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息实体映射
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
