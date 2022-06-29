package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.ware.WareConstant;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.atguigu.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        // 此时没有采购单id，则先进行保存
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        // 保存采购单需求细节
        // 采购单状态是0或者1才可以合并
        PurchaseEntity purchaseEntityById = this.getById(purchaseId);
        if (purchaseEntityById.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                || purchaseEntityById.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
            List<Long> items = mergeVo.getItems();
            Long finalPurchaseId = purchaseId;
            List<PurchaseDetailEntity> collect = items.stream().map(item -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(item);
                purchaseDetailEntity.setPurchaseId(finalPurchaseId); // 保存采购单的id
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());

            purchaseDetailService.updateBatchById(collect);
        }

    }

    @Override
    public void received(List<Long> ids) {
        // 确认当前采购单是新建或者已分配的状态
        List<PurchaseEntity> purchaseEntityList = ids.stream().map(id -> {
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity;
        }).filter(purchaseEntity ->
                purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                        || purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
                .map(purchaseEntity -> {
                    purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    return purchaseEntity;
                })
                .collect(Collectors.toList());

        // 修改采购单状态
        this.updateBatchById(purchaseEntityList);

        // 改变采购项的状态
        purchaseEntityList.forEach(purchaseEntity -> {
            List<PurchaseDetailEntity> purchaseDetailEntityList = purchaseDetailService.getPurchaseDetailByPurchaseId(purchaseEntity.getId());
            List<PurchaseDetailEntity> collect = purchaseDetailEntityList.stream().map(purchaseDetailEntity -> {
                PurchaseDetailEntity purchaseDetailEntity1 = new PurchaseDetailEntity();
                purchaseDetailEntity1.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                purchaseDetailEntity1.setId(purchaseDetailEntity.getId());
                return purchaseDetailEntity1;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect);
        });

    }

    @Override
    @Transactional
    public void done(PurchaseDoneVo doneVo) {
        boolean flag = true; // 标记这个采购单是否有异常

        List<PurchaseDetailEntity> purchaseDetailEntityUpdateList = new ArrayList<>();
        for (PurchaseItemDoneVo item : doneVo.getItems()) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.HASERROR.getCode()) {
                flag = false;
                purchaseDetailEntity.setStatus(item.getStatus());
            } else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                // 将成功采购的进行入库
                PurchaseDetailEntity entity = purchaseDetailService.getOne(new QueryWrapper<PurchaseDetailEntity>().eq("sku_id", item.getItemId()));
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            purchaseDetailEntity.setWareId(item.getItemId());
            // TODO 异常原因可以添加reason字段
//            purchaseDetailEntityUpdateList.add(purchaseDetailEntity);
            purchaseDetailService.update(purchaseDetailEntity, new QueryWrapper<PurchaseDetailEntity>().eq("sku_id", item.getItemId()));
        }

        // 批量更新
//        purchaseDetailService.updateBatchById(purchaseDetailEntityUpdateList);

        // 修改采购单
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(doneVo.getId());
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        this.updateById(purchaseEntity);

    }

}