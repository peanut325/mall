package com.atguigu.gulimall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            /**
             * 重写apply方法
             * @param requestTemplate 新请求
             */
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 获取原来的请求
                // 拿到刚进来的请求RequestContextHolder
                // ServletRequestAttributes是spring封装的，AbstractRequestAttributes（RequestContextHolder返回类型）子类
                // 如果不使用的化，也可以通过在controller中接收request，并通过ThreadLocal也可以共享，只是这里spring提供了可以直接获取的类
                ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (servletRequestAttributes != null) {
                    HttpServletRequest request = servletRequestAttributes.getRequest();
                    if (request != null) {
                        // 同步请求cookie，老请求放到新请求中
                        // 所有Cookie被封装在请求头中，所以吧Cookie放到新请求即可
                        String cookie = request.getHeader("Cookie");
                        requestTemplate.header("Cookie", cookie);
                    }
                }
            }
        };
    }

}
