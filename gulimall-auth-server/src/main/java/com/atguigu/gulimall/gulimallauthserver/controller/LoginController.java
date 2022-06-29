package com.atguigu.gulimall.gulimallauthserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.to.member.MemberRespVo;
import com.atguigu.common.to.member.UserLoginTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.gulimallauthserver.client.MemberServiceClient;
import com.atguigu.common.constant.auth.AuthConstant;
import com.atguigu.gulimall.gulimallauthserver.client.ThirdPartyFeignService;
import com.atguigu.gulimall.gulimallauthserver.vo.UserLoginVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MemberServiceClient memberServiceClient;

    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        Object attribute = session.getAttribute(AuthConstant.LOGIN_USER);
        if (attribute == null) {
            // 没登录
            return "login.html";
        }
        return "redirect:http://gulimalls.com";
    }

    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        // TODO 接口防刷

        // 60s防止用户再次发送请求
        String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);

        if (!StringUtils.isEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {   // 一分钟之内就不发送
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        // 根据时间生成验证码，存入redis需要带上时间，发短信不需要
        // String code = UUID.randomUUID().toString().substring(0, 4);
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
        String codeToRedis = code + "_" + System.currentTimeMillis();

        // 存入redis
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX + phone, codeToRedis, 10, TimeUnit.MINUTES);

        thirdPartyFeignService.sendMsg(phone, String.valueOf(code));
        return R.ok();
    }

    @PostMapping("login")
    public String login(UserLoginVo userLoginVo, RedirectAttributes redirectAttributes, HttpSession session) {
        // 远程调用
        UserLoginTo userLoginTo = new UserLoginTo();
        BeanUtils.copyProperties(userLoginVo, userLoginTo);
        R r = memberServiceClient.login(userLoginTo);
        if (r.getCode() == 0) {
            // 登录成功
            MemberRespVo memberRespVo = r.getData("data", new TypeReference<MemberRespVo>() {
            });
            session.setAttribute(AuthConstant.LOGIN_USER, memberRespVo);
            return "redirect:http://gulimalls.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.getData(new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimalls.com/login.html";
        }
    }

}
