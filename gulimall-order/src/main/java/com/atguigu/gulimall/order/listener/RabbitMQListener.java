package com.atguigu.gulimall.order.listener;

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

@Slf4j
@Service
@RabbitListener(queues = {"order.release.order.queue"})
public class RabbitMQListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void closeOrder(OrderEntity orderEntity, Message message, Channel channel) throws IOException {
        log.info("订单超过未支付时间，开始关闭订单操作...");
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception exception) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
