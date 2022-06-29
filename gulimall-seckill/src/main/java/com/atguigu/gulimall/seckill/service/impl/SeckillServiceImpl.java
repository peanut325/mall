package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.seckill.SeckillConstant;
import com.atguigu.common.to.member.MemberRespVo;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.to.seckill.SeckillSessionWithSkusTo;
import com.atguigu.common.to.seckill.SeckillSkuRedisTo;
import com.atguigu.common.to.seckill.SkuInfoTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void uploadSeckillSkuLatest3Day() {
        R r = couponFeignService.getLates3DaySession();
        List<SeckillSessionWithSkusTo> seckillSessionWithSkusToList = r.getData(new TypeReference<List<SeckillSessionWithSkusTo>>() {
        });

        // 保存秒杀场的信息
        saveSessionInfos(seckillSessionWithSkusToList);

        // 保存每个秒杀场次的商品信息
        saveSessionSkuInfos(seckillSessionWithSkusToList);
    }

    /**
     * 返回当前可以参加的秒杀场次信息
     *
     * @return
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        // 获取当前时间
        long time = new Date().getTime();

        // 返回匹配该前缀的所有值 seckill:sessions:xxx
        Set<String> keys = redisTemplate.keys(SeckillConstant.SESSION_CACHE_PREFIX + "*");

        for (String key : keys) {
            String replace = key.replace(SeckillConstant.SESSION_CACHE_PREFIX, "");
            String[] array = replace.split("_");
            long start = Long.parseLong(array[0]);
            long end = Long.parseLong(array[1]);
            // 在时间范围之内
            if (time >= start && time <= end) {
                // 获取这个秒杀场次所需要的商品信息，0 —— -1 表示所有
                List<String> range = redisTemplate.opsForList().range(key, 0, -1);
                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SeckillConstant.SECKILL_CHARE_KEY);
                // 批量获取商品信息
                List<String> skuList = hashOps.multiGet(range);
                if (skuList != null) {
                    List<SeckillSkuRedisTo> collect = skuList.stream().map(skuItem -> {
                        SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject((String) skuItem, SeckillSkuRedisTo.class);
                        return seckillSkuRedisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuInfoBySkuId(Long skuId) {
        // 找到所有参与的商品
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SeckillConstant.SECKILL_CHARE_KEY);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            // 正则表达式是为了找到这个商品所有参加的场次
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                // 如果和正则表达式匹配
                if (Pattern.matches(regx, key)) {
                    String redisStr = hashOps.get(key);
                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject((String) redisStr, SeckillSkuRedisTo.class);

                    // 获取当前时间进行判断，如果秒杀已经开始需要知道随机码，如果未开始，不需要携带随机码
                    long time = new Date().getTime();
                    if (time < seckillSkuRedisTo.getStartTime() || time > seckillSkuRedisTo.getEndTime()) {
                        seckillSkuRedisTo.setRandomCode(null);
                    }
                    return seckillSkuRedisTo;
                }
            }
        }
        return null;
    }

    // TODO 上架秒杀商品的时候，每一个数据都有过期时间
    // TODO 秒杀后续的流程,简化了收货地址等信息。
    @Override
    public String killSku(String killId, String key, Integer num) {
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        // 绑定hash操作
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SeckillConstant.SECKILL_CHARE_KEY);
        String redisStr = hashOps.get(killId);
        if (!StringUtils.isEmpty(redisStr)) {
            SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject((String) redisStr, SeckillSkuRedisTo.class);

            // 开始进行时间合法性校验
            long time = new Date().getTime();
            Long startTime = seckillSkuRedisTo.getStartTime();
            Long endTime = seckillSkuRedisTo.getEndTime();
            if (time >= startTime && time <= endTime) {
                // 开始进行随机码的校验，防止别人恶意刷商品
                // 将skuId的验证一起校验
                String randomCode = seckillSkuRedisTo.getRandomCode();
                // 拼接起来，和redis做判断
                String skuId = seckillSkuRedisTo.getPromotionSessionId().toString() + "_" + seckillSkuRedisTo.getSkuId();
                if (randomCode.equals(key) && killId.equals(skuId)) {
                    // 如果都匹配，校验数量是否合理
                    Integer seckillLimit = seckillSkuRedisTo.getSeckillLimit();
                    if (num <= seckillLimit) {
                        // 数量合理，此时需要检验用户是否已经买过，防止用户多次购买
                        // 这里也是保证了幂等性，买一次就需要一个幂等性字段
                        // 解决：如果秒杀成功，那么使用该用户的id和商品id在redis中占一个坑
                        String redisKey = memberRespVo.getId() + "_" + skuId;
                        // 占坑，使用setnx操作（类似于加锁），并设置过期时间
                        // 生成过期时间
                        long ttl = endTime - time;
                        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (ifAbsent) {
                            // 占坑成功，说明从来没有买过，可以进行购买
                            // 此时判断是否能获取信号量（也就是是否还有库存）
                            RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + randomCode);
                            // 这里使用tryAcquire，而不是acquire的元婴是，后者如果没有信号量会一致阻塞获取，而不释放
                            // 注意扣取的信号量
                            try {
                                boolean acquire = semaphore.tryAcquire(num);
                                if (acquire) {
                                    // 成功之后，发送到消息队列，订单慢慢处理
                                    // 使用rabbitMQ消峰
                                    SeckillOrderTo order = new SeckillOrderTo();
                                    String orderSn = IdWorker.getTimeId();// 订单号
                                    order.setOrderSn(orderSn);// 订单号
                                    order.setMemberId(memberRespVo.getId());// 用户ID
                                    order.setNum(num);// 商品上来给你
                                    order.setPromotionSessionId(seckillSkuRedisTo.getPromotionSessionId());// 场次id
                                    order.setSkuId(seckillSkuRedisTo.getSkuId());// 商品id
                                    order.setSeckillPrice(seckillSkuRedisTo.getSeckillPrice());// 秒杀价格
                                    // TODO 需要保证可靠消息，发送者确认+消费者确认（本地事务的形式）
                                    rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", order);
                                    long time1 = new Date().getTime();
                                    log.info("秒杀总共耗时：{}", time1 - time);
                                    return orderSn;
                                }
                            } catch (Exception e) {
                                return null;
                            }
                        } else {
                            // 占坑失败
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    private void saveSessionInfos(List<SeckillSessionWithSkusTo> list) {
        // 存储在redis中的List数据类型
        // key:start_end value:skuList
        list.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SeckillConstant.SESSION_CACHE_PREFIX + startTime + "_" + endTime;// 场次的key
            // 没有才放入，保证幂等性
            if (!redisTemplate.hasKey(key)) {
                List<String> collect = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionWithSkusTo> list) {
        list.stream().forEach(session -> {

            // 准备hash操作
            BoundHashOperations<String, Object, Object> boundHashOps = redisTemplate.boundHashOps(SeckillConstant.SECKILL_CHARE_KEY);

            session.getRelationSkus().stream().forEach(item -> {
                // 生成随机码作为标识，防止别人恶意扣库存
                String token = UUID.randomUUID().toString().replace("-", "");

                // redis中没有才保存，保证幂等性
                if (!boundHashOps.hasKey(item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString())) {


                    // 保存在redis中的对象
                    SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();

                    // 远程调用，查询sku的信息
                    R r = productFeignService.info(item.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoTo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoTo>() {
                        });
                        seckillSkuRedisTo.setSkuInfo(skuInfo);
                    }

                    // 拷贝属性
                    BeanUtils.copyProperties(item, seckillSkuRedisTo);

                    // 设置限购数量
                    seckillSkuRedisTo.setSeckillLimit(item.getSeckillLimit().intValue());

                    // 设置商品的开始和结束时间
                    seckillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(session.getEndTime().getTime());

                    // 设置随机码
                    seckillSkuRedisTo.setRandomCode(token);

                    // 存入redis
                    String jsonString = JSON.toJSONString(seckillSkuRedisTo);
                    boundHashOps.put(item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString(), jsonString);

                    // 使用库存作为信号量，信号量用完，就不能在扣库存，起到了限流的功能
                    // 使用redisson作为信号量框架
                    // redis中没有key，才加入信号量，保证幂等性
                    RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(item.getSeckillCount().intValue());
                }
            });
        });
    }

}
