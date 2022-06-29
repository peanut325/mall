package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.service.SkuItemService;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {

    @Autowired
    private SkuItemService skuItemService;

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") String skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = skuItemService.item(skuId);
        model.addAttribute("item", skuItemVo);
        return "item";
    }

}
