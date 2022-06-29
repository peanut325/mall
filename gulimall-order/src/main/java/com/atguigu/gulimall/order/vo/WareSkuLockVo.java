package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.vo.OrderItemVo;
import lombok.Data;

import java.util.List;

/**
 * 锁定库存传输对象
 * 创建订单时封装所有订单项进行锁定
 */
@Data
public class WareSkuLockVo {
    private String orderSn;
    /**
     * 需要锁住的所有库存信息
     **/
    private List<OrderItemVo> locks;
}
