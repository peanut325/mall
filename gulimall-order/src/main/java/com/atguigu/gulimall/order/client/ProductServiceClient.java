package com.atguigu.gulimall.order.client;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-product")
public interface ProductServiceClient {

    @GetMapping("/product/spuinfo/getSpuBySkuId/{skuId}")
    public R getSpuBySkuId(@PathVariable("skuId") Long skuId);

}
