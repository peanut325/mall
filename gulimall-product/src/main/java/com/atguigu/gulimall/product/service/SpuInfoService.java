package com.atguigu.gulimall.product.service;

import com.atguigu.common.to.order.SpuInfoTo;
import com.atguigu.gulimall.product.vo.SpuSaveVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author peanut
 * @email peanut@gmail.com
 * @date 2022-04-20 09:55:47
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 大保存（保存）
     * @param spuSaveVo
     */
    void saveSpuInfo(SpuSaveVo spuSaveVo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    /**
     * Spu带条件检索
     * @param params
     * @return
     */
    PageUtils queryPageCondition(Map<String, Object> params);

    void up(Long spuId);

    SpuInfoEntity getSpuBySkuId(Long skuId);
}

