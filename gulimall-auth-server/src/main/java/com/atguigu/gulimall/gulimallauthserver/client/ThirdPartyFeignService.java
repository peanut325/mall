package com.atguigu.gulimall.gulimallauthserver.client;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third-party")
@Component
public interface ThirdPartyFeignService {

    @GetMapping("/sms/sendMsg")
    public R sendMsg(@RequestParam("phone") String phone, @RequestParam("code") String code);

}
