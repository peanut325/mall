package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.common.constant.seckill.SeckillConstant;
import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品的定时上架;
 * 每天晚上3点;上架最近三天需要秒杀的商品。
 * 当天00:0e;00 - 23:59:59
 * 明天00:00:00 - 23:59:59
 * 后天00:00;00 - 23:59:59
 */
@Slf4j
@Service
public class SeckillScheduled {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedissonClient redissonClient;

    // 每分钟一次
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        log.info("正在执行上架任务...");
        // 加入分布式锁，反正多个服务器一起上架
        RLock lock = redissonClient.getLock(SeckillConstant.UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Day();
        } catch (Exception exception) {
            lock.unlock();
        }
    }

}
