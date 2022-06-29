package com.atguigu.gulimall.gulimallauthserver.entity;

import lombok.Data;

@Data
public class ProviderToken {
    private String clientId; //客户端id
    private String redirectUri; //登陆后后的回调地址
    private String clientSecret; //密钥
    private String code;
    private String state;
}
