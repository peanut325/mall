package com.atguigu.gulimall.gulimallauthserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.member.UserRegisterTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.gulimallauthserver.client.MemberServiceClient;
import com.atguigu.common.constant.auth.AuthConstant;
import com.atguigu.gulimall.gulimallauthserver.vo.UserRegisterVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class RegisterController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MemberServiceClient memberServiceClient;

    /**
     * TODO 重定向携带数据，利用session原理。将数据放在session中。
     * 下一个页面使用session数据后，session就会失效
     * RedirectAttributes redirectAttributes:模拟重定向携带数据
     * TODO 分布式下的session问题
     *
     * @param userRegisterVo
     * @param bindingResult
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo userRegisterVo, BindingResult bindingResult,
                           // 重定向报错数据信息
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);

            // 校验出错，重定向
            return "redirect:http://auth.gulimalls.com/reg.html";

        }

        String code = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());
        if (!StringUtils.isEmpty(code)) {
            if (userRegisterVo.getCode().equals(code.split("_")[0])) {
                // 删除验证码
                redisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());
                // 远程调用注册服务
                UserRegisterTo userRegisterTo = new UserRegisterTo();
                BeanUtils.copyProperties(userRegisterVo, userRegisterTo);
                R r = memberServiceClient.register(userRegisterTo);
                if (r.getCode() == 0) {
                    // 注册成功，回到登录页
                    return "redirect:http://auth.gulimalls.com/login.html";
                } else {
                    HashMap<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData(new TypeReference<String>() {
                    }));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimalls.com/reg.html";
                }
            } else {
                HashMap<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimalls.com/reg.html";
            }
        } else {
            HashMap<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimalls.com/reg.html";
        }
    }

}
