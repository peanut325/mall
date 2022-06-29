package com.atguigu.gulimall.gulimallsearch.client;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-product")
public interface ProductFeignClient {

    @RequestMapping("/product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);

    @RequestMapping("/product/brand/getInfoByBrandIds")
    public R getInfoByBrandIds(@RequestParam("brandIds") List<Long> brandIds);

}
