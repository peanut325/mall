package com.atguigu.gulimall.order.service;

import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author peanut
 * @email peanut@gmail.com
 * @date 2022-04-20 12:44:20
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 返回订单确认数据
     *
     * @return
     */
    OrderConfirmVo getOrderConfirm() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo getSubmitOrderResponseVo(OrderSubmitVo orderSubmitVo);

    OrderEntity getOrderStatus(String orderSn);

    void closeOrder(OrderEntity orderEntity);

    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo payAsyncVo);

    void createSeckillOrder(SeckillOrderTo seckillOrderTo);
}

