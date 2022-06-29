package com.atguigu.gulimall.seckill.interceptor;

import com.atguigu.common.constant.auth.AuthConstant;
import com.atguigu.common.to.member.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/kill", uri);
        // 是秒杀需要登录
        if (match) {
            HttpSession session = request.getSession();
            MemberRespVo memberRespVo = (MemberRespVo) session.getAttribute(AuthConstant.LOGIN_USER);
            if (memberRespVo != null) {
                // 保存到ThreadLocal中
                threadLocal.set(memberRespVo);
                return true;
            } else {
                // 设置提示
                request.getSession().setAttribute("msg", "请先登录!");
                // 重定向去登录
                response.sendRedirect("http://auth.gulimalls.com/login.html");
                return false;
            }
        }

        // 不是秒杀请求直接放行
        return true;

    }
}
