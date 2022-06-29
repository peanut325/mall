package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.to.member.GiteeUserTo;
import com.atguigu.common.to.member.UserLoginTo;
import com.atguigu.common.to.member.UserRegisterTo;
import com.atguigu.common.to.member.WBSocialUserTo;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(UserRegisterTo userRegisterTo) {
        MemberEntity memberEntity = new MemberEntity();

        // 设置默认等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(memberLevelEntity.getId());

        // 检查手机号和用户名是否唯一
        this.existPhone(userRegisterTo.getPhone());
        this.existUsername(userRegisterTo.getUserName());

        memberEntity.setMobile(userRegisterTo.getPhone());
        memberEntity.setUsername(userRegisterTo.getUserName());
        memberEntity.setNickname(userRegisterTo.getUserName());

        // 存储密码，使用盐值加密(BCryptPasswordEncoder提供的类)
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(userRegisterTo.getPassword());
        memberEntity.setPassword(encode);

        // TODO 其他信息

        baseMapper.insert(memberEntity);
    }

    @Override
    public void existPhone(String phone) throws PhoneExistException {
        int mobile = this.count(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void existUsername(String userName) throws UsernameExistException {
        int username = this.count(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (username > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(UserLoginTo userLoginTo) {
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", userLoginTo.getLoginacct()).eq("phone", userLoginTo.getLoginacct()));

        if (memberEntity != null) {
            String password = userLoginTo.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String encode = bCryptPasswordEncoder.encode(password);
            if (encode.matches(memberEntity.getPassword())) {
                return memberEntity;
            } else {
                return null;
            }
        } else {
            // 登录失败
            return null;
        }
    }

    @Override
    public MemberEntity giteeLogin(GiteeUserTo giteeUserTo) {
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("gitee_id", giteeUserTo.getId()));
        // 注册
        if (memberEntity == null) {
            // 未注册
            MemberEntity member = new MemberEntity();
            member.setGiteeId(giteeUserTo.getId());
            member.setEmail(giteeUserTo.getEmail());
            member.setNickname(giteeUserTo.getName());
            baseMapper.insert(member);
            return member;
        } else {
            // 已注册，直接返回
            // gitee我只获取了id和name，其他不进行更新，只更新name
            MemberEntity memberEntity1 = new MemberEntity();
            memberEntity1.setEmail(giteeUserTo.getEmail());
            memberEntity1.setNickname(giteeUserTo.getName());
            baseMapper.update(memberEntity1, new UpdateWrapper<MemberEntity>().eq("gitee_id", giteeUserTo.getId()));
            return memberEntity;
        }
    }

    @Override
    public MemberEntity login(WBSocialUserTo wbSocialUserTo) {
        String uid = wbSocialUserTo.getUid();
        // 数据库中是否有用户
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity != null) {
            // 已注册
            MemberEntity entity = new MemberEntity();
            entity.setId(memberEntity.getId());
            // 更新token
            entity.setAccessToken(wbSocialUserTo.getAccess_token());
            entity.setExpiresIn(wbSocialUserTo.getExpires_in());
            baseMapper.updateById(memberEntity);
            memberEntity.setAccessToken(wbSocialUserTo.getAccess_token());
            memberEntity.setExpiresIn(wbSocialUserTo.getExpires_in());
            return memberEntity;
        } else {
            // 进行注册
            MemberEntity registerMember = new MemberEntity();
            try {
                Map<String, String> query = new HashMap<>();
                query.put("access_token", wbSocialUserTo.getAccess_token());
                query.put("uid", wbSocialUserTo.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", new HashMap<String, String>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    //查询成功
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    String profileImageUrl = jsonObject.getString("profile_image_url");
                    // 封装注册信息
                    registerMember.setNickname(name);
                    registerMember.setGender("m".equals(gender) ? 1 : 0);
                    registerMember.setHeader(profileImageUrl);
                    registerMember.setCreateTime(new Date());
                }
            } catch (Exception exception) {
            }
            // 不管是否出现异常，这三个字段都要保存
            registerMember.setSocialUid(wbSocialUserTo.getUid());
            registerMember.setAccessToken(wbSocialUserTo.getAccess_token());
            registerMember.setExpiresIn(wbSocialUserTo.getExpires_in());
            baseMapper.insert(registerMember);
            return registerMember;
        }
    }


}