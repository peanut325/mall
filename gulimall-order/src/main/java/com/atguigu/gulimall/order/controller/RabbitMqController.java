package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderReturnApplyEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@RestController
public class RabbitMqController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMessage")
    public String sendMessage() {
        OrderReturnApplyEntity entity = new OrderReturnApplyEntity();
        entity.setId(1L);
        entity.setDescPics("hello world!!!");
        entity.setCreateTime(new Date());
        // 发送消息为对象时，需要序列化
        rabbitTemplate.convertAndSend("hello.java.exchange", "hello.java", entity, new CorrelationData(UUID.randomUUID().toString()));
        return "ok";
    }

}
