package com.atguigu.common.to.member;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * 注册使用的vo，使用JSR303校验
 */
@Data
public class UserRegisterTo {

    private String userName;

    private String password;

    private String phone;

}