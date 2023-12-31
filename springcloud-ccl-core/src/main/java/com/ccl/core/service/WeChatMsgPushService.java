package com.ccl.core.service;


import com.ccl.bean.utils.R;
import com.ccl.bean.vo.weixin.TemplateParams;
import com.ccl.bean.vo.weixin.WeChatMsgPush;
import com.ccl.bean.vo.weixin.WxConfigParams;
import com.ccl.core.entity.weChat.TWxUserInfo;

public interface WeChatMsgPushService {

    //获取小程序全局唯一后台接口调用凭据
    R<String> getAccessToken();

    //根据用户公众号Openid获取信息
    R<TWxUserInfo> getUserInfoByPublicOpenid(String openId);

    //公众号消息推送
    R wxPublicMsgPush(TemplateParams templateParams);

    //公众号消息推送(参数组装)
    R weChatMpeMsgPush(WeChatMsgPush weChatMsgPush);

    WxConfigParams selectWxConfigValue();

    R wxPublicMsgPushTest(WeChatMsgPush weChatMsgPush);
}
