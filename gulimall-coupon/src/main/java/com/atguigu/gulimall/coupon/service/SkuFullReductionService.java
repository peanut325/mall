package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.to.product.SkuReductionTo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author peanut
 * @email peanut@gmail.com
 * @date 2022-04-20 11:34:44
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存sku的优惠、满减等信息
     * @param skuReductionTo
     */
    void saveSkuReductionTo(SkuReductionTo skuReductionTo);
}

