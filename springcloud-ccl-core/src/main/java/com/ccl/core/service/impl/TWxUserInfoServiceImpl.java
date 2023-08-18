package com.ccl.core.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccl.bean.utils.CodeEnum;
import com.ccl.bean.utils.R;
import com.ccl.core.entity.weChat.TWxUserInfo;
import com.ccl.core.mapper.TWxUserInfoMapper;
import com.ccl.core.service.TWxUserInfoService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * <p>
 * 微信用户信息表 服务实现类
 * </p>
 *
 * @author liuc
 * @since 2021-09-03
 */
@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service
public class TWxUserInfoServiceImpl extends ServiceImpl<TWxUserInfoMapper, TWxUserInfo> implements TWxUserInfoService {

    @Resource
    private TWxUserInfoMapper wxUserInfoMapper;

    /**
     * @return com.furen.common.utils.R
     * @Author liuc
     * @Description 订阅事件
     * @Date 2021/9/6 11:27
     * @Param [paramsMap]
     **/
    @Override
    public R subscribeForText(WxMpUser userWxInfo) {
        int count = 0;
        Date nowDate = new Date();
        TWxUserInfo wxUserInfo = copyTWxUserInfoVoToWxMpUser(userWxInfo);
        try {
            TWxUserInfo wxUserInfoVoSel = wxUserInfoMapper.selectWxUserInfoByUnionId(userWxInfo.getUnionId());
            wxUserInfo.setIsSubscribe(CodeEnum.COMMON_ONE.getCode());
            if (wxUserInfoVoSel == null) {
                wxUserInfo.setCreateTime(nowDate);
                wxUserInfo.setUpdateTime(nowDate);
                //第一次关注公众号并且未使用过小程序
                count = wxUserInfoMapper.insert(wxUserInfo);
            } else {
                //关注过小程序或之前关注过公众号
                wxUserInfo.setWxUserId(wxUserInfoVoSel.getWxUserId());
                wxUserInfo.setUpdateTime(nowDate);
                count = wxUserInfoMapper.updateById(wxUserInfo);
            }
        } catch (Exception e) {
            log.error("更新微信用户信息失败[{}]", e);
        }
        if (count > 0) {
            log.info("更新微信用户信息成功:[{}]");
            return R.ok().resultData(count).message("更新微信用户信息成功");
        } else {
            log.info("更新微信用户信息失败:[{}]");
            return R.error().resultData(count).message("更新微信用户信息失败");
        }
    }


    /**
     * @Author liuc
     * @Description 取消订阅
     * @Date 2021/9/6 16:50
     * @Param [paramsMap]
     * @return com.furen.common.utils.R
     **/
    @Override
    public R unSubscribeForText(String openId) {
        //用户公众号openId
        TWxUserInfo wxUserInfo = new TWxUserInfo();
        wxUserInfo.setPublicOpenid(openId);
        wxUserInfo.setIsSubscribe(CodeEnum.COMMON_ZERO.getCode());
        wxUserInfo.setUpdateTime(new Date());
        int count = wxUserInfoMapper.updWxUserInfoByPublicOpenid(wxUserInfo);
        if (count > 0) {
            return R.ok().resultData(count).message("更新微信用户信息成功");
        } else {
            return R.error().resultData(count).message("更新微信用户信息失败");
        }
    }


    /**
     * @Author liuc
     * @Description 新增微信用户信息并且返回主键
     * @Date 2021/9/7 15:35
     * @Param [wxUserInfoVo]
     * @return com.furen.common.utils.R
     **/
    @Override
    public R<Integer> insertWxUserInfo(TWxUserInfo wxUserInfo) {
        Date nowDate = new Date();
        wxUserInfo.setCreateTime(nowDate);
        wxUserInfo.setUpdateTime(nowDate);
        Integer count = wxUserInfoMapper.insertWxUserInfo(wxUserInfo);
        if(count > 0){
            return R.ok().resultData(wxUserInfo.getWxUserId());
        }else {
            return R.error().resultData(wxUserInfo.getWxUserId()).message("信息新增失败");
        }

    }

    /**
     * @Author liuc
     * @Description 绑定微信用户信息
     * @Date 2021/9/7 16:29
     * @Param [appletOpenid, unionId]
     * @return com.furen.common.utils.R<java.lang.Integer>
     **/
    @Override
    public R<Integer> bindingWxUserInfo(TWxUserInfo wxUserInfo) {
        TWxUserInfo wxUserInfoSel = wxUserInfoMapper.selectWxUserInfoByUnionId(wxUserInfo.getUnionId());
        Integer wxUserId;
        Date nowDate = new Date();
        if (wxUserInfoSel == null) {
            wxUserInfo.setIsSubscribe(CodeEnum.COMMON_ZERO.getCode());
            wxUserInfo.setCreateTime(nowDate);
            wxUserInfo.setUpdateTime(nowDate);
            wxUserInfoMapper.insertWxUserInfo(wxUserInfo);
            wxUserId = wxUserInfo.getWxUserId();
        } else {
            TWxUserInfo wxUserInfoUpd = new TWxUserInfo();
            wxUserInfoUpd.setWxUserId(wxUserInfoSel.getWxUserId());
            wxUserInfoUpd.setAppletOpenid(wxUserInfoUpd.getAppletOpenid());
            wxUserInfoUpd.setUpdateTime(nowDate);
            wxUserInfoMapper.updateById(wxUserInfoUpd);
            wxUserId = wxUserInfoSel.getWxUserId();
        }
        return R.ok().resultData(wxUserId);
    }


    /**
     * @Author liuc
     * @Description 校验用户是否关注了公众号
     * @Date 2021/9/8 9:06
     * @Param [memberNum]
     * @return com.furen.common.utils.R<java.lang.Boolean>
     **/
    @Override
    public R<TWxUserInfo> checkUserIsSubscribe(String mobile) {
        TWxUserInfo wxUserInfo = wxUserInfoMapper.selectWxUserInfoByMobile(mobile);
        if (wxUserInfo != null) {
            if (wxUserInfo.getIsSubscribe() == CodeEnum.COMMON_ONE.getCode()) {
                return R.ok().resultData(wxUserInfo);
            }
        }
        return R.error().resultData(wxUserInfo);
    }

    /**
     * @Author liuc
     * @Description 根据memberNum查询用户微信信息
     * @Date 2021/9/8 11:22
     * @Param [memberNum]
     * @return com.furen.member.vo.wxUserInfo.TWxUserInfoVo
     **/
    @Override
    public TWxUserInfo selectWxUserInfoByMobile(String mobile) {
        TWxUserInfo wxUserInfo = wxUserInfoMapper.selectWxUserInfoByMobile(mobile);
        return wxUserInfo;
    }


    /**
     * @Author liuc
     * @Description 转换
     * @Date 2021/9/23 15:40
     * @Param [user]
     * @return com.furen.member.entity.TWxUserInfo
     **/
    private TWxUserInfo copyTWxUserInfoVoToWxMpUser(WxMpUser user){
        TWxUserInfo wxUserInfo = new TWxUserInfo();
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
        return wxUserInfo;
    }
}
