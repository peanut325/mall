package com.atguigu.common.to.cart;

import lombok.Data;
import lombok.ToString;

/**
 * 用户信息
 * 由拦截器封装生成
 **/
@ToString
@Data
public class UserInfoTo {
    private Long userId;// 用户ID，登录状态下该值非空
    private String userKey; // 临时用户UUID，非登录状态下该值非空
    private boolean tempUser = false;// 判断客户端是否存在游客cookie（true：存在，不自动续期；false：不存在，需要分配一个）
}
