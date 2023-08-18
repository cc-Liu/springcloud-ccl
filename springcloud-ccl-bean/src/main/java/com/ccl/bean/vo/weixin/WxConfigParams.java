package com.ccl.bean.vo.weixin;

import lombok.Data;

/**
 * @Author liuc
 * @Description 微信参数
 * @Date 2022/3/24 15:34
 * @Param
 * @return
 **/
@Data
public class WxConfigParams {

    /**
     * 小程序appId
     **/
    private String appletAppId;

    /**
     * 小程序secret
     **/
    private String appletSecret;

    /**
     * 开发者密码(AppSecret)
     **/
    private String appSecret;

    /**
     * 开发者ID
     **/
    private String appId;

    /**
     * 小程序路径
     **/
    private String appletUrl;


    /**
     * 用户公众号openid
     **/
    private String publicOpenid;

    /**
     * 令牌(Token)
     **/
    private String token;

    /**
     * 消息加解密密钥(EncodingAESKey)
     **/
    private String aesKey;


    /**
     * 用微信关注自动回复
     **/
    private String wxFollowReply;

    /**
     * 充值模板头部
     **/
    private String czFirstData;

    /**
     * 消费模板头部
     **/
    private String xfFirstData;

    /**
     * 无余额消息模板
     **/
    private String NoBalanceTemplateId;

    /**
     * 有余额消息模板
     **/
    private String balanceTemplateId;

    /**
     * 订单退款头部
     **/
    private String tkFirstData;

    /**
     * 公众号自动回复
     **/
    private String autoReply;
}
