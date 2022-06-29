package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * 提交订单返回结果
 */
@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;   // 0表示成功，1表示令牌校验失败，2表示金额校验不正确，3表示没有库存
}
