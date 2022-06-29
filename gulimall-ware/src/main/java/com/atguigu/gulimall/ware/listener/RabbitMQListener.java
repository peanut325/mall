package com.atguigu.gulimall.ware.listener;

import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.to.order.OrderTo;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RabbitListener(queues = {"stock.release.stock.queue"})
public class RabbitMQListener {

    @Autowired
    private WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        log.info("收到自动回滚消息，开始处理回滚操作");
        try {
            wareSkuService.handleStockLockedRelease(stockLockedTo);
            // 只要没有异常，就确认收到消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception exception) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    @RabbitHandler
    public void handleStockLockedReleaseByOrderSend(OrderTo orderTo, Message message, Channel channel) throws IOException {
        log.info("收到订单发送关单后的回滚消息，开始处理回滚操作");
        try {
            wareSkuService.handleStockLockedReleaseByOrderSend(orderTo);
            // 只要没有异常，就确认收到消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception exception) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
