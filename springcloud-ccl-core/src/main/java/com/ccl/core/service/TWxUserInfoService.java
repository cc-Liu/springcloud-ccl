package com.ccl.core.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ccl.bean.utils.R;
import com.ccl.core.entity.weChat.TWxUserInfo;
import me.chanjar.weixin.mp.bean.result.WxMpUser;

/**
 * <p>
 * 微信用户信息表 服务类
 * </p>
 *
 * @author liuc
 * @since 2021-09-03
 */
public interface TWxUserInfoService extends IService<TWxUserInfo> {

    R subscribeForText(WxMpUser userWxInfo);

    R unSubscribeForText(String openId);

    R<Integer> insertWxUserInfo(TWxUserInfo wxUserInfo);

    R<Integer> bindingWxUserInfo(TWxUserInfo wxUserInfo);

    R<TWxUserInfo> checkUserIsSubscribe(String mobile);

    TWxUserInfo selectWxUserInfoByMobile(String mobile);
}
