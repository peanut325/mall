package com.atguigu.gulimall.product.client;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-seckill")
public interface SeckillFeignService {

    @GetMapping("/getSkuInfo/{skuId}")
    public R getSkuInfoBySkuId(@PathVariable("skuId") Long skuId);

}
