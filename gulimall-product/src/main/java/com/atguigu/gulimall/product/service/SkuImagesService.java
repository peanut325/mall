package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author peanut
 * @email peanut@gmail.com
 * @date 2022-04-20 09:55:47
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据sku_id获取图片
     * @param skuId
     * @return
     */
    List<SkuImagesEntity> getImgBySkuId(String skuId);
}

