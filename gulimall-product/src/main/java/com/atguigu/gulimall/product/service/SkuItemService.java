package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.SkuItemVo;

import java.util.concurrent.ExecutionException;

public interface SkuItemService {
    SkuItemVo item(String skuId) throws ExecutionException, InterruptedException;
}
