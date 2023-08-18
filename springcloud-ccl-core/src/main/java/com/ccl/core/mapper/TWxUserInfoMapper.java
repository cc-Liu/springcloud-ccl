package com.ccl.core.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ccl.core.entity.weChat.TWxUserInfo;

/**
 * <p>
 * 微信用户信息表 Mapper 接口
 * </p>
 *
 * @author liuc
 * @since 2021-09-03
 */
public interface TWxUserInfoMapper extends BaseMapper<TWxUserInfo> {

    TWxUserInfo selectWxUserInfoByUnionId(String unionId);

    Integer insertWxUserInfo(TWxUserInfo wxUserInfo);

    Integer updWxUserInfoByUnionId(TWxUserInfo wxUserInfo);

    TWxUserInfo selectWxUserInfoByMobile(String mobile);

    Integer updWxUserInfoByPublicOpenid(TWxUserInfo wxUserInfo);

}
