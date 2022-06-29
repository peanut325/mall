package com.atguigu.gulimall.ware.service;

import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.to.order.OrderTo;
import com.atguigu.common.to.ware.SkuHasStockTo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author peanut
 * @email peanut@gmail.com
 * @date 2022-04-20 12:56:02
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPageCondition(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockTo> getSkusHasStock(List<Long> skuIds);

    Boolean lockOrderStock(WareSkuLockVo wareSkuLockVo);

    void handleStockLockedRelease(StockLockedTo stockLockedTo) throws Exception;

    void handleStockLockedReleaseByOrderSend(OrderTo orderTo);
}

