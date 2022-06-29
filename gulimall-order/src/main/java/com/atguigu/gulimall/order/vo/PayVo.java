package com.atguigu.gulimall.order.vo;

import lombok.Data;

@Data
public class PayVo {

    private String out_trade_no; // 商户订单号 必填
    private String subject; // 订单名称 必填
    private String total_amount;  // 付款金额 必填
    private String body; // 商品描述 可空
    private String return_url;// 同步回调地址
    private String notify_url;// 异步回调地址

    // 前端传参
    private String orderSn;// 订单号
}
