package com.atguigu.gulimall.order.to;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建的订单TO对象
 * 1、订单
 * 2、订单项
 */
@Data
public class OrderCreateTo {
    private OrderEntity order;  // 订单
    private List<OrderItemEntity> orderItems; // 订单项
    /**
     * 订单计算的应付价格 TODO 是否可删？
     **/
    private BigDecimal payPrice;
    /**
     * 运费 TODO 是否可删？
     **/
    private BigDecimal fare;
}
