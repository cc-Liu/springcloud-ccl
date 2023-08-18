package com.ccl.core.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ccl.core.entity.weChat.WeChatMenu;

import java.util.List;


/**
 * <p>
 * 微信菜单管理 Mapper 接口
 * </p>
 *
 * @author liuc
 * @since 2021-09-17
 */
public interface WeChatMenuMapper extends BaseMapper<WeChatMenu> {

    List<WeChatMenu> selectWeChatMenuList();

    List<WeChatMenu> selectFirstWeChatMenu();

    List<WeChatMenu> selectSecondWeChatMenu();

    WeChatMenu selectWeChatMenuByMenuEventKey(String menuEventKey);
}
