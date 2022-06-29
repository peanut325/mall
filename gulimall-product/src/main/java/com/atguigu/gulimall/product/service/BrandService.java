package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.BrandEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author peanut
 * @email peanut@gmail.com
 * @date 2022-04-20 10:40:05
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 更新品牌，同步修改关联表
     * @param brand
     */
    void updateDetails(BrandEntity brand);

    List<BrandEntity> getInfoByBrandIds(List<Long> brandIds);
}

