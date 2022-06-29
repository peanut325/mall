package com.atguigu.gulimall.gulimallauthserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.member.GiteeUserTo;
import com.atguigu.common.to.member.WBSocialUserTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.gulimallauthserver.client.MemberServiceClient;
import com.atguigu.common.constant.auth.AuthConstant;
import com.atguigu.gulimall.gulimallauthserver.entity.GiteeUser;
import com.atguigu.gulimall.gulimallauthserver.entity.ProviderToken;
import com.atguigu.gulimall.gulimallauthserver.service.GiteeProviderService;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.to.member.MemberRespVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    private GiteeProviderService giteeProviderService;

    @Autowired
    private MemberServiceClient memberServiceClient;

    @Value("${gitee.client_id}")
    private String clientId;

    @Value("${gitee.redirect_uri}")
    private String redirectUri;

    @Value("${gitee.client_secret}")
    private String clientSecret;

    // TODO Gitee登录
    @GetMapping("/success")
    public String callback(@RequestParam("code") String code,
                           @RequestParam("state") String state,
                           Model model) {
        //用户授权信息
        ProviderToken token = new ProviderToken();
        token.setClientId(clientId);
        token.setRedirectUri(redirectUri);
        token.setClientSecret(clientSecret);
        token.setCode(code);
        token.setState(state);
        //获取token和登录的用户信息
        String accessToken = giteeProviderService.getGiteeToken(token);
        GiteeUser giteeUser = giteeProviderService.getGiteeUser(accessToken);

        // 远程调用保存信息
        GiteeUserTo giteeUserTo = new GiteeUserTo();
        BeanUtils.copyProperties(giteeUser, giteeUserTo);
        R r = memberServiceClient.giteeRegister(giteeUserTo);
        if (r.getCode() == 0) {
            // 跳回首页
            return "redirect:http://gulimalls.com";
        } else {
            // 登录失败，调回登录页
            return "redirect:http://auth.gulimalls.com/login.html";
        }
    }

    /**
     * 授权回调页
     *
     * @param code 根据code换取Access Token，且code只能兑换一次Access Token
     */
    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        // 1.根据code换取Access Token
        Map<String, String> headers = new HashMap<>();
        Map<String, String> querys = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "2516299543");
        map.put("client_secret", "58124c5db70121821d778d446af28096");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimalls.com/oauth2.0/weibo/success");
        map.put("code", code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", headers, querys, map);

        // 判断是否授权成功
        if (response.getStatusLine().getStatusCode() == 200) {
            // 获取到了 accesstoken（此时使用一个对象封装）
            String json = EntityUtils.toString(response.getEntity());
            WBSocialUserTo wbSocialUserTo = JSON.parseObject(json, WBSocialUserTo.class);

            // 远程调用登录功能
            R r = memberServiceClient.loginWB(wbSocialUserTo);
            if (r.getCode() == 0) {
                MemberRespVo memberRespVo = r.getData(new TypeReference<MemberRespVo>() {
                });
                session.setAttribute(AuthConstant.LOGIN_USER, memberRespVo);
                // 跳回首页
                return "redirect:http://gulimalls.com";
            } else {
                // 登录失败，调回登录页
                return "redirect:http://auth.gulimalls.com/login.html";
            }
        } else {
            return "redirect:http://auth.gulimalls.com/login.html";
        }

    }
}
