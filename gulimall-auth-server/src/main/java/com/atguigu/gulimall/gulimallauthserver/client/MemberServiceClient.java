package com.atguigu.gulimall.gulimallauthserver.client;

import com.atguigu.common.to.member.GiteeUserTo;
import com.atguigu.common.to.member.UserLoginTo;
import com.atguigu.common.to.member.UserRegisterTo;
import com.atguigu.common.to.member.WBSocialUserTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
@Component
public interface MemberServiceClient {

    @PostMapping("/member/member/register")
    public R register(@RequestBody UserRegisterTo userRegisterTo);

    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginTo userLoginTo);

    @PostMapping("/member/member/oauth2/login")
    public R loginWB(@RequestBody WBSocialUserTo wbSocialUserTo);

    @PostMapping("/member/member/gitee/register")
    public R giteeRegister(@RequestBody GiteeUserTo giteeUserTo);

}
