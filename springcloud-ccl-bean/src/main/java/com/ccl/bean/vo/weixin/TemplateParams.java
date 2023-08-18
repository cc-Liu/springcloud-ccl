package com.ccl.bean.vo.weixin;

import lombok.Data;

import java.util.List;

/**
 * @Description: 公众号消息推送对象
 * @Author admin
 * @Date 2021/6/1 15:37
 * @Version 1.0
 */
@Data
public class TemplateParams {

    /**
     * 模板消息内容
     */
    private List<KeyWordParams> valueList;

    /**
     * 模板类型 1:能源消费 2:非油品消费 3:充值
     */
    private Integer templateType;

    /**
     * 会员手机号
     */
    private String mobile;

    /**
     * 模板id
     */
    private String templateId;

}
