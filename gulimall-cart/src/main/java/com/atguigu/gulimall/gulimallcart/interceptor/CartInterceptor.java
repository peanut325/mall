package com.atguigu.gulimall.gulimallcart.interceptor;

import com.atguigu.common.constant.auth.AuthConstant;
import com.atguigu.common.constant.cart.CartConstant;
import com.atguigu.common.to.cart.UserInfoTo;
import com.atguigu.common.to.member.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberRespVo member = (MemberRespVo) session.getAttribute(AuthConstant.LOGIN_USER);
        if (member != null) {
            // 登录状态，封装用户ID，供controller使用
            userInfoTo.setUserId(member.getId());
        }

        // 获取当前请求游客用户标识user-key
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                // 如果cookie有user_key
                if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    // 获取user-key值封装到user，供controller使用
                    userInfoTo.setUserKey(cookie.getValue());
                    // 表示已经登录，不是零时用户
                    userInfoTo.setTempUser(true);
                    break;
                }
            }
        }

        // 没有零时用户一定分配一个零时用户
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            // 无游客标识，分配游客标识
            userInfoTo.setUserKey(UUID.randomUUID().toString());
        }

        // 封装用户信息（登录状态userId非空，游客状态userId空）
        threadLocal.set(userInfoTo);

        return true;
    }

    /**
     * 业务执行之后，让浏览器保存临时用户信息
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        // 如果是零时用户
        if (!userInfoTo.isTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("gulimalls.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
