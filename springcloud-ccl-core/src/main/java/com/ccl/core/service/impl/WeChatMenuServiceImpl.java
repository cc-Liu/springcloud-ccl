package com.ccl.core.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccl.bean.utils.R;
import com.ccl.bean.vo.weixin.WxConfigParams;
import com.ccl.core.entity.weChat.WeChatMenu;
import com.ccl.core.mapper.WeChatMenuMapper;
import com.ccl.core.service.WeChatMenuService;
import com.ccl.core.service.WeChatMsgPushService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 微信菜单管理 服务实现类
 *
 * @author liuc
 * @since 2021-09-17
 */
@Slf4j
@Service
public class WeChatMenuServiceImpl extends ServiceImpl<WeChatMenuMapper, WeChatMenu> implements WeChatMenuService {

    @Resource
    private WeChatMenuMapper weChatMenuMapper;

    @Resource
    private WxMpService wxService;

    @Resource
    private WeChatMsgPushService weChatMsgPushService;

    /**
     * @return com.furen.common.utils.R
     * @Author liuc
     * @Description 创建微信菜单
     * @Date 2021/9/17 10:04
     * @Param []
     **/
    @Override
    public R createWeChatMenu() {
        log.info("\n创建微信菜单");
        List<WeChatMenu> firstWeChatMenu = weChatMenuMapper.selectFirstWeChatMenu();
        List<WeChatMenu> secondWeChatMenu = weChatMenuMapper.selectSecondWeChatMenu();
        //将子菜单关联到主菜单中
        for (WeChatMenu firstWeChat : firstWeChatMenu) {
            for (WeChatMenu secondWeChat : secondWeChatMenu) {
                if (secondWeChat.getMenuLevel() == firstWeChat.getMenuId()) {
                    firstWeChat.setWeChatMenuVoList(secondWeChatMenu);
                }
            }
        }

        WxMenu menu = new WxMenu();
        List<WxMenuButton> mainButtonsList = new ArrayList<>(); //主菜单
        List<WxMenuButton> subButtonsList = new ArrayList<>();  //子菜单

        WxMenuButton mainButtons = new WxMenuButton();
        WxMenuButton subButtons = new WxMenuButton();

        for (WeChatMenu firstWeChat : firstWeChatMenu) {
            //主菜单
            mainButtons = matchMenuType(firstWeChat);

            if (!CollectionUtil.isEmpty(firstWeChat.getWeChatMenuVoList())) {
                //子菜单
                for (WeChatMenu weChatMenuVo : firstWeChat.getWeChatMenuVoList()) {
                    subButtons = matchMenuType(weChatMenuVo);
                    subButtonsList.add(subButtons);
                    mainButtons.setSubButtons(subButtonsList);
                }
            }
            mainButtonsList.add(mainButtons);
        }
        menu.setButtons(mainButtonsList);
        log.info("\n自定义菜单参数：{}", JSONUtil.parse(menu));
        String result = null;
        try {
            result = wxService.getMenuService().menuCreate(menu);
        } catch (WxErrorException e) {
            e.printStackTrace();
            log.error("\n自定义菜单创建失败：{}", e);
            return R.error().resultData(e);
        }
        return R.ok().resultData(result);
    }


    /**
     * @Author liuc
     * @Description 菜单类型匹配
     * @Date 2021/9/27 16:48
     * @Param [menuType]
     * @return java.lang.String
     **/
    private WxMenuButton matchMenuType(WeChatMenu weChatMenu) {
        WxConfigParams wxConfigParams = weChatMsgPushService.selectWxConfigValue();
        WxMenuButton wxMenuButton = new WxMenuButton();
        //菜单的响应动作类型匹配
        //菜单类型(二级底部菜单为0) 0:底部菜单 1:url类型 2:回复文字类型 3:小程序 4:回复图片类型
        String menuTypeStr = WxConsts.MenuButtonType.VIEW;
        switch (weChatMenu.getMenuType()) {
            case 1://url类型
                menuTypeStr = WxConsts.MenuButtonType.VIEW;
                wxMenuButton.setUrl(weChatMenu.getMenuUrl());
                break;
            case 2://回复文字类型
                menuTypeStr = WxConsts.MenuButtonType.CLICK;
                wxMenuButton.setKey(weChatMenu.getMenuEventKey());
                break;
            case 3://小程序
                menuTypeStr = WxConsts.MenuButtonType.MINIPROGRAM;
                wxMenuButton.setUrl("https://www.baidu.com/");
                wxMenuButton.setAppId(wxConfigParams.getAppletAppId());
                wxMenuButton.setPagePath(weChatMenu.getMenuUrl());
                break;
            case 4://回复图片类型
                menuTypeStr = WxConsts.MenuButtonType.VIEW_LIMITED;
                break;
            default:
                break;
        }
        wxMenuButton.setName(weChatMenu.getMenuName());
        wxMenuButton.setType(menuTypeStr);
        return wxMenuButton;
    }


    /**
     * @Author liuc
     * @Description 自定义菜单删除接口
     * @Date 2021/9/24 16:52
     **/
    @Override
    public R delWeChatMenu() {
        log.info("\n删除微信自定义菜单");
        try {
            wxService.getMenuService().menuDelete();
        } catch (WxErrorException e) {
            log.error("\n自定义菜单删除失败：{}", e);
        }
        return R.ok().resultData("自定义菜单删除成功");
    }

}
