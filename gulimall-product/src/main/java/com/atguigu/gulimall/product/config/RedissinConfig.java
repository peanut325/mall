package com.atguigu.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissinConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissson() {
        // 默认连接地址 127.0.0.1:6379
//        RedissonClient redisson = Redisson.create();

        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.241.130:6379");
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }


}
