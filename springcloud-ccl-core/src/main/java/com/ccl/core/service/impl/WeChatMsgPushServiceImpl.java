package com.ccl.core.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.ccl.bean.utils.R;
import com.ccl.bean.vo.weixin.KeyWordParams;
import com.ccl.bean.vo.weixin.TemplateParams;
import com.ccl.bean.vo.weixin.WeChatMsgPush;
import com.ccl.bean.vo.weixin.WxConfigParams;
import com.ccl.core.entity.weChat.TWxUserInfo;
import com.ccl.core.service.TWxUserInfoService;
import com.ccl.core.service.WeChatMsgPushService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Transactional(rollbackFor = Exception.class)
@Slf4j
@Service
public class WeChatMsgPushServiceImpl implements WeChatMsgPushService {

    /*@Resource
    private RedisClient redisClient;

    @Resource
    private OpenPlatformFeignService openPlatformFeignService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;*/

    @Resource
    private TWxUserInfoService wxUserInfoService;

    @Resource
    private WxMpService wxService;


    /**
     * @return com.furen.common.utils.R<java.lang.String>
     * @Author liuc
     * @Description 获取小程序全局唯一后台接口调用凭据
     * @Date 2021/9/7 10:10
     * @Param []
     **/
    @Override
    public R<String> getAccessToken() {
        String accessToken = "";
        try {
            accessToken = wxService.getAccessToken();
        } catch (WxErrorException e) {
            log.error("\n获取accessToken失败：{}", e);
        }
        return R.ok().resultData(accessToken);
    }

    /**
     * @return com.furen.common.utils.R<java.util.HashMap < java.lang.String, java.lang.Object>>
     * @Author liuc
     * @Description 根据用户公众号Openid获取信息 postForEntity
     * @Date 2021/9/7 10:18 TWxUserInfo
     * @Param [openId]
     **/
    @Override
    public R<TWxUserInfo> getUserInfoByPublicOpenid(String openId) {
        WxMpUser user;
        TWxUserInfo wxUserInfo = new TWxUserInfo();
        try {
            user = wxService.getUserService().userInfo(openId);
            wxUserInfo.setIsSubscribe(user.getSubscribe().equals(true) ? 1 : 0);
            wxUserInfo.setPublicOpenid(user.getOpenId());
            wxUserInfo.setNickname(user.getNickname());
            wxUserInfo.setSex(user.getSex());
            wxUserInfo.setLanguage(user.getLanguage());
            wxUserInfo.setCity(user.getCity());
            wxUserInfo.setProvince(user.getProvince());
            wxUserInfo.setCountry(user.getCountry());
            wxUserInfo.setHeadimgurl(user.getHeadImgUrl());
            wxUserInfo.setSubscribeTime(DateUtil.date(Long.valueOf(user.getSubscribeTime() + "000")));
            wxUserInfo.setUnionId(user.getUnionId());
            wxUserInfo.setRemark(user.getRemark());
            wxUserInfo.setGroupId(user.getGroupId());
            String tagidStr = Convert.toStr(user.getPrivileges());
            wxUserInfo.setTagidList(tagidStr);
            wxUserInfo.setSubscribeScene(user.getSubscribeScene());
            wxUserInfo.setQrScene(user.getQrScene());
            wxUserInfo.setQrSceneStr(user.getQrSceneStr());
        } catch (WxErrorException e) {
            log.error("\n获取用户信息错误：{}", e);
        }
        return R.ok().resultData(wxUserInfo);
    }

    /**
     * @return com.furen.common.utils.R
     * @Author liuc
     * @Description 公众号消息推送
     * @Date 2021/9/7 10:19
     * @Param [templateParams]
     **/
    /*@Async
    @Override
    public R wxPublicMsgPush(TemplateParams templateParams) {
        log.info("\n公众号消息推送参数：{}", JSONUtil.parse(templateParams));
        WxConfigParams wxConfigParams = selectWxConfigValue();
        R<TWxUserInfoVo> tWxUserInfoVoR = wxUserInfoService.checkUserIsSubscribe(templateParams.getMobile());
        if (!tWxUserInfoVoR.getSuccess()) {
            return R.error().resultData("该用户没有关注公众号");
        }
        TWxUserInfoVo wxUserInfoVo = tWxUserInfoVoR.getResultData();
        List<WxMpTemplateData> data = new ArrayList<>();
        //模板内容
        List<KeyWordParams> valueList = templateParams.getValueList();
        //模板id
        String first = "";
        String remark = "";
        //模板类型 1:消费 2:充值 3:退款
        switch (templateParams.getTemplateType()) {
            case (1): //1:消费
                first = wxConfigParams.getXfFirstData();
                remark = templateParams.getRemark() + "消费";
                break;
            case (2): //2:充值
                first = wxConfigParams.getCzFirstData();
                remark = templateParams.getRemark() + "充值";
                break;
            case (3): //3:退款
                first = wxConfigParams.getTkFirstData();
                remark = templateParams.getRemark() + "退款";
                break;
        }
        data.add(new WxMpTemplateData("remark", remark, "#FF0000"));
        data.add(new WxMpTemplateData("first", first, "#FF0000"));


        int i = 1;
        //组装推送信息
        for (KeyWordParams keyWordParams : valueList) {
            data.add(new WxMpTemplateData("keyword" + i, keyWordParams.getValue(), keyWordParams.getColor()));
            i++;
        }
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(wxUserInfoVo.getPublicOpenid())
                .templateId(templateParams.getTemplateId())
                .miniProgram(new WxMpTemplateMessage.MiniProgram(wxConfigParams.getAppletAppid(), wxConfigParams.getAppletUrl(), false))
                .build();
        templateMessage.setData(data);

        String msgId = null;
        try {
            msgId = this.wxService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            log.error("\n模板发送失败：{}", e);
            return R.error().resultData("模板发送失败");
        }
        return R.ok().resultData(msgId);
    }*/

    @Async
    @Override
    public R wxPublicMsgPush(TemplateParams templateParams) {
        log.info("\n公众号消息推送参数：{}", JSONUtil.parse(templateParams));
        WxConfigParams wxConfigParams = selectWxConfigValue();
        R<TWxUserInfo> tWxUserInfoVoR = wxUserInfoService.checkUserIsSubscribe(templateParams.getMobile());
        if (!tWxUserInfoVoR.getSuccess()) {
            return R.error().resultData("该用户没有关注公众号");
        }
        TWxUserInfo wxUserInfoVo = tWxUserInfoVoR.getResultData();
        List<WxMpTemplateData> data = new ArrayList<>();
        //模板内容
        List<KeyWordParams> valueList = templateParams.getValueList();
        int i = 1;
        //组装推送信息
        for (KeyWordParams keyWordParams : valueList) {
            data.add(new WxMpTemplateData(keyWordParams.getKeyWordType(), keyWordParams.getValue()));
            i++;
        }
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(wxUserInfoVo.getPublicOpenid())
                .templateId(templateParams.getTemplateId())
                .miniProgram(new WxMpTemplateMessage.MiniProgram(wxConfigParams.getAppletAppId(), wxConfigParams.getAppletUrl(), false))
                .build();
        templateMessage.setData(data);

        String msgId = null;
        try {
            msgId = this.wxService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            log.error("\n模板发送失败：{}", e);
            return R.error().resultData("模板发送失败");
        }
        return R.ok().resultData(msgId);
    }

    /**
     * @Author liuc
     * @Description 微信公众号消息推送
     * @Date 2022/3/31 9:18
     * @Param [weChatMsgPush]
     * @return com.furen.common.utils.R
     **/
    @Override
    public R weChatMpeMsgPush(WeChatMsgPush weChatMsgPush) {
        //付款明细 支付方式 + 金额
        String keyWordParams2 = weChatMsgPush.getPayType() + "支付" + weChatMsgPush.getPayPrice() + "元";
        WxConfigParams wxConfigParams = selectWxConfigValue();
        //公众号推送消息
        TemplateParams templateParams = new TemplateParams();
        List<KeyWordParams> valueList = new ArrayList<>();
        valueList.add(new KeyWordParams(weChatMsgPush.getSiteName(),"thing2"));
        valueList.add(new KeyWordParams(keyWordParams2,"thing4"));
        valueList.add(new KeyWordParams(weChatMsgPush.getTemplateType(),"thing3"));
        if (StringUtils.isNotBlank(weChatMsgPush.getBalance())) {
            //没有余额则使用另一套模板
            valueList.add(new KeyWordParams(weChatMsgPush.getBalance(),"amount5"));
            templateParams.setTemplateId(wxConfigParams.getBalanceTemplateId());
        }else {
            templateParams.setTemplateId(wxConfigParams.getNoBalanceTemplateId());
        }
        valueList.add(new KeyWordParams(weChatMsgPush.getPayTime(),"time10"));
        templateParams.setMobile(weChatMsgPush.getMobile());
        templateParams.setValueList(valueList);
        return wxPublicMsgPush(templateParams);
    }

    /**
     * @Author liuc
     * @Description 查询微信配置信息，如果没有则查询出来缓存到redis中
     * @Date 2022/3/24 16:12
     * @Param []
     * @return com.furen.member.weixin.WxConfigParams
     **/
    public WxConfigParams selectWxConfigValue() {
        /*Map<Object, Object> mapData = redisClient.getMapData(WechatInterface.SYS_CONFIG, WechatInterface.WX_CONFIG);
        if(mapData == null || mapData.size() <= 0){
            List<BaseSysCodeVo> wxDataList = openPlatformFeignService.selectThreeConfig(WechatInterface.WX_CONFIG).getResultData();
            Map<String, String> map = Optional.ofNullable(wxDataList).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(BaseSysCodeVo::getCode, BaseSysCodeVo::getName));
            stringRedisTemplate.opsForHash().putAll("sys_config:" + WechatInterface.WX_CONFIG, map);
            mapData = redisClient.getMapData(WechatInterface.SYS_CONFIG, WechatInterface.WX_CONFIG);
        }
        WxConfigParams wxConfigParams = BeanUtil.fillBeanWithMap(mapData, new WxConfigParams(), true, false);*/
        return new WxConfigParams();
    }

    @Override
    public R wxPublicMsgPushTest(WeChatMsgPush weChatMsgPush) {
        TemplateParams templateParams = new TemplateParams();
        List<KeyWordParams> valueList2 = new ArrayList<>();
        valueList2.add(new KeyWordParams(weChatMsgPush.getSiteName()));
        valueList2.add(new KeyWordParams(weChatMsgPush.getPayType()));
        valueList2.add(new KeyWordParams(weChatMsgPush.getPayPrice()));
        valueList2.add(new KeyWordParams(weChatMsgPush.getPayTime()));
        templateParams.setTemplateId("lLxcigAGjHDTyb10QEROrYdO6B3KSrUjAKG0vLezqBk");
        templateParams.setMobile(weChatMsgPush.getMobile());
        templateParams.setValueList(valueList2);


        R<TWxUserInfo> tWxUserInfoR = wxUserInfoService.checkUserIsSubscribe(templateParams.getMobile());
        if (tWxUserInfoR.getSuccess()) {
            TWxUserInfo wxUserInfoVo = tWxUserInfoR.getResultData();
            List<WxMpTemplateData> data = new ArrayList<>();
            //模板内容
            List<KeyWordParams> valueList = templateParams.getValueList();

            int i = 1;
            //组装推送信息
            for (KeyWordParams keyWordParams : valueList) {
                data.add(new WxMpTemplateData(keyWordParams.getKeyWordType() + i, keyWordParams.getValue()));
                i++;
            }
            WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                    .toUser(wxUserInfoVo.getPublicOpenid())
                    .templateId(templateParams.getTemplateId())
                    .miniProgram(new WxMpTemplateMessage.MiniProgram("wxf4d3095010e4413c", "", false))
                    .build();
            templateMessage.setData(data);

            String msgId = null;
            try {
                msgId = this.wxService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            } catch (WxErrorException e) {
                log.error("\n模板发送失败：{}", e);
                return R.error().resultData("模板发送失败");
            }
            log.info("\n公众号通知：{}", templateParams.getMobile());
            return R.ok().resultData(msgId);
        }
        return R.error().resultData("该用户没有关注公众号");
    }
}
