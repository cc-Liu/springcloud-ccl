package com.ccl.bean.vo.weixin;

import lombok.Data;

/**
 * @Author liuc
 * @Description 公众号消息推送模板内容
 * @Date 2023/8/18 16:22
 **/
@Data
public class KeyWordParams {
    /**
     * 内容
     */
    private String value;

    /**
     * 颜色(模板消息更新，此参数不再需要)
     */
//    private String color;

    /**
     * 参数类型 1:thing 2:amount 3:time
     */
    private String KeyWordType;

    public KeyWordParams(String value) {
        this.value = value;
    }

    public KeyWordParams(String value, String KeyWordType) {
        this.value = value;
        this.KeyWordType = KeyWordType;
    }

}
