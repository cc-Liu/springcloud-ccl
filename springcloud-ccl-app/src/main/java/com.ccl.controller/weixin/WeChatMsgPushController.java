package com.ccl.controller.weixin;

import com.ccl.bean.utils.R;
import com.ccl.bean.vo.weixin.WeChatMsgPush;
import com.ccl.bean.vo.weixin.WxConfigParams;
import com.ccl.core.entity.weChat.TWxUserInfo;
import com.ccl.core.service.WeChatMsgPushService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Description: 微信小程序相关接口
 * @Author admin
 * @Date 2021/6/1 10:46
 * @Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/wxMsgPush")
public class WeChatMsgPushController {

    @Resource
    private WeChatMsgPushService weChatMsgPushService;

    @Resource
    private WxMpService wxService;

    @Resource
    private WxMpMessageRouter messageRouter;

    /**
     * @return com.furen.common.utils.R
     * @Author liuc
     * @Description 服务消息推送
     * @Date 2021/6/1 15:53
     * @Param [templateParams]
     **/
   /* @ApiOperation(value = "服务消息推送", notes = "")
    @PostMapping("/wxMsgPush")
    public R wxMsgPush(@RequestBody TemplateParams templateParams) {
        String accessToken = getAccessToken();
        if (accessToken != null) {
            return R.error().message("微信accessToken获取失败！");
        }
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;

        //拼接推送的模版
        WxMssVo wxMssVo = new WxMssVo();
        //wxMssVo.setTouser(templateParams.getOpenid());//用户的openid（要发送给那个用户，通常这里应该动态传进来的）
        wxMssVo.setTemplate_id("8-jkZf7Pmh_XCSPuqkbY9MNeYlvCYx3NSjiPWcoMqik");//订阅消息模板id
        //wxMssVo.setPage("pages/index/index");

        Map<String, TemplateData> m = new HashMap<>();
        m.put("character_string5", new TemplateData(templateParams.getOrderNum()));
        m.put("thing6", new TemplateData(templateParams.getProductName()));
        m.put("amount3", new TemplateData(templateParams.getOrderAmount()));
        m.put("time2", new TemplateData(templateParams.getCompletionTime()));
        m.put("thing4", new TemplateData(templateParams.getRemarks()));

        wxMssVo.setData(m);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, wxMssVo, String.class);
        HashMap<String, Object> hashMap = JSON.parseObject(responseEntity.getBody(), HashMap.class);
        Integer errcode = Integer.valueOf(hashMap.get("errcode").toString());
        String errmsg = (String) hashMap.get("errmsg");
        if (errcode == 0) {
            return R.ok().message(errcode + ":" + errmsg);
        } else {
            return R.error().message(errcode + ":" + errmsg);
        }
    }*/

    /**
     * @return java.lang.String
     * @Author liuc
     * @Description 获取小程序全局唯一后台接口调用凭据
     * @Date 2021/6/1 14:52
     * @Param []
     **/
    @GetMapping("/getAccessToken")
    public R getAccessToken() {
        return weChatMsgPushService.getAccessToken();
    }

    /**
     * @Author liuc
     * @Description 获取用户基本信息(UnionID机制)
     * @Date 2023/8/18 15:36
     * @Param [openId]
     * @return R<TWxUserInfo>
     **/
    @GetMapping("/getUserInfoByPublicOpenid")
    public R<TWxUserInfo> getUserInfoByPublicOpenid(@RequestParam String openId) {
        R<TWxUserInfo> wxUserInfo = weChatMsgPushService.getUserInfoByPublicOpenid(openId);
        return R.ok().resultData(wxUserInfo);
    }

    /**
     * @return
     * @description 微信公众号服务器配置校验token
     * @author: liuc
     * @date 2019-05-09 9:38
     */
    @GetMapping(value = "/checkToken",produces = "text/plain;charset=utf-8")
    public String authGet(@RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echostr) {

        log.info("\n接收到来自微信服务器的认证消息：[{}, {}, {}, {}]", signature,
                timestamp, nonce, echostr);
        if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
            throw new IllegalArgumentException("请求参数非法，请核实!");
        }

        if (wxService.checkSignature(timestamp, nonce, signature)) {
            log.info("echostr参数：{}",echostr);
            return echostr;
        }

        return "非法请求";
    }

    /**
     * @return void
     * @Author liuc
     * @Description 接受公众号返回的消息
     * @Date 2021/9/2 17:11
     * @Param [request:请求, response:回复]
     **/
    @PostMapping(value = "/checkToken",produces = "application/xml; charset=UTF-8")
    public String post(@RequestBody String requestBody,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("openid") String openid,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        log.info("\n接收微信请求：[openid=[{}], [signature=[{}], encType=[{}], msgSignature=[{}],"
                        + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ",
                openid, signature, encType, msgSignature, timestamp, nonce, requestBody);

        if (!wxService.checkSignature(timestamp, nonce, signature)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }

        String out = null;
        if (encType == null) {
            // 明文传输的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return "";
            }

            out = outMessage.toXml();
        } else if ("aes".equalsIgnoreCase(encType)) {
            // aes加密的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody, wxService.getWxMpConfigStorage(),
                    timestamp, nonce, msgSignature);
            log.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return "";
            }

            out = outMessage.toEncryptedXml(wxService.getWxMpConfigStorage());
        }

        log.info("\n组装回复信息：{}", out);
        return out;
    }

    private WxMpXmlOutMessage route(WxMpXmlMessage message) {
        try {
            return this.messageRouter.route(message);
        } catch (Exception e) {
            log.error("路由消息时出现异常！", e);
        }
        return null;
    }

    /**
     * @Author liuc
     * @Description 查询微信配置信息，如果没有则查询出来缓存到redis中
     * @Date 2022/3/31 9:30
     * @Param []
     * @return com.furen.common.utils.R
     **/
    @GetMapping("/selectWxConfigValue")
    public R selectWxConfigValue() {
        WxConfigParams wxConfigParams = weChatMsgPushService.selectWxConfigValue();
        return R.ok().resultData(wxConfigParams);
    }

    /**
     * @Author liuc
     * @Description 微信公众号消息推送
     * @Date 2022/3/31 9:29
     * @Param [weChatMsgPush]
     * @return com.furen.common.utils.R
     **/
    @PostMapping("/weChatMpeMsgPush")
    public R weChatMpeMsgPush(@RequestBody WeChatMsgPush weChatMsgPush) {
        return weChatMsgPushService.weChatMpeMsgPush(weChatMsgPush);
    }


    @PostMapping("/weChatMpeMsgPushTest")
    public R<TWxUserInfo> weChatMpeMsgPushTest(@RequestBody WeChatMsgPush weChatMsgPush) {
        R r = weChatMsgPushService.wxPublicMsgPushTest(weChatMsgPush);
        return R.ok().resultData(r);
    }

}