package com.atguigu.gulimall.seckill.service;


import com.atguigu.common.to.seckill.SeckillSkuRedisTo;

import java.util.List;

public interface SeckillService {

    void uploadSeckillSkuLatest3Day();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuInfoBySkuId(Long skuId);

    String killSku(String killId, String key, Integer num) throws InterruptedException;
}
