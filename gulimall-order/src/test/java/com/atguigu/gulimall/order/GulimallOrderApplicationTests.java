package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderReturnApplyEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.channels.Channel;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void directExchange() {
        DirectExchange directExchange = new DirectExchange("hello.java.exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
    }

    @Test
    public void createQueue() {
        Queue queue = new Queue("hello.java.queue", true, false, false);
        amqpAdmin.declareQueue(queue);
    }

    @Test
    public void Binding() {
        Binding binding = new Binding("hello.java.queue",
                Binding.DestinationType.QUEUE,
                "hello.java.exchange",
                "hello.java", null);
        amqpAdmin.declareBinding(binding);
    }

    @Test
    public void sendMessage() {
        OrderReturnApplyEntity entity = new OrderReturnApplyEntity();
        entity.setId(1L);
        entity.setDescPics("hello world!!!");
        entity.setCreateTime(new Date());
        // 发送消息为对象时，需要序列化
        rabbitTemplate.convertAndSend("hello.java.exchange2", "hello.java", entity, new CorrelationData(UUID.randomUUID().toString()));
    }

}
