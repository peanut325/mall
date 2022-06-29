package com.atguigu.gulimall.gulimallauthserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.gulimallauthserver.entity.GiteeUser;
import com.atguigu.gulimall.gulimallauthserver.entity.ProviderToken;
import com.atguigu.gulimall.gulimallauthserver.service.GiteeProviderService;
import okhttp3.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class GiteeProviderServiceImpl implements GiteeProviderService {
    /**
     * 根据用户授权信息获取当前用户的 accessToken
     * 对用户进行授权，先创建一个GET请求，请求gitee中对应用户的访问令牌
     * @param providerToken 用户授权信息
     * @return 当前用户的 accessToken
     */
    public String getGiteeToken(ProviderToken providerToken) {
        //1. 创建http请求，构建请求体和请求url等，并向gitee发起请求
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON.toJSONString(providerToken), mediaType);
        String url = "https://gitee.com/oauth/token?grant_type=authorization_code&code=" + providerToken.getCode()
                + "&client_id=" + providerToken.getClientId()
                + "&redirect_uri=" + providerToken.getRedirectUri()
                + "&client_secret=" + providerToken.getClientSecret();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        //2. 获取gitee对应的响应消息，根据消息解析出用户的 access token
        try (Response response = client.newCall(request).execute()) {
            String tokenStr = Objects.requireNonNull(response.body()).string();
            String accessToken = tokenStr.split(",")[0].split(":")[1];
            accessToken = accessToken.substring(1, accessToken.length() - 1);
            //System.out.println("accessToken = " + accessToken);
            return accessToken;
        } catch (Exception e) {
            e.getStackTrace();
            //log.error("getAccessToken error,{}", accessTokenDTO, e);
        }
        return null;
    }

    /**
     * 根据用户的 access token 获取当前gitee用户的详细信息
     * @param accessToken 用户的访问令牌
     * @return gitee用户对象
     */
    public GiteeUser getGiteeUser(String accessToken) {
        //1. 构建http的GET请求，向gitee请求用户数据
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://gitee.com/api/v5/user?access_token=" + accessToken).build();

        //2. 获取gitee传回来的响应消息，根据消息解析出用户消息
        try {
            Response response = client.newCall(request).execute();
            //String string = response.body().string();
            String giteeUserStr = Objects.requireNonNull(response.body()).string();
            return JSON.parseObject(giteeUserStr, GiteeUser.class);
        } catch (Exception e) {
            //log.error("getGiteeUser error,{}", accessToken, e);
            e.getStackTrace();
        }
        return null;
    }
}
