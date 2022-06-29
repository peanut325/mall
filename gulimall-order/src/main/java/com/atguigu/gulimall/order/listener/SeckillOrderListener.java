package com.atguigu.gulimall.order.listener;

import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RabbitListener(queues = {"order.seckill.order.queue"})
@Service
@Slf4j
public class SeckillOrderListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void closeOrder(SeckillOrderTo seckillOrderTo, Message message, Channel channel) throws IOException {
        log.info("准备创建秒杀订单...");
        try {
            orderService.createSeckillOrder(seckillOrderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception exception) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
