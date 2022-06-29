package com.atguigu.gulimall.gulimallthirdparty.service;

public interface MsgService {
    /**
     * 发送手机验证码
     * @param phone
     * @param code
     * @return
     */
    public boolean sendMessage(String phone, String code);
}
