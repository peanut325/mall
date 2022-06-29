package com.atguigu.gulimall.member.service;

import com.atguigu.common.to.member.GiteeUserTo;
import com.atguigu.common.to.member.UserLoginTo;
import com.atguigu.common.to.member.UserRegisterTo;
import com.atguigu.common.to.member.WBSocialUserTo;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author peanut
 * @email peanut@gmail.com
 * @date 2022-04-20 12:38:51
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterTo userRegisterTo);

    void existPhone(String phone) throws PhoneExistException;

    void existUsername(String userName) throws UsernameExistException;

    MemberEntity login(UserLoginTo userLoginTo);

    MemberEntity giteeLogin(GiteeUserTo giteeUserTo);

    MemberEntity login(WBSocialUserTo wbSocialUserTo);
}

