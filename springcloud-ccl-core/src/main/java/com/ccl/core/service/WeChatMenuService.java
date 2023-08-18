package com.ccl.core.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ccl.bean.utils.R;
import com.ccl.core.entity.weChat.WeChatMenu;

/**
 * <p>
 * 微信菜单管理 服务类
 * </p>
 *
 * @author liuc
 * @since 2021-09-17
 */
public interface WeChatMenuService extends IService<WeChatMenu> {

    R createWeChatMenu();

    R delWeChatMenu();

}
