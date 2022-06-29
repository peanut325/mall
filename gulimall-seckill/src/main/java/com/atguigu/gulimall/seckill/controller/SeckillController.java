package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.to.seckill.SeckillSkuRedisTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) throws InterruptedException {
        String orderSn = seckillService.killSku(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }

    @ResponseBody
    @GetMapping("/getSkuInfo/{skuId}")
    public R getSkuInfoBySkuId(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo seckillSkuRedisTo = seckillService.getSkuInfoBySkuId(skuId);
        return R.ok().setData(seckillSkuRedisTo);
    }

    @ResponseBody
    @GetMapping("/getCurrentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> list = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(list);
    }

}
