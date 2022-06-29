package com.atguigu.gulimall.gulimallauthserver.service;

import com.atguigu.gulimall.gulimallauthserver.entity.GiteeUser;
import com.atguigu.gulimall.gulimallauthserver.entity.ProviderToken;

public interface GiteeProviderService {
    String getGiteeToken(ProviderToken token);

    GiteeUser getGiteeUser(String accessToken);
}
