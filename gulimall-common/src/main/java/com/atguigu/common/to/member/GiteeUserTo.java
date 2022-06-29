package com.atguigu.common.to.member;

import lombok.Data;

@Data
public class GiteeUserTo {
    private Integer id; //NOT NULL
    private String name; //NOT NULL
    private String email; //可能为空
}