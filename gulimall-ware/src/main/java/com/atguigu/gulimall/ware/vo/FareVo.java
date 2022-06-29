package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: wan
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
