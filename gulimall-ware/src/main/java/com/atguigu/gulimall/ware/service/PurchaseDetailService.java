package com.atguigu.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author peanut
 * @email peanut@gmail.com
 * @date 2022-04-20 12:56:02
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPageCondition(Map<String, Object> params);

    /**
     * 根据purchaseId查找采购项
     * @param id
     * @return
     */
    List<PurchaseDetailEntity> getPurchaseDetailByPurchaseId(Long id);
}

