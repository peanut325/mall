package com.atguigu.gulimall.member.interceptor;

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

//        // 此时查询订单状态查询不需要登录，所以放行库存回滚时远程查询订单状态的服务
//        String uri = request.getRequestURI();
//        AntPathMatcher antPathMatcher = new AntPathMatcher();
//        boolean match = antPathMatcher.match("/member/**", uri); // 查询订单消息
//        if (match) {
//            // 如果匹配直接放行
//            return true;
//        }
//
//        HttpSession session = request.getSession();
//        MemberRespVo memberRespVo = (MemberRespVo) session.getAttribute(AuthConstant.LOGIN_USER);
//        if (memberRespVo != null) {
//            // 保存到ThreadLocal中
//            threadLocal.set(memberRespVo);
//            return true;
//        }else {
//            // 设置提示
//            request.getSession().setAttribute("msg", "请先登录!");
//            // 重定向去登录
//            response.sendRedirect("http://auth.gulimalls.com/login.html");
//            return false;
//        }
        return true;
    }
}
