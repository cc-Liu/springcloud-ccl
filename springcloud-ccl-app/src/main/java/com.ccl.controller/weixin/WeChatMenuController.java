package com.ccl.controller.weixin;


import com.ccl.bean.utils.R;
import com.ccl.core.service.WeChatMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 微信菜单管理 前端控制器
 *
 * @author liuc
 * @since 2021-09-17
 */
@Slf4j
@RestController
@RequestMapping("/weChatMenu")
public class WeChatMenuController {

    @Resource
    private WeChatMenuService weChatMenuService;

    /**
     * @Author liuc
     * @Description 创建微信菜单
     * @Date 2023/8/18 17:21
     **/
    @PostMapping("/createWeChatMenu")
    public R createWeChatMenu() {
        return weChatMenuService.createWeChatMenu();
    }

    /**
     * @Author liuc
     * @Description 菜单删除
     * @Date 2023/8/18 17:21
     **/
    @GetMapping("/delWeChatMenu")
    public R delWeChatMenu() {
        return weChatMenuService.delWeChatMenu();
    }
}

