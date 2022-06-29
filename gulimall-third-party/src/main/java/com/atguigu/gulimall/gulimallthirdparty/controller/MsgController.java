package com.atguigu.gulimall.gulimallthirdparty.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.gulimallthirdparty.service.MsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class MsgController {

    @Autowired
    private MsgService msgService;

    @GetMapping("/sendMsg")
    public R sendMsg(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        boolean b = msgService.sendMessage(phone, code);
        return b ? R.ok() : R.error();
    }
}
